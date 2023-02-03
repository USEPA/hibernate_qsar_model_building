package gov.epa.endpoints.models;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.validation.ConstraintViolationException;

import com.google.gson.Gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.epa.databases.dev_qsar.DevQsarConstants;

import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;
import gov.epa.databases.dev_qsar.qsar_models.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingService;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.SplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticServiceImpl;

import gov.epa.web_services.ModelWebService;
import gov.epa.web_services.embedding_service.CalculationInfo;

public class WebServiceModelBuilder extends ModelBuilder {
	
	private DescriptorEmbeddingService descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();
	private ModelBytesService modelBytesService = new ModelBytesServiceImpl();
	
	StatisticServiceImpl statisticService=new StatisticServiceImpl();
	ModelStatisticServiceImpl modelStatisticService=new ModelStatisticServiceImpl();
	SplittingServiceImpl splittingService=new SplittingServiceImpl();

	
	protected ModelWebService modelWebService;
	
	public WebServiceModelBuilder(ModelWebService modelWebService, String lanId) {
		super(lanId);
		this.modelWebService = modelWebService;
	}

	public void listDescriptors(Long modelId) {
		if (modelId==null) {
//			logger.error("Model with supplied parameters has not been built");
			return;
		}
		
		ModelBytes modelBytes = modelBytesService.findByModelId(modelId);
		if (modelBytes==null) {
			return;
		}
		
		byte[] bytes = modelBytes.getBytes();
		
		Model model = modelBytes.getModel();
		String fullMethodName = model.getMethod().getName();
		String methodName = fullMethodName.substring(0, fullMethodName.indexOf("_"));
		
		String strModelId = String.valueOf(modelId);
		String descriptors = modelWebService.callDescriptors(bytes, methodName, strModelId).getBody();
		
		String fileName = "data/descriptors/" + model.getDatasetName()
			+ "_" + model.getDescriptorSetName()
			+ "_" + model.getSplittingName()
			+ "_" + methodName + "_" + modelId + "_descriptors.txt";
		File file = new File(fileName);
		file.getParentFile().mkdirs();
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			bw.write(descriptors);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Builds a Python model with the given data and parameters
	 * @param data
	 * @param params
	 */
	@SuppressWarnings("deprecation")
	public Long train(ModelData data, String methodName) throws ConstraintViolationException {
		return trainWithPreselectedDescriptors(data, methodName, null);
	}

	Method getDetails(String methodName,Model model,JsonObject joDetails) {
		
		String version = joDetails.get("version").getAsString();
		Boolean isBinary = joDetails.get("is_binary").getAsBoolean();				
		String classOrRegr = isBinary ? "classifier" : "regressor";				
		String fullMethodName = methodName + "_" + classOrRegr + "_" + version;		
				
		String description=joDetails.get("description").getAsString();
		String description_url=joDetails.get("description_url").getAsString();
		
		Gson gson2 = new Gson();		
		String hyperparameters=gson2.toJson(joDetails.get("hyperparameter_grid").getAsJsonObject());
				
		Method method = methodService.findByName(fullMethodName);
		if (method==null) {
			method = new Method(fullMethodName, description, description_url,hyperparameters, isBinary, lanId);
			methodService.create(method);
		} else {
//			JsonParser parser = new JsonParser();
//			if (!parser.parse(hyperparameters).equals(parser.parse(method.getHyperparameters()))) {
//				System.out.println("Hyperparameters for " + fullMethodName + " have changed");
//			}
		}
		return method;

	}
	
	

	void createCV_Statistics(JsonObject jo,Model model) {

		JsonObject joTrainingStats = jo.get("training_stats").getAsJsonObject();		

		double R2_CV_Training=joTrainingStats.get("training_cv_r2").getAsDouble();
		System.out.println("storing R2_CV_Training="+R2_CV_Training);
		Statistic statistic=statisticService.findByName("R2_CV_Training");					
		ModelStatistic modelStatistic=new ModelStatistic(statistic, model, R2_CV_Training, lanId);
		modelStatistic=modelStatisticService.create(modelStatistic);

		double Q2_CV_Training=joTrainingStats.get("training_cv_q2").getAsDouble();
		System.out.println("storing Q2_CV_Training="+Q2_CV_Training);
		statistic=statisticService.findByName("Q2_CV_Training");					
		modelStatistic=new ModelStatistic(statistic, model, Q2_CV_Training, lanId);
		modelStatistic=modelStatisticService.create(modelStatistic);
		
		JsonArray jaPreds=joTrainingStats.get("training_cv_predictions").getAsJsonArray();
		
		ModelPrediction[] mps = gson.fromJson(jaPreds, ModelPrediction[].class);
				
//		for (CrossValidationPrediction cvp:cvps) {
//			System.out.println(cvp.id+"\t"+cvp.exp+"\t"+cvp.pred+"\t"+cvp.split);
//		}

		Splitting splitting=splittingService.findByName(DevQsarConstants.SPLITTING_TRAINING_CROSS_VALIDATION);
		postPredictions(Arrays.asList(mps), model,splitting);	//use split+10 because split 1 is already taken by test set predictions

	}
	
	/**
	 * Builds a Python and uses SQLalchemy to store
	 * @param data
	 * @param params
	 */
	@SuppressWarnings("deprecation")
	public Long trainWithPythonStorage(ModelData data, String methodName, String descriptorEmbeddingName) 
			throws ConstraintViolationException {
		if (data.trainingSetInstances==null) {
//			logger.error("Dataset instances were not initialized");
			System.out.println("Dataset instances were not initialized");
			return null;
		}
		
		System.out.println("Building Python model with dataset = " + data.datasetName + ", descriptors = " + data.descriptorSetName
				+ ", splitting = " + data.splittingName + " using QSAR method = " + methodName);
		DescriptorEmbedding descriptorEmbedding = null;
		if (descriptorEmbeddingName!= null) {
			descriptorEmbedding = descriptorEmbeddingService.findByName(descriptorEmbeddingName);
			if (descriptorEmbedding==null) {
			System.out.println("No such descriptor embedding");
			return null;
			} 
			else if (!descriptorEmbedding.getDescriptorSetName().equals(data.descriptorSetName)) {
			System.out.println("Descriptor embedding for wrong descriptor set");
			return null;
			}
			// CR: why impose this? if it's the right property I don't see a problem 
			/*
			else if (!descriptorEmbedding.getDatasetName().equals(data.datasetName)) {
			System.out.println("Descriptor embedding for wrong dataset");
			return null;
			} */
		}
		
		
		Method genericMethod = methodService.findByName(methodName);
		if (genericMethod==null) {
			genericMethod = new Method(methodName, methodName, null,null, false, lanId);
			methodService.create(genericMethod);
		}
		
		Model model = new Model(genericMethod, descriptorEmbedding, data.descriptorSetName, data.datasetName, data.splittingName, lanId);
		modelService.create(model);
		String hyperparameters = null;
		String strModelId = String.valueOf(model.getId());
		System.out.println("the model id is:" + strModelId);
		if (descriptorEmbedding != null) {
			modelWebService.callTrainWithPreselectedDescriptorsPythonStorage(data.trainingSetInstances, 
				data.removeLogP_Descriptors, methodName, strModelId, descriptorEmbedding.getEmbeddingTsv()).getBody();
			hyperparameters = modelWebService.callDetails(methodName, strModelId).getBody();
		} else {
			modelWebService.callTrainPythonStorage(data.trainingSetInstances, 
				data.removeLogP_Descriptors, methodName, strModelId);
			hyperparameters = modelWebService.callDetails(methodName, strModelId).getBody();
		}
		String description = modelWebService.callInfo(methodName).getBody();
		//TODO get description_url
		String description_url=null;
		
		JsonObject jo = gson.fromJson(hyperparameters, JsonObject.class);
		String version = jo.get("version").getAsString();
		Boolean isBinary = jo.get("is_binary").getAsBoolean();
		String classOrRegr = isBinary ? "classifier" : "regressor";
		String fullMethodName = methodName + "_" + classOrRegr + "_" + version;
		
		Method method = methodService.findByName(fullMethodName);
		if (method==null) {
			method = new Method(fullMethodName, description, description_url,hyperparameters, isBinary, lanId);
			methodService.create(method);
		} else {
			JsonParser parser = new JsonParser();
			if (!parser.parse(hyperparameters).equals(parser.parse(method.getHyperparameters()))) {
//				logger.warn("Hyperparameters for " + fullMethodName + " have changed");
			}
		}
		
		model.setMethod(method);
		modelService.update(model);
		
		return model.getId();
	}

	/**
	 * Builds a Python model with the given data and parameters
	 * @param data
	 * @param params
	 */
	@SuppressWarnings("deprecation")
	public Long trainWithPreselectedDescriptorsByEmbeddingName(ModelData data, String methodName, String descriptorEmbeddingName) 
			throws ConstraintViolationException {
		
		DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByName(descriptorEmbeddingName);
		
		if (descriptorEmbedding==null) {
			System.out.println("Embedding is null for "+descriptorEmbeddingName);
			return null;
		} else if (!descriptorEmbedding.getDescriptorSetName().equals(data.descriptorSetName)) {
			System.out.println("Descriptor embedding for wrong descriptor set");
			return null;
		} else if (!descriptorEmbedding.getDatasetName().equals(data.datasetName)) {
			System.out.println("Descriptor embedding for wrong dataset");
			return null;
		}
				
		return trainWithPreselectedDescriptors(data, methodName, descriptorEmbedding);
	}
	
	
	/**
	 * Builds a Python model with the given data and parameters
	 * @param data
	 * @param params
	 */
	
	public Long trainWithPreselectedDescriptors(ModelData data, String methodName, DescriptorEmbedding descriptorEmbedding) 
			throws ConstraintViolationException {

		if (data.trainingSetInstances==null) {
//			logger.error("Dataset instances were not initialized");
			System.out.println("Dataset instances were not initialized");

			return null;
		}
		
		System.out.println("Building Python model with dataset = " + data.datasetName + ", descriptors = " + data.descriptorSetName
				+ ", splitting = " + data.splittingName + " using QSAR method = " + methodName);
		
		if (descriptorEmbedding!=null) {
			System.out.println("Embedding "+descriptorEmbedding.getName()+":\t"+descriptorEmbedding.getEmbeddingTsv());	
		} else {
			System.out.println("No embedding used");
		}
		
		
		Method genericMethod = methodService.findByName(methodName);
		if (genericMethod==null) {
			genericMethod = new Method(methodName, methodName, null,null, false, lanId);
			methodService.create(genericMethod);
		}
		
		Model model = new Model(genericMethod, descriptorEmbedding, data.descriptorSetName, data.datasetName, data.splittingName, lanId);
		modelService.create(model);
		
		String strModelId = String.valueOf(model.getId());
		
		System.out.println("strModelID="+strModelId);
		
		byte[] bytes=null;
		
		if (descriptorEmbedding!=null) {		
			bytes = modelWebService.callTrainWithPreselectedDescriptors(data.trainingSetInstances, data.removeLogP_Descriptors, methodName, strModelId, descriptorEmbedding.getEmbeddingTsv()).getBody();
		} else {
			bytes = modelWebService.callTrain(data.trainingSetInstances,data.removeLogP_Descriptors, methodName, strModelId).getBody();
		}

//		String description = modelWebService.callInfo(methodName).getBody();//can get from details call instead

		String details = modelWebService.callDetails(methodName, model.getId()+"").getBody();			
		JsonObject joDetails = gson.fromJson(details, JsonObject.class);
		
		Method method=getDetails(methodName, model,joDetails);

		createCV_Statistics(joDetails, model);//Add stats

		Gson gson2=new Gson();//no pretty printing
		String hyperparameters=gson2.toJson(joDetails.get("hyperparameters").getAsJsonObject());
		
		model.setMethod(method);
		model.setHyperparameters(hyperparameters);		
		modelService.update(model);
		
		ModelBytes modelBytes = new ModelBytes(model, bytes, lanId);
		modelBytesService.create(modelBytes);
		
		return model.getId();
	}
	/**
	 * Validates a Python model with the given data and parameters
	 * @param data
	 * @param params
	 */
	public void predictPythonStorage(ModelData data, String methodName, Long modelId) throws ConstraintViolationException {
		if (modelId==null) {
//			logger.error("Model with supplied parameters has not been built");
			System.out.println("Model with supplied parameters has not been built");
			return;
		} else if (data.predictionSetInstances==null) {
//			logger.error("Dataset instances were not initialized");
			System.out.println("Dataset instances were not initialized");
			return;
		}
		
//		logger.debug("Validating Python model with dataset = " + data.datasetName + ", descriptors = " + data.descriptorSetName
//				+ ", splitting = " + data.splittingName + " using QSAR method = " + methodName 
//				+ " (qsar_models ID = " + modelId + ")");
		
		ModelBytes modelBytes = modelBytesService.findByModelId(modelId);
		Model model = modelBytes.getModel();
		
		String strModelId = String.valueOf(modelId);

		Splitting splitting=splittingService.findByName(DevQsarConstants.SPLITTING_RND_REPRESENTATIVE);

		String predictResponse = modelWebService.callPredictSQLAlchemy(data.predictionSetInstances, methodName, strModelId).getBody();		
		ModelPrediction[] modelPredictions = gson.fromJson(predictResponse, ModelPrediction[].class);				
		for(ModelPrediction mp:modelPredictions)mp.split=DevQsarConstants.TEST_SPLIT_NUM;
		postPredictions(Arrays.asList(modelPredictions), model,splitting);
		
		if (data.trainingSetInstances==null) {
			System.out.println("Dataset instances were not initialized");
		}

		String predictTrainingResponse = modelWebService.callPredictSQLAlchemy(data.trainingSetInstances, methodName, strModelId).getBody();
		ModelPrediction[] modelTrainingPredictions = gson.fromJson(predictTrainingResponse, ModelPrediction[].class);	
		for(ModelPrediction mp:modelTrainingPredictions)mp.split=DevQsarConstants.TRAIN_SPLIT_NUM;
		postPredictions(Arrays.asList(modelTrainingPredictions), model,splitting);
		
		calculateAndPostModelStatistics(Arrays.asList(modelTrainingPredictions), Arrays.asList(modelPredictions), model);

	}

	/**
	 * Validates a Python model with the given data and parameters
	 * @param data
	 * @param params
	 */
	public void predict(ModelData data, String methodName, Long modelId) throws ConstraintViolationException {
		if (modelId==null) {
//			logger.error("Model with supplied parameters has not been built");
			return;
		} else if (data.predictionSetInstances==null) {
//			logger.error("Dataset instances were not initialized");
			return;
		}
		
//		logger.debug("Validating Python model with dataset = " + data.datasetName + ", descriptors = " + data.descriptorSetName
//				+ ", splitting = " + data.splittingName + " using QSAR method = " + methodName 
//				+ " (qsar_models ID = " + modelId + ")");
		
		ModelBytes modelBytes = modelBytesService.findByModelId(modelId);
		Model model = modelBytes.getModel();
		byte[] bytes = modelBytes.getBytes();
		
		String strModelId = String.valueOf(modelId);
		modelWebService.callInit(bytes, methodName, strModelId).getBody();
		
		Splitting splitting=splittingService.findByName(DevQsarConstants.SPLITTING_RND_REPRESENTATIVE);
				
		String predictResponse = modelWebService.callPredict(data.predictionSetInstances, methodName, strModelId).getBody();
		ModelPrediction[] modelPredictionsArray = gson.fromJson(predictResponse, ModelPrediction[].class);
		List<ModelPrediction>modelTestPredictions=Arrays.asList(modelPredictionsArray);			
		for(ModelPrediction mp:modelTestPredictions)mp.split=DevQsarConstants.TEST_SPLIT_NUM;		
		postPredictions(modelTestPredictions, model,splitting);		
		
		List<ModelPrediction> modelTrainingPredictions = predictTraining(model, data, methodName, strModelId);
		for(ModelPrediction mp:modelTrainingPredictions) mp.split=DevQsarConstants.TRAIN_SPLIT_NUM;
		postPredictions(modelTrainingPredictions, model,splitting);
		
		calculateAndPostModelStatistics(modelTrainingPredictions, modelTestPredictions, model);
	}

	private List<ModelPrediction> predictTraining(Model model, ModelData data, String methodName, String strModelId) {
		if (data.trainingSetInstances==null) {
//			logger.error("Dataset instances were not initialized");
			return null;
		}
		
		String predictTrainingResponse = modelWebService.callPredict(data.trainingSetInstances, methodName, strModelId).getBody();
		ModelPrediction[] modelTrainingPredictions = gson.fromJson(predictTrainingResponse, ModelPrediction[].class);

		return Arrays.asList(modelTrainingPredictions);
	}

	public Long build(String datasetName, String descriptorSetName, String splittingName, boolean removeLogP_Descriptors,
			String methodName) throws ConstraintViolationException {
		ModelData data = ModelData.initModelData(datasetName, descriptorSetName, splittingName, removeLogP_Descriptors,false);
		
		Long modelId = train(data, methodName);
		predict(data, methodName, modelId);
		
		return modelId;
	}
	
	public Long buildWithPythonStorage(String datasetName, String descriptorSetName, String splittingName, 
			boolean removeLogDescriptors, String methodName, String descriptorEmbeddingName) throws ConstraintViolationException {
		ModelData data = ModelData.initModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors,false);
		
		Long modelId = trainWithPythonStorage(data, methodName, descriptorEmbeddingName);
		predictPythonStorage(data, methodName, modelId);
		
		return modelId;
	}


