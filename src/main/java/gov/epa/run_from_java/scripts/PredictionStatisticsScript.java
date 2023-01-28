package gov.epa.run_from_java.scripts;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.models.ModelStatisticCalculator;
import gov.epa.endpoints.reports.predictions.QsarPredictedValue;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportDataPoint;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;
import gov.epa.web_services.embedding_service.CalculationInfo;

public class PredictionStatisticsScript {

	
	String lanId="tmarti02";
	Connection conn=DatabaseLookup.getConnection();

	
	/**
	 * Prints summary of stats to the screen
	 * TODO make it write to file
	 * 
	 * 
	 * @param statisticName
	 * @param methodName
	 * @param modelSetNames
	 * @param datasetNames
	 */
	private void createSummaryTable(String statisticName, String methodName, List<String> modelSetNames,
			List<String> datasetNames) {
		DecimalFormat df=new DecimalFormat("0.00");
		
		System.out.println("\nResults for "+methodName+" method");
		System.out.print("DatasetName\t");
		
		for (int j=0;j<modelSetNames.size();j++) {
			String modelSetName=modelSetNames.get(j);
		
			System.out.print(modelSetName);			
			if (j<modelSetNames.size()-1) System.out.print("\t");
			else System.out.print("\r\n");
		}
		
		for (int i=0;i<datasetNames.size();i++) {
			String datasetName=datasetNames.get(i);
			
//			String datasetName2=datasetName.replace(" from exp_prop and chemprop", "");
						
			System.out.print(datasetName+"\t");
			
			List<String>modelSetStats=new ArrayList<>();
			
			for (int j=0;j<modelSetNames.size();j++) {
				String modelSetName=modelSetNames.get(j);
				Long modelId=getModelId(modelSetName, datasetName, methodName);
				
				if (modelId==null) {
					modelSetStats.add(null);
					continue;
				}

				Double stat=getStat(modelId, statisticName);
				
				if (stat==null)	modelSetStats.add(null);					
				else modelSetStats.add(df.format(stat));
			}
			
			for (int j=0;j<modelSetStats.size();j++) {
				String modelSetStat=modelSetStats.get(j);
				System.out.print(modelSetStat);			
				if (j<modelSetStats.size()-1) System.out.print("\t");
				else System.out.print("\r\n");
			}
			
		}
	}
	
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
		String strId=DatabaseLookup.runSQL(conn, sql);
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
		DecimalFormat df=new DecimalFormat("0.00");
		
		System.out.println("\n"+statName+" results for model set = "+methodName);
		System.out.print("DatasetName\t");
		
		for (int j=0;j<modelSetNames.size();j++) {
			String modelSetName=modelSetNames.get(j);
		
			System.out.print(modelSetName);			
			if (j<modelSetNames.size()-1) System.out.print("\t");
			else System.out.print("\r\n");
		}
		
