package gov.epa.endpoints.reports.predictions.ExcelReports;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;


public class ExcelModelStatisticsOld {
	
	public String path;
	
	static class ReportInfo {
		public String modelID;
		public String Endpoint;
		public String descriptorSoftware;
		public String creator;
		public String splitting;
		public String method;
		public String units;
		public String t_p;
		public Double R2;
		public Double Q2;
		public Double Coverage;
		public Double MAE;
		public Double RMSE;
		public Double BA;
		public Double SN;
		public Double SP;
		public Double Concordance;
		public Double Positive_Concordance;
		public Double Negative_Concordance;	
	}

	private static Gson gson = new GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.create();

	private void createWorksheet(Map < Long, Object[] > map) {
		Workbook wb = new XSSFWorkbook();
		XSSFSheet sheet = (XSSFSheet) wb.createSheet("Models");
	    CellStyle cellStyle = wb.createCellStyle();
	    cellStyle.setDataFormat(wb.createDataFormat().getFormat("0.000"));


		XSSFRow row;
		   Set < Long > keyid = map.keySet();
		   
		     int rowid = 0;
		     for (Long key : keyid)
		     {
		         row = sheet.createRow(rowid++);
		         Object [] objectArr = map.get(key);
		         int cellid = 0;
		         for (Object obj : objectArr)
		         {
		            Cell cell = row.createCell(cellid++);
		            
		            if (obj instanceof Number) {
		            cell.setCellValue((Double)obj);
		            if ((cellid != 1)) cell.setCellStyle(cellStyle);
		            
		            } else {
		            cell.setCellValue((String)obj);

		            }
		         }
		      }
		     
		     sheet.setAutoFilter(CellRangeAddress.valueOf("A1:M1"));
		     
		     autoSizeColumns(wb);
		     



		     
		     FileOutputStream out;
		     try {
		    	 
		 		File file = new File(path + File.separator + "Models_Report_" + java.time.LocalDate.now().toString() + ".xlsx");
		 		if (file.getParentFile()!=null) {
					file.getParentFile().mkdirs();
				}

		     out = new FileOutputStream(file);
		             wb.write(out);
		             out.close();
		     } catch (FileNotFoundException e) {
		    	e.printStackTrace();
		    	} catch (IOException e) {
		    	e.printStackTrace();
		    	}
		    	System.out.println("Spreadsheet written successfully" );



		
		
	}
	
	   public void autoSizeColumns(Workbook workbook) {
	        int numberOfSheets = workbook.getNumberOfSheets();
	        for (int i = 0; i < numberOfSheets; i++) {
	        	XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(i);
	            if (sheet.getPhysicalNumberOfRows() > 0) {
	            	XSSFRow row = sheet.getRow(sheet.getFirstRowNum());
	                Iterator<Cell> cellIterator = row.cellIterator();
	                while (cellIterator.hasNext()) {
	                    Cell cell = cellIterator.next();
	                    int columnIndex = cell.getColumnIndex();
	                    sheet.autoSizeColumn(columnIndex);
	                    int currentColumnWidth = sheet.getColumnWidth(columnIndex);
	                    sheet.setColumnWidth(columnIndex, (currentColumnWidth + 200));
	                }
	            }
	        }
	    }


   public static ExcelModelStatisticsOld prepareFactory(String path) {
	   ExcelModelStatisticsOld ems = new ExcelModelStatisticsOld();
	   ems.path = path;
	   return ems;
   }
   
   public static void main(String[] args) {
	   ExcelModelStatisticsOld ems = ExcelModelStatisticsOld.prepareFactory("data/testpath2");
	   ems.generate();
   }
	   
	   
	public void generate() {
		DatasetService datasetService = new DatasetServiceImpl();
		ModelService modelService = new ModelServiceImpl();
		ModelStatisticService modelStatisticService = new ModelStatisticServiceImpl();

		Logger apacheLogger = LogManager.getLogger("org.apache.http");
		apacheLogger.setLevel(Level.WARN);

		List<Model> allModels = modelService.getAll();
		
		ArrayList<ReportInfo> info = new ArrayList<ReportInfo>();
		for (int i = 0; i < allModels.size(); i++) {
			Model m = allModels.get(i);
			ReportInfo modelinfo = new ReportInfo();
			modelinfo.descriptorSoftware = m.getDescriptorSetName();
			modelinfo.modelID = m.getId().toString();
			String datasetname = m.getDatasetName();
			Dataset searchedDataset = datasetService.findByName(datasetname);
			modelinfo.Endpoint = searchedDataset.getProperty().getName();
			modelinfo.method = m.getMethod().getName();
			modelinfo.units = searchedDataset.getUnit().getName();
			modelinfo.creator = m.getCreatedBy();
			modelinfo.splitting = m.getSplittingName();
			
			
			List<ModelStatistic> ms = modelStatisticService.findByModelId(m.getId());
			
			for (int j = 0; j < ms.size(); j++) {
				String s = ms.get(j).getStatistic().getName().toLowerCase();
				switch (s) {
				case "concordance":
					modelinfo.Concordance = ms.get(j).getStatisticValue();
					break;
				case "r2":
					modelinfo.R2 = ms.get(j).getStatisticValue();
					break;
				case "q2":
					modelinfo.Q2 = ms.get(j).getStatisticValue();
					break;
				case "mae":
					modelinfo.MAE = ms.get(j).getStatisticValue();
					break;
				case "ba":
					modelinfo.BA = ms.get(j).getStatisticValue();
					break;
				case "sn":
					modelinfo.SN = ms.get(j).getStatisticValue();
					break;
				case "sp":
					modelinfo.SP = ms.get(j).getStatisticValue();
					break;
				case "coverage":
					modelinfo.Coverage= ms.get(j).getStatisticValue();
					break;
				case "positive concordance":
					modelinfo.Positive_Concordance= ms.get(j).getStatisticValue();
					break;
				case "negative concordance":
					modelinfo.Negative_Concordance= ms.get(j).getStatisticValue();
					break;
	
				
				}
				
				
			}
			
			info.add(modelinfo);
			
				
		}
		
		
		Map < Long, Object[] > spreadsheetMap = new TreeMap < Long, Object[] >();
		   spreadsheetMap.put( 0L, new Object[] { "ID","Endpoint", "Splitting", "DescriptorSoftware", "Method", "Creator", "R2", "Q2", "MAE", "BA", "SN", "SP", "Coverage" });
		   for (int i = 0; i < info.size(); i++) {
			   ReportInfo ri = info.get(i);
			   spreadsheetMap.put(Long.valueOf(ri.modelID), new Object[] {Double.parseDouble(ri.modelID), ri.Endpoint, ri.splitting, ri.descriptorSoftware, ri.method, ri.creator, ri.R2, ri.Q2, ri.MAE, ri.BA, ri.SN, ri.SP, ri.Coverage});
		   }
		   
		
		   createWorksheet(spreadsheetMap);
		
		
		
	}
	


}




