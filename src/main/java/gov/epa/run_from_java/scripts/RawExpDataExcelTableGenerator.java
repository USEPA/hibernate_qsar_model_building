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
public class RawExpDataExcelTableGenerator {

	Connection conn=SqlUtilities.getConnectionPostgres();

	JsonArray getRawRecordsChemicalsDashboardByPropertyNameAndDTXSID_FromView(HashSet<String> dtxsids) {

		try  {
			Iterator<String> it=dtxsids.iterator();
			String strDtxsids="";
			while (it.hasNext()) {
				strDtxsids+="'"+it.next()+"'";
				if(it.hasNext()) strDtxsids+=",";
			}

			String sql="select * from public.v_experimental_data\n"
					+"where dtxsid in ("+strDtxsids+")\n"
					+ "order by prop_value;";

			System.out.println(sql);


			ResultSet rs = SqlUtilities.runSQL2(conn, sql);
			JsonArray ja = RawExpDataTableGenerator.getJsonArray(rs);
			RawExpDataTableGenerator.convertViewFieldNames(ja);

			System.out.println(Utilities.gson.toJson(ja));

			return ja;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
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


	public static void main(String[] args) {
		RawExpDataExcelTableGenerator r=new RawExpDataExcelTableGenerator();

		
//		String columnName="DTXSID";
		String columnName="RC_DTXSID_Comptox";
		
		
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 scientists\\Tony\\export pchem data\\";
//		String filenameInput="HESI_UVCB_substances.xlsx";
		String filenameInput="HESI_Representative_Substances_1115_2024.xlsx";
						
//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 scientists\\elaine hubbal\\";
//		String filenameInput="11_14_2024 10_39_31 AM.xlsx";
//		String filenameInput="AHHS_joined_DTXSIDs.xlsx";

		HashSet<String>dtxsids=r.getDTXSIDsFromExcel(folder+filenameInput,columnName);
		//		List<String>dtxsids=Arrays.asList("DTXSID3039242");

		JsonArray ja=r.getRawRecordsChemicalsDashboardByPropertyNameAndDTXSID_FromView(dtxsids);
		
		String filenameOutput=filenameInput.replace(".xlsx","_exp_data.xlsx");
		String excelFilePath=folder+filenameOutput;

		String []fields={"dtxsid","prop_type","dataset","property",
				"property_value","property_units","property_value_text","property_value_original",
				"temperature_C","pressure_mmHg","pH",
				"response_site","species_latin","species_common",
				"public_source_name","public_source_description","public_source_url",
				"literature_source_name","short_citation","literature_source_description","literature_source_doi",
				"direct_link","update_date",  "data_version"};
		//TODO add public_source_original fields

		ExcelCreator.createExcel3(ja, excelFilePath, fields, null);

	}

}
