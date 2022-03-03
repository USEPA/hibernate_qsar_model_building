package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelQmrfDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelQmrfDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelQmrf;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public class ModelQmrfServiceImpl implements ModelQmrfService {
	
	private Validator validator;
	
	public ModelQmrfServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public ModelQmrf findByModelId(Long modelId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByModelId(modelId, session);
	}
	
	public ModelQmrf findByModelId(Long modelId, Session session) {
		Transaction t = session.beginTransaction();
		ModelQmrfDao modelQmrfDao = new ModelQmrfDaoImpl();
		ModelQmrf modelQmrf = modelQmrfDao.findByModelId(modelId, session);
		t.rollback();
		return modelQmrf;
	}

	@Override
	public ModelQmrf create(ModelQmrf modelQmrf) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(modelQmrf, session);
	}

	@Override
	public ModelQmrf create(ModelQmrf modelQmrf, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<ModelQmrf>> violations = validator.validate(modelQmrf);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(modelQmrf);
			session.flush();
			session.refresh(modelQmrf);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return modelQmrf;
	}

	@Override
	public void delete(ModelQmrf modelQmrf) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		delete(modelQmrf, session);
	}

	@Override
	public void delete(ModelQmrf modelQmrf, Session session) {
		if (modelQmrf.getId()==null) {
			return;
		}
		
		Transaction t = session.beginTransaction();
		session.delete(modelQmrf);
		session.flush();
		t.commit();
	}

}
