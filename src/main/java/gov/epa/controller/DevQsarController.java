package gov.epa.controller;

import java.util.Random;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.endpoints.models.ModelBuilder;
import gov.epa.endpoints.reports.descriptors.DescriptorReport;
import gov.epa.endpoints.reports.descriptors.DescriptorReportGenerator;
import gov.epa.endpoints.reports.model_sets.ModelSetTable;
import gov.epa.endpoints.reports.model_sets.ModelSetTableGenerator;
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
	
	@DeleteMapping("/models")
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
	
	/*
	@GetMapping("/reports/predictions/all")
	public PredictionReport reportAllPredictions(@RequestParam(name="dataset-name") String datasetName, 
			@RequestParam(name="descriptor-set-name") String descriptorSetName,
			@RequestParam(name="splitting-name") String splittingName) {
		PredictionReportGenerator gen = new PredictionReportGenerator();
		return gen.generateForAllPredictions(datasetName, descriptorSetName, splittingName);
	}
	*/
	
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
	
	@GetMapping("/reports/model_sets")
	public ModelSetTable reportModelSetTable(@RequestParam(name="model-set-name") String modelSetName) {
		ModelSetTableGenerator gen = new ModelSetTableGenerator();
		return gen.generate(modelSetName);
	}

	
	
	// method argument should be prediction report json rather than predictionreport object right?
	@GetMapping("/download/reports/")
	public ResponseEntity<Resource> downloadExcelPredictionReport() { //@RequestParam(name="prediction-report") PredictionReport predictionReport) {
		byte[] blob = new byte[20];
		new Random().nextBytes(blob);
		ByteArrayResource resource = new ByteArrayResource(blob);
		String fileName = "NonsenseFileName.bin";
		Long fileLength = Long.valueOf(blob.length);
		
		return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(fileLength)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);

	}
}
