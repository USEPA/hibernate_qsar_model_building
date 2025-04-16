package gov.epa.databases.dsstox.dao;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.hibernate.transform.AliasToBeanResultTransformer;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.DsstoxSession;
import gov.epa.databases.dsstox.entity.GenericSubstance;

public class GenericSubstanceDaoImpl implements GenericSubstanceDao {
	private static final String HQL_BY_ID = "from GenericSubstance gs where gs.id = :id";
	private static final String HQL_BY_DTXSID = "from GenericSubstance gs where gs.dsstoxSubstanceId = :dtxsid";
	private static final String HQL_BY_DTXSID_LIST = "from GenericSubstance gs where gs.dsstoxSubstanceId in (:dtxsids)";
	private static final String HQL_BY_CASRN = "from GenericSubstance gs where gs.casrn = :casrn";
	private static final String HQL_BY_CASRN_LIST = "from GenericSubstance gs where gs.casrn in (:casrns)";
	
	private static final String HQL_SELECT_AS_DSSTOX_RECORDS = "select distinct gs.dsstoxSubstanceId as dsstoxSubstanceId, "
			+ "c.dsstoxCompoundId as dsstoxCompoundId, "
			+ "gs.casrn as casrn, "
			+ "gs.preferredName as preferredName, "
			+ "gs.substanceType as substanceType, "
			+ "c.smiles as smiles, "
			+ "c.molWeight as molWeight, "
			+ "c.jchemInchikey as jchemInchikey, "
			+ "c.indigoInchikey as indigoInchikey, "
			+ "c2.smiles as qsarReadySmiles "
			+ "from GenericSubstance gs "
			+ "left join gs.genericSubstanceCompound gsc "
			+ "left join gsc.compound c "
			+ "left join c.successorRelationships cr with cr.compoundRelationshipType.id = 1 "
			+ "left join DsstoxCompound c2 on cr.successorCompound = c2 ";
	
	
	private static final String HQL_SELECT_AS_DSSTOX_RECORDS_WITH_OTHER_CAS = "select distinct gs.dsstoxSubstanceId as dsstoxSubstanceId, "
			+ "c.dsstoxCompoundId as dsstoxCompoundId, "
			+ "gs.casrn as casrn, "
			+ "oc.casrn as casrnOther, "
			+ "gs.preferredName as preferredName, "
			+ "gs.substanceType as substanceType, "
			+ "c.smiles as smiles, "
			+ "c.molWeight as molWeight, "
			+ "c2.smiles as qsarReadySmiles "
			+ "from GenericSubstance gs "
			+ "left join gs.genericSubstanceCompound gsc "
			+ "left join gsc.compound c "
			+ "left join c.successorRelationships cr with cr.compoundRelationshipType.id = 1 "
			+ "left join DsstoxCompound c2 on cr.successorCompound = c2 ";

	private static final String HQL_WHERE_BY_DTXSID_LIST = "where gs.dsstoxSubstanceId in (:dtxsids)";
	
	private static final String HQL_WHERE_BY_InChiKey_LIST = "where gs.dsstoxSubstanceId in (:inChiKeys)";

	
	private static final String HQL_WHERE_BY_CASRN_LIST = "where gs.casrn in (:casrns)";
	
	
	private static final String HQL_WHERE_BY_DTXSID = "where gs.dsstoxSubstanceId = :dtxsid";
	private static final String HQL_WHERE_BY_CASRN = "where gs.casrn = :casrn";
	private static final String HQL_WHERE_BY_PREFERRED_NAME = "where gs.preferredName = :preferredName";
	private static final String HQL_WHERE_BY_OTHER_CASRN = "join OtherCasrn oc on oc.genericSubstance = gs "
			+ "where oc.casrn = :otherCasrn";

	private static final String HQL_WHERE_BY_OTHERCASRN_LIST = "join OtherCasrn oc on oc.genericSubstance = gs "
			+ "where oc.casrn in (:casrns)";

	private static final String HQL_AS_DSSTOX_RECORDS_WITH_SYNONYM_QUALITY_BY_SYNONYM = 
			"select distinct gs.dsstoxSubstanceId as dsstoxSubstanceId, "
			+ "c.dsstoxCompoundId as dsstoxCompoundId, "
			+ "gs.casrn as casrn, "
			+ "gs.preferredName as preferredName, "
			+ "gs.substanceType as substanceType, "
			+ "c.smiles as smiles, "
			+ "c.molWeight as molWeight, "
			+ "c2.smiles as qsarReadySmiles, "
			+ "s.synonymQuality as synonymQuality "
			+ "from GenericSubstance gs "
			+ "left join gs.genericSubstanceCompound gsc "
			+ "left join gsc.compound c "
			+ "left join c.successorRelationships cr with cr.compoundRelationshipType.id = 1 "
			+ "left join DsstoxCompound c2 on cr.successorCompound = c2 "
			+ "join Synonym s on s.genericSubstance = gs "
			+ "where s.identifier = :synonym";


