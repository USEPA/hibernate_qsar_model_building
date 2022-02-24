package gov.epa.endpoints.reports.predictions.ExcelReports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.charts.AxisCrosses;
import org.apache.poi.ss.usermodel.charts.AxisPosition;
import org.apache.poi.ss.usermodel.charts.ChartDataSource;
import org.apache.poi.ss.usermodel.charts.DataSources;
import org.apache.poi.ss.usermodel.charts.LegendPosition;
import org.apache.poi.ss.usermodel.charts.ScatterChartSeries;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.charts.XSSFChartLegend;
import org.apache.poi.xssf.usermodel.charts.XSSFScatterChartData;
import org.apache.poi.xssf.usermodel.charts.XSSFValueAxis;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTitle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTValAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.STMarkerStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.models.ModelStatisticCalculator;
import gov.epa.endpoints.reports.OriginalCompound;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportDataPoint;

public class ExcelPredictionReportGenerator {
	
	
	String outpath;
	String inpath = "data\\reports";

	
	public Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	private PredictionReport report;
	
	public Workbook wb = new XSSFWorkbook();
	
	private Boolean isBinary;
	String reportName;
	public FileOutputStream out;

	
	
	private static class ContinuousStats {
		Double R2;
		Double Q2;
		Double RMSE;
		Double MAE;
		Double Coverage;
		private ArrayList<String> continuousStats = new ArrayList<String>(Arrays.asList("R2", "Q2", "RMSE", "MAE", "Coverage"));
		
		// switch statement limited to newer version of java on strings for some reason
		private void align(String statisticName, Double statisticValue) {
			if (statisticName.equals("PearsonRSQ_Test")) {
				this.R2 = statisticValue;
			} else if (statisticName.equals("Q2_Test")) {
				this.Q2 = statisticValue;
			} else if (statisticName.equals("RMSE_Test")) {
				this.RMSE = statisticValue;
			} else if (statisticName.equals("MAE_Test")) {
				this.MAE = statisticValue;
			} else if (statisticName.equals("Coverage_Test")) {
				this.Coverage = statisticValue;
			}
		
			}
		}
	
	
	private static class BinaryStats {
		Double BA;
		Double SN;
		Double SP;
		Double Coverage;
		private ArrayList<String> binaryStats = new ArrayList<String>(Arrays.asList("BA", "SN", "SP", "Coverage"));
		
		// switch statement limited to newer version of java on strings for some reason
		private void align(String statisticName, Double statisticValue) {
			if (statisticName.equals("BA_Test")) {
				this.BA = statisticValue;
			} else if (statisticName.equals("SN_Test")) {
				this.SN = statisticValue;
			} else if (statisticName.equals("SP_Test")) {
				this.SP = statisticValue;
			} else if (statisticName.equals("Coverage_Test")) {
				this.Coverage = statisticValue;
			}
		
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
		ExcelPredictionReportGenerator per = prepareReportFactory("LLNA TEST_T.E.S.T. 5.1_PredictionReport.json","data\\ExcelReports");
		per.generate(per.wb);
	}
	
	
	public static ExcelPredictionReportGenerator prepareReportFactory(String reportName, String outpath) {
		ExcelPredictionReportGenerator per = new ExcelPredictionReportGenerator();
		per.reportName = reportName;
		per.outpath = outpath;
		return per;
	}


