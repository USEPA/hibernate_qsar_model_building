package gov.epa.endpoints.models;

public class ModelPrediction {
	public String ID;
	public Double exp;
	public Double pred;
	
	public ModelPrediction(String ID, Double exp, Double pred) {
		this.ID = ID;
		this.exp = exp;
		this.pred = pred;
	}
}