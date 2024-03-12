package gov.epa.run_from_java.data_loading;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.validation.ConstraintViolationException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

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
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.endpoints.datasets.DatasetCreator;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.OPERA.OPERA_lookups;
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
	static boolean loadSourceChemicalMap=true;
	Map<String, LiteratureSource> literatureSourcesMap = new HashMap<String, LiteratureSource>();
	Map<String, Parameter> parametersMap = new HashMap<String, Parameter>();
	Map<String, ExpPropProperty> propertiesMap = new HashMap<String, ExpPropProperty>();
	Map<String, PublicSource> publicSourcesMap = new HashMap<String, PublicSource>();
	Map<String, ExpPropUnit> unitsMap = new HashMap<String, ExpPropUnit>();
	
	Map<String, Set<String>> propertyAcceptableParametersMap = new HashMap<String, Set<String>>();
	Map<String, Set<String>> propertyAcceptableUnitsMap = new HashMap<String, Set<String>>();
	Map<String, Set<String>> parameterAcceptableUnitsMap = new HashMap<String, Set<String>>();
	
	Map<String, SourceChemical> sourceChemicalMap = new HashMap<String, SourceChemical>();

	
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

	private static String[] ignoreCollectionNames = { 
			"PhysPropNCCT", // OPERA duplicate (came from EPISUITE)
//			"Data Warrior", // OPERA duplicate 
			"PubMed Counts", // Not interested
//			"HTTK_Package_Data", // Not interested
			"EPACDR", // Not interested
			"OECD_Toolbox", // Duplicate
			"NICEATM_LLNA", // Duplicate
			"eChemPortal" // Duplicate
			};
