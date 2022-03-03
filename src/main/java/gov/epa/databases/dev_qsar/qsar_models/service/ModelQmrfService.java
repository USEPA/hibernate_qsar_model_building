package gov.epa.databases.dev_qsar.qsar_models.service;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelQmrf;

public interface ModelQmrfService {
	
	public ModelQmrf findByModelId(Long modelId);
	
	public ModelQmrf findByModelId(Long modelId, Session session);
	
	public ModelQmrf create(ModelQmrf modelQmrf) throws ConstraintViolationException;
	
	public ModelQmrf create(ModelQmrf modelQmrf, Session session) throws ConstraintViolationException;
	
	public void delete(ModelQmrf modelQmrf);
	
	public void delete(ModelQmrf modelQmrf, Session session);

}
