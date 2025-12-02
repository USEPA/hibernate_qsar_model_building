package gov.epa.run_from_java.data_loading;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
* @author TMARTI02
*/
public class JsonArrayToExcel {

	
	 private static final int MAX_COLUMN_WIDTH = 256 * 50;
	 private static final int COLUMN_WIDTH_BUFFER = 256 * 2; // Buffer width to account for filter drop-down


    // Collect all unique keys from the JSON array
    private static Set<String> collectAllKeys(JsonArray jsonArray) {
        Set<String> allKeys = new java.util.HashSet<>();
        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            allKeys.addAll(jsonObject.keySet());
        }
        return allKeys;
    }
    
    
    private static List<String> filterKeysWithData(JsonArray jsonArray, List<String> sortedKeys) {
        List<String> keysWithData = new ArrayList<>();
        for (String key : sortedKeys) {
            boolean hasData = false;
            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();
                JsonElement value = jsonObject.get(key);
                if (value != null && !value.isJsonNull()) {
                    hasData = true;
                    break;
                }
            }
            if (hasData) {
                keysWithData.add(key);
            }
        }
        return keysWithData;
    }
	
    
 // Determine if all values for a field are numerical
    //TODO should store as a hashtable so that it doesnt have to do this for every value
    private static boolean isNumericField(JsonArray jsonArray, String key) {
        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonElement value = jsonObject.get(key);
            if (value != null && !value.isJsonNull() && !value.isJsonPrimitive()) {
                return false;
            }
            if (value != null && value.isJsonPrimitive() && !value.getAsJsonPrimitive().isNumber()) {
                return false;
            }
        }
        return true;
    }
	
	public static void convertJsonArrayToExcel(JsonArray jsonArray,String filepathExcel) {
        // Create a new workbook and sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        // Create a header row
        
        Set<String> allKeys = collectAllKeys(jsonArray);
        List<String> sortedKeys = new ArrayList<>(allKeys);
        Collections.sort(sortedKeys);
        

     // Filter out keys with no data
        List<String> keysWithData = filterKeysWithData(jsonArray, sortedKeys);

        // Create a row for counts
        Row subtotalRow = sheet.createRow(0);
        int cellIndex = 0;
        for (String key : keysWithData) {
            Cell cell = subtotalRow.createCell(cellIndex++);
            String columnLetter = CellReference.convertNumToColString(cellIndex - 1);
            int startRow = 4; // Start from row 3 (first data row after headers)
            int endRow = jsonArray.size() + 3; // End at the last row of data
            String formula = String.format("SUBTOTAL(3, %s%d:%s%d)", columnLetter, startRow, columnLetter, endRow);
            cell.setCellFormula(formula);
        }

        // Add a blank row between counts and header
        sheet.createRow(1);

        // Create a header row
        Row headerRow = sheet.createRow(2);  // Start from row 2 for header
        cellIndex = 0;
        for (String key : keysWithData) {
            Cell cell = headerRow.createCell(cellIndex++);
            cell.setCellValue(key);
        }

        int rowIndex = 3;  // Start from row 3, leaving rows 0 and 1 for counts and blank
        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            Row row = sheet.createRow(rowIndex++);
            cellIndex = 0;
            for (String key : keysWithData) {
                Cell cell = row.createCell(cellIndex++);
                JsonElement value = jsonObject.get(key);
                if (value != null && !value.isJsonNull()) {
                    if (isNumericField(jsonArray, key)) {
                        cell.setCellValue(value.getAsDouble());
                    } else {
                        cell.setCellValue(value.getAsString());
                    }
                }
            }
        }

        // Enable filtering for the table
        sheet.setAutoFilter(new CellRangeAddress(2, sheet.getLastRowNum(), 0, keysWithData.size() - 1));

        // Auto-size columns and limit maximum width
        for (int i = 0; i < keysWithData.size(); i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            int newWidth = Math.min(currentWidth + COLUMN_WIDTH_BUFFER, MAX_COLUMN_WIDTH);
            sheet.setColumnWidth(i, newWidth);
        }

        // Write the output to a file
        try (FileOutputStream fileOut = new FileOutputStream(filepathExcel)) {
            workbook.write(fileOut);
            System.out.println("Excel file created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Close the workbook
        try {
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
