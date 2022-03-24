package gov.epa.endpoints.datasets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesServiceImpl;
import gov.epa.web_services.descriptors.SciDataExpertsDescriptorWebService;
import gov.epa.web_services.descriptors.SciDataExpertsDescriptorWebService.SciDataExpertsChemical;
import gov.epa.web_services.descriptors.SciDataExpertsDescriptorWebService.SciDataExpertsDescriptorResponse;

public class SciDataExpertsDescriptorValuesCalculator {
	
	private DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();
	private DescriptorValuesService descriptorValuesService = new DescriptorValuesServiceImpl();
	private DataPointService dataPointService = new DataPointServiceImpl();
	
	private SciDataExpertsDescriptorWebService descriptorWebService;
	private String lanId;
	
	private Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	public SciDataExpertsDescriptorValuesCalculator(String sciDataExpertsUrl, String lanId) {
		this.descriptorWebService = new SciDataExpertsDescriptorWebService(sciDataExpertsUrl);
		this.lanId = lanId;
	}
	
	public void calculateDescriptors(String datasetName, String descriptorSetName) {
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);
		List<String> canonQsarSmilesToCalculate = new ArrayList<String>();
		for (DataPoint dp:dataPoints) {
			String canonQsarSmiles = dp.getCanonQsarSmiles();
			if (dp.getCanonQsarSmiles()==null) {
				// Don't calculate descriptors on compounds without standardization
				continue;
			} 
			
			DescriptorValues descriptorValues = descriptorValuesService
					.findByCanonQsarSmilesAndDescriptorSetName(canonQsarSmiles, descriptorSetName);
			if (descriptorValues==null) {
				canonQsarSmilesToCalculate.add(canonQsarSmiles);
			}
		}
		
		calculateDescriptors(canonQsarSmilesToCalculate, descriptorSetName);
	}
	
	public void calculateDescriptors(List<String> canonQsarSmilesToCalculate, String descriptorSetName) {
		DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);
		if (descriptorSet==null) {
			System.out.println("No such descriptor set: " + descriptorSetName);
		}
		
		String descriptorService = descriptorSet.getDescriptorService();
		String descriptorServiceOptionsStr = descriptorSet.getDescriptorServiceOptions();
		Map<String, Object> descriptorServiceOptions = new HashMap<String, Object>();
		if (descriptorSet.getDescriptorServiceOptions()!=null) {
			JsonObject jo = gson.fromJson(descriptorServiceOptionsStr, JsonObject.class);
			for (String key:jo.keySet()) {
				descriptorServiceOptions.put(key, jo.get(key));
			}
		}
		
		// Calculate descriptors
		SciDataExpertsDescriptorResponse response = null;
		if (descriptorServiceOptions.isEmpty()) {
			response = descriptorWebService.calculateDescriptors(canonQsarSmilesToCalculate, descriptorService).getBody();
		} else {
			if (descriptorSet.getHeadersTsv()==null && descriptorServiceOptions.containsKey("headers")) {
				Map<String, Object> descriptorServiceOptionsWithHeaders = new HashMap<String, Object>(descriptorServiceOptions);
				descriptorServiceOptionsWithHeaders.put("headers", true);
				
				response = descriptorWebService
						.calculateDescriptorsWithOptions(canonQsarSmilesToCalculate, descriptorService, 
								descriptorServiceOptionsWithHeaders).getBody();
				
				if (response.headers!=null) {
					String headersTsv = String.join("\t", response.headers);
//					System.out.println(headersTsv);
					descriptorSet.setHeadersTsv(headersTsv);
					descriptorSet = descriptorSetService.update(descriptorSet);
				}
			} else {
				response = descriptorWebService
						.calculateDescriptorsWithOptions(canonQsarSmilesToCalculate, descriptorService, descriptorServiceOptions).getBody();
			}
		}
		
//		System.out.println(gson.toJson(response));
		
		// Store descriptors
		// Store null or failed descriptors so we don't keep trying to calculate them every time
		if (response!=null) {
			List<SciDataExpertsChemical> chemicals = response.chemicals;
			if (chemicals!=null) {
				for (SciDataExpertsChemical chemical:chemicals) {
					String valuesTsv = null;
					if (chemical.descriptors!=null) {
						valuesTsv = String.join("\t", chemical.descriptors.stream()
								.map(d -> String.valueOf(d))
								.collect(Collectors.toList()));
					}
					
					// If descriptor calculation failed, set null so we can check easily when we try to use them later
					if (valuesTsv.contains("Error")) {
						valuesTsv = null;
					}
					
					DescriptorValues descriptorValues = new DescriptorValues(chemical.smiles, descriptorSet, valuesTsv, lanId);
					try {
						descriptorValuesService.create(descriptorValues);
					} catch (ConstraintViolationException e) {
						System.out.println(e.getMessage());
					}
				}
			}
		}
	}
}
