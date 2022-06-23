package gov.epa.run_from_java.scripts;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gov.epa.util.wekalite.CSVLoader;
import gov.epa.util.wekalite.Instances;


public class GetExpPropInfo {
	
	static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	static String createQuery1(String smiles, int fk_dataset_id) {
		String sql="select dp.canon_qsar_smiles,dp.qsar_property_value, dpc.exp_prop_id\n"; 
		sql+="from qsar_datasets.data_points dp\n";  
		sql+="inner join qsar_datasets.data_point_contributors dpc\n";
		sql+="on dp.id =dpc.fk_data_point_id\n";
		sql+="where dp.canon_qsar_smiles ='"+smiles+"' and dp.fk_dataset_id ="+fk_dataset_id;
		return sql;
	}

//	static String createQuery2(int id) {
//		String sql="select * from exp_prop.property_values\n";  
//		sql+="where id="+id;
//		return sql;
//	}
	
	static String lookupUnits(int id, Connection conn) {
		String sql="select \"name\" from exp_prop.units u \r\n"
				+ "where u.id="+id;
		return runSQL(conn, sql);
	}
	
	static String lookupDTXCID(String smiles, Connection conn) {
		String sql="select dtxcid from qsar_descriptors.compounds t \r\n"
				+ "where t.canon_qsar_smiles='"+smiles+"';";
		return runSQL(conn, sql);
	}

	static String lookupQSAR_units(int id, Connection conn) {
		//TODO change to inner join instead of 2 queries
		
		String sql="select fk_unit_id from qsar_datasets.datasets t \r\n"
				+ "where t.id='"+id+"';";
		String fk_units_id=runSQL(conn, sql);

		
		sql="select name from qsar_datasets.units t \r\n"
				+ "where t.id='"+fk_units_id+"';";
		String units=runSQL(conn, sql);

//		System.out.println(units);
		return units;
		
	}

	
	private static String runSQL(Connection conn, String sql) {
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
		return "";
	}
	
	
	static String lookupMolWt(String dtxcid, Connection connDSSTOX) {
		String sql="select mol_weight from compounds t \r\n"
				+ "where t.dsstox_compound_id='"+dtxcid+"';";
		
//		System.out.println(sql);
		
		return runSQL(connDSSTOX, sql);
	}
	
