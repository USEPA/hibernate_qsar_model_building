package gov.epa.run_from_java.scripts.GetExpPropInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.entity.SourceSubstance;
import gov.epa.databases.dsstox.entity.SourceSubstanceIdentifier;
import gov.epa.databases.dsstox.service.GenericSubstanceServiceImpl;
import gov.epa.databases.dsstox.service.SourceSubstanceServiceImpl;

public class DatabaseLookup {
	
	private static Map<String, Connection> connPool = new HashMap<>();
	
	
	public static DsstoxRecord lookUpByRID(String dtxrid) {
		
		SourceSubstanceServiceImpl sssi=new SourceSubstanceServiceImpl();
		SourceSubstance ss=sssi.findByDtxrid(dtxrid);
		GenericSubstanceServiceImpl gssi=new GenericSubstanceServiceImpl();
		String dtxsid=ss.getSourceGenericSubstanceMapping().getGenericSubstance().getDsstoxSubstanceId();
		List<DsstoxRecord>recs=gssi.findAsDsstoxRecordsByDtxsid(dtxsid);		
		DsstoxRecord dr=recs.get(0);		
		return dr;

	}
	
	

	
	public static String lookUpSourceChemicalIdentifiersByRID(String dtxrid) {
		
		SourceSubstanceServiceImpl sssi=new SourceSubstanceServiceImpl();
		SourceSubstance ss=sssi.findByDtxrid(dtxrid);
		
		List<SourceSubstanceIdentifier>identifiers=ss.getSourceSubstanceIdentifiers();

		JsonObject jo=new JsonObject();
		
		for (SourceSubstanceIdentifier identifier:identifiers) {
//			System.out.println(identifier.getIdentifierType()+"\t"+identifier.getIdentifier());
			jo.addProperty(identifier.getIdentifierType(), identifier.getIdentifier());
		}
		return Utilities.gson.toJson(jo);

	}
	
	
	
	

	
	static String lookupMolWt(String dtxcid, Connection connDSSTOX) {
		String sql="select mol_weight from compounds t \r\n"
				+ "where t.dsstox_compound_id='"+dtxcid+"';";

		//		System.out.println(sql);

		return runSQL(connDSSTOX, sql);
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
	
	
	static String createQuery1(String smiles, int fk_dataset_id) {
		String sql="select dp.canon_qsar_smiles,dp.qsar_property_value, dpc.exp_prop_id\n"; 
		sql+="from qsar_datasets.data_points dp\n";  
		sql+="inner join qsar_datasets.data_point_contributors dpc\n";
		sql+="on dp.id =dpc.fk_data_point_id\n";
		sql+="where dp.canon_qsar_smiles ='"+smiles+"' and dp.fk_dataset_id ="+fk_dataset_id;
		return sql;
	}


	static String createDatapointsQuery(long fk_dataset_id) {
		String sql="select * from qsar_datasets.data_points\n";  
		sql+="where fk_dataset_id="+fk_dataset_id;		
		return sql;
	}
	
	static String lookupQSAR_units(long id, Connection conn) {
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

	
	static String getDataSetName(long id,Connection conn) {

		String sql="select d.\"name\" \r\n"
				+ "from qsar_datasets.datasets d \r\n"
				+ "where id="+id;

		return runSQL(conn, sql);

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

					String canon_qsar_smiles=rs.getString(1);
					jo.addProperty("canon_qsar_smiles",canon_qsar_smiles);
					jo.addProperty("qsar_property_value",rs.getString(2));

					String qsar_units=lookupQSAR_units(id_dataset, conn);
					jo.addProperty("qsar_property_units", qsar_units);

					String dtxcid=lookupDTXCID(canon_qsar_smiles, conn);
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

	static String createDatapointsQuery2(long fk_dataset_id) {

		String sql="select dp.id, dp.canon_qsar_smiles, dp.dtxcid, dp.qsar_property_value, dpc.exp_prop_id, dpc.dtxcid\r\n"
				+ "	from qsar_datasets.data_points dp\r\n"
				+ "	inner join qsar_datasets.data_point_contributors dpc \r\n"
				+ "	on dpc.fk_data_point_id =dp.id \r\n"
				+ "	where fk_dataset_id="+fk_dataset_id+"\r\n";

		return sql;
	}
	
	
	@Deprecated
	/**
	 * This shouldnt be used anymore because have no way of being sure if correct CID is retrieved for our final flat record
	 * Now CID is stored in the datapoints table- get it from there
	 * 
	 * 
	 * @param smiles
	 * @param conn
	 * @return
	 */
	static String lookupDTXCID(String smiles, Connection conn) {
		String sql="select dtxcid from qsar_descriptors.compounds t \r\n"
				+ "where t.canon_qsar_smiles='"+smiles+"';";
		return runSQL(conn, sql);
	}

	
	static void lookupParameters(long fk_property_value_id, Connection conn,JsonObject jo) {
		//TODO is there a simple hibernate way to do this???

		String sql="select p.\"name\" as \"parameter\", pv.value_point_estimate as \"value\", u.\"name\" as \"units\"\n"
				+ "from exp_prop.parameter_values pv\n"
				+"inner join exp_prop.units u\n"
				+"on u.id =pv.fk_unit_id\n"
				+"inner join exp_prop.parameters p\n" 
				+"on p.id=pv.fk_parameter_id\n" 
				+"where pv.fk_property_value_id="+fk_property_value_id;

		//		System.out.println(sql);

		try {
			Statement st = conn.createStatement();			
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {
				String parameter=rs.getString(1);
				String parameterValue=rs.getString(2);
				String parameterUnits=rs.getString(3);//Gabriel used consistent units for parameters
				//				System.out.println(parameter+"\t"+parameterValue+"\t"+parameterUnits);

				if (parameter.equals("Temperature") || parameter.equals("Pressure") || parameter.equals("pH")) {					
					if (parameter.equals("Temperature")) parameter+="_C";
					if (parameter.equals("Pressure")) parameter+="_mmHg";					
					jo.addProperty(parameter, parameterValue);
				} 

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	static String lookupUnits(int id, Connection conn) {
		String sql="select \"name\" from exp_prop.units u \r\n"
				+ "where u.id="+id;
		return runSQL(conn, sql);
	}

	
	
	public static void main(String[] args) {

//		DsstoxRecord dr=lookUpByRID("DTXRID2016296331");
//		System.out.println(dr.casrn+"\t"+dr.preferredName);		
//
//		String json=lookUpSourceChemicalIdentifiersByRID("DTXRID2016296331");
//		System.out.println(json);		
		
	}
}
