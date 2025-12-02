package gov.epa.run_from_java.scripts.custom_datasets;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.*;
import java.util.stream.Collectors;

import org.json.CDL;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetServiceImpl;
import gov.epa.endpoints.datasets.descriptor_values.SciDataExpertsDescriptorValuesCalculator;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer;
import kong.unirest.HttpResponse;

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
		String descriptorSetName="Mordred-default";
//		String descriptorSetName="RDKit-default";

		
//		String serverHost="https://hazard-dev.sciencedataexperts.com";
		String serverHost = "https://hcd.rtpnc.epa.gov";
		SciDataExpertsDescriptorValuesCalculator descriptorCalculator=new SciDataExpertsDescriptorValuesCalculator(serverHost, "tmarti02");

		
		try {
			InputStream inputStream= new FileInputStream(folder+inputFileName);

			BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
			String csvAsString = br.lines().collect(Collectors.joining("\n"));

			DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);

			String descriptorAbbrev=descriptorSet.getDescriptorService();
			
			String outputFileName="LC50_Tr_descriptors_"+descriptorAbbrev+".tsv";
			String outputFilePath=folder+outputFileName;
			File of=new File(outputFilePath);
			Hashtable<String,String>ht=new Hashtable<String,String>();
			
			boolean haveOF=of.exists();
			
			if(haveOF) ht=getDescriptorsHashtable(outputFilePath);

//			System.out.println(csvAsString);

			br.close();

			String json = CDL.toJSONArray(csvAsString).toString();
			//		System.out.println("Done loading results file");

			JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);


			FileWriter fw=new FileWriter(outputFilePath,of.exists());
			
			if(!haveOF)
				fw.write("QSAR_READY_SMILES\t"+descriptorSet.getHeadersTsv()+"\r\n");	
			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				
				String QSAR_READY_SMILES=jo.get("QSAR_READY_SMILES").getAsString();
				
				if(ht.containsKey(QSAR_READY_SMILES)) continue;
				
				String descriptorsTsv=null;
				descriptorsTsv=descriptorCalculator.calculateDescriptors(QSAR_READY_SMILES, descriptorSet);

				System.out.println((i+1)+"\t"+QSAR_READY_SMILES+"\t"+descriptorsTsv);
				
				if (descriptorsTsv==null) {
					fw.write(QSAR_READY_SMILES+"\tError\r\n");
					
				} else {
					fw.write(QSAR_READY_SMILES+"\t"+descriptorsTsv+"\r\n");
					ht.put(QSAR_READY_SMILES, descriptorsTsv);
				}
				fw.flush();
			}		
			fw.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void generateDescriptorFilePrediction() {

		String folder="data\\modeling\\CoMPAIT\\";
		String inputFileName="PredictionSet.csv";
		
//		String descriptorSetName="WebTEST-default";
		String descriptorSetName="Mordred-default";
//		String descriptorSetName="RDKit-default";

		String serverHost = "https://hcd.rtpnc.epa.gov";
//		String serverHost="https://hazard-dev.sciencedataexperts.com";
		
		SciDataExpertsDescriptorValuesCalculator descriptorCalculator=new SciDataExpertsDescriptorValuesCalculator(serverHost, "tmarti02");

		String workflow = "qsar-ready_08232023";
//		String serverHost = "https://hazard-dev.sciencedataexperts.com";
		
		SciDataExpertsStandardizer standardizer = new SciDataExpertsStandardizer(workflow, serverHost);
		boolean useFullStandardize=false;
		
		
		
		try {
			InputStream inputStream= new FileInputStream(folder+inputFileName);

			BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
			String csvAsString = br.lines().collect(Collectors.joining("\n"));

			DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);

			String descriptorAbbrev=descriptorSet.getDescriptorService();
			
			String outputFileName="LC50_Prediction_descriptors_"+descriptorAbbrev+".tsv";
			String outputFilePath=folder+outputFileName;
			
			String predictionEditSmilesPath=folder+"LC50_Prediction_descriptors_edit_smiles.tsv";
			File of=new File(outputFilePath);
			
			Hashtable<String,String>ht=new Hashtable<String,String>();
			
			boolean haveOF=of.exists();
			
			if(haveOF) ht=getDescriptorsHashtable(outputFilePath);
			
			System.out.println(ht.size()+"\talready ran");
			
//			if(true)return;
			

//			System.out.println(csvAsString);

			br.close();

			String json = CDL.toJSONArray(csvAsString).toString();
			//		System.out.println("Done loading results file");

			JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);

			
			FileWriter fw=new FileWriter(outputFilePath,of.exists());
			FileWriter fw2=new FileWriter(predictionEditSmilesPath);	

			fw2.write("DSSTOX_SUBSTANCE_ID\tOriginal_SMILES\tQSAR_READY_SMILES_OPERA\tQSAR_READY_SMILES_SDE\tQSAR_READY_SMILES_SDE_LONGEST\r\n");
			
			if(!haveOF)
				fw.write("QSAR_READY_SMILES\t"+descriptorSet.getHeadersTsv()+"\r\n");
			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				
				String QSAR_READY_SMILES_OPERA=jo.get("QSAR_READY_SMILES").getAsString();
				String Original_SMILES=jo.get("Original_SMILES").getAsString();
				String DSSTOX_SUBSTANCE_ID=jo.get("DSSTOX_SUBSTANCE_ID").getAsString();;
				
				if(QSAR_READY_SMILES_OPERA.equals("CC(O)C(=O)OC(C)C(=O)OC(C)C(=O)OC(C)C(=O)OC(COC(=O)C(C)OC(=O)C(C)OC(=O)C(C)OC(=O)C(C)O)C(OC(=O)C(C)OC(=O)C(C)OC(=O)C(C)OC(=O)C(C)O)C(OC(=O)C(C)OC(=O)C(C)OC(=O)C(C)OC(=O)C(C)O)C(COC(=O)C(C)OC(=O)C(C)OC(=O)C(C)OC(=O)C(C)O)OC(=O)C(C)OC(=O)C(C)OC(=O)C(C)OC(=O)C(C)O")) {
					System.out.println("Found long one");
					System.out.println(ht.containsKey(QSAR_READY_SMILES_OPERA));
				}
					
				
				
				if(ht.containsKey(QSAR_READY_SMILES_OPERA)) continue;
				
				String descriptorsTsv=null;
				descriptorsTsv=descriptorCalculator.calculateDescriptors(QSAR_READY_SMILES_OPERA, descriptorSet);

