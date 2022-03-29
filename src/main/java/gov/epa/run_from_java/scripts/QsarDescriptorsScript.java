package gov.epa.run_from_java.scripts;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.datasets.descriptor_values.DescriptorValuesCalculator;
import gov.epa.endpoints.datasets.descriptor_values.SciDataExpertsDescriptorValuesCalculator;
import gov.epa.endpoints.datasets.descriptor_values.TestDescriptorValuesCalculator;

public class QsarDescriptorsScript {
	
	private static final String ML_SCI_DATA_EXPERTS_URL = "https://ml.sciencedataexperts.com";
	private static final String HCD_SCI_DATA_EXPERTS_URL = "https://hazard-dev.sciencedataexperts.com";
	
	public static String calculateDescriptorsForDataset(String datasetName, String descriptorSetName, String descriptorWebServiceUrl, String lanId,boolean writeToDatabase) {
		DescriptorValuesCalculator calc = null;
		if (descriptorSetName.equals(DevQsarConstants.DESCRIPTOR_SET_TEST)) {
			calc = new TestDescriptorValuesCalculator(descriptorWebServiceUrl, lanId);
		} else {
			calc = new SciDataExpertsDescriptorValuesCalculator(descriptorWebServiceUrl, lanId);
		}
		
		return calc.calculateDescriptors(datasetName, descriptorSetName,writeToDatabase);
	}
	
	public static void main(String[] args) {
//		String testDescriptorWebServiceUrl = DevQsarConstants.SERVER_819 + ":" + DevQsarConstants.PORT_TEST_DESCRIPTORS;
//		String tsv=calculateDescriptorsForDataset("LogHalfLife OPERA", DevQsarConstants.DESCRIPTOR_SET_TEST, testDescriptorWebServiceUrl, "gsincl01",false);
//		System.out.println(tsv);
		

		String testDescriptorWebServiceUrl = ML_SCI_DATA_EXPERTS_URL;	
		String set="PaDEL-default";
//		String set="WebTEST-default";
		String tsv=calculateDescriptorsForDataset("LogHalfLife OPERA", set, testDescriptorWebServiceUrl, "tmarti02",false);
		System.out.println(tsv);
	}

}
