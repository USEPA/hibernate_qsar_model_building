package gov.epa.web_services;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import gov.epa.web_services.embedding_service.CalculationInfo;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 * Model building class that handles web service access
 * @author GSINCL01
 *
 */
public class ModelWebService extends WebService {

	public ModelWebService(String server, int port) {
		super(server, port);
	}

	public HttpResponse<byte[]> callTrain(String trainingSet, Boolean removeLogDescriptors, String qsarMethod, String modelId) {
		HttpResponse<byte[]> response = Unirest.post(address+"/models/{qsar_method}/train")
				.routeParam("qsar_method", qsarMethod)
				.field("training_tsv", trainingSet)
				.field("embedding_tsv", "")
				.field("model_id", modelId)
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
	
	public HttpResponse<byte[]> callTrainWithPreselectedDescriptors(String trainingSet, Boolean removeLogDescriptors, 
			String qsarMethod, String modelId, String embeddingTsv) {
		HttpResponse<byte[]> response = Unirest.post(address+"/models/{qsar_method}/train")
				.routeParam("qsar_method", qsarMethod)
				.field("training_tsv", trainingSet)
				.field("model_id", modelId)
				.field("embedding_tsv", embeddingTsv)
				.field("remove_log_p", String.valueOf(removeLogDescriptors))
				.asBytes();
		
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

	
	public HttpResponse<String> crossValidate(String qsarMethod,String training_tsv,String prediction_tsv,
			boolean remove_log_p, int num_jobs,String embeddingTsv,String params) {

//		System.out.println(this.address+ "/models/" + qsarMethod +"/cross_validate");

		HttpResponse<String> response = Unirest.post(this.address+ "/models/{qsar_method}/cross_validate")
				.routeParam("qsar_method", qsarMethod)
				.field("training_tsv",training_tsv)
				.field("prediction_tsv", prediction_tsv)
				.field("remove_log_p",String.valueOf(remove_log_p))
				.field("num_jobs",String.valueOf(num_jobs))
				.field("embedding_tsv", embeddingTsv)
				.field("params", params)
				.asString();
		return response;
	}

	
	public HttpResponse<String> callDetails(String qsarMethod, String modelId) {
		System.out.println(address+"/models/" + qsarMethod + "/" + modelId);
		HttpResponse<String> response = Unirest.get(address+"/models/{qsar_method}/{model_id}")
				.routeParam("qsar_method", qsarMethod)
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

	public HttpResponse<String> callInit(byte[] modelBytes, String qsarMethod, String modelId) {
		InputStream model = new BufferedInputStream(new ByteArrayInputStream(modelBytes));
		HttpResponse<String> response = Unirest.post(address+"/models/{qsar_method}/init")
				.routeParam("qsar_method", qsarMethod)
				.field("model_id", modelId)
				.field("model", model, "model.bin")
				.asString();
		
		return response;
	}

	public HttpResponse<String> callPredict(String predictionSet, String qsarMethod, String modelId) {
		HttpResponse<String> response = Unirest.post(address+"/models/{qsar_method}/predict")
				.routeParam("qsar_method", qsarMethod)
				.field("prediction_tsv", predictionSet)
				.field("model_id", modelId)
				.asString();
		
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

	
	public HttpResponse<String> callDescriptors(byte[] modelBytes, String qsarMethod, String modelId) {
		InputStream model = new BufferedInputStream(new ByteArrayInputStream(modelBytes));
		HttpResponse<String> response = Unirest.post(address+"/models/{qsar_method}/descriptors")
				.routeParam("qsar_method", qsarMethod)
				.field("model_id", modelId)
				.field("model", model, "model.bin")
				.asString();
		
		return response;
	}

}
