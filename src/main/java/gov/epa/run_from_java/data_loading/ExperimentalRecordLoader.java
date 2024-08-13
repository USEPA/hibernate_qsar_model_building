package gov.epa.run_from_java.data_loading;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;
import java.util.regex.Pattern;

import javax.validation.ConstraintViolationException;

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
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.util.JSONUtilities;

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
	
	public ExperimentalRecordLoader(String lanId,boolean loadmapTables) {
		this.lanId = lanId;
		
		if(loadmapTables) {
			System.out.print("loading maps...");
			mapTables();
			System.out.println("done");
		}
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
	
	
	public List<PropertyValue> load(List<ExperimentalRecord> records, String type, boolean createDBEntries,String sourceName, String propertyName) {

		ExperimentalRecords failedRecords = new ExperimentalRecords();
		ExperimentalRecords loadedRecords = new ExperimentalRecords();
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
				
				if(pv.getParameterValues()!=null) {
					for (ParameterValue paramValue : pv.getParameterValues()) {
						if(paramValue.getUnit()==null) {
							//TMM bail right away if we have a parameter missing a unit:
							System.out.println("Missing unit for "+paramValue.getParameter().getName());
							return null;
						}
					}
				}
				
				if (createDBEntries) {
					success = propValCreator.postPropertyValue(pv);//TODO add batch insert instead	
				} else {
					success=true;
				}
				
				
				if (success) {
					countSuccess++;
					loadedRecords.add(rec);
					loadedPropertyValues.add(pv);
				} else {
					failedRecords.add(rec);
					System.out.println("fail1");
//					logger.warn(rec.id_physchem + ": Loading failed");
					countFailure++;
				}
			} catch (Exception e) {
				System.out.println("fail2:\t"+e.getMessage());
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
			String failedRecordsFilePath=null;
			if(propertyName==null) {
				failedRecordsFilePath = "data/dev_qsar/exp_prop/" + type + "/"
						+ sourceName + "/Experimental Records-Failed.json";
			} else {
				failedRecordsFilePath = "data/dev_qsar/exp_prop/" + type + "/"
						+ sourceName + "/" + propertyName.replace(":", "") + " Experimental Records-Failed.json";
			}
			
			
			JSONUtilities.batchAndWriteJSON(failedRecords, failedRecordsFilePath);
		}
		
		if (!loadedRecords.isEmpty()) {
			String loadedRecordsFilePath=null;
			if(propertyName==null) {
				loadedRecordsFilePath = "data/dev_qsar/exp_prop/" + type + "/"
						+ sourceName + "/ExperimentalRecords-Loaded.json";
			} else {
				loadedRecordsFilePath = "data/dev_qsar/exp_prop/" + type + "/"
						+ sourceName + "/" + propertyName.replace(":", "") + " ExperimentalRecords-Loaded.json";
				
			}
			JSONUtilities.batchAndWriteJSON(loadedRecords, loadedRecordsFilePath);
			
//			JsonArray ja=new JsonArray();
			List <JsonObject>ja=new ArrayList<>();//use list so can use same batch write method for json
			
			for (PropertyValue pv:loadedPropertyValues) {
				JsonObject jo = pv.createJsonObjectFromPropertyValue();
				ja.add(jo);
//				System.out.println(gson.toJson(jo));
			}

			System.out.println("createDBEntries="+createDBEntries);
			
			File of=new File("data/dev_qsar/exp_prop/" + type + "/"+ sourceName);
			
			System.out.println("See results at\n"+of.getAbsolutePath());
			
			String loadedPropertyValuesFilePath=null;
			if(propertyName==null) {
				loadedPropertyValuesFilePath = "data/dev_qsar/exp_prop/" + type + "/"
						+ sourceName + "/PropertyValues-Loaded.json";
			} else {
				loadedPropertyValuesFilePath = "data/dev_qsar/exp_prop/" + type + "/"
						+ sourceName + "/" + propertyName.replace(":", "") + " PropertyValues-Loaded.json";
			}
			
			JSONUtilities.batchAndWriteJSON(ja, loadedPropertyValuesFilePath);
		}
		return loadedPropertyValues;

	}
	
	public void createLiteratureSources(List<ExperimentalRecord> records) {

		for (ExperimentalRecord er:records) {
			try {
				
				if (er.literatureSource==null || literatureSourcesMap.containsKey(er.literatureSource.getCitation())) continue;
					
				LiteratureSource ls=er.literatureSource;
				ls.setCreatedBy(lanId);

				try {
					
					System.out.println(Utilities.gson.toJson(er.literatureSource));
					
					ls = literatureSourceService.create(ls);
				} catch (Exception ex) {
					ex.printStackTrace();
				}				

				literatureSourcesMap.put(er.literatureSource.getCitation(), ls);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
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
		
		parametersMap.put(name, parameter);
		
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
		
		load(records, typeOther, createDBEntries,sourceName,propertyName);
		
	}
	
	void loadAcuteAquaticToxicityDataEcotox() {

		debug=true;//prints values loaded from database like property
		
		boolean createDBEntries=true;
			
		String sourceName="ECOTOX_2023_12_14";
		
//		String propertyName=DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50;
		String propertyName=DevQsarConstants.NINETY_SIX_HOUR_BLUEGILL_LC50;

		String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
		String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+"_"+propertyName+" Experimental Records.json";
		
		File jsonFile=new File(filePath);
		
		System.out.println(filePath+"\t"+jsonFile.exists());
		

		//**********************************************************************************************
		//First create the property
		ExpPropProperty property=getProperty(propertyName, "concentration that kills half of fathead minnow in 96 hours");
		
		//**********************************************************************************************
		//Add entries for properties_acceptable_units:
		
		//Note: first time you run this property, uncomment out the following lines:
		addPropertyAcceptableUnit(getUnit("G_L","g/L"), property);
		addPropertyAcceptableUnit(getUnit("MOLAR","M"), property);

		addPropertyAcceptableParameter(getParameter("test_id","test_id field in tests table in Ecotox database"),property);		
		addPropertyAcceptableParameter(getParameter("exposure_type","Type of exposure (S=static, F=flowthrough, R=renewal, etc)"),property);
		addPropertyAcceptableParameter(getParameter("chem_analysis_method","Analysis method for water concentration (M=measured, U=unmeasured etc)"),property);		
		addPropertyAcceptableParameter(getParameter("concentration_type","How concentration is reported"),property);
				
		addParameterAcceptableUnit(getUnit("TEXT",""), getParameter("test_id","test_id field in tests table in Ecotox database"));
		addParameterAcceptableUnit(getUnit("TEXT",""), getParameter("exposure_type","Type of exposure (S=static, F=flow-through, R=renewal, etc)"));		
		addParameterAcceptableUnit(getUnit("TEXT",""), getParameter("chem_analysis_method","Analysis method for water concentration (M=measured, U=unmeasured etc)"));
		addParameterAcceptableUnit(getUnit("TEXT",""), getParameter("concentration_type","How concentration is reported"));

//		addPropertyInCategory(getPropertyCategory("Acute aquatic toxicity", "Acute aquatic toxicity"), property);
		
		//*******************************************************************************************************

		ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, gson);
//		printUniqueUnitsListInExperimentalRecords(records);
		System.out.println("experimentalRecords.size()="+records.size());
		
		load(records, typeOther, createDBEntries,sourceName,propertyName);
		
	}
	
	void loadToxCastTTR_Binding() {

		debug=true;//prints values loaded from database like property
		
		boolean createDBEntries=true;
			
		String sourceName="ToxCast";
		
//		String propertyName=DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50;
		String propertyName=DevQsarConstants.TTR_BINDING;

		String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
//		String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+"_"+propertyName+" Experimental Records.json";
		String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+" Experimental Records.json";
		
		File jsonFile=new File(filePath);
		
		System.out.println(filePath+"\t"+jsonFile.exists());

		//**********************************************************************************************
		//First create the property
		ExpPropProperty property=getProperty(propertyName, "concentration that kills half of fathead minnow in 96 hours");
		
		//**********************************************************************************************
		//Add entries for properties_acceptable_units:
		
		//Note: first time you run this property, uncomment out the following lines:
		addPropertyAcceptableUnit(getUnit("DIMENSIONLESS","Dimensionless"), property);

		addPropertyAcceptableParameter(getParameter("maximum_concentration","Maximum concentration tested"),property);		
		addPropertyAcceptableParameter(getParameter("library","Experimental data library used"),property);
		addPropertyAcceptableParameter(getParameter("tested_in_concentration_response","Whether or not tested for concentration response"),property);		
		addPropertyAcceptableParameter(getParameter("dataset","Which dataset the chemical appears in"),property);
				
		addParameterAcceptableUnit(getUnit("MICRO_MOLAR","μM"), getParameter("maximum_concentration","Maximum concentration tested"));
		addParameterAcceptableUnit(getUnit("TEXT",""), getParameter("library","Experimental data library used"));		
		addParameterAcceptableUnit(getUnit("TEXT",""), getParameter("tested_in_concentration_response","Whether or not tested for concentration response"));
		addParameterAcceptableUnit(getUnit("TEXT",""), getParameter("dataset","Which dataset the chemical appears in"));

//		addPropertyInCategory(getPropertyCategory("Acute aquatic toxicity", "Acute aquatic toxicity"), property);
		
		//*******************************************************************************************************

		ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, gson);
//		printUniqueUnitsListInExperimentalRecords(records);
		System.out.println("experimentalRecords.size()="+records.size());
		
		load(records, typeOther, createDBEntries,sourceName,propertyName);
		
	}
	
	
	void loadRatLC50_CoMPAIT() {

		debug=true;//prints values loaded from database like property
		
		boolean createDBEntries=true;
			
		String sourceName="CoMPAIT";
		
		String propertyName=DevQsarConstants.FOUR_HOUR_INHALATION_RAT_LC50;

		String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
//		String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+"_"+propertyName+" Experimental Records.json";
		String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+" Experimental Records.json";
		
		File jsonFile=new File(filePath);
		
		System.out.println(filePath+"\t"+jsonFile.exists());

		//**********************************************************************************************
		//First create the property
		ExpPropProperty property=getProperty(propertyName, "Inhalation concentration that kills half of rats in 4 hours");
		
		//**********************************************************************************************
		//Add entries for properties_acceptable_units:
		
		//Note: first time you run this property, uncomment out the following lines:
		addPropertyAcceptableUnit(getUnit("LOG_MG_L","log10(mg/L)"), property);
		addPropertyAcceptableUnit(getUnit("LOG_PPM","log10(ppm)"), property);

		addPropertyAcceptableParameter(getParameter("qsar_ready_smiles","qsar_ready_smiles"),property);		
		addParameterAcceptableUnit(getUnit("TEXT",""), getParameter("qsar_ready_smiles","qsar_ready_smiles"));		

		//*******************************************************************************************************

		ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, gson);
