package gov.epa.web_services.embedding_service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.models.ModelData;

/**
* @author TMARTI02
*/
public class CalculationInfoImportance extends CalculationInfo{

//	public double datapoints_to_descriptors_ratio=10;
	public double datapoints_to_descriptors_ratio=5;//used to set min_descriptor_count for small datasets where 20 descriptors might be too much
	
	
	@Expose
	public int num_generations=1;
	
	@Expose
	public boolean use_permutative=true;
	
	@Expose
	public double fraction_of_max_importance;//depends on qsar method
	
	@Expose
	public int min_descriptor_count=20;

	@Expose
	public int max_descriptor_count=30;

	@Expose
	public int n_threads=20;
	
	@Expose
	public boolean run_rfe=true;
	
	@Expose
	public boolean use_wards=false;
	
	
	//Add remove_correlated_descriptors variable? i.e. removes ones that are 0.95 correlated if not using wards


//	public CalculationInfoImportance() {}
	
	public CalculationInfoImportance(CalculationInfo ci) {
		this.datasetName=ci.datasetName;
		this.descriptorSetName=ci.descriptorSetName;
		this.splittingName=ci.splittingName;
		this.remove_log_p=ci.remove_log_p;
		this.save_to_database=ci.save_to_database;
		
//		this.tsv_training=ci.tsv_training;
//		this.tsv_prediction=ci.tsv_prediction;
		this.qsarMethodEmbedding=ci.qsarMethodEmbedding;
		
		if (qsarMethodEmbedding.equals(DevQsarConstants.RF)) {
			fraction_of_max_importance = 0.25;
		} else if (qsarMethodEmbedding.equals(DevQsarConstants.XGB)) {
			fraction_of_max_importance = 0.03;
		} else {
			System.out.println("Invalid QSAR method" + qsarMethodEmbedding);
			return;
		}
	}
	

	public String toString() {
//		Gson gson2 = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();		
		Gson gson2 = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		return gson2.toJson(this);
	}
}
