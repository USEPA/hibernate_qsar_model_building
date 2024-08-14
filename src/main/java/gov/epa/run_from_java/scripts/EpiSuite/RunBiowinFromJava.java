package gov.epa.run_from_java.scripts.EpiSuite;

import com.srcinc.episuite.biodegradationrate.BiodegradationRateParameters;
import com.srcinc.episuite.biodegradationrate.BiodegradationRateResults;

import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

import java.sql.Connection;
import java.sql.DriverManager;

import com.srcinc.episuite.EpiSuite;
import com.srcinc.episuite.EpiSuiteParameters;
import com.srcinc.episuite.EpiSuiteResults;
import com.srcinc.episuite.biodegradationrate.BiodegradationRate;
import com.srcinc.episuite.biodegradationrate.BiodegradationRateModel;


/**
* @author TMARTI02
*/
public class RunBiowinFromJava {
	
	
	public static void runAllFromJava(String smiles) {
		EpiSuiteParameters parameters = new EpiSuiteParameters();
		try {
			String dbpath="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\episuite\\EPI_Unified.sqlite";
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbpath);
//			System.out.println(conn==null);
			
			parameters.setEpiConnection(conn);//TODO needs this work 
			parameters.setSmiles("CCO");

			EpiSuiteResults results = EpiSuite.getResults(parameters);
			System.out.println(Utilities.gson.toJson(results));
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	

	public static BiodegradationRateResults runFromJava(String smiles) {
		
		 BiodegradationRateParameters parameters = new BiodegradationRateParameters(smiles, false);

		 try {
			BiodegradationRateResults biodegradationRateResults = BiodegradationRate.getResults(parameters);
			
			System.out.println(Utilities.gson.toJson(biodegradationRateResults));
			
			System.out.println(biodegradationRateResults.getModels().get(2).getValue());
			
			return biodegradationRateResults;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			return null;
		}
        
	}
	
	public static BiodegradationRateResults runFromJava2(String smiles) {
		
		 BiodegradationRateParameters parameters = new BiodegradationRateParameters(smiles, false);

		 try {
			BiodegradationRateResults biodegradationRateResults = BiodegradationRate.getResults(parameters);
			
//			System.out.println(Utilities.gson.toJson(biodegradationRateResults));
			
			return biodegradationRateResults;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			return null;
		}
       
	}
	
	public static Double getBiowinResult(BiodegradationRateResults biodegradationRateResults,int modelNumber) {
		
		if (biodegradationRateResults==null) return null;
		
		for(BiodegradationRateModel model: biodegradationRateResults.getModels()) {
			if(model.getNumber()==modelNumber) {
				return model.getValue();
			}
		}
		
		return null;
		
	}
	
	
	public static void main(String[] args) {

//		String smiles="CC1=CC=CC=C1NC(=O)C1=CC=CC=C1C";
//		String smiles="OO";
//		String smiles="C1=CC23C=CC45C=CC67C=CC11C=CC89C=CC%10(C=C2)C=CC2(C=C4)C=CC(C=C6)(C=C8)C46C77C11C33C57C24C%103C961";
//
//		String line="[Li].[Pb] |^1:0| DTXSID50726662";
//		
//		if(line.contains("| ")) {
//			smiles=line.substring(0,line.indexOf("| ")).trim();
//		}
//		
//		System.out.println(smiles);
		
//		long t1=System.currentTimeMillis();
//		BiodegradationRateResults biodegradationRateResults=RunBiowinFromJava.runFromJava(smiles);
//		Double predBiowin3=RunBiowinFromJava.getBiowinResult(biodegradationRateResults,3);
//		long t2=System.currentTimeMillis();
//		System.out.println(predBiowin3+"\t"+(t2-t1)+" milliseconds");
		
//		for (int i=1;i<=10;i++) {
//			String smiles="CC1=CC=CC=C1NC(=O)C1=CC=CC=C1C";
//			long t1=System.currentTimeMillis();
//			BiodegradationRateResults biodegradationRateResults=RunBiowinFromJava.runFromJava(smiles);
//			Double predBiowin3=RunBiowinFromJava.getBiowinResult(biodegradationRateResults,3);
//			long t2=System.currentTimeMillis();
//			System.out.println(predBiowin3+"\t"+(t2-t1)+" milliseconds");
//		}
//		RunBiowinFromJava.runAllFromJava("[H][C@@]12C[C@@H](O)C(=O)[C@@]1(C)CC[C@]1([H])C3=CC=C(O)C=C3CC[C@@]21[H]");
		
		runFromJava("[H][C@@]12C[C@@H](O)C(=O)[C@@]1(C)CC[C@]1([H])C3=CC=C(O)C=C3CC[C@@]21[H]");
		
	}

	
	
}
