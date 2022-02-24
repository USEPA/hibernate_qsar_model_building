package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelBytesDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelBytesDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public class ModelBytesServiceImpl implements ModelBytesService {
	
	private Validator validator;
	
	public ModelBytesServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public ModelBytes findByModelId(Long modelId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByModelId(modelId, session);
	}
	
	public ModelBytes findByModelId(Long modelId, Session session) {
		Transaction t = session.beginTransaction();
		ModelBytesDao modelBytesDao = new ModelBytesDaoImpl();
		ModelBytes modelBytes = modelBytesDao.findByModelId(modelId, session);
		t.rollback();
		return modelBytes;
	}

	@Override
	public ModelBytes create(ModelBytes modelBytes) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(modelBytes, session);
	}

	@Override
	public ModelBytes create(ModelBytes modelBytes, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<ModelBytes>> violations = validator.validate(modelBytes);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(modelBytes);
			session.flush();
			session.refresh(modelBytes);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return modelBytes;
	}

	@Override
	public void delete(ModelBytes modelBytes) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		delete(modelBytes, session);
	}

	@Override
	public void delete(ModelBytes modelBytes, Session session) {
		if (modelBytes.getId()==null) {
			return;
		}
		
		Transaction t = session.beginTransaction();
		session.delete(modelBytes);
		session.flush();
		t.commit();
	}

}
