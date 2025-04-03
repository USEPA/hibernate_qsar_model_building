package gov.epa.run_from_java.scripts;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.poi.xssf.usermodel.XSSFPivotCacheRecords;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.endpoints.datasets.BoundParameterValue;
import gov.epa.endpoints.datasets.BoundPropertyValue;
import gov.epa.endpoints.datasets.DatasetCreator;
import gov.epa.endpoints.datasets.DatasetParams;
import gov.epa.endpoints.datasets.ExplainedResponse;
import gov.epa.endpoints.datasets.DatasetParams.MappingParams;
import gov.epa.endpoints.datasets.dsstox_mapping.DsstoxMapper;

import gov.epa.run_from_java.data_loading.ChangeKeptPropertyValues;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer;
import kong.unirest.HttpResponse;

/**
 * To create a dataset, first need entries in PropertyValueValidator for:
 * checkRealisticValueForProperty()
 * checkRangeForProperty(
 * checkUnits() -
 * checkMedianQsarValuesForDatapoint
 * 
 * Need entries in DevQsarConstants for 
 * getDatasetFinalUnitsNameMap 
 * getContributorUnitsNameMap()
 * 
 * UnitConverter.setQsarPropertyValue()
 */

public class DatasetCreatorScript {

	// Default settings:
	String dsstoxMappingId = DevQsarConstants.MAPPING_BY_LIST;
	boolean isNaive = false;
	boolean useValidation = true;
	boolean requireValidation = false;
	boolean resolveConflicts = true;
	boolean validateConflictsTogether = true;
	boolean omitOpsinAmbiguousNames = false;
	boolean omitUvcbNames = true;
	boolean omitSalts = true;
	boolean validateStructure = true;
	boolean validateMedian = true;//important when creating modeling datasets, may not want to exclude records if for the dashboard

	
	boolean excludeBasedOnMissingExposureType=false;
	boolean excludeBasedOnPredictedWS=false;
	boolean excludeBasedOnBaselineToxicity=false;
	boolean excludeBasedOnConcentrationType=false;
	
	
	String workflow = "qsar-ready";
	String serverHost = "https://hcd.rtpnc.epa.gov";
//	String serverHost = "https://hazard-dev.sciencedataexperts.com";
	
	SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY,
			workflow, serverHost);

//	/**
//	 * This method may not work properly
//	 * 
//	 */
//	void fixQsarExpPropIds() {
//		List<String>datasetNames=new ArrayList<>();
//
////		datasetNames.add("HLC v1");
////		datasetNames.add("VP v1");
////		datasetNames.add("WS v1");
////		datasetNames.add("BP v1");
////		datasetNames.add("LogP v1");
//		datasetNames.add("MP v1");
//		
//		
//		for (String datasetName:datasetNames) {
//			DataPointDaoImpl d=new DataPointDaoImpl ();
//			List<DataPoint>dps=d.findByDatasetNameSql(datasetName);
//
//			Connection conn=SqlUtilities.getConnectionPostgres();
//
//			for (DataPoint dp:dps) {
//
//				String qsarExpPropID2 = fixQsarExpPropId(dp);
//				
//				System.out.println(dp.getQsar_exp_prop_property_values_id()+"\t"+qsarExpPropID2);
//				
//
////				String sql="UPDATE qsar_datasets.data_points dp\r\n"
////						+ "SET qsar_exp_prop_property_values_id = "+qsarExpPropID2+"\r\n"
////						+ "WHERE id="+dp.getId()+";";
////
////				SqlUtilities.runSQLUpdate(conn, sql);
////				System.out.println(qsarExpPropID2);
//			}
//
//		}
//		
////		System.out.println(Utilities.gson.toJson(dps));
//
//	}

	private String fixQsarExpPropId(DataPoint dp) {
		String qsarExpPropID2 = dp.getQsar_exp_prop_property_values_id().replace("EXP", "");
		String[] ids = qsarExpPropID2.split("\\|");
		if (ids.length == 1) {
			qsarExpPropID2 = Integer.parseInt(ids[0]) + "";
		} else if (ids.length == 2) {
			String id1 = Integer.parseInt(ids[0]) + "";
			String id2 = Integer.parseInt(ids[1]) + "";
			qsarExpPropID2 = id1 + "|" + id2;
		}
		return qsarExpPropID2;
	}

	void createFHM_LC50_toxval() {

		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");

		boolean isNaive = true;
		boolean requireValidation = false;
		boolean useValidation = false;


		String datasetName = DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50 + " ToxValv93";

		String datasetDescription = "96 hour fathead minnow LC50 data taken from toxval v93. SQL filter: toxval_type = LC50, "
				+ "rs.quality not like '3%' AND rs.quality not like '4%' AND "
				+ "tv.media in ('-', 'Fresh water') AND ttd.toxval_type_supercategory in ('Point of Departure', 'Toxicity Value', 'Lethality Effect Level') AND "
				+ "tv.toxval_numeric > 0";

		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(0.0, 1000.0);// TODO

//		System.out.println(datasetDescription);

		String propertyName = DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50;

		List<BoundParameterValue> bounds = null;// leave empty
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_DTXSID, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, null, omitSalts, validateStructure, validateMedian, bounds, boundPropertyValue);


		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();
//		excludedSources.add("ECHA eChemPortal: ECHA REACH");// bad data

		creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);

	}
	
	/**
	 * - Omit ECOTOX records where have matching Arnot?
	 * 
	 * - criterion_water_concentration_measured
	 * 		o criterion_water_concentration_measured=3
	 * 		o chem_analysis_method = Unmeasured or not reported or null
	 * 
	 * - criterion_radiolabel 
	 * 		o radiolabel criterion =3
	 * 		o test_radiolabel !="NR", don't see a way to tell if have correction for radiolabeling in ECOTOX 
	 * 
	 * - criterion_aqueous_solubility:
	 * 		o correct BCF: BCFcorrected = BCFmeasured (CW/SW)  
	 * 		o omit BCF: criterion_aqueous_solubility=3 or CW>SW 
	 * 
	 *  - criterion_exposure_duration
	 *  	o Omit if t80>exposure_duration
	 *  
	 *  - criterion_tissue_analyzed
	 *  	o Omit if not Whole body- omits a lot of ECOTOX
	 */
	public void createBCF_modeling() {
		
//		Fish BCF
//		Arnot
//		Arnot+NITE
//		Arnot+NITE+ECOTOX
//
//		Fish Whole body BCF
//		Arnot
//		Arnot+NITE
//		Arnot+NITE+ECOTOX
		
		
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;

		String endpoint = DevQsarConstants.BCF;
		String sourceArnot="Arnot 2006";

//		String datasetName="exp_prop_BCF_v0 modeling_map_by_list";
//		String datasetDescription = endpoint+" from "+sourceArnot+" map by list";
//		String dsstoxMappingId=DevQsarConstants.MAPPING_BY_LIST;

		
//		String datasetName="exp_prop_BCF_v0 modeling_map_by_CAS";
//		String datasetDescription = endpoint+" from "+sourceArnot+" map by CAS";
//		boolean isNaive=true;
//		String dsstoxMappingId=DevQsarConstants.MAPPING_BY_CASRN;
		
		String datasetNameOriginal="exp_prop_BCF_v0 modeling_map_by_CAS";

//		String datasetName = "exp_prop_BCF_fish_whole_body_v1_modeling_map_by_CAS";
//		String responseSite="whole body";
//		String typeAnimal="Fish";
//		String overallScore=null;
//		boolean isNaive=true;
//		String dsstoxMappingId=DevQsarConstants.MAPPING_BY_CASRN;
//		String datasetDescription = typeAnimal+" "+responseSite+" "+endpoint+" from "+sourceArnot+" map by CAS";
		
		String datasetName = "exp_prop_BCF_fish_whole_body_overall_score_1_v2_modeling_map_by_CAS";
		String responseSite="whole body";
		String typeAnimal="Fish";
		String overallScore="1: Acceptable BCF";
		boolean isNaive=true;
		String dsstoxMappingId=DevQsarConstants.MAPPING_BY_CASRN;
		String datasetDescription = typeAnimal+" "+responseSite+" "+endpoint+" with Overall Score ="+overallScore+" from "+sourceArnot+" map by CAS";
		
		
		List<String> includedSources = Arrays.asList(sourceArnot);		
//		List<String> includedSources = Arrays.asList(sourceECOTOX,sourceBurkhard,sourceOPERA);

		List<BoundParameterValue> boundsParameterValues = null;
		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(null, null);
		
		ArrayList<String> listNameArray = new ArrayList<String>(Arrays.asList("exp_prop_Arnot 2006"));

		MappingParams listMappingParams = new MappingParams(dsstoxMappingId, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, boundsParameterValues, boundPropertyValue);

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, endpoint,
				listMappingParams);

		
		System.out.println(Utilities.gson.toJson(listMappedParams));
		
//		Dataset dataset=creator.createPropertyDatasetWithSpecifiedSources(listMappedParams,false, includedSources);

		
		Dataset dataset=creator.createPropertyDatasetWithSpecifiedSourcesBCF(datasetNameOriginal,listMappedParams,
				false, includedSources,typeAnimal,responseSite,overallScore);

	}
	
	void createBCF() {

		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;

		String propertyName = DevQsarConstants.BCF;
		String datasetName = "exp_prop_BCF_v2.0";
		String datasetDescription = propertyName+" data for the Dashboard";
		
		List<String> sources = Arrays.asList("Arnot 2006", "Burkhard",
				"QSAR_Toolbox","ECOTOX_2024_12_12",
				"OPERA2.8","ThreeM");
		
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		if(listNameArray==null) {
			return;
		} else {
			System.out.println(listNameArray);
		}
		
		
		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(1e-6, null);// TODO
		List<BoundParameterValue> bounds = null;// leave empty

		useExperimentalDashboardSettings();
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, bounds, boundPropertyValue);
		
		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);


//		List<String> includedSources = Arrays.asList(sourceECOTOX,sourceBurkhard);
		List<String> excludedSources = new ArrayList<>();
		
		excludedSources.add("ECOTOX_2023_12_14");
		excludedSources.add("OPERA2.9");
		excludedSources.add("PhysPropNCCT");
		
//		excludedSources.add("ThreeM");//only 1
//		

		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		if(dataset!=null) {
			addEntryForDatasetsInDashboard(dataset);
//			updateEntryForDatasetsInDashboard(dataset);
		}

	}
	
	void createBAF() {

		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;

		String propertyName = DevQsarConstants.BAF;
		String datasetName = "exp_prop_BAF_v1.0";
		String datasetDescription = propertyName+" data for the Dashboard";
		
		List<String> sources = Arrays.asList("Arnot 2006", "Burkhard",
				"ECOTOX_2024_12_12","OPERA2.8","ThreeM");
		
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		if(listNameArray==null) {
			return;
		} else {
			System.out.println(listNameArray);
		}
		
		
		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(1e-6, null);// TODO
		List<BoundParameterValue> bounds = null;// leave empty

		useExperimentalDashboardSettings();
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, bounds, boundPropertyValue);
		
		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);


//		List<String> includedSources = Arrays.asList(sourceECOTOX,sourceBurkhard);
		List<String> excludedSources = new ArrayList<>();
		
		excludedSources.add("ECOTOX_2023_12_14");
		excludedSources.add("OPERA2.9");
		excludedSources.add("PhysPropNCCT");
		
