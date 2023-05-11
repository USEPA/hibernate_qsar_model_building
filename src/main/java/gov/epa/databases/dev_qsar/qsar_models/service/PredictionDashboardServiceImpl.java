package gov.epa.databases.dev_qsar.qsar_models.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_descriptors.QsarDescriptorsSession;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.Bob;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.run_from_java.scripts.SqlUtilities;

public class PredictionDashboardServiceImpl implements PredictionDashboardService {
	Validator validator;

	public PredictionDashboardServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}

	
	@Override
	public PredictionDashboard create(PredictionDashboard predictionDashboard) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(predictionDashboard, session);
	}


	@Override
	public PredictionDashboard create(PredictionDashboard predictionDashboard, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<PredictionDashboard>> violations = validator.validate(predictionDashboard);

		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(predictionDashboard);
			session.flush();
			session.refresh(predictionDashboard);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return predictionDashboard;
	}


	@Override
	public List<PredictionDashboard> createBatch(List<PredictionDashboard> predictionDashboard)
			throws org.hibernate.exception.ConstraintViolationException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<PredictionDashboard> createBatch(List<PredictionDashboard> predictionDashboards, Session session)
			throws org.hibernate.exception.ConstraintViolationException {
		Transaction tx = session.beginTransaction();
		try {
		for (int i = 0; i < predictionDashboards.size(); i++) {
			PredictionDashboard predictionDashboard = predictionDashboards.get(i);
			session.save(predictionDashboard);
		    if ( i % 20 == 0 ) { //20, same as the JDBC batch size
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
		return predictionDashboards;
	}
	
	
	
	
	@Override
	public void createSQL (List<PredictionDashboard> predictionDashboards) {

		Connection conn=SqlUtilities.getConnectionPostgres();
		
		String [] fieldNames= {"smiles", "canon_qsar_smiles", "dtxcid", "dtxsid",
				"fk_model_id", "prediction_value", "prediction_string", "prediction_error",
				 "updated_by", "created_by", "created_at"};

		int batchSize=1000;
		
		String sql="INSERT INTO qsar_models.predictions_dashboard (";
		
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
			long t1=System.currentTimeMillis();

			for (int counter = 0; counter < predictionDashboards.size(); counter++) {
				PredictionDashboard p=predictionDashboards.get(counter);
				prep.setString(1, p.getSmiles());
				prep.setString(2, p.getCanonQsarSmiles());
				prep.setString(3, p.getDtxcid());
				prep.setString(4, p.getDtxsid());
				prep.setLong(5, p.getModel().getId());
				prep.setDouble(6, p.getPredictionValue());
				prep.setString(7, p.getPredictionString());
				prep.setString(8, p.getPredictionError());
				prep.setString(9, p.getUpdatedBy());
				prep.setString(10, p.getCreatedBy());
				
				
				prep.addBatch();
				
				if (counter % batchSize == 0 && counter!=0) {
					// System.out.println(counter);
					prep.executeBatch();
				}
			}

			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+predictionDashboards.size()+" predictions using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
//			conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}			


}
