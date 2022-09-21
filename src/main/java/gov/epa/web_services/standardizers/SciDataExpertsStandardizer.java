package gov.epa.web_services.standardizers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class SciDataExpertsStandardizer extends Standardizer {
	
	public static class SciDataExpertsStandardization {
		public String id;
		public String cid;
		public String sid;
		public String casrn;
		public String name;
		public String smiles;
		public String canonicalSmiles;
		public String inchi;
		public String inchiKey;
		public String mol;
	}
	
	private static final String SCI_DATA_EXPERTS_API_URL = "https://hazard-dev.sciencedataexperts.com/api/stdizer/";
//	private static final String QSAR_READY_WORKFLOW = "QSAR-ready_CNL_edits";
//	private static final String MS_READY_WORKFLOW = "ms-ready_CNL_edits";
	private static final String QSAR_READY_WORKFLOW = "qsar-ready";
	private static final String MS_READY_WORKFLOW = "ms-ready";
	
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	public SciDataExpertsStandardizer(String standardizerType) {
		this.standardizerName = DevQsarConstants.STANDARDIZER_SCI_DATA_EXPERTS + "_" + standardizerType;
		this.standardizerType = standardizerType;
		this.useBatchStandardize = false;
	}
	
	@Override
	public StandardizeResponseWithStatus callStandardize(String smiles) {
		StandardizeResponseWithStatus response = null;
		switch (standardizerType) {
		case DevQsarConstants.QSAR_READY:
			response = callQsarReadyStandardize(smiles);
			break;
		case DevQsarConstants.MS_READY:
			response = callMsReadyStandardize(smiles);
			break;
		}
		
//		if (response.standardizeResponse.msStandardizedSmiles!=null 
//				&& response.standardizeResponse.msStandardizedSmiles.size() > 1) {
//			System.out.println(gson.toJson(response));
//		}
		
		return response;
	}
	
	public StandardizeResponseWithStatus callQsarReadyStandardize(String smiles) {
		long t0 = System.currentTimeMillis();
		HttpResponse<SciDataExpertsStandardization[]> response = Unirest.get(SCI_DATA_EXPERTS_API_URL)
				.queryString("workflow", QSAR_READY_WORKFLOW)
				.queryString("smiles", smiles)				
				.asObject(SciDataExpertsStandardization[].class);
		long t = System.currentTimeMillis();
		
		String time = (t-t0)/1000.0 + " s";
		StandardizeResponseWithStatus responseWithStatus = 
				getQsarResponseWithStatusFromHttpResponse(response, smiles, time);
		
		return responseWithStatus;
	}
	
	public StandardizeResponseWithStatus callMsReadyStandardize(String smiles) {
		long t0 = System.currentTimeMillis();
		HttpResponse<SciDataExpertsStandardization[]> response = Unirest.get(SCI_DATA_EXPERTS_API_URL)
				.queryString("workflow", MS_READY_WORKFLOW)
				.queryString("smiles", smiles)				
				.asObject(SciDataExpertsStandardization[].class);
		long t = System.currentTimeMillis();
		
		String time = (t-t0)/1000.0 + " s";
		StandardizeResponseWithStatus responseWithStatus = 
				getMsResponseWithStatusFromHttpResponse(response, smiles, time);
		
		return responseWithStatus;
	}
	
	private StandardizeResponseWithStatus getQsarResponseWithStatusFromHttpResponse(
			HttpResponse<SciDataExpertsStandardization[]> response, String smiles, String time) {
		StandardizeResponseWithStatus responseWithStatus = new StandardizeResponseWithStatus();
		responseWithStatus.status = response.getStatus();
		
		StandardizeResponse standardizeResponse = new StandardizeResponse();
		standardizeResponse.smiles = smiles;
		standardizeResponse.time = time;
		
		if (responseWithStatus.status!=200 || response.getBody()==null || response.getBody().length==0) {
			standardizeResponse.success = false;
		} else {
			List<SciDataExpertsStandardization> standardizations = Arrays.asList(response.getBody());
			
			//TMM: Added if block 9/21/22 so that mixtures don't have QSAR ready smiles
			if (standardizations.size()>1 || standardizations.size()==0) {
				standardizeResponse.qsarStandardizedSmiles = null;
			} else if (standardizations.size()==1) {
				standardizeResponse.qsarStandardizedSmiles = standardizations.get(0).canonicalSmiles;
			} 
			
			
			standardizeResponse.success = standardizeResponse.qsarStandardizedSmiles!=null 
					&& !standardizeResponse.qsarStandardizedSmiles.isBlank();
		}
		
		responseWithStatus.standardizeResponse = standardizeResponse;
		return responseWithStatus;
	}

	private StandardizeResponseWithStatus getMsResponseWithStatusFromHttpResponse(
			HttpResponse<SciDataExpertsStandardization[]> response, String smiles, String time) {
		StandardizeResponseWithStatus responseWithStatus = new StandardizeResponseWithStatus();
		responseWithStatus.status = response.getStatus();
		
		StandardizeResponse standardizeResponse = new StandardizeResponse();
		standardizeResponse.smiles = smiles;
		standardizeResponse.time = time;
		
		if (responseWithStatus.status!=200 || response.getBody()==null) {
			standardizeResponse.success = false;
		} else {
			List<SciDataExpertsStandardization> standardizations = Arrays.asList(response.getBody());
			standardizeResponse.msStandardizedSmiles = 
					standardizations.stream().map(std -> std.canonicalSmiles).collect(Collectors.toList());
			standardizeResponse.success = standardizeResponse.msStandardizedSmiles!=null 
					&& !standardizeResponse.msStandardizedSmiles.isEmpty();
		}
		
		responseWithStatus.standardizeResponse = standardizeResponse;
		return responseWithStatus;
	}

	@Override
	public BatchStandardizeResponseWithStatus callBatchStandardize(List<String> smiles) {
		throw new IllegalArgumentException("Batch mode not implemented in SciDataExperts standardizer");
	}

	@Override
	public BatchStandardizeResponseWithStatus callBatchStandardize(String filePath) {
		throw new IllegalArgumentException("Batch mode not implemented in SciDataExperts standardizer");
	}

}
