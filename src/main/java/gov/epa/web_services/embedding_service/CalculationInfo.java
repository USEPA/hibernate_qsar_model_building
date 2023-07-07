package gov.epa.web_services.embedding_service;

import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

/**
* @author TMARTI02
*/
public class CalculationInfo {

	
	public Boolean save_to_database;
	public String tsv_training;
	public String tsv_prediction;

	//added additional non exposed params for convenience (TMM):
	public String datasetName;
	public String descriptorSetName;
	public String splittingName;
	public Boolean remove_log_p;
	public String qsarMethodEmbedding;
	
	public String toString2() {
		return Utilities.gson.toJson(this);
	}
	
	
	
}
