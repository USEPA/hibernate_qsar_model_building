package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import jakarta.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;

public interface ExpPropUnitService {
	
	public ExpPropUnit findByName(String unitName);
	
	public ExpPropUnit findByName(String unitName, Session session);
	
	public List<ExpPropUnit> findAll();
	
	public List<ExpPropUnit> findAll(Session session);
	
	public ExpPropUnit create(ExpPropUnit unit) throws ConstraintViolationException;
	
	public ExpPropUnit create(ExpPropUnit unit, Session session) throws ConstraintViolationException;
	
}
