package gov.epa.endpoints.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.SplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInConsensusModel;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInConsensusModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInConsensusMethodServiceImpl;

public class ConsensusModelBuilder extends ModelBuilder {
	
	private ModelServiceImpl modelService = new ModelServiceImpl();
	private ModelInConsensusModelService modelInConsensusMethodService = new ModelInConsensusMethodServiceImpl();
	SplittingServiceImpl splittingService=new SplittingServiceImpl();
	DataPointServiceImpl dataPointService=new DataPointServiceImpl();
	
	
	StatisticServiceImpl statisticService=new StatisticServiceImpl();
	ModelStatisticServiceImpl modelStatisticService=new ModelStatisticServiceImpl();
	
	public ConsensusModelBuilder(String lanId) {
		super(lanId);
	}
	
	private Long createUnweighted(Set<Long> modelIds) {
		Map<Long, Double> modelIdsWithNullWeights = new HashMap<Long, Double>();
		for (Long id:modelIds) {
			modelIdsWithNullWeights.put(id, 1.0);
		}
		return createWeighted(modelIdsWithNullWeights);
	}
	
	private Long createWeighted(Map<Long, Double> modelIdsWithWeights) {
		Set<Long> modelIds = modelIdsWithWeights.keySet();
		List<Model> models = modelService.findByIdIn(modelIds);
		if (models.size() < modelIds.size()) {
			System.out.println("One or more specified models was not retrieved");
			return null;
		}
		
		// Enforce same dataset and splitting, not necessarily same descriptor set or embedding
		Iterator<Model> it = models.iterator();
		Model firstModel = it.next();
		Boolean isBinary = firstModel.getMethod().getIsBinary();
		String datasetName = firstModel.getDatasetName();
		String splittingName = firstModel.getSplittingName();
		while (it.hasNext()) {
			Model model = it.next();
			Method method = model.getMethod();
			if (!model.getDatasetName().equals(datasetName)) {
				System.out.println("Specified models do not all have same dataset");
				return null;
			} else if (!model.getSplittingName().equals(splittingName)) {
				System.out.println("Specified models do not all have same splitting");
				return null;
			} else if (!method.getIsBinary().equals(isBinary)) {
				System.out.println("Cannot mix classification and regression methods");
				return null;
			}
		}
		
		// If all models are consistent as desired, set up the method with the appropriate models
		String tag = isBinary ? "_classifier" : "_regressor";
		String consensusMethodName = DevQsarConstants.CONSENSUS + tag;
		Method consensusMethod = methodService.findByName(consensusMethodName);
		if (consensusMethod==null) {
			consensusMethod = new Method(consensusMethodName, 
					"Consensus, see individual models for descriptions, hyperparameters, and weights",
					null,
					null,
					isBinary,
					lanId);
			consensusMethod = methodService.create(consensusMethod);
		}
		
		Model consensusModel = new Model(consensusMethod, DevQsarConstants.CONSENSUS, datasetName, splittingName, lanId);
		consensusModel = modelService.create(consensusModel);
		
		for (Model model:models) {
			Double weight = modelIdsWithWeights.get(model.getId());
			ModelInConsensusModel micm = new ModelInConsensusModel(model, consensusModel, weight, lanId);
			modelInConsensusMethodService.create(micm);
		}
		
		return consensusModel.getId();
	}
	
//	private void predict(Long consensusModelId) {
//		Model consensusModel = modelService.findById(consensusModelId);
//		if (consensusModel==null) {
//			System.out.println("Consensus model not found for ID " + consensusModelId);
//			return;
//		}
//		
//		List<ModelInConsensusModel> modelsInConsensusModel = consensusModel.getModelsInConsensusModel();
//		if (modelsInConsensusModel==null || modelsInConsensusModel.isEmpty()) {
//			System.out.println("No models assigned to consensus model with ID " + consensusModelId);
//		}
//		
//		List<DataPointInSplitting> dataPointsInSplitting = dataPointInSplittingService
//				.findByDatasetNameAndSplittingName(consensusModel.getDatasetName(), consensusModel.getSplittingName());
//		
//		List<DataPoint> trainingSet = dataPointsInSplitting.stream()
//				.filter(dpis -> dpis.getSplitNum().equals(DevQsarConstants.TRAIN_SPLIT_NUM))
//				.map(dpis -> dpis.getDataPoint())
//				.collect(Collectors.toList());
//		
//		List<DataPoint> testSet = dataPointsInSplitting.stream()
//				.filter(dpis -> dpis.getSplitNum().equals(DevQsarConstants.TEST_SPLIT_NUM))
//				.map(dpis -> dpis.getDataPoint())
//				.collect(Collectors.toList());
//		
//		
//		Splitting splitting=ssi.findByName(DevQsarConstants.SPLITTING_RND_REPRESENTATIVE);
//		
//		List<ModelPrediction> trainingSetPredictions = computeConsensusPredictions(trainingSet, modelsInConsensusModel);
//		postPredictions(trainingSetPredictions, consensusModel,DevQsarConstants.TRAIN_SPLIT_NUM,splitting);
//				
//		List<ModelPrediction> testSetPredictions = computeConsensusPredictions(testSet, modelsInConsensusModel);
//		postPredictions(testSetPredictions, consensusModel,DevQsarConstants.TEST_SPLIT_NUM,splitting);
//		
//		calculateAndPostModelStatistics(trainingSetPredictions, testSetPredictions, consensusModel);
//	}
	
	
	private void predict(Long consensusModelId) {
		Model consensusModel = modelService.findById(consensusModelId);
		if (consensusModel==null) {
			System.out.println("Consensus model not found for ID " + consensusModelId);
			return;
		}
		
		List<ModelInConsensusModel> modelsInConsensusModel = consensusModel.getModelsInConsensusModel();
		
		if (modelsInConsensusModel==null || modelsInConsensusModel.isEmpty()) {
			System.out.println("No models assigned to consensus model with ID " + consensusModelId);
		}
		
		List<DataPoint> dataPoints = 
				dataPointService.findByDatasetName(consensusModel.getDatasetName());

		Map<String, Double> expMap = dataPoints.stream()
				.collect(Collectors.toMap(dp -> dp.getCanonQsarSmiles(), dp -> dp.getQsarPropertyValue()));

						
		predictModelSplitting(consensusModel, modelsInConsensusModel, expMap);
		
							
		predictCV(consensusModel, modelsInConsensusModel, expMap,true);
				
	}
	
