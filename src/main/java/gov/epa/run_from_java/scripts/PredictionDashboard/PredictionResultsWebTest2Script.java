package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.util.ArrayList;
import java.util.HashMap;

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
		
		for (int i = 0; i < smiles.size(); i++) {
			script.createPredictionDashboard(vr, smiles, i, predictionDashboards, dsstoxCompoundService, modelService, cidBySmiles);
		}
		PredictionDashboardService predictionDashboardService = new PredictionDashboardServiceImpl();
		for (PredictionDashboard predictionDashboard:predictionDashboards) {
			predictionDashboardService.create(predictionDashboard);
		}
		


	}
	
	// provide it 
	public void createPredictionDashboard(ValeryResponse[] valeryResponse, ArrayList<String> originalSmiles, int originalSmilesIndex, ArrayList<PredictionDashboard> predictionDashboards,
			DsstoxCompoundService dsstoxCompoundService, ModelService modelService, HashMap<String,String> cidBySmiles) {
		PredictionDashboard p = new PredictionDashboard();
		p.setCreatedBy("cramslan");
		p.setSmiles(originalSmiles.get(originalSmilesIndex));
		p.setCanonQsarSmiles(valeryResponse[0].predictions.get(originalSmilesIndex).chemical.smiles);
		String inchiKey = valeryResponse[0].predictions.get(originalSmilesIndex).chemical.smiles;
		DsstoxCompound compound = dsstoxCompoundService.findByDtxcid(cidBySmiles.get(originalSmiles.get(originalSmilesIndex)));
		p.setDtxcid(compound.getDsstoxCompoundId());
		GenericSubstanceCompound gsCompound = compound.getGenericSubstanceCompound();
		p.setDtxsid(gsCompound.getGenericSubstance().getDsstoxSubstanceId());
		if (originalSmiles.get(originalSmilesIndex).contains(".")) {
			p.setPredictionError("salt compound");
			predictionDashboards.add(p);
			return;
		}
		String valuesJSON = gson.toJson(valeryResponse[0].predictions.get(originalSmilesIndex).values);
		JSONObject valuesJO = new JSONObject(valuesJSON);
		
		for (int i = 0; i < valeryResponse[0].dataset.methods.size(); i++) {
			PredictionDashboard p1 = p;
			String methodName = valeryResponse[0].dataset.methods.get(i).name;
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
	
}
