package gov.epa.databases.dev_qsar.qsar_models.service;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.ConfigDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.ConfigDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Config;

public class ConfigServiceImpl implements ConfigService {
	
	public Config findByKey(String configKey) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByKey(configKey, session);
	}
	
	public Config findByKey(String configKey, Session session) {
		Transaction t = session.beginTransaction();
		ConfigDao configDao = new ConfigDaoImpl();
		Config config = configDao.findByKey(configKey, session);
		t.rollback();
		return config;
	}

}
