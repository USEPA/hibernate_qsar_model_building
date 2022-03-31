package gov.epa.databases.dev_qsar.qsar_models.service;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Config;

public interface ConfigService {
	
	public Config findByKey(String configKey);
	
	public Config findByKey(String configKey, Session session);

}
