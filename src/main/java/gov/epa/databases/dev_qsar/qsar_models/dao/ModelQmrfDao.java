package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelQmrf;

public interface ModelQmrfDao {
	
	public ModelQmrf findByModelId(Long modelId, Session session);

}
