package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Method;

public interface MethodService {
	
	public Method findByName(String methodName);
	
	public Method findByName(String methodName, Session session);
	
	public Set<ConstraintViolation<Method>> create(Method method);
	
	public Set<ConstraintViolation<Method>> create(Method method, Session session);

}
