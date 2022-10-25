package gov.epa.run_from_java.scripts;


import java.util.Hashtable;
import javax.validation.ConstraintViolationException;

import com.google.gson.Gson;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.endpoints.models.ModelBuilder;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportDataPoint;
import gov.epa.endpoints.reports.predictions.QsarPredictedValue;
import gov.epa.web_services.ModelWebService;
import kong.unirest.Unirest;


public class RerunPredictions {

	
	/**
	 * Check if rerunning test set gives same result as values in the database
	 * 
	 * @param modelId
	 * @throws ConstraintViolationException
	 */
	public void predict(long modelId)  {

		ModelBuilder mb=new ModelBuilder("tmarti02");
		ModelServiceImpl msi=new ModelServiceImpl();
		Model model=msi.findById(modelId);
//		System.out.println(model.getDatasetName());
		
		//Get training and test set instances as strings using TEST descriptors:
		ModelData md=mb.initModelData(model.getDatasetName(), model.getDescriptorSetName(),model.getSplittingName(), false);
//		System.out.print(md.predictionSetInstances);
		
		ModelBytesService modelBytesService = new ModelBytesServiceImpl();
			
		ModelBytes modelBytes = modelBytesService.findByModelId(modelId);
		
		byte[] bytes = modelBytes.getBytes();
				
//		System.out.println(bytes.length);
		
//		String modelWsServer=DevQsarConstants.SERVER_819;
		String modelWsServer=DevQsarConstants.SERVER_LOCAL;
		int modelWsPort=DevQsarConstants.PORT_PYTHON_MODEL_BUILDING;
		
		
		ModelWebService modelWs = new ModelWebService(modelWsServer, modelWsPort);
		
		String strModelId = String.valueOf(modelId);
		String response= modelWs.callInit(bytes, model.getMethod().getName(), strModelId).getBody();
		
//		System.out.println(response);
		
		
		String modelSetName="Sample models T.E.S.T. 5.1 descriptors";	//TODO pass this variable	
		PredictionReport pr=ReportGenerationScript.reportAllPredictions(model.getDatasetName(), model.getSplittingName(),modelSetName,true);
		Hashtable <String,Double>htDB=new Hashtable<>();
		
		for(PredictionReportDataPoint prdp:	pr.predictionReportDataPoints) {
			for (QsarPredictedValue qpv:prdp.qsarPredictedValues) {
				
				if (!qpv.qsarMethodName.equals(model.getMethod().getName())) continue;
				if (qpv.splitNum!=1) continue;
				if (qpv.qsarPredictedValue==null) continue;
				
//				System.out.println(prdp.canonQsarSmiles+"\t"+qpv.qsarPredictedValue);
				htDB.put(prdp.canonQsarSmiles,qpv.qsarPredictedValue);				
			}
		}

		String predictResponse = modelWs.callPredict(md.predictionSetInstances, model.getMethod().getName(), strModelId).getBody();
		System.out.println("predictResponse="+predictResponse);
		
		Gson gson=new Gson();

		ModelPrediction[] modelPredictions = gson.fromJson(predictResponse, ModelPrediction[].class);

		for (ModelPrediction mp:modelPredictions) {
			System.out.println(mp.ID+"\t"+mp.pred+"\t"+htDB.get(mp.ID));
		}
	}
	public static void main(String[] args) {
		Unirest.config().connectTimeout(0).socketTimeout(0);
		
		RerunPredictions r=new RerunPredictions();
//		r.predict(136L);
		r.predict(62L);
	}

}
