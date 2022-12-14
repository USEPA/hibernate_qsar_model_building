package gov.epa.web_services.embedding_service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.dao.DescriptorEmbeddingDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingService;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingServiceImpl;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.models.WebServiceModelBuilder;
import gov.epa.web_services.WebService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class EmbeddingWebService2 extends WebService {
	
	CalculationInfo calculationInfo;

	public EmbeddingWebService2(String server, int port) {
		super(server, port);
	}

	public static void main(String[] args) {
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);
		
		String propertyName = DevQsarConstants.HENRYS_LAW_CONSTANT;
		String datasetName = propertyName + " OPERA";
		String lanId = "cramslan";
		String descriptorSetName="T.E.S.T. 5.1";
		String splittingName="OPERA";
		Boolean removeLogDescriptors=false;		
		String tsv = null;
		try {
			tsv = retrieveTrainingData(datasetName, descriptorSetName, splittingName, removeLogDescriptors, lanId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CalculationInfo ci = new CalculationInfo();
		ci.tsv = tsv;
		ci.remove_log_p = propertyName.equals(DevQsarConstants.LOG_KOW);
		ci.qsarMethod = DevQsarConstants.KNN;
		ci.num_generations = 10;
		
		HttpResponse<String> response = ews2.findEmbedding(ci);
		System.out.println(response.getStatus());
		
		String data = response.getBody();
		System.out.println(data);
		
		Gson gson = new Gson();
		CalculationResponse cr = gson.fromJson(data, CalculationResponse.class);
		String embedding = cr.embedding.stream().map(Object::toString).collect(Collectors.joining("\t"));
		
		DescriptorEmbedding desE = new DescriptorEmbedding();
		desE.setDatasetName(datasetName);
		desE.setCreatedBy(lanId);
		desE.setDescription("num_generations=" + String.valueOf(ci.num_generations)
				+ " num_optimizers=" + String.valueOf(ci.num_optimizers)
				+ " num_jobs=" + String.valueOf(ci.num_jobs)
				+ " n_threads=" + String.valueOf(ci.n_threads)
				+ " max_length=" + String.valueOf(ci.max_length)
				+ " descriptor_coefficient=" + String.valueOf(ci.descriptor_coefficient));
		desE.setDescriptorSetName(descriptorSetName);
		desE.setEmbeddingTsv(embedding);
		desE.setQsarMethod(ci.qsarMethod);
		desE.setName("hlc-testing4");
		desE.setDatasetName(datasetName);
		desE.setImportanceTsv("not null importances");
		
		Date date = new Date();
		Timestamp timestamp2 = new Timestamp(date.getTime());
		desE.setCreatedAt(timestamp2);
		desE.setUpdatedAt(timestamp2);

		
		if (true) {
		DescriptorEmbeddingService deSer = new DescriptorEmbeddingServiceImpl();
		deSer.create(desE);
		}


		
	}
	
	public void getExistingEmbedding(String datasetName, String embeddingName, String embeddingQsarMethod) {
		DescriptorEmbeddingDaoImpl deai = new DescriptorEmbeddingDaoImpl();
		DescriptorEmbedding descriptorEmbedding = deai.findByName(embeddingName, null);
		
	}
	
	
	
	public static String retrieveTrainingData(String datasetName, String descriptorSetName, 
			String splittingName, Boolean removeLogDescriptors, String lanId) {
		WebServiceModelBuilder wsmb = new WebServiceModelBuilder(null, lanId);
		ModelData data = wsmb.initModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors);
		return data.trainingSetInstances;
	}
	
	public HttpResponse<String> findEmbedding(CalculationInfo calculationInfo) {
		System.out.println(this.address+ "/models/" + calculationInfo.qsarMethod +"/embedding");
//		System.out.println(calculationInfo.tsv);
				
		HttpResponse<String> response = Unirest.post(this.address+ "/models/{qsar_method}/embedding")
				.routeParam("qsar_method", calculationInfo.qsarMethod)
				.field("training_tsv",calculationInfo.tsv)
//				.field("save_to_database",calculationInfo.save_to_database)
				.field("remove_log_p",String.valueOf(calculationInfo.remove_log_p))
				.field("num_optimizers",String.valueOf(calculationInfo.num_optimizers))
				.field("num_jobs",String.valueOf(calculationInfo.num_jobs))
				.field("num_generations",String.valueOf(calculationInfo.num_generations))
				.field("max_length",String.valueOf(calculationInfo.max_length))
				.field("descriptor_coefficient",String.valueOf(calculationInfo.descriptor_coefficient))
				.asString();
		
		return response;
	}

	

}
