package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.gson.Gson;

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
	
	Gson gson = new Gson();

	
	public static void main(String[] args) {
//		r.runChemical("DTXSID3039242");
		
		int maxCount=10;//number of chemicals to run
		boolean skipMissingSID=false;//skip entries without an SDF
		
		String folderSrc="C:\\Users\\cramslan\\Documents\\code\\java\\hibernate_qsar_modelbuilding\\data\\dsstox\\sdf\\";
		String fileNameSDF="snapshot_compounds3.sdf";
		String filepathSDF=folderSrc+fileNameSDF;
		
		String strOutputFolder="reports/prediction_json";
		new File(strOutputFolder).mkdirs();
		
		String outputFileName="snapshot_compounds1-WS.json";
		String destJsonPath=strOutputFolder+File.separator+outputFileName;
		
		ArrayList<PredictionDashboard> predictionDashboards = new ArrayList<>();
		ArrayList<String> datasetIds = new ArrayList<String>();
		//31 WS, 44 LLNA

		datasetIds.add("31");
		datasetIds.add("44");



		runSDF(filepathSDF, 50L, destJsonPath, skipMissingSID, maxCount, datasetIds, "2", predictionDashboards);
		
		PredictionDashboardService predictionDashboardService = new PredictionDashboardServiceImpl();
		predictionDashboardService.createBatch(predictionDashboards);



	}
	
	public static void main2(String[] args) {
		PredictionResultsWebTest2Script script = new PredictionResultsWebTest2Script();
		Gson gson = new Gson();
		ArrayList<String> smiles = new ArrayList<String>();
		smiles.add("CCCC(=O)OC(C)OP(=O)(OCC)OCC");
		HashMap<String,String> cidBySmiles = new HashMap<>();
		cidBySmiles.put("CCCC(=O)OC(C)OP(=O)(OCC)OCC", "DTXCID001000007");
//		smiles.add("CCCO");
		ArrayList<String> datasetIds = new ArrayList<String>();
		datasetIds.add("31");
		ValeryBody vb = new ValeryBody();
		vb.setChemicals(addChemicalsToBody(smiles));
		vb.setDatasets(addDatasetIdsToBody(datasetIds));
		vb.setModelSetId("2");
		vb.setWorkflow("qsar-ready");
		String json = gson.toJson(vb);
		System.out.println(json);
		// printthings(jo,"chemicals");
		ValeryPredictionWebService valery = new ValeryPredictionWebService("https://hcd.rtpnc.epa.gov/api/");
		HttpResponse<String> response = valery.predict(json);
		System.out.print("response body=" + response.getBody());
		ValeryResponse[] vr = gson.fromJson(response.getBody(), ValeryResponse[].class);
		HashMap<String, String> predictions = new HashMap<>();
		for (int i = 0; i < vr[0].predictions.size(); i++) {
			predictions.put(vr[0].predictions.get(i).chemical.smiles, gson.toJson(vr[0].predictions.get(i).values)); // chemical[i].smiles, json);
		}
		
		HashMap<String, Long> methodsMap = new HashMap<>();
		for (int i = 0; i < vr[0].dataset.methods.size(); i++) {
			methodsMap.put(vr[0].dataset.methods.get(i).name, vr[0].dataset.methods.get(i).model_id);
		}
		
		
		ArrayList<PredictionDashboard> predictionDashboards = new ArrayList<>();
		DsstoxCompoundService dsstoxCompoundService = new DsstoxCompoundServiceImpl();
		
		ModelService modelService = new ModelServiceImpl();
		
//		for (int i = 0; i < smiles.size(); i++) {
//			script.createPredictionDashboard(vr, smiles, i, predictionDashboards, dsstoxCompoundService, modelService, cidBySmiles);
//		}
		PredictionDashboardService predictionDashboardService = new PredictionDashboardServiceImpl();
		for (PredictionDashboard predictionDashboard:predictionDashboards) {
			predictionDashboardService.create(predictionDashboard);
		}
		


	}
	
	// provide it 
	public static void createPredictionDashboard(ValeryResponse[] valeryResponse, ArrayList<String> originalSmiles, int originalSmilesIndex, ArrayList<PredictionDashboard> predictionDashboards,
			DsstoxCompoundService dsstoxCompoundService, ModelService modelService, HashMap<String,String> cidBySmiles, DashboardPredictionUtilities dpu) {
		PredictionDashboard p = new PredictionDashboard();
		p.setCreatedBy("cramslan");
		p.setSmiles(originalSmiles.get(originalSmilesIndex));
		p.setCanonQsarSmiles(valeryResponse[0].predictions.get(originalSmilesIndex).chemical.smiles);
		String inchiKey = valeryResponse[0].predictions.get(originalSmilesIndex).chemical.smiles;
		DsstoxCompound compound = dsstoxCompoundService.findByDtxcid(cidBySmiles.get(originalSmiles.get(originalSmilesIndex)));
		p.setDtxcid(compound.getDsstoxCompoundId());
		GenericSubstanceCompound gsCompound = compound.getGenericSubstanceCompound();
		if (gsCompound != null) {
		p.setDtxsid(gsCompound.getGenericSubstance().getDsstoxSubstanceId());
		} else {
			p.setDtxsid("N/A");
		}
		if (originalSmiles.get(originalSmilesIndex).contains(".")) {
			p.setPredictionError("salt compound");
			predictionDashboards.add(p);
			return;
		}
		String valuesJSON = dpu.gson.toJson(valeryResponse[0].predictions.get(originalSmilesIndex).values);
		JSONObject valuesJO = new JSONObject(valuesJSON);
		
		for (int i = 0; i < valeryResponse[0].dataset.methods.size(); i++) {
			
			PredictionDashboard p1 = p;
			String methodName = valeryResponse[0].dataset.methods.get(i).name;
			if (methodName.equals("consensus")) {
				methodName = "avg";
			}
			Long modelId = valeryResponse[0].dataset.methods.get(i).model_id;
			modelId = modelIdRemapping(modelId);
			Model model = modelService.findById(modelId);
			p1.setModel(model);
			Double predictionValue = valuesJO.getDouble(methodName);
			p1.setPredictionString(String.valueOf(predictionValue));
			p1.setPredictionValue(predictionValue);
			predictionDashboards.add(p1);
		}
		
	}
	
	public static Long modelIdRemapping(Long id) {
		int idInt = Math.toIntExact(id);
		switch(idInt) {
		case(152):
			return 1310L;
		case(151):
			return 1303L;
		case(253):
			return 1315L;
		}
		return id;
	}
	
	public static void printthings(JSONObject jo, String field) {
		System.out.println(jo.getJSONArray("chemicals").toString());
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
	
	
	public static void runSDF(String SDFFilePath, Long datasetId, String destJsonPath,boolean skipMissingSID,int maxCount,
			ArrayList<String> datasetIds, String modelSetId, 		ArrayList<PredictionDashboard> predictionDashboards
) {
		DashboardPredictionUtilities dpu = new DashboardPredictionUtilities();

		List<Object>allResults=new ArrayList<>();//need to create a PredictionResults class based on WebTEST2.0 prediction json output
		
		AtomContainerSet acs= dpu.readSDFV3000(SDFFilePath);
		
		AtomContainerSet acs2 = dpu.filterAtomContainerSet(acs, skipMissingSID, maxCount);

		Iterator<IAtomContainer> iterator= acs2.atomContainers().iterator();

		ValeryPredictionWebService valery = new ValeryPredictionWebService("https://hcd.rtpnc.epa.gov/api/");

		
		ModelService modelService = new ModelServiceImpl();

		DsstoxCompoundService dsstoxCompoundService = new DsstoxCompoundServiceImpl();

		
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
			
			System.out.println("***"+count+"\t"+smiles);
			
			
			ArrayList<String> smilesList = new ArrayList<String>();
			smilesList.add(smiles);
			HashMap<String,String> cidBySmiles = new HashMap<>();
			cidBySmiles.put(smiles, dtxcid);
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
				createPredictionDashboard(vr, smilesList, i, predictionDashboards, dsstoxCompoundService, modelService, cidBySmiles, dpu);
			}
			
		}

//		System.out.println(Utilities.gson.toJson(allResults));
		//TODO do API call for json Report, store that too
		
	}

}
	

