package gov.epa.run_from_java.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.PropertyValueDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueServiceImpl;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.service.DsstoxCompoundService;
import gov.epa.databases.dsstox.service.DsstoxCompoundServiceImpl;
import gov.epa.util.MathUtil;
import gov.epa.util.wekalite.CSVLoader;
import gov.epa.util.wekalite.Instances;


public class GetExpPropInfo {

	static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	//	static String[]  fieldsFinal= {"exp_prop_id","canon_qsar_smiles","dtxcid_final","qsar_property_value","qsar_property_units",
	//			"exp_prop_id", "fk_public_source_id", "fk_literature_source_id",
	//			"fk_source_chemical_id",  "name","description", "type","url", "page_url",  "source_dtxrid",
	//			"source_dtxsid", "source_casrn", "source_chemical_name", "source_smiles", "notes", "authors", "doi",
	//			"title", "value_qualifier","value_original","value_max", "value_min", 
	//			"qc_flag","dtxcid_mapped","dtxsid_mapped","smiles_mapped","mol_weight_mapped","value_point_estimate","units","Temperature_C","Pressure_mmHg","pH" };

	static String[]  fieldsFinal= {"exp_prop_id","canon_qsar_smiles","qsar_property_value","qsar_property_units",
			"page_url","source_url","source_doi",
			"source_name","source_description", "source_type", "source_authors", "source_title",
			"source_dtxrid","source_dtxsid", "source_casrn", "source_chemical_name", "source_smiles",
			"mapped_dtxcid","mapped_dtxsid","mapped_cas","mapped_chemical_name","mapped_smiles","mapped_molweight",
			"value_original","value_max", "value_min","value_point_estimate","value_units","temperature_c","pressure_mmHg","pH",
			"notes","qc_flag"};

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

	static String createDatapointsQuery2(long fk_dataset_id) {

		String sql="select dp.id, dp.canon_qsar_smiles, dp.dtxcid, dp.qsar_property_value, dpc.exp_prop_id, dpc.dtxcid\r\n"
				+ "	from qsar_datasets.data_points dp\r\n"
				+ "	inner join qsar_datasets.data_point_contributors dpc \r\n"
				+ "	on dpc.fk_data_point_id =dp.id \r\n"
				+ "	where fk_dataset_id="+fk_dataset_id+"\r\n";

		return sql;
	}

	static String getDataSetName(long id,Connection conn) {

		String sql="select d.\"name\" \r\n"
				+ "from qsar_datasets.datasets d \r\n"
				+ "where id="+id;

		return runSQL(conn, sql);

	}


