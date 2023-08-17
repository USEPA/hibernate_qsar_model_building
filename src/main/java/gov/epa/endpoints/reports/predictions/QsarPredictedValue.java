package gov.epa.endpoints.reports.predictions;

public class QsarPredictedValue {
	public String qsarMethodName;
	public Double qsarPredictedValue;
	public Integer splitNum;
	public Boolean AD;
	
	public QsarPredictedValue(String qsarMethodName, Double qsarPredictedValue, Integer splitNum) {
		this.qsarMethodName = qsarMethodName;
		this.qsarPredictedValue = qsarPredictedValue;
		this.splitNum = splitNum;
	}
}