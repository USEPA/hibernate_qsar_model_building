package gov.epa.databases.dev_qsar.qsar_datasets.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;

public interface DataPointDao {

	public List<DataPoint> findByDatasetName(String datasetName, Session session);
	
	public List<DataPoint> findByDatasetId(Long datasetId, Session session);

}
