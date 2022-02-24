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
import gov.epa.databases.dev_qsar.exp_prop.dao.LiteratureSourceDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.LiteratureSourceDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;

public class LiteratureSourceServiceImpl implements LiteratureSourceService {
	
	private Validator validator;
	
	public LiteratureSourceServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public LiteratureSource findByName(String sourceName) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findByName(sourceName, session);
	}
	
	public LiteratureSource findByName(String sourceName, Session session) {
		Transaction t = session.beginTransaction();
		LiteratureSourceDao literatureSourceDao = new LiteratureSourceDaoImpl();
		LiteratureSource literatureSource = literatureSourceDao.findByName(sourceName, session);
		t.rollback();
		return literatureSource;
	}
	
	public List<LiteratureSource> findAll() {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	public List<LiteratureSource> findAll(Session session) {
		Transaction t = session.beginTransaction();
		LiteratureSourceDao literatureSourceDao = new LiteratureSourceDaoImpl();
		List<LiteratureSource> literatureSources = literatureSourceDao.findAll(session);
		t.rollback();
		return literatureSources;
	}

	@Override
	public LiteratureSource create(LiteratureSource ls) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return create(ls, session);
	}

	@Override
	public LiteratureSource create(LiteratureSource ls, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<LiteratureSource>> violations = validator.validate(ls);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(ls);
			session.flush();
			session.refresh(ls);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		} catch (Exception e) {
			t.rollback();
			throw e;
		}
		
		return ls;
	}

}
