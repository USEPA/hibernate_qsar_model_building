package gov.epa.databases.dev_qsar.qsar_datasets.service;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DatasetDao;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DatasetDaoImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;

public class DatasetServiceImpl implements DatasetService {
	
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

}
