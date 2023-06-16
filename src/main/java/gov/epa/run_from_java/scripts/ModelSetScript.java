package gov.epa.run_from_java.scripts;

import java.sql.Connection;
import java.sql.ResultSet;
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


public class ModelSetScript {

	ModelService ms=new ModelServiceImpl();
	ModelSetService mss=new ModelSetServiceImpl();
	ModelInModelSetService mimss=new ModelInModelSetServiceImpl();
	String lanId="tmarti02";
	Connection conn=SqlUtilities.getConnectionPostgres();
	
	void createModelSets() {

		ModelSet ms=new ModelSet();
		ms.setName("WebTEST2.1 Sample models");
		ms.setDescription("Reduced feature set models based on TEST5.1 and OPERA 2.7 data sets using WebTEST-default descriptors");
		ms.setCreatedBy(lanId);
		mss.create(ms);

		
//		ModelSet ms=new ModelSet();
//		ms.setName("WebTEST2.0 PFAS");
//		ms.setDescription("Models based on PFAS data in exp_prop database");
//		ms.setCreatedBy(lanId);
//		mss.create(ms);
//
//		ms=new ModelSet();
//		ms.setName("WebTEST2.1 PFAS");
//		ms.setDescription("Reduced feature models based on PFAS data in exp_prop database");
//		ms.setCreatedBy(lanId);
//		mss.create(ms);
//
//		ms=new ModelSet();
//		ms.setName("WebTEST2.0 All but PFAS");
//		ms.setDescription("Models based on all chemicals except PFAS in exp_prop database (for comparison purposes)");
//		ms.setCreatedBy(lanId);
//		mss.create(ms);
//
//		ms=new ModelSet();
//		ms.setName("WebTEST2.1 All but PFAS");
//		ms.setDescription("Reduced feature models based on all chemicals except PFAS derived from exp_prop (for comparison purposes)");
//		ms.setCreatedBy(lanId);
//		mss.create(ms);
		
		
		
	}
	
	class ModelSet2 {
		
		String splitting;
		String descriptorsetName;
		Boolean useEmbedding;
		String modelsetName;
		
	}
	
	
	void assignModelsToModelSets() {		
		
		List<String>datasetNames=new ArrayList<>();
		datasetNames.add("HLC from exp_prop and chemprop");
		datasetNames.add("WS from exp_prop and chemprop");
		datasetNames.add("VP from exp_prop and chemprop");
		datasetNames.add("LogP from exp_prop and chemprop");
		datasetNames.add("MP from exp_prop and chemprop");
		datasetNames.add("BP from exp_prop and chemprop");

		ModelSet2 ms1=new ModelSet2();
		ms1.splitting="T=PFAS only, P=PFAS";
		ms1.descriptorsetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		ms1.useEmbedding=false;
		ms1.modelsetName="WebTEST2.0 PFAS";
		
		
		ModelSet2 ms2=new ModelSet2();
		ms2.splitting ="T=PFAS only, P=PFAS";
		ms2.descriptorsetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		ms2.useEmbedding=true;
		ms2.modelsetName="WebTEST2.1 PFAS";

		ModelSet2 ms3=new ModelSet2();
		ms3.splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
		ms3.descriptorsetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		ms3.useEmbedding=false;
		ms3.modelsetName="WebTEST2.0";

		ModelSet2 ms4=new ModelSet2();
		ms4.splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
		ms4.descriptorsetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		ms4.useEmbedding=true;
		ms4.modelsetName="WebTEST2.1";

		ModelSet2 ms5=new ModelSet2();
		ms5.splitting ="T=all but PFAS, P=PFAS";
		ms5.descriptorsetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		ms5.useEmbedding=false;
		ms5.modelsetName="WebTEST2.0 All but PFAS";

		ModelSet2 ms6=new ModelSet2();
		ms6.splitting ="T=all but PFAS, P=PFAS";
		ms6.descriptorsetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		ms6.useEmbedding=true;
		ms6.modelsetName="WebTEST2.1 All but PFAS";
		
		List<ModelSet2>sets=new ArrayList<>();
		sets.add(ms1);
//		sets.add(ms2);//done
		sets.add(ms3);
		sets.add(ms4);
		sets.add(ms5);
		sets.add(ms6);
		
		
		for (ModelSet2 set:sets) {
			assignToModelSet(set,datasetNames);
		}
			
		
	}
	
