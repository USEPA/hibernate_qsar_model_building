package gov.epa.run_from_java.scripts;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.poi.xssf.usermodel.XSSFPivotCacheRecords;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DataPointDaoImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
//import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.endpoints.datasets.BoundParameterValue;
import gov.epa.endpoints.datasets.DatasetCreator;
import gov.epa.endpoints.datasets.DatasetParams;
import gov.epa.endpoints.datasets.DatasetParams.MappingParams;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;
import gov.epa.run_from_java.scripts.GetExpPropInfo.GetExpPropInfo;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.OPERA_Old.SqliteUtilities;
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer;
import kong.unirest.HttpResponse;

public class DatasetCreatorScript {

	//Default settings:
	String dsstoxMappingId=DevQsarConstants.MAPPING_BY_LIST;
	boolean isNaive = false;
	boolean useValidation=true;
	boolean requireValidation=false;
	boolean resolveConflicts=true;
	boolean validateConflictsTogether=true;
	boolean omitOpsinAmbiguousNames=false;
	boolean omitUvcbNames=true;
	boolean omitSalts=true;
	boolean validateStructure=true;


	String workflow="qsar-ready";
	String serverHost="https://hcd.rtpnc.epa.gov";
	SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY,workflow,serverHost);

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
		String qsarExpPropID2=dp.getQsar_exp_prop_property_values_id().replace("EXP", "");
		String [] ids=qsarExpPropID2.split("\\|");
		if (ids.length==1) {
			qsarExpPropID2=Integer.parseInt(ids[0])+"";
		} else if (ids.length==2) {
			String id1=Integer.parseInt(ids[0])+"";
			String id2=Integer.parseInt(ids[1])+"";
			qsarExpPropID2=id1+"|"+id2;
		}
		return qsarExpPropID2;
	}
	
	
	void createFHM_LC50() {

		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		
		boolean isNaive=true;
		boolean requireValidation=false;
		boolean useValidation=false;
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_DTXSID, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, null, omitSalts,validateStructure);

		
		String datasetName = DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50+" ToxValv93";
		
		
		String datasetDescription = "96 hour fathead minnow LC50 data taken from toxval v93. SQL filter: toxval_type = LC50, "+
				"rs.quality not like '3%' AND rs.quality not like '4%' AND "+
				"tv.media in ('-', 'Fresh water') AND ttd.toxval_type_supercategory in ('Point of Departure', 'Toxicity Value', 'Lethality Effect Level') AND "+
				"tv.toxval_numeric > 0";

		System.out.println(datasetDescription);
		
		String propertyName=DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50;
		
		List<BoundParameterValue> bounds = null;//leave empty
		
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				bounds);
		
		List<String>excludedSources=new ArrayList<>();	
