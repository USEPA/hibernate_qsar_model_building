package gov.epa.databases.dsstox.dao;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.transform.AliasToBeanResultTransformer;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.DsstoxSession;
import gov.epa.databases.dsstox.entity.DsstoxCompound;

public class DsstoxCompoundDaoImpl implements DsstoxCompoundDao {
	private static final String HQL_BY_ID = "select c from DsstoxCompound c "
			+ "left join fetch c.genericSubstanceCompound gsc "
			+ "left join fetch gsc.genericSubstance gs "
			+ "left join fetch gs.qcLevel "
			+ "where c.id = :id";
	private static final String HQL_BY_ID_LIST = "select c from DsstoxCompound c "
			+ "left join fetch c.genericSubstanceCompound gsc "
			+ "left join fetch gsc.genericSubstance gs "
			+ "left join fetch gs.qcLevel "
			+ "where c.id in (:ids)";
	private static final String HQL_BY_DTXCID = "from DsstoxCompound c where c.dsstoxCompoundId = :dtxcid";
	private static final String HQL_BY_DTXCID_LIST = "from DsstoxCompound c "
			+ "left join fetch c.genericSubstanceCompound gsc "
			+ "left join fetch gsc.genericSubstance gs "
			+ "left join fetch gs.qcLevel "
			+ "where c.dsstoxCompoundId in (:dtxcids)";
	private static final String HQL_BY_INCHIKEY = "select c from DsstoxCompound c "
			+ "left join fetch c.genericSubstanceCompound gsc "
			+ "left join fetch gsc.genericSubstance gs "
			+ "left join fetch gs.qcLevel "
			+ "where c.jchemInchikey = :inchikey or c.indigoInchikey = :inchikey";
	private static final String HQL_BY_INCHIKEY_LIST = "select c from DsstoxCompound c "
			+ "left join fetch c.genericSubstanceCompound gsc "
			+ "left join fetch gsc.genericSubstance gs "
			+ "left join fetch gs.qcLevel "
			+ "where c.jchemInchikey in (:inchikeys) or c.indigoInchikey in (:inchikeys)";
	private static final String HQL_DSSTOX_RECORDS_BY_DTXCID_LIST = "select distinct gs.dsstoxSubstanceId as dsstoxSubstanceId, "
			+ "c.dsstoxCompoundId as dsstoxCompoundId, "
			+ "gs.casrn as casrn, "
			+ "gs.preferredName as preferredName, "
			+ "gs.substanceType as substanceType, "
			+ "c.smiles as smiles, "
			+ "c.molWeight as molWeight, "
			+ "c2.smiles as qsarReadySmiles "
			+ "from DsstoxCompound c "
			+ "left join c.genericSubstanceCompound gsc "
			+ "left join gsc.genericSubstance gs "
			+ "left join c.successorRelationships cr "
			+ "left join DsstoxCompound c2 on cr.successorCompound = c2 "
			+ "where c.dsstoxCompoundId in (:dtxcids) ";

	@Override
	public DsstoxCompound findById(Long id, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_ID);
		query.setParameter("id", id);
		return (DsstoxCompound) query.uniqueResult();
	}
	
	@Override
	public List<DsstoxCompound> findByIdIn(Collection<Long> ids, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_ID_LIST);
		query.setParameterList("ids", ids);
		return (List<DsstoxCompound>) query.list();
	}
	
	@Override
	public DsstoxCompound findByDtxcid(String dtxcid, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_DTXCID);
		query.setParameter("dtxcid", dtxcid);
		return (DsstoxCompound) query.uniqueResult();
	}

	@Override
	public List<DsstoxCompound> findByDtxcidIn(Collection<String> dtxcids, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_DTXCID_LIST);
		query.setParameterList("dtxcids", dtxcids);
		return (List<DsstoxCompound>) query.list();
	}
	
	@Override
	public DsstoxCompound findByInchikey(String inchikey, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_INCHIKEY);
		query.setParameter("inchikey", inchikey);
		return (DsstoxCompound) query.uniqueResult();
	}
	
	@Override
	public List<DsstoxCompound> findByInchikeyIn(Collection<String> inchikeys, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_INCHIKEY_LIST);
		query.setParameterList("inchikeys", inchikeys);
		return (List<DsstoxCompound>) query.list();
	}
	
	@Override
	public List<DsstoxRecord> findDsstoxRecordsByDtxcidIn(Collection<String> dtxcids, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_DSSTOX_RECORDS_BY_DTXCID_LIST);
		query.setParameterList("dtxcids", dtxcids);		
		query.setResultTransformer(new AliasToBeanResultTransformer(DsstoxRecord.class));
		return (List<DsstoxRecord>) query.list();
	}

}
