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
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.web_services.standardizers.Standardizer.BatchStandardizeResponseWithStatus;
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

//	private static final String SCI_DATA_EXPERTS_API_URL = "https://hazard-dev.sciencedataexperts.com/api/stdizer/";
	public static final String SCI_DATA_EXPERTS_API_URL = "https://hcd.rtpnc.epa.gov/api/stdizer/";

	//	private static final String SCI_DATA_EXPERTS_API_URL = "https://ccte-cced.epa.gov/api/stdizer/";

	//	private static final String QSAR_READY_WORKFLOW = "QSAR-ready_CNL_edits";
	//	QSAR-ready_CNL_edits_TMM_2


	//	private static final String MS_READY_WORKFLOW = "ms-ready_CNL_edits";
	private static final String QSAR_READY_WORKFLOW = "qsar-ready";
	private static final String MS_READY_WORKFLOW = "ms-ready";

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	String workflow;
	String serverHost;
	
	
	public SciDataExpertsStandardizer(String standardizerType,String workflow,String serverHost) {
		this.standardizerName = DevQsarConstants.STANDARDIZER_SCI_DATA_EXPERTS + "_" + standardizerType;
		this.standardizerType = standardizerType;
		this.useBatchStandardize = false;
		
		this.workflow=workflow;
		this.serverHost=serverHost;
		
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

	//TODO get doesnt work for standardize anymore...
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

		System.out.println(url);

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


	public static String getQsarReadySmilesFromPostJson(String json,boolean full) {

		if (full) {
			return handleFullOutput(json);
		} else {
			return handleSimpleOutput(json);
		}

	}

	private static String handleFullOutput(String json) {
		JsonObject jo=Utilities.gson.fromJson(json, JsonObject.class);

		JsonArray records=jo.get("records").getAsJsonArray();

		//			System.out.println(Utilities.gson.toJson(records));	


		if (records.size()==1) {
			JsonObject record=records.get(0).getAsJsonObject();
			return getSmilesFromRecord(record);
		} else if (records.size()==0) {
			return null;
		} else {
			String smiles="";
			
			for (int i=0;i<records.size();i++) {
				JsonObject record=records.get(i).getAsJsonObject();
				
				if (record.get("status").getAsString().equals("SKIPPED")) continue;

				String smiles_i = getSmilesFromRecord(record);
				
				if (smiles.isEmpty())smiles=smiles_i;
				else smiles=smiles+"."+smiles_i;

//				System.out.println(i+"\t"+Utilities.gson.toJson(record)+"\n\n");	
//				System.out.println(i+"\t"+smiles_i);
			}
			
			return smiles;
		}
		
	}
	
	
	static String getChangesApplied(String json) {
		
		String changes="";
		
		JsonObject jo=Utilities.gson.fromJson(json, JsonObject.class);
		JsonArray records=jo.get("records").getAsJsonArray();
		
		for (int i=0;i<records.size();i++) {
			JsonObject joi=records.get(i).getAsJsonObject();
			
			JsonArray transformLog=joi.get("transformLog").getAsJsonArray();
			
			if (transformLog.size()==0) continue;
			
			for (int j=0;j<transformLog.size();j++) {
				JsonObject joTransform=transformLog.get(j).getAsJsonObject();
				
				JsonObject joChange=joTransform.get("change").getAsJsonObject();
				
				if(joChange.get("value").isJsonNull()) continue;
				
				String text=joChange.get("value").getAsString();
				
				if (changes.isEmpty()) {
					changes+=text;	
				} else {
					changes+="\n"+text;
				}
				
//				System.out.println(gson.toJson(joTransform));
				
			}
			
//			System.out.println(changes);
		}
		
		return changes;
		
	}

	private static String getSmilesFromRecord(JsonObject record) {
		String smiles_i=null;

		JsonArray transformLog=record.get("transformLog").getAsJsonArray();
		
		if(transformLog.size()>0 && !record.get("status").getAsString().equals("FAILED")) {
			//					System.out.println(Utilities.gson.toJson(transformLog));
			//use last one in log:
			JsonObject original=transformLog.get(transformLog.size()-1).getAsJsonObject();
			JsonObject chemical=original.get("chemical").getAsJsonObject();
			smiles_i=chemical.get("canonicalSmiles").getAsString();
		
		} else if(record.get("status").getAsString().equals("FAILED")) {

			JsonArray jaIssues=record.get("issues").getAsJsonArray();

			for (int j=0;j<jaIssues.size();j++) {
				JsonObject joIssue=jaIssues.get(j).getAsJsonObject();
				String severity=joIssue.get("severity").getAsString();
				String message=joIssue.get("message").getAsString();

//				System.out.println(severity+"\t"+message);
				
				if (severity.equals("ERROR")) {
					smiles_i="error:"+message;
				}
			}
			
			if(smiles_i==null)	smiles_i="error";

		} else {
			//					System.out.println(Utilities.gson.toJson(record));
			JsonObject original=record.get("original").getAsJsonObject();
			JsonObject chemical=original.get("chemical").getAsJsonObject();
			smiles_i=chemical.get("canonicalSmiles").getAsString();
		}
		return smiles_i;
	}

	private static String handleSimpleOutput(String json) {
		JsonArray results=Utilities.gson.fromJson(json, JsonArray.class);

		if (results.size()==0) {
			return null;
		} else if (results.size()==1) {
			JsonObject result=results.get(0).getAsJsonObject();
			return result.get("canonicalSmiles").getAsString();
		} else {

			String smiles="";
			for (int i=0;i<results.size();i++) {
				JsonObject result=results.get(i).getAsJsonObject();
				smiles+=result.get("canonicalSmiles").getAsString();
				if (i<results.size()-1) smiles+=".";
			}
			return smiles;
		}
	}

	public static String getResponseBody(HttpResponse<String> response, boolean full) {
		
		Object responseBody=null;

		if (full) {
			responseBody=Utilities.gson.fromJson(response.getBody(), JsonObject.class);
		} else {
			responseBody=Utilities.gson.fromJson(response.getBody(), JsonArray.class);
		}

//		System.out.println(response.getStatus());
		
		return Utilities.gson.toJson(responseBody);//convert back and forth to get prettyprinting
	}


	public HttpResponse<String> callQsarReadyStandardizePost(String smiles,boolean full) {
		//		Unirest.setTimeouts(0, 0);

		JsonObject joBody=new JsonObject();

		joBody.addProperty("full", full);

		JsonObject joOptions=new JsonObject();
		joOptions.addProperty("workflow", workflow);
		joBody.add("options", joOptions);

		JsonArray chemicals=new JsonArray();
		JsonObject chemical=new JsonObject();
		chemical.addProperty("smiles", smiles);
		chemicals.add(chemical);
		joBody.add("chemicals", chemicals);

		//		System.out.println(Utilities.gson.toJson(joBody));

		HttpResponse<String> response = Unirest.post(serverHost+"/api/stdizer/chemicals")
				.header("Content-Type", "application/json")
				.body(Utilities.gson.toJson(joBody))
				.asString();

		return response;
		

	}

	
	public HttpResponse<String> callQsarReadyStandardizePost(String host, String workflow, List<String>smilesList,boolean full) {
		//		Unirest.setTimeouts(0, 0);

		JsonObject joBody=new JsonObject();

		joBody.addProperty("full", full);

		JsonObject joOptions=new JsonObject();
		joOptions.addProperty("workflow", workflow);
		joBody.add("options", joOptions);

		JsonArray chemicals=new JsonArray();
		
		for (String smiles:smilesList) {
			JsonObject chemical=new JsonObject();
			chemical.addProperty("smiles", smiles);
			chemicals.add(chemical);
		}
		
		joBody.add("chemicals", chemicals);

		//		System.out.println(Utilities.gson.toJson(joBody));

		HttpResponse<String> response = Unirest.post(host+"/api/stdizer/chemicals")
				.header("Content-Type", "application/json")
				.body(Utilities.gson.toJson(joBody))
				.asString();

		return response;
		

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

			//TMM: 1/7/23, if have multiple structures in response, the qsar smiles is now a "." delimited mixture

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
	public BatchStandardizeResponseWithStatus callBatchStandardize(String filePath) {
		throw new IllegalArgumentException("Batch mode not implemented in SciDataExperts standardizer");
	}
	
	@Override
	public BatchStandardizeResponseWithStatus callBatchStandardize(List<String> smiles) {
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
		//		String url="https://ccte-cced.epa.gov/api/stdizer/";
//		String url="https://hcd.rtpnc.epa.gov/api/stdizer/";
		String serverHost="https://hcd.rtpnc.epa.gov";

		String workflow="QSAR-ready_CNL_edits_TMM_2";
		//		String workflow="qsar-ready";
		//		String workflow="QSAR-ready_CNL_edits";

		//		String smiles="CI.CCCC";
		//		String smiles="CI.CCCC.Cl.[Ag+].ClCCl";
		//		String smiles="[Na+].[OH-][B+3]([C-]=1C=CC=CC1)([C-]=2C=CC=CC2)[C-]=3C=CC=CC3";

		//		String smiles="CCCCCCCCCCCCOS(O)(=O)=O.OCCN(CCO)CCO";
//		String smiles="CCCCCCCCCCCCOS(O)(=O)=O.OCCN(CCO)CCO.c1ccccc1";
//				String smiles="CCCC";
//				String smiles="c1ccccc1";
		//		String smiles="c1ccccc1.Cl";
		//		String smiles="CCCCCCCCCCCCOS(O)(=O)=O";
//		String smiles="CCC(C)[N+]([O-])(O)C1C=CC(=CC=1)NC1C=CC=CC=1";
		
//		String smiles="ON=C1C2=C(C=C(C=C2)Cl)N(O)C=C1";
		
//		String smiles="[I-].CN(C)C(=[S+]C1C=C(Br)C=CC=1)N(C)Cc1ccccc1";
//		String smiles="XXX";
//		String smiles="[OH-].[OH-].[Mg+2].NC1=NC(=O)C(O1)C1C=CC=CC=1";
		
		//		String smiles="bobert";
		//		String smiles="CC1=C2N=C3C(NC(=N)N=C3O)=NCC2C2CNC3NC(=N)N=C(O)C=3N12";//fails standardization
		//		String smiles="CCCC.bobert";

				String smiles="CCCC";
		//		String smiles="CI";
		//		String smiles="[Ag+].[C-]#[N+][O-]";

		//		Vector<String>inchiKeyExclude=getInchiExclusions("http://v2626umcth819.rtord.epa.gov:443/api/stdizer/groups/qsar-ready-exclusions_TMM");
		//		StandardizeResponseWithStatus responseWithStatus=callQsarReadyStandardize(url, workflow, smiles,inchiKeyExclude);

		//		StandardizeResponseWithStatus responseWithStatus=callQsarReadyStandardize(url, workflow, smiles);

		//		System.out.println(responseWithStatus.standardizeResponse.qsarStandardizedSmiles);

		boolean full=true;
		SciDataExpertsStandardizer standardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY,workflow,serverHost);

		HttpResponse<String>response=standardizer.callQsarReadyStandardizePost(smiles,full);
		String jsonResponse=getResponseBody(response, full);
		
		System.out.println(jsonResponse);
		
		String qsarSmiles=getQsarReadySmilesFromPostJson(jsonResponse, full);
		
		String changes=getChangesApplied(jsonResponse);
		
		System.out.println(qsarSmiles);
		System.out.println(changes);

	}



	

}
