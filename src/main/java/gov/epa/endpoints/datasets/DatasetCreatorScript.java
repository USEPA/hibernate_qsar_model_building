package gov.epa.endpoints.datasets;

import java.util.ArrayList;
import java.util.List;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.datasets.DatasetParams.MappingParams;
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer;


public class DatasetCreatorScript {

	public static void main(String[] args) {
		createLogP();
	}
	

	// methods like these 
	public static void createVP() {
		// comment for diff
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
	
	public static void createLogP() {
		// comment for diff
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");
		
		String propertyName = DevQsarConstants.LOG_KOW;
		// String listName = "ExpProp_HLC_WithChemProp_121421";
		
		ArrayList<String> listNameArray = new ArrayList<String>();
		listNameArray.add("ExpProp_LogP_import_1_to_20000");
		listNameArray.add("ExpProp_LogP_import_20001_to_40000");
		listNameArray.add("ExpProp_LogP_import_40001_to_60000");
		listNameArray.add("ExpProp_LogP_import_60001_to_80000");
		listNameArray.add("ExpProp_LogP_import_80001_to_100000");
		listNameArray.add("ExpProp_LogP_import_100001_to_100850");

		BoundParameterValue PressureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);

		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, 
				false, true, false, true, true, false, false, listNameArray);
		
		
		String listMappingName = "ExpProp_LogP_WithChemProp_MULTIPLE";
		String listMappingDescription = "MULTIPLE LIST Exprop LogP with 20.0 < T (C) < 30.0";
		DatasetParams listMappedParams = new DatasetParams(listMappingName, 
				listMappingDescription, 
				propertyName,
				listMappingParams,
				bounds);
		creator.createPropertyDataset(listMappedParams, false);
		

	}
	
	
	// methods like these 
	public static void createHLC() {
		// comment for diff
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");
		
		String propertyName = DevQsarConstants.HENRYS_LAW_CONSTANT;
		// String listName = "ExpProp_HLC_WithChemProp_121421";
		
		ArrayList<String> listNameArray = new ArrayList<String>();
		listNameArray.add("ExpProp_hlc_WithChemProp_071922_ml_1_to_2000");
		listNameArray.add("ExpProp_hlc_WithChemProp_071922_ml_2001_to_4000");
		listNameArray.add("ExpProp_hlc_WithChemProp_071922_ml_4001_to_4849");

		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		BoundParameterValue phBound = new BoundParameterValue("pH", 6.0, 8.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);
		bounds.add(phBound);
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, 
				false, true, false, true, true, false, false, listNameArray);
		String listMappingName = "ATTEMPT8 ExpProp_HLC_WithChemProp_071922_MULTIPLE";
		String listMappingDescription = "ATTEMPT8 MULTIPLE LIST Exprop HLC with 20 < T (C) < 30 and 6 < pH < 8";
		DatasetParams listMappedParams = new DatasetParams(listMappingName, 
				listMappingDescription, 
				propertyName,
				listMappingParams,
				bounds);
		creator.createPropertyDataset(listMappedParams, false);
		

	}
	
	public static void createMPRegisteredCAS() {
		// comment for diff
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");
		
		String propertyName = DevQsarConstants.MELTING_POINT;

		BoundParameterValue PressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);


		
		MappingParams casrnMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_CASRN, null,
				true, false, false, false, false, false, true, null);
		
		String casrnMappingName = "ExpProp_MP_CASRN";
		String casrnMappingDescription = "Exprop HLC with 740 < P (mmHg) < 780";
		DatasetParams casrnMappedParams = new DatasetParams(casrnMappingName, 
				casrnMappingDescription, 
				propertyName,
				casrnMappingParams,
				bounds);

		creator.createPropertyDataset(casrnMappedParams, false);
	}
	
	public static void createBPRegisteredCAS() {
		// comment for diff
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");
		
		String propertyName = DevQsarConstants.BOILING_POINT;

		BoundParameterValue PressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);


		
		MappingParams casrnMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_CASRN, null,
				true, false, false, false, false, false, true, null);
		
		String casrnMappingName = "ExpProp_BP_CASRN";
		String casrnMappingDescription = "Exprop BP with 740 < P (mmHg) < 780";
		DatasetParams casrnMappedParams = new DatasetParams(casrnMappingName, 
				casrnMappingDescription, 
				propertyName,
				casrnMappingParams,
				bounds);

		creator.createPropertyDataset(casrnMappedParams, false);
	}

	
	
	public static void createLogPRegisteredCAS() {
		// comment for diff
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");
		
		String propertyName = DevQsarConstants.LOG_KOW;

		BoundParameterValue PressureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);


		
		MappingParams casrnMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_CASRN, null,
				true, false, false, false, false, false, true, null);
		
		String casrnMappingName = "ExpProp_LogKOW_CASRN";
		String casrnMappingDescription = "Exprop LogKOW with  20 < T (C) < 30";
		DatasetParams casrnMappedParams = new DatasetParams(casrnMappingName, 
				casrnMappingDescription, 
				propertyName,
				casrnMappingParams,
				bounds);

		creator.createPropertyDataset(casrnMappedParams, false);
	}


	
	
	public static void createHLCRegisteredCAS() {
		// comment for diff
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");
		
		String propertyName = DevQsarConstants.HENRYS_LAW_CONSTANT;

		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		BoundParameterValue phBound = new BoundParameterValue("pH", 6.0, 8.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);
		bounds.add(phBound);

		
		MappingParams casrnMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_CASRN, null,
				true, false, false, false, false, false, true, null);
		
		String casrnMappingName = "ExpProp_HLC_CASRN";
		String casrnMappingDescription = "Exprop HLC with 20 < T (C) < 30 and 6 < pH < 8";
		DatasetParams casrnMappedParams = new DatasetParams(casrnMappingName, 
				casrnMappingDescription, 
				propertyName,
				casrnMappingParams,
				bounds);

		creator.createPropertyDataset(casrnMappedParams, false);
		

	}
	
	public static void createBCF() {
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");
		
		String propertyName = DevQsarConstants.LOG_BCF_FISH_WHOLEBODY;
		String listName = "ExpProp_bcfwbf_072222";
		
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, 
				false, true, false, true, true, false, false, null);
		String listMappingName = "ExpProp BCF Fish WholeBody zeros omitted";
		String listMappingDescription = "BCF values for fish with whole body tissue measured ZEROS OMITTED";
		DatasetParams listMappedParams = new DatasetParams(listMappingName, 
				listMappingDescription, 
				propertyName,
				listMappingParams,
				null);
		creator.createPropertyDataset(listMappedParams, false);
		

	}
	
	public static void createBP() {
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");
		
		String propertyName = DevQsarConstants.BOILING_POINT;
		String listName = "ExpProp_BP_072522";
		
		BoundParameterValue PressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);

		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, 
				false, true, false, true, true, false, false, null);
		String listMappingName = "Standard Boiling Point from exp_prop";
		String listMappingDescription = "Melting Point 740 < P (mmHg) < 780";
		DatasetParams listMappedParams = new DatasetParams(listMappingName, 
				listMappingDescription, 
				propertyName,
				listMappingParams,
				bounds);
		creator.createPropertyDataset(listMappedParams, false);
		

	}
	
	public static void createbcf2() {
		System.out.println("eclipse recognizes new code2");

		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");
		
		String propertyName = "LogBCF_Fish_WholeBody";
		String listName = "bcf_list";
		
		/*
		ArrayList<String> chemicalsLists = new ArrayList<String>();
		chemicalsLists.add("ExpProp_HLC_WithChemProp_073022_4001_to_4849");
		chemicalsLists.add("ExpProp_HLC_WithChemProp_073022_2001_to_4000");
		chemicalsLists.add("ExpProp_HLC_WithChemProp_073022_1_to_2000");
		*/
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, 
				false, true, false, true, true, false, false, null);
		MappingParams casrnMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_CASRN, null,
				true, false, false, false, false, false, true, null);
		String listMappingName = "ExpProp_BCFWHOLEBODYFISH_070822";
		String casrnMappingName = "CASRN mapping of " + propertyName + " from exp_prop, without eChemPortal";
		String listMappingDescription = "ExpProp_BCFWHOLEBODYFISH_070822 for fish whole body";
		String casrnMappingDescription = propertyName + " with species = Mouse, mapped by CASRN";
		DatasetParams casrnMappedParams = new DatasetParams(casrnMappingName, 
				casrnMappingDescription, 
				propertyName,
				casrnMappingParams,
				null);
		DatasetParams listMappedParams = new DatasetParams(listMappingName, 
				listMappingDescription, 
				propertyName,
				listMappingParams,
				null);

//		creator.createPropertyDataset(casrnMappedParams);
		creator.createPropertyDataset(listMappedParams, false);
		

	}
	


	

}