	@Override
	public GenericSubstance findById(Long id, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_ID);
		query.setParameter("id", id);
		return (GenericSubstance) query.uniqueResult();
	}
	
	@Override
	public GenericSubstance findByDtxsid(String dtxsid, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_DTXSID);
		query.setParameter("dtxsid", dtxsid);
		return (GenericSubstance) query.uniqueResult();
	}

	@Override
	public List<GenericSubstance> findByDtxsidIn(Collection<String> dtxsids, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_DTXSID_LIST);
		query.setParameterList("dtxsids", dtxsids);
		return (List<GenericSubstance>) query.list();
	}
	
	@Override
	public GenericSubstance findByCasrn(String casrn, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_CASRN);
		query.setParameter("casrn", casrn);
		return (GenericSubstance) query.uniqueResult();
	}

	@Override
	public List<GenericSubstance> findByCasrnIn(Collection<String> casrns, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_CASRN_LIST);
		query.setParameterList("casrns", casrns);
		return (List<GenericSubstance>) query.list();
	}
	
	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByDtxsid(String dtxsid, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_SELECT_AS_DSSTOX_RECORDS + HQL_WHERE_BY_DTXSID);
		query.setParameter("dtxsid", dtxsid);		
		query.setResultTransformer(new AliasToBeanResultTransformer(DsstoxRecord.class));
		return (List<DsstoxRecord>) query.list();
	}
	
	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByCasrn(String casrn, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_SELECT_AS_DSSTOX_RECORDS + HQL_WHERE_BY_CASRN);
		query.setParameter("casrn", casrn);		
		query.setResultTransformer(new AliasToBeanResultTransformer(DsstoxRecord.class));
		return (List<DsstoxRecord>) query.list();
	}
	
	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByPreferredName(String preferredName, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_SELECT_AS_DSSTOX_RECORDS + HQL_WHERE_BY_PREFERRED_NAME);
		query.setParameter("preferredName", preferredName);		
		query.setResultTransformer(new AliasToBeanResultTransformer(DsstoxRecord.class));
		return (List<DsstoxRecord>) query.list();
	}
	
	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByOtherCasrn(String otherCasrn, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_SELECT_AS_DSSTOX_RECORDS_WITH_OTHER_CAS + HQL_WHERE_BY_OTHER_CASRN);
		query.setParameter("otherCasrn", otherCasrn);		
		query.setResultTransformer(new AliasToBeanResultTransformer(DsstoxRecord.class));
		return (List<DsstoxRecord>) query.list();
	}
	
	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByDtxsidIn(Collection<String> dtxsids, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_SELECT_AS_DSSTOX_RECORDS + HQL_WHERE_BY_DTXSID_LIST);
		query.setParameterList("dtxsids", dtxsids);		
		query.setResultTransformer(new AliasToBeanResultTransformer(DsstoxRecord.class));
		return (List<DsstoxRecord>) query.list();
	}
	
	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByCasrnIn(Collection<String> casrns, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_SELECT_AS_DSSTOX_RECORDS + HQL_WHERE_BY_CASRN_LIST);
		query.setParameterList("casrns", casrns);		
		query.setResultTransformer(new AliasToBeanResultTransformer(DsstoxRecord.class));
		return (List<DsstoxRecord>) query.list();
	}
	
	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByOtherCasrnIn(Collection<String> casrns, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_SELECT_AS_DSSTOX_RECORDS_WITH_OTHER_CAS + HQL_WHERE_BY_OTHERCASRN_LIST);
		query.setParameterList("casrns", casrns);		
		query.setResultTransformer(new AliasToBeanResultTransformer(DsstoxRecord.class));
		return (List<DsstoxRecord>) query.list();
	}


	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsWithSynonymQualityBySynonym(String synonym, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_AS_DSSTOX_RECORDS_WITH_SYNONYM_QUALITY_BY_SYNONYM);
		query.setParameter("synonym", synonym);		
		query.setResultTransformer(new AliasToBeanResultTransformer(DsstoxRecord.class));
		return (List<DsstoxRecord>) query.list();
	}


}