//		excludedSources.add("ECHA eChemPortal: ECHA REACH");// bad data

		
		creator.createPropertyDatasetExcludeSources(listMappedParams, false,excludedSources);

	}
	
	
	public static void main(String[] args) {
//		DatasetCreatorScript dcs=new DatasetCreatorScript();
//		dcs.testStandardize();
		
		DatasetServiceImpl ds=new DatasetServiceImpl();
//		for(int i=109;i<=112;i++) ds.deleteSQL(i);		
//		for(int i=13;i<15;i++) ds.delete(i);//slowwww when on vpn
//		ds.deleteSQL(95L);
		ds.deleteSQL(26L);		
//		if(true)return;
		
//		dcs.createFHM_LC50();
//		dcs.createWS();

//		dcs.createHLC_modeling();
//		dcs.createWS_modeling();
//		dcs.createMP_modeling();
//		dcs.createVP_modeling();
//		dcs.createBP_modeling();
//		dcs.createLogP_modeling();
		
//		fixQsarExpPropIds();
		
		//*********************************************************************************************

//		DataPointServiceImpl dpsi=new DataPointServiceImpl();
//		List<DataPoint>datapoints=dpsi.findByDatasetName("VP v1");
//		List<DataPoint>datapoints=dpsi.findByDatasetName("HLC v1");
//		System.out.println(datapoints.size());
		
//		DataPointDaoImpl d=new DataPointDaoImpl ();
//		List<DataPoint>dps=d.findByDatasetNameSql("HLC v1");
//		System.out.println(Utilities.gson.toJson(dps));
//		System.out.println(dps.size());
		
		//*********************************************************************************************
		
//		Connection conn=SqlUtilities.getConnectionPostgres();
//		String dataSetName="HLC";
//		String sql="select id from qsar_datasets.datasets d where d.\"name\" ='"+dataSetName+"'";
//		String datasetId=DatabaseLookup.runSQL(conn, sql);
//		sql="select count(id) from qsar_datasets.data_points dp where dp.fk_dataset_id="+datasetId;			
//		String countDatapoints=DatabaseLookup.runSQL(conn, sql);
//		System.out.println(countDatapoints);

		//*********************************************************************************************
		
//		dcs.getDatasetStats();//Get record counts for the papers
//		dcs.getDatasetStatsUsingSql();//Get record counts for the papers
//		getDatasetStatsForOneDataset();
//		dcs.getMappedRecordCountsBySourceAndProperty();
		
//		
		
		
//		String folder="data\\dev_qsar\\output\\";
		// CR: 2/14/23 this was causing an error
//		System.out.println("fix this error in datasetcreator before running again");
//		GetExpPropInfo.createCheckingSpreadsheet("pKa_a from exp_prop and chemprop", folder, null);
		
	}
	
	void testStandardize() {

		String workflow="qsar-ready";
		String serverHost="https://hcd.rtpnc.epa.gov";
		SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY,workflow,serverHost);

		
		String smiles="CC(=O)N[C@H]1[C@H](O[C@H]2[C@@H](O)[C@@H](CO)O[C@@H](O[C@H]3[C@H](O)[C@@H](O)C(O)O[C@@H]3CO)[C@@H]2O)O[C@H](CO)[C@@H](O)[C@@H]1O[C@@H]1O[C@H](CO)[C@H](O)[C@H](O)[C@H]1O";
		boolean useFullStandardize=false;
		
				
		HttpResponse<String> standardizeResponse = sciDataExpertsStandardizer.callQsarReadyStandardizePost(smiles, false);

		System.out.println("status=" + standardizeResponse.getStatus());

		if (standardizeResponse.getStatus() == 200) {
			String jsonResponse = SciDataExpertsStandardizer.getResponseBody(standardizeResponse, useFullStandardize);
			String qsarSmiles=SciDataExpertsStandardizer.getQsarReadySmilesFromPostJson(jsonResponse, useFullStandardize);
			System.out.println(smiles+"\t"+qsarSmiles);
		} 

	}

	
	private void getMappedRecordCountsBySourceAndProperty() {
		
		List<String>datasetNames=new ArrayList<>();

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


		
		DatasetServiceImpl datasetService=new DatasetServiceImpl(); 
		
		List<String>globalSourceList=new ArrayList<>();
		
		Hashtable<String,Hashtable<String,Integer>>htAllSets=new Hashtable<>();
		
		
		for (String datasetName:datasetNames) {	
			
			
			Dataset dataset=datasetService.findByName(datasetName);
			
//			System.out.println(datasetName+"\t"+dataset.getName());
			
//			Hashtable<String, Integer>htRaw=getRawRecordCountsBySource(dataset.getProperty().getName());			
			Hashtable<String,Integer>htMapped=getMappedRecordsBySource(datasetName);
			
			for (String key:htMapped.keySet()) {
				if(!globalSourceList.contains(key)) globalSourceList.add(key);
			}
			
			htAllSets.put(datasetName,htMapped);
//			System.out.println("");
		}
		
//		System.out.println("All sources:");
		
		System.out.print("Source\t");
		for (String key:htAllSets.keySet()) {
			System.out.print(key.replace(" v1 modeling", "")+"\t");
		}
		System.out.print("\n");
		
		for (String source:globalSourceList) {
			System.out.print(source+"\t");
			
			for (String key:htAllSets.keySet()) {
				
				if (htAllSets.get(key).get(source)!=null) {
					System.out.print(htAllSets.get(key).get(source)+"\t");	
				} else {
					System.out.print("0\t");
				}
				
				
			}
			System.out.print("\n");
			
		}
		
	}

	void getDatasetStats() {

		System.out.println("propertyName\tRaw*\tDSSTox Mapped\tDataset");

		List<String>datasetNames=new ArrayList<>();
		datasetNames.add("HLC v1");
		datasetNames.add("WS v1");
		datasetNames.add("BP v1");
		datasetNames.add("VP v1");
		datasetNames.add("LogP v1");
		datasetNames.add("MP v1");
		
		
		DatasetServiceImpl ds=new DatasetServiceImpl();
		PropertyValueServiceImpl propertyValueService = new PropertyValueServiceImpl();
		DataPointServiceImpl dpsi=new DataPointServiceImpl();

		
		for (int i=0;i<datasetNames.size();i++) {
			
//			String propertyName=propertyNames.get(i);
			String dataSetName=datasetNames.get(i);
			
			Dataset dataset=ds.findByName(dataSetName);
			
			String propertyName=dataset.getProperty().getName();
			
//			System.out.println(dataset.getName()+"\t"+propertyName);
			
			List<PropertyValue> propertyValues = propertyValueService.findByPropertyNameWithOptions(propertyName, true, true);
			
			System.out.println("Number of raw records ="+propertyValues.size());

			String folder="data\\dev_qsar\\output\\";
			folder+=dataSetName+"\\";		
			String jsonPath=folder+dataSetName+"_Mapped_Records.json";
			jsonPath=jsonPath.replace(" ", "_").replace("=", "_");
			JsonArray mappedRecords=Utilities.getJsonArrayFromJsonFile(jsonPath);
			
			System.out.println("Number of mapped records ="+mappedRecords.size());


//			String sql="select count(id) from qsar_datasets.data_points dp where dp.fk_dataset_id="+90
			
			List<DataPoint>datapoints=dpsi.findByDatasetName(dataSetName);
					System.out.println("Number of datapoints ="+datapoints.size());

			System.out.println(propertyName+"\t"+propertyValues.size()+"\t"+mappedRecords.size()+"\t"+datapoints.size());
		}
	}
	
	
	void getDatasetStatsUsingSql() {

		System.out.println("propertyName\tRaw*\tDSSTox Mapped\tDataset");

		Connection conn=SqlUtilities.getConnectionPostgres();
		
		List<String>datasetNames=new ArrayList<>();

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

		String descriptorSetName="WebTEST-default";
		String sql="select id from qsar_descriptors.descriptor_sets where name='"+descriptorSetName+"';";
		String descriptorSetId=DatabaseLookup.runSQL(conn, sql);
		
		for (int i=0;i<datasetNames.size();i++) {
			
			String dataSetName=datasetNames.get(i);
			
			sql="select id from qsar_datasets.datasets d where d.\"name\" ='"+dataSetName+"'";
			String datasetId=DatabaseLookup.runSQL(conn, sql);

//			System.out.println(datasetId);
			
			
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

			//Following gets datapoint count but ignores whether descriptors are available:
//			sql="select count(id) from qsar_datasets.data_points dp where dp.fk_dataset_id="+datasetId;			
			
			//To get datapoint counts, need to also check if selected descriptors are available for each datapoint
			sql="select count(dp.id) from qsar_datasets.data_points dp\n"+
			"join qsar_datasets.datasets on dp.fk_dataset_id = datasets.id\n"+
			"join qsar_descriptors.descriptor_values dv on dv.canon_qsar_smiles=dp.canon_qsar_smiles\n"+
			"where dp.fk_dataset_id="+datasetId+" and dv.fk_descriptor_set_id="+descriptorSetId+" and dv.values_tsv is not null;";

			String countDatapoints=DatabaseLookup.runSQL(conn, sql);
//			System.out.println("Number of datapoints ="+countDatapoints);
						
			System.out.println(propertyName+"\t"+countRaw+"\t"+countMapped+"\t"+countDatapoints);
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
		
		String dataSetName="WS v1";
		getDiscardedRecordsByReason(dataSetName);
		
		
		
		

	}

	private Hashtable<String,Integer> getMappedRecordsBySource(String dataSetName) {
		
		Hashtable<String,Integer>htCountsMapped=new Hashtable<>();
		String folder="data\\dev_qsar\\output\\";
		folder+=dataSetName+"\\";		
		String jsonPath=folder+dataSetName+"_Mapped_Records.json";
		jsonPath=jsonPath.replace(" ", "_").replace("=", "_");
		JsonArray mappedRecords=Utilities.getJsonArrayFromJsonFile(jsonPath);
		
//		Set<String>rawSources=htRaw.keySet();
		
//		for (String source:rawSources) htCountsMapped.put(source, 0);
		
		System.out.println(dataSetName+"\t"+mappedRecords.size());
		
		for (int i=0;i<mappedRecords.size();i++) {
			JsonObject jo=mappedRecords.get(i).getAsJsonObject();
			
			String sourceName;
			
			if(jo.get("public_source_name")==null) {
				sourceName=jo.get("literature_source_citation").getAsString();	
			} else {
				sourceName=jo.get("public_source_name").getAsString();	
			}
			
//			System.out.println(sourceName);
			
			if(htCountsMapped.get(sourceName)==null) {
				htCountsMapped.put(sourceName,1);
			} else {
				htCountsMapped.put(sourceName,htCountsMapped.get(sourceName)+1);			
			}
		}
		
//		for (String source:rawSources) {
//			System.out.println(source+"\t"+htRaw.get(source)+"\t"+htCountsMapped.get(source));
//		}
		return htCountsMapped;

	}
	
	private void getDiscardedRecordsByReason(String dataSetName,String sourceName) {
		

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
	

	private void getDiscardedRecordsByReason(String dataSetName) {
		

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

	/**
	 * This method assumes a property value has either a public source or a literature source
	 * 
	 * @param propertyName
	 * @return
	 */
	private Hashtable<String, Integer> getRawRecordCountsBySource(String propertyName) {
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
	

	/**
	 * Creates WS dataset for exposing raw experimental data to dashboard
	 */
	public void createWS() {
		
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

		validateStructure=false;
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, null, omitSalts,validateStructure);
		
//		String datasetName = "WS from exp_prop and chemprop v2";
		String datasetName = "WS2";
				
		String datasetDescription = "WS from exp_prop and chemprop where "
				+ DevQsarConstants.MIN_WATER_SOLUBILITY_G_L+ " < WS(g/L) < "+DevQsarConstants.MAX_WATER_SOLUBILITY_G_L+", " 
				+ "20 < T (C) < 30, "
				+ "740 < P (mmHg) < 780, "
				+ "6.5 < pH < 7.5, "
				+ "omit LookChem, validateStructure="+validateStructure;
				
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				bounds);
		
//		creator.createPropertyDataset(listMappedParams, false);

		List<String>excludedSources=new ArrayList<>();
		excludedSources.add("LookChem");
		creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
		
	}
	
	public void createWS_modeling() {
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		
		creator.postToDB=false;
		

		String propertyName = DevQsarConstants.WATER_SOLUBILITY;
		String listName = "ExpProp_WaterSolubility_WithChemProp_120121";
		
		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		BoundParameterValue pressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		BoundParameterValue phBound = new BoundParameterValue("pH", 6.5, 7.5, true);//TODO can the chemical itself cause low pH? how do we know if records like in echemportal are due to added additional component which caused banding issue?
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);
		bounds.add(pressureBound);
		bounds.add(phBound);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, null, omitSalts,validateStructure);
		
//		String datasetName = "WS v1";
//		String datasetName = "WS v1 res_qsar";
		String datasetName = "WS v1 modeling";
		
//		String datasetDescription = "WS from exp_prop and chemprop where 1e-14 < WS(M) < 100, 1e-11 < WS(g/L) < 990, " 
//				+ "20 < T (C) < 30, 740 < P (mmHg) < 780, 6.5 < pH < 7.5, omit LookChem";
				
		String datasetDescription = "WS from exp_prop and chemprop where "+
		DevQsarConstants.MIN_WATER_SOLUBILITY_G_L+" < WS(g/L) < "+DevQsarConstants.MAX_WATER_SOLUBILITY_G_L+", " 
				+ "20 < T (C) < 30, 740 < P (mmHg) < 780, 6.5 < pH < 7.5, omit LookChem, OFMPub, and Angus";
		
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				bounds);
		
		List<String>excludedSources=new ArrayList<>();

		excludedSources.add("LookChem");// chemical company, MAE is 35% higher than OPERA 
		excludedSources.add("ANGUS Chemical Company (Chemical company)");// Chemical company
		excludedSources.add("OFMPub");// some curation issues

		creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);

		
	}

	// methods like these 
	public void createVP() {
		// comment for diff
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");

		String propertyName = DevQsarConstants.VAPOR_PRESSURE;
		String listName = "ExpProp_VP_WithChemProp_070822";

		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, null, omitSalts,validateStructure);

		
		String datasetName = "VP from exp_prop and chemprop";
		String datasetDescription = "VP from exp_prop and chemprop where 1e-14 < VP(mmHg) < 1e6 and 20 < T(C) < 30";
		
		
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				bounds);
		creator.createPropertyDataset(listMappedParams, false);


	}
	
	public void createVP_modeling() {
		// comment for diff
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");

		String propertyName = DevQsarConstants.VAPOR_PRESSURE;
		String listName = "ExpProp_VP_WithChemProp_070822";

		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, null, omitSalts,validateStructure);

		
