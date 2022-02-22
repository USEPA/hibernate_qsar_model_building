package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.ParameterAcceptableUnitDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.ParameterAcceptableUnitDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterAcceptableUnit;

public class ParameterAcceptableUnitServiceImpl implements ParameterAcceptableUnitService {
	
	private Validator validator;
	
	public ParameterAcceptableUnitServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public List<ParameterAcceptableUnit> findAll() {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	public List<ParameterAcceptableUnit> findAll(Session session) {
		Transaction t = session.beginTransaction();
		ParameterAcceptableUnitDao parameterAcceptableUnitDao = new ParameterAcceptableUnitDaoImpl();
		List<ParameterAcceptableUnit> parameterAcceptableUnits = parameterAcceptableUnitDao.findAll(session);
		t.rollback();
		return parameterAcceptableUnits;
	}

	@Override
	public ParameterAcceptableUnit create(ParameterAcceptableUnit parameterAcceptableUnit) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return create(parameterAcceptableUnit, session);
	}

	@Override
	public ParameterAcceptableUnit create(ParameterAcceptableUnit parameterAcceptableUnit, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<ParameterAcceptableUnit>> violations = validator.validate(parameterAcceptableUnit);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(parameterAcceptableUnit);
			session.flush();
			session.refresh(parameterAcceptableUnit);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		} catch (Exception e) {
			t.rollback();
			throw e;
		}
		
		return parameterAcceptableUnit;
	}

}
