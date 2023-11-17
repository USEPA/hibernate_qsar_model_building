package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.MethodAD;

public interface MethodADService {
	
	public MethodAD findByName(String methodName);
	
	public MethodAD findByName(String methodName, Session session);
	
	public MethodAD create(MethodAD method) throws ConstraintViolationException;
	
	public MethodAD create(MethodAD method, Session session) throws ConstraintViolationException;
	
	
	public List<MethodAD> findAll();
	
	public List<MethodAD> findAll(Session session);


}
