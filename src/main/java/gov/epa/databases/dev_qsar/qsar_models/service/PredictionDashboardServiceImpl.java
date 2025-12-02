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
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

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
	
	/**
	 * runs 100x faster than hibernate createBatch
	 */
	@Override
	public void createSQL(List<PredictionDashboard> predictionDashboards) {

//		if(true)return;
		
		Connection conn=SqlUtilities.getConnectionPostgres();
		int batchSize=1000;

		try {
			conn.setAutoCommit(false);
			long t1=System.currentTimeMillis();

			List<PredictionDashboard> predictionDashboards2=new ArrayList<>();

			for (int i=0;i<predictionDashboards.size();i++) {
				
				predictionDashboards2.add(predictionDashboards.get(i));

				if(predictionDashboards2.size()==batchSize) {
					saveToPredictionsDashboardTable(predictionDashboards2, conn);//
					saveToPredictionReportsTable(predictionDashboards2, conn);
					saveToADTable(predictionDashboards2, conn);
					saveToNeighborsTable(predictionDashboards2, conn);
					conn.commit();
					predictionDashboards2.clear();
				}
				//TODO is it faster to use createBatch with hibernate (should cascade)
//				saveToPredictionsDashboardTableNoKeys(predictionDashboards2, conn);
				
			}

			//Do what's left:
			if(predictionDashboards2.size()>0) {
				saveToPredictionsDashboardTable(predictionDashboards2, conn);//
				saveToPredictionReportsTable(predictionDashboards2, conn);
				saveToADTable(predictionDashboards2, conn);
				saveToNeighborsTable(predictionDashboards2, conn);
				conn.commit();	
			}
			
			long t2=System.currentTimeMillis();
			System.out.println("using createSQL2, time to post "+predictionDashboards.size()+" predictions using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			
			
//			conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String createSqlInsert(String[] fieldNames,String tableName) {
		
		String sql="INSERT INTO qsar_models."+tableName+" (";
		
		for (int i=0;i<fieldNames.length;i++) {
			sql+=fieldNames[i];
			if (i<fieldNames.length-1)sql+=",";
			else sql+=") VALUES (";
		}
		
		for (int i=0;i<fieldNames.length-1;i++) {
			sql+="?";
			if (i<fieldNames.length-1)sql+=",";			 		
		}
		sql+="current_timestamp) ON CONFLICT DO NOTHING";
		return sql;
	}

//	private List<Long> saveToPredictionsDashboardTable(List<PredictionDashboard> predictionDashboards, Connection conn) throws SQLException {
//		
//		long t1=System.currentTimeMillis();
//		
//		String [] fieldNames= {"canon_qsar_smiles", "fk_dsstox_records_id",	"fk_model_id", 
//				"prediction_value", "prediction_string", "prediction_error",
//				"experimental_string","experimental_value","dtxcid",  
//				"created_by", "created_at",
//				 };
//
//		
//		String sql = createSqlInsert(fieldNames,"predictions_dashboard");	
//		PreparedStatement prep = conn.prepareStatement(sql);
//		
//		List<Long>predictionDashboardIds=new ArrayList<>();
//		Statement statement = conn.createStatement();
//		
//		for (int counter = 0; counter < predictionDashboards.size(); counter++) {
//
//			PredictionDashboard p=predictionDashboards.get(counter);
//			prep.setString(1, p.getCanonQsarSmiles());
//			prep.setLong(2, p.getDsstoxRecord().getId());
//			prep.setLong(3, p.getModel().getId());
//
//			if (p.getPredictionValue()==null) {
//				prep.setNull(4,Types.DOUBLE);
//			} else {
//				prep.setDouble(4, p.getPredictionValue());	
//			}
//
//			prep.setString(5, p.getPredictionString());
//			prep.setString(6, p.getPredictionError());
//
//			if (p.getExperimentalString()==null) {
//				prep.setNull(7, Types.VARCHAR);
//			} else {
//				prep.setString(7, p.getExperimentalString());
//			}
//
//			if (p.getExperimentalValue()==null) {
//				prep.setNull(8,Types.DOUBLE);
//			} else {
//				prep.setDouble(8, p.getExperimentalValue());	
//			}
//
//			prep.setString(9, p.getDtxcid());
//			prep.setString(10, p.getCreatedBy());
//			//			prep.addBatch();
//
//			//			System.out.println(prep);
//
//
//
//			prep.executeUpdate(prep.toString(),Statement.RETURN_GENERATED_KEYS);
//
//			ResultSet keys=prep.getGeneratedKeys();
////			ResultSetMetaData metaData = keys.getMetaData();
//
//			//				for (int j = 0; j < metaData.getColumnCount(); j++) {
//			//				    System.out.println("Col name: "+metaData.getColumnName(j+1));
//			//				}
//
//			while (keys!=null && keys.next()) {
//				Long key = keys.getLong(1);
//				predictionDashboardIds.add(key);
//				//		            System.out.println(p.getDtxcid()+"\t"+p.getModel().getId()+"\t"+key);
//			}
//
//
//
//		}
//
//		long t2=System.currentTimeMillis();
//		System.out.println("\ttime to post "+predictionDashboards.size()+" predictionsDashboard ="+(t2-t1)/1000.0 +" seconds");
//
//		
//		return predictionDashboardIds;
//	}	
	
	

	/**
	 * This version gets the generated keys in one batch
	 * 
	 * @param predictionDashboards
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private void saveToPredictionsDashboardTable(List<PredictionDashboard> predictionDashboards, Connection conn) throws SQLException {
		
		long t1=System.currentTimeMillis();
		
		String [] fieldNames= {"canon_qsar_smiles", "dtxcid","fk_dsstox_records_id","fk_model_id", 
				"prediction_value", "prediction_string", "prediction_error",
				"experimental_string","experimental_value",  
				"created_by", "created_at",
				 };

		
		String sql = createSqlInsert(fieldNames,"predictions_dashboard");
		
//		System.out.println(sql);
		
//		PreparedStatement prep = conn.prepareStatement(sql);
		
//		PreparedStatement prep = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);//slowww!

//		https://stackoverflow.com/questions/4224228/preparedstatement-with-statement-return-generated-keys
		PreparedStatement prep = conn.prepareStatement(sql, new String[]{"id"});//for some reason much faster than using Statement.RETURN_GENERATED_KEYS!
		
		List<Long>predictionDashboardIds=new ArrayList<>();
		
		for (int counter = 0; counter < predictionDashboards.size(); counter++) {

			PredictionDashboard p=predictionDashboards.get(counter);
			
			if(p.getPredictionError()!=null && p.getPredictionError().length()>255) {
				System.out.println("error too long:"+p.getPredictionError());
			}
			
			
			int col=1;
			
			prep.setString(col++, p.getCanonQsarSmiles());
			prep.setString(col++, p.getDtxcid());
			prep.setLong(col++, p.getDsstoxRecord().getId());
			prep.setLong(col++, p.getModel().getId());

			if (p.getPredictionValue()==null) {
				prep.setNull(col++,Types.DOUBLE);
			} else {
				prep.setDouble(col++, p.getPredictionValue());	
			}

			prep.setString(col++, p.getPredictionString());
			prep.setString(col++, p.getPredictionError());

			if (p.getExperimentalString()==null) {
				prep.setNull(col++, Types.VARCHAR);
			} else {
				prep.setString(col++, p.getExperimentalString());
			}

			if (p.getExperimentalValue()==null) {
				prep.setNull(col++,Types.DOUBLE);
			} else {
				prep.setDouble(col++, p.getExperimentalValue());	
			}

			prep.setString(col++, p.getCreatedBy());
			prep.addBatch();

			//System.out.println(prep);

		}

		prep.executeBatch();
		
		long t1a=System.currentTimeMillis();
		
		ResultSet keys=prep.getGeneratedKeys();
//		ResultSetMetaData metaData = keys.getMetaData();

		//				for (int j = 0; j < metaData.getColumnCount(); j++) {
		//				    System.out.println("Col name: "+metaData.getColumnName(j+1));
		//				}

		Iterator<PredictionDashboard> pdIterator=predictionDashboards.iterator();
		
//		
		while (keys!=null && keys.next()) {
			PredictionDashboard pd=pdIterator.next();
			Long key = keys.getLong(1);
			pd.setId(key);
//			predictionDashboardIds.add(key);
//			System.out.println(key);
		}
		
		long t2=System.currentTimeMillis();

//		System.out.println("\ttime to get keys ="+(t2-t1a)/1000.0 +" seconds");
//		System.out.println("\ttime to post "+predictionDashboards.size()+" predictionsDashboard ="+(t2-t1)/1000.0 +" seconds");

		
//		return predictionDashboardIds;
	}	
	

	private void saveToPredictionsDashboardTableNoKeys(List<PredictionDashboard> predictionDashboards, Connection conn) throws SQLException {
		
		long t1=System.currentTimeMillis();
		
		String [] fieldNames= {"canon_qsar_smiles", "dtxcid",	"fk_model_id", 
				"prediction_value", "prediction_string", "prediction_error",
				"experimental_string","experimental_value","dtxcid",  
				"created_by", "created_at",
				 };

		
		String sql = createSqlInsert(fieldNames,"predictions_dashboard");	
		PreparedStatement prep = conn.prepareStatement(sql);
		
		List<Long>predictionDashboardIds=new ArrayList<>();
		Statement statement = conn.createStatement();
		
		for (int counter = 0; counter < predictionDashboards.size(); counter++) {

			PredictionDashboard p=predictionDashboards.get(counter);
			prep.setString(1, p.getCanonQsarSmiles());
			prep.setString(2, p.getDtxcid());
			prep.setLong(3, p.getModel().getId());

			if (p.getPredictionValue()==null) {
				prep.setNull(4,Types.DOUBLE);
			} else {
				prep.setDouble(4, p.getPredictionValue());	
			}

			prep.setString(5, p.getPredictionString());
			prep.setString(6, p.getPredictionError());

			if (p.getExperimentalString()==null) {
				prep.setNull(7, Types.VARCHAR);
			} else {
				prep.setString(7, p.getExperimentalString());
			}

			if (p.getExperimentalValue()==null) {
				prep.setNull(8,Types.DOUBLE);
			} else {
				prep.setDouble(8, p.getExperimentalValue());	
			}

			prep.setString(9, p.getDtxcid());
			prep.setString(10, p.getCreatedBy());
			prep.addBatch();
			//System.out.println(prep);
		}

		prep.executeBatch();
		
		long t2=System.currentTimeMillis();
		System.out.println("\ttime to post "+predictionDashboards.size()+" predictionsDashboard ="+(t2-t1)/1000.0 +" seconds");

		
		
	}	
	
	

	private void saveToPredictionReportsTable(List<PredictionDashboard> predictionDashboards, Connection conn) throws SQLException {
		
		String [] fieldNames= {"fk_predictions_dashboard_id","file_json","file_html","created_by", "created_at"};

		String sql = createSqlInsert(fieldNames,"prediction_reports");	
		PreparedStatement prep = conn.prepareStatement(sql);
		
		long t1=System.currentTimeMillis();
		
		Iterator<PredictionDashboard> pdIterator=predictionDashboards.iterator();
		
		while(pdIterator.hasNext())	 {		
			PredictionDashboard pd=pdIterator.next();
			
			if(pd.getPredictionReport()==null) {
				System.out.println("Missing a report so skipping saving to prediction_reports table");
				return;
			}
			
			PredictionReport pr=pd.getPredictionReport();
			
			if(pd.getId()==null) continue;
			
			prep.setLong(1, pd.getId());
			prep.setBytes(2, pr.getFileJson());
			prep.setBytes(3, pr.getFileHtml());
			prep.setString(4, pr.getCreatedBy());
			prep.addBatch();
		}
		prep.executeBatch();
		
		long t2=System.currentTimeMillis();
//		System.out.println("\ttime to post "+predictionDashboards.size()+" prediction reports ="+(t2-t1)/1000.0 +" seconds");
	}		
	
	

	private void saveToADTable(List<PredictionDashboard> predictionDashboards, Connection conn) throws SQLException {
		
		String [] fieldNames= {"fk_predictions_dashboard_id",
				"fk_ad_method_id",
				"applicability_value",
				"conclusion",
				"reasoning",
				"created_by", 
				"created_at"};
		
		String sql = createSqlInsert(fieldNames,"qsar_predicted_ad_estimates");	
		PreparedStatement prep = conn.prepareStatement(sql);
		
		Iterator<PredictionDashboard> pdIterator=predictionDashboards.iterator();
		
		while(pdIterator.hasNext())	 {		
			PredictionDashboard pd=pdIterator.next();
			
			if(pd.getId()==null) continue;
			
			if(pd.getQsarPredictedADEstimates()==null) continue;
			
			for (QsarPredictedADEstimate adEstimate: pd.getQsarPredictedADEstimates()) {

				prep.setLong(1, pd.getId());
				prep.setLong(2, adEstimate.getMethodAD().getId());
				
				if (adEstimate.getApplicabilityValue()==null) {
					prep.setNull(3,Types.DOUBLE);
				} else {
					prep.setDouble(3, adEstimate.getApplicabilityValue());	
				}

				if (adEstimate.getConclusion()==null) {
					prep.setNull(4,Types.VARCHAR);
				} else {
					prep.setString(4, adEstimate.getConclusion());	
				}

				if (adEstimate.getReasoning()==null) {
					prep.setNull(5,Types.VARCHAR);
				} else {
					prep.setString(5, adEstimate.getReasoning());	
				}
				
				prep.setString(6, pd.getPredictionReport().getCreatedBy());				
				prep.addBatch();
			}
		}
		prep.executeBatch();
		
		
	}	
	
	

	private void saveToNeighborsTable(List<PredictionDashboard> predictionDashboards, Connection conn) throws SQLException {
		
		String [] fieldNames= {"fk_predictions_dashboard_id",//1
				"fk_dsstox_records_id",//2
				"dtxsid",//3
				"casrn",//4
				"inchi_key_qsar_ready",//5
				"match_by",//6
				"neighbor_number",//7
				"experimental_string",//8
				"experimental_value",//9
				"predicted_string",//10
				"predicted_value",//11
				"split_num",//12
				"similarity_coefficient",//13
				"created_by", //14
				"created_at"};//15

		String sql = createSqlInsert(fieldNames,"qsar_predicted_neighbors");	
		PreparedStatement prep = conn.prepareStatement(sql);
		
		Iterator<PredictionDashboard> pdIterator=predictionDashboards.iterator();
		
		while(pdIterator.hasNext())	 {		
			PredictionDashboard pd=pdIterator.next();
			
			if(pd.getId()==null) continue;
			if(pd.getQsarPredictedNeighbors()==null) continue;
			
			for (QsarPredictedNeighbor neighbor: pd.getQsarPredictedNeighbors()) {

				prep.setLong(1, pd.getId());
								
				if(neighbor.getDsstoxRecord()==null) {					
					prep.setNull(2, Types.BIGINT);
				} else {
					prep.setLong(2, neighbor.getDsstoxRecord().getId());	
				}
				
				if (neighbor.getDtxsid()==null) {
					prep.setNull(3,Types.VARCHAR);
				} else {
					prep.setString(3, neighbor.getDtxsid());	
				}

				if (neighbor.getCasrn()==null) {
					prep.setNull(4,Types.VARCHAR);
				} else {
					prep.setString(4, neighbor.getCasrn());	
				}
				
				if (neighbor.getInchiKey()==null) {
					prep.setNull(5,Types.VARCHAR);
				} else {
					prep.setString(5, neighbor.getInchiKey());	
				}
				
				if (neighbor.getMatchBy()==null) {
					prep.setNull(6,Types.VARCHAR);
				} else {
					prep.setString(6, neighbor.getMatchBy());	
				}

				prep.setInt(7, neighbor.getNeighborNumber());		
				
				if (neighbor.getExperimentalString()==null) {
					prep.setNull(8,Types.VARCHAR);
				} else {
					prep.setString(8, neighbor.getExperimentalString());	
				}
				
				if (neighbor.getExperimentalValue()==null) {
					prep.setNull(9,Types.DOUBLE);
				} else {
					prep.setDouble(9, neighbor.getExperimentalValue());	
				}

				if (neighbor.getPredictedString()==null) {
					prep.setNull(10,Types.VARCHAR);
				} else {
					prep.setString(10, neighbor.getPredictedString());	
				}
				
				if (neighbor.getPredictedValue()==null) {
					prep.setNull(11,Types.DOUBLE);
				} else {
					prep.setDouble(11, neighbor.getPredictedValue());	
				}
				
				if (neighbor.getSplitNum()==null) {
					prep.setNull(12,Types.INTEGER);
				} else {
					prep.setInt(12, neighbor.getSplitNum());	
				}
				
				if (neighbor.getSimilarityCoefficient()==null) {
					prep.setNull(13,Types.DOUBLE);
				} else {
					prep.setDouble(13, neighbor.getSimilarityCoefficient());	
				}

				prep.setString(14, pd.getPredictionReport().getCreatedBy());				
				prep.addBatch();
			}
		}
		prep.executeBatch();
	}	
	/**
	 * Use cross schema query so can get precise property name from dataset
	 * 
	 * @param dtxcid
	 * @param propertyName
	 * @param modelSource
	 * @return
	 */
	public String getPredictionDashboardAsJson(String dtxsid, String propertyName, String modelSource) {
		
		String sql="select p.\"name\" as property, m.\"source\",m.\"name\" as model_name,"
				+ "pd.canon_qsar_smiles, pd.smiles, pd.dtxsid, pd.dtxcid,"
				+ "pd.prediction_value as prediction_value,"
				+ "u.abbreviation as prediction_units, "
				+ "pd.prediction_string as prediction_string, "
				+ "pd.prediction_error as prediction_error"
				+ " from qsar_models.predictions_dashboard pd\n"+
		"join qsar_models.models m on m.id=pd.fk_model_id\n"+
		"join qsar_datasets.datasets d on d.\"name\" =m.dataset_name\n"+
		"join qsar_datasets.properties p on p.id=d.fk_property_id\n"+
		"join qsar_datasets.units u on u.id=d.fk_unit_id_contributor\n"+
		"where m.\"source\" ='"+modelSource+"' and dtxsid='"+dtxsid+"' and p.\"name\"='"+propertyName+"'";
		
//		System.out.println(sql);
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		
		try {
			
			JsonObject jo=new JsonObject();
			
			ResultSetMetaData metadata=rs.getMetaData();	
			
//			System.out.println(metadata.getColumnCount());			
			
			while (rs.next()) {
				
				for (int i=1;i<=metadata.getColumnCount();i++) {
					jo.addProperty(metadata.getColumnLabel(i),rs.getString(i));
					
//					System.out.println(i+"\t"+metadata.getColumnLabel(i)+"\t"+rs.getString(i));
				}
				return Utilities.gson.toJson(jo);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "error getting report";
		
	}
	
	public static void main(String[] args) {
		PredictionDashboardServiceImpl p=new PredictionDashboardServiceImpl();
		String dtxsid="DTXSID80177704";
		String propertyName="Water solubility";
		String modelSource="TEST5.1.3";
		String json=p.getPredictionDashboardAsJson(dtxsid, "Water solubility", modelSource);
		System.out.println(json);
	}
	

}
