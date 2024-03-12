package gov.epa.run_from_java.data_loading;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;
import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.Parameter;
import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterAcceptableUnit;
import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyAcceptableParameter;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyAcceptableUnit;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyCategory;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyInCategory;
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
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyCategoryService;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyCategoryServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyInCategoryService;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyInCategoryServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.PublicSourceService;
import gov.epa.databases.dev_qsar.exp_prop.service.PublicSourceServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.SourceChemicalService;
import gov.epa.databases.dev_qsar.exp_prop.service.SourceChemicalServiceImpl;
import gov.epa.run_from_java.scripts.SqlUtilities;

public class ExperimentalRecordLoader {

	static boolean loadSourceChemicalMap=true;//takes a while but faster for loading lots of records
	boolean debug=false;
	
	static final Pattern STRING_COLUMN_PATTERN = Pattern.compile("([~><=]{1,2})?(-?[0-9\\.]+)([-~])?(-?[0-9\\.]+)?");
	
	ParameterService parameterService = new ParameterServiceImpl();
	PropertyCategoryService propertyCategoryService = new PropertyCategoryServiceImpl();
	PropertyInCategoryService propertyInCategoryService = new PropertyInCategoryServiceImpl();
	ExpPropPropertyService expPropPropertyService = new ExpPropPropertyServiceImpl();
	ExpPropUnitService expPropUnitService = new ExpPropUnitServiceImpl();
	LiteratureSourceService literatureSourceService = new LiteratureSourceServiceImpl();
	PublicSourceService publicSourceService = new PublicSourceServiceImpl();
	SourceChemicalService sourceChemicalService=new SourceChemicalServiceImpl(); 
	
	ParameterAcceptableUnitService parameterAcceptableUnitService=new ParameterAcceptableUnitServiceImpl();
	PropertyAcceptableUnitService propertyAcceptableUnitService=new PropertyAcceptableUnitServiceImpl();  
	PropertyAcceptableParameterService propertyAcceptableParameterService=new PropertyAcceptableParameterServiceImpl();
	
	
	Map<String, LiteratureSource> literatureSourcesMap = new HashMap<String, LiteratureSource>();//key is citation 
	Map<String, Parameter> parametersMap = new HashMap<String, Parameter>();
	Map<String, ExpPropProperty> propertiesMap = new HashMap<String, ExpPropProperty>();
	Map<String, PublicSource> publicSourcesMap = new HashMap<String, PublicSource>();
	Map<String, ExpPropUnit> unitsMap = new HashMap<String, ExpPropUnit>();
	Map<String, PropertyCategory> propertyCategoryMap = new HashMap<String, PropertyCategory>();
	
	Map<String, SourceChemical> sourceChemicalMap = new HashMap<String, SourceChemical>();
	
	List<ParameterAcceptableUnit>parameterAcceptableUnits=null;
	List<PropertyAcceptableUnit>propertyAcceptableUnits=null;
	List<PropertyAcceptableParameter>propertyAcceptableParameters=null;
	List<PropertyInCategory>propertyInCategories=null;
	
	String lanId;
	
	public static final String typePhyschem="Physchem";
	public static final String typeTox="Tox";
	public static final String typeOther="Other";
	
	public static Gson gson =  new GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.serializeSpecialFloatingPointValues()
			.create();
	
	public ExperimentalRecordLoader(String lanId) {
		this.lanId = lanId;
		System.out.print("loading maps...");
		mapTables();
		System.out.println("done");
	}
	
