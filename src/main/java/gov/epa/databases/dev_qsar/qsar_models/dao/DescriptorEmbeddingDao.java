package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.web_services.embedding_service.CalculationInfo;

public interface DescriptorEmbeddingDao {
	
	public DescriptorEmbedding findByName(String descriptorEmbeddingName, Session session);
	
	public DescriptorEmbedding findByGASettings(CalculationInfo calculationInfo, Session session);

}
