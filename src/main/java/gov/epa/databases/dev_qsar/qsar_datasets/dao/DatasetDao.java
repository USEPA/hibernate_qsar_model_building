package gov.epa.databases.dev_qsar.qsar_datasets.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;

public interface DatasetDao {
	
	public Dataset findById(Long datasetId, Session session);
	
	public Dataset findByName(String datasetName, Session session);

	public List<Dataset> findAll(Session session);

}
