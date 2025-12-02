package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.List;
import org.hibernate.Session;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import jakarta.validation.ConstraintViolationException;

public interface SplittingService {
	
	public Splitting findByName(String splittingName);
	
	public Splitting findByName(String splittingName, Session session);
	
	public List<Splitting> findByDatasetName(String datasetName);
	
	public List<Splitting> findByDatasetName(String datasetName, Session session);
	
	public Splitting findByDatasetNameAndSplittingName(String datasetName, String splittingName);
	
	public Splitting findByDatasetNameAndSplittingName(String datasetName, String splittingName, Session session);
	
	public Splitting create(Splitting splitting) throws ConstraintViolationException;
	
	public Splitting create(Splitting splitting, Session session) throws ConstraintViolationException;

}
