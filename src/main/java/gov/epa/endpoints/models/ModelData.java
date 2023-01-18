package gov.epa.endpoints.models;

import java.sql.Connection;
import java.sql.ResultSet;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;

/**
 * Class defining the data to be used for a particular model
 * @author GSINCL01
 *
 */
public class ModelData {
	public String datasetName;
	public String descriptorSetName;
	public String splittingName;
	
	public boolean removeLogP_Descriptors;
	
	// Set by querying from qsar_datasets and qsar_descriptors using ModelBuilder.initInstances()
	 public String trainingSetInstances;
	 public String predictionSetInstances;
	
	public ModelData(String datasetName, String descriptorSetName, String splittingName,boolean removeLogP_Descriptors) {
		this.datasetName = datasetName;
		this.descriptorSetName = descriptorSetName;
		this.splittingName = splittingName;
		this.removeLogP_Descriptors=removeLogP_Descriptors;
	
	}
	
//	public void initInstances(List<DataPointInSplitting> dataPointsInSplitting, List<DescriptorValues> descriptorValues) {
//		Map<String, DataPointInSplitting> dataPointsInSplittingMap = 
//				dataPointsInSplitting.stream().collect(Collectors.toMap(dpis -> dpis.getDataPoint().getCanonQsarSmiles(), dp -> dp));
//		Map<String, DescriptorValues> descriptorValuesMap = descriptorValues.stream()
//				.collect(Collectors.toMap(dv -> dv.getCanonQsarSmiles(), dv -> dv));
//		
//		DescriptorSet descriptorSet = descriptorValues.iterator().next().getDescriptorSet();
//		String instanceHeader = "ID\tProperty\t" + descriptorSet.getHeadersTsv() + "\r\n";
//		StringBuilder sbOverall = new StringBuilder(instanceHeader);
//		StringBuilder sbTraining = new StringBuilder(instanceHeader);
//		StringBuilder sbPrediction = new StringBuilder(instanceHeader);
//		for (String smiles:dataPointsInSplittingMap.keySet()) {
//			DataPointInSplitting dpis = dataPointsInSplittingMap.get(smiles);
//			DataPoint dp = dpis.getDataPoint();
//			DescriptorValues dv = descriptorValuesMap.get(smiles);
//			
//			if (dp!=null && dv!=null && !dp.getOutlier()) {
//				String instance = generateInstance(smiles, dp, dv);
//				if (instance!=null) {
//					sbOverall.append(instance);
//					Integer splitNum = dpis.getSplitNum();
//					if (splitNum==DevQsarConstants.TRAIN_SPLIT_NUM) {
//						sbTraining.append(instance);
//					} else if (splitNum==DevQsarConstants.TEST_SPLIT_NUM) {
//						sbPrediction.append(instance);
//					}
//				}
//			}
//		}
//		
//		this.trainingSetInstances = sbTraining.toString();
//		this.predictionSetInstances = sbPrediction.toString();
//	}
	
	
	public void initTrainingPredictionInstances(String datasetName,String descriptorSetName,String splittingName,boolean useDTXCIDs) {
		
		
		Connection conn=DatabaseLookup.getConnection();
		String sql="select id from qsar_datasets.datasets d where d.\"name\" ='"+datasetName+"'";
		String datasetId=DatabaseLookup.runSQL(conn, sql);

		sql="select id from qsar_descriptors.descriptor_sets d where d.\"name\" ='"+descriptorSetName+"'";
		String descriptorSetId=DatabaseLookup.runSQL(conn, sql);
		
		String idField="canon_qsar_smiles";
		if(useDTXCIDs) idField="qsar_dtxcid";

		sql="select headers_tsv from qsar_descriptors.descriptor_sets d where d.id="+descriptorSetId;
		String instanceHeader="ID\tProperty\t"+DatabaseLookup.runSQL(conn, sql)+"\r\n";

		StringBuilder sbTraining = new StringBuilder(instanceHeader);
		StringBuilder sbPrediction = new StringBuilder(instanceHeader);

	
		sql="select dp."+idField+", dp.qsar_property_value, dv.values_tsv, dpis.split_num from qsar_datasets.data_points dp\n"+ 
		"inner join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles\n"+ 
		"inner join qsar_datasets.data_points_in_splittings dpis on dpis.fk_data_point_id = dp.id\n"+ 
		"where dp.fk_dataset_id="+datasetId+" and dv.fk_descriptor_set_id="+descriptorSetId+";";

		System.out.println(sql);
		
		try {
			
			ResultSet rs=DatabaseLookup.runSQL2(conn, sql);
			
			int counter=0;
			
			while (rs.next()) {
				
//				System.out.println(++counter);
				
				String id=rs.getString(1);
				String qsar_property_value=rs.getString(2);
				String descriptors=rs.getString(3);
				int splitNum=Integer.parseInt(rs.getString(4));
				
				
				String instance=generateInstance(id, qsar_property_value, descriptors);
				if (instance==null) continue;

				if (splitNum==DevQsarConstants.TRAIN_SPLIT_NUM) {
					sbTraining.append(instance);
				} else if (splitNum==DevQsarConstants.TEST_SPLIT_NUM) {
					sbPrediction.append(instance);
				}
				
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
				
		this.trainingSetInstances = sbTraining.toString();
		this.predictionSetInstances = sbPrediction.toString();
	}

	
	public static String generateInstancesWithoutSplitting(String datasetName,String descriptorSetName) {
		return generateInstancesWithoutSplitting(datasetName,descriptorSetName,false);		
	}
	
	public static String generateInstancesWithoutSplitting(String datasetName,String descriptorSetName,boolean useDTXCIDs) {
			
		Connection conn=DatabaseLookup.getConnection();
		String sql="select id from qsar_datasets.datasets d where d.\"name\" ='"+datasetName+"'";
		String datasetId=DatabaseLookup.runSQL(conn, sql);

		sql="select id from qsar_descriptors.descriptor_sets d where d.\"name\" ='"+descriptorSetName+"'";
		String descriptorSetId=DatabaseLookup.runSQL(conn, sql);
		
		String idField="canon_qsar_smiles";
		if(useDTXCIDs) idField="qsar_dtxcid";

		sql="select headers_tsv from qsar_descriptors.descriptor_sets d where d.id="+descriptorSetId;
		String instanceHeader="ID\tProperty\t"+DatabaseLookup.runSQL(conn, sql)+"\r\n";
				
		StringBuilder sbOverall = new StringBuilder(instanceHeader);

		
		sql="select dp."+idField+", dp.qsar_property_value, dv.values_tsv from qsar_datasets.data_points dp\n"+ 
		"inner join qsar_descriptors.descriptor_values dv\n"+ 
		"on dp.canon_qsar_smiles=dv.canon_qsar_smiles\n"+ 
		"where dp.fk_dataset_id="+datasetId+" and dv.fk_descriptor_set_id="+descriptorSetId;
		
		System.out.println("\n"+sql+"\n");
		
		try {
			
			ResultSet rs=DatabaseLookup.runSQL2(conn, sql);
			
			
			int counter=0;
			
			while (rs.next()) {

				counter++;
				
				String id=rs.getString(1);
				String qsar_property_value=rs.getString(2);
				String descriptors=rs.getString(3);
				
				String instance=generateInstance(id, qsar_property_value, descriptors);
				if (instance==null) continue;

				sbOverall.append(instance);
				
//				if(counter==100) break; 
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return sbOverall.toString();

	}
	
	
//	public static String generateInstancesWithoutSplitting(List<DataPoint> dataPoints, List<DescriptorValues> descriptorValues,
//			boolean fetchDtxcids) {
//		Map<String, DataPoint> dataPointsMap = dataPoints.stream().collect(Collectors.toMap(dp -> dp.getCanonQsarSmiles(), dp -> dp));
//		Map<String, DescriptorValues> descriptorValuesMap = descriptorValues.stream()
//				.collect(Collectors.toMap(dv -> dv.getCanonQsarSmiles(), dv -> dv));
//		
//		CompoundService compoundService = new CompoundServiceImpl();
//		
//		DescriptorSet descriptorSet = descriptorValues.iterator().next().getDescriptorSet();
//		String instanceHeader = "ID\tProperty\t" + descriptorSet.getHeadersTsv() + "\r\n";
//		StringBuilder sbOverall = new StringBuilder(instanceHeader);
//		for (String smiles:dataPointsMap.keySet()) {
//			DataPoint dp = dataPointsMap.get(smiles);
//			DescriptorValues dv = descriptorValuesMap.get(smiles);
//			
//			if (dp!=null && dv!=null && !dp.getOutlier()) {
//				String id = smiles;
//				if (fetchDtxcids) {
//					List<Compound> compounds = compoundService.findByCanonQsarSmiles(smiles);
//					if (compounds!=null) {
//						List<String> dtxcids = compounds.stream()
//								.map(c -> c.getDtxcid())
//								.distinct()
//								.sorted()
//								.collect(Collectors.toList());
//						if (!dtxcids.isEmpty()) {
//							id = dtxcids.get(0);
//						}
//					}
//				}
//				
//				String instance = generateInstance(id, dp, dv);
//				if (instance!=null) {
//					sbOverall.append(instance);
//				}
//			}
//		}
//		
//		return sbOverall.toString();
//	}
	
	private static String generateInstance(String smiles, String qsar_property_value, String valuesTsv) {
		if (valuesTsv==null) return null;
		//TODO need to go through all instances of overall set and remove bad columns instead of rejecting rows
//		if (valuesTsv.toLowerCase().contains("infinity")) return null;
//		if (valuesTsv.toLowerCase().contains("∞")) return null;
		if (valuesTsv.toLowerCase().contains("error")) return null;
		return smiles + "\t" + qsar_property_value+ "\t" + valuesTsv + "\r\n";
	}
}
