package gov.epa.databases.dev_qsar.qsar_models.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openscience.cdk.tools.SystemOutLoggingTool;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.PredictionReportDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.PredictionReportDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.GenericSubstance;
import gov.epa.databases.dsstox.entity.GenericSubstanceCompound;
import gov.epa.run_from_java.scripts.SqlUtilities;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public class PredictionReportServiceImpl implements PredictionReportService {
	
	private Validator validator;
	
	public PredictionReportServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public PredictionReport findByPredictionDashboardId(Long predictionDashboardId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByPredictionDashboardId(predictionDashboardId, session);
	}
	
	public PredictionReport findByPredictionDashboardId(Long predictionDashboardId, Session session) {
		Transaction t = session.beginTransaction();
		PredictionReportDao predictionReportDao = new PredictionReportDaoImpl();
		PredictionReport predictionReport = predictionReportDao.findByPredictionDashboardId(predictionDashboardId, session);
		t.rollback();
		return predictionReport;
	}

	@Override
	public PredictionReport create(PredictionReport predictionReport) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(predictionReport, session);
	}

	@Override
	public PredictionReport create(PredictionReport predictionReport, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<PredictionReport>> violations = validator.validate(predictionReport);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(predictionReport);
			session.flush();
			session.refresh(predictionReport);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return predictionReport;
	}
	
	@Override
	public List<PredictionReport> createBatch(List<PredictionReport> reports) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return createBatch(reports, session);
	}

	@Override
	public List<PredictionReport> createBatch(List<PredictionReport> reports, Session session)
			throws ConstraintViolationException {

		Transaction tx = session.beginTransaction();
		try {
			for (int i = 0; i < reports.size(); i++) {
				PredictionReport report = reports.get(i);
				session.save(report);
				if ( i % 1000 == 0 ) { //50, same as the JDBC batch size
					//flush a batch of inserts and release memory:
					session.flush();
					session.clear();
				}
			}

			session.flush();//do the remaining ones
			session.clear();


		} catch (org.hibernate.exception.ConstraintViolationException e) {
			tx.rollback();
		}

		tx.commit();
		session.close();
		return reports;
	}

	@Override
	public void delete(PredictionReport predictionReport) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		delete(predictionReport, session);
	}

	@Override
	public void delete(PredictionReport predictionReport, Session session) {
		if (predictionReport.getId()==null) {
			return;
		}
		
		Transaction t = session.beginTransaction();
		session.delete(predictionReport);
		session.flush();
		t.commit();
	}

