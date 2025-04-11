package gov.epa.run_from_java.scripts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelFile;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSet;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelFileServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetServiceImpl;
import gov.epa.endpoints.reports.WebTEST.GenerateWebTestReport;
import gov.epa.endpoints.reports.model_sets.ModelSetTable;
import gov.epa.endpoints.reports.model_sets.ModelSetTable.ModelSetTableRow;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportModelMetadata;
import gov.epa.endpoints.reports.predictions.ExcelReports.ExcelPredictionReportGenerator;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

public class SampleReportWriter {


	ModelFileServiceImpl mfs = new ModelFileServiceImpl();
	QsarModelsScript qms = new QsarModelsScript("tmarti02");		
	ModelSetServiceImpl mss = new ModelSetServiceImpl();
	ExcelPredictionReportGenerator eprg = new ExcelPredictionReportGenerator();
	ApplicabilityDomainScript ads=new ApplicabilityDomainScript();

	
	public PredictionReport createPredictionReport(String modelSetName, String datasetName, 
			String splittingName,boolean overWriteJsonReport, boolean includeDescriptors) {
		
		PredictionReport predictionReport = null;

		String filepathReport = "data/reports/" + modelSetName +"/"+ datasetName + "_PredictionReport.json";
		File reportFile = new File(filepathReport);

		
		if(!reportFile.exists() || overWriteJsonReport) {
			predictionReport = ReportGenerationScript.reportAllPredictions(datasetName, splittingName, modelSetName,
					true,includeDescriptors);
			System.out.println("Created:" + filepathReport);
		} else {
			predictionReport = GenerateWebTestReport.loadDataSetFromJson(filepathReport);
			System.out.println("Loaded:" + filepathReport);
		}

		return predictionReport;
		
	}
	