	public Long buildWithPreselectedDescriptors(String datasetName, String descriptorSetName, String splittingName, 
			boolean removeLogDescriptors, String methodName, String descriptorEmbeddingName) throws ConstraintViolationException {
		ModelData data =ModelData.initModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors,false);
		
		Long modelId = trainWithPreselectedDescriptorsByEmbeddingName(data, methodName, descriptorEmbeddingName);
		predict(data, methodName, modelId);
		
		return modelId;
	}
	
	
	public Long buildWithPreselectedDescriptors(String methodName,CalculationInfo ci, DescriptorEmbedding descriptorEmbedding) throws ConstraintViolationException {
		ModelData data = ModelData.initModelData(ci.datasetName, ci.descriptorSetName, ci.splittingName, ci.remove_log_p,false);
		
		Long modelId = trainWithPreselectedDescriptors(data, methodName, descriptorEmbedding);
		predict(data, methodName, modelId);
		
		return modelId;
	}


	public ModelPrediction[] rerunExistingModelPredictions(Long modelId) {
		ModelBytes modelBytes = modelBytesService.findByModelId(modelId);
		Model model = modelBytes.getModel();
		
		ModelData data = ModelData.initModelData(model.getDatasetName(), model.getDescriptorSetName(), model.getSplittingName(), false,false);
		
		byte[] bytes = modelBytes.getBytes();
		String methodName = model.getMethod().getName();
		String pythonMethodName = methodName.substring(0, methodName.indexOf("_"));
		String strModelId = String.valueOf(modelId);
		
		modelWebService.callInit(bytes, pythonMethodName, strModelId).getBody();
		String predictResponse = modelWebService.callPredict(data.predictionSetInstances, pythonMethodName, strModelId).getBody();
		ModelPrediction[] modelPredictions = gson.fromJson(predictResponse, ModelPrediction[].class);
		
		return modelPredictions;
	}

	public long build(String methodName, CalculationInfo ci){
		
		ModelData data = ModelData.initModelData(ci,false);
		
		Long modelId = train(data, methodName);
		predict(data, methodName, modelId);
		
		return modelId;
	}

}
