package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;

public interface ExpPropPropertyService {
	
	public List<ExpPropProperty> findByPropertyCategoryName(String propertyCategoryName);
	
	public List<ExpPropProperty> findByPropertyCategoryName(String propertyCategoryName, Session session);
	
	public List<ExpPropProperty> findAll();
	
	public List<ExpPropProperty> findAll(Session session);

}
