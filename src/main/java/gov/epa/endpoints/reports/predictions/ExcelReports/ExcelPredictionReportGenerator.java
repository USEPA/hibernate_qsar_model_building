package gov.epa.endpoints.reports.predictions.ExcelReports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;

import org.apache.poi.xddf.usermodel.*;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.reports.OriginalCompound;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.endpoints.reports.predictions.PredictionReportGenerator;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportDataPoint;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportModelMetadata;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportModelStatistic;
import gov.epa.run_from_java.scripts.GetExpPropInfo.ExcelCreator;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

public class ExcelPredictionReportGenerator {

	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	int countTest;
	int countTestXgb;
	ExcelUtilities eu = new ExcelUtilities();

	
	String[] fieldsMappedRecords = { "exp_prop_id", "canon_qsar_smiles", "page_url", "source_url", "source_doi",
			"source_name", "source_description", "source_authors", "source_title", "source_dtxrid",
			"source_dtxsid", "source_casrn", "source_chemical_name", "source_smiles", "mapped_dtxcid", "mapped_dtxsid",
			"mapped_cas", "mapped_chemical_name", "mapped_smiles", "mapped_molweight", "value_original", "value_max",
			"value_min", "value_point_estimate", "value_units", "qsar_property_value", "qsar_property_units",
			"temperature_c", "pressure_mmHg", "pH", "notes", "qc_flag"};

	
	private static class Stats {
		
		List<String> continuousStats= new ArrayList<String>(Arrays.asList("Split","Q2_CV","R2","Q2","RMSE", "MAE", "Coverage"));
		List<String> binaryStats = (Arrays.asList("Split", "BA", "SN", "SP", "Coverage"));
		
		//TODO add R2_Inside_AD, BA_inside_AD (store in database...)
		
		
		Hashtable<String,Object>htStatsTraining=new Hashtable<>();
		Hashtable<String,Object>htStatsTest=new Hashtable<>();
				
		// switch statement limited to newer version of java on strings for some reason
		private void storeStats(PredictionReportModelStatistic s) {
						
			if (s.statisticName.contains("Test")) {
				String statisticName=s.statisticName.replace("_Test", "").replace("_Training", "");
				statisticName=statisticName.replace("PearsonRSQ", "R2");
				htStatsTest.put(statisticName, s.statisticValue);
				htStatsTest.put("Split", "Test");
			} else {
				String statisticName=s.statisticName.replace("_Test", "").replace("_Training", "");
				statisticName=statisticName.replace("PearsonRSQ", "R2");
				htStatsTraining.put(statisticName, s.statisticValue);
				htStatsTraining.put("Split", "Train");
			}
			
		}

		private List<Object> provideStats(String set, boolean isBinary) {
			
			List<String>statNames=null;
			if (isBinary) statNames=binaryStats;		
			else statNames=continuousStats;
			
			Hashtable<String,Object>htStats=null;
			if (set.equals("Test")) htStats=htStatsTest;
			else htStats=htStatsTraining;

			List<Object>statValues=new ArrayList<>();			
			for (String statname:statNames) statValues.add(htStats.get(statname));
			return statValues;
		}

	}
	
	
	private static class Stats2 {
		
		
		List<String> continuousStats= new ArrayList<String>(Arrays.asList("R2_CV_Training","Q2_CV_Training",				
				"R2_Test","Q2_Test","MAE_Test","RMSE_Test","Coverage_Test"));
		
									
		List<String> binaryStats = (Arrays.asList("Split", "BA", "SN", "SP", "Coverage"));
		
		//TODO add R2_Inside_AD, BA_inside_AD (store in database...)
				
		Hashtable<String,Object>htStats=new Hashtable<>();
				
		// switch statement limited to newer version of java on strings for some reason
		private void storeStats(PredictionReportModelStatistic s) {
						
			String statisticName=s.statisticName.replace("PearsonRSQ", "R2");			
			htStats.put(statisticName, s.statisticValue);
		}

		private List<Object> provideStats(boolean isBinary) {
			
			List<String>statNames=null;
			if (isBinary) statNames=binaryStats;		
			else statNames=continuousStats;
			
			List<Object>statValues=new ArrayList<>();			
			for (String statname:statNames) statValues.add(htStats.get(statname));
			return statValues;
		}

	}

	private static void printHT(Hashtable hashtable) {
		Enumeration<String> keys = hashtable.keys();
		while(keys.hasMoreElements()){
			String key = keys.nextElement();
			System.out.println("key:" + key + " value:" + hashtable.get(key));
		}
	}

	private static void printmap(Map map) {
		Iterator<Integer> itr = map.keySet().iterator();
		while (itr.hasNext()) {
			System.out.println(itr.next());
		}

	}

