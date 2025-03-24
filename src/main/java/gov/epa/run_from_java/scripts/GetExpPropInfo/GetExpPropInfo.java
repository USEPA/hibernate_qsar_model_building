package gov.epa.run_from_java.scripts.GetExpPropInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.validation.ConstraintViolationException;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.w3c.tidy.Out;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.PropertyValueDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundServiceImpl;
import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.entity.ChemicalList;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.service.ChemicalListServiceImpl;
import gov.epa.databases.dsstox.service.DsstoxCompoundService;
import gov.epa.databases.dsstox.service.DsstoxCompoundServiceImpl;
import gov.epa.databases.dsstox.service.SourceSubstanceServiceImpl;
import gov.epa.endpoints.reports.Outliers.Outlier;
import gov.epa.run_from_java.data_loading.ExperimentalRecords;
import gov.epa.run_from_java.data_loading.ExperimentalRecord;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.util.MathUtil;
import gov.epa.util.wekalite.CSVLoader;
import gov.epa.util.wekalite.Instances;
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer;
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer.StandardizeResult;
import gov.epa.web_services.standardizers.Standardizer.StandardizeResponse;
import gov.epa.web_services.standardizers.Standardizer.StandardizeResponseWithStatus;


public class GetExpPropInfo {

	final String strStatus="ICF_status";

	public static final String str_exp_prop_id_previous_version="exp_prop_id_previous_version";

	static ChemicalListServiceImpl listService=new ChemicalListServiceImpl();
	static SourceSubstanceServiceImpl sourceSubstanceService=new SourceSubstanceServiceImpl();


	//	static String[]  fieldsFinal= {"exp_prop_id","canon_qsar_smiles","dtxcid_final","qsar_property_value","qsar_property_units",
	//			"exp_prop_id", "fk_public_source_id", "fk_literature_source_id",
	//			"fk_source_chemical_id",  "name","description", "type","url", "page_url",  "source_dtxrid",
	//			"source_dtxsid", "source_casrn", "source_chemical_name", "source_smiles", "notes", "authors", "doi",
	//			"title", "value_qualifier","value_original","value_max", "value_min", 
	//			"qc_flag","dtxcid_mapped","dtxsid_mapped","smiles_mapped","mol_weight_mapped","value_point_estimate","units","Temperature_C","Pressure_mmHg","pH" };


