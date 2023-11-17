package gov.epa.run_from_java.scripts.OPERA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.run_from_java.scripts.OPERA_Old.RecordOpera;

public class Neighbor {
	
	public Integer num;//Neighbor number (e.g. 1,2,3,4,5) 
	public String exp;
	
	public String pred;
	public String CAS;
	public String SID;
	public String CID;
	public String InChiKey;
	
	public DsstoxRecord dsstoxRecord;
	public String mapping;
	
	public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	public String getGlobalKey() {
		String globalKey=CAS+"\t"+SID;
		return globalKey;
	}

	
	public static List<Neighbor> getNeighbors(RecordOpera r) {
		Vector<Neighbor>neighbors=new Vector<>();
		
		for (int i=1;i<=5;i++) {
	        try {
	    		Neighbor n=new Neighbor();
	        	
	        	n.SID=(String)RecordOpera.class.getField("DTXSID_neighbor_"+i).get(r);
	        	n.CAS=(String)RecordOpera.class.getField("CAS_neighbor_"+i).get(r);
	        	n.exp=(String)RecordOpera.class.getField("Exp_neighbor_"+i).get(r);
	        	n.pred=(String)RecordOpera.class.getField("pred_neighbor_"+i).get(r);
	        	n.InChiKey=(String)RecordOpera.class.getField("InChiKey_neighbor_"+i).get(r);
	    		n.num=i;
	    		neighbors.add(n);
	    		
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		return neighbors;
	}
	
	public static List<Neighbor> splitNeighbors(List<Neighbor> neighbors) {
	
		List<Neighbor>newNeighbors=new ArrayList<>();
		
//		for (int i=0;i<neighbors.size();i++) {
//			Neighbor n=neighbors.get(i);
//			System.out.println("here"+n.num);
//		}
		
//		System.out.println(neighbors.size());
		
		for (int i=0;i<neighbors.size();i++) {
			
			Neighbor n=neighbors.get(i);
			neighbors.remove(i--);
			
			
			if(n.SID.contains("|")) {
				
				String [] SIDs=n.SID.split("\\|");
				String [] CASRNs=n.CAS.split("\\|");
				
//				System.out.println(SIDs.length+"\t"+CASRNs.length);
				
				for (int  j=0;j<SIDs.length;j++) {
					Neighbor nnew=new Neighbor();
					nnew.CAS=null;//Cant assign since number of cas may not match number of SIDs provided
					nnew.SID=SIDs[j];
					nnew.num=n.num;
//					System.out.println(nnew.CAS+"\t"+nnew.SID);
					
					nnew.exp=n.exp;
					nnew.pred=n.pred;
					nnew.InChiKey=n.InChiKey;
					newNeighbors.add(nnew);
									
				}
			} else if (n.CAS.contains("|")) {				
				String [] CASRNs=n.CAS.split("\\|");				
								
				for (int  j=0;j<CASRNs.length;j++) {
					Neighbor nnew=new Neighbor();
					nnew.CAS=CASRNs[j];
					nnew.SID=null;
					nnew.num=n.num;
//					System.out.println(nnew.CAS+"\t"+nnew.SID);
					nnew.exp=n.exp;
					nnew.pred=n.pred;
					nnew.InChiKey=n.InChiKey;
					newNeighbors.add(nnew);
					
//					System.out.println(gson.toJson(nnew));
				}
				
			} else {
				newNeighbors.add(n);
			}
			
			
			
		}
		
		Collections.sort(newNeighbors, new Comparator<Neighbor>() {
            @Override
            public int compare(Neighbor n1, Neighbor n2) {
                return n1.num.compareTo(n2.num);
            }
        });

		
//		for (Neighbor n:neighbors) {
//			if (n.SID.contains("|")) {
//				System.out.println("Still have |");
//				System.out.println(gson.toJson(n));
//			}
//		}
//		if (newNeighbors.size()>5)
//			System.out.println(gson.toJson(newNeighbors));
		
		return newNeighbors;
	}
}