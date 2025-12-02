package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DataPointDao;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DataPointDaoImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.run_from_java.scripts.SqlUtilities;

public class DataPointServiceImpl implements DataPointService {

	private Validator validator;
	
	public DataPointServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public List<DataPoint> findByDatasetName(String dataPointName) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findByDatasetName(dataPointName, session);
	}
	
	public List<DataPoint> findByDatasetName(String dataPointName, Session session) {
		Transaction t = session.beginTransaction();
		DataPointDao dataPointDao = new DataPointDaoImpl();
		List<DataPoint> dataPoints = dataPointDao.findByDatasetName(dataPointName, session);
		t.rollback();
		return dataPoints;
	}
	
	public List<DataPoint> findByDatasetId(Long datasetId) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findByDatasetId(datasetId, session);
	}
	
	public List<DataPoint> findByDatasetId(Long datasetId, Session session) {
		Transaction t = session.beginTransaction();
		DataPointDao dataPointDao = new DataPointDaoImpl();
		List<DataPoint> dataPoints = dataPointDao.findByDatasetId(datasetId, session);
		t.rollback();
		return dataPoints;
	}
	
	@Override
	public DataPoint create(DataPoint dataPoint) throws ConstraintViolationException {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return create(dataPoint, session);
	}

	@Override
	public DataPoint create(DataPoint dataPoint, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<DataPoint>> violations = validator.validate(dataPoint);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.persist(dataPoint);
			session.flush();
//			session.refresh(dataPoint);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return dataPoint;
	}

	@Override
	public List<DataPoint> createBatch(List<DataPoint> dataPoints) throws ConstraintViolationException {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return createBatch(dataPoints, session);
	}

	@Override
	public List<DataPoint> createBatch(List<DataPoint> dataPoints, Session session)
			throws ConstraintViolationException {
		Transaction tx = session.beginTransaction();
		try {
		for (int i = 0; i < dataPoints.size(); i++) {
			DataPoint dataPoint = dataPoints.get(i);
			session.persist(dataPoint);
		    if ( i % 1000 == 0 ) { //50, same as the JDBC batch size
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
		return dataPoints;
	}
	

	@Override
	public void createBatchSQL (List<DataPoint> dataPoints) {

		Connection conn=SqlUtilities.getConnectionPostgres();
		
		String [] fieldNames= {"canon_qsar_smiles","qsar_property_value","qsar_dtxcid","qsar_exp_prop_property_values_id","outlier","fk_dataset_id","created_by","created_at"};
		int batchSize=1000;
		
		String sql="INSERT INTO qsar_datasets.data_points (";
		
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

			for (int counter = 0; counter < dataPoints.size(); counter++) {
				DataPoint dp=dataPoints.get(counter);
				
				prep.setString(1, dp.getCanonQsarSmiles());
				prep.setDouble(2,dp.getQsarPropertyValue());
				prep.setString(3, dp.getQsar_dtxcid());
				prep.setString(4, dp.getQsar_exp_prop_property_values_id());
				prep.setBoolean(5, false);
				prep.setLong(6, dp.getDataset().getId());
				prep.setString(7, dp.getCreatedBy());
				prep.addBatch();
				
				if (counter % batchSize == 0 && counter!=0) {
					// System.out.println(counter);
					prep.executeBatch();
				}
			}

			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+dataPoints.size()+" datapoints using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
//			conn.setAutoCommit(true);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}	
	

}
