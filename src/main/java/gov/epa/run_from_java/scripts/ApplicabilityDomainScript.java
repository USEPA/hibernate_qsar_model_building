package gov.epa.run_from_java.scripts;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingServiceImpl;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.endpoints.reports.predictions.QsarPredictedValue;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportDataPoint;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportModelMetadata;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportModelStatistic;
import gov.epa.run_from_java.scripts.ApplicabilityDomainScript.ApplicabilityDomainPrediction;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.web_services.ModelWebService;
import gov.epa.web_services.embedding_service.CalculationInfo;
import gov.epa.web_services.embedding_service.CalculationInfoGA;
import kong.unirest.Unirest;

public class ApplicabilityDomainScript {
	
//	static String lanId="cramslan";
	static String lanId = "tmarti02";
	
	static int portModelBuilding=DevQsarConstants.PORT_PYTHON_MODEL_BUILDING;
	static String serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
//	static String serverModelBuilding=DevQsarConstants.SERVER_819;

	static String descriptorSetName = DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
	
	static String qsarMethodEmbedding = DevQsarConstants.KNN;
	
	ModelWebService mws=new ModelWebService(serverModelBuilding, portModelBuilding);

	
	
	
	static String strSampleResponse="{\"idTest\":\"OC(=O)C(F)(F)C(F)(F)F\",\"idNeighbor1\":\"OCC(F)(F)C(F)(F)F\",\"idNeighbor2\":\"OCC(F)(F)C(F)F\",\"idNeighbor3\":\"FC(F)(Cl)C(F)(F)C(F)Cl\",\"AD\":true}\r\n"
	+ "{\"idTest\":\"OC(=O)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)F\",\"idNeighbor1\":\"OCCC(F)(F)C(F)(F)C(F)(F)C(F)(F)F\",\"idNeighbor2\":\"OC(=O)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)F\",\"idNeighbor3\":\"OC(=O)C1C=CC=CC=1\",\"AD\":true}\r\n"
	+ "{\"idTest\":\"FC(F)(Cl)C(F)(F)Cl\",\"idNeighbor1\":\"FC(F)(F)C(F)(Cl)Cl\",\"idNeighbor2\":\"FC(F)(Br)C(F)(F)Br\",\"idNeighbor3\":\"O=C(F)C(F)(F)F\",\"AD\":true}\r\n"
	+ "{\"idTest\":\"FCC(F)(F)F\",\"idNeighbor1\":\"FC(F)(F)C(F)(F)F\",\"idNeighbor2\":\"O=C(F)C(F)(F)F\",\"idNeighbor3\":\"FC(Cl)C(F)(F)F\",\"AD\":false}\r\n"
	+ "{\"idTest\":\"FC(F)(F)C(F)(F)C(Cl)Cl\",\"idNeighbor1\":\"FC(F)(Cl)C(F)(F)C(F)Cl\",\"idNeighbor2\":\"FC(F)OC(F)(F)C(F)Cl\",\"idNeighbor3\":\"FC(F)(C(F)(F)F)C(F)(F)F\",\"AD\":true}\r\n"
	+ "{\"idTest\":\"FC(F)C(F)(F)F\",\"idNeighbor1\":\"FC(F)(F)C(F)(F)F\",\"idNeighbor2\":\"O=C(F)C(F)(F)F\",\"idNeighbor3\":\"FC(Cl)C(F)(F)F\",\"AD\":false}\r\n"
	+ "{\"idTest\":\"CC(C)(CS(C)(=O)=O)NC(=O)C1=C(I)C=CC=C1C(=O)NC1=CC=C(C=C1C)C(F)(C(F)(F)F)C(F)(F)F\",\"idNeighbor1\":\"CC1(C)C(C1C=CC(=O)OC(C(F)(F)F)C(F)(F)F)C(=O)OC(C#N)C1=CC(=CC=C1)OC1C=CC=CC=1\",\"idNeighbor2\":\"O=C(NC(=O)NC1C=C(Cl)C(OC(F)(F)C(F)F)=C(Cl)C=1)C1C(F)=CC=CC=1F\",\"idNeighbor3\":\"O=C(NC1C=C(Cl)C(=CC=1Cl)OC(F)(F)C(F)C(F)(F)F)NC(=O)C1C(F)=CC=CC=1F\",\"AD\":true}\r\n"
	+ "{\"idTest\":\"FC1(F)C(F)(F)C(F)(F)C1(F)F\",\"idNeighbor1\":\"FC(F)OC(F)(F)C(F)Cl\",\"idNeighbor2\":\"OC(C(F)(F)F)C(F)(F)F\",\"idNeighbor3\":\"FC(F)(Cl)C(F)(F)C(F)Cl\",\"AD\":true}";

	
	static class ApplicabilityDomainPrediction {
		String id;
		List<String>idNeighbors;
		Boolean AD;
	}
	
	DescriptorEmbeddingServiceImpl descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();

	public ApplicabilityDomainScript() {
		Unirest.config().connectTimeout(0).socketTimeout(0);
	}
	