	public void generate(Workbook wb) {
		int methods;
		PredictionReport report = null;
		
		List<PredictionReportDataPoint> predictionReportDataPoints = null;
        
		File jsonFile = new File(System.getProperty("user.dir") + File.separator + inpath + File.separator + reportName);

		
		try {
			report = gson.fromJson(new FileReader(jsonFile), PredictionReport.class);
			
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		isBinary = report.predictionReportMetadata.datasetUnit.equalsIgnoreCase("binary") ? true : false;
		
		
		generateCoverSheet(report);
		generateSplitSheet(report);
		
		ArrayList<modelHashTables> manyModelHashTables = new ArrayList<modelHashTables>();
		// purely for resizing later on
		ArrayList<String> modelNames = new ArrayList<String>();
		for (int i = 0; i < report.predictionReportDataPoints.get(0).qsarPredictedValues.size(); i++) {
		
		// this is like the second return type for generatePredictionSheet, added to w/ side effects
		modelHashTables modelHashTables = new modelHashTables();
		Map < String, Object[] > map = generatePredictionSheet(report.predictionReportDataPoints,i,modelHashTables);
		
		populateSheet(map, modelHashTables.modelName, false);
		manyModelHashTables.add(modelHashTables);
		
		modelNames.add(modelHashTables.modelName);
		}
		List<ModelPrediction> modelPredictions = new ArrayList<ModelPrediction>();
		modelHashTables consensusHashTables = new modelHashTables(); //
		
		Map < String, Object[] > consensusMap = generateConsensusSheet(manyModelHashTables, consensusHashTables, modelPredictions);
		
		
		populateSheet(consensusMap, "Consensus", false);
		if (isBinary == false) {
		Map<String, Double> StatisticsMap = ModelStatisticCalculator.calculateContinuousStatistics(modelPredictions, findExperimentalAverage(modelPredictions), DevQsarConstants.TAG_TEST);
		generateSummarySheet(report, StatisticsMap);

		} else if (isBinary == true) {
			Map<String, Double> StatisticsMap = ModelStatisticCalculator.calculateBinaryStatistics(modelPredictions, 0.5, DevQsarConstants.TAG_TEST);
			generateSummarySheet(report, StatisticsMap);


		}
		

		
		wb.setSheetOrder("Summary", 1);
		
		
		String[] trainTest = {"Training", "Test"};
		for (String s:trainTest) {
			XSSFSheet sheet = (XSSFSheet) wb.getSheet(s);
			sheet.setColumnWidth(2, 50 * 256);
			sheet.setColumnWidth(3, 50 * 256);
			sheet.setColumnWidth(5, 50 * 256);
		}
		
		modelNames.add("Consensus");
		for (String s:modelNames) {
			XSSFSheet sheet = (XSSFSheet) wb.getSheet(s);
			sheet.setColumnWidth(0, 50 * 256);
		}
	    
	     try {
	    	 
		 		File file = new File(outpath + File.separator + report.predictionReportMetadata.datasetName + "_Prediction_Report.xlsx");
		 		if (file.getParentFile()!=null) {
					file.getParentFile().mkdirs();
				}
		 		out = new FileOutputStream(file);

	     		
	             wb.write(out);
	             out.close();
	     } catch (FileNotFoundException e) {
	    	// TODO Auto-generated catch block
	    	e.printStackTrace();
	    	} catch (IOException e) {
	    	// TODO Auto-generated catch block
	    	e.printStackTrace();
	    	}
	    	System.out.println("Spreadsheet written successfully" );
		}

		
		
	
		public void generateSplitSheet(PredictionReport predictionReport) {
			Map < String, Object[] > trainMap = new TreeMap < String, Object[] >();
			trainMap.put( "AAA", new Object[] { "DTXCID","CASRN", "Preferred Name", "Smiles", "MolecularWeight", "Canonical QSAR Ready Smiles", "Experimental Value" });
			
			Map < String, Object[] > testMap = new TreeMap < String, Object[] >();
			testMap.put( "AAA", new Object[] { "DTXCID","CASRN", "Preferred Name", "Smiles", "MolecularWeight", "Canonical QSAR Ready Smiles", "Experimental Value" });

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
				boolean train = predictionReport.predictionReportDataPoints.get(i).qsarPredictedValues.get(0).splitNum == 0 ? true : false;
				if (oc != null) {
				Object[] row = new Object[] { oc.dtxcid, oc.casrn, oc.preferredName, oc.smiles, oc.molWeight, canonQsarSmiles, predictionReport.predictionReportDataPoints.get(i).experimentalPropertyValue 
						};
				
				if (train == true) {
					trainMap.put("BBB" + String.valueOf(i), row);
				} else {
					testMap.put("BBB" + String.valueOf(i), row);
				}
				}
			}
			
		    populateSheet(trainMap, "Training", true);
		    populateSheet(testMap, "Test", true);
		    

		}
	
