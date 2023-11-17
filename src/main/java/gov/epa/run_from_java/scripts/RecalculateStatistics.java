package gov.epa.run_from_java.scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.SplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInConsensusMethodServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInConsensusModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionService;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticServiceImpl;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.models.ModelStatisticCalculator;
import gov.epa.run_from_java.scripts.RecalcStatsScript.SplitPredictions;

/**
* @author TMARTI02
*/
public class RecalculateStatistics {
	
	ModelServiceImpl modelService = new ModelServiceImpl();
	ModelInConsensusModelService modelInConsensusMethodService = new ModelInConsensusMethodServiceImpl();
	SplittingServiceImpl splittingService=new SplittingServiceImpl();
	DataPointServiceImpl dataPointService=new DataPointServiceImpl();
	StatisticServiceImpl statisticService=new StatisticServiceImpl();
	ModelStatisticServiceImpl modelStatisticService=new ModelStatisticServiceImpl();
	DatasetServiceImpl datasetService=new DatasetServiceImpl();
	PredictionService predictionService = new PredictionServiceImpl();
	DataPointInSplittingService dataPointInSplittingService = new DataPointInSplittingServiceImpl();
	
	String lanId="tmarti02";
	
	void recalcStats() {
		
//		String splittingName=DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
//		String splittingName=SplittingGeneratorPFAS_Script.splittingPFASOnly;
//		String splittingName=SplittingGeneratorPFAS_Script.splittingAllButPFAS;

		List<String>splittingNames=new ArrayList<>();
		splittingNames.add(DevQsarConstants.SPLITTING_RND_REPRESENTATIVE);
		splittingNames.add(SplittingGeneratorPFAS_Script.splittingPFASOnly);
		splittingNames.add(SplittingGeneratorPFAS_Script.splittingAllButPFAS);
		
		
		List<String>datasetNames=new ArrayList<>();	
		datasetNames.add("HLC v1 modeling");
		datasetNames.add("WS v1 modeling");
		datasetNames.add("VP v1 modeling");
		datasetNames.add("LogP v1 modeling");
		datasetNames.add("BP v1 modeling");
		datasetNames.add("MP v1 modeling");
		
//		String datasetName="WS v1 modeling";
		
//		for (String datasetName:datasetNames) {
//			updateModelsInDataset(splittingName, datasetName);
//		}
		

		for (String splittingName:splittingNames) {
			for (String datasetName:datasetNames) {
				updateModelsInDataset(splittingName, datasetName);
			}
		}
		
		
	}

	private void updateModelsInDataset(String splittingName, String datasetName) {
		
		List<Model>models=modelService.findByDatasetName(datasetName);

		List<DataPoint> dataPoints = 
				dataPointService.findByDatasetName(datasetName);
		Map<String, Double> expMap = dataPoints.stream()
				.collect(Collectors.toMap(dp -> dp.getCanonQsarSmiles(), dp -> dp.getQsarPropertyValue()));

		
		for (Model model:models) {
			if(!model.getSplittingName().equals(splittingName)) continue;
			
//			if(model.getDescriptorEmbedding()==null) continue;
//			if(!model.getMethod().getName().contains("consensus")) continue;
			System.out.println("\n"+datasetName+"\t"+model.getId()+"\t"+model.getMethod().getName()+"\t"+model.getSplittingName());
//			System.out.println("dataPoints.size()="+dataPoints.size());
//			recalcStatsForModelSplitting(model, expMap, false);
			recalcStatsForCrossValidation(model, expMap, false);
//			if(true)break;
		}
	}
	
