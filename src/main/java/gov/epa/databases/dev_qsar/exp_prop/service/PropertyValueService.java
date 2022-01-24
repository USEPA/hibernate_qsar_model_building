package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;

public interface PropertyValueService {
	
	public List<PropertyValue> findByPropertyNameWithOptions(String propertyName, boolean useKeep, boolean omitValueQualifiers);
	
	public List<PropertyValue> findByPropertyNameWithOptions(String propertyName, boolean useKeep, boolean omitValueQualifiers, Session session);

}