	private void mapTables() {
		
		List<Parameter> parameters = parameterService.findAll();
		for (Parameter p:parameters) {
			parametersMap.put(p.getName(), p);
		}
		
		List<ExpPropProperty> properties = expPropPropertyService.findAll();
		for (ExpPropProperty p:properties) {
			propertiesMap.put(p.getName(), p);
		}

		
		List<LiteratureSource> literatureSources = literatureSourceService.findAll();
		for (LiteratureSource ls:literatureSources) {
			literatureSourcesMap.put(ls.getCitation(), ls);//use citation because more likely to be unique
		}

		List<PublicSource> publicSources = publicSourceService.findAll();
		for (PublicSource ps:publicSources) {
			publicSourcesMap.put(ps.getName(), ps);
		}
		
		List<ExpPropUnit> units = expPropUnitService.findAll();
		for (ExpPropUnit u:units) {
			unitsMap.put(u.getName(), u);
		}
		
		parameterAcceptableUnits=parameterAcceptableUnitService.findAll();
		propertyAcceptableUnits=propertyAcceptableUnitService.findAll();
		propertyAcceptableParameters=propertyAcceptableParameterService.findAll();
		propertyInCategories=propertyInCategoryService.findAll();
		
		List<PropertyCategory>propertyCategories=propertyCategoryService.findAll();
		for (PropertyCategory pc:propertyCategories) {
			propertyCategoryMap.put(pc.getName(), pc);
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
	
	private static ExperimentalRecords getPublicSourceRecords(String publicSourceName, String type) {
		ExperimentalRecords records = new ExperimentalRecords();
		String publicSourceFolderPath = "C:\\Users\\CRAMSLAN\\OneDrive - Environmental Protection Agency (EPA)\\VDI_Repo\\java\\github\\ghs-data-gathering\\data\\experimental" + "/" + publicSourceName;
		File publicSourceFolder = new File(publicSourceFolderPath);
		File[] publicSourceFiles = publicSourceFolder.listFiles();
		
		String trimmedPublicSourceName = publicSourceName;
		if (publicSourceName.contains("/")) {
			trimmedPublicSourceName = publicSourceName.substring(publicSourceName.lastIndexOf("/") + 1);
		}
		for (File file:publicSourceFiles) {
			String fileName = file.getName();
//			System.out.println(fileName);
			if (!fileName.endsWith(".json")) {
				continue;
			} else if (((fileName.startsWith(trimmedPublicSourceName + " Experimental Records")
					&& type.contains("physchem"))
					|| (fileName.startsWith(trimmedPublicSourceName + " Toxicity Experimental Records"))
					&& type.contains("tox"))
					&& !fileName.contains("Failed")) {
				records.addAll(ExperimentalRecords.loadFromJson(file.getAbsolutePath(), gson));
			}
		}
		
		return records;
	}
	
//	public void load(List<ExperimentalRecord> records, String type, boolean createDBEntries) {
//		List<ExperimentalRecord> failedRecords = new ArrayList<ExperimentalRecord>();
//		List<ExperimentalRecord> loadedRecords = new ArrayList<ExperimentalRecord>();
//		int countSuccess = 0;
//		int countFailure = 0;
//		int countTotal = 0;
//		for (ExperimentalRecord rec:records) {
//			try {
//				
//				
//				boolean success = false;
//				
//				ExpPropData expPropData=null;
//				
//				if (type.contains("physchem")) {
//					expPropData = new PhyschemExpPropData(this);
//				} else if (type.contains("tox")) {
//					expPropData = new ToxExpPropData(this);
//				}
//				expPropData.getValues(rec);
//
//				if(createDBEntries) {
//					expPropData.constructPropertyValue(createDBEntries);
//					success = expPropData.postPropertyValue();
//				} else {
//					success=true;
//				}
//				
//				if (success) {
//					countSuccess++;
//					loadedRecords.add(rec);
//				} else {
//					failedRecords.add(rec);
////					logger.warn(rec.id_physchem + ": Loading failed");
//					countFailure++;
//				}
//			} catch (Exception e) {
//				failedRecords.add(rec);
////				logger.warn(rec.id_physchem + ": Loading failed with exception: " + e.getMessage());
//				countFailure++;
//			}
//			
//			countTotal++;
//			if (countTotal % 1000 == 0) {
//				System.out.println("Attempted to load " + countTotal + " property values: " 
//						+ countSuccess + " successful; " 
//						+ countFailure + " failed");
//			}
//		}
//		System.out.println("Finished attempt to load " + countTotal + " property values: " 
//				+ countSuccess + " successful; " 
//				+ countFailure + " failed");
//		
//		if (!failedRecords.isEmpty()) {
//			ExperimentalRecord recSample = failedRecords.iterator().next();
//			String publicSourceName = recSample.source_name;
//			String failedFilePath = "data/dev_qsar/exp_prop/" + type + "/"
//					+ publicSourceName + "/" + publicSourceName + " Experimental Records-Failed.json";
//			
//			writeRecordsToFile(failedRecords, failedFilePath);
//		}
//		
//		if (!loadedRecords.isEmpty()) {
//			ExperimentalRecord recSample = loadedRecords.iterator().next();
//			String loadedRecordsFilePath = "data/dev_qsar/exp_prop/" + type + "/"
//					+ recSample.source_name + "/" + recSample.property_name + " Experimental Records-Loaded.json";
//			
//			writeRecordsToFile(loadedRecords, loadedRecordsFilePath);
//		}
//
//	}
	
	
	public List<PropertyValue> load(List<ExperimentalRecord> records, String type, boolean createDBEntries) {

		List<ExperimentalRecord> failedRecords = new ArrayList<>();
		List<ExperimentalRecord> loadedRecords = new ArrayList<>();
		List<PropertyValue> loadedPropertyValues = new ArrayList<>();
		
		int countSuccess = 0;
		int countFailure = 0;
		int countTotal = 0;
		
		int counter=0;
		
		PropertyValueCreator propValCreator=new PropertyValueCreator(this);
		ParameterValueCreator paramValCreator=new ParameterValueCreator(this);
		
		List<PropertyValue> propertyValues = new ArrayList<>();
		
		
		for (ExperimentalRecord rec:records) {
			counter++;
			
			if(counter%1000==0)
				System.out.println(counter);
			
			try {
				boolean success = false;
				
				PropertyValue pv=propValCreator.createPropertyValue(rec, createDBEntries);
				
				if (pv.getUnit()==null) {
					failedRecords.add(rec);
					rec.keep=false;
					rec.reason="Units not in database";
//					logger.warn(rec.id_physchem + ": Loading failed");
					countFailure++;
					continue;
				}
				
				
				if (type.equals(typePhyschem)) {
					paramValCreator.addPhyschemParameterValues(rec, pv);
				} else if (type.equals(typeTox)) {
					paramValCreator.addToxParameterValues(rec, pv);
				} else {
					//typeOther: dont need to pull parameters from fields in rec
				}
				paramValCreator.addGenericParametersValues(rec,pv);


				if (createDBEntries) {
					success = propValCreator.postPropertyValue(pv);//TODO add batch insert instead	
				} else {
					success=true;
				}
				
				
//				JsonObject jo = createJsonObjectFromPropertyValue(pv);
//				System.out.println(gson.toJson(jo));
//				System.out.println(counter+"\t"+success+"\t"+expPropData.propertyValue.getLiteratureSource().getName()+"\t"+expPropData.propertyValue.getSourceChemical().getSourceChemicalName());
				
				if (success) {
					countSuccess++;
					loadedRecords.add(rec);
					loadedPropertyValues.add(pv);
				} else {
					failedRecords.add(rec);
//					logger.warn(rec.id_physchem + ": Loading failed");
					countFailure++;
				}
			} catch (Exception e) {
				failedRecords.add(rec);
//				logger.warn(rec.id_physchem + ": Loading failed with exception: " + e.getMessage());
				countFailure++;
			}
			
			countTotal++;
			if (countTotal % 1000 == 0) {
				System.out.println("Attempted to load " + countTotal + " property values: " 
						+ countSuccess + " successful; " 
						+ countFailure + " failed");
				
			}
			
//			if(true) break;

		}
		System.out.println("Finished attempt to load " + countTotal + " property values: " 
				+ countSuccess + " successful; " 
				+ countFailure + " failed");
		
		
		
		
		if (!failedRecords.isEmpty()) {
			ExperimentalRecord recSample = failedRecords.iterator().next();
			String failedRecordsFilePath = "data/dev_qsar/exp_prop/" + type + "/"
					+ recSample.source_name + "/" + recSample.property_name.replace(":", "") + " Experimental Records-Failed.json";
			
			
			
			writeRecordsToFile(failedRecords, failedRecordsFilePath);
		}
		
		if (!loadedRecords.isEmpty()) {
			ExperimentalRecord recSample = records.iterator().next();
			
			String loadedRecordsFilePath = "data/dev_qsar/exp_prop/" + type + "/"
					+ recSample.source_name + "/" + recSample.property_name.replace(":", "") + " ExperimentalRecords-Loaded.json";
			writeRecordsToFile(loadedRecords, loadedRecordsFilePath);
			
			
			JsonArray ja=new JsonArray();
			for (PropertyValue pv:loadedPropertyValues) {
				JsonObject jo = pv.createJsonObjectFromPropertyValue();
				ja.add(jo);
//				System.out.println(gson.toJson(jo));
			}

			String loadedPropertyValuesFilePath = "data/dev_qsar/exp_prop/" + type + "/"
					+ recSample.source_name + "/" + recSample.property_name.replace(":", "") + " PropertyValues-Loaded.json";
			writeRecordsToFile(ja, loadedPropertyValuesFilePath);
		}
		return loadedPropertyValues;

	}
	
	
	private void writeRecordsToFile(Object records, String filePath) {
		File failedFile = new File(filePath);
		if (failedFile.getParentFile()!=null) { failedFile.getParentFile().mkdirs(); }
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
			bw.write(gson.toJson(records));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	static void loadBCF() {
		System.out.println("eclipse recognizes new code4");
		String sourceName = "Burkhard_BCF";
		String type = "physchem";
		
		System.out.println("Retrieving records...");
		ExperimentalRecords records = getPublicSourceRecords(sourceName, type);
		System.out.println("Retrieved " + records.size() + " records");
		
		System.out.println("Loading " + records.size() + " records...");
		long t0 = System.currentTimeMillis();
		ExperimentalRecordLoader loader = new ExperimentalRecordLoader("cramslan");
		loader.load(records, type, true);
		long t = System.currentTimeMillis();
		System.out.println("Loading completed in " + (t - t0)/1000.0 + " s");
	}
	
	ExpPropProperty getProperty(String name, String description) {
		ExpPropProperty property=propertiesMap.get(name);
		
		if (property!=null) {
			if (debug) System.out.println("already have property in db: "+property.getName()+"\t"+property.getDescription());
			return property;
		}
		
		property=new ExpPropProperty();
		property.setName(name);
		property.setCreatedBy(lanId);
		property.setDescription(description);
		property= expPropPropertyService.create(property);
		
		propertiesMap.put(name,property);
		
		return property;
	}
	
	
	ExpPropUnit getUnit(String name, String abbreviation) {
		ExpPropUnit unit=unitsMap.get(name);
		
		if (unit!=null) {
			if (debug) System.out.println("already have unit in db: "+unit.getName()+"\t"+unit.getAbbreviation());
			return unit;
		}
		
		unit=new ExpPropUnit();
		unit.setName(name);
		unit.setCreatedBy(lanId);
		unit.setAbbreviation(abbreviation);
		unit= expPropUnitService.create(unit);
		
		unitsMap.put(name,unit);
		
		return unit;
	}
	
	
	Parameter getParameter(String name, String description) {
		Parameter parameter=parametersMap.get(name);
		
		if (parameter!=null) {
			if (debug) System.out.println("already have parameter in db: "+parameter.getName()+"\t"+parameter.getDescription());
			return parameter;
		}
		
		parameter=new Parameter();
		parameter.setName(name);
		parameter.setCreatedBy(lanId);
		parameter.setDescription(description);
		parameter= parameterService.create(parameter);
		return parameter;
	}

	void addPropertyAcceptableUnit(ExpPropUnit unit, ExpPropProperty property) {

		for(PropertyAcceptableUnit propertyAcceptableUnit:propertyAcceptableUnits) {
			if(propertyAcceptableUnit.getUnit().getId()==unit.getId()) {
				if(propertyAcceptableUnit.getProperty().getId()==property.getId()) {
//					System.out.println("Have propertyAcceptableUnit:"+unit.getName()+"\t"+property.getName());
					return;
				}
			}
		}
		
		PropertyAcceptableUnit propertyAcceptableUnit=new PropertyAcceptableUnit ();
		propertyAcceptableUnit.setCreatedBy(lanId);
		propertyAcceptableUnit.setUnit(unit);
		propertyAcceptableUnit.setProperty(property);					
		propertyAcceptableUnitService.create(propertyAcceptableUnit);
	}
	
	
	void addParameterAcceptableUnit(ExpPropUnit unit, Parameter parameter) {

		for(ParameterAcceptableUnit parameterAcceptableUnit:parameterAcceptableUnits) {
			if(parameterAcceptableUnit.getUnit().getId()==unit.getId()) {
				if(parameterAcceptableUnit.getParameter().getId()==parameter.getId()) {
//					System.out.println("Have parameterAcceptableUnit:"+unit.getName()+"\t"+parameter.getName());
					return;
				}
			}
		}
		
		ParameterAcceptableUnit parameterAcceptableUnit=new ParameterAcceptableUnit();
		parameterAcceptableUnit.setCreatedBy(lanId);
		parameterAcceptableUnit.setUnit(unit);
		parameterAcceptableUnit.setParameter(parameter);					
		parameterAcceptableUnitService.create(parameterAcceptableUnit);
	}
	
	
	void addPropertyInCategory(PropertyCategory propertyCategory, ExpPropProperty property) {
		
		for(PropertyInCategory propertyInCategory:propertyInCategories) {
			if(propertyInCategory.getPropertyCategory().getId()==propertyCategory.getId()) {
				if(propertyInCategory.getProperty().getId()==property.getId()) {
//					System.out.println("Already have propertyInCategory:"+propertyCategory.getName()+"\t"+property.getName());
					return;
				}
			}
		}
		
		PropertyInCategory propertyInCategory=new PropertyInCategory();
		propertyInCategory.setCreatedBy(lanId);
		propertyInCategory.setPropertyCategory(propertyCategory);
		propertyInCategory.setProperty(property);					
		propertyInCategoryService.create(propertyInCategory);
		
		
		
	}
	
	
	void addPropertyAcceptableParameter(Parameter parameter, ExpPropProperty property) {

		for(PropertyAcceptableParameter propertyAcceptableParameter:propertyAcceptableParameters) {
			if(propertyAcceptableParameter.getParameter().getId()==parameter.getId()) {
				if(propertyAcceptableParameter.getProperty().getId()==property.getId()) {
//					System.out.println("Have propertyAcceptableParameter:"+parameter.getName()+"\t"+property.getName());
					return;
				}
			}
		}
		
		PropertyAcceptableParameter propertyAcceptableParameter=new PropertyAcceptableParameter();
		propertyAcceptableParameter.setCreatedBy(lanId);
		propertyAcceptableParameter.setParameter(parameter);
		propertyAcceptableParameter.setProperty(property);					
		propertyAcceptableParameterService.create(propertyAcceptableParameter);
	}

	PropertyCategory getPropertyCategory(String name,String description) {
		
		PropertyCategory propertyCategory=propertyCategoryMap.get(name);
		
		if (propertyCategory!=null) {
			if (debug) System.out.println("already have PropertyCategory in db: "+propertyCategory.getName()+"\t"+propertyCategory.getDescription());
			return propertyCategory;
		}
		
		propertyCategory=new PropertyCategory();
		propertyCategory.setName(name);
		propertyCategory.setCreatedBy(lanId);
		propertyCategory.setDescription(description);
		propertyCategory= propertyCategoryService.create(propertyCategory);
		return propertyCategory;
	}
	
	void loadAcuteAquaticToxicityDataToxVal() {

		debug=true;//prints values loaded from database like property
		
		boolean createDBEntries=true;
		
		
		String sourceName="ToxVal_V93";
		String propertyName=DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50;
		
		String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\ghs-data-gathering\\";
		String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+" "+propertyName+" Experimental Records.json";

		//**********************************************************************************************
		//First create the property
		ExpPropProperty property=getProperty(propertyName, "concentration that kills half of fathead minnow in 96 hours");
		
		//**********************************************************************************************
		//Add entries for properties_acceptable_units:
		addPropertyAcceptableUnit(getUnit("G_L","g/L"), property);
		addPropertyAcceptableUnit(getUnit("MOLAR","M"), property);

//		addPropertyAcceptableParameter(getParameter("toxval_id","toxval_id field in toxval table in ToxVal database"),property);
//		addPropertyAcceptableParameter(getParameter("Lifestage","lifestage field in toxval table in ToxVal database"),property);
//		addPropertyAcceptableParameter(getParameter("Exposure route","exposure route field in toxval table in ToxVal database"),property);
//		addPropertyAcceptableParameter(getParameter("Reliability","Reliability"),property);
		
//		addPropertyInCategory(getPropertyCategory("Acute aquatic toxicity", "Acute aquatic toxicity"), property);
		
		//There are no units for parameters for acute aquatic tox but here is example:
//		addParameterAcceptableUnit(getUnit("C","C"), getParameter("Temperature","Temperature"));		
		
		//*******************************************************************************************************

		ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, gson);
//		printUniqueUnitsListInExperimentalRecords(records);
		System.out.println("experimentalRecords.size()="+records.size());
		
		load(records, typeOther, createDBEntries);
		
	}
	
	void loadAcuteAquaticToxicityDataEcotox() {

		debug=true;//prints values loaded from database like property
		
		boolean createDBEntries=true;
			
		String sourceName="ECOTOX_2023_12_14";
		
		String propertyName=DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50;
		
		String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
		String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+"_"+propertyName+" Experimental Records.json";
		
		File jsonFile=new File(filePath);
		
		System.out.println(filePath+"\t"+jsonFile.exists());
		

		//**********************************************************************************************
		//First create the property
		ExpPropProperty property=getProperty(propertyName, "concentration that kills half of fathead minnow in 96 hours");
		
		//**********************************************************************************************
		//Add entries for properties_acceptable_units:
		addPropertyAcceptableUnit(getUnit("G_L","g/L"), property);
		addPropertyAcceptableUnit(getUnit("MOLAR","M"), property);

//		addPropertyAcceptableParameter(getParameter("test_id","test_id field in tests table in Ecotox database"),property);		
//		//See exposure_type_codes table in ECOTOX:
//		addPropertyAcceptableParameter(getParameter("exposure_type","Type of exposure (S=static, F=flowthrough, R=renewal, etc)"),property);
//		//See chemical_analysis_codes table in ECOTOX:
//		addPropertyAcceptableParameter(getParameter("chem_analysis_method","Analysis method for water concentration (M=measured, U=unmeasured etc)"),property);		
//		
//		addParameterAcceptableUnit(getUnit("TEXT",""), getParameter("test_id","test_id field in tests table in Ecotox database"));
//		addParameterAcceptableUnit(getUnit("TEXT",""), getParameter("exposure_type","Type of exposure (S=static, F=flow-through, R=renewal, etc)"));		
//		addParameterAcceptableUnit(getUnit("TEXT",""), getParameter("chem_analysis_method","Analysis method for water concentration (M=measured, U=unmeasured etc)"));

//		addPropertyInCategory(getPropertyCategory("Acute aquatic toxicity", "Acute aquatic toxicity"), property);
		
		//*******************************************************************************************************

		ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, gson);
//		printUniqueUnitsListInExperimentalRecords(records);
		System.out.println("experimentalRecords.size()="+records.size());
		
		load(records, typeOther, createDBEntries);
		
	}

	private void printUniqueUnitsListInExperimentalRecords(ExperimentalRecords records) {
		//Following gets list of unique units in experimental records:
		List<String>unitsList=new ArrayList();
		for (ExperimentalRecord er:records) {
//			System.out.println(er.property_value_units_final);
			if (!unitsList.contains(er.property_value_units_final)) unitsList.add(er.property_value_units_final);
		}
		for (String units:unitsList) {
			System.out.println(units);
		}
	}
	
	static void deleteExpPropData() {

		Connection conn=SqlUtilities.getConnectionPostgres();

//		int propertyId=19;//b
//		int public_source_id=73;//ToxValBCF
//		int public_source_id=74;//Burkhard_BCF

//		int propertyId=22;//FHM lc50
//		int public_source_id=79;//ToxValv93
				
		
//		int publicSourceId=12;//OPERA
//		String propertyName=DevQsarConstants.KmHL;
//		String propertyName=DevQsarConstants.BIODEG_HL_HC;
//		String propertyName=DevQsarConstants.OH;
//		String propertyName=DevQsarConstants.BCF;
//		String propertyName=DevQsarConstants.KOC;

		int publicSourceId=253;//OPERA2.9
//		String propertyName=DevQsarConstants.ESTROGEN_RECEPTOR_AGONIST;
//		String propertyName=DevQsarConstants.ESTROGEN_RECEPTOR_ANTAGONIST;
//		String propertyName=DevQsarConstants.ESTROGEN_RECEPTOR_BINDING;
//		String propertyName=DevQsarConstants.ANDROGEN_RECEPTOR_AGONIST;
//		String propertyName=DevQsarConstants.ANDROGEN_RECEPTOR_ANTAGONIST;
//		String propertyName=DevQsarConstants.ANDROGEN_RECEPTOR_BINDING;
		
		String propertyName=DevQsarConstants.HENRYS_LAW_CONSTANT;
		
		String sqlPropertyName="select id from exp_prop.properties p where p.name='"+propertyName+"';";
		Long propertyId=Long.parseLong(SqlUtilities.runSQL(conn, sqlPropertyName));
		
		String sqlParameters="delete from exp_prop.parameter_values pv2 using exp_prop.property_values pv\n"+
				"where pv2.fk_property_value_id=pv.id and pv.fk_property_id="+propertyId+" and pv2.created_by='tmarti02'";

//		-- delete property values for fhm lc50 endpoint
		String sqlPropertyValues="delete from exp_prop.property_values pv where fk_public_source_id="+publicSourceId+" and fk_property_id="+propertyId+" and created_by='tmarti02'";
//		String sqlPropertyValues="delete from exp_prop.property_values pv where fk_public_source_id="+publicSourceId+" and created_by='tmarti02'";
		
//	-- Delete source chemicals that came from toxvalv93:
		String sqlSourceCHemicals="delete from exp_prop.source_chemicals sc where sc.fk_public_source_id="+publicSourceId+" and created_by='tmarti02'";

		//Literature sources
		String sqlLiteratureSources="delete from exp_prop.literature_sources where created_by='tmarti02' and id>1677";

		//Public sources
		String sqlPublicSources="delete from exp_prop.public_sources where created_by='tmarti02' and id>104";

		
//		SqlUtilities.runSQLUpdate(conn, sqlParameters);
		SqlUtilities.runSQLUpdate(conn, sqlPropertyValues);
//		SqlUtilities.runSQLUpdate(conn, sqlSourceCHemicals);
		
//		SqlUtilities.runSQLUpdate(conn, sqlLiteratureSources);
//		SqlUtilities.runSQLUpdate(conn, sqlPublicSources);
				
	}
	
	static void deleteEcotoxData() {

		Connection conn=SqlUtilities.getConnectionPostgres();

		int publicSourceId=254;//ECOTOX		
		String propertyName=DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50;
		
		String sqlPropertyName="select id from exp_prop.properties p where p.name='"+propertyName+"';";
		Long propertyId=Long.parseLong(SqlUtilities.runSQL(conn, sqlPropertyName));
		
		String sqlParameters="delete from exp_prop.parameter_values pv2 using exp_prop.property_values pv\n"+
				"where pv2.fk_property_value_id=pv.id and pv.fk_property_id="+propertyId+" and pv.fk_public_source_id="+publicSourceId;

		String sqlPropertyValues="delete from exp_prop.property_values pv where fk_public_source_id="+publicSourceId+" and fk_property_id="+propertyId+";";
		String sqlSourceCHemicals="delete from exp_prop.source_chemicals sc where sc.fk_public_source_id="+publicSourceId+" and created_by='tmarti02'";
		String sqlLiteratureSources="delete from exp_prop.literature_sources where created_by='tmarti02' and id>3378";
//		String sqlPublicSources="delete from exp_prop.public_sources where created_by='tmarti02' and id>104";

		SqlUtilities.runSQLUpdate(conn, sqlParameters);
		SqlUtilities.runSQLUpdate(conn, sqlPropertyValues);
//		SqlUtilities.runSQLUpdate(conn, sqlSourceCHemicals);
//		SqlUtilities.runSQLUpdate(conn, sqlLiteratureSources);
//		SqlUtilities.runSQLUpdate(conn, sqlPublicSources);
				
	}


	void loadPropertyValuesFromThreeM_ExperimentalRecordsFile() {
		
		boolean store=true;
		
//		String propertyName=DevQsarConstants.KOC;
		String propertyName=DevQsarConstants.BCF;
		
//		List<String> propertyNames=Arrays.asList(DevQsarConstants.KOC,DevQsarConstants.BCF);

		//TODO should reload following properties in the future because Three3M parsing was cleaned up by TMM
//		Vapor pressure - already there
//		Melting point- already there
//		Water solubility - already there

		//Rest of these dont make it into a dataset:
//		Boiling point - already there
//		LogKow: Octanol-Water - already there
//		pKA - dont need
//		
		System.out.println("\nLoading property values for "+propertyName);
		
		String sourceName="ThreeM";
		String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
		String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+" Experimental Records.json";

		ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, ExperimentalRecordLoader.gson);
		
		List<String>propertiesInJson=new ArrayList<>();
		
		for (int i=0;i<records.size();i++) {
			ExperimentalRecord er=records.get(i);
			
			if (!propertiesInJson.contains(er.property_name) && er.keep) propertiesInJson.add(er.property_name);			
			
//			if (!propertyNames.contains(er.property_name)) records.remove(i--);
			if (!er.property_name.equals(propertyName)) records.remove(i--);
		}

		
//		for (String prop:propertiesInJson) {
//			System.out.println(prop);
//		}

		System.out.println(gson.toJson(records));
		
//		printUniqueUnitsListInExperimentalRecords(records);
		
		System.out.println("experimentalRecords.size()="+records.size());

		List<PropertyValue>propertyValues=load(records,typePhyschem,store);
		
//		for (PropertyValue pv:propertyValues) {
//			pv.getProperty().setPropertiesInCategories(null);//to enable json print
//			pv.getProperty().setPropertiesAcceptableParameters(null);//to enable json print
//			pv.getProperty().setPropertiesAcceptableUnits(null);
//		}
//		System.out.println(gson.toJson(propertyValues));
	}

	
	
	public static void main(String[] args) {
//		loadBCF();
//		loader.loadPropertyValuesFromThreeM_ExperimentalRecordsFile();		
	
//		deleteExpPropData();
//		loader.loadAcuteAquaticToxicityDataToxVal();
		
//		deleteEcotoxData();
		ExperimentalRecordLoader loader = new ExperimentalRecordLoader("tmarti02");
		loader.loadAcuteAquaticToxicityDataEcotox();
		
	}

}