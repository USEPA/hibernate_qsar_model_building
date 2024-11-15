package gov.epa.run_from_java.scripts.OPERA;


import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.CDL;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;

import com.google.gson.*;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.models.ModelStatisticCalculator;
import gov.epa.run_from_java.scripts.PredictionStatisticsScript;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.DashboardPredictionUtilities;

/**
* @author TMARTI02
*/
public class CompareADs {
	
	DashboardPredictionUtilities dpu=new DashboardPredictionUtilities();

	
	class ModelPrediction2  {
		String id;
		Double exp;
		Double pred;
		Integer split;
		Double AD_Global;
		Double AD_Local;
		Double AD_Confidence;
		
		public ModelPrediction2(String id, Double exp, Double pred, Integer split,Double AD_Global,Double AD_Local,Double AD_Confidence) {
			this.id=id;
			this.exp=exp;
			this.pred=pred;
			this.split=split;
			this.AD_Global=AD_Global;
			this.AD_Local=AD_Local;
			this.AD_Confidence=AD_Confidence;
		}
		
	}
	

	
	
	

	void compareADs(String endpoint) {
	
		
		String abbrev1=null;
		String abbrev2=null;
		
		if(endpoint.equals(DevQsarConstants.VAPOR_PRESSURE)) {
			abbrev1="VP";
			abbrev2="LogVP";
		} else if(endpoint.equals(DevQsarConstants.BOILING_POINT)) {
			abbrev1="BP";
			abbrev2="BP";
			
		}
		
		Hashtable<String,String>htEndpoint2=new Hashtable<>();
		htEndpoint2.put("BCF", "LogBCF");

		
		String folder="data\\opera\\OPERA Sets\\";
		
		File Folder=new File(folder);
		
		File expFile=null;
		File predFile=null;
		
		
		for (File file:Folder.listFiles()) {
			if(file.getName().contains(endpoint) && file.getName().contains("OPERA2.8Pred")) predFile=file;
			if(file.getName().equals(endpoint+" external to opera.tsv")) expFile=file;
		}
		
		System.out.println(predFile.getName());
		System.out.println(expFile.getName());
		
		String endpoint2=htEndpoint2.get(endpoint);
		
		
		Hashtable<String,Double>htExp=getExpHashtable(endpoint, expFile);
		System.out.println(htExp.size());

		List<ModelPrediction2>modelPredictions2=getPredictions(abbrev1,abbrev2,predFile);

		for (ModelPrediction2 mp2:modelPredictions2) {
			mp2.exp=htExp.get(mp2.id);
			
//			if(mp2.AD_Local==0.4) {
//				System.out.println(Utilities.gson.toJson(mp2));
//			}
		}

		
		
		
//		System.out.println(Utilities.gson.toJson(modelPredictions2));

		System.out.println("");
		
		calcStatsBasedOnAD("Global=0, Local<0.4", 0.0, null, 0.4, null,null, modelPredictions2);	
		calcStatsBasedOnAD("Global=1, Local<0.4, Confidence<0.5",1.0, null, 0.4, null,0.5, modelPredictions2);	
		calcStatsBasedOnAD("Global=1, Local<0.4, Confidence>=0.5",1.0, null, 0.4, 0.5,null, modelPredictions2);	
		calcStatsBasedOnAD("Global=0, 0.4 <=Local<=0.6",0.0, 0.4, 0.6, null,null, modelPredictions2);	
		calcStatsBasedOnAD("Global=1, 0.4 <=Local<=0.6",1.0, 0.4, 0.6, null,null, modelPredictions2);	
		calcStatsBasedOnAD("Global=0, Local>0.6",0.0, 0.6, null, null,null, modelPredictions2);	
		calcStatsBasedOnAD("Global=1, Local>0.6",1.0, 0.6, null, null,null, modelPredictions2);	

		System.out.println("");
		
		calcStatsBasedOnDecisionTreeAD("Outside DT", false, modelPredictions2);	
		calcStatsBasedOnDecisionTreeAD("Inside DT", true, modelPredictions2);
			
		System.out.println("");
		
		calcStatsBasedOnAD("Global=0",0.0, null, null, null,null, modelPredictions2);	
		calcStatsBasedOnAD("Global=1",1.0, null, null, null,null, modelPredictions2);	
		
		System.out.println("");
		
		List<Double> cutoffs= Arrays.asList(0.5,0.45, 0.4,0.35, 0.3,0.25, 0.2,0.15,0.10);
		
		for(Double cutoff:cutoffs) {
			calcStatsBasedOnAD("Local<"+cutoff,cutoff,true, modelPredictions2);	
			calcStatsBasedOnAD("Local>="+cutoff,cutoff,false, modelPredictions2);
			System.out.print("\n");
				
			
		}
		

	}
	

