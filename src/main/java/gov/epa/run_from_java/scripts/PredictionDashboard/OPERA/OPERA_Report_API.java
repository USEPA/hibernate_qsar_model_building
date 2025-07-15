package gov.epa.run_from_java.scripts.PredictionDashboard.OPERA;

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
public class OPERA_Report_API {

		
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
	OPERA_Report getOperaReportFromPredictionDashboard(String id,String modelName,boolean useLegacyModelIds,Long dsstoxRecordId) {
		
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
		OPERA_Report or=new OPERA_Report(pd,property,unitAbbreviation,useLegacyModelIds);
		
		String json=Utilities.gson.toJson(or);
		
		System.out.println(json);
		
		gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport pr=new gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport(pd,json,null,user);
		pr.setUpdatedBy(user);
		prs.updateSQL_by_predictionDashboardId(pr);

		
		return or;
		
	}
	
	public void viewReportsFromDatabase(String id,boolean useJson,boolean regenerateReportFromPredictionDashboard, boolean useLegacyModelIds,int fk_dsstox_snapshot_id) {
		
		
		PredictionDashboardScriptOPERA.version="2.8";
		
//		String id="DTXCID1015";
//		String id="DTXSID7020001";
//		String id="DTXCID505";
//		String id="DTXCID101";
		
//		String [] propertyNames= {DevQsarConstants.ORAL_RAT_LD50,
//				DevQsarConstants.WATER_SOLUBILITY,
//				DevQsarConstants.RBIODEG,
//				DevQsarConstants.ANDROGEN_RECEPTOR_AGONIST};

//		String [] propertyNames= {DevQsarConstants.RBIODEG,DevQsarConstants.ANDROGEN_RECEPTOR_AGONIST};

//		String [] propertyNames= {DevQsarConstants.WATER_SOLUBILITY};
		List<String> propertyNames=DevQsarConstants.getOPERA_PropertyNames();
		
		
		String folder="data\\opera2.8\\reports";
		String destFolder=folder+File.separator+id;
		
		File DF=new File(destFolder);
		if(!DF.exists())DF.mkdirs();
		
		HTMLReportCreatorOpera h=new HTMLReportCreatorOpera();
		
		
		Long dsstoxRecordId = getDsstoxRecordId(id, fk_dsstox_snapshot_id);

		
		PredictionDashboardScriptOPERA p=new PredictionDashboardScriptOPERA();
		
		for (String propertyName:propertyNames) {
			
			String modelName=p.initializeDB.getModelName(propertyName);
			
//			if(!modelName.equals("BP OPERA2.8")) continue;
			
			System.out.println(modelName);
			
//			System.out.println(or.modelDetails.modelName);
			
			PredictionReport or=null;


			String filename=id+"_"+modelName+".html";

			
			if(useJson) {
				String json=null;
				
				if(regenerateReportFromPredictionDashboard) {
					or=getOperaReportFromPredictionDashboard(id,modelName,useLegacyModelIds,dsstoxRecordId);
					json=Utilities.gson.toJson(or);
				}
				else {
					json=DatabaseUtilities.getJsonPredictionReport(id,modelName,dsstoxRecordId);
					or=PredictionReport.fromJson(json);
				}
				
				h.toHTMLFile(or, destFolder,filename);
				PredictionReport.viewInWebBrowser(destFolder+File.separator+filename);
				
			} else {
				String html=DatabaseUtilities.getHtmlPredictionReport(id,modelName,dsstoxRecordId);
				HTMLReportCreator.writeStringToFile(html, destFolder, filename);
				PredictionReport.viewInWebBrowser(destFolder+File.separator+filename);
			}

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
		OPERA_Report_API o=new OPERA_Report_API();
		
//		String id="DTXSID80943885";
//		String id="DTXSID50943897";
//		String id="DTXSID301346793";
		
//		String id="DTXSID7020182";//bisphenol-A
		String id="DTXSID3039242";//bz
		boolean regenerateFromPD=false;
		boolean useLegacyModelIds=false;
		int fk_dsstox_snapshot_id=2;
		boolean useJson=false;
		
		o.viewReportsFromDatabase(id,useJson, regenerateFromPD,useLegacyModelIds, fk_dsstox_snapshot_id);
//		o.transposeCSV_Row(id);
		
	}	

}
