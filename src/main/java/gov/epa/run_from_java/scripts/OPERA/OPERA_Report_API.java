package gov.epa.run_from_java.scripts.OPERA;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedADEstimate;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.QsarPredictedADEstimateServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.QsarPredictedNeighborServiceImpl;
import gov.epa.run_from_java.scripts.SqlUtilities;

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
	 * @return
	 */
	OPERA_Report getOperaReportFromPredictionReport(String id,String modelName) {
		
		String idCol="dtxcid";
		if (id.contains("SID")) idCol="dtxsid";
		
				
		String sql="select file from qsar_models.prediction_reports pr\r\n"
				+ "join qsar_models.predictions_dashboard pd on pr.fk_prediction_dashboard_id = pd.id\r\n"
				+ "join qsar_models.models m on pd.fk_model_id = m.id\r\n"
				+ "join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id\r\n"
				+ "where dr."+idCol+"='"+id+"' and dr.fk_dsstox_snapshot_id=1 and m.name='"+modelName+"';";
				
		try {
			ResultSet rs=SqlUtilities.runSQL2(conn, sql);

			if (rs.next()) {
				String json=new String(rs.getBytes(1));
				return OPERA_Report.fromJson(json);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}

	/**
	 * Assembles OPERA report from several tables in database on the fly
	 * 
	 * @param id
	 * @param modelName
	 * @return
	 */
	OPERA_Report getOperaReportFromPredictionDashboard(String id,String modelName,boolean useModelImageAPI) {
		
		String idCol="dtxcid";
		if (id.contains("SID")) idCol="dtxsid";
		
				
		String sql="Select id from qsar_models.models m where m.name='"+modelName+"'";
		Long modelId=	Long.parseLong(SqlUtilities.runSQL(conn, sql));
		sql="Select id from qsar_models.dsstox_records dr where dr."+idCol+"='"+id+"' and dr.fk_dsstox_snapshot_id=1";
		Long dsstoxRecordId=Long.parseLong(SqlUtilities.runSQL(conn, sql));
		
		PredictionDashboard pd=pds.findByIds(modelId, dsstoxRecordId);
//		System.out.println(pd.getQsarPredictedNeighbors().size());
//		System.out.println(pd.getDsstoxRecord().getDtxcid()+"\t"+pd.getId());
		
		pd.setQsarPredictedNeighbors(qpns.findById(pd.getId()));
		pd.setQsarPredictedADEstimates(qpas.findById(pd.getId()));


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
		
		String unitAbbreviation=dataset.getUnitContributor().getAbbreviation();
		OPERA_Report or=new OPERA_Report(pd,unitAbbreviation,useModelImageAPI);
		
		return or;
		
	}
	
	
	
	public static void main(String[] args) {
		OPERA_Report_API o=new OPERA_Report_API();
		
//		String id="DTXCID101";
		String id="DTXSID7020001";
		
//		String modelName="OPERA2.9_CACO2";
//		String modelName="OPERA2.9_WS";
		
		String [] modelNames= {"OPERA2.9_WS","OPERA2.9_VP","OPERA2.9_MP"};
		
		String folder="data\\opera\\reports";
		
		boolean useModelImageAPI=true;//set to false for testing purposes to get a viewable image
				
		for (String modelName:modelNames) {
			System.out.println(modelName);
			OPERA_Report or=o.getOperaReportFromPredictionDashboard(id,modelName,useModelImageAPI);
//			OPERA_Report or=o.getOperaReportFromPredictionReport(id,modelName);
			String filename=or.chemicalIdentifiers.dtxcid+"_"+or.modelDetails.modelName+".html";
			or.toHTMLFile(folder,filename);
			or.viewInWebBrowser(folder+File.separator+filename);
		}
	}	

}
