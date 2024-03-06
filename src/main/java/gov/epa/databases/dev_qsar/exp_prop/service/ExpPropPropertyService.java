package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;

public interface ExpPropPropertyService {
	
	public List<ExpPropProperty> findByPropertyCategoryName(String propertyCategoryName);
	
	public List<ExpPropProperty> findByPropertyCategoryName(String propertyCategoryName, Session session);
	
	public List<ExpPropProperty> findAll();
	
	public List<ExpPropProperty> findAll(Session session);
	
	public ExpPropProperty create(ExpPropProperty property) throws ConstraintViolationException;
	
	public ExpPropProperty create(ExpPropProperty property, Session session) throws ConstraintViolationException;

	public void delete(ExpPropProperty property, Session session);
	public void delete(ExpPropProperty property);

}
