package gov.epa.endpoints.datasets;

import java.lang.reflect.Field;

//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.UnitConverter;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.databases.dsstox.DsstoxRecord;

public class MappedPropertyValue {
	public String id; // Depends on mapping strategy - CASRN, DTXRID, DTXCID, etc.
	public PropertyValue propertyValue;
	public DsstoxRecord dsstoxRecord;
	
	public Compound compound;
	public Boolean isStandardized;
	public String standardizedSmiles;
	
	public Double qsarPropertyValue;
	public String qsarPropertyUnits;
	
//	private static final Logger logger = LogManager.getLogger(MappedPropertyValue.class);
	
	public MappedPropertyValue() {}
	
	public MappedPropertyValue(String id, PropertyValue propertyValue, DsstoxRecord dsstoxRecord, double value, String finalUnitName) {
		this.id = id;
		this.propertyValue = propertyValue;
		this.dsstoxRecord = dsstoxRecord;
		
		this.isStandardized = false;
		this.qsarPropertyUnits=finalUnitName;
		UnitConverter.setQsarPropertyValue(this,value, finalUnitName);
	}
	
	
}
