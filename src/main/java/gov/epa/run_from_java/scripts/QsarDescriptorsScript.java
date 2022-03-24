package gov.epa.run_from_java.scripts;

import java.util.List;

import gov.epa.endpoints.datasets.SciDataExpertsDescriptorValuesCalculator;

public class QsarDescriptorsScript {
	
	private static final String ML_SCI_DATA_EXPERTS_URL = "https://ml.sciencedataexperts.com";
	private static final String HCD_SCI_DATA_EXPERTS_URL = "https://hazard-dev.sciencedataexperts.com";
	
	public static void calculateDescriptorsTest(List<String> smiles, String descriptorSetName, String sciDataExpertsUrl, String lanId) {
		SciDataExpertsDescriptorValuesCalculator calc = new SciDataExpertsDescriptorValuesCalculator(sciDataExpertsUrl, lanId);
		calc.calculateDescriptors(smiles, descriptorSetName);
	}
	
	public static void calculateDescriptorsForDataset(String datasetName, String descriptorSetName, String sciDataExpertsUrl, String lanId) {
		SciDataExpertsDescriptorValuesCalculator calc = new SciDataExpertsDescriptorValuesCalculator(sciDataExpertsUrl, lanId);
		calc.calculateDescriptors(datasetName, descriptorSetName);
	}
	
	public static void main(String[] args) {
		calculateDescriptorsForDataset("LogHalfLife OPERA", "WebTEST-default", ML_SCI_DATA_EXPERTS_URL, "gsincl01");
	}

}