	public void runCaseStudyExpProp_All_Endpoints() {

		

		serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
		portModelBuilding=5004;
//		serverModelBuilding=DevQsarConstants.SERVER_819;
//		portModelBuilding=5014;

		//*************************************************************************************************
		
		String modelSetName="WebTEST2.1";
//		String modelSetName="WebTEST2.0";
				
		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;

		
		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_Embedding_Cosine;
//		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_Embedding_Euclidean;		
//		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Cosine; 		
//		String applicability_domain=DevQsarConstants.Applicability_Domain_OPERA_local_index;
		
		System.out.println("\t"+applicability_domain);
		
		boolean storeNeighbors=false;
		boolean includeFracInside=true;
		

		if (includeFracInside) {
			System.out.println("Dataset\tR2_Inside\tFrac_Inside\tR2_Outside");	
		} else {
			System.out.println("datasetName\tR2_Inside\tR2_Outside");
		}
//		System.out.println("datasetName\tR2_Test_Inside_AD\tFraction_inside_AD\tProduct\tR2_Test_Outside_AD");
		

		Unirest.config().connectTimeout(0).socketTimeout(0);
		
		ModelWebService mws=new ModelWebService(serverModelBuilding, portModelBuilding);
		
		List<String>datasetNames=new ArrayList<>();

//		datasetNames.add("HLC from exp_prop and chemprop");
//		datasetNames.add("WS from exp_prop and chemprop");
//		datasetNames.add("VP from exp_prop and chemprop");
//		datasetNames.add("LogP from exp_prop and chemprop");
//		datasetNames.add("pKa_a from exp_prop and chemprop");
//		datasetNames.add("pKa_b from exp_prop and chemprop");
//		datasetNames.add("MP from exp_prop and chemprop");
//		datasetNames.add("BP from exp_prop and chemprop");
		
		datasetNames.add("HLC v1");
		datasetNames.add("VP v1");
		datasetNames.add("BP v1");
		datasetNames.add("WS v1");
		datasetNames.add("LogP v1");
		datasetNames.add("MP v1");

		

		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		if (modelSetName.contains("2.0") && !applicability_domain.equals(DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Cosine)) {
			System.out.println("Invalid AD!");
			return;			
		}
				
		for (String datasetName:datasetNames) {
						
			boolean remove_log_p = false;
			if(datasetName.contains("LogP")) remove_log_p=true;
			
			CalculationInfoGA ci = new CalculationInfoGA();
			ci.num_generations = 100;
			
			
//			if (datasetName.contains("BP") || splitting.equals("T=all but PFAS, P=PFAS")) ci.num_generations=10;//takes too long to do 100			

			ci.remove_log_p = remove_log_p;
			ci.qsarMethodEmbedding = qsarMethodEmbedding;
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=splitting;
						
			DescriptorEmbedding de=null;
			
			if (modelSetName.contains("2.1")) {
				de=getEmbedding(ci);
				if (de==null) {
					continue;
				}
			} 
			
		
			ModelData data = ModelData.initModelData(ci,false);
			
//			System.out.println(data.predictionSetInstances);
//			if(true)return;

			//Run AD calculations using webservice:			
			String strResponse=null;
			
			if (de==null) {
				strResponse=mws.callPredictionApplicabilityDomain(data.trainingSetInstances,data.predictionSetInstances,
						remove_log_p,applicability_domain).getBody();				
			} else {
				strResponse=mws.callPredictionApplicabilityDomain(data.trainingSetInstances,data.predictionSetInstances,
						remove_log_p,de.getEmbeddingTsv(),applicability_domain).getBody();
			}
			

//			System.out.println(strResponse);
//			String strResponse=strSampleResponse;

			Hashtable<String, ApplicabilityDomainPrediction>adPredictions=convertResponse(strResponse,storeNeighbors);


//			for (ApplicabilityDomainPrediction pred:adPredictions) {
//				if (!pred.AD)
//					System.out.println(pred.id+"\t"+pred.AD);
//			}
			
//			System.out.println("Results="+Utilities.gson.toJson(adPredictions)+"\n");
			
			PredictionReport predictionReport=SampleReportWriter.getReport(modelSetName, datasetName, splitting);
			
//			System.out.println(Utilities.gson.toJson(predictionReport.predictionReportModelMetadata.get(0)));
			
			PredictionStatisticsScript.getStatsInsideAD(predictionReport, adPredictions,null);			
			String stats=getStatsInsideWithFrac(predictionReport);

			PredictionStatisticsScript.getStatsOutsideAD(predictionReport, adPredictions,null);
			String statsOutside=getStatsOutside(predictionReport);
			

//			System.out.println(datasetName.replace(" from exp_prop and chemprop", "")+"\t"+stats+"\t"+statsOutside);

//			System.out.println(datasetName+"\t"+getR2NoAD(predictionReport));
		}
	}
	
