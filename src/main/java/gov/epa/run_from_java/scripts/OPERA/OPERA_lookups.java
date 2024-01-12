package gov.epa.run_from_java.scripts.OPERA;

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
import gov.epa.databases.dev_qsar.qsar_models.service.MethodADServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;


/**
* @author TMARTI02
*/
public class OPERA_lookups {

	public static TreeMap<String,DsstoxRecord>mapDsstoxRecordsByCID=new TreeMap<>();
	public static TreeMap<String,DsstoxRecord>mapDsstoxRecordsBySID=new TreeMap<>();
	public static TreeMap<String,DsstoxRecord>mapDsstoxRecordsByCAS=new TreeMap<>();
	
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
	static File fileJsonDsstoxRecords=new File("data\\dsstox\\json\\2023_04_snapshot_dsstox_records_2024_01_09.json");
	static File fileJsonOtherCAS=new File("data\\dsstox\\json\\2023_04_snapshot_other_casrn lookup.json");

	
	class OtherCAS {
		String casrn;
		String dsstox_substance_id;
	}
	
	OPERA_lookups(boolean useJsonForDsstoxRecords) {
		
		if (useJsonForDsstoxRecords) {
			getDsstoxRecordsFromJsonExport();//loads from fileJsonDsstoxRecords
		} else {
			getDsstoxRecordsFromDatabase();	
		}		

//		DsstoxRecord dr=mapDsstoxRecordsByCAS.get("71-43-2");
//		System.out.println(dr.getDtxsid());

//		DsstoxRecord dr=mapDsstoxRecordsByOtherCAS.get("725266-05-7");
//		System.out.println(dr.getPreferredName());
		
		System.out.println("Getting maps");
		
		System.out.println("Getting property map");
		mapProperties=getPropertyMap(); 

		System.out.println("Getting dataset map");
		mapDatasets=getDatasetsMap();
		
		System.out.println("Getting model map");
		mapModels=getModelsMap();
		
		System.out.println("Getting methodAD map");
		mapMethodAD=getMethodAD_Map();
		
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

	
	
	public static List<OtherCAS> getOtherCASMapFromJson() {
		List<OtherCAS>recsOtherCAS=null;
		Type listType2 = new TypeToken<ArrayList<OtherCAS>>(){}.getType();
		
		try {
			recsOtherCAS = Utilities.gson.fromJson(new FileReader(fileJsonOtherCAS), listType2);
			
			for (OtherCAS oc:recsOtherCAS) {
				
				DsstoxRecord dr=mapDsstoxRecordsBySID.get(oc.dsstox_substance_id);
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
	public static void getDsstoxRecordsFromJsonExport() {
		
		
		dsstoxRecords=new ArrayList<>();
		
		try {
			JsonArray ja = Utilities.gson.fromJson(new FileReader(fileJsonDsstoxRecords), JsonArray.class);
			
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
			
			getOtherCASMapFromJson(); //loads from fileJsonOtherCAS
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Gets dsstox records from dsstox_records table in postgres and creates maps
	 *  to look up neighbors (takes about 1 minute when not in office)
	 * 
	 */
	public static void getDsstoxRecordsFromDatabase() {
		int fk_snapshot_id=1;
		
		dsstoxRecords=new ArrayList<>();

		try {
			
			TreeMap<Long, List<DsstoxOtherCASRN>> tmOtherCAS = getOtherCAS_Map();//ideally hibernate could autopopulate the other casrns stored in dsstoxRecord but this is work around for now

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

	
	/**
	 * Gets map of other casrns
	 * Key = DsstoxRecord id
	 * Value = list of DsstoxOtherCASRNs
	 * 
	 * @return
	 */
	public static TreeMap<Long, List<DsstoxOtherCASRN>> getOtherCAS_Map() {
		DsstoxRecordOtherCASRNServiceImpl ocs=new DsstoxRecordOtherCASRNServiceImpl();
		
//		System.out.println("Getting recs OtherCAS");
//		List<DsstoxOtherCASRN>recsOtherCAS=ocs.findAll();//slow for some reason
		List<DsstoxOtherCASRN>recsOtherCAS=ocs.findAllSql();
		
//		System.out.println("Done");
		
		TreeMap<Long,List<DsstoxOtherCASRN>> tmOtherCAS=new TreeMap<>();
		
		for(DsstoxOtherCASRN recOtherCAS:recsOtherCAS) {
			
			long id=recOtherCAS.getFk_dsstox_record_id();
			
			if(tmOtherCAS.get(id)==null) {
				List<DsstoxOtherCASRN>otherCASRNs=new ArrayList<>();
				tmOtherCAS.put(id,otherCASRNs);
				otherCASRNs.add(recOtherCAS);
			} else {
				List<DsstoxOtherCASRN>otherCASRNs=tmOtherCAS.get(id);
				otherCASRNs.add(recOtherCAS);
			}
		}
		return tmOtherCAS;
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
	
	
	
	public static TreeMap <String,Property> getPropertyMap() {
		
		PropertyServiceImpl ps=new PropertyServiceImpl();
		List<Property>properties=ps.findAll();

		TreeMap <String,Property>mapProperties=new TreeMap<>();
		for (Property property:properties) {
			mapProperties.put(property.getName(), property);
		}
		return mapProperties;
	}
	
	private TreeMap<String,MethodAD> getMethodAD_Map() {

		MethodADServiceImpl servMAD=new MethodADServiceImpl();
		
		List<MethodAD>methodADs=servMAD.findAll();

		TreeMap<String,MethodAD>map=new TreeMap<>();
		
		for (MethodAD methodAD:methodADs) {
			map.put(methodAD.getName(), methodAD);
//			System.out.println(methodAD.getName()+"\t"+methodAD.getDescription());
		}

		return map;
	}

	
	public static TreeMap<String, Dataset> getDatasetsMap() {
		
		DatasetServiceImpl ps=new DatasetServiceImpl();
		List<Dataset>datasets=ps.findAll();

		TreeMap <String,Dataset>mapDatasets=new TreeMap<>();
		for (Dataset dataset:datasets) {
			mapDatasets.put(dataset.getName(), dataset);
		}
		return mapDatasets;
	}
	
	public static TreeMap<String, Model> getModelsMap() {
		
		ModelServiceImpl ps=new ModelServiceImpl();
		List<Model>models=ps.getAll();

		TreeMap <String,Model>mapModels=new TreeMap<>();
		for (Model model:models) {
			mapModels.put(model.getName(), model);
		}
		return mapModels;
	}
	
	private TreeMap<String, Statistic> getStatisticsMap() {
		
		StatisticService ps=new StatisticServiceImpl();
		List<Statistic>statistics=ps.getAll();

		TreeMap <String,Statistic>mapStatistics=new TreeMap<>();
		for (Statistic statistic:statistics) {
			mapStatistics.put(statistic.getName(), statistic);
		}
		return mapStatistics;
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