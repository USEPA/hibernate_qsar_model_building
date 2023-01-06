package gov.epa.endpoints.datasets;

import java.util.ArrayList;
import java.util.List;

import gov.epa.databases.dev_qsar.DevQsarConstants;
//import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.endpoints.datasets.DatasetParams.MappingParams;
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer;


public class DatasetCreatorScript {

	//Default settings:
	static String dsstoxMappingId=DevQsarConstants.MAPPING_BY_LIST;
	static boolean isNaive = false;
	static boolean useValidation=true;
	static boolean requireValidation=false;
	static boolean resolveConflicts=true;
	static boolean validateConflictsTogether=true;
	static boolean omitOpsinAmbiguousNames=false;
	static boolean omitUvcbNames=true;
	static boolean omitSalts=true;


	public static void main(String[] args) {

		DatasetServiceImpl ds=new DatasetServiceImpl();
		ds.delete(98);

		//createLogP();
//		createHLC_tmm();		
		// createWS_tmm();
//		createMP();
//		createWS_tmm();
//		createBP();
//		createLogP();
//		createVP();
//		createBCF();
//		create_pKA();
		
		
	}

	public static void createWS_tmm() {
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");

		String propertyName = DevQsarConstants.WATER_SOLUBILITY;
		String listName = "ExpProp_WaterSolubility_WithChemProp_120121";
		
		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		BoundParameterValue pressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		BoundParameterValue phBound = new BoundParameterValue("pH", 6.5, 7.5, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);
		bounds.add(pressureBound);
		bounds.add(phBound);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, null, omitSalts);
		
		String listMappingName = listName+"_TMM";
		String listMappingDescription = "Water solubility with 20 < T (C) < 30, 740 < P (mmHg) < 780, 6.5 < pH < 7.5";
		DatasetParams listMappedParams = new DatasetParams(listMappingName, 
				listMappingDescription, 
				propertyName,
				listMappingParams,
				bounds);
		creator.createPropertyDataset(listMappedParams, false);
		
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

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, null, omitSalts);

		
		String listMappingName = "ExpProp_VP_WithChemProp_070822_TMM";
		String listMappingDescription = "Vapor Pressure with 20 < T (C) < 30";
		DatasetParams listMappedParams = new DatasetParams(listMappingName, 
				listMappingDescription, 
				propertyName,
				listMappingParams,
				bounds);
		creator.createPropertyDataset(listMappedParams, false);


	}
	
	public static void create_pKA() {
		// comment for diff
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");

		String propertyName = DevQsarConstants.PKA;//pKA, pkAa, pkAb- TODO determine which sources use which
		String listName = "ExpProp_"+propertyName+"_1130822";

		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, null, omitSalts);

		
		String listMappingName = listName+"_TMM";
		String listMappingDescription = "pKA with 20 < T (C) < 30";
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

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, listNameArray, omitSalts);

