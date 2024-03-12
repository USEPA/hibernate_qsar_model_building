package gov.epa.run_from_java.scripts;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInModelSet;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelFile;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSet;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingService;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInModelSetService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInModelSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelFileServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionServiceImpl;
import gov.epa.endpoints.models.ModelBuilder;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.models.ModelStatisticCalculator;
import gov.epa.endpoints.models.WebServiceModelBuilder;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.ModelSetScript.ModelSet2;
import gov.epa.web_services.ModelWebService;
import gov.epa.web_services.embedding_service.CalculationInfo;
import gov.epa.web_services.embedding_service.CalculationInfoGA;
import gov.epa.web_services.embedding_service.CalculationInfoImportance;
import gov.epa.web_services.embedding_service.EmbeddingWebService2;

public class RunCaseStudiesTodd {

	static ModelServiceImpl modelService=new ModelServiceImpl();
	static ModelInModelSetService mimss=new ModelInModelSetServiceImpl();	
	static ModelSetServiceImpl mss=new ModelSetServiceImpl();
	
	static String lanId = "tmarti02";
	
	static int portModelBuilding=DevQsarConstants.PORT_PYTHON_MODEL_BUILDING;

	static String serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
//	static String serverModelBuilding=DevQsarConstants.SERVER_819;
//	static String serverModelBuilding="10.140.73.169";
	
	static String qsarMethodEmbedding = DevQsarConstants.KNN;

	static String descriptorSetName = DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
	//  ""T.E.S.T. 5.1",	"PaDEL-default", "RDKit-default", "WebTEST-default", "ToxPrints-default",

	static String[] methodsConsensus = { DevQsarConstants.KNN, DevQsarConstants.RF, DevQsarConstants.XGB,
			DevQsarConstants.SVM };
	
	static String[] methodsConsensusNoKNN = { DevQsarConstants.RF, DevQsarConstants.XGB,DevQsarConstants.SVM };

	static String[] methodsConsensusRF_XGB = { DevQsarConstants.RF, DevQsarConstants.XGB};

	
	public static void runCaseStudyOPERA() {
		boolean use_pmml=false;
		boolean include_standardization_in_pmml=true;
		boolean use_sklearn2pmml=false;
		
		
		DescriptorEmbeddingService descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

		String sampleSource="OPERA";
		String endpoint=DevQsarConstants.LOG_HALF_LIFE;
		
//		String endpoint=DevQsarConstants.LOG_OH;
//		String endpoint=DevQsarConstants.LOG_KOW;
//		String endpoint=DevQsarConstants.LOG_BCF;
//		String endpoint=DevQsarConstants.MELTING_POINT;
//		String endpoint=DevQsarConstants.LOG_HALF_LIFE;
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
		
		CalculationInfoGA ci = new CalculationInfoGA();
		ci.num_generations = 100;
		ci.remove_log_p = removeLogDescriptors;
		ci.qsarMethodEmbedding = qsarMethodEmbedding;
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
			descriptorEmbedding = ews2.generateGA_Embedding(lanId,ci);
			System.out.println("New embedding from web service:"+descriptorEmbedding.getEmbeddingTsv());
		} else {
			System.out.println("Have embedding from db:"+descriptorEmbedding.getEmbeddingTsv());
		}
		
		if(true) return;//skip model building

		String methods[]= {DevQsarConstants.KNN, DevQsarConstants.RF, DevQsarConstants.XGB, DevQsarConstants.SVM};

		for (String method:methods) {
			System.out.println(method + "descriptor" + descriptorSetName);
			ModelBuildingScript.buildModel(lanId,serverModelBuilding,portModelBuilding,method,descriptorEmbedding,ci, use_pmml, include_standardization_in_pmml,use_sklearn2pmml);
		}
		
