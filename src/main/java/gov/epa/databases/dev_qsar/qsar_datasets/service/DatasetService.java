package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.List;

import jakarta.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;

public interface DatasetService {
	
	public Dataset findById(Long datasetId);
	
	public Dataset findById(Long datasetId, Session session);
	
	public Dataset findByName(String datasetName);
	
	public Dataset findByName(String datasetName, Session session);
	
	public Dataset create(Dataset dataset) throws ConstraintViolationException;
	
	public Dataset create(Dataset dataset, Session session) throws ConstraintViolationException;

	public List<Dataset> findAll();

	public List<Dataset> findAll(Session session);

}
