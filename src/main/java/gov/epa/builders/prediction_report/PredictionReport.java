package gov.epa.builders.prediction_report;

import java.util.ArrayList;
import java.util.List;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;

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
	
	public static class PredictionReportDataPoint {
		public String canonQsarSmiles;
		public List<OriginalCompound> originalCompounds = new ArrayList<OriginalCompound>();
		public double experimentalPropertyValue;
		public List<QsarPredictedValue> qsarPredictedValues = new ArrayList<QsarPredictedValue>();
		
		public String descriptorValues;

		public PredictionReportDataPoint(DataPoint dp) {
			this.canonQsarSmiles = dp.getCanonQsarSmiles();
			this.experimentalPropertyValue = dp.getQsarPropertyValue();
		}
	}
	
	public static class QsarPredictedValue {
		public String qsarMethodName;
		public Double qsarPredictedValue;
		public int splitNum;
		
		public QsarPredictedValue(String qsarMethodName, Double qsarPredictedValue, int splitNum) {
			this.qsarMethodName = qsarMethodName;
			this.qsarPredictedValue = qsarPredictedValue;
			this.splitNum = splitNum;
		}
	}
	
	public static class OriginalCompound {
		public String dtxcid;
		public String casrn;
		public String preferredName;
		public String smiles;
		
		public OriginalCompound(String dtxcid, String casrn, String preferredName, String smiles) {
			this.dtxcid = dtxcid;
			this.casrn = casrn;
			this.preferredName = preferredName;
			this.smiles = smiles;
		}
	}
	
	public PredictionReportMetadata predictionReportMetadata;
	public List<PredictionReportDataPoint> predictionReportDataPoints = new ArrayList<PredictionReportDataPoint>();
}
