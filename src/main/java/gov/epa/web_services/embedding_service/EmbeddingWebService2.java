package gov.epa.web_services.embedding_service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.dao.DescriptorEmbeddingDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingService;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingServiceImpl;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.models.WebServiceModelBuilder;
import gov.epa.web_services.WebService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class EmbeddingWebService2 extends WebService {

	CalculationInfoGA calculationInfo;
	DescriptorEmbeddingServiceImpl descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl(); 

	
	public EmbeddingWebService2(String server, int port) {
		super(server, port);
		
		try {
			Unirest.config().followRedirects(true).socketTimeout(000).connectTimeout(000);//TMM
		} catch (Exception e) {
			// Ignore
		}

	}



	public DescriptorEmbedding generateGA_Embedding(String lanId,	CalculationInfoGA ci) {

		try {			
			ModelData md=ModelData.initModelData(ci, false);//TODO make it store it directly in ci?
			ci.tsv_training = md.trainingSetInstances;
			ci.tsv_prediction = md.predictionSetInstances;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Cant retrieve tsv for "+ci.datasetName);
			return null;
		}

		
		HttpResponse<String> response = find_GA_Embedding_API_Call(ci);
		System.out.println("calculation response status=" + response.getStatus());

		String data = response.getBody();
		System.out.println("calculation response data=" + data);

		Gson gson = new Gson();		
		CalculationResponse cr = gson.fromJson(data, CalculationResponse.class);

		String embedding = cr.embedding.stream().map(Object::toString).collect(Collectors.joining("\t"));

		DescriptorEmbedding desE = new DescriptorEmbedding(ci,embedding,lanId);

		Date date = new Date();
		Timestamp timestamp2 = new Timestamp(date.getTime());
		desE.setCreatedAt(timestamp2);
		desE.setUpdatedAt(timestamp2);

		//Store embedding in the database:
		DescriptorEmbeddingService deSer = new DescriptorEmbeddingServiceImpl();
		return deSer.create(desE);

	}
	
	
	
//	private static DescriptorEmbedding getEmbeddingImportance(CalculationInfo ci,
//			DescriptorEmbeddingServiceImpl descriptorEmbeddingService, EmbeddingWebService2 ews2) {
//		
//		
//		
////		System.out.println("\n***"+ci.datasetName+"\t"+ci.splittingName);
////		System.out.println(ciImportance.toString());
//		
//		DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ciImportance);		
//
//		if (descriptorEmbedding == null) {
//			System.out.println("Dont have existing embedding:"+ciImportance.toString());
//			descriptorEmbedding = ews2.generateImportanceEmbedding(lanId,ciImportance);
//			System.out.println("New embedding from web service:"+descriptorEmbedding.getEmbeddingTsv());
////			continue;
//		} else {
//			System.out.println("Have embedding from db:"+descriptorEmbedding.getEmbeddingTsv());
//		}
//
//		return descriptorEmbedding;
//		
//	}
	
	public DescriptorEmbedding getEmbeddingGA(CalculationInfo ci,String lanId) {
		
		CalculationInfoGA ciGA=new CalculationInfoGA(ci);
		
		ciGA.qsarMethodEmbedding=DevQsarConstants.KNN;
		
//		System.out.println("use_wards="+ciGA.use_wards);
		
		
//		if (datasetName.contains("BP") && !splitting.equals(SplittingGeneratorPFAS_Script.splittingPFASOnly))
//			ci.num_generations = 10;// takes too long to do 100
//		ci.num_jobs=4;

		System.out.println("\n***"+ci.datasetName+"\t"+ci.splittingName+"\t"+"num_generations="+ciGA.num_generations+"***");

		DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ciGA);
		
//		if (descriptorEmbedding==null) {//look for one of the ones made using offline python run:			
//		ci.num_jobs=2;//just takes slighter longer
//		ci.n_threads=16;//doesnt impact knn
//		descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);				
//	}			

		if (descriptorEmbedding == null) {
			System.out.println("Dont have existing embedding:"+ciGA.toString());
			descriptorEmbedding = generateGA_Embedding(lanId,ciGA);
			System.out.println("New embedding from web service:"+descriptorEmbedding.getEmbeddingTsv());
//			continue;
		} else {
			System.out.println("Have embedding from db:"+descriptorEmbedding.getEmbeddingTsv());
		}

		return descriptorEmbedding;
		
	}
	
	public DescriptorEmbedding generateImportanceEmbedding(CalculationInfo calculationInfo,String lanId,boolean checkDBForEmbedding, boolean storeInDB) {

		//Following sets min number of descriptors based on data set size, it has to load the data sets though which takes time
		
		
		CalculationInfoImportance ciImportance=new CalculationInfoImportance(calculationInfo);
		
		try {			
			ModelData md=ModelData.initModelData(ciImportance, false);//TODO make it store it directly in ci?
			ciImportance.tsv_training = md.trainingSetInstances;
			ciImportance.tsv_prediction = md.predictionSetInstances;
			
			int minDescriptors2=(int)Math.round(md.countTraining/ciImportance.datapoints_to_descriptors_ratio);
			
			if (minDescriptors2 < ciImportance.min_descriptor_count) {
				ciImportance.min_descriptor_count = minDescriptors2;//avoids having small models with way too many descriptors
			}			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Cant retrieve tsv for "+ciImportance.datasetName);
			return null;
		}
		
		
		if (checkDBForEmbedding) {
			DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ciImportance);		
			if (descriptorEmbedding != null) {
				System.out.println("Have embedding from db:"+descriptorEmbedding.getEmbeddingTsv());
				return descriptorEmbedding; 
			} 
		}
		
				
		System.out.println("Dont have embedding, creating a new one...");

		HttpResponse<String> response = find_Importance_Embedding_API_Call(ciImportance);
		System.out.println(response.getStatus());

		String data = response.getBody();
		
		Gson gson = new Gson();
		CalculationResponse cr = gson.fromJson(data, CalculationResponse.class);
		String embedding = cr.embedding.stream().map(Object::toString).collect(Collectors.joining("\t"));

		DescriptorEmbedding descriptorEmbedding = new DescriptorEmbedding(ciImportance, embedding,lanId);//TODO
		System.out.println("New embedding from web service:"+descriptorEmbedding.getEmbeddingTsv());
		
		
		if (storeInDB) {
			Date date = new Date();
			Timestamp timestamp2 = new Timestamp(date.getTime());
			descriptorEmbedding.setCreatedAt(timestamp2);
			DescriptorEmbeddingService deSer = new DescriptorEmbeddingServiceImpl();
			return deSer.create(descriptorEmbedding);
		} else {
			return descriptorEmbedding;
		}
		
	}
	
