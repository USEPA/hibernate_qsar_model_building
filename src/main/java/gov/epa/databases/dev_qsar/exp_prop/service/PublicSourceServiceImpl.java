package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.PublicSourceDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.PublicSourceDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;

public class PublicSourceServiceImpl implements PublicSourceService {
	
	public PublicSource findByName(String sourceName) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findByName(sourceName, session);
	}
	
	public PublicSource findByName(String sourceName, Session session) {
		Transaction t = session.beginTransaction();
		PublicSourceDao publicSourceDao = new PublicSourceDaoImpl();
		PublicSource publicSource = publicSourceDao.findByName(sourceName, session);
		t.rollback();
		return publicSource;
	}
	
	public List<PublicSource> findAll() {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	public List<PublicSource> findAll(Session session) {
		Transaction t = session.beginTransaction();
		PublicSourceDao publicSourceDao = new PublicSourceDaoImpl();
		List<PublicSource> publicSources = publicSourceDao.findAll(session);
		t.rollback();
		return publicSources;
	}

}
