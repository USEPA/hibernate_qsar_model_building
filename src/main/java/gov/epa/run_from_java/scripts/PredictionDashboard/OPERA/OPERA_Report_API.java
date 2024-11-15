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
import gov.epa.databases.dev_qsar.qsar_models.service.QsarPredictedADEstimateServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.QsarPredictedNeighborServiceImpl;
import gov.epa.run_from_java.scripts.SqlUtilities;
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
	
	/**
	 * Gets report from cached json report in prediction_reports table
	 * 
	 * @param id
	 * @param modelName
	 * @param lookups 
	 * @return
	 */
	PredictionReport getOperaReportFromPredictionReport(String id,String modelName) {
		
		String idCol="dtxcid";
		if (id.contains("SID")) idCol="dtxsid";
		
				
		String sql="select file from qsar_models.prediction_reports pr\r\n"
				+ "join qsar_models.predictions_dashboard pd on pr.fk_predictions_dashboard_id = pd.id\r\n"
				+ "join qsar_models.models m on pd.fk_model_id = m.id\r\n"
				+ "join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id\r\n"
				+ "where dr."+idCol+"='"+id+"' and dr.fk_dsstox_snapshot_id=1 and m.name='"+modelName+"';";
				
		try {
			ResultSet rs=SqlUtilities.runSQL2(conn, sql);

			if (rs.next()) {
				String json=new String(rs.getBytes(1));
				return PredictionReport.fromJson(json);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}

	/**
	 * Assembles OPERA report from several tables in database on the fly
	 * @param lookups 
	 * 
	 * @param id: dtxcid or dtxsid of chemical being looked up
	 * @param modelName: name of model
	 * @return
	 */
	OPERA_Report getOperaReportFromPredictionDashboard(String id,String modelName,boolean useModelImageAPI) {
		
		String idCol="dtxcid";
		if (id.contains("SID")) idCol="dtxsid";
		
				
		String sql="Select m.id from qsar_models.models m\n"+ 
		"join qsar_models.sources s on m.fk_source_id = s.id "+
		"where m.name='"+modelName+"';";

		String strModelID=SqlUtilities.runSQL(conn, sql);
		
		if(strModelID==null) {
			System.out.println("prediction for "+modelName+" and "+id+" not in db");
			return null;
		}
		
		Long modelId=Long.parseLong(strModelID);
		sql="Select id from qsar_models.dsstox_records dr where dr."+idCol+"='"+id+"' and dr.fk_dsstox_snapshot_id=1";
		Long dsstoxRecordId=Long.parseLong(SqlUtilities.runSQL(conn, sql));
		
		System.out.println("modelID="+modelId+",dsstoxRecordId="+dsstoxRecordId);
		
		
		PredictionDashboard pd=pds.findByIds(modelId, dsstoxRecordId);
//		System.out.println(pd.getQsarPredictedNeighbors().size());
//		System.out.println(pd.getDsstoxRecord().getDtxcid()+"\t"+pd.getId());
		
		pd.setQsarPredictedNeighbors(qpns.findById(pd.getId()));//TODO hibernate doesnt seem to automatically populate it
		pd.setQsarPredictedADEstimates(qpas.findById(pd.getId()));//TODO hibernate doesnt seem to automatically populate it


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
		OPERA_Report or=new OPERA_Report(pd,property,unitAbbreviation,useModelImageAPI);
		
		return or;
		
	}
	
	public void viewReportsFromDatabase(String id) {
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
		
		
		String folder="data\\opera\\reports";
		
		boolean regenerateReport=true;
		
		boolean useModelImageAPI=false;//set to false for testing purposes to get a viewable image
		
		HTMLReportCreatorOpera h=new HTMLReportCreatorOpera();
				
		for (String propertyName:propertyNames) {
			
			String modelName=OPERA_csv_to_PostGres_DB.getModelName(propertyName);
//			System.out.println(modelName);
			
			PredictionReport or=null;
			
			if(regenerateReport) or=getOperaReportFromPredictionDashboard(id,modelName,useModelImageAPI);
			else or=getOperaReportFromPredictionReport(id,modelName);
			
			if(or==null) {
				System.out.println("No report for "+propertyName);
				continue;
			}
			
			String filename=or.chemicalIdentifiers.dtxcid+"_"+or.modelDetails.modelName+".html";
			h.toHTMLFile(or, folder,filename);
			or.viewInWebBrowser(folder+File.separator+filename);
		}
		
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
		
		String id="DTXCID505";
		o.viewReportsFromDatabase(id);
//		o.transposeCSV_Row(id);
	}	

}
