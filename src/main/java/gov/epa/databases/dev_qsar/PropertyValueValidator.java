package gov.epa.databases.dev_qsar;

import java.util.Hashtable;
import java.util.List;

import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.endpoints.datasets.ExplainedResponse;

/**
* @author TMARTI02
* 
* Need to add entry for property in:
* checkRealisticValueForProperty()
* checkRangeForProperty()
* checkUnits()
* 
* 
*/
public class PropertyValueValidator {

	public static ExplainedResponse validatePropertyValue(PropertyValue pv,DsstoxRecord dr) {
		
		
		String propertyName = pv.getProperty().getName();
		
		
		Double pointEstimate = pv.getValuePointEstimate();
		Double value = null;
		if (pointEstimate!=null) {
			// If point estimate available, set as candidate value
			value = pointEstimate;
		} else {
			// If min and max exist...
			Double max = pv.getValueMax();
			Double min = pv.getValueMin();
			
			if (max==null && min==null) {
				return new ExplainedResponse(false, "No numerical data");
			} else if (max==null || min==null) {
				return new ExplainedResponse(false, "Range with null max or min");
			}
		
			// ...and are within defined tolerance...
			Boolean rangeCheck = checkRangeForProperty(min, max, propertyName);
			if (rangeCheck==null || !rangeCheck) {
				return new ExplainedResponse(false, "Range width outside tolerance");
			}
		
			// ...then set mean as candidate value
			value = (min + max) / 2.0;
		}
		
		String unitName = pv.getUnit().getName();
		if (unitName.equals(DevQsarConstants.BINARY)) {
			// Check if binary value is actually binary
			if (!(value==1 || value==0)) {
				return new ExplainedResponse(false, "Binary value indicated, but property value is not binary");
			}
		} else {
			
			Boolean unitsCheck=checkUnits(propertyName, pv.getUnit().getName());
			if(!unitsCheck) {
				return new ExplainedResponse(false, "Invalid units for property");
			}

			// Check if property value (in original units) is realistic for property in question
			Boolean realisticValueCheck = checkRealisticValueForProperty(value, propertyName, pv.getUnit().getName(),dr);
			if (realisticValueCheck==null || !realisticValueCheck) {
				return new ExplainedResponse(false, "Unrealistic value for property");
			}
		}
		
		return new ExplainedResponse(true, value, "Convertible QSAR property value available");
	}
	
	
	private static Boolean checkUnits(String propertyName, String unitName) {
		
		
		if (propertyName.equals(DevQsarConstants.WATER_SOLUBILITY) || propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50)) {
			return (unitName.equals("G_L") || unitName.equals("MOLAR"));
		} else if (propertyName.equals(DevQsarConstants.VAPOR_PRESSURE)) {
			return unitName.equals("MMHG");
		} else if (propertyName.equals(DevQsarConstants.HENRYS_LAW_CONSTANT)) {
			return unitName.equals("ATM_M3_MOL");
		} else if (propertyName.equals(DevQsarConstants.MELTING_POINT) || propertyName.equals(DevQsarConstants.BOILING_POINT) || propertyName.equals(DevQsarConstants.FLASH_POINT)) {
			return unitName.equals("DEG_C");
		} else if (propertyName.equals(DevQsarConstants.LOG_KOW)) {
			return unitName.equals("LOG_UNITS");
		} else if (propertyName.equals(DevQsarConstants.LOG_BCF_FISH_WHOLEBODY)) {
			return unitName.equals("LOG_L_KG");//LOG_UNITS is not 100% correct
		} else {
			System.out.println("Missing entry in checkUnits() for "+propertyName);
			return false;
		}
	}

	/**
	 * Sometimes property values are reported as a range (min and max values). 
	 * 
	 * For some properties (e.g. water solubility with units of g/L) they arent in log units so
	 * need to take log when comparing the range values
	 * 
	 * @param min
	 * @param max
	 * @param propertyName
	 * @return
	 */
	public static Boolean checkRangeForProperty(double min, double max, String propertyName) {

		if (propertyName.equals(DevQsarConstants.PKA) || propertyName.equals(DevQsarConstants.PKA_A) || propertyName.equals(DevQsarConstants.PKA_B)  
				|| propertyName.equals(DevQsarConstants.LOG_KOW)
				|| propertyName.equals(DevQsarConstants.LOG_BCF_FISH_WHOLEBODY)) {
			return isRangeWithinTolerance(min, max, DevQsarConstants.LOG_RANGE_TOLERANCE);
		} else if (propertyName.equals(DevQsarConstants.MELTING_POINT) 
				|| propertyName.equals(DevQsarConstants.BOILING_POINT) 
				|| propertyName.equals(DevQsarConstants.FLASH_POINT)) {
			return isRangeWithinTolerance(min, max, DevQsarConstants.TEMP_RANGE_TOLERANCE);
		} else if (propertyName.equals(DevQsarConstants.DENSITY)) {
			return isRangeWithinTolerance(min, max, DevQsarConstants.DENSITY_RANGE_TOLERANCE);
		} else if (propertyName.equals(DevQsarConstants.VAPOR_PRESSURE) 
				|| propertyName.equals(DevQsarConstants.HENRYS_LAW_CONSTANT) 
				|| propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50)
				|| propertyName.equals(DevQsarConstants.FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50)
				|| propertyName.equals(DevQsarConstants.FORTY_EIGHT_HR_TETRAHYMENA_PYRIFORMIS_IGC50)
				|| propertyName.equals(DevQsarConstants.WATER_SOLUBILITY)) {
			return isRangeWithinLogTolerance(min, max, DevQsarConstants.LOG_RANGE_TOLERANCE, DevQsarConstants.ZERO_TOLERANCE);
		} else {
			System.out.println("Missing entry in checkRangeForProperty for "+propertyName);
			return null;
		}
	}
	
	/**
	 * The median qsar property values have log units for most properties
	 * 
	 * Note: can't just use checkRangeForProperty method because it assumes 
	 * some of the properties are not in log units yet when comparing the values
	 * 
	 * @param median1
	 * @param median2
	 * @param propertyName
	 * @return
	 */
	public static Boolean checkMedianQsarValuesForDatapoint(double median1, double median2, String propertyName) {

		if (propertyName.equals(DevQsarConstants.PKA) || propertyName.equals(DevQsarConstants.PKA_A) || propertyName.equals(DevQsarConstants.PKA_B)  
				|| propertyName.equals(DevQsarConstants.LOG_KOW)
				|| propertyName.equals(DevQsarConstants.VAPOR_PRESSURE) 
				|| propertyName.equals(DevQsarConstants.HENRYS_LAW_CONSTANT) 
				|| propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50)
				|| propertyName.equals(DevQsarConstants.FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50)
				|| propertyName.equals(DevQsarConstants.FORTY_EIGHT_HR_TETRAHYMENA_PYRIFORMIS_IGC50)
				|| propertyName.equals(DevQsarConstants.WATER_SOLUBILITY)
				|| propertyName.equals(DevQsarConstants.LOG_BCF_FISH_WHOLEBODY)) {
			return isRangeWithinTolerance(median1, median2, DevQsarConstants.LOG_RANGE_TOLERANCE);
		} else if (propertyName.equals(DevQsarConstants.MELTING_POINT) 
				|| propertyName.equals(DevQsarConstants.BOILING_POINT) 
				|| propertyName.equals(DevQsarConstants.FLASH_POINT)) {
			return isRangeWithinTolerance(median1, median2, DevQsarConstants.TEMP_RANGE_TOLERANCE);
		} else if (propertyName.equals(DevQsarConstants.DENSITY)) {
			return isRangeWithinTolerance(median1, median2, DevQsarConstants.DENSITY_RANGE_TOLERANCE);
		} else {
			System.out.println("Missing entry in checkMedianValuesForProperty for "+propertyName);
			return null;
		}
	}

	
	
	
	private static boolean isRangeWithinTolerance(double min, double max, double rangeTolerance) {
		return max - min <= rangeTolerance;
	}
	
	private static boolean isRangeWithinLogTolerance(double min, double max, double logRangeTolerance, double zeroTolerance) {
		if (Math.abs(min) > zeroTolerance) {
			return Math.log10(max / min) <= logRangeTolerance;
		} else {
			return false;
		}
	}
	
	// Check if property value (in original units) is realistic for property in question
		private static Boolean checkRealisticValueForProperty(Double candidateValue, String propertyName, String unitName,DsstoxRecord dr) {

			//TODO should we just check this after converting to molar?
			
			if (propertyName.equals(DevQsarConstants.WATER_SOLUBILITY)) {

				
				if(unitName.equals("G_L")) {
					if(candidateValue>DevQsarConstants.MAX_WATER_SOLUBILITY_G_L || 
							candidateValue<DevQsarConstants.MIN_WATER_SOLUBILITY_G_L) {
						
//						System.out.println(candidateValue+"\t"+DevQsarConstants.MIN_WATER_SOLUBILITY_G_L+"\t"+DevQsarConstants.MAX_WATER_SOLUBILITY_G_L+"\t"+unitAbbreviation);
						return false;
					} else {
						return true;
					}	
				} else if (unitName.equals("MOLAR")){//Need to convert to check
					if (dr.molWeight==null) {
						System.out.println("molWeight missing for "+dr.dsstoxCompoundId);
						return false;
					}
					double candidateValue_G_L=candidateValue*dr.molWeight;
					
					if(candidateValue_G_L>DevQsarConstants.MAX_WATER_SOLUBILITY_G_L || 
							candidateValue_G_L<DevQsarConstants.MIN_WATER_SOLUBILITY_G_L) {
//						System.out.println(candidateValue+"\t"+DevQsarConstants.MIN_WATER_SOLUBILITY_MOLAR+"\t"+DevQsarConstants.MAX_WATER_SOLUBILITY_MOLAR+"\t"+unitAbbreviation);
						return false;
					} else {
						return true;
					}	
				} else {
//					System.out.println(candidateValue+"\t"+unitAbbreviation);

					return false;
				}

			} else if (propertyName.equals(DevQsarConstants.LOG_KOW)) {
				//TODO should we try to fix the ones where the value is actually P instead of logP (especially echemportal)?
				if(candidateValue>DevQsarConstants.MAX_LOG_KOW || 
					candidateValue<DevQsarConstants.MIN_LOG_KOW) {//Assumes all values are log values
					return false;
				} else {
					return true;
				}			
			} else if (propertyName.equals(DevQsarConstants.HENRYS_LAW_CONSTANT)) {
				if(candidateValue>DevQsarConstants.MAX_HENRYS_LAW_CONSTANT_ATM_M3_MOL || 
					candidateValue<DevQsarConstants.MIN_HENRYS_LAW_CONSTANT_ATM_M3_MOL) {
					return false;
				} else {
					return true;
				}	
			} else if (propertyName.equals(DevQsarConstants.VAPOR_PRESSURE)) {

				if(candidateValue>DevQsarConstants.MAX_VAPOR_PRESSURE_MMHG ||
					candidateValue<DevQsarConstants.MIN_VAPOR_PRESSURE_MMHG) {
					return false;
				} else {
					return true;
				}			
			} else if (propertyName.equals(DevQsarConstants.LOG_BCF_FISH_WHOLEBODY)) {
				
				return candidateValue != 0.0;//Dont use if exactly zero due to toxval storing blanks as zeros TMM. Might lose a handful accidentally
				
			} else if (propertyName.equals(DevQsarConstants.MELTING_POINT)) {
				if(unitName.equals("DEG_C") && (candidateValue>DevQsarConstants.MAX_MELTING_POINT_C) ||
						candidateValue<DevQsarConstants.MIN_MELTING_POINT_C) {
					return false;
				} else {
					return true;
				}			
			} else if (propertyName.equals(DevQsarConstants.BOILING_POINT)) {
				if(unitName.equals("DEG_C") && (candidateValue>DevQsarConstants.MAX_BOILING_POINT_C) ||
						candidateValue<DevQsarConstants.MIN_BOILING_POINT_C) {
					return false;
				} else {
					return true;
				}			
			} else if (propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50)) {
				
				return candidateValue > 0.0;//TODO check if exceeds water solubility? Omit really big values?
				
			} else {
				// TBD
				System.out.println("Missing entry in checkRealisticValueForProperty for "+propertyName);
				return true;
			}
		}

}
