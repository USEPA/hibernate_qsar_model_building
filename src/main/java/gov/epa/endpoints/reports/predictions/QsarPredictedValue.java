package gov.epa.endpoints.reports.predictions;

public class QsarPredictedValue {
	public String qsarMethodName;
	public String qsarMethodDescription;
	public Double qsarPredictedValue;
	public int splitNum;
	
	public QsarPredictedValue(String qsarMethodName, String qsarMethodDescription, Double qsarPredictedValue, int splitNum) {
		this.qsarMethodName = qsarMethodName;
		this.qsarMethodDescription = qsarMethodDescription;
		this.qsarPredictedValue = qsarPredictedValue;
		this.splitNum = splitNum;
	}
}