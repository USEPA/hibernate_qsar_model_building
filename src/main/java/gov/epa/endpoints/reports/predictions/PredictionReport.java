package gov.epa.endpoints.reports.predictions;

import java.util.ArrayList;
import java.util.List;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.endpoints.reports.ReportDataPoint;
import gov.epa.endpoints.reports.ModelMetadata;

public class PredictionReport {
	
	public static class PredictionReportMetadata {
		public String datasetName;
		public String datasetDescription;
		public String datasetProperty;
		public String datasetPropertyDescription;
		public String datasetUnit;
		public String splittingName;
		
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
		
		public PredictionReportModelMetadata(Long modelId, String qsarMethodName, String qsarMethodDescription) {
			super(modelId, qsarMethodName, qsarMethodDescription);
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
		}
	}
	
	public PredictionReportMetadata predictionReportMetadata;
	public List<PredictionReportModelMetadata> predictionReportModelMetadata = new ArrayList<PredictionReportModelMetadata>();
	public List<PredictionReportDataPoint> predictionReportDataPoints = new ArrayList<PredictionReportDataPoint>();
}
