package gov.epa.run_from_java.scripts;

import java.io.File;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSetReport;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetReportServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetServiceImpl;
import gov.epa.endpoints.reports.ModelMetadata;
import gov.epa.endpoints.reports.model_sets.ModelSetTable;
import gov.epa.endpoints.reports.model_sets.ModelSetTable.ModelSetTableRow;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.endpoints.reports.predictions.PredictionReportGenerator;
import gov.epa.endpoints.reports.predictions.ExcelReports.ExcelPredictionReportGenerator;

public class SampleReportWriter {

	
	public void generateSamplePredictionReports(long modelSetID) {
		ExcelPredictionReportGenerator eprg=new ExcelPredictionReportGenerator();
		ModelSetTable table=SampleModelQmrfWriter.getModelsInModelSet(modelSetID);
		
		ModelSetReportServiceImpl m2=new ModelSetReportServiceImpl();
		
//		System.out.println(getJson(table));
		
		QsarModelsScript script = new QsarModelsScript("tmarti02");
		PredictionReportGenerator p=new PredictionReportGenerator();
				
		ModelSetServiceImpl m=new ModelSetServiceImpl();		
		String modelSetName=m.findById(modelSetID).getName();
		
		String downloadFolder="data/reports/prediction reports upload";
		
		File f=new File(downloadFolder);
		if (!f.exists()) f.mkdirs();
		
		
		for (ModelSetTableRow modelSetTableRow:table.modelSetTableRows) {			
			
//			String filepath="predictionReport.xlsx";
			
			String filepath = downloadFolder + File.separator + String.join("_", modelSetName,
					modelSetTableRow.datasetName, 
					modelSetTableRow.splittingName)
					+ ".xlsx";
			
			
			for (ModelMetadata mmd: modelSetTableRow.modelMetadata) {
				System.out.println(mmd.qsarMethodName);
			}
			
				
//			System.out.println(modelSetTableRow.datasetName);
			System.out.println(filepath);
			
			try {
				
				ModelSetReport msr=m2.findByModelSetIdAndModelData(modelSetID, modelSetTableRow.datasetName,modelSetTableRow.splittingName);
				
				if (msr!=null) {
					System.out.println(modelSetTableRow.datasetName+" exists skipping!");
					continue;//skip it we already did it
				}
				

				//Cant seem to generate prediction reports for large data sets:
//				if (modelSetTableRow.datasetName.equals("Melting point OPERA")) continue;
//				if (modelSetTableRow.datasetName.equals("Octanol water partition coefficient OPERA")) continue;
//				if (modelSetTableRow.datasetName.equals("LD50 TEST")) continue;
//				if (modelSetTableRow.datasetName.equals("Standard Water solubility from exp_prop")) continue;
				
								
				PredictionReport predictionReport=p.generateForModelSetPredictions(modelSetTableRow.datasetName,modelSetTableRow.splittingName,modelSetName);				
								
				
				eprg.generate(predictionReport, filepath);
				script.uploadModelSetReport(modelSetID, modelSetTableRow.datasetName, modelSetTableRow.splittingName,filepath);
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			
//			if (true) break;
		}		
	}
	
	public static void main(String[] args) {
		
		SampleReportWriter g=new SampleReportWriter();
		g.generateSamplePredictionReports(2L);
		

		//**************************************************************
//		QsarModelsScript q=new QsarModelsScript("tmarti02");
//		String datasetName="LC50DM TEST";
//		String descriptorSetName="T.E.S.T. 5.1";
//		String splittingName="TEST";
//		q.downloadModelSetReport(1L, datasetName, descriptorSetName, splittingName, "data/reports/prediction reports");

		//**************************************************************
//		QsarModelsScript q=new QsarModelsScript("tmarti02");
////		String datasetName="Standard Henry's law constant from exp_prop";
//		String datasetName="Standard Water solubility from exp_prop";
//		String descriptorSetName="T.E.S.T. 5.1";
//		String splittingName="RND_REPRESENTATIVE";
//		q.downloadModelSetReport(2L, datasetName, descriptorSetName, splittingName, "data/reports/prediction reports");

	}

}
