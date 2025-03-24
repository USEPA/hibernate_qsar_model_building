package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import gov.epa.ghs_data_gathering.Parse.ToxVal.SqlUtilities;
import gov.epa.run_from_java.scripts.QsarModelsScript;

/**
* @author TMARTI02
*/
public class ExportModelFiles {

	
	public class ModelFile {

		public Long id;
		public Long file_type_id;
		public Long model_id;
		public String file_type_name;
		public String model_name;
		public String model_source;
		public String property_name;
		public String export_date;
		public byte[] file_bytes;
		public String data_version;
	}
	
	void exportFromModelFileView () {
		String sql="select * from public.mv_model_files mf;";
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		String urlBase="https://ctx-api-dev.ccte.epa.gov/chemical/property/model/file/search/";
		try {
			
			String folder="data\\reports\\model files download\\";
			FileWriter fw=new FileWriter(folder+"test images.html");
			FileWriter fw2=new FileWriter(folder+"test files.html");
			
			while (rs.next()) {
				ModelFile mf=new ModelFile();
				SqlUtilities.createRecord(rs, mf);//uses reflection to store rs into 
				
				if(mf.file_type_name.contains("plot")) {
					String url=urlBase+"?modelId="+mf.model_id+"&typeId="+mf.file_type_id;
					
					fw.write(mf.model_name+"\t"+mf.file_type_name+"<br>");
					fw.write("<img src=\""+url+"\" height=200 alt=\"\"><br><br>\n");
				} else {
					String url=urlBase+"?modelId="+mf.model_id+"&typeId="+mf.file_type_id;
					fw2.write("<a href=\""+url+"\">"+mf.model_name+"\t"+mf.file_type_name+"</a><br><br>\n");
				}
				
				
//				if(mf.model_id==1070 && mf.file_type_name.equals("QMRF")) {
				if(mf.model_name.contains("Martin") && mf.file_type_name.equals("QMRF")) {
					String filepath=folder+mf.model_name+" "+mf.file_type_name+".pdf";
					QsarModelsScript.safelyWriteBytes(filepath, mf.file_bytes, true);
					
				}
				
				if(mf.model_name.contains("Martin") && mf.file_type_name.equals("Excel summary")) {
					String filepath=folder+mf.model_name+" "+mf.file_type_name+".xlsx";
					QsarModelsScript.safelyWriteBytes(filepath, mf.file_bytes, false);
					
				}

				
				System.out.println(mf.file_type_id+"\t"+  mf.file_type_name+"\t"+mf.model_name);

//				System.out.println(Utilities.gson.toJson(mf));
			}
			
			fw.flush();
			fw.close();
			
			fw2.flush();
			fw2.close();

			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	public static void main(String[] args) {
		ExportModelFiles e=new ExportModelFiles();
		e.exportFromModelFileView();

	}

}
