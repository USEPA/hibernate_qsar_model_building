package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;

public interface PropertyService {
	
	public Property findByName(String propertyName);
	
	public Property findByName(String propertyName, Session session);
	
	public Set<ConstraintViolation<Property>> create(Property property);
	
	public Set<ConstraintViolation<Property>> create(Property property, Session session);

}
