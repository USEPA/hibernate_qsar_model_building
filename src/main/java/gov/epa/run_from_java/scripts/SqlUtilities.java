package gov.epa.run_from_java.scripts;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.run_from_java.scripts.RecalcStatsScript.SplitPredictions;

public class SqlUtilities {
	
	private static Map<String, Connection> connPool = new HashMap<>();
	
	public static final String dbPostGres="Postgres";
	public static final String dbDSSTOX="DSSTOX";
	public static final String dbToxVal93="dbToxVal93";
	
	/**
	 * Gives you connection to Postgres database based on the environment variables
	 * connPool allows the next call to be instantaneous
	 * 
	 * @return
	 */
	public static Connection getConnectionPostgres() {
		
		try {
			if (connPool.containsKey(dbPostGres) && connPool.get(dbPostGres) != null && !connPool.get(dbPostGres).isClosed()) {
//				System.out.println("have active conn");
				return connPool.get(dbPostGres);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String host = System.getenv().get("DEV_QSAR_HOST");
		String port = System.getenv().get("DEV_QSAR_PORT");
		String db = System.getenv().get("DEV_QSAR_DATABASE");

		String url = "jdbc:postgresql://" + host + ":" + port + "/" + db;
		String user = System.getenv().get("DEV_QSAR_USER");
		String password = System.getenv().get("DEV_QSAR_PASS");

		try {
			Connection conn = DriverManager.getConnection(url, user, password);
			connPool.put(dbPostGres, conn);
			return conn;
		} catch (Exception ex) {
			return null;
		}
	}

	public static Connection getConnectionDSSTOX() {
		
		try {
			if (connPool.containsKey(dbDSSTOX) && connPool.get(dbDSSTOX) != null && !connPool.get(dbDSSTOX).isClosed()) {
				return connPool.get(dbDSSTOX);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		String host = System.getenv().get("DSSTOX_HOST");
		String port = System.getenv().get("DSSTOX_PORT");
		String db = System.getenv().get("DSSTOX_DATABASE");

		String url = "jdbc:mysql://" + host + ":" + port + "/" + db;
		String user = System.getenv().get("DSSTOX_USER");
		String password = System.getenv().get("DSSTOX_PASS");

		try {
			Connection conn = DriverManager.getConnection(url, user, password);
			
			connPool.put(dbDSSTOX, conn);
			
			return conn;
		} catch (Exception ex) {
			return null;
		}
	}
	
	
public static Connection getConnectionToxValV93() {
		
		try {
			if (connPool.containsKey(dbDSSTOX) && connPool.get(dbDSSTOX) != null && !connPool.get(dbDSSTOX).isClosed()) {
				return connPool.get(dbDSSTOX);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		String host = System.getenv().get("DSSTOX_HOST");
		String port = System.getenv().get("DSSTOX_PORT");
		String db = "prod_toxval_v93";

		String url = "jdbc:mysql://" + host + ":" + port + "/" + db;
		String user = System.getenv().get("DSSTOX_USER");
		String password = System.getenv().get("DSSTOX_PASS");

		try {
			Connection conn = DriverManager.getConnection(url, user, password);
			
			connPool.put(dbToxVal93, conn);
			
			return conn;
		} catch (Exception ex) {
			return null;
		}
	}
	
	/**
	 * Lets you look up splitNum from smiles for the given splittingName and datasetName
	 * @param datasetName
	 * @param splittingName
	 * @return
	 */
	public static Hashtable<String, Integer> getHashtableSplitNum(String datasetName, String splittingName) {
		Hashtable<String,Integer>htSplitNum=new Hashtable<>();

		String sql="select dp.canon_qsar_smiles ,dpis.split_num  from qsar_datasets.data_points_in_splittings dpis\n"+
		"join qsar_datasets.splittings s on s.id=dpis.fk_splitting_id\n"+
		"join qsar_datasets.data_points dp on dp.id=dpis.fk_data_point_id\n"+
		"join qsar_datasets.datasets d on d.id=dp.fk_dataset_id\n"+
		"where s.\"name\" ='"+splittingName+"' and d.\"name\" ='"+datasetName+"';";

		
		ResultSet rs=runSQL2(getConnectionPostgres(), sql);
		
		try {
			while (rs.next()) {				
				String ID=rs.getString(1);
				Integer splitNum=Integer.parseInt(rs.getString(2));
				htSplitNum.put(ID, splitNum);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return htSplitNum;
	}
	
//	/**
//	 * Get predicted values for the model's splitting
//	 * @param modelId
//	 * @return
//	 */
//	public static Hashtable<String, Double> getHashtablePredValues(long modelId) {
//		//Get pred values:
//
//		String sql="select p.canon_qsar_smiles,p.qsar_predicted_value, p.fk_splitting_id  from qsar_models.predictions p\n"+ 
//		"join qsar_models.models m on m.id=p.fk_model_id\n"+
//		"join qsar_datasets.splittings s on s.id=p.fk_splitting_id\n"+ 
//		"where fk_model_id="+modelId+" and s.\"name\" =m.splitting_name;";
//
//		Connection conn=getConnectionPostgres();
//		ResultSet rs=DatabaseLookup.runSQL2(conn, sql);
//		Hashtable<String,Double>htPred=new Hashtable<>();
//		
//		try {
//			while (rs.next()) {				
//				String ID=rs.getString(1);
//				Double pred=rs.getDouble(2);
//				int splittingId=rs.getInt(3);//Just for inspection
//				htPred.put(ID, pred);
//			}
//			
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		}
//		return htPred;
//	}
	
	/**
	 * Get predicted values for specified splitting (e.g. CV split)
	 * Lets you look up predicted value from smiles
	 * 
	 * @param modelId
	 * @param splittingName
	 * @return
	 */
	public static Hashtable<String, Double> getHashtablePredValues(long modelId,String splittingName) {
		//Get pred values:

		String sql="select p.canon_qsar_smiles,p.qsar_predicted_value, p.fk_splitting_id  from qsar_models.predictions p\n"+ 
		"join qsar_models.models m on m.id=p.fk_model_id\n"+
		"join qsar_datasets.splittings s on s.id=p.fk_splitting_id\n"+ 
		"where fk_model_id="+modelId+" and s.\"name\"='"+splittingName+"';";
		
//		System.out.println(sql);
		

		ResultSet rs=runSQL2(getConnectionPostgres(), sql);
		Hashtable<String,Double>htPred=new Hashtable<>();
		
		try {
			while (rs.next()) {				
				String ID=rs.getString(1);
				Double pred=rs.getDouble(2);
				int splittingId=rs.getInt(3);//Just for inspection
				htPred.put(ID, pred);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return htPred;
	}
	
	/**
	 * Shortcut to getting SplitPredictions
	 * 
	 * @param model
	 * @param splittingName
	 * @return
	 */
	public static SplitPredictions getSplitPredictionsSql(Model model, String splittingName) {
		return SplitPredictions.getSplitPredictionsSql(model, splittingName);
	}
	
	
	
	/**
	 * Lets you look up experimental value from smiles
	 * 
	 * @param datasetName
	 * @return
	 */
	public static Hashtable<String,Double> getHashtableExp(Dataset dataset) {
		Hashtable<String,Double>htExps=new Hashtable<>();


		String sql="select dp.canon_qsar_smiles ,dp.qsar_property_value  from qsar_datasets.data_points dp\n"+ 
		"join qsar_datasets.datasets d on d.id=dp.fk_dataset_id\n"+ 
		"where dp.fk_dataset_id="+dataset.getId()+";";
		
		ResultSet rs=runSQL2(getConnectionPostgres(), sql);
		
		try {
			while (rs.next()) {				
				String ID=rs.getString(1);
				Double exp=rs.getDouble(2);
				htExps.put(ID, exp);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return htExps;
	}
	
	public static String runSQL(Connection conn, String sql) {
		try {
			Statement st = conn.createStatement();			
			ResultSet rs = st.executeQuery(sql);
			if (rs.next()) {
				return rs.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void runSQLUpdate(Connection conn, String sql) {
		try {
			Statement st = conn.createStatement();			
			st.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public static ResultSet runSQL2(Connection conn, String sql) {
		try {
			Statement st = conn.createStatement();			
			ResultSet rs = st.executeQuery(sql);
			return rs;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}		
	}
	
	
	
	
}