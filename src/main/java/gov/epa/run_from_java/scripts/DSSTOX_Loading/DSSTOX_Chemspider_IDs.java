package gov.epa.run_from_java.scripts.DSSTOX_Loading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import org.json.CDL;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.GenericSubstance;
import gov.epa.databases.dsstox.entity.GenericSubstanceCompound;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;


/**
 * @author TMARTI02
 */
public class DSSTOX_Chemspider_IDs {

	String lanId="tmarti02";
	
	List<DsstoxCompound> getCompoundsBySQL(int offset, int limit) {

		Connection conn = SqlUtilities.getConnectionDSSTOX();

		List<DsstoxCompound> compounds = new ArrayList<>();

		String sql = "select c.dsstox_compound_id,gs.dsstox_substance_id, chemspider_id from compounds c\r\n"
				+ "left join generic_substance_compounds gsc on c.id = gsc.fk_compound_id\r\n"
				+ "left join generic_substances gs on gsc.fk_generic_substance_id = gs.id\r\n"
				+ "where chemspider_id is not null\n" + "ORDER BY c.dsstox_compound_id\n" + "LIMIT " + limit
				+ " OFFSET " + offset;

		// System.out.println(sql);

		ResultSet rs = SqlUtilities.runSQL2(conn, sql);

		int counter = 1;

		try {
			while (rs.next()) {
				DsstoxCompound compound = new DsstoxCompound();

				compound.setDsstoxCompoundId(rs.getString(1));

				GenericSubstanceCompound gsc = new GenericSubstanceCompound();
				GenericSubstance gs = new GenericSubstance();
				compound.setGenericSubstanceCompound(gsc);
				gsc.setGenericSubstance(gs);
				gs.setDsstoxSubstanceId(rs.getString(2));

				// if(rs.getString(3)==null) continue;

				compound.setChemspiderId(Long.parseLong(rs.getString(3)));

				compounds.add(compound);

				// if(counter==1) {
				// System.out.println(compound.getDsstoxCompoundId()+"\t"+compound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId()+"\t"+compound.getChemspiderId());
				// }

				counter++;

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// System.out.println(compounds.size());
		return compounds;
	}

	HashSet<String> getCIDsInChemSpiderTable(int offset, int limit, String version) {

		Connection conn = SqlUtilities.getConnectionDSSTOX();
		HashSet<String> CIDs = new HashSet<>();

		String sql = "select dsstox_compound_id from chemspider_ids c\r\n" + "where version = '" + version + "'\r\n"
				+ "LIMIT " + limit + " OFFSET " + offset;
		// System.out.println(sql);

		ResultSet rs = SqlUtilities.runSQL2(conn, sql);

		int counter = 1;
		try {
			while (rs.next()) {
				CIDs.add(rs.getString(1));
				counter++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(CIDs.size());
		return CIDs;
	}

	/**
	 * Downloads chemspider_ids from the compounds table and then uploads to
	 * chemspider_ids table
	 */
	void goThroughCompoundsTable() {

		String version = "v1";
		String lanId = "tmarti02";

		int batchSize = 50000;
		int i = 0;

		while (true) {

			List<DsstoxCompound> compounds = getCompoundsBySQL(i * batchSize, batchSize);

			if (compounds.size() == 0) {
				break;
			} else {
				System.out.println(compounds.size());
				createSQL(compounds, version, lanId);

				// for (DsstoxCompound compound:compounds) {
				// System.out.println(compound.getDsstoxCompoundId()+"\t"+compound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId()+"\t"+compound.getChemspiderId());
				// }
				i++;
			}
		}
	}

	public void createSQL(List<DsstoxCompound> compounds, String version, String lanId) {

		Connection conn = SqlUtilities.getConnectionDSSTOX();

		String[] fieldNames = { "dsstox_substance_id", "dsstox_compound_id", "chemspider_id", "InChiKey_Indigo",
				"version", "created_by", "created_at" };
		int batchSize = 1000;

		String sql = "INSERT INTO chemspider_ids (";

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

			for (int counter = 0; counter < compounds.size(); counter++) {
				DsstoxCompound compound = compounds.get(counter);
				prep.setString(1, compound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId());
				prep.setString(2, compound.getDsstoxCompoundId());
				prep.setLong(3, compound.getChemspiderId());
				prep.setString(4, compound.getIndigoInchikey());
				prep.setString(5, version);
				prep.setString(6, lanId);

				prep.addBatch();

				if (counter % batchSize == 0 && counter != 0) {
					System.out.println(counter);
					prep.executeBatch();
					conn.commit();
				}
			}

			int[] count = prep.executeBatch();// do what's left

			long t2 = System.currentTimeMillis();
			System.out.println("time to post " + compounds.size() + " names using batchsize=" + batchSize + ":\t"
					+ (t2 - t1) / 1000.0 + " seconds");
			conn.commit();
			// conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateSQL(List<DsstoxCompound> compounds, String lanId) {

		Connection conn = SqlUtilities.getConnectionDSSTOX();

		String SQL_UPDATE = "UPDATE compounds SET chemspider_id=?, updated_by=? WHERE dsstox_compound_id=?";

		int batchSize = 1000;

		try {
			conn.setAutoCommit(false);
			PreparedStatement prep = conn.prepareStatement(SQL_UPDATE);

			long t1 = System.currentTimeMillis();

			for (int counter = 0; counter < compounds.size(); counter++) {
				// for (int counter = 0; counter < 1; counter++) {

				// System.out.println(counter);

				DsstoxCompound compound = compounds.get(counter);
				prep.setLong(1, compound.getChemspiderId());
				prep.setString(2, lanId);
				prep.setString(3, compound.getDsstoxCompoundId());
				prep.addBatch();

				if (counter % batchSize == 0 && counter != 0) {
					System.out.println(counter);
					prep.executeBatch();
					conn.commit();
				}
			}

			int[] count = prep.executeBatch();// do what's left

			long t2 = System.currentTimeMillis();
			System.out.println("time to post " + compounds.size() + " names using batchsize=" + batchSize + ":\t"
					+ (t2 - t1) / 1000.0 + " seconds");
			conn.commit();
			// conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Go through csv file provided by chemspider
	 * 
	 */
	void goThroughCsvFile() {
		String version = "v2";
		String lanId = "tmarti02";

		String filename = "dsstox-csids.csv";

		try {
			InputStream inputStream = new FileInputStream("data/dsstox/" + filename);

			String csvAsString = new BufferedReader(new InputStreamReader(inputStream)).lines()
					.collect(Collectors.joining("\n"));
			String json = CDL.toJSONArray(csvAsString).toString();
			inputStream.close();

			JsonArray ja = Utilities.gson.fromJson(json, JsonArray.class);

			// List<DsstoxCompound>compounds=new ArrayList<>();

			Hashtable<String, DsstoxCompound> htCompounds = new Hashtable<>();

			// Note HashSet runs a lot faster when using "contains" compared to ArrayList
			HashSet<String> CIDs = getCIDsInChemSpiderTable(0, 999999999, version);// get the CIDs we already have

			for (int i = 0; i < ja.size(); i++) {

				// if(i!=0 && i%1000==0) System.out.println(i);

				JsonObject jo = ja.get(i).getAsJsonObject();

				DsstoxCompound compound = new DsstoxCompound();

				String cid = jo.get("DSSTox_Compound_id").getAsString();

				if (CIDs.contains(cid)) {
					// System.out.println("already have "+cid);
					continue;
				}

				compound.setDsstoxCompoundId(cid);
				compound.setChemspiderId(Long.parseLong(jo.get("CSID").getAsString()));
				compound.setIndigoInchikey(jo.get("InChiKey_Indigo").getAsString());

				GenericSubstanceCompound gsc = new GenericSubstanceCompound();
				compound.setGenericSubstanceCompound(gsc);
				GenericSubstance gs = new GenericSubstance();
				gsc.setGenericSubstance(gs);

				gs.setDsstoxSubstanceId(jo.get("DSSTox_Substance_id").getAsString());

				String key = compound.getDsstoxCompoundId() + "\t"
						+ compound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId() + "\t"
						+ compound.getChemspiderId() + "\t" + compound.getIndigoInchikey();

				if (htCompounds.get(key) == null) {
					htCompounds.put(key, compound);
				} else {
					// System.out.println("Duplicate\t"+compound.getDsstoxCompoundId()+"\t"+compound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId()+"\t"+compound.getChemspiderId()+"\t"+compound.getIndigoInchikey());
					// DsstoxCompound compoundOld=htCompounds.get(key);
					// System.out.println("Previous\t"+compoundOld.getDsstoxCompoundId()+"\t"+compoundOld.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId()+"\t"+compoundOld.getChemspiderId()+"\t"+compoundOld.getIndigoInchikey()+"\n");
				}
			}

			List<DsstoxCompound> compounds = new ArrayList<>();

			for (String key : htCompounds.keySet()) {
				compounds.add(htCompounds.get(key));
			}

			System.out.println(compounds.size());

			createSQL(compounds, version, lanId);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Go through csv file provided by chemspider
	 * 
	 */
	void updateCompoundsTableUsingCsvFile() {
		String version = "v2";
		String lanId = "tmarti02";

		String filename = "dsstox-csids.csv";

		try {
			InputStream inputStream = new FileInputStream("data/dsstox/" + filename);

			FileWriter fw = new FileWriter("data/dsstox/chemspider_id_changed.tsv");

			fw.write("dsstox_compound_id\tchemidspider_id_old\tchemidspider_id_new\r\n");

			String csvAsString = new BufferedReader(new InputStreamReader(inputStream)).lines()
					.collect(Collectors.joining("\n"));
			String json = CDL.toJSONArray(csvAsString).toString();
			inputStream.close();

			JsonArray ja = Utilities.gson.fromJson(json, JsonArray.class);

			Hashtable<String, DsstoxCompound> htCompounds = new Hashtable<>();

			for (int i = 0; i < ja.size(); i++) {

				// if(i!=0 && i%1000==0) System.out.println(i);

				JsonObject jo = ja.get(i).getAsJsonObject();

				DsstoxCompound compound = new DsstoxCompound();

				String cid = jo.get("DSSTox_Compound_id").getAsString();

				compound.setDsstoxCompoundId(cid);
				compound.setChemspiderId(Long.parseLong(jo.get("CSID").getAsString()));
				compound.setIndigoInchikey(jo.get("InChiKey_Indigo").getAsString());

				GenericSubstanceCompound gsc = new GenericSubstanceCompound();
				compound.setGenericSubstanceCompound(gsc);
				GenericSubstance gs = new GenericSubstance();
				gsc.setGenericSubstance(gs);

				gs.setDsstoxSubstanceId(jo.get("DSSTox_Substance_id").getAsString());

				String key = compound.getDsstoxCompoundId() + "\t"
						+ compound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId() + "\t"
						+ compound.getChemspiderId() + "\t" + compound.getIndigoInchikey();

				if (htCompounds.get(key) == null) {
					htCompounds.put(key, compound);
				} else {
					// System.out.println("Duplicate\t"+compound.getDsstoxCompoundId()+"\t"+compound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId()+"\t"+compound.getChemspiderId()+"\t"+compound.getIndigoInchikey());
					// DsstoxCompound compoundOld=htCompounds.get(key);
					// System.out.println("Previous\t"+compoundOld.getDsstoxCompoundId()+"\t"+compoundOld.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId()+"\t"+compoundOld.getChemspiderId()+"\t"+compoundOld.getIndigoInchikey()+"\n");
				}
			}

			List<DsstoxCompound> compounds = new ArrayList<>();

			for (String key : htCompounds.keySet()) {
				compounds.add(htCompounds.get(key));
			}

			Connection conn = SqlUtilities.getConnectionDSSTOX();

			updateSQL(compounds, lanId);

			fw.close();

			// System.out.println(compounds.size());

			// createSQL(compounds, version, lanId);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void nullOutOldChemspiderIds() {

		String version1 = "v1";
		String version2 = "v2";

		Connection conn = SqlUtilities.getConnectionDSSTOX();
		
		Hashtable<String, Long> htChemspider1 = getHashtableIds(version1,conn);
		Hashtable<String, Long> htChemspider2 = getHashtableIds(version2,conn);
		
		List<String>dtxcids=new ArrayList<>();
		int countToNull=0;
		
		for (String dtxcid:htChemspider1.keySet()) {
		
			if(htChemspider2.get(dtxcid)==null) {
				countToNull++;
				System.out.println(countToNull+"\t"+dtxcid);
				dtxcids.add(dtxcid);
				
//				if(true)break;
			}
		}
		
		nullOutChemSpiderIds(dtxcids,conn);
	}
	
	
	public void nullOutChemSpiderIds(List<String> dtxcids,Connection conn) {

		//DTXCID30814652
		
		
		int batchSize=100;
		
		try {
			conn.setAutoCommit(false);

			PreparedStatement prep = conn.prepareStatement(
					"UPDATE compounds SET chemspider_id = ?, updated_by = ?, updated_at=current_timestamp WHERE dsstox_compound_id = ?");

			long t1=System.currentTimeMillis();

			for (int counter = 0; counter < dtxcids.size(); counter++) {
				
				String dtxcid=dtxcids.get(counter);
				
				prep.setNull(1,Types.BIGINT);
				prep.setString(2,lanId);
				prep.setString(3,dtxcid);
				prep.addBatch();
				
				
				if (counter % batchSize == 0 && counter!=0) {
					System.out.println("\t"+counter);
					prep.executeBatch();
					conn.commit();
				}
			}

			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+dtxcids.size()+" using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
//			conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	void compareVersions() {

		String version1 = "v1";
		String version2 = "v2";

		Connection conn = SqlUtilities.getConnectionDSSTOX();

		Hashtable<String, Long> htChemspider1 = getHashtableIds(version1,conn);
		Hashtable<String, Long> htChemspider2 = getHashtableIds(version2,conn);

		HashSet<String> allCids = new HashSet<>();

		for (String dtxcid : htChemspider1.keySet()) {
			allCids.add(dtxcid);
		}

		for (String dtxcid : htChemspider2.keySet()) {
			if (!allCids.contains(dtxcid))
				allCids.add(dtxcid);
		}

		int counter = 0;

		try {
			FileWriter fw = new FileWriter(
					"data/dsstox/have chemspider_id in dsstox but not in new chemspider csv.txt");
			FileWriter fw2 = new FileWriter("data/dsstox/chemspider_id changed.txt");

			
			fw.write("dtxcid\tchemspider_id_old\r\n");
			fw2.write("dtxcid\tchemspider_id_old\tchemspider_id_new\r\n");
			
			for (String dtxcid : allCids) {
				counter++;

				Long chemidspiderId1 = htChemspider1.get(dtxcid);
				Long chemidspiderId2 = htChemspider2.get(dtxcid);

				if (htChemspider1.get(dtxcid) == null) {
//				System.out.println(counter+"\t"+dtxcid+"\tnull\t"+chemidspiderId2);
				} else if (htChemspider2.get(dtxcid) == null) {
					fw.write(dtxcid + "\t" + htChemspider1.get(dtxcid) + "\r\n");
				} else {
					if (!chemidspiderId1.equals(chemidspiderId2)) {
						fw2.write(dtxcid + "\t" + chemidspiderId1 + "\t" + chemidspiderId2 + "\r\n");
					}
				}
			}

			fw.flush();
			fw.close();

			fw2.flush();
			fw2.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Hashtable<String, Long> getHashtableIds(String version1,Connection conn) {
		Hashtable<String, Long> htChemspider = new Hashtable<>();

		String sql = "select dsstox_compound_id, chemspider_id from chemspider_ids where version='" + version1 + "'";


		ResultSet rs = SqlUtilities.runSQL2(conn, sql);

		try {
			while (rs.next()) {
				String dsstox_compound_id = rs.getString(1);
				Long chemspider_id = rs.getLong(2);

				htChemspider.put(dsstox_compound_id, chemspider_id);
//				System.out.println(dsstox_compound_id+"\t"+chemspider_id);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return htChemspider;
	}

	public static void main(String[] args) {

		DSSTOX_Chemspider_IDs d = new DSSTOX_Chemspider_IDs();
		// d.goThroughCompoundsTable();
		// d.goThroughCsvFile();
		 d.updateCompoundsTableUsingCsvFile();

		// d.getCIDsInChemSpiderTable(0, 999999999,"v2");

		d.compareVersions();
//		d.nullOutOldChemspiderIds();

	}

}