	public void runCaseStudyExpProp_All_Endpoints_modelSpecificAD() {

		
		String modelSetName="WebTEST2.1";
		String splittingName =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
//		boolean limitToPFAS=true;
		boolean limitToPFAS=false;

//		String modelSetName="WebTEST2.1 PFAS";
//		String splittingName =SplittingGeneratorPFAS_Script.splittingPFASOnly;
//		boolean limitToPFAS=false;
		
//		String modelSetName="WebTEST2.1 All but PFAS";
//		String splittingName =SplittingGeneratorPFAS_Script.splittingAllButPFAS;
//		boolean limitToPFAS=false;
		
		
		String statName="MAE_Test";
//		String statName="Q2_Test";
		
		String listName="PFASSTRUCTV4";		
		String folder="data/dev_qsar/dataset_files/";
		String filePath=folder+listName+"_qsar_ready_smiles.txt";
		
//		HashSet<String>smilesArray=null;//dont need since pulling pfas specific report json		
//		if(limitToPFAS)	smilesArray=SplittingGeneratorPFAS_Script.getPFASSmiles(filePath);
//		System.out.println(smilesArray.size());
		//*************************************************************************************************
		
//		String applicability_domain=DevQsarConstants.Applicability_Domain_Kernel_Density;
//		String applicability_domain=DevQsarConstants.Applicability_Domain_OPERA_global_index;
//		String applicability_domain=DevQsarConstants.Applicability_Domain_OPERA_local_index;
//		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_Embedding_Cosine;
		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_Embedding_Euclidean;		
//		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Cosine; 		
//		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Euclidean;
		
		System.out.println(applicability_domain+"\t"+splittingName+"\t"+"limitToPFAS="+limitToPFAS);
		
		boolean storeNeighbors=false;
		

		System.out.println("Dataset\tMethod\tFrac_Inside\t"+statName+"_Inside\t"+statName+"_Outside");	

		List<String>datasetNames=new ArrayList<>();
//		datasetNames.add("HLC v1 modeling");
//		datasetNames.add("WS v1 modeling");
//		datasetNames.add("VP v1 modeling");
//		datasetNames.add("LogP v1 modeling");
//		datasetNames.add("BP v1 modeling");
//		datasetNames.add("MP v1 modeling");
		
		datasetNames.add("exp_prop_96HR_FHM_LC50_v1 modeling");
		
		List<String>methodNames=new ArrayList<>();
//		methodNames.add(DevQsarConstants.RF);
		methodNames.add(DevQsarConstants.XGB);
		
				
		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
				
		for (String datasetName:datasetNames) {
			
			PredictionReport predictionReport=SampleReportWriter.getReport(modelSetName, datasetName, splittingName,limitToPFAS);
			
			for (String methodName:methodNames) {
				calculateAD_stats(predictionReport, splittingName, methodName, statName, applicability_domain,
					storeNeighbors, descriptorSetName, datasetName);
			}
			
//			Hashtable<String, ApplicabilityDomainPrediction>htAD_Consensus=addConsensusAD(predictionReport, methodNames);
			
//			calculateAD_statsConsensus(predictionReport, htAD_Consensus, statName, datasetName);
			
			predictionReport.AD=applicability_domain;
			
//			if(true) continue;
			
			//Write out new reports with AD info: (TODO later need to store this info in the database...
			SampleReportWriter.writeReportWithAD(modelSetName, datasetName, splittingName, limitToPFAS, predictionReport);

		}//end loop over datasets
		
	}
	

	/**
	 * Adds AD results to prediction report using web service
	 * 
	 * @param methodName
	 * @param splittingName
	 * @param descriptorSetName
	 * @param applicability_domain
	 * @param datasetName
	 * @param predictionReport
	 */
	public void runAD(String methodName, String splittingName,String descriptorSetName, 
			String applicability_domain,String datasetName,PredictionReport predictionReport) {
		
		String statName="MAE_Test";
//		String statName="Q2_Test";
		
		boolean storeNeighbors=false;
		System.out.println("Dataset\tMethod\tFrac_Inside\t"+statName+"_Inside\t"+statName+"_Outside");	
		
		calculateAD_stats(predictionReport, splittingName, methodName, statName, applicability_domain,
				storeNeighbors, descriptorSetName, datasetName);				
		
	}

public void runCaseStudyExpProp_All_Endpoints_modelSpecificAD_kNN() {

		
		String modelSetName="WebTEST2.1";
		String splittingName =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
//		boolean limitToPFAS=true;
		boolean limitToPFAS=false;

//		String modelSetName="WebTEST2.1 PFAS";
//		String splittingName =SplittingGeneratorPFAS_Script.splittingPFASOnly;
//		boolean limitToPFAS=false;
		
//		String modelSetName="WebTEST2.1 All but PFAS";
//		String splittingName =SplittingGeneratorPFAS_Script.splittingAllButPFAS;
//		boolean limitToPFAS=false;
		
		
		String statName="MAE_Test";
//		String statName="Q2_Test";
		
//		String listName="PFASSTRUCTV4";		
//		String folder="data/dev_qsar/dataset_files/";
//		String filePath=folder+listName+"_qsar_ready_smiles.txt";
//		
//		HashSet<String>smilesArray=null;//dont need since pulling pfas specific report json		
//		if(limitToPFAS)	smilesArray=SplittingGeneratorPFAS_Script.getPFASSmiles(filePath);
//		System.out.println(smilesArray.size());
		//*************************************************************************************************
		
				
//		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_Embedding_Cosine;
//		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_Embedding_Euclidean;		
//		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Cosine; 		
		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Euclidean;
//		String applicability_domain=DevQsarConstants.Applicability_Domain_OPERA_local_index;
//		String applicability_domain=DevQsarConstants.Applicability_Domain_OPERA_global_index;
		
		System.out.println(applicability_domain+"\t"+splittingName+"\t"+"limitToPFAS="+limitToPFAS);
		
		boolean storeNeighbors=false;
		
		System.out.println("Dataset\tMethod\tFrac_Inside\t"+statName+"_Inside\t"+statName+"_Outside");	

		List<String>datasetNames=new ArrayList<>();
		datasetNames.add("HLC v1 modeling");
		datasetNames.add("VP v1 modeling");
		datasetNames.add("BP v1 modeling");
		datasetNames.add("WS v1 modeling");
		datasetNames.add("LogP v1 modeling");
		datasetNames.add("MP v1 modeling");
		
		
		String methodName="knn_regressor_1.2";
		
				
		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		if (modelSetName.contains("2.0") && !applicability_domain.equals(DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Cosine)) {
			System.out.println("Invalid AD!");
			return;			
		}
				
		for (String datasetName:datasetNames) {
			
			String filepathReport = "data/reports/" + modelSetName +"/"+ datasetName+"_"+methodName + "_PredictionReport.json";
			
//			System.out.println(filepathReport);
			
			PredictionReport predictionReport=SampleReportWriter.getReport(filepathReport);
			
			
			calculateAD_stats(predictionReport, splittingName, methodName, statName, applicability_domain,
					storeNeighbors, descriptorSetName, datasetName);
			
			predictionReport.AD=applicability_domain;
			
			if(true) continue;
			
			//Write out new reports with AD info: (TODO later need to store this info in the database...
			String filepathReport2 = "data/reports/" + modelSetName +"/"+ datasetName+"_"+methodName + "_PredictionReport_withAD.json";
			Utilities.saveJson(predictionReport, filepathReport2);

		}//end loop over datasets
		
	}
	