		public void generateCoverSheet(PredictionReport predictionReport) {
			Map < String, Object[] > spreadsheetMap = new TreeMap < String, Object[] >();
			
			spreadsheetMap.put("A", prepareCoverSheetRow("Property Name", predictionReport.predictionReportMetadata.datasetProperty));
			spreadsheetMap.put("B", prepareCoverSheetRow("Property Description", predictionReport.predictionReportMetadata.datasetPropertyDescription));
			spreadsheetMap.put("C", prepareCoverSheetRow("Dataset Name", predictionReport.predictionReportMetadata.datasetName));
			spreadsheetMap.put("D", prepareCoverSheetRow("Dataset Description", predictionReport.predictionReportMetadata.datasetDescription));
			spreadsheetMap.put("E", prepareCoverSheetRow("Property Units", predictionReport.predictionReportMetadata.datasetUnit));
			spreadsheetMap.put("F", prepareCoverSheetRow("Descriptor Set Name", predictionReport.predictionReportMetadata.descriptorSetName));
			
			int nTrain = 0;
			int nPredict = 0;
			for (int i = 0; i < predictionReport.predictionReportDataPoints.size(); i++) {
				if (!(predictionReport.predictionReportDataPoints.get(i) == null)) {
				if (predictionReport.predictionReportDataPoints.get(i).qsarPredictedValues.get(0).splitNum == 0) {
					nTrain++;
				} else {
					nPredict++;
				}
				}
			}
			
			spreadsheetMap.put("G", prepareCoverSheetRow("nTraining", String.valueOf(nTrain)));
			spreadsheetMap.put("H", prepareCoverSheetRow("nTEST", String.valueOf(nPredict)));

		    populateSheet(spreadsheetMap, "Cover sheet", true);

		}
		
		private static Object[] prepareCoverSheetRow(String name, String value) {
			List<String> rowArrayList = new ArrayList<String>(Arrays.asList(name, value));
			return rowArrayList.toArray(new Object[rowArrayList.size()]);
		}
	
