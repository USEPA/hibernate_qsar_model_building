package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyAcceptableUnit;

public interface PropertyAcceptableUnitDao {
	
	public List<PropertyAcceptableUnit> findAll(Session session);

}
