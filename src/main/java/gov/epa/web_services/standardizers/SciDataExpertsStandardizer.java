package gov.epa.web_services.standardizers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoException;
import com.epam.indigo.IndigoInchi;
import com.epam.indigo.IndigoObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


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
	
	
	public static StandardizeResponseWithStatus callQsarReadyStandardize(String url, String workflow, String smiles) {
		long t0 = System.currentTimeMillis();
		HttpResponse<SciDataExpertsStandardization[]> response = Unirest.get(url)
				.queryString("workflow", workflow)
				.queryString("smiles", smiles)				
				.asObject(SciDataExpertsStandardization[].class);
		long t = System.currentTimeMillis();
		
		String time = (t-t0)/1000.0 + " s";
		StandardizeResponseWithStatus responseWithStatus = 
				getQsarResponseWithStatusFromHttpResponse(response, smiles, time);
		
		return responseWithStatus;
	}
	
	
	public static StandardizeResponseWithStatus callQsarReadyStandardize(String url, String workflow, 
			String smiles,Vector<String>inchiKeyExclude) {
		long t0 = System.currentTimeMillis();
		HttpResponse<SciDataExpertsStandardization[]> response = Unirest.get(url)
				.queryString("workflow", workflow)
				.queryString("smiles", smiles)				
				.asObject(SciDataExpertsStandardization[].class);
		long t = System.currentTimeMillis();
		
		String time = (t-t0)/1000.0 + " s";
		StandardizeResponseWithStatus responseWithStatus = 
				getQsarResponseWithStatusFromHttpResponse(response, smiles, time,inchiKeyExclude);
		
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
	
	public static String getStringFromURL(String strUrl) {
		try {
			URL url = new URL(strUrl);
			InputStream input = url.openStream();
	        InputStreamReader isr = new InputStreamReader(input);
	        BufferedReader reader = new BufferedReader(isr);
	        StringBuilder json = new StringBuilder();
	        int c;
	        while ((c = reader.read()) != -1) {
	            json.append((char) c);
	        }
	        return json.toString();
	    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error";
		}
	}
	
	
	private static StandardizeResponseWithStatus getQsarResponseWithStatusFromHttpResponse(
			HttpResponse<SciDataExpertsStandardization[]> response, String smiles, String time) {
		StandardizeResponseWithStatus responseWithStatus = new StandardizeResponseWithStatus();
		responseWithStatus.status = response.getStatus();
		
		StandardizeResponse standardizeResponse = new StandardizeResponse();
		standardizeResponse.smiles = smiles;
		standardizeResponse.time = time;
		
		if (responseWithStatus.status!=200 || response.getBody()==null || response.getBody().length==0) {
			standardizeResponse.success = false;
			
//			System.out.println("failed");
			
		} else {
			List<SciDataExpertsStandardization> standardizations = Arrays.asList(response.getBody());
			
			//TMM: 1/7/23, if have multiple structures in response, the qsar smiles is now a "." delimted mixture
			
			if (standardizations.size()>1 || standardizations.size()==0) {
				
//				standardizeResponse.qsarStandardizedSmiles = null;
				standardizeResponse.qsarStandardizedSmiles = "";
				
				for (int i=0;i<standardizations.size();i++) {
					standardizeResponse.qsarStandardizedSmiles+=standardizations.get(i).canonicalSmiles;
//					System.out.println(i+"\t"+standardizations.get(i).canonicalSmiles);
					if(i<standardizations.size()-1) standardizeResponse.qsarStandardizedSmiles+="."; 
				}
				
			} else if (standardizations.size()==1) {
				standardizeResponse.qsarStandardizedSmiles = standardizations.get(0).canonicalSmiles;
			} 
			
			standardizeResponse.success = standardizeResponse.qsarStandardizedSmiles!=null 
					&& !standardizeResponse.qsarStandardizedSmiles.isBlank();
		}
		
		responseWithStatus.standardizeResponse = standardizeResponse;
		return responseWithStatus;
	}
	
	/**
	 * Method added by TMM that will manually exclude structures on the excluisions list
	 * 
	 * @param response
	 * @param smiles
	 * @param time
	 * @param inchiKeyExclude
	 * @return
	 */
	private static StandardizeResponseWithStatus getQsarResponseWithStatusFromHttpResponse(
			HttpResponse<SciDataExpertsStandardization[]> response, String smiles, String time,Vector<String>inchiKeyExclude) {
		StandardizeResponseWithStatus responseWithStatus = new StandardizeResponseWithStatus();
		responseWithStatus.status = response.getStatus();
		
		StandardizeResponse standardizeResponse = new StandardizeResponse();
		standardizeResponse.smiles = smiles;
		standardizeResponse.time = time;
				
		
		if (responseWithStatus.status!=200 || response.getBody()==null || response.getBody().length==0) {
			standardizeResponse.success = false;			
			System.out.println(smiles+"\tfailed");
			
		} else {
			ArrayList<SciDataExpertsStandardization> standardizations = new ArrayList(Arrays.asList(response.getBody()));
			
			//TMM: Added if block 9/21/22 so that mixtures don't have QSAR ready smiles
			if (standardizations.size()>1 || standardizations.size()==0) {
				
				for (int i=0;i<standardizations.size();i++) {					
					String inchiKey=toInchiKey(standardizations.get(i).canonicalSmiles);					
//					System.out.println(standardizations.get(i).canonicalSmiles+"\t"+inchiKey);
					
					if (inchiKeyExclude.contains(inchiKey)) {						
						System.out.println(smiles+"\t"+i+"\t"+standardizations.get(i).canonicalSmiles+"\tExcluded");
						standardizations.remove(i--);						
					}
				}
				if (standardizations.size()==1) {
					standardizeResponse.qsarStandardizedSmiles = standardizations.get(0).canonicalSmiles;	
				} else {
					standardizeResponse.qsarStandardizedSmiles = null;
				}
				
			} else if (standardizations.size()==1) {				
				System.out.println(smiles+"\tsize=1");				
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
	

	/**
	 * Reads exclusions json from url and converts to inchiKey vector
	 * 
	 * @param url
	 * @return vector of inchiKeys for exclusions
	 */	
	static Vector<String> getInchiExclusions(String url) {
		String jsonExclusions=getStringFromURL(url);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonObject jobob=gson.fromJson(jsonExclusions, JsonObject.class);
//		System.out.println(gson.toJson(jobob));
		JsonArray ja=jobob.get("operations").getAsJsonArray();		
		Vector<String>inchiKeyExclude=new Vector<>();
		
		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			String smiles=jo.get("value").getAsString();
			String inchiKey=toInchiKey(smiles);
			inchiKeyExclude.add(inchiKey);
//			System.out.println(smiles+"\t"+inchiKey);
		}
		return inchiKeyExclude;
	}
	
	/**
	 * Converts mol or smiles into inchiKey using indigo
	 * 
	 * @param mol
	 * @return
	 */
	public static String toInchiKey(String mol) {
		try {
			Indigo indigo = new Indigo();
			indigo.setOption("ignore-stereochemistry-errors", true);
			IndigoInchi indigoInchi = new IndigoInchi(indigo);
			IndigoObject m = indigo.loadMolecule(mol);
			return indigoInchi.getInchiKey(indigoInchi.getInchi(m));

		} catch (IndigoException ex) {
			//			log.error(ex.getMessage());
			return null;
		}
	}
		
	public static void main(String[] args) {
		String url="https://ccte-cced.epa.gov/api/stdizer/";
		
//		String workflow="QSAR-ready_CNL_edits_TMM_2";
//		String workflow="qsar-ready";
		String workflow="QSAR-ready_CNL_edits";
		
//		String smiles="CI.CCCC";
//		String smiles="CI.CCCC.Cl.[Ag+].ClCCl";
//		String smiles="[Na+].[OH-][B+3]([C-]=1C=CC=CC1)([C-]=2C=CC=CC2)[C-]=3C=CC=CC3";
		
		String smiles="CCCCCCCCCCCCOS(O)(=O)=O.OCCN(CCO)CCO";

		
//		String smiles="CCCC";
//		String smiles="CI";
//		String smiles="[Ag+].[C-]#[N+][O-]";
		
//		Vector<String>inchiKeyExclude=getInchiExclusions("http://v2626umcth819.rtord.epa.gov:443/api/stdizer/groups/qsar-ready-exclusions_TMM");
//		StandardizeResponseWithStatus responseWithStatus=callQsarReadyStandardize(url, workflow, smiles,inchiKeyExclude);

		StandardizeResponseWithStatus responseWithStatus=callQsarReadyStandardize(url, workflow, smiles);
		
		System.out.println(responseWithStatus.standardizeResponse.qsarStandardizedSmiles);
	}

}
