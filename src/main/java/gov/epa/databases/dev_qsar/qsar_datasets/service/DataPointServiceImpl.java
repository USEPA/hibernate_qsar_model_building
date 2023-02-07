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
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DataPointDao;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DataPointDaoImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.Bob;

public class DataPointServiceImpl implements DataPointService {

	private Validator validator;
	
	public DataPointServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public List<DataPoint> findByDatasetName(String dataPointName) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findByDatasetName(dataPointName, session);
	}
	
	public List<DataPoint> findByDatasetName(String dataPointName, Session session) {
		Transaction t = session.beginTransaction();
		DataPointDao dataPointDao = new DataPointDaoImpl();
		List<DataPoint> dataPoints = dataPointDao.findByDatasetName(dataPointName, session);
		t.rollback();
		return dataPoints;
	}
	
	public List<DataPoint> findByDatasetId(Long datasetId) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findByDatasetId(datasetId, session);
	}
	
	public List<DataPoint> findByDatasetId(Long datasetId, Session session) {
		Transaction t = session.beginTransaction();
		DataPointDao dataPointDao = new DataPointDaoImpl();
		List<DataPoint> dataPoints = dataPointDao.findByDatasetId(datasetId, session);
		t.rollback();
		return dataPoints;
	}
	
	@Override
	public DataPoint create(DataPoint dataPoint) throws ConstraintViolationException {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return create(dataPoint, session);
	}

	@Override
	public DataPoint create(DataPoint dataPoint, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<DataPoint>> violations = validator.validate(dataPoint);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(dataPoint);
			session.flush();
//			session.refresh(dataPoint);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return dataPoint;
	}

	@Override
	public List<DataPoint> createBatch(List<DataPoint> dataPoints) throws ConstraintViolationException {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return createBatch(dataPoints, session);
	}

	@Override
	public List<DataPoint> createBatch(List<DataPoint> dataPoints, Session session)
			throws ConstraintViolationException {
		Transaction tx = session.beginTransaction();
		try {
		for (int i = 0; i < dataPoints.size(); i++) {
			DataPoint dataPoint = dataPoints.get(i);
			session.save(dataPoint);
		    if ( i % 50 == 0 ) { //50, same as the JDBC batch size
		        //flush a batch of inserts and release memory:
		        session.flush();
		        session.clear();
		    }
		}
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			tx.rollback();
		}
		
		tx.commit();
		session.close();
		return dataPoints;
	}

}
