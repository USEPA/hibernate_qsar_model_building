package gov.epa.run_from_java.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.models.ModelBuilder;
import gov.epa.endpoints.models.WebServiceModelBuilder;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportDataPoint;
import gov.epa.endpoints.reports.predictions.PredictionReportGenerator;
import gov.epa.endpoints.reports.predictions.ExcelReports.ExcelModelStatisticsOld;
import gov.epa.endpoints.reports.predictions.ExcelReports.ExcelPredictionReportGenerator;
import kong.unirest.Unirest;

public class ReportGenerationScript {
	
	
	
	public ReportGenerationScript() {
		// Make sure Unirest is configured
		try {
			Unirest.config().followRedirects(true).socketTimeout(000).connectTimeout(000);
		} catch (Exception e) {
			// Ignore
		}
	}

	static PredictionReportGenerator gen = new PredictionReportGenerator();




	public static PredictionReport reportAllPredictions(String datasetName,String splittingName,String modelSetName,boolean deleteMissingSplitting,boolean includeDescriptors) {

		long t1=System.currentTimeMillis();
		PredictionReport report=gen.generateForModelSetPredictions(datasetName, splittingName, modelSetName,includeDescriptors);
		long t2=System.currentTimeMillis();

		if (deleteMissingSplitting) {
			deleteMissingSplitting(report);			
		}
		
		
		double time=(t2-t1)/1000.0;
		System.out.println("Time to generate report for "+datasetName+" = "+time+" seconds");

		
		String filePath = "data/reports/" + modelSetName+"/"+datasetName + "_PredictionReport.json";

		report.toFile(filePath);
		return report;
	}
	

	
	public static PredictionReport reportPredictionsMethod(String modelSetName, String datasetName,String splittingName,String methodName,boolean deleteMissingSplitting,boolean includeDescriptors,boolean includeOriginalCompounds,String filePath,boolean writeFile) {

		long t1=System.currentTimeMillis();
		PredictionReport report=gen.generateMethodPredictions(modelSetName, datasetName, splittingName, methodName,includeDescriptors,includeOriginalCompounds);
		long t2=System.currentTimeMillis();

		if (deleteMissingSplitting) deleteMissingSplitting(report);			
		
		double time=(t2-t1)/1000.0;
		System.out.println("Time to generate report for "+datasetName+" = "+time+" seconds");

		if(writeFile) report.toFile(filePath);
		return report;
	}


	private static void deleteMissingSplitting(PredictionReport report) {
		for(int i=0;i<report.predictionReportDataPoints.size();i++) {
			PredictionReportDataPoint dp=report.predictionReportDataPoints.get(i);
			
			if(dp.qsarPredictedValues.size()==0 || dp.qsarPredictedValues.get(0).splitNum==null) {
//					System.out.println(datasetName+"\t"+dp.canonQsarSmiles+"\tremoving data point since no predictions");					
				report.predictionReportDataPoints.remove(i--);
			}
			
		}
	}



	public static void reportAllPredictions(Vector<String> datasetNames,String splittingName,String modelSetName) {
		for (String datasetName:datasetNames) {
			reportAllPredictions(datasetName, splittingName,modelSetName,true,false);
		}
	}

	static Vector<String>getSampleDataSets(boolean includeOPERA,boolean includeTEST) {
		Vector<String>sets=new Vector<>();		

		if(includeOPERA) {
			sets.add(DevQsarConstants.LOG_HALF_LIFE+" OPERA");//
			sets.add(DevQsarConstants.LOG_KOA+" OPERA");//
			sets.add(DevQsarConstants.LOG_KM_HL+" OPERA");//
			sets.add(DevQsarConstants.HENRYS_LAW_CONSTANT+" OPERA");//
			sets.add(DevQsarConstants.WATER_SOLUBILITY+" OPERA");//
			sets.add(DevQsarConstants.LOG_BCF+" OPERA");//
			sets.add(DevQsarConstants.LOG_OH+" OPERA");//
			sets.add(DevQsarConstants.VAPOR_PRESSURE+" OPERA");//
			sets.add(DevQsarConstants.LOG_KOC+" OPERA");//
			sets.add(DevQsarConstants.BOILING_POINT+" OPERA");
			sets.add(DevQsarConstants.MELTING_POINT+" OPERA");//
			sets.add(DevQsarConstants.LOG_KOW+" OPERA");
		}

		if(includeTEST) {
			sets.add(DevQsarConstants.DEV_TOX+" TEST");//
			sets.add(DevQsarConstants.IGC50+" TEST");//
			sets.add(DevQsarConstants.LC50+" TEST");//
			sets.add(DevQsarConstants.LC50DM+" TEST");//
			sets.add(DevQsarConstants.LD50+" TEST");//
			sets.add(DevQsarConstants.LLNA+" TEST");//
			sets.add(DevQsarConstants.MUTAGENICITY+" TEST");//

		}
		return sets;
	}

	public static void main(String[] args) {
		String lanId="tmarti02";
		ReportGenerationScript run=new ReportGenerationScript();
		
		//*****************************************************************************************			
				String descriptorSetName = "Mordred-default";
				String endpoint=DevQsarConstants.HENRYS_LAW_CONSTANT;
				String datasetName = endpoint+" OPERA";
		ReportGenerationScript.reportAllPredictions(datasetName,"OPERA", "Sample models Mordred descriptors",false,false);		

		//*****************************************************************************************	
		//		run.reportAllPredictions(getSampleDataSets(false,true), descriptorSetName);
		//*****************************************************************************************

		// generate excel prediction reports //
		/*
		ExcelPredictionReportGenerator e = ExcelPredictionReportGenerator.prepareReportFactory("Water solubility OPERA_T.E.S.T. 5.1_OPERA_PredictionReport.json","data\\ExcelReports");
		per.generate(per.wb);
		e.generate(predictionReport, folder.getAbsolutePath()+File.separator+datasetName + "_report.xlsx");
		*/
		// generate full model database report //
//		ExcelModelStatisticsOld ems = ExcelModelStatisticsOld.prepareFactory("data/ExcelReports");
//		ems.generate();



	}

}
