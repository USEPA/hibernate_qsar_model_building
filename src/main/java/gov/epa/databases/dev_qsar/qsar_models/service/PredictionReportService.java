package gov.epa.databases.dev_qsar.qsar_models.service;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport;

public interface PredictionReportService {
	
	public PredictionReport findByPredictionDashboardId(Long modelId);
	
	public PredictionReport findByPredictionDashboardId(Long modelId, Session session);
	
	public PredictionReport create(PredictionReport predictionReport) throws ConstraintViolationException;
	
	public PredictionReport create(PredictionReport predictionReport, Session session) throws ConstraintViolationException;
	
	public void delete(PredictionReport predictionReport);
	
	public void delete(PredictionReport predictionReport, Session session);

}
