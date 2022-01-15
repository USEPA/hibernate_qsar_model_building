package gov.epa.endpoints.reports.WebTEST.ReportClasses;

import java.util.List;

import gov.epa.endpoints.reports.WebTEST.ApplicabilityDomain.Analog;

public class ApplicabilityDomainNN {
	private double fracTrainingForAD;
	
	private double scFracTraining;//similarity coefficient that gives a certain fraction of training set to be inside AD
	private double avgSCNN;//average similarity of neighbors to test compound
	private List<Analog>analogsAD;

	
	public double getFracTrainingForAD() {
		return fracTrainingForAD;
	}
	public void setFracTrainingForAD(double fracTrainingForAD) {
		this.fracTrainingForAD = fracTrainingForAD;
	}

	public double getScFracTraining() {
		return scFracTraining;
	}
	public void setScFracTraining(double scFracTraining) {
		this.scFracTraining = scFracTraining;
	}
	public double getAvgSCNN() {
		return avgSCNN;
	}
	public void setAvgSCNN(double avgSCNN) {
		this.avgSCNN = avgSCNN;
	}
	public List<Analog> getAnalogsAD() {
		return analogsAD;
	}
	public void setAnalogsAD(List<Analog> analogsAD) {
		this.analogsAD = analogsAD;
	}
}
