package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.Collection;
import java.util.List;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Model;

public interface ModelService {
	
	public Model findById(Long modelId);
	
	public Model findById(Long modelId, Session session);
	
	public List<Model> getAll();
	
	public List<Model> getAll(Session session);
	
	public List<Model> findByIdIn(Collection<Long> modelIds);
	
	public List<Model> findByIdIn(Collection<Long> modelIds, Session session);
	
	public List<Model> findByIdInRangeInclusive(Long minModelId, Long maxModelId);
	
	public List<Model> findByIdInRangeInclusive(Long minModelId, Long maxModelId, Session session);
	
	public List<Model> findByDatasetName(String datasetName);
	
	public List<Model> findByDatasetName(String datasetName, Session session);
	
	public List<Model> findByModelSetId(Long modelSetId);
	
	public List<Model> findByModelSetId(Long modelSetId, Session session);
	
	public Model create(Model model) throws ConstraintViolationException;
	
	public Model create(Model model, Session session) throws ConstraintViolationException;
	
	public Model update(Model model) throws ConstraintViolationException;
	
	public Model update(Model model, Session session) throws ConstraintViolationException;
	
	public void delete(Model model);
	
	public void delete(Model model, Session session);

}