//		printUniqueUnitsListInExperimentalRecords(records);
		System.out.println("experimentalRecords.size()="+records.size());
		
		load(records, typeOther, createDBEntries,sourceName,propertyName);
		
	}
	
	void loadBCFDataEcotox() {

		debug=true;//prints values loaded from database like property
		
		boolean createDBEntries=true;
			
		String sourceName="ECOTOX_2023_12_14";
		String propertyName=DevQsarConstants.BCF;
		String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
		String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+"_"+propertyName+" Experimental Records.json";
		File jsonFile=new File(filePath);
		
		System.out.println(filePath+"\t"+jsonFile.exists());
		
		//**********************************************************************************************
		//First create the property
		ExpPropProperty property=getProperty(propertyName, DevQsarConstants.BCF);
		
		//**********************************************************************************************
		//Add entries for properties_acceptable_units:
		
		//Note: first time you run this property, uncomment out the following lines:
		addPropertyAcceptableUnit(getUnit("L_KG",DevQsarConstants.L_KG), property);

		List<Parameter>params=new ArrayList<>();
		params.add(getParameter("test_id","test_id field in tests table in Ecotox database"));
		params.add(getParameter("exposure_type","Type of exposure (S=static, F=flowthrough, R=renewal, etc)"));
		params.add(getParameter("Media type","Media type where exposure occurs (e.g. fresh water)"));
		params.add(getParameter("Species latin","Latin name of tested organism (e.g. Pimephales promelas)"));
		params.add(getParameter("Species common","Common name of tested organism (e.g. Fathead minnow)"));
		params.add(getParameter("Response site","Part of organism tested for the chemical"));
		params.add(getParameter("Test location","Where the test was performed (e.g. laboratory"));
		params.add(getParameter("Exposure concentration","Water concentration"));

		ExpPropUnit unitText=getUnit("TEXT","");
		for(Parameter param:params) {
			addParameterAcceptableUnit(unitText, param);	
		}
		
		//*******************************************************************************************************

		ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, gson);
		printUniqueUnitsListInExperimentalRecords(records);
		System.out.println("experimentalRecords.size()="+records.size());
		
		load(records, typeOther, createDBEntries,sourceName,propertyName);
		
	}
	
	void loadSanderHLC() {

		debug=true;//prints values loaded from database like property
		
		boolean createDBEntries=true;
			
		String sourceName="Sander_v5";
		String propertyName=DevQsarConstants.HENRYS_LAW_CONSTANT;
		String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
		String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+" Experimental Records.json";
		File jsonFile=new File(filePath);
		
		System.out.println(filePath+"\t"+jsonFile.exists());
		
		//**********************************************************************************************
		//First create the property
		ExpPropProperty property=getProperty(propertyName, propertyName);
		
		//**********************************************************************************************
		//Acceptable units for property already in db
		//*******************************************************************************************************

		ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, gson);
		printUniqueUnitsListInExperimentalRecords(records);
		System.out.println("experimentalRecords.size()="+records.size());
		
		load(records, typePhyschem, createDBEntries,sourceName,propertyName);
	}
	
	
	void loadOChem() {

		debug=true;//prints values loaded from database like property
		boolean createDBEntries=true;
		String type=typePhyschem;
		
			
//		String propertyName=DevQsarConstants.HENRYS_LAW_CONSTANT;
//		String propertyName=DevQsarConstants.MELTING_POINT;
//		String propertyName=DevQsarConstants.WATER_SOLUBILITY;
		String propertyName=null;
		
		String sourceName="OChem_2024_04_03";
		String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
				
		//Initial loading attempt:
		String folderPath=mainFolder+"data\\experimental\\"+sourceName;
		String substring=sourceName+" Experimental Records ";
		ExperimentalRecords records=getExperimentalRecords(propertyName, folderPath,substring);		

//		String folderPath="data/dev_qsar/exp_prop/" + type + "/"+ sourceName + "/6-12-24";
//		ExperimentalRecords records=getExperimentalRecords(propertyName, folderPath,"Experimental Records-Failed");
		
		records.getRecordsByProperty();
		
		if(true) return;
		
		printUniqueUnitsListInExperimentalRecords(records);
		System.out.println("experimentalRecords.size()="+records.size());
		
		
//		createLiteratureSources(records);
		load(records, type, createDBEntries,sourceName,propertyName);

	}
	
	
	void loadPubChem() {

		debug=true;//prints values loaded from database like property
		boolean createDBEntries=true;
		String type=typePhyschem;
		
			
//		String propertyName=DevQsarConstants.HENRYS_LAW_CONSTANT;
//		String propertyName=DevQsarConstants.MELTING_POINT;
//		String propertyName=DevQsarConstants.WATER_SOLUBILITY;
		String propertyName=null;
		
		String sourceName="PubChem_2024_03_20";
		String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
				

		//Initial loading attempt:
		String folderPath=mainFolder+"data\\experimental\\"+sourceName;
		String substring=sourceName+" Experimental Records ";
		ExperimentalRecords records=getExperimentalRecords(propertyName, folderPath,substring);		

//		String folderPath="data/dev_qsar/exp_prop/" + type + "/"+ sourceName + "/6-12-24";
//		ExperimentalRecords records=getExperimentalRecords(propertyName, folderPath,"Experimental Records-Failed");
//		if(true) return;
		
		printUniqueUnitsListInExperimentalRecords(records);
		System.out.println("experimentalRecords.size()="+records.size());
		
		printPropertiesInExperimentalRecords(records);
		
//		createLiteratureSources(records);
//		load(records, type, createDBEntries,sourceName,propertyName);

	}

	private ExperimentalRecords getExperimentalRecords(String propertyName, String folderPath,String subString) {
		ExperimentalRecords records=new ExperimentalRecords();
		
		File folder=new File(folderPath);
		
		System.out.println(folderPath+"\t"+folder.exists());
		
		for(File file: folder.listFiles()) {
			if(!file.getName().contains(subString) || !file.getName().contains(".json")) continue;
//			System.out.println(file.getName());
			
			ExperimentalRecords recordsi=ExperimentalRecords.loadFromJson(file.getAbsolutePath(), gson);
			for(ExperimentalRecord er:recordsi) {		 
				if (er.property_name.equals(propertyName) || propertyName==null) records.add(er);
			}
			System.out.println(file.getName()+"\t"+recordsi.size());
		}
		return records;
	}
	
	