	public void runCaseStudyExpProp_All_Endpoints_allDescriptorsAD() {

		
		String modelSetName="WebTEST2.0";
		String splittingName =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
//		boolean limitToPFAS=true;
		boolean limitToPFAS=false;

		String statName="MAE_Test";
//		String statName="Q2_Test";
		
		String listName="PFASSTRUCTV4";		
		String folder="data/dev_qsar/dataset_files/";
		String filePath=folder+listName+"_qsar_ready_smiles.txt";
		
//		HashSet<String>smilesArray=null;//dont need since pulling pfas specific report json		
//		if(limitToPFAS)	smilesArray=SplittingGeneratorPFAS_Script.getPFASSmiles(filePath);
//		System.out.println(smilesArray.size());
		//*************************************************************************************************
		
				
//		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_Embedding_Cosine;
//		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_Embedding_Euclidean;		
//		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Cosine; 		
		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Euclidean;
//		String applicability_domain=DevQsarConstants.Applicability_Domain_OPERA_local_index;
		
		System.out.println(applicability_domain+"\t"+splittingName+"\t"+"limitToPFAS="+limitToPFAS);
		
		boolean storeNeighbors=false;
		

		System.out.println("Dataset\tMethod\tFrac_Inside\t"+statName+"_Inside\t"+statName+"_Outside");	

		List<String>datasetNames=new ArrayList<>();
//		datasetNames.add("HLC v1 modeling");
//		datasetNames.add("WS v1 modeling");
//		datasetNames.add("VP v1 modeling");
//		datasetNames.add("LogP v1 modeling");
//		datasetNames.add("BP v1 modeling");
//		datasetNames.add("MP v1 modeling");
		
		datasetNames.add("exp_prop_96HR_FHM_LC50_v1 modeling");
		
		List<String>methodNames=new ArrayList<>();
//		methodNames.add(DevQsarConstants.RF);
		methodNames.add(DevQsarConstants.XGB);
		
				
		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		if (modelSetName.contains("2.0") && !applicability_domain.equals(DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Euclidean)) {
			System.out.println("Invalid AD!");
			return;			
		}
				
		for (String datasetName:datasetNames) {
			
			PredictionReport predictionReport=SampleReportWriter.getReport(modelSetName, datasetName, splittingName,limitToPFAS);
			
			for (String methodName:methodNames) {
				calculateAD_stats(predictionReport, splittingName, methodName, statName, applicability_domain,
					storeNeighbors, descriptorSetName, datasetName);
			}
			
			Hashtable<String, ApplicabilityDomainPrediction>htAD_Consensus=addConsensusAD(predictionReport, methodNames);
			
//			calculateAD_statsConsensus(predictionReport, htAD_Consensus, statName, datasetName);
			
			predictionReport.AD=applicability_domain;
			
			//Write out new reports with AD info: (TODO later need to store this info in the database...
			SampleReportWriter.writeReportWithAD(modelSetName, datasetName, splittingName, limitToPFAS, predictionReport);

		}//end loop over datasets
		
	}
	
	
public void runCaseStudyExpProp_All_Endpoints_allDescriptorsAD_kNN() {

		
		String modelSetName="WebTEST2.0";
		String splittingName =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
//		boolean limitToPFAS=true;
		boolean limitToPFAS=false;

		String statName="MAE_Test";
//		String statName="Q2_Test";
		
		String listName="PFASSTRUCTV4";		
		String folder="data/dev_qsar/dataset_files/";
		String filePath=folder+listName+"_qsar_ready_smiles.txt";
		
//		HashSet<String>smilesArray=null;//dont need since pulling pfas specific report json		
//		if(limitToPFAS)	smilesArray=SplittingGeneratorPFAS_Script.getPFASSmiles(filePath);
//		System.out.println(smilesArray.size());
		//*************************************************************************************************
				
		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Cosine; 		
		System.out.println(applicability_domain+"\t"+splittingName+"\t"+"limitToPFAS="+limitToPFAS);
		
		boolean storeNeighbors=false;

		System.out.println("Dataset\tMethod\tFrac_Inside\t"+statName+"_Inside\t"+statName+"_Outside");	

		List<String>datasetNames=new ArrayList<>();
		datasetNames.add("HLC v1 modeling");
		datasetNames.add("VP v1 modeling");
		datasetNames.add("BP v1 modeling");
		datasetNames.add("WS v1 modeling");
		datasetNames.add("LogP v1 modeling");
		datasetNames.add("MP v1 modeling");

		String methodName="knn_regressor_1.2";
		
		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		if (modelSetName.contains("2.0") && !applicability_domain.equals(DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Cosine)) {
			System.out.println("Invalid AD!");
			return;			
		}
				
		for (String datasetName:datasetNames) {
			
			String filepathReport = "data/reports/" + modelSetName +"/"+ datasetName+"_"+methodName + "_PredictionReport.json";

			PredictionReport predictionReport=SampleReportWriter.getReport(filepathReport);

			calculateAD_stats(predictionReport, splittingName, methodName, statName, applicability_domain,
					storeNeighbors, descriptorSetName, datasetName);

			predictionReport.AD=applicability_domain;

			String filepathReport2 = "data/reports/" + modelSetName +"/"+ datasetName+"_"+methodName + "_PredictionReport_withAD.json";
			Utilities.saveJson(predictionReport, filepathReport2);

		}//end loop over datasets
		
	}
	
