package gov.epa.endpoints.reports;

public class OriginalCompound {
	public String dtxcid;
	public String casrn;
	public String preferredName;
	public String smiles;
	
	public OriginalCompound(String dtxcid, String casrn, String preferredName, String smiles) {
		this.dtxcid = dtxcid;
		this.casrn = casrn;
		this.preferredName = preferredName;
		this.smiles = smiles;
	}
}