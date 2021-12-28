package gov.epa.databases.dev_qsar.qsar_descriptors.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_descriptors.QsarDescriptorsSession;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;

public class CompoundDaoImpl implements CompoundDao {
	
	private static final String HQL_BY_CANON_QSAR_SMILES = 
			"from Compound c where c.canonQsarSmiles = :canonQsarSmiles";
	
	@Override
	public List<Compound> findByCanonQsarSmiles(String canonQsarSmiles, Session session) {
		if (session==null) { session = QsarDescriptorsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_CANON_QSAR_SMILES);
		query.setParameter("canonQsarSmiles", canonQsarSmiles);
		return (List<Compound>) query.list();
	}

}
