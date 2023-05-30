package gov.epa.run_from_java.scripts.DSSTOX_Loading;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.json.CDL;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.DSSTOX_Loading.DSSTOX_Qsar_Ready_Script.DSSTOX_QSAR_Ready;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

/**
 * @author TMARTI02
 */
public class DSSTOX_MS_Ready_Script {

	static class DSSTOX_MS_Ready {

		String canonical_msr;
		String created_at;
		String created_by;
		String dtxcid;
		String dtxsid;
		String filename;
		String InChI_Key_MSr;
		String inchi_code_msr;
		String insert_dt;
		String original_smiles;
		String smiles;
		String updated_at;
		String updated_by;
		String software_version;

	}

	static void checkmsReady() {
		String filePath = "data/dsstox/NITRO_chemicals.txt";
		Connection conn = SqlUtilities.getConnectionDSSTOX();
		try {

			BufferedReader br = new BufferedReader(new FileReader(filePath));

			String header = br.readLine();
			while (true) {
				String line = br.readLine();

				if (line == null)
					break;

				String[] vals = line.split("\t");
				String cid = vals[2];
				String smiles = vals[3];

				String sql = "select canonical_msr from dsstox_msready dm where dtxcid =\'" + cid + "_3'";

				String msreadySmiles = SqlUtilities.runSQL(conn, sql);

				System.out.println(cid + "\t" + smiles + "\t" + msreadySmiles);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void createSQL(List<DSSTOX_MS_Ready> recs) {

		Connection conn = SqlUtilities.getConnectionDSSTOX();

		int batchSize = 1000;

		String[] fieldNames = { "dtxsid", "dtxcid", 
				"original_smiles", "smiles","canonical_msr", 
				"InChI_Key_MSr","inchi_code_msr", 
				"filename","software_version", "created_by", "created_at" };

		String sql = "INSERT INTO dsstox_msready (";

		for (int i = 0; i < fieldNames.length; i++) {

			if (fieldNames[i].contains(" ")) {
				sql += "\"" + fieldNames[i] + "\"";
			} else {
				sql += fieldNames[i];
			}

			if (i < fieldNames.length - 1)
				sql += ",";
			else
				sql += ") VALUES (";
		}

		for (int i = 0; i < fieldNames.length - 1; i++) {
			sql += "?";
			if (i < fieldNames.length - 1)
				sql += ",";
		}
		sql += "current_timestamp)";
		System.out.println(sql);

		try {
			conn.setAutoCommit(false);
			PreparedStatement prep = conn.prepareStatement(sql);

			long t1 = System.currentTimeMillis();

			for (int counter = 0; counter < recs.size(); counter++) {
				DSSTOX_MS_Ready rec = recs.get(counter);
				prep.setString(1, rec.dtxsid);
				prep.setString(2, rec.dtxcid);
				prep.setString(3, rec.original_smiles);
				prep.setString(4, rec.smiles);
				prep.setString(5, rec.canonical_msr);
				prep.setString(6, rec.InChI_Key_MSr);
				prep.setString(7, rec.inchi_code_msr);
				prep.setString(8, rec.filename);
				prep.setString(9, rec.software_version);
				prep.setString(10, rec.created_by);

				prep.addBatch();

				if (counter % batchSize == 0 && counter != 0) {
					System.out.println(counter);
					prep.executeBatch();
				}
			}

			int[] count = prep.executeBatch();// do what's left
			long t2 = System.currentTimeMillis();
			System.out.println("time to post " + recs.size() + " names using batchsize=" + batchSize + ":\t"
					+ (t2 - t1) / 1000.0 + " seconds");
			conn.commit();
//			conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadMS_Ready(boolean fixCids) {
		try {

			String software_version="OPERA 2.8";
			String lanId="tmarti02";

			String filename="snapshot_compounds_curated_MSready_Summary_file_final.csv";
			InputStream inputStream = new FileInputStream("data/dsstox/tsv/"+filename);
			String csvAsString = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
			String json = CDL.toJSONArray(csvAsString).toString();
			inputStream.close();
			
			
			JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);

			System.out.println("Number of records in csv:"+ja.size());

			List<DSSTOX_MS_Ready>recs=new ArrayList<>();


			for (int i=0;i<ja.size();i++) {
//			for (int i=0;i<1;i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				//					System.out.println(Utilities.gson.toJson(jo));
				
				DSSTOX_MS_Ready rec=new DSSTOX_MS_Ready();

				rec.dtxcid=jo.get("Original_DTXCID").getAsString();
				rec.dtxsid=jo.get("Original_DTXSID").getAsString();
				rec.filename=filename;
				rec.created_by=lanId;

				rec.original_smiles=jo.get("Original_SMILES").getAsString();
				rec.canonical_msr=jo.get("Canonical_MSr").getAsString();
				
				//	rec.smiles="";//TODO leave as null for now

				rec.InChI_Key_MSr=jo.get("InChI Key_MSr").getAsString();
				rec.inchi_code_msr=jo.get("InChI_Code_MSr").getAsString();
				rec.software_version=software_version;
				recs.add(rec);


			}
			
			if (fixCids) {
				addUnderscoreNumbers(recs);
			}
			
			//Store records in the database:
			createSQL(recs);


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Adds underscore numbers to cid for records with same sid
	 *  
	 * @param recs
	 */
	private void addUnderscoreNumbers(List<DSSTOX_MS_Ready> recs) {
		HashMap<String,List<DSSTOX_MS_Ready>> hm=new HashMap<>();
		
		for (DSSTOX_MS_Ready rec:recs) {
			if(hm.get(rec.dtxsid)==null) {
				List<DSSTOX_MS_Ready>list=new ArrayList<>();
				list.add(rec);
				hm.put(rec.dtxsid, list);
			} else {
				List<DSSTOX_MS_Ready>list=hm.get(rec.dtxsid);
				list.add(rec);
			}
		}
		
		for (String key:hm.keySet()) {
			List<DSSTOX_MS_Ready>list=hm.get(key);
			
			for (int i=0;i<list.size();i++) {
				DSSTOX_MS_Ready rec=list.get(i);
				rec.dtxcid=rec.dtxcid+"_"+(i+1);
//				System.out.println(rec.dtxsid+"\t"+rec.dtxcid);
			}
			
		}
	}
	
	
	
	
	public static void main(String[] args) {
		DSSTOX_MS_Ready_Script d=new DSSTOX_MS_Ready_Script();
		d.loadMS_Ready(true);
	}

}
