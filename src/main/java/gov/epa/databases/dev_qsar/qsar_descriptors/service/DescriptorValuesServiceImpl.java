package gov.epa.databases.dev_qsar.qsar_descriptors.service;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_descriptors.QsarDescriptorsSession;
import gov.epa.databases.dev_qsar.qsar_descriptors.dao.DescriptorValuesDao;
import gov.epa.databases.dev_qsar.qsar_descriptors.dao.DescriptorValuesDaoImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;

public class DescriptorValuesServiceImpl implements DescriptorValuesService {
	
	private Validator validator;
	
	public DescriptorValuesServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public DescriptorValues findByCanonQsarSmilesAndDescriptorSetName(String canonQsarSmiles, String descriptorValuesName) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return findByCanonQsarSmilesAndDescriptorSetName(canonQsarSmiles, descriptorValuesName, session);
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
	
	@Override
	public List<DescriptorValues> findByCanonQsarSmiles(String canonQsarSmiles) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return findByCanonQsarSmiles(canonQsarSmiles, session);
	}

	@Override
	public List<DescriptorValues> findByCanonQsarSmiles(String canonQsarSmiles, Session session) {
		Transaction t = session.beginTransaction();
		DescriptorValuesDao descriptorValuesDao = new DescriptorValuesDaoImpl();
		List<DescriptorValues> descriptorValues = 
				descriptorValuesDao.findByCanonQsarSmiles(canonQsarSmiles, session);
		t.rollback();
		return descriptorValues;
	}
	

	@Override
	public Set<ConstraintViolation<DescriptorValues>> create(DescriptorValues descriptorValues) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return create(descriptorValues, session);
	}

	@Override
	public Set<ConstraintViolation<DescriptorValues>> create(DescriptorValues descriptorValues, Session session) {
		Set<ConstraintViolation<DescriptorValues>> violations = validator.validate(descriptorValues);
		if (!violations.isEmpty()) {
			return violations;
		}
		
		Transaction t = session.beginTransaction();
		session.save(descriptorValues);
		session.flush();
		session.refresh(descriptorValues);
		t.commit();
		return null;
	}

}
