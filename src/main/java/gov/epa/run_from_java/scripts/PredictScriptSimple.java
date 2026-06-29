package gov.epa.run_from_java.scripts;

import java.io.*;
import java.sql.*;
import java.util.List;

import com.google.gson.*;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import gov.epa.run_from_java.scripts.PredictScriptSimple.ModelInit.ModelDetails;
import gov.epa.run_from_java.scripts.PredictScriptSimple.PredictAPI.ModelPrediction;
import gov.epa.run_from_java.scripts.PredictScriptSimple.PredictAPI.ModelResults;

/**
 * @author TMARTI02
 */
public class PredictScriptSimple {

	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
			.serializeSpecialFloatingPointValues().create();


	class PredictAPI {
		
		public static HttpResponse<String> callPredict(String serverHost, String predictionSet, long modelId) {
			HttpResponse<String> response = Unirest.post(serverHost+"/models/predict")
					.field("prediction_tsv", predictionSet)
					.field("model_id", modelId+"")
					.asString();
			return response;
		}
		
		public static ModelPrediction[] getModelPredictions (HttpResponse<String>response) {
			ModelPrediction[]mps=gson.fromJson(response.getBody().toString(), ModelPrediction[].class);
			return mps;
		}
		
		class ModelPrediction {
			public String id;
			public Double exp;
			public Double pred;
			public Double weight;
			public Integer split;//T=0,P=1
			public String methodAbbrev;
			public Boolean insideAD;
		}
		
		static class ModelResults {

			String smiles;
			String qsarSmiles;
			ModelDetails modelDetails;
			Double predictionValue;
			String predictionUnits;
			ADResult adResult;
			
			public ModelResults(String smiles, String qsarSmiles, Double predictionValue, ModelDetails modelDetails,ADResult adResult) {
				this.smiles=smiles;
				this.qsarSmiles=qsarSmiles;
				this.modelDetails=modelDetails;
				this.predictionValue=predictionValue;
				this.predictionUnits=modelDetails.unitsName;
				
				this.adResult=adResult;
			}
		}

		
		/**
		 * 
		 * @param serverHost
		 * @param modelDetails
		 * @param predictionTsv the new chemicals being predicted
		 * @return
		 */
		public static HttpResponse<String> callPredictionApplicabilityDomain(String serverHost, ModelDetails modelDetails,
				String predictionTsv) {
			
//			System.out.println(address + "/models/prediction_applicability_domain");
			
			boolean removeLogPDescriptors=false;//probably dont need to remove from dataframe for AD calculations (mostly used when training LogP models for fairness)
			
			HttpResponse<String> response= Unirest.post(serverHost + "/models/prediction_applicability_domain")
					.field("training_tsv", modelDetails.trainingSetTsv)
					.field("test_tsv", predictionTsv)
					.field("embedding_tsv", modelDetails.descriptorEmbeddingTsv) //TODO does this work when embedding is null?
					.field("remove_log_p", String.valueOf(removeLogPDescriptors))
					.field("applicability_domain", modelDetails.applicabilityDomainName).asString();
			
								
//			System.out.println(response.getStatus()+"\t"+response.getStatusText());
			return response;
		}

	}
	
	static class ADResult {
		String idTest;
		//TODO store neighbors as a list instead we want to change to 5 neighbors
		String idNeighbor1;
		String idNeighbor2;
		String idNeighbor3;
		Boolean AD;
	}
	
	
	class DescriptorAPI {
	
		public static HttpResponse<String> calculateDescriptorsAsString(String server, String smiles, String descriptorName) {
//			System.out.println(address + "/api/descriptors");
//			System.out.println(descriptorName);
			HttpResponse<String> response = Unirest.get(server + "/api/descriptors")
			.queryString("type", descriptorName)
			.queryString("smiles", smiles)
			.queryString("headers", true) //doesnt work for all descriptor sets
			.asString();
			return response;
		}
		
		
		static class DescriptorResponse {
			
			Info info;
			List<String>headers;
			Options options;
			List<Chemical>chemicals;
			
			class Options {
				boolean headers;
			}
			
			class Info {
				String name;
				String version;
			}
			
