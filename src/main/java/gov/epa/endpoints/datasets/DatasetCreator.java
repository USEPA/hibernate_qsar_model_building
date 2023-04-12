package gov.epa.endpoints.datasets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;
import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;
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
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundServiceImpl;
import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.endpoints.datasets.DatasetParams.MappingParams;
import gov.epa.endpoints.datasets.dsstox_mapping.DiscardedPropertyValue;
import gov.epa.endpoints.datasets.dsstox_mapping.DsstoxMapper;
import gov.epa.run_from_java.scripts.GetExpPropInfo.ExcelCreator;
import gov.epa.run_from_java.scripts.GetExpPropInfo.GetExpPropInfo;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.util.MathUtil;
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
	
	private String standardizerName;
	
	private Map<String, String> finalUnits;
	private Map<String, String> contributorUnits;

	private Set<String> physchemPropertyNames;
	private Set<String> acceptableAtoms;

	
	
	
	
	public static boolean createExcelFiles=true;//whether to create excel files for discarded and mapped records in addition to the json files
	
	public static void main(String[] args) {
		System.out.println("eclipse recognizes new code2");

		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");
//		creator.createExternalValidationSets("LLNA TEST", "LLNA from exp_prop, without eChemPortal");
		
		String propertyName = DevQsarConstants.VAPOR_PRESSURE;
		String listName = "ExpProp_VP_WithChemProp_070822";
		
		/*
		ArrayList<String> chemicalsLists = new ArrayList<String>();
		chemicalsLists.add("ExpProp_HLC_WithChemProp_073022_4001_to_4849");
		chemicalsLists.add("ExpProp_HLC_WithChemProp_073022_2001_to_4000");
		chemicalsLists.add("ExpProp_HLC_WithChemProp_073022_1_to_2000");
		*/
		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
//		BoundParameterValue phBound = new BoundParameterValue("pH", 6.0, 8.0, true);
//		BoundParameterValue speciesBound = new BoundParameterValue("Species", "Mouse", false);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);
//		bounds.add(phBound);
//		bounds.add(speciesBound);
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, 
				false, true, false, true, true, false, false, null,true,true);
		MappingParams casrnMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_CASRN, null,
				true, false, false, false, false, false, true, null,true,true);
		String listMappingName = "TESTING2_ExpProp_VP_WithChemProp_070822";
		String casrnMappingName = "CASRN mapping of " + propertyName + " from exp_prop, without eChemPortal";
		String listMappingDescription = "TESTRUN2 Vapor Pressure with 20 < T (C) < 30";
		String casrnMappingDescription = propertyName + " with species = Mouse, mapped by CASRN";
		
		DatasetParams casrnMappedParams = new DatasetParams(casrnMappingName, 
				casrnMappingDescription, 
				propertyName,
				casrnMappingParams,
				bounds);
		
		DatasetParams listMappedParams = new DatasetParams(listMappingName, 
				listMappingDescription, 
				propertyName,
				listMappingParams,
				bounds);

