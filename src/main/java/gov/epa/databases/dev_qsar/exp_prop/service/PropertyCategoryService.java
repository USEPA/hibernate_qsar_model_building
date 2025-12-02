package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import jakarta.validation.ConstraintViolationException;

import org.hibernate.Session;


import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyCategory;


/**
 * @author TMARTI02
 *
 */
public interface PropertyCategoryService {

	public PropertyCategory create(PropertyCategory propertyCategory, Session session) throws ConstraintViolationException;

	public PropertyCategory create(PropertyCategory propertyCategory) throws ConstraintViolationException;

	public List<PropertyCategory> findAll();

	public List<PropertyCategory> findAll(Session session);

}
