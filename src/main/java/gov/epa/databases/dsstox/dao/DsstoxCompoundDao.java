package gov.epa.databases.dsstox.dao;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.entity.DsstoxCompound;

public interface DsstoxCompoundDao {
	
	public DsstoxCompound findById(Long id, Session session);
	
	public List<DsstoxCompound> findByIdIn(Collection<Long> ids, Session session);
	
	public DsstoxCompound findByDtxcid(String dtxcid, Session session);

	public List<DsstoxCompound> findByDtxcidIn(Collection<String> dtxcids, Session session);
	
	public DsstoxCompound findByInchikey(String inchikey, Session session);
	
	public List<DsstoxCompound> findByInchikeyIn(Collection<String> inchikeys, Session session);
	
	public List<DsstoxRecord> findAsDsstoxRecordsByDtxcid(String dtxcid, Session session);
	
	public List<DsstoxRecord> findAsDsstoxRecordsByInchikey(String inchikey, Session session);

	public List<DsstoxRecord> findAsDsstoxRecordsByDtxcidIn(Collection<String> dtxcids, Session session);

	public List<DsstoxRecord> findAsDsstoxRecordsByInChiKeyIn(Collection<String> inChiKeys, Session session);

	List<DsstoxCompound> findAll(Session session);

}
