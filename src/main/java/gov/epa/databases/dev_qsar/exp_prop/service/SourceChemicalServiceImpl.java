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
import gov.epa.databases.dev_qsar.exp_prop.dao.ParameterDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.ParameterDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.dao.SourceChemicalDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.SourceChemicalDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.Parameter;
import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;

public class SourceChemicalServiceImpl implements SourceChemicalService {
	
	private Validator validator;
	
	public SourceChemicalServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}

	@Override
	public SourceChemical findMatch(SourceChemical sc) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findMatch(sc, session);
	}

	@Override
	public SourceChemical findMatch(SourceChemical sc, Session session) {
		Transaction t = session.beginTransaction();
		SourceChemicalDao sourceChemicalDao = new SourceChemicalDaoImpl();
		SourceChemical sourceChemical = sourceChemicalDao.findMatch(sc, session);
		t.rollback();
		return sourceChemical;
	}

	@Override
	public SourceChemical create(SourceChemical sourceChemical) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return create(sourceChemical, session);
	}

	@Override
	public SourceChemical create(SourceChemical sourceChemical, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<SourceChemical>> violations = validator.validate(sourceChemical);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(sourceChemical);
			session.flush();
			session.refresh(sourceChemical);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		} catch (Exception e) {
			t.rollback();
			throw e;
		}
		
		return sourceChemical;
	}
	
	@Override
	public List<SourceChemical> findAll() {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	@Override
	public List<SourceChemical> findAll(Session session) {
		Transaction t = session.beginTransaction();
		SourceChemicalDao sourceChemicalDao = new SourceChemicalDaoImpl();
		List<SourceChemical> parameters = sourceChemicalDao.findAll(session);
		t.rollback();
		return parameters;
	}
	
	@Override
	public List<SourceChemical> findAllFromSource(PublicSource ps) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findAllFromSource(ps, session);
	}
	
	@Override
	public List<SourceChemical> findAllFromSource(PublicSource ps,Session session) {
		Transaction t = session.beginTransaction();
		SourceChemicalDao sourceChemicalDao = new SourceChemicalDaoImpl();
		List<SourceChemical> parameters = sourceChemicalDao.findAll(ps, session);
		t.rollback();
		return parameters;
	}



}
