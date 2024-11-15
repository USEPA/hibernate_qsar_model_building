package gov.epa.run_from_java.scripts.PredictionDashboard.OPERA_Old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;


import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.MethodAD;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedProperty;
import gov.epa.databases.dev_qsar.qsar_models.service.QsarPredictedPropertyServiceImpl;
import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.service.DsstoxCompoundServiceImpl;
import gov.epa.databases.dsstox.service.GenericSubstanceServiceImpl;
import gov.epa.run_from_java.scripts.PredictionDashboard.CreatorScript;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;

/**
 * Takes sqlite db and converts to text files for db loading for SCDCD
 * 
 * @author TMARTI02
 *
 */
public class OPERA_DB_to_csv_files {


	String version="2.8";
	String username="tmarti02";


	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	

	QsarPredictedPropertyServiceImpl servQPP=new QsarPredictedPropertyServiceImpl();
	static GenericSubstanceServiceImpl gssi=new GenericSubstanceServiceImpl();
	static DsstoxCompoundServiceImpl dcsi=new DsstoxCompoundServiceImpl();

	
	
	
	
	/**
	 * 
	 * Hashtable for database units
	 * 
	 * key = modelName, value = units
	 * @return
	 */
	Hashtable<String,String>createDatabaseUnitsHashtable() {
		Hashtable<String,String>ht=new Hashtable<>();
		
		ht.put("OPERA_Clint","ul/min/10^6 cells");
		ht.put("OPERA_liq_chrom_Retention_Time","min");
		ht.put("OPERA_CATMOS_LD50","mg/kg");
		ht.put("OPERA_WS","mol/L");
		ht.put("OPERA_VP","mmHg");
		ht.put("OPERA_BCF","L/kg");
		ht.put("OPERA_KOC","L/kg");
		ht.put("OPERA_BIODEG","days");
		ht.put("OPERA_KM","days");
		ht.put("OPERA_AOH","cm3/molecule*sec");
		ht.put("OPERA_HL","atm-m3/mole");
		ht.put("OPERA_CACO2","cm/s");
		ht.put("OPERA_FUB","fraction [0-1]");
		ht.put("OPERA_BP","°C");
		ht.put("OPERA_MP","°C");
		ht.put("OPERA_CERAPP_Agonist","Binary 0/1");
		ht.put("OPERA_CERAPP_Antagonist","Binary 0/1");
		ht.put("OPERA_CERAPP_Binding","Binary 0/1");
		ht.put("OPERA_CoMPARA_Agonist","Binary 0/1");
		ht.put("OPERA_CoMPARA_Antagonist","Binary 0/1");
		ht.put("OPERA_CoMPARA_Binding","Binary 0/1");
		ht.put("OPERA_RBiodeg","Binary 0/1");
		ht.put("OPERA_pKa_Acidic","Log10 unitless");
		ht.put("OPERA_pKa_Basic","Log10 unitless");
		ht.put("OPERA_LogKOA","Log10 unitless");
		ht.put("OPERA_LogP","Log10 unitless");
		ht.put("OPERA_LogD_ph5.5","Log10 unitless");
		ht.put("OPERA_LogD_ph7.4","Log10 unitless");

		return ht;
		
	}
	
	
	/**
	 * 
	 * Hashtable for opera units
	 * 
	 * key = modelName, value = units
	 * @return
	 */

	Hashtable<String,String>createOperaUnitsHashtable() {
		Hashtable<String,String>ht=new Hashtable<>();
		
		ht.put("OPERA_Clint","ul/min/10^6 cells");
		ht.put("OPERA_liq_chrom_Retention_Time","Minutes ");
		ht.put("OPERA_CATMOS_LD50","mg/kg");
		ht.put("OPERA_WS","Log10 moles/L");
		ht.put("OPERA_VP","Log10 mmHg");
		ht.put("OPERA_BCF","Log10 L/Kg");
		ht.put("OPERA_KOC","Log10 L/Kg");
		ht.put("OPERA_BIODEG","Log10 days");
		ht.put("OPERA_KM","Log10 days");
		ht.put("OPERA_AOH","Log10 cm3/molecule-sec");
		ht.put("OPERA_HL","Log10 atm-m3/mole");
		ht.put("OPERA_CACO2","log(cm/s)");
		ht.put("OPERA_FUB","fraction [0-1]");
		ht.put("OPERA_BP","Degree C");
		ht.put("OPERA_MP","Degree C");
		ht.put("OPERA_CERAPP_Agonist","Binary 0/1");
		ht.put("OPERA_CERAPP_Antagonist","Binary 0/1");
		ht.put("OPERA_CERAPP_Binding","Binary 0/1");
		ht.put("OPERA_CoMPARA_Agonist","Binary 0/1");
		ht.put("OPERA_CoMPARA_Antagonist","Binary 0/1");
		ht.put("OPERA_CoMPARA_Binding","Binary 0/1");
		ht.put("OPERA_RBiodeg","Binary 0/1");
		ht.put("OPERA_pKa_Acidic","Unitless ");
		ht.put("OPERA_pKa_Basic","Unitless ");
		ht.put("OPERA_LogKOA","Log 10 unitless");
		ht.put("OPERA_LogP","Log 10 unitless");
		ht.put("OPERA_LogD_ph5.5","Log10 unitless");
		ht.put("OPERA_LogD_ph7.4","Log10 unitless");


		return ht;
	}
	
