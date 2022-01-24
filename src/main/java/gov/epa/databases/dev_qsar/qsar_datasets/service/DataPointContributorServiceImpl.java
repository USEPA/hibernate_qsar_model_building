package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointContributor;

public class DataPointContributorServiceImpl implements DataPointContributorService {

	private Validator validator;
	
	public DataPointContributorServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}

	@Override
	public Set<ConstraintViolation<DataPointContributor>> create(DataPointContributor dataPointContributor) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return create(dataPointContributor, session);
	}

	@Override
	public Set<ConstraintViolation<DataPointContributor>> create(DataPointContributor dataPointContributor, Session session) {
		Set<ConstraintViolation<DataPointContributor>> violations = validator.validate(dataPointContributor);
		if (!violations.isEmpty()) {
			return violations;
		}
		
		Transaction t = session.beginTransaction();
		session.save(dataPointContributor);
		session.flush();
		session.refresh(dataPointContributor);
		t.commit();
		return null;
	}

}
