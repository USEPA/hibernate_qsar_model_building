package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.web_services.embedding_service.CalculationInfo;

public class DescriptorEmbeddingDaoImpl implements DescriptorEmbeddingDao {
	
	private static final String HQL_BY_NAME = 
			"from DescriptorEmbedding de where de.name = :descriptorEmbeddingName";
	
	private static final String HQL_BY_FIELDS = 
			"from DescriptorEmbedding de where de.qsarMethod = :qsar_method"
			+ " and de.datasetName = :dataset_name and de.descriptorSetName = :descriptor_set_name"
					+ " and de.description = :description and de.splittingName = :splitting_name";

	@Override
	public DescriptorEmbedding findByName(String descriptorEmbeddingName, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query <DescriptorEmbedding>query = session.createQuery(HQL_BY_NAME,DescriptorEmbedding.class);
		query.setParameter("descriptorEmbeddingName", descriptorEmbeddingName);
		return query.uniqueResult();
	}

	@Override
	public DescriptorEmbedding findByGASettings(CalculationInfo ci, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }

        Query<DescriptorEmbedding> query = session.createQuery(HQL_BY_FIELDS, DescriptorEmbedding.class);
		query.setParameter("qsar_method", ci.qsarMethodEmbedding);
		query.setParameter("dataset_name", ci.datasetName);
		query.setParameter("descriptor_set_name",ci.descriptorSetName);
		query.setParameter("description",ci.toString());
		query.setParameter("splitting_name",ci.splittingName);	

		
		System.out.println(ci.qsarMethodEmbedding);
		System.out.println(ci.datasetName);
		System.out.println(ci.descriptorSetName);
		System.out.println(ci.toString());
		System.out.println(ci.splittingName);
		
		
		return query.uniqueResult();
		
	}

}
