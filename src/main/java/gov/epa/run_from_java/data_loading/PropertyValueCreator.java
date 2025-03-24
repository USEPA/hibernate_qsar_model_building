package gov.epa.run_from_java.data_loading;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.validation.ConstraintViolationException;

import org.apache.xmpbox.type.AbstractComplexProperty;

import com.google.gson.Gson;

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
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueService;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.PublicSourceService;
import gov.epa.databases.dev_qsar.exp_prop.service.PublicSourceServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.SourceChemicalService;
import gov.epa.databases.dev_qsar.exp_prop.service.SourceChemicalServiceImpl;
import gov.epa.run_from_java.scripts.SqlUtilities;

/**
* @author TMARTI02
*/
public class PropertyValueCreator {
	/**
	 * 
	 */
	ParameterService parameterService = new ParameterServiceImpl();
	PropertyCategoryService propertyCategoryService = new PropertyCategoryServiceImpl();
	PropertyInCategoryService propertyInCategoryService = new PropertyInCategoryServiceImpl();

	ExpPropUnitService expPropUnitService = new ExpPropUnitServiceImpl();
	LiteratureSourceServiceImpl literatureSourceService = new LiteratureSourceServiceImpl();
	PublicSourceServiceImpl publicSourceService = new PublicSourceServiceImpl();

	ParameterAcceptableUnitService parameterAcceptableUnitService=new ParameterAcceptableUnitServiceImpl();
	PropertyAcceptableUnitService propertyAcceptableUnitService=new PropertyAcceptableUnitServiceImpl();  
	PropertyAcceptableParameterService propertyAcceptableParameterService=new PropertyAcceptableParameterServiceImpl();

	SourceChemicalServiceImpl sourceChemicalService = new SourceChemicalServiceImpl();
	PropertyValueServiceImpl propertyValueService = new PropertyValueServiceImpl();

	ExpPropPropertyServiceImpl expPropPropertyService = new ExpPropPropertyServiceImpl();

	String lanId;
	boolean debug=false;
	
	/**
	 * @param experimentalRecordLoader
	 */

	
	public PropertyValueCreator(String lanId,boolean debug) {
		this.lanId=lanId;
		this.debug=debug;
	}
	
	

	public PropertyValue createPropertyValue(ExperimentalRecord er,boolean createDB_Entries) {

		PropertyValue pv=getPropertyValue(er);

		pv.setProperty(getProperty(er.property_name, null));
		
		
		String abbrev=er.property_value_units_final;
		String unitName=DevQsarConstants.getExpPropUnitName(er.property_name, abbrev);
		pv.setUnit(getUnit(unitName,abbrev));

		pv.setDocumentName(er.document_name);
		pv.setFileName(er.file_name);
		
		pv.setPublicSource(getPublicSource(er, createDB_Entries));
		
		
		getPublicSourceOriginal(er, pv, createDB_Entries);//in some cases better to store original source info in the document_name if dont need entry in public_sources table
		getLiteratureSource(er, pv, createDB_Entries);

		setSourceChemical(er,pv,createDB_Entries);		

		//		System.out.println("publicSourceId="+pv.getPublicSource().getId());
		//		System.out.println("publicSourceOriginalId="+pv.getPublicSourceOriginal().getId());
		//		System.out.println("literatureSourceId="+pv.getLiteratureSource().getId());
		//		System.out.println("sourceChemicalId="+pv.getSourceChemical().getId());

		String url = er.url;

		if (url==null || url.isBlank() || 
				(pv.getPublicSource()!=null && url.equals(pv.getPublicSource().getUrl())) 
				|| (pv.getLiteratureSource()!=null && url.equals(pv.getLiteratureSource().getUrl()))) {
			// No individual page URL, do nothing
		} else {
			pv.setPageUrl(url);
		}

		return pv;
	}

	ExpPropProperty getProperty(String name,String description) {
		ExpPropProperty property=propertiesMap.get(name);

		if (property!=null) {
			if (debug) System.out.println("already have property in db: "+property.getName()+"\t"+property.getDescription());
			return property;
		}

		property=new ExpPropProperty();
		property.setName(name);
		property.setCreatedBy(lanId);
		
		if(description==null) {
			property.setDescription("to do add description");	
		} else {
			property.setDescription(description);
		}
		
		
		property= this.expPropPropertyService.create(property);
		System.out.println("Created property="+name);

		propertiesMap.put(name,property);

		return property;
	}
	
	
	

	


