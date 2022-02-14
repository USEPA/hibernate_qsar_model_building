package gov.epa.endpoints.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.epa.databases.dev_qsar.DevQsarConstants;

/**
 * Class to calculate model statistics
 * @author GSINCL01
 *
 */
public class ModelStatisticCalculator {
	
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
