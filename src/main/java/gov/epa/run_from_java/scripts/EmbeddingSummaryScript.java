package gov.epa.run_from_java.scripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.common.collect.ContiguousSet;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.reports.predictions.ExcelReports.ExcelPredictionReportGenerator;

/**
* @author TMARTI02
*/
public class EmbeddingSummaryScript {
	
	void createSummary() {
		
		ExcelPredictionReportGenerator eprg=new ExcelPredictionReportGenerator();
		
		Hashtable<String, String>htDefs=eprg.getDescriptorDefinitionHashtable(1);
		Hashtable<String, String>htClasses=eprg.getDescriptorDefinitionHashtable(2);
		
		File folderOut=new File("data/embedding Summary v1");
		folderOut.mkdirs();

		
		List<String>datasetNames=new ArrayList<>();	
		
		datasetNames.add("HLC v1 modeling");
		datasetNames.add("WS v1 modeling");
//			datasetNames.add("VP v1 modeling");
//			datasetNames.add("LogP v1 modeling");
//			datasetNames.add("BP v1 modeling");
//			datasetNames.add("MP v1 modeling");
		
		
		List<String>splittingNames=new ArrayList<>();
		
		splittingNames.add("T=PFAS only, P=PFAS");
		splittingNames.add("RND_REPRESENTATIVE");
//		splittingNames.add("T=all but PFAS, P=PFAS");
		
		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		List<String>qsarMethodNames=new ArrayList<>();
		qsarMethodNames.add("rf");
		qsarMethodNames.add("xgb");
		
		Connection conn=SqlUtilities.getConnectionPostgres();
		
		for (String datasetName:datasetNames) {
			
			String excelFileName=datasetName+".xlsx";

			Workbook wb = new XSSFWorkbook();
			
			System.out.println(excelFileName);
			
			for (String splittingName:splittingNames) {
				
				String sheetName=null;

				if(splittingName.equals("T=PFAS only, P=PFAS")) {
					sheetName="T=PFAS";
				} else if(splittingName.equals("RND_REPRESENTATIVE")) {
					sheetName="T=all";
				} else if (splittingName.equals("T=all but PFAS, P=PFAS")) {
					sheetName="T=all but PFAS";					
				} else {
					System.out.println("Unknown splitting:"+splittingName);
				}
				
				wb.createSheet(sheetName);
				
				
				System.out.println("\t"+sheetName);
				
				int rowNum=0;
				
				for (String qsarMethodName:qsarMethodNames) {
					
					System.out.println("\t\t"+qsarMethodName);
					
					String embedding = getEmbedding(descriptorSetName, conn, datasetName, splittingName,
							qsarMethodName);

					
					String [] descriptorNames=embedding.split("\t");
					
					System.out.println("\t\t\tDescriptor\tDefinition");
					for (String descriptor:descriptorNames) {
						System.out.println("\t\t\t"+descriptor+"\t"+htDefs.get(descriptor));
					}
					
										
					rowNum=addDescriptorTable(htDefs, htClasses, wb, sheetName, qsarMethodName, descriptorNames, rowNum);

					System.out.println("");
					
					
//					String [] descriptorNames=embedding.split("\t");
					
				}//end loop of methodNames

				Sheet sheet=wb.getSheet(sheetName);
				for (int i=0;i<=3;i++) sheet.autoSizeColumn(i);
				sheet.setColumnWidth(4, 90*256);

			}//end loop over splittings
			
			
			try {
				FileOutputStream out = new FileOutputStream(folderOut.getAbsolutePath()+File.separator+excelFileName);
				wb.write(out);
				wb.close();			
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}//end loop over dataset names
				

		
		
	}

	

	void createSummarySingleFile() {
		
		ExcelPredictionReportGenerator eprg=new ExcelPredictionReportGenerator();
		
		Hashtable<String, String>htDefs=eprg.getDescriptorDefinitionHashtable(1);
		Hashtable<String, String>htClasses=eprg.getDescriptorDefinitionHashtable(2);
		
		File folderOut=new File("data/reportsForPaperV1");
		folderOut.mkdirs();

//		String excelFileName="embedding Summary global models v1.xlsx";
		String excelFileName="embedding Summary local models v1.xlsx";
		Workbook wb = new XSSFWorkbook();

		
		List<String>datasetNames=new ArrayList<>();	
		
		datasetNames.add("HLC v1 modeling");
		datasetNames.add("WS v1 modeling");
		datasetNames.add("LogP v1 modeling");
		datasetNames.add("VP v1 modeling");
		datasetNames.add("BP v1 modeling");
		datasetNames.add("MP v1 modeling");
		
		
		List<String>splittingNames=new ArrayList<>();
		
		splittingNames.add("T=PFAS only, P=PFAS");
//		splittingNames.add("RND_REPRESENTATIVE");
//		splittingNames.add("T=all but PFAS, P=PFAS");
		
		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		List<String>qsarMethodNames=new ArrayList<>();
		qsarMethodNames.add("rf");
		qsarMethodNames.add("xgb");
		
		Connection conn=SqlUtilities.getConnectionPostgres();
		
		for (String datasetName:datasetNames) {
			
			String sheetName=datasetName;
			wb.createSheet(sheetName);
			
			Sheet sheet=wb.getSheet(sheetName);

			createHeaderRow2(sheet,wb);

			int rowNum=0;
			
			for (String splittingName:splittingNames) {
				
				String trainingSet="";

				if(splittingName.equals("T=PFAS only, P=PFAS")) {
					trainingSet="T=PFAS";
				} else if(splittingName.equals("RND_REPRESENTATIVE")) {
					trainingSet="T=all";
				} else if (splittingName.equals("T=all but PFAS, P=PFAS")) {
					trainingSet="T=all but PFAS";					
				} else {
					System.out.println("Unknown splitting:"+splittingName);
				}
				
				System.out.println("\t"+sheetName);
				
				for (String qsarMethodName:qsarMethodNames) {
					
					System.out.println("\t\t"+qsarMethodName);
					
					String embedding = getEmbedding(descriptorSetName, conn, datasetName, splittingName,
							qsarMethodName);
					
					if (embedding==null) continue;
					
					String [] descriptorNames=embedding.split("\t");
					
					System.out.println("\t\t\tDescriptor\tDefinition");
					for (String descriptor:descriptorNames) {
						System.out.println("\t\t\t"+descriptor+"\t"+htDefs.get(descriptor));
					}
									
					int nTrain=SplittingGeneratorPFAS_Script.getCount(conn, datasetName, splittingName, DevQsarConstants.TRAIN_SPLIT_NUM);
														
					rowNum=addDescriptorTable2(htDefs, htClasses, wb, sheetName, qsarMethodName, trainingSet,nTrain, descriptorNames, rowNum);

					System.out.println("");
					
					
//					String [] descriptorNames=embedding.split("\t");
					
				}//end loop of methodNames
				

			}//end loop over splittings
			
			int colCount=sheet.getRow(0).getLastCellNum();
			for (int i=0;i<colCount-1;i++) sheet.autoSizeColumn(i);
			sheet.setColumnWidth(colCount-1, 90*256);

			
		}//end loop over dataset names
				

		try {
			FileOutputStream out = new FileOutputStream(folderOut.getAbsolutePath()+File.separator+excelFileName);
			wb.write(out);
			wb.close();			
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}



	private String getEmbedding(String descriptorSetName, Connection conn, String datasetName, String splittingName,
			String qsarMethodName) {
		String sql="select embedding_tsv from qsar_models.descriptor_embeddings "
				+ "where qsar_method = '"+qsarMethodName+"' and "
				+ "descriptor_set_name = '"+descriptorSetName+"' and "
				+ "dataset_name = '"+datasetName+"' and "
				+"splitting_name ='"+splittingName+"';";
		
		String embedding=SqlUtilities.runSQL(conn, sql);
		return embedding;
	}



	private void createHeaderRow(Sheet sheet,Workbook wb) {

		CellStyle boldstyle = wb.createCellStyle();//Create style
		Font font = wb.createFont();;//Create font
		font.setBold(true);//Make font bold
		boldstyle.setFont(font);//set it to bold

		
		Row row=sheet.createRow(0);
		
		Cell cell = row.createCell(0);
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

		cell = row.createCell(4);
		cell.setCellValue("Reasoning");
		cell.setCellStyle(boldstyle);
	}
	
	private void createHeaderRow2(Sheet sheet,Workbook wb) {

		CellStyle boldstyle = wb.createCellStyle();//Create style
		Font font = wb.createFont();;//Create font
		font.setBold(true);//Make font bold
		boldstyle.setFont(font);//set it to bold
		
		Row row=sheet.createRow(0);
		
		Cell cell = row.createCell(0);
		cell.setCellValue("Training set");
		cell.setCellStyle(boldstyle);


		cell = row.createCell(1);
		cell.setCellValue("Method");
		cell.setCellStyle(boldstyle);
		
		cell = row.createCell(2);
		cell.setCellValue("nTrain");
		cell.setCellStyle(boldstyle);

		cell = row.createCell(3);
		cell.setCellValue("nDescriptors");
		cell.setCellStyle(boldstyle);

		cell = row.createCell(4);
		cell.setCellValue("Descriptor");
		cell.setCellStyle(boldstyle);
		
		cell = row.createCell(5);
		cell.setCellValue("Definition");
		cell.setCellStyle(boldstyle);
		
		cell = row.createCell(6);
		cell.setCellValue("Class");
		cell.setCellStyle(boldstyle);

		cell = row.createCell(7);
		cell.setCellValue("Reasoning");
		cell.setCellStyle(boldstyle);
	}


	private int addDescriptorTable(Hashtable<String, String> htDefs, Hashtable<String, String> htClasses, Workbook wb, String sheetName,String methodName,
			String[] descriptorNames, int rowNum) {

		Sheet sheet=wb.getSheet(sheetName);
		
		createHeaderRow(sheet,wb);

		for (int i=0;i<descriptorNames.length;i++) {
			rowNum++;
			Row row=sheet.getRow(rowNum);
			if(row==null) {
				row=sheet.createRow(rowNum);
			}
			
			Cell cell;
			
			if(i==0) {
				cell = row.createCell(0);
				cell.setCellValue(methodName.toUpperCase());
			}
			
			cell = row.createCell(1);
			cell.setCellValue(descriptorNames[i]);

			cell = row.createCell(2);
			cell.setCellValue(htDefs.get(descriptorNames[i]));

			cell = row.createCell(3);
			cell.setCellValue(htClasses.get(descriptorNames[i]));
			
//						System.out.println(descriptorNames[i]+"\t"+htDefinitions.get(descriptorNames[i]));
			
		}
		return rowNum+2;
	}
	
	
	private int addDescriptorTable2(Hashtable<String, String> htDefs, Hashtable<String, String> htClasses, Workbook wb, String sheetName,String methodName,
			String trainingSet,int nTrain, String[] descriptorNames, int rowNum) {

		
		Sheet sheet=wb.getSheet(sheetName);
		
		for (int i=0;i<descriptorNames.length;i++) {
			rowNum++;
			
			Row row=sheet.getRow(rowNum);

			if(row==null) {
				row=sheet.createRow(rowNum);
			}
			
			Cell cell;
			

			
			if(i==0) {
				cell = row.createCell(0);
				cell.setCellValue(trainingSet);
				
				cell = row.createCell(1);
				cell.setCellValue(methodName.toUpperCase());

				cell = row.createCell(2);
				cell.setCellValue(nTrain);
				
				cell = row.createCell(3);
				cell.setCellValue(descriptorNames.length);


			} 
			
			cell = row.createCell(4);
			cell.setCellValue(descriptorNames[i]);

			cell = row.createCell(5);
			cell.setCellValue(htDefs.get(descriptorNames[i]));

			cell = row.createCell(6);
			cell.setCellValue(htClasses.get(descriptorNames[i]));
			
//						System.out.println(descriptorNames[i]+"\t"+htDefinitions.get(descriptorNames[i]));
			
			
		}
		return rowNum+1;
	}
	
	
	public static void main(String[] args) {
		EmbeddingSummaryScript e=new EmbeddingSummaryScript();
//		e.createSummary();
		e.createSummarySingleFile();
	}
	

}
