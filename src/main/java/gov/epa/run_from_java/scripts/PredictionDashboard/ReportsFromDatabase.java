package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionReportServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.OPERA.HTMLReportCreatorOpera;
import gov.epa.run_from_java.scripts.PredictionDashboard.OPERA.PredictionDashboardScriptOPERA;
import gov.epa.run_from_java.scripts.PredictionDashboard.Percepta.HTMLReportCreatorPercepta;

/**
* @author TMARTI02
*/
public class ReportsFromDatabase {

	public void viewReportsFromDatabase(String id,String sourceName) {
		
		
		String folder="data\\"+sourceName+"\\sample reports";
			
		new File(folder).mkdirs();
		
		PredictionReportServiceImpl prs=new PredictionReportServiceImpl();
		
		List<String>jsons=prs.getReportsByDsstoxRecordId(id); 
		
		List<PredictionReport>reports=new ArrayList<>();
		List<String>reportsHTML=new ArrayList<>();
		
		HTMLReportCreatorPercepta reportCreatorPercepta=new HTMLReportCreatorPercepta();
		
		for (String json:jsons) {
			
			PredictionReport pr=Utilities.gson.fromJson(json, PredictionReport.class);
	
			if(!sourceName.equals(pr.modelDetails.modelSourceName)) continue;
			reports.add(pr);
			
			if(sourceName.contains("Percepta")) {
				reportsHTML.add(reportCreatorPercepta.createReport(pr));
			} else {
				System.out.println("Need to call html generator for "+sourceName);
				return;
			}
			
		}
		String htmlAll=combineReports(reportsHTML);
	
		String filename=reports.get(0).chemicalIdentifiers.dtxsid+".html";
		
		PredictionReport.toFile(htmlAll, folder, filename);
		PredictionReport.viewInWebBrowser(folder+File.separator+filename);
		
	}

	String combineReports(List<String>reportsHTML) {
		String htmlAll="";
		for (String htmlReport:reportsHTML) {
			htmlAll+=htmlReport+"<hr>\n";
		}
		return htmlAll;

	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ReportsFromDatabase r=new ReportsFromDatabase();
//		r.viewReportsFromDatabase("DTXSID7020182", "Percepta2023.1.2");
		r.viewReportsFromDatabase("DTXSID40177725", "Percepta2023.1.2");
		
		
	}

}
