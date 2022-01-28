package gov.epa.endpoints.datasets.dsstox_mapping;

import java.util.ArrayList;
import java.util.List;

import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;
import gov.epa.databases.dsstox.DsstoxRecord;

public class DsstoxConflict {
	public SourceChemical sourceChemical;
	public DsstoxRecord bestDsstoxRecord;
	public String bestStandardizedSmiles;
	public List<DsstoxConflictRecord> dsstoxConflictRecords;
	public Double fracAgree;
	
	public boolean accept;
	public String reason;
	
	public DsstoxConflict(SourceChemical sourceChemical, DsstoxRecord bestDsstoxRecord) {
		this.sourceChemical = sourceChemical;
		this.bestDsstoxRecord = bestDsstoxRecord;
		this.dsstoxConflictRecords = new ArrayList<DsstoxConflictRecord>();
	}
	
	public void setAcceptAndReason(boolean accept, String reason) {
		this.accept = accept;
		this.reason = reason;
	}
}