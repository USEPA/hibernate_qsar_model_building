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
* @author TMARTI02
*/
public class ResQsarPrediction {
	
	String dtxsid;
	String property_name;
	String model_name;
	Double prediction_value;
	String unit;
	String prediction_error;
	String source_name;
	
	public ResQsarPrediction() {}
	
	public ResQsarPrediction(DatahubPrediction pred) {
		this.dtxsid=pred.dtxsid;
		this.property_name=pred.name;
		this.prediction_value=pred.value;
		this.unit=pred.unit;
		this.source_name=pred.source;
	}
	
	public static ResQsarPrediction getResQsarPrediction(ChemicalProperty.Datum datum,ChemicalProperty chemicalProperty,String source) {

		if(datum.predicted==null) return null;
		
		for (ChemicalProperty.Rawdatum rawdatum:datum.predicted.rawdata) {
			if(rawdatum.source.contains(source)) {
				ResQsarPrediction r=new ResQsarPrediction();
				r.dtxsid=chemicalProperty.dtxsid;
				r.property_name=datum.name;		
				r.unit=datum.unit;
//				System.out.println(Utilities.gson.toJson(rawdatum));
				r.prediction_value=rawdatum.value;
				r.source_name=rawdatum.source;
				r.model_name=rawdatum.modelName;
				
				return r;
			}
		}
		return null;
		
	}
	
	


	void exportPerceptaPredictions() {

		int batchSize=50000;
		int i=0;
		
		Gson gson=new Gson();

		try {
			File file=new File("data/percepta/materialized_view_2024_11_06.json");
			FileWriter fw=new FileWriter(file);

			Connection conn=SqlUtilities.getConnectionPostgres();

			while(true) {

				List<ResQsarPrediction>dps=getMaterializedViewPredictions(i*batchSize, batchSize,conn);

				if(dps.size()==0) {
					break;
				} else {
					for (ResQsarPrediction dp:dps) {
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
	
	void exportSamplePerceptaPredictions(String folder,String date, int count) {

		
		Gson gson=new Gson();

		try {
			
			
			File file=new File(folder+"materialized_view_"+date+"_sample_"+count+".json");
			FileWriter fw=new FileWriter(file);

			Connection conn=SqlUtilities.getConnectionPostgres();

			HashSet<String>dtxsidsSample=CompareResQsarToDatahub.getDtxsidsFromTextFile(folder+"sampleDtxsids"+count+".txt");
			
			List<ResQsarPrediction>dps=getMaterializedViewPredictions(dtxsidsSample,conn);

			
			for (ResQsarPrediction dp:dps) {
				fw.write(gson.toJson(dp)+"\r\n");
			}
			System.out.println(dps.size());
			fw.flush();

			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	

	public static Hashtable<String,List<ResQsarPrediction>> getPredictionsFromJsonFile(String filepath) {	
	
		Hashtable<String,List<ResQsarPrediction>>ht=new Hashtable<>();
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			while (true) {
				String line=br.readLine();
				if(line==null) break;
				
				ResQsarPrediction pred=Utilities.gson.fromJson(line,ResQsarPrediction.class);

				
				if(ht.containsKey(pred.dtxsid)) {
					List<ResQsarPrediction>preds=ht.get(pred.dtxsid);
					preds.add(pred);
				} else {
					List<ResQsarPrediction>preds=new ArrayList<>();
					preds.add(pred);
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
	
	private List<ResQsarPrediction> getMaterializedViewPredictions(HashSet<String> dtxsidsSample, Connection conn) {
		List<ResQsarPrediction>dps=new ArrayList<>();

		String sql="select dtxsid,prop_name, model_name, prop_value,"
				+ "prop_unit,prop_value_error,source_name"
				+ " from public.mv_predicted_data\n";

		sql+="where source_name='Percepta2023.1.2' and\n";
		
		Iterator<String> it=dtxsidsSample.iterator();
		String strDtxsids="";
		while (it.hasNext()) {
			strDtxsids+="'"+it.next()+"'";
			if(it.hasNext()) strDtxsids+=",";
		}
		
		sql+="dtxsid in ("+strDtxsids+")\n";
		sql+="ORDER BY dtxsid,prop_name;";
		System.out.println(sql);

		ResultSet rs=SqlUtilities.runSQL2(conn, sql);

		try {
			while (rs.next()) {
				ResQsarPrediction dp=new ResQsarPrediction();				

				dp.dtxsid=rs.getString(1);
				dp.property_name=rs.getString(2);
				dp.model_name=rs.getString(3);

//				if(rs.getDouble(4)!=0.0) dp.prediction_value=rs.getDouble(4);
				
				//https://stackoverflow.com/questions/2920364/checking-for-a-null-int-value-from-a-java-resultset
				dp.prediction_value = rs.getObject(4) != null ? rs.getDouble(4) : null;
				
				dp.unit=rs.getString(5);
				dp.prediction_error=rs.getString(6);
				dp.source_name=rs.getString(7);

				dps.add(dp);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		System.out.println(compounds.size());
		return dps;	}

	List<ResQsarPrediction> getMaterializedViewPredictions(int offset,int limit, Connection conn) {

		List<ResQsarPrediction>dps=new ArrayList<>();

		String sql="select dtxsid,property_name, model_name, prediction_value,unit,prediction_error from public.v_predicted_data vpd where source_name='Percepta2023.1.2'\n";
		sql+="ORDER BY dtxsid,name\n";
		sql+="LIMIT "+limit+" OFFSET "+offset+";";

//		System.out.println(sql);

		ResultSet rs=SqlUtilities.runSQL2(conn, sql);

		try {
			while (rs.next()) {
				ResQsarPrediction dp=new ResQsarPrediction();				

				dp.dtxsid=rs.getString(1);
				dp.property_name=rs.getString(2);
				dp.model_name=rs.getString(3);
				dp.prediction_value = rs.getObject(4) != null ? rs.getDouble(4) : null;
				dp.unit=rs.getString(5);
				dp.prediction_error=rs.getString(5);

				dps.add(dp);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		System.out.println(compounds.size());
		return dps;
	}
	
	

	List<String> getDTXSIDs(Connection conn) {
		
		List<String>dtxsids=new ArrayList<>();
		String sql="select distinct(dtxsid) from public.mv_predicted_data vpd where source_name='Percepta2023.1.2';";

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
	void exportDtxsids(String folder) {
		try {
			Connection conn=SqlUtilities.getConnectionPostgres();
			List<String>dtxsids=getDTXSIDs(conn);
			CompareResQsarToDatahub.dtxsidsToFile(folder+"mv_predicted_data_percepta_dtxsids.txt", dtxsids);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	

	


	public static void main(String[] args) {
		ResQsarPrediction r=new ResQsarPrediction();
//		r.exportPerceptaPredictions();

//		String folder="data\\Percepta2023.1.2\\test load\\";
		String folder="data\\percepta\\";
		
//		r.exportDtxsids(folder);
//		String date="2025_07_22";
		String date="2025_08_14_stg";
		
		r.exportSamplePerceptaPredictions(folder,date, 5000);

//		r.getPredictionsFromJsonFile("data//percepta//materialized_view_2024_11_06_sample.json");
		

	}

}
