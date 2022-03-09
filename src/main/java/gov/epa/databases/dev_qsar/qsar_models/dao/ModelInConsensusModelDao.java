package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInConsensusModel;

public interface ModelInConsensusModelDao {
	
	public List<ModelInConsensusModel> findByConsensusModelId(Long consensusModelId, Session session);

}
