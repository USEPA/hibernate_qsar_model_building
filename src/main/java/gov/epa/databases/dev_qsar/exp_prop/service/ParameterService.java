package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import jakarta.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.Parameter;

public interface ParameterService {
	
	public Parameter findByName(String parameterName);
	
	public Parameter findByName(String parameterName, Session session);
	
	public List<Parameter> findAll();
	
	public List<Parameter> findAll(Session session);
	
	public Parameter create(Parameter parameter) throws ConstraintViolationException;
	
	public Parameter create(Parameter parameter, Session session) throws ConstraintViolationException;
	
}
