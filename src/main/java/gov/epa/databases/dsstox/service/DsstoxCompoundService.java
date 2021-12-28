package gov.epa.databases.dsstox.service;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.entity.DsstoxCompound;

public interface DsstoxCompoundService {

	DsstoxCompound findById(Long id);

	DsstoxCompound findById(Long id, Session session);
	
	List<DsstoxCompound> findByIdIn(Collection<Long> dtxcids);

	List<DsstoxCompound> findByIdIn(Collection<Long> dtxcids, Session session);

	DsstoxCompound findByDtxcid(String dtxcid);

	DsstoxCompound findByDtxcid(String dtxcid, Session session);

	List<DsstoxCompound> findByDtxcidIn(Collection<String> dtxcids);

	List<DsstoxCompound> findByDtxcidIn(Collection<String> dtxcids, Session session);
	
	DsstoxCompound findByInchikey(String inchikey);

	DsstoxCompound findByInchikey(String inchikey, Session session);
	
	List<DsstoxCompound> findByInchikeyIn(Collection<String> dtxcids);

	List<DsstoxCompound> findByInchikeyIn(Collection<String> dtxcids, Session session);
	
	public List<DsstoxRecord> findDsstoxRecordsByDtxcidIn(Collection<String> dtxcids);
	
	public List<DsstoxRecord> findDsstoxRecordsByDtxcidIn(Collection<String> dtxcids, Session session);

}