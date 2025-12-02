package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.io.File;
import java.util.List;
import java.util.TreeMap;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;

/**
* @author TMARTI02
*/
public class ReportViewerFromDB {

	
	static TreeMap<String, Dataset>mapDatasets=CreatorScript.getDatasetsMap();
	
	/**
	 * Get the html report and display as tabbed webpage
	 * 
	 * @param sourceName
	 * @param dtxsid
	 */
	public static void viewReportsFromDatabase(String sourceName,String dtxsid) {
		
		PredictionDashboardServiceImpl pdsi=new PredictionDashboardServiceImpl();
		List<PredictionDashboard>pds=pdsi.findBySourceNameAndDTXSID(sourceName, dtxsid);

		HTMLReportCreator hrc=new HTMLReportCreator();
    	String title=sourceName+" predictions for "+dtxsid;
    	String html = hrc.writeTabbedWebpage(title, pds,mapDatasets);
		String folder="data\\"+sourceName+"\\reports\\"+dtxsid+"\\";
		String filename=dtxsid+".html";

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
		viewReportsFromDatabase("OPERA2.8","DTXSID8024498");
		

	}

}
