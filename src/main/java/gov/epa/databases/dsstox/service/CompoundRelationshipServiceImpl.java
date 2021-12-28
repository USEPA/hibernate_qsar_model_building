package gov.epa.databases.dsstox.service;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dsstox.DsstoxSession;
import gov.epa.databases.dsstox.dao.CompoundRelationshipDao;
import gov.epa.databases.dsstox.dao.CompoundRelationshipDaoImpl;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.CompoundRelationship;

public class CompoundRelationshipServiceImpl implements CompoundRelationshipService {
	
	@Override
	public CompoundRelationship findQsarReadyByParent(DsstoxCompound parent) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findQsarReadyByParent(parent, session);
	}
	
	@Override
	public CompoundRelationship findQsarReadyByParent(DsstoxCompound parent, Session session) {
		Transaction t = session.beginTransaction();
		CompoundRelationshipDao compoundRelationshipDao = new CompoundRelationshipDaoImpl();
		CompoundRelationship compoundRelationship = compoundRelationshipDao.findQsarReadyByParent(parent, session);
		t.rollback();
		return compoundRelationship;
	}
	
}
