package gov.epa.endpoints.datasets;

import java.util.ArrayList;
import java.util.List;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.datasets.DatasetParams.MappingParams;
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer;


public class DatasetCreatorScript {

	public static void main(String[] args) {
		createVP();
	}

	// methods like these 
	public static void createVP() {
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");
		
		String propertyName = DevQsarConstants.VAPOR_PRESSURE;
		String listName = "ExpProp_VP_WithChemProp_070822";
		
		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, 
				false, true, false, true, true, false, false, null);
		String listMappingName = "ExpProp_VP_WithChemProp_070822";
		String listMappingDescription = "Vapor Pressure with 20 < T (C) < 30";
		DatasetParams listMappedParams = new DatasetParams(listMappingName, 
				listMappingDescription, 
				propertyName,
				listMappingParams,
				bounds);
		creator.createPropertyDataset(listMappedParams, false);
		

	}
	

}