//		String datasetName = "VP v1";
//		String datasetName = "VP v1 res_qsar";
		String datasetName = "VP v1 modeling";
		
		String datasetDescription = "VP from exp_prop and chemprop where 1e-14 < VP(mmHg) < 1e6 and 20 < T(C) < 30 and "+
		"omit eChemPortalAPI, PubChem, OFMPub, ChemicalBook and Angus sources";
		

		List<String>excludedSources=new ArrayList<>();
		
		excludedSources.add("eChemPortalAPI");// high error rate
		excludedSources.add("PubChem");// high error rate
		excludedSources.add("OFMPub");// low quality data
		excludedSources.add("ChemicalBook");// chemical company
		excludedSources.add("ANGUS Chemical Company (Chemical company)");// chemical company

		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				bounds);
		creator.createPropertyDatasetExcludeSources(listMappedParams, false,excludedSources);


	}
	
	
	public void create_pKA() {
		// comment for diff
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");

		String propertyName = DevQsarConstants.PKA;//pKA, pkAa, pkAb- TODO determine which sources use which
		String listName = "ExpProp_"+propertyName+"_1130822";

		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, null, omitSalts,validateStructure);

		
		String datasetName = listName+"_TMM";
		String datasetDescription = "pKA with 20 < T (C) < 30";
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				bounds);
		creator.createPropertyDataset(listMappedParams, false);


	}
	
	public void createPKAa() {

//		DatasetServiceImpl ds=new DatasetServiceImpl();
//		ds.delete(126);
//		if(true) return;
		
				// comment for diff
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");

		String propertyName = DevQsarConstants.PKA_A;
		// String listName = "ExpProp_HLC_WithChemProp_121421";

		ArrayList<String> listNameArray = new ArrayList<String>();
		listNameArray.add("ExpProp_PKAa_WithChemProp_011423");

		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, listNameArray, omitSalts,validateStructure);

		String datasetName = propertyName+" from exp_prop and chemprop";
		String datasetDescription = propertyName+" from exp_prop and chemprop with no bounds";
		
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				null);
		creator.createPropertyDataset(listMappedParams, false);
		
	}
	
	public void createPKAb() {
		// comment for diff
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");

		String propertyName = DevQsarConstants.PKA_B;
		// String listName = "ExpProp_HLC_WithChemProp_121421";

		ArrayList<String> listNameArray = new ArrayList<String>();
		listNameArray.add("ExpProp_PKAb_WithChemProp_011423");

		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();

		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, listNameArray, omitSalts,validateStructure);

		String datasetName = propertyName+" from exp_prop and chemprop";
		String datasetDescription = propertyName+" from exp_prop and chemprop with no bounds";
		
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				null);
		creator.createPropertyDataset(listMappedParams, false);
		
	}




	public void createLogP() {
		// comment for diff
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
				omitOpsinAmbiguousNames, omitUvcbNames, listNameArray, omitSalts,validateStructure);

