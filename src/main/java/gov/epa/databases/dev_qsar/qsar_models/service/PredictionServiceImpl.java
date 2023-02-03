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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public class PredictionServiceImpl implements PredictionService {
	
	private Validator validator;
	
	public PredictionServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public List<Prediction> findByIds(Long modelId,Long splittingId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByIds(modelId, splittingId, session);
	}
	
	public List<Prediction> findByIds(Long modelId, Long splittingId, Session session) {
		Transaction t = session.beginTransaction();
		PredictionDao predictionDao = new PredictionDaoImpl();
		List<Prediction> predictions = predictionDao.findByIds(modelId, splittingId, session);
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
	
//	@Override
//	public void create(List<Prediction>predictions) {
//		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
//
//		Transaction t = session.beginTransaction();
//		//TODO need to figure out how to create/find persistence.xml and get the name for the factory
//		EntityManagerFactory emf = Persistence.createEntityManagerFactory("gov.epa.databases.dev_qsar.qsar_models.entity.Prediction");
//		EntityManager entityManager=emf.createEntityManager();
//		
//		int BATCH_SIZE=1000;
//		
//		for (int i=0;i<predictions.size();i++) {
//			Prediction prediction=predictions.get(i);
//			
//			Set<ConstraintViolation<Prediction>> violations = validator.validate(prediction);
//			if (!violations.isEmpty()) {
//				throw new ConstraintViolationException(violations);
//			}
//			
//			session.save(prediction);
//			
//			if (i > 0 && i % BATCH_SIZE == 0) {
//	            entityManager.flush();
//	            entityManager.clear();
//	        }
//		}
//	}
	


}
