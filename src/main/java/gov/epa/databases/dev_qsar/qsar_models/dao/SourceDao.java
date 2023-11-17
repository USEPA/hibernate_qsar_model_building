package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Source;

public interface SourceDao {
	
	public Source findByName(String statisticName, Session session);

}
