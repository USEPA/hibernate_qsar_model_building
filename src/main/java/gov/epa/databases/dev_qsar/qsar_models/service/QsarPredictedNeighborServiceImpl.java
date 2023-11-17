package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.PredictionDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.PredictionDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.dao.QsarPredictedNeighborDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.QsarPredictedNeighborDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public class QsarPredictedNeighborServiceImpl implements QsarPredictedNeighborService {
	
	private Validator validator;
	
	public QsarPredictedNeighborServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	@Override
	public List<QsarPredictedNeighbor> findById(Long predictionDashboardId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findById(predictionDashboardId, session);
	}
	
	@Override
	public List<QsarPredictedNeighbor> findById(Long predictionDashboardId, Session session) {
		Transaction t = session.beginTransaction();
		QsarPredictedNeighborDao predictionDao = new QsarPredictedNeighborDaoImpl();
		List<QsarPredictedNeighbor> predictions = predictionDao.findById(predictionDashboardId, session);
		t.rollback();
		return predictions;
	}


	@Override
	public QsarPredictedNeighbor create(QsarPredictedNeighbor QsarPredictedNeighbor) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(QsarPredictedNeighbor, session);
	}

	@Override
	public QsarPredictedNeighbor create(QsarPredictedNeighbor QsarPredictedNeighbor, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<QsarPredictedNeighbor>> violations = validator.validate(QsarPredictedNeighbor);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(QsarPredictedNeighbor);
			session.flush();
			session.refresh(QsarPredictedNeighbor);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return QsarPredictedNeighbor;
	}
	
	
	@Override
	public List<QsarPredictedNeighbor> createBatch(List<QsarPredictedNeighbor> QsarPredictedNeighbors) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return createBatch(QsarPredictedNeighbors, session);
	}

	@Override
	public List<QsarPredictedNeighbor> createBatch(List<QsarPredictedNeighbor> qsarPredictedNeighbors, Session session)
			throws ConstraintViolationException {

		Transaction tx = session.beginTransaction();
		try {
			for (int i = 0; i < qsarPredictedNeighbors.size(); i++) {
				QsarPredictedNeighbor qsarPredictedNeighbor = qsarPredictedNeighbors.get(i);
				session.save(qsarPredictedNeighbor);
				if ( i % 1000 == 0 ) { //50, same as the JDBC batch size
					//flush a batch of inserts and release memory:
					session.flush();
					session.clear();
				}
			}

			session.flush();//do the remaining ones
			session.clear();


		} catch (org.hibernate.exception.ConstraintViolationException e) {
			tx.rollback();
		}

		tx.commit();
		session.close();
		return qsarPredictedNeighbors;
	}
	


}
