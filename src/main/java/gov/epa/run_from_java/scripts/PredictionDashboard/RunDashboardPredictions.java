package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.smiles.SmilesParser;

import com.google.gson.reflect.TypeToken;
import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.service.GenericSubstanceServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
* @author TMARTI02
*/
public class RunDashboardPredictions {

	GenericSubstanceServiceImpl gss=new GenericSubstanceServiceImpl();
	
	final String urlWebTEST="https://hcd.rtpnc.epa.gov";
	
	
	public RunDashboardPredictions() {
		try {
			Unirest.config().followRedirects(true).socketTimeout(000).connectTimeout(000);
		} catch (Exception e) {
			// Ignore
		}
	}
	
	void runChemical(String DTXSID) {
		//2 different ways to run it- using our python webservice and using valery webservice
		List<DsstoxRecord>records=gss.findAsDsstoxRecordsByDtxsid(DTXSID);
		
		DsstoxRecord dr=records.get(0);
		System.out.println(Utilities.gson.toJson(dr));
		
		String json=doAPI_Prediction_WebTEST2(dr.smiles,1,21);//Note these ids dont correspond to models in postgres_testing (but can still store results using model_id
		
		System.out.println(json);
		
		//convert json to java object=>dashboard predictipn object
		
		
	}
	
	
	
	void runSDF(String SDFFilePath, Long modelSetID,Long datasetId, String destJsonPath,boolean skipMissingSID,int maxCount) {

		List<Object>allResults=new ArrayList<>();//need to create a PredictionResults class based on WebTEST2.0 prediction json output
		
		AtomContainerSet acs=readSDFV3000(SDFFilePath);
		
		AtomContainerSet acs2 = filterAtomContainerSet(acs, skipMissingSID, maxCount);

		Iterator<IAtomContainer> iterator= acs2.atomContainers().iterator();

		
		System.out.println(acs2.getAtomContainerCount());

		int count=0;
		
		while (iterator.hasNext()) {
			count++;
			
			AtomContainer ac=(AtomContainer) iterator.next();
			String smiles=ac.getProperty("smiles");//TODO should we convert ac to smiles or just use smiles in DSSTOX?
			
			System.out.println("***"+count+"\t"+smiles);
			
			String json=doAPI_Prediction_WebTEST2(smiles,modelSetID,datasetId);//Note these ids dont correspond to models in postgres_testing (but can still store results using model_id
		
			System.out.println(json);
			
			Object obj=Utilities.gson.fromJson(json, Object.class);
			allResults.add(obj);
			
		}

//		System.out.println(Utilities.gson.toJson(allResults));
		//TODO do API call for json Report, store that too
		
		Utilities.saveJson(allResults, destJsonPath);
	}
	
	
	/**
	 * Writing my own V3000 reader because CDK sucks and cant read SDFs for all the dashboard chemicals and get the properties too
	 * 
	 * @param sdfFilePath
	 * @return
	 */
	public static AtomContainerSet readSDFV3000(String sdfFilePath) {

		AtomContainerSet acs = new AtomContainerSet();

		MDLV3000Reader mr=new MDLV3000Reader();

		SmilesParser sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());

		
		try {

			FileInputStream fis=new FileInputStream(sdfFilePath);
			BufferedReader br=new BufferedReader (new InputStreamReader(fis, "UTF-8"));

			boolean stop=false;

			while (true) {

				String strStructure="";

				while (true) {
					String Line=br.readLine();

					if(Line==null) {
						stop=true;
						break;
					}

					//				System.out.println(Line);
					strStructure+=Line+"\r\n";
					if(Line.contains("M  END"))break;
				}

				if(stop)break;


				InputStream stream = new ByteArrayInputStream(strStructure.getBytes());
				mr.setReader(stream);

				IAtomContainer molecule=null;
				
				try {
					molecule = (IAtomContainer) mr.readMolecule(DefaultChemObjectBuilder.getInstance());
				} catch (Exception ex) {
					molecule=new AtomContainer();
				}

				while (true) {
					String Line=br.readLine();
					//				System.out.println(Line);

					if(Line.contains(">  <")) {
						String field=Line.substring(Line.indexOf("<")+1,Line.length()-1);
						String value=br.readLine();
						molecule.setProperty(field, value);
						//					System.out.println(field);
					}

					if(Line.contains("$$$"))break;
				}

				if(molecule.getAtomCount()==0) {
				
					AtomContainer molecule2=null;
					
					String smiles=molecule.getProperty("smiles");
					
					if (smiles!=null) {
						try {
							molecule2 = (AtomContainer)sp.parseSmiles(smiles);
//						System.out.println(DTXCID+"\t"+smiles+"\t"+molecule2.getAtomCount());
						} catch (Exception ex) {
							molecule2=new AtomContainer();
						}
						
					}else {
						molecule2 = new AtomContainer();
					}							
						
					molecule2.setProperties(molecule.getProperties());
					acs.addAtomContainer(molecule2);
					
				} else {
					acs.addAtomContainer(molecule);
				}
						
			}
			
			br.close();
			mr.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return acs;
	}
	
