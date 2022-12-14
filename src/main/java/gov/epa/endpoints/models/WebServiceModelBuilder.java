package gov.epa.endpoints.models;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.validation.ConstraintViolationException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingService;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesServiceImpl;
import gov.epa.web_services.ModelWebService;

public class WebServiceModelBuilder extends ModelBuilder {
	
	private DescriptorEmbeddingService descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();
	private ModelBytesService modelBytesService = new ModelBytesServiceImpl();
	
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
		if (data==null || data.trainingSetInstances==null) {
//			logger.error("Dataset instances were not initialized");
			return null;
		}
		
//		logger.debug("Building Python model with dataset = " + data.datasetName + ", descriptors = " + data.descriptorSetName
//				+ ", splitting = " + data.splittingName + " using QSAR method = " + methodName);
		
		Method genericMethod = methodService.findByName(methodName);
		if (genericMethod==null) {
			genericMethod = new Method(methodName, methodName, null, false, lanId);
			methodService.create(genericMethod);
		}
		
		Model model = new Model(genericMethod, data.descriptorSetName, data.datasetName, data.splittingName, lanId);
		modelService.create(model);
		
		String strModelId = String.valueOf(model.getId());
		byte[] bytes = modelWebService.callTrain(data.trainingSetInstances, 
				data.removeLogDescriptors, methodName, strModelId).getBody();
		String hyperparameters = modelWebService.callDetails(methodName, strModelId).getBody();
		String description = modelWebService.callInfo(methodName).getBody();
		
		JsonObject jo = gson.fromJson(hyperparameters, JsonObject.class);
		String version = jo.get("version").getAsString();
		Boolean isBinary = jo.get("is_binary").getAsBoolean();
		String classOrRegr = isBinary ? "classifier" : "regressor";
		String fullMethodName = methodName + "_" + classOrRegr + "_" + version;
		
		Method method = methodService.findByName(fullMethodName);
		if (method==null) {
			method = new Method(fullMethodName, description, hyperparameters, isBinary, lanId);
			methodService.create(method);
		} else {
			JsonParser parser = new JsonParser();
			if (!parser.parse(hyperparameters).equals(parser.parse(method.getHyperparameters()))) {
//				logger.warn("Hyperparameters for " + fullMethodName + " have changed");
			}
		}
		
		model.setMethod(method);
		modelService.update(model);
		
		ModelBytes modelBytes = new ModelBytes(model, bytes, lanId);
		modelBytesService.create(modelBytes);
		
