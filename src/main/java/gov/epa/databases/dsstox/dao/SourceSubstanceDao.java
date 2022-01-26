package gov.epa.databases.dsstox.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.entity.SourceSubstance;

public interface SourceSubstanceDao {
	
	public SourceSubstance findByDtxrid(String dtxrid, Session session);
	
	public List<DsstoxRecord> findDsstoxRecordsByChemicalListName(String chemicalListName, Session session);

}
