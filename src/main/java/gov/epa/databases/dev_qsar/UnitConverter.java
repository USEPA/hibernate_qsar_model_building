package gov.epa.databases.dev_qsar;

import org.checkerframework.checker.units.qual.mol;

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

		String unitName = propertyValue.getUnit().getName();
		String propertyName = propertyValue.getProperty().getName();
		String chemicalId=propertyValue.getId()+"";
		
		return convertUnits(value, unitName, finalUnitName, dsstoxRecord.molWeight, propertyName, chemicalId);
		
	}

	public static Double convertUnits(double value, String unitName,String finalUnitName, 	
			Double molecularWeight,  String propertyName, String chemicalId) {

		if (unitName.equals(finalUnitName)) {
			return value;
		}
		
		if (propertyName.equals(DevQsarConstants.WATER_SOLUBILITY) || 
				propertyName.equals(DevQsarConstants.ACUTE_AQUATIC_TOXICITY) ||
				propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50) ||
				propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_SCUD_LC50) ||
				propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_RAINBOW_TROUT_LC50) ||
				propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_BLUEGILL_LC50) ||
				propertyName.equals(DevQsarConstants.FORTY_EIGHT_HR_TETRAHYMENA_PYRIFORMIS_IGC50) ||
				propertyName.equals(DevQsarConstants.FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50)) {
			return handle_WATER_SOLUBILITY(propertyName, value, finalUnitName, chemicalId, molecularWeight, unitName);
		
		
		} else if (propertyName.equals(DevQsarConstants.ORAL_RAT_LD50)) {
			return handle_ORAL_RAT_LD50(propertyName,value, finalUnitName, chemicalId, molecularWeight,unitName);
		} else if (propertyName.equals(DevQsarConstants.HENRYS_LAW_CONSTANT)) {
			return handle_HENRYS_LAW_CONSTANT(propertyName,value, finalUnitName, chemicalId, unitName);
		} else if (propertyName.equals(DevQsarConstants.KmHL) || propertyName.equals(DevQsarConstants.BIODEG_HL_HC)) {
			return handle_KMHL(propertyName,value, finalUnitName, chemicalId, unitName);
		} else if (propertyName.equals(DevQsarConstants.VAPOR_PRESSURE)) {
			return handle_VAPOR_PRESSURE(propertyName,value, finalUnitName, chemicalId, unitName);
		} else if (propertyName.equals(DevQsarConstants.SURFACE_TENSION)) {
			return handle_SURFACE_TENSION(propertyName,value, finalUnitName, chemicalId, unitName);

		} else if (propertyName.equals(DevQsarConstants.VISCOSITY)) {
			return handle_VISCOSITY(propertyName,value, finalUnitName, chemicalId, unitName);
		} else if (propertyName.equals(DevQsarConstants.KOC) || propertyName.equals(DevQsarConstants.BCF)|| propertyName.equals(DevQsarConstants.BAF)) {
			return handle_KOC(propertyName,value, finalUnitName, chemicalId, unitName);
		} else if (propertyName.equals(DevQsarConstants.OH)) {
			return handle_OH(propertyName,value, finalUnitName, chemicalId, unitName);
		} else if (propertyName.equals(DevQsarConstants.CLINT)) {
			return handle_CLINT(propertyName,value, finalUnitName, chemicalId, unitName);

		} else if (propertyName.equals(DevQsarConstants.FUB) || propertyName.equals(DevQsarConstants.TTR_BINDING)) {
			return handle_dimensionless(propertyName,value, finalUnitName, chemicalId, unitName);
		
		} else if (propertyName.equals(DevQsarConstants.RBIODEG)) {
			return handle_binary(propertyName,value, finalUnitName, chemicalId, unitName);			
		} else if (propertyName.equals(DevQsarConstants.FOUR_HOUR_INHALATION_RAT_LC50)) {
			return handle_inhalation_LC50(propertyName,value, finalUnitName, chemicalId, unitName);
		} else {			
			System.out.println(chemicalId + ": Undefined property value conversion for property: " + propertyName+", "+unitName+" to "+finalUnitName);
			return null;
		}
	}

	

	private static Double handle_SURFACE_TENSION(String propertyName, double value, String finalUnitName,
			String chemicalId, String unitName) {
		
		if (finalUnitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.LOG_CP))) {
			if(unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.CP))) {
				return Math.log10(value);
			}
			
		} else if (finalUnitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.CP))) {
			if(unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.LOG_CP))) {
				return Math.pow(10, value);
			}
		}
		
		getErrorMessage(propertyName,finalUnitName, chemicalId, unitName);
		return null;
	}

	private static Double handle_ORAL_RAT_LD50(String propertyName, double value, String finalUnitName,
			String chemicalId, Double molecularWeight, String unitName) {
		
		if (finalUnitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.MOL_KG))) {
			if(unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.NEG_LOG_MOL_KG))) {
//				System.out.println(DevQsarConstants.NEG_LOG_MOL_KG+" to "+DevQsarConstants.getConstantNameByReflection(DevQsarConstants.MOL_KG));
				return Math.pow(10, -value);
			}
		}
		getErrorMessage(propertyName,finalUnitName, chemicalId, unitName);
		return null;
		
	}

	private static Double handle_inhalation_LC50(String propertyName, double value, String finalUnitName,
			String chemicalId, String unitName) {
		
		if (finalUnitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.MG_L))) {
			if(unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.LOG_MG_L))) {
				return Math.pow(10, value);
			}
		} else	if (finalUnitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.PPM))) {
			if(unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.LOG_PPM))) {
				return Math.pow(10, value);
			}
		} 
		
		getErrorMessage(propertyName,finalUnitName, chemicalId, unitName);
		return null;

	}
	
	private static Double handle_LD50(String propertyName, double value, String finalUnitName, String chemicalId,
			DsstoxRecord dsstoxRecord, String unitName) {
		
		if (finalUnitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.NEG_LOG_MOL_KG))) {
			
			if (unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.LOG_MOL_KG))) {
				return -value;
			} else if (unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.MOL_KG)) && value!=0) {
				return -Math.log10(value);
			} else if (unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.MG_KG))) {

				if (dsstoxRecord.molWeight!=null) {
					return -Math.log10(value/1000.0/dsstoxRecord.molWeight);	
				} else if (value==0) {
					System.out.println(chemicalId + ": value=0 for "+dsstoxRecord.dsstoxSubstanceId+", so cant convert to "+finalUnitName);
					return null;
				} else if (dsstoxRecord.molWeight==null) {
					//Will show up in discarded records spreadsheets as "Unit conversion failed" 
					System.out.println(chemicalId + ": missing MW for "+dsstoxRecord.dsstoxSubstanceId+", so cant convert to "+finalUnitName);
				}
			} else if (unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.UL_KG))) {
				//we didnt have a density to convert it earlier in data gathering project
				return null;
			}

		} else if (finalUnitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.MOL_KG))) {
			if(unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.NEG_LOG_MOL_KG))) {
				return Math.pow(10, -value);
			} 
		} 
		
		getErrorMessage(propertyName,finalUnitName, chemicalId, unitName);
		return null;

	}

	private static Double handle_CLINT(String propertyName, double value, String finalUnitName, String chemicalId,
			String unitName) {

		if (finalUnitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.LOG_UL_MIN_1MM_CELLS))) {
			if(unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.UL_MIN_1MM_CELLS))) {
				return Math.log10(value);
			}
			
		} else if (finalUnitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.UL_MIN_1MM_CELLS))) {
			if(unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.LOG_UL_MIN_1MM_CELLS))) {
				return Math.pow(10, value);
			}
		}
		getErrorMessage(propertyName,finalUnitName, chemicalId, unitName);
		return null;
	}

	private static Double handle_binary(String propertyName, double value, String finalUnitName, String chemicalId,
			String unitName) {
		
		if(finalUnitName.equals("BINARY") && unitName.equals("BINARY")) {
			return value;
		}
		getErrorMessage(propertyName,finalUnitName, chemicalId, unitName);
		return null;
	}
	
	private static Double handle_dimensionless(String propertyName, double value, String finalUnitName, String chemicalId,
			String unitName) {
		
		if(finalUnitName.equals("DIMENSIONLESS") && unitName.equals("DIMENSIONLESS")) {
			return value;
		}
		getErrorMessage(propertyName,finalUnitName, chemicalId, unitName);
		return null;
	}

	private static Double handle_OH(String propertyName, double value, String finalUnitName, String chemicalId,
			String unitName) {
		
		if (finalUnitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.LOG_CM3_MOLECULE_SEC))) {
			if(unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.CM3_MOLECULE_SEC))) {
				return Math.log10(value);
			}
			
		} else if (finalUnitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.CM3_MOLECULE_SEC))) {
			if(unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.LOG_CM3_MOLECULE_SEC))) {
				return Math.pow(10, value);
			}
		}
		getErrorMessage(propertyName,finalUnitName, chemicalId, unitName);
		return null;

	}

	private static Double handle_KOC(String propertyName, double value, String finalUnitName, String chemicalId,
			String unitName) {
		
		if (finalUnitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.LOG_L_KG))) {
			if(unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.L_KG))) {
				return Math.log10(value);
			}
			
		} else if (finalUnitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.L_KG))) {
			if(unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.LOG_L_KG))) {
				return Math.pow(10, value);
			}
		}
		getErrorMessage(propertyName,finalUnitName, chemicalId, unitName);
		
		return null;
	}

	private static Double handle_KMHL(String propertyName, double value, String finalUnitName, String chemicalId,
			String unitName) {

		if (finalUnitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.LOG_DAYS))) {
			if(unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.DAYS))) {
				return Math.log10(value);
			}
			
		} else if (finalUnitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.DAYS))) {
			if(unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.LOG_DAYS))) {
				return Math.pow(10, value);
			}
		}
		getErrorMessage(propertyName,finalUnitName, chemicalId, unitName);
		return null;
	}

	private static Double handle_VAPOR_PRESSURE(String propertyName, double value, String finalUnitName, String chemicalId,
			String unitName) {
		if (finalUnitName.equals("LOG_MMHG")) {
			if (unitName.equals("MMHG")) {
				return Math.log10(value);
			} 
		} else if (finalUnitName.equals("MMHG")) {
			if (unitName.equals("LOG_MMHG")) {
				return Math.pow(10, value);
			} 
		}
		
		getErrorMessage(propertyName,finalUnitName, chemicalId, unitName);
		return null;

	}
	
	private static Double handle_VISCOSITY(String propertyName, double value, String finalUnitName, String chemicalId,
			String unitName) {
		
		if (finalUnitName.equals("LOG_CP")) {
			if (unitName.equals("CP")) {
				return Math.log10(value);
			} 
		} else if (finalUnitName.equals("CP")) {
			if (unitName.equals("LOG_CP")) {
				return Math.pow(10, value);
			} 
		}
		
		getErrorMessage(propertyName,finalUnitName, chemicalId, unitName);
		return null;

	}

	private static Double handle_HENRYS_LAW_CONSTANT(String propertyName, double value, String finalUnitName, String chemicalId,
			String unitName) {

		if (finalUnitName.equals("NEG_LOG_ATM_M3_MOL")) {
			if (unitName.equals("ATM_M3_MOL")) {
				return -Math.log10(value);
			} else {
				System.out.println(chemicalId + ": Undefined property value conversion for unit: " + unitName);
				return null;
			}

		} else if (finalUnitName.equals("ATM_M3_MOL")) {
			if (unitName.equals("NEG_LOG_ATM_M3_MOL")) {
				return Math.pow(10, -value);
			}
		}

		getErrorMessage(propertyName, finalUnitName, chemicalId, unitName);
		return null;
		
	}

	private static void getErrorMessage(String propertyName, String finalUnitName, String chemicalId, String unitName) {
		System.out.println(chemicalId + ": Undefined unitConverter conversion for " + unitName+" to "+finalUnitName+" for "+propertyName);
	}

	private static Double handle_WATER_SOLUBILITY(String propertyName, double value, String finalUnitName, String chemicalId,
			Double molecularWeight, String unitName) {

		if (finalUnitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.NEG_LOG_M))) {
			
			if (unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.LOG_M))) {
				return -value;
			} else if (unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.MOLAR)) && value!=0) {
				return -Math.log10(value);
			} else if (unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.G_L))) {

				if (molecularWeight!=null) {
					return -Math.log10(value/molecularWeight);	
				} else if (value==0) {
					System.out.println(chemicalId + ": value=0 for "+chemicalId+", so cant convert to "+finalUnitName);
					return null;
				} else if (molecularWeight==null) {
					//Will show up in discarded records spreadsheets as "Unit conversion failed" 
					System.out.println(chemicalId + ": missing MW for "+chemicalId+", so cant convert to "+finalUnitName);
					return null;
				}
			} 

		} else if (finalUnitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.MOLAR))) {
			if(unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.NEG_LOG_M))) {
				return Math.pow(10, -value);
			} 
		} 
		
		getErrorMessage(propertyName,finalUnitName, chemicalId, unitName);
		return null;
	}

}
