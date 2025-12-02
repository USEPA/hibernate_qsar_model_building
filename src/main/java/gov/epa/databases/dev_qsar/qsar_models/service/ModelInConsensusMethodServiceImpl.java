package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelInConsensusModelDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelInConsensusModelDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInConsensusModel;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

public class ModelInConsensusMethodServiceImpl implements ModelInConsensusModelService {
	
	private Validator validator;
	
	public ModelInConsensusMethodServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}

	@Override
	public List<ModelInConsensusModel> findByConsensusModelId(Long consensusModelId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByConsensusModelId(consensusModelId, session);
	}

	@Override
	public List<ModelInConsensusModel> findByConsensusModelId(Long consensusModelId, Session session) {
		Transaction t = session.beginTransaction();
		ModelInConsensusModelDao modelInConsensusMethodDao = new ModelInConsensusModelDaoImpl();
		List<ModelInConsensusModel> modelInConsensusMethod = modelInConsensusMethodDao.findByConsensusModelId(consensusModelId, session);
		t.rollback();
		return modelInConsensusMethod;
	}

	@Override
	public ModelInConsensusModel create(ModelInConsensusModel modelInConsensusMethod) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(modelInConsensusMethod, session);
	}

	@Override
	public ModelInConsensusModel create(ModelInConsensusModel modelInConsensusMethod, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<ModelInConsensusModel>> violations = validator.validate(modelInConsensusMethod);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.persist(modelInConsensusMethod);
			session.flush();
			session.refresh(modelInConsensusMethod);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return modelInConsensusMethod;
	}

}