	static String createQueryRID(String RID) {
		String sql = "select ss.dsstox_record_id ,ssi.identifier_type ,ssi.identifier\n"
				+ "from prod_dsstox.source_substances ss join prod_dsstox.source_substance_identifiers ssi\n"
				+ "on ss.id =ssi.fk_source_substance_id\n" + "where ss.dsstox_record_id =\"" + RID + "\"";
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

	@Deprecated
	/**
	 * This shouldnt be used anymore because have no way of being sure if correct CID is retrieved for our final flat record
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


	//	static String lookupDTXCID_DatapointContributor(String fk_datapoint_id, Connection conn) {
	//		String sql="select dtxcid from qsar_datasets.data_points_contributors dpc \r\n"
	//				+ "where dpc.fk_datapoint_id='"+fk_datapoint_id+"';";
	//		return runSQL(conn, sql);
	//	}



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

	public static Connection getConnection() {
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

	static void lookupIdentifierFromRIDs(Connection conn) {


		try {
			List<String> RIDS= Files.readAllLines(Paths.get("data/WS check DSSTIXRIDS.txt"));


			Statement st = conn.createStatement();


			System.out.println("RID\tCAS\tName");

			for (String RID:RIDS) {
				String sql = createQueryRID(RID);
				ResultSet rs = st.executeQuery(sql);

				String Name="";
				String CAS="";


				while (rs.next()) {
					String strRID=rs.getString(1);
					String Type=rs.getString(2);
					String Value=rs.getString(3);
					if (Type.equals("NAME")) Name=Value;
					if (Type.equals("CASRN")) CAS=Value;
				}

				System.out.println(RID+"\t"+CAS+"\t"+Name);

			}


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	static class SourceChemical {
		String NAME;
		String CASRN;
		String INCHIKEY;
	}
	
	
	static SourceChemical lookupNameCAS_From_RID(Connection conn, String RID) {
		SourceChemical sc = new SourceChemical();

		try {
			Statement st = conn.createStatement();

//			System.out.println("RID\tCAS\tName");

			String sql = createQueryRID(RID);
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {
				String strRID = rs.getString(1);
				String Type = rs.getString(2);
				String Value = rs.getString(3);
				if (Type.equals("NAME"))
					sc.NAME = Value;
				if (Type.equals("CASRN"))
					sc.CASRN = Value;
				if(Type.equals("STRUCTURE_INCHIKEY")) {
					sc.INCHIKEY=Value;
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return sc;
	}




	/**
	 * Creates excel file for a dataset (not flattened)
	 * 
	 * @param id_dataset
	 * @param conn
	 * @param connDSSTOX
	 * @param folder
	 */
	public static void getDataSetDataFlat(long id_dataset,Connection conn,Connection connDSSTOX,String folder) {


		try {

			Statement st = conn.createStatement();

			String sql=createDatapointsQuery(id_dataset);

			ResultSet rs = st.executeQuery(sql);
			JsonArray jaRecords=new JsonArray();
			String qsar_units=lookupQSAR_units(id_dataset, conn);


			//			System.out.println(qsar_units);

			int counter=1;

			Hashtable<String,String>htMW=new Hashtable<>();

			while (rs.next()) {

				JsonObject jo=new JsonObject();

				jo.addProperty("id_data_points",rs.getString(1));
				jo.addProperty("canon_qsar_smiles",rs.getString(2));
				jo.addProperty("qsar_property_value",rs.getString(5));

				String dtxcid=rs.getString(10);
				jo.addProperty("dtxcid",dtxcid);
				jo.addProperty("qsar_property_units", qsar_units);

				if (htMW.get(dtxcid)==null) {					

					if (dtxcid.contains("|")) {
						dtxcid=dtxcid.substring(0,dtxcid.indexOf("|")-1);
						//						System.out.println(dtxcid+"\t"+lookupMolWt(dtxcid, connDSSTOX));
					}

					String mol_weight=lookupMolWt(dtxcid, connDSSTOX);
					jo.addProperty("mol_weight",mol_weight);
					htMW.put(dtxcid, mol_weight);
				} else {
					jo.addProperty("mol_weight",htMW.get(dtxcid));
				}


				if (counter%10==0) System.out.println(counter);
				//				System.out.println(gson.toJson(jo));
				jaRecords.add(jo);
				counter++;

				//				if (counter==101) break;

			}

			String dataSetName=getDataSetName(id_dataset, conn);
			//			System.out.println(dataSetName);

			FileWriter fw=new FileWriter(folder+dataSetName+"//"+dataSetName+"_flat.json");			
			fw.write(gson.toJson(jaRecords));
			fw.flush();
			fw.close();

			String [] fields= {"id_data_points","canon_qsar_smiles","qsar_property_value","qsar_property_units",
					"dtxcid","mol_weight"};

			createExcel2(jaRecords, folder+dataSetName+"//"+dataSetName+"_flat.xlsx",fields);

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}



	}
	/**
	 * Creates excel file for a dataset (not flattened)
	 * 
	 * @param id_dataset
	 * @param conn
	 * @param connDSSTOX
	 * @param folder
	 */
	public static void getDataSetData(long id_dataset,Connection conn,Connection connDSSTOX,String folder) {

		try {

			Statement st = conn.createStatement();
			String sql=createDatapointsQuery2(id_dataset);

			System.out.println(sql);

			ResultSet rs = st.executeQuery(sql);
			JsonArray jaRecords=new JsonArray();

			String qsar_units=lookupQSAR_units(id_dataset, conn);
			//			System.out.println(qsar_units);
			int counter=1;
			Hashtable<String,String>htMW=new Hashtable<>();
			int max=1000;

			DsstoxCompoundService d=new DsstoxCompoundServiceImpl();

			while (rs.next()) {
				JsonObject jo=new JsonObject();				

				jo.addProperty("id_data_points",rs.getString(1));
				jo.addProperty("canon_qsar_smiles",rs.getString(2));

				String dtxcid_final=rs.getString(3);
				jo.addProperty("dtxcid_final",dtxcid_final);

				jo.addProperty("qsar_property_value",rs.getString(4));				

				String str_exp_prop_id=rs.getString(5);
				Integer exp_prop_id=Integer.parseInt(str_exp_prop_id.replace("EXP", ""));

				//				if (exp_prop_id!=460913) continue;

				String dtxcid_mapped=rs.getString(6);

				//				if (!dtxcid_final.equals(dtxcid_mapped)) {
				//					System.out.println("Final CID doesnt match CID for current original record:\t"+dtxcid_final+"\t"+dtxcid_mapped);
				//				}

				jo.addProperty("dtxcid_mapped",dtxcid_mapped);

				DsstoxCompound dc=d.findByDtxcid(dtxcid_mapped);
				jo.addProperty("smiles_mapped",dc.getSmiles());
				jo.addProperty("dtxsid_mapped",dc.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId());
				jo.addProperty("qsar_property_units", qsar_units);

				if (htMW.get(dtxcid_mapped)==null) {
					String mol_weight=lookupMolWt(dtxcid_mapped, connDSSTOX);
					jo.addProperty("mol_weight_mapped",mol_weight);
					htMW.put(dtxcid_mapped, mol_weight);
				} else {
					jo.addProperty("mol_weight_mapped",htMW.get(dtxcid_mapped));
				}

				jo.addProperty("exp_prop_id",exp_prop_id);
				getAllValues(exp_prop_id, "exp_prop.property_values",conn, jo);

				lookupParameters(exp_prop_id, conn,jo);

				jo.addProperty("units",lookupUnits(jo.get("fk_unit_id").getAsInt(), conn));

				if (!jo.get("fk_literature_source_id").isJsonNull()) {
					int id=jo.get("fk_literature_source_id").getAsInt();
					getAllValues(id,"exp_prop.literature_sources", conn, jo);
				}

				if (!jo.get("fk_public_source_id").isJsonNull()) {
					int id=jo.get("fk_public_source_id").getAsInt();
					getAllValues(id,"exp_prop.public_sources", conn, jo);
				}

				int id=jo.get("fk_source_chemical_id").getAsInt();
				getAllValues(id,"exp_prop.source_chemicals", conn, jo);

				jo.remove("access_date");
				jo.remove("created_at");
				jo.remove("updated_at");
				jo.remove("created_by");
				jo.remove("keep");
				//				
				jo.remove("id");
				jo.remove("fk_unit_id");				
				jo.remove("fk_property_id");

				if (counter%10==0) System.out.println(counter);

				//				System.out.println(gson.toJson(jo));
				jaRecords.add(jo);
				counter++;

				//				if (counter==max) break;

			}

			String dataSetName=getDataSetName(id_dataset, conn);
			//			System.out.println(dataSetName);

			String filepath=folder+dataSetName+"//"+dataSetName+".json";

			saveJson(jaRecords, filepath);


			createExcel2(jaRecords, folder+dataSetName+"//"+dataSetName+".xlsx",fieldsFinal);

			//			System.out.println(gson.toJson(jaRecords));

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}



	}

	
	
	static void lookatEchemPortalRecordsLogKow(String dataSetName,Connection conn,Connection connDSSTOX,String folder) {
//		String dataSetName=getDataSetName(dataset_id, conn);

		dataSetName=dataSetName.replace(" ", "_").replace("="," ");
		
		String jsonPath=folder+"//"+dataSetName+"//"+dataSetName+"_Mapped_Records.json";

		JsonArray ja=getRecordsFromFile(jsonPath);
		
		Hashtable<String,JsonArray>htRecsByCID=new Hashtable<>();

		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			String cid=jo.get("mapped_dtxcid").getAsString();
			String canon_qsar_smiles=jo.get("canon_qsar_smiles").getAsString();
			String source_name=jo.get("source_name").getAsString();
			
			if(!source_name.equals("eChemPortalAPI")) continue;
				
			if(htRecsByCID.get(cid)==null) {
				JsonArray ja2=new JsonArray();
				ja2.add(jo);
				htRecsByCID.put(cid,ja2);
			} else {
				JsonArray ja2=htRecsByCID.get(cid);
				ja2.add(jo);
			}
		}	
		
		Set<String>cids=htRecsByCID.keySet();
		
		System.out.println(cids.size());
		
		int count=0;
		
		for (String cid:cids) {
			
			JsonArray ja2=htRecsByCID.get(cid);
			if(ja2.size()==1) continue;

			double max=-9999999;
			double min=999999999;
				
			for(int i=0;i<ja2.size();i++) {
				JsonObject jo=ja2.get(i).getAsJsonObject();

				if (jo.get("value_point_estimate")==null) continue;
				double value_point_estimate=jo.get("value_point_estimate").getAsDouble();					

				if(value_point_estimate<min) min=value_point_estimate;
				if(value_point_estimate>max) max=value_point_estimate;
				//					System.out.println(gson.toJson(jo));
			}
				
			if(max<5) continue;
						
			
			for (int i = 0; i < ja2.size(); i++) {
				JsonObject jo = ja2.get(i).getAsJsonObject();

				if (jo.get("value_point_estimate") == null) continue;
				
				
//				if(cid.equals("DTXCID6033530")) {
//					System.out.println(gson.toJson(jo));
//				}

				double value_point_estimate = jo.get("value_point_estimate").getAsDouble();
				String exp_prop_id=jo.get("exp_prop_id").getAsString();

				double diff=Math.abs(max-value_point_estimate);
				
//				System.out.println(cid+"\t"+max+"\t"+value_point_estimate+"\t"+diff);

				if (value_point_estimate>15) {
					System.out.println(++count+"\t"+exp_prop_id+"\t"+cid+"\t"+value_point_estimate+"\t"+min+"\tpoint_estimate>15");					
					//TODO convert
					
				} else if(diff<0.001 && Math.abs(max-min)>5) {
//					System.out.println(gson.toJson(jo));
					double logdiff=Math.abs(Math.log10(max)-min);
					DecimalFormat df=new DecimalFormat("0.00");
					System.out.println(++count+"\t"+exp_prop_id+"\t"+cid+"\t"+value_point_estimate+"\t"+min+"\tis max value && Math.abs(max-min)>5");
				
				} else {//OK values?
//					System.out.println(++count+"\t"+exp_prop_id+"\t"+cid+"\t"+value_point_estimate+"\t"+min+"\tRemainder");	
				}
				
//				if ((diff<0.001 && Math.abs(max-min)>5) || value_point_estimate>15) {
////					System.out.println(gson.toJson(jo));
//					double logdiff=Math.abs(Math.log10(max)-min);
//					//TODO convert to log units
//					System.out.println(++count+"\t"+exp_prop_id+"\t"+cid+"\t"+value_point_estimate);
//				} 
				

			}				
			
			
		}
//		System.out.println(count);
		

	}

