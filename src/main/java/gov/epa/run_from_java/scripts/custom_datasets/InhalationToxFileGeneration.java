package gov.epa.run_from_java.scripts.custom_datasets;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.*;
import java.util.stream.Collectors;

import org.json.CDL;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetServiceImpl;
import gov.epa.endpoints.datasets.descriptor_values.SciDataExpertsDescriptorValuesCalculator;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

/**
* @author TMARTI02
*/
public class InhalationToxFileGeneration {

	DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();

	
	Hashtable<String,String> getDescriptorsHashtable(String filepath) {
		
		Hashtable<String,String>htDesc=new Hashtable<String,String>();
		
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			String header=br.readLine();
			
			while(true) {
				String Line=br.readLine();
				if(Line==null) break;
				String smiles=Line.substring(0,Line.indexOf("\t"));
				String desc=Line.substring(Line.indexOf("\t")+1,Line.length());
				
				if(!desc.equals("Error"))	htDesc.put(smiles,desc);
//				System.out.println(desc);
			}
			
			br.close();
			
		}catch (Exception ex) {
			ex.printStackTrace();
		}
		return htDesc;
		
		
	}
	
public static Hashtable<String,String> getDescriptorsHashtableOPERA(String filepath) {
		
		Hashtable<String,String>htDesc=new Hashtable<String,String>();
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			String header=br.readLine();
			
			String varNames=header.substring(header.indexOf(",")+1,header.length()).replace(",", "\t");
			
			htDesc.put("header", varNames);
			
			while(true) {
				String Line=br.readLine();
				if(Line==null) break;
				
				String smiles=Line.substring(0,Line.indexOf(","));
				String desc=Line.substring(Line.indexOf(",")+1,Line.length()).replace(",", "\t");
				htDesc.put(smiles,desc);
				
//				System.out.println(smiles+"\t"+desc);
				
//				System.out.println(desc);
			}
			
			br.close();
			
		}catch (Exception ex) {
			ex.printStackTrace();
		}
		return htDesc;
		
		
	}
	
	
	void createSplittingFiles() {

		String units="ppm";
//		String units="mgL";
		
//		String descriptorSetName="WebTEST-default";
//		String descriptorSetName="PaDEL-default";
		String descriptorSetName="Mordred-default";

		int NumFolds=5;
		String folder="data\\modeling\\CoMPAIT\\";
		String inputFileName="LC50_Tr_modeling_set_all_folds.csv";

		try {

			InputStream inputStream= new FileInputStream(folder+inputFileName);
			BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
			String csvAsString = br.lines().collect(Collectors.joining("\n"));
//			System.out.println(csvAsString);
			
			List<List<DataPoint>> dpLists=new ArrayList<>(); // vector of array lists
			for (int i=0;i<NumFolds;i++) {
				dpLists.add(new ArrayList<DataPoint>());
			}

			String json = CDL.toJSONArray(csvAsString).toString();
			JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);
			DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);
			String fileNameDescriptors="LC50_Tr_descriptors_"+descriptorSet.getDescriptorService()+".tsv";
			Hashtable<String,String>htDesc= getDescriptorsHashtable(folder+fileNameDescriptors);
			
//			fw.write("QSAR_READY_SMILES\t"+descriptorSet.getHeadersTsv()+"\r\n");
			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				String QSAR_READY_SMILES=jo.get("QSAR_READY_SMILES").getAsString();
				double propertyValue=jo.get("4_hr_value_"+units).getAsDouble();
				String strFold=jo.get("Fold").getAsString();
				int iFold=Integer.parseInt(strFold.replace("Fold", ""));
//				System.out.println(QSAR_READY_SMILES+"\t"+propertyValue+"\t"+iFold);
				List<DataPoint>dpsFold=dpLists.get(iFold-1);
				Dataset dataset=null;
				DataPoint dp=new DataPoint(QSAR_READY_SMILES, propertyValue, dataset, false, "tmarti02");
				dpsFold.add(dp);
			}
			
			
