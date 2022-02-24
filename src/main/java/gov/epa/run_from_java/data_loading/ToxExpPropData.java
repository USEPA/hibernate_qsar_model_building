package gov.epa.run_from_java.data_loading;

import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue;

public class ToxExpPropData extends ExpPropData {

	ToxExpPropData(ExperimentalRecordLoader experimentalRecordLoader) {
		super(experimentalRecordLoader);
		// TODO Auto-generated constructor stub
	}
	
	public ParameterValue speciesValue;
	public ParameterValue adminRouteValue;
	public ParameterValue purityValue;
	
	public void getValues(ExperimentalRecord rec) {
		super.getValues(rec);
		speciesValue = getSpeciesValue(rec);
		adminRouteValue = getAdminRouteValue(rec);
		purityValue = getPurityValue(rec);
	}
	
	public void constructPropertyValue() {
		super.constructPropertyValue();
		addToxParameterValues();
	}
	
	private void addToxParameterValues() {
		if (speciesValue!=null) {
			speciesValue.setPropertyValue(propertyValue);
			speciesValue.setParameter(loader.parametersMap.get("Species"));
			speciesValue.setUnit(loader.unitsMap.get("Text"));
			propertyValue.addParameterValue(speciesValue);
		}
		
		if (adminRouteValue!=null) {
			adminRouteValue.setPropertyValue(propertyValue);
			adminRouteValue.setParameter(loader.parametersMap.get("Route of administration"));
			adminRouteValue.setUnit(loader.unitsMap.get("Text"));
			propertyValue.addParameterValue(adminRouteValue);
		}
		
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
			if (ExpPropData.parseStringColumn(rec.note, purityValue)) {
				return purityValue;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

}