	/**
	 * Handles predictions for model's splitting
	 * 
	 * @param consensusModel
	 * @param modelsInConsensusModel
	 */
	private void predictModelSplitting(Model consensusModel,List<ModelInConsensusModel> modelsInConsensusModel,Map<String, Double> expMap) {
		
		String splittingName=modelsInConsensusModel.get(0).getConsensusModel().getSplittingName();
		
		Splitting splitting=splittingService.findByName(splittingName);
		String datasetName=consensusModel.getDatasetName();
		
		List<DataPointInSplitting> dataPointsInSplitting = 
				dataPointInSplittingService.findByDatasetNameAndSplittingName(datasetName, splittingName);

		Map<String, Integer> splittingMap = dataPointsInSplitting.stream()
				.collect(Collectors.toMap(dpis -> dpis.getDataPoint().getCanonQsarSmiles(), dpis -> dpis.getSplitNum()));

		List<ModelPrediction> trainPreds = computeConsensusPredictions(modelsInConsensusModel,splittingMap,expMap, DevQsarConstants.TRAIN_SPLIT_NUM,splitting,datasetName);
//		System.out.println(trainPreds.size());
				
		System.out.print("Posting predictions...");
		postPredictions(trainPreds, consensusModel,splitting);
		System.out.print("done\n");
				
		List<ModelPrediction> testPreds = computeConsensusPredictions(modelsInConsensusModel, splittingMap,expMap,DevQsarConstants.TEST_SPLIT_NUM,splitting,datasetName);
		postPredictions(testPreds, consensusModel,splitting);

		System.out.print("Posting stats...");
		calculateAndPostModelStatistics(trainPreds, testPreds, consensusModel);
		System.out.print("done\n");
	}
	
