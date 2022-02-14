package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;

public class ExpPropUnitDaoImpl implements ExpPropUnitDao {
	
	private static final String HQL_BY_NAME = "from ExpPropUnit epu where epu.name = :unitName";
	private static final String HQL_ALL = "from ExpPropUnit";

	@Override
	public ExpPropUnit findByName(String unitName, Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("unitName", unitName);
		return (ExpPropUnit) query.uniqueResult();
	}
	
	@Override
	public List<ExpPropUnit> findAll(Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_ALL);
		return (List<ExpPropUnit>) query.list();
	}

}
