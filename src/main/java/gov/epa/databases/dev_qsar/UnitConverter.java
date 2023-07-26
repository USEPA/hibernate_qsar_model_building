package gov.epa.databases.dev_qsar;

import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.endpoints.datasets.MappedPropertyValue;

/**
 * 
 * 
* @author TMARTI02
*/
public class UnitConverter {

	
	/**
	 * Converts units to qsar units
	 * 
	 * @param mappedPropertyValue
	 * @param value
	 * @param finalUnitName
	 * @return
	 */
	public static void setQsarPropertyValue(MappedPropertyValue mappedPropertyValue, double value, String finalUnitName) {
		// If units are already correct, assign QSAR property value
		
		PropertyValue propertyValue=mappedPropertyValue.propertyValue;
		DsstoxRecord dsstoxRecord=mappedPropertyValue.dsstoxRecord;
		
		mappedPropertyValue.qsarPropertyValue=getValue(value, finalUnitName, propertyValue, dsstoxRecord);
		
	}

	private static Double getValue(double value, String finalUnitName, PropertyValue propertyValue,
			DsstoxRecord dsstoxRecord) {
		if (propertyValue.getUnit().getName().equals(finalUnitName)) {
			return value;
		}
		
		String unitName = propertyValue.getUnit().getName();
		String propertyName = propertyValue.getProperty().getName();
		
		if (propertyName.equals(DevQsarConstants.WATER_SOLUBILITY) || propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50)) {
			return handle_WATER_SOLUBILITY(value, finalUnitName, propertyValue, dsstoxRecord, unitName);
		} else if (propertyName.equals(DevQsarConstants.HENRYS_LAW_CONSTANT)) {
			return handle_HENRYS_LAW_CONSTANT(value, finalUnitName, propertyValue, unitName);
		} else if (propertyName.equals(DevQsarConstants.VAPOR_PRESSURE)) {
			return handle_VAPOR_PRESSURE(value, finalUnitName, propertyValue, unitName);
		} else if (propertyName.equals("LogBCF_Fish_WholeBody")) {
			return value;
		} else {			
			System.out.println(propertyValue.getId() + ": Undefined property value conversion for property: " + propertyName);
			return null;
		}
	}

	private static Double handle_VAPOR_PRESSURE(double value, String finalUnitName, PropertyValue propertyValue,
			String unitName) {
		if (finalUnitName.equals("LOG_MMHG")) {
			if (unitName.equals("MMHG")) {
				return Math.log10(value);
			} else {
				System.out.println(propertyValue.getId() + ": Undefined property value conversion for unit: " + unitName);
				return null;
			}
			
		} else {
			System.out.println("Need to implement code in UnitConverter.getQsarPropertyValue() for "+finalUnitName);				
			return null;
		}
	}

	private static Double handle_HENRYS_LAW_CONSTANT(double value, String finalUnitName, PropertyValue propertyValue,
			String unitName) {
		if (finalUnitName.equals("NEG_LOG_ATM_M3_MOL")) {
			if (unitName.equals("ATM_M3_MOL")) {
				return -Math.log10(value);
			} else {
				System.out.println(propertyValue.getId() + ": Undefined property value conversion for unit: " + unitName);
				return null;
			}
		} else {
			System.out.println("Need to implement code in UnitConverter.getQsarPropertyValue() for "+finalUnitName);				
			return null;
		}
	}

	private static Double handle_WATER_SOLUBILITY(double value, String finalUnitName, PropertyValue propertyValue,
			DsstoxRecord dsstoxRecord, String unitName) {

		if (finalUnitName.equals("NEG_LOG_M")) {
			if (unitName.equals("LOG_M")) {
				return -value;
			} else if (unitName.equals("MOLAR") && value!=0) {
				return -Math.log10(value);
			} else if (unitName.equals("G_L") && value!=0 && dsstoxRecord.molWeight!=null) {
				return -Math.log10(value/dsstoxRecord.molWeight);
			} else {
				System.out.println(propertyValue.getId() + ": Undefined property value conversion for unit: " + unitName);
				return null;
			}
		} else {
			System.out.println("Need to implement code in UnitConverter.getQsarPropertyValue() for "+finalUnitName);
			return null;
		}
	}

}
