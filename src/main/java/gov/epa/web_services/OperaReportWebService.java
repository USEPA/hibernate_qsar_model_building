package gov.epa.web_services;

import java.sql.Connection;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedADEstimate;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.QsarPredictedADEstimateServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.QsarPredictedNeighborServiceImpl;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.OPERA.OPERA_Report;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 * @author TMARTI02
 */
public class OperaReportWebService extends WebService {


	
	
	/**
	 * 
	 * @param modelName: e.g. OPERA2.9_WS
	 * @param id: DTXSID or DTXCID
	 * 
	 * @return
	 */
	public HttpResponse<String> getOperaReport(String modelName, String id) {
		
		HttpResponse<String> response = Unirest.get(address+"/opera_report/{model}/{id}")
				.routeParam("id", id)
				.routeParam("model", modelName)
				.asString();

		//TODO create spring boot application
		return response;
	}


	public OperaReportWebService(String server, int port) {
		super(server, port);
	}


	public static void main(String[] args) {
		OperaReportWebService m=new OperaReportWebService("http://localhost",5100);
	}
}
