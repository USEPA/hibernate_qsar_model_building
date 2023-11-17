package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.Source;

public class SourceDaoImpl implements SourceDao {
	
	private static final String HQL_BY_NAME = 
			"from Source s where s.name = :sourceName";

	@Override
	public Source findByName(String sourceName, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("sourceName", sourceName);
		return (Source) query.uniqueResult();
	}

}
