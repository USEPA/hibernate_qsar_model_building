package gov.epa.run_from_java.scripts.PredictionDashboard;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionReportServiceImpl;
import gov.epa.run_from_java.scripts.PredictionDashboard.Percepta.Percepta_Report;

/**
* @author TMARTI02
*/
public class UpdateReportsInDatabase {
	PredictionReportServiceImpl pds=new PredictionReportServiceImpl();
	String userName="tmarti02";
	
	void updateReports(String sourceName) {

		
		//		DsstoxCompoundServiceImpl compoundService=new DsstoxCompoundServiceImpl();
		//		List<DsstoxCompound>compounds=compoundService.findAll();

		int batchSize=100000;
		
		int i=0;

		while(true) {
			
			System.out.println(i*batchSize);
			
//			List<PredictionReport>reports=pds.getReportsBySource(sourceName, i*batchSize, batchSize);

			List<String>modelNames=Arrays.asList("ACD_VP","ACD_Prop_Molar_Volume");
			List<PredictionReport>reports=pds.getReportsBySource(sourceName, modelNames, i*batchSize, batchSize);

			if(reports.size()==0) {
				break;
			} else {
				updateReports(reports,sourceName);
				i++;
			}
			
//			if(true)break;
		}

	}
	

	private void updateReports(List<PredictionReport> reports,String sourceName) {
		Gson gson=new Gson();
		
		List<PredictionReport> reportsToUpdate=new ArrayList<>();
		
		int counter=0;
		
		for (PredictionReport pr:reports) {
			counter++;
			String json=new String(pr.getFileJson());
//			System.out.println(json);
			
			if(sourceName.toLowerCase().contains("percepta")) {
				gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport report=Percepta_Report.updatePerceptaReport(json);
				addReportToUpdate(gson, reportsToUpdate, pr, report);
			}
		}
		
//		System.out.println(reportsToUpdate.size());
		pds.updateSQL(reportsToUpdate);
		
	}


	private void addReportToUpdate(Gson gson, List<PredictionReport> reportsToUpdate, PredictionReport pr,
			gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport report) {

		
		if(!report.updated) return;

		String jsonNew=	gson.toJson(report);
		pr.setFileJson(jsonNew.getBytes());
		pr.setUpdatedBy(userName);				
		reportsToUpdate.add(pr);
//		System.out.println(pr.getId()+"\t"+counter+"\t"+jsonNew);

	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		UpdateReportsInDatabase u=new UpdateReportsInDatabase();
		u.updateReports("Percepta2023.1.2");
	}

}
