package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;

public interface PublicSourceDao {
	
	public PublicSource findByName(String sourceName, Session session);
	
	public List<PublicSource> findAll(Session session);

}
