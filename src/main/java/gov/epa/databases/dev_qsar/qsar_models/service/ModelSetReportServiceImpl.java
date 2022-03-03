package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelSetReportDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelSetReportDaoImpl;
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
	public ModelSetReport findByModelSetIdAndModelData(Long modelSetId, String datasetName, String descriptorSetName,
			String splittingName) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByModelSetIdAndModelData(modelSetId, datasetName, descriptorSetName, splittingName, session);
	}

	@Override
	public ModelSetReport findByModelSetIdAndModelData(Long modelSetId, String datasetName, String descriptorSetName,
			String splittingName, Session session) {
		Transaction t = session.beginTransaction();
		ModelSetReportDao modelSetReportDao = new ModelSetReportDaoImpl();
		ModelSetReport modelSetReport = modelSetReportDao.findByModelSetIdAndModelData(modelSetId, 
				datasetName, descriptorSetName, splittingName,
				session);
		t.rollback();
		return modelSetReport;
	}

	@Override
	public List<ModelSetReport> findByModelSetId(Long modelSetId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByModelSetId(modelSetId, session);
	}

	@Override
	public List<ModelSetReport> findByModelSetId(Long modelSetId, Session session) {
		Transaction t = session.beginTransaction();
		ModelSetReportDao modelSetReportDao = new ModelSetReportDaoImpl();
		List<ModelSetReport> modelSetReports = modelSetReportDao.findByModelSetId(modelSetId, session);
		t.rollback();
		return modelSetReports;
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
