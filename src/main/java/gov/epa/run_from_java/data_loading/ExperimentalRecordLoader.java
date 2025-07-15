package gov.epa.run_from_java.data_loading;

import java.io.File;
import java.sql.Connection;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;
import gov.epa.databases.dev_qsar.exp_prop.entity.Parameter;
import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.util.JSONUtilities;

public class ExperimentalRecordLoader {

	Loaders loaders=new Loaders();
	Delete delete=new Delete();
	PropertyValueCreator pvc;
	ParameterValueCreator paramValCreator;

	
	public static final String typePhyschem="Physchem";
	public static final String typeTox="Tox";
	public static final String typeOther="Other";

	public static Gson gson =  new GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.serializeSpecialFloatingPointValues()
			.create();
	
	boolean debug=false;
	String lanId;

	ExperimentalRecordLoader (String lanId) {
		this.lanId=lanId;		
		pvc=new PropertyValueCreator(lanId,debug);
		paramValCreator=new ParameterValueCreator(pvc);

	}
	
	
	class Delete {

		void deleteByPublicSourceName() {
		
		
			Connection conn=SqlUtilities.getConnectionPostgres();
		
			//		String sourceName=DevQsarConstants.sourceNameOChem_2024_04_03;
//			String sourceName=DevQsarConstants.sourceNamePubChem_2024_03_20;
//			String sourceName=DevQsarConstants.sourceNamePubChem_2024_11_27;
//			String sourceName=DevQsarConstants.sourceNameOPERA28;
			String sourceName=DevQsarConstants.sourceNameArnot2006;
		
			String sqlSourceName="select id from exp_prop.public_sources where name='"+sourceName+"';";
			int publicSourceId=Integer.parseInt(SqlUtilities.runSQL(conn, sqlSourceName));
		
			System.out.println(sourceName+"\t"+publicSourceId);
		
			//Parameter values cascade on deleting the property values- so shouldnt need to delete separately:
			//		String sqlParameters="delete from exp_prop.parameter_values pv2 using exp_prop.property_values pv\n"+
			//				"where pv2.fk_property_value_id=pv.id and pv.fk_public_source_id="+publicSourceId;
			//		SqlUtilities.runSQLUpdate(conn, sqlParameters);//NOTE: not needed if foreign key to property value is set to cascade on delete
		
			String sqlPropertyValues="delete from exp_prop.property_values pv where fk_public_source_id="+publicSourceId+";";
			System.out.println(sqlPropertyValues);
			SqlUtilities.runSQLUpdate(conn, sqlPropertyValues);
		
			//		String sqlSourceCHemicals="delete from exp_prop.source_chemicals sc where sc.fk_public_source_id="+publicSourceId+" and created_by='tmarti02'";
			//		SqlUtilities.runSQLUpdate(conn, sqlSourceCHemicals);
		
			//TODO delete literature sources associated with public source if no longer need
			//TODO delete public source original sources associated with public source if no longer need
		}
		
		void deleteByPublicSourceNameAndProperty() {
			
			
			Connection conn=SqlUtilities.getConnectionPostgres();
		
			//		String sourceName=DevQsarConstants.sourceNameOChem_2024_04_03;
//			String sourceName=DevQsarConstants.sourceNamePubChem_2024_03_20;
//			String sourceName=DevQsarConstants.sourceNamePubChem_2024_11_27;

//			String sourceName=DevQsarConstants.sourceNameOPERA28;
//			String propertyName=DevQsarConstants.ESTROGEN_RECEPTOR_BINDING;

//			String sourceName="Burkhard";
			String sourceName="ECOTOX_2024_12_12";
//			String propertyName=DevQsarConstants.BCF;
			String propertyName=DevQsarConstants.ACUTE_AQUATIC_TOXICITY;
			
			
			String sqlProperty="select id from exp_prop.properties where name='"+propertyName+"';";
			int propertyId=Integer.parseInt(SqlUtilities.runSQL(conn, sqlProperty));
			System.out.println(propertyId+"\t"+propertyName);

			String sqlSourceName="select id from exp_prop.public_sources where name='"+sourceName+"';";
			int publicSourceId=Integer.parseInt(SqlUtilities.runSQL(conn, sqlSourceName));
			System.out.println(publicSourceId+"\t"+sourceName);
		
			//Parameter values cascade on deleting the property values- so shouldnt need to delete separately:
			//		String sqlParameters="delete from exp_prop.parameter_values pv2 using exp_prop.property_values pv\n"+
			//				"where pv2.fk_property_value_id=pv.id and pv.fk_public_source_id="+publicSourceId;
			//		SqlUtilities.runSQLUpdate(conn, sqlParameters);//NOTE: not needed if foreign key to property value is set to cascade on delete
		
			
			String sqlPropertyValues="delete from exp_prop.property_values pv where fk_public_source_id="+publicSourceId+" and fk_property_id="+propertyId+";";
			System.out.println(sqlPropertyValues);
			SqlUtilities.runSQLUpdate(conn, sqlPropertyValues);
		
			//		String sqlSourceCHemicals="delete from exp_prop.source_chemicals sc where sc.fk_public_source_id="+publicSourceId+" and created_by='tmarti02'";
			//		String sqlLiteratureSources="delete from exp_prop.literature_sources where created_by='tmarti02' and id>3378";
			//		String sqlPublicSources="delete from exp_prop.public_sources where created_by='tmarti02' and id>104";
			//		SqlUtilities.runSQLUpdate(conn, sqlSourceCHemicals);
			//		SqlUtilities.runSQLUpdate(conn, sqlLiteratureSources);
			//		SqlUtilities.runSQLUpdate(conn, sqlPublicSources);
		
		}


