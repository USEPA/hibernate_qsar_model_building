package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedADEstimate;

public interface QsarPredictedADEstimateDao {

	public List<QsarPredictedADEstimate> findById(Long predictionDashboardId, Session session);
}
