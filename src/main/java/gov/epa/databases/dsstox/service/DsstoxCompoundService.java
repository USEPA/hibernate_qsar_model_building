package gov.epa.databases.dsstox.service;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.entity.DsstoxCompound;

public interface DsstoxCompoundService {

	public DsstoxCompound findById(Long id);

	public DsstoxCompound findById(Long id, Session session);
	
	public List<DsstoxCompound> findByIdIn(Collection<Long> ids);

	public List<DsstoxCompound> findByIdIn(Collection<Long> ids, Session session);

	public DsstoxCompound findByDtxcid(String dtxcid);

	public DsstoxCompound findByDtxcid(String dtxcid, Session session);

	public List<DsstoxCompound> findByDtxcidIn(Collection<String> dtxcids);

	public List<DsstoxCompound> findByDtxcidIn(Collection<String> dtxcids, Session session);
	
	public DsstoxCompound findByInchikey(String inchikey);

	public DsstoxCompound findByInchikey(String inchikey, Session session);
	
	public List<DsstoxCompound> findByInchikeyIn(Collection<String> inchikeys);

	public List<DsstoxCompound> findByInchikeyIn(Collection<String> inchikeys, Session session);
	
	public List<DsstoxRecord> findDsstoxRecordsByDtxcidIn(Collection<String> dtxcids);
	
	public List<DsstoxRecord> findDsstoxRecordsByDtxcidIn(Collection<String> dtxcids, Session session);

}