			class Chemical {
				int ordinal;
				String smiles;
				String inchi;
				String inchiKey;
				List<Double>descriptors;
			}
		}
		
		
		public static String createTabDelimitedString(List<Double> descriptors) {
	        StringBuilder sb = new StringBuilder();
	        for (int i = 0; i < descriptors.size(); i++) {
	            sb.append(descriptors.get(i));
	            if (i < descriptors.size() - 1) {
	                sb.append("\t");
	            }
	        }
	        return sb.toString();
	    }
		
		public static String getDescriptorTsv(String server,String qsarSmiles,String descriptorSetName) {
			
			
			String strResponse=calculateDescriptorsAsString(server,qsarSmiles, descriptorSetName).getBody();
			
			DescriptorResponse dr=gson.fromJson(strResponse,DescriptorResponse.class);
//			System.out.println(gson.toJson(dr));
//			System.out.println(strResponse);
			
			if(dr.chemicals==null) return null;

			String tsv="ID\tProperty\t"+String.join("\t",dr.headers)+"\r\n";
			tsv+=qsarSmiles+"\t-9999\t"+createTabDelimitedString(dr.chemicals.get(0).descriptors)+"\r\n";
			
			return tsv;

		}
		
		
	}
	
	
	class ModelInit {
		
		
		