	void compareADs2(String endpoint) {
		
		String folder="data\\opera\\OPERA Sets\\";
		File predFile=new File(folder+endpoint.replace(":","_")+" external to opera.tsv");
		if(!predFile.exists()) {
			System.out.println(predFile.getAbsolutePath()+" doesnt exist");
			return;
		}
		
		List<ModelPrediction2>modelPredictions2=getPredictions2(predFile);
//		System.out.println(Utilities.gson.toJson(modelPredictions2));
//		if(true) return;
		

		System.out.println(endpoint+"\n\nAD	RMSE	fraction of compounds");
		
		//Version 1.0
//		calcStatsBasedOnAD("Global=0, Local<0.4", 0.0, null, 0.4, null,null, modelPredictions2);	
//		calcStatsBasedOnAD("Global=1, Local<0.4, Confidence<0.5",1.0, null, 0.4, null,0.5, modelPredictions2);	
//		calcStatsBasedOnAD("Global=1, Local<0.4, Confidence>=0.5",1.0, null, 0.4, 0.5,null, modelPredictions2);	
//		calcStatsBasedOnAD("Global=0, 0.4 <=Local<=0.6",0.0, 0.4, 0.6, null,null, modelPredictions2);	
//		calcStatsBasedOnAD("Global=1, 0.4 <=Local<=0.6",1.0, 0.4, 0.6, null,null, modelPredictions2);	
//		calcStatsBasedOnAD("Global=0, Local>0.6",0.0, 0.6, null, null,null, modelPredictions2);	
//		calcStatsBasedOnAD("Global=1, Local>0.6",1.0, 0.6, null, null,null, modelPredictions2);	

		//Version 2.0
		calcStatsBasedOnAD("Local<0.4", 0.0, null, 0.4, null,null, modelPredictions2);	
		calcStatsBasedOnAD("Global=0, 0.4 <=Local<=0.6",0.0, 0.4, 0.6, null,null, modelPredictions2);	
		calcStatsBasedOnAD("Global=1, 0.4 <=Local<=0.6",1.0, 0.4, 0.6, null,null, modelPredictions2);	
		calcStatsBasedOnAD("Global=0, Local>0.6",0.0, 0.6, null, null,null, modelPredictions2);	
		calcStatsBasedOnAD("Global=1, Local>0.6",1.0, 0.6, null, null,null, modelPredictions2);	

		
		System.out.println("");
		
//		calcStatsBasedOnAD("Global=1, Local<0.4, Confidence<0.75",1.0, null, 0.4, null,0.75, modelPredictions2);	
//		calcStatsBasedOnAD("Global=1, Local<0.4, Confidence>=0.75",1.0, null, 0.4, 0.75, null, modelPredictions2);	
//		System.out.println("");
		
//		calcStatsBasedOnDecisionTreeAD("Outside DT", false, modelPredictions2);	
//		calcStatsBasedOnDecisionTreeAD("Inside DT", true, modelPredictions2);
			
		System.out.println("");
		
		calcStatsBasedOnAD("Global=0",0.0, null, null, null,null, modelPredictions2);	
		calcStatsBasedOnAD("Global=1",1.0, null, null, null,null, modelPredictions2);	
		
		System.out.println("");
		
		List<Double> cutoffs= Arrays.asList(0.5,0.45, 0.4,0.35, 0.3,0.25, 0.2,0.15,0.10);
		
		for(Double cutoff:cutoffs) {
			calcStatsBasedOnAD("Local<"+cutoff,cutoff,true, modelPredictions2);	
			calcStatsBasedOnAD("Local>="+cutoff,cutoff,false, modelPredictions2);
			System.out.print("\n");
		}
		

		
	}
	

