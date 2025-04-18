package gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.Gson;
import com.srcinc.episuite.biodegradationrate.BiodegradationRateResults;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * @author TMARTI02
 */
public class GetBiodegFragmentCounts {

	public GetBiodegFragmentCounts() {
		Unirest.config().followRedirects(true).socketTimeout(000).connectTimeout(000);
	}

	Hashtable<String,Integer> runBiowin(String smiles) {

		HttpResponse<String> response = Unirest.post("https://episuite.dev/EpiWebServices/Biowin")
				.header("Content-Type", "text/xml")
				.body("<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"> <soap:Body> <ns1:getBiowinResult xmlns:ns1=\"http://epiwebservices.srcinc.com/\"> <smiles>"
						+ smiles + "</smiles> </ns1:getBiowinResult> </soap:Body> </soap:Envelope>\r\n")
				.asString();

		String xml = response.getBody().toString();
//		xml=xml.substring(xml.indexOf("<Models>",xml.length()));

		try {		
			xml = xml.substring(xml.indexOf("<Models><ModelNumber>5"),xml.indexOf("<Models><ModelNumber>6"));
		} catch (Exception ex) {
			return null;
		}
		
//		System.out.println(xml);

		Document doc = Jsoup.parse(xml, "utf-8");
		Hashtable<String,Integer>ht=new Hashtable<>();
		
		for (Element factor : doc.select("Factors")) {
//		    System.out.println(e+"\n\n");
		    
		    String fragmentDescription=factor.select("fragmentdescription").text();
		    
		    if (fragmentDescription.equals("Molecular Weight Parameter") || fragmentDescription.equals("Equation Constant")) continue;
		    
		    int NumberOfFragments=Integer.parseInt(factor.select("NumberOfFragments").text());
//		    System.out.println(fragmentDescription+"\t"+NumberOfFragments);
		    
		    ht.put(fragmentDescription, NumberOfFragments);
		}
//		System.out.println("");
		
		return ht;
		
	}

	/**
	 * Cleaner version that uses jsoup to get ModelNumber 5
	 * 
	 * @param smiles
	 * @return
	 */
	Hashtable<String,Integer> runBiowin2(String smiles) {

		HttpResponse<String> response = Unirest.post("https://episuite.dev/EpiWebServices/Biowin")
				.header("Content-Type", "text/xml")
				.body("<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"> <soap:Body> <ns1:getBiowinResult xmlns:ns1=\"http://epiwebservices.srcinc.com/\"> <smiles>"
						+ smiles + "</smiles> </ns1:getBiowinResult> </soap:Body> </soap:Envelope>\r\n")
				.asString();

		String xml = response.getBody().toString();
		if(xml.indexOf("<ModelNumber>5")==-1) return null;
		
		Document doc = Jsoup.parse(xml, "utf-8");
		Hashtable<String,Integer>ht=new Hashtable<>();
		
		for (Element model : doc.select("Models")) {
			
		    String modelNumber=model.select("ModelNumber").text();
		    
		    if(!modelNumber.equals("5")) continue;
		    			
			for (Element factor : model.select("Factors")) {
//			    System.out.println(e+"\n\n");
			    
			    String fragmentDescription=factor.select("fragmentdescription").text();
			    if (fragmentDescription.equals("Molecular Weight Parameter") || fragmentDescription.equals("Equation Constant")) continue;
			    int NumberOfFragments=Integer.parseInt(factor.select("NumberOfFragments").text());
			    System.out.println(fragmentDescription+"\t"+NumberOfFragments);
			    ht.put(fragmentDescription, NumberOfFragments);
//			    System.out.println(ht);
			}
			return ht;
		}
		return null;
		
	}


