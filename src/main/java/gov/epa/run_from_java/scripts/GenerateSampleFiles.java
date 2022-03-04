package gov.epa.run_from_java.scripts;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.qsar_models.service.ModelQmrfServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetServiceImpl;
import gov.epa.endpoints.reports.ModelMetadata;
import gov.epa.endpoints.reports.model_sets.ModelSetTable;
import gov.epa.endpoints.reports.model_sets.ModelSetTable.ModelSetTableRow;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.endpoints.reports.predictions.PredictionReportGenerator;
import gov.epa.endpoints.reports.predictions.ExcelReports.ExcelPredictionReportGenerator;
import gov.epa.endpoints.reports.model_sets.ModelSetTableGenerator;
import gov.epa.util.FileUtils;
import gov.epa.util.HtmlUtils;

public class GenerateSampleFiles {

	public ModelSetTable getModelsInModelSet(long modelSet) {
		
		ModelSetTableGenerator gen = new ModelSetTableGenerator();
		ModelSetTable table = gen.generate(modelSet);
		return table;
	}
	
	public static String getJson(Object object) {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		return gson.toJson(object);
	}
	
	String getModelRowMetadata(ModelSetTableRow row) {
		String [] names={"propertyName","propertyDescription","datasetName","datasetDescription","datasetUnitName", "descriptorSetName"};		
		String m = getFieldValuesHTML(row, names);		
		return m;
	}
	
	
	String getMethodMetadata(ModelMetadata row) {
		String [] names={"qsarMethodName","qsarMethodDescription"};		
		String m = getFieldValuesHTML(row, names);		
		return m;
	}

	private String getFieldValues(Object obj, String[] names) {
		String m="";
		
		for (int i=0;i<names.length;i++) {
			Field field;
			try {
				field = obj.getClass().getField(names[i]);
				m+=names[i]+"\t"+field.get(obj)+"\n";
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
		}
		return m;
	}
	
	
	private String getFieldValuesHTML(Object obj, String[] names) {
		String m="";
		
		for (int i=0;i<names.length;i++) {
			Field field;
			try {				
				m+="<tr>\n";				
				field = obj.getClass().getField(names[i]);
				m+="<td>"+names[i]+"</td>";
				m+="<td>"+field.get(obj)+"</td>";
				m+="</tr>\n";
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
		}
		return m;
	}
	
	
	void generateSampleQMRF(ModelSetTableRow modelSetTableRow, ModelMetadata modelMetadata,String filepath) {
		
		try {
			
			FileWriter fw=new FileWriter(filepath);
			
			fw.write("<html>");
			
			fw.write("<h3>Dummy QMRF Document</h3>");
			
            fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");            
            fw.write("<tr bgcolor=\"#D3D3D3\">\n");

			fw.write("<th>Parameter</th>");
			fw.write("<th>Value</th>");
			fw.write("</tr>");

			fw.write(getModelRowMetadata(modelSetTableRow));
			fw.write(getMethodMetadata(modelMetadata));
			
			
			fw.write("</table>");
			
			fw.write("</html>");
			
			fw.flush();
			
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	public void generateSampleQMRFs(long modelSetID) {
				
		QsarModelsScript script = new QsarModelsScript("tmarti02");
				
		ModelSetTable table=getModelsInModelSet(modelSetID);		
		
//		ModelQmrfServiceImpl m=new ModelQmrfServiceImpl();
//		m.delete(m.findByModelId(1L)); 
		
		for (ModelSetTableRow modelSetTableRow:table.modelSetTableRows) {			
			//create prediction excel
			
			
			for (ModelMetadata modelMetadata:modelSetTableRow.modelMetadata) {
				
				String htmlPath="dummy qmrf.html";
				String pdfPath=FileUtils.replaceExtension(htmlPath, ".pdf");
				
				try {
					generateSampleQMRF(modelSetTableRow, modelMetadata, htmlPath);				
					HtmlUtils.HtmlToPdf(htmlPath, pdfPath);
					script.uploadModelQmrf(modelMetadata.modelId, pdfPath);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
//				if (true) break;
			}
//			if (true) break;
		}		
	}
	
	public void generateSamplePredictionReports(long modelSetID) {
		ExcelPredictionReportGenerator eprg=new ExcelPredictionReportGenerator();
		ModelSetTable table=getModelsInModelSet(modelSetID);		
//		System.out.println(getJson(table));
		
		QsarModelsScript script = new QsarModelsScript("tmarti02");
		PredictionReportGenerator p=new PredictionReportGenerator();
				
		ModelSetServiceImpl m=new ModelSetServiceImpl();		
		String modelSetName=m.findById(modelSetID).getName();
		
		for (ModelSetTableRow modelSetTableRow:table.modelSetTableRows) {			
			
			String filepath="predictionReport.xlsx";
									
			try {
				PredictionReport predictionReport=p.generateForModelSetPredictions(modelSetTableRow.datasetName,modelSetTableRow.descriptorSetName, modelSetTableRow.splittingName,modelSetName);				
				eprg.generate(predictionReport, filepath);
				script.uploadModelSetReport(modelSetID, modelSetTableRow.datasetName, modelSetTableRow.descriptorSetName, modelSetTableRow.splittingName,filepath);
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				System.out.println(ex.getMessage());
			}
			
//			if (true) break;
		}		
	}
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GenerateSampleFiles g=new GenerateSampleFiles();		
		
//		g.generateSampleQMRFs(1L);
		g.generateSamplePredictionReports(1L);
		

		//**************************************************************
//		QsarModelsScript q=new QsarModelsScript("tmarti02");
//		q.downloadModelQmrf(152L, "data/reports/qmrf");

		//**************************************************************
//		QsarModelsScript q=new QsarModelsScript("tmarti02");
//		String datasetName="LogKOC OPERA";
//		String descriptorSetName="T.E.S.T. 5.1";
//		String splittingName="OPERA";
//		q.downloadModelSetReport(1L, datasetName, descriptorSetName, splittingName, "data/reports/prediction reports");

	}

}
