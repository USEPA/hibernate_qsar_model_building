package gov.epa.web_services;


import java.util.ArrayList;

import com.google.gson.Gson;

import gov.epa.run_from_java.scripts.PredictionDashboard.valery.Chemical;
import gov.epa.run_from_java.scripts.PredictionDashboard.valery.Dataset;
import gov.epa.run_from_java.scripts.PredictionDashboard.valery.ValeryBody;
import gov.epa.run_from_java.scripts.PredictionDashboard.valery.WebTEST2PredictionResponse;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

public class ValeryPredictionWebService extends WebService {
	
	public Gson gson = new Gson();
	
	public ValeryPredictionWebService(String url) {
		super(url);
	}

	
	public HttpResponse<String> predict(String json) {
			HttpResponse<String> response = Unirest.post(address + "/predictor/predict")
			.header("Content-Type", "application/json")
			.body(new JSONObject(json))
		    .asString();
			System.out.println("status= " + response.getStatus());
		return response;
	}
	
	public WebTEST2PredictionResponse predictSingleChemical(String smiles, String datasetId, String modelSetId) {
		ArrayList<String> smilesList = new ArrayList<String>();
		smilesList.add(smiles);
	//	smiles.add("CCCO");
		ValeryBody vb = new ValeryBody();
		ArrayList<Chemical> chemicals = new ArrayList<>();
		for (int i = 0; i < smilesList.size(); i++) {
			Chemical chemical = new Chemical();
			chemical.setSmiles(smilesList.get(i));
			chemicals.add(chemical);
		}
		vb.setChemicals(chemicals);
		ArrayList<Dataset> datasets = new ArrayList<Dataset>();
		ArrayList<String> datasetIds = new ArrayList<String>();
		datasetIds.add(datasetId);
		for (int i = 0; i < datasetIds.size(); i++) {
			Dataset dataset = new Dataset();
			dataset.setDatasetId(datasetIds.get(i));
			datasets.add(dataset);
		}
		vb.setDatasets(datasets);
		vb.setModelSetId(modelSetId);
		vb.setWorkflow("qsar-ready");
		String json = gson.toJson(vb);
		HttpResponse<String> response = predict(json);
		System.out.print("response body=" + response.getBody());
		if (response.getStatus() == 400) {
			System.out.println("cannot predict");
			return null;
		}
		WebTEST2PredictionResponse[] vr = gson.fromJson(response.getBody(), WebTEST2PredictionResponse[].class);
		WebTEST2PredictionResponse valeryResponse = vr[0];
		System.out.println(valeryResponse);
	return valeryResponse;


	}
	
}
