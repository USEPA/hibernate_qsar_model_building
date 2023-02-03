package gov.epa.endpoints.models;

import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;

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

	public ModelPrediction(String id, Double exp, Double pred) {
		this.id = id;
		this.exp = exp;
		this.pred = pred;
	}
	
	public ModelPrediction(Prediction p) {
		this.id = p.getCanonQsarSmiles();
		this.exp = p.getQsarExperimentalValue();
		this.pred = p.getQsarPredictedValue();
		this.split=p.getSplitNum();
	}


}