package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.ParameterDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.ParameterDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.Parameter;

public class ParameterServiceImpl implements ParameterService {
	
	public Parameter findByName(String parameterName) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findByName(parameterName, session);
	}
	
	public Parameter findByName(String parameterName, Session session) {
		Transaction t = session.beginTransaction();
		ParameterDao parameterDao = new ParameterDaoImpl();
		Parameter parameter = parameterDao.findByName(parameterName, session);
		t.rollback();
		return parameter;
	}
	
	public List<Parameter> findAll() {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	public List<Parameter> findAll(Session session) {
		Transaction t = session.beginTransaction();
		ParameterDao parameterDao = new ParameterDaoImpl();
		List<Parameter> parameters = parameterDao.findAll(session);
		t.rollback();
		return parameters;
	}

}