//		excludedSources.add("ThreeM");//only 1
//		

		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		if(dataset!=null) {
			addEntryForDatasetsInDashboard(dataset);
//			updateEntryForDatasetsInDashboard(dataset);
		}

	}
	
	

	
	void createDatasetsForDashboard() {

//		createWS();//done
//		createVP();//done
//		createBP();//done
//		createHLC();//done
//		createLogP();//done
//		createD();//done
//		createMP();//rerun

		
//		createpKa_a();
//		createpKa_b();
//		createKM();
//		createBIODEG_HL_HC();
//		createKOC();
//		createOH();		
//		createRBIODEG();
//		createCLINT();
//		createFUB();
//		createKOA();
//		createFP();
		
//		createOPERA_ER_AR_datasets();
		
//		createBCF();
		createBAF();
		
//		createST();//TODO

	}
	void createModelingDatasets() {
//		createWS_modeling();
//		createLogP_modeling();
//		createHLC_modeling();
		
//		createBP_modeling();
		createVP_modeling();
//		createMP_modeling();
		
//		createBP_OChem();
//		createVP_OChem();
//		createMP_OChem();
		
	}
	
	
	void createBiodegDatasets() {
//		createRBIODEG_NITE_OPPT();
//		createRBIODEG_NITE_OPPT_CAS();
		
	}
	
	void deletePubchemDatasets() {
		
		String sql="select id,name from qsar_datasets.datasets d where name like '%PubChem_2024_03_20%';";
		
		DatasetServiceImpl ds=new DatasetServiceImpl();
		
		try {
			ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
			while (rs.next()) {
				long id=rs.getLong(1);
				String name=rs.getString(2);
				System.out.print("Deleting " +name);
				ds.deleteSQL(id);
				System.out.println("done");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	void deleteDatasets() {
		DatasetServiceImpl ds=new DatasetServiceImpl();
		
		ds.deleteSQL(502L);
		
//		for (Long i=388L;i<=391L;i++) ds.deleteSQL(i);
//		ds.deleteSQL(261L);
		
	}
	
	void updateTTR_Binding_PropertyValues(List<PropertyValue> propertyValues,String set) {

		for(int i=0;i<propertyValues.size();i++) {
			
			PropertyValue pv=propertyValues.get(i);
			
			String dataset=pv.getParameterValue("dataset").getValueText();
			double property_value=pv.getValuePointEstimate();

//			if(maximum_concentration<90) {
//				if (property_value<90) {
//					System.out.println(pv.getSourceChemical().getSourceDtxsid()+"\t"+maximum_concentration+"\t"+property_value);
//					pv.setKeep(false);
//					pv.setKeepReason("maximum_concentration < 90 and property_value < 90");
//				}
//			} else if(maximum_concentration>110) { 
//				System.out.println(pv.getSourceChemical().getSourceDtxsid()+"\t"+maximum_concentration+"\t"+property_value);
//				pv.setKeep(false);
//				pv.setKeepReason("maximum_concentration>110");
//			}

			if(!dataset.equals(set)) {
				propertyValues.remove(i--);
				continue;
			} 
			
			if(pv.getParameterValue("maximum_concentration")==null) continue;
			double maximum_concentration=pv.getParameterValue("maximum_concentration").getValuePointEstimate();
			
			if(maximum_concentration<90) {
				if (property_value<90) {
					propertyValues.remove(i--);
				}
			} else if(maximum_concentration>110) { 
				propertyValues.remove(i--);
			}
		}
		
	}
	
	void createSingleSourceDatasets() {

		createHLC_SingleSource();
		createLogP_SingleSource();
		createBP_SingleSource();
		createVP_SingleSource();
		createMP_SingleSource();
		createWS_SingleSource();
		
	}
	
	
	public static void main(String[] args) {
		DatasetCreatorScript dcs = new DatasetCreatorScript();

//		dcs.deleteDatasets();

//		dcs.createDatasetsForDashboard();
		
//		dcs.createSingleSourceDatasets();
		
//		dcs.create_LC50_Ecotox_modeling();
//		dcs.createBCF_modeling();
		
//		dcs.createToxCast_TTR_Binding();
//		dcs.createRatLC50_CoMPAIT();
		

//		dcs.createModelingDatasets();

//		dcs.createFishToxModels();
//		dcs.createBiodegDatasets();
//		
//		dcs.getAutoMappingsFromChemRegList();
		
//		dcs.getDatasetStats();//Get record counts for the papers
		dcs.getDatasetStatsUsingSql();//Get record counts for the papers
//		getDatasetStatsForOneDataset();
//		dcs.getMappedRecordCountsBySourceAndProperty();


	}


	void testStandardize() {

		String workflow = "qsar-ready";
		String serverHost = "https://hcd.rtpnc.epa.gov";
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(
				DevQsarConstants.QSAR_READY, workflow, serverHost);

		String smiles = "CC(=O)N[C@H]1[C@H](O[C@H]2[C@@H](O)[C@@H](CO)O[C@@H](O[C@H]3[C@H](O)[C@@H](O)C(O)O[C@@H]3CO)[C@@H]2O)O[C@H](CO)[C@@H](O)[C@@H]1O[C@@H]1O[C@H](CO)[C@H](O)[C@H](O)[C@H]1O";
		boolean useFullStandardize = false;

		HttpResponse<String> standardizeResponse = sciDataExpertsStandardizer.callQsarReadyStandardizePost(smiles,
				false);

		System.out.println("status=" + standardizeResponse.getStatus());

		if (standardizeResponse.getStatus() == 200) {
			String jsonResponse = SciDataExpertsStandardizer.getResponseBody(standardizeResponse, useFullStandardize);
			String qsarSmiles = SciDataExpertsStandardizer.getQsarReadySmilesFromPostJson(jsonResponse,
					useFullStandardize);
			System.out.println(smiles + "\t" + qsarSmiles);
		}

	}

	private void getMappedRecordCountsBySourceAndProperty() {

		List<String> datasetNames = new ArrayList<>();

//		datasetNames.add("HLC from exp_prop and chemprop");
//		datasetNames.add("WS from exp_prop and chemprop");
//		datasetNames.add("VP from exp_prop and chemprop");
//		datasetNames.add("LogP from exp_prop and chemprop");
//		datasetNames.add("MP from exp_prop and chemprop");
//		datasetNames.add("BP from exp_prop and chemprop v2");
//		datasetNames.add("pKa_a from exp_prop and chemprop");
//		datasetNames.add("pKa_b from exp_prop and chemprop");

//		datasetNames.add("HLC v1");
//		datasetNames.add("VP v1");
//		datasetNames.add("BP v1");
//		datasetNames.add("WS v1");
//		datasetNames.add("LogP v1");
//		datasetNames.add("MP v1");

		datasetNames.add("HLC v1 modeling");
		datasetNames.add("WS v1 modeling");
		datasetNames.add("VP v1 modeling");
		datasetNames.add("BP v1 modeling");
		datasetNames.add("LogP v1 modeling");
		datasetNames.add("MP v1 modeling");

		DatasetServiceImpl datasetService = new DatasetServiceImpl();

		List<String> globalSourceList = new ArrayList<>();

		Hashtable<String, Hashtable<String, Integer>> htAllSets = new Hashtable<>();

		for (String datasetName : datasetNames) {

			Dataset dataset = datasetService.findByName(datasetName);

//			System.out.println(datasetName+"\t"+dataset.getName());

//			Hashtable<String, Integer>htRaw=getRawRecordCountsBySource(dataset.getProperty().getName());			
			Hashtable<String, Integer> htMapped = getMappedRecordsBySource(datasetName);

			for (String key : htMapped.keySet()) {
				if (!globalSourceList.contains(key))
					globalSourceList.add(key);
			}

			htAllSets.put(datasetName, htMapped);
//			System.out.println("");
		}

//		System.out.println("All sources:");

		System.out.print("Source\t");
		for (String key : htAllSets.keySet()) {
			System.out.print(key.replace(" v1 modeling", "") + "\t");
		}
		System.out.print("\n");

		for (String source : globalSourceList) {
			System.out.print(source + "\t");

			for (String key : htAllSets.keySet()) {

				if (htAllSets.get(key).get(source) != null) {
					System.out.print(htAllSets.get(key).get(source) + "\t");
				} else {
					System.out.print("0\t");
				}

			}
			System.out.print("\n");

		}

	}

	void getDatasetStats() {

		System.out.println("propertyName\tRaw*\tDSSTox Mapped\tDataset");

		List<String> datasetNames = new ArrayList<>();
		datasetNames.add("HLC v1");
		datasetNames.add("WS v1");
		datasetNames.add("BP v1");
		datasetNames.add("VP v1");
		datasetNames.add("LogP v1");
		datasetNames.add("MP v1");

		DatasetServiceImpl ds = new DatasetServiceImpl();
		PropertyValueServiceImpl propertyValueService = new PropertyValueServiceImpl();
		DataPointServiceImpl dpsi = new DataPointServiceImpl();

		for (int i = 0; i < datasetNames.size(); i++) {

//			String propertyName=propertyNames.get(i);
			String dataSetName = datasetNames.get(i);

			Dataset dataset = ds.findByName(dataSetName);

			String propertyName = dataset.getProperty().getName();

//			System.out.println(dataset.getName()+"\t"+propertyName);

			List<PropertyValue> propertyValues = propertyValueService.findByPropertyNameWithOptions(propertyName, true,
					true);

			System.out.println("Number of raw records =" + propertyValues.size());

			String folder = "data\\dev_qsar\\output\\";
			folder += dataSetName + "\\";
			String jsonPath = folder + dataSetName + "_Mapped_Records.json";
			jsonPath = jsonPath.replace(" ", "_").replace("=", "_");
			JsonArray mappedRecords = Utilities.getJsonArrayFromJsonFile(jsonPath);

			System.out.println("Number of mapped records =" + mappedRecords.size());

//			String sql="select count(id) from qsar_datasets.data_points dp where dp.fk_dataset_id="+90

			List<DataPoint> datapoints = dpsi.findByDatasetName(dataSetName);
			System.out.println("Number of datapoints =" + datapoints.size());

			System.out.println(propertyName + "\t" + propertyValues.size() + "\t" + mappedRecords.size() + "\t"
					+ datapoints.size());
		}
	}

	void getDatasetStatsUsingSql() {

		System.out.println("propertyName\tRaw*\tDSSTox Mapped\tDataset");

		Connection conn = SqlUtilities.getConnectionPostgres();

		List<String> datasetNames = new ArrayList<>();

//		datasetNames.add("MP from exp_prop and chemprop");
//		datasetNames.add("BP from exp_prop and chemprop v3");
//		datasetNames.add("WS from exp_prop and chemprop");
//		datasetNames.add("LogP from exp_prop and chemprop");
//		datasetNames.add("VP from exp_prop and chemprop");
//		datasetNames.add("HLC from exp_prop and chemprop");
//		datasetNames.add("pKa_a from exp_prop and chemprop");
//		datasetNames.add("pKa_b from exp_prop and chemprop");

//		datasetNames.add("HLC v1");
//		datasetNames.add("VP v1");
//		datasetNames.add("BP v1");
//		datasetNames.add("WS v1");
//		datasetNames.add("LogP v1");
//		datasetNames.add("MP v1");

//		datasetNames.add("HLC v1 res_qsar");
//		datasetNames.add("VP v1 res_qsar");
//		datasetNames.add("BP v1 res_qsar");
//		datasetNames.add("WS v1 res_qsar");
//		datasetNames.add("LogP v1 res_qsar");
//		datasetNames.add("MP v1 res_qsar");

		datasetNames.add("HLC v1 modeling");
		datasetNames.add("WS v1 modeling");
		datasetNames.add("VP v1 modeling");
		datasetNames.add("BP v1 modeling");
		datasetNames.add("LogP v1 modeling");
		datasetNames.add("MP v1 modeling");

		String descriptorSetName = "WebTEST-default";
		String sql = "select id from qsar_descriptors.descriptor_sets where name='" + descriptorSetName + "';";
		String descriptorSetId = DatabaseLookup.runSQL(conn, sql);

		for (int i = 0; i < datasetNames.size(); i++) {

			String dataSetName = datasetNames.get(i);

			sql = "select id from qsar_datasets.datasets d where d.\"name\" ='" + dataSetName + "'";
			String datasetId = DatabaseLookup.runSQL(conn, sql);

//			System.out.println(datasetId);

			sql = "select fk_property_id from qsar_datasets.datasets d where d.\"name\" ='" + dataSetName + "'";
			String propertyId_qsar_datasets = DatabaseLookup.runSQL(conn, sql);

			sql = "select p.\"name\" from qsar_datasets.properties p where p.id=" + propertyId_qsar_datasets;
			String propertyName = DatabaseLookup.runSQL(conn, sql);

			sql = "select id from exp_prop.properties d where d.\"name\" ='" + propertyName.replace("'", "''") + "'";
			String propertyIdE_exp_prop = DatabaseLookup.runSQL(conn, sql);

//			System.out.println(dataSetName+"\t"+datasetId+"\t"+propertyIdE_exp_prop);

			sql = "select count (pv.id) from exp_prop.property_values pv " + "where fk_property_id="
					+ propertyIdE_exp_prop + " and keep=true and "
					+ "(value_qualifier is null or value_qualifier ='~')";

//			System.out.println(sql);

			String countRaw = DatabaseLookup.runSQL(conn, sql);

//			System.out.println("Number of raw records ="+countRaw);

			String folder = "data\\dev_qsar\\output\\";
			folder += dataSetName + "\\";
			String jsonPath = folder + dataSetName + "_Mapped_Records.json";
			jsonPath = jsonPath.replace(" ", "_").replace("=", "_");
			JsonArray mappedRecords = Utilities.getJsonArrayFromJsonFile(jsonPath);

			String countMapped = mappedRecords.size() + "";
//			System.out.println("Number of mapped records ="+countMapped);

			// Following gets datapoint count but ignores whether descriptors are available:
//			sql="select count(id) from qsar_datasets.data_points dp where dp.fk_dataset_id="+datasetId;			

			// To get datapoint counts, need to also check if selected descriptors are
			// available for each datapoint
			sql = "select count(dp.id) from qsar_datasets.data_points dp\n"
					+ "join qsar_datasets.datasets on dp.fk_dataset_id = datasets.id\n"
					+ "join qsar_descriptors.descriptor_values dv on dv.canon_qsar_smiles=dp.canon_qsar_smiles\n"
					+ "where dp.fk_dataset_id=" + datasetId + " and dv.fk_descriptor_set_id=" + descriptorSetId
					+ " and dv.values_tsv is not null;";

			String countDatapoints = DatabaseLookup.runSQL(conn, sql);
//			System.out.println("Number of datapoints ="+countDatapoints);

			System.out.println(propertyName + "\t" + countRaw + "\t" + countMapped + "\t" + countDatapoints);
		}
	}

	void getDatasetStatsForOneDataset() {

//		String dataSetName="ExpProp_WaterSolubility_WithChemProp_120121_TMM";

//		String propertyName=DevQsarConstants.WATER_SOLUBILITY;
//		String dataSetName="WS from exp_prop and chemprop";

//		String dataSetName="ExpProp BCF Fish_TMM";
//		String propertyName="LogBCF_Fish_WholeBody";//TODO look up from dataset from database		

//		String propertyName=DevQsarConstants.BOILING_POINT;
////		String dataSetName="Standard Boiling Point from exp_prop_TMM";
//		String dataSetName="BP from exp_prop and chemprop";

//		Hashtable<String, Integer>htRaw=getRawRecordCountsBySource(propertyName);
//		getMappedRecordsBySource(dataSetName,htRaw);		
//		getDiscardedRecordsByReason(dataSetName, "LookChem");
//		System.out.println("");
//		getDiscardedRecordsByReason(dataSetName, "PubChem");

		String dataSetName = "WS v1";
		getDiscardedRecordsByReason(dataSetName);

	}

	private Hashtable<String, Integer> getMappedRecordsBySource(String dataSetName) {

		Hashtable<String, Integer> htCountsMapped = new Hashtable<>();
		String folder = "data\\dev_qsar\\output\\";
		folder += dataSetName + "\\";
		String jsonPath = folder + dataSetName + "_Mapped_Records.json";
		jsonPath = jsonPath.replace(" ", "_").replace("=", "_");
		JsonArray mappedRecords = Utilities.getJsonArrayFromJsonFile(jsonPath);

//		Set<String>rawSources=htRaw.keySet();

//		for (String source:rawSources) htCountsMapped.put(source, 0);

		System.out.println(dataSetName + "\t" + mappedRecords.size());

		for (int i = 0; i < mappedRecords.size(); i++) {
			JsonObject jo = mappedRecords.get(i).getAsJsonObject();

			String sourceName;

			if (jo.get("public_source_name") == null) {
				sourceName = jo.get("literature_source_citation").getAsString();
			} else {
				sourceName = jo.get("public_source_name").getAsString();
			}

//			System.out.println(sourceName);

			if (htCountsMapped.get(sourceName) == null) {
				htCountsMapped.put(sourceName, 1);
			} else {
				htCountsMapped.put(sourceName, htCountsMapped.get(sourceName) + 1);
			}
		}

//		for (String source:rawSources) {
//			System.out.println(source+"\t"+htRaw.get(source)+"\t"+htCountsMapped.get(source));
//		}
		return htCountsMapped;

	}

	private void getDiscardedRecordsByReason(String dataSetName, String sourceName) {

		String folder = "data\\dev_qsar\\output\\";
		folder += dataSetName + "\\";
		String jsonPath = folder + dataSetName + "_Discarded_Records.json";
		jsonPath = jsonPath.replace(" ", "_").replace("=", "_");
		JsonArray discardedRecords = Utilities.getJsonArrayFromJsonFile(jsonPath);

		Hashtable<String, Integer> htReasonCounts = new Hashtable<>();

		System.out.println(discardedRecords.size());

		for (int i = 0; i < discardedRecords.size(); i++) {
			JsonObject jo = discardedRecords.get(i).getAsJsonObject();

			String sourceNameCurrent = jo.get("source_name").getAsString();

			if (!sourceName.equals(sourceNameCurrent))
				continue;

			String reason = jo.get("reason_discarded").getAsString();

			if (htReasonCounts.get(reason) == null) {
				htReasonCounts.put(reason, 1);
			} else {
				htReasonCounts.put(reason, htReasonCounts.get(reason) + 1);
			}
		}

		Set<String> reasons = htReasonCounts.keySet();

		for (String reason : reasons) {
			System.out.println(reason + "\t" + htReasonCounts.get(reason));
		}

	}

	private void getDiscardedRecordsByReason(String dataSetName) {

		String folder = "data\\dev_qsar\\output\\";
		folder += dataSetName + "\\";
		String jsonPath = folder + dataSetName + "_Discarded_Records.json";
		jsonPath = jsonPath.replace(" ", "_").replace("=", "_");
		JsonArray discardedRecords = Utilities.getJsonArrayFromJsonFile(jsonPath);

		Hashtable<String, Integer> htReasonCounts = new Hashtable<>();

		System.out.println(discardedRecords.size());

		for (int i = 0; i < discardedRecords.size(); i++) {
			JsonObject jo = discardedRecords.get(i).getAsJsonObject();

			String sourceNameCurrent = jo.get("source_name").getAsString();

			String reason = jo.get("reason_discarded").getAsString();

			if (htReasonCounts.get(reason) == null) {
				htReasonCounts.put(reason, 1);
			} else {
				htReasonCounts.put(reason, htReasonCounts.get(reason) + 1);
			}
		}

		Set<String> reasons = htReasonCounts.keySet();

		for (String reason : reasons) {
			System.out.println(reason + "\t" + htReasonCounts.get(reason));
		}

	}

	/**
	 * This method assumes a property value has either a public source or a
	 * literature source
	 * 
	 * @param propertyName
	 * @return
	 */
	private Hashtable<String, Integer> getRawRecordCountsBySource(String propertyName) {
		PropertyValueServiceImpl propertyValueService = new PropertyValueServiceImpl();
		List<PropertyValue> propertyValues = propertyValueService.findByPropertyNameWithOptions(propertyName, true,
				true);

		Hashtable<String, Integer> htCountsRaw = new Hashtable<>();

		for (PropertyValue pv : propertyValues) {

			String sourceName = null;

			if (pv.getPublicSource() != null) {
				sourceName = pv.getPublicSource().getName();
			} else if (pv.getLiteratureSource() != null) {
				sourceName = pv.getLiteratureSource().getName();
			}

			if (htCountsRaw.get(sourceName) == null) {
				htCountsRaw.put(sourceName, 1);
			} else {
				htCountsRaw.put(sourceName, htCountsRaw.get(sourceName) + 1);
			}

		}

		Set<String> sourceNames = htCountsRaw.keySet();
		List<String> sourceList = new ArrayList<String>(sourceNames);

		Collections.sort(sourceList);

//		for (String source:sourceList) {
//			System.out.println(source+"\t"+htCountsRaw.get(source));
//		}
		return htCountsRaw;
	}

	/**
	 * Creates WS dataset for exposing raw experimental data to dashboard
	 */

	public void createWS() {
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;
		String propertyName = DevQsarConstants.WATER_SOLUBILITY;

		List<String> sources = Arrays.asList("prod_chemprop", "ADDoPT", "AqSolDB", "Bradley", 
				"ChemicalBook",	"eChemPortalAPI", "ICF", "OChem_2024_04_03", 
				"OFMPub", "OPERA2.8", "PubChem_2024_11_27", "QSARDB", "ThreeM");

		ArrayList<String> listNameArray = getChemRegListNames(sources);
		System.out.println(listNameArray);

//		if(true) return;

		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		BoundParameterValue pressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		BoundParameterValue phBound = new BoundParameterValue("pH", 6.5, 7.5, true);

//		BoundPropertyValue boundPV=new BoundPropertyValue(DevQsarConstants.MIN_WATER_SOLUBILITY_G_L,DevQsarConstants.MAX_WATER_SOLUBILITY_G_L);
		BoundPropertyValue boundPV = new BoundPropertyValue(DevQsarConstants.MIN_WATER_SOLUBILITY_G_L/100.0, null);// avoid infinite -logWS

		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);
		bounds.add(pressureBound);
		bounds.add(phBound);

		useExperimentalDashboardSettings();
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, bounds, boundPV);

		String propAbbrev=DevQsarConstants.getConstantNameByReflection(propertyName);
		String datasetName = "exp_prop_"+propAbbrev+"_v2.0";
		System.out.println("datasetName="+datasetName);

		String datasetDescription = propertyName + " data from OPERA2.8, exp_prop, and chemprop";

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);


		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("LookChem");// OPERA2.9 includes this when corroborated by other source
		excludedSources.add("OChem");// we have OChem_2024_04_03
		excludedSources.add("PubChem");// we have PubChem_2024_11_27
		excludedSources.add("PubChem_2024_03_20");// we have PubChem_2024_11_27
		excludedSources.add("OPERA2.9");// we are now using OPERA2.8
		excludedSources.add("PhysPropNCCT");//already have OPERA2.8
		excludedSources.add("ANGUS Chemical Company (Chemical company)");// only will have 4 and who knows reliability
		excludedSources.add("OFMPub");// some curation issues
		excludedSources.add("Tetko et al. 2011");//duplicate of OChem
		
		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		if(dataset!=null) {
//			addEntryForDatasetsInDashboard(dataset);
			updateEntryForDatasetsInDashboard(dataset);
		}
		
	}
	
	
