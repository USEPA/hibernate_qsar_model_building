package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxSnapshot;
import gov.epa.databases.dev_qsar.qsar_models.entity.Source;
import gov.epa.run_from_java.scripts.SqlUtilities;

/**
* @author TMARTI02
*/
public class DatabaseUtilities {

	public void deleteRecordsSimple(String table, long minModelNumber,long maxModelNumber) {
		long t1=System.currentTimeMillis();
		System.out.print("Deleting from "+table+"...");
		String sql="delete from qsar_models."+table+" pr using qsar_models.predictions_dashboard pd\n"+
				"where pr.fk_predictions_dashboard_id = pd.id and pd.fk_model_id >="+minModelNumber+" and pd.fk_model_id <="+maxModelNumber+";";
		SqlUtilities.runSQLUpdate(SqlUtilities.getConnectionPostgres(), sql);
	
		long t2=System.currentTimeMillis();
		System.out.println("Done in "+(t2-t1)/1000.0+" seconds");
		//		System.out.println(sql);
	
	}
	


	public void deleteAllRecords(long minModelId,long maxModelId) {
		//		deleteRecords("qsar_predicted_neighbors",1);
		//		deleteRecords("qsar_predicted_ad_estimates",1);
		//		deleteRecords("prediction_reports",1);
		//		deleteOPERA_Predictions();
	
		deleteRecordsSimple("qsar_predicted_neighbors",minModelId,maxModelId);
		deleteRecordsSimple("qsar_predicted_ad_estimates",minModelId,maxModelId);
		deleteRecordsSimple("prediction_reports",minModelId,maxModelId);
		deletePredictionsSimple(minModelId,maxModelId);
	
	}
	
	void vacuum(String schema, String tableName) {
		
		String sql="VACUUM (ANALYZE, VERBOSE, FULL) "+schema+"."+tableName+";";
		SqlUtilities.runSQLUpdate(SqlUtilities.getConnectionPostgres(), sql);
		
	}
	
	
	public void deleteAllRecords(String sourceName) {
		
		String sql="select id from qsar_models.sources where name='"+sourceName+"';";
		
		System.out.println(sql);
		
		int fk_source_id=Integer.parseInt(SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql));

		System.out.println("Source id="+fk_source_id);
		
		deletePredictionsDashboard(fk_source_id);
		
//		String [] tableNames= {"predictions_dashboard","prediction_reports","qsar_predicted_ad_estimates","qsar_predicted_neighbors"};
//		for (String tableName:tableNames) vacuum( "qsar_models", tableName);

