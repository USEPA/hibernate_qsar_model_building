package gov.epa.run_from_java.data_loading;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;
import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;
import gov.epa.databases.dev_qsar.exp_prop.service.LiteratureSourceService;
import gov.epa.databases.dev_qsar.exp_prop.service.LiteratureSourceServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueService;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;
import gov.epa.databases.dev_qsar.qsar_datasets.service.PropertyService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.PropertyServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.UnitService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.UnitServiceImpl;
import gov.epa.endpoints.datasets.DatasetCreator;
import gov.epa.endpoints.datasets.dsstox_mapping.DsstoxMapper;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

/**
* @author TMARTI02
*/
public class OperaLoader {

	PropertyValueServiceImpl propertyValueService= new PropertyValueServiceImpl();
	LiteratureSourceService literatureSourceService = new LiteratureSourceServiceImpl();

	Map<String, LiteratureSource> literatureSourcesMap = new HashMap<String, LiteratureSource>();//key is citation 

	ExperimentalRecordLoader loader;
	
	
	OperaLoader(boolean loadSC) {
		ExperimentalRecordLoader.loadSourceChemicalMap=loadSC;
		loader=new ExperimentalRecordLoader("tmarti02",true);
	}
	

	/**
	 * Adds literature sources from sdfs to existing property value records
	 * @param propertyName
	 */
	void addLiteratureSources(String propertyName,boolean createDB_Entries) {
		
		List<String> includedSources=Arrays.asList("OPERA");
		
		System.out.println("Selecting experimental property data for " + propertyName + "...");
		long t5 = System.currentTimeMillis();
		List<PropertyValue> propertyValues = propertyValueService.findByPropertyNameWithOptions(propertyName, true, true);
		long t6 = System.currentTimeMillis();
		System.out.println("Selection time = " + (t6 - t5)/1000.0 + " s");

		System.out.println("Raw records:"+propertyValues.size());		
		DatasetCreator.excludePropertyValues2(includedSources, propertyValues);
		if (includedSources.size()>0) System.out.println("Raw records after source exclusion:"+propertyValues.size());

		Hashtable<String, String> htOperaReferences = getReferenceHashtable(propertyName);
		
//		for (String dtxsid:htOperaReferences.keySet()) {
//			System.out.println(dtxsid+"\t"+htOperaReferences.get(dtxsid));
//		}
		
		System.out.println("pv size="+propertyValues.size());
		System.out.println("refs size="+htOperaReferences.size());
		List<PropertyValue>propertyValuesUpdate=new ArrayList<>();
		
		
		for (PropertyValue pv:propertyValues) {

//			if (propertyName.equals(DevQsarConstants.LOG_OH)) {//Fix units
//				String unitNameNew=DevQsarConstants.getUnitNameByReflection(DevQsarConstants.LOG_CM3_MOLECULE_SEC);
//				ExpPropUnit unitNew=loader.unitsMap.get(unitNameNew);
//				pv.setUnit(unitNew);
//			}

			if(pv.getLiteratureSource()!=null) continue;
			
			String key=pv.getSourceChemical().getSourceDtxsid();
			if (key==null) key=pv.getSourceChemical().getSourceCasrn();
			
			if (key==null) {
				System.out.println("key is null for "+pv.getSourceChemical().getSourceCasrn());
			}
			
			if (htOperaReferences.containsKey(key)) {
				
				String reference=htOperaReferences.get(key);

//				addLiteratureSource(createDB_Entries, pv, key, reference);
//				if (createDB_Entries && pv.getLiteratureSource() != null) {
//					propertyValuesUpdate.add(pv);
//				}

				pv.setDocumentName(reference);//much simpler and doesnt create extra source chemicals due to having a literature source
				pv.setUpdatedBy("tmarti02");
				
				System.out.println(pv.getId()+"\t"+pv.getProperty().getName()+"\t"+pv.getSourceChemical().getSourceDtxsid()+"\t"+reference);
				
				if (createDB_Entries) {
					propertyValuesUpdate.add(pv);//add it to the list for batch update
				}
			
			}//end reference!=null
			
			if (pv.getLiteratureSource()!=null) {
//				System.out.println(pv.getSourceChemical().getKey()+"\t"+pv.getValuePointEstimate()+"\t"+pv.getUnit().getName()+"\t"+pv.getLiteratureSource().getCitation());
			} else {
//				System.out.println(pv.getSourceChemical().getSourceDtxsid()+"\t"+pv.getSourceChemical().getSourceCasrn()+"\t"+pv.getValuePointEstimate()+"\t"+pv.getUnit().getName()+"\tnull");
			}
			
			
//			System.out.println(pv.getUnit().getName()+"\t"+dtxsid+"\t"+reference);
		}
		
		propertyValueService.update(propertyValuesUpdate);

//		try {
//			System.out.println("Updating pv for " + key+"\t"+pv.getLiteratureSource().getCitation());
//			propertyValueService.update(pv);
//		} catch (Exception ex) {
//			System.out.println(ex.getMessage()+": Couldnt update pv for " + key);
//		}

		
	}


