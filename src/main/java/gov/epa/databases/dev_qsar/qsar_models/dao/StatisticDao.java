package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;

public interface StatisticDao {
	
	public Statistic findByName(String Name, Session session);

	public List<Statistic> getAll(Session session);

}
