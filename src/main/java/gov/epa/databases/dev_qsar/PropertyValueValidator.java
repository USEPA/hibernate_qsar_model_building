
package gov.epa.databases.dev_qsar;
import java.util.Hashtable;
import java.util.List;

import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.endpoints.datasets.BoundPropertyValue;
import gov.epa.endpoints.datasets.ExplainedResponse;

/**
 * @author TMARTI02
 * 
 *         Need to add entry for property in: checkRealisticValueForProperty()
 *         checkRangeForProperty() checkUnits()
 * 
 * 
 */
public class PropertyValueValidator {

	public static ExplainedResponse validatePropertyValue(PropertyValue pv, DsstoxRecord dr,
			BoundPropertyValue boundPropertyValue) {

		String propertyName = pv.getProperty().getName();
		Double pointEstimate = pv.getValuePointEstimate();
		Double value = null;

		if (pointEstimate != null) {
			// If point estimate available, set as candidate value
			value = pointEstimate;
		} else {
			// If min and max exist...
			Double max = pv.getValueMax();
			Double min = pv.getValueMin();

			if (max == null && min == null) {
				return new ExplainedResponse(false, "No numerical data");
			} else if (max == null || min == null) {
				return new ExplainedResponse(false, "Range with null max or min");
			}

			// ...and are within defined tolerance...
			Boolean rangeCheck = checkRangeForProperty(min, max, propertyName);
			if (rangeCheck == null || !rangeCheck) {
				return new ExplainedResponse(false, "Range width outside tolerance");
			}

			// ...then set mean as candidate value
			value = (min + max) / 2.0;
		}

		String unitName = pv.getUnit().getName();
		if (unitName.equals(DevQsarConstants.BINARY)) {
			// Check if binary value is actually binary
			if (!(value == 1 || value == 0)) {
				return new ExplainedResponse(false, "Binary value indicated, but property value is not binary");
			}
		} else {

			Boolean unitsCheck = checkUnits(propertyName, pv.getUnit().getName());
			if (!unitsCheck) {
				return new ExplainedResponse(false, "Invalid units for property");
			}

			// Check if property value (in original units) is realistic for property in
			// question
			Boolean realisticValueCheck = checkRealisticValueForProperty(value, propertyName, pv.getUnit().getName(),
					dr, boundPropertyValue);

			if (realisticValueCheck == null || !realisticValueCheck) {
				return new ExplainedResponse(false, "Unrealistic value for property");
			}
		}

		return new ExplainedResponse(true, value, "Convertible QSAR property value available");
	}

