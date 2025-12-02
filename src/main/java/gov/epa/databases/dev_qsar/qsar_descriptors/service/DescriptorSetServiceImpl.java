package gov.epa.databases.dev_qsar.qsar_descriptors.service;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_descriptors.QsarDescriptorsSession;
import gov.epa.databases.dev_qsar.qsar_descriptors.dao.DescriptorSetDao;
import gov.epa.databases.dev_qsar.qsar_descriptors.dao.DescriptorSetDaoImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;

public class DescriptorSetServiceImpl implements DescriptorSetService {

	private Validator validator;
	
	public DescriptorSetServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
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
	
	@Override
	public DescriptorSet create(DescriptorSet descriptorSet) throws ConstraintViolationException {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return create(descriptorSet, session);
	}

	@Override
	public DescriptorSet create(DescriptorSet descriptorSet, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<DescriptorSet>> violations = validator.validate(descriptorSet);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.persist(descriptorSet);
			session.flush();
			session.refresh(descriptorSet);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return descriptorSet;
	}
	
	@Override
	public DescriptorSet update(DescriptorSet descriptorSet) throws ConstraintViolationException {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return update(descriptorSet, session);
	}

	@Override
	public DescriptorSet update(DescriptorSet descriptorSet, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<DescriptorSet>> violations = validator.validate(descriptorSet);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.clear();
			session.merge(descriptorSet);
			session.flush();
			session.refresh(descriptorSet);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return descriptorSet;
	}

}
