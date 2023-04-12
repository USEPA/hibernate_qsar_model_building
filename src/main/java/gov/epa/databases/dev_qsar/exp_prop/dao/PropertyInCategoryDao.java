package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyInCategory;

public interface PropertyInCategoryDao {

	public List<PropertyInCategory> findAll(Session session);

}