		for (int i=0;i<datasetNames.size();i++) {
			String datasetName=datasetNames.get(i);
			
//			String datasetName2=datasetName.replace(" from exp_prop and chemprop", "");						
			System.out.print(datasetName+"\t");
						
								
			for (int j=0;j<modelSetNames.size();j++) {
				String modelSetName=modelSetNames.get(j);
				
				String key=methodName+"\t"+modelSetName+"\t"+datasetName;
				
//				System.out.println(key);
				
				Double modelSetStat=htVals.get(key);
				
				if (modelSetName==null) System.out.print("N/A");				
				else System.out.print(df.format(modelSetStat));
				
				if (j<modelSetNames.size()-1) System.out.print("\t");
				else System.out.print("\r\n");
			}
			
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
		DecimalFormat df=new DecimalFormat("0.00");
		
		System.out.println("\n"+statName+" results for model set = "+modelSetName);
		System.out.print("DatasetName\t");
		
		for (int j=0;j<methodNames.size();j++) {
			String methodName=methodNames.get(j);
		
			System.out.print(methodName);			
			if (j<methodNames.size()-1) System.out.print("\t");
			else System.out.print("\r\n");
		}
		
		for (int i=0;i<datasetNames.size();i++) {
			String datasetName=datasetNames.get(i);
			
//			String datasetName2=datasetName.replace(" from exp_prop and chemprop", "");						
			System.out.print(datasetName+"\t");
						
			for (int j=0;j<methodNames.size();j++) {
				String methodName=methodNames.get(j);

				String key=methodName+"\t"+modelSetName+"\t"+datasetName;
				
//				System.out.println(key);
				
				Double modelSetStat=htVals.get(key);
								
				if (modelSetName==null) System.out.print("N/A");				
				else System.out.print(df.format(modelSetStat));

				if (j<methodNames.size()-1) System.out.print("\t");
				else System.out.print("\r\n");
			}
		}
	}
	
	
	void createSummaryTableForMethod() {
		String statisticName = "MAE_Test";
//		String statisticName="PearsonRSQ_Test";
		
		List<String> modelSetNames=new ArrayList<>();
				
		modelSetNames.add("WebTEST2.0 PFAS");
//		modelSetNames.add("WebTEST2.0");//TODO calc stats just for PFAS
//		modelSetNames.add("WebTEST2.0 All but PFAS");
//		modelSetNames.add("WebTEST2.0");
		
		modelSetNames.add("WebTEST2.1 PFAS");
//		modelSetNames.add("WebTEST2.1");//TODO calc stats just for PFAS
//		modelSetNames.add("WebTEST2.1 All but PFAS");
//		modelSetNames.add("WebTEST2.1");
		
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

		for (String modelSetName:modelSetNames) {
			createSummaryTableForModelSet(statisticName, modelSetName, methodNames, datasetNames, htVals);
		}

//		for (String methodName:methodNames) {
//			createSummaryTableForMethod(statisticName, methodName, modelSetNames, datasetNames, htVals);
//		}
		
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
	
	void createSummaryTableForMethodTEST() {
		
		List<String> modelSetNames=new ArrayList<>();
		modelSetNames.add("WebTEST2.1 Sample models");

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

		for (String modelSetName:modelSetNames) {
			createSummaryTableForModelSet("BA_Test", modelSetName, methodNames, datasetNames, htVals);
		}

		
		List<String>datasetNames2=new ArrayList<>();
		datasetNames2.add(DevQsarConstants.LD50+" TEST");
		datasetNames2.add(DevQsarConstants.LC50+" TEST");
		datasetNames2.add(DevQsarConstants.LC50DM+" TEST");
		datasetNames2.add(DevQsarConstants.IGC50+" TEST");

		for (String methodName:methodNames) {
			addHashtableEntry("PearsonRSQ_Test", methodName, modelSetNames, datasetNames2,htVals);
		}

		for (String modelSetName:modelSetNames) {
			createSummaryTableForModelSet("PearsonRSQ_Test", modelSetName, methodNames, datasetNames2, htVals);
		}

//		for (String methodName:methodNames) {
//			createSummaryTableForMethod(statisticName, methodName, modelSetNames, datasetNames, htVals);
//		}
		
	}
	
	
	
	
	String[] getDatasetSplitting(long modelId) {
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
	public Double calcPredictionStatsForPFAS(int modelId,String statisticName) {
		
		Hashtable<String, Double> htPred = getPredValues(modelId);

		String []vals=getDatasetSplitting(modelId);
		String datasetName=vals[0];
		String splittingName=vals[1];
		
		List<ModelPrediction> modelPredictions = getExpValues(htPred, datasetName, splittingName);
		
		//	***************************************
		// Getting predictions for PFAS compounds in test set:		
		String listName="PFASSTRUCTV4";		
		String folder="data/dev_qsar/dataset_files/";
		String filePath=folder+listName+"_qsar_ready_smiles.txt";
		ArrayList<String>smilesArray=SplittingGeneratorPFAS.getPFASSmiles(filePath);
		
//		for (String smiles:smilesArray) {
//			System.out.println(smiles);
//		}
		
		//Remove non PFAS compounds:
		for (int i=0;i<modelPredictions.size();i++) {
			ModelPrediction mp=modelPredictions.get(i);
			if(!smilesArray.contains(mp.ID)) {
				modelPredictions.remove(i--);
			}
		}
		
//		for (int i=0;i<modelPredictions.size();i++) {
//			ModelPrediction mp=modelPredictions.get(i);
//			System.out.println(mp.ID+"\t"+mp.exp+"\t"+mp.pred);
//		}

		//	***************************************
		// Calc stats		
		double mean_exp_training=0;//TODO q2 will be wrong unless fixed. 
		Map<String, Double>statsMap=ModelStatisticCalculator.calculateContinuousStatistics(modelPredictions,mean_exp_training,DevQsarConstants.TAG_TEST);
		double stat=statsMap.get(statisticName);
		System.out.println("number of preds="+modelPredictions.size());
		System.out.println(statisticName+"="+stat);//need to make sure number of chemicals matches excel table
		return stat;
	}

	private List<ModelPrediction> getExpValues(Hashtable<String, Double> htPred, String datasetName,
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
				modelPredictions.add(new ModelPrediction(ID,exp,pred));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}
		return modelPredictions;
	}

	private Hashtable<String, Double> getPredValues(int modelId) {
		//Get pred values:
		String sql="select canon_qsar_smiles,qsar_predicted_value  from qsar_models.predictions p where fk_model_id="+modelId;
		ResultSet rs=DatabaseLookup.runSQL2(conn, sql);
		Hashtable<String,Double>htPred=new Hashtable<>();
		
		try {
			while (rs.next()) {				
				String ID=rs.getString(1);
				Double pred=Double.parseDouble(rs.getString(2));
				htPred.put(ID, pred);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return htPred;
	}
	
	public static void main(String[] args) {
		PredictionStatisticsScript ms=new PredictionStatisticsScript();
//		ms.createSummaryTableForMethod();
//		ms.createSummaryTableForMethodTEST();
		Double stat=ms.calcPredictionStatsForPFAS(816,"MAE_Test");
	}

}
