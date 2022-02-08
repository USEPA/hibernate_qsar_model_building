package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.MethodDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.MethodDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public class MethodServiceImpl implements MethodService {
	
	private Validator validator;
	
	public MethodServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public Method findByName(String methodName) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByName(methodName, session);
	}
	
	public Method findByName(String methodName, Session session) {
		Transaction t = session.beginTransaction();
		MethodDao methodDao = new MethodDaoImpl();
		Method method = methodDao.findByName(methodName, session);
		t.rollback();
		return method;
	}
	
	@Override
	public Method create(Method method) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(method, session);
	}

	@Override
	public Method create(Method method, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<Method>> violations = validator.validate(method);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(method);
			session.flush();
			session.refresh(method);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return method;
	}

}