	/**
	 * This version stores the MW based on total coefficient in model 5
	 * 
	 * @param smiles
	 * @return
	 */
	public LinkedHashMap<String,Double> runBiowin5(String smiles) {

		double MW_converter=-634.1080267;//constant that converts the total coefficient for MW to a MW value
		
		HttpResponse<String> response = Unirest.post("https://episuite.dev/EpiWebServices/Biowin")
				.header("Content-Type", "text/xml")
				.body("<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"> <soap:Body> <ns1:getBiowinResult xmlns:ns1=\"http://epiwebservices.srcinc.com/\"> <smiles>"
						+ smiles + "</smiles> </ns1:getBiowinResult> </soap:Body> </soap:Envelope>\r\n")
				.asString();

		String xml = response.getBody().toString();
		if(xml.indexOf("<ModelNumber>5")==-1) return null;
		
		Document doc = Jsoup.parse(xml, "utf-8");
		LinkedHashMap<String,Double>ht=new LinkedHashMap<>();
		
		for (Element model : doc.select("Models")) {
			
		    String modelNumber=model.select("ModelNumber").text();
		    
		    if(!modelNumber.equals("5")) continue;
		    			
			for (Element factor : model.select("Factors")) {
//			    System.out.println(e+"\n\n");
			    
			    String fragmentDescription=factor.select("fragmentdescription").text();

			    if (fragmentDescription.equals("Equation Constant")) continue;
			    
			    if (fragmentDescription.equals("Molecular Weight Parameter")) {
				    double TotalCoefficient=Double.parseDouble(factor.select("TotalCoefficient").text());
			    	double MW=TotalCoefficient*MW_converter;
				    ht.put("MW", MW);			    	
			    } else {
				    double NumberOfFragments=Double.parseDouble(factor.select("NumberOfFragments").text());
//				    System.out.println(fragmentDescription+"\t"+NumberOfFragments);
				    ht.put(fragmentDescription, NumberOfFragments);
			    }
//			    System.out.println(ht);
			}
			return ht;
		}
		return null;
		
	}
	
	
	public Double getBiowin3PredictionSoap(String smiles) {

		HttpResponse<String> response = Unirest.post("https://episuite.app/EpiWebServices/Biowin")
				.header("Content-Type", "text/xml")
				.body("<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"> <soap:Body> <ns1:getBiowinResult xmlns:ns1=\"http://epiwebservices.srcinc.com/\"> <smiles>"
						+ smiles + "</smiles> </ns1:getBiowinResult> </soap:Body> </soap:Envelope>\r\n")
				.asString();

		String xml = response.getBody().toString();
		if(xml.indexOf("<ModelNumber>3")==-1) return null;
		
		Document doc = Jsoup.parse(xml, "utf-8");
		Hashtable<String,Double>ht=new Hashtable<>();
		
		for (Element model : doc.select("Models")) {
		    String modelNumber=model.select("ModelNumber").text();
		    if(!modelNumber.equals("3")) continue;

		    double pred=0;
		    for (Element factor : model.select("Factors")) {
//			    System.out.println(e+"\n\n");
			    
			    double TC=Double.parseDouble(factor.select("TotalCoefficient").text());
			    pred+=TC;//calculate using sum of total coefficients- BiowinValue field has too much round off error
			}
		    
		    
			return pred;
		}
		return null;
	}
	
	
	public Double getBiowin3Local(String smiles) {
		HttpResponse<String> response = Unirest.get("http://localhost:9000/biowin")
				 .header("Content-Type", "application/json")
				  .queryString("smiles", smiles)
				  .asString();
		String json = response.getBody().toString();
		Gson gson=new Gson();
		
//		System.out.println(json);
		
		if(json.equals("Internal Server Error")) return null;
		
		BiodegradationRateResults b=gson.fromJson(json,BiodegradationRateResults.class);
		return RunBiowinFromJava.getBiowinResult(b, 3);
	}
	

