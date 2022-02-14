package gov.epa.run_from_java;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.endpoints.models.ModelBuilder;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.endpoints.reports.predictions.PredictionReportGenerator;
import gov.epa.endpoints.reports.predictions.ExcelReports.ExcelModelStatisticsOld;
import gov.epa.endpoints.reports.predictions.ExcelReports.ExcelPredictionReportGenerator;
import gov.epa.web_services.ModelWebService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class RunFromJava {
	
	
	
	public RunFromJava() {
		// Make sure Unirest is configured
		try {
			Unirest.config().followRedirects(true).socketTimeout(000).connectTimeout(000);
		} catch (Exception e) {
			// Ignore
		}
	}

	static PredictionReportGenerator gen = new PredictionReportGenerator();

	

	/**
	 * Run using webservice
	 * 
	 * @param server
	 * @param port
	 * @param modelWsServer
	 * @param modelWsPort
	 * @param datasetName
	 * @param descriptorSetName
	 * @param splittingName
	 * @param removeLogDescriptors
	 * @param methodName
	 * @param lanId
	 */
	public void buildModel(String server,String port, String modelWsServer,int modelWsPort,String datasetName,String descriptorSetName,
			String splittingName, boolean removeLogDescriptors,String methodName,String lanId) {

		System.out.println(server+":"+port+"/models/build");
		
//		Unirest.config()
//        .followRedirects(true)   
//		.socketTimeout(000)
//           .connectTimeout(000);

		
		HttpResponse<String> response = Unirest.get(server+":"+port+"/models/build")
				.queryString("model-ws-port", modelWsPort)
				.queryString("model-ws-server", modelWsServer)				
				.queryString("dataset-name", datasetName)
				.queryString("descriptor-set-name", descriptorSetName)
				.queryString("splitting-name",splittingName)
				.queryString("remove-log-descriptors",removeLogDescriptors+"")
				.queryString("method-name",methodName)
				.queryString("lanid",lanId)
				.asString();
		
//		System.out.println("Report="+response.getBody());
		
	}
	
	/**
	 * Run using direct java code
	 * 
	 * @param modelWsServer
	 * @param modelWsPort
	 * @param datasetName
	 * @param descriptorSetName
	 * @param splittingName
	 * @param removeLogDescriptors
	 * @param methodName
	 * @param lanId
	 */
	public void buildModel(String modelWsServer,int modelWsPort,String datasetName,String descriptorSetName,
			String splittingName, boolean removeLogDescriptors,String methodName,String lanId) {

		System.out.println("Building "+methodName+" model for "+datasetName);
		
		if (!modelWsServer.startsWith("http://")) {
			modelWsServer = "http://" + modelWsServer;
		}
		
		ModelWebService modelWs = new ModelWebService(modelWsServer, modelWsPort);
		ModelBuilder mb = new ModelBuilder(modelWs, lanId);
		Long modelId = mb.build(datasetName, descriptorSetName, splittingName, removeLogDescriptors, methodName);

//		PredictionReportGenerator gen = new PredictionReportGenerator();
//		PredictionReport report=gen.generateForModelPredictions(modelId);
//		writeReport(datasetName, descriptorSetName, report);//dont really need to write report since we probably want report from all models in one json file anyways

	}



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



	public static PredictionReport reportAllPredictions(String datasetName,String descriptorSetName,String splittingName) {

		long t1=System.currentTimeMillis();
		PredictionReport report=gen.generateForAllPredictions(datasetName, descriptorSetName,splittingName);
		long t2=System.currentTimeMillis();

		double time=(t2-t1)/1000.0;
		System.out.println("Time to generate report for "+datasetName+" = "+time+" seconds");

		writeReport(datasetName, descriptorSetName, report);
		return report;
	}


	public void reportAllPredictions(Vector<String> datasetNames,String descriptorSetName,String splittingName) {
		for (String datasetName:datasetNames) {
			reportAllPredictions(datasetName, descriptorSetName,splittingName);
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

	void testInit(String modelWsServer,int modelWsPort,String methodName,long modelID) {

		ModelBytesService modelBytesService = new ModelBytesServiceImpl();
		
		ModelBytes modelBytes = modelBytesService.findByModelId(modelID);
		Model model = modelBytes.getModel();
		byte[] bytes = modelBytes.getBytes();
		
		System.out.println("bytes.length loaded from db="+bytes.length);
		
		ModelWebService modelWs = new ModelWebService(modelWsServer, modelWsPort);
		modelWs.callInit(bytes, methodName, modelID+"").getBody();

	}

	void deleteModel(long modelID) {
		ModelService modelService = new ModelServiceImpl();
		Model model = modelService.findById(modelID);
		modelService.delete(model);

	}
	
	public static void main(String[] args) {

		String lanId="tmarti02";
		RunFromJava run=new RunFromJava();

//		run.deleteModel(127L);
//		if(true) return;
		
		
		//*****************************************************************************************
		// Build model:		
		String modelWsServer=DevQsarConstants.SERVER_819;
//		String modelWsServer=DevQsarConstants.SERVER_LOCAL;
		int modelWsPort=DevQsarConstants.PORT_PYTHON_MODEL_BUILDING;
		
		String sampleSource="OPERA";
//		String sampleSource="TEST";
		
//		String endpoint=DevQsarConstants.LOG_OH;
//		String endpoint=DevQsarConstants.LOG_KOW;
//		String endpoint=DevQsarConstants.LOG_HALF_LIFE;
//		String endpoint=DevQsarConstants.BOILING_POINT;
//		String endpoint=DevQsarConstants.LLNA;
//		String endpoint=DevQsarConstants.LOG_KOW;
		String endpoint=DevQsarConstants.HENRYS_LAW_CONSTANT;
//		String endpoint=DevQsarConstants.DEV_TOX;
				
		String datasetName = endpoint +" "+sampleSource;
		String splittingName=sampleSource;		
		String descriptorSetName = "T.E.S.T. 5.1";		
		boolean removeLogDescriptors=endpoint.equals(DevQsarConstants.LOG_KOW);
		
//		String methods[]= {DevQsarConstants.SVM,DevQsarConstants.DNN,DevQsarConstants.RF,DevQsarConstants.XGB};
//		for (String method:methods) {
//			System.out.println(method);
//			run.buildModel(modelWsServer,modelWsPort,datasetName,descriptorSetName,
//			splittingName, removeLogDescriptors,method,lanId);
////			run.buildModel("http://localhost","8080", modelWsServer,modelWsPort,datasetName,descriptorSetName,
////			splittingName, removeLogDescriptors,method,lanId);
//
//		}
		
		//*****************************************************************************************
		String methodName=DevQsarConstants.SVM;
//		String methodName=DevQsarConstants.DNN;
//		String methodName=DevQsarConstants.RF;
//		String methodName=DevQsarConstants.XGB;
//
//		run.buildModel(modelWsServer,modelWsPort,datasetName,descriptorSetName,
//				splittingName, removeLogDescriptors,methodName,lanId);
//
//		run.buildModel("http://localhost","8080", modelWsServer,modelWsPort,datasetName,descriptorSetName,
//				splittingName, removeLogDescriptors,methodName,lanId);


		//*****************************************************************************************
//		d.testInit(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING, methodName, 104L);	
		//*****************************************************************************************			
		//		String descriptorSetName = "T.E.S.T. 5.1";
		//		String endpoint=DevQsarConstants.LOG_HALF_LIFE;
		//		String datasetName = endpoint+" OPERA";
		run.reportAllPredictions(datasetName,descriptorSetName,splittingName);		

		//*****************************************************************************************	
		//		run.reportAllPredictions(getSampleDataSets(false,true), descriptorSetName);
		//*****************************************************************************************

	*/
		// generate excel prediction reports //
		
		ExcelPredictionReportGenerator per = ExcelPredictionReportGenerator.prepareReportFactory("Water solubility OPERA_T.E.S.T. 5.1_OPERA_PredictionReport.json","data\\ExcelReports");
		per.generate(per.wb);
		
		// generate full model database report //
		ExcelModelStatisticsOld ems = ExcelModelStatisticsOld.prepareFactory("data/ExcelReports");
		ems.generate();



	}

}
