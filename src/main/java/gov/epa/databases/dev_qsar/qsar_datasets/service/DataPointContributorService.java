package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointContributor;

public interface DataPointContributorService {
	
	public Set<ConstraintViolation<DataPointContributor>> create(DataPointContributor dataPointContributor);
	
	public Set<ConstraintViolation<DataPointContributor>> create(DataPointContributor dataPointContributor, Session session);

}