		//		deleteRecords("qsar_predicted_neighbors",id);
		//		deleteRecords("qsar_predicted_ad_estimates",1);
		//		deleteRecords("prediction_reports",1);
		//		deleteOPERA_Predictions();
	
//		deleteRecordsSimple("qsar_predicted_neighbors",minModelId,maxModelId);
//		deleteRecordsSimple("qsar_predicted_ad_estimates",minModelId,maxModelId);
//		deleteRecordsSimple("prediction_reports",minModelId,maxModelId);
//		deletePredictionsSimple(minModelId,maxModelId);
	
	}

	
	/**
	 * Gets unique values of PredictionDashboard keys (see PredictionDashboard.getKey())
	 * 
	 * @param source
	 * @param snapshot
	 * @return
	 */
	public static HashSet<String>getLoadedKeys(Source source,DsstoxSnapshot snapshot) {
		
		int limit=50000;
		HashSet<String>values=new HashSet<>();
		
		try {
			Connection conn=gov.epa.run_from_java.scripts.SqlUtilities.getConnectionPostgres();
						
			int i=0;
			
			while (true) {
				
				String sql="select pd.canon_qsar_smiles, dr.id, m.id from qsar_models.predictions_dashboard pd\r\n";
				sql+="join qsar_models.models m on m.id=pd.fk_model_id\r\n";
				sql+="join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id\r\n";
//				sql+="where m.fk_source_id="+source.getId()+" and dr.fk_dsstox_snapshot_id="+snapshot.getId()+"\r\n";
				sql+="where m.fk_source_id="+source.getId()+"\r\n";

//				String sql="select pd.canon_qsar_smiles, dr.id, pd.fk_model_id\r\n"
//						+ "from qsar_models.predictions_dashboard pd\r\n"
//						+ "join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id\r\n"
//						+ "where fk_model_id in (1161,1162,1163,1164,1165,1166,1167,1168,1169,1170,1171,1172,1173,\r\n"
//						+ "                      1174,1175,1176,1177,1178,1179,1180,1181,1182,1183,1184,1491,\r\n"
//						+ "                      1492,1493,1494)\n";
				
				
				sql+="limit "+limit+" offset "+limit*i+";";
//				System.out.println(sql);
				
				long t1=System.currentTimeMillis();
				
				ResultSet rs=SqlUtilities.runSQL2(conn , sql);	
				int count=0;
				
				long t2=System.currentTimeMillis();
				while (rs.next()) {
					values.add(rs.getString(1)+"\t"+rs.getLong(2)+"\t"+rs.getLong(3));
					count++;
				}
				long t3=System.currentTimeMillis();
				
				System.out.println("Time to get result set:"+(t2-t1));
				System.out.println("Time to iterate result set:"+(t3-t2));
				
				if(count==0) break;
				
				System.out.println(values.size());
				
				i++;
			}
			
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return values;
		
	}
	
	public static HashSet<String>getLoadedKeys2(Source source,DsstoxSnapshot snapshot) {

		HashSet<String>values=new HashSet<>();

		try {
			Connection conn=gov.epa.run_from_java.scripts.SqlUtilities.getConnectionPostgres();

			String sql="select pd.canon_qsar_smiles, dr.id, m.id from qsar_models.predictions_dashboard pd\r\n";
			sql+="join qsar_models.models m on m.id=pd.fk_model_id\r\n";
			sql+="join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id\r\n";
			//				sql+="where m.fk_source_id="+source.getId()+" and dr.fk_dsstox_snapshot_id="+snapshot.getId()+"\r\n";
			sql+="where m.fk_source_id="+source.getId()+"\r\n";

			long t1=System.currentTimeMillis();
			ResultSet rs=SqlUtilities.runSQL2(conn , sql);	
			long t2=System.currentTimeMillis();
			System.out.println("Time to get result set:"+(t2-t1));
			
			while (rs.next()) {
				values.add(rs.getString(1)+"\t"+rs.getLong(2)+"\t"+rs.getLong(3));
			}
			long t3=System.currentTimeMillis();

			System.out.println("Time to iterate result set:"+(t3-t2));
			System.out.println(values.size());

		} catch(Exception ex) {
			ex.printStackTrace();
		}

		return values;

	}
	
	public static HashSet<String>getLoadedKeys(String filepath) {

		HashSet<String>values=new HashSet<>();

		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			br.readLine();
			
			while (true) {
				String Line=br.readLine();
				if(Line==null)break;
				values.add(Line);
			}
			br.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return values;

	}
	
	/**
	 * Delete with single query 
	 * In child tables should have cascade on delete for fk_predictions_dashboard foreign settings in datagrip
	 * Then it will delete records in prediction_reports, qsar_predicted_neighbors, qsar_predicted_ad_estimates
	 */
	void deletePredictionsDashboard(int fk_source_id) {
		System.out.print("Deleting from predictions_dashboard");
	
		String sql="delete from qsar_models.predictions_dashboard pd using qsar_models.models m\n"+
				"where pd.fk_model_id = m.id and m.fk_source_id="+fk_source_id+";";
		SqlUtilities.runSQLUpdate(SqlUtilities.getConnectionPostgres(), sql);
		
	}

	public void deletePredictionsSimple(long minModelNumber,long maxModelNumber) {
		long t1=System.currentTimeMillis();
		System.out.print("Deleting from predictions_dashboard...");
	
		String sql="delete from qsar_models.predictions_dashboard pd\n"+
				"where pd.fk_model_id >="+minModelNumber+" and pd.fk_model_id <="+maxModelNumber+";";
	
		SqlUtilities.runSQLUpdate(SqlUtilities.getConnectionPostgres(), sql);
	
		long t2=System.currentTimeMillis();
		System.out.println("Done in "+(t2-t1)/1000.0+" seconds");
	
	}

	/**
	 * Instead of complicated delete sql, find the records one by one and delete them
	 */
	public void deleteRecords(String table, int fk_source_id) {
		System.out.print("Deleting from "+table);
		String sql="select pr.id from qsar_models."+table+" pr\n"+
				"join qsar_models.predictions_dashboard pd on pr.fk_predictions_dashboard_id = pd.id\n"+
				"join qsar_models.models m on pd.fk_model_id = m.id\n"+
				"where m.fk_source_id="+fk_source_id+";";//TODO maybe add join to sources table 
	
		System.out.print("\n"+sql+"\n");
	
		try {
	
			Connection conn=SqlUtilities.getConnectionPostgres();
	
	
			ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
	
			while (rs.next()) {
				String id=rs.getString(1);
				sql="Delete from qsar_models."+table+" where id="+id+";";
				SqlUtilities.runSQLUpdate(SqlUtilities.getConnectionPostgres(), sql);
				System.out.println(id);
			}
	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Done");
	
	}
	
	/**
	 * Gets report from cached json report in prediction_reports table
	 * 
	 * @param id
	 * @param modelName
	 * @param lookups 
	 * @return
	 */
	public static String getPredictionReport(String id,String modelName,Long dsstox_records_id) {
		
		Connection conn=SqlUtilities.getConnectionPostgres();
		
		String idCol="dtxcid";
		if (id.contains("SID")) idCol="dtxsid";
		
				
		String sql="select file_json from qsar_models.prediction_reports pr\r\n"
				+ "join qsar_models.predictions_dashboard pd on pr.fk_predictions_dashboard_id = pd.id\r\n"
				+ "join qsar_models.models m on pd.fk_model_id = m.id\r\n"
				+ "where pd.fk_dsstox_records_id='"+dsstox_records_id+"' and m.name='"+modelName+"';";
				
//		System.out.println(sql);
		
		
		try {
			ResultSet rs=SqlUtilities.runSQL2(conn, sql);

			if (rs.next()) {
				String json=new String(rs.getBytes(1));
				return json;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}

	public void deleteRecords(String table, String sourceName) {
		System.out.print("Deleting from "+table);
		String sql="select pr.id from qsar_models."+table+" pr\n"+
				"join qsar_models.predictions_dashboard pd on pr.fk_predictions_dashboard_id = pd.id\n"+
				"join qsar_models.models m on pd.fk_model_id = m.id\n"+
				"join qsar_models.sources s on m.fk_source_id = s.id\n"+
				"where s.name='"+sourceName+"';";//TODO maybe add join to sources table 
	
		System.out.print("\n"+sql+"\n");
	
		//		if(true)return;
	
		try {
	
			Connection conn=SqlUtilities.getConnectionPostgres();
	
	
			ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
	
			while (rs.next()) {
				String id=rs.getString(1);
				sql="Delete from qsar_models."+table+" where id="+id+";";
	
				System.out.println(sql);
	
				SqlUtilities.runSQLUpdate(SqlUtilities.getConnectionPostgres(), sql);
				System.out.println(id);
			}
	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Done");
	
	}
	
	//	private Double getCategoryEPA(String strvalue) {
	//
	//		if(strvalue.equals("501-5000")) return 3.0;
	//				
	//		double value=Double.parseDouble(strvalue);
	//		if (value<=50) return 1.0;
	//		else if (value<=500) return 2.0;
	//		else if(value<=5000) return 3.0;
	//		else if(value>5000) return 4.0;
	//		else return null;
	//	}
	//	
	//	private Double getCategoryGHS(String strvalue) {
	//		double value=Double.parseDouble(strvalue);
	//		if (value<=5) return 1.0;
	//		else if (value<=50) return 2.0;
	//		else if(value<=300) return 3.0;
	//		else if(value<=2000) return 4.0;
	//		else if(value>2000) return 5.0;
	//		else return null;
	//	}
	
	
	
	@Deprecated
	public static HashSet<String> getPredictionsDashboardKeysInDB(long minModelId,long maxModelId)  {
	
		try {
	
			HashSet<String> pd_keys=new HashSet<>();
	
			String sql="select canon_qsar_smiles, fk_dsstox_records_id, fk_model_id from qsar_models.predictions_dashboard pd\n"+
					"where fk_model_id>="+minModelId+" and fk_model_id<="+maxModelId+";";
	
			ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
	
			while (rs.next()) {
				String canon_qsar_smiles=rs.getString(1);
				Long fk_dsstox_records_id=rs.getLong(2);
				String fk_model_id=rs.getString(3);
				String key=canon_qsar_smiles+"\t"+fk_dsstox_records_id+"\t"+fk_model_id;
				//				System.out.println(key);
				pd_keys.add(key);
			}
	
			//			System.out.println("Got keys for test predictions in predictions dashboard:"+pd_keys.size());
	
			return pd_keys;
	
		} catch (Exception ex) {
			return null;
		}
	}



	public static void main(String[] args) {
		DatabaseUtilities d=new DatabaseUtilities();
//		d.deleteAllRecords("Percepta2020.2.1");
//		d.deleteAllRecords("Percepta2023.1.2");
		
	}



	public static HashSet<String> getLoadedCIDs(Source source, DsstoxSnapshot snapshot) {
		HashSet<String>values=new HashSet<>();
		
		String sql="select distinct (dr.dtxcid)\r\n"
				+ "from qsar_models.predictions_dashboard pd\r\n"
				+ "join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id\r\n"
				+ "join qsar_models.models m on m.id=pd.fk_model_id\r\n"
				+ "where m.fk_source_id="+source.getId()+" and dr.fk_dsstox_snapshot_id="+snapshot.getId()+";";
				
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres() , sql);
		
		try {
			while (rs.next()) {
				values.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return values;
	}

}
