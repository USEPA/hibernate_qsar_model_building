package gov.epa.web_services;

import kong.unirest.HttpResponse;
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

	public HttpResponse<SplittingCalculationResponse[]> callCalculation(String tsv, boolean removeLogP) {
		HttpResponse<SplittingCalculationResponse[]> response = Unirest.get(address+"/calculation")
				.queryString("tsv", tsv)
				.queryString("remove_log_p", removeLogP)
				.asObject(SplittingCalculationResponse[].class);
		
		return response;
	}
}
