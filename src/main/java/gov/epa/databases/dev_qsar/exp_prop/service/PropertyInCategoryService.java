package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import jakarta.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyAcceptableParameter;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyInCategory;

/**
 * @author TMARTI02
 *
 */
public interface PropertyInCategoryService {
	
	public List<PropertyInCategory> findAll();
	
	public List<PropertyInCategory> findAll(Session session);
	
	public PropertyInCategory create(PropertyInCategory propertyInCategory) throws ConstraintViolationException;
	
	public PropertyInCategory create(PropertyInCategory propertyInCategory, Session session) throws ConstraintViolationException;
	
}
