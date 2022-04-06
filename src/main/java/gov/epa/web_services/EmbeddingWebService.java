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
	
	public HttpResponse<String> findEmbedding(String tsv, int numDesc) {
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

	
	public void gatherdata(EmbeddingWebService ews) {
//		String datasetName = endpoint+" OPERA";
		String datasetName = "LLNA from exp_prop, without eChemPortal";
		String lanId = "cramslan";
		String descriptorSetName="T.E.S.T. 5.1";
		String splittingName="RND_REPRESENTATIVE";
		String modelSetName="";
		Boolean removeLogDescriptors=false;
		int numDesc = 12;
		
		WebServiceModelBuilder wsmb = new WebServiceModelBuilder(null, "cramslan");
		ModelData data = wsmb.initModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors);
				
		try {
			FileUtils.writeStringToFile(new File("LLNA.tsv"), data.trainingSetInstances);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HttpResponse<String> doesthiswork = ews.findEmbedding(data.trainingSetInstances, numDesc);
		
		EmbeddingCalculationResponse obj = gson.fromJson(doesthiswork.getBody(), EmbeddingCalculationResponse.class);

		System.out.println(obj.embedding);
		System.out.println(obj.importances);
		
		
		String embeddingDescription = "Ward collinearity filtering/permutative importance";
		DescriptorEmbedding desE = new DescriptorEmbedding();
		
		String embeddingName = "TestEmbeddingLLNAexpPrpWOeCP" + Integer.valueOf(numDesc).toString();
		
		desE.setDatasetName(datasetName);
		desE.setCreatedBy(lanId);
		desE.setDescription(embeddingDescription);
		desE.setDescriptorSetName(descriptorSetName);
		desE.setEmbeddingTsv(obj.embedding);
		desE.setName(embeddingName);
		desE.setDatasetName(datasetName);
		desE.setImportanceTsv(obj.importances);
		
		Date date = new Date();
		Timestamp timestamp2 = new Timestamp(date.getTime());


		
		desE.setCreatedAt(timestamp2);
		desE.setUpdatedAt(timestamp2);
		
		
		DescriptorEmbedding desE2 = new DescriptorEmbedding();
		
		DescriptorEmbeddingService deSer = new DescriptorEmbeddingServiceImpl();
		deSer.create(desE);
		
		
	}
	
	public static void main(String[] args) {
		EmbeddingWebService ews = new EmbeddingWebService(DevQsarConstants.SERVER_LOCAL, 9092);
		ews.gatherdata(ews);
	}


}
