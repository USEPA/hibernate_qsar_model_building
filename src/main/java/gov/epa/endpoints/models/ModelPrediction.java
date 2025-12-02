package gov.epa.endpoints.models;

import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteValidation.CheckStructure;

public class ModelPrediction implements Comparable<ModelPrediction>{
	public String id;
	public Double exp;
	public Double pred;
	public Double weight;
	public Integer split;//T=0,P=1
	
//	public String dtxcid;
	public Boolean insideAD;
	public CheckStructure checkStructure;

	public String qsarClass;
	
	public ModelPrediction(String id, Double exp, Double pred, Integer split) {
		this.id = id;
		this.exp=exp;
		this.pred = pred;
		this.split=split;
	}
	
	
	public ModelPrediction(String id, Double exp, Double pred, Integer split,Boolean insideAD) {
		this.id = id;
		this.exp=exp;
		this.pred = pred;
		this.split=split;
		this.insideAD=insideAD;
	}

	public Double absError() {
		if(exp==null || pred==null)return null;
		return Math.abs(exp-pred);
	}
	
	
	@Override
	public int compareTo(ModelPrediction mp) {
		return -Double.compare(this.absError(), mp.absError());//puts largest errors first
	}

//	public ModelPrediction(String id, Double exp, Double pred) {
//		this.id = id;
//		this.exp = exp;
//		this.pred = pred;
//	}
	

}