	private void addLiteratureSource(boolean createDB_Entries, PropertyValue pv, String key, String reference) {
		LiteratureSource lsNew = createLiteratureSource(reference);
		LiteratureSource ls = loader.literatureSourcesMap.get(lsNew.getCitation());

		if (ls!=null) {
			pv.setLiteratureSource(ls);
		} else {
//					reference=reference.replace("'", "''");//otherwise it crashes
			ls=lsNew;
			if(createDB_Entries) {
				try {
					System.out.println("Creating lsNew="+ls.getCitation());
					ls = loader.literatureSourceService.create(ls);
				} catch (Exception ex) {
					System.out.println(ex.getMessage()+"\tFailed to create " +reference+" for "+key);
					
				}
			}
			loader.literatureSourcesMap.put(ls.getCitation(), ls);
			pv.setLiteratureSource(ls);
		}
	}


	private Hashtable<String, String> getReferenceHashtable(String propertyName) {
		Hashtable<String,String> htOperaReferences=null;

		if (propertyName.equals(DevQsarConstants.LOG_OH)) {
			htOperaReferences = Utilities.createOpera_Reference_Lookup("AOH", "OH Reference");
		} else if (propertyName.equals(DevQsarConstants.VAPOR_PRESSURE)) {
			htOperaReferences = Utilities.createOpera_Reference_Lookup("VP", "VP Reference");
		} else if (propertyName.equals(DevQsarConstants.WATER_SOLUBILITY)) {
			htOperaReferences = Utilities.createOpera_Reference_Lookup("WS", "WS Reference");
		} else if (propertyName.equals(DevQsarConstants.HENRYS_LAW_CONSTANT)) {
			htOperaReferences = Utilities.createOpera_Reference_Lookup("HL", "HL Reference");
		} else if (propertyName.equals(DevQsarConstants.LOG_KOW)) {
			htOperaReferences = Utilities.createOpera_Reference_Lookup("LogP", "Kow Reference");
		} else if (propertyName.equals(DevQsarConstants.LOG_KOC)) {
			htOperaReferences = Utilities.createOpera_Reference_Lookup("KOC", "KocRef");
		} else if (propertyName.equals(DevQsarConstants.LOG_KOA)) {
			htOperaReferences = Utilities.createOpera_Reference_Lookup("KOA", "LogKOA_Ref");
		}
		return htOperaReferences;
	}


	private LiteratureSource createLiteratureSource(String reference) {

		LiteratureSource ls=new LiteratureSource();

		if (reference.contains(";")) {
			reference=reference.substring(0,reference.indexOf(";"));
		}
		
		if (reference.contains("(")) {
			ls.setAuthor(reference.substring(0,reference.indexOf("(")).trim());
			ls.setYear(reference.substring(reference.indexOf("(")+1,reference.indexOf(")")));
		}

		ls.setCreatedBy(loader.lanId);
		ls.setCitation(reference);
		ls.setName(reference);

//		System.out.println(reference+"\t"+ls.getAuthor()+"\t"+ls.getYear());
		return ls;
	}
	