	/**
	 * Look up for modelName
	 * 
	 * key=OPERA model name, value=final model name
	 * 
	 * @return
	 */
	Hashtable<String,String>createModelNameHashtable() {

		Hashtable<String,String>ht=new Hashtable<>();
		
		ht.put("Clint","OPERA_Clint");
		ht.put("RT","OPERA_liq_chrom_Retention_Time");
		ht.put("CATMOS_LD50","OPERA_CATMOS_LD50");
		ht.put("WS","OPERA_WS");
		ht.put("LogVP","OPERA_VP");
		ht.put("LogBCF","OPERA_BCF");
		ht.put("LogKoc","OPERA_KOC");
		ht.put("BioDeg_LogHalfLife","OPERA_BIODEG");
		ht.put("LogKM","OPERA_KM");
		ht.put("LogOH","OPERA_AOH");
		ht.put("LogHL","OPERA_HL");
		ht.put("CACO2","OPERA_CACO2");
		ht.put("FUB","OPERA_FUB");
		ht.put("BP","OPERA_BP");
		ht.put("MP","OPERA_MP");
		ht.put("CERAPP_Ago","OPERA_CERAPP_Agonist");
		ht.put("CERAPP_Anta","OPERA_CERAPP_Antagonist");
		ht.put("CERAPP_Bind","OPERA_CERAPP_Binding");
		ht.put("CoMPARA_Ago","OPERA_CoMPARA_Agonist");
		ht.put("CoMPARA_Anta","OPERA_CoMPARA_Antagonist");
		ht.put("CoMPARA_Bind","OPERA_CoMPARA_Binding");
		ht.put("ReadyBiodeg","OPERA_RBiodeg");
		ht.put("pKa_a","OPERA_pKa_Acidic");
		ht.put("pKa_b","OPERA_pKa_Basic");
		ht.put("LogKOA","OPERA_LogKOA");
		ht.put("LogP","OPERA_LogP");
		ht.put("LogD55","OPERA_LogD_ph5.5");
		ht.put("LogD74","OPERA_LogD_ph7.4");

		
		return ht;
		
		
	}

	
	/**
	 * Gets version of each model in OPERA 2.8 
	 * 
	 * From OPERA_models_2.8.xlsx
	 * 
	 * key=original OPERA name, value = opera version for that model
	 * 
	 * 
	 * @return
	 */
	
