package gov.epa.endpoints.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import gov.epa.databases.dev_qsar.DevQsarConstants;

/**
 * Class to calculate model statistics
 * @author GSINCL01
 *
 */
public class ModelStatisticCalculator {
	
	
	public static Double calcMeanExpTraining(List<ModelPrediction>trainMP) {
		
		double meanExpTraining = 0.0;
		int count = 0;
		
		for (ModelPrediction mp:trainMP) {
			if (mp.exp!=null) {
				meanExpTraining += mp.exp;
				count++;
			}
		}
		meanExpTraining /= count;
		return meanExpTraining;
	}
	
	public static Double calcAvgSumSqError(List<ModelPrediction>testMP) {
		Double ASSE = 0.0;
		int count=0;
		for (ModelPrediction mp:testMP) {	
			if (mp.exp!=null && mp.pred!=null) {
				ASSE+=Math.pow((mp.exp-mp.pred),2);
				count++;
			}
		}
		ASSE /= count;
		return ASSE;
	}

	public static double calcAvgYminusYbar(List<ModelPrediction> trainMP,double Ybar_training) {				
		double AvgYminusYbar=0;
		int count=0;
		for (ModelPrediction mp:trainMP) {
			if (mp.exp!=null) {
				AvgYminusYbar+=Math.pow((mp.exp-Ybar_training),2);
				count++;
			} 
		}
		AvgYminusYbar/=count;
		return AvgYminusYbar;				
	}
	
	/**
	 * Calculates Q2
	 * 
	 * @param trainMP
	 * @param testMP
	 * @return
	 */
	public static double calculateQ2(List<ModelPrediction> trainMP, List<ModelPrediction> testMP) {		
		double YbarTrain=calcMeanExpTraining(trainMP);
		double numerator= calcAvgSumSqError(testMP);
		double denominator = calcAvgYminusYbar(trainMP,YbarTrain);		
		double q2=1-numerator/denominator;
		return q2;
	}
	

	/**
	 * 
	 * Calculates Q2_F3 see eqn 2 of Consonni et al, 2019 (https://onlinelibrary.wiley.com/doi/full/10.1002/minf.201800029)
	 * 
	 * @param modelId
	 * @return
	 */
	public static double calculateQ2_CV(List<ModelPrediction>modelPredictions) {
		
		Hashtable<Integer, List<ModelPrediction>> htMP = createModelPredictionHashtable(modelPredictions);
		
		//Now calculate Q2 ext:
		
		double q2=0;
		
		for (int i=1;i<=htMP.keySet().size();i++) {
			List<ModelPrediction>trainMP=new ArrayList<>();
			List<ModelPrediction>testMP=new ArrayList<>();
			
			for (int j=1;j<=5;j++) {
				
				if(i==j) {
					testMP.addAll(htMP.get(j));
				} else {
					trainMP.addAll(htMP.get(j));
				}
			}
//			System.out.println(trainMP.size()+"\t"+testMP.size());
			
			double q2i = ModelStatisticCalculator.calculateQ2(trainMP, testMP);
		    System.out.println(i+"\t"+q2i);
		    q2 = q2 + q2i;
		}
		
		q2/=htMP.keySet().size();
		System.out.println("q2="+q2+"\n");
		
		return q2;
		
		
	}
	
	/**
	 * 
	 * Calculates Q2_F3 see eqn 2 of Consonni et al, 2019 (https://onlinelibrary.wiley.com/doi/full/10.1002/minf.201800029)
	 * 
	 * @param modelId
	 * @return
	 */
	public static double calculateR2_CV(List<ModelPrediction>modelPredictions) {
		
		Hashtable<Integer, List<ModelPrediction>> htMP = createModelPredictionHashtable(modelPredictions);
		
		//Now calculate Q2 ext:
		
		double r2=0;
		
		for (int i=1;i<=htMP.keySet().size();i++) {
			List<ModelPrediction>trainMP=new ArrayList<>();
			List<ModelPrediction>testMP=new ArrayList<>();
			
			for (int j=1;j<=5;j++) {
				
				if(i==j) {
					testMP.addAll(htMP.get(j));
				} else {
					trainMP.addAll(htMP.get(j));
				}
			}
//			System.out.println(trainMP.size()+"\t"+testMP.size());
			
			double YbarTrain=calcMeanExpTraining(trainMP);
			
			Map<String, Double>mapStats=calculateContinuousStatistics(trainMP, YbarTrain, DevQsarConstants.TAG_TEST);
			
			double r2i=mapStats.get(DevQsarConstants.PEARSON_RSQ + DevQsarConstants.TAG_TEST);
						
		    System.out.println(i+"\t"+r2i);
		    r2 = r2 + r2i;
		}
		
		r2/=htMP.keySet().size();
		System.out.println("r2_cv="+r2+"\n");
		
		return r2;
		
		
	}
	
	