	static String getAllValues(int id, String table, Connection conn,JsonObject jo) {
		String sql="select * from "+table+" t \r\n"
				+ "where t.id="+id;
				
		try {
			Statement st = conn.createStatement();
			
			ResultSet rs = st.executeQuery(sql);
			
			if (rs.next()) {
				ResultSetMetaData rsMetaData = rs.getMetaData();
				
				int count = rsMetaData.getColumnCount();
				for (Integer i = 1; i <= count; i++) {
//			       	System.out.println(rsMetaData.getColumnName(i));					
					jo.addProperty(rsMetaData.getColumnName(i), rs.getString(i));				
				}				
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	static Connection getConnection() {
		String host = System.getenv().get("DEV_QSAR_HOST");
		String port = System.getenv().get("DEV_QSAR_PORT");
		String db = System.getenv().get("DEV_QSAR_DATABASE");

		String url = "jdbc:postgresql://" + host + ":" + port + "/" + db;
		String user = System.getenv().get("DEV_QSAR_USER");
		String password = System.getenv().get("DEV_QSAR_PASS");

		try {
			Connection conn = DriverManager.getConnection(url, user, password);
			return conn;
		} catch (Exception ex) {
			return null;
		}
	}
	
	static Connection getConnectionDSSTOX() {
		String host = System.getenv().get("DSSTOX_HOST");
		String port = System.getenv().get("DSSTOX_PORT");
		String db = System.getenv().get("DSSTOX_DATABASE");

		String url = "jdbc:mysql://" + host + ":" + port + "/" + db;
		String user = System.getenv().get("DSSTOX_USER");
		String password = System.getenv().get("DSSTOX_PASS");

		try {
			Connection conn = DriverManager.getConnection(url, user, password);
			return conn;
		} catch (Exception ex) {
			return null;
		}
	}

	
	
	static void getDataForSmiles(Connection conn, Connection connDSSTOX, String smiles,int id_dataset, JsonArray ja) {

		try {
			
			Statement st = conn.createStatement();
			String sql = createQuery1(smiles, id_dataset);

			ResultSet rs = st.executeQuery(sql);
			
			JsonObject jo=null;
			JsonArray jaRecords=null;
						
			
			while (rs.next()) {

				if (jo==null) {
					jo=new JsonObject();
					ja.add(jo);
					
					jo.addProperty("canon_qsar_smiles",rs.getString(1));
					jo.addProperty("qsar_property_value",rs.getString(2));

					String qsar_units=lookupQSAR_units(id_dataset, conn);
					jo.addProperty("qsar_property_units", qsar_units);

					String dtxcid=lookupDTXCID(rs.getString(1), conn);
					jo.addProperty("dtxcid",dtxcid);
					
					String mol_weight=lookupMolWt(dtxcid, connDSSTOX);
					jo.addProperty("mol_weight",mol_weight);

					jaRecords=new JsonArray();
					jo.add("Records", jaRecords);
				}
				
				
				JsonObject joRecord=new JsonObject();
				
				String str_exp_prop_id = rs.getString(3);
				
				Integer exp_prop_id=Integer.parseInt(str_exp_prop_id.replace("EXP", ""));
				joRecord.addProperty("exp_prop_id",exp_prop_id);
				getAllValues(exp_prop_id, "exp_prop.property_values",conn, joRecord);

				
				joRecord.addProperty("units",lookupUnits(joRecord.get("fk_unit_id").getAsInt(), conn));


				if (!joRecord.get("fk_literature_source_id").isJsonNull()) {
					int id=joRecord.get("fk_literature_source_id").getAsInt();
					getAllValues(id,"exp_prop.literature_sources", conn, joRecord);
				}
					
				if (!joRecord.get("fk_public_source_id").isJsonNull()) {
					int id=joRecord.get("fk_public_source_id").getAsInt();
					getAllValues(id,"exp_prop.public_sources", conn, joRecord);
				}
					
				int id=joRecord.get("fk_source_chemical_id").getAsInt();
				getAllValues(id,"exp_prop.source_chemicals", conn, joRecord);

				joRecord.remove("access_date");
				joRecord.remove("created_at");
				joRecord.remove("updated_at");
				joRecord.remove("created_by");
				joRecord.remove("keep");
//				
				joRecord.remove("id");
				joRecord.remove("fk_unit_id");				
				joRecord.remove("fk_property_id");

//				jo.remove("exp_prop_id");				
//				jo.remove("fk_source_chemical_id");
//				jo.remove("fk_public_source_id");
//				jo.remove("fk_literature_source_id");
				
				jaRecords.add(joRecord);     			
			}
			rs.close();


		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		}
	}

	public static void main(String[] args) {
		
		Connection conn=getConnection();
		Connection connDSSTOX=getConnectionDSSTOX();
		String folder="data\\dev_qsar\\dataset_files\\";
		
//		String inputFileName="Standard Water solubility from exp_prop_T.E.S.T. 5.1_T=PFAS only, P=PFAS_prediction.tsv";
//		String outputFileName="Water solubility PFAS prediction records.json";
		
		String inputFileName="Standard Water solubility from exp_prop_T.E.S.T. 5.1_T=PFAS only, P=PFAS_training.tsv";
		String outputFileName="Water solubility PFAS training records.json";
		
		
		getRecords(conn, connDSSTOX, folder, inputFileName,outputFileName);
	}

	private static void getRecords(Connection conn, Connection connDSSTOX, String folder, String inputFileName,String outputFileName) {
		CSVLoader c=new CSVLoader();
		try {
			Instances instances=c.getDataSetFromFile(folder+inputFileName,"\t");
			JsonArray ja=new JsonArray();
			
			int id_dataset=31;
			
			for (int i=0;i<instances.numInstances();i++) {
				System.out.println(i);
				String smiles=instances.instance(i).getName();
				getDataForSmiles(conn, connDSSTOX, smiles,id_dataset,ja);
				
			}
			
			FileWriter fw=new FileWriter(folder+outputFileName);			
			fw.write(gson.toJson(ja));
			fw.flush();
			fw.close();
			
//			System.out.println(ja.size());
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
