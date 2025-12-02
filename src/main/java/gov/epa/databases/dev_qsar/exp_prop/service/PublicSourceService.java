package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import jakarta.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;

public interface PublicSourceService {
	
	public PublicSource findByName(String sourceName);
	
	public PublicSource findByName(String sourceName, Session session);
	
	public List<PublicSource> findAll();
	
	public List<PublicSource> findAll(Session session);
	
	public PublicSource create(PublicSource ps) throws ConstraintViolationException;
	
	public PublicSource create(PublicSource ps, Session session) throws ConstraintViolationException;
	
}
