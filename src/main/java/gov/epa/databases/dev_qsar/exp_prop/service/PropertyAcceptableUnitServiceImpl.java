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
import gov.epa.databases.dev_qsar.exp_prop.dao.PropertyAcceptableUnitDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.PropertyAcceptableUnitDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyAcceptableUnit;

public class PropertyAcceptableUnitServiceImpl implements PropertyAcceptableUnitService {
	
	private Validator validator;
	
	public PropertyAcceptableUnitServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public List<PropertyAcceptableUnit> findAll() {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	public List<PropertyAcceptableUnit> findAll(Session session) {
		Transaction t = session.beginTransaction();
		PropertyAcceptableUnitDao propertyAcceptableUnitDao = new PropertyAcceptableUnitDaoImpl();
		List<PropertyAcceptableUnit> propertyAcceptableUnits = propertyAcceptableUnitDao.findAll(session);
		t.rollback();
		return propertyAcceptableUnits;
	}

	@Override
	public PropertyAcceptableUnit create(PropertyAcceptableUnit propertyAcceptableUnit) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return create(propertyAcceptableUnit, session);
	}

	@Override
	public PropertyAcceptableUnit create(PropertyAcceptableUnit propertyAcceptableUnit, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<PropertyAcceptableUnit>> violations = validator.validate(propertyAcceptableUnit);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.persist(propertyAcceptableUnit);
			session.flush();
			session.refresh(propertyAcceptableUnit);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		} catch (Exception e) {
			t.rollback();
			throw e;
		}
		
		return propertyAcceptableUnit;
	}

}