		public void generateSummarySheet(PredictionReport predictionReport, Map<String, Double> consensusStatisticsMap) {
			Map < String, Object[] > spreadsheetMap = new TreeMap < String, Object[] >();
		
		
			// String[] headers = new String[] { "modelid","datasetname", "descriptorsoftware", "splittingname", "methodname"};
			ArrayList<String> headerStats = new ArrayList<String>(Arrays.asList("Dataset Name", "Descriptor Software", "Method Name"));
			if (isBinary == false) {
				headerStats.addAll(new ContinuousStats().continuousStats);
			} else if (isBinary == true) {
				headerStats.addAll(new BinaryStats().binaryStats);
			}

		   /*
		   Hashtable<String,Double> statistics = new Hashtable<String,Double>();
	        for(int i = 0; i < predictionReport.predictionReportModelMetadata.get(0).predictionReportModelStatistics.size(); i++){
	        	headerStats.add(predictionReport.predictionReportModelMetadata.get(0).predictionReportModelStatistics.get(i).statisticName);
	        	
	        }
	       */
	        String[] headerStatsArray = new String[headerStats.size()];
	        headerStatsArray = headerStats.toArray(headerStatsArray);
	        Object[] headerStatsObject = headerStatsArray;
	        spreadsheetMap.put("AAA", headerStatsObject);	        

			
	        for (int i = 0; i < predictionReport.predictionReportModelMetadata.size(); i++) {
	        	ArrayList<Object> rowArrayList = new ArrayList<Object>();
	        	rowArrayList.add(predictionReport.predictionReportMetadata.datasetName);
	        	rowArrayList.add(predictionReport.predictionReportMetadata.descriptorSetName);
	        	rowArrayList.add(predictionReport.predictionReportModelMetadata.get(i).qsarMethodName);
	        	
	        	// "R2", "Q2", "RMSE", "MAE", "Coverage"
	        	
	        	if (isBinary == true) {
	        		rowArrayList.add(predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics);
		        	BinaryStats bs = new BinaryStats();

		        	for (int j = 0; j < predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics.size(); j++) {
		        		bs.align(predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticName, predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticValue);	
		        	}

	        		Object[] row = new Object[] { predictionReport.predictionReportMetadata.datasetName,
	        				predictionReport.predictionReportMetadata.descriptorSetName,
	        				predictionReport.predictionReportModelMetadata.get(i).qsarMethodName,
	        				bs.BA,bs.SN,bs.SP,bs.Coverage};

			        spreadsheetMap.put("BBB" + String.valueOf(i), row);

	        	
	        	} else if (isBinary == false) {
	        		rowArrayList.add(predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics);
	        	
	        	ContinuousStats cs = new ContinuousStats();
	        	for (int j = 0; j < predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics.size(); j++) {
	        		cs.align(predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticName, predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticValue);	
	        	}
        		// rowArrayList.addAll(Arrays.asList(cs.R2,cs.Q2,cs.RMSE,cs.MAE,cs.Coverage));
        		
        		
	        	
        		
        		Object[] row = new Object[] { predictionReport.predictionReportMetadata.datasetName,
        				predictionReport.predictionReportMetadata.descriptorSetName,
        				predictionReport.predictionReportModelMetadata.get(i).qsarMethodName,
        				cs.R2,cs.Q2,cs.RMSE,cs.MAE,cs.Coverage
        		};
	        	
		        spreadsheetMap.put("BBB" + String.valueOf(i), row);
		        

	        	}
	        }
	        
	        if (isBinary == false) {
	        // adds the consensus row
        	ContinuousStats cs = new ContinuousStats();
        	// printmap(consensusStatisticsMap);
        	
        	cs.R2 = consensusStatisticsMap.get(DevQsarConstants.PEARSON_RSQ + DevQsarConstants.TAG_TEST);
        	cs.MAE = consensusStatisticsMap.get(DevQsarConstants.MAE +  DevQsarConstants.TAG_TEST);
        	cs.RMSE = consensusStatisticsMap.get(DevQsarConstants.RMSE + DevQsarConstants.TAG_TEST);
        	cs.Q2 = consensusStatisticsMap.get(DevQsarConstants.Q2_TEST);
        	cs.Coverage = consensusStatisticsMap.get(DevQsarConstants.COVERAGE+ DevQsarConstants.TAG_TEST);;
        	
    		Object[] consensusrow = new Object[] { predictionReport.predictionReportMetadata.datasetName,
    				predictionReport.predictionReportMetadata.descriptorSetName,
    				"Consensus",
    				cs.R2,cs.Q2,cs.RMSE,cs.MAE,cs.Coverage
    		};
	        spreadsheetMap.put("CCC", consensusrow);

	        /*
	        this should be for adding consensus stats
	        */
		    populateSheet(spreadsheetMap, "Summary", true);
	        } else if (isBinary == true) {
	        	BinaryStats bs = new BinaryStats();
	        	bs.BA = consensusStatisticsMap.get(DevQsarConstants.BALANCED_ACCURACY  + DevQsarConstants.TAG_TEST);
	        	bs.SN = consensusStatisticsMap.get(DevQsarConstants.SENSITIVITY+  DevQsarConstants.TAG_TEST);
	        	bs.SP = consensusStatisticsMap.get(DevQsarConstants.SPECIFICITY + DevQsarConstants.TAG_TEST);
	        	bs.Coverage = consensusStatisticsMap.get(DevQsarConstants.COVERAGE + DevQsarConstants.TAG_TEST);
	        	
	    		Object[] consensusrow = new Object[] { predictionReport.predictionReportMetadata.datasetName,
	    				predictionReport.predictionReportMetadata.descriptorSetName,
	    				"Consensus",
	    				bs.BA,bs.SN,bs.SP,bs.Coverage
	    		};
		        spreadsheetMap.put("CCC", consensusrow);

			    populateSheet(spreadsheetMap, "Summary", true);

	        }


	}	

