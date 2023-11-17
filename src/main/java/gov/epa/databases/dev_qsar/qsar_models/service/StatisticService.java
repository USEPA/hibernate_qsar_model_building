package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;

public interface StatisticService {
	
	public Statistic findByName(String statisticName);
	
	public Statistic findByName(String statisticName, Session session);

	void createBatchSQL(List<Statistic> statistics);

	public List<Statistic> getAll();

	public List<Statistic> getAll(Session session);

}
