package gov.epa.run_from_java.scripts.PredictionDashboard.OPERA_Old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * Creates a sqlite database from the results zip files
 * 
 * @author TMARTI02
 *
 */
public class SqliteUtilities {
    
    private static Map<String, Connection> connPool = new HashMap<>();
    
	public static Connection getConnection(String databasePath)  {
			try {
				if (connPool.containsKey(databasePath) && connPool.get(databasePath) != null && !connPool.get(databasePath).isClosed()) {
					return connPool.get(databasePath);
				} else {
					Class.forName("org.sqlite.JDBC");
					Connection conn = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
					connPool.put(databasePath, conn); 
					return conn;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
	}
	
	public static void createRecord(ResultSet rs, Object r,String propName1,String propName2) {
		ResultSetMetaData rsmd;
		try {
			rsmd = rs.getMetaData();

			int columnCount = rsmd.getColumnCount();

			// The column count starts from 1
			for (int i = 1; i <= columnCount; i++ ) {
				String name = rsmd.getColumnName(i);
				//				System.out.println(name);								
				String val=rs.getString(i);
				
				if (!name.equals("DSSTOX_COMPOUND_ID")) {
					name=name.replace(propName1, "");
					name=name.replace(propName2, "");
				}

				if (name.substring(0,1).equals("_")) name=name.substring(1,name.length());
				if (name.substring(name.length()-1,name.length()).equals("_")) name=name.substring(0,name.length()-1);

				if (val!=null) {
					Field myField = r.getClass().getDeclaredField(name);			
					myField.set(r, val);
				}

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean createRecord(ResultSet rs, RecordOpera r,List<String>colNames,List<String>colNamesAll,String operaUnits) {

		boolean convertedExpPred=false;

		
		try {
			
			int predCount=0;
			int expCount=0;
			
			
			for (String colName:colNames) {

				int col=colNamesAll.indexOf(colName);
//				System.out.println("*\t"+colName+"\t"+col);
				
				String val=rs.getString(col+1);

				String fieldName="";
				
				if (colName.equals("DSSTOX_COMPOUND_ID")) {
					fieldName=colName;
				} else if (colName.contains("AD_index")) {
					fieldName="AD_index";								
				} else if (colName.contains("Conf_index")) {
					fieldName="Conf_index";
				} else if (colName.contains("CAS_neighbor")) {
					fieldName=colName.substring(colName.indexOf("CAS_neighbor"),colName.length());
				} else if (colName.contains("InChiKey_neighbor")) {
					fieldName=colName.substring(colName.indexOf("InChiKey_neighbor"),colName.length());
				} else if (colName.contains("DSSTOXMPID_neighbor")) {
					fieldName=colName.substring(colName.indexOf("DSSTOXMPID_neighbor"),colName.length());
				} else if (colName.contains("DTXSID_neighbor")) {
					fieldName=colName.substring(colName.indexOf("DTXSID_neighbor"),colName.length());
				} else if (colName.contains("Exp_neighbor")) {
					fieldName=colName.substring(colName.indexOf("Exp_neighbor"),colName.length());
				} else if (colName.contains("pred_neighbor")) {
					fieldName=colName.substring(colName.indexOf("pred_neighbor"),colName.length());
				} else if (colName.contains("AD_")) {
					fieldName="AD";
				} else if  (colName.contains("_exp")) {
					fieldName="exp";
				} else if (colName.contains("_pred")) {
					fieldName="pred";
				} else {
					System.out.println(colName);
//					continue;
				}
				
				if (fieldName.equals("pred")) predCount++;
				if (fieldName.equals("exp")) expCount++;
				
				if (val.isBlank() || val.equals("?")) val=null;
				
				
				if (val!=null) {
					
					if (fieldName.toLowerCase().contains("exp") || fieldName.toLowerCase().contains("pred")) {
						if (operaUnits.toLowerCase().contains("log") && !r.model_name.toLowerCase().contains("log")) {
							
//							System.out.println("converting units to non log for "+colName);
							
							convertedExpPred=true;
							
							try {
								val=Math.pow(10, Double.parseDouble(val))+"";	
							} catch (Exception ex) {
								System.out.println(r.model_name+"\tcant convert value:"+val);
							}
						}
						
					}
					
					Field myField = r.getClass().getDeclaredField(fieldName);			
					myField.set(r, val);
					
//					if (r.propName.equals("LogP")) {
//						System.out.println(colName+"\t"+fieldName+"\t"+val);	
//					}
				}
			}
			
//			if (predCount>1) {
//				System.out.println(r.model_name+"\tpredCount="+predCount);
//				for (String colName:colNames) {
//					System.out.println(colName);
//				}
//			}
//
//			if (expCount>1) {
//				System.out.println(r.model_name+"\texpCount="+expCount);
//			}
						
			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
		return convertedExpPred;
	}
	
	public static Statement getStatement(Connection conn)  {
	    
		try {
			return conn.createStatement();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public static Statement getStatement(String databasePath) {

		try {
			Class.forName("org.sqlite.JDBC");

			// create the db:
			Connection conn = getConnection(databasePath);

//			System.out.println("getting statement for "+databasePath);
			
			Statement stat = conn.createStatement();
			return stat;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	public static ResultSet getRecords(Statement stat,String tableName,String keyField,String keyValue) {

		try {
			String query="select * from "+tableName+" where "+keyField+" = \""+keyValue+"\";";
//			System.out.println(query);
			ResultSet rs = stat.executeQuery(query);
//			ResultSetMetaData rsmd = rs.getMetaData();
			
			return rs;
//			this.printResultSet(rs);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	
	public static String getValueFromSQL(Connection conn,String sql) {
		try {
			Statement sqliteStatement=SqliteUtilities.getStatement(conn);
			ResultSet rs=SqliteUtilities.getRecords(sqliteStatement, sql);
			
			if (rs.next()) {
				return rs.getString(1);
//				System.out.println(n.CID);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();		
		}
		
		return null;
	}
	
	
	public static ResultSet getRecords(Statement stat,String sql) {

		try {
			
//			System.out.println(query);
			ResultSet rs = stat.executeQuery(sql);
//			ResultSetMetaData rsmd = rs.getMetaData();
			
			return rs;
//			this.printResultSet(rs);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}	


}