		private static double findExperimentalAverage(List<ModelPrediction> modelPredictions) {
        	Double sum = 0.0;
			for (int i = 0; i < modelPredictions.size(); i++) {
				sum += modelPredictions.get(i).exp;
			}
        	return modelPredictions.size() > 0 ? sum / modelPredictions.size() : 0.0d;
		}
		
		private static Map < String, Object[] > generateConsensusSheet(ArrayList<modelHashTables> manyModelHashTables, modelHashTables consensusHashTables, List<ModelPrediction> modelPredictions) {
			final Hashtable<String,Double> predConsensusHash = new Hashtable<String,Double>();
			final Hashtable<String,ArrayList<Double>> predTable = new Hashtable<String,ArrayList<Double>>();
			final Hashtable<String,Double> expConsensusHash = manyModelHashTables.get(0).expHash;
			Set<String> keys = manyModelHashTables.get(0).getPredictionHash().keySet();
			ArrayList<String> presentInAllModelKeys = new ArrayList<String>();
			presentInAllModelKeys.addAll(keys);
			
			// ensures predictions are only collected for id's predicted for in all models
			for (int i = 0; i < manyModelHashTables.size(); i++) {
				for (String key:keys) {
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
			
			/*
			modelHashTables consensusTables = new modelHashTables();
			consensusTables.modelName = "Consensus";
			consensusTables.expHash = expConsensusHash;
			consensusTables.predictionHash = predConsensusHash;
			consensusHashTables = consensusTables;
			*/
			
			Map < String, Object[] > spreadsheetMap = new TreeMap < String, Object[] >();
			   spreadsheetMap.put( "AAA", new Object[] { "ID","Exp", "Pred", "Error" });
		    for(String key: presentInAllModelKeys){
		    	ModelPrediction modelPrediction = new ModelPrediction(key, expConsensusHash.get(key), predConsensusHash.get(key));
		    	modelPredictions.add(modelPrediction);
		        spreadsheetMap.put(key, new Object[] { key, expConsensusHash.get(key) , predConsensusHash.get(key), Math.abs(expConsensusHash.get(key) - predConsensusHash.get(key)) });
		        }

			   
			
			return spreadsheetMap;
		}


	
		private void buildSpreadSheet(Map < String, Object[] > map) {
			
		}
	
		private static Map < String, Object[] > generatePredictionSheet(List<PredictionReportDataPoint> predictionReportDataPoints, int methodID, modelHashTables modelHashTable) {
			
			final Hashtable<String,Double> expHash = new Hashtable<String,Double>();
			final Hashtable<String,Double> predictionHash = new Hashtable<String,Double>();

			
			for (int i = 0; i < predictionReportDataPoints.size(); i++) {
				
				String compoundIdentifier = predictionReportDataPoints.get(i).canonQsarSmiles;
				
				if (hasPrediction(predictionReportDataPoints.get(i), methodID)) {
					
				expHash.put(compoundIdentifier, predictionReportDataPoints.get(i).experimentalPropertyValue);
				if (predictionReportDataPoints.get(i).qsarPredictedValues.get(methodID).qsarPredictedValue != null) {
				predictionHash.put(compoundIdentifier, predictionReportDataPoints.get(i).qsarPredictedValues.get(methodID).qsarPredictedValue);
				}
				}
								
			}
			modelHashTable.modelName = predictionReportDataPoints.get(0).qsarPredictedValues.get(methodID).qsarMethodName;
			modelHashTable.expHash = expHash;
			modelHashTable.predictionHash = predictionHash;
			
			Map < String, Object[] > spreadsheetMap = new TreeMap < String, Object[] >();
			   spreadsheetMap.put( "AAA", new Object[] { "Canonical QSAR Ready Smiles","Exp", "Pred", "Error" });

			
			Set<String> keys = predictionHash.keySet();
	        for(String key: keys){
	        	spreadsheetMap.put(key, new Object[] { key, expHash.get(key) , predictionHash.get(key), Math.abs(expHash.get(key) - predictionHash.get(key)) });
	        }

	        return spreadsheetMap;

			
			
			
		}
	
	private static boolean hasPrediction(PredictionReportDataPoint prdp, int methodID) {
		if (prdp.qsarPredictedValues.get(methodID).splitNum == 1) {
			return true;
		}
		else {
			return false;
		}
	}
		
	
	private void populateSheet(Map < String, Object[] > spreadsheetMap, String methodName, boolean summary) {
		
		
			if (wb.getSheet(methodName) != null) {
				return;
			}
		   XSSFSheet sheet = (XSSFSheet) wb.createSheet(methodName);
		   CellStyle cellStyle = wb.createCellStyle();
		   cellStyle.setDataFormat(wb.createDataFormat().getFormat("0.000"));
		   cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		   CellStyle cellStyle2 = wb.createCellStyle();
		   cellStyle2.setDataFormat(wb.createDataFormat().getFormat("0"));
		   cellStyle2.setAlignment(CellStyle.ALIGN_CENTER);

		   XSSFRow row;
		   Set < String > keyid = spreadsheetMap.keySet();
		   
		   
		    CellStyle boldstyle = wb.createCellStyle();//Create style
		    Font font = wb.createFont();;//Create font
		    font.setBold(true);//Make font bold
		    boldstyle.setFont(font);//set it to bold

		   
		     int rowid = 0;
		     for (String key : keyid)
		     {
		         row = sheet.createRow(rowid++);
		         Object [] objectArr = spreadsheetMap.get(key);
		         int cellid = 0;
		         for (Object obj : objectArr)
		         {
		            Cell cell = row.createCell(cellid++);
		            
		            if (obj instanceof Number) {
		            cell.setCellValue((Double)obj);
		            		            
		            
		            
				     if (!(methodName.equals("Cover sheet")) && (cellid != 1)) {
				    	 cell.setCellStyle(cellStyle);
				     }
				     
				     
				    	 if (isBinary==true) {
					     // sheet.setAutoFilter(CellRangeAddress.valueOf("A1:J1"));
					     // autoSizeColumns(wb);
					     
				    	 } else if (isBinary == false) {
						  sheet.setAutoFilter(CellRangeAddress.valueOf("A1:I1"));
				    	 } else if (methodName.equals("Test") || methodName.equals("Training")) {
				    		 sheet.setAutoFilter(CellRangeAddress.valueOf("A1:G1"));

				    	 
				     } 

		            
		            } else if (obj instanceof String) {
			            cell.setCellValue((String)obj);
			            
			            
			            // logic that handles bolding
			            if (!methodName.equals("Cover sheet")) {
			            	if (rowid == 1) {
			            		cell.setCellStyle(boldstyle);
			            	}
			            } else {
			            	if (cellid == 1) {
			            		cell.setCellStyle(boldstyle);
			            	}
			            }

			            
			            
			            
			            //
		            /*
		            // gives non-ID columns 3 decimal places
		            if ((cellid != 1) && summary == true) {cell.setCellStyle(cellStyle);}
		            else { cell.setCellStyle(cellStyle);}

		            } else {
		            cell.setCellValue((String)obj);
		            
		            */

		            }
		         }
		      }
		     
		     if (methodName.equals("Summary")) {
		    	 if (isBinary==true) {
			     sheet.setAutoFilter(CellRangeAddress.valueOf("A1:G1"));
			     // autoSizeColumns(wb);
			     
		    	 } else if (isBinary == false) {
				  sheet.setAutoFilter(CellRangeAddress.valueOf("A1:H1"));
		    	 } 
		    	 
		     } else if (methodName.equals("Test") || methodName.equals("Training")) {
	    		 sheet.setAutoFilter(CellRangeAddress.valueOf("A1:G1"));



		     } else if (methodName.equals("Cover sheet")) {
		     } else {
		    	 
		    if (isBinary == false) {
		     ExcelUtilities eu = new ExcelUtilities();
		     eu.GenerateChart(sheet,"Exp","Pred",methodName,"");
		     
		     sheet.setAutoFilter(CellRangeAddress.valueOf("A1:D1"));
		    } else if (isBinary == true) {
		    
			     sheet.setAutoFilter(CellRangeAddress.valueOf("A1:D1"));

		    }
		     }
		     autoSizeColumns(wb);



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
	   
		public void GenerateChart(XSSFSheet sheet,String source1,String source2,String property,String units) {
			   XSSFDrawing drawing = sheet.createDrawingPatriarch();
			   XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 5, 0, 16, 24);

			   XSSFChart chart = drawing.createChart(anchor);

			   if (chart instanceof XSSFChart) ((XSSFChart)chart).setTitleText(property+": "+source1+" vs. "+source2);


			   XSSFValueAxis bottomAxis = chart.createValueAxis(AxisPosition.BOTTOM);
			   XSSFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
			   leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);    
			   
			   CellRangeAddress crXData = new CellRangeAddress(1, sheet.getLastRowNum(), 1, 1);
			   CellRangeAddress crYData = new CellRangeAddress(1, sheet.getLastRowNum(), 2, 2);
			   CellReference crTitle = new CellReference(0,1);
//			    Cell cell = sheet.getRow(crTitle.getRow()).getCell(crTitle.getCol());

			   ChartDataSource<Number> dsXData = DataSources.fromNumericCellRange(sheet, crXData);
			   ChartDataSource<Number> dsYData = DataSources.fromNumericCellRange(sheet, crYData);

			   XSSFScatterChartData data = chart.getChartDataFactory().createScatterChartData();

			   ScatterChartSeries series1 = data.addSerie(dsXData, dsYData);
			   ScatterChartSeries series2 = data.addSerie(dsXData, dsXData);
			       
			   series1.setTitle("Exp. Data");
			   series2.setTitle("Y=X");
			   chart.plot(data, bottomAxis, leftAxis);

			   //Set axis titles:
			   CTValAx valAx = chart.getCTChart().getPlotArea().getValAxArray(0);
			   CTValAx valAy = chart.getCTChart().getPlotArea().getValAxArray(1);
			   setAxisTitle(source1+" "+units, valAx);
			   setAxisTitle(source2+" "+units, valAy);
			   // *******
			   
			   //set properties of first scatter chart data series to not smooth the line:
			   ((XSSFChart)chart).getCTChart().getPlotArea().getScatterChartArray(0).getSerArray(0)
			    .addNewSmooth().setVal(false);
			       
//			    System.out.println(chart.getCTChart().getPlotArea().getScatterChartArray(0).getSerArray(0).getSpPr());
			   
			  //Set series line to no fill: 
			chart.getCTChart().getPlotArea().getScatterChartArray(0).getSerArray(0)
			   .addNewSpPr().addNewLn().addNewNoFill();

			chart.getCTChart().getPlotArea().getScatterChartArray(0).getSerArray(1)
			   .addNewMarker().addNewSymbol().setVal(STMarkerStyle.NONE);


			
			
			//Add linear trend line:
			chart.getCTChart().getPlotArea().getScatterChartArray(0).getSerArray(0).addNewTrendline().addNewTrendlineType().setVal(org.openxmlformats.schemas.drawingml.x2006.chart.STTrendlineType.LINEAR);
			// *******

			   XSSFChartLegend legend = chart.getOrCreateLegend();
			   legend.setPosition(LegendPosition.BOTTOM);
			
			   

			   //set properties of first scatter chart series to not vary the colors:
			   ((XSSFChart)chart).getCTChart().getPlotArea().getScatterChartArray(0)
			    .addNewVaryColors().setVal(false);
			   


			}
		
		
		private void setAxisTitle(String source1, CTValAx valAx) {
			CTTitle ctTitle = valAx.addNewTitle();
			   ctTitle.addNewLayout();
			   ctTitle.addNewOverlay().setVal(false);
			   CTTextBody rich = ctTitle.addNewTx().addNewRich();
			   rich.addNewBodyPr();
			   rich.addNewLstStyle();
			   CTTextParagraph p = rich.addNewP();
			   p.addNewPPr().addNewDefRPr();
			   p.addNewR().setT(source1);
			   p.addNewEndParaRPr();
			}
		
	   }
		


}

