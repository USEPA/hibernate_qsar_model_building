package gov.epa.databases.dev_qsar.qsar_models.service;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.StatisticDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.StatisticDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;

public class StatisticServiceImpl implements StatisticService {
	
	public Statistic findByName(String statisticName) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByName(statisticName, session);
	}
	
	public Statistic findByName(String statisticName, Session session) {
		Transaction t = session.beginTransaction();
		StatisticDao statisticDao = new StatisticDaoImpl();
		Statistic statistic = statisticDao.findByName(statisticName, session);
		t.rollback();
		return statistic;
	}

}
