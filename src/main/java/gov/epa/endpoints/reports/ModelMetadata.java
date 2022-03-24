package gov.epa.endpoints.reports;

public class ModelMetadata {
	public Long modelId;
	public String qsarMethodName;
	public String qsarMethodDescription;
	public String descriptorSetName;
	
	public ModelMetadata(Long modelId, String qsarMethodName, String qsarMethodDescription, String descriptorSetName) {
		this.modelId = modelId;
		this.qsarMethodName = qsarMethodName;
		this.qsarMethodDescription = qsarMethodDescription;
		this.descriptorSetName = descriptorSetName;
	}

}
