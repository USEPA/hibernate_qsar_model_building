package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;

public interface PredictionDashboardService {

	public PredictionDashboard create(PredictionDashboard predictionDashboard) throws ConstraintViolationException;
	
	public PredictionDashboard create(PredictionDashboard predictionDashboard, Session session) throws ConstraintViolationException;
	
	public List<PredictionDashboard> createBatch(List<PredictionDashboard> predictionDashboard) throws ConstraintViolationException;
	
	public List<PredictionDashboard> createBatch(List<PredictionDashboard> predictionDashboard, Session session) throws ConstraintViolationException;

	public void createSQL(List<PredictionDashboard> predictionDashboards);

	public PredictionDashboard findByIds(Long fk_model_id, Long fk_dsstox_record_id);

	public PredictionDashboard findByIds(Long fk_model_id, Long fk_dsstox_record_id, Session session);


}

