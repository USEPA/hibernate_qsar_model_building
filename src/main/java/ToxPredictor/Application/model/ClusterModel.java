package ToxPredictor.Application.model;

import java.util.Vector;

public class ClusterModel {
	
	private String url;
	private String urlDescriptors;
	private String clusterID;
	private String messageAD;
	private String minMaxValue;
//	private Double predValue;
//	private Double predUncertainty;
	private boolean omitted;

	private boolean binary;
	
	private String r2;
	private String q2;
			
	private String concordance;
	private String sensitivity;
	private String specificity;
	
	private Integer numChemicals;
	
	private Vector<Descriptor> Descriptors;
	
	private String modelEquation;
	private String plotImage;

	String PredictedValue;	
	String PredictedValueLabel;
	
	
	public boolean isBinary() {
		return binary;
	}
	public void setBinary(boolean binary) {
		this.binary = binary;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUrlDescriptors() {
		return urlDescriptors;
	}
	public void setUrlDescriptors(String urlDescriptors) {
		this.urlDescriptors = urlDescriptors;
	}
	public String getClusterID() {
		return clusterID;
	}
	public void setClusterID(String clusterID) {
		this.clusterID = clusterID;
	}
	public String getMessageAD() {
		return messageAD;
	}
	public void setMessageAD(String messageAD) {
		this.messageAD = messageAD;
	}
	public String getMinMaxValue() {
		return minMaxValue;
	}
	public void setMinMaxValue(String minMaxValue) {
		this.minMaxValue = minMaxValue;
	}
	public boolean isOmitted() {
		return omitted;
	}
	public void setOmitted(boolean omitted) {
		this.omitted = omitted;
	}
	public String getR2() {
		return r2;
	}
	public void setR2(String r2) {
		this.r2 = r2;
	}
	public String getQ2() {
		return q2;
	}
	public void setQ2(String q2) {
		this.q2 = q2;
	}
	public String getConcordance() {
		return concordance;
	}
	public void setConcordance(String concordance) {
		this.concordance = concordance;
	}
	public String getSensitivity() {
		return sensitivity;
	}
	public void setSensitivity(String sensitivity) {
		this.sensitivity = sensitivity;
	}
	public String getSpecificity() {
		return specificity;
	}
	public void setSpecificity(String specificity) {
		this.specificity = specificity;
	}
	public Integer getNumChemicals() {
		return numChemicals;
	}
	public void setNumChemicals(Integer numChemicals) {
		this.numChemicals = numChemicals;
	}
	public Vector<Descriptor> getDescriptors() {
		return Descriptors;
	}
	public void setDescriptors(Vector<Descriptor> descriptors) {
		Descriptors = descriptors;
	}
	public String getModelEquation() {
		return modelEquation;
	}
	public void setModelEquation(String modelEquation) {
		this.modelEquation = modelEquation;
	}
	public String getPlotImage() {
		return plotImage;
	}
	public void setPlotImage(String plotImage) {
		this.plotImage = plotImage;
	}
	public String getPredictedValue() {
		return PredictedValue;
	}
	public void setPredictedValue(String predictedValue) {
		PredictedValue = predictedValue;
	}
	public String getPredictedValueLabel() {
		return PredictedValueLabel;
	}
	public void setPredictedValueLabel(String predictedValueLabel) {
		PredictedValueLabel = predictedValueLabel;
	}	

	
}
