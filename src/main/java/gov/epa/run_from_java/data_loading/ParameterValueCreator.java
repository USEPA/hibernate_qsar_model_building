package gov.epa.run_from_java.data_loading;

import java.util.regex.Matcher;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;
import gov.epa.databases.dev_qsar.exp_prop.entity.Parameter;
import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;

/**
* @author TMARTI02
*/
public class ParameterValueCreator {

	
	private ExperimentalRecordLoader loader;

	public ParameterValueCreator(ExperimentalRecordLoader experimentalRecordLoader) {
		this.loader=experimentalRecordLoader;
	}
	
	/**
	 * This method assumes there are explicit temperature_C, pressure_mmHg, and pH fields in ExperimentalRecord

	 * @param rec
	 * @param propertyValue
	 */
	public void addPhyschemParameterValues(ExperimentalRecord rec,PropertyValue propertyValue) {

		ParameterValue pressureValue = getPressureValue(rec);
		if (pressureValue!=null) {
			pressureValue.setPropertyValue(propertyValue);
			pressureValue.setParameter(loader.parametersMap.get("Pressure"));
			pressureValue.setUnit(loader.unitsMap.get(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.MMHG)));
			//				QueryExpPropDb.postParameterValue(expPropDbUrl, pressureValue);
			propertyValue.addParameterValue(pressureValue);
		}

		//*******************************************************************************************************

