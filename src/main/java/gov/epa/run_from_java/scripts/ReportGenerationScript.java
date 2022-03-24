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
import gov.epa.endpoints.reports.predictions.PredictionReportGenerator;
import gov.epa.endpoints.reports.predictions.ExcelReports.ExcelModelStatisticsOld;
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

	private static void writeReport(String datasetName, String descriptorSetName, PredictionReport report) {
		String filePath = "data/reports/"+ datasetName + "_" + descriptorSetName + "_PredictionReport.json";


		File file = new File(filePath);
		if (file.getParentFile()!=null) {
			file.getParentFile().mkdirs();
			System.out.println(file.getAbsolutePath());
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			writer.write(gson.toJson(report));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	public static PredictionReport reportAllPredictions(String datasetName,String descriptorSetName,String splittingName,String modelSetName) {

		long t1=System.currentTimeMillis();
		PredictionReport report=gen.generateForModelSetPredictions(datasetName, descriptorSetName, splittingName, modelSetName);
		long t2=System.currentTimeMillis();

		double time=(t2-t1)/1000.0;
		System.out.println("Time to generate report for "+datasetName+" = "+time+" seconds");

		writeReport(datasetName, descriptorSetName, report);
		return report;
	}


	public static void reportAllPredictions(Vector<String> datasetNames,String descriptorSetName,String splittingName,String modelSetName) {
		for (String datasetName:datasetNames) {
			reportAllPredictions(datasetName, descriptorSetName,splittingName,modelSetName);
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
				String descriptorSetName = "T.E.S.T. 5.1";
				String endpoint=DevQsarConstants.LOG_HALF_LIFE;
				String datasetName = endpoint+" OPERA";
		ReportGenerationScript.reportAllPredictions(datasetName,descriptorSetName,"OPERA", "Sample models");		

		//*****************************************************************************************	
		//		run.reportAllPredictions(getSampleDataSets(false,true), descriptorSetName);
		//*****************************************************************************************

		// generate excel prediction reports //
		
//		ExcelPredictionReportGenerator per = ExcelPredictionReportGenerator.prepareReportFactory("Water solubility OPERA_T.E.S.T. 5.1_OPERA_PredictionReport.json","data\\ExcelReports");
//		per.generate(per.wb);
		
		// generate full model database report //
//		ExcelModelStatisticsOld ems = ExcelModelStatisticsOld.prepareFactory("data/ExcelReports");
//		ems.generate();



	}

}
