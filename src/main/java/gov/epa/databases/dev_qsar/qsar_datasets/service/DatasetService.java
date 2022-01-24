package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;

public interface DatasetService {
	
	public Dataset findByName(String datasetName);
	
	public Dataset findByName(String datasetName, Session session);
	
	public Set<ConstraintViolation<Dataset>> create(Dataset dataset);
	
	public Set<ConstraintViolation<Dataset>> create(Dataset dataset, Session session);

}