/**
 * Prints automapping for a list without a dataset
 */
	public void getAutoMappingsFromChemRegList() {
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");

//		String propertyName = DevQsarConstants.ESTROGEN_RECEPTOR_BINDING;
//		String listName="exp_prop_CERAPP";		
//		String listName = "exp_prop_2024_02_02_from_OPERA2.9";
//		String listName="DMSO_Solubility_OChem";
		
//		String propertyName = "TTR_ANSA";
//		String listName="TTR_ANSA_Challenge";
		
//		String propertyName="Collisional Cross Section Value";
//		String listName="PubCHEMLITE112024";
		
		
		String propertyName="Property";
//		String listName="cvtdb20241001_tmm";
		String listName="exp_prop_PubChem_2024_11_27_20000_1"; 

		
		validateStructure = true;// for Dashboard not modeling
		omitSalts = false;
		omitUvcbNames = false;

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, null, omitSalts, validateStructure, validateMedian, null, null);

		String datasetName = listName;
		String datasetDescription = null;

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

//		creator.createPropertyDataset(listMappedParams, false);
		HashMap<String, Compound> hmQsarSmilesLookup = creator.getQsarSmilesLookupFromDB();

		try {
			DsstoxMapper dsstoxMapper = new DsstoxMapper(listMappedParams, this.sciDataExpertsStandardizer,
					hmQsarSmilesLookup, null, creator.acceptableAtoms, "tmarti02");

			List<ExplainedResponse> responses = dsstoxMapper.mapByExternalID(listName);
//			List<ExplainedResponse> responses = dsstoxMapper.mapBySourceSubstanceID(listName);

//			for (ExplainedResponse response:responses) {
//				System.out.println(response.record.externalId+"\t"+response.response+"\t"+response.reason+"\t"+response.record.dsstoxSubstanceId);
//			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void createWS_modeling() {
		
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = false;

		String propertyName = DevQsarConstants.WATER_SOLUBILITY;
//		String listName = "ExpProp_WaterSolubility_WithChemProp_120121";
		
		List<String> sources = Arrays.asList("prod_chemprop", "ADDoPT", "AqSolDB", "Bradley", "ChemicalBook",
				"eChemPortalAPI", "ICF", DevQsarConstants.sourceNameOChem_2024_04_03, "OFMPub", "OPERA2.8", "PubChem", "QSARDB", "ThreeM");

		//TODO change to latest pubchem
		
		ArrayList<String> listNameArray = getChemRegListNames(sources);
		
//		for (String listName:listNameArray) System.out.println(listName);
		
		
		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		BoundParameterValue pressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);

		// Unknown if the pH is for pure water value or from buffer solution. This fix is to avoid banding from echemportal but might omit legit records from other sources
		BoundParameterValue phBound = new BoundParameterValue("pH", 6.5, 7.5, true);

		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);
		bounds.add(pressureBound);
		bounds.add(phBound);

		BoundPropertyValue boundPV = new BoundPropertyValue(DevQsarConstants.MIN_WATER_SOLUBILITY_G_L,
				DevQsarConstants.MAX_WATER_SOLUBILITY_G_L);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, bounds, boundPV);

//		String datasetName = "WS v1";
//		String datasetName = "WS v1 res_qsar";
		String datasetName = "WS v2 modeling";

//		String datasetDescription = "WS from exp_prop and chemprop where 1e-14 < WS(M) < 100, 1e-11 < WS(g/L) < 990, " 
//				+ "20 < T (C) < 30, 740 < P (mmHg) < 780, 6.5 < pH < 7.5, omit LookChem";

		String datasetDescription = "WS from exp_prop and chemprop where " + DevQsarConstants.MIN_WATER_SOLUBILITY_G_L
				+ " < WS(g/L) < " + DevQsarConstants.MAX_WATER_SOLUBILITY_G_L + ", "
				+ "20 < T (C) < 30, 740 < P (mmHg) < 780, 6.5 < pH < 7.5, omit LookChem, OFMPub, and Angus";

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("LookChem");// chemical company, MAE is 35% higher than OPERA
		excludedSources.add("ANGUS Chemical Company (Chemical company)");// Chemical company
		excludedSources.add("OFMPub");// some curation issues
		excludedSources.add("PubChem");// old messy data- TODO updated data ok?
		creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);

	}
	
	

	public void createWS_SingleSource() {
		
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;
		
//		String sourceName=DevQsarConstants.sourceNameOChem_2024_04_03;
		String sourceName=DevQsarConstants.sourceNamePubChem_2024_03_20;
		
		String propertyAbbrev="WS";
		String propertyName = DevQsarConstants.WATER_SOLUBILITY;
		
		List<String> sources = Arrays.asList(sourceName);
		ArrayList<String> listNameArray = getChemRegListNames(sources);
		
//		for (String listName:listNameArray) {
//			System.out.println(listName);
//		}
		
		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		BoundParameterValue pressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);

		// Unknown if the pH is for pure water value or from buffer solution. This fix is to avoid banding from echemportal but might omit legit records from other sources
		BoundParameterValue phBound = new BoundParameterValue("pH", 6.5, 7.5, true);
		
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);
		bounds.add(pressureBound);
		bounds.add(phBound);

		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(DevQsarConstants.MIN_WATER_SOLUBILITY_G_L,
				DevQsarConstants.MAX_WATER_SOLUBILITY_G_L);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, bounds, boundPropertyValue);

		String datasetName = propertyAbbrev+" "+sourceName;

//		String datasetDescription = "WS from "+sourceName+" where " + DevQsarConstants.MIN_WATER_SOLUBILITY_G_L
//				+ " < WS(g/L) < " + DevQsarConstants.MAX_WATER_SOLUBILITY_G_L + ", "
//				+ "20 < T (C) < 30, 740 < P (mmHg) < 780, 6.5 < pH < 7.5";
		
		String datasetDescription = propertyAbbrev + " from " + sourceName + " where "
				+ getPropertyBounds(boundPropertyValue, propertyAbbrev, propertyName) + ", "
				+ getTempBounds(temperatureBound) + ", " + getPressureBounds(pressureBound) + ", "
				+ getpHBounds(phBound);
		
		System.out.println(datasetDescription);

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> includedSources = new ArrayList<>();
		includedSources.add(sourceName);
		creator.createPropertyDatasetWithSpecifiedSources(listMappedParams, false, includedSources);

	}

	// methods like these
	public void createVP() {

		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;
		
		String propertyName = DevQsarConstants.VAPOR_PRESSURE;

		List<String> sources = Arrays.asList("prod_chemprop",  "ChemicalBook", "eChemPortalAPI", "ICF",
				"OChem_2024_04_03", "OFMPub", "OPERA2.8","PubChem_2024_11_27", "QSARDB", "ThreeM");
		
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		System.out.println(listNameArray);

//		if(true) return;

		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);

		BoundPropertyValue boundPV = new BoundPropertyValue(0.0, null);

		useExperimentalDashboardSettings();
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, bounds, boundPV);

		String propAbbrev=DevQsarConstants.getConstantNameByReflection(propertyName);
		String datasetName = "exp_prop_"+propAbbrev+"_v2.0";
		System.out.println("datasetName="+datasetName);

		String datasetDescription = propertyName + " data from OPERA2.8, exp_prop, and chemprop";

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);


		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("OPERA2.9");// we are now using OPERA2.8
		excludedSources.add("PhysPropNCCT");//already have OPERA2.8
		excludedSources.add("OChem");//have newer version
		excludedSources.add("PubChem");// have newer
		excludedSources.add("PubChem_2024_03_20");// have newer
		excludedSources.add("OFMPub");// low quality data for some properties
		excludedSources.add("ANGUS Chemical Company (Chemical company)");// chemical company
		excludedSources.add("Tetko et al. 2011");//duplicate of OChem
		
