package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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

		System.out.println(fk_source_id);
		
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
	
	public static void main(String[] args) {
		DatabaseUtilities d=new DatabaseUtilities();
//		d.deleteAllRecords("Percepta2020.2.1");
		d.deleteAllRecords("Percepta2023.1.2");
		
	}

}
