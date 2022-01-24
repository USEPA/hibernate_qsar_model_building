package gov.epa.databases.dev_qsar.qsar_descriptors.service;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

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
	public Set<ConstraintViolation<DescriptorSet>> create(DescriptorSet descriptorSet) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return create(descriptorSet, session);
	}

	@Override
	public Set<ConstraintViolation<DescriptorSet>> create(DescriptorSet descriptorSet, Session session) {
		Set<ConstraintViolation<DescriptorSet>> violations = validator.validate(descriptorSet);
		if (!violations.isEmpty()) {
			return violations;
		}
		
		Transaction t = session.beginTransaction();
		session.save(descriptorSet);
		session.flush();
		session.refresh(descriptorSet);
		t.commit();
		return null;
	}

}
