package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;

public class MethodDaoImpl implements MethodDao {
	
	private static final String HQL_BY_NAME = 
			"from Method m where m.name = :methodName";

	@Override
	public Method findByName(String methodName, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("methodName", methodName);
		return (Method) query.uniqueResult();
	}

}
