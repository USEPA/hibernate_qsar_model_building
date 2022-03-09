package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInConsensusModel;

public interface ModelInConsensusModelService {
	
	public List<ModelInConsensusModel> findByConsensusModelId(Long consensusModelId);
	
	public List<ModelInConsensusModel> findByConsensusModelId(Long consensusModelId, Session session);
	
	public ModelInConsensusModel create(ModelInConsensusModel micm) throws ConstraintViolationException;
	
	public ModelInConsensusModel create(ModelInConsensusModel micm, Session session) throws ConstraintViolationException;

}
