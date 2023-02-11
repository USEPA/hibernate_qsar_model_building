package gov.epa.run_from_java.scripts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.JsonArray;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSet;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetServiceImpl;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.models.ModelStatisticCalculator;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.endpoints.reports.predictions.ExcelReports.ExcelPredictionReportGenerator;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportDataPoint;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportModelMetadata;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportModelStatistic;
import gov.epa.endpoints.reports.predictions.QsarPredictedValue;
import gov.epa.run_from_java.scripts.ApplicabilityDomainScript.ApplicabilityDomainPrediction;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;
import gov.epa.run_from_java.scripts.GetExpPropInfo.ExcelCreator;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.RecalcStatsScript.SplitPredictions;

public class PredictionStatisticsScript {

	
	String lanId="tmarti02";
	static Connection conn=SqlUtilities.getConnectionPostgres();
	RecalcStatsScript recalcStatsScript=new RecalcStatsScript();
	
	ModelServiceImpl modelService=new ModelServiceImpl();
	
	/**
	 * Get the modelID for model for given dataset, method, and modelSet
	 * 
	 * @param modelId
	 * @param datasetName
	 * @param methodName
	 * @return
	 */
	Long getModelId(String modelSetName,String datasetName,String methodName) {
		
		String sql="select mims.fk_model_id from qsar_models.models_in_model_sets mims\n"+ 
		"join qsar_models.models m on m.id=mims.fk_model_id\n"+ 
		"join qsar_models.methods m2 on m2.id=m.fk_method_id\n"+
		"join qsar_models.model_sets ms on ms.id=mims.fk_model_set_id\n"+ 
		"where ms.\"name\"='"+modelSetName+"' and \n"+
		"m.dataset_name ='"+datasetName+"' and \n"+
		"m2.\"name\" like '"+methodName+"%';";
		
//		System.out.println(sql+"\n");
		String strId=SqlUtilities.runSQL(conn, sql);
		if(strId==null) return null;
		else return (Long.parseLong(strId));
	}
	
	
	/**
	 * Get prediction statistic for model
	 * 
	 * @param modelId
	 * @param datasetName
	 * @param methodName
	 * @return
	 */
	Double getStat(long modelId,String statisticName) {
				
		String sql="select ms.statistic_value  from qsar_models.model_statistics ms\n"+
		"join qsar_models.\"statistics\" s2  on s2.id=ms.fk_statistic_id\n"+
		"where ms.fk_model_id="+modelId+" and s2.\"name\" ='"+statisticName+"';";
		
		
//		System.out.println(sql+"\n");
//		System.out.println(modelId);
		String result=DatabaseLookup.runSQL(conn, sql);
		if(result==null) return null;
		else return (Double.parseDouble(result));
	}
	