	HashMap<String,String>createVersionNumberOpera2_8_HashMap() {
		
		HashMap<String,String> hm=new HashMap<>();
		
		hm.put("BP","2.6");
		hm.put("LogHL","2.6");
		hm.put("LogKOA","2.6");
		hm.put("LogP","2.6");
		hm.put("MP","2.6");
		hm.put("LogVP","2.6");
		hm.put("WS","2.6");
		hm.put("RT","2.6");
		hm.put("pKa_a","2.6");
		hm.put("pKa_b","2.6");
		hm.put("LogD74","2.6");
		hm.put("LogD55","2.6");
		hm.put("LogBCF","2.6");
		hm.put("LogOH","2.6");
		hm.put("BioDeg_LogHalfLife","2.6");
		hm.put("ReadyBiodeg","2.6");
		hm.put("LogKM","2.6");
		hm.put("LogKoc","2.6");
		
		hm.put("CERAPP_Bind","2.6");
		hm.put("CERAPP_Ago","2.6");
		hm.put("CERAPP_Anta","2.6");
		hm.put("CoMPARA_Bind","2.6");
		hm.put("CoMPARA_Ago","2.6");
		hm.put("CoMPARA_Anta","2.6");
		
//		hm.put("CATMoS-VT","2.6");
//		hm.put("CATMoS-NT","2.6");
//		hm.put("CATMoS-EPA","2.6");
//		hm.put("CATMoS-GHS","2.6");
		hm.put("CATMOS_LD50","2.6");
		
		hm.put("FUB","2.8");
		hm.put("Clint","2.8");
		hm.put("CACO2","2.8");


		return hm;
		
		
	}
	
	
	/**
	 * pulls records from sqlite and stores in postgres
	 * 
	 * @param propName1
	 * @param propName2
	 */
	void loadRecordsToCSV() {
		int limit=-1;
//		int limit=1000;
//		int limit=1;
		int offset=0;

		
		boolean store=false;


		try {
			Vector<String>colNamesAll=Lookup.getColumnNames();
			
			Hashtable<String,List<String>>htColNames=new Hashtable<>();
			assignColumnsByProperty(colNamesAll, htColNames);
			
//			if(true) return;
									
			String sql=Lookup.createSQLAll(offset,limit);
			
			Statement sqliteStatement=SqliteUtilities.getStatement(Lookup.conn);
			ResultSet rs=SqliteUtilities.getRecords(sqliteStatement, sql);
			System.out.println("Done getting records");

			long t1=System.currentTimeMillis();
			
			Set<String>keys=htColNames.keySet();
			
			Hashtable<String,FileWriter>htFW=new Hashtable<>();
			
			Hashtable<String,String>htPropName=createModelNameHashtable();
			Hashtable<String,String>htOperaUnits=createOperaUnitsHashtable();
			Hashtable<String,String>htDatabaseUnits=createDatabaseUnitsHashtable();
			HashMap<String,String>hmVersion=createVersionNumberOpera2_8_HashMap();
			
			for (String key:keys) {
				
//				String versionNumber=hmVersion.get(key);
//				
//				if (versionNumber==null)
//					System.out.println(key+	"\t"+versionNumber);
			
				String modelName=htPropName.get(key);
//				String operaUnits=htOperaUnits.get(modelName);
//				String databaseUnits=htDatabaseUnits.get(modelName);				
//				System.out.println(key+"\t"+modelName+"\t"+operaUnits+"\t"+databaseUnits);
//				if(true) continue;
				
				FileWriter fw=new FileWriter("data/opera/csv/"+modelName+".csv");
				fw.write(RecordOpera.toHeaderString(",",RecordOpera.fieldNames)+"\r\n");
				htFW.put(key, fw);
			}
			
//			if(true) return;
						
			int counter=0;
			
			boolean stop=false;
			
			
//			for (String colNameAll:colNamesAll) {
//				System.out.println(colNameAll);
//			}

			
			while (rs.next()) {						 								
				counter++;				
				
//				if (counter==10) break;
				
				for (String key:keys) {
					
					RecordOpera r=new RecordOpera();
					r.model_name=htPropName.get(key);
					r.model_units=htDatabaseUnits.get(r.model_name);				
					r.model_source="OPERA "+version;
					
					String version=hmVersion.get(key);
					String operaUnits=htOperaUnits.get(r.model_name);
//					if (!key.equals("pKa_a")) continue;
					
					List<String> colNames = htColNames.get(key);
					
					
					boolean convertedExpPred=SqliteUtilities.createRecord(rs,r,colNames,colNamesAll,operaUnits);	
					
					if(counter==1 && convertedExpPred) {
						System.out.println(key+"\t"+operaUnits+"\t"+r.model_name+"\t"+convertedExpPred);
					}
					
					
//					if(r==null) {
//						System.out.println("Error converting db record");
//						stop=true;
//						break;
//					}

					htFW.get(key).write(r.toString(",",RecordOpera.fieldNames)+"\r\n");
					
//					if (r.propName.equals("LogP")) {
//						System.out.println(r.AD_index);
//						System.out.println(gson.toJson(r));
//					}
				}
				
				if (stop)break;
				
				if (counter%1000==0) System.out.println(counter);
			}
			for (String key:keys) {
				FileWriter fw=htFW.get(key);
				fw.flush();
				fw.close();
			}
			
					
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}



	private void assignColumnsByProperty(Vector<String> colNamesAll, Hashtable<String, List<String>> ht) {
		
		
		List<String> colNamesSpecial = new ArrayList<>();
		ht.put("LogD55", colNamesSpecial);

		colNamesSpecial = new ArrayList<>();
		ht.put("LogD74", colNamesSpecial);
		
		colNamesSpecial = new ArrayList<>();
		ht.put("pKa_a", colNamesSpecial);

		colNamesSpecial = new ArrayList<>();
		ht.put("pKa_b", colNamesSpecial);

		for (int i=0;i<colNamesAll.size();i++) {
			
			String colName=colNamesAll.get(i);
			
			if (colName.contains("predRange") || !colName.contains("_")
					|| colName.equals("DSSTOX_COMPOUND_ID")) 
				continue;

			
			if (colName.contains("LogD")) {
				String propName="LogD55";
				List<String> colNames = ht.get(propName);
				if(colName.contains("_pred")) {
					if (colName.contains(propName)) colNames.add(colName);
				} else colNames.add(colName);				

				propName="LogD74";
				colNames = ht.get(propName);
				if(colName.contains("_pred")) {
					if (colName.contains(propName)) colNames.add(colName);
				} else colNames.add(colName);				
				
			}
			
			if (colName.contains("pKa")) {
				String propName="pKa_a";
				List<String> colNames = ht.get(propName);
				
				if(colName.contains("_pred")) {
					if (colName.contains(propName)) colNames.add(colName);
				} else if (!colName.contains("pKa_b")){
					colNames.add(colName);				
				}

				propName="pKa_b";
				colNames = ht.get(propName);
				if(colName.contains("_pred")) {
					if (colName.contains(propName)) colNames.add(colName);
				} else if (!colName.contains("pKa_a")){
					colNames.add(colName);				
				}
				
			}
		}
		
//		List<String> colNames2 = ht.get("LogD55");
//		
//		for (String colName:colNames2) {
//			System.out.println(colName);
//		}
//		
//		List <String>colNames2 = ht.get("LogD74");
//		
//		for (String colName:colNames2) {
//			System.out.println(colName);
//		}		
//		List<String>colNames2 = ht.get("pKa_a");
//		
//		for (String colName:colNames2) {
//			System.out.println(colName);
//		}
		
//		List<String>colNames2 = ht.get("pKa_b");
//		
//		for (String colName:colNames2) {
//			System.out.println(colName);
//		}

//		if(true) return;
		
		for (int i=0;i<colNamesAll.size();i++) {
			String colName=colNamesAll.get(i);
			String propName = "";

			if (colName.contains("predRange") || !colName.contains("_") ||					
					colName.equals("DSSTOX_COMPOUND_ID") || colName.contains("pKa") || colName.contains("LogD")) {
				continue;
			} else if (colName.contains("LogOH") || colName.contains("AOH")) {
				propName = "LogOH";
			} else if (colName.contains("BCF")) {
				propName = "LogBCF";				
			} else if (colName.contains("HL")) {
				propName = "LogHL";				
			} else if (colName.contains("KOA")) {
				propName = "LogKOA";				
			} else if (colName.contains("Koc")) {
				propName = "LogKoc";				
			} else if (colName.contains("KM")) {
				propName = "LogKM";				
			} else if (colName.contains("ReadyBiodeg")) {
				propName = "ReadyBiodeg";				
			} else if (colName.contains("BioDeg")) {
				propName = "BioDeg_LogHalfLife";				
			} else if (colName.contains("BP")) {
				propName = "BP";				
			} else if (colName.contains("RT")) {
				propName = "RT";				
			} else if (colName.contains("WS")) {
				propName = "WS";				
			} else if (colName.contains("VP")) {
				propName = "LogVP";				
			} else if (colName.contains("LogP")) {
				propName = "LogP";				
			} else if (colName.contains("CERAPP_Bind")) {
				propName = "CERAPP_Bind";				
			} else if (colName.contains("CERAPP_Ago")) {
				propName = "CERAPP_Ago";				
			} else if (colName.contains("CERAPP_Anta")) {
				propName = "CERAPP_Anta";				
			} else if (colName.contains("CoMPARA_Bind")) {
				propName = "CoMPARA_Bind";				
			} else if (colName.contains("CoMPARA_Ago")) {
				propName = "CoMPARA_Ago";				
			} else if (colName.contains("CoMPARA_Anta")) {
				propName = "CoMPARA_Anta";				
			} else if (colName.contains("CACO2")) {
				propName = "CACO2";				
			} else if (colName.contains("Clint")) {
				propName = "Clint";				
			} else if (colName.contains("FUB")) {
				propName = "FUB";				
			} else if (colName.contains("MP")) {
				propName = "MP";								
			} else if (colName.contains("LD50") || 
					colName.contains("CATMOS") || colName.contains("CATMoS") || 
					colName.startsWith("CAS_neighbor_") || colName.startsWith("DTXSID_neighbor_")) {
				
				propName = "CATMOS_LD50";	
				
				if (colName.equals("CATMoS_VT_pred") || colName.equals("CATMoS_NT_pred") || 
					colName.equals("CATMoS_EPA_pred") || colName.equals("CATMoS_GHS_pred")) {
					continue;
				}
			} else {
//				System.out.println(i+"\t"+colName);
			}

			if (ht.get(propName) == null) {
				List<String> colNames = new ArrayList<>();
				colNames.add(colName);
				ht.put(propName, colNames);
			} else {
				List<String> colNames = ht.get(propName);
				colNames.add(colName);
			}
		}
		//Add CID to each list
		Set<String> keys=ht.keySet();		
		for (String key:keys) {
			List<String> colNames = ht.get(key);
			colNames.add(0,"DSSTOX_COMPOUND_ID");

//			if(colNames.size()!=26) {
//				System.out.println(key+"\t"+colNames.size());	
//			}
		}
		
//		List<String> colNames = ht.get("CATMOS_LD50");
//		for (int i=0;i<colNames.size();i++) {
//			System.out.println((i+1)+"\t"+colNames.get(i));
//		}
		
		ht.remove("");
		
//		for(String key:ht.keySet()) {
//			System.out.println("\n"+key);
//			
//			List<String> colNames = ht.get(key);
//			for (String colName:colNames) {
//				System.out.println("\t"+colName);
//			}
//		}


	}


//	private void getExpMatchCount(String propName1, File folder, List<QsarPredictedNeighbor> qsarPredictedNeighbors)
//			throws FileNotFoundException, IOException {
//		TreeMap<String, Double> htDatapoints2=getDatapointsLookupFromText(propName1, folder);
//
////		System.out.println(htDatapoints2.size());
////		System.out.println("Neighbors.size()="+qsarPredictedNeighbors.size());
//		int count=0;
//		int countMatch=0;
//		
//		for (QsarPredictedNeighbor qpn:qsarPredictedNeighbors) {
//			
////			if (!qpn.exp.equals("NA")) {
////				System.out.println(qpn.casrn+"\t"+qpn.dtxcid+"\t"+qpn.exp);
////				//TODO there are experimental values in the neighbors that doesnt show up in the exp value for the qsar predictions
////			}
//			
//			if (qpn.dtxcid==null || qpn.exp.equals("NA")) continue;
//			if (htDatapoints2.get(qpn.dtxcid)==null) continue;
//															
//			double expLookup=htDatapoints2.get(qpn.dtxcid);
//			
////				System.out.println(qpn.dtxcid+"\t"+qpn.exp);
//			
//			double expNeighbor=1;				
//			if (qpn.exp.equals("Inactive")) expNeighbor=0;
//			count++;
//
//			if (expNeighbor!=expLookup) {
//				
////					System.out.println(qpn.dtxcid+"\t"+expLookup+"\t"+expNeighbor);
//
//				System.out.println(qpn.qsarPredictedProperty.getDtxcid()+"\t"+qpn.dtxcid+"\t"+qpn.casrn+
//						"\t"+qpn.dtxsid+"\t"+"\t"+expNeighbor+"\t"+expLookup);
//			} else {
//				countMatch++;
//			}
//		}
//		
//		
//		System.out.println("Exp:"+countMatch+" of "+count);
//	}
//
//	private void getPredictionMatchCount(String propName1, File folder,
//			List<QsarPredictedNeighbor> qsarPredictedNeighbors) throws FileNotFoundException, IOException {
//		
//		TreeMap<String, Double> htPredictions2 = getPredictionLookupFromText(propName1, folder);
//		
//		int count=0;
//		int countMatch=0;
//		
//		for (QsarPredictedNeighbor qpn:qsarPredictedNeighbors) {
//			if (qpn.dtxcid==null) continue;
//			if (htPredictions2.get(qpn.dtxcid)==null) continue;
//								
//			double predLookup=htPredictions2.get(qpn.dtxcid);
//			double predNeighbor=1;				
//			if (qpn.pred.equals("Inactive")) predNeighbor=0;
//			count++;
//			
//			if (predNeighbor!=predLookup) {				
////					System.out.println(qpn.qsarPredictedProperty.getDtxcid()+"\t"+qpn.dtxcid+"\t"+qpn.casrn+
////							"\t"+qpn.dtxsid+"\t"+"\t"+predNeighbor+"\t"+predLookup);
//			} else {
//				countMatch++;
//			}
//		}
//
//		System.out.println("Pred:"+countMatch+" of "+count);
//	}

	private TreeMap<String, Double> getDatapointsLookupFromText(String propName1, File folder) throws FileNotFoundException, IOException {
		TreeMap<String, Double> htDatapoints2=new TreeMap<>();
		BufferedReader br=new BufferedReader (new FileReader(folder.getAbsolutePath()+File.separator+propName1+" datapoints.txt"));
		br.readLine();
		
		while(true) {
			String line=br.readLine();
			if (line==null) break;
			String []vals=line.split("\t");
			String cid=vals[0];
			String exp=vals[1];
			
			
			htDatapoints2.put(cid,Double.parseDouble(exp));
		}
		br.close();
		return htDatapoints2;
	}

	private TreeMap<String, Double> getPredictionLookupFromText(String propName1, File folder)
			throws FileNotFoundException, IOException {
		TreeMap<String, Double> htPredictions2=new TreeMap<>();
		BufferedReader br=new BufferedReader (new FileReader(folder.getAbsolutePath()+File.separator+propName1+" predictions.txt"));
		br.readLine();
		
		while(true) {
			String line=br.readLine();
			if (line==null) break;
			String []vals=line.split("\t");
			String cid=vals[0];
			String pred=vals[1];
			htPredictions2.put(cid,Double.parseDouble(pred));
		}
		
		br.close();
		
		return htPredictions2;
	}

	void saveToJsonFile(Object object,String filepath) {
		try {
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	

	void getNeighbors(String propName1,String propName2) {
//		int limit=1000;
//		int limit=10000000;
		int limit=-1;
		int offset=0;

//		Vector<RecordOpera> records=new Vector<>();
		Vector<String>colNamesAll=Lookup.getColumnNames();

		try {
			String sql=Lookup.createSQL2(propName1,propName2,offset,limit,colNamesAll);
			//				System.out.println(sql);
			Statement sqliteStatement=SqliteUtilities.getStatement(Lookup.conn);
			ResultSet rs=SqliteUtilities.getRecords(sqliteStatement, sql);
			System.out.println("Done getting records");

			TreeMap<String,Neighbor>htNeighbors=new TreeMap<>();
			int counter=0;
			
			while (rs.next()) {						 

				counter++;
				
				RecordOpera r=new RecordOpera();							
				SqliteUtilities.createRecord(rs,r,propName1,propName2);
//				records.add(r);
				
				
				List<Neighbor> neighbors = Neighbor.getNeighbors(r);
//				Neighbor.splitNeighbors(neighbors);//If have | in SID, then make into separate neighbors			
				
				for (Neighbor neighbor:neighbors) {
					neighbor.num=null;
					
					String key=neighbor.getGlobalKey();					

					if (htNeighbors.get(key)==null) {
						htNeighbors.put(key, neighbor);
					}
					
				}
				
				if (counter%10000==0) {
					System.out.println(counter+"\t"+htNeighbors.size());
				}				
			}
			
			Set<String>keys=htNeighbors.keySet();
			Vector<Neighbor> allNeighbors=new Vector<>();
			
			for (String key:keys) {
				allNeighbors.add(htNeighbors.get(key));
			}
						
			
			File folder=new File("data/opera/"+propName1);
			if (!folder.exists()) {
				folder.mkdirs();
			}

			FileWriter fw=new FileWriter(folder.getAbsolutePath()+File.separator+propName1+" neighbors.json");
			fw.write(gson.toJson(allNeighbors));
			fw.flush();
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	private DsstoxRecord getCIDfromCAS(TreeMap<String, DsstoxRecord> htCAS,Neighbor n) {
		
		if (n.CAS!=null && !n.CAS.isBlank()) {							
			if (htCAS.get(n.CAS)!=null) {
				return htCAS.get(n.CAS);
			} else {
//				System.out.println(n.CAS+" was not in hashtable looking it up:");
				List<DsstoxRecord>recs=gssi.findAsDsstoxRecordsByCasrn(n.CAS);							

				if (recs.size()>0)
					return recs.get(0);
			}
		}	
		
		return null;
	}


	private DsstoxRecord getCIDfromSID(TreeMap<String, DsstoxRecord> htSID, Neighbor n) {
		
		if (n.SID!=null && !n.SID.isBlank()) {							

			if (htSID.get(n.SID)!=null) {
				return htSID.get(n.SID);
			} else {
//				System.out.println(n.SID+" was not in hashtable looking it up:");
				List<DsstoxRecord>recs=gssi.findAsDsstoxRecordsByDtxsid(n.SID);							
								
				if (n.CAS.isBlank()) return recs.get(0);
				
				for (int i=0;i<recs.size();i++) {
					DsstoxRecord dr=recs.get(i);					
					if (dr.casrn.equals(n.CAS)) return dr;
				}
			}
		}
		return null;
		
	}

	void createLookups(String propName1) {

		try {

			BufferedReader br = new BufferedReader(new FileReader("data/opera/" + propName1 + " neighbors.json"));
			Type listType = new TypeToken<List<Neighbor>>() {}.getType();
			
			List<Neighbor> neighbors = new Gson().fromJson(br, listType);
			br.close();

			Vector<String>casList=new Vector<>();
			Vector<String>sidList=new Vector<>();
			Vector<String>inChiKeyList=new Vector<>();
			
			for (Neighbor neighbor : neighbors) {
				
				String [] CASRNs=neighbor.CAS.split("\\|");
				
				for (String CASRN:CASRNs) {
					if (CASRN.isEmpty()) continue;
					if (!casList.contains(CASRN)) casList.add(CASRN);
				}
				
				String [] SIDs=neighbor.SID.split("\\|");
				
				for (String SID:SIDs) {
					if (SID.isEmpty()) continue;
					if (!sidList.contains(SID)) sidList.add(SID);
				}
				
				inChiKeyList.add(neighbor.InChiKey);
			}
			
			File folder=new File("data/opera/"+propName1);
			if (!folder.exists()) {
				folder.mkdirs();
			}

			createLookupJson(casList,"CAS",folder.getAbsolutePath()+File.separator+propName1+" DSSTOX lookup by CAS.json");
			createLookupJson(sidList,"SID",folder.getAbsolutePath()+File.separator+propName1+" DSSTOX lookup by SID.json");
//			createLookupJson(inChiKeyList,"InChiKey",folder.getAbsolutePath()+File.separator+propName1+" DSSTOX lookup by InChiKey.json");

			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public class DsstoxCompound2 {
		public String dsstoxCompoundId;
		public String jchemInchikey;
		public String indigoInchikey;
	}
	
	public List<Object> createLookupJson(List<String> idList,String field,String outputPath) throws IOException {
		
		List<String> idList2=new ArrayList<>();
		idList2.addAll(idList);//make a copy
				
		int batch=500;
		
		List<Object>recs=new ArrayList<>();
		
//		GsonBuilder builder = new GsonBuilder();
//		Gson gson = builder.create();
//		2015-07-13 12:00:48.0
		
//		Gson gson=Lookup.getGson();
		
		while (idList2.size()>0) {
			List<String>IDs2=new ArrayList<String>();

			for (int i=1;i<=batch;i++) {
				IDs2.add(idList2.remove(0));
				if (idList2.isEmpty()) break;
			}

			List<DsstoxRecord>recsDR=null;
			List<DsstoxCompound>recsDC=null;
			List<DsstoxCompound2>recsDC2=new ArrayList<>();
			
			if (field.equals("CAS")) {
				recsDR=gssi.findAsDsstoxRecordsByCasrnIn(IDs2);				
				recs.addAll(recsDR);
				
				recsDR=gssi.findAsDsstoxRecordsByOtherCasrnIn(IDs2);//stored otherCAS in DSSTox record so could use to create hashtable later				
//				System.out.println(recsDR.size());
//				for (DsstoxRecord dr:recsDR) {
//					System.out.println(gson.toJson(dr));
//				}
				recs.addAll(recsDR);
			} else if (field.equals("SID")) {
				recsDR=gssi.findAsDsstoxRecordsByDtxsidIn(IDs2);
				recs.addAll(recsDR);
			} else if (field.equals("InChiKey")) {
				recsDC=dcsi.findByInchikeyIn(IDs2);
				
				//Gson refuses to serialize the DSSTox Compounds- so making a small copy:
				for (DsstoxCompound compound:recsDC) {
					DsstoxCompound2 compound2=new DsstoxCompound2();
					compound2.dsstoxCompoundId=compound.getDsstoxCompoundId();
					compound2.indigoInchikey=compound.getIndigoInchikey();
					compound2.jchemInchikey=compound.getJchemInchikey();
					recsDC2.add(compound2);
				}
				
				recs.addAll(recsDC2);
			} else {
				System.out.println("Field "+field+" not implemented");
				return null;
			}			
			System.out.println(recs.size());
			if (idList2.isEmpty()) break;
			
//			if(true) break;
			
		}			
		
		FileWriter fw=new FileWriter(outputPath);
		fw.write(gson.toJson(recs));
		fw.flush();
		fw.close();
		
		return recs;
	}
	
	
	void goThroughNeighbors(String propName1) {
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader("data/opera/"+propName1+File.separator+propName1+" neighbors.json"));
			Type listType = new TypeToken<List<Neighbor>>() {}.getType();
			List<Neighbor> neighbors= new Gson().fromJson(br, listType);
			br.close();

			for(int i=0;i<100;i++) {
				
				Neighbor n=neighbors.get(i);
				
				
				System.out.println(n.CAS+n.SID+"\t"+n.exp+"\t"+n.pred);
				
				
			}
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	

	
	

	private void splitBySID(boolean split, TreeMap<String, DsstoxRecord> htSID, Neighbor neighbor) {
		if (!split && neighbor.SID.contains("|")) {
			
			String [] SIDs=neighbor.SID.split("\\|");
			
			for (String SID:SIDs) {						

				if (htSID.get(SID)!=null) {
					DsstoxRecord dr=htSID.get(SID);
					
					if (dr.dsstoxCompoundId!=null) {
						System.out.println(neighbor.SID+"\t"+dr.dsstoxCompoundId+"\t"+dr.qsarReadySmiles);
					} else {
						System.out.println(neighbor.SID+"\tNo smiles");
					}
				}						
			}
		}
	}

	void goThroughSDF() {
		
		String filePath="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\ghs-data-gathering\\data\\experimental\\OPERA\\OPERA_SDFS\\CERAPP_QR.sdf";
		
		try {
			IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filePath),DefaultChemObjectBuilder.getInstance());

			List<String>listCAS=new ArrayList<>();
			
			while (mr.hasNext()) {
				
				AtomContainer m=null;
				try {
					m = (AtomContainer)mr.next();
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
//				if (m==null || m.getAtomCount()==0) break;
				if (m==null) break;
				
				String CASRN=m.getProperty("CASRN");
				
				if (!CASRN.isEmpty()) {
					
					String [] CASRNs=CASRN.split("\\|");
					
					for (String CAS:CASRNs) {
						if (!listCAS.contains(CAS)) {
							listCAS.add(CAS);
						}
					}
				}
//				if(listCAS.size()>1000) break;
				
			}
			
			System.out.println(listCAS.contains("10108-80-2"));

			List<Object>recs=createLookupJson(listCAS,"CAS","data/opera/DSSTOX lookup by CAS from SDF.json");

			
			FileWriter fw=new FileWriter("data/opera/CAS numbers in CERAPP_QR.sdf not in DSSTOX.txt");
			
			for (String CAS:listCAS) {

				boolean haveCAS=false;
				
				for (Object obj:recs) {
					DsstoxRecord rec=(DsstoxRecord)obj;				
				
					if (rec.getCasrn().equals(CAS)) {
						haveCAS=true;
						break;
					}				
				}
				
				if(!haveCAS) {
					System.out.println(CAS);
					fw.write(CAS+"\r\n");
				}
			}
			
			fw.flush();
			fw.close();
			

		} catch (Exception ex) {
			ex.printStackTrace();
			
		}		
		
	}
	
	
	
	
	
	/**
	 * Gets list of neighbor keys (CAS\tInchiKey) which dont have an SID from all property csv files
	 * 
	 * @param keys
	 * @param filePath
	 */
	void getListNeighborCAS_noSID() {
		
		List<String>keys=new ArrayList<>();
		
		File folder=new File("data/opera/csv");
		File [] files=folder.listFiles();
		
		for (File file:files) {
			if (!file.getName().contains(".csv")) continue;
			getNeighborKeys(keys, file.getAbsolutePath());	
		}
				
		try {
			FileWriter fw = new FileWriter ("data/opera/csv/keys.txt");
			
			Collections.sort(keys);
			
			for (String key:keys) {
				fw.write(key+"\r\n"); 
			}
			
			fw.flush();
			fw.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * Stores a list of all the CAS numbers for the neighbors with no SID numbers
	 * 
	 */
	void addSIDsToCSVs() {
		String jsonFilePath="data/opera/csv/DSSTOX lookup for CAS.json";
		TreeMap<String, DsstoxRecord> htCAS=new TreeMap<>();
		Lookup.loadLookupJsonFileCAS(htCAS, jsonFilePath);
		
		File folder=new File("data/opera/csv");
		File [] files=folder.listFiles();
		
		for (File file:files) {
			if (!file.getName().contains(".csv")) continue;
			
			System.out.println("\n"+file.getName());
			
//			if(!file.getName().contains("Copy")) continue;
 			addSIDs(file.getAbsolutePath(),htCAS);
		}
		
//		addSIDs("data/opera/csv/opera 2.8 CERAPP_Bind.csv",htCAS);
	}


	private void addSIDs(String srcRecordPath,TreeMap<String, DsstoxRecord> htCAS) {
		
		try {
			
			File fileSrc=new File(srcRecordPath);
			
			if (!fileSrc.exists()) {
				System.out.println(srcRecordPath+" doesnt exist");
				return;
			}
						
			String destRecordPath=fileSrc.getParentFile().getParentFile().getAbsolutePath()+File.separator+"csv2"+File.separator+fileSrc.getName();
						
			if(destRecordPath.equals(srcRecordPath)) {
				System.out.println("cant overwrite filepath="+srcRecordPath);
				return;
			}
			
			FileWriter fw=new FileWriter(destRecordPath);
			fw.write(RecordOpera.toHeaderString(",",RecordOpera.fieldNames2)+"\r\n");
			
			List<String> lines = Files.readAllLines(Paths.get(srcRecordPath));
						
			String headerLine=lines.remove(0);
			
			List<String> headers =Parse3(headerLine,",");
			
			int counter=0;
			
			for (String line:lines) {
				
				List<String> vals = Parse3(line,",");
				
				while (vals.size()<headers.size()) {
					vals.add("");
				}
				
				RecordOpera r=new RecordOpera();
				
				for (int i=0;i<headers.size();i++) {
					Field myField = r.getClass().getDeclaredField(headers.get(i));
					myField.set(r, vals.get(i));
				}
				
				for (int i=1;i<=5;i++) {
					
					Field myFieldSID = r.getClass().getDeclaredField("DTXSID_neighbor_"+i);
					String SID=(String)myFieldSID.get(r);
					
					if (SID.contains("SID")) continue;//skip it if we already have an SID 

					Field myFieldCAS = r.getClass().getDeclaredField("CAS_neighbor_"+i);
					String CAS=(String)myFieldCAS.get(r);
					
					String SIDnew=DsstoxMapping.mapByCAS(htCAS, CAS);
					
					if (SIDnew.isEmpty()) continue; //skip it if we didnt get a new SID to put there
					
//					System.out.println(i+"\t"+CAS+"\t"+SIDnew);
					
					myFieldSID.set(r, SIDnew);
					
//					System.out.println(gson.toJson(r));
					
				}
				
//				fw.write(r.toString(",")+"\r\n");
				
				
				fw.write(r.toString(",",RecordOpera.fieldNames2)+"\r\n");
				
				
//				System.out.println(gson.toJson(r));
				
				counter++;				
				if(counter%100000==0) System.out.println(counter);
			}
			
			fw.flush();
			fw.close();
//			System.out.println(lines.size());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}



	/** parses a delimited string into a list- accounts for the fact that can have quotation marks in comma delimited lines
	   * 
	   * @param Line - line to be parsed
	   * @param Delimiter - character used to separate line into fields
	   * @return
	   */
	  public static LinkedList<String> Parse3(String Line, String Delimiter) {

		    LinkedList<String> myList = new LinkedList<String>();

		    int tabpos = 1;

		    while (tabpos > -1) {

		    	tabpos = Line.indexOf(Delimiter);
		    	
		    	if (Line.length()<1) break;
		    	
		    	if (Line.substring(0,1).equals("\"")) {
		    		Line=Line.substring(1,Line.length()); // kill first " mark
		    		
		    		if (Line.length()==0) break;
		    		
		    		if (Line.indexOf("\"")>-1) {
		    			myList.add(Line.substring(0, Line.indexOf("\"")));

			    		if (Line.indexOf("\"")<Line.length()-1)
			    			Line = Line.substring(Line.indexOf("\"") + 2, Line.length());
			    		else 
			    			break;
		    		}
		    		
		    		
		    	} else {
					

					if (tabpos > 0) {
						myList.add(Line.substring(0, tabpos));
						Line = Line.substring(tabpos + 1, Line.length());
					} else if (tabpos == 0) {
						myList.add("");
						Line = Line.substring(tabpos + 1, Line.length());
					} else {
						myList.add(Line.trim());
					}

		    	}
		    			
			}// end while loop

		    
//		    for (int j = 0; j <= myList.size() - 1; j++) {
//				System.out.println(j + "\t" + myList.get(j));					
//			}
		    
		    return myList;

		  }
	
	/**
	 * Gets list of neighbor keys (CAS\tInchiKey) which dont have an SID for a given property csv file
	 * @param keys
	 * @param filePath
	 */
	private void getNeighborKeys(List<String> keys, String filePath) {
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filePath));
			
			String headerLine=br.readLine();
			List<String> headers =Parse3(headerLine,",");
			
			int counter=0;
												
			while (true) {
				String Line=br.readLine();				
				if (Line==null) break;				
				counter++;				
				List<String> vals = Parse3(Line,",");
				
				boolean flag=false;
				
				while (vals.size()<headers.size()) {
//					System.out.println(Line);
					flag=true;
					vals.add("");
				}
								
				
				RecordOpera r=new RecordOpera();
				
				for (int i=0;i<headers.size();i++) {
					Field myField = r.getClass().getDeclaredField(headers.get(i));
					myField.set(r, vals.get(i));
				}
				
//				if(flag) {
//					System.out.println(gson.toJson(r));
//				}
				
								
				List<Neighbor> neighbors=Neighbor.getNeighbors(r);
				
				for (Neighbor neighbor:neighbors) {

//					System.out.println(neighbor.SID);
					
					if (!neighbor.SID.contains("SID")) {
						
						String [] CASRNs=neighbor.CAS.split("\\|");
						
						List<String>casList=new ArrayList(Arrays.asList(CASRNs));
						
						for (int i=0;i<casList.size();i++) {
							String cas=casList.get(i);
							if(cas.contains("CHEMBL") || cas.contains("SRC") || cas.contains("?")) casList.remove(i--);
						}
						
						String [] InChiKeys=neighbor.InChiKey.split("\\|");
						
//						System.out.println(CASRNs.length+"\t"+InChiKeys.length);
						
						if(casList.size()==0) continue;
						
//						if (casList.size()!=InChiKeys.length) {
//							System.out.println("mismatch:"+neighbor.CAS+"\t"+neighbor.InChiKey);
//						}
						
						
						for (int i=0;i<casList.size();i++) {							
							String CASRN=casList.get(i);
							
							if (CASRN.contains("CHEMBL")) continue;
							
							String InChiKey="";
							 
							if (i>InChiKeys.length-1) {
								InChiKey=InChiKeys[0];
								
								if (InChiKeys.length>1)
									System.out.println("mismatch using key0:"+neighbor.CAS+"\t"+neighbor.InChiKey);
								
							} else {
								InChiKey=InChiKeys[i];	
							}
								
								
							
							if (CASRN.isEmpty()) continue;
							if (CASRN.contains("NO")) continue;
							if (CASRN.contains("SRC")) continue;
							
							String key=CASRN+"\t"+InChiKey;
							
//							System.out.println(key);
							
							if (!keys.contains(key)) {
								keys.add(key);		
								System.out.println("key="+key);
//								System.out.println(neighbor.CAS+"\t"+CASRN+"\t"+neighbor.SID);
							}
						}
					}
				}
								
//				System.out.println(gson.toJson(r));						
//				if (counter==1000) break;
				if (counter%100000==0) System.out.println(filePath+"\t"+counter);
			}
			
//			for (String key:keys) {
//				System.out.println(key);
//			}
			System.out.println(filePath+"\t"+keys.size());
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Creates Json look up file for dsstox records and a text file of no hits
	 */
	void createDSSTOX_Dictionary() {
		
		try {
			
			List<String> Lines = Files.readAllLines(Paths.get("data/opera/csv/keys.txt"));
			
			List<String> CASRNs=new ArrayList<>();
			
			for (String Line:Lines) {
				String [] vals=Line.split("\t");
				String CASRN=vals[0];
				CASRNs.add(CASRN);
			}
			
			List<String> CASRNs_noHitTony = Files.readAllLines(Paths.get("data/opera/csv/CASRN no hits (Tony).txt"));
			
			
			String jsonFilePath="data/opera/csv/DSSTOX lookup for CAS.json";
			List<Object>recs=createLookupJson(CASRNs, "CAS", jsonFilePath);//comment out if dont need to search dsstox again
			
			TreeMap<String, DsstoxRecord> htCAS=new TreeMap<>();
			Lookup.loadLookupJsonFileCAS(htCAS, jsonFilePath);

			int countMissing=0;
			FileWriter fw=new FileWriter("data/opera/csv/CASRN no hits and no SID and not in Tony list.txt");
			
			for (String Line:Lines) {
			
				String [] vals=Line.split("\t");
				
				String CASRN=vals[0];
				
				String InchiKey="";
				
				if(vals.length>1) {
					InchiKey=vals[1];	
				}
				
				if(htCAS.get(CASRN)==null && !CASRNs_noHitTony.contains(CASRN) && isCAS(CASRN)) {
					countMissing++;
					System.out.println(countMissing+"\t"+CASRN+"\tNot in DSSTOX");
					fw.write(CASRN+"\t"+InchiKey+"\r\n");
				} else {
					if (!isCAS(CASRN)) {
						System.out.println(CASRN+"\tBad CAS");
					}
				}
			}			
			
			fw.flush();
			fw.close();
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	static boolean isAllNumbers(String text) {		
		String text2=text.replace("-", "").replace("\n", "");
		if(text2.matches("[0-9]+")) return true;
		else return false;
	}
	
	public static boolean isCAS(String text) {
		if(!isAllNumbers(text)) return false;
		
		String text2=text.replace("-", "").replace("\n", "");
		if (text2.length()<5) return false;
		if (!isCASValid(text2)) return false;
		return true;
	}
	
	public static boolean isCASValid(String cas)  {

		try {
        cas = cas.replaceAll("-","");
        // Although the definition is usually expressed as a 
        // right-to-left fn, this works left-to-right.
        // Note the loop stops one character shy of the end.
        int sum = 0;
        for (int indx=0; indx < cas.length()-1; indx++) {
            sum += (cas.length()-indx-1)*Integer.parseInt(cas.substring(indx,indx+1));
        }
        // Check digit is the last char, compare to sum mod 10.
        return Integer.parseInt(cas.substring(cas.length()-1)) == (sum % 10);
		} catch (Exception ex) {
			return false;
		}
	}
	
	
	public static void main(String[] args) {
		OPERA_DB_to_csv_files opera_to_csv=new OPERA_DB_to_csv_files();

//		lo.goThroughNeighbors("CERAPP_Bind");
		
//		lo.loadRecords("CERAPP_Bind","");

		//******************************************************************************
		//For SCDCD flat table effort:
		opera_to_csv.loadRecordsToCSV();		
		opera_to_csv.getListNeighborCAS_noSID();
		opera_to_csv.createDSSTOX_Dictionary();
		opera_to_csv.addSIDsToCSVs();
		
		
		//******************************************************************************
		
//		lo.getOperaRecords("CERAPP_Bind");
//		lo.getNeighbors("CERAPP_Bind","");
//		lo.getNeighbors("CERAPP_Ago","");
//		lo.getNeighbors("CERAPP_Anta","");
//		lo.goThroughSDF();		
		
//		lo.createLookups("CERAPP_Bind");
//		lo.createLookups("CERAPP_Ago");
//		lo.createLookups("CERAPP_Anta");
		//*********************************************************************************
//		lo.determineNoHitCASNumbersCerapp();
		
		//*********************************************************************************

		
		//		DatasetServiceImpl d=new DatasetServiceImpl();
		//		d.delete(102L);

		//		loadRecords("LogOH","AOH");
		//		loadRecords("LogBCF","BCF");
		//		loadRecords("BioDeg_LogHalfLife","BioDeg");
		//		lo.loadRecords("BP","");
		//		lo.lookSimple("BP", "");
		//		lo.lookSimple("CERAPP_Bind", "");

		//TODO handle CATMOS		
		//		loadRecords("CERAPP_Bind","");
		//		lo.look("CERAPP_Bind","");
		//		lo.look2("CERAPP_Bind","");
		//		lo.look("CERAPP_Ago","");
		//		lo.look("CERAPP_Anta","");

		//		lo.look2("BP","");


	}



}
