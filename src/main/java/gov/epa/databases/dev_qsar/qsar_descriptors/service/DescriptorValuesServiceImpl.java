package gov.epa.databases.dev_qsar.qsar_descriptors.service;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.qsar_descriptors.QsarDescriptorsSession;
import gov.epa.databases.dev_qsar.qsar_descriptors.dao.DescriptorValuesDao;
import gov.epa.databases.dev_qsar.qsar_descriptors.dao.DescriptorValuesDaoImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;

public class DescriptorValuesServiceImpl implements DescriptorValuesService {
	
	public DescriptorValues findByCanonQsarSmilesAndDescriptorSetName(String canonQsarSmiles, String descriptorSetName) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return findByCanonQsarSmilesAndDescriptorSetName(canonQsarSmiles, descriptorSetName, session);
	}
	
	public DescriptorValues findByCanonQsarSmilesAndDescriptorSetName(String canonQsarSmiles, String descriptorSetName, Session session) {
		Transaction t = session.beginTransaction();
		DescriptorValuesDao descriptorValuesDao = new DescriptorValuesDaoImpl();
		DescriptorValues descriptorValues = 
				descriptorValuesDao.findByCanonQsarSmilesAndDescriptorSetName(canonQsarSmiles, descriptorSetName, session);
		t.rollback();
		return descriptorValues;
	}

	@Override
	public List<DescriptorValues> findByDescriptorSetName(String descriptorSetName) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return findByDescriptorSetName(descriptorSetName, session);
	}

	@Override
	public List<DescriptorValues> findByDescriptorSetName(String descriptorSetName, Session session) {
		Transaction t = session.beginTransaction();
		DescriptorValuesDao descriptorValuesDao = new DescriptorValuesDaoImpl();
		List<DescriptorValues> descriptorValues = 
				descriptorValuesDao.findByDescriptorSetName(descriptorSetName, session);
		t.rollback();
		return descriptorValues;
	}

}
