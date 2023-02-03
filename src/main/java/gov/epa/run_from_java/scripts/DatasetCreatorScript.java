package gov.epa.run_from_java.scripts;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
//import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.endpoints.datasets.BoundParameterValue;
import gov.epa.endpoints.datasets.DatasetCreator;
import gov.epa.endpoints.datasets.DatasetParams;
import gov.epa.endpoints.datasets.DatasetParams.MappingParams;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
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

//		getDatasetStats();//Get record counts for the papers
//		getDatasetStatsUsingSql();//Get record counts for the papers
//		getDatasetStatsForOneDataset();
		createPKAb();
//		DatasetServiceImpl ds=new DatasetServiceImpl();
//		ds.delete(112);

		
//		createHLC();//done		
//		createVP();//done
//		createWS();//done
//		createLogP();//done
//		createBCF();//done
//		createMP();//done
//		createBP();
//		

		
		//TODO recreate WS dataset now that keep was set to false based on manual check
//		create_pKA();
//		createBP_exclude_LookChem();
		
		
	}
	
	static void getDatasetStats() {

		System.out.println("propertyName\tRaw*\tDSSTox Mapped\tDataset");

		List<String>propertyNames=new ArrayList<>();
		List<String>datasetNames=new ArrayList<>();

//		propertyNames.add(DevQsarConstants.MELTING_POINT);
//		datasetNames.add("MP_from_exp_prop_and_chemprop");

		propertyNames.add(DevQsarConstants.BOILING_POINT);
		datasetNames.add("BP_from_exp_prop_and_chemprop");
		
//		propertyNames.add(DevQsarConstants.WATER_SOLUBILITY);
//		datasetNames.add("WS_from_exp_prop_and_chemprop");
//
//		propertyNames.add(DevQsarConstants.LOG_KOW);
//		datasetNames.add("LogP_from_exp_prop_and_chemprop");
//		
//		propertyNames.add(DevQsarConstants.VAPOR_PRESSURE);
//		datasetNames.add("VP_from_exp_prop_and_chemprop");
//		
//		propertyNames.add(DevQsarConstants.HENRYS_LAW_CONSTANT);
//		datasetNames.add("HLC_from_exp_prop_and_chemprop");
		
		
		
		for (int i=0;i<propertyNames.size();i++) {
			
			String propertyName=propertyNames.get(i);
			String dataSetName=datasetNames.get(i);
			
			PropertyValueServiceImpl propertyValueService = new PropertyValueServiceImpl();
			List<PropertyValue> propertyValues = propertyValueService.findByPropertyNameWithOptions(propertyName, true, true);
				System.out.println("Number of raw records ="+propertyValues.size());

			String folder="data\\dev_qsar\\output\\";
			folder+=dataSetName+"\\";		
			String jsonPath=folder+dataSetName+"_Mapped_Records.json";
			jsonPath=jsonPath.replace(" ", "_").replace("=", "_");
			JsonArray mappedRecords=Utilities.getJsonArrayFromJsonFile(jsonPath);
			System.out.println("Number of mapped records ="+mappedRecords.size());


//			String sql="select count(id) from qsar_datasets.data_points dp where dp.fk_dataset_id="+90
			
			DataPointServiceImpl dpsi=new DataPointServiceImpl();
			List<DataPoint>datapoints=dpsi.findByDatasetName(dataSetName);
			//		System.out.println("Number of datapoints ="+datapoints.size());

			System.out.println(propertyName+"\t"+propertyValues.size()+"\t"+mappedRecords.size()+"\t"+datapoints.size());
		}
	}
	
	
	static void getDatasetStatsUsingSql() {

		System.out.println("propertyName\tRaw*\tDSSTox Mapped\tDataset");

		Connection conn=DatabaseLookup.getConnection();
		
		List<String>datasetNames=new ArrayList<>();

		datasetNames.add("MP from exp_prop and chemprop");
		datasetNames.add("BP from exp_prop and chemprop");
		datasetNames.add("WS from exp_prop and chemprop");
		datasetNames.add("LogP from exp_prop and chemprop");
		datasetNames.add("VP from exp_prop and chemprop");
		datasetNames.add("HLC from exp_prop and chemprop");
		
		for (int i=0;i<datasetNames.size();i++) {
			
			String dataSetName=datasetNames.get(i);
			
			String sql="select id from qsar_datasets.datasets d where d.\"name\" ='"+dataSetName+"'";
			String datasetId=DatabaseLookup.runSQL(conn, sql);

			sql="select fk_property_id from qsar_datasets.datasets d where d.\"name\" ='"+dataSetName+"'";
			String propertyId_qsar_datasets=DatabaseLookup.runSQL(conn, sql);
			
			sql="select p.\"name\" from qsar_datasets.properties p where p.id="+propertyId_qsar_datasets;
			String propertyName=DatabaseLookup.runSQL(conn, sql);
			
			sql="select id from exp_prop.properties d where d.\"name\" ='"+propertyName.replace("'", "''")+"'";
			String propertyIdE_exp_prop=DatabaseLookup.runSQL(conn, sql);

//			System.out.println(dataSetName+"\t"+datasetId+"\t"+propertyIdE_exp_prop);
		
			sql="select count (pv.id) from exp_prop.property_values pv "+
					"where fk_property_id="+propertyIdE_exp_prop+" and keep=true and "+
					"(value_qualifier is null or value_qualifier ='~')";

//			System.out.println(sql);
			
			String countRaw=DatabaseLookup.runSQL(conn, sql);
						
//			System.out.println("Number of raw records ="+countRaw);

			String folder="data\\dev_qsar\\output\\";
			folder+=dataSetName+"\\";		
			String jsonPath=folder+dataSetName+"_Mapped_Records.json";
			jsonPath=jsonPath.replace(" ", "_").replace("=", "_");
			JsonArray mappedRecords=Utilities.getJsonArrayFromJsonFile(jsonPath);
			
			String countMapped=mappedRecords.size()+"";
//			System.out.println("Number of mapped records ="+countMapped);

			sql="select count(id) from qsar_datasets.data_points dp where dp.fk_dataset_id="+datasetId;			
			String countDatapoints=DatabaseLookup.runSQL(conn, sql);
//			System.out.println("Number of datapoints ="+countDatapoints);
						
			System.out.println(propertyName+"\t"+countRaw+"\t"+countMapped+"\t"+countDatapoints);
		}
	}
	
	static void getDatasetStatsForOneDataset() {

//		String dataSetName="ExpProp_WaterSolubility_WithChemProp_120121_TMM";
//		String propertyName=DevQsarConstants.WATER_SOLUBILITY;
//		String dataSetName="WS from exp_prop and chemprop";
		
		String dataSetName="ExpProp BCF Fish_TMM";
		String propertyName="LogBCF_Fish_WholeBody";//TODO look up from dataset from database		
		
//		String propertyName=DevQsarConstants.BOILING_POINT;
////		String dataSetName="Standard Boiling Point from exp_prop_TMM";
//		String dataSetName="BP from exp_prop and chemprop";
		
		
		Hashtable<String, Integer>htRaw=getRawRecordCountsBySource(propertyName);
		getMappedRecordsBySource(dataSetName,htRaw);		
//		getDiscardedRecordsByReason(dataSetName, "LookChem");
		System.out.println("");
//		getDiscardedRecordsByReason(dataSetName, "PubChem");
//		getDiscardedRecordsByReason(dataSetName);

	}

	private static void getMappedRecordsBySource(String dataSetName,Hashtable<String, Integer>htRaw) {
		
		Hashtable<String,Integer>htCountsMapped=new Hashtable<>();
		String folder="data\\dev_qsar\\output\\";
		folder+=dataSetName+"\\";		
		String jsonPath=folder+dataSetName+"_Mapped_Records.json";
		jsonPath=jsonPath.replace(" ", "_").replace("=", "_");
		JsonArray mappedRecords=Utilities.getJsonArrayFromJsonFile(jsonPath);
		
		Set<String>rawSources=htRaw.keySet();
		
		for (String source:rawSources) htCountsMapped.put(source, 0);
		
		System.out.println(mappedRecords.size());
		
		for (int i=0;i<mappedRecords.size();i++) {
			JsonObject jo=mappedRecords.get(i).getAsJsonObject();
			String sourceName=jo.get("source_name").getAsString();
			if(htCountsMapped.get(sourceName)==null) {
				htCountsMapped.put(sourceName,1);
			} else {
				htCountsMapped.put(sourceName,htCountsMapped.get(sourceName)+1);			
			}
		}
		
		for (String source:rawSources) {
			System.out.println(source+"\t"+htRaw.get(source)+"\t"+htCountsMapped.get(source));
		}

	}
	
	private static void getDiscardedRecordsByReason(String dataSetName,String sourceName) {
		

		String folder="data\\dev_qsar\\output\\";
		folder+=dataSetName+"\\";		
		String jsonPath=folder+dataSetName+"_Discarded_Records.json";
		jsonPath=jsonPath.replace(" ", "_").replace("=", "_");
		JsonArray discardedRecords=Utilities.getJsonArrayFromJsonFile(jsonPath);

		Hashtable<String,Integer>htReasonCounts=new Hashtable<>();
		
		System.out.println(discardedRecords.size());
		
		for (int i=0;i<discardedRecords.size();i++) {
			JsonObject jo=discardedRecords.get(i).getAsJsonObject();
			
			String sourceNameCurrent=jo.get("source_name").getAsString();
			
			if (!sourceName.equals(sourceNameCurrent)) continue;
			
			String reason=jo.get("reason_discarded").getAsString();
			
			if(htReasonCounts.get(reason)==null) {
				htReasonCounts.put(reason,1);
			} else {
				htReasonCounts.put(reason,htReasonCounts.get(reason)+1);			
			}
		}
		
		Set<String>reasons=htReasonCounts.keySet();
		
		for (String reason:reasons) {
			System.out.println(reason+"\t"+htReasonCounts.get(reason));
		}

	}
	

	private static void getDiscardedRecordsByReason(String dataSetName) {
		

		String folder="data\\dev_qsar\\output\\";
		folder+=dataSetName+"\\";		
		String jsonPath=folder+dataSetName+"_Discarded_Records.json";
		jsonPath=jsonPath.replace(" ", "_").replace("=", "_");
		JsonArray discardedRecords=Utilities.getJsonArrayFromJsonFile(jsonPath);

		Hashtable<String,Integer>htReasonCounts=new Hashtable<>();
		
		System.out.println(discardedRecords.size());
		
		for (int i=0;i<discardedRecords.size();i++) {
			JsonObject jo=discardedRecords.get(i).getAsJsonObject();
			
			String sourceNameCurrent=jo.get("source_name").getAsString();
						
			String reason=jo.get("reason_discarded").getAsString();
			
			if(htReasonCounts.get(reason)==null) {
				htReasonCounts.put(reason,1);
			} else {
				htReasonCounts.put(reason,htReasonCounts.get(reason)+1);			
			}
		}
		
		Set<String>reasons=htReasonCounts.keySet();
		
		for (String reason:reasons) {
			System.out.println(reason+"\t"+htReasonCounts.get(reason));
		}

	}

	private static Hashtable<String, Integer> getRawRecordCountsBySource(String propertyName) {
		PropertyValueServiceImpl propertyValueService = new PropertyValueServiceImpl();
		List<PropertyValue> propertyValues = propertyValueService.findByPropertyNameWithOptions(propertyName, true, true);
		
		Hashtable<String,Integer>htCountsRaw=new Hashtable<>();
		
		
		for (PropertyValue pv:propertyValues) {
			
			String sourceName=null;
			
			if (pv.getPublicSource()!=null) {
				sourceName=pv.getPublicSource().getName();
			} else if (pv.getLiteratureSource()!=null) {
				sourceName=pv.getLiteratureSource().getName();
			}
			
			if(htCountsRaw.get(sourceName)==null) {
				htCountsRaw.put(sourceName,1);
			} else {
				htCountsRaw.put(sourceName,htCountsRaw.get(sourceName)+1);			
			}
			
		}

		Set<String>sourceNames=htCountsRaw.keySet();
		List<String> sourceList = new ArrayList<String>(sourceNames) ; 
		
		Collections.sort(sourceList);
		
//		for (String source:sourceList) {
//			System.out.println(source+"\t"+htCountsRaw.get(source));
//		}
		return htCountsRaw;
	}
	

	public static void createWS() {
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
		
		String datasetName = "WS from exp_prop and chemprop";
		String datasetDescription = "WS from exp_prop and chemprop where 1e-14 < WS(M) < 100, 1e-11 < WS(g/L) < 990, " 
				+ "20 < T (C) < 30, 740 < P (mmHg) < 780, 6.5 < pH < 7.5";
				
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
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

		
		String datasetName = "VP from exp_prop and chemprop";
		String datasetDescription = "VP from exp_prop and chemprop where 1e-14 < VP(mmHg) < 1e6 and 20 < T(C) < 30";
		
		
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
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

		
		String datasetName = listName+"_TMM";
		String datasetDescription = "pKA with 20 < T (C) < 30";
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				bounds);
		creator.createPropertyDataset(listMappedParams, false);


	}
	
	public static void createPKAa() {
		// comment for diff
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");

		String propertyName = DevQsarConstants.PKA_A;
		// String listName = "ExpProp_HLC_WithChemProp_121421";

		ArrayList<String> listNameArray = new ArrayList<String>();
		listNameArray.add("ExpProp_PKAa_WithChemProp_011423");

		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, listNameArray, omitSalts);

