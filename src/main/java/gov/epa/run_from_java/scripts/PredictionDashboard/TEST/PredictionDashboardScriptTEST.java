package gov.epa.run_from_java.scripts.PredictionDashboard.TEST;

import java.awt.Desktop;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import java.util.zip.GZIPInputStream;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.Calculations.RunFromCommandLine.CompareStandaloneToSDE;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSmiles;
import ToxPredictor.Application.model.IndividualPredictionsForConsensus;
import ToxPredictor.Application.model.IndividualPredictionsForConsensus.PredictionIndividualMethod;
import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Application.model.PredictionResultsPrimaryTable;
import ToxPredictor.Application.model.SimilarChemical;
import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.Database.ResolverDb2;
import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxSnapshot;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.MethodAD;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedADEstimate;
import gov.epa.databases.dev_qsar.qsar_models.entity.Source;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxRecordServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxSnapshotServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.MethodServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionReportServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceService;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceServiceImpl;
import gov.epa.databases.dsstox.DsstoxSession;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.CreatorScript;
import gov.epa.run_from_java.scripts.PredictionDashboard.DatabaseUtilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.OPERA.OPERA_Report;
import gov.epa.run_from_java.scripts.PredictionDashboard.OPERA.OPERA_csv_to_PostGres_DB;
import gov.epa.run_from_java.scripts.PredictionDashboard.OPERA.OPERA_lookups;


/**
* @author TMARTI02
*/
public class PredictionDashboardScriptTEST  {
	
	MethodServiceImpl methodService=new MethodServiceImpl();
	PredictionDashboardServiceImpl predictionDashboardService=new PredictionDashboardServiceImpl();
	PredictionReportServiceImpl predictionReportService=new PredictionReportServiceImpl();
	DsstoxRecordServiceImpl dsstoxRecordService=new  DsstoxRecordServiceImpl();
	
	long minModelId=223L;
	long maxModelID=240L;
	
	
	String lanId="tmarti02";
	static String version="5.1.3";

	
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
			
			TreeMap<String, Model> hmModels = createModels(hmMethods);
			
//			if(true)return;
			
			DsstoxSnapshotServiceImpl snapshotService=new  DsstoxSnapshotServiceImpl();
			DsstoxSnapshot snapshot=snapshotService.findByName("DSSTOX Snapshot 04/23");
			Hashtable<String,Long> htCIDtoDsstoxRecordId=dsstoxRecordService.getRecordIdHashtable(snapshot,"dtxcid");

			
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
		Hashtable<String,Long> htCIDtoDsstoxRecordId=dsstoxRecordService.getRecordIdHashtable(snapshot,"dtxcid");

		
		Type listOfMyClassObject = new TypeToken<List<PredictionResults>>() {}.getType();
		
