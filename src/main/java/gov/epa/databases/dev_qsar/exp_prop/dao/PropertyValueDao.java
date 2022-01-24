package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;

public interface PropertyValueDao {
	
	public List<PropertyValue> findByPropertyNameWithOptions(String propertyName, boolean useKeep, boolean omitValueQualifiers, Session session);

}
