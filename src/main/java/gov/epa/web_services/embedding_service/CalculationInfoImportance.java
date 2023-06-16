package gov.epa.web_services.embedding_service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

/**
* @author TMARTI02
*/
public class CalculationInfoImportance {

	public String qsarMethod;
	public String tsv_training;
	public String tsv_prediction;
	public boolean remove_log_p;
	
	public String datasetName;
	public String descriptorSetName;
	public String splittingName;

	
	@Expose
	public int num_generations=1;
	
	@Expose
	public boolean use_permutative=true;
	
	@Expose
	public double fraction_of_max_importance;
	
	@Expose
	public int min_descriptor_count=8;

	@Expose
	public int max_descriptor_count;

	@Expose
	public int n_threads=20;
	
	@Expose
	public boolean run_rfe;

	

	public String toString() {
//		Gson gson2 = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();		
		Gson gson2 = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		return gson2.toJson(this);
	}
}
