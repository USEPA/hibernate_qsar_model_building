package gov.epa.run_from_java.scripts;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.run_from_java.scripts.GetExpPropInfo.ExcelCreator;
import gov.epa.run_from_java.scripts.GetExpPropInfo.GetExpPropInfo;
//import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;



/**
 * @author TMARTI02
 */
public class PredictionDashboardExcelTableGenerator {

	Connection conn=SqlUtilities.getConnectionPostgres();

	JsonArray getRawRecordsChemicalsDashboardByPropertyNameAndDTXSID_FromView2(HashSet<String> dtxsids) {

		try  {
			Iterator<String> it=dtxsids.iterator();
			String strDtxsids="";
			while (it.hasNext()) {
				strDtxsids+="'"+it.next()+"'";
				if(it.hasNext()) strDtxsids+=",";
			}

			String sql="select * from public.mv_predicted_data\n"
					+"where dtxsid in ("+strDtxsids+")\n"
					+ "order by dtxsid, prop_name, prop_value;";

//			System.out.println(sql);


			ResultSet rs = SqlUtilities.runSQL2(conn, sql);
			JsonArray ja = RawExpDataTableGenerator.getJsonArray(rs);
			convertViewFieldNames(ja);

//			System.out.println(Utilities.gson.toJson(ja));

			return ja;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	public static void convertViewFieldNames(JsonArray ja) {
		Hashtable<String,String>ht=new Hashtable<>();

		ht.put("prop_value", "property_value");
		ht.put("prop_unit", "property_units");
		ht.put("prop_name", "property");
		
		ht.put("prop_type", "property_type");
		ht.put("prop_category", "property_category");

		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			for (String key:ht.keySet()) {
				
//				System.out.println(key+"\t"+jo.get(key));
				
				if(jo.get(key)!=null && !jo.get(key).isJsonNull()) {
//					System.out.println(key+"\t"+jo.get(key));
					jo.addProperty(ht.get(key), jo.get(key).getAsString());	
					
//					System.out.println(key+"\t"+ht.get(key)+"\t"+jo.get(key));
					
					
					jo.remove(key);
				}
			}
		}
	}
	
	
	HashSet<String>getDTXSIDsFromExcel(String filepath,String columnName) {

		HashSet<String>dtxsids=new HashSet<>();
		FileInputStream fis;
		try {

//			System.out.println(filepath);

			fis = new FileInputStream(new File(filepath));
			Workbook wb = WorkbookFactory.create(fis);
			Sheet sheet = wb.getSheetAt(0);

			JsonArray ja=GetExpPropInfo.parseRecordsFromExcel(sheet, true,0);
			
//			System.out.println(Utilities.gson.toJson(ja));

			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				
				
//				System.out.println(Utilities.gson.toJson(jo));
				
				if(jo.get(columnName)==null || jo.get(columnName).isJsonNull())continue;
				String dtxsid=jo.get(columnName).getAsString().trim();
				
//				System.out.println(dtxsid);
				
				if(!dtxsid.contains("DTXSID")) continue;
				if(dtxsid.contains(" ")) continue;

//				System.out.println(dtxsid);
				dtxsids.add(dtxsid);
			}

			//			System.out.println(Utilities.gson.toJson(dtxsids));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return dtxsids;
	}
	
	
	void createPredictionDashboardSpreadsheet() {
		
		String columnName="DTXSID";

		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 scientists\\dan chang\\";
		String filenameInput="dtxsid_512_from_Dale_Hoff.xlsx";

		HashSet<String>dtxsids=getDTXSIDsFromExcel(folder+filenameInput,columnName);
		
//		System.out.println(dtxsids);
		
//		HashSet<String>dtxsids=new HashSet(Arrays.asList("DTXSID00192353","DTXSID6067331","DTXSID30891564","DTXSID6062599","DTXSID90868151","DTXSID8031863","DTXSID8031865","DTXSID1037303","DTXSID8047553","DTXSID60663110","DTXSID70191136","DTXSID3037709","DTXSID3059921","DTXSID3031860","DTXSID8037706","DTXSID8059920","DTXSID3031862","DTXSID30382063","DTXSID00379268","DTXSID20874028","DTXSID3037707"));
		
		JsonArray ja=getRawRecordsChemicalsDashboardByPropertyNameAndDTXSID_FromView2(dtxsids);
		
		String filenameOutput=filenameInput.replace(".xlsx","_pred_data.xlsx");
		String excelFilePath=folder+filenameOutput;

		
		JsonObject jo0=ja.get(0).getAsJsonObject();
//		String[] fields = jo0.keySet().toArray(new String[0]);
		
		String[] fields = { "dtxsid", "smiles", "canon_qsar_smiles", "property", "prop_type", "prop_category",
				"property_description", "model_name", "source_name", "source_description", "property_value",
				"property_units", "prop_value_experimental", "prop_value_experimental_string", "prop_value_string",
				"prop_value_error", "ad_method", "ad_value", "ad_conclusion", "ad_reasoning", "ad_method_global",
				"ad_value_global", "ad_conclusion_global", "ad_reasoning_global", "qmrf_url", "export_date",
				"data_version" };

		ExcelCreator.createExcel3(ja, excelFilePath, fields, null);
		
	}


	public static void main(String[] args) {
		PredictionDashboardExcelTableGenerator r=new PredictionDashboardExcelTableGenerator();
		r.createPredictionDashboardSpreadsheet();
		

	}

}
