package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.DescriptorEmbeddingDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.DescriptorEmbeddingDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;

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

}