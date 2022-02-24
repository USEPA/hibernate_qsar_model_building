package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyAcceptableUnit;

public interface PropertyAcceptableUnitService {
	
	public List<PropertyAcceptableUnit> findAll();
	
	public List<PropertyAcceptableUnit> findAll(Session session);
	
	public PropertyAcceptableUnit create(PropertyAcceptableUnit propertyAcceptableUnit) throws ConstraintViolationException;
	
	public PropertyAcceptableUnit create(PropertyAcceptableUnit propertyAcceptableUnit, Session session) throws ConstraintViolationException;
	
}
