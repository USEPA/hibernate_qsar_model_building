package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DataPointDao;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DataPointDaoImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;

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
	
	@Override
	public Set<ConstraintViolation<DataPoint>> create(DataPoint dataPoint) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return create(dataPoint, session);
	}

	@Override
	public Set<ConstraintViolation<DataPoint>> create(DataPoint dataPoint, Session session) {
		Set<ConstraintViolation<DataPoint>> violations = validator.validate(dataPoint);
		if (!violations.isEmpty()) {
			return violations;
		}
		
		Transaction t = session.beginTransaction();
		session.save(dataPoint);
		session.flush();
		session.refresh(dataPoint);
		t.commit();
		return null;
	}

}
