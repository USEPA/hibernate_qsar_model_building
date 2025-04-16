package gov.epa.run_from_java.scripts.GetExpPropInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.endpoints.reports.predictions.ExcelReports.ExcelPredictionReportGenerator;
import gov.epa.util.StructureImageUtil;

public class ExcelCreator {

	
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

	
	public static JsonArray convertExcelToJsonArray(String filepath,int rowNumHeader,String sheetName) {

		JsonArray ja=new JsonArray();
		try {

			FileInputStream inputStream;

			inputStream = new FileInputStream(new File(filepath));

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheet(sheetName);

			List<String>fields=new ArrayList<>();

			Row rowHeader=sheet.getRow(rowNumHeader);	        

			for (int i=0;i<rowHeader.getLastCellNum();i++) {
				String fieldName=rowHeader.getCell(i).getStringCellValue();
				//		        	System.out.println(fieldName);
				fields.add(fieldName);
				
//				System.out.println(fieldName);
			}

			for (int rowNum=rowNumHeader+1;rowNum<=sheet.getLastRowNum();rowNum++) {
				JsonObject jo=new JsonObject();

				Row row=sheet.getRow(rowNum);

				if (row==null) break;

				for (int colNum=0;colNum<row.getLastCellNum();colNum++) {

					try {

						Cell cell=row.getCell(colNum);

						FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator(); 
						CellValue value=evaluator.evaluate(cell);

						if (value!=null) {
							String strValue=value.formatAsString().replace("\"", "");
							jo.addProperty(fields.get(colNum), strValue);

						}

					} catch (Exception ex) {
						System.out.println(ex.getMessage()+"\tError evaluating formula for "+rowNum+"\t"+colNum);
					}
				}
				ja.add(jo);
			}


			workbook.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ja;
	}
	
	/**
	 *	TODO- this might run out of memory
	 *
	 * Create excel file using data in JsonArray with fields in specified order
	 * 
	 * @param ja
	 * @param excelFilePath
	 * @param fields
	 */
	public static void createExcel2(JsonArray ja,String excelFilePath,String []fields,Hashtable<String,String>htDescriptions) {
		try {

			Workbook wb = new XSSFWorkbook();

			CellStyle styleURL=createStyleURL(wb);			

			if(htDescriptions!=null)
				createDescriptionsTab(wb,htDescriptions,fields);
			
			
			Sheet sheet = wb.createSheet("Records");
			

			Row recSubtotalRow = sheet.createRow(0);
			Row recHeaderRow = sheet.createRow(1);

			CellStyle csLtBlue=wb.createCellStyle();
			csLtBlue.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		    csLtBlue.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		    
			CellStyle csLtGreen=wb.createCellStyle();
			csLtGreen.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		    csLtGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CellStyle csLtGrey=createStyleHeader(wb);

			CellStyle csYellow=wb.createCellStyle();
			csYellow.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		    csYellow.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		    
		    
			for (int i=0;i<fields.length;i++) {
				Cell cell=recHeaderRow.createCell(i);
				cell.setCellValue(fields[i]);
				
				if (fields[i].contains("ICF")) {
					cell.setCellStyle(csLtGrey);
				} else if(fields[i].contains("mapped_")) {
					cell.setCellStyle(csLtBlue);
				} else if (fields[i].contains("source_")) {
					cell.setCellStyle(csLtGreen);
				}
			}

			for (int i=0;i<fields.length;i++) {
				sheet.autoSizeColumn(i);
				//				sheet.setColumnWidth(i, sheet.getColumnWidth(i)+20);
			}

			int rowNum=2;

			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();

				Row row=sheet.createRow(rowNum++);	

				for (int k=0;k<fields.length;k++) {
					Cell cell=row.createCell(k);

					if(jo.get("predicted_CV")!=null) {
						cell.setCellStyle(csYellow);
					}

					if (jo.get(fields[k])==null) continue;
					if (jo.get(fields[k]).isJsonNull()) continue;
					if (jo.get(fields[k]).getAsString().isBlank()) continue;
					

					String value=jo.get(fields[k]).getAsString();

					//					try {
						//						//TODO have better way to decide if integer, double, or string...
					//						
					//						if (fields[k].equals("qsar_property_value") || fields[k].contains("mol_weight")
					//								|| fields[k].contains("value_m") || fields[k].contains("value_point_estimate") 
					//								|| fields[k].toLowerCase().contains("temperature") || fields[k].toLowerCase().contains("pressure") || fields[k].equals("pH")) {
					//							cell.setCellValue( Double.parseDouble(value));								
					//						} else if ((fields[k].contains("_id") || fields[k].contains("id_")) && !fields[k].toLowerCase().contains("dtx")) {
					//							cell.setCellValue(Integer.parseInt(value));
					//						} else {
					//							cell.setCellValue(value);	
					//						}
					//						
					//					} catch (Exception ex) {
					//						System.out.println("Error setting cell value = "+value+" for "+fields[k]);
					//					}

					try {//Use brute force- first set as double then as string if that fails:
						cell.setCellValue(Double.parseDouble(value));	
					} catch (Exception ex) {
						//						System.out.println("Error setting cell value = "+value+" for "+fields[k]);
						cell.setCellValue(value);	
					}

					if (fields[k].contains("url")) {
						try {
							Hyperlink link = wb.getCreationHelper().createHyperlink(HyperlinkType.URL);
							link.setAddress(value);
							cell.setHyperlink(link);
							cell.setCellStyle(styleURL);
						} catch (Exception ex) {
							//							System.out.println(ex.getMessage());
						}
					}

				}
			}

			for (int i = 0; i < fields.length; i++) {
				String col = CellReference.convertNumToColString(i);
				String recSubtotal = "SUBTOTAL(3,"+col+"$3:"+col+"$"+(ja.size()+2)+")";
				recSubtotalRow.createCell(i).setCellFormula(recSubtotal);
			}


			String lastCol = CellReference.convertNumToColString(fields.length-1);
			sheet.setAutoFilter(CellRangeAddress.valueOf("A2:"+lastCol+ja.size()+2));
			sheet.createFreezePane(0, 2);



			OutputStream fos = new FileOutputStream(excelFilePath);
			wb.write(fos);
			wb.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	/**
	 *	This version doesnt set any colors
	 *
	 * Create excel file using data in JsonArray with fields in specified order
	 * 
	 * @param ja
	 * @param excelFilePath
	 * @param fields
	 */
	public static void createExcel3(JsonArray ja,String excelFilePath,String []fields,Hashtable<String,String>htDescriptions) {
		try {

			Workbook wb = new XSSFWorkbook();

			CellStyle styleURL=createStyleURL(wb);			

			if(htDescriptions!=null)
				createDescriptionsTab(wb,htDescriptions,fields);
			
			
			Sheet sheet = wb.createSheet("Records");
			
			Row recSubtotalRow = sheet.createRow(0);
			Row recHeaderRow = sheet.createRow(1);

//			CellStyle csLtBlue=wb.createCellStyle();
//			csLtBlue.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
//		    csLtBlue.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//		    
//			CellStyle csLtGreen=wb.createCellStyle();
//			csLtGreen.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
//		    csLtGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//
//			CellStyle csLtGrey=createStyleHeader(wb);
//
//			CellStyle csYellow=wb.createCellStyle();
//			csYellow.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
//		    csYellow.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		    
		    
			for (int i=0;i<fields.length;i++) {
				Cell cell=recHeaderRow.createCell(i);
				cell.setCellValue(fields[i]);
				
//				if (fields[i].contains("ICF")) {
//					cell.setCellStyle(csLtGrey);
//				} else if(fields[i].contains("mapped_")) {
//					cell.setCellStyle(csLtBlue);
//				} else if (fields[i].contains("source_")) {
//					cell.setCellStyle(csLtGreen);
//				}
			}

			for (int i=0;i<fields.length;i++) {
				sheet.autoSizeColumn(i);
				//				sheet.setColumnWidth(i, sheet.getColumnWidth(i)+20);
			}

			int rowNum=2;

			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();

				Row row=sheet.createRow(rowNum++);	

				for (int k=0;k<fields.length;k++) {
					Cell cell=row.createCell(k);

//					if(jo.get("predicted_CV")!=null) {
//						cell.setCellStyle(csYellow);
//					}

					if (jo.get(fields[k])==null) continue;
					if (jo.get(fields[k]).isJsonNull()) continue;
					if (jo.get(fields[k]).getAsString().isBlank()) continue;
					

					String value=jo.get(fields[k]).getAsString();

					try {//Use brute force- first set as double then as string if that fails:
						cell.setCellValue(Double.parseDouble(value));	
					} catch (Exception ex) {
						//						System.out.println("Error setting cell value = "+value+" for "+fields[k]);
						cell.setCellValue(value);	
					}

					if (fields[k].contains("url")) {
						try {
							Hyperlink link = wb.getCreationHelper().createHyperlink(HyperlinkType.URL);
							link.setAddress(value);
							cell.setHyperlink(link);
							cell.setCellStyle(styleURL);
						} catch (Exception ex) {
							//							System.out.println(ex.getMessage());
						}
					}

				}
			}

			for (int i = 0; i < fields.length; i++) {
				String col = CellReference.convertNumToColString(i);
				String recSubtotal = "SUBTOTAL(3,"+col+"$3:"+col+"$"+(ja.size()+2)+")";
				recSubtotalRow.createCell(i).setCellFormula(recSubtotal);
			}


			String lastCol = CellReference.convertNumToColString(fields.length-1);
			sheet.setAutoFilter(CellRangeAddress.valueOf("A2:"+lastCol+ja.size()+2));
			sheet.createFreezePane(0, 2);



			OutputStream fos = new FileOutputStream(excelFilePath);
			wb.write(fos);
			wb.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static void createDescriptionsTab(Workbook wb,Hashtable <String,String>htDesc,String []fieldnames) {

		CellStyle cs=createStyleHeader(wb);
				
		Sheet sheetDesc = wb.createSheet("Records field descriptions");			
		Row row0 = sheetDesc.createRow(0);
		Cell cell0=row0.createCell(0);
		cell0.setCellValue("Field");
		cell0.setCellStyle(cs);
		
		Cell cell1=row0.createCell(1);
		cell1.setCellValue("Description");
		cell1.setCellStyle(cs);
		
		Set<String>keys=htDesc.keySet();
		
		int rowNum=0;
		for (String fieldname:fieldnames) {
			rowNum++;
			
			Row row = sheetDesc.createRow(rowNum);
			cell0=row.createCell(0);
			cell0.setCellValue(fieldname);
			
			cell1=row.createCell(1);
			cell1.setCellValue(htDesc.get(fieldname));
			
		}
		
		sheetDesc.autoSizeColumn(0);
		sheetDesc.autoSizeColumn(1);
		ExcelPredictionReportGenerator.setAutofilter(sheetDesc);
		
	}
	
	/**
	 *	TODO- this might run out of memory
	 *
	 * Create excel file using data in JsonArray with fields in specified order
	 * 
	 * @param ja
	 * @param excelFilePath
	 * @param fields
	 */
	public static void addSheet(Workbook wb, String sheetName, JsonArray ja,String []fields,Hashtable<String,String>htDescriptions) {
		try {

			Sheet sheet=wb.createSheet(sheetName);

			CellStyle styleURL=createStyleURL(wb);			

			if(htDescriptions!=null)
				createDescriptionsTab(wb,htDescriptions,fields);
			
//			Row recSubtotalRow = sheet.createRow(0);
			Row recHeaderRow = sheet.createRow(0);

			for (int i=0;i<fields.length;i++) {
				Cell cell=recHeaderRow.createCell(i);
				cell.setCellValue(fields[i]);
			}

			for (int i=0;i<fields.length;i++) {
				sheet.autoSizeColumn(i);				
				sheet.setColumnWidth(i, (int)(sheet.getColumnWidth(i)*1.20));
				//sheet.setColumnWidth(i, sheet.getColumnWidth(i)+20);
			}

			int rowNum=1;

			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();

				Row row=sheet.createRow(rowNum++);	

				for (int k=0;k<fields.length;k++) {
					Cell cell=row.createCell(k);

					if (jo.get(fields[k])==null) continue;
					if (jo.get(fields[k]).isJsonNull()) continue;
					if (jo.get(fields[k]).getAsString().isBlank()) continue;

					String value=jo.get(fields[k]).getAsString();

					try {
						cell.setCellValue(Double.parseDouble(value));	
					} catch (Exception ex) {
						//						System.out.println("Error setting cell value = "+value+" for "+fields[k]);
						cell.setCellValue(value);	
					}

					if (fields[k].contains("url")) {
						try {
							Hyperlink link = wb.getCreationHelper().createHyperlink(HyperlinkType.URL);
							link.setAddress(value);
							cell.setHyperlink(link);
							cell.setCellStyle(styleURL);
						} catch (Exception ex) {
							//							System.out.println(ex.getMessage());
						}
					}

				}
			}

//			for (int i = 0; i < fields.length; i++) {
//				String col = CellReference.convertNumToColString(i);
//				String recSubtotal = "SUBTOTAL(3,"+col+"$3:"+col+"$"+(ja.size()+2)+")";
//				recSubtotalRow.createCell(i).setCellFormula(recSubtotal);
//			}

			String lastCol = CellReference.convertNumToColString(fields.length-1);

			sheet.createFreezePane(0, 1);

			if(ja.size()<100000) {//Causes excel to be damaged otherwise and have to have excel fix it on opening
				sheet.setAutoFilter(CellRangeAddress.valueOf("A1:"+lastCol+ja.size()+2));
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	
	
	static void createExcel(JsonArray ja,String excelFilePath) {
		try {

			Workbook wb = new XSSFWorkbook();

			CellStyle styleURL=createStyleURL(wb);			

			Sheet sheet = wb.createSheet("records");


			String[]  mainFields= {"canon_qsar_smiles","qsar_property_value","qsar_property_units","dtxcid","mol_weight"};

			//Sorted fields:
			String[] recordFields = { "exp_prop_id", "fk_public_source_id", "fk_literature_source_id",
					"fk_source_chemical_id",  "name","description", "type","url", "page_url",  "source_dtxrid",
					"source_dtxsid", "source_casrn", "source_chemical_name", "source_smiles", "notes", "authors", "doi",
					"title", "value_original", "value_qualifier", "value_max", "value_min", "value_point_estimate","units",
			"qc_flag" };



			Row row0=sheet.createRow(0);

			for (int i=0;i<mainFields.length;i++) {
				Cell cell=row0.createCell(i);
				cell.setCellValue(mainFields[i]);				
			}

			for (int i=0;i<recordFields.length;i++) {
				Cell cell=row0.createCell(i+mainFields.length);
				cell.setCellValue(recordFields[i]);				
			}

			int rowNum=1;

			for (int i=0;i<ja.size();i++) {
				JsonObject joMainRecord=ja.get(i).getAsJsonObject();
				JsonArray jaRecords=joMainRecord.get("Records").getAsJsonArray();

				for (int j=0;j<jaRecords.size();j++) {
					Row row=sheet.createRow(rowNum++);	

					JsonObject joRecord=jaRecords.get(j).getAsJsonObject();

					for (int k=0;k<mainFields.length;k++) {
						Cell cell=row.createCell(k);
						if (mainFields[k].equals("qsar_property_value") || mainFields[k].equals("mol_weight") ) {
							cell.setCellValue(Double.parseDouble(joMainRecord.get(mainFields[k]).getAsString()));								
						} else {
							cell.setCellValue(joMainRecord.get(mainFields[k]).getAsString());	
						}
					}

					for (int k=0;k<recordFields.length;k++) {
						Cell cell=row.createCell(k+mainFields.length);
						if (joRecord.get(recordFields[k])!=null) {

							String value=joRecord.get(recordFields[k]).getAsString();

							if (recordFields[k].contains("_id")) {
								cell.setCellValue(Integer.parseInt(value));
							} else if (recordFields[k].contains("value_point_estimate")) {
								cell.setCellValue(Double.parseDouble(value));
							} else {
								cell.setCellValue(value);	
							}
							if (recordFields[k].contains("url")) {
								try {
									Hyperlink link = wb.getCreationHelper().createHyperlink(HyperlinkType.URL);
									link.setAddress(value);
									cell.setHyperlink(link);
									cell.setCellStyle(styleURL);
								} catch (Exception ex) {
									//									System.out.println(ex.getMessage());
								}
							}

						}

					}

				}
			}


			OutputStream fos = new FileOutputStream(excelFilePath);
			wb.write(fos);
			wb.close();


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	static CellStyle createStyleURL(Workbook workbook) {
		CellStyle hlink_style = workbook.createCellStyle();
		Font hlink_font = workbook.createFont();
		hlink_font.setUnderline(Font.U_SINGLE);
		hlink_font.setColor(Font.COLOR_RED);
		hlink_style.setFont(hlink_font);
		return hlink_style;
	}
	
	static CellStyle createStyleHeader(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);
		style.setFont(font);	
//		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
//	    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return style;
	}
	
	public static Hashtable<String,String>getColumnDescriptions() {
		Hashtable<String,String>htDescriptions=new Hashtable<>();
		
		
		htDescriptions.put("exp_prop_id","raw property id number in our database");
		htDescriptions.put("exp_prop_id_previous_version","raw property id number from older version of the source");
		

		htDescriptions.put("canon_qsar_smiles","qsar_ready_smiles associated with the mapped smiles");
		htDescriptions.put("page_url","url that the property value is associated with");


		htDescriptions.put("public_source_name","name of the public source");
		htDescriptions.put("public_source_description","description of the public source");
		htDescriptions.put("public_source_url","url of the public source");

		htDescriptions.put("public_source_original_name","name of the original public source");
		htDescriptions.put("public_source_original_description","description of the original public source");
		htDescriptions.put("public_source_original_url","url of the original public source");
		
		htDescriptions.put("literature_source_citation","citation for the literature source");
		htDescriptions.put("literature_source_doi","doi url for the literature source");

		htDescriptions.put("experimental_median","Median experimental property value for modeling dataset");
		htDescriptions.put("predicted_CV","Prediction from random forest model during 5 fold cross validation");
		htDescriptions.put("predicted_CV_Error","Prediction error from random forest model during 5 fold cross validation");
		
		htDescriptions.put("source_dtxrid","DSSTOX record id with the source chemical");
		htDescriptions.put("source_dtxsid","DSSTOX substance id associated with the source chemical");
		htDescriptions.put("source_casrn","source chemical CASRN");
		htDescriptions.put("source_chemical_name","source chemical name");
		htDescriptions.put("source_smiles","source chemical SMILES");
		
		htDescriptions.put("mapped_dtxcid","DSSTOX compound id for the record mapped to the source chemical");
		htDescriptions.put("mapped_dtxsid","DSSTOX substance id for the record  mapped to the source chemical");
		htDescriptions.put("mapped_cas","DSSTOX CASRN  for the record mapped to the source chemical");
		htDescriptions.put("mapped_chemical_name","DSSTOX chemical name for the record mapped to the source chemical");
		htDescriptions.put("mapped_smiles","DSSTOX SMILES  for the record mapped to the source chemical");
		htDescriptions.put("mapped_molweight","DSSTOX molecular weight  for the record mapped to the source chemical");
		
		htDescriptions.put("value_original","Original property value from the source");
		htDescriptions.put("value_original_parsed","Original property value from the source that was parsed (sometimes there are multiple semicolon delimited values)");
		htDescriptions.put("value_max","Original maximum property value from the source");
		htDescriptions.put("value_min","Original minimum property value from the source");
		htDescriptions.put("value_point_estimate","Point estimate for the property value derived from value_original or value_max and value_min");
		htDescriptions.put("value_units","units for the value_point_estimate");
		htDescriptions.put("qsar_property_value","value_point_estimate converted to the qsar_property_units");
		htDescriptions.put("qsar_property_units","units for the qsar_property_value");
		
		htDescriptions.put("temperature_c","temperature at which the experiment was performed in C");
		htDescriptions.put("pressure_mmHg","pressure at which the experiment was performed in mmHg");
		htDescriptions.put("pH","pH at which the experiment was performed");
		
		htDescriptions.put("test_id","ECOTOX db test_id");
		htDescriptions.put("exposure_type","ECOTOX db exposure_type");
		htDescriptions.put("chem_analysis_method","ECOTOX db chem_analysis_method");
		htDescriptions.put("concentration_type","ECOTOX db concentration_type");		
		
		htDescriptions.put("notes","notes on the record");
		htDescriptions.put("qc_flag","whether or not a quality control flag has been issued");

		
		htDescriptions.put("ICF_Notes","Clarifying context for results or other description of any challenges encountered during QC review");
		htDescriptions.put("ICF_duplicate","Duplicate exp_prop_id");
		htDescriptions.put("ICF_chemical_matches","Does the chemical in the primary source match the source chemical (see source_casrn, source_chemical_name, and source_smiles) ?  Yes/No/?");
		htDescriptions.put("ICF_is_experimental","Was the property value experimentally determined? Yes/No/?");
		htDescriptions.put("ICF_source_url","URL/doi for the source used to validate the record");
		htDescriptions.put("ICF_source_type","For journal article/reports, Type of literature source used to validate: Primary/Secondary");
		htDescriptions.put("ICF_citation","For journal article/reports, full citation for the source");
		htDescriptions.put("ICF_property_value","Property value from the checking source converted to the same units as qsar_property_units field");
		htDescriptions.put("ICF_units_conversion_error","Was there a unit conversion error in recording the property value+units? E.g. the website says the experimental property is “P” but the record was saved as a “LogP” record with log units in “qsar_property_units”.");
		htDescriptions.put("ICF_temperature_c","Experimental temperature if it is different from temperature_c field. Especially important for vapor pressure records far away from 25 C");
		htDescriptions.put("ICF_pressure_mmHg","Experimental pressure if it is different from pressure_mmHg field. Especially important for boiling point records far away from 760 mmHg");
		htDescriptions.put("ICF_pH","Experimental pH if it is different from pH field. ");

		
		htDescriptions.put("ICF_cluster",
				"Matching numerical values assigned to entries that have the same mapped chemical identity (sorted by CAS-RN) and qsar_property_value. Defined as being within 0.02 of a qsar unit. Sub-clusters within 0.02 of a qsar unit are given an a, b, or c distinction. One additional cluster that was identified after review began is marked with \"x.\"");

		htDescriptions.put("ICF_status",
				"Entries that have been reviewed or are part of a reviewed cluster. \"Reviewed\" = Entry was reviewed and QCd to the extent possible."
						+ "\"Reviewed - could not confirm\" = Entry was not able to be reviewed, with explanation of steps taken provided in ICF_Notes."
						+ "\"Reviewed (duplicate)\" = Entry was a duplicate value for another reviewed entry.");

		htDescriptions.put("ICF_notes","Clarifying context for results or other description of any challenges encountered during QC review");
		htDescriptions.put("ICF_additional_resources_needed","For values where the primary source could be tentatively identified, but not retrieved, the reference information for that source is provided here.");
		htDescriptions.put("ICF_property_mismatch","If the values reported in the paper are not the property of interest, name the property reported. Otherwise, leave blank.");
		htDescriptions.put("ICF_corrected_chemical","Where the chemical did not match, but was clearly intended as the correct chemical (e.g., a conjugate base instead of the acid form), the DTXSID of the corrected chemical will be reported here.");
		htDescriptions.put("ICF_exp_method","Type of experimental method used to determine property value.");
		htDescriptions.put("ICF_test_guidelines","Test guidelines used to determine property value, where reported. Ex: \"OPPTS 830.7570\" or \"OECD 117\"");
		htDescriptions.put("ICF_reportedvalue","The original value reported in the primary source, where available");
		htDescriptions.put("ICF_reportedunits","The unit for the original value reported in the primary source, where available");

		
		
		return htDescriptions;
	}

}
