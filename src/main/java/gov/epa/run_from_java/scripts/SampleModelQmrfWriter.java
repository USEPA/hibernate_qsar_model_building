package gov.epa.run_from_java.scripts;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelFile;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelFileServiceImpl;
import gov.epa.endpoints.reports.ModelMetadata;
import gov.epa.endpoints.reports.model_sets.ModelSetTable;
import gov.epa.endpoints.reports.model_sets.ModelSetTable.ModelSetTableRow;
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
		String [] names={"modelId","qsarMethodName","qsarMethodDescription","descriptorSetName"};		
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
	
	public void generateSampleQMRFs(long modelSetID,boolean upload, boolean deleteExistingReportInDatabase) {
				
		QsarModelsScript script = new QsarModelsScript("tmarti02");
				
		ModelSetTable table=getModelsInModelSet(modelSetID);		
		
		ModelFileServiceImpl mqs=new ModelFileServiceImpl();
		
		String outputFolder="data/reports/qmrf upload";
		
		File f=new File(outputFolder);
		if (!f.exists()) f.mkdirs();

		
		for (ModelSetTableRow modelSetTableRow:table.modelSetTableRows) {			
			//create prediction excel
			
			for (ModelMetadata modelMetadata:modelSetTableRow.modelMetadata) {
				
				if (!modelMetadata.qsarMethodName.contains("consensus")) continue;
				
				System.out.println(modelSetTableRow.datasetName+"\t"+modelMetadata.modelId);
				
				
				ModelFile mq=mqs.findByModelId(modelMetadata.modelId,1L);
				
				if (mq != null) {
					
					if (deleteExistingReportInDatabase) {
						mqs.delete(mq);
					} else {
						System.out.println("QMRF for model "+modelMetadata.modelId+" exists skipping!");
						continue;// skip it we already did it						
					}
				}
				
							
				String htmlPath = outputFolder + File.separator + String.join("_", "model"+modelMetadata.modelId, modelSetTableRow.datasetName,
						modelSetTableRow.splittingName,
						modelMetadata.qsarMethodName) 
						+ ".pdf";

				String pdfPath=FileUtils.replaceExtension(htmlPath, ".pdf");
								
				
				try {
					generateSampleQMRF(modelSetTableRow, modelMetadata, htmlPath);				
					HtmlUtils.HtmlToPdf(htmlPath, pdfPath);
					if (upload) script.uploadModelFile(modelMetadata.modelId, 1L, pdfPath);
					
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
//				
//				if (true) break;
			}

			
//			if (true) break;
		}		
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SampleModelQmrfWriter g=new SampleModelQmrfWriter();
		
		g.generateSampleQMRFs(1L,false,false);
//		g.generateSampleQMRFs(2L,true,true);
//		g.generateSampleQMRFs(4L,true,true);


//		QsarModelsScript q=new QsarModelsScript("tmarti02");
//		q.downloadModelQmrf(256L, "data/reports/qmrf download");

		
	}

}
