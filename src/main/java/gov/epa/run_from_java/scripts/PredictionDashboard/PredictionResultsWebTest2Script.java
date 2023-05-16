package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.gson.Gson;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardService;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.GenericSubstanceCompound;
import gov.epa.databases.dsstox.service.DsstoxCompoundService;
import gov.epa.databases.dsstox.service.DsstoxCompoundServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.valery.Chemical;
import gov.epa.run_from_java.scripts.PredictionDashboard.valery.Dataset;
import gov.epa.run_from_java.scripts.PredictionDashboard.valery.ValeryBody;
import gov.epa.run_from_java.scripts.PredictionDashboard.valery.WebTEST2PredictionResponse;
import gov.epa.web_services.ValeryPredictionWebService;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;

/**
* @author TMARTI02
*/
public class PredictionResultsWebTest2Script {
	PredictionDashboardService predictionDashboardService = new PredictionDashboardServiceImpl();
	ValeryPredictionWebService valery = new ValeryPredictionWebService("https://hcd.rtpnc.epa.gov/api/");
	
	ModelService modelService = new ModelServiceImpl();

	DsstoxCompoundService dsstoxCompoundService = new DsstoxCompoundServiceImpl();

	DatasetService datasetService = new DatasetServiceImpl();
	
	DashboardPredictionUtilities dpu = new DashboardPredictionUtilities();
	

	public static void main(String[] args) {
//		r.runChemical("DTXSID3039242");
		PredictionResultsWebTest2Script script = new PredictionResultsWebTest2Script();
		// script.runPredictionSnapshot();
		WebTEST2PredictionResponse predictSingleChemical = script.valery.predictSingleChemical("Br[Ga](Br)Br", "31", "2");
	}
	
	private void runPredictionSnapshot() {
		int maxCount=20;//number of chemicals to run
		boolean skipMissingSID=false;//skip entries without an SDF
		
		String folderSrc="C:\\Users\\cramslan\\Documents\\code\\java\\hibernate_qsar_modelbuilding\\data\\dsstox\\sdf\\";
		String fileName="snapshot_compounds7";
		String fileNameSDF = fileName + ".sdf";
		String filepathSDF=folderSrc+fileNameSDF;
		
		String strOutputFolder="reports/prediction_json";
		new File(strOutputFolder).mkdirs();
		String propertyAbbreviation = "WS";
		String outputFileName= fileName + "-" + propertyAbbreviation + ".json";
		String destJsonPath=strOutputFolder+File.separator+outputFileName;
		
		ArrayList<String> datasetIds = new ArrayList<String>();
		//31 WS, 44 LLNA

		datasetIds.add("31");
		// datasetIds.add("44");
		String modelSetId = "2";

		ArrayList<PredictionDashboard> predictionDashboards = runSDF(filepathSDF, destJsonPath, skipMissingSID, maxCount, datasetIds, modelSetId);
		
		DashboardPredictionUtilities dpu = new DashboardPredictionUtilities();
		PredictionDashboardService predictionDashboardService = new PredictionDashboardServiceImpl();
		for (int i = 0; i < predictionDashboards.size(); i++) {
			try {
				predictionDashboardService.create(predictionDashboards.get(i));
			} catch (Exception ex) {
				continue;
			}
		}

	}


	// provide it 
	public ArrayList<PredictionDashboard> createPredictionDashboard(WebTEST2PredictionResponse valeryResponse, Boolean isSalt) {
		ArrayList<PredictionDashboard> predictionDashboards = new ArrayList<>();
		PredictionDashboard p = new PredictionDashboard();
		p.setCreatedBy("cramslan");
		p.setSmiles(valeryResponse.predictions.get(0).chemical.originalSmiles);
		p.setCanonQsarSmiles(valeryResponse.predictions.get(0).chemical.smiles);
		p.setDtxcid(valeryResponse.predictions.get(0).chemical.dtxcid);
		p.setDtxsid(valeryResponse.predictions.get(0).chemical.dtxsid);
		String datasetName = valeryResponse.dataset.dataset_name;
		String unit = valeryResponse.dataset.unit;

		if (isSalt) {
			p.setPredictionError("salt compound");
			predictionDashboards.add(p);
			return predictionDashboards;
		}
		String valuesJSON = dpu.gson.toJson(valeryResponse.predictions.get(0).values);
		JSONObject valuesJO = new JSONObject(valuesJSON);
		System.out.println("methods size=" + valeryResponse.dataset.methods.size());
		for (int i = 0; i < valeryResponse.dataset.methods.size(); i++) {			
			PredictionDashboard p1 = p;
			String methodName = valeryResponse.dataset.methods.get(i).name;
			if (methodName.equals("consensus")) {
				methodName = "avg";
			}
			int modelId = valeryResponse.dataset.methods.get(i).model_id;
			Long newmodelId = modelIdRemapping(modelId);
			System.out.println("new model id =" + newmodelId);
			Model model = modelService.findById(newmodelId);
			System.out.println("model is null?" + (model == null));
			p1.setModel(model);
			if (!(valuesJO.has(methodName))) {
				continue;
			}
			Double predictionValueOriginalUnits = valuesJO.getDouble(methodName);
			Double predictionValueConverted = convertUnits(datasetName, predictionValueOriginalUnits);
			p1.setPredictionString(String.valueOf(predictionValueConverted) + " " + unit);
			p1.setPredictionValue(predictionValueConverted);			
			predictionDashboards.add(p1);
		}
		
		return predictionDashboards;
		
	}
	
