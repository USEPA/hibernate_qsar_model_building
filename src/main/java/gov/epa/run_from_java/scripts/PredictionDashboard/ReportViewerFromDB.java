package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;
import gov.epa.run_from_java.scripts.SqlUtilities;

//import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport;
import gov.epa.util.JsonUtilities;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport;
/**
* @author TMARTI02
*/
public class ReportViewerFromDB {


	static TreeMap<String, Dataset>mapDatasets=CreatorScript.getDatasetsMap();//TODO get this with SQL instead of hibernate

	
	static List<PredictionReport>getPredictionReports(String sourceName, String id) {
				
		List<PredictionReport>prs= new ArrayList<>();
		
		String sql = """
					select pr.file_json, pr.file_html from qsar_models.predictions_dashboard pd
					join qsar_models.dsstox_records dr on pd.dtxcid =dr.dtxcid
					join qsar_models.prediction_reports pr on pr.fk_predictions_dashboard_id =pd.id
					join qsar_models.models m on m.id=pd.fk_model_id 
					join qsar_models.sources s on s.id = m.fk_source_id 
				""";

		if (id.contains("DTXSID")) {
			sql+="where dr.fk_dsstox_snapshot_id =4 and dr.dtxsid ='"+id+"' and s.\"name\" ='"+sourceName+"';";
		} else if (id.contains("DTXCID")) {
			sql+="where dr.fk_dsstox_snapshot_id =4 and dr.dtxcid ='"+id+"' and s.\"name\" ='"+sourceName+"';";
		}
//		System.out.println(sql);
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		
		try {
			while (rs.next()) {
				PredictionReport pr=new PredictionReport();
				pr.setFileJson(rs.getBytes(1));
				pr.setFileHtml(rs.getBytes(2));
				prs.add(pr);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return prs;
		
	}
	
	
	
	
	/**
	 * Get the html report and display as tabbed webpage
	 * 
	 * @param sourceName
	 * @param dtxsid
	 */
	public static void viewReportsFromDatabase(String sourceName,String id) {
		
		PredictionDashboardServiceImpl pdsi=new PredictionDashboardServiceImpl();

		
		System.out.println("Retrieving reports...");
		List<PredictionReport>prs=getPredictionReports(sourceName, id);
		System.out.println("Done retrieving reports");

		HTMLReportCreator hrc=new HTMLReportCreator();
    	String title=sourceName+" predictions for "+id;
//    	String html = hrc.writeTabbedWebpage(title, pds, mapDatasets);
    	String html = hrc.writeTabbedWebpage(title, prs);
		String folder="data\\"+sourceName+"\\reports\\"+id+"\\";
		String filename=id+".html";

		File DF=new File(folder);
		if(!DF.exists())DF.mkdirs();

		HTMLReportCreator.writeStringToFile(html, folder, filename);
		HTMLReportCreator.viewInWebBrowser(folder+filename);
		
//		for(PredictionDashboard pd:pds) {
//			System.out.println(pd.toJson());
//		}
		
	}

	
	public static void main(String[] args) {

//		viewReportsFromDatabase("TEST5.1.3","DTXSID7020182");
//		viewReportsFromDatabase("TEST5.1.3","DTXSID80161840");
//		viewReportsFromDatabase("OPERA2.8","DTXSID7020182");
//		viewReportsFromDatabase("OPERA2.8","DTXSID9025114");
//		viewReportsFromDatabase("OPERA2.8","DTXSID8024498");
//		viewReportsFromDatabase("OPERA2.8","DTXSID3039242");//benzene
//		viewReportsFromDatabase("TEST5.1.3", "DTXCID5050");//salt
//		viewReportsFromDatabase("TEST5.1.3", "DTXCID80218060");
		
//		viewReportsFromDatabase("OPERA2.8","DTXSID6020482");
//		viewReportsFromDatabase("OPERA2.8","DTXSID7022041");// has opera exp values
		
		viewReportsFromDatabase("OPERA2.8","DTXCID301865892");// has opera exp values
		
		
		

	}

}
