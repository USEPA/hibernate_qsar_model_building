package gov.epa.databases.dsstox.service;

import org.hibernate.Session;

import gov.epa.databases.dsstox.entity.ChemicalList;

public interface ChemicalListService {
	
	public ChemicalList findByName(String chemicalListName);

	public ChemicalList findByName(String chemicalListName, Session session);

}