//		creator.createPropertyDataset(casrnMappedParams);
		creator.createPropertyDataset(listMappedParams, false);
		

	}
	
	
	
	public DatasetCreator(Standardizer standardizer, String lanId) 
			throws ConstraintViolationException {
		this.compoundService = new CompoundServiceImpl();
		
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
		
		this.standardizerName = this.standardizer==null ? 
				DevQsarConstants.STANDARDIZER_NONE : this.standardizer.standardizerName;
		
		try {
			Unirest.config().followRedirects(true).socketTimeout(000).connectTimeout(000);
		} catch (Exception e) {
//			logger.warn("Unirest configuration failed: " + e.getMessage());
		}

//		Logger apacheLogger = LogManager.getLogger("org.apache.http");
//		apacheLogger.setLevel(Level.WARN);
//		Logger mapperLogger = LogManager.getLogger(DsstoxMapper.class);
//		mapperLogger.setLevel(Level.WARN);
//		Logger mpvLogger = LogManager.getLogger(MappedPropertyValue.class);
//		mpvLogger.setLevel(Level.WARN);
//		logger.setLevel(Level.WARN);
		
//		this.gson = new GsonBuilder().disableHtmlEscaping().create();
		this.acceptableAtoms = DevQsarConstants.getAcceptableAtomsSet();
		this.finalUnits = DevQsarConstants.getFinalUnitsMap();
		this.contributorUnits = DevQsarConstants.getContributorUnitsMap();
		
		physchemPropertyNames = expPropPropertyService.findByPropertyCategoryName("Physchem").stream()
				.map(p -> p.getName())
				.collect(Collectors.toSet());
	}
	
	public DatasetCreator(String lanId) {
		this(null, lanId); // If initialized without a standardizer web service, uses DSSTox QSAR-ready SMILES
	}
	
	private List<MappedPropertyValue> mapPropertyValuesToDsstoxRecords(List<PropertyValue> propertyValues, DatasetParams params,HashMap<String,String>hmCanonSmilesLookup) {
		List<MappedPropertyValue> mappedPropertyValues = new ArrayList<MappedPropertyValue>();
		
		String finalUnitName = finalUnits.get(params.propertyName);
//		params.mappingParams.omitSalts = physchemPropertyNames.contains(params.propertyName); // Omit salts for physchem properties
//		params.mappingParams.omitSalts = false;
		
		try {
			DsstoxMapper dsstoxMapper = new DsstoxMapper(params, standardizer, hmCanonSmilesLookup,finalUnitName, acceptableAtoms, lanId);
			
			if (params.mappingParams.dsstoxMappingId.equals(DevQsarConstants.MAPPING_BY_CASRN)
					|| params.mappingParams.dsstoxMappingId.equals(DevQsarConstants.MAPPING_BY_DTXSID)
					|| params.mappingParams.dsstoxMappingId.equals(DevQsarConstants.MAPPING_BY_DTXCID)
					|| params.mappingParams.dsstoxMappingId.equals(DevQsarConstants.MAPPING_BY_LIST)) {
			
				//Map the records to DSSTOX:
				mappedPropertyValues = dsstoxMapper.map(propertyValues,createExcelFiles);
			
			} else {
				// TODO implementation of:
				// Mapping by provided DTXRID?
//				logger.error("Mapping strategy " + params.mappingParams.dsstoxMappingId + " not implemented");
				return null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mappedPropertyValues;
	}
	
	
	
	private void standardize(List<MappedPropertyValue> mappedPropertyValues, HashMap<String, String> hmQsarSmilesLookup) {
		// Go through all compounds retrieved from DSSTox
		List<String> smilesToBatchStandardize = new ArrayList<String>();
		
		int counter=0;
		
		//TODO make a batch based method to get the standardized smiles to speed it up
		
		
		for (MappedPropertyValue mpv:mappedPropertyValues) {
			counter++;

			DsstoxRecord dr = mpv.dsstoxRecord;

//			if (mpv.propertyValue.getSourceChemical().getSourceChemicalName()!=null) {
//				if (mpv.propertyValue.getSourceChemical().getSourceChemicalName().equals("radon")) {
//					System.out.println("*** For smiles ="+dr.smiles+",qsarSmiles="+mpv.standardizedSmiles);	
//				}
//			}
			
			if(counter%1000==0) System.out.println(counter+ " of "+mappedPropertyValues.size());
			
			
			if(hmQsarSmilesLookup.containsKey(dr.smiles)) {
				mpv.standardizedSmiles = hmQsarSmilesLookup.get(dr.smiles);
				mpv.isStandardized = true;
				mpv.compound = new Compound(dr.dsstoxCompoundId,dr.smiles, mpv.standardizedSmiles, standardizerName, lanId);
				
				
//				System.out.println("For smiles = "+dr.smiles+",qsarSmiles="+mpv.standardizedSmiles);
				continue;
			}
			

			// Check (by DTXCID) if compound already has a standardization
			Compound compound = compoundService.findByDtxcidSmilesAndStandardizer(dr.dsstoxCompoundId, dr.smiles,standardizerName);

			if (compound!=null) {
				System.out.println("Standardization in db:"+compound.getDtxcid()+"\t"+compound.getSmiles()+"\t"+compound.getCanonQsarSmiles());
				
				// If already standardized, set standardization and mark record as standardized
				// Additionally store mapped compound so we don't have to query it later
				mpv.compound = compound;
				mpv.standardizedSmiles = compound.getCanonQsarSmiles();
				mpv.isStandardized = true;
//				logger.trace(mpv.id + ": Found existing standardization: " + dr.smiles + " -> " + mpv.standardizedSmiles);
			} else if (standardizer==null) {
				// If using DSSTox QSAR-ready SMILES, just set it and mark standardized
				mpv.standardizedSmiles = dr.qsarReadySmiles;
				mpv.isStandardized = true;
//				logger.debug(mpv.id + ": Found DSSTox standardization: " + dr.smiles + " -> " + mpv.standardizedSmiles);
			} else {
				if (standardizer.useBatchStandardize) {
					// Add SMILES to list for batch standardization
					smilesToBatchStandardize.add(dr.smiles);
				} else {
					// Standardize one at a time
					StandardizeResponseWithStatus standardizeResponse = standardizer.callStandardize(dr.smiles);
					
					
//					if (mpv.propertyValue.getSourceChemical().getSourceChemicalName()!=null) {
//						if (mpv.propertyValue.getSourceChemical().getSourceChemicalName().equals("radon")) {
//							System.out.println("***radon status="+standardizeResponse.status);
//						}
//					}
					
					if (standardizeResponse.status==200) {
						
						StandardizeResponse standardizeResponseData = standardizeResponse.standardizeResponse;
						
						if (standardizeResponseData.success) {
							mpv.standardizedSmiles = standardizeResponseData.qsarStandardizedSmiles;
							mpv.isStandardized = true;
							
							//**************************************************************
							//TMM 6/8/22 store in database:
							compound = new Compound(dr.dsstoxCompoundId,dr.smiles, mpv.standardizedSmiles, standardizerName, lanId);
							
							System.out.println("standardized:"+dr.dsstoxCompoundId+"\t"+dr.smiles+"\t"+mpv.standardizedSmiles);
							
							//TODO check if this addition causes duplication error message...
							try {
								mpv.compound = compoundService.create(compound);
							} catch (ConstraintViolationException e) {
								System.out.println(e.getMessage());
							}
							//**************************************************************
							
							
//							logger.debug(mpv.id + ": Standardized: " + dr.smiles + " -> " + mpv.standardizedSmiles);
						} else {
//							logger.warn(mpv.id + ": Standardization failed for SMILES: " + dr.smiles);
//							System.out.println(mpv.id + ": Standardization failed for SMILES: " + dr.smiles);
						}
					
					} else if (standardizeResponse.status==404) {

						mpv.standardizedSmiles=null;
						compound = new Compound(dr.dsstoxCompoundId,dr.smiles, mpv.standardizedSmiles, standardizerName, lanId);
						System.out.println("standardized:"+dr.dsstoxCompoundId+"\t"+dr.smiles+"\t"+mpv.standardizedSmiles);
						
						//TODO check if this addition causes duplication error message...
						try {
							mpv.compound = compoundService.create(compound);
						} catch (ConstraintViolationException e) {
							System.out.println(e.getMessage());
						}
						
					} else {
//						logger.warn(mpv.id + ": Standardizer HTTP response failed for SMILES: " 
//								+ dr.smiles + " with code " + standardizeResponse.status);
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
			
			if (mpv.standardizedSmiles!=null && mpv.standardizedSmiles.isBlank()) {
				mpv.standardizedSmiles = null;
			}
			
			Compound compound = new Compound(dr.dsstoxCompoundId, dr.smiles, mpv.standardizedSmiles, standardizerName, lanId);
			
			try {
				mpv.compound = compoundService.create(compound);
			} catch (ConstraintViolationException e) {
				System.out.println(e.getMessage());
			}
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
//				logger.error("Batch standardization failed");
			}
		} else {
//			logger.error("Batch standardizer HTTP response failed with code " + batchStandardizeResponse.status);
		}
		
		return standardizedSmilesMap;
	}
	
	
	
	private Map<String, List<MappedPropertyValue>> unifyPropertyValuesByStructure(List<MappedPropertyValue> mappedPropertyValues,
			boolean useStdevFilter,boolean validateStructure) {
		Map<String, List<MappedPropertyValue>> unifiedPropertyValues = 
				new HashMap<String, List<MappedPropertyValue>>();
		Double datasetStdev = MathUtil.stdevS(mappedPropertyValues.stream().map(mpv -> mpv.qsarPropertyValue).collect(Collectors.toList()));
		
		
		int counter=0;
		
		for (MappedPropertyValue mpv:mappedPropertyValues) {
			String structure = mpv.standardizedSmiles;
			
			// For testing purposes, option to use Inchikey1 for structure unification like TM's old code
			// Danger: Don't use this if data will be posted to the database!
//			String structure = IndigoUtil.toInchikey(data.standardizedSmiles);
//			structure = structure==null ? null : structure.substring(0, 14);
			
			if (validateStructure) {//TODO we need to change it so that qsar ready smiles should stay a mixture instead of null when have multiple components
				if (structure==null) {
					System.out.println(mpv.id+": Skipped unification since QSAR Ready smiles was null");
//					logger.info(mpv.id + ": Skipped unification due to missing structure");
					continue;
				}
				
				if (structure.contains(".")) {
					System.out.println(mpv.id+": Skipped unification since QSAR Ready smiles was still a mixture (i.e. it contained a period)");
					continue;
				}
			} else {

				if (mpv.compound==null) {
					System.out.println(mpv.id+": Skipped unification since compound was null:"+mpv.propertyValue.getSourceChemical().getSourceChemicalName());
//					logger.info(mpv.id + ": Skipped unification due to missing structure");
					continue;
				}

				
				if (structure==null) {
//					System.out.println(mpv.id+": using original smiles:"+mpv.compound.getSmiles());
					structure=mpv.compound.getSmiles();
//					logger.info(mpv.id + ": Skipped unification due to missing structure");
				}
				
			}

			
			List<MappedPropertyValue> structurePropertyValues = unifiedPropertyValues.get(structure);
			if (structurePropertyValues==null) { structurePropertyValues = new ArrayList<MappedPropertyValue>(); }
			structurePropertyValues.add(mpv);
			unifiedPropertyValues.put(structure, structurePropertyValues);
			counter++;
		}
		
		if (useStdevFilter) {
			for (String structure:unifiedPropertyValues.keySet()) {
				List<MappedPropertyValue> structurePropertyValues = unifiedPropertyValues.get(structure);
				Double stdev = MathUtil.stdevS(structurePropertyValues.stream().map(mpv -> mpv.qsarPropertyValue).collect(Collectors.toList()));
				if (stdev > DevQsarConstants.STDEV_WIDTH_TOLERANCE * datasetStdev) {
//					logger.info(structure + ": Removed data point due to high stdev: " 
//							+ stdev + " > " + DevQsarConstants.STDEV_WIDTH_TOLERANCE + "*" + datasetStdev);
					unifiedPropertyValues.remove(structure);
					counter--;
				}
			}
		}
		System.out.println("Count of unified records:\t"+counter);
		
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
	
	private Unit initializeUnit(String finalUnitName) throws ConstraintViolationException {
		
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
		Dataset dbDataset = datasetService.findByName(dataset.getName());
		
		if (dbDataset!=null) {
			System.out.println("Dataset with name " + dataset.getName() + " already exists");
			dataset = null;
		} else {
			dataset=datasetService.create(dataset);
		}
		
		return dataset;
	}
	
	private void postDataPoints(Map<String, List<MappedPropertyValue>> unifiedPropertyValues, Dataset dataset,Unit datapointContributerUnit) {
		for (String structure:unifiedPropertyValues.keySet()) {
			List<MappedPropertyValue> structurePropertyValues = unifiedPropertyValues.get(structure);
			String unitName = dataset.getUnit().getName();
			Double finalValue = null;
			if (unitName.equals(DevQsarConstants.BINARY)) {
				finalValue = PropertyValueMerger.mergeBinary(structurePropertyValues);
			} else {
				finalValue = PropertyValueMerger.mergeContinuous(structurePropertyValues, dataset.getProperty().getName());
			}
			
			if (finalValue==null) {
//				logger.info(structure + ": Skipped posting data point due to invalid consensus value");
				continue;
			}
			
			DataPoint dataPoint = new DataPoint(structure, finalValue, dataset, false, lanId);
			
			if (dataPoint.getCanonQsarSmiles()==null) continue;
			
			try {
				dataPointService.create(dataPoint);
					
				for (MappedPropertyValue mpv:structurePropertyValues) {
					
					DataPointContributor dataPointContributor = new DataPointContributor(dataPoint,mpv,datapointContributerUnit, lanId);
					
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
	
	/**
	 * Flattens the records and posts them to the database. It stores the final property value with associated CIDs and exp_prop_ids
	 *  
	 * @param unifiedPropertyValues
	 * @param dataset
	 */
	private void postDataPointsWithCIDs(Map<String, List<MappedPropertyValue>> unifiedPropertyValues, Dataset dataset,Unit unitDatapointContributor) {
		List<DataPoint> dataPoints = new ArrayList<DataPoint>();
		List<DataPointContributor> dataPointContributors = new ArrayList<DataPointContributor>();
		
		for (String structure:unifiedPropertyValues.keySet()) {
			List<MappedPropertyValue> structurePropertyValues = unifiedPropertyValues.get(structure);
			String unitName = dataset.getUnit().getName();
			
			
			String [] values=null;
			
			if (unitName.equals(DevQsarConstants.BINARY)) {
				values = PropertyValueMerger.mergeBinaryIncludeFinalCIDsExpPropIDs(structurePropertyValues);
			} else {
				values = PropertyValueMerger.mergeContinuousIncludeFinalCIDsExpPropIDs(structurePropertyValues, dataset.getProperty().getName());
			}
			
			if (values==null) {
				System.out.println(structure + ": Skipped posting data point due to invalid flattened value");
				continue;
			}

			Double finalValue = Double.parseDouble(values[0]);
			String finalCIDs = values[1];
			String finalExpPropIDs = values[2];
			
			DataPoint dataPoint = new DataPoint(structure, finalCIDs, finalExpPropIDs, finalValue, dataset, false, lanId);
			
			if (dataPoint.getCanonQsarSmiles()==null) continue;
			
			try {
				// dataPointService.create(dataPoint);
				dataPoints.add(dataPoint);
				
				for (MappedPropertyValue mpv:structurePropertyValues) {
					
//					String expPropId = mpv.propertyValue.getId()+"";//change to make things easier
					DataPointContributor dataPointContributor = new DataPointContributor(dataPoint, mpv,unitDatapointContributor, lanId);
					dataPointContributors.add(dataPointContributor);
					try {
						// dataPointContributorService.create(dataPointContributor);
					} catch (ConstraintViolationException e1) {
						System.out.println(e1.getMessage());
					}
				}
			} catch (ConstraintViolationException e) {
				System.out.println(e.getMessage());
			}
		}
		try {
//			dataPointService.createBatch(dataPoints);
//			dataPointContributorService.createBatch(dataPointContributors);
			
			dataPointService.createBatchSQL(dataPoints);
			dataPointContributorService.createBatchSQL(dataset,dataPointContributors);

			
		} catch (ConstraintViolationException e1) {
			System.out.println(e1.getMessage());
		}
		
	}
	
	
	private void lookatDataPoints(Map<String, List<MappedPropertyValue>> unifiedPropertyValues, Dataset dataset) {
		int counter=0;
		
		for (String structure:unifiedPropertyValues.keySet()) {
			List<MappedPropertyValue> structurePropertyValues = unifiedPropertyValues.get(structure);
			
			String unitName = dataset.getUnit().getName();
			
			
			String [] values=null;
			
			if (unitName.equals(DevQsarConstants.BINARY)) {
				values = PropertyValueMerger.mergeBinaryIncludeFinalCIDsExpPropIDs(structurePropertyValues);
			} else {
				values = PropertyValueMerger.mergeContinuousIncludeFinalCIDsExpPropIDs(structurePropertyValues, dataset.getProperty().getName());
			}
			
			if (values==null) {
//				logger.info(structure + ": Skipped posting data point due to invalid consensus value");
				continue;
			}

			Double finalValue = Double.parseDouble(values[0]);
			String finalCIDs = values[1];
			String finalExpPropIDs = values[2];
			
			DataPoint dataPoint = new DataPoint(structure, finalCIDs,finalExpPropIDs,finalValue, dataset, false, lanId);
			
			if (dataPoint.getCanonQsarSmiles()==null) continue;
			
			for (MappedPropertyValue mpv:structurePropertyValues) {
				DataPointContributor dataPointContributor = new DataPointContributor(dataPoint, mpv, null, lanId);
				counter++;
			}
		}
		
		System.out.println("Number of data point contributors:\t"+counter);
	}

	public void createPropertyDataset(DatasetParams params, boolean useStdevFilter) {
		List<String>excludedSources=new ArrayList<>();
		createPropertyDataset(params, useStdevFilter, excludedSources);
	}
	
	public void createPropertyDataset(DatasetParams params, boolean useStdevFilter,List<String>excludedSources) {
//		System.out.println("enter createPropertyDataset with excluded sources");

		HashMap<String,String>hmQsarSmilesLookup = getQsarSmilesLookupFromDB();
				
		Dataset datasetDB = datasetService.findByName(params.datasetName);
		if(datasetDB!=null) {
			System.out.println("already have "+params.datasetName+" in db");
			return;
		}
		
		Gson gson = new Gson();
		
		System.out.println("Selecting experimental property data for " + params.propertyName + "...");
		long t5 = System.currentTimeMillis();
		List<PropertyValue> propertyValues = propertyValueService.findByPropertyNameWithOptions(params.propertyName, true, true);
		long t6 = System.currentTimeMillis();
		System.out.println("Selection time = " + (t6 - t5)/1000.0 + " s");

		System.out.println("Raw records:"+propertyValues.size());		
		excludePropertyValues(excludedSources, propertyValues);
		if (excludedSources.size()>0) System.out.println("Raw records after source exclusion:"+propertyValues.size());
		
		if (propertyValues==null || propertyValues.isEmpty()) {
//			logger.error(params.datasetName + ": Experimental property data unavailable");
			System.out.println(params.datasetName + ": Experimental property data unavailable");
			return;
		}
		
		System.out.println("Retrieving DSSTox structure data...");
		List<MappedPropertyValue> mappedPropertyValues = null;
		try {
			mappedPropertyValues = mapPropertyValuesToDsstoxRecords(propertyValues, params,hmQsarSmilesLookup);
		} catch (Exception e) {
//			logger.error("Failed DSSTox query: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		if (mappedPropertyValues==null || mappedPropertyValues.isEmpty()) {
				System.out.println(params.datasetName + ": DSSTox structure data unavailable");
			return;
		}
		
		//TODO store mapping of source chemicals to dtxsid/dtxcid in source_chemical_mappings table or use datasets with contributer table
		
		Property property = initializeProperty(propertyValues);
		if (property.getId()==null) { return; }
		
		String finalUnitName = finalUnits.get(params.propertyName);		
		Unit unit = initializeUnit(finalUnitName);
		
		String contributorUnitName=contributorUnits.get(params.propertyName);
		Unit unitDatapointContributor=initializeUnit(contributorUnitName);
		
		if (unit.getId()==null) { return; }
				
		System.out.println("Standardizing structures using " + standardizerName + "...");
		long t1 = System.currentTimeMillis();
		standardize(mappedPropertyValues,hmQsarSmilesLookup);
		long t2 = System.currentTimeMillis();
		System.out.println("Standardization time: " + (t2 - t1)/1000.0 + " s");
		
		System.out.println("Unifying structures...");
		Map<String, List<MappedPropertyValue>> unifiedPropertyValues = unifyPropertyValuesByStructure(mappedPropertyValues, useStdevFilter,params.mappingParams.validateStructure);
		
		System.out.println("Saving unification data to examine...");
//		saveUnifiedData(unifiedPropertyValues, params.datasetName, unit);redundant- we have excel and json
		saveUnifiedData(unifiedPropertyValues, params.datasetName, unit,createExcelFiles);
		
		
		
		
		if(true) return;
		
		Dataset dataset = new Dataset(params.datasetName, params.datasetDescription, property, unit, unitDatapointContributor, 
				gson.toJson(params.mappingParams), lanId);
		
		Dataset dataset2=dataset;//used to look at datapoints later
		
		dataset = initializeDataset(dataset);

		System.out.println("Posting final merged values...");
		long t7 = System.currentTimeMillis();
		
		if (dataset!=null) {//We can post:
//			postDataPoints(unifiedPropertyValues, dataset);//old method doesnt have CIDs or exp_prop_ids 
			postDataPointsWithCIDs(unifiedPropertyValues, dataset,unitDatapointContributor);
		
		} else {//Data set already exists lets look at datapoints:
			lookatDataPoints(unifiedPropertyValues, dataset2);		
		}
		
		long t8 = System.currentTimeMillis();
		System.out.println("Time to post: " + (t8 - t7)/1000.0 + " s");
		
		
		if (dataset==null) {
			System.out.println("*** Warning dataset already exists! New dataset not created ***"); 
		} 

	}



	private HashMap<String,String> getQsarSmilesLookupFromDB() {
		HashMap<String,String>htQsarSmiles=new HashMap<>();

		List<Compound>standardizedCompounds=compoundService.findAllWithStandardizerSmilesNotNull(standardizerName);
		
		System.out.println("Number of standardized compounds in db:"+standardizedCompounds.size());
		
		for (Compound compound:standardizedCompounds) {
//			System.out.println(compound.getSmiles()+"\t"+compound.getCanonQsarSmiles());
			htQsarSmiles.put(compound.getSmiles(), compound.getCanonQsarSmiles());
		}
		return htQsarSmiles;
		
	}
	
	
//	public void mapSourceChemicalsForProperty(DatasetParams params) {
//		HashMap<String,String>hmQsarSmilesLookup = getQsarSmilesLookupFromDB();
//		
//		Gson gson = new Gson();
//		
//		System.out.println("Selecting experimental property data for " + params.propertyName + "...");
//		long t5 = System.currentTimeMillis();
//		List<PropertyValue> propertyValues = propertyValueService.findByPropertyNameWithOptions(params.propertyName, true, true);
//		long t6 = System.currentTimeMillis();
//		System.out.println("Selection time = " + (t6 - t5)/1000.0 + " s");
//
//		System.out.println("Raw records:"+propertyValues.size());		
//		
////		excludePropertyValues(excludedSources, propertyValues);
////		if (excludedSources.size()>0) System.out.println("Raw records after source exclusion:"+propertyValues.size());
//		
//		if (propertyValues==null || propertyValues.isEmpty()) {
////			logger.error(params.datasetName + ": Experimental property data unavailable");
//			System.out.println(params.propertyName + ": Experimental property data unavailable");
//			return;
//		}
//		
//		System.out.println("Retrieving DSSTox structure data...");
//		List<MappedPropertyValue> mappedPropertyValues = null;
//		try {
//			mappedPropertyValues = mapPropertyValuesToDsstoxRecords(propertyValues, params,hmQsarSmilesLookup);
//			
//			
//		} catch (Exception e) {
////			logger.error("Failed DSSTox query: " + e.getMessage());
//			e.printStackTrace();
//			return;
//		}
//		
//		if (mappedPropertyValues==null || mappedPropertyValues.isEmpty()) {
//				System.out.println(params.datasetName + ": DSSTox structure data unavailable");
//			return;
//		}
//		
//		for (MappedPropertyValue mpv:mappedPropertyValues) {
//			
//			if(mpv.dsstoxRecord==null && mpv.dsstoxRecord.getDsstoxSubstanceId()==null) continue;
//			
//			String dtxsid=mpv.dsstoxRecord.getDsstoxSubstanceId();
//			Long sourceChemicalId=mpv.propertyValue.getSourceChemical().getId();
//			System.out.println(dtxsid+"\t"+sourceChemicalId);
//			
//			//TODO store this mapping in source_chemical_mapping table
//		}
//
//	}

	
	
	public void createPropertyDatasetWithSpecifiedSources(DatasetParams params, boolean useStdevFilter,List<String>includedSources) {
	
		HashMap<String,String>hmQsarSmilesLookup = getQsarSmilesLookupFromDB();
		
		System.out.println("Enter createPropertyDatasetWithSpecifiedSources");
		Dataset datasetDB = datasetService.findByName(params.datasetName);
		if(datasetDB!=null) {
			System.out.println("already have "+params.datasetName+" in db");
			return;
		}
		
		Gson gson = new Gson();
		
		System.out.println("Selecting experimental property data for " + params.propertyName + "...");
		long t5 = System.currentTimeMillis();
		List<PropertyValue> propertyValues = propertyValueService.findByPropertyNameWithOptions(params.propertyName, true, true);
		long t6 = System.currentTimeMillis();
		System.out.println("Selection time = " + (t6 - t5)/1000.0 + " s");

		System.out.println("Raw records:"+propertyValues.size());		
		excludePropertyValues2(includedSources, propertyValues);
		if (includedSources.size()>0) System.out.println("Raw records after source exclusion:"+propertyValues.size());
		
		if (propertyValues==null || propertyValues.isEmpty()) {
//			logger.error(params.datasetName + ": Experimental property data unavailable");
			System.out.println(params.datasetName + ": Experimental property data unavailable");
			return;
		}
		
		System.out.println("Retrieving DSSTox structure data...");
		List<MappedPropertyValue> mappedPropertyValues = null;
		try {
			mappedPropertyValues = mapPropertyValuesToDsstoxRecords(propertyValues, params,hmQsarSmilesLookup);
		} catch (Exception e) {
//			logger.error("Failed DSSTox query: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		if (mappedPropertyValues==null || mappedPropertyValues.isEmpty()) {
				System.out.println(params.datasetName + ": DSSTox structure data unavailable");
			return;
		}
		
		Property property = initializeProperty(propertyValues);
		if (property.getId()==null) { return; }
		
		String finalUnitName = finalUnits.get(params.propertyName);		
		Unit unit = initializeUnit(finalUnitName);
		
		String contributorUnitName=contributorUnits.get(params.propertyName);
		Unit unitDatapointContributor=initializeUnit(contributorUnitName);
		
		if (unit.getId()==null) { return; }
		
		Dataset dataset = new Dataset(params.datasetName, params.datasetDescription, property, unit,unitDatapointContributor, 
				gson.toJson(params.mappingParams), lanId);
		
		Dataset dataset2=dataset;//used to look at datapoints later
		
		dataset = initializeDataset(dataset);
		
		
		System.out.println("Standardizing structures using " + standardizerName + "...");
		long t1 = System.currentTimeMillis();
		standardize(mappedPropertyValues,hmQsarSmilesLookup);
		long t2 = System.currentTimeMillis();
		System.out.println("Standardization time: " + (t2 - t1)/1000.0 + " s");
		
		System.out.println("Unifying structures...");
		Map<String, List<MappedPropertyValue>> unifiedPropertyValues = unifyPropertyValuesByStructure(mappedPropertyValues, useStdevFilter,params.mappingParams.validateStructure);
		
		System.out.println("Saving unification data to examine...");
//		saveUnifiedData(unifiedPropertyValues, params.datasetName, unit);redundant- we have excel and json
		saveUnifiedData(unifiedPropertyValues, params.datasetName, unit,createExcelFiles);
		
		System.out.println("Posting final merged values...");
		long t7 = System.currentTimeMillis();
		
		if (dataset!=null) {//We can post:
//			postDataPoints(unifiedPropertyValues, dataset);//old method doesnt have CIDs or exp_prop_ids 
			postDataPointsWithCIDs(unifiedPropertyValues, dataset,unitDatapointContributor);
		
		} else {//Data set already exists lets look at datapoints:
			lookatDataPoints(unifiedPropertyValues, dataset2);		
		}
		
		long t8 = System.currentTimeMillis();
		System.out.println("Time to post: " + (t8 - t7)/1000.0 + " s");
		
		
		if (dataset==null) {
			System.out.println("*** Warning dataset already exists! New dataset not created ***"); 
		} 

	}



	private void excludePropertyValues(List<String> excludedSources, List<PropertyValue> propertyValues) {
	
		if(excludedSources.size()==0) return;
		
		for (int i=0;i<propertyValues.size();i++) {
			PropertyValue pv=propertyValues.get(i);
			
			if(pv.getPublicSource()!=null) {
				if(excludedSources.contains(pv.getPublicSource().getName())) 
					propertyValues.remove(i--);				
			}
			
			if(pv.getLiteratureSource()!=null) {
				if(excludedSources.contains(pv.getLiteratureSource().getName())) 
					propertyValues.remove(i--);				
			}
		}
	}
	
	private void excludePropertyValues2(List<String> includedSources, List<PropertyValue> propertyValues) {
		
		if(includedSources.size()==0) return;
		
		for (int i=0;i<propertyValues.size();i++) {
			PropertyValue pv=propertyValues.get(i);
			
			if(pv.getPublicSource()!=null) {
				if(!includedSources.contains(pv.getPublicSource().getName())) 
					propertyValues.remove(i--);				
			}
			
			if(pv.getLiteratureSource()!=null) {
				if(!includedSources.contains(pv.getLiteratureSource().getName())) 
					propertyValues.remove(i--);				
			}
		}
	}
	
	@Deprecated
	/**
	 * Old method to store mapped records as tsv
	 * 
	 * @param unifiedPropertyValues
	 * @param datasetName
	 * @param unit
	 */
	public void saveUnifiedData(Map<String, List<MappedPropertyValue>> unifiedPropertyValues, String datasetName, Unit unit) {
	

		String datasetFileName = datasetName.replaceAll("[^A-Za-z0-9-_]+","_");
		String datasetFolderPath = DevQsarConstants.OUTPUT_FOLDER_PATH + File.separator + datasetFileName;
		String filePath = datasetFolderPath + "/unified.tsv";
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
			bw.write("EXP_PROP_ID\tSTRUCTURE\tSOURCE_CHEMICAL_NAME\tSOURCE_CASRN\tSOURCE_SMILES\tSOURCE_DTXSID\tSOURCE_DTXCID\tSOURCE_DTXRID"
					+ "\tLIT_SOURCE_NAME\tPUB_SOURCE_NAME"
					+ "\tMAPPED_DTXSID\tMAPPED_PREFERRED_NAME\tMAPPED_CASRN\tMAPPED_SMILES\tQSAR_PROPERTY_VALUE\tUNIT\r\n");
			for (String structure:unifiedPropertyValues.keySet()) {
				List<MappedPropertyValue> structurePropertyValues = unifiedPropertyValues.get(structure);
				for (MappedPropertyValue mpv:structurePropertyValues) {
					PropertyValue pv = mpv.propertyValue;
					SourceChemical sc = pv.getSourceChemical();
					String litSourceName = pv.getLiteratureSource()==null ? null : pv.getLiteratureSource().getName();
					String pubSourceName = pv.getPublicSource()==null ? null : pv.getPublicSource().getName();
					DsstoxRecord dr = mpv.dsstoxRecord;
					
					//TMM: hopefully mpv.propertyValue.getId() will get the exp_prop_id
					// Why does unified.tsv give diff number of records than GetExpPropInfo.getDataSetData() ??? Maybe there are some null values?
					//TODO make this method export same columns as GetExpPropInfo.getDataSetData() so we dont need to query the db to get this info
										
					String line = String.join("\t", String.valueOf(mpv.propertyValue.getId()), structure, sc.getSourceChemicalName(), sc.getSourceCasrn(), sc.getSourceSmiles(),
							sc.getSourceDtxsid(), sc.getSourceDtxcid(), sc.getSourceDtxrid(), litSourceName, pubSourceName,
							dr.dsstoxSubstanceId, dr.preferredName, dr.casrn, dr.smiles, String.valueOf(mpv.qsarPropertyValue),
							unit.getAbbreviation());
					bw.write(line.replaceAll("null","") + "\r\n");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves mapped data as json and xlsx
	 * 
	 * @param unifiedPropertyValues
	 * @param datasetName
	 * @param unit
	 * @param createExcel
	 */
	public void saveUnifiedData(Map<String, List<MappedPropertyValue>> unifiedPropertyValues, String datasetName, Unit unit, boolean createExcel) {
//		Map<String, List<MappedPropertyValue>> unifiedPropertyValues = unifyPropertyValuesByStructure(mappedPropertyValues, false);

		System.out.println("Enter saveUnifiedData - creates excel file for mapped records");
		
		String[] fields = { "canon_qsar_smiles","exp_prop_id", 
				"source_chemical_id","source_dtxrid","source_dtxsid", "source_dtxcid", "source_casrn", "source_smiles", "source_chemical_name",
				"mapped_dtxcid", "mapped_dtxsid", "mapped_chemical_name", "mapped_cas", "mapped_smiles", "mapped_molweight","mapped_connection_reason",
				"source_name", "source_description", "source_authors", "source_title", "source_doi", "source_url",
				"source_type", "page_url", "notes", "qc_flag", "temperature_c", "pressure_mmHg", "pH",
				"value_qualifier", "value_original", "value_text","value_max", "value_min", "value_point_estimate",
				"value_units","qsar_property_value","qsar_property_units" };

		List<String>keys=new ArrayList<>();
		
		for (String key:unifiedPropertyValues.keySet()) {
			keys.add(key);
		}
		
		String datasetFileName = datasetName.replaceAll("[^A-Za-z0-9-_]+","_");
		String datasetFolderPath = DevQsarConstants.OUTPUT_FOLDER_PATH + File.separator + datasetFileName;
		JsonArray jaAll = makeJsonArrayFromUnifiedRecords(unifiedPropertyValues, unit, keys);
		String filePathJson = datasetFolderPath + "/"+datasetFileName+"_Mapped_Records.json";
		Utilities.saveJson(jaAll, filePathJson.replace(".xlsx", ".json"));//Save to json first in case excel writing fails
		
		if(!createExcel) return;
		
		int max=50000;
		int fileNum=1;
		
		while(keys.size()>0) {
			
			List<String>keys2=new ArrayList<>();
			
			for(int i=1;i<=max;i++) {
				keys2.add(keys.remove(0));
				if(keys.size()==0) break;
			}
						
			JsonArray ja = makeJsonArrayFromUnifiedRecords(unifiedPropertyValues, unit, keys2);			
					
			try {
				String filePath=null;
				if(keys.size()<max) {
					filePath = datasetFolderPath + "/"+datasetFileName+"_Mapped_Records.xlsx";
				} else {
					filePath = datasetFolderPath + "/"+datasetFileName+"_Mapped_Records"+fileNum+".xlsx";
				}
				ExcelCreator.createExcel2(ja, filePath, fields,null);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			fileNum++;
			if(keys.size()==0) break;
		}
	}



	private JsonArray makeJsonArrayFromUnifiedRecords(Map<String, List<MappedPropertyValue>> unifiedPropertyValues,
			Unit unit, List<String> keys2) {
		JsonArray ja=new JsonArray();
		
		for (int i = 0; i < keys2.size(); i++) {
			String key=keys2.get(i);
			List<MappedPropertyValue> structurePropertyValues = unifiedPropertyValues.get(key);			
			for (MappedPropertyValue mpv:structurePropertyValues) {
				JsonObject jo = getDatapointAsJsonObject(key, mpv, unit);					
				ja.add(jo);
			}
		}
		return ja;
	}



	public static JsonObject getDatapointAsJsonObject(String structure, MappedPropertyValue mpv,Unit unit) {
				
		PropertyValue pv = mpv.propertyValue;
		SourceChemical sc = pv.getSourceChemical();
		DsstoxRecord dr = mpv.dsstoxRecord;
		
		JsonObject jo=new JsonObject();

		jo.addProperty("canon_qsar_smiles", structure);
		jo.addProperty("exp_prop_id", String.valueOf(pv.getId()));
				
		jo.addProperty("source_chemical_id", sc.getId());
		jo.addProperty("source_dtxrid", sc.getSourceDtxrid());
		jo.addProperty("source_dtxsid", sc.getSourceDtxsid());
		jo.addProperty("source_dtxcid", sc.getSourceDtxcid());
		jo.addProperty("source_casrn", sc.getSourceCasrn());
		jo.addProperty("source_smiles", sc.getSourceSmiles());
		jo.addProperty("source_chemical_name", sc.getSourceChemicalName());
		
		if (dr!=null) {
			jo.addProperty("mapped_dtxcid", dr.getDsstoxCompoundId());
			jo.addProperty("mapped_dtxsid", dr.getDsstoxSubstanceId());
			jo.addProperty("mapped_chemical_name", dr.getPreferredName());
			jo.addProperty("mapped_cas", dr.getCasrn());
			jo.addProperty("mapped_smiles", dr.getSmiles());
			jo.addProperty("mapped_molweight", dr.getMolWeight());
			jo.addProperty("mapped_connection_reason", dr.getConnectionReason());
		}

		if (pv.getLiteratureSource()!=null) {
			LiteratureSource ls=pv.getLiteratureSource();
			jo.addProperty("source_name", ls.getName());
			jo.addProperty("source_description", ls.getDescription());
			jo.addProperty("source_authors", ls.getAuthors());
			jo.addProperty("source_title", ls.getTitle());
			jo.addProperty("source_doi", ls.getDoi());
			jo.addProperty("source_url", ls.getUrl());
		} else if (pv.getPublicSource()!=null) { 
			PublicSource ps=pv.getPublicSource();
			jo.addProperty("source_name", ps.getName());
			jo.addProperty("source_description", ps.getDescription());
			jo.addProperty("source_type", ps.getType());
			jo.addProperty("source_url", ps.getUrl());
		}
		jo.addProperty("page_url", pv.getPageUrl());
		jo.addProperty("notes", pv.getNotes());
		
		jo.addProperty("qc_flag", pv.getQcFlag());
		
		jo.addProperty("value_qualifier", pv.getValueQualifier());
		jo.addProperty("value_original", pv.getValueOriginal());
		jo.addProperty("value_text", pv.getValueText());
		
		jo.addProperty("value_max", pv.getValueMax());
		jo.addProperty("value_min", pv.getValueMin());
		jo.addProperty("value_point_estimate", pv.getValuePointEstimate());
		jo.addProperty("value_units", pv.getUnit().getAbbreviation());
		
		if (pv.getParameterValue("Temperature")!=null)						
			jo.addProperty("temperature_c", pv.getParameterValue("Temperature").getValuePointEstimate());
		
		if (pv.getParameterValue("Pressure")!=null)						
			jo.addProperty("pressure_mmHg", pv.getParameterValue("Pressure").getValuePointEstimate());

		if (pv.getParameterValue("pH")!=null)						
			jo.addProperty("pH", pv.getParameterValue("pH").getValuePointEstimate());
		
		if(mpv.qsarPropertyValue!=null) {
			jo.addProperty("qsar_property_value", mpv.qsarPropertyValue);
//			jo.addProperty("qsar_property_units", unit.getAbbreviation());
			jo.addProperty("qsar_property_units", unit.getName());
		}

		return jo;
	}

	
	public void createExternalValidationSets(String datasetName1, String datasetName2) {
		List<DataPoint> dataPoints1 = dataPointService.findByDatasetName(datasetName1);
		List<DataPoint> dataPoints2 = dataPointService.findByDatasetName(datasetName2);
		
		Set<String> structures1 = dataPoints1.stream().map(dp -> dp.getCanonQsarSmiles()).collect(Collectors.toSet());
		Set<String> structures2 = dataPoints2.stream().map(dp -> dp.getCanonQsarSmiles()).collect(Collectors.toSet());
		
		List<DataPoint> inDataset1NotInDataset2 = new ArrayList<DataPoint>();
		for (DataPoint dp1:dataPoints1) {
			if (!structures2.contains(dp1.getCanonQsarSmiles())) {
				inDataset1NotInDataset2.add(dp1);
			}
		}
		
		System.out.println(inDataset1NotInDataset2.size());
		
		List<DataPoint> inDataset2NotInDataset1 = new ArrayList<DataPoint>();
		for (DataPoint dp2:dataPoints2) {
			if (!structures1.contains(dp2.getCanonQsarSmiles())) {
				inDataset2NotInDataset1.add(dp2);
			}
		}
		
		System.out.println(inDataset2NotInDataset1.size());
		
		Dataset dataset1 = datasetService.findByName(datasetName1);
		Dataset dataset2 = datasetService.findByName(datasetName2);
		
		if (!inDataset1NotInDataset2.isEmpty()) {
			Dataset dataset1NotInDataset2 = new Dataset("Data from " + datasetName1 + " external to " + datasetName2, 
					"Data points from " + datasetName1 + " with structures not found in " + datasetName2, 
					dataset1.getProperty(), 
					dataset1.getUnit(), null,
					dataset1.getDsstoxMappingStrategy(), 
					lanId);
			dataset1NotInDataset2 = initializeDataset(dataset1NotInDataset2);
			
			for (DataPoint dp:inDataset1NotInDataset2) {
				DataPoint dataPoint = new DataPoint(dp.getCanonQsarSmiles(), 
						dp.getQsarPropertyValue(), dataset1NotInDataset2, false, lanId);
				try {
					dataPointService.create(dataPoint);
					for (DataPointContributor dpc:dp.getDataPointContributors()) {
//						DataPointContributor dataPointContributor = new DataPointContributor(dataPoint, dpc.getExpPropId(), lanId);						
						DataPointContributor dataPointContributor = new DataPointContributor(dataPoint, dpc.getExp_prop_property_values_id(), dpc.getDtxcid(), lanId);
						
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
		
		if (!inDataset2NotInDataset1.isEmpty()) {
			Dataset dataset2NotInDataset1 = new Dataset("Data from " + datasetName2 + " external to " + datasetName1, 
					"Data points from " + datasetName2 + " with structures not found in " + datasetName1, 
					dataset2.getProperty(), 
					dataset2.getUnit(), null,
					dataset2.getDsstoxMappingStrategy(), 
					lanId);
			dataset2NotInDataset1 = initializeDataset(dataset2NotInDataset1);
			
			for (DataPoint dp:inDataset2NotInDataset1) {
				DataPoint dataPoint = new DataPoint(dp.getCanonQsarSmiles(), 
						dp.getQsarPropertyValue(), dataset2NotInDataset1, false, lanId);
				try {
					dataPointService.create(dataPoint);
					for (DataPointContributor dpc:dp.getDataPointContributors()) {
						DataPointContributor dataPointContributor = new DataPointContributor(dataPoint, dpc.getExp_prop_property_values_id(), dpc.getDtxcid(), lanId);
						
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
	}
	

}
