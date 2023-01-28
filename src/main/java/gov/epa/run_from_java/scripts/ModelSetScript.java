package gov.epa.run_from_java.scripts;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInModelSet;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSet;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInModelSetService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInModelSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetServiceImpl;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.models.ModelStatisticCalculator;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;
import gov.epa.web_services.embedding_service.CalculationInfo;

public class ModelSetScript {

	ModelService ms=new ModelServiceImpl();
	ModelSetService mss=new ModelSetServiceImpl();
	ModelInModelSetService mimss=new ModelInModelSetServiceImpl();
	String lanId="tmarti02";
	Connection conn=DatabaseLookup.getConnection();
	
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
		
//		System.out.println(sql);
		
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
	
	
	
	public static void main(String[] args) {
		ModelSetScript ms=new ModelSetScript();
//		ms.createModelSets();		
		ms.assignModelsToModelSets();
//		ms.assignModelsToModelSetsOPERA();
//		ms.assignModelsToModelSetsTEST();

	}

}
