package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyAcceptableUnit;

public class PropertyAcceptableUnitDaoImpl implements PropertyAcceptableUnitDao {

	private static final String HQL_ALL = "from PropertyAcceptableUnit";
	
	@Override
	public List<PropertyAcceptableUnit> findAll(Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_ALL);
		return (List<PropertyAcceptableUnit>) query.list();
	}

}
