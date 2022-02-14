package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Model;

public interface ModelDao {
	
	public List<Model> getAll(Session session);
	
	public Model findById(Long modelId, Session session);
	
	public List<Model> findByIdIn(List<Long> modelIds, Session session);
	
	public List<Model> findByIdInRangeInclusive(Long minModelId, Long maxModelId, Session session);
	
	public List<Model> findByDatasetName(String datasetName, Session session);
	
	public List<Model> findByModelSetId(Long modelSetId, Session session);

}
