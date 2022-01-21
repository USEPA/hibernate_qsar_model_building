package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInModelSet;

public interface ModelInModelSetDao {
	
	public ModelInModelSet findByModelIdAndModelSetId(Long modelId, Long modelSetId, Session session);

}
