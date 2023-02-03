package gov.epa.endpoints.models;


public class ModelPrediction {
	public String id;
	public Double exp;
	public Double pred;
	public Double weight;
	public Integer split;
	

	public ModelPrediction(String id, Double exp, Double pred, Integer split) {
		this.id = id;
		this.exp=exp;
		this.pred = pred;
		this.split=split;
	}

//	public ModelPrediction(String id, Double exp, Double pred) {
//		this.id = id;
//		this.exp = exp;
//		this.pred = pred;
//	}
	

}