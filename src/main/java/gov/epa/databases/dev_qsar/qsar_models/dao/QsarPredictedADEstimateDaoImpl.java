package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedADEstimate;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor;

public class QsarPredictedADEstimateDaoImpl implements QsarPredictedADEstimateDao {

	
	private static final String HQL_BY_ID = "select q from QsarPredictedADEstimate q \n"+
			"join q.predictionDashboard p\n"+
			"where p.id = :predictionDashboardId";

	
	@Override
	public List<QsarPredictedADEstimate> findById(Long predictionDashboardId, Session session) {
		// TODO Auto-generated method stub
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_ID);
			
		query.setParameter("predictionDashboardId", predictionDashboardId);
		return (List<QsarPredictedADEstimate>) query.list();

	}
}
