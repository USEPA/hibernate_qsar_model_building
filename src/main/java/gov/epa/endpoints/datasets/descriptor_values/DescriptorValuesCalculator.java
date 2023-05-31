package gov.epa.endpoints.datasets.descriptor_values;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintViolationException;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesServiceImpl;

public class DescriptorValuesCalculator {
	
	protected DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();
	protected DescriptorValuesService descriptorValuesService = new DescriptorValuesServiceImpl();
	protected DataPointService dataPointService = new DataPointServiceImpl();
	
	protected String lanId;
	protected static final String TAB_DEL = "\t";
	protected static final String LINE_BREAK = "\r\n";
	protected static final String START_HEADER = "ID" + TAB_DEL + "Property" + TAB_DEL;
	
	public DescriptorValuesCalculator(String lanId) {
		this.lanId = lanId;
	}
	
	public void deleteDescriptorSetValuesForDataset(String datasetName, String descriptorSetName) {
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);
		for (DataPoint dp:dataPoints) {
			String canonQsarSmiles = dp.getCanonQsarSmiles();
			if (dp.getCanonQsarSmiles()==null) {
				continue;
			} 
			
			DescriptorValues descriptorValues = descriptorValuesService
					.findByCanonQsarSmilesAndDescriptorSetName(canonQsarSmiles, descriptorSetName);
			if (descriptorValues!=null) {
				descriptorValuesService.delete(descriptorValues);
			}
		}
	}
	
	public String calculateDescriptors(String datasetName, String descriptorSetName, boolean writeToDatabase) {
		System.out.println("Override calculateDescriptors()!");
		return null;
	}
	
	protected static String buildTsv(List<DataPoint> dataPoints, Map<String, String> descriptorsMap, DescriptorSet descriptorSet) {
		StringBuilder sb = new StringBuilder(START_HEADER + descriptorSet.getHeadersTsv() + LINE_BREAK);
		for (DataPoint dp:dataPoints) {
			String canonQsarSmiles = dp.getCanonQsarSmiles();
			if (dp.getCanonQsarSmiles()==null) {
				// Don't calculate descriptors on compounds without standardization
				continue;
			}
			
			String valuesTsv = descriptorsMap.get(canonQsarSmiles);
			String instance = generateInstance(dp, valuesTsv);
			if (instance!=null) {
				sb.append(instance);
			}
		}
		
		return sb.toString();
	}
	
	protected static String generateInstance(DataPoint dataPoint, String valuesTsv) {
		String qsarPropertyValue = String.valueOf(dataPoint.getQsarPropertyValue());

		if (valuesTsv==null || valuesTsv.isBlank()) {
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
		
		return dataPoint.getCanonQsarSmiles() + TAB_DEL + qsarPropertyValue + TAB_DEL + valuesTsv + LINE_BREAK;
	}
	
	protected void writeDescriptorValuesToDatabase(String canonQsarSmiles, DescriptorSet descriptorSet, String valuesTsv) {
		DescriptorValues newDescriptorValues = new DescriptorValues(canonQsarSmiles, descriptorSet, valuesTsv, lanId);
		try {
			descriptorValuesService.create(newDescriptorValues);
		} catch (ConstraintViolationException e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	protected void writeDescriptorValuesToDatabase(Map<String, String> descriptorsMap, DescriptorSet descriptorSet,String createdBy) {
		
		List<DescriptorValues>valuesArray=new ArrayList<>();
		
		for (String smiles:descriptorsMap.keySet()) {
			DescriptorValues dv=new DescriptorValues(smiles,descriptorSet,descriptorsMap.get(smiles),createdBy);
			valuesArray.add(dv);
		}
		descriptorValuesService.createSql(valuesArray);
		
	}

}
