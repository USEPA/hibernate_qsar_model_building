package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInConsensusModel;

public class ModelInConsensusModelDaoImpl implements ModelInConsensusModelDao {
	
	private static final String HQL_BY_CONSENSUS_MODEL_ID = "from ModelInConsensusModel micm where micm.consensusModel.id = :consensusModelId";

	@Override
	public List<ModelInConsensusModel> findByConsensusModelId(Long consensusModelId, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_CONSENSUS_MODEL_ID);
		query.setParameter("consensusModelId", consensusModelId);
		return (List<ModelInConsensusModel>) query.list();
	}

}
