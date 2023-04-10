package gov.epa.databases.dev_qsar.qsar_models.service;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;

import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionsDashboard;

public interface PredictionDashboardService {

	public PredictionsDashboard create(PredictionsDashboard predictionDashboard) throws ConstraintViolationException;
	
	public PredictionsDashboard create(PredictionsDashboard predictionDashboard, Session session) throws ConstraintViolationException;

}

