package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor;

import jakarta.validation.ConstraintViolationException;

public interface QsarPredictedNeighborService {
		
	public QsarPredictedNeighbor create(QsarPredictedNeighbor QsarPredictedNeighbor) throws ConstraintViolationException;
	
	public QsarPredictedNeighbor create(QsarPredictedNeighbor QsarPredictedNeighbor, Session session) throws ConstraintViolationException;

	public List<QsarPredictedNeighbor> createBatch(List<QsarPredictedNeighbor> QsarPredictedNeighbors)
			throws ConstraintViolationException;

	public List<QsarPredictedNeighbor> createBatch(List<QsarPredictedNeighbor> QsarPredictedNeighbors, Session session)
			throws ConstraintViolationException;

	public List<QsarPredictedNeighbor> findById(Long predictionDashboardId);

	public List<QsarPredictedNeighbor> findById(Long predictionDashboardId, Session session);

}