	public static Long modelIdRemapping(int idInt) {
		switch(idInt) {
		case(152):
			return 1310L;
		case(151):
			return 1303L;
		case(253):
			return 1315L;
		case(254):
			return 1446L;
		case(185):
			return 1444L;
		case(183):
			return 1445L;
		case(182):
			return 1443L;
		}
		return 0L;
		
	}
	
	
	public static ArrayList<Chemical> addChemicalsToBody(ArrayList<String> smiles) {
		ArrayList<Chemical> chemicals = new ArrayList<>();
		for (int i = 0; i < smiles.size(); i++) {
			Chemical chemical = new Chemical();
			chemical.setSmiles(smiles.get(i));
			chemicals.add(chemical);
		}
		return chemicals;
	}
	
	public static ArrayList<Dataset> addDatasetIdsToBody(ArrayList<String> datasetIds) {
		ArrayList<Dataset> datasets = new ArrayList<Dataset>();
		for (int i = 0; i < datasetIds.size(); i++) {
			Dataset dataset = new Dataset();
			dataset.setDatasetId(datasetIds.get(i));
			datasets.add(dataset);
		}
		return datasets;
	}
	
	
	public ArrayList<PredictionDashboard> runSDF(String SDFFilePath, String destJsonPath,boolean skipMissingSID,int maxCount,
			ArrayList<String> datasetIds, String modelSetId) {
		ArrayList<PredictionDashboard> predictionDashboards = new ArrayList<>();
		DashboardPredictionUtilities dpu = new DashboardPredictionUtilities();
		
		AtomContainerSet acs= dpu.readSDFV3000(SDFFilePath);
		
		AtomContainerSet acs2 = dpu.filterAtomContainerSet(acs, skipMissingSID, maxCount);

		Iterator<IAtomContainer> iterator= acs2.atomContainers().iterator();

		// if datasets were linked up, this is where we'd get dataset from		
		
		System.out.println(acs2.getAtomContainerCount());

		int count=0;
		
		while (iterator.hasNext()) {
			count++;
			
			AtomContainer ac=(AtomContainer) iterator.next();
			String smiles=ac.getProperty("smiles");//TODO should we convert ac to smiles or just use smiles in DSSTOX?
			String dtxcid = ac.getProperty("DTXCID");
			if (dtxcid == null) {
				dtxcid = "N/A";
			}
			String dtxsid = ac.getProperty("DTXSID");
			if (dtxsid == null) {
				dtxsid = "N/A";
			}
			Boolean isSalt = isSalt(ac);
			System.out.println(isSalt);
			System.out.println("***"+count+"\t"+smiles);
			
			ArrayList<String> smilesList = new ArrayList<String>();
			smilesList.add(smiles);
//			smiles.add("CCCO");
			ValeryBody vb = new ValeryBody();
			vb.setChemicals(addChemicalsToBody(smilesList));
			vb.setDatasets(addDatasetIdsToBody(datasetIds));
			vb.setModelSetId(modelSetId);
			vb.setWorkflow("qsar-ready");
			String json = dpu.gson.toJson(vb);
			HttpResponse<String> response = valery.predict(json);
			if (response.getStatus() == 400) {
				System.out.println("cannot predict " + smiles);
				continue;
			}
			System.out.print("response body=" + response.getBody());
			WebTEST2PredictionResponse[] vr = dpu.gson.fromJson(response.getBody(), WebTEST2PredictionResponse[].class);
			WebTEST2PredictionResponse valeryResponse = vr[0];
			valeryResponse.predictions.get(0).chemical.dtxcid = dtxcid;
			valeryResponse.predictions.get(0).chemical.dtxsid = dtxsid;
			valeryResponse.predictions.get(0).chemical.originalSmiles = smiles;

			ArrayList<PredictionDashboard> chemicalPredictionDashboard = createPredictionDashboard(valeryResponse, isSalt);
			
		}

//		System.out.println(Utilities.gson.toJson(allResults));
		//TODO do API call for json Report, store that too
		return predictionDashboards;
	}
	
	private static Double convertUnits(String dataset, Double predictionValue) {
		Double convertedValue;
		if (dataset.toLowerCase().contains("water")) {
			convertedValue = (Math.pow(10.0, predictionValue));
			return convertedValue;
		}
		return predictionValue;
	}
	
	
	
	private static boolean isSalt(AtomContainer ac) {
		AtomContainerSet moleculeSet = (AtomContainerSet) ConnectivityChecker.partitionIntoMolecules(ac);
		return (moleculeSet.getAtomContainerCount() > 2);
	}

}
	

