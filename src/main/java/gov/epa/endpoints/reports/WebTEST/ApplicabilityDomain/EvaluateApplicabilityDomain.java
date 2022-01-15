package gov.epa.endpoints.reports.WebTEST.ApplicabilityDomain;

import java.io.File;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.QSAR.CompareAnalogs.AnalogFinder;
import gov.epa.QSAR.Report.PredictionReportData;
import gov.epa.QSAR.api.RecordPredictedValue;
import gov.epa.QSAR.api.RecordsPredictedValue;
import gov.epa.QSAR.build.kNN.kNNOptimalResults;
import gov.epa.QSAR.build.stats.StatsBinary;
import gov.epa.QSAR.build.stats.StatsContinuous;
import gov.epa.QSAR.utilities.InstanceUtilitiesWeka;
import gov.epa.QSAR.utilities.Utilities;


public class EvaluateApplicabilityDomain {

	public static final String simNN_All_descriptors_fixed_cutoff="simNN_All_descriptors_fixed_cutoff";
	public static final String simNN_All_descriptors_frac="simNN_All_descriptors_frac";
	public static final String simkNN_Best_Model="simkNN_BestModel";
	
	static class Result {
		double accuracy;
		double coverage;
		double product;

		public String toString() {
			return Utilities.formatNicely(accuracy)+"\t"+Utilities.formatNicely(coverage)+"\t"+Utilities.formatNicely(product);
		}
	}
	
	
	static Result runDataSet(String filePathJSON,String AD,String simMeasure, String descriptorSoftware,double fracPredTraining,String dbPath) {
		
		PredictionReportData data=PredictionReportData.loadDataSetFromJson(filePathJSON);
		
		weka.core.Instances instancesTraining = PredictionReportData.createWekaCoreInstances(data, data.predictionReportMetadata.descriptorSetHeader,0);
		weka.core.Instances instancesPrediction = PredictionReportData.createWekaCoreInstances(data, data.predictionReportMetadata.descriptorSetHeader, 1);
		
		double [] mean=InstanceUtilitiesWeka.CalculateMeans(instancesTraining);
        double [] stdev=InstanceUtilitiesWeka.CalculateStdDevs(instancesTraining);
        double [] offsets=new double[mean.length-2];
        double [] scales=new double[mean.length-2];	        		
		
        for (int i=instancesTraining.classIndex()+1;i<instancesTraining.numAttributes();i++) {
			//Need to fix vector to remove values for ID and property fields (wekalite doesnt need to do this):
			offsets[i-2]=mean[i];
			scales[i-2]=stdev[i];
		}
		
//		wekalite.Instances instancesTrainingWekalite = PredictionReportData.createWekaliteInstances(data, data.predictionReportMetadata.descriptorSetHeader,0);
//		wekalite.Instances instancesPredictionWekalite = PredictionReportData.createWekaliteInstances(data, data.predictionReportMetadata.descriptorSetHeader, 1);
		
		RecordsPredictedValue preds=new RecordsPredictedValue();
		
		boolean isBinary=true;
		double Yexpbar=0;
		double count=0;
		
		kNNOptimalResults orBest=null;
		double avgSCTrainingForFracPred=0;
		
		if (AD.equals(simkNN_Best_Model)) {
			String splitting="T.E.S.T. rnd";
			String endpoint=data.predictionReportMetadata.datasetName.replace(" TEST", "");
//			System.out.println(endpoint);
			orBest=AD_simNN_knn_best_model.getBestkNN_Model(endpoint,splitting,descriptorSoftware,instancesTraining,dbPath);
			
//			Gson gson = new GsonBuilder().setPrettyPrinting().create();
//	        String json = gson.toJson(orBest);
//	        System.out.println(json);
			
			avgSCTrainingForFracPred=AD_simNN_knn_best_model.calculateAvgSC_trainingSet(orBest, instancesTraining, simMeasure,fracPredTraining,offsets,scales);
//			System.out.println("avgSCTrainingForFracPred="+avgSCTrainingForFracPred);
		
		} else if (AD.equals(simNN_All_descriptors_frac)) {
			orBest=new kNNOptimalResults();
			orBest.setNumNeighbors(AD_simNN_All_descriptors.countNeighbors);			
			int [] selAttr=new int [instancesTraining.numAttributes()-2];//based on weka core, need to remove attributes for ID and property value

			for (int i=instancesTraining.classIndex()+1;i<instancesTraining.numAttributes();i++) {
				//Use all descriptors:
				selAttr[i-2]=i;
			}
			orBest.setDescriptors(selAttr);
		
			avgSCTrainingForFracPred=AD_simNN_knn_best_model.calculateAvgSC_trainingSet(orBest, instancesTraining, simMeasure,fracPredTraining,offsets,scales);
//			System.out.println("avgSCTrainingForFracPred="+avgSCTrainingForFracPred);
		}
		
		for (PredictionReportData.PredictionReportDataPoint dataPoint:data.predictionReportDataPoints) {
			int splitNumCurrent=dataPoint.qsarPredictedValues.get(0).splitNum;
			
			if (splitNumCurrent!=1) continue;//not in test set so skip
			if (dataPoint.canonQsarSmiles==null || dataPoint.descriptorValues==null) continue;//not a good data point so skip			
			if (dataPoint.experimentalPropertyValue==null) continue;//shouldnt happen
			
			RecordPredictedValue pred=new RecordPredictedValue();
			preds.add(pred);

			pred.canonQsarSmiles=dataPoint.canonQsarSmiles;			
//			System.out.println(pred.canonQsarSmiles);
			
			if (dataPoint.originalCompounds!=null && dataPoint.originalCompounds.size()>0)			
				pred.dtxcid=dataPoint.originalCompounds.get(0).dtxcid;//use first one

			pred.exp=dataPoint.experimentalPropertyValue;
			pred.pred=PredictionReportData.calculateConsensusToxicity(dataPoint.qsarPredictedValues);

//			Instance testInstance=instancesPrediction.instance(pred.canonQsarSmiles);	
			weka.core.Instance testInstance=InstanceUtilitiesWeka.getInstance(instancesPrediction, pred.canonQsarSmiles);
//			wekalite.Instance testInstanceWekalite=instancesPredictionWekalite.instance(pred.canonQsarSmiles);
			
			if (AD.equals(simNN_All_descriptors_fixed_cutoff) ) {//AD = whether average similarity of k neighbors exceeds a cutoff (all descriptors used to define similarity)

				double cutoff=0;				
				if (simMeasure.equals(AnalogFinder.typeSimilarityMeasureCosineSimilarityCoefficient)) {
					cutoff=0.5;
				} else if (simMeasure.equals(AnalogFinder.typeSimilarityMeasureTanimotoCoefficient)) {
					cutoff=0.2;
				}				
				pred.AD=AD_simNN_All_descriptors.calculateAD_Fixed_Cutoff_weka_core(testInstance, instancesTraining,offsets,scales,simMeasure,cutoff);
//				pred.AD=AD_simNN_All_descriptors.calculateAD_Fixed_Cutoff_wekalite(testInstanceWekalite, instancesTrainingWekalite,simMeasure);
			} else if (AD.equals(simNN_All_descriptors_frac)) {
				pred.AD=AD_simNN_All_descriptors.calculateAD_frac_weka_core(testInstance, instancesTraining,offsets,scales,simMeasure,avgSCTrainingForFracPred);
			} else if (AD.equals(simkNN_Best_Model)) {
				pred.AD=AD_simNN_knn_best_model.calculateAD(orBest, testInstance, instancesTraining, simMeasure, avgSCTrainingForFracPred,offsets,scales);
			}
			
			if (pred.exp!=0 && pred.exp!=1) isBinary=false;
			Yexpbar+=pred.exp;			
			count++;
		}
		Yexpbar/=(double)count;
						
		
		Result result=new Result(); 
		
		if (isBinary) {
			StatsBinary sc=StatsBinary.CalculateBinaryStats(preds);
			result.accuracy=sc.balancedAccuracy;
			result.coverage=sc.coverage;
		} else {
			StatsContinuous sc=StatsContinuous.CalculateContinuousStats(preds,Yexpbar);
			result.accuracy=sc.q2;
			result.coverage=sc.coverage;
		}
		
		result.product=result.accuracy*result.coverage;
		
		System.out.println(data.predictionReportMetadata.datasetName+"\t"+result);
		
		return result;
	}
	
