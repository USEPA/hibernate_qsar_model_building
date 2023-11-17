package gov.epa.run_from_java.scripts;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInConsensusModel;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInConsensusMethodServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInModelSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dsstox.entity.GenericSubstance;
import gov.epa.databases.dsstox.service.DsstoxCompoundServiceImpl;
import gov.epa.databases.dsstox.service.GenericSubstanceServiceImpl;
import gov.epa.endpoints.datasets.descriptor_values.SciDataExpertsDescriptorValuesCalculator;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.models.WebServiceModelBuilder;
import gov.epa.run_from_java.scripts.ApplicabilityDomainScript.ApplicabilityDomainPrediction;
import gov.epa.web_services.ModelWebService;
import gov.epa.web_services.embedding_service.CalculationInfo;
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer;
import kong.unirest.HttpResponse;

/**
* @author TMARTI02
*/
public class GeneratePredictionTsvFromCASList {

	void createTsvFromCASList() {
		
		String workflow="qsar-ready";
		String serverHost="https://hcd.rtpnc.epa.gov";
		SciDataExpertsStandardizer standardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY,workflow,serverHost);
		GenericSubstanceServiceImpl gss=new GenericSubstanceServiceImpl();
		DsstoxCompoundServiceImpl dcs=new DsstoxCompoundServiceImpl();

		SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(serverHost, "tmarti02");

		DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();
		DescriptorSet descriptorSet = descriptorSetService.findByName("WebTEST-default");
		 DescriptorValuesService descriptorValuesService = new DescriptorValuesServiceImpl();
		
