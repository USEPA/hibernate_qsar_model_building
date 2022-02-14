package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;

public interface ExpPropPropertyDao {
	
	public List<ExpPropProperty> findByPropertyCategoryName(String propertyCategoryName, Session session);
	
	public List<ExpPropProperty> findAll(Session session);

}
