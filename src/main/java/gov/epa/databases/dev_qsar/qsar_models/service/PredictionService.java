package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.endpoints.models.ModelPrediction;

import javax.validation.ConstraintViolationException;

public interface PredictionService {
	
	public List<Prediction> findByIds(Long modelId,Long splittingId);
	
	public List<Prediction> findByIds(Long modelId, Long splittingId, Session session);
	
//	public void create(List<Prediction> predictions) throws ConstraintViolationException;
	
	public Prediction create(Prediction prediction, Session session) throws ConstraintViolationException;

	Prediction create(Prediction prediction) throws ConstraintViolationException;

	

}
