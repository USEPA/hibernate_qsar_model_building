package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;


import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Application.model.PredictionResultsPrimaryTable;
import ToxPredictor.Application.model.SimilarChemical;
import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxSnapshot;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport;
import gov.epa.databases.dev_qsar.qsar_models.entity.Source;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxRecordServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxSnapshotServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.MethodServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionReportServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceService;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceServiceImpl;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;


/**
* @author TMARTI02
*/
public class PredictionDashboardScriptTEST  {
	
	MethodServiceImpl methodService=new MethodServiceImpl();
	PredictionDashboardServiceImpl predictionDashboardService=new PredictionDashboardServiceImpl();
	PredictionReportServiceImpl predictionReportService=new PredictionReportServiceImpl();
	DsstoxRecordServiceImpl dsstoxRecordService=new  DsstoxRecordServiceImpl();
	
	
	
	String lanId="tmarti02";
	String version="5.1.3";

	
	String[] propertyNames = { "Fathead minnow LC50 (96 hr)", "Daphnia magna LC50 (48 hr)",
			"T. pyriformis IGC50 (48 hr)", "Oral rat LD50", "Bioconcentration factor", "Developmental Toxicity",
			"Mutagenicity", "Estrogen Receptor Binding", "Estrogen Receptor RBA", "Normal boiling point",
			"Melting point", "Flash point", "Vapor pressure at 25?C", "Density", "Surface tension at 25?C",
			"Thermal conductivity at 25?C", "Viscosity at 25?C", "Water solubility at 25?C" };

	
	void runFromSampleJsonFileHashtable(String filepathJson,String versionTEST) {
		
		Type listOfMyClassObject = new TypeToken<Hashtable<String,List<PredictionResults>>>() {}.getType();
		
		try {
			Hashtable<String,List<PredictionResults>>htResultsAll=Utilities.gson.fromJson(new FileReader(filepathJson), listOfMyClassObject);

			//TODO add code to create automatically new methods if they arent in the methods table in db
			HashMap<String,Method> hmMethods=new HashMap<>();
			hmMethods.put("consensus_regressor",methodService.findByName("consensus_regressor"));
			hmMethods.put("consensus_classifier",methodService.findByName("consensus_classifier"));
			
			HashMap<String, Model> hmModels = createModels(hmMethods);
			
//			if(true)return;
			
			DsstoxSnapshotServiceImpl snapshotService=new  DsstoxSnapshotServiceImpl();
			DsstoxSnapshot snapshot=snapshotService.findByName("DSSTOX Snapshot 04/23");
			Hashtable<String,Long> htCIDtoDsstoxRecordId=dsstoxRecordService.getRecordIdHashtable(snapshot);

			
			for (String DTXSID:htResultsAll.keySet()) {
				
				List<PredictionResults>listPredictionResults=htResultsAll.get(DTXSID);
				
				for (PredictionResults pr:listPredictionResults) {
					PredictionDashboard pd=convertPredictionResultsToPredictionDashboard(pr,hmModels,true,htCIDtoDsstoxRecordId);
					predictionDashboardService.create(pd);
//					System.out.println(Utilities.gson.toJson(pd));
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	
	void runFromSampleJsonFile(String filepathJson) {
		
		DsstoxSnapshotServiceImpl snapshotService=new  DsstoxSnapshotServiceImpl();
		DsstoxSnapshot snapshot=snapshotService.findByName("DSSTOX Snapshot 04/23");
		Hashtable<String,Long> htCIDtoDsstoxRecordId=dsstoxRecordService.getRecordIdHashtable(snapshot);

		
		Type listOfMyClassObject = new TypeToken<List<PredictionResults>>() {}.getType();
		
		try {
			List<PredictionResults>resultsAll=Utilities.gson.fromJson(new FileReader(filepathJson), listOfMyClassObject);
			
			//TODO add code to create automatically new methods if they arent in the methods table in db
			HashMap<String,Method> hmMethods=new HashMap<>();
			hmMethods.put("consensus_regressor",methodService.findByName("consensus_regressor"));
			hmMethods.put("consensus_classifier",methodService.findByName("consensus_classifier"));
			
			HashMap<String, Model> hmModels = createModels(hmMethods);
			
//			if(true)return;
			
			for (PredictionResults pr:resultsAll) {
				PredictionDashboard pd=convertPredictionResultsToPredictionDashboard(pr,hmModels,true,htCIDtoDsstoxRecordId);
				predictionDashboardService.create(pd);
//				System.out.println(Utilities.gson.toJson(pd));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	
	/* Possible prediction errors:
	
	 * [
  {
    "prediction_error": "Error processing record with CAS null, error=Timeout 120000 ms while generating paths for null."
  },
  {
    "prediction_error": "The consensus prediction for this chemical is considered unreliable since only one prediction can only be made"
  },
  {
    "prediction_error": "FindPaths"
  },
  {
    "prediction_error": "Only one nonhydrogen atom"
  },
  {
    "prediction_error": "FindRings"
  },
  {
    "prediction_error": "Molecule does not contain carbon"
  },
  {
    "prediction_error": "Molecule contains unsupported element"
  },
  {
    "prediction_error": "No prediction could be made due to applicability domain violation"
  },
  {
    "prediction_error": "Multiple molecules"
  }
]
	 */
	

	void runFromDashboardJsonFileBatchPost(String filepathJson) {
		
		try {
			
			System.out.println(filepathJson);
			
			DsstoxSnapshotServiceImpl snapshotService=new  DsstoxSnapshotServiceImpl();
			DsstoxSnapshot snapshot=snapshotService.findByName("DSSTOX Snapshot 04/23");
			Hashtable<String,Long> htCIDtoDsstoxRecordId=dsstoxRecordService.getRecordIdHashtable(snapshot);
			
			//TODO add code to create automatically new methods if they arent in the methods table in db
			HashMap<String,Method> hmMethods=new HashMap<>();
			hmMethods.put("consensus_regressor",methodService.findByName("consensus_regressor"));
			hmMethods.put("consensus_classifier",methodService.findByName("consensus_classifier"));
			
			HashMap<String, Model> hmModels = createModels(hmMethods);
			
			BufferedReader br=new BufferedReader(new FileReader(filepathJson));
			
			int counter=0;
			int countToPost=10000;
			
			List<PredictionDashboard>predictions=new ArrayList<>();
			List<PredictionReport>predictionReports=new ArrayList<>();
						
			//Get list of prediction dashboard keys already in the database:
			HashSet<String> pd_keys = getPredictionsDashboardKeysInDB();

			int countAlreadyHave=0;
			Gson gson=new Gson();
			
			while (true) {
				
//				System.out.println("start loop");
				
				String strPredictionResults=br.readLine();
				if(strPredictionResults==null) break;
				counter++;
				
//				System.out.println(strPredictionResults);
								
				if(counter%countToPost==0) System.out.println(counter);
				
				PredictionResults predictionResults=Utilities.gson.fromJson(strPredictionResults,PredictionResults.class);
				
				fixPredictionResults(predictionResults);//fixes error where CAS was set to the SID for the test chemical in the similar chemicals table
//				System.out.println(Utilities.gson.toJson(predictionResults));
				
				//Store fixed report as string:
				String strPredictionResults2=gson.toJson(predictionResults);
								
				PredictionDashboard pd=convertPredictionResultsToPredictionDashboard(predictionResults,hmModels,true,htCIDtoDsstoxRecordId);
//				pd=predictionDashboardService.create(pd);				
				
				//See if prediction is already in the database:
				if(pd_keys.contains(pd.getKey())) {
					countAlreadyHave++;
//					System.out.println("Already have in db ("+countAlreadyHave+"):"+pd.getKey());
					continue;
				}
				
				predictions.add(pd);
				
//				if(predictionDashboard.getPredictionError()!=null) continue;//For testing
				
//				byte[] bytes=PredictionReport.compress(strPredictionResults);
//				byte[] bytes=strPredictionResults.getBytes(StandardCharsets.ISO_8859_1);
				byte[] bytes=strPredictionResults2.getBytes();
//				String line2=PredictionReport.decompress(bytes);
//				System.out.println("Decompressed:"+line2);
				
//				System.out.println(pd.getDtxcid()+"\t"+pd.getModel().getName()+"\t"+pd.getPredictionValue()+"\t"+pd.getPredictionString()+"\t"+pd.getPredictionError());

				PredictionReport predictionReport=new PredictionReport(pd, bytes, lanId);
//				predictionReportService.create(predictionReport);
								
				predictionReports.add(predictionReport);
				
				if(predictions.size()==countToPost) {
					
//					System.out.println(counter);
					
					predictionDashboardService.createSQL(predictions);
					predictions.clear();
					
					predictionReportService.createSQL(predictionReports);
					predictionReports.clear();

				}
				
//				if(true) break;
			}
			br.close();
			
			System.out.println("exited main loop");
			
			predictionDashboardService.createSQL(predictions);
			predictionReportService.createSQL(predictionReports);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}

	private HashSet<String> getPredictionsDashboardKeysInDB() throws SQLException {
		HashSet<String> pd_keys=new HashSet<>();

		String sql="select canon_qsar_smiles, fk_dsstox_records_id, fk_model_id from qsar_models.predictions_dashboard pd\n"+
				"where fk_model_id>=223 and fk_model_id<=240";
				
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
					
		while (rs.next()) {
			String canon_qsar_smiles=rs.getString(1);
			Long fk_dsstox_records_id=rs.getLong(2);
			String fk_model_id=rs.getString(3);
			String key=canon_qsar_smiles+"\t"+fk_dsstox_records_id+"\t"+fk_model_id;
//				System.out.println(key);
			pd_keys.add(key);
		}
		
		System.out.println("Got keys for test predictions in predictions dashboard:"+pd_keys.size());
		
		return pd_keys;
	}
	
	

	/**
	 * TODO this method needs to be updated based on latest schema for predictions_dashboard table
	 * 
	 * @param filepathJson
	 */
	void findMissingPredictions(String filepathJson) {
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filepathJson));
			HashSet<String> sidsJsonFile=new HashSet<>();
			
			while (true) {
				String strPredictionResults=br.readLine();
				if(strPredictionResults==null) break;
				
				PredictionResults predictionResults=Utilities.gson.fromJson(strPredictionResults,PredictionResults.class);
				
				if (!sidsJsonFile.contains(predictionResults.getDTXSID())) {
					sidsJsonFile.add(predictionResults.getDTXSID());	
//					System.out.println(predictionResults.getDTXSID());
//					if (sidsJsonFile.size()==100) break;
					
					if (sidsJsonFile.size()%1000==0) {
						System.out.println("\t"+sidsJsonFile.size());
					}
					
				}
				
			}
			br.close();
			
			
			System.out.println("Unique sids in json file="+sidsJsonFile.size());
			
			HashSet<String> sidsDB=new HashSet<>();
			
			String sql="select dtxsid from qsar_models.predictions_dashboard pd where fk_model_id=223";
			
			ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
			
			while (rs.next()) {
				String sid=rs.getString(1);
//				System.out.println(sid);
				sidsDB.add(sid);
			}
			
			int countMissing=0;
			for (String sidJsonFile:sidsJsonFile) {
				if(!sidsDB.contains(sidJsonFile)) {
					countMissing++;
				}
			}
			System.out.println("Unique sids in db for TEST models="+sidsDB.size());
			
			System.out.println("countMissing="+countMissing);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	
	
	

	void uploadMissingReports(String filepathJson) {
		
		try {
			
			HashSet<String> pd_keys = getPredictionDashboardKeysMissingReports();
			System.out.println("Number of predictions missing a report="+pd_keys.size());
			
			DsstoxSnapshotServiceImpl snapshotService=new  DsstoxSnapshotServiceImpl();
			DsstoxSnapshot snapshot=snapshotService.findByName("DSSTOX Snapshot 04/23");
			Hashtable<String,Long> htCIDtoDsstoxRecordId=dsstoxRecordService.getRecordIdHashtable(snapshot);

			
			HashMap<String,Method> hmMethods=new HashMap<>();
			hmMethods.put("consensus_regressor",methodService.findByName("consensus_regressor"));
			hmMethods.put("consensus_classifier",methodService.findByName("consensus_classifier"));
			HashMap<String, Model> hmModels = createModels(hmMethods);
			
			BufferedReader br=new BufferedReader(new FileReader(filepathJson));
								
			int countOK=0;
			
			List<PredictionReport>missingReports=new ArrayList<>();
			
			int counter=0;
			
			while (true) {
			
				
				String strPredictionResults=br.readLine();
				if(strPredictionResults==null) break;
				
				counter++;
				if(counter%1000==0) System.out.println(counter);
				
				PredictionResults predictionResults=Utilities.gson.fromJson(strPredictionResults,PredictionResults.class);
				
				long t3=System.currentTimeMillis();
				
//				if(!predictionResults.getDTXSID().equals("DTXSID10976888")) continue;
				
				PredictionDashboard pd=convertPredictionResultsToPredictionDashboard(predictionResults,hmModels,true,htCIDtoDsstoxRecordId);

				//Using key of all the main variables is faster than trying to do a database look up for each predictionDashboard record:
//				String pd_id=predictionReportService.getPredictionDashboardId(SqlUtilities.getConnectionPostgres(), pd);
				String pd_key=pd.getKey();
				
//				String strSQL="select id from qsar_models.prediction_reports pr where pr.fk_prediction_dashboard_id="+pd_id;
//				String pr_id=SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), strSQL);
				
				if(pd_keys.contains(pd_key)) {
					byte[] bytes=strPredictionResults.getBytes();
					PredictionReport predictionReport=new PredictionReport(pd, bytes, lanId);
					missingReports.add(predictionReport);
					System.out.println(missingReports.size()+" Missing\tpd_key="+pd_key);	
				}else {
					countOK++;
//					System.out.println(countOK+" In DB: pd_id="+ pd_key);
				}
				
				if (missingReports.size()==pd_keys.size()) {
					System.out.println("Exiting loop, found all the missing reports");
					break;
				}
			}
			
			System.out.println("missingReports.size()="+missingReports.size());
			
			predictionReportService.createSQL(missingReports);
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}

	private HashSet<String> getPredictionDashboardKeysMissingReports() throws SQLException {
		HashSet<String> pd_keys=new HashSet<>();
		

		String sql="select canon_qsar_smiles, fk_dsstox_records_id ,fk_model_id from qsar_models.predictions_dashboard pd\n"+
				"left join qsar_models.prediction_reports pr on pd.id = pr.fk_prediction_dashboard_id\n"+
				"where fk_model_id>=223 and fk_model_id<=240 and pr.file is null";
				
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		
		
		while (rs.next()) {
			String canon_qsar_smiles=rs.getString(1);
			Long fk_dsstox_records_id=rs.getLong(2);
			String fk_model_id=rs.getString(3);
			String key=canon_qsar_smiles+"\t"+fk_dsstox_records_id+"\t"+fk_model_id;
			
//				System.out.println(key);
			pd_keys.add(key);
		}
		return pd_keys;
	}
	
	/**
	 * Gets records for a specific dtxcid in a json file
	 * 
	 * @param filepathJson
	 * @param filepathOutput
	 * @param dtxcid
	 */
	void extractRecords(String filepathJson,String filepathOutput,String dtxcid) {
		
		try {
			
			
			BufferedReader br=new BufferedReader(new FileReader(filepathJson));
			FileWriter fw=new FileWriter(filepathOutput);
			
			int count=0;
			
			Gson gson=new Gson();
			

//			for (String line:lines) {
			while (true) {
				String line=br.readLine();
				if(line==null) break;
				count++;
				PredictionResults pr=Utilities.gson.fromJson(line,PredictionResults.class);
				
				if (pr.getDTXCID().equals(dtxcid)) {
					System.out.println(Utilities.gson.toJson(pr));
					fw.write(gson.toJson(pr)+"\r\n");
					fw.flush();
				}
				
				
			}
			fw.close();
			br.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}


	
	void extractRecords2(String filepathJson,String filepathOutput,String dtxsid) {
		
		try {
			
			
			BufferedReader br=new BufferedReader(new FileReader(filepathJson));
			FileWriter fw=new FileWriter(filepathOutput);
			
			int count=0;
			
			Gson gson=new Gson();
			

//			for (String line:lines) {
			while (true) {
				String line=br.readLine();
				if(line==null) break;
				count++;
				PredictionResults pr=Utilities.gson.fromJson(line,PredictionResults.class);
				
				if (pr.getDTXSID().equals(dtxsid)) {
					System.out.println(Utilities.gson.toJson(pr));
					fw.write(gson.toJson(pr)+"\r\n");
					fw.flush();
				}
				
				
			}
			fw.close();
			br.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}


	private HashMap<String, Model> createModels(HashMap<String, Method> hmMethods) {
		HashMap<String,Model> hmModels=new HashMap<>();
		
		String sourceName=getSoftwareName();
		
		SourceService ss=new SourceServiceImpl();
		Source source=ss.findByName(sourceName);

		
		for (String propertyName:propertyNames) {
			String descriptorSetName=getSoftwareName();

			String splittingName="TEST";
			String propertyNameDB=getPropertyNameDB(propertyName);
			String datasetName=getDatasetName(propertyNameDB);
				
			String methodName=null;
			if (propertyNameDB.equals(DevQsarConstants.DEVELOPMENTAL_TOXICITY) || propertyNameDB.equals(DevQsarConstants.MUTAGENICITY) || propertyNameDB.equals(DevQsarConstants.ESTROGEN_RECEPTOR_BINDING)) {
				methodName="consensus_classifier";
			} else {
				methodName="consensus_regressor";
			}

			String modelName=getModelName(propertyNameDB); 
			
//			System.out.println(modelName);
			
			Model model=new Model(modelName, hmMethods.get(methodName), null,descriptorSetName, datasetName, splittingName, source,lanId);
			model=CreatorScript.createModel(model);
			
//			System.out.println(modelName+"\t"+model.getName());
			hmModels.put(modelName, model);
		}
		return hmModels;
	}
	
	

	
	
	String getDatasetDescription(String propertyNameDB) {
		
		
		if (propertyNameDB.equals(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50)) {
			return "96 hour fathead minnow LC50 data compiled from ECOTOX for the TEST software";
		} else if (propertyNameDB.equals(DevQsarConstants.FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50)) {
			return("48 hour Daphnia magna LC50 data compiled from ECOTOX for the TEST software");
		} else if (propertyNameDB.equals(DevQsarConstants.FORTY_EIGHT_HR_TETRAHYMENA_PYRIFORMIS_IGC50)) {
			return("48 hour T. pyriformis IGC50 data compiled from Schultz et al for the TEST software");
		} else if (propertyNameDB.equals(DevQsarConstants.ORAL_RAT_LD50)) {
			return("Oral rat LD50 data compiled from ChemIDplus for the TEST software");
		} else if (propertyNameDB.equals(DevQsarConstants.THERMAL_CONDUCTIVITY)) {
			return "Thermal conductivity data compiled from Jamieson and Vargaftik for the TEST software";
		} else if (propertyNameDB.equals(DevQsarConstants.SURFACE_TENSION)) {
			return "Surface tension data compiled from Jaspar for the TEST software";
		} else if (propertyNameDB.equals(DevQsarConstants.VISCOSITY)) {
			return "Viscosity data compiled from Viswanath and Riddick for the TEST software";
		} else if (propertyNameDB.equals(DevQsarConstants.BOILING_POINT)) { 
			return "Boiling point data compiled from EPISUITE data for the TEST software"; 
		} else if (propertyNameDB.equals(DevQsarConstants.VAPOR_PRESSURE)) { 
			return "Vapor pressure data compiled from EPISUITE data for the TEST software"; 
		} else if (propertyNameDB.equals(DevQsarConstants.WATER_SOLUBILITY)) { 
			return "Water solubility data compiled from EPISUITE data for the TEST software"; 
		} else if (propertyNameDB.equals(DevQsarConstants.MELTING_POINT)) { 
			return "Melting point data compiled from EPISUITE data for the TEST software"; 
		} else if (propertyNameDB.equals(DevQsarConstants.DENSITY)) { 
			return "Density data compiled from LookChem.com for the TEST software"; 
		} else if (propertyNameDB.equals(DevQsarConstants.FLASH_POINT)) { 
			return "Flash point data compiled from LookChem.com for the TEST software"; 
		} else if (propertyNameDB.equals(DevQsarConstants.BCF)) {
			return("Bioconcentration factor data compiled from literature sources for the TEST software");//TODO doesnt exist			
		} else if (propertyNameDB.equals(DevQsarConstants.DEVELOPMENTAL_TOXICITY)) {
			return("Developmental toxicity data compiled from Arena et al for the TEST software");//TODO doesnt exist
		} else if (propertyNameDB.equals(DevQsarConstants.AMES_MUTAGENICITY)) {
			return("Ames mutagenicity data  compiled from Hansen et al for the TEST software");//TODO doesnt exist

		} else  {
			return propertyNameDB;
		} 
			

	}

	
	
	String getPropertyNameDB(String propertyName) {
		
		if (propertyName.equals("Fathead minnow LC50 (96 hr)")) {
			return DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50;
		} else if (propertyName.equals("Daphnia magna LC50 (48 hr)")) {
			return DevQsarConstants.FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50;
		} else if (propertyName.equals("T. pyriformis IGC50 (48 hr)")) {
			return DevQsarConstants.FORTY_EIGHT_HR_TETRAHYMENA_PYRIFORMIS_IGC50;
		} else if (propertyName.equals("Oral rat LD50")) {
			return DevQsarConstants.ORAL_RAT_LD50;
		} else if (propertyName.equals("Developmental Toxicity")) {
			return DevQsarConstants.DEVELOPMENTAL_TOXICITY;
		} else if (propertyName.equals("Normal boiling point")) { 
			return DevQsarConstants.BOILING_POINT;
		} else if (propertyName.equals("Melting point")) { 
			return DevQsarConstants.MELTING_POINT;
		} else if (propertyName.equals("Flash point")) { 
			return DevQsarConstants.FLASH_POINT;
		} else if (propertyName.equals("Density")) { 
			return DevQsarConstants.DENSITY;
		} else if (propertyName.contains("Bioconcentration factor")) {
			return DevQsarConstants.BCF;			
		} else if (propertyName.contains(" at 25")) { 
			propertyName=propertyName.substring(0,propertyName.indexOf(" at 25"));
			return propertyName; 
		} else if (propertyName.equals("Estrogen Receptor Binding")) {
			return DevQsarConstants.ESTROGEN_RECEPTOR_BINDING;
		} else if (propertyName.equals("Estrogen Receptor RBA")) {
			return DevQsarConstants.ESTROGEN_RECEPTOR_RBA;
		} else if (propertyName.equals("Mutagenicity")) {
			return DevQsarConstants.AMES_MUTAGENICITY;
		} else if (propertyName.equals("Developmental Toxicity")) {
			return DevQsarConstants.DEVELOPMENTAL_TOXICITY;
		} else  {
			return "*"+propertyName;
		} 
			

	}
	

	

	
		
//	String getFieldValue(String fieldName)  {
//		
//		try {
//			DevQsarConstants d=new DevQsarConstants();
//			
//			Field myField = d.getClass().getField(fieldName);
//			
//			return (String)myField.get(d);
//
//		
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
//		
//		return "";
//		
//	}
	
	String getSoftwareName() {
		return "TEST"+version;
	}
	
	String getModelName(String propertyNameDB) {
		return propertyNameDB+" "+getSoftwareName();
	}

	private String getDatasetName(String propertyNameDB) {
		return getModelName(propertyNameDB);
	}
	
	
	

	
	PredictionDashboard convertPredictionResultsToPredictionDashboard(PredictionResults pr,HashMap<String,Model>htModels,boolean convertPredictionMolarUnits,Hashtable<String,Long> htCIDtoDsstoxRecordId) {

		if(pr.getSmiles()==null) pr.setSmiles("N/A");
		
		PredictionDashboard pd=new PredictionDashboard();
		
		try {

			PredictionResultsPrimaryTable pt=pr.getPredictionResultsPrimaryTable();

			try {

				String propertyName=pr.getEndpoint();
				String propertyNameDB=getPropertyNameDB(propertyName);
				String modelName=getModelName(propertyNameDB);
				
//				System.out.println(Utilities.gson.toJson(htModels.get(modelName)));
				
				pd.setModel(htModels.get(modelName));
				
//				System.out.println("here");
								
				pd.setCanonQsarSmiles("N/A");
				
				DsstoxRecord dr=new DsstoxRecord();
				dr.setId(htCIDtoDsstoxRecordId.get(pr.getDTXCID()));
				pd.setDsstoxRecord(dr);

//				pd.setDtxsid(pr.getDTXSID());
//				pd.setDtxcid(pr.getDTXCID());
//				pd.setSmiles(pr.getSmiles());
				
				pd.setCreatedBy(lanId);

				if (pr.getError()!=null && !pr.getError().isBlank()) {
					pd.setPredictionError(pr.getError());
				} else {
					if (pr.isBinaryEndpoint()) {
						if (pt.getPredToxValue().equals("N/A")) {
							pd.setPredictionError(pt.getMessage());
						} else {
							pd.setPredictionValue(Double.parseDouble(pt.getPredToxValue()));
							pd.setPredictionString(pt.getPredValueEndpoint());
						}
					} else if (pr.isLogMolarEndpoint()) {

						if (pt.getPredToxValue().equals("N/A")) {
							pd.setPredictionError(pt.getMessage());
						} else {
							if (convertPredictionMolarUnits)
								convertLogMolarUnits(pd, pt);
							else {
								pd.setPredictionValue(Double.parseDouble(pt.getPredToxValue()));
//								pd.prediction_units=pt.getMolarLogUnits();
							}
						}
					} else {

						if (pt.getPredToxValMass().equals("N/A")) {
							pd.setPredictionError(pt.getMessage());
						} else {
							pd.setPredictionValue(Double.parseDouble(pt.getPredToxValMass()));
						}
					}
				}

				if (pd.getPredictionError()!=null) {
					if (pd.getPredictionError().equals("No prediction could be made")) {
						pd.setPredictionError("No prediction could be made due to applicability domain violation");
					} else if (pd.getPredictionError().contains("could not parse")) {
						pd.setPredictionError("Could not parse smiles");	
					}
				}

//				pd.setModel(null);//so can print out
//				System.out.println(Utilities.gson.toJson(pd));
			} catch (Exception ex) {
				//					System.out.println(gson.toJson(pr));
				ex.printStackTrace();
			}



		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return pd;
	}

	
	private static void convertLogMolarUnits(PredictionDashboard pd, PredictionResultsPrimaryTable pt) {
		
		String modelName=pd.getModel().getName();
		String name=modelName.replace(" "+pd.getModel().getSource(), "");
		
		if (name.equals(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50)
				|| name.equals(DevQsarConstants.FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50)
				|| name.equals(DevQsarConstants.FORTY_EIGHT_HR_TETRAHYMENA_PYRIFORMIS_IGC50)
				|| name.contains(DevQsarConstants.WATER_SOLUBILITY)) {
			pd.setPredictionValue(Math.pow(10.0,-Double.parseDouble(pt.getPredToxValue())));
//			pd.prediction_units="M";
		} else if (name.equals(DevQsarConstants.ORAL_RAT_LD50)) {
			pd.setPredictionValue(Math.pow(10.0,-Double.parseDouble(pt.getPredToxValue())));
//			pd.prediction_units="mol/kg";
		} else if (name.equals(DevQsarConstants.BCF)) {
			pd.setPredictionValue(Math.pow(10.0,Double.parseDouble(pt.getPredToxValue())));
//			pd.prediction_units="L/kg";								
		} else if (name.contains(DevQsarConstants.VAPOR_PRESSURE)) {
			pd.setPredictionValue(Math.pow(10.0,Double.parseDouble(pt.getPredToxValue())));
//			pd.prediction_units="mmHg";								
		} else if (name.contains(DevQsarConstants.VISCOSITY)) {
			pd.setPredictionValue(Math.pow(10.0,Double.parseDouble(pt.getPredToxValue())));
//			pd.prediction_units="cP";								
		} else if (name.equals(DevQsarConstants.ESTROGEN_RECEPTOR_RBA)) {
			pd.setPredictionValue(Math.pow(10.0,Double.parseDouble(pt.getPredToxValue())));
//			pd.prediction_units="Dimensionless";
		} else {
			System.out.println("Not handled:"+name);
		}
	}
	
	


	void createDatasets() {
		
		HashMap<String, String>hmUnitsDataset=DevQsarConstants.getDatasetFinalUnitsNameMap();
		HashMap<String, String>hmUnitsDatasetContributor=DevQsarConstants.getContributorUnitsNameMap();
		
		for (String propertyName:propertyNames) {
			
			String propertyNameDB=getPropertyNameDB(propertyName);
			
			
			String datasetName = getDatasetName(propertyNameDB);
			
			String unitAbbrev=hmUnitsDataset.get(propertyNameDB);
			String unitContributorAbbrev=hmUnitsDatasetContributor.get(propertyNameDB);

			Unit unit=CreatorScript.createUnit(unitAbbrev,lanId);
			Unit unitContributor=CreatorScript.createUnit(unitContributorAbbrev,lanId);
			
			String propertyDescriptionDB=DevQsarConstants.getPropertyDescription(propertyNameDB);

//			System.out.println(propertyName+"\t"+datasetName+"\t"+unitName+"\t"+unitContributorName);
//			System.out.println(propertyName+"\t"+propertyDescriptionDB);
//			System.out.println(propertyName+"\t"+unitName+"\t"+unitContributorName);
			
			//			
			Property property=CreatorScript.createProperty(propertyNameDB, propertyDescriptionDB,lanId);
			String datasetDescription=getDatasetDescription(propertyNameDB);
//			System.out.println(datasetName+"\t"+datasetDescription);

			String dsstoxMappingStrategy="CASRN";
			
			Dataset dataset=new Dataset(datasetName, datasetDescription, property, unit, unitContributor,
					dsstoxMappingStrategy, lanId);
			
			CreatorScript.createDataset(dataset);
			
			System.out.println(Utilities.gson.toJson(dataset));
			
			
		}
		
	}



	
	
	
	void testRetrievePredictionReport() {

		//To do it via sql:  select convert_from(file, 'ISO-8859-1') from qsar_models.prediction_reports r

		PredictionReport pr=predictionReportService.findByPredictionDashboardId(1406L);
//		String strPredictionReport=pr.decompress(pr.getFile());
//		String strPredictionReport=new String(pr.getFile(), StandardCharsets.ISO_8859_1);
		String strPredictionReport=new String(pr.getFile());
		
		System.out.println(strPredictionReport);
			
		PredictionResults predictionResults=Utilities.gson.fromJson(strPredictionReport, PredictionResults.class);
		System.out.println(Utilities.gson.toJson(predictionResults));
	}
	
	
	void checkReportsForChemical() {

		String dtxsid="DTXSID00223252";
		String sql="select pr.id, pr.file from qsar_models.predictions_dashboard pd\n"
				+ "join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id=dr.id\n"
				+ "join qsar_models.models m on m.id=pd.fk_model_id\n"
				+ "join qsar_models.prediction_reports pr on pd.id = pr.fk_prediction_dashboard_id\n"
				+ "where dr.dtxsid='"+dtxsid+"' and m.\"source\" ='TEST5.1.3'";
				
		
		Connection conn=SqlUtilities.getConnectionPostgres();
		
		try {
			
			ResultSet rs=SqlUtilities.runSQL2(conn, sql);
			
			while (rs.next()) {
				
				Long id=rs.getLong(1);
				String strPredictionReport=new String(rs.getBytes(2));
				
				if (strPredictionReport.contains("test chemical")) {
					JsonObject jo=Utilities.gson.fromJson(strPredictionReport, JsonObject.class);
					System.out.println(id+"\t"+Utilities.gson.toJson(jo));
					break;
				}
				
			}
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	
	/**
	 * Fixes issue where the DTXSID was stored in the cas field for the test chemical in the similar chemicals tables
	 */
	void fixReportsInDB() {
		
//		String sql="select file from qsar_models.pred"

		int batchSize=1000;
		int batch=0;
		int counter=0;
		
		List<PredictionReport>updatedReports=new ArrayList<>();
		
		while (true) {
			
			List<PredictionReport>reports=this.predictionReportService.getNonUpdatedPredictionReportsBySQL(batch*batchSize, batchSize);

			if (reports.size()==0) break;
			
			Gson gson=new Gson();
			
			for (PredictionReport report:reports) {
				
				counter++;
				
				if(counter%1000==0) System.out.println(counter);
				
				String strPredictionResults=new String(report.getFile());
				
				if (!strPredictionResults.contains("(test chemical)")) {
//					System.out.println("Doesnt have \"(test chemical)\", skipping");
					continue;
				} 
				
				
				PredictionResults pr=gson.fromJson(strPredictionResults, PredictionResults.class);
				
				fixPredictionResults(pr);
				
//				if (counter==1) {
//					System.out.println(report.getId());
//					System.out.println(Utilities.gson.toJson(pr));
//					String strPredictionResults2=gson.toJson(pr);
//					report.setFile(strPredictionResults2.getBytes());
////					this.predictionReportService.updateSQL(report);
//					updatedReports.add(report);
//					if(updatedReports.size()==batchSize) {
//						this.predictionReportService.updateSQL(updatedReports);
//						updatedReports.clear();
//					}
////					if(true) break;
////					this.predictionReportService.create(report)
//				}
				
				String strPredictionResults2=gson.toJson(pr);
				report.setFile(strPredictionResults2.getBytes());
				report.setUpdatedBy(lanId);
				updatedReports.add(report);
				
//				if (counter==1) {
//					System.out.println(report.getId());
//					this.predictionReportService.updateSQL(report);
//				}
				
				
				if(updatedReports.size()==batchSize) {
					this.predictionReportService.updateSQL(updatedReports);
					updatedReports.clear();
				}
					
//				if(true) break;
				
			}//end loop over reports from sql query
			
			batch++;//update which batch to return in main sql query
			
			
//			if(true) break;
			
		}//end while true
				
		this.predictionReportService.updateSQL(updatedReports);//update any remaining reports
		
		
	}
	
	void deleteTEST_Predictions2() {
		
		for (int i=66300000;i<69000000;i=i+10000) {
			String sql="delete from qsar_models.predictions_dashboard pd\n"
					+ "where fk_model_id>=223 and fk_model_id<=240 and id<"+i;
			SqlUtilities.runSQLUpdate(SqlUtilities.getConnectionPostgres(), sql);
			
		}
		
		
	}
	
	/**
	 * Instead of complicated delete sql, find the OPERA reports one by one and delete them
	 */
	void deleteTEST_Predictions() {
		
		String sql="select pd.id from qsar_models.predictions_dashboard pd\n"+
				"join qsar_models.models m on m.id=pd.fk_model_id\n"+
				"join qsar_models.sources s on s.id=m.fk_source_id\n"+
				"where pd.fk_model_id = m.id and s.name='TEST5.1.3'";
		
		System.out.print("\n"+sql+"\n");
		
		try {
			
			Connection conn=SqlUtilities.getConnectionPostgres();
			

			ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
			
			int counter=0;
			
			while (rs.next()) {
				counter++;
				String id=rs.getString(1);
				sql="Delete from qsar_models.predictions_dashboard where id="+id+";";
				SqlUtilities.runSQLUpdate(SqlUtilities.getConnectionPostgres(), sql);
				if(counter%1000==0) System.out.println(counter);
//				System.out.println(id);
				
			}
						

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Done");
		
	}

	public static void main(String[] args) {
		PredictionDashboardScriptTEST pds=new PredictionDashboardScriptTEST();
		
		pds.deleteTEST_Predictions2();
		
		
//		pds.createDatasets();//TODO need to add the datapoints

		pds.version="5.1.3";

//		String filePathJson="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\RunTestCalculationsFromJar\\reports\\sample.json";
		

//		int num=35;
//		String filePathJson="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\RunTestCalculationsFromJar\\reports\\TEST_results_all_endpoints_snapshot_compounds"+num+".json";//		
//////		String filePathJson="C:\\Users\\TMARTI02\\Documents\\reports\\TEST_results_all_endpoints_snapshot_compounds"+num+".json";
//		pds.runFromDashboardJsonFileBatchPost(filePathJson);

		
//		for (int num=15;num<=18;num++) {
////			String filePathJson="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\RunTestCalculationsFromJar\\reports\\TEST_results_all_endpoints_snapshot_compounds"+num+".json";//		
//			String filePathJson="C:\\Users\\TMARTI02\\Documents\\reports\\TEST_results_all_endpoints_snapshot_compounds"+num+".json";
//			pds.runFromDashboardJsonFileBatchPost(filePathJson);
//		}
		
		
		
//		pds.findMissingPredictions(filePathJson);
//		pds.uploadMissingReports(filePathJson);

		//*********************************************************************
				
//		pds.fixReportsInDB();
//		pds.checkReportsForChemical();

//		String filePathJson="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18\\reports\\sample_predictions.json";
//		pds.runFromSampleJsonFileHashtable(filePathJson,SoftwareVersion);

//		String filePathJson="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18\\reports\\TEST_results_all_endpoints_snapshot_compounds1.json";
//		pds.runFromSampleJsonFile(filePathJson);


		//*********************************************************************
//		pds.predictionReportService.getHashtableLookupPredictionDashboardId();
		
		//*********************************************************************
//		pds.lookAtValuesInDatabase();
//		pds.fixReportInDatabase();
		//*********************************************************************

//		String dtxsid="DTXSID10976888";
//		String filePathJson="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\RunTestCalculationsFromJar\\reports\\TEST_results_all_endpoints_snapshot_compounds1.json";
//		String filePathJson2="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\RunTestCalculationsFromJar\\reports\\"+dtxsid+".json";
//		pds.extractRecords2(filePathJson,filePathJson2,dtxsid);
		

	}

	private void lookAtValuesInDatabase() {
//		String dtxsid="DTXSID80177704";//N-Methyl-N'-(4-methylphenyl)-N-nitrosourea
		String dtxsid="DTXSID40177523";
		
		String modelSource="TEST5.1.3";
//		String propertyName=DevQsarConstants.WATER_SOLUBILITY;

		File folder=new File("reports/"+dtxsid);
		folder.mkdirs();

		
		for (String propertyName: DevQsarConstants.TEST_SOFTWARE_PROPERTIES) {
		
			String report=predictionReportService.getReport(dtxsid, propertyName,modelSource);
//			String report=pds.predictionReportService.getReport(dtxsid, "Boiling point TEST5.1.3");
			PredictionResults pr=Utilities.gson.fromJson(report, PredictionResults.class);
			
//			String report2=Utilities.gson.toJson(pr);
			
//			System.out.println(Utilities.gson.toJson(pr));
				
			String json=predictionDashboardService.getPredictionDashboardAsJson(dtxsid, propertyName, modelSource);
//			System.out.println(json);
			
						
			try {
				FileWriter fw = new FileWriter(folder.getAbsolutePath()+File.separator+propertyName+".json");
				fw.write(report);
				fw.flush();
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}
	}
	
	private void fixReportInDatabase() {
		String dtxsid="DTXSID80177704";//N-Methyl-N'-(4-methylphenyl)-N-nitrosourea
		String modelSource="TEST5.1.3";
		String propertyName=DevQsarConstants.WATER_SOLUBILITY;

		
		String report=predictionReportService.getReport(dtxsid, propertyName,modelSource);
//		String report=pds.predictionReportService.getReport(dtxsid, "Boiling point TEST5.1.3");
		PredictionResults pr=Utilities.gson.fromJson(report, PredictionResults.class);
				
		fixPredictionResults(pr);
		
//		System.out.println(Utilities.gson.toJson(similarChemicals0));
//		System.out.println(Utilities.gson.toJson(similarChemicals1));
		System.out.println(Utilities.gson.toJson(pr));
		
	}

	/**
	 * Fixes error where CAS was set to the SID for the test chemical in the similar chemicals table
	 * 
	 * @param pr
	 */
	private void fixPredictionResults(PredictionResults pr) {
				
		if (pr.getSimilarChemicals().size()==0) {
//			System.out.println(Utilities.gson.toJson(pr));
			return;
		}
		
		Vector<SimilarChemical>similarChemicals0=pr.getSimilarChemicals().get(0).getSimilarChemicalsList();
		Vector<SimilarChemical>similarChemicals1=pr.getSimilarChemicals().get(1).getSimilarChemicalsList();
						
		if (pr.getCAS()==null) {
//			System.out.println("CAS is null for "+pr.getDTXSID());
		} else {
			if (!pr.getCAS().contains("-")) {
//				System.out.println("CAS="+pr.getCAS());
			}
		}
		
		if(similarChemicals0.size()>0) {
			SimilarChemical sc0_0=similarChemicals0.get(0);
			if(pr.getCAS()!=null) sc0_0.setCAS(pr.getCAS());
			else sc0_0.setCAS("N/A");
		}
		
		if(similarChemicals1.size()>0) {
			SimilarChemical sc1_0=similarChemicals1.get(0);
			if(pr.getCAS()!=null) sc1_0.setCAS(pr.getCAS());
			else sc1_0.setCAS("N/A");
		}
	}
	

}

