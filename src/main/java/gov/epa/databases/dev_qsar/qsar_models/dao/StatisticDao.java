package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;

public interface StatisticDao {
	
	public Statistic findByName(String statisticName, Session session);

}
