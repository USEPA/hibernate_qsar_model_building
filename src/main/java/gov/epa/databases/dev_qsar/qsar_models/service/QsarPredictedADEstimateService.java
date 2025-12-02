package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedADEstimate;


import jakarta.validation.ConstraintViolationException;

public interface QsarPredictedADEstimateService {
	
	
	public QsarPredictedADEstimate create(QsarPredictedADEstimate QsarPredictedADEstimate) throws ConstraintViolationException;
	
	public QsarPredictedADEstimate create(QsarPredictedADEstimate QsarPredictedADEstimate, Session session) throws ConstraintViolationException;

	public List<QsarPredictedADEstimate> createBatch(List<QsarPredictedADEstimate> qsarPredictedADEstimates)
			throws ConstraintViolationException;

	public List<QsarPredictedADEstimate> createBatch(List<QsarPredictedADEstimate> qsarPredictedADEstimates, Session session)
			throws ConstraintViolationException;

	public List<QsarPredictedADEstimate> findById(Long predictionDashboardId);

	public 	List<QsarPredictedADEstimate> findById(Long predictionDashboardId, Session session);

}
