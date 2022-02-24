package gov.epa.databases.dev_qsar.qsar_models.service;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes;

public interface ModelBytesService {
	
	public ModelBytes findByModelId(Long modelId);
	
	public ModelBytes findByModelId(Long modelId, Session session);
	
	public ModelBytes create(ModelBytes modelBytes) throws ConstraintViolationException;
	
	public ModelBytes create(ModelBytes modelBytes, Session session) throws ConstraintViolationException;
	
	public void delete(ModelBytes modelBytes);
	
	public void delete(ModelBytes modelBytes, Session session);

}