//			for (int i=0;i<NumFolds;i++) {
//				List<DataPoint> dpsFold=v.get(i);
//				System.out.println((i+1)+"\t"+dpsFold.size());
//			}


			List<FileWriter>fwTrainList=new ArrayList<>();
			List<FileWriter>fwTestList=new ArrayList<>();
			
			String header="QSAR_READY_SMILES\tlog10_LC50_"+units+"\t"+descriptorSet.getHeadersTsv();
			
			for (int i=0;i<NumFolds;i++) {
				
				String trainPathi=folder+"LC50_tr_log10_"+units+"_"+descriptorSet.getDescriptorService()+"_train_CV"+(i+1)+".tsv";
				String testPathi=folder+"LC50_tr_log10_"+units+"_"+descriptorSet.getDescriptorService()+"_test_CV"+(i+1)+".tsv";
				FileWriter fwTrain=new FileWriter(trainPathi);
				FileWriter fwTest=new FileWriter(testPathi);
				
				fwTrain.write(header+"\r\n");
				fwTest.write(header+"\r\n");
				
				fwTrainList.add(fwTrain);
				fwTestList.add(fwTest);
			}
			
			for (int i=0;i<NumFolds;i++) {
				List<DataPoint> dpsFold=dpLists.get(i);
				for (DataPoint dp:dpsFold) {
					for (int j=0;j<NumFolds;j++) {
						
						FileWriter fw=null;
						if(i==j) fw=fwTestList.get(j);
						else fw=fwTrainList.get(j);
						
						String descVals=htDesc.get(dp.getCanonQsarSmiles());
						
						if(descVals==null) {
							System.out.println("no desc for "+dp.getCanonQsarSmiles());
							continue;
						}
						
						fw.write(dp.getCanonQsarSmiles()+"\t"+dp.getQsarPropertyValue()+"\t"+descVals+"\r\n");
						fw.flush();
					}
				}
			}
			
			
			for (int i=0;i<NumFolds;i++) {
				fwTrainList.get(i).close();
				fwTestList.get(i).close();
			}
			
			
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	void createSplittingFilesIncludeOPERA() {

		String units="ppm";
//		String units="mgL";
		
		int NumFolds=5;
		String folder="data\\modeling\\CoMPAIT\\";
		String inputFileName="LC50_Tr_modeling_set_all_folds.csv";
		
//		String descriptorSetName="WebTEST-default";
		String descriptorSetName="Mordred-default";
		

		try {

			InputStream inputStream= new FileInputStream(folder+inputFileName);
			BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
			String csvAsString = br.lines().collect(Collectors.joining("\n"));
//			System.out.println(csvAsString);
			
			List<List<DataPoint>> v=new ArrayList<>(); // vector of array lists
			for (int i=0;i<NumFolds;i++) {
				v.add(new ArrayList<DataPoint>());
			}

			String json = CDL.toJSONArray(csvAsString).toString();

			JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);


			
			DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);


			String fileNameDescriptors="LC50_Tr_descriptors_"+descriptorSet.getDescriptorService()+".tsv";
			Hashtable<String,String>htDesc= getDescriptorsHashtable(folder+fileNameDescriptors);

			
			String operaFileName="LC50_qsar_smiles-smi_OPERA2.9Pred2.csv";
			Hashtable<String,String>htDescOpera= getDescriptorsHashtableOPERA(folder+operaFileName);
			
			String descriptorSet2=descriptorSet.getDescriptorService()+"_opera";
			
			
//			fw.write("QSAR_READY_SMILES\t"+descriptorSet.getHeadersTsv()+"\r\n");
			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				String QSAR_READY_SMILES=jo.get("QSAR_READY_SMILES").getAsString();
				double propertyValue=jo.get("4_hr_value_"+units).getAsDouble();
				String strFold=jo.get("Fold").getAsString();
				int iFold=Integer.parseInt(strFold.replace("Fold", ""));
//				System.out.println(QSAR_READY_SMILES+"\t"+propertyValue+"\t"+iFold);
				List<DataPoint>dpsFold=v.get(iFold-1);
				Dataset dataset=null;
				DataPoint dp=new DataPoint(QSAR_READY_SMILES, propertyValue, dataset, false, "tmarti02");
				dpsFold.add(dp);
			}
			
			
