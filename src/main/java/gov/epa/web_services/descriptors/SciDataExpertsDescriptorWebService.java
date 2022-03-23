package gov.epa.web_services.descriptors;

import java.util.List;
import java.util.Map;

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
		public Integer ordinal;
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
		public Boolean headers;
		public Boolean compute2D;
		public Boolean compute3D;
		public Boolean computeFingerprints;
		public Boolean removeSalt;
		public Boolean standardizeNitro;
		public Boolean standardizeTautomers;
		public Integer bits;
		public Integer radius;
		public String type;
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
	
	public HttpResponse<SciDataExpertsDescriptorResponse> calculateDescriptorsWithOptions(String smiles, String descriptorName, 
			Map<String, Object> options) {
		HttpResponse<SciDataExpertsDescriptorResponse> response = Unirest.get(address + "/api/{descriptorName}")
				.routeParam("descriptorName", descriptorName)
				.queryString("smiles", smiles)
				.queryString(options)
				.asObject(SciDataExpertsDescriptorResponse.class);
		
		return response;
	}

}
