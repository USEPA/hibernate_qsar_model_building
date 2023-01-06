package gov.epa.run_from_java.scripts;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingService;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.web_services.embedding_service.CalculationInfo;
import gov.epa.web_services.embedding_service.EmbeddingWebService2;

public class RunCaseStudies {

//	static String lanId="cramslan";
	static String lanId = "tmarti02";
	
	static int portModelBuilding=DevQsarConstants.PORT_PYTHON_MODEL_BUILDING;

	static String serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
//	static String serverModelBuilding=DevQsarConstants.SERVER_819;
//	static String serverModelBuilding="10.140.73.169";
	
	static String qsarMethodGA = DevQsarConstants.KNN;

	static String descriptorSetName = "T.E.S.T. 5.1";
	// 	"PaDEL-default", "RDKit-default", "WebTEST-default", "ToxPrints-default",

	
	public static void runCaseStudyOPERA() {
		
		DescriptorEmbeddingService descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

		String sampleSource="OPERA";
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
		boolean removeLogDescriptors=endpoint.equals(DevQsarConstants.LOG_KOW);
		
		CalculationInfo ci = new CalculationInfo();
		ci.num_generations = 1;
		ci.remove_log_p = removeLogDescriptors;
		ci.qsarMethodGA = qsarMethodGA;
		ci.datasetName=datasetName;
		ci.descriptorSetName=descriptorSetName;
		ci.splittingName=sampleSource;
					
		DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);
		
//		if(descriptorEmbedding!=null) {
//			descriptorEmbeddingService.delete(descriptorEmbedding);//start fresh
//			descriptorEmbedding=null;			
//			System.out.println("embedding deleted!");
//		}
		
		if (descriptorEmbedding == null) {
			descriptorEmbedding = ews2.generateEmbedding(serverModelBuilding, portModelBuilding, lanId,ci);
			System.out.println("New embedding from web service:"+descriptorEmbedding.getEmbeddingTsv());
		} else {
			System.out.println("Have embedding from db:"+descriptorEmbedding.getEmbeddingTsv());
		}
		
//		if(true) return;

		String methods[]= {DevQsarConstants.KNN, DevQsarConstants.RF, DevQsarConstants.XGB, DevQsarConstants.SVM};

		for (String method:methods) {
			System.out.println(method + "descriptor" + descriptorSetName);
			ModelBuildingScript.buildModel(lanId,serverModelBuilding,portModelBuilding,method,descriptorEmbedding,ci);
		}
		
		buildConsensusModelForEmbeddedModels(descriptorEmbedding, datasetName);

	}
	private static void buildConsensusModelForEmbeddedModels(DescriptorEmbedding descriptorEmbedding,String datasetName) {
		ModelServiceImpl modelService=new ModelServiceImpl();
		List<Model>models=modelService.findByDatasetName(datasetName);
		
		Set<Long> consensusModelIDs = new HashSet<Long>();
		
		for (Model model:models) {
			if (model.getDescriptorEmbedding()==null) continue;			
			if (!model.getDescriptorEmbedding().getId().equals(descriptorEmbedding.getId())) continue;
			System.out.println(model.getId());
			consensusModelIDs.add(model.getId());
		}
		ModelBuildingScript.buildUnweightedConsensusModel(consensusModelIDs, lanId);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		runCaseStudyOPERA();
		
	}

}