		void deleteEcotoxData() {
		
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
		
		
		
		
		void deleteExpPropData() {
		
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
		
		
		void deleteSander() {
		
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
		
		
		
	}
	
	
	class Loaders {

		private void loadOChem() {
		
			debug=true;//prints values loaded from database like property
			boolean createDBEntries=true;
			String type=typePhyschem;
		
		
			//		String propertyName=DevQsarConstants.HENRYS_LAW_CONSTANT;
			//		String propertyName=DevQsarConstants.MELTING_POINT;
			//		String propertyName=DevQsarConstants.WATER_SOLUBILITY;
			String propertyName=null;
		
			String sourceName="OChem_2024_04_03";
			String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
		
		
			pvc.mapTables(sourceName);
		
			//Initial loading attempt:
			String folderPath=mainFolder+"data\\experimental\\"+sourceName;
			String substring=sourceName+" Experimental Records ";
			ExperimentalRecords records=ExperimentalRecords.getExperimentalRecords(propertyName, folderPath,substring);		
		
			//		String folderPath="data/dev_qsar/exp_prop/" + type + "/"+ sourceName + "/6-12-24";
			//		ExperimentalRecords records=getExperimentalRecords(propertyName, folderPath,"Experimental Records-Failed");
		
			records.getRecordsByProperty();
		
			if(true) return;
		
			records.printUniqueUnitsListInExperimentalRecords();
			System.out.println("experimentalRecords.size()="+records.size());
		
		
			//		createLiteratureSources(records);
			load(records, type, createDBEntries,sourceName,propertyName);
		
		}

		private void loadPubChem() {
		
			debug=true;//prints values loaded from database like property
		
			boolean createDBEntries=true;
		
			String type=typePhyschem;
		
			//		String sourceName=DevQsarConstants.sourceNamePubChem_2024_03_20;
			String sourceName=DevQsarConstants.sourceNamePubChem_2024_11_27;
			pvc.mapTables(sourceName);
		
			String propertyName=null;//load all properties
		
			String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
		
			//Initial loading attempt:
			String folderPath=mainFolder+"data\\experimental\\"+sourceName;
			String substring=sourceName+" Experimental Records";
			ExperimentalRecords records=ExperimentalRecords.getExperimentalRecords(propertyName, folderPath,substring);		
		
			//		for(ExperimentalRecord er:records) {
			////			if(er.property_value_units_final==null && er.property_value_qualitative==null) {
			//			if(er.property_value_units_final==null) {
			//				System.out.println(er.toJson(er.outputFieldNames));
			//			}
			//		}
		
			//		int countAlreadyLoaded=56790;
			//		for (int i=1;i<=countAlreadyLoaded;i++) records.remove(0);		
			//		System.out.println("After removing already ran: "+records.size());
		
		
			//		if(true)return;
			//		String folderPath="data/dev_qsar/exp_prop/" + type + "/"+ sourceName + "/6-12-24";
			//		ExperimentalRecords records=getExperimentalRecords(propertyName, folderPath,"Experimental Records-Failed");
			//		if(true) return;
		
			records.printUniqueUnitsListInExperimentalRecords();
			System.out.println("experimentalRecords.size()="+records.size());
		
			records.printPropertiesInExperimentalRecords();
		
			long t1=System.currentTimeMillis();
		
			loadBatchWise(records, type, createDBEntries,sourceName,propertyName);
		
			long t2=System.currentTimeMillis();
		
			System.out.println("Load time="+(t2-t1)/1000.0+" seconds");
		
		
		}

		private void loadRatLC50_CoMPAIT() {
		
			debug=true;//prints values loaded from database like property
		
			boolean createDBEntries=true;
		
			String sourceName="CoMPAIT";
		
			String propertyName=DevQsarConstants.FOUR_HOUR_INHALATION_RAT_LC50;
		
			String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
			//		String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+"_"+propertyName+" Experimental Records.json";
			String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+" Experimental Records.json";
		
			File jsonFile=new File(filePath);
		
			System.out.println(filePath+"\t"+jsonFile.exists());
		
			pvc.mapTables(sourceName);
		
		
			//**********************************************************************************************
			//First create the property
			ExpPropProperty property=pvc.getProperty(propertyName, "Inhalation concentration that kills half of rats in 4 hours");
		
			//**********************************************************************************************
			//Add entries for properties_acceptable_units:
		
			//Note: first time you run this property, uncomment out the following lines:
			pvc.addPropertyAcceptableUnit(pvc.getUnit("LOG_MG_L","log10(mg/L)"), property);
			pvc.addPropertyAcceptableUnit(pvc.getUnit("LOG_PPM","log10(ppm)"), property);
		
			pvc.addPropertyAcceptableParameter(pvc.getParameter("qsar_ready_smiles","qsar_ready_smiles"),property);		
			pvc.addParameterAcceptableUnit(pvc.getUnit("TEXT",""), pvc.getParameter("qsar_ready_smiles","qsar_ready_smiles"));		
		
			//*******************************************************************************************************
		
			ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, gson);
			//		printUniqueUnitsListInExperimentalRecords(records);
			System.out.println("experimentalRecords.size()="+records.size());
		
			load(records, typeOther, createDBEntries,sourceName,propertyName);
		
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
		
		
		
		private void loadBCFDataBurkhard() {
		
			debug=true;//prints values loaded from database like property
		
			boolean createDBEntries=true;
		
			String sourceName="Burkhard";
//			String propertyName=DevQsarConstants.BCF;
			String propertyName=DevQsarConstants.BAF;
			
			String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
			String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+propertyName+"\\"+sourceName+" Experimental Records.json";
			File jsonFile=new File(filePath);
			System.out.println(filePath+"\t"+jsonFile.exists());
		
			pvc.mapTables(sourceName);
		
			//**********************************************************************************************
			//First create the property
			ExpPropProperty property=pvc.getProperty(propertyName, DevQsarConstants.BCF);
		
			//**********************************************************************************************
			//Add entries for properties_acceptable_units:
		
			//Note: first time you run this property, uncomment out the following lines:
			pvc.addPropertyAcceptableUnit(pvc.getUnit("L_KG",DevQsarConstants.L_KG), property);
		
			List<Parameter>params=new ArrayList<>();
			params.add(pvc.getParameter("Media type","Media type where exposure occurs (e.g. fresh water)"));
			params.add(pvc.getParameter("Species latin","Latin name of tested organism (e.g. Pimephales promelas)"));
			params.add(pvc.getParameter("Species common","Common name of tested organism (e.g. Fathead minnow)"));
			params.add(pvc.getParameter("Response site","Part of organism tested for the chemical"));
			params.add(pvc.getParameter("Test location","Where the test was performed (e.g. laboratory"));
			params.add(pvc.getParameter("Exposure concentration","Water concentration"));
			params.add(pvc.getParameter("Measurement method","Method used to measure the property"));
			params.add(pvc.getParameter("Reliability","Reliability of the experimental data point"));
		
			ExpPropUnit unitText=pvc.getUnit("TEXT","");
			for(Parameter param:params) {
				pvc.addParameterAcceptableUnit(unitText, param);	
			}
		
			//*******************************************************************************************************
		
			ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, gson);
			records.printUniqueUnitsListInExperimentalRecords();
			System.out.println("experimentalRecords.size()="+records.size());
		
			loadBatchWise(records, typeOther, createDBEntries,sourceName,propertyName);
		
		}
		
		
		private void loadBCFDataNITE() {
			
			debug=true;//prints values loaded from database like property
		
			boolean createDBEntries=true;
		
			String sourceName="QSAR_Toolbox";
			String sourceNameOriginal="BCF NITE";
			String propertyName=DevQsarConstants.BCF;
			
			String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
			String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceNameOriginal+"\\"+propertyName+"\\"+sourceName+" Experimental Records.json";
			File jsonFile=new File(filePath);
			System.out.println(filePath+"\t"+jsonFile.exists());
			
			if(!jsonFile.exists())return;
		
			pvc.mapTables(sourceName);
		
			//**********************************************************************************************
			//First create the property
			ExpPropProperty property=pvc.getProperty(propertyName, DevQsarConstants.BCF);

			List<Parameter>params=new ArrayList<>();
			params.add(pvc.getParameter("Test guideline","Test guideline used"));
					
//			List<String>unitsParameterWaterConcentrationNew=Arrays.asList("Bq/L","Ci/L","cpm/L","ueq/L","M");
//			pvc.updateParameter("Water concentration", unitsParameterWaterConcentrationNew);
			
			ExpPropUnit unitText=pvc.getUnit("TEXT","");
			for(Parameter param:params) {
				pvc.addParameterAcceptableUnit(unitText, param);	
			}

			//*******************************************************************************************************
		
			ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, gson);
			records.printUniqueUnitsListInExperimentalRecords();
			System.out.println("experimentalRecords.size()="+records.size());
		
			loadBatchWise(records, typeOther, createDBEntries,sourceName,propertyName);
		
		}
		
		private void loadBCFArnot() {
			
			debug=true;//prints values loaded from database like property
		
			boolean createDBEntries=true;
			
			String type=typeOther;
			String sourceName="Arnot 2006";
			pvc.mapTables(sourceName);
//			String propertyName=DevQsarConstants.BCF;
			String propertyName=DevQsarConstants.BAF;
			
			String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
			String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+propertyName+"\\"+sourceName+" Experimental Records.json";
			File jsonFile=new File(filePath);
		
			System.out.println(filePath+"\t"+jsonFile.exists());
			
			if(!jsonFile.exists())return;
		
			//**********************************************************************************************
			//First create the property
			ExpPropProperty property=pvc.getProperty(propertyName, DevQsarConstants.BCF);
		
			//**********************************************************************************************
			//Add entries for properties_acceptable_units:
		
			//Note: first time you run this property, uncomment out the following lines:
			pvc.addPropertyAcceptableUnit(pvc.getUnit("L_KG",DevQsarConstants.L_KG), property);
		
		
			//*******************************************************************************************************
		
			ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, gson);
			records.printUniqueUnitsListInExperimentalRecords();
			System.out.println("experimentalRecords.size()="+records.size());
			
			records.printPropertiesInExperimentalRecords();

			pvc.createTextParameter("Criterion 1- Water Concentration",
					"Whether or not water concentration has been measured on a scale from 1 to 3");
			pvc.createTextParameter("Criterion 2- Radiolabel",
					"Whether or not radiolabel based concentrations have been corrected for metabolites on a scale from 1 to 3");
			pvc.createTextParameter("Criterion 3- Aqueous Solubility",
					"Whether or not the water concentration exceeds the water solubility on a scale from 1 to 3");
			pvc.createTextParameter("Criterion 4- Exposure Duration",
					"Whether or not the exposure duration is long enough to achieve steady state on a scale from 1 to 3");
			pvc.createTextParameter("Criterion 5- Tissue Analyzed",
					"Whether or not the response_site is whole body on a scale from 1 to 3");
			pvc.createTextParameter("Criterion 6- Other Major Source",
					"Whether or not another criterion has not been met on a scale from 1 to 3");
			
			pvc.createTextParameter("Overall Score","Whether or not a data record is acceptable on a scale from 1 to 3");
			pvc.createTextParameter("Species supercategory","Type of organism (e.g. fish)");
			pvc.createTextParameter("Organism classification","Organism classification (e.g. vertebrate)");

			pvc.getUnit("CI_MOL", "Ci/mol");
			pvc.getUnit("BQ_ML", "Bq/mL");
			pvc.getUnit("DPM_ML", "dpm/mL");
			
			
			
			HashSet<String>abbrevsWaterConc=getUnitAbbreviations(records, "Water concentration");
			pvc.createParameter("Water concentration", "Concentration in water",abbrevsWaterConc);

			HashSet<String>abbrevsExposureDuration=getUnitAbbreviations(records, "Exposure duration");
			pvc.createParameter("Exposure duration", "Time exposed to chemical",abbrevsExposureDuration);

			//Old parameter:
//			pvc.createTextParameter("Exposure Duration (in days or Lifetime)","Exposure Duration (in days or Lifetime)");
			
			loadBatchWise(records, type, createDBEntries,sourceName,propertyName);
		
		}
		
		
		HashSet<String>getUnitAbbreviations(ExperimentalRecords records,String parameterName) {
			
			HashSet<String>abbrevs=new HashSet<>();
			
			for (ExperimentalRecord er:records) {
				
				if(er.parameter_values==null)continue;
				
				
				for (ParameterValue pv:er.parameter_values) {
					if(!pv.getParameter().getName().equals(parameterName))continue;
					abbrevs.add(pv.getUnit().getAbbreviation());
				}
			}
			
//			for (String abbrev:abbrevs) {
//				System.out.println(parameterName+"\t"+abbrev);
//			}
			
			return abbrevs;
		}
		
		
		private void loadAcuteAquaticToxicityDataEcotox() {
		
			debug=true;//prints values loaded from database like property
		
			boolean createDBEntries=true;
		
//			String sourceName="ECOTOX_2023_12_14";
			String sourceName="ECOTOX_2024_12_12";
			
			String type=typeOther;
			
			pvc.mapTables(sourceName);
		
			//		String propertyName=DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50;
			//		String propertyName=DevQsarConstants.NINETY_SIX_HOUR_BLUEGILL_LC50;
			//		String propertyName=DevQsarConstants.NINETY_SIX_HOUR_RAINBOW_TROUT_LC50;
			//		String propertyName=DevQsarConstants.FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50;
			//		String propertyName=DevQsarConstants.NINETY_SIX_HOUR_BLUEGILL_LC50;
//			String propertyName=DevQsarConstants.HENRYS_LAW_CONSTANT;
			String propertyName=DevQsarConstants.ACUTE_AQUATIC_TOXICITY;
			
			String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";

//			String mainFolder="C:\\Users\\lbatts\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\data_gathering\\";
		
//			String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+"_"+propertyName+" Experimental Records.json";
//			String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+propertyName+"\\"+sourceName+" Experimental Records.json";
//			File jsonFile=new File(filePath);
		
//			System.out.println(filePath+"\t"+jsonFile.exists());

		
			//**********************************************************************************************
			//First create the property
			ExpPropProperty property=pvc.getProperty(propertyName, "concentration that kills half of fathead minnow in 96 hours");
		
			//**********************************************************************************************
			//Add entries for properties_acceptable_units:
		
			//Note: first time you run this property, uncomment out the following lines:
			pvc.addPropertyAcceptableUnit(pvc.getUnit("G_L","g/L"), property);
			pvc.addPropertyAcceptableUnit(pvc.getUnit("MOLAR","M"), property);
		
			pvc.addPropertyAcceptableParameter(pvc.getParameter("test_id","test_id field in tests table in Ecotox database"),property);		
			pvc.addPropertyAcceptableParameter(pvc.getParameter("exposure_type","Type of exposure (S=static, F=flowthrough, R=renewal, etc)"),property);
			pvc.addPropertyAcceptableParameter(pvc.getParameter("chem_analysis_method","Analysis method for water concentration (M=measured, U=unmeasured etc)"),property);		
			pvc.addPropertyAcceptableParameter(pvc.getParameter("concentration_type","How concentration is reported"),property);
			pvc.addPropertyAcceptableParameter(pvc.getParameter("Species type","Type of species (e.g. standard, invasive, etc.)"),property);
			pvc.addPropertyAcceptableParameter(pvc.getParameter("test_type","Type of test (e.g. LC50, EC50)"),property);
			pvc.addPropertyAcceptableParameter(pvc.getParameter("Effect","Toxicity effect (e.g. mortality)"),property);
			pvc.addPropertyAcceptableParameter(pvc.getParameter("result_id","result_id field in results table in Ecotox database"),property);
			pvc.addPropertyAcceptableParameter(pvc.getParameter("Observation duration","Duration for the given toxicity value"),property);
			

			pvc.addParameterAcceptableUnit(pvc.getUnit("TEXT",""), pvc.getParameter("test_id","test_id field in tests table in Ecotox database"));
			pvc.addParameterAcceptableUnit(pvc.getUnit("TEXT",""), pvc.getParameter("exposure_type","Type of exposure (S=static, F=flow-through, R=renewal, etc)"));		
			pvc.addParameterAcceptableUnit(pvc.getUnit("TEXT",""), pvc.getParameter("chem_analysis_method","Analysis method for water concentration (M=measured, U=unmeasured etc)"));
			pvc.addParameterAcceptableUnit(pvc.getUnit("TEXT",""), pvc.getParameter("concentration_type","How concentration is reported"));
			
			pvc.addParameterAcceptableUnit(pvc.getUnit("DAYS",""), pvc.getParameter("Observation duration","Duration for the given toxicity value"));
			
		
			//		addPropertyInCategory(getPropertyCategory("Acute aquatic toxicity", "Acute aquatic toxicity"), property);
		
			//*******************************************************************************************************
		
			ExperimentalRecords records=ExperimentalRecords.getExperimentalRecords(sourceName,propertyName);
			//		printUniqueUnitsListInExperimentalRecords(records);
			System.out.println("experimentalRecords.size()="+records.size());
		
//			if(true)return;
			
//			load(records, typeOther, createDBEntries,sourceName,propertyName);
			loadBatchWise(records, type, createDBEntries, sourceName, propertyName);
		
		}
		
