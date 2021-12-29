package gov.epa.endpoints.reports.descriptors;

import java.util.ArrayList;
import java.util.List;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.endpoints.reports.ReportDataPoint;

public class DescriptorReport {
	
	public static class DescriptorReportMetadata {
		public String datasetName;
		public String datasetProperty;
		public String datasetUnit;
		public List<DescriptorSetMetadata> descriptorSetMetadata;
		
		public DescriptorReportMetadata(String datasetName, String datasetProperty, String datasetUnit) {
			this.datasetName = datasetName;
			this.datasetProperty = datasetProperty;
			this.datasetUnit = datasetUnit;
			this.descriptorSetMetadata = new ArrayList<DescriptorSetMetadata>();
		}
	}
	
	public static class DescriptorSetMetadata {
		public String descriptorSetName;
		public String descriptorSetHeader;
		
		public DescriptorSetMetadata(String descriptorSetName, String descriptorSetHeader) {
			this.descriptorSetName = descriptorSetName;
			this.descriptorSetHeader = descriptorSetHeader;
		}
	}
	
	public static class DescriptorReportDataPoint extends ReportDataPoint {
		public List<DescriptorSetValues> descriptorSetValues;
		
		public DescriptorReportDataPoint(DataPoint dp) {
			this.canonQsarSmiles = dp.getCanonQsarSmiles();
			this.descriptorSetValues = new ArrayList<DescriptorSetValues>();
		}
	}
	
	public static class DescriptorSetValues {
		public String descriptorSetName;
		public String descriptorValues;
		
		public DescriptorSetValues(String descriptorSetName, String descriptorValues) {
			this.descriptorSetName = descriptorSetName;
			this.descriptorValues = descriptorValues;
		}
	}
	
	public DescriptorReportMetadata descriptorReportMetadata;
	public List<DescriptorReportDataPoint> descriptorReportDataPoints;

}