//		String datasetName = "ExpProp_LogP_WithChemProp_MULTIPLE";
		String datasetName = "pKAa from exp_prop and chemprop";
		String datasetDescription = "pKAa from exp_prop and chemprop with no bounds";
		
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				null);
		creator.createPropertyDataset(listMappedParams, false);
		
	}
	
	public static void createPKAb() {
		// comment for diff
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");

		String propertyName = DevQsarConstants.PKA_B;
		// String listName = "ExpProp_HLC_WithChemProp_121421";

		ArrayList<String> listNameArray = new ArrayList<String>();
		listNameArray.add("ExpProp_PKAb_WithChemProp_011423");

		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, listNameArray, omitSalts);

//		String datasetName = "ExpProp_LogP_WithChemProp_MULTIPLE";
		String datasetName = "pKAb from exp_prop and chemprop";
		String datasetDescription = "pKAb from exp_prop and chemprop with no bounds";
		
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				null);
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

		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, listNameArray, omitSalts);

//		String datasetName = "ExpProp_LogP_WithChemProp_MULTIPLE";
		String datasetName = "LogP from exp_prop and chemprop";
		String datasetDescription = "LogP from exp_prop and chemprop with -6 < LogKow < 11 and 20 < T(C) < 30, ";
		
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				bounds);
		creator.createPropertyDataset(listMappedParams, false);
		
	}

	// methods like these 
