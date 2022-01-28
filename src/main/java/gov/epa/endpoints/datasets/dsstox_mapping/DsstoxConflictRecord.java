package gov.epa.endpoints.datasets.dsstox_mapping;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dsstox.DsstoxRecord;

public class DsstoxConflictRecord {
	public DsstoxRecord dsstoxRecord;
	public String standardizedSmiles;
	public String conflictType;
	
	public DsstoxConflictRecord(DsstoxRecord dsstoxRecord, String conflictType) {
		this.dsstoxRecord = dsstoxRecord;
		this.conflictType = conflictType;
	}
	
	public double score() {
		double score = 0.0;
		
		String[] dsstoxIdTypes = { DevQsarConstants.INPUT_DTXSID, DevQsarConstants.INPUT_DTXCID };
		for (String type:dsstoxIdTypes) {
			if (conflictType.contains(type)) {
				score += DsstoxMapper.DSSTOX_ID_SCORE;
			}
		}
		
		String[] casrnOrStrictNameTypes = { DevQsarConstants.INPUT_CASRN,
				DevQsarConstants.INPUT_PREFERRED_NAME,
				"VALID_" + DevQsarConstants.INPUT_SYNONYM,
				"VALID_SOURCE_" + DevQsarConstants.INPUT_SYNONYM,
				"UNIQUE_" + DevQsarConstants.INPUT_SYNONYM };
		for (String type:casrnOrStrictNameTypes) {
			if (conflictType.contains(type)) {
				score += DsstoxMapper.CASRN_OR_STRICT_NAME_SCORE;
			}
		}
		
		String[] lenientNameTypes = { "AMBIGUOUS_" + DevQsarConstants.INPUT_SYNONYM,
				DevQsarConstants.INPUT_MAPPED_IDENTIFIER,
				DevQsarConstants.INPUT_NAME2STRUCTURE };
		for (String type:lenientNameTypes) {
			if (conflictType.contains(type)) {
				score += DsstoxMapper.LENIENT_NAME_SCORE;
			}
		}
		
		return score;
	}
}