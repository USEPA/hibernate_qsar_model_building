package gov.epa.run_from_java.scripts.PredictionDashboard.compare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.bson.Document;

import com.google.gson.Gson;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

//import static com.mongodb.client.model.Filter.and;
//import static com.mongodb.client.model.Filter.eq;
//import static com.mongodb.client.model.Filter.gt;

import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.compare.ChemicalProperty.Datum;
import gov.epa.run_from_java.scripts.PredictionDashboard.compare.ChemicalProperty.Rawdatum;


/**
 * 
 * /** Findings
 * 1. source_name should be Percepta2023.1.2 not ACD/Labs. ACD/Labs is the company not the software.
 * 2. When a chemical doesnt have a Percepta prediction for the property, 
 *    the summary page has a null prediction even though they may have an 
 *    OPERA prediction
 * 3. DTXSID50281842 is missing in the mongo
 * 
 * 
 * @author TMARTI02
 */
public class MongoReport {

	
	
	void exportPerceptaPredictionsSampleResQsar(int count) {

		Gson gson=new Gson();

		try {
			
			String folder="data//percepta//";
			
//			int count=5000;
			
			File file=new File(folder+"chemicalProperty_2024_11_06_sample_"+count+".json");
			FileWriter fw=new FileWriter(file);

			HashSet<String>dtxsidsSample=CompareResQsarToDatahub.getDtxsidsFromTextFile(folder+"sampleDtxsids"+count+".txt");

			System.out.println(dtxsidsSample.size());
			
			List<ResQsarPrediction>dps=getChemicalPropertiesResQsar(dtxsidsSample);
				
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

	
	void exportDataJsonsSample(int count) {

		Gson gson=new Gson();

		try {
			
			String folder="data//percepta//";
			
//			int count=5000;
			
			File file=new File(folder+"chemicalProperty_2024_11_06_sample_data_"+count+".json");
			FileWriter fw=new FileWriter(file);

			HashSet<String>dtxsidsSample=CompareResQsarToDatahub.getDtxsidsFromTextFile(folder+"sampleDtxsids"+count+".txt");

			List<ChemicalProperty>cps=getChemicalProperties(dtxsidsSample);
				
			for (ChemicalProperty cp:cps) {
				fw.write(gson.toJson(cp)+"\r\n");
			}
			System.out.println(cps.size());
			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	

	public static Hashtable<String,List<ResQsarPrediction>> getResQsarPredictionsFromJsonFile(String filepath) {	
		
		Hashtable<String,List<ResQsarPrediction>>ht=new Hashtable<>();
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			while (true) {
				String line=br.readLine();
				if(line==null) break;
				
				ResQsarPrediction predRQ=Utilities.gson.fromJson(line,ResQsarPrediction.class);

				if(ht.containsKey(predRQ.dtxsid)) {
					List<ResQsarPrediction>preds=ht.get(predRQ.dtxsid);
					preds.add(predRQ);
				} else {
					List<ResQsarPrediction>preds=new ArrayList<>();
					preds.add(predRQ);
					ht.put(predRQ.dtxsid, preds);
				}
			}
			
			br.close();
			
//			System.out.println(Utilities.gson.toJson(ht));
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ht;
		
	}	
	
	static class Result {
		int count;
		double avg;
	}

	public static void lookAtSummaryStats(String filepath) {	
		
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			int count=0;
			
			while (true) {
				
				String line=br.readLine();
				if(line==null) break;
				
				ChemicalProperty cp=Utilities.gson.fromJson(line,ChemicalProperty.class);

				for (ChemicalProperty.Datum datum: cp.data) {
					Result result=getResult(datum);
					if(result.count!=datum.predicted.count) {
						System.out.println(cp.dtxsid+"\t"+datum.name+"\t"+datum.predicted.count+"\t"+result.count+"\t"+datum.predicted.mean+"\t"+result.avg);
					}
				}
				
				count++;
				if(count==10) break;
			}
			
			br.close();
			
//			System.out.println(Utilities.gson.toJson(ht));
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}	
	
	private static Result getResult(Datum datum) {

		Result result=new MongoReport.Result();
		
		result.count=0;
		result.avg=0;
		
		for (Rawdatum rd:datum.predicted.rawdata) {
			if(rd.value!=null) {
				result.count++;
				result.avg+=rd.value;
			}
		}
		
		result.avg/=(double)result.count;
		
		
		return result;
	}


	List<ResQsarPrediction> getChemicalPropertiesResQsar(HashSet<String>dtxsids) {
		
//		String sourceAbbrev="ACD/Labs";
		String sourceAbbrev="Percepta2023.1.2";
		
		MongoDatabase database=SqlUtilities.getMongoDatabase();
		MongoCollection<Document> collection = database.getCollection("chemicalProperty");
//		System.out.println(collection.countDocuments());

		Gson gson=new Gson();
//		System.out.println(gson.toJson(dtxsids));
		
//		String strFilter="{dtxsid: { $in: [ \"DTXSID3039242\",\"DTXSID001108150\"] }}";
		String strFilter="{dtxsid:{$in:"+gson.toJson(dtxsids)+"}}";
		
//		System.out.println(strFilter);
		
//		Document filter = new Document("dtxsid", "DTXSID3039242");
		Document filter = Document.parse(strFilter);
		
		FindIterable<Document> documents = collection.find(filter);
		// Iterate over the results
		
		
		List<ResQsarPrediction>rqps=new ArrayList<>();
		
		for (Document document : documents) {
//			System.out.println(document.toJson()); 
			
			ChemicalProperty cp=gson.fromJson(document.toJson(), ChemicalProperty.class);

			for(ChemicalProperty.Datum datum:cp.data) {
				ResQsarPrediction rqp=ResQsarPrediction.getResQsarPrediction(datum,cp,sourceAbbrev);
				if(rqp==null) continue;
				rqps.add(rqp);
//				System.out.println(Utilities.gson.toJson(rqp));
//				System.out.println(gson.toJson(rqp));
			}
		}
		return rqps;
	}
	
	

	List<ChemicalProperty> getChemicalProperties(HashSet<String>dtxsids) {
		
		String sourceAbbrev="ACD/Labs";
		
		MongoDatabase database=SqlUtilities.getMongoDatabase();
		MongoCollection<Document> collection = database.getCollection("chemicalProperty");
//		System.out.println(collection.countDocuments());

		Gson gson=new Gson();
//		System.out.println(gson.toJson(dtxsids));
		
//		String strFilter="{dtxsid: { $in: [ \"DTXSID3039242\",\"DTXSID001108150\"] }}";
		String strFilter="{dtxsid:{$in:"+gson.toJson(dtxsids)+"}}";
		
//		System.out.println(strFilter);
		
//		Document filter = new Document("dtxsid", "DTXSID3039242");
		Document filter = Document.parse(strFilter);
		
		FindIterable<Document> documents = collection.find(filter);
		// Iterate over the results
		
		
		List<ChemicalProperty>cps=new ArrayList<>();
		
		for (Document document : documents) {
//			System.out.println(document.toJson()); 
			
			ChemicalProperty cp=gson.fromJson(document.toJson(), ChemicalProperty.class);
			cps.add(cp);

		}
		return cps;
	}

	
	
	public static void main(String[] args) {

		
		MongoReport mr=new MongoReport();
//		HashSet<String>dtxsids=new HashSet<>();
////		dtxsids.add("DTXSID3039242");
//		dtxsids.add("DTXSID001108150");
//		mr.getChemicalProperties(dtxsids);
		
		int count=1000;
		mr.exportPerceptaPredictionsSampleResQsar(count);
		mr.exportDataJsonsSample(count);
		
		String folder="data//percepta//";
		
		File file=new File(folder+"chemicalProperty_2024_11_06_sample_data_"+count+".json");
		mr.lookAtSummaryStats(file.getAbsolutePath());
		
	}
}

class ChemicalProperty {
    public Id _id;
    public String dtxsid;
    public String dtxcid;
    public ArrayList<Datum> data;
    
    class Datum{
        public String unit;
        public String name;
        public Predicted predicted;
        public Experimental experimental;
    }

    class Details{
        public String value;
        public boolean showLink;
        public String link;
    }

    class Experimental{
        public ArrayList<Rawdatum> rawdata;
        public int count;
        public double mean;
        public double min;
        public double max;
        public ArrayList<Double> range;
        public double median;
    }

    class ExperimentDetails{
        public Object name;
        public String dtxrid;
        public Object value;
    }

    class Id{
        public String $oid;
    }

    class Predicted{
        public ArrayList<Rawdatum> rawdata;
        public Integer count;
        public Double mean;
        public Double min;
        public Double max;
        public ArrayList<Double> range;
        public Double median;
    }

    class Qmrf{
        public String value;
        public boolean showLink;
        public String link;
    }

    class Rawdatum{
        public Double value;
        public Object minValue;
        public Object maxValue;
        public String source;
        public String description;
        public String modelName;
        public int modelId;
        public boolean hasOpera;
        public int globalApplicability;
        public boolean hasQmrfPdf;
        public Details details;
        public Qmrf qmrf;
        public ExperimentDetails experimentDetails;
    }
}

