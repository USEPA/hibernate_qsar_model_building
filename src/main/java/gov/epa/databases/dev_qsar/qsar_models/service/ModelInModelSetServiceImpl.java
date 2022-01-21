package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelInModelSetDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelInModelSetDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInModelSet;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

public class ModelInModelSetServiceImpl implements ModelInModelSetService {
	
	private Validator validator;
	
	public ModelInModelSetServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	@Override
	public Set<ConstraintViolation<ModelInModelSet>> create(ModelInModelSet modelInModelSet) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(modelInModelSet, session);
	}

	@Override
	public Set<ConstraintViolation<ModelInModelSet>> create(ModelInModelSet modelInModelSet, Session session) {
		Set<ConstraintViolation<ModelInModelSet>> violations = validator.validate(modelInModelSet);
		if (!violations.isEmpty()) {
			return violations;
		}
		
		Transaction t = session.beginTransaction();
		session.save(modelInModelSet);
		session.flush();
		session.refresh(modelInModelSet);
		t.commit();
		return null;
	}

	@Override
	public ModelInModelSet findByModelIdAndModelSetId(Long modelId, Long modelSetId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByModelIdAndModelSetId(modelId, modelSetId, session);
	}

	@Override
	public ModelInModelSet findByModelIdAndModelSetId(Long modelId, Long modelSetId, Session session) {
		Transaction t = session.beginTransaction();
		ModelInModelSetDao modelInModelSetDao = new ModelInModelSetDaoImpl();
		ModelInModelSet modelInModelSet = modelInModelSetDao.findByModelIdAndModelSetId(modelId, modelSetId, session);
		t.rollback();
		return modelInModelSet;
	}

}
