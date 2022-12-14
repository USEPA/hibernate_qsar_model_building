package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;

public class DescriptorEmbeddingDaoImpl implements DescriptorEmbeddingDao {
	
	private static final String HQL_BY_NAME = 
			"from DescriptorEmbedding de where de.name = :descriptorEmbeddingName";
	
	private static final String HQL_BY_FIELDS = 
			"from DescriptorEmbedding de where de.qsar_method = :qsar_method:"
			+ " and de.dataset_name = :dataset_name: and de.descriptor_set_name = :descriptor_set_name:"
					+ " and de.description = :description:";

	@Override
	public DescriptorEmbedding findByName(String descriptorEmbeddingName, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("descriptorEmbeddingName", descriptorEmbeddingName);
		return (DescriptorEmbedding) query.uniqueResult();
	}

	@Override
	public DescriptorEmbedding findByGASettings(String qsar_method, String dataset_name, String descriptor_set_name, 
			String description, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("qsar_method", qsar_method);
		query.setParameter("dataset_name", dataset_name);
		query.setParameter("descriptor_set_name",descriptor_set_name);
		query.setParameter("descriptionJson",description);
		return (DescriptorEmbedding) query.uniqueResult();


		
	}

}
