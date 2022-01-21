package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInModelSet;

public class ModelInModelSetDaoImpl implements ModelInModelSetDao {
	
	private static final String HQL_BY_MODEL_ID_AND_MODEL_SET_ID = "select mms from ModelInModelSet mms "
			+ "join mms.model m "
			+ "join mms.modelSet ms "
			+ "where m.id = :modelId and ms.id = :modelSetId";

	@Override
	public ModelInModelSet findByModelIdAndModelSetId(Long modelId, Long modelSetId, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_MODEL_ID_AND_MODEL_SET_ID);
		query.setParameter("modelId", modelId);
		query.setParameter("modelSetId", modelSetId);
		return (ModelInModelSet) query.uniqueResult();
	}

}
