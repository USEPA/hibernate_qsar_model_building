package ToxPredictor.Application.model;

import java.util.Vector;

public class ClusterTable {
	private String caption;
	private String units;
	private String message;
	Vector<ClusterModel>clusterModels;
	
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}


	public Vector<ClusterModel> getClusterModels() {
		return clusterModels;
	}

	public void setClusterModels(Vector<ClusterModel> clusterModels) {
		this.clusterModels = clusterModels;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

}
