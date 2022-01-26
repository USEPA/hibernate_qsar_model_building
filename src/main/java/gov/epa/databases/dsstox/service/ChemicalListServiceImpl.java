package gov.epa.databases.dsstox.service;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dsstox.DsstoxSession;
import gov.epa.databases.dsstox.dao.ChemicalListDao;
import gov.epa.databases.dsstox.dao.ChemicalListDaoImpl;
import gov.epa.databases.dsstox.entity.ChemicalList;

public class ChemicalListServiceImpl implements ChemicalListService {
	
	@Override
	public ChemicalList findByName(String chemicalListName) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findByName(chemicalListName, session);
	}
	
	@Override
	public ChemicalList findByName(String chemicalListName, Session session) {
		Transaction t = session.beginTransaction();
		ChemicalListDao chemicalListDao = new ChemicalListDaoImpl();
		ChemicalList chemicalList = chemicalListDao.findByName(chemicalListName, session);
		t.rollback();
		return chemicalList;
	}
	
}