//		String datasetName = "ExpProp_LogP_WithChemProp_MULTIPLE";
		String datasetName = "LogP from exp_prop and chemprop";
		String datasetDescription = "LogP from exp_prop and chemprop with -6 < LogKow < 11 and 20 < T(C) < 30";
		
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				bounds);
		creator.createPropertyDataset(listMappedParams, false);
		
	}
	
	public void createLogP_modeling() {
		// comment for diff
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");

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
				omitOpsinAmbiguousNames, omitUvcbNames, listNameArray, omitSalts,validateStructure);

//		String datasetName = "LogP v1";
//		String datasetName = "LogP v1 res_qsar";
		String datasetName = "LogP v1 modeling";

		String datasetDescription = "LogP from exp_prop and chemprop with -6 < LogKow < 11 and 20 < T(C) < 30 and "
				+ "omit eChemPortalAPI, PubChem, OFMPub and Angus sources";
		
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				bounds);

		List<String>excludedSources=new ArrayList<>();

		excludedSources.add("OFMPub");// curation issues
		excludedSources.add("ANGUS Chemical Company (Chemical company)");// chemical company
		excludedSources.add("eChemPortalAPI");// bad data
		excludedSources.add("PubChem");// bad data
		
		creator.createPropertyDatasetExcludeSources(listMappedParams, false,excludedSources);
		
	}

	// methods like these 
