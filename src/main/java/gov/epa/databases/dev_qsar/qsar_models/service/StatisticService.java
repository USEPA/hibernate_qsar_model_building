package gov.epa.databases.dev_qsar.qsar_models.service;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;

public interface StatisticService {
	
	public Statistic findByName(String statisticName);
	
	public Statistic findByName(String statisticName, Session session);

}
