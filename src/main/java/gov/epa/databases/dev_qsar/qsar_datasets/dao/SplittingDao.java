package gov.epa.databases.dev_qsar.qsar_datasets.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;

public interface SplittingDao {
	
	public Splitting findByName(String splittingName, Session session);

	public List<Splitting> findByDatasetName(String datasetName, Session session);

	public Splitting findByDatasetNameAndSplittingName(String datasetName, String splittingName, Session session);

}
