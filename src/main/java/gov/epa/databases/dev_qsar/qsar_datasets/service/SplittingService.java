package gov.epa.databases.dev_qsar.qsar_datasets.service;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;

public interface SplittingService {
	
	public Splitting findByName(String splittingName);
	
	public Splitting findByName(String splittingName, Session session);
	
	public Splitting create(Splitting splitting) throws ConstraintViolationException;
	
	public Splitting create(Splitting splitting, Session session) throws ConstraintViolationException;

}
