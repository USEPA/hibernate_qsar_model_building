package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;

public class StatisticDaoImpl implements StatisticDao {
	
	private static final String HQL_BY_NAME = 
			"from Statistic s where s.name = :statisticName";

	@Override
	public Statistic findByName(String statisticName, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("statisticName", statisticName);
		return (Statistic) query.uniqueResult();
	}

}
