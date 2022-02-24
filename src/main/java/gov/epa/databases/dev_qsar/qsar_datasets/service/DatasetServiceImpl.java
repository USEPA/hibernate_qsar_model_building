package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DatasetDao;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DatasetDaoImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;

public class DatasetServiceImpl implements DatasetService {

	private Validator validator;
	
	public DatasetServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public Dataset findByName(String datasetName) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findByName(datasetName, session);
	}
	
	public Dataset findByName(String datasetName, Session session) {
		Transaction t = session.beginTransaction();
		DatasetDao datasetDao = new DatasetDaoImpl();
		Dataset dataset = datasetDao.findByName(datasetName, session);
		t.rollback();
		return dataset;
	}
	
	@Override
	public Dataset create(Dataset dataset) throws ConstraintViolationException {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return create(dataset, session);
	}

	@Override
	public Dataset create(Dataset dataset, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<Dataset>> violations = validator.validate(dataset);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(dataset);
			session.flush();
			session.refresh(dataset);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return dataset;
	}

	@Override
	public Dataset findById(Long datasetId) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findById(datasetId, session);
	}

	@Override
	public Dataset findById(Long datasetId, Session session) {
		Transaction t = session.beginTransaction();
		DatasetDao datasetDao = new DatasetDaoImpl();
		Dataset dataset = datasetDao.findById(datasetId, session);
		t.rollback();
		return dataset;
	}

}
