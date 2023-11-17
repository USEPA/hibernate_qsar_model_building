package gov.epa.run_from_java.data_loading;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolationException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.Parameter;
import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterAcceptableUnit;
import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyAcceptableParameter;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyAcceptableUnit;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropPropertyService;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropPropertyServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropUnitService;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropUnitServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.LiteratureSourceService;
import gov.epa.databases.dev_qsar.exp_prop.service.LiteratureSourceServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.ParameterAcceptableUnitService;
import gov.epa.databases.dev_qsar.exp_prop.service.ParameterAcceptableUnitServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.ParameterService;
import gov.epa.databases.dev_qsar.exp_prop.service.ParameterServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyAcceptableParameterService;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyAcceptableParameterServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyAcceptableUnitService;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyAcceptableUnitServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueService;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.PublicSourceService;
import gov.epa.databases.dev_qsar.exp_prop.service.PublicSourceServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.SourceChemicalService;
import gov.epa.databases.dev_qsar.exp_prop.service.SourceChemicalServiceImpl;
import gov.epa.databases.chemprop.ChemPropParameter;
import gov.epa.databases.chemprop.ChemPropParameterValue;
import gov.epa.databases.chemprop.CollectionDetail;
import gov.epa.databases.chemprop.Endpoint;
import gov.epa.databases.chemprop.MeasuredProperty;
import gov.epa.databases.chemprop.SourceDetail;
import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;

public class ChemPropLoader {
	Map<String, LiteratureSource> literatureSourcesMap = new HashMap<String, LiteratureSource>();
	Map<String, Parameter> parametersMap = new HashMap<String, Parameter>();
	Map<String, ExpPropProperty> propertiesMap = new HashMap<String, ExpPropProperty>();
	Map<String, PublicSource> publicSourcesMap = new HashMap<String, PublicSource>();
	Map<String, ExpPropUnit> unitsMap = new HashMap<String, ExpPropUnit>();
	
	Map<String, Set<String>> propertyAcceptableParametersMap = new HashMap<String, Set<String>>();
	Map<String, Set<String>> propertyAcceptableUnitsMap = new HashMap<String, Set<String>>();
	Map<String, Set<String>> parameterAcceptableUnitsMap = new HashMap<String, Set<String>>();
	
	private LiteratureSourceService literatureSourceService = new LiteratureSourceServiceImpl();
	private ParameterService parameterService = new ParameterServiceImpl();
	private ExpPropPropertyService expPropPropertyService = new ExpPropPropertyServiceImpl();
	private PublicSourceService publicSourceService = new PublicSourceServiceImpl();
	private ExpPropUnitService expPropUnitService = new ExpPropUnitServiceImpl();
	private SourceChemicalService sourceChemicalService = new SourceChemicalServiceImpl();
	private PropertyValueService propertyValueService = new PropertyValueServiceImpl();
	
	private PropertyAcceptableParameterService propertyAcceptableParameterService = new PropertyAcceptableParameterServiceImpl();
	private PropertyAcceptableUnitService propertyAcceptableUnitService = new PropertyAcceptableUnitServiceImpl();
	private ParameterAcceptableUnitService parameterAcceptableUnitService = new ParameterAcceptableUnitServiceImpl();
	
	private static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	private static final String JSON_FOLDER_PATH = "data/chemprop";
	private static String[] ignoreCollectionNames = { "PhysPropNCCT", // OPERA duplicate
			"PubMed Counts", // Not interested
			"HTTK_Package_Data", // Not interested
			"EPACDR", // Not interested
			"OECD_Toolbox", // Duplicate
			"NICEATM_LLNA", // Duplicate
			"eChemPortal" // Duplicate
			};
	
	public ChemPropLoader() {
//		init();
	}
	
