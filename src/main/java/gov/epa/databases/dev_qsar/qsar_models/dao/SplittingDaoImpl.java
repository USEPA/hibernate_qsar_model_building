package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.Splitting;


public class SplittingDaoImpl {
	
	private static final String HQL_BY_NAME = "from Splitting s where s.name = :splittingName";
	
	public Splitting findByName(String splittingName) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession(); 
		return findByName(splittingName, session);
	}

	public Splitting findByName(String splittingName, Session session) {
		if (!session.getTransaction().isActive()) {
			Transaction t = session.beginTransaction();
		}
					
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("splittingName", splittingName);
		return (Splitting) query.uniqueResult();
	}
}
