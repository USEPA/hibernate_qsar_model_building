package gov.epa.databases.dev_qsar.qsar_datasets.dao;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;

public interface PropertyDao {
	
	public Property findByName(String propertyName, Session session);

}
