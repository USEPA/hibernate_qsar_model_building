package gov.epa.databases.dev_qsar.exp_prop.service;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;

public interface ExpPropUnitService {
	
	public ExpPropUnit findByName(String unitName);
	
	public ExpPropUnit findByName(String unitName, Session session);

}