	public void deletePropertyValues(String propertyName) {
		List<String> includedSources=Arrays.asList("OPERA","ThreeM");
		
		System.out.println("Selecting experimental property data for " + propertyName + "...");
		long t5 = System.currentTimeMillis();
		List<PropertyValue> propertyValues = propertyValueService.findByPropertyNameWithOptions(propertyName, false,false);
		long t6 = System.currentTimeMillis();
		System.out.println("Selection time = " + (t6 - t5)/1000.0 + " s");

		System.out.println("Raw records:"+propertyValues.size());		
		DatasetCreator.excludePropertyValues2(includedSources, propertyValues);
		if (includedSources.size()>0) System.out.println("Raw records after source exclusion:"+propertyValues.size());
		
		propertyValueService.delete(propertyValues);
		
		

	}
	
	
	
	void loadPropertyValuesFromOPERA_ExperimentalRecordsFile(String propertyName,String type,String sourceName) {
		
		int numFiles=2;
				
		ExpPropProperty prop=loader.propertiesMap.get(propertyName);
		
		if(prop==null) {
			System.out.println(propertyName +" missing in exp_prop schema");
			return;
		} 
	
		System.out.println("\nLoading property values for "+propertyName+"("+prop.getDescription()+")");
		
				
//		String sourceName="OPERA";
		
		String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\"+sourceName+"\\";

		ExperimentalRecords recordsAll=new ExperimentalRecords();
		
		for (int fileNum=1;fileNum<=numFiles;fileNum++) {
			String filePath=mainFolder+sourceName+" Experimental Records "+fileNum+".json";

			ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, ExperimentalRecordLoader.gson);
			for (int i=0;i<records.size();i++) {
				ExperimentalRecord er=records.get(i);
				if (er.property_name.equals(propertyName)) {
					recordsAll.add(er);
				}
			}
		}
		
//		System.out.println(Utilities.gson.toJson(recordsAll));
//		printUniqueUnitsListInExperimentalRecords(records);
		System.out.println("experimentalRecords.size()="+recordsAll.size());
		loader.load(recordsAll,type,true,sourceName,propertyName);
	}
	
	void reloadFailedPropertyValuesFromOPERA_ExperimentalRecordsFile(String propertyName,String type,String sourceName) {
		
		ExpPropProperty prop=loader.propertiesMap.get(propertyName);
		
		if(prop==null) {
			System.out.println(propertyName +" missing in exp_prop schema");
			return;
		} 
	
		System.out.println("\nLoading property values for "+propertyName+"("+prop.getDescription()+")");
		
				
//		String sourceName="OPERA";
		
		String mainFolder="data\\dev_qsar\\exp_prop\\physchem\\"+sourceName+"\\";

		ExperimentalRecords recordsAll=new ExperimentalRecords();
		
		String filePath=mainFolder+propertyName+" Experimental Records-Failed.json";

		ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, ExperimentalRecordLoader.gson);
		
		
		for (int i=0;i<records.size();i++) {
			ExperimentalRecord er=records.get(i);
			
//			System.out.println(er.chemical_name.length());
			
			if (er.property_name.equals(propertyName)) {
				recordsAll.add(er);
			}
		}
		
//		System.out.println(Utilities.gson.toJson(recordsAll));
//		printUniqueUnitsListInExperimentalRecords(records);
		System.out.println("experimentalRecords.size()="+recordsAll.size());
		loader.load(recordsAll,type,true,sourceName,propertyName);
	}

	
	

	void loadMissingSourceChemicalsFromOPERA_ExperimentalRecordsFile(String propertyName) {
		
		System.out.println("\nLoading property values for "+propertyName);
		
		String sourceName="OPERA";
		String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
		String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+" Experimental Records.json";

		ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, ExperimentalRecordLoader.gson);
		
		for (int i=0;i<records.size();i++) {
			ExperimentalRecord er=records.get(i);
			if (!er.property_name.equals(propertyName)) records.remove(i--);
		}
		
