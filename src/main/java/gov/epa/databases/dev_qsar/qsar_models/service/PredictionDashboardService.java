package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;

import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;

public interface PredictionDashboardService {

	public PredictionDashboard create(PredictionDashboard predictionDashboard) throws ConstraintViolationException;
	
	public PredictionDashboard create(PredictionDashboard predictionDashboard, Session session) throws ConstraintViolationException;
	
	public List<PredictionDashboard> createBatch(List<PredictionDashboard> predictionDashboard) throws ConstraintViolationException;
	
	public List<PredictionDashboard> createBatch(List<PredictionDashboard> predictionDashboard, Session session) throws ConstraintViolationException;

}

