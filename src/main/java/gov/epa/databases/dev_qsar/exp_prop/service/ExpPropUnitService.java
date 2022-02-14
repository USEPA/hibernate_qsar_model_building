package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;

public interface ExpPropUnitService {
	
	public ExpPropUnit findByName(String unitName);
	
	public ExpPropUnit findByName(String unitName, Session session);
	
	public List<ExpPropUnit> findAll();
	
	public List<ExpPropUnit> findAll(Session session);
	
}
