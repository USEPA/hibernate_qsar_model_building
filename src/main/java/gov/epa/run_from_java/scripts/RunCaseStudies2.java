package gov.epa.run_from_java.scripts;

import java.util.ArrayList;
import java.util.List;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingServiceImpl;
import gov.epa.web_services.embedding_service.CalculationInfo;
import gov.epa.web_services.embedding_service.EmbeddingWebService2;

public class RunCaseStudies2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RunCaseStudies rcs = new RunCaseStudies();
		runCaseStudyWithoutLookChem(rcs);
		
	}
	
	public static void runCaseStudyWithoutLookChem(RunCaseStudies rcs) {
		rcs.lanId="cramslan";		
		boolean buildModels=true;
		
		rcs.serverModelBuilding=DevQsarConstants.SERVER_819;
//		serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
		rcs.portModelBuilding=5004;
		
		DescriptorEmbeddingServiceImpl descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(rcs.serverModelBuilding, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

		List<String>datasetNames=new ArrayList<>();

//		datasetNames.add("LogP v1");
		datasetNames.add("WS v1");
//		datasetNames.add("HLC v1");
//		datasetNames.add("MP v1");
//		datasetNames.add("BP v1");
//		datasetNames.add("VP v1");
		
//		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
		String splitting ="T=PFAS only, P=PFAS";
//		String splitting = "T=all but PFAS, P=PFAS";

		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		for (String datasetName:datasetNames) {
						
			boolean remove_log_p = false;
			if(datasetName.contains("LogP")) remove_log_p=true;
			
			CalculationInfo ci = new CalculationInfo();
			ci.num_generations = 40;			
			if (datasetName.contains("BP") || splitting.equals("T=all but PFAS, P=PFAS")) ;//takes too long to do 100			

			ci.remove_log_p = remove_log_p;
			ci.qsarMethodGA = rcs.qsarMethodGA;
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=splitting;
//			ci.num_jobs=4;
			
					
			System.out.println("\n***"+datasetName+"\t"+splitting+"\t"+"num_generations="+ci.num_generations+"***");
			
			DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);
			
			if (descriptorEmbedding==null) {//look for one of the ones made using offline python run:			
				ci.num_jobs=2;//just takes slighter longer
				ci.n_threads=16;//doesnt impact knn
				descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);				
			}			

			if (descriptorEmbedding == null) {
				descriptorEmbedding = ews2.generateEmbedding(rcs.serverModelBuilding, rcs.portModelBuilding, rcs.lanId,ci);
//				System.out.println("New embedding from web service:"+descriptorEmbedding.getEmbeddingTsv());
//				System.out.println("Dont have existing embedding:"+ci.toString());
				continue;
			} else {
				System.out.println("Have embedding from db:"+descriptorEmbedding.getEmbeddingTsv());
			}

			if (!buildModels) continue;

//			String methods[]= {DevQsarConstants.KNN, DevQsarConstants.RF, DevQsarConstants.XGB, DevQsarConstants.SVM};
//			String methods[]= {DevQsarConstants.SVM};
//			String methods[]= {DevQsarConstants.KNN};
			String methods[] = {DevQsarConstants.REG};
//			String methods[]= {DevQsarConstants.RF, DevQsarConstants.XGB, DevQsarConstants.SVM};
//			String methods[]= {DevQsarConstants.XGB, DevQsarConstants.SVM};
//			String methods[]= {DevQsarConstants.KNN, DevQsarConstants.RF};

			for (String method:methods) {
				System.out.println(method + "descriptor" + descriptorSetName);
				ModelBuildingScript.buildModel(rcs.lanId,rcs.serverModelBuilding,rcs.portModelBuilding,method,descriptorEmbedding,ci);
			}
//			buildConsensusModelForEmbeddedModels(descriptorEmbedding, datasetName,methods.length);
		}

	}


}
