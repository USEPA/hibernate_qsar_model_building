package gov.epa.endpoints.models;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundServiceImpl;

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
	 public String trainingSetInstances;
	 public String predictionSetInstances;
	
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
					} else if (splitNum==DevQsarConstants.TEST_SPLIT_NUM) {
						sbPrediction.append(instance);
					}
				}
			}
		}
		
		this.trainingSetInstances = sbTraining.toString();
		this.predictionSetInstances = sbPrediction.toString();
	}
	
	public static String generateInstancesWithoutSplitting(List<DataPoint> dataPoints, List<DescriptorValues> descriptorValues,
			boolean fetchDtxcids) {
		Map<String, DataPoint> dataPointsMap = dataPoints.stream().collect(Collectors.toMap(dp -> dp.getCanonQsarSmiles(), dp -> dp));
		Map<String, DescriptorValues> descriptorValuesMap = descriptorValues.stream()
				.collect(Collectors.toMap(dv -> dv.getCanonQsarSmiles(), dv -> dv));
		
		CompoundService compoundService = new CompoundServiceImpl();
		
		DescriptorSet descriptorSet = descriptorValues.iterator().next().getDescriptorSet();
		String instanceHeader = "ID\tProperty\t" + descriptorSet.getHeadersTsv() + "\r\n";
		StringBuilder sbOverall = new StringBuilder(instanceHeader);
		for (String smiles:dataPointsMap.keySet()) {
			DataPoint dp = dataPointsMap.get(smiles);
			DescriptorValues dv = descriptorValuesMap.get(smiles);
			
			if (dp!=null && dv!=null && !dp.getOutlier()) {
				String id = smiles;
				if (fetchDtxcids) {
					List<Compound> compounds = compoundService.findByCanonQsarSmiles(smiles);
					if (compounds!=null) {
						List<String> dtxcids = compounds.stream()
								.map(c -> c.getDtxcid())
								.distinct()
								.sorted()
								.collect(Collectors.toList());
						if (!dtxcids.isEmpty()) {
							id = dtxcids.get(0);
						}
					}
				}
				
				String instance = generateInstance(id, dp, dv);
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
		
		if (valuesTsv.toLowerCase().contains("∞")) {
			return null;
		}
		
		if (valuesTsv.toLowerCase().contains("error")) {
			return null;
		}
		
		return smiles + "\t" + qsarPropertyValue + "\t" + valuesTsv + "\r\n";
	}
}
