package gov.epa.run_from_java.scripts.PredictionDashboard.OPERA_Old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.service.GenericSubstanceServiceImpl;

import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

public class Lookup {

	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	private  static String DB_path="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\OPERA_2.8.db";
	//	public  Statement sqliteStatement = SqliteUtilities.getStatement(DB_path);	

	public static Connection conn = SqliteUtilities.getConnection(DB_path);

	
	private static void handleNoSID_multipleCAS(Neighbor n) {
		System.out.println("SID is null, CAS="+n.CAS);

		String [] CASs=n.SID.split("\\|");

		boolean ok=true;

		Vector<String>smilesArray=new Vector<>();

		for (String CAS:CASs) {
			String sql="select DSSTOX_COMPOUND_ID from IDs where IDs.CASRN='"+CAS+"'";
			String CID=SqliteUtilities.getValueFromSQL(conn, sql);

			if (CID==null) {
				ok=false;
				break;
			}														
			sql="select Canonical_QSARr from Structure where DSSTOX_COMPOUND_ID='"+CID+"'";
			String QSARSmiles=SqliteUtilities.getValueFromSQL(conn, sql);

			if (QSARSmiles==null) {
				ok=false;
				break;
			}

			if (!smilesArray.contains(QSARSmiles)) {
				smilesArray.add(QSARSmiles);
			}
			//					System.out.println(CID+"\t"+QSARSmiles);
		}

		if (ok) {
			if (smilesArray.size()==1) {
				System.out.println("OK:"+n.CAS);	
			} else {
				//				System.out.println("Mixture:"+n.CAS);
				//				for (String smiles:smilesArray) {
				//					System.out.println("\t"+smiles);
				//				}
				n.CID="Error: mixture for CAS";

			}

		} else {
			System.out.println("Cant get CIDs and smiles for "+n.CAS);
			n.CID="Error: cant get smiles for CAS";
		}
	}
	
	public static Gson getGson() {
	    // Trick to get the DefaultDateTypeAdatpter instance
	    // Create a first instance a Gson
	    Gson gson = new GsonBuilder()
	            .setDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
	            .create();

	    // Get the date adapter
	    TypeAdapter<Date> dateTypeAdapter = gson.getAdapter(Date.class);

	    // Ensure the DateTypeAdapter is null safe
	    TypeAdapter<Date> safeDateTypeAdapter = dateTypeAdapter.nullSafe();

	    // Build the definitive safe Gson instance
	    return new GsonBuilder()
	            .registerTypeAdapter(Date.class, safeDateTypeAdapter)
	            .setPrettyPrinting()
	            .create();
	}

