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

	
	private PropertyValueCreator pvc;

	public ParameterValueCreator(PropertyValueCreator pvc) {
		this.pvc=pvc;
	}
	
	
	
	/**
	 * This method assumes there are explicit temperature_C, pressure_mmHg, and pH fields in ExperimentalRecord

	 * @param rec
	 * @param propertyValue
	 */
	private void addPhyschemParameterValues(ExperimentalRecord rec,PropertyValue propertyValue) {

		ParameterValue pressureValue = getPressureValue(rec);
		if (pressureValue!=null) {
			pressureValue.setPropertyValue(propertyValue);
			pressureValue.setParameter(pvc.parametersMap.get("Pressure"));
			pressureValue.setUnit(pvc.unitsMap.get(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.MMHG)));
			//				QueryExpPropDb.postParameterValue(expPropDbUrl, pressureValue);
			propertyValue.addParameterValue(pressureValue);
		}

		//*******************************************************************************************************

		ParameterValue temperatureValue = getTemperatureValue(rec);
		if (temperatureValue!=null) {
			temperatureValue.setPropertyValue(propertyValue);
			temperatureValue.setParameter(pvc.parametersMap.get("Temperature"));
			temperatureValue.setUnit(pvc.unitsMap.get(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.DEG_C)));
			//				QueryExpPropDb.postParameterValue(expPropDbUrl, temperatureValue);
			propertyValue.addParameterValue(temperatureValue);
		}

		//*******************************************************************************************************

		ParameterValue phValue = getPhValue(rec);

		if (phValue!=null) {
			phValue.setPropertyValue(propertyValue);
			phValue.setParameter(pvc.parametersMap.get("pH"));
			phValue.setUnit(pvc.unitsMap.get(DevQsarConstants.getConstantNameByReflection(DevQsarConstants.LOG_UNITS)));
			//				QueryExpPropDb.postParameterValue(expPropDbUrl, phValue);
			propertyValue.addParameterValue(phValue);
		}

		//*******************************************************************************************************

		ParameterValue measurementMethodValue = getMeasurementMethodValue(rec);

		if (measurementMethodValue!=null) {
			measurementMethodValue.setPropertyValue(propertyValue);
			measurementMethodValue.setParameter(pvc.parametersMap.get("Measurement method"));
			measurementMethodValue.setUnit(pvc.unitsMap.get("TEXT"));
			//				QueryExpPropDb.postParameterValue(expPropDbUrl, measurementMethodValue);
			propertyValue.addParameterValue(measurementMethodValue);
		}
	}

	
	private ParameterValue getMeasurementMethodValue(ExperimentalRecord rec) {
		if (rec.measurement_method!=null) {
			ParameterValue measurementMethodValue = new ParameterValue();
			measurementMethodValue.setCreatedBy(pvc.lanId);
			measurementMethodValue.setValueText(rec.measurement_method);
//			System.out.println(measurementMethodValue.getValueText());
			return measurementMethodValue;
		} else {
			return null;
		}
	}

	
	private ParameterValue getPressureValue(ExperimentalRecord rec) {
		if (rec.pressure_mmHg!=null) {
			ParameterValue pressureValue = new ParameterValue();
			pressureValue.setCreatedBy(pvc.lanId);
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

		
		if(columnContents.isBlank()) {
//			System.out.println("blank parameter value");
			return false;
		}
		
		Matcher matcher = PropertyValueCreator.STRING_COLUMN_PATTERN.matcher(columnContents);

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
			temperatureValue.setCreatedBy(pvc.lanId);
			temperatureValue.setValuePointEstimate(rec.temperature_C);
			return temperatureValue;
		} else {
			return null;
		}
	}
