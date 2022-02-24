package gov.epa.run_from_java.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesServiceImpl;
import gov.epa.endpoints.models.ModelData;

public class DatasetFileWriter {
	
	private DatasetService datasetService = new DatasetServiceImpl();
	private DataPointService dataPointService = new DataPointServiceImpl();
	private DescriptorValuesService descriptorValuesService = new DescriptorValuesServiceImpl();
	
	public void writeWithoutSplitting(String datasetName, String descriptorSetName, String outputFolderPath) {
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);
		List<DescriptorValues> descriptorValues = descriptorValuesService.findByDescriptorSetName(descriptorSetName);
		
		String outputFileName = datasetName + "_" + descriptorSetName + "_full.tsv";
		String outputFilePath = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileName;
		File outputFile = new File(outputFilePath);
		outputFile.getParentFile().mkdirs();
		
		String instances = ModelData.generateInstancesWithoutSplitting(dataPoints, descriptorValues);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {
			bw.write(instances);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeWithoutSplitting(Long datasetId, String descriptorSetName, String outputFolderPath) {
		Dataset dataset = datasetService.findById(datasetId);
		if (dataset==null) {
			return;
		}
		
		writeWithoutSplitting(dataset.getName(), descriptorSetName, outputFolderPath);
	}
	
	public static void main(String[] args) {
		DatasetFileWriter writer = new DatasetFileWriter();
		writer.writeWithoutSplitting(38L, "T.E.S.T. 5.1", "data/dev_qsar/dataset_files/");
	}

}
