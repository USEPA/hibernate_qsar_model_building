package gov.epa.run_from_java.scripts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingService;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingServiceImpl;
import gov.epa.web_services.embedding_service.CalculationInfo;

public class TestLoadEmbeddings {

	public static void main(String[] args) {
		String endpoint = DevQsarConstants.LOG_HALF_LIFE;

		String sampleSource = "OPERA";
		CalculationInfo ci=new CalculationInfo();		
		ci.datasetName = endpoint +" "+sampleSource;
		ci.descriptorSetName = "T.E.S.T. 5.1";		
		ci.qsarMethodGA = DevQsarConstants.KNN;
		ci.splittingName="OPERA";
		

		DescriptorEmbeddingServiceImpl descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();
		DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);
		String descriptorEmbeddingName = descriptorEmbedding.getName();
		System.out.println(descriptorEmbeddingName);
		
		DescriptorEmbedding descriptorEmbedding2 = descriptorEmbeddingService.findByName(descriptorEmbeddingName);
		System.out.println(descriptorEmbedding2.getName());

	}

}