	/**
	 * Prints summary of stats to the screen for a given method
	 * 
	 * TODO make it write to file
	 * 
	 * 
	 * @param statisticName
	 * @param methodName
	 * @param modelSetNames
	 * @param datasetNames
	 */
	private void createSummaryTableForMethod(String statName, String methodName, List<String> modelSetNames,
			List<String> datasetNames, Hashtable<String,Double>htVals) {
		
		
		StringBuffer sb=new StringBuffer();
		
		DecimalFormat df=new DecimalFormat("0.000");
		
		sb.append("\n"+statName+" results for method = "+methodName+"\n");
		sb.append("DatasetName\t");
		
		for (int j=0;j<modelSetNames.size();j++) {
			String modelSetName=modelSetNames.get(j);
		
			sb.append(modelSetName);			
			if (j<modelSetNames.size()-1) sb.append("\t");
			else sb.append("\n");
		}
		
		for (int i=0;i<datasetNames.size();i++) {
			String datasetName=datasetNames.get(i);
			sb.append(datasetName+"\t");
								
			for (int j=0;j<modelSetNames.size();j++) {
				String modelSetName=modelSetNames.get(j);
				
				String key=methodName+"\t"+modelSetName+"\t"+datasetName;
				
//				System.out.println(key);
				
				Double modelSetStat=htVals.get(key);
				
				if (modelSetStat==null) sb.append("NaN");				
				else sb.append(df.format(modelSetStat));
				
				if (j<modelSetNames.size()-1) sb.append("\t");
				else sb.append("\n");
			}
			
		}
		System.out.println(sb.toString());
		
		try {
			FileWriter fw=new FileWriter("data/reports/stats/"+methodName+"_"+statName+".txt");
			fw.write(sb.toString());
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	
	/**
	 * Prints summary of stats for model set
	 * 
	 * TODO make it write to file
	 * 
	 * 
	 * @param statisticName
	 * @param methodName
	 * @param modelSetNames
	 * @param datasetNames
	 */
	private void createSummaryTableForModelSet(String statName, String modelSetName,List<String> methodNames, 
			List<String> datasetNames, Hashtable<String,Double>htVals) {
		
		StringBuffer sb=new StringBuffer();
		DecimalFormat df=new DecimalFormat("0.00");
		
		sb.append("\n"+statName+" results for model set = "+modelSetName+"\n");
		sb.append("DatasetName\t");
		
		for (int j=0;j<methodNames.size();j++) {
			String methodName=methodNames.get(j);
			sb.append(methodName);			
			if (j<methodNames.size()-1) sb.append("\t");
			else sb.append("\r\n");
		}
		
		for (int i=0;i<datasetNames.size();i++) {
			String datasetName=datasetNames.get(i);
			
//			String datasetName2=datasetName.replace(" from exp_prop and chemprop", "");						
			sb.append(datasetName+"\t");
						
			for (int j=0;j<methodNames.size();j++) {
				String methodName=methodNames.get(j);

				String key=methodName+"\t"+modelSetName+"\t"+datasetName;
				
//				System.out.println(key);
				
				Double modelSetStat=htVals.get(key);
								
				if (modelSetName==null) sb.append("N/A");				
				else sb.append(df.format(modelSetStat));

				if (j<methodNames.size()-1) sb.append("\t");
				else sb.append("\r\n");
			}
		}
		
		System.out.println(sb.toString());
		
		try {
			FileWriter fw=new FileWriter("data/reports/stats/"+statName+"_"+modelSetName+".txt");
			fw.write(sb.toString());
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	
	void createSummaryTableForMethod() {
		
		String methodName=DevQsarConstants.CONSENSUS;
	
//		String statisticName = "MAE_Test";
		String statisticName="PearsonRSQ_Test";
//		String statisticName="Q2_CV_Training";
		
		// Getting predictions for PFAS compounds in test set:		
		String listName="PFASSTRUCTV4";		
		String folder="data/dev_qsar/dataset_files/";
		String filePath=folder+listName+"_qsar_ready_smiles.txt";
		ArrayList<String>smilesArrayPFAS=SplittingGeneratorPFAS_Script.getPFASSmiles(filePath);
		
		List<String> modelSetNames=new ArrayList<>();
		
		modelSetNames.add("WebTEST2.0 PFAS");
		modelSetNames.add("WebTEST2.0 All but PFAS");
		modelSetNames.add("WebTEST2.0_justPFAS");
		modelSetNames.add("WebTEST2.0");
		
		modelSetNames.add("WebTEST2.1 PFAS");
		modelSetNames.add("WebTEST2.1 All but PFAS");
		modelSetNames.add("WebTEST2.1_justPFAS");
		modelSetNames.add("WebTEST2.1");

		List<String>datasetNames=new ArrayList<>();
		datasetNames.add("HLC from exp_prop and chemprop");
		datasetNames.add("WS from exp_prop and chemprop");
		datasetNames.add("VP from exp_prop and chemprop");
		datasetNames.add("LogP from exp_prop and chemprop");
		datasetNames.add("MP from exp_prop and chemprop");
		datasetNames.add("BP from exp_prop and chemprop");

		datasetNames.add("pKa_a from exp_prop and chemprop");
		datasetNames.add("pKa_b from exp_prop and chemprop");

		Hashtable<String,Double>htVals=new Hashtable<>();
		addHashtableEntry(statisticName, methodName, modelSetNames, datasetNames,htVals);
				
		for (String datasetName:datasetNames) {
			addHashtableEntryLimitToPFAS(statisticName, methodName, datasetName,htVals,smilesArrayPFAS);
		}

		createSummaryTableForMethod(statisticName, DevQsarConstants.CONSENSUS, modelSetNames, datasetNames, htVals);
		
	}
	
	void createSummaryTableForSet() {
//		String statisticName = "MAE_Test";
		String statisticName="PearsonRSQ_Test";
//		String statisticName="Q2_CV_Training";
		
		// Getting predictions for PFAS compounds in test set:		
		String listName="PFASSTRUCTV4";		
		String folder="data/dev_qsar/dataset_files/";
		String filePath=folder+listName+"_qsar_ready_smiles.txt";
		ArrayList<String>smilesArrayPFAS=SplittingGeneratorPFAS_Script.getPFASSmiles(filePath);

//		String modelSetName="WebTEST2.0 PFAS";
//		String modelSetName="WebTEST2.0 All but PFAS";
//		String modelSetName="WebTEST2.0_justPFAS";
//		String modelSetName="WebTEST2.0";
		
//		String modelSetName="WebTEST2.1 PFAS";
//		String modelSetName="WebTEST2.1 All but PFAS";
//		String modelSetName="WebTEST2.1_justPFAS";
		String modelSetName="WebTEST2.1";
		
		List<String> modelSetNames=new ArrayList<>();
		modelSetNames.add(modelSetName);

		List<String>datasetNames=new ArrayList<>();
		datasetNames.add("HLC from exp_prop and chemprop");
		datasetNames.add("WS from exp_prop and chemprop");
		datasetNames.add("VP from exp_prop and chemprop");
		datasetNames.add("LogP from exp_prop and chemprop");
		datasetNames.add("MP from exp_prop and chemprop");
		datasetNames.add("BP from exp_prop and chemprop");
		datasetNames.add("pKa_a from exp_prop and chemprop");
		datasetNames.add("pKa_b from exp_prop and chemprop");


		List<String> methodNames=new ArrayList<>();
		methodNames.add(DevQsarConstants.KNN);
		methodNames.add(DevQsarConstants.RF);
		methodNames.add(DevQsarConstants.XGB);
		methodNames.add(DevQsarConstants.SVM);
		methodNames.add(DevQsarConstants.CONSENSUS);

		Hashtable<String,Double>htVals=new Hashtable<>();
		for (String methodName:methodNames) {
			addHashtableEntry(statisticName, methodName, modelSetNames, datasetNames,htVals);
		}
		
		if (modelSetName.contains("_justPFAS")) {
			for (String methodName:methodNames) {
				System.out.println("\n"+methodName);
				for (String datasetName:datasetNames) {
					addHashtableEntryLimitToPFAS(statisticName, methodName, datasetName,htVals,smilesArrayPFAS);
				}
			}
		}
		
		//Create the summary table as text file:
		createSummaryTableForModelSet(statisticName, modelSetName, methodNames, datasetNames, htVals);

	}
	
	void createSummaryTableForMethod_Rnd_Representative() {
//		String statisticName = "MAE_Test";
		String statisticName="PearsonRSQ_Test";
		
		List<String> modelSetNames=new ArrayList<>();
				
		modelSetNames.add("WebTEST2.0");//TODO calc stats just for PFAS
		modelSetNames.add("WebTEST2.1");
		
		List<String>datasetNames=new ArrayList<>();
		datasetNames.add("HLC from exp_prop and chemprop");
		datasetNames.add("WS from exp_prop and chemprop");
		datasetNames.add("VP from exp_prop and chemprop");
		datasetNames.add("LogP from exp_prop and chemprop");
		datasetNames.add("MP from exp_prop and chemprop");
		datasetNames.add("BP from exp_prop and chemprop");

		List<String> methodNames=new ArrayList<>();
		methodNames.add(DevQsarConstants.KNN);
		methodNames.add(DevQsarConstants.RF);
		methodNames.add(DevQsarConstants.XGB);
		methodNames.add(DevQsarConstants.SVM);
		methodNames.add(DevQsarConstants.CONSENSUS);

		Hashtable<String,Double>htVals=new Hashtable<>();
		for (String methodName:methodNames) {
			addHashtableEntry(statisticName, methodName, modelSetNames, datasetNames,htVals);
		}
		
		StringBuffer sb=new StringBuffer();
		
//		for (String modelSetName:modelSetNames) {
//			createSummaryTableForModelSet(statisticName, modelSetName, methodNames, datasetNames, htVals,sb);
//		}
//		
//		System.out.println(sb.toString());
		
		try {
			FileWriter fw=new FileWriter("data/reports/"+statisticName+".txt");
			fw.write(sb.toString());
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}


//		for (String methodName:methodNames) {
//			createSummaryTableForMethod(statisticName, methodName, modelSetNames, datasetNames, htVals);
//		}
		
		createSummaryTableForMethod(statisticName, DevQsarConstants.KNN, modelSetNames, datasetNames, htVals);
		
	}
	
	/**
	 * Stores stat in hashtable
	 * 
	 * @param statisticName
	 * @param methodName
	 * @param modelSetNames
	 * @param datasetNames
	 */
	private void addHashtableEntry(String statisticName, String methodName, List<String> modelSetNames,
			List<String> datasetNames,Hashtable<String,Double>htVals) {
		
		for (int i=0;i<datasetNames.size();i++) {
			String datasetName=datasetNames.get(i);
						
			for (int j=0;j<modelSetNames.size();j++) {
				String modelSetName=modelSetNames.get(j);
				
				if(modelSetName.contains("_justPFAS")) {
//					System.out.println("Skipping"+modelSetName);
					continue;
				}

				String key=methodName+"\t"+modelSetName+"\t"+datasetName;
				
				Long modelId=getModelId(modelSetName, datasetName, methodName);
				
				if (modelId==null) {
					htVals.put(key, Double.NaN);
					continue;
				}
				
				Double stat=getStat(modelId, statisticName);
				
//				System.out.println(modelId+"\t"+key+"\t"+stat);
				
				if (stat!=null)	htVals.put(key, stat);
				else htVals.put(key, Double.NaN);
					
			}
		}
	}
	
	/**
	 * Stores stat in hashtable
	 * 
	 * @param statisticName
	 * @param methodName
	 * @param modelSetNames
	 * @param datasetNames
	 */
	private void addHashtableEntryLimitToPFAS(String statisticName, String methodName, String datasetName,
			Hashtable<String,Double>htVals,List<String>smilesArrayPFAS) {
		addPFAS_Stats(statisticName, methodName, htVals, smilesArrayPFAS, datasetName, "WebTEST2.0");
		addPFAS_Stats(statisticName, methodName, htVals, smilesArrayPFAS, datasetName, "WebTEST2.1");
	}


	private void addPFAS_Stats(String statisticName, String methodName, Hashtable<String, Double> htVals,
			List<String> smilesArrayPFAS, String datasetName, String modelSetName) {

		System.out.println("Adding PFAS stats for "+datasetName+"\t"+modelSetName);

		String key=methodName+"\t"+modelSetName+"_justPFAS"+"\t"+datasetName;
			
		Long modelId=getModelId(modelSetName, datasetName, methodName);
			
		if (modelId==null) {
			htVals.put(key, Double.NaN);
			return;
		}

		Double stat=null;
		
		Model model=modelService.findById(modelId);
		
		if (statisticName.equals("Q2_CV_Training") || statisticName.equals("R2_CV_Training")) {
			stat=calculateCV_Stat(model, statisticName, smilesArrayPFAS);
		} else {
			stat=calcPredictionStatsForPFAS(model,statisticName,smilesArrayPFAS);
		}
					
//		System.out.println(key+"\t"+stat);
		
		if (stat!=null)	htVals.put(key, stat);
		else htVals.put(key, Double.NaN);

	}
	
	
	void createSummaryTableForMethodTEST() {
		
		List<String> modelSetNames=new ArrayList<>();
		
		
		String modelSetName="WebTEST2.1 Sample models";

		List<String> methodNames=new ArrayList<>();
		methodNames.add(DevQsarConstants.KNN);
		methodNames.add(DevQsarConstants.RF);
		methodNames.add(DevQsarConstants.XGB);
		methodNames.add(DevQsarConstants.SVM);
		methodNames.add(DevQsarConstants.CONSENSUS);

		List<String>datasetNames=new ArrayList<>();
		datasetNames.add(DevQsarConstants.DEV_TOX+" TEST");
		datasetNames.add(DevQsarConstants.MUTAGENICITY+" TEST");
		datasetNames.add(DevQsarConstants.LLNA+" TEST");

		Hashtable<String,Double>htVals=new Hashtable<>();

		for (String methodName:methodNames) {
			addHashtableEntry("BA_Test", methodName, modelSetNames, datasetNames,htVals);
		}
		
		createSummaryTableForModelSet("BA_Test", modelSetName, methodNames, datasetNames, htVals);			
		
		List<String>datasetNames2=new ArrayList<>();
		datasetNames2.add(DevQsarConstants.LD50+" TEST");
		datasetNames2.add(DevQsarConstants.LC50+" TEST");
		datasetNames2.add(DevQsarConstants.LC50DM+" TEST");
		datasetNames2.add(DevQsarConstants.IGC50+" TEST");

		for (String methodName:methodNames) {
			addHashtableEntry("PearsonRSQ_Test", methodName, modelSetNames, datasetNames2,htVals);
		}
		createSummaryTableForModelSet("PearsonRSQ_Test", modelSetName, methodNames, datasetNames, htVals);			
		
	}
	
	
	
	
	String[] getDatasetSplittingNames(long modelId) {
		String[] values = new String[2];
		String sql = "select dataset_name,splitting_name  from qsar_models.models m\n" + "where m.id=" + modelId;
		ResultSet rs = DatabaseLookup.runSQL2(conn, sql);
		try {
			if (rs.next()) {
				values[0] = rs.getString(1);
				values[1] = rs.getString(2);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return values;
	}
	
		
	
	/**
	 * For splitting= "T=all, P=PFAS" generates the stats P=PFAS without having to make new models
	 * for this splitting (can use model for RND_REPRESENTATIVE)
	 * 
	 */
	public Double calcPredictionStatsForPFAS(Model model,String statisticName,List<String>smilesArrayPFAS) {
				
		SplitPredictions sp=SplitPredictions.getSplitPredictionsSql(model, model.getSplittingName());
				
		//Remove non PFAS compounds:
		if (smilesArrayPFAS!=null) {
			sp.removeNonPFAS(smilesArrayPFAS);
		}

//		for (int i=0;i<modelPredictions.size();i++) {
//			ModelPrediction mp=modelPredictions.get(i);
//			System.out.println(mp.ID+"\t"+mp.exp+"\t"+mp.pred);
//		}

		//	***************************************
		// Calc stats		
		double mean_exp_training=ModelStatisticCalculator.calcMeanExpTraining(sp.trainingSetPredictions);
								
		Map<String, Double>statsMap=ModelStatisticCalculator.calculateContinuousStatistics(sp.testSetPredictions,mean_exp_training,DevQsarConstants.TAG_TEST);
		
		if (statsMap.get(statisticName)==null) return null;
		
		double stat=statsMap.get(statisticName);

//		System.out.println(datasetName+"\t"+"number of preds="+modelPredictions.size());
//		System.out.println(statisticName+"="+stat);//need to make sure number of chemicals matches excel table
		return stat;
	}
	
	/**
	 * 
	 * Calculates Q2_F3 see eqn 2 of Consonni et al, 2019 (https://onlinelibrary.wiley.com/doi/full/10.1002/minf.201800029)
	 * 
	 * @param modelId
	 * @return
	 */
	public double calculateCV_Stat(Model model,String stat,List<String>smilesArrayPFAS) {
		
		double stat_Avg=0;

		for (int i=1;i<=5;i++) {
			
			String splittingName=model.getSplittingName()+"_CV"+i;
			
			SplitPredictions sp=SplitPredictions.getSplitPredictionsSql(model, splittingName);
			
			if (smilesArrayPFAS!=null) {
				sp.removeNonPFAS(smilesArrayPFAS);
			}
			
//			System.out.println("***"+sp.testSetPredictions.size()+"\t"+sp.trainingSetPredictions.size());
			
//			for (ModelPrediction mp:sp.trainingSetPredictions) {
//				System.out.println(mp.id+"\t"+mp.exp+"\t"+mp.pred);
//			}
			
						
			double stat_i=0;
			
			if(stat.equals("Q2_CV_Training")) {
				stat_i=ModelStatisticCalculator.calculateQ2(sp.trainingSetPredictions, sp.testSetPredictions);	
			} else if(stat.equals("R2_CV_Training")) {				
				double YbarTrain=ModelStatisticCalculator.calcMeanExpTraining(sp.trainingSetPredictions);				
				Map<String, Double>mapStats=ModelStatisticCalculator.calculateContinuousStatistics(sp.testSetPredictions, YbarTrain, DevQsarConstants.TAG_TEST);				
				stat_i=mapStats.get(DevQsarConstants.PEARSON_RSQ + DevQsarConstants.TAG_TEST);
			}
						
//			System.out.println("stat"+i+"="+stat_i);			
			stat_Avg+=stat_i;
		}		
		stat_Avg/=5.0;
//		System.out.println("stat_Avg="+stat_Avg);
		
		return stat_Avg;
		
	}

	private List<ModelPrediction> mergeExpPredValues(Hashtable<String, Double> htPred, String datasetName,
			String splittingName) {
		String sql="select dp.canon_qsar_smiles, dp. qsar_property_value from qsar_datasets.data_points dp\n"+ 
				"join qsar_datasets.datasets d on dp.fk_dataset_id =d.id\n"+
				"join qsar_datasets.data_points_in_splittings dpis on dpis.fk_data_point_id =dp.id\n"+
				"join qsar_datasets.splittings s on s.id=dpis.fk_splitting_id\n"+
				"where d.\"name\" ='"+datasetName+"' and dpis.split_num =1 and s.\"name\"='"+splittingName+"'"; 

		ResultSet rs=DatabaseLookup.runSQL2(conn, sql);
		List<ModelPrediction>modelPredictions=new ArrayList<>(); 
		
		try {
			while (rs.next()) {				
				String ID=rs.getString(1);
				Double exp=Double.parseDouble(rs.getString(2));
				Double pred=htPred.get(ID);				
				modelPredictions.add(new ModelPrediction(ID,exp,pred,1));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}
		return modelPredictions;
	}

	
	
	void createPredictionReportsExcelForJustPFAS() {
		
//		String modelSetName="WebTEST2.0";
		String modelSetName="WebTEST2.1";

		
		String listName="PFASSTRUCTV4";		
		String folder="data/dev_qsar/dataset_files/";
		String filePathPFAS=folder+listName+"_qsar_ready_smiles.txt";
		ArrayList<String>smilesArray=SplittingGeneratorPFAS_Script.getPFASSmiles(filePathPFAS);
		
		List<String>datasetNames=new ArrayList<>();
		datasetNames.add("HLC from exp_prop and chemprop");
		datasetNames.add("WS from exp_prop and chemprop");
		datasetNames.add("VP from exp_prop and chemprop");
		datasetNames.add("LogP from exp_prop and chemprop");
		datasetNames.add("MP from exp_prop and chemprop");
		datasetNames.add("BP from exp_prop and chemprop");
		
		datasetNames.add("pKa_a from exp_prop and chemprop");
		datasetNames.add("pKa_b from exp_prop and chemprop");

		
		SampleReportWriter srw = new SampleReportWriter();

		boolean overWriteReportFiles=false;
		boolean deleteExistingReportInDatabase=false;
		boolean upload=false;

		String splittingName=DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
		
		ModelSetServiceImpl modelSetService=new ModelSetServiceImpl();
		ModelSet ms=modelSetService.findByName(modelSetName);

	
//		Create the PredictionReport for all compounds in SPLITTING_RND_REPRESENTATIVE:
		for (String datasetName:datasetNames) {
			srw.generateSamplePredictionReport(ms.getId(), datasetName, splittingName,upload,deleteExistingReportInDatabase,overWriteReportFiles);
		}
		
		for (String datasetName:datasetNames) {
			
			if (datasetName.contains("pKa"))continue;
			
			PredictionReport predictionReport=srw.getReport(modelSetName, datasetName, splittingName);

			//Delete non PFAS from report and recalc stats:
			limitPredictionReportToPFAS(smilesArray, predictionReport);
			
			String filePath = "data/reports/" + modelSetName+"/"+datasetName + "_PredictionReport_only_PFAS.json";
			ReportGenerationScript.writeReport(predictionReport, filePath);
			 
//			System.out.println(Utilities.gson.toJson(predictionReport));
			
			String outputFolder = "data/reports/prediction reports upload";
			
			String filepathExcel = outputFolder + File.separator + modelSetName+File.separator+String.join("_", datasetName, splittingName)
			+ "_PFAS.xlsx";
			
			if(!new File(filepathExcel).exists()) {
				ExcelPredictionReportGenerator eprg = new ExcelPredictionReportGenerator();
				eprg.generate(predictionReport, filepathExcel,smilesArray);
				System.out.println("Created:"+filepathExcel);
			} else {
				System.out.println("Exists:"+filepathExcel);
			}
			
		}
		
		
	}
	
	
	private void createPredictionReportsExcelPFASOnlyModels() {
		

		String splittingName="T=PFAS only, P=PFAS";
		String modelSetName="WebTEST2.0 PFAS";//TODO look up from id
		
//		String splittingName="T=PFAS only, P=PFAS";
//		String modelSetName="WebTEST2.1 PFAS";//TODO look up from id

//		String splittingName="T=all but PFAS, P=PFAS";	
//		String modelSetName="WebTEST2.0 All but PFAS";//TODO look up from id
		
//		String splittingName="T=all but PFAS, P=PFAS";	
//		String modelSetName="WebTEST2.1 All but PFAS";//TODO look up from id

		ModelSetServiceImpl modelSetService=new ModelSetServiceImpl();
		ModelSet ms=modelSetService.findByName(modelSetName);
		
		String listName="PFASSTRUCTV4";		
		String folder="data/dev_qsar/dataset_files/";
		String filePathPFAS=folder+listName+"_qsar_ready_smiles.txt";
		ArrayList<String>smilesArray=SplittingGeneratorPFAS_Script.getPFASSmiles(filePathPFAS);	
		
		List<String>datasetNames=new ArrayList<>();
		datasetNames.add("HLC from exp_prop and chemprop");
		datasetNames.add("WS from exp_prop and chemprop");
		datasetNames.add("VP from exp_prop and chemprop");
		datasetNames.add("LogP from exp_prop and chemprop");
		datasetNames.add("MP from exp_prop and chemprop");
		datasetNames.add("BP from exp_prop and chemprop");
		
		SampleReportWriter srw = new SampleReportWriter();
		
		boolean overWriteReportFiles=false;
				
		for (String datasetName:datasetNames) {

			PredictionReport predictionReport=srw.createPredictionReport(ms.getId(), datasetName, splittingName,overWriteReportFiles);
			
			String outputFolder = "data/reports/prediction reports upload";
			
			String filepathExcel = outputFolder + File.separator +modelSetName+File.separator+ String.join("_", datasetName, splittingName)
			+ ".xlsx";
			
			if(!new File(filepathExcel).exists()) {
				ExcelPredictionReportGenerator eprg = new ExcelPredictionReportGenerator();
				eprg.generate(predictionReport, filepathExcel,smilesArray);
				System.out.println("Created:"+filepathExcel);
			} else {
				System.out.println("Exists:"+filepathExcel);
			}
			
		}
		
		

	}

	void addMappedRecordsToExcel(String filepath,String dataSetName) {
		
		try {
            			
			XSSFWorkbook wb = (XSSFWorkbook)WorkbookFactory.create(new File(filepath));

			String dataSetName2=dataSetName.replace(" ", "_");
			String folder="data\\dev_qsar\\output\\";
			String jsonPath=folder+"//"+dataSetName2+"//"+dataSetName2+"_Mapped_Records.json";

			JsonArray ja=Utilities.getJsonArrayFromJsonFile(jsonPath);
			
			ExcelCreator ec=new ExcelCreator();
			
			String[] fields = { "exp_prop_id", "canon_qsar_smiles", "page_url", "source_url", "source_doi",
					"source_name", "source_description", "source_type", "source_authors", "source_title", "source_dtxrid",
					"source_dtxsid", "source_casrn", "source_chemical_name", "source_smiles", "mapped_dtxcid", "mapped_dtxsid",
					"mapped_cas", "mapped_chemical_name", "mapped_smiles", "mapped_molweight", "value_original", "value_max",
					"value_min", "value_point_estimate", "value_units", "qsar_property_value", "qsar_property_units",
					"temperature_c", "pressure_mmHg", "pH", "notes", "qc_flag"};

			Hashtable<String,String>htDescriptions=ExcelCreator.getColumnDescriptions();
			
			ec.addSheet(wb, "Exp. Records",ja,fields, htDescriptions);
			
			System.out.println(wb.getNumberOfSheets());
			

			FileOutputStream fos = new FileOutputStream(filepath);
			wb.write(fos);
			wb.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}
	
	public static void getStatsInsideAD(PredictionReport predictionReport,List<ApplicabilityDomainPrediction>adPredictions,ArrayList<String> smilesArray) {

		Hashtable<String,ApplicabilityDomainPrediction>htAD=new Hashtable<>();
		
		for (ApplicabilityDomainPrediction ad:adPredictions) {
			htAD.put(ad.id,ad);
		}
		
//		System.out.println(htAD.size());
		
		
		//Delete old statistics:
		for (PredictionReportModelMetadata prmmd:predictionReport.predictionReportModelMetadata) {
			prmmd.predictionReportModelStatistics.clear();
		}
		
		Hashtable<String,List<ModelPrediction>>htModelPredictionsTestSet=new Hashtable<>();
		Hashtable<String,List<ModelPrediction>>htModelPredictionsTrainingSet=new Hashtable<>();
		
		for (int i=0;i<predictionReport.predictionReportDataPoints.size();i++) {				
			PredictionReportDataPoint dp=predictionReport.predictionReportDataPoints.get(i);
			
			for (QsarPredictedValue qpv:dp.qsarPredictedValues) {
				
				if(qpv.splitNum==DevQsarConstants.TEST_SPLIT_NUM) {
					storePredictionInHashtable(htModelPredictionsTestSet, dp, qpv);
				} 
				if (qpv.splitNum==DevQsarConstants.TRAIN_SPLIT_NUM) {
					storePredictionInHashtable(htModelPredictionsTrainingSet, dp, qpv);
				}
			}
		}

		for (PredictionReportModelMetadata prmm:predictionReport.predictionReportModelMetadata) {

//			System.out.println(prmm.qsarMethodName);

			List<ModelPrediction>trainingSetPredictions=htModelPredictionsTrainingSet.get(prmm.qsarMethodName);
			List<ModelPrediction>testSetPredictions=htModelPredictionsTestSet.get(prmm.qsarMethodName);
			
			for(int i=0;i<testSetPredictions.size(); i++) {								
				ModelPrediction mp=testSetPredictions.get(i);				
				
				if (smilesArray!=null) {
					if(!smilesArray.contains(mp.id)) {
						testSetPredictions.remove(i--);
					}
				}
				
				if (htAD.get(mp.id)!=null) {				
					ApplicabilityDomainPrediction ad=htAD.get(mp.id);					
					if (!ad.AD)	mp.pred=null;//stat calculations use null preds to calc coverage
					
//					System.out.println(mp.ID+"\t"+mp.AD);
				}
				
//				System.out.println(mp.ID+"\t"+mp.exp+"\t"+mp.pred);
			}
			
			for(int i=0;i<trainingSetPredictions.size(); i++) {
				ModelPrediction mp=trainingSetPredictions.get(i);
				
				if (smilesArray!=null) {
					if(!smilesArray.contains(mp.id)) {
						trainingSetPredictions.remove(i--);
					}
				}
//				System.out.println(mp.ID+"\t"+mp.exp+"\t"+mp.pred);
			}

			System.out.println(testSetPredictions.size());
			System.out.println(trainingSetPredictions.size());

			double meanExpTraining = calculateMeanExpTraining(trainingSetPredictions);

			Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator
					.calculateContinuousStatistics(testSetPredictions, meanExpTraining, DevQsarConstants.TAG_TEST);

			Map<String, Double> modelTrainingStatisticValues = ModelStatisticCalculator.calculateContinuousStatistics(
					trainingSetPredictions, meanExpTraining, DevQsarConstants.TAG_TRAINING);
			
			for (String statisticName:modelTestStatisticValues.keySet()) {
				prmm.predictionReportModelStatistics.add(new PredictionReportModelStatistic(statisticName, modelTestStatisticValues.get(statisticName)));
			}

			for (String statisticName:modelTrainingStatisticValues.keySet()) {
				prmm.predictionReportModelStatistics.add(new PredictionReportModelStatistic(statisticName, modelTrainingStatisticValues.get(statisticName)));
			}
			
//			System.out.println(Utilities.gson.toJson(prmm.predictionReportModelStatistics));
			
			
		}//end loop over model metadata
	}
	
	
	private void limitPredictionReportToPFAS(ArrayList<String> smilesArray, PredictionReport predictionReport) {
		//Delete old statistics:
		for (PredictionReportModelMetadata prmmd:predictionReport.predictionReportModelMetadata) {
			prmmd.predictionReportModelStatistics.clear();
		}
		
		Hashtable<String,List<ModelPrediction>>htModelPredictionsTestSet=new Hashtable<>();
		Hashtable<String,List<ModelPrediction>>htModelPredictionsTrainingSet=new Hashtable<>();
		
		for (int i=0;i<predictionReport.predictionReportDataPoints.size();i++) {				
			PredictionReportDataPoint dp=predictionReport.predictionReportDataPoints.get(i);
			
			if(!smilesArray.contains(dp.canonQsarSmiles)) {
//					System.out.println(dp.canonQsarSmiles);
				predictionReport.predictionReportDataPoints.remove(i--);
				continue;
			}

			for (QsarPredictedValue qpv:dp.qsarPredictedValues) {
				
				if(qpv.splitNum==DevQsarConstants.TEST_SPLIT_NUM) {
					storePredictionInHashtable(htModelPredictionsTestSet, dp, qpv);
				} else if (qpv.splitNum==DevQsarConstants.TRAIN_SPLIT_NUM) {
					storePredictionInHashtable(htModelPredictionsTrainingSet, dp, qpv);
				}
			}
		}

		for (PredictionReportModelMetadata prmm:predictionReport.predictionReportModelMetadata) {

//			System.out.println(prmm.qsarMethodName);

			List<ModelPrediction>trainingSetPredictions=htModelPredictionsTrainingSet.get(prmm.qsarMethodName);
			List<ModelPrediction>testSetPredictions=htModelPredictionsTestSet.get(prmm.qsarMethodName);
			
//			if (prmm.qsarMethodName.equals("knn_regressor_1.2")) {
//				for (ModelPrediction mp:testSetPredictions) {
//					System.out.println(mp.ID+"\t"+mp.exp+"\t"+mp.pred);
//				}
//			}
			
			double meanExpTraining = calculateMeanExpTraining(trainingSetPredictions);

			Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator
			.calculateContinuousStatistics(testSetPredictions, meanExpTraining, DevQsarConstants.TAG_TEST);

			Map<String, Double> modelTrainingStatisticValues = ModelStatisticCalculator.calculateContinuousStatistics(
			trainingSetPredictions, meanExpTraining, DevQsarConstants.TAG_TRAINING);
			
			for (String statisticName:modelTestStatisticValues.keySet()) {
				prmm.predictionReportModelStatistics.add(new PredictionReportModelStatistic(statisticName, modelTestStatisticValues.get(statisticName)));
			}

			for (String statisticName:modelTrainingStatisticValues.keySet()) {
				prmm.predictionReportModelStatistics.add(new PredictionReportModelStatistic(statisticName, modelTrainingStatisticValues.get(statisticName)));
			}
		}
	}


	private static double calculateMeanExpTraining(List<ModelPrediction> trainingSetPredictions) {
		double meanExpTraining = 0.0;
		int count = 0;
		for (ModelPrediction mp:trainingSetPredictions) {
			if (mp.exp!=null) {
				meanExpTraining += mp.exp;
				count++;
			}
		}
		meanExpTraining /= count;
		return meanExpTraining;
	}


	private static void storePredictionInHashtable(Hashtable<String, List<ModelPrediction>> htModelPredictions,
			PredictionReportDataPoint dp, QsarPredictedValue qpv) {
		if (htModelPredictions.get(qpv.qsarMethodName)==null) {
			List<ModelPrediction>modelPredictions=new ArrayList<>();
			htModelPredictions.put(qpv.qsarMethodName,modelPredictions);
			modelPredictions.add(new ModelPrediction(dp.canonQsarSmiles,dp.experimentalPropertyValue,qpv.qsarPredictedValue,qpv.splitNum));
		} else {
			List<ModelPrediction>modelPredictions=htModelPredictions.get(qpv.qsarMethodName);
			modelPredictions.add(new ModelPrediction(dp.canonQsarSmiles,dp.experimentalPropertyValue,qpv.qsarPredictedValue,qpv.splitNum));
		}
	}

	
	
	
	public static void main(String[] args) {
		PredictionStatisticsScript ms=new PredictionStatisticsScript();
		
//		ms.createSummaryTableForMethod_Rnd_Representative();
		
		ms.createSummaryTableForMethod();
		ms.createSummaryTableForSet();
		
//		ms.createPredictionReportsExcelForJustPFAS();
//		ms.createPredictionReportsExcelPFASOnlyModels();
		
//		ms.createSummaryTableForMethodTEST();
//		Double stat=ms.calcPredictionStatsForPFAS(816,"MAE_Test",null);
		
//		
//		RecalcStatsScript.viewPredsForSplitSet(1111L);
	}

}
