package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.Transaction;
import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.ExpPropPropertyDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.ExpPropPropertyDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

public class ExpPropPropertyServiceImpl implements ExpPropPropertyService {
	
	private Validator validator;
	
	public ExpPropPropertyServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public List<ExpPropProperty> findByPropertyCategoryName(String propertyCategoryName) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findByPropertyCategoryName(propertyCategoryName, session);
	}
	
	public List<ExpPropProperty> findByPropertyCategoryName(String propertyCategoryName, Session session) {
		Transaction t = session.beginTransaction();
		ExpPropPropertyDao expPropPropertyDao = new ExpPropPropertyDaoImpl();
		List<ExpPropProperty> expPropProperty = expPropPropertyDao.findByPropertyCategoryName(propertyCategoryName, session);
		t.rollback();
		return expPropProperty;
	}

	public ExpPropProperty findByPropertyName(String propertyName) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findByPropertyName(session,propertyName);
	}

	
	private ExpPropProperty findByPropertyName(Session session, String propertyName) {
		Transaction t = session.beginTransaction();
		ExpPropPropertyDao expPropPropertyDao = new ExpPropPropertyDaoImpl();
		ExpPropProperty expPropProperty = expPropPropertyDao.findByPropertyName(propertyName,session);
		t.rollback();
		return expPropProperty;
	}

	public List<ExpPropProperty> findAll() {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	public List<ExpPropProperty> findAll(Session session) {
		Transaction t = session.beginTransaction();
		ExpPropPropertyDao expPropPropertyDao = new ExpPropPropertyDaoImpl();
		List<ExpPropProperty> expPropPropertys = expPropPropertyDao.findAll(session);
		t.rollback();
		return expPropPropertys;
	}

	@Override
	public ExpPropProperty create(ExpPropProperty property) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return create(property, session);
	}

	@Override
	public ExpPropProperty create(ExpPropProperty property, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<ExpPropProperty>> violations = validator.validate(property);
		if (!violations.isEmpty()) {
			throw new jakarta.validation.ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.persist(property);
			session.flush();
			session.refresh(property);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		} catch (Exception e) {
			t.rollback();
			throw e;
		}
		
		return property;
	}

	
	@Override
	public void delete(ExpPropProperty property) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		delete(property,session);
	}

	@Override
	public void delete(ExpPropProperty property, Session session) {
		
		Transaction t = session.beginTransaction();
		
		try {
			
			session.remove(property);
			session.flush();
//			session.refresh(property);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		} catch (Exception e) {
			t.rollback();
			throw e;
		}
		
	}

	


}
