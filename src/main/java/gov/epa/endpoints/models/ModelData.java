package gov.epa.endpoints.models;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.SplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticServiceImpl;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;
import gov.epa.web_services.embedding_service.CalculationInfo;

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
	public boolean useDTXCIDs;
	
	SplittingServiceImpl splittingService=new SplittingServiceImpl();
	DatasetServiceImpl datasetService=new DatasetServiceImpl();
	DescriptorSetServiceImpl descriptorSetService=new DescriptorSetServiceImpl(); 
	
	public ModelData(String datasetName, String descriptorSetName, String splittingName,boolean removeLogP_Descriptors,boolean useDTXCIDs) {
		this.datasetName = datasetName;
		this.descriptorSetName = descriptorSetName;
		this.splittingName = splittingName;
		this.removeLogP_Descriptors=removeLogP_Descriptors;
		this.useDTXCIDs=useDTXCIDs;	
	}
		
	public ModelData(CalculationInfo ci, boolean useDTXCIDs) {
		this.datasetName = ci.datasetName;
		this.descriptorSetName = ci.descriptorSetName;
		this.splittingName = ci.splittingName;
		this.removeLogP_Descriptors=ci.remove_log_p;
		this.useDTXCIDs=useDTXCIDs;	
	}
	
	public static ModelData initModelData(String datasetName, String descriptorSetName, String splittingName, boolean removeLogP,boolean useDTXCIDs) {
		ModelData data = new ModelData(datasetName, descriptorSetName, splittingName,removeLogP,useDTXCIDs);
		data.initTrainingPredictionInstances();
		return data;
	}

	public static ModelData initModelData(CalculationInfo ci, boolean useDTXCIDs) {
		ModelData data = new ModelData(ci,useDTXCIDs);
		data.initTrainingPredictionInstances();
		return data;
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
	
	/**
	 * Get training and prediction set tsvs using sql 
	 */
	public void initTrainingPredictionInstances() {
		
		Connection conn=SqlUtilities.getConnectionPostgres();
		String idField="canon_qsar_smiles";
		if(useDTXCIDs) idField="qsar_dtxcid";
		
		Dataset dataset=datasetService.findByName(datasetName);
		Splitting splitting=splittingService.findByName(splittingName);
		DescriptorSet descriptorSet=descriptorSetService.findByName(descriptorSetName);
				
		String sql="select headers_tsv from qsar_descriptors.descriptor_sets d\n"+					
					"where d.\"name\"='"+descriptorSetName+"';";
		String instanceHeader="ID\tProperty\t"+SqlUtilities.runSQL(conn, sql)+"\r\n";
//		System.out.println(instanceHeader+"\n");
		
		sql="select dp."+idField+", dp.qsar_property_value, dv.values_tsv, dpis.split_num from qsar_datasets.data_points dp\n"+ 
		"join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles\n"+ 
		"join qsar_datasets.data_points_in_splittings dpis on dpis.fk_data_point_id = dp.id\n"+ 
		"where dp.fk_dataset_id="+dataset.getId()+" and dv.fk_descriptor_set_id="+descriptorSet.getId()+" and dpis.fk_splitting_id="+splitting.getId()+";";
		
//		System.out.println("\n"+sql);

		StringBuilder sbTraining = new StringBuilder(instanceHeader);
		StringBuilder sbPrediction = new StringBuilder(instanceHeader);

		int counter=0;
		
		try {
			
			ResultSet rs=SqlUtilities.runSQL2(conn, sql);
			
			while (rs.next()) {
				counter++;
				
//				if (counter%1000==0) System.out.println(counter+ "\tbuilding instances");
				
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
				
		System.out.println("Training / prediction instances created:"+counter);
		
		this.trainingSetInstances = sbTraining.toString();
		this.predictionSetInstances = sbPrediction.toString();
	}
	
	
	/**
	 * Get training and prediction set tsvs using sql 
	 */
	public static List<String> getTrainingIds(Dataset dataset, Splitting splitting, boolean useDTXCIDs) {
		
		Connection conn=SqlUtilities.getConnectionPostgres();
		String idField="canon_qsar_smiles";
		if(useDTXCIDs) idField="qsar_dtxcid";
		
		String sql="select dp."+idField+" from qsar_datasets.data_points dp\n"+ 
		"inner join qsar_datasets.data_points_in_splittings dpis on dpis.fk_data_point_id = dp.id\n"+ 
		"where dp.fk_dataset_id="+dataset.getId()+" and "+
		"dpis.fk_splitting_id="+splitting.getId()+" and "+
		"dpis.split_num="+DevQsarConstants.TRAIN_SPLIT_NUM+";";
		
//		System.out.println(sql);
		
		try {			
			List<String>ids=new ArrayList<>();		
			ResultSet rs=SqlUtilities.runSQL2(conn, sql);
			while (rs.next()) {
				String id=rs.getString(1);
				ids.add(id);
			}
			return ids;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}


	
	public String generateInstancesWithoutSplitting(String datasetName,String descriptorSetName) {
		return generateInstancesWithoutSplitting(datasetName,descriptorSetName,false);		
	}
	
	public String generateInstancesWithoutSplitting(String datasetName,String descriptorSetName,boolean useDTXCIDs) {
			
		Connection conn=SqlUtilities.getConnectionPostgres();
		
		Dataset dataset=datasetService.findByName(datasetName);
		DescriptorSet descriptorSet=descriptorSetService.findByName(descriptorSetName);
				
		String idField="canon_qsar_smiles";
		if(useDTXCIDs) idField="qsar_dtxcid";

		String sql="select headers_tsv from qsar_descriptors.descriptor_sets d where d.id="+descriptorSet.getId();
		String instanceHeader="ID\tProperty\t"+SqlUtilities.runSQL(conn, sql)+"\r\n";
				
		StringBuilder sbOverall = new StringBuilder(instanceHeader);

		
		sql="select dp."+idField+", dp.qsar_property_value, dv.values_tsv from qsar_datasets.data_points dp\n"+ 
		"inner join qsar_descriptors.descriptor_values dv\n"+ 
		"on dp.canon_qsar_smiles=dv.canon_qsar_smiles\n"+ 
		"where dp.fk_dataset_id="+dataset.getId()+" and dv.fk_descriptor_set_id="+descriptorSet.getId();
		
//		System.out.println("\n"+sql+"\n");
		
		try {
			
			ResultSet rs=SqlUtilities.runSQL2(conn, sql);
			
			
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
	
	
	public void generateInstancesNotinOperaPredictionSet() {
		
		Connection conn=SqlUtilities.getConnectionPostgres();
		
		Dataset dataset=datasetService.findByName(datasetName);
		DescriptorSet descriptorSet=descriptorSetService.findByName(descriptorSetName);
		
		String idField="canon_qsar_smiles";

		String sql="select headers_tsv from qsar_descriptors.descriptor_sets d where d.id="+descriptorSet.getId();
		String instanceHeader="ID\tProperty\t"+SqlUtilities.runSQL(conn, sql)+"\r\n";

		sql="select p.name from qsar_datasets.datasets d \r\n"
				+ "inner join qsar_datasets.properties p on p.id =d.fk_property_id \r\n"
				+ "where d.id="+dataset.getId()+";";
				
		String propertyName=SqlUtilities.runSQL(conn, sql);
		
		String propertyNameOpera=propertyName;
		if(propertyName.equals("LogBCF_Fish_WholeBody")) propertyNameOpera="LogBCF";
		
		
		//*****************************************************************************************
		String datasetNameOpera=propertyNameOpera+" OPERA";
		Dataset datasetOpera=datasetService.findByName(datasetNameOpera);
		Splitting splittingOpera=splittingService.findByName("OPERA");	
				
		String sqlOpera="select dp."+idField+", dp.qsar_property_value, dv.values_tsv, dpis.split_num from qsar_datasets.data_points dp\n"+ 
		"inner join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles\n"+ 
		"inner join qsar_datasets.data_points_in_splittings dpis on dpis.fk_data_point_id = dp.id\n"+ 
		"where dp.fk_dataset_id="+datasetOpera.getId()+" and dv.fk_descriptor_set_id="+descriptorSet.getId()+" and "+
		"dpis.split_num=1"+" and dpis.fk_splitting_id="+splittingOpera.getId()+";";
		//*****************************************************************************************
		
		
		sql="select dp."+idField+", dp.qsar_property_value, dv.values_tsv from qsar_datasets.data_points dp\n"+ 
		"inner join qsar_descriptors.descriptor_values dv\n"+ 
		"on dp.canon_qsar_smiles=dv.canon_qsar_smiles\n"+ 
		"where dp.fk_dataset_id="+dataset.getId()+" and dv.fk_descriptor_set_id="+descriptorSet.getId();
//		System.out.println("\n"+sql+"\n");

		
		StringBuilder sbTraining = new StringBuilder(instanceHeader);
		StringBuilder sbPrediction = new StringBuilder(instanceHeader);

		try {
			
			ResultSet rs=SqlUtilities.runSQL2(conn, sql);

			//Make look up for Opera pred set instances:
			ResultSet rsOpera=SqlUtilities.runSQL2(conn, sqlOpera);
			Hashtable<String,String>htOperaPredSet=new Hashtable<>();
			while (rsOpera.next()) {
				String id=rsOpera.getString(1);
//				System.out.println(id);
				String qsar_property_value=rsOpera.getString(2);
				String descriptors=rsOpera.getString(3);
				String instance=generateInstance(id, qsar_property_value, descriptors);
				if (instance==null) continue;
				sbPrediction.append(instance);
				htOperaPredSet.put(id,instance);
			}
			
			while (rs.next()) {
				String id=rs.getString(1);
				String qsar_property_value=rs.getString(2);
				String descriptors=rs.getString(3);
				String instance=generateInstance(id, qsar_property_value, descriptors);
				if (instance==null) continue;				
				if(htOperaPredSet.get(id)!=null) continue;//If in Opera prediction set dont add to training set
				sbTraining.append(instance);
//				if(counter==100) break; 
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		this.trainingSetInstances = sbTraining.toString();
		this.predictionSetInstances = sbPrediction.toString();

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

	public void generateInstancesNotinOperaTrainingSet() {
		Connection conn=SqlUtilities.getConnectionPostgres();
				
		Dataset dataset=datasetService.findByName(datasetName);
		DescriptorSet descriptorSet=descriptorSetService.findByName(descriptorSetName);

		String idField="canon_qsar_smiles";

		String sql="select headers_tsv from qsar_descriptors.descriptor_sets d where d.id="+descriptorSet.getId();
		String instanceHeader="ID\tProperty\t"+SqlUtilities.runSQL(conn, sql)+"\r\n";

		sql="select p.name from qsar_datasets.datasets d \r\n"
				+ "inner join qsar_datasets.properties p on p.id =d.fk_property_id \r\n"
				+ "where d.id="+dataset.getId()+";";
				
		String propertyName=SqlUtilities.runSQL(conn, sql);
		
		String propertyNameOpera=propertyName;
		if(propertyName.equals("LogBCF_Fish_WholeBody")) propertyNameOpera="LogBCF";
		
		
		//*****************************************************************************************
		String datasetNameOpera=propertyNameOpera+" OPERA";
		Dataset datasetOpera=datasetService.findByName(datasetNameOpera);
		Splitting splittingOpera=splittingService.findByName("OPERA");		
		
		String sqlOpera="select dp."+idField+", dp.qsar_property_value, dv.values_tsv, dpis.split_num from qsar_datasets.data_points dp\n"+ 
		"inner join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles\n"+ 
		"inner join qsar_datasets.data_points_in_splittings dpis on dpis.fk_data_point_id = dp.id\n"+ 
		"where dp.fk_dataset_id="+datasetOpera.getId()+" and dv.fk_descriptor_set_id="+descriptorSet.getId()+" and "+
		"dpis.split_num=0"+" and dpis.fk_splitting_id="+splittingOpera.getId()+";";
		//*****************************************************************************************
		
		
		sql="select dp."+idField+", dp.qsar_property_value, dv.values_tsv from qsar_datasets.data_points dp\n"+ 
		"inner join qsar_descriptors.descriptor_values dv\n"+ 
		"on dp.canon_qsar_smiles=dv.canon_qsar_smiles\n"+ 
		"where dp.fk_dataset_id="+dataset.getId()+" and dv.fk_descriptor_set_id="+descriptorSet.getId();
//		System.out.println("\n"+sql+"\n");

		
		StringBuilder sbTraining = new StringBuilder(instanceHeader);
		StringBuilder sbPrediction = new StringBuilder(instanceHeader);

		try {
			
			ResultSet rs=SqlUtilities.runSQL2(conn, sql);

			//Make look up for Opera pred set instances:
			ResultSet rsOpera=SqlUtilities.runSQL2(conn, sqlOpera);
			Hashtable<String,String>htOperaTrainingSet=new Hashtable<>();
			while (rsOpera.next()) {
				String id=rsOpera.getString(1);
//				System.out.println(id);
				String qsar_property_value=rsOpera.getString(2);
				String descriptors=rsOpera.getString(3);
				String instance=generateInstance(id, qsar_property_value, descriptors);
				if (instance==null) continue;
				sbTraining.append(instance);
				htOperaTrainingSet.put(id,instance);
			}
			
			while (rs.next()) {
				String id=rs.getString(1);
				String qsar_property_value=rs.getString(2);
				String descriptors=rs.getString(3);
				String instance=generateInstance(id, qsar_property_value, descriptors);
				if (instance==null) continue;				
				if(htOperaTrainingSet.get(id)!=null) continue;//If in Opera prediction set dont add to training set
				sbPrediction.append(instance);
//				if(counter==100) break; 
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		this.trainingSetInstances = sbTraining.toString();
		this.predictionSetInstances = sbPrediction.toString();
	}
}
