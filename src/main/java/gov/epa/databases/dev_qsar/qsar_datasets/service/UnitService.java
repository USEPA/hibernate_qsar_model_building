package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;

public interface UnitService {
	
	public Unit findByName(String unitName);
	
	public Unit findByName(String unitName, Session session);
	
	public Set<ConstraintViolation<Unit>> create(Unit unit);
	
	public Set<ConstraintViolation<Unit>> create(Unit unit, Session session);

}