//	NCCT_Physchem- ok but might be same as OPERA
	
	public ChemPropLoader(boolean doInit, boolean loadSCmap) {
		loadSourceChemicalMap=loadSCmap;
		if(doInit) init();
	}
	
	private void init() {
		
		//TODO add sourceChemical map to avoid duplicating source chemicals!
		
		
		List<LiteratureSource> literatureSources = literatureSourceService.findAll();
		for (LiteratureSource ls:literatureSources) {
			literatureSourcesMap.put(ls.getCitation(), ls);//use citation instead of a name field because Author_Year is hardly unique
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
		
		if (loadSourceChemicalMap) {
			System.out.print("Loading sourceChemical map...");
			List<SourceChemical> sourceChemicals = sourceChemicalService.findAll();
			for (SourceChemical sourceChemical:sourceChemicals) {
				sourceChemicalMap.put(sourceChemical.getKey(),sourceChemical);
			}
			System.out.println("Done");
		}

	}
	
	public SourceChemical getSourceChemical(MeasuredProperty mp, String lanId) {
		SourceChemical sc = new SourceChemical();
		sc.setSourceDtxrid(mp.generateDtxrid());
		sc.setCreatedBy(lanId);
		return sc;
	}
	
	/**
	 * Loads data into following tables: 
	 * 
	 * property_values
	 * units XXX
	 * public_sources
	 * literature_sources
	 * source_chemicals
	 * parameters 
	 * parameters_acceptable_units
	 * properties XXX
	 * properties_acceptable_units
	 * properties_acceptable_parameters
	 * 
	 * @param restrictEndpointName
	 * @param lanId
	 * @return
	 */
	public List<MeasuredProperty> loadAllTables(String restrictEndpointName, String lanId,boolean postToDB) {
		HashMap<Long, CollectionDetail> collectionDetails = CollectionDetail.getTableFromJsonFiles2(JSON_FOLDER_PATH);
		HashMap<Long, Endpoint> endpoints = Endpoint.getTableFromJsonFiles2(JSON_FOLDER_PATH);
		HashMap<Long, MeasuredProperty> measuredProperties = MeasuredProperty.getTableFromJsonFiles2(JSON_FOLDER_PATH);
		HashMap<Long, ChemPropParameter> originalParameters = ChemPropParameter.getTableFromJsonFiles2(JSON_FOLDER_PATH);
		HashMap<Long, ChemPropParameterValue> originalParameterValues = ChemPropParameterValue.getTableFromJsonFiles2(JSON_FOLDER_PATH);
		HashMap<Long, SourceDetail> sourceDetails = SourceDetail.getTableFromJsonFiles2(JSON_FOLDER_PATH);
		
		List<String> badCollectionNamesList = Arrays.asList(ignoreCollectionNames);

		List<MeasuredProperty> failedMeasuredProperties = new ArrayList<MeasuredProperty>();
		int count = 0;
		int countProperty=0;

		for (Long id:measuredProperties.keySet()) {
			
			count++;
			
			if (count % 10000==0 && count>0) {
				System.out.println("Processed " + count + " records");
			}

			MeasuredProperty measuredProperty = measuredProperties.get(id);
			
//			if (count < 867) {//TMM: Why did gabriel skip the first 867?
//				System.out.println(measuredProperty.fk_endpoint_id);
//				count++;
//				continue;
//			}
			
			// It's labeled "measurement method" but keyed to "collection detail"--Chris, why?!
			Long collectionDetailId = measuredProperty.fk_measurement_method_id;
			CollectionDetail collectionDetail = collectionDetails.get(collectionDetailId);
			
			if (collectionDetail!=null && collectionDetail.collection_type!=null && 
					collectionDetail.collection_type.equals("ECHA")) {
				continue;
			} else if (collectionDetail!=null && 
					badCollectionNamesList.contains(collectionDetail.name)) {
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
			
			if(acceptableUnits==null) {
				acceptableUnits=new HashSet<String>();
			}

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
				
				//TMM: not sure how gabriel was able to correctly identify the collection detail records that 
			    // were journal articles (should be stored as literature_sources and not public_sources) 
				// but here is my work around:				
				// TODO should we store public source as chemprop?
				
				if (isLiteratureSource(collectionDetail.name))  {
					String citationDB=getExistingCitationInDB(collectionDetail.name);


					LiteratureSource ls=null;
					LiteratureSource lsNew =collectionDetail.asLiteratureSource(lanId);
					
					if(citationDB!=null) {
						ls=literatureSourcesMap.get(citationDB);
						System.out.println("We have match for "+ls.getAuthor()+"_"+ls.getYear()+"\t"+collectionDetail.name);
					} else {
						ls=literatureSourcesMap.get(lsNew.getCitation());

						if (ls==null) {
							ls = lsNew;
							ls = literatureSourceService.create(ls);
							literatureSourcesMap.put(ls.getCitation(), ls);
						} else {
							System.out.println("We have match for "+collectionDetail.name);
						}
					}
					
					propertyValue.setLiteratureSource(ls);
					sourceChemical.setLiteratureSource(ls);
					
				} else {
					PublicSource ps = publicSourcesMap.get(collectionDetail.name);
					if (ps==null) {
						ps = collectionDetail.asPublicSource(lanId);
						ps = publicSourceService.create(ps);
						publicSourcesMap.put(ps.getName(), ps);
					}
					propertyValue.setPublicSource(ps);
					sourceChemical.setPublicSource(ps);
					
				}
				
				
			}
			
			if (sourceDetail!=null) {//only Bioconcentration Factor has sourceDetails since it came from ecotox
				
				LiteratureSource lsNew = sourceDetail.asLiteratureSource(lanId);
				LiteratureSource ls=literatureSourcesMap.get(lsNew.getCitation());//previously gabriel looked up by sourceDetailId from chemprop but our database autonumbers the id field in the literature_sources table so cant match this way
				
				if (ls==null) {
					ls = lsNew;
					ls = literatureSourceService.create(ls);
					literatureSourcesMap.put(ls.getCitation(), ls);
				} else {
					System.out.println("We have match for "+ls.getAuthor()+"_"+ls.getYear()+"\t"+sourceDetail.name);
				}
				propertyValue.setLiteratureSource(ls);
				sourceChemical.setLiteratureSource(ls);
			}
			
			SourceChemical dbSourceChemical=null;
			
			if(sourceChemicalMap.containsKey(sourceChemical.getKey())) {
				dbSourceChemical=sourceChemicalMap.get(sourceChemical.getKey());
//				System.out.println("Found in map\t"+sourceChemical.getSourceDtxrid());

			} else if (!loadSourceChemicalMap) {
				dbSourceChemical = sourceChemicalService.findMatch(sourceChemical);
//				System.out.println("Found by service\t"+sourceChemical.getSourceDtxrid());
			}			
			

			
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
			
			JsonObject jo = propertyValue.createJsonObjectFromPropertyValue();
			System.out.println(gson.toJson(jo));

			
			if (postToDB) {

				try {
					propertyValue = propertyValueService.create(propertyValue);
				} catch (ConstraintViolationException e) {
					failedMeasuredProperties.add(measuredProperty);
					continue;
				}
			}
			if (propertyValue.getId()==null) {
				failedMeasuredProperties.add(measuredProperty);
			} else {
				countProperty++;
			}
			
		}
		
		System.out.println("Loaded " + count + " records");
		System.out.println("Loaded " + countProperty + " records for "+restrictEndpointName);
		System.out.println("Failed to load " + failedMeasuredProperties.size() + " records");
		
		return failedMeasuredProperties;
	}
	
	
	/**
	 * Only load the records for specific endpoint and collection detail
	 * 
	 * @param sourceName
	 * @param restrictEndpointName
	 * @param lanId
	 * @param postToDB
	 * @return
	 */
	public List<MeasuredProperty> loadAllTables(String sourceName, String restrictEndpointName, String lanId,boolean postToDB) {

		HashMap<Long, CollectionDetail> collectionDetails = CollectionDetail.getTableFromJsonFiles2(JSON_FOLDER_PATH);
		HashMap<Long, Endpoint> endpoints = Endpoint.getTableFromJsonFiles2(JSON_FOLDER_PATH);
		HashMap<Long, MeasuredProperty> measuredProperties = MeasuredProperty.getTableFromJsonFiles2(JSON_FOLDER_PATH);
		HashMap<Long, ChemPropParameter> originalParameters = ChemPropParameter.getTableFromJsonFiles2(JSON_FOLDER_PATH);
		HashMap<Long, ChemPropParameterValue> originalParameterValues = ChemPropParameterValue.getTableFromJsonFiles2(JSON_FOLDER_PATH);
		HashMap<Long, SourceDetail> sourceDetails = SourceDetail.getTableFromJsonFiles2(JSON_FOLDER_PATH);

		List<MeasuredProperty> failedMeasuredProperties = new ArrayList<>();
		List<PropertyValue> failedPropertyValues = new ArrayList<>();
		int count = 0;
		int countProperty=0;

		for (Long id:measuredProperties.keySet()) {
			
			count++;
			
			if (count % 10000==0 && count>0) {
				System.out.println("Processed " + count + " records");
			}

			MeasuredProperty measuredProperty = measuredProperties.get(id);
			
//			if (count < 867) {//TMM: Why did gabriel skip the first 867?
//				System.out.println(measuredProperty.fk_endpoint_id);
//				count++;
//				continue;
//			}
			
			// It's labeled "measurement method" but keyed to "collection detail"--Chris, why?!
			Long collectionDetailId = measuredProperty.fk_measurement_method_id;
			CollectionDetail collectionDetail = collectionDetails.get(collectionDetailId);
			
			if (collectionDetail ==null || !collectionDetail.name.equals(sourceName)) {
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
			
//			System.out.println(unitName);
			
//			if(true) return null;

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
			
			if(acceptableUnits==null) {
				acceptableUnits=new HashSet<String>();
			}

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
				
				//TMM: not sure how gabriel was able to correctly identify the collection detail records that 
			    // were journal articles (should be stored as literature_sources and not public_sources) 
				// but here is my work around:				
				// TODO should we store public source as chemprop?
				
				if (isLiteratureSource(collectionDetail.name))  {
					String citationDB=getExistingCitationInDB(collectionDetail.name);


					LiteratureSource ls=null;
					LiteratureSource lsNew =collectionDetail.asLiteratureSource(lanId);
					
					if(citationDB!=null) {
						ls=literatureSourcesMap.get(citationDB);
						System.out.println("We have match for "+ls.getAuthor()+"_"+ls.getYear()+"\t"+collectionDetail.name);
					} else {
						ls=literatureSourcesMap.get(lsNew.getCitation());

						if (ls==null) {
							ls = lsNew;
							ls = literatureSourceService.create(ls);
							literatureSourcesMap.put(ls.getCitation(), ls);
						} else {
							System.out.println("We have match for "+collectionDetail.name);
						}
					}
					
					propertyValue.setLiteratureSource(ls);
					sourceChemical.setLiteratureSource(ls);
					
				} else {
					PublicSource ps = publicSourcesMap.get(collectionDetail.name);
					if (ps==null) {
						ps = collectionDetail.asPublicSource(lanId);
						ps = publicSourceService.create(ps);
						publicSourcesMap.put(ps.getName(), ps);
					}
					propertyValue.setPublicSource(ps);
					sourceChemical.setPublicSource(ps);
					
				}
				
				
			}
			
			if (sourceDetail!=null) {//only Bioconcentration Factor has sourceDetails since it came from ecotox
				
				LiteratureSource lsNew = sourceDetail.asLiteratureSource(lanId);
				LiteratureSource ls=literatureSourcesMap.get(lsNew.getCitation());//previously gabriel looked up by sourceDetailId from chemprop but our database autonumbers the id field in the literature_sources table so cant match this way
				
				if (ls==null) {
					ls = lsNew;
					ls = literatureSourceService.create(ls);
					literatureSourcesMap.put(ls.getCitation(), ls);
				} else {
					System.out.println("We have match for "+ls.getAuthor()+"_"+ls.getYear()+"\t"+sourceDetail.name);
				}
				propertyValue.setLiteratureSource(ls);
				sourceChemical.setLiteratureSource(ls);
			}
			
			SourceChemical dbSourceChemical=null;
			
			if(sourceChemicalMap.containsKey(sourceChemical.getKey())) {
				dbSourceChemical=sourceChemicalMap.get(sourceChemical.getKey());
//				System.out.println("Found in map\t"+sourceChemical.getSourceDtxrid());

			} else if (!loadSourceChemicalMap) {
				dbSourceChemical = sourceChemicalService.findMatch(sourceChemical);
				System.out.println("Found by service\t"+sourceChemical.getSourceDtxrid());
			}			
			

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
			
			Set<String> acceptableParameters = propertyAcceptableParametersMap.get(propertyName);
			
			if(acceptableParameters==null) {
				acceptableParameters=new HashSet<String>();
			}
			
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
					
					if (!acceptableParameters.contains(p.getName())) {
						PropertyAcceptableParameter pap = new PropertyAcceptableParameter(property, p, lanId);
						propertyAcceptableParameterService.create(pap);
						acceptableParameters.add(p.getName());
						System.out.println("Adding acceptable parameter "+p.getName()+" for "+propertyName);
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
			
			JsonObject jo = propertyValue.createJsonObjectFromPropertyValue();
			System.out.println(gson.toJson(jo));
			
			
			if (postToDB) {

				try {
					propertyValue = propertyValueService.create(propertyValue);
				} catch (ConstraintViolationException e) {
					failedMeasuredProperties.add(measuredProperty);
					failedPropertyValues.add(propertyValue);
					continue;
				}
			}
			if (propertyValue.getId()==null) {
				failedMeasuredProperties.add(measuredProperty);
				failedPropertyValues.add(propertyValue);
			} else {
				countProperty++;
			}
			
		}
		
		System.out.println("Loaded " + count + " records");
		System.out.println("Loaded " + countProperty + " records for "+restrictEndpointName);
		System.out.println("Failed to load " + failedMeasuredProperties.size() + " records");
		
//		System.out.println(gson.toJson(failedPropertyValues));
		
		return failedMeasuredProperties;
	}
	
	
	private SourceChemical setSourceChemical(SourceChemical sourceChemical) {
		
		
		return sourceChemical;
	}

	boolean isLiteratureSource (String collection_details_name) {
		List<String>sources=new ArrayList<>();
		sources.add("Hughes, et. al. J. Chem. Inf. and Mod. 48.1 (2008): 220-232");
		sources.add("Kovdienko, et. al. Molecular informatics 29.5 (2010): 394-406.");
		sources.add("Lewis, et. al. \" The Pesticide Chemicals Database\"");
		sources.add("Boobier, et. al. \"Can human experts predict solubility better than computers?\"");
		sources.add("Li et al. J. Phys. Chem. Ref. Data 32(4): 1545-1590. ");
		sources.add("Kurz et al. Chemosphere 38 (1999) 573–586 ");
		sources.add("Ding et al. Crit. Reviews in Environ. Sci. and Tech., Volume 43, 2013 - Issue 6");
		sources.add("Goss et al. Environ. Sci. Technol. 2006, 40, 11, 3572-3577");
		sources.add("Tetko et al. J. Chem. Inf. Model. 2013, 53, 8, 1990-2000");
		sources.add("Rayne et al, J. Env. Sci. and Health Part A, (2009) 44(12):1145-1199");
		sources.add("Tetko et al, J Comput Aided Mol Des. 2011; 25(6):533-54");
		sources.add("Tetko et al. J. Chem. Inf. and Comp. Sci. 41.6 (2001): 1488-1493");
		sources.add("Zang, et al 2017");
		sources.add("Dreyer et al. J. Chem. Eng. Data 2009, 54(11) 3022-3025");
		sources.add("Abooali et al. Int. J. Refrig. 2014, 40, 282–293");
		sources.add("De Bruijn et al Environ. Toxicol. Chem. 8(6): 499-512");
		sources.add("Burggraaf et al. Env. Tox. and Chem., Vol. 15, No. 3, pp. 369–375, 1996 ");
		sources.add("Carmosini et al. Environ. Sci. Technol. 2008, 42, 17, 6559-6565");
		sources.add("Yalkowsky et al. Chemosphere 2002, 48, 487–509");
		sources.add("Bhhatarai et al. Environ. Sci. Technol. 2011, 45, 8120–8128.");
		sources.add("Braekevelt et al. Chemosphere 51 (2003) 563–567");
		sources.add("Hidalgo at al ");
		sources.add("Savu: Kirk‐Othmer Encyclopedia of Chemical Technology");
		return sources.contains(collection_details_name);

	}
	
	String getExistingCitationInDB(String collection_details_name) {
		Hashtable<String,String> ht=new Hashtable<>();
		ht.put("Abooali et al. Int. J. Refrig. 2014, 40, 282–293","Abooali, D., & Sobati, M. A. (2014). Novel method for prediction of normal boiling point and enthalpy of vaporization at normal boiling point of pure refrigerants: A QSPR approach. International Journal of Refrigeration, 40, 282-293. https://doi.org/https://doi.org/10.1016/j.ijrefrig.2013.12.007 ");
		ht.put("Bhhatarai et al. Environ. Sci. Technol. 2011, 45, 8120–8128.","Bhhatarai, B., & Gramatica, P. (2011). Prediction of Aqueous Solubility, Vapor Pressure and Critical Micelle Concentration for Aquatic Partitioning of Perfluorinated Chemicals [10.1021/es101181g]. Environ. Sci. Technol., 45(19), 8120-8128. https://doi.org/10.1021/es101181g ");
		ht.put("Boobier, et. al. \"Can human experts predict solubility better than computers?\"","Boobier, S., Osbourn, A., & Mitchell, J. B. O. (2017). Can human experts predict solubility better than computers? Journal of Cheminformatics, 9(1), 63. https://doi.org/10.1186/s13321-017-0250-y ");
		ht.put("Braekevelt et al. Chemosphere 51 (2003) 563–567","Braekevelt, E., Tittlemier, S., & Tomy, T. (2003). Direct measure of octanol-water partition coefficients of some environmentally relevant brominated diphenyl ether congeners. Chemosphere, 51, 563-567. https://doi.org/10.1016/S0045-6535(02)00841-X ");
		ht.put("Carmosini et al. Environ. Sci. Technol. 2008, 42, 17, 6559-6565","Carmosini, N., & Lee, L. S. (2008). Partitioning of Fluorotelomer Alcohols to Octanol and Different Sources of Dissolved Organic Carbon. Environmental science & technology, 42(17), 6559-6565. https://doi.org/10.1021/es800263t ");
		ht.put("De Bruijn et al Environ. Toxicol. Chem. 8(6): 499-512","De Bruijn, J., Busser, F., Seinen, W., & Hermens, J. (1989). Determination of octanol/water partition coefficients for hydrophobic organic chemicals with the “slow-stirring” method [https://doi.org/10.1002/etc.5620080607]. Environmental Toxicology and Chemistry, 8(6), 499-512. https://doi.org/https://doi.org/10.1002/etc.5620080607 ");
		ht.put("Ding et al. Crit. Reviews in Environ. Sci. and Tech., Volume 43, 2013 - Issue 6","Ding, G., & Peijnenburg, W. J. G. M. (2013). Physicochemical Properties and Aquatic Toxicity of Poly- and Perfluorinated Compounds. Critical Reviews in Environmental Science and Technology, 43(6), 598-678. https://doi.org/10.1080/10643389.2011.627016 ");
		ht.put("Hidalgo at al ","Hidalgo, A., & Mora-Diez, N. (2015). Novel approach for predicting partition coefficients of linear perfluorinated compounds. Theoretical Chemistry Accounts, 135(1), 18. https://doi.org/10.1007/s00214-015-1784-6 ");
		ht.put("Hughes, et. al. J. Chem. Inf. and Mod. 48.1 (2008): 220-232","Hughes, L. D., Palmer, D. S., Nigsch, F., & Mitchell, J. B. O. (2008). Why Are Some Properties More Difficult To Predict than Others? A Study of QSPR Models of Solubility, Melting Point, and Log P. Journal of Chemical Information and Modeling, 48(1), 220-232. https://doi.org/10.1021/ci700307p ");
		ht.put("Kovdienko, et. al. Molecular informatics 29.5 (2010): 394-406.","Kovdienko, N., Polishchuk, P., Muratov, E., Artemenko, A., Kuz'min, V., Gorb, L., Hill, F., & Leszczynski, J. (2010). Application of Random Forest and Multiple Linear Regression Techniques to QSPR Prediction of an Aqueous Solubility for Military Compounds. Mol Inform, 29, 394-406. https://doi.org/10.1002/minf.201000001 ");
		ht.put("Kurz et al. Chemosphere 38 (1999) 573–586 ","Kurz, J., & Ballschmiter, K. (1999). Vapour pressures, aqueous solubilities, Henry's law constants, partition coefficients between gas/water (Kgw), N-octanol/water (Kow) and gas/N-octanol (Kgo) of 106 polychlorinated diphenyl ethers (PCDE). Chemosphere, 38(3), 573-586. doi:https://doi.org/10.1016/S0045-6535(98)00212-4");
		ht.put("Lewis, et. al. \" The Pesticide Chemicals Database\"","Lewis, K. A., Tzilivakis, J., Warner, D. J., & Green, A. (2016). An international database for pesticide risk assessments and management. Human and Ecological Risk Assessment: An International Journal, 22(4), 1050-1064. https://doi.org/10.1080/10807039.2015.1133242 ");
		ht.put("Li et al. J. Phys. Chem. Ref. Data 32(4): 1545-1590. ","Li, N., Wania, F., Lei, Y. D., & Daly, G. L. (2003). A Comprehensive and Critical Compilation, Evaluation, and Selection of Physical–Chemical Property Data for Selected Polychlorinated Biphenyls. Journal of Physical and Chemical Reference Data, 32(4), 1545-1590. https://doi.org/10.1063/1.1562632 ");
		ht.put("Yalkowsky et al. Chemosphere 2002, 48, 487–509","Ran, Y., He, Y., Yang, G., Johnson, J. L. H., & Yalkowsky, S. H. (2002). Estimation of aqueous solubility of organic compounds by using the general solubility equation. Chemosphere, 48(5), 487-509. https://doi.org/https://doi.org/10.1016/S0045-6535(02)00118-2 ");
		ht.put("Rayne et al, J. Env. Sci. and Health Part A, (2009) 44(12):1145-1199","Rayne, S., & Forest, K. (2009). Perfluoroalkyl sulfonic and carboxylic acids: A critical review of physicochemical properties, levels and patterns in waters and wastewaters, and treatment methods. Journal of Environmental Science and Health, Part A, 44(12), 1145-1199. https://doi.org/10.1080/10934520903139811 ");
		ht.put("Savu: Kirk‐Othmer Encyclopedia of Chemical Technology","Savu, P. (2000). Fluorine-Containing Polymers, Perfluoroalkanesulfonic Acids. In Kirk-Othmer Encyclopedia of Chemical Technology. https://doi.org/https://doi.org/10.1002/0471238961.1605180619012221.a01 ");
		ht.put("Tetko et al, J Comput Aided Mol Des. 2011; 25(6):533-54","Sushko, I., Novotarskyi, S., Körner, R., Pandey, A. K., Rupp, M., Teetz, W., Brandmaier, S., Abdelaziz, A., Prokopenko, V. V., Tanchuk, V. Y., Todeschini, R., Varnek, A., Marcou, G., Ertl, P., Potemkin, V., Grishina, M., Gasteiger, J., Schwab, C., Baskin, I. I., . . . Tetko, I. V. (2011). Online chemical modeling environment (OCHEM): web platform for data storage, model development and publishing of chemical information. Journal of Computer-Aided Molecular Design, 25(6), 533-554. https://doi.org/10.1007/s10822-011-9440-2 ");
		ht.put("Tetko et al. J. Chem. Inf. and Comp. Sci. 41.6 (2001): 1488-1493","Tetko, I. V., Tanchuk, V. Y., Kasheva, T. N., & Villa, A. E. P. (2001). Estimation of Aqueous Solubility of Chemical Compounds Using E-State Indices. Journal of Chemical Information and Computer Sciences, 41(6), 1488-1493. https://doi.org/10.1021/ci000392t ");
		ht.put("Zang, et al 2017","Zang, Q., Mansouri, K., Williams, A. J., Judson, R. S., Allen, D. G., Casey, W. M., & Kleinstreuer, N. C. (2017). In Silico Prediction of Physicochemical Properties of Environmental Chemicals Using Molecular Fingerprints and Machine Learning. Journal of Chemical Information and Modeling, 57(1), 36-49. https://doi.org/10.1021/acs.jcim.6b00625 ");
		return ht.get(collection_details_name);
		
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
				//Skip ECHA data
				continue;
			} else if (collectionDetail!=null && badCollectionNamesList.contains(collectionDetail.name)) {
				//Skip ones in bad list
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
					literatureSourcesMap.put(ls.getCitation(), ls);
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

			
			if (endpointName.equals("Bioconcentration Factor")){
				unitName = DevQsarConstants.getConstantNameByReflection(DevQsarConstants.L_KG);
			} else if (endpointName.contains("Log") || endpointName.contains("pKa")) {
				unitName = DevQsarConstants.getConstantNameByReflection(DevQsarConstants.LOG_UNITS);
			} else if (endpointName.contains("Class") || endpointName.contains("Concern") || endpointName.contains("Binary")
					|| endpointName.contains("Mutagenicity") || endpointName.contains("Developmental Toxicity")) {
				unitName = DevQsarConstants.getConstantNameByReflection(DevQsarConstants.BINARY);
			} else if (endpointName.contains("Factor") || endpointName.contains("Fraction") || endpointName.contains("Index") || endpointName.contains("Constant")) {
				unitName =DevQsarConstants.getConstantNameByReflection(DevQsarConstants.DIMENSIONLESS);
			} else if (endpointName.contains("count")) {
				unitName = DevQsarConstants.getConstantNameByReflection(DevQsarConstants.COUNT);
			} else if (endpointName.contains("Volume") || endpointName.contains("Production")) {
				unitName =DevQsarConstants.getConstantNameByReflection(DevQsarConstants.POUNDS);
			} else {
				System.out.println("Please handle units for: " + endpointName);
			}

		} else if (unitName.equals("uL/min/million hepatocytes")) {
			unitName =DevQsarConstants.getConstantNameByReflection(DevQsarConstants.UL_MIN_1MM_CELLS);
		} else if (unitName.equals("atm-m3/mole")) {
			unitName =DevQsarConstants.getConstantNameByReflection(DevQsarConstants.ATM_M3_MOL);
		} else if (unitName.equals("cm3/molecule*sec")) {
			unitName =DevQsarConstants.getConstantNameByReflection(DevQsarConstants.CM3_MOLECULE_SEC);
		} else if (unitName.equals("L/kg")) {
			unitName =DevQsarConstants.getConstantNameByReflection(DevQsarConstants.L_KG);
		} else if (unitName.equals("°C")) {
			unitName =DevQsarConstants.getConstantNameByReflection(DevQsarConstants.DEG_C);
		} else if (unitName.equals("mol/L")) {
			unitName =DevQsarConstants.getConstantNameByReflection(DevQsarConstants.MOLAR);
		} else if (unitName.equals("days")) {
			unitName =DevQsarConstants.getConstantNameByReflection(DevQsarConstants.DAYS);
		} else if (unitName.equals("g/cm^3")) {
			unitName =DevQsarConstants.getConstantNameByReflection(DevQsarConstants.G_CM3);
		} else if (unitName.equals("dyn/cm")) {
			unitName =DevQsarConstants.getConstantNameByReflection(DevQsarConstants.DYN_CM);
		} else if (unitName.equals("mmHg")) {
			unitName =DevQsarConstants.getConstantNameByReflection(DevQsarConstants.MMHG);
		}

		
		return unitName;
	}
	
	public static String correctPropertyName(String endpointName) {
		// Done:
		// Density
		switch (endpointName) {

		case "Acute Oral LD50":
			endpointName=DevQsarConstants.ORAL_RAT_LD50;
			break;
		case "Fraction Unbound in Human Plasma":
			endpointName = DevQsarConstants.FUB;
			break;
		case "In Vitro Intrinsic Hepatic Clearance":
			endpointName = DevQsarConstants.CLINT;
			break;
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
		case "LogKoa: Octanol-Air":
			endpointName = DevQsarConstants.LOG_KOA;
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
			endpointName = DevQsarConstants.BCF;
			break;
		case "Atmos. Hydroxylation Rate":
			endpointName = DevQsarConstants.OH;
			break;
		case "Soil Adsorp. Coeff. (Koc)":
			endpointName = DevQsarConstants.KOC;
			break;
		case "Biodegration Class":
			endpointName = DevQsarConstants.RBIODEG;
			break;
		case "Biodeg. Half-Life":
			endpointName = DevQsarConstants.BIODEG_HL_HC;
			break;
		case "Fish Biotrans. Half-Life (Km)":
			endpointName = DevQsarConstants.KmHL;
			break;
		}

		return endpointName;
	}
	
	void runEndpoints(String [] endpoints,String user,boolean post) {
		for (String endpoint:endpoints) {
			List<MeasuredProperty> failedMeasuredProperties = loadAllTables(endpoint, user,post);
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
	
	void runEndpoints(String sourceName, String [] endpoints,String user,boolean post) {

		File folder=new File(JSON_FOLDER_PATH + "/failed");
		folder.mkdirs();
		
		for (String endpoint:endpoints) {
			
			List<MeasuredProperty> failedMeasuredProperties = loadAllTables(sourceName, endpoint, user,post);
			
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
	
	void testRunEndpoints(String [] endpoints,String user) {
		for (String endpoint:endpoints) {
			List<MeasuredProperty> failedMeasuredProperties = testLoadAllTables(endpoint, user);
			
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

	void seeOutstandingChemPropData() {
		
		HashMap<Long, Endpoint> endpoints = Endpoint.getTableFromJsonFiles2(JSON_FOLDER_PATH);
		TreeMap <String,Property>mapProperties=OPERA_lookups.getPropertyMap();
		
		for (Long id:endpoints.keySet()) {
			Endpoint endpoint=endpoints.get(id);
			String propertyNameCorrected=correctPropertyName(endpoint.name);
			System.out.println(endpoint.name+"\t"+propertyNameCorrected+"\t"+mapProperties.containsKey(propertyNameCorrected));
		}
		
		
	}
	void lookAtSourceDetails() {
		HashMap<Long, SourceDetail> sourceDetails = SourceDetail.getTableFromJsonFiles2(JSON_FOLDER_PATH);
	
		for (Long id:sourceDetails.keySet()) {
			SourceDetail sd=sourceDetails.get(id);
			
			LiteratureSource ls=sd.asLiteratureSource("tmarti02");
			
			System.out.println(ls.getCitation());
			
//			System.out.println(Utilities.gson.toJson(sd));
//			System.out.println(Utilities.gson.toJson(ls)+"\n\n");
					
		}
	}
	
	
	
	
	
	
	public static void main(String[] args) {
		
//		String[] endpoints = { "LogKow: Octanol-Water" };
//		String[] endpoints = { "Surface Tension" };
//		String[] endpoints = { "Water Solubility" };
//		String[] endpoints = { "Boiling Point" };
//		String[] endpoints = { "Melting Point" };
//		String[] endpoints = { "Vapor Pressure" };
//		String[] endpoints = { "Flash Point" };
//		String[] endpoints = { "Density" };
		
//		String[] endpoints = { "Surface Tension","Log(air-water partition coefficient)" };

		String[] endpoints = { "Surface Tension"};
		
//		String[] endpoints = { "Fish Biotrans. Half-Life (Km)"};//days
//		String[] endpoints = { "Biodeg. Half-Life"};//days
//		String[] endpoints = { "Soil Adsorp. Coeff. (Koc)"};//
//		String[] endpoints = { "Atmos. Hydroxylation Rate"};//
//		String[] endpoints = { "Biodegration Class"};//
//		String[] endpoints = { "Henry's Law"};//
//		String[] endpoints = { "LogKow: Octanol-Water"};
//		String[] endpoints = { "LogKoa: Octanol-Air"};
//		String[] endpoints = { "Bioconcentration Factor"};
//		String[] endpoints = { "Fraction Unbound in Human Plasma"};
//		String[] endpoints = { "In Vitro Intrinsic Hepatic Clearance"};
//		String[] endpoints = { "pKa Basic Apparent"};
//		String[] endpoints = { "pKa Acidic Apparent"};
		

		ChemPropLoader c=new ChemPropLoader(true,true);
		c.runEndpoints(endpoints,"tmarti02",true);
		
//		c.runEndpoints("PhysPropNCCT",endpoints,"tmarti02",true);
//		c.runEndpoints("ECOTOX: aquatic",endpoints,"tmarti02",false);
//		c.runEndpoints("ECOTOX: terrestrial",endpoints,"tmarti02",true);
		
		
		
		
//		c.runEndpoints("Rayne et al, J. Env. Sci. and Health Part A, (2009) 44(12):1145-1199",endpoints,"tmarti02",true);
				
//		c.testRunEndpoints(endpoints,"tmarti02");
//		c.seeOutstandingChemPropData();
//		c.lookAtSourceDetails();
		
		
	}

	
}	
	
