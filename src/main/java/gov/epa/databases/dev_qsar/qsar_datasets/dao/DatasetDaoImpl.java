package gov.epa.databases.dev_qsar.qsar_datasets.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;

public class DatasetDaoImpl implements DatasetDao {
	
	private static final String HQL_BY_ID = "from Dataset d where d.id = :datasetId";
	private static final String HQL_BY_NAME = 
			"from Dataset d where d.name = :datasetName";
	
	@Override
	public Dataset findByName(String datasetName, Session session) {
		if (session==null) { session = QsarDatasetsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("datasetName", datasetName);
		return (Dataset) query.uniqueResult();
	}

	@Override
	public Dataset findById(Long datasetId, Session session) {
		if (session==null) { session = QsarDatasetsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_ID);
		query.setParameter("datasetId", datasetId);
		return (Dataset) query.uniqueResult();
	}

}
