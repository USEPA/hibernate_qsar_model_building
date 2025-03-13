package gov.epa.databases.dev_qsar.qsar_models.service;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelFile;

public interface ModelFileService {
	
	public ModelFile findByModelId(Long modelId, Long fileTypeId);
	
	public ModelFile findByModelId(Long modelId, Long fileTypeId, Session session);
	
	public ModelFile create(ModelFile modelFile) throws ConstraintViolationException;
	
	public ModelFile create(ModelFile modelFile, Session session) throws ConstraintViolationException;

	public ModelFile update(ModelFile modelFile) throws ConstraintViolationException;
	
	public ModelFile update(ModelFile modelFile, Session session) throws ConstraintViolationException;

	public void delete(ModelFile modelFile);
	
	public void delete(ModelFile modelFile, Session session);

}
