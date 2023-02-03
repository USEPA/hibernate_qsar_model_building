package gov.epa.run_from_java.scripts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionService;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.SplittingServiceImpl;
import kong.unirest.Unirest;

public class PredictorComparisonScript {
	
	public static class PredictorRequest {
		public List<PredictorChemical> chemicals = new ArrayList<PredictorChemical>();
		public List<PredictorDataset> datasets = new ArrayList<PredictorDataset>();
		public Long modelset_id;
		
		public void addDataset(PredictorDataset dataset) {
			this.datasets.add(dataset);
		}
	}

	
	public static class PredictorResponse {
		public List<PredictorChemicalValues> predictions;
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
	private static double SAMPLE_FRACTION = 0.1; // Percent of dataset to sample
	private static int SAMPLE_MAX = 100; // Cap on dataset sample size
	public static final double COMPARISON_TOLERANCE = 0.000001;
	
	public static Map<String, Prediction> getDatabasePredictions(Long modelId) {
		SplittingServiceImpl splittingService = new SplittingServiceImpl();
		Splitting splitting=splittingService.findByName(DevQsarConstants.SPLITTING_RND_REPRESENTATIVE);
		
		PredictionService predictionService = new PredictionServiceImpl();
		List<Prediction> predictions = predictionService.findByIds(modelId,splitting.getId());
		return predictions.stream().collect(Collectors.toMap(p -> p.getCanonQsarSmiles(), p -> p));
	}
	
	
	public static List<PredictorChemical> getSample(Long modelId, Long modelSetID) {
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
		
//		System.out.println("predictorChemicals:");
//		for (int i=0;i<predictorChemicals.size();i++) {
//			System.out.println(i+"\t"+predictorChemicals.get(i).smiles);
//		}
		
		
		int sampleSize = Math.min(SAMPLE_MAX, (int) Math.round(SAMPLE_FRACTION * predictorChemicals.size()));
		Collections.shuffle(predictorChemicals);
		predictorChemicals = predictorChemicals.subList(0, sampleSize);
		return predictorChemicals;
		
		
	}
	
	public static List<PredictorChemicalValues> getPredictorPredictionsSample(List<PredictorChemical>predictorChemicals,Long modelId, Long modelSetID) {
		ModelService modelService = new ModelServiceImpl();
		Model model = modelService.findById(modelId);
		String datasetName = model.getDatasetName();
		
		DatasetService datasetService = new DatasetServiceImpl();
		Dataset dataset = datasetService.findByName(datasetName);
		PredictorDataset predictorDataset = new PredictorDataset(dataset.getId());
		predictorDataset.addMethod(new PredictorMethod(modelId, "pred"));
						
		PredictorRequest request = new PredictorRequest();
		request.addDataset(predictorDataset);
		request.chemicals = predictorChemicals;
		request.modelset_id=modelSetID;
		
		System.out.println(new Gson().toJson(request));
		
		Unirest.config()
        .socketTimeout(0)
        .connectTimeout(0);
        
		String body = Unirest.post(PREDICT_API_URL)
				.header("Content-Type", "application/json")
				.header("Accept", "*/*")
				.header("Accept-Encoding", "gzip, deflate, br")
				.body(request)
				.asString()
				.getBody();
		
//		System.out.println(body);
		
		PredictorResponse[] response = new Gson().fromJson(body, PredictorResponse[].class);
		return response[0].predictions;
	}
	
	public static void comparePredictions(Long modelId,Long modelSetID) {
				
		List<PredictorChemical>sampleChemicals=getSample(modelId, modelSetID);		
		List<PredictorChemicalValues> predictorPredictions = getPredictorPredictionsSample(sampleChemicals,modelId,modelSetID);
		
		Map<String, Prediction> databasePredictions = getDatabasePredictions(modelId);
		double tol=1e-5;
		
		for (int i=0;i<predictorPredictions.size();i++) {
			
			PredictorChemicalValues pcv=predictorPredictions.get(i);
			String smiles = pcv.chemical.smiles;
			
			String smilesOriginal=sampleChemicals.get(i).smiles;
			
			//TODO need to set prediction abbrev based on modelID
			Double predictorPred = pcv.values.get("rf");//for now hardcoded to get it working
			
//			System.out.println(smiles+"\t"+smilesOriginal+"\t"+predictorPred);
			
			Double databasePred = databasePredictions.get(smilesOriginal).getQsarPredictedValue();
			if (Math.abs(predictorPred-databasePred)>tol)
				System.out.println(String.join("\t", smilesOriginal, smiles, String.valueOf(databasePred), String.valueOf(predictorPred)));
		}
	}
	
	public static List<String> getDatasetSmiles(String datasetName) {
		DataPointService dataPointService = new DataPointServiceImpl();
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);
		return dataPoints.stream().map(dp -> dp.getCanonQsarSmiles()).collect(Collectors.toList());
	}
	
	public static void main(String[] args) {
		
		PredictorComparisonScript.SAMPLE_MAX=200;
		Unirest.config().connectTimeout(0).socketTimeout(0);
//		PredictorComparisonScript.SAMPLE_FRACTION=0.1;
		comparePredictions(151L,2L);
	}

}