//		excludedSources.add("eChemPortalAPI");// high error rate
//		excludedSources.add("ChemicalBook");// chemical company

		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		
		if (dataset!=null) {
//			addEntryForDatasetsInDashboard(dataset);	
			updateEntryForDatasetsInDashboard(dataset);
		}
		
	}

	// methods like these
	public void createD() {
		
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;
		
		String propertyName = DevQsarConstants.DENSITY;

		List<String> sources = Arrays.asList("prod_chemprop", "ChemicalBook", "eChemPortalAPI", 
				"PubChem_2024_11_27", "OChem_2024_04_03");
		
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		System.out.println(listNameArray);

		//TODO are there any logKow data with temperature listed? 
		List<BoundParameterValue> boundsParameterValues = new ArrayList<BoundParameterValue>();

		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		boundsParameterValues.add(temperatureBound);

		BoundParameterValue PressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		boundsParameterValues.add(PressureBound);

		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(null, 22.59);

		useExperimentalDashboardSettings();
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, boundsParameterValues, boundPropertyValue);

		String propAbbrev=DevQsarConstants.getConstantNameByReflection(propertyName);
		String datasetName = "exp_prop_"+propAbbrev+"_v2.0";
		System.out.println("datasetName="+datasetName);

		String datasetDescription = propertyName + " data from OPERA2.8, exp_prop, and chemprop";

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);


		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("LookChem");
		excludedSources.add("OFMPub");// curation issues
		excludedSources.add("ANGUS Chemical Company (Chemical company)");// chemical company
		excludedSources.add("PubChem");// bad data
		excludedSources.add("OChem");//have newer version
		excludedSources.add("PubChem_2024_03_20");// have newer
		excludedSources.add("Tetko et al. 2011");//duplicate of OChem

		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		
		if(dataset!=null) {
//			addEntryForDatasetsInDashboard(dataset);
			updateEntryForDatasetsInDashboard(dataset);
		}
		


	}

	// methods like these
	public void createFP() {
		
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;
		
		String propertyName = DevQsarConstants.FLASH_POINT;

		List<String> sources = Arrays.asList( "prod_chemprop", "eChemPortalAPI", "PubChem_2024_11_27", "OChem_2024_04_03");
		
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		System.out.println(listNameArray);

		//TODO are there any logKow data with temperature listed? 
		List<BoundParameterValue> boundsParameterValues = null;
		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(null, null);

		useExperimentalDashboardSettings();
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, boundsParameterValues, boundPropertyValue);

		String propAbbrev=DevQsarConstants.getConstantNameByReflection(propertyName);
		String datasetName = "exp_prop_"+propAbbrev+"_v2.0";
		System.out.println("datasetName="+datasetName);

		String datasetDescription = propertyName + " data from OPERA2.8, exp_prop, and chemprop";

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("LookChem");
		excludedSources.add("OChem");//have newer version
		excludedSources.add("PubChem");// have newer
		excludedSources.add("PubChem_2024_03_20");// have newer
		excludedSources.add("ANGUS Chemical Company (Chemical company)");// chemical company
		excludedSources.add("Tetko et al. 2011");//duplicate of OChem
		

		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		if(dataset!=null) {
//			addEntryForDatasetsInDashboard(dataset);
			updateEntryForDatasetsInDashboard(dataset);
		}

	}
	
	
	// methods like these
	public void createOPERA_ER_AR_datasets() {
		
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;
		
		List<String> propertyNames = Arrays.asList(DevQsarConstants.ESTROGEN_RECEPTOR_AGONIST,
				DevQsarConstants.ESTROGEN_RECEPTOR_ANTAGONIST, 
				DevQsarConstants.ESTROGEN_RECEPTOR_BINDING,
				DevQsarConstants.ANDROGEN_RECEPTOR_AGONIST,
				DevQsarConstants.ANDROGEN_RECEPTOR_ANTAGONIST,
				DevQsarConstants.ANDROGEN_RECEPTOR_BINDING);

		String sourceName="OPERA2.8";
		
		for (String propertyName:propertyNames) {
			createER_AR(creator, propertyName,sourceName);
		}

//		String propertyName = DevQsarConstants.ESTROGEN_RECEPTOR_AGONIST;
//		String propertyName = DevQsarConstants.ESTROGEN_RECEPTOR_ANTAGONIST;
//		String propertyName = DevQsarConstants.ESTROGEN_RECEPTOR_BINDING;
//		String propertyName = DevQsarConstants.ANDROGEN_RECEPTOR_AGONIST;
//		String propertyName = DevQsarConstants.ANDROGEN_RECEPTOR_ANTAGONIST;
//		String propertyName = DevQsarConstants.ANDROGEN_RECEPTOR_BINDING;
//		createER_AR(creator, propertyName,sourceName);
		

	}

	
	
	/**
	 * Creates one of the ER_AR datasets
	 *
	 * Note: I had set numeric values based on the text based tox values:
	 *	Inactive	0
	 * 	Active(very weak)	0.25
	 * 	Active(weak)	0.5
	 * 	Active(medium)	0.75
	 * 	Active(strong)	1
	 * 	Active(NA)	0.625
	 * 
	 * @param creator
	 * @param propertyName
	 */
	private void createER_AR(DatasetCreator creator, String propertyName,String sourceName) {
		
		List<String> sources = Arrays.asList(sourceName);
		ArrayList<String> listNameArray = getChemRegListNames(sources);
		System.out.println(listNameArray);

		List<BoundParameterValue> boundsParameterValues = null;
		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(null, null);

		useExperimentalDashboardSettings();
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, boundsParameterValues, boundPropertyValue);

		String propAbbrev=DevQsarConstants.getConstantNameByReflection(propertyName);
		String datasetName = "exp_prop_"+propAbbrev+"_v2.0";//need to update each time
		System.out.println("datasetName="+datasetName);

		String datasetDescription = propertyName + " data from "+sourceName;

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);


		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("OPERA2.9");//need to update each time

		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		if(dataset!=null) {
//			addEntryForDatasetsInDashboard(dataset);
			updateEntryForDatasetsInDashboard(dataset);
		}
	}

	public void createBIODEG_HL_HC() {
		
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;

		String propertyName = DevQsarConstants.BIODEG_HL_HC;
		List<String> sources = Arrays.asList("prod_chemprop", "OPERA2.8");
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		useExperimentalDashboardSettings();
		
		BoundPropertyValue boundPV = new BoundPropertyValue(0.0, null);// minimum 0 


		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, null, boundPV);

		String datasetName = "exp_prop_BIODEG_HL_HC_v3.0";
		
		String datasetDescription = DevQsarConstants.BIODEG_HL_HC + " data from OPERA2.8 and SRC Technical Report 98-008";
		
		//Note for OPERA2.9, if had DTXSID, the record was automapped, some mappings might be out of date, but allows one to have more mappings
		//For PhysPropNCCT, DTXRID was used to retrieve dsstox record- but mapping only accepted if automapper accepts the mapping based on the original identifiers

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("OPERA2.9");//we have OPERA2.8
		excludedSources.add("PhysPropNCCT");//already have OPERA2.8

		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		
		System.out.println("property Id="+dataset.getProperty().getId());
		System.out.println("dataset Id="+dataset.getId());
		
		if(dataset!=null) {
//			addEntryForDatasetsInDashboard(dataset);
			updateEntryForDatasetsInDashboard(dataset);
		}
		

	}

	public void createRBIODEG() {
		
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;

		String propertyName = DevQsarConstants.RBIODEG;
		List<String> sources = Arrays.asList("prod_chemprop", "OPERA2.8","NITE_OPPT");
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		useExperimentalDashboardSettings();

		BoundPropertyValue boundPV = new BoundPropertyValue(0.0, 1.0);// binary 


		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, null, boundPV);

		String datasetName = "exp_prop_RBIODEG_v3.0";
		
		String datasetDescription = DevQsarConstants.RBIODEG + " data from OPERA2.8";
		
		//Note for OPERA2.9, if had DTXSID, the record was automapped, some mappings might be out of date, but allows one to have more mappings
		//For PhysPropNCCT, DTXRID was used to retrieve dsstox record- but mapping only accepted if automapper accepts the mapping based on the original identifiers

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("OPERA2.9");//we have OPERA2.8
		excludedSources.add("PhysPropNCCT");//already have OPERA2.8

		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		
		System.out.println("property Id="+dataset.getProperty().getId());
		System.out.println("dataset Id="+dataset.getId());
		
		if(dataset!=null) {
//			addEntryForDatasetsInDashboard(dataset);
			updateEntryForDatasetsInDashboard(dataset);
		}
		
	}
	
	

	public void createRBIODEG_NITE_OPPT() {
		
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = false;

		String propertyName = DevQsarConstants.RBIODEG;
//		List<String> sources = Arrays.asList("NITE_OPPT");
		String listName = "exp_prop_RBIODEG_NITE_OPPT";

		BoundPropertyValue boundPV = new BoundPropertyValue(0.0, 1.0);// binary
		
		omitSalts=false;//want to keep since OPPT did- hopefully qsar ready smiles allows for TEST descriptor generation


		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, null, omitSalts, validateStructure, validateMedian, null, boundPV);

		String datasetName = "exp_prop_RBIODEG_NITE_OPPT v1.0";
		
		String datasetDescription = DevQsarConstants.RBIODEG + " data from NITE_OPPT";
		
		//Note for OPERA2.9, if had DTXSID, the record was automapped, some mappings might be out of date, but allows one to have more mappings
		//For PhysPropNCCT, DTXRID was used to retrieve dsstox record- but mapping only accepted if automapper accepts the mapping based on the original identifiers

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> includedSources = new ArrayList<>();
		includedSources.add("NITE_OPPT");//we have OPERA2.9
		

		Dataset dataset=creator.createPropertyDatasetWithSpecifiedSources(listMappedParams, false, includedSources);
		if(dataset==null) return;
		
		System.out.println("property Id="+dataset.getProperty().getId());
		System.out.println("dataset Id="+dataset.getId());
//		addEntryForDatasetsInDashboard(dataset);
	}
	
	public void createRBIODEG_NITE_OPPT_CAS() {
		
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = false;

		String propertyName = DevQsarConstants.RBIODEG;
//		List<String> sources = Arrays.asList("NITE_OPPT");
//		String listName = "exp_prop_RBIODEG_NITE_OPPT";

		BoundPropertyValue boundPV = new BoundPropertyValue(0.0, 1.0);// binary
		
		omitSalts=false;//want to keep since OPPT did- hopefully qsar ready smiles allows for TEST descriptor generation
		isNaive=true;

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_CASRN, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, null, omitSalts, validateStructure, validateMedian, null, boundPV);

		String datasetName = "exp_prop_RBIODEG_NITE_OPPT_BY_CAS";
		
		String datasetDescription = DevQsarConstants.RBIODEG + " data from NITE_OPPT_BY_CAS";
		
		//Note for OPERA2.9, if had DTXSID, the record was automapped, some mappings might be out of date, but allows one to have more mappings
		//For PhysPropNCCT, DTXRID was used to retrieve dsstox record- but mapping only accepted if automapper accepts the mapping based on the original identifiers

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> includedSources = new ArrayList<>();
		includedSources.add("NITE_OPPT");//we have OPERA2.9
		

		Dataset dataset=creator.createPropertyDatasetWithSpecifiedSources(listMappedParams, false, includedSources);
		if(dataset==null) return;
		
		System.out.println("property Id="+dataset.getProperty().getId());
		System.out.println("dataset Id="+dataset.getId());