//				System.out.println((i+1)+"\t"+QSAR_READY_SMILES+"\t"+descriptorsTsv);
				
				
				if(i%100==0) System.out.println(i);
				
				if (descriptorsTsv==null) {
					
					HttpResponse<String> standardizeResponse = standardizer.callQsarReadyStandardizePost(Original_SMILES, useFullStandardize);
					if (standardizeResponse.getStatus() == 200) {
						String jsonResponse = SciDataExpertsStandardizer.getResponseBody(standardizeResponse, useFullStandardize);
						String qsarSmilesSDE = SciDataExpertsStandardizer.getQsarReadySmilesFromPostJson(jsonResponse,
								useFullStandardize);
						

						String qsarSmilesSDE_Longest="";
						List<String>smilesList=new ArrayList<String>();
						String [] smilesSplit=qsarSmilesSDE.split("\\.");
						
						smilesList.add(qsarSmilesSDE);
						for(String smiles:smilesSplit) {
							if(smiles.length()>qsarSmilesSDE_Longest.length()) qsarSmilesSDE_Longest=smiles;
							if(!smilesList.contains(smiles)) smilesList.add(smiles);
						}
						
						System.out.println("\n"+qsarSmilesSDE);
						
						for(int j=0;j<smilesList.size();j++) {
							String smiles=smilesList.get(j);
							if(ht.containsKey(smiles)) continue;
							
							System.out.println("\t"+j+"\t"+smiles);
							
							descriptorsTsv=descriptorCalculator.calculateDescriptors(smiles, descriptorSet);

							if(descriptorsTsv!=null) {
								fw.write(smiles+"\t"+descriptorsTsv+"\r\n");
								ht.put(smiles, descriptorsTsv);
								fw.flush();
							}

						}
						fw2.write(DSSTOX_SUBSTANCE_ID+"\t"+Original_SMILES+"\t"+QSAR_READY_SMILES_OPERA+"\t"+qsarSmilesSDE+"\t"+qsarSmilesSDE_Longest+"\r\n");
						fw2.flush();
						
						
					} else {
						System.out.println((i+1)+"\t"+Original_SMILES+"\tstatus="+standardizeResponse.getStatus());
						fw2.write(DSSTOX_SUBSTANCE_ID+"\t"+Original_SMILES+"\t"+QSAR_READY_SMILES_OPERA+"\tN/A\tN/A"+"\r\n");

					}

					
//					fw.write(QSAR_READY_SMILES+"\tError\r\n");
//					System.out.println((i+1)+"\t"+QSAR_READY_SMILES+"\tError");
				} else {
//					System.out.println((i+1)+"\t"+QSAR_READY_SMILES+"\tOK");
					fw.write(QSAR_READY_SMILES_OPERA+"\t"+descriptorsTsv+"\r\n");
					ht.put(QSAR_READY_SMILES_OPERA, descriptorsTsv);
					fw.flush();
				}
				
			}		
			fw.close();
			fw2.flush();
			fw2.flush();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		InhalationToxFileGeneration i=new InhalationToxFileGeneration();
//		i.generateDescriptorFile();
		i.generateDescriptorFilePrediction();
//		i.createSplittingFiles();
//		i.createSplittingFilesIncludeOPERA();
//		i.getDescriptorsHashtable();
//		i.getDescriptorsHashtableOPERA();

	}

}