//	private static void runGA_Embedding(String propertyName, String datasetName, String lanId, String descriptorSetName,
//			String splittingName, Boolean removeLogDescriptors, EmbeddingWebService2 ews2) {
//		ModelData modelData = null;
//		try {
//			modelData = retrieveModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors,
//					lanId);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		String name = propertyName + "_" + descriptorSetName + "_" + System.currentTimeMillis();
//
//		CalculationInfoGA ci = new CalculationInfoGA();
//		ci.tsv_training = modelData.trainingSetInstances;
//		ci.tsv_prediction = modelData.predictionSetInstances;
//
//		ci.remove_log_p = propertyName.equals(DevQsarConstants.LOG_KOW);
//		ci.num_generations = 10;
//		ci.qsarMethodEmbedding = DevQsarConstants.KNN;
//		ci.datasetName = datasetName;
//		ci.descriptorSetName = descriptorSetName;
//
//		HttpResponse<String> response = ews2.findGA_Embedding(ci);
//		System.out.println(response.getStatus());
//
//		String data = response.getBody();
//		System.out.println(data);
//
//		Gson gson = new Gson();
//		CalculationResponse cr = gson.fromJson(data, CalculationResponse.class);
//		String embedding = cr.embedding.stream().map(Object::toString).collect(Collectors.joining("\t"));
//
//		DescriptorEmbedding desE = new DescriptorEmbedding(ci, embedding,lanId);
//
//		Date date = new Date();
//		Timestamp timestamp2 = new Timestamp(date.getTime());
//		desE.setCreatedAt(timestamp2);
//		desE.setUpdatedAt(timestamp2);
//
//		if (true) {
//			DescriptorEmbeddingService deSer = new DescriptorEmbeddingServiceImpl();
//			deSer.create(desE);
//		}
//
//	}


	public void getExistingEmbedding(String datasetName, String embeddingName, String embeddingQsarMethod) {
		DescriptorEmbeddingDaoImpl deai = new DescriptorEmbeddingDaoImpl();
		DescriptorEmbedding descriptorEmbedding = deai.findByName(embeddingName, null);

	}

