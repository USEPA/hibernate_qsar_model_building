package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;



import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.PropertyServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxOtherCASRN;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.MethodAD;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;
import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxRecordOtherCASRNServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxRecordServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;


/**
* @author TMARTI02
*/
public class PredictionDashboardTableMaps {

	public  TreeMap<String,DsstoxRecord>mapDsstoxRecordsByCID=new TreeMap<>();
	public  TreeMap<String,DsstoxRecord>mapDsstoxRecordsBySID=new TreeMap<>();
	public  TreeMap<String,DsstoxRecord>mapDsstoxRecordsByCAS=new TreeMap<>();
	
	//No longer need following maps because added records with no dtxcids to the DsstoxRecord table (and json export)
//	public TreeMap<String,DsstoxRecord>mapDsstoxRecordsBySID_NoCompound=new TreeMap<>();
//	public TreeMap<String,DsstoxRecord>mapDsstoxRecordsByCAS_NoCompound=new TreeMap<>();

//	public static TreeMap<String,DsstoxRecord>mapDsstoxRecordsByOtherCAS=new TreeMap<>();//TODO do we need to have this as separate map or just store in the map above?
	
	public static List<DsstoxRecord>dsstoxRecords=null;
//	public List<DsstoxRecord>dsstoxRecordsNoCompound=null;
	
//	public TreeMap<String,String>htPropNameOperaAbbrevToPropNameDB=null;
	
	public TreeMap<String,Property>mapProperties=null;
	public TreeMap<String, Dataset>mapDatasets=null;
	public TreeMap<String, Model>mapModels=null;
	
	public TreeMap<String, MethodAD>mapMethodAD=null;
	public TreeMap<String, Statistic>mapStatistics=null;
	
//	Following files are used to fix the neighbors which are missing dtxsids- but might need to pull info from prod_dsstox instead???
	public static File fileJsonDsstoxRecords2023_04_04=new File("data\\dsstox\\snapshot-2023-04-04\\json\\2023_04_snapshot_dsstox_records_2024_01_09.json");
	public static File fileJsonDsstoxRecords2024_11_12=new File("data\\dsstox\\snapshot-2024-11-12\\json\\2024_11_12_snapshot_dsstox_records.json");
	
	
	public static File fileJsonOtherCAS2023_04_04=new File("data\\dsstox\\snapshot-2023-04-04\\json\\2023_04_snapshot_other_casrn lookup.json");
	public static File fileJsonOtherCAS2024_11_12=new File("data\\dsstox\\snapshot-2024-11-12\\json\\2024_11_12_snapshot_other_casrn lookup.json");

	
	public class OtherCAS {
		String casrn;
		String dsstox_substance_id;
	}
	
	
	
