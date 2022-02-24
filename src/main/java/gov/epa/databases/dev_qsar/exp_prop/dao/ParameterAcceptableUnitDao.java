package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterAcceptableUnit;

public interface ParameterAcceptableUnitDao {
	
	public List<ParameterAcceptableUnit> findAll(Session session);

}
