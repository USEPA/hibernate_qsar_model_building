package gov.epa.web_services;

import java.io.File;

import kong.unirest.HttpResponse;
import kong.unirest.MultipartBody;
import kong.unirest.Unirest;

/**
 * Descriptor generation class that handles web service access
 * @author GSINCL01
 *
 */
public class SplittingWebService extends WebService {
	
	public String splittingName;
	public Integer numSplits;
	
	public static class SplittingCalculationResponse {
		public String ID;
		public Double exp;
		public Double pred;
		public String t_p;
	}
	
	public SplittingWebService(String server, int port, String splittingName, Integer numSplits) {
		super(server, port);
		this.splittingName = splittingName;
		this.numSplits = numSplits;
	}

	//Following doesnt work for large tsvs:
//	public HttpResponse<SplittingCalculationResponse[]> callCalculation(String tsv, boolean removeLogP, int n_threads) {
//		HttpResponse<SplittingCalculationResponse[]> response = Unirest.get(address+"/calculation")
//				.queryString("tsv", tsv)
//				.queryString("remove_log_p", removeLogP)
//				.queryString("n_threads", n_threads)
//				.asObject(SplittingCalculationResponse[].class);
//		
//		return response;
//	}
	

	public HttpResponse<SplittingCalculationResponse[]> callCalculation(String tsv, boolean removeLogP, int n_threads) {
		HttpResponse<SplittingCalculationResponse[]> response = Unirest.post(address+"/calculation")
				.field("tsv", tsv)
				.field("remove_log_p", removeLogP+"")
				.field("n_threads", n_threads+"")
				.asObject(SplittingCalculationResponse[].class);		
		return response;
	}

	
//	public String callCalculation(String tsv, boolean removeLogP, int n_threads) {
////	HttpResponse<SplittingCalculationResponse[]> 
//	
//	 MultipartBody body= Unirest.post(address+"/calculation")
//			.field("remove_log_p", removeLogP+"")
//			.field("n_threads", n_threads+"")
//			.field("tsv", new File("data/tempOverallSet.tsv"));
//	 
//	 	String json=body.asJson().getBody().toPrettyString();
//	
//	 	System.out.println(json);
//	 
//		return null;
//	}
	

//	public HttpResponse<String> callBob() {
//	HttpResponse<String> response = Unirest.post(address+"/bob")
//			.asString();	
//	return response;
//}

}
