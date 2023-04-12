package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyCategory;

public interface PropertyCategoryDao {

	public List<PropertyCategory> findAll(Session session);

	public PropertyCategory findByName(String propertyCategoryName, Session session);

}
