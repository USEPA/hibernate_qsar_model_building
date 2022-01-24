package gov.epa.endpoints.datasets.classes;

import gov.epa.databases.dsstox.DsstoxRecord;

public class ExplainedResponse {
	public boolean response;
	public DsstoxRecord record;
	public double value;
	public String reason;
	
	public ExplainedResponse(boolean response, String reason) {
		this.response = response;
		this.reason = reason;
	}
	
	public ExplainedResponse(boolean response, double value, String reason) {
		this.response = response;
		this.value = value;
		this.reason = reason;
	}
	
	public ExplainedResponse(boolean response, DsstoxRecord record, String reason) {
		this.response = response;
		this.record = record;
		this.reason = reason;
	}
}