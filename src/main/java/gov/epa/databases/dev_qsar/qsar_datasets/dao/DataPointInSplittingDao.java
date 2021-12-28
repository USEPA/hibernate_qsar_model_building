package gov.epa.databases.dev_qsar.qsar_datasets.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;

public interface DataPointInSplittingDao {

	public List<DataPointInSplitting> findByDatasetNameAndSplittingName(String datasetName, String splittingName, Session session);

}
