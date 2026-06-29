package gov.epa.databases.dev_qsar.qsar_models.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import java.util.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_descriptors.QsarDescriptorsSession;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.PredictionDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.PredictionDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.dao.PredictionDashboardDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.PredictionDashboardDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Bob;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedADEstimate;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.util.JsonUtilities;

public class PredictionDashboardServiceImpl implements PredictionDashboardService {
	Validator validator;

	public PredictionDashboardServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}

	@Override
	public PredictionDashboard findByIds(Long modelID,Long dsstoxRecordId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByIds(modelID, dsstoxRecordId, session);
	}
	
	public List<PredictionDashboard> findBySourceNameAndDTXSID(String sourceName, String DTXSID) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findBySourceNameAndDTXSID(sourceName, DTXSID, session);
	}
	
	
	public List<PredictionDashboard> findBySourceNameAndDTXCID(String sourceName, String DTXCID) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findBySourceNameAndDTXCID(sourceName, DTXCID, session);
	}



	private List<PredictionDashboard> findBySourceNameAndDTXCID(String sourceName, String DTXCID, Session session) {
		Transaction t = session.beginTransaction();
		PredictionDashboardDao predictionDao = new PredictionDashboardDaoImpl();
		List<PredictionDashboard> predictionsDashboard = predictionDao.findBySourceNameAndDTXCID(sourceName, DTXCID, session);
		t.rollback();
		return predictionsDashboard;
	}

	private List<PredictionDashboard> findBySourceNameAndDTXSID(String sourceName, String DTXSID, Session session) {
		Transaction t = session.beginTransaction();
		PredictionDashboardDao predictionDao = new PredictionDashboardDaoImpl();
		List<PredictionDashboard> predictionsDashboard = predictionDao.findBySourceNameAndDTXSID(sourceName, DTXSID, session);
		t.rollback();
		return predictionsDashboard;
	}

	@Override
	public PredictionDashboard findByIds(Long modelId,Long dsstoxRecordId, Session session) {
		Transaction t = session.beginTransaction();
		PredictionDashboardDao predictionDao = new PredictionDashboardDaoImpl();
		PredictionDashboard predictionsDashboard = predictionDao.findByIds(modelId, dsstoxRecordId, session);
		t.rollback();
		return predictionsDashboard;
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
			session.persist(predictionDashboard);
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
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return createBatch(predictionDashboard, session);
	}


	@Override
	public List<PredictionDashboard> createBatch(List<PredictionDashboard> predictionDashboards, Session session)
			throws org.hibernate.exception.ConstraintViolationException {
		Transaction tx = session.beginTransaction();
		
		int batchSize=100;
		
		try {
			
			long t1=System.currentTimeMillis();
			
//			for (int i = 0; i < predictionDashboards.size(); i++) {
//				PredictionDashboard predictionDashboard = predictionDashboards.get(i);
//				System.out.println(i+"\t"+predictionDashboard.getCanonQsarSmiles());
//			}
			
			for (int i = 0; i < predictionDashboards.size(); i++) {
				PredictionDashboard predictionDashboard = predictionDashboards.get(i);
				session.persist(predictionDashboard);
				
				
				if ( i % batchSize == 0 ) { //20, same as the JDBC batch size
					//flush a batch of inserts and release memory:
					session.flush();
					session.clear();
					System.out.println(i);
				}
			}

			session.flush();//do the remaining ones
			session.clear();

			long t2=System.currentTimeMillis();

			System.out.println("using createBatch, time to post "+predictionDashboards.size()+" predictions using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");


		} catch (org.hibernate.exception.ConstraintViolationException e) {
			e.printStackTrace();
			tx.rollback();
		}

		tx.commit();
		session.close();
		
		return predictionDashboards;//TODO this doesnt update with the stored id numbers
	}
	

}
