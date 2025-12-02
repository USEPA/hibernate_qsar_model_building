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
import gov.epa.databases.dev_qsar.exp_prop.dao.PropertyAcceptableParameterDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.PropertyAcceptableParameterDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyAcceptableParameter;

public class PropertyAcceptableParameterServiceImpl implements PropertyAcceptableParameterService {
	
	private Validator validator;
	
	public PropertyAcceptableParameterServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public List<PropertyAcceptableParameter> findAll() {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	public List<PropertyAcceptableParameter> findAll(Session session) {
		Transaction t = session.beginTransaction();
		PropertyAcceptableParameterDao propertyAcceptableParameterDao = new PropertyAcceptableParameterDaoImpl();
		List<PropertyAcceptableParameter> propertyAcceptableParameters = propertyAcceptableParameterDao.findAll(session);
		t.rollback();
		return propertyAcceptableParameters;
	}

	@Override
	public PropertyAcceptableParameter create(PropertyAcceptableParameter propertyAcceptableParameter) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return create(propertyAcceptableParameter, session);
	}

	@Override
	public PropertyAcceptableParameter create(PropertyAcceptableParameter propertyAcceptableParameter, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<PropertyAcceptableParameter>> violations = validator.validate(propertyAcceptableParameter);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.persist(propertyAcceptableParameter);
			session.flush();
			session.refresh(propertyAcceptableParameter);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		} catch (Exception e) {
			t.rollback();
			throw e;
		}
		
		return propertyAcceptableParameter;
	}

}
