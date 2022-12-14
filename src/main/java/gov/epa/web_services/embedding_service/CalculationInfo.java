package gov.epa.web_services.embedding_service;

/**
 * @author CRAMSLAN
 * TODO: add json object to devqsarconstants with these presets
 * anytime go to build model, query based on GA method, descriptor set, dataset, and (json) descriptionstandard.toString()
 * 
 *  
 *  TODO: create an easy way to make default embeddings
 *  
 *  unique embedding name: datasetname_uniquetime in milliseconds
 *  
 *  make listsplitter less annoying to use
 */
public class CalculationInfo {
	public Boolean save_to_database;
	public String tsv;
	public String qsarMethod;
	
	public Boolean remove_log_p;
	
	//Default GA params:
	public Integer num_generations=100;
	public Integer num_optimizers=10;
	public Integer num_jobs=4;
	public Integer n_threads=20;
	public Integer max_length=24;
	public Double descriptor_coefficient=0.002;
	
	public static CalculationInfo createDefault() {
		return new CalculationInfo();
	}
	
}
