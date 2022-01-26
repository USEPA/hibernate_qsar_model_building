package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSet;

public interface ModelSetDao {
	
	public ModelSet findById(Long modelSetId, Session session);
	
	public ModelSet findByName(String modelSetName, Session session);

}
