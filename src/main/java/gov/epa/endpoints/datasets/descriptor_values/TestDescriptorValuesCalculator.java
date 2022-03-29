package gov.epa.endpoints.datasets.descriptor_values;

import java.util.List;

import javax.validation.ConstraintViolationException;

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
	public void calculateDescriptors(String datasetName, String descriptorSetName) {
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);
		DescriptorSet descriptorSet = descriptorSetService.findByName(DevQsarConstants.DESCRIPTOR_SET_TEST);
		for (DataPoint dp:dataPoints) {
			String canonQsarSmiles = dp.getCanonQsarSmiles();
			if (dp.getCanonQsarSmiles()==null) {
				// Don't calculate descriptors on compounds without standardization
				continue;
			} 
			
			DescriptorValues descriptorValues = descriptorValuesService
					.findByCanonQsarSmilesAndDescriptorSetName(canonQsarSmiles, descriptorSetName);
			if (descriptorValues==null) {
				TestDescriptorCalculationResponse response = descriptorWebService.callCalculation(canonQsarSmiles).getBody();
				
				String valuesTsv = response.valuesTsv;
				// If descriptor calculation failed, set null so we can check easily when we try to use them later
				if (valuesTsv.contains("Error")) {
					valuesTsv = null;
				}
				
				DescriptorValues newDescriptorValues = new DescriptorValues(canonQsarSmiles, descriptorSet, valuesTsv, lanId);
				try {
					descriptorValuesService.create(newDescriptorValues);
				} catch (ConstraintViolationException e) {
					System.out.println(e.getMessage());
				}
			}
		}
	}
}
