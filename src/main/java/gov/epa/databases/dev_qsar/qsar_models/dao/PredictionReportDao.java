package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport;

public interface PredictionReportDao {
	
	public PredictionReport findByPredictionDashboardId(Long predictionDashboardId, Session session);

}
