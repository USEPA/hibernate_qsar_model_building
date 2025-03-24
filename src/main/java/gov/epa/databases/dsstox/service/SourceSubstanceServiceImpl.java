package gov.epa.databases.dsstox.service;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.DsstoxSession;
import gov.epa.databases.dsstox.dao.SourceSubstanceDao;
import gov.epa.databases.dsstox.dao.SourceSubstanceDaoImpl;
import gov.epa.databases.dsstox.entity.SourceSubstance;

public class SourceSubstanceServiceImpl implements SourceSubstanceService {
	
	@Override
	public SourceSubstance findByDtxrid(String dtxrid) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findByDtxrid(dtxrid, session);
	}
	
	@Override
	public SourceSubstance findByDtxrid(String dtxrid, Session session) {
		Transaction t = session.beginTransaction();
		SourceSubstanceDao sourceSubstanceDao = new SourceSubstanceDaoImpl();
		SourceSubstance dsstoxRecords = sourceSubstanceDao.findByDtxrid(dtxrid, session);
		t.rollback();
		return dsstoxRecords;
	}
	
	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsWithSourceSubstanceByChemicalListName(String chemicalListName) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findAsDsstoxRecordsWithSourceSubstanceByChemicalListName(chemicalListName, session);
	}
	
	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsWithSourceSubstanceByChemicalListName(String chemicalListName, Session session) {
		Transaction t = session.beginTransaction();
		SourceSubstanceDao sourceSubstanceDao = new SourceSubstanceDaoImpl();
		List<DsstoxRecord> dsstoxRecords = sourceSubstanceDao.findAsDsstoxRecordsWithSourceSubstanceByChemicalListName(chemicalListName, session);
		t.rollback();
		return dsstoxRecords;
	}

	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsWithSourceSubstanceByIdentifier(String identifier) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findAsDsstoxRecordsWithSourceSubstanceByIdentifier(identifier, session);
	}

	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsWithSourceSubstanceByIdentifier(String identifier, Session session) {
		Transaction t = session.beginTransaction();
		SourceSubstanceDao sourceSubstanceDao = new SourceSubstanceDaoImpl();
		List<DsstoxRecord> dsstoxRecords = sourceSubstanceDao.findAsDsstoxRecordsWithSourceSubstanceByIdentifier(identifier, session);
		t.rollback();
		return dsstoxRecords;
	}

}
