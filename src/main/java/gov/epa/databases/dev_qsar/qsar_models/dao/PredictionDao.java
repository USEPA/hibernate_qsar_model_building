package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;

public interface PredictionDao {
	
	public List<Prediction> findByIds(Long modelId, Long splittingId, Session session);

}
