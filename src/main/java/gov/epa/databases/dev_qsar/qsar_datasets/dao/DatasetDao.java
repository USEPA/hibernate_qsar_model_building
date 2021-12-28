package gov.epa.databases.dev_qsar.qsar_datasets.dao;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;

public interface DatasetDao {
	
	public Dataset findByName(String datasetName, Session session);

}
