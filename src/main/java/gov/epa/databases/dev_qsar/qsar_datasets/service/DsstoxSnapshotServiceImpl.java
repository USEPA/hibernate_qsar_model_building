package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.sql.Connection;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DsstoxSnapshotDaoImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DsstoxSnapshot;
import gov.epa.run_from_java.scripts.SqlUtilities;

public class DsstoxSnapshotServiceImpl  {

	private Validator validator;
	
	public DsstoxSnapshotServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public DsstoxSnapshot findByName(String snapshotName) {
		Session session =  QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findByName(snapshotName, session);
	}
	
	public DsstoxSnapshot findByName(String snapshotName, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxSnapshotDaoImpl DsstoxSnapshotDao = new DsstoxSnapshotDaoImpl();
		DsstoxSnapshot DsstoxSnapshot = DsstoxSnapshotDao.findByName(snapshotName, session);
		t.rollback();
		return DsstoxSnapshot;
	}
	
	
	public DsstoxSnapshot create(DsstoxSnapshot DsstoxSnapshot) throws ConstraintViolationException {
		Session session =  QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return create(DsstoxSnapshot, session);
	}

	
	public DsstoxSnapshot create(DsstoxSnapshot DsstoxSnapshot, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<DsstoxSnapshot>> violations = validator.validate(DsstoxSnapshot);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(DsstoxSnapshot);
			session.flush();
			session.refresh(DsstoxSnapshot);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return DsstoxSnapshot;
	}
	
		
	public DsstoxSnapshot findById(Long DsstoxSnapshotId) {
		Session session =  QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findById(DsstoxSnapshotId, session);
	}
	
	public DsstoxSnapshot findById(Long DsstoxSnapshotId, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxSnapshotDaoImpl DsstoxSnapshotDao = new DsstoxSnapshotDaoImpl();
		DsstoxSnapshot DsstoxSnapshot = DsstoxSnapshotDao.findById(DsstoxSnapshotId, session);
		t.rollback();
		return DsstoxSnapshot;
	}
	


}
