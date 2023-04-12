package gov.epa.databases.dev_qsar.qsar_descriptors.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;

public interface CompoundDao {
	
//	public Compound findByDtxcidAndStandardizer(String dtxcid, String standardizer, Session session);
	
	public Compound findByDtxcidSmilesAndStandardizer(String dtxcid, String smiles, String standardizer, Session session);
	
	public List<Compound> findByCanonQsarSmiles(String canonQsarSmiles, Session session);

	public List<Compound> findAllWithStandardizerSmilesNotNull(String standardizer, Session session);

}
