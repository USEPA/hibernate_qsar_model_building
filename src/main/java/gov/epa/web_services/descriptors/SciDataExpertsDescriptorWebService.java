package gov.epa.web_services.descriptors;

import java.util.List;

import gov.epa.web_services.WebService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class SciDataExpertsDescriptorWebService extends WebService {
	
	public static class SciDataExpertsDescriptorResponse {
		public List<SciDataExpertsChemical> chemicals;
		public List<String> headers;
		public SciDataExpertsDescriptorInfo info;
		public SciDataExpertsDescriptorOptions options;
	}
	
	public static class SciDataExpertsChemical {
		public List<Double> descriptors;
		public String id;
		public String inchi;
		public String inchikey;
		public String smiles;
	}
	
	public static class SciDataExpertsDescriptorInfo {
		public String name;
		public String version;
	}
	
	public static class SciDataExpertsDescriptorOptions {
		public boolean headers;
		public boolean compute2D;
		public boolean compute3D;
		public boolean computeFingerprints;
		public boolean removeSalt;
		public boolean standardizeNitro;
		public boolean standardizeTautomers;
	}

	public SciDataExpertsDescriptorWebService(String url) {
		super(url);
	}
	
	public HttpResponse<SciDataExpertsDescriptorResponse> calculateDescriptors(String smiles, String descriptorName) {
		HttpResponse<SciDataExpertsDescriptorResponse> response = Unirest.get(address + "/api/{descriptorName}")
				.routeParam("descriptorName", descriptorName)
				.queryString("smiles", smiles)
				.asObject(SciDataExpertsDescriptorResponse.class);
		
		return response;
	}

}
