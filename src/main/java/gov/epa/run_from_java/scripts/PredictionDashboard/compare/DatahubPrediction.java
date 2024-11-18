package gov.epa.run_from_java.scripts.PredictionDashboard.compare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;

import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

/**
 * 

	1. For LogKow, I only stored the output from the ACD/Labs consensus model. So following chemical_properties record is ok:
	{"dtxsid":"DTXSID001007489","unit":"Log units","name":"LogKow: Octanol-Water","value":3.092,"source":"ACD/Labs"}

	since it matches the record in materialized view (note the model_name):
	{"dtxsid":"DTXSID001007489","property_name":"LogKow: Octanol-Water","model_name":"ACD_LogP_Consensus","prediction_value":3.092,"unit":"Log units","source_name":"Percepta2023.1.2"}

	However, you need to omit chemical_properties records where source='ACD/Labs Consensus':
	{"dtxsid":"DTXSID001007489","name":"LogKow: Octanol-Water","value":7.0,"source":"ACD/Labs Consensus"}

	2.In chemical_properties records sometimes the value=0.0:
	{"dtxsid":"DTXSID001007489","unit":"Log units","name":"pKa Acidic Apparent","value":0.0,"source":"ACD/Labs"}

	In my materialized view they have a prediction_value of null and the prediction_error field tells you why:
	{"dtxsid":"DTXSID001007489","property_name":"pKa Acidic Apparent","model_name":"ACD_pKa_Apparent_MA","unit":"Log units","prediction_error":"The structure does not contain ionization centers calculated by current version of ACD/pKa","source_name":"Percepta2023.1.2"}

	So either set the value to null or omit the record entirely.

	3. There are missing records in chemical_properties for Dielectric Constant, LogD5.5, and LogD7.4. These are in my materialized view:
	{"dtxsid":"DTXSID001007489","property_name":"Dielectric Constant","model_name":"ACD_Prop_Dielectric_Constant","unit":"Dimensionless","prediction_error":"Cannot calculate ChemSketch properties","source_name":"Percepta2023.1.2"}
	{"dtxsid":"DTXSID001007489","property_name":"LogD5.5","model_name":"ACD_LogD_5_5","prediction_value":3.092,"unit":"Log units","source_name":"Percepta2023.1.2"}
	{"dtxsid":"DTXSID001007489","property_name":"LogD7.4","model_name":"ACD_LogD_7_4","prediction_value":3.092,"unit":"Log units","source_name":"Percepta2023.1.2"}
	
	4. Sometimes the acidic apparent from the materialized view did not get assigned correctly:
	
	Materialized view:
	{"dtxsid":"DTXSID601216371","property_name":"pKa Acidic Apparent","model_name":"ACD_pKa_Apparent_MA","prediction_value":3.958,"unit":"Log units","source_name":"Percepta2023.1.2"}

	chemical_properties:
	{"dtxsid":"DTXSID601216371","name":"pKa Basic Apparent","value":3.958,"source":"ACD/Labs"}

	It should be acidic not basic (acidic matches OPERA). This has been wrong on the dashboard webpages for a long time.
	
	5. For some chemicals, one or more records are just missing entirely in chemical_properties even though they are in the materialized view

	For example these records in materialized view:
	{"dtxsid":"DTXSID90950404","property_name":"Water Solubility","model_name":"ACD_SolInPW","prediction_value":3.8E-9,"unit":"mol/L","source_name":"Percepta2023.1.2"}
	{"dtxsid":"DTXSID90950404","property_name":"pKa Acidic Apparent","model_name":"ACD_pKa_Apparent_MA","prediction_value":8.754,"unit":"Log units","source_name":"Percepta2023.1.2"}
	{"dtxsid":"DTXSID90950404","property_name":"pKa Basic Apparent","model_name":"ACD_pKa_Apparent_MB","prediction_value":11.567,"unit":"Log units","source_name":"Percepta2023.1.2"}

	
 * 
 * 
* @author TMARTI02
*/
public class DatahubPrediction {
	
	
	String dtxsid;
	String unit;
	String name;
	Double value;
	String source;
	

	
	
