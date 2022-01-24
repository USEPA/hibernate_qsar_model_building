package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;

public class PropertyValueDaoImpl implements PropertyValueDao {
	
	private static final String HQL_BY_PROPERTY_NAME = "from PropertyValue pv where pv.property.name = :propertyName";
	private static final String HQL_BY_PROPERTY_NAME_USE_KEEP = "from PropertyValue pv "
			+ "where pv.property.name = :propertyName "
			+ "and pv.keep = true";
	private static final String HQL_BY_PROPERTY_NAME_OMIT_VALUE_QUALIFIERS = "from PropertyValue pv "
			+ "where pv.property.name = :propertyName "
			+ "and (pv.valueQualifier is null or pv.valueQualifier = '~')";
	private static final String HQL_BY_PROPERTY_NAME_USE_KEEP_AND_OMIT_VALUE_QUALIFIERS = "from PropertyValue pv "
			+ "where pv.property.name = :propertyName "
			+ "and pv.keep = true "
			+ "and (pv.valueQualifier is null or pv.valueQualifier = '~')";


	@Override
	public List<PropertyValue> findByPropertyNameWithOptions(String propertyName, boolean useKeep,
			boolean omitValueQualifiers, Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		
		String hql = null;
		if (useKeep && omitValueQualifiers) {
			hql = HQL_BY_PROPERTY_NAME_USE_KEEP_AND_OMIT_VALUE_QUALIFIERS;
		} else if (useKeep) {
			hql = HQL_BY_PROPERTY_NAME_USE_KEEP;
		} else if (omitValueQualifiers) {
			hql = HQL_BY_PROPERTY_NAME_OMIT_VALUE_QUALIFIERS;
		} else {
			hql = HQL_BY_PROPERTY_NAME;
		}
		
		Query query = session.createQuery(hql);
		query.setParameter("propertyName", propertyName);
		return (List<PropertyValue>) query.list();
	}

}
