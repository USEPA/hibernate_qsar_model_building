package gov.epa.endpoints.models;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;
import gov.epa.databases.dev_qsar.qsar_models.service.MethodService;
import gov.epa.databases.dev_qsar.qsar_models.service.MethodServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionService;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticServiceImpl;
import gov.epa.web_services.ModelWebService;
import kong.unirest.Unirest;

public class ModelBuilder {
	private DataPointInSplittingService dataPointInSplittingService;
	private DescriptorValuesService descriptorValuesService;
	private StatisticService statisticService;
	private MethodService methodService;
	private ModelBytesService modelBytesService;
	private ModelService modelService;
	private ModelStatisticService modelStatisticService;
	private PredictionService predictionService;
	private ModelWebService modelWebService;
	private String lanId;
	
	private static Logger logger = LogManager.getLogger(ModelBuilder.class);
	private static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static Random rand = new Random();
	
	public ModelBuilder(ModelWebService modelWebService, String lanId) {
		// Set logging providers for Hibernate and MChange
		System.setProperty("org.jboss.logging.provider", "log4j");
		System.setProperty("com.mchange.v2.log.MLog", "log4j");
		
		// Reduce logging output from Apache, Hibernate, and C3P0
		String[] loggerNames = {"org.apache.http", "org.hibernate", "com.mchange"};
		for (String loggerName:loggerNames) {
			Logger thisLogger = LogManager.getLogger(loggerName);
			thisLogger.setLevel(Level.WARN);
		}
		
		// Make sure Unirest is configured
		try {
			Unirest.config().followRedirects(true).socketTimeout(000).connectTimeout(000);
		} catch (Exception e) {
			logger.debug("Unirest already configured, ignoring");
		}
		
		// Provide the model building web service and user ID
		this.modelWebService = modelWebService;
		this.lanId = lanId;
		
		// Open database connections
		dataPointInSplittingService = new DataPointInSplittingServiceImpl();
		descriptorValuesService = new DescriptorValuesServiceImpl();
		statisticService = new StatisticServiceImpl();
		methodService = new MethodServiceImpl();
		modelBytesService = new ModelBytesServiceImpl();
		modelService = new ModelServiceImpl();
		modelStatisticService = new ModelStatisticServiceImpl();
		predictionService = new PredictionServiceImpl();
	}
	
	public ModelData initModelData(String datasetName, String descriptorSetName, String splittingName, boolean removeLogDescriptors) {
		List<DataPointInSplitting> dataPointsInSplitting = 
				dataPointInSplittingService.findByDatasetNameAndSplittingName(datasetName, splittingName);

		if (dataPointsInSplitting.size()==0) {
			logger.error("Splitting " + splittingName + " not available for dataset " + datasetName);
			return null;
		}
		
		List<DescriptorValues> descriptorValues = descriptorValuesService.findByDescriptorSetName(descriptorSetName);
		
		if (descriptorValues.size()==0) {
			logger.error("Descriptor set " + descriptorSetName + " not available");
			return null;
		}
		
		ModelData data = new ModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors);
		data.initInstances(dataPointsInSplitting, descriptorValues);
		
		return data;
	}
	
	/**
	 * Adds a set of model statistics to the qsar_models database
	 * @param modelStatisticValues	a map of statistic names to calculated values
	 * @param model					the model the statistics were calculated before (TODO: this is inelegant--how to fix it?)
	 */
	private void postModelStatistics(Map<String, Double> modelStatisticValues, Model model) {
		for (String statisticName:modelStatisticValues.keySet()) {
			Statistic statistic = statisticService.findByName(statisticName);
			ModelStatistic modelStatistic = new ModelStatistic(statistic, model, modelStatisticValues.get(statisticName), lanId);
			modelStatisticService.create(modelStatistic);
		}
	}
	
	/**
	 * Builds a Python model with the given data and parameters
	 * @param data
	 * @param params
	 */
	public Long train(ModelData data, String methodName) {
		if (data.trainingSetInstances==null) {
			logger.error("Dataset instances were not initialized");
			return null;
		}
		
		logger.debug("Building Python model with dataset = " + data.datasetName + ", descriptors = " + data.descriptorSetName
				+ ", splitting = " + data.splittingName + " using QSAR method = " + methodName);
		
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
				logger.warn("Hyperparameters for " + fullMethodName + " have changed");
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
	public void predict(ModelData data, String methodName, Long modelId) {
		if (modelId==null) {
			logger.error("Model with supplied parameters has not been built");
			return;
		} else if (data.predictionSetInstances==null) {
			logger.error("Dataset instances were not initialized");
			return;
		}
		
		logger.debug("Validating Python model with dataset = " + data.datasetName + ", descriptors = " + data.descriptorSetName
				+ ", splitting = " + data.splittingName + " using QSAR method = " + methodName 
				+ " (qsar_models ID = " + modelId + ")");
		
		ModelBytes modelBytes = modelBytesService.findByModelId(modelId);
		Model model = modelBytes.getModel();
		byte[] bytes = modelBytes.getBytes();
		
		String strModelId = String.valueOf(modelId);
		modelWebService.callInit(bytes, methodName, strModelId).getBody();
		
		String predictResponse = modelWebService.callPredict(data.predictionSetInstances, methodName, strModelId).getBody();
		ModelPrediction[] modelPredictions = gson.fromJson(predictResponse, ModelPrediction[].class);
		
		for (ModelPrediction pythonModelResponse:modelPredictions) {
			Prediction prediction = new Prediction(pythonModelResponse.ID, model, pythonModelResponse.pred, lanId);
			predictionService.create(prediction);
		}
		
		predictTraining(model, data, methodName, strModelId);
		
		Map<String, Double> modelStatisticValues = null;
		if (model.getMethod().getIsBinary()) {
			modelStatisticValues = 
					ModelStatisticCalculator.calculateBinaryStatistics(Arrays.asList(modelPredictions), DevQsarConstants.BINARY_CUTOFF);
		} else {
			modelStatisticValues = 
					ModelStatisticCalculator.calculateContinuousStatistics(Arrays.asList(modelPredictions), data.meanExpTraining);
		}
		postModelStatistics(modelStatisticValues, model);
	}
	
	private void predictTraining(Model model, ModelData data, String methodName, String strModelId) {
		if (data.trainingSetInstances==null) {
			logger.error("Dataset instances were not initialized");
			return;
		}
		
		String predictTrainingResponse = modelWebService.callPredict(data.trainingSetInstances, methodName, strModelId).getBody();
		ModelPrediction[] modelTrainingPredictions = gson.fromJson(predictTrainingResponse, ModelPrediction[].class);
		
		for (ModelPrediction pythonModelResponse:modelTrainingPredictions) {
			Prediction prediction = new Prediction(pythonModelResponse.ID, model, pythonModelResponse.pred, lanId);
			predictionService.create(prediction);
		}
	}

	public Long build(String datasetName, String descriptorSetName, String splittingName, boolean removeLogDescriptors,
			String methodName) {
		ModelData data = initModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors);
		
		Long modelId = train(data, methodName);
		predict(data, methodName, modelId);
		
		return modelId;
	}
}
