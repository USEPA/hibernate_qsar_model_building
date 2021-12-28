package gov.epa.databases.dev_qsar.qsar_datasets.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;

public class DataPointDaoImpl implements DataPointDao {
	
	private static final String HQL_BY_DATASET_NAME = 
			"select distinct dp from DataPoint dp "
			+ "join dp.dataset d "
			+ "left join fetch dp.dataPointContributors dpc "
			+ "where d.name = :datasetName";
	
	public List<DataPoint> findByDatasetName(String datasetName, Session session) {
		if (session==null) { session = QsarDatasetsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_DATASET_NAME);
		query.setParameter("datasetName", datasetName);
		return (List<DataPoint>) query.list();
	}

}
