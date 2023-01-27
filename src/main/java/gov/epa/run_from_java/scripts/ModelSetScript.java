package gov.epa.run_from_java.scripts;

import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInModelSet;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSet;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInModelSetService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInModelSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;

public class ModelSetScript {

	ModelService ms=new ModelServiceImpl();
	ModelSetService mss=new ModelSetServiceImpl();
	ModelInModelSetService mimss=new ModelInModelSetServiceImpl();
	String lanId="tmarti02";
	Connection conn=DatabaseLookup.getConnection();
	
	void createModelSets() {

		ModelSet ms=new ModelSet();
		ms.setName("WebTEST2.0 PFAS");
		ms.setDescription("Models based on PFAS data in exp_prop database");
		ms.setCreatedBy(lanId);
		mss.create(ms);

		ms=new ModelSet();
		ms.setName("WebTEST2.1 PFAS");
		ms.setDescription("Reduced feature models based on PFAS data in exp_prop database");
		ms.setCreatedBy(lanId);
		mss.create(ms);

		ms=new ModelSet();
		ms.setName("WebTEST2.0 All but PFAS");
		ms.setDescription("Models based on all chemicals except PFAS in exp_prop database (for comparison purposes)");
		ms.setCreatedBy(lanId);
		mss.create(ms);

		ms=new ModelSet();
		ms.setName("WebTEST2.1 All but PFAS");
		ms.setDescription("Reduced feature models based on all chemicals except PFAS derived from exp_prop (for comparison purposes)");
		ms.setCreatedBy(lanId);
		mss.create(ms);
	}
	
	void assignModelsToModelSets() {		
		
		List<String>datasetNames=new ArrayList<>();
		datasetNames.add("HLC from exp_prop and chemprop");
		datasetNames.add("WS from exp_prop and chemprop");
		datasetNames.add("VP from exp_prop and chemprop");
		datasetNames.add("LogP from exp_prop and chemprop");
		datasetNames.add("MP from exp_prop and chemprop");
		datasetNames.add("BP from exp_prop and chemprop");

		
//		String splitting ="T=PFAS only, P=PFAS";
//		String descriptorsetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
//		boolean useEmbedding=false;
//		String modelsetName="WebTEST2.0 PFAS";

		String splitting ="T=PFAS only, P=PFAS";
		String descriptorsetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		boolean useEmbedding=true;
		String modelsetName="WebTEST2.1 PFAS";

//		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
//		String descriptorsetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
//		boolean useEmbedding=false;
//		String modelsetName="WebTEST2.0";

//		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
//		String descriptorsetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
//		boolean useEmbedding=true;
//		String modelsetName="WebTEST2.1";

//		String splitting ="T=All but PFAS, P=PFAS";
//		String descriptorsetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
//		boolean useEmbedding=false;
//		String modelsetName="WebTEST2.0 All but PFAS";

//		String splitting ="T=All but PFAS, P=PFAS";
//		String descriptorsetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
//		boolean useEmbedding=true;
//		String modelsetName="WebTEST2.1 All but PFAS";
		
		
		
		ModelSet modelSet=mss.findByName(modelsetName);
				
		for (String datasetName:datasetNames) {
			List<Long>modelIds=getModels(datasetName, splitting, descriptorsetName, useEmbedding);

			Long consensusModelId=getConsensusModelId(modelIds.get(0));
			
			if (consensusModelId!=null)
				modelIds.add(consensusModelId);
			
			for (Long modelId:modelIds) {
				ModelInModelSet m=new ModelInModelSet();				
				m.setCreatedBy(lanId);
				m.setModel(ms.findById(modelId));
				m.setModelSet(modelSet);
				
				try {
					mimss.create(m);
					System.out.println(modelId+"\t"+modelSet.getId()+"\t"+modelSet.getName()+"\tcreated");
				} catch (Exception ex) {
//					System.out.println(ex.getMessage());
					System.out.println(modelId+"\t"+modelSet.getId()+"\t"+modelSet.getName()+"\tNOT created");
				}
			}						
		}
	}
	
	
	Long getConsensusModelId(long modelId) {
		
		String sql="select micm.fk_consensus_model_id from qsar_models.models_in_consensus_models micm where fk_model_id="+modelId;
		String strId=DatabaseLookup.runSQL(conn, sql);
		if(strId==null) return null;
		else return (Long.parseLong(strId));
	}

	/**
	 * Gets the models built for set of variables (except consensus)
	 * 
	 * @param datasetName
	 * @param splittingName
	 * @param descriptorsetName
	 * @param usesEmbedding
	 * @return
	 */
	List<Long>getModels(String datasetName,String splittingName,String descriptorsetName,boolean usesEmbedding) {
		
		String sql="select m.id from qsar_models.models m\n"+
		"where m.dataset_name='"+datasetName+"' and\n"+ 
		"m.splitting_name ='"+splittingName+"' and \n"+
		"m.descriptor_set_name ='"+descriptorsetName+"' and \n";
		
		if (usesEmbedding) 
			sql+="m.fk_descriptor_embedding_id is not null;";
		else
			sql+="m.fk_descriptor_embedding_id is null;";
		
		
		
		ResultSet rs=DatabaseLookup.runSQL2(conn, sql);
		
		List<Long> consensusModelIDs = new ArrayList<Long>(); 
		
		try {
			while (rs.next()) {
				consensusModelIDs.add(Long.parseLong(rs.getString(1)));
			}
		} catch (Exception ex) {
			return null;
		}
		return consensusModelIDs;
	}
	
