package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;

public class PredictionDaoImpl implements PredictionDao {
	
	private static final String HQL_BY_IDs = "select p from Prediction p "
				+ "join p.model m "
				+ "join p.splitting s "
				+ "where m.id = :modelId and s.id = :splittingId";
		
		
	@Override
	public List<Prediction> findByIds(Long modelId, Long splittingId, Session session) {
		
//		System.out.println(HQL_BY_IDs);
		
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_IDs);
			
		query.setParameter("modelId", modelId);
		query.setParameter("splittingId", splittingId);
		return (List<Prediction>) query.list();
	}

}
