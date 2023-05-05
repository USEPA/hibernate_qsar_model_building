package gov.epa.run_from_java.scripts.PredictionDashboard.valery;

import com.google.gson.annotations.Expose;

public class Chemical {
	
	@Expose
	public String inchi_key;
	@Expose
    public String smiles;
	public String dtxcid;
	public String dtxsid;
	public String originalSmiles;
    
	public Chemical() {};

	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}

}