	/**
	 * Generates prediction report and excel file with AD results
	 * 
	 * @param modelSetName
	 * @param descriptorSetName
	 * @param methodName
	 * @param datasetName
	 * @param splittingName
	 * @param overWriteJsonReport
	 * @param includeDescriptors
	 * @param includeOriginalCompounds
	 * @return
	 */
	public PredictionReport createPredictionReportMethod(String modelSetName, String descriptorSetName, String methodName, String datasetName, 
			String splittingName,boolean overWriteJsonReport, boolean includeDescriptors,boolean includeOriginalCompounds) {
		

//		String listName = "PFASSTRUCTV4";
//		String folder = "data/dev_qsar/dataset_files/";
//		String filePathPFAS = folder + listName + "_qsar_ready_smiles.txt";// TODO pass as parameter
//		HashSet<String> smilesArray = SplittingGeneratorPFAS_Script.getPFASSmiles(filePathPFAS);

		
		PredictionReport predictionReport = null;

		String filepathReport = "data/reports/" + modelSetName +"/"+ datasetName+"_"+methodName + "_PredictionReport.json";
		File reportFile = new File(filepathReport);

		
		if(!reportFile.exists() || overWriteJsonReport) {
			predictionReport = ReportGenerationScript.reportPredictionsMethod(modelSetName,datasetName, splittingName, methodName,
					true,includeDescriptors,includeOriginalCompounds,filepathReport,false);
			System.out.println("Created:" + filepathReport);
		} else {
			predictionReport = GenerateWebTestReport.loadDataSetFromJson(filepathReport);
			System.out.println("Loaded:" + filepathReport);
		}

//		if(limitToPFAS) {
//			PredictionStatisticsScript.limitPredictionReportToPFAS(smilesArray, predictionReport);
//		}
		
		String outputFolder = "data/reports/prediction reports upload/"+modelSetName;
		
		File f = new File(outputFolder);
		if (!f.exists())
			f.mkdirs();


		String applicability_domain=null;
		if(modelSetName.equals("WebTEST2.0")) {
			applicability_domain=DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Euclidean;
		} else if(modelSetName.equals("WebTEST2.1")) {
			applicability_domain=DevQsarConstants.Applicability_Domain_TEST_Embedding_Euclidean;
		}
		
		ads.runAD(methodName, splittingName, descriptorSetName, applicability_domain,datasetName,predictionReport);

//		if(true)return null;
		
		String fileNameReport=datasetName+"_"+methodName + "_PredictionReport_with_AD.json";
		
//		if(limitToPFAS)fileNameReport=fileNameReport.replace(".json", "_only_PFAS.json");
		
		filepathReport = "data/reports/" + modelSetName +"/"+fileNameReport ;
		predictionReport.toFile(filepathReport);
		
		System.out.println(filepathReport);
		
		String fileNameExcel=String.join("_", datasetName, splittingName, methodName,"with_AD") + ".xlsx";
		
//		if(limitToPFAS)fileNameExcel=fileNameReport.replace(".xlsx", "_only_PFAS.xlsx");
		
		String filepathExcel = outputFolder + File.separator + fileNameExcel;
		createExcelReport(methodName, predictionReport, filepathExcel, overWriteJsonReport);
		
//		System.out.println(filepathExcel);
		

		// Copy excel files to paper folder:		
		try {
			
			String folderDest="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2023 8.4.11 papers\\00000 2025 paper\\";
			folderDest+=modelSetName+"\\";
			new File(folderDest).mkdirs();

			Files.copy(Paths.get(filepathExcel),
			        Paths.get(folderDest+fileNameExcel), StandardCopyOption.REPLACE_EXISTING);
			
			Files.copy(Paths.get(filepathReport),
			        Paths.get(folderDest+fileNameReport), StandardCopyOption.REPLACE_EXISTING);
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		
		return predictionReport;
		
	}
	
	
	public static PredictionReport getReport(String modelSetName, String datasetName, String splittingName) {
		
		String filepathReport = "data/reports/" + modelSetName + "/" + datasetName + "_PredictionReport.json";
		File reportFile = new File(filepathReport);

		if (reportFile.exists()) {
			return GenerateWebTestReport.loadDataSetFromJson(filepathReport);
		} else {
			System.out.println("JSON report doesnt exist at "+filepathReport); 
			return null;
		}
	}
	
	public static PredictionReport getReport(String modelSetName, String datasetName, String splittingName,boolean limitToPFAS) {
		String filepathReport;
		
		if (limitToPFAS) {
			filepathReport = "data/reports/" + modelSetName + "/" + datasetName + "_PredictionReport_only_PFAS.json";
		} else {
			filepathReport = "data/reports/" + modelSetName + "/" + datasetName + "_PredictionReport.json";
		}
		
		File reportFile = new File(filepathReport);

		if (reportFile.exists()) {
			return GenerateWebTestReport.loadDataSetFromJson(filepathReport);
		} else {
			System.out.println("JSON report doesnt exist at "+filepathReport); 
			return null;
		}
	}
	
	public static PredictionReport getReport(String filepathReport) {
		
		File reportFile = new File(filepathReport);

		if (reportFile.exists()) {
			return GenerateWebTestReport.loadDataSetFromJson(filepathReport);
		} else {
			System.out.println("JSON report doesnt exist at "+filepathReport); 
			return null;
		}
	}
	
	public static void writeReportWithAD(String modelSetName, String datasetName, String splittingName,boolean limitToPFAS,PredictionReport pr) {
		String filepathReport;
		if (limitToPFAS) {
			filepathReport = "data/reports/" + modelSetName + "/" + datasetName + "_PredictionReport_only_PFAS_with_AD.json";
		} else {
			filepathReport = "data/reports/" + modelSetName + "/" + datasetName + "_PredictionReport_with_AD.json";
		}
		Utilities.saveJson(pr, filepathReport);
	}


	
	public PredictionReport createPredictionReport(long modelSetID, String datasetName, 
			String splittingName,boolean overWriteJsonReport,boolean includeDescriptors) {
		ModelSetServiceImpl m = new ModelSetServiceImpl();
		String modelSetName = m.findById(modelSetID).getName();		
		return createPredictionReport(modelSetName, datasetName, splittingName,overWriteJsonReport,includeDescriptors);
	}
	
	public void generateSamplePredictionReports(long modelSetID, boolean upload, boolean deleteExistingReportInDatabase) {

		ModelSetTable table = SampleModelQmrfWriter.getModelsInModelSet(modelSetID);
		
		for (ModelSetTableRow msRow : table.modelSetTableRows) {			
			generateSamplePredictionReport(modelSetID, msRow.datasetName, msRow.splittingName, upload, deleteExistingReportInDatabase, false,false,false);
		}
	}
	
	public String createExcelReport (long modelSetId,String datasetName, String splittingName,PredictionReport predictionReport,boolean overwrite) {

		String modelSetName = mss.findById(modelSetId).getName();
		
		String outputFolder = "data/reports/prediction reports upload/"+modelSetName;
		
		File f = new File(outputFolder);
		if (!f.exists())
			f.mkdirs();

		String filepath = outputFolder + File.separator + String.join("_", datasetName, splittingName) + ".xlsx";
		
		File excelFile=new File(filepath);
		
		if (overwrite || !excelFile.exists()) {
			ExcelPredictionReportGenerator eprg = new ExcelPredictionReportGenerator();
			eprg.generate(predictionReport, filepath,null,null);
		} else {
			System.out.println("Excel report already exists at "+filepath);
		}
		
		return filepath;
	}

	public void createExcelReport (String methodName, PredictionReport predictionReport,String outputFilePath, boolean overwrite) {
		File excelFile=new File(outputFilePath);
		if (overwrite || !excelFile.exists()) {
			ExcelPredictionReportGenerator eprg = new ExcelPredictionReportGenerator();
			eprg.generate(predictionReport, outputFilePath,null,null);
		} else {
			System.out.println("Excel report already exists at "+outputFilePath);
		}
	}
		


	public void generateSamplePredictionReport(long modelSetID, String datasetName, String splittingName,
			boolean upload, boolean deleteExistingReportInDatabase, 
			boolean overWriteReportFiles, boolean overWriteExcelFiles, boolean includeDescriptors) {
		
		PredictionReport predictionReport=createPredictionReport(modelSetID, datasetName, splittingName,overWriteReportFiles,includeDescriptors);
		
		Long modelId=null;//modelId to associate report with
		Long fileTypeId=2L;//excel summary 
		
		for (PredictionReportModelMetadata mmd: predictionReport.predictionReportModelMetadata) {
			if(mmd.qsarMethodName.contains("consensus") || predictionReport.predictionReportModelMetadata.size()==1) {
				modelId=mmd.modelId;
				break;
			}
		}
		
		if(modelId==null) {
			System.out.println("Cant associate model for "+datasetName+"\t"+splittingName+"\tmodelSetId="+modelSetID);
			return;
		}
				
		String excelFilePath=createExcelReport(modelSetID, datasetName, splittingName, predictionReport,overWriteExcelFiles);
						
		ModelFile msr = mfs.findByModelId(modelId, fileTypeId);

		if (msr != null) {
			if (deleteExistingReportInDatabase) {
				mfs.delete(msr);
			
			} else {
				if (upload) {
					System.out.println(datasetName + " exists skipping!");
					return;// skip it we already did it						
				}
			}
		}

		if(!upload)return;
		
		try {
			qms.uploadModelFile(modelId,fileTypeId, excelFilePath);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	
	public void generateSamplePredictionReport(String methodName, ModelSet ms, String datasetName, String splittingName,
			boolean upload, boolean deleteExistingReportInDatabase, 
			boolean overWriteReportFiles, boolean overWriteExcelFiles, boolean includeDescriptors) {
		
		
		
//		Long modelId=null;//modelId to associate report with
//		Long fileTypeId=2L;//excel summary 
//		
//		for (PredictionReportModelMetadata mmd: predictionReport.predictionReportModelMetadata) {
//			if(mmd.qsarMethodName.contains("consensus") || predictionReport.predictionReportModelMetadata.size()==1) {
//				modelId=mmd.modelId;
//				break;
//			}
//		}
//		
//		if(modelId==null) {
//			System.out.println("Cant associate model for "+datasetName+"\t"+splittingName+"\tmodelSetId="+modelSetID);
//			return;
//		}
//				
//		String excelFilePath=createExcelReport(modelSetID, datasetName, splittingName, predictionReport,overWriteExcelFiles);
//						
//		ModelFile msr = mfs.findByModelId(modelId, fileTypeId);
//
//		if (msr != null) {
//			if (deleteExistingReportInDatabase) {
//				mfs.delete(msr);
//			
//			} else {
//				if (upload) {
//					System.out.println(datasetName + " exists skipping!");
//					return;// skip it we already did it						
//				}
//			}
//		}
//
//		if(!upload)return;
//		
//		try {
//			qms.uploadModelFile(modelId,fileTypeId, excelFilePath);
//		} catch (Exception e) {
//			e.printStackTrace();
//		} 
	}
	


	public static void main(String[] args) {

		SampleReportWriter g = new SampleReportWriter();

		boolean upload=false;
		boolean deleteExistingReportInDatabase=false;
		boolean overWriteReportFiles=false;

//		g.generateSamplePredictionReports(4L,false,false);
//		g.generateSamplePredictionReports(1L,true,true);
//		g.generateSamplePredictionReports(2L,true,true);
//		g.generateSamplePredictionReports(14L,false,false);
		
		
		// **************************************************************
//		 QsarModelsScript q=new QsarModelsScript("tmarti02");
//		 String datasetName="LC50DM TEST";
//		 String splittingName="TEST";
//		 q.downloadModelSetReport(1L, datasetName, splittingName,
//		 "data/reports/prediction reports download");

		// **************************************************************
//		 QsarModelsScript q=new QsarModelsScript("tmarti02");
//		// String datasetName="Standard Henry's law constant from exp_prop";
//		 String datasetName="Standard Water solubility from exp_prop";
//		 String splittingName="RND_REPRESENTATIVE";
//		 q.downloadModelSetReport(2L, datasetName, splittingName,
//		 "data/reports/prediction reports download");

	}

}
