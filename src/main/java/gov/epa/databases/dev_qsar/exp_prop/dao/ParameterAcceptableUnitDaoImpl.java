package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterAcceptableUnit;

public class ParameterAcceptableUnitDaoImpl implements ParameterAcceptableUnitDao {

	private static final String HQL_ALL = "from ParameterAcceptableUnit";
	
	@Override
	public List<ParameterAcceptableUnit> findAll(Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_ALL);
		return (List<ParameterAcceptableUnit>) query.list();
	}

}
