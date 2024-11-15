package gov.epa.run_from_java.scripts.PredictionDashboard.OPERA_Old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import gov.epa.databases.dsstox.DsstoxRecord;

/**
* @author TMARTI02
*/
public class DsstoxMapping {

	
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	
	List<Neighbor> mapNeighbors(String propName1) {
		List<Neighbor> neighbors=null;
		
		try {
		
			boolean split=true;
			
			TreeMap<String, DsstoxRecord> htSID=new TreeMap<>();
			TreeMap<String, DsstoxRecord> htCAS=new TreeMap<>();

			Lookup.loadLookupJsonFileSID(htSID, "data/opera/"+propName1+" DSSTOX lookup by SID.json");
			Lookup.loadLookupJsonFileCAS(htCAS, "data/opera/"+propName1+" DSSTOX lookup by CAS.json");
			
			BufferedReader br=new BufferedReader(new FileReader("data/opera/"+propName1+" neighbors.json"));
			Type listType = new TypeToken<List<Neighbor>>() {}.getType();
			neighbors = new Gson().fromJson(br, listType);
			br.close();
			
			
			List<Neighbor> unMappedNeighbors=new ArrayList<>();
			
			if(split) Neighbor.splitNeighbors(neighbors);
			
			for (Neighbor neighbor:neighbors) {
				neighbor.num=null;
				
//				System.out.println(neighbor.CAS+"\t"+neighbor.SID);
				
				mapBySID(htSID, neighbor);
				if (neighbor.dsstoxRecord!=null) continue;
				mapByCAS(htCAS, neighbor);
				if (neighbor.dsstoxRecord!=null) continue;
				
//				splitBySID(split, htSID, neighbor);
				
				if (neighbor.dsstoxRecord==null) {
//					System.out.println(gson.toJson(neighbor));
					unMappedNeighbors.add(neighbor);					
				}
			}
			
//			int count=0;
//			for(Neighbor neighbor:neighbors) {
//				if(neighbor.mapping==null || neighbor.mapping.equals("No hit by CAS")) {
//					System.out.println(++count+"\t"+neighbor.CAS);
//				}
//			}
//			System.out.println(gson.toJson(neighbors));
			
			
			File folder=new File("data/opera/"+propName1);
			if (!folder.exists()) {
				folder.mkdirs();
			}

			FileWriter fw=new FileWriter(folder.getAbsolutePath()+File.separator+propName1+" mapped neighbors.json");
			fw.write(gson.toJson(neighbors));

			fw.flush();
			fw.close();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return neighbors;
	}
	
	private void determineNoHitCASNumbersCerapp() {
		List<Neighbor>n1=mapNeighbors("CERAPP_Bind");
		List<Neighbor>n2=mapNeighbors("CERAPP_Ago");
		List<Neighbor>n3=mapNeighbors("CERAPP_Anta");
		
		List<Neighbor>nAll=new ArrayList<>();
		nAll.addAll(n1);
		nAll.addAll(n2);
		nAll.addAll(n3);
//		System.out.println(nAll.size());
		
		List<String>noHitCAS=new ArrayList<>();
		for (Neighbor n:nAll) {			
//			System.out.println(n.mapping);			
			if (n.mapping==null || n.mapping.equals("No hit by CAS")) {
				if(!noHitCAS.contains(n.CAS)) {
					noHitCAS.add(n.CAS);
				}
			}
		}
		Collections.sort(noHitCAS);
	
		for (String CAS:noHitCAS) {
			System.out.println(CAS);
		}
		System.out.println(noHitCAS.size());
	}
	
	public static void mapByCAS(TreeMap<String, DsstoxRecord> htCAS, Neighbor neighbor) {
		if (!neighbor.CAS.isBlank()) {					
			if (htCAS.get(neighbor.CAS)!=null) {
				DsstoxRecord dr=htCAS.get(neighbor.CAS);						
				
				neighbor.dsstoxRecord=dr;
				
				if (dr.dsstoxCompoundId==null) {
					neighbor.mapping="No CID for CAS in DSSTOX";
//							System.out.println(neighbor.CAS+"\t"+neighbor.SID+"\t"+neighbor.mapping);							
				} else {
					neighbor.mapping="CID from CAS";
//							System.out.println(neighbor.CAS+"\t"+neighbor.SID+"\t"+neighbor.CID+"\t"+neighbor.mapping);
				}
			} else {
				neighbor.mapping="No hit by CAS";
			}
			
		}
	}
	
	
	
	
	public static  String mapByCAS(TreeMap<String, DsstoxRecord> htCAS, String CAS) {

		String SID="";

		List<String>SIDs=new ArrayList<>();
		
		String [] CASRNs=CAS.split("\\|");

		for (String CASRN:CASRNs) {
			if (htCAS.get(CASRN)!=null) {
				DsstoxRecord dr=htCAS.get(CASRN);
				SIDs.add(dr.dsstoxSubstanceId);
			}
		}

		for (int i=0;i<SIDs.size();i++) {
			SID+=SIDs.get(i);
			if (i<SIDs.size()-1) SID+="|";
		}
		return SID;
	}


	public static void mapBySID(TreeMap<String, DsstoxRecord> htSID, Neighbor neighbor) {
		if (neighbor.SID!=null && !neighbor.SID.isBlank()) {					
			
			if (htSID.get(neighbor.SID)!=null) {
				DsstoxRecord dr=htSID.get(neighbor.SID);						
				
				neighbor.dsstoxRecord=dr;
				
				if (dr.dsstoxCompoundId==null) {
					neighbor.mapping="No CID for SID in DSSTOX";
//							System.out.println(neighbor.CAS+"\t"+neighbor.SID+"\t"+neighbor.mapping);							
				} else {
					neighbor.mapping="CID from SID";
//							System.out.println(neighbor.CAS+"\t"+neighbor.SID+"\t"+neighbor.mapping);
				}
			}
		}
	}
	
}
