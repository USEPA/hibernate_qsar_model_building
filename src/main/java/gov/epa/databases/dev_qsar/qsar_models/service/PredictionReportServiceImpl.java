package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.PredictionReportDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.PredictionReportDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public class PredictionReportServiceImpl implements PredictionReportService {
	
	private Validator validator;
	
	public PredictionReportServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public PredictionReport findByPredictionDashboardId(Long predictionDashboardId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByPredictionDashboardId(predictionDashboardId, session);
	}
	
	public PredictionReport findByPredictionDashboardId(Long predictionDashboardId, Session session) {
		Transaction t = session.beginTransaction();
		PredictionReportDao predictionReportDao = new PredictionReportDaoImpl();
		PredictionReport predictionReport = predictionReportDao.findByPredictionDashboardId(predictionDashboardId, session);
		t.rollback();
		return predictionReport;
	}

	@Override
	public PredictionReport create(PredictionReport predictionReport) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(predictionReport, session);
	}

	@Override
	public PredictionReport create(PredictionReport predictionReport, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<PredictionReport>> violations = validator.validate(predictionReport);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(predictionReport);
			session.flush();
			session.refresh(predictionReport);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return predictionReport;
	}

	@Override
	public void delete(PredictionReport predictionReport) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		delete(predictionReport, session);
	}

	@Override
	public void delete(PredictionReport predictionReport, Session session) {
		if (predictionReport.getId()==null) {
			return;
		}
		
		Transaction t = session.beginTransaction();
		session.delete(predictionReport);
		session.flush();
		t.commit();
	}


}
