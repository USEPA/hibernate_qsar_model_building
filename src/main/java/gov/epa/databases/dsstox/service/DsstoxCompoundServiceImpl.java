package gov.epa.databases.dsstox.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

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
	public List<DsstoxCompound> findAll() {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}

	
	
	@Override
	public List<DsstoxCompound> findAll(Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		List<DsstoxCompound> compounds = compoundDao.findAll(session);
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
	public List<DsstoxRecord> findAsDsstoxRecordsByDtxcidIn(Collection<String> dtxcids) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findAsDsstoxRecordsByDtxcidIn(dtxcids, session);
	}
	
	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByDtxcidIn(Collection<String> dtxcids, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		List<DsstoxRecord> compounds = compoundDao.findAsDsstoxRecordsByDtxcidIn(dtxcids, session);
		t.rollback();
		return compounds;
	}

	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByInChiKeyIn(Collection<String> inChiKeys) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findAsDsstoxRecordsByInChiKeyIn(inChiKeys, session);
	}

	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByInChiKeyIn(Collection<String> inChiKeys, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		List<DsstoxRecord> compounds = compoundDao.findAsDsstoxRecordsByInChiKeyIn(inChiKeys, session);
		t.rollback();
		return compounds;
	}

	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByDtxcid(String dtxcid) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findAsDsstoxRecordsByDtxcid(dtxcid, session);
	}

	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByDtxcid(String dtxcid, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		List<DsstoxRecord> compounds = compoundDao.findAsDsstoxRecordsByDtxcid(dtxcid, session);
		t.rollback();
		return compounds;
	}

	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByInchikey(String inchikey) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findAsDsstoxRecordsByInchikey(inchikey, session);
	}

	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByInchikey(String inchikey, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		List<DsstoxRecord> compounds = compoundDao.findAsDsstoxRecordsByInchikey(inchikey, session);
		t.rollback();
		return compounds;
	}
	
	public static void main(String[] args) {
		DsstoxCompoundServiceImpl d=new DsstoxCompoundServiceImpl();
		List<String> dtxcids=new ArrayList<>();
		
		dtxcids.add("DTXCID501515091");
		dtxcids.add("DTXCID301515095");
		dtxcids.add("DTXCID201323318");
		dtxcids.add("DTXCID901475302");
		dtxcids.add("DTXCID701508652");
		dtxcids.add("DTXCID701508769");
		dtxcids.add("DTXCID101509002");
		dtxcids.add("DTXCID001508651");
		dtxcids.add("DTXCID201508807");
		dtxcids.add("DTXCID801766110");
		dtxcids.add("DTXCID501506236");
		dtxcids.add("DTXCID401513880");
		dtxcids.add("DTXCID30509096");//not markush
		
		
		List<DsstoxRecord>recs=d.findAsDsstoxRecordsByDtxcidIn(dtxcids);
		
		for (DsstoxRecord rec:recs) {
			System.out.println(rec.dsstoxCompoundId+"\t"+rec.qsarReadySmiles);
			
		}
	}

}