	private static Boolean checkUnits(String propertyName, String unitName) {

		if (propertyName.equals(DevQsarConstants.WATER_SOLUBILITY)
				|| propertyName.equals(DevQsarConstants.FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50)
				|| propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50)
				|| propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_SCUD_LC50)
				|| propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_RAINBOW_TROUT_LC50)
				|| propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_BLUEGILL_LC50)
				|| propertyName.equals(DevQsarConstants.FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50)) {
			return (unitName.equals("G_L") || unitName.equals("MOLAR"));
		} else if (propertyName.equals(DevQsarConstants.VAPOR_PRESSURE)) {
			return unitName.equals("MMHG");
		
		} else if (propertyName.equals(DevQsarConstants.OH)) {
			return unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.CM3_MOLECULE_SEC));
		} else if (propertyName.equals(DevQsarConstants.TTR_BINDING)) {
			return unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.DIMENSIONLESS));
		} else if (propertyName.equals(DevQsarConstants.HENRYS_LAW_CONSTANT)) {
			return unitName.equals("ATM_M3_MOL");
		} else if (propertyName.equals(DevQsarConstants.MELTING_POINT)
				|| propertyName.equals(DevQsarConstants.BOILING_POINT)
				|| propertyName.equals(DevQsarConstants.FLASH_POINT)) {
			return unitName.equals("DEG_C");
		} else if (propertyName.equals(DevQsarConstants.FUB) || propertyName.equals(DevQsarConstants.TTR_BINDING)) {
			return unitName.equals("DIMENSIONLESS");
		} else if (propertyName.equals(DevQsarConstants.SURFACE_TENSION)) {
			return unitName.equals("DYN_CM");
		} else if (propertyName.equals(DevQsarConstants.DENSITY)) {
			return unitName.equals("G_CM3");

		} else if (propertyName.equals(DevQsarConstants.LOG_KOW) 
				|| propertyName.equals(DevQsarConstants.LOG_KOA)
				|| propertyName.equals(DevQsarConstants.PKA_A)
				|| propertyName.equals(DevQsarConstants.PKA_B)
				|| propertyName.equals(DevQsarConstants.PKA)) {
			return unitName.equals("LOG_UNITS");
		} else if (propertyName.equals(DevQsarConstants.KOC) || propertyName.equals(DevQsarConstants.BCF)) {
			return unitName.equals("L_KG");// LOG_UNITS is not 100% correct
		} else if (propertyName.equals(DevQsarConstants.BIODEG_HL_HC) || propertyName.equals(DevQsarConstants.KmHL)) {
			return unitName.equals("DAYS");// LOG_UNITS is not 100% correct		
		} else if (propertyName.equals(DevQsarConstants.RBIODEG) ||
				propertyName.equals(DevQsarConstants.ESTROGEN_RECEPTOR_AGONIST) ||
				propertyName.equals(DevQsarConstants.ESTROGEN_RECEPTOR_ANTAGONIST) ||
				propertyName.equals(DevQsarConstants.ESTROGEN_RECEPTOR_BINDING) ||
				propertyName.equals(DevQsarConstants.ANDROGEN_RECEPTOR_AGONIST) ||
				propertyName.equals(DevQsarConstants.ANDROGEN_RECEPTOR_ANTAGONIST) ||
				propertyName.equals(DevQsarConstants.ANDROGEN_RECEPTOR_BINDING)) {
			return unitName.equals("BINARY");
		} else if (propertyName.equals(DevQsarConstants.CLINT)) {
			return unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.UL_MIN_1MM_CELLS));

		} else if (propertyName.equals(DevQsarConstants.FOUR_HOUR_INHALATION_RAT_LC50)) {
			return (unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.LOG_PPM))
					|| unitName.equals(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.LOG_MG_L)));
			
		} else {
			System.out.println(
					"Missing entry in checkUnits() for propertyName=" + propertyName + " for unitName=" + unitName);
			return false;
		}
	}

	/**
	 * Sometimes property values are reported as a range (min and max values).
	 * 
	 * For some properties (e.g. water solubility with units of g/L) they arent in
	 * log units so need to take log when comparing the range values
	 * 
	 * @param min
	 * @param max
	 * @param propertyName
	 * @return
	 */
	public static Boolean checkRangeForProperty(double min, double max, String propertyName) {

		if (propertyName.equals(DevQsarConstants.PKA) 
				|| propertyName.equals(DevQsarConstants.LOG_KOA)
				|| propertyName.equals(DevQsarConstants.LOG_KOW)
				|| propertyName.equals(DevQsarConstants.PKA_A)
				|| propertyName.equals(DevQsarConstants.PKA_B)) {
			// already in log units:
			return isRangeWithinTolerance(min, max, DevQsarConstants.LOG_RANGE_TOLERANCE);
		} else if (propertyName.equals(DevQsarConstants.MELTING_POINT)
				|| propertyName.equals(DevQsarConstants.BOILING_POINT)
				|| propertyName.equals(DevQsarConstants.FLASH_POINT)) {
			return isRangeWithinTolerance(min, max, DevQsarConstants.TEMP_RANGE_TOLERANCE);
		} else if (propertyName.equals(DevQsarConstants.DENSITY) || 
				propertyName.equals(DevQsarConstants.FUB)) {
			return isRangeWithinTolerance(min, max, DevQsarConstants.DENSITY_RANGE_TOLERANCE);
		
		} else if (propertyName.equals(DevQsarConstants.TTR_BINDING)) {
			return isRangeWithinTolerance(min, max, 10.0);
			
		} else if (propertyName.equals(DevQsarConstants.VAPOR_PRESSURE)
				|| propertyName.equals(DevQsarConstants.HENRYS_LAW_CONSTANT)
				|| propertyName.equals(DevQsarConstants.KmHL) || propertyName.equals(DevQsarConstants.BIODEG_HL_HC)
				|| propertyName.equals(DevQsarConstants.OH)
				|| propertyName.equals(DevQsarConstants.CLINT)
				|| propertyName.equals(DevQsarConstants.SURFACE_TENSION)
				|| propertyName.equals(DevQsarConstants.KOC) || propertyName.equals(DevQsarConstants.BCF)
				|| propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50)
				|| propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_SCUD_LC50)
				|| propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_RAINBOW_TROUT_LC50)
				|| propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_BLUEGILL_LC50)
				|| propertyName.equals(DevQsarConstants.FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50)
				|| propertyName.equals(DevQsarConstants.FORTY_EIGHT_HR_TETRAHYMENA_PYRIFORMIS_IGC50)
				|| propertyName.equals(DevQsarConstants.WATER_SOLUBILITY)) {
			return isRangeWithinLogTolerance(min, max, DevQsarConstants.LOG_RANGE_TOLERANCE,
					DevQsarConstants.ZERO_TOLERANCE);
		} else {
			System.out.println("Missing entry in checkRangeForProperty for " + propertyName);
			return null;
		}
	}

	/**
	 * The median qsar property values have log units for most properties
	 * 
	 * Note: can't just use checkRangeForProperty method because it assumes some of
	 * the properties are not in log units yet when comparing the values
	 * 
	 * TODO should we instead pass the qsar unit name and see if is log units ?
	 * 
	 * @param median1
	 * @param median2
	 * @param propertyName
	 * @return
	 */
	public static Boolean checkMedianQsarValuesForDatapoint(double median1, double median2, String propertyName) {

		if (propertyName.equals(DevQsarConstants.PKA) || propertyName.equals(DevQsarConstants.PKA_A)
				|| propertyName.equals(DevQsarConstants.PKA_B) 
				|| propertyName.equals(DevQsarConstants.LOG_KOW) 
				|| propertyName.equals(DevQsarConstants.LOG_KOA)
				|| propertyName.equals(DevQsarConstants.VAPOR_PRESSURE)
				|| propertyName.equals(DevQsarConstants.SURFACE_TENSION)
				|| propertyName.equals(DevQsarConstants.HENRYS_LAW_CONSTANT)
				|| propertyName.equals(DevQsarConstants.OH)
				|| propertyName.equals(DevQsarConstants.CLINT)
				|| propertyName.equals(DevQsarConstants.KmHL) || propertyName.equals(DevQsarConstants.BCF)
				|| propertyName.equals(DevQsarConstants.KOC) || propertyName.equals(DevQsarConstants.BIODEG_HL_HC)
				|| propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50)
				|| propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_SCUD_LC50)
				|| propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_RAINBOW_TROUT_LC50)
				|| propertyName.equals(DevQsarConstants.NINETY_SIX_HOUR_BLUEGILL_LC50)				
				|| propertyName.equals(DevQsarConstants.FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50)
				|| propertyName.equals(DevQsarConstants.FORTY_EIGHT_HR_TETRAHYMENA_PYRIFORMIS_IGC50)
				|| propertyName.equals(DevQsarConstants.WATER_SOLUBILITY)) {
			return isRangeWithinTolerance(median1, median2, DevQsarConstants.LOG_RANGE_TOLERANCE);
		} else if (propertyName.equals(DevQsarConstants.MELTING_POINT)
				|| propertyName.equals(DevQsarConstants.BOILING_POINT)
				|| propertyName.equals(DevQsarConstants.FLASH_POINT)) {
			return isRangeWithinTolerance(median1, median2, DevQsarConstants.TEMP_RANGE_TOLERANCE);
		} else if (propertyName.equals(DevQsarConstants.DENSITY) 
				|| propertyName.equals(DevQsarConstants.FUB)) {
			return isRangeWithinTolerance(median1, median2, DevQsarConstants.DENSITY_RANGE_TOLERANCE);
		} else if (propertyName.equals(DevQsarConstants.TTR_BINDING)) {
			return isRangeWithinTolerance(median1, median2, 10.0);
		} else if (propertyName.equals(DevQsarConstants.RBIODEG)) {
			return isRangeWithinBinaryTolerance(median1, median2);//TODO does this ever get called?
		} else {
			System.out.println("Missing entry in checkMedianValuesForProperty for " + propertyName);
			return null;
		}
	}

	private static Boolean isRangeWithinBinaryTolerance(double median1, double median2) {
		return median1==median2;
	}

	private static boolean isRangeWithinTolerance(double min, double max, double rangeTolerance) {
		return max - min <= rangeTolerance;
	}

	private static boolean isRangeWithinLogTolerance(double min, double max, double logRangeTolerance,
			double zeroTolerance) {
		if (Math.abs(min) > zeroTolerance) {
			return Math.log10(max / min) <= logRangeTolerance;
		} else {
			return false;
		}
	}

	// Check if property value (in original units) is realistic for property in
	// question
	private static Boolean checkRealisticValueForProperty(Double candidateValue, String propertyName, String unitName,
			DsstoxRecord dr, BoundPropertyValue boundPropertyValue) {

		if (boundPropertyValue != null) {
			if (propertyName.equals(DevQsarConstants.WATER_SOLUBILITY)) {// Needed MW before could convert all to g/L

				if (unitName.equals("G_L")) {
					return boundPropertyValue.test(candidateValue).response;
				} else if (unitName.equals("MOLAR")) {// Need to convert to check
					if (dr.molWeight == null) {
						System.out.println("molWeight missing for " + dr.dsstoxCompoundId);
						return false;
					}
					double candidateValue_G_L = candidateValue * dr.molWeight;
					return boundPropertyValue.test(candidateValue_G_L).response;
				}

			} else {
//				System.out.println(unitName+"\t"+candidateValue);
				return boundPropertyValue.test(candidateValue).response;
			}
		}

//			System.out.println("No bound on "+propertyName+" in checkRealisticValueForProperty");
		return true;
	}

}
