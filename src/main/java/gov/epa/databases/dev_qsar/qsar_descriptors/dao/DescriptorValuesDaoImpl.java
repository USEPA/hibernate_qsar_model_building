package gov.epa.databases.dev_qsar.qsar_descriptors.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_descriptors.QsarDescriptorsSession;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;

public class DescriptorValuesDaoImpl implements DescriptorValuesDao {
	
	private static final String HQL_BY_CANON_QSAR_SMILES_AND_DESCRIPTOR_SET_NAME = 
			"select dv from DescriptorValues dv "
			+ "join dv.descriptorSet ds "
			+ "where dv.canonQsarSmiles = :canonQsarSmiles and ds.name = :descriptorSetName";
	private static final String HQL_BY_DESCRIPTOR_SET_NAME = 
			"select dv from DescriptorValues dv "
			+ "join dv.descriptorSet ds "
			+ "where ds.name = :descriptorSetName";
	private static final String HQL_BY_CANON_QSAR_SMILES = 
			"from DescriptorValues dv where dv.canonQsarSmiles = :canonQsarSmiles";
	
	@Override
	public DescriptorValues findByCanonQsarSmilesAndDescriptorSetName(String canonQsarSmiles, String descriptorSetName, Session session) {
		if (session==null) { session = QsarDescriptorsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_CANON_QSAR_SMILES_AND_DESCRIPTOR_SET_NAME);
		query.setParameter("canonQsarSmiles", canonQsarSmiles);
		query.setParameter("descriptorSetName", descriptorSetName);
		return (DescriptorValues) query.uniqueResult();
	}
	
	@Override
	public List<DescriptorValues> findByDescriptorSetName(String descriptorSetName, Session session) {
		if (session==null) { session = QsarDescriptorsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_DESCRIPTOR_SET_NAME);
		query.setParameter("descriptorSetName", descriptorSetName);
		return (List<DescriptorValues>) query.list();
	}
	
	@Override
	public List<DescriptorValues> findByCanonQsarSmiles(String canonQsarSmiles, Session session) {
		if (session==null) { session = QsarDescriptorsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_CANON_QSAR_SMILES);
		query.setParameter("canonQsarSmiles", canonQsarSmiles);
		return (List<DescriptorValues>) query.list();
	}

}