//	public static void createHLC() {
//		// comment for diff
//		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
//		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");
//
//		String propertyName = DevQsarConstants.HENRYS_LAW_CONSTANT;
//		// String listName = "ExpProp_HLC_WithChemProp_121421";
//
//		ArrayList<String> listNameArray = new ArrayList<String>();
//		listNameArray.add("ExpProp_hlc_WithChemProp_071922_ml_1_to_2000");
//		listNameArray.add("ExpProp_hlc_WithChemProp_071922_ml_2001_to_4000");
//		listNameArray.add("ExpProp_hlc_WithChemProp_071922_ml_4001_to_4849");
//
//		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
//		BoundParameterValue phBound = new BoundParameterValue("pH", 6.0, 8.0, true);
//		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
//		bounds.add(temperatureBound);
//		bounds.add(phBound);
//
//		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, 
//				false, true, false, true, true, false, false, listNameArray,true);
//		String datasetName = "ATTEMPT8 ExpProp_HLC_WithChemProp_071922_MULTIPLE";
//		String datasetDescription = "ATTEMPT8 MULTIPLE LIST Exprop HLC with 20 < T (C) < 30 and 6 < pH < 8";
//		DatasetParams listMappedParams = new DatasetParams(datasetName, 
//				datasetDescription, 
//				propertyName,
//				listMappingParams,
//				bounds);
//		creator.createPropertyDataset(listMappedParams, false);
//
//
//	}


	public static void createHLC() {
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

		
		String datasetName = "HLC from exp_prop and chemprop";
//		String datasetName = "HLC delete later";
		
		String datasetDescription = "HLC from exp_prop and chemprop where "
				+ "1e-13 < HLC (atm m^3/mol) < 1e2 , 20 < T (C) < 30, 6.5 < pH < 7.5";


		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
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

				
		String datasetName = "MP from exp_prop and chemprop";
		String datasetDescription = "MP from exp_prop and chemprop where -250 < MP < 550 and 740 < P < 780";

		
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
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
		String datasetName = "ExpProp BCF Fish_TMM";
		String datasetDescription = "BCF values for fish with whole body tissue (measured ZEROS OMITTED)";
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
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


		String datasetName = "BP from exp_prop and chemprop";
		String datasetDescription = "BP from exp_prop and chemprop where -150 < BP < 600 and 740 < P < 780";

		
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				bounds);
		creator.createPropertyDataset(listMappedParams, false);

	}
	
	/**
	 * create boiling point dataset based on chemreg lists
	 */
	public static void createBP_exclude_LookChem() {
		
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


		String datasetName = "Standard Boiling Point from exp_prop_TMM_exclude_LookChem";
		String datasetDescription = "Boiling Point 740 < P (mmHg) < 780";
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				bounds);
		
		List<String>excludedSources=new ArrayList<>();
		excludedSources.add("LookChem");
		
		creator.createPropertyDataset(listMappedParams, false, excludedSources);

	}

}
