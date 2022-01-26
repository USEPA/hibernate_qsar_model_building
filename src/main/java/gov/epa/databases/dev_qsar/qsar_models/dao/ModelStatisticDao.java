package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;

public interface ModelStatisticDao {
	
	public List<ModelStatistic> findByModelId(Long modelId, Session session);

}
