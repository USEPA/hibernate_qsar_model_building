package gov.epa.web_services;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 * Descriptor generation class that handles web service access
 * @author GSINCL01
 *
 */
public class DescriptorWebService extends WebService {
	
	private String urlTail;
	
	public static class DescriptorInfoResponse {
		public String name;
		public String version;
		public String description;
		public String headersTsv;
		public String is3d;
	}
	
	public static class DescriptorCalculationResponse {
		public String smiles;
		public String valuesTsv;
	}
	
	public DescriptorWebService(String server, int port, String urlTail) {
		super(server, port);
		this.urlTail = urlTail;
	}
	
	public DescriptorWebService(String server, int port) {
		this(server, port, "");
	}

	public HttpResponse<DescriptorInfoResponse> callInfo() {
		HttpResponse<DescriptorInfoResponse> response = Unirest.get(server+":"+port+"/"+urlTail+"info")
				.asObject(DescriptorInfoResponse.class);
		
		return response;
	}

	public HttpResponse<DescriptorCalculationResponse> callCalculation(String smiles) {
		HttpResponse<DescriptorCalculationResponse> response = Unirest.get(server+":"+port+"/"+urlTail+"calculation")
				.queryString("smiles", smiles)
				.asObject(DescriptorCalculationResponse.class);
		
		return response;
	}
}