//	public void createHLC() {
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


	public void createHLC() {
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

		validateStructure=false;
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, listNameArray, omitSalts,validateStructure);

		
//		String datasetName = "HLC from exp_prop and chemprop";
		String datasetName = "HLC2";
		
		String datasetDescription = "HLC from exp_prop and chemprop where "
				+ "1e-13 < HLC (atm m^3/mol) < 1e2 , 20 < T (C) < 30, 6.5 < pH < 7.5, validateStructure="+validateStructure;

		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				bounds);
		
		creator.createPropertyDataset(listMappedParams, false);
		
//		List<String>sources=new ArrayList<>();
//		sources.add("Kurz & Ballschmiter 1999");
//		creator.createPropertyDatasetWithSpecifiedSources(listMappedParams, false,sources);

	}
	
	
	public void createHLC_modeling() {
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
				omitOpsinAmbiguousNames, omitUvcbNames, listNameArray, omitSalts,validateStructure);

		
//		String datasetName = "HLC v1";
//		String datasetName = "HLC v1 res_qsar";
		String datasetName = "HLC v1 modeling";
		
		String datasetDescription = "HLC from exp_prop and chemprop where "
				+ "1e-13 < HLC (atm m^3/mol) < 1e2 , 20 < T (C) < 30, 6.5 < pH < 7.5 and "
				+ "omit echemPortalAPI and OChem";


		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				bounds);
		
		List<String>excludedSources=new ArrayList<>();
		
		excludedSources.add("eChemPortalAPI");// bad data
		excludedSources.add("OChem");// bad data
		
		creator.createPropertyDatasetExcludeSources(listMappedParams, false,excludedSources);
		
