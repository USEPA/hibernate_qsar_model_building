package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.ExpPropPropertyDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.ExpPropPropertyDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;

public class ExpPropPropertyServiceImpl implements ExpPropPropertyService {
	
	public List<ExpPropProperty> findByPropertyCategoryName(String propertyCategoryName) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findByPropertyCategoryName(propertyCategoryName, session);
	}
	
	public List<ExpPropProperty> findByPropertyCategoryName(String propertyCategoryName, Session session) {
		Transaction t = session.beginTransaction();
		ExpPropPropertyDao expPropPropertyDao = new ExpPropPropertyDaoImpl();
		List<ExpPropProperty> expPropProperty = expPropPropertyDao.findByPropertyCategoryName(propertyCategoryName, session);
		t.rollback();
		return expPropProperty;
	}
	
	public List<ExpPropProperty> findAll() {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	public List<ExpPropProperty> findAll(Session session) {
		Transaction t = session.beginTransaction();
		ExpPropPropertyDao expPropPropertyDao = new ExpPropPropertyDaoImpl();
		List<ExpPropProperty> expPropPropertys = expPropPropertyDao.findAll(session);
		t.rollback();
		return expPropPropertys;
	}

}
