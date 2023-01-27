package gov.epa.run_from_java.scripts;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingService;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionServiceImpl;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.models.ModelStatisticCalculator;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;
import gov.epa.web_services.embedding_service.CalculationInfo;
import gov.epa.web_services.embedding_service.EmbeddingWebService2;

public class RunCaseStudies {

	static String lanId="cramslan";
//	static String lanId = "tmarti02";
	
	static int portModelBuilding=DevQsarConstants.PORT_PYTHON_MODEL_BUILDING;

//	static String serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
	static String serverModelBuilding=DevQsarConstants.SERVER_819;
//	static String serverModelBuilding="10.140.73.169";
	
	static String qsarMethodGA = DevQsarConstants.KNN;

	static String descriptorSetName = DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
	//  ""T.E.S.T. 5.1",	"PaDEL-default", "RDKit-default", "WebTEST-default", "ToxPrints-default",

	
	public static void runCaseStudyOPERA() {
		
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
		
		CalculationInfo ci = new CalculationInfo();
		ci.num_generations = 100;
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
		
		if(true) return;//skip model building

		String methods[]= {DevQsarConstants.KNN, DevQsarConstants.RF, DevQsarConstants.XGB, DevQsarConstants.SVM};

		for (String method:methods) {
			System.out.println(method + "descriptor" + descriptorSetName);
			ModelBuildingScript.buildModel(lanId,serverModelBuilding,portModelBuilding,method,descriptorEmbedding,ci);
		}
		
		buildConsensusModelForEmbeddedModels(descriptorEmbedding, datasetName, methods.length);

	}
	
	
	public static void runCaseStudyTest_All_Endpoints() {
		
		DescriptorEmbeddingService descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

		String sampleSource="TEST";
		
		/*
		*/
		String [] endpoints= {DevQsarConstants.MUTAGENICITY, DevQsarConstants.LD50,
				DevQsarConstants.LC50DM, DevQsarConstants.DEV_TOX, DevQsarConstants.LLNA,
				DevQsarConstants.LC50, DevQsarConstants.IGC50};


		for (String endpoint:endpoints) {
			System.out.println(endpoint);

			String datasetName = endpoint +" "+sampleSource;
			boolean removeLogDescriptors=endpoint.equals(DevQsarConstants.LOG_KOW);

			CalculationInfo ci = new CalculationInfo();
			ci.num_generations = 100;
			ci.remove_log_p = removeLogDescriptors;
			ci.qsarMethodGA = qsarMethodGA;
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=sampleSource;

			DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);

			if (descriptorEmbedding == null) {
				descriptorEmbedding = ews2.generateEmbedding(serverModelBuilding, portModelBuilding, lanId,ci);
				System.out.println("New embedding from web service:"+descriptorEmbedding.getEmbeddingTsv());
			} else {
				System.out.println("Have embedding from db:"+descriptorEmbedding.getEmbeddingTsv());
			}

//			if (true) continue;//skip model building

			String methods[]= {DevQsarConstants.KNN, DevQsarConstants.RF, DevQsarConstants.XGB, DevQsarConstants.SVM};

			for (String method:methods) {
				System.out.println(method + "descriptor" + descriptorSetName);
				ModelBuildingScript.buildModel(lanId,serverModelBuilding,portModelBuilding,method,descriptorEmbedding,ci);
			}

			buildConsensusModelForEmbeddedModels(descriptorEmbedding, datasetName,methods.length);

		}

	}
	
	public static void runCaseStudyTest() {
		lanId="tmarti02";
		
		boolean buildModel=true;
		
		serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
		
		DescriptorEmbeddingService descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(serverModelBuilding, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

		String sampleSource="TEST";
		String endpoint=DevQsarConstants.MUTAGENICITY;
		String method=DevQsarConstants.SVM;

		String datasetName = endpoint +" "+sampleSource;
		boolean removeLogDescriptors=endpoint.equals(DevQsarConstants.LOG_KOW);

		CalculationInfo ci = new CalculationInfo();
		ci.num_generations = 100;
		ci.remove_log_p = removeLogDescriptors;
		ci.qsarMethodGA = qsarMethodGA;
		ci.datasetName=datasetName;
		ci.descriptorSetName=descriptorSetName;
		ci.splittingName=sampleSource;

		DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);

		if (descriptorEmbedding == null) {
			descriptorEmbedding = ews2.generateEmbedding(serverModelBuilding, portModelBuilding, lanId,ci);
			System.out.println("New embedding from web service:"+descriptorEmbedding.getEmbeddingTsv());
		} else {
			System.out.println("Have embedding from db:"+descriptorEmbedding.getEmbeddingTsv());
		}

		if (!buildModel) return;//skip model building

		System.out.println(method + "descriptor" + descriptorSetName);
		ModelBuildingScript.buildModel(lanId,serverModelBuilding,portModelBuilding,method,descriptorEmbedding,ci);


	}
	
	

	public static void runCaseStudyExpProp_All_Endpoints() {
		
		lanId="tmarti02";		
		boolean buildModels=true;
		
//		serverModelBuilding=DevQsarConstants.SERVER_819;
		serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
		portModelBuilding=5004;
		
		DescriptorEmbeddingServiceImpl descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(serverModelBuilding, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

		List<String>datasetNames=new ArrayList<>();

//		datasetNames.add("HLC from exp_prop and chemprop");
		datasetNames.add("WS from exp_prop and chemprop");
		datasetNames.add("VP from exp_prop and chemprop");
		datasetNames.add("LogP from exp_prop and chemprop");
		datasetNames.add("MP from exp_prop and chemprop");
		datasetNames.add("BP from exp_prop and chemprop");
		
//		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
		String splitting ="T=PFAS only, P=PFAS";
//		String splitting = "T=all but PFAS, P=PFAS";

		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		for (String datasetName:datasetNames) {
						
			boolean remove_log_p = false;
			if(datasetName.contains("LogP")) remove_log_p=true;
			
			CalculationInfo ci = new CalculationInfo();
			ci.num_generations = 100;			
			if (datasetName.contains("BP") || splitting.equals("T=all but PFAS, P=PFAS")) ci.num_generations=10;//takes too long to do 100			

			ci.remove_log_p = remove_log_p;
			ci.qsarMethodGA = qsarMethodGA;
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
//				descriptorEmbedding = ews2.generateEmbedding(serverModelBuilding, portModelBuilding, lanId,ci);
//				System.out.println("New embedding from web service:"+descriptorEmbedding.getEmbeddingTsv());
				System.out.println("Dont have existing embedding:"+ci.toString());
				continue;
			} else {
				System.out.println("Have embedding from db:"+descriptorEmbedding.getEmbeddingTsv());
			}

			if (!buildModels) continue;

			String methods[]= {DevQsarConstants.KNN, DevQsarConstants.RF, DevQsarConstants.XGB, DevQsarConstants.SVM};
//			String methods[]= {DevQsarConstants.SVM};
//			String methods[]= {DevQsarConstants.KNN};
//			String methods[]= {DevQsarConstants.RF, DevQsarConstants.XGB, DevQsarConstants.SVM};
//			String methods[]= {DevQsarConstants.XGB, DevQsarConstants.SVM};
//			String methods[]= {DevQsarConstants.KNN, DevQsarConstants.RF};

//			for (String method:methods) {
//				System.out.println(method + "descriptor" + descriptorSetName);
//				ModelBuildingScript.buildModel(lanId,serverModelBuilding,portModelBuilding,method,descriptorEmbedding,ci);
//			}
			buildConsensusModelForEmbeddedModels(descriptorEmbedding, datasetName,methods.length);
		}

	}
	
	
	public static void runCaseStudyExpProp_All_Endpoints_No_Embedding() {
		
		lanId="tmarti02";		
		
		serverModelBuilding=DevQsarConstants.SERVER_819;
		portModelBuilding=5005;
		
		
//		String server=DevQsarConstants.SERVER_819;

		List<String>datasetNames=new ArrayList<>();

		datasetNames.add("HLC from exp_prop and chemprop");
		datasetNames.add("WS from exp_prop and chemprop");
		datasetNames.add("VP from exp_prop and chemprop");
		datasetNames.add("LogP from exp_prop and chemprop");
//		datasetNames.add("MP from exp_prop and chemprop");
//		datasetNames.add("BP from exp_prop and chemprop");
		
//		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
		String splitting ="T=PFAS only, P=PFAS";
//		String splittingAllButPFAS = "T=all but PFAS, P=PFAS";

		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		for (String datasetName:datasetNames) {
						
			boolean remove_log_p = false;
			if(datasetName.contains("LogP")) remove_log_p=true;
						
			CalculationInfo ci=new CalculationInfo();
			ci.remove_log_p = remove_log_p;
			ci.qsarMethodGA = qsarMethodGA;
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=splitting;

					
			System.out.println("\n***"+datasetName+"\t"+splitting+"***");
			
			String methods[]= {DevQsarConstants.KNN, DevQsarConstants.RF, DevQsarConstants.XGB, DevQsarConstants.SVM};
//			String methods[]= {DevQsarConstants.SVM};
//			String methods[]= {DevQsarConstants.KNN};
//			String methods[]= {DevQsarConstants.RF, DevQsarConstants.XGB, DevQsarConstants.SVM};
//			String methods[]= {DevQsarConstants.XGB, DevQsarConstants.SVM};
//			String methods[]= {DevQsarConstants.KNN, DevQsarConstants.RF};

//			for (String method:methods) {
//				System.out.println(method + "descriptor" + descriptorSetName);
//				ModelBuildingScript.buildModel(lanId,serverModelBuilding,portModelBuilding,method,null, ci);
//			}
			
			buildConsensusModel(datasetName,splitting,descriptorSetName,methods.length);
			
		}

	}
	
	
	public static void runCaseStudyOPERA_All_Endpoints() {
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
		String [] endpoints= {DevQsarConstants.BOILING_POINT, DevQsarConstants.MELTING_POINT,
				DevQsarConstants.LOG_KOW};

		for (String endpoint:endpoints) {
			System.out.println(endpoint);

			String datasetName = endpoint +" "+sampleSource;
			boolean removeLogDescriptors=endpoint.equals(DevQsarConstants.LOG_KOW);

			CalculationInfo ci = new CalculationInfo();
			ci.num_generations = 100;
			ci.remove_log_p = removeLogDescriptors;
			ci.qsarMethodGA = qsarMethodGA;
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=sampleSource;

			DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);

			if (descriptorEmbedding == null) {
				descriptorEmbedding = ews2.generateEmbedding(server, portModelBuilding, lanId,ci);
				System.out.println("New embedding from web service:"+descriptorEmbedding.getEmbeddingTsv());
			} else {
				System.out.println("Have embedding from db:"+descriptorEmbedding.getEmbeddingTsv());
			}

//			if (true) continue;//skip model building

			String methods[]= {DevQsarConstants.KNN, DevQsarConstants.RF, DevQsarConstants.XGB, DevQsarConstants.SVM};

			for (String method:methods) {
				System.out.println(method + "descriptor" + descriptorSetName);
				ModelBuildingScript.buildModel(lanId,server,portModelBuilding,method,descriptorEmbedding,ci);
			}

			buildConsensusModelForEmbeddedModels(descriptorEmbedding, datasetName,methods.length);

		}

	}
	
	
	private static void buildConsensusModel(String datasetName,String splittingName,String descriptorsetName, int countRequired) {
		
		
		String sql="select id from qsar_models.models m "
				+ "where m.splitting_name ='"+splittingName+"' and "
				+ "dataset_name ='"+datasetName+"' and "
				+ "descriptor_set_name ='"+descriptorsetName+"' and "
				+ "fk_descriptor_embedding_id is null;";
		
		
		Connection conn=DatabaseLookup.getConnection();
		ResultSet rs=DatabaseLookup.runSQL2(conn, sql);
		
		Set<Long> consensusModelIDs = new HashSet<Long>(); 
		
		try {
			while (rs.next()) {
				consensusModelIDs.add(Long.parseLong(rs.getString(1)));
			}
			boolean OK=areModelsOKForConsensus(countRequired, consensusModelIDs);
			
			if (OK) {
				System.out.println("ok to build consensus");	
			} else {
				return;
			}
			
			ModelBuildingScript.buildUnweightedConsensusModel(consensusModelIDs, lanId);
			
		} catch (Exception ex) {
			return;
		}
	
	}
	
	
	private static void buildConsensusModelForEmbeddedModels(DescriptorEmbedding descriptorEmbedding,String datasetName, int countRequired) {
		ModelServiceImpl modelService=new ModelServiceImpl();
		List<Model>models=modelService.findByDatasetName(datasetName);
		
		Set<Long> consensusModelIDs = new HashSet<Long>();
		
		for (Model model:models) {
			if (model.getDescriptorEmbedding()==null) continue;			
			if (!model.getDescriptorEmbedding().getId().equals(descriptorEmbedding.getId())) continue;
//			System.out.println(model.getId());
			consensusModelIDs.add(model.getId());
		}
		
		boolean OK=areModelsOKForConsensus(countRequired, consensusModelIDs);
		
		if(OK) {
			System.out.println("ok to build consensus");	
		} else {
			return;
		}
		
		ModelBuildingScript.buildUnweightedConsensusModel(consensusModelIDs, lanId);
	}


	private static boolean areModelsOKForConsensus(int countRequired, Set<Long> consensusModelIDs) {

		if(countRequired !=consensusModelIDs.size()) {
			System.out.println("Mismatch have "+consensusModelIDs.size()+" potential models in consensus");
			return false;
		}
		
		Iterator<Long>iterator=consensusModelIDs.iterator();
		Connection conn=DatabaseLookup.getConnection();
		
		List <Integer>counts=new ArrayList<>();
		while(iterator.hasNext()) {
			String sql="select count(id) from qsar_models.predictions p where p.fk_model_id="+iterator.next()+";";
			String result=DatabaseLookup.runSQL(conn, sql);
			counts.add(Integer.parseInt(result));
//			System.out.println(result);
		}
		
		int count0=counts.get(0);
		boolean allMatch=true;
		for (int i=1;i<counts.size();i++) {
			if(counts.get(i)!=count0) {
				allMatch=false;
				break;
			}
		}
		
		if(!allMatch) {
			System.out.println("mismatch in prediction size");
			return false;
		}
		
		return true;
	}

	/**
	 * Deletes a model by id
	 * Needs to delete the bytes first or it wont work (doesnt happen automatically like other tables do)
	 * 
	 * @param id
	 */
	static void deleteModel(long id) {
		ModelServiceImpl ms=new ModelServiceImpl();
		Model model=ms.findById(id);
		
		if(model==null) return;
		
		System.out.println("deleting:"+id);		
		ModelBytesServiceImpl mb=new ModelBytesServiceImpl();
		ModelBytes modelBytes=mb.findByModelId(model.getId());
		if(modelBytes!=null) mb.delete(modelBytes);		
		ms.delete(model);
	}
	
	/**
	 * Deletes a model by id
	 * Needs to delete the bytes first or it wont work (doesnt happen automatically like other tables do)
	 * 
	 * @param id
	 */
	static void deleteModelsNoBytes() {
		ModelServiceImpl ms=new ModelServiceImpl();
		ModelBytesServiceImpl mb=new ModelBytesServiceImpl();
		
		List<Model>models=ms.getAll();
		System.out.println("Number of models="+models.size());
		
		
//		for (Model model:models) {
		for (int i=0;i<models.size();i++) {
			
			if(i<500) continue;
			
			Model model=models.get(i);
			
			System.out.println(i);
			
			if (model.getMethod().getName().contains("consensus")) continue;				
			if (model.getMethod().getName().contains("_")) continue;

			ModelBytes modelBytes=mb.findByModelId(model.getId());
			
			if(modelBytes==null) {
				
//				System.out.println(model.getId()+"\t"+model.getMethod().getName());
//				if (model.getId()==571L) continue;//need to delete consensus it's attached to
				System.out.println("deleting:"+model.getId()+"\t"+model.getMethod().getName());
				ms.delete(model);
								
			}
		}
	}

	/**
	 * For splitting= "T=all, P=PFAS" generates the stats P=PFAS without having to make new models
	 * for this splitting (can use model for RND_REPRESENTATIVE)
	 * 
	 */
	public static void calcPredictionStatsForPFAS() {
		
		//Getting the model:
		String datasetName="Boiling point OPERA";
		String splittingName="OPERA";
		String descriptorsetName="T.E.S.T. 5.1";
		CalculationInfo ci=new CalculationInfo();
		String modelAbbrev=DevQsarConstants.KNN;
		ci.threshold=null;//TODO omit this later since this is an old model!
		
		
		List<Model> models=findModels(datasetName,modelAbbrev,ci.toString(),splittingName,descriptorsetName);
		
		if(models.size()==0) {
			System.out.println("cant find model for "+datasetName+"\t"+modelAbbrev+"\t"+splittingName);
			System.out.println(ci.toString());
			return;
		}
		
		if(models.size()>1) {
			System.out.println("more than one model for "+datasetName+"\t"+modelAbbrev+"\t"+splittingName);
			System.out.println(ci.toString());
			return;
		}

		Model model=models.get(0);
		
		//	***************************************
		// Getting predictions for PFAS compounds in test set:		
		List<ModelPrediction>testSetPredictions=getModelPredictions(datasetName, model,1);
		
		String listName="PFASSTRUCTV4";		
		String folder="data/dev_qsar/dataset_files/";
		String filePath=folder+listName+"_qsar_ready_smiles.txt";
		ArrayList<String>smilesArray=SplittingGeneratorPFAS.getPFASSmiles(filePath);
		
		//Remove non PFAS compounds:
		for (int i=0;i<testSetPredictions.size();i++) {
			ModelPrediction mp=testSetPredictions.get(i);
			if(!smilesArray.contains(mp.ID)) {
				testSetPredictions.remove(i--);
			}
		}
		
		for (int i=0;i<testSetPredictions.size();i++) {
			ModelPrediction mp=testSetPredictions.get(i);
			System.out.println(mp.ID+"\t"+mp.exp+"\t"+mp.pred);
		}

		//	***************************************
		// Calc stats		
		double mean_exp_training=0;//TODO q2 will be wrong unless fixed. 
		Map<String, Double>statsMap=ModelStatisticCalculator.calculateContinuousStatistics(testSetPredictions,mean_exp_training,DevQsarConstants.TAG_TEST);
		double pearsonRsq=statsMap.get(DevQsarConstants.PEARSON_RSQ + DevQsarConstants.TAG_TEST);
		System.out.println(testSetPredictions.size()+"\t"+pearsonRsq);//need to make sure number of chemicals matches excel table
	}


	private static List<ModelPrediction> getModelPredictions(String datasetName, Model model,int splitNum) {
		
		String sql="select dp.canon_qsar_smiles, dp.qsar_property_value, p.qsar_predicted_value  from qsar_datasets.data_points dp\n"+ 
		"join qsar_datasets.data_points_in_splittings dpis on dpis.fk_data_point_id =dp.id\n"+ 
		"join qsar_datasets.datasets d on d.id=dp.fk_dataset_id\n"+
		"join qsar_models.predictions p on p.canon_qsar_smiles =dp.canon_qsar_smiles\n"+ 
		"where d.\"name\" ='"+datasetName+"' and dpis.split_num="+splitNum+" and p.fk_model_id="+model.getId()+";";

		Connection conn=DatabaseLookup.getConnection();
		ResultSet rs=DatabaseLookup.runSQL2(conn, sql);
		List<ModelPrediction>modelPredictions=new ArrayList<>(); 
		
		try {
			while (rs.next()) {				
				String ID=rs.getString(1);
				Double exp=Double.parseDouble(rs.getString(2));
				Double pred=Double.parseDouble(rs.getString(3));
//				System.out.println(id+"\t"+qsar_property_value+"\t"+qsar_predicted_value);
				modelPredictions.add(new ModelPrediction(ID,exp,pred));
			}
			return modelPredictions;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
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
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		runCaseStudyOPERA();
//		runCaseStudyTest_All_Endpoints();
//		runCaseStudyTest();
//		runCaseStudyOPERA_All_Endpoints();
		
//		runCaseStudyExpProp_All_Endpoints();		
		runCaseStudyExpProp_All_Endpoints_No_Embedding();
		
//		for (int i=641;i<=649;i++) {
//			deleteModel(i);
//		}
		
//		deleteModel(658L);
//		deleteModelsNoBytes();
//		calcPredictionStatsForPFAS();
	}
	
	

}
