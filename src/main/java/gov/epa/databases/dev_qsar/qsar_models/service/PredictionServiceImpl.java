package gov.epa.databases.dev_qsar.qsar_models.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.PredictionDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.PredictionDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public class PredictionServiceImpl implements PredictionService {
	
	private Validator validator;
	
	public PredictionServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public List<Prediction> findByIds(Long modelId,Long splittingId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByIds(modelId, splittingId, session);
	}
	
	public List<Prediction> findByIds(Long modelId, Long splittingId, Session session) {
		Transaction t = session.beginTransaction();
		PredictionDao predictionDao = new PredictionDaoImpl();
		List<Prediction> predictions = predictionDao.findByIds(modelId, splittingId, session);
		t.rollback();
		return predictions;
	}

	@Override
	public Prediction create(Prediction prediction) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(prediction, session);
	}

	@Override
	public Prediction create(Prediction prediction, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<Prediction>> violations = validator.validate(prediction);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(prediction);
			session.flush();
			session.refresh(prediction);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return prediction;
	}
	
	@Override
	public void createSQL (List<Prediction> predictions) {

		Connection conn=SqlUtilities.getConnectionPostgres();
		
		String [] fieldNames= {"canon_qsar_smiles","qsar_predicted_value","fk_model_id","fk_splitting_id","created_by","created_at"};
		int batchSize=1000;
		
		String sql="INSERT INTO qsar_models.predictions (";
		
		for (int i=0;i<fieldNames.length;i++) {
			sql+=fieldNames[i];
			if (i<fieldNames.length-1)sql+=",";
			else sql+=") VALUES (";
		}
		
		for (int i=0;i<fieldNames.length-1;i++) {
			sql+="?";
			if (i<fieldNames.length-1)sql+=",";			 		
		}
		sql+="current_timestamp)";	
//		System.out.println(sql);
		
		try {
			conn.setAutoCommit(false);
			PreparedStatement prep = conn.prepareStatement(sql);
			prep.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			long t1=System.currentTimeMillis();

			for (int counter = 0; counter < predictions.size(); counter++) {
				Prediction p=predictions.get(counter);
				prep.setString(1, p.getCanonQsarSmiles());
				prep.setDouble(2, p.getQsarPredictedValue());
				prep.setLong(3, p.getModel().getId());
				prep.setLong(4, p.getFk_splitting_id());//TODO figure out how to make splitting in predictions table an object even though its in different schema
				prep.setString(5, p.getCreatedBy());
				prep.addBatch();
				
				if (counter % batchSize == 0 && counter!=0) {
					// System.out.println(counter);
					prep.executeBatch();
				}
			}

			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+predictions.size()+" predictions using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
//			conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}			

	
//	@Override
//	public void create(List<Prediction>predictions) {
//		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
//
//		Transaction t = session.beginTransaction();
//		//TODO need to figure out how to create/find persistence.xml and get the name for the factory
//		EntityManagerFactory emf = Persistence.createEntityManagerFactory("gov.epa.databases.dev_qsar.qsar_models.entity.Prediction");
//		EntityManager entityManager=emf.createEntityManager();
//		
//		int BATCH_SIZE=1000;
//		
//		for (int i=0;i<predictions.size();i++) {
//			Prediction prediction=predictions.get(i);
//			
//			Set<ConstraintViolation<Prediction>> violations = validator.validate(prediction);
//			if (!violations.isEmpty()) {
//				throw new ConstraintViolationException(violations);
//			}
//			
//			session.save(prediction);
//			
//			if (i > 0 && i % BATCH_SIZE == 0) {
//	            entityManager.flush();
//	            entityManager.clear();
//	        }
//		}
//	}
	


}
