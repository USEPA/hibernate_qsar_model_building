package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelSetDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelSetDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSet;

public class ModelSetServiceImpl implements ModelSetService {
	
	private Validator validator;
	
	public ModelSetServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public ModelSet findById(Long modelSetId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findById(modelSetId, session);
	}
	
	public ModelSet findById(Long modelSetId, Session session) {
		Transaction t = session.beginTransaction();
		ModelSetDao modelSetDao = new ModelSetDaoImpl();
		ModelSet modelSet = modelSetDao.findById(modelSetId, session);
		t.rollback();
		return modelSet;
	}
	
	public ModelSet findByName(String modelSetName) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByName(modelSetName, session);
	}
	
	public ModelSet findByName(String modelSetName, Session session) {
		Transaction t = session.beginTransaction();
		ModelSetDao modelSetDao = new ModelSetDaoImpl();
		ModelSet modelSet = modelSetDao.findByName(modelSetName, session);
		t.rollback();
		return modelSet;
	}
	
	@Override
	public ModelSet create(ModelSet modelSet) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(modelSet, session);
	}

	@Override
	public ModelSet create(ModelSet modelSet, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<ModelSet>> violations = validator.validate(modelSet);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.persist(modelSet);
			session.flush();
			session.refresh(modelSet);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return modelSet;
	}

}
