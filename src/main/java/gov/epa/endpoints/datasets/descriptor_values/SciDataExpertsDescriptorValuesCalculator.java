package gov.epa.endpoints.datasets.descriptor_values;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.web_services.descriptors.SciDataExpertsDescriptorWebService;
import gov.epa.web_services.descriptors.SciDataExpertsDescriptorWebService.SciDataExpertsChemical;
import gov.epa.web_services.descriptors.SciDataExpertsDescriptorWebService.SciDataExpertsDescriptorResponse;

public class SciDataExpertsDescriptorValuesCalculator extends DescriptorValuesCalculator {
	
	private SciDataExpertsDescriptorWebService descriptorWebService;
	private Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	public SciDataExpertsDescriptorValuesCalculator(String sciDataExpertsUrl, String lanId) {
		super(lanId);
		this.descriptorWebService = new SciDataExpertsDescriptorWebService(sciDataExpertsUrl);
	}
	
	@Override
	public String calculateDescriptors(String datasetName, String descriptorSetName, boolean writeToDatabase) {
		DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);
		if (descriptorSet==null) {
			System.out.println("No such descriptor set: " + descriptorSetName);
		}
		
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);
		if (dataPoints==null || dataPoints.size()==0) {
			System.out.println("No data points for set: " + datasetName);
		}
		
		List<String> canonQsarSmilesToCalculate = new ArrayList<String>();
		Map<String, String> descriptorsMap = new HashMap<String, String>();
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
			} else {
				descriptorsMap.put(canonQsarSmiles, descriptorValues.getValuesTsv());
			}
		}
		
		calculateDescriptors(canonQsarSmilesToCalculate, descriptorSet, descriptorsMap, writeToDatabase);
		
		return buildTsv(dataPoints, descriptorsMap, descriptorSet);
	}
	
	private void calculateDescriptors(List<String> canonQsarSmilesToCalculate, DescriptorSet descriptorSet, 
			Map<String, String> descriptorsMap, boolean writeToDatabase) {
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
					String headersTsv = String.join(TAB_DEL, response.headers);
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
		if (response!=null) {
			List<SciDataExpertsChemical> chemicals = response.chemicals;
			if (chemicals!=null) {
				for (SciDataExpertsChemical chemical:chemicals) {
					String valuesTsv = null;
					if (chemical.descriptors!=null) {
						valuesTsv = String.join(TAB_DEL, chemical.descriptors.stream()
								.map(d -> String.valueOf(d))
								.collect(Collectors.toList()));
					}
					
					// If descriptor calculation failed, set null so we can check easily when we try to use them later
					if (valuesTsv.contains("Error")) {
						valuesTsv = null;
					}
					
					descriptorsMap.put(chemical.smiles, valuesTsv);
					
					if (writeToDatabase) {
						writeDescriptorValuesToDatabase(chemical.smiles, descriptorSet, valuesTsv);
					}
				}
			}
		}
	}
}
