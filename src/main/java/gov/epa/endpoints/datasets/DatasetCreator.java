package gov.epa.endpoints.datasets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gson.Gson;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropPropertyService;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropPropertyServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropUnitService;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropUnitServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueService;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointContributor;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointContributorService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointContributorServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.PropertyService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.PropertyServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.UnitService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.UnitServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesServiceImpl;
import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.endpoints.datasets.DatasetParams.MappingParams;
import gov.epa.endpoints.datasets.dsstox_mapping.DsstoxMapper;
import gov.epa.util.MathUtil;
import gov.epa.web_services.DescriptorWebService;
import gov.epa.web_services.DescriptorWebService.DescriptorCalculationResponse;
import gov.epa.web_services.DescriptorWebService.DescriptorInfoResponse;
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer;
import gov.epa.web_services.standardizers.Standardizer;
import gov.epa.web_services.standardizers.Standardizer.BatchStandardizeResponse;
import gov.epa.web_services.standardizers.Standardizer.BatchStandardizeResponse.Standardization;
import gov.epa.web_services.standardizers.Standardizer.BatchStandardizeResponseWithStatus;
import gov.epa.web_services.standardizers.Standardizer.StandardizeResponse;
import gov.epa.web_services.standardizers.Standardizer.StandardizeResponseWithStatus;
import kong.unirest.Unirest;

public class DatasetCreator {
	
	private CompoundService compoundService;
	private DescriptorSetService descriptorSetService;
	private DescriptorValuesService descriptorValuesService;
	
	private UnitService unitService;
	private PropertyService propertyService;
	private DatasetService datasetService;
	private DataPointService dataPointService;
	private DataPointContributorService dataPointContributorService;
	
	private ExpPropPropertyService expPropPropertyService;
	private ExpPropUnitService expPropUnitService;
	private PropertyValueService propertyValueService;

	private String lanId;
	private Standardizer standardizer;
	private DescriptorWebService descriptorWebService;
	
	private String standardizerName;
	private DescriptorSet descriptorSet;
	
	private Map<String, String> finalUnits;
	private Set<String> physchemPropertyNames;
	private Set<String> acceptableAtoms;
	
	private static final Logger logger = LogManager.getLogger(DatasetCreator.class);
	
	public DatasetCreator(Standardizer standardizer, DescriptorWebService descriptorWebService, String lanId) 
			throws ConstraintViolationException {
		this.compoundService = new CompoundServiceImpl();
		this.descriptorSetService = new DescriptorSetServiceImpl();
		this.descriptorValuesService = new DescriptorValuesServiceImpl();
		
		this.unitService = new UnitServiceImpl();
		this.propertyService = new PropertyServiceImpl();
		this.datasetService = new DatasetServiceImpl();
		this.dataPointService = new DataPointServiceImpl();
		this.dataPointContributorService = new DataPointContributorServiceImpl();
		
		this.expPropPropertyService = new ExpPropPropertyServiceImpl();
		this.expPropUnitService = new ExpPropUnitServiceImpl();
		this.propertyValueService = new PropertyValueServiceImpl();
		
		this.lanId = lanId;
		this.standardizer = standardizer;
		this.descriptorWebService = descriptorWebService;
		
		this.standardizerName = this.standardizer==null ? 
				DevQsarConstants.STANDARDIZER_NONE : this.standardizer.standardizerName;
		
		try {
			Unirest.config().followRedirects(true).socketTimeout(000).connectTimeout(000);
		} catch (Exception e) {
			logger.warn("Unirest configuration failed: " + e.getMessage());
		}

		Logger apacheLogger = LogManager.getLogger("org.apache.http");
		apacheLogger.setLevel(Level.WARN);
		Logger mapperLogger = LogManager.getLogger(DsstoxMapper.class);
		mapperLogger.setLevel(Level.WARN);
		Logger mpvLogger = LogManager.getLogger(MappedPropertyValue.class);
		mpvLogger.setLevel(Level.WARN);
		logger.setLevel(Level.WARN);
		
		DescriptorInfoResponse descriptorInfoResponse = descriptorWebService.callInfo().getBody();
		String descriptorSetName = descriptorInfoResponse.name + " " + descriptorInfoResponse.version;
		
		descriptorSet = descriptorSetService.findByName(descriptorSetName);
		if (descriptorSet==null) {
			descriptorSet = new DescriptorSet(descriptorSetName, 
					descriptorInfoResponse.description, 
					descriptorInfoResponse.headersTsv,
					lanId);
			descriptorSetService.create(descriptorSet);
		}
		
//		this.gson = new GsonBuilder().disableHtmlEscaping().create();
		this.acceptableAtoms = DevQsarConstants.getAcceptableAtomsSet();
		this.finalUnits = DevQsarConstants.getFinalUnitsMap();
		
		physchemPropertyNames = expPropPropertyService.findByPropertyCategoryName("Physchem").stream()
				.map(p -> p.getName())
				.collect(Collectors.toSet());
	}
	