	PublicSource getPublicSource(ExperimentalRecord er, boolean createDB_Entries) {

		if(er.source_name==null) return null;
		String name=er.source_name;

		if(publicSourcesMap.containsKey(name)) {
			return publicSourcesMap.get(name);
		} else {

			PublicSource ps = new PublicSource();
			ps.setName(name);
			ps.setDescription("TODO");
			ps.setCreatedBy(lanId);
			

			if(createDB_Entries) {
				ps = publicSourceService.create(ps);
				System.out.println("Created PublicSource="+name);
				publicSourcesMap.put(name, ps);
			}
			return ps;
		}

	}


	private void getPublicSourceOriginal(ExperimentalRecord er,PropertyValue pv, boolean createDB_Entries) {

		String name=null;
		if(er.publicSourceOriginal!=null) {
			name=er.publicSourceOriginal.getName();
		} else {
			name=er.original_source_name;
		}

		if(name==null) return;

		if(publicSourcesMap.containsKey(name)) {
			pv.setPublicSourceOriginal(publicSourcesMap.get(name));
		} else {
			PublicSource ps = new PublicSource();
			if(er.publicSourceOriginal!=null) {
				ps.setName(name);
				if(er.publicSourceOriginal.getDescription()!=null) {
					ps.setDescription(er.publicSourceOriginal.getDescription());
				} else {
					ps.setDescription("TODO");	
				}
				ps.setUrl(er.publicSourceOriginal.getUrl());
			} else {
				ps.setName(name);
				ps.setDescription("TODO");
			}

			ps.setCreatedBy(lanId);

			if(createDB_Entries) {
				try {
					ps = publicSourceService.create(ps);
					publicSourcesMap.put(name, ps);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			pv.setPublicSourceOriginal(ps);
		}



	}



	/**
	 * Note this doesnt assure uniqueness of name field in the database.
	 * 
	 * @param er
	 * @param pv
	 * @param createDB_Entries
	 */
	private void getLiteratureSource(ExperimentalRecord er, PropertyValue pv,boolean createDB_Entries) {

		if (er.literatureSource==null) {
			return;
		}

		String literatureSourceCitation=er.literatureSource.getCitation();//should already be set		

		//			if(er.literatureSource.getName()==null) {
		//				er.literatureSource.setName(literatureSourceCitation);
		//			}


		if (literatureSourcesMap.containsKey(literatureSourceCitation)) {
			pv.setLiteratureSource(literatureSourcesMap.get(literatureSourceCitation));
		} else {

			LiteratureSource ls=er.literatureSource;
			ls.setCreatedBy(lanId);

			if(createDB_Entries) {
				try {
					System.out.println("Creating ls, citation="+ls.getCitation());
					ls = literatureSourceService.create(ls);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			literatureSourcesMap.put(literatureSourceCitation, ls);
			pv.setLiteratureSource(ls);

		}

	}

	public boolean postPropertyValue(PropertyValue propertyValue) {

		try {
			propertyValue = propertyValueService.create(propertyValue);
			return (propertyValue!=null);

		} catch(Exception ex) {
			ex.printStackTrace();
			return false;
		}

	}


	private void setSourceChemical(ExperimentalRecord rec,PropertyValue pv,boolean createDB_Entries) {

		SourceChemical sourceChemical =rec.getSourceChemical(lanId, pv.getPublicSource(), pv.getLiteratureSource()); 

		SourceChemical dbSourceChemical=null;

		//			if(loader.sourceChemicalMap.containsKey(sourceChemical.getKey())) {
		//				dbSourceChemical=loader.sourceChemicalMap.get(sourceChemical.getKey());
		//				if(debug) System.out.println("Found in map\t"+sourceChemical.getKey());
		//			} else if (!ExperimentalRecordLoader.loadSourceChemicalMap) {
		//				dbSourceChemical = sourceChemicalService.findMatch(sourceChemical);
		//				if(debug) System.out.println("Found by one at a time service\t"+sourceChemical.getKey());
		//			}

		if(sourceChemicalMap.containsKey(sourceChemical.getKey())) {
			dbSourceChemical=sourceChemicalMap.get(sourceChemical.getKey());
			if(debug) System.out.println("Found in map\t"+sourceChemical.getKey());
		} 		

		if (dbSourceChemical==null) {
			
//			if(sourceChemical.getSourceCasrn().equals("50-04-4")) {
//				System.out.println(sourceChemical.getKey());
//			}

			if(createDB_Entries) {
				try {
					if(debug) System.out.println("Creating "+sourceChemical.getKey());
					sourceChemical = sourceChemicalService.create(sourceChemical);

					//Store in map:
					sourceChemicalMap.put(sourceChemical.getKey(),sourceChemical);
					//TODO Note: unique index for source_chemicals table isnt stopping creation of duplicates if dont put in map in line above

				} catch (ConstraintViolationException e) {
					e.printStackTrace();
				}
			}
		} else {
			sourceChemical = dbSourceChemical;
		}


		pv.setSourceChemical(sourceChemical);
	}

	private PropertyValue getPropertyValue(ExperimentalRecord rec) {
		PropertyValue propertyValue = new PropertyValue();
		propertyValue.setCreatedBy(lanId);

		propertyValue.setValueQualifier(rec.property_value_numeric_qualifier);
		propertyValue.setValuePointEstimate(rec.property_value_point_estimate_final);
		propertyValue.setValueMin(rec.property_value_min_final);
		propertyValue.setValueMax(rec.property_value_max_final);

		if (rec.property_value_qualitative!=null && 
				rec.property_value_qualitative.length()>255) { 
			rec.property_value_qualitative = rec.property_value_qualitative.substring(0, 255);
		}

		propertyValue.setValueText(rec.property_value_qualitative);

		if (rec.property_value_string!=null && 
				rec.property_value_string.length()>1000) { 
			rec.property_value_string = rec.property_value_string.substring(0, 1000);
		}
		propertyValue.setValueOriginal(rec.property_value_string);


		if (rec.property_value_string_parsed!=null && 
				rec.property_value_string_parsed.length()>1000) { 
			rec.property_value_string_parsed = rec.property_value_string_parsed.substring(0, 1000);
		}
		propertyValue.setValueOriginalParsed(rec.property_value_string_parsed);



		propertyValue.setNotes(rec.note);
		propertyValue.setQcFlag(rec.flag);
		propertyValue.setKeep(rec.keep);
		propertyValue.setKeepReason(rec.reason);

		return propertyValue;
	}

	

	public void createParameter(String name,String description,HashSet<String>unitAbbrevs) {
		
		Parameter parameter=getParameter(name, description);
		
		for (String unitAbbrev:unitAbbrevs) {
			
			String unitName=DevQsarConstants.getConstantNameByReflection(unitAbbrev);
			
			ExpPropUnit unit=this.unitsMap.get(unitName);
			
			if(unit==null) {
				System.out.println("No unit in database with abbrev="+unitAbbrev+" for parameter name="+name);
				continue;
			} else {
				if(!isAcceptableUnitForParameter(unit, parameter)) {
					System.out.println("Adding acceptable unit "+unitAbbrev+" for parameter "+name );
					addParameterAcceptableUnit(unit, parameter);
				}
			}
			
		}
		
		
		

	}
	
	public HashSet<String> getMissingParameters (ExperimentalRecords records) {

		HashSet<String>parameterNames=new HashSet<>();
		
		for (ExperimentalRecord er:records) {
			if(er.experimental_parameters!=null) {
				for(String parameterName:er.experimental_parameters.keySet()) {
					parameterNames.add(parameterName);
				}
			}
			if(er.parameter_values!=null) {
				for(ParameterValue parameterValue:er.parameter_values) {
					parameterNames.add(parameterValue.getParameter().getName());
				}
			}
		}
		
		Iterator<String> iterator = parameterNames.iterator();
        while (iterator.hasNext()) {
            String parameterName = iterator.next();
            
            boolean haveParameterInDB=false;
            for (String parameterNameDB:parametersMap.keySet()) {
				if(parameterNameDB.equals(parameterName)) {
					haveParameterInDB=true;
					break;
				}
			}
            if (haveParameterInDB)iterator.remove(); // Safely remove the element
            
        }
		
		for (String parameterName:parameterNames) {
			System.out.println(parameterName+"\tparameter not in parameters");
		}
		
		return parameterNames;
		
		
	}

	public void createTextParameter(String name,String description) {
		ExpPropUnit unit=getUnit("TEXT","Text");
		Parameter parameter=getParameter(name, description);		
		addParameterAcceptableUnit(unit, parameter);
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

	private ExperimentalRecords getPublicSourceRecords(String publicSourceName, String type) {
		Gson gson=new Gson();
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

	private PropertyCategory getPropertyCategory(String name,String description) {

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
		
		System.out.println("Creating unit="+name+", "+abbreviation);
		unit= expPropUnitService.create(unit);
	
		unitsMap.put(name,unit);
	
		return unit;
	}

//	private void setUnit(ExperimentalRecord er,PropertyValue pv) {
	//
//			if(er.property_value_units_final==null) {
//				//				System.out.println("No units");
//				return;
//			}
	//
//			String unitName=DevQsarConstants.getExpPropUnitName(er.property_name,er.property_value_units_final);
	//
//			if (loader.unitsMap.containsKey(unitName)) {
//				pv.setUnit(loader.unitsMap.get(unitName));
//			} else {
//				//TODO should we add missing units to units table?
//				System.out.println("Unknown unitName for "+er.property_value_units_final);
//			}
//		}
	
	public void mapTables(String sourceName) {
	
		System.out.println("Mapping tables...");
		
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
	
		//		if (loadSourceChemicalMap) {
		//			System.out.print("Loading sourceChemical map...");
		//			List<SourceChemical> sourceChemicals = sourceChemicalService.findAll();
		//			for (SourceChemical sourceChemical:sourceChemicals) {
		//				sourceChemicalMap.put(sourceChemical.getKey(),sourceChemical);
		//			}
		//			System.out.println("Done");
		//		}
	
		loadSourceChemicalMap(sourceName);
		
		System.out.println("done");
	
	}

	void loadSourceChemicalMap(String publicSourceName) {
	
		PublicSource ps=this.publicSourceService.findByName(publicSourceName);
	
		if(ps==null) {
			System.out.println("Public source="+publicSourceName+" doesnt exist yet, cant load source chemicals for it");
			return;
		}
	
		System.out.print("Loading sourceChemical map...");
		//		List<SourceChemical> sourceChemicals = sourceChemicalService.findAllFromSource(ps);//slowwwww
	
		List<SourceChemical> sourceChemicals = sourceChemicalService.findAllFromSourceSql(ps);
		
//		System.out.println("Source chemicals in db for "+publicSourceName+":\t"+sourceChemicals.size());
	
		for (SourceChemical sourceChemical:sourceChemicals) {
			//			System.out.println(Utilities.gson.toJson(sourceChemical));
			sourceChemicalMap.put(sourceChemical.getKey(),sourceChemical);
			
//			if(sourceChemical.getSourceCasrn().equals("50-04-4")) {
//				System.out.println(sourceChemical.getKey());
//			}
				
		}
		System.out.println("Done:"+sourceChemicalMap.size()+" source chemicals");
	
		//TODO this will only have literature source with an id. If want full ls, need to pass a source chemical map by id to look it up
	
	}

	void createSourcesBatch(List<ExperimentalRecord> records) {
		List<LiteratureSource>litSources = new ArrayList<>();
		List<PublicSource> pubSources = new ArrayList<>();
	
		for (ExperimentalRecord rec:records) {
			
			if(rec.literatureSource!=null) {
				if (!literatureSourcesMap.containsKey(rec.literatureSource.getCitation())) {
					LiteratureSource ls=rec.literatureSource;
					ls.setCreatedBy(lanId);
					literatureSourcesMap.put(rec.literatureSource.getCitation(),ls);
					
					litSources.add(ls);
				
				}
			}
	
			String publicSourceOriginalName=rec.original_source_name;
			if(rec.publicSourceOriginal!=null) publicSourceOriginalName=rec.publicSourceOriginal.getName();
			if (publicSourceOriginalName!=null && !publicSourcesMap.containsKey(publicSourceOriginalName)) {
				PublicSource psO = new PublicSource();
				psO.setName(rec.source_name);
				psO.setDescription("TODO");
				psO.setCreatedBy(lanId);
				publicSourcesMap.put(publicSourceOriginalName,psO);
				pubSources.add(psO);
			} else if(publicSourceOriginalName!=null) {
//				System.out.println(publicSourceOriginalName+"\t"+publicSourcesMap.get(publicSourceOriginalName).getId());
			}
		}
		
		try {
			
			System.out.println("litSources.size()="+litSources.size());
			this.literatureSourceService.createBatchSQL(litSources, SqlUtilities.getConnectionPostgres());
			
			System.out.println("pubSources.size()="+pubSources.size());
			this.publicSourceService.createBatchSQL(pubSources,SqlUtilities.getConnectionPostgres());
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	
	}

	void createSourceChemicals(List<ExperimentalRecord> records) {
		List<SourceChemical>sourceChemicals=new ArrayList<>();

		for (ExperimentalRecord rec:records) {

			PublicSource ps=publicSourcesMap.get(rec.source_name);

			LiteratureSource ls=null;

			if(rec.literatureSource!=null) {
				ls=literatureSourcesMap.get(rec.literatureSource.getCitation());
			}
			SourceChemical sourceChemical =rec.getSourceChemical(lanId, ps, ls); 
			if(!sourceChemicalMap.containsKey(sourceChemical.getKey())) {
				sourceChemicals.add(sourceChemical);
				sourceChemicalMap.put(sourceChemical.getKey(),sourceChemical);
			} 		
		}

		System.out.println("Source chemicals to create:"+sourceChemicals.size());

		List<SourceChemical> sourceChemicals2=new ArrayList<>();
		int batchSize=1000;
		
//		if(true)return;

		int created=0;
		
		try {

			for (int i=0;i<sourceChemicals.size();i++) {
				
				sourceChemicals2.add(sourceChemicals.get(i));
				
				if(sourceChemicals2.size()==batchSize) {
					sourceChemicalService.createBatchSql(sourceChemicals2, SqlUtilities.getConnectionPostgres());
					created+=batchSize;
					sourceChemicals2.clear();
				}
			}

			//Do what's left:
			sourceChemicalService.createBatchSql(sourceChemicals2, SqlUtilities.getConnectionPostgres());
			
			created+=sourceChemicals2.size();

			for(SourceChemical sc:sourceChemicals) {
				sourceChemicalMap.put(sc.getKey(),sc);
//				System.out.println(sc.getId()+"\t"+sc.getKey());
			}
			
			System.out.println("Source chemicals created:"+created);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public HashSet<String> getMissingParameterAcceptableUnits (ExperimentalRecords records) {
		HashSet<String>unitAbbrevs=new HashSet<>();
		
		for (ExperimentalRecord er:records) {
	
			if(er.parameter_values==null) continue; 
	
			for(ParameterValue parameterValue:er.parameter_values) {
				
				ExpPropUnit unit=parameterValue.getUnit();
				Parameter parameter=parameterValue.getParameter();
								
				String unitName=DevQsarConstants.getConstantNameByReflection(unit.getAbbreviation());
				
				if(!isAcceptableUnitForParameter(unit, parameter)) {
					unitAbbrevs.add(unit.getAbbreviation());
//					System.out.println("Missing units:\t"+unitName+" for parameter:"+parameterValue.getParameter().getName()+", unitAbbrev="+unit.getAbbreviation());
				}
			}
	
		}
		
		for (String unitAbbrev:unitAbbrevs) {
			System.out.println("Unit not acceptable:\t"+unitAbbrev);
		}
	
		return unitAbbrevs;
		
		
	}



	private boolean isAcceptableUnitForParameter(ExpPropUnit unit, Parameter parameter) {

		boolean isAcceptable=false;
		
		for (ParameterAcceptableUnit pau:parameterAcceptableUnits) {
			
			if(pau.getParameter().getName().equals(parameter.getName())) {

				if(pau.getUnit().getAbbreviation().equals(unit.getAbbreviation())) {
					isAcceptable=true;
					break;
				}
				
			}
			
		}
		return isAcceptable;
	}
	
	
	
//	static boolean loadSourceChemicalMap=true;//takes a while but faster for loading lots of records


public HashSet<String> getMissingParameterUnits (ExperimentalRecords records) {
	
		HashSet<String>unitAbbrevs=new HashSet<>();
	
		for (ExperimentalRecord er:records) {
	
			if(er.parameter_values==null) continue; 
	
			for(ParameterValue parameterValue:er.parameter_values) {
				ExpPropUnit unit=parameterValue.getUnit();
				
				String unitName=DevQsarConstants.getConstantNameByReflection(unit.getAbbreviation());
				
				if(!unitsMap.containsKey(unitName)) {
					unitAbbrevs.add(unit.getAbbreviation());
					System.out.println("Missing units:\t"+unitName+" for parameter:"+parameterValue.getParameter().getName()+", unitAbbrev="+unit.getAbbreviation());
				}
			}
	
		}
		
		for (String unitAbbrev:unitAbbrevs) {
			System.out.println("Missing unitAbbrev:\t"+unitAbbrev);
		}
	
		return unitAbbrevs;
	}


public static final Pattern STRING_COLUMN_PATTERN = Pattern.compile("([~><=]{1,2})?(-?[0-9\\.]+)([-~])?(-?[0-9\\.]+)?");

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
}
