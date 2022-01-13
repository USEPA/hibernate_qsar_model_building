package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Model;

public interface ModelService {
	
	public Model findById(Long modelId);
	
	public Model findById(Long modelId, Session session);
	
	public List<Model> findByDatasetName(String datasetName);
	
	public List<Model> findByDatasetName(String datasetName, Session session);
	
	public Set<ConstraintViolation<Model>> create(Model model);
	
	public Set<ConstraintViolation<Model>> create(Model model, Session session);
	
	public Set<ConstraintViolation<Model>> update(Model model);
	
	public Set<ConstraintViolation<Model>> update(Model model, Session session);

}
