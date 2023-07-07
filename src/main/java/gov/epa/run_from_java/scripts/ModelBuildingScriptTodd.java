package gov.epa.run_from_java.scripts;

import java.util.HashSet;
import java.util.Set;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.reports.ModelMetadata;
import gov.epa.endpoints.reports.model_sets.ModelSetTable;
import gov.epa.endpoints.reports.model_sets.ModelSetTableGenerator;
import gov.epa.endpoints.reports.model_sets.ModelSetTable.ModelSetTableRow;

public class ModelBuildingScriptTodd {


	void buildPostgresModel() {
		boolean use_pmml=false;
		
		String lanId="tmarti02";

		String methodName=DevQsarConstants.RF;
//		String methodName=DevQsarConstants.XGB;
//			String methodName=DevQsarConstants.SVM;
//			String methodName=DevQsarConstants.DNN;


//		String datasetName="LLNA from exp_prop, without eChemPortal";
		String datasetName="Standard Water solubility from exp_prop";
//		String datasetName="Standard Henry's law constant from exp_prop";
				
		String descriptorSetName = "T.E.S.T. 5.1";		
//		String splittingName="RND_REPRESENTATIVE";
		String splittingName=SplittingGeneratorPFAS_Script.splittingPFASOnly;
//		String splittingName=PFAS_SplittingGenerator.splittingAll;
//		String splittingName=PFAS_SplittingGenerator.splittingAllButPFAS;
		
		boolean removeLogDescriptors=false;
		
		
//		String modelWsServer=DevQsarConstants.SERVER_LOCAL;
			String modelWsServer=DevQsarConstants.SERVER_819;
		int modelWsPort=DevQsarConstants.PORT_PYTHON_MODEL_BUILDING;

		boolean usePythonStorage=true;
		String embeddingName=null;
		
		ModelBuildingScript.buildModel(modelWsServer,modelWsPort,datasetName,descriptorSetName,
				splittingName, removeLogDescriptors,methodName,lanId,embeddingName,usePythonStorage,use_pmml);
		
		
//		Long[] sourceArray = { 182L,183L,184L,185L };
//	    Set<Long> set = new HashSet<Long>(Arrays.asList(sourceArray));	    
//		ModelBuildingScript.buildUnweightedConsensusModel(set, lanId);

		//	run.buildModel("http://localhost","8080", modelWsServer,modelWsPort,datasetName,descriptorSetName,
		//			splittingName, removeLogDescriptors,methodName,lanId);

		
//		long modelSetID=2L;
		
//		autoCreateConsensusModel(lanId, datasetName, modelSetID);

		
	}

	void buildSampleSetModel() {
		boolean use_pmml=false;
		String lanId="tmarti02";
		String sampleSource="OPERA";
//		String sampleSource="TEST";

//		String endpoint=DevQsarConstants.LOG_BCF;
		//		String endpoint=DevQsarConstants.LOG_OH;
//				String endpoint=DevQsarConstants.LOG_KOW;
//				String endpoint=DevQsarConstants.LOG_HALF_LIFE;
//				String endpoint=DevQsarConstants.BOILING_POINT;
				String endpoint=DevQsarConstants.WATER_SOLUBILITY;
//				String endpoint=DevQsarConstants.LLNA;
//		String endpoint=DevQsarConstants.MUTAGENICITY;
		//		String endpoint=DevQsarConstants.LOG_KOW;
		//		String endpoint=DevQsarConstants.HENRYS_LAW_CONSTANT;
//				String endpoint=DevQsarConstants.DEV_TOX;
//		String endpoint=DevQsarConstants.LD50;

		String datasetName = endpoint +" "+sampleSource;
		String splittingName=sampleSource;		

//		String descriptorSetName = "T.E.S.T. 5.1";		
//		String descriptorSetName = "WebTEST-default";
		String descriptorSetName = "Mordred-default";
		
		boolean removeLogDescriptors=endpoint.equals(DevQsarConstants.LOG_KOW);

		//		for (Long l = 1L; l <= 128L; l++) {
		//			run.listDescriptors(modelWsServer, modelWsPort, l, "gsincl01");
		//		}

//		String modelWsServer=DevQsarConstants.SERVER_LOCAL;
	String modelWsServer=DevQsarConstants.SERVER_819;
		int modelWsPort=DevQsarConstants.PORT_PYTHON_MODEL_BUILDING;

//		String methodName=DevQsarConstants.SVM;
		String methodName=DevQsarConstants.RF;		
		////		String methodName=DevQsarConstants.DNN;
		////		String methodName=DevQsarConstants.XGB;
		
		boolean usePythonStorage=true;
		String embeddingName=null;

		//	
		ModelBuildingScript.buildModel(modelWsServer,modelWsPort,datasetName,descriptorSetName,
				splittingName, removeLogDescriptors,methodName,lanId,embeddingName,usePythonStorage,use_pmml);
		
		long modelSetID=1L;
		
//		autoCreateConsensusModel(lanId, datasetName, modelSetID);
		

		
	}


	
	private void autoCreateConsensusModel(String lanId, String datasetName, long modelSetID) {
		ModelSetTableGenerator gen = new ModelSetTableGenerator();
		ModelSetTable table = gen.generate(modelSetID);

		Set<Long> set = new HashSet<Long>();
		
		for (ModelSetTableRow row:table.modelSetTableRows) {
			
			if (row.datasetName.equals(datasetName)) {
				System.out.println(row.datasetName);
				
				for (ModelMetadata mmd:row.modelMetadata) {
					System.out.println(mmd.qsarMethodName+"\t"+mmd.modelId);
					set.add(mmd.modelId);
				}
			}
		}		
		
		ModelBuildingScript.buildUnweightedConsensusModel(set, lanId);
		
//		Long[] sourceArray = { 182L,183L,184L,185L };
//	    Set<Long> set = new HashSet<Long>(Arrays.asList(sourceArray));	    
//		ModelBuildingScript.buildUnweightedConsensusModel(set, lanId);

	}



	public static void main(String[] args) {
		ModelBuildingScriptTodd run=new ModelBuildingScriptTodd();

//		run.buildSampleSetModel();
		run.buildPostgresModel();


		//*****************************************************************************************

		//*****************************************************************************************
		//		d.testInit(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING, methodName, 104L);	
		//*****************************************************************************************			
		//		String descriptorSetName = "T.E.S.T. 5.1";
		//		String endpoint=DevQsarConstants.LOG_HALF_LIFE;
		//		String datasetName = endpoint+" OPERA";
		//		run.reportAllPredictions(datasetName,descriptorSetName,splittingName);		

		//*****************************************************************************************	
		//		run.reportAllPredictions(getSampleDataSets(false,true), descriptorSetName);
		//*****************************************************************************************

	}

}