	private static Hashtable<Integer, List<ModelPrediction>> createModelPredictionHashtable(List<ModelPrediction> mpsAll) {
		Hashtable<Integer,List<ModelPrediction>>htMP=new Hashtable<>();
		
		for (ModelPrediction mp:mpsAll) {
			
			if(htMP.get(mp.split)==null) {
				List<ModelPrediction>mps=new ArrayList<>();
				htMP.put(mp.split, mps);				
				mps.add(mp);
			} else {
				List<ModelPrediction>mps=htMP.get(mp.split);
				mps.add(mp);
			}
		}
		return htMP;
	}

	
	/**
	 * Calculates a basic set of binary model statistics
	 * @param modelPredictions	a list of tuples of experimental and predicted values
	 * @param cutoff			the cutoff to consider a non-binary value as positive or negative
	 * @return					a map of statistic names to calculated values
	 */
	public static Map<String, Double> calculateBinaryStatistics(List<ModelPrediction> modelPredictions, Double cutoff, String tag) {
		int countTotal = 0;
		int countPredicted = 0;
		int countPositive = 0;
		int countNegative = 0;
		Double countTrue = 0.0;
		Double countTruePositive = 0.0;
		Double countTrueNegative = 0.0;
		for (ModelPrediction mp:modelPredictions) {
			if (mp.exp!=null) {
				countTotal++;
			} else {
				continue;
			}
			
			if (mp.pred!=null) {
				countPredicted++;
				int predBinary = mp.pred >= cutoff ? 1 : 0;
				if (mp.exp==1) {
					countPositive++;
					if (predBinary==1) {
						countTrue++;
						countTruePositive++;
					}
				} else if (mp.exp==0) {
					countNegative++;
					if (predBinary==0) {
						countTrue++;
						countTrueNegative++;
					}
				}
			}
		}
		
		Double coverage = (double) countPredicted / (double) countTotal;
		Double concordance = countTrue /= (double) countPredicted;
		Double sensitivity = countTruePositive /= (double) countPositive;
		Double specificity  = countTrueNegative /= (double) countNegative;
		Double balancedAccuracy = (sensitivity + specificity) / 2.0;
		
		HashMap<String, Double> modelStatisticValues = new HashMap<String, Double>();
		modelStatisticValues.put(DevQsarConstants.COVERAGE + tag, coverage);
		modelStatisticValues.put(DevQsarConstants.CONCORDANCE + tag, concordance);
		modelStatisticValues.put(DevQsarConstants.SENSITIVITY + tag, sensitivity);
		modelStatisticValues.put(DevQsarConstants.SPECIFICITY + tag, specificity);
		modelStatisticValues.put(DevQsarConstants.BALANCED_ACCURACY + tag, balancedAccuracy);
		
		return modelStatisticValues;
	}
	
	
	 
	
	/**
	 * Calculates a basic set of continuous model statistics
	 * 
	 * @param modelPredictions	a list of tuples of experimental and predicted values
	 * @param meanExpTraining	the average experimental value in the training set
	 * @return					a map of statistic names to calculated values
	 */
	public static Map<String, Double> calculateContinuousStatistics(List<ModelPrediction> modelPredictions, Double meanExpTraining, 
			String tag) {
		// Loop once to get counts and means
		int countTotal = 0;
		int countPredicted = 0;
		Double meanExp = 0.0;
		Double meanPred = 0.0;
		for (ModelPrediction mp:modelPredictions) {
			
//			System.out.println(mp.id+"\t"+mp.exp+"\t"+mp.pred);
			
			if (mp.exp!=null) {
				countTotal++;
			} else {
				continue;
			}
			
			if (mp.pred!=null) {
				countPredicted++;
				meanExp += mp.exp;
				meanPred += mp.pred;
			}
		}
		meanExp /= (double) countPredicted;
		meanPred /= (double) countPredicted;
		
		// Loop again to calculate stats
		Double mae = 0.0;
		Double termXY = 0.0;
		Double termXX = 0.0;
		Double termYY = 0.0;
		Double ss = 0.0;
		Double ssTotal = 0.0;
		for (ModelPrediction mp:modelPredictions) {
			if (mp.exp==null || mp.pred==null) { 
				continue;
			}
			
			// Update MAE
			mae += Math.abs(mp.exp - mp.pred);
			
			// Update terms for Person RSQ
			termXY += (mp.exp - meanExp) * (mp.pred - meanPred);
			termXX += (mp.exp - meanExp) * (mp.exp - meanExp);
			termYY += (mp.pred - meanPred) * (mp.pred - meanPred);
			
			// Update sums for coefficient of determination
			ss += Math.pow(mp.exp - mp.pred, 2.0);
			ssTotal += Math.pow(mp.exp - meanExpTraining, 2.0);
		}
		
		Double coverage = (double) countPredicted / (double) countTotal;
		mae /= (double) countPredicted;
		Double pearsonRsq = termXY * termXY / (termXX * termYY);
		Double coeffDet = 1 - ss / ssTotal;
		Double rmse = Math.sqrt(ss / (double) countPredicted);
		
		HashMap<String, Double> modelStatisticValues = new HashMap<String, Double>();
		modelStatisticValues.put(DevQsarConstants.COVERAGE + tag, coverage);
		modelStatisticValues.put(DevQsarConstants.MAE + tag, mae);
		modelStatisticValues.put(DevQsarConstants.PEARSON_RSQ + tag, pearsonRsq);
		modelStatisticValues.put(DevQsarConstants.RMSE + tag, rmse);
		
		if (tag.equals(DevQsarConstants.TAG_TEST)) {
			modelStatisticValues.put(DevQsarConstants.Q2_TEST, coeffDet);
		} else if (tag.equals(DevQsarConstants.TAG_TRAINING)) {
			modelStatisticValues.put(DevQsarConstants.R2_TRAINING, coeffDet);
		}
			
		
		return modelStatisticValues;
	}

}
