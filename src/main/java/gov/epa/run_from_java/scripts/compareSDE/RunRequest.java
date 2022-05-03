package gov.epa.run_from_java.scripts.compareSDE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;



import com.google.gson.Gson;
import com.google.gson.JsonObject;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;


public class RunRequest {

	public static Hashtable<String,String> htDescriptors=new Hashtable<>();	
	public static int port=8080;
	
	static class RequestSDE {
		public String type="webtest";
		public List<String>chemicals;
		public String format="JSON";
		
		public options options=new options();
//		public options options;
		
		public RequestSDE(List<String>smiles,String type) {
			this.chemicals=smiles;
			this.type=type;
		}
		
		public RequestSDE(List<String>smiles) {
			this.chemicals=smiles;
		}

		static class options {
			public boolean headers=true;
			
		}
	}
	static class ResponseSDE {
		
		public info info;
		public List<String>headers;
		public options options;
		
		public List<chemical>chemicals;
		
		public class options {
			public boolean headers=true;
		}
		
		public class info {
			public String name;
			public String version;
		}
		
		public class chemical {
			public int ordinal;
			public String smiles;
			public String inchi;
			public String inchiKey;
			public List<Double>descriptors;
		}
		
		
	}
	
	public static String runRequest(String smiles) {
		
		String strDescriptors=null;

		try {
			HttpResponse<JsonNode> response = Unirest.get("http://localhost:"+port+"/TEST-descriptors/calculation")		        				
					.queryString("smiles", smiles)
					.asJson();	
			JSONObject myObj = response.getBody().getObject();
			strDescriptors = myObj.getString("valuesTsv");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strDescriptors;

	}
	
	
	public static String compareTESTDescriptors(String smiles,boolean standardize) {
		
		RunRequest.configLogger();
		List<String>smilesList=new ArrayList<>();
		
		smilesList.add(smiles);
		RequestSDE request=new RequestSDE(smilesList,"webtest");			
		ResponseSDE responseSDE=RunRequest.runRequestBatchSDE2(request,standardize);
		
//		ResponseSDE responseSDE_single=RunRequest.runRequestSDE(smiles);
		
		List<Double>descriptors=responseSDE.chemicals.get(0).descriptors;
		
		if (descriptors==null) return "null descriptors";
		
//		List<Double>descriptors=responseSDE_single.chemicals.get(0).descriptors;
		
		List<String>headers=responseSDE.headers;
		
		String strDescriptors=RunRequest.runRequest(smiles);
		
		String []vals=strDescriptors.split("\t");
		
		for(int i=0;i<vals.length;i++) {
			
			if (i>=descriptors.size()) return "null descriptors";
			
			double ourval=Double.parseDouble(vals[i]);
			double sdeval=descriptors.get(i);
			
			
			if (Math.abs(ourval-sdeval)>1e-5) {
//				System.out.println(smiles+"\t"+headers.get(i)+"\t"+ourval+"\t"+sdeval);
				return "descriptors different";
			}
			
		}
		return "ok";
		
//		System.out.println(strDescriptors);
	}
	
	public static ResponseSDE runRequestSDE(String smiles) {
		
		String strDescriptors=null;

		try {
			HttpResponse<JsonNode> response = Unirest.get("https://ml.sciencedataexperts.com/api/descriptors/")		        				
					.queryString("smiles", smiles)
					.queryString("type","webtest")
					.asJson();	
			JSONObject myObj = response.getBody().getObject();
			
			ResponseSDE responseSDE=new Gson().fromJson(response.getBody().toString(), ResponseSDE.class);

			return responseSDE;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	
//	public static String runRequestBatch(List<String>smiles) {
//		BatchRequest br=new BatchRequest(smiles);
//				
//		try {
//			
//			String jsonInString = new Gson().toJson(br);
////			System.out.println(jsonInString);
////			JSONObject mJSONObject = new JSONObject(jsonInString);
//			
//			HttpResponse<String> response = Unirest.post("http://localhost:"+port+"/TEST-descriptors/calculationBatch")		        				
//					.header("Content-Type", "application/json")
//
//					//Not working for some reason???
////					.body(br)
//					.body(jsonInString)
//					.asString();	
//			
//			return response.getBody();
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return e.getMessage();
//		}
//	}
	
	public static String runRequestBatchSDE(List<String>smiles) {
		RequestSDE br=new RequestSDE(smiles);
				
		try {
			
			String jsonInString = new Gson().toJson(br);
//			System.out.println(jsonInString);
//			JSONObject mJSONObject = new JSONObject(jsonInString);
			
			String urlBase="https://ml.sciencedataexperts.com/api/descriptors/";
					
			
			HttpResponse<String> response = Unirest.post(urlBase)		        				
					.header("Content-Type", "application/json")

					//Not working for some reason???
//					.body(br)
					.body(jsonInString)
					.asString();	
			
			return response.getBody();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	public static ResponseSDE runRequestBatchSDE2(List<String>smiles) {
		RequestSDE br=new RequestSDE(smiles);
				
		try {
			Gson gson=new Gson();
			String jsonInString = gson.toJson(br);
//			System.out.println(jsonInString);
//			JSONObject mJSONObject = new JSONObject(jsonInString);			
			
			System.out.println(jsonInString);

			
//			String urlBase="https://ml.sciencedataexperts.com/api/descriptors/";
			String urlBase="https://hazard-dev.sciencedataexperts.com/api/descriptors";		
			
			HttpResponse<JsonNode> response = Unirest.post(urlBase)		        				
					.header("Content-Type", "application/json")
					//Not working for some reason???
//					.body(br)
					.body(jsonInString)
					.asJson();	
						
			ResponseSDE responseSDE=gson.fromJson(response.getBody().toString(), ResponseSDE.class);
			
			return responseSDE;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static ResponseSDE runRequestBatchSDE2(RequestSDE request,boolean standardize) {
		
				
		try {
			Gson gson=new Gson();
			String jsonInString = gson.toJson(request);
//			System.out.println(jsonInString);
//			JSONObject mJSONObject = new JSONObject(jsonInString);			
			
//			System.out.println(jsonInString);

			
			if (standardize) {
				JsonObject jo=gson.fromJson(jsonInString, JsonObject.class);			
				JsonObject options=jo.getAsJsonObject("options");
				options.addProperty("standardizer-workflow", "qsar-ready");
				jsonInString=gson.toJson(jo);
			}
			
			
//			String urlBase="https://ml.sciencedataexperts.com/api/descriptors/";
			String urlBase="https://hazard-dev.sciencedataexperts.com/api/descriptors";		
			
			HttpResponse<JsonNode> response = Unirest.post(urlBase)		        				
					.header("Content-Type", "application/json")
					//Not working for some reason???
//					.body(br)
					.body(jsonInString)
					.asJson();	
						
			ResponseSDE responseSDE=gson.fromJson(response.getBody().toString(), ResponseSDE.class);
			
			return responseSDE;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		
		List<String>smiles=new ArrayList<>();
		smiles.add("C(C(C(=CC(F)(F)C(F)(F)C(F)(F)F)[N+]([O-])=O)O)O");
		
		RequestSDE request=new RequestSDE(smiles,"padel");
		
		
		ResponseSDE response=runRequestBatchSDE2(request,false);

		Gson gson=new Gson();
		String jsonInString = gson.toJson(response);
		
		System.out.println("response="+jsonInString);
		

		
	}

	
	public static String runRequestHeader() {
		
		String strHeader=null;

		try {
			
			HttpResponse<JsonNode> response = Unirest.get("http://localhost:"+port+"/TEST-descriptors/info")		        				
					.asJson();	
			JSONObject myObj = response.getBody().getObject();
			strHeader = myObj.getString("headersTsv");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		return strHeader;

	}
	
	public static String runRequestHeader3d() {
		
		String strHeader=null;

		try {
			
			HttpResponse<JsonNode> response = Unirest.get("http://localhost:"+port+"/TEST-3D-descriptors/info")		        				
					.asJson();	
			JSONObject myObj = response.getBody().getObject();
			strHeader = myObj.getString("headersTsv");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		return strHeader;

	}

	
	
	
		
	
	public static void configLogger() {
		Set<String> artifactoryLoggers = new HashSet<String>(Arrays.asList("org.apache.http", "groovyx.net.http"));
		for (String log : artifactoryLoggers) {
			ch.qos.logback.classic.Logger artLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
					.getLogger(log);
			artLogger.setLevel(ch.qos.logback.classic.Level.INFO);
			artLogger.setAdditive(false);
		}
	}
	
	

}
