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
	public Set<ConstraintViolation<Method>> create(Method method) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(method, session);
	}

	@Override
	public Set<ConstraintViolation<Method>> create(Method method, Session session) {
		Set<ConstraintViolation<Method>> violations = validator.validate(method);
		if (!violations.isEmpty()) {
			return violations;
		}
		
		Transaction t = session.beginTransaction();
		session.save(method);
		session.flush();
		session.refresh(method);
		t.commit();
		return null;
	}

}