	public PredictionDashboardTableMaps(File fileJsonDsstoxRecords,File fileJsonOtherCAS) {
		
		if (fileJsonDsstoxRecords!=null) {
			getDsstoxRecordsFromJsonExport(fileJsonDsstoxRecords,fileJsonOtherCAS);//loads from fileJsonDsstoxRecords
		} else {
			getDsstoxRecordsFromDatabase();	
		}		

//		DsstoxRecord dr=mapDsstoxRecordsByCAS.get("71-43-2");
//		System.out.println(dr.getDtxsid());

//		DsstoxRecord dr=mapDsstoxRecordsByOtherCAS.get("725266-05-7");
//		System.out.println(dr.getPreferredName());
		
		
		long t1=System.currentTimeMillis();
		System.out.println("Getting maps");
		
		System.out.println("Getting property map");
		mapProperties=CreatorScript.getPropertyMap();
		
		System.out.println("Getting dataset map");
		mapDatasets=CreatorScript.getDatasetsMap();
		
		System.out.println("Getting model map");
		mapModels=CreatorScript.getModelsMap();
		
		System.out.println("Getting methodAD map");
		mapMethodAD=CreatorScript.getMethodAD_Map();
		
		long t2=System.currentTimeMillis();
		
		System.out.println("Time to load other maps:\t"+(t2-t1)/1000+" secs");
		
//		System.out.println("Getting statistic map");
//		mapStatistics=getStatisticsMap();
		
//		System.out.println("Getting model statistics");
//		setModelStatistics();
		
		
		System.out.println("done");
		
	}
	
//	/**
//	 * Get database name of property based on abbreviation from OPERA
//	 * 
//	 * @param propertyNameOPERA
//	 * @return
//	 */
//	public static TreeMap<String,String> createOperaPropertyAbbreviationToDatabasePropertyNameHashtable() {
//
//		TreeMap<String,String>ht=new TreeMap<>();
//		
//		ht.put("BP",DevQsarConstants.BOILING_POINT);
//		ht.put("MP",DevQsarConstants.MELTING_POINT);
//		ht.put("WS",DevQsarConstants.WATER_SOLUBILITY);
//		ht.put("LogP",DevQsarConstants.LOG_KOW);
//		ht.put("LogD55",DevQsarConstants.LogD_pH_5_5);
//		ht.put("LogD74",DevQsarConstants.LogD_pH_7_4);
//		ht.put("LogKOA",DevQsarConstants.LOG_KOA);
//		ht.put("LogHL",DevQsarConstants.HENRYS_LAW_CONSTANT);
//		ht.put("LogVP",DevQsarConstants.VAPOR_PRESSURE);
//		ht.put("FUB",DevQsarConstants.FUB);
//		ht.put("RT",DevQsarConstants.RT);
//		ht.put("Clint",DevQsarConstants.CLINT);
//		ht.put("LogBCF",DevQsarConstants.BCF);
//		ht.put("CACO2",DevQsarConstants.CACO2);
//		ht.put("LogKM",DevQsarConstants.KM);
//		ht.put("LogKoc",DevQsarConstants.KOC);
//		ht.put("pKa_a",DevQsarConstants.PKA_A);
//		ht.put("pKa_b",DevQsarConstants.PKA_B);
//		
//		ht.put("LogOH",DevQsarConstants.OH);
//		ht.put("ReadyBiodeg",DevQsarConstants.RBIODEG);
//		ht.put("BioDeg_LogHalfLife",DevQsarConstants.BIODEG_HL_HC);
//		
//		ht.put("CATMoS_LD50",DevQsarConstants.ORAL_RAT_LD50);
//		ht.put("CATMoS_VT",DevQsarConstants.ORAL_RAT_VERY_TOXIC);
//		ht.put("CATMoS_NT",DevQsarConstants.ORAL_RAT_NON_TOXIC);
//		ht.put("CATMoS_EPA",DevQsarConstants.ORAL_RAT_EPA_CATEGORY);
//		ht.put("CATMoS_GHS",DevQsarConstants.ORAL_RAT_GHS_CATEGORY);
//				
//		ht.put("CERAPP_Ago",DevQsarConstants.ESTROGEN_RECEPTOR_AGONIST);
//		ht.put("CERAPP_Anta",DevQsarConstants.ESTROGEN_RECEPTOR_ANTAGONIST);
//		ht.put("CERAPP_Bind",DevQsarConstants.ESTROGEN_RECEPTOR_BINDING);
//		
//		ht.put("CoMPARA_Ago",DevQsarConstants.ANDROGEN_RECEPTOR_AGONIST);
//		ht.put("CoMPARA_Anta",DevQsarConstants.ANDROGEN_RECEPTOR_ANTAGONIST);
//		ht.put("CoMPARA_Bind",DevQsarConstants.ANDROGEN_RECEPTOR_BINDING);
//		
//		return ht;
//		
//	}

	
	
