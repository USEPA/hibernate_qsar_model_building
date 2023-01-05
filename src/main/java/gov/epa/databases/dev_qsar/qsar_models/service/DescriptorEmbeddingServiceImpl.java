package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.DescriptorEmbeddingDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.DescriptorEmbeddingDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.web_services.embedding_service.CalculationInfo;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public class DescriptorEmbeddingServiceImpl implements DescriptorEmbeddingService {
	
	private Validator validator;
	
	public DescriptorEmbeddingServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public DescriptorEmbedding findByName(String descriptorEmbeddingName) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByName(descriptorEmbeddingName, session);
	}
	
	public DescriptorEmbedding findByName(String descriptorEmbeddingName, Session session) {
		Transaction t = session.beginTransaction();
		DescriptorEmbeddingDao descriptorEmbeddingDao = new DescriptorEmbeddingDaoImpl();
		DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingDao.findByName(descriptorEmbeddingName, session);
		t.rollback();
		return descriptorEmbedding;
	}
	
	public DescriptorEmbedding findByGASettings(CalculationInfo ci) {
			Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
			return findByGASettings(ci,session);
	}
	
	public DescriptorEmbedding findByGASettings(CalculationInfo ci, Session session) {
		
		Transaction t = session.beginTransaction();
		DescriptorEmbeddingDao descriptorEmbeddingDao = new DescriptorEmbeddingDaoImpl();
		DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingDao.findByGASettings(ci.qsarMethodGA, 
				ci.datasetName, ci.descriptorSetName,ci.toString(), session);
		
		t.rollback();
		return descriptorEmbedding;
	}
	
	
	
	public DescriptorEmbedding findByGASettings(String qsar_method, String dataset_name, String descriptor_set_name,
			String descriptionJson) {
			Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
			return findByGASettings(qsar_method, dataset_name, descriptor_set_name, 
					descriptionJson, session);
	}
	
	public DescriptorEmbedding findByGASettings(String qsar_method, String dataset_name, String descriptor_set_name, 
			String descriptionJson, Session session) {
		Transaction t = session.beginTransaction();
		DescriptorEmbeddingDao descriptorEmbeddingDao = new DescriptorEmbeddingDaoImpl();
		DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingDao.findByGASettings(qsar_method, dataset_name, descriptor_set_name, 
				descriptionJson, session);
		t.rollback();
		return descriptorEmbedding;
	}
	
	@Override
	public DescriptorEmbedding create(DescriptorEmbedding descriptorEmbedding) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(descriptorEmbedding, session);
	}

	@Override
	public DescriptorEmbedding create(DescriptorEmbedding descriptorEmbedding, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<DescriptorEmbedding>> violations = validator.validate(descriptorEmbedding);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(descriptorEmbedding);
			session.flush();
			session.refresh(descriptorEmbedding);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return descriptorEmbedding;
	}

	@Override
	public void delete(DescriptorEmbedding de) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		delete(de,session);
	}

	@Override
	public void delete(DescriptorEmbedding de, Session session) {
		if (de.getId()==null) {
			return;
		}
		
		Transaction t = session.beginTransaction();
		session.delete(de);
		session.flush();
		t.commit();
	}

}
