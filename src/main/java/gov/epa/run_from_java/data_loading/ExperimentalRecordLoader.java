package gov.epa.run_from_java.data_loading;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
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
import gov.epa.databases.dsstox.entity.SourceSubstance;
import gov.epa.databases.dsstox.service.SourceSubstanceServiceImpl;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

public class ExperimentalRecordLoader {
	static boolean loadSourceChemicalMap=true;
	boolean debug=false;
	
	static final Pattern STRING_COLUMN_PATTERN = Pattern.compile("([~><=]{1,2})?(-?[0-9\\.]+)([-~])?(-?[0-9\\.]+)?");
	
	private ParameterService parameterService = new ParameterServiceImpl();
	private PropertyCategoryService propertyCategoryService = new PropertyCategoryServiceImpl();
	private PropertyInCategoryService propertyInCategoryService = new PropertyInCategoryServiceImpl();
	private ExpPropPropertyService expPropPropertyService = new ExpPropPropertyServiceImpl();
	private ExpPropUnitService expPropUnitService = new ExpPropUnitServiceImpl();
	LiteratureSourceService literatureSourceService = new LiteratureSourceServiceImpl();
	PublicSourceService publicSourceService = new PublicSourceServiceImpl();
	SourceChemicalService sourceChemicalService=new SourceChemicalServiceImpl(); 
	
	
	ParameterAcceptableUnitService parameterAcceptableUnitService=new ParameterAcceptableUnitServiceImpl();
	PropertyAcceptableUnitService propertyAcceptableUnitService=new PropertyAcceptableUnitServiceImpl();  
	PropertyAcceptableParameterService propertyAcceptableParameterService=new PropertyAcceptableParameterServiceImpl();
	
	
	Map<String, LiteratureSource> literatureSourcesMap = new HashMap<String, LiteratureSource>();
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
	
	private static Gson gson =  new GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.serializeSpecialFloatingPointValues()
			.create();
	
	public ExperimentalRecordLoader(String lanId) {
		this.lanId = lanId;
		mapTables();
	}
	
	private void mapTables() {
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
	
	
	public void load(List<ExperimentalRecord> records, String type, boolean createDBEntries) {
		List<ExperimentalRecord> failedRecords = new ArrayList<>();
		List<ExperimentalRecord> loadedRecords = new ArrayList<>();
		List<PropertyValue> loadedPropertyValues = new ArrayList<>();
		
		int countSuccess = 0;
		int countFailure = 0;
		int countTotal = 0;
		
		int counter=0;
		
		PropertyValueCreator propValCreator=new PropertyValueCreator(this);
		ParameterValueCreator paramValCreator=new ParameterValueCreator(this);
		
		for (ExperimentalRecord rec:records) {
			counter++;
			
//			System.out.println(counter);
			
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
					//do nothing
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
					+ recSample.source_name + "/" + recSample.property_name + " Experimental Records-Failed.json";
			
			writeRecordsToFile(failedRecords, failedRecordsFilePath);
		}
		
		if (!loadedRecords.isEmpty()) {
			ExperimentalRecord recSample = records.iterator().next();
			
			String loadedRecordsFilePath = "data/dev_qsar/exp_prop/" + type + "/"
					+ recSample.source_name + "/" + recSample.property_name + " ExperimentalRecords-Loaded.json";
			writeRecordsToFile(loadedRecords, loadedRecordsFilePath);
			
			
			JsonArray ja=new JsonArray();
			for (PropertyValue pv:loadedPropertyValues) {
				JsonObject jo = pv.createJsonObjectFromPropertyValue();
				ja.add(jo);
//				System.out.println(gson.toJson(jo));
			}

			String loadedPropertyValuesFilePath = "data/dev_qsar/exp_prop/" + type + "/"
					+ recSample.source_name + "/" + recSample.property_name + " PropertyValues-Loaded.json";
			writeRecordsToFile(ja, loadedPropertyValuesFilePath);
		}

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
	
	void loadAcuteAquaticToxicityData() {

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

		addPropertyAcceptableParameter(getParameter("toxval_id","toxval_id field in toxval table in ToxVal database"),property);
		addPropertyAcceptableParameter(getParameter("Lifestage","lifestage field in toxval table in ToxVal database"),property);
		addPropertyAcceptableParameter(getParameter("Exposure route","exposure route field in toxval table in ToxVal database"),property);
		addPropertyAcceptableParameter(getParameter("Reliability","Reliability"),property);
		
		addPropertyInCategory(getPropertyCategory("Acute aquatic toxicity", "Acute aquatic toxicity"), property);
		
		//There are no units for parameters for acute aquatic tox but here is example:
//		addParameterAcceptableUnit(getUnit("C","C"), getParameter("Temperature","Temperature"));		
		
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

		int propertyId=22;
		int public_source_id=79;
		
		String sqlParameters="delete from exp_prop.parameter_values pv2 using exp_prop.property_values pv\n"+
				"where pv2.fk_property_value_id=pv.id and pv.fk_property_id="+propertyId;

//		-- delete property values for fhm lc50 endpoint
		String sqlPropertyValues="delete from exp_prop.property_values pv where fk_property_id="+propertyId;
		
//	-- Delete source chemicals that came from toxvalv93:
		String sqlSourceCHemicals="delete from exp_prop.source_chemicals sc where sc.fk_public_source_id="+public_source_id;

		//Literature sources
		String sqlLiteratureSources="delete from exp_prop.literature_sources where created_by='tmarti02'";

		SqlUtilities.runSQLUpdate(conn, sqlParameters);
		SqlUtilities.runSQLUpdate(conn, sqlPropertyValues);
		SqlUtilities.runSQLUpdate(conn, sqlSourceCHemicals);
		SqlUtilities.runSQLUpdate(conn, sqlLiteratureSources);
				
	}
	
	
	
	public static void main(String[] args) {
//		loadBCF();
	
		deleteExpPropData();
		
		ExperimentalRecordLoader.loadSourceChemicalMap=true;
		ExperimentalRecordLoader loader = new ExperimentalRecordLoader("tmarti02");
		loader.loadAcuteAquaticToxicityData();
		
		
		
	}

}