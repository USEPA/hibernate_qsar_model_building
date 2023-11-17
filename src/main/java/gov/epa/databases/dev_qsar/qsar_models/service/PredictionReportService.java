package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;

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

	public List<PredictionReport> createBatch(List<PredictionReport> reports) throws ConstraintViolationException;

	public 	List<PredictionReport> createBatch(List<PredictionReport> reports, Session session)
			throws ConstraintViolationException;

}