	private void init() {
		List<LiteratureSource> literatureSources = literatureSourceService.findAll();
		for (LiteratureSource ls:literatureSources) {
			literatureSourcesMap.put(ls.getName(), ls);
		}
		
		List<Parameter> parameters = parameterService.findAll();
		for (Parameter p:parameters) {
			parametersMap.put(p.getName(), p);
		}
		
		List<ExpPropProperty> properties = expPropPropertyService.findAll();
		for (ExpPropProperty p:properties) {
			propertiesMap.put(p.getName(), p);
		}
		
		List<PublicSource> publicSources = publicSourceService.findAll();
		for (PublicSource ps:publicSources) {
			publicSourcesMap.put(ps.getName(), ps);
		}
		
		List<ExpPropUnit> units = expPropUnitService.findAll();
		for (ExpPropUnit u:units) {
			unitsMap.put(u.getName(), u);
		}
		
		List<PropertyAcceptableParameter> propertyAcceptableParameters = propertyAcceptableParameterService.findAll();
		for (PropertyAcceptableParameter pap:propertyAcceptableParameters) {
			String propertyName = pap.getProperty().getName();
			Set<String> acceptableParameters = propertyAcceptableParametersMap.get(propertyName);
			if (acceptableParameters==null) {
				acceptableParameters = new HashSet<String>();
			}
			
			String parameterName = pap.getParameter().getName();
			acceptableParameters.add(parameterName);
			
			propertyAcceptableParametersMap.put(propertyName, acceptableParameters);
		}
		
		List<PropertyAcceptableUnit> propertyAcceptableUnits = propertyAcceptableUnitService.findAll();
		for (PropertyAcceptableUnit pau:propertyAcceptableUnits) {
			String propertyName = pau.getProperty().getName();
			Set<String> acceptableUnits = propertyAcceptableUnitsMap.get(propertyName);
			if (acceptableUnits==null) {
				acceptableUnits = new HashSet<String>();
			}
			
			String unitName = pau.getUnit().getName();
			acceptableUnits.add(unitName);
			
			propertyAcceptableUnitsMap.put(propertyName, acceptableUnits);
		}
		
		List<ParameterAcceptableUnit> parameterAcceptableUnits = parameterAcceptableUnitService.findAll();
		for (ParameterAcceptableUnit par_au:parameterAcceptableUnits) {
			String parameterName = par_au.getParameter().getName();
			Set<String> acceptableUnits = parameterAcceptableUnitsMap.get(parameterName);
			if (acceptableUnits==null) {
				acceptableUnits = new HashSet<String>();
			}
			
			String unitName = par_au.getUnit().getName();
			acceptableUnits.add(unitName);
			
			parameterAcceptableUnitsMap.put(parameterName, acceptableUnits);
		}
	}
	
	public SourceChemical getSourceChemical(MeasuredProperty mp, String lanId) {
		SourceChemical sc = new SourceChemical();
		sc.setSourceDtxrid(mp.generateDtxrid());
		sc.setCreatedBy(lanId);
		return sc;
	}
	