//		String listMappingName = "ExpProp_LogP_WithChemProp_MULTIPLE";
		String listMappingName = "ExpProp_LogP_WithChemProp_TMM";
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
				false, true, false, true, true, false, false, listNameArray,true);
		String listMappingName = "ATTEMPT8 ExpProp_HLC_WithChemProp_071922_MULTIPLE";
		String listMappingDescription = "ATTEMPT8 MULTIPLE LIST Exprop HLC with 20 < T (C) < 30 and 6 < pH < 8";
		DatasetParams listMappedParams = new DatasetParams(listMappingName, 
				listMappingDescription, 
				propertyName,
				listMappingParams,
				bounds);
		creator.createPropertyDataset(listMappedParams, false);


	}


	public static void createHLC_tmm() {
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");

		String propertyName = DevQsarConstants.HENRYS_LAW_CONSTANT;
		// String listName = "ExpProp_HLC_WithChemProp_121421";

		ArrayList<String> listNameArray = new ArrayList<String>();
		listNameArray.add("ExpProp_hlc_WithChemProp_071922_ml_1_to_2000");
		listNameArray.add("ExpProp_hlc_WithChemProp_071922_ml_2001_to_4000");
		listNameArray.add("ExpProp_hlc_WithChemProp_071922_ml_4001_to_4849");

		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		BoundParameterValue phBound = new BoundParameterValue("pH", 6.5, 7.5, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);
		bounds.add(phBound);
				
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, listNameArray, omitSalts);

		String listMappingName = "ExpProp_HLC_TMM";
		
		String listMappingDescription = listMappingName+" with 20 < T (C) < 30 and 6.5 < pH < 7.5";

		DatasetParams listMappedParams = new DatasetParams(listMappingName, 
				listMappingDescription, 
				propertyName,
				listMappingParams,
				bounds);
		creator.createPropertyDataset(listMappedParams, false);

	}

	/**
	 * create melting point dataset based on CAS registration
	 */
	public static void createMPRegisteredCAS() {
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");

		String propertyName = DevQsarConstants.MELTING_POINT;

		BoundParameterValue PressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);

		MappingParams casrnMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_CASRN, null,
				true, false, false, false, false, false, true, null,true);

		String casrnMappingName = "ExpProp_MP_CASRN";
		String casrnMappingDescription = "Exprop MP with 740 < P (mmHg) < 780";
		DatasetParams casrnMappedParams = new DatasetParams(casrnMappingName, 
				casrnMappingDescription, 
				propertyName,
				casrnMappingParams,
				bounds);

		creator.createPropertyDataset(casrnMappedParams, false);
	}
	
	/**
	 * create melting point dataset from expprop records based on list registration
	 */
	public static void createMP() {
		
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");
		
		ArrayList<String> listNameArray = new ArrayList<String>();
		listNameArray.add("ExpProp_MP_WithChemProp_063022_1_to_40000");
		listNameArray.add("ExpProp_MP_WithChemProp_063022_40001_to_80000");
		listNameArray.add("ExpProp_MP_WithChemProp_063022_80001_to_120000");
		listNameArray.add("ExpProp_MP_WithChemProp_063022_120001_to_160000");
		listNameArray.add("ExpProp_MP_WithChemProp_063022_160001_to_200000");
		listNameArray.add("ExpProp_MP_WithChemProp_063022_200001_to_240000");
		listNameArray.add("ExpProp_MP_WithChemProp_063022_240001_to_280000");
		listNameArray.add("ExpProp_MP_WithChemProp_063022_280001_to_320000");
		listNameArray.add("ExpProp_MP_WithChemProp_063022_320001_to_360000");
		listNameArray.add("ExpProp_MP_WithChemProp_063022_360001_to_400000");
		listNameArray.add("ExpProp_MP_WithChemProp_063022_400001_to_427100");

		String propertyName = DevQsarConstants.MELTING_POINT;

		BoundParameterValue PressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, listNameArray, omitSalts);

		String listMappingName = "Standard Melting Point from exp_prop_TMM";
		String listMappingDescription = "Melting Point 740 < P (mmHg) < 780";
		DatasetParams listMappedParams = new DatasetParams(listMappingName, 
				listMappingDescription, 
				propertyName,
				listMappingParams,
				bounds);
		creator.createPropertyDataset(listMappedParams, false);

	}

	/**
	 * creates Boiling point dataset using records from chemreg with CAS registration
	 */
	public static void createBPRegisteredCAS() {
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");

		String propertyName = DevQsarConstants.BOILING_POINT;

		BoundParameterValue PressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);

		MappingParams casrnMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_CASRN, null,
				true, false, false, false, false, false, true, null,true);

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
				true, false, false, false, false, false, true, null,true);

		String casrnMappingName = "ExpProp_LogKOW_CASRN";
		String casrnMappingDescription = "Exprop LogKOW with  20 < T (C) < 30";
		DatasetParams casrnMappedParams = new DatasetParams(casrnMappingName, 
				casrnMappingDescription, 
				propertyName,
				casrnMappingParams,
				bounds);

		creator.createPropertyDataset(casrnMappedParams, false);
	}




	/**
	 * creates Henry's law constant dataset using records from chemreg with CAS registration.
	 */
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
				true, false, false, false, false, false, true, null,true);

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
				false, true, false, true, true, false, false, null,true);
		String listMappingName = "ExpProp BCF Fish_TMM";
		String listMappingDescription = "BCF values for fish with whole body tissue (measured ZEROS OMITTED)";
		DatasetParams listMappedParams = new DatasetParams(listMappingName, 
				listMappingDescription, 
				propertyName,
				listMappingParams,
				null);
		creator.createPropertyDataset(listMappedParams, false);


	}

	
	/**
	 * create boiling point dataset based on chemreg lists
	 */
	public static void createBP() {
		
		ArrayList<String> listNameArray = new ArrayList<String>();
		listNameArray.add("ExpProp_BP_072522_Import_1_to_20000");
		listNameArray.add("ExpProp_BP_072522_Import_20001_to_40000");
		listNameArray.add("ExpProp_BP_072522_Import_40001_to_60000");
		listNameArray.add("ExpProp_BP_072522_Import_60001_to_80000");
		listNameArray.add("ExpProp_BP_072522_Import_80001_to_100000_2");
		listNameArray.add("ExpProp_BP_072522_Import_100001_to_120000");
		listNameArray.add("ExpProp_BP_072522_Import_120001_to_140000");
		listNameArray.add("ExpProp_BP_072522_Import_140001_to_160000_2");
		listNameArray.add("ExpProp_BP_072522_Import_160001_to_180000");
		listNameArray.add("ExpProp_BP_072522_Import_180001_to_200000");
		listNameArray.add("ExpProp_BP_072522_Import_200001_to_220000");
		listNameArray.add("ExpProp_BP_072522_Import_220001_to_240000");
		listNameArray.add("ExpProp_BP_072522_Import_240001_to_260000");
		listNameArray.add("ExpProp_BP_072522_Import_260001_to_280000");
		listNameArray.add("ExpProp_BP_072522_Import_280001_to_300000");
		listNameArray.add("ExpProp_BP_072522_Import_300001_to_320000");
		listNameArray.add("ExpProp_BP_072522_Import_320001_to_340000");
		listNameArray.add("ExpProp_BP_072522_Import_340001_to_360000");
		listNameArray.add("ExpProp_BP_072522_Import_360001_to_363160");
		
		
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");

		String propertyName = DevQsarConstants.BOILING_POINT;

		BoundParameterValue PressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, listNameArray, omitSalts);



		String listMappingName = "Standard Boiling Point from exp_prop_TMM";
		String listMappingDescription = "Boiling Point 740 < P (mmHg) < 780";
		DatasetParams listMappedParams = new DatasetParams(listMappingName, 
				listMappingDescription, 
				propertyName,
				listMappingParams,
				bounds);
		creator.createPropertyDataset(listMappedParams, false);

	}

}
