package gov.epa.databases.dsstox.dao;

import org.hibernate.Session;

import gov.epa.databases.dsstox.entity.ChemicalList;

public interface ChemicalListDao {
	
	public ChemicalList findByName(String chemicalListName, Session session);
	
}
