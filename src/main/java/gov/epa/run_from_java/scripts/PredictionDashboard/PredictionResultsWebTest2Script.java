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
import gov.epa.run_from_java.scripts.PredictionDashboard.valery.ValeryResponse;
import gov.epa.web_services.ValeryPredictionWebService;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;

/**
* @author TMARTI02
*/
public class PredictionResultsWebTest2Script {
	

	
	public static void main(String[] args) {
//		r.runChemical("DTXSID3039242");
		
		int maxCount=20;//number of chemicals to run
		boolean skipMissingSID=false;//skip entries without an SDF
		
		String folderSrc="C:\\Users\\cramslan\\Documents\\code\\java\\hibernate_qsar_modelbuilding\\data\\dsstox\\sdf\\";
		String fileName="snapshot_compounds4";
		String fileNameSDF = fileName + ".sdf";
		String filepathSDF=folderSrc+fileNameSDF;
		
		String strOutputFolder="reports/prediction_json";
		new File(strOutputFolder).mkdirs();
		String propertyAbbreviation = "WS";
		String outputFileName= fileName + "-" + propertyAbbreviation + ".json";
		String destJsonPath=strOutputFolder+File.separator+outputFileName;
		
		ArrayList<PredictionDashboard> predictionDashboards = new ArrayList<>();
		ArrayList<String> datasetIds = new ArrayList<String>();
		//31 WS, 44 LLNA

		datasetIds.add("31");
		// datasetIds.add("44");
		String modelSetId = "2";

		runSDF(filepathSDF, destJsonPath, skipMissingSID, maxCount, datasetIds, modelSetId, predictionDashboards);
		
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
	public static void createPredictionDashboard(ValeryResponse[] valeryResponse, ArrayList<String> originalSmiles, int originalSmilesIndex, ArrayList<PredictionDashboard> predictionDashboards,
			DsstoxCompoundService dsstoxCompoundService, ModelService modelService, DashboardPredictionUtilities dpu, String dtxsid, String dtxcid, Boolean isSalt) {
		PredictionDashboard p = new PredictionDashboard();
		p.setCreatedBy("cramslan");
		p.setSmiles(originalSmiles.get(originalSmilesIndex));
		p.setCanonQsarSmiles(valeryResponse[0].predictions.get(originalSmilesIndex).chemical.smiles);
		p.setDtxcid(dtxcid);
		p.setDtxsid(dtxsid);
		ArrayList<PredictionDashboard> predictionDashboardsAllModels = new ArrayList<>();
		String datasetName = valeryResponse[0].dataset.dataset_name;

		
		if (isSalt) {
			p.setPredictionError("salt compound");
			predictionDashboardsAllModels.add(p);
			return;
		}
		String valuesJSON = dpu.gson.toJson(valeryResponse[0].predictions.get(originalSmilesIndex).values);
		JSONObject valuesJO = new JSONObject(valuesJSON);
		
		for (int i = 0; i < valeryResponse[0].dataset.methods.size(); i++) {
			System.out.println(i);
			
			PredictionDashboard p1 = p;
			String methodName = valeryResponse[0].dataset.methods.get(i).name;
			if (methodName.equals("consensus")) {
				methodName = "avg";
			}
			int modelId = valeryResponse[0].dataset.methods.get(i).model_id;
			Long newmodelId = modelIdRemapping(modelId);
			System.out.println("new model id =" + newmodelId);
			Model model = modelService.findById(newmodelId);
			System.out.println("model is null?" + (model == null));
			p1.setModel(model);
			if (!(valuesJO.has(methodName))) {
				continue;
			}
			Double predictionValue = valuesJO.getDouble(methodName);
			

			p1.setPredictionString(String.valueOf(predictionValue));
			p1.setPredictionValue(predictionValue);
			PredictionDashboard p2 = convertUnits(datasetName, p1);
			
			predictionDashboardsAllModels.add(p1);
		}
		
		predictionDashboards.addAll(predictionDashboardsAllModels);
		
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
	
	
	public static void runSDF(String SDFFilePath, String destJsonPath,boolean skipMissingSID,int maxCount,
			ArrayList<String> datasetIds, String modelSetId, 		ArrayList<PredictionDashboard> predictionDashboards
) {
		DashboardPredictionUtilities dpu = new DashboardPredictionUtilities();
		
		AtomContainerSet acs= dpu.readSDFV3000(SDFFilePath);
		
		AtomContainerSet acs2 = dpu.filterAtomContainerSet(acs, skipMissingSID, maxCount);

		Iterator<IAtomContainer> iterator= acs2.atomContainers().iterator();

		ValeryPredictionWebService valery = new ValeryPredictionWebService("https://hcd.rtpnc.epa.gov/api/");
		
		ModelService modelService = new ModelServiceImpl();

		DsstoxCompoundService dsstoxCompoundService = new DsstoxCompoundServiceImpl();

		DatasetService datasetService = new DatasetServiceImpl();
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
			System.out.print("response body=" + response.getBody());
			ValeryResponse[] vr = dpu.gson.fromJson(response.getBody(), ValeryResponse[].class);
			

			for (int i = 0; i < smilesList.size(); i++) {
				createPredictionDashboard(vr, smilesList, i, predictionDashboards, dsstoxCompoundService, modelService, dpu, dtxsid, dtxcid, isSalt);
			}
			
		}

//		System.out.println(Utilities.gson.toJson(allResults));
		//TODO do API call for json Report, store that too
		
	}
	
	private static PredictionDashboard convertUnits(String dataset, PredictionDashboard pd) {
		if (dataset.toLowerCase().contains("water")) {
			pd.setPredictionValue(Math.pow(10.0,pd.getPredictionValue()));
		}
		return pd;
	}
	
	
	
	private static boolean isSalt(AtomContainer ac) {
		AtomContainerSet moleculeSet = (AtomContainerSet) ConnectivityChecker.partitionIntoMolecules(ac);
		return (moleculeSet.getAtomContainerCount() > 2);
	}

}
	