//		printUniqueUnitsListInExperimentalRecords(records);
		System.out.println("experimentalRecords.size()="+records.size());
//		loader.load(records,type,true);
		
		for (int i=0;i<records.size();i++) {
			ExperimentalRecord er=records.get(i);
			
			String key=er.casrn+"\t"+er.smiles+"\t"+er.chemical_name+"\t"+er.dsstox_substance_id+"\t"+
			"null\tnull\t12\tnull";
					
			if(!loader.sourceChemicalMap.containsKey(key)) {
				System.out.println("Missing\t"+key);
				SourceChemical sc=createSourceChemical(er);
				System.out.println(sc==null);
			}
			
		}		
		
	}
	
	SourceChemical createSourceChemical(ExperimentalRecord rec) {
		SourceChemical sourceChemical = new SourceChemical();
		sourceChemical.setCreatedBy(loader.lanId);

		if (rec.casrn!=null && !rec.casrn.isBlank()) {
			sourceChemical.setSourceCasrn(rec.casrn);
		}

		if (rec.chemical_name!=null && !rec.chemical_name.isBlank()) {
			sourceChemical.setSourceChemicalName(rec.chemical_name);
		}

		if (rec.smiles!=null && !rec.smiles.isBlank()) {
			sourceChemical.setSourceSmiles(rec.smiles);
		}

		if (rec.dsstox_substance_id!=null && !rec.dsstox_substance_id.isBlank()) {
			if (rec.dsstox_substance_id.startsWith("DTXCID")) {
				sourceChemical.setSourceDtxcid(rec.dsstox_substance_id);
			} else if (rec.dsstox_substance_id.startsWith("DTXSID")) {
				sourceChemical.setSourceDtxsid(rec.dsstox_substance_id);
			} else if (rec.dsstox_substance_id.startsWith("DTXRID")) {
				sourceChemical.setSourceDtxrid(rec.dsstox_substance_id);
			}
		}
		
		
		sourceChemical.setPublicSource(loader.publicSourcesMap.get("OPERA"));
		
//		if(pv.getLiteratureSource()!=null) {
//			sourceChemical.setLiteratureSource(pv.getLiteratureSource());
//		}

		
		if (loader.sourceChemicalMap.containsKey(sourceChemical.getKey())) {
			sourceChemical=loader.sourceChemicalMap.get(sourceChemical.getKey());
//			System.out.println("Found sc in map:\t"+sourceChemical.getKey());
		} else {
			try {
				sourceChemical = loader.sourceChemicalService.create(sourceChemical);
				loader.sourceChemicalMap.put(sourceChemical.getKey(), sourceChemical);
				System.out.println("Created sc:\t"+sourceChemical.getKey());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			 
		}
		
		return sourceChemical;
	}
	
	
	void createExpPropProperties () {
		PropertyService propertyService = new PropertyServiceImpl();
		UnitService unitService=new UnitServiceImpl();


//		ExpPropProperty property=loader.getProperty(DevQsarConstants.BCF, "Bioconcentration factor: the ratio of the chemical concentration in fish as a result of absorption via the respiratory surface to that in water at steady state");
//		ExpPropUnit unit=loader.getUnit("LOG_L_KG","log10(L/kg)");
//		loader.addPropertyAcceptableUnit(unit, property);
//		
//		property=loader.getProperty(DevQsarConstants.BIODEG_HL_HC, "biodegradation half-life for compounds containing only carbon and hydrogen");
//		unit=loader.getUnit("LOG_DAYS","log10(days)");
//		loader.addPropertyAcceptableUnit(unit, property);
//		
//		property=loader.getProperty(DevQsarConstants.KmHL, "The whole body primary biotransformation rate (half-life) constant for organic chemicals in fish");
//		unit=loader.getUnit("LOG_DAYS","log10(days)");
//		loader.addPropertyAcceptableUnit(unit, property);
//		
//		property=loader.getProperty(DevQsarConstants.KOC, "Soil adsorption coefficient of organic compounds");
//		unit=loader.getUnit("LOG_L_KG","log10(L/kg)");
//		loader.addPropertyAcceptableUnit(unit, property);
//
//		property=loader.getProperty(DevQsarConstants.OH, "OH rate constant for the atmospheric, gas-phase reaction between photochemically produced hydroxyl radicals and organic chemicals");
//		unit=loader.getUnit("LOG_CM3_MOLECULE_SEC","log10(cm3/molecule-sec)");
//		loader.addPropertyAcceptableUnit(unit, property);

		//**********************************************************************************************************
		//Clone records from datasets schema since already created for the OPERA predictions work:
		
//		Property property=propertyService.findByName(DevQsarConstants.RBIODEG);
//		Unit unit=unitService.findByName(DevQsarConstants.getUnitNameByReflection(DevQsarConstants.BINARY));
//		System.out.println(property.getName()+"\t"+unit.getName());
//		ExpPropProperty propertyExpProp=loader.getProperty(property.getName(), property.getDescription());
//		ExpPropUnit unitExpProp=loader.getUnit(unit.getName(),unit.getAbbreviation());
//		loader.addPropertyAcceptableUnit(unitExpProp, propertyExpProp);
		
//		Property property=propertyService.findByName(DevQsarConstants.CLINT);
//		Unit unit=unitService.findByName(DevQsarConstants.getUnitNameByReflection(DevQsarConstants.UL_MIN_1MM_CELLS));
//		System.out.println(property.getName()+"\t"+unit.getName());
//		ExpPropProperty propertyExpProp=loader.getProperty(property.getName(), property.getDescription());
//		ExpPropUnit unitExpProp=loader.getUnit(unit.getName(),unit.getAbbreviation());
//		loader.addPropertyAcceptableUnit(unitExpProp, propertyExpProp);
		
//		Property property=propertyService.findByName(DevQsarConstants.FUB);
//		Unit unit=unitService.findByName(DevQsarConstants.getUnitNameByReflection(DevQsarConstants.DIMENSIONLESS));
//		System.out.println(property.getName()+"\t"+unit.getName());
//		ExpPropProperty propertyExpProp=loader.getProperty(property.getName(), property.getDescription());
//		ExpPropUnit unitExpProp=loader.getUnit(unit.getName(),unit.getAbbreviation());
//		loader.addPropertyAcceptableUnit(unitExpProp, propertyExpProp);

//		Property property=propertyService.findByName(DevQsarConstants.ORAL_RAT_LD50);
//		Unit unit=unitService.findByName(DevQsarConstants.getUnitNameByReflection(DevQsarConstants.MG_KG));
//		System.out.println(property.getName()+"\t"+unit.getName());
//		ExpPropProperty propertyExpProp=loader.getProperty(property.getName(), property.getDescription());
//		ExpPropUnit unitExpProp=loader.getUnit(unit.getName(),unit.getAbbreviation());
//		loader.addPropertyAcceptableUnit(unitExpProp, propertyExpProp);

//		Property property=propertyService.findByName(DevQsarConstants.CACO2);
//		Unit unit=unitService.findByName(DevQsarConstants.getUnitNameByReflection(DevQsarConstants.CM_SEC));
//		System.out.println(property.getName()+"\t"+unit.getName());
//		ExpPropProperty propertyExpProp=loader.getProperty(property.getName(), property.getDescription());
//		ExpPropUnit unitExpProp=loader.getUnit(unit.getName(),unit.getAbbreviation());
//		loader.addPropertyAcceptableUnit(unitExpProp, propertyExpProp);
		
//		ExpPropUnit unitExpProp=loader.getUnit("TEXT",null);
//		createProperty(DevQsarConstants.ESTROGEN_RECEPTOR_BINDING,propertyService, unitExpProp);
//		createProperty(DevQsarConstants.ESTROGEN_RECEPTOR_AGONIST,propertyService, unitExpProp);
//		createProperty(DevQsarConstants.ESTROGEN_RECEPTOR_ANTAGONIST,propertyService, unitExpProp);
//		createProperty(DevQsarConstants.ANDROGEN_RECEPTOR_BINDING,propertyService, unitExpProp);
//		createProperty(DevQsarConstants.ANDROGEN_RECEPTOR_AGONIST,propertyService, unitExpProp);
//		createProperty(DevQsarConstants.ANDROGEN_RECEPTOR_ANTAGONIST,propertyService, unitExpProp);
		
		
	}

	private void createProperty(String propertyName, PropertyService propertyService, ExpPropUnit unitExpProp) {
		Property property=propertyService.findByName(propertyName);
		ExpPropProperty propertyExpProp=loader.getProperty(property.getName(), property.getDescription());
		loader.addPropertyAcceptableUnit(unitExpProp, propertyExpProp);
		System.out.println(property.getName()+"\t"+unitExpProp.getName());

	}
	
	
	/**
	 * Creates chemreg file for all Opera source chemicals
	 * 
	 */
	private void writeChemRegImportFileOPERA(String source) {

//		select distinct pv.fk_source_chemical_id  from exp_prop.property_values pv
//        join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
//        where pv.created_by='tmarti02' and sc.source_dtxrid is null

		String filePath="data\\dev_qsar\\output\\new chemreg lists\\"+source+".txt";
		
		System.out.println(filePath);

		List<SourceChemical>listSC=new ArrayList<>();

		for (String key:loader.sourceChemicalMap.keySet()) {
			SourceChemical sc=loader.sourceChemicalMap.get(key);
			if(sc.getPublicSource()==null) continue;
			if(!sc.getPublicSource().getName().equals(source)) continue;
			listSC.add(sc);
//			System.out.println(sc.getKey());
		}
		DsstoxMapper.writeChemRegImportFile(listSC, filePath);
	}
	
	private void writeChemRegImportFileChemProp() {
		
//		select distinct pv.fk_source_chemical_id  from exp_prop.property_values pv
//      join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
//      where pv.created_by='tmarti02' and sc.source_dtxrid is not null


		String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
		String filePath=mainFolder+"data\\experimental\\OPERA\\ChemRegImportChemProp.txt";
		
		System.out.println(filePath);

		List<SourceChemical>listSC=new ArrayList<>();

		for (String key:loader.sourceChemicalMap.keySet()) {
			SourceChemical sc=loader.sourceChemicalMap.get(key);
			
//			if(sc.getPublicSource()==null) continue;
//			if(!sc.getPublicSource().getName().equals("OPERA")) continue;

			if(!sc.getCreatedBy().equals("tmarti02")) continue;
			if(sc.getSourceDtxrid()==null) continue;

			listSC.add(sc);
			
			System.out.println(sc.getKey());
		}
		
		DsstoxMapper.writeChemRegImportFile(listSC, filePath);
	}

	public static void bob() {
		System.out.println("hello");
		
		try {
			XSSFWorkbook wb = new XSSFWorkbook("compare to SDE.xlsx");
			
			System.out.println(wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	void loadPropertyValues() {
		String sourceName="OPERA2.9";

//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.BOILING_POINT,ExperimentalRecordLoader.typePhyschem,sourceName);
//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.MELTING_POINT,ExperimentalRecordLoader.typePhyschem,sourceName);
//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.HENRYS_LAW_CONSTANT,ExperimentalRecordLoader.typePhyschem,sourceName);
//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.LOG_KOA,ExperimentalRecordLoader.typePhyschem,sourceName);
//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.LOG_KOW,ExperimentalRecordLoader.typePhyschem,sourceName);
//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.VAPOR_PRESSURE,ExperimentalRecordLoader.typePhyschem,sourceName);
//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.WATER_SOLUBILITY,ExperimentalRecordLoader.typePhyschem,sourceName);
//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.PKA_B,ExperimentalRecordLoader.typePhyschem,sourceName);
//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.PKA_A,ExperimentalRecordLoader.typePhyschem,sourceName);
//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.BCF,ExperimentalRecordLoader.typePhyschem,sourceName);
//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.OH,ExperimentalRecordLoader.typePhyschem,sourceName);//has temperature
//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.BIODEG_HL_HC,ExperimentalRecordLoader.typePhyschem,sourceName);
//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.RBIODEG,ExperimentalRecordLoader.typePhyschem,sourceName);
//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.KOC,ExperimentalRecordLoader.typePhyschem,sourceName);
//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.KmHL,ExperimentalRecordLoader.typePhyschem,sourceName);
//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.ORAL_RAT_LD50,ExperimentalRecordLoader.typePhyschem,sourceName);
//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.ESTROGEN_RECEPTOR_BINDING,ExperimentalRecordLoader.typePhyschem,sourceName);
		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.ESTROGEN_RECEPTOR_AGONIST,ExperimentalRecordLoader.typePhyschem,sourceName);
		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.ESTROGEN_RECEPTOR_ANTAGONIST,ExperimentalRecordLoader.typePhyschem,sourceName);
		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.ANDROGEN_RECEPTOR_BINDING,ExperimentalRecordLoader.typePhyschem,sourceName);
		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.ANDROGEN_RECEPTOR_AGONIST,ExperimentalRecordLoader.typePhyschem,sourceName);
		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.ANDROGEN_RECEPTOR_ANTAGONIST,ExperimentalRecordLoader.typePhyschem,sourceName);
