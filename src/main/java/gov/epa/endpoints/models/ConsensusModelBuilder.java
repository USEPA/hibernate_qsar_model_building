package gov.epa.endpoints.models;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInConsensusModel;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInConsensusModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInConsensusMethodServiceImpl;

public class ConsensusModelBuilder extends ModelBuilder {
	
	private ModelInConsensusModelService modelInConsensusMethodService = new ModelInConsensusMethodServiceImpl();
	
	public ConsensusModelBuilder(String lanId) {
		super(lanId);
		// TODO Auto-generated constructor stub
	}
	
	private Long createUnweighted(Set<Long> modelIds) {
		Map<Long, Double> modelIdsWithNullWeights = new HashMap<Long, Double>();
		for (Long id:modelIds) {
			modelIdsWithNullWeights.put(id, null);
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
		
		List<DataPointInSplitting> dataPointsInSplitting = dataPointInSplittingService
				.findByDatasetNameAndSplittingName(consensusModel.getDatasetName(), consensusModel.getSplittingName());
		List<DataPoint> trainingSet = dataPointsInSplitting.stream()
				.filter(dpis -> dpis.getSplitNum().equals(DevQsarConstants.TRAIN_SPLIT_NUM))
				.map(dpis -> dpis.getDataPoint())
				.collect(Collectors.toList());
		List<DataPoint> testSet = dataPointsInSplitting.stream()
				.filter(dpis -> dpis.getSplitNum().equals(DevQsarConstants.TEST_SPLIT_NUM))
				.map(dpis -> dpis.getDataPoint())
				.collect(Collectors.toList());
		
		List<ModelPrediction> trainingSetPredictions = computeConsensusPredictions(trainingSet, modelsInConsensusModel);
		postPredictions(trainingSetPredictions, consensusModel);
		
		List<ModelPrediction> testSetPredictions = computeConsensusPredictions(testSet, modelsInConsensusModel);
		postPredictions(testSetPredictions, consensusModel);
		
		calculateAndPostModelStatistics(trainingSetPredictions, testSetPredictions, consensusModel);
	}
	
	private List<ModelPrediction> computeConsensusPredictions(List<DataPoint> dataPoints, 
			List<ModelInConsensusModel> modelsInConsensusMethod) {
		List<ModelPrediction> consensusPredictions = dataPoints.stream()
				.map(dp -> new ModelPrediction(dp.getCanonQsarSmiles(), dp.getQsarPropertyValue(), 0.0))
				.collect(Collectors.toList());
		
		double totalWeight = 0.0;
		for (ModelInConsensusModel micm:modelsInConsensusMethod) {
			totalWeight += addModelToConsensusPredictions(consensusPredictions, micm);
		}
		
		final double finalTotalWeight = totalWeight; // Fixes weird scope error
		consensusPredictions.forEach(mp -> mp.pred /= finalTotalWeight);
		
		return consensusPredictions;
	}
	
	private Double addModelToConsensusPredictions(List<ModelPrediction> consensusPredictions, ModelInConsensusModel micm) {
		Double weight = micm.getWeight()==null ? 1.0 : micm.getWeight();
		List<Prediction> predictions = predictionService.findByModelId(micm.getModel().getId());
		Map<String, Double> predictedValues = new HashMap<String, Double>();
		for (Prediction p:predictions) {
			predictedValues.put(p.getCanonQsarSmiles(), p.getQsarPredictedValue());
		}
		
		ListIterator<ModelPrediction> li = consensusPredictions.listIterator();
		while (li.hasNext()) {
			ModelPrediction mp = li.next();
			Double predictedValue = predictedValues.get(mp.ID);
			if (predictedValue==null) {
				System.out.println("Prediction for " + mp.ID + " missing from model " + micm.getModel().getId() 
						+ ", removing from consensus set");
				li.remove();
			} else {
				mp.pred += predictedValue * weight;
				li.set(mp);
			}
		}
		
		return weight;
	}
	
	public void buildUnweighted(Set<Long> modelIds) {
		Long consensusModelId = createUnweighted(modelIds);
		predict(consensusModelId);
	}
	
	public void buildWeighted(Map<Long, Double> modelIdsWithWeights) {
		Long consensusModelId = createWeighted(modelIdsWithWeights);
		predict(consensusModelId);
	}
}
