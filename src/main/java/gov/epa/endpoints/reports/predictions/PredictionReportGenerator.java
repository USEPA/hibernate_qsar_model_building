package gov.epa.endpoints.reports.predictions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSet;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionService;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionServiceImpl;
import gov.epa.endpoints.reports.ReportGenerator;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportDataPoint;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportMetadata;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportModelMetadata;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportModelStatistic;

public class PredictionReportGenerator extends ReportGenerator {
	private DatasetService datasetService;
	private DescriptorSetService descriptorSetService;
	private DataPointService dataPointService;
	private DescriptorValuesService descriptorValuesService;
	private PredictionService predictionService;
	private DataPointInSplittingService dataPointInSplittingService;
	private ModelService modelService;
	private ModelStatisticService modelStatisticService;
	private ModelSetService modelSetService;
	
	private PredictionReport predictionReport;
	private Map<String, Integer> splittingMap;
	
	public PredictionReportGenerator() {
		super();
		datasetService = new DatasetServiceImpl();
		descriptorSetService = new DescriptorSetServiceImpl();
		dataPointService = new DataPointServiceImpl();
		descriptorValuesService = new DescriptorValuesServiceImpl();
		predictionService = new PredictionServiceImpl();
		dataPointInSplittingService = new DataPointInSplittingServiceImpl();
		modelService = new ModelServiceImpl();
		modelStatisticService = new ModelStatisticServiceImpl();
		modelSetService = new ModelSetServiceImpl();
	}
	
	private void initPredictionReport(String datasetName, String splittingName) {
		this.predictionReport = new PredictionReport();
		
		Dataset dataset = datasetService.findByName(datasetName);
		this.predictionReport.predictionReportMetadata = new PredictionReportMetadata(datasetName, 
				dataset.getDescription(),
				dataset.getProperty().getName(), 
				dataset.getProperty().getDescription(),
				dataset.getUnit().getName(),
				splittingName);
		
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);
		List<PredictionReportDataPoint> predictionReportData = dataPoints.stream()
				.map(dp -> new PredictionReportDataPoint(dp))
				.collect(Collectors.toList());
		this.predictionReport.predictionReportDataPoints = predictionReportData;
		System.out.println(predictionReportData.size());
		