//		addEntryForDatasetsInDashboard(dataset);
	}

	public void createKOC() {

		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;

		String propertyName = DevQsarConstants.KOC;
		List<String> sources = Arrays.asList("prod_chemprop", "OPERA2.8","ThreeM");
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		useExperimentalDashboardSettings();		
		BoundPropertyValue boundPV = new BoundPropertyValue(0.0, null);// minimum 0 


		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, null, boundPV);

		String datasetName = "exp_prop_KOC_v3.0";
		
		String datasetDescription = DevQsarConstants.KOC + " data from OPERA2.8, and 3M";
		
		//Note for OPERA2.9, if had DTXSID, the record was automapped, some mappings might be out of date, but allows one to have more mappings
		//For PhysPropNCCT, DTXRID was used to retrieve dsstox record- but mapping only accepted if automapper accepts the mapping based on the original identifiers

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("OPERA2.9");//we have OPERA2.8
		excludedSources.add("PhysPropNCCT");//already have OPERA2.8

		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		
		System.out.println("property Id="+dataset.getProperty().getId());
		System.out.println("dataset Id="+dataset.getId());
		
		if(dataset!=null) {
//			addEntryForDatasetsInDashboard(dataset);	
			updateEntryForDatasetsInDashboard(dataset);
		}
		

	}

	public void createKOA() {
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;

		String propertyName = DevQsarConstants.LOG_KOA;
		List<String> sources = Arrays.asList("prod_chemprop", "OPERA2.8");
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		useExperimentalDashboardSettings();
		
		BoundPropertyValue boundPV = new BoundPropertyValue(null,null);// binary 

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, null, boundPV);

		String propAbbrev=DevQsarConstants.getConstantNameByReflection(propertyName);
		String datasetName = "exp_prop_"+propAbbrev+"_v3.0";
		System.out.println("datasetName="+datasetName);
		
		String datasetDescription = propertyName + " data from OPERA2.8";
		
		//Note for OPERA2.9, if had DTXSID, the record was automapped, some mappings might be out of date, but allows one to have more mappings
		//For chemprop data, DTXRID was used to retrieve dsstox record- but mapping only accepted if automapper accepts the mapping based on the original identifiers

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("OPERA2.9");//we have OPERA2.8
		excludedSources.add("PhysPropNCCT");//already have OPERA2.8

		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		
		if(dataset!=null) {
//			addEntryForDatasetsInDashboard(dataset);
			updateEntryForDatasetsInDashboard(dataset);
		}
		
	}

	public void createST() {
		//TODO add data from TEST
		
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
//		DatasetCreator.postToDB = true;
		
		String propertyName = DevQsarConstants.SURFACE_TENSION;

		List<String> sources = Arrays.asList("prod_chemprop");
				
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		System.out.println(listNameArray);

//		if(true) return;

		List<BoundParameterValue> bounds = null;
		
		BoundPropertyValue boundPV = new BoundPropertyValue(0.0, null);

		useExperimentalDashboardSettings();
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, bounds, boundPV);

		String propAbbrev=DevQsarConstants.getConstantNameByReflection(propertyName);
		String datasetName = "exp_prop_"+propAbbrev+"_v1.0";
		System.out.println("datasetName="+datasetName);

		String datasetDescription = propertyName + " data from chemprop";

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);


		List<String> excludedSources = new ArrayList<>();
		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);

		addEntryForDatasetsInDashboard(dataset);

	}

	public void createpKa_a() {
		
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;

		String propertyName = DevQsarConstants.PKA_A;
		List<String> sources = Arrays.asList("prod_chemprop", "OPERA2.8");
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		useExperimentalDashboardSettings();
		
		BoundPropertyValue boundPV = new BoundPropertyValue(null,null);// binary 

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, null, boundPV);

		String propAbbrev=DevQsarConstants.getConstantNameByReflection(propertyName);
		String datasetName = "exp_prop_"+propAbbrev+"_v3.0";
		System.out.println("datasetName="+datasetName);
		
		String datasetDescription = propertyName + " data from OPERA2.8";
		
		//Note for OPERA2.9, if had DTXSID, the record was automapped, some mappings might be out of date, but allows one to have more mappings
		//For PhysPropNCCT, DTXRID was used to retrieve dsstox record- but mapping only accepted if automapper accepts the mapping based on the original identifiers

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("OPERA2.9");//we have OPERA2.8
		excludedSources.add("Data Warrior");// doesnt map well using automapper,already have OPERA2.9
		
		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		
		if(dataset!=null) {
//			addEntryForDatasetsInDashboard(dataset);
			updateEntryForDatasetsInDashboard(dataset);
		}

	}

	public void createpKa_b() {
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;

		String propertyName = DevQsarConstants.PKA_B;
		List<String> sources = Arrays.asList("prod_chemprop", "OPERA2.8");
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		useExperimentalDashboardSettings();
		
		BoundPropertyValue boundPV = new BoundPropertyValue(null,null);// binary 


		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, null, boundPV);

		
		String propAbbrev=DevQsarConstants.getConstantNameByReflection(propertyName);
		String datasetName = "exp_prop_"+propAbbrev+"_v3.0";
		System.out.println("datasetName="+datasetName);
		
		String datasetDescription = propertyName + " data from OPERA2.8";
		
		//Note for OPERA2.9, if had DTXSID, the record was automapped, some mappings might be out of date, but allows one to have more mappings
		//For chemprop data, DTXRID was used to retrieve dsstox record- but mapping only accepted if automapper accepts the mapping based on the original identifiers

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("OPERA2.9");//we have OPERA2.8
		excludedSources.add("Data Warrior");// doesnt map well using automapper,already have OPERA2.9
		
		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		
		if(dataset!=null) {
//			addEntryForDatasetsInDashboard(dataset);
			updateEntryForDatasetsInDashboard(dataset);
		}

	}

	public void createCLINT() {

		
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;

		String propertyName = DevQsarConstants.CLINT;
		List<String> sources = Arrays.asList("prod_chemprop", "OPERA2.8");
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		useExperimentalDashboardSettings();
		
		BoundPropertyValue boundPV = new BoundPropertyValue(null, null);// binary 


		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, null, boundPV);

		String datasetName = "exp_prop_CLINT_v2.0";
		
		String datasetDescription = DevQsarConstants.CLINT + " data from OPERA2.8, HTTK_Package_Data";
		
		//Note for OPERA2.9, if had DTXSID, the record was automapped, some mappings might be out of date, but allows one to have more mappings
		//For PhysPropNCCT, DTXRID was used to retrieve dsstox record- but mapping only accepted if automapper accepts the mapping based on the original identifiers

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("OPERA2.9");//we have OPERA2.8
		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		
		System.out.println("property Id="+dataset.getProperty().getId());
		System.out.println("dataset Id="+dataset.getId());
		
		if(dataset!=null) {
//			addEntryForDatasetsInDashboard(dataset);
			updateEntryForDatasetsInDashboard(dataset);
		}
		

	}

	public void createFUB() {
		
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;

		String propertyName = DevQsarConstants.FUB;
		List<String> sources = Arrays.asList("prod_chemprop", "OPERA2.8");
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		useExperimentalDashboardSettings();
		
		BoundPropertyValue boundPV = new BoundPropertyValue(0.0,1.0);// binary 


		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, null, boundPV);

		String datasetName = "exp_prop_FUB_v2.0";
		
		String datasetDescription = DevQsarConstants.FUB + " data from OPERA2.8, HTTK_Package_Data";
		
		//Note for OPERA2.9, if had DTXSID, the record was automapped, some mappings might be out of date, but allows one to have more mappings
		//For PhysPropNCCT, DTXRID was used to retrieve dsstox record- but mapping only accepted if automapper accepts the mapping based on the original identifiers

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("OPERA2.9");//we have OPERA2.8
		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		
		System.out.println("property Id="+dataset.getProperty().getId());
		System.out.println("dataset Id="+dataset.getId());
		
		if(dataset!=null) {
//			addEntryForDatasetsInDashboard(dataset);
			updateEntryForDatasetsInDashboard(dataset);
		}
		

	}

	public void createOH() {
		
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;

		String propertyName = DevQsarConstants.OH;
		List<String> sources = Arrays.asList("prod_chemprop", "OPERA2.8");
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		useExperimentalDashboardSettings();
		
		BoundPropertyValue boundPV = new BoundPropertyValue(0.0, null);// minimum 0 

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, null, boundPV);

		String datasetName = "exp_prop_OH_v3.0";
		
		String datasetDescription = DevQsarConstants.OH + " data from OPERA2.8, PhysPropNCCT";
		
		//Note for OPERA2.9, if had DTXSID, the record was automapped, some mappings might be out of date, but allows one to have more mappings
		//For PhysPropNCCT, DTXRID was used to retrieve dsstox record- but mapping only accepted if automapper accepts the mapping based on the original identifiers

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("OPERA2.9");//we have OPERA2.8
		excludedSources.add("PhysPropNCCT");//already have OPERA2.8
		
		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		
		System.out.println("property Id="+dataset.getProperty().getId());
		System.out.println("dataset Id="+dataset.getId());
		
		if(dataset!=null) {
//			addEntryForDatasetsInDashboard(dataset);
			updateEntryForDatasetsInDashboard(dataset);
		}
		

	}

	public void createVP_modeling() {
		// comment for diff
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = false;

		String propertyName = DevQsarConstants.VAPOR_PRESSURE;
//		String listName = "ExpProp_VP_WithChemProp_070822";
		
		List<String> sources = Arrays.asList("OPERA2.8", "ATSDR_Perfluoroalkyl_Cheminfo", "Danish_EPA_PFOA_Report_2015",
				"OChem_2024_04_03", "QSARDB", "ICF", "Danish_EPA_PFOA_Report_2005", 
				"ThreeM","prod_chemprop");
		
//		"Kurz & Ballschmiter 1999",
//		"Li et al. J. Phys. Chem. Ref. Data 32(4): 1545-1590. ",
//		"Bhhatarai et al. Environ. Sci. Technol. 2011, 45, 8120–8128."
//		"Tetko et al. 2011"
//		"Rayne et al, J. Env. Sci. and Health Part A, (2009) 44(12):1145-1199"

		
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);

		BoundPropertyValue boundPV = new BoundPropertyValue(DevQsarConstants.MIN_VAPOR_PRESSURE_MMHG,
				DevQsarConstants.MAX_VAPOR_PRESSURE_MMHG);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, bounds, boundPV);

//		String datasetName = "VP v1";
//		String datasetName = "VP v1 res_qsar";
		String datasetName = "VP v2 modeling";
//		String datasetName = "VP "+DevQsarConstants.sourceNameOChem_2024_04_03;

		String datasetDescription = "VP from exp_prop and chemprop where 1e-14 < VP(mmHg) < 1e6 and 20 < T(C) < 30 and "
				+ "omit eChemPortalAPI, PubChem, OFMPub, ChemicalBook and Angus sources";

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		
		//TODO should I instead just used the chemreg sources?
		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("eChemPortalAPI");// high error rate
		excludedSources.add("PubChem");// high error rate
		excludedSources.add("Tetko et al. 2011");//Old OChem
		excludedSources.add("OChem");// Old OChem
		excludedSources.add("OFMPub");// low quality data
		excludedSources.add("ChemicalBook");// chemical company
		excludedSources.add("OPERA 2.9");// it includes our data, 2.8 is better
		excludedSources.add("ANGUS Chemical Company (Chemical company)");// chemical company
		creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		
		
//		List<String> includedSources = new ArrayList<>();
//		includedSources.add(DevQsarConstants.sourceNameOChem_2024_04_03);
//		creator.createPropertyDatasetWithSpecifiedSources(listMappedParams, false, includedSources);


	}
	
	public void createVP_SingleSource() {
		// comment for diff
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;
		
		String propertyAbbrev="VP";
		String propertyName = DevQsarConstants.VAPOR_PRESSURE;

//		String sourceName=DevQsarConstants.sourceNameOChem_2024_04_03;
		String sourceName=DevQsarConstants.sourceNamePubChem_2024_03_20;
		
//		String listName = "ExpProp_VP_WithChemProp_070822";
		List<String> sources = Arrays.asList(sourceName);
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);

		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(DevQsarConstants.MIN_VAPOR_PRESSURE_MMHG,
				DevQsarConstants.MAX_VAPOR_PRESSURE_MMHG);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, bounds, boundPropertyValue);

		String datasetName = propertyAbbrev+" "+sourceName;

		
		String datasetDescription = propertyAbbrev +" from "+sourceName+" where "
				+ getPropertyBounds(boundPropertyValue, propertyAbbrev, propertyName)+", "+getTempBounds(temperatureBound);


		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);
		
		creator.createPropertyDatasetWithSpecifiedSources(listMappedParams, false, sources);


	}

	public void create_pKA() {
		// comment for diff
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");

		String propertyName = DevQsarConstants.PKA;// pKA, pkAa, pkAb- TODO determine which sources use which
		String listName = "ExpProp_" + propertyName + "_1130822";

		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);

		BoundPropertyValue boundPV = new BoundPropertyValue(null, null);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, null, omitSalts, validateStructure, validateMedian, bounds, boundPV);

		String datasetName = listName + "_TMM";
		String datasetDescription = "pKA data for the Dashboard";
		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);
		creator.createPropertyDataset(listMappedParams, false);

	}

	public void createPKAa() {

//		DatasetServiceImpl ds=new DatasetServiceImpl();
//		ds.delete(126);
//		if(true) return;

		// comment for diff
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");

		String propertyName = DevQsarConstants.PKA_A;
		// String listName = "ExpProp_HLC_WithChemProp_121421";

		ArrayList<String> listNameArray = new ArrayList<String>();
		listNameArray.add("ExpProp_PKAa_WithChemProp_011423");

//		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, null, null);

		String datasetName = propertyName + " from exp_prop and chemprop";
		String datasetDescription = propertyName + " from exp_prop and chemprop with no bounds";

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);
		creator.createPropertyDataset(listMappedParams, false);

	}

	public void createPKAb() {
		// comment for diff
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");

		String propertyName = DevQsarConstants.PKA_B;
		// String listName = "ExpProp_HLC_WithChemProp_121421";

		ArrayList<String> listNameArray = new ArrayList<String>();
		listNameArray.add("ExpProp_PKAb_WithChemProp_011423");

		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, null, null);

		String datasetName = propertyName + " from exp_prop and chemprop";
		String datasetDescription = propertyName + " from exp_prop and chemprop with no bounds";

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);
		creator.createPropertyDataset(listMappedParams, false);

	}

	public void createLogP() {
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;
		
		String propertyName = DevQsarConstants.LOG_KOW;

		List<String> sources = Arrays.asList("prod_chemprop", "eChemPortalAPI", "ICF", 
				"OChem_2024_04_03", "OFMPub", "OPERA2.8", "PubChem_2024_11_27","QSARDB", "ThreeM");
		
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		System.out.println(listNameArray);

		//TODO are there any logKow data with temperature listed? 
		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		List<BoundParameterValue> boundsParameterValues = new ArrayList<BoundParameterValue>();
		boundsParameterValues.add(temperatureBound);

		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(null, null);

		useExperimentalDashboardSettings();
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, boundsParameterValues, boundPropertyValue);

		String propAbbrev=DevQsarConstants.getConstantNameByReflection(propertyName);
		String datasetName = "exp_prop_"+propAbbrev+"_v2.0";
		System.out.println("datasetName="+datasetName);

		String datasetDescription = propertyName + " data from OPERA2.8, exp_prop, and chemprop";

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("OPERA2.9");// we are now using OPERA2.9
		excludedSources.add("PhysPropNCCT");//already have OPERA2.9
		excludedSources.add("OFMPub");// curation issues
		excludedSources.add("ANGUS Chemical Company (Chemical company)");// chemical company
		excludedSources.add("PubChem");// bad data
		excludedSources.add("OChem");//have newer version
		excludedSources.add("PubChem_2024_03_20");// have newer
		excludedSources.add("Tetko et al. 2011");//duplicate of OChem
		
