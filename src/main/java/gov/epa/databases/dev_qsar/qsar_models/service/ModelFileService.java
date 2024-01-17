package gov.epa.databases.dev_qsar.qsar_models.service;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelFile;

public interface ModelFileService {
	
	public ModelFile findByModelId(Long modelId, Long fileTypeId);
	
	public ModelFile findByModelId(Long modelId, Long fileTypeId, Session session);
	
	public ModelFile create(ModelFile modelQmrf) throws ConstraintViolationException;
	
	public ModelFile create(ModelFile modelQmrf, Session session) throws ConstraintViolationException;
	
	public void delete(ModelFile modelFile);
	
	public void delete(ModelFile modelFile, Session session);

}