	void createSummaryTableForMethod() {
		String statisticName = "MAE_Test";

//		String methodName=DevQsarConstants.KNN;
		String methodName=DevQsarConstants.CONSENSUS;
		
		List<String> modelSetNames=new ArrayList<>();
				
		modelSetNames.add("WebTEST2.0 PFAS");
//		modelSetNames.add("WebTEST2.0");//TODO calc stats just for PFAS
//		modelSetNames.add("WebTEST2.0 All but PFAS");
//		modelSetNames.add("WebTEST2.0");
		
		modelSetNames.add("WebTEST2.1 PFAS");
//		modelSetNames.add("WebTEST2.1");//TODO calc stats just for PFAS
//		modelSetNames.add("WebTEST2.1 All but PFAS");
//		modelSetNames.add("WebTEST2.1");
		
		List<String>datasetNames=new ArrayList<>();
		datasetNames.add("HLC from exp_prop and chemprop");
		datasetNames.add("WS from exp_prop and chemprop");
		datasetNames.add("VP from exp_prop and chemprop");
		datasetNames.add("LogP from exp_prop and chemprop");
		datasetNames.add("MP from exp_prop and chemprop");
		datasetNames.add("BP from exp_prop and chemprop");
		
		createSummaryTable(statisticName, methodName, modelSetNames, datasetNames);

	}

	/**
	 * Prints summary of stats to the screen
	 * TODO make it write to file
	 * 
	 * 
	 * @param statisticName
	 * @param methodName
	 * @param modelSetNames
	 * @param datasetNames
	 */
	private void createSummaryTable(String statisticName, String methodName, List<String> modelSetNames,
			List<String> datasetNames) {
		DecimalFormat df=new DecimalFormat("0.00");
		
		System.out.print("DatasetName\t");
		
		for (int j=0;j<modelSetNames.size();j++) {
			String modelSetName=modelSetNames.get(j);
		
			System.out.print(modelSetName);			
			if (j<modelSetNames.size()-1) System.out.print("\t");
			else System.out.print("\r\n");
		}
		
		for (int i=0;i<datasetNames.size();i++) {
			String datasetName=datasetNames.get(i);
			
//			String datasetName2=datasetName.replace(" from exp_prop and chemprop", "");
						
			System.out.print(datasetName+"\t");
			
			List<String>modelSetStats=new ArrayList<>();
			
			for (int j=0;j<modelSetNames.size();j++) {
				String modelSetName=modelSetNames.get(j);
				Long modelId=getModelId(modelSetName, datasetName, methodName);
				
				if (modelId==null) {
					modelSetStats.add(null);
					continue;
				}

				Double stat=getStat(modelId, statisticName);
				
				if (stat==null)	modelSetStats.add(null);					
				else modelSetStats.add(df.format(stat));
			}
			
			for (int j=0;j<modelSetStats.size();j++) {
				String modelSetStat=modelSetStats.get(j);
				System.out.print(modelSetStat);			
				if (j<modelSetStats.size()-1) System.out.print("\t");
				else System.out.print("\r\n");
			}
			
		}
	}
	
	/**
	 * Get the modelID for model for given dataset, method, and modelSet
	 * 
	 * @param modelId
	 * @param datasetName
	 * @param methodName
	 * @return
	 */
	Long getModelId(String modelSetName,String datasetName,String methodName) {
		
		String sql="select mims.fk_model_id from qsar_models.models_in_model_sets mims\n"+ 
		"join qsar_models.models m on m.id=mims.fk_model_id\n"+ 
		"join qsar_models.methods m2 on m2.id=m.fk_method_id\n"+
		"join qsar_models.model_sets ms on ms.id=mims.fk_model_set_id\n"+ 
		"where ms.\"name\"='"+modelSetName+"' and \n"+
		"m.dataset_name ='"+datasetName+"' and \n"+
		"m2.\"name\" like '"+methodName+"%';";
		
//		System.out.println(sql+"\n");
		String strId=DatabaseLookup.runSQL(conn, sql);
		if(strId==null) return null;
		else return (Long.parseLong(strId));
	}
	
	/**
	 * Get prediction statistic for model
	 * 
	 * @param modelId
	 * @param datasetName
	 * @param methodName
	 * @return
	 */
	Double getStat(long modelId,String statisticName) {
		
		String sql="select ms.statistic_value  from qsar_models.model_statistics ms\n"+
				   "join qsar_models.\"statistics\" s  on s.id=ms.fk_statistic_id\n"+
				   "where s.\"name\"='"+statisticName+"' and \n"+
				   "ms.fk_model_id="+modelId+";";			
		
//		System.out.println(sql+"\n");
		String result=DatabaseLookup.runSQL(conn, sql);
		if(result==null) return null;
		else return (Double.parseDouble(result));
	}
	
	public static void main(String[] args) {
		ModelSetScript ms=new ModelSetScript();
//		ms.createModelSets();		
//		ms.assignModelsToModelSets();
		ms.createSummaryTableForMethod();

	}

}
