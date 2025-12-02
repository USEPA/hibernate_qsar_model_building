package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import jakarta.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyAcceptableParameter;

public interface PropertyAcceptableParameterService {
	
	public List<PropertyAcceptableParameter> findAll();
	
	public List<PropertyAcceptableParameter> findAll(Session session);
	
	public PropertyAcceptableParameter create(PropertyAcceptableParameter propertyAcceptableParameter) throws ConstraintViolationException;
	
	public PropertyAcceptableParameter create(PropertyAcceptableParameter propertyAcceptableParameter, Session session) throws ConstraintViolationException;
	
}
