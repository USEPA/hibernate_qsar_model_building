package gov.epa.web_services.standardizers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

/**
* @author TMARTI02
*/
public class QsarReadyPaperSampleResults {

	
	void createSpreadsheetFromOperaResultsCSV(String filepathOperaOutput,String idField) {
		
		try {
		
			InputStream inputStream = new FileInputStream(filepathOperaOutput);
			BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
			String csvAsString = br.lines().collect(Collectors.joining("\n"));
			br.close();
			String json = CDL.toJSONArray(csvAsString).toString();
			JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);
			
			XSSFWorkbook workbook = new XSSFWorkbook();
			writeRows(workbook,"results",ja,idField,true);
			
			String outputPath=filepathOperaOutput.replace(".csv", ".xlsx");
			
			FileOutputStream saveExcel = new FileOutputStream(outputPath);
			workbook.write(saveExcel);
			workbook.close();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void writeRows(Workbook workbook,String sheetName,JsonArray ja,String idField,boolean addSaltImage) throws IOException, CDKException {


		Sheet sheet=workbook.createSheet(sheetName);

		Row row1 = sheet.createRow(0);
		row1.createCell(0).setCellValue(idField);
		row1.createCell(1).setCellValue("Original_SMILES");
		row1.createCell(2).setCellValue("OPERA_QSAR_READY");
		row1.createCell(3).setCellValue("Salt_Solvent");

		sheet.setColumnWidth(0, 20*256);
		sheet.setColumnWidth(1, 60*256);
		sheet.setColumnWidth(2, 60*256);
		if(addSaltImage) sheet.setColumnWidth(3, 60*256);

		for (int i = 0; i < ja.size(); i++) {

			JsonObject jo = ja.get(i).getAsJsonObject();

			int irow=(i+1);
			Row rowi = sheet.createRow(irow);


			String CID = jo.get(idField).getAsString();
			String Original_Smiles = jo.get("Original_SMILES").getAsString();
			String Salt_Solvent = jo.get("Salt_Solvent").getAsString();
			
			

			
			System.out.println(Original_Smiles);
			String QSAR_Ready_SMILES_OPERA = jo.get("Canonical_QSARr").getAsString();
			

			rowi.createCell(0).setCellValue(CID);
			rowi.createCell(1).setCellValue(Original_Smiles);
			rowi.createCell(2).setCellValue(QSAR_Ready_SMILES_OPERA);
			rowi.createCell(3).setCellValue(Salt_Solvent);
			
			rowi.setHeight((short)2000);

			createImage(Original_Smiles, irow, 1, sheet, 1);
			createImage(QSAR_Ready_SMILES_OPERA, irow, 2, sheet, 1);
			
			if(addSaltImage) createImage(Salt_Solvent, irow, 3, sheet, 1);
			
			rowi.setHeight((short)(2000*1.15));//add some space for smiles at bottom

		}

	}
	
	public static void createImage(String smiles, int startRow,int column,Sheet sheet, int rowspan) {

		Workbook wb=sheet.getWorkbook();
		if (smiles==null || smiles.equals("N/A") || smiles.contains("error")) return;		
		byte[] imageBytes=StructureImageUtil.generateImageBytesFromSmiles(smiles);		
		if (imageBytes==null) return;
		
		int pictureIdx = wb.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);

		//create an anchor with upper left cell column/startRow, only one cell anchor since bottom right depends on resizing
		CreationHelper helper = wb.getCreationHelper();
		ClientAnchor anchor = helper.createClientAnchor();
		anchor.setCol1(column);
		anchor.setRow1(startRow);

		//create a picture anchored to Col1 and Row1
		Drawing drawing = sheet.createDrawingPatriarch();
		Picture pict = drawing.createPicture(anchor, pictureIdx);

		//get the picture width in px
		int pictWidthPx = pict.getImageDimension().width;
		//get the picture height in px
		int pictHeightPx = pict.getImageDimension().height;

		//get column width of column in px
		float columnWidthPx = sheet.getColumnWidthInPixels(column);

		//get the heights of all merged rows in px
		float[] rowHeightsPx = new float[startRow+rowspan];
		float rowsHeightPx = 0f;
		for (int r = startRow; r < startRow+rowspan; r++) {
			Row row = sheet.getRow(r);
			float rowHeightPt = row.getHeightInPoints();
			rowHeightsPx[r-startRow] = rowHeightPt * Units.PIXEL_DPI / Units.POINT_DPI;
			rowsHeightPx += rowHeightsPx[r-startRow];
		}

		//calculate scale
		float scale = 1;
		if (pictHeightPx > rowsHeightPx) {
			float tmpscale = rowsHeightPx / (float)pictHeightPx;
			if (tmpscale < scale) scale = tmpscale;
		}
		if (pictWidthPx > columnWidthPx) {
			float tmpscale = columnWidthPx / (float)pictWidthPx;
			if (tmpscale < scale) scale = tmpscale;
		}

		//calculate the horizontal center position
		int horCenterPosPx = Math.round(columnWidthPx/2f - pictWidthPx*scale/2f);
		//set the horizontal center position as Dx1 of anchor


		anchor.setDx1(horCenterPosPx * Units.EMU_PER_PIXEL); //in unit EMU for XSSF


		//calculate the vertical center position
		int vertCenterPosPx = Math.round(rowsHeightPx/2f - pictHeightPx*scale/2f);
		//get Row1
		Integer row1 = null;
		rowsHeightPx = 0f;
		for (int r = 0; r < rowHeightsPx.length; r++) {
			float rowHeightPx = rowHeightsPx[r];
			if (rowsHeightPx + rowHeightPx > vertCenterPosPx) {
				row1 = r + startRow;
				break;
			}
			rowsHeightPx += rowHeightPx;
		}
		//set the vertical center position as Row1 plus Dy1 of anchor
		if (row1 != null) {
			anchor.setRow1(row1);
			if (wb instanceof XSSFWorkbook) {
				anchor.setDy1(Math.round(vertCenterPosPx - rowsHeightPx) * Units.EMU_PER_PIXEL); //in unit EMU for XSSF
			} else if (wb instanceof HSSFWorkbook) {
				//see https://stackoverflow.com/questions/48567203/apache-poi-xssfclientanchor-not-positioning-picture-with-respect-to-dx1-dy1-dx/48607117#48607117 for HSSF
				float DEFAULT_ROW_HEIGHT = 12.75f;
				anchor.setDy1(Math.round((vertCenterPosPx - rowsHeightPx) * Units.PIXEL_DPI / Units.POINT_DPI * 14.75f * DEFAULT_ROW_HEIGHT / rowHeightsPx[row1]));
			}
		}

		//set Col2 of anchor the same as Col1 as all is in one column
		anchor.setCol2(column);

		//calculate the horizontal end position of picture
		int horCenterEndPosPx = Math.round(horCenterPosPx + pictWidthPx*scale);
		//set set the horizontal end position as Dx2 of anchor

		anchor.setDx2(horCenterEndPosPx * Units.EMU_PER_PIXEL); //in unit EMU for XSSF

		//calculate the vertical end position of picture
		int vertCenterEndPosPx = Math.round(vertCenterPosPx + pictHeightPx*scale);
		//get Row2
		Integer row2 = null;
		rowsHeightPx = 0f;
		for (int r = 0; r < rowHeightsPx.length; r++) {
			float rowHeightPx = rowHeightsPx[r];
			if (rowsHeightPx + rowHeightPx > vertCenterEndPosPx) {
				row2 = r + startRow;
				break;
			}
			rowsHeightPx += rowHeightPx;
		}

		//set the vertical end position as Row2 plus Dy2 of anchor
		if (row2 != null) {
			anchor.setRow2(row2);
			anchor.setDy2(Math.round(vertCenterEndPosPx - rowsHeightPx) * Units.EMU_PER_PIXEL); //in unit EMU for XSSF
		}
	}
	
	
	public static void main(String[] args) {

		
		QsarReadyPaperSampleResults q=new QsarReadyPaperSampleResults();
		
		String folder="data/qsar ready/";
		String filename="sample qsar ready output for kamel paper.csv";
		q.createSpreadsheetFromOperaResultsCSV(folder+filename,"DSSTOX_COMPOUND_ID");
		
//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2023 qsar ready paper\\1000 output\\";
//		String filename="Sample_input_Summary_file.csv";
//		q.createSpreadsheetFromOperaResultsCSV(folder+filename,"Molecule name");
		
		
		

	}

}