	/**
	 * Currently updates PEARSON_RSQ_CV_TRAINING
	 * TODO fix Q2_CV_TRAINING
	 * 
	 * @param model
	 * @param expMap
	 * @param postPredictions
	 */
	public void recalcStatsForCrossValidation(Model model,Map<String, Double> expMap,boolean postPredictions) {
		
//		System.out.println(model.getSplittingName());
					
		Dataset dataset=datasetService.findByName(model.getDatasetName());

		List<ModelPrediction> mpsTestSetPooled=new ArrayList<>();
		
//		double Q2_CV_AVG=0;
//		double R2_CV_AVG=0;
				
		for (int i=1;i<=5;i++) {
			
			Splitting splitting=splittingService.findByName(model.getSplittingName()+"_CV"+i);
//			
			SplitPredictions modelPredictions=getModelPredictions(model, expMap, splitting);
			
//			SplitPredictions modelPredictions=RecalcStatsScript.SplitPredictions.getSplitPredictions(model, splitting);
			
//			System.out.println(i+"\t"+modelPredictions.trainingSetPredictions.size()+"\t"+modelPredictions.testSetPredictions.size());
			
			mpsTestSetPooled.addAll(modelPredictions.testSetPredictions);

//			double Q2_CV_i=ModelStatisticCalculator.calculateQ2_F3(modelPredictions.trainingSetPredictions, modelPredictions.testSetPredictions);
//			Q2_CV_AVG+=Q2_CV_i;
//			
//			double YbarTrain=ModelStatisticCalculator.calcMeanExpTraining(modelPredictions.trainingSetPredictions);				
//			Map<String, Double>mapStats=ModelStatisticCalculator.calculateContinuousStatistics(modelPredictions.testSetPredictions, YbarTrain, DevQsarConstants.TAG_TEST);				
//			double R2_CV_i=mapStats.get(DevQsarConstants.PEARSON_RSQ + DevQsarConstants.TAG_TEST);
//			R2_CV_AVG+=R2_CV_i;
			
//			System.out.println(i+"\t"+R2_CV_i);
			
		}

//		R2_CV_AVG/=5.0;
//		Q2_CV_AVG/=5.0;
		
//		System.out.println(mpsTestSetPooled.size());
		
		Map<String, Double>mapStatsPooled=ModelStatisticCalculator.calculateContinuousStatistics(mpsTestSetPooled, 0.0, DevQsarConstants.TAG_TEST);
		double R2_CV_pooled=mapStatsPooled.get(DevQsarConstants.PEARSON_RSQ + DevQsarConstants.TAG_TEST);
		double R2_CV=R2_CV_pooled;//Note R2_CV_AVG is virtually identical to R2_CV_Pooled for large sets

		double MAE_CV=mapStatsPooled.get(DevQsarConstants.MAE + DevQsarConstants.TAG_TEST);
		
		
		System.out.println("MAE_CV\t"+MAE_CV);
//		
//		System.out.println(DevQsarConstants.PEARSON_RSQ_CV_TRAINING+"\t"+R2_CV_pooled);
//		System.out.println("Q2_CV_AVG\t"+Q2_CV_AVG);
		
//		Statistic statistic=statisticService.findByName(DevQsarConstants.PEARSON_RSQ_CV_TRAINING);
//		ModelStatistic modelStatistic=modelStatisticService.findByModelId(model.getId(), statistic.getId());
//		modelStatistic.setStatisticValue(R2_CV);
//		modelStatistic.setUpdatedBy(lanId);		
//		modelStatistic=modelStatisticService.update(modelStatistic);
//		System.out.println(modelStatistic.getUpdatedAt());
		
		Statistic statistic=statisticService.findByName(DevQsarConstants.MAE_CV_TRAINING);
		ModelStatistic modelStatistic=new ModelStatistic(statistic,model, MAE_CV, lanId);
		modelStatistic=modelStatisticService.create(modelStatistic);
		System.out.println(modelStatistic.getCreatedAt());

		
	}
	
	

	
//	class SplitPredictions {
//		List<ModelPrediction>mpsTrain=new ArrayList<>();
//		List<ModelPrediction>mpsTest=new ArrayList<>();
//	}
	