		private void loadAcuteAquaticToxicityDataQsarToolbox() {
			
			debug=true;//prints values loaded from database like property
		
			boolean createDBEntries=true;
		
			String sourceName="QSAR_Toolbox";
			
			String type=typeOther;
			
			pvc.mapTables(sourceName);
		
			String propertyName=DevQsarConstants.ACUTE_AQUATIC_TOXICITY;
			
			String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";

			//**********************************************************************************************
			//First create the property
			ExpPropProperty property=pvc.getProperty(propertyName, "Water concentration that kills half of fish");
		
			//**********************************************************************************************
			//Add entries for properties_acceptable_units:
		
			//Note: first time you run this property, uncomment out the following lines:
			pvc.addPropertyAcceptableUnit(pvc.getUnit("G_L","g/L"), property);
			pvc.addPropertyAcceptableUnit(pvc.getUnit("MOLAR","M"), property);
		

			pvc.addPropertyAcceptableParameter(pvc.getParameter("exposure_type","Type of exposure (S=static, F=flowthrough, R=renewal, etc)"),property);
			pvc.addPropertyAcceptableParameter(pvc.getParameter("Observation duration","Duration for the given toxicity value"),property);

			pvc.addParameterAcceptableUnit(pvc.getUnit("TEXT",""), pvc.getParameter("test_id","test_id field in tests table in Ecotox database"));
			pvc.addParameterAcceptableUnit(pvc.getUnit("TEXT",""), pvc.getParameter("exposure_type","Type of exposure (S=static, F=flow-through, R=renewal, etc)"));		
			pvc.addParameterAcceptableUnit(pvc.getUnit("DAYS",""), pvc.getParameter("Observation duration","Duration for the given toxicity value"));
			
		
			//		addPropertyInCategory(getPropertyCategory("Acute aquatic toxicity", "Acute aquatic toxicity"), property);
		
			//*******************************************************************************************************
		
			String subfolder="Fish tox ECHA\\"+propertyName;
			
			ExperimentalRecords records=ExperimentalRecords.getExperimentalRecords(sourceName,subfolder);
			//		printUniqueUnitsListInExperimentalRecords(records);
			System.out.println("experimentalRecords.size()="+records.size());
		
//			if(true)return;
			
//			load(records, typeOther, createDBEntries,sourceName,propertyName);
			loadBatchWise(records, type, createDBEntries, sourceName, propertyName);
		
		}

