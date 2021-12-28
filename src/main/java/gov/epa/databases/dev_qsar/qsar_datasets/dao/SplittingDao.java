package gov.epa.databases.dev_qsar.qsar_datasets.dao;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;

public interface SplittingDao {
	
	public Splitting findByName(String splittingName, Session session);

}
