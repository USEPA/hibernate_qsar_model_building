package gov.epa.run_from_java.scripts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelQmrf;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSetReport;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelQmrfService;
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

public class SampleModelQmrfWriter {

	public static ModelSetTable getModelsInModelSet(long modelSet) {
		
		ModelSetTableGenerator gen = new ModelSetTableGenerator();
		ModelSetTable table = gen.generate(modelSet);
		return table;
	}
	
	public static String getJson(Object object) {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		return gson.toJson(object);
	}
	
	String getModelRowMetadata(ModelSetTableRow row) {
		String [] names={"propertyName","propertyDescription","datasetName","datasetDescription","datasetUnitName"};		
		String m = getFieldValuesHTML(row, names);		
		return m;
	}
	
	
	String getMethodMetadata(ModelMetadata row) {
		String [] names={"qsarMethodName","qsarMethodDescription","descriptorSetName"};		
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
	
	public void generateSampleQMRFs(long modelSetID,boolean upload) {
				
		QsarModelsScript script = new QsarModelsScript("tmarti02");
				
		ModelSetTable table=getModelsInModelSet(modelSetID);		
		
//		ModelQmrfServiceImpl m=new ModelQmrfServiceImpl();
//		m.delete(m.findByModelId(1L)); 
				
		
		String outputFolder="data/reports/qmrf upload";
		
		File f=new File(outputFolder);
		if (!f.exists()) f.mkdirs();

		
		for (ModelSetTableRow modelSetTableRow:table.modelSetTableRows) {			
			//create prediction excel
			
			for (ModelMetadata modelMetadata:modelSetTableRow.modelMetadata) {
				
				
				String htmlPath = outputFolder + File.separator + String.join("_", modelSetTableRow.datasetName,
						modelSetTableRow.splittingName,
						modelMetadata.qsarMethodName) 
						+ ".pdf";

				String pdfPath=FileUtils.replaceExtension(htmlPath, ".pdf");
								
				
				try {
					generateSampleQMRF(modelSetTableRow, modelMetadata, htmlPath);				
					HtmlUtils.HtmlToPdf(htmlPath, pdfPath);
					if (upload) script.uploadModelQmrf(modelMetadata.modelId, pdfPath);
					
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				
//				if (true) break;
			}

			
//			if (true) break;
		}		
	}
	
	
	
	void deleteOldQMRFs() {
		
		//alternatively run this in DBeaver:
		
		//delete from qsar_models.model_qmrfs ;
		ModelQmrfService service=new ModelQmrfServiceImpl();
		
		for (Long modelID=63L;modelID<=103L;modelID++) {
			
			try {
				ModelQmrf mq=service.findByModelId(modelID);
				service.delete(mq);
			} catch (Exception ex) {
				System.out.println(ex);
			}
		}
		
		
		
		
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SampleModelQmrfWriter g=new SampleModelQmrfWriter();
		
//		g.deleteOldQMRFs();
		
		g.generateSampleQMRFs(1L,true);
//		g.generateSampleQMRFs(2L,true);

//		QsarModelsScript q=new QsarModelsScript("tmarti02");
//		q.downloadModelQmrf(186L, "data/reports/qmrf download");


		
	}

}
