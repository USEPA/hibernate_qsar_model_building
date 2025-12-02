package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.QsarPredictedADEstimateDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.QsarPredictedADEstimateDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedADEstimate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

public class QsarPredictedADEstimateServiceImpl implements QsarPredictedADEstimateService {
	
	private Validator validator;
	
	public QsarPredictedADEstimateServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	@Override
	public List<QsarPredictedADEstimate> findById(Long predictionDashboardId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findById(predictionDashboardId, session);
	}
	
	@Override
	public List<QsarPredictedADEstimate> findById(Long predictionDashboardId, Session session) {
		Transaction t = session.beginTransaction();
		QsarPredictedADEstimateDao predictionDao = new QsarPredictedADEstimateDaoImpl();
		List<QsarPredictedADEstimate> predictions = predictionDao.findById(predictionDashboardId, session);
		t.rollback();
		return predictions;
	}
	@Override
	public QsarPredictedADEstimate create(QsarPredictedADEstimate QsarPredictedADEstimate) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(QsarPredictedADEstimate, session);
	}

	@Override
	public QsarPredictedADEstimate create(QsarPredictedADEstimate QsarPredictedADEstimate, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<QsarPredictedADEstimate>> violations = validator.validate(QsarPredictedADEstimate);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.persist(QsarPredictedADEstimate);
			session.flush();
			session.refresh(QsarPredictedADEstimate);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return QsarPredictedADEstimate;
	}
	
	
	@Override
	public List<QsarPredictedADEstimate> createBatch(List<QsarPredictedADEstimate> qsarPredictedADEstimates) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return createBatch(qsarPredictedADEstimates, session);
	}

	@Override
	public List<QsarPredictedADEstimate> createBatch(List<QsarPredictedADEstimate> qsarPredictedADEstimates, Session session)
			throws ConstraintViolationException {

		Transaction tx = session.beginTransaction();
		try {
			for (int i = 0; i < qsarPredictedADEstimates.size(); i++) {
				QsarPredictedADEstimate qsarPredictedADEstimate = qsarPredictedADEstimates.get(i);
				session.persist(qsarPredictedADEstimate);
				if ( i % 1000 == 0 ) { //50, same as the JDBC batch size
					//flush a batch of inserts and release memory:
					session.flush();
					session.clear();
				}
			}

			session.flush();//do the remaining ones
			session.clear();


		} catch (org.hibernate.exception.ConstraintViolationException e) {
			e.printStackTrace();
			tx.rollback();
		}

		tx.commit();
		session.close();
		return qsarPredictedADEstimates;
	}
	


}
