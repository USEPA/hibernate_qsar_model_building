package gov.epa.databases.dev_qsar.qsar_models.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.List;

import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.DsstoxRecordDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxSnapshot;
import gov.epa.run_from_java.scripts.SqlUtilities;

public class DsstoxRecordServiceImpl  {

	private Validator validator;
	
	public DsstoxRecordServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
//	public List<DataPoint> findByDatasetName(String dataPointName) {
//		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
//		return findByDatasetName(dataPointName, session);
//	}
//	
//	public List<DataPoint> findByDatasetName(String dataPointName, Session session) {
//		Transaction t = session.beginTransaction();
//		DataPointDao dataPointDao = new DataPointDaoImpl();
//		List<DataPoint> dataPoints = dataPointDao.findByDatasetName(dataPointName, session);
//		t.rollback();
//		return dataPoints;
//	}
//	
//	public List<DataPoint> findByDatasetId(Long datasetId) {
//		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
//		return findByDatasetId(datasetId, session);
//	}
//	
//	public List<DataPoint> findByDatasetId(Long datasetId, Session session) {
//		Transaction t = session.beginTransaction();
//		DataPointDao dataPointDao = new DataPointDaoImpl();
//		List<DataPoint> dataPoints = dataPointDao.findByDatasetId(datasetId, session);
//		t.rollback();
//		return dataPoints;
//	}
//	
//	
//	public DataPoint create(DataPoint dataPoint) throws ConstraintViolationException {
//		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
//		return create(dataPoint, session);
//	}
//
//	
//	public DataPoint create(DataPoint dataPoint, Session session) throws ConstraintViolationException {
//		Set<ConstraintViolation<DataPoint>> violations = validator.validate(dataPoint);
//		if (!violations.isEmpty()) {
//			throw new ConstraintViolationException(violations);
//		}
//		
//		Transaction t = session.beginTransaction();
//		
//		try {
//			session.save(dataPoint);
//			session.flush();
////			session.refresh(dataPoint);
//			t.commit();
//		} catch (org.hibernate.exception.ConstraintViolationException e) {
//			t.rollback();
//			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
//		}
//		
//		return dataPoint;
//	}

	
//	public List<DataPoint> createBatch(List<DataPoint> dataPoints) throws ConstraintViolationException {
//		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
//		return createBatch(dataPoints, session);
//	}
//
//	
//	public List<DataPoint> createBatch(List<DataPoint> dataPoints, Session session)
//			throws ConstraintViolationException {
//		Transaction tx = session.beginTransaction();
//		try {
//		for (int i = 0; i < dataPoints.size(); i++) {
//			DataPoint dataPoint = dataPoints.get(i);
//			session.save(dataPoint);
//		    if ( i % 1000 == 0 ) { //50, same as the JDBC batch size
//		        //flush a batch of inserts and release memory:
//		        session.flush();
//		        session.clear();
//		    }
//		}
//		} catch (org.hibernate.exception.ConstraintViolationException e) {
//			tx.rollback();
//		}
//		
//		tx.commit();
//		session.close();
//		return dataPoints;
//	}
	


