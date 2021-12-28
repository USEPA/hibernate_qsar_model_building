package gov.epa.builders.model;

/**
 * Class defining the parameters to be used for a particular model
 * @author GSINCL01
 *
 */
public class ModelParams {
	String qsarMethod;
	
	// Set when model is built and added to database
	// Same params may be reused/rebuilt and this will update accordingly
	String wsModelId;
	Long dbModelId;
	
	public ModelParams(String qsarMethod) {
		this.qsarMethod = qsarMethod;
	}
}
