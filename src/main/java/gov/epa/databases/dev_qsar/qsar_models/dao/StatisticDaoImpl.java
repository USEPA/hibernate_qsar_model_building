package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;

public class StatisticDaoImpl implements StatisticDao {
	private static final String HQL_GET_ALL = "select s from Statistic s ";

	private static final String HQL_BY_NAME = 
			"from Statistic s where s.name = :statisticName";

	@Override
	public Statistic findByName(String statisticName, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("statisticName", statisticName);
		return (Statistic) query.uniqueResult();
	}

	@Override
	public List<Statistic> getAll(Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_GET_ALL);
		return (List<Statistic>) query.list();
	}
}
