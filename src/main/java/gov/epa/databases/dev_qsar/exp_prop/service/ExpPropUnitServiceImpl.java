package gov.epa.databases.dev_qsar.exp_prop.service;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.ExpPropUnitDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.ExpPropUnitDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;

public class ExpPropUnitServiceImpl implements ExpPropUnitService {
	
	public ExpPropUnit findByName(String unitName) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findByName(unitName, session);
	}
	
	public ExpPropUnit findByName(String unitName, Session session) {
		Transaction t = session.beginTransaction();
		ExpPropUnitDao expPropUnitDao = new ExpPropUnitDaoImpl();
		ExpPropUnit expPropUnit = expPropUnitDao.findByName(unitName, session);
		t.rollback();
		return expPropUnit;
	}

}