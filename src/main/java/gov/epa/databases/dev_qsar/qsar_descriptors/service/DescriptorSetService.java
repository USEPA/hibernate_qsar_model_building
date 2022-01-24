package gov.epa.databases.dev_qsar.qsar_descriptors.service;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;

public interface DescriptorSetService {
	
	public DescriptorSet findByName(String descriptorSetName);
	
	public DescriptorSet findByName(String descriptorSetName, Session session);
	
	public Set<ConstraintViolation<DescriptorSet>> create(DescriptorSet descriptorSet);
	
	public Set<ConstraintViolation<DescriptorSet>> create(DescriptorSet descriptorSet, Session session);

}
