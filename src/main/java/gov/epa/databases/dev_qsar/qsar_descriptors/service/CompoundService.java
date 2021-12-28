package gov.epa.databases.dev_qsar.qsar_descriptors.service;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.databases.dsstox.DsstoxRecord;

public interface CompoundService {
	
	public List<Compound> findByCanonQsarSmiles(String canonQsarSmiles);
	
	public List<Compound> findByCanonQsarSmiles(String canonQsarSmiles, Session session);

}
