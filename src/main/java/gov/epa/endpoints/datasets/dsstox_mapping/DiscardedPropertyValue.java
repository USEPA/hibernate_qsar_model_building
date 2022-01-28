package gov.epa.endpoints.datasets.dsstox_mapping;

import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dsstox.DsstoxRecord;

public class DiscardedPropertyValue {
	public PropertyValue propertyValue;
	public DsstoxRecord dsstoxRecord;
	public String reason;
	
	public DiscardedPropertyValue(PropertyValue propertyValue, DsstoxRecord dsstoxRecord, String reason) {
		this.propertyValue = propertyValue;
		this.dsstoxRecord = dsstoxRecord;
		this.reason = reason;
	}
}