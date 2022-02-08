package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;

import javax.validation.ConstraintViolationException;

public interface PredictionService {
	
	public List<Prediction> findByModelId(Long modelId);
	
	public List<Prediction> findByModelId(Long modelId, Session session);
	
	public Prediction create(Prediction prediction) throws ConstraintViolationException;
	
	public Prediction create(Prediction prediction, Session session) throws ConstraintViolationException;

}
