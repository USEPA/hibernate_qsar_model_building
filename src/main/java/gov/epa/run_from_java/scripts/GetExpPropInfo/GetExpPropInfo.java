package gov.epa.run_from_java.scripts.GetExpPropInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.PropertyValueDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.service.DsstoxCompoundService;
import gov.epa.databases.dsstox.service.DsstoxCompoundServiceImpl;
import gov.epa.util.MathUtil;
import gov.epa.util.wekalite.CSVLoader;
import gov.epa.util.wekalite.Instances;


public class GetExpPropInfo {


	//	static String[]  fieldsFinal= {"exp_prop_id","canon_qsar_smiles","dtxcid_final","qsar_property_value","qsar_property_units",
	//			"exp_prop_id", "fk_public_source_id", "fk_literature_source_id",
	//			"fk_source_chemical_id",  "name","description", "type","url", "page_url",  "source_dtxrid",
	//			"source_dtxsid", "source_casrn", "source_chemical_name", "source_smiles", "notes", "authors", "doi",
	//			"title", "value_qualifier","value_original","value_max", "value_min", 
	//			"qc_flag","dtxcid_mapped","dtxsid_mapped","smiles_mapped","mol_weight_mapped","value_point_estimate","units","Temperature_C","Pressure_mmHg","pH" };

	static String[] fieldsFinal = { "exp_prop_id", "canon_qsar_smiles", "page_url", "source_url", "source_doi",
			"source_name", "source_description", "source_type", "source_authors", "source_title", "source_dtxrid",
			"source_dtxsid", "source_casrn", "source_chemical_name", "source_smiles", "mapped_dtxcid", "mapped_dtxsid",
			"mapped_cas", "mapped_chemical_name", "mapped_smiles", "mapped_molweight", "value_original", "value_max",
			"value_min", "value_point_estimate", "value_units", "qsar_property_value", "qsar_property_units",
			"temperature_c", "pressure_mmHg", "pH", "notes", "qc_flag",
			"ICF_chemical_matches", "ICF_is_experimental", "ICF_source_url", "ICF_source_type", "ICF_citation",
			"ICF_property_value", "ICF_units_conversion_error", "ICF_temperature_c", "ICF_pressure_mmHg", "ICF_pH"};

	static void lookatEchemPortalRecordsLogKow(String dataSetName,Connection conn,Connection connDSSTOX,String folder) {
//		String dataSetName=getDataSetName(dataset_id, conn);

		dataSetName=dataSetName.replace(" ", "_").replace("="," ");
		
		String jsonPath=folder+"//"+dataSetName+"//"+dataSetName+"_Mapped_Records.json";

		JsonArray ja=Utilities.getJsonArrayFromJsonFile(jsonPath);
		
		Hashtable<String,JsonArray>htRecsBySmiles=new Hashtable<>();
		
		DataPointServiceImpl dpsi=new DataPointServiceImpl();
		List<DataPoint>datapoints=dpsi.findByDatasetName(dataSetName);
		
		Hashtable<String,DataPoint>htDatapointsBySmiles=new Hashtable<>();
		
		for (DataPoint dp:datapoints) {
			htDatapointsBySmiles.put(dp.getCanonQsarSmiles(), dp);
		}


		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
//			String cid=jo.get("mapped_dtxcid").getAsString();
			String canon_qsar_smiles=jo.get("canon_qsar_smiles").getAsString();
			String source_name=jo.get("source_name").getAsString();
			
			if(!source_name.equals("eChemPortalAPI")) continue;
				
			if(htRecsBySmiles.get(canon_qsar_smiles)==null) {
				JsonArray ja2=new JsonArray();
				ja2.add(jo);
				htRecsBySmiles.put(canon_qsar_smiles,ja2);
			} else {
				JsonArray ja2=htRecsBySmiles.get(canon_qsar_smiles);
				ja2.add(jo);
			}
		}	
		
		Set<String>smilesKeys=htRecsBySmiles.keySet();
		
		System.out.println(smilesKeys.size());
		
		int count=0;
		