		List<DataPointInSplitting> dataPointsInSplitting = 
				dataPointInSplittingService.findByDatasetNameAndSplittingName(datasetName, splittingName);
		this.splittingMap = dataPointsInSplitting.stream()
				.collect(Collectors.toMap(dpis -> dpis.getDataPoint().getCanonQsarSmiles(), dpis -> dpis.getSplitNum()));
	}
	
	private void addAllPredictions() {
		List<Model> models = modelService.findByDatasetName(predictionReport.predictionReportMetadata.datasetName);
		models = models.stream()
				.filter(m -> m.getSplittingName().equals(predictionReport.predictionReportMetadata.splittingName))
				.collect(Collectors.toList());
		for (Model model:models) {
			addModelPredictionsAndMetadata(model);
		}
	}
	
	private void addModelSetPredictions(String modelSetName) {
		ModelSet modelSet = modelSetService.findByName(modelSetName);
		List<Model> models = modelService.findByModelSetId(modelSet.getId());
		models = models.stream()
				.filter(m -> m.getDatasetName().equals(predictionReport.predictionReportMetadata.datasetName))
				.filter(m -> m.getSplittingName().equals(predictionReport.predictionReportMetadata.splittingName))
				.collect(Collectors.toList());
		for (Model model:models) {
			addModelPredictionsAndMetadata(model);
		}
	}
	
	private void addModelPredictionsAndMetadata(Model model) {
		if (model==null) {
			return;
		}
		
		PredictionReportModelMetadata modelMetadata = new PredictionReportModelMetadata(model.getId(), model.getMethod().getName(),
				model.getMethod().getDescription(), model.getDescriptorSetName());
		List<ModelStatistic> modelStatistics = modelStatisticService.findByModelId(model.getId());
		modelMetadata.predictionReportModelStatistics = modelStatistics.stream()
				.map(ms -> new PredictionReportModelStatistic(ms.getStatistic().getName(), ms.getStatisticValue()))
				.collect(Collectors.toList());
		predictionReport.predictionReportModelMetadata.add(modelMetadata);
		
		Method method = model.getMethod();
		List<Prediction> modelPredictions = predictionService.findByModelId(model.getId());
		Map<String, Prediction> modelPredictionsMap = modelPredictions.stream()
				.collect(Collectors.toMap(p -> p.getCanonQsarSmiles(), p -> p));
		for (PredictionReportDataPoint data:predictionReport.predictionReportDataPoints) {
			Prediction pred = modelPredictionsMap.get(data.canonQsarSmiles);
			if (pred!=null) {
				data.qsarPredictedValues.add(new QsarPredictedValue(method.getName(), 
						pred.getQsarPredictedValue(), splittingMap.get(data.canonQsarSmiles)));
			} else {
				data.qsarPredictedValues.add(new QsarPredictedValue(method.getName(), 
						null, splittingMap.get(data.canonQsarSmiles)));
			}
		}
	}
	
	public PredictionReport generateForAllPredictions(String datasetName, String splittingName) {
		initPredictionReport(datasetName, splittingName);
		addOriginalCompounds(predictionReport.predictionReportDataPoints);
		addAllPredictions();
		return predictionReport;
	}
	
	public PredictionReport generateForModelSetPredictions(String datasetName, String splittingName,
			String modelSetName) {
		initPredictionReport(datasetName, splittingName);
		addOriginalCompounds(predictionReport.predictionReportDataPoints);
		addModelSetPredictions(modelSetName);
		return predictionReport;
	}
	
	public PredictionReport generateForModelPredictions(Long modelId) {
		Model model = modelService.findById(modelId);
		initPredictionReport(model.getDatasetName(), model.getSplittingName());
		addOriginalCompounds(predictionReport.predictionReportDataPoints);
		addModelPredictionsAndMetadata(model);
		return predictionReport;
	}
	
	void createAllReports () {
		
//		String [] datasets= {"LC50 TEST","IGC50 TEST","LC50DM TEST",
//				"LD50 TEST","LLNA TEST","Mutagenicity TEST","LogBCF OPERA",
//				"LogHalfLife OPERA","LogKmHL OPERA","LogKOA OPERA","LogKOC OPERA","LogOH OPERA",
//				"Water solubility OPERA","Melting point OPERA","Vapor pressure OPERA","Octanol water partition coefficient OPERA"};

		
		String [] datasets= {"LC50DM TEST",
				"LD50 TEST","LLNA TEST","Mutagenicity TEST","LogBCF OPERA",
				"LogHalfLife OPERA","LogKmHL OPERA","LogKOA OPERA","LogKOC OPERA","LogOH OPERA",
				"Water solubility OPERA","Melting point OPERA"};

		
		String descriptorSetName = "T.E.S.T. 5.1";

		
		for (String datasetName:datasets) {
			String splittingName = datasetName.substring(datasetName.lastIndexOf(" ") + 1);
			
			String filePath = "data/reports/"+ datasetName + "_" + descriptorSetName + "_" + splittingName + "_PredictionReport.json";
			
			File file = new File(filePath);
			if (file.getParentFile()!=null) {
				file.getParentFile().mkdirs();
			}
			
			long t1=System.currentTimeMillis();
			
			PredictionReport report=generateForAllPredictions(datasetName, splittingName);
			
			long t2=System.currentTimeMillis();
			
			double time=(t2-t1)/1000.0;
			System.out.println("Time to generate report = "+time+" seconds");

			
			Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
				writer.write(gson.toJson(report));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	public static void main(String[] args) {
		PredictionReportGenerator gen = new PredictionReportGenerator();
		gen.createAllReports();
		
		if (true) return;
		
		
//		String datasetName = "LC50 TEST";
		String datasetName = "IGC50 TEST";
//		String datasetName = "LC50 DM TEST";
//		String datasetName = "LD50 TEST";
//		String datasetName = "LLNA TEST";
//		String datasetName = "Mutagenicity TEST";
//		String datasetName = "LogBCF OPERA";
//		String datasetName = "LogHalfLife OPERA";
//		String datasetName = "LogKmHL OPERA";
//		String datasetName = "LogKOA OPERA";
//		String datasetName = "LogKOC OPERA";
//		String datasetName = "LogOH OPERA";
//		String datasetName = "Water solubility OPERA";
//		String datasetName = "Melting point OPERA";
//		String datasetName = "Vapor pressure OPERA";
//		String datasetName = "Octanol water partition coefficient OPERA";
		
		String splittingName = "TEST";
//		String splittingName = "OPERA";
		
		String descriptorSetName = "T.E.S.T. 5.1";
		
		String filePath = "data/reports/"+ datasetName + "_" + descriptorSetName + "_PredictionReport.json";
		
		File file = new File(filePath);
		if (file.getParentFile()!=null) {
			file.getParentFile().mkdirs();
		}
		
		long t1=System.currentTimeMillis();
		
		PredictionReport report=gen.generateForAllPredictions(datasetName, splittingName);
		
		long t2=System.currentTimeMillis();
		
		double time=(t2-t1)/1000.0;
		System.out.println("Time to generate report = "+time+" seconds");

		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			writer.write(gson.toJson(report));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
