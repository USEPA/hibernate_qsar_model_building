package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DataPointInSplittingDao;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DataPointInSplittingDaoImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;

public class DataPointInSplittingServiceImpl implements DataPointInSplittingService {
	
	private Validator validator;
	
	@Override
	public void createSQL (List<DataPointInSplitting> dpisList) {

		Connection conn=DatabaseLookup.getConnectionPostgres();
		
		String [] fieldNames= {"fk_data_point_id","fk_splitting_id","split_num","created_by","created_at"};
		int batchSize=1000;
		
		
		String sql="INSERT INTO qsar_datasets.data_points_in_splittings (";
		
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

			for (int counter = 0; counter < dpisList.size(); counter++) {
				DataPointInSplitting dpis=dpisList.get(counter);
				prep.setLong(1, dpis.getDataPoint().getId());
				prep.setLong(2, dpis.getSplitting().getId());
				prep.setLong(3, dpis.getSplitNum());
				prep.setString(4, dpis.getCreatedBy());
				prep.addBatch();
				
				if (counter % batchSize == 0 && counter!=0) {
					// System.out.println(counter);
					prep.executeBatch();
				}
			}

			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+dpisList.size()+" DPIS using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
//			conn.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}	
	
	
	
	
	
	
	public DataPointInSplittingServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public List<DataPointInSplitting> findByDatasetNameAndSplittingName(String datasetName, String splittingName) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findByDatasetNameAndSplittingName(datasetName, splittingName, session);
	}
	
	public List<DataPointInSplitting> findByDatasetNameAndSplittingName(String datasetName, String splittingName, Session session) {
		Transaction t = session.beginTransaction();
		DataPointInSplittingDao dataPointInSplittingDao = new DataPointInSplittingDaoImpl();
		List<DataPointInSplitting> dataPointsInSplitting = 
				dataPointInSplittingDao.findByDatasetNameAndSplittingName(datasetName, splittingName, session);
		t.rollback();
		return dataPointsInSplitting;
	}
	

	@Override
	public DataPointInSplitting create(DataPointInSplitting dpis) throws ConstraintViolationException {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return create(dpis, session);
	}

	//***** TODO need batch insert mode!!!- this is slow on VPN
	@Override
	public DataPointInSplitting create(DataPointInSplitting dpis, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<DataPointInSplitting>> violations = validator.validate(dpis);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(dpis);
			session.flush();
//			session.refresh(dpis);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return dpis;
	}
	
	@Override
	public void delete(DataPointInSplitting dpis) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		delete(dpis, session);
	}

	@Override
	public void delete(DataPointInSplitting dpis, Session session) {
		if (dpis.getId()==null) {
			return;
		}
		
		Transaction t = session.beginTransaction();
		session.delete(dpis);
		session.flush();
		t.commit();
	}
	
}
