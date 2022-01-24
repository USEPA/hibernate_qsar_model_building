package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

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
	public Set<ConstraintViolation<Unit>> create(Unit unit) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return create(unit, session);
	}

	@Override
	public Set<ConstraintViolation<Unit>> create(Unit unit, Session session) {
		Set<ConstraintViolation<Unit>> violations = validator.validate(unit);
		if (!violations.isEmpty()) {
			return violations;
		}
		
		Transaction t = session.beginTransaction();
		session.save(unit);
		session.flush();
		session.refresh(unit);
		t.commit();
		return null;
	}

}