	List<String> getDTXSIDs(Connection conn) {
		
		List<String>dtxsids=new ArrayList<>();
		String sql="select distinct(dtxsid) from ccd_app.chemical_properties cp where cp.\"source\" like 'ACD%'\n";

		try {
			
			ResultSet rs=SqlUtilities.runSQL2(conn, sql);
			
			while (rs.next()) {
				String	dtxsid=rs.getString(1);
				dtxsids.add(dtxsid);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return dtxsids;
		
	}

	
	List<DatahubPrediction> getDashboardPredictions(int offset,int limit, Connection conn) {

		List<DatahubPrediction>dps=new ArrayList<>();

		String sql="select dtxsid,unit,name,value,source from ccd_app.chemical_properties cp where cp.\"source\" like 'ACD%'\n";
		sql+="ORDER BY dtxsid,name\n";
		sql+="LIMIT "+limit+" OFFSET "+offset+";";

//		System.out.println(sql);

		ResultSet rs=SqlUtilities.runSQL2(conn, sql);

		try {
			while (rs.next()) {
				DatahubPrediction dp=new DatahubPrediction();				

				dp.dtxsid=rs.getString(1);
				dp.unit=rs.getString(2);
				dp.name=rs.getString(3);
//				dp.value=rs.getDouble(4);
				
				
				
				if(rs.getDouble(4)!=0.0) dp.value=rs.getDouble(4);
				
				dp.source=rs.getString(5);

				dps.add(dp);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		System.out.println(compounds.size());
		return dps;
	}
	
	void exportPerceptaPredictions() {

		int batchSize=50000;
		int i=0;
		
		Gson gson=new Gson();

		try {
			File file=new File("data/percepta/chemical_properties_2024_11_06.json");
			FileWriter fw=new FileWriter(file);

			Connection conn=SqlUtilities.getConnectionPostgres();

			while(true) {

				List<DatahubPrediction>dps=getDashboardPredictions(i*batchSize, batchSize,conn);

				if(dps.size()==0) {
					break;
				} else {
					for (DatahubPrediction dp:dps) {
						fw.write(gson.toJson(dp)+"\r\n");
					}
					System.out.println((i+1)+"\t"+dps.size());
					fw.flush();
					i++;
				}
			}			
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	void exportPerceptaPredictionsSample() {

		
		Gson gson=new Gson();

		try {
			
			String folder="data//percepta//";
			
			File file=new File(folder+"chemical_properties_2024_11_06_sample.json");
			FileWriter fw=new FileWriter(file);

			Connection conn=SqlUtilities.getConnectionPostgres();


			HashSet<String>dtxsidsSample=compare.getDtxsidsFromTextFile(folder+"sampleDtxsids.txt");

			List<DatahubPrediction>dps=getDashboardPredictionsSample(dtxsidsSample,conn);
				
			for (DatahubPrediction dp:dps) {
				fw.write(gson.toJson(dp)+"\r\n");
			}
			System.out.println(dps.size());
			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	private List<DatahubPrediction> getDashboardPredictionsSample(HashSet<String> dtxsidsSample, Connection conn) {
		List<DatahubPrediction>dps=new ArrayList<>();

		String sql="select dtxsid,unit,name,value,source from ccd_app.chemical_properties cp where cp.\"source\" like 'ACD%' and \n";
		
		Iterator<String> it=dtxsidsSample.iterator();
		String strDtxsids="";
		while (it.hasNext()) {
			strDtxsids+="'"+it.next()+"'";
			if(it.hasNext()) strDtxsids+=",";
		}
		
		sql+="dtxsid in ("+strDtxsids+")\n";

		sql+="ORDER BY dtxsid,name\n";
		
		System.out.println(sql);

		ResultSet rs=SqlUtilities.runSQL2(conn, sql);

		try {
			while (rs.next()) {
				DatahubPrediction dp=new DatahubPrediction();				

				dp.dtxsid=rs.getString(1);
				dp.unit=rs.getString(2);
				dp.name=rs.getString(3);
				
//				dp.value=rs.getDouble(4);				
				if(rs.getDouble(4)!=0.0) dp.value=rs.getDouble(4);
				
				dp.source=rs.getString(5);

				dps.add(dp);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		System.out.println(compounds.size());
		return dps;	}


	void exportDtxsids() {
		try {
			Connection conn=SqlUtilities.getConnectionPostgres();
			List<String>dtxsids=getDTXSIDs(conn);
			compare.dtxsidsToFile("data/percepta/chemical_properties_percepta_dtxsids.txt", dtxsids);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	public static Hashtable<String,List<DatahubPrediction>> getPredictionsFromJsonFile(String filepath,boolean omitConsensus) {	
		
		Hashtable<String,List<DatahubPrediction>>ht=new Hashtable<>();
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			while (true) {
				String line=br.readLine();
				if(line==null) break;
				
				DatahubPrediction pred=Utilities.gson.fromJson(line,DatahubPrediction.class);

				if(omitConsensus &&  pred.source.equals("ACD/Labs Consensus")) continue;
				
				if(ht.containsKey(pred.dtxsid)) {
					List<DatahubPrediction>preds=ht.get(pred.dtxsid);
					preds.add(pred);
				} else {
					List<DatahubPrediction>preds=new ArrayList<>();
					preds.add(pred);
					ht.put(pred.dtxsid, preds);
				}
			}
			
			br.close();
			
			System.out.println(Utilities.gson.toJson(ht));
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ht;
		
	}	
	
	

	public static Hashtable<String,List<ResQsarPrediction>> getResQsarPredictionsFromJsonFile(String filepath,boolean omitConsensus) {	
		
		Hashtable<String,List<ResQsarPrediction>>ht=new Hashtable<>();
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			while (true) {
				String line=br.readLine();
				if(line==null) break;
				
				DatahubPrediction pred=Utilities.gson.fromJson(line,DatahubPrediction.class);

				ResQsarPrediction predRQ=new ResQsarPrediction(pred);
				
				if(omitConsensus &&  pred.source.equals("ACD/Labs Consensus")) continue;
				
				if(ht.containsKey(pred.dtxsid)) {
					List<ResQsarPrediction>preds=ht.get(pred.dtxsid);
					preds.add(predRQ);
				} else {
					List<ResQsarPrediction>preds=new ArrayList<>();
					preds.add(predRQ);
					ht.put(pred.dtxsid, preds);
				}
			}
			
			br.close();
			
//			System.out.println(Utilities.gson.toJson(ht));
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ht;
		
	}	
		
	public static void main(String[] args) {
		DatahubPrediction cp=new DatahubPrediction();
//		cp.exportPerceptaPredictions();

//		cp.exportDtxsids();
		cp.exportPerceptaPredictionsSample();
		
//		cp.getPredictionsFromJsonFile("data//percepta//chemical_properties_2024_11_06_sample.json",true);
		
		
	}
}
