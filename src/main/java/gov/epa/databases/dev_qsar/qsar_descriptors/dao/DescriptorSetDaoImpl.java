package gov.epa.databases.dev_qsar.qsar_descriptors.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_descriptors.QsarDescriptorsSession;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;

public class DescriptorSetDaoImpl implements DescriptorSetDao {
	
	private static final String HQL_BY_NAME = 
			"from DescriptorSet ds where ds.name = :descriptorSetName";
	
	@Override
	public DescriptorSet findByName(String descriptorSetName, Session session) {
		if (session==null) { session = QsarDescriptorsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("descriptorSetName", descriptorSetName);
		return (DescriptorSet) query.uniqueResult();
	}

}
