package gov.epa.run_from_java.scripts.PredictionDashboard.OPERA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
* @author TMARTI02
*/

public class RecordToxValModel {
		public Integer model_id;//not needed
		public Integer chemical_id;//not needed
		public String dtxsid;
		public String dtxcid;
		public String casrn;
		public String name; 
		
		public String model="OPERA";
		public String metric;
		public Double value;
		public String units;
		public String qualifier;
		
		public String toTSV() {
			return model_id+"\t"+chemical_id+"\t"+dtxsid+"\t"+model+"\t"+metric+"\t"+value+"\t"+units+"\t"+qualifier;
		}
		
		public String toHeaderTSV() {
			return "model_id\tchemical_id\tdtxsid\tmodel\tmetric\tvalue\tunits\tqualifier";
		}

		
		public static Hashtable<String,RecordToxValModel> getHashtable(List<RecordToxValModel>recs,String metric) {
			Hashtable<String,RecordToxValModel>ht=new Hashtable<>();
			
			for (RecordToxValModel r:recs) {
				if(!r.metric.equals(metric)) continue;
			
				ht.put(r.dtxsid, r);
				
			}
			return ht;
		}

		public static List<RecordToxValModel>getRecordsFromTSV(File f) {
			
			try {
				List<RecordToxValModel>records=new ArrayList<>();
				
				BufferedReader br=new BufferedReader(new FileReader(f));
				
				String header=br.readLine();
				
				while (true) {
					String Line=br.readLine();
					if(Line==null) break;
					
					String [] vals=Line.split("\t");
					
					RecordToxValModel r=new RecordToxValModel();
					
					r.model_id=Integer.parseInt(vals[0]);
					r.chemical_id=Integer.parseInt(vals[1]);
					r.dtxsid=vals[2];
					r.model=vals[3];
					r.metric=vals[4];
					
					if(!vals[5].equals("null")) {
						r.value=Double.parseDouble(vals[5]);
					} 
					
					r.units=vals[6];
					r.qualifier=vals[7];
					records.add(r);
//					
				}
				
				
				br.close();
				return records;
				
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			
			
			return null;
			
		}
		
		
	
}