	public static void saveJson(Object jaRecords, String filepath)  {
		try {

			FileWriter fw=new FileWriter(filepath);			
			fw.write(gson.toJson(jaRecords));
			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	/**
	 * This method creates the checking spreadsheet using the json file that was
	 * created during dataset creation rather than requerying the database
	 * 
	 * @param dataset_id
	 * @param conn
	 * @param folder
	 * @param arrayPFAS_CIDs
	 */
	static void createCheckingSpreadsheet_PFAS_data(String dataSetName,Connection conn,Connection connDSSTOX,String folder,ArrayList<String>arrayPFAS_CIDs,String versionRulesPFAS) {
//		String dataSetName=getDataSetName(dataset_id, conn);

		dataSetName=dataSetName.replace(" ", "_").replace("="," ");
		
		String jsonPath=folder+"//"+dataSetName+"//"+dataSetName+"_Mapped_Records.json";

		JsonArray ja=getRecordsFromFile(jsonPath);
		JsonArray ja2=new JsonArray();


		ArrayList<String>arrayQSARSmiles=new ArrayList<>();

		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			
//			lookupInfoFromRID(connDSSTOX, jo);//dont need to- look at mapped_cas instead
			
			String dtxcid_original=jo.get("mapped_dtxcid").getAsString();
			String canon_qsar_smiles=jo.get("canon_qsar_smiles").getAsString();

			if (arrayPFAS_CIDs.contains(dtxcid_original)) {
				//				System.out.println(dtxcid_original+"\t"+arrayPFAS_CIDs.contains(dtxcid_original));
				ja2.add(jo);				
				if (!arrayQSARSmiles.contains(canon_qsar_smiles)) arrayQSARSmiles.add(canon_qsar_smiles);

			}
		}

		System.out.println("Number of PFAS records="+ja2.size());
		System.out.println("Number of unique PFAS records="+arrayQSARSmiles.size());

		String pathout=folder+"//"+dataSetName+"//PFAS "+dataSetName+"_"+versionRulesPFAS+".xlsx";
		createExcel2(ja2, pathout,fieldsFinal);

		System.out.println("Excel file created:\t"+pathout);

	}


	private static void lookupInfoFromRID(Connection connDSSTOX, JsonObject jo) {
		if (jo.get("source_dtxrid")!=null) {
			String dtxrid=jo.get("source_dtxrid").getAsString();
			SourceChemical sc=lookupNameCAS_From_RID(connDSSTOX, dtxrid);
			if (sc.NAME!=null) {
				System.out.println(dtxrid+"\t"+sc.NAME);	
			}
			
			if (sc.CASRN!=null) {
				System.out.println(dtxrid+"\t"+sc.CASRN);	
			}
			if (sc.INCHIKEY!=null) {
				System.out.println(dtxrid+"\t"+sc.INCHIKEY);
			}
			
		}
	}


	static ArrayList<String> getPFAS_CIDs(String filepath) {
		try {

			List<String> Lines = Files.readAllLines(Paths.get(filepath));

			ArrayList<String>arrayCIDs=new ArrayList<>();

			for (String Line:Lines) {
				String [] values=Line.split("\t");
				arrayCIDs.add(values[0]);
				
//				System.out.println(values[0]);
			}
			return arrayCIDs;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Old flawed way of getting records- which assumes you can get CID based on the qsar ready 
	 * smiles in the compounds table (instead of new value stored in data_points table
	 * 
	 * It does a series of sql queries which is inefficient but doesnt take that long
	 * 
	 */
	static void oldGetRecords() {

		String folder="data\\dev_qsar\\dataset_files\\";
		Connection conn=getConnection();
		Connection connDSSTOX=getConnectionDSSTOX();					

		int id_dataset=31;
		String inputFileName="Standard Water solubility from exp_prop_T.E.S.T. 5.1_T=PFAS only, P=PFAS_prediction.tsv";
		String outputFileName="Water solubility PFAS prediction records.json";
		getRecords(id_dataset,conn, connDSSTOX, folder, inputFileName,outputFileName);

		inputFileName="Standard Water solubility from exp_prop_T.E.S.T. 5.1_T=PFAS only, P=PFAS_training.tsv";
		outputFileName="Water solubility PFAS training records.json";
		getRecords(id_dataset,conn, connDSSTOX, folder, inputFileName,outputFileName);

		JsonArray jaTraining=getRecordsFromFile(folder+"Water solubility PFAS training records.json");
		JsonArray jaPrediction=getRecordsFromFile(folder+"Water solubility PFAS prediction records.json");
		JsonArray jaOverall=new JsonArray();
		jaOverall.addAll(jaTraining);
		jaOverall.addAll(jaPrediction);
		System.out.println(jaOverall.size());

		String excelFilePath=folder+"Water solubility PFAS records.xlsx";
		createExcel(jaOverall, excelFilePath);

	}




	/**
	 * Looks at checking spreadsheet to determine effect of Good? field
	 * 
	 * @param filepath
	 */
	static void lookAtPFASChecking (String filepath) {

		JsonArray ja=convertExcelToJsonArray(filepath,1);

		//		System.out.println(gson.toJson(ja));
		System.out.println("number of records="+ja.size());

		Hashtable<String,JsonArray>ht=new Hashtable<>();

		boolean omitNo=true;
		boolean omitMaybe=true;
		double tol=0.5;

		for (int i=0;i<ja.size();i++) {

			JsonObject jo=ja.get(i).getAsJsonObject();

			String canon_qsar_smiles=jo.get("canon_qsar_smiles").getAsString();

			if(ht.get(canon_qsar_smiles)==null) {
				JsonArray ja2=new JsonArray();
				ja2.add(jo);
				ht.put(canon_qsar_smiles,ja2);

			} else {
				JsonArray ja2=ht.get(canon_qsar_smiles);
				ja2.add(jo);
			}
		}


		Set<String>keys=ht.keySet();
		System.out.println("number of unique smiles="+keys.size());

		int countZeroRecords=0;
		int countStddev=0;
		double avgDev=0;
		int countChangedMedian=0;

		for (String canon_qsar_smiles:keys) {

			//			System.out.println(canon_qsar_smiles);

			JsonArray ja2=ht.get(canon_qsar_smiles);

			int initialSize=ja2.size();
			
			for (int i=0;i<ja2.size();i++) {
				JsonObject jo=ja2.get(i).getAsJsonObject();
				String Good=jo.get("Good?").getAsString();
//				System.out.println(canon_qsar_smiles+"\t"+Good);
				if (omitNo && Good.contentEquals("No")) ja2.remove(i--);				
				if (omitMaybe && Good.contentEquals("Maybe")) ja2.remove(i--);
			}

//			System.out.println(canon_qsar_smiles+"\t"+initialSize+"\t"+ja2.size());
			
			if (ja2.size()==0) {
				countZeroRecords++;
				//				System.out.println(canon_qsar_smiles);
			} else {
				
				List<Double>vals=new ArrayList();

				double qsar_property_value=Double.parseDouble(ja2.get(0).getAsJsonObject().get("qsar_property_value").getAsString());


				for (int i=0;i<ja2.size();i++) {
					JsonObject jo=ja2.get(i).getAsJsonObject();
					double pointEstimate=Double.parseDouble(jo.get("point estimate in -logM").getAsString());
					vals.add(pointEstimate);
//					System.out.println(qsar_property_value+"\t"+pointEstimate);
				}
				
				
				Collections.sort(vals);
				
				
				double qsar_property_value_new=-9999;
				
				if (vals.size() % 2 == 0)
					qsar_property_value_new = (vals.get(vals.size()/2) + vals.get(vals.size()/2 - 1))/2.0;
				else
					qsar_property_value_new = vals.get(vals.size()/2);

				if (Math.abs(qsar_property_value_new-qsar_property_value)>tol) {
					countChangedMedian++;
//					System.out.println(qsar_property_value+"\t"+qsar_property_value_new);	
				}
				
				if (ja2.size()>1) {
					countStddev++;
					double stddev=MathUtil.stdevS(vals);
					avgDev+=stddev;
					System.out.println(canon_qsar_smiles+"\t"+stddev);
				}
				
			}


		}
		
		
		avgDev/=(double)countStddev;
		
		System.out.println("omitNo\t"+omitNo);
		System.out.println("omitMaybe\t"+omitMaybe);
		System.out.println("Number of smiles with no records\t"+countZeroRecords);
		System.out.println("Number of smiles with changed median\t"+countChangedMedian);
		System.out.println("Avg std dev\t"+avgDev);
		System.out.println("Count std dev\t"+countStddev);

	}
	
	/**
	 * Uses PFAS manual checking spreadsheet to omit bad exp_prop records from property_values table
	 * 
	 * @param filepath
	 */
	static void omitBadDataPointsFromExpProp (String filepath) {

		JsonArray ja=convertExcelToJsonArray(filepath,1);

		//		System.out.println(gson.toJson(ja));
		System.out.println("number of records="+ja.size());

		Hashtable<String,JsonArray>ht=new Hashtable<>();

		boolean omitNo=true;
		boolean omitMaybe=false;
		double tol=0.5;
		
		PropertyValueDaoImpl pvdi=new PropertyValueDaoImpl (); 
		PropertyValueServiceImpl pvsi=new PropertyValueServiceImpl();
		
//		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
//		session.getTransaction().begin();
		
		int count=0;
		
		for (int i=0;i<ja.size();i++) {

			JsonObject jo=ja.get(i).getAsJsonObject();

			String canon_qsar_smiles=jo.get("canon_qsar_smiles").getAsString();
			String Good=jo.get("Good?").getAsString();
			
			if((omitNo && Good.equals("No")) || (omitMaybe) && Good.equals("Maybe")) {
			
				String Reasoning=jo.get("Reasoning").getAsString();
//				System.out.println(Reason);
				
				long exp_prop_id=(long)Double.parseDouble(jo.get("exp_prop_id").getAsString());

				String value_point_estimate=jo.get("value_point_estimate").getAsString();
				
				PropertyValue pv=pvdi.findById(exp_prop_id);

//				System.out.println(exp_prop_id+"\t"+value_point_estimate+"\t"+pv.getValuePointEstimate()+"\t"+Good+"\t"+Reasoning);
				System.out.println(canon_qsar_smiles);

//				if(true) continue;
				
				//Update the PropertyValue:
				try {
					pv.setKeep(false);
					pv.setKeepReason("Omit from manual literature check: "+Reasoning);
//					pvsi.update(pv);
					count++;
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
			
		}

		
		System.out.println("Count omitted="+count);


	}

	private static JsonArray convertExcelToJsonArray(String filepath,int rowNumHeader) {

		JsonArray ja=new JsonArray();
		try {

			FileInputStream inputStream;

			inputStream = new FileInputStream(new File(filepath));

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheet("Records");

			List<String>fields=new ArrayList<>();

			Row rowHeader=sheet.getRow(rowNumHeader);	        

			for (int i=0;i<rowHeader.getLastCellNum();i++) {
				String fieldName=rowHeader.getCell(i).getStringCellValue();
				//		        	System.out.println(fieldName);
				fields.add(fieldName);
				
//				System.out.println(fieldName);
			}

			for (int rowNum=rowNumHeader+1;rowNum<sheet.getLastRowNum();rowNum++) {
				JsonObject jo=new JsonObject();

				Row row=sheet.getRow(rowNum);

				if (row==null) break;

				for (int colNum=0;colNum<row.getLastCellNum();colNum++) {

					Cell cell=row.getCell(colNum);

					FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator(); 
					CellValue value=evaluator.evaluate(cell);
					
					if (value!=null) {
						String strValue=value.formatAsString().replace("\"", "");
						jo.addProperty(fields.get(colNum), strValue);
						
					}

				}
				ja.add(jo);
			}


			workbook.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ja;
	}


	public static void main(String[] args) {

		Connection conn=getConnection();
		Connection connDSSTOX=getConnectionDSSTOX();					

//		long dataset_id=88L;
		//	getDataSetData(dataset_id,conn,connDSSTOX,folder);//pulls data from the database
		//	getDataSetDataFlat(dataset_id,conn,connDSSTOX,folder);

		String folder="data\\dev_qsar\\output\\";

//******************************************************************************************		
//		String dataSetName=getDataSetName(dataset_id, conn);
//		String dataSetName="Standard Boiling Point from exp_prop_TMM";
//		String dataSetName="ExpProp_VP_WithChemProp_070822_TMM";
//		ArrayList<String>arrayPFAS_CIDs=getPFAS_CIDs("data\\dev_qsar\\dataset_files\\PFASSTRUCTV5_qsar_ready_smiles.txt");
//		createCheckingSpreadsheet_PFAS_data(dataSetName,conn,connDSSTOX, folder,arrayPFAS_CIDs);//create checking spreadsheet using json file for mapped records that was created when dataset was created

		
//		String [] datasetNames= {"ExpProp_WaterSolubility_WithChemProp_120121_omit_Good=No","ExpProp_VP_WithChemProp_070822_TMM",
//				"Standard Melting Point from exp_prop_TMM","ExpProp_LogP_WithChemProp_TMM","ExpProp_HLC_TMM",
//				"Standard Boiling Point from exp_prop_TMM","ExpProp BCF Fish_TMM"};

		String [] datasetNames= {"WS_omit_Good_No","ExpProp_VP_WithChemProp_070822_TMM",
		"Standard Melting Point from exp_prop_TMM","ExpProp_LogP_WithChemProp_TMM","ExpProp_HLC_TMM",
		"Standard Boiling Point from exp_prop_TMM","ExpProp BCF Fish_TMM"};

//		String version="V4";
//		ArrayList<String>arrayPFAS_CIDs=getPFAS_CIDs("data\\dev_qsar\\dataset_files\\PFASSTRUCT"+version+"_qsar_ready_smiles.txt");
//		
//		for(String dataSetName:datasetNames) {
//			System.out.println(dataSetName);
//			createCheckingSpreadsheet_PFAS_data(dataSetName,conn,connDSSTOX, folder,arrayPFAS_CIDs,version);//create checking spreadsheet using json file for mapped records that was created when dataset was created
//			System.out.println("");
//		}
		
		lookatEchemPortalRecordsLogKow("ExpProp_LogP_WithChemProp_TMM", conn, connDSSTOX, folder);
			
		
//		lookAtPFASChecking("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\pfas phys prop\\000000 PFAS data checking\\checking Water solubility PFAS records.xlsx");
//		omitBadDataPointsFromExpProp("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\pfas phys prop\\000000 PFAS data checking\\checking Water solubility PFAS records.xlsx");

		//		oldGetRecords();
//		lookupIdentifierFromRIDs(connDSSTOX);  //TODO make the createChecking code use this method too look CAS, name
		//		lookupOperaReferences();
		//		lookupEPISUITE_Isis_References();

	}

	/**
	 * Get OPERA WS references (probably all from epiphys)
	 * 
	 */
	static void lookupOperaReferences() {
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\ghs-data-gathering\\data\\experimental\\OPERA\\OPERA_SDFS\\";
		String filepath=folder+"WS_QR.sdf";


		try {
			IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepath),DefaultChemObjectBuilder.getInstance());

			while (mr.hasNext()) {

				AtomContainer m=null;
				try {
					m = (AtomContainer)mr.next();
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				//				if (m==null || m.getAtomCount()==0) break;
				if (m==null) break;

				String DTXSID=m.getProperty("dsstox_substance_id");
				String CAS=m.getProperty("CAS");
				String WS_Reference=m.getProperty("WS Reference");
				String LogMolar=m.getProperty("LogMolar");

				System.out.println(DTXSID+"~"+CAS+"~"+WS_Reference+"~"+LogMolar);


			}

		} catch (Exception ex) {
			ex.printStackTrace();

		}

	}

	/**
	 * Get OPERA WS references (probably all from epiphys)
	 * 
	 */
	static void lookupEPISUITE_Isis_References() {

		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\ghs-data-gathering\\data\\experimental\\EpisuiteISIS\\EPI_SDF_Data\\";
		String filepath=folder+"EPI_Wskowwin_Data_SDF.sdf";

		try {
			IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepath),DefaultChemObjectBuilder.getInstance());

			while (mr.hasNext()) {

				AtomContainer m=null;
				try {
					m = (AtomContainer)mr.next();
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				//				if (m==null || m.getAtomCount()==0) break;
				if (m==null) break;

				//				String DTXSID=m.getProperty("dsstox_substance_id");
				String CAS=m.getProperty("CAS");
				String WS_Reference=m.getProperty("WS Reference");
				String WS_Data_Type=m.getProperty("WS Data Type");
				String LogMolar=m.getProperty("LogMolar");


				System.out.println(CAS+"|"+WS_Reference+"|"+WS_Data_Type+"|"+LogMolar);


			}

		} catch (Exception ex) {
			ex.printStackTrace();

		}

	}



	static CellStyle createStyleURL(Workbook workbook) {
		CellStyle hlink_style = workbook.createCellStyle();
		Font hlink_font = workbook.createFont();
		hlink_font.setUnderline(Font.U_SINGLE);
		hlink_font.setColor(Font.COLOR_RED);
		hlink_style.setFont(hlink_font);
		return hlink_style;
	}

	static void createExcel(JsonArray ja,String excelFilePath) {
		try {

			Workbook wb = new XSSFWorkbook();

			CellStyle styleURL=createStyleURL(wb);			

			Sheet sheet = wb.createSheet("records");


			String[]  mainFields= {"canon_qsar_smiles","qsar_property_value","qsar_property_units","dtxcid","mol_weight"};

			//Sorted fields:
			String[] recordFields = { "exp_prop_id", "fk_public_source_id", "fk_literature_source_id",
					"fk_source_chemical_id",  "name","description", "type","url", "page_url",  "source_dtxrid",
					"source_dtxsid", "source_casrn", "source_chemical_name", "source_smiles", "notes", "authors", "doi",
					"title", "value_original", "value_qualifier", "value_max", "value_min", "value_point_estimate","units",
			"qc_flag" };



			Row row0=sheet.createRow(0);

			for (int i=0;i<mainFields.length;i++) {
				Cell cell=row0.createCell(i);
				cell.setCellValue(mainFields[i]);				
			}

			for (int i=0;i<recordFields.length;i++) {
				Cell cell=row0.createCell(i+mainFields.length);
				cell.setCellValue(recordFields[i]);				
			}

			int rowNum=1;

			for (int i=0;i<ja.size();i++) {
				JsonObject joMainRecord=ja.get(i).getAsJsonObject();
				JsonArray jaRecords=joMainRecord.get("Records").getAsJsonArray();

				for (int j=0;j<jaRecords.size();j++) {
					Row row=sheet.createRow(rowNum++);	

					JsonObject joRecord=jaRecords.get(j).getAsJsonObject();

					for (int k=0;k<mainFields.length;k++) {
						Cell cell=row.createCell(k);
						if (mainFields[k].equals("qsar_property_value") || mainFields[k].equals("mol_weight") ) {
							cell.setCellValue(Double.parseDouble(joMainRecord.get(mainFields[k]).getAsString()));								
						} else {
							cell.setCellValue(joMainRecord.get(mainFields[k]).getAsString());	
						}
					}

					for (int k=0;k<recordFields.length;k++) {
						Cell cell=row.createCell(k+mainFields.length);
						if (joRecord.get(recordFields[k])!=null) {

							String value=joRecord.get(recordFields[k]).getAsString();

							if (recordFields[k].contains("_id")) {
								cell.setCellValue(Integer.parseInt(value));
							} else if (recordFields[k].contains("value_point_estimate")) {
								cell.setCellValue(Double.parseDouble(value));
							} else {
								cell.setCellValue(value);	
							}
							if (recordFields[k].contains("url")) {
								try {
									Hyperlink link = wb.getCreationHelper().createHyperlink(HyperlinkType.URL);
									link.setAddress(value);
									cell.setHyperlink(link);
									cell.setCellStyle(styleURL);
								} catch (Exception ex) {
									//									System.out.println(ex.getMessage());
								}
							}

						}

					}

				}
			}


			OutputStream fos = new FileOutputStream(excelFilePath);
			wb.write(fos);
			wb.close();


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}



	/**
	 *	TODO- this might run out of memory
	 *
	 * Create excel file using data in JsonArray with fields in specified order
	 * 
	 * @param ja
	 * @param excelFilePath
	 * @param fields
	 */
	public static void createExcel2(JsonArray ja,String excelFilePath,String []fields) {
		try {

			Workbook wb = new XSSFWorkbook();

			CellStyle styleURL=createStyleURL(wb);			

			Sheet sheet = wb.createSheet("records");

			Row recSubtotalRow = sheet.createRow(0);
			Row recHeaderRow = sheet.createRow(1);

			CellStyle csLtBlue=wb.createCellStyle();
			csLtBlue.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		    csLtBlue.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		    
			CellStyle csLtGreen=wb.createCellStyle();
			csLtGreen.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		    csLtGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		    
			for (int i=0;i<fields.length;i++) {
				Cell cell=recHeaderRow.createCell(i);
				cell.setCellValue(fields[i]);
				
				if(fields[i].contains("mapped_")) {
					cell.setCellStyle(csLtBlue);
				} else if (fields[i].contains("source_")) {
					cell.setCellStyle(csLtGreen);
				}

				
			}


			for (int i=0;i<fields.length;i++) {
				sheet.autoSizeColumn(i);
				//				sheet.setColumnWidth(i, sheet.getColumnWidth(i)+20);
			}


			int rowNum=2;

			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();

				Row row=sheet.createRow(rowNum++);	

				for (int k=0;k<fields.length;k++) {
					Cell cell=row.createCell(k);

					if (jo.get(fields[k])==null) continue;
					if (jo.get(fields[k]).isJsonNull()) continue;
					if (jo.get(fields[k]).getAsString().isBlank()) continue;


					String value=jo.get(fields[k]).getAsString();

					//					try {
						//						//TODO have better way to decide if integer, double, or string...
					//						
					//						if (fields[k].equals("qsar_property_value") || fields[k].contains("mol_weight")
					//								|| fields[k].contains("value_m") || fields[k].contains("value_point_estimate") 
					//								|| fields[k].toLowerCase().contains("temperature") || fields[k].toLowerCase().contains("pressure") || fields[k].equals("pH")) {
					//							cell.setCellValue( Double.parseDouble(value));								
					//						} else if ((fields[k].contains("_id") || fields[k].contains("id_")) && !fields[k].toLowerCase().contains("dtx")) {
					//							cell.setCellValue(Integer.parseInt(value));
					//						} else {
					//							cell.setCellValue(value);	
					//						}
					//						
					//					} catch (Exception ex) {
					//						System.out.println("Error setting cell value = "+value+" for "+fields[k]);
					//					}

					try {
						cell.setCellValue(Double.parseDouble(value));	
					} catch (Exception ex) {
						//						System.out.println("Error setting cell value = "+value+" for "+fields[k]);
						cell.setCellValue(value);	
					}

					if (fields[k].contains("url")) {
						try {
							Hyperlink link = wb.getCreationHelper().createHyperlink(HyperlinkType.URL);
							link.setAddress(value);
							cell.setHyperlink(link);
							cell.setCellStyle(styleURL);
						} catch (Exception ex) {
							//							System.out.println(ex.getMessage());
						}
					}

				}
			}

			for (int i = 0; i < fields.length; i++) {
				String col = CellReference.convertNumToColString(i);
				String recSubtotal = "SUBTOTAL(3,"+col+"$3:"+col+"$"+(ja.size()+2)+")";
				recSubtotalRow.createCell(i).setCellFormula(recSubtotal);
			}


			String lastCol = CellReference.convertNumToColString(fields.length-1);
			sheet.setAutoFilter(CellRangeAddress.valueOf("A2:"+lastCol+ja.size()+2));
			sheet.createFreezePane(0, 2);



			OutputStream fos = new FileOutputStream(excelFilePath);
			wb.write(fos);
			wb.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static Vector<String> getListOfFields(JsonArray jaOverall) {
		Vector<String>recordFields=new Vector<>();

		for (int i=0;i<jaOverall.size();i++) {
			JsonObject jo=jaOverall.get(i).getAsJsonObject();
			JsonArray jaRecords=jo.get("Records").getAsJsonArray();

			for (int j=0;j<jaRecords.size();j++) {
				JsonObject joRecord=jaRecords.get(j).getAsJsonObject();

				Set<Map.Entry<String, JsonElement>> entries = joRecord.entrySet();
				for(Map.Entry<String, JsonElement> entry: entries) {
					if (!recordFields.contains(entry.getKey())) {
						recordFields.add(entry.getKey());
					}
				}
			}

		}

		for (String recordField:recordFields) {
			System.out.println(recordField);
		}
		return recordFields;
	}


	public static JsonArray getRecordsFromFile(String filepath) {

		try {

			Reader reader = Files.newBufferedReader(Paths.get(filepath));

			JsonArray ja=gson.fromJson(reader, JsonArray.class);
			return ja;

		} catch(Exception ex) {
			ex.printStackTrace();
			return null;

		}
	}


	private static void getRecords(int  id_dataset, Connection conn, Connection connDSSTOX, String folder, String inputFileName,String outputFileName) {
		CSVLoader c=new CSVLoader();
		try {
			Instances instances=c.getDataSetFromFile(folder+inputFileName,"\t");
			JsonArray ja=new JsonArray();

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
