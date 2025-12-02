package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.QsarPredictedPropertyDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.QsarPredictedPropertyDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedProperty;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

public class QsarPredictedPropertyServiceImpl implements QsarPredictedPropertyService {
	
	private Validator validator;
	
	public QsarPredictedPropertyServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	@Override
	public List<QsarPredictedProperty> findByModelId(Long modelId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByModelId(modelId, session);
	}
	
	@Override
	public List<QsarPredictedProperty> findByModelId(Long modelId, Session session) {
		Transaction t = session.beginTransaction();
		QsarPredictedPropertyDao QsarPredictedPropertyDao = new QsarPredictedPropertyDaoImpl();
		List<QsarPredictedProperty> QsarPredictedPropertys = QsarPredictedPropertyDao.findByModelId(modelId, session);
		t.rollback();
		return QsarPredictedPropertys;
	}

	@Override
	public QsarPredictedProperty create(QsarPredictedProperty QsarPredictedProperty) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(QsarPredictedProperty, session);
	}

	@Override
	public QsarPredictedProperty create(QsarPredictedProperty QsarPredictedProperty, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<QsarPredictedProperty>> violations = validator.validate(QsarPredictedProperty);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.persist(QsarPredictedProperty);
			session.flush();
			session.refresh(QsarPredictedProperty);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return QsarPredictedProperty;
	}

	@Override
	public QsarPredictedProperty find(Long modelId, String dtxcid, String canonQsarSmiles) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return find(modelId, dtxcid, canonQsarSmiles,session);
	}

	
	@Override
	public QsarPredictedProperty find(Long modelId, String dtxcid, String canonQsarSmiles, Session session) {
		Transaction t = session.beginTransaction();
		QsarPredictedPropertyDao QsarPredictedPropertyDao = new QsarPredictedPropertyDaoImpl();
		QsarPredictedProperty QsarPredictedProperty = QsarPredictedPropertyDao.find(modelId, dtxcid,canonQsarSmiles,session);
		t.rollback();
		return QsarPredictedProperty;
	}

}
