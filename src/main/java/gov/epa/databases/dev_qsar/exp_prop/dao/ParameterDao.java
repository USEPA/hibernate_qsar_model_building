package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.Parameter;

public interface ParameterDao {
	
	public Parameter findByName(String parameterName, Session session);
	
	public List<Parameter> findAll(Session session);

}
