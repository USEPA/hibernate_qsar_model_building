package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelFile;

public interface ModelFileDao {
	
	public ModelFile findByModelId(Long modelId, Long fileTypeId, Session session);

}
