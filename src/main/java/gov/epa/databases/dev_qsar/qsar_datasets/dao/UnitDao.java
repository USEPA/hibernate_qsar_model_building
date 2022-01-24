package gov.epa.databases.dev_qsar.qsar_datasets.dao;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;

public interface UnitDao {
	
	public Unit findByName(String unitName, Session session);

}