//	static void loadBCF() {
//		System.out.println("eclipse recognizes new code4");
//		String sourceName = "Burkhard_BCF";
//		String type = "physchem";
//		
//		System.out.println("Retrieving records...");
//		ExperimentalRecords records = getPublicSourceRecords(sourceName, type);
//		System.out.println("Retrieved " + records.size() + " records");
//		
//		System.out.println("Loading " + records.size() + " records...");
//		long t0 = System.currentTimeMillis();
//		ExperimentalRecordLoader loader = new ExperimentalRecordLoader("cramslan");
//		loader.load(records, type, true,sourceName,propertyName);
//		long t = System.currentTimeMillis();
//		System.out.println("Loading completed in " + (t - t0)/1000.0 + " s");
//	}

	
	
	void loadBCFDataBurkhard() {

		debug=true;//prints values loaded from database like property
		
		boolean createDBEntries=true;
			
		String sourceName="Burkhard";
		String propertyName=DevQsarConstants.BCF;
		String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
		String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+"_"+propertyName+" Experimental Records.json";
		File jsonFile=new File(filePath);
		
		System.out.println(filePath+"\t"+jsonFile.exists());
		
		//**********************************************************************************************
		//First create the property
		ExpPropProperty property=getProperty(propertyName, DevQsarConstants.BCF);
		
		//**********************************************************************************************
		//Add entries for properties_acceptable_units:
		
		//Note: first time you run this property, uncomment out the following lines:
		addPropertyAcceptableUnit(getUnit("L_KG",DevQsarConstants.L_KG), property);

		List<Parameter>params=new ArrayList<>();
		params.add(getParameter("Media type","Media type where exposure occurs (e.g. fresh water)"));
		params.add(getParameter("Species latin","Latin name of tested organism (e.g. Pimephales promelas)"));
		params.add(getParameter("Species common","Common name of tested organism (e.g. Fathead minnow)"));
		params.add(getParameter("Response site","Part of organism tested for the chemical"));
		params.add(getParameter("Test location","Where the test was performed (e.g. laboratory"));
		params.add(getParameter("Exposure concentration","Water concentration"));
		params.add(getParameter("Measurement method","Method used to measure the property"));
		params.add(getParameter("Reliability","Reliability of the experimental data point"));
		
		ExpPropUnit unitText=getUnit("TEXT","");
		for(Parameter param:params) {
			addParameterAcceptableUnit(unitText, param);	
		}
		
		//*******************************************************************************************************

		ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, gson);
		printUniqueUnitsListInExperimentalRecords(records);
		System.out.println("experimentalRecords.size()="+records.size());
		
		load(records, typeOther, createDBEntries,sourceName,propertyName);
		
	}
	
	void loadRBIODEG_NITE_OPPT() {

		debug=true;//prints values loaded from database like property
		
		boolean createDBEntries=true;
			
		String sourceName="NITE_OPPT";
		
		String propertyName=DevQsarConstants.RBIODEG;

		String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
		String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+"_"+propertyName+" Experimental Records.json";
		
		File jsonFile=new File(filePath);
		
		System.out.println(filePath+"\t"+jsonFile.exists());
		

		//**********************************************************************************************
		//First create the property
		ExpPropProperty property=getProperty(propertyName, DevQsarConstants.RBIODEG);
		
		//**********************************************************************************************
		//Add entries for properties_acceptable_units:
		
		//Note: first time you run this property, uncomment out the following lines:
		addPropertyAcceptableUnit(getUnit("BINARY","Binary"), property);
		
		addPropertyAcceptableParameter(getParameter("set","Training (T) or Validation (V) set"),property);		
		addParameterAcceptableUnit(getUnit("TEXT",""), getParameter("set","Training (T) or Validation (V) set"));

		//*******************************************************************************************************
		ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, gson);