	public static void main(String [] args) {
		ExcelPredictionReportGenerator e=new ExcelPredictionReportGenerator();

		PredictionReportGenerator gen = new PredictionReportGenerator();
		/*
		String endpoint=DevQsarConstants.DEV_TOX;
		String datasetName = endpoint+" TEST";
		String descriptorSetName="T.E.S.T. 5.1";
		*/
		String endpoint=DevQsarConstants.LOG_HALF_LIFE;
		String datasetName= endpoint + " OPERA";
		String descriptorSetName="T.E.S.T. 5.1";
		
		String splittingName="OPERA";
		String modelSetName="Sample models";

		PredictionReport predictionReport=null;
		
//		predictionReport=gen.generateForModelSetPredictions(datasetName, splittingName,modelSetName);

		File jsonFile = new File("data/reports/PFAS mdoels_Standard Water solubility from exp_prop_PredictionReport.json");

		try {
			predictionReport = gson.fromJson(new FileReader(jsonFile), PredictionReport.class);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		/*
		try {
			FileUtils.writeStringToFile(new File("data/report.json"), gson.toJson(predictionReport));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/


		File folder=new File("data/reports");
		folder.mkdirs();		
		//		e.generate( predictionReport,folder.getAbsolutePath()+File.separator+"report.xlsx");
		e.generate(predictionReport, folder.getAbsolutePath()+File.separator+datasetName + "_report.xlsx");
	}

	
	String getMappedJsonPath(String dataSetName) {
		String dataSetName2=dataSetName.replace(" ", "_");
		String folder="data\\dev_qsar\\output\\";
		String jsonPath=folder+"//"+dataSetName2+"//"+dataSetName2+"_Mapped_Records.json";
		return jsonPath;
	}
	
	JsonArray addExperimentalRecordsSheet(Workbook wb,String jsonPath) {
		
		JsonArray ja=Utilities.getJsonArrayFromJsonFile(jsonPath);
		
		Hashtable<String,String>htDescriptions=ExcelCreator.getColumnDescriptions();
		
		ExcelCreator.addSheet(wb, "Records",ja,fieldsMappedRecords, htDescriptions);
		
		wb.setSheetOrder("Records", wb.getSheetIndex("Test set")+1);
		wb.setSheetOrder("Records field descriptions", wb.getSheetIndex("Records")+1);
		
		return ja;
		
	}
	
	
	JsonArray addExperimentalRecordsSheet(Workbook wb,String jsonPath,List<String>smilesList) {
		
		JsonArray ja=Utilities.getJsonArrayFromJsonFile(jsonPath);
		
		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			String smiles=jo.get("canon_qsar_smiles").getAsString();
			if(!smilesList.contains(smiles)) {
				ja.remove(i--);
			}
		}
		
		Hashtable<String,String>htDescriptions=ExcelCreator.getColumnDescriptions();
		ExcelCreator.addSheet(wb, "Records",ja,fieldsMappedRecords, htDescriptions);
		wb.setSheetOrder("Records", wb.getSheetIndex("Test set")+1);
		wb.setSheetOrder("Records field descriptions", wb.getSheetIndex("Records")+1);
		return ja;
		
	}
	
	/**
	 * Adds original experimental records
	 * 
	 * @param report
	 * @param filepathOut
	 */
	public void generate(PredictionReport report, String filepathOut) {
		
		Workbook wb = new XSSFWorkbook();
		
		boolean isBinary = report.predictionReportMetadata.datasetUnit.equalsIgnoreCase("binary") ? true : false;
		
		System.out.println("Generating cover sheet");
		generateCoverSheet2(report,wb, isBinary);

		System.out.println("Generating summary sheet");
		generateSummarySheet2(report, wb, isBinary);
		
		System.out.println("Generating training and prediction set sheets");
		generateSplitSheet2(report, wb, isBinary);

		
		System.out.println("Generating prediction sheets");
		for (int i = 0; i < report.predictionReportDataPoints.get(0).qsarPredictedValues.size(); i++) {
			generatePredictionSheet2(report.predictionReportDataPoints, i, wb, isBinary, report.predictionReportMetadata.datasetProperty,report.predictionReportMetadata.datasetUnit);
		}

		columnResizing(wb);
		
		String jsonPath=getMappedJsonPath(report.predictionReportMetadata.datasetName);
		
		if(new File(jsonPath).exists()) {
			
			System.out.println("Adding experimental mapped records sheet");
			JsonArray ja=addExperimentalRecordsSheet(wb,jsonPath);	
			
			Hashtable<String,Integer>htExp_Prop_ids=new Hashtable<>();			
			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				String exp_prop_id=jo.get("exp_prop_id").getAsString();
				htExp_Prop_ids.put(exp_prop_id, i);
//				System.out.println(i+"\t"+exp_prop_id);
			}
			
			System.out.println("Adding intrahyperlinks to sheets");			
			addHyperlinksToRecords(report,wb,htExp_Prop_ids);
		} else {
			System.out.println("Cant add experimental records json is missing:"+jsonPath);
		}
		
		try {
			FileOutputStream out = new FileOutputStream(filepathOut);
			wb.write(out);
			wb.close();			
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Spreadsheet written successfully" );
		
		

//		System.out.println("***\t"+report.predictionReportMetadata.datasetName+"\t"+countTest+"\t"+countTestXgb);

	}
	
	
	/**
	 * Adds original experimental records
	 * 
	 * @param report
	 * @param filepathOut
	 */
	public void generate(PredictionReport report, String filepathOut,List<String>smiles) {
		
		Workbook wb = new XSSFWorkbook();
		
		boolean isBinary = report.predictionReportMetadata.datasetUnit.equalsIgnoreCase("binary") ? true : false;
		generateCoverSheet2(report,wb, isBinary);

		generateSummarySheet2(report, wb, isBinary);
		
		generateSplitSheet2(report, wb, isBinary);

		for (int i = 0; i < report.predictionReportDataPoints.get(0).qsarPredictedValues.size(); i++) {
			generatePredictionSheet2(report.predictionReportDataPoints, i, wb, isBinary, report.predictionReportMetadata.datasetProperty,report.predictionReportMetadata.datasetUnit);
		}

		columnResizing(wb);
		
		String jsonPath=getMappedJsonPath(report.predictionReportMetadata.datasetName);
		
		if(new File(jsonPath).exists()) {
			JsonArray ja=addExperimentalRecordsSheet(wb,jsonPath,smiles);
			
			Hashtable<String,Integer>htExp_Prop_ids=new Hashtable<>();//look up index of specific exp_prop_id			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				String exp_prop_id=jo.get("exp_prop_id").getAsString();
				htExp_Prop_ids.put(exp_prop_id, i);
//				System.out.println("*"+exp_prop_id+"\t"+i);
			}
			
			addHyperlinksToRecords(report,wb,htExp_Prop_ids);
		} else {
			System.out.println("Cant add experimental records json is missing:"+jsonPath);
		}
		
		try {
			FileOutputStream out = new FileOutputStream(filepathOut);
			wb.write(out);
			wb.close();			
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Spreadsheet written successfully" );
		
		

//		System.out.println("***\t"+report.predictionReportMetadata.datasetName+"\t"+countTest+"\t"+countTestXgb);

	}


	private void addHyperlinksToRecords(PredictionReport pr,Workbook wb,Hashtable <String,Integer>htExp_Prop_Ids) {

		XSSFSheet sheetRecords=(XSSFSheet) wb.getSheet("Records");
		
		CreationHelper createHelper = wb.getCreationHelper();
		
		CellStyle hlink_style = wb.createCellStyle();
		Font hlink_font = wb.createFont();
		hlink_font.setUnderline(Font.U_SINGLE);
		hlink_font.setColor(IndexedColors.BLUE.getIndex());
		hlink_style.setFont(hlink_font);
		
//		exp_prop_id
		
		int colNumRecords=eu.getColumnNumber(sheetRecords, "exp_prop_id", 0);
				
	
//		Hashtable <String,XSSFHyperlink>htLinks=new Hashtable<>();

		XSSFSheet sheetTest=(XSSFSheet) wb.getSheet("Test set");
		addHyperlink(sheetTest,sheetRecords, createHelper, hlink_style, colNumRecords,htExp_Prop_Ids);
		
		XSSFSheet sheetTraining=(XSSFSheet) wb.getSheet("Training set");
		addHyperlink(sheetTraining,sheetRecords, createHelper, hlink_style, colNumRecords,htExp_Prop_Ids);

		for (PredictionReportModelMetadata md:pr.predictionReportModelMetadata) {
//			System.out.println(md.qsarMethodName);
			XSSFSheet sheetMethod=(XSSFSheet) wb.getSheet(md.qsarMethodName);
			addHyperlink(sheetMethod,sheetRecords, createHelper, hlink_style, colNumRecords,htExp_Prop_Ids);
		}
		
	}

	private void addHyperlink(XSSFSheet sheet, XSSFSheet sheetRecords, CreationHelper createHelper, CellStyle hlink_style,
			int colNumRecords,Hashtable <String,Integer>htExp_Prop_Ids) {
		
		int colNumPred=eu.getColumnNumber(sheet, "exp_prop_id", 0);
		
//		System.out.println(sheet.getSheetName()+"\t"+colNumPred);
			
		for (int i=1;i<=sheet.getLastRowNum();i++) {
			Row row=sheet.getRow(i);
			Cell cell=row.getCell(colNumPred);
			addHyperlink(sheetRecords, createHelper, hlink_style, colNumRecords, cell,htExp_Prop_Ids);
		}
	}

	private void addHyperlink(XSSFSheet sheetRecords, CreationHelper createHelper, CellStyle hlink_style,
			int colNumRecords, Cell cell, Hashtable <String,Integer>htExp_Prop_Ids) {
		
		String exp_prop_id=cell.getStringCellValue();
		
//		System.out.println(exp_prop_id);
		
		String []ids=exp_prop_id.split("\\|");
		String firstId=ids[0];//just use first one since cant set multiple links
		
		if (htExp_Prop_Ids.get(firstId)==null) {
//			System.out.println("cant add hyperlink for "+firstId);
			return;			
		}
		
		int row=htExp_Prop_Ids.get(firstId)+1;
		CellAddress cellAddress=sheetRecords.getRow(row).getCell(colNumRecords).getAddress();
		XSSFHyperlink link = (XSSFHyperlink)createHelper.createHyperlink(HyperlinkType.DOCUMENT);
		link.setAddress("'Records'!"+cellAddress.formatAsR1C1String());

		cell.setHyperlink(link);
		cell.setCellStyle(hlink_style);
		
	}

	public void generate(String inputFilePath,String outputFilePath) {

		PredictionReport report = null;

		File jsonFile = new File(inputFilePath);

		try {
			report = gson.fromJson(new FileReader(jsonFile), PredictionReport.class);

		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		generate(report,outputFilePath);
	}

	public void generateCoverSheet2(PredictionReport predictionReport, Workbook wb, boolean isBinary) {
		Map < Integer, Object[] > spreadsheetMap = new TreeMap < Integer, Object[] >();

		spreadsheetMap.put(0, prepareCoverSheetRow("Property Name", predictionReport.predictionReportMetadata.datasetProperty));
		spreadsheetMap.put(1, prepareCoverSheetRow("Property Description", predictionReport.predictionReportMetadata.datasetPropertyDescription));
		spreadsheetMap.put(2, prepareCoverSheetRow("Dataset Name", predictionReport.predictionReportMetadata.datasetName));
		spreadsheetMap.put(3, prepareCoverSheetRow("Dataset Description", predictionReport.predictionReportMetadata.datasetDescription));
		spreadsheetMap.put(4, prepareCoverSheetRow("Property Units", predictionReport.predictionReportMetadata.datasetUnit));
		//	spreadsheetMap.put(5, prepareCoverSheetRow("Descriptor Set Name", predictionReport.predictionReportMetadata.descriptorSetName));

		int nTrain = 0;
		int nPredict = 0;
		for (int i = 0; i < predictionReport.predictionReportDataPoints.size(); i++) {
			if (predictionReport.predictionReportDataPoints.get(i) != null) {
				
				if (predictionReport.predictionReportDataPoints.get(i).qsarPredictedValues.get(0).splitNum == 0) {
					nTrain++;
				} else {
					nPredict++;
				}
			}
		}

		spreadsheetMap.put(6, prepareCoverSheetRow("nTraining", String.valueOf(nTrain)));
		spreadsheetMap.put(7, prepareCoverSheetRow("nTest", String.valueOf(nPredict)));

		populateSheet2(spreadsheetMap, wb, isBinary, "Cover sheet", null, null);

	}



	private static Object[] prepareCoverSheetRow(String name, Object value) {
		List<Object> rowArrayList = new ArrayList<Object>(Arrays.asList(name, value));
		return rowArrayList.toArray(new Object[rowArrayList.size()]);
	}

	
	public void generateSplitSheet2(PredictionReport predictionReport, Workbook wb,boolean isBinary) {
	
		String unit=predictionReport.predictionReportMetadata.datasetUnit;
		Map < Integer, Object[] > trainMap = new TreeMap < Integer, Object[] >();

		String [] columnNames= {"exp_prop_id", "DTXCID","CASRN", "Preferred Name", "Smiles", "MolecularWeight", "Canonical QSAR Ready Smiles", "Experimental Value" + " " + "(" + unit + ")"};
		
		trainMap.put( 0, columnNames);

		Map < Integer, Object[] > testMap = new TreeMap < Integer, Object[] >();
		testMap.put( 0, columnNames);

		for (int i = 0; i < predictionReport.predictionReportDataPoints.size(); i++) {

			OriginalCompound oc = null;
			// need to sort for consistency
			try {
				oc = predictionReport.predictionReportDataPoints.get(i).originalCompounds.get(0);
			} catch (IndexOutOfBoundsException ex) {
				// ex.printStackTrace();
				continue;
			}
			String canonQsarSmiles = predictionReport.predictionReportDataPoints.get(i).canonQsarSmiles;
			
			
			if (oc != null) {
				
				PredictionReportDataPoint dp=predictionReport.predictionReportDataPoints.get(i);
				
				Object[] row = new Object[] { dp.qsar_exp_prop_property_values_id,oc.dtxcid, oc.casrn, oc.preferredName, oc.smiles, oc.molWeight, canonQsarSmiles, dp.experimentalPropertyValue 
				};

				if (predictionReport.predictionReportDataPoints.get(i).qsarPredictedValues.get(0).splitNum == DevQsarConstants.TRAIN_SPLIT_NUM) {
					trainMap.put(2 * i + 1, row);
				} else if (predictionReport.predictionReportDataPoints.get(i).qsarPredictedValues.get(0).splitNum == DevQsarConstants.TEST_SPLIT_NUM) {
					testMap.put(2 * i + 2, row);
				}
			}
		}

		populateSheet2(trainMap,  wb, isBinary, "Training set", null, null);
		populateSheet2(testMap, wb, isBinary, "Test set", null, null);


	}



//	private void generateSummarySheet2(PredictionReport report, Workbook wb,
//			boolean isBinary) {
//		Map < Integer, Object[] > spreadsheetMap = new TreeMap < Integer, Object[] >();
//		ArrayList<String> headerStats = new ArrayList<String>(Arrays.asList("Dataset Name", "Descriptor Software", "Method Name"));
//
//		if (isBinary) headerStats.addAll(new Stats().binaryStats);
//		else headerStats.addAll(new Stats().continuousStats);
//
//		String[] headerStatsArray = new String[headerStats.size()];
//		headerStatsArray = headerStats.toArray(headerStatsArray);
//		Object[] headerStatsObject = headerStatsArray;
//		spreadsheetMap.put(0, headerStatsObject);	        
//
//
//		int i=0;
//		
//		for (PredictionReportModelMetadata mmd:report.predictionReportModelMetadata) {
//
//			Stats stats = new Stats();
//			
//			ArrayList<Object> modelSplitInfoTrain = new ArrayList<Object>();
//			modelSplitInfoTrain.add(report.predictionReportMetadata.datasetName);
//			modelSplitInfoTrain.add(mmd.descriptorSetName);
//			modelSplitInfoTrain.add(mmd.qsarMethodName);
//
//			ArrayList<Object> modelSplitInfoTest = (ArrayList<Object>) modelSplitInfoTrain.clone();
//
//			
//			for (PredictionReportModelStatistic ms:mmd.predictionReportModelStatistics) {
//				stats.storeStats(ms);
//			}
//			
//			modelSplitInfoTrain.addAll(stats.provideStats("Train",isBinary));
//			modelSplitInfoTest.addAll(stats.provideStats("Test",isBinary));
//
//			Object[] trainRow = modelSplitInfoTrain.toArray();
//			Object[] testRow = modelSplitInfoTest.toArray();
//
//			spreadsheetMap.put(2*i+1, trainRow);
//			spreadsheetMap.put(2*i + 2, testRow);
//			i++;
//
//		}
//
//		populateSheet2(spreadsheetMap, wb, isBinary, "Summary sheet", null, null);
//
//	}
	
	/**
	 * This version has one row per model instead of separating training and prediction
	 * 
	 * @param report
	 * @param wb
	 * @param isBinary
	 */
	private void generateSummarySheet2(PredictionReport report, Workbook wb,
			boolean isBinary) {
		
		Map < Integer, Object[] > spreadsheetMap = new TreeMap < Integer, Object[] >();
		ArrayList<String> headerStats = new ArrayList<String>(Arrays.asList("Dataset Name", "Descriptor Software", "Method Name"));

		if (isBinary) headerStats.addAll(new Stats2().binaryStats);
		else headerStats.addAll(new Stats2().continuousStats);

		String[] headerStatsArray = new String[headerStats.size()];
		headerStatsArray = headerStats.toArray(headerStatsArray);
		Object[] headerStatsObject = headerStatsArray;
		spreadsheetMap.put(0, headerStatsObject);	        


		int i=0;
		
		for (PredictionReportModelMetadata mmd:report.predictionReportModelMetadata) {

			Stats2 stats2 = new Stats2();
			
			ArrayList<Object> modelSplitInfo = new ArrayList<Object>();
			modelSplitInfo.add(report.predictionReportMetadata.datasetName);
			modelSplitInfo.add(mmd.descriptorSetName);
			modelSplitInfo.add(mmd.qsarMethodName);

			for (PredictionReportModelStatistic ms:mmd.predictionReportModelStatistics) {
				stats2.storeStats(ms);
			}
			
			modelSplitInfo.addAll(stats2.provideStats(isBinary));

			Object[] trainRow = modelSplitInfo.toArray();		
			spreadsheetMap.put(i+1, trainRow);			
			i++;

		}

		populateSheet2(spreadsheetMap, wb, isBinary, "Summary sheet", null, null);

	}
	
//	private void generateSummarySheet2(PredictionReport report, Workbook wb,
//			boolean isBinary) {
//		Map < Integer, Object[] > spreadsheetMap = new TreeMap < Integer, Object[] >();
//		ArrayList<String> headerStats = new ArrayList<String>(Arrays.asList("Dataset Name", "Descriptor Software", "Method Name"));
//
//		if (isBinary) headerStats.addAll(new Stats().binaryStats);
//		else headerStats.addAll(new Stats().continuousStats);
//
//		String[] headerStatsArray = new String[headerStats.size()];
//		headerStatsArray = headerStats.toArray(headerStatsArray);
//		Object[] headerStatsObject = headerStatsArray;
//		spreadsheetMap.put(0, headerStatsObject);	        
//
//
//		int i=0;
//		
//		for (PredictionReportModelMetadata mmd:report.predictionReportModelMetadata) {
//
//			Stats stats = new Stats();
//			
//			ArrayList<Object> modelSplitInfoTrain = new ArrayList<Object>();
//			modelSplitInfoTrain.add(report.predictionReportMetadata.datasetName);
//			modelSplitInfoTrain.add(mmd.descriptorSetName);
//			modelSplitInfoTrain.add(mmd.qsarMethodName);
//
//			ArrayList<Object> modelSplitInfoTest = (ArrayList<Object>) modelSplitInfoTrain.clone();
//
//			
//			for (PredictionReportModelStatistic ms:mmd.predictionReportModelStatistics) {
//				stats.storeStats(ms);
//			}
//			
//			modelSplitInfoTrain.addAll(stats.provideStats("Train",isBinary));
//			modelSplitInfoTest.addAll(stats.provideStats("Test",isBinary));
//
//			Object[] trainRow = modelSplitInfoTrain.toArray();
//			Object[] testRow = modelSplitInfoTest.toArray();
//
//			spreadsheetMap.put(2*i+1, trainRow);
//			spreadsheetMap.put(2*i + 2, testRow);
//			i++;
//
//		}
//
//		populateSheet2(spreadsheetMap, wb, isBinary, "Summary sheet", null, null);
//
//	}

	private static double findExperimentalAverage(List<ModelPrediction> modelPredictions) {
		Double sum = 0.0;
		for (int i = 0; i < modelPredictions.size(); i++) {			
			sum += modelPredictions.get(i).exp;
		}
		return modelPredictions.size() > 0 ? sum / modelPredictions.size() : 0.0d;
	}

	private static Map < String, Object[] > generateConsensusSheet(ArrayList<modelHashTables> manyModelHashTables, modelHashTables consensusHashTables, List<ModelPrediction> modelPredictionsTest, List<ModelPrediction> modelPredictionsTraining) {
		final Hashtable<String,Double> predConsensusHash = new Hashtable<String,Double>();
		final Hashtable<String,ArrayList<Double>> predTable = new Hashtable<String,ArrayList<Double>>();
		final Hashtable<String,Double> expConsensusHash = manyModelHashTables.get(0).expHash;
		Set<String> keysTEST = manyModelHashTables.get(0).getPredictionHash().keySet();
		Set<String> keysTrain = manyModelHashTables.get(0).getExpHash().keySet();
		ArrayList<String> presentInAllModelKeys = new ArrayList<String>();
		presentInAllModelKeys.addAll(keysTEST);
		ArrayList<String> presentinTraining = new ArrayList<String>();
		presentinTraining.addAll(keysTrain);
		// ensures predictions are only collected for id's predicted for in all models
		for (int i = 0; i < manyModelHashTables.size(); i++) {
			for (String key:keysTEST) {
				if (!(manyModelHashTables.get(i).predictionHash.containsKey(key))) {
					presentInAllModelKeys.remove(key);
					expConsensusHash.remove(key);
				}
			}
		}


		for (int i = 0; i < manyModelHashTables.size(); i++) {
			for(String key: presentInAllModelKeys){

				if (!predTable.containsKey(key)) {
					ArrayList<Double> preds = new ArrayList<Double>();
					preds.add(manyModelHashTables.get(i).getPredictionHash().get(key));
					predTable.put(key,preds);
				} else {
					ArrayList<Double> preds = predTable.get(key);
					preds.add(manyModelHashTables.get(i).getPredictionHash().get(key));
					predTable.put(key,preds);
				}
			}

		}

		for (String key: presentInAllModelKeys) {
			ArrayList<Double> preds = predTable.get(key);
			Double sum = 0.0;
			for (Double pred : preds) {
				sum += pred;
			}

			Double avg = preds.size() > 0 ? sum / preds.size() : 0.0d;
			predConsensusHash.put(key, avg);
		}


		Map < String, Object[] > spreadsheetMap = new TreeMap < String, Object[] >();
		spreadsheetMap.put( "AAA", new Object[] { "ID","Exp", "Pred", "Error" });
		for(String key: presentInAllModelKeys){
			ModelPrediction modelPrediction = new ModelPrediction(key, expConsensusHash.get(key), predConsensusHash.get(key),DevQsarConstants.TRAIN_SPLIT_NUM);
			modelPredictionsTraining.add(modelPrediction);
			spreadsheetMap.put(key, new Object[] { key, expConsensusHash.get(key) , predConsensusHash.get(key), Math.abs(expConsensusHash.get(key) - predConsensusHash.get(key)) });
		}

		for(String key: presentinTraining) {
			ModelPrediction modelPrediction = new ModelPrediction(key, expConsensusHash.get(key), predConsensusHash.get(key),DevQsarConstants.TEST_SPLIT_NUM);
			modelPredictionsTest.add(modelPrediction);
		}



		return spreadsheetMap;
	}



//	private void buildSpreadSheet(Map < String, Object[] > map) {
//
//	}

//	private static Map < String, Object[] > generatePredictionSheet(List<PredictionReportDataPoint> predictionReportDataPoints, int methodID, modelHashTables modelHashTable) {
//
//		final Hashtable<String,Double> expHash = new Hashtable<String,Double>();
//		final Hashtable<String,Double> predictionHash = new Hashtable<String,Double>();
//
//
//		for (int i = 0; i < predictionReportDataPoints.size(); i++) {
//
//			String compoundIdentifier = predictionReportDataPoints.get(i).canonQsarSmiles;
//
//			if (hasPrediction(predictionReportDataPoints.get(i), methodID)) {
//
//				expHash.put(compoundIdentifier, predictionReportDataPoints.get(i).experimentalPropertyValue);
//				if (predictionReportDataPoints.get(i).qsarPredictedValues.get(methodID).qsarPredictedValue != null) {
//					predictionHash.put(compoundIdentifier, predictionReportDataPoints.get(i).qsarPredictedValues.get(methodID).qsarPredictedValue);
//				}
//			}
//
//		}
//		modelHashTable.modelName = predictionReportDataPoints.get(0).qsarPredictedValues.get(methodID).qsarMethodName;
//		modelHashTable.expHash = expHash;
//		modelHashTable.predictionHash = predictionHash;
//
//		Map < String, Object[] > spreadsheetMap = new TreeMap < String, Object[] >();
//		spreadsheetMap.put( "AAA", new Object[] { "exp_prop_id","Canonical QSAR Ready Smiles","Exp", "Pred", "Error" });
//
//
//		Set<String> keys = predictionHash.keySet();
//		for(String key: keys){
//			spreadsheetMap.put(key, new Object[] { key, expHash.get(key) , predictionHash.get(key), Math.abs(expHash.get(key) - predictionHash.get(key)) });
//		}
//
//		return spreadsheetMap;
//
//
//
//
//	}

	private void generatePredictionSheet2(List<PredictionReportDataPoint> predictionReportDataPoints, int methodID, Workbook wb, Boolean isBinary, String propertyName, String units) {
		
		Map < Integer, Object[] > spreadsheetMap = new TreeMap < Integer, Object[] >();
		
		String [] columnNames= { "exp_prop_id","Canonical QSAR Ready Smiles","Observed " + "(" + units + ")", "Predicted " + "(" + units + ")", "Error"};
		spreadsheetMap.put( 0,columnNames );

		Hashtable<String,String> idHash = new Hashtable<String,String>();
		Hashtable<String,Double> expHash = new Hashtable<String,Double>();
		Hashtable<String,Double> predictionHash = new Hashtable<String,Double>();

		String methodName = predictionReportDataPoints.get(0).qsarPredictedValues.get(methodID).qsarMethodName;

		for (int i = 0; i < predictionReportDataPoints.size(); i++) {

			PredictionReportDataPoint dp=predictionReportDataPoints.get(i);
			String compoundIdentifier = dp.canonQsarSmiles;
			
//			System.out.println(compoundIdentifier+"\t"+dp.qsar_exp_prop_property_values_id);
			
			idHash.put(compoundIdentifier, dp.qsar_exp_prop_property_values_id);

			if (hasPrediction(predictionReportDataPoints.get(i), methodID)) {

				expHash.put(compoundIdentifier, predictionReportDataPoints.get(i).experimentalPropertyValue);
				if (predictionReportDataPoints.get(i).qsarPredictedValues.get(methodID).qsarPredictedValue != null) {
					predictionHash.put(compoundIdentifier, predictionReportDataPoints.get(i).qsarPredictedValues.get(methodID).qsarPredictedValue);
				}
			}

		}

		int i=1;
		for(String key:expHash.keySet()) {
			String exp_prop_id=idHash.get(key);
			Double exp=expHash.get(key);
			
			Double pred=Double.NaN;
			
			if (predictionHash.get(key)!=null) {
				pred=predictionHash.get(key);	
			}
			
			
			//TODO add AD here
			
			spreadsheetMap.put(i++, new Object[] {exp_prop_id, key, exp , pred, Math.abs(exp-pred) });
		}

		populateSheet2(spreadsheetMap, wb, isBinary, methodName, propertyName, units);
	}



	private static boolean hasPrediction(PredictionReportDataPoint prdp, int methodID) {
		if (prdp.qsarPredictedValues.get(methodID).splitNum == 1) {
			return true;
		}
		else {
			return false;
		}
	}

	
	
	private void columnResizing(Workbook wb) {

		eu.autoSizeColumns(wb);
		
		String[] trainTest = {"Training set", "Test set"};

		String [] colNames= {"Preferred Name","Smiles","Canonical QSAR Ready Smiles"};
		
		for (String s:trainTest) {
			XSSFSheet sheet = (XSSFSheet) wb.getSheet(s);
			
			for (String colName:colNames) {
				
				int colNum=eu.getColumnNumber(sheet,colName,0);
				if (colNum!=-1)
					sheet.setColumnWidth(colNum, 50 * 256);	
			}
		}

		// fixes column width for plot sheets
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
        	XSSFSheet sheet = (XSSFSheet) wb.getSheetAt(i);
        	String sheetName = sheet.getSheetName();
        	if (!(sheetName.equals("Cover sheet") || sheetName.equals("Summary sheet") || sheetName.equals("Training set") || sheetName.equals("Test set"))) {
        		sheet.setColumnWidth(eu.getColumnNumber(sheet, "Canonical QSAR Ready Smiles",0), 30 * 256);
        	}
        }
	}

	private void populateSheet2(Map < Integer, Object[] > sheetMap, Workbook wb, boolean isBinary, String sheetName, String propertyName, String units) {
		
//		System.out.println(sheetName);
		if (wb.getSheet(sheetName) != null) return;
		
		XSSFSheet sheet = (XSSFSheet) wb.createSheet(sheetName);
		// 2 decimal center aligned numeric cell style
		CellStyle cellStyle = wb.createCellStyle();
		cellStyle.setDataFormat(wb.createDataFormat().getFormat("0.00"));//TODO dont use this for nTrain and nTEST
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		// boldstyle
		CellStyle boldstyle = wb.createCellStyle();//Create style
		Font font = wb.createFont();;//Create font
		font.setBold(true);//Make font bold
		boldstyle.setFont(font);//set it to bold

		XSSFRow row;
		Set < Integer > keyid = sheetMap.keySet();

		int rowid = 0;
		for (Integer key : keyid)
		{
			row = sheet.createRow(rowid++);
			Object [] objectArr = sheetMap.get(key);
			int cellid = 0;
			for (Object obj : objectArr)
			{
				Cell cell = row.createCell(cellid++);

				if (obj instanceof Number) {
					cell.setCellValue((Double)obj);

					/*TODO : come up with a sensible way of handling binary uniformally
					 * 
					 */
					if (rowid > 1) cell.setCellStyle(cellStyle);


				} else if (obj instanceof String) {
					cell.setCellValue((String)obj);


					// logic that handles bolding of left hand column of coversheet
					if (sheetName.equals("Cover sheet") && (cellid == 1)) {
						cell.setCellStyle(boldstyle);
					}

					if (sheetName.equals("Summary sheet")) {
						if (rowid == 1) cell.setCellStyle(boldstyle);
						if (isBinary) sheet.setAutoFilter(CellRangeAddress.valueOf("A1:J1"));
						else sheet.setAutoFilter(CellRangeAddress.valueOf("A1:I1"));//TODO shouldnt be hardcoded
					}


					if (sheetName.equals("Test set") || sheetName.equals("Training set")) {
						if (rowid == 1) cell.setCellStyle(boldstyle);
						sheet.setAutoFilter(CellRangeAddress.valueOf("A1:H1"));//TODO shouldnt be hardcoded
					}

					if (!(sheetName.equals("Cover sheet") || sheetName.equals("Summary sheet") || sheetName.equals("Training set") || sheetName.equals("Test set"))) {
						sheet.setAutoFilter(CellRangeAddress.valueOf("A1:E1"));//TODO shouldnt be hardcoded
						if (rowid == 1) cell.setCellStyle(boldstyle);
					}
				}
			}
		}

		if (!(isBinary)) {
			if (!(sheetName.equals("Cover sheet") || sheetName.equals("Summary sheet") || sheetName.equals("Training set") || sheetName.equals("Test set"))) {
				eu.GenerateChart(sheet,"Experimental" + propertyName,"Predicted" + propertyName,sheetName,propertyName,units);
			}
		}

		if (sheetName.equals("Test set")) countTest=rowid-1;
		if (sheetName.contains("xgb")) countTestXgb=rowid-1;

	}




	

	static class modelHashTables {
		String modelName;
		Hashtable<String,Double> expHash;
		Hashtable<String,Double> predictionHash;

		Hashtable<String,Double> getPredictionHash(){
			return predictionHash;
		}

		Hashtable<String,Double> getExpHash(){
			return expHash;
		}

	}

	class ExcelUtilities {

		/**
		 * Creates prediction plot. Updated to poi version 5.2.3
		 * 
		 * @param sheet
		 * @param source1
		 * @param source2
		 * @param methodName
		 * @param propertyName
		 * @param units
		 */
		public void GenerateChart(XSSFSheet sheet,String source1,String source2,String methodName, String propertyName,String units) {
			XSSFDrawing drawing = sheet.createDrawingPatriarch();
			XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 6, 0, 14, 24);

			XSSFChart chart = drawing.createChart(anchor);

			if (chart instanceof XSSFChart) ((XSSFChart)chart).setTitleText(methodName);


			XDDFValueAxis bottomAxis = chart.createValueAxis(AxisPosition.BOTTOM);
			bottomAxis.setTitle("Observed " + propertyName + " " + "(" + units + ")"); // https://stackoverflow.com/questions/32010765
						
			XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
            leftAxis.setTitle("Predicted "+ propertyName + " " + "(" + units + ")");


			CellRangeAddress crXData = new CellRangeAddress(1, sheet.getLastRowNum(), 2, 2);
			CellRangeAddress crYData = new CellRangeAddress(1, sheet.getLastRowNum(), 3, 3);

			XDDFDataSource<Double> dsXData = XDDFDataSourcesFactory.fromNumericCellRange(sheet, crXData);
			XDDFNumericalDataSource<Double> dsXData2 = XDDFDataSourcesFactory.fromNumericCellRange(sheet, crXData);
			XDDFNumericalDataSource<Double> dsYData = XDDFDataSourcesFactory.fromNumericCellRange(sheet, crYData);
			
			XDDFScatterChartData data = (XDDFScatterChartData) chart.createData(ChartTypes.SCATTER, bottomAxis, leftAxis);
			
			XDDFScatterChartData.Series series1 = (XDDFScatterChartData.Series) data.addSeries(dsXData, dsYData);			 
			XDDFScatterChartData.Series series2 = (XDDFScatterChartData.Series) data.addSeries(dsXData, dsXData2);

			series1.setTitle("Exp. Data");
			series2.setTitle("Y=X");
			
			chart.plot(data);

			//set properties of first scatter chart data series to not smooth the line:
			((XSSFChart)chart).getCTChart().getPlotArea().getScatterChartArray(0).getSerArray(0)
			.addNewSmooth().setVal(false);

			//			    System.out.println(chart.getCTChart().getPlotArea().getScatterChartArray(0).getSerArray(0).getSpPr());
			series1.setMarkerStyle(MarkerStyle.CIRCLE);
			removeLine(series1);
			
			series2.setMarkerStyle(MarkerStyle.NONE);
			setLineColor(series2,PresetColor.BLACK);
			
			//Add linear trend line:  TMM: we dont need regression line- makes chart too busy- Y=X line only is preferred
//			chart.getCTChart().getPlotArea().getScatterChartArray(0).getSerArray(0).addNewTrendline().addNewTrendlineType().setVal(org.openxmlformats.schemas.drawingml.x2006.chart.STTrendlineType.LINEAR);
			// *******

			XDDFChartLegend legend = chart.getOrAddLegend();
			legend.setPosition(LegendPosition.BOTTOM);
			
			bottomAxis.setCrosses(AxisCrosses.MIN);
            leftAxis.setCrosses(AxisCrosses.MIN);


		}
		
		int getColumnNumber(XSSFSheet sheet,String colName, int rowNum) {		
			
			if (sheet==null) return -1;
			Row row=sheet.getRow(rowNum);
			
			for (int i=0;i<row.getLastCellNum();i++) {
				if (row.getCell(i).getStringCellValue().equals(colName)) {
					return i;
				}
			}
			return -1;
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
						
						int currentColumnWidthNew = sheet.getColumnWidth(columnIndex)+400;
						
						try {
							sheet.setColumnWidth(columnIndex, currentColumnWidthNew);
						} catch (Exception ex) {
//							System.out.println("Cant set columnWidth to "+currentColumnWidthNew);
						}
					}
				}
			}
		}
		
		/**
		 * Changes color of line for a series
		 * 
		 * @param series
		 * @param color
		 */
		private void setLineColor(XDDFChartData.Series series, PresetColor color) {
			//https://stackoverflow.com/questions/55188664/setting-the-color-of-an-excel-sheet-scatter-chart-marker-icon-with-apache-poi
	        XDDFSolidFillProperties fill = new XDDFSolidFillProperties(XDDFColor.from(color));
	        XDDFLineProperties line = new XDDFLineProperties();
	        line.setFillProperties(fill);
	        XDDFShapeProperties properties = series.getShapeProperties();
	        if (properties == null) {
	            properties = new XDDFShapeProperties();
	        }
	        properties.setLineProperties(line);
	        series.setShapeProperties(properties);
	    }
		
		
		/**
		 * Gets rid of line for a series
		 * 		
		 * @param series
		 */
	    private void removeLine(XDDFScatterChartData.Series series) {
	        XDDFNoFillProperties noFillProperties = new XDDFNoFillProperties();
	        XDDFLineProperties lineProperties = new XDDFLineProperties();
	        lineProperties.setFillProperties(noFillProperties);
	        XDDFShapeProperties shapeProperties = series.getShapeProperties();
	        if (shapeProperties == null) shapeProperties = new XDDFShapeProperties();
	        shapeProperties.setLineProperties(lineProperties);
	        series.setShapeProperties(shapeProperties);
	    }



//		private void setAxisTitle(String source1, CTValAx valAx) {
//			CTTitle ctTitle = valAx.addNewTitle();
//			ctTitle.addNewLayout();
//			ctTitle.addNewOverlay().setVal(false);
//			CTTextBody rich = ctTitle.addNewTx().addNewRich();
//			rich.addNewBodyPr();
//			rich.addNewLstStyle();
//			CTTextParagraph p = rich.addNewP();
//			p.addNewPPr().addNewDefRPr();
//			p.addNewR().setT(source1);
//			p.addNewEndParaRPr();
//		}

	}



}

