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
import gov.epa.web_services.embedding_service.CalculationInfoGA;
import gov.epa.web_services.embedding_service.CalculationInfoImportance;
import gov.epa.web_services.embedding_service.CalculationResponse;
import gov.epa.web_services.embedding_service.EmbeddingWebService2;
import kong.unirest.HttpResponse;

public class CreateEmbeddingsScript {

	void runOperaDatasets() {
		List<String> endpoints = new ArrayList<String>();
		
		String qsarMethod=DevQsarConstants.KNN;
		
//		endpoints.add(DevQsarConstants.LOG_OH);
//		endpoints.add(DevQsarConstants.LOG_KOW);
//		endpoints.add(DevQsarConstants.LOG_BCF);
//		endpoints.add(DevQsarConstants.MELTING_POINT);
//		endpoints.add(DevQsarConstants.LOG_HALF_LIFE);
//		endpoints.add(DevQsarConstants.LOG_KOC);
//		endpoints.add(DevQsarConstants.LOG_KOW);
		endpoints.add(DevQsarConstants.LOG_KOA);
		
		/*
		 * endpoints.add(DevQsarConstants.VAPOR_PRESSURE);
		 * endpoints.add(DevQsarConstants.HENRYS_LAW_CONSTANT);
		 * endpoints.add(DevQsarConstants.LC50);
		 */

		String lanId = "tmarti02";
//		String descriptorSetName = "T.E.S.T. 5.1";
		String descriptorSetName = "WebTEST-default";
		String splittingName = "OPERA";
		
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(DevQsarConstants.SERVER_LOCAL,
				DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

		for (String endpoint : endpoints) {
			String datasetName = endpoint + " OPERA";
			
			CalculationInfo ci=new CalculationInfo();
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=splittingName;
			ci.remove_log_p=endpoint.equals(DevQsarConstants.LOG_KOW);
			ci.qsarMethodEmbedding=qsarMethod;
			
			CalculationInfoGA ciGA=new CalculationInfoGA(ci);
			ews2.generateGA_Embedding(lanId,ciGA);
			
		}
	}

	void runOperaDataset() {

//		String endpoint="LogBCF";
		String endpoint=DevQsarConstants.WATER_SOLUBILITY;
//		String endpoint=DevQsarConstants.MELTING_POINT;
//		String endpoint=DevQsarConstants.HENRYS_LAW_CONSTANT;
//		String endpoint = DevQsarConstants.LOG_KOC;

		String lanId = "tmarti02";
		String descriptorSetName = "WebTEST-default";
		String splittingName = "OPERA";

		Boolean removeLogDescriptors = false;

		EmbeddingWebService2 ews2 = new EmbeddingWebService2(DevQsarConstants.SERVER_LOCAL,
				DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

		String datasetName = endpoint + " OPERA";
		

		String qsarMethod=DevQsarConstants.RF;
//		String qsarMethod = DevQsarConstants.XGB;
		
		boolean remove_log_p=false;
		if (endpoint.equals(DevQsarConstants.LOG_KOW)) remove_log_p=true;
		
		boolean storeResult=true;
		
		CalculationInfo ci=new CalculationInfo();
		ci.datasetName=datasetName;
		ci.descriptorSetName=descriptorSetName;
		ci.splittingName=splittingName;
		ci.remove_log_p=remove_log_p;
		ci.qsarMethodEmbedding=qsarMethod;
		
		CalculationInfoGA ciGA=new CalculationInfoGA(ci);
		ews2.generateGA_Embedding(lanId,ciGA);

	}

	void lookAtEmbeddings() {
		DescriptorEmbeddingServiceImpl descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();

		List<String> datasetNames = new ArrayList<>();

		datasetNames.add("HLC from exp_prop and chemprop");
		datasetNames.add("WS from exp_prop and chemprop");
		datasetNames.add("VP from exp_prop and chemprop");
		datasetNames.add("LogP from exp_prop and chemprop");
		datasetNames.add("MP from exp_prop and chemprop");
		datasetNames.add("BP from exp_prop and chemprop");
//		datasetNames.add("pKa_a from exp_prop and chemprop");
//		datasetNames.add("pKa_b from exp_prop and chemprop");

		String qsarMethodGA = DevQsarConstants.KNN;
		String descriptorSetName = DevQsarConstants.DESCRIPTOR_SET_WEBTEST;

//		String splittingName=SplittingGeneratorPFAS_Script.splittingPFASOnly;
//		String splittingName=SplittingGeneratorPFAS_Script.splittingAllButPFAS;
		String splittingName = DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;

		for (String datasetName : datasetNames) {

			boolean remove_log_p = false;
			if (datasetName.contains("LogP"))
				remove_log_p = true;

			CalculationInfoGA ci = new CalculationInfoGA();
			ci.num_generations = 100;
			if (datasetName.contains("BP") || splittingName.equals("T=all but PFAS, P=PFAS"))
				ci.num_generations = 10;// takes too long to do 100

			ci.remove_log_p = remove_log_p;
			ci.qsarMethodEmbedding = qsarMethodGA;
			ci.datasetName = datasetName;
			ci.descriptorSetName = descriptorSetName;
			ci.splittingName = splittingName;
			ci.num_jobs = 4;

			DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);

			if (descriptorEmbedding == null) {// look for one of the ones made using offline python run:
				ci.num_jobs = 2;// just takes slighter longer
				ci.n_threads = 16;// doesnt impact knn
				descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);
			}

//			System.out.println(datasetName+"\t"+descriptorEmbedding.getEmbeddingTsv());
			System.out.println(datasetName + "\t" + descriptorEmbedding.getEmbeddingTsv().split("\t").length);

		}

	}

	void runDatasetsExpProp() {
		
		List<String>datasetNames=new ArrayList<>();

//		String qsarMethod=DevQsarConstants.RF;
		String qsarMethod = DevQsarConstants.XGB;
		
//		datasetNames.add("HLC v1");
//		datasetNames.add("VP v1");
//		datasetNames.add("BP v1");
//		datasetNames.add("LogP v1");
//		datasetNames.add("MP v1");
//		datasetNames.add("WS v1 res_qsar");
		datasetNames.add("HLC v1 res_qsar");

		String lanId = "tmarti02";
		String descriptorSetName = "WebTEST-default";
		String splittingName = DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;

		
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(DevQsarConstants.SERVER_LOCAL,
				DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

		boolean checkDBForEmbedding=false;
		boolean storeInDB=false;
		
		
		for (String datasetName:datasetNames) {
			
			boolean remove_log_p = false;
			if(datasetName.contains("LogP")) remove_log_p=true;
			
			CalculationInfo ci=new CalculationInfo();
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=splittingName;
			ci.remove_log_p=remove_log_p;
			ci.qsarMethodEmbedding=qsarMethod;
			
			CalculationInfoImportance ciImportance=new CalculationInfoImportance(ci);
			ews2.generateImportanceEmbedding(ciImportance,lanId,checkDBForEmbedding,storeInDB);
		}
		
	}
	
	
	public static void main(String[] args) {
		CreateEmbeddingsScript c = new CreateEmbeddingsScript();
//		c.runEmbeddings();
//		c.runEmbedding();
//		c.lookAtEmbeddings();
		c.runDatasetsExpProp();
	}

	



	public static ModelData retrieveModelData(String datasetName, String descriptorSetName, String splittingName,
			Boolean removeLogDescriptors, String lanId) {
		WebServiceModelBuilder wsmb = new WebServiceModelBuilder(null, lanId);
		ModelData data = ModelData.initModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors,
				false);
		return data;
	}

}
