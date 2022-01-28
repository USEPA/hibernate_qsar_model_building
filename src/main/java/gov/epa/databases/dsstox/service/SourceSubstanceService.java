package gov.epa.databases.dsstox.service;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.entity.SourceSubstance;

public interface SourceSubstanceService {
	
	public SourceSubstance findByDtxrid(String dtxrid);
	
	public SourceSubstance findByDtxrid(String dtxrid, Session session);

	public List<DsstoxRecord> findAsDsstoxRecordsWithSourceSubstanceByChemicalListName(String chemicalListName);
	
	public List<DsstoxRecord> findAsDsstoxRecordsWithSourceSubstanceByChemicalListName(String chemicalListName, Session session);
	
	public List<DsstoxRecord> findAsDsstoxRecordsWithSourceSubstanceByIdentifier(String identifier);
	
	public List<DsstoxRecord> findAsDsstoxRecordsWithSourceSubstanceByIdentifier(String identifier, Session session);

}