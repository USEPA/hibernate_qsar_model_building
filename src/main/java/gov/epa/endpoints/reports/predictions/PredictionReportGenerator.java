package gov.epa.endpoints.reports.predictions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DataPointDaoImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;

import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSet;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionService;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.SplittingServiceImpl;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.reports.ReportGenerator;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportDataPoint;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportMetadata;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportModelMetadata;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportModelStatistic;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

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
	private SplittingServiceImpl splittingService;
	
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
		splittingService=new SplittingServiceImpl();
	}
	
	private void initPredictionReport(String datasetName, String splittingName) {
		this.predictionReport = new PredictionReport();
		
		Dataset dataset = datasetService.findByName(datasetName);
		
		String units="";
		
		if(dataset.getUnit().getAbbreviation()!=null) units=dataset.getUnit().getAbbreviation();
		
		
		System.out.println(datasetName+"\t"+units);
		
		this.predictionReport.predictionReportMetadata = new PredictionReportMetadata(datasetName, 
				dataset.getDescription(),
				dataset.getProperty().getName(), 
				dataset.getProperty().getDescription(),
				units,
				splittingName);
		
//		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);
		
		DataPointDaoImpl dpdi=new DataPointDaoImpl();

		List<DataPoint> dataPoints = dpdi.findByDatasetNameSql(datasetName);
		
		List<PredictionReportDataPoint> predictionReportData = dataPoints.stream()
				.map(dp -> new PredictionReportDataPoint(dp))
				.collect(Collectors.toList());
		this.predictionReport.predictionReportDataPoints = predictionReportData;
//		System.out.println(predictionReportData.size());

		//No longer need it because have splits in prediction table:
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
				
		Collections.sort(models, (o1, o2) -> (int)(o1.getId() - o2.getId()));//sort in order so consensus last and also so can get the descriptor set name from the first model
		
		for (Model model:models) {
			
			System.out.println(model.getId()+"\t"+model.getMethod().getName());
			
			addModelPredictionsAndMetadata(model);
		}
	}
	

	
	private void addMethodPredictions(String methodName,boolean hasEmbedding) {
		
		List<Model> models = modelService.getAll();
		
		if (hasEmbedding) {
			models = models.stream()
					.filter(m -> m.getDatasetName().equals(predictionReport.predictionReportMetadata.datasetName))
					.filter(m -> m.getSplittingName().equals(predictionReport.predictionReportMetadata.splittingName))
					.filter(m -> m.getMethod().getName().contains(methodName))
					.filter(m ->m.getDescriptorEmbedding()!=null)
					.collect(Collectors.toList());
			
		} else {
			models = models.stream()
					.filter(m -> m.getDatasetName().equals(predictionReport.predictionReportMetadata.datasetName))
					.filter(m -> m.getSplittingName().equals(predictionReport.predictionReportMetadata.splittingName))
					.filter(m -> m.getMethod().getName().contains(methodName))
					.filter(m -> m.getDescriptorEmbedding()==null)
					.collect(Collectors.toList());
		}
		
				
		Collections.sort(models, (o1, o2) -> (int)(o1.getId() - o2.getId()));//sort in order so consensus last and also so can get the descriptor set name from the first model
		
		System.out.println("In addMethodPredicitions(), Model.size()="+models.size());
		
		if (models.size()>1) {
			
			System.out.println("Multiple models!!");
			
			for (Model model:models) {
				System.out.println(model.getId());
			}
			
			return;
		}
		
		for (Model model:models) {
			
			System.out.println(model.getId()+"\t"+model.getMethod().getName());
			
			addModelPredictionsAndMetadata(model);
		}
	}

	

	
	private void addModelPredictionsAndMetadata(Model model) {
		
		if (model==null) {
			return;
		}
		
		DescriptorEmbedding descriptorEmbedding = model.getDescriptorEmbedding();
		String descriptorEmbeddingName = null;
		String descriptorEmbeddingTsv = null;
		if (descriptorEmbedding!=null) {
			descriptorEmbeddingName = descriptorEmbedding.getName();
			descriptorEmbeddingTsv = descriptorEmbedding.getEmbeddingTsv();
		}
		
		PredictionReportModelMetadata modelMetadata = new PredictionReportModelMetadata(model.getId(), model.getMethod().getName(),
				model.getMethod().getDescription(), model.getDescriptorSetName(), descriptorEmbeddingName, descriptorEmbeddingTsv);
		List<ModelStatistic> modelStatistics = modelStatisticService.findByModelId(model.getId());
		modelMetadata.predictionReportModelStatistics = modelStatistics.stream()
				.map(ms -> new PredictionReportModelStatistic(ms.getStatistic().getName(), ms.getStatisticValue()))
				.collect(Collectors.toList());
		predictionReport.predictionReportModelMetadata.add(modelMetadata);
		
		Method method = model.getMethod();
		
		Splitting splitting=splittingService.findByName(model.getSplittingName());
		
		List<Prediction> modelPredictions = predictionService.findByIds(model.getId(),splitting.getId());
		
//		List<DataPoint>dataPoints=dataPointService.findByDatasetName(model.getDatasetName());

		
		Map<String, Prediction> modelPredictionsMap = modelPredictions.stream()
				.collect(Collectors.toMap(p -> p.getCanonQsarSmiles(), p -> p));
		
		for (PredictionReportDataPoint data:predictionReport.predictionReportDataPoints) {

			Prediction pred = modelPredictionsMap.get(data.canonQsarSmiles);
			
			if (splittingMap.get(data.canonQsarSmiles)==null) {
//				System.out.println("Missing split num for "+data.canonQsarSmiles);
				continue;
			}
			
			int splitNum=splittingMap.get(data.canonQsarSmiles);
			
			if(splitNum!=DevQsarConstants.TRAIN_SPLIT_NUM && splitNum!=DevQsarConstants.TEST_SPLIT_NUM) continue;
			
//			System.out.println(splitNum+"\t"+pred);
			
			if (pred!=null) {
				data.qsarPredictedValues.add(new QsarPredictedValue(method.getName(), 
						pred.getQsarPredictedValue(), splitNum));
			} else {
				data.qsarPredictedValues.add(new QsarPredictedValue(method.getName(), 
						null, splitNum));
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
			String modelSetName,boolean includeDescriptors) {
		initPredictionReport(datasetName, splittingName);
		addOriginalCompounds(predictionReport.predictionReportDataPoints);
		
		addModelSetPredictions(modelSetName);
		
		if(includeDescriptors) addDescriptors(datasetName);
		
		return predictionReport;
	}
	
	public PredictionReport generateMethodPredictions(String modelSetName, String datasetName, String splittingName,
			String methodName,boolean includeDescriptors,boolean includeOriginalCompounds) {
		
		initPredictionReport(datasetName, splittingName);
		
		if(includeOriginalCompounds)  addOriginalCompounds(predictionReport.predictionReportDataPoints);
		
		if(modelSetName.equals("WebTEST2.0")) {
			addMethodPredictions(methodName,false);	
		} else {
			addMethodPredictions(methodName,true);
		}
		
		if(includeDescriptors) addDescriptors(datasetName);
		
		return predictionReport;
	}
	
	
	
	public PredictionReport generateMethodPredictions(Model model,boolean includeDescriptors,
			boolean includeOriginalCompounds) {
		
		initPredictionReport(model.getDatasetName(), model.getSplittingName());
		if(includeOriginalCompounds)  addOriginalCompounds(predictionReport.predictionReportDataPoints);
		addModelPredictionsAndMetadata(model);
		if(includeDescriptors) addDescriptors(model.getDatasetName());
		
		return predictionReport;
	}


	
	private void addDescriptors(String datasetName) {
		
//		System.out.println(Utilities.gson.toJson(predictionReport));
		
		if(predictionReport.predictionReportModelMetadata.size()==0) return;
		
		String descriptorSetName=predictionReport.predictionReportModelMetadata.get(0).descriptorSetName;
		
		System.out.println(descriptorSetName);
		
		Hashtable<String,String>htDescriptors=ModelData.generateDescriptorHashtable(datasetName, descriptorSetName, false);

		for (PredictionReportDataPoint dp:predictionReport.predictionReportDataPoints) {
			dp.descriptorValues=htDescriptors.get(dp.canonQsarSmiles);
		}
		
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
