package gov.epa.databases.dev_qsar.qsar_descriptors.service;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;

public interface CompoundService {
	
	public Compound findByDtxcidSmilesAndStandardizer(String dtxcid, String smiles, String standardizer);	

	public Compound findByDtxcidSmilesAndStandardizer(String dtxcid, String smiles, String standardizer, Session session);

	public List<Compound> findAllWithStandardizerSmilesNotNull(String standardizer);
	
	public List<Compound> findAllWithStandardizerSmilesNotNull(String standardizer, Session session);
	
	public List<Compound> findByCanonQsarSmiles(String canonQsarSmiles);
	
	public List<Compound> findByCanonQsarSmiles(String canonQsarSmiles, Session session);
	
	public Compound create(Compound compound) throws ConstraintViolationException;
	
	public Compound create(Compound compound, Session session) throws ConstraintViolationException;

}
