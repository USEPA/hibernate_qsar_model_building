package gov.epa.endpoints.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintViolationException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingServiceImpl;


import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;
import gov.epa.databases.dev_qsar.qsar_models.service.MethodService;
import gov.epa.databases.dev_qsar.qsar_models.service.MethodServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionService;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import kong.unirest.Unirest;

public class ModelBuilder {
	protected DataPointInSplittingService dataPointInSplittingService = new DataPointInSplittingServiceImpl();
	private StatisticService statisticService = new StatisticServiceImpl();
	protected MethodService methodService = new MethodServiceImpl();
	protected ModelService modelService = new ModelServiceImpl();
	protected ModelStatisticService modelStatisticService = new ModelStatisticServiceImpl();
	protected PredictionService predictionService = new PredictionServiceImpl();
	
	protected String lanId;
	
	protected Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	public ModelBuilder(String lanId) {
		// Set logging providers for Hibernate and MChange
		System.setProperty("org.jboss.logging.provider", "log4j");
		System.setProperty("com.mchange.v2.log.MLog", "log4j");
		
		// Reduce logging output from Apache, Hibernate, and C3P0
//		String[] loggerNames = {"org.apache.http", "org.hibernate", "com.mchange"};
//		for (String loggerName:loggerNames) {
//			Logger thisLogger = LogManager.getLogger(loggerName);
//			thisLogger.setLevel(Level.ERROR);
//		}
		
		// Make sure Unirest is configured
		try {
			Unirest.config().followRedirects(true).socketTimeout(000).connectTimeout(000);
		} catch (Exception e) {
			// Ignore
		}
		
		// Provide the user ID
		this.lanId = lanId;
	}
	
	
	
	
	public void postPredictions(List<ModelPrediction> modelPredictions, Model model,Splitting splitting) {

		List<Prediction>predictions=new ArrayList<>();
		for (ModelPrediction mp:modelPredictions) {
			Prediction prediction = new Prediction(mp, model, splitting, lanId);
			predictions.add(prediction);
		}

		
		//TODO for now use sql implementation unless hibernate can be sped up:		
		predictionService.createSQL(predictions);
		
// Attempt at batch insert- not working yet		
//		
//		predictionService.create(predictions);
		
//		for (ModelPrediction mp:modelPredictions) {
//			Prediction prediction = new Prediction(mp, model, splitting, lanId);
//			
//			try {
//				predictionService.create(prediction);
//			} catch (ConstraintViolationException e) {
//				System.out.println(e.getMessage());
//			}
//		}
	}
	
	public void calculateAndPostModelStatistics(List<ModelPrediction> trainingSetPredictions, List<ModelPrediction> testSetPredictions,
			Model model,boolean postPredictions) {
		
		
		double meanExpTraining= ModelStatisticCalculator.calcMeanExpTraining(trainingSetPredictions);
		
		Map<String, Double> modelTestStatisticValues = null;
		Map<String, Double> modelTrainingStatisticValues = null;
		
		System.out.println("calculateAndPostModelStatistics(), model.getMethod().getIsBinary()="+model.getMethod().getIsBinary());
		
		if (model.getMethod().getIsBinary()) {
			modelTestStatisticValues = 
					ModelStatisticCalculator.calculateBinaryStatistics(testSetPredictions, 
							DevQsarConstants.BINARY_CUTOFF,
							DevQsarConstants.TAG_TEST);
			modelTrainingStatisticValues = 
					ModelStatisticCalculator.calculateBinaryStatistics(trainingSetPredictions, 
							DevQsarConstants.BINARY_CUTOFF,
							DevQsarConstants.TAG_TRAINING);
		} else {
			modelTestStatisticValues = 
					ModelStatisticCalculator.calculateContinuousStatistics(testSetPredictions, 
							meanExpTraining,
							DevQsarConstants.TAG_TEST);
			
//			double Q2_TEST=modelTestStatisticValues.get(DevQsarConstants.Q2_TEST);
			
			double Q2_F3_TEST=ModelStatisticCalculator.calculateQ2_F3(trainingSetPredictions, testSetPredictions);
			modelTestStatisticValues.put(DevQsarConstants.Q2_F3_TEST,Q2_F3_TEST);
			
//			System.out.println("Q2_TEST="+Q2_TEST);
//			System.out.println("Q2_Consonni="+Q2_Consonni);
			
			
			modelTrainingStatisticValues = 
					ModelStatisticCalculator.calculateContinuousStatistics(trainingSetPredictions, 
							meanExpTraining,
							DevQsarConstants.TAG_TRAINING);
		}
		
		if(postPredictions) {
			System.out.print("Posting stats:");
			postModelStatistics(modelTestStatisticValues, model);
			postModelStatistics(modelTrainingStatisticValues, model);
			System.out.println("done");
		} else {
//			System.out.println(model.getName());
			System.out.println(Utilities.gson.toJson(modelTestStatisticValues));
			System.out.println(Utilities.gson.toJson(modelTrainingStatisticValues));
		}
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
			
			try {
				modelStatisticService.create(modelStatistic);
			} catch (ConstraintViolationException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	
}
