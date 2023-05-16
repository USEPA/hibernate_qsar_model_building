package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelQmrf;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport;

public class PredictionReportDaoImpl implements PredictionReportDao {
	
	private static final String HQL_BY_PREDICTION_DASHBOARD_ID = "select pr from PredictionReport pr "
			+ "join pr.predictionDashboard pd "
			+ "where pd.id = :predictionDashboardId";

	@Override
	public PredictionReport findByPredictionDashboardId(Long predictionDashboardId, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_PREDICTION_DASHBOARD_ID);
		query.setParameter("predictionDashboardId", predictionDashboardId);
		return (PredictionReport) query.uniqueResult();
	}


}
