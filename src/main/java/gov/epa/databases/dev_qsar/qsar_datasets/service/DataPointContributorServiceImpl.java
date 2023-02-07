package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointContributor;

public class DataPointContributorServiceImpl implements DataPointContributorService {

	private Validator validator;
	
	public DataPointContributorServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}

	@Override
	public DataPointContributor create(DataPointContributor dataPointContributor) throws ConstraintViolationException {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return create(dataPointContributor, session);
	}

	@Override
	public DataPointContributor create(DataPointContributor dataPointContributor, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<DataPointContributor>> violations = validator.validate(dataPointContributor);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(dataPointContributor);
			session.flush();
//			session.refresh(dataPointContributor);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return dataPointContributor;
	}
	
	
	@Override
	public List<DataPointContributor> createBatch(List<DataPointContributor> dataPointContributors) throws ConstraintViolationException {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return createBatch(dataPointContributors, session);
	}

	@Override
	public List<DataPointContributor> createBatch(List<DataPointContributor> dataPointContributors, Session session)
			throws ConstraintViolationException {
		Transaction tx = session.beginTransaction();
		try {
		for (int i = 0; i < dataPointContributors.size(); i++) {
			DataPointContributor dataPointContributor = dataPointContributors.get(i);
			session.save(dataPointContributor);
		    if ( i % 50 == 0 ) { //50, same as the JDBC batch size
		        //flush a batch of inserts and release memory:
		        session.flush();
		        session.clear();
		    }
		}
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			tx.rollback();
		}
		
		tx.commit();
		session.close();
		return dataPointContributors;
	}

}
