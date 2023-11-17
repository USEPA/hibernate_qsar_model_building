package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxSnapshot;

public class DsstoxSnapshotDaoImpl  {
	
	private static final String HQL_BY_ID = "from DsstoxSnapshot d where d.id = :snapshotId";
	private static final String HQL_BY_NAME = 
			"from DsstoxSnapshot d where d.name = :snapshotName";
	
	
	public DsstoxSnapshot findByName(String snapshotName, Session session) {
		if (session==null) { session = QsarDatasetsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("snapshotName", snapshotName);
		return (DsstoxSnapshot) query.uniqueResult();
	}

	
	public DsstoxSnapshot findById(Long snapshotId, Session session) {
		if (session==null) { session = QsarDatasetsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_ID);
		query.setParameter("snapshotId", snapshotId);
		return (DsstoxSnapshot) query.uniqueResult();
	}

}