	/**
	 * This version uses molecular weight stored in the input file
	 * 
	 */
	void goThroughCSV() {
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\0000 biodegradation OPPT\\biodegradation\\biowin update\\";
		String filepath=folder+"smiles mw.txt";
//		String filepath=folder+"smiles+index.txt";
//		String folder="data/biodeg/";
//		String filepath=folder+"unique rifm smiles.txt";
		
		FileInputStream fis;

		try {

			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			List<Hashtable<String,Integer>>values=new ArrayList<>();
			List<String>smilesList=new ArrayList<>();
			List<String>mwList=new ArrayList<>();

			br.readLine();//header, discard

			//***********************************************************************
			//Run chemicals through the API:
			int counter=0;
			while (true) {
				String line=br.readLine();//each line has smiles and MW				
				if(line==null)break;

				System.out.println(++counter);
				
				String smiles=line.substring(0,line.indexOf("\t"));
				String MW=line.substring(line.indexOf("\t")+1,line.length());
				
				smilesList.add(smiles);
				mwList.add(MW);
				Hashtable<String,Integer>ht=runBiowin2(smiles);
				values.add(ht);
			}
			
			List<String>fragmentNames=new ArrayList<>();

			//***********************************************************************
			//Get the list of possible fragment names for the chemicals:
			for(Hashtable<String,Integer>ht:values) {
				if(ht!=null) {
					for (String name:ht.keySet()) {
						if(!fragmentNames.contains(name)) fragmentNames.add(name);
					}
				}
			}
			Collections.sort(fragmentNames);

			//***********************************************************************
			//Write out the fragment counts for the list of chemicals:
			FileWriter fw=new FileWriter(folder+"fragcounts.txt");
			fw.write("SMILES\t");
			for(int i=0;i<fragmentNames.size();i++) {
				fw.write(fragmentNames.get(i));
				fw.write("\t");
			}
			fw.write("MW\n");
			
			for (int i=0;i<values.size();i++) {

				fw.write(smilesList.get(i)+"\t");

				Hashtable<String,Integer>ht=values.get(i);

				if(ht==null) {

					for(int j=0;j<fragmentNames.size();j++) {
						fw.write("N/A\t");
					}
					
				} else {
					for(int j=0;j<fragmentNames.size();j++) {
						
						if(ht.containsKey(fragmentNames.get(j))) {
							int count=ht.get(fragmentNames.get(j));						
							fw.write(count+"");						
						} else {
							fw.write("0");
						}
						
						fw.write("\t");
					}
					
				}
								
				fw.write(mwList.get(i)+"\n");
				fw.flush();
			}
			fw.flush();
			br.close();
			fw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * This version figures out MW based on the total coefficient for molecular weight in model number 5
	 * 
	 * assumes file has smiles\tID\tTox format
	 * 
	 */
	public void goThroughCSV_getMW_from_model5_factor(String filepathInput,String filepathOutput) {
		
//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\0000 biodegradation OPPT\\biodegradation\\biowin update\\";
////		String filepath=folder+"smiles mw.txt";
//		String filepath=folder+"smiles+index.txt";
		
//		String folder="data/biodeg/";
//		String filepath=folder+"unique rifm smiles.txt";
		
//		String filePathOut=filepath.replace(".txt", " episuite fragment output.txt");
		
		try {

			BufferedReader br=new BufferedReader(new FileReader(filepathInput));
			
			List<LinkedHashMap<String,Double>>fragValues=new ArrayList<>();
			List<String>smilesList=new ArrayList<>();
			List<String>idList=new ArrayList<>();
			List<String>toxList=new ArrayList<>();
			

			br.readLine();//header, discard

			//***********************************************************************
			//Run chemicals through the API:
			int counter=0;
			while (true) {
				String line=br.readLine();//each line has smiles and MW				
				if(line==null)break;

				System.out.println(++counter);
				
				String [] values=line.split("\t");
				
				String smiles=values[0];
				String id=values[1];
				String tox=values[2];
				
				smilesList.add(smiles);
				idList.add(id);
				toxList.add(tox);
				
				LinkedHashMap<String,Double>ht=runBiowin5(smiles);
				fragValues.add(ht);
			}
			
			List<String>fragmentNames=new ArrayList<>();

			//***********************************************************************
			//Get the list of possible fragment names for the chemicals:
			for(LinkedHashMap<String,Double>ht:fragValues) {
				if(ht!=null) {
					for (String name:ht.keySet()) {
						if(!fragmentNames.contains(name)) fragmentNames.add(name);
					}
				}
			}
			Collections.sort(fragmentNames);
			
			fragmentNames.remove("MW");
			fragmentNames.add("MW");//put at the end
			//***********************************************************************
			//Write out the fragment counts for the list of chemicals:
			FileWriter fw=new FileWriter(filepathOutput);
			fw.write("SMILES\tTox\t");
			for(int j=0;j<fragmentNames.size();j++) {
				fw.write(fragmentNames.get(j));
				if(j<fragmentNames.size()-1) fw.write("\t");
			}
			fw.write("\n");
			
			for (int i=0;i<fragValues.size();i++) {

				fw.write(smilesList.get(i)+"\t"+toxList.get(i)+"\t");

				LinkedHashMap<String,Double>ht=fragValues.get(i);

				if(ht==null) {

					for(int j=0;j<fragmentNames.size();j++) {
						fw.write("N/A");
						if(j<fragmentNames.size()-1) fw.write("\t");
					}
					
				} else {
					for(int j=0;j<fragmentNames.size();j++) {
						
						if(ht.containsKey(fragmentNames.get(j))) {
							double count=ht.get(fragmentNames.get(j));						
							fw.write(count+"");						
						} else {
							fw.write("0");
						}
						
						if(j<fragmentNames.size()-1) fw.write("\t");
					}
					
				}
								
				fw.write("\n");
				fw.flush();
			}
			fw.flush();
			br.close();
			fw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
		
	public static void main(String[] args) {
		GetBiodegFragmentCounts g = new GetBiodegFragmentCounts();

//		g.runBiowin3("CCO");
		
//		g.goThroughCSV();
		
		
//		for (int i=1;i<=5;i++) {
//			long t1=System.currentTimeMillis();
//			Double pred=g.getBiowin3PredictionSoap("CCO");
//			long t2=System.currentTimeMillis();
//			System.out.println(pred+"\t"+(t2-t1)+"\tmilliseconds");
//			
//		}
		
		
	}

}
