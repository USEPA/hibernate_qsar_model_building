package gov.epa.databases.dsstox.service;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dsstox.DsstoxSession;
import gov.epa.databases.dsstox.dao.GenericSubstanceDao;
import gov.epa.databases.dsstox.dao.GenericSubstanceDaoImpl;
import gov.epa.databases.dsstox.entity.GenericSubstance;

public class GenericSubstanceServiceImpl implements GenericSubstanceService {
	
	@Override
	public GenericSubstance findByDtxsid(String dtxsid) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findByDtxsid(dtxsid, session);
	}
	
	@Override
	public GenericSubstance findByDtxsid(String dtxsid, Session session) {
		Transaction t = session.beginTransaction();
		GenericSubstanceDao GenericSubstanceDao = new GenericSubstanceDaoImpl();
		GenericSubstance GenericSubstance = GenericSubstanceDao.findByDtxsid(dtxsid, session);
		t.rollback();
		return GenericSubstance;
	}
	
	@Override
	public List<GenericSubstance> findByDtxsidIn(Collection<String> dtxsids) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findByDtxsidIn(dtxsids, session);
	}
	
	@Override
	public List<GenericSubstance> findByDtxsidIn(Collection<String> dtxsids, Session session) {
		Transaction t = session.beginTransaction();
		GenericSubstanceDao GenericSubstanceDao = new GenericSubstanceDaoImpl();
		List<GenericSubstance> GenericSubstances = GenericSubstanceDao.findByDtxsidIn(dtxsids, session);
		t.rollback();
		return GenericSubstances;
	}
	
	@Override
	public GenericSubstance findByCasrn(String casrn) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findByCasrn(casrn, session);
	}
	
	@Override
	public GenericSubstance findByCasrn(String casrn, Session session) {
		Transaction t = session.beginTransaction();
		GenericSubstanceDao GenericSubstanceDao = new GenericSubstanceDaoImpl();
		GenericSubstance GenericSubstance = GenericSubstanceDao.findByCasrn(casrn, session);
		t.rollback();
		return GenericSubstance;
	}
	
	@Override
	public List<GenericSubstance> findByCasrnIn(Collection<String> casrns) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findByCasrnIn(casrns, session);
	}
	
	@Override
	public List<GenericSubstance> findByCasrnIn(Collection<String> casrns, Session session) {
		Transaction t = session.beginTransaction();
		GenericSubstanceDao GenericSubstanceDao = new GenericSubstanceDaoImpl();
		List<GenericSubstance> GenericSubstances = GenericSubstanceDao.findByCasrnIn(casrns, session);
		t.rollback();
		return GenericSubstances;
	}

}
