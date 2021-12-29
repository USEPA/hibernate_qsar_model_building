package gov.epa.endpoints.models;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;

/**
 * Class defining the data to be used for a particular model
 * @author GSINCL01
 *
 */
public class ModelData {
	public String datasetName;
	public String descriptorSetName;
	public String splittingName;
	boolean removeLogDescriptors;
	
	// Set by querying from qsar_datasets and qsar_descriptors using ModelBuilder.initInstances()
	String trainingSetInstances;
	String predictionSetInstances;
	Double meanExpTraining;
	
	public ModelData(String datasetName, String descriptorSetName, String splittingName, boolean removeLogDescriptors) {
		this.datasetName = datasetName;
		this.descriptorSetName = descriptorSetName;
		this.splittingName = splittingName;
		this.removeLogDescriptors = removeLogDescriptors;
	}
	
	public void initInstances(List<DataPointInSplitting> dataPointsInSplitting, List<DescriptorValues> descriptorValues) {
		Map<String, DataPointInSplitting> dataPointsInSplittingMap = 
				dataPointsInSplitting.stream().collect(Collectors.toMap(dpis -> dpis.getDataPoint().getCanonQsarSmiles(), dp -> dp));
		Map<String, DescriptorValues> descriptorValuesMap = descriptorValues.stream()
				.collect(Collectors.toMap(dv -> dv.getCanonQsarSmiles(), dv -> dv));
		
		DescriptorSet descriptorSet = descriptorValues.iterator().next().getDescriptorSet();
		String instanceHeader = "ID\tProperty\t" + descriptorSet.getHeadersTsv() + "\r\n";
		StringBuilder sbOverall = new StringBuilder(instanceHeader);
		StringBuilder sbTraining = new StringBuilder(instanceHeader);
		StringBuilder sbPrediction = new StringBuilder(instanceHeader);
		int countTraining = 0;
		this.meanExpTraining = 0.0;
		for (String smiles:dataPointsInSplittingMap.keySet()) {
			DataPointInSplitting dpis = dataPointsInSplittingMap.get(smiles);
			DataPoint dp = dpis.getDataPoint();
			DescriptorValues dv = descriptorValuesMap.get(smiles);
			
			if (dp!=null && dv!=null && !dp.getOutlier()) {
				String instance = generateInstance(smiles, dp, dv);
				if (instance!=null) {
					sbOverall.append(instance);
					Integer splitNum = dpis.getSplitNum();
					if (splitNum==DevQsarConstants.TRAIN_SPLIT_NUM) {
						sbTraining.append(instance);
						this.meanExpTraining += dp.getQsarPropertyValue();
						countTraining++;
					} else if (splitNum==DevQsarConstants.PREDICT_SPLIT_NUM) {
						sbPrediction.append(instance);
					}
				}
			}
		}
		
		this.trainingSetInstances = sbTraining.toString();
		this.predictionSetInstances = sbPrediction.toString();
		this.meanExpTraining /= (double) countTraining;
	}
	
	public static String generateInstancesWithoutSplitting(List<DataPoint> dataPoints, List<DescriptorValues> descriptorValues) {
		Map<String, DataPoint> dataPointsMap = dataPoints.stream().collect(Collectors.toMap(dp -> dp.getCanonQsarSmiles(), dp -> dp));
		Map<String, DescriptorValues> descriptorValuesMap = descriptorValues.stream()
				.collect(Collectors.toMap(dv -> dv.getCanonQsarSmiles(), dv -> dv));
		
		DescriptorSet descriptorSet = descriptorValues.iterator().next().getDescriptorSet();
		String instanceHeader = "ID\tProperty\t" + descriptorSet.getHeadersTsv() + "\r\n";
		StringBuilder sbOverall = new StringBuilder(instanceHeader);
		for (String smiles:dataPointsMap.keySet()) {
			DataPoint dp = dataPointsMap.get(smiles);
			DescriptorValues dv = descriptorValuesMap.get(smiles);
			
			if (dp!=null && dv!=null && !dp.getOutlier()) {
				String instance = generateInstance(smiles, dp, dv);
				if (instance!=null) {
					sbOverall.append(instance);
				}
			}
		}
		
		return sbOverall.toString();
	}
	
	private static String generateInstance(String smiles, DataPoint dataPoint, DescriptorValues descriptorValues) {
		String valuesTsv = descriptorValues.getValuesTsv();
		String qsarPropertyValue = String.valueOf(dataPoint.getQsarPropertyValue());
		
		if (valuesTsv==null) {
			return null;
		}
		
		if (valuesTsv.toLowerCase().contains("infinity")) {
			return null;
		}
		
		if (valuesTsv.toLowerCase().contains("error")) {
			return null;
		}
		
		return smiles + "\t" + qsarPropertyValue + "\t" + valuesTsv + "\r\n";
	}
}
