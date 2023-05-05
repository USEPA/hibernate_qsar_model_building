package gov.epa.web_services;


import gov.epa.run_from_java.scripts.PredictionDashboard.valery.ValeryBody;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

public class ValeryPredictionWebService extends WebService {
	
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
	
	

}