//		excludedSources.add("eChemPortalAPI");// bad data???

		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		
		if(dataset!=null) {
//			addEntryForDatasetsInDashboard(dataset);
			updateEntryForDatasetsInDashboard(dataset);
		}

	}

	
	/**
	 * When determining logKow (octanol-water partition coefficient), the most relevant pH value to use is typically pH 7 as it represents a neutral condition and allows for a more accurate comparison of the partitioning behavior of ionizable compounds under environmentally relevant conditions; however, for specific cases where the compound's pKa is very low or high, adjusting the pH to better reflect the dominant species might be necessary.
	 */
	public void createLogP_modeling() {
		// comment for diff
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = false;

		String propertyAbbrev="LogP";
		String propertyName = DevQsarConstants.LOG_KOW;
		// String listName = "ExpProp_HLC_WithChemProp_121421";

		List<String> sources = Arrays.asList("prod_chemprop", "ICF", 
				DevQsarConstants.sourceNameOChem_2024_04_03, "OPERA2.8", 
				"QSARDB", "ThreeM");//do we want latest PubChem ?
		

		ArrayList<String> listNameArray = getChemRegListNames(sources);


		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);

		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(DevQsarConstants.MIN_LOG_KOW,
				DevQsarConstants.MAX_LOG_KOW);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, bounds, boundPropertyValue);

		String datasetName = propertyAbbrev+" v2 modeling";

		String datasetDescription = propertyAbbrev +" from exp_prop where "
				+ getPropertyBounds(boundPropertyValue, propertyAbbrev, propertyName)+", "+getTempBounds(temperatureBound);

		
		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("OFMPub");// curation issues
		excludedSources.add("ANGUS Chemical Company (Chemical company)");// chemical company
		excludedSources.add("eChemPortalAPI");// bad data
		excludedSources.add("PubChem");// bad data
		excludedSources.add("OChem");// bad data
		creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);

	}

	
	public void createLogP_SingleSource() {
		// comment for diff
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;

		String propertyAbbrev="LogP";		
		String propertyName = DevQsarConstants.LOG_KOW;

//		String sourceName=DevQsarConstants.sourceNameOChem_2024_04_03;
		String sourceName=DevQsarConstants.sourceNamePubChem_2024_03_20;
		
		List<String> sources = Arrays.asList(sourceName);
		ArrayList<String> listNameArray = getChemRegListNames(sources);


		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);

		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(DevQsarConstants.MIN_LOG_KOW,
				DevQsarConstants.MAX_LOG_KOW);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, bounds, boundPropertyValue);

		String datasetName = propertyAbbrev+" "+sourceName;

		String datasetDescription = propertyAbbrev +" from "+sourceName+" where "
				+ getPropertyBounds(boundPropertyValue, propertyAbbrev, propertyName)+", "+getTempBounds(temperatureBound);

		
		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		creator.createPropertyDatasetWithSpecifiedSources(listMappedParams, false, sources);

	}


	
	public void createHLC() {

		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;

		String propertyName = DevQsarConstants.HENRYS_LAW_CONSTANT;
		
		List<String> sources = Arrays.asList("prod_chemprop", "OPERA2.8","eChemPortalAPI",
				"OChem_2024_04_03", "ICF","PubChem_2024_11_27","Sander_v5_2");
		
		ArrayList<String> listNameArray = getChemRegListNames(sources);
		
//		listNameArray=null;

		useExperimentalDashboardSettings();
		
		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		BoundParameterValue phBound = new BoundParameterValue("pH", 6.5, 7.5, true);
		List<BoundParameterValue> boundsParameters = new ArrayList<BoundParameterValue>();
		boundsParameters.add(temperatureBound);
		boundsParameters.add(phBound);

		BoundPropertyValue boundPropertyValue=new BoundPropertyValue(DevQsarConstants.MIN_HENRYS_LAW_CONSTANT_ATM_M3_MOL/100.0,null);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST,null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, boundsParameters, boundPropertyValue);

//		listMappingParams.createChemRegMapOutstandingSourceChemicals=true;
		
		
		String propAbbrev=DevQsarConstants.getConstantNameByReflection(propertyName);
		String datasetName = "exp_prop_"+propAbbrev+"_v3.0";
		System.out.println("datasetName="+datasetName);
		
		String datasetDescription = propertyName + " data from chemprop, OPERA2.8, eChemPortalAPI, OChem, ICF, Sander_V5";
		
		//Note for OPERA2.9, if had DTXSID, the record was automapped, some mappings might be out of date, but allows one to have more mappings
		//For chemprop data, DTXRID was used to retrieve dsstox record- but mapping only accepted if automapper accepts the mapping based on the original identifiers

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("OPERA2.9");//we have OPERA2.8
		excludedSources.add("OChem");//have newer version
		excludedSources.add("PubChem");// have newer
		excludedSources.add("PubChem_2024_03_20");// have newer
		excludedSources.add("PhysPropNCCT");//already have OPERA2.9
		excludedSources.add("Sander");//we have Sander_v5 now
		excludedSources.add("Tetko et al. 2011");//duplicate of OChem


		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);

		if(dataset!=null) {
//			addEntryForDatasetsInDashboard(dataset);
			updateEntryForDatasetsInDashboard(dataset);
		}


	}

	public void createHLC_modeling() {
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = false;
		
		String propertyAbbrev="HLC";
		String propertyName = DevQsarConstants.HENRYS_LAW_CONSTANT;
		
		List<String> listSources = Arrays.asList("prod_chemprop", "OPERA2.8","ICF","Sander_v5_2",
				DevQsarConstants.sourceNameOChem_2024_04_03);//used to get chemreg list names
		
		ArrayList<String> listNameArray = getChemRegListNames(listSources);
		
		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		BoundParameterValue phBound = new BoundParameterValue("pH", 6.5, 7.5, true);

		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);
		bounds.add(phBound);

		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(
				DevQsarConstants.MIN_HENRYS_LAW_CONSTANT_ATM_M3_MOL,
				DevQsarConstants.MAX_HENRYS_LAW_CONSTANT_ATM_M3_MOL);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, bounds, boundPropertyValue);

		String datasetName = propertyAbbrev+" v2 modeling";//TODO not stored in db yet

		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("eChemPortalAPI");// bad data
		excludedSources.add("OChem");// bad data- TODO what about latest OChem source?		
		excludedSources.add("OPERA2.9");// we are using 2.8 since 2.9 duplicates some of our data
		excludedSources.add("Sander");//old version
		excludedSources.add("Tetko et al. 2011");//OCHEM in chemprop (duplicate)
		excludedSources.add("PhysPropNCCT");//duplicate of OPERA2.8 (not mapped to dtxsid?)
		
		
		String datasetDescription = propertyAbbrev +" from res_qsar where "
				+ getPropertyBounds(boundPropertyValue, propertyAbbrev, propertyName)+", "+
				getTempBounds(temperatureBound)+ ", "+
				getpHBounds(phBound)+", omitted sources: "+getSources(excludedSources);
		
		System.out.println(datasetDescription);

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);
		
		creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
				
//		creator.mapSourceChemicalsForProperty(listMappedParams);

	}
	
	String getSources(List<String>sources) {
		
		String strSources="";
		
		for (int i=0;i<sources.size();i++) {
			strSources+=sources.get(i);
			if(i<=sources.size()-2) strSources+=", ";
			if(i==sources.size()-2) strSources+="and ";
		}
		return strSources;
	}
	
	
	public void createHLC_SingleSource() {
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;

		String propertyAbbrev="HLC";
		String propertyName = DevQsarConstants.HENRYS_LAW_CONSTANT;

		//		String sourceName=DevQsarConstants.sourceNameOChem_2024_04_03;
		String sourceName=DevQsarConstants.sourceNamePubChem_2024_03_20;
		
		List<String> sources = Arrays.asList(sourceName);
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		BoundParameterValue phBound = new BoundParameterValue("pH", 6.5, 7.5, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);
		bounds.add(phBound);

		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(
				DevQsarConstants.MIN_HENRYS_LAW_CONSTANT_ATM_M3_MOL,
				DevQsarConstants.MAX_HENRYS_LAW_CONSTANT_ATM_M3_MOL);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, bounds, boundPropertyValue);

		String datasetName = propertyAbbrev+" "+sourceName;

		String datasetDescription = propertyAbbrev +" from "+sourceName+" where "
				+ getPropertyBounds(boundPropertyValue, propertyAbbrev, propertyName)+", "+getTempBounds(temperatureBound)
								+ ", "+getpHBounds(phBound);
		
		System.out.println(datasetDescription);
		
		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

				
		List<String> includedSources = new ArrayList<>();
		includedSources.add(sourceName);
		creator.createPropertyDatasetWithSpecifiedSources(listMappedParams, false, includedSources);

	}
	
	static String getPropertyBounds(BoundPropertyValue boundPV,String propertyAbbrev,String propertyUnits) {
		return boundPV.getValueMin()+" < "+propertyAbbrev+" (" +propertyUnits+") < "
				+ boundPV.getValueMax();
	}
	
	static String getpHBounds(BoundParameterValue phBound) {
		return phBound.getValueMin()+" < pH < "+phBound.getValueMax();
	}

	static String getTempBounds(BoundParameterValue temperatureBound) {
		return temperatureBound.getValueMin()+" < T (C) < "+temperatureBound.getValueMax();		
	}
	
	static String getPressureBounds(BoundParameterValue pressureBound) {
		return pressureBound.getValueMin()+" < P (mmHg) < "+pressureBound.getValueMax();		
	}
	

	/**
	 * Settings for creating datasets to expose experimental data to the Chemicals Dashboard
	 */
	void useExperimentalDashboardSettings() {
		
		dsstoxMappingId = DevQsarConstants.MAPPING_BY_LIST;
		isNaive = false;
		useValidation = true;
		requireValidation = false;
		resolveConflicts = true;
		validateConflictsTogether = true;
		omitOpsinAmbiguousNames = false;
		
		validateStructure = false;// for Dashboard not modeling
		omitSalts = false;//we want to keep salts
		omitUvcbNames=false;//we want to keep UVCBs
		validateMedian=false;//we dont care if the median values are far apart since just want the datapoint contributors and not the datapoints
		
	}
	
	
	/**
	 * Creates a dataset for the dashboard for KmHL
	 * 
	 */
	public void createKM() {

		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;

		String propertyName = DevQsarConstants.KmHL;
		List<String> sources = Arrays.asList("prod_chemprop", "OPERA2.8");
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		
		BoundPropertyValue boundPV = new BoundPropertyValue(0.0, null);// minimum 0 
		
		useExperimentalDashboardSettings();

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, null, boundPV);

//		String datasetName = propertyName;
		String datasetName = "exp_prop_KM_v3.0";
		
		String datasetDescription = DevQsarConstants.KmHL + " data from OPERA2.8";
		
		//Note for OPERA2.9, if had DTXSID, the record was automapped, some mappings might be out of date, but allows one to have more mappings
		//For PhysPropNCCT, DTXRID was used to retrieve dsstox record- but mapping only accepted if automapper accepts the mapping based on the original identifiers

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("OPERA2.9");//we have OPERA2.8
		excludedSources.add("PhysPropNCCT");//already have OPERA2.8

		
		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);

		if(dataset!=null) {
//			addEntryForDatasetsInDashboard(dataset);
			updateEntryForDatasetsInDashboard(dataset);
		}
		
		

//		creator.mapSourceChemicalsForProperty(listMappedParams);

	}

	private void addEntryForDatasetsInDashboard(Dataset dataset) {
		String sql="INSERT into qsar_datasets.datasets_in_dashboard  (created_at,created_by,fk_property_id,fk_datasets_id) "
				+ "VALUES (CURRENT_TIMESTAMP,"
				+ "'tmarti02',"
				+ dataset.getProperty().getId()+","+dataset.getId()+");";
		SqlUtilities.runSQLUpdate(SqlUtilities.getConnectionPostgres(), sql);
	}
	
	
	private void updateEntryForDatasetsInDashboard(Dataset dataset) {
		
		String sql="update qsar_datasets.datasets_in_dashboard set fk_datasets_id="+dataset.getId()+
				", updated_at=CURRENT_TIMESTAMP, updated_by='tmarti02' "
				+ "where fk_property_id="+dataset.getProperty().getId()+";";
		
		System.out.println(sql);
		
		SqlUtilities.runSQLUpdate(SqlUtilities.getConnectionPostgres(), sql);
	}

	public void createKM_modeling() {
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");

		String propertyName = DevQsarConstants.KmHL;

		ArrayList<String> listNameArray = new ArrayList<String>();
		listNameArray.add("ExpProp_KmHL_2024_01_25");

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, null, null);

		String datasetName = propertyName;

		String datasetDescription = DevQsarConstants.KmHL + " data from OPERA";

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();

		excludedSources.add("PhysPropNCCT");// OPERA is mostly overlapping and more curated
		creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);

//		creator.mapSourceChemicalsForProperty(listMappedParams);

	}

	/**
	 * create melting point dataset based on CAS registration
	 */
	public void createMPRegisteredCAS() {
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");

		String propertyName = DevQsarConstants.MELTING_POINT;

		BoundParameterValue PressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);

		BoundPropertyValue boundPV = new BoundPropertyValue(null, null);

		MappingParams casrnMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_CASRN, null, true, false,
				false, false, false, false, true, null, true, validateStructure, validateMedian, bounds, boundPV);

		String casrnMappingName = "ExpProp_MP_CASRN";
		String casrnMappingDescription = "ExpProp MP mapped by CAS";
		DatasetParams casrnMappedParams = new DatasetParams(casrnMappingName, casrnMappingDescription, propertyName,
				casrnMappingParams);

		creator.createPropertyDataset(casrnMappedParams, false);
	}

	/**
	 * create melting point dataset from expprop records based on list registration
	 */
	public void createMP() {

		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;
		
		String propertyName = DevQsarConstants.MELTING_POINT;

//				
		List<String> sources = Arrays.asList("prod_chemprop", "ChemicalBook", "eChemPortalAPI", 
				"OChem_2024_04_03", "OFMPub", "OPERA2.8","PubChem_2024_11_27", "QSARDB", "ThreeM");
		
		ArrayList<String> listNameArray = getChemRegListNames(sources);
		System.out.println(listNameArray);

//		if(true) return;

		//TODO are there any MP data with pressure listed? 
		BoundParameterValue PressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		List<BoundParameterValue> boundsParameterValues = new ArrayList<BoundParameterValue>();
		boundsParameterValues.add(PressureBound);

		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(null, null);

		useExperimentalDashboardSettings();
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, boundsParameterValues, boundPropertyValue);

		String propAbbrev=DevQsarConstants.getConstantNameByReflection(propertyName);
		String datasetName = "exp_prop_"+propAbbrev+"_v2.0";
		System.out.println("datasetName="+datasetName);

		String datasetDescription = propertyName + " data from OPERA2.8, exp_prop, and chemprop";

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);


		List<String> excludedSources = new ArrayList<>();

		excludedSources.add("OPERA2.9");// we are now using OPERA2.9
		excludedSources.add("LookChem");// OPERA2.9 has it when corroborated
		excludedSources.add("PhysPropNCCT");//already have OPERA2.9
		excludedSources.add("OFMPub");// curation issues
		excludedSources.add("ANGUS Chemical Company (Chemical company)");// chemical company
		excludedSources.add("OChem");//have newer version
		excludedSources.add("PubChem");// bad data
		excludedSources.add("PubChem_2024_03_20");// have newer
		excludedSources.add("Tetko et al. 2011");//duplicate of OChem

		//Following will get excluded by mapping code anyways:
