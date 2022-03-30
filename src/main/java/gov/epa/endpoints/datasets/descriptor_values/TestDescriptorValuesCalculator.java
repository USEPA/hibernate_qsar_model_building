package gov.epa.endpoints.datasets.descriptor_values;

import java.util.List;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.web_services.descriptors.TestDescriptorWebService;
import gov.epa.web_services.descriptors.TestDescriptorWebService.TestDescriptorCalculationResponse;

public class TestDescriptorValuesCalculator extends DescriptorValuesCalculator {

	private TestDescriptorWebService descriptorWebService;
	
	public TestDescriptorValuesCalculator(String testDescriptorUrl, String lanId) {
		super(lanId);
		this.descriptorWebService = new TestDescriptorWebService(testDescriptorUrl);
	}
	
	@Override
	public String calculateDescriptors(String datasetName, String descriptorSetName, boolean writeToDatabase) {
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);
		DescriptorSet descriptorSet = descriptorSetService.findByName(DevQsarConstants.DESCRIPTOR_SET_TEST);
		StringBuilder sb = new StringBuilder(START_HEADER + descriptorSet.getHeadersTsv() + LINE_BREAK);
		for (DataPoint dp:dataPoints) {
			String canonQsarSmiles = dp.getCanonQsarSmiles();
			if (dp.getCanonQsarSmiles()==null) {
				// Don't calculate descriptors on compounds without standardization
				continue;
			} 
			
			String valuesTsv = null;
			DescriptorValues descriptorValues = descriptorValuesService
					.findByCanonQsarSmilesAndDescriptorSetName(canonQsarSmiles, descriptorSetName);
			if (descriptorValues==null) {
				TestDescriptorCalculationResponse response = descriptorWebService.callCalculation(canonQsarSmiles).getBody();
				valuesTsv = response.valuesTsv;
				// If descriptor calculation failed, set null so we can check easily when we try to use them later
				if (valuesTsv.contains("Error")) {
					valuesTsv = null;
				}
				
				if (writeToDatabase) {
					writeDescriptorValuesToDatabase(canonQsarSmiles, descriptorSet, valuesTsv);
				}
			} else {
				valuesTsv = descriptorValues.getValuesTsv();
			}
			
			String instance = generateInstance(dp, valuesTsv);
			if (instance!=null) {
				sb.append(instance);
			}
		}
		
		return sb.toString();
	}
}
