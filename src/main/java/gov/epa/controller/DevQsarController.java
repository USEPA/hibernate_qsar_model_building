package gov.epa.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.endpoints.models.ModelBuilder;
import gov.epa.endpoints.reports.descriptors.DescriptorReport;
import gov.epa.endpoints.reports.descriptors.DescriptorReportGenerator;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.endpoints.reports.predictions.PredictionReportGenerator;
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
		
		PredictionReportGenerator gen = new PredictionReportGenerator();
		return gen.generateForModelPredictions(modelId);
	}
	
	@GetMapping("/models/delete")
	public void deleteModel(@RequestParam(name="model-id") Long modelId) {
		ModelService modelService = new ModelServiceImpl();
		Model model = modelService.findById(modelId);
		modelService.delete(model);
	}
	
	@GetMapping("/reports/predictions/model")
	public PredictionReport reportModelPredictions(@RequestParam(name="model-id") Long modelId) {
		PredictionReportGenerator gen = new PredictionReportGenerator();
		return gen.generateForModelPredictions(modelId);
	}
	
	@GetMapping("/reports/predictions/all")
	public PredictionReport reportAllPredictions(@RequestParam(name="dataset-name") String datasetName, 
			@RequestParam(name="descriptor-set-name") String descriptorSetName) {
		PredictionReportGenerator gen = new PredictionReportGenerator();
		return gen.generateForAllPredictions(datasetName, descriptorSetName);
	}
	
	@GetMapping("/reports/descriptors/all")
	public DescriptorReport reportDescriptors(@RequestParam(name="dataset-name") String datasetName) {
		DescriptorReportGenerator gen = new DescriptorReportGenerator();
		return gen.generateForAllDescriptorSets(datasetName);
	}
	
	@GetMapping("/reports/descriptors/set")
	public DescriptorReport reportDescriptors(@RequestParam(name="dataset-name") String datasetName,
			@RequestParam(name="descriptor-set-name") String descriptorSetName) {
		DescriptorReportGenerator gen = new DescriptorReportGenerator();
		return gen.generateForDescriptorSet(datasetName, descriptorSetName);
	}

}
