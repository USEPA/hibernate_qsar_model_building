package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Method;

public interface MethodDao {
	
	public Method findByName(String methodName, Session session);

}
