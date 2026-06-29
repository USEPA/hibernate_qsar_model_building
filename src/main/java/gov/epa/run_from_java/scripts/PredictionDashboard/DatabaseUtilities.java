package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxSnapshot;
import gov.epa.databases.dev_qsar.qsar_models.entity.Source;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.util.JsonUtilities;
import gov.epa.util.StructureUtil;
import gov.epa.util.StructureUtil.APIMolecule;

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
				
				System.out.println("Time to get result set:"+(t2-t1)/1000.0);
				System.out.println("Time to iterate result set:"+(t3-t2)/1000.0);
				
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
	
	public static HashSet<String>getLoadedKeysFromFile(String filepath) {

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
	public static String getJsonPredictionReport(String id,String modelName,Long dsstox_records_id) {
		String reportColumn="file_json";
		return getReportAsString(modelName, dsstox_records_id, reportColumn);
	}



	private static String getReportAsString(String modelName, Long dsstox_records_id, String reportColumn) {
		
		
		Connection conn=SqlUtilities.getConnectionPostgres();

		String sql="select "+reportColumn+" from qsar_models.prediction_reports pr\r\n"
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
	
	/**
	 * Gets report from cached json report in prediction_reports table
	 * 
	 * @param id
	 * @param modelName
	 * @param lookups 
	 * @return
	 */
	public static String getHtmlPredictionReport(String id,String modelName,Long dsstox_records_id) {
		String reportColumn="file_html";
		return getReportAsString(modelName, dsstox_records_id, reportColumn);
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


	public List<DsstoxRecord> getUnpredictedBySource(String sourceName)  {

		Connection conn=SqlUtilities.getConnectionPostgres();

		final String sql = """
				SELECT 
				    dr.dtxcid,
				    dr.dtxsid,
				    dr.smiles,
				    dr.created_at
				FROM qsar_models.dsstox_records dr
				WHERE dr.fk_dsstox_snapshot_id = 4
				  AND NOT EXISTS (
				    SELECT 1
				    FROM qsar_models.predictions_dashboard pd
				    JOIN qsar_models.models m
				      ON m.id = pd.fk_model_id
				    JOIN qsar_models.sources s
				      ON s.id = m.fk_source_id
				    WHERE s.name = ?
				      AND pd.dtxcid = dr.dtxcid
				  )
				""";

		try {

			List<DsstoxRecord> results = new ArrayList<>();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, sourceName);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						DsstoxRecord dr= new DsstoxRecord();
						dr.setDtxcid(rs.getString("dtxcid"));
						dr.setDtxsid(rs.getString("dtxsid"));
						dr.setSmiles(rs.getString("smiles"));
						dr.setCreatedAt(rs.getTimestamp("created_at"));
						results.add(dr);
					}
				}
			}
			return results;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	
	public long getCountUnpredictedBySource(String sourceName)  {
	    final String sql = """
	        SELECT COUNT(*) AS missing_count
	        FROM qsar_models.dsstox_records dr
	        WHERE dr.fk_dsstox_snapshot_id = 4
	          AND NOT EXISTS (
	            SELECT 1
	            FROM qsar_models.predictions_dashboard pd
	            JOIN qsar_models.models m ON m.id = pd.fk_model_id
	            WHERE m.fk_source_id = (
	              SELECT id
	              FROM qsar_models.sources
	              WHERE name = ?
	            )
	            AND pd.dtxcid = dr.dtxcid
	          )
	        """;

	    try (PreparedStatement ps = SqlUtilities.getConnectionPostgres().prepareStatement(sql)) {
	        ps.setString(1, sourceName);
	        try (ResultSet rs = ps.executeQuery()) {
	            return rs.next() ? rs.getLong(1) : 0L;
	        }
	    } catch (Exception ex) {
	    	ex.printStackTrace();
	    	return 0;
	    }
	}

	
	void lookForMissingPredictionsInOperaOutputFiles() {
		
		String source="OPERA2.8";
		List<DsstoxRecord>drs = getUnpredictedBySource(source);
		Hashtable<String,DsstoxRecord>htDR_by_SID=new Hashtable<>();
		
		int count=0;
		for (DsstoxRecord dr:drs) {
			if (dr.getSmiles()==null) continue;
			dr.setOtherCasrns(null);
			htDR_by_SID.put(dr.getDtxsid(), dr);
			if (dr.getSmiles().contains(".")) continue;
			count++;
//			System.out.println(JsonUtilities.gson.toJson(dr));
		}
		
		String folderSDF="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\dsstox_2025_12_31_missing_opera2.8_predictions";
		HashSet<String>mySIDs=new HashSet<>();
		for (File file:new File(folderSDF).listFiles()) {
			if (!file.getName().contains(".sdf")) continue;
			List<APIMolecule>mols=StructureUtil.readSDF_to_API_Molecules(file.getAbsolutePath(), -1);
			for (APIMolecule mol:mols) {
//				System.out.println(file.getName()+"\t"+ mol.htProperties.get("DTXSID"));
				mySIDs.add((String)mol.htProperties.get("DTXSID"));
			}
		}
		
		
				
		String folderSDF_Kamel="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\difference with 12-31-25 snapshot";
		HashSet<String>kamelSIDs_SDF=new HashSet<>();
		HashSet<String>kamelSIDs_Pred=new HashSet<>();
		for (File file:new File(folderSDF_Kamel).listFiles()) {
			if (file.getName().contains(".sdf")) {
				List<APIMolecule>mols=StructureUtil.readSDF_to_API_Molecules(file.getAbsolutePath(), -1);
				for (APIMolecule mol:mols) {
//					System.out.println(file.getName()+"\t"+ mol.htProperties.get("DTXSID"));
					kamelSIDs_SDF.add((String)mol.htProperties.get("DTXSID"));
				}
				
			} else if (file.getName().contains("pred_")) {
				try {
					BufferedReader br=new BufferedReader(new FileReader(file));
					while(true ) {
						String Line=br.readLine();
						if(Line==null) break;
						String dtxsid=Line.substring(0,Line.indexOf(","));
						kamelSIDs_Pred.add(dtxsid);
					}
					
					br.close();
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		
		String dtxsidMissing="DTXSID50161110";
		System.out.println("mySIDs.contains="+mySIDs.contains(dtxsidMissing));
		System.out.println("kamelSIDs_SDF.contains="+kamelSIDs_SDF.contains(dtxsidMissing));
		System.out.println("kamelSIDs_pred.contains="+kamelSIDs_Pred.contains(dtxsidMissing));
		System.out.println("htDR_by_SID.contains="+htDR_by_SID.containsKey(dtxsidMissing));

		for (String dtxsid:htDR_by_SID.keySet()) {
			
			if(!kamelSIDs_Pred.contains(dtxsid) && !kamelSIDs_SDF.contains(dtxsid))			
				System.out.println(dtxsid+"\t"+kamelSIDs_SDF.contains(dtxsid)+"\t"+kamelSIDs_Pred.contains(dtxsid)+"\t"+htDR_by_SID.get(dtxsid).getSmiles());
		}
		
		
	}

	public static void main(String[] args) {
		
		DatabaseUtilities d=new DatabaseUtilities();
//		d.deleteAllRecords("Percepta2020.2.1");
//		d.deleteAllRecords("Percepta2023.1.2");
		
		List<String>sources=Arrays.asList("OPERA2.8", "TEST5.1.3","Percepta2025.1.4");
		
//		for (String source:sources) {
//			long count = d.getCountUnpredictedBySource(source);
//			System.out.println(source+" "+count);
//		}
		
		d.lookForMissingPredictionsInOperaOutputFiles();

		
		
//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\difference with 12-31-25 snapshot\\";
//		String filepathKeys = folder+"loaded_keys.tsv"; 
//		DatabaseUtilities.dumpLoadedKeysForSourceToFile("OPERA2.8", filepathKeys);
//		HashSet<String>keys = DatabaseUtilities.loadLinesIntoHashSet(filepathKeys);
//		System.out.println(keys.size()/28);
		
		
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
	
	@Deprecated
	public static HashSet<String> getLoadedCIDsWithCountOld(Source source, DsstoxSnapshot snapshot,int count) {
		HashSet<String>values=new HashSet<>();
		
		String sql="select dr.dtxcid\r\n"
				+ "from qsar_models.predictions_dashboard pd\r\n"
				+ "join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id\r\n"
				+ "join qsar_models.models m on m.id=pd.fk_model_id\r\n"
				+ "where m.fk_source_id="+source.getId()+" and dr.fk_dsstox_snapshot_id="+snapshot.getId()+"\n"+
				"group by dr.dtxcid\n"+
				"having count(dr.dtxcid)="+count+";";
				
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
	
	
	public static HashSet<String> getLoadedCIDsWithCount(String sourceName, int count) {
		HashSet<String>values=new HashSet<>();
		
		String sql="select pd.dtxcid\r\n"
				+ "from qsar_models.predictions_dashboard pd\r\n"
				+ "join qsar_models.models m on m.id=pd.fk_model_id\r\n"
				+ "join qsar_models.sources s on s.id=m.fk_source_id\r\n"
				+ "where s.name='"+sourceName+"'\n"+
				"group by pd.dtxcid\n"+
				"having count(pd.dtxcid)="+count+";";
//		System.out.println(sql);
		
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
	
	public static HashSet<String> getLoadedCIDsWithCount2(String sourceName, int count) {
		HashSet<String>values=new HashSet<>();
		
		String sql = null;

		if (count<=0) {
			sql = """
				   WITH ms AS (
					  SELECT m.id
					  FROM qsar_models.models m
					  JOIN qsar_models.sources s ON s.id = m.fk_source_id
					  WHERE s.name = 'OPERA2.8'
					)
					SELECT DISTINCT pd.dtxcid
					FROM qsar_models.predictions_dashboard pd
					JOIN ms ON ms.id = pd.fk_model_id;
					""";
		} else {
			sql = """
				    WITH ms AS (
				      SELECT m.id
				      FROM qsar_models.models m
				      JOIN qsar_models.sources s ON s.id = m.fk_source_id
				      WHERE s.name = 'OPERA2.8'
				    )
				    SELECT pd.dtxcid
				    FROM qsar_models.predictions_dashboard pd
				    JOIN ms ON ms.id = pd.fk_model_id
				    GROUP BY pd.dtxcid
				    """;
			sql+="\nHAVING COUNT(DISTINCT pd.fk_model_id) >= "+count+";";
		}
		
//		System.out.println(sql);
		
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
	
	
	public static HashSet<String> getLoadedKeysForSource(String sourceName) {
		HashSet<String> values = new HashSet<>();

		String sql = """
				WITH ms AS (
				  SELECT m.id
				  FROM qsar_models.models m
				  JOIN qsar_models.sources s ON s.id = m.fk_source_id
				  WHERE s.name = ?
				)
				SELECT pd.canon_qsar_smiles, pd.dtxcid, pd.fk_model_id
				FROM qsar_models.predictions_dashboard pd
				JOIN ms ON ms.id = pd.fk_model_id;
				""";

		System.out.println("Getting keys in db for " + sourceName);

		try (java.sql.Connection conn = SqlUtilities.getConnectionPostgres();
				java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, sourceName);
			try (java.sql.ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					values.add(rs.getString(1) + "\t" + rs.getString(2) + "\t" + rs.getLong(3));
				}
			}
			System.out.println(values.size() + " keys in db for " + sourceName);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return values;
	}
	
	
	
	public static HashSet<String> loadLinesIntoHashSet(String path) {
	    
	    System.out.println("Loading pd_keys from file");

		HashSet<String> set = new HashSet<>();
	    try (BufferedReader br = Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8)) {
	        String line;
	        while ((line = br.readLine()) != null) {
	            String s = line.strip();
	            if (s.isEmpty()) continue;           // skip empty lines
	            // if (s.startsWith("#")) continue;  // uncomment to skip comments
	            set.add(s);
	        }
	        
	    } catch (Exception ex) {
	    	ex.printStackTrace();
	    }
	    return set;
	}
	
	
	
	public static void dumpLoadedKeysForSourceToFile(String sourceName, String outFilePath) {
	    String sql = """
	        WITH ms AS (
	          SELECT m.id
	          FROM qsar_models.models m
	          JOIN qsar_models.sources s ON s.id = m.fk_source_id
	          WHERE s.name = ?
	        )
	        SELECT pd.canon_qsar_smiles, pd.dtxcid, pd.fk_model_id
	        FROM qsar_models.predictions_dashboard pd
	        JOIN ms ON ms.id = pd.fk_model_id
	        """;

	    
	    int count=0;
	    
	    System.out.println("Dumping pd_keys to file");
	    
	    try (java.sql.Connection conn = SqlUtilities.getConnectionPostgres()) {
	        conn.setAutoCommit(false); // required for cursor-based fetch on PG
	        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
	            ps.setFetchSize(1000);   // tune as appropriate
	            ps.setString(1, sourceName);

	            try (java.sql.ResultSet rs = ps.executeQuery();
	                 BufferedWriter w = Files.newBufferedWriter(Path.of(outFilePath))) {

	                while (rs.next()) {
	                    String canon = rs.getString(1);
	                    String dtxcid = rs.getString(2);
	                    long modelId = rs.getLong(3);
	                    w.write(canon);
	                    w.write('\t');
	                    w.write(dtxcid);
	                    w.write('\t');
	                    w.write(Long.toString(modelId));
	                    w.newLine();
	                    count++;
	                    
	                    if(count%100000==0) {
	                    	System.out.println(count);
	                    }
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	}
	
	
	public static HashSet<String> getLoadedCIDs(String sourceName, String countCriterion) {
		HashSet<String>values=new HashSet<>();
		
		String sql="select pd.dtxcid\r\n"
				+ "from qsar_models.predictions_dashboard pd\r\n"
				+ "join qsar_models.models m on m.id=pd.fk_model_id\r\n"
				+ "join qsar_models.sources s on s.id=m.fk_source_id\r\n"
				+ "where s.name='"+sourceName+"'\n"+
				"group by pd.dtxcid\n"+
				"having count(pd.dtxcid)"+countCriterion+";";
				
//		System.out.println(sql);
		
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
