package gov.epa.run_from_java.scripts;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesServiceImpl;
import gov.epa.endpoints.models.ConsensusModelBuilder;
import gov.epa.endpoints.models.WebServiceModelBuilder;
import gov.epa.web_services.ModelWebService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class ModelBuildingScript {

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
		public static void buildModel(String server,String port, String modelWsServer,int modelWsPort,String datasetName,String descriptorSetName,
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
		public static void buildModel(String modelWsServer,int modelWsPort,String datasetName,String descriptorSetName,
				String splittingName, boolean removeLogDescriptors,String methodName,String lanId) {
	
			System.out.println("Building "+methodName+" model for "+datasetName);
			
			if (!modelWsServer.startsWith("http://")) {
				modelWsServer = "http://" + modelWsServer;
			}
			
			ModelWebService modelWs = new ModelWebService(modelWsServer, modelWsPort);
			WebServiceModelBuilder mb = new WebServiceModelBuilder(modelWs, lanId);
			Long modelId = mb.build(datasetName, descriptorSetName, splittingName, removeLogDescriptors, methodName);
	
	//		PredictionReportGenerator gen = new PredictionReportGenerator();
	//		PredictionReport report=gen.generateForModelPredictions(modelId);
	//		writeReport(datasetName, descriptorSetName, report);//dont really need to write report since we probably want report from all models in one json file anyways
	
		}

	static void testInit(String modelWsServer,int modelWsPort,String methodName,long modelID) {
	
		ModelBytesService modelBytesService = new ModelBytesServiceImpl();
		
		ModelBytes modelBytes = modelBytesService.findByModelId(modelID);
		Model model = modelBytes.getModel();
		byte[] bytes = modelBytes.getBytes();
		
		System.out.println("bytes.length loaded from db="+bytes.length);
		
		ModelWebService modelWs = new ModelWebService(modelWsServer, modelWsPort);
		modelWs.callInit(bytes, methodName, modelID+"").getBody();
	
	}
	
	public static void buildUnweightedConsensusModel(Set<Long> modelIds, String lanId) {
		ConsensusModelBuilder cmb = new ConsensusModelBuilder(lanId);
		cmb.buildUnweighted(modelIds);
	}
	
	public static void buildWeightedConsensusModel(Map<Long, Double> modelIdsWithWeights, String lanId) {
		ConsensusModelBuilder cmb = new ConsensusModelBuilder(lanId);
		cmb.buildWeighted(modelIdsWithWeights);
	}
	
	// DANGER: Check your array lengths! This is just for ease of testing and does not perform any checks itself.
	public static void buildWeightedConsensusModelFromArrays(Long[] modelIds, Double[] weights, String lanId) {
		Map<Long, Double> modelIdsWithWeights = new HashMap<Long, Double>();
		for (int i = 0; i < modelIds.length; i++) {
			modelIdsWithWeights.put(modelIds[i], weights[i]);
		}
		buildWeightedConsensusModel(modelIdsWithWeights, lanId);
	}
	
	public static void main(String[] args) {
		String lanId="tmarti02";
		ModelBuildingScript run=new ModelBuildingScript();
		
		//*****************************************************************************************
		// Build model:		
		String modelWsServer="10.140.73.169";
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
//		String methodName=DevQsarConstants.SVM;
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
	}

}
