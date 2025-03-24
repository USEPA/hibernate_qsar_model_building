package gov.epa.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;



/**
 * Class to read data sources provided as a single, column-based Excel spreadsheet
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class ExcelSourceReader {
	public String sourceName;
	public static String lastUpdated;

	private String sourceFolderPath;
	private String fileName;
	public Sheet sheet;
	
	public ExcelSourceReader() {
		
	}
	
	
	public ExcelSourceReader(String filePath) {
		
		this.lastUpdated = getStringCreationDate(filePath); // TODO add lastUpdated as parameter instead?
		try {
			FileInputStream fis = new FileInputStream(new File(filePath));
			Workbook wb = WorkbookFactory.create(fis);
			sheet = wb.getSheetAt(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Initializes a new reader for the given source from the given filename
	 * NOTE: Currently can only read a single sheet from a single file
	 * @param fileName		The file to read records from
	 * @param sourceName	The data source to assign records to
	 */
	public ExcelSourceReader(String fileName, String sourceName) {
		this.sourceName = sourceName;
		this.fileName = fileName;
		sourceFolderPath = "data" + File.separator + "experimental" + File.separator + sourceName;
		
		String filePath = sourceFolderPath + File.separator + "excel files" + File.separator + fileName;
		this.lastUpdated = getStringCreationDate(filePath); // TODO add lastUpdated as parameter instead?
		try {
			FileInputStream fis = new FileInputStream(new File(filePath));
			Workbook wb = WorkbookFactory.create(fis);
			sheet = wb.getSheetAt(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initializes a new reader for the given source from the given filename
	 * NOTE: Currently can only read a single sheet from a single file
	 * @param fileName		The file to read records from
	 * @param sourceName	The data source to assign records to
	 */
	public ExcelSourceReader(String fileName, String mainFolderPath, String sourceName) {
		this.sourceName = sourceName;
		this.fileName = fileName;
		
		sourceFolderPath = mainFolderPath + File.separator + sourceName;
		
		String filePath = sourceFolderPath + File.separator + "excel files" + File.separator + fileName;
		
		System.out.println(filePath);
		
		this.lastUpdated = getStringCreationDate(filePath); // TODO add lastUpdated as parameter instead?
		try {
			FileInputStream fis = new FileInputStream(new File(filePath));
			Workbook wb = WorkbookFactory.create(fis);
			sheet = wb.getSheetAt(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initializes a new reader for the given source from the given filename
	 * NOTE: Currently can only read a single sheet from a single file
	 * @param fileName		The file to read records from
	 * @param sourceName	The data source to assign records to
	 */
	public ExcelSourceReader(String fileName, String mainFolderPath, String sourceName,String sheetName) {
		this.sourceName = sourceName;
		this.fileName = fileName;
		
		sourceFolderPath = mainFolderPath + File.separator + sourceName;
		
		String filePath = sourceFolderPath + File.separator + "excel files" + File.separator + fileName;
		
		System.out.println(filePath);
		
		this.lastUpdated = getStringCreationDate(filePath); // TODO add lastUpdated as parameter instead?
		try {
			FileInputStream fis = new FileInputStream(new File(filePath));
			Workbook wb = WorkbookFactory.create(fis);
			sheet = wb.getSheet(sheetName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	// Gets the creation date of any file as a string
	public static String getStringCreationDate(String filepath) {
		Path path = Paths.get(filepath);
		try {
			BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
			FileTime createdAt = attrs.creationTime();
			Date creationDate = new Date(createdAt.toMillis());
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");  
			String strCreationDate=formatter.format(creationDate);
			return strCreationDate;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Initializes a new reader for the given source from the given filename
	 * NOTE: Currently can only read a single sheet from a single file
	 * @param fileName		The file to read records from
	 * @param sourceName	The data source to assign records to
	 */
	public void getSheet(String excelFilePath, int sheetNum) {
		
		lastUpdated = getStringCreationDate(excelFilePath); // TODO add lastUpdated as parameter instead?
		try {
			FileInputStream fis = new FileInputStream(new File(excelFilePath));
			Workbook wb = WorkbookFactory.create(fis);
			sheet=wb.getSheetAt(sheetNum);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
//	/**
//	 * Writes records from a spreadsheet to JSON original records format consistent with field names of an existing Record[SourceName] class
//	 * @param hmFieldNames	Matches column numbers to output fields of a Record[SourceName] class
//	 * @param chemicalNameIndex		Column index containing chemical names (for special escape character treatment)
//	 */
//	public Vector<JsonObject> parseRecordsFromExcel(HashMap<Integer,String> hmFieldNames, int chemicalNameIndex) {
//		Vector<JsonObject> records = new Vector<JsonObject>();
//		try {
//			int numRows = sheet.getLastRowNum();
//			for (int i = 1; i <= numRows; i++) {
//				Row row = sheet.getRow(i);
//				if (row==null) { continue; }
//				JsonObject jo = new JsonObject();
//				boolean hasAnyFields = false;
//				for (int k:hmFieldNames.keySet()) {
//					Cell cell = row.getCell(k);
//					if (cell==null) { continue; }
//					cell.setCellType(CELL_TYPE_STRING);
//					String content = "";
//					if (k==chemicalNameIndex) {
//						content = StringEscapeUtils.escapeHtml4(row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
//					} else {
//						content = row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
//					}
////					Hyperlink url  = row.getCell(k).getHyperlink();
////					if (url!=null) {
////						System.out.println(hmFieldNames.get(k)+"_url"+"\t"+url.getLabel());
////						jo.addProperty(hmFieldNames.get(k)+"_url", url.getAddress());
////					}
//					
//					if (content!=null && !content.isBlank()) { hasAnyFields = true; }
//					jo.addProperty(hmFieldNames.get(k), content);
//				}
//				if (hasAnyFields) { records.add(jo); }
//			}
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		return records;
//	}

	
	/**
	 * Writes records from a spreadsheet to JSON original records format consistent with field names of an existing Record[SourceName] class
	 * @param hmFieldNames	Matches column numbers to output fields of a Record[SourceName] class
	 * @param chemicalNameIndex		Column index containing chemical names (for special escape character treatment)
	 */
	public static JsonArray parseRecordsFromExcel(Sheet sheet, HashMap<Integer,String> hmFieldNames) {
		JsonArray records = new JsonArray();
		try {
			int numRows = sheet.getLastRowNum();
			for (int i = 1; i <= numRows; i++) {
				Row row = sheet.getRow(i);
				if (row==null) { continue; }
				JsonObject jo = new JsonObject();
				boolean hasAnyFields = false;
				for (int k:hmFieldNames.keySet()) {
					Cell cell = row.getCell(k);
					if (cell==null) { continue; }
					
					String content = "";

					try {
						
						CellType type = cell.getCellType();
	                    if (type == CellType.STRING) {
	                    	content=cell.getStringCellValue();
	                    } else if (type == CellType.NUMERIC) {
	                    	content=cell.getNumericCellValue()+"";
	                    } else if (type == CellType.BOOLEAN) {
	                    	content=cell.getBooleanCellValue()+"";		                    	
	                    } else if (type == CellType.BLANK) {
	                    	content="";
	                    }
						
						
//						content = row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
					} catch (Exception ex) {
						System.out.println(hmFieldNames.get(k)+"\t"+ex.getMessage());
					}
					
					
//					if (k==chemicalNameIndex) {
//						content = StringEscapeUtils.escapeHtml4(row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
//					} else {
//						content = row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
//					}
					
//					content = row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
					
					if (content!=null && !content.isBlank()) { hasAnyFields = true; }
					jo.addProperty(hmFieldNames.get(k), content);
				}
				if (hasAnyFields) { records.add(jo); }
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}

	
	
	/**
	 * Writes records from a spreadsheet to JSON original records format consistent with field names of an existing Record[SourceName] class
	 * @param hmFieldNames	Matches column numbers to output fields of a Record[SourceName] class
	 * @param chemicalNameIndex		Column index containing chemical names (for special escape character treatment)
	 */
	public static JsonArray parseRecordsFromExcel(Sheet sheet) {
		JsonArray records = new JsonArray();
		try {
			int numRows = sheet.getLastRowNum();
			
			Row row0 = sheet.getRow(0);
			
			HashMap<Integer,String> hmFieldNames = new HashMap<Integer,String>();
			
			for (int i=0;i<row0.getLastCellNum();i++) {
				Cell celli=row0.getCell(i);
				String colName=celli.getStringCellValue();
				hmFieldNames.put(i,colName);
				System.out.println(i+"\t"+colName);
			}
			
			
			for (int i = 1; i <= numRows; i++) {
				Row row = sheet.getRow(i);
				if (row==null) { continue; }
				JsonObject jo = new JsonObject();
				boolean hasAnyFields = false;
				for (int k:hmFieldNames.keySet()) {
					Cell cell = row.getCell(k);
					if (cell==null) { continue; }
					
					
					String content = "";
					
					FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
					CellValue cellValue = evaluator.evaluate(cell);

					try {
						
						CellType type = cell.getCellType();
	                    if (type == CellType.STRING) {
	                    	content=cellValue.getStringValue();
	                    } else if (type == CellType.NUMERIC) {
	                    	content=cellValue.getNumberValue()+"";
	                    } else if (type == CellType.BOOLEAN) {
	                    	content=cellValue.getBooleanValue()+"";		                    	
	                    } else if (type == CellType.BLANK) {
	                    	content="";	                    
	                    }
	                    
//	                    if (hmFieldNames.get(k).equals("Median % activity")) {	                    	
//	                    	System.out.println(hmFieldNames.get(k)+"\t"+content+"\t"+cellValue.getCellType());	
//	                    }
	                    
//						content = row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
					} catch (Exception ex) {
						System.out.println(hmFieldNames.get(k)+"\t"+ex.getMessage());
					}
					
					//					if (k==chemicalNameIndex) {
//						content = StringEscapeUtils.escapeHtml4(row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
//					} else {
//						content = row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
//					}
					
					if(content.isBlank()) content=null;
					
					if (content!=null && !content.isBlank()) { hasAnyFields = true; }
					jo.addProperty(hmFieldNames.get(k), content);
				}
				if (hasAnyFields) { records.add(jo); }
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}
	
	
	private String getContent(boolean setBlankToNull, FormulaEvaluator evaluator, Cell cell) {

		CellType type = cell.getCellType();
		
		String content=null;
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
		} else {
			System.out.println(type);
		}
		return content;
	}
	
	/**
	 * Writes records from a spreadsheet to JSON original records format consistent
	 * with field names of an existing Record[SourceName] class
	 * 
	 * @param hmFieldNames      Matches column numbers to output fields of a
	 *                          Record[SourceName] class
	 * @param chemicalNameIndex Column index containing chemical names (for special
	 *                          escape character treatment)
	 */
	public JsonArray parseRecordsFromExcel(String filepathExcel, int tabNum, int headerRowNum, boolean setBlankToNull) {
		
		try {
			FileInputStream fis = new FileInputStream(new File(filepathExcel));
			Workbook wb = WorkbookFactory.create(fis);
			sheet = wb.getSheetAt(tabNum);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		String[] fieldNames = getHeaders(headerRowNum);
		HashMap<Integer,String> hmFieldNames = generateDefaultMap(fieldNames, 0);
		
		JsonArray records = new JsonArray();
		FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();

		int numRows = sheet.getLastRowNum();
		
		for (int i = headerRowNum+1; i <= numRows; i++) {
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
					content = getContent(setBlankToNull, evaluator, cell);
					if(content!=null && content.contentEquals("filtered out")) content=null;

				} catch (Exception ex) {
					ex.printStackTrace();
					System.out.println("Error parsing for col " + k + "\tfor row " + i);
				}

				// if(content.contains("Cadmium sulphate")) System.out.println("here1:
				// "+content);

				if (content != null && !content.isBlank()) {
					hasAnyFields = true;
					content=content.trim();
				}
				
				if(content!=null && content.isBlank() && setBlankToNull) content=null;
				
				jo.addProperty(hmFieldNames.get(k), content);
				
			}
			
			jo.addProperty("lastUpdated", lastUpdated);

			
			if (hasAnyFields) {
				records.add(jo);
			}
		}
		return records;
	}

	
	/**
	 * Writes records from a spreadsheet to JSON original records format consistent with field names of an existing Record[SourceName] class
	 * @param hmFieldNames	Matches column numbers to output fields of a Record[SourceName] class
	 * @param chemicalNameIndex		Column index containing chemical names (for special escape character treatment)
	 */
	public JsonArray parseRecordsFromExcel(HashMap<Integer,String> hmFieldNames, int chemicalNameIndex,boolean setBlankToNull) {
		JsonArray records = new JsonArray();

		int numRows = sheet.getLastRowNum();
		for (int i = 1; i <= numRows; i++) {
			Row row = sheet.getRow(i);
			if (row==null) { continue; }
			JsonObject jo = new JsonObject();
			boolean hasAnyFields = false;
			for (int k:hmFieldNames.keySet()) {
				Cell cell = row.getCell(k);
				if (cell==null) { continue; }
				//					cell.setCellType(CELL_TYPE_STRING);

				String content = "";

				try {
					if (k==chemicalNameIndex) {
						content = StringEscapeUtils.escapeHtml4(row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());						
					} else {													
						CellType type = cell.getCellType();
						if (type == CellType.STRING) {
							content=cell.getStringCellValue();
						} else if (type == CellType.NUMERIC) {
							content=cell.getNumericCellValue()+"";
						} else if (type == CellType.BOOLEAN) {
							content=cell.getBooleanCellValue()+"";		                    	
						} else if (type == CellType.BLANK) {
							content="";
							if (setBlankToNull)	content=null;
						}

					}

				} catch (Exception ex) {
					System.out.println("Error parsing for col "+k+"\tfor row "+i);
				}

				//					if(content.contains("Cadmium sulphate")) System.out.println("here1: "+content);


				if (content!=null && !content.isBlank()) { hasAnyFields = true; }
				jo.addProperty(hmFieldNames.get(k), content);
			}
			if (hasAnyFields) { records.add(jo); }
		}
		return records;
	}
	
	/**
	 * Writes records from a spreadsheet to JSON original records format consistent with field names of an existing Record[SourceName] class
	 * @param hmFieldNames	Matches column numbers to output fields of a Record[SourceName] class
	 * @param chemicalNameIndex		Column index containing chemical names (for special escape character treatment)
	 */
	public JsonArray parseRecordsFromExcel(HashMap<Integer,String> hmFieldNames, int chemicalNameIndex) {
		return parseRecordsFromExcel(hmFieldNames, chemicalNameIndex, true);//TODO is this desired default behavior? set blanks to null?
	}
	
		
	public static String fixSpecialChars(String content) {
//		if(content.contains("(second CAS# 31119-53-6)")) System.out.println("here1:"+"\t"+fieldName+"\t"+content);
		if (content==null) return content;
		content=content.replace("\r"," ").replace("\n"," ");
		while (content.contains("  ")) {
			content=content.replace("  ", " ");
		}
		return content;
	}
	

	
	/**
	 * Writes records from a spreadsheet to JSON original records format assuming the template created by generateRecordClassTemplate()
	 * @param chemicalNameIndex		Column index containing chemical names (for special escape character treatment)
	 */
	public JsonArray parseRecordsFromExcel(int chemicalNameIndex) {
		String[] fieldNames = getHeaders(0);
		HashMap<Integer,String> hm = generateDefaultMap(fieldNames, 0);
		return parseRecordsFromExcel(hm, chemicalNameIndex,false);
	}
	
	public JsonArray parseRecordsFromExcel(int chemicalNameIndex,boolean setBlankToNull) {
		String[] fieldNames = getHeaders(0);
		HashMap<Integer,String> hm = generateDefaultMap(fieldNames, 0);
		return parseRecordsFromExcel(hm, chemicalNameIndex,setBlankToNull);
	}


	/**
	 * Gets column headers in appropriate format for field naming (alphanumeric and _ only)
	 * @return	Formatted column headers as a string array
	 */
	public String[] getHeaders(int headerRowNum) {
		Row headerRow = sheet.getRow(headerRowNum);
		int numHeaders = headerRow.getLastCellNum();
		String[] headers = new String[numHeaders];
		for (int i = 0; i < numHeaders; i++) {
			Cell headerCell = headerRow.getCell(i, MissingCellPolicy.CREATE_NULL_AS_BLANK);
			
//			System.out.println(headerCell.getStringCellValue());
			
//			headerCell.setCellType(CELL_TYPE_STRING);
			String headerContent = headerCell.getStringCellValue().trim().replaceAll("[^\\p{Alnum}]+", "_").replaceAll("^_", "").replaceAll("_$", "");
			if (headerContent==null || headerContent.equals("_") || headerContent.equals("")) {
				headers[i] = "field" + i;
			} else {
				headers[i] = headerContent;
			}
		}
		return headers;
	}

	/**
	 * Generates a default map from column number to field name, i.e. field names in same order as columns and none skipped
	 * Offset allows skipping blank columns at beginning of sheet
	 * @param fieldNames	The field names of the Record[SourceName] class
	 * @param offset		The number of blank columns at the beginning of the sheet
	 * @return				A map from column number to field names
	 */
	public static HashMap<Integer,String> generateDefaultMap(String[] fieldNames, int offset) {
		HashMap<Integer,String> hmFieldNames = new HashMap<Integer,String>();
		for (int i = 0; i < fieldNames.length; i++) {
			hmFieldNames.put(i + offset, fieldNames[i]);
		}
		return hmFieldNames;
	}

	

	public static void main(String[] args) {
	}

}
