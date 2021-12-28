package gov.epa.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.epa.builders.model.ModelBuilder;
import gov.epa.builders.prediction_report.PredictionReport;
import gov.epa.builders.prediction_report.PredictionReportBuilder;
import gov.epa.web_services.ModelWebService;

@RestController
public class DevQsarController {
	
	@GetMapping("/models/build")
	public PredictionReport buildModel(@RequestParam(name="model-ws-server") String modelWsServer, 
			@RequestParam(name="model-ws-port") int modelWsPort, 
			@RequestParam(name="dataset-name") String datasetName, 
			@RequestParam(name="descriptor-set-name") String descriptorSetName, 
			@RequestParam(name="splitting-name") String splittingName,
			@RequestParam(name="remove-log-descriptors") boolean removeLogDescriptors, 
			@RequestParam(name="method-name") String methodName, 
			@RequestParam(name="lanid") String lanId) {
		if (!modelWsServer.startsWith("http://")) {
			modelWsServer = "http://" + modelWsServer;
		}
		
		ModelWebService modelWs = new ModelWebService(modelWsServer, modelWsPort);
		ModelBuilder mb = new ModelBuilder(modelWs, lanId);
		Long modelId = mb.build(datasetName, descriptorSetName, splittingName, removeLogDescriptors, methodName);
		
		PredictionReportBuilder prb = new PredictionReportBuilder();
		return prb.buildWithModelPredictions(modelId);
	}
	
	@GetMapping("/reports/model-predictions")
	public PredictionReport reportModelPredictions(@RequestParam(name="model-id") Long modelId) {
		PredictionReportBuilder prb = new PredictionReportBuilder();
		return prb.buildWithModelPredictions(modelId);
	}
	
	@GetMapping("/reports/all-predictions")
	public PredictionReport reportAllPredictions(@RequestParam(name="dataset-name") String datasetName, 
			@RequestParam(name="descriptor-set-name") String descriptorSetName) {
		PredictionReportBuilder prb = new PredictionReportBuilder();
		return prb.buildWithAllPredictions(datasetName, descriptorSetName);
	}

}
