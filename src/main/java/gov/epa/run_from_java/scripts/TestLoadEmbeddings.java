package gov.epa.run_from_java.scripts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingService;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingServiceImpl;

public class TestLoadEmbeddings {

	public static void main(String[] args) {
		String endpoint = DevQsarConstants.LOG_HALF_LIFE;
		String sampleSource = "OPERA";
		DescriptorEmbeddingService descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();
		String datasetName = endpoint +" "+sampleSource;
		String descriptorSetName = "T.E.S.T. 5.1";		

		String qsar_method = DevQsarConstants.KNN;
		
		String embeddingDescription = "num_generations=10 num_optimizers=10 num_jobs=4 n_threads=20 max_length=24 descriptor_coefficient=0.002";
				

		DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(qsar_method, datasetName, descriptorSetName, embeddingDescription);
		String descriptorEmbeddingName = descriptorEmbedding.getName();
		System.out.println(descriptorEmbeddingName);
		
		DescriptorEmbedding descriptorEmbedding2 = descriptorEmbeddingService.findByName(descriptorEmbeddingName);
		System.out.println(descriptorEmbedding2.getName());

	}

}
