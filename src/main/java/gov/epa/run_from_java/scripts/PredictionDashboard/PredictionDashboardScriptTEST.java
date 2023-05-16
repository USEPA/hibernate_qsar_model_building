package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Application.model.PredictionResultsPrimaryTable;
import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport;
import gov.epa.databases.dev_qsar.qsar_models.service.MethodServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionReportServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;


/**
* @author TMARTI02
*/
public class PredictionDashboardScriptTEST  {
	
	MethodServiceImpl methodService=new MethodServiceImpl();
	PredictionDashboardServiceImpl predictionDashboardService=new PredictionDashboardServiceImpl();
	PredictionReportServiceImpl predictionReportService=new PredictionReportServiceImpl();

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
			
			for (String DTXSID:htResultsAll.keySet()) {
				
				List<PredictionResults>listPredictionResults=htResultsAll.get(DTXSID);
				
				for (PredictionResults pr:listPredictionResults) {
					PredictionDashboard pd=convertPredictionResultsToPredictionDashboard(pr,hmModels,true);
					predictionDashboardService.create(pd);
//					System.out.println(Utilities.gson.toJson(pd));
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	
	void runFromSampleJsonFile(String filepathJson) {
		
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
				PredictionDashboard pd=convertPredictionResultsToPredictionDashboard(pr,hmModels,true);
				predictionDashboardService.create(pd);
//				System.out.println(Utilities.gson.toJson(pd));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	
	
	void runFromDashboardJsonFile(String filepathJson) {
		
		
		try {
			
			//TODO add code to create automatically new methods if they arent in the methods table in db
			HashMap<String,Method> hmMethods=new HashMap<>();
			hmMethods.put("consensus_regressor",methodService.findByName("consensus_regressor"));
			hmMethods.put("consensus_classifier",methodService.findByName("consensus_classifier"));
			
			HashMap<String, Model> hmModels = createModels(hmMethods);
			
			BufferedReader br=new BufferedReader(new FileReader(filepathJson));
			
			int count=0;

//			for (String line:lines) {
			while (true) {
				String strPredictionResults=br.readLine();
				if(strPredictionResults==null) break;
				count++;
				
				PredictionResults predictionResults=Utilities.gson.fromJson(strPredictionResults,PredictionResults.class);
//				System.out.println(Utilities.gson.toJson(pr));
				
				PredictionDashboard pd=convertPredictionResultsToPredictionDashboard(predictionResults,hmModels,true);
				pd=predictionDashboardService.create(pd);

//				if(predictionDashboard.getPredictionError()!=null) continue;//For testing
				
//				byte[] bytes=PredictionReport.compress(strPredictionResults);
//				byte[] bytes=strPredictionResults.getBytes(StandardCharsets.ISO_8859_1);
				byte[] bytes=strPredictionResults.getBytes();
				
//				String line2=PredictionReport.decompress(bytes);
//				System.out.println("Decompressed:"+line2);
				
				
				System.out.println(pd.getDtxcid()+"\t"+pd.getModel().getName()+"\t"+pd.getPredictionValue()+"\t"+pd.getPredictionString()+"\t"+pd.getPredictionError());

				PredictionReport predictionReport=new PredictionReport(pd, bytes, lanId);
				predictionReportService.create(predictionReport);
				
//				if(true) break;
				
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	
	
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


	



	private HashMap<String, Model> createModels(HashMap<String, Method> hmMethods) {
		HashMap<String,Model> hmModels=new HashMap<>();
		
		for (String propertyName:propertyNames) {
			String source=getSoftwareName();
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
		
		
		if (propertyNameDB.equals(DevQsarConstants.NINETY_SIX_HOUR_LC50)) {
			return "96 hour fathead minnow LC50 data compiled from ECOTOX for the TEST software";
		} else if (propertyNameDB.equals(DevQsarConstants.FORTY_EIGHT_HR_DM_LC50)) {
			return("48 hour Daphnia magna LC50 data compiled from ECOTOX for the TEST software");
		} else if (propertyNameDB.equals(DevQsarConstants.FORTY_EIGHT_HR_IGC50)) {
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
			return DevQsarConstants.NINETY_SIX_HOUR_LC50;
		} else if (propertyName.equals("Daphnia magna LC50 (48 hr)")) {
			return DevQsarConstants.FORTY_EIGHT_HR_DM_LC50;
		} else if (propertyName.equals("T. pyriformis IGC50 (48 hr)")) {
			return DevQsarConstants.FORTY_EIGHT_HR_IGC50;
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
	
	
	

	
	PredictionDashboard convertPredictionResultsToPredictionDashboard(PredictionResults pr,HashMap<String,Model>htModels,boolean convertPredictionMolarUnits) {

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
				pd.setDtxsid(pr.getDTXSID());
				pd.setDtxcid(pr.getDTXCID());
				pd.setSmiles(pr.getSmiles());
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
		
		if (name.equals(DevQsarConstants.NINETY_SIX_HOUR_LC50)
				|| name.equals(DevQsarConstants.FORTY_EIGHT_HR_DM_LC50)
				|| name.equals(DevQsarConstants.FORTY_EIGHT_HR_IGC50)
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
		
		HashMap<String, String>hmUnitsDataset=DevQsarConstants.getDatasetFinalUnitsMap();
		HashMap<String, String>hmUnitsDatasetContributor=DevQsarConstants.getContributorUnitsMap();
		
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
	
	

	public static void main(String[] args) {
		PredictionDashboardScriptTEST pds=new PredictionDashboardScriptTEST();

		pds.version="5.1.3";
		
//		pds.createDatasets();//TODO need to add the datapoints

//		String filePathJson="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18\\reports\\sample_predictions.json";
//		pds.runFromSampleJsonFileHashtable(filePathJson,SoftwareVersion);

//		String filePathJson="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18\\reports\\TEST_results_all_endpoints_snapshot_compounds1.json";
//		pds.runFromSampleJsonFile(filePathJson);

		
//		String filePathJson="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\RunTestCalculationsFromJar\\reports\\TEST_results_all_endpoints_snapshot_compounds4.json";
		String filePathJson="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\RunTestCalculationsFromJar\\reports\\sample.json";
//		pds.runFromDashboardJsonFile(filePathJson);
		
		pds.testRetrievePredictionReport();
				

//		String filePathJson="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\RunTestCalculationsFromJar\\reports\\TEST_results_all_endpoints_snapshot_compounds4.json";
//		String filePathJson2="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\RunTestCalculationsFromJar\\reports\\sample.json";
//		pds.extractRecords(filePathJson,filePathJson2,"DTXCID0080822");
		
		//TODO create createSQL (List<PredictionDashboard> predictions)- this way you can create predictions which arent in the models table
		//TODO make SQL query to assemble the results for displaying on dashboard...
	}

	

}
