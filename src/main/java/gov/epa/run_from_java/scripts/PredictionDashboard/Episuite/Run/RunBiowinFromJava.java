package gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run;

import com.srcinc.episuite.biodegradationrate.BiodegradationRateParameters;
import com.srcinc.episuite.biodegradationrate.BiodegradationRateResults;

import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.OPERA.RecordToxValModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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


	//	void runUsingWebService(String method) {
	//		GetBiodegFragmentCounts g=new GetBiodegFragmentCounts();
	//		EpisuiteWebserviceScript e=new EpisuiteWebserviceScript ();
	//		
	//		try {
	//			
	//			String folder="data\\dsstox\\smi\\";
	//			String outputFilePath=folder+"biowin3 output-local.txt";
	//						
	//			BufferedReader br=new BufferedReader(new FileReader(folder+"snapshot_compounds.txt"));
	//			
	//			if(!new File(outputFilePath).exists()) {
	//				FileWriter fw=new FileWriter(outputFilePath);
	//				fw.write("dtxsid\tsmiles\tpred_BIOWIN3\r\n");
	//				fw.flush();
	//				fw.close();
	//			}
	//			
	//			
	//			FileWriter fw=new FileWriter(outputFilePath,true);
	//			
	//			int count=0;
	//			
	//			BufferedReader brOut=new BufferedReader(new FileReader(outputFilePath));
	//			brOut.readLine();
	//			
	//			String dtxsidLast=null;
	//			while (true) {
	//				String Line=brOut.readLine();
	//				if(Line==null) break;
	//				String [] vals=Line.split("\t");
	//				dtxsidLast=vals[0];
	//			}
	//			brOut.close();
	//			System.out.println("Last dtxsid="+dtxsidLast);
	//			
	//			boolean start=false;
	//
	//			long t1=System.currentTimeMillis();
	//			
	//			while (true) {
	//				String Line=br.readLine();
	//				if(Line==null) break;
	//				count++;
	//				
	////				System.out.println(Line);
	//				
	//				String [] vals=Line.split(" ");
	//				
	//				String smiles=vals[0];
	//				String dtxsid=vals[1];
	//				
	//				if(dtxsid.equals(dtxsidLast)) {
	//					start=true;
	//					continue;
	//				}
	//				
	//				
	//				if(dtxsidLast!=null && !start) continue;
	//				
	//				Double pred=null;
	//				
	//				if(method.equals("soap")) {
	//					pred=g.getBiowin3PredictionSoap(smiles);
	//				} else if (method.equals("full")) {
	//					String json=e.runEpiwin(smiles);
	//					if(!json.contains("error")) {
	//						pred=e.getBiowin3(json);						
	//					}
	//				} else if (method.equals("local")) {
	//					pred=g.getBiowin3Local(smiles);
	//				}
	//				
	//				System.out.println(smiles+"\t"+pred);
	//				
	//
	//				if (count%1000==0) {
	//					long t2=System.currentTimeMillis();
	//					System.out.println(count+"\t"+(t2-t1)/1e6+" secs per chemical");
	//					t1=System.currentTimeMillis();
	//				}
	//				
	//				
	//				fw.write(dtxsid+"\t"+smiles+"\t"+pred+"\r\n");
	//				fw.flush();
	//				
	////				if(count==10)break;
	//				
	////				TimeUnit.MILLISECONDS.sleep(100);
	//			}
	//			fw.close();
	//			
	//		} catch (Exception ex) {
	//			ex.printStackTrace();
	//		}
	//		
	//	}



	/**
	 * Figures out list of dtxsids that were ran and then skips those
	 * 
	 */
	void runBiowin3ForSmilesList(String method,String filename) {
		GetBiodegFragmentCounts g=new GetBiodegFragmentCounts();
		EpisuiteWebserviceScript e=new EpisuiteWebserviceScript ();

		try {

			String folder="data\\dsstox\\smi\\";
			String outputFilePath="data\\episuite\\"+filename;

			BufferedReader br=new BufferedReader(new FileReader(folder+"snapshot_compounds.txt"));

			if(!new File(outputFilePath).exists()) {
				FileWriter fw=new FileWriter(outputFilePath);
				fw.write("dtxsid\tsmiles\tpred_BIOWIN3\r\n");
				fw.flush();
				fw.close();
			}


			FileWriter fw=new FileWriter(outputFilePath,true);

			int count=0;

			BufferedReader brOut=new BufferedReader(new FileReader(outputFilePath));
			brOut.readLine();

			Set<String>dtxsids=new HashSet<String>();

			while (true) {
				String Line=brOut.readLine();
				if(Line==null) break;
				String [] vals=Line.split("\t");
				dtxsids.add(vals[0]);
			}
			brOut.close();

			System.out.println(dtxsids.size()+"\talready ran");

			Long t0=null;
			Long t1=null;


			while (true) {
				String Line=br.readLine();
				if(Line==null) break;

				//				System.out.println(Line);

				String [] vals=Line.split(" ");

				String smiles=vals[0];
				String dtxsid=vals[1];

				if(Line.contains("| ")) {
					smiles=Line.substring(0,Line.indexOf("| ")).trim();
					dtxsid=Line.substring(Line.indexOf("| ")+2,Line.length()).trim();

					//					System.out.println(dtxsid);
					//					System.out.println(smiles+"\n");
				}


				//				System.out.println(dtxsid+"\t"+smiles);

				//				if (count%10000==0) {
				//					System.out.println(count);
				//				}

				if(dtxsid.equals("DTXSID601337444")) continue;


				if(dtxsids.contains(dtxsid)) {
					//					System.out.println("Skipping "+dtxsid);
					continue;
				}

				count++;

				if(t0==null) {
					t0=System.currentTimeMillis();
					t1=t0;
				}

				//				System.out.println(dtxsid+"\t"+smiles);

				Double predBiowin3=null;

				if(method.equals("soap")) {
					predBiowin3=g.getBiowin3PredictionSoap(smiles);
				} else if (method.equals("full")) {
					String json=e.runEpiwin(smiles);
					if(!json.contains("error")) {
						predBiowin3=e.getBiowin3(json);						
					}
				} else if (method.equals("local")) {
					predBiowin3=g.getBiowin3Local(smiles);
				} else if (method.equals("java")) {
					BiodegradationRateResults biodegradationRateResults=runFromJava(smiles);
					predBiowin3=RunBiowinFromJava.getBiowinResult(biodegradationRateResults,3);
				}


				if (count%100==0) {
					long t2=System.currentTimeMillis();
					//					System.out.println(count+"\t"+(t2-t1)/1e3+" millisecs per chemical");

					double time2=(t2-t1)/1000.0;
					double time=(t2-t0)/(double)count;

					System.out.println(count+"\t"+time+" millisecs per chemical overall");
					//					System.out.println(count+"\t"+time2+" millisecs per chemical for the batch");
					t1=System.currentTimeMillis();
				}

				//				System.out.println(dtxsid+"\t"+predBiowin3);

				fw.write(dtxsid+"\t"+smiles+"\t"+predBiowin3+"\r\n");
				fw.flush();

				//				if(count==10)break;

				//				TimeUnit.MILLISECONDS.sleep(100);
			}
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}


	//	void runUsingWebService(String method) {
	//		GetBiodegFragmentCounts g=new GetBiodegFragmentCounts();
	//		EpisuiteWebserviceScript e=new EpisuiteWebserviceScript ();
	//		
	//		try {
	//			
	//			String folder="data\\dsstox\\smi\\";
	//			String outputFilePath=folder+"biowin3 output-local.txt";
	//						
	//			BufferedReader br=new BufferedReader(new FileReader(folder+"snapshot_compounds.txt"));
	//			
	//			if(!new File(outputFilePath).exists()) {
	//				FileWriter fw=new FileWriter(outputFilePath);
	//				fw.write("dtxsid\tsmiles\tpred_BIOWIN3\r\n");
	//				fw.flush();
	//				fw.close();
	//			}
	//			
	//			
	//			FileWriter fw=new FileWriter(outputFilePath,true);
	//			
	//			int count=0;
	//			
	//			BufferedReader brOut=new BufferedReader(new FileReader(outputFilePath));
	//			brOut.readLine();
	//			
	//			String dtxsidLast=null;
	//			while (true) {
	//				String Line=brOut.readLine();
	//				if(Line==null) break;
	//				String [] vals=Line.split("\t");
	//				dtxsidLast=vals[0];
	//			}
	//			brOut.close();
	//			System.out.println("Last dtxsid="+dtxsidLast);
	//			
	//			boolean start=false;
	//
	//			long t1=System.currentTimeMillis();
	//			
	//			while (true) {
	//				String Line=br.readLine();
	//				if(Line==null) break;
	//				count++;
	//				
	////				System.out.println(Line);
	//				
	//				String [] vals=Line.split(" ");
	//				
	//				String smiles=vals[0];
	//				String dtxsid=vals[1];
	//				
	//				if(dtxsid.equals(dtxsidLast)) {
	//					start=true;
	//					continue;
	//				}
	//				
	//				
	//				if(dtxsidLast!=null && !start) continue;
	//				
	//				Double pred=null;
	//				
	//				if(method.equals("soap")) {
	//					pred=g.getBiowin3PredictionSoap(smiles);
	//				} else if (method.equals("full")) {
	//					String json=e.runEpiwin(smiles);
	//					if(!json.contains("error")) {
	//						pred=e.getBiowin3(json);						
	//					}
	//				} else if (method.equals("local")) {
	//					pred=g.getBiowin3Local(smiles);
	//				}
	//				
	//				System.out.println(smiles+"\t"+pred);
	//				
	//
	//				if (count%1000==0) {
	//					long t2=System.currentTimeMillis();
	//					System.out.println(count+"\t"+(t2-t1)/1e6+" secs per chemical");
	//					t1=System.currentTimeMillis();
	//				}
	//				
	//				
	//				fw.write(dtxsid+"\t"+smiles+"\t"+pred+"\r\n");
	//				fw.flush();
	//				
	////				if(count==10)break;
	//				
	////				TimeUnit.MILLISECONDS.sleep(100);
	//			}
	//			fw.close();
	//			
	//		} catch (Exception ex) {
	//			ex.printStackTrace();
	//		}
	//		
	//	}





	//	void runUsingWebService(String method) {
	//		GetBiodegFragmentCounts g=new GetBiodegFragmentCounts();
	//		EpisuiteWebserviceScript e=new EpisuiteWebserviceScript ();
	//		
	//		try {
	//			
	//			String folder="data\\dsstox\\smi\\";
	//			String outputFilePath=folder+"biowin3 output-local.txt";
	//						
	//			BufferedReader br=new BufferedReader(new FileReader(folder+"snapshot_compounds.txt"));
	//			
	//			if(!new File(outputFilePath).exists()) {
	//				FileWriter fw=new FileWriter(outputFilePath);
	//				fw.write("dtxsid\tsmiles\tpred_BIOWIN3\r\n");
	//				fw.flush();
	//				fw.close();
	//			}
	//			
	//			
	//			FileWriter fw=new FileWriter(outputFilePath,true);
	//			
	//			int count=0;
	//			
	//			BufferedReader brOut=new BufferedReader(new FileReader(outputFilePath));
	//			brOut.readLine();
	//			
	//			String dtxsidLast=null;
	//			while (true) {
	//				String Line=brOut.readLine();
	//				if(Line==null) break;
	//				String [] vals=Line.split("\t");
	//				dtxsidLast=vals[0];
	//			}
	//			brOut.close();
	//			System.out.println("Last dtxsid="+dtxsidLast);
	//			
	//			boolean start=false;
	//
	//			long t1=System.currentTimeMillis();
	//			
	//			while (true) {
	//				String Line=br.readLine();
	//				if(Line==null) break;
	//				count++;
	//				
	////				System.out.println(Line);
	//				
	//				String [] vals=Line.split(" ");
	//				
	//				String smiles=vals[0];
	//				String dtxsid=vals[1];
	//				
	//				if(dtxsid.equals(dtxsidLast)) {
	//					start=true;
	//					continue;
	//				}
	//				
	//				
	//				if(dtxsidLast!=null && !start) continue;
	//				
	//				Double pred=null;
	//				
	//				if(method.equals("soap")) {
	//					pred=g.getBiowin3PredictionSoap(smiles);
	//				} else if (method.equals("full")) {
	//					String json=e.runEpiwin(smiles);
	//					if(!json.contains("error")) {
	//						pred=e.getBiowin3(json);						
	//					}
	//				} else if (method.equals("local")) {
	//					pred=g.getBiowin3Local(smiles);
	//				}
	//				
	//				System.out.println(smiles+"\t"+pred);
	//				
	//
	//				if (count%1000==0) {
	//					long t2=System.currentTimeMillis();
	//					System.out.println(count+"\t"+(t2-t1)/1e6+" secs per chemical");
	//					t1=System.currentTimeMillis();
	//				}
	//				
	//				
	//				fw.write(dtxsid+"\t"+smiles+"\t"+pred+"\r\n");
	//				fw.flush();
	//				
	////				if(count==10)break;
	//				
	////				TimeUnit.MILLISECONDS.sleep(100);
	//			}
	//			fw.close();
	//			
	//		} catch (Exception ex) {
	//			ex.printStackTrace();
	//		}
	//		
	//	}





	//	void runUsingWebService(String method) {
	//		GetBiodegFragmentCounts g=new GetBiodegFragmentCounts();
	//		EpisuiteWebserviceScript e=new EpisuiteWebserviceScript ();
	//		
	//		try {
	//			
	//			String folder="data\\dsstox\\smi\\";
	//			String outputFilePath=folder+"biowin3 output-local.txt";
	//						
	//			BufferedReader br=new BufferedReader(new FileReader(folder+"snapshot_compounds.txt"));
	//			
	//			if(!new File(outputFilePath).exists()) {
	//				FileWriter fw=new FileWriter(outputFilePath);
	//				fw.write("dtxsid\tsmiles\tpred_BIOWIN3\r\n");
	//				fw.flush();
	//				fw.close();
	//			}
	//			
	//			
	//			FileWriter fw=new FileWriter(outputFilePath,true);
	//			
	//			int count=0;
	//			
	//			BufferedReader brOut=new BufferedReader(new FileReader(outputFilePath));
	//			brOut.readLine();
	//			
	//			String dtxsidLast=null;
	//			while (true) {
	//				String Line=brOut.readLine();
	//				if(Line==null) break;
	//				String [] vals=Line.split("\t");
	//				dtxsidLast=vals[0];
	//			}
	//			brOut.close();
	//			System.out.println("Last dtxsid="+dtxsidLast);
	//			
	//			boolean start=false;
	//
	//			long t1=System.currentTimeMillis();
	//			
	//			while (true) {
	//				String Line=br.readLine();
	//				if(Line==null) break;
	//				count++;
	//				
	////				System.out.println(Line);
	//				
	//				String [] vals=Line.split(" ");
	//				
	//				String smiles=vals[0];
	//				String dtxsid=vals[1];
	//				
	//				if(dtxsid.equals(dtxsidLast)) {
	//					start=true;
	//					continue;
	//				}
	//				
	//				
	//				if(dtxsidLast!=null && !start) continue;
	//				
	//				Double pred=null;
	//				
	//				if(method.equals("soap")) {
	//					pred=g.getBiowin3PredictionSoap(smiles);
	//				} else if (method.equals("full")) {
	//					String json=e.runEpiwin(smiles);
	//					if(!json.contains("error")) {
	//						pred=e.getBiowin3(json);						
	//					}
	//				} else if (method.equals("local")) {
	//					pred=g.getBiowin3Local(smiles);
	//				}
	//				
	//				System.out.println(smiles+"\t"+pred);
	//				
	//
	//				if (count%1000==0) {
	//					long t2=System.currentTimeMillis();
	//					System.out.println(count+"\t"+(t2-t1)/1e6+" secs per chemical");
	//					t1=System.currentTimeMillis();
	//				}
	//				
	//				
	//				fw.write(dtxsid+"\t"+smiles+"\t"+pred+"\r\n");
	//				fw.flush();
	//				
	////				if(count==10)break;
	//				
	////				TimeUnit.MILLISECONDS.sleep(100);
	//			}
	//			fw.close();
	//			
	//		} catch (Exception ex) {
	//			ex.printStackTrace();
	//		}
	//		
	//	}



	void runBiowin3ForSmilesListMultithreaded(String method,String filename) {
		GetBiodegFragmentCounts g=new GetBiodegFragmentCounts();
		EpisuiteWebserviceScript e=new EpisuiteWebserviceScript ();

		try {

			ExecutorService es = Executors.newFixedThreadPool(10);

			String folder="data\\dsstox\\smi\\";
			String outputFilePath="data\\episuite\\"+filename;

			BufferedReader br=new BufferedReader(new FileReader(folder+"snapshot_compounds.txt"));

			if(!new File(outputFilePath).exists()) {
				FileWriter fw=new FileWriter(outputFilePath);
				fw.write("dtxsid\tsmiles\tpred_BIOWIN3\r\n");
				fw.flush();
				fw.close();
			}


			FileWriter fw=new FileWriter(outputFilePath,true);

			int count=0;

			BufferedReader brOut=new BufferedReader(new FileReader(outputFilePath));
			brOut.readLine();

			Set<String>dtxsids=new HashSet<String>();

			while (true) {
				String Line=brOut.readLine();
				if(Line==null) break;
				String [] vals=Line.split("\t");
				dtxsids.add(vals[0]);
			}
			brOut.close();

			System.out.println(dtxsids.size()+"\talready ran");

			//			Long t1=null;

			List<String>smilesList=new ArrayList<>();
			List<String>dtxsidList=new ArrayList<>();

			while (true) {
				String Line=br.readLine();
				if(Line==null) break;

				//				System.out.println(Line);

				String [] vals=Line.split(" ");

				String smiles=vals[0];
				String dtxsid=vals[1];

				if(Line.contains("| ")) {
					smiles=Line.substring(0,Line.indexOf("| ")).trim();
					dtxsid=Line.substring(Line.indexOf("| ")+2,Line.length()).trim();
				}

				if(dtxsid.equals("DTXSID601337444")) continue;

				if(dtxsids.contains(dtxsid)) {
					//					System.out.println("Skipping "+dtxsid);
					continue;
				}

				count++;


				smilesList.add(smiles);
				dtxsidList.add(dtxsid);

				if(count==100000) break;

				//				if(count==10)break;
				//				TimeUnit.MILLISECONDS.sleep(100);
			}

			es.submit(() -> {
				for (int i=0;i<smilesList.size();i++) {
					try {
						runChemical(method, g, e, fw, smilesList.get(i), dtxsidList.get(i));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});

			es.shutdown();
			//			fw.close();


		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}


	//	void runUsingWebService(String method) {
	//		GetBiodegFragmentCounts g=new GetBiodegFragmentCounts();
	//		EpisuiteWebserviceScript e=new EpisuiteWebserviceScript ();
	//		
	//		try {
	//			
	//			String folder="data\\dsstox\\smi\\";
	//			String outputFilePath=folder+"biowin3 output-local.txt";
	//						
	//			BufferedReader br=new BufferedReader(new FileReader(folder+"snapshot_compounds.txt"));
	//			
	//			if(!new File(outputFilePath).exists()) {
	//				FileWriter fw=new FileWriter(outputFilePath);
	//				fw.write("dtxsid\tsmiles\tpred_BIOWIN3\r\n");
	//				fw.flush();
	//				fw.close();
	//			}
	//			
	//			
	//			FileWriter fw=new FileWriter(outputFilePath,true);
	//			
	//			int count=0;
	//			
	//			BufferedReader brOut=new BufferedReader(new FileReader(outputFilePath));
	//			brOut.readLine();
	//			
	//			String dtxsidLast=null;
	//			while (true) {
	//				String Line=brOut.readLine();
	//				if(Line==null) break;
	//				String [] vals=Line.split("\t");
	//				dtxsidLast=vals[0];
	//			}
	//			brOut.close();
	//			System.out.println("Last dtxsid="+dtxsidLast);
	//			
	//			boolean start=false;
	//
	//			long t1=System.currentTimeMillis();
	//			
	//			while (true) {
	//				String Line=br.readLine();
	//				if(Line==null) break;
	//				count++;
	//				
	////				System.out.println(Line);
	//				
	//				String [] vals=Line.split(" ");
	//				
	//				String smiles=vals[0];
	//				String dtxsid=vals[1];
	//				
	//				if(dtxsid.equals(dtxsidLast)) {
	//					start=true;
	//					continue;
	//				}
	//				
	//				
	//				if(dtxsidLast!=null && !start) continue;
	//				
	//				Double pred=null;
	//				
	//				if(method.equals("soap")) {
	//					pred=g.getBiowin3PredictionSoap(smiles);
	//				} else if (method.equals("full")) {
	//					String json=e.runEpiwin(smiles);
	//					if(!json.contains("error")) {
	//						pred=e.getBiowin3(json);						
	//					}
	//				} else if (method.equals("local")) {
	//					pred=g.getBiowin3Local(smiles);
	//				}
	//				
	//				System.out.println(smiles+"\t"+pred);
	//				
	//
	//				if (count%1000==0) {
	//					long t2=System.currentTimeMillis();
	//					System.out.println(count+"\t"+(t2-t1)/1e6+" secs per chemical");
	//					t1=System.currentTimeMillis();
	//				}
	//				
	//				
	//				fw.write(dtxsid+"\t"+smiles+"\t"+pred+"\r\n");
	//				fw.flush();
	//				
	////				if(count==10)break;
	//				
	////				TimeUnit.MILLISECONDS.sleep(100);
	//			}
	//			fw.close();
	//			
	//		} catch (Exception ex) {
	//			ex.printStackTrace();
	//		}
	//		
	//	}



	private void runChemical(String method, GetBiodegFragmentCounts g, EpisuiteWebserviceScript e, FileWriter fw,
			int count, Long t0, String smiles, String dtxsid) throws IOException {
		Double predBiowin3=null;

		if(method.equals("soap")) {
			predBiowin3=g.getBiowin3PredictionSoap(smiles);
		} else if (method.equals("full")) {
			String json=e.runEpiwin(smiles);
			if(!json.contains("error")) {
				predBiowin3=e.getBiowin3(json);						
			}
		} else if (method.equals("local")) {
			predBiowin3=g.getBiowin3Local(smiles);
		} else if (method.equals("java")) {
			BiodegradationRateResults biodegradationRateResults=RunBiowinFromJava.runFromJava(smiles);
			predBiowin3=RunBiowinFromJava.getBiowinResult(biodegradationRateResults,3);
		}


		if (count%100==0) {
			long t2=System.currentTimeMillis();
			//					System.out.println(count+"\t"+(t2-t1)/1e3+" millisecs per chemical");

			//					double time2=(t2-t1)/1000.0;
			double time=(t2-t0)/(double)count;

			System.out.println(count+"\t"+time+" millisecs per chemical overall");
			//					System.out.println(count+"\t"+time2+" millisecs per chemical for the batch");
			//					t1=System.currentTimeMillis();
		}

		//				System.out.println(dtxsid+"\t"+predBiowin3);

		fw.write(dtxsid+"\t"+smiles+"\t"+predBiowin3+"\r\n");
		fw.flush();

	}


	//	void runUsingWebService(String method) {
	//		GetBiodegFragmentCounts g=new GetBiodegFragmentCounts();
	//		EpisuiteWebserviceScript e=new EpisuiteWebserviceScript ();
	//		
	//		try {
	//			
	//			String folder="data\\dsstox\\smi\\";
	//			String outputFilePath=folder+"biowin3 output-local.txt";
	//						
	//			BufferedReader br=new BufferedReader(new FileReader(folder+"snapshot_compounds.txt"));
	//			
	//			if(!new File(outputFilePath).exists()) {
	//				FileWriter fw=new FileWriter(outputFilePath);
	//				fw.write("dtxsid\tsmiles\tpred_BIOWIN3\r\n");
	//				fw.flush();
	//				fw.close();
	//			}
	//			
	//			
	//			FileWriter fw=new FileWriter(outputFilePath,true);
	//			
	//			int count=0;
	//			
	//			BufferedReader brOut=new BufferedReader(new FileReader(outputFilePath));
	//			brOut.readLine();
	//			
	//			String dtxsidLast=null;
	//			while (true) {
	//				String Line=brOut.readLine();
	//				if(Line==null) break;
	//				String [] vals=Line.split("\t");
	//				dtxsidLast=vals[0];
	//			}
	//			brOut.close();
	//			System.out.println("Last dtxsid="+dtxsidLast);
	//			
	//			boolean start=false;
	//
	//			long t1=System.currentTimeMillis();
	//			
	//			while (true) {
	//				String Line=br.readLine();
	//				if(Line==null) break;
	//				count++;
	//				
	////				System.out.println(Line);
	//				
	//				String [] vals=Line.split(" ");
	//				
	//				String smiles=vals[0];
	//				String dtxsid=vals[1];
	//				
	//				if(dtxsid.equals(dtxsidLast)) {
	//					start=true;
	//					continue;
	//				}
	//				
	//				
	//				if(dtxsidLast!=null && !start) continue;
	//				
	//				Double pred=null;
	//				
	//				if(method.equals("soap")) {
	//					pred=g.getBiowin3PredictionSoap(smiles);
	//				} else if (method.equals("full")) {
	//					String json=e.runEpiwin(smiles);
	//					if(!json.contains("error")) {
	//						pred=e.getBiowin3(json);						
	//					}
	//				} else if (method.equals("local")) {
	//					pred=g.getBiowin3Local(smiles);
	//				}
	//				
	//				System.out.println(smiles+"\t"+pred);
	//				
	//
	//				if (count%1000==0) {
	//					long t2=System.currentTimeMillis();
	//					System.out.println(count+"\t"+(t2-t1)/1e6+" secs per chemical");
	//					t1=System.currentTimeMillis();
	//				}
	//				
	//				
	//				fw.write(dtxsid+"\t"+smiles+"\t"+pred+"\r\n");
	//				fw.flush();
	//				
	////				if(count==10)break;
	//				
	////				TimeUnit.MILLISECONDS.sleep(100);
	//			}
	//			fw.close();
	//			
	//		} catch (Exception ex) {
	//			ex.printStackTrace();
	//		}
	//		
	//	}






	//	void runUsingWebService(String method) {
	//		GetBiodegFragmentCounts g=new GetBiodegFragmentCounts();
	//		EpisuiteWebserviceScript e=new EpisuiteWebserviceScript ();
	//		
	//		try {
	//			
	//			String folder="data\\dsstox\\smi\\";
	//			String outputFilePath=folder+"biowin3 output-local.txt";
	//						
	//			BufferedReader br=new BufferedReader(new FileReader(folder+"snapshot_compounds.txt"));
	//			
	//			if(!new File(outputFilePath).exists()) {
	//				FileWriter fw=new FileWriter(outputFilePath);
	//				fw.write("dtxsid\tsmiles\tpred_BIOWIN3\r\n");
	//				fw.flush();
	//				fw.close();
	//			}
	//			
	//			
	//			FileWriter fw=new FileWriter(outputFilePath,true);
	//			
	//			int count=0;
	//			
	//			BufferedReader brOut=new BufferedReader(new FileReader(outputFilePath));
	//			brOut.readLine();
	//			
	//			String dtxsidLast=null;
	//			while (true) {
	//				String Line=brOut.readLine();
	//				if(Line==null) break;
	//				String [] vals=Line.split("\t");
	//				dtxsidLast=vals[0];
	//			}
	//			brOut.close();
	//			System.out.println("Last dtxsid="+dtxsidLast);
	//			
	//			boolean start=false;
	//
	//			long t1=System.currentTimeMillis();
	//			
	//			while (true) {
	//				String Line=br.readLine();
	//				if(Line==null) break;
	//				count++;
	//				
	////				System.out.println(Line);
	//				
	//				String [] vals=Line.split(" ");
	//				
	//				String smiles=vals[0];
	//				String dtxsid=vals[1];
	//				
	//				if(dtxsid.equals(dtxsidLast)) {
	//					start=true;
	//					continue;
	//				}
	//				
	//				
	//				if(dtxsidLast!=null && !start) continue;
	//				
	//				Double pred=null;
	//				
	//				if(method.equals("soap")) {
	//					pred=g.getBiowin3PredictionSoap(smiles);
	//				} else if (method.equals("full")) {
	//					String json=e.runEpiwin(smiles);
	//					if(!json.contains("error")) {
	//						pred=e.getBiowin3(json);						
	//					}
	//				} else if (method.equals("local")) {
	//					pred=g.getBiowin3Local(smiles);
	//				}
	//				
	//				System.out.println(smiles+"\t"+pred);
	//				
	//
	//				if (count%1000==0) {
	//					long t2=System.currentTimeMillis();
	//					System.out.println(count+"\t"+(t2-t1)/1e6+" secs per chemical");
	//					t1=System.currentTimeMillis();
	//				}
	//				
	//				
	//				fw.write(dtxsid+"\t"+smiles+"\t"+pred+"\r\n");
	//				fw.flush();
	//				
	////				if(count==10)break;
	//				
	////				TimeUnit.MILLISECONDS.sleep(100);
	//			}
	//			fw.close();
	//			
	//		} catch (Exception ex) {
	//			ex.printStackTrace();
	//		}
	//		
	//	}



	private void runChemical(String method, GetBiodegFragmentCounts g, EpisuiteWebserviceScript e, FileWriter fw,
			String smiles, String dtxsid) throws IOException {
		Double predBiowin3=null;

		if(method.equals("soap")) {
			predBiowin3=g.getBiowin3PredictionSoap(smiles);
		} else if (method.equals("full")) {
			String json=e.runEpiwin(smiles);
			if(!json.contains("error")) {
				predBiowin3=e.getBiowin3(json);						
			}
		} else if (method.equals("local")) {
			predBiowin3=g.getBiowin3Local(smiles);
		} else if (method.equals("java")) {
			BiodegradationRateResults biodegradationRateResults=RunBiowinFromJava.runFromJava(smiles);
			predBiowin3=RunBiowinFromJava.getBiowinResult(biodegradationRateResults,3);
		}

		//				System.out.println(dtxsid+"\t"+predBiowin3);

		fw.write(dtxsid+"\t"+smiles+"\t"+predBiowin3+"\r\n");
		fw.flush();

	}


	void runBioWin() {
		runBiowin3ForSmilesList("java","biowin3 output from Java.txt");
		//			runBiowin3ForSmilesList("local","biowin3 output from local api.txt");
		//			runBiowin3ForSmilesListMultithreaded("local","biowin3 output from local api-multithreaded10.txt");
		//			runBiowin3ForSmilesList("soap","biowin3 output soap.txt");
	}


	void compareBiowinFiles() {
		String filepath1="data\\episuite\\biowin3 output from Java.txt";
		String filepath2="data\\episuite\\biowin3 output.txt";
		Hashtable<String,Double>ht1=getHashtable(filepath1);
	
		Hashtable<String,Double>ht2=getHashtable(filepath2);
	
		System.out.println(ht1.size());
		System.out.println(ht2.size());
	
	
		for(String dtxsid:ht1.keySet()) {
	
			if (ht2.get(dtxsid)==null) {
				System.out.println(dtxsid+"\tNot in ht2");
				continue;
			}
	
			double diff=Math.abs(ht1.get(dtxsid)-ht2.get(dtxsid));
	
			if(diff>0.001)
				System.out.println(dtxsid+"\t"+ht1.get(dtxsid)+"\t"+ht2.get(dtxsid));
		}
	
	}


	void compareResults () {
	
		String folder="data\\episuite\\toxval\\";
	
		//		File fileOld=new File(folder+"BCF BIOWIN3 from toxval v8.tsv");
		File fileOld=new File(folder+"biowin3 output from Java.tsv");
		File fileNew=new File(folder+"episuite BCF BIOWIN3 for toxval models.tsv");
	
	
		List<RecordToxValModel>recsNew=RecordToxValModel.getRecordsFromTSV(fileNew);
		List<RecordToxValModel>recsOld=RecordToxValModel.getRecordsFromTSV(fileOld);
	
	
	
		//		String metric="BCF";
		//		boolean takeLog=true;
	
		String metric="Biodegradation Score";
		boolean takeLog=false;
	
		double tol=0.01;
	
		Hashtable<String,RecordToxValModel>htNew=RecordToxValModel.getHashtable(recsNew, metric);		
		Hashtable<String,RecordToxValModel>htOld=RecordToxValModel.getHashtable(recsOld, metric);
	
		System.out.println("Records old="+htOld.size());
		System.out.println("Records new="+htNew.size());
	
		DecimalFormat df=new DecimalFormat("0.000");
	
		int count=0;
	
		for(String dtxsid:htOld.keySet()) {
			RecordToxValModel rNew=htNew.get(dtxsid);
			RecordToxValModel rOld=htOld.get(dtxsid);
	
			if(rNew==null) continue;
			if(rNew.value==null) continue;
	
	
	
			if(takeLog) {
				double logNew=Math.log10(rNew.value);
				double logOld=Math.log10(rOld.value);
				double diff=Math.abs(logNew-logOld);
	
				if(diff>tol) {
					count++;
					System.out.println(count+"\t"+dtxsid+"\t"+df.format(logNew)+"\t"+df.format(logOld)+"\t"+df.format(diff));
				}
			} else {
	
				if(rOld.value==null) {
					System.out.println(count+"\t"+dtxsid+"\t"+df.format(rNew.value)+"\t"+rOld.value+"\tN/A");
	
				} else {
					double diff=Math.abs(rNew.value-rOld.value);
					if(diff>tol) {
						count++;
						System.out.println(count+"\t"+dtxsid+"\t"+df.format(rNew.value)+"\t"+df.format(rOld.value)+"\t"+df.format(diff));
					}
	
				}
	
	
	
			}
	
	
		}
	
	}


	//	void runUsingWebService(String method) {
	//		GetBiodegFragmentCounts g=new GetBiodegFragmentCounts();
	//		EpisuiteWebserviceScript e=new EpisuiteWebserviceScript ();
	//		
	//		try {
	//			
	//			String folder="data\\dsstox\\smi\\";
	//			String outputFilePath=folder+"biowin3 output-local.txt";
	//						
	//			BufferedReader br=new BufferedReader(new FileReader(folder+"snapshot_compounds.txt"));
	//			
	//			if(!new File(outputFilePath).exists()) {
	//				FileWriter fw=new FileWriter(outputFilePath);
	//				fw.write("dtxsid\tsmiles\tpred_BIOWIN3\r\n");
	//				fw.flush();
	//				fw.close();
	//			}
	//			
	//			
	//			FileWriter fw=new FileWriter(outputFilePath,true);
	//			
	//			int count=0;
	//			
	//			BufferedReader brOut=new BufferedReader(new FileReader(outputFilePath));
	//			brOut.readLine();
	//			
	//			String dtxsidLast=null;
	//			while (true) {
	//				String Line=brOut.readLine();
	//				if(Line==null) break;
	//				String [] vals=Line.split("\t");
	//				dtxsidLast=vals[0];
	//			}
	//			brOut.close();
	//			System.out.println("Last dtxsid="+dtxsidLast);
	//			
	//			boolean start=false;
	//
	//			long t1=System.currentTimeMillis();
	//			
	//			while (true) {
	//				String Line=br.readLine();
	//				if(Line==null) break;
	//				count++;
	//				
	////				System.out.println(Line);
	//				
	//				String [] vals=Line.split(" ");
	//				
	//				String smiles=vals[0];
	//				String dtxsid=vals[1];
	//				
	//				if(dtxsid.equals(dtxsidLast)) {
	//					start=true;
	//					continue;
	//				}
	//				
	//				
	//				if(dtxsidLast!=null && !start) continue;
	//				
	//				Double pred=null;
	//				
	//				if(method.equals("soap")) {
	//					pred=g.getBiowin3PredictionSoap(smiles);
	//				} else if (method.equals("full")) {
	//					String json=e.runEpiwin(smiles);
	//					if(!json.contains("error")) {
	//						pred=e.getBiowin3(json);						
	//					}
	//				} else if (method.equals("local")) {
	//					pred=g.getBiowin3Local(smiles);
	//				}
	//				
	//				System.out.println(smiles+"\t"+pred);
	//				
	//
	//				if (count%1000==0) {
	//					long t2=System.currentTimeMillis();
	//					System.out.println(count+"\t"+(t2-t1)/1e6+" secs per chemical");
	//					t1=System.currentTimeMillis();
	//				}
	//				
	//				
	//				fw.write(dtxsid+"\t"+smiles+"\t"+pred+"\r\n");
	//				fw.flush();
	//				
	////				if(count==10)break;
	//				
	////				TimeUnit.MILLISECONDS.sleep(100);
	//			}
	//			fw.close();
	//			
	//		} catch (Exception ex) {
	//			ex.printStackTrace();
	//		}
	//		
	//	}
	
	
	
	private Hashtable<String, Double> getHashtable(String filepath) {
		Hashtable<String,Double>ht=new Hashtable<String,Double>();
	
	
		try {
	
	
			BufferedReader brOut=new BufferedReader(new FileReader(filepath));
			brOut.readLine();
	
			Set<String>dtxsids=new HashSet<String>();
	
			while (true) {
				String Line=brOut.readLine();
				if(Line==null) break;
				String [] vals=Line.split("\t");
	
				String dtxsid=vals[0];
	
				Double pred=null;
	
				if(!vals[2].equals("null"))	{
					pred=Double.parseDouble(vals[2]);
					ht.put(dtxsid, pred);
					//				System.out.println(dtxsid+"\t"+pred);
				}
	
	
				dtxsids.add(vals[0]);
			}
	
			brOut.close();
	
	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ht;
	}



}
