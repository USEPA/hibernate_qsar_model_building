package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSetReport;

public interface ModelSetReportService {
	
	public ModelSetReport findByModelSetIdAndModelData(Long modelSetId, String datasetName, String descriptorSetName, String splittingName);
	
	public ModelSetReport findByModelSetIdAndModelData(Long modelSetId, String datasetName, String descriptorSetName, String splittingName,
			Session session);

	public List<ModelSetReport> findByModelSetId(Long modelSetId);
	
	public List<ModelSetReport> findByModelSetId(Long modelSetId, Session session);
	
	public ModelSetReport create(ModelSetReport modelSetReport) throws ConstraintViolationException;
	
	public ModelSetReport create(ModelSetReport modelSetReport, Session session) throws ConstraintViolationException;
	
	public void delete(ModelSetReport modelSetReport);
	
	public void delete(ModelSetReport modelSetReport, Session session);

}
