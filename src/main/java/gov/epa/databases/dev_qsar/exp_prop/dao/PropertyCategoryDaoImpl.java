package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;
import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyCategory;

/**
 * @author TMARTI02
 *
 */
public class PropertyCategoryDaoImpl implements PropertyCategoryDao {

	private static final String HQL_BY_NAME = "from PropertyCategory pc where pc.name = :propertyCategoryName";
	private static final String HQL_ALL = "from PropertyCategory";

	@Override
	public PropertyCategory findByName(String propertyCategoryName, Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("propertyCategoryName", propertyCategoryName);
		return (PropertyCategory) query.uniqueResult();
	}
	
	@Override
	public List<PropertyCategory> findAll(Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_ALL);
		return (List<PropertyCategory>) query.list();
	}
}