	public static AtomContainerSet filterAtomContainerSet(AtomContainerSet acs, boolean skipMissingSID, int maxCount) {

		
		int count=0;
		
		Iterator<IAtomContainer> iterator= acs.atomContainers().iterator();

		AtomContainerSet acs2=new AtomContainerSet();

		
		while (iterator.hasNext()) {
			AtomContainer ac=(AtomContainer) iterator.next();
			String SID=ac.getProperty("DTXSID");
			if(skipMissingSID && SID==null) continue;
			acs2.addAtomContainer(ac);
			count++;
//			System.out.println(ac.getProperty("DTXSID")+"\t"+ac.getProperty("smiles"));
//			WebTEST4.checkAtomContainer(ac);//theoretically the webservice has its own checking
			
			if(count==maxCount)break;
		}
		return acs2;
	}

	
	//TODO make batch mode for this
	String doAPI_Prediction_WebTEST2(String smiles,long modelset_id,long dataset_id) {
		
		//TODO create the json string using objects and gson
		
//		String body="{\"chemicals\":[{\"smiles\":\""+smiles+"\"}],\"modelset_id\":"+modelset_id +",\"datasets\":[{\"dataset_id\":"+dataset_id+"}],\"workflow\":\"qsar-ready\"}\r\n\r\n";

		String body="{\"chemicals\":[{\"smiles\":\""+smiles+"\"}],\"modelset_id\":"+modelset_id +",\"datasets\":[{\"dataset_id\":"+dataset_id+"}]}\r\n\r\n";

		
//		System.out.println(body);
		
		HttpResponse<String> response = Unirest.post(urlWebTEST+"/api/predictor/predict")
		  .header("Content-Type", "application/json")
		  .body(body)
		  .asString();
		return response.getBody();
	}
	
	public static void main(String[] args) {
		RunDashboardPredictions r=new RunDashboardPredictions();
//		r.runChemical("DTXSID3039242");
		
		int maxCount=10;//number of chemicals to run
		boolean skipMissingSID=true;//skip entries without an SDF
		
		String folderSrc="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\hibernate_qsar_model_building\\data\\dsstox\\sdf\\";
		String fileNameSDF="snapshot_compounds1.sdf";
		String filepathSDF=folderSrc+fileNameSDF;
		
		String strOutputFolder="reports/prediction_json";
		new File(strOutputFolder).mkdirs();
		
		String outputFileName="snapshot_compounds1-WS.json";
		String destJsonPath=strOutputFolder+File.separator+outputFileName;

		//31 WS, 44 LLNA
		r.runSDF(filepathSDF, 2L, 31L, destJsonPath, skipMissingSID, maxCount);
		
	}

}
