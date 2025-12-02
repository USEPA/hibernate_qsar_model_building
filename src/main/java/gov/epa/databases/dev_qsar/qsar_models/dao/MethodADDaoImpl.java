package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;



import org.hibernate.Session;
import org.hibernate.query.Query;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.MethodAD;

public class MethodADDaoImpl implements MethodADDao {
	
	private static final String HQL_BY_NAME = 
			"from MethodAD m where m.name = :name";
	private static final String HQL_ALL = "from MethodAD";

	@Override
	public MethodAD findByName(String name, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("name", name);
		return (MethodAD) query.uniqueResult();
	}

	
	@Override
	public List<MethodAD> findAll(Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_ALL);
		return (List<MethodAD>) query.list();
	}


}
