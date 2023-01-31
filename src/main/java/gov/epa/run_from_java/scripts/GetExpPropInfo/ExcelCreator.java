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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ExcelCreator {

	
	public static JsonArray convertExcelToJsonArray(String filepath,int rowNumHeader) {

		JsonArray ja=new JsonArray();
		try {

			FileInputStream inputStream;

			inputStream = new FileInputStream(new File(filepath));

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheet("Records");

			List<String>fields=new ArrayList<>();

			Row rowHeader=sheet.getRow(rowNumHeader);	        

			for (int i=0;i<rowHeader.getLastCellNum();i++) {
				String fieldName=rowHeader.getCell(i).getStringCellValue();
				//		        	System.out.println(fieldName);
				fields.add(fieldName);
				
//				System.out.println(fieldName);
			}

			for (int rowNum=rowNumHeader+1;rowNum<sheet.getLastRowNum();rowNum++) {
				JsonObject jo=new JsonObject();

				Row row=sheet.getRow(rowNum);

				if (row==null) break;

				for (int colNum=0;colNum<row.getLastCellNum();colNum++) {

					Cell cell=row.getCell(colNum);

					FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator(); 
					CellValue value=evaluator.evaluate(cell);
					
					if (value!=null) {
						String strValue=value.formatAsString().replace("\"", "");
						jo.addProperty(fields.get(colNum), strValue);
						
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
			
			Row recSubtotalRow = sheet.createRow(0);
			Row recHeaderRow = sheet.createRow(1);

			for (int i=0;i<fields.length;i++) {
				Cell cell=recHeaderRow.createCell(i);
				cell.setCellValue(fields[i]);
			}

			for (int i=0;i<fields.length;i++) {
				sheet.autoSizeColumn(i);
				//sheet.setColumnWidth(i, sheet.getColumnWidth(i)+20);
			}

			int rowNum=2;

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

			for (int i = 0; i < fields.length; i++) {
				String col = CellReference.convertNumToColString(i);
				String recSubtotal = "SUBTOTAL(3,"+col+"$3:"+col+"$"+(ja.size()+2)+")";
				recSubtotalRow.createCell(i).setCellFormula(recSubtotal);
			}

			String lastCol = CellReference.convertNumToColString(fields.length-1);
			sheet.setAutoFilter(CellRangeAddress.valueOf("A2:"+lastCol+ja.size()+2));
			sheet.createFreezePane(0, 2);


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
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return style;
	}
	
	public static Hashtable<String,String>getColumnDescriptions() {
		Hashtable<String,String>htDescriptions=new Hashtable<>();
		
		
		htDescriptions.put("exp_prop_id","raw property id number in our database");
		htDescriptions.put("canon_qsar_smiles","qsar_ready_smiles associated with the mapped smiles");
		htDescriptions.put("page_url","url that the property value is associated with");
		htDescriptions.put("source_url","main url for the source");
		htDescriptions.put("source_doi","doi url for the source");
		htDescriptions.put("source_name","name of the source");
		htDescriptions.put("source_description","description of the source");
		htDescriptions.put("source_type","type of the source (values our team scraped or from ChemProp)");
		htDescriptions.put("source_authors","authors of journal article");
		htDescriptions.put("source_title","title of a journal article");
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
		htDescriptions.put("value_max","Original maximum property value from the source");
		htDescriptions.put("value_min","Original minimum property value from the source");
		htDescriptions.put("value_point_estimate","Point estimate for the property value derived from value_original or value_max and value_min");
		htDescriptions.put("value_units","units for the value_point_estimate");
		htDescriptions.put("qsar_property_value","value_point_estimate converted to the qsar_property_units");
		htDescriptions.put("qsar_property_units","units for the qsar_property_value");
		htDescriptions.put("temperature_c","temperature at which the experiment was performed in C");
		htDescriptions.put("pressure_mmHg","pressure at which the experiment was performed in mmHg");
		htDescriptions.put("pH","pH at which the experiment was performed");
		htDescriptions.put("notes","notes on the record");
		htDescriptions.put("qc_flag","whether or not a quality control flag has been issued");

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

		
		return htDescriptions;
	}

}
