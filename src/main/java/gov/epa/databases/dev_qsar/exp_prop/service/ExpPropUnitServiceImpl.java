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
import gov.epa.databases.dev_qsar.exp_prop.dao.ExpPropUnitDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.ExpPropUnitDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;

public class ExpPropUnitServiceImpl implements ExpPropUnitService {
	
	private Validator validator;
	
	public ExpPropUnitServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public ExpPropUnit findByName(String unitName) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findByName(unitName, session);
	}
	
	public ExpPropUnit findByName(String unitName, Session session) {
		Transaction t = session.beginTransaction();
		ExpPropUnitDao expPropUnitDao = new ExpPropUnitDaoImpl();
		ExpPropUnit expPropUnit = expPropUnitDao.findByName(unitName, session);
		t.rollback();
		return expPropUnit;
	}
	
	public List<ExpPropUnit> findAll() {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	public List<ExpPropUnit> findAll(Session session) {
		Transaction t = session.beginTransaction();
		ExpPropUnitDao expPropUnitDao = new ExpPropUnitDaoImpl();
		List<ExpPropUnit> expPropUnits = expPropUnitDao.findAll(session);
		t.rollback();
		return expPropUnits;
	}
	
	@Override
	public ExpPropUnit create(ExpPropUnit unit) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return create(unit, session);
	}

	@Override
	public ExpPropUnit create(ExpPropUnit unit, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<ExpPropUnit>> violations = validator.validate(unit);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.persist(unit);
			session.flush();
			session.refresh(unit);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		} catch (Exception e) {
			t.rollback();
			throw e;
		}
		
		return unit;
	}

}
