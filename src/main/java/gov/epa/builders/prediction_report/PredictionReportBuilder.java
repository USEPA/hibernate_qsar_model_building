package gov.epa.builders.prediction_report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.builders.prediction_report.PredictionReport.OriginalCompound;
import gov.epa.builders.prediction_report.PredictionReport.PredictionReportDataPoint;
import gov.epa.builders.prediction_report.PredictionReport.PredictionReportMetadata;
import gov.epa.builders.prediction_report.PredictionReport.QsarPredictedValue;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionService;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionServiceImpl;
import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.service.DsstoxCompoundService;
import gov.epa.databases.dsstox.service.DsstoxCompoundServiceImpl;
import kong.unirest.Unirest;

public class PredictionReportBuilder {
	DatasetService datasetService;
	DescriptorSetService descriptorSetService;
	DataPointService dataPointService;
	DescriptorValuesService descriptorValuesService;
	CompoundService compoundService;
	DsstoxCompoundService dsstoxCompoundService;
	PredictionService predictionService;
	DataPointInSplittingService dataPointInSplittingService;
	ModelService modelService;
	
	private PredictionReport predictionReport;
	
	private static Logger logger = LogManager.getLogger(PredictionReportBuilder.class);
	
	public PredictionReportBuilder() {
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
				
		this.predictionReport = new PredictionReport();
		
		datasetService = new DatasetServiceImpl();
		descriptorSetService = new DescriptorSetServiceImpl();
		dataPointService = new DataPointServiceImpl();
		descriptorValuesService = new DescriptorValuesServiceImpl();
		compoundService = new CompoundServiceImpl();
		dsstoxCompoundService = new DsstoxCompoundServiceImpl();
		predictionService = new PredictionServiceImpl();
		dataPointInSplittingService = new DataPointInSplittingServiceImpl();
		modelService = new ModelServiceImpl();
	}
	
