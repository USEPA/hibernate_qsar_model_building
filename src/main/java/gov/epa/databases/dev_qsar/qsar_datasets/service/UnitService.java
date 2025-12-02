package gov.epa.databases.dev_qsar.qsar_datasets.service;

import jakarta.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;

public interface UnitService {
	
	public Unit findByName(String unitName);
	
	public Unit findByName(String unitName, Session session);
	
	public Unit create(Unit unit) throws ConstraintViolationException;
	
	public Unit create(Unit unit, Session session) throws ConstraintViolationException;

}
