package gov.epa.endpoints.reports.predictions.ExcelReports;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Units;
import org.apache.poi.xddf.usermodel.*;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.json.CDL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetServiceImpl;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.reports.OriginalCompound;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.endpoints.reports.predictions.PredictionReportGenerator;
import gov.epa.endpoints.reports.predictions.QsarPredictedValue;
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

	
	String[] fieldsMappedRecords = { "exp_prop_id", "canon_qsar_smiles", "page_url", 			
			"public_source_name","public_source_url","public_source_original_name","public_source_original_url",
			"literature_source_citation","literature_source_doi",
//			"source_url", "source_doi",	"source_name", "source_description", "source_authors", "source_title", 
			"source_dtxrid","source_dtxsid", "source_casrn", "source_chemical_name", "source_smiles", 
			"mapped_dtxcid", "mapped_dtxsid","mapped_cas", "mapped_chemical_name", "mapped_smiles", "mapped_molweight", 
			"value_original", "value_max", "value_min", "value_point_estimate", "value_units", 
			"qsar_property_value", "qsar_property_units",
			"temperature_c", "pressure_mmHg", "pH", "notes", "qc_flag"};

	
//	private static class Stats {
//		
//		List<String> continuousStats= new ArrayList<String>(Arrays.asList("Split","Q2_CV","R2","Q2","RMSE", "MAE"));
//		List<String> binaryStats = (Arrays.asList("Split", "BA", "SN", "SP"));
//		
//		//TODO add R2_Inside_AD, BA_inside_AD (store in database...)
//		
//		
//		Hashtable<String,Object>htStatsTraining=new Hashtable<>();
//		Hashtable<String,Object>htStatsTest=new Hashtable<>();
//				
//		// switch statement limited to newer version of java on strings for some reason
//		private void storeStats(PredictionReportModelStatistic s) {
//						
//			if (s.statisticName.contains("Test")) {
//				String statisticName=s.statisticName.replace("_Test", "").replace("_Training", "");
//				statisticName=statisticName.replace("PearsonRSQ", "R2");
//				htStatsTest.put(statisticName, s.statisticValue);
//				htStatsTest.put("Split", "Test");
//			} else {
//				String statisticName=s.statisticName.replace("_Test", "").replace("_Training", "");
//				statisticName=statisticName.replace("PearsonRSQ", "R2");
//				htStatsTraining.put(statisticName, s.statisticValue);
//				htStatsTraining.put("Split", "Train");
//			}
//			
//		}
//
//		private List<Object> provideStats(String set, boolean isBinary) {
//			
//			List<String>statNames=null;
//			if (isBinary) statNames=binaryStats;		
//			else statNames=continuousStats;
//			
//			Hashtable<String,Object>htStats=null;
//			if (set.equals("Test")) htStats=htStatsTest;
//			else htStats=htStatsTraining;
//
//			List<Object>statValues=new ArrayList<>();			
//			for (String statname:statNames) statValues.add(htStats.get(statname));
//			return statValues;
//		}
//
//	}

	
	private class Stats {
				
		List<String> binaryStats;
		List<String> continuousStats;
		
		Stats(String AD) {

			binaryStats = (Arrays.asList("Split", "BA", "SN", "SP"));
			
			continuousStats= new ArrayList<String>(Arrays.asList(
					DevQsarConstants.PEARSON_RSQ_TRAINING,DevQsarConstants.PEARSON_RSQ_CV_TRAINING,				
					DevQsarConstants.PEARSON_RSQ_TEST,DevQsarConstants.Q2_TEST,
					DevQsarConstants.RMSE_TEST,
					DevQsarConstants.MAE_CV_TRAINING,
					DevQsarConstants.MAE_TEST));
						
			
			if(AD!=null) {
				continuousStats.add("MAE_Test_inside_AD");
				continuousStats.add("MAE_Test_outside_AD");
				continuousStats.add("Coverage_Test");
			}
		}
				

		private List<Object> provideStats(boolean isBinary, Hashtable<String,Object>htStats) {
			
			List<String>statNames=null;
			if (isBinary) statNames=binaryStats;		
			else statNames=continuousStats;
			
			List<Object>statValues=new ArrayList<>();			
			for (String statname:statNames) {
				Double value=(Double)htStats.get(statname);
				if (value==null || Double.isNaN(value)) {
					statValues.add("N/A");
				} else {
					statValues.add(value);	
				}
			}
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

	void sampleMakeReport() {
		PredictionReportGenerator gen = new PredictionReportGenerator();
		PredictionReport predictionReport=null;
		
//		predictionReport=gen.generateForModelSetPredictions(datasetName, splittingName,modelSetName);

//		File jsonFile = new File("data\\reports\\WebTEST2.1\\WS v1 modeling_PredictionReport_only_PFAS_with_AD.json");
//		File jsonFile = new File("data\\reports\\WebTEST2.1\\HLC v1 modeling_PredictionReport_with_AD.json");
		File jsonFile = new File("data\\reports\\WebTEST2.1\\HLC v1 modeling_PredictionReport_only_PFAS_with_AD.json");

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

		String fileName=jsonFile.getName().replace("json", "xlsx");

		File folder=new File("data/reports");
		folder.mkdirs();		
		//		e.generate( predictionReport,folder.getAbsolutePath()+File.separator+"report.xlsx");
		generate(predictionReport, folder.getAbsolutePath()+File.separator+fileName,null,null);
		
	}
	
	public static void main(String [] args) {
		ExcelPredictionReportGenerator e=new ExcelPredictionReportGenerator();
		e.sampleMakeReport();
		
//		e.getDescriptorDefinitionHashtable();
	}

	
	public static String getMappedJsonPath(String dataSetName) {
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
	
	
	JsonArray addExperimentalRecordsSheet(Workbook wb,String jsonPath,HashSet<String>smilesList) {
		
		JsonArray ja=Utilities.getJsonArrayFromJsonFile(jsonPath);
		
		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			String smiles=jo.get("canon_qsar_smiles").getAsString();
			
			
			if(smilesList!=null && !smilesList.contains(smiles)) {
				ja.remove(i--);
			}
		}
		
		Hashtable<String,String>htDescriptions=ExcelCreator.getColumnDescriptions();
		ExcelCreator.addSheet(wb, "Records",ja,fieldsMappedRecords, htDescriptions);
		wb.setSheetOrder("Records", wb.getSheetIndex("Test set")+1);
		wb.setSheetOrder("Records field descriptions", wb.getSheetIndex("Records")+1);
		return ja;
		
	}
	
	void addEquationImageToStatisticsSheet(Workbook wb,String sheetName) {
		Sheet sheet=wb.getSheet(sheetName);
		
		int rowNum=sheet.getLastRowNum()+2;
		
		String filePath="data\\reports\\prediction reports upload\\equations2.png";
		
		Path path = Paths.get(filePath);

		try {
			byte[] imageData = Files.readAllBytes(path);
			
			Row row=sheet.createRow(rowNum);
			
			eu.createImageSimple(wb, sheetName, imageData, rowNum, 1);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Adds original experimental records
	 * 
	 * @param report
	 * @param filepathOut
	 */
	public void generate(PredictionReport report, String filepathOut,HashSet<String>smiles,String applicabilityDomain) {
		
		Workbook wb = new XSSFWorkbook();
		
		boolean isBinary = report.predictionReportMetadata.datasetUnit.equalsIgnoreCase("binary") ? true : false;
		
		System.out.println("Generating cover sheet");
		generateCoverSheet2(report,wb, isBinary,applicabilityDomain);

		System.out.println("Generating Statistics sheet");
		generateStatisticsSheet(report, wb, isBinary);
		
		System.out.println("Generating training and prediction set sheets");
		generateSplitSheet2(report, wb, isBinary);

		
		System.out.println("Generating prediction sheets");
		for (int i = 0; i < report.predictionReportModelMetadata.size(); i++) {
			
			String propertyName=report.predictionReportMetadata.datasetProperty;
			String propertyUnits=report.predictionReportMetadata.datasetUnit;
			
			String methodName = report.predictionReportModelMetadata.get(i).qsarMethodName;
			

			String sheetName=null;
			
			if (report.predictionReportModelMetadata.size()>1) {
				sheetName=methodName+" predictions";	
			} else {
				sheetName="Test set predictions";
			}
			
			generatePredictionSheet2(report, i, wb, isBinary,propertyName ,propertyUnits,sheetName);

			
			if (!(isBinary)) {
				eu.GenerateChart(wb,methodName,"Experimental" + propertyName,"Predicted" + propertyName,propertyName,propertyUnits,sheetName);
			}

		}
		
		addEmbeddingDescriptions(wb, report);
		
		for (int i = 0; i < report.predictionReportModelMetadata.size(); i++) {
			
			String sheetName=null;
			
			if (report.predictionReportModelMetadata.size()>1) {
				sheetName=report.predictionReportModelMetadata.get(i).qsarMethodName+" descriptor values";	
			} else {
				sheetName="Model descriptor values";
			}
			addEmbeddingDescriptors(report, i, wb,sheetName);
		}
		
		eu.autoSizeColumns(wb);
		
		
		String jsonPath=getMappedJsonPath(report.predictionReportMetadata.datasetName);
		
		
		if(new File(jsonPath).exists()) {
			
			System.out.println("Adding experimental mapped records sheet");
			JsonArray ja=addExperimentalRecordsSheet(wb,jsonPath,smiles);	
			
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
		
		
//		for (int i = 0; i < report.predictionReportModelMetadata.size(); i++) {
//			addEmbeddingDescriptors(report, i,wb);
//		}

		
//		if(true) {
//			System.out.println("Need to uncomment: 7878");
//			return;
//		}

		
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
		
		System.out.println("Excel report created at "+filepathOut);


//		System.out.println("***\t"+report.predictionReportMetadata.datasetName+"\t"+countTest+"\t"+countTestXgb);

	}
	
	
	/**
	 * Adds original experimental records
	 * 
	 * @param report
	 * @param filepathOut
	 */
//	public void generate(PredictionReport report, String filepathOut,List<String>smiles) {
//		
//		Workbook wb = new XSSFWorkbook();
//		
//		boolean isBinary = report.predictionReportMetadata.datasetUnit.equalsIgnoreCase("binary") ? true : false;
//		generateCoverSheet2(report,wb, isBinary);
//
//		generateSummarySheet2(report, wb, isBinary);
//		
//		generateSplitSheet2(report, wb, isBinary);
//
////		for (int i = 0; i < report.predictionReportDataPoints.get(0).qsarPredictedValues.size(); i++) {
//		for (int i = 0; i < report.predictionReportModelMetadata.size(); i++) {
//			generatePredictionSheet2(report, i, wb, isBinary, report.predictionReportMetadata.datasetProperty,report.predictionReportMetadata.datasetUnit);
//		}
//
//		columnResizing(wb);
//		
//		String jsonPath=getMappedJsonPath(report.predictionReportMetadata.datasetName);
//		
//		if(new File(jsonPath).exists()) {
//			JsonArray ja=addExperimentalRecordsSheet(wb,jsonPath,smiles);
//			
//			Hashtable<String,Integer>htExp_Prop_ids=new Hashtable<>();//look up index of specific exp_prop_id			
//			for (int i=0;i<ja.size();i++) {
//				JsonObject jo=ja.get(i).getAsJsonObject();
//				String exp_prop_id=jo.get("exp_prop_id").getAsString();
//				htExp_Prop_ids.put(exp_prop_id, i);
////				System.out.println("*"+exp_prop_id+"\t"+i);
//			}
//			
//			addHyperlinksToRecords(report,wb,htExp_Prop_ids);
//		} else {
//			System.out.println("Cant add experimental records json is missing:"+jsonPath);
//		}
//		
//		
//		for (int i = 0; i < report.predictionReportModelMetadata.size(); i++) {
//			addEmbeddingDescriptors(report, i,wb);
//		}
//		
//		
////		if(true) {
////			System.out.println("Need to uncomment: 12433");
////			return;
////		}
//		
//		
//		try {
//			FileOutputStream out = new FileOutputStream(filepathOut);
//			wb.write(out);
//			wb.close();			
//			out.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println("Excel report created at "+filepathOut );
//		
//		
//
////		System.out.println("***\t"+report.predictionReportMetadata.datasetName+"\t"+countTest+"\t"+countTestXgb);
//
//	}

	
//	void addEmbeddingDescriptors(PredictionReport r,int methodID,Workbook wb) {
//
//		String embedding=r.predictionReportModelMetadata.get(methodID).descriptorEmbeddingTsv;
//		if (embedding==null) return; 
//
//		String methodName=r.predictionReportModelMetadata.get(methodID).qsarMethodName;
//		
//		Sheet sheet=wb.getSheet(methodName);
//		if(sheet==null) sheet=wb.createSheet(methodName);
//		
//		System.out.println(methodName+"\t"+embedding);		
//
//		String [] descriptorNames=embedding.split("\t");
//
//		Hashtable<String,String>htDefinitions=getDescriptorDefinitionHashtable(1);
//		
//		int rowNum=0;
//		Row row=sheet.getRow(rowNum);
//
//		if(row==null) {
//			row=sheet.createRow(rowNum);
//		}
//
//		
//		CellStyle boldstyle = wb.createCellStyle();//Create style
//		Font font = wb.createFont();;//Create font
//		font.setBold(true);//Make font bold
//		boldstyle.setFont(font);//set it to bold
//
//		
//		Cell cell = row.createCell(15);
//		cell.setCellValue("Model descriptor");
//		cell.setCellStyle(boldstyle);
//
//		cell = row.createCell(16);
//		cell.setCellValue("Definition");
//		cell.setCellStyle(boldstyle);
//
//		for (int i=0;i<descriptorNames.length;i++) {
//			rowNum++;
//			row=sheet.getRow(rowNum);
//			if(row==null) {
//				row=sheet.createRow(rowNum);
//			}
//			cell = row.createCell(15);
//			cell.setCellValue(descriptorNames[i]);
//			cell = row.createCell(16);
//			cell.setCellValue(htDefinitions.get(descriptorNames[i]));
//			
////			System.out.println(descriptorNames[i]+"\t"+htDefinitions.get(descriptorNames[i]));
//			
//		}
//		
////		sheet.autoSizeColumn(6);
////		sheet.autoSizeColumn(7);
//
//	}
	
	private void addEmbeddingDescriptions(Workbook wb,PredictionReport pr) {

		System.out.println("Generating embedding descriptions");
		
		boolean haveEmbedding=false;
		for (PredictionReportModelMetadata metadata:pr.predictionReportModelMetadata) {
			if (metadata.descriptorEmbeddingTsv!=null) haveEmbedding=true;
		}
		if(!haveEmbedding)return;
		
		
		Sheet sheet=wb.createSheet("Model descriptors");
		
		CellStyle boldstyle = wb.createCellStyle();//Create style
		Font font = wb.createFont();;//Create font
		font.setBold(true);//Make font bold
		boldstyle.setFont(font);//set it to bold

		int rowNum=0;
		Row row=sheet.createRow(0);
		
		Cell cell = row.createCell(rowNum);
		cell.setCellValue("Method");
		cell.setCellStyle(boldstyle);
		
		cell = row.createCell(1);
		cell.setCellValue("Descriptor");
		cell.setCellStyle(boldstyle);

		cell = row.createCell(2);
		cell.setCellValue("Definition");
		cell.setCellStyle(boldstyle);
		
		cell = row.createCell(3);
		cell.setCellValue("Class");
		cell.setCellStyle(boldstyle);

		
		Hashtable<String, String>htDefs=getDescriptorDefinitionHashtable(1);
		Hashtable<String, String>htClasses=getDescriptorDefinitionHashtable(2);
		
		for (PredictionReportModelMetadata metadata:pr.predictionReportModelMetadata) {
			
			if (metadata.descriptorEmbeddingTsv==null) continue;
			
			String embedding = metadata.descriptorEmbeddingTsv;
			
			String [] descriptorNames=embedding.split("\t");
			
//			System.out.println("\t\t\tDescriptor\tDefinition");
//			for (String descriptor:descriptorNames) {
//				System.out.println("\t\t\t"+descriptor+"\t"+htDefs.get(descriptor));
//			}
			

			for (int i=0;i<descriptorNames.length;i++) {
				rowNum++;
				
				row=sheet.createRow(rowNum);
				
				if(i==0) {
					cell = row.createCell(0);
					cell.setCellValue(metadata.qsarMethodName);
				}
				
				cell = row.createCell(1);
				cell.setCellValue(descriptorNames[i]);

				cell = row.createCell(2);
				cell.setCellValue(htDefs.get(descriptorNames[i]));

				cell = row.createCell(3);
				cell.setCellValue(htClasses.get(descriptorNames[i]));
				
//							System.out.println(descriptorNames[i]+"\t"+htDefinitions.get(descriptorNames[i]));
			}
			rowNum++;
			

//			System.out.println("");
//			String [] descriptorNames=embedding.split("\t");
			
		}//end loop of methodNames
		
		setAutofilter(sheet);
		
		
		
		
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
			
			String sheetName=null;
			
			if (pr.predictionReportModelMetadata.size()>1) {
				sheetName=md.qsarMethodName+" predictions";	
			} else {
				sheetName="Test set predictions";
			}

			
			XSSFSheet sheetMethod=(XSSFSheet) wb.getSheet(sheetName);
			addHyperlink(sheetMethod,sheetRecords, createHelper, hlink_style, colNumRecords,htExp_Prop_Ids);
		}
		
	}

	private void addHyperlink(XSSFSheet sheet, XSSFSheet sheetRecords, CreationHelper createHelper, CellStyle hlink_style,
			int colNumRecords,Hashtable <String,Integer>htExp_Prop_Ids) {
		
		int colNumPred=eu.getColumnNumber(sheet, "exp_prop_id", 0);
		
//		System.out.println(sheet.getSheetName()+"\t"+colNumPred);
			
		for (int i=1;i<=sheet.getLastRowNum();i++) {
			Row row=sheet.getRow(i);
			
			if(row==null) continue;
			
			Cell cell=row.getCell(colNumPred);
			
			if(cell==null) continue;
			
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
		generate(report,outputFilePath, null,null);
	}

	public void generateCoverSheet2(PredictionReport predictionReport, Workbook wb, boolean isBinary,String applicabilityDomain) {
		List <List<Object> > spreadsheetMap = new ArrayList <List<Object> >();

		spreadsheetMap.add(prepareCoverSheetRow("Property Name", predictionReport.predictionReportMetadata.datasetProperty));
		spreadsheetMap.add(prepareCoverSheetRow("Property Description", predictionReport.predictionReportMetadata.datasetPropertyDescription));
		spreadsheetMap.add(prepareCoverSheetRow("Dataset Name", predictionReport.predictionReportMetadata.datasetName));
		spreadsheetMap.add(prepareCoverSheetRow("Dataset Description", predictionReport.predictionReportMetadata.datasetDescription));

		int nTrain = 0;
		int nPredict = 0;
		for (int i = 0; i < predictionReport.predictionReportDataPoints.size(); i++) {
			if (predictionReport.predictionReportDataPoints.get(i) != null) {
				
				if (predictionReport.predictionReportDataPoints.get(i).qsarPredictedValues.get(0).splitNum == DevQsarConstants.TRAIN_SPLIT_NUM) {
					nTrain++;
				} else {
					nPredict++;
				}
			}
		}
		
		spreadsheetMap.add(prepareCoverSheetRow("nTraining", nTrain));
		spreadsheetMap.add(prepareCoverSheetRow("nTest", nPredict));

		spreadsheetMap.add(prepareCoverSheetRow("Property Units", predictionReport.predictionReportMetadata.datasetUnit));
		//	spreadsheetMap.put(5, prepareCoverSheetRow("Descriptor Set Name", predictionReport.predictionReportMetadata.descriptorSetName));

		
		if(predictionReport.predictionReportModelMetadata.size()==1) {			
			PredictionReportModelMetadata prmm=predictionReport.predictionReportModelMetadata.get(0);
			spreadsheetMap.add(prepareCoverSheetRow("Method name", prmm.qsarMethodName));
			spreadsheetMap.add(prepareCoverSheetRow("Method description", prmm.qsarMethodDescription));			
		}
		
		
		if(applicabilityDomain!=null) {			
			if (applicabilityDomain.equals(DevQsarConstants.Applicability_Domain_TEST_Embedding_Euclidean)) {
				spreadsheetMap.add(prepareCoverSheetRow("Applicability domain", "Average distance of three most similar chemicals in the training set to the test chemical"));
				spreadsheetMap.add(prepareCoverSheetRow("Applicability domain cutoff", "Average distance at which 95% of training set is inside the applicability domain"));
				spreadsheetMap.add(prepareCoverSheetRow("Applicability domain descriptors", "Model descriptors"));
				spreadsheetMap.add(prepareCoverSheetRow("Applicability domain distance measure", "Euclidean distance"));
			}
		}

		populateSheet2(spreadsheetMap, wb, isBinary, "Cover sheet", null, null);

	}



	private static List<Object> prepareCoverSheetRow(String name, Object value) {
		return new ArrayList<Object>(Arrays.asList(name, value));
	}

	
	public void generateSplitSheet2(PredictionReport predictionReport, Workbook wb,boolean isBinary) {
	
		String unit=predictionReport.predictionReportMetadata.datasetUnit;

		List <List<Object>> trainMap = new ArrayList<List<Object>>();
		List <List<Object>> testMap = new ArrayList<List<Object>>();

		List<Object> columnNames = new ArrayList<>(Arrays.asList("exp_prop_id", "DTXCID","CASRN", "Preferred Name", "Smiles", "Molecular Weight", "Canonical QSAR Ready Smiles", "Experimental Value" + " " + "(" + unit + ")"));
		trainMap.add(columnNames);
		testMap.add(columnNames);

		for (PredictionReportDataPoint dp:predictionReport.predictionReportDataPoints) {

			OriginalCompound oc = null;
			// need to sort for consistency
			try {
				oc = dp.originalCompounds.get(0);
			} catch (IndexOutOfBoundsException ex) {
				// ex.printStackTrace();
				continue;
			}
			
			if (oc == null) continue;

			List<Object> row = new ArrayList<>(Arrays.asList(dp.qsar_exp_prop_property_values_id,oc.dtxcid, oc.casrn, oc.preferredName, oc.smiles, oc.molWeight, dp.canonQsarSmiles, dp.experimentalPropertyValue));

			if (dp.qsarPredictedValues.get(0).splitNum == DevQsarConstants.TRAIN_SPLIT_NUM) {
				trainMap.add(row);
			} else if (dp.qsarPredictedValues.get(0).splitNum == DevQsarConstants.TEST_SPLIT_NUM) {
				testMap.add(row);
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
//		populateSheet2(spreadsheetMap, wb, isBinary, "Summary", null, null);
//
//	}
	
	/**
	 * This version has one row per model instead of separating training and prediction
	 * 
	 * @param report
	 * @param wb
	 * @param isBinary
	 */
	private void generateStatisticsSheet(PredictionReport report, Workbook wb,
			boolean isBinary) {
		
		String sheetName="Statistics";
		
		List<List<Object> > spreadsheetMap = new ArrayList <List<Object>>();

		ArrayList<Object> headerStats = new ArrayList<Object>(Arrays.asList("Dataset Name", "Descriptor Set", 
				"Method Name"));

		Stats stats = new Stats(report.AD);
		
		
		if (isBinary) headerStats.addAll(stats.binaryStats);
		else {
			for (String statName:stats.continuousStats) {
				headerStats.add(statName.replace("Pearson", ""));//make shorter	
			}
		}
		
		spreadsheetMap.add(headerStats);	        

		int i=0;
		
		for (PredictionReportModelMetadata mmd:report.predictionReportModelMetadata) {

			Hashtable<String,Object>htStats=new Hashtable<>();
			
			ArrayList<Object> modelSplitInfo = new ArrayList<Object>();
			modelSplitInfo.add(report.predictionReportMetadata.datasetName);
			modelSplitInfo.add(mmd.descriptorSetName);
			modelSplitInfo.add(mmd.qsarMethodName);

			for (PredictionReportModelStatistic ms:mmd.predictionReportModelStatistics) {
				htStats.put(ms.statisticName, ms.statisticValue);
			}
			
			modelSplitInfo.addAll(stats.provideStats(isBinary,htStats));
			spreadsheetMap.add(modelSplitInfo);			
			i++;

		}

		populateSheet2(spreadsheetMap, wb, isBinary, sheetName, null, null);
		
		addEquationImageToStatisticsSheet(wb,sheetName);


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
//		populateSheet2(spreadsheetMap, wb, isBinary, "Summary", null, null);
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

	private void generatePredictionSheet2(PredictionReport r, int methodID, Workbook wb, Boolean isBinary, String propertyName, String units,String sheetName) {
		
		List <List<Object> > spreadsheetMap = new ArrayList <>();
		
		List<Object> listColumnNames = new ArrayList<>();		
		listColumnNames.add("exp_prop_id");
		listColumnNames.add("Canonical QSAR Ready Smiles");
		listColumnNames.add("Observed " + "(" + units + ")");
		listColumnNames.add("Predicted " + "(" + units + ")");
		listColumnNames.add("Error");
		
		if(r.AD!=null) {
			listColumnNames.add("Inside AD");
		}
		
		spreadsheetMap.add(listColumnNames );

//		Hashtable<String,String> idHash = new Hashtable<String,String>();
//		Hashtable<String,Double> expHash = new Hashtable<String,Double>();
//		Hashtable<String,Double> predictionHash = new Hashtable<String,Double>();
		
//		String methodName = r.predictionReportDataPoints.get(0).qsarPredictedValues.get(methodID).qsarMethodName;
		String methodName = r.predictionReportModelMetadata.get(methodID).qsarMethodName;
		
		System.out.println("Generating prediction sheet for "+methodName);
		
		for (int i = 0; i < r.predictionReportDataPoints.size(); i++) {

			PredictionReportDataPoint dp=r.predictionReportDataPoints.get(i);
			QsarPredictedValue qpv=dp.qsarPredictedValues.get(methodID);
			if(qpv.splitNum!=DevQsarConstants.TEST_SPLIT_NUM) continue;
			
			List<Object> listCellValues = new ArrayList<>();
			listCellValues.add(dp.qsar_exp_prop_property_values_id);
			listCellValues.add(dp.canonQsarSmiles);
			listCellValues.add(dp.experimentalPropertyValue);
			listCellValues.add(qpv.qsarPredictedValue);
			listCellValues.add(Math.abs(dp.experimentalPropertyValue-qpv.qsarPredictedValue));
			
			if(r.AD!=null) {
				listCellValues.add(qpv.AD+"");
			}

			
			spreadsheetMap.add(listCellValues);
		}

		populateSheet2(spreadsheetMap, wb, isBinary, sheetName, propertyName, units);


//		System.out.println("Done Generating prediction sheet");

	}
	

	private void addEmbeddingDescriptors(PredictionReport predictionReport, int methodID, Workbook wb,String sheetName) {

		String propertyName=predictionReport.predictionReportMetadata.datasetProperty;
		String propertyUnits=predictionReport.predictionReportMetadata.datasetUnit;

		
		String embedding=predictionReport.predictionReportModelMetadata.get(methodID).descriptorEmbeddingTsv;
		boolean addEmbeddingDescriptors= embedding!=null && predictionReport.predictionReportDataPoints.get(0).descriptorValues!=null;
		if(!addEmbeddingDescriptors) return;

		String methodName = predictionReport.predictionReportModelMetadata.get(methodID).qsarMethodName;
		System.out.println("Generating descriptors sheet for "+methodName);

		String descriptorSetName=predictionReport.predictionReportModelMetadata.get(methodID).descriptorSetName;
		DescriptorSetServiceImpl dssi= new DescriptorSetServiceImpl();
		DescriptorSet ds=dssi.findByName(descriptorSetName);
		String headerAllDescriptors=ds.getHeadersTsv();

		List < List<Object> > spreadsheetMap = new ArrayList <>();
		
		List < List<Object> > spreadsheetMapTraining = new ArrayList <>();
		List < List<Object> > spreadsheetMapTest = new ArrayList <>();
		
		List<Object> listColumnNames = new ArrayList<>();		
		listColumnNames.add("Canonical QSAR Ready Smiles");
		listColumnNames.add("Observed " + "(" + propertyUnits + ")");
		listColumnNames.add("Predicted " + "(" + propertyUnits + ")");
		listColumnNames.add("Set");

		spreadsheetMap.add( listColumnNames );

		String [] descriptorNamesEmbedding=embedding.split("\t");
		String [] descriptorNamesAll=headerAllDescriptors.split("\t");
		
		for (int i=0;i<descriptorNamesEmbedding.length;i++) {
			String descriptorName=descriptorNamesEmbedding[i];
			listColumnNames.add(descriptorName);
		}

		Hashtable<String,Integer>htCols=new Hashtable<>();
		for (int i=0;i<descriptorNamesAll.length;i++) {
			String descriptorName=descriptorNamesAll[i];
			htCols.put(descriptorName, i);
		}
		
		for (PredictionReportDataPoint dp:predictionReport.predictionReportDataPoints) {

//			if(dp.qsarPredictedValues.get(methodID).splitNum!=DevQsarConstants.TEST_SPLIT_NUM) continue;
			
			List<Object> listCellValues = new ArrayList<>();
			
			listCellValues.add(dp.canonQsarSmiles);
			listCellValues.add(dp.experimentalPropertyValue);
			listCellValues.add(dp.qsarPredictedValues.get(methodID).qsarPredictedValue);
			
			if(dp.qsarPredictedValues.get(methodID).splitNum==DevQsarConstants.TEST_SPLIT_NUM) {
				listCellValues.add("Test");
			} else if(dp.qsarPredictedValues.get(methodID).splitNum==DevQsarConstants.TRAIN_SPLIT_NUM) {
				listCellValues.add("Training");
			}
			
			
			
			String [] descriptorValues=dp.descriptorValues.split("\t");

			for (int j=0;j<descriptorNamesEmbedding.length;j++) {
				String descriptorName=descriptorNamesEmbedding[j];
				int colDescriptor=htCols.get(descriptorName);
				String strDescriptorValue=descriptorValues[colDescriptor];
				double descriptorValue=Double.parseDouble(strDescriptorValue);
				listCellValues.add(descriptorValue);
			}
						
			if(dp.qsarPredictedValues.get(methodID).splitNum==DevQsarConstants.TEST_SPLIT_NUM) {
				spreadsheetMapTest.add(listCellValues);
			} else if(dp.qsarPredictedValues.get(methodID).splitNum==DevQsarConstants.TRAIN_SPLIT_NUM) { 
				spreadsheetMapTraining.add(listCellValues);
			}
			
		}

		//Following keeps training and test sets together:
		spreadsheetMap.addAll(spreadsheetMapTest);
		spreadsheetMap.addAll(spreadsheetMapTraining);
		
		populateSheet2(spreadsheetMap, wb, false, sheetName, null,null);
//		System.out.println("Done Generating prediction sheet");

	}

	public Hashtable<String, String> getDescriptorDefinitionHashtable(int colReturn) {
		try {

			Hashtable<String, String> htDef = new Hashtable<>();

			InputStream is = this.getClass().getClassLoader().getResourceAsStream("variable definitions-ed.txt");//fixes encoding of long dashes ("–" to "-")
			BufferedReader br = new BufferedReader(new InputStreamReader(is,StandardCharsets.ISO_8859_1));

			String header = br.readLine();

			while (true) {
				String line = br.readLine();
				if (line == null)
					break;

				String[] vals = line.split("\t");

				String variable = vals[0];
				String definition = vals[colReturn];


//				definition = new String(definition.getBytes("ISO-8859-1"), "UTF-8");
				
				htDef.put(variable, definition);
				
//				byte[] bytes = definition.getBytes(StandardCharsets.ISO_8859_1);
//				String utf8EncodedString = new String(bytes, StandardCharsets.UTF_8);
				
//				if (variable.equals("SHsOH")) {
//					System.out.println(variable + "\t" + definition + "\t" + descriptorClass);
//					System.out.println(definition.indexOf("–"));
//				}
				
				
//				System.out.println(variable + "\t" + definition);
			}
			br.close();
			return htDef;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}



	public static void setAutofilter(Sheet sheet) {

		if (sheet.getSheetName().equals("Cover sheet")) return;
		
		Row row=sheet.getRow(0);
		
		if(row==null) return;
		
		String lastCol = CellReference.convertNumToColString(row.getLastCellNum()-1);
		int lastRow=sheet.getLastRowNum();
		
		sheet.createFreezePane(0, 1);

		if(lastRow<100000) {//Causes excel to be damaged otherwise and have to have excel fix it on opening
			sheet.setAutoFilter(CellRangeAddress.valueOf("A1:"+lastCol+lastRow));
		}
	}
	
	private void populateSheet2(List<List<Object>> sheetMap, Workbook wb, boolean isBinary, String sheetName, String propertyName, String units) {
		
//		System.out.println(sheetName);
		if (wb.getSheet(sheetName) != null) return;
		
		System.out.println(sheetName);
		
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
		
		
		CellStyle cellStyleLeft = wb.createCellStyle();
		cellStyleLeft.setAlignment(HorizontalAlignment.LEFT);
		

		XSSFRow row;

//		Set < Integer > keyid = sheetMap.keySet();

		int rowid=0;
		
		for (List<Object>objectArr:sheetMap) 
		{
			row = sheet.createRow(rowid);
			
			int cellid = 0;

			for (Object obj : objectArr) {

				Cell cell = row.createCell(cellid);
				String colName=(String)sheetMap.get(0).get(cellid);
				
				if (obj instanceof Double) {
//					System.out.println(sheetName+"\t"+colName);
					
					cell.setCellValue((Double)obj);

					if (!sheetName.contains("descriptors") || colName.contains("Observed") || colName.contains("Predicted"))
						cell.setCellStyle(cellStyle);

				} else if (obj instanceof Integer) {
					cell.setCellValue((Integer)obj);
					cell.setCellStyle(cellStyleLeft);
				} else if (obj instanceof String) {
					cell.setCellValue((String)obj);
					// logic that handles bolding of left hand column of coversheet
					if (sheetName.equals("Cover sheet") && (cellid == 0)) {
						cell.setCellStyle(boldstyle);
					} else if (rowid==0 && !sheetName.equals("Cover sheet")) {
						cell.setCellStyle(boldstyle);
					}
					
				}
				
				cellid++;
			}//end loop over columns
			rowid++;
		}//end loop over rows
		
		setAutofilter(sheet);

		if (sheetName.equals("Test set")) countTest=rowid;
		if (sheetName.contains("xgb")) countTestXgb=rowid;

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
		public void GenerateChart(Workbook wb, String methodName,String source1,String source2, String propertyName,String units,String sheetName) {

			XSSFSheet sheet=(XSSFSheet) wb.getSheet(sheetName);
			
			XSSFDrawing drawing = sheet.createDrawingPatriarch();
			XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 6, 1, 14, 25);

			XSSFChart chart = drawing.createChart(anchor);

			if (chart instanceof XSSFChart) ((XSSFChart)chart).setTitleText(methodName);


			XDDFValueAxis bottomAxis = chart.createValueAxis(AxisPosition.BOTTOM);
			bottomAxis.setTitle("Observed " + propertyName + " " + "(" + units + ")"); // https://stackoverflow.com/questions/32010765
						
			XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
            leftAxis.setTitle("Predicted "+ propertyName + " " + "(" + units + ")");

//            System.out.println(methodName+"\t"+sheet.getLastRowNum());
            

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
			
			if(row==null) return -1;
			
			for (int i=0;i<row.getLastCellNum();i++) {
				
				if(row.getCell(i)==null) continue;
				
				if (row.getCell(i).getCellType()==CellType.STRING && 
						row.getCell(i).getStringCellValue().equals(colName)) {
					return i;
				}
			}
			return -1;
		}

		
		public void autoSizeColumns(Workbook workbook) {
			int numberOfSheets = workbook.getNumberOfSheets();

			HashSet <String>colNames=new HashSet<>();
			colNames.add("Preferred Name");
			colNames.add("Smiles");
			colNames.add("Canonical QSAR Ready Smiles");
			

			for (int i = 0; i < numberOfSheets; i++) {
				XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(i);
				
				if (sheet.getPhysicalNumberOfRows() ==0) continue;
				
				
				XSSFRow row = sheet.getRow(sheet.getFirstRowNum());
				Iterator<Cell> cellIterator = row.cellIterator();

				while (cellIterator.hasNext()) {

					Cell cell = cellIterator.next();
					
					int columnIndex = cell.getColumnIndex();
					
					if (colNames.contains(cell.getStringCellValue())) {
						sheet.setColumnWidth(columnIndex, 50 * 256);
					} else {
						sheet.autoSizeColumn(columnIndex);
						int currentColumnWidthNew = sheet.getColumnWidth(columnIndex)+400;
						try {
							sheet.setColumnWidth(columnIndex, currentColumnWidthNew);
						} catch (Exception ex) {
							//System.out.println("Cant set columnWidth to "+currentColumnWidthNew);
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

	    
	    public void createImage(byte[] imageBytes, int startRow,int column,Sheet sheet, int rowspan) {

			Workbook wb=sheet.getWorkbook();
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
			
			
			System.out.println(scale);

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
	    
	    public void createImageSimple(Workbook wb, String sheetName, byte[] imageData, int startRow,int column) {

	    	try {

	    		int picID = wb.addPicture(imageData, Workbook.PICTURE_TYPE_PNG);
	    		Sheet sheet=wb.getSheet(sheetName);

	    		Drawing drawing = sheet.createDrawingPatriarch();
	    		CreationHelper helper = wb.getCreationHelper();
	    		ClientAnchor anchor = helper.createClientAnchor();

	    		
	    		anchor.setCol1(0); // Sets the column (0 based) of the first cell.
	    		anchor.setCol2(10); // Sets the column (0 based) of the Second cell.
	    		anchor.setRow1(startRow); // Sets the row (0 based) of the first cell.
	    		anchor.setRow2(startRow+1); //

	    		Row row=sheet.getRow(startRow);
	    		row.setHeight((short)(8563));
	    		
	    		Picture pic = drawing.createPicture(anchor, picID);
	    		pic.setLineStyleColor(0, 0, 0);
	    		pic.resize(0.75);
	    		
	    	} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	    
	}//end excel utilities



}