	public DatasetCreator(DescriptorWebService descriptorWebService, String lanId) {
		this(null, descriptorWebService, lanId); // If initialized without a standardizer web service, uses DSSTox QSAR-ready SMILES
	}
	
	private List<MappedPropertyValue> mapPropertyValuesToDsstoxRecords(List<PropertyValue> propertyValues, DatasetParams params) {
		List<MappedPropertyValue> mappedPropertyValues = new ArrayList<MappedPropertyValue>();
		
		String finalUnitName = finalUnits.get(params.propertyName);
		params.mappingParams.omitSalts = physchemPropertyNames.contains(params.propertyName); // Omit salts for physchem properties

		try {
			DsstoxMapper dsstoxMapper = new DsstoxMapper(params, standardizer, finalUnitName, params.mappingParams.omitSalts, 
					acceptableAtoms, lanId);
			
			if (params.mappingParams.dsstoxMappingId.equals(DevQsarConstants.MAPPING_BY_CASRN)
					|| params.mappingParams.dsstoxMappingId.equals(DevQsarConstants.MAPPING_BY_DTXSID)
					|| params.mappingParams.dsstoxMappingId.equals(DevQsarConstants.MAPPING_BY_DTXCID)
					|| params.mappingParams.dsstoxMappingId.equals(DevQsarConstants.MAPPING_BY_LIST)) {
				mappedPropertyValues = dsstoxMapper.map(propertyValues);
			} else {
				// TODO implementation of:
				// Mapping by provided DTXRID?
				logger.error("Mapping strategy " + params.mappingParams.dsstoxMappingId + " not implemented");
				return null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mappedPropertyValues;
	}
	
	private void standardize(List<MappedPropertyValue> mappedPropertyValues) {
		// Go through all compounds retrieved from DSSTox
		List<String> smilesToBatchStandardize = new ArrayList<String>();
		for (MappedPropertyValue mpv:mappedPropertyValues) {
			DsstoxRecord dr = mpv.dsstoxRecord;

			// Check (by DTXCID) if compound already has a standardization
			Compound compound = compoundService.findByDtxcidAndStandardizer(dr.dsstoxCompoundId, standardizerName);

			if (compound!=null) {
				// If already standardized, set standardization and mark record as standardized
				// Additionally store mapped compound so we don't have to query it later
				mpv.compound = compound;
				mpv.standardizedSmiles = compound.getCanonQsarSmiles();
				mpv.isStandardized = true;
				logger.trace(mpv.id + ": Found existing standardization: " + dr.smiles + " -> " + mpv.standardizedSmiles);
			} else if (standardizer==null) {
				// If using DSSTox QSAR-ready SMILES, just set it and mark standardized
				mpv.standardizedSmiles = dr.qsarReadySmiles;
				mpv.isStandardized = true;
				logger.debug(mpv.id + ": Found DSSTox standardization: " + dr.smiles + " -> " + mpv.standardizedSmiles);
			} else {
				if (standardizer.useBatchStandardize) {
					// Add SMILES to list for batch standardization
					smilesToBatchStandardize.add(dr.smiles);
				} else {
					// Standardize one at a time
					StandardizeResponseWithStatus standardizeResponse = standardizer.callStandardize(dr.smiles);
					if (standardizeResponse.status==200) {
						StandardizeResponse standardizeResponseData = standardizeResponse.standardizeResponse;
						if (standardizeResponseData.success) {
							mpv.standardizedSmiles = standardizeResponseData.qsarStandardizedSmiles;
							mpv.isStandardized = true;
							logger.debug(mpv.id + ": Standardized: " + dr.smiles + " -> " + mpv.standardizedSmiles);
						} else {
							logger.warn(mpv.id + ": Standardization failed for SMILES: " + dr.smiles);
						}
					} else {
						logger.warn(mpv.id + ": Standardizer HTTP response failed for SMILES: " 
								+ dr.smiles + " with code " + standardizeResponse.status);
					}
				}
			}
		}
		
		if (standardizer==null || smilesToBatchStandardize.isEmpty()) { 
			// Don't call web service if using DSSTox QSAR-ready SMILES
			// Don't call web service if no SMILES to standardize
			return;
		}
		
		// Send list through batch standardization and get output as a map from input SMILES to standardized SMILES
		Map<String, String> standardizedSmilesMap = batchStandardizeSmiles(smilesToBatchStandardize);
		// If standardization failed, don't try to get results
		if (standardizedSmilesMap.isEmpty()) { return; }
		
		for (MappedPropertyValue mpv:mappedPropertyValues) {
			// Skip records without structure or already standardized
			DsstoxRecord dr = mpv.dsstoxRecord;
			if (dr.smiles==null || dr.smiles.isBlank()) { continue; }
			if (mpv.isStandardized) { continue; }
			
			// Get standardization from map
			mpv.standardizedSmiles = standardizedSmilesMap.get(dr.smiles);
			mpv.isStandardized = true;
		}
	}

	private Map<String, String> batchStandardizeSmiles(List<String> smilesToBatchStandardize) {
		Map<String, String> standardizedSmilesMap = new HashMap<String, String>();
		BatchStandardizeResponseWithStatus batchStandardizeResponse = standardizer.callBatchStandardize(smilesToBatchStandardize);
		
		if (batchStandardizeResponse.status==200) {
			BatchStandardizeResponse batchStandardizeResponseData = batchStandardizeResponse.batchStandardizeResponse;
			if (batchStandardizeResponseData.success) {
				List<Standardization> standardizations = batchStandardizeResponseData.standardizations;
				for (Standardization standardization:standardizations) {
					standardizedSmilesMap.put(standardization.smiles, standardization.standardizedSmiles);
				}
			} else {
				logger.error("Batch standardization failed");
			}
		} else {
			logger.error("Batch standardizer HTTP response failed with code " + batchStandardizeResponse.status);
		}
		
		return standardizedSmilesMap;
	}
	
	// TODO logging
	private void calculateDescriptors(List<MappedPropertyValue> mappedPropertyValues) {
		for (MappedPropertyValue mpv:mappedPropertyValues) {
			DsstoxRecord dr = mpv.dsstoxRecord;
			
			// Upload all new compounds, even the ones with a null standardization
			// That way we don't end up retrying the standardization every time
			if (mpv.isStandardized && mpv.compound==null) {
				// Convert blank standardizations to null for consistency
				// TODO Check for valid SMILES with Indigo or CDK instead? (In case of error strings)
				if (mpv.standardizedSmiles!=null && mpv.standardizedSmiles.isBlank()) {
					mpv.standardizedSmiles = null;
				}
				
				Compound compound = new Compound(dr.dsstoxCompoundId, mpv.standardizedSmiles, standardizerName, lanId);
				
				try {
					mpv.compound = compoundService.create(compound);
				} catch (ConstraintViolationException e) {
					System.out.println(e.getMessage());
				}
			} else if (!mpv.isStandardized) {
				logger.info(mpv.id + ": Skipped compound upload due to missing standardization");
			} else if (mpv.compound!=null) {
				logger.trace(mpv.id + ": Found existing compound");
			}
			
			if (mpv.standardizedSmiles==null) {
				// Don't calculate descriptors on compounds without standardization
				logger.info(mpv.id + ": Skipped descriptor calculation due to null standardization");
				continue;
			} 
			
			DescriptorValues descriptorValues = descriptorValuesService
					.findByCanonQsarSmilesAndDescriptorSetName(mpv.standardizedSmiles, descriptorSet.getName());
			if (descriptorValues!=null) {
				// Should this store descriptors for calculation later? Probably not
				// Do nothing
				logger.trace(mpv.id + ": Found existing descriptor values");
			} else {
				// Calculate descriptors
				DescriptorCalculationResponse descriptorCalculationResponse = 
						descriptorWebService.callCalculation(mpv.standardizedSmiles).getBody();
				
				// Store descriptors
				// Again, store null or failed descriptors so we don't keep trying to calculate them every time
				if (mpv.compound!=null && descriptorCalculationResponse!=null) {
					String valuesTsv = descriptorCalculationResponse.valuesTsv;
					// If descriptor calculation failed, set null so we can check easily when we try to use them later
					if (valuesTsv.contains("Error")) {
						logger.info(mpv.id + ": Error calculating descriptors: " + valuesTsv);
						valuesTsv = null;
					} else {
						logger.debug(mpv.id + ": Calculated descriptors: " + valuesTsv.substring(0,255));
					}
					
					descriptorValues = new DescriptorValues(mpv.standardizedSmiles, descriptorSet, valuesTsv, lanId);
					
					try {
						descriptorValuesService.create(descriptorValues);
					} catch (ConstraintViolationException e) {
						System.out.println(e.getMessage());
					}
				}
			}
		}
	}
	
	private Map<String, List<MappedPropertyValue>> unifyPropertyValuesByStructure(List<MappedPropertyValue> mappedPropertyValues) {
		Map<String, List<MappedPropertyValue>> unifiedPropertyValues = 
				new HashMap<String, List<MappedPropertyValue>>();
		Double datasetStdev = MathUtil.stdevS(mappedPropertyValues.stream().map(mpv -> mpv.qsarPropertyValue).collect(Collectors.toList()));
		for (MappedPropertyValue mpv:mappedPropertyValues) {
			String structure = mpv.standardizedSmiles;
			
			// For testing purposes, option to use Inchikey1 for structure unification like TM's old code
			// Danger: Don't use this if data will be posted to the database!
//			String structure = IndigoUtil.toInchikey(data.standardizedSmiles);
//			structure = structure==null ? null : structure.substring(0, 14);
			
			if (structure==null) {
				logger.info(mpv.id + ": Skipped unification due to missing structure");
				continue;
			}
			
			List<MappedPropertyValue> structurePropertyValues = unifiedPropertyValues.get(structure);
			if (structurePropertyValues==null) { structurePropertyValues = new ArrayList<MappedPropertyValue>(); }
			structurePropertyValues.add(mpv);
			unifiedPropertyValues.put(structure, structurePropertyValues);
		}
		
		for (String structure:unifiedPropertyValues.keySet()) {
			List<MappedPropertyValue> structurePropertyValues = unifiedPropertyValues.get(structure);
			Double stdev = MathUtil.stdevS(structurePropertyValues.stream().map(mpv -> mpv.qsarPropertyValue).collect(Collectors.toList()));
			if (stdev > DevQsarConstants.STDEV_WIDTH_TOLERANCE * datasetStdev) {
				logger.info(structure + ": Removed data point due to high stdev: " 
						+ stdev + " > " + DevQsarConstants.STDEV_WIDTH_TOLERANCE + "*" + datasetStdev);
				unifiedPropertyValues.remove(structure);
			}
		}
		
		return unifiedPropertyValues;
	}
	
	private Property initializeProperty(List<PropertyValue> propertyValues) throws ConstraintViolationException {
		ExpPropProperty expPropProperty = propertyValues.iterator().next().getProperty();
		Property property = propertyService.findByName(expPropProperty.getName());
		if (property==null) {
			property = Property.fromExpPropProperty(expPropProperty, lanId);
			propertyService.create(property);
		}
		
		return property;
	}
	
	private Unit initializeUnit(DatasetParams params) throws ConstraintViolationException {
		String finalUnitName = finalUnits.get(params.propertyName);
		Unit unit = unitService.findByName(finalUnitName);
		if (unit==null) {
			// If not, search exp_prop
			ExpPropUnit expPropUnit = expPropUnitService.findByName(finalUnitName);
			if (expPropUnit!=null) {
				// If unit in exp_prop, grab it and post to qsar_datasets
				unit = Unit.fromExpPropUnit(expPropUnit, lanId);
			} else {
				// If unit not in exp_prop or qsar_datasets, create and post it
				// Should just be the final converted -log10 units for certain properties
				unit = new Unit(finalUnitName, finalUnitName, lanId);
				
			}
			unitService.create(unit);
		}
		
		return unit;
	}
	
	private Dataset initializeDataset(Dataset dataset) throws ConstraintViolationException {
		Dataset findDataset = datasetService.findByName(dataset.getName());
		if (findDataset!=null) {
			logger.error("Dataset with name " + dataset.getName() + " already exists");
			dataset = null;
		} else {
			datasetService.create(dataset);
		}
		
		return dataset;
	}
	
	private void postDataPoints(Map<String, List<MappedPropertyValue>> unifiedPropertyValues, Dataset dataset) {
		for (String structure:unifiedPropertyValues.keySet()) {
			List<MappedPropertyValue> structurePropertyValues = unifiedPropertyValues.get(structure);
			String unitName = dataset.getUnit().getName();
			Double finalValue = null;
			if (unitName.equals(DevQsarConstants.BINARY)) {
				finalValue = PropertyValueMerger.mergeBinary(structurePropertyValues);
			} else {
				finalValue = PropertyValueMerger.mergeContinuous(structurePropertyValues, dataset.getProperty().getName());
			}
			
			// Print information on unified structures and final QSAR property values for testing
			if (structurePropertyValues.size() > 1) {
				System.out.println(structure + "\t" + finalValue + " " + unitName);
				for (MappedPropertyValue mpv:structurePropertyValues) {
					System.out.println("\t" + mpv.id + " (" + mpv.dsstoxRecord.preferredName + ")\t" 
							+ mpv.qsarPropertyValue + " " + unitName);
				}
			} else if (structurePropertyValues.size()==1) {
				MappedPropertyValue mpv = structurePropertyValues.iterator().next();
				System.out.println(structure + "\t" + mpv.id + " (" + mpv.dsstoxRecord.preferredName + ")\t" 
						+ mpv.qsarPropertyValue + " " + unitName);
			}
			
			if (finalValue==null) {
				logger.info(structure + ": Skipped posting data point due to invalid consensus value");
				continue;
			}
			
			DataPoint dataPoint = new DataPoint(structure, finalValue, dataset, false, lanId);
			
			try {
				dataPointService.create(dataPoint);
					
				for (MappedPropertyValue mpv:structurePropertyValues) {
					String expPropId = mpv.propertyValue.generateExpPropId();
					DataPointContributor dataPointContributor = new DataPointContributor(dataPoint, expPropId, lanId);
					
					try {
						dataPointContributorService.create(dataPointContributor);
					} catch (ConstraintViolationException e1) {
						System.out.println(e1.getMessage());
					}
				}
			} catch (ConstraintViolationException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	public void createPropertyDataset(DatasetParams params, boolean testMapping) {
		Gson gson = new Gson();
		
		System.out.println("Selecting experimental property data for " + params.propertyName + "...");
		long t5 = System.currentTimeMillis();
		List<PropertyValue> propertyValues = propertyValueService.findByPropertyNameWithOptions(params.propertyName, true, true);
		long t6 = System.currentTimeMillis();
		System.out.println("Selection time = " + (t6 - t5)/1000.0 + " s");
		
		if (propertyValues==null || propertyValues.isEmpty()) {
			logger.error(params.datasetName + ": Experimental property data unavailable");
			return;
		}
		
		System.out.println("Retrieving DSSTox structure data...");
		List<MappedPropertyValue> mappedPropertyValues = null;
		try {
			mappedPropertyValues = mapPropertyValuesToDsstoxRecords(propertyValues, params);
		} catch (Exception e) {
			logger.error("Failed DSSTox query: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		if (mappedPropertyValues==null || mappedPropertyValues.isEmpty()) {
			logger.error(params.datasetName + ": DSSTox structure data unavailable");
			return;
		}
		
		if (testMapping) {
			logger.info(params.datasetName + ": Exiting DSSTox mapping test, " + mappedPropertyValues.size() + " mappings accepted");
			return;
		}
		
		Property property = initializeProperty(propertyValues);
		if (property==null) { return; }
		
		Unit unit = initializeUnit(params);
		if (unit==null) { return; }
		
		Dataset dataset = new Dataset(params.datasetName, params.datasetDescription, property, unit, 
				gson.toJson(params.mappingParams), lanId);
		dataset = initializeDataset(dataset);
		if (dataset==null) { return; }
		
		System.out.println("Standardizing structures using " + standardizerName + "...");
		long t1 = System.currentTimeMillis();
		standardize(mappedPropertyValues);
		long t2 = System.currentTimeMillis();
		System.out.println("Standardization time: " + (t2 - t1)/1000.0 + " s");
		
		System.out.println("Calculating descriptors using " + descriptorSet.getName() + "...");
		long t3 = System.currentTimeMillis();
		calculateDescriptors(mappedPropertyValues);
		long t4 = System.currentTimeMillis();
		System.out.println("Calculation time: " + (t4 - t3)/1000.0 + " s");
		
		System.out.println("Unifying structures...");
		Map<String, List<MappedPropertyValue>> unifiedPropertyValues = unifyPropertyValuesByStructure(mappedPropertyValues);
		
		System.out.println("Posting final merged values...");
		long t7 = System.currentTimeMillis();
		postDataPoints(unifiedPropertyValues, dataset);
		long t8 = System.currentTimeMillis();
		System.out.println("Time to post: " + (t8 - t7)/1000.0 + " s");
	}
	
	public static void main(String[] args) {
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DescriptorWebService testDescriptorWebService = new DescriptorWebService(DevQsarConstants.SERVER_819,
				DevQsarConstants.PORT_TEST_DESCRIPTORS,
				"TEST-descriptors/");
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, testDescriptorWebService, "gsincl01");
		
//		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
//		BoundParameterValue phBound = new BoundParameterValue("pH", 6.5, 7.5, true);
//		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
//		bounds.add(temperatureBound);
//		bounds.add(phBound);
		MappingParams mappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, "ExpProp_WaterSolubility_WithChemProp_120121", 
				false, true, false, true, true, false, true);
		DatasetParams params = new DatasetParams("ExpProp_WaterSolubility_WithChemProp_Unfiltered_Hibernate", 
				"Final water solubility experimental dataset from exp_prop, "
				+ "without parameter filtering, with mapping in Hibernate", 
				DevQsarConstants.WATER_SOLUBILITY,
				mappingParams);
//				bounds);

		creator.createPropertyDataset(params, false);
	}
}
