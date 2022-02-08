package gov.epa.databases.dev_qsar.qsar_datasets.service;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointContributor;

public interface DataPointContributorService {
	
	public DataPointContributor create(DataPointContributor dataPointContributor) throws ConstraintViolationException;
	
	public DataPointContributor create(DataPointContributor dataPointContributor, Session session) throws ConstraintViolationException;

}
