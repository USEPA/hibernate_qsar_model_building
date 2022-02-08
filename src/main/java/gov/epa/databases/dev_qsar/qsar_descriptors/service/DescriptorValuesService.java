package gov.epa.databases.dev_qsar.qsar_descriptors.service;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;

public interface DescriptorValuesService {
	
	public DescriptorValues findByCanonQsarSmilesAndDescriptorSetName(String canonQsarSmiles, String descriptorSetName);
	
	public DescriptorValues findByCanonQsarSmilesAndDescriptorSetName(String canonQsarSmiles, String descriptorSetName, Session session);
	
	public List<DescriptorValues> findByDescriptorSetName(String descriptorSetName);
	
	public List<DescriptorValues> findByDescriptorSetName(String descriptorSetName, Session session);

	public List<DescriptorValues> findByCanonQsarSmiles(String canonQsarSmiles);
	
	public List<DescriptorValues> findByCanonQsarSmiles(String canonQsarSmiles, Session session);
	
	public DescriptorValues create(DescriptorValues descriptorValues) throws ConstraintViolationException;
	
	public DescriptorValues create(DescriptorValues descriptorValues, Session session) throws ConstraintViolationException;

}
