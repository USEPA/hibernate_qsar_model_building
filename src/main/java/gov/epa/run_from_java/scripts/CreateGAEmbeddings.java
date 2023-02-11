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

	void runEmbeddings() {
		List<String> endpoints = new ArrayList<String>();
//		endpoints.add(DevQsarConstants.LOG_OH);
		endpoints.add(DevQsarConstants.LOG_KOW);
		endpoints.add(DevQsarConstants.LOG_BCF);

		endpoints.add(DevQsarConstants.MELTING_POINT);
		endpoints.add(DevQsarConstants.LOG_HALF_LIFE);
		endpoints.add(DevQsarConstants.LOG_KOC);
		endpoints.add(DevQsarConstants.LOG_KOW);
		/*
		 * endpoints.add(DevQsarConstants.VAPOR_PRESSURE);
		 * endpoints.add(DevQsarConstants.HENRYS_LAW_CONSTANT);
		 * endpoints.add(DevQsarConstants.LC50);
		 */

		String propertyName = DevQsarConstants.HENRYS_LAW_CONSTANT;
		String lanId = "cramslan";
		String descriptorSetName = "T.E.S.T. 5.1";
		String splittingName = "OPERA";
		Boolean removeLogDescriptors = false;
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(DevQsarConstants.SERVER_LOCAL,
				DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

		for (String endpoint : endpoints) {
			String datasetName = endpoint + " OPERA";
			runEmbeddings(endpoint, datasetName, lanId, descriptorSetName, splittingName, removeLogDescriptors, ews2);
		}
	}

	void lookAtEmbeddings() {
		DescriptorEmbeddingServiceImpl descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();

		List<String>datasetNames=new ArrayList<>();

		datasetNames.add("HLC from exp_prop and chemprop");
		datasetNames.add("WS from exp_prop and chemprop");
		datasetNames.add("VP from exp_prop and chemprop");
		datasetNames.add("LogP from exp_prop and chemprop");
		datasetNames.add("MP from exp_prop and chemprop");
		datasetNames.add("BP from exp_prop and chemprop");
//		datasetNames.add("pKa_a from exp_prop and chemprop");
//		datasetNames.add("pKa_b from exp_prop and chemprop");
		
		String qsarMethodGA = DevQsarConstants.KNN;
		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
//		String splittingName=SplittingGeneratorPFAS_Script.splittingPFASOnly;
//		String splittingName=SplittingGeneratorPFAS_Script.splittingAllButPFAS;
		String splittingName=DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;

		
		for (String datasetName:datasetNames) {
			
			boolean remove_log_p = false;
			if(datasetName.contains("LogP")) remove_log_p=true;


			CalculationInfo ci = new CalculationInfo();
			ci.num_generations = 100;			
			if (datasetName.contains("BP") || splittingName.equals("T=all but PFAS, P=PFAS")) ci.num_generations=10;//takes too long to do 100			

			ci.remove_log_p = remove_log_p;
			ci.qsarMethodGA = qsarMethodGA;
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=splittingName;
			ci.num_jobs=4;

			DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);

			if (descriptorEmbedding==null) {//look for one of the ones made using offline python run:			
				ci.num_jobs=2;//just takes slighter longer
				ci.n_threads=16;//doesnt impact knn
				descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);				
			}
			
//			System.out.println(datasetName+"\t"+descriptorEmbedding.getEmbeddingTsv());
			System.out.println(datasetName+"\t"+descriptorEmbedding.getEmbeddingTsv().split("\t").length);
			
		}
		
	}
	
	
	
	
	public static void main(String[] args) {
		CreateGAEmbeddings c=new CreateGAEmbeddings();
//		c.runEmbeddings();
		c.lookAtEmbeddings();
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
		ci.datasetName = datasetName;
		ci.descriptorSetName = descriptorSetName;

		HttpResponse<String> response = ews2.findEmbedding(ci);
		System.out.println(response.getStatus());

		String data = response.getBody();
		System.out.println(data);

		Gson gson = new Gson();
		CalculationResponse cr = gson.fromJson(data, CalculationResponse.class);
		String embedding = cr.embedding.stream().map(Object::toString).collect(Collectors.joining("\t"));

		DescriptorEmbedding desE = new DescriptorEmbedding(ci, lanId, embedding);

		Date date = new Date();
		Timestamp timestamp2 = new Timestamp(date.getTime());
		desE.setCreatedAt(timestamp2);
		desE.setUpdatedAt(timestamp2);

		if (true) {
			DescriptorEmbeddingService deSer = new DescriptorEmbeddingServiceImpl();
			deSer.create(desE);
		}

	}

	public static String retrieveTrainingData(String datasetName, String descriptorSetName, String splittingName,
			Boolean removeLogDescriptors, String lanId) {
		WebServiceModelBuilder wsmb = new WebServiceModelBuilder(null, lanId);
		ModelData data = ModelData.initModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors,
				false);
		return data.trainingSetInstances;
	}

}
