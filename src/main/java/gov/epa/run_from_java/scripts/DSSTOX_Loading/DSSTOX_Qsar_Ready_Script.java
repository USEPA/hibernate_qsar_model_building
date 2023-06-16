package gov.epa.run_from_java.scripts.DSSTOX_Loading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.CDL;
import org.openscience.cdk.exception.CDKException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.ExcelCreator;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.util.StructureImageUtil;
import gov.epa.util.StructureUtil;

/**
 * @author TMARTI02
 */
public class DSSTOX_Qsar_Ready_Script {


	static class DSSTOX_QSAR_Ready {

		String dtxcid;
		String dtxsid;

		String original_smiles;
		String smiles;
		String canonical_qsarr;

		String InChI_Key_QSARr;
		String inchi_code_qsarr;
		String Number_of_connected_components;
		String salt_solvent;
		String salt_solvent_id;

		String filename;
		String software_version;
		String created_by;
		String created_at;

		String insert_dt;
		String updated_at;
		String updated_by;

	}

	
	void randomlySampleResults() {
		
		
		try {
			String folder="data/dsstox/tsv/";
			String filename="snapshot_compounds_curated_QSARready_Summary_file_final.csv";
			InputStream inputStream = new FileInputStream(folder+filename);
			String csvAsString = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
			String json = CDL.toJSONArray(csvAsString).toString();
			
			System.out.println("Done loading results file");
			
			JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);
			
			Random rn = new Random(42);
			
			JsonArray jaSample=new JsonArray();
			
			int count=5000;
			
			while (jaSample.size()<count) {
				jaSample.add(ja.get(rn.nextInt(ja.size())));
				if(jaSample.size()%100==0) {
//					System.out.println(jaSample.size());
				}
			}
			
			XSSFWorkbook workbook = new XSSFWorkbook();
			
			writeRows(workbook, "sample", jaSample);
			
			String fileNameOut="QSARready_sample.xlsx";

			File Folder=new File(folder);
			Folder.mkdirs();


			System.out.println(folder+fileNameOut);

			FileOutputStream saveExcel = new FileOutputStream(folder+fileNameOut);

			workbook.write(saveExcel);
			workbook.close();
			
			
		
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	
	private static void writeRows(Workbook workbook,String sheetName,JsonArray jaResults) throws IOException, CDKException {

		Sheet sheet=workbook.createSheet(sheetName);

		Row row1 = sheet.createRow(0);

		row1.createCell(0).setCellValue("Original_DTXSID");
		row1.createCell(1).setCellValue("Original_SMILES");
		row1.createCell(2).setCellValue("Canonical_QSARr");

		
		for (int i=1;i<=6;i++) {
			sheet.setColumnWidth(i, 60*256);	
		}
		

		int irow=0;
		
		
		for (int i = 0; i < jaResults.size(); i++) {

			JsonObject jo = jaResults.get(i).getAsJsonObject();

			String Original_DTXSID=jo.get("Original_DTXSID").getAsString();
			String Original_SMILES = jo.get("Original_SMILES").getAsString();
			String Canonical_QSARr = jo.get("Canonical_QSARr").getAsString();
			
			
			try {
				String inchiKeyOriginal=StructureUtil.toInchiIndigo(Original_SMILES).inchiKey;
				String inchiKeyOPERA=StructureUtil.toInchiIndigo(Canonical_QSARr).inchiKey;

				
				if(inchiKeyOriginal.equals(inchiKeyOPERA)) {
					continue;
				}

				
				Row rowi = sheet.createRow(++irow);
				rowi.createCell(0).setCellValue(Original_DTXSID);
				rowi.createCell(1).setCellValue(Original_SMILES);
				rowi.createCell(2).setCellValue(Canonical_QSARr);
				rowi.setHeight((short)2000);

				ExcelCreator.createImage(Original_SMILES, irow, 1, sheet, 1);
				ExcelCreator.createImage(Canonical_QSARr, irow, 2, sheet, 1);
				rowi.setHeight((short)(2000*1.15));//add some space for smiles at bottom
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}
	
	
	
	
	
	public void loadQSAR_Ready() {
		try {

			String software_version="OPERA 2.8";
			String lanId="tmarti02";

			String filename="snapshot_compounds_curated_QSARready_Summary_file_final.csv";
			InputStream inputStream = new FileInputStream("data/dsstox/tsv/"+filename);
			String csvAsString = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
			String json = CDL.toJSONArray(csvAsString).toString();

			JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);

			System.out.println("Number of records in csv:"+ja.size());

			List<DSSTOX_QSAR_Ready>recs=new ArrayList<>();


			for (int i=0;i<ja.size();i++) {
				//				for (int i=0;i<1;i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();

				//					System.out.println(Utilities.gson.toJson(jo));

				DSSTOX_QSAR_Ready rec=new DSSTOX_QSAR_Ready();

				rec.dtxcid=jo.get("Original_DTXCID").getAsString();
				rec.dtxsid=jo.get("Original_DTXSID").getAsString();
				rec.filename=filename;
				rec.created_by=lanId;

				rec.original_smiles=jo.get("Original_SMILES").getAsString();
				rec.canonical_qsarr=jo.get("Canonical_QSARr").getAsString();
				//					rec.smiles="";

				rec.InChI_Key_QSARr=jo.get("InChI Key_QSARr").getAsString();
				rec.inchi_code_qsarr=jo.get("InChI_Code_QSARr").getAsString();
				rec.Number_of_connected_components=jo.get("Number of connected components").getAsString();
				rec.salt_solvent=jo.get("Salt_Solvent").getAsString();
				rec.salt_solvent_id=jo.get("Salt_Solvent_ID").getAsString();

				rec.software_version=software_version;
				recs.add(rec);



			}
			createSQL(recs);


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void compareQSARReady () {
		
		Connection conn=SqlUtilities.getConnectionDSSTOX();
		
		int limit=100000;
		
		String sql="select software_version, original_smiles, canonical_qsarr, "
				+ "dtxsid, dtxcid from dsstox_qsar order by dtxcid, software_version limit "+limit;
		
		ResultSet rs=SqlUtilities.runSQL2(conn, sql);
		
		Hashtable<String,DSSTOX_QSAR_Ready>htNew=new Hashtable<>();
		Hashtable<String,DSSTOX_QSAR_Ready>htOld=new Hashtable<>();
		
		try {
			while (rs.next()) {
				DSSTOX_QSAR_Ready d=new DSSTOX_QSAR_Ready();
				d.software_version=rs.getString(1);
				d.original_smiles=rs.getString(2);
				d.canonical_qsarr=rs.getString(3);
				d.dtxsid=rs.getString(4);
				d.dtxcid=rs.getString(5);
				
				if(d.software_version.equals("OPERA")) {
					htOld.put(d.dtxcid, d);
				} else {
					htNew.put(d.dtxcid, d);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		for (String dtxcid:htNew.keySet()) {
			
			DSSTOX_QSAR_Ready dNew=htNew.get(dtxcid);
			
			if(htOld.get(dtxcid)==null) continue;
			
			DSSTOX_QSAR_Ready dOld=htOld.get(dtxcid);
			
//			if(!dNew.original_smiles.equals(dOld.original_smiles)) {
//				System.out.println("original smiles mismatch\t"+dtxcid+"\t"+dNew.original_smiles+"\t"+dOld.original_smiles);
//				continue;
//			}
			
			if(!dNew.original_smiles.equals(dOld.original_smiles)) continue;
			
			if(!dNew.canonical_qsarr.equals(dOld.canonical_qsarr)) {
				System.out.println("qsar smiles mismatch\t"+dtxcid+"\t"+dNew.canonical_qsarr+"\t"+dOld.canonical_qsarr);
			}

		}
		
		
	}
	
	public void createSQL (List<DSSTOX_QSAR_Ready> recs) {

		Connection conn=SqlUtilities.getConnectionDSSTOX();

		String [] fieldNames= {"dtxsid","dtxcid",
				"original_smiles","smiles", "canonical_qsarr",
				"InChI_Key_QSARr","inchi_code_qsarr",
				"Number_of_connected_components","salt_solvent","salt_solvent_id",
				"filename","software_version","created_by","created_at"};
		int batchSize=1000;

		String sql="INSERT INTO dsstox_qsar (";

		for (int i=0;i<fieldNames.length;i++) {

			if (fieldNames[i].contains(" ")) {
				sql+="\""+fieldNames[i]+"\"";	
			} else {
				sql+=fieldNames[i];
			}

			if (i<fieldNames.length-1)sql+=",";
			else sql+=") VALUES (";
		}

		for (int i=0;i<fieldNames.length-1;i++) {
			sql+="?";
			if (i<fieldNames.length-1)sql+=",";			 		
		}
		sql+="current_timestamp)";	
		System.out.println(sql);

		try {
			conn.setAutoCommit(false);
			PreparedStatement prep = conn.prepareStatement(sql);

			long t1=System.currentTimeMillis();

			for (int counter = 0; counter < recs.size(); counter++) {
				DSSTOX_QSAR_Ready rec=recs.get(counter);
				prep.setString(1, rec.dtxsid);
				prep.setString(2, rec.dtxcid);
				prep.setString(3, rec.original_smiles);
				prep.setString(4, rec.smiles);
				prep.setString(5, rec.canonical_qsarr);
				prep.setString(6, rec.InChI_Key_QSARr);
				prep.setString(7, rec.inchi_code_qsarr);
				prep.setString(8, rec.Number_of_connected_components);
				prep.setString(9, rec.salt_solvent);
				prep.setString(10, rec.salt_solvent_id);
				prep.setString(11, rec.filename);
				prep.setString(12, rec.software_version);
				prep.setString(13, rec.created_by);

				prep.addBatch();

				if (counter % batchSize == 0 && counter!=0) {
					System.out.println(counter);
					prep.executeBatch();
				}
			}

			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+recs.size()+" names using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
			//			conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		DSSTOX_Qsar_Ready_Script d=new DSSTOX_Qsar_Ready_Script();
//		d.loadQSAR_Ready();
//		d.randomlySampleResults();
		d.compareQSARReady();
	}
}
