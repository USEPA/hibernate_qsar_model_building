package gov.epa.databases.dev_qsar.qsar_descriptors.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_descriptors.QsarDescriptorsSession;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;

public class CompoundDaoImpl implements CompoundDao {
	
	private static final String HQL_BY_DTXCID_SMILES_AND_STANDARDIZER = "from Compound c where c.dtxcid = :dtxcid and c.smiles = :smiles and c.standardizer = :standardizer";
	private static final String HQL_BY_CANON_QSAR_SMILES = 
			"from Compound c where c.canonQsarSmiles = :canonQsarSmiles";
	
	private static final String HQL_BY_STANDARDIZER="from Compound c where c.smiles is not null and c.standardizer = :standardizer";
	
//	@Override
//	public Compound findByDtxcidAndStandardizer(String dtxcid, String standardizer, Session session) {
//		if (session==null) { session = QsarDescriptorsSession.getSessionFactory().getCurrentSession(); }
//		Query query = session.createQuery(HQL_BY_DTXCID_AND_STANDARDIZER);
//		query.setParameter("dtxcid", dtxcid);
//		query.setParameter("standardizer", standardizer);
//		return (Compound) query.uniqueResult();
//	}
	
	
	@Override
	public Compound findByDtxcidSmilesAndStandardizer(String dtxcid, String smiles, String standardizer, Session session) {
		if (session==null) { session = QsarDescriptorsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_DTXCID_SMILES_AND_STANDARDIZER);
		query.setParameter("dtxcid", dtxcid);
		query.setParameter("smiles", smiles);
		query.setParameter("standardizer", standardizer);
		return (Compound) query.uniqueResult();
	}

	
	@Override
	public List<Compound> findByCanonQsarSmiles(String canonQsarSmiles, Session session) {
		if (session==null) { session = QsarDescriptorsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_CANON_QSAR_SMILES);
		query.setParameter("canonQsarSmiles", canonQsarSmiles);
		return (List<Compound>) query.list();
	}


	@Override
	public List<Compound> findAllWithStandardizerSmilesNotNull(String standardizer, Session session) {
		if (session==null) { session = QsarDescriptorsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_STANDARDIZER);
		query.setParameter("standardizer", standardizer);
		return (List<Compound>) query.list();
	}

}