//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.CLINT,ExperimentalRecordLoader.typePhyschem,sourceName);//log
//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.FUB,ExperimentalRecordLoader.typePhyschem,sourceName);
//		loadPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.CACO2,ExperimentalRecordLoader.typePhyschem,sourceName);
		
	}
	
	void addLiteratureSourcesOPERA2_8 () {
//		addLiteratureSources(DevQsarConstants.VAPOR_PRESSURE,true);
//		addLiteratureSources(DevQsarConstants.WATER_SOLUBILITY,true);
//		addLiteratureSources(DevQsarConstants.HENRYS_LAW_CONSTANT,true);
//		addLiteratureSources(DevQsarConstants.LOG_KOW,true);		
//		addLiteratureSources(DevQsarConstants.LOG_KOA,false);
	}
	
	void deletePropertyValues() {
//		deletePropertyValues("LogBCF");
//		deletePropertyValues(DevQsarConstants.LOG_HALF_LIFE);
//		deletePropertyValues(DevQsarConstants.LOG_OH);
//		deletePropertyValues(DevQsarConstants.LOG_KM_HL);
//		deletePropertyValues(DevQsarConstants.KmHL);
//		deletePropertyValues(DevQsarConstants.BIODEG_HL_HC);
//		deletePropertyValues(DevQsarConstants.OH);
//		deletePropertyValues(DevQsarConstants.BCF);
//		deletePropertyValues(DevQsarConstants.KOC);
//		deletePropertyValues(DevQsarConstants.FUB);
//		deletePropertyValues(DevQsarConstants.ORAL_RAT_LD50);
	}
	
	public static void main(String[] args) {

//		bob();
		boolean loadSCmap=true;
//		boolean loadSCmap=false;
		OperaLoader o=new OperaLoader(loadSCmap);
		
//		o.reloadFailedPropertyValuesFromOPERA_ExperimentalRecordsFile(DevQsarConstants.CACO2,ExperimentalRecordLoader.typePhyschem,"OPERA2.9");
		
//		o.createExpPropProperties();
		o.loadPropertyValues();
		
		
//		o.writeChemRegImportFileOPERA("OPERA2.9");
//		o.writeChemRegImportFileChemProp();
		
		
//		ExpPropProperty prop=o.loader.propertiesMap.get("LogHalfLife");
//		o.loader.expPropPropertyService.delete(prop);


	}
}
