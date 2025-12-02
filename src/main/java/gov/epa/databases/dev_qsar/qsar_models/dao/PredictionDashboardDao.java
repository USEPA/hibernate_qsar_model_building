package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;

public interface PredictionDashboardDao {
	
	public PredictionDashboard findByIds(Long modelId, Long dsstoxRecordId, Session session);

	public List<PredictionDashboard> findBySourceNameAndDTXSID(String sourceName, String dTXSID, Session session);

}
