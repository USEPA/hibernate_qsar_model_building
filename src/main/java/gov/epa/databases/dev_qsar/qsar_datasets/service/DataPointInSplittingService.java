package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;

public interface DataPointInSplittingService {
	
	public List<DataPointInSplitting> findByDatasetNameAndSplittingName(String datasetName, String splittingName);
	
	public List<DataPointInSplitting> findByDatasetNameAndSplittingName(String datasetName, String splittingName, Session session);
	
	public DataPointInSplitting create(DataPointInSplitting dpis) throws ConstraintViolationException;
	
	public DataPointInSplitting create(DataPointInSplitting dpis, Session session) throws ConstraintViolationException;
	
	public void delete(DataPointInSplitting dpis);
	
	public void delete(DataPointInSplitting dpis, Session session);

	void createSQL(List<DataPointInSplitting> dpisList);

}