//	public static String retrieveTrainingData(String datasetName, String descriptorSetName, 
//			String splittingName, Boolean removeLogDescriptors, String lanId) {
//		WebServiceModelBuilder wsmb = new WebServiceModelBuilder(null, lanId);
//		ModelData data = wsmb.initModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors, false);
//		return data.trainingSetInstances;
//	}
//	
//	public static String retrievePredictionData(String datasetName, String descriptorSetName, 
//			String splittingName, Boolean removeLogDescriptors, String lanId) {
//		WebServiceModelBuilder wsmb = new WebServiceModelBuilder(null, lanId);
//		ModelData data = wsmb.initModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors, false);
//		return data.predictionSetInstances;
//	}


	public HttpResponse<String> find_GA_Embedding_API_Call(CalculationInfoGA calculationInfo) {
		System.out.println(address+ "/models/" + calculationInfo.qsarMethodEmbedding +"/embedding");
		//		System.out.println(calculationInfo.tsv);

//		System.out.println("use_wards="+calculationInfo.use_wards);
		
		HttpResponse<String> response = Unirest.post(address+ "/models/{qsar_method}/embedding")
				.routeParam("qsar_method", calculationInfo.qsarMethodEmbedding)
				.field("training_tsv",calculationInfo.tsv_training)
				//				.field("save_to_database",calculationInfo.save_to_database)
				.field("remove_log_p",String.valueOf(calculationInfo.remove_log_p))
				.field("prediction_tsv", calculationInfo.tsv_prediction)
				.field("threshold", String.valueOf(calculationInfo.threshold))
				.field("num_optimizers",String.valueOf(calculationInfo.num_optimizers))
				.field("num_jobs",String.valueOf(calculationInfo.num_jobs))
				.field("n_threads",String.valueOf(calculationInfo.n_threads))
				.field("num_generations",String.valueOf(calculationInfo.num_generations))
				.field("max_length",String.valueOf(calculationInfo.max_length))
				.field("descriptor_coefficient",String.valueOf(calculationInfo.descriptor_coefficient))
				.field("use_wards",String.valueOf(calculationInfo.use_wards))							
				.asString();

		return response;
	}
	
	public HttpResponse<String> find_Importance_Embedding_API_Call(CalculationInfoImportance calculationInfo) {
//		System.out.println(address+ "/models/" + calculationInfo.qsarMethodEmbedding +"/embedding_importance");
				
		HttpResponse<String> response = Unirest.post(address+ "/models/{qsar_method}/embedding_importance")
				.routeParam("qsar_method", calculationInfo.qsarMethodEmbedding)
				.field("training_tsv",calculationInfo.tsv_training)
				.field("remove_log_p",String.valueOf(calculationInfo.remove_log_p))
				.field("prediction_tsv", calculationInfo.tsv_prediction)
				.field("num_generations",String.valueOf(calculationInfo.num_generations))
				.field("n_threads",String.valueOf(calculationInfo.n_threads))
				.field("use_permutative", String.valueOf(calculationInfo.use_permutative))
				.field("run_rfe", String.valueOf(calculationInfo.run_rfe))
				.field("fraction_of_max_importance",String.valueOf(calculationInfo.fraction_of_max_importance))
				.field("min_descriptor_count",String.valueOf(calculationInfo.min_descriptor_count))
				.field("max_descriptor_count",String.valueOf(calculationInfo.max_descriptor_count))
				.field("use_wards",String.valueOf(calculationInfo.use_wards))
				.asString();

		return response;
	}

	public static void main(String[] args) {
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

		String lanId = "cramslan";
		String descriptorSetName="T.E.S.T. 5.1";
		String splittingName="OPERA";
		Boolean removeLogDescriptors=false;
		String propertyName = DevQsarConstants.LOG_HALF_LIFE;
		int num_generations = 10;
/*
		ews2.generateEmbedding(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING,
				propertyName, lanId, descriptorSetName, splittingName,
				removeLogDescriptors, num_generations);
*/
	}


}