//		if (rec.temperature_C=null) {
//			ParameterValue pressureValue = new ParameterValue();
//			pressureValue.setCreatedBy(loader.lanId);
//			if (parseStringColumn(rec.temperature_C, pressureValue)) {
//				return pressureValue;
//			} else {
//				return null;
//			}
//		} else {
//			return null;
//		}
//	}

	private ParameterValue getPhValue(ExperimentalRecord rec) {
		if (rec.pH!=null) {
			ParameterValue phValue = new ParameterValue();
			phValue.setCreatedBy(pvc.lanId);
			
			if (parseStringColumn(rec.pH, phValue)) {
				return phValue;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}


	private void addToxParameterValues(ExperimentalRecord rec,PropertyValue propertyValue) {
		
		//could also just store these values in rec rather than constructing from the the propertyName or make the propertyName very specific
		
		ParameterValue speciesValue = getSpeciesValue(rec);
		if (speciesValue!=null) {
			speciesValue.setPropertyValue(propertyValue);
			speciesValue.setParameter(pvc.parametersMap.get("Species"));
			speciesValue.setUnit(pvc.unitsMap.get("Text"));
			propertyValue.addParameterValue(speciesValue);
		}

		ParameterValue adminRouteValue = getAdminRouteValue(rec);

		if (adminRouteValue!=null) {
			adminRouteValue.setPropertyValue(propertyValue);
			adminRouteValue.setParameter(pvc.parametersMap.get("Route of administration"));
			adminRouteValue.setUnit(pvc.unitsMap.get("Text"));
			propertyValue.addParameterValue(adminRouteValue);
		}
		
		ParameterValue purityValue = getPurityValue(rec);
		
		if (purityValue!=null) {
			purityValue.setPropertyValue(propertyValue);
			purityValue.setParameter(pvc.parametersMap.get("Purity"));
			purityValue.setUnit(pvc.unitsMap.get("%"));
			propertyValue.addParameterValue(purityValue);
			System.out.println(purityValue.generateConciseValueString());
		}
	}
	
	private ParameterValue getSpeciesValue(ExperimentalRecord rec) {
		if (rec.property_name!=null) {
			ParameterValue speciesValue = new ParameterValue();
			speciesValue.setCreatedBy(pvc.lanId);
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
			adminRouteValue.setCreatedBy(pvc.lanId);
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
			purityValue.setCreatedBy(pvc.lanId);
			if (parseStringColumn(rec.note, purityValue)) {
				return purityValue;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public  boolean addParameters(String type, ExperimentalRecord rec, PropertyValue pv) {
		
		if (type.equals(ExperimentalRecordLoader.typePhyschem)) {
			addPhyschemParameterValues(rec, pv);
		} else if (type.equals(ExperimentalRecordLoader.typeTox)) {
			addToxParameterValues(rec, pv);
		} else {
			//typeOther: dont need to pull parameters from fields in rec
		}
		
		setReliabilityValue(rec,pv);
		
		addGenericParametersValues(rec,pv);
		
		addParametersValues(rec, pv);
				
		if(pv.getParameterValues()!=null) {
			for (ParameterValue paramValue : pv.getParameterValues()) {
				if(paramValue.getUnit()==null) {
					//TMM bail right away if we have a parameter missing a unit:
					System.out.println("Missing parameter unit for "+paramValue.getParameter().getName());
					return false;
				}
			}
		}

		return true;
		
	}
	
	
	public void setReliabilityValue(ExperimentalRecord rec,PropertyValue pv) {

		if (rec.reliability!=null) {
			ParameterValue reliabilityValue = new ParameterValue();
			reliabilityValue.setCreatedBy(pvc.lanId);
			reliabilityValue.setValueText(rec.reliability);
			reliabilityValue.setPropertyValue(pv);
			reliabilityValue.setParameter(pvc.parametersMap.get("Reliability"));
			reliabilityValue.setUnit(pvc.unitsMap.get("TEXT"));
			pv.addParameterValue(reliabilityValue);
		} 
	}

	
	/**
	 * Get parameters from experimental_parameters hashtable in ExperimentalRecord
	 * TODO these properties wont post unless the parameters are in properties_acceptable_parameters for the given property
	 * 
	 * @param rec
	 */
	private void addGenericParametersValues(ExperimentalRecord rec,PropertyValue propertyValue) {

		if (rec.experimental_parameters==null) return;


		for (String parameterName:rec.experimental_parameters.keySet()) {

			Object value=rec.experimental_parameters.get(parameterName);

			//				System.out.println(parameterName+"\t"+value+"\t"+value.getClass().getName());


			ParameterValue parameterValue = new ParameterValue();
			parameterValue.setCreatedBy(pvc.lanId);

			if (value instanceof Double) {
				parameterValue.setValuePointEstimate((Double)value);
			} else if (value instanceof String) {
				parameterValue.setValueText((String)value);	
			}

//			System.out.println(parameterName);
			
			Parameter parameter=pvc.parametersMap.get(parameterName);
			
			if(parameter==null) {
				System.out.println("Missing "+parameterName+" in parameters table");
				return;
			}
			
			
//			System.out.println(parameterName);
			
			ExpPropUnit unit=pvc.unitsMap.get("TEXT");//assume it's text otherwise we need store detailed parameter value objects in the experimental record json

			parameterValue.setPropertyValue(propertyValue);
			parameterValue.setParameter(parameter);//need to add parameter to Parameters table first
			parameterValue.setUnit(unit);
			propertyValue.addParameterValue(parameterValue);
			
//			System.out.println(parameter.getName()+"\t"+parameterValue.getUnit().getName());
			

		}

		//			System.out.println("done add generic params");

	}
	
	/**
	 * Get parameters from the parameter_values list in ExperimentalRecord
	 * 
	 * @param rec
	 * @param propertyValue
	 */
	private void addParametersValues(ExperimentalRecord rec,PropertyValue propertyValue) {

		if (rec.parameter_values==null) return;

		for (ParameterValue parameterValue:rec.parameter_values) {

			parameterValue.setCreatedBy(pvc.lanId);
			Parameter parameter=pvc.parametersMap.get(parameterValue.getParameter().getName());
			
			if(parameter==null) {
				System.out.println("Missing "+parameterValue.getParameter().getName()+" in parameters table");
				return;
			}
			
			String unitName=DevQsarConstants.getConstantNameByReflection(parameterValue.getUnit().getAbbreviation());
			
			ExpPropUnit unit=pvc.unitsMap.get(unitName);
			
			if(unit==null) {
				System.out.println("Missing unit abbrev "+parameterValue.getUnit().getName()+" in units table");
				return;
			}
			
			parameterValue.setPropertyValue(propertyValue);
			parameterValue.setParameter(parameter);//need to add parameter to Parameters table first
			parameterValue.setUnit(unit);
			propertyValue.addParameterValue(parameterValue);
//			System.out.println(parameter.getName()+"\t"+parameterValue.getUnit().getName());
		}
		
		//TODO should ParameterValue have been stored in experimental_parameters all along?
		
		//Store parameter values in experimental_parameters so as to not mess up serialized to json of loaded records:
		if(rec.parameter_values!=null) {
			for (int i=0;i<rec.parameter_values.size();i++) {
				ParameterValue parameterValue=rec.parameter_values.get(i);
				String strValue=parameterValue.getValuePointEstimate()+" "+parameterValue.getUnit().getAbbreviation();
				rec.experimental_parameters.put(parameterValue.getParameter().getName(),strValue);
			}
			rec.parameter_values=null;
		}

	}
	

}