	void assignToModelSet(ModelSet2 set,List<String>datasetNames) {
		ModelSet modelSet=mss.findByName(set.modelsetName);
		
		System.out.println("\n"+set.modelsetName);
		
		for (String datasetName:datasetNames) {
			
			String datasetNameShort=datasetName.replace(" from exp_prop and chemprop", "");
			
			System.out.println("\n"+datasetNameShort);
			
			List<Long>modelIds=getModels(datasetName, set.splitting, set.descriptorsetName, set.useEmbedding);

			if (modelIds.size()==0) {
				System.out.println("Models not built for "+datasetName);
				continue;
			}
						
			Long consensusModelId=getConsensusModelId(modelIds.get(0));
			
			if (consensusModelId!=null) {
				modelIds.add(consensusModelId);
			} 
			
			for (Long modelId:modelIds) {
				ModelInModelSet m=new ModelInModelSet();				
				m.setCreatedBy(lanId);
				m.setModel(ms.findById(modelId));
				m.setModelSet(modelSet);

				String sql="select m2.\"name\"  from qsar_models.models m\n"+ 
				"join qsar_models.methods m2 on m2.id=m.fk_method_id\n"+
				"where m.id="+modelId;		
				String methodName=SqlUtilities.runSQL(conn, sql);
				
				try {
					mimss.create(m);
					System.out.println(methodName+"\tcreated");
				} catch (Exception ex) {
//					System.out.println(ex.getMessage());
					System.out.println(methodName+"\tNOT created");
				}
			}
						
			if(consensusModelId==null)	System.out.println("Consensus model\tmissing");
		}
	}
	
	void assignModelsToModelSetsOPERA() {		
		
		String [] datasetNames= {DevQsarConstants.LOG_KOA,DevQsarConstants.LOG_KM_HL,DevQsarConstants.HENRYS_LAW_CONSTANT,
				DevQsarConstants.LOG_BCF,DevQsarConstants.LOG_OH,DevQsarConstants.LOG_KOC,DevQsarConstants.VAPOR_PRESSURE,
				DevQsarConstants.WATER_SOLUBILITY, DevQsarConstants.BOILING_POINT, DevQsarConstants.MELTING_POINT,
				DevQsarConstants.LOG_KOW};

		
		String splitting ="OPERA";
		String descriptorsetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		boolean useEmbedding=true;
		String modelsetName="WebTEST2.1 Sample models";

		ModelSet modelSet=mss.findByName(modelsetName);
				
		for (String datasetNameShort:datasetNames) {
			
			String datasetName=datasetNameShort+" "+splitting;
			
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
	
	void assignModelsToModelSetsTEST() {		
		
		String [] datasetNames= {DevQsarConstants.MUTAGENICITY, DevQsarConstants.LD50,
				DevQsarConstants.LC50DM, DevQsarConstants.DEV_TOX, DevQsarConstants.LLNA,
				DevQsarConstants.LC50, DevQsarConstants.IGC50};
		
		String splitting ="TEST";
		String descriptorsetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		boolean useEmbedding=true;
		String modelsetName="WebTEST2.1 Sample models";

		ModelSet modelSet=mss.findByName(modelsetName);
				
		for (String datasetNameShort:datasetNames) {
			
			String datasetName=datasetNameShort+" "+splitting;
			
			List<Long>modelIds=getModels(datasetName, splitting, descriptorsetName, useEmbedding);

			if(modelIds.size()==0) {
				System.out.println("models in dataset = 0");
				continue;
			}
			
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
		String strId=SqlUtilities.runSQL(conn, sql);
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
		"where m.dataset_name='"+datasetName.replace("'", "''")+"' and\n"+ 
		"m.splitting_name ='"+splittingName+"' and \n"+
		"m.descriptor_set_name ='"+descriptorsetName+"' and \n";
		
//		System.out.println(sql);
		
		if (usesEmbedding) 
			sql+="m.fk_descriptor_embedding_id is not null;";
		else
			sql+="m.fk_descriptor_embedding_id is null;";
		
		ResultSet rs=SqlUtilities.runSQL2(conn, sql);
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
	
	
	
	public static void main(String[] args) {
		ModelSetScript ms=new ModelSetScript();
//		ms.createModelSets();		
		ms.assignModelsToModelSets();
//		ms.assignModelsToModelSetsOPERA();
//		ms.assignModelsToModelSetsTEST();

	}

}
