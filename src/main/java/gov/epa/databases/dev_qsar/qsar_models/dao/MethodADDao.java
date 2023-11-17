package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.MethodAD;

public interface MethodADDao {
	
	public MethodAD findByName(String methodName, Session session);
	
	
	public List<MethodAD> findAll(Session session);

}
