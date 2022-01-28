package gov.epa.databases.dsstox.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.transform.AliasToBeanResultTransformer;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.DsstoxSession;
import gov.epa.databases.dsstox.entity.SourceSubstance;

public class SourceSubstanceDaoImpl implements SourceSubstanceDao {
	
	private static final String HQL_BY_DTXRID = "from SourceSubstance ss where ss.dsstoxRecordId = :dtxrid";
	private static final String HQL_SELECT_AS_DSSTOX_RECORDS_WITH_SOURCE_SUBSTANCE = 
			"select distinct ss.dsstoxRecordId as dsstoxRecordId, "
			+ "ss.externalId as externalId, "
			+ "sgsm.connectionReason as connectionReason, "
			+ "sgsm.linkageScore as linkageScore, "
			+ "sgsm.curatorValidated as curatorValidated, "
			+ "gs.dsstoxSubstanceId as dsstoxSubstanceId, "
			+ "c.dsstoxCompoundId as dsstoxCompoundId, "
			+ "gs.casrn as casrn, "
			+ "gs.preferredName as preferredName, "
			+ "gs.substanceType as substanceType, "
			+ "c.smiles as smiles, "
			+ "c.molWeight as molWeight, "
			+ "c2.smiles as qsarReadySmiles "
			+ "from SourceSubstance ss "
			+ "left join ss.sourceGenericSubstanceMapping sgsm "
			+ "left join sgsm.genericSubstance gs "
			+ "left join gs.genericSubstanceCompound gsc "
			+ "left join gsc.compound c "
			+ "left join c.successorRelationships cr with cr.compoundRelationshipType.id = 1 "
			+ "left join DsstoxCompound c2 on cr.successorCompound = c2 ";
	private static final String HQL_WHERE_BY_CHEMICAL_LIST_NAME = "join ss.chemicalList cl "
			+ "where cl.name = :chemicalListName";
	private static final String HQL_WHERE_BY_IDENTIFIER = "join SourceSubstanceIdentifier ssi on ssi.sourceSubstance = ss "
			+ "where ssi.identifier = :identifier";

	@Override
	public SourceSubstance findByDtxrid(String dtxrid, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_DTXRID);
		query.setParameter("dtxrid", dtxrid);
		return (SourceSubstance) query.uniqueResult();
	}
	
	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsWithSourceSubstanceByChemicalListName(String chemicalListName, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_SELECT_AS_DSSTOX_RECORDS_WITH_SOURCE_SUBSTANCE + HQL_WHERE_BY_CHEMICAL_LIST_NAME);
		query.setParameter("chemicalListName", chemicalListName);
		query.setResultTransformer(new AliasToBeanResultTransformer(DsstoxRecord.class));
		return (List<DsstoxRecord>) query.list();
	}
	
	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsWithSourceSubstanceByIdentifier(String identifier, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_SELECT_AS_DSSTOX_RECORDS_WITH_SOURCE_SUBSTANCE + HQL_WHERE_BY_IDENTIFIER);
		query.setParameter("identifier", identifier);
		query.setResultTransformer(new AliasToBeanResultTransformer(DsstoxRecord.class));
		return (List<DsstoxRecord>) query.list();
	}

}
