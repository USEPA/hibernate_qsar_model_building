package gov.epa.run_from_java.scripts;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesServiceImpl;
import gov.epa.endpoints.models.ConsensusModelBuilder;
import gov.epa.endpoints.models.WebServiceModelBuilder;
import gov.epa.web_services.ModelWebService;
import gov.epa.web_services.embedding_service.CalculationInfo;
import gov.epa.web_services.embedding_service.CalculationInfoGA;
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
	 * @param include_standardization_in_pmml 
	 */
	public static void buildModel(String modelWsServer,int modelWsPort,String datasetName,String descriptorSetName,
			String splittingName, boolean removeLogDescriptors,String methodName,String lanId, String descriptorEmbeddingName,
			boolean pythonStorage,boolean usePMML, boolean include_standardization_in_pmml,boolean use_sklearn2pmml) {

		System.out.println("Building "+methodName+" model for "+datasetName + "with" + descriptorSetName + "descriptors");

		if (!modelWsServer.startsWith("http://")) {
			modelWsServer = "http://" + modelWsServer;
		}

		ModelWebService modelWs = new ModelWebService(modelWsServer, modelWsPort);
		WebServiceModelBuilder mb = new WebServiceModelBuilder(modelWs, lanId);
		
		if (pythonStorage) {
			Long modelId = mb.buildWithPythonStorage(datasetName, descriptorSetName, splittingName, removeLogDescriptors, methodName, descriptorEmbeddingName,usePMML);
		}
		else {
			if (descriptorEmbeddingName == null) {
				Long modelId = mb.build(datasetName, descriptorSetName, splittingName, removeLogDescriptors, methodName,usePMML,include_standardization_in_pmml,use_sklearn2pmml);
			} else {
				Long modelID = mb.buildWithPreselectedDescriptors(datasetName, descriptorSetName, splittingName, removeLogDescriptors, methodName, descriptorEmbeddingName,usePMML,include_standardization_in_pmml,use_sklearn2pmml);
			}
		}


		//		PredictionReportGenerator gen = new PredictionReportGenerator();
		//		PredictionReport report=gen.generateForModelPredictions(modelId);
		//		writeReport(datasetName, descriptorSetName, report);//dont really need to write report since we probably want report from all models in one json file anyways

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
	 * @param include_standardization_in_pmml 
	 */
	public static long buildModel(String lanId,String modelWsServer,int modelWsPort,String methodName,
			DescriptorEmbedding de,CalculationInfo ci,boolean usePMML, boolean include_standardization_in_pmml,boolean use_sklearn2pmml) {

		System.out.println("\nBuilding "+methodName+" model for "+ci.datasetName + "with" + ci.descriptorSetName + "descriptors");

		if (!modelWsServer.startsWith("http://")) {
			modelWsServer = "http://" + modelWsServer;
		}

		ModelWebService modelWs = new ModelWebService(modelWsServer, modelWsPort);
		WebServiceModelBuilder mb = new WebServiceModelBuilder(modelWs, lanId);
			
		long modelID=-1;
		
		if (de==null) {
			modelID = mb.build(methodName, ci,usePMML,include_standardization_in_pmml,use_sklearn2pmml);
		} else {
			modelID = mb.buildWithPreselectedDescriptors(methodName, ci, de,usePMML,include_standardization_in_pmml,use_sklearn2pmml);	
		}
//		System.out.println("modelID="+modelID);
		

		//	PredictionReportGenerator gen = new PredictionReportGenerator();
		//	PredictionReport report=gen.generateForModelPredictions(modelId);
		//	writeReport(datasetName, descriptorSetName, report);//dont really need to write report since we probably want report from all models in one json file anyways

		return modelID;
	}


	public static Long buildUnweightedConsensusModel(Set<Long> modelIds, String lanId) {
		ConsensusModelBuilder cmb = new ConsensusModelBuilder(lanId);
		return cmb.buildUnweighted(modelIds);
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
		System.out.println("eclipse recognizes");
		String lanId="cramslan";

		//*****************************************************************************************
		// Build model:		
		//		String modelWsServer="10.140.73.169";
		String modelWsServer=DevQsarConstants.SERVER_LOCAL;
		//		String modelWsServer=DevQsarConstants.SERVER_819;

		int modelWsPort=DevQsarConstants.PORT_PYTHON_MODEL_BUILDING;

		String sampleSource="OPERA";
		//		String sampleSource="TEST";

		//		String endpoint=DevQsarConstants.LOG_OH;
		//		String endpoint=DevQsarConstants.LOG_KOW;
		//		String endpoint=DevQsarConstants.LOG_BCF;
		//		String endpoint=DevQsarConstants.MELTING_POINT;
		//		String endpoint=DevQsarConstants.LOG_HALF_LIFE;
		//		String endpoint=DevQsarConstants.LOG_KOC;
		//		String endpoint=DevQsarConstants.MUTAGENICITY;
		//		String endpoint=DevQsarConstants.LOG_KOW;
		//		String endpoint=DevQsarConstants.LLNA;
		//		String endpoint=DevQsarConstants.DEV_TOX;
		//		String endpoint=DevQsarConstants.VAPOR_PRESSURE;
		String endpoint=DevQsarConstants.HENRYS_LAW_CONSTANT;
		//		String endpoint=DevQsarConstants.LC50;
		//		String endpoint=DevQsarConstants.MUTAGENICITY;
		//		String endpoint=DevQsarConstants.LOG_KOW;

		String datasetName = endpoint +" "+sampleSource;
		String splittingName=sampleSource;

		//		String datasetName = "Standard Henry's law constant from exp_prop";
		//		String splittingName="RND_REPRESENTATIVE";
		String descriptorSetName = "T.E.S.T. 5.1";		


		boolean removeLogDescriptors=endpoint.equals(DevQsarConstants.LOG_KOW);
		// 	"PaDEL-default", "RDKit-default", "WebTEST-default", "ToxPrints-default",
		/*
		String[] sciDataExpertsDescriptorSetNames = {
				"PaDEL-default"
				};


		String methods[]= {DevQsarConstants.RF};
		for (String desc:sciDataExpertsDescriptorSetNames) {
			for (String method:methods) {
				System.out.println(method + "descriptor" + desc);
				ModelBuildingScript.buildModel(modelWsServer,modelWsPort, datasetName,desc,
						splittingName, removeLogDescriptors,method,lanId,null);
			}

		}
		 */	

		//*****************************************************************************************
		//		String methodName=DevQsarConstants.SVM;
		//		String methodName=DevQsarConstants.DNN;
		String methodName=DevQsarConstants.RF;
		//		String methodName=DevQsarConstants.XGB;

		//		String descriptorEmbeddingName="TestEmbeddingHLCexpPrp";
		//		String methodName=DevQsarConstants.XGB;
		//

		//		String datasetName="LLNA from exp_prop, without eChemPortal";
		//		String descriptorEmbeddingName="TestEmbeddingHLCexpPrp";

		String embeddingName="Henry's law constant OPERA_1671052729185";

		ModelBuildingScript.buildModel(modelWsServer,modelWsPort,datasetName,descriptorSetName,
				splittingName, removeLogDescriptors,methodName,lanId, embeddingName, false,false,false,false);
		//
		//		run.buildModel("http://localhost","8080", modelWsServer,modelWsPort,datasetName,descriptorSetName,
		//				splittingName, removeLogDescriptors,methodName,lanId);


		//*****************************************************************************************
	}

}
