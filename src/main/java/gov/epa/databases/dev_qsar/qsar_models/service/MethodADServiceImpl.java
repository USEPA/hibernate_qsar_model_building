package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.ExpPropPropertyDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.ExpPropPropertyDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.MethodADDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.MethodADDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.dao.MethodDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.MethodDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.MethodAD;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public class MethodADServiceImpl implements MethodADService {
	
	private Validator validator;
	
	public MethodADServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public MethodAD findByName(String methodName) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByName(methodName, session);
	}
	
	public MethodAD findByName(String methodName, Session session) {
		Transaction t = session.beginTransaction();
		MethodADDao methodDao = new MethodADDaoImpl();
		MethodAD method = methodDao.findByName(methodName, session);
		t.rollback();
		return method;
	}
	
	@Override
	public MethodAD create(MethodAD methodAD) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(methodAD, session);
	}

	@Override
	public MethodAD create(MethodAD methodAD, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<MethodAD>> violations = validator.validate(methodAD);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(methodAD);
			session.flush();
			session.refresh(methodAD);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return methodAD;
	}

	
	@Override
	public List<MethodAD> findAll() {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	@Override
	public List<MethodAD> findAll(Session session) {
		Transaction t = session.beginTransaction();
		MethodADDao dao = new MethodADDaoImpl();
		List<MethodAD> methodADs = dao.findAll(session);
		t.rollback();
		return methodADs;
	}


}
