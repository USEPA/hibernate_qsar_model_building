package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.UnitDao;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.UnitDaoImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;

public class UnitServiceImpl implements UnitService {
	
	private Validator validator;
	
	public UnitServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public Unit findByName(String unitName) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findByName(unitName, session);
	}
	
	public Unit findByName(String unitName, Session session) {
		Transaction t = session.beginTransaction();
		UnitDao UnitDao = new UnitDaoImpl();
		Unit unit = UnitDao.findByName(unitName, session);
		t.rollback();
		return unit;
	}
	
	@Override
	public Unit create(Unit unit) throws ConstraintViolationException {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return create(unit, session);
	}

	@Override
	public Unit create(Unit unit, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<Unit>> violations = validator.validate(unit);
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
		}
		
		return unit;
	}

}
