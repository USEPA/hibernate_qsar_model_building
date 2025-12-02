package gov.epa.databases.dev_qsar.qsar_models.service;
import java.util.List;

import jakarta.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Bob;


public interface BobService {
	public Bob create(Bob bob) throws ConstraintViolationException;
	
	public Bob create(Bob bob, Session session) throws ConstraintViolationException;
	
	public List<Bob> batchCreate(List<Bob> bobs) throws ConstraintViolationException;
	
	public List<Bob> batchCreate(List<Bob> bobs, Session session) throws ConstraintViolationException;


}
