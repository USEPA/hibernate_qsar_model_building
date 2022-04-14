package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Config;

public interface ConfigDao {
	
	public Config findByKey(String configKey, Session session);

}
