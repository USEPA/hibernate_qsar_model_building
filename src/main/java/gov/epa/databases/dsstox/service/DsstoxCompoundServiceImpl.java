package gov.epa.databases.dsstox.service;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.DsstoxSession;
import gov.epa.databases.dsstox.dao.DsstoxCompoundDao;
import gov.epa.databases.dsstox.dao.DsstoxCompoundDaoImpl;
import gov.epa.databases.dsstox.entity.DsstoxCompound;

public class DsstoxCompoundServiceImpl implements DsstoxCompoundService {

	@Override
	public DsstoxCompound findById(Long id) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findById(id, session);
	}
	
	@Override
	public DsstoxCompound findById(Long id, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		DsstoxCompound compound = compoundDao.findById(id, session);
		t.rollback();
		return compound;
	}
	
	@Override
	public List<DsstoxCompound> findByIdIn(Collection<Long> ids) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findByIdIn(ids, session);
	}
	
	@Override
	public List<DsstoxCompound> findByIdIn(Collection<Long> ids, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		List<DsstoxCompound> compounds = compoundDao.findByIdIn(ids, session);
		t.rollback();
		return compounds;
	}
	
	@Override
	public DsstoxCompound findByDtxcid(String dtxcid) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findByDtxcid(dtxcid, session);
	}
	
	@Override
	public DsstoxCompound findByDtxcid(String dtxcid, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		DsstoxCompound compound = compoundDao.findByDtxcid(dtxcid, session);
		t.rollback();
		return compound;
	}
	
	@Override
	public List<DsstoxCompound> findByDtxcidIn(Collection<String> dtxcids) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findByDtxcidIn(dtxcids, session);
	}
	
	@Override
	public List<DsstoxCompound> findByDtxcidIn(Collection<String> dtxcids, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		List<DsstoxCompound> compounds = compoundDao.findByDtxcidIn(dtxcids, session);
		t.rollback();
		return compounds;
	}
	
	@Override
	public DsstoxCompound findByInchikey(String inchikey) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findByInchikey(inchikey, session);
	}
	
	@Override
	public DsstoxCompound findByInchikey(String inchikey, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		DsstoxCompound compound = compoundDao.findByInchikey(inchikey, session);
		t.rollback();
		return compound;
	}
	
	@Override
	public List<DsstoxCompound> findByInchikeyIn(Collection<String> inchikeys) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findByInchikeyIn(inchikeys, session);
	}
	
	@Override
	public List<DsstoxCompound> findByInchikeyIn(Collection<String> inchikeys, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		List<DsstoxCompound> compounds = compoundDao.findByInchikeyIn(inchikeys, session);
		t.rollback();
		return compounds;
	}
	
	@Override
	public List<DsstoxRecord> findDsstoxRecordsByDtxcidIn(Collection<String> dtxcids) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findDsstoxRecordsByDtxcidIn(dtxcids, session);
	}
	
	@Override
	public List<DsstoxRecord> findDsstoxRecordsByDtxcidIn(Collection<String> dtxcids, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		List<DsstoxRecord> compounds = compoundDao.findDsstoxRecordsByDtxcidIn(dtxcids, session);
		t.rollback();
		return compounds;
	}
}
