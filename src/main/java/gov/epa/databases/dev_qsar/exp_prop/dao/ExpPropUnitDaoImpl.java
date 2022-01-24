package gov.epa.databases.dev_qsar.exp_prop.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;

public class ExpPropUnitDaoImpl implements ExpPropUnitDao {
	
	private static final String HQL_BY_NAME = "from ExpPropUnit epu where epu.name = :unitName";

	@Override
	public ExpPropUnit findByName(String unitName, Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("unitName", unitName);
		return (ExpPropUnit) query.uniqueResult();
	}

}
