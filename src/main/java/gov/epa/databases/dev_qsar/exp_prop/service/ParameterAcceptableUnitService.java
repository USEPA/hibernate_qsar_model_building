package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterAcceptableUnit;

public interface ParameterAcceptableUnitService {
	
	public List<ParameterAcceptableUnit> findAll();
	
	public List<ParameterAcceptableUnit> findAll(Session session);
	
	public ParameterAcceptableUnit create(ParameterAcceptableUnit parameterAcceptableUnit) throws ConstraintViolationException;
	
	public ParameterAcceptableUnit create(ParameterAcceptableUnit parameterAcceptableUnit, Session session) throws ConstraintViolationException;
	
}
