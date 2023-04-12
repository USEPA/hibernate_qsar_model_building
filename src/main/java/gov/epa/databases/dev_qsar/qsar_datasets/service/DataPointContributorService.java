package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointContributor;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;

public interface DataPointContributorService {
	
	public DataPointContributor create(DataPointContributor dataPointContributor) throws ConstraintViolationException;
	
	public DataPointContributor create(DataPointContributor dataPointContributor, Session session) throws ConstraintViolationException;
	
	public List<DataPointContributor> createBatch(List<DataPointContributor> dataPointContributors) throws ConstraintViolationException;
	
	public List<DataPointContributor> createBatch(List<DataPointContributor> dataPointContributors, Session session) throws ConstraintViolationException;


	public void createBatchSQL(Dataset dataset, List<DataPointContributor> dataPointContributors);


}
