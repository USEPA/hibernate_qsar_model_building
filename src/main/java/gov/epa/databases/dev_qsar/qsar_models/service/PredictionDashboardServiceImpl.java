package gov.epa.databases.dev_qsar.qsar_models.service;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_descriptors.QsarDescriptorsSession;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionsDashboard;

public class PredictionDashboardServiceImpl implements PredictionDashboardService {
	Validator validator;

	public PredictionDashboardServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}

	
	@Override
	public PredictionsDashboard create(PredictionsDashboard predictionDashboard) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(predictionDashboard, session);
	}


	@Override
	public PredictionsDashboard create(PredictionsDashboard predictionDashboard, Session session)
			throws ConstraintViolationException {
		// TODO Auto-generated method stub
		return null;
	}

}
