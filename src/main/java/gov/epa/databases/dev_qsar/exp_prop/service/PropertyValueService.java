package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;

public interface PropertyValueService {
	
	public PropertyValue findByExpPropId(String expPropId);
	
	public PropertyValue findByExpPropId(String expPropId, Session session);
	
	public List<PropertyValue> findByPropertyNameWithOptions(String propertyName, boolean useKeep, boolean omitValueQualifiers);
	
	public List<PropertyValue> findByPropertyNameWithOptions(String propertyName, boolean useKeep, boolean omitValueQualifiers, Session session);

	public PropertyValue create(PropertyValue propertyValue) throws ConstraintViolationException;
	
	public PropertyValue create(PropertyValue propertyValue, Session session) throws ConstraintViolationException;

	public PropertyValue update(PropertyValue propertyValue) throws ConstraintViolationException;

	public PropertyValue update(PropertyValue propertyValue, Session session) throws ConstraintViolationException;

	public List<PropertyValue> update(List<PropertyValue> propertyValues) throws ConstraintViolationException;

	public List<PropertyValue> update(List<PropertyValue> propertyValues, Session session) throws ConstraintViolationException;

	
	public void delete(List<PropertyValue> propertyValues) throws ConstraintViolationException;

	public void delete(List<PropertyValue> propertyValues, Session session);

	
}
