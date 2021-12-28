package gov.epa.web_services.standardizers;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import kong.unirest.HttpResponse;

public abstract class Standardizer {
	
	public static class StandardizeResponse {
		@SerializedName("success")
		public Boolean success;
		@SerializedName("SMILES")
		public String smiles;
		@SerializedName(value="standardized SMILES", alternate= {"Standardized SMILES"})
		public String qsarStandardizedSmiles;
		public List<String> msStandardizedSmiles;
		@SerializedName("time")
		public String time;
	}

	public static class BatchStandardizeResponse {
		@SerializedName("success")
		public Boolean success;
		@SerializedName("time")
		public String time;
		@SerializedName("standardizations")
		public List<Standardization> standardizations;
		
		public static class Standardization {
			@SerializedName("SMILES")
			public String smiles;
			@SerializedName("standardized SMILES")
			public String standardizedSmiles;
		}
	}
	
	public static class StandardizeResponseWithStatus {
		public StandardizeResponse standardizeResponse;
		public int status;
		
		public static StandardizeResponseWithStatus fromHttpStandardizeResponse(HttpResponse<StandardizeResponse> httpResponse) {
			StandardizeResponseWithStatus response = new StandardizeResponseWithStatus();
			response.standardizeResponse = httpResponse.getBody();
			response.status = httpResponse.getStatus();
			return response;
		}
	}
	
	public static class BatchStandardizeResponseWithStatus {
		public BatchStandardizeResponse batchStandardizeResponse;
		public int status;
		
		public static BatchStandardizeResponseWithStatus fromHttpBatchStandardizeResponse(HttpResponse<BatchStandardizeResponse> httpResponse) {
			BatchStandardizeResponseWithStatus response = new BatchStandardizeResponseWithStatus();
			response.batchStandardizeResponse = httpResponse.getBody();
			response.status = httpResponse.getStatus();
			return response;
		}
	}
	
	public String standardizerName;
	public String standardizerType;
	public boolean useBatchStandardize;
	
	public abstract StandardizeResponseWithStatus callStandardize(String smiles);
	
	public abstract BatchStandardizeResponseWithStatus callBatchStandardize(List<String> smiles);
	
	public abstract BatchStandardizeResponseWithStatus callBatchStandardize(String filePath);
}