//		excludedSources.add("CSDeposition Service");// no URL, not much data
//		excludedSources.add("ONSChallenge");// URL inactive, only 1 chemical
//		excludedSources.add("Alfa Aesar (Chemical company)");// chemical company
//		excludedSources.add("Indofine (Chemical company)");// chemical company
//		excludedSources.add("LKT Labs (Chemical company)");// chemical company
//		excludedSources.add("MolMall");// chemical company
//		excludedSources.add("ChemicalBook");// chemical company
//		excludedSources.add("Renaissance Chemicals (Chemical company)");// chemical company
//		excludedSources.add("SLI Technologies");// chemical company
//		excludedSources.add("SynQuest Labs (Chemical company)");// chemical company
//		excludedSources.add("Tokyo Chemical Industry (Chemical company)");// chemical company
//		excludedSources.add("Matrix Scientific (Chemical company)");// chemical company
//		excludedSources.add("Biosynth (Chemical company)");// chemical company
//		excludedSources.add("Merck Millipore (Chemical company)");// chemical company
//		excludedSources.add("Manchester Organics");// chemical company
//		excludedSources.add("Key Organics (Chemical company)");// chemical company
//		excludedSources.add("Oakwood (Chemical company)");// chemical company
//		excludedSources.add("Fluorochem (Chemical company)");// chemical company
//		excludedSources.add("Sinova (Chemical company)");// chemical company
//		excludedSources.add("Chemodex");// chemical company
//		excludedSources.add("ANGUS Chemical Company (Chemical company)");// chemical company
//		excludedSources.add("Aspira Scientific");// chemical company

		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		
		if(dataset!=null) {
//			addEntryForDatasetsInDashboard(dataset);
			updateEntryForDatasetsInDashboard(dataset);
		}

	}
	
	
	
	public void createToxCast_TTR_Binding() {

		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;
		
		String propertyName = DevQsarConstants.TTR_BINDING;
		String set="training";
		String listName="ToxCast_TTR_Binding";

		List<BoundParameterValue> boundsParameterValues = null;
		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(null, null);

		omitSalts=false;
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, null, omitSalts, validateStructure, validateMedian, boundsParameterValues, boundPropertyValue);

		String datasetName = "TTR_Binding_"+set+"_remove_bad_max_conc";
		System.out.println("datasetName="+datasetName);
		String datasetDescription = propertyName+" "+set+" set (omit based on max_conc)";

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		
//		creator.createPropertyDataset(listMappedParams, false);
		
		Dataset datasetDB = creator.getDataset(listMappedParams);
		if (datasetDB != null && DatasetCreator.postToDB) {
			System.out.println("already have " + listMappedParams.datasetName + " in db");
			return;
		}

		System.out.println("Selecting experimental property data for " + listMappedParams.propertyName + "...");
		long t5 = System.currentTimeMillis();
		List<PropertyValue> propertyValues = creator.getPropertyValues(listMappedParams,true,true);
		long t6 = System.currentTimeMillis();
		System.out.println("Selection time = " + (t6 - t5) / 1000.0 + " s");
		System.out.println("Raw records:" + propertyValues.size());

		updateTTR_Binding_PropertyValues(propertyValues,"training");
		System.out.println("After updating property values:" + propertyValues.size());

		
//		System.out.println(Utilities.gson.toJson(propertyValues));

		creator.convertPropertyValuesToDatapoints(propertyValues, listMappedParams, false);

	}
	
	
	public void createRatLC50_CoMPAIT() {

		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = false;
		
		String unitsAbbrev=DevQsarConstants.LOG_PPM;
		String unitsName=DevQsarConstants.getConstantNameByReflection(unitsAbbrev);
		
		String unitsContributorAbbrev=DevQsarConstants.PPM;
		String unitsContributorName=DevQsarConstants.getConstantNameByReflection(unitsContributorAbbrev);
		
		String propertyName = DevQsarConstants.FOUR_HOUR_INHALATION_RAT_LC50;
		String set="training";
		String listName="4_hr_rat_inhalation_LC50_CoMPAIT";

		creator.finalUnitsNameMap.put(propertyName, unitsName);
		creator.contributorUnitsNameMap.put(propertyName, unitsContributorName);
		
		List<BoundParameterValue> boundsParameterValues = null;
		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(null, null);

		omitSalts=true;
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, null, omitSalts, validateStructure, validateMedian, boundsParameterValues, boundPropertyValue);

		String datasetName = propertyName+" in "+unitsAbbrev+"_CoMPAIT";
		System.out.println("datasetName="+datasetName);
		
		String datasetDescription = datasetName;

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		
//		creator.createPropertyDataset(listMappedParams, false);
		
		Dataset datasetDB = creator.getDataset(listMappedParams);
		if (datasetDB != null && DatasetCreator.postToDB) {
			System.out.println("already have " + listMappedParams.datasetName + " in db");
			return;
		}

		System.out.println("Selecting experimental property data for " + listMappedParams.propertyName + "...");
		long t5 = System.currentTimeMillis();
		List<PropertyValue> propertyValues = creator.getPropertyValues(listMappedParams,true,true);
		long t6 = System.currentTimeMillis();
		System.out.println("Selection time = " + (t6 - t5) / 1000.0 + " s");
		System.out.println("Raw records:" + propertyValues.size());
		
		for(int i=0;i<propertyValues.size();i++) {
			PropertyValue pv=propertyValues.get(i);
			String abbrev=pv.getUnit().getAbbreviation();
			if(!abbrev.equals(unitsAbbrev)) {
				propertyValues.remove(i--);
				continue;
			} 
		}
		
		System.out.println("After updating property values:" + propertyValues.size());
//		System.out.println(Utilities.gson.toJson(propertyValues));
		creator.convertPropertyValuesToDatapoints(propertyValues, listMappedParams, false);

	}
	
	Object getParameterValue(PropertyValue propertyValue, String parameterName) {

		for (ParameterValue pv:propertyValue.getParameterValues()) {
			if(pv.getParameter().getName().equals(parameterName)) {
				if(pv.getValuePointEstimate()!=null) return pv.getValuePointEstimate();
				else if (pv.getValueText()!=null) return pv.getValueText();
			}
		}
		return null;
	}
	
	

	/**
	 * create melting point dataset from expprop records based on list registration
	 * 
	 * TODO there are a lot of no hits in DSSTOX
	 * 
	 */
	public void createMP_modeling() {

		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");

		//TODO fix the sources to use chemreg lists based on specific data sources
		
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

		BoundPropertyValue boundPV = new BoundPropertyValue(DevQsarConstants.MIN_MELTING_POINT_C,
				DevQsarConstants.MAX_MELTING_POINT_C);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, bounds, boundPV);

//		String datasetName = "MP v1";
//		String datasetName = "MP v1 res_qsar";
		String datasetName = "MP v2 modeling";

		String datasetDescription = "MP from exp_prop and chemprop for modeling";

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();

		excludedSources.add("OFMPub");// some curation issues
		excludedSources.add("CSDeposition Service");// no URL, not much data
		excludedSources.add("ONSChallenge");// URL inactive, only 1 chemical

		excludedSources.add("LookChem");// chemical company, MAE same as opera, large set
		excludedSources.add("Alfa Aesar (Chemical company)");// chemical company
		excludedSources.add("Indofine (Chemical company)");// chemical company
		excludedSources.add("LKT Labs (Chemical company)");// chemical company
		excludedSources.add("MolMall");// chemical company
		excludedSources.add("ChemicalBook");// chemical company
		excludedSources.add("Renaissance Chemicals (Chemical company)");// chemical company
		excludedSources.add("SLI Technologies");// chemical company
		excludedSources.add("SynQuest Labs (Chemical company)");// chemical company
		excludedSources.add("Tokyo Chemical Industry (Chemical company)");// chemical company
		excludedSources.add("Matrix Scientific (Chemical company)");// chemical company
		excludedSources.add("Biosynth (Chemical company)");// chemical company
		excludedSources.add("Merck Millipore (Chemical company)");// chemical company
		excludedSources.add("Manchester Organics");// chemical company
		excludedSources.add("Key Organics (Chemical company)");// chemical company
		excludedSources.add("Oakwood (Chemical company)");// chemical company
		excludedSources.add("Fluorochem (Chemical company)");// chemical company
		excludedSources.add("Sinova (Chemical company)");// chemical company
		excludedSources.add("Chemodex");// chemical company
		excludedSources.add("ANGUS Chemical Company (Chemical company)");// chemical company
		excludedSources.add("Aspira Scientific");// chemical company

		creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);

	}
	
	
	public void createMP_SingleSource() {

		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;

		String propertyName = DevQsarConstants.MELTING_POINT;
		String propertyAbbrev="MP";

		String sourceName=DevQsarConstants.sourceNamePubChem_2024_03_20;
//		String sourceName=DevQsarConstants.sourceNameOChem_2024_04_03;
		
		List<String> sources = Arrays.asList(sourceName);
		ArrayList<String> listNameArray = getChemRegListNames(sources);
		if(listNameArray==null) {
			return;
		}


		BoundParameterValue PressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);

		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(DevQsarConstants.MIN_MELTING_POINT_C,
				DevQsarConstants.MAX_MELTING_POINT_C);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, bounds, boundPropertyValue);

		String datasetName = propertyAbbrev+" "+sourceName;

		String datasetDescription = propertyAbbrev +" from "+sourceName+" where "
				+ getPropertyBounds(boundPropertyValue, propertyAbbrev, propertyName)+", "+getPressureBounds(PressureBound);


		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		creator.createPropertyDatasetWithSpecifiedSources(listMappedParams, false, sources);


	}
	
	
	
	
	public void create_LC50_Ecotox_modeling() {

		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
//		DatasetCreator.postToDB = true;//otherwise wont create the dataset
		DatasetCreator.postToDB = false;//otherwise wont create the dataset
		
		String sourceName="ECOTOX_2023_12_14";
		String chemicalListName="exp_prop_"+sourceName;				
		
//		String duration="96HR";
//		String animalAbbrev="FHM";
//		String typeAnimal=ChangeKeptPropertyValues.typeAnimalFatheadMinnow;		
//		String propertyName = DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50;

//		String duration="96HR";
//		String animalAbbrev="BG";
//		String typeAnimal=ChangeKeptPropertyValues.typeAnimalFish;		
//		String propertyName = DevQsarConstants.NINETY_SIX_HOUR_BLUEGILL_LC50;
		
//		String duration="96HR";
//		String animalAbbrev="RT";
//		String typeAnimal=ChangeKeptPropertyValues.typeAnimalFish;		
//		String propertyName = DevQsarConstants.NINETY_SIX_HOUR_RAINBOW_TROUT_LC50;
		
		String duration="48HR";
		String animalAbbrev="DM";
		String typeAnimal=ChangeKeptPropertyValues.typeAnimalDaphnid;		
		String propertyName = DevQsarConstants.FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50;
		
		
		String propAbbrev=DevQsarConstants.getConstantNameByReflection(propertyName) ;
		String endpoint=duration+"_"+animalAbbrev+"_LC50";
		String datasetNameOriginal = "exp_prop_"+endpoint+"_v1 modeling";


//		excludeBasedOnMissingExposureType=false;
//		excludeBasedOnPredictedWS=false;
//		excludeBasedOnBaselineToxicity=false;
//		String datasetName = "exp_prop_"+endpoint+"_v1 modeling";
		
//		excludeBasedOnMissingExposureType=true;
//		excludeBasedOnPredictedWS=false;
//		excludeBasedOnBaselineToxicity=false;
//		String datasetName = "exp_prop_"+endpoint+"_v2 modeling";

		
//		excludeBasedOnMissingExposureType=true;
//		excludeBasedOnPredictedWS=true;
//		excludeBasedOnBaselineToxicity=false;
//		String datasetName = "exp_prop_"+endpoint+"_v3 modeling";


//		excludeBasedOnMissingExposureType=true;
//		excludeBasedOnPredictedWS=true;
//		excludeBasedOnBaselineToxicity=true;
//		String datasetName = "exp_prop_"+endpoint+"_v4 modeling";

		
		excludeBasedOnMissingExposureType=true;
		excludeBasedOnPredictedWS=true;
		excludeBasedOnBaselineToxicity=true;
		excludeBasedOnConcentrationType=true;
		String datasetName = "exp_prop_"+endpoint+"_v5 modeling";


		List<BoundParameterValue> boundsParameterValues = null;
		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(null, null);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, chemicalListName, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, null, omitSalts, validateStructure, validateMedian, boundsParameterValues, boundPropertyValue);


		String datasetDescription = endpoint+" from "+sourceName+
				", excludeBasedOnMissingExposureType="+excludeBasedOnMissingExposureType+
				", excludeBasedOnPredictedWS="+excludeBasedOnPredictedWS+
				", excludeBasedOnBaselineToxicity="+excludeBasedOnBaselineToxicity+
				", excludeBasedOnConcentrationType="+excludeBasedOnConcentrationType;
		
		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> includedSources = new ArrayList<>();
		includedSources.add(sourceName);
		
		creator.createPropertyDatasetWithSpecifiedSources(datasetNameOriginal, listMappedParams, false, includedSources,
				excludeBasedOnPredictedWS, excludeBasedOnBaselineToxicity, excludeBasedOnMissingExposureType,
				excludeBasedOnConcentrationType, typeAnimal);
		
	}
	
	/**
	 * creates Boiling point dataset using records from chemreg with CAS
	 * registration
	 */
	public void createBPRegisteredCAS() {
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");

		String propertyName = DevQsarConstants.BOILING_POINT;

		BoundParameterValue PressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);

		BoundPropertyValue boundPV = new BoundPropertyValue(-273.0, null);

		MappingParams casrnMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_CASRN, null, true, false,
				false, false, false, false, true, null, true, validateStructure, validateMedian, bounds, boundPV);

		String casrnMappingName = "ExpProp_BP_CASRN";
		String casrnMappingDescription = "CAS mapped ExpProp BP";
		DatasetParams casrnMappedParams = new DatasetParams(casrnMappingName, casrnMappingDescription, propertyName,
				casrnMappingParams);

		creator.createPropertyDataset(casrnMappedParams, false);
	}

	public void createLogPRegisteredCAS() {
		// comment for diff
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");

		String propertyName = DevQsarConstants.LOG_KOW;

		BoundParameterValue PressureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);

		BoundPropertyValue boundPV = new BoundPropertyValue(null, null);

		MappingParams casrnMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_CASRN, null, true, false,
				false, false, false, false, true, null, true, validateStructure, validateMedian, bounds, boundPV);

		String casrnMappingName = "ExpProp_LogKOW_CASRN";
		String casrnMappingDescription = "Exprop LogKOW mapped by CAS";
		DatasetParams casrnMappedParams = new DatasetParams(casrnMappingName, casrnMappingDescription, propertyName,
				casrnMappingParams);

		creator.createPropertyDataset(casrnMappedParams, false);
	}

	/**
	 * creates Henry's law constant dataset using records from chemreg with CAS
	 * registration.
	 */
	public void createHLCRegisteredCAS() {
		// comment for diff
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");

		String propertyName = DevQsarConstants.HENRYS_LAW_CONSTANT;

		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		BoundParameterValue phBound = new BoundParameterValue("pH", 6.0, 8.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);
		bounds.add(phBound);

		BoundPropertyValue boundPV = new BoundPropertyValue(null, null);

		MappingParams casrnMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_CASRN, null, true, false,
				false, false, false, false, true, null, true, validateStructure, validateMedian, bounds, boundPV);

		String casrnMappingName = "ExpProp_HLC_CASRN";
		String casrnMappingDescription = "ExpProp HLC mapped by CAS";
		DatasetParams casrnMappedParams = new DatasetParams(casrnMappingName, casrnMappingDescription, propertyName,
				casrnMappingParams);

		creator.createPropertyDataset(casrnMappedParams, false);

	}