	//TODO update following fields with revised fields for public_source, public_source_original, and literature_source:
	static String[] fieldsFinal = { "exp_prop_id", "canon_qsar_smiles", "page_url", 

			"public_source_name", "public_source_description", "public_source_url",
			"public_source_original_name", "public_source_original_description", "public_source_original_url",
			"literature_source_citation","literature_source_doi",
			//			"source_url", "source_doi",
			//			"source_name", "source_description", "source_type", "source_authors", "source_title", "source_dtxrid",
			"source_dtxsid", "source_casrn", "source_chemical_name", "source_smiles", "mapped_dtxcid", "mapped_dtxsid",
			"mapped_cas", "mapped_chemical_name", "mapped_smiles", "mapped_molweight", "value_original", "value_max",
			"value_min", "value_point_estimate", "value_units", 
			"qsar_property_value", "qsar_property_units",
			"experimental_median","predicted_CV","predicted_CV_Error",
			"temperature_c", "pressure_mmHg", "pH", "notes", "qc_flag",
			"ICF_cluster","ICF_status","ICF_notes","ICF_additional_resources_needed",
			"ICF_duplicate","ICF_property_mismatch",
			"ICF_chemical_matches", "ICF_corrected_chemical",
			"ICF_is_experimental","ICF_exp_method","ICF_test_guidelines",
			"ICF_source_url", "ICF_source_type", "ICF_citation",
			"ICF_reportedvalue","ICF_reportedunits",
			"ICF_property_value", "ICF_units_conversion_error", "ICF_temperature_c", "ICF_pressure_mmHg", "ICF_pH"};


	
	static String[] fieldsFinalNew = { "exp_prop_id", str_exp_prop_id_previous_version,"canon_qsar_smiles", "page_url", 

			"public_source_name", "public_source_description", "public_source_url",
			"public_source_original_name", "public_source_original_description", "public_source_original_url",
			"literature_source_citation","literature_source_doi",
			//			"source_url", "source_doi",
			//			"source_name", "source_description", "source_type", "source_authors", "source_title", "source_dtxrid",
			"source_dtxsid", "source_casrn", "source_chemical_name", "source_smiles", "mapped_dtxcid", "mapped_dtxsid",
			"mapped_cas", "mapped_chemical_name", "mapped_smiles", "mapped_molweight", "value_original","value_original_parsed", "value_max",
			"value_min", "value_point_estimate", "value_units", 
			"qsar_property_value", "qsar_property_units",
			"experimental_median","predicted_CV","predicted_CV_Error",
			"temperature_c", "pressure_mmHg", "pH", "notes", "qc_flag",
			"ICF_cluster","ICF_status","ICF_notes","ICF_additional_resources_needed",
			"ICF_duplicate","ICF_property_mismatch",
			"ICF_chemical_matches", "ICF_corrected_chemical",
			"ICF_is_experimental","ICF_exp_method","ICF_test_guidelines",
			"ICF_source_url", "ICF_source_type", "ICF_citation",
			"ICF_reportedvalue","ICF_reportedunits",
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
	 * @param flagLargeCVError 
	 */
	static void createCheckingSpreadsheet_PFAS_data(String dataSetName,String folder,HashSet<String>arrayPFAS_CIDs,String listName,Hashtable<String,String> htOperaReferences, boolean flagLargeCVError) {
		//		String dataSetName=getDataSetName(dataset_id, conn);
		
		Hashtable<String,Outlier>htOutliers=null;
		
		if (flagLargeCVError) {
			htOutliers=loadOutliers(dataSetName);
		}

		
		String dataSetName2=dataSetName.replace(" ", "_").replace("="," ");//filenames in output folder have spaces converted to underscores

		String jsonPath=folder+dataSetName2+"/"+dataSetName2+"_Mapped_Records.json";

		File fileJson=new File(jsonPath);

		if(!fileJson.exists()) {
			System.out.println(jsonPath+" doesnt exist");
		}


		JsonArray ja=Utilities.getJsonArrayFromJsonFile(jsonPath);
		JsonArray ja2=new JsonArray();

		
		ArrayList<String>arrayQSARSmiles=new ArrayList<>();

		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();

			//			System.out.println(Utilities.gson.toJson(jo));

			//			lookupInfoFromRID(connDSSTOX, jo);//dont need to- look at mapped_cas instead

			try {

				String dtxcid_original=jo.get("mapped_dtxcid").getAsString();
				String dtxsid_original=jo.get("mapped_dtxsid").getAsString();
				
				if(htOutliers!=null && htOutliers.get(dtxcid_original)!=null) {
					Outlier outlier=htOutliers.get(dtxcid_original);
					double error=Math.abs(outlier.exp-outlier.pred);
					jo.addProperty("experimental_median",outlier.exp);
					jo.addProperty("predicted_CV",outlier.pred);
					jo.addProperty("predicted_CV_Error",error);
//					System.out.println(dtxcid_original+"\t"+error);
				}
				
				if (jo.get("public_source_name")!=null) {
					String public_source_name=jo.get("public_source_name").getAsString();

					if(htOperaReferences!=null && htOperaReferences.get(dtxsid_original)!=null && public_source_name.equals("OPERA")) {
						String operaReference=htOperaReferences.get(dtxsid_original);
						jo.addProperty("literature_source_citation", operaReference);
						//						System.out.println(operaReference);
					}
				}

//				setICF_duplicate_Sushko_OCHEM(ja, jo);

				if (jo.get("page_url")!=null) {//Fix sander URL
					String page_url=jo.get("page_url").getAsString().replace("http://satellite.mpic.de/henry/","https://www.henrys-law.org/henry/");
					jo.addProperty("page_url", page_url);
				}

				if (jo.get("public_source_url")!=null) {//Fix sander URL
					String source_url=jo.get("public_source_url").getAsString().replace("http://satellite.mpic.de/henry/","https://www.henrys-law.org/henry/");
					jo.addProperty("public_source_url", source_url);
				}

				String canon_qsar_smiles=jo.get("canon_qsar_smiles").getAsString();

				if (arrayPFAS_CIDs.contains(dtxcid_original)) {
					//				System.out.println(dtxcid_original+"\t"+arrayPFAS_CIDs.contains(dtxcid_original));
					ja2.add(jo);				
					if (!arrayQSARSmiles.contains(canon_qsar_smiles)) arrayQSARSmiles.add(canon_qsar_smiles);

				}

			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println("Error:\n"+Utilities.gson.toJson(jo));
			}
		}

		System.out.println(dataSetName2+"\t"+ja2.size()+"\t"+arrayQSARSmiles.size());

		//		System.out.println("Number of PFAS records="+ja2.size());
		//		System.out.println("Number of unique PFAS records="+arrayQSARSmiles.size());

		String pathout=folder+"//"+dataSetName2+"//PFAS "+dataSetName2+"_"+listName+".xlsx";

		Hashtable<String,String>htDescriptions=ExcelCreator.getColumnDescriptions();
		ExcelCreator.createExcel2(ja2, pathout,fieldsFinal,htDescriptions);

		String pathout2=folder+"//checking spreadsheets//"+listName+"_PFAS_"+dataSetName2+".xlsx";

		try {
			Files.copy(Paths.get(pathout), Paths.get(pathout2), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		//		System.out.println("Excel file created:\t"+pathout);

	}
	
	/**
	 * This method creates the checking spreadsheet using the json file that was
	 * created during dataset creation rather than requerying the database
	 * 
	 * @param dataset_id
	 * @param conn
	 * @param folder
	 * @param arrayPFAS_CIDs
	 * @param flagLargeCVError 
	 */
	static void createCheckingSpreadsheet_PFAS_revised_data(String dataSetName,String folder,String listName,JsonArray jaCurate) {
		//		String dataSetName=getDataSetName(dataset_id, conn);
		
		
		String dataSetName2=dataSetName.replace(" ", "_").replace("="," ");//filenames in output folder have spaces converted to underscores

		String pathout=folder+"//"+dataSetName2+"//PFAS "+dataSetName2+"_"+listName+".xlsx";

		Hashtable<String,String>htDescriptions=ExcelCreator.getColumnDescriptions();
		ExcelCreator.createExcel2(jaCurate, pathout,fieldsFinal,htDescriptions);

		String pathout2=folder+"//checking spreadsheets//"+listName+"_PFAS_"+dataSetName2+".xlsx";

		try {
			Files.copy(Paths.get(pathout), Paths.get(pathout2), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		//		System.out.println("Excel file created:\t"+pathout);

	}


	private static Hashtable<String,Outlier> loadOutliers(String dataSetName) {
		
		Hashtable<String,Outlier>htOutliers=new Hashtable<>();
		
		String folderOutlier="data\\reports\\Outlier testing\\";
		
		String filepathJsonOutlier=folderOutlier+dataSetName+File.separator+dataSetName+".json";
		
		if (new File(filepathJsonOutlier).exists()) {

			try {
				String json = Files.readString(Path.of(filepathJsonOutlier));
				Outlier[] outliers= new Gson().fromJson(json, Outlier[].class);
				
				for (Outlier outlier:outliers) {
//					System.out.println(outlier.ID+"\t"+outlier.exp+"\t"+outlier.pred);
					
					String [] ids=outlier.ID.split("\\|");
					
					for (String id:ids) {
						if (outlier.ID.contains("|") && !ids[0].equals(ids[1])) {
//							System.out.println(ids[0]+"\t"+ids[1]);
						}
							
						htOutliers.put(id, outlier);	
					}
					
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else {
			System.out.println(filepathJsonOutlier + " doesnt exist");
		}
		return htOutliers;
	}

	private static void setICF_duplicate_Sushko_OCHEM(JsonArray ja, JsonObject jo) {

		if (jo.get("literature_source_doi")==null) {
			return;
		}

		String literature_source_doi=jo.get("literature_source_doi").getAsString();
		if(!literature_source_doi.equals("https://doi.org/10.1007/s10822-011-9440-2")) {//We need to flag Sushko ones that came from chemprop as duplicates
			return;
		}

		Long exp_prop_id=jo.get("exp_prop_id").getAsLong();				
		String dtxsid_original=jo.get("mapped_dtxsid").getAsString();
		double qsar_property_value=jo.get("qsar_property_value").getAsDouble();	

		//		System.out.println(literature_source_doi);

		double minError=9999;
		DecimalFormat df=new DecimalFormat("0.00");

		for (int j=0;j<ja.size();j++) {
			JsonObject joj=ja.get(j).getAsJsonObject();
			String dtxsid_originalj=joj.get("mapped_dtxsid").getAsString();

			if (joj.get("public_source_name")==null) continue;
			String public_source_originalj=joj.get("public_source_name").getAsString();
			if(!public_source_originalj.equals("OChem")) continue;
			if (!dtxsid_original.equals(dtxsid_originalj)) continue;

			//	System.out.println(dtxsid_original+"\t"+dtxsid_originalj);

			Long exp_prop_idj=joj.get("exp_prop_id").getAsLong();				

			double qsar_property_valuej=joj.get("qsar_property_value").getAsDouble();	

			double err=Math.abs(qsar_property_value-qsar_property_valuej);
			
			if (err<minError && err<0.05) {
				jo.addProperty("ICF_duplicate", exp_prop_idj);
//				System.out.println(exp_prop_id+"\t"+exp_prop_idj+"\t"+df.format(err)+"\tSushko_duplicate");				minError=err;
			}

		}
		//				System.out.println("OCHEM duplicate");


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
	public static void createCheckingSpreadsheet(String dataSetName,String folder,Hashtable<String,String> htOperaReferences) {
		//		String dataSetName=getDataSetName(dataset_id, conn);

		dataSetName=dataSetName.replace(" ", "_").replace("="," ");

		String jsonPath=folder+"//"+dataSetName+"//"+dataSetName+"_Mapped_Records.json";

		JsonArray ja=Utilities.getJsonArrayFromJsonFile(jsonPath);

		System.out.println(ja.size());


		ArrayList<String>arrayQSARSmiles=new ArrayList<>();

		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();

			//			lookupInfoFromRID(connDSSTOX, jo);//dont need to- look at mapped_cas instead

			String dtxcid_original=jo.get("mapped_dtxcid").getAsString();
			String dtxsid_original=jo.get("mapped_dtxsid").getAsString();

			if(htOperaReferences!=null && htOperaReferences.get(dtxsid_original)!=null) {
				String operaReference=htOperaReferences.get(dtxsid_original);
				jo.addProperty("source_description", operaReference);
				//				System.out.println(operaReference);
			}
		}

		String pathout=folder+"//"+dataSetName+"//"+dataSetName+".xlsx";

		Hashtable<String,String>htDescriptions=ExcelCreator.getColumnDescriptions();
		ExcelCreator.createExcel2(ja, pathout,fieldsFinal,htDescriptions);

		String pathout2=folder+"//checking spreadsheets//"+dataSetName+".xlsx";

		try {
			Files.copy(Paths.get(pathout), Paths.get(pathout2), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


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

		JsonArray ja=ExcelCreator.convertExcelToJsonArray(filepath,1,"Records");

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

		JsonArray ja=ExcelCreator.convertExcelToJsonArray(filepath,1,"Records");

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


	public static List<DsstoxRecord> getChemicalsFromDSSTOXList(String listName) {
		ChemicalList chemicalList = listService.findByName(listName);

		if (chemicalList != null) {
			List<DsstoxRecord>dsstoxRecords = sourceSubstanceService
					.findAsDsstoxRecordsWithSourceSubstanceByChemicalListName(listName);
			return dsstoxRecords;
		}
		return null;

	}

	static void createCheckingSpreadsheets() {

		boolean flagLargeCVError=true;
		
		String folder="data/dev_qsar/output/";
		Connection conn=SqlUtilities.getConnectionPostgres();
		Connection connDSSTOX=SqlUtilities.getConnectionDSSTOX();					

		List<String>datasetNames=new ArrayList<>();

		//		datasetNames.add("MP from exp_prop and chemprop");//Done
		//		datasetNames.add("BP from exp_prop and chemprop");
		//		datasetNames.add("VP from exp_prop and chemprop");
		//		datasetNames.add("HLC from exp_prop and chemprop");//Done
		//		datasetNames.add("LogP from exp_prop and chemprop");//DONE
		//		datasetNames.add("WS from exp_prop and chemprop");
		//		datasetNames.add("pKa_a from exp_prop and chemprop");
		//		datasetNames.add("pKa_b from exp_prop and chemprop");

		datasetNames.add("BP v1 modeling");//DONE
		datasetNames.add("VP v1 modeling");//DONE
		datasetNames.add("MP v1 modeling");//DONE
		
		datasetNames.add("HLC v1 modeling");//DONE
		datasetNames.add("WS v1 modeling");//DONE
		datasetNames.add("LogP v1 modeling");//DONE
		

		//		String listName="CCL5PFAS";
		String listName="PFASSTRUCTV4";
		//		String listName="PFASSTRUCTV5";

		//New way get it directly from list in DSSTOX:
		List<DsstoxRecord>dsstoxRecords=getChemicalsFromDSSTOXList(listName);		
		HashSet<String>arrayPFAS_CIDs=new HashSet<>();
		for (DsstoxRecord dr:dsstoxRecords) arrayPFAS_CIDs.add(dr.dsstoxCompoundId);


		System.out.println("dataSetName\tRecords\tuniqueRecords");
		for(String dataSetName:datasetNames) {
			//			System.out.println(dataSetName);

			String abbrev=dataSetName.substring(0,dataSetName.indexOf(" ")).trim().replace("HLC", "HL");

//			System.out.println(abbrev);

			Hashtable<String,String> htOperaReferences=null;

			if (abbrev.equals("LogP")) htOperaReferences=Utilities.createOpera_Reference_Lookup("LogP","Kow Reference");
			if (abbrev.equals("WS")) htOperaReferences=Utilities.createOpera_Reference_Lookup("WS Reference","WS");
			if (abbrev.equals("VP")) htOperaReferences=Utilities.createOpera_Reference_Lookup("VP","VP Reference");
			if (abbrev.equals("HL")) htOperaReferences=Utilities.createOpera_Reference_Lookup("HL","HL Reference");
			//	if (abbrev.equals("MP")) htOperaReferences=Utilities.createOpera_Reference_Lookup("MP","MP");//Has no references
			//	if (abbrev.equals("BP")) htOperaReferences=Utilities.createOpera_Reference_Lookup("BP","BP");//Has no references

			createCheckingSpreadsheet_PFAS_data(dataSetName,folder,arrayPFAS_CIDs,listName,htOperaReferences,flagLargeCVError);//create checking spreadsheet using json file for mapped records that was created when dataset was created
			//			System.out.println("");
		}

		

	}
	
	
	



	static void createPFAS_text_File() {
		boolean standardize=true;

		SciDataExpertsStandardizer standardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY,"qsar-ready","https://hcd.rtpnc.epa.gov");

		CompoundServiceImpl compoundService=new CompoundServiceImpl();


		String listName="PFASSTRUCTV4";
		//		String listName="PFASSTRUCTV5";
		//		String listName="PFASSTRUCTV5";

		//New way get it directly from list in DSSTOX:
		List<DsstoxRecord>dsstoxRecords=getChemicalsFromDSSTOXList(listName);		

		Connection conn=SqlUtilities.getConnectionPostgres();

		String folder="data/dev_qsar/dataset_files/";
		String filePath=folder+listName+"_qsar_ready_smiles.txt";

		FileWriter fw;
		try {
			fw = new FileWriter(filePath);

			fw.write("DTXCID\tcanon_qsar_smiles\tsmiles\n");

			for (DsstoxRecord dr:dsstoxRecords) {

				if(dr.smiles==null) continue;

				String sql="select canon_qsar_smiles from qsar_descriptors.compounds c where \n"+
						"c.smiles ='"+dr.smiles+"' and \n"+
						"standardizer ='SCI_DATA_EXPERTS_QSAR_READY' and \n"+
						"dtxcid ='"+dr.dsstoxCompoundId+"'";

				String canonSmiles=SqlUtilities.runSQL(conn, sql);

				if (canonSmiles==null && standardize) {
					standardize(dr,standardizer,compoundService);
				}

				if (canonSmiles==null) {
					//				System.out.println(dr.smiles);
					continue;
				}

				if (canonSmiles.contains("F")) {
					System.out.println(dr.dsstoxCompoundId+"\t"+canonSmiles+"\t"+dr.smiles);	
					fw.write(dr.dsstoxCompoundId+"\t"+canonSmiles+"\t"+dr.smiles+"\n");
					fw.flush();
				} else {
					System.out.println("Not PFAS\t"+dr.dsstoxCompoundId+"\t"+canonSmiles+dr.smiles);	
				}



			}
			fw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	static void standardize(DsstoxRecord dr,SciDataExpertsStandardizer standardizer,CompoundServiceImpl compoundService) {

		StandardizeResult sr=standardizer.runStandardize(dr.smiles, false);

		//		StandardizeResponseWithStatus standardizeResponse = standardizer.callStandardize(dr.smiles);

		if (sr.status==200) {
			//TMM 6/8/22 store in database:
			Compound compound = new Compound(dr.dsstoxCompoundId,dr.smiles, sr.qsarReadySmiles, standardizer.standardizerName, "tmarti02");

			try {
				compound = compoundService.create(compound);
				System.out.println("standardized:"+dr.dsstoxCompoundId+"\t"+dr.smiles+"\t"+sr.qsarReadySmiles);
			} catch (ConstraintViolationException e) {
				System.out.println(e.getMessage());
			}
		} 
	}
	static void createOPERALookupFile(String propertyAbbrev,String propertyAbbrev2) {



		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\ghs-data-gathering\\data\\experimental\\OPERA\\OPERA_SDFS\\";




		try {
			FileWriter fw=new FileWriter(folder+"OPERA "+propertyAbbrev+" references.txt");

			fw.write("CAS\tDTXSID\tpreferred_name\tReference\r\n");

			String filepath=folder+propertyAbbrev+"_QR.sdf";

			IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepath),DefaultChemObjectBuilder.getInstance());
			while (mr.hasNext()) {
				AtomContainer m=null;
				try {
					m = (AtomContainer)mr.next();
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				if (m==null) break;

				String DTXSID=m.getProperty("dsstox_substance_id");

				if (m.getProperty(propertyAbbrev2+" Reference")==null) {
					System.out.println(propertyAbbrev+"\t"+DTXSID+"\tref missing");

					continue;
				}

				String CAS=m.getProperty("CAS");
				String preferred_name=m.getProperty("preferred_name");
				String Reference=m.getProperty(propertyAbbrev2+" Reference");

				fw.write(CAS+"\t"+DTXSID+"\t"+preferred_name+"\t"+Reference+"\r\n");


			}		


			fw.flush();
			fw.close();


		} catch (Exception ex) {
			ex.printStackTrace();
		}




	}

	static void createEPISUITE_ISIS_Reference_Lookup(String propertyAbbrev,String model) {

		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\ghs-data-gathering\\data\\experimental\\EpisuiteISIS\\EPI_SDF_Data\\";
		String filepath=folder+"EPI_"+model+"_Data_SDF.sdf";

		try {

			FileWriter fw=new FileWriter(folder+model+"_refs.txt");

			fw.write("CAS\tNAME\tKOW\tKowDataType\tTrain\tReference\r\n");

			IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepath),DefaultChemObjectBuilder.getInstance());
			while (mr.hasNext()) {
				AtomContainer m=null;
				try {
					m = (AtomContainer)mr.next();
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				if (m==null) break;

				String CAS=m.getProperty("CAS");
				String NAME=m.getProperty("NAME");
				String KOW=m.getProperty("Kow");
				String Train=m.getProperty("Train");
				String KowDataType=m.getProperty("Kow Data Type");

				if (m.getProperty(propertyAbbrev+" Reference")==null) {
					System.out.println(propertyAbbrev+"\t"+CAS+"\tref missing");
					continue;
				}

				String Reference=m.getProperty(propertyAbbrev+" Reference");

				while (CAS.substring(0, 1).equals("0")) CAS=CAS.substring(1,CAS.length());


				fw.write(CAS+"\t"+NAME+"\t"+KOW+"\t"+KowDataType+"\t"+Train+"\t"+Reference+"\r\n");


			}
			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}


	static void checkOmittedRecordsCVError() {
		
		String folderChecking="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\pfas phys prop\\ICF\\primary source checking results\\";
		
		String checkingFilePath=folderChecking+"PFAS LogP for EPA_23-07-18.xlsx";
		String datasetName="LogP v1 modeling";
		
		JsonArray ja=ExcelCreator.convertExcelToJsonArray(checkingFilePath, 1, "Clustered Records");
		JsonArray jaOmitted=omitRecords(ja);
		
//		System.out.println(Utilities.gson.toJson(jaOmitted));
		
		
		Hashtable<String,Outlier>htOutliers=loadOutliers(datasetName);
		
		int count=0;
		
		for (int i=0;i<jaOmitted.size();i++) {
			JsonObject jo=jaOmitted.get(i).getAsJsonObject();
			
			String mapped_dtxcid=jo.get("mapped_dtxcid").getAsString();
			
			if (htOutliers.get(mapped_dtxcid)!=null) {
				Outlier outlier=htOutliers.get(mapped_dtxcid);
				System.out.println(mapped_dtxcid+"\t"+outlier.exp+"\t"+outlier.pred);
				count++;
			}
		}
		
		
		System.out.println(count+" of "+jaOmitted.size());

		
	}

	private static JsonArray omitRecords(JsonArray ja) {
		JsonArray jaOmitted=new JsonArray();

		
//		System.out.println(Utilities.gson.toJson(ja));
		
		List<Integer>omittedIds=new ArrayList<>();
	
		for (int i=0;i<ja.size();i++) {
			
			JsonObject jo=ja.get(i).getAsJsonObject();
			
			String exp_prop_id=jo.get("exp_prop_id").getAsString();

			if(jo.get("omit_tmm")==null) continue;
						
			jaOmitted.add(jo);
			ja.remove(i--);
//				System.out.println(Utilities.gson.toJson(jo));
			
		}
			
//		System.out.println(jaOmitted.size());
		
		//Loop back through and see if all the duplicates got omitted:
		
		for (int i=0;i<ja.size();i++) {
			
			JsonObject jo=ja.get(i).getAsJsonObject();
			
			if (jo.get("ICF_duplicate")==null) continue;
			
				
			String ICF_duplicate=jo.get("ICF_duplicate").getAsString();
						
			for (int j=0;j<jaOmitted.size();j++) {
				
				JsonObject joOmitted=jaOmitted.get(j).getAsJsonObject();
				String exp_prop_id=joOmitted.get("exp_prop_id").getAsString();
				
				if(ICF_duplicate.equals(exp_prop_id)) {
					
					jo.addProperty("omit_tmm", joOmitted.get("omit_tmm").getAsBoolean());
					jo.addProperty("omit_reason_tmm", joOmitted.get("omit_reason_tmm").getAsString());
//					System.out.println("\n\nMatch!");
//					System.out.println(Utilities.gson.toJson(joOmitted));
//					System.out.println(Utilities.gson.toJson(jo));

					jaOmitted.add(jo);
					ja.remove(i--);
					break;

				}
			}
			
		}
		return jaOmitted;
	}
	
	
	class DatasetParameters {
		String propertyName;
		String dataset;
		String datasetOutlierReport;
		String fileNameICF;
		
		public DatasetParameters(String propertyName, String dataset,String fileNameICF) {
			this.propertyName=propertyName;
			this.dataset=dataset;
			this.fileNameICF=fileNameICF;
		}
	}
	
	
	void compareNewOChemDataToICF() {

		//**TODO need to annotate the ICF spreadsheets with good/bad then can set keep value in exp_prop with code

		List<DatasetParameters>paramList=new ArrayList<DatasetParameters>();

		paramList.add(new DatasetParameters(DevQsarConstants.BOILING_POINT,"BP OChem_2024_04_03","PFAS BP_for EPA_2024_03_26.xlsx"));
		paramList.add(new DatasetParameters(DevQsarConstants.VAPOR_PRESSURE,"VP OChem_2024_04_03","PFAS VP_for EPA_2024-04-25.xlsx"));
		paramList.add(new DatasetParameters(DevQsarConstants.MELTING_POINT,"MP OChem_2024_04_03","PFAS MP_for EPA_2024_06_10.xlsx"));
		paramList.add(new DatasetParameters(DevQsarConstants.HENRYS_LAW_CONSTANT,"HLC OChem_2024_04_03","PFAS HLC for EPA_23-09-26.xlsx"));

		//These 2 datasets arent in the db but there are json files for them:
		paramList.add(new DatasetParameters(DevQsarConstants.WATER_SOLUBILITY,"WS OChem_2024_04_03","PFAS WS_For EPA_2023_12_15.xlsx"));
		paramList.add(new DatasetParameters(DevQsarConstants.LOG_KOW,"LogP OChem_2024_04_03","PFAS LogP for EPA_23-07-18.xlsx"));
		
		String filePathAqSolDBRecords="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\AqSolDB\\AqSolDB Experimental Records.json";
		ExperimentalRecords erAqSolDB=ExperimentalRecords.loadFromJson(filePathAqSolDBRecords, Utilities.gson);
		
		String folderICF_Results="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\pfas phys prop\\ICF\\primary source checking results\\";
		String copyFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\pfas phys prop\\ICF\\checking spreadsheets OChem_2024_04_03\\";
		for(DatasetParameters dp:paramList) {
			compareNewDataToICF(dp, folderICF_Results,copyFolder,erAqSolDB);
		}
		
		
	}
	
	void compareNewPubChemDataToICF() {

		//**TODO need to annotate the ICF spreadsheets with good/bad then can set keep value in exp_prop with code

		List<DatasetParameters>paramList=new ArrayList<DatasetParameters>();

		String sourceName="PubChem_2024_03_20";
		
		paramList.add(new DatasetParameters(DevQsarConstants.BOILING_POINT,"BP "+sourceName,"PFAS BP_for EPA_2024_03_26.xlsx"));
		paramList.add(new DatasetParameters(DevQsarConstants.VAPOR_PRESSURE,"VP "+sourceName,"PFAS VP_for EPA_2024-04-25.xlsx"));
		paramList.add(new DatasetParameters(DevQsarConstants.MELTING_POINT,"MP "+sourceName,"PFAS MP_for EPA_2024_06_10.xlsx"));
		paramList.add(new DatasetParameters(DevQsarConstants.HENRYS_LAW_CONSTANT,"HLC "+sourceName,"PFAS HLC for EPA_23-09-26.xlsx"));
		paramList.add(new DatasetParameters(DevQsarConstants.WATER_SOLUBILITY,"WS "+sourceName,"PFAS WS_For EPA_2023_12_15.xlsx"));
		paramList.add(new DatasetParameters(DevQsarConstants.LOG_KOW,"LogP "+sourceName,"PFAS LogP for EPA_23-07-18.xlsx"));
		
		String filePathAqSolDBRecords="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\AqSolDB\\AqSolDB Experimental Records.json";
		ExperimentalRecords erAqSolDB=ExperimentalRecords.loadFromJson(filePathAqSolDBRecords, Utilities.gson);
		
		String folderICF_Results="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\pfas phys prop\\ICF\\primary source checking results\\";
		String copyFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\pfas phys prop\\ICF\\checking spreadsheets PubChem_2024_03_20\\";
		for(DatasetParameters dp:paramList) {
			compareNewDataToICF(dp, folderICF_Results,copyFolder,erAqSolDB);
		}
		
		
	}


	private void compareNewDataToICF(DatasetParameters dp,String folderICF_Results,String copyFolder, ExperimentalRecords recsAqSolDB) {
		
		boolean flagLargeCVError=true;
		
		HashMap<String, JsonArray>hmBySmiles_ICF=loadExcelResults(folderICF_Results,dp.fileNameICF,2);
//		System.out.println(Utilities.gson.toJson(hmBySmiles_ICF));

		HashMap<String, JsonArray>hmBySmiles_EPA=null;
		
		if(dp.propertyName.equals(DevQsarConstants.WATER_SOLUBILITY)) {
			String folderEPA_Results="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\pfas phys prop\\000000 PFAS data checking\\";
			hmBySmiles_EPA=loadExcelResults(folderEPA_Results,"checking Water solubility PFAS records.xlsx",1);
//			System.out.println(Utilities.gson.toJson(hmBySmiles_EPA));
		}
		
		
		Hashtable<String,Outlier>htOutliers=null;
		
		if (flagLargeCVError) {
			htOutliers=loadOutliers(dp.dataset);
			System.out.println("Number of outliers="+htOutliers.size());
		}

		String dataSetName2=dp.dataset.replace(" ", "_").replace("="," ");
		String folder="data\\dev_qsar\\output\\"+dataSetName2+"\\";
		String filePathMapped=folder+dataSetName2+"_Mapped_Records.json";
		
		try {

			String listName="PFASSTRUCTV4";
//			List<DsstoxRecord>dsstoxRecords=getChemicalsFromDSSTOXList(listName);
			Type listType = new TypeToken<ArrayList<DsstoxRecord>>(){}.getType();
			List<DsstoxRecord>dsstoxRecords=Utilities.gson.fromJson(new FileReader("data\\dev_qsar\\output\\"+listName+".json"),listType);

			HashSet<String>arrayPFAS_CIDs=new HashSet<>();
			for (DsstoxRecord dr:dsstoxRecords) arrayPFAS_CIDs.add(dr.dsstoxCompoundId);

			JsonArray jaNew=Utilities.getJsonArrayFromJsonFile(filePathMapped);
			
			System.out.println("For "+dp.propertyName+", number of records="+jaNew.size());
			
			JsonArray jaNewPFAS=new JsonArray();
			for (JsonElement je : jaNew) {
				JsonObject jo=je.getAsJsonObject();
				String mapped_dtxcid=jo.get("mapped_dtxcid").getAsString();
				if (arrayPFAS_CIDs.contains(mapped_dtxcid)) {
					jaNewPFAS.add(jo);				
				}
			}
			
			System.out.println("Number of New PFAS records="+jaNewPFAS.size());

			HashMap<String, JsonArray>hmBySmilesNewPFAS=convertJsonArrayToMapBySmiles(jaNewPFAS);
			
			//Here is where the magic happens:
			JsonArray []jas=a_compareToICF(dp.propertyName, hmBySmiles_ICF, hmBySmilesNewPFAS,hmBySmiles_EPA,recsAqSolDB,htOutliers);

			Hashtable<String,String>htDescriptions=ExcelCreator.getColumnDescriptions();

			
			JsonArray jaCurate=jas[0];
			JsonArray jaAlreadyCurated=jas[1];
			
			new File(copyFolder).mkdirs();
			
			String excelFileName="PFAS "+dataSetName2+"_PFASSTRUCTV4_to_curate.xlsx";
			String excelFilePath=folder+excelFileName;
			ExcelCreator.createExcel2(jaCurate, excelFilePath,fieldsFinalNew,htDescriptions);

			new File(copyFolder+"to curate").mkdirs();
			Files.copy(Paths.get(excelFilePath), Paths.get(copyFolder+"to curate\\"+excelFileName), StandardCopyOption.REPLACE_EXISTING);

			excelFileName="PFAS "+dataSetName2+"_PFASSTRUCTV4_already_curated.xlsx";
			excelFilePath=folder+excelFileName;
			ExcelCreator.createExcel2(jaAlreadyCurated, excelFilePath,fieldsFinalNew,htDescriptions);

			new File(copyFolder+"already curated").mkdirs();
			Files.copy(Paths.get(excelFilePath), Paths.get(copyFolder+"already curated\\"+excelFileName), StandardCopyOption.REPLACE_EXISTING);

		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void getAqSolDBInfo(JsonObject joOChem,ExperimentalRecords recsAqSolDB, JsonObject joBestMatchICF) {
		
		if(joBestMatchICF!=null) {
			if (joBestMatchICF.get("Original_URL")!=null && !joBestMatchICF.get("Original_URL").isJsonNull()) {
				joOChem.addProperty("page_url", joBestMatchICF.get("Original_URL").getAsString());	
				System.out.println("From ICF spreadsheet, aqsoldb ref="+joBestMatchICF.get("Original_URL").getAsString());
				return;
			}
		}
		
		
		for (ExperimentalRecord er:recsAqSolDB) {
			String source_chemical_name=null;
			String source_casrn=null;
			String source_smiles=null;
			
			if(joOChem.get("source_chemical_name")!=null) {
				source_chemical_name=joOChem.get("source_chemical_name").getAsString();	
			}
			
			if(joOChem.get("source_casrn")!=null) {
				source_casrn=joOChem.get("source_casrn").getAsString();	
			}
			
			if(joOChem.get("source_smiles")!=null) {
				source_smiles=joOChem.get("source_smiles").getAsString();	
			}
			String comboID=source_chemical_name+"\t"+source_casrn+"\t"+source_smiles;
			String comboID_AqSolDB=er.chemical_name+"\t"+er.casrn+"\t"+er.smiles;
			
			if (comboID.equals(comboID_AqSolDB)) {
//			if (source_chemical_name.equals(er.chemical_name)) {
				System.out.println("From OChem, page_url="+joOChem.get("page_url").getAsString());
				joOChem.addProperty("page_url", er.url);
				System.out.println("From er, page_url="+er.url);

			}
				
			break;
		}
	}
	
	
	
	private JsonArray [] a_compareToICF(String propertyName, HashMap<String, JsonArray> hmBySmiles_ICF, HashMap<String, JsonArray> hmBySmilesOChemPFAS,HashMap<String, JsonArray>hmBySmiles_EPA, ExperimentalRecords recsAqSolDB,Hashtable<String,Outlier>htOutliers) {
		
		boolean debug=true;
		
		DecimalFormat df=new DecimalFormat("0.000");
		
		JsonArray jaToCurate=new JsonArray();
		JsonArray jaAlreadyCurated=new JsonArray();
		
		for(String smiles:hmBySmilesOChemPFAS.keySet()) {

			JsonArray jaOChem=hmBySmilesOChemPFAS.get(smiles);
			JsonArray jaICF=hmBySmiles_ICF.get(smiles);

			
//			if (smiles.equals("OC(C(F)(F)F)C(F)(F)F")) {
//				System.out.println(jaICF.size());
//				System.out.println(Utilities.gson.toJson(jaICF));
//			}

			for (JsonElement je:jaOChem) {
				JsonObject joOChem=je.getAsJsonObject();
				String exp_prop_id_OChem=joOChem.get("exp_prop_id").getAsString();
				double qsar_property_value_OChem=Double.parseDouble(joOChem.get("qsar_property_value").getAsString());

//				if(exp_prop_id_OChem.equals("3650383")) System.out.println("HERE1");
//				System.out.println(exp_prop_id_OChem);
				
				JsonObject joBestMatchICF=null;
				
				if(jaICF!=null) 
					joBestMatchICF=getICFBestMatch(jaICF,propertyName,qsar_property_value_OChem,smiles);	
				

				String dtxcid_original=joOChem.get("mapped_dtxcid").getAsString();
				String dtxsid_original=joOChem.get("mapped_dtxsid").getAsString();
				
				if(htOutliers!=null && htOutliers.get(dtxcid_original)!=null) {
					Outlier outlier=htOutliers.get(dtxcid_original);
					double error=Math.abs(outlier.exp-outlier.pred);
					joOChem.addProperty("experimental_median",outlier.exp);
					joOChem.addProperty("predicted_CV",outlier.pred);
					joOChem.addProperty("predicted_CV_Error",error);
//					System.out.println(dtxcid_original+"\t"+error);
				}
				
				
//				if(joOChem.get("literature_source_citation")!=null &&  joOChem.get("literature_source_citation").getAsString().contains("AqSolDB") && propertyName.equals(DevQsarConstants.WATER_SOLUBILITY)) {
////					System.out.println(Utilities.gson.toJson(joOChem));
//					getAqSolDBInfo(joOChem, recsAqSolDB,joBestMatchICF);
//				}

				
				if(jaICF==null && joBestMatchICF==null) {
					
//					if(exp_prop_id_OChem.equals("3650383")) System.out.println("HERE2");
//					System.out.println("No ICF match");
					
					if(hmBySmiles_EPA!=null && hmBySmiles_EPA.containsKey(smiles)) {
						
						
						JsonArray jaEPA=hmBySmiles_EPA.get(smiles);
						JsonObject joEPABestMatch=getEPABestMatch(jaEPA, propertyName, qsar_property_value_OChem);
						
						if(joEPABestMatch!=null) {
							String good=joEPABestMatch.get("Good").getAsString();
							String qsarValueEPA=df.format(joEPABestMatch.get("point_estimate_in_logM").getAsDouble());

							String diff=df.format(joEPABestMatch.get("diff").getAsDouble());
							if(debug) System.out.println(smiles+"\t"+exp_prop_id_OChem+"\tN/A"+"\t"+qsar_property_value_OChem+"\t"+qsarValueEPA+"\t"+diff+"\tGood="+good+"\tNo ICF match");
							jaAlreadyCurated.add(joOChem);	
						} else {
							jaToCurate.add(joOChem);					
							if(debug) System.out.println(smiles+"\t"+exp_prop_id_OChem+"\tN/A"+"\t"+qsar_property_value_OChem+"\tN/A"+"\tN/A\tNo EPA or ICF match");
						}
					} else {
						if(debug) System.out.println(smiles+"\t"+exp_prop_id_OChem+"\tN/A"+"\t"+qsar_property_value_OChem+"\tNo ICF smiles match, No EPA spreadsheet");
						jaToCurate.add(joOChem);					
						
						if(hmBySmiles_EPA!=null && !hmBySmiles_EPA.containsKey(smiles)) {
							System.out.println(smiles +" not in EPA file");
						}
						
					}
					continue;
				}

				
				if (joBestMatchICF!=null) {
					
//					if(exp_prop_id_OChem.equals("3650383")) System.out.println("HERE3");
					
					double minDiff=joBestMatchICF.get("diff").getAsDouble();

					String exp_prop_id_ICF=joBestMatchICF.get("exp_prop_id").getAsString();
					exp_prop_id_ICF=(int)Double.parseDouble(exp_prop_id_ICF)+"";
					double qsar_property_value_ICF=Double.parseDouble(joBestMatchICF.get("qsar_property_value").getAsString());

					String status=joBestMatchICF.get(strStatus).getAsString();
					
					if(status.equals("Reviewed - could not confirm")) {					
						if(debug) System.out.println(smiles+"\t"+exp_prop_id_OChem+"\t"+exp_prop_id_ICF+"\t"+df.format(qsar_property_value_OChem)+"\t"+df.format(qsar_property_value_ICF)+"\t"+df.format(minDiff)+"\t"+status+"\tCould not confirm");
//						System.out.println(smiles+"\t"+exp_prop_id_OChem+"\t"+exp_prop_id_ICF+"\t"+df.format(qsar_property_value_OChem)+"\t"+df.format(qsar_property_value_ICF)+"\t"+df.format(minDiff)+"\t"+status);
						jaToCurate.add(joOChem);
						joOChem.addProperty(str_exp_prop_id_previous_version, exp_prop_id_ICF);
					} else if(status.equals("Reviewed by EPA") || status.equals("Reviewed")) {
						if(debug) System.out.println(smiles+"\t"+exp_prop_id_OChem+"\t"+exp_prop_id_ICF+"\t"+df.format(qsar_property_value_OChem)+"\t"+df.format(qsar_property_value_ICF)+"\t"+df.format(minDiff)+"\t"+status+"\tAlready curated");
						jaAlreadyCurated.add(joOChem);
						joOChem.addProperty(str_exp_prop_id_previous_version, exp_prop_id_ICF);
					} else {
						if(debug) System.out.println(smiles+"\t"+exp_prop_id_OChem+"\t"+exp_prop_id_ICF+"\t"+df.format(qsar_property_value_OChem)+"\t"+df.format(qsar_property_value_ICF)+"\t"+df.format(minDiff)+"\t"+status+"\t???");
					} 

					//				System.out.println("OChem:"+Utilities.gson.toJson(joOChem)+"\nICF:"+Utilities.gson.toJson(joBestMatchICF)+"\n\n");
				} else {
//					if(debug) System.out.println(smiles+"\t"+exp_prop_id_OChem+"\t"+qsar_property_value_OChem+"\tNo reviewed ICF match");
					jaToCurate.add(joOChem);
				}
				//			System.out.println("OChem:"+Utilities.gson.toJson(joOChem)+"\nICF:"+Utilities.gson.toJson(joBestMatchICF)+"\n\n");

			}
		}
		System.out.println("To curate="+jaToCurate.size()+"\tAlready curated="+jaAlreadyCurated.size()+"\n***********************\n\n");
//		System.out.println(Utilities.gson.toJson(jaToCurate));
		JsonArray []jas= {jaToCurate,jaAlreadyCurated};
		return jas;
	}
	
	
	
	JsonObject getICFBestMatch(JsonArray jaICF,String propertyName,double qsar_property_value_OChem,String smiles) {

		
		double minDiff=99999;
		JsonObject joBestMatchICF=null;

		//	System.out.println(jaICF.size());

		int counter=0;
		
		for (JsonElement je2:jaICF) {
			counter++;
			JsonObject joICF=je2.getAsJsonObject();

			String exp_prop_id_ICF=joICF.get("exp_prop_id").getAsString();
			exp_prop_id_ICF=(int)Double.parseDouble(exp_prop_id_ICF)+"";
			double qsar_property_value_ICF=Double.parseDouble(joICF.get("qsar_property_value").getAsString());

//			if (smiles.equals("OC(C(F)(F)F)C(F)(F)F")) {
//				System.out.println(smiles+"\t"+counter+"\t"+joICF.get(strStatus));
//			}

			if(joICF.get(strStatus)==null || joICF.get(strStatus).isJsonNull()  || joICF.get(strStatus).getAsString().equals("Reviewed (duplicate)")) {
				//			if(joICF.get("Status")!=null) System.out.println("***"+joICF.get("Status"));
				//			System.out.println(smiles+"\t"+exp_prop_id_ICF+"\t"+joICF.get("Status"));
				//Dont match with duplicates or ones with no status
				continue;
			}


			double currentDiff=Math.abs(qsar_property_value_OChem-qsar_property_value_ICF);
			boolean isMatch=isMatch(propertyName, currentDiff, qsar_property_value_OChem);
			
//			if (smiles.equals("OC(C(F)(F)F)C(F)(F)F")) {
//				System.out.println("\t"+currentDiff+"\t"+isMatch);
//			}


			if (currentDiff<minDiff && isMatch) {
				minDiff=currentDiff;
				joBestMatchICF=joICF;
			}
		}
		
		if(joBestMatchICF!=null) {
			joBestMatchICF.addProperty("diff", minDiff);
			
//			String status=joBestMatchICF.get(strStatus).getAsString();
//			if(status.equals("Reviewed") && status.equals("Reviewed By EPA"))
//				System.out.println("~~~"+joBestMatchICF.get(strStatus));
		}
		
		return joBestMatchICF;
	}
	
	
	JsonObject getEPABestMatch(JsonArray jaEPA,String propertyName,double qsar_property_value_OChem) {

		double minDiff=99999;
		JsonObject joBestMatch=null;

		//	System.out.println(jaICF.size());

		for (JsonElement je2:jaEPA) {
			JsonObject joEPA=je2.getAsJsonObject();

//			System.out.println(Utilities.gson.toJson(jaEPA));
			
			String exp_prop_id_old=joEPA.get("exp_prop_id").getAsString();
			exp_prop_id_old=(int)Double.parseDouble(exp_prop_id_old)+"";
			double point_estimate_old=Double.parseDouble(joEPA.get("point_estimate_in_logM").getAsString());
			
			double currentDiff=Math.abs(qsar_property_value_OChem-point_estimate_old);
			
			boolean isMatch=isMatch(propertyName, currentDiff, qsar_property_value_OChem);
			
//			System.out.println("\t"+exp_prop_id_old+"\t"+qsar_property_value_OChem+"\t"+point_estimate_old+"\t"+currentDiff+"\t"+isMatch);


			if (currentDiff<minDiff && isMatch) {
				minDiff=currentDiff;
				joBestMatch=joEPA;
			}
		}
		
		if(joBestMatch!=null) {
			joBestMatch.addProperty("diff", minDiff);

//			String exp_prop_id_old=joBestMatch.get("exp_prop_id").getAsString();
//			double point_estimate_old=Double.parseDouble(joBestMatch.get("point_estimate_in_logM").getAsString());
//			System.out.println(exp_prop_id_old+"\t"+qsar_property_value_OChem+"\t"+point_estimate_old);
		}
		
		return joBestMatch;
	}
	
	boolean isMatch(String propertyName,double diff,double propVal1) {
		
		double tol=0.02;
		
		if(propertyName.equals(DevQsarConstants.WATER_SOLUBILITY) 
				|| propertyName.equals(DevQsarConstants.LOG_KOW) 
				|| propertyName.equals(DevQsarConstants.HENRYS_LAW_CONSTANT)
				|| propertyName.equals(DevQsarConstants.VAPOR_PRESSURE)
				|| propertyName.equals(DevQsarConstants.BOILING_POINT)
				|| propertyName.equals(DevQsarConstants.MELTING_POINT)
				) {//qsar value in log units
			if(diff<tol) return true;
		} else {
			System.out.println("Need entry in isMatch for "+propertyName);
		}
		
		return false;
	}
	
	
	void writePFASDsstoxRecords() {
		String listName="PFASSTRUCTV4";
		
		try {
			List<DsstoxRecord>dsstoxRecords=getChemicalsFromDSSTOXList(listName);
			FileWriter fw=new FileWriter("data\\dev_qsar\\output\\"+listName+".json");
			fw.write(Utilities.gson.toJson(dsstoxRecords));
			fw.flush();
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	

	public static void main(String[] args) {
		GetExpPropInfo g=new GetExpPropInfo();

//		g.writePFASDsstoxRecords();

//		g.compareNewOChemDataToICF();
		g.compareNewPubChemDataToICF();
		
//		createCheckingSpreadsheets();
//		checkOmittedRecordsCVError();
		//		createPFAS_text_File();


		//		detectBadLogPvalues();

//		lookAtPFASChecking("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\pfas phys prop\\000000 PFAS data checking\\checking Water solubility PFAS records.xlsx");
		//		omitBadDataPointsFromExpProp("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\pfas phys prop\\000000 PFAS data checking\\checking Water solubility PFAS records.xlsx");

		//		createEPISUITE_ISIS_Reference_Lookup("Kow", "Kowwin");
		//		createOPERALookupFile("LogP","Kow");

	}

	private HashMap<String, JsonArray> loadExcelResults(String folder,String filename,int headerRow) {

		try {

			String filePath=folder+filename;
			FileInputStream fis = new FileInputStream(new File(filePath));
			Workbook wb = WorkbookFactory.create(fis);
			
			Sheet sheet = wb.getSheet("Records");
			
			if (sheet==null) {
				sheet = wb.getSheet("Clustered Records");
			}
			
			JsonArray ja=parseRecordsFromExcel(sheet, true,headerRow);
			
			HashMap<String, JsonArray>hmBySmiles=convertJsonArrayToMapBySmiles(ja);
			
			return hmBySmiles;
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		
	}

	private HashMap<String, JsonArray> convertJsonArrayToMapBySmiles(JsonArray ja) {
		HashMap<String, JsonArray>hmBySmiles=new HashMap<String, JsonArray>();
		
		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			String smiles=jo.get("canon_qsar_smiles").getAsString();
			
			if(hmBySmiles.get(smiles)==null) {
				JsonArray ja2=new JsonArray();
				ja2.add(jo);
				hmBySmiles.put(smiles, ja2);
			} else {
				JsonArray ja2=hmBySmiles.get(smiles);
				ja2.add(jo);
			}
		}
		return hmBySmiles;
	}
	public static JsonArray parseRecordsFromExcel(Sheet sheet,boolean setBlankToNull,int rowHeader) {

		
		String [] headers=getHeaders(sheet,rowHeader);
		HashMap<Integer, String> hmFieldNames=new HashMap<Integer,String>();
		for (int i=0;i<headers.length;i++) {
			hmFieldNames.put(i, headers[i]);
		}

		
		JsonArray records = new JsonArray();
		FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();

		int numRows = sheet.getLastRowNum();
		for (int i = 1; i <= numRows; i++) {
			Row row = sheet.getRow(i);
			if (row == null) {
				continue;
			}
			JsonObject jo = new JsonObject();
			boolean hasAnyFields = false;
			for (int k : hmFieldNames.keySet()) {
				Cell cell = row.getCell(k);
				if (cell == null) {
					continue;
				}
				// cell.setCellType(CELL_TYPE_STRING);

				String content = "";

				try {
					
					CellType type = cell.getCellType();
					
					if (type == CellType.STRING) {
						content = cell.getStringCellValue();
					} else if (type == CellType.NUMERIC) {
						content = cell.getNumericCellValue() + "";
					} else if (type == CellType.BOOLEAN) {
						content = cell.getBooleanCellValue() + "";
					} else if (type == CellType.BLANK) {
						content = "";
						if (setBlankToNull)
							content = null;
					} else if (type == CellType.FORMULA) {//2024-01-23 (TMM)
						type = evaluator.evaluateFormulaCell(cell);
						if (type == CellType.STRING) {
							content = cell.getStringCellValue();
						} else if (type == CellType.NUMERIC) {
							content = cell.getNumericCellValue() + "";
						} else if (type == CellType.BOOLEAN) {
							content = cell.getBooleanCellValue() + "";
						} else if (type == CellType.BLANK) {
							content = "";
							if (setBlankToNull)
								content = null;
						}
					}

				} catch (Exception ex) {
					System.out.println("Error parsing for col " + k + "\tfor row " + i);
				}

				// if(content.contains("Cadmium sulphate")) System.out.println("here1:
				// "+content);

				if (content != null && !content.isBlank()) {
					hasAnyFields = true;
					content=content.trim();
				}
				jo.addProperty(hmFieldNames.get(k), content);
			}
			if (hasAnyFields) {
				records.add(jo);
			}
		}
		return records;
	}
	public static String[] getHeaders(Sheet sheet,int row) {
		Row headerRow = sheet.getRow(row);
		
		int numHeaders = headerRow.getLastCellNum();
		String[] headers = new String[numHeaders];
		for (int i = 0; i < numHeaders; i++) {
			Cell headerCell = headerRow.getCell(i, MissingCellPolicy.CREATE_NULL_AS_BLANK);

//			System.out.println(headerCell.getStringCellValue());

//			headerCell.setCellType(CELL_TYPE_STRING);
			String headerContent = headerCell.getStringCellValue().trim().replaceAll("[^\\p{Alnum}]+", "_")
					.replaceAll("^_", "").replaceAll("_$", "");
			if (headerContent == null || headerContent.equals("_") || headerContent.equals("")) {
				headers[i] = "field" + i;
			} else {
				headers[i] = headerContent;
			}
		}
		return headers;
	}


	private static void detectBadLogPvalues() {
		String folder="data\\dev_qsar\\output\\";
		Connection conn=SqlUtilities.getConnectionPostgres();
		Connection connDSSTOX=SqlUtilities.getConnectionDSSTOX();					
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
