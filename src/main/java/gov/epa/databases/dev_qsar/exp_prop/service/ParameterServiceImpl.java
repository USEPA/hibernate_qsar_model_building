package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.ParameterDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.ParameterDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.Parameter;

public class ParameterServiceImpl implements ParameterService {
	
	private Validator validator;
	
	public ParameterServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
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

	@Override
	public Parameter create(Parameter parameter) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return create(parameter, session);
	}

	@Override
	public Parameter create(Parameter parameter, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<Parameter>> violations = validator.validate(parameter);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.persist(parameter);
			session.flush();
			session.refresh(parameter);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		} catch (Exception e) {
			t.rollback();
			throw e;
		}
		
		return parameter;
	}

}
