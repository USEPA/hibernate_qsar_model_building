package gov.epa.web_services;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingService;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingServiceImpl;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.models.WebServiceModelBuilder;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class EmbeddingWebService extends WebService {

	Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	public EmbeddingWebService(String server, int port) {
		super(server, port);
	}

	
	public static class EmbeddingCalculationResponse {
		public String embedding;
		public String importances;
	}
	
	public static HttpResponse<String> findEmbedding(String tsv, int numDesc) {
		HttpResponse<String> response = Unirest.get(DevQsarConstants.SERVER_819+":9092/findembedding/")
				.queryString("tsv", tsv)
				.asString();
		return response;
	}
	
	/*
	public void callCalculation2(String ex) {
		HttpResponse<Integer> response = Unirest.get(DevQsarConstants.SERVER_LOCAL+":9092/printstring")
				.queryString("string", ex)
				.asObject(Integer.class);
		
	}
	*/

	
	public void createEmbedding(String datasetName, String lanId, String descriptorSetName,
			String splittingName, Boolean removeLogDescriptors, int numDescriptors,
			String embeddingName, String embeddingDescription, Boolean writeToDatabase) {
		WebServiceModelBuilder wsmb = new WebServiceModelBuilder(null, lanId);
		ModelData data = wsmb.initModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors);

		
		HttpResponse<String> response = findEmbedding(data.trainingSetInstances, numDescriptors);

		
		EmbeddingCalculationResponse obj = gson.fromJson(response.getBody(), EmbeddingCalculationResponse.class);

		System.out.println("embedding=" + obj.embedding);
		System.out.println("importances=" + obj.importances);
		
		DescriptorEmbedding desE = new DescriptorEmbedding();
				
		desE.setDatasetName(datasetName);
		desE.setCreatedBy(lanId);
		desE.setDescription(embeddingDescription);
		desE.setDescriptorSetName(descriptorSetName);
		desE.setEmbeddingTsv(obj.embedding);
		desE.setName(embeddingName + Integer.valueOf(numDescriptors).toString());
		desE.setDatasetName(datasetName);
		desE.setImportanceTsv(obj.importances);
		
		Date date = new Date();
		Timestamp timestamp2 = new Timestamp(date.getTime());
		desE.setCreatedAt(timestamp2);
		desE.setUpdatedAt(timestamp2);
		
		if (writeToDatabase) {
		DescriptorEmbeddingService deSer = new DescriptorEmbeddingServiceImpl();
		deSer.create(desE);

		}

	}
	
	public static void main(String[] args) {
		EmbeddingWebService ews = new EmbeddingWebService(DevQsarConstants.SERVER_LOCAL, 9092);
		String datasetName = DevQsarConstants.LOG_HALF_LIFE + " OPERA";
		String lanId = "cramslan";
		String descriptorSetName="T.E.S.T. 5.1";
		String splittingName="OPERA";
		Boolean removeLogDescriptors=false;
		int numDesc = 12;
		String embeddingName = "WebTESTEmbeddingHLCOPERA";
		String embeddingDescription = "Ward collinearity filtering/permutative importance";
		
		ews.createEmbedding(datasetName, lanId, descriptorSetName, splittingName, false, numDesc,
				embeddingName, embeddingDescription, false);
	}


}
