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
	public void train(ModelData data, ModelParams params) {
		if (data.trainingSetInstances==null) {
			logger.error("Dataset instances were not initialized");
			return;
		}
		
		logger.debug("Building Python model with dataset = " + data.datasetName + ", descriptors = " + data.descriptorSetName
				+ ", splitting = " + data.splittingName + " using QSAR method = " + params.qsarMethod);
		
		params.wsModelId = String.valueOf(rand.nextInt(1000000));
		
		byte[] bytes = modelWebService.callTrain(data.trainingSetInstances, 
				data.removeLogDescriptors, params.qsarMethod, params.wsModelId).getBody();
		String hyperparameters = modelWebService.callDetails(params.qsarMethod, params.wsModelId).getBody();
		String description = modelWebService.callInfo(params.qsarMethod).getBody();
		
		JsonObject jo = gson.fromJson(hyperparameters, JsonObject.class);
		String version = jo.get("version").getAsString();
		Boolean isBinary = jo.get("is_binary").getAsBoolean();
		String classOrRegr = isBinary ? "classifier" : "regressor";
		String fullMethodName = params.qsarMethod + "_" + classOrRegr + "_" + version;
		
		Method dbMethod = methodService.findByName(fullMethodName);
		if (dbMethod==null) {
			Method method = new Method(fullMethodName, description, hyperparameters, isBinary, lanId);
			methodService.create(method);
		} else {
			JsonParser parser = new JsonParser();
			if (!parser.parse(hyperparameters).equals(parser.parse(dbMethod.getHyperparameters()))) {
				logger.warn("Hyperparameters for " + fullMethodName + " have changed");
			}
		}
		
		Model model = new Model(dbMethod, data.descriptorSetName, data.datasetName, data.splittingName, lanId);
		modelService.create(model);
		ModelBytes modelBytes = new ModelBytes(model, bytes, lanId);
		modelBytesService.create(modelBytes);
		
		params.dbModelId = model.getId();
	}

	/**
	 * Validates a Python model with the given data and parameters
	 * @param data
	 * @param params
	 */
	public void predict(ModelData data, ModelParams params) {
		if (params.dbModelId==null) {
			logger.error("Model with supplied parameters has not been built");
			return;
		} else if (data.predictionSetInstances==null) {
			logger.error("Dataset instances were not initialized");
			return;
		}
		
		logger.debug("Validating Python model with dataset = " + data.datasetName + ", descriptors = " + data.descriptorSetName
				+ ", splitting = " + data.splittingName + " using QSAR method = " + params.qsarMethod 
				+ " (qsar_models ID = " + params.dbModelId + ")");
		
		ModelBytes modelBytes = modelBytesService.findByModelId(params.dbModelId);
		Model model = modelBytes.getModel();
		byte[] bytes = modelBytes.getBytes();
		
		modelWebService.callInit(bytes, params.qsarMethod, params.wsModelId).getBody();
		
		String predictResponse = modelWebService.callPredict(data.predictionSetInstances, params.qsarMethod, params.wsModelId).getBody();
		ModelPrediction[] modelPredictions = gson.fromJson(predictResponse, ModelPrediction[].class);
		
		for (ModelPrediction pythonModelResponse:modelPredictions) {
			Prediction prediction = new Prediction(pythonModelResponse.ID, model, pythonModelResponse.pred, lanId);
			predictionService.create(prediction);
		}
		
		predictTraining(model, data, params);
		
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
	
	private void predictTraining(Model model, ModelData data, ModelParams params) {
		if (data.trainingSetInstances==null) {
			logger.error("Dataset instances were not initialized");
			return;
		}
		
		String predictTrainingResponse = modelWebService.callPredict(data.trainingSetInstances, params.qsarMethod, params.wsModelId).getBody();
		ModelPrediction[] modelTrainingPredictions = gson.fromJson(predictTrainingResponse, ModelPrediction[].class);
		
		for (ModelPrediction pythonModelResponse:modelTrainingPredictions) {
			Prediction prediction = new Prediction(pythonModelResponse.ID, model, pythonModelResponse.pred, lanId);
			predictionService.create(prediction);
		}
	}

	public Long build(String datasetName, String descriptorSetName, String splittingName, boolean removeLogDescriptors,
			String methodName) {
		ModelData data = initModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors);
		ModelParams params = new ModelParams(methodName);
		
		train(data, params);
		predict(data, params);
		
		return params.dbModelId;
	}
}