		for (String smiles:smilesKeys) {
			
			JsonArray ja2=htRecsBySmiles.get(smiles);
			if(ja2.size()==1) continue;

			double max=-9999999;
			double min=999999999;
				
			for(int i=0;i<ja2.size();i++) {
				JsonObject jo=ja2.get(i).getAsJsonObject();

				if (jo.get("value_point_estimate")==null) continue;
				double value_point_estimate=jo.get("value_point_estimate").getAsDouble();					

				if(value_point_estimate<min) min=value_point_estimate;
				if(value_point_estimate>max) max=value_point_estimate;
				//					System.out.println(gson.toJson(jo));
			}
			
			DataPoint dp=htDatapointsBySmiles.get(smiles);
			
				
//			if(max<5) continue;
									
			for (int i = 0; i < ja2.size(); i++) {
				JsonObject jo = ja2.get(i).getAsJsonObject();

				if (jo.get("value_point_estimate") == null) continue;
				
				
				double value_point_estimate = jo.get("value_point_estimate").getAsDouble();
				String exp_prop_id=jo.get("exp_prop_id").getAsString();

//				double diff=Math.abs(max-value_point_estimate);
				
				
				
				
//				if (value_point_estimate>15) {
//					System.out.println(++count+"\t"+exp_prop_id+"\t"+smiles+"\t"+value_point_estimate+"\t"+min+"\tpoint_estimate>15");					
//					//TODO convert
//				} else if(diff<0.001 && Math.abs(max-min)>5) {
////					System.out.println(gson.toJson(jo));
//					double logdiff=Math.abs(Math.log10(max)-min);
//					DecimalFormat df=new DecimalFormat("0.00");
//					System.out.println(++count+"\t"+exp_prop_id+"\t"+smiles+"\t"+value_point_estimate+"\t"+min+"\tis max value && Math.abs(max-min)>5");
//				} else {//OK values?
////					System.out.println(++count+"\t"+exp_prop_id+"\t"+cid+"\t"+value_point_estimate+"\t"+min+"\tRemainder");	
//				}

			}				
			
			
		}
//		System.out.println(count);
		

	}
	
	/**
	 * Looks to see if taking the log makes a value match the qsar property value
	 * 
	 * @param dataSetName
	 * @param conn
	 * @param folder
	 */
	static void detectBadLogPValuesFromExpprop(String dataSetName,Connection conn,String folder) {
//		String dataSetName=getDataSetName(dataset_id, conn);

		dataSetName=dataSetName.replace(" ", "_").replace("="," ");

		//Getting mapped records:
		String jsonPath=folder+"//"+dataSetName+"//"+dataSetName+"_Mapped_Records.json";
		JsonArray ja=Utilities.getJsonArrayFromJsonFile(jsonPath);
		Hashtable<String,JsonArray>htRecsBySmiles=new Hashtable<>();
		
		//Getting flattened datapoints:
		DataPointServiceImpl dpsi=new DataPointServiceImpl();
		List<DataPoint>datapoints=dpsi.findByDatasetName(dataSetName);
		Hashtable<String,DataPoint>htDatapointsBySmiles=new Hashtable<>();
		for (DataPoint dp:datapoints) {
			htDatapointsBySmiles.put(dp.getCanonQsarSmiles(), dp);
		}

		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
//			String cid=jo.get("mapped_dtxcid").getAsString();
			String canon_qsar_smiles=jo.get("canon_qsar_smiles").getAsString();
//			String source_name=jo.get("source_name").getAsString();			
//			if(!source_name.equals("eChemPortalAPI")) continue;
				
			if(htRecsBySmiles.get(canon_qsar_smiles)==null) {
				JsonArray ja2=new JsonArray();
				ja2.add(jo);
				htRecsBySmiles.put(canon_qsar_smiles,ja2);
			} else {
				JsonArray ja2=htRecsBySmiles.get(canon_qsar_smiles);
				ja2.add(jo);
			}
		}	
		
		Set<String>smilesKeys=htRecsBySmiles.keySet();
		
		System.out.println(smilesKeys.size());
		
		DecimalFormat df=new DecimalFormat("0.00");
		
		int count=0;
		
		for (String smiles:smilesKeys) {
			
			if (htDatapointsBySmiles.get(smiles)==null) continue;
			
			DataPoint dp=htDatapointsBySmiles.get(smiles);
			
			JsonArray ja2=htRecsBySmiles.get(smiles);
			if(ja2.size()==1) continue;
				
			for(int i=0;i<ja2.size();i++) {
				JsonObject jo=ja2.get(i).getAsJsonObject();
				
				String source_name=jo.get("source_name").getAsString();
				String exp_prop_id=jo.get("exp_prop_id").getAsString();

				Double value_point_estimate=null;
				
				if (jo.get("value_point_estimate")==null) {
					value_point_estimate = (jo.get("value_max").getAsDouble()+jo.get("value_min").getAsDouble())/2.0;
					
				} else {
					value_point_estimate=jo.get("value_point_estimate").getAsDouble();					
				}
					
				double diff=Math.abs(value_point_estimate-dp.getQsarPropertyValue());
				
				double diff2=Math.abs(Math.log10(value_point_estimate)-dp.getQsarPropertyValue());

				if (diff>0.5 && diff2<0.5)
					System.out.println(++count+"\t"+smiles+"\t"+value_point_estimate+"\t"+df.format(Math.log10(value_point_estimate))+"\t"+df.format(dp.getQsarPropertyValue())+"\t"+df.format(diff2)+"\t"+exp_prop_id+"\t"+source_name);

//				if (diff>0.5 && diff2>0.5)
//					System.out.println(++count+"\t"+smiles+"\t"+value_point_estimate+"\t"+df.format(Math.log10(value_point_estimate))+"\t"+df.format(dp.getQsarPropertyValue())+"\t"+df.format(diff2)+"\t"+exp_prop_id+"\t"+source_name);

				
			}
			

			
		}
