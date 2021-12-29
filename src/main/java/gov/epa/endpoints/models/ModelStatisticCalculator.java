package gov.epa.endpoints.models;

import java.util.HashMap;
import java.util.List;

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
	public static HashMap<String, Double> calculateBinaryStatistics(List<ModelPrediction> modelPredictions, Double cutoff) {
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
		modelStatisticValues.put(DevQsarConstants.COVERAGE, coverage);
		modelStatisticValues.put(DevQsarConstants.CONCORDANCE, concordance);
		modelStatisticValues.put(DevQsarConstants.SENSITIVITY, sensitivity);
		modelStatisticValues.put(DevQsarConstants.SPECIFICITY, specificity);
		modelStatisticValues.put(DevQsarConstants.BALANCED_ACCURACY, balancedAccuracy);
		
		return modelStatisticValues;
	}
	
	/**
	 * Calculates a basic set of continuous model statistics
	 * @param modelPredictions	a list of tuples of experimental and predicted values
	 * @param meanExpTraining	the average experimental value in the training set
	 * @return					a map of statistic names to calculated values
	 */
	public static HashMap<String, Double> calculateContinuousStatistics(List<ModelPrediction> modelPredictions, Double meanExpTraining) {
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
			
			// Update terms for R2
			termXY += (mp.exp - meanExp) * (mp.pred - meanPred);
			termXX += (mp.exp - meanExp) * (mp.exp - meanExp);
			termYY += (mp.pred - meanPred) * (mp.pred - meanPred);
			
			// Update sums for Q2
			ss += Math.pow(mp.exp - mp.pred, 2.0);
			ssTotal += Math.pow(mp.exp - meanExpTraining, 2.0);
		}
		
		Double coverage = (double) countPredicted / (double) countTotal;
		mae /= (double) countPredicted;
		Double r2 = termXY * termXY / (termXX * termYY);
		Double q2 = 1 - ss / ssTotal;
		Double rmse = Math.sqrt(ss / (double) countPredicted);
		
		HashMap<String, Double> modelStatisticValues = new HashMap<String, Double>();
		modelStatisticValues.put(DevQsarConstants.COVERAGE, coverage);
		modelStatisticValues.put(DevQsarConstants.MAE, mae);
		modelStatisticValues.put(DevQsarConstants.R2, r2);
		modelStatisticValues.put(DevQsarConstants.Q2, q2);
		modelStatisticValues.put(DevQsarConstants.RMSE, rmse);
		
		return modelStatisticValues;
	}

}
