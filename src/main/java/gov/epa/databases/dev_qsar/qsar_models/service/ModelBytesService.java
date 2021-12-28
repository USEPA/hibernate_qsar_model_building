package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes;

public interface ModelBytesService {
	
	public ModelBytes findByModelId(Long modelId);
	
	public ModelBytes findByModelId(Long modelId, Session session);
	
	public Set<ConstraintViolation<ModelBytes>> create(ModelBytes modelBytes);
	
	public Set<ConstraintViolation<ModelBytes>> create(ModelBytes modelBytes, Session session);

}
