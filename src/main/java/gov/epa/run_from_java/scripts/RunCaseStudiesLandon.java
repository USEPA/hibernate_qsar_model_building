package gov.epa.run_from_java.scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInModelSetService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInModelSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetServiceImpl;
import gov.epa.web_services.ModelWebService;
import gov.epa.web_services.embedding_service.CalculationInfoGA;

/**
* @author TMARTI02
*/
public class RunCaseStudiesLandon {

	static ModelServiceImpl modelService=new ModelServiceImpl();
	static ModelInModelSetService mimss=new ModelInModelSetServiceImpl();	
	static ModelSetServiceImpl mss=new ModelSetServiceImpl();
	
	static String lanId = "tmarti02";
	
	static int portModelBuilding=DevQsarConstants.PORT_PYTHON_MODEL_BUILDING;

	static String serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
//	static String serverModelBuilding=DevQsarConstants.SERVER_819;
//	static String serverModelBuilding="10.140.73.169";
	
	static String qsarMethodEmbedding = DevQsarConstants.KNN;

//	static String descriptorSetName = DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
	//  ""T.E.S.T. 5.1",	"PaDEL-default", "RDKit-default", "WebTEST-default", "ToxPrints-default",

	static String[] methodsConsensus = { DevQsarConstants.KNN, DevQsarConstants.RF, DevQsarConstants.XGB,
			DevQsarConstants.SVM };
	
	static String[] methodsConsensusNoKNN = { DevQsarConstants.RF, DevQsarConstants.XGB,DevQsarConstants.SVM };

	static String[] methodsConsensusRF_XGB = { DevQsarConstants.RF, DevQsarConstants.XGB};

	
	public static void compare_Acute_Aquatic_Tox_Versions_No_Embedding() {
	
		boolean use_pmml=false;
		boolean include_standardization_in_pmml=true;
		boolean use_sklearn2pmml=false;

		lanId="tmarti02";		
		ModelWebService.num_jobs=8;

		serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
		portModelBuilding=5004;
		System.out.println("\n*** portNumber="+portModelBuilding+" ***");
		
		
		List<String>datasetNames=new ArrayList<>();
		List<String>speciesAbbrevs=Arrays.asList("FHM","BG","RT");
		
		for (String speciesAbbrev:speciesAbbrevs) {
			for (int version=1;version<=5;version++) {
				datasetNames.add("exp_prop_96HR_"+speciesAbbrev+"_LC50_v"+version+" modeling");//create dataset names programmatically
			}
		}
		
//		if(true)return;
			
		String descriptorSetName="Mordred-default";
		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;	
//		String method="rf";
		String method="las";
		
		for (String datasetName:datasetNames) {
			List<Long>modelIds=new ArrayList<>();
			
			boolean remove_log_p = false;
			if(datasetName.contains("LogP")) remove_log_p=true;
						
			CalculationInfoGA ci=new CalculationInfoGA();
			ci.remove_log_p = remove_log_p;
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=splitting;
					
			System.out.println("\n***"+datasetName+"\t"+method + "\t" + descriptorSetName+"\t"+splitting+"***");

			long modelId=ModelBuildingScript.buildModel(lanId,serverModelBuilding,portModelBuilding,method,null, ci,use_pmml, include_standardization_in_pmml,use_sklearn2pmml);
			modelIds.add(modelId);
			
		}

	}
	
	public static void compare_Qsar_Methods_No_Embedding() {
		
		boolean use_pmml=false;
		boolean include_standardization_in_pmml=true;
		boolean use_sklearn2pmml=false;

		boolean buildIndividualModels=true;
		boolean buildConsensusModels=false;

		lanId="tmarti02";		
		ModelWebService.num_jobs=8;

		serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
		portModelBuilding=5004;
		
//		serverModelBuilding=DevQsarConstants.SERVER_819;
//		portModelBuilding=5014;

		List<String>datasetNames=new ArrayList<>();

		List<String>speciesAbbrevs=Arrays.asList("FHM","BG","RT");
		
		String version="v5";
		
		for (String speciesAbbrev:speciesAbbrevs) {
			datasetNames.add("exp_prop_96HR_"+speciesAbbrev+"_LC50_"+version+" modeling");
		}
		
		
//		if(true)return;

		List<String>methods=new ArrayList<>();			
		methods.add(DevQsarConstants.RF);
		methods.add(DevQsarConstants.XGB);
		methods.add(DevQsarConstants.KNN);
		methods.add(DevQsarConstants.SVM);
//		methods.add(DevQsarConstants.LAS);//Not sure if working if called from Java?
		
		List<String>descriptorSets=new ArrayList<>();
		descriptorSets.add("WebTEST-default");
		descriptorSets.add("PaDEL-default");
		descriptorSets.add("ToxPrints-default");
		descriptorSets.add("Mordred-default");
		
		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;	
		boolean remove_log_p = false;

		System.out.println("\n*** portNumber="+portModelBuilding+" ***");

		for (String datasetName:datasetNames) {
			
			for (String descriptorSetName:descriptorSets) {

				List<Long>modelIds=new ArrayList<>();
				
				CalculationInfoGA ci=new CalculationInfoGA();
				ci.remove_log_p = remove_log_p;
				ci.datasetName=datasetName;
				ci.descriptorSetName=descriptorSetName;
				ci.splittingName=splitting;
						
				System.out.println("\n***"+datasetName+"\t"+splitting+"***");

				if (buildIndividualModels) {

					for (String method:methods) {
						System.out.println(method + "\t" + descriptorSetName);
						long modelId=ModelBuildingScript.buildModel(lanId,serverModelBuilding,portModelBuilding,method,null, ci,use_pmml, include_standardization_in_pmml,use_sklearn2pmml);
						modelIds.add(modelId);
					}
				}
				
				if (buildConsensusModels) {
					modelIds=RunCaseStudiesTodd.buildConsensusModel2(datasetName,splitting,descriptorSetName,methodsConsensusRF_XGB);
				} 
//				assignModelSetNoEmbedding(splitting, modelIds);
			}
		}

	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		compare_Acute_Aquatic_Tox_Versions_No_Embedding();
		compare_Qsar_Methods_No_Embedding();
	}

}