	/**
	 * Handles CV
	 * 
	 */
	double [] predictCV(Model consensusModel,List<ModelInConsensusModel> micm,Map<String, Double> expMap,boolean postPredictions) {

		Model model0=modelService.findById(micm.get(0).getModel().getId());
		String datasetName=model0.getDatasetName();
		
		double Q2_CV=0;
		double R2_CV=0;
		
		for (int i=1;i<=5;i++) {
			
			Splitting splittingCV=splittingService.findByName(model0.getSplittingName()+"_CV"+i);

			List<DataPointInSplitting> dataPointsInSplittingCV = 
					dataPointInSplittingService.findByDatasetNameAndSplittingName(datasetName, splittingCV.getName());

			Map<String, Integer> splittingMapCV = dataPointsInSplittingCV.stream()
					.collect(Collectors.toMap(dpis -> dpis.getDataPoint().getCanonQsarSmiles(), dpis -> dpis.getSplitNum()));

			List<ModelPrediction> mpsTestSet = computeConsensusPredictions(micm,splittingMapCV,expMap, DevQsarConstants.TEST_SPLIT_NUM,splittingCV,datasetName);				
						
			if (postPredictions) {
				System.out.print("Posting CV predictions Split "+i+" ");
				postPredictions(mpsTestSet, consensusModel, splittingCV);
				System.out.print("done\n");
			}
			
			List<ModelPrediction> mpsTrainSet=new ArrayList<>();
						
			for (DataPointInSplitting dpis:dataPointsInSplittingCV) {

				if(dpis.getSplitNum()!=DevQsarConstants.TRAIN_SPLIT_NUM) continue;//can have splitNum=2 for the PFAS ones...
				
				String id=dpis.getDataPoint().getCanonQsarSmiles();
				double exp=expMap.get(dpis.getDataPoint().getCanonQsarSmiles());
				double pred=Double.NaN;//we dont have preds for training in CV 
								
				mpsTrainSet.add(new ModelPrediction(id, exp, pred, DevQsarConstants.TRAIN_SPLIT_NUM));
			}
							
			double Q2_CV_i=ModelStatisticCalculator.calculateQ2(mpsTrainSet, mpsTestSet);
			Q2_CV+=Q2_CV_i;
			
			double YbarTrain=ModelStatisticCalculator.calcMeanExpTraining(mpsTrainSet);				
			Map<String, Double>mapStats=ModelStatisticCalculator.calculateContinuousStatistics(mpsTestSet, YbarTrain, DevQsarConstants.TAG_TEST);				
			double R2_CV_i=mapStats.get(DevQsarConstants.PEARSON_RSQ + DevQsarConstants.TAG_TEST);
			R2_CV+=R2_CV_i;

		}		
		
		R2_CV/=5.0;
		Q2_CV/=5.0;

		if (postPredictions) {			
			System.out.println("storing R2_CV_Training="+R2_CV);
			Statistic statistic=statisticService.findByName("R2_CV_Training");					
			ModelStatistic modelStatistic=new ModelStatistic(statistic, consensusModel, R2_CV, lanId);
			modelStatistic=modelStatisticService.create(modelStatistic);
				
			System.out.println("storing Q2_CV_Training="+Q2_CV);
			statistic=statisticService.findByName("Q2_CV_Training");					
			modelStatistic=new ModelStatistic(statistic, consensusModel, Q2_CV, lanId);
			modelStatistic=modelStatisticService.create(modelStatistic);
		} else {
			System.out.println("R2_CV_Training="+R2_CV);
			System.out.println("Q2_CV_Training="+Q2_CV);
		}

		double [] results={R2_CV,Q2_CV};
		return results;
		
	}
	private List<ModelPrediction> computeConsensusPredictions(List<ModelInConsensusModel> modelsInConsensusMethod,Map<String, Integer> splittingMap,Map<String, Double> expMap, Integer splitNum,Splitting splitting,String datasetName) {
		
		Hashtable<String, List<ModelPrediction>> htConsensusPredictions =new Hashtable<>();
		
		for (ModelInConsensusModel micm:modelsInConsensusMethod) {
			addPredictionsToConsensusPredictions(htConsensusPredictions,splittingMap, expMap,  micm, splitNum,splitting);			
		}
		
		List<ModelPrediction>consensusPreds=new ArrayList<>();
				
		for (String key:htConsensusPredictions.keySet()) {
			List<ModelPrediction>mps=htConsensusPredictions.get(key);

			double weightTotal=0;
			double pred=0;
			
			for(ModelPrediction mp:mps) {
				pred+=mp.pred*mp.weight;
				weightTotal+=mp.weight;
			}
			pred/=weightTotal;
						
			String ID=key;			
			Double exp=mps.get(0).exp;
			Integer split=mps.get(0).split;
						
			ModelPrediction mpNew=new ModelPrediction(ID, exp, pred,split);
			
//			System.out.println(exp+"\t"+mpNew.exp);			
			
			consensusPreds.add(mpNew);
			
			System.out.println(mpNew.id+"\t"+mpNew.exp+"\t"+"\t"+mpNew.pred+"\t"+mps.size());
		}
		System.out.println("");
		
		
		return consensusPreds;
	}
	
	
	private List<ModelPrediction> computeConsensusCVPredictions(List<ModelInConsensusModel> modelsInConsensusMethod,Map<String, Integer> splittingMap,Map<String, Double> expMap,Splitting splitting) {
		
		Hashtable<String, List<ModelPrediction>> htConsensusPredictions =new Hashtable<>();
		
		for (ModelInConsensusModel micm:modelsInConsensusMethod) {
			addPredictionsToConsensusPredictions(htConsensusPredictions, splittingMap,expMap, micm, -1,splitting);			
		}
		
		List<ModelPrediction>consensusPreds=new ArrayList<>();
				
		for (String key:htConsensusPredictions.keySet()) {
			List<ModelPrediction>preds=htConsensusPredictions.get(key);
			double weightTotal=0;
			double pred=0;
			
			for(ModelPrediction mp:preds) {
				pred+=mp.pred*mp.weight;
				weightTotal+=mp.weight;
			}
			pred/=weightTotal;
			
			consensusPreds.add(new ModelPrediction(key,preds.get(0).exp, pred,preds.get(0).split));
			System.out.println(key+"\t"+preds.size()+"\t"+pred);
		}
		
		return consensusPreds;
	}
	