	void compareErrorToAD(String endpoint) {
		
		String folder="data\\opera\\OPERA Sets\\";
		File predFile=new File(folder+endpoint.replace(":","_")+" external to opera.tsv");
		if(!predFile.exists()) {
			System.out.println(predFile.getAbsolutePath()+" doesnt exist");
			return;
		}
		
		List<ModelPrediction2>modelPredictions2=getPredictions2(predFile);

		for (ModelPrediction2 mp2:modelPredictions2) {
			double absErr=Math.abs(mp2.exp-mp2.pred);
			System.out.println(mp2.AD_Confidence+"\t"+absErr);
		}

	}
	
	List<ModelPrediction>calcStatsBasedOnAD(String type, Double global,Double minLocal,Double maxLocal,Double minConfidence,Double maxConfidence,List<ModelPrediction2>preds) {
		
		List<ModelPrediction>mps=new ArrayList<ModelPrediction>();
		
		for (ModelPrediction2 mp2:preds) {

			Double pred=mp2.pred;
			
			if (global!=null) {
				double diff=Math.abs(global-mp2.AD_Global);
//				System.out.println(diff);
				if(diff>0.00001) {
//					System.out.println("here\t"+global+"\t"+mp2.AD_Global);
					continue;
//					System.out.println(mp2.pred+"\t"+pred);
				} 
			}
			
//			System.out.println("here\t"+global+"\t"+mp2.AD_Global);
			
			if (minLocal!=null && maxLocal!=null) {
				if(mp2.AD_Local<minLocal) continue;
				if(mp2.AD_Local>maxLocal) continue;
			} else {
				
				if(minLocal!=null) {
					if(mp2.AD_Local<=minLocal) continue;
				}
				
				if(maxLocal!=null) {
					if(mp2.AD_Local>=maxLocal) continue;
				}
			}
			
			if(minConfidence!=null) {
				if(mp2.AD_Confidence<minConfidence) continue;
			}
			
			if(maxConfidence!=null) {
				if(mp2.AD_Confidence>=maxConfidence) continue;
			}

//			if(type.equals("Global=1, Local<0.4, Confidence<0.5")) {
//				System.out.println(type+"\n"+Utilities.gson.toJson(mp2));
//			}
			
//			System.out.println("here1\t"+mp2.exp+"\t"+mp2.pred);
			ModelPrediction mp=new ModelPrediction(mp2.id,mp2.exp,pred,mp2.split);
			mps.add(mp);
			
		}
		
		double frac=mps.size()/(double)preds.size();
		
		double meanExpTraining = PredictionStatisticsScript.calculateMeanExpTraining(mps);
		Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator
				.calculateContinuousStatistics(mps, meanExpTraining, DevQsarConstants.TAG_TEST);
		
		DecimalFormat df=new DecimalFormat ("0.000");
		double RMSE=modelTestStatisticValues.get(DevQsarConstants.RMSE + DevQsarConstants.TAG_TEST);
		
		System.out.println(type+"\t"+df.format(RMSE)+"\t"+df.format(frac));
		
		return mps;
	}
	
	

	List<ModelPrediction>calcStatsBasedOnDecisionTreeAD(String type, boolean isInside,List<ModelPrediction2>preds) {
		
		List<ModelPrediction>mps=new ArrayList<ModelPrediction>();
		
		for (ModelPrediction2 mp2:preds) {

			Double pred=mp2.pred;
			
			if (isInside) {
				
				if((mp2.AD_Global==0 && mp2.AD_Local<0.4) || (mp2.AD_Global==1 && mp2.AD_Local<0.4 && mp2.AD_Confidence<0.5) ) {
					continue;
				}
			} else {
				if(!(mp2.AD_Global==0 && mp2.AD_Local<0.4) && !(mp2.AD_Global==1 && mp2.AD_Local<0.4 && mp2.AD_Confidence<0.5) ) {
					continue;
				}
			}
			
			
//			if(type.equals("Global=1, Local<0.4, Confidence<0.5")) {
//				System.out.println(type+"\n"+Utilities.gson.toJson(mp2));
//			}
			
//			System.out.println("here1\t"+mp2.exp+"\t"+mp2.pred);
			ModelPrediction mp=new ModelPrediction(mp2.id,mp2.exp,pred,mp2.split);
			mps.add(mp);
			
		}
		
		double frac=mps.size()/(double)preds.size();
		
		
		double meanExpTraining = PredictionStatisticsScript.calculateMeanExpTraining(mps);
		Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator
				.calculateContinuousStatistics(mps, meanExpTraining, DevQsarConstants.TAG_TEST);
		
		
		DecimalFormat df=new DecimalFormat ("0.000");
		double RMSE=modelTestStatisticValues.get(DevQsarConstants.RMSE + DevQsarConstants.TAG_TEST);
		
		System.out.println(type+"\t"+df.format(RMSE)+"\t"+df.format(frac));
		
		return mps;
	}
	

	
	