		private void loadAcuteAquaticToxicityDataToxVal() {
		
			debug=true;//prints values loaded from database like property
		
			boolean createDBEntries=true;
		
		
			String sourceName="ToxVal_V93";
			String propertyName=DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50;
		
			String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\ghs-data-gathering\\";
			String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+" "+propertyName+" Experimental Records.json";
		
			//**********************************************************************************************
			//First create the property
			ExpPropProperty property=pvc.getProperty(propertyName, "concentration that kills half of fathead minnow in 96 hours");
		
			//**********************************************************************************************
			//Add entries for properties_acceptable_units:
			pvc.addPropertyAcceptableUnit(pvc.getUnit("G_L","g/L"), property);
			pvc.addPropertyAcceptableUnit(pvc.getUnit("MOLAR","M"), property);
		
//			pvc.addPropertyAcceptableParameter(pvc.getParameter("toxval_id","toxval_id field in toxval table in ToxVal database"),property);
			//		pvc.addPropertyAcceptableParameter(pvc.getParameter("Lifestage","lifestage field in toxval table in ToxVal database"),property);
			//		pvc.addPropertyAcceptableParameter(pvc.getParameter("Exposure route","exposure route field in toxval table in ToxVal database"),property);
			//		pvc.addPropertyAcceptableParameter(pvc.getParameter("Reliability","Reliability"),property);
		
			//		addPropertyInCategory(getPropertyCategory("Acute aquatic toxicity", "Acute aquatic toxicity"), property);
		
			//There are no units for parameters for acute aquatic tox but here is example:
			pvc.addParameterAcceptableUnit(pvc.getUnit("C","C"), pvc.getParameter("Temperature","Temperature"));		
		
			//*******************************************************************************************************
		
			ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, gson);
			//		printUniqueUnitsListInExperimentalRecords(records);
			System.out.println("experimentalRecords.size()="+records.size());
		
