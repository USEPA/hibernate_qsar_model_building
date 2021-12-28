package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;

import javax.validation.ConstraintViolation;

public interface PredictionService {
	
	public List<Prediction> findByModelId(Long modelId);
	
	public List<Prediction> findByModelId(Long modelId, Session session);
	
	public Set<ConstraintViolation<Prediction>> create(Prediction prediction);
	
	public Set<ConstraintViolation<Prediction>> create(Prediction prediction, Session session);

}
