package gov.epa.databases.dev_qsar.qsar_models.service;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Method;

public interface MethodService {
	
	public Method findByName(String methodName);
	
	public Method findByName(String methodName, Session session);
	
	public Method create(Method method) throws ConstraintViolationException;
	
	public Method create(Method method, Session session) throws ConstraintViolationException;

}
