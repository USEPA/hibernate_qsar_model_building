package gov.epa.endpoints.reports.predictions;

import java.util.ArrayList;
import java.util.List;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.endpoints.reports.ReportDataPoint;

public class PredictionReport {
	
	public static class PredictionReportMetadata {
		public String datasetName;
		public String datasetProperty;
		public String datasetUnit;
		public String descriptorSetName;
		public String descriptorSetHeader;
		
		public PredictionReportMetadata(String datasetName, String datasetProperty, String datasetUnit, 
				String descriptorSetName, String descriptorSetHeader) {
			this.datasetName = datasetName;
			this.datasetProperty = datasetProperty;
			this.datasetUnit = datasetUnit;
			this.descriptorSetName = descriptorSetName;
			this.descriptorSetHeader = descriptorSetHeader;
		}
	}
	
	public static class PredictionReportDataPoint extends ReportDataPoint {
		public Double experimentalPropertyValue;
		public List<QsarPredictedValue> qsarPredictedValues = new ArrayList<QsarPredictedValue>();
		public String descriptorValues;
		public String errorMessage;//in case prediction couldnt be generated due to descriptor generation error for example

		public PredictionReportDataPoint(DataPoint dp) {
			this.canonQsarSmiles = dp.getCanonQsarSmiles();
			this.experimentalPropertyValue = dp.getQsarPropertyValue();
		}
	}
	
	public PredictionReportMetadata predictionReportMetadata;
	public List<PredictionReportDataPoint> predictionReportDataPoints = new ArrayList<PredictionReportDataPoint>();
}
