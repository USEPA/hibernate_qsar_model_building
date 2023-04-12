package ToxPredictor.Application.model;

public class MOAPrediction {
	private String MOA;
	private String color;
	private String tag;


	ClusterModel clusterModelMOA;
	ClusterModel clusterModelLC50;
	

	private String MOAScore;
	private String MOAScoreMsg;
	
	private String LC50Score;
	private String LC50ScoreMsg;

	
	public ClusterModel getClusterModelMOA() {
		return clusterModelMOA;
	}
	public void setClusterModelMOA(ClusterModel clusterModelMOA) {
		this.clusterModelMOA = clusterModelMOA;
	}
	public ClusterModel getClusterModelLC50() {
		return clusterModelLC50;
	}
	public void setClusterModelLC50(ClusterModel clusterModelLC50) {
		this.clusterModelLC50 = clusterModelLC50;
	}
	public String getMOAScoreMsg() {
		return MOAScoreMsg;
	}
	public void setMOAScoreMsg(String mOAScoreMsg) {
		MOAScoreMsg = mOAScoreMsg;
	}
	public String getLC50ScoreMsg() {
		return LC50ScoreMsg;
	}
	public void setLC50ScoreMsg(String lC50ScoreMsg) {
		LC50ScoreMsg = lC50ScoreMsg;
	}
	public String getMOAScore() {
		return MOAScore;
	}
	public void setMOAScore(String mOAScore) {
		MOAScore = mOAScore;
	}
	public String getLC50Score() {
		return LC50Score;
	}
	public void setLC50Score(String lC50Score) {
		LC50Score = lC50Score;
	}
	public String getMOA() {
		return MOA;
	}
	public void setMOA(String mOA) {
		MOA = mOA;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	
	
}
