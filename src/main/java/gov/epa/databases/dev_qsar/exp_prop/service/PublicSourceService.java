package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;

public interface PublicSourceService {
	
	public PublicSource findByName(String sourceName);
	
	public PublicSource findByName(String sourceName, Session session);
	
	public List<PublicSource> findAll();
	
	public List<PublicSource> findAll(Session session);
	
}