	private Hashtable<String, ApplicabilityDomainPrediction> addConsensusAD(PredictionReport predictionReport,List<String>consensusMethods) {
		
		Hashtable<String, ApplicabilityDomainPrediction> htAD = new Hashtable<>();


		for (int i=0;i<predictionReport.predictionReportDataPoints.size();i++) {				
			PredictionReportDataPoint dp=predictionReport.predictionReportDataPoints.get(i);
			
			boolean allWithinAD=true;
			
			if(dp.qsarPredictedValues.get(0).splitNum!=DevQsarConstants.TEST_SPLIT_NUM) continue;
			
			for (QsarPredictedValue qpv:dp.qsarPredictedValues) {
				String currentMethod=qpv.qsarMethodName.substring(0,qpv.qsarMethodName.indexOf("_"));
				if(!consensusMethods.contains(currentMethod)) continue;
				if(qpv.AD==null) {
					System.out.println("null AD for "+dp.canonQsarSmiles);
					continue;
				}
				if(!qpv.AD) allWithinAD=false;
			}
		
			for (QsarPredictedValue qpv:dp.qsarPredictedValues) {
				
				String currentMethod=qpv.qsarMethodName.substring(0,qpv.qsarMethodName.indexOf("_"));
		
				if(currentMethod.equals("consensus")) {
					qpv.AD=allWithinAD;
					
					ApplicabilityDomainPrediction adp=new ApplicabilityDomainPrediction();					
					adp.id=dp.canonQsarSmiles;
					adp.AD=qpv.AD;					
					htAD.put(dp.canonQsarSmiles, adp);
//					System.out.println(adp.id+"\t"+adp.AD);
				}
			}
		}
		return htAD;
	}
	
	private PredictionReport calculateAD_stats(PredictionReport predictionReport, String splittingName, String methodName,
			String statName, String applicability_domain, boolean storeNeighbors,
			String descriptorSetName, String datasetName) {
		
		
		PredictionReportModelMetadata prmm=getModelMetadata(predictionReport, methodName);
//			System.out.println(embeddingTsv);

		boolean remove_log_p = false;
		if(datasetName.contains("LogP")) remove_log_p=true;
		
		CalculationInfo ci = new CalculationInfo();
		ci.remove_log_p = remove_log_p;
		ci.qsarMethodEmbedding = qsarMethodEmbedding;
		ci.datasetName=datasetName;
		ci.descriptorSetName=descriptorSetName;
		ci.splittingName=splittingName;
		
		ModelData data = ModelData.initModelData(ci,false);
		
//			System.out.println(data.predictionSetInstances);
//			if(true)return;

		//Run AD calculations using webservice:	
		
		String strResponse=null;
		
		if (prmm.descriptorEmbeddingTsv!=null) {
			strResponse=mws.callPredictionApplicabilityDomain(data.trainingSetInstances,data.predictionSetInstances,
					remove_log_p,prmm.descriptorEmbeddingTsv,applicability_domain).getBody();
			
		} else {
			strResponse=mws.callPredictionApplicabilityDomain(data.trainingSetInstances,data.predictionSetInstances,
					remove_log_p,applicability_domain).getBody();
			
		}
		
//			System.out.println(strResponse);
//			String strResponse=strSampleResponse;

		Hashtable<String, ApplicabilityDomainPrediction>htAD=convertResponse(strResponse,storeNeighbors);

		PredictionStatisticsScript.addADsToReport(predictionReport, prmm.qsarMethodName, htAD);
		
//			for (ApplicabilityDomainPrediction pred:adPredictions) {
//				if (!pred.AD)
//					System.out.println(pred.id+"\t"+pred.AD);
//			}
//			
////			System.out.println("Results="+Utilities.gson.toJson(adPredictions)+"\n");
////			System.out.println(Utilities.gson.toJson(predictionReport.predictionReportModelMetadata.get(0)));
//			
		PredictionStatisticsScript.getStatsInsideAD(predictionReport, htAD,null,prmm);			
//			String stats=getStatsInsideWithFrac(predictionReport);
//
		String statsInside=getStat(prmm, statName+"_inside_AD");
		PredictionStatisticsScript.getStatsOutsideAD(predictionReport, htAD,null,prmm);
//			String statsOutside=getStatsOutside(predictionReport);
				
		String statsOriginal=getStat(prmm, statName);
		String statsCoverage=getStat(prmm, "Coverage_Test");
		String statsOutside=getStat(prmm, statName+"_outside_AD");
		
//			System.out.println(statsOriginal);
		
		System.out.println(datasetName.replace(" v1 modeling", "")+"\t"+methodName+"\t"+statsCoverage+"\t"+statsInside+"\t"+statsOutside);
		
		predictionReport.AD=applicability_domain;
		
		return predictionReport;
	}
	
