package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;

public interface DataPointService {
	
	public List<DataPoint> findByDatasetName(String datasetName);
	
	public List<DataPoint> findByDatasetName(String datasetName, Session session);
	
	public Set<ConstraintViolation<DataPoint>> create(DataPoint dataPoint);
	
	public Set<ConstraintViolation<DataPoint>> create(DataPoint dataPoint, Session session);

}
