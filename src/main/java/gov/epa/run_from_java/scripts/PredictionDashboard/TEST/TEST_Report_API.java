package gov.epa.run_from_java.scripts.PredictionDashboard.TEST;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.TreeMap;

import com.opencsv.CSVReader;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.MethodAD;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionReportServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.QsarPredictedADEstimateServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.QsarPredictedNeighborServiceImpl;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.DatabaseUtilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.HTMLReportCreator;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport;

/**
* @author TMARTI02
*/
public class TEST_Report_API {

		
	DatasetService ds=new DatasetServiceImpl();
	PredictionDashboardServiceImpl pds=new PredictionDashboardServiceImpl();
	QsarPredictedNeighborServiceImpl qpns=new QsarPredictedNeighborServiceImpl();
	QsarPredictedADEstimateServiceImpl qpas=new QsarPredictedADEstimateServiceImpl();
	Connection conn=SqlUtilities.getConnectionPostgres();
	
	PredictionReportServiceImpl prs=new PredictionReportServiceImpl();
	String user="tmarti02";

	/**
	 * Assembles OPERA report from several tables in database on the fly
	 * @param lookups 
	 * 
	 * @param id: dtxcid or dtxsid of chemical being looked up
	 * @param modelName: name of model
	 * @return
	 */
	TEST_Report getReportFromPredictionDashboard(String id,String modelName,boolean useLegacyModelIds,Long dsstoxRecordId) {
		
		String sql="Select m.id from qsar_models.models m\n"+ 
		"join qsar_models.sources s on m.fk_source_id = s.id "+
		"where m.name='"+modelName+"';";
		String strModelID=SqlUtilities.runSQL(conn, sql);
		if(strModelID==null) {
			System.out.println("prediction for "+modelName+" and "+id+" not in db");
			return null;
		}
		Long modelId=Long.parseLong(strModelID);
		System.out.println("modelName="+modelName+", modelID="+modelId+",dsstoxRecordId="+dsstoxRecordId);
		
		
		PredictionDashboard pd=pds.findByIds(modelId, dsstoxRecordId);
//		System.out.println(pd.getQsarPredictedNeighbors().size());
//		System.out.println(pd.getDsstoxRecord().getDtxcid()+"\t"+pd.getId());
		
		
		pd.setQsarPredictedNeighbors(qpns.findById(pd.getId()));//TODO hibernate doesnt seem to automatically populate it
		pd.setQsarPredictedADEstimates(qpas.findById(pd.getId()));//TODO hibernate doesnt seem to automatically populate it

//		System.out.println(pd.getQsarPredictedNeighbors().size());
//		System.out.println(modelId+"\t"+dsstoxRecordId+"\t"+pd.getId());

		
//		for (QsarPredictedNeighbor n:pd.getQsarPredictedNeighbors()) {
//			System.out.println(n.getCasrn());
//		}
//		
//		for (QsarPredictedADEstimate n:pd.getQsarPredictedADEstimates()) {
//			System.out.println(n.getMethodAD().getName()+"\t"+n.getApplicabilityValue());
//		}
		
		String datasetName=pd.getModel().getDatasetName();
		Dataset dataset=ds.findByName(datasetName);
		Property property=dataset.getProperty();
		String unitAbbreviation=dataset.getUnitContributor().getAbbreviation_ccd();
		String unitAbbreviationNeighbor=dataset.getUnit().getAbbreviation_ccd();
		
		//TODO need the plots which arent stored in pd (stored in PredictionResults)
		TEST_Report or=new TEST_Report(pd,null, property,unitAbbreviation,unitAbbreviationNeighbor, useLegacyModelIds);
		
		String json=Utilities.gson.toJson(or);
		
		System.out.println(json);
		
		gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport pr=new gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport(pd,json,null,user);
		pr.setUpdatedBy(user);
		prs.updateSQL_by_predictionDashboardId(pr);

		
		return or;
		
	}
	
