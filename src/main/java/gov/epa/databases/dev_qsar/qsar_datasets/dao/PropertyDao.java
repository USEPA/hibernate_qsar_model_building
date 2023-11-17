package gov.epa.databases.dev_qsar.qsar_datasets.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;

public interface PropertyDao {
	
	public Property findByName(String propertyName, Session session);

	public List<Property> findAll(Session session);

}
