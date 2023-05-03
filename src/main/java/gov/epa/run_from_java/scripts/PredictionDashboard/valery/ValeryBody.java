package gov.epa.run_from_java.scripts.PredictionDashboard.valery;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

public class ValeryBody {

	@Expose
	public ArrayList<Chemical> chemicals;
	@Expose
    public String modelset_id;
	@Expose
    public ArrayList<Dataset> datasets;
	@Expose
    public String workflow;
    
    public ValeryBody() {}
    
    public void setChemicals(ArrayList<Chemical> chemicals) {
    	this.chemicals = chemicals;
    }
    
    public void setDatasets(ArrayList<Dataset> datasets) {
    	this.datasets = datasets;
    }
    
    public void setModelSetId(String modelSetId) {
    	this.modelset_id = modelSetId;
    }
    
    public void setWorkflow(String workflow) {
    	this.workflow = workflow;
    }
    
}
