package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes;

public interface ModelBytesDao {
	
	public ModelBytes findByModelId(Long modelId, Session session);

}
