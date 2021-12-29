package gov.epa.endpoints.reports.descriptors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesServiceImpl;
import gov.epa.endpoints.reports.ReportGenerator;
import gov.epa.endpoints.reports.descriptors.DescriptorReport.DescriptorReportDataPoint;
import gov.epa.endpoints.reports.descriptors.DescriptorReport.DescriptorReportMetadata;
import gov.epa.endpoints.reports.descriptors.DescriptorReport.DescriptorSetMetadata;
import gov.epa.endpoints.reports.descriptors.DescriptorReport.DescriptorSetValues;

public class DescriptorReportGenerator extends ReportGenerator {
	private DatasetService datasetService;
	private DataPointService dataPointService;
	private DescriptorValuesService descriptorValuesService;
	
	private DescriptorReport descriptorReport;
	
	public DescriptorReportGenerator() {
		super();
				
		this.descriptorReport = new DescriptorReport();
		
		datasetService = new DatasetServiceImpl();
		dataPointService = new DataPointServiceImpl();
		descriptorValuesService = new DescriptorValuesServiceImpl();
	}
	
	public void initDescriptorReport(String datasetName) {
		Dataset dataset = datasetService.findByName(datasetName);
		descriptorReport.descriptorReportMetadata = new DescriptorReportMetadata(datasetName, 
				dataset.getProperty().getName(), 
				dataset.getUnit().getName());
		
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);
		List<DescriptorReportDataPoint> descriptorReportData = dataPoints.stream()
				.map(dp -> new DescriptorReportDataPoint(dp))
				.collect(Collectors.toList());
		descriptorReport.descriptorReportDataPoints = descriptorReportData;
	}
	
	private void addAllDescriptorValues() {
		Set<String> descriptorSetNames = new HashSet<String>();
		for (DescriptorReportDataPoint dp:descriptorReport.descriptorReportDataPoints) {
			List<DescriptorValues> descriptorValues = descriptorValuesService.findByCanonQsarSmiles(dp.canonQsarSmiles);
			if (descriptorValues!=null) {
				for (DescriptorValues dv:descriptorValues) {
					String descriptorSetName = dv.getDescriptorSet().getName();
					if (descriptorSetNames.add(descriptorSetName)) {
						descriptorReport.descriptorReportMetadata.descriptorSetMetadata
							.add(new DescriptorSetMetadata(descriptorSetName, dv.getDescriptorSet().getHeadersTsv()));
					}
					dp.descriptorSetValues.add(new DescriptorSetValues(descriptorSetName, dv.getValuesTsv()));
				}
			}
		}
	}
	
	private void addDescriptorSetDescriptorValues(String descriptorSetName) {
		for (DescriptorReportDataPoint dp:descriptorReport.descriptorReportDataPoints) {
			DescriptorValues dv = descriptorValuesService.findByCanonQsarSmilesAndDescriptorSetName(dp.canonQsarSmiles, descriptorSetName);
			if (dv!=null) {
				if (descriptorReport.descriptorReportMetadata.descriptorSetMetadata.isEmpty()) {
					descriptorReport.descriptorReportMetadata.descriptorSetMetadata
						.add(new DescriptorSetMetadata(descriptorSetName, dv.getDescriptorSet().getHeadersTsv()));
				}
				
				dp.descriptorSetValues.add(new DescriptorSetValues(descriptorSetName, dv.getValuesTsv()));
			}
		}
	}
	
	public DescriptorReport generateForAllDescriptorSets(String datasetName) {
		initDescriptorReport(datasetName);
		addOriginalCompounds(descriptorReport.descriptorReportDataPoints);
		addAllDescriptorValues();
		return descriptorReport;
	}
	
	public DescriptorReport generateForDescriptorSet(String datasetName, String descriptorSetName) {
		initDescriptorReport(datasetName);
		addOriginalCompounds(descriptorReport.descriptorReportDataPoints);
		addDescriptorSetDescriptorValues(descriptorSetName);
		return descriptorReport;
	}
}
