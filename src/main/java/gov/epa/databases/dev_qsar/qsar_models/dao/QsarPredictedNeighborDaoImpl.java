package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor;


public class QsarPredictedNeighborDaoImpl implements QsarPredictedNeighborDao {

	private static final String HQL_BY_ID = "select q from QsarPredictedNeighbor q \n"+
			"join q.predictionDashboard p\n"+
			"where p.id = :predictionDashboardId";

	
	@Override
	public List<QsarPredictedNeighbor> findById(Long predictionDashboardId, Session session) {
		// TODO Auto-generated method stub
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_ID);
			
		query.setParameter("predictionDashboardId", predictionDashboardId);
		return (List<QsarPredictedNeighbor>) query.list();

	}

	
}
