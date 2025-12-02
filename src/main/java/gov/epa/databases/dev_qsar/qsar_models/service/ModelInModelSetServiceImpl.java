package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelInModelSetDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelInModelSetDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInModelSet;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

public class ModelInModelSetServiceImpl implements ModelInModelSetService {
	
	private Validator validator;
	
	public ModelInModelSetServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	@Override
	public ModelInModelSet create(ModelInModelSet modelInModelSet) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(modelInModelSet, session);
	}

	@Override
	public ModelInModelSet create(ModelInModelSet modelInModelSet, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<ModelInModelSet>> violations = validator.validate(modelInModelSet);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.persist(modelInModelSet);
			session.flush();
			session.refresh(modelInModelSet);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return modelInModelSet;
	}

	@Override
	public ModelInModelSet findByModelIdAndModelSetId(Long modelId, Long modelSetId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByModelIdAndModelSetId(modelId, modelSetId, session);
	}

	@Override
	public ModelInModelSet findByModelIdAndModelSetId(Long modelId, Long modelSetId, Session session) {
		Transaction t = session.beginTransaction();
		ModelInModelSetDao modelInModelSetDao = new ModelInModelSetDaoImpl();
		ModelInModelSet modelInModelSet = modelInModelSetDao.findByModelIdAndModelSetId(modelId, modelSetId, session);
		t.rollback();
		return modelInModelSet;
	}

	@Override
	public void delete(ModelInModelSet modelInModelSet) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		delete(modelInModelSet, session);
	}

	@Override
	public void delete(ModelInModelSet modelInModelSet, Session session) {
		if (modelInModelSet.getId()==null) {
			return;
		}
		
		Transaction t = session.beginTransaction();
		session.remove(modelInModelSet);
		session.flush();
		t.commit();
	}

}
