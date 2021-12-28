package gov.epa.databases.dev_qsar.qsar_descriptors.service;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.qsar_descriptors.QsarDescriptorsSession;
import gov.epa.databases.dev_qsar.qsar_descriptors.dao.DescriptorSetDao;
import gov.epa.databases.dev_qsar.qsar_descriptors.dao.DescriptorSetDaoImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;

public class DescriptorSetServiceImpl implements DescriptorSetService {
	
	public DescriptorSet findByName(String descriptorSetName) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return findByName(descriptorSetName, session);
	}
	
	public DescriptorSet findByName(String descriptorSetName, Session session) {
		Transaction t = session.beginTransaction();
		DescriptorSetDao descriptorSetDao = new DescriptorSetDaoImpl();
		DescriptorSet descriptorSet = 
				descriptorSetDao.findByName(descriptorSetName, session);
		t.rollback();
		return descriptorSet;
	}

}
