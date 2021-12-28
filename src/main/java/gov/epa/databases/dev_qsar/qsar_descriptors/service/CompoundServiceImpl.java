package gov.epa.databases.dev_qsar.qsar_descriptors.service;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.qsar_descriptors.QsarDescriptorsSession;
import gov.epa.databases.dev_qsar.qsar_descriptors.dao.CompoundDao;
import gov.epa.databases.dev_qsar.qsar_descriptors.dao.CompoundDaoImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;

public class CompoundServiceImpl implements CompoundService {
	
	public List<Compound> findByCanonQsarSmiles(String canonQsarSmiles) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return findByCanonQsarSmiles(canonQsarSmiles, session);
	}
	
	public List<Compound> findByCanonQsarSmiles(String canonQsarSmiles, Session session) {
		Transaction t = session.beginTransaction();
		CompoundDao compoundDao = new CompoundDaoImpl();
		List<Compound> compounds = compoundDao.findByCanonQsarSmiles(canonQsarSmiles, session);
		t.rollback();
		return compounds;
	}

}