	List<ModelPrediction>calcStatsBasedOnAD(String type, double cutoff,boolean isMin,List<ModelPrediction2>preds) {
		
		List<ModelPrediction>mps=new ArrayList<ModelPrediction>();
		
		for (ModelPrediction2 mp2:preds) {

			Double pred=mp2.pred;
			
			
			if (isMin) {
				if(mp2.AD_Local>=cutoff) {
					continue;
				}
			} else {
				if(mp2.AD_Local<cutoff) {
					continue;
				}
			}
			
//			System.out.println("here1\t"+mp2.exp+"\t"+mp2.pred);
			ModelPrediction mp=new ModelPrediction(mp2.id,mp2.exp,pred,mp2.split);
			mps.add(mp);
			
		}
		
		double frac=mps.size()/(double)preds.size();
		
		
		double meanExpTraining = PredictionStatisticsScript.calculateMeanExpTraining(mps);
		Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator
				.calculateContinuousStatistics(mps, meanExpTraining, DevQsarConstants.TAG_TEST);
		
		
		DecimalFormat df=new DecimalFormat ("0.000");
		double RMSE=modelTestStatisticValues.get(DevQsarConstants.RMSE + DevQsarConstants.TAG_TEST);
		
		System.out.println(type+"\t"+df.format(RMSE)+"\t"+df.format(frac));
		
		return mps;
	}
	

	List<ModelPrediction>calcStatsBasedOnLocalAD(String type, Double cutoff,List<ModelPrediction2>preds) {
		
		List<ModelPrediction>mps=new ArrayList<ModelPrediction>();
		
		for (ModelPrediction2 mp2:preds) {
			Double pred=mp2.pred;
			if(mp2.AD_Local<cutoff) continue;
//			System.out.println("here1\t"+mp2.exp+"\t"+mp2.pred);
			ModelPrediction mp=new ModelPrediction(mp2.id,mp2.exp,pred,mp2.split);
			mps.add(mp);
			
		}
		
		double frac=mps.size()/(double)preds.size();
		
		
		double meanExpTraining = PredictionStatisticsScript.calculateMeanExpTraining(mps);
		Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator
				.calculateContinuousStatistics(mps, meanExpTraining, DevQsarConstants.TAG_TEST);
		
		
		DecimalFormat df=new DecimalFormat ("0.000");
		double RMSE=modelTestStatisticValues.get(DevQsarConstants.RMSE + DevQsarConstants.TAG_TEST);
		
		System.out.println(type+"\t"+df.format(RMSE)+"\t"+df.format(frac));
		
		return mps;
	}
	

