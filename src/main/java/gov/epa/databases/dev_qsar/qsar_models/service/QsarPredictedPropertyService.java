package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedProperty;

import javax.validation.ConstraintViolationException;

public interface QsarPredictedPropertyService {
	
	public List<QsarPredictedProperty> findByModelId(Long modelId);
	
	public List<QsarPredictedProperty> findByModelId(Long modelId, Session session);
	
	public QsarPredictedProperty find(Long modelId, String dtxcid,String canonQsarSmiles);
	public QsarPredictedProperty find(Long modelId, String dtxcid,String canonQsarSmiles, Session session);
	
	
	public QsarPredictedProperty create(QsarPredictedProperty QsarPredictedProperty) throws ConstraintViolationException;
	
	public QsarPredictedProperty create(QsarPredictedProperty QsarPredictedProperty, Session session) throws ConstraintViolationException;

}
