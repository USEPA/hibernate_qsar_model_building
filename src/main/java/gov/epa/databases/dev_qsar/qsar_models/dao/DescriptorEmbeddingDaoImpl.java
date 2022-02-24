package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;

public class DescriptorEmbeddingDaoImpl implements DescriptorEmbeddingDao {
	
	private static final String HQL_BY_NAME = 
			"from DescriptorEmbedding de where de.name = :descriptorEmbeddingName";

	@Override
	public DescriptorEmbedding findByName(String descriptorEmbeddingName, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("descriptorEmbeddingName", descriptorEmbeddingName);
		return (DescriptorEmbedding) query.uniqueResult();
	}

}
