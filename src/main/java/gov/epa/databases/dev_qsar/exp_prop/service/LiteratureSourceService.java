package gov.epa.databases.dev_qsar.exp_prop.service;

import java.sql.Connection;
import java.util.List;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;

public interface LiteratureSourceService {
	
	public LiteratureSource findByName(String sourceName);
	
	public LiteratureSource findByName(String sourceName, Session session);
	
	public List<LiteratureSource> findAll();
	
	public List<LiteratureSource> findAll(Session session);
	
	public LiteratureSource create(LiteratureSource ls) throws ConstraintViolationException;
	
	public LiteratureSource create(LiteratureSource ls, Session session) throws ConstraintViolationException;

	public void createBatchSQL(List<LiteratureSource> litSources, Connection conn);
	
}
