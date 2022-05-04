package gov.epa.run_from_java.scripts.compareSDE;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


public class CompareSDE_Predictions_to_Database {

	
	static void go() {
		
		try {
			
			boolean standardize=false;//We need this to work so can explain differences
			
			BufferedReader br=new BufferedReader(new FileReader("diffs.csv"));
						
			String strHeader=br.readLine();
						
			String [] headers=strHeader.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
			
			Hashtable<String,Integer>htSto=new Hashtable<String,Integer>();
			Hashtable<String,Integer>htPre=new Hashtable<String,Integer>();
			
			
			int colSmi=0;
			
			for (int i=0;i<headers.length;i++) {
				headers[i]=headers[i].replace("\"","");				
				
				if (headers[i].contains("sto-")) htSto.put(headers[i].replace("sto-",""), i);
				if (headers[i].contains("pre-")) htPre.put(headers[i].replace("pre-",""), i);				
//				System.out.println(headers[i]);
			}
			
			Vector<String>badSmiles=new Vector<>();
												
			while(true) {
				String Line=br.readLine();
				if (Line==null) break;
								
				String [] vals=Line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
//				System.out.println(vals.length);			

				Enumeration<String> e = htSto.keys();
		        while (e.hasMoreElements()) {
		 
		            String key = e.nextElement();
		            
//		            if (!model.contains("T.E.S.T. 5.1")) continue;
		            		            
		            if (htPre.get(key)==null) {//Dont have pre column
		            	continue;
		            }
		            
		            int colPre=htPre.get(key);
		            int colSto=htSto.get(key);
		            
		            String smi=vals[colSmi];
		            String sto=vals[colSto];
		            String pre=vals[colPre];
		            
		            if (sto.isEmpty() || pre.isEmpty()) continue;
		            
//		            if (key.contains("LLNA") || key.contains("Mutagenicity") || key.contains("DevTox")) continue;
		            
		            
		            if (Math.abs(Double.parseDouble(sto)-Double.parseDouble(pre))>1e-5) {		            	
		            	if (!badSmiles.contains(smi)) badSmiles.add(smi);	
		            	
		            	System.out.println(key+"\t"+smi+"\t"+sto+"\t"+pre);	
		            }
		        }
					
//				if (true) break;
				
			}
			
			
			for (String smiles:badSmiles) {
				String result=RunRequest.compareTESTDescriptors(smiles,standardize);
				System.out.println(smiles+"\t"+result);
			}
			
			
		}catch (Exception ex) {
			ex.printStackTrace();
		}
		 
	}
	
	
	
	
	public static void main(String[] args) {
		
		CompareSDE_Predictions_to_Database.go();
		
	}
}