//		printUniqueUnitsListInExperimentalRecords(records);
		System.out.println("experimentalRecords.size()="+records.size());
		
		load(records, typeOther, createDBEntries,sourceName,propertyName);
		
	}

	private void printUniqueUnitsListInExperimentalRecords(ExperimentalRecords records) {
		//Following gets list of unique units in experimental records:
		List<String>unitsList=new ArrayList<String>();
		
		for (ExperimentalRecord er:records) {
//			System.out.println(er.property_value_units_final);
			String value=er.property_name+"\t"+er.property_value_units_final;
			if (!unitsList.contains(value)) unitsList.add(value);
		}
		Collections.sort(unitsList);
		for (String units:unitsList) System.out.println(units);
		
	}
	
	private void printPropertiesInExperimentalRecords(ExperimentalRecords records) {
		//Following gets list of unique units in experimental records:
		List<String>list=new ArrayList();
		for (ExperimentalRecord er:records) {
//			System.out.println(er.property_value_units_final);
			if (!list.contains(er.property_name)) list.add(er.property_name);
		}
		for (String units:list) {
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
		
//		int publicSourceId=254;//ECOTOX_2023_12_14		
		
//		int publicSourceId=12;//OPERA
//		String propertyName=DevQsarConstants.KmHL;
//		String propertyName=DevQsarConstants.BIODEG_HL_HC;
//		String propertyName=DevQsarConstants.OH;
//		String propertyName=DevQsarConstants.BCF;
//		String propertyName=DevQsarConstants.KOC;

//		int publicSourceId=253;//OPERA2.9
//		String propertyName=DevQsarConstants.ESTROGEN_RECEPTOR_AGONIST;
//		String propertyName=DevQsarConstants.ESTROGEN_RECEPTOR_ANTAGONIST;
//		String propertyName=DevQsarConstants.ESTROGEN_RECEPTOR_BINDING;
//		String propertyName=DevQsarConstants.ANDROGEN_RECEPTOR_AGONIST;
//		String propertyName=DevQsarConstants.ANDROGEN_RECEPTOR_ANTAGONIST;
//		String propertyName=DevQsarConstants.ANDROGEN_RECEPTOR_BINDING;
		
//		String propertyName=DevQsarConstants.HENRYS_LAW_CONSTANT;
		
		String publicSourceName="ECOTOX_2023_12_14";
//		String propertyName=DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50;
		String propertyName=DevQsarConstants.NINETY_SIX_HOUR_BLUEGILL_LC50;
//		String propertyName="96 hour scud LC50";
		
		String sqlPublicSourceName="select id from exp_prop.public_sources ps where ps.name='"+publicSourceName+"';";
		Long publicSourceId=Long.parseLong(SqlUtilities.runSQL(conn, sqlPublicSourceName));
		
		String sqlPropertyName="select id from exp_prop.properties p where p.name='"+propertyName+"';";
		Long propertyId=Long.parseLong(SqlUtilities.runSQL(conn, sqlPropertyName));
				
		String sqlParameters="delete from exp_prop.parameter_values pv2 using exp_prop.property_values pv\n"+
				"where pv2.fk_property_value_id=pv.id and pv.fk_property_id="+propertyId+" and pv.fk_public_source_id="+publicSourceId+" and pv2.created_by='tmarti02'";

//		-- delete property values for fhm lc50 endpoint
		String sqlPropertyValues="delete from exp_prop.property_values pv where fk_public_source_id="+publicSourceId+" and fk_property_id="+propertyId+" and created_by='tmarti02'";
//		String sqlPropertyValues="delete from exp_prop.property_values pv where fk_public_source_id="+publicSourceId+" and created_by='tmarti02'";
		
//	-- Delete source chemicals that came from toxvalv93:
		String sqlSourceChemicals="delete from exp_prop.source_chemicals sc where sc.fk_public_source_id="+publicSourceId+" and created_by='tmarti02'";

		//Literature sources
		String sqlLiteratureSources="delete from exp_prop.literature_sources where created_by='tmarti02' and id>1677";

		//Public sources
		String sqlPublicSources="delete from exp_prop.public_sources where created_by='tmarti02' and id>104";


//		System.out.println(sqlParameters+"\n");
//		System.out.println(sqlPropertyValues);
		
		SqlUtilities.runSQLUpdate(conn, sqlParameters);
		SqlUtilities.runSQLUpdate(conn, sqlPropertyValues);
				
//		SqlUtilities.runSQLUpdate(conn, sqlSourceChemicals);
//		SqlUtilities.runSQLUpdate(conn, sqlLiteratureSources);
//		SqlUtilities.runSQLUpdate(conn, sqlPublicSources);
				
	}
	
	static void deleteEcotoxData() {

		Connection conn=SqlUtilities.getConnectionPostgres();

		int publicSourceId=254;//ECOTOX		
//		String propertyName=DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50;
		String propertyName=DevQsarConstants.BCF;
		
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
	
	static void deleteSander() {
		

		Connection conn=SqlUtilities.getConnectionPostgres();

		int publicSourceId=257;//Sander_v5		
		String propertyName=DevQsarConstants.HENRYS_LAW_CONSTANT.replace("'","''");
		
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
	

	static void deleteOChemNew() {
		

		Connection conn=SqlUtilities.getConnectionPostgres();

		int publicSourceId=259;//OChem_2024_04_03		
		
		
		String sqlParameters="delete from exp_prop.parameter_values pv2 using exp_prop.property_values pv\n"+
				"where pv2.fk_property_value_id=pv.id and pv.fk_public_source_id="+publicSourceId;

		String sqlPropertyValues="delete from exp_prop.property_values pv where fk_public_source_id="+publicSourceId+";";
		
//		String sqlSourceCHemicals="delete from exp_prop.source_chemicals sc where sc.fk_public_source_id="+publicSourceId+" and created_by='tmarti02'";
//		String sqlLiteratureSources="delete from exp_prop.literature_sources where created_by='tmarti02' and id>3378";
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

		List<PropertyValue>propertyValues=load(records,typePhyschem,store,sourceName,propertyName);
		
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
		
//		loadSourceChemicalMap=false;
//		deleteEcotoxData();
		
		ExperimentalRecordLoader loader = new ExperimentalRecordLoader("tmarti02",true);
		loader.loadAcuteAquaticToxicityDataEcotox();
		
		
//		loader.loadRBIODEG_NITE_OPPT();;
//		loader.loadBCFDataEcotox();
//		loader.loadBCFDataBurkhard();

//		loader.loadSanderHLC();
//		deleteSander();

//		loadSourceChemicalMap=false;

//		loadSourceChemicalMap=false;
//		ExperimentalRecordLoader loader = new ExperimentalRecordLoader("tmarti02",true);
//		loader.loadToxCastTTR_Binding();
//		loader.loadRatLC50_CoMPAIT();
		
//		loader.loadOChem();
//		loader.loadPubChem();
		
//		ExperimentalRecordLoader loader = new ExperimentalRecordLoader("tmarti02",false);
//		deleteOChemNew();
//		
		
//		
	}

}

