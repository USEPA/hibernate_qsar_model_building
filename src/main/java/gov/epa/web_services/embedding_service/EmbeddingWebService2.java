package gov.epa.web_services.embedding_service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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


	public void generateEmbedding(String server, int port, String datasetName, String lanId, String descriptorSetName,
			String splittingName, Boolean removeLogDescriptors, int num_generations) {
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(server, port);
		
		String tsv = null;
		try {
			tsv = retrieveTrainingData(datasetName, descriptorSetName, splittingName, removeLogDescriptors, lanId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		CalculationInfo ci = new CalculationInfo();
		ci.tsv = tsv;
		ci.remove_log_p = removeLogDescriptors;
		ci.qsarMethodGA = DevQsarConstants.KNN;
		ci.num_generations = num_generations;

		HttpResponse<String> response = ews2.findEmbedding(ci);
		System.out.println("calculation response status=" + response.getStatus());

		String data = response.getBody();
		System.out.println("calculation response data=" + data);
		Gson gson = new Gson();
		Gson gson2 = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		CalculationResponse cr = gson.fromJson(data, CalculationResponse.class);
		String embedding = cr.embedding.stream().map(Object::toString).collect(Collectors.joining("\t"));

		DescriptorEmbedding desE = new DescriptorEmbedding();
		desE.setDatasetName(datasetName);
		desE.setCreatedBy(lanId);
		desE.setDescription(gson2.toJson(ci));
		desE.setDescriptorSetName(descriptorSetName);
		desE.setEmbeddingTsv(embedding);
		desE.setQsarMethod(ci.qsarMethodGA);
		desE.setName(datasetName + "_" + descriptorSetName + "_" + System.currentTimeMillis());
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

	public DescriptorEmbedding generateEmbedding(String server, int port, String lanId,	CalculationInfo ci) {
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(server, port);

		String tsv = null;
		try {
			tsv = retrieveTrainingData(ci.datasetName, ci.descriptorSetName, ci.splittingName, ci.remove_log_p, lanId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Cant retrieve tsv for "+ci.datasetName);
			return null;
		}
		ci.tsv = tsv;

		HttpResponse<String> response = ews2.findEmbedding(ci);
		System.out.println("calculation response status=" + response.getStatus());

		String data = response.getBody();
		System.out.println("calculation response data=" + data);

		Gson gson = new Gson();		
		CalculationResponse cr = gson.fromJson(data, CalculationResponse.class);

		String embedding = cr.embedding.stream().map(Object::toString).collect(Collectors.joining("\t"));

		DescriptorEmbedding desE = new DescriptorEmbedding();
		desE.setDatasetName(ci.datasetName);
		desE.setCreatedBy(lanId);
		desE.setDescription(ci.toString());
		desE.setDescriptorSetName(ci.descriptorSetName);
		desE.setEmbeddingTsv(embedding);
		desE.setQsarMethod(ci.qsarMethodGA);
		desE.setName(ci.datasetName + "_" + ci.descriptorSetName + "_" + System.currentTimeMillis());
		desE.setDatasetName(ci.datasetName);
		desE.setImportanceTsv("not null importances");

		Date date = new Date();
		Timestamp timestamp2 = new Timestamp(date.getTime());
		desE.setCreatedAt(timestamp2);
		desE.setUpdatedAt(timestamp2);

		//Store embedding in the database:
		DescriptorEmbeddingService deSer = new DescriptorEmbeddingServiceImpl();
		return deSer.create(desE);

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
		System.out.println(this.address+ "/models/" + calculationInfo.qsarMethodGA +"/embedding");
		//		System.out.println(calculationInfo.tsv);

		HttpResponse<String> response = Unirest.post(this.address+ "/models/{qsar_method}/embedding")
				.routeParam("qsar_method", calculationInfo.qsarMethodGA)
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

	public static void main(String[] args) {
		EmbeddingWebService2 ews2 = new EmbeddingWebService2(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

		String lanId = "cramslan";
		String descriptorSetName="T.E.S.T. 5.1";
		String splittingName="OPERA";
		Boolean removeLogDescriptors=false;
		String propertyName = DevQsarConstants.LOG_HALF_LIFE;
		int num_generations = 10;

		ews2.generateEmbedding(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING,
				propertyName, lanId, descriptorSetName, splittingName,
				removeLogDescriptors, num_generations);
	}


}
