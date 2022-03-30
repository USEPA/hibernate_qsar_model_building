package gov.epa.run_from_java.scripts;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.datasets.descriptor_values.DescriptorValuesCalculator;
import gov.epa.endpoints.datasets.descriptor_values.SciDataExpertsDescriptorValuesCalculator;
import gov.epa.endpoints.datasets.descriptor_values.TestDescriptorValuesCalculator;

public class QsarDescriptorsScript {
	
	private static final String ML_SCI_DATA_EXPERTS_URL = "https://ml.sciencedataexperts.com";
	private static final String HCD_SCI_DATA_EXPERTS_URL = "https://hazard-dev.sciencedataexperts.com";
	
	public static String calculateDescriptorsForDataset(String datasetName, String descriptorSetName, String descriptorWebServiceUrl, 
			boolean writeToDatabase, String lanId) {
		DescriptorValuesCalculator calc = null;
		if (descriptorSetName.equals(DevQsarConstants.DESCRIPTOR_SET_TEST)) {
			calc = new TestDescriptorValuesCalculator(descriptorWebServiceUrl, lanId);
		} else {
			calc = new SciDataExpertsDescriptorValuesCalculator(descriptorWebServiceUrl, lanId);
		}
		
		return calc.calculateDescriptors(datasetName, descriptorSetName, writeToDatabase);
	}
	
	public void deleteDescriptorsForDataset(String datasetName, String descriptorSetName) {
		
	}
	
	public static void main(String[] args) {
		String testDescriptorWebServiceUrl = DevQsarConstants.SERVER_819 + ":" + DevQsarConstants.PORT_TEST_DESCRIPTORS;
		String tsv = calculateDescriptorsForDataset("LogHalfLife OPERA", "RDKit-default", ML_SCI_DATA_EXPERTS_URL, false, "gsincl01");
		System.out.println(tsv.substring(0, tsv.indexOf("\r")));
	}

}