	private void calculateAD_statsConsensus(PredictionReport predictionReport, 
			Hashtable<String, ApplicabilityDomainPrediction>htAD,  
			String statName, String datasetName) {
		
		PredictionReportModelMetadata prmm=getModelMetadata(predictionReport, "consensus");			
		PredictionStatisticsScript.getStatsInsideAD(predictionReport, htAD,null,prmm);			
//			String stats=getStatsInsideWithFrac(predictionReport);
//
		String statsInside=getStat(prmm, statName+"_inside_AD");
		PredictionStatisticsScript.getStatsOutsideAD(predictionReport, htAD,null,prmm);
//			String statsOutside=getStatsOutside(predictionReport);
				
		String statsOriginal=getStat(prmm, statName);
		String statsCoverage=getStat(prmm, "Coverage_Test");
		String statsOutside=getStat(prmm, statName+"_outside_AD");
		
//			System.out.println(statsOriginal);
		System.out.println(datasetName.replace(" v1 modeling", "")+"\tConsensus\t"+statsCoverage+"\t"+statsInside+"\t"+statsOutside);
		
	}
	
	PredictionReportModelMetadata getModelMetadata(PredictionReport predictionReport, String methodName) {
		
		for (PredictionReportModelMetadata modelMetadata:predictionReport.predictionReportModelMetadata) {
			
//			System.out.println(modelMetadata.qsarMethodName+"\t"+modelMetadata.descriptorEmbeddingTsv);
			if (modelMetadata.qsarMethodName.contains(methodName)) {
				return modelMetadata;
			}
			
		}
		return null;
	}

