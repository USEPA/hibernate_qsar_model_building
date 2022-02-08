package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;

public class PropertyValueDaoImpl implements PropertyValueDao {
	
	private static final String HQL_SELECT_WITH_FETCH = "select distinct pv from PropertyValue pv "
			+ "left join fetch pv.sourceChemical sc "
			+ "left join fetch pv.unit u "
			+ "left join fetch pv.property p "
			+ "left join fetch pv.parameterValues pav "
			+ "left join fetch pv.literatureSource ls "
			+ "left join fetch pv.publicSource ps ";
	private static final String HQL_WHERE_BY_PROPERTY_NAME = "where pv.property.name = :propertyName";
	private static final String HQL_WHERE_BY_PROPERTY_NAME_USE_KEEP = "where pv.property.name = :propertyName "
			+ "and pv.keep = true";
	private static final String HQL_WHERE_BY_PROPERTY_NAME_OMIT_VALUE_QUALIFIERS = "where pv.property.name = :propertyName "
			+ "and (pv.valueQualifier is null or pv.valueQualifier = '~')";
	private static final String HQL_WHERE_BY_PROPERTY_NAME_USE_KEEP_AND_OMIT_VALUE_QUALIFIERS = "where pv.property.name = :propertyName "
			+ "and pv.keep = true "
			+ "and (pv.valueQualifier is null or pv.valueQualifier = '~')";


	@Override
	public List<PropertyValue> findByPropertyNameWithOptions(String propertyName, boolean useKeep,
			boolean omitValueQualifiers, Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		
		String hql = HQL_SELECT_WITH_FETCH;
		if (useKeep && omitValueQualifiers) {
			hql += HQL_WHERE_BY_PROPERTY_NAME_USE_KEEP_AND_OMIT_VALUE_QUALIFIERS;
		} else if (useKeep) {
			hql += HQL_WHERE_BY_PROPERTY_NAME_USE_KEEP;
		} else if (omitValueQualifiers) {
			hql += HQL_WHERE_BY_PROPERTY_NAME_OMIT_VALUE_QUALIFIERS;
		} else {
			hql += HQL_WHERE_BY_PROPERTY_NAME;
		}
		
		Query query = session.createQuery(hql);
		query.setParameter("propertyName", propertyName);
		return (List<PropertyValue>) query.list();
	}

}
