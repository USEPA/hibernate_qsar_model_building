package gov.epa.databases.dev_qsar.qsar_models.service;

import jakarta.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInModelSet;

public interface ModelInModelSetService {

	public ModelInModelSet create(ModelInModelSet modelInModelSet) throws ConstraintViolationException;
	
	public ModelInModelSet create(ModelInModelSet modelInModelSet, Session session) throws ConstraintViolationException;
	
	public ModelInModelSet findByModelIdAndModelSetId(Long modelId, Long modelSetId);
	
	public ModelInModelSet findByModelIdAndModelSetId(Long modelId, Long modelSetId, Session session);
	
	public void delete(ModelInModelSet modelInModelSet);
	
	public void delete(ModelInModelSet modelInModelSet, Session session);

}