	public List<MeasuredProperty> loadAllTables(String restrictEndpointName, String lanId) {
		HashMap<Long, CollectionDetail> collectionDetails = CollectionDetail.getTableFromJsonFiles(JSON_FOLDER_PATH);
		HashMap<Long, Endpoint> endpoints = Endpoint.getTableFromJsonFiles(JSON_FOLDER_PATH);
		HashMap<Long, MeasuredProperty> measuredProperties = MeasuredProperty.getTableFromJsonFiles(JSON_FOLDER_PATH);
		HashMap<Long, ChemPropParameter> originalParameters = ChemPropParameter.getTableFromJsonFiles(JSON_FOLDER_PATH);
		HashMap<Long, ChemPropParameterValue> originalParameterValues = ChemPropParameterValue.getTableFromJsonFiles(JSON_FOLDER_PATH);
		HashMap<Long, SourceDetail> sourceDetails = SourceDetail.getTableFromJsonFiles(JSON_FOLDER_PATH);
		
		List<String> badCollectionNamesList = Arrays.asList(ignoreCollectionNames);

		List<MeasuredProperty> failedMeasuredProperties = new ArrayList<MeasuredProperty>();
		int count = 0;
		for (Long id:measuredProperties.keySet()) {
			if (count < 867) {
				count++;
				continue;
			}
			
			MeasuredProperty measuredProperty = measuredProperties.get(id);
			
			// It's labeled "measurement method" but keyed to "collection detail"--Chris, why?!
			Long collectionDetailId = measuredProperty.fk_measurement_method_id;
			CollectionDetail collectionDetail = collectionDetails.get(collectionDetailId);
			
			if (collectionDetail!=null && collectionDetail.collection_type!=null && collectionDetail.collection_type.equals("ECHA")) {
				continue;
			} else if (collectionDetail!=null && badCollectionNamesList.contains(collectionDetail.name)) {
				continue;
			}
			
			Long endpointId = measuredProperty.fk_endpoint_id;
			Endpoint endpoint = endpoints.get(endpointId);
			
			if (endpoint==null || !endpoint.name.equals(restrictEndpointName)) {
				continue; // Just load one endpoint at a time for now
			}
			
			Long sourceDetailId = measuredProperty.fk_source_detail_id;
			SourceDetail sourceDetail = sourceDetails.get(sourceDetailId);
			
			String propertyName = correctPropertyName(endpoint.name);
			String unitName = correctUnitName(endpoint.standard_unit, endpoint.name);

			ExpPropProperty property = propertiesMap.get(propertyName);
			if (property==null) {
				property = new ExpPropProperty(propertyName, endpoint.description, lanId);
				property = expPropPropertyService.create(property);
				propertiesMap.put(propertyName, property);
			}
			
			ExpPropUnit unit = unitsMap.get(unitName);
			if (unit==null) {
				unit = new ExpPropUnit(unitName, unitName, lanId);
				unit = expPropUnitService.create(unit);
				unitsMap.put(unitName, unit);
			}
			
			Set<String> acceptableUnits = propertyAcceptableUnitsMap.get(propertyName);
			if (!acceptableUnits.contains(unitName)) {
				PropertyAcceptableUnit pau = new PropertyAcceptableUnit(property, unit, lanId);
				propertyAcceptableUnitService.create(pau);
				acceptableUnits.add(unitName);
				propertyAcceptableUnitsMap.put(propertyName, acceptableUnits);
			}
			
			PropertyValue propertyValue = measuredProperty.asPropertyValue(lanId);
			propertyValue.setProperty(property);
			propertyValue.setUnit(unit);
			
			SourceChemical sourceChemical = getSourceChemical(measuredProperty, lanId);
			
			if (collectionDetail!=null) {
				PublicSource ps = publicSourcesMap.get(collectionDetail.name);
				if (ps==null) {
					ps = collectionDetail.asPublicSource(lanId);
					ps = publicSourceService.create(ps);
					publicSourcesMap.put(ps.getName(), ps);
				}
				propertyValue.setPublicSource(ps);
				sourceChemical.setPublicSource(ps);
			}
			
			if (sourceDetail!=null) {
				LiteratureSource ls = literatureSourcesMap.get(sourceDetail.name);
				if (ls==null) {
					ls = sourceDetail.asLiteratureSource(lanId);
					ls = literatureSourceService.create(ls);
					literatureSourcesMap.put(ls.getName(), ls);
				}
				propertyValue.setLiteratureSource(ls);
				sourceChemical.setLiteratureSource(ls);
			}
			
			SourceChemical dbSourceChemical = sourceChemicalService.findMatch(sourceChemical);
			if (dbSourceChemical==null) {
				try {
					sourceChemical = sourceChemicalService.create(sourceChemical);
				} catch (ConstraintViolationException e) {
					failedMeasuredProperties.add(measuredProperty);
					continue;
				}
			} else {
				sourceChemical = dbSourceChemical;
			}
			propertyValue.setSourceChemical(sourceChemical);
			
			for (Long opvId:originalParameterValues.keySet()) {
				ChemPropParameterValue opv = originalParameterValues.get(opvId);
				if (opv.fk_parameter_set_id==measuredProperty.fk_parameter_set_id) {
					ParameterValue pv = opv.asParameterValue(lanId);
					
					ChemPropParameter op = originalParameters.get(opv.fk_parameter_id);
					Parameter p = parametersMap.get(op.name);
					if (p==null) {
						p = new Parameter(op.name, op.description, lanId);
						p = parameterService.create(p);
						parametersMap.put(op.name, p);
					}
					
					Set<String> acceptableParameters = propertyAcceptableParametersMap.get(propertyName);
					if (!acceptableParameters.contains(p.getName())) {
						PropertyAcceptableParameter pap = new PropertyAcceptableParameter(property, p, lanId);
						propertyAcceptableParameterService.create(pap);
						acceptableParameters.add(p.getName());
						propertyAcceptableParametersMap.put(propertyName, acceptableParameters);
					}
					
					ExpPropUnit u = unitsMap.get(op.standard_unit);
					if (u==null) {
						u = new ExpPropUnit(op.standard_unit, op.standard_unit, lanId);
						u = expPropUnitService.create(u);
						unitsMap.put(op.standard_unit, u);
					}
					
					Set<String> parAcceptableUnits = parameterAcceptableUnitsMap.get(p.getName());
					if (!parAcceptableUnits.contains(u.getName())) {
						ParameterAcceptableUnit par_au = new ParameterAcceptableUnit(p, u, lanId);
						parameterAcceptableUnitService.create(par_au);
						parAcceptableUnits.add(u.getName());
						parameterAcceptableUnitsMap.put(p.getName(), parAcceptableUnits);
					}
					
					pv.setParameter(p);
					pv.setUnit(u);
					
					propertyValue.addParameterValue(pv);
				}
			}
			
			try {
				propertyValue = propertyValueService.create(propertyValue);
			} catch (ConstraintViolationException e) {
				failedMeasuredProperties.add(measuredProperty);
				continue;
			}
			
			if (propertyValue.getId()==null) {
				failedMeasuredProperties.add(measuredProperty);
			} else {
				count++;
			}
			
			if (count % 1000==0) {
				System.out.println("Loaded " + count + " records");
			}
		}
		
		System.out.println("Loaded " + count + " records");
		System.out.println("Failed to load " + failedMeasuredProperties.size() + " records");
		
		return failedMeasuredProperties;
	}
	
