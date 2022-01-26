package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSet;

public interface ModelSetService {
	
	public ModelSet findById(Long modelSetId);
	
	public ModelSet findById(Long modelSetId, Session session);
	
	public ModelSet findByName(String modelSetName);
	
	public ModelSet findByName(String modelSetName, Session session);
	
	public Set<ConstraintViolation<ModelSet>> create(ModelSet modelSet);
	
	public Set<ConstraintViolation<ModelSet>> create(ModelSet modelSet, Session session);

}