	/**
	 * Currently this adds Q2_F3_Test statistic
	 * 
	 * @param model
	 * @param expMap
	 * @param postPredictions
	 */
	private void recalcStatsForModelSplitting(Model model,Map<String, Double> expMap,boolean postPredictions) {
		
		Splitting splitting=splittingService.findByName(model.getSplittingName());
		
		
		SplitPredictions modelPredictions=getModelPredictions(model, expMap,splitting);
		
		System.out.println(modelPredictions.trainingSetPredictions.size()+"\t"+modelPredictions.testSetPredictions.size());
		
		
		double meanExpTraining= ModelStatisticCalculator.calcMeanExpTraining(modelPredictions.trainingSetPredictions);
		
		Map<String, Double> modelTestStatisticValues = 
				ModelStatisticCalculator.calculateContinuousStatistics(modelPredictions.testSetPredictions,meanExpTraining,
						DevQsarConstants.TAG_TEST);
		
		Map<String, Double> modelTrainingStatisticValues = 
				ModelStatisticCalculator.calculateContinuousStatistics(modelPredictions.trainingSetPredictions,meanExpTraining,
						DevQsarConstants.TAG_TRAINING);

		
//		for (String stat:modelTestStatisticValues.keySet()) {
//			System.out.println(stat+"\t"+modelTestStatisticValues.get(stat));
//		}
//		
//		for (String stat:modelTrainingStatisticValues.keySet()) {
//			System.out.println(stat+"\t"+modelTrainingStatisticValues.get(stat));
//		}
		
		System.out.println("Q2_Test\t"+modelTestStatisticValues.get("Q2_Test"));
		
		
		double Q2_F3=ModelStatisticCalculator.calculateQ2_F3(modelPredictions.trainingSetPredictions, modelPredictions.testSetPredictions);
		System.out.println("Q2_F3_Test\t"+Q2_F3);
				
		Statistic statistic=statisticService.findByName(DevQsarConstants.Q2_F3_TEST);
		ModelStatistic modelStatistic=new ModelStatistic(statistic, model, Q2_F3, lanId);
		
		//TODO add code to change it to sql update if it already exists
		
		modelStatistic=modelStatisticService.create(modelStatistic);
		System.out.println(modelStatistic.getCreatedAt());

	}

	public SplitPredictions getModelPredictions(Model model, Map<String, Double> expMap, Splitting splitting) {
		SplitPredictions modelPredictions =new SplitPredictions();
		
		List<Prediction> predictions = predictionService.findByIds(model.getId(),splitting.getId());

		List<DataPointInSplitting> dataPointsInSplitting = 
				dataPointInSplittingService.findByDatasetNameAndSplittingName(model.getDatasetName(), splitting.getName());
		Map<String, Integer> splittingMap = dataPointsInSplitting.stream()
				.collect(Collectors.toMap(dpis -> dpis.getDataPoint().getCanonQsarSmiles(), dpis -> dpis.getSplitNum()));
		
		
		for (Prediction prediction:predictions) {
			
			int splitNum=splittingMap.get(prediction.getCanonQsarSmiles());
			double exp=expMap.get(prediction.getCanonQsarSmiles());
			double pred=prediction.getQsarPredictedValue();
			ModelPrediction mp=new ModelPrediction(prediction.getCanonQsarSmiles(), exp, pred, splitNum);

			if(splittingMap.get(prediction.getCanonQsarSmiles())==DevQsarConstants.TRAIN_SPLIT_NUM) {
				modelPredictions.trainingSetPredictions.add(mp);
			} else if(splittingMap.get(prediction.getCanonQsarSmiles())==DevQsarConstants.TEST_SPLIT_NUM) {
				modelPredictions.testSetPredictions.add(mp);
			}
//			System.out.println(splitting.getName()+"\t"+prediction.getCanonQsarSmiles()+"\t"+exp+"\t"+pred+"\t"+splitNum);
			
		}
		
		if (modelPredictions.trainingSetPredictions.size()>0) return modelPredictions;
		
		
		for (DataPointInSplitting dpis:dataPointsInSplitting) {
			if(dpis.getSplitNum()!=DevQsarConstants.TRAIN_SPLIT_NUM) continue;//can have splitNum=2 for the PFAS ones...
			String id=dpis.getDataPoint().getCanonQsarSmiles();
			double exp=expMap.get(dpis.getDataPoint().getCanonQsarSmiles());
			double pred=Double.NaN;//we dont have preds for training in CV 
			modelPredictions.trainingSetPredictions.add(new ModelPrediction(id, exp, pred, DevQsarConstants.TRAIN_SPLIT_NUM));
		}

		
		return modelPredictions;
	}
	
	public static void main(String[] args) {
		
		RecalculateStatistics r=new RecalculateStatistics();
		r.recalcStats();
	}

}
