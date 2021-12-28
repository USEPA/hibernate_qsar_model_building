package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

public class ModelServiceImpl implements ModelService {
	
	private Validator validator;
	
	public ModelServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public Model findById(Long modelId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findById(modelId, session);
	}
	
	public Model findById(Long modelId, Session session) {
		Transaction t = session.beginTransaction();
		ModelDao modelDao = new ModelDaoImpl();
		Model model = modelDao.findById(modelId, session);
		t.rollback();
		return model;
	}
	
	public List<Model> findByDatasetName(String datasetName) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByDatasetName(datasetName, session);
	}
	
	public List<Model> findByDatasetName(String datasetName, Session session) {
		Transaction t = session.beginTransaction();
		ModelDao modelDao = new ModelDaoImpl();
		List<Model> models = modelDao.findByDatasetName(datasetName, session);
		t.rollback();
		return models;
	}

	@Override
	public Set<ConstraintViolation<Model>> create(Model model) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(model, session);
	}

	@Override
	public Set<ConstraintViolation<Model>> create(Model model, Session session) {
		Set<ConstraintViolation<Model>> violations = validator.validate(model);
		if (!violations.isEmpty()) {
			return violations;
		}
		
		Transaction t = session.beginTransaction();
		session.save(model);
		session.flush();
		session.refresh(model);
		t.commit();
		return null;
	}

}