	public void initPredictionReport(String datasetName, String descriptorSetName) {
		Dataset dataset = datasetService.findByName(datasetName);
		DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);
		predictionReport.predictionReportMetadata = new PredictionReportMetadata(datasetName, 
				dataset.getProperty().getName(), 
				dataset.getUnit().getName(),
				descriptorSetName,
				descriptorSet.getHeadersTsv());
		
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);
		List<PredictionReportDataPoint> predictionReportData = dataPoints.stream()
				.map(dp -> new PredictionReportDataPoint(dp))
				.collect(Collectors.toList());
		predictionReport.predictionReportDataPoints = predictionReportData;
	}
	
	public void addDescriptorValues() {
		for (PredictionReportDataPoint data:predictionReport.predictionReportDataPoints) {
			DescriptorValues dv = descriptorValuesService.findByCanonQsarSmilesAndDescriptorSetName(data.canonQsarSmiles, 
					predictionReport.predictionReportMetadata.descriptorSetName);
			if (dv!=null) {
				data.descriptorValues = dv.getValuesTsv();
			}
		}
	}
	
	public void addOriginalCompounds() {
		Set<String> allDtxcids = new HashSet<String>();
		Map<String, Set<String>> mapDtxcidsByCanonQsarSmiles = new HashMap<String, Set<String>>();
		for (PredictionReportDataPoint data:predictionReport.predictionReportDataPoints) {
			List<Compound> compounds = compoundService.findByCanonQsarSmiles(data.canonQsarSmiles);
			if (compounds!=null) {
				Set<String> dtxcids = compounds.stream().map(c -> c.getDtxcid()).collect(Collectors.toSet());
				allDtxcids.addAll(dtxcids);
				mapDtxcidsByCanonQsarSmiles.put(data.canonQsarSmiles, dtxcids);
			}
		}
		
		List<DsstoxRecord> dsstoxRecords = dsstoxCompoundService.findDsstoxRecordsByDtxcidIn(allDtxcids);
		Map<String, DsstoxRecord> mapDsstoxRecords = new HashMap<String, DsstoxRecord>();
		for (DsstoxRecord dr:dsstoxRecords) {
			if (allDtxcids.remove(dr.dsstoxCompoundId)) {
				mapDsstoxRecords.put(dr.dsstoxCompoundId, dr);
			}
		}
		
		for (PredictionReportDataPoint data:predictionReport.predictionReportDataPoints) {
			Set<String> dtxcids = mapDtxcidsByCanonQsarSmiles.get(data.canonQsarSmiles);
			for (String dtxcid:dtxcids) {
				DsstoxRecord dr = mapDsstoxRecords.get(dtxcid);
				if (dr!=null) {
					data.originalCompounds.add(new OriginalCompound(dtxcid, dr.casrn, dr.preferredName, dr.smiles));
				} else {
					System.out.println("DSSTox record not found for DTXCID: " + dtxcid);
				}
			}
		}
	}
	
	public void addAllPredictions() {
		List<Model> models = modelService.findByDatasetName(predictionReport.predictionReportMetadata.datasetName);
		for (Model model:models) {
			if (!model.getDescriptorSetName().equals(predictionReport.predictionReportMetadata.descriptorSetName)) {
				continue;
			}
			
			List<DataPointInSplitting> dataPointsInSplitting = 
					dataPointInSplittingService.findByDatasetNameAndSplittingName(predictionReport.predictionReportMetadata.datasetName, 
					model.getSplittingName());
			Map<String, Integer> splittingMap = dataPointsInSplitting.stream()
					.collect(Collectors.toMap(dpis -> dpis.getDataPoint().getCanonQsarSmiles(), dpis -> dpis.getSplitNum()));
			
			String modelMethodName = model.getMethod().getName();
			List<Prediction> modelPredictions = predictionService.findByModelId(model.getId());
			Map<String, Prediction> modelPredictionsMap = modelPredictions.stream()
					.collect(Collectors.toMap(p -> p.getCanonQsarSmiles(), p -> p));
			for (PredictionReportDataPoint data:predictionReport.predictionReportDataPoints) {
				Prediction pred = modelPredictionsMap.get(data.canonQsarSmiles);
				if (pred!=null) {
					data.qsarPredictedValues.add(new QsarPredictedValue(modelMethodName, pred.getQsarPredictedValue(), 
							splittingMap.get(data.canonQsarSmiles)));
				} else {
					data.qsarPredictedValues.add(new QsarPredictedValue(modelMethodName, null, splittingMap.get(data.canonQsarSmiles)));
				}
			}
		}
	}
	
	public void addModelPredictions(Long modelId) {
		Model model = modelService.findById(modelId);
		
		List<DataPointInSplitting> dataPointsInSplitting = 
				dataPointInSplittingService.findByDatasetNameAndSplittingName(predictionReport.predictionReportMetadata.datasetName, 
				model.getSplittingName());
		Map<String, Integer> splittingMap = dataPointsInSplitting.stream()
				.collect(Collectors.toMap(dpis -> dpis.getDataPoint().getCanonQsarSmiles(), dpis -> dpis.getSplitNum()));
		
		String modelMethodName = model.getMethod().getName();
		List<Prediction> modelPredictions = predictionService.findByModelId(model.getId());
		Map<String, Prediction> modelPredictionsMap = modelPredictions.stream()
				.collect(Collectors.toMap(p -> p.getCanonQsarSmiles(), p -> p));
		for (PredictionReportDataPoint data:predictionReport.predictionReportDataPoints) {
			Prediction pred = modelPredictionsMap.get(data.canonQsarSmiles);
			if (pred!=null) {
				data.qsarPredictedValues.add(new QsarPredictedValue(modelMethodName, pred.getQsarPredictedValue(), 
						splittingMap.get(data.canonQsarSmiles)));
			} else {
				data.qsarPredictedValues.add(new QsarPredictedValue(modelMethodName, null, splittingMap.get(data.canonQsarSmiles)));
			}
		}
	}
	
	public PredictionReport buildWithAllPredictions(String datasetName, String descriptorSetName) {
		initPredictionReport(datasetName, descriptorSetName);
		addOriginalCompounds();
		addDescriptorValues();
		addAllPredictions();
		return predictionReport;
	}
	
	public PredictionReport buildWithModelPredictions(Long modelId) {
		Model model = modelService.findById(modelId);
		initPredictionReport(model.getDatasetName(), model.getDescriptorSetName());
		addOriginalCompounds();
		addDescriptorValues();
		addModelPredictions(modelId);
		return predictionReport;
	}
}
