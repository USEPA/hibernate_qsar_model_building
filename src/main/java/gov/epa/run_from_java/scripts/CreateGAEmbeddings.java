package gov.epa.run_from_java.scripts;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingService;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingServiceImpl;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.models.WebServiceModelBuilder;
import gov.epa.web_services.embedding_service.CalculationInfo;
import gov.epa.web_services.embedding_service.CalculationResponse;
import gov.epa.web_services.embedding_service.EmbeddingWebService2;
import kong.unirest.HttpResponse;

public class CreateGAEmbeddings {

	public static void main(String[] args) {
		List<String> endpoints = new ArrayList<String>();
//		endpoints.add(DevQsarConstants.LOG_OH);
		endpoints.add(DevQsarConstants.LOG_KOW);
		endpoints.add(DevQsarConstants.LOG_BCF);
		
		endpoints.add(DevQsarConstants.MELTING_POINT);
		endpoints.add(DevQsarConstants.LOG_HALF_LIFE);
		endpoints.add(DevQsarConstants.LOG_KOC);
		endpoints.add(DevQsarConstants.LOG_KOW);
		/*
		endpoints.add(DevQsarConstants.VAPOR_PRESSURE);
		endpoints.add(DevQsarConstants.HENRYS_LAW_CONSTANT);
		endpoints.add(DevQsarConstants.LC50);
		*/
		
		String propertyName = DevQsarConstants.HENRYS_LAW_CONSTANT;
		String lanId = "cramslan";
		String descriptorSetName="T.E.S.T. 5.1";
		String splittingName="OPERA";
		Boolean removeLogDescriptors=false;	
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);		

		
		for (String endpoint:endpoints) {
		String datasetName = endpoint + " OPERA";
		runEmbeddings(endpoint, datasetName, lanId, descriptorSetName, splittingName, removeLogDescriptors, ews2);
		}
	}
	
	private static void runEmbeddings(String propertyName, String datasetName, String lanId, String descriptorSetName,
			String splittingName, Boolean removeLogDescriptors, EmbeddingWebService2 ews2) {
		String tsv = null;
		try {
			tsv = retrieveTrainingData(datasetName, descriptorSetName, splittingName, removeLogDescriptors, lanId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String name = propertyName + "_" + descriptorSetName + "_" + System.currentTimeMillis();
		
		CalculationInfo ci = new CalculationInfo();
		ci.tsv_training = tsv;
		ci.remove_log_p = propertyName.equals(DevQsarConstants.LOG_KOW);
		ci.qsarMethodGA = DevQsarConstants.KNN;
		ci.num_generations = 10;
		
		HttpResponse<String> response = ews2.findEmbedding(ci);
		System.out.println(response.getStatus());
		
		String data = response.getBody();
		System.out.println(data);
		
		Gson gson = new Gson();
		CalculationResponse cr = gson.fromJson(data, CalculationResponse.class);
		String embedding = cr.embedding.stream().map(Object::toString).collect(Collectors.joining("\t"));
		
		DescriptorEmbedding desE = new DescriptorEmbedding();
		desE.setDatasetName(datasetName);
		desE.setCreatedBy(lanId);
		desE.setDescription(ci.toString());
		
		desE.setDescriptorSetName(descriptorSetName);
		desE.setEmbeddingTsv(embedding);
		desE.setQsarMethod(ci.qsarMethodGA);
		desE.setName(name);
		desE.setDatasetName(datasetName);
		desE.setImportanceTsv("not null importances");
		
		Date date = new Date();
		Timestamp timestamp2 = new Timestamp(date.getTime());
		desE.setCreatedAt(timestamp2);
		desE.setUpdatedAt(timestamp2);

		
		if (true) {
		DescriptorEmbeddingService deSer = new DescriptorEmbeddingServiceImpl();
		deSer.create(desE);
		}


	}
	
	public static String retrieveTrainingData(String datasetName, String descriptorSetName, 
			String splittingName, Boolean removeLogDescriptors, String lanId) {
		WebServiceModelBuilder wsmb = new WebServiceModelBuilder(null, lanId);
		ModelData data = ModelData.initModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors,false);
		return data.trainingSetInstances;
	}


}
