package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.PredictionDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.PredictionDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public class PredictionServiceImpl implements PredictionService {
	
	private Validator validator;
	
	public PredictionServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public List<Prediction> findByModelId(Long modelId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByModelId(modelId, session);
	}
	
	public List<Prediction> findByModelId(Long modelId, Session session) {
		Transaction t = session.beginTransaction();
		PredictionDao predictionDao = new PredictionDaoImpl();
		List<Prediction> predictions = predictionDao.findByModelId(modelId, session);
		t.rollback();
		return predictions;
	}

	@Override
	public Prediction create(Prediction prediction) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(prediction, session);
	}

	@Override
	public Prediction create(Prediction prediction, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<Prediction>> violations = validator.validate(prediction);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(prediction);
			session.flush();
			session.refresh(prediction);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return prediction;
	}

}
