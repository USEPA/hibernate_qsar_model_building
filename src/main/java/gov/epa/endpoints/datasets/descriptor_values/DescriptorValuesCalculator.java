package gov.epa.endpoints.datasets.descriptor_values;

import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesServiceImpl;

public class DescriptorValuesCalculator {
	
	protected DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();
	protected DescriptorValuesService descriptorValuesService = new DescriptorValuesServiceImpl();
	protected DataPointService dataPointService = new DataPointServiceImpl();
	
	protected String lanId;
	
	public DescriptorValuesCalculator(String lanId) {
		this.lanId = lanId;
	}
	
	public void calculateDescriptors(String datasetName, String descriptorSetName) {
		System.out.println("Override calculateDescriptors()!");
	}

}