//		creator.mapSourceChemicalsForProperty(listMappedParams);

	}

	/**
	 * create melting point dataset based on CAS registration
	 */
	public void createMPRegisteredCAS() {
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");

		String propertyName = DevQsarConstants.MELTING_POINT;

		BoundParameterValue PressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);

		MappingParams casrnMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_CASRN, null,
				true, false, false, false, false, false, true, null,true,validateStructure);

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
	public void createMP() {
		
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
				omitOpsinAmbiguousNames, omitUvcbNames, listNameArray, omitSalts,validateStructure);

				
		String datasetName = "MP from exp_prop and chemprop v2";
		String datasetDescription = "MP from exp_prop and chemprop where "+DevQsarConstants.MIN_MELTING_POINT_C+
				" < MP < "+DevQsarConstants.MAX_MELTING_POINT_C+" and 740 < P < 780, omit Lookchem";

		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				bounds);
		
		List<String>excludedSources=new ArrayList<>();
		excludedSources.add("LookChem");
		
//		creator.createPropertyDataset(listMappedParams, false);
		creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);

	}
	
	/**
	 * create melting point dataset from expprop records based on list registration
	 * 
	 * TODO there are a lot of no hits in DSSTOX
	 * 
	 */
	public void createMP_modeling() {
		
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");
		
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
				omitOpsinAmbiguousNames, omitUvcbNames, listNameArray, omitSalts,validateStructure);

				
