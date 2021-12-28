package gov.epa.databases.dev_qsar.qsar_datasets.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;

public class SplittingDaoImpl implements SplittingDao {
	
	private static final String HQL_BY_NAME = 
			"from Splitting s where s.name = :splittingName";
	
	@Override
	public Splitting findByName(String splittingName, Session session) {
		if (session==null) { session = QsarDatasetsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("splittingName", splittingName);
		return (Splitting) query.uniqueResult();
	}

}
