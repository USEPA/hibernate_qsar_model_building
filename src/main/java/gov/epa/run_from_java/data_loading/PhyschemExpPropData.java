package gov.epa.run_from_java.data_loading;

import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue;

public class PhyschemExpPropData extends ExpPropData {

	PhyschemExpPropData(ExperimentalRecordLoader experimentalRecordLoader) {
		super(experimentalRecordLoader);
	}
	
	public void getValues(ExperimentalRecord rec) {
		super.getValues(rec);
		addPhyschemParameterValues(rec);
	}
	
	
	private void addPhyschemParameterValues(ExperimentalRecord rec) {
		
		ParameterValue pressureValue = getPressureValue(rec);
		if (pressureValue!=null) {
			pressureValue.setPropertyValue(propertyValue);
			pressureValue.setParameter(loader.parametersMap.get("Pressure"));
			pressureValue.setUnit(loader.unitsMap.get("mmHg"));
//			QueryExpPropDb.postParameterValue(expPropDbUrl, pressureValue);
			propertyValue.addParameterValue(pressureValue);
		}

		//*******************************************************************************************************
		
		ParameterValue temperatureValue = getTemperatureValue(rec);
		if (temperatureValue!=null) {
			temperatureValue.setPropertyValue(propertyValue);
			temperatureValue.setParameter(loader.parametersMap.get("Temperature"));
			temperatureValue.setUnit(loader.unitsMap.get("C"));
//			QueryExpPropDb.postParameterValue(expPropDbUrl, temperatureValue);
			propertyValue.addParameterValue(temperatureValue);
		}

		//*******************************************************************************************************

		ParameterValue phValue = getPhValue(rec);

		if (phValue!=null) {
			phValue.setPropertyValue(propertyValue);
			phValue.setParameter(loader.parametersMap.get("pH"));
			phValue.setUnit(loader.unitsMap.get("Log units"));
//			QueryExpPropDb.postParameterValue(expPropDbUrl, phValue);
			propertyValue.addParameterValue(phValue);
		}
		
		//*******************************************************************************************************

		ParameterValue measurementMethodValue = getMeasurementMethodValue(rec);

		if (measurementMethodValue!=null) {
			measurementMethodValue.setPropertyValue(propertyValue);
			measurementMethodValue.setParameter(loader.parametersMap.get("Measurement method"));
			measurementMethodValue.setUnit(loader.unitsMap.get("Text"));
//			QueryExpPropDb.postParameterValue(expPropDbUrl, measurementMethodValue);
			propertyValue.addParameterValue(measurementMethodValue);
		}
	}

	private ParameterValue getPressureValue(ExperimentalRecord rec) {
		if (rec.pressure_mmHg!=null) {
			ParameterValue pressureValue = new ParameterValue();
			pressureValue.setCreatedBy(loader.lanId);
			if (ExpPropData.parseStringColumn(rec.pressure_mmHg, pressureValue)) {
				return pressureValue;
			} else {
				return null;
			}
		} else {
			return null;
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
			if (ExpPropData.parseStringColumn(rec.pH, phValue)) {
				return phValue;
			} else {
				return null;
			}
		} else {
			return null;
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

}
