package gov.epa.run_from_java.scripts.PredictionDashboard.Episuite;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.srcinc.episuite.biodegradationrate.BiodegradationRateResults;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxSnapshot;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport;
import gov.epa.databases.dev_qsar.qsar_models.entity.Source;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxRecordServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxSnapshotServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.MethodServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardService;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceService;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceServiceImpl;
import gov.epa.run_from_java.scripts.EpiSuite.EpisuiteResults;
import gov.epa.run_from_java.scripts.EpiSuite.EpisuiteWebserviceScript;
import gov.epa.run_from_java.scripts.EpiSuite.GetBiodegFragmentCounts;
import gov.epa.run_from_java.scripts.EpiSuite.RunBiowinFromJava;
import gov.epa.run_from_java.scripts.EpiSuite.EpisuiteResults.PropertyResult2;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.CreatorScript;
import gov.epa.run_from_java.scripts.PredictionDashboard.DashboardPredictionUtilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.OPERA.OPERA_lookups;
import gov.epa.run_from_java.scripts.PredictionDashboard.OPERA.RecordToxValModel;
import gov.epa.run_from_java.scripts.PredictionDashboard.valery.ValeryBody;
import gov.epa.run_from_java.scripts.PredictionDashboard.valery.WebTEST2PredictionResponse;
import kong.unirest.HttpResponse;
import kong.unirest.json.JSONObject;
import java.util.concurrent.atomic.AtomicReference;
/**
* @author TMARTI02
*/
public class PredictionResultsEPISUITEScript {

	String userId="tmarti02";
	DsstoxRecordServiceImpl dsstoxRecordService=new  DsstoxRecordServiceImpl();
	DatasetService datasetService = new DatasetServiceImpl();
	ModelService modelService = new ModelServiceImpl();
	MethodServiceImpl methodService=new MethodServiceImpl();
	PredictionDashboardServiceImpl predictionDashboardService=new PredictionDashboardServiceImpl();
	
	String version="API_1.0";
	String lanId="tmarti02";
	
	static Gson gson=new Gson();
	
	private void runPredictionSnapshot() {
//		int maxCount=20;//number of chemicals to run
		int maxCount=20;//number of chemicals to run
		boolean skipMissingSID=true;//skip entries without an SID
		
//		String folderSrc="C:\\Users\\cramslan\\Documents\\code\\java\\hibernate_qsar_modelbuilding\\data\\dsstox\\sdf\\";
		String folderSrc="data\\dsstox\\sdf\\";
		String fileName="snapshot_compounds1";
		String fileNameSDF = fileName + ".sdf";
		String filepathSDF=folderSrc+fileNameSDF;
		String episuiteModelName="Biowin3 (Ultimate Survey Model)";
		
		String strOutputFolder="reports/prediction_json";
		new File(strOutputFolder).mkdirs();
		String propertyAbbreviation = "WS";
		String outputFileName= fileName + "-" + propertyAbbreviation + ".json";
		String destJsonPath=strOutputFolder+File.separator+outputFileName;
		
		
		DsstoxSnapshotServiceImpl snapshotService=new  DsstoxSnapshotServiceImpl();
		DsstoxSnapshot snapshot=snapshotService.findByName("DSSTOX Snapshot 04/23");
		Hashtable<String,Long> htCIDtoDsstoxRecordId=dsstoxRecordService.getRecordIdHashtable(snapshot,"dtxcid");

		// datasetIds.add("44");
		ArrayList<PredictionDashboard> predictionDashboards = runSDF(filepathSDF, destJsonPath, skipMissingSID, maxCount, htCIDtoDsstoxRecordId,episuiteModelName);
		
		String filepathSmi=filepathSDF.replace(".sdf", ".smi");
		
		if(true) return;
				
//		PredictionDashboardService predictionDashboardService = new PredictionDashboardServiceImpl();
//		for (int i = 0; i < predictionDashboards.size(); i++) {
//			try {
//				predictionDashboardService.create(predictionDashboards.get(i));
//			} catch (Exception ex) {
//				continue;
//			}
//		}
	}
	
	/**
	 * Create files for batch running in biowin module
	 * 
	 */
	private void createBatchSmilesFiles() {
//		int maxCount=20;//number of chemicals to run
		int maxCount=-1;//number of chemicals to run
		boolean skipMissingSID=true;//skip entries without an SID- will also ensure that usually has CAS
		boolean skipSalts=false;
		
//		String folderSrc="C:\\Users\\cramslan\\Documents\\code\\java\\hibernate_qsar_modelbuilding\\data\\dsstox\\sdf\\";
		String folderSrc="data\\dsstox\\sdf\\";
		String folderSmi="data\\dsstox\\smi\\";
		
		for (int num=1;num<=35;num++) {
			String fileName="snapshot_compounds"+num;
			String fileNameSDF = fileName + ".sdf";
			String filepathSDF=folderSrc+fileNameSDF;
			String filepathSmi=folderSmi+fileName + ".txt";;
			createBatchFile(filepathSDF, filepathSmi, skipMissingSID, maxCount,skipSalts);
		}
		
	}
	
