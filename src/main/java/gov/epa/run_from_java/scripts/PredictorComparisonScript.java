package gov.epa.run_from_java.scripts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionService;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionServiceImpl;
import kong.unirest.Unirest;

public class PredictorComparisonScript {
	
	public static class PredictorRequest {
		public List<PredictorChemical> chemicals = new ArrayList<PredictorChemical>();
		public List<PredictorDataset> datasets = new ArrayList<PredictorDataset>();
		
		public void addDataset(PredictorDataset dataset) {
			this.datasets.add(dataset);
		}
	}

	
	public static class PredictorResponse {
		public List<PredictorChemicalValues> chemicals;
		public PredictorDataset dataset;
	}

	public static class PredictorChemical {
		public String inchi;
		public String inchi_key;
		public String smiles;
		
		public PredictorChemical(String smiles) {
			this.smiles = smiles;
		}
	}
	
	public static class PredictorChemicalValues {
		public PredictorChemical chemical;
		public Map<String, Double> values;
	}
	
	public static class PredictorDataset {
		public Boolean binary;
		public Long dataset_id;
		public List<PredictorMethod> methods;
		
		public PredictorDataset(Long datasetId) {
			this.dataset_id = datasetId;
			this.methods = new ArrayList<PredictorMethod>();
		}
		
		public void addMethod(PredictorMethod method) {
			this.methods.add(method);
		}
	}
	
	public static class PredictorMethod {
		public Long model_id;
		public String name;
		
		public PredictorMethod(Long modelId, String name) {
			this.model_id = modelId;
			this.name = name;
		}
	}
	
	private static final String PREDICT_API_URL = "https://hazard-dev.sciencedataexperts.com/api/predictor/predict";
	private static final double SAMPLE_FRACTION = 0.1; // Percent of dataset to sample
	private static final int SAMPLE_MAX = 100; // Cap on dataset sample size
	public static final double COMPARISON_TOLERANCE = 0.000001;
	
	public static Map<String, Prediction> getDatabasePredictions(Long modelId) {
		PredictionService predictionService = new PredictionServiceImpl();
		List<Prediction> predictions = predictionService.findByModelId(modelId);
		return predictions.stream().collect(Collectors.toMap(p -> p.getCanonQsarSmiles(), p -> p));
	}
	
	public static List<PredictorChemicalValues> getPredictorPredictionsSample(Long modelId) {
		ModelService modelService = new ModelServiceImpl();
		Model model = modelService.findById(modelId);
		String datasetName = model.getDatasetName();
		
		DatasetService datasetService = new DatasetServiceImpl();
		Dataset dataset = datasetService.findByName(datasetName);
		PredictorDataset predictorDataset = new PredictorDataset(dataset.getId());
		predictorDataset.addMethod(new PredictorMethod(modelId, "pred"));
		
		List<PredictorChemical> predictorChemicals = getDatasetSmiles(datasetName).stream()
				.map(s -> new PredictorChemical(s))
				.collect(Collectors.toList());
		
		int sampleSize = Math.min(SAMPLE_MAX, (int) Math.round(SAMPLE_FRACTION * predictorChemicals.size()));
		Collections.shuffle(predictorChemicals);
		predictorChemicals = predictorChemicals.subList(0, sampleSize);
		
		PredictorRequest request = new PredictorRequest();
		request.addDataset(predictorDataset);
		request.chemicals = predictorChemicals;
		
		String body = Unirest.post(PREDICT_API_URL)
				.header("Content-Type", "application/json")
				.header("Accept", "*/*")
				.header("Accept-Encoding", "gzip, deflate, br")
				.body(request)
				.asString()
				.getBody();
		
		PredictorResponse[] response = new Gson().fromJson(body, PredictorResponse[].class);
		return response[0].chemicals;
	}
	
	public static void comparePredictions(Long modelId) {
		List<PredictorChemicalValues> predictorPredictions = getPredictorPredictionsSample(modelId);
		Map<String, Prediction> databasePredictions = getDatabasePredictions(modelId);
		
		for (PredictorChemicalValues predictorPrediction:predictorPredictions) {
			String smiles = predictorPrediction.chemical.smiles;
			Double predictorPred = predictorPrediction.values.get("pred");
			Double databasePred = databasePredictions.get(smiles).getQsarPredictedValue();
			
			System.out.println(String.join("\t", smiles, String.valueOf(databasePred), String.valueOf(predictorPred)));
		}
	}
	
	public static List<String> getDatasetSmiles(String datasetName) {
		DataPointService dataPointService = new DataPointServiceImpl();
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);
		return dataPoints.stream().map(dp -> dp.getCanonQsarSmiles()).collect(Collectors.toList());
	}
	
	public static void main(String[] args) {
		Unirest.config().connectTimeout(0).socketTimeout(0);
		comparePredictions(151L);
	}

}