	public List<MeasuredProperty> testLoadAllTables(String restrictEndpointName, String lanId) {
		HashMap<Long, CollectionDetail> collectionDetails = CollectionDetail.getTableFromJsonFiles2(JSON_FOLDER_PATH);
		HashMap<Long, Endpoint> endpoints = Endpoint.getTableFromJsonFiles2(JSON_FOLDER_PATH);
		HashMap<Long, MeasuredProperty> measuredProperties = MeasuredProperty.getTableFromJsonFiles2(JSON_FOLDER_PATH);
		HashMap<Long, ChemPropParameter> originalParameters = ChemPropParameter.getTableFromJsonFiles2(JSON_FOLDER_PATH);
		HashMap<Long, ChemPropParameterValue> originalParameterValues = ChemPropParameterValue.getTableFromJsonFiles2(JSON_FOLDER_PATH);
		HashMap<Long, SourceDetail> sourceDetails = SourceDetail.getTableFromJsonFiles2(JSON_FOLDER_PATH);
		
		if(true) return null;

		
		List<String> badCollectionNamesList = Arrays.asList(ignoreCollectionNames);

		List<MeasuredProperty> failedMeasuredProperties = new ArrayList<MeasuredProperty>();
		int count = 0;
		
		for (Long id:measuredProperties.keySet()) {
			if (count < 867) {//TODO why are these skipped? TMM
				count++;
				continue;
			}
			
			MeasuredProperty measuredProperty = measuredProperties.get(id);
			
			// It's labeled "measurement method" but keyed to "collection detail"--Chris, why?!
			Long collectionDetailId = measuredProperty.fk_measurement_method_id;
			CollectionDetail collectionDetail = collectionDetails.get(collectionDetailId);
			
			if (collectionDetail!=null && collectionDetail.collection_type!=null && collectionDetail.collection_type.equals("ECHA")) {
				continue;
			} else if (collectionDetail!=null && badCollectionNamesList.contains(collectionDetail.name)) {
				continue;
			}
			
			Long endpointId = measuredProperty.fk_endpoint_id;
			Endpoint endpoint = endpoints.get(endpointId);
			
			if (endpoint==null || !endpoint.name.equals(restrictEndpointName)) {
				continue; // Just load one endpoint at a time for now
			}
			
			Long sourceDetailId = measuredProperty.fk_source_detail_id;
			SourceDetail sourceDetail = sourceDetails.get(sourceDetailId);
			
			String propertyName = correctPropertyName(endpoint.name);
			String unitName = correctUnitName(endpoint.standard_unit, endpoint.name);

			ExpPropProperty property = propertiesMap.get(propertyName);
			if (property==null) {
				property = new ExpPropProperty(propertyName, endpoint.description, lanId);
				property = expPropPropertyService.create(property);
				propertiesMap.put(propertyName, property);
			}
			
			ExpPropUnit unit = unitsMap.get(unitName);
			if (unit==null) {
				unit = new ExpPropUnit(unitName, unitName, lanId);
				unit = expPropUnitService.create(unit);
				unitsMap.put(unitName, unit);
			}
			
			Set<String> acceptableUnits = propertyAcceptableUnitsMap.get(propertyName);
			if (!acceptableUnits.contains(unitName)) {
				PropertyAcceptableUnit pau = new PropertyAcceptableUnit(property, unit, lanId);
				propertyAcceptableUnitService.create(pau);
				acceptableUnits.add(unitName);
				propertyAcceptableUnitsMap.put(propertyName, acceptableUnits);
			}
			
			PropertyValue propertyValue = measuredProperty.asPropertyValue(lanId);
			propertyValue.setProperty(property);
			propertyValue.setUnit(unit);
			
			SourceChemical sourceChemical = getSourceChemical(measuredProperty, lanId);
			
			if (collectionDetail!=null) {
				PublicSource ps = publicSourcesMap.get(collectionDetail.name);
				if (ps==null) {
					ps = collectionDetail.asPublicSource(lanId);
					ps = publicSourceService.create(ps);
					publicSourcesMap.put(ps.getName(), ps);
				}
				propertyValue.setPublicSource(ps);
				sourceChemical.setPublicSource(ps);
			}
			
			if (sourceDetail!=null) {
				LiteratureSource ls = literatureSourcesMap.get(sourceDetail.name);
				if (ls==null) {
					ls = sourceDetail.asLiteratureSource(lanId);
					ls = literatureSourceService.create(ls);
					literatureSourcesMap.put(ls.getName(), ls);
				}
				propertyValue.setLiteratureSource(ls);
				sourceChemical.setLiteratureSource(ls);
			}
			
			SourceChemical dbSourceChemical = sourceChemicalService.findMatch(sourceChemical);
			if (dbSourceChemical==null) {
				try {
					sourceChemical = sourceChemicalService.create(sourceChemical);
				} catch (ConstraintViolationException e) {
					failedMeasuredProperties.add(measuredProperty);
					continue;
				}
			} else {
				sourceChemical = dbSourceChemical;
			}
			propertyValue.setSourceChemical(sourceChemical);
			
			for (Long opvId:originalParameterValues.keySet()) {
				ChemPropParameterValue opv = originalParameterValues.get(opvId);
				if (opv.fk_parameter_set_id==measuredProperty.fk_parameter_set_id) {
					ParameterValue pv = opv.asParameterValue(lanId);
					
					ChemPropParameter op = originalParameters.get(opv.fk_parameter_id);
					Parameter p = parametersMap.get(op.name);
					if (p==null) {
						p = new Parameter(op.name, op.description, lanId);
						p = parameterService.create(p);
						parametersMap.put(op.name, p);
					}
					
					Set<String> acceptableParameters = propertyAcceptableParametersMap.get(propertyName);
					if (!acceptableParameters.contains(p.getName())) {
						PropertyAcceptableParameter pap = new PropertyAcceptableParameter(property, p, lanId);
						propertyAcceptableParameterService.create(pap);
						acceptableParameters.add(p.getName());
						propertyAcceptableParametersMap.put(propertyName, acceptableParameters);
					}
					
					ExpPropUnit u = unitsMap.get(op.standard_unit);
					if (u==null) {
						u = new ExpPropUnit(op.standard_unit, op.standard_unit, lanId);
						u = expPropUnitService.create(u);
						unitsMap.put(op.standard_unit, u);
					}
					
					Set<String> parAcceptableUnits = parameterAcceptableUnitsMap.get(p.getName());
					if (!parAcceptableUnits.contains(u.getName())) {
						ParameterAcceptableUnit par_au = new ParameterAcceptableUnit(p, u, lanId);
						parameterAcceptableUnitService.create(par_au);
						parAcceptableUnits.add(u.getName());
						parameterAcceptableUnitsMap.put(p.getName(), parAcceptableUnits);
					}
					
					pv.setParameter(p);
					pv.setUnit(u);
					
					propertyValue.addParameterValue(pv);
				}
			}
			
			try {
				propertyValue = propertyValueService.create(propertyValue);
			} catch (ConstraintViolationException e) {
				failedMeasuredProperties.add(measuredProperty);
				continue;
			}
			
			if (propertyValue.getId()==null) {
				failedMeasuredProperties.add(measuredProperty);
			} else {
				count++;
			}
			
			if (count % 1000==0) {
				System.out.println("Loaded " + count + " records");
			}
		}
		
		System.out.println("Loaded " + count + " records");
		System.out.println("Failed to load " + failedMeasuredProperties.size() + " records");
		
		return failedMeasuredProperties;
	}
	
