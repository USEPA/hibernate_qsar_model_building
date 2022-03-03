package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSetReport;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public class ModelSetReportServiceImpl implements ModelSetReportService {
	
	private Validator validator;
	
	public ModelSetReportServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	@Override
	public ModelSetReport create(ModelSetReport modelSetReport) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(modelSetReport, session);
	}

	@Override
	public ModelSetReport create(ModelSetReport modelSetReport, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<ModelSetReport>> violations = validator.validate(modelSetReport);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(modelSetReport);
			session.flush();
			session.refresh(modelSetReport);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return modelSetReport;
	}

	@Override
	public void delete(ModelSetReport modelSetReport) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		delete(modelSetReport, session);
	}

	@Override
	public void delete(ModelSetReport modelSetReport, Session session) {
		if (modelSetReport.getId()==null) {
			return;
		}
		
		Transaction t = session.beginTransaction();
		session.delete(modelSetReport);
		session.flush();
		t.commit();
	}

}
