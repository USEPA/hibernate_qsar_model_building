package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInModelSet;

public interface ModelInModelSetService {

	public Set<ConstraintViolation<ModelInModelSet>> create(ModelInModelSet modelInModelSet);
	
	public Set<ConstraintViolation<ModelInModelSet>> create(ModelInModelSet modelInModelSet, Session session);
	
	public ModelInModelSet findByModelIdAndModelSetId(Long modelId, Long modelSetId);
	
	public ModelInModelSet findByModelIdAndModelSetId(Long modelId, Long modelSetId, Session session);
	
	public void delete(ModelInModelSet modelInModelSet);
	
	public void delete(ModelInModelSet modelInModelSet, Session session);

}
