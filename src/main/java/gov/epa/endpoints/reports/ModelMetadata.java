package gov.epa.endpoints.reports;

public class ModelMetadata {
	public Long modelId;
	public String qsarMethodName;
	public String qsarMethodDescription;
	
	public ModelMetadata(Long modelId, String qsarMethodName, String qsarMethodDescription) {
		this.modelId = modelId;
		this.qsarMethodName = qsarMethodName;
		this.qsarMethodDescription = qsarMethodDescription;
	}

}
