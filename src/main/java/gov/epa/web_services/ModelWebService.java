package gov.epa.web_services;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.run_from_java.scripts.PredictionDashboard.valery.SDE_Prediction_Request;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 * Model building class that handles web service access
 * @author GSINCL01
 *
 */
public class ModelWebService extends WebService {

	public static int num_jobs=8;
	
	public ModelWebService(String server, int port) {
		super(server, port);
	}

	public HttpResponse<byte[]> callTrain(String trainingSet, String predictionSet,Boolean removeLogDescriptors, String qsarMethod, String modelId,
			boolean use_pmml,boolean include_standardization_in_pmml) {
		HttpResponse<byte[]> response = Unirest.post(address+"/models/{qsar_method}/train")
				.routeParam("qsar_method", qsarMethod)
				.field("use_pmml", use_pmml+"")
				.field("include_standardization_in_pmml", include_standardization_in_pmml+"")
				.field("training_tsv", trainingSet)
				.field("prediction_tsv", predictionSet)
				.field("num_jobs", String.valueOf(num_jobs))
				.field("model_id", modelId)
				.field("remove_log_p", String.valueOf(removeLogDescriptors))
				.asBytes();
		
		return response;
	}
	

	public void configUnirest(boolean turnOffLogging) {
		
		try {//Need to suppress logging because it slows things down when have big data sets...

			if (turnOffLogging) {
				Set<String> artifactoryLoggers = new HashSet<String>(Arrays.asList("org.apache.http", "groovyx.net.http"));
				for(String log:artifactoryLoggers) {
					ch.qos.logback.classic.Logger artLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(log);
					artLogger.setLevel(ch.qos.logback.classic.Level.INFO);
					artLogger.setAdditive(false);
				}
			}
			
			Unirest.config()
	        .followRedirects(true)   
			.socketTimeout(000)
	           .connectTimeout(000);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	public HttpResponse<byte[]> callTrainWithPreselectedDescriptors(String trainingSet,String predictionSet, Boolean removeLogDescriptors, 
			String qsarMethod, String modelId, String embeddingTsv,boolean use_pmml,boolean include_standardization_in_pmml) {
		HttpResponse<byte[]> response = Unirest.post(address+"/models/{qsar_method}/train")
				.routeParam("qsar_method", qsarMethod)
				.field("use_pmml", use_pmml+"")
				.field("include_standardization_in_pmml", include_standardization_in_pmml+"")
				.field("training_tsv", trainingSet)
				.field("prediction_tsv", predictionSet)
				.field("model_id", modelId)
				.field("num_jobs", String.valueOf(num_jobs))
				.field("embedding_tsv", embeddingTsv)
				.field("remove_log_p", String.valueOf(removeLogDescriptors))
				.asBytes();
		
		return response;
	}

	
	public HttpResponse<String> callTrainPythonStorage(String trainingSet, Boolean removeLogPDescriptors, String qsarMethod, String modelId) {
		HttpResponse<String> response = Unirest.post(address + "/models/{qsar_method}/trainsa")
				.routeParam("qsar_method", qsarMethod)				
				.field("training_tsv", trainingSet)
				.field("embedding_tsv", "")
				.field("model_id", modelId)
				.field("remove_log_p", String.valueOf(removeLogPDescriptors))
				.asString();
		
		System.out.println("status= " + response.getStatus());
		System.out.println(response.getBody());
		return response;
	}
	
	public HttpResponse<String> callTrainWithPreselectedDescriptorsPythonStorage(String trainingSet, Boolean removeLogPDescriptors, 
			String qsarMethod, String modelId, String embeddingTsv) {
			HttpResponse<String> response = Unirest.post(address + "/models/{qsar_method}/trainsa")
				.routeParam("qsar_method", qsarMethod)
				.field("training_tsv", trainingSet)
				.field("embedding_tsv", embeddingTsv)
				.field("model_id", modelId)
				.field("remove_log_p", String.valueOf(removeLogPDescriptors))
				.asString();
			System.out.println("status= " + response.getStatus());
			System.out.println(response.getBody());
		return response;
	}
	
	
	public HttpResponse<String> callPredictionApplicabilityDomain(String trainingSet,String testSet, Boolean removeLogDescriptors,
			String embeddingTsv, String applicability_domain) {
		
		HttpResponse<String> response= Unirest.post(address + "/models/prediction_applicability_domain")
				.field("training_tsv", trainingSet)
				.field("test_tsv", testSet)
				.field("embedding_tsv", embeddingTsv)
				.field("remove_log_p", String.valueOf(removeLogDescriptors))
				.field("applicability_domain", applicability_domain).asString();

		return response;
	}

	public HttpResponse<String> callPredictionApplicabilityDomain(String trainingSet,String testSet, Boolean removeLogDescriptors,
			String applicability_domain) {
		HttpResponse<String> response= Unirest.post(address + "/models/prediction_applicability_domain")
				.field("training_tsv", trainingSet)
				.field("test_tsv", testSet)
				.field("remove_log_p", String.valueOf(removeLogDescriptors))
				.field("applicability_domain", applicability_domain).asString();
		return response;
	}

	
	public HttpResponse<String> crossValidate(String qsarMethod,String training_tsv,String prediction_tsv,
			boolean remove_log_p, int num_jobs,String embeddingTsv,String params,boolean use_pmml) {

//		System.out.println(this.address+ "/models/" + qsarMethod +"/cross_validate");

		HttpResponse<String> response = Unirest.post(this.address+ "/models/{qsar_method}/cross_validate")
				.routeParam("qsar_method", qsarMethod)
				.field("use_pmml",use_pmml)
				.field("training_tsv",training_tsv)
				.field("prediction_tsv", prediction_tsv)
				.field("remove_log_p",String.valueOf(remove_log_p))
				.field("num_jobs",String.valueOf(num_jobs))
				.field("embedding_tsv", embeddingTsv)
				.field("hyperparameters", params)
				.asString();
		return response;
	}

	public HttpResponse<String> crossValidate(String qsarMethod,String training_tsv,String prediction_tsv,
			boolean remove_log_p, int num_jobs,String params,boolean use_pmml) {

//		System.out.println(this.address+ "/models/" + qsarMethod +"/cross_validate");

		HttpResponse<String> response = Unirest.post(this.address+ "/models/{qsar_method}/cross_validate")
				.routeParam("qsar_method", qsarMethod)
				.field("use_pmml",use_pmml)
				.field("training_tsv",training_tsv)
				.field("prediction_tsv", prediction_tsv)
				.field("remove_log_p",String.valueOf(remove_log_p))
				.field("num_jobs",String.valueOf(num_jobs))
				.field("hyperparameters", params)
				.asString();
		return response;
	}
	
	public HttpResponse<String> callDetails(String modelId) {
		System.out.println(address+"/models/" + modelId);
		HttpResponse<String> response = Unirest.get(address+"/models/{model_id}")
				.routeParam("model_id", modelId)
				.asString();
		
		return response;
	}

	public HttpResponse<String> callInfo(String qsarMethod) {
		HttpResponse<String> response = Unirest.get(address+"/models/{qsar_method}/info")
				.routeParam("qsar_method", qsarMethod)
				.asString();
		
		return response;
	}

	static class InitRequest {
		public String model_id;
		
	}
	
	public HttpResponse<String> callInitPickle(byte[] modelBytes, String modelId) {
		InputStream model = new BufferedInputStream(new ByteArrayInputStream(modelBytes));
		
		HttpResponse<String> response = Unirest.post(address+"/models/initPickle")
				.field("model_id", modelId)
				.field("model", model, "model.bin")
				.asString();
		
		System.out.println("Status of init call = "+response.getStatus());
		
		return response;
	}
	
	
	
	
	public HttpResponse<String> callInitPmml(byte[] modelBytes, String modelId,String details,boolean use_sklearn2pmml) {
		Gson gson=new Gson();
//		System.out.println(details);

		JsonObject jo=gson.fromJson(details, JsonObject.class);
		String model=new String (modelBytes);
//		System.out.println(model);
		jo.addProperty("model_id", modelId);
		jo.addProperty("model", model);
		jo.addProperty("use_sklearn2pmml", use_sklearn2pmml);
				
		String body=gson.toJson(jo);
		
		HttpResponse<String> response = Unirest.post(address+"/models/initPMML")
				.header("Content-Type", "application/json")
				.body(body)				
				.asString();
		
		System.out.println("Status of init call = "+response.getStatus()+"\t"+response.getStatusText());
		return response;

	}
	
	void testCallInit() {
		
		Unirest.config()
        .followRedirects(true)   
		.socketTimeout(000)
        .connectTimeout(000);

		Long model_id=457L;
		boolean use_pmml=true;
		boolean useSklearn2pmml=false;
				
//		Long model_id=272L;
//		boolean use_pmml=false;
				
		ModelService modelService = new ModelServiceImpl();
		ModelBytesService modelBytesService = new ModelBytesServiceImpl();
		Model model=modelService.findById(model_id);

		System.out.print("Getting model bytes...");
//		byte[]modelBytes=modelBytesService.findByModelId(model_id,decompress).getBytes();
		
		byte[]modelBytes=modelBytesService.getBytesSql(model_id,use_pmml);
		System.out.println("Got "+ modelBytes.length +" bytes");		
		
		if (use_pmml) {
			String details=new String(model.getDetails());
			String result=callInitPmml(modelBytes,model_id+"", details, useSklearn2pmml).getBody().toString();
			System.out.print("result="+result);
		} else {
			String result=callInitPickle(modelBytes,model_id+"").getBody().toString();
			System.out.print("result="+result);
		}
	}
 	
	public static void main(String[] args) {
		
		ModelWebService m=new ModelWebService("http://localhost",5004);
		m.testCallInit();
	}

	public HttpResponse<String> callPredict(String predictionSet, String modelId) {
		HttpResponse<String> response = Unirest.post(address+"/models/predict")
				.field("prediction_tsv", predictionSet)
				.field("model_id", modelId)
				.asString();
		
		return response;
	}
	
	
	
	public HttpResponse<String> callPredictSDE(String predictionSet, String modelSetId,String datasetId, String workflow,boolean use_cache) {
		
		SDE_Prediction_Request request=new SDE_Prediction_Request();
		
		request.getFromTSV(predictionSet,modelSetId,datasetId, workflow,use_cache);
		

		Gson gson=new Gson();
		String body=gson.toJson(request);
		
		System.out.println(body);
		
		HttpResponse<String> response = Unirest.post(address+"/api/predictor/predict")
				.header("Content-Type", "application/json")
				.body(body)				
				.asString();
		
		System.out.println(response.getBody());
				
		return response;
	}

	
	public HttpResponse<String> callPredictSQLAlchemy(String predictionSet, String qsarMethod, String modelId) {
		HttpResponse<String> response = Unirest.post(address+"/models/{qsar_method}/predictsa")
				.routeParam("qsar_method", qsarMethod)
				.field("prediction_tsv", predictionSet)
				.field("model_id", modelId)
				.asString();
		System.out.println(response.getStatus());
		return response;
	}

	
//	public HttpResponse<String> callDescriptors(byte[] modelBytes, String qsarMethod, String modelId) {
//		InputStream model = new BufferedInputStream(new ByteArrayInputStream(modelBytes));
//		HttpResponse<String> response = Unirest.post(address+"/models/{qsar_method}/descriptors")
//				.routeParam("qsar_method", qsarMethod)
//				.field("model_id", modelId)
//				.field("model", model, "model.bin")
//				.asString();
//		
//		return response;
//	}


}