		try {
			
//			String filepathIn="data/cas list/bp cas list.txt";
			String filepathIn="data/cas list/bp cas list2.txt";
//			String filepathIn="data/cas list/mp cas list.txt";

			String filepathOut=filepathIn.replace("txt","tsv");

			
			List<String> lines= Files.readAllLines(Paths.get(filepathIn));
			FileWriter fw=new FileWriter(filepathOut);

			
			Connection conn=SqlUtilities.getConnectionPostgres();
			
			String sqlHeader="select headers_tsv from qsar_descriptors.descriptor_sets d\n"+					
					"where d.\"name\"='"+descriptorSet.getName()+"';";
		
			String instanceHeader="ID\tProperty\t"+SqlUtilities.runSQL(conn, sqlHeader);

			fw.write(instanceHeader+"\r\n");
			
			for(String line:lines) {
				
				String [] vals=line.split("\t");
				
				String cas=vals[0];
				String property=vals[1];
				
				GenericSubstance gs=gss.findByCasrn(cas);
				String smiles=null;
				
				if (cas.equals("15957-30-9")) {
					smiles="O=C(O)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(C(F)(F)F)C(F)(F)F";
				} else if (cas.equals("18122-53-7")) {
					smiles="O=C(O)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(C(F)(F)F)C(F)(F)F";
				} else if (cas.equals("338-82-9")) {
					smiles="C(C(F)(F)F)(C(F)(F)N(C(C(F)(F)F)(F)F)C(C(F)(F)C(F)(F)F)(F)F)(F)F";
				} else if (cas.equals("558-75-8")) {
					smiles="C(F)(F)(N(C(C(F)(F)F)(F)F)C(C(F)(F)F)(F)F)C1(C(C(C(C(C1(F)F)(F)F)(F)F)(F)F)(F)F)F";
				} else if (cas.equals("357-99-3")) {
					smiles="C(OC(C(C(F)(F)F)F)(F)F)CC";
				} else {
					smiles=gs.getGenericSubstanceCompound().getCompound().getSmiles();
				}

				HttpResponse<String> standardizeResponse = standardizer.callQsarReadyStandardizePost(smiles, false);
				String jsonResponse = SciDataExpertsStandardizer.getResponseBody(standardizeResponse, false);
				String qsarSmiles=SciDataExpertsStandardizer.getQsarReadySmilesFromPostJson(jsonResponse, false);

				String sql="select values_tsv from qsar_descriptors.descriptor_values dv where dv.canon_qsar_smiles='"+qsarSmiles+"' and fk_descriptor_set_id=6";
				
				
				String values_tsv=SqlUtilities.runSQL(conn, sql);

				if (values_tsv==null) {
					System.out.println("null val, calculating:");
					values_tsv=calc.calculateDescriptors(qsarSmiles, descriptorSet);
					descriptorValuesService.create(new DescriptorValues(qsarSmiles, descriptorSet, values_tsv, "tmarti02"));
				}
				System.out.println(cas+"\t"+smiles+"\t"+qsarSmiles);
//				System.out.println(cas+"\t"+qsarSmiles+"\t"+property+"\t"+values_tsv);
				fw.write(qsarSmiles+"\t"+property+"\t"+values_tsv+"\r\n");
			}
			
			fw.flush();
			fw.close();
			
			
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	

	void createTsvFromSmilesList() {
		
		String workflow="qsar-ready";
		String serverHost="https://hcd.rtpnc.epa.gov";
		SciDataExpertsStandardizer standardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY,workflow,serverHost);
		GenericSubstanceServiceImpl gss=new GenericSubstanceServiceImpl();
		DsstoxCompoundServiceImpl dcs=new DsstoxCompoundServiceImpl();

		SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(serverHost, "tmarti02");

		DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();
		DescriptorSet descriptorSet = descriptorSetService.findByName("WebTEST-default");
		 DescriptorValuesService descriptorValuesService = new DescriptorValuesServiceImpl();
		
		try {
			
			String filename="mp smiles list.txt";
//			String filename="bp smiles list.txt";
			
			
//			FileWriter fw=new FileWriter("data/cas list/bp list.tsv");			
//			List<String> lines= Files.readAllLines(Paths.get("data/cas list/bp list.txt"));

			FileWriter fw=new FileWriter("data/cas list/"+filename.replace("txt","tsv"));			
			
			List<String> lines= Files.readAllLines(Paths.get("data/cas list/"+filename));

			
			Connection conn=SqlUtilities.getConnectionPostgres();
			
			String sqlHeader="select headers_tsv from qsar_descriptors.descriptor_sets d\n"+					
					"where d.\"name\"='"+descriptorSet.getName()+"';";
		
			String instanceHeader="ID\tProperty\t"+SqlUtilities.runSQL(conn, sqlHeader);

			fw.write(instanceHeader+"\r\n");
			
			for(String line:lines) {
				
				String [] vals=line.split("\t");
				
				String smiles=vals[0];
				String property=vals[1];
				
				HttpResponse<String> standardizeResponse = standardizer.callQsarReadyStandardizePost(smiles, false);
				String jsonResponse = SciDataExpertsStandardizer.getResponseBody(standardizeResponse, false);
				String qsarSmiles=SciDataExpertsStandardizer.getQsarReadySmilesFromPostJson(jsonResponse, false);

				String sql="select values_tsv from qsar_descriptors.descriptor_values dv where dv.canon_qsar_smiles='"+qsarSmiles+"' and fk_descriptor_set_id=6";
				
				
				String values_tsv=SqlUtilities.runSQL(conn, sql);

				if (values_tsv==null) {
					System.out.println("null val, calculating:");
					values_tsv=calc.calculateDescriptors(qsarSmiles, descriptorSet);
					descriptorValuesService.create(new DescriptorValues(qsarSmiles, descriptorSet, values_tsv, "tmarti02"));
				}
				
//				System.out.println(qsarSmiles+"\t"+property+"\t"+values_tsv);
				System.out.println(smiles+"\t"+qsarSmiles+"\t"+property);
				fw.write(qsarSmiles+"\t"+property+"\t"+values_tsv+"\r\n");
			}
			
			fw.flush();
			fw.close();
			
			
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	
	void runPredictions() {
		String splittingName="RND_REPRESENTATIVE";
//		String splittingName="T=PFAS only, P=PFAS";
		
		
		String folder="data/cas list/";
		
//		String filepathTsv=folder+"bp cas list.tsv";
		String filepathTsv=folder+"bp cas list2.tsv";
//		String filepathTsv=folder+"bp smiles list.tsv";
		String datasetName="BP v1 modeling";
		
//		String filepathTsv=folder+"mp cas list.tsv";
//		String filepathTsv=folder+"mp smiles list.tsv";
//		String datasetName="MP v1 modeling";
		
		
		List <Model>modelsInCon=getEmbeddedCON_Model(datasetName,splittingName);
//		System.out.println(modelCON.getId());

		
		String modelWsServer=DevQsarConstants.SERVER_LOCAL;
		int modelWsPort=5004;

		ModelWebService mws = new ModelWebService(modelWsServer, modelWsPort);
		WebServiceModelBuilder mb = new WebServiceModelBuilder(mws, "tmarti02");
		
		String predictionSetInstances=null;
		try {
			predictionSetInstances = new BufferedReader(new FileReader(filepathTsv)).lines().collect(Collectors.joining("\n"));
			
//			System.out.println(predictionSetInstances);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		ModelPrediction[] mps1=mb.getModelPredictionsFromAPI(modelsInCon.get(0), predictionSetInstances);
		ModelPrediction[] mps2=mb.getModelPredictionsFromAPI(modelsInCon.get(1), predictionSetInstances);
//		System.out.println(modelsInCon.get(0).getMethod().getName());
		
		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_Embedding_Cosine;
		Hashtable<String, ApplicabilityDomainPrediction>htAD1=runAD(modelsInCon.get(0),mws,applicability_domain,predictionSetInstances);
		Hashtable<String, ApplicabilityDomainPrediction>htAD2=runAD(modelsInCon.get(1),mws,applicability_domain,predictionSetInstances);
						
		for (int i=0;i<mps1.length;i++) {
			ModelPrediction mp1=mps1[i];
			ModelPrediction mp2=mps2[i];
			
			double pred_con=(mp1.pred+mp2.pred)/2.0;

//			System.out.println(mp1.id+"\t"+mp1.exp+"\t"+pred_con);
			
			boolean AD1=htAD1.get(mp1.id).AD;
			boolean AD2=htAD2.get(mp1.id).AD;
			boolean AD_CON=AD1&&AD2;
						
//			if(!AD_CON)
				System.out.println(mp1.id+"\t"+mp1.exp+"\t"+pred_con+"\t"+AD1+"\t"+AD2+"\t"+AD_CON);
			
		}

		
	}
	
	Hashtable<String, ApplicabilityDomainPrediction> runAD(Model model,ModelWebService mws,String applicability_domain,String predictionSetInstances) {
		
		CalculationInfo ci = new CalculationInfo();
		ci.remove_log_p = false;
		ci.datasetName=model.getDatasetName();
		ci.descriptorSetName=model.getDescriptorSetName();
		ci.splittingName=model.getSplittingName();
		
		ModelData data = ModelData.initModelData(ci,false);
		
//			System.out.println(data.predictionSetInstances);
//			if(true)return;

		//Run AD calculations using webservice:			
		String strResponse=mws.callPredictionApplicabilityDomain(data.trainingSetInstances,predictionSetInstances,
					ci.remove_log_p,model.getDescriptorEmbedding().getEmbeddingTsv(),applicability_domain).getBody();
		
//		System.out.println(strResponse);
//			String strResponse=strSampleResponse;
		Hashtable<String, ApplicabilityDomainPrediction>htAD=ApplicabilityDomainScript.convertResponse(strResponse,true);
		return htAD;

	}
	
	
	List<Model> getEmbeddedCON_Model(String datasetName,String splittingName) {
		
		List <Model>modelsInCon=new ArrayList<>();
		
		ModelServiceImpl ms=new ModelServiceImpl();
		ModelInConsensusMethodServiceImpl ms2=new ModelInConsensusMethodServiceImpl();
		List<Model> models=ms.findByDatasetName(datasetName);

		for (Model model:models) {
			if(!model.getSplittingName().equals(splittingName)) continue;
			if(!model.getMethod().getName().equals("consensus_regressor")) continue;

			List<ModelInConsensusModel>models2=ms2.findByConsensusModelId(model.getId());
			
			for (ModelInConsensusModel model2:models2) {
				 if(model2.getModel().getDescriptorEmbedding()!=null) {
					 modelsInCon.add(model2.getModel());
				 }
			}
		}
		return modelsInCon; 
	}
	
	
	public static void main(String[] args) {
		GeneratePredictionTsvFromCASList g=new GeneratePredictionTsvFromCASList();
//		g.createTsvFromCASList();
//		g.createTsvFromSmilesList();
		g.runPredictions();
//		

	}

}