//		System.out.println(count);
		

	}


	/**
	 * This method creates the checking spreadsheet using the json file that was
	 * created during dataset creation rather than requerying the database
	 * 
	 * @param dataset_id
	 * @param conn
	 * @param folder
	 * @param arrayPFAS_CIDs
	 */
	static void getPropertyBoundsForMappedRecords(String dataSetName,String folder, Connection conn) {
//		String dataSetName=getDataSetName(dataset_id, conn);

		DatasetServiceImpl dsi=new DatasetServiceImpl();
		Dataset dataset_exp_prop=dsi.findByName(dataSetName);
		
		if(dataset_exp_prop==null) {
			System.out.println("*"+dataSetName+"* is null");
			return;
		}
		
		String property=dataset_exp_prop.getProperty().getName();
		if (property.equals("LogBCF_Fish_WholeBody")) property="LogBCF";
		
		Dataset datasetOpera=dsi.findByName(property+" OPERA");
		String sqlMax="select max(qsar_property_value) from qsar_datasets.data_points dp where dp.fk_dataset_id ="+datasetOpera.getId();
		double operaMax=Double.parseDouble(DatabaseLookup.runSQL(conn, sqlMax));
		String sqlMin="select min(qsar_property_value) from qsar_datasets.data_points dp where dp.fk_dataset_id ="+datasetOpera.getId();
		double operaMin=Double.parseDouble(DatabaseLookup.runSQL(conn, sqlMin));
		
		String operaUnits=datasetOpera.getUnit().getName();
		
		if(operaUnits.equals("log10(M)") || operaUnits.equals("log10(atm-m3/mol)")) {
			double bob=-operaMax;
			operaMax=-operaMin;
			operaMin=bob;			
			datasetOpera.getUnit().setName("-"+operaUnits);
		}

		String dataSetName2=dataSetName.replace(" ", "_").replace("="," ");		
		String jsonPath=folder+"//"+dataSetName2+"//"+dataSetName2+"_Mapped_Records.json";
		
//		if (property.contains("Water solubility")) {//filepath too long
//			jsonPath="data//WS_Mapped_Records.json";
//			File file=new File(jsonPath);
//			System.out.println(file.getAbsolutePath());
//		}
		
		JsonArray ja=Utilities.getJsonArrayFromJsonFile(jsonPath);

		JsonObject joMin=new JsonObject();
		joMin.addProperty("qsar_property_value", 999999999);
		JsonObject joMax=new JsonObject();
		joMax.addProperty("qsar_property_value", -999999999);
		
		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			double qsar_property_value=jo.get("qsar_property_value").getAsDouble();
			if(qsar_property_value>joMax.get("qsar_property_value").getAsDouble()) joMax=jo;
			if(qsar_property_value<joMin.get("qsar_property_value").getAsDouble())	joMin=jo;
		}

		double exp_prop_Min=joMin.get("qsar_property_value").getAsDouble();
		double exp_prop_Max=joMax.get("qsar_property_value").getAsDouble();

		System.out.println("dataset:"+dataSetName);
		System.out.println("opera bounds:"+operaMin+"\t"+operaMax+"\t"+datasetOpera.getUnit().getName());
		System.out.println("mapped record bounds:"+exp_prop_Min+"\t"+exp_prop_Max+"\t"+dataset_exp_prop.getUnit().getName());
		
