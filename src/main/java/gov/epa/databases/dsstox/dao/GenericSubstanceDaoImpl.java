package gov.epa.databases.dsstox.dao;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dsstox.DsstoxSession;
import gov.epa.databases.dsstox.entity.GenericSubstance;

public class GenericSubstanceDaoImpl implements GenericSubstanceDao {
	private static final String QUERY_BY_ID = "from GenericSubstance gs where gs.id = :id";
	private static final String QUERY_BY_DTXSID = "from GenericSubstance gs where gs.dsstoxSubstanceId = :dtxsid";
	private static final String QUERY_BY_DTXSID_LIST = "from GenericSubstance gs "
			+ "where gs.dsstoxSubstanceId in (:dtxsids)";
	private static final String QUERY_BY_CASRN = "from GenericSubstance gs where gs.casrn = :casrn";
	private static final String QUERY_BY_CASRN_LIST = "from GenericSubstance gs "
			+ "where gs.casrn in (:casrns)";

	@Override
	public GenericSubstance findById(Long id, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(QUERY_BY_ID);
		query.setParameter("id", id);
		return (GenericSubstance) query.uniqueResult();
	}
	
	@Override
	public GenericSubstance findByDtxsid(String dtxsid, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(QUERY_BY_DTXSID);
		query.setParameter("dtxsid", dtxsid);
		return (GenericSubstance) query.uniqueResult();
	}

	@Override
	public List<GenericSubstance> findByDtxsidIn(Collection<String> dtxsids, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(QUERY_BY_DTXSID_LIST);
		query.setParameterList("dtxsids", dtxsids);
		return (List<GenericSubstance>) query.list();
	}
	
	@Override
	public GenericSubstance findByCasrn(String casrn, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(QUERY_BY_CASRN);
		query.setParameter("casrn", casrn);
		return (GenericSubstance) query.uniqueResult();
	}

	@Override
	public List<GenericSubstance> findByCasrnIn(Collection<String> casrns, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(QUERY_BY_CASRN_LIST);
		query.setParameterList("casrns", casrns);
		return (List<GenericSubstance>) query.list();
	}
	
}
