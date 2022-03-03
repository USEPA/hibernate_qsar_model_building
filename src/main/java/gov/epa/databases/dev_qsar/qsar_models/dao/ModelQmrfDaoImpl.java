package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelQmrf;

public class ModelQmrfDaoImpl implements ModelQmrfDao {
	
	private static final String HQL_BY_MODEL_ID = "select mq from ModelQmrf mq "
			+ "join mq.model m "
			+ "where m.id = :modelId";

	@Override
	public ModelQmrf findByModelId(Long modelId, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_MODEL_ID);
		query.setParameter("modelId", modelId);
		return (ModelQmrf) query.uniqueResult();
	}

}
