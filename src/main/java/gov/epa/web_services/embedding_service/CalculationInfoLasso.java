package gov.epa.web_services.embedding_service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import gov.epa.endpoints.models.ModelData;

public class CalculationInfoLasso extends CalculationInfo{

	@Expose
	public int n_threads=20;
	
	@Expose
	public boolean run_rfe=true;
		
	
	public CalculationInfoLasso() {}
	
	public CalculationInfoLasso(CalculationInfo ci) {
		this.datasetName=ci.datasetName;
		this.descriptorSetName=ci.descriptorSetName;
		this.splittingName=ci.splittingName;
		this.remove_log_p=ci.remove_log_p;
		this.save_to_database=ci.save_to_database;
		
//		this.tsv_training=ci.tsv_training;//gets set from ModelData class
//		this.tsv_prediction=ci.tsv_prediction;
		this.qsarMethodEmbedding=ci.qsarMethodEmbedding;
	}
	
	
	public String toString() {
//		Gson gson2 = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();		
		Gson gson2 = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		return gson2.toJson(this);
	}
	
}