//			for (int i=0;i<NumFolds;i++) {
//				List<DataPoint> dpsFold=v.get(i);
//				System.out.println((i+1)+"\t"+dpsFold.size());
//			}


			List<FileWriter>fwTrainList=new ArrayList<>();
			List<FileWriter>fwTestList=new ArrayList<>();
			
			String header="QSAR_READY_SMILES\tlog10_LC50_"+units+"\t"+descriptorSet.getHeadersTsv();
			
			for (int i=0;i<NumFolds;i++) {
				
				String trainPathi=folder+"LC50_tr_log10_"+units+"_"+descriptorSet2+"_train_CV"+(i+1)+".tsv";
				String testPathi=folder+"LC50_tr_log10_"+units+"_"+descriptorSet2+"_test_CV"+(i+1)+".tsv";
				FileWriter fwTrain=new FileWriter(trainPathi);
				FileWriter fwTest=new FileWriter(testPathi);
				
				fwTrain.write(header+"\t"+htDescOpera.get("header")+"\r\n");
				fwTest.write(header+"\t"+htDescOpera.get("header")+"\r\n");
				
				fwTrainList.add(fwTrain);
				fwTestList.add(fwTest);
			}
			
			for (int i=0;i<NumFolds;i++) {
				List<DataPoint> dpsFold=v.get(i);
				for (DataPoint dp:dpsFold) {
					for (int j=0;j<NumFolds;j++) {
						
						FileWriter fw=null;
						if(i==j) fw=fwTestList.get(j);
						else fw=fwTrainList.get(j);
						
						String descVals=htDesc.get(dp.getCanonQsarSmiles());
						String descValsOpera=htDescOpera.get(dp.getCanonQsarSmiles());
						
						if(descVals==null) {
							System.out.println("no desc for "+dp.getCanonQsarSmiles());
							continue;
						}
						
						fw.write(dp.getCanonQsarSmiles()+"\t"+dp.getQsarPropertyValue()+"\t"+descVals+"\t"+descValsOpera+"\r\n");
						fw.flush();
					}
				}
			}
			
			
			for (int i=0;i<NumFolds;i++) {
				fwTrainList.get(i).close();
				fwTestList.get(i).close();
			}
			
			
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void generateDescriptorFile() {

		String folder="data\\modeling\\CoMPAIT\\";
		String inputFileName="LC50_Tr.csv";
		
//		String descriptorSetName="WebTEST-default";
//		String descriptorSetName="Mordred-default";
		String descriptorSetName="RDKit-default";

		
		String server="https://hazard-dev.sciencedataexperts.com";
		SciDataExpertsDescriptorValuesCalculator descriptorCalculator=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");

		
		try {
			InputStream inputStream= new FileInputStream(folder+inputFileName);

			BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
			String csvAsString = br.lines().collect(Collectors.joining("\n"));

			DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);

			String descriptorAbbrev=descriptorSet.getDescriptorService();
			
			String outputFileName="LC50_Tr_descriptors_"+descriptorAbbrev+".tsv";
			FileWriter fw=new FileWriter(folder+outputFileName);
			

//			System.out.println(csvAsString);

			br.close();

			String json = CDL.toJSONArray(csvAsString).toString();
			//		System.out.println("Done loading results file");

			JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);



			
			fw.write("QSAR_READY_SMILES\t"+descriptorSet.getHeadersTsv()+"\r\n");
			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				
				String QSAR_READY_SMILES=jo.get("QSAR_READY_SMILES").getAsString();
				String descriptorsTsv=null;
				descriptorsTsv=descriptorCalculator.calculateDescriptors(QSAR_READY_SMILES, descriptorSet);

				System.out.println((i+1)+"\t"+QSAR_READY_SMILES+"\t"+descriptorsTsv);
				
				if (descriptorsTsv==null) {
					fw.write(QSAR_READY_SMILES+"\tError\r\n");
					
				} else {
					fw.write(QSAR_READY_SMILES+"\t"+descriptorsTsv+"\r\n");
				}
				fw.flush();
			}		
			fw.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] args) {
		InhalationToxFileGeneration i=new InhalationToxFileGeneration();
//		i.generateDescriptorFile();
//		i.createSplittingFiles();
		i.createSplittingFilesIncludeOPERA();
//		i.getDescriptorsHashtable();
//		i.getDescriptorsHashtableOPERA();

	}

}
