package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;

import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;



/**
* @author TMARTI02
*/
public class CompareToChemprop {

	void getSampleMapFromChempropResults(String folder,String filename) {
		
		boolean skipSalts=true;

		HashSet<String>dtxsids=	getDtxsids(folder, filename, skipSalts);
		
//		System.out.println(dtxsids.size());
		
		ArrayList<String> listDtxsids=new ArrayList<>(dtxsids);
		
		Collections.shuffle(listDtxsids);
		
		int n=1000;//number of chemicals
		
		dtxsids=new HashSet<String>();
		
		for (int i=0;i<n;i++) {
			String dtxsid=listDtxsids.get(i);
			dtxsids.add(dtxsid);
//			System.out.println(dtxsid);
		}
		
//		SortedMap<String, SortedMap<String,Prediction>>map=getResults(dtxsids, folder, filename);
//		Utilities.saveJson(map, folder+"percepta_chemprop_sample.json");

		SortedMap<String, List<Prediction>>map=getResultsMapList(dtxsids, folder, filename);
		Utilities.saveJson(map, folder+"percepta_chemprop_sample_simple.json");
		
	}


	public class Prediction {
		String dtxcid;
		String modelName;
		String smiles;
		Double predictionValue;
		String predictionError;
		
		Prediction () {}
			
		
		Prediction (PredictionDashboard pd) {
			dtxcid=pd.getDtxcid();
			modelName=pd.getModel().getName();
//			smiles=pd.getDsstoxRecord().getSmiles();
			predictionValue=pd.getPredictionValue();
			predictionError=pd.getPredictionError();
		}
		
		@Override
		public String toString() {
			return dtxcid+"\t"+modelName+"\t"+smiles+"\t"+predictionValue+"\t"+predictionError;
		}
	}
	

	public SortedMap<String, SortedMap<String,Prediction>> convertPredictionsDashboardToMap(List<PredictionDashboard> pds) {
		
		SortedMap<String, SortedMap<String,Prediction>> map = new TreeMap<>();
		
		for (PredictionDashboard pd:pds) {
			Prediction p=new Prediction(pd);
			
//			System.out.println(p);
			
			if(map.get(p.dtxcid)==null) {
				SortedMap<String,Prediction>mapProperties=new TreeMap<>();
				mapProperties.put(p.modelName, p);
				map.put(p.dtxcid, mapProperties);
			} else {
				SortedMap<String,Prediction>mapProperties=map.get(p.dtxcid);
				mapProperties.put(p.modelName, p);
			}			
		}
				
		return map;
		
	}
	
