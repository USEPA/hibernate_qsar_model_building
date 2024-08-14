package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelStatisticDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelStatisticDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;
import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public class ModelStatisticServiceImpl implements ModelStatisticService {
	
	private Validator validator;
	
	public ModelStatisticServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}

	
	@Override
	public ModelStatistic findByModelId(Long modelId, Long statisticId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByModelId(modelId, statisticId,session);
	}

	@Override
	public ModelStatistic findByModelId(Long modelId, Long statisticId, Session session) {
		Transaction t = session.beginTransaction();
		ModelStatisticDao modelStatisticDao = new ModelStatisticDaoImpl();
		ModelStatistic modelStatistic = modelStatisticDao.findByModelId(modelId, statisticId, session);
		t.rollback();
		return modelStatistic;
	}
	
	@Override
	public List<ModelStatistic> findByModelId(Long modelId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByModelId(modelId, session);				
	}

	@Override
	public List<ModelStatistic> findByModelId(Long modelId, Session session) {
		Transaction t = session.beginTransaction();
		ModelStatisticDao modelStatisticDao = new ModelStatisticDaoImpl();
		List<ModelStatistic> modelStatistic = modelStatisticDao.findByModelId(modelId, session);
		t.rollback();
		return modelStatistic;
	}

	

	@Override
	public ModelStatistic create(ModelStatistic modelStatistic) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		
		Model model=modelStatistic.getModel();
		
		for (ModelStatistic ms:model.getModelStatistics()) {
			if(ms.getStatistic().getName().equals(modelStatistic.getStatistic().getName())) {
//				System.out.println("Already have statistic "+ms.getStatistic().getName()+ " for "+model.getName());
				return ms;
			}
		}

//		System.out.println("Creating statistic "+modelStatistic.getStatistic().getName()+ " for "+model.getName()+", value="+modelStatistic.getStatisticValue());
		System.out.println("Creating statistic "+modelStatistic.getStatistic().getName()+", value="+modelStatistic.getStatisticValue()+", modelName="+model.getName());
		return create(modelStatistic, session);
	}

	@Override
	public ModelStatistic create(ModelStatistic modelStatistic, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<ModelStatistic>> violations = validator.validate(modelStatistic);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(modelStatistic);
			session.flush();
			session.refresh(modelStatistic);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return modelStatistic;
	}
	
	
	@Override
	public ModelStatistic update(ModelStatistic modelStatistic) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return update(modelStatistic, session);
	}

	@Override
	public ModelStatistic update(ModelStatistic modelStatistic, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<ModelStatistic>> violations = validator.validate(modelStatistic);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t=session.getTransaction();
		if (!t.isActive()) session.beginTransaction();
		
		try {
			session.clear();
			session.update(modelStatistic);
			session.flush();
			session.refresh(modelStatistic);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return modelStatistic;
	}

	
	
	@Override
	public List<ModelStatistic> getAll() {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return getAll(session);
	}

	@Override
	public List<ModelStatistic> getAll(Session session) {
		Transaction t = session.beginTransaction();
		ModelStatisticDao modelDao = new ModelStatisticDaoImpl();
		List<ModelStatistic> modelStatistics = modelDao.getAll(session);
		t.rollback();
		return modelStatistics;
	}

}
