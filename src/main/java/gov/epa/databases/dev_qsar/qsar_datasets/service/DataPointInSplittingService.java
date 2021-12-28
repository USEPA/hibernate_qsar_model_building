package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;

public interface DataPointInSplittingService {
	
	public List<DataPointInSplitting> findByDatasetNameAndSplittingName(String datasetName, String splittingName);
	
	public List<DataPointInSplitting> findByDatasetNameAndSplittingName(String datasetName, String splittingName, Session session);
	
	public Set<ConstraintViolation<DataPointInSplitting>> create(DataPointInSplitting dpis);
	
	public Set<ConstraintViolation<DataPointInSplitting>> create(DataPointInSplitting dpis, Session session);

}