	public static void runDataSetsInFolder(String folderPath,String AD,String simMeasure,double fracPredTraining,String descriptorSoftware,String dbPath) {
		File folder=new File(folderPath);
		
		File [] files=folder.listFiles();
		
		Vector<Result>results=new Vector<>();
		
		for (File file:files) {
			if (!file.getName().contains(".json")) continue;	
			
			if (file.length()>10000000) continue;
			
//			System.out.println(file.length());
			results.add(runDataSet(file.getAbsolutePath(),AD,simMeasure,descriptorSoftware,fracPredTraining,dbPath));			
		}
		
		double avgProduct=0;
		for (Result result:results) {
			avgProduct+=result.product;
		}
		avgProduct/=(double)(results.size());
		System.out.println("average product\t"+Utilities.formatNicely(avgProduct));
	}
	
	public static void main(String[] args) {
//		String AD=simNN_All_descriptors_fixed_cutoff;
		String AD=simNN_All_descriptors_frac;
//		String AD=simkNN_Best_Model;
		
//		String simMeasure=AnalogFinder.typeSimilarityMeasureCosineSimilarityCoefficient;
		String simMeasure=AnalogFinder.typeSimilarityMeasureTanimotoCoefficient;
//		String simMeasure=AnalogFinder.typeSimilarityMeasureEuclideanDistance;
		
		double fracPredTraining=0.95;
		System.out.println("fracPredTraining\t"+fracPredTraining);
		
		String dbPath="databases/TEST_Toxicity_12_28_21.db";

		String reportFolderPath="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\QSAR_Model_Building\\reports\\";
		
		String reportFileName="LC50 TEST_T.E.S.T. 5.1_PredictionReport.json";
//		String reportFileName="LLNA TEST_T.E.S.T. 5.1_PredictionReport.json";
		
		
		String descriptorSoftware="T.E.S.T. 5.1";
//		runDataSet(reportFolderPath+reportFileName, AD,simMeasure,descriptorSoftware,fracPredTraining,dbPath);

		String jsonFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\QSAR_Model_Building\\reports";
		runDataSetsInFolder(jsonFolder, AD,simMeasure,fracPredTraining,descriptorSoftware,dbPath);
		
	}

}