			loaders.load(records, typeOther, createDBEntries,sourceName,propertyName);
		
		}

		private void loadPropertyValuesFromThreeM_ExperimentalRecordsFile() {
		
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
		
			pvc.mapTables(sourceName);
		
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

		private void loadPubChem(ExperimentalRecords experimentalRecords, String sourceName, String propertyName,boolean createDBEntries) {
		
			debug=true;//prints values loaded from database like property
		
		
			String type=typePhyschem;
		
			ExperimentalRecords records=new ExperimentalRecords();
		
			for (ExperimentalRecord er:experimentalRecords) {
				if(er.property_name.equals(propertyName)) records.add(er);
			}
		
			//		printUniqueUnitsListInExperimentalRecords(records);
		
			//		for(ExperimentalRecord er:records) {
			////	if(er.property_value_units_final==null && er.property_value_qualitative==null) {
			//	if(er.property_value_units_final==null) {
			//		System.out.println(er.toJson(er.outputFieldNames));
			//	}
			//}
		
			long t1=System.currentTimeMillis();
		
			//		createLiteratureSources(records);
			//		loadAsBatch(records, type, createDBEntries,sourceName,propertyName);
			loadBatchWise(records, type, createDBEntries,sourceName,propertyName);
		
			long t2=System.currentTimeMillis();
		
			System.out.println("Load time="+(t2-t1)/1000.0+" seconds");
		
		}

		/**
		 * Loads specific list of properties for pubchem
		 * 
		 */
		private void loadPubchemMultiple() {
		
			boolean createDBEntries=true;
		
			String sourceName=DevQsarConstants.sourceNamePubChem_2024_03_20;
			pvc.mapTables(sourceName);
		
		
			//		Appearance, Autoignition temperature, Density, Flash point, Odor, Surface tension, Vapor density, viscosity
		
			List<String> propertyNames = Arrays.asList(DevQsarConstants.HENRYS_LAW_CONSTANT,DevQsarConstants.LOG_KOW, 
					DevQsarConstants.MELTING_POINT,	DevQsarConstants.BOILING_POINT, 
					DevQsarConstants.VAPOR_PRESSURE,DevQsarConstants.WATER_SOLUBILITY);
		
			String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
		
			//Initial loading attempt:
			String folderPath=mainFolder+"data\\experimental\\"+sourceName;
			String substring=sourceName+" Experimental Records ";
			ExperimentalRecords records=ExperimentalRecords.getExperimentalRecords(null, folderPath,substring);		
		
			System.out.println("experimentalRecords.size()="+records.size());
		
			//		printPropertiesInExperimentalRecords(records);
		
			for (String propertyName:propertyNames) {//load one property at a time:
				loadPubChem(records, sourceName, propertyName, createDBEntries);
			}
		}

		private void loadBCFDataEcotox() {
		
			debug=true;//prints values loaded from database like property
		
			boolean createDBEntries=true;
		
//			String sourceName="ECOTOX_2023_12_14";
			String sourceName="ECOTOX_2024_12_12";
						
//			String propertyName=DevQsarConstants.BCF;
			String propertyName=DevQsarConstants.BAF;
			
			String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
			String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+propertyName+"\\"+sourceName+" Experimental Records.json";
			File jsonFile=new File(filePath);
		
			System.out.println(filePath+"\t"+jsonFile.exists());
		
		
			pvc.mapTables(sourceName);
		
			//**********************************************************************************************
			//First create the property
			ExpPropProperty property=pvc.getProperty(propertyName, DevQsarConstants.BCF);
		
			//**********************************************************************************************
			//Add entries for properties_acceptable_units:
		
			//Note: first time you run this property, uncomment out the following lines:
			pvc.addPropertyAcceptableUnit(pvc.getUnit("L_KG",DevQsarConstants.L_KG), property);
		
			List<Parameter>params=new ArrayList<>();
			params.add(pvc.getParameter("test_id","test_id field in tests table in Ecotox database"));
			params.add(pvc.getParameter("exposure_type","Type of exposure (S=static, F=flowthrough, R=renewal, etc)"));
			params.add(pvc.getParameter("Media type","Media type where exposure occurs (e.g. fresh water)"));
			params.add(pvc.getParameter("Species latin","Latin name of tested organism (e.g. Pimephales promelas)"));
			params.add(pvc.getParameter("Species common","Common name of tested organism (e.g. Fathead minnow)"));
			params.add(pvc.getParameter("Response site","Part of organism tested for the chemical"));
			params.add(pvc.getParameter("Test location","Where the test was performed (e.g. laboratory"));
			params.add(pvc.getParameter("Exposure concentration","Water concentration"));
					
			pvc.getUnit("BQ_L", "Bq/L");
			pvc.getUnit("CI_L", "Ci/L");
			pvc.getUnit("CPM_L", "cpm/L");
			pvc.getUnit("UEQ_L", "ueq/L");
			
			List<String>unitsParameterWaterConcentrationNew=Arrays.asList("Bq/L","Ci/L","cpm/L","ueq/L","M");
			pvc.updateParameter("Water concentration", unitsParameterWaterConcentrationNew);
			
			ExpPropUnit unitText=pvc.getUnit("TEXT","");
			for(Parameter param:params) {
				pvc.addParameterAcceptableUnit(unitText, param);	
			}
		
			//*******************************************************************************************************
		
			ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, gson);
			records.printUniqueUnitsListInExperimentalRecords();
			System.out.println("experimentalRecords.size()="+records.size());
		
			loadBatchWise(records, typeOther, createDBEntries,sourceName,propertyName);
		
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
		
		
		
		private void loadRBIODEG_NITE_OPPT() {
		
			debug=true;//prints values loaded from database like property
		
			boolean createDBEntries=true;
		
			String sourceName="NITE_OPPT";
		
			pvc.mapTables(sourceName);
		
			String propertyName=DevQsarConstants.RBIODEG;
		
			String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
			String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+"_"+propertyName+" Experimental Records.json";
		
			File jsonFile=new File(filePath);
		
			System.out.println(filePath+"\t"+jsonFile.exists());
		
		
			//**********************************************************************************************
			//First create the property
			ExpPropProperty property=pvc.getProperty(propertyName, DevQsarConstants.RBIODEG);
		
			//**********************************************************************************************
			//Add entries for properties_acceptable_units:
		
			//Note: first time you run this property, uncomment out the following lines:
			pvc.addPropertyAcceptableUnit(pvc.getUnit("BINARY","Binary"), property);
		
			pvc.addPropertyAcceptableParameter(pvc.getParameter("set","Training (T) or Validation (V) set"),property);		
			pvc.addParameterAcceptableUnit(pvc.getUnit("TEXT",""), pvc.getParameter("set","Training (T) or Validation (V) set"));
		
			//*******************************************************************************************************
			ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, gson);
			//		printUniqueUnitsListInExperimentalRecords(records);
			System.out.println("experimentalRecords.size()="+records.size());
		
			loadBatchWise(records, typeOther, createDBEntries,sourceName,propertyName);
		
		}
		