	private static String correctUnitName(String unitName, String endpointName) {
		if (unitName==null) {
			if (endpointName.contains("Log") || endpointName.contains("pKa")) {
				unitName = "Log units";
			} else if (endpointName.contains("Class") || endpointName.contains("Concern") || endpointName.contains("Binary")
					|| endpointName.contains("Mutagenicity") || endpointName.contains("Developmental Toxicity")) {
				unitName = "Binary";
			} else if (endpointName.contains("Factor") || endpointName.contains("Fraction") || endpointName.contains("Index") || endpointName.contains("Constant")) {
				unitName = "Dimensionless";
			} else if (endpointName.contains("count")) {
				unitName = "Count";
			} else if (endpointName.contains("Volume") || endpointName.contains("Production")) {
				unitName = "Pounds";
			} else {
				System.out.println("Please handle units for: " + endpointName);
			}
		} else if (unitName.equals("°C")) {
			unitName = DevQsarConstants.DEG_C;
		} else if (unitName.equals("mol/L")) {
			unitName = DevQsarConstants.MOLAR;
		} else if (unitName.equals("g/cm^3")) {
			unitName = DevQsarConstants.G_CM3;
		} else if (unitName.equals("atm-m3/mole")) {
			unitName = DevQsarConstants.ATM_M3_MOL;
		}
		
		return unitName;
	}
	
