package gov.epa.databases.dev_qsar.qsar_descriptors.service;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;

public interface DescriptorSetService {
	
	public DescriptorSet findByName(String descriptorSetName);
	
	public DescriptorSet findByName(String descriptorSetName, Session session);
	
	public DescriptorSet create(DescriptorSet descriptorSet) throws ConstraintViolationException;
	
	public DescriptorSet create(DescriptorSet descriptorSet, Session session) throws ConstraintViolationException;
	
	public DescriptorSet update(DescriptorSet descriptorSet) throws ConstraintViolationException;
	
	public DescriptorSet update(DescriptorSet descriptorSet, Session session) throws ConstraintViolationException;

}
