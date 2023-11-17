package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;

public class PredictionDashboardDaoImpl implements PredictionDashboardDao {

	//Following works when Splitting is an object:
//	private static final String HQL_BY_IDs = "select p from Prediction p "
//				+ "join p.model m "
//				+ "join p.splitting s "
//				+ "where m.id = :modelId and s.id = :splittingId";
		
	//Following works when Splitting is just a foreign key:
	private static final String HQL_BY_IDs = "select p from PredictionDashboard p "
			+ "join p.model m \n"
			+ "join p.dsstoxRecord dr "
			+ "where m.id = :modelId and dr.id = :dsstoxRecordId";

	@Override
	public PredictionDashboard findByIds(Long modelId, Long dsstoxRecordId, Session session) {
		
//		System.out.println(HQL_BY_IDs);
		
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_IDs);
			
		query.setParameter("modelId", modelId);
		query.setParameter("dsstoxRecordId", dsstoxRecordId);
		return (PredictionDashboard) query.uniqueResult();
	}

}