//		System.out.println(Utilities.gson.toJson(joMin));
//		System.out.println(Utilities.gson.toJson(joMax));
		
	}
	

	/**
	 * This method creates the checking spreadsheet using the json file that was
	 * created during dataset creation rather than requerying the database
	 * 
	 * @param dataset_id
	 * @param conn
	 * @param folder
	 * @param arrayPFAS_CIDs
	 */
	static void createCheckingSpreadsheet_PFAS_data(String dataSetName,Connection conn,Connection connDSSTOX,String folder,ArrayList<String>arrayPFAS_CIDs,String versionRulesPFAS,Hashtable<String,String> htOperaReferences) {
//		String dataSetName=getDataSetName(dataset_id, conn);

		dataSetName=dataSetName.replace(" ", "_").replace("="," ");
		
		String jsonPath=folder+"//"+dataSetName+"//"+dataSetName+"_Mapped_Records.json";

		JsonArray ja=Utilities.getJsonArrayFromJsonFile(jsonPath);
		JsonArray ja2=new JsonArray();


		ArrayList<String>arrayQSARSmiles=new ArrayList<>();

		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			
//			lookupInfoFromRID(connDSSTOX, jo);//dont need to- look at mapped_cas instead
			
			String dtxcid_original=jo.get("mapped_dtxcid").getAsString();
			String dtxsid_original=jo.get("mapped_dtxsid").getAsString();
			
			if(htOperaReferences.get(dtxsid_original)!=null) {
				String operaReference=htOperaReferences.get(dtxsid_original);
				jo.addProperty("source_description", operaReference);
				
//				System.out.println(operaReference);
			}
			
			String canon_qsar_smiles=jo.get("canon_qsar_smiles").getAsString();

			if (arrayPFAS_CIDs.contains(dtxcid_original)) {
				//				System.out.println(dtxcid_original+"\t"+arrayPFAS_CIDs.contains(dtxcid_original));
				ja2.add(jo);				
				if (!arrayQSARSmiles.contains(canon_qsar_smiles)) arrayQSARSmiles.add(canon_qsar_smiles);

			}
		}

		System.out.println(dataSetName+"\t"+ja2.size()+"\t"+arrayQSARSmiles.size());
		
//		System.out.println("Number of PFAS records="+ja2.size());
//		System.out.println("Number of unique PFAS records="+arrayQSARSmiles.size());

		String pathout=folder+"//"+dataSetName+"//PFAS "+dataSetName+"_PFASSTRUCT"+versionRulesPFAS+".xlsx";

		Hashtable<String,String>htDescriptions=ExcelCreator.getColumnDescriptions();
		ExcelCreator.createExcel2(ja2, pathout,fieldsFinal,htDescriptions);