	public void runCaseStudyExpProp_All_Endpoints_just_R2_NOAD() {
		String modelSetName="WebTEST2.1";
//		String modelSetName="WebTEST2.0";
		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;

		List<String>datasetNames=new ArrayList<>();
		datasetNames.add("HLC v1");
		datasetNames.add("VP v1");
		datasetNames.add("BP v1");
		datasetNames.add("WS v1");
		datasetNames.add("LogP v1");
		datasetNames.add("MP v1");
		System.out.println("datasetName\tR2_NO_AD");

		for (String datasetName:datasetNames) {
			PredictionReport predictionReport=SampleReportWriter.getReport(modelSetName, datasetName, splitting);
			System.out.println(datasetName+"\t"+getR2NoAD(predictionReport));
		}
	}
	
	
	public void runCaseStudyExpProp_All_Endpoints_PFAS() {
				
		
//		String modelSetName="WebTEST2.1 PFAS";
//		String splitting =SplittingGeneratorPFAS_Script.splittingPFASOnly;

//		String modelSetName="WebTEST2.1 All but PFAS";
//		String splitting =SplittingGeneratorPFAS_Script.splittingAllButPFAS;
		
		String modelSetName="WebTEST2.1";
		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;


//		String modelSetName="WebTEST2.0 PFAS";
//		String splitting =SplittingGeneratorPFAS_Script.splittingPFASOnly;

//		String modelSetName="WebTEST2.0 All but PFAS";
//		String splitting =SplittingGeneratorPFAS_Script.splittingAllButPFAS;

//		String modelSetName="WebTEST2.0";
//		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;

				
		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_Embedding_Cosine;
//		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_Embedding_Euclidean;		
//		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Cosine; 		
//		String applicability_domain=DevQsarConstants.Applicability_Domain_OPERA_local_index;
		
		System.out.println("\t"+applicability_domain);
//		System.out.println("datasetName\tR2_Test_Inside_AD\tFraction_inside_AD\tProduct\tR2_Test_Outside_AD");
		System.out.println("Dataset\tR2_Inside\tFrac_Inside\tR2_Outside");
		
		boolean storeNeighbors=false;
		
//		serverModelBuilding=DevQsarConstants.SERVER_819;
//		portModelBuilding=5014;
		
		serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
		portModelBuilding=5004;
		
		Unirest.config().connectTimeout(0).socketTimeout(0);
		
		ModelWebService mws=new ModelWebService(serverModelBuilding, portModelBuilding);
		
		
		String listName="PFASSTRUCTV4";		
		String folder="data/dev_qsar/dataset_files/";
		String filePath=folder+listName+"_qsar_ready_smiles.txt";
		HashSet<String>smilesArray=SplittingGeneratorPFAS_Script.getPFASSmiles(filePath);

		List<String>datasetNames=new ArrayList<>();

//		datasetNames.add("HLC from exp_prop and chemprop");
//		datasetNames.add("WS from exp_prop and chemprop");
//		datasetNames.add("VP from exp_prop and chemprop");
//		datasetNames.add("LogP from exp_prop and chemprop");
//		datasetNames.add("MP from exp_prop and chemprop");
//		datasetNames.add("BP from exp_prop and chemprop");

		datasetNames.add("HLC v1");
		datasetNames.add("VP v1");
		datasetNames.add("BP v1");
		datasetNames.add("WS v1");
		datasetNames.add("LogP v1");
		datasetNames.add("MP v1");

		
		
		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		if (modelSetName.contains("2.0") && !applicability_domain.equals(DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Cosine)) {
			System.out.println("Invalid AD!");
			return;			
		}
				
		for (String datasetName:datasetNames) {
						
			boolean remove_log_p = false;
			if(datasetName.contains("LogP")) remove_log_p=true;
			
			CalculationInfoGA ci = new CalculationInfoGA();
			ci.num_generations = 100;			
//			if (datasetName.contains("BP") || splitting.equals("T=all but PFAS, P=PFAS")) ci.num_generations=10;//takes too long to do 100			

			ci.remove_log_p = remove_log_p;
			ci.qsarMethodEmbedding = qsarMethodEmbedding;
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=splitting;
						
			DescriptorEmbedding de=null;
			
			if (modelSetName.contains("2.1")) {
				de=getEmbedding(ci);
//				System.out.println(de.getEmbeddingTsv());
				
				if (de==null) {
					continue;
				}
			} 
			
		
			ModelData data = ModelData.initModelData(ci,false);
			
//			System.out.println(data.predictionSetInstances);
//			if(true)return;

			//Run AD calculations using webservice:			
			String strResponse=null;
			
			if (de==null) {
				strResponse=mws.callPredictionApplicabilityDomain(data.trainingSetInstances,data.predictionSetInstances,
						remove_log_p,applicability_domain).getBody();				
			} else {
				strResponse=mws.callPredictionApplicabilityDomain(data.trainingSetInstances,data.predictionSetInstances,
						remove_log_p,de.getEmbeddingTsv(),applicability_domain).getBody();
			}
			

//			System.out.println(strResponse);
//			String strResponse=strSampleResponse;

			Hashtable<String, ApplicabilityDomainPrediction>adPredictions=convertResponse(strResponse,storeNeighbors);

			
//			System.out.println("Results="+Utilities.gson.toJson(adPredictions)+"\n");
			
			PredictionReport predictionReport=SampleReportWriter.getReport(modelSetName, datasetName, splitting);
			
//			System.out.println(Utilities.gson.toJson(predictionReport.predictionReportModelMetadata.get(0)));
			
			if (splitting.equals(DevQsarConstants.SPLITTING_RND_REPRESENTATIVE)) {
				PredictionStatisticsScript.getStatsInsideAD(predictionReport, adPredictions,smilesArray);
			} else {
				PredictionStatisticsScript.getStatsInsideAD(predictionReport, adPredictions,null);	
			}
			
			String stats=getStatsInsideWithFrac(predictionReport);

			if (splitting.equals(DevQsarConstants.SPLITTING_RND_REPRESENTATIVE)) {
				PredictionStatisticsScript.getStatsOutsideAD(predictionReport, adPredictions,smilesArray);
			} else {
				PredictionStatisticsScript.getStatsOutsideAD(predictionReport, adPredictions,null);					
			}
			
			String statsOutside=getStatsOutside(predictionReport);
			

			System.out.println(datasetName.replace(" from exp_prop and chemprop", "")+"\t"+stats+"\t"+statsOutside);

			
			
//			System.out.println(Utilities.gson.toJson(predictionReport.predictionReportModelMetadata.get(0)));
		}
	}

	/**
	 * For now look at consensus
	 * 
	 * @param report
	 * @return
	 */
	String getStatsInside(PredictionReport report) {
		DecimalFormat df=new DecimalFormat("0.000");
		
		for (PredictionReportModelMetadata prmm:report.predictionReportModelMetadata) {

			if (!prmm.qsarMethodName.contains("consensus")) continue;

			Double coverage_TEST=getStat(prmm.predictionReportModelStatistics,"Coverage_Test");
			Double PearsonRSQ_Test_inside_AD=getStat(prmm.predictionReportModelStatistics,"PearsonRSQ_Test_inside_AD");

			double product=PearsonRSQ_Test_inside_AD*coverage_TEST;
			
//			return(df.format(PearsonRSQ_Test_inside_AD)+"\t"+df.format(coverage_TEST)+"\t"+df.format(product));
			return(df.format(PearsonRSQ_Test_inside_AD));
		}
		
		return null;
		
	}
	
	
	String getStatsInsideWithFrac(PredictionReport report) {
		DecimalFormat df=new DecimalFormat("0.000");
		
		for (PredictionReportModelMetadata prmm:report.predictionReportModelMetadata) {

			if (!prmm.qsarMethodName.contains("consensus")) continue;

			Double coverage_TEST=getStat(prmm.predictionReportModelStatistics,"Coverage_Test");
			Double PearsonRSQ_Test_inside_AD=getStat(prmm.predictionReportModelStatistics,"PearsonRSQ_Test_inside_AD");

			double product=PearsonRSQ_Test_inside_AD*coverage_TEST;
			
			return(df.format(PearsonRSQ_Test_inside_AD)+"\t"+df.format(coverage_TEST));
//			return(df.format(PearsonRSQ_Test_inside_AD));
		}
		
		return null;
		
	}
	
