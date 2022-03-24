package gov.epa.endpoints.reports.predictions;

public class QsarPredictedValue {
	public String qsarMethodName;
	public Double qsarPredictedValue;
	public int splitNum;
	
	public QsarPredictedValue(String qsarMethodName, Double qsarPredictedValue, Integer splitNum) {
		this.qsarMethodName = qsarMethodName;
		this.qsarPredictedValue = qsarPredictedValue;
		this.splitNum = splitNum;
	}
}