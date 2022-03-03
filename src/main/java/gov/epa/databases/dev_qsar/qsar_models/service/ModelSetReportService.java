package gov.epa.databases.dev_qsar.qsar_models.service;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSetReport;

public interface ModelSetReportService {
	
	public ModelSetReport create(ModelSetReport modelSetReport) throws ConstraintViolationException;
	
	public ModelSetReport create(ModelSetReport modelSetReport, Session session) throws ConstraintViolationException;
	
	public void delete(ModelSetReport modelSetReport);
	
	public void delete(ModelSetReport modelSetReport, Session session);

}