		try {
			List<PredictionResults>resultsAll=Utilities.gson.fromJson(new FileReader(filepathJson), listOfMyClassObject);
			
			//TODO add code to create automatically new methods if they arent in the methods table in db
			HashMap<String,Method> hmMethods=new HashMap<>();
			hmMethods.put("consensus_regressor",methodService.findByName("consensus_regressor"));
			hmMethods.put("consensus_classifier",methodService.findByName("consensus_classifier"));
			
			TreeMap<String, Model> hmModels = createModels(hmMethods);
			
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
    "Error processing record with CAS null, error=Timeout 120000 ms while generating paths for null."
    "The consensus prediction for this chemical is considered unreliable since only one prediction can only be made"
    "FindPaths"
    "Only one nonhydrogen atom"
    "FindRings"
    "Molecule does not contain carbon"
    "Molecule contains unsupported element"
    "No prediction could be made due to applicability domain violation"
    "Multiple molecules"
	 */
	void runFromDashboardJsonFileBatchPost(String filepathJson) {
		
		try {
			
			System.out.println(filepathJson);
			
			DsstoxSnapshotServiceImpl snapshotService=new  DsstoxSnapshotServiceImpl();
			DsstoxSnapshot snapshot=snapshotService.findByName("DSSTOX Snapshot 04/23");
			Hashtable<String,Long> htCIDtoDsstoxRecordId=dsstoxRecordService.getRecordIdHashtable(snapshot,"dtxcid");
			
			//TODO add code to create automatically new methods if they arent in the methods table in db
			HashMap<String,Method> hmMethods=new HashMap<>();
			hmMethods.put("consensus_regressor",methodService.findByName("consensus_regressor"));
			hmMethods.put("consensus_classifier",methodService.findByName("consensus_classifier"));
			
			TreeMap<String, Model> hmModels = createModels(hmMethods);
			
			BufferedReader br=new BufferedReader(new FileReader(filepathJson));
			
			int counter=0;
			int countToPost=10000;
			
			List<PredictionDashboard>predictions=new ArrayList<>();
			List<PredictionReport>predictionReports=new ArrayList<>();
						
			//Get list of prediction dashboard keys already in the database:
			HashSet<String> pd_keys = OPERA_csv_to_PostGres_DB.getPredictionsDashboardKeysInDB(minModelId,maxModelID);


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
	

	void createRecordsFromJsonFile(String filepathJson,boolean fixReports,boolean skipER,boolean writeToDB) {
		
		try {
			System.out.println(filepathJson);
			
			DsstoxSnapshotServiceImpl snapshotService=new  DsstoxSnapshotServiceImpl();
			DsstoxSnapshot snapshot=snapshotService.findByName("DSSTOX Snapshot 04/23");
			Hashtable<String,Long> htCIDtoDsstoxRecordId=dsstoxRecordService.getRecordIdHashtable(snapshot,"dtxcid");
			
//			HashMap<String, Model> hmModels = createModels(hmMethods);
			TreeMap<String, Model> hmModels = CreatorScript.getModelsMap();
			TreeMap<String,MethodAD>hmMethodAD=CreatorScript.getMethodAD_Map();
			
			BufferedReader br=new BufferedReader(new FileReader(filepathJson));
			
			int counter=0;
			int countToPost=1000;
			
			List<PredictionDashboard>predictions=new ArrayList<>();
						
			//Get list of prediction dashboard keys already in the database:
			HashSet<String> pd_keys = OPERA_csv_to_PostGres_DB.getPredictionsDashboardKeysInDB(minModelId,maxModelID);

			int countAlreadyHave=0;
			Gson gson=new Gson();
			
			while (true) {
				
				String strPredictionResults=br.readLine();
				if(strPredictionResults==null) break;
				counter++;
				
//				System.out.println(strPredictionResults);
								
				if(counter%countToPost==0) System.out.println(counter);
				
				PredictionResults predictionResults=Utilities.gson.fromJson(strPredictionResults,PredictionResults.class);
				
//				System.out.println(predictionResults.getEndpoint()+"\t"+predictionResults.getError());
				
				if(fixReports)
					fixPredictionResults(predictionResults);//fixes error where CAS was set to the SID for the test chemical in the similar chemicals table
				
//				System.out.println(Utilities.gson.toJson(predictionResults));
								
				PredictionDashboard pd=convertPredictionResultsToPredictionDashboard(predictionResults,hmModels,true,htCIDtoDsstoxRecordId);
				
				if (skipER) {
					if (predictionResults.getEndpoint().equals(DevQsarConstants.ESTROGEN_RECEPTOR_BINDING)
							|| predictionResults.getEndpoint().equals(DevQsarConstants.ESTROGEN_RECEPTOR_RBA))
						continue;
				}

				
//				System.out.println("*"+predictionResults.getError()+"*"+"\t"+predictionResults.getPredictionResultsPrimaryTable().getMessage());

				addApplicabilityDomain(predictionResults,pd,hmMethodAD);

				
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
				
				
//				if(counter==1) {
//					System.out.println(Utilities.gson.toJson(predictionResults));
//					String fileName="results.html";
//					String folder="data\\TEST1.0\\";
//					displayHTMLReport(predictionResults, fileName, folder);
//				}
//				if(true) return;
				
				//Store fixed report as string:
				String strPredictionResults2=gson.toJson(predictionResults);

				byte[] bytes=strPredictionResults2.getBytes();
				
//				String line2=PredictionReport.decompress(bytes);
//				System.out.println("Decompressed:"+line2);
				
//				System.out.println(pd.getDtxcid()+"\t"+pd.getModel().getName()+"\t"+pd.getPredictionValue()+"\t"+pd.getPredictionString()+"\t"+pd.getPredictionError());

				pd.setPredictionReport(new PredictionReport(pd, bytes, lanId));
				
//				predictionReportService.create(predictionReport);
								
				if(writeToDB && predictions.size()==countToPost) {
//					System.out.println(counter);
					predictionDashboardService.createSQL(predictions);
					predictions.clear();
				}
				
				
//				if(true) break;
			}
			br.close();

			if(writeToDB) predictionDashboardService.createSQL(predictions);//do last ones

			System.out.println("exited main loop");
			System.out.println("countAlreadyHave="+countAlreadyHave);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}

	private void addApplicabilityDomain(PredictionResults pr,PredictionDashboard pd, TreeMap<String, MethodAD> hmMethodAD) {
		
		QsarPredictedADEstimate adEstimate=new QsarPredictedADEstimate();
		adEstimate.setCreatedBy(lanId);
		adEstimate.setMethodAD(hmMethodAD.get(DevQsarConstants.Applicability_Domain_Combined));

		
		if(pd.getPredictionError()==null) {
			adEstimate.setApplicabilityValue(1.0);
			adEstimate.setConclusion("Inside");
			adEstimate.setReasoning("Compound is inside WebTEST applicability domains");
			
		} else {
//			if(pr.getDTXSID().equals("DTXSID7020005"))
//				System.out.println("Error not null, "+pr.getDTXCID()+"\t"+pr.getEndpoint()+", error="+pd.getPredictionError());
			
			adEstimate.setApplicabilityValue(0.0);
			adEstimate.setConclusion("Outside");
			adEstimate.setReasoning(pd.getPredictionError());
		}
		
		adEstimate.setPredictionDashboard(pd);
		List<QsarPredictedADEstimate>adEstimates=new ArrayList<>();
		adEstimates.add(adEstimate);
		
		pd.setQsarPredictedADEstimates(adEstimates);
		
	}

	private void displayHTMLReport(PredictionResults predictionResults, String fileName, String folder) {
		String htmlReport=RunFromSmiles.getReportAsHTMLString(predictionResults);
		//		System.out.println(htmlReport);
		try {
			File file=new File(folder+fileName);
			FileWriter fw=new FileWriter(file);
			fw.write(htmlReport);
			fw.flush();
			fw.close();
			Desktop desktop = Desktop.getDesktop();
			desktop.browse(file.toURI());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
			Hashtable<String,Long> htCIDtoDsstoxRecordId=dsstoxRecordService.getRecordIdHashtable(snapshot,"dtxcid");
			
			HashMap<String,Method> hmMethods=new HashMap<>();
			hmMethods.put("consensus_regressor",methodService.findByName("consensus_regressor"));
			hmMethods.put("consensus_classifier",methodService.findByName("consensus_classifier"));
			TreeMap<String, Model> hmModels = createModels(hmMethods);
			
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


	private TreeMap<String, Model> createModels(HashMap<String, Method> hmMethods) {
		TreeMap<String,Model> hmModels=CreatorScript.getModelsMap();
		
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
			
			if(hmModels.containsKey(modelName)) continue;
			
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
	
	static String getSoftwareName() {
		return "TEST"+version;
	}
	
	String getModelName(String propertyNameDB) {
		return propertyNameDB+" "+getSoftwareName();
	}

	private String getDatasetName(String propertyNameDB) {
		return getModelName(propertyNameDB);
	}
	
	
	

	
	PredictionDashboard convertPredictionResultsToPredictionDashboard(PredictionResults pr,TreeMap<String,Model>htModels,boolean convertPredictionMolarUnits,Hashtable<String,Long> htCIDtoDsstoxRecordId) {

		if(pr.getSmiles()==null) pr.setSmiles("N/A");
		
		PredictionDashboard pd=new PredictionDashboard();
		
		try {

			PredictionResultsPrimaryTable pt=pr.getPredictionResultsPrimaryTable();

			try {

				String propertyName=pr.getEndpoint();
				String propertyNameDB=getPropertyNameDB(propertyName);
				String modelName=getModelName(propertyNameDB);
				
				pr.setEndpoint(propertyNameDB);
				
//				System.out.println(Utilities.gson.toJson(htModels.get(modelName)));
				
				pd.setModel(htModels.get(modelName));
				
//				System.out.println("here");
								
				pd.setCanonQsarSmiles("N/A");
				pd.setDtxcid(pr.getDTXCID());
				
				DsstoxRecord dr=new DsstoxRecord();
				dr.setId(htCIDtoDsstoxRecordId.get(pr.getDTXCID()));
				pd.setDsstoxRecord(dr);

//				pd.setDtxsid(pr.getDTXSID());
//				pd.setDtxcid(pr.getDTXCID());
//				pd.setSmiles(pr.getSmiles());
				
				pd.setCreatedBy(lanId);
				
//				if(pr.getPredictionResultsPrimaryTable()!=null && pr.getPredictionResultsPrimaryTable().getMessage()!=null) {
//					System.out.println("message="+pr.getPredictionResultsPrimaryTable().getMessage());
//				}
				

				if (pr.getError()!=null && !pr.getError().isBlank()) {
					pd.setPredictionError(pr.getError());
				} else {
					setExperimentalPredictedValues(pr, convertPredictionMolarUnits, pd, pt);
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

	private void setExperimentalPredictedValues(PredictionResults pr, boolean convertPredictionMolarUnits,
			PredictionDashboard pd, PredictionResultsPrimaryTable pt) {
		
		
		if (pr.isBinaryEndpoint()) {
			
			if (pt.getPredToxValue().equals("N/A")) {
				pd.setPredictionError(pt.getMessage());
			} else {
				pd.setPredictionValue(Double.parseDouble(pt.getPredToxValue()));
				pd.setPredictionString(pt.getPredValueEndpoint());
			}
			
			if (!pt.getExpToxValue().equals("N/A")) {
				pd.setExperimentalValue(Double.parseDouble(pt.getExpToxValue()));
				pd.setExperimentalString(pt.getExpToxValueEndpoint());
//				System.out.println(pr.getDTXCID()+"\t"+pr.getEndpoint()+"\tExperimental value="+pd.getExperimentalValue()+",Experimental string="+pd.getExperimentalString());
			}
			
			
		} else if (pr.isLogMolarEndpoint()) {

			if (pt.getPredToxValue().equals("N/A")) {
				pd.setPredictionError(pt.getMessage());
			} else {
				if (convertPredictionMolarUnits)
					pd.setPredictionValue(convertLogMolarUnits(pr.getEndpoint(), pt.getPredToxValue()));
				else {
					pd.setPredictionValue(Double.parseDouble(pt.getPredToxValue()));
//								pd.prediction_units=pt.getMolarLogUnits();
				}
			}
			
			if (!pt.getExpToxValue().equals("N/A")) {
				if (convertPredictionMolarUnits)
					pd.setExperimentalValue(convertLogMolarUnits(pr.getEndpoint(), pt.getExpToxValue()));
				else {
					pd.setExperimentalValue(Double.parseDouble(pt.getExpToxValue()));
				}
			}
			
//			System.out.println(pr.getDTXCID()+"\t"+pr.getEndpoint()+"\tExperimental value="+pd.getExperimentalValue());
			
			
		} else {

			if (pt.getPredToxValMass().equals("N/A")) {
				pd.setPredictionError(pt.getMessage());
			} else {
				pd.setPredictionValue(Double.parseDouble(pt.getPredToxValMass()));
			}
			
			if (!pt.getExpToxValMass().equals("N/A")) {
				pd.setExperimentalValue(Double.parseDouble(pt.getExpToxValMass()));
//				System.out.println(pr.getDTXCID()+"\t"+pr.getEndpoint()+"\tExperimental value="+pd.getExperimentalValue());
			}
		}
	}

	
	private static Double convertLogMolarUnits(String endpoint, String massValue) {
		
		if (endpoint.equals(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50)
				|| endpoint.equals(DevQsarConstants.FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50)
				|| endpoint.equals(DevQsarConstants.FORTY_EIGHT_HR_TETRAHYMENA_PYRIFORMIS_IGC50)
				|| endpoint.contains(DevQsarConstants.WATER_SOLUBILITY)
				|| endpoint.equals(DevQsarConstants.ORAL_RAT_LD50)) {
			return Math.pow(10.0,-Double.parseDouble(massValue));
		} else if (endpoint.equals(DevQsarConstants.BCF) 
				|| endpoint.contains(DevQsarConstants.VAPOR_PRESSURE)
				|| endpoint.contains(DevQsarConstants.VISCOSITY)
				|| endpoint.equals(DevQsarConstants.ESTROGEN_RECEPTOR_RBA)) {
			return Math.pow(10.0,Double.parseDouble(massValue));
		} else {
			System.out.println("Not handled:"+endpoint);
			return null;
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
	
	void deleteTEST_PredictionsSimple() {
		System.out.print("Deleting from predictions_dashboard");

		String sql="delete from qsar_models.predictions_dashboard pd using qsar_models.models m\n"+
		"where pd.fk_model_id = m.id and m.fk_source_id=2;";
		
		SqlUtilities.runSQLUpdate(SqlUtilities.getConnectionPostgres(), sql);

		System.out.println("Done");
	}
	
	

	
	/* Possible prediction errors:
    "The consensus prediction for this chemical is considered unreliable since only one prediction can only be made"
    "No prediction could be made due to applicability domain violation"
    "FindPaths"
    "FindRings"
    "Only one nonhydrogen atom"
    "Molecule does not contain carbon"
    "Molecule contains unsupported element"
    "Multiple molecules"
	 */
	void loadSDE_Json_File() {
		String folder="data\\TEST1.0\\";
		String filepath=folder+"predictionResults.json.gz";
		
		
		CompareStandaloneToSDE c=new CompareStandaloneToSDE();
		
		try {
			
			InputStream fileStream = new FileInputStream(filepath);
			InputStream gzipStream = new GZIPInputStream(fileStream);
			Reader decoder = new InputStreamReader(gzipStream,  StandardCharsets.UTF_8);
			BufferedReader br = new BufferedReader(decoder);
			
			List<String> errors=new ArrayList<>();
			
			int index=1383;
			
			for (int i=1;i<=5000;i++) {
				
				String Line=br.readLine();
				PredictionResults pr=Utilities.gson.fromJson(Line, PredictionResults.class);
				
				if (pr.getError()!=null) {					
					if(!errors.contains(pr.getError())){
						errors.add(pr.getError());
					}
//					System.out.println(pr.getDTXSID()+"\t"+pr.getEndpoint()+"\t"+pr.getError());
				}
//				System.out.println(Utilities.gson.toJson(pr));
				
//				if(i%1000==0) System.out.println(i);
				
//				if(pr.getError()==null || pr.getError().isBlank()) {
//					System.out.println(i+"\t"+pr.getDTXSID()+"\t"+pr.getEndpoint()+"\t"+pr.getPredictionResultsPrimaryTable().getPredToxValMass());
//					System.out.println(Utilities.gson.toJson(pr));
//					break;
//				}
				
				c.fixPredictionResultsSDE(pr);
				
				if(i==index) {
					System.out.println(Utilities.gson.toJson(pr));
					String fileName="results.html";
					displayHTMLReport(pr, fileName, folder);
				}
				
			}
			
			for (String error:errors) {
				System.out.println(error);
			}
			
			
			br.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	void deleteTEST_Records() {
		DatabaseUtilities o=new DatabaseUtilities();
		
		o.deleteRecordsSimple("prediction_reports",minModelId,maxModelID);
		o.deleteRecordsSimple("qsar_predicted_ad_estimates",minModelId,maxModelID);
		o.deletePredictionsSimple(minModelId,maxModelID);
		
	}
	

	PredictionResults getPredictionResultsFromPredictionReport(String id,String modelName) {
		
		String idCol="dtxcid";
		if (id.contains("SID")) idCol="dtxsid";
		
				
		String sql="select file from qsar_models.prediction_reports pr\r\n"
				+ "join qsar_models.predictions_dashboard pd on pr.fk_predictions_dashboard_id = pd.id\r\n"
				+ "join qsar_models.models m on pd.fk_model_id = m.id\r\n"
				+ "join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id\r\n"
				+ "where dr."+idCol+"='"+id+"' and dr.fk_dsstox_snapshot_id=1 and m.name='"+modelName+"';";
				
		try {
			Connection conn=SqlUtilities.getConnectionPostgres();
			
			ResultSet rs=SqlUtilities.runSQL2(conn, sql);

			if (rs.next()) {
				String json=new String(rs.getBytes(1));
				return Utilities.gson.fromJson(json,PredictionResults.class);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	public void viewReportsFromDatabase(String id) {

		String folder="data\\opera\\reports";

		//TODO just add list of TEST endpoints to DevQsarConstants similar to OPERA
		List<String> propertyNames=TESTConstants.getFullEndpoints(null);
		List<String> propertyNamesDB=new ArrayList<>();
		
		for(String propertyName:propertyNames) {
			propertyNamesDB.add(getPropertyNameDB(propertyName));
		}
		
		for (String propertyName:propertyNamesDB) {
			
			String modelName=getModelName(propertyName);
//			System.out.println(modelName);
			
			PredictionResults pr=getPredictionResultsFromPredictionReport(id,modelName);
			
			if(pr==null) {
				System.out.println("No report for "+propertyName);
				continue;
			}
			
			if(propertyName.equals(DevQsarConstants.WATER_SOLUBILITY)) {
				System.out.println(Utilities.gson.toJson(pr));
			}
			
			String filename=pr.getDTXCID()+"_"+pr.getEndpoint()+".html";
			displayHTMLReport(pr, filename, folder);
		}
		
	}
	
	public static void main(String[] args) {
		PredictionDashboardScriptTEST pds=new PredictionDashboardScriptTEST();
		
//		pds.deleteTEST_Records();
//		pds.runNewPredictionsFromTextFile();		
		
		
		boolean fixReports=false;
		boolean skipER=true;
		boolean writeToDB=true;
//		pds.createRecordsFromJsonFile("data\\TEST1.0\\sample compounds.json",fixReports,skipER,writeToDB);

		pds.viewReportsFromDatabase("DTXCID505");
		
		
		//************************************************************************************
		
//		pds.loadSDE_Json_File();
		
//		pds.deleteTEST_Predictions2();

//		pds.createDatasets();//TODO need to add the datapoints
		
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

	private void runNewPredictionsFromTextFile() {

		RunFromSmiles.debug=false;
		
		//Need to set path of structure db because it's a relative path and this is calling TEST project:
		ResolverDb2.setSqlitePath("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18_EPA_Github\\databases\\snapshot.db");
		
		boolean createReports=true;//whether to store report
		boolean createDetailedReports=false;//detailed reports have lots more info and creates more html files
		String method =TESTConstants.ChoiceConsensus;//what QSAR method being used (default- runs all methods and takes average)

		List<String>endpoints=TESTConstants.getFullEndpoints(null);
//		endpoints= Arrays.asList(TESTConstants.ChoiceFHM_LC50);
		
		Gson gson=new Gson();
		try {
			
			String filepath="data\\TEST1.0\\sample compounds.txt";
			String filepathOut="data\\TEST1.0\\sample compounds.json";
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			br.readLine();
			
			FileWriter fw=new FileWriter(filepath.replace(".txt", ".json"));
			
			int count=0;
			
			while (true) {
				String Line=br.readLine();
				if(Line==null) break;
				
				count++;
				
				String [] vals=Line.split("\t");
				
				String dtxsid=vals[0];
				String dtxcid=vals[1];
				String casrn=vals[2];
				String smiles=vals[3];
				
				
				AtomContainer molecule=RunFromSmiles.createMolecule(smiles, dtxsid, dtxcid, casrn);
				

				AtomContainerSet acs=new AtomContainerSet();
				
				acs.addAtomContainer(molecule);
//				acs.addAtomContainer(RunFromSmiles.createMolecule("NC1=C(C=CC(=C1)NC(=O)C)OCC", "DTXSID7020053","17026-81-2"));
				
				
				List<PredictionResults>listPR=RunFromSmiles.runEndpointsAsList(acs, endpoints, method,createReports,createDetailedReports,DSSToxRecord.strSID);		
				
				System.out.println(count+"\t"+smiles+"\t"+listPR.size());
				
//				if (smiles.contains(".")) {
//					System.out.println(Utilities.gson.toJson(listPR.get(0)));	
//				}
				
				for (PredictionResults pr:listPR) {
					String json=gson.toJson(pr);
					fw.write(json+"\r\n");
				}
				fw.flush();
				
//				System.out.println(dtxsid+"\t"+smiles+"\t"+molecule.getAtomCount());
				
//				if(true) break;
				
			}
			
			br.close();
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
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
	
	
	private void fixPredictionResultsSDE(PredictionResults pr) {
		
		for(PredictionIndividualMethod pred:pr.getIndividualPredictionsForConsensus().getConsensusPredictions()) {
			pred.setMethod(TESTConstants.getFullMethod(pred.getMethod()));
		}
		
		pr.setEndpoint(TESTConstants.getFullEndpoint(pr.getEndpoint()));
		
	}
	

}