//		System.out.println("Excel file created:\t"+pathout);

	}
	
	static ArrayList<String> getPFAS_CIDs(String filepath) {
		try {
			List<String> Lines = Files.readAllLines(Paths.get(filepath));
			ArrayList<String>arrayCIDs=new ArrayList<>();

			for (String Line:Lines) {
				String [] values=Line.split("\t");
				arrayCIDs.add(values[0]);
//				System.out.println(values[0]);
			}
			return arrayCIDs;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	

	/**
	 * Looks at checking spreadsheet to determine effect of Good? field from curation
	 * 
	 * @param filepath
	 */
	static void lookAtPFASChecking (String filepath) {

		JsonArray ja=ExcelCreator.convertExcelToJsonArray(filepath,1);

		//		System.out.println(gson.toJson(ja));
		System.out.println("number of records="+ja.size());

		Hashtable<String,JsonArray>ht=new Hashtable<>();

		boolean omitNo=true;
		boolean omitMaybe=true;
		double tol=0.5;

		for (int i=0;i<ja.size();i++) {

			JsonObject jo=ja.get(i).getAsJsonObject();

			String canon_qsar_smiles=jo.get("canon_qsar_smiles").getAsString();

			if(ht.get(canon_qsar_smiles)==null) {
				JsonArray ja2=new JsonArray();
				ja2.add(jo);
				ht.put(canon_qsar_smiles,ja2);

			} else {
				JsonArray ja2=ht.get(canon_qsar_smiles);
				ja2.add(jo);
			}
		}


		Set<String>keys=ht.keySet();
		System.out.println("number of unique smiles="+keys.size());

		int countZeroRecords=0;
		int countStddev=0;
		double avgDev=0;
		int countChangedMedian=0;

		for (String canon_qsar_smiles:keys) {

			//			System.out.println(canon_qsar_smiles);

			JsonArray ja2=ht.get(canon_qsar_smiles);

			int initialSize=ja2.size();
			
			for (int i=0;i<ja2.size();i++) {
				JsonObject jo=ja2.get(i).getAsJsonObject();
				String Good=jo.get("Good?").getAsString();
//				System.out.println(canon_qsar_smiles+"\t"+Good);
				if (omitNo && Good.contentEquals("No")) ja2.remove(i--);				
				if (omitMaybe && Good.contentEquals("Maybe")) ja2.remove(i--);
			}

//			System.out.println(canon_qsar_smiles+"\t"+initialSize+"\t"+ja2.size());
			
			if (ja2.size()==0) {
				countZeroRecords++;
				//				System.out.println(canon_qsar_smiles);
			} else {
				
				List<Double>vals=new ArrayList();

				double qsar_property_value=Double.parseDouble(ja2.get(0).getAsJsonObject().get("qsar_property_value").getAsString());


				for (int i=0;i<ja2.size();i++) {
					JsonObject jo=ja2.get(i).getAsJsonObject();
					double pointEstimate=Double.parseDouble(jo.get("point estimate in -logM").getAsString());
					vals.add(pointEstimate);
//					System.out.println(qsar_property_value+"\t"+pointEstimate);
				}
				
				
				Collections.sort(vals);
				
				
				double qsar_property_value_new=-9999;
				
				if (vals.size() % 2 == 0)
					qsar_property_value_new = (vals.get(vals.size()/2) + vals.get(vals.size()/2 - 1))/2.0;
				else
					qsar_property_value_new = vals.get(vals.size()/2);

				if (Math.abs(qsar_property_value_new-qsar_property_value)>tol) {
					countChangedMedian++;
//					System.out.println(qsar_property_value+"\t"+qsar_property_value_new);	
				}
				
				if (ja2.size()>1) {
					countStddev++;
					double stddev=MathUtil.stdevS(vals);
					avgDev+=stddev;
					System.out.println(canon_qsar_smiles+"\t"+stddev);
				}
				
			}


		}
		
		
		avgDev/=(double)countStddev;
		
		System.out.println("omitNo\t"+omitNo);
		System.out.println("omitMaybe\t"+omitMaybe);
		System.out.println("Number of smiles with no records\t"+countZeroRecords);
		System.out.println("Number of smiles with changed median\t"+countChangedMedian);
		System.out.println("Avg std dev\t"+avgDev);
		System.out.println("Count std dev\t"+countStddev);

	}
	
	/**
	 * Uses PFAS manual checking spreadsheet to omit bad exp_prop records from property_values table
	 * 
	 * @param filepath
	 */
	static void omitBadDataPointsFromExpProp (String filepath) {

		JsonArray ja=ExcelCreator.convertExcelToJsonArray(filepath,1);

		//		System.out.println(gson.toJson(ja));
		System.out.println("number of records="+ja.size());

		Hashtable<String,JsonArray>ht=new Hashtable<>();

		boolean omitNo=true;
		boolean omitMaybe=false;
		double tol=0.5;
		
		PropertyValueDaoImpl pvdi=new PropertyValueDaoImpl (); 
		PropertyValueServiceImpl pvsi=new PropertyValueServiceImpl();
		
//		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
//		session.getTransaction().begin();
		
		int count=0;
		
		for (int i=0;i<ja.size();i++) {

			JsonObject jo=ja.get(i).getAsJsonObject();

			String canon_qsar_smiles=jo.get("canon_qsar_smiles").getAsString();
			String Good=jo.get("Good?").getAsString();
			
			if((omitNo && Good.equals("No")) || (omitMaybe) && Good.equals("Maybe")) {
			
				String Reasoning=jo.get("Reasoning").getAsString();
//				System.out.println(Reason);
				
				long exp_prop_id=(long)Double.parseDouble(jo.get("exp_prop_id").getAsString());

				String value_point_estimate=jo.get("value_point_estimate").getAsString();
				
				PropertyValue pv=pvdi.findById(exp_prop_id);

//				System.out.println(exp_prop_id+"\t"+value_point_estimate+"\t"+pv.getValuePointEstimate()+"\t"+Good+"\t"+Reasoning);
				System.out.println(canon_qsar_smiles);

//				if(true) continue;
				
				//Update the PropertyValue:
				try {
					pv.setKeep(false);
					pv.setKeepReason("Omit from manual literature check: "+Reasoning);
//					pvsi.update(pv);
					count++;
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
			
		}

		
		System.out.println("Count omitted="+count);


	}


	static void createCheckingSpreadsheets() {
		
		String folder="data\\dev_qsar\\output\\";
		Connection conn=DatabaseLookup.getConnection();
		Connection connDSSTOX=DatabaseLookup.getConnectionDSSTOX();					
		
		List<String>datasetNames=new ArrayList<>();

		datasetNames.add("MP from exp_prop and chemprop");
		datasetNames.add("BP from exp_prop and chemprop");
		datasetNames.add("WS from exp_prop and chemprop");
		datasetNames.add("LogP from exp_prop and chemprop");
		datasetNames.add("VP from exp_prop and chemprop");
		datasetNames.add("HLC from exp_prop and chemprop");
		datasetNames.add("ExpProp_BCF_Fish_TMM");
		
//		String version="V4";
		String version="V5";
		//From File generated from PFAS_SplittingGenerator.generateQSAR_ReadyPFAS_STRUCT, create list of PFAS cids:
		ArrayList<String>arrayPFAS_CIDs=getPFAS_CIDs("data\\dev_qsar\\dataset_files\\PFASSTRUCT"+version+"_qsar_ready_smiles.txt");

		Hashtable<String,String> htOperaReferences=Utilities.createOpera_Reference_Lookup("LogP","Kow");

		System.out.println("dataSetName\tRecords\tuniqueRecords");
		for(String dataSetName:datasetNames) {
//			System.out.println(dataSetName);
			createCheckingSpreadsheet_PFAS_data(dataSetName,conn,connDSSTOX, folder,arrayPFAS_CIDs,version,htOperaReferences);//create checking spreadsheet using json file for mapped records that was created when dataset was created
//			System.out.println("");
		}

	}
	
	

	public static void main(String[] args) {

		createCheckingSpreadsheets();
		
//		detectBadLogPvalues();
		
//		lookAtPFASChecking("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\pfas phys prop\\000000 PFAS data checking\\checking Water solubility PFAS records.xlsx");
//		omitBadDataPointsFromExpProp("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\pfas phys prop\\000000 PFAS data checking\\checking Water solubility PFAS records.xlsx");


	}

	private static void detectBadLogPvalues() {
		String folder="data\\dev_qsar\\output\\";
		Connection conn=DatabaseLookup.getConnection();
		Connection connDSSTOX=DatabaseLookup.getConnectionDSSTOX();					
		detectBadLogPValuesFromExpprop("ExpProp_LogP_WithChemProp_TMM3", conn, folder);
	}

	

	private static Vector<String> getListOfFields(JsonArray jaOverall) {
		Vector<String>recordFields=new Vector<>();

		for (int i=0;i<jaOverall.size();i++) {
			JsonObject jo=jaOverall.get(i).getAsJsonObject();
			JsonArray jaRecords=jo.get("Records").getAsJsonArray();

			for (int j=0;j<jaRecords.size();j++) {
				JsonObject joRecord=jaRecords.get(j).getAsJsonObject();

				Set<Map.Entry<String, JsonElement>> entries = joRecord.entrySet();
				for(Map.Entry<String, JsonElement> entry: entries) {
					if (!recordFields.contains(entry.getKey())) {
						recordFields.add(entry.getKey());
					}
				}
			}

		}

		for (String recordField:recordFields) {
			System.out.println(recordField);
		}
		return recordFields;
	}


}
