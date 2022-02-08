package gov.epa.databases.dev_qsar.qsar_datasets.service;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;

public interface PropertyService {
	
	public Property findByName(String propertyName);
	
	public Property findByName(String propertyName, Session session);
	
	public Property create(Property property) throws ConstraintViolationException;
	
	public Property create(Property property, Session session) throws ConstraintViolationException;

}
