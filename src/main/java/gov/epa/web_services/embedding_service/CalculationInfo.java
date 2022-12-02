package gov.epa.web_services.embedding_service;

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
	
}