		return model.getId();
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
			genericMethod = new Method(methodName, methodName, null, false, lanId);
			methodService.create(genericMethod);
		}
		
		Model model = new Model(genericMethod, descriptorEmbedding, data.descriptorSetName, data.datasetName, data.splittingName, lanId);
		modelService.create(model);
		String hyperparameters = null;
		String strModelId = String.valueOf(model.getId());
		System.out.println("the model id is:" + strModelId);
		if (descriptorEmbedding != null) {
			modelWebService.callTrainWithPreselectedDescriptorsPythonStorage(data.trainingSetInstances, 
				data.removeLogDescriptors, methodName, strModelId, descriptorEmbedding.getEmbeddingTsv()).getBody();
			hyperparameters = modelWebService.callDetails(methodName, strModelId).getBody();
		} else {
			modelWebService.callTrainPythonStorage(data.trainingSetInstances, 
				data.removeLogDescriptors, methodName, strModelId);
			hyperparameters = modelWebService.callDetails(methodName, strModelId).getBody();
		}
		String description = modelWebService.callInfo(methodName).getBody();
		
		JsonObject jo = gson.fromJson(hyperparameters, JsonObject.class);
		String version = jo.get("version").getAsString();
		Boolean isBinary = jo.get("is_binary").getAsBoolean();
		String classOrRegr = isBinary ? "classifier" : "regressor";
		String fullMethodName = methodName + "_" + classOrRegr + "_" + version;
		
		Method method = methodService.findByName(fullMethodName);
		if (method==null) {
			method = new Method(fullMethodName, description, hyperparameters, isBinary, lanId);
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
	public Long trainWithPreselectedDescriptors(ModelData data, String methodName, String descriptorEmbeddingName) 
			throws ConstraintViolationException {
		System.out.println("descriptor embedding name = " + descriptorEmbeddingName);
		if (data.trainingSetInstances==null) {
//			logger.error("Dataset instances were not initialized");
			System.out.println("Dataset instances were not initialized");

			return null;
		}
		
		System.out.println("Building Python model with dataset = " + data.datasetName + ", descriptors = " + data.descriptorSetName
				+ ", splitting = " + data.splittingName + " using QSAR method = " + methodName);
		
		DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByName(descriptorEmbeddingName);
		if (descriptorEmbedding==null) {
			return null;
		} else if (!descriptorEmbedding.getDescriptorSetName().equals(data.descriptorSetName)) {
//			logger.error("Descriptor embedding for wrong descriptor set");
			return null;
		} else if (!descriptorEmbedding.getDatasetName().equals(data.datasetName)) {
//			logger.error("Descriptor embedding for wrong dataset");
			return null;
		}
		System.out.println("embedding tsv =" + descriptorEmbedding.getEmbeddingTsv());

		Method genericMethod = methodService.findByName(methodName);
		if (genericMethod==null) {
			genericMethod = new Method(methodName, methodName, null, false, lanId);
			methodService.create(genericMethod);
		}
		
		Model model = new Model(genericMethod, descriptorEmbedding, data.descriptorSetName, data.datasetName, data.splittingName, lanId);
		modelService.create(model);
		
		String strModelId = String.valueOf(model.getId());
		byte[] bytes = modelWebService.callTrainWithPreselectedDescriptors(data.trainingSetInstances, 
				data.removeLogDescriptors, methodName, strModelId, descriptorEmbedding.getEmbeddingTsv()).getBody();
		String hyperparameters = modelWebService.callDetails(methodName, strModelId).getBody();
		String description = modelWebService.callInfo(methodName).getBody();
		
		JsonObject jo = gson.fromJson(hyperparameters, JsonObject.class);
		String version = jo.get("version").getAsString();
		Boolean isBinary = jo.get("is_binary").getAsBoolean();
		String classOrRegr = isBinary ? "classifier" : "regressor";
		String fullMethodName = methodName + "_" + classOrRegr + "_" + version;
		
		Method method = methodService.findByName(fullMethodName);
		if (method==null) {
			method = new Method(fullMethodName, description, hyperparameters, isBinary, lanId);
			methodService.create(method);
		} else {
			JsonParser parser = new JsonParser();
			if (!parser.parse(hyperparameters).equals(parser.parse(method.getHyperparameters()))) {
//				logger.warn("Hyperparameters for " + fullMethodName + " have changed");
			}
		}
		
		model.setMethod(method);
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
		
		String predictResponse = modelWebService.callPredictSQLAlchemy(data.predictionSetInstances, methodName, strModelId).getBody();
		ModelPrediction[] modelPredictions = gson.fromJson(predictResponse, ModelPrediction[].class);
		postPredictions(Arrays.asList(modelPredictions), model);
		
		if (data.trainingSetInstances==null) {
			System.out.println("Dataset instances were not initialized");
		}
		
		String predictTrainingResponse = modelWebService.callPredictSQLAlchemy(data.trainingSetInstances, methodName, strModelId).getBody();
		ModelPrediction[] modelTrainingPredictionsArray = gson.fromJson(predictTrainingResponse, ModelPrediction[].class);

		List<ModelPrediction> modelTrainingPredictions = Arrays.asList(modelTrainingPredictionsArray);
		
		
		postPredictions(modelTrainingPredictions, model);
		
		calculateAndPostModelStatistics(modelTrainingPredictions, Arrays.asList(modelPredictions), model);
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
		
		String predictResponse = modelWebService.callPredict(data.predictionSetInstances, methodName, strModelId).getBody();
		ModelPrediction[] modelPredictions = gson.fromJson(predictResponse, ModelPrediction[].class);
		postPredictions(Arrays.asList(modelPredictions), model);
		
		List<ModelPrediction> modelTrainingPredictions = predictTraining(model, data, methodName, strModelId);
		postPredictions(modelTrainingPredictions, model);
		
		calculateAndPostModelStatistics(modelTrainingPredictions, Arrays.asList(modelPredictions), model);
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

	public Long build(String datasetName, String descriptorSetName, String splittingName, boolean removeLogDescriptors,
			String methodName) throws ConstraintViolationException {
		ModelData data = initModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors);
		
		Long modelId = train(data, methodName);
		predict(data, methodName, modelId);
		
		return modelId;
	}
	
	public Long buildWithPythonStorage(String datasetName, String descriptorSetName, String splittingName, 
			boolean removeLogDescriptors, String methodName, String descriptorEmbeddingName) throws ConstraintViolationException {
		ModelData data = initModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors);
		
		Long modelId = trainWithPythonStorage(data, methodName, descriptorEmbeddingName);
		predictPythonStorage(data, methodName, modelId);
		
		return modelId;
	}


	public Long buildWithPreselectedDescriptors(String datasetName, String descriptorSetName, String splittingName, 
			boolean removeLogDescriptors, String methodName, String descriptorEmbeddingName) throws ConstraintViolationException {
		ModelData data = initModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors);
		
		Long modelId = trainWithPreselectedDescriptors(data, methodName, descriptorEmbeddingName);
		predict(data, methodName, modelId);
		
		return modelId;
	}

	public ModelPrediction[] rerunExistingModelPredictions(Long modelId) {
		ModelBytes modelBytes = modelBytesService.findByModelId(modelId);
		Model model = modelBytes.getModel();
		
		ModelData data = initModelData(model.getDatasetName(), model.getDescriptorSetName(), model.getSplittingName(), false);
		
		byte[] bytes = modelBytes.getBytes();
		String methodName = model.getMethod().getName();
		String pythonMethodName = methodName.substring(0, methodName.indexOf("_"));
		String strModelId = String.valueOf(modelId);
		
		modelWebService.callInit(bytes, pythonMethodName, strModelId).getBody();
		String predictResponse = modelWebService.callPredict(data.predictionSetInstances, pythonMethodName, strModelId).getBody();
		ModelPrediction[] modelPredictions = gson.fromJson(predictResponse, ModelPrediction[].class);
		
		return modelPredictions;
	}

}