		ParameterValue temperatureValue = getTemperatureValue(rec);
		if (temperatureValue!=null) {
			temperatureValue.setPropertyValue(propertyValue);
			temperatureValue.setParameter(loader.parametersMap.get("Temperature"));
			temperatureValue.setUnit(loader.unitsMap.get(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.DEG_C)));
			//				QueryExpPropDb.postParameterValue(expPropDbUrl, temperatureValue);
			propertyValue.addParameterValue(temperatureValue);
		}

		//*******************************************************************************************************

		ParameterValue phValue = getPhValue(rec);

		if (phValue!=null) {
			phValue.setPropertyValue(propertyValue);
			phValue.setParameter(loader.parametersMap.get("pH"));
			phValue.setUnit(loader.unitsMap.get(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.LOG_UNITS)));
			//				QueryExpPropDb.postParameterValue(expPropDbUrl, phValue);
			propertyValue.addParameterValue(phValue);
		}

		//*******************************************************************************************************

		ParameterValue measurementMethodValue = getMeasurementMethodValue(rec);

		if (measurementMethodValue!=null) {
			measurementMethodValue.setPropertyValue(propertyValue);
			measurementMethodValue.setParameter(loader.parametersMap.get("Measurement method"));
			measurementMethodValue.setUnit(loader.unitsMap.get("Text"));
			//				QueryExpPropDb.postParameterValue(expPropDbUrl, measurementMethodValue);
			propertyValue.addParameterValue(measurementMethodValue);
		}
	}

	
	private ParameterValue getMeasurementMethodValue(ExperimentalRecord rec) {
		if (rec.measurement_method!=null) {
			ParameterValue measurementMethodValue = new ParameterValue();
			measurementMethodValue.setCreatedBy(loader.lanId);
			measurementMethodValue.setValueText(rec.measurement_method);
			System.out.println(measurementMethodValue.getValueText());
			return measurementMethodValue;
		} else {
			return null;
		}
	}

	
	private ParameterValue getPressureValue(ExperimentalRecord rec) {
		if (rec.pressure_mmHg!=null) {
			ParameterValue pressureValue = new ParameterValue();
			pressureValue.setCreatedBy(loader.lanId);
			if (parseStringColumn(rec.pressure_mmHg, pressureValue)) {
				return pressureValue;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	private static boolean parseStringColumn(String columnContents, ParameterValue value) {

		Matcher matcher = ExperimentalRecordLoader.STRING_COLUMN_PATTERN.matcher(columnContents);

		if (matcher.find()) {
			String qualifier = matcher.group(1);
			String double1 = matcher.group(2);
			String isRange = matcher.group(3);
			String double2 = matcher.group(4);

			try {
				value.setValueQualifier(qualifier);
				if (isRange!=null) {
					value.setValueMin(Double.parseDouble(double1));
					value.setValueMax(Double.parseDouble(double2));
				} else {
					value.setValuePointEstimate(Double.parseDouble(double1));
				}
			} catch (Exception e) {
				return false;
			}

			return true;
		} else {
			System.out.println("Warning: Failed to parse parameter value from: " + columnContents);
			return false;
		}
	}


	private ParameterValue getTemperatureValue(ExperimentalRecord rec) {
		if (rec.temperature_C!=null) {
			ParameterValue temperatureValue = new ParameterValue();
			temperatureValue.setCreatedBy(loader.lanId);
			temperatureValue.setValuePointEstimate(rec.temperature_C);
			return temperatureValue;
		} else {
			return null;
		}
	}

	private ParameterValue getPhValue(ExperimentalRecord rec) {
		if (rec.pH!=null) {
			ParameterValue phValue = new ParameterValue();
			phValue.setCreatedBy(loader.lanId);
			
			if (parseStringColumn(rec.pH, phValue)) {
				return phValue;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}


	public void addToxParameterValues(ExperimentalRecord rec,PropertyValue propertyValue) {
		
		//could also just store these values in rec rather than constructing from the the propertyName or make the propertyName very specific
		
		ParameterValue speciesValue = getSpeciesValue(rec);
		if (speciesValue!=null) {
			speciesValue.setPropertyValue(propertyValue);
			speciesValue.setParameter(loader.parametersMap.get("Species"));
			speciesValue.setUnit(loader.unitsMap.get("Text"));
			propertyValue.addParameterValue(speciesValue);
		}

		ParameterValue adminRouteValue = getAdminRouteValue(rec);

		if (adminRouteValue!=null) {
			adminRouteValue.setPropertyValue(propertyValue);
			adminRouteValue.setParameter(loader.parametersMap.get("Route of administration"));
			adminRouteValue.setUnit(loader.unitsMap.get("Text"));
			propertyValue.addParameterValue(adminRouteValue);
		}
		
		ParameterValue purityValue = getPurityValue(rec);
		
		if (purityValue!=null) {
			purityValue.setPropertyValue(propertyValue);
			purityValue.setParameter(loader.parametersMap.get("Purity"));
			purityValue.setUnit(loader.unitsMap.get("%"));
			propertyValue.addParameterValue(purityValue);
			System.out.println(purityValue.generateConciseValueString());
		}
	}
	
	private ParameterValue getSpeciesValue(ExperimentalRecord rec) {
		if (rec.property_name!=null) {
			ParameterValue speciesValue = new ParameterValue();
			speciesValue.setCreatedBy(loader.lanId);
			if (rec.property_name.equals("SkinSensitizationLLNA")) {
				speciesValue.setValueText("Mouse");
			} else if (rec.property_name.startsWith("guinea_pig_")) {
				speciesValue.setValueText("Guinea pig");
			} else if (rec.property_name.startsWith("mouse_")) {
				speciesValue.setValueText("Mouse");
			} else if (rec.property_name.startsWith("rabbit_")) {
				speciesValue.setValueText("Rabbit");
			} else if (rec.property_name.startsWith("rat_")) {
				speciesValue.setValueText("Rat");
			} else {
				return null;
			}
			return speciesValue;
		} else {
			return null;
		}
	}
	
	private ParameterValue getAdminRouteValue(ExperimentalRecord rec) {
		if (rec.property_name!=null) {
			ParameterValue adminRouteValue = new ParameterValue();
			adminRouteValue.setCreatedBy(loader.lanId);
			if (rec.property_name.contains("_skin_")) {
				adminRouteValue.setValueText("Dermal");
			} else if (rec.property_name.contains("_oral_")) {
				adminRouteValue.setValueText("Oral");
			} else if (rec.property_name.contains("_inhalation_")) {
				adminRouteValue.setValueText("Inhalation");
			} else {
				return null;
			}
			return adminRouteValue;
		} else {
			return null;
		}
	}
	
	private ParameterValue getPurityValue(ExperimentalRecord rec) {
		if (rec.note!=null && rec.note.startsWith("Purity: ")) {
			ParameterValue purityValue = new ParameterValue();
			purityValue.setCreatedBy(loader.lanId);
			if (parseStringColumn(rec.note, purityValue)) {
				return purityValue;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * TODO these properties wont post unless the parameters are in properties_acceptable_parameters for the given property
	 * 
	 * @param rec
	 */
	public void addGenericParametersValues(ExperimentalRecord rec,PropertyValue propertyValue) {

		if (rec.experimental_parameters==null) return;


		for (String parameterName:rec.experimental_parameters.keySet()) {

			Object value=rec.experimental_parameters.get(parameterName);

			//				System.out.println(parameterName+"\t"+value+"\t"+value.getClass().getName());


			ParameterValue parameterValue = new ParameterValue();
			parameterValue.setCreatedBy(loader.lanId);

			if (value instanceof Double) {
				parameterValue.setValuePointEstimate((Double)value);
			} else if (value instanceof String) {
				parameterValue.setValueText((String)value);	
			}

			Parameter parameter=loader.parametersMap.get(parameterName);
			ExpPropUnit unit=loader.unitsMap.get("Text");//TODO how do we handle generic unit that has specific units? add units to experimental_parameters dictionary for each entry? or add handle like temperature or pressure was

			parameterValue.setPropertyValue(propertyValue);
			parameterValue.setParameter(parameter);//need to add parameter to Parameters table first
			parameterValue.setUnit(unit);
			propertyValue.addParameterValue(parameterValue);

		}

		//			System.out.println("done add generic params");

	}
}
