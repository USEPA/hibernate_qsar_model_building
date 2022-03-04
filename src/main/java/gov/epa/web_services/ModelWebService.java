package gov.epa.web_services;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

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
		HttpResponse<byte[]> response = Unirest.post(server+":"+port+"/models/{qsar_method}/train")
				.routeParam("qsar_method", qsarMethod)
				.field("training_tsv", trainingSet)
				.field("embedding_tsv", "")
				.field("model_id", modelId)
				.field("remove_log_p", String.valueOf(removeLogDescriptors))
				.asBytes();
		
		return response;
	}
	
	public HttpResponse<byte[]> callTrainWithPreselectedDescriptors(String trainingSet, Boolean removeLogDescriptors, 
			String qsarMethod, String modelId, String embeddingTsv) {
		HttpResponse<byte[]> response = Unirest.post(server+":"+port+"/models/{qsar_method}/train")
				.routeParam("qsar_method", qsarMethod)
				.field("training_tsv", trainingSet)
				.field("model_id", modelId)
				.field("embedding_tsv", embeddingTsv)
				.field("remove_log_p", String.valueOf(removeLogDescriptors))
				.asBytes();
		
		return response;
	}

	public HttpResponse<String> callDetails(String qsarMethod, String modelId) {
		HttpResponse<String> response = Unirest.get(server+":"+port+"/models/{qsar_method}/{model_id}")
				.routeParam("qsar_method", qsarMethod)
				.routeParam("model_id", modelId)
				.asString();
		
		return response;
	}

	public HttpResponse<String> callInfo(String qsarMethod) {
		HttpResponse<String> response = Unirest.get(server+":"+port+"/models/{qsar_method}/info")
				.routeParam("qsar_method", qsarMethod)
				.asString();
		
		return response;
	}

	public HttpResponse<String> callInit(byte[] modelBytes, String qsarMethod, String modelId) {
		InputStream model = new BufferedInputStream(new ByteArrayInputStream(modelBytes));
		HttpResponse<String> response = Unirest.post(server+":"+port+"/models/{qsar_method}/init")
				.routeParam("qsar_method", qsarMethod)
				.field("model_id", modelId)
				.field("model", model, "model.bin")
				.asString();
		
		return response;
	}

	public HttpResponse<String> callPredict(String predictionSet, String qsarMethod, String modelId) {
		HttpResponse<String> response = Unirest.post(server+":"+port+"/models/{qsar_method}/predict")
				.routeParam("qsar_method", qsarMethod)
				.field("prediction_tsv", predictionSet)
				.field("model_id", modelId)
				.asString();
		
		return response;
	}
	
	public HttpResponse<String> callDescriptors(byte[] modelBytes, String qsarMethod, String modelId) {
		InputStream model = new BufferedInputStream(new ByteArrayInputStream(modelBytes));
		HttpResponse<String> response = Unirest.post(server+":"+port+"/models/{qsar_method}/descriptors")
				.routeParam("qsar_method", qsarMethod)
				.field("model_id", modelId)
				.field("model", model, "model.bin")
				.asString();
		
		return response;
	}
}
