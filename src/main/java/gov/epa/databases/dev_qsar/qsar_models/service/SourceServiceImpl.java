package gov.epa.databases.dev_qsar.qsar_models.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.SourceDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.SourceDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Source;

public class SourceServiceImpl implements SourceService {
	
	public Source findByName(String sourceName) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByName(sourceName, session);
	}
	
	public Source findByName(String sourceName, Session session) {
		Transaction t = session.beginTransaction();
		SourceDao sourceDao = new SourceDaoImpl();
		Source source = sourceDao.findByName(sourceName, session);
		t.rollback();
		return source;
	}
	
	

}