	private void addPredictionsToConsensusPredictions(Hashtable<String, List<ModelPrediction>> htConsensusPredictions, Map<String, Integer> splittingMap, Map<String, Double> expMap, ModelInConsensusModel micm,Integer splitNum,Splitting splitting) {

		System.out.println(splitting.getId()+"\t"+micm.getModel().getId());
		
		List<Prediction> predictions = predictionService.findByIds(micm.getModel().getId(),splitting.getId());
				
		
		for(Prediction prediction:predictions) {

			int splitNumPred=splittingMap.get(prediction.getCanonQsarSmiles()); 			
			if (splitNumPred!=splitNum) continue;
			
			double exp=expMap.get(prediction.getCanonQsarSmiles());
			double pred=prediction.getQsarPredictedValue();
			String smiles=prediction.getCanonQsarSmiles();
						
			if (htConsensusPredictions.get(prediction.getCanonQsarSmiles())==null) {
				List<ModelPrediction>consensusPredictions=new ArrayList<>();
				htConsensusPredictions.put(prediction.getCanonQsarSmiles(), consensusPredictions);
																
				ModelPrediction mp=new ModelPrediction(smiles,exp,pred,splitNum);
//				System.out.println(micm.getModel().getMethod().getName()+"\t"+mp.id+"\t"+mp.exp+"\t"+pred);
				
				mp.weight=micm.getWeight();
				consensusPredictions.add(mp);
				
			} else {
				List<ModelPrediction>consensusPredictions=htConsensusPredictions.get(prediction.getCanonQsarSmiles());
				htConsensusPredictions.put(prediction.getCanonQsarSmiles(), consensusPredictions);
				
				ModelPrediction mp=new ModelPrediction(smiles,exp,pred,splitNum);
				mp.weight=micm.getWeight();
				
//				System.out.println(micm.getModel().getMethod().getName()+"\t"+mp.id+"\t"+mp.exp+"\t"+pred);
				
				consensusPredictions.add(mp);
			}
		}
		
	}
	
