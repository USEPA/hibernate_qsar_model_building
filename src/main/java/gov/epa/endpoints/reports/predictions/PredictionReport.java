package gov.epa.endpoints.reports.predictions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.endpoints.reports.ReportDataPoint;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.reports.ModelMetadata;

public class PredictionReport {

//	public Boolean hasAD=false;
	
	public PredictionReportMetadata predictionReportMetadata;
	public List<PredictionReportModelMetadata> predictionReportModelMetadata = new ArrayList<PredictionReportModelMetadata>();
	public List<PredictionReportDataPoint> predictionReportDataPoints = new ArrayList<PredictionReportDataPoint>();

	
	public static class PredictionReportMetadata {
		public String datasetName;
		public String datasetDescription;
		public String datasetProperty;
		public String datasetPropertyDescription;
		public String datasetUnit;
		public String splittingName;
		public String AD;

		
		public PredictionReportMetadata(String datasetName, String datasetDescription, String datasetProperty, String datasetPropertyDescription, 
				String datasetUnit, String splittingName) {
			this.datasetName = datasetName;
			this.datasetDescription = datasetDescription;
			this.datasetProperty = datasetProperty;
			this.datasetPropertyDescription = datasetPropertyDescription;
			this.datasetUnit = datasetUnit;
			this.splittingName = splittingName;
		}
	}
	
	public static class PredictionReportModelMetadata extends ModelMetadata {
		public List<PredictionReportModelStatistic> predictionReportModelStatistics = new ArrayList<PredictionReportModelStatistic>();
		
		public PredictionReportModelMetadata(Long modelId, String qsarMethodName, String qsarMethodDescription, String descriptorSetName,
				String descriptorEmbeddingName, String descriptorEmbeddingTsv) {
			super(modelId, qsarMethodName, qsarMethodDescription, descriptorSetName, descriptorEmbeddingName, descriptorEmbeddingTsv);
		}
	}
	
	public static class PredictionReportModelStatistic {
		public String statisticName;
		public Double statisticValue;
		
		public PredictionReportModelStatistic(String statisticName, Double statisticValue) {
			this.statisticName = statisticName;
			this.statisticValue = statisticValue;
		}
	}
	
	public static class PredictionReportDataPoint extends ReportDataPoint {
		public Double experimentalPropertyValue;
		public List<QsarPredictedValue> qsarPredictedValues = new ArrayList<QsarPredictedValue>();
		public String descriptorValues;
		public String errorMessage; // in case prediction couldnt be generated due to descriptor generation error for example

		public PredictionReportDataPoint(DataPoint dp) {
			this.canonQsarSmiles = dp.getCanonQsarSmiles();
			this.experimentalPropertyValue = dp.getQsarPropertyValue();
			this.qsar_dtxcid=dp.getQsar_dtxcid();
			
//			String []ids=dp.getQsar_exp_prop_id().split("\\|");
//			
//			this.qsar_exp_prop_id="";
//			
//			for (int i=0;i<ids.length;i++) {
//				String intId=Integer.parseInt(ids[i].replace("EXP", ""))+"";
//				this.qsar_exp_prop_id+=intId;
//				if (i<ids.length-1) this.qsar_exp_prop_id+="|";
//			}
			
			
			this.qsar_exp_prop_property_values_id=dp.getQsar_exp_prop_property_values_id();
			
		}
	}
	
	
	public void toFile(String filePath) {
		
		File file = new File(filePath);
		if (file.getParentFile()!=null) {
			file.getParentFile().mkdirs();
//			System.out.println(file.getAbsolutePath());
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().disableHtmlEscaping().create();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			writer.write(gson.toJson(this));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
