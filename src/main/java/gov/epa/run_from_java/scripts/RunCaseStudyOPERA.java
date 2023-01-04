package gov.epa.run_from_java.scripts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingService;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingServiceImpl;
import gov.epa.web_services.embedding_service.CalculationInfo;
import gov.epa.web_services.embedding_service.EmbeddingWebService2;

public class RunCaseStudyOPERA {

	
	public static void main(String[] args) {
		String lanId="cramslan";
		DescriptorEmbeddingService descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();
				
		//*****************************************************************************************
		// Build model:		
//		String modelWsServer="10.140.73.169";
		String modelWsServer=DevQsarConstants.SERVER_LOCAL;
//		String modelWsServer=DevQsarConstants.SERVER_819;

		int modelWsPort=DevQsarConstants.PORT_PYTHON_MODEL_BUILDING;
		
		String sampleSource="OPERA";
//		String sampleSource="TEST";
		
//		String endpoint=DevQsarConstants.LOG_OH;
//		String endpoint=DevQsarConstants.LOG_KOW;
//		String endpoint=DevQsarConstants.LOG_BCF;
//		String endpoint=DevQsarConstants.MELTING_POINT;
		String endpoint=DevQsarConstants.LOG_HALF_LIFE;
//		String endpoint=DevQsarConstants.LOG_KOC;
//		String endpoint=DevQsarConstants.MUTAGENICITY;
//		String endpoint=DevQsarConstants.LOG_KOW;
//		String endpoint=DevQsarConstants.LLNA;
//		String endpoint=DevQsarConstants.DEV_TOX;
//		String endpoint=DevQsarConstants.VAPOR_PRESSURE;
//		String endpoint=DevQsarConstants.HENRYS_LAW_CONSTANT;
//		String endpoint=DevQsarConstants.LC50;
//		String endpoint=DevQsarConstants.MUTAGENICITY;
//		String endpoint=DevQsarConstants.LOG_KOW;

		String datasetName = endpoint +" "+sampleSource;
		String splittingName=sampleSource;
		
//		String datasetName = "Standard Henry's law constant from exp_prop";
//		String splittingName="RND_REPRESENTATIVE";
		String descriptorSetName = "T.E.S.T. 5.1";

		boolean removeLogDescriptors=endpoint.equals(DevQsarConstants.LOG_KOW);
		String qsar_method = DevQsarConstants.KNN;
		
//		String embeddingDescription = "num_generations=10 num_optimizers=10 num_jobs=4 n_threads=20 max_length=24 descriptor_coefficient=0.002";
		Gson gson2 = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		CalculationInfo ci = new CalculationInfo();
		ci.num_generations = 2;
		String embeddingDescription = gson2.toJson(ci);
		
		DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(qsar_method, datasetName, descriptorSetName, embeddingDescription);
		if (descriptorEmbedding == null) {
			EmbeddingWebService2 ews2 = new EmbeddingWebService2(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);
			ews2.main2(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING,
					endpoint, lanId, descriptorSetName, splittingName,
					removeLogDescriptors, ci.num_generations);
			descriptorEmbedding = descriptorEmbeddingService.findByGASettings(qsar_method, datasetName, descriptorSetName, embeddingDescription);
		}
		
		// 	"PaDEL-default", "RDKit-default", "WebTEST-default", "ToxPrints-default",

		String[] sciDataExpertsDescriptorSetNames = {
				"T.E.S.T. 5.1"
				};
		
		System.out.println("is descriptorembedding null?" + descriptorEmbedding.getName());
		String methods[]= {DevQsarConstants.RF, DevQsarConstants.XGB, DevQsarConstants.SVM};
		for (String desc:sciDataExpertsDescriptorSetNames) {
			for (String method:methods) {
				System.out.println(method + "descriptor" + desc);
				ModelBuildingScript.buildModel(modelWsServer,modelWsPort,datasetName,descriptorSetName,
						splittingName, removeLogDescriptors,method,lanId, descriptorEmbedding.getName(), false);

			}

		}

		
		//*****************************************************************************************
//		String methodName=DevQsarConstants.SVM;
//		String methodName=DevQsarConstants.DNN;
		String methodName=DevQsarConstants.RF;
//		String methodName=DevQsarConstants.XGB;
		
//		String descriptorEmbeddingName="TestEmbeddingHLCexpPrp";
//		String methodName=DevQsarConstants.XGB;
//
		
//		String datasetName="LLNA from exp_prop, without eChemPortal";
//		String descriptorEmbeddingName="TestEmbeddingHLCexpPrp";
//		EmbeddingWebService2 ews2 = new EmbeddingWebService2(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);
		
		
	/*	

	*/
//
//		run.buildModel("http://localhost","8080", modelWsServer,modelWsPort,datasetName,descriptorSetName,
//				splittingName, removeLogDescriptors,methodName,lanId);


		//*****************************************************************************************
//		d.testInit(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING, methodName, 104L);
	}

}
	
	