	private SortedMap<String, SortedMap<String,Prediction>> getResults(HashSet<String>dtxsids, String folder, String filename) {
		
		SortedMap<String, SortedMap<String,Prediction>> map = new TreeMap<>();

		try {

			CSVReader reader = new CSVReader(new FileReader(folder+filename));
			String []colNames=reader.readNext();

			int linesRead=0;
			
			String[] nextRecord; 

			while ((nextRecord = reader.readNext()) != null) { 
				
				linesRead++;
				
				Prediction p=new Prediction();
				
				p.dtxcid=nextRecord[0];
				p.modelName=nextRecord[1];
				p.smiles=nextRecord[2];
				p.predictionValue=Double.parseDouble(nextRecord[3]);
												
				if(!dtxsids.contains(p.dtxcid)) continue;
				
//				if(p.dtxcid.contentEquals("DTXSID80838161")) {
//					System.out.println("\t"+p.modelName);
//				}
				
				if(map.get(p.dtxcid)==null) {
					SortedMap<String,Prediction>mapProperties=new TreeMap<>();
					mapProperties.put(p.modelName, p);
					map.put(p.dtxcid, mapProperties);
				} else {
					SortedMap<String,Prediction>mapProperties=map.get(p.dtxcid);
					mapProperties.put(p.modelName, p);
				}
			}
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return map;

	}
	

	private SortedMap<String, List<Prediction>> getResultsMapList(HashSet<String>dtxsids, String folder, String filename) {
		
		SortedMap<String, List<Prediction>> map = new TreeMap<>();

		try {

			CSVReader reader = new CSVReader(new FileReader(folder+filename));
			String []colNames=reader.readNext();

			int linesRead=0;
			
			String[] nextRecord; 

			while ((nextRecord = reader.readNext()) != null) { 
				
				linesRead++;
				
				Prediction p=new Prediction();
				
				p.dtxcid=nextRecord[0];
				p.modelName=nextRecord[1];
				p.smiles=nextRecord[2];
				p.predictionValue=Double.parseDouble(nextRecord[3]);
												
				if(!dtxsids.contains(p.dtxcid)) continue;
				
//				if(p.dtxcid.contentEquals("DTXSID80838161")) {
//					System.out.println("\t"+p.modelName);
//				}
				
				if(map.get(p.dtxcid)==null) {
					List<Prediction>mapProperties=new ArrayList<>();
					mapProperties.add(p);
					map.put(p.dtxcid, mapProperties);
				} else {
					List<Prediction>mapProperties=map.get(p.dtxcid);
					mapProperties.add(p);
				}
			}
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return map;

	}
	
	
	
	
	private HashSet<String> getDtxsids(String folder, String filename,boolean skipSalts) {
		
		HashSet<String>dtxsids=new HashSet<>();

		try {

			CSVReader reader = new CSVReader(new FileReader(folder+filename));
			String []colNames=reader.readNext();

			int linesRead=0;

			String[] nextRecord; 

			while ((nextRecord = reader.readNext()) != null) { 
				linesRead++;

				String dtxsid=nextRecord[0];
				String smiles=nextRecord[2];
				
				if(skipSalts && smiles.contains(".")) continue;
				
				dtxsids.add(dtxsid);
			}
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return dtxsids;

	}
	
private HashSet<String> printLines(String folder, String filename,String dtxsid) {
		
		HashSet<String>dtxsids=new HashSet<>();

		try {

			CSVReader reader = new CSVReader(new FileReader(folder+filename));
			String []colNames=reader.readNext();

			int linesRead=0;

			String[] nextRecord; 

			while ((nextRecord = reader.readNext()) != null) { 
				linesRead++;

				String dtxsidCurrent=nextRecord[0];
				String smiles=nextRecord[2];
				
				if(dtxsidCurrent.equals(dtxsid)) {
					System.out.println(nextRecord[1]);
				}
				
				
				dtxsids.add(dtxsid);
			}
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return dtxsids;

	}
	
	void compare() {
		
		String filepath_chemprop_sample="data\\percepta\\percepta_chemprop_sample.json";
		SortedMap<String, SortedMap<String,Prediction>>mapChemPropSample=getPredictionMap(filepath_chemprop_sample);

		String filepath_res_qsar_Sample="data\\percepta\\percepta_res_qsar_sample.json";
		SortedMap<String, SortedMap<String,Prediction>>mapResQsarSample=getPredictionMap(filepath_res_qsar_Sample);

		SortedMap<String,String>modelConverter=new TreeMap<>();
		
		modelConverter.put("ACD_BP","ACD_BP");
		modelConverter.put("ACD_FP","ACD_FP");
		modelConverter.put("ACD_LogP_v_LogP_Consensus","ACD_LogP_Consensus");
		modelConverter.put("ACD_pKa_Acidic","ACD_pKa_Apparent_MA");
		modelConverter.put("ACD_pKa_Basic","ACD_pKa_Apparent_MB");
		modelConverter.put("ACD_Prop_Density","ACD_Prop_Density");
		modelConverter.put("ACD_Prop_Index_Of_Refraction","ACD_Prop_Index_Of_Refraction");
		modelConverter.put("ACD_Prop_Molar_Refractivity","ACD_Prop_Molar_Refractivity");
		modelConverter.put("ACD_Prop_Molar_Volume","ACD_Prop_Molar_Volume");
		modelConverter.put("ACD_Prop_Polarizability","ACD_Prop_Polarizability");
		modelConverter.put("ACD_Prop_Surface_Tension","ACD_Prop_Surface_Tension");
		modelConverter.put("ACD_Sol","ACD_SolInPW");
		modelConverter.put("ACD_VP","ACD_VP");
		modelConverter.put("ACD_Prop_Dielectric_Constant","ACD_Prop_Dielectric_Constant");

		HashSet<String> badDTXSIDs=new HashSet<>();
		
		for (String modelNameChemProp:modelConverter.keySet()) {
			
			int diffCount=0;
			int chemicalCount=0;
			
			for (String dtxsid:mapChemPropSample.keySet()) {
				SortedMap<String,Prediction>mapPredictionsChemProp=mapChemPropSample.get(dtxsid);
				if(!mapPredictionsChemProp.containsKey(modelNameChemProp))continue;				
				Prediction predChemProp=mapPredictionsChemProp.get(modelNameChemProp);

				chemicalCount++;
				
				if(!mapResQsarSample.containsKey(dtxsid)) continue; 
				SortedMap<String,Prediction>mapPredictionsResQsar=mapResQsarSample.get(dtxsid);

				String modelNameResQsar=modelConverter.get(modelNameChemProp);
				if(!mapPredictionsResQsar.containsKey(modelNameResQsar))continue;
				
				Prediction predResQSAR=mapPredictionsResQsar.get(modelNameResQsar);

				if(predResQSAR.predictionError==null) {
					
					double diff=predChemProp.predictionValue-predResQSAR.predictionValue;

//					if(Math.abs(diff)>0.01) {
					if(Math.abs(diff)>0.01 && predChemProp.smiles.equals(predResQSAR.smiles)) {

						System.out.println(modelNameChemProp+"\t"+ predChemProp.dtxcid+"\t"+ predChemProp.predictionValue+"\t"+predResQSAR.predictionValue);
//						System.out.println(predChemProp.dtxcid+"\t"+ predChemProp.predictionValue+"\t"+predResQSAR.predictionValue+"\t"+predChemProp.smiles+"\t"+predResQSAR.smiles);
						diffCount++;						
						
						if(!modelNameChemProp.equals("ACD_Sol"))
							badDTXSIDs.add(dtxsid);						
					}
				}
			}
			
			System.out.println("\n"+modelNameChemProp+"\t"+diffCount+" of "+chemicalCount+"\n");

//			if(true) break;
		}
		
		System.out.println("Mismatch dtxsids:");
		
		for(String dtxsid:badDTXSIDs) {
			System.out.println(dtxsid);
		}
		
		
		
	}
	
	/**
	 * Goes through all the json files and only adds the ones that are in sample file
	 */
	void combineMapFiles () {
		
		
		String filepathChemPropSample="data\\percepta\\percepta_chemprop_sample.json";
		SortedMap<String, SortedMap<String,Prediction>>mapChemPropSample=getPredictionMap(filepathChemPropSample);

		
		File folder=new File("data\\percepta\\2024-10\\");
		
		SortedMap<String, SortedMap<String,Prediction>>mapAll=new TreeMap<>();
		
		for (File file:folder.listFiles()) {
			if(!file.getName().contains(".json")) continue;
			SortedMap<String, SortedMap<String,Prediction>>map=getPredictionMap(file.getAbsolutePath());
			
			for (String dtxsid:map.keySet()) {
				SortedMap<String,Prediction>propertyMap=map.get(dtxsid);				
				if(mapChemPropSample.containsKey(dtxsid)) mapAll.put(dtxsid, propertyMap);
			}
			System.out.println(file.getName()+"\t"+map.size()+"\t"+mapAll.size());
		}
		
		Utilities.saveJson(mapAll, "data\\percepta\\percepta_res_qsar_sample.json");
	}

	private SortedMap<String, SortedMap<String,Prediction>> getPredictionMap(String filepathChemPropSample) {
		try {
			Gson gson=new Gson();
			Reader reader = Files.newBufferedReader(Paths.get(filepathChemPropSample));
			
			
			Type type = new TypeToken<SortedMap<String, SortedMap<String,Prediction>>>() {}.getType();
			
			SortedMap<String, SortedMap<String,Prediction>>map=gson.fromJson(reader, type);

//			for(String dtxsid:map.keySet()) {
//				System.out.println(dtxsid);
//			}
			
			return map;
			
		} catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	
	

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String folder="data\\percepta\\";
		String filename="percepta_chemprop.csv";
		CompareToChemprop c=new CompareToChemprop();
		
//		c.getSampleMapFromChempropResults(folder, filename);
//		c.printLines(folder, filename, "DTXSID80838161");
//		
//		c.combineMapFiles();
		c.compare();
	}

}
