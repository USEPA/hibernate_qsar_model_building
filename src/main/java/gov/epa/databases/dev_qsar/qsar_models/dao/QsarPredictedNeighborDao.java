package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor;

public interface QsarPredictedNeighborDao {

	public List<QsarPredictedNeighbor> findById(Long predictionDashboardId, Session session);

}