	/**
	 * TODO fix this method to add all the columns that are currently in the table
	 * 
	 * @param records
	 */
	public void createBatchSQL (List<DsstoxRecord> records) {

		Connection conn=SqlUtilities.getConnectionPostgres();
		
		Long fk_snapshot_id=records.get(0).getDsstoxSnapshot().getId();
		
		String [] fieldNames= {"dtxcid","dtxsid","smiles","fk_dsstox_snapshot_id","created_by","created_at"};
		int batchSize=1000;
		
		String sql="INSERT INTO qsar_models.dsstox_records (";
		
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

			for (int counter = 0; counter < records.size(); counter++) {
				DsstoxRecord record=records.get(counter);
				
				prep.setString(1, record.getDtxcid());
				prep.setString(2, record.getDtxsid());
				prep.setString(3, record.getSmiles());				
				prep.setLong(4,record.getDsstoxSnapshot().getId());
				prep.setString(5, record.getCreatedBy());
				prep.addBatch();
				
				if (counter % batchSize == 0 && counter!=0) {
					// System.out.println(counter);
					prep.executeBatch();
					conn.commit();
				}
			}

			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+records.size()+" records using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
//			conn.setAutoCommit(true);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}	
	
	public void createBatchSQLNoCompound (List<DsstoxRecord> records) {

		Connection conn=SqlUtilities.getConnectionPostgres();
		
		Long fk_snapshot_id=records.get(0).getDsstoxSnapshot().getId();
		
		String [] fieldNames= {"dtxsid","casrn","preferred_name","mol_image_png_available","fk_dsstox_snapshot_id","created_by","created_at"};

		int batchSize=1000;
		
		String sql="INSERT INTO qsar_models.dsstox_records (";
		
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

			for (int counter = 0; counter < records.size(); counter++) {
				DsstoxRecord record=records.get(counter);
				
				prep.setString(1, record.getDtxsid());
				prep.setString(2, record.getCasrn());
				prep.setString(3, record.getPreferredName());
				prep.setBoolean(4, false);
				prep.setLong(5,record.getDsstoxSnapshot().getId());
				prep.setString(6, record.getCreatedBy());
				
				prep.addBatch();
				
				if (counter % batchSize == 0 && counter!=0) {
					// System.out.println(counter);
					prep.executeBatch();
					conn.commit();
				}
			}

			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+records.size()+" records using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
//			conn.setAutoCommit(true);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}	
	
	public Hashtable<String,Long> getRecordIdHashtable(DsstoxSnapshot snapshot,String keyName) {
		
		String sql2="select "+keyName+", id from qsar_models.dsstox_records "
				+ "where fk_dsstox_snapshot_id="+snapshot.getId();

		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql2);
		
		Hashtable<String,Long>htID_to_FK=new Hashtable<>();
		
		try {
			while (rs.next()) {
				String key=rs.getString(1);
				Long id=rs.getLong(2);
				
				if(key==null) continue;
				
//				System.out.println(dtxcid+"\t"+id);
				
				htID_to_FK.put(key, id);
				
//				System.out.println(dtxcid+"\t"+id);
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Got look up from id to dsstox_records_id:"+htID_to_FK.size());
		
		return htID_to_FK;
	}

	public void updateFkCompoundsIdBatchSQL(List<DsstoxRecord> records,String lanId) {

		Connection conn=SqlUtilities.getConnectionPostgres();
		
		
		String SQL_UPDATE = "UPDATE qsar_models.dsstox_records SET cid=?, updated_by=?, updated_at=current_timestamp WHERE dtxcid=?";
		int batchSize=1000;
		
		try {
			conn.setAutoCommit(false);
			PreparedStatement prep = conn.prepareStatement(SQL_UPDATE);
			prep.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			long t1=System.currentTimeMillis();

			for (int counter = 0; counter < records.size(); counter++) {
				DsstoxRecord record=records.get(counter);
				
				
				prep.setLong(1, record.getCid());
				prep.setString(2, lanId);
				prep.setString(3, record.getDtxcid());
				prep.addBatch();

				if (counter % batchSize == 0 && counter!=0) {
					// System.out.println(counter);
					prep.executeBatch();
					conn.commit();
				}
				
//				if(true) break;
				
			}

			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+records.size()+" records using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
//			conn.setAutoCommit(true);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void updateMolWeightBatchSQL(List<DsstoxRecord> records,String lanId) {

		Connection conn=SqlUtilities.getConnectionPostgres();
		
		
		String SQL_UPDATE = "UPDATE qsar_models.dsstox_records SET mol_weight=?, updated_by=?, updated_at=current_timestamp WHERE dtxcid=?";
		int batchSize=1000;
		
		try {
			conn.setAutoCommit(false);
			PreparedStatement prep = conn.prepareStatement(SQL_UPDATE);
			prep.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			long t1=System.currentTimeMillis();

			for (int counter = 0; counter < records.size(); counter++) {
				DsstoxRecord record=records.get(counter);
				
				prep.setDouble(1, record.getMolWeight());
				prep.setString(2, lanId);
				prep.setString(3, record.getDtxcid());
				prep.addBatch();
				
				if (counter % batchSize == 0 && counter!=0) {
					// System.out.println(counter);
					prep.executeBatch();
					conn.commit();
				}
				
//				if(true) break;
				
			}

			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+records.size()+" records using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
//			conn.setAutoCommit(true);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	public void updatePreferredNameCASRNBatchSQL(List<DsstoxRecord> records, String lanId) {
Connection conn=SqlUtilities.getConnectionPostgres();
		
		
		String SQL_UPDATE = "UPDATE qsar_models.dsstox_records SET preferred_name=?, casrn=?, updated_by=?, updated_at=current_timestamp WHERE dtxcid=?";
		int batchSize=1000;
		
		try {
			conn.setAutoCommit(false);
			PreparedStatement prep = conn.prepareStatement(SQL_UPDATE);
			prep.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			long t1=System.currentTimeMillis();

			for (int counter = 0; counter < records.size(); counter++) {
				DsstoxRecord record=records.get(counter);
				
				
//				System.out.println(record.getPreferredName()+"\t"+record.getCasrn());
				
				prep.setString(1, record.getPreferredName());
				prep.setString(2, record.getCasrn());
				prep.setString(3, lanId);
				prep.setString(4, record.getDtxcid());
				prep.addBatch();
				
				if (counter % batchSize == 0 && counter!=0) {
					// System.out.println(counter);
					prep.executeBatch();
					conn.commit();
				}
				
//				if(true) break;
				
			}

			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+records.size()+" records using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
//			conn.setAutoCommit(true);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}



	public List<DsstoxRecord> findAll() {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	

	
	public List<DsstoxRecord> findAll(Session session) {
		Transaction t = session.beginTransaction();
		DsstoxRecordDaoImpl dao = new DsstoxRecordDaoImpl();
		List<DsstoxRecord> recs = dao.findAll(session);
		t.rollback();
		return recs;
	}


	

}
