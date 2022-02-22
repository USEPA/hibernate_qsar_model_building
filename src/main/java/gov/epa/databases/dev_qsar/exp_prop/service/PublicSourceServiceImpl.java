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
import gov.epa.databases.dev_qsar.exp_prop.dao.PublicSourceDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.PublicSourceDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;

public class PublicSourceServiceImpl implements PublicSourceService {
	
	private Validator validator;
	
	public PublicSourceServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public PublicSource findByName(String sourceName) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findByName(sourceName, session);
	}
	
	public PublicSource findByName(String sourceName, Session session) {
		Transaction t = session.beginTransaction();
		PublicSourceDao publicSourceDao = new PublicSourceDaoImpl();
		PublicSource publicSource = publicSourceDao.findByName(sourceName, session);
		t.rollback();
		return publicSource;
	}
	
	public List<PublicSource> findAll() {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	public List<PublicSource> findAll(Session session) {
		Transaction t = session.beginTransaction();
		PublicSourceDao publicSourceDao = new PublicSourceDaoImpl();
		List<PublicSource> publicSources = publicSourceDao.findAll(session);
		t.rollback();
		return publicSources;
	}

	@Override
	public PublicSource create(PublicSource ps) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return create(ps, session);
	}

	@Override
	public PublicSource create(PublicSource ps, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<PublicSource>> violations = validator.validate(ps);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(ps);
			session.flush();
			session.refresh(ps);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		} catch (Exception e) {
			t.rollback();
			throw e;
		}
		
		return ps;
	}

}
