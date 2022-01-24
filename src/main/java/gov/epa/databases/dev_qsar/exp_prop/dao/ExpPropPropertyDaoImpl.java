package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;

public class ExpPropPropertyDaoImpl implements ExpPropPropertyDao {
	
	private static final String HQL_BY_PROPERTY_CATEGORY_NAME = "select epp from ExpPropProperty epp "
			+ "join epp.propertiesInCategories pic "
			+ "join pic.propertyCategory pc "
			+ "where pc.name = :propertyCategoryName";

	@Override
	public List<ExpPropProperty> findByPropertyCategoryName(String propertyCategoryName, Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_PROPERTY_CATEGORY_NAME);
		query.setParameter("propertyCategoryName", propertyCategoryName);
		return (List<ExpPropProperty>) query.list();
	}

}
