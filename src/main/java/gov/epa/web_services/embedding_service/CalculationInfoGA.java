package gov.epa.web_services.embedding_service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import gov.epa.databases.dev_qsar.DevQsarConstants;

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
public class CalculationInfoGA extends CalculationInfo{
	
	//Default GA params:
	@Expose
	public Integer num_generations=100;
	@Expose
	public Integer num_optimizers=10;
	@Expose
	public Integer num_jobs=4;
	@Expose
	public Integer n_threads=20;
	@Expose
	public Integer max_length=24;
	@Expose
	public Double descriptor_coefficient=0.002;
	@Expose
	public Integer threshold = 1;

	@Expose
	public Boolean use_wards=true;
	
	public CalculationInfoGA() {}
	
	public CalculationInfoGA(CalculationInfo ci) {
		this.datasetName=ci.datasetName;
		this.descriptorSetName=ci.descriptorSetName;
		this.splittingName=ci.splittingName;
		this.remove_log_p=ci.remove_log_p;
		this.save_to_database=ci.save_to_database;
		this.tsv_training=ci.tsv_training;
		this.tsv_prediction=ci.tsv_prediction;
		this.qsarMethodEmbedding=ci.qsarMethodEmbedding;
	}


	public static CalculationInfoGA createDefault() {
		return new CalculationInfoGA();
	}
	
	
	public String toString() {
//		Gson gson2 = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();		
		Gson gson2 = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		return gson2.toJson(this);
	}
	
	public static void main(String[] args) {
		CalculationInfoGA ci=createDefault();
		System.out.println(ci);
		
	}
	
	
}