	Hashtable<String,Double> getExpHashtable(String endpoint, File expFile) {
	
		try {
		
		Hashtable<String,Double>htExp=new Hashtable<String,Double>();
		InputStream inputStream;
		inputStream = new FileInputStream(expFile);
		
		BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
		String csvAsString = br.lines().collect(Collectors.joining("\n"));
		csvAsString=csvAsString.replace("\t", ",");
		
//		System.out.println(csvAsString);
		
		br.close();
		
		String json = CDL.toJSONArray(csvAsString).toString();
//		System.out.println("Done loading results file");
		
		JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);
		
		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			
			double exp=jo.get("propertyValue").getAsDouble();
			String id=jo.get("dataPointId").getAsString();
			
			htExp.put(id,exp);
			
//			System.out.println(id+"\t"+exp);
			
		}		
		return htExp;
		
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	Hashtable<String,String> getChemID_DTXSIDHashtableFromOPERA_SDF(String endpoint, File sdfFile, DashboardPredictionUtilities dpu,String endpoint2) {
		Hashtable<String,String> ht=new Hashtable<String,String>();
		AtomContainerSet acs=dpu.readSDFV2000(sdfFile.getAbsolutePath());
		
		for (int i=0;i<acs.getAtomContainerCount();i++) {
			AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
			
			String ChemID=ac.getProperty("ChemID").toString();
			String DTXSID=ac.getProperty("dsstox_substance_id");
			
			if(DTXSID.indexOf("|")>-1) {
				DTXSID=DTXSID.substring(0,DTXSID.indexOf("|"));
			}
			
//			System.out.println(ChemID+"\t"+exp);
			ht.put(ChemID, DTXSID);
		}
		return ht;
	}


