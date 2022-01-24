package gov.epa.databases.dev_qsar.exp_prop.dao;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;

public interface ExpPropUnitDao {
	
	public ExpPropUnit findByName(String unitName, Session session);

}
