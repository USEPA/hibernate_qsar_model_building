package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;

public interface LiteratureSourceDao {
	
	public LiteratureSource findByName(String sourceName, Session session);
	
	public List<LiteratureSource> findAll(Session session);

}
