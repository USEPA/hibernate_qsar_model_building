package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DataPointDao;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DataPointDaoImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;

public class DataPointServiceImpl implements DataPointService {
	
	public List<DataPoint> findByDatasetName(String datasetName) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findByDatasetName(datasetName, session);
	}
	
	public List<DataPoint> findByDatasetName(String datasetName, Session session) {
		Transaction t = session.beginTransaction();
		DataPointDao dataPointDao = new DataPointDaoImpl();
		List<DataPoint> dataPoints = dataPointDao.findByDatasetName(datasetName, session);
		t.rollback();
		return dataPoints;
	}

}
