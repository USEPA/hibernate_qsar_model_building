package gov.epa.databases.dev_qsar.qsar_descriptors.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;

public interface DescriptorValuesDao {
	
	public DescriptorValues findByCanonQsarSmilesAndDescriptorSetName(String canonQsarSmiles, String descriptorSetName, Session session);
	
	public List<DescriptorValues> findByDescriptorSetName(String descriptorSetName, Session session);
	
	public List<DescriptorValues> findByCanonQsarSmiles(String canonQsarSmiles, Session session);

}