	private void createBatchSmilesFile() {
//		int maxCount=20;//number of chemicals to run
		int maxCount=-1;//number of chemicals to run
		boolean skipMissingSID=true;//skip entries without an SID- will also ensure that usually has CAS
		boolean skipSalts=false;
		
//		String folderSrc="C:\\Users\\cramslan\\Documents\\code\\java\\hibernate_qsar_modelbuilding\\data\\dsstox\\sdf\\";
		String folderSrc="data\\dsstox\\sdf\\";
		String folderSmi="data\\dsstox\\smi\\";
		
		List<String>linesAll=new ArrayList<String>();
		
		for (int num=1;num<=35;num++) {
			String fileName="snapshot_compounds"+num;
//			System.out.println(fileName);
			String fileNameSDF = fileName + ".sdf";
			String filepathSDF=folderSrc+fileNameSDF;
			List<String>lines=getSmilesLines(filepathSDF, skipMissingSID, maxCount, skipSalts);
			linesAll.addAll(lines);
		}
		
		try {
			FileWriter fw=new FileWriter(folderSmi+"snapshot_compounds.txt");
			for(String line:linesAll) {
				fw.write(line+"\r\n");
				fw.flush();
			}
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	public ArrayList<PredictionDashboard> runSDF(String SDFFilePath, String destJsonPath,
			boolean skipMissingSID,int maxCount,
			Hashtable<String,Long> htCIDtoDsstoxRecordId,String episuiteModelName) {
		ArrayList<PredictionDashboard> predictionDashboards = new ArrayList<>();
		DashboardPredictionUtilities dpu = new DashboardPredictionUtilities();
		
		AtomContainerSet acs= dpu.readSDFV3000(SDFFilePath);
		
		AtomContainerSet acs2 = dpu.filterAtomContainerSet(acs, skipMissingSID, maxCount);

		Iterator<IAtomContainer> iterator= acs2.atomContainers().iterator();

		// if datasets were linked up, this is where we'd get dataset from		
		
		System.out.println(acs2.getAtomContainerCount());

		int count=0;
		
		long modelId=-1;
		
		Model model = modelService.findById(modelId);
		
		while (iterator.hasNext()) {
			count++;
			
			AtomContainer ac=(AtomContainer) iterator.next();
			String smiles=ac.getProperty("smiles");//TODO should we convert ac to smiles or just use smiles in DSSTOX?
			String dtxcid = ac.getProperty("DTXCID");

			if (dtxcid == null) {
				dtxcid = "N/A";
			}
			String dtxsid = ac.getProperty("DTXSID");
			if (dtxsid == null) {
				dtxsid = "N/A";
			}
//			Boolean isSalt = DashboardPredictionUtilities.isSalt(ac);
//			String prediction=null;
//			
//			if (isSalt) {
//				prediction="error: salt";
//			} else {
//				prediction=runEPISUITE(smiles, dtxcid, episuiteModelName);				
//			}

			String prediction=runEPISUITE(smiles, dtxcid, episuiteModelName);
			
//			System.out.println(isSalt);
//			System.out.println("***"+count+"\t"+smiles);
			
//			System.out.println(smiles+"\t"+dtxcid+"\t"+isSalt+"\t"+prediction);
			System.out.println(smiles+"\t"+dtxcid+"\t"+prediction);
			
			PredictionDashboard chemicalPredictionDashboard = createPredictionDashboard(-1, model, dtxcid, htCIDtoDsstoxRecordId);
			predictionDashboards.add(chemicalPredictionDashboard);	
		}

//		System.out.println(Utilities.gson.toJson(allResults));
		//TODO do API call for json Report, store that too
		return predictionDashboards;
	}
	
	public void createBatchFile(String SDFFilePath, String destSmilesPath,
			boolean skipMissingSID,int maxCount,boolean skipSalts) {
		ArrayList<PredictionDashboard> predictionDashboards = new ArrayList<>();
		DashboardPredictionUtilities dpu = new DashboardPredictionUtilities();
		
		AtomContainerSet acs= dpu.readSDFV3000(SDFFilePath);
		
		AtomContainerSet acs2 = dpu.filterAtomContainerSet(acs, skipMissingSID, maxCount);

		Iterator<IAtomContainer> iterator= acs2.atomContainers().iterator();

		// if datasets were linked up, this is where we'd get dataset from		
		
		System.out.println(SDFFilePath+"\t"+acs2.getAtomContainerCount());
		int count=0;
		
		try {

			FileWriter fw=new FileWriter(destSmilesPath);

			while (iterator.hasNext()) {
				count++;
				
				AtomContainer ac=(AtomContainer) iterator.next();
				String smiles=ac.getProperty("smiles");//TODO should we convert ac to smiles or just use smiles in DSSTOX?
				String dtxcid = ac.getProperty("DTXCID");

				if (dtxcid == null) {
					dtxcid = "N/A";
				}
				String dtxsid = ac.getProperty("DTXSID");
				if (dtxsid == null) {
					dtxsid = "N/A";
				}
				
				if(dtxsid.equals("DTXSID6021874")) {
					System.out.println("DTXSID6021874\t"+destSmilesPath);
				}
				
				Boolean isSalt = DashboardPredictionUtilities.isSalt(ac);
			
				if(skipSalts && isSalt) continue;
					
				fw.write(smiles+" "+dtxsid+"\r\n");
			}
			
			fw.flush();
			fw.close();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		

//		System.out.println(Utilities.gson.toJson(allResults));
		//TODO do API call for json Report, store that too
		
	}
	
	public List<String> getSmilesLines(String SDFFilePath, 
			boolean skipMissingSID,int maxCount,boolean skipSalts) {

		DashboardPredictionUtilities dpu = new DashboardPredictionUtilities();
		AtomContainerSet acs= dpu.readSDFV3000(SDFFilePath);
		AtomContainerSet acs2 = dpu.filterAtomContainerSet(acs, skipMissingSID, maxCount);

		Iterator<IAtomContainer> iterator= acs2.atomContainers().iterator();
		System.out.println(SDFFilePath+"\t"+acs2.getAtomContainerCount());

		int count=0;
		List<String>lines=new ArrayList<String>();
		
		try {

			while (iterator.hasNext()) {
				count++;
				AtomContainer ac=(AtomContainer) iterator.next();
				String smiles=ac.getProperty("smiles");//TODO should we convert ac to smiles or just use smiles in DSSTOX?
				String dtxcid = ac.getProperty("DTXCID");

				if (dtxcid == null) {
					dtxcid = "N/A";
				}
				String dtxsid = ac.getProperty("DTXSID");
				if (dtxsid == null) {
					dtxsid = "N/A";
				}
				
				Boolean isSalt = DashboardPredictionUtilities.isSalt(ac);
				if(skipSalts && isSalt) continue;
				lines.add(smiles+" "+dtxsid);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return lines;

//		System.out.println(Utilities.gson.toJson(allResults));
		//TODO do API call for json Report, store that too
		
	}
	
	
	public PredictionDashboard createPredictionDashboard(double prediction,Model model, String dtxcid, Hashtable<String,Long> htCIDtoDsstoxRecordId) {
		PredictionDashboard pd = new PredictionDashboard();
		pd.setCreatedBy(userId);
		
		DsstoxRecord dr=new DsstoxRecord();
		dr.setId(htCIDtoDsstoxRecordId.get(dtxcid));
		pd.setDsstoxRecord(dr);
		pd.setCanonQsarSmiles("N/A");
		pd.setModel(model);
		pd.setPredictionValue(prediction);			
		return pd;
	}

	/**
	 * Runs from command line
	 * 
	 * @param smiles
	 * @param chemicalName
	 * @param modelName
	 * @return
	 */
	String runEPISUITE(String smiles,String chemicalName,String modelName) {
		String folder="C:\\Users\\TMARTI02\\Program Files\\EPISUITE\\";
		
		createInputFile(smiles, chemicalName,folder);

		String prediction=null;
		
		try {

			Process p=Runtime.getRuntime().exec("cmd /c start /wait epiwin1.exe",
			        null, new File(folder));
//			System.out.print("Waiting for prediction ...");
			p.waitFor();
//			System.out.println("done.");

			String filepathOut=folder+"summary";

			prediction=	parseSummaryByModelName(filepathOut, chemicalName, modelName);
			//			System.out.println("here\t"+smiles+"\t"+prediction);

		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			prediction="error: running executable";
		}
		return prediction;		
	}
	
	
	List<String> getLines(String filepath) {

		try {
			List<String>lines=new ArrayList<>();
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			while (true) {
				String Line=br.readLine();
				if(Line==null) break;
				lines.add(Line);
			}
			br.close();
			return lines;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	String parseSummaryByModelName(String filepathSummary, String chemicalName,String modelName) {

		List<String>lines=getLines(filepathSummary);
		String nameInFile=null;
		String prediction=null;

		for (String Line:lines) {
			if(Line.contains("CHEM   :")) {
				nameInFile=Line.substring(Line.indexOf(":")+1,Line.length()).trim();
			} 
			if (Line.contains(modelName)) {
				prediction=Line.substring(Line.indexOf(":")+1,Line.length()).trim();

				//TODO make code more intelligent to handle different models, for now just discard stuff in parentheses:
				if(prediction.contains("(")) {
					prediction=prediction.substring(0,prediction.indexOf("(")).trim();
				}
			}
		}
		//			System.out.println(smiles+"\t"+prediction);				

		if(chemicalName.contentEquals(nameInFile)) {
			return prediction;
		} else {
//			System.out.println("name mismatch:"+chemicalName+"\t"+nameInFile);
			return "error: name doesn't match:\t"+chemicalName+"\t"+nameInFile;
		}

	}
	void createInputFile(String smiles,String chemicalName,String folder) {
		
		String filepath=folder+"epi_inp.txt";
				
		try {
		
			FileWriter fw=new FileWriter(filepath);
			fw.write("CALCULATE\r\n");
			fw.write(smiles+"\r\n");
			fw.write(chemicalName+"\r\n");
			
//			System.out.println("In createInputFile name="+chemicalName+"\t"+filepath);

			fw.write("(null)\r\n"
					+ "(null)\r\n"
					+ "(null)\r\n"
					+ "(null)\r\n"
					+ "(null)\r\n"
					+ "0 \r\n"
					+ "1\r\n"
					+ "1\r\n"
					+ "5\r\n"
					+ "0.5\r\n"
					+ "1\r\n"
					+ "0.05\r\n"
					+ "(null)\r\n"
					+ "0\r\n");

			fw.flush();
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
					BiodegradationRateResults biodegradationRateResults=RunBiowinFromJava.runFromJava(smiles);
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

	
	public Hashtable<String,String> getHashtableSmilesFile(String filepath) {
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			Hashtable<String,String>ht=new Hashtable<>();
			
			while (true) {
				String Line=br.readLine();

				if(Line==null) {
					break;
				} 
				
				String [] values=Line.split("\t");

				String smiles=values[0];
				String dtxsid=values[1];
				ht.put(dtxsid,smiles);
				
			}
			
			return ht;
			
			
		} catch (Exception ex) {
//			ex.printStackTrace();
			return null;
		}
		
	}

	
	/**
	 * Gets results from a json file from the api
	 * Each line is a different set of api predictions
	 * 	 * 
	 * @param filepath
	 * @param models
	 * @return
	 */
	public static List<PredictionDashboard> getResultsFromAPIJsonFile(String filepath,List<Model> models,Hashtable<String,DsstoxRecord> htDTXSIDtoDsstoxRecord,Hashtable<String,Dataset>htModelNameToDataset) {

		String user="tmarti02";

		try {

			List<PredictionDashboard>allResults=new ArrayList<>();		

			BufferedReader br=new BufferedReader(new FileReader(filepath));

			String Line=null;

			int counter=0;

			long t1= System.currentTimeMillis();

			while (true) {
				counter++;
				Line=br.readLine();
				//			System.out.println(Line);
				if(Line==null) break;

				EpisuiteResults results=EpisuiteResults.getResults(Line);
				//			JsonObject results=Utilities.gson.fromJson(Line,JsonObject.class);

				if(!results.dtxsid.contains("DTXSID")) {
					continue;
					//				System.out.println(results.dtxsid+"\t"+results.smiles+"\t"+results.bioconcentration.bioaccumulationFactor);
				}

				if(counter%1000==0) {
					long t2= System.currentTimeMillis();
					System.out.println("\t"+counter+"\t"+(t2-t1)+" ms");
					t1=t2;
				}

				//			System.out.println(endpoint);
				convertEpisuiteResultsToPredictionDashboard(models, user, allResults, results, htDTXSIDtoDsstoxRecord,htModelNameToDataset);

			}

			//		System.out.println(lastLine);

			return allResults;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}
	

	public static List<PredictionDashboard> getResultsFromAPIJsonFile(String filepath,List<Model> models,String dtxsid,
			Hashtable<String,DsstoxRecord> htDTXSIDtoDsstoxRecord,Hashtable<String,Dataset>htModelNameToDataset) {

		String json=GetJson(dtxsid, filepath);
		if(json==null) return null;
		
		JsonObject jo=Utilities.gson.fromJson(json, JsonObject.class);		
		System.out.println(Utilities.gson.toJson(jo));
				
		EpisuiteResults results=EpisuiteResults.getResults(json);
		
		List<PredictionDashboard>allResults=new ArrayList<>();
		String user="tmarti02";
		convertEpisuiteResultsToPredictionDashboard(models, user, allResults, 
				results, htDTXSIDtoDsstoxRecord,htModelNameToDataset);
		
		return allResults;

	}
	
	public static List<PredictionDashboard> getResultsFromAPI(String dtxsid,List<Model> models,
			Hashtable<String,DsstoxRecord> htDTXSIDtoDsstoxRecord,Hashtable<String,Dataset>htModelNameToDataset) {

		String smiles=htDTXSIDtoDsstoxRecord.get(dtxsid).getSmiles();
		
		String json=EpisuiteWebserviceScript.runEpiwin(smiles);
		if(json==null) return null;
		
		JsonObject jo=Utilities.gson.fromJson(json, JsonObject.class);		
//		System.out.println(Utilities.gson.toJson(jo));
		
		jo.addProperty("dtxsid", dtxsid);
		
		
		json=gson.toJson(jo);
		
		Utilities.jsonToPrettyJson(json,"data\\episuite\\sample reports\\EPISUITE_RESULTS.json" );
		
		EpisuiteResults results=EpisuiteResults.getResults(json);
		
		List<PredictionDashboard>allResults=new ArrayList<>();
		String user="tmarti02";
		convertEpisuiteResultsToPredictionDashboard(models, user, allResults, results, 
				htDTXSIDtoDsstoxRecord,htModelNameToDataset);
		
		return allResults;

	}


	private static void convertEpisuiteResultsToPredictionDashboard(List<Model> models, String user,
			List<PredictionDashboard> allResults, EpisuiteResults results,
			Hashtable<String,DsstoxRecord> htDTXSIDtoDsstoxRecord,
			Hashtable<String,Dataset>htModelNameToDataset) {
		
		
		
		for(Model model:models) {

			PredictionDashboard pd=new PredictionDashboard();
			pd.setCanonQsarSmiles("N/A");
			pd.setCreatedBy(user);
			pd.setUpdatedBy(user);

//			DsstoxRecord dr=new DsstoxRecord();
//			dr.setDtxsid(results.dtxsid);
//			dr.setSmiles(results.smiles);
//			pd.setDsstoxRecord(dr);
			
			pd.setDsstoxRecord(htDTXSIDtoDsstoxRecord.get(results.dtxsid));			

			pd.setModel(model);
			results.setExpPred(pd,htModelNameToDataset);
			allResults.add(pd);
			
									
		}
	}
	
	
	public static List<String> getChemicalsFromAPIJsonFile(String filepath) {

		String user="tmarti02";

		try {
			
			File file=new File(filepath);

			List<String>allChemicals=new ArrayList<>();		

			BufferedReader br=new BufferedReader(new FileReader(filepath));

			String Line=null;

			int counter=0;

			long t1= System.currentTimeMillis();

			while (true) {
				counter++;
				Line=br.readLine();
				//			System.out.println(Line);
				if(Line==null) break;

				EpisuiteResults results=EpisuiteResults.getResults(Line);
				//			JsonObject results=Utilities.gson.fromJson(Line,JsonObject.class);

				if(!results.dtxsid.contains("DTXSID")) {
					continue;
					//				System.out.println(results.dtxsid+"\t"+results.smiles+"\t"+results.bioconcentration.bioaccumulationFactor);
				}

				if(counter%10000==0) {
					long t2= System.currentTimeMillis();
//					System.out.println(counter+"\t"+(t2-t1)+" ms");
					t1=t2;
				}


				String line=results.dtxsid+"\t"+results.smiles+"\t"+file.getName();
				
				allChemicals.add(line);
			}

			//		System.out.println(lastLine);

			return allChemicals;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}
	

	void compileApiJsonResultsFromFolder(String folderpath) {
		boolean writeToDB=false;
		
		LinkedHashMap<String,String>hmModelNameToPropertyName=getModelNameToPropertyNameMap();
		
		DsstoxSnapshotServiceImpl snapshotService=new  DsstoxSnapshotServiceImpl();
		DsstoxSnapshot snapshot=snapshotService.findByName("DSSTOX Snapshot 04/23");
//		Hashtable<String,Long> htDTXSIDtoDsstoxRecordId=dsstoxRecordService.getRecordIdHashtable(snapshot,"dtxsid");
//		Hashtable<String,Long> htDTXSIDtoDsstoxRecordId=dsstoxRecordService.getDsstoxRecordsHashtableFromJsonExport(OPERA_lookups.fileJsonDsstoxRecords,"dtxsid");
		Hashtable<String,DsstoxRecord> htDTXSIDtoDsstoxRecord=dsstoxRecordService.getDsstoxRecordsHashtableFromJsonExport(OPERA_lookups.fileJsonDsstoxRecords,"dtxsid");

		
		HashMap<String, Model>hmMap=createModels();		

		List<Model> models=new ArrayList<>();
		
		for(String modelName:hmModelNameToPropertyName.keySet()) {
//			System.out.println(modelName);
			models.add(hmMap.get(modelName));
		}

		Hashtable<String,Dataset>htModelNameToDataset=createDatasets();


//		if(true)return;

		File folder=new File(folderpath);
//		Hashtable<String,PredictionDashboard>allPreds=new Hashtable<>();
		HashSet<String>dtxsids=new HashSet<>();
		
		List<PredictionDashboard>resultsAll=new ArrayList<>();

					
		for (File file:folder.listFiles()) {
			if(!file.getName().contains(".json")) continue;
			if(file.getName().contains("sample")) continue;

			if(!file.getName().equals("episuite results 9013.json")) continue;
//			if(!file.getName().equals("snapshot_chemicals_to_run_9000.json")) continue;
			
			
			System.out.println(file.getName());

			List<PredictionDashboard>results=getResultsFromAPIJsonFile(file.getAbsolutePath(),models,htDTXSIDtoDsstoxRecord,htModelNameToDataset);

			
			for(PredictionDashboard pd:results) {
				String dtxsid=pd.getDsstoxRecord().getDtxsid();
				
				
				if(!dtxsids.contains(dtxsid)) dtxsids.add(dtxsid);
				
				if(pd.getExperimentalValue()!=null) {
					System.out.println(pd.toTsv());	
				}
				
//				addMetadataToReport(htModelNameToDataset, pd);
				
//				System.out.println(dtxsid+"\t"+key+"\t"+pd.getModel().getName()+"\t"+pd.getExperimentalValue()+"\t"+pd.getPredictionValue()+"\t"+pd.getPredictionError());
//				System.out.println(dtxsid+"\t"+pd.getModel().getName()+"\t"+pd.getExperimentalValue()+"\t"+pd.getPredictionValue()+"\t"+pd.getPredictionString()+"\t"+pd.getPredictionError());
				resultsAll.add(pd);
			}
			
			System.out.println(file.getName()+"\t"+results.size()+"\t"+resultsAll.size()+"\t"+dtxsids.size());
			
//			System.out.println(file.getName()+"\t"+results.size());
		}
		
//		System.out.println(Utilities.gson.toJson(resultsAll));
		
		if(writeToDB) predictionDashboardService.createSQL(resultsAll);//write to DB

//		writeRemainingSnapshotChemicalsToRunFile(folderpath, dtxsids);
		
		
	}
	
	void convertRecordToPredictionsDashboard(String filepath,String dtxsid) {
		
		
		boolean writeToDB=false;
		
		LinkedHashMap<String,String>hmModelNameToPropertyName=getModelNameToPropertyNameMap();
		
		DsstoxSnapshotServiceImpl snapshotService=new  DsstoxSnapshotServiceImpl();
		DsstoxSnapshot snapshot=snapshotService.findByName("DSSTOX Snapshot 04/23");

//		Hashtable<String,Long> htDTXSIDtoDsstoxRecordId=dsstoxRecordService.getRecordIdHashtable(snapshot,"dtxsid");
		Hashtable<String,DsstoxRecord> htDTXSIDtoDsstoxRecord=dsstoxRecordService.getDsstoxRecordsHashtableFromJsonExport(OPERA_lookups.fileJsonDsstoxRecords,"dtxsid");

		HashMap<String, Model>hmMap=createModels();		

		Hashtable<String,Dataset>htModelNameToDataset=createDatasets();
		
		List<Model> models=new ArrayList<>();
		
		for(String modelName:hmModelNameToPropertyName.keySet()) {
			models.add(hmMap.get(modelName));
		}

		List<PredictionDashboard>results=getResultsFromAPIJsonFile(filepath,models,dtxsid, htDTXSIDtoDsstoxRecord,htModelNameToDataset);
				
		System.out.println(PredictionDashboard.getHeader());
		
		for(PredictionDashboard pd:results) {
//			pd.getDsstoxRecord().setId(htDTXSIDtoDsstoxRecord.get(dtxsid).getId());
//			if(pd.getExperimentalValue()!=null) {
//				System.out.println(pd.toTsv());	
//			}
			
			System.out.println(pd.toTsv());

//			addMetadataToReport(htModelNameToDataset, pd);
			
			String json=new String(pd.getPredictionReport().getFile());
			String filepathJsonReport="data\\episuite\\sample reports\\"+pd.getModel().getName()+".json";
			String json2=Utilities.jsonToPrettyJson(json,filepathJsonReport);
			
			if(json.contains("output")) {
				System.out.println(pd.getModel().getName()+"\tHas output");
			}
			
			
//			if(pd.getPredictionReport()!=null) {
//				String report=new String(pd.getPredictionReport().getFile());
//				JsonObject jo=Utilities.gson.fromJson(report, JsonObject.class);
//				String prettyReport=Utilities.gson.toJson(jo);
//				System.out.println(prettyReport);
//			}
			
//			System.out.println(dtxsid+"\t"+key+"\t"+pd.getModel().getName()+"\t"+pd.getExperimentalValue()+"\t"+pd.getPredictionValue()+"\t"+pd.getPredictionError());
//			System.out.println(dtxsid+"\t"+pd.getModel().getName()+"\t"+pd.getExperimentalValue()+"\t"+pd.getPredictionValue()+"\t"+pd.getPredictionString()+"\t"+pd.getPredictionError());
		}
		
		if(writeToDB) predictionDashboardService.createSQL(results);//write to DB
		
	}
	

	void convertSmilesToPredictionsDashboard(String dtxsid) {
		
		
		boolean writeToDB=false;
		
		LinkedHashMap<String,String>hmModelNameToPropertyName=getModelNameToPropertyNameMap();
		
		DsstoxSnapshotServiceImpl snapshotService=new  DsstoxSnapshotServiceImpl();
		DsstoxSnapshot snapshot=snapshotService.findByName("DSSTOX Snapshot 04/23");

//		Hashtable<String,Long> htDTXSIDtoDsstoxRecordId=dsstoxRecordService.getRecordIdHashtable(snapshot,"dtxsid");
		Hashtable<String,DsstoxRecord> htDTXSIDtoDsstoxRecord=dsstoxRecordService.getDsstoxRecordsHashtableFromJsonExport(OPERA_lookups.fileJsonDsstoxRecords,"dtxsid");

		HashMap<String, Model>hmMap=createModels();		

		Hashtable<String,Dataset>htModelNameToDataset=createDatasets();
		
		List<Model> models=new ArrayList<>();
		
		for(String modelName:hmModelNameToPropertyName.keySet()) {
			models.add(hmMap.get(modelName));
		}
		
		List<PredictionDashboard>results=getResultsFromAPI(dtxsid,models, htDTXSIDtoDsstoxRecord,htModelNameToDataset);
				
		System.out.println(PredictionDashboard.getHeader());
		
		for(PredictionDashboard pd:results) {
			pd.getDsstoxRecord().setId(htDTXSIDtoDsstoxRecord.get(dtxsid).getId());
//			if(pd.getExperimentalValue()!=null) {
//				System.out.println(pd.toTsv());	
//			}
			
			System.out.println(pd.toTsv());

			long t1=System.currentTimeMillis();
//			addMetadataToReport(htModelNameToDataset, pd);
			long t2=System.currentTimeMillis();
			
//			System.out.println((t2-t1)+" milliseconds");
			
			String json=new String(pd.getPredictionReport().getFile());
			String filepathJsonReport="data\\episuite\\sample reports\\"+pd.getModel().getName()+".json";
			String json2=Utilities.jsonToPrettyJson(json,filepathJsonReport);
			
			PropertyResult2 pr2=gson.fromJson(json, PropertyResult2.class);
			
			EpisuiteReport er=new EpisuiteReport(pr2, pd, htModelNameToDataset.get(pd.getModel().getName()));			
			String json3=Utilities.saveJson(er,filepathJsonReport.replace(".json", "_2.json"));
			
			
			if(json.contains("output")) {
				System.out.println(pd.getModel().getName()+"\tHas output");
			}
			
			
//			System.out.println(dtxsid+"\t"+key+"\t"+pd.getModel().getName()+"\t"+pd.getExperimentalValue()+"\t"+pd.getPredictionValue()+"\t"+pd.getPredictionError());
//			System.out.println(dtxsid+"\t"+pd.getModel().getName()+"\t"+pd.getExperimentalValue()+"\t"+pd.getPredictionValue()+"\t"+pd.getPredictionString()+"\t"+pd.getPredictionError());
		}
		
		if(writeToDB) predictionDashboardService.createSQL(results);//write to DB
		
	}

//	private void addMetadataToReport(Hashtable<String, Dataset> htModelToDataset, PredictionDashboard pd) {
//
//		PredictionReport pr=pd.getPredictionReport();
//		
//		String json=new String(pr.getFile());
//		JsonObject jo=gson.fromJson(json, JsonObject.class);
//		
//		Dataset dataset=htModelToDataset.get(pd.getModel().getName());
//		String propertyName=dataset.getProperty().getName();
//		
////		System.out.println(propertyName);
//		
//		//Convert model to array to make it consistent:
//		jo.addProperty("propertyName",propertyName);
//		jo.addProperty("propertyDescription",dataset.getProperty().getDescription());
//
//		json=gson.toJson(jo);
////		System.out.println(json);
//		
//		pr.setFile(json.getBytes());
//	}
	
	
	void compileApiJsonResultsFromFolderToChemicalCSV(String folderpath) {
		
		File folder=new File(folderpath);
//		Hashtable<String,PredictionDashboard>allPreds=new Hashtable<>();
		HashSet<String>dtxsids=new HashSet<>();
		
		List<PredictionDashboard>resultsAll=new ArrayList<>();
		
		
		try {

			FileWriter fw=new FileWriter(folderpath+"chemicals ran by api.txt");

			fw.write("DTXSID\tsmiles\tfilename\r\n");
			
			for (File file:folder.listFiles()) {
				if(!file.getName().contains(".json")) continue;
				if(file.getName().contains("sample")) continue;

				//			if(!file.getName().equals("episuite results 9012.json")) continue;
				//			if(!file.getName().equals("snapshot_chemicals_to_run_9000.json")) continue;
				//			System.out.println(file.getName());

				List<String>lines=getChemicalsFromAPIJsonFile(file.getAbsolutePath());
				System.out.println(file.getName()+"\t"+lines.size());


				for (String line:lines) {
					fw.write(line+"\r\n");
				}

				fw.flush();

				//			System.out.println(file.getName()+"\t"+results.size()+"\t"+resultsAll.size()+"\t"+dtxsids.size());


				//			System.out.println(file.getName()+"\t"+results.size());
			}
			
			fw.close();

			//		System.out.println(Utilities.gson.toJson(resultsAll));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	
	
	
	void writeToxvalModelRecordsFromJsonResultsFromFolder(String folderpath) {
		boolean writeToDB=false;

//		DsstoxSnapshotServiceImpl snapshotService=new  DsstoxSnapshotServiceImpl();
//		DsstoxSnapshot snapshot=snapshotService.findByName("DSSTOX Snapshot 04/23");
//		Hashtable<String,Long> htDTXSIDtoDsstoxRecordId=dsstoxRecordService.getRecordIdHashtable(snapshot,"dtxsid");

		HashMap<String, Model>hmMap=createModels();		

		List<String>desiredModelNames=Arrays.asList("EPISUITE_BIOWIN3","EPISUITE_BCF");
		String outputFileName="episuite BCF BIOWIN3 for toxval models.tsv";
		
		
		List<Model> models=new ArrayList<>();
		for(String modelName:desiredModelNames) models.add(hmMap.get(modelName));
	
		Hashtable<String,DsstoxRecord> htDTXSIDtoDsstoxRecord=dsstoxRecordService.getDsstoxRecordsHashtableFromJsonExport(OPERA_lookups.fileJsonDsstoxRecords,"dtxsid");

		Hashtable<String,Dataset>htModelNameToDataset=createDatasets();
		
		//		if(true)return;

		File folder=new File(folderpath);
		HashSet<String>dtxsids=new HashSet<>();

		List<PredictionDashboard>resultsAll=new ArrayList<>();

		HashSet <String>keys=new HashSet<>();
		
		try {

			
			File f=new File(folderpath+"toxval");
			f.mkdirs();
			
			FileWriter fw=new FileWriter(folderpath+"toxval\\"+outputFileName);
			fw.write(new RecordToxValModel().toHeaderTSV()+"\r\n");

			for (File file:folder.listFiles()) {
				if(!file.getName().contains(".json")) continue;
				if(file.getName().contains("sample")) continue;

//				if(!file.getName().equals("episuite results 9012.json")) continue;
				//			if(!file.getName().equals("snapshot_chemicals_to_run_9000.json")) continue;


				//			System.out.println(file.getName());

				List<PredictionDashboard>results=getResultsFromAPIJsonFile(file.getAbsolutePath(),models,htDTXSIDtoDsstoxRecord,htModelNameToDataset);


				for(PredictionDashboard pd:results) {
					
					String modelName=pd.getModel().getName();
					String dtxsid=pd.getDsstoxRecord().getDtxsid();
//					pd.getDsstoxRecord().setId(htDTXSIDtoDsstoxRecordId.get(dtxsid));//dont need id since not writing to db
					
					String key=dtxsid+"\t"+modelName;
					
					if(!keys.contains(key)) {
						keys.add(key);
					} else {
//						System.out.println("Already have "+key);
						continue;
					}
					

					if(!dtxsids.contains(dtxsid)) dtxsids.add(dtxsid);

					//				if(pd.getExperimentalValue()!=null) {
					//					System.out.println(pd.toTsv());	
					//				}

					//				System.out.println(dtxsid+"\t"+key+"\t"+pd.getModel().getName()+"\t"+pd.getExperimentalValue()+"\t"+pd.getPredictionValue()+"\t"+pd.getPredictionError());
					//				System.out.println(dtxsid+"\t"+pd.getModel().getName()+"\t"+pd.getExperimentalValue()+"\t"+pd.getPredictionValue()+"\t"+pd.getPredictionString()+"\t"+pd.getPredictionError());
					resultsAll.add(pd);

//					System.out.println(pd.toTsv());


					RecordToxValModel r=new RecordToxValModel();
					r.model_id=-1;
					r.chemical_id=-1;
					r.dtxsid=pd.getDsstoxRecord().getDtxsid();
					r.qualifier="=";
					r.model="EpiSuite";
					r.value=pd.getPredictionValue();


					if(modelName.equals("EPISUITE_BCF")) {
						r.metric="BCF";
						r.units="L/kg wet-wt";
					} else if(modelName.equals("EPISUITE_BIOWIN3")) {
						r.metric="Biodegradation Score";
						r.units="unitless";
					}

					fw.write(r.toTSV()+"\r\n");

				}

				System.out.println(file.getName()+"\t"+results.size()+"\t"+resultsAll.size()+"\t"+dtxsids.size());
				//			System.out.println(file.getName()+"\t"+results.size());
			}

			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void writeRemainingSnapshotChemicalsToRunFile(String folderpath, HashSet<String>dtxsidsRan) {
		try {
			
			Hashtable<String,String>htAll=getHashtableSmilesFile(folderpath+"snapshot_compounds_fixed.txt");
			FileWriter fw=new FileWriter(folderpath+"snapshot_chemicals_to_run.txt");
			
			for(String dtxsid:htAll.keySet()) {
				if (dtxsidsRan.contains(dtxsid)) continue;
				String smiles=htAll.get(dtxsid);
				fw.write(smiles+"\t"+dtxsid+"\r\n");
			}
			
			fw.flush();
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	void getPredictionCountChemprop() {
		
//		select  count(distinct(efk_dsstox_compound_id)),e.name   from prod_chemprop.qsar_predicted_properties qpp
//		join prod_qsar.models m on qpp.efk_qsar_model_id=m.id
//		join prod_dsstox.compounds c on c.id=qpp.efk_dsstox_compound_id
//		join prod_chemprop.endpoints e on e.id=m.efk_chemprop_endpoint_id
//		where m.name like '%EPISUITE%'
//		group by e.name;
		
//		predcount	name
//		109233	Water Solubility
//		113530	Boiling Point
//		113530	LogKow: Octanol-Water
//		113530	Melting Point
//		1219	Bioaccumulation Factor
//		1219	Bioconcentration Factor
//		1219	EC50 for Algae
//		1219	Half Life in Air
//		1219	Half Life in Soil
//		1219	Half Life in Water
//		1219	HalfLife in Sediment
//		1219	Soil Adsorp. Coeff. (Koc)
//		1219	The LC50 for Daphnia Magna
//		1219	The LC50 for Fish

	}
	

	LinkedHashMap<String,String>getModelNameToPropertyNameMap() {
		
		LinkedHashMap<String,String>hm=new LinkedHashMap<>();
		
		hm.put("EPISUITE_LOGP",DevQsarConstants.LOG_KOW);
		hm.put("EPISUITE_LOGKOA",DevQsarConstants.LOG_KOA);
		
		hm.put("EPISUITE_MP",DevQsarConstants.MELTING_POINT);
		hm.put("EPISUITE_BP",DevQsarConstants.BOILING_POINT);

		hm.put("EPISUITE_WS_KOW",DevQsarConstants.WATER_SOLUBILITY);
		hm.put("EPISUITE_WATERNT",DevQsarConstants.WATER_SOLUBILITY);

		hm.put("EPISUITE_VP",DevQsarConstants.VAPOR_PRESSURE);
		hm.put("EPISUITE_HLC",DevQsarConstants.HENRYS_LAW_CONSTANT);
		
		hm.put("EPISUITE_AOH",DevQsarConstants.OH);
		
		hm.put("EPISUITE_BIOWIN1",DevQsarConstants.BIODEG);
		hm.put("EPISUITE_BIOWIN2",DevQsarConstants.BIODEG);
		hm.put("EPISUITE_BIOWIN3",DevQsarConstants.ULTIMATE_BIODEG);
		hm.put("EPISUITE_BIOWIN4",DevQsarConstants.PRIMARY_BIODEG);
		hm.put("EPISUITE_BIOWIN5",DevQsarConstants.RBIODEG);
		hm.put("EPISUITE_BIOWIN6",DevQsarConstants.RBIODEG);
		hm.put("EPISUITE_BIOWIN7",DevQsarConstants.BIODEG_ANAEROBIC);
		hm.put("EPISUITE_RBIODEG",DevQsarConstants.RBIODEG);

		hm.put("EPISUITE_BioHCWIN",DevQsarConstants.BIODEG_HL_HC);
		hm.put("EPISUITE_KOC",DevQsarConstants.KOC);
		
		hm.put("EPISUITE_BCF_UPPER_TROPHIC",DevQsarConstants.BCF);
		hm.put("EPISUITE_BAF_UPPER_TROPHIC",DevQsarConstants.BAF);
		hm.put("EPISUITE_BIOTRANS_HL",DevQsarConstants.KmHL);

		hm.put("EPISUITE_BCF",DevQsarConstants.BCF);
		
		

		return hm;
	}
	
	
	private String getDatasetName(String modelName) {
		String datasetName=modelName+" EPI Suite "+version;
		return datasetName;
	}
	
	
	Hashtable<String,Dataset> createDatasets() {


		Hashtable<String,Dataset>htModelNameToDataset=new Hashtable<>();
		
		
		HashMap<String, String>hmUnitsDatasetContributor=DevQsarConstants.getContributorUnitsNameMap();
		HashMap<String,String>hmModelNameToPropertyName=getModelNameToPropertyNameMap();
		
		
		for (String modelName:hmModelNameToPropertyName.keySet()) {
			String propertyNameDB=hmModelNameToPropertyName.get(modelName);
			
			String datasetName = getDatasetName(modelName);

			String unitAbbrev=hmUnitsDatasetContributor.get(propertyNameDB);

			if(unitAbbrev==null) {
				System.out.println(modelName+"\t"+propertyNameDB+"\t"+datasetName+"\tMissing units skipping");
				continue;
			} 

			Unit unit=CreatorScript.createUnit(unitAbbrev,lanId);
			Unit unitContributor=CreatorScript.createUnit(unitAbbrev,lanId);

			String propertyDescriptionDB=DevQsarConstants.getPropertyDescription(propertyNameDB);
			
			if(propertyDescriptionDB.contains("*")) {
				System.out.println(propertyNameDB+"\t"+propertyDescriptionDB);
			}
			
			Property property=CreatorScript.createProperty(propertyNameDB, propertyDescriptionDB,lanId);

			String datasetDescription=propertyNameDB+" dataset from EPI Suite "+version;
//			//		System.out.println(datasetName+"\t"+datasetDescription);
//
			String dsstoxMappingStrategy=null;

			Dataset dataset=new Dataset(datasetName, datasetDescription, property, unit, unitContributor,
					dsstoxMappingStrategy, lanId);

			dataset=CreatorScript.createDataset(dataset);
			
			htModelNameToDataset.put(modelName,dataset);

//			System.out.println(dataset.getName()+"\t"+dataset.getDescription()+"\t"+dataset.getUnit().getName()+"\t"+dataset.getUnitContributor().getName());
		}

		System.out.println("Got datasets");
		
		return htModelNameToDataset;


	}
	

	private HashMap<String, Model> createModels() {

		HashMap<String,String>hmModelNameToPropertyName=getModelNameToPropertyNameMap();

		String sourceName="EPI Suite "+version;
		String descriptorSetName="EPI Suite fragment counts";
		String splittingName="EPI Suite";
		String methodName="reg";

		SourceService ss=new SourceServiceImpl();
		Source source=ss.findByName(sourceName);
//		System.out.println(sourceName);
//		System.out.println("sourceId="+ source.getId());
		
		HashMap<String,Method> hmMethods=new HashMap<>();
		hmMethods.put(methodName,methodService.findByName(methodName));
		
		HashMap<String,Model> hmModels=new HashMap<>();
		
		for (String modelName:hmModelNameToPropertyName.keySet()) {
			String propertyNameDB=hmModelNameToPropertyName.get(modelName);
			String datasetName=getDatasetName(propertyNameDB);
//			System.out.println(modelName+"\t"+propertyNameDB);
			Model model=new Model(modelName,hmMethods.get(methodName), null,descriptorSetName, datasetName, splittingName, source,lanId);
			model=CreatorScript.createModel(model);
			hmModels.put(modelName, model);
		}
		
		System.out.println("Got models");
		
		return hmModels;
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
	
	class ResultChemical{
		String DTXSID;
		String smiles;
		String filename;
	}
	
	
	/*
	 * Find duplicates from the text file list of chemicals generated by compileApiJsonResultsFromFolderToChemicalCSV()
	 */
	void lookAtRanChemicals() {
		String folder="data\\episuite\\";
		
		
		List<String>dupFiles=new ArrayList<>();
		
		Hashtable<String,List<ResultChemical>>htFixed=new Hashtable<>();
		
//		
		
		try {
			
			BufferedReader brFixed=new BufferedReader(new FileReader(folder+"snapshot_compounds_fixed.txt"));
			while (true) {
				String Line=brFixed.readLine();
				if(Line==null) { 
					break;
				}
				String [] vals=Line.split("\t");
				ResultChemical rc=new ResultChemical();
				rc.smiles=vals[0];
				rc.DTXSID=vals[1];
				
				if(htFixed.containsKey(rc.DTXSID)) {
					List<ResultChemical>resultChemicals=htFixed.get(rc.DTXSID);
					resultChemicals.add(rc);
					System.out.println("Duplicated in fixed file for "+rc.DTXSID);
				} else {
					List<ResultChemical>resultChemicals=new ArrayList<>();
					resultChemicals.add(rc);
					htFixed.put(rc.DTXSID, resultChemicals);
				}
			}			
			brFixed.close();

			System.out.println(htFixed.size()+"\tnumber of unique dtxsids in \"snapshot_compounds_fixed.txt\" smiles file");
			
			
			Hashtable<String,List<ResultChemical>>ht=new Hashtable<>();
			BufferedReader br=new BufferedReader(new FileReader(folder+"chemicals ran by api.txt"));
			br.readLine();//header
			
			while (true) {
				String Line=br.readLine();
				
				if(Line==null) { 
					break;
				}
				
				String [] vals=Line.split("\t");
				
				ResultChemical rc=new ResultChemical();
				rc.DTXSID=vals[0];
				rc.smiles=vals[1];
				rc.filename=vals[2];
				
				if(ht.containsKey(rc.DTXSID)) {
					List<ResultChemical>resultChemicals=ht.get(rc.DTXSID);
					resultChemicals.add(rc);
				} else {
					List<ResultChemical>resultChemicals=new ArrayList<>();
					resultChemicals.add(rc);
					ht.put(rc.DTXSID, resultChemicals);
				}
				
			}
			
			System.out.println(ht.size()+"\tnumber of unique dtxsids in json files");
			
			for (String DTXSID:ht.keySet()) {
				List<ResultChemical>resultChemicals=ht.get(DTXSID);
				
				if(resultChemicals.size()==2) {
					
					ResultChemical rc1=resultChemicals.get(0);
					ResultChemical rc2=resultChemicals.get(1);

					String dupLine=rc1.filename+"\t"+rc2.filename;
//					String dupLine=DTXSID+"\t"+rc1.filename+"\t"+rc2.filename;
					
					if(!dupFiles.contains(dupLine)) {
						dupFiles.add(dupLine);
					}
					
//					for(ResultChemical rc:resultChemicals) {
//						System.out.println(rc.DTXSID+"\t"+rc.smiles+"\t"+rc.filename);
//					}
				}
			}

			//Print out which files have duplicates with each other
			Collections.sort(dupFiles);
			for(String line:dupFiles) {
				System.out.println("Duplicate:\t"+line);
			}

			//Print out DTXSIDs are in the fixed file but missing in the json file
			for(String dtxsid:htFixed.keySet()) {
				if(!ht.containsKey(dtxsid)) {
					String smiles=htFixed.get(dtxsid).get(0).smiles;
					System.out.println(dtxsid+"\t"+smiles+"\tmissing in json files");
				}
			}
			
			
			
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	void loadData() {
		String folder="data\\episuite\\";
		
		
//		createModels();
//		compileApiJsonResultsFromFolder(folder);
		
//		compileApiJsonResultsFromFolderToChemicalCSV("data\\episuite\\");
//		lookAtRanChemicals();
		
//		convertRecordToPredictionsDashboard(folder+"episuite results 9000.json","DTXSID2021739");

//		convertRecordToPredictionsDashboard(folder+"snapshot_chemicals_to_run_9011.json","DTXSID10338866");

		//Has error:
//		convertRecordToPredictionsDashboard(folder+"snapshot_chemicals_to_run_9011.json","DTXSID00983369");
		
//		convertSmilesToPredictionsDashboard("DTXSID10338866");
//		convertSmilesToPredictionsDashboard("DTXSID3039242");
	
//		convertSmilesToPredictionsDashboard("DTXSID0020523");//has exp BCF
		
//		convertSmilesToPredictionsDashboard("DTXSID8025545");//methane has exp AOH
		convertSmilesToPredictionsDashboard("DTXSID3039242");//benzene
//		convertSmilesToPredictionsDashboard("DTXSID6026296");//water inorganic
		
		
		
	}
	
	
	
	 static String GetJson(String dtxsid,String filepath) {

		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));

			int counter=0;
			
			while (true) {
				String Line=br.readLine();
				if(Line==null) {
					break;
				} 
				counter++;
		
				if(Line.contains("\"dtxsid\":\""+dtxsid+"\"")) {
					return Line;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;

	 }

	
	
	public static void main(String[] args) {
		PredictionResultsEPISUITEScript script = new PredictionResultsEPISUITEScript();
		
		script.loadData();
		
//		script.writeToxvalModelRecordsFromJsonResultsFromFolder("data\\episuite\\");
//		script.compareResults();
		
//		 script.runPredictionSnapshot();
//		script.runEPISUITE("CCCCO", "butanol","Biowin3 (Ultimate Survey Model)");
//		script.createBatchSmilesFiles();
//		script.createBatchSmilesFile();
//		script.runUsingWebService("local");
		
//		script.runBiowin3ForSmilesList("java","biowin3 output from Java.txt");
//		script.runBiowin3ForSmilesList("local","biowin3 output from local api.txt");
//		script.runBiowin3ForSmilesListMultithreaded("local","biowin3 output from local api-multithreaded10.txt");
//		script.runBiowin3ForSmilesList("soap","biowin3 output soap.txt");
		
		
//		script.compareBiowinFiles();
	}

}
