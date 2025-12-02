package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.SplittingDao;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.SplittingDaoImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;

public class SplittingServiceImpl implements SplittingService {
	
	private Validator validator;
	
	public SplittingServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public Splitting findByName(String splittingName) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findByName(splittingName, session);
	}
	
	public Splitting findByName(String splittingName, Session session) {
		Transaction t = session.beginTransaction();
		SplittingDao splittingDao = new SplittingDaoImpl();
		Splitting splitting = splittingDao.findByName(splittingName, session);
		t.rollback();
		return splitting;
	}
	
	public List<Splitting> findByDatasetName(String datasetName) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findByDatasetName(datasetName, session);
	}
	
	public List<Splitting> findByDatasetName(String datasetName, Session session) {
		Transaction t = session.beginTransaction();
		SplittingDao splittingDao = new SplittingDaoImpl();
		List<Splitting> splitting = splittingDao.findByDatasetName(datasetName, session);
		t.rollback();
		return splitting;
	}
	
	@Override
	public Splitting findByDatasetNameAndSplittingName(String datasetName, String splittingName) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findByDatasetNameAndSplittingName(datasetName, splittingName, session);
	}

	@Override
	public Splitting findByDatasetNameAndSplittingName(String datasetName, String splittingName, Session session) {
		Transaction t = session.beginTransaction();
		SplittingDao splittingDao = new SplittingDaoImpl();
		Splitting splitting = splittingDao.findByDatasetNameAndSplittingName(datasetName, splittingName, session);
		t.rollback();
		return splitting;
	}

	@Override
	public Splitting create(Splitting splitting) throws ConstraintViolationException {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return create(splitting, session);
	}

	@Override
	public Splitting create(Splitting splitting, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<Splitting>> violations = validator.validate(splitting);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.persist(splitting);
			session.flush();
			session.refresh(splitting);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return splitting;
	}

}
