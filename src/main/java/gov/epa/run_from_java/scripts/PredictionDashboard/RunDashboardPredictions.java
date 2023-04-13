package gov.epa.run_from_java.scripts;

import java.util.List;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.service.GenericSubstanceServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
* @author TMARTI02
*/
public class RunDashboardPredictions {

	GenericSubstanceServiceImpl gss=new GenericSubstanceServiceImpl();
	
	public RunDashboardPredictions() {
		try {
			Unirest.config().followRedirects(true).socketTimeout(000).connectTimeout(000);
		} catch (Exception e) {
			// Ignore
		}
	}
	
	void runChemical(String DTXSID) {
		//2 different ways to run it- using our python webservice and using valery webservice
		List<DsstoxRecord>records=gss.findAsDsstoxRecordsByDtxsid(DTXSID);
		
		DsstoxRecord dr=records.get(0);
		System.out.println(Utilities.gson.toJson(dr));
		
		String json=doAPI_Prediction_WebTEST2(dr.smiles,1,21);//Note these ids dont correspond to models in postgres_testing (but can still store results using model_id
		
		System.out.println(json);
		
		//convert json to java object=>dashboard predictipn object
		
		
	}
	
	void doPredictionTEST51() {
		//TODO add code to call java code for running test predictions using runFromSmiles method
		
	}
	
	String doAPI_Prediction_WebTEST2(String smiles,int modelset_id,int dataset_id) {
		
		String body="{\"chemicals\":[{\"smiles\":\""+smiles+"\"}],\"modelset_id\":"+modelset_id +",\"datasets\":[{\"dataset_id\":"+dataset_id+"}],\"workflow\":\"qsar-ready\"}\r\n\r\n";

		System.out.println(body);
		
		HttpResponse<String> response = Unirest.post("https://hazard-dev.sciencedataexperts.com/api/predictor/predict")
		  .header("Content-Type", "application/json")
		  .body(body)
		  .asString();
		return response.getBody();
	}
	
	public static void main(String[] args) {
		RunDashboardPredictions r=new RunDashboardPredictions();
		r.runChemical("DTXSID3039242");
	}

}
