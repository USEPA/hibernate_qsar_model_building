package gov.epa.run_from_java.scripts.custom_datasets;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.CDL;

import com.google.gson.*;

import java.util.*;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.SplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetServiceImpl;
import gov.epa.endpoints.datasets.descriptor_values.SciDataExpertsDescriptorValuesCalculator;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.ExcelCreator;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.util.ExcelSourceReader;
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer;
import kong.unirest.HttpResponse;

/**
* @author TMARTI02
*/
public class TTR_Binding_Challenge {
	
	SplittingServiceImpl ssi= new SplittingServiceImpl();
	DataPointServiceImpl ds=new DataPointServiceImpl();
	DataPointInSplittingServiceImpl dpisService=new DataPointInSplittingServiceImpl();

	
	void createSplits() {
				
		List<DataPoint>dps=ds.findByDatasetName("TTR_Binding_training_remove_bad_max_conc");
		System.out.println(dps.size());
		
//		createBaseSplitting(dps);
		createRandomSplittings(5, dps);

	}
	
	void writeOfflineCVFiles() {

//		String descriptorSetName="WebTEST-default";
		String descriptorSetName="PaDEL-default";
//		String descriptorSetName="Mordred-default";
		
		DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();
		DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);
		
		for (int fold=1;fold<=5;fold++) {
			String sql="select dp.canon_qsar_smiles,dp.qsar_property_value, dv.values_tsv, dpis.split_num from qsar_datasets.data_points dp\n"+
		    "join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\n"+
		    "join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\n"+
		    "join qsar_datasets.splittings s on dpis.fk_splitting_id = s.id\n"+
		    "join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles\n"+
		    "join qsar_descriptors.descriptor_sets ds on dv.fk_descriptor_set_id = ds.id\n"+
		    "where d.name='TTR_Binding_training_remove_bad_max_conc' and "
		    + "s.name='RND_REPRESENTATIVE_CV"+fold+"' and "
		    + "ds.name='"+descriptorSetName+"';";
//			System.out.println(sql+"\n");
			
			try {
				
				String folder="data\\modeling\\TTR_Binding_challenge\\";
				
				String trainPathi=folder+"TTR_Binding_"+descriptorSet.getDescriptorService()+"_train_CV"+fold+".tsv";
				String testPathi=folder+"TTR_Binding_"+descriptorSet.getDescriptorService()+"_test_CV"+fold+".tsv";

				FileWriter fwTrain=new FileWriter(trainPathi);
				FileWriter fwTest=new FileWriter(testPathi);
				
				String header="QSAR_READY_SMILES\tmedianPercentActivityAtMaxConc\t"+descriptorSet.getHeadersTsv();

				fwTrain.write(header+"\r\n");
				fwTest.write(header+"\r\n");
				
				ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
				
				while (rs.next()) {
					
					String canon_qsar_smiles=rs.getString(1);
					String qsar_property_value=rs.getString(2);
					String values_tsv=rs.getString(3);
					int split_num=rs.getInt(4);
					
					if(split_num==0) {
						fwTrain.write(canon_qsar_smiles+"\t"+qsar_property_value+"\t"+values_tsv+"\r\n");
						fwTrain.flush();
					} else if (split_num==1) {
						fwTest.write(canon_qsar_smiles+"\t"+qsar_property_value+"\t"+values_tsv+"\r\n");
						fwTest.flush();
					}
				}
				
				fwTrain.close();
				fwTest.close();
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	void writeOfflineCVFilesWithOPERA() {

		String descriptorSetName="WebTEST-default";
//		String descriptorSetName="PaDEL-default";
//		String descriptorSetName="Mordred-default";
		
		DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();
		DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);
		
		for (int fold=1;fold<=5;fold++) {
			String sql="select dp.canon_qsar_smiles,dp.qsar_property_value, dv.values_tsv, dpis.split_num from qsar_datasets.data_points dp\n"+
		    "join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\n"+
		    "join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\n"+
		    "join qsar_datasets.splittings s on dpis.fk_splitting_id = s.id\n"+
		    "join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles\n"+
		    "join qsar_descriptors.descriptor_sets ds on dv.fk_descriptor_set_id = ds.id\n"+
		    "where d.name='TTR_Binding_training_remove_bad_max_conc' and "
		    + "s.name='RND_REPRESENTATIVE_CV"+fold+"' and "
		    + "ds.name='"+descriptorSetName+"';";
//			System.out.println(sql+"\n");
			
			try {
				
				String folder="data\\modeling\\TTR_Binding_challenge\\";
				
				Hashtable<String,String>htOpera=InhalationToxFileGeneration.getDescriptorsHashtableOPERA(folder+"training set-smi_OPERA2.9Pred3.csv");
				
				
				String trainPathi=folder+"TTR_Binding_"+descriptorSet.getDescriptorService()+"_opera_train_CV"+fold+".tsv";
				String testPathi=folder+"TTR_Binding_"+descriptorSet.getDescriptorService()+"_opera_test_CV"+fold+".tsv";

				FileWriter fwTrain=new FileWriter(trainPathi);
				FileWriter fwTest=new FileWriter(testPathi);
				
				String header="QSAR_READY_SMILES\tmedianPercentActivityAtMaxConc\t"+descriptorSet.getHeadersTsv()+"\t"+htOpera.get("header");

				fwTrain.write(header+"\r\n");
				fwTest.write(header+"\r\n");
				
				ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
				
				while (rs.next()) {
					
					String canon_qsar_smiles=rs.getString(1);
					String qsar_property_value=rs.getString(2);
					String values_tsv=rs.getString(3);
					String values_opera_pred=htOpera.get(canon_qsar_smiles);
					int split_num=rs.getInt(4);
					
					if(split_num==0) {
						fwTrain.write(canon_qsar_smiles+"\t"+qsar_property_value+"\t"+values_tsv+"\t"+values_opera_pred+"\r\n");
						fwTrain.flush();
					} else if (split_num==1) {
						fwTest.write(canon_qsar_smiles+"\t"+qsar_property_value+"\t"+values_tsv+"\t"+values_opera_pred+"\r\n");
						fwTest.flush();
					}
				}
				
				fwTrain.close();
				fwTest.close();
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Need to create dpis for RND_REPRESENTATIVE (even though all in the training set)
	 * @param dps
	 */
	void createBaseSplitting(List<DataPoint>dps) {

		List<DataPointInSplitting>dpisList=new ArrayList<DataPointInSplitting>();

		Splitting splitting=ssi.findByName("RND_REPRESENTATIVE");
		
		for (DataPoint dp:dps) {
			DataPointInSplitting dpis=new DataPointInSplitting(dp,splitting,0,"tmarti02");
			dpisList.add(dpis);
		}
		dpisService.createSQL(dpisList);
	}
	
	
	void createRandomSplittings(int NumFolds,List<DataPoint>dps) {

		try {
			
			Collections.shuffle(dps);//randomly shuffle chemicals
			List<List<DataPoint>> v=new ArrayList<>(); // vector of array lists
			for (int i=0;i<NumFolds;i++) {
				v.add(new ArrayList<DataPoint>());
			}

			int NumInFold=(int)Math.floor((double)dps.size()/(double)NumFolds);
						
			System.out.println("Number in each fold="+NumInFold);

			for (int fold=0;fold<NumFolds;fold++) {
				List<DataPoint> dpsFold=v.get(fold);
				for (int j=0;j<NumInFold;j++) {
					dpsFold.add(dps.remove(0));
				}
			}


			//Add remaining to first fold
			List<DataPoint> dpsFold0=v.get(0);
			for (int j=0;j<dps.size();j++) {
				dpsFold0.add(dps.remove(0));
			}
			
			
			List<Splitting>splittings=new ArrayList<Splitting>();
			
			for (int i=1;i<=NumFolds;i++) {
				splittings.add(ssi.findByName("RND_REPRESENTATIVE_CV"+i));
			}
			
			List<DataPointInSplitting>dpisList=new ArrayList<DataPointInSplitting>();
			
			for (int i=0;i<NumFolds;i++) {
				List<DataPoint> dpsFold=v.get(i);
				for (DataPoint dp:dpsFold) {
					for (int j=0;j<NumFolds;j++) {
						if(i==j) {
							DataPointInSplitting dpis=new DataPointInSplitting(dp,splittings.get(j),1,"tmarti02");
							dpisList.add(dpis);
						} else {
							DataPointInSplitting dpis=new DataPointInSplitting(dp,splittings.get(j),0,"tmarti02");
							dpisList.add(dpis);
						}
					}
				}
			}
			
			int countCV1=0;
			for (DataPointInSplitting dpis:dpisList) {
				System.out.println(dpis.getDataPoint().getCanonQsarSmiles()+"\t"+dpis.getSplitting().getName()+"\t"+dpis.getSplitNum());
				if (dpis.getSplitting().getName().equals("RND_REPRESENTATIVE_CV1")) countCV1++;
			}
			
//			System.out.println("countCV1="+countCV1);
			
			//Store splittings in db:
			dpisService.createSQL(dpisList);
			
			
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void parseOperaFile() {

		
		String folder="data\\modeling\\TTR_Binding_challenge\\";
		String filepath=folder+"training set-smi_OPERA2.9Pred2.csv";
		
	
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(filepath);

			BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
			FileWriter fw=new FileWriter(folder+"training set-smi_OPERA2.9Pred3.csv");
			
			String csvAsString = br.lines().collect(Collectors.joining("\n"));
//			csvAsString=csvAsString.replace("\t", ",");
//			System.out.println(csvAsString);
			br.close();
			
			String json = CDL.toJSONArray(csvAsString).toString();
//			System.out.println(json);
			
			JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);
			
			JsonObject jo0=ja.get(0).getAsJsonObject();
			Set<Map.Entry<String, JsonElement>> entrySet = jo0.entrySet();
			
			List<String>fields=new ArrayList<String>();
			
			fields.add("canon_qsar_smiles");
			
			for(Map.Entry<String,JsonElement> entry : entrySet){
				if(entry.getKey().contains("_pred") && !entry.getKey().contains("Range")) {
//					System.out.println(entry.getKey());
					fields.add(entry.getKey());
				}
			}

			for (int i=0;i<fields.size();i++) {
				fw.write(fields.get(i));
				if(i<fields.size()-1) {
					fw.write(",");
				} else {
					fw.write("\n");
				}
			}
			
			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				for (int j=0;j<fields.size();j++) {
					
					if (j==0) {
						fw.write(jo.get(fields.get(j)).getAsString());
					} else {
						fw.write(jo.get(fields.get(j)).getAsDouble()+"");						
					}
					
					if(j<fields.size()-1) {
						fw.write(",");
					} else {
						fw.write("\n");
					}
				}
				
				
//				System.out.println(canon_qsar_smiles);
				
			}
			fw.close();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	
	void goThroughExcelFile() {
		try {
			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2024 tetko challenge\\";
//			String filepath=folder+"ttr-supplemental-tables.xlsx";
			String filepath=folder+"ttr-supplemental-tables-with-leaderboard_values.xlsx";
//			String filepathOut=folder+"ttr-supplemental-tables-annotated.xlsx";
			
			FileInputStream fis = new FileInputStream(new File(filepath));
			Workbook wb = WorkbookFactory.create(fis);
			Sheet sheet = wb.getSheet("S4-Single concentration");
			
			JsonArray ja=ExcelSourceReader.parseRecordsFromExcel(sheet);
			
			
//			String workflow = "qsar-ready";
			String workflow = "qsar-ready_08232023";
			
			String serverHost = "https://hcd.rtpnc.epa.gov";
//			String serverHost = "https://hazard-dev.sciencedataexperts.com";
			
			SciDataExpertsStandardizer standardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY,
					workflow, serverHost);

			boolean useFullStandardize=false;
			
//			DTXSID	Chemical	CASRN	SMILES	Library	Max conc	Median % activity	Tested in CR?	dataset

			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				
				String dtxsid=jo.get("DTXSID").getAsString();
				String dataset=jo.get("dataset").getAsString();
				
//				if(dataset.equals("training")) continue;
				
				getDSSTOXInfo(jo);
				
//				if(jo.get("Median % activity")==null || jo.get("Median % activity").isJsonNull()) {
//					System.out.println(dtxsid+"\t"+dataset+"\tnull activity");	
//				}
				
//				double activity=jo.get("Median % activity").getAsDouble();

				String smiles=null;
				
				if(jo.get("smiles_DSSTOX")!=null && !jo.get("smiles_DSSTOX").isJsonNull()) {
					smiles=jo.get("smiles_DSSTOX").getAsString();	
				}
				String qsarSmilesSDE=null;
				
//				if(i==10) break;
				
				if (smiles!=null) {
					HttpResponse<String> standardizeResponse = standardizer.callQsarReadyStandardizePost(smiles, useFullStandardize);
					if (standardizeResponse.getStatus() == 200) {
						String jsonResponse = SciDataExpertsStandardizer.getResponseBody(standardizeResponse, useFullStandardize);
						qsarSmilesSDE = SciDataExpertsStandardizer.getQsarReadySmilesFromPostJson(jsonResponse,
								useFullStandardize);
						
						jo.addProperty("qsarSmilesSDE", qsarSmilesSDE);
						System.out.println(i+"\t"+smiles+"\t"+qsarSmilesSDE);
					} 
				}
				
			}
			
//			for(int i=0;i<10;i++) {
//				System.out.println(Utilities.gson.toJson(ja.get(i).getAsJsonObject()));	
//			}
			
			
			FileWriter fw=new FileWriter(filepath.replace(".xlsx", ".json"));
			fw.write(Utilities.gson.toJson(ja));
			fw.flush();
			fw.close();
					
//			String[] fields = { "DTXSID", "Chemical", "CASRN", "SMILES", "Library", "Max conc", "Median % activity",
//					"Tested in CR?", "dataset", "substance_type", "relationship", "chemical_type", "organic_form",
//					"smiles_DSSTOX", "qsarSmilesOPERA", "qsarSmilesSDE" };
//			
//			ExcelCreator.createExcel2(ja, filepathOut, fields, null);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void goThroughExcelFileAQC() {

		try {
			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2024 tetko challenge\\";
//			String filepath=folder+"TTR-supplemental-tables_with Annotations_AJW_0627_2024.xlsx";
			String filepath=folder+"TTR-supplemental-tables_with Annotations_AJW_0719_2024.xlsx";
			
			
			FileInputStream fis = new FileInputStream(new File(filepath));
			Workbook wb = WorkbookFactory.create(fis);
			Sheet sheet = wb.getSheet("S4-Single concentration");
			
			JsonArray ja=ExcelSourceReader.parseRecordsFromExcel(sheet);
			
			FileWriter fw=new FileWriter(filepath.replace(".xlsx", ".json"));
			fw.write(Utilities.gson.toJson(ja));
			fw.flush();
			fw.close();
					
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void getDSSTOXInfo(JsonObject jo) {
		
		String DTXSID=jo.get("DTXSID").getAsString();
				
		
		String sql="select gs.dsstox_substance_id,  substance_type, gsc.relationship, c.chemical_type, c.organic_form, c.smiles, c2.smiles as qsar_smiles_opera from generic_substances gs\r\n"
				+ "left join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id\r\n"
				+ "left join compounds c on gsc.fk_compound_id = c.id\r\n"
				+ "left join compound_relationships cr on c.id = cr.fk_compound_id_predecessor\r\n"
				+ "left join compounds c2 on c2.id=cr.fk_compound_id_successor\r\n"
				+ "where gs.dsstox_substance_id='"+DTXSID+"'\r\n"
				+ "order by cr.created_at desc;";
		
		Connection conn=SqlUtilities.getConnectionDSSTOX();
		
		try {
		
			ResultSet rs=SqlUtilities.runSQL2(conn, sql);
			
			if (rs.next()) {
				
				jo.addProperty("substance_type", rs.getString(2));
				jo.addProperty("relationship", rs.getString(3));
				jo.addProperty("chemical_type", rs.getString(4));
				jo.addProperty("organic_form", rs.getString(5));
				jo.addProperty("smiles_DSSTOX", rs.getString(6));
				jo.addProperty("qsarSmilesOPERA", rs.getString(7));
				
//				System.out.println(jo.get("smiles_DSSTOX").getAsString());
				
			}
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
		
		
	}
	
	int filterOnQsarSmiles(JsonArray ja) {
	
		int countFailsQsarSmiles=0;
		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();

			jo.addProperty("fails_qsar_ready_smiles_SDE", false);
			
			if(jo.get("qsarSmilesSDE")==null || jo.get("qsarSmilesSDE").getAsString().contains(".")) {
				jo.addProperty("fails_qsar_ready_smiles_SDE", true);
				countFailsQsarSmiles++;
			} 
		}
		return countFailsQsarSmiles;
		
	}

	
	int filterOnMaxConc(JsonArray ja,double tolerance) {
		int countFailsMaxConc=0;
		
		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			
			jo.addProperty("fails_max_conc", false);
			
			if(jo.get("Max conc")!=null && jo.get("Median % activity")!=null) {
				double Max_conc=jo.get("Max conc").getAsDouble();
				double median_percent_activity=jo.get("Median % activity").getAsDouble();

				if((Max_conc<100-tolerance && median_percent_activity<100-tolerance) || Max_conc>100+tolerance) {
					jo.addProperty("fails_max_conc", true);
					countFailsMaxConc++;
				} 
			}
			
//			jo.addProperty("fails_flattening", false);
			
		}//end loop over ja
		
		return countFailsMaxConc;
	}
	
	
	void createDescriptorsFile(JsonArray ja,String descriptorSetName,SciDataExpertsDescriptorValuesCalculator calc) {
		
//		String server="https://ccte-cced.epa.gov/";
		
		String descriptorSetSimple=descriptorSetName.replace("-default","").toLowerCase();
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2024 tetko challenge\\modeling\\";
		String filepathOut=folder+"TTR "+descriptorSetSimple+" descriptors.csv";

		File of=new File(filepathOut);
		
		HashSet<String>smilesAlreadyRan=new HashSet<>();
		
		if(of.exists()) {
			smilesAlreadyRan=getSmilesAlreadyRan(filepathOut);
		}
		
		System.out.println("Already ran="+smilesAlreadyRan.size());
		
		
//		if(true) return;
		
		HashSet<String>smilesToRun=new HashSet<>();
		
		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();

			if(jo.get("qsarSmilesSDE")!=null && !jo.get("qsarSmilesSDE").getAsString().contains(".")) {
				String smiles=jo.get("qsarSmilesSDE").getAsString();
				if(!smilesToRun.contains(smiles) && !smilesAlreadyRan.contains(smiles) ) smilesToRun.add(smiles);
			} 
		}
		
		System.out.println("Numbers of smiles to calc descriptors="+smilesToRun.size());
		
		if(smilesToRun.size()==0) {
			return;
		}
		
		String header=calc.getHeader(descriptorSetName);
		
		String [] colNames=header.split("\t");
		
		String header2="";
		for(int i=0;i<colNames.length;i++) {
			String colName="\""+colNames[i]+"\"";
			header2+=colName;
			
			if(i<colNames.length-1) header2+=",";
		}


		try {
			
			FileWriter fw=null;
			
			if(of.exists()) {
				fw=new FileWriter(filepathOut,true);
			} else {
				fw=new FileWriter(filepathOut);
				fw.write("QsarSmiles,"+header2+"\r\n");
			}
			
			int counter=0;
			for(String smiles:smilesToRun) {
				
				counter++;
				System.out.println(counter+"\t"+smiles);
				String tsv=calc.runSingleChemicalGet(descriptorSetName, smiles);

				if(tsv==null) {
//					System.out.println("Cant calculate "+descriptorSetName+" descriptors for "+smiles);
					continue;
				}
				fw.write(smiles+","+tsv.replace("\t", ",")+"\r\n");
				fw.flush();
			}
			fw.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private HashSet<String> getSmilesAlreadyRan(String filepathOut) {
		HashSet<String>smilesAlreadyRan=new HashSet<>();

		
		try {
			BufferedReader br = new BufferedReader(new FileReader(filepathOut));
			
			String header=br.readLine();
			
			while (true) {
				String Line=br.readLine();
				if(Line==null) break;
				String smiles=Line.substring(0,Line.indexOf(","));
				smilesAlreadyRan.add(smiles);
//				System.out.println(smiles);
			}
			
			br.close();
			return smilesAlreadyRan;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
	void calculateDescriptors() {

		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2024 tetko challenge\\";
		String filepath=folder+"ttr-supplemental-tables.json";

		try {
			
			String server="https://hazard-dev.sciencedataexperts.com";
			SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");

			JsonArray ja=Utilities.gson.fromJson(new FileReader(filepath), JsonArray.class);
//			createDescriptorsFile(ja,"WebTEST-default",calc);
			createDescriptorsFile(ja,"PaDEL-default",calc);
//			createDescriptorsFile(ja,"Mordred-default",calc);

		} catch (Exception e) {
			e.printStackTrace();
		}


	}
	void goThroughJson() {
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2024 tetko challenge\\";
		String filepath=folder+"ttr-supplemental-tables.json";
		List<String>validSets=new ArrayList<String>();
		validSets.add("training");

		
		double tolerance=10;
		
		int countFailsTotal=0;
		
		try {
			JsonArray ja=Utilities.gson.fromJson(new FileReader(filepath), JsonArray.class);
			
			int countFailsQsarSmiles=filterOnQsarSmiles(ja);
			int countFailsMaxConc=filterOnMaxConc(ja, tolerance);
			int countFailsFlattening = filterOnFlattening(ja,tolerance,validSets);
			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				
				String DTXSID=jo.get("DTXSID").getAsString();
				
				String smilesDSSTOX=null;
				
				if (jo.get("smiles_DSSTOX")!=null) {
					smilesDSSTOX=jo.get("smiles_DSSTOX").getAsString();
				}
				
				boolean fails_qsar_ready_smiles_SDE=jo.get("fails_qsar_ready_smiles_SDE").getAsBoolean();
				boolean fails_max_conc=jo.get("fails_max_conc").getAsBoolean();
				boolean fails_flattening=jo.get("fails_flattening").getAsBoolean();
				
				if(fails_qsar_ready_smiles_SDE || fails_max_conc || fails_flattening) {
					jo.addProperty("fails_any", true);
					countFailsTotal++;
				} else {
					jo.addProperty("fails_any", false);
				}
				
//				if(DTXSID.equals("DTXSID9021477") || DTXSID.equals("DTXSID6027345") || DTXSID.equals("DTXSID1020194")) {
//					System.out.println(DTXSID+"\t"+fails_qsar_ready_smiles_SDE+"\t"+fails_max_conc+"\t"+fails_flattening);
//				}
				
				Double activity=null;
				if(jo.get("Median % activity")!=null) {
					activity=jo.get("Median % activity").getAsDouble();
				}
				
//				if(fails_qsar_ready_smiles_SDE && smilesDSSTOX!=null && activity!=null && !smilesDSSTOX.contains("*") && !smilesDSSTOX.contains("|")) {
//					System.out.println(DTXSID+"\t"+smilesDSSTOX+"\t"+activity);
//				}
				
				String dataset=jo.get("dataset").getAsString();

				
				if(fails_qsar_ready_smiles_SDE ) {
					System.out.println(DTXSID+"\t"+smilesDSSTOX+"\t"+activity+"\t"+dataset);
					
				}

				
			}
			
			if(true) return;
			
			String[] fields = { "DTXSID","fails_any", "fails_qsar_ready_smiles_SDE","fails_max_conc", "fails_flattening", "Chemical", "CASRN", "SMILES", "Library", "Max conc", "Median % activity",
			"Tested in CR?", "dataset", "substance_type", "relationship", "chemical_type", "organic_form",
			"smiles_DSSTOX", "qsarSmilesOPERA", "qsarSmilesSDE" };
	
			String filepathOut=folder+"modeling\\ttr dataset.xlsx";
			ExcelCreator.createExcel2(ja, filepathOut, fields, null);
			System.out.println("countFailsMaxConc="+countFailsMaxConc);
			System.out.println("countFailsFlattening="+countFailsFlattening);
			System.out.println("countFailsQsarSmiles="+countFailsQsarSmiles);
			System.out.println("countFailsTotal="+countFailsTotal);
//			System.out.println(Utilities.gson.toJson(ja));
			
//			String filepathSplitting=folder+"modeling\\TTR training 5 fold splitting file.tsv";

			
			List<DataPoint>dpsFlat=makeFlatTrainingSet(ja,validSets);
			String filepathSplitting=folder+"modeling\\TTR training 5 fold splitting file.csv";
			createSplittingFileTraining(dpsFlat, filepathSplitting, 5);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
	
	void goThroughJsonWithLeaderboard() {
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2024 tetko challenge\\";
		String filepath=folder+"ttr-supplemental-tables-with-leaderboard_values.json";
				
		double tolerance=10;
		List<String>validSets=new ArrayList<String>();
		validSets.add("training");
		validSets.add("leaderboard");

		int countFailsTotal=0;
		
		try {
			JsonArray ja=Utilities.gson.fromJson(new FileReader(filepath), JsonArray.class);
			
			int countFailsQsarSmiles=filterOnQsarSmiles(ja);
			int countFailsMaxConc=filterOnMaxConc(ja, tolerance);
			int countFailsFlattening = filterOnFlattening(ja,tolerance,validSets);
			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				
				String DTXSID=jo.get("DTXSID").getAsString();
				
				String smilesDSSTOX=null;
				
				if (jo.get("smiles_DSSTOX")!=null) {
					smilesDSSTOX=jo.get("smiles_DSSTOX").getAsString();
				}
				
				boolean fails_qsar_ready_smiles_SDE=jo.get("fails_qsar_ready_smiles_SDE").getAsBoolean();
				boolean fails_max_conc=jo.get("fails_max_conc").getAsBoolean();
				boolean fails_flattening=jo.get("fails_flattening").getAsBoolean();
				
				if(fails_qsar_ready_smiles_SDE || fails_max_conc || fails_flattening) {
					jo.addProperty("fails_any", true);
					countFailsTotal++;
				} else {
					jo.addProperty("fails_any", false);
				}
				
//				if(DTXSID.equals("DTXSID9021477") || DTXSID.equals("DTXSID6027345") || DTXSID.equals("DTXSID1020194")) {
//					System.out.println(DTXSID+"\t"+fails_qsar_ready_smiles_SDE+"\t"+fails_max_conc+"\t"+fails_flattening);
//				}
				
				Double activity=null;
				if(jo.get("Median % activity")!=null) {
					activity=jo.get("Median % activity").getAsDouble();
				}
				
//				if(fails_qsar_ready_smiles_SDE && smilesDSSTOX!=null && activity!=null && !smilesDSSTOX.contains("*") && !smilesDSSTOX.contains("|")) {
//					System.out.println(DTXSID+"\t"+smilesDSSTOX+"\t"+activity);
//				}
				
				String dataset=jo.get("dataset").getAsString();

				
				if(fails_qsar_ready_smiles_SDE ) {
//					System.out.println(DTXSID+"\t"+smilesDSSTOX+"\t"+activity+"\t"+dataset);					
				}

				
			}
			
			String[] fields = { "DTXSID","fails_any", "fails_qsar_ready_smiles_SDE","fails_max_conc", "fails_flattening", "Chemical", "CASRN", "SMILES", "Library", "Max conc", "Median % activity",
			"Tested in CR?", "dataset", "substance_type", "relationship", "chemical_type", "organic_form",
			"smiles_DSSTOX", "qsarSmilesOPERA", "qsarSmilesSDE" };
	
			String filepathOut=folder+"modeling\\ttr dataset.xlsx";
			ExcelCreator.createExcel2(ja, filepathOut, fields, null);
			System.out.println("countFailsMaxConc="+countFailsMaxConc);
			System.out.println("countFailsFlattening="+countFailsFlattening);
			System.out.println("countFailsQsarSmiles="+countFailsQsarSmiles);
			System.out.println("countFailsTotal="+countFailsTotal);
//			System.out.println(Utilities.gson.toJson(ja));
			
//			String filepathSplitting=folder+"modeling\\TTR training 5 fold splitting file.tsv";


			
			List<DataPoint>dpsFlat=makeFlatTrainingSet(ja,validSets);
			System.out.println(dpsFlat.size());
						
			String filepathTraining=folder+"modeling\\TTR training with leaderboard.csv";
			createTraining(dpsFlat, filepathTraining);
			
			String filepathSplitting=folder+"modeling\\TTR training + leaderboard 5 fold splitting file.csv";
			createSplittingFileTraining(dpsFlat, filepathSplitting, 5);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
	
	void createExternalSmilesFileFromJsonFile() {
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2024 tetko challenge\\";
		String filepath=folder+"ttr-supplemental-tables.json";
		
		try {
			JsonArray ja=Utilities.gson.fromJson(new FileReader(filepath), JsonArray.class);
			
			FileWriter fw=new FileWriter(folder+"modeling\\TTR predictions.csv");
			fw.write("DTXSID,QsarSmiles,dataset\r\n");
			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				
				String dataset=jo.get("dataset").getAsString();
				
				if(!dataset.equals("blind test") && !dataset.equals("leaderboard")) continue;
				
				String DTXSID=jo.get("DTXSID").getAsString();
				String SMILES=jo.get("SMILES").getAsString();
				
				if(jo.get("qsarSmilesSDE")==null) {
					System.out.println(DTXSID+"\t"+SMILES+"\t"+dataset+"\tqsarSmiles=null");
					continue;
				} 
				
				String qsarSmilesSDE=jo.get("qsarSmilesSDE").getAsString();
				if(qsarSmilesSDE.contains(".")) {
					System.out.println(DTXSID+"\t"+SMILES+"\t"+dataset+"\tqsarSmiles=mixture");
				}
				
				fw.write(DTXSID+","+qsarSmilesSDE+","+dataset+"\r\n");
//				
//				String DTXSID=jo.get("DTXSID").getAsString();
//				boolean fails_qsar_ready_smiles_SDE=jo.get("fails_qsar_ready_smiles_SDE").getAsBoolean();
//				boolean fails_max_conc=jo.get("fails_max_conc").getAsBoolean();
//				boolean fails_flattening=jo.get("fails_flattening").getAsBoolean();
			}
			
			fw.flush();
			fw.close();

			
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
	
	
	void createBlindSmilesFileFromJsonFile() {
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2024 tetko challenge\\";
		String filepath=folder+"ttr-supplemental-tables-with-leaderboard_values.json";
		
		try {
			JsonArray ja=Utilities.gson.fromJson(new FileReader(filepath), JsonArray.class);
			
			FileWriter fw=new FileWriter(folder+"modeling\\TTR blind.csv");
			fw.write("DTXSID,QsarSmiles,dataset\r\n");
			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				
				String dataset=jo.get("dataset").getAsString();
				
				if(!dataset.equals("blind test")) continue;
				
				String DTXSID=jo.get("DTXSID").getAsString();
				String SMILES=jo.get("SMILES").getAsString();
				
				if(jo.get("qsarSmilesSDE")==null) {
					System.out.println(DTXSID+"\t"+SMILES+"\t"+dataset+"\tqsarSmiles=null");
					continue;
				} 
				
				String qsarSmilesSDE=jo.get("qsarSmilesSDE").getAsString();
				if(qsarSmilesSDE.contains(".")) {
					System.out.println(DTXSID+"\t"+SMILES+"\t"+dataset+"\tqsarSmiles=mixture");
				}
				
				fw.write(DTXSID+","+qsarSmilesSDE+","+dataset+"\r\n");
//				
//				String DTXSID=jo.get("DTXSID").getAsString();
//				boolean fails_qsar_ready_smiles_SDE=jo.get("fails_qsar_ready_smiles_SDE").getAsBoolean();
//				boolean fails_max_conc=jo.get("fails_max_conc").getAsBoolean();
//				boolean fails_flattening=jo.get("fails_flattening").getAsBoolean();
			}
			
			fw.flush();
			fw.close();

			
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
	
	void goThroughJsonWithAQC() {
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2024 tetko challenge\\";
		String filepath=folder+"ttr-supplemental-tables.json";
		String filepathAqc=folder+"TTR-supplemental-tables_with Annotations_AJW_0719_2024.json";
		List<String>validSets=new ArrayList<String>();
		validSets.add("training");

		double tolerance=10;
		
		int countFailsTotal=0;
		
		try {
			JsonArray ja=Utilities.gson.fromJson(new FileReader(filepath), JsonArray.class);
			JsonArray jaAQC=Utilities.gson.fromJson(new FileReader(filepathAqc), JsonArray.class);

			int countFailsAQC=filterOnAQC(ja,jaAQC);
			int countFailsQsarSmiles=filterOnQsarSmiles(ja);
			int countFailsMaxConc=filterOnMaxConc(ja, tolerance);
			int countFailsFlattening = filterOnFlattening(ja,tolerance,validSets);

			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				boolean fails_qsar_ready_smiles_SDE=jo.get("fails_qsar_ready_smiles_SDE").getAsBoolean();
				boolean fails_max_conc=jo.get("fails_max_conc").getAsBoolean();
				boolean fails_flattening=jo.get("fails_flattening").getAsBoolean();
				boolean fails_AQC=jo.get("fails_AQC").getAsBoolean();
				
				if(fails_qsar_ready_smiles_SDE || fails_max_conc || fails_flattening || fails_AQC) {
					jo.addProperty("fails_any", true);
					countFailsTotal++;
				} else {
					jo.addProperty("fails_any", false);
				}
			}
			
			String[] fields = { "DTXSID","fails_any", "fails_AQC","fails_qsar_ready_smiles_SDE","fails_max_conc", "fails_flattening", "Chemical", "CASRN", "SMILES", "Library", "Max conc", "Median % activity",
			"Tested in CR?", "dataset", "substance_type", "relationship", "chemical_type", "organic_form",
			"smiles_DSSTOX", "qsarSmilesOPERA", "qsarSmilesSDE" };
	
//			String filepathOut=folder+"modeling\\ttr dataset with AQC.xlsx";
//			ExcelCreator.createExcel2(ja, filepathOut, fields, null);
						
			System.out.println("countFailsMaxConc="+countFailsMaxConc);
			System.out.println("countFailsFlattening="+countFailsFlattening);
			System.out.println("countFailsQsarSmiles="+countFailsQsarSmiles);
			System.out.println("countFailsAQC="+countFailsAQC);
			System.out.println("countFailsTotal="+countFailsTotal);
			
//			System.out.println(Utilities.gson.toJson(ja));
		
			
			String filepathSplitting=folder+"modeling\\TTR training 5 fold splitting file with AQC 0719_2024.csv";
						
			
			List<DataPoint>dpsFlat=makeFlatTrainingSet(ja,validSets);
			createSplittingFileTraining(dpsFlat, filepathSplitting, 5);
			
			List<DataPoint>dpsFlatFailAQC=getTrainingDataFailsAQC(ja,folder+"modeling\\TTR training fails AQC.csv");
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	private List<DataPoint> makeFlatTrainingSet(JsonArray ja,List<String>validSets) {
		
		Hashtable<String,List<DataPoint>>htDP=new Hashtable<>();
		
		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			
			String DTXSID=jo.get("DTXSID").getAsString();
			String strDataset=jo.get("dataset").getAsString();
			
			if(!validSets.contains(strDataset)) continue;
			
			boolean fails_any=jo.get("fails_any").getAsBoolean();			
			if(fails_any) continue;

			String canonQsarSmiles=jo.get("qsarSmilesSDE").getAsString();
			
			if(jo.get("Median % activity")==null) {
				System.out.println(DTXSID+"\tNo activity\t"+strDataset);
				continue;
			}
			
			
			Double qsarPropertyValue=jo.get("Median % activity").getAsDouble();
			
			DataPoint dp=new DataPoint(canonQsarSmiles, qsarPropertyValue, null, false, "tmarti02");
			
			if(htDP.get(canonQsarSmiles)==null) {
				List<DataPoint>dps=new ArrayList<>();
				dps.add(dp);
				htDP.put(canonQsarSmiles, dps);
			} else {
				List<DataPoint>dps=htDP.get(canonQsarSmiles);
				dps.add(dp);
			}
		}
		
		List<DataPoint>dpsFlat=new ArrayList<>();
		
		for(String canonQsarSmiles:htDP.keySet()) {
			List<DataPoint>dps=htDP.get(canonQsarSmiles);
			
			if(dps.size()==1) {
				dpsFlat.add(dps.get(0));
			} else if (dps.size()==2) {
				double val1=dps.get(0).getQsarPropertyValue();
				double val2=dps.get(1).getQsarPropertyValue();
				double valAvg=(val1+val2)/2.0;
				dps.get(0).setQsarPropertyValue(valAvg);
				dpsFlat.add(dps.get(0));
//				System.out.println(canonQsarSmiles+"\t"+val1+"\t"+val2);	
			} else if (dps.size()==3) {
								
				Collections.sort(dps, new Comparator<DataPoint>() {
					@Override
					public int compare(DataPoint u1, DataPoint u2) {
						return u1.getQsarPropertyValue().compareTo(u2.getQsarPropertyValue());
					}
				});				

//				System.out.println("0\t"+dps.get(0).getQsarPropertyValue());
//				System.out.println("1\t"+dps.get(1).getQsarPropertyValue());
//				System.out.println("2\t"+dps.get(2).getQsarPropertyValue());
				
				dpsFlat.add(dps.get(1));
			
			} else {
				//TODO code to calculate median value
				System.out.println("need to handle dps.size()="+dps.size());
			}
		}
		
		return dpsFlat;
	}
	
	

	private List<DataPoint> getTrainingDataFailsAQC(JsonArray ja,String filepathOut) {
		
		Hashtable<String,List<DataPoint>>htDP=new Hashtable<>();
		
		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			
			String strDataset=jo.get("dataset").getAsString();
			if(!strDataset.equals("training")) continue;
			
			boolean fails_AQC=jo.get("fails_AQC").getAsBoolean();			
			if(!fails_AQC) continue;

			boolean fails_max_conc=jo.get("fails_max_conc").getAsBoolean();
			if(fails_max_conc) continue;

			boolean  fails_qsar_ready_smiles_SDE=jo.get("fails_qsar_ready_smiles_SDE").getAsBoolean();
			if(fails_qsar_ready_smiles_SDE) continue;

			boolean  fails_flattening=jo.get("fails_flattening").getAsBoolean();
			if(fails_flattening) continue;
			
			boolean fails_any=jo.get("fails_any").getAsBoolean();			
			
//			System.out.println(Utilities.gson.toJson(jo));
			
			String canonQsarSmiles=jo.get("qsarSmilesSDE").getAsString();
			Double qsarPropertyValue=jo.get("Median % activity").getAsDouble();
			
			DataPoint dp=new DataPoint(canonQsarSmiles, qsarPropertyValue, null, false, "tmarti02");
			
			if(htDP.get(canonQsarSmiles)==null) {
				List<DataPoint>dps=new ArrayList<>();
				dps.add(dp);
				htDP.put(canonQsarSmiles, dps);
			} else {
				List<DataPoint>dps=htDP.get(canonQsarSmiles);
				dps.add(dp);
			}
		}
		
		List<DataPoint>dpsFlat=new ArrayList<>();
		
		for(String canonQsarSmiles:htDP.keySet()) {
			List<DataPoint>dps=htDP.get(canonQsarSmiles);
			
			if(dps.size()==1) {
				dpsFlat.add(dps.get(0));
			} else if (dps.size()==2) {
				double val1=dps.get(0).getQsarPropertyValue();
				double val2=dps.get(1).getQsarPropertyValue();
				double valAvg=(val1+val2)/2.0;
				dps.get(0).setQsarPropertyValue(valAvg);
				dpsFlat.add(dps.get(0));
//				System.out.println(canonQsarSmiles+"\t"+val1+"\t"+val2);	
			} else {
				//TODO code to calculate median value
				System.out.println("need to handle dps.size()="+dps.size());
			}
		}
		
		System.out.println("Training Datapoints that fail training Aqc="+dpsFlat.size());

		try {
			FileWriter fw=new FileWriter(filepathOut);
			fw.write("QsarSmiles,median_activity_%\r\n");
			
			for (DataPoint dp:dpsFlat) {
				fw.write(dp.getCanonQsarSmiles()+","+dp.getQsarPropertyValue()+"\r\n");		
			}
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return dpsFlat;
	}

	void createSplittingFileTraining(List<DataPoint>dps, String filepathOut,int NumFolds) {
		
		String del="\t";
		if(filepathOut.contains(".csv")) del=",";
		
		
		System.out.println("number of flat dps="+dps.size());

		Collections.shuffle(dps);//randomly shuffle chemicals
		List<List<DataPoint>> v=new ArrayList<>(); // vector of array lists
		for (int i=0;i<NumFolds;i++) {
			v.add(new ArrayList<DataPoint>());
		}

		int NumInFold=(int)Math.floor((double)dps.size()/(double)NumFolds);
					
		System.out.println("Number in each fold="+NumInFold);

		for (int fold=0;fold<NumFolds;fold++) {
			List<DataPoint> dpsFold=v.get(fold);
			for (int j=1;j<=NumInFold;j++) {
				dpsFold.add(dps.remove(0));
			}
		}

		//Add remaining to first fold
		List<DataPoint> dpsFold0=v.get(0);
		
		int countRemaining=dps.size();
		System.out.println("Left over="+dps.size());
		
		for (int j=1;j<=countRemaining;j++) {
			dpsFold0.add(dps.remove(0));
		}
		
		try {
			
			FileWriter fw=new FileWriter(filepathOut);
			
			fw.write("QsarSmiles"+del+"median_activity_%"+del+"Fold\r\n");
			
			for (int fold=0;fold<NumFolds;fold++) {
				List<DataPoint> dpsFold=v.get(fold);
				System.out.println((fold+1)+"\t"+dpsFold.size());
				for (DataPoint dp:dpsFold) {
					fw.write(dp.getCanonQsarSmiles()+del+dp.getQsarPropertyValue()+del+"Fold"+(fold+1)+"\r\n");		
				}
			}
			
			
			
			fw.flush();
			fw.close();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	

	void createTraining(List<DataPoint>dps, String filepathOut) {
		
		String del="\t";
		if(filepathOut.contains(".csv")) del=",";
		
		try {
			
			FileWriter fw=new FileWriter(filepathOut);
			fw.write("QsarSmiles"+del+"median_activity_%\r\n");
			
			for (DataPoint dp:dps) {
				fw.write( dp.getCanonQsarSmiles()+del+dp.getQsarPropertyValue()+"\r\n");		
			}
			
			fw.flush();
			fw.close();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	

	private int filterOnAQC(JsonArray ja, JsonArray jaAQC) {
		int countFailsAQC=0;
		
		Hashtable <String,JsonObject>htAQC=new Hashtable<String,JsonObject>();
		
		String colnameAQC="T0";
		
		for(int i=0;i<jaAQC.size();i++) {
			JsonObject jo=jaAQC.get(i).getAsJsonObject();
			htAQC.put(jo.get("DTXSID").getAsString(),jo);
		}
		
		for(int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			String DTXSID=jo.get("DTXSID").getAsString();
			JsonObject joAQC=htAQC.get(DTXSID);
			
			jo.addProperty("fails_AQC", false);
			
			String call=joAQC.get(colnameAQC).getAsString();
//			System.out.println(DTXSID+"\t"+Call1);
			
			if(!call.equals("A") && !call.equals("B")) {
				countFailsAQC++;
				jo.addProperty("fails_AQC", true);
				System.out.println("fail\t"+call);

			} else {
				System.out.println("pass\t"+call);
			}
		}
		
		
		return countFailsAQC;
	}

	private int filterOnFlattening(JsonArray ja, double tolerance,List<String>validSets) {
		
		Hashtable<String,JsonArray>htQsarSmiles=new Hashtable<String,JsonArray>();
		
		for (int i=0;i<ja.size();i++) {

			JsonObject jo=ja.get(i).getAsJsonObject();
			jo.addProperty("fails_flattening", false);
			String dataset=jo.get("dataset").getAsString();
			
			
//			if(!dataset.equals("training")) continue;
			if(!validSets.contains(dataset)) continue;
			
			if(jo.get("qsarSmilesSDE")==null) continue;
			
			boolean fails_qsar_ready_smiles_SDE=jo.get("fails_qsar_ready_smiles_SDE").getAsBoolean();
			boolean fails_max_conc=jo.get("fails_max_conc").getAsBoolean();
			
			boolean fails_AQC=false;
			if(jo.get("fails_AQC")!=null) {
				fails_AQC=jo.get("fails_AQC").getAsBoolean();
			}
			
			if(fails_max_conc || fails_qsar_ready_smiles_SDE || fails_AQC) {
//				System.out.println(Utilities.gson.toJson(jo));
				continue;
			}
			
			String qsarSmilesSDE=jo.get("qsarSmilesSDE").getAsString();
			if(htQsarSmiles.get(qsarSmilesSDE)==null) {
				JsonArray ja2=new JsonArray();
				ja2.add(jo);
				htQsarSmiles.put(qsarSmilesSDE,ja2);
			} else {
				JsonArray ja2=htQsarSmiles.get(qsarSmilesSDE);
				ja2.add(jo);
			}
		}
		
//		if(dataset.equals("training")) {
////	System.out.println(qsarSmilesSDE);
//	
//}


		int countFailsFlattening=0;
		
		for (String smiles:htQsarSmiles.keySet()) {
			JsonArray ja2=htQsarSmiles.get(smiles);
			
			if(ja2.size()<2) continue;
			
//			System.out.println(ja2.size());
			
			if(ja2.size()==2) {
				
				JsonObject jo0=ja2.get(0).getAsJsonObject();
				JsonObject jo1=ja2.get(1).getAsJsonObject();
				
				double val1=jo0.get("Median % activity").getAsDouble();
				double val2=jo1.get("Median % activity").getAsDouble();

				String smiles1=ja2.get(0).getAsJsonObject().get("smiles_DSSTOX").getAsString();
				String smiles2=ja2.get(1).getAsJsonObject().get("smiles_DSSTOX").getAsString();
				
				if(Math.abs(val1-val2)>tolerance) {
					jo0.addProperty("fails_flattening", true);
					jo1.addProperty("fails_flattening", true);
					countFailsFlattening+=2;
//					System.out.println(Utilities.gson.toJson(jo0));
//					System.out.println(Utilities.gson.toJson(jo1));
				}
				
			} else if(ja2.size()==3) {
				JsonObject jo0=ja2.get(0).getAsJsonObject();
				JsonObject jo1=ja2.get(1).getAsJsonObject();
				JsonObject jo2=ja2.get(2).getAsJsonObject();
				double val1=jo0.get("Median % activity").getAsDouble();
				double val2=jo1.get("Median % activity").getAsDouble();
				double val3=jo2.get("Median % activity").getAsDouble();
//				System.out.println(val1+"\t"+val2+"\t"+val3);
			} else {
				System.out.println("Need code for ja.size="+ja2.size());
			}
		}
		return countFailsFlattening;
	}
	
	void createFilesFailsQsarSmiles() {
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2024 tetko challenge\\";
		String filepath=folder+"fails qsar ready smiles.csv";

//		String server="https://hazard-dev.sciencedataexperts.com";
		String server="https://hcd.rtpnc.epa.gov";
		
		String workflow = "qsar-ready_08232023";
		
		SciDataExpertsStandardizer standardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY,
				workflow, server);
		boolean useFullStandardize=false;
		
//		String descriptorSetName="WebTEST-default";
		String descriptorSetName="Mordred-default";
		SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");

		DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();
		DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);
		if (descriptorSet==null) {
			System.out.println("No such descriptor set: " + descriptorSetName);
		}


		try {
			
			FileWriter fw=new FileWriter(folder+"modeling\\special chemicals.tsv");
			
			InputStream inputStream = new FileInputStream(filepath);
			String csvAsString = new BufferedReader(new InputStreamReader(inputStream)).lines()
					.collect(Collectors.joining("\n"));
			String json = CDL.toJSONArray(csvAsString).toString();
			inputStream.close();
			Gson gson=new Gson();
			JsonArray ja = gson.fromJson(json, JsonArray.class);
			
			
			fw.write("id\tProperty\t"+descriptorSet.getHeadersTsv()+"\r\n");
			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				
				String type=jo.get("Type").getAsString();
				String DTXSID=jo.get("DTXSID").getAsString();
				String finalSmiles=jo.get("Final smiles").getAsString();
				
				Double activity=null;
				
				if(jo.get("Activity")!=null && !jo.get("Activity").getAsString().equals("null"))
					activity =jo.get("Activity").getAsDouble();
				else activity=-9999.0;
				
				if(type.equals("Organic mixture") || type.equals("UVCB")) {
					
					String []smilesArray=finalSmiles.split("\\.");
					
					for(String smiles:smilesArray) {
						
						HttpResponse<String> standardizeResponse = standardizer.callQsarReadyStandardizePost(smiles, useFullStandardize);
						
						if (standardizeResponse.getStatus() == 200) {
							String jsonResponse = SciDataExpertsStandardizer.getResponseBody(standardizeResponse, useFullStandardize);
							String qsarSmilesSDE = SciDataExpertsStandardizer.getQsarReadySmilesFromPostJson(jsonResponse,
									useFullStandardize);
							
							String tsvValues=calc.calculateDescriptors(qsarSmilesSDE,descriptorSet);
							
							fw.write(DTXSID+"_"+qsarSmilesSDE+"\t"+activity+"\t"+tsvValues+"\r\n");
						} 
					}
					
//					System.out.println(Utilities.gson.toJson(jo));
					
				} else if (type.equals("Inorganic")) {
					String tsvValues=calc.calculateDescriptors(finalSmiles,descriptorSet);
					fw.write(DTXSID+"_"+finalSmiles+"\t"+activity+"\t"+tsvValues+"\r\n");
				}
			}
			
			fw.flush();
			fw.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	void createFilesFailsQsarSmiles2() {
		
//		String set="blind";
		String set="training";
		
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2024 tetko challenge\\";
		String filepath=folder+"fails qsar ready smiles.csv";

//		String server="https://hazard-dev.sciencedataexperts.com";
		String server="https://hcd.rtpnc.epa.gov";
		
		String workflow = "qsar-ready_08232023";
		
		SciDataExpertsStandardizer standardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY,
				workflow, server);
		boolean useFullStandardize=false;
		
//		String descriptorSetName="WebTEST-default";
		String descriptorSetName="Mordred-default";
		SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");

		DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();
		DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);
		if (descriptorSet==null) {
			System.out.println("No such descriptor set: " + descriptorSetName);
		}


		try {
			
			FileWriter fw=new FileWriter(folder+"modeling\\special chemicals "+set+".tsv");
			
			InputStream inputStream = new FileInputStream(filepath);
			String csvAsString = new BufferedReader(new InputStreamReader(inputStream)).lines()
					.collect(Collectors.joining("\n"));
			String json = CDL.toJSONArray(csvAsString).toString();
			inputStream.close();
			Gson gson=new Gson();
			JsonArray ja = gson.fromJson(json, JsonArray.class);
			
			
			fw.write("DTXSID\tQsarSmiles\tProperty\t"+descriptorSet.getHeadersTsv()+"\r\n");
			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				
				String type=jo.get("Type").getAsString();
				String DTXSID=jo.get("DTXSID").getAsString();
				String finalSmiles=jo.get("Final smiles").getAsString();
				String dataset=jo.get("Dataset").getAsString();
				
				if(set.equals("training") && !dataset.equals(set)) continue;
				if(!set.equals("training") && dataset.equals("training")) continue;
							
				Double activity=null;
				
				if(jo.get("Activity")!=null && !jo.get("Activity").getAsString().equals("null"))
					activity =jo.get("Activity").getAsDouble();
				else activity=-9999.0;
				
				if(type.equals("Organic mixture") || type.equals("UVCB") || type.equals("Complex")) {
					
					String []smilesArray=finalSmiles.split("\\.");
					
					for(String smiles:smilesArray) {
						
						HttpResponse<String> standardizeResponse = standardizer.callQsarReadyStandardizePost(smiles, useFullStandardize);
						
						if (standardizeResponse.getStatus() == 200) {
							String jsonResponse = SciDataExpertsStandardizer.getResponseBody(standardizeResponse, useFullStandardize);
							String qsarSmilesSDE = SciDataExpertsStandardizer.getQsarReadySmilesFromPostJson(jsonResponse,
									useFullStandardize);
							
							String tsvValues=calc.calculateDescriptors(qsarSmilesSDE,descriptorSet);
							
							fw.write(DTXSID+"\t"+qsarSmilesSDE+"\t"+activity+"\t"+tsvValues+"\r\n");
						} 
					}
					
//					System.out.println(Utilities.gson.toJson(jo));
					
				} else if (type.equals("Inorganic")) {
					String tsvValues=calc.calculateDescriptors(finalSmiles,descriptorSet);
					fw.write(DTXSID+"\t"+finalSmiles+"\t"+activity+"\t"+tsvValues+"\r\n");
				}
			}
			
			fw.flush();
			fw.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TTR_Binding_Challenge t=new TTR_Binding_Challenge();
//		t.createSplits();
//		t.writeOfflineCVFiles();
//		t.writeOfflineCVFilesWithOPERA();
//		t.parseOperaFile();
		
//		t.goThroughExcelFile();//		
//		t.goThroughJson();
		
		t.goThroughJsonWithLeaderboard();
//		t.createBlindSmilesFileFromJsonFile();
		
//		t.createExternalSmilesFileFromJsonFile();
//		t.calculateDescriptors();
		
//		t.goThroughExcelFileAQC();
//		t.goThroughJsonWithAQC();
		
//		t.createFilesFailsQsarSmiles();
//		t.createFilesFailsQsarSmiles2();
		
	}

}