	public void buildUnweighted(Set<Long> modelIds) {
		Long consensusModelId = createUnweighted(modelIds);
		predict(consensusModelId);
	}
	
	public void buildWeighted(Map<Long, Double> modelIdsWithWeights) {
		Long consensusModelId = createWeighted(modelIdsWithWeights);
		predict(consensusModelId);
	}
	
	
	void testCalcConsensus(List<Long> consensusModelIDs) {
		
			
		List<ModelInConsensusModel>consensusModels=new ArrayList<>();
		
		Model consensusModel=new Model();
		
		for(Long id:consensusModelIDs) {
			Model model=modelService.findById(id);
//			System.out.println(model.getDatasetName());
			consensusModels.add(new ModelInConsensusModel(model, consensusModel, 1.0, lanId));
		}
		
		String datasetName=consensusModels.get(0).getModel().getDatasetName();
		
		System.out.println(datasetName);
		
		
		List<DataPoint> dataPoints =dataPointService.findByDatasetName(datasetName);
		
		Map<String, Double> expMap = dataPoints.stream()
				.collect(Collectors.toMap(dp -> dp.getCanonQsarSmiles(), dp -> dp.getQsarPropertyValue()));
		
		predResultsModelSplitting(consensusModels,expMap);
		
		//For cross-validation:
		predictCV(consensusModel, consensusModels, expMap,false);
		
			
	}

	private void predResultsModelSplitting(List<ModelInConsensusModel> consensusModels,Map<String, Double> expMap) {
				
		Model model0=modelService.findById(consensusModels.get(0).getModel().getId());
		String datasetName=model0.getDatasetName();

		
		//For train/test set:
		Splitting splitting=splittingService.findByName(model0.getSplittingName());
				
		List<DataPointInSplitting> dataPointsInSplitting = 
				dataPointInSplittingService.findByDatasetNameAndSplittingName(datasetName, splitting.getName());

		Map<String, Integer> splittingMap = dataPointsInSplitting.stream()
				.collect(Collectors.toMap(dpis -> dpis.getDataPoint().getCanonQsarSmiles(), dpis -> dpis.getSplitNum()));
		
		List<ModelPrediction> trainPreds = computeConsensusPredictions(consensusModels, splittingMap, expMap, DevQsarConstants.TRAIN_SPLIT_NUM,splitting,datasetName);
		double meanExpTraining=ModelStatisticCalculator.calcMeanExpTraining(trainPreds);
				
		Map<String,Double>modelTrainingStatisticValues =ModelStatisticCalculator.calculateContinuousStatistics(trainPreds, 
				meanExpTraining,DevQsarConstants.TAG_TRAINING);
						
		System.out.println("Training stats:");
		for (String key:modelTrainingStatisticValues.keySet()) {
			System.out.println(key+"\t"+modelTrainingStatisticValues.get(key));
		}
		System.out.println("");

		List<ModelPrediction> testPreds = computeConsensusPredictions(consensusModels, splittingMap, expMap, DevQsarConstants.TEST_SPLIT_NUM,splitting,datasetName);

		Map<String,Double>modelTestStatisticValues =ModelStatisticCalculator.calculateContinuousStatistics(testPreds, 
				meanExpTraining,DevQsarConstants.TAG_TEST);

		System.out.println("Test stats:");
		for (String key:modelTestStatisticValues.keySet()) {
			System.out.println(key+"\t"+modelTestStatisticValues.get(key));
		}
		System.out.println("");
	}

	public static void main(String[] args) {
		ConsensusModelBuilder cmb = new ConsensusModelBuilder("tmarti02");
		List<Long> consensusModelIDs = new ArrayList<Long>();
		for (Long i=1143L;i<=1146L;i++) consensusModelIDs.add(i);
		cmb.testCalcConsensus(consensusModelIDs);
		
	}

	
	
}
