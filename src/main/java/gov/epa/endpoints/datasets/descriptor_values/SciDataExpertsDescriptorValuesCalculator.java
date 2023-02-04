package gov.epa.endpoints.datasets.descriptor_values;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;
import gov.epa.run_from_java.scripts.GetExpPropInfo.GetExpPropInfo;
import gov.epa.web_services.descriptors.SciDataExpertsDescriptorWebService;
import gov.epa.web_services.descriptors.SciDataExpertsDescriptorWebService.SciDataExpertsChemical;
import gov.epa.web_services.descriptors.SciDataExpertsDescriptorWebService.SciDataExpertsDescriptorRequest;
import gov.epa.web_services.descriptors.SciDataExpertsDescriptorWebService.SciDataExpertsDescriptorResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class SciDataExpertsDescriptorValuesCalculator extends DescriptorValuesCalculator {
	
	private SciDataExpertsDescriptorWebService descriptorWebService;
	private Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	public SciDataExpertsDescriptorValuesCalculator(String sciDataExpertsUrl, String lanId) {
		super(lanId);
		this.descriptorWebService = new SciDataExpertsDescriptorWebService(sciDataExpertsUrl);
	}
	
	public String runSingleChemical(String descriptorSetName,String qsarSmiles) {
		DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);
		
		String descriptorService = descriptorSet.getDescriptorService();
		
//		System.out.println(descriptorService);
		
		String descriptorServiceOptionsStr = descriptorSet.getDescriptorServiceOptions();
		
		Map<String, Object> descriptorServiceOptions = new HashMap<String, Object>();
		if (descriptorSet.getDescriptorServiceOptions()!=null) {
			JsonObject jo = gson.fromJson(descriptorServiceOptionsStr, JsonObject.class);
			for (String key:jo.keySet()) {
				descriptorServiceOptions.put(key, jo.get(key));
//				System.out.println(key+"\t"+jo.get(key));
			}
		}
		
//		descriptorServiceOptions.put("radius", 3);
//		descriptorServiceOptions.put("type", "ecfp");
//		descriptorServiceOptions.put("bits", "1024");
//		descriptorServiceOptions.put("headers", true);
//		descriptorServiceOptions.put("stdizer-workflow", null);
		
		
//		for (Map.Entry<String,Object> entry : descriptorServiceOptions.entrySet()) { 
//            System.out.println("Key = " + entry.getKey() +
//                             ", Value = " + entry.getValue());
//		}
		
		SciDataExpertsDescriptorResponse response = null;
		
		List<String> canonQsarSmilesToCalculate = new ArrayList<String>();		
		canonQsarSmilesToCalculate.add(qsarSmiles);
		
		response = descriptorWebService
				.calculateDescriptorsWithOptions(canonQsarSmilesToCalculate, descriptorService, descriptorServiceOptions).getBody();

		
//		String result = descriptorWebService
//				.calculateDescriptorsWithOptions2(canonQsarSmilesToCalculate, descriptorService, descriptorServiceOptions).getBody();
//		
//		response=gson.fromJson(result,SciDataExpertsDescriptorResponse.class);
//		System.out.println(result);
		
		
		SciDataExpertsChemical chemical=response.chemicals.get(0);
		
//		System.out.println(response.headers.size());
//		System.out.println(chemical.descriptors.size());
		
		String valuesTsv = null;
		if (chemical.descriptors!=null) {
			valuesTsv = String.join(TAB_DEL, chemical.descriptors.stream()
					.map(d -> String.valueOf(d))
					.collect(Collectors.toList()));
									
			if (valuesTsv.contains("Error")) {
				System.out.println("error for\t"+chemical.smiles);
				valuesTsv = null;
			} 

		} else {
			System.out.println("null descriptors for\t"+chemical.smiles);
		}

		
		String [] vals=valuesTsv.split("\t");
		for (int i=0;i<vals.length;i++) {
			
			double val=Double.parseDouble(vals[i]);
			
//			if (val>0)
				System.out.println(i+"\t"+vals[i]);
		}

//		System.out.println(valuesTsv);
		return valuesTsv;		
		
		
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
		
		int counter=0;
		System.out.println(descriptorSetName);
		
		for (DataPoint dp:dataPoints) {
			String canonQsarSmiles = dp.getCanonQsarSmiles();
			if (dp.getCanonQsarSmiles()==null) {
				// Don't calculate descriptors on compounds without standardization
				continue;
			} 
			
			counter++;			
			if (counter%100==0) System.out.println(counter);
			
			DescriptorValues descriptorValues = descriptorValuesService
					.findByCanonQsarSmilesAndDescriptorSetName(canonQsarSmiles, descriptorSetName);
			
					
			if (descriptorValues==null) {
//				System.out.println(canonQsarSmiles);
				canonQsarSmilesToCalculate.add(canonQsarSmiles);
			} else {
				descriptorsMap.put(canonQsarSmiles, descriptorValues.getValuesTsv());
			}
		}
		
		//Run in batches(TMM):
		int count=200;
		
		System.out.println(canonQsarSmilesToCalculate.size()+"\tremaining to run");
		
		while (true) {
			List<String> canonQsarSmilesToCalculate2 = new ArrayList<String>();
			int stop=count;
			if (count>canonQsarSmilesToCalculate.size()) stop=canonQsarSmilesToCalculate.size();
			
			for (int i=0;i<stop;i++) {
				canonQsarSmilesToCalculate2.add(canonQsarSmilesToCalculate.remove(0));
			}
			calculateDescriptors(canonQsarSmilesToCalculate2, descriptorSet, descriptorsMap, writeToDatabase);
			
			System.out.println(canonQsarSmilesToCalculate.size()+"\tremaining to run");
			
			if (canonQsarSmilesToCalculate.size()==0) break;
		}
		
		
//		System.out.println(canonQsarSmilesToCalculate.size());
//		for (String smiles:canonQsarSmilesToCalculate) {
//			System.out.println(smiles);
//		}
		
//		calculateDescriptors(canonQsarSmilesToCalculate, descriptorSet, descriptorsMap, writeToDatabase);
		
		return buildTsv(dataPoints, descriptorsMap, descriptorSet);
	}
	
	
	/**
	 * Quick and dirty method based on sql queries that runs a lot faster since it doesnt have to query the database one chemical at a time

	 * @param datasetName
	 * @param descriptorSetName
	 * @param writeToDatabase
	 */
	public void calculateDescriptors_useSqlToExcludeExisting(String datasetName, String descriptorSetName, boolean writeToDatabase, int batchSize) {
		
		
		
		DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);
		if (descriptorSet==null) {
			System.out.println("No such descriptor set: " + descriptorSetName);
		}
		
		System.out.println("Calculating "+descriptorSetName+" descriptors for "+datasetName);
		
		DatasetService datasetService = new DatasetServiceImpl();		
		Dataset dataset=datasetService.findByName(datasetName);
		
		Long id_descriptor=descriptorSet.getId();
		Long id_dataset=dataset.getId();
				
		Connection conn=DatabaseLookup.getConnectionPostgres();
		
		//gets list of smiles in the dataset
		String sql="select dp.canon_qsar_smiles from qsar_datasets.data_points dp \r\n"
				+ "where dp.fk_dataset_id ="+id_dataset+";";
		
		List<String>canonQsarSmilesToCalculate=new ArrayList<>();
		
		try {
			Statement st = conn.createStatement();			
			ResultSet rs = st.executeQuery(sql);
			while (rs.next()) {
				canonQsarSmilesToCalculate.add(rs.getString(1));
			}
			
//			for (int i=0;i<canonQsarSmilesToCalculate.size();i++) {
//				System.out.println(i+"\t"+canonQsarSmilesToCalculate.get(i));
//			}

			
			//List of ones where we already have descriptors:
			sql="select dp.canon_qsar_smiles from qsar_datasets.data_points dp \r\n"
					+ "			inner join qsar_descriptors.descriptor_values dv \r\n"
					+ "			on dp.canon_qsar_smiles =dv.canon_qsar_smiles \r\n"
					+ "			where dp.fk_dataset_id ="+id_dataset+" and dv.fk_descriptor_set_id ="+id_descriptor+";";
			
			rs = st.executeQuery(sql);
			while (rs.next()) {//Remove the ones that are already in the descriptor values table:
				canonQsarSmilesToCalculate.remove(rs.getString(1));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
//		for (int i=0;i<canonQsarSmilesToCalculate.size();i++) {
//			System.out.println(i+"\t"+canonQsarSmilesToCalculate.get(i));
//		}
		
				
//		//Run in batches(TMM):
		int count=batchSize;		
		System.out.println(canonQsarSmilesToCalculate.size()+"\tremaining to run");
		
		while (canonQsarSmilesToCalculate.size()>0) {
			List<String> canonQsarSmilesToCalculate2 = new ArrayList<String>();
			int stop=count;
			if (count>canonQsarSmilesToCalculate.size()) stop=canonQsarSmilesToCalculate.size();
			
			for (int i=0;i<stop;i++) {
				canonQsarSmilesToCalculate2.add(canonQsarSmilesToCalculate.remove(0));
			}
			
//			System.out.println(canonQsarSmilesToCalculate2.get(0));
			
			calculateDescriptors(canonQsarSmilesToCalculate2, descriptorSet, null, writeToDatabase);			
			System.out.println(canonQsarSmilesToCalculate.size()+"\tremaining to run");			
			if (canonQsarSmilesToCalculate.size()==0) break;
			
//			if(true) break;
			
		}
		
	}

	/**
	 * Run SDE descriptor generation. Workflow is null since we standardized the smiles already and dont want to change it during descriptor generation
	 * 
	 * @param canonQsarSmilesToCalculate
	 * @param descriptorSet
	 * @param descriptorsMap
	 * @param writeToDatabase
	 */
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
//			System.out.println("here1");
			response = descriptorWebService.calculateDescriptors(canonQsarSmilesToCalculate, descriptorService).getBody();
		} else {
			if (descriptorSet.getHeadersTsv()==null && descriptorServiceOptions.containsKey("headers")) {
				Map<String, Object> descriptorServiceOptionsWithHeaders = new HashMap<String, Object>(descriptorServiceOptions);
				descriptorServiceOptionsWithHeaders.put("headers", true);
//				descriptorServiceOptionsWithHeaders.put("stdizer-workflow", "qsar-ready");
//				System.out.println("here2");
				
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
//				System.out.println("here3");
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
						
//						for(Double descriptor:chemical.descriptors) {
//							System.out.println(chemical.smiles+"\t"+descriptor);
//						}
//						System.out.println("");
												
						if (valuesTsv.contains("Error")) {
							System.out.println("error for\t"+chemical.smiles);
							valuesTsv = null;
						} 

					} else {
						System.out.println("null descriptors for\t"+chemical.smiles);
					}
					
					
					// If descriptor calculation failed, set null so we can check easily when we try to use them later
					
					if (descriptorsMap!=null)
						descriptorsMap.put(chemical.smiles, valuesTsv);
					
					if (writeToDatabase) {
//						System.out.println(chemical.smiles+"\t"+valuesTsv);
						writeDescriptorValuesToDatabase(chemical.smiles, descriptorSet, valuesTsv);
					}
				}
			} else {
				System.out.println("chemicals are null for "+canonQsarSmilesToCalculate.size()+" chemicals");
				System.out.println("first one with null response\t"+canonQsarSmilesToCalculate.get(0));
			}
		} else {
			System.out.println("response is null for "+canonQsarSmilesToCalculate.size()+" chemicals");
			
			
		}
	}
}
