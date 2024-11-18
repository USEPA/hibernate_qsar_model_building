package gov.epa.run_from_java.scripts.PredictionDashboard.compare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import gov.epa.run_from_java.scripts.SqlUtilities;

/**
* @author TMARTI02
*/
public class compare {

	void getRandomSample() {
		
		String folder="data//percepta//";
		
		
		String fileDatahub=folder+"chemical_properties_percepta_dtxsids.txt";
		String fileResQsar=folder+"v_predicted_data_percepta_dtxsids.txt";
		
		HashSet <String>dtxsidsDatahub=getDtxsidsFromTextFile(fileDatahub);
		HashSet <String>dtxsidsResQsar=getDtxsidsFromTextFile(fileResQsar);
		List<String>dtxsidsBoth=new ArrayList<>();
		
		for (String dtxsid:dtxsidsResQsar) {
			if(dtxsidsDatahub.contains(dtxsid)) {
				dtxsidsBoth.add(dtxsid);
			}
		}
		Collections.shuffle(dtxsidsBoth);
		
		List<String>dtxsidsSample=new ArrayList<>();
		
		for (int i=0;i<1000;i++) {
			dtxsidsSample.add(dtxsidsBoth.get(i));
		}
		
		dtxsidsToFile(folder+"sampleDtxsids.txt", dtxsidsSample);
		
		
	}
	
	public static void dtxsidsToFile(String filepath,List<String>dtxsids) {
		try {

			File file=new File(filepath);
			FileWriter fw=new FileWriter(file);

			Connection conn=SqlUtilities.getConnectionPostgres();
			
			for(String dtxsid:dtxsids) {
				fw.write(dtxsid+"\r\n");
			}

			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	
	public static HashSet<String> getDtxsidsFromTextFile(String filepath) {
		
		HashSet<String>dtxsids=new HashSet<>();
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			while (true) {
				String line=br.readLine();
				if(line==null) break;
				
				dtxsids.add(line);
			}
			
			br.close();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return dtxsids;
		
	}
	
	void compare() {
		
		boolean omitConsensus=false;

		Hashtable<String,List<ResQsarPrediction>>htDH=DatahubPrediction.getResQsarPredictionsFromJsonFile("data//percepta//chemical_properties_2024_11_06_sample.json",omitConsensus);
		Hashtable<String,List<ResQsarPrediction>>htRQ=ResQsarPrediction.getPredictionsFromJsonFile("data//percepta//materialized_view_2024_11_06_sample.json");

		//iterate over RQ records:
		
		compare(htRQ,htDH);
//		compare(htDH,htRQ);
		
	}

	private void compare(Hashtable<String, List<ResQsarPrediction>> ht1,Hashtable<String, List<ResQsarPrediction>> ht2) {
		System.out.println("Enter compare");
		
		List<String>properties1=getPropertyList(ht1);
		List<String>properties2=getPropertyList(ht2);
		
		for (String property:properties1) {
			if(!properties2.contains(property)) {
				System.out.println(property+" not in 2");
			}
		}

		System.out.println("");
		for (String property:properties2) {
			if(!properties1.contains(property)) {
				System.out.println(property+" not in 1");
			}
		}

		
		for(String dtxsid:ht1.keySet()) {
			
			List<ResQsarPrediction> preds1=ht1.get(dtxsid);
			List<ResQsarPrediction> preds2=ht2.get(dtxsid);
			
			boolean havePred=false;
			for(ResQsarPrediction pred:preds1) {
				if(pred.prediction_value!=null)  {
					havePred=true;
					break;
				}
			}
			
			if(!havePred) {
				System.out.println(dtxsid+"\tall preds null");
				continue;
			}
			
			boolean mismatch=false;
			
//			if(preds2==null) {
//				System.out.println(dtxsid+"\tpreds2 null");
//				continue;
//			}

			
//			System.out.println(dtxsid);
			for (String property:properties1) {
				if(!properties2.contains(property)) continue;

				ResQsarPrediction pred1=getPrediction(preds1, property);
				
				
				ResQsarPrediction pred2=getPrediction(preds2, property);
				
				String strPred1;
				String strPred2;
				
				if(pred1==null)strPred1="null";
				else strPred1=pred1.prediction_value+"";

//				if(pred2!=null) {
//					if(pred2.prediction_value==0.0) pred2.prediction_value=null;
//				}

				if(pred2==null)strPred2="null";
				else strPred2=pred2.prediction_value+"";
				
//				System.out.println(pred1.dtxsid+"\t"+pred1.prediction_value+"\t"+pred2.prediction_value);
				
				
				
				
				if(!strPred1.equals(strPred2)) {
					mismatch=true;
					System.out.println(dtxsid+"\t"+property+"\t"+strPred1+"\t"+strPred2);					
				}

				
//				System.out.println(dtxsid+"\t"+property+"\t"+strPred1+"\t"+strPred2);					

			}
			if(mismatch)System.out.println("");
			
		}

		
	}
	
	ResQsarPrediction getPrediction(List<ResQsarPrediction>preds,String propertyName) {
		for(ResQsarPrediction pred:preds) {
			if(pred.property_name.equals(propertyName)) return pred;
		}
		return null;
	}
	

	private List<String> getPropertyList(Hashtable<String, List<ResQsarPrediction>> ht1) {
		List<String>properties=new ArrayList<>();
		
		for(String dtxsid:ht1.keySet()) {
			List<ResQsarPrediction> predsRQ=ht1.get(dtxsid);
			
			for(ResQsarPrediction pred:predsRQ) {
				if(!properties.contains(pred.property_name)) {
					properties.add(pred.property_name);
				}
			}
		}

		Collections.sort(properties);
		
//		for (String property:properties) {
//			System.out.println(property);
//		}
		return properties;
	}
	
	
	public static void main(String[] args) {
		compare c=new compare();
//		c.getRandomSample();
		c.compare();

	}

}
