package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;

public class PredictionDaoImpl implements PredictionDao {
	
	private static final String HQL_BY_MODEL_ID = "select p from Prediction p "
			+ "join p.model m "
			+ "where m.id = :modelId";

	@Override
	public List<Prediction> findByModelId(Long modelId, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_MODEL_ID);
		query.setParameter("modelId", modelId);
		return (List<Prediction>) query.list();
	}

}