	private static String correctPropertyName(String endpointName) {
		// Done:
		// Density
		switch (endpointName) {
		case "Water Solubility": // Done
			endpointName = DevQsarConstants.WATER_SOLUBILITY;
			break;
		case "Melting Point": // Done
			endpointName = DevQsarConstants.MELTING_POINT;
			break;
		case "Boiling Point": // Done
			endpointName = DevQsarConstants.BOILING_POINT;
			break;
		case "Flash Point": // Done
			endpointName = DevQsarConstants.FLASH_POINT;
			break;
		case "LogKow: Octanol-Water": // Done
			endpointName = DevQsarConstants.LOG_KOW;
			break;
		case "Vapor Pressure": // Done
			endpointName = DevQsarConstants.VAPOR_PRESSURE;
			break;
		case "Henry's Law": // Done
			endpointName = DevQsarConstants.HENRYS_LAW_CONSTANT;
			break;
		case "pKa Acidic Apparent": // Done
			endpointName = DevQsarConstants.PKA_A;
			break;
		case "pKa Basic Apparent": // Done
			endpointName = DevQsarConstants.PKA_B;
			break;
		case "Bioconcentration Factor":
			endpointName = DevQsarConstants.LOG_BCF;
			break;
		case "Atmos. Hydroxylation Rate":
			endpointName = DevQsarConstants.LOG_OH;
			break;
		case "Soil Adsorp. Coeff. (Koc)":
			endpointName = DevQsarConstants.LOG_KOC;
			break;
		case "Biodeg. Half-Life":
			endpointName = DevQsarConstants.LOG_HALF_LIFE;
			break;
		case "Fish Biotrans. Half-Life (Km)":
			endpointName = DevQsarConstants.LOG_KM_HL;
			break;
		case "LogKoa: Octanol-Air":
			endpointName = DevQsarConstants.LOG_KOA;
			break;
		}

		return endpointName;
	}
	
	void runEndpoints(String [] endpoints) {
		for (String endpoint:endpoints) {
			ChemPropLoader loader = new ChemPropLoader();
			List<MeasuredProperty> failedMeasuredProperties = loader.loadAllTables(endpoint, "gsincl01");
			if (!failedMeasuredProperties.isEmpty()) {
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(JSON_FOLDER_PATH + "/failed/" + endpoint + "_021622.json"))) {
					Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
					bw.write(gson.toJson(failedMeasuredProperties));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
	}
	
	void testRunEndpoints(String [] endpoints) {
		for (String endpoint:endpoints) {
			List<MeasuredProperty> failedMeasuredProperties = testLoadAllTables(endpoint, "gsincl01");
			
//			if (!failedMeasuredProperties.isEmpty()) {
//				try (BufferedWriter bw = new BufferedWriter(new FileWriter(JSON_FOLDER_PATH + "/failed/" + endpoint + "_021622.json"))) {
//					Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
//					bw.write(gson.toJson(failedMeasuredProperties));
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
		}
	}

	
	public static void main(String[] args) {
		ChemPropLoader c=new ChemPropLoader();
		String[] endpoints = { "LogKow: Octanol-Water" };
//		c.runEndpoints(endpoints);
		c.testRunEndpoints(endpoints);
	}
}
