package gov.epa.web_services.descriptors;

import gov.epa.web_services.WebService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 * Descriptor generation class that handles web service access
 * @author GSINCL01
 *
 */
public class TestDescriptorWebService extends WebService {
	
	private String urlTail;
	
	public static class TestDescriptorInfoResponse {
		public String name;
		public String version;
		public String description;
		public String headersTsv;
		public String is3d;
	}
	
	public static class TestDescriptorCalculationResponse {
		public String smiles;
		public String valuesTsv;
	}
	
	public TestDescriptorWebService(String server, int port, String urlTail) {
		super(server, port);
		this.urlTail = urlTail;
	}
	
	public TestDescriptorWebService(String url, String urlTail) {
		super(url);
		this.urlTail = urlTail;
	}
	
	public TestDescriptorWebService(String server, int port) {
		this(server, port, "");
	}
	
	public TestDescriptorWebService(String url) {
		this(url, "");
	}

	public HttpResponse<TestDescriptorInfoResponse> callInfo() {
		HttpResponse<TestDescriptorInfoResponse> response = Unirest.get(address+"/"+urlTail+"info")
				.asObject(TestDescriptorInfoResponse.class);
		
		return response;
	}

	public HttpResponse<TestDescriptorCalculationResponse> callCalculation(String smiles) {
		HttpResponse<TestDescriptorCalculationResponse> response = Unirest.get(address+"/"+urlTail+"calculation")
				.queryString("smiles", smiles)
				.asObject(TestDescriptorCalculationResponse.class);
		
		return response;
	}
}