	private List<ModelPrediction2> getPredictions(String abbrev1,String abbrev2, File predFile) {
		List<ModelPrediction2> testSetPredictions = new ArrayList<ModelPrediction2>();
		
		InputStream inputStream;
		
		try {
			inputStream = new FileInputStream(predFile);
			String csvAsString = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
			String json = CDL.toJSONArray(csvAsString).toString();
//			System.out.println("Done loading results file");
			
			JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);
			
			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				
				double pred=jo.get(abbrev2+"_pred").getAsDouble();
				String MoleculeID=jo.get("MoleculeID").getAsString();
				
//				AD_BCF,AD_index_BCF,Conf_index_BCF
				
				Double AD_Global=jo.get("AD_"+abbrev1).getAsDouble();
				Double AD_Local=jo.get("AD_index_"+abbrev1).getAsDouble();
				Double AD_Confidence=jo.get("Conf_index_"+abbrev1).getAsDouble();
				
				ModelPrediction2 mp=new ModelPrediction2(MoleculeID,null, pred,1,AD_Global,AD_Local,AD_Confidence);
				testSetPredictions.add(mp);
				
			}
			
//			System.out.println(Utilities.gson.toJson(testSetPredictions));
			return testSetPredictions;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	private List<ModelPrediction2> getPredictions2(File predFile) {
		List<ModelPrediction2> testSetPredictions = new ArrayList<ModelPrediction2>();
		
		InputStream inputStream;
		
		try {
			inputStream = new FileInputStream(predFile);
			
			BufferedReader br= new BufferedReader(new InputStreamReader(inputStream));
			
			String csvAsString =br.lines().collect(Collectors.joining("\n")).replace("\t",",");
			br.close();
			
			String json = CDL.toJSONArray(csvAsString).toString();
//			System.out.println("Done loading results file");
			
			JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);
			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();

				
//				System.out.println(jo.get("dataPointId"));
				
				String ID=jo.get("dataPointId").getAsInt()+"";
				double exp=jo.get("exp").getAsDouble();
				
				if(jo.get("pred").getAsString().isEmpty()) continue;
				
				double pred=jo.get("pred").getAsDouble();
				
				
				
				Double AD_Global=jo.get("AD_global").getAsDouble();
				Double AD_Local=jo.get("AD_Local").getAsDouble();
				Double AD_Confidence=jo.get("AD_Confidence").getAsDouble();
				
				ModelPrediction2 mp=new ModelPrediction2(ID,exp, pred,1,AD_Global,AD_Local,AD_Confidence);
				testSetPredictions.add(mp);
				
			}
			
//			System.out.println(Utilities.gson.toJson(testSetPredictions));
			return testSetPredictions;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}

	
	void getRecordsNotInOpera() {
		
		String[] properties = { DevQsarConstants.HENRYS_LAW_CONSTANT, DevQsarConstants.BOILING_POINT,
				DevQsarConstants.MELTING_POINT, DevQsarConstants.LOG_KOW, DevQsarConstants.VAPOR_PRESSURE,
				DevQsarConstants.WATER_SOLUBILITY };
		
//		String property=DevQsarConstants.LOG_KOW;
//		writeExternalOperaFiles(property);
		
		FileWriter fw;
		try {
			fw = new FileWriter("data\\opera\\OPERA Sets\\external to opera.tsv");
			fw.write("dataPointId\tcanonSmiles\tpropertyValue\tunits\tdtxcid\tproperty\r\n");

			for (String property:properties) {
				List<String>lines=writeExternalOperaFiles(property);
				for (String line:lines) {
					fw.write(line+"\r\n");
				}
			}
			fw.flush();
			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	private List<String> writeExternalOperaFiles(String property) {
		
		List<String>lines=new ArrayList<String>();

		
		String sql="select fk_datasets_id from qsar_datasets.datasets_in_cheminformatics_modules dicm\r\n"
				+ "join qsar_datasets.properties p on p.id=dicm.fk_property_id\r\n"
				+ "where name='"+property.replace("'", "''")+"';";
		
		String datasetId=SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql);
		
		String sqlSmilesInOperaData="select distinct canon_qsar_smiles from qsar_datasets.data_points dp\r\n"
				+ "join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id=dp.id\r\n"
				+ "join exp_prop.property_values pv on dpc.exp_prop_property_values_id=pv.id\r\n"
				+ "join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id\r\n"
				+ "where dp.fk_dataset_id="+datasetId+" and ps.name='OPERA2.8';";

		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sqlSmilesInOperaData);
		
		List<String>operaSmilesList=new ArrayList<String>();
		try {
			while (rs.next()) {
				String canonSmiles=rs.getString(1);
				operaSmilesList.add(canonSmiles);
//				System.out.println(canonSmiles);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
//		System.out.println(datasetId);
		
		String sqlDatapoints="select dp.id,canon_qsar_smiles, dp.qsar_property_value, u.abbreviation_ccd, qsar_dtxcid from qsar_datasets.data_points dp\r\n"
				+ "join qsar_datasets.datasets d on d.id=dp.fk_dataset_id\r\n"
				+ "join qsar_datasets.units u on d.fk_unit_id = u.id\r\n"
				+ "where d.id="+datasetId+";";


		rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sqlDatapoints);
		
		
		try {
			
			String property2=property.replace(":", "_");
			
			FileWriter fw=new FileWriter("data\\opera\\OPERA Sets\\"+property2+" external to opera.tsv");
			FileWriter fw2=new FileWriter("data\\opera\\OPERA Sets\\"+property2+" external to opera.smi");

			
			
			fw.write("dataPointId\tcanonSmiles\tpropertyValue\tunits\tdtxcid\tproperty\r\n");
			while (rs.next()) {
				Long dataPointId=rs.getLong(1);
				String canonSmiles=rs.getString(2);
				Double propertyValue=rs.getDouble(3);
				String units=rs.getString(4);
				String dtxcid=rs.getString(5);
				
				if(dtxcid.contains("|")) {
					dtxcid=dtxcid.substring(0,dtxcid.indexOf("|"));
				}
				
				
				if(!operaSmilesList.contains(canonSmiles)) {
					String lineTSV=dataPointId+"\t"+canonSmiles+"\t"+propertyValue+"\t"+units+"\t"+dtxcid+"\t"+property;
					fw.write(lineTSV+"\r\n");
					fw2.write(canonSmiles+"\t"+dataPointId+"\r\n");
					lines.add(lineTSV);
				}
				
//				System.out.println(canonSmiles);
			}

			fw2.flush();
			fw2.close();

			
			fw.flush();
			fw.close();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lines;
	}
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		CompareADs c=new CompareADs();

		//	c.getRecordsNotInOpera();

//		c.compareADs2(DevQsarConstants.MELTING_POINT);
//		c.compareADs2(DevQsarConstants.BOILING_POINT);
//		c.compareADs2(DevQsarConstants.VAPOR_PRESSURE);
//		c.compareADs2(DevQsarConstants.WATER_SOLUBILITY);
//		c.compareADs2(DevQsarConstants.HENRYS_LAW_CONSTANT);
//		c.compareADs2(DevQsarConstants.LOG_KOW);
		
		c.compareErrorToAD(DevQsarConstants.BOILING_POINT);

	}

}