	public void viewReportsFromDatabase(String id,boolean useJson, boolean regenerateReport,boolean useLegacyModelIds,int fk_dsstox_snapshot_id) {
				
		String folder="data\\opera\\reports";
		String destFolder=folder+File.separator+id;
		
		File DF=new File(destFolder);
		if(!DF.exists())DF.mkdirs();
		
		HTMLReportCreatorTEST h=new HTMLReportCreatorTEST();
		
		PredictionDashboardScriptTEST p=new PredictionDashboardScriptTEST(); 
		
		
		Long dsstoxRecordId = getDsstoxRecordId(id, fk_dsstox_snapshot_id);

		
		for (String propertyName:DevQsarConstants.getTEST_PropertyNames()) {
			
			String modelName=p.initializeDB.getModelName(propertyName);
//			System.out.println(modelName);
			
			PredictionReport or=null;


//			String json=null;
			
//			if(regenerateReport) {
//				or=getReportFromPredictionDashboard(id,modelName,useLegacyModelIds,dsstoxRecordId);
//				json=Utilities.gson.toJson(or);
//			}
//			else {
//				json=DatabaseUtilities.getJsonPredictionReport(id,modelName,dsstoxRecordId);
//				or=PredictionReport.fromJson(json);
//			}
			
			
			String filename=id+"_"+modelName+".html";
			
			if(useJson) {
				String json=null;
				
				if(regenerateReport) {
					or=getReportFromPredictionDashboard(id,modelName,useLegacyModelIds,dsstoxRecordId);
					json=Utilities.gson.toJson(or);
				}
				else {
					json=DatabaseUtilities.getJsonPredictionReport(id,modelName,dsstoxRecordId);
					or=PredictionReport.fromJson(json);
				}

				String filenameJson=id+"_"+or.modelDetails.modelName+".json";
				Utilities.jsonToPrettyJson(json, destFolder+File.separator+filenameJson.replace(".html", ".json"));

				
				h.toHTMLFile(or, destFolder,filename);
				PredictionReport.viewInWebBrowser(destFolder+File.separator+filename);
				
			} else {
				String html=DatabaseUtilities.getHtmlPredictionReport(id,modelName,dsstoxRecordId);
				
				if(html==null) {
					System.out.println(propertyName+"\tNo html report");
					continue;
				}
				
				HTMLReportCreator.writeStringToFile(html, destFolder, filename);
				PredictionReport.viewInWebBrowser(destFolder+File.separator+filename);
			}
			
			
//			if(or==null) {
//				System.out.println("No report for "+propertyName);
//				continue;
//			}
//
//			String filename=id+"_"+or.modelDetails.modelName+".html";
//			h.toHTMLFile(or, destFolder,filename);
//			or.viewInWebBrowser(destFolder+File.separator+filename);
			
			
			
		}
		
	}

	private Long getDsstoxRecordId(String id, int fk_dsstox_snapshot_id) {
		String idCol="dtxcid";
		if (id.contains("SID")) idCol="dtxsid";
		String sql="Select id from qsar_models.dsstox_records dr where dr."+idCol+"='"+id+"' and dr.fk_dsstox_snapshot_id="+fk_dsstox_snapshot_id;
		Long dsstoxRecordId=Long.parseLong(SqlUtilities.runSQL(conn, sql));
		return dsstoxRecordId;
	}
	
	void transposeCSV_Row(String dtxcid) {

		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.9\\";

		String filepathPredictionCsv=folder+"OPERA2.8_DSSTox_082021_1_first1000.csv";

		try {
			CSVReader reader = new CSVReader(new FileReader(filepathPredictionCsv));
			String []colNames=reader.readNext();


			while (true) {
				String []values=reader.readNext();

				if(values==null) break;

				if(values[0].equals(dtxcid)) {
					for (int i=0;i<colNames.length;i++) {
						System.out.println(colNames[i]+"\t"+values[i]);
					}
					break;
				}

			} 


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		TEST_Report_API o=new TEST_Report_API();
		
//		String id="DTXSID80943885";
//		String id="DTXSID50943897";
//		String id="DTXSID301346793";
		
//		String id="DTXSID7020182";//bisphenol-A
		String id="DTXSID40166952";
//		String id="DTXSID50967078";
//		String id="DTXSID90492637";
		
		boolean useJson=false;
		boolean regenerate=true;
		boolean useLegacyModelIds=true;
		int fk_dsstox_snapshot_id=2;
		o.viewReportsFromDatabase(id,useJson, regenerate,useLegacyModelIds, fk_dsstox_snapshot_id);
//		o.transposeCSV_Row(id);
	}	

}
