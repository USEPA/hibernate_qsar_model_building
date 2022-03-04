package gov.epa.run_from_java.scripts;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSetReport;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelQmrfServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetReportServiceImpl;
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
		
		
		String htmlPath="dummy qmrf.html";
		String pdfPath=FileUtils.replaceExtension(htmlPath, ".pdf");
		
		for (ModelSetTableRow modelSetTableRow:table.modelSetTableRows) {			
			//create prediction excel
			
			for (ModelMetadata modelMetadata:modelSetTableRow.modelMetadata) {
				
				try {
					generateSampleQMRF(modelSetTableRow, modelMetadata, htmlPath);				
					HtmlUtils.HtmlToPdf(htmlPath, pdfPath);
					script.uploadModelQmrf(modelMetadata.modelId, pdfPath);
					
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				
//				if (true) break;
			}

			
//			if (true) break;
		}		
	}
	
	public void generateSamplePredictionReports(long modelSetID) {
		ExcelPredictionReportGenerator eprg=new ExcelPredictionReportGenerator();
		ModelSetTable table=getModelsInModelSet(modelSetID);
		
		ModelSetReportServiceImpl m2=new ModelSetReportServiceImpl();
		
//		System.out.println(getJson(table));
		
		QsarModelsScript script = new QsarModelsScript("tmarti02");
		PredictionReportGenerator p=new PredictionReportGenerator();
				
		ModelSetServiceImpl m=new ModelSetServiceImpl();		
		String modelSetName=m.findById(modelSetID).getName();
		
		for (ModelSetTableRow modelSetTableRow:table.modelSetTableRows) {			
			
			String filepath="predictionReport.xlsx";
				
			System.out.println(modelSetTableRow.datasetName);
			
			try {
				
				ModelSetReport msr=m2.findByModelSetIdAndModelData(modelSetID, modelSetTableRow.datasetName, 
						modelSetTableRow.descriptorSetName,modelSetTableRow.splittingName);
				
				if (msr!=null) continue;//skip it we already did it
				

				//Cant seem to generate prediction reports for large data sets:
//				if (modelSetTableRow.datasetName.equals("Melting point OPERA")) continue;
//				if (modelSetTableRow.datasetName.equals("Octanol water partition coefficient OPERA")) continue;
//				if (modelSetTableRow.datasetName.equals("LD50 TEST")) continue;
//				if (modelSetTableRow.datasetName.equals("Standard Water solubility from exp_prop")) continue;
				
								
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
		g.generateSamplePredictionReports(2L);
		

		//**************************************************************
//		QsarModelsScript q=new QsarModelsScript("tmarti02");
//		q.downloadModelQmrf(152L, "data/reports/qmrf");

		//**************************************************************
//		QsarModelsScript q=new QsarModelsScript("tmarti02");
//		String datasetName="LC50DM TEST";
//		String descriptorSetName="T.E.S.T. 5.1";
//		String splittingName="TEST";
//		q.downloadModelSetReport(1L, datasetName, descriptorSetName, splittingName, "data/reports/prediction reports");

		//**************************************************************
		QsarModelsScript q=new QsarModelsScript("tmarti02");
//		String datasetName="Standard Henry's law constant from exp_prop";
		String datasetName="Standard Water solubility from exp_prop";
		String descriptorSetName="T.E.S.T. 5.1";
		String splittingName="RND_REPRESENTATIVE";
		q.downloadModelSetReport(2L, datasetName, descriptorSetName, splittingName, "data/reports/prediction reports");

		
	}

}
