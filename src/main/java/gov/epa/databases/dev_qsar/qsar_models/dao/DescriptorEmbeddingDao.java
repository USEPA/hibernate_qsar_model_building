package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;

public interface DescriptorEmbeddingDao {
	
	public DescriptorEmbedding findByName(String descriptorEmbeddingName, Session session);
	
	public DescriptorEmbedding findByGASettings(String qsar_method, String dataset_name, String descriptor_set_name, 
			String descriptionJson, Session session);

}
