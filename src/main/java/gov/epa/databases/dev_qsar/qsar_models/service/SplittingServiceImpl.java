package gov.epa.databases.dev_qsar.qsar_models.service;


import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.SplittingDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Splitting;

public class SplittingServiceImpl  {
	
	private Validator validator;
	
	public SplittingServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public Splitting findByName(String splittingName) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByName(splittingName, session);
	}
	
	public Splitting findByName(String splittingName, Session session) {
		Transaction t = session.beginTransaction();
		SplittingDaoImpl splittingDao = new SplittingDaoImpl();
		Splitting splitting = splittingDao.findByName(splittingName, session);
		t.rollback();
		return splitting;
	}

}