	public static void loadLookupFile(TreeMap<String, String> htCID, String filepath)
			throws FileNotFoundException, IOException {
		BufferedReader br=new BufferedReader(new FileReader(filepath));
		br.readLine();
		
		while (true) {
			String Line=br.readLine();
			if (Line==null) break;
			String [] vals=Line.split("\t");				
			if (vals.length<2) break;				
//					System.out.println(vals[0]+"\t"+vals[1]);				
			htCID.put(vals[0], vals[1]);
		}
		
		br.close();
	} 
	
	
	public static void loadLookupJsonFileSID(TreeMap<String, DsstoxRecord> ht, String filepath){
		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));
		
			Type listType = new TypeToken<List<DsstoxRecord>>() {}.getType();

			List<DsstoxRecord> recs = new Gson().fromJson(br, listType);
		
			for (DsstoxRecord dr:recs) {
				ht.put(dr.dsstoxSubstanceId,dr);			
			}
		
			br.close();
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	} 
	
	public static void saveLookupJsonFile(TreeMap<String, DsstoxRecord> ht, String filepath){
		try {

		
			Set <String>keys=ht.keySet();
			
			ArrayList<DsstoxRecord>recs=new ArrayList<>();
			
			for (String key:keys) {
				recs.add(ht.get(key));
			}
			
			FileWriter fw=new FileWriter(filepath);
			fw.write(gson.toJson(recs));
			fw.flush();
			fw.close();
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	} 
	
	
	
	public static void loadLookupJsonFileCAS(TreeMap<String, DsstoxRecord> ht, String filepath){
		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));
		
			Type listType = new TypeToken<List<DsstoxRecord>>() {}.getType();

			List<DsstoxRecord> recs = new Gson().fromJson(br, listType);
		
			for (DsstoxRecord dr:recs) {
				if (dr.casrn!=null)
					ht.put(dr.casrn,dr);		
				
				if (dr.casrnOther!=null)
					ht.put(dr.casrnOther,dr);
			}
		
			br.close();
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	} 
	
	
	private static  void handleSingleSID(Neighbor n) {
		
		String sql="select DSSTOX_COMPOUND_ID from IDs where IDs.DSSTOX_SUBSTANCE_ID='"+n.SID+"'";
		String CID=SqliteUtilities.getValueFromSQL(conn, sql);

		if (CID!=null) {
			n.CID=CID;
			//					System.out.println(n.CAS+"\t"+n.CID);
		} else {
			//					System.out.println("SID="+n.SID+" but cant get CID");
			//TODO look up in DSSTOX
			n.CID="Error: CID not available for SID";
		}
	}
	
	private static void handleNoSIDSingleCAS(Neighbor n) {
		String sql="select DSSTOX_COMPOUND_ID from IDs where IDs.CASRN='"+n.CAS+"'";
		String CID=SqliteUtilities.getValueFromSQL(conn, sql);
		if (CID!=null) {
			n.CID=CID;
			//					System.out.println(n.CAS+"\t"+n.CID);
		} else {
			//					System.out.println("SID is null, CAS="+n.CAS+" but cant get CID");
			//TODO look up in DSSTOX			
			n.CID="Error: CID is not available for CAS";
		}


		sql="select DSSTOX_SUBSTANCE_ID from IDs where IDs.CASRN='"+n.CAS+"'";
		String SID=SqliteUtilities.getValueFromSQL(conn, sql);

		if (SID!=null) {
			n.SID=CID;
			//					System.out.println(n.CAS+"\t"+n.CID);
		} else {
			//					System.out.println("SID is null, CAS="+n.CAS+" but cant get CID");
			//TODO look up in DSSTOX			
			n.SID="Error: SID is not available for CAS";
		}

	}


	
	public static  void getCID(Neighbor n,TreeMap<String,String>htCID) {
				
		String globalKey = n.getGlobalKey();
		
		if (htCID.get(globalKey)!=null) {
			n.CID=htCID.get(globalKey);
//			System.out.println("Found via global key='"+globalKey+"'\t"+n.CID);
			return;
		}
		
//		System.out.println("Didnt have global match");
		
		
		if (n.SID==null || n.SID.isBlank()) {
//			System.out.println("SID is empty, CAS="+n.CAS);
			if (n.CAS.contains("|")) {				
				handleNoSID_multipleCAS(n);
			} else {
				handleNoSIDSingleCAS(n);
			}
			
		} else {//Have SID
			if (n.SID.contains("|")) {
//				System.out.println(n.SID);
				handleMultipleSID(n);
			} else {
				handleSingleSID(n);

			}
		}
		
		if (n.CID!=null) {
			htCID.put(globalKey, n.CID);
		}
		
	}
	
	
	private void getCID2(Neighbor n,TreeMap<String,String>htCID) {

		String globalKey = n.getGlobalKey();

		if (htCID.get(globalKey)!=null) {
			n.CID=htCID.get(globalKey);
			//			System.out.println("Found via global key='"+globalKey+"'\t"+n.CID);
			return;
		}

		//		System.out.println("Didnt have global match");

		if (n.SID==null || n.SID.isBlank()) {
			handleNoSIDSingleCAS(n);
		} else {
			handleSingleSID(n);
		} 				

		if (n.CID!=null) {
			htCID.put(globalKey, n.CID);
		}

	}
	
	private static void handleMultipleSID(Neighbor n) {
		
		long t1=System.currentTimeMillis();
		
		String [] SIDs=n.SID.split("\\|");
		
		boolean ok=true;
		
		Vector<String>smilesArray=new Vector<>();
		
		String CID="";
		
		for (String SID:SIDs) {
			String sql="select DSSTOX_COMPOUND_ID from IDs where IDs.DSSTOX_SUBSTANCE_ID='"+SID+"'";
			CID=SqliteUtilities.getValueFromSQL(conn, sql);
		
			if (CID==null) {
				ok=false;
				break;
			}
												
			sql="select Canonical_QSARr from Structure where DSSTOX_COMPOUND_ID='"+CID+"'";
			String QSARSmiles=SqliteUtilities.getValueFromSQL(conn, sql);
			
			if (QSARSmiles==null) {
				ok=false;
				break;
			}
			
			if (!smilesArray.contains(QSARSmiles)) {
				smilesArray.add(QSARSmiles);
			}
//					System.out.println(CID+"\t"+QSARSmiles);
		}
		
		if (ok) {
			if (smilesArray.size()==1) {
				n.CID=CID;		
//				System.out.println("All same smiles:"+n.SID+"\t"+n.CID);
				
			} else {
//				System.out.println("Mixture:"+n.SID);
//				for (String smiles:smilesArray) {
//					System.out.println("\t"+smiles);
//				}
				n.CID="Error: SIDs indicate mixture";
				
				
			}
		} else {
//			System.out.println("Cant get CIDs and smiles for "+n.SID);
			n.CID="Error: can't get smiles for SID";
		}
		
		
		long t2=System.currentTimeMillis();
		
//		System.out.println("Handle multiple SID, time="+(t2-t1)+"\t"+n.SID);
		
	}
	
	static Vector<String> getColumnNames() {
		try {
			String sql="Select * from Results where id=1";
			//			System.out.println(sql);

			Statement sqliteStatement=SqliteUtilities.getStatement(conn);

			ResultSet rs=SqliteUtilities.getRecords(sqliteStatement, sql);

			ResultSetMetaData rsmd=rs.getMetaData();

			int columnCount = rsmd.getColumnCount();

			Vector<String>colNames=new Vector<>();

			// The column count starts from 1
			for (int i = 1; i <= columnCount; i++ ) {
				String name = rsmd.getColumnName(i);
				colNames.add(name);
			}				
			return colNames;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	void look(String propName1,String propName2) {
		int limit=999999999;
		//		int limit=100;
		int offset=0;

		Vector<RecordOpera> records=new Vector<>();
		Vector<String>colNames=getColumnNames();

		try {
			String sql=createSQL2(propName1,propName2,offset,limit,colNames);

			Statement sqliteStatement=SqliteUtilities.getStatement(this.conn);

			ResultSet rs=SqliteUtilities.getRecords(sqliteStatement, sql);

			Hashtable<String,String>htExp=new Hashtable<>();
			Hashtable<String,String>htPred=new Hashtable<>();

			Hashtable<String,String>htExpN=new Hashtable<>();
			Hashtable<String,String>htPredN=new Hashtable<>();

			TreeMap<String,String>htCID=new TreeMap<>();

			String filepath="data/opera/keysOPERA.txt";

			if (new File(filepath).exists()) {
				Lookup.loadLookupFile(htCID, filepath);
			}

			while (rs.next()) {						 
				RecordOpera r=new RecordOpera();							
				SqliteUtilities.createRecord(rs,r,propName1,propName2);				

				if (records.size()%1000==0 && records.size()>0) {
					System.out.println(records.size());
				}

				if (r.exp!=null && !r.exp.equals("NA")) {
					//					System.out.println(r.DSSTOX_COMPOUND_ID+"\t"+ r.exp);
					htExp.put(r.DSSTOX_COMPOUND_ID, r.exp);
				}

				if (r.pred!=null) htPred.put(r.DSSTOX_COMPOUND_ID, r.pred);				
				//				System.out.println(r.DSSTOX_COMPOUND_ID+"\t"+ r.pred);

				List<Neighbor> neighbors = Neighbor.getNeighbors(r);

				for (Neighbor n:neighbors) {
					getCID(n,htCID);

					//					if (n.CID.contains("Error")) {
					//						System.out.println(n.InChiKey+"\t"+n.CAS+"|"+n.SID);
					//					}

					if (n.CID!=null) {
						htExpN.put(n.CID, n.exp);

						if (n.pred.equals("Inactive")) {
							htPredN.put(n.CID, "0");
						} else if (n.pred.contains("Active")) {
							htPredN.put(n.CID, "1");
						}
						//						htPredN.put(n.CID, n.pred);
						//						System.out.println(n.CID+"\t"+n.exp+"\t"+n.pred);
					}
				}

				records.add(r);
			}

			Set <String>expKeys=htExp.keySet();
			Set <String>predKeys=htPred.keySet();

			Set <String>cidKeys=htCID.keySet();

			//			for (String key:expKeys) {
			//				if (htExpN.get(key)!=null) {
			//					if (!htExp.get(key).equals(htExpN.get(key)))
			//							System.out.println(key+"\t"+htExp.get(key)+"\t"+htExpN.get(key));
			//				}
			//			}

			//			for (String key:predKeys) {
			//				if (htPredN.get(key)!=null) {
			//					if (!htPred.get(key).equals(htPredN.get(key)))
			//							System.out.println(key+"\t"+htPred.get(key)+"\t"+htPredN.get(key));
			//				}
			//			}

			//			System.out.println(records.size());
			System.out.println("\n"+htExp.size());
			System.out.println(htPred.size());

			System.out.println(htExpN.size());
			System.out.println(htPredN.size());

			FileWriter fw=new FileWriter(filepath);
			fw.write("Key\tCID\r\n");


			for (String key:cidKeys) {
				fw.write(key+"\t"+htCID.get(key)+"\r\n");
			}


			fw.flush();
			fw.close();
			//			System.out.println(gson.toJson(records));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	void look2(String propName1,String propName2) {
		//			int limit=999999999;
		int limit=1000;
		int offset=0;

		Vector<RecordOpera> records=new Vector<>();
		Vector<String>colNames=getColumnNames();

		try {
			String sql=createSQL2(propName1,propName2,offset,limit,colNames);

			Statement sqliteStatement=SqliteUtilities.getStatement(this.conn);

			ResultSet rs=SqliteUtilities.getRecords(sqliteStatement, sql);

			Hashtable<String,String>htExp=new Hashtable<>();
			Hashtable<String,String>htPred=new Hashtable<>();

			Hashtable<String,String>htExpN=new Hashtable<>();
			Hashtable<String,String>htPredN=new Hashtable<>();

			TreeMap<String,String>htCID=new TreeMap<>();

			String filepath="data/opera/keysOPERA2.txt";

			if (new File(filepath).exists()) {
				Lookup.loadLookupFile(htCID, filepath);
			}

			while (rs.next()) {						 
				RecordOpera r=new RecordOpera();							
				SqliteUtilities.createRecord(rs,r,propName1,propName2);				

				if (records.size()%1000==0 && records.size()>0) {
					System.out.println(records.size());
				}

				if (r.exp!=null && !r.exp.equals("NA")) {
					//						System.out.println(r.DSSTOX_COMPOUND_ID+"\t"+ r.exp);
					htExp.put(r.DSSTOX_COMPOUND_ID, r.exp);
				}

				if (r.pred!=null) htPred.put(r.DSSTOX_COMPOUND_ID, r.pred);				
				//					System.out.println(r.DSSTOX_COMPOUND_ID+"\t"+ r.pred);

				List<Neighbor> neighbors = Neighbor.getNeighbors(r);

				Neighbor.splitNeighbors(neighbors);//If have | in SID, then make into separate neighbors

				if (r.DSSTOX_COMPOUND_ID.equals("DTXCID0086")) {
					System.out.println(gson.toJson(r));
					System.out.println(gson.toJson(neighbors));
				}

				//					if (neighbors.size()>5) {
				//						System.out.println(gson.toJson(neighbors));
				//						System.out.println("--------");
				//					}


				//					for (Neighbor n:neighbors) {
				//						if (n.SID.contains("|")) {
				//							System.out.println("Still have |");
				//							System.out.println(gson.toJson(n));
				//						}
				//					}


				for (Neighbor n:neighbors) {
					Lookup.getCID(n,htCID);

					//						if (n.CID.contains("Error")) {
					//							System.out.println(n.InChiKey+"\t"+n.CAS+"|"+n.SID);
					//						}

					if (n.CID!=null) {
						htExpN.put(n.CID, n.exp);

						if (n.pred.equals("Inactive")) {
							htPredN.put(n.CID, "0");
						} else if (n.pred.contains("Active")) {
							htPredN.put(n.CID, "1");
						}
						//							htPredN.put(n.CID, n.pred);
						//							System.out.println(n.CID+"\t"+n.exp+"\t"+n.pred);
					}
				}

				records.add(r);
			}

			Set <String>expKeys=htExp.keySet();
			Set <String>predKeys=htPred.keySet();

			Set <String>cidKeys=htCID.keySet();

			//				for (String key:expKeys) {
			//					if (htExpN.get(key)!=null) {
			//						if (!htExp.get(key).equals(htExpN.get(key)))
			//								System.out.println(key+"\t"+htExp.get(key)+"\t"+htExpN.get(key));
			//					}
			//				}

			//				for (String key:predKeys) {
			//					if (htPredN.get(key)!=null) {
			//						if (!htPred.get(key).equals(htPredN.get(key)))
			//								System.out.println(key+"\t"+htPred.get(key)+"\t"+htPredN.get(key));
			//					}
			//				}

			//				System.out.println(records.size());
			System.out.println("\n"+htExp.size());
			System.out.println(htPred.size());

			System.out.println(htExpN.size());
			System.out.println(htPredN.size());

			FileWriter fw=new FileWriter(filepath);
			fw.write("Key\tCID\r\n");


			for (String key:cidKeys) {
				fw.write(key+"\t"+htCID.get(key)+"\r\n");
			}


			fw.flush();
			fw.close();
			//				System.out.println(gson.toJson(records));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	void lookSimple(String propName1,String propName2) {
		int limit=1000000000;
		int offset=0;

		//			if (true) return;
		Vector<RecordOpera> records=new Vector<>();
		Vector<String>colNames=Lookup.getColumnNames();

		try {

			String sql=Lookup.createSQL2(propName1,propName2,offset,limit,colNames);
			System.out.println(sql);

			Statement sqliteStatement=SqliteUtilities.getStatement(this.conn);

			ResultSet rs=SqliteUtilities.getRecords(sqliteStatement, sql);

			while (rs.next()) {						 
				RecordOpera r=new RecordOpera();							
				SqliteUtilities.createRecord(rs,r,propName1,propName2);
				records.add(r);

				//					String [] SIDs=r.DTXSID_neighbor_1.split("\\|");
				//					if (SIDs.length>2) {
				//						System.out.println(gson.toJson(r));
				//					}

				if (!r.DTXSID_neighbor_1.contains("DTXSID") && !r.CAS_neighbor_1.isEmpty()) {
					System.out.println(gson.toJson(r));
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	static void createLookupBySID(String propName1,String propName2) {
		
//		int limit=1000000000;
		int limit=100000;
//		int incr=100000;
//		int limit=incr;
		int offset=0;
		//			if (true) return;		
		Vector<String>colNames=Lookup.getColumnNames();		
		GenericSubstanceServiceImpl gssi=new GenericSubstanceServiceImpl();
		
		try {

			List<DsstoxRecord>recs=new ArrayList<>();
			Vector<String>sids=new Vector<>();
			
//			while (true) {
//				boolean done=getSIDs(propName1, propName2, limit, offset, colNames,sids);				
//				System.out.println(sids.size());
//				if (done) break;
//				offset+=incr;
//			}
			
			getSIDs(propName1, propName2, limit, offset, colNames,sids);				

			
//			System.out.println(sql);
									
			while (sids.size()>0) {
				Vector<String>sids2=new Vector<String>();
				for (int i=1;i<=500;i++) {
					sids2.add(sids.remove(0));
					if (sids.isEmpty()) break;
				}

				List<DsstoxRecord>recs2=gssi.findAsDsstoxRecordsByDtxsidIn(sids2);						
				recs.addAll(recs2);
				System.out.println(recs.size());
				if (sids.isEmpty()) break;
			}
									
//			long t1=System.currentTimeMillis();
//			long t2=System.currentTimeMillis();
//			System.out.println(gson.toJson(recs));
//			System.out.println((t2-t1)+" millisecs");
			
			FileWriter fw=new FileWriter("data/opera/DSSTOX lookup by SID.json");
			fw.write(gson.toJson(recs));
			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	static void createLookupByCAS(String propName1,String propName2) {
		
//		int limit=1000000000;
		int limit=100000;
//		int incr=100000;
//		int limit=incr;
		int offset=0;
		//			if (true) return;		
		Vector<String>colNames=Lookup.getColumnNames();		
		GenericSubstanceServiceImpl gssi=new GenericSubstanceServiceImpl();
		
		try {

			List<DsstoxRecord>recs=new ArrayList<>();
			Vector<String>CASRNs=new Vector<>();
			
//			while (true) {
//				boolean done=getCASRNs(propName1, propName2, limit, offset, colNames,sids);				
//				System.out.println(sids.size());
//				if (done) break;
//				offset+=incr;
//			}
			
			getCASRNs(propName1, propName2, limit, offset, colNames,CASRNs);				

			
//			System.out.println(sql);
									
			while (CASRNs.size()>0) {
				Vector<String>CASRNs2=new Vector<String>();
				for (int i=1;i<=500;i++) {
					CASRNs2.add(CASRNs.remove(0));
					if (CASRNs.isEmpty()) break;
				}

				List<DsstoxRecord>recs2=gssi.findAsDsstoxRecordsByCasrnIn(CASRNs2);						
				recs.addAll(recs2);
				System.out.println(recs.size());
				if (CASRNs.isEmpty()) break;
			}
									
//			long t1=System.currentTimeMillis();
//			long t2=System.currentTimeMillis();
//			System.out.println(gson.toJson(recs));
//			System.out.println((t2-t1)+" millisecs");
			
			FileWriter fw=new FileWriter("data/opera/DSSTOX lookup by CAS.json");
			fw.write(gson.toJson(recs));
			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static boolean getSIDs(String propName1, String propName2, int limit, int offset,
			Vector<String> colNames,Vector<String>sids) throws SQLException {
		String sql=Lookup.createSQL2(propName1,propName2,offset,limit,colNames);

		Statement sqliteStatement=SqliteUtilities.getStatement(conn);

		ResultSet rs=SqliteUtilities.getRecords(sqliteStatement, sql);

		if (!rs.next()) return true;
		
		while (rs.next()) {						 
			RecordOpera r=new RecordOpera();							
			SqliteUtilities.createRecord(rs,r,propName1,propName2);

			List<Neighbor> neighbors = Neighbor.getNeighbors(r);
			Neighbor.splitNeighbors(neighbors);//If have | in SID, then make into separate neighbors

			for (Neighbor n:neighbors) {
				if (n.SID!=null && !n.SID.isEmpty()) {
//							System.out.println(n.SID);

					if (!sids.contains(n.SID))
						sids.add(n.SID);
				}
			}
		}
		
		return false;
		
		
	}
	
	private static boolean getCASRNs(String propName1, String propName2, int limit, int offset,
			Vector<String> colNames,Vector<String>CASRNs) throws SQLException {
		String sql=Lookup.createSQL2(propName1,propName2,offset,limit,colNames);

		Statement sqliteStatement=SqliteUtilities.getStatement(conn);

		ResultSet rs=SqliteUtilities.getRecords(sqliteStatement, sql);

		if (!rs.next()) return true;
		
		while (rs.next()) {						 
			RecordOpera r=new RecordOpera();							
			SqliteUtilities.createRecord(rs,r,propName1,propName2);

			List<Neighbor> neighbors = Neighbor.getNeighbors(r);
			Neighbor.splitNeighbors(neighbors);//If have | in SID, then make into separate neighbors

			for (Neighbor n:neighbors) {
				if (n.CAS!=null && !n.CAS.isEmpty()) {
//							System.out.println(n.SID);

					if (!CASRNs.contains(n.CAS))
						CASRNs.add(n.CAS);
				}
			}
		}
		
		return false;
		
		
	}

	
	private static  Vector<String> getFieldList(String propName1, String propName2, Vector<String> colNames) {
		Vector<String>fields=new Vector<>();
		fields.add("DSSTOX_COMPOUND_ID");
		addField(propName1+"_exp",propName1, propName2, colNames, fields);
		addField(propName1+"_pred",propName1, propName2, colNames, fields);
		//		addField(propName1+"_predRange",propName1, propName2, colNames, fields);
		addField("AD_"+propName1,propName1, propName2, colNames, fields);
		addField("AD_index_"+propName1,propName1, propName2, colNames, fields);
		addField("Conf_index_"+propName1,propName1, propName2, colNames, fields);

		for (int i=1;i<=5;i++) {
			addField(propName1+"_DTXSID_neighbor_"+i,propName1, propName2, colNames, fields);
			addField(propName1+"_CAS_neighbor_"+i,propName1, propName2, colNames, fields);
			addField(propName1+"_Exp_neighbor_"+i,propName1, propName2, colNames, fields);
			addField(propName1+"_pred_neighbor_"+i,propName1, propName2, colNames, fields);
			addField(propName1+"_InChiKey_neighbor_"+i,propName1, propName2, colNames, fields);
		}
		return fields;
	}

	private static void addField(String fieldName,String propName1, String propName2, Vector<String> colNames, Vector<String> fields) {
		if (!colNames.contains(fieldName)) fieldName=fieldName.replace(propName1, propName2);
		fields.add(fieldName);
	}

	
	
	
	
	String createSQL(String propName1,String propName2, int offset,int limit,Vector<String>colNames) {

		Vector<String> fields = getFieldList(propName1, propName2, colNames);

		String SQL="SELECT ";							
		for (int i=0;i<fields.size();i++) {
			SQL+=fields.get(i);
			if (i<fields.size()-1) SQL+=",";
		}
		SQL+= "\nFrom Results LIMIT "+limit+" OFFSET "+offset+"\n";
		return SQL;
	}

	/**
	 * Creates sql query with OPERA prediction record with smiles included
	 * 
	 * @param propName1
	 * @param propName2
	 * @param offset
	 * @param limit
	 * @param colNames
	 * @return
	 */
	static String createSQL2(String propName1,String propName2, int offset,int limit,Vector<String>colNames) {

		Vector<String> fields = getFieldList(propName1, propName2, colNames);

		String SQL="SELECT ";							
		for (int i=0;i<fields.size();i++) {
			SQL+="r."+fields.get(i);
			SQL+=",";
		}
		SQL+= "s.Canonical_QSARr,s.Original_SMILES\nFrom Results r\n"
				+ "INNER JOIN Structure s\n"
				+ "ON s.DSSTOX_COMPOUND_ID = r.DSSTOX_COMPOUND_ID\n";
				
		if (limit!=-1)
			SQL+="LIMIT "+limit+" OFFSET "+offset+"\n";
		
		return SQL;
	}
	
	public static String createSQLAll(int offset,int limit) {

		String SQL="SELECT * from Results\r\n";							

		if (limit!=-1)
			SQL+="LIMIT "+limit+" OFFSET "+offset+"\n";
		
		return SQL;
	}
	
	
	
	public static void main(String[] args) {
//		createLookupBySID("CERAPP_Bind","");
//		createLookupByCAS("CERAPP_Bind","");
		
	}

}
