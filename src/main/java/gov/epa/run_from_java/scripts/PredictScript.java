package gov.epa.run_from_java.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.stream.Stream;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;

import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.endpoints.datasets.descriptor_values.SciDataExpertsDescriptorValuesCalculator;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.models.ModelStatisticCalculator;
import gov.epa.run_from_java.scripts.ApplicabilityDomainScript.ApplicabilityDomainPrediction;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteValidation;
import gov.epa.util.ExcelSourceReader;
import gov.epa.web_services.ModelWebService;
import gov.epa.web_services.embedding_service.CalculationInfo;
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer;
import kong.unirest.HttpResponse;

/**
 * Runs an external set for a model
 * 
* @author TMARTI02
*/
public class PredictScript {

	public ModelService modelService = new ModelServiceImpl();
	ModelBytesService modelBytesService = new ModelBytesServiceImpl();
	public ModelWebService modelWebService = new ModelWebService(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

	
	String workflow = "qsar-ready";
//	String serverHost = "https://hcd.rtpnc.epa.gov";
	String serverHost = "https://hazard-dev.sciencedataexperts.com";
	SciDataExpertsStandardizer standardizer = new SciDataExpertsStandardizer(workflow, serverHost);
	SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(serverHost, "tmarti02");
	DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();

	
	public static List<DsstoxRecord>getDsstoxRecords() {
		Gson gson=new Gson();

		List<DsstoxRecord>records=new ArrayList<>();
		
		
//		String filepath="data\\dsstox\\json\\2023_04_snapshot_dsstox_records_2024_01_09.json";
		
		String filepath="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\dsstox\\snapshot-2024-11-12\\json\\2024_11_12_snapshot_dsstox_records.json";

		try {
			
			JsonObject jo2 = Utilities.gson.fromJson(new FileReader(filepath), JsonObject.class);
			JsonArray ja =jo2.get("select * from qsar_models.dsstox_records where fk_dsstox_snapshot_id=2").getAsJsonArray();
			
//			JsonArray ja = Utilities.gson.fromJson(new FileReader(filepath), JsonArray.class);

			for (JsonElement je:ja) {
				JsonObject jo=(JsonObject)je;
				Set<Map.Entry<String, JsonElement>> entries = jo.entrySet();

				DsstoxRecord rec=new DsstoxRecord();

				for(Map.Entry<String, JsonElement> entry: entries) {
					String fieldName=entry.getKey();
					JsonElement value=entry.getValue();

					if (value.isJsonNull()) continue;

					if(fieldName.equals("id")) 	rec.setId(value.getAsLong());
					if(fieldName.equals("cid"))	rec.setCid(value.getAsLong());
					if(fieldName.equals("dtxcid")) 	rec.setDtxcid(value.getAsString());
					if(fieldName.equals("dtxsid")) 	rec.setDtxsid(value.getAsString());
					if(fieldName.equals("preferred_name")) 	rec.setPreferredName(value.getAsString());
					if(fieldName.equals("casrn")) 	rec.setCasrn(value.getAsString());
					if(fieldName.equals("smiles")) 	rec.setSmiles(value.getAsString());
					if(fieldName.equals("mol_weight")) 	rec.setMolWeight(value.getAsDouble());
					if(fieldName.equals("mol_image_png_available")) rec.setMolImagePNGAvailable(value.getAsBoolean());

				}
				records.add(rec);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return records;

	}
	
	public static Hashtable<String, DsstoxRecord> getDsstoxHashtableByDTXCID(List<DsstoxRecord>records) {
		Hashtable<String, DsstoxRecord> ht=new Hashtable<>();
		
		for (DsstoxRecord record:records) {
			if(record.getDtxcid()==null) continue;
			ht.put(record.getDtxcid(),record);
		}
		return ht;
	}
	
	public static Hashtable<String, DsstoxRecord> getDsstoxHashtableByCASRN(List<DsstoxRecord>records) {
		Hashtable<String, DsstoxRecord> ht=new Hashtable<>();
		
		for (DsstoxRecord record:records) {
			if(record.getCasrn()==null) continue;
			ht.put(record.getCasrn(),record);
		}
		return ht;
	}

	
	public static Hashtable<String, DsstoxRecord> getDsstoxHashtableByDTXSID(List<DsstoxRecord>records) {
		Hashtable<String, DsstoxRecord> ht=new Hashtable<>();
		
		for (DsstoxRecord record:records) {
			if(record.getDtxsid()==null) continue;
			ht.put(record.getDtxsid(),record);
		}
		return ht;
	}
	
	public void predict(String folder, String tsvFileName, Long modelId,boolean use_pmml, boolean use_sklearn2pmml) throws ConstraintViolationException {
		
		try {
			
			Path path = Paths.get(folder+tsvFileName);

			Stream<String> lines = Files.lines(path);
			String predictionTSV = lines.collect(Collectors.joining("\n"));
			lines.close();
//			System.out.println(predictionTSV);
			
			if (modelId==null) {
				//			logger.error("Model with supplied parameters has not been built");
				return;
			} else if (predictionTSV==null) {
				//			logger.error("Dataset instances were not initialized");
				return;
			}

			Model model=modelService.findById(modelId);
			byte[] bytes = modelBytesService.getBytesSql(modelId, use_pmml);

			String strModelId = String.valueOf(modelId);

			//Following may not be necessary if webservice hasnt been restarted:
			if (use_pmml) {
				String details=new String(model.getDetails());
				HttpResponse<String>response=modelWebService.callInitPmml(bytes, strModelId, details,use_sklearn2pmml);
			} else {
				HttpResponse<String>response=modelWebService.callInitPickle(bytes,strModelId);
			}

			//		System.out.println("Splitting id = "+splitting.getId());
			String predictResponse = modelWebService.callPredict(predictionTSV, strModelId).getBody();
			ModelPrediction[] modelPredictionsArray = Utilities.gson.fromJson(predictResponse, ModelPrediction[].class);
			
			
			List<DsstoxRecord>records=getDsstoxRecords();
			Hashtable<String,DsstoxRecord>htDsstox=getDsstoxHashtableByDTXCID(records);

			Hashtable<String,Double>htPredWS=new Hashtable<>();
			
			for (ModelPrediction mp:modelPredictionsArray) {
				
				String [] dtxcids=mp.id.split("\\|");
				
				for (String dtxcid:dtxcids) {
					DsstoxRecord dr=htDsstox.get(dtxcid);
					String dtxsid=dr.getDtxsid();
					double pred_Neg_Log_molar=mp.pred;
					double pred_molar=Math.pow(10.0, -pred_Neg_Log_molar);
					double pred_g_L=pred_molar*dr.getMolWeight();
//					System.out.println(dtxsid+"\t"+pred_g_L);
					
					htPredWS.put(dtxsid, pred_g_L);
				}
			}
			
			
			String json=Utilities.gson.toJson(htPredWS);
			
			System.out.println(json);
			
			Hashtable<String,Double>htPredWS2=Utilities.gson.fromJson(json, (Hashtable.class));
			
			System.out.println(htPredWS2.get("DTXSID6021953"));
			
			
			FileWriter fw=new FileWriter(folder+"WS pred xgb.json");
			fw.write(json);
			fw.flush();
			fw.close();
			
//			System.out.println(predictResponse);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 *  Generates tsv on the fly by dtxsid for the dataset being predicted by the model 
	 *  
	 * @param folder
	 * @param modelId modelId for the model to use
	 * @param datasetName dataset to make predictions for
	 * @throws ConstraintViolationException
	 */
	public Hashtable<String,Double> predict(String filePathOut, Long modelId,String datasetName) throws ConstraintViolationException {
		
		try {
			
			boolean use_pmml=false;
			boolean use_sklearn2pmml=false;
					
			Model model=modelService.findById(modelId);
			String predictionTSV = ModelData.getOverallInstancesByDTXSID(datasetName, model.getSplittingName(), model.getDescriptorSetName());
			
//			System.out.println(predictionTSV);
			
			byte[] bytes = modelBytesService.getBytesSql(modelId, use_pmml);

			String strModelId = String.valueOf(modelId);

			//Following may not be necessary if webservice hasnt been restarted:
			if (use_pmml) {
				String details=new String(model.getDetails());
				modelWebService.callInitPmml(bytes, strModelId, details,use_sklearn2pmml);
			} else {
				modelWebService.callInitPickle(bytes,strModelId);
			}

			String predictResponse = modelWebService.callPredict(predictionTSV, strModelId).getBody();
			ModelPrediction[] modelPredictionsArray = Utilities.gson.fromJson(predictResponse, ModelPrediction[].class);
			
			Hashtable<String,Double>htPredWS=new Hashtable<>();
			
			for (ModelPrediction mp:modelPredictionsArray) {
				String dtxsid=mp.id;												
				htPredWS.put(dtxsid, mp.pred);
			}
			
			String json=Utilities.gson.toJson(htPredWS);
			
//			System.out.println(json);
//			Hashtable<String,Double>htPredWS2=Utilities.gson.fromJson(json, (Hashtable.class));
//			System.out.println(htPredWS2.get("DTXSID6021953"));

			if (filePathOut!=null) {				
				System.out.println("New file for model predictions:\t"+filePathOut);
				FileWriter fw=new FileWriter(filePathOut);
				fw.write(json);
				fw.flush();
				fw.close();
			}
			
			return htPredWS;			
//			System.out.println(predictResponse);

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	

	public ModelPrediction[] predict(Long modelId,String predictionTSV) throws ConstraintViolationException {
		
		try {
			
			boolean use_pmml=false;
			boolean use_sklearn2pmml=false;
					
			Model model=modelService.findById(modelId);
			
//			System.out.println(predictionTSV);
			
			byte[] bytes = modelBytesService.getBytesSql(modelId, use_pmml);

			String strModelId = String.valueOf(modelId);

			//Following may not be necessary if webservice hasnt been restarted:
			if (use_pmml) {
				String details=new String(model.getDetails());
				modelWebService.callInitPmml(bytes, strModelId, details,use_sklearn2pmml);
			} else {
				modelWebService.callInitPickle(bytes,strModelId);
			}

			String predictResponse = modelWebService.callPredict(predictionTSV, strModelId).getBody();
			ModelPrediction[] modelPredictionsArray = Utilities.gson.fromJson(predictResponse, ModelPrediction[].class);
			
			return modelPredictionsArray;
			
//			System.out.println(predictResponse);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getPropertyNameModel(Long modelId) {
		String sql="select p.name from qsar_models.models m\r\n"
				+ "			join qsar_datasets.datasets d on m.dataset_name=d.name\r\n"
				+ "			join qsar_datasets.properties p on d.fk_property_id = p.id\r\n"
				+ "			where m.id="+modelId+";";

		
		String propertyNameModel=SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql);
		return propertyNameModel;
	}
	
	
	void runExternalSet() {

//		boolean isBinary=true;
//		long modelId=1569L;
//		long fk_dataset_external_id = 533L;//exp_prop_RBIODEG_NITE_OPPT_BY_CAS
//		long fk_dataset_id_omit_training_data = 532L;//exp_prop_RBIODEG_RIFM_BY_CAS
		
		boolean isBinary=false;
//		long modelId=1525L;//mordred- TODO get all the descriptors calculated
		long modelId=1522L;//webtest
		long fk_dataset_external_id = 534L;//QSAR_Toolbox_96HR_Fish_LC50_v3 modeling
		long fk_dataset_id_omit_training_data = 512L;//ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling
		
		boolean omitOnlyTrainingSet=true;
		
		ModelData.debug=false;
		
		Model model=modelService.findById(modelId);
		
		String predictionTSV=ModelData.getExternalPredictionSet(fk_dataset_external_id, fk_dataset_id_omit_training_data, model.getDescriptorSetName(), model.getSplittingName(),omitOnlyTrainingSet);
	
		boolean use_pmml=false;
		boolean use_sklearn2pmml=false;
		

		System.out.println(predictionTSV);
		
		byte[] bytes = modelBytesService.getBytesSql(modelId, use_pmml);

		String strModelId = String.valueOf(modelId);

		//Following may not be necessary if webservice hasnt been restarted:
		if (use_pmml) {
			String details=new String(model.getDetails());
			modelWebService.callInitPmml(bytes, strModelId, details,use_sklearn2pmml);
		} else {
			modelWebService.callInitPickle(bytes,strModelId);
		}

		String predictResponse = modelWebService.callPredict(predictionTSV, strModelId).getBody();
		ModelPrediction[] modelPredictionsArray = Utilities.gson.fromJson(predictResponse, ModelPrediction[].class);
	
		List<ModelPrediction>modelTestPredictions=Arrays.asList(modelPredictionsArray);			

		Map<String, Double> modelTestStatisticValues=null;
		
		if (isBinary) {
			modelTestStatisticValues = 
					ModelStatisticCalculator.calculateBinaryStatistics(modelTestPredictions, 
							DevQsarConstants.BINARY_CUTOFF,
							DevQsarConstants.TAG_TEST);
			
		} else {
			modelTestStatisticValues = 
					ModelStatisticCalculator.calculateContinuousStatistics(modelTestPredictions, 
							-9999.0,
							DevQsarConstants.TAG_TEST);

		}
		

//		System.out.println(Utilities.gson.toJson(modelPredictionsArray));
		System.out.println("\nOverall:"+Utilities.gson.toJson(modelTestStatisticValues));
		
		String strResponse=null;

		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Euclidean;

		CalculationInfo ci = new CalculationInfo();
		ci.remove_log_p = false;
		ci.qsarMethodEmbedding = null;//TODO
		ci.datasetName=model.getDatasetName();
		ci.descriptorSetName=model.getDescriptorSetName();
		ci.splittingName=model.getSplittingName();
		
		ModelData data = ModelData.initModelData(ci,false);
		
		
		if (model.getDescriptorEmbedding()!=null) {
			strResponse=modelWebService.callPredictionApplicabilityDomain(data.trainingSetInstances,predictionTSV,
					false,model.getDescriptorEmbedding().getEmbeddingTsv(),applicability_domain).getBody();
			
		} else {
			strResponse=modelWebService.callPredictionApplicabilityDomain(data.trainingSetInstances,predictionTSV,
					false,applicability_domain).getBody();
			
		}

		boolean storeNeighbors=false;
		Hashtable<String, ApplicabilityDomainPrediction>htAD =  ApplicabilityDomainScript.convertResponse(strResponse,storeNeighbors);

		Map<String, Double> statsInside=PredictionStatisticsScript.StatsAD.getStats(htAD, modelTestPredictions, isBinary,true);
		Map<String, Double> statsOutside=PredictionStatisticsScript.StatsAD.getStats(htAD, modelTestPredictions, isBinary,false);
		
		System.out.println("\nInsideAD:"+Utilities.gson.toJson(statsInside));
		System.out.println("\nOutsideAD:"+Utilities.gson.toJson(statsOutside));
		
	}
	
	
	public List<ModelPrediction> runExternalSet(long modelId, long fk_dataset_external_id,
			long fk_dataset_id_omit_training_data, boolean omitOnlyTraining) {
		
		boolean isBinary=false;
		
		ModelData.debug=false;
		
		Model model=modelService.findById(modelId);
		
		String predictionTSV = ModelData.getExternalPredictionSet(fk_dataset_external_id,
				fk_dataset_id_omit_training_data, model.getDescriptorSetName(), model.getSplittingName(),
				omitOnlyTraining);
	
		boolean use_pmml=false;
		boolean use_sklearn2pmml=false;
		
//		System.out.println(predictionTSV);
		
		byte[] bytes = modelBytesService.getBytesSql(modelId, use_pmml);

		String strModelId = String.valueOf(modelId);

		//Following may not be necessary if webservice hasnt been restarted:
		if (use_pmml) {
			String details=new String(model.getDetails());
			modelWebService.callInitPmml(bytes, strModelId, details,use_sklearn2pmml);
		} else {
			modelWebService.callInitPickle(bytes,strModelId);
		}

		String predictResponse = modelWebService.callPredict(predictionTSV, strModelId).getBody();
		ModelPrediction[] modelPredictionsArray = Utilities.gson.fromJson(predictResponse, ModelPrediction[].class);
	
		List<ModelPrediction>modelTestPredictions=Arrays.asList(modelPredictionsArray);			

		return modelTestPredictions;
		
	}
	
	
	public List<ModelPrediction> run(long modelId,String predictionTSV) {
		
		boolean isBinary=false;
		
		ModelData.debug=false;
		
		Model model=modelService.findById(modelId);
		
	
		boolean use_pmml=false;
		boolean use_sklearn2pmml=false;
		
//		System.out.println(predictionTSV);
		
		byte[] bytes = modelBytesService.getBytesSql(modelId, use_pmml);

		String strModelId = String.valueOf(modelId);

		//Following may not be necessary if webservice hasnt been restarted:
		if (use_pmml) {
			String details=new String(model.getDetails());
			modelWebService.callInitPmml(bytes, strModelId, details,use_sklearn2pmml);
		} else {
			modelWebService.callInitPickle(bytes,strModelId);
		}

		String predictResponse = modelWebService.callPredict(predictionTSV, strModelId).getBody();
		ModelPrediction[] modelPredictionsArray = Utilities.gson.fromJson(predictResponse, ModelPrediction[].class);
	
		List<ModelPrediction>modelTestPredictions=Arrays.asList(modelPredictionsArray);			

		return modelTestPredictions;
		
	}
	
	
	
	void makePlot(String abbrev,boolean haveEmbedding,String plotType,String trainingSet,String predictionSet,boolean initModel) {
		String splittingName=null;
		
		
		if(trainingSet.equals("All")) {
			splittingName="RND_REPRESENTATIVE";
		} else if (trainingSet.equals("PFAS")) {
			splittingName="T=PFAS only, P=PFAS";
		} else if (trainingSet.equals("All But PFAS")) {
			splittingName="T=all but PFAS, P=PFAS";
		}
		
		long modelId = EpisuiteValidation.getModelIdResQsar(abbrev,splittingName, haveEmbedding);
		
		boolean use_pmml=false;
		boolean use_sklearn2pmml=false;

		Model model=modelService.findById(modelId);
		
		String strModelId = String.valueOf(modelId);

		if (initModel) {
			
			byte[] bytes = modelBytesService.getBytesSql(modelId, use_pmml);

			//Following may not be necessary if webservice hasnt been restarted:
			if (use_pmml) {
				String details=new String(model.getDetails());
				modelWebService.callInitPmml(bytes, strModelId, details,use_sklearn2pmml);
			} else {
				modelWebService.callInitPickle(bytes,strModelId);
			}
		}
		
		CalculationInfo ci = new CalculationInfo();
		ci.remove_log_p = false;
		ci.qsarMethodEmbedding = null;//TODO
		ci.datasetName=model.getDatasetName();
		ci.descriptorSetName=model.getDescriptorSetName();
		ci.splittingName=model.getSplittingName();


		ModelData data = ModelData.initModelData(ci,false);
		String trainingTsv=data.trainingSetInstances;
		String predictionTsv=data.predictionSetInstances;

		int numT=data.countTraining;
		int numP=data.countPrediction;
		
		if(trainingSet.equals("All") && !predictionSet.equals("All")) {//Use PFAS instead for prediction set
			ci.splittingName="T=PFAS only, P=PFAS";
			data = ModelData.initModelData(ci,false);
			predictionTsv=data.predictionSetInstances;
			numP=data.countPrediction;
//			System.out.println("T=All, P=PFAS");
		} 
		System.out.println("Property="+abbrev+"\tmodelId="+modelId+"\tnumT="+numT+"\tnumP="+numP);
		
		String modelName=abbrev+" (T="+trainingSet+", P="+predictionSet+")";
		
		modelWebService.callGeneratePlot(trainingTsv,predictionTsv, modelId+"",modelName,plotType);
		

	}
	
	
	public List<ModelPrediction> run(long modelId,String predictionTSV, boolean init) {
		
		boolean isBinary=false;
		
		ModelData.debug=false;
		
		Model model=modelService.findById(modelId);
		
	
		boolean use_pmml=false;
		boolean use_sklearn2pmml=false;
		
//		System.out.println(predictionTSV);
		
		String strModelId = String.valueOf(modelId);
		
		if (init) {
			
			byte[] bytes = modelBytesService.getBytesSql(modelId, use_pmml);

			//Following may not be necessary if webservice hasnt been restarted:
			if (use_pmml) {
				String details=new String(model.getDetails());
				modelWebService.callInitPmml(bytes, strModelId, details,use_sklearn2pmml);
			} else {
				modelWebService.callInitPickle(bytes,strModelId);
			}
		}

		String predictResponse = modelWebService.callPredict(predictionTSV, strModelId).getBody();
		ModelPrediction[] modelPredictionsArray = Utilities.gson.fromJson(predictResponse, ModelPrediction[].class);
	
		List<ModelPrediction>modelTestPredictions=Arrays.asList(modelPredictionsArray);			

		return modelTestPredictions;
		
	}
	
	
	void runPrediction(String smiles,long modelId) {
		
		
		HttpResponse<String> standardizeResponse = standardizer.callQsarReadyStandardizePost(smiles, false);
//		System.out.println("status=" + standardizeResponse.getStatus());
		boolean useFullStandardize=false;
		
		if (standardizeResponse.getStatus() != 200) {
			System.out.println(standardizeResponse.getStatusText());
			return;
		}

		String jsonResponse = SciDataExpertsStandardizer.getResponseBody(standardizeResponse, useFullStandardize);
		String qsarSmiles = SciDataExpertsStandardizer.getQsarReadySmilesFromPostJson(jsonResponse,
					useFullStandardize);
		
		String descriptorSetName="WebTEST-default";
		DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);

		String descriptors=calc.calculateDescriptors(qsarSmiles, descriptorSet);
		
//		String [] headers=descriptorSet.getHeadersTsv().split("\t");
//		String [] values=descriptors.split("\t");
//		System.out.println("\n"+smiles+"\t"+qsarSmiles);
//		for (int i=0;i<headers.length;i++) {
//			System.out.println(headers[i]+"\t"+values[i]);
//		}
		
		boolean use_pmml=false;
		boolean use_sklearn2pmml=false;
		
		Model model=modelService.findById(modelId);
		
		byte[] bytes = modelBytesService.getBytesSql(modelId, use_pmml);
		
		
		if (use_pmml) {
			String details=new String(model.getDetails());
			modelWebService.callInitPmml(bytes, modelId+"", details,use_sklearn2pmml);
		} else {
			modelWebService.callInitPickle(bytes,modelId+"");
		}
		
		String predictionTSV="ID\tProperty\t"+descriptorSet.getHeadersTsv()+"\r\n";
		
//		predictionTSV+="O=C(NC(C(=O)NC(C(=O)N)C(C)CC)CC(C)C)C	-9999	15.86987901	9.18364697	8.52556774	5.22815334	3.85787731	2.81158647	1.29910382	0.57297855	0.52098294	0.09072184	0	1.92398387	0	2.9674333	0	0	0	0	0	0	0	0	-1.04344944	13.02570978	7.0529839	5.65681909	3.21836301	1.93164149	1.05583764	0.52923283	0.22707493	0.16636028	0.03975517	0	1.05837498	0	1.41319997	0	0	0	0	0	0	0	0	-0.35482499	20	9.8496	8.14792899	18.90909091	8.95752301	7.33111659	8.46893085	9.05877771	0	1.23638094	0	0	0	-1.15645253	0	0	-1.19278988	0	0	0	5.32313728	0	5.25889243	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	34.80538738	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	5	0	2	0	0	0	4	0	0	3	0	0	0	1	0	2	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	3	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	7.82664959	0	0	1.69130446	3.78677693	0	0	1.924625	12.21583711	0.49170511	-0.71211073	1.924625	0	6	4	0	45.38741709	5.47808139	0	4.6875	1.171875	0.78125	2	4.22192809	11.95573525	84.4385619	86.4385619	0.97686218	0.02313782	4.21090952	181.17701927	0.45294255	199.42124551	5.24792751	1.51288762	4.16150087	209.49114571	0.58030788	282.19280949	5.64385619	581.26641203	3.05929691	6243.5730006	7.38010993	3.42192809	4.29409588	492.15724925	2.87811257	656	4732.34761677	7.21394454	0	0	0	0	0	133.50151197	194.88489393	1.61940276	2.6075526	8.69246085	0	0.16666667	4.76922073	0	7.08734414	0	0	0.51299278	0	0	0	0.47140452	0	0.33333333	0	0	3.72054191	3.54586735	3.30831307	3.19186704	3.11069755	2.7000516	2.56364861	2.3308765	1.90391183	1.86378067	1.62036683	1.51860093	1.42173236	1.33170805	1.24895843	1.05147106	3.63927538	3.51444789	3.23629745	3.09745947	2.95866351	2.71409991	2.45950043	2.41390317	1.91065494	1.82230619	1.63885905	1.53910024	1.40255183	1.30048636	1.11704799	1.10242315	3.79920552	3.65097432	3.43707728	3.32584293	3.20462935	2.96497755	2.78407363	2.70150516	1.72245363	1.64407481	1.38981981	1.26023197	1.11967999	0.97706715	0.75958674	0.70573395	3.63981556	3.52177443	3.23814003	3.09826081	2.96052127	2.73283028	2.45847393	2.44644777	1.90808009	1.80771581	1.62620322	1.52963597	1.39698735	1.26601489	1.08697334	1.06947049	88	246	95	206	4.72299651	10.59053663	114	2.05771747	2.22821295	144.65848516	2.22331302	5.21583711	30.77483418	846	4.45263158	285.3868	6.07205957	25.68969665	46.96722505	27.47443182	53.33333333	0.54658929	0.99930266	0.58456238	2.66666667	47	20	46	19	3	22	0	0	0	3	0	0	27	14	3	3	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	3.08314698	3.39516231	3.43352564	3.54049529	3.59663223	3.26086631	3.03623712	3.11291852	2.83382107	3.01883258	3.05398037	3.12946828	3.05380933	2.91686364	2.78084279	2.65763649	3.08226405	3.39374569	3.43204058	3.53934302	3.59523243	3.25976508	3.03553408	3.11178814	2.80287817	2.97534837	3.01055091	3.07851598	2.99599967	2.8808171	2.75351594	2.6134359	-0.10605856	0.08500053	0.06443194	-0.39548229	0.07689586	-0.05343606	-0.13243392	0.06540477	-0.12850462	0.10041309	0.11296363	-0.42579515	0.04370628	-0.0163165	-0.13502855	0.0502682	-0.10470627	0.08372889	0.06164533	-0.39344608	0.07870086	-0.0554819	-0.13217757	0.06620272	-0.13852363	0.10341687	0.13617661	-0.4369536	0.02669411	0.00240386	-0.13505667	0.04222431	0.77642766	0.76350712	0.76745057	1.34634717	1.22652406	0.94635234	0.91578895	1.15665286	0.83017745	0.78414431	0.76348922	1.37421545	1.20943245	0.91353779	0.91485921	1.11654619	0.77330948	0.76286925	0.76789657	1.34447517	1.22751087	0.94813138	0.91559154	1.15882724	0.85552519	0.8001952	0.76406325	1.38447403	1.20131885	0.89665496	0.9115815	1.09591162	19	4.48863637	5.25227343	6.09356977	6.89264164	7.73324565	8.5461693	9.38538552	10.20459165	11.04300183	20	38	0	138	0	578	0	2610	0	12348	19	25	26	29	29	22	18	18	4	0	3.13549422	3.4657359	3.49650756	3.66356165	3.8286414	3.36729583	3.13549422	3.33220451	1.79175947	0	49.21669475	108.63951515	46.98587053	37.66117261	1.88305863	20.25334868	1.247	1.555009	1.0002	1.00040004	76.4805	1.97715465	2	0	0	0	0	0	0	0	0	0	0	5	0	2	0	4	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	1	0	0	0	2	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	3	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0\r\n"
//				+ "N=C(O)C(N=C(O)C(N=C(O)C)CC(C)C)C(C)CC	-9999	15.86987901	9.18364697	8.52556774	5.22815334	3.85787731	2.81158647	1.29910382	0.57297855	0.52098294	0.09072184	0	1.92398387	0	2.9674333	0	0	0	0	0	0	0	0	-1.04344944	12.95968262	6.95901783	5.52456816	3.06983886	1.79758071	0.97319108	0.46781287	0.19539853	0.14322751	0.03469224	0	1.03205799	0	1.33362029	0	0	0	0	0	0	0	0	-0.3015623	20	9.8496	8.14792899	18.90909091	8.95752301	7.33111659	8.46893085	9.1580813	0	1.25074476	0	0	0	-1.22278048	0	0	-0.95216488	0	0	0	0	7.35492237	0	0	0	7.98654904	0	0	0	0	0	0	0	0	0	0	0	0	0	28.75798123	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	5	0	2	0	0	0	4	0	0	3	0	0	0	0	1	0	0	0	2	0	0	0	0	0	0	0	0	0	0	0	0	0	3	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	7.94795101	8.01006433	2.17422286	0	0	0	0	0	2.70156774	10.09445673	0.49800931	-0.77420257	2.70156774	0	6	4	0	44.09945263	10.12217387	0	4.86835166	1.21708791	0.81139194	2	4.22192809	11.95573525	84.4385619	86.4385619	0.97686218	0.02313782	4.21090952	181.17701927	0.45294255	199.42124551	5.24792751	1.51288762	4.16150087	209.49114571	0.58030788	282.19280949	5.64385619	581.26641203	3.05929691	6243.5730006	7.38010993	3.42192809	4.29409588	492.15724925	2.87811257	656	4732.34761677	7.21394454	0	0	0	0	0	133.50151197	198.73761185	1.61940276	2.6075526	8.69246085	0	0.16666667	4.76922073	0	7.08734414	0	0	0.51299278	0	0	0	0.47140452	0	0.33333333	0	0	3.74844715	3.54533107	3.30712655	3.19066176	3.09236255	2.69033997	2.52436595	2.3308765	1.90708092	1.8600168	1.62630145	1.51377699	1.40940912	1.31126155	1.24895843	0.98489645	3.66343382	3.51295737	3.25531163	3.1094226	2.93658546	2.71742921	2.44846767	2.41390317	1.91581804	1.81570336	1.64072055	1.5339934	1.38248494	1.29564863	1.11704799	1.05356228	3.81574064	3.64660673	3.44097367	3.32672552	3.18401838	2.96185812	2.74792388	2.70150516	1.73831226	1.64084924	1.40410143	1.26620333	1.0974797	0.97766893	0.75958674	0.67882564	3.66179978	3.51974245	3.25749571	3.10933403	2.93666833	2.73647008	2.45027033	2.44644777	1.91480297	1.80082264	1.62793899	1.52563779	1.37823225	1.26112194	1.06947049	1.0356378	88	238	95	212	4.72299651	10.59053663	114	2.05771747	2.22821295	131.06629819	2.23713246	4.09445673	30.19890527	846	4.45263158	285.3868	6.07205957	25.68969665	46.96722505	27.47443182	52.33333333	0.54658929	0.99930266	0.58456238	2.61666667	47	20	46	19	3	22	0	0	0	3	0	0	27	14	3	3	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	3.08314698	3.39516231	3.43352564	3.54049529	3.59663223	3.26086631	3.03623712	3.11291852	2.83382107	3.01883258	3.05398037	3.12946828	3.05380933	2.91686364	2.78084279	2.65763649	3.08226405	3.39374569	3.43204058	3.53934302	3.59523243	3.25976508	3.03553408	3.11178814	2.80287817	2.97534837	3.01055091	3.07851598	2.99599967	2.8808171	2.75351594	2.6134359	-0.10605856	0.08500053	0.06443194	-0.39548229	0.07689586	-0.05343606	-0.13243392	0.06540477	-0.12850462	0.10041309	0.11296363	-0.42579515	0.04370628	-0.0163165	-0.13502855	0.0502682	-0.10470627	0.08372889	0.06164533	-0.39344608	0.07870086	-0.0554819	-0.13217757	0.06620272	-0.13852363	0.10341687	0.13617661	-0.4369536	0.02669411	0.00240386	-0.13505667	0.04222431	0.77642766	0.76350712	0.76745057	1.34634717	1.22652406	0.94635234	0.91578895	1.15665286	0.83017745	0.78414431	0.76348922	1.37421545	1.20943245	0.91353779	0.91485921	1.11654619	0.77330948	0.76286925	0.76789657	1.34447517	1.22751087	0.94813138	0.91559154	1.15882724	0.85552519	0.8001952	0.76406325	1.38447403	1.20131885	0.89665496	0.9115815	1.09591162	19	4.48863637	5.25227343	6.09356977	6.89264164	7.73324565	8.5461693	9.38538552	10.20459165	11.04300183	20	38	0	138	0	578	0	2610	0	12348	19	25	26	29	29	22	18	18	4	0	3.13549422	3.52636052	3.66356165	3.98898405	4.18965474	4.04305127	4.07753744	4.20469262	2.56494936	0	53.39428586	108.63951515	46.98587053	37.66117261	1.88305863	20.25334868	2.741	7.513081	1.9386	3.75816996	78.0456	1.97715465	2	0	0	0	0	0	0	0	0	0	0	5	0	2	0	4	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	3	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	1	2	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0\r\n";

		predictionTSV+=qsarSmiles+"\t-9999.0\t"+  descriptors+"\r\n";
		
		System.out.println(predictionTSV);

		String predictResponse = modelWebService.callPredict(predictionTSV, modelId+"").getBody();
		ModelPrediction[] modelPredictionsArray = Utilities.gson.fromJson(predictResponse, ModelPrediction[].class);
	
		List<ModelPrediction>modelTestPredictions=Arrays.asList(modelPredictionsArray);			

		System.out.println(Utilities.gson.toJson(modelTestPredictions));
		 
				
		
	}
	
	
	public String getPredictionTsv(DescriptorSet descriptorSet,String dtxsid,  String smiles,SciDataExpertsDescriptorValuesCalculator calc) {

		String strDesc=calc.calculateDescriptors(smiles, descriptorSet);
		String line=dtxsid+"\t-9999\t"+strDesc;
		return line;
	}
	
	
	String standardize(String smiles, boolean useFullStandardize,String workflow) {

		HttpResponse<String> standardizeResponse = standardizer.callQsarReadyStandardizePost(smiles, false, workflow);

		//			System.out.println("status=" + standardizeResponse.getStatus());

		if (standardizeResponse.getStatus() == 200) {
			String jsonResponse = SciDataExpertsStandardizer.getResponseBody(standardizeResponse, useFullStandardize);
			String qsarSmiles = SciDataExpertsStandardizer.getQsarReadySmilesFromPostJson(jsonResponse,
					useFullStandardize);
			return qsarSmiles;
		}

		return null;
	}

	void compareTautomers() {
		
		String abbrev="LogP";		
		String datasetName=abbrev+" v1 modeling";
		
		String sql="select m.id from qsar_models.models m\r\n"
				+ "where dataset_name = '"+datasetName+"'\r\n"
				+ "and splitting_name='RND_REPRESENTATIVE' and descriptor_set_name='WebTEST-default' and fk_descriptor_embedding_id is not null\r\n"
				+ "order by dataset_name;";

//		System.out.println(sql);
		String modelId=SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql);
		
		System.out.println(modelId);
		
		String descriptorSetName="WebTEST-default";//can get from modelId TODO
		
//		String server="https://hcd.rtpnc.epa.gov/";
		String server = "https://hazard-dev.sciencedataexperts.com";
		
//		SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");
		
		DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();
		DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);
		
		
		String predictionTsv="ID\tExp\t"+descriptorSet.getHeadersTsv()+"\r\n";
		
		
		
		
		
	}
	
	void runFromDtxsids() {
		
		long modelId=1069L;//logKow
		String descriptorSetName="WebTEST-default";//can get from modelId TODO
		
//		String server="https://hcd.rtpnc.epa.gov/";
		String server = "https://hazard-dev.sciencedataexperts.com";
		SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");
		
		HashSet<String>dtxsids=new HashSet(Arrays.asList("DTXSID00192353","DTXSID6067331","DTXSID30891564","DTXSID6062599","DTXSID90868151","DTXSID8031863","DTXSID8031865","DTXSID1037303","DTXSID8047553","DTXSID60663110","DTXSID70191136","DTXSID3037709","DTXSID3059921","DTXSID3031860","DTXSID8037706","DTXSID8059920","DTXSID3031862","DTXSID30382063","DTXSID00379268","DTXSID20874028","DTXSID3037707"));

		List<DsstoxRecord>recs=getDsstoxRecords(dtxsids);
		
		DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();
		DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);

		String predictionTsv="ID\tExp\t"+descriptorSet.getHeadersTsv()+"\r\n";
		for(DsstoxRecord rec:recs) {
			String qsarSmiles=standardize(rec.getSmiles(), false, workflow);
			System.out.println(rec.getDtxsid()+"\t"+rec.getSmiles()+"\t"+qsarSmiles);
			predictionTsv+=getPredictionTsv(descriptorSet, rec.getDtxsid(),qsarSmiles,calc)+"\r\n";
		}
		
		List<ModelPrediction> modelPredictions1 = run(modelId, predictionTsv,false);

//		System.out.println(predictionTsv);		
		
//		System.out.println(Utilities.gson.toJson(modelPredictions1));
		
		ExcelSourceReader esr=new ExcelSourceReader();
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 scientists\\catherine sumner\\";
		String jsonPreds=Utilities.gson.toJson(modelPredictions1);
		JsonArray ja=Utilities.gson.fromJson(jsonPreds,JsonArray.class);
		esr.convertJsonArrayToExcel(ja, folder+"logKow predictions.xlsx");

		
	}
	
	public String convertToInClause(HashSet<String>items) {
		StringBuilder sb = new StringBuilder();

		int count = 0;
		for (String item : items) {
			sb.append("'").append(item).append("'");
			if (count < items.size() - 1) {
				sb.append(", ");
			}
			count++;
		}

		return sb.toString();
	}
	
	public List<DsstoxRecord> getDsstoxRecords(HashSet<String>dtxsids) {

		String sql="SELECT gs.dsstox_substance_id,gs.casrn,c.dsstox_compound_id,c.smiles,ql.name,gs.updated_at FROM generic_substances gs\r\n"
				+ "	         join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id\r\n"
				+"			join qc_levels ql on gs.fk_qc_level_id = ql.id\r\n"
				+ "	join compounds c on gsc.fk_compound_id = c.id\r\n"
				+ "	where gs.dsstox_substance_id in ("+convertToInClause(dtxsids)+");";

//		System.out.println(sql);

		List<DsstoxRecord>recs=new ArrayList<>();
		try {
			ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionDSSTOX(), sql);
			while (rs.next()) {
				DsstoxRecord dr=new DsstoxRecord();					
				dr.setDtxsid(rs.getString(1));
				dr.setCasrn(rs.getString(2));
				dr.setDtxcid(rs.getString(3));
				dr.setSmiles(rs.getString(4));
//				dr.qcLevel=rs.getString(5);
//				dr.setUpdatedAt(rs.getString(6));
				recs.add(dr);
			}
		}catch (Exception ex) {
			ex.printStackTrace();
		}
		return recs;

	}
	
	
	void makePlotWebPage() {
		
		String type="PCA";
//		String type="UMAP";
//		String type="t-SNE";
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 python\\modeling services\\pf_python_modelbuilding\\datasets_v1_modeling\\plots "+type;
		String train="T=All But PFAS";
		
		List<String> imageFilenames = new ArrayList<>();
		
		System.out.println(folder);
		
		for (File file: new File(folder).listFiles()) {
			
			if(!file.getName().contains("png"))continue;
			if(file.getName().contains(train)) {
				imageFilenames.add(file.getName());		
			}
		}
		
        // Generate HTML content
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html>\n<html>\n<head>\n<title>Image Grid</title>\n</head>\n<body>\n");
        htmlContent.append("<table>\n");

        int columns = 2; // Number of columns
        for (int i = 0; i < imageFilenames.size(); i++) {
            if (i % columns == 0) {
                htmlContent.append("<tr>\n"); // Start a new row
            }
            htmlContent.append("<td><img src=\"")
                       .append(imageFilenames.get(i))
                       .append("\" alt=\"Image\" style=\"width:400px;height:400px;\"></td>\n");
            if (i % columns == columns - 1) {
                htmlContent.append("</tr>\n"); // End the row
            }
        }

        htmlContent.append("</table>\n</body>\n</html>");

        try  {
        	
        	BufferedWriter writer = new BufferedWriter(new FileWriter(folder+File.separator+train+".html"));
            writer.write(htmlContent.toString());
            writer.flush();
            writer.close();
            System.out.println("HTML file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

	}
	
	
	void makePlots() {
		
		String [] abbrevs= {"LogP"};
//		String [] abbrevs= {"LogP","MP"};
//		String [] abbrevs= {"HLC", "VP","WS","LogP","BP","MP"};

//		String [] abbrevs= {"LogP"};
		
		String plotType="PCA";
//		String plotType="t-SNE";
//		String plotType="UMAP";
//		String plotType="UMAP3d";

		boolean haveEmbedding=true;
		boolean initModel=true;
		for (String abbrev:abbrevs) {
			makePlot(abbrev,haveEmbedding,plotType,"All","All",initModel);
			makePlot(abbrev,haveEmbedding,plotType,"All","PFAS",false);
			makePlot(abbrev,haveEmbedding,plotType,"PFAS","PFAS",initModel);
			makePlot(abbrev,haveEmbedding,plotType,"All But PFAS","PFAS",initModel);
			
			System.out.println("\n");
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PredictScript ps=new PredictScript();
		
//		ps.runPrediction("CCC(C)C(C(=O)N)NC(=O)C(CC(C)C)NC(=O)C", 1069L);
//		ps.runPrediction("CC(O)=NC(CC(C)C)C(O)=NC(C(C)CC)C(=N)O", 1069L);
		
		//Estimate water solubility values for 96 hr FHM toxicity dataset:
//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\dev_qsar\\output\\exp_prop_96HR_FHM_LC50_v1_modeling\\";
//		String tsvFileName="exp_prop_96HR_FHM_LC50_v1 modeling_WebTEST-default_full.tsv";
//		ps.predict(folder,tsvFileName, 1066L, false, false);
		
//		boolean convertLogMolar=true;
//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\ECOTOX_2023_12_14\\";
//		String filePathOut=folder+"96HR_FHM_LC50_WS.json";
//		ps.predict(filePathOut,1066L, "exp_prop_96HR_FHM_LC50_v1 modeling");
		
//		ps.runExternalSet();
//		ps.runFromDtxsids();
//		ps.compareTautomers();
		
//		ps.makeTsne_plot();
		
		ps.makePlots();
		ps.makePlotWebPage();

		
		
	}

}
