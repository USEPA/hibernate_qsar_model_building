package gov.epa.run_from_java.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointContributor;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesServiceImpl;
import gov.epa.endpoints.models.ModelBuilder;
import gov.epa.endpoints.models.ModelData;

public class DatasetFileWriter {
	
	private DatasetService datasetService = new DatasetServiceImpl();
	private DataPointService dataPointService = new DataPointServiceImpl();
	private DescriptorValuesService descriptorValuesService = new DescriptorValuesServiceImpl();
	
	public String writeWithoutSplitting(String datasetName, String descriptorSetName, String outputFolderPath, boolean fetchDtxcids) {
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);
		List<DescriptorValues> descriptorValues = descriptorValuesService.findByDescriptorSetName(descriptorSetName);
		
		String outputFileName = datasetName + "_" + descriptorSetName + "_full.tsv";
		String outputFilePath = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileName;
		File outputFile = new File(outputFilePath);
		outputFile.getParentFile().mkdirs();
		
//		try {
//			Gson gson=new Gson();
//			String json=gson.toJson(dataPoints);
//			String jsonFilePath= outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileName.replace("tsv", ".json");
//			FileWriter fw=new FileWriter(jsonFilePath);
//			fw.write(json);
//			fw.flush();
//			fw.close();
//		} catch(Exception ex) {
//			ex.printStackTrace();
//		}
		
		String instances = ModelData.generateInstancesWithoutSplitting(dataPoints, descriptorValues, fetchDtxcids);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {
			bw.write(instances);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return instances;
	}
	
	public void writeWithSplitting(String descriptorSetName,String splittingName,String datasetName,String outputFolderPath) {
		ModelBuilder mb=new ModelBuilder("tmarti02");
				
		//Get training and test set instances as strings using TEST descriptors:
		ModelData md=mb.initModelData(datasetName, descriptorSetName,splittingName, false);

		File outputFolder = new File(outputFolderPath);
		outputFolder.mkdirs();

		String outputFileNameTraining = datasetName + "_" + descriptorSetName+"_"+splittingName + "_training.tsv";
		String outputFilePathTraining = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileNameTraining;
		
		String outputFileNamePrediction = datasetName + "_" + descriptorSetName+"_"+splittingName + "_prediction.tsv";
		String outputFilePathPrediction = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileNamePrediction;

		try {
			
			FileWriter fw=new FileWriter(outputFilePathTraining);
			fw.write(md.trainingSetInstances);
			fw.flush();
			fw.close();
			
			fw=new FileWriter(outputFilePathPrediction);
			fw.write(md.predictionSetInstances);
			fw.flush();
			fw.close();

			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		

		
	}
	
	
	public String writeWithoutSplitting(Long datasetId, String descriptorSetName, String outputFolderPath, boolean fetchDtxcids) {
		Dataset dataset = datasetService.findById(datasetId);
		if (dataset==null) {
			return datasetId+" not found";
		}
		
		return writeWithoutSplitting(dataset.getName(), descriptorSetName, outputFolderPath, fetchDtxcids);
	}
	
	public static void main(String[] args) {
		DatasetFileWriter writer = new DatasetFileWriter();
		
		String outputFolderPath="data/dev_qsar/dataset_files/";
		String descriptorSetName="T.E.S.T. 5.1";
		
//		writer.writeWithoutSplitting(38L, descriptorSetName, outputFolderPath);
//		writer.writeWithoutSplitting(36L, descriptorSetName, outputFolderPath");
//		writer.writeWithoutSplitting(42L, descriptorSetName, outputFolderPath);
//		writer.writeWithoutSplitting(43L, descriptorSetName, outputFolderPath, true);
//		writer.writeWithoutSplitting(34L, descriptorSetName, outputFolderPath, true);
//		writer.writeWithoutSplitting(31L, descriptorSetName, outputFolderPath, true);
		
		//TODO need to compile list of sources used in compiling a dataset
//		writer.writeWithSplitting(descriptorSetName,"RND_REPRESENTATIVE","Standard Water solubility from exp_prop",outputFolderPath);
//		writer.writeWithSplitting(descriptorSetName,PFAS_SplittingGenerator.splittingPFASOnly,"Standard Water solubility from exp_prop",outputFolderPath);
		writer.writeWithSplitting(descriptorSetName,PFAS_SplittingGenerator.splittingAll,"Standard Water solubility from exp_prop",outputFolderPath);
		writer.writeWithSplitting(descriptorSetName,PFAS_SplittingGenerator.splittingAllButPFAS,"Standard Water solubility from exp_prop",outputFolderPath);
		
	}

}
