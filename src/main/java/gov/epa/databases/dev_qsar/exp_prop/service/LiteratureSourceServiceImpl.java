package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.LiteratureSourceDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.LiteratureSourceDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;

public class LiteratureSourceServiceImpl implements LiteratureSourceService {
	
	public LiteratureSource findByName(String sourceName) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findByName(sourceName, session);
	}
	
	public LiteratureSource findByName(String sourceName, Session session) {
		Transaction t = session.beginTransaction();
		LiteratureSourceDao literatureSourceDao = new LiteratureSourceDaoImpl();
		LiteratureSource literatureSource = literatureSourceDao.findByName(sourceName, session);
		t.rollback();
		return literatureSource;
	}
	
	public List<LiteratureSource> findAll() {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	public List<LiteratureSource> findAll(Session session) {
		Transaction t = session.beginTransaction();
		LiteratureSourceDao literatureSourceDao = new LiteratureSourceDaoImpl();
		List<LiteratureSource> literatureSources = literatureSourceDao.findAll(session);
		t.rollback();
		return literatureSources;
	}

}