		private void loadRBIODEG_RIFM() {
			
			debug=true;//prints values loaded from database like property
		
			boolean createDBEntries=true;
		
			String sourceName="RIFM";
		
			pvc.mapTables(sourceName);
		
			String propertyName=DevQsarConstants.RBIODEG;
		
			String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
			String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+" Experimental Records.json";
		
			File jsonFile=new File(filePath);
		
			System.out.println(filePath+"\t"+jsonFile.exists());
		
		
			//**********************************************************************************************
			//First create the property
			ExpPropProperty property=pvc.getProperty(propertyName, DevQsarConstants.RBIODEG);
		
			//**********************************************************************************************
			//Add entries for properties_acceptable_units:
		
			//Note: first time you run this property, uncomment out the following lines:
			pvc.addPropertyAcceptableUnit(pvc.getUnit("BINARY","Binary"), property);
		
			pvc.addPropertyAcceptableParameter(pvc.getParameter("Test guideline","Test guideline used"),property);
			pvc.addPropertyAcceptableParameter(pvc.getParameter("Observation duration","TODO"),property);
						
			
			//*******************************************************************************************************
			ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, gson);
			//		printUniqueUnitsListInExperimentalRecords(records);
			System.out.println("experimentalRecords.size()="+records.size());
		
			loadBatchWise(records, typeOther, createDBEntries,sourceName,propertyName);
		
		}

		private void loadSanderHLC() {
		
			debug=true;//prints values loaded from database like property
		
			boolean createDBEntries=true;
		
			String sourceName="Sander_v5";
			String propertyName=DevQsarConstants.HENRYS_LAW_CONSTANT;
			String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\";
			String filePath=mainFolder+"data\\experimental\\"+sourceName+"\\"+sourceName+" Experimental Records.json";
			File jsonFile=new File(filePath);
		
			System.out.println(filePath+"\t"+jsonFile.exists());
		
			pvc.mapTables(sourceName);
		
			//**********************************************************************************************
			//First create the property
			ExpPropProperty property=pvc.getProperty(propertyName, propertyName);
		
			//**********************************************************************************************
			//Acceptable units for property already in db
			//*******************************************************************************************************
		
			ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, gson);
			records.printUniqueUnitsListInExperimentalRecords();
			System.out.println("experimentalRecords.size()="+records.size());
		
			load(records, typePhyschem, createDBEntries,sourceName,propertyName);
		}

		private void loadToxCastTTR_Binding() {
		
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
		
			pvc.mapTables(sourceName);
		
			//**********************************************************************************************
			//First create the property
			ExpPropProperty property=pvc.getProperty(propertyName, "concentration that kills half of fathead minnow in 96 hours");
		
			//**********************************************************************************************
			//Add entries for properties_acceptable_units:
		
			//Note: first time you run this property, uncomment out the following lines:
			pvc.addPropertyAcceptableUnit(pvc.getUnit("DIMENSIONLESS","Dimensionless"), property);
		
			pvc.addPropertyAcceptableParameter(pvc.getParameter("maximum_concentration","Maximum concentration tested"),property);		
			pvc.addPropertyAcceptableParameter(pvc.getParameter("library","Experimental data library used"),property);
			pvc.addPropertyAcceptableParameter(pvc.getParameter("tested_in_concentration_response","Whether or not tested for concentration response"),property);		
			pvc.addPropertyAcceptableParameter(pvc.getParameter("dataset","Which dataset the chemical appears in"),property);
		
			pvc.addParameterAcceptableUnit(pvc.getUnit("MICRO_MOLAR","μM"), pvc.getParameter("maximum_concentration","Maximum concentration tested"));
			pvc.addParameterAcceptableUnit(pvc.getUnit("TEXT",""), pvc.getParameter("library","Experimental data library used"));		
			pvc.addParameterAcceptableUnit(pvc.getUnit("TEXT",""), pvc.getParameter("tested_in_concentration_response","Whether or not tested for concentration response"));
			pvc.addParameterAcceptableUnit(pvc.getUnit("TEXT",""), pvc.getParameter("dataset","Which dataset the chemical appears in"));
		
			//		addPropertyInCategory(getPropertyCategory("Acute aquatic toxicity", "Acute aquatic toxicity"), property);
		
			//*******************************************************************************************************
		
			ExperimentalRecords records=ExperimentalRecords.loadFromJson(filePath, gson);
			//		printUniqueUnitsListInExperimentalRecords(records);
			System.out.println("experimentalRecords.size()="+records.size());
		
			load(records, typeOther, createDBEntries,sourceName,propertyName);
		
		}

		/**
		 * Creates Public Source, Property, Units, LiteratureSource, PublicSourceOriginal, and SourceChemical
		 * and has id filled in
		 *  
		 * @param records
		 * @param type
		 * @param createDBEntries
		 * @param sourceName
		 * @param propertyName
		 */
		private void loadAuxTables(List<ExperimentalRecord> records, String type, String sourceName, String propertyName) {
		
			System.out.println("\nStart loading aux records, #records="+records.size());
		
			//Create public source, property, and units
			for (ExperimentalRecord rec:records) {
				pvc.getPublicSource(rec, true);
				pvc.getProperty(rec.property_name,null);//dont need batch insert since only a handful
				
				if(rec.property_value_units_final!=null) {
					String unitName=DevQsarConstants.getExpPropUnitName(rec.property_name, rec.property_value_units_final);
					pvc.getUnit(unitName,rec.property_value_units_final);
				}
			}
			
			
			pvc.createSourcesBatch(records);
			
//			if(true) return;
			
			pvc.createSourceChemicals(records);
		
		}

		public List<PropertyValue> load(List<ExperimentalRecord> records, String type, boolean createDBEntries,String sourceName, String propertyName) {
		
			System.out.println("\nStart loading "+propertyName+"\t"+records.size());
		
			ExperimentalRecords failedRecords = new ExperimentalRecords();
			ExperimentalRecords loadedRecords = new ExperimentalRecords();
			List<PropertyValue> loadedPropertyValues = new ArrayList<>();
		
			int countSuccess = 0;
			int countFailure = 0;
			int countTotal = 0;
		
			int counter=0;
		
		
			List<PropertyValue> propertyValues = new ArrayList<>();
		
		
			for (ExperimentalRecord rec:records) {
				counter++;
		
				//			if(counter<=71246) continue;
		
				if(counter%1000==0)
					System.out.println(counter);
		
				try {
					boolean success = false;
		
					long t1=System.currentTimeMillis();
					PropertyValue pv=pvc.createPropertyValue(rec, createDBEntries);
					long t2=System.currentTimeMillis();
		
					//				System.out.println("Time to create property value subobjects:"+(t2-t1)+" milliseconds");
		
		
					if (pv.getUnit()==null) {
						failedRecords.add(rec);
						rec.keep=false;
						rec.reason="Units not in database";
						//					logger.warn(rec.id_physchem + ": Loading failed");
						countFailure++;
						continue;
					}
		
					boolean paramsOk=paramValCreator.addParameters(type, rec, pv);

					if(!paramsOk) {
						failedRecords.add(rec);
						continue;
					}
		
					propertyValues.add(pv);
		
		
					if (createDBEntries) {
						success = pvc.postPropertyValue(pv);//TODO add batch insert instead	
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
		
		
			writeFailedRecords(type, sourceName, propertyName, failedRecords);
			writeLoadedRecords(type, createDBEntries, sourceName, propertyName, loadedRecords, loadedPropertyValues);
			return loadedPropertyValues;
		
		}

		/**
		 * This version uses loadAuxTables to create objects with id numbers before creating propertyValues
		 * 
		 * @param records
		 * @param type
		 * @param createDBEntries
		 * @param sourceName
		 * @param propertyName
		 * @return
		 */
		public void loadBatchWise(ExperimentalRecords records, String type, boolean createDBEntries,String sourceName, String propertyName) {
			
			System.out.println("\nEnter loadBatchwise "+propertyName+"\t"+records.size());
			
			HashSet<String>missingParameters=pvc.getMissingParameters(records);
			if(missingParameters.size()>0) {
				return;
			}
			HashSet<String>missingUnits=pvc.getMissingParameterUnits(records);
			if(missingUnits.size()>0) {
				return;
			}
			HashSet<String>missingAcceptableUnits=pvc.getMissingParameterAcceptableUnits(records);
			if(missingAcceptableUnits.size()>0) {
				return;
			}
			
			if(pvc.propertiesMap==null)						
				pvc.mapTables(sourceName);
			
			if(createDBEntries)
				loadAuxTables(records, type, sourceName, propertyName);

			
//			if(true)return;
			
			System.out.println("\nStart loading "+propertyName+"\t"+records.size());
		
			ExperimentalRecords failedRecords = new ExperimentalRecords();
			ExperimentalRecords loadedRecords = new ExperimentalRecords();
			ExperimentalRecords recs = new ExperimentalRecords();
			List<PropertyValue> loadedPropertyValues = new ArrayList<>();
			
			int countSuccess = 0;
			int countFailure = 0;
			int countTotal = 0;
			int counter=0;
		
			List<PropertyValue> propertyValues = new ArrayList<>();
		
		
			for (ExperimentalRecord rec:records) {
				counter++;

				recs.add(rec);

				if(counter%1000==0)
					System.out.println(counter);

				boolean success = false;

				long t1=System.currentTimeMillis();
				PropertyValue pv=pvc.createPropertyValue(rec, false);//loadAuxTables should have handled the loading
				long t2=System.currentTimeMillis();

				//				System.out.println("Time to create property value subobjects:"+(t2-t1)+" milliseconds");


				if (pv.getUnit()==null) {
					failedRecords.add(rec);
					rec.keep=false;
					rec.reason="Units not in database";
					System.out.println(rec.reason);
					//					logger.warn(rec.id_physchem + ": Loading failed");
					countFailure++;
					continue;
				}
				
				
				if (pv.getSourceChemical()!=null && pv.getSourceChemical().getId()==null) {
					failedRecords.add(rec);
					rec.keep=false;
					rec.reason="SourceChemical not set";
					System.out.println(rec.reason);
					//					logger.warn(rec.id_physchem + ": Loading failed");
					countFailure++;
					continue;
				}

				
				if (pv.getLiteratureSource()!=null && pv.getLiteratureSource().getId()==null) {
					rec.keep=false;
					rec.reason="LiteratureSource missing id:"+pv.getLiteratureSource().getCitation();
					System.out.println(rec.reason);
					//					logger.warn(rec.id_physchem + ": Loading failed");
					countFailure++;
					continue;
					
				}
				
				if (pv.getPublicSourceOriginal()!=null && pv.getPublicSourceOriginal().getId()==null) {
					rec.keep=false;
					rec.reason="PublicSourceOriginal missing id:"+pv.getPublicSourceOriginal().getName();
					System.out.println(rec.reason);
					//					logger.warn(rec.id_physchem + ": Loading failed");
					countFailure++;
					continue;
					
				}

				boolean paramsOk=paramValCreator.addParameters(type, rec, pv);

				if(!paramsOk) {
					failedRecords.add(rec);
					continue;
				}

				propertyValues.add(pv);

				if(createDBEntries && propertyValues.size()==10000) {
					boolean loaded=pvc.propertyValueService.createSql(propertyValues,SqlUtilities.getConnectionPostgres());

					if(loaded) {
						loadedPropertyValues.addAll(propertyValues);
						countSuccess+=propertyValues.size();
						loadedRecords.addAll(recs);
					} else {
						failedRecords.addAll(recs);
					}
					propertyValues.clear();
					recs.clear();
				} 

				countTotal++;
				
//				if(countTotal==10000) break;

			}//end loop over records
			
			//Do what's left
			
			if(createDBEntries) {

				boolean loaded=pvc.propertyValueService.createSql(propertyValues,SqlUtilities.getConnectionPostgres());
				
				if(loaded) {
					loadedPropertyValues.addAll(propertyValues);
					countSuccess+=propertyValues.size();
					loadedRecords.addAll(recs);
				} else {
					failedRecords.addAll(recs);
				}
			} else {
				loadedPropertyValues.addAll(propertyValues);
				countSuccess+=propertyValues.size();
				loadedRecords.addAll(recs);
			}
			
			System.out.println("Finished attempt to load " + countTotal + " property values: " 
					+ countSuccess + " successful; " 
					+ countFailure + " failed");
		
			writeFailedRecords(type, sourceName, propertyName, failedRecords);
			writeLoadedRecords(type, createDBEntries, sourceName, propertyName, loadedRecords, loadedPropertyValues);
		
		}

		private void writeLoadedRecords(String type, boolean createDBEntries, String sourceName, String propertyName,
				ExperimentalRecords loadedRecords, List<PropertyValue> loadedPropertyValues) {
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
		}

		private void writeFailedRecords(String type, String sourceName, String propertyName,
				ExperimentalRecords failedRecords) {
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
		}

		
		
	}
	
	
	public static void main(String[] args) {
		
		
		ExperimentalRecordLoader loader = new ExperimentalRecordLoader("tmarti02");

//		loader.delete.deleteExpPropData();
//		loader.delete.deleteByPublicSourceName();
//		loader.delete.deleteByPublicSourceNameAndProperty();
//		loader.delete.deleteByPublicSourceName();
		
//		loader.loaders.loadBCFArnot();//loaded
//		loader.loaders.loadBCFDataEcotox();//loaded
//		loader.loaders.loadBCFDataBurkhard();//loaded
//		loader.loaders.loadBCFDataNITE();//need to run
		
		//		loader.loadOChem();
//		loader.loaders.loadPubchemMultiple();
//		loader.loaders.loadPubChem();

//		loader.loaders.loadAcuteAquaticToxicityDataEcotox();
		loader.loaders.loadAcuteAquaticToxicityDataQsarToolbox();
		
		
//		loader.loaders.loadRBIODEG_NITE_OPPT();
//		loader.loaders.loadRBIODEG_RIFM();
//		
		//		deleteEcotoxData();

	}

}


