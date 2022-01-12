package gov.epa.runFromJava;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import gov.epa.endpoints.models.ModelBuilder;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.endpoints.reports.predictions.PredictionReportGenerator;
import gov.epa.web_services.ModelWebService;

public class DevQsarFromJava {
	
	
	
	public void buildModel(String modelWsServer,int modelWsPort,String datasetName,String descriptorSetName,
			String splittingName, boolean removeLogDescriptors,String methodName,String lanId) {
				
		Logger apacheLogger = LogManager.getLogger("org.apache.http");
		apacheLogger.setLevel(Level.WARN);
		
		if (!modelWsServer.startsWith("http://")) {
			modelWsServer = "http://" + modelWsServer;
		}
		
		ModelWebService modelWs = new ModelWebService(modelWsServer, modelWsPort);
		ModelBuilder mb = new ModelBuilder(modelWs, lanId);
		Long modelId = mb.build(datasetName, descriptorSetName, splittingName, removeLogDescriptors, methodName);
		
		PredictionReportGenerator gen = new PredictionReportGenerator();
		PredictionReport report=gen.generateForModelPredictions(modelId);
		
		writeReport(datasetName, descriptorSetName, report);
		
	}


	private void writeReport(String datasetName, String descriptorSetName, PredictionReport report) {
		String filePath = "reports/"+ datasetName + "_" + descriptorSetName + "_PredictionReport.json";

		File file = new File(filePath);
		if (file.getParentFile()!=null) {
			file.getParentFile().mkdirs();
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			writer.write(gson.toJson(report));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	public void reportAllPredictions(String datasetName,String descriptorSetName) {
		String filePath = "reports/"+ datasetName + "_" + descriptorSetName + "_PredictionReport.json";

		File file = new File(filePath);
		if (file.getParentFile()!=null) {
			file.getParentFile().mkdirs();
		}

		long t1=System.currentTimeMillis();
		PredictionReportGenerator gen = new PredictionReportGenerator();
		PredictionReport report=gen.generateForAllPredictions(datasetName, descriptorSetName);
		long t2=System.currentTimeMillis();

		double time=(t2-t1)/1000.0;
		System.out.println("Time to generate report for "+datasetName+" = "+time+" seconds");

		writeReport(datasetName, descriptorSetName, report);	
	}
	
	
	public void reportAllPredictions(Vector<String> datasetNames,String descriptorSetName) {
		for (String datasetName:datasetNames) {
			reportAllPredictions(datasetName, descriptorSetName);
		}
	}
	
	static Vector<String>getSampleDataSets(boolean includeOPERA,boolean includeTEST) {
		Vector<String>sets=new Vector<>();		
		
		if(includeOPERA) {
			sets.add(DevQsarConstants.LOG_HALF_LIFE+" OPERA");
			sets.add(DevQsarConstants.LOG_KOA+" OPERA");
			sets.add(DevQsarConstants.LOG_KM_HL+" OPERA");
			sets.add(DevQsarConstants.HENRYS_LAW_CONSTANT+" OPERA");
			sets.add(DevQsarConstants.WATER_SOLUBILITY+" OPERA");
			sets.add(DevQsarConstants.LOG_BCF+" OPERA");
			sets.add(DevQsarConstants.LOG_OH+" OPERA");
			sets.add(DevQsarConstants.VAPOR_PRESSURE+" OPERA");
			sets.add(DevQsarConstants.LOG_KOC+" OPERA");
			sets.add(DevQsarConstants.BOILING_POINT+" OPERA");
			sets.add(DevQsarConstants.MELTING_POINT+" OPERA");
			sets.add(DevQsarConstants.LOG_KOW+" OPERA");
		}
		
		if(includeTEST) {
			sets.add(DevQsarConstants.DEV_TOX+" TEST");
			sets.add(DevQsarConstants.IGC50+" TEST");
			sets.add(DevQsarConstants.LC50+" TEST");
			sets.add(DevQsarConstants.LC50DM+" TEST");
			sets.add(DevQsarConstants.LD50+" TEST");
			sets.add(DevQsarConstants.LLNA+" TEST");
			sets.add(DevQsarConstants.MUTAGENCITY+" TEST");
			
		}
		return sets;
	}
	
	
	public static void main(String[] args) {
		
		DevQsarFromJava d=new DevQsarFromJava();
		
		//*****************************************************************************************
		// Build model:		
		String modelWsServer=DevQsarConstants.SERVER_819;
		int modelWsPort=DevQsarConstants.PORT_PYTHON_MODEL_BUILDING;
//		String endpoint=DevQsarConstants.LOG_BCF;
		String endpoint=DevQsarConstants.LOG_KOW;
		String datasetName = endpoint+" OPERA";
		String descriptorSetName = "T.E.S.T. 5.1";
		String splittingName="OPERA";
		boolean removeLogDescriptors=endpoint.equals(DevQsarConstants.LOG_KOW);
		String methodName=DevQsarConstants.SVM;
		String lanId="tmarti02";
		d.buildModel(modelWsServer,modelWsPort,datasetName,descriptorSetName,
			splittingName, removeLogDescriptors,methodName,lanId);

		//*****************************************************************************************			
//		String descriptorSetName = "T.E.S.T. 5.1";
//		String endpoint=DevQsarConstants.LOG_HALF_LIFE;
//		String datasetName = endpoint+" OPERA";
//		d.reportAllPredictions(datasetName,descriptorSetName);		
		//*****************************************************************************************	
//		d.reportAllPredictions(getSampleDataSets(false,true), descriptorSetName);
		//*****************************************************************************************
		
		
	}

}
