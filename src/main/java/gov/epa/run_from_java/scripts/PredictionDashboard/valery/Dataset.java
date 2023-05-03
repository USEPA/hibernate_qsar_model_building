package gov.epa.run_from_java.scripts.PredictionDashboard.valery;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

public class Dataset {
	@Expose
    public Boolean binary;
	@Expose
    public String dataset_description;
	@Expose
    public String dataset_id;
	@Expose
    public String dataset_name;
	@Expose
    public String mass_unit;
	@Expose
    public ArrayList<Method> methods;
	@Expose
    public String property_description;
	@Expose
    public Integer property_id;
	@Expose
    public String property_name;
	@Expose
    public String unit;
    
    public void setDatasetId(String datasetId) {
    	this.dataset_id = datasetId;
    }
}