//		String datasetName = "MP v1";
//		String datasetName = "MP v1 res_qsar";
		String datasetName = "MP v1 modeling";
		
		String datasetDescription = "MP from exp_prop and chemprop where "+DevQsarConstants.MIN_MELTING_POINT_C+
				" < MP < "+DevQsarConstants.MAX_MELTING_POINT_C+" and 740 < P < 780, and "
						+ "omit OFMPub, CSDeposition Service, ONSChallenge, and chemical company sources";

		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				bounds);
		
		List<String>excludedSources=new ArrayList<>();
		
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

	/**
	 * creates Boiling point dataset using records from chemreg with CAS registration
	 */
	public void createBPRegisteredCAS() {
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");

		String propertyName = DevQsarConstants.BOILING_POINT;

		BoundParameterValue PressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);

		MappingParams casrnMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_CASRN, null,
				true, false, false, false, false, false, true, null,true,validateStructure);

		String casrnMappingName = "ExpProp_BP_CASRN";
		String casrnMappingDescription = "Exprop BP with 740 < P (mmHg) < 780";
		DatasetParams casrnMappedParams = new DatasetParams(casrnMappingName, 
				casrnMappingDescription, 
				propertyName,
				casrnMappingParams,
				bounds);

		creator.createPropertyDataset(casrnMappedParams, false);
	}



	public void createLogPRegisteredCAS() {
		// comment for diff
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");

		String propertyName = DevQsarConstants.LOG_KOW;

		BoundParameterValue PressureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);



		MappingParams casrnMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_CASRN, null,
				true, false, false, false, false, false, true, null,true,validateStructure);

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
	public void createHLCRegisteredCAS() {
		// comment for diff
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");

		String propertyName = DevQsarConstants.HENRYS_LAW_CONSTANT;

		BoundParameterValue temperatureBound = new BoundParameterValue("Temperature", 20.0, 30.0, true);
		BoundParameterValue phBound = new BoundParameterValue("pH", 6.0, 8.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(temperatureBound);
		bounds.add(phBound);


		MappingParams casrnMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_CASRN, null,
				true, false, false, false, false, false, true, null,true,validateStructure);

		String casrnMappingName = "ExpProp_HLC_CASRN";
		String casrnMappingDescription = "Exprop HLC with 20 < T (C) < 30 and 6 < pH < 8";
		DatasetParams casrnMappedParams = new DatasetParams(casrnMappingName, 
				casrnMappingDescription, 
				propertyName,
				casrnMappingParams,
				bounds);

		creator.createPropertyDataset(casrnMappedParams, false);


	}

	public void createBCF() {
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "cramslan");

		String propertyName = DevQsarConstants.LOG_BCF_FISH_WHOLEBODY;
		String listName = "ExpProp_bcfwbf_072222";


		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, listName, 
				false, true, false, true, true, false, false, null,true,validateStructure);
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
	public void createBP() {
		
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
		
		
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");

		String propertyName = DevQsarConstants.BOILING_POINT;

		BoundParameterValue PressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, listNameArray, omitSalts,validateStructure);


		String datasetName = "BP from exp_prop and chemprop v3";
		
		String datasetDescription = "BP from exp_prop and chemprop where "+DevQsarConstants.MIN_BOILING_POINT_C+
				" < BP < "+DevQsarConstants.MAX_BOILING_POINT_C+" and 740 < P < 780, omit Lookchem";

		
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				bounds);
		
		List<String>excludedSources=new ArrayList<>();
		excludedSources.add("LookChem");
		
		creator.createExcelFiles=true;
		
		creator.createPropertyDatasetExcludeSources(listMappedParams, false, excludedSources);
//		creator.createPropertyDataset(listMappedParams, false);

	}
	
	/**
	 * create boiling point dataset based on chemreg lists
	 */
	public void createBP_modeling() {
		
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
		
		
		DatasetCreator creator = new DatasetCreator(sciDataExpertsStandardizer, "tmarti02");

		String propertyName = DevQsarConstants.BOILING_POINT;

		BoundParameterValue PressureBound = new BoundParameterValue("Pressure", 740.0, 780.0, true);
		List<BoundParameterValue> bounds = new ArrayList<BoundParameterValue>();
		bounds.add(PressureBound);
		
		MappingParams listMappingParams = new MappingParams(DevQsarConstants.MAPPING_BY_LIST, null, isNaive,
				useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
				omitOpsinAmbiguousNames, omitUvcbNames, listNameArray, omitSalts,validateStructure);


//		String datasetName = "BP v1";
//		String datasetName = "BP v1 res_qsar";
		String datasetName = "BP v1 modeling";
		
		String datasetDescription = "BP from exp_prop and chemprop where "+DevQsarConstants.MIN_BOILING_POINT_C+
				" < BP < "+DevQsarConstants.MAX_BOILING_POINT_C+" and 740 < P < 780, and "
						+ "omit eChemPortalAPI, PubChem, Oxford University Chemical Safety Data, OFMPub and chemical company sources";

		
		DatasetParams listMappedParams = new DatasetParams(datasetName, 
				datasetDescription, 
				propertyName,
				listMappingParams,
				bounds);
		
		List<String>excludedSources=new ArrayList<>();
		excludedSources.add("eChemPortalAPI");// bad data
		excludedSources.add("PubChem");// bad data
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

	}
	
	

}
