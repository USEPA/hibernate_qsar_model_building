package gov.epa.databases.dev_qsar.qsar_descriptors.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;

public interface CompoundDao {
	
	public List<Compound> findByCanonQsarSmiles(String canonQsarSmiles, Session session);

}
