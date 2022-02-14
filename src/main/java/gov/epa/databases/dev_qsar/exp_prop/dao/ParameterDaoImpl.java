package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.entity.Parameter;

public class ParameterDaoImpl implements ParameterDao {
	
	private static final String HQL_BY_NAME = "from Parameter p where p.name = :parameterName";
	private static final String HQL_ALL = "from Parameter";

	@Override
	public Parameter findByName(String parameterName, Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("parameterName", parameterName);
		return (Parameter) query.uniqueResult();
	}
	
	@Override
	public List<Parameter> findAll(Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_ALL);
		return (List<Parameter>) query.list();
	}

}
