package gov.epa.web_services;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 * Outlier detection class that handles web service access
 * @author GSINCL01
 *
 */
public class OutlierDetectionWebService extends WebService {
	
	public static class OutlierDetectionCalculationResponse {
		public String ID;
		public Double exp;
		public Double pred;
	}
	
	public OutlierDetectionWebService(String server, int port) {
		super(server, port);
	}

	public HttpResponse<OutlierDetectionCalculationResponse[]> callCalculation(String tsv, boolean removeLogP) {
		HttpResponse<OutlierDetectionCalculationResponse[]> response = Unirest.get(address+"/calculation")
				.queryString("tsv", tsv)
				.queryString("remove_log_p", removeLogP)
				.asObject(OutlierDetectionCalculationResponse[].class);
		
		return response;
	}
}
