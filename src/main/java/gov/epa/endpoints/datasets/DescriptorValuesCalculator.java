package gov.epa.endpoints.datasets;

import java.util.List;

import javax.validation.ConstraintViolationException;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesService;
import gov.epa.web_services.descriptors.TestDescriptorWebService;
import gov.epa.web_services.descriptors.TestDescriptorWebService.TestDescriptorCalculationResponse;
import gov.epa.web_services.descriptors.TestDescriptorWebService.TestDescriptorInfoResponse;

public class DescriptorValuesCalculator {
	
	private DescriptorSetService descriptorSetService;
	private DescriptorValuesService descriptorValuesService;
	
	private TestDescriptorWebService descriptorWebService;
	
	private String lanId;

	private void calculateDescriptors(List<DataPoint> dataPoints) {
		TestDescriptorInfoResponse descriptorInfoResponse = descriptorWebService.callInfo().getBody();
		String descriptorSetName = descriptorInfoResponse.name + " " + descriptorInfoResponse.version;
		
		DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);
		if (descriptorSet==null) {
			descriptorSet = new DescriptorSet(descriptorSetName, 
					descriptorInfoResponse.description, 
					descriptorInfoResponse.headersTsv,
					lanId);
			descriptorSetService.create(descriptorSet);
		}
		
		for (DataPoint dp:dataPoints) {
			String canonQsarSmiles = dp.getCanonQsarSmiles();
			if (dp.getCanonQsarSmiles()==null) {
				// Don't calculate descriptors on compounds without standardization
//				logger.info(mpv.id + ": Skipped descriptor calculation due to null standardization");
				continue;
			} 
			
			DescriptorValues descriptorValues = descriptorValuesService
					.findByCanonQsarSmilesAndDescriptorSetName(canonQsarSmiles, descriptorSet.getName());
			if (descriptorValues!=null) {
				// Should this store descriptors for calculation later? Probably not
				// Do nothing
//					logger.trace(mpv.id + ": Found existing descriptor values");
			} else {
				// Calculate descriptors
				TestDescriptorCalculationResponse descriptorCalculationResponse = 
						descriptorWebService.callCalculation(canonQsarSmiles).getBody();
				
				// Store descriptors
				// Again, store null or failed descriptors so we don't keep trying to calculate them every time
				if (descriptorCalculationResponse!=null) {
					String valuesTsv = descriptorCalculationResponse.valuesTsv;
					// If descriptor calculation failed, set null so we can check easily when we try to use them later
					if (valuesTsv.contains("Error")) {
//							logger.info(mpv.id + ": Error calculating descriptors: " + valuesTsv);
						valuesTsv = null;
					} else {
//							logger.debug(mpv.id + ": Calculated descriptors: " + valuesTsv.substring(0,255));
					}
					
					descriptorValues = new DescriptorValues(canonQsarSmiles, descriptorSet, valuesTsv, lanId);
					
					try {
						descriptorValuesService.create(descriptorValues);
					} catch (ConstraintViolationException e) {
						System.out.println(e.getMessage());
					}
				}
			}
		}
	}
}
