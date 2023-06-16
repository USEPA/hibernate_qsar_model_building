package gov.epa.endpoints.datasets;

import java.lang.reflect.Field;

//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;

import gov.epa.databases.dev_qsar.DevQsarConstants;
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
		
		this.setQsarPropertyValue(value, finalUnitName);
	}
	
	private void setQsarPropertyValue(double value, String finalUnitName) {
		// If units are already correct, assign QSAR property value
		
		if (propertyValue.getUnit().getName().equals(finalUnitName)) {
			qsarPropertyValue = value;
			return;
		}
		
		String unitName = propertyValue.getUnit().getName();
		String propertyName = propertyValue.getProperty().getName();
		
		if (propertyName.equals(DevQsarConstants.WATER_SOLUBILITY)) {
			
			if (finalUnitName.equals("NEG_LOG_M")) {
				if (unitName.equals("LOG_M")) {
					qsarPropertyValue = -value;
				} else if (unitName.equals("MOLAR") && value!=0) {
					qsarPropertyValue = -Math.log10(value);
				} else if (unitName.equals("G_L") && value!=0 && dsstoxRecord.molWeight!=null) {
					qsarPropertyValue = -Math.log10(value/dsstoxRecord.molWeight);
				} else {
					System.out.println(propertyValue.getId() + ": Undefined property value conversion for unit: " + unitName);
					return;
				}
			} else {
				System.out.println("Need to implement code in MappedPropertyValue.setQsarPropertyValue() for "+finalUnitName);
			}
		} else if (propertyName.equals(DevQsarConstants.HENRYS_LAW_CONSTANT)) {
			
			if (finalUnitName.equals("NEG_LOG_ATM_M3_MOL")) {
				if (unitName.equals("ATM_M3_MOL")) {
					qsarPropertyValue = -Math.log10(value);
				} else {
					System.out.println(propertyValue.getId() + ": Undefined property value conversion for unit: " + unitName);
					return;
				}
			} else {
				System.out.println("Need to implement code in MappedPropertyValue.setQsarPropertyValue() for "+finalUnitName);				
			}
			
		} else if (propertyName.equals(DevQsarConstants.VAPOR_PRESSURE)) {
			
			if (finalUnitName.equals("LOG_MMHG")) {
				if (unitName.equals("MMHG")) {
					qsarPropertyValue = Math.log10(value);
				} else {
					System.out.println(propertyValue.getId() + ": Undefined property value conversion for unit: " + unitName);
					return;
				}
				
			} else {
				System.out.println("Need to implement code in MappedPropertyValue.setQsarPropertyValue() for "+finalUnitName);				
			}
		} else if (propertyName.equals("LogBCF_Fish_WholeBody")) {
			qsarPropertyValue = value;
		} else {			
			System.out.println(propertyValue.getId() + ": Undefined property value conversion for property: " + propertyName);
		}
	}
}
