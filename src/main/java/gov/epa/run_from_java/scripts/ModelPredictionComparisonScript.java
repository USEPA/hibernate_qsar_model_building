package gov.epa.run_from_java.scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.models.ModelStatisticCalculator;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

/**
* @author TMARTI02
*/
public class ModelPredictionComparisonScript {

	/** Compare predictions in test set of model 1 that are not in training set of model2
	 * 
	 * @param datasetName1
	 * @param datasetName2
	 * @param descriptorSetName
	 * @param splittingName
	 */
	void compare(String datasetName1,String datasetName2, String descriptorSetName,String splittingName) {
		
		boolean removeLogP=false;
		boolean useDTXCIDs=false;
		
		//Get training and test set instances:
		ModelData md1=ModelData.initModelData(datasetName1, descriptorSetName,splittingName, removeLogP,useDTXCIDs);
		ModelData md2=ModelData.initModelData(datasetName2, descriptorSetName,splittingName, removeLogP,useDTXCIDs);
		
		//Determine which chemicals in md1.testSet are not in md2.trainingSet:
		
		String  [] linesTest1=md1.predictionSetInstances.split("\r\n");
		String  [] linesTraining2=md2.trainingSetInstances.split("\r\n");
		
		HashSet<String>idsTraining2=new HashSet<>();
		
		for (int i=1;i<linesTraining2.length;i++) {
			
			String line=linesTraining2[i];
			String id=line.substring(0,line.indexOf("\t"));
//			System.out.println(id);
			idsTraining2.add(id);
		}
		
		
		String predictionTsv=linesTest1[0]+"\r\n";
		
		int count=0;
		
		for (int i=1;i<linesTest1.length;i++) {
			String line=linesTest1[i];
			String id=line.substring(0,line.indexOf("\t"));
			if(idsTraining2.contains(id)) {//skip if in training set 2
//				System.out.println(id+"\tin training 2");
				continue;
			}
			predictionTsv+=line+"\r\n";
			count++;
		}
		
		System.out.println("Number in new prediction tsv="+count);
		
		//TODO these sql queries need to also look at qsar method and embedding to get the right model id
		
		String sql1="select * from qsar_models.models where dataset_name='"+datasetName1+ "' and descriptor_set_name='"+descriptorSetName+"';";
		long modelId1=Long.parseLong(SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql1));
		System.out.println(modelId1);

		String sql2="select * from qsar_models.models where dataset_name='"+datasetName2+ "' and descriptor_set_name='"+descriptorSetName+"';";
		long modelId2=Long.parseLong(SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql2));
		System.out.println(modelId2);

		PredictScript ps=new PredictScript();
		
		List<ModelPrediction>mps1=Arrays.asList(ps.predict(modelId1, predictionTsv));
		List<ModelPrediction>mps2=Arrays.asList(ps.predict(modelId2, predictionTsv));
		
		Map<String, Double>mapstats1=ModelStatisticCalculator.calculateContinuousStatistics(mps1,1.0,DevQsarConstants.TAG_TEST);
		Map<String, Double>mapstats2=ModelStatisticCalculator.calculateContinuousStatistics(mps2,1.0,DevQsarConstants.TAG_TEST);

		System.out.println(Utilities.gson.toJson(mapstats1));
		System.out.println(Utilities.gson.toJson(mapstats2));
		
	}
	
	public static void main(String[] args) {
		ModelPredictionComparisonScript m=new ModelPredictionComparisonScript();
		
		String datasetName1="exp_prop_BCF_fish_whole_body_overall_score_1_v2_modeling_map_by_CAS";
		
		String datasetName2="exp_prop_BCF_fish_whole_body_v1_modeling_map_by_CAS";
		
		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_MORDRED;
		String splittingName=DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
		m.compare(datasetName1, datasetName2,descriptorSetName, splittingName);

	}

}