	String getStatWithFrac(PredictionReportModelMetadata prmm,String statName) {
		DecimalFormat df=new DecimalFormat("0.000");
		Double coverage_TEST=getStat(prmm.predictionReportModelStatistics,"Coverage_Test");
		Double statValue=getStat(prmm.predictionReportModelStatistics,statName);
		return(df.format(statValue)+"\t"+df.format(coverage_TEST));
	}
	
	String getStat(PredictionReportModelMetadata prmm,String statName) {
		DecimalFormat df=new DecimalFormat("0.000");
		Double statValue=getStat(prmm.predictionReportModelStatistics,statName);
		return(df.format(statValue));
	}
	
	String getR2NoAD(PredictionReport report) {
		DecimalFormat df=new DecimalFormat("0.000");
		
		for (PredictionReportModelMetadata prmm:report.predictionReportModelMetadata) {
			if (!prmm.qsarMethodName.contains("consensus")) continue;
			Double PearsonRSQ_Test=getStat(prmm.predictionReportModelStatistics,"PearsonRSQ_Test");
			return(df.format(PearsonRSQ_Test));
		}
		return null;
	}

	
	
	
	/**
	 * For now look at consensus
	 * 
	 * @param report
	 * @return
	 */
	String getStatsOutside(PredictionReport report) {
		DecimalFormat df=new DecimalFormat("0.000");
		
		for (PredictionReportModelMetadata prmm:report.predictionReportModelMetadata) {

			if (!prmm.qsarMethodName.contains("consensus")) continue;

			Double PearsonRSQ_Test_outside_AD=getStat(prmm.predictionReportModelStatistics,"PearsonRSQ_Test_outside_AD");
			return(df.format(PearsonRSQ_Test_outside_AD));
		}
		
		return null;
		
	}
	
	
	Double getStat(List<PredictionReportModelStatistic>stats,String statisticName) {
		
		for (PredictionReportModelStatistic stat:stats) {
			if(statisticName.equals(stat.statisticName)) {
				return stat.statisticValue;
			}
//			System.out.println(stat.statisticName);
		}
		
		return Double.NaN;
		
	}
	
	
	
	
	DescriptorEmbedding getEmbedding(CalculationInfoGA ci) {
		
		DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);
		
		if (descriptorEmbedding==null) {//look for one of the ones made using offline python run:			
			ci.num_jobs=2;//just takes slighter longer
			ci.n_threads=16;//doesnt impact knn
			descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);				
		}			

		if (descriptorEmbedding == null) {
//			descriptorEmbedding = ews2.generateEmbedding(serverModelBuilding, portModelBuilding, lanId,ci);
//			System.out.println("New embedding from web service:"+descriptorEmbedding.getEmbeddingTsv());
			System.out.println("Dont have existing embedding:"+ci.toString());
			return null;
			
		} else {
//			System.out.println("Have embedding from db:"+descriptorEmbedding.getEmbeddingTsv());
			return descriptorEmbedding;
		}
	}
	
	public static Hashtable<String, ApplicabilityDomainPrediction> convertResponse(String response,boolean storeNeighbors) {

		List<ApplicabilityDomainPrediction>preds=new ArrayList<>();
		
		String [] lines=response.split("\n");
		
		int counter=1;
		for (String line:lines) {
			
//			System.out.println(counter+"\t"+line);
		
			ApplicabilityDomainPrediction pred=new ApplicabilityDomainPrediction();
			
			JsonObject jo=Utilities.gson.fromJson(line, JsonObject.class);
			
			pred.id=jo.get("idTest").getAsString();
			pred.AD=jo.get("AD").getAsBoolean();
			
			if (storeNeighbors) {
				pred.idNeighbors=new ArrayList<>();

				List<String> keys = jo.entrySet()
						.stream()
						.map(i -> i.getKey())
						.collect(Collectors.toCollection(ArrayList::new));

				for (String key:keys) {
					if(key.contains("Neighbor")) {
						pred.idNeighbors.add(jo.get(key).getAsString());
					}
				}

			}
			
			preds.add(pred);
						
//			System.out.println(counter+"\t"+Utilities.gson.toJson(pred)+"\n");
			counter++;
		}
		
		Hashtable<String, ApplicabilityDomainPrediction> htAD = new Hashtable<>();

		for (ApplicabilityDomainPrediction ad : preds) {
			htAD.put(ad.id, ad);
//			System.out.println(ad.id+"\t"+ad.AD);
		}

		
		return htAD;
	}
	
	public static void main(String[] args) {
		ApplicabilityDomainScript ads=new ApplicabilityDomainScript();
		
		ads.runCaseStudyExpProp_All_Endpoints_modelSpecificAD();
		ads.runCaseStudyExpProp_All_Endpoints_allDescriptorsAD();
		
//		ads.runCaseStudyExpProp_All_Endpoints_modelSpecificAD_kNN();
//		ads.runCaseStudyExpProp_All_Endpoints_allDescriptorsAD_kNN();

//		ads.runCaseStudyExpProp_All_Endpoints();
//		ads.runCaseStudyExpProp_All_Endpoints_just_R2_NOAD();
//		ads.runCaseStudyExpProp_All_Endpoints_PFAS();
		
	}

}
