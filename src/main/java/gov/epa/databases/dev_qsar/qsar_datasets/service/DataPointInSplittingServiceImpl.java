package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DataPointInSplittingDao;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DataPointInSplittingDaoImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;

public class DataPointInSplittingServiceImpl implements DataPointInSplittingService {
	
	private Validator validator;
	
	public DataPointInSplittingServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public List<DataPointInSplitting> findByDatasetNameAndSplittingName(String datasetName, String splittingName) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findByDatasetNameAndSplittingName(datasetName, splittingName, session);
	}
	
	public List<DataPointInSplitting> findByDatasetNameAndSplittingName(String datasetName, String splittingName, Session session) {
		Transaction t = session.beginTransaction();
		DataPointInSplittingDao dataPointInSplittingDao = new DataPointInSplittingDaoImpl();
		List<DataPointInSplitting> dataPointsInSplitting = 
				dataPointInSplittingDao.findByDatasetNameAndSplittingName(datasetName, splittingName, session);
		t.rollback();
		return dataPointsInSplitting;
	}
	
	@Override
	public DataPointInSplitting create(DataPointInSplitting dpis) throws ConstraintViolationException {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return create(dpis, session);
	}

	@Override
	public DataPointInSplitting create(DataPointInSplitting dpis, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<DataPointInSplitting>> violations = validator.validate(dpis);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(dpis);
			session.flush();
			session.refresh(dpis);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return dpis;
	}

}
