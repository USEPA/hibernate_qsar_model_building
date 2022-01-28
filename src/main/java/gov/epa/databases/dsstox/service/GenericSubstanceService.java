package gov.epa.databases.dsstox.service;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.entity.GenericSubstance;

public interface GenericSubstanceService {
	
	public GenericSubstance findByDtxsid(String dtxsid);

	public GenericSubstance findByDtxsid(String dtxsid, Session session);

	public List<GenericSubstance> findByDtxsidIn(Collection<String> dtxsids);

	public List<GenericSubstance> findByDtxsidIn(Collection<String> dtxsids, Session session);
	
	public GenericSubstance findByCasrn(String casrn);

	public GenericSubstance findByCasrn(String casrn, Session session);

	public List<GenericSubstance> findByCasrnIn(Collection<String> casrns);

	public List<GenericSubstance> findByCasrnIn(Collection<String> casrns, Session session);
	
	public List<DsstoxRecord> findAsDsstoxRecordsByDtxsid(String dtxsid);
	
	public List<DsstoxRecord> findAsDsstoxRecordsByDtxsid(String dtxsid, Session session);
	
	public List<DsstoxRecord> findAsDsstoxRecordsByCasrn(String casrn);
	
	public List<DsstoxRecord> findAsDsstoxRecordsByCasrn(String casrn, Session session);
	
	public List<DsstoxRecord> findAsDsstoxRecordsByPreferredName(String preferredName);
	
	public List<DsstoxRecord> findAsDsstoxRecordsByPreferredName(String preferredName, Session session);
	
	public List<DsstoxRecord> findAsDsstoxRecordsByOtherCasrn(String otherCasrn);
	
	public List<DsstoxRecord> findAsDsstoxRecordsByOtherCasrn(String otherCasrn, Session session);
	
	public List<DsstoxRecord> findAsDsstoxRecordsWithSynonymQualityBySynonym(String synonym);
	
	public List<DsstoxRecord> findAsDsstoxRecordsWithSynonymQualityBySynonym(String synonym, Session session);
	
	public List<DsstoxRecord> findAsDsstoxRecordsByDtxsidIn(Collection<String> dtxsids);
	
	public List<DsstoxRecord> findAsDsstoxRecordsByDtxsidIn(Collection<String> dtxsids, Session session);
	
	public List<DsstoxRecord> findAsDsstoxRecordsByCasrnIn(Collection<String> casrns);
	
	public List<DsstoxRecord> findAsDsstoxRecordsByCasrnIn(Collection<String> casrns, Session session);

}