	public  List<OtherCAS> getOtherCASMapFromJson(File fileJsonOtherCAS) {
		
		if(fileJsonOtherCAS==null)return null;
		
		List<OtherCAS>recsOtherCAS=null;
		Type listType2 = new TypeToken<ArrayList<OtherCAS>>(){}.getType();
		
		try {
			recsOtherCAS = Utilities.gson.fromJson(new FileReader(fileJsonOtherCAS), listType2);
			
			for (OtherCAS oc:recsOtherCAS) {
				
				DsstoxRecord dr=mapDsstoxRecordsBySID.get(oc.dsstox_substance_id);
				
				if(dr==null) {
//					System.out.println("Dont have oc.dsstox_substance_id="+oc.dsstox_substance_id+" in map");
					continue;
				}
				mapDsstoxRecordsByCAS.put(oc.casrn,dr);
				
				DsstoxOtherCASRN d=new DsstoxOtherCASRN();
				d.setCasrn(oc.casrn);
				d.setFk_dsstox_record_id(dr.getId());
				dr.getOtherCasrns().add(d);
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return recsOtherCAS;
	}


	
//	private void getDsstoxRecordsFromGenericSubstancesNoCompound() {
//		
//		dsstoxRecordsNoCompound=new ArrayList<>();
//		
//		File fileJson=new File("data\\dsstox\\json\\snapshot_dsstox_generic_substances_no_compound.json");
//		try {
//			JsonArray ja = Utilities.gson.fromJson(new FileReader(fileJson), JsonArray.class);
//			
////			System.out.println(ja.size());
//			
//			
//			for (JsonElement je:ja) {
//				JsonObject jo=(JsonObject)je;
//				Set<Map.Entry<String, JsonElement>> entries = jo.entrySet();
//
//				DsstoxRecord rec=new DsstoxRecord();
//
//				for(Map.Entry<String, JsonElement> entry: entries) {
//					String fieldName=entry.getKey();
//					JsonElement value=entry.getValue();
//					
//					if (value.isJsonNull()) continue;
//					if(fieldName.equals("dsstox_substance_id")) rec.setDtxsid(value.getAsString());
//					if(fieldName.equals("preferred_name")) 	rec.setPreferredName(value.getAsString());
//					if(fieldName.equals("casrn")) 	rec.setCasrn(value.getAsString());
//					rec.setMolImagePNGAvailable(false);
//				}
//
//				dsstoxRecordsNoCompound.add(rec);
//			}
//			
////			System.out.println(Utilities.gson.toJson(recs));
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//	}


	/**
	 * Gets dsstox records from json file export from dsstox snapshot and creates hashtables for look up for neighbors
	 */
	public void getDsstoxRecordsFromJsonExport(File fileJsonDsstoxRecords,File fileJsonOtherCAS) {
		
		System.out.println("Getting dsstox records from json files");
		
		dsstoxRecords=new ArrayList<>();
		
		try {

			
			
//			JsonArray ja = Utilities.gson.fromJson(new FileReader(fileJsonDsstoxRecords), JsonArray.class);

			//When exporting from dbeaver it puts array inside a json object with the query as the object name:
			JsonObject jo2 = Utilities.gson.fromJson(new FileReader(fileJsonDsstoxRecords), JsonObject.class);
			JsonArray ja =jo2.get("select * from qsar_models.dsstox_records where fk_dsstox_snapshot_id=2").getAsJsonArray();
			
//			System.out.println(ja.size());
			
			
			for (JsonElement je:ja) {
				JsonObject jo=(JsonObject)je;
				Set<Map.Entry<String, JsonElement>> entries = jo.entrySet();

				DsstoxRecord rec=new DsstoxRecord();

				for(Map.Entry<String, JsonElement> entry: entries) {
					String fieldName=entry.getKey();
					JsonElement value=entry.getValue();
					
					if (value.isJsonNull()) continue;
					
					if(fieldName.equals("id")) 	rec.setId(value.getAsLong());
					if(fieldName.equals("cid"))	rec.setCid(value.getAsLong());
					if(fieldName.equals("dtxcid")) 	rec.setDtxcid(value.getAsString());
					if(fieldName.equals("dtxsid")) 	rec.setDtxsid(value.getAsString());
					if(fieldName.equals("preferred_name")) 	rec.setPreferredName(value.getAsString());
					if(fieldName.equals("casrn")) 	rec.setCasrn(value.getAsString());
					if(fieldName.equals("smiles")) 	rec.setSmiles(value.getAsString());
					if(fieldName.equals("mol_weight")) 	rec.setMolWeight(value.getAsDouble());
					if(fieldName.equals("mol_image_png_available")) rec.setMolImagePNGAvailable(value.getAsBoolean());
					
				}

				dsstoxRecords.add(rec);

			}
			
			//Populate hashtables:
			for (DsstoxRecord rec:dsstoxRecords) {
				if (rec.getDtxcid()!=null) mapDsstoxRecordsByCID.put(rec.getDtxcid(),rec);
				mapDsstoxRecordsBySID.put(rec.getDtxsid(),rec);
				mapDsstoxRecordsByCAS.put(rec.getCasrn(),rec);
			}
			
			
			
			getOtherCASMapFromJson(fileJsonOtherCAS); //loads from fileJsonOtherCAS
			
//			System.out.println(mapDsstoxRecordsBySID.size());
			System.out.println("Done");
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Gets dsstox records from dsstox_records table in postgres and creates maps
	 *  to look up neighbors (takes about 1 minute when not in office)
	 * 
	 */
	public void getDsstoxRecordsFromDatabase() {
		int fk_snapshot_id=2;
		
		dsstoxRecords=new ArrayList<>();

		try {
			
			TreeMap<Long, List<DsstoxOtherCASRN>> tmOtherCAS = CreatorScript.getOtherCAS_Map();//ideally hibernate could autopopulate the other casrns stored in dsstoxRecord but this is work around for now

			DsstoxRecordServiceImpl rs=new DsstoxRecordServiceImpl();
			List<DsstoxRecord>recsDB=rs.findAll();

			
			for (DsstoxRecord rec:recsDB) 	{
				
				if(rec.getDsstoxSnapshot().getId()!=fk_snapshot_id) continue;//skip record if not right snapshot
				
				dsstoxRecords.add(rec);

				//Populate hashtables:
				if (rec.getDtxcid()!=null) mapDsstoxRecordsByCID.put(rec.getDtxcid(),rec);
				mapDsstoxRecordsBySID.put(rec.getDtxsid(),rec);
				mapDsstoxRecordsByCAS.put(rec.getCasrn(),rec);
				
				if(tmOtherCAS.get(rec.getId())==null) continue;

				List<DsstoxOtherCASRN>otherCASRNs=tmOtherCAS.get(rec.getId());
				rec.setOtherCasrns(otherCASRNs);//manually set it rather than lazy loading it

				for(DsstoxOtherCASRN otherCASRN:otherCASRNs) {//store other casrns in lookup:
					mapDsstoxRecordsByCAS.put(otherCASRN.getCasrn(),rec);
//					System.out.println(rec.getDtxsid()+"\t"+otherCASRN.getCasrn());
				}
				
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	
	private DsstoxRecord createNaphthalene() {
		DsstoxRecord dr=new DsstoxRecord();
		//Naphthalene
		dr.setDtxcid("DTXCID00913");
		dr.setDtxsid("DTXSID8020913");
		dr.setSmiles("C1=CC2=CC=CC=C2C=C1");
		dr.setMolWeight(128.174);
		dr.setCid(913L);
		dr.setId(113102L);
		
		return dr;
	}
	
	
	
	
	
	
	
	
	
	
	/**
	 * Hibernate doesnt seem to want to automatically populate the model stats, so loading by brute force
	 * 
	 * @return
	 */
	private void setModelStatistics() {
		
		ModelStatisticService ps=new ModelStatisticServiceImpl();
		List<ModelStatistic>modelStatistics=ps.getAll();

		for (ModelStatistic modelStatistic:modelStatistics) {
			
			Model model=modelStatistic.getModel();
			
//			System.out.println(model.getName()+"\t"+modelStatistic.getStatistic().getName());
			

			if (model.getModelStatistics()==null) {
				List<ModelStatistic>listModelStats=new ArrayList<>();
				listModelStats.add(modelStatistic);
				model.setModelStatistics(listModelStats);
			} else {
				List<ModelStatistic>listModelStats=model.getModelStatistics();
				listModelStats.add(modelStatistic);
			}
		}

	}

}