//	public Hashtable<String,Long>getHashtableLookupPredictionDashboardId(long minModelId,long maxModelId) {
	public Hashtable<String,Long>getHashtableLookupPredictionDashboardId() {

		long t1=System.currentTimeMillis();
		Hashtable<String,Long>ht=new Hashtable<>();
		
//		String sql2="select pd.canon_qsar_smiles,smiles,dtxsid,dtxcid,fk_model_id, pd.id from qsar_models.predictions_dashboard pd\n"+
//					"where fk_model_id>="+minModelId+" and fk_model_id<="+maxModelId;

		String sql2="select pd.canon_qsar_smiles,smiles,dtxsid,dtxcid,fk_model_id, pd.id from qsar_models.predictions_dashboard pd";
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql2);
		
		try {
			while (rs.next()) {
				String key=rs.getString(1)+"\t"+rs.getString(2)+"\t"+rs.getString(3)+"\t"+rs.getString(4)+"\t"+rs.getLong(5);
				Long value=rs.getLong(6);
				
				System.out.println(key+"\t"+value);
				ht.put(key,value );
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long t2=System.currentTimeMillis();
		
		System.out.println("time to get ht:"+(t2-t1)/1000.0+" seconds");
		
		return ht;
		
		
	}
	
	public List<PredictionReport> getNonUpdatedPredictionReportsBySQL(int offset,int limit) {

		List<PredictionReport>reports=new ArrayList<>();

		String sql="SELECT id, file, created_by, created_at, fk_prediction_dashboard_id from qsar_models.prediction_reports\n";  
		sql+="where updated_by is null\n";
		sql+="ORDER BY id\n";
		sql+="LIMIT "+limit+" OFFSET "+offset;

//		System.out.println(sql);

		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);

		try {
			
			while (rs.next()) {

				PredictionReport pr=new PredictionReport();				

				pr.setId(rs.getLong(1));
				pr.setFile(rs.getBytes(2));
				pr.setCreatedBy(rs.getString(3));
				pr.setCreatedAt(rs.getDate(4));
				
				PredictionDashboard pd=new PredictionDashboard();
				pr.setPredictionDashboard(pd);
				pd.setId(rs.getLong(5));

				reports.add(pr);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		System.out.println(compounds.size());
		return reports;
	}

	
	public void createSQL(List<PredictionReport> predictionReports) {

//		Hashtable<String,Long>htPredictionDashboardIds=getHashtableLookupPredictionDashboardId(minModelId,maxModelId);		
//		Hashtable<String,Long>htPredictionDashboardIds=getHashtableLookupPredictionDashboardId();
		
		
		Connection conn=SqlUtilities.getConnectionPostgres();
		
		String [] fieldNames= {"fk_prediction_dashboard_id", "file","created_by", "created_at"};

		int batchSize=1000;
		
		String sql="INSERT INTO qsar_models.prediction_reports (";
		
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

			for (int counter = 0; counter < predictionReports.size(); counter++) {
				
				PredictionReport pr=predictionReports.get(counter);
				PredictionDashboard pd=pr.getPredictionDashboard();
				
				
				String fk_prediction_dashboard_id = getPredictionDashboardId(conn, pd);
				
//				Long fk_prediction_dashboard_id=htPredictionDashboardIds.get(pd.getKey());
								
				if (fk_prediction_dashboard_id==null) {
					System.out.println("Couldnt retrieve id for:"+pd.getKey()+"\t"+pd.getModel().getName());
					continue;
				}
				
				
//				System.out.println("time to get id="+(t1b-t1a)/1000.0+" seconds");
//				String key=pd.getCanonQsarSmiles()+pd.getSmiles()+pd.getDtxsid()+pd.getDtxcid()+pd.getModel().getId();
//				Long fk_prediction_dashboard_id=htPredictionDashboardIds.get(key);
				
//				System.out.println(fk_prediction_dashboard_id);	
				
				prep.setLong(1, Long.parseLong(fk_prediction_dashboard_id));
//				prep.setLong(1, fk_prediction_dashboard_id);
				prep.setBytes(2, pr.getFile());
				prep.setString(3, pr.getCreatedBy());
				prep.addBatch();
				
				if (counter % batchSize == 0 && counter!=0) {
//					System.out.println("\t"+counter);
					prep.executeBatch();
					conn.commit();
				}
			}

			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+predictionReports.size()+" reports using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
//			conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static void updateSQL (PredictionReport pr) 	{
		
		try	{
			
			Connection conn=SqlUtilities.getConnectionPostgres();
			
			// create our java preparedstatement using a sql update query
			PreparedStatement prep = conn.prepareStatement(
					"UPDATE qsar_models.prediction_reports SET file = ?, updated_by = ?, updated_at=current_timestamp WHERE id = ?");

			// set the preparedstatement parameters
			prep.setBytes(1,pr.getFile());
			prep.setString(2,pr.getUpdatedBy());
			prep.setLong(3,pr.getId());

			// call executeUpdate to execute our sql update statement
			prep.executeUpdate();
			prep.close();
		}  catch (Exception se)  {
			// log the exception
			se.printStackTrace();
		}
	}
	
	public void updateSQL(List<PredictionReport> predictionReports) {

//		Hashtable<String,Long>htPredictionDashboardIds=getHashtableLookupPredictionDashboardId(minModelId,maxModelId);		
//		Hashtable<String,Long>htPredictionDashboardIds=getHashtableLookupPredictionDashboardId();
		
		
		Connection conn=SqlUtilities.getConnectionPostgres();

		int batchSize=100;
		
		try {
			conn.setAutoCommit(false);

			PreparedStatement prep = conn.prepareStatement(
					"UPDATE qsar_models.prediction_reports SET file = ?, updated_by = ?, updated_at=current_timestamp WHERE id = ?");

			long t1=System.currentTimeMillis();

			for (int counter = 0; counter < predictionReports.size(); counter++) {
				
				PredictionReport pr=predictionReports.get(counter);
				
				prep.setBytes(1,pr.getFile());
				prep.setString(2,pr.getUpdatedBy());
				prep.setLong(3,pr.getId());
				prep.addBatch();
				
				
				if (counter % batchSize == 0 && counter!=0) {
					System.out.println("\t"+counter);
					prep.executeBatch();
					conn.commit();
				}
			}

			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+predictionReports.size()+" updates using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
//			conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	public String getPredictionDashboardId(Connection conn, PredictionDashboard pd) {
		long t1a=System.currentTimeMillis();
		String sql2="select id from qsar_models.predictions_dashboard where\n"+
				"canon_qsar_smiles='"+pd.getCanonQsarSmiles()+"'\n"+
				"and fk_dsstox_records_id="+pd.getDsstoxRecord().getId()+"\n"+
				"and fk_model_id="+pd.getModel().getId()+";";
		
//				System.out.println(sql2);		
		String strID=SqlUtilities.runSQL(conn, sql2);


//				System.out.println(fk_prediction_dashboard_id);	
		long t1b=System.currentTimeMillis();
		
//		System.out.println((t1b-t1a)/1000.0+" secs");
		
		
		return strID;
	}


	
	/**
	 * Use cross schema query so can get precise property name from dataset
	 * 
	 * @param dtxcid
	 * @param propertyName
	 * @param modelSource
	 * @return
	 */
	public String getReport(String dtxsid, String propertyName, String modelSource) {
		
		String sql=" select pr.file as report from qsar_models.prediction_reports pr\n"+
        "join qsar_models.predictions_dashboard pd on pr.fk_prediction_dashboard_id = pd.id\n"+
        "join qsar_models.models m on pd.fk_model_id = m.id\n"+
        "join qsar_datasets.datasets d on d.\"name\" = m.dataset_name\n"+
        "join qsar_datasets.properties p on d.fk_property_id = p.id\n"+
        "where pd.dtxsid = '"+dtxsid+"' and p.\"name\" = '"+propertyName+"' and m.source='"+modelSource+"';";

//		System.out.println(sql);
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		
		try {
			while (rs.next()) {
				byte[]bytes=rs.getBytes(1);
				return new String (bytes);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "error getting report";
		
	}
	
	
//	/**
//	 * Use simpler query based on dataset name = propertyName + " "+modelSource
//	 * 
//	 * @param dtxcid
//	 * @param propertyName
//	 * @param modelSource
//	 * @return
//	 */
//	public String getReport(String dtxsid, String datasetName) {
//		
//		String sql=" select pr.file as report from qsar_models.prediction_reports pr\n"+
//        "join qsar_models.predictions_dashboard pd on pr.fk_prediction_dashboard_id = pd.id\n"+
//        "join qsar_models.models m on pd.fk_model_id = m.id\n"+
//        "where pd.dtxsid = '"+dtxsid+"' and m.dataset_name = '"+datasetName+"';";
//
////		System.out.println(sql);
//		
//		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
//		
//		try {
//			while (rs.next()) {
//				byte[]bytes=rs.getBytes(1);
//				return new String (bytes);
//			}
//			
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return "error getting report";
//		
//	}
}
