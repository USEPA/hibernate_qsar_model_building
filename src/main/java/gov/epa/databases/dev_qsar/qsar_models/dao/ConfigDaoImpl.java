package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.Config;

public class ConfigDaoImpl implements ConfigDao {
	
	private static final String HQL_BY_KEY = 
			"from Config c where c.key = :configKey";

	@Override
	public Config findByKey(String configKey, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_KEY);
		query.setParameter("configKey", configKey);
		return (Config) query.uniqueResult();
	}

}
