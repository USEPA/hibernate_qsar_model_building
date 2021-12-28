package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;

public interface SplittingService {
	
	public Splitting findByName(String splittingName);
	
	public Splitting findByName(String splittingName, Session session);
	
	public Set<ConstraintViolation<Splitting>> create(Splitting splitting);
	
	public Set<ConstraintViolation<Splitting>> create(Splitting splitting, Session session);

}