		buildConsensusModelForEmbeddedModels(descriptorEmbedding, datasetName, methodsConsensus);

	}
	
	
	public static void runCaseStudyTest_All_Endpoints() {
		boolean use_pmml=false;
		boolean include_standardization_in_pmml=true;
		boolean use_sklearn2pmml=false;
		
		serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
		portModelBuilding=5004;

		
		DescriptorEmbeddingService descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(serverModelBuilding, portModelBuilding);

		String sampleSource="TEST";
		
		/*
		*/
//		String [] endpoints= {DevQsarConstants.MUTAGENICITY, DevQsarConstants.LD50,
//				DevQsarConstants.LC50DM, DevQsarConstants.DEV_TOX, DevQsarConstants.LLNA,
//				DevQsarConstants.LC50, DevQsarConstants.IGC50};

//		String [] endpoints= {DevQsarConstants.LC50DM};
		String [] endpoints= {DevQsarConstants.LLNA};
		

		for (String endpoint:endpoints) {
			System.out.println(endpoint);

			String datasetName = endpoint +" "+sampleSource;
			boolean removeLogDescriptors=endpoint.equals(DevQsarConstants.LOG_KOW);

			CalculationInfoGA ci = new CalculationInfoGA();
			ci.num_generations = 1;
			ci.remove_log_p = removeLogDescriptors;
			ci.qsarMethodEmbedding = qsarMethodEmbedding;
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=sampleSource;

			DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);

			if (descriptorEmbedding == null) {
				descriptorEmbedding = ews2.generateGA_Embedding(lanId,ci);
				System.out.println("New embedding from web service:"+descriptorEmbedding.getEmbeddingTsv());
			} else {
				System.out.println("Have embedding from db:"+descriptorEmbedding.getEmbeddingTsv());
			}

			if (true) continue;//skip model building

			String methods[]= {DevQsarConstants.KNN, DevQsarConstants.RF, DevQsarConstants.XGB, DevQsarConstants.SVM};

			for (String method:methods) {
				System.out.println(method + "descriptor" + descriptorSetName);
				ModelBuildingScript.buildModel(lanId,serverModelBuilding,portModelBuilding,method,descriptorEmbedding,ci,use_pmml, include_standardization_in_pmml,use_sklearn2pmml);
			}

			buildConsensusModelForEmbeddedModels(descriptorEmbedding, datasetName,methodsConsensus);

		}

	}
	
	public static void runCaseStudyTest() {
		lanId="tmarti02";
		boolean use_pmml=false;
		boolean include_standardization_in_pmml=true;
		boolean use_sklearn2pmml=false;
		
		boolean buildModel=true;
		
		serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
		
		DescriptorEmbeddingService descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(serverModelBuilding, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

		String sampleSource="TEST";
		String endpoint=DevQsarConstants.MUTAGENICITY;
		String method=DevQsarConstants.SVM;

		String datasetName = endpoint +" "+sampleSource;
		boolean removeLogDescriptors=endpoint.equals(DevQsarConstants.LOG_KOW);

		CalculationInfoGA ci = new CalculationInfoGA();
		ci.num_generations = 100;
		ci.remove_log_p = removeLogDescriptors;
		ci.qsarMethodEmbedding = qsarMethodEmbedding;
		ci.datasetName=datasetName;
		ci.descriptorSetName=descriptorSetName;
		ci.splittingName=sampleSource;

		DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);

		if (descriptorEmbedding == null) {
			descriptorEmbedding = ews2.generateGA_Embedding(lanId,ci);
			System.out.println("New embedding from web service:"+descriptorEmbedding.getEmbeddingTsv());
		} else {
			System.out.println("Have embedding from db:"+descriptorEmbedding.getEmbeddingTsv());
		}

		if (!buildModel) return;//skip model building

		System.out.println(method + "descriptor" + descriptorSetName);
		ModelBuildingScript.buildModel(lanId,serverModelBuilding,portModelBuilding,method,descriptorEmbedding,ci,use_pmml, include_standardization_in_pmml,use_sklearn2pmml);


	}
	
	

	public static void runCaseStudyExpProp_All_Endpoints() {
		boolean use_pmml=false;
		boolean include_standardization_in_pmml=true;
		boolean use_sklearn2pmml=false;
		
		lanId="tmarti02";		
		boolean buildModels=false;
		
//		serverModelBuilding=DevQsarConstants.SERVER_819;
//		portModelBuilding=5014;

		serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
		portModelBuilding=5004;
		
		ModelWebService.num_jobs=8;
		
		DescriptorEmbeddingServiceImpl descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(serverModelBuilding, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

		List<String>datasetNames=new ArrayList<>();

//		datasetNames.add("HLC from exp_prop and chemprop");
//		datasetNames.add("VP from exp_prop and chemprop");
//		datasetNames.add("LogP from exp_prop and chemprop");
//		datasetNames.add("WS from exp_prop and chemprop v2");
//		datasetNames.add("BP from exp_prop and chemprop v3");
//		datasetNames.add("MP from exp_prop and chemprop v2");

//		datasetNames.add("HLC v1");
//		datasetNames.add("VP v1");
//		datasetNames.add("WS v1");
//		datasetNames.add("BP v1");
//		datasetNames.add("LogP v1");
//		datasetNames.add("MP v1");
		
		datasetNames.add("WS v1 res_qsar");

		
//		datasetNames.add("pKa_a from exp_prop and chemprop");
//		datasetNames.add("pKa_b from exp_prop and chemprop");

//		String splitting =SplittingGeneratorPFAS_Script.splittingPFASOnly;		
		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
//		String splitting = SplittingGeneratorPFAS_Script.splittingAllButPFAS;

		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		System.out.println("\n*** portNumber="+portModelBuilding+" ***");
		
		for (String datasetName:datasetNames) {
						
			boolean remove_log_p = false;
			if(datasetName.contains("LogP")) remove_log_p=true;
			
			CalculationInfoGA ci = new CalculationInfoGA();
			ci.num_generations = 100;			
			
//			if (datasetName.contains("BP") && !splitting.equals(SplittingGeneratorPFAS_Script.splittingPFASOnly))
//				ci.num_generations = 10;// takes too long to do 100

			ci.remove_log_p = remove_log_p;
			ci.qsarMethodEmbedding = qsarMethodEmbedding;
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=splitting;
//			ci.num_jobs=4;
			
			System.out.println("\n***"+datasetName+"\t"+splitting+"\t"+"num_generations="+ci.num_generations+"***");
			
			DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);
			
//			if (descriptorEmbedding==null) {//look for one of the ones made using offline python run:			
//				ci.num_jobs=2;//just takes slighter longer
//				ci.n_threads=16;//doesnt impact knn
//				descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);				
//			}			

			if (descriptorEmbedding == null) {
				System.out.println("Dont have existing embedding:"+ci.toString());
				descriptorEmbedding = ews2.generateGA_Embedding(lanId,ci);
				System.out.println("New embedding from web service:"+descriptorEmbedding.getEmbeddingTsv());
//				continue;
			} else {
				System.out.println("Have embedding from db:"+descriptorEmbedding.getEmbeddingTsv());
			}

			if (!buildModels) continue;

			List<String>methods=new ArrayList<>();			
			methods.add(DevQsarConstants.KNN);
			methods.add(DevQsarConstants.RF);
			methods.add(DevQsarConstants.XGB);
			methods.add(DevQsarConstants.SVM);
			
			for (String method:methods) {
				System.out.println(method + "\t"+ descriptorSetName);
				ModelBuildingScript.buildModel(lanId,serverModelBuilding,portModelBuilding,method,descriptorEmbedding,ci,use_pmml, include_standardization_in_pmml,use_sklearn2pmml);
			}
						
			List<Long>modelIds=buildConsensusModelForEmbeddedModels(descriptorEmbedding, datasetName,methodsConsensus);

			assignModelSetWithEmbedding(splitting, modelIds);
			
		}

	}
	
	

	
	/**
	 * This method uses embedding built specifically for the qsar method
	 * 
	 */
	public static void runCaseStudyExpProp_All_Endpoints_method_specific_embedding() {

		boolean use_pmml=false;
		boolean include_standardization_in_pmml=true;
		boolean use_sklearn2pmml=false;
		
		lanId="tmarti02";		
		boolean buildModels=true;
		boolean buildConsensus=false;
		
//		serverModelBuilding=DevQsarConstants.SERVER_819;
//		portModelBuilding=5014;

		serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
		portModelBuilding=5004;
		
		ModelWebService.num_jobs=8;
		
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(serverModelBuilding, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

		List<String>datasetNames=new ArrayList<>();

//		datasetNames.add("HLC v1 modeling");
//		datasetNames.add("WS v1 modeling");
//		datasetNames.add("VP v1 modeling");
//		datasetNames.add("BP v1 modeling");
//		datasetNames.add("LogP v1 modeling");
//		datasetNames.add("MP v1 modeling");
		
		datasetNames.add("exp_prop_96HR_FHM_LC50_v1 modeling");
		datasetNames.add("exp_prop_96HR_FHM_LC50_v2 modeling");
		
		
		List<String>methods=new ArrayList<>();			
//		methods.add(DevQsarConstants.RF);
		methods.add(DevQsarConstants.XGB);
//		methods.add(DevQsarConstants.KNN);//takes forever to run GA
//		methods.add(DevQsarConstants.SVM);//*** We dont have way yet to make embedding based on this method unless use GA

		
//		String splitting =SplittingGeneratorPFAS_Script.splittingPFASOnly;		
		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
//		String splitting = SplittingGeneratorPFAS_Script.splittingAllButPFAS;

		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
//		System.out.println("\n*** portNumber="+portModelBuilding+" ***");
		
		for (String datasetName:datasetNames) {

			List<Long>modelIds=new ArrayList<>();
			
			boolean remove_log_p = false;
			if(datasetName.contains("LogP")) remove_log_p=true;
			
			
			for (String method:methods) {
				
				if (!buildModels) continue;

				CalculationInfo ci = new CalculationInfo();
				ci.remove_log_p = remove_log_p;
				
//				ci.qsarMethodEmbedding="rf";//**For testing purposes, just use rf embedding for both rf and xgb
				ci.qsarMethodEmbedding=method;//unique embedding for each method
				
				ci.datasetName=datasetName;
				ci.descriptorSetName=descriptorSetName;
				ci.splittingName=splitting;
				

				System.out.println("\n***********************\n"+ci.toString2());
				
				DescriptorEmbedding descriptorEmbedding=null;
				
				if(method.equals(DevQsarConstants.KNN)) {
					
					CalculationInfoGA ciGA=new CalculationInfoGA(ci);
					ciGA.use_wards=false;
					ciGA.qsarMethodEmbedding=DevQsarConstants.KNN;
//					System.out.println("use_wards="+ciGA.use_wards);
					if ((ci.datasetName.contains("LogP") || ci.datasetName.contains("MP") )  
							&& !ci.splittingName.equals(SplittingGeneratorPFAS_Script.splittingPFASOnly)) {
						ciGA.num_generations = 20;// takes too long to do 100
					}
					System.out.println("num gens = "+ciGA.num_generations);
//					ci.num_jobs=4;
					System.out.println("\n***"+ci.datasetName+"\t"+ci.splittingName+"\t"+"num_generations="+ciGA.num_generations+"***");
					descriptorEmbedding=ews2.getEmbeddingGA(ciGA,lanId);
				} else if (method.equals(DevQsarConstants.RF) || method.equals(DevQsarConstants.XGB)) {
					descriptorEmbedding=ews2.generateImportanceEmbedding(ci,lanId,true,true);
				}
				
//				if(true) continue;

//				System.out.println(method+"\t"+descriptorEmbedding.getEmbeddingTsv());

				long modelId=ModelBuildingScript.buildModel(lanId,serverModelBuilding,portModelBuilding,method,descriptorEmbedding,ci,use_pmml, include_standardization_in_pmml,use_sklearn2pmml);
				modelIds.add(modelId);
			}
						
			
			if (buildConsensus) {
				modelIds=buildConsensusModelForEmbeddedModels2(datasetName, methodsConsensusRF_XGB,splitting);
			} 
			
			assignModelSetWithEmbedding(splitting, modelIds);
			
//			String[] methodsConsensus = { DevQsarConstants.KNN, DevQsarConstants.RF, DevQsarConstants.XGB};
			
		}

	}
	
	/**
	 * This method uses embedding built specifically for the qsar method
	 * 
	 */
	public static void runCaseStudyExpProp_All_Endpoints_knn_method_with_GA_embedding() {

		boolean use_pmml=true;
		boolean include_standardization_in_pmml=true;//if false can have descriptor scaling saved in pmml
		boolean use_sklearn2pmml=false;
		
		lanId="tmarti02";		
//		boolean buildModels=true;
//		boolean buildConsensus=true;
		
//		serverModelBuilding=DevQsarConstants.SERVER_819;
//		portModelBuilding=5014;

		serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
		portModelBuilding=5004;
		
		ModelWebService.num_jobs=8;
		
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(serverModelBuilding, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

		List<String>datasetNames=new ArrayList<>();

//		datasetNames.add("HLC v1 modeling");
//		datasetNames.add("WS v1 modeling");
//		datasetNames.add("VP v1 modeling");
//		datasetNames.add("BP v1 modeling");
//		datasetNames.add("LogP v1 modeling");
		datasetNames.add("MP v1 modeling");
		

//		String splitting =SplittingGeneratorPFAS_Script.splittingPFASOnly;		
		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
//		String splitting = SplittingGeneratorPFAS_Script.splittingAllButPFAS;

		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
//		System.out.println("\n*** portNumber="+portModelBuilding+" ***");
		
		for (String datasetName:datasetNames) {
						
			boolean remove_log_p = false;
			if(datasetName.contains("LogP")) remove_log_p=true;
			
			String method=DevQsarConstants.KNN;
			
			CalculationInfo ci = new CalculationInfo();
			ci.remove_log_p = remove_log_p;
			ci.qsarMethodEmbedding=method;//unique embedding for each method
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=splitting;
		
			CalculationInfoGA ciGA=new CalculationInfoGA(ci);
			ciGA.use_wards=false;
			ciGA.qsarMethodEmbedding=DevQsarConstants.KNN;
			if ((ci.datasetName.contains("LogP") || ci.datasetName.contains("MP") )  
					&& !ci.splittingName.equals(SplittingGeneratorPFAS_Script.splittingPFASOnly)) {
				ciGA.num_generations = 20;// takes too long to do 100
			}

			System.out.println("\n***"+ci.datasetName+"\t"+ci.splittingName+"\t"+"num_generations="+ciGA.num_generations+"***");
			System.out.println("\n***********************\n"+ci.toString2());
			DescriptorEmbedding descriptorEmbedding=null;
			descriptorEmbedding=ews2.getEmbeddingGA(ciGA,lanId);
			ModelBuildingScript.buildModel(lanId,serverModelBuilding,portModelBuilding,method,descriptorEmbedding,ci,use_pmml, include_standardization_in_pmml,use_sklearn2pmml);
		}
	}
	

	

	public static void printEmbeddings() {
		
		lanId="tmarti02";		
		
		DescriptorEmbeddingServiceImpl descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();

		List<String>datasetNames=new ArrayList<>();
		datasetNames.add("HLC v1");
		datasetNames.add("VP v1");
		datasetNames.add("BP v1");
		datasetNames.add("WS v1");
		datasetNames.add("LogP v1");
		datasetNames.add("MP v1");

//		String splitting =SplittingGeneratorPFAS_Script.splittingPFASOnly;
//		String splitting = SplittingGeneratorPFAS_Script.splittingAllButPFAS;
		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;

		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;

		System.out.println(splitting);
		System.out.println("Dataset	Len(Embedding)	Embedding");
				
		
		
		for (String datasetName:datasetNames) {
						
			boolean remove_log_p = false;
			if(datasetName.contains("LogP")) remove_log_p=true;
			
			CalculationInfoGA ci = new CalculationInfoGA();
			ci.num_generations = 100;			
			ci.remove_log_p = remove_log_p;
			ci.qsarMethodEmbedding = qsarMethodEmbedding;
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=splitting;
			
			DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);
			
			String [] descriptors=descriptorEmbedding.getEmbeddingTsv().split("\t");
			
			String strDescriptors="";
			
			for (int i=0;i<descriptors.length;i++) {
				String descriptor=descriptors[i];
				strDescriptors+="\""+descriptor+"\"";
				if(i<descriptors.length-1) strDescriptors+=", ";
			}
			
			System.out.println(datasetName+"\t"+descriptors.length+"\t'"+strDescriptors);
		}

	}
	


	public static void runCaseStudyExpProp_All_Endpoints_No_Embedding() {
		boolean use_pmml=false;
		boolean include_standardization_in_pmml=true;
		boolean use_sklearn2pmml=false;
		
		lanId="tmarti02";		
		
		ModelWebService.num_jobs=8;

		serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
		portModelBuilding=5004;
		
		
		
//		serverModelBuilding=DevQsarConstants.SERVER_819;
////		portModelBuilding=5016;
//		portModelBuilding=5014;
		
//		String server=DevQsarConstants.SERVER_819;

		List<String>datasetNames=new ArrayList<>();

//		datasetNames.add("HLC from exp_prop and chemprop");
//		datasetNames.add("WS from exp_prop and chemprop");
//		datasetNames.add("VP from exp_prop and chemprop");
//		datasetNames.add("LogP from exp_prop and chemprop");
//		datasetNames.add("MP from exp_prop and chemprop");
//		datasetNames.add("BP from exp_prop and chemprop v2");
		
//		datasetNames.add("WS from exp_prop and chemprop v2");
//		datasetNames.add("BP from exp_prop and chemprop v3");
//		datasetNames.add("MP from exp_prop and chemprop v2");
		
//		datasetNames.add("HLC v1");
//		datasetNames.add("VP v1");
//		datasetNames.add("WS v1");
//		datasetNames.add("BP v1");
//		datasetNames.add("LogP v1");
//		datasetNames.add("MP v1");
		
		datasetNames.add("WS v1 res_qsar");


//		datasetNames.add("pKa_a from exp_prop and chemprop");
//		datasetNames.add("pKa_b from exp_prop and chemprop");
		
//		String splitting =SplittingGeneratorPFAS_Script.splittingPFASOnly;
		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;		
//		String splitting = SplittingGeneratorPFAS_Script.splittingAllButPFAS;

		System.out.println("\n*** portNumber="+portModelBuilding+" ***");
		
		
		List<String>methods=new ArrayList<>();			
		methods.add(DevQsarConstants.KNN);
		methods.add(DevQsarConstants.RF);
		methods.add(DevQsarConstants.XGB);
		methods.add(DevQsarConstants.SVM);

		
		
		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		for (String datasetName:datasetNames) {
						
			boolean remove_log_p = false;
			if(datasetName.contains("LogP")) remove_log_p=true;
						
			CalculationInfoGA ci=new CalculationInfoGA();
			ci.remove_log_p = remove_log_p;
			ci.qsarMethodEmbedding = qsarMethodEmbedding;
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=splitting;
					
			System.out.println("\n***"+datasetName+"\t"+splitting+"***");
			

			for (String method:methods) {
				System.out.println(method + "\t" + descriptorSetName);
				ModelBuildingScript.buildModel(lanId,serverModelBuilding,portModelBuilding,method,null, ci,use_pmml, include_standardization_in_pmml,use_sklearn2pmml);
			}
			buildConsensusModel(datasetName,splitting,descriptorSetName,methodsConsensus);
		}

	}
	
	
	public static void printDataSetSize() {
		
		lanId="tmarti02";		
		
		List<String>datasetNames=new ArrayList<>();
		datasetNames.add("HLC v1");
		datasetNames.add("VP v1");
		datasetNames.add("BP v1");
		datasetNames.add("WS v1");
		datasetNames.add("LogP v1");
		datasetNames.add("MP v1");
		
//		String splitting =SplittingGeneratorPFAS_Script.splittingPFASOnly;
//		String splitting = SplittingGeneratorPFAS_Script.splittingAllButPFAS;
		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;		

		
		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;

		System.out.println(splitting);

		System.out.println("Dataset\tNtrain\tNtest");
		
		for (String datasetName:datasetNames) {
						
			boolean remove_log_p = false;
			if(datasetName.contains("LogP")) remove_log_p=true;
						
			CalculationInfoGA ci=new CalculationInfoGA();
			ci.remove_log_p = remove_log_p;
			ci.qsarMethodEmbedding = qsarMethodEmbedding;
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=splitting;
					
			
			ModelData data = ModelData.initModelData(ci,false);
			
			long countTraining = data.trainingSetInstances.chars().filter(ch -> ch == '\n').count()-1;
			long countPrediction = data.predictionSetInstances.chars().filter(ch -> ch == '\n').count()-1;
			
			
			System.out.println(datasetName+"\t"+countTraining+"\t"+countPrediction);
//			System.out.println(data.predictionSetInstances);
			
		}

	}
	
	
	public static void runCaseStudyExpProp_All_Endpoints_No_Embedding_RF_XGB() {
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

//		datasetNames.add("HLC v1");
//		datasetNames.add("VP v1");
//		datasetNames.add("WS v1");
//		datasetNames.add("BP v1");
//		datasetNames.add("LogP v1");
//		datasetNames.add("MP v1");

//		datasetNames.add("HLC v1 modeling");
//		datasetNames.add("WS v1 modeling");
//		datasetNames.add("VP v1 modeling");
//		datasetNames.add("BP v1 modeling");
//		datasetNames.add("LogP v1 modeling");
//		datasetNames.add("MP v1 modeling");
		
		datasetNames.add("exp_prop_96HR_FHM_LC50_v1 modeling");
		datasetNames.add("exp_prop_96HR_FHM_LC50_v2 modeling");

		List<String>methods=new ArrayList<>();			
//		methods.add(DevQsarConstants.RF);
		methods.add(DevQsarConstants.XGB);
//		methods.add(DevQsarConstants.KNN);

		
//		String splitting =SplittingGeneratorPFAS_Script.splittingPFASOnly;
		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;		
//		String splitting = SplittingGeneratorPFAS_Script.splittingAllButPFAS;

		System.out.println("\n*** portNumber="+portModelBuilding+" ***");
		
		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		for (String datasetName:datasetNames) {
			List<Long>modelIds=new ArrayList<>();
			
			boolean remove_log_p = false;
			if(datasetName.contains("LogP")) remove_log_p=true;
						
			CalculationInfoGA ci=new CalculationInfoGA();
			ci.remove_log_p = remove_log_p;
			ci.qsarMethodEmbedding = qsarMethodEmbedding;
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
				modelIds=buildConsensusModel2(datasetName,splitting,descriptorSetName,methodsConsensusRF_XGB);
			} 
			assignModelSetNoEmbedding(splitting, modelIds);

		}

	}
	
	public static void runCaseStudyExpProp_All_Endpoints_No_Embedding_kNN() {
		boolean use_pmml=true;
		boolean include_standardization_in_pmml=true;
		boolean use_sklearn2pmml=false;

		lanId="tmarti02";		
		ModelWebService.num_jobs=8;

		serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
		portModelBuilding=5004;
		
//		serverModelBuilding=DevQsarConstants.SERVER_819;
//		portModelBuilding=5014;

		List<String>datasetNames=new ArrayList<>();

//		datasetNames.add("HLC v1");
//		datasetNames.add("VP v1");
//		datasetNames.add("WS v1");
//		datasetNames.add("BP v1");
//		datasetNames.add("LogP v1");
//		datasetNames.add("MP v1");

//		datasetNames.add("HLC v1 modeling");
//		datasetNames.add("WS v1 modeling");
//		datasetNames.add("VP v1 modeling");
//		datasetNames.add("BP v1 modeling");
//		datasetNames.add("LogP v1 modeling");
		datasetNames.add("MP v1 modeling");
		
		String method=DevQsarConstants.KNN;


//		String splitting =SplittingGeneratorPFAS_Script.splittingPFASOnly;
		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;		
//		String splitting = SplittingGeneratorPFAS_Script.splittingAllButPFAS;

		System.out.println("\n*** portNumber="+portModelBuilding+" ***");
		
		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		for (String datasetName:datasetNames) {
			boolean remove_log_p = false;
			if(datasetName.contains("LogP")) remove_log_p=true;
						
			CalculationInfoGA ci=new CalculationInfoGA();
			ci.remove_log_p = remove_log_p;
			ci.qsarMethodEmbedding = qsarMethodEmbedding;
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=splitting;
					
			System.out.println("\n***"+datasetName+"\t"+splitting+"***");
			ModelBuildingScript.buildModel(lanId,serverModelBuilding,portModelBuilding,method,null, ci,use_pmml, include_standardization_in_pmml,use_sklearn2pmml);
		}

	}
	

	public static void runCaseStudyExpProp_All_Endpoints_No_Embedding_Include_kNN() {
		boolean use_pmml=false;
		boolean include_standardization_in_pmml=true;
		boolean use_sklearn2pmml=false;
		
		boolean buildIndividualModels=true;
		boolean buildConsensus=false;

		lanId="tmarti02";		
		ModelWebService.num_jobs=8;

		serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
		portModelBuilding=5004;
		
//		serverModelBuilding=DevQsarConstants.SERVER_819;
//		portModelBuilding=5014;

		List<String>datasetNames=new ArrayList<>();

		datasetNames.add("HLC v1 res_qsar");
//		datasetNames.add("VP v1");
//		datasetNames.add("WS v1");
//		datasetNames.add("BP v1");
//		datasetNames.add("LogP v1");
//		datasetNames.add("MP v1");

//		datasetNames.add("WS v1 res_qsar");
		

		String splitting =SplittingGeneratorPFAS_Script.splittingPFASOnly;
//		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;		
//		String splitting = SplittingGeneratorPFAS_Script.splittingAllButPFAS;

		System.out.println("\n*** portNumber="+portModelBuilding+" ***");
		
		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		for (String datasetName:datasetNames) {
						
			boolean remove_log_p = false;
			if(datasetName.contains("LogP")) remove_log_p=true;
						
			CalculationInfoGA ci=new CalculationInfoGA();
			ci.remove_log_p = remove_log_p;
			ci.qsarMethodEmbedding = qsarMethodEmbedding;
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=splitting;
					
			System.out.println("\n***"+datasetName+"\t"+splitting+"***");

			if (buildIndividualModels) {
				
				List<String>methods=new ArrayList<>();			
				
				methods.add(DevQsarConstants.KNN);
				methods.add(DevQsarConstants.RF);
				methods.add(DevQsarConstants.XGB);
				methods.add(DevQsarConstants.SVM);

				for (String method:methods) {
					System.out.println(method + "\t" + descriptorSetName);
					ModelBuildingScript.buildModel(lanId,serverModelBuilding,portModelBuilding,method,null, ci,use_pmml, include_standardization_in_pmml,use_sklearn2pmml);
				}
			}
			
			if(!buildConsensus) return;
			
			List<Long>modelIds=buildConsensusModel2(datasetName,splitting,descriptorSetName,methodsConsensus);

			assignModelSetNoEmbedding(splitting, modelIds);
			
		}

	}


	private static void assignModelSetNoEmbedding(String splitting, List<Long> modelIds) {
		ModelSet modelSet=null;

		if(splitting.equals(SplittingGeneratorPFAS_Script.splittingPFASOnly)) {
			modelSet=mss.findByName("WebTEST2.0 PFAS");
		} else if(splitting.equals(SplittingGeneratorPFAS_Script.splittingAllButPFAS)) {
			modelSet=mss.findByName("WebTEST2.0 All but PFAS");
		} else if(splitting.equals(DevQsarConstants.SPLITTING_RND_REPRESENTATIVE)) {
			modelSet=mss.findByName("WebTEST2.0");
		}
		
		assignModelsToModelSet(modelIds, modelSet);
	}

	
	private static void assignModelSetWithEmbedding(String splitting, List<Long> modelIds) {
		ModelSet modelSet=null;

		if(splitting.equals(SplittingGeneratorPFAS_Script.splittingPFASOnly)) {
			modelSet=mss.findByName("WebTEST2.1 PFAS");
		} else if(splitting.equals(SplittingGeneratorPFAS_Script.splittingAllButPFAS)) {
			modelSet=mss.findByName("WebTEST2.1 All but PFAS");
		} else if(splitting.equals(DevQsarConstants.SPLITTING_RND_REPRESENTATIVE)) {
			modelSet=mss.findByName("WebTEST2.1");
		}
		
		assignModelsToModelSet(modelIds, modelSet);
		
	}

	
	private static void assignModelsToModelSet(List<Long> modelIds, ModelSet modelSet) {
		for (Long modelId:modelIds) {
			ModelInModelSet m=new ModelInModelSet();				
			m.setCreatedBy(lanId);
			m.setModel(modelService.findById(modelId));
			m.setModelSet(modelSet);
			
			Model model=modelService.findById(modelId);

			try {
				mimss.create(m);
				System.out.println(modelId+"\t"+model.getMethod().getName()+"\tcreated");
			} catch (Exception ex) {
//					System.out.println(ex.getMessage());
				System.out.println(modelId+"\t"+model.getMethod().getName()+"\tNOT created");
			}
		}
	}

	public static void runCV_and_Predict_for_Model() {
		
		lanId="tmarti02";		
		
		ModelWebService.num_jobs=8;

//		serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
//		portModelBuilding=5004;
		serverModelBuilding=DevQsarConstants.SERVER_819;
		portModelBuilding=5014;
		
		long modelId=1702L;
		String qsarMethod=DevQsarConstants.KNN;
		
		boolean use_pmml=false;
		boolean include_standardization_in_pmml=true;
								
		boolean remove_log_p = false;
		
		boolean postPredictions=true;

		ModelWebService modelWs = new ModelWebService(serverModelBuilding, portModelBuilding);
		WebServiceModelBuilder mb = new WebServiceModelBuilder(modelWs, lanId);
		ModelServiceImpl modelService=new ModelServiceImpl();		
		Model model=modelService.findById(modelId);
		
		//Run cross validation:
		mb.crossValidate.crossValidate(model, remove_log_p, ModelWebService.num_jobs, postPredictions,use_pmml);
		
		ModelData modelData = ModelData.initModelData(model.getDatasetName(), model.getDescriptorSetName(), model.getSplittingName(), remove_log_p,false);

		//Run test set predictions:
		mb.predict(modelData, qsarMethod, modelId,use_pmml,include_standardization_in_pmml);
				
//		mb.predictTraining(modelData, qsarMethod, modelId);
	}
	
	
	public static void runCaseStudyOPERA_All_Endpoints() {
		boolean use_pmml=false;
		boolean include_standardization_in_pmml=true;
		boolean use_sklearn2pmml=false;
		
		String server=DevQsarConstants.SERVER_LOCAL;
		DescriptorEmbeddingService descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(server, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

		String sampleSource="OPERA";
			/*	
		String [] endpoints= {DevQsarConstants.LOG_KOA,DevQsarConstants.LOG_KM_HL,DevQsarConstants.HENRYS_LAW_CONSTANT,
				DevQsarConstants.LOG_BCF,DevQsarConstants.LOG_OH,DevQsarConstants.LOG_KOC,DevQsarConstants.VAPOR_PRESSURE,
				DevQsarConstants.WATER_SOLUBILITY, DevQsarConstants.BOILING_POINT, DevQsarConstants.MELTING_POINT,
				DevQsarConstants.LOG_KOW};
			*/
		String [] endpoints= {DevQsarConstants.HENRYS_LAW_CONSTANT};

		for (String endpoint:endpoints) {
			System.out.println(endpoint);

			String datasetName = endpoint +" "+sampleSource;
			boolean removeLogDescriptors=endpoint.equals(DevQsarConstants.LOG_KOW);

			CalculationInfoGA ci = new CalculationInfoGA();
			ci.num_generations = 100;
			ci.remove_log_p = removeLogDescriptors;
			ci.qsarMethodEmbedding = qsarMethodEmbedding;
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=sampleSource;

			DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);

			if (descriptorEmbedding == null) {
				descriptorEmbedding = ews2.generateGA_Embedding(lanId,ci);
				System.out.println("New embedding from web service:"+descriptorEmbedding.getEmbeddingTsv());
			} else {
				System.out.println("Have embedding from db:"+descriptorEmbedding.getEmbeddingTsv());
			}

//			if (true) continue;//skip model building

//			String methods[]= {DevQsarConstants.KNN, DevQsarConstants.RF, DevQsarConstants.XGB, DevQsarConstants.SVM};
			String methods[]= {DevQsarConstants.KNN};

			for (String method:methods) {
				System.out.println(method + "descriptor" + descriptorSetName);
				ModelBuildingScript.buildModel(lanId,server,portModelBuilding,method,descriptorEmbedding,ci,use_pmml, include_standardization_in_pmml,use_sklearn2pmml);
			}

			buildConsensusModelForEmbeddedModels(descriptorEmbedding, datasetName,methodsConsensus);

		}

	}
	
	
	private static void buildConsensusModel(String datasetName,String splittingName,String descriptorsetName,String []methodsConsensus) {
		
		
		String sql="select id from qsar_models.models m "
				+ "where m.splitting_name ='"+splittingName+"' and "
				+ "dataset_name ='"+datasetName+"' and "
				+ "descriptor_set_name ='"+descriptorsetName+"' and "
				+ "fk_descriptor_embedding_id is null;";
		
		
		Connection conn=SqlUtilities.getConnectionPostgres();
		ResultSet rs=SqlUtilities.runSQL2(conn, sql);
		
		Set<Long> consensusModelIDs = new HashSet<Long>(); 
		
		try {
			while (rs.next()) {
				consensusModelIDs.add(Long.parseLong(rs.getString(1)));
			}
			boolean OK=areModelsOKForConsensus(methodsConsensus, consensusModelIDs);
			
			if (OK) {
				System.out.println("ok to build consensus");	
			} else {
				return;
			}
			
			ModelBuildingScript.buildUnweightedConsensusModel(consensusModelIDs, lanId);
			System.out.println("model built");
			
		} catch (Exception ex) {
			return;
		}
	
	}
	
	
	private static List<Long> buildConsensusModel2(String datasetName,String splittingName,String descriptorsetName,String []methodsConsensus) {
		
		
		
		String sql="select id from qsar_models.models m "
				+ "where m.splitting_name ='"+splittingName+"' and "
				+ "dataset_name ='"+datasetName+"' and "
				+ "descriptor_set_name ='"+descriptorsetName+"' and "
				+ "fk_descriptor_embedding_id is null;";
		
		
		Connection conn=SqlUtilities.getConnectionPostgres();
		ResultSet rs=SqlUtilities.runSQL2(conn, sql);
		
		Set<Long> consensusModelIDs = new HashSet<Long>(); 
		
		try {
			
			while (rs.next()) {
				Long modelID=Long.parseLong(rs.getString(1));
				
				Model model=modelService.findById(modelID);
				
				if (!model.getMethod().getName().contains("_")) continue;
				
				String modelAbbrev=model.getMethod().getName();
				modelAbbrev=modelAbbrev.substring(0,modelAbbrev.indexOf("_"));
				
				boolean methodInConsensusList=false;
				for (String methodAbbrev:methodsConsensus) {
					if(modelAbbrev.equals(methodAbbrev)) {
						methodInConsensusList=true;
						break;
					}
				}				
				if(!methodInConsensusList) continue;
				
				
				System.out.println("ok model: "+modelID+"\t"+modelAbbrev);
				
				consensusModelIDs.add(modelID);
			}
			
			boolean OK=areModelsOKForConsensus(methodsConsensus, consensusModelIDs);
			
			if (OK) {
				System.out.println("ok to build consensus");	
			} else {
				System.out.println("NOT ok to build consensus");
				return null;
			}
			
//			if (true)return null;
			
			Long modelID_Consensus=ModelBuildingScript.buildUnweightedConsensusModel(consensusModelIDs, lanId);
			
			System.out.println("model built");
			
			List<Long>modelIds=new ArrayList<>();//model ids including consensus model
			for (Long id:consensusModelIDs) {
				modelIds.add(id);
			}
			modelIds.add(modelID_Consensus);
			return modelIds;
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	
	}
	
	
	private static List<Long> buildConsensusModelForEmbeddedModels(DescriptorEmbedding descriptorEmbedding,String datasetName, String[] methodsConsensus) {
		ModelServiceImpl modelService=new ModelServiceImpl();
		List<Model>models=modelService.findByDatasetName(datasetName);
		
		Set<Long> consensusModelIDs = new HashSet<Long>();
		
		for (Model model:models) {
			if (model.getDescriptorEmbedding()==null) continue;//this will skip consensus models			
			if (!model.getDescriptorEmbedding().getId().equals(descriptorEmbedding.getId())) continue;
//			System.out.println(model.getId());
			
			if (!model.getMethod().getName().contains("_")) continue;
			
			String modelAbbrev=model.getMethod().getName();
			modelAbbrev=modelAbbrev.substring(0,modelAbbrev.indexOf("_"));
			
			boolean methodInConsensusList=false;
			for (String methodAbbrev:methodsConsensus) {
				if(modelAbbrev.equals(methodAbbrev)) {
					methodInConsensusList=true;
					break;
				}
			}				
			if(!methodInConsensusList) continue;

			consensusModelIDs.add(model.getId());
		}
		
		boolean OK=areModelsOKForConsensus(methodsConsensus, consensusModelIDs);
		
		if(OK) {
			System.out.println("ok to build consensus");	
		} else {
			return null;
		}
		
		Long modelIdConsensus=ModelBuildingScript.buildUnweightedConsensusModel(consensusModelIDs, lanId);
		
		System.out.println("model built");
		
		List<Long>modelIds=new ArrayList<>();//model ids including consensus model
		for (Long id:consensusModelIDs) {
			modelIds.add(id);
		}
		modelIds.add(modelIdConsensus);
		return modelIds;
		
	}

	
	private static List<Long> buildConsensusModelForEmbeddedModels2(String datasetName, String[] methodsConsensus,String splittingName) {
		ModelServiceImpl modelService=new ModelServiceImpl();
		List<Model>models=modelService.findByDatasetName(datasetName);
		
		Set<Long> consensusModelIDs = new HashSet<Long>();
		
		List<String>remainingMethodsToInclude=new ArrayList<>();
		for (String method:methodsConsensus) remainingMethodsToInclude.add(method);
		
//		System.out.println(remainingMethodsToInclude.size());
		
		
		for (Model model:models) {

//			if(model.getId()<721) continue;
			
			if (!model.getMethod().getName().contains("_")) continue;
			if (!model.getSplittingName().equals(splittingName)) continue;

			String modelAbbrev=model.getMethod().getName();
			modelAbbrev=modelAbbrev.substring(0,modelAbbrev.indexOf("_"));

			if (model.getDescriptorEmbedding()==null) continue;//this will skip consensus models			
			
			if(!modelAbbrev.equals(model.getDescriptorEmbedding().getQsarMethod())) {
				continue;
			}
			
			boolean methodInConsensusList=false;
			for (String methodAbbrev:methodsConsensus) {
				if(modelAbbrev.equals(methodAbbrev)) {
					methodInConsensusList=true;
					break;
				}
			}				
			if(!methodInConsensusList) continue;

			System.out.println(model.getId()+"\t"+model.getMethod().getName()+"\t"+model.getDescriptorEmbedding().getQsarMethod());
			remainingMethodsToInclude.remove(modelAbbrev);
			consensusModelIDs.add(model.getId());
		}
		
		
//		System.out.println(remainingMethodsToInclude.size());
		
		if (remainingMethodsToInclude.size()>0) {
			System.out.println("Havent created all required methods for consensus");
			return null;
		}
		
		boolean OK=areModelsOKForConsensus(methodsConsensus, consensusModelIDs);
		
		if(OK) {
			System.out.println("ok to build consensus");	
		} else {
			return null;
		}
		
		
//		if(true) {
//			System.out.println("Need to uncomment 1234");
//			return null;
//		}
		
		Long modelIdConsensus=ModelBuildingScript.buildUnweightedConsensusModel(consensusModelIDs, lanId);
		
		System.out.println("model built");
		
		List<Long>modelIds=new ArrayList<>();//model ids including consensus model
		for (Long id:consensusModelIDs) {
			modelIds.add(id);
		}
		modelIds.add(modelIdConsensus);
		return modelIds;
		
	}

	private static boolean areModelsOKForConsensus(String [] methodsConsensus, Set<Long> consensusModelIDs) {

//		System.out.println("Enter areModelsOKForConsensus");
		
		
		if(methodsConsensus.length !=consensusModelIDs.size()) {
			System.out.println("Mismatch have "+consensusModelIDs.size()+" potential models in consensus");
			return false;
		}
		
		Iterator<Long>iterator=consensusModelIDs.iterator();
		Connection conn=SqlUtilities.getConnectionPostgres();
		
		List <Integer>counts=new ArrayList<>();
		while(iterator.hasNext()) {
			Long modelID=iterator.next();
			
			String sql="select count(id) from qsar_models.predictions p where p.fk_model_id="+modelID+";";
			String result=SqlUtilities.runSQL(conn, sql);
			counts.add(Integer.parseInt(result));
//			System.out.println(modelID+"\t"+result);
		}
		
		int count0=counts.get(0);
		boolean allMatch=true;
		
//		System.out.println(count0);
				
		for (int i=1;i<counts.size();i++) {
//			System.out.println(counts.get(i));
			if(counts.get(i)!=count0) {
				allMatch=false;
				break;
			}
		}
		
		if(!allMatch) {
			System.out.println("mismatch in predictions size for methods in consensus");
			return false;
		}
		
		return true;
	}


	/**
	 * Programmatic way of finding the model. Could do it using sql faster though
	 * 
	 * @param datasetName
	 * @param qsarMethodAbbrev
	 * @param embeddingDescription
	 * @param splittingName
	 * @param descriptorsetName
	 * @return
	 */
	private static List<Model> findModels(String datasetName,String qsarMethodAbbrev,String embeddingDescription,
			String splittingName,String descriptorsetName) {

		ModelServiceImpl ms=new ModelServiceImpl();
		
		List<Model> models=ms.findByDatasetName(datasetName);
		List<Model> models2=ms.findByDatasetName(datasetName);
		
		for (Model model:models) {
			if (model.getDescriptorEmbedding()==null) continue;
			if(model.getSplittingName()==null) continue;

			if (!model.getMethod().getName().contains(qsarMethodAbbrev)) {
//				System.out.println("methodName mismatch:"+model.getMethod().getName());
				continue;			
			}
			if (!model.getDescriptorEmbedding().getDescription().equals(embeddingDescription)) {
//				System.out.println("embeddingDescription mismatch:"+model.getDescriptorEmbedding().getDescription());
				continue;
			}
			if (!model.getSplittingName().equals(splittingName)) {
//				System.out.println("splittingName mismatch:"+model.getSplittingName());
				continue;
			}
			
			if (!model.getDescriptorSetName().equals(descriptorsetName)) {
//				System.out.println("descriptorsetName mismatch:"+model.getDescriptorSetName());
				continue;
			}
			
			models2.add(model);
			
		}
		
		return models2;
	}
	
	
	static void getNumDescriptorsFromDetails() {

		List<String> datasetNames = new ArrayList<>();
		datasetNames.add("HLC v1 modeling");
		datasetNames.add("VP v1 modeling");
		datasetNames.add("BP v1 modeling");
		datasetNames.add("WS v1 modeling");
		datasetNames.add("LogP v1 modeling");
		datasetNames.add("MP v1 modeling");


		for (String dataset:datasetNames) {

			String splitting_name="RND_REPRESENTATIVE";
			//		long fk_method_id=7;//RF
			long fk_method_id=5;//kNN

			String sql="select details from qsar_models.models m where m.dataset_name='"+dataset+"' "
					+ "and splitting_name='"+splitting_name+"' and "
					+ "fk_method_id="+fk_method_id+" and fk_descriptor_embedding_id is not null";


			Connection conn=SqlUtilities.getConnectionPostgres();

			ResultSet rs=SqlUtilities.runSQL2(conn, sql);

			try {
				while(rs.next()) {
					String results=new String(rs.getBytes(1));

					JsonObject jo=Utilities.gson.fromJson(results, JsonObject.class);

					JsonArray ja=jo.get("embedding").getAsJsonArray();


					System.out.println(dataset+"\t"+ja.size());	
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		runCaseStudyOPERA();
//		runCaseStudyTest_All_Endpoints();
//		runCaseStudyTest();
//		runCaseStudyOPERA_All_Endpoints();
		
//		ModelBuilder mb=new ModelBuilder("tmarti02");
//		mb.postPredictionsSQL(null, null, new Splitting(), null);

//		printDataSetSize();
//		printEmbeddings();
		
//		runCaseStudyExpProp_All_Endpoints();
		
		runCaseStudyExpProp_All_Endpoints_method_specific_embedding();
		runCaseStudyExpProp_All_Endpoints_No_Embedding_RF_XGB();
		
//		runCaseStudyExpProp_All_Endpoints_No_Embedding_kNN();
//		runCaseStudyExpProp_All_Endpoints_knn_method_with_GA_embedding();
		
//		runCaseStudyExpProp_All_Endpoints_No_Embedding();
//		runCaseStudyExpProp_All_Endpoints_No_Embedding_Include_kNN();
		
//		runCV_and_Predict_for_Model();
//		getNumDescriptorsFromDetails();
		
	}
	
	

}