//	public void createBCF() {
//		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
//
//		String propertyName = DevQsarConstants.LOG_BCF_FISH_WHOLEBODY;
//		String listName = "ExpProp_bcfwbf_072222";
//
//		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, false, true,
//				false, true, true, false, false, null, true, validateStructure, validateMedian, null, null);
//		String datasetName = "ExpProp BCF Fish_TMM";
//		String datasetDescription = "BCF values for fish with whole body tissue (measured ZEROS OMITTED)";
//
//		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
//				listMappingParams);
//		creator.createPropertyDataset(listMappedParams, false);
//
//	}


	
	
	
	public static ArrayList<String> getChemRegListNames(List<String> sources) {
		ArrayList<String> listNames = new ArrayList<String>();

		for (String source : sources) {

			if (source.equals(DevQsarConstants.sourceNameOChem)) {
				for (int i = 1; i <= 12; i++) { 
					listNames.add("exp_prop_2024_02_02_from_OChem_40000_" + i);
				}
			} else if (source.equals(DevQsarConstants.sourceNameOChem_2024_04_03)) {
				for (int i = 1; i <= 13; i++) {
					listNames.add("exp_prop_2024_04_03_from_OChem_40000_" + i);
				}
			} else if (source.equals(DevQsarConstants.sourceNamePubChem_2024_03_20)) {
				for (int i = 1; i <= 5; i++) {
					listNames.add("exp_prop_PubChem_2024_03_20_" + i);
				}
			} else if (source.equals(DevQsarConstants.sourceNamePubChem_2024_11_27)) {
				for (int i = 1; i <= 2; i++) {
					listNames.add("exp_prop_PubChem_2024_11_27_20000_" + i);
				}
			} else if (source.equals("Sander_v5_2")) {
				listNames.add("exp_prop_2024_04_04_from_Sander_v5_2");
			} else if (source.equals("NITE_OPPT")) {
				listNames.add("exp_prop_2025_03_24_NITE_OPPT");
			} else if (source.equals("Arnot 2006")) {
				listNames.add("exp_prop_Arnot 2006");//created
			} else if (source.equals("Burkhard")) {
				listNames.add("exp_prop_Burkhard");
			} else if (source.equals("ECOTOX_2024_12_12") ||
					source.equals("QSAR_Toolbox")) {
				listNames.add("exp_prop_2025_03_25_"+source);
			} else {
				listNames.add("exp_prop_2024_02_02_from_" + source);
			}
			
		}

		boolean haveMissing=false;
		
		for (String listName : listNames) {
			String sql = "select id from chemical_lists cl where cl.name='" + listName + "';";
//			System.out.println(sql);
			String id = SqlUtilities.runSQL(SqlUtilities.getConnectionDSSTOX(), sql);
//			System.out.println(listName+"\t"+id);
			if (id == null) {
				System.out.println("Missing chemreg list for listName=" + listName);
				haveMissing=true;
			}
		}
		
		if(haveMissing)return null;

		return listNames;
	}

	/**
	 * create boiling point dataset based on chemreg lists
	 */
	public void createBP() {

		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;
		
		String propertyName = DevQsarConstants.BOILING_POINT;

		List<String> sources = Arrays.asList("prod_chemprop", "ChemicalBook", "eChemPortalAPI", "ICF", 
				"OChem_2024_04_03","OFMPub", "OPERA2.8", "PubChem_2024_11_27","ThreeM");
		
//		"OChem_2024_04_03", "PubChem_2024_11_27"
		
		
		ArrayList<String> listNameArray = getChemRegListNames(sources);

		System.out.println(listNameArray);

//		if(true) return;

		BoundParameterValue PressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		List<BoundParameterValue> boundsParameterValues = new ArrayList<BoundParameterValue>();
		boundsParameterValues.add(PressureBound);

		BoundPropertyValue boundProperty = new BoundPropertyValue(null,null);

		useExperimentalDashboardSettings();
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, boundsParameterValues, boundProperty);

		String propAbbrev=DevQsarConstants.getConstantNameByReflection(propertyName);
		String datasetName = "exp_prop_"+propAbbrev+"_v2.0";
		System.out.println("datasetName="+datasetName);

		String datasetDescription = propertyName + " data from OPERA2.8, exp_prop, and chemprop";

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();

		excludedSources.add("OPERA2.9");// we are now using OPERA2.8
		excludedSources.add("LookChem");// OPERA2.9 has it when corroborated
		excludedSources.add("PhysPropNCCT");//already have OPERA2.8
		excludedSources.add("ANGUS Chemical Company (Chemical company)");// chemical company
		excludedSources.add("OChem");//have newer version
		excludedSources.add("PubChem");// have newer
		excludedSources.add("PubChem_2024_03_20");// have newer
		excludedSources.add("OFMPub");// low quality data for some properties
		excludedSources.add("Tetko et al. 2011");//duplicate of OChem
		
		//Following wont make it past mapping code because source chemical doesnt have enough info to map to dsstox:
		
//		excludedSources.add("eChemPortalAPI");// bad data
//		excludedSources.add("Oxford University Chemical Safety Data (No longer updated)");// bad data
//		excludedSources.add("Alfa Aesar (Chemical company)");// chemical company
//		excludedSources.add("Biosynth (Chemical company)");// chemical company
//		excludedSources.add("SynQuest Labs (Chemical company)");// chemical company
//		excludedSources.add("Arkema (Chemical company)");// chemical company
//		excludedSources.add("Aspira Scientific");// chemical company
//		excludedSources.add("Matrix Scientific (Chemical company)");// chemical company
//		excludedSources.add("ChemicalBook");// chemical company
//		excludedSources.add("Manchester Organics");// chemical company
//		excludedSources.add("LKT Labs (Chemical company)");// chemical company
//		excludedSources.add("Oakwood (Chemical company)");// chemical company
//		excludedSources.add("Fluorochem (Chemical company)");// chemical company
//		excludedSources.add("ANGUS Chemical Company (Chemical company)");// chemical company
//		excludedSources.add("Chemodex");// chemical company
//		excludedSources.add("MolMall");// chemical company
//		excludedSources.add("SLI Technologies");// chemical company
//		excludedSources.add("Helix Molecules");// chemical company


		Dataset dataset=creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		if(dataset!=null) {
//			addEntryForDatasetsInDashboard(dataset);
			updateEntryForDatasetsInDashboard(dataset);
		}

	}

	/**
	 * create boiling point dataset based on chemreg lists
	 */
	public void createBP_modeling() {
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;
		
//		List<String> sources = Arrays.asList(DevQsarConstants.sourceNameOChem_2024_04_03);
		
		List<String> sources = Arrays.asList(DevQsarConstants.sourceNameOChem_2024_04_03,"OPERA2.8",
				"prod_chemprop","ICF", "ThreeM");

		ArrayList<String> listNameArray = getChemRegListNames(sources);
		
		if(listNameArray==null) {
			return;
		}
		

		String propertyName = DevQsarConstants.BOILING_POINT;

		BoundParameterValue PressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);

		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(DevQsarConstants.MIN_BOILING_POINT_C,
				DevQsarConstants.MAX_BOILING_POINT_C);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, bounds, boundPropertyValue);

		
//		listMappingParams.createChemRegMapOutstandingSourceChemicals=true;
		
//		String datasetName = "BP v1";
//		String datasetName = "BP v1 res_qsar";
//		String datasetName = "BP v1 modeling";
//		String datasetName = "BP "+DevQsarConstants.sourceNameOChem_2024_04_03;
		String datasetName = "BP v2 modeling";

		String datasetDescription = "BP from exp_prop and chemprop for modeling, updated OChem";

		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);

		List<String> excludedSources = new ArrayList<>();
		excludedSources.add("OPERA2.9");//*** Better to use 2.8 because 2.9 has duplicate data
		excludedSources.add("PhysPropNCCT");//we are using OPERA2.8 (which is derived from PhysPropNCCT)
		excludedSources.add("OChem");//Have newer version
		
		excludedSources.add("eChemPortalAPI");// bad data
		excludedSources.add("PubChem");// bad data
		excludedSources.add("PubChem_2024_03_20");// bad data ?
		
		excludedSources.add("Oxford University Chemical Safety Data (No longer updated)");// bad data
		excludedSources.add("OFMPub");// bad data
		excludedSources.add("LookChem");// chemical company, very large set, MAE 1.5x opera
		
		excludedSources.add("Alfa Aesar (Chemical company)");// chemical company
		excludedSources.add("Biosynth (Chemical company)");// chemical company
		excludedSources.add("SynQuest Labs (Chemical company)");// chemical company
		excludedSources.add("Arkema (Chemical company)");// chemical company
		excludedSources.add("Aspira Scientific");// chemical company
		excludedSources.add("Matrix Scientific (Chemical company)");// chemical company
		excludedSources.add("ChemicalBook");// chemical company
		excludedSources.add("Manchester Organics");// chemical company
		excludedSources.add("LKT Labs (Chemical company)");// chemical company
		excludedSources.add("Oakwood (Chemical company)");// chemical company
		excludedSources.add("Fluorochem (Chemical company)");// chemical company
		excludedSources.add("ANGUS Chemical Company (Chemical company)");// chemical company
		excludedSources.add("Chemodex");// chemical company
		excludedSources.add("MolMall");// chemical company
		excludedSources.add("SLI Technologies");// chemical company
		excludedSources.add("Helix Molecules");// chemical company
		creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		
//		List<String> includedSources = new ArrayList<>();
//		includedSources.add(DevQsarConstants.sourceNameOChem_2024_04_03);
//		creator.createPropertyDatasetWithSpecifiedSources(listMappedParams, false, includedSources);

	}
	
	/**
	 * create boiling point dataset based on chemreg lists
	 */
	public void createBP_SingleSource() {

		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		DatasetCreator.postToDB = true;

		String propertyName = DevQsarConstants.BOILING_POINT;
		String propertyAbbrev="BP";
//		String sourceName=DevQsarConstants.sourceNameOChem_2024_04_03;
		String sourceName=DevQsarConstants.sourceNamePubChem_2024_03_20;
		

		List<String> sources = Arrays.asList(sourceName);
		ArrayList<String> listNameArray = getChemRegListNames(sources);
		
		if(listNameArray==null) {
			return;
		}
				
		BoundParameterValue PressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);

		BoundPropertyValue boundPropertyValue = new BoundPropertyValue(DevQsarConstants.MIN_BOILING_POINT_C,
				DevQsarConstants.MAX_BOILING_POINT_C);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether, omitOpsinAmbiguousNames,
				omitUvcbNames, listNameArray, omitSalts, validateStructure, validateMedian, bounds, boundPropertyValue);

//		listMappingParams.createChemRegMapOutstandingSourceChemicals=true;
		
		
		String datasetName = propertyAbbrev+" "+sourceName;

		String datasetDescription = propertyAbbrev +" from "+sourceName+" where "
				+ getPropertyBounds(boundPropertyValue, propertyAbbrev, propertyName)+", "+getPressureBounds(PressureBound);
								
		DatasetParams listMappedParams = new DatasetParams(datasetName, datasetDescription, propertyName,
				listMappingParams);
		creator.createPropertyDatasetWithSpecifiedSources(listMappedParams, false, sources);

	}

}
