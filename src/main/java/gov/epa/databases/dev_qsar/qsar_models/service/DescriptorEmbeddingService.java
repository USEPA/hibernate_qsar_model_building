package gov.epa.databases.dev_qsar.qsar_models.service;

import jakarta.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.web_services.embedding_service.CalculationInfo;

public interface DescriptorEmbeddingService {
	
	public DescriptorEmbedding findByName(String descriptorEmbeddingName);
	
	public DescriptorEmbedding findByName(String descriptorEmbeddingName, Session session);
	
	public DescriptorEmbedding create(DescriptorEmbedding descriptorEmbedding) throws ConstraintViolationException;
	
	public DescriptorEmbedding create(DescriptorEmbedding descriptorEmbedding, Session session) throws ConstraintViolationException;

	public DescriptorEmbedding findByGASettings(CalculationInfo ci);
	
	public void delete(DescriptorEmbedding de);
	
	public void delete(DescriptorEmbedding de,Session session);

}
