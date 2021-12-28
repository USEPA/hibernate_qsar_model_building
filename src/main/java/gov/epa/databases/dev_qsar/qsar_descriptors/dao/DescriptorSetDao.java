package gov.epa.databases.dev_qsar.qsar_descriptors.dao;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;

public interface DescriptorSetDao {
	
	public DescriptorSet findByName(String descriptorSetName, Session session);

}