		public static byte [] getModelBytes(Long modelId) {
			String sql="select bytes from qsar_models.model_bytes where fk_model_id="+modelId+" order by id";
			Connection conn=SqlUtilities.getConnectionPostgres();

			try {
				ResultSet rs=SqlUtilities.runSQL2(conn, sql);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
				int counter=1;
				while (rs.next()) {
//					System.out.println(counter++);
					outputStream.write(rs.getBytes(1));
				}
				return outputStream.toByteArray();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		
		public static HttpResponse<String> callInitPickle(String serverPredict, byte[] modelBytes, Long modelId) {
			InputStream model = new BufferedInputStream(new ByteArrayInputStream(modelBytes));
			
			HttpResponse<String> response = Unirest.post(serverPredict+"/models/initPickle")
					.field("model_id", modelId)
					.field("model", model, "model.bin")
					.asString();
			System.out.println("Status of model init call = "+response.getStatus());
			return response;
		}
		
		public HttpResponse<String> callInitPickle(String serverPredict,byte[] modelBytes, String modelId) {
			InputStream model = new BufferedInputStream(new ByteArrayInputStream(modelBytes));
			
			HttpResponse<String> response = Unirest.post(serverPredict+"/models/initPickle")
					.field("model_id", modelId)
					.field("model", model, "model.bin")
					.asString();
			
			System.out.println("Status of model init call = "+response.getStatus());
			return response;
		}
		
		
		private static String generateInstance(String smiles, String qsar_property_value, String valuesTsv) {
			if (valuesTsv==null) return null;
			//TODO need to go through all instances of overall set and remove bad columns instead of rejecting rows
			if (valuesTsv.toLowerCase().contains("error")) {
				System.out.println("error in tsv\t"+smiles+"\t"+valuesTsv);
				return null;
			}
			return smiles + "\t" + qsar_property_value+ "\t" + valuesTsv + "\r\n";
		}
		
		
		/**
		 * Gets the training and test set Descriptor tsvs from the database
		 * Theoretically could just ping the descriptor API to regenerate but that would take much longer
		 * 
		 * @param modelDetails
		 */
		public static void getTrainingPredictionInstances(ModelDetails modelDetails) {
			
			Integer TRAIN_SPLIT_NUM = 0;
			Integer TEST_SPLIT_NUM = 1;
			
			boolean debug=true;
			boolean useDTXCIDs=false;
			String idField="canon_qsar_smiles";
			if(useDTXCIDs) idField="qsar_dtxcid";
			
			if (debug) {
				System.out.println("Getting training/test set tsvs");
			}
			
			String instanceHeader="ID\tProperty\t"+modelDetails.headersTsv+"\r\n";
			String sql="select dp."+idField+", dp.qsar_property_value, dv.values_tsv, dpis.split_num from qsar_datasets.data_points dp\n"+ 
			"join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles\n"+ 
			"join qsar_datasets.data_points_in_splittings dpis on dpis.fk_data_point_id = dp.id\n"+ 
			"where dp.fk_dataset_id="+modelDetails.datasetId+" and dv.fk_descriptor_set_id="+modelDetails.descriptorSetId+
			" and dpis.fk_splitting_id="+modelDetails.splittingId+ //could just do some more joins and not have to store the ids
			"order by dp."+idField+";";
			
//			System.out.println("\n"+sql);

			StringBuilder sbTraining = new StringBuilder(instanceHeader);
			StringBuilder sbPrediction = new StringBuilder(instanceHeader);

			int counterTrain=0;
			int counterTest=0;
			
			try {
				
				ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
				
				while (rs.next()) {
					
//					if (counter%1000==0) System.out.println(counter+ "\tbuilding instances");
					
					String id=rs.getString(1);
					String qsar_property_value=rs.getString(2);
					String descriptors=rs.getString(3);
					int splitNum=Integer.parseInt(rs.getString(4));
					
					String instance=generateInstance(id, qsar_property_value, descriptors);
					
					if (instance==null && debug) {
						System.out.println(id+"\tnull instance\tdatasetName="+modelDetails.datasetName+"\tdescriptorSetName="+modelDetails.descriptorSetName);
						continue;
					}

					if (splitNum==TRAIN_SPLIT_NUM) {
						sbTraining.append(instance);
						counterTrain++;

//						System.out.print(instance);
						
					} else if (splitNum==TEST_SPLIT_NUM) {
						sbPrediction.append(instance);
						counterTest++;
					} else {
//						System.out.println(splitNum);
					}
				}
				
				if (debug) {
					System.out.println("Training instances created:"+counterTrain);
					System.out.println("Test instances created:"+counterTest);
				}
				
				modelDetails.trainingSetTsv= sbTraining.toString();
				modelDetails.testSetTsv= sbPrediction.toString();
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		
		static class ModelDetails {
			
			String modelName;
			
			Long datasetId;
			String datasetName;
			String unitsName;
			String dsstox_mapping_strategy;
			Boolean omitSalts;
			String qsarReadyRuleSet;
			String propertyName;
			
			Long descriptorSetId;
			String descriptorSetName;
			String descriptorService;
			String headersTsv;

			Long splittingId;
			String splittingName;

			String applicabilityDomainName;
			String descriptorEmbeddingTsv;
			
			
			transient String  trainingSetTsv;//transient omits from json serialization
			transient String  testSetTsv;
			
		}
		
		public static ModelDetails getModelDetails (long modelId) {
			
			String sql="\r\nselect "
					+ "m.name_ccd,"
					+ "d.id,"
					+ "d.name,"
					+ "u.abbreviation_ccd,"
					+ "d.dsstox_mapping_strategy,"
					+ "p.name_ccd, "
					+ "ds.id, "
					+ "ds.name, "
					+ "ds.descriptor_service, "
					+ "ds.headers_tsv, "
					+ "s.id, "
					+ "s.name, "
					+ "adm.name,"
					+ "de.embedding_tsv\n"
					+ "from qsar_models.models m\r\n"
					+ "join qsar_datasets.datasets d on d.name=m.dataset_name\r\n"
					+ "join qsar_datasets.units u on d.fk_unit_id = u.id\r\n"
					+ "join qsar_datasets.properties p on d.fk_property_id = p.id\r\n"
					+ "join qsar_descriptors.descriptor_sets ds on m.descriptor_set_name=ds.name\r\n"
					+ "join qsar_datasets.splittings s on m.splitting_name=s.name\r\n"
					+ "join qsar_models.ad_methods adm on m.fk_ad_method = adm.id\r\n"
					+ "join qsar_models.descriptor_embeddings de on m.fk_descriptor_embedding_id = de.id\r\n"
					+ "where m.id="+modelId+";";
			
//			System.out.println(sql);
			
			try {
				
				ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
				
				while (rs.next()) {
					ModelDetails m=new ModelDetails();
					int col=1;
					m.modelName=rs.getString(col++);
					m.datasetId=rs.getLong(col++);
					m.datasetName=rs.getString(col++);
					m.unitsName=rs.getString(col++);
					m.dsstox_mapping_strategy=rs.getString(col++);
					m.propertyName=rs.getString(col++);
					
					m.descriptorSetId=rs.getLong(col++);
					m.descriptorSetName=rs.getString(col++);
					m.descriptorService=rs.getString(col++);
					m.headersTsv=rs.getString(col++);
					
					m.splittingId=rs.getLong(col++);
					m.splittingName=rs.getString(col++);
					
					m.applicabilityDomainName=rs.getString(col++);
					m.descriptorEmbeddingTsv=rs.getString(col++);
					
					JsonObject jo=gson.fromJson(m.dsstox_mapping_strategy,JsonObject.class);
					
					if(jo.get("omitSalts")!=null && !jo.get("omitSalts").isJsonNull()) {
						m.omitSalts=jo.get("omitSalts").getAsBoolean();
					}
					
					//TODO note- the qsarReadyRuleSet is NOT currently stored in the Json...
					if(jo.get("qsarReadyRuleSet")!=null && !jo.get("qsarReadyRuleSet").isJsonNull()) {
						m.qsarReadyRuleSet=jo.get("qsarReadyRuleSet").getAsString();
					} else {
						m.qsarReadyRuleSet="qsar-ready";
					}
					
					return m;
				}
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;
		}
	}
	
	class QsarSmilesAPI {
		
		
		public static String getQsarReadySmilesFromPostJson(String json,boolean full) {

			if (full) {
				return handleFullOutputSingleChemical(json);
			} else {
				return handleSimpleOutputSingleChemical(json);
			}

		}
		
		private static String handleSimpleOutputSingleChemical(String json) {
			JsonArray results=gson.fromJson(json, JsonArray.class);

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
		
		private static String handleFullOutputSingleChemical(String json) {
			JsonObject jo=gson.fromJson(json, JsonObject.class);

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

//					System.out.println(i+"\t"+Utilities.gson.toJson(record)+"\n\n");	
//					System.out.println(i+"\t"+smiles_i);
				}
				
				return smiles;
			}
			
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

//					System.out.println(severity+"\t"+message);
					
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
		
		
		

		public static HttpResponse<String> callQsarReadyStandardizePost(String smiles, String serverHost,
				boolean full, String workflow) {
			// Unirest.setTimeouts(0, 0);
			JsonObject joBody = new JsonObject();

			joBody.addProperty("full", full);

			JsonObject joOptions = new JsonObject();
			joOptions.addProperty("workflow", workflow);
			joBody.add("options", joOptions);

			JsonArray chemicals = new JsonArray();
			JsonObject chemical = new JsonObject();
			chemical.addProperty("smiles", smiles);
			chemicals.add(chemical);
			joBody.add("chemicals", chemicals);

			// System.out.println(Utilities.gson.toJson(joBody));
			HttpResponse<String> response = Unirest.post(serverHost + "/api/stdizer/chemicals")
					.header("Content-Type", "application/json").body(gson.toJson(joBody)).asString();
			return response;

		}
		
		/**
		 *  convert back and forth to get prettyprinting
		 * @param response
		 * @param full
		 * @return
		 */
		public static String getResponseBody(HttpResponse<String> response, boolean full) {
			Object responseBody=gson.fromJson(response.getBody(), Object.class);
			return gson.toJson(responseBody);
		}
	}
	
	
	/**
	 * Runs everything from Java code contained in this class
	 * 
	 * @param smiles
	 * @param modelId
	 */
	void runPredictionWithAD(String smiles, long modelId) {

		int port_model = 5004;
		String serverPredict = "http://localhost:" + port_model;
//		String serverAPIs = "https://hcd.rtpnc.epa.gov";
		String serverAPIs= "https://cim-dev.sciencedataexperts.com";

		
		boolean initModel = true;// set to false if model already loaded in web service
		boolean use_pmml = false;//models are currently pickled sklearn models and not pmml format
		boolean useFullStandardize = false;
		
		try {
//
			//Get model details:
			ModelDetails modelDetails=ModelInit.getModelDetails(modelId);
			
			if (initModel) {
				
				//TODO could also init the training set and have a more 
				// complex object to store all model data such as predictions
				//for training and test set chemicals for report
				
				byte[] bytes = ModelInit.getModelBytes(modelId);
				System.out.println("Model bytes="+bytes.length);
				
				if (use_pmml) {
//					String details=new String(model.getDetails());
//					modelWebService.callInitPmml(bytes, modelId+"", details,use_sklearn2pmml);
				} else {
					ModelInit.callInitPickle(serverPredict, bytes,modelId);
				}
				ModelInit.getTrainingPredictionInstances(modelDetails);
			}
			
//			System.out.println("modelDetails:\n"+gson.toJson(modelDetails));
			
			HttpResponse<String> standardizeResponse = QsarSmilesAPI.callQsarReadyStandardizePost(smiles, serverAPIs, useFullStandardize,
					modelDetails.qsarReadyRuleSet);

			System.out.println("status=" + standardizeResponse.getStatus());

			if (standardizeResponse.getStatus() != 200) {
				System.out.println(standardizeResponse.getStatusText());
				return;
			}
			
			String jsonResponse = QsarSmilesAPI.getResponseBody(standardizeResponse, useFullStandardize);
			String qsarSmiles = QsarSmilesAPI.getQsarReadySmilesFromPostJson(jsonResponse,
						useFullStandardize);
//			System.out.println(jsonResponse);
			System.out.println("qsarSmiles="+qsarSmiles);
			
			String predictionTSV=DescriptorAPI.getDescriptorTsv(serverAPIs, qsarSmiles, modelDetails.descriptorService);
			System.out.println(predictionTSV);


			HttpResponse<String> responsePredict =PredictAPI.callPredict(serverPredict, predictionTSV, modelId);
			
			if (responsePredict.getStatus() != 200) {
				System.out.println(responsePredict.getStatusText());
				return;
			}

			ModelPrediction[]modelPredictions = PredictAPI.getModelPredictions(responsePredict); 
			double pred=modelPredictions[0].pred;
	
			HttpResponse<String>responseAD=PredictAPI.callPredictionApplicabilityDomain(serverPredict, modelDetails,predictionTSV);
			
			//TODO fix AD code to exclude neighbor if it matches smiles of compound being predicted? Or is that ok since should be predicted well? OPERA doesnt exclude it
			
			if (responseAD.getStatus() != 200) {
				System.out.println(responseAD.getStatusText());
				return;
			}
			
			ADResult adResult=gson.fromJson(responseAD.getBody(), ADResult.class);
			ModelResults modelResults=new ModelResults(smiles,qsarSmiles,pred, modelDetails,adResult);
			System.out.println("modelResults:\n"+gson.toJson(modelResults));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	

	/**
	 * Run everything from python:
	 * 
	 * @param smiles
	 * @param modelId
	 */
	void pingAPI(String smiles,Long modelId) {
		int port_model = 5004;
		String serverPredict = "http://localhost:" + port_model;

		HttpResponse<String> response = Unirest.post(serverPredict+"/models/predictDB")
				.field("smiles", smiles)
				.field("model_id", modelId+"")
				.asString();
		
		if(response.getStatus()!=200) {
			System.out.println(response.getStatusText()+"\t"+response.getBody());
			
		} else {
			System.out.println(response.getBody());

		}
		
	}
	
	public static void main(String[] args) {
		PredictScriptSimple p=new PredictScriptSimple();
		
		System.out.println("herro");
		if(true)return;

		//Run from python
		p.pingAPI("c1ccccc1", 1068L);//benzene BP model
		p.pingAPI("[O-][N+](=O)C1=CC(=C(C=C1)ON=CC1=CC(Br)=C(O)C(Br)=C1)[N+]([O-])=O", 1614L);//outside AD
		p.pingAPI("[O-][N+](=O)C1=CC(=C(C=C1)ON=CC1=CC(Br)=C(O)C(Br)=C1)[N+]([O-])=O", 1613L);//no embedding

		
		//Run everything from java:		
		p.runPredictionWithAD("c1ccccc1", 1068L);//benzene and BP model
		
		
		//Run everything from python:
//		p.pingAPI("c1ccccc1", 1068L);
//		p.pingAPI("c1ccccc1", 1613L);//model has no embedding
		
		

		
//		p.pingAPI("[O-][N+](=O)C1=CC(=C(C=C1)ON=CC1=CC(Br)=C(O)C(Br)=C1)[N+]([O-])=O", 1614L);//outside AD?
		
		
//		p.pingAPI("CC[Se]CC", 1068L);//invalid element
//		p.pingAPI("C", 1068L);//only 1 atom
//		p.pingAPI("SS", 1068L);//no carbon
		
		
//		for (int i=1;i<=20;i++)
//			p.pingAPI("[O-][N+](=O)C1=CC(=C(C=C1)ON=CC1=CC(Br)=C(O)C(Br)=C1)[N+]([O-])=O", 1614L);//outside AD for Koc XGB model

		//TODO get code working for model without an embedding like 1613
		
		
//		p.pingAPI("SSSSS", 1068L);
//		p.pingAPI("C", 1068L);
//		p.pingAPI("COCOCOCOC.CCCC", 1068L);

	}

}
