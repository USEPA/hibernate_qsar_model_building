package gov.epa.endpoints.models;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;
import com.google.gson.Gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.epa.databases.dev_qsar.DevQsarConstants;

import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;
import gov.epa.databases.dev_qsar.qsar_models.entity.Source;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingService;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInModelSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceService;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.SplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticServiceImpl;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.valery.SDE_Prediction_Response;
import gov.epa.run_from_java.scripts.PredictionDashboard.valery.SDE_Prediction_Response.Prediction;
import gov.epa.web_services.ModelWebService;
import gov.epa.web_services.embedding_service.CalculationInfo;
import gov.epa.web_services.embedding_service.CalculationInfoGA;
import kong.unirest.HttpResponse;

public class WebServiceModelBuilder extends ModelBuilder {
	
	private DescriptorEmbeddingService descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();
	private ModelBytesService modelBytesService = new ModelBytesServiceImpl();
	
	StatisticServiceImpl statisticService=new StatisticServiceImpl();
	ModelStatisticServiceImpl modelStatisticService=new ModelStatisticServiceImpl();
	SplittingServiceImpl splittingService=new SplittingServiceImpl();
	DataPointInSplittingService dataPointInSplittingService = new DataPointInSplittingServiceImpl();
	DataPointServiceImpl dataPointService=new DataPointServiceImpl();
	DatasetServiceImpl datasetService=new DatasetServiceImpl();
	
//	boolean compressModelBytes=true;
	
	
	public CrossValidate crossValidate=new CrossValidate();
	
	
	protected ModelWebService modelWebService;
	
	public WebServiceModelBuilder(ModelWebService modelWebService, String lanId) {
		super(lanId);
		this.modelWebService = modelWebService;
	}

//	public void listDescriptors(Long modelId) {
//		if (modelId==null) {
////			logger.error("Model with supplied parameters has not been built");
//			return;
//		}
//		
//		ModelBytes modelBytes = modelBytesService.findByModelId(modelId,compressModelBytes);
//		if (modelBytes==null) {
//			return;
//		}
//		
//		byte[] bytes = modelBytes.getBytes();
//		
//		Model model = modelBytes.getModel();
//		String fullMethodName = model.getMethod().getName();
//		String methodName = fullMethodName.substring(0, fullMethodName.indexOf("_"));
//		
//		String strModelId = String.valueOf(modelId);
//		String descriptors = modelWebService.callDescriptors(bytes, methodName, strModelId).getBody();
//		
//		String fileName = "data/descriptors/" + model.getDatasetName()
//			+ "_" + model.getDescriptorSetName()
//			+ "_" + model.getSplittingName()
//			+ "_" + methodName + "_" + modelId + "_descriptors.txt";
//		File file = new File(fileName);
//		file.getParentFile().mkdirs();
//		
//		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
//			bw.write(descriptors);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	
	public ModelPrediction[] getModelPredictionsFromAPI(Model model) {

		
		String details=new String(model.getDetails());
		JsonObject joDetails=Utilities.gson.fromJson(details, JsonObject.class); 
		boolean use_pmml=joDetails.get("use_pmml").getAsBoolean();
		boolean use_sklearn2pmml=false;//if use true you cant have standarization included in pmml

		long model_id=model.getId();
		
		
		
		//Get training and test set instances as strings using TEST descriptors:
		ModelData md=ModelData.initModelData(model.getDatasetName(), model.getDescriptorSetName(),model.getSplittingName(), false, false);
//		System.out.print(md.predictionSetInstances);
			
		ModelBytes modelBytes = modelBytesService.findByModelId(model_id,use_pmml);//python can figure out the string even if the string bytes are compressed- so can send smaller bytes...
//		System.out.println(modelBytes.getBytes().length);
//		System.out.println(bytes.length);

		System.out.println(use_pmml+"\t"+modelBytes.getBytes().length);

				
		String strModelId = String.valueOf(model_id);
		
		if (use_pmml) {
			HttpResponse<String>response=modelWebService.callInitPmml(modelBytes.getBytes(), model_id+"", details,use_sklearn2pmml);
		} else {
			HttpResponse<String>response=modelWebService.callInitPickle(modelBytes.getBytes(),model_id+"");
		}
		
		String predictResponse = modelWebService.callPredict(md.predictionSetInstances, strModelId).getBody();
		System.out.println("predictResponse="+predictResponse);
		
		
		Gson gson=new Gson();
		ModelPrediction[] modelPredictions = gson.fromJson(predictResponse, ModelPrediction[].class);
		return modelPredictions;
	}
	
	public ModelPrediction[] getModelPredictionsFromAPI(Model model,String predictionSetInstances) {

		
		String details=new String(model.getDetails());
		JsonObject joDetails=Utilities.gson.fromJson(details, JsonObject.class); 
		boolean use_pmml=joDetails.get("use_pmml").getAsBoolean();
		boolean use_sklearn2pmml=false;//if use true you cant have standarization included in pmml

		long model_id=model.getId();
		
			
		ModelBytes modelBytes = modelBytesService.findByModelId(model_id,use_pmml);//python can figure out the string even if the string bytes are compressed- so can send smaller bytes...
//		System.out.println(modelBytes.getBytes().length);
//		System.out.println(bytes.length);

		System.out.println(use_pmml+"\t"+modelBytes.getBytes().length);

				
		String strModelId = String.valueOf(model_id);
		
		if (use_pmml) {
			HttpResponse<String>response=modelWebService.callInitPmml(modelBytes.getBytes(), model_id+"", details,use_sklearn2pmml);
		} else {
			HttpResponse<String>response=modelWebService.callInitPickle(modelBytes.getBytes(),model_id+"");
		}
		
		String predictResponse = modelWebService.callPredict(predictionSetInstances, strModelId).getBody();
		System.out.println("predictResponse="+predictResponse);
		
		
		Gson gson=new Gson();
		ModelPrediction[] modelPredictions = gson.fromJson(predictResponse, ModelPrediction[].class);
		return modelPredictions;
	}

	

	public ModelPrediction[] getModelPredictionsFromSDE_API(Model model,String workflow,boolean use_cache) {
		
		long model_id=model.getId();
		
		//Get training and test set instances as strings using TEST descriptors:
		ModelData md=ModelData.initModelData(model.getDatasetName(), model.getDescriptorSetName(),model.getSplittingName(), false, false);
//		System.out.print(md.predictionSetInstances);
			
		//TODO add a hibernate version of following:
		String sql="select m.fk_model_set_id  from  qsar_models.models_in_model_sets m where fk_model_id="+model_id;
		String strModelSetId=SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql);		
		
		DatasetServiceImpl dss=new DatasetServiceImpl();
		String strDatasetId=dss.findByName(model.getDatasetName()).getId()+"";		
		
		String predictResponse = modelWebService.callPredictSDE(md.predictionSetInstances, strModelSetId,strDatasetId, workflow, use_cache).getBody();
//		System.out.println("predictResponse="+predictResponse);
		
		ModelPrediction[] modelPredictions = SDE_Prediction_Response.toModelPredictions(predictResponse,model.getMethod().getName());
		return modelPredictions;
	}


	Method getMethodFromDetails(String methodName,Model model,JsonObject joDetails) {
		
		String version = joDetails.get("version").getAsString();
		Boolean isBinary = joDetails.get("is_binary").getAsBoolean();		
		
		System.out.println("getMethodFromDetails(), isBinary="+isBinary);
		
		String classOrRegr = isBinary ? "classifier" : "regressor";				
		String fullMethodName = methodName + "_" + classOrRegr + "_" + version;		
				
		String description=joDetails.get("description").getAsString();
		String description_url=joDetails.get("description_url").getAsString();
		
		Gson gson2 = new Gson();		
		String hyperparameter_grid=gson2.toJson(joDetails.get("hyperparameter_grid").getAsJsonObject());
				
		Method method = methodService.findByName(fullMethodName);
		
		if (method==null) {
			method = new Method(fullMethodName, description, description_url,hyperparameter_grid, isBinary, lanId);
			methodService.create(method);
		} else {
//			JsonParser parser = new JsonParser();
//			if (!parser.parse(hyperparameters).equals(parser.parse(method.getHyperparameters()))) {
//				System.out.println("Hyperparameters for " + fullMethodName + " have changed");
//			}
		}
		return method;

	}
	
	


	
	/**
	 * Builds a Python and uses SQLalchemy to store
	 * @param data
	 * @param params
	 */
	@SuppressWarnings("deprecation")
	public Long trainWithPythonStorage(ModelData data, String methodName, String descriptorEmbeddingName) 
			throws ConstraintViolationException {
		if (data.trainingSetInstances==null) {
//			logger.error("Dataset instances were not initialized");
			System.out.println("Dataset instances were not initialized");
			return null;
		}
		
		System.out.println("Building Python model with dataset = " + data.datasetName + ", descriptors = " + data.descriptorSetName
				+ ", splitting = " + data.splittingName + " using QSAR method = " + methodName);
		DescriptorEmbedding descriptorEmbedding = null;
		if (descriptorEmbeddingName!= null) {
			descriptorEmbedding = descriptorEmbeddingService.findByName(descriptorEmbeddingName);
			if (descriptorEmbedding==null) {
			System.out.println("No such descriptor embedding");
			return null;
			} 
			else if (!descriptorEmbedding.getDescriptorSetName().equals(data.descriptorSetName)) {
			System.out.println("Descriptor embedding for wrong descriptor set");
			return null;
			}
			// CR: why impose this? if it's the right property I don't see a problem 
			/*
			else if (!descriptorEmbedding.getDatasetName().equals(data.datasetName)) {
			System.out.println("Descriptor embedding for wrong dataset");
			return null;
			} */
		}
		
		
		Method genericMethod = methodService.findByName(methodName);
		if (genericMethod==null) {
			genericMethod = new Method(methodName, methodName, null,null, false, lanId);
			methodService.create(genericMethod);
		}
		
		String modelName="model"+System.currentTimeMillis();//TODO maybe use dataset name and method name in modelName
		
		SourceService ss=new SourceServiceImpl();
		Source source=ss.findByName(DevQsarConstants.SOURCE_CHEMINFORMATICS_MODULES);
		
		Model model = new Model(modelName, genericMethod, descriptorEmbedding, data.descriptorSetName, data.datasetName, data.splittingName, source, lanId);
		
		
		modelService.create(model);
		String hyperparameters = null;
		String strModelId = String.valueOf(model.getId());
		System.out.println("the model id is:" + strModelId);
		if (descriptorEmbedding != null) {
			modelWebService.callTrainWithPreselectedDescriptorsPythonStorage(data.trainingSetInstances, 
				data.removeLogP_Descriptors, methodName, strModelId, descriptorEmbedding.getEmbeddingTsv()).getBody();
			hyperparameters = modelWebService.callDetails(strModelId).getBody();
		} else {
			modelWebService.callTrainPythonStorage(data.trainingSetInstances, 
				data.removeLogP_Descriptors, methodName, strModelId);
			hyperparameters = modelWebService.callDetails(strModelId).getBody();
		}
		String description = modelWebService.callInfo(methodName).getBody();
		//TODO get description_url
		String description_url=null;
		
		JsonObject jo = gson.fromJson(hyperparameters, JsonObject.class);
		String version = jo.get("version").getAsString();
		Boolean isBinary = jo.get("is_binary").getAsBoolean();
		String classOrRegr = isBinary ? "classifier" : "regressor";
		String fullMethodName = methodName + "_" + classOrRegr + "_" + version;
		
		Method method = methodService.findByName(fullMethodName);
		if (method==null) {
			method = new Method(fullMethodName, description, description_url,hyperparameters, isBinary, lanId);
			methodService.create(method);
		} else {
			JsonParser parser = new JsonParser();
			if (!parser.parse(hyperparameters).equals(parser.parse(method.getHyperparameter_grid()))) {
//				logger.warn("Hyperparameters for " + fullMethodName + " have changed");
			}
		}
		
		model.setMethod(method);
		modelService.update(model);
		
		return model.getId();
	}

	/**
	 * Builds a Python model with the given data and parameters
	 * @param data
	 * @param params
	 */
	@SuppressWarnings("deprecation")
	public Long trainWithPreselectedDescriptorsByEmbeddingName(ModelData data, String methodName, String descriptorEmbeddingName,boolean use_pmml,boolean include_standardization_in_pmml) 
			throws ConstraintViolationException {
		
		DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByName(descriptorEmbeddingName);
		
		if (descriptorEmbedding==null) {
			System.out.println("Embedding is null for "+descriptorEmbeddingName);
			return null;
		} else if (!descriptorEmbedding.getDescriptorSetName().equals(data.descriptorSetName)) {
			System.out.println("Descriptor embedding for wrong descriptor set");
			return null;
		} else if (!descriptorEmbedding.getDatasetName().equals(data.datasetName)) {
			System.out.println("Descriptor embedding for wrong dataset");
			return null;
		}
				
		return trainWithPreselectedDescriptors(data, methodName, descriptorEmbedding,use_pmml,include_standardization_in_pmml);
	}
	
	/**
	 * Builds a Python model with the given data and parameters
	 * @param data
	 * @param params
	 */
	@SuppressWarnings("deprecation")
	public Long train(ModelData data, String methodName,boolean use_pmml,boolean include_standardization_in_pmml) throws ConstraintViolationException {
		return trainWithPreselectedDescriptors(data, methodName, null,use_pmml,include_standardization_in_pmml);
	}

	
	/**
	 * Builds a Python model with the given data and parameters
	 * @param data
	 * @param include_standardization_in_pmml 
	 * @param params
	 */
	
	public Long trainWithPreselectedDescriptors(ModelData data, String methodName, DescriptorEmbedding descriptorEmbedding,boolean use_pmml, boolean include_standardization_in_pmml) 
			throws ConstraintViolationException {

		boolean compressModelBytes=false;

		if (use_pmml) {
			compressModelBytes=true;
		}
		
		
		if (data.trainingSetInstances==null) {
//			logger.error("Dataset instances were not initialized");
			System.out.println("Dataset instances were not initialized");

			return null;
		}
		
		System.out.println("Building Python model with dataset = " + data.datasetName + ", descriptors = " + data.descriptorSetName
				+ ", splitting = " + data.splittingName + " using QSAR method = " + methodName);
		
		if (descriptorEmbedding!=null) {
			System.out.println("Embedding "+descriptorEmbedding.getName()+":\t"+descriptorEmbedding.getEmbeddingTsv());	
		} else {
			System.out.println("No embedding used");
		}
		
		
		Method genericMethod = methodService.findByName(methodName);
		if (genericMethod==null) {
			genericMethod = new Method(methodName, methodName, null,null, false, lanId);
			methodService.create(genericMethod);
		}
		
		String modelName="model"+System.currentTimeMillis();//TODO maybe use dataset name and method name in modelName

		SourceService ss=new SourceServiceImpl();
		Source source=ss.findByName(DevQsarConstants.SOURCE_CHEMINFORMATICS_MODULES);
		
		Model model = new Model(modelName, genericMethod, descriptorEmbedding, data.descriptorSetName, data.datasetName, data.splittingName,source, lanId);
		modelService.create(model);
		
		String strModelId = String.valueOf(model.getId());
		
		System.out.println("strModelID="+strModelId);
		
		byte[] bytes=null;
		
		if (descriptorEmbedding != null) {
			bytes = modelWebService
					.callTrainWithPreselectedDescriptors(data.trainingSetInstances,data.predictionSetInstances, data.removeLogP_Descriptors,
							methodName, strModelId, descriptorEmbedding.getEmbeddingTsv(), use_pmml, include_standardization_in_pmml)
					.getBody();
		} else {
			bytes = modelWebService
					.callTrain(data.trainingSetInstances,data.predictionSetInstances, data.removeLogP_Descriptors, methodName, strModelId, use_pmml,include_standardization_in_pmml)
					.getBody();
		}
		
//		String pmml = new String(bytes);
//		System.out.println("pmml");
//		System.out.println(pmml);
		

//		String description = modelWebService.callInfo(methodName).getBody();//can get from details call instead

		String details = modelWebService.callDetails(model.getId()+"").getBody();			
		
		System.out.println("details="+details);
		
		JsonObject joDetails = gson.fromJson(details, JsonObject.class);
		
		Method method=getMethodFromDetails(methodName, model,joDetails);

		//Call cross validate here:
		
//		createCV_Statistics(joDetails, model);//old way

		Gson gson2=new Gson();//no pretty printing
		
		String hyperparameters=gson2.toJson(joDetails.get("hyperparameters").getAsJsonObject());
		model.setHyperparameters(hyperparameters);		
		
		model.setMethod(method);
		model.setDetails(details.getBytes());
		
		modelService.update(model);

		ModelBytes modelBytes = new ModelBytes(model, bytes, lanId);
//		modelBytesService.create(modelBytes);
		modelBytesService.createSQL(modelBytes,compressModelBytes);
		
		crossValidate.crossValidate(model,data.removeLogP_Descriptors,modelWebService.num_jobs, true,use_pmml);			
		
		return model.getId();
	}
	
	public class CrossValidate {
		
		int numSplits=5;
		
		
		public void crossValidate(Model model, boolean remove_log_p, int num_jobs, boolean postPredictions,boolean use_pmml) {
			
			System.out.println(model.getSplittingName());
						
			Dataset dataset=datasetService.findByName(model.getDatasetName());
			
			
			addCV_DPIS(model,dataset);
			
			DescriptorEmbedding descriptorEmbedding=model.getDescriptorEmbedding();
			
//			List<DataPoint> dataPoints =dataPointService.findByDatasetName(model.getDatasetName());//this is a lot slower
//
//			Map<String, Double> expMap = dataPoints.stream()
//					.collect(Collectors.toMap(dp -> dp.getCanonQsarSmiles(), dp -> dp.getQsarPropertyValue()));
			
			Hashtable<String,Double>expMap=SqlUtilities.getHashtableExp(dataset);
			

			System.out.println("Cross validation for dataset = " + model.getDatasetName() + ", descriptors = " + model.getDescriptorSetName()
			+ ", splitting = " + model.getSplittingName() + " using QSAR method = " + model.getMethod().getName());

			if (descriptorEmbedding != null) System.out.println("Embedding="+descriptorEmbedding.getEmbeddingTsv());

			calcCV_Folds(model, remove_log_p, num_jobs, expMap, postPredictions,use_pmml);
			
		}
		

		public void addCV_DPIS(Model model,Dataset dataset) {
//			System.out.println(model.getSplittingName());
			Splitting splittingCV1=splittingService.findByName(model.getSplittingName()+"_CV1");
			
			if (splittingCV1==null)  {
				createSplittings(model);
				splittingCV1=splittingService.findByName(model.getSplittingName()+"_CV1");
			}
			
			 createDataPointInSplittings(model, dataset,splittingCV1);
		}


		private void createDataPointInSplittings(Model model, Dataset dataset, Splitting splittingCV1) {

			//Check if have DPIS:
			String sql="select count(dpis.id) from qsar_datasets.data_points_in_splittings dpis\n"+ 
			"join qsar_datasets.data_points dp on dp.id=dpis.fk_data_point_id\n"+ 
			"where dp.fk_dataset_id="+dataset.getId()+" and dpis.fk_splitting_id="+splittingCV1.getId()+";"; 
			
//			System.out.println(sql);
			
			int countDPIS=Integer.parseInt(SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql));
			
			if (countDPIS!=0) return;
						
//			System.out.println(countDPIS);
			
			Splitting splittingModel=splittingService.findByName(model.getSplittingName());
			
			List<String>ids=ModelData.getTrainingIds(dataset, splittingModel, false);
			Collections.shuffle(ids);
			
//			for (String id:ids) {
//				System.out.println(id);
//			}
			
			
//			System.out.println(idCount);
			
			Hashtable<Integer,List<String>>htSplits=createSplitHashtable(ids);
			
			
			List<DataPoint> dataPoints = 
					dataPointService.findByDatasetName(model.getDatasetName());

			Map<String, DataPoint> dpMap = dataPoints.stream()
					.collect(Collectors.toMap(dp -> dp.getCanonQsarSmiles(), dp -> dp));

			
			for (int fold=1;fold<=numSplits;fold++) {
				
				List<String>idsTrain=new ArrayList<>();
				List<String>idsTest=new ArrayList<>();
				
				for (int i=1;i<=numSplits;i++) {
					List<String>idsFold=htSplits.get(i);
					
					if (i!=fold) {
						idsTrain.addAll(idsFold);						
					} else {
						idsTest.addAll(idsFold);
					}
				}
				
//				System.out.println(fold+"\t"+idsTrain.size()+"\t"+idsTest.size());
				
				Splitting splittingFold=splittingService.findByName(model.getSplittingName()+"_CV"+fold);
				List<DataPointInSplitting> dpisTrain = createDPIS(dpMap, idsTrain, splittingFold,DevQsarConstants.TRAIN_SPLIT_NUM);
				List<DataPointInSplitting> dpisTest = createDPIS(dpMap, idsTest, splittingFold,DevQsarConstants.TEST_SPLIT_NUM);
				
//				System.out.println(fold+"\t"+dpisTrain.size()+"\t"+dpisTest.size());
				
			}
			
		}


		private List<DataPointInSplitting> createDPIS(Map<String, DataPoint> dpMap, List<String> idsSet,
				Splitting splittingFold, int splitNum) {
			List<DataPointInSplitting>dpisTrain=new ArrayList<>();
			for(String id:idsSet) {
				DataPointInSplitting dpis=new DataPointInSplitting(dpMap.get(id), splittingFold, splitNum, lanId);
				dpisTrain.add(dpis);
			}				
			dataPointInSplittingService.createSQL(dpisTrain);
			return dpisTrain;
		}


		private void createSplittings(Model model) {
			
			
			for (int fold=1;fold<=5;fold++) {
				String name=model.getSplittingName()+"_CV"+fold;
				String description="Cross validation split "+fold+" for splitting = "+model.getSplittingName();
				Splitting splitting=new Splitting(name, description, 2, lanId);
				splittingService.create(splitting);
			}
			
		}


		private  Hashtable<Integer,List<String>> createSplitHashtable(List<String> ids) {
			Hashtable<Integer,List<String>>htSplits=new Hashtable<>();

			
			while (true) {
				for (int fold=1;fold<=numSplits;fold++) {

					if(htSplits.get(fold)==null) {
						List<String>ids_i=new ArrayList<>();
						htSplits.put(fold, ids_i);
						ids_i.add(ids.remove(0));
					} else {
						List<String>ids_i=htSplits.get(fold);
						ids_i.add(ids.remove(0));
					}
					
					if(ids.size()==0) {
						return htSplits;
					}

				}
			}
			
		}

		void calcCV_Folds( Model model, boolean remove_log_p, int num_jobs,Map<String, Double> expMap,boolean postPredictions,boolean use_pmml) {
			
//			double Q2_CV_AVG=0;
//			double R2_CV_AVG=0;

			int countChemicals=0;
			
			List<ModelPrediction> mpsTestSetPooled=new ArrayList<>();
			
			HashSet<Double>expVals=new HashSet<>();
			
			for (int fold=1;fold<=5;fold++) {
								
				String splittingNameCV=model.getSplittingName()+"_CV"+fold;
				
				System.out.println("running "+splittingNameCV);
				
				List<ModelPrediction>mpsTestSet=crossValidateWithPreselectedDescriptors(model, remove_log_p, splittingNameCV, num_jobs,use_pmml);
				List<ModelPrediction>mpsTrainSet=getTrainingSetModelPredictions(dataPointInSplittingService, model.getDatasetName(), expMap, splittingNameCV);
								
				mpsTestSetPooled.addAll(mpsTestSet);
				
				System.out.println("Fold "+fold+", P="+mpsTestSet.size()+"\tT="+mpsTrainSet.size());
				
//				double Q2_CV_i=ModelStatisticCalculator.calculateQ2_F3(mpsTrainSet, mpsTestSet);
//				Q2_CV_AVG+=Q2_CV_i;
//				
//				double YbarTrain=ModelStatisticCalculator.calcMeanExpTraining(mpsTrainSet);				
//				Map<String, Double>mapStats=ModelStatisticCalculator.calculateContinuousStatistics(mpsTestSet, YbarTrain, DevQsarConstants.TAG_TEST);				
//				double R2_CV_i=mapStats.get(DevQsarConstants.PEARSON_RSQ + DevQsarConstants.TAG_TEST);
//				R2_CV_AVG+=R2_CV_i;

				if (postPredictions) {
					postPredictions(mpsTestSet, model, splittingService.findByName(splittingNameCV));
				}
				
//				System.out.println(Utilities.gson.toJson(mpsTest));
				countChemicals+=mpsTestSet.size();
			}
			
//			System.out.println(Utilities.gson.toJson(mpsTestSetPooled));
			
			for(ModelPrediction mp:mpsTestSetPooled) {
				expVals.add(mp.exp);
			}
			
			if(expVals.size()>2) {
				System.out.println("\nPosting continuous CV statistics");
				postCV_Statistics_Continuous(model, postPredictions, mpsTestSetPooled);	
			} else if(expVals.size()==2) {
				System.out.println("\nPosting binary CV statistics");
				postCV_Statistics_Binary(model, postPredictions, mpsTestSetPooled);	
			} 
			
			
//			System.out.println(countChemicals);
			
//			R2_CV_AVG/=5.0;
//			Q2_CV_AVG/=5.0;
			
			

		}


		private void postCV_Statistics_Continuous(Model model, boolean postPredictions,
				List<ModelPrediction> mpsTestSetPooled) {
			Map<String, Double>mapStats=ModelStatisticCalculator.calculateContinuousStatistics(mpsTestSetPooled, 0.0, DevQsarConstants.TAG_TEST);
			double R2_CV_pooled=mapStats.get(DevQsarConstants.PEARSON_RSQ_TEST);
			double MAE_CV_pooled=mapStats.get(DevQsarConstants.MAE_TEST);
			double RMSE_CV_pooled=mapStats.get(DevQsarConstants.RMSE_TEST);
						
			if (postPredictions) {			

//				System.out.println("storing "+DevQsarConstants.PEARSON_RSQ_CV_TRAINING+"="+R2_CV_pooled);
				Statistic statistic=statisticService.findByName(DevQsarConstants.PEARSON_RSQ_CV_TRAINING);		
				ModelStatistic modelStatistic=new ModelStatistic(statistic, model, R2_CV_pooled, lanId);
				modelStatistic=modelStatisticService.create(modelStatistic);

//				System.out.println("storing "+DevQsarConstants.MAE_CV_TRAINING+"="+MAE_CV_pooled);
				statistic=statisticService.findByName(DevQsarConstants.MAE_CV_TRAINING);		
				modelStatistic=new ModelStatistic(statistic, model, MAE_CV_pooled, lanId);
				modelStatistic=modelStatisticService.create(modelStatistic);

//				System.out.println("storing "+DevQsarConstants.RMSE_CV_TRAINING+"="+RMSE_CV_pooled);
				statistic=statisticService.findByName(DevQsarConstants.RMSE_CV_TRAINING);		
				modelStatistic=new ModelStatistic(statistic, model, RMSE_CV_pooled, lanId);
				modelStatistic=modelStatisticService.create(modelStatistic);

				
//				String sql="Select ms.statistic_value from qsar_models.model_statistics ms where ms.fk_model_id="+model.getId()+" and ms.fk_statistic_id="+statistic.getId();
//				String statistic_value=SqlUtilitiesrunSQL(SqlUtilities.getConnectionPostgres(), sql);
																
//				if (statistic_value==null) {
//					modelStatistic=modelStatisticService.create(modelStatistic);
//				} else {
//					System.out.println("Already have "+statistic.getName()+" for "+model.getId());
//				}

//				System.out.println("storing Q2_CV_Training="+Q2_CV_AVG);
//				statistic=statisticService.findByName("Q2_CV_Training");
//				modelStatistic=new ModelStatistic(statistic, model, Q2_CV_AVG, lanId);
//				modelStatistic=modelStatisticService.create(modelStatistic);
				
//				if (modelStatisticService.findByModelId(model.getId(),statistic.getId())==null) {
//					modelStatistic=modelStatisticService.create(modelStatistic);
//				} else {
//					System.out.println("Already have "+statistic.getName()+" for "+model.getId());
//				}

			} else {
				System.out.println("Model = "+model.getId());
				System.out.println(DevQsarConstants.PEARSON_RSQ_CV_TRAINING+"="+R2_CV_pooled);
//				System.out.println("Q2_CV_Training="+Q2_CV_AVG);
				System.out.println("");
			}
		}
		
		private void postCV_Statistics_Binary(Model model, boolean postPredictions,
				List<ModelPrediction> mpsTestSetPooled) {
			
			Map<String, Double>mapStats=ModelStatisticCalculator.calculateBinaryStatistics(mpsTestSetPooled, 0.5, DevQsarConstants.TAG_TEST);
			
			double BA_CV_pooled=mapStats.get(DevQsarConstants.BA_TEST);
			double SN_CV_pooled=mapStats.get(DevQsarConstants.SN_TEST);
			double SP_CV_pooled=mapStats.get(DevQsarConstants.SP_TEST);
						
			if (postPredictions) {			

//				System.out.println("storing "+DevQsarConstants.PEARSON_RSQ_CV_TRAINING+"="+R2_CV_pooled);
				Statistic statistic=statisticService.findByName(DevQsarConstants.BA_CV_TRAINING);		
				ModelStatistic modelStatistic=new ModelStatistic(statistic, model, BA_CV_pooled, lanId);
				modelStatistic=modelStatisticService.create(modelStatistic);

//				System.out.println("storing "+DevQsarConstants.MAE_CV_TRAINING+"="+MAE_CV_pooled);
				statistic=statisticService.findByName(DevQsarConstants.SN_CV_TRAINING);		
				modelStatistic=new ModelStatistic(statistic, model, SN_CV_pooled, lanId);
				modelStatistic=modelStatisticService.create(modelStatistic);

//				System.out.println("storing "+DevQsarConstants.RMSE_CV_TRAINING+"="+RMSE_CV_pooled);
				statistic=statisticService.findByName(DevQsarConstants.SP_CV_TRAINING);		
				modelStatistic=new ModelStatistic(statistic, model, SP_CV_pooled, lanId);
				modelStatistic=modelStatisticService.create(modelStatistic);

			} else {
				System.out.println("Model = "+model.getId());
				System.out.println(DevQsarConstants.BA_CV_TRAINING+"="+BA_CV_pooled);
//				System.out.println("Q2_CV_Training="+Q2_CV_AVG);
				System.out.println("");
			}
		}
		
		public List<ModelPrediction> crossValidateWithPreselectedDescriptors(Model model, boolean removeLogP,String splittingNameCV, int num_jobs,boolean use_pmml)  {
			
			String qsarMethod=model.getMethod().getName();		
			qsarMethod=qsarMethod.substring(0,qsarMethod.indexOf("_"));
			
//			System.out.println(qsarMethod);
			
			ModelData data = ModelData.initModelData(model.getDatasetName(), model.getDescriptorSetName(), splittingNameCV, removeLogP,false);
			
			if (data.trainingSetInstances==null) {
//				logger.error("Dataset instances were not initialized");
				System.out.println("Dataset instances were not initialized");
				return null;
			}
						
			String predictResponse=null;

			if (model.getDescriptorEmbedding()==null) {
				predictResponse=modelWebService.crossValidate(qsarMethod,data.trainingSetInstances, data.predictionSetInstances, 
						removeLogP, num_jobs, model.getHyperparameters(),use_pmml).getBody();
			} else {
				predictResponse=modelWebService.crossValidate(qsarMethod,data.trainingSetInstances, data.predictionSetInstances, 
						removeLogP, num_jobs, model.getDescriptorEmbedding().getEmbeddingTsv(), model.getHyperparameters(),use_pmml).getBody();			
			}
						
			ModelPrediction[] modelPredictions = gson.fromJson(predictResponse, ModelPrediction[].class);
			return Arrays.asList(modelPredictions);
			
		}
		
		private List<ModelPrediction>  getTrainingSetModelPredictions(DataPointInSplittingService dataPointInSplittingService,
				String datasetName, Map<String, Double> expMap, String splittingNameCV) {
			List<ModelPrediction> mpsTrainSet=new ArrayList<>();

			List<DataPointInSplitting> dataPointsInSplittingCV = 
					dataPointInSplittingService.findByDatasetNameAndSplittingName(datasetName, splittingNameCV);


			for (DataPointInSplitting dpis:dataPointsInSplittingCV) {

				if(dpis.getSplitNum()!=DevQsarConstants.TRAIN_SPLIT_NUM) continue;//can have splitNum=2 for the PFAS ones...

				String id=dpis.getDataPoint().getCanonQsarSmiles();
				double exp=expMap.get(dpis.getDataPoint().getCanonQsarSmiles());
				double pred=Double.NaN;//we dont have preds for training in CV 

				mpsTrainSet.add(new ModelPrediction(id, exp, pred, DevQsarConstants.TRAIN_SPLIT_NUM));
			}
			return mpsTrainSet;
		}
		
	
//		@Deprecated
//		void createCV_Statistics(JsonObject jo,Model model) {
//
//			JsonObject joTrainingStats = jo.get("training_stats").getAsJsonObject();		
//
//			double R2_CV_Training=joTrainingStats.get("training_cv_r2").getAsDouble();
//			System.out.println("storing R2_CV_Training="+R2_CV_Training);
//			Statistic statistic=statisticService.findByName("R2_CV_Training");					
//			ModelStatistic modelStatistic=new ModelStatistic(statistic, model, R2_CV_Training, lanId);
//			modelStatistic=modelStatisticService.create(modelStatistic);
//
//			double Q2_CV_Training=joTrainingStats.get("training_cv_q2").getAsDouble();
//			System.out.println("storing Q2_CV_Training="+Q2_CV_Training);
//			statistic=statisticService.findByName("Q2_CV_Training");					
//			modelStatistic=new ModelStatistic(statistic, model, Q2_CV_Training, lanId);
//			modelStatistic=modelStatisticService.create(modelStatistic);
//			
//			JsonArray jaPreds=joTrainingStats.get("training_cv_predictions").getAsJsonArray();
//			
//			ModelPrediction[] mps = gson.fromJson(jaPreds, ModelPrediction[].class);
//					
////			for (CrossValidationPrediction cvp:cvps) {
////				System.out.println(cvp.id+"\t"+cvp.exp+"\t"+cvp.pred+"\t"+cvp.split);
////			}
//
//			//TODO need to loop over 5 cross validation splittings
//			
//					
//			List<DataPointInSplitting> dataPointsInSplittingCV1 = 
//					dataPointInSplittingService.findByDatasetNameAndSplittingName(model.getDatasetName(), "CV1");
//			
//			postDPIS_and_CV_predictions(model, mps,dataPointsInSplittingCV1.size()==0);
//			
//			
////			Splitting splitting=splittingService.findByName(DevQsarConstants.SPLITTING_TRAINING_CROSS_VALIDATION);
////			postPredictions(Arrays.asList(mps), model,splitting);	//use split+10 because split 1 is already taken by test set predictions
//
//		}
//		
//		/**
//		 * Creates DataPointsInSplitting and posts CV prediction
//		 * 
//		 * @param model
//		 * @param mps
//		 * @param createDPIS
//		 */
//		@Deprecated
//		private void postDPIS_and_CV_predictions(Model model, ModelPrediction[] mps,boolean createDPIS) {
//			
//			Hashtable<Integer,Splitting>htSplittings=new Hashtable<>();
//			for (int i=1;i<=5;i++) {
//				Splitting splitting=splittingService.findByName("CV"+i);
//				htSplittings.put(i,splitting);
//			}
//					
//			Hashtable<Integer,List<ModelPrediction>>htSets=new Hashtable<>();
//			for (ModelPrediction mp:mps) {
//				if(htSets.get(mp.split)==null) {
//					List<ModelPrediction>mpsTestSet=new ArrayList<>();
//					mpsTestSet.add(mp);
//					htSets.put(mp.split, mpsTestSet);
//					
//				} else {
//					List<ModelPrediction>mpsTestSet=htSets.get(mp.split);
//					mpsTestSet.add(mp);					
//				}
//			}
//			
//			for (Integer key:htSets.keySet()) {			
//				Splitting splittingCV=htSplittings.get(key);
//				List<ModelPrediction>mpsTestSet=htSets.get(key);//predicts in test sets for CV
//
//				if (createDPIS) {				
//					System.out.print("Creating CV DPIS Split "+key+" ");
//					createCV_DPIS(model, splittingCV, mpsTestSet);
//					System.out.print("done\n");
//				}			
//				
//				System.out.print("Posting CV predictions Split "+key+" ");
//				postPredictions(mpsTestSet, model, splittingCV);
//				System.out.print("done\n");
//				
//			}
//		}
//		
//		
//		@Deprecated
//		private void createCV_DPIS(Model model, Splitting splittingCV, List<ModelPrediction> mpsTestSet) {
//			
//			
//			List<DataPointInSplitting> dataPointsInSplittingModel = 
//					dataPointInSplittingService.findByDatasetNameAndSplittingName(model.getDatasetName(), model.getSplittingName());
//
//			
//			List<DataPoint> dataPoints = 
//					dataPointService.findByDatasetName(model.getDatasetName());
//
//			Map<String, DataPoint> dataPointMap = dataPoints.stream()
//					.collect(Collectors.toMap(dp -> dp.getCanonQsarSmiles(), dp -> dp));
//
//			
//			
//			List<String>idsTest=new ArrayList<>();
//			for (ModelPrediction mp:mpsTestSet) idsTest.add(mp.id);					
//
//			
//			List<DataPointInSplitting>dpisTrainingSet=new ArrayList<>();//training set for CV- need for stats
//			List<DataPointInSplitting>dpisTestSet=new ArrayList<>();//training set for CV- need for stats
//
//			for(ModelPrediction mp:mpsTestSet) {
//				DataPointInSplitting dpisNew=new DataPointInSplitting(dataPointMap.get(mp.id), splittingCV, DevQsarConstants.TEST_SPLIT_NUM, lanId);
//				dpisTestSet.add(dpisNew);										
//			}
//			
//			for (DataPointInSplitting dpis:dataPointsInSplittingModel) {
//
//				if(dpis.getSplitNum()!=DevQsarConstants.TRAIN_SPLIT_NUM) continue;//can have splitNum=2 for the PFAS ones...
//				
//				if(!idsTest.contains(dpis.getDataPoint().getCanonQsarSmiles())) {
//					//in the model splitting training set and not in CV test set:
//					DataPointInSplitting dpisNew=new DataPointInSplitting(dpis.getDataPoint(), splittingCV, DevQsarConstants.TRAIN_SPLIT_NUM, lanId);
//					dpisTrainingSet.add(dpisNew);										
//				}
//			}
//			
//			for (DataPointInSplitting dpis:dpisTrainingSet) {
//				dataPointInSplittingService.create(dpis);//TODO do this in batch mode
//			}
//			
//			for (DataPointInSplitting dpis:dpisTestSet) {
//				dataPointInSplittingService.create(dpis);//TODO do this in batch mode
//			}
//		}

	}//end CrossValidate class
	
	

	/**
	 * Validates a Python model with the given data and parameters
	 * @param data
	 * @param params
	 */
	public void predictPythonStorage(ModelData data, String methodName, Long modelId,boolean use_pmml) throws ConstraintViolationException {
		if (modelId==null) {
//			logger.error("Model with supplied parameters has not been built");
			System.out.println("Model with supplied parameters has not been built");
			return;
		} else if (data.predictionSetInstances==null) {
//			logger.error("Dataset instances were not initialized");
			System.out.println("Dataset instances were not initialized");
			return;
		}
		
//		logger.debug("Validating Python model with dataset = " + data.datasetName + ", descriptors = " + data.descriptorSetName
//				+ ", splitting = " + data.splittingName + " using QSAR method = " + methodName 
//				+ " (qsar_models ID = " + modelId + ")");
		
		ModelBytes modelBytes = modelBytesService.findByModelId(modelId,use_pmml);
		Model model = modelBytes.getModel();
		
		String strModelId = String.valueOf(modelId);

		Splitting splitting=splittingService.findByName(model.getSplittingName());

		String predictResponse = modelWebService.callPredictSQLAlchemy(data.predictionSetInstances, methodName, strModelId).getBody();		
		ModelPrediction[] modelPredictions = gson.fromJson(predictResponse, ModelPrediction[].class);				
		for(ModelPrediction mp:modelPredictions)mp.split=DevQsarConstants.TEST_SPLIT_NUM;
		postPredictions(Arrays.asList(modelPredictions), model,splitting);
		
		if (data.trainingSetInstances==null) {
			System.out.println("Dataset instances were not initialized");
		}

		String predictTrainingResponse = modelWebService.callPredictSQLAlchemy(data.trainingSetInstances, methodName, strModelId).getBody();
		ModelPrediction[] modelTrainingPredictions = gson.fromJson(predictTrainingResponse, ModelPrediction[].class);	
		for(ModelPrediction mp:modelTrainingPredictions)mp.split=DevQsarConstants.TRAIN_SPLIT_NUM;
		postPredictions(Arrays.asList(modelTrainingPredictions), model,splitting);
		
		calculateAndPostModelStatistics(Arrays.asList(modelTrainingPredictions), Arrays.asList(modelPredictions), model,true);

	}

	/**
	 * Validates a Python model with the given data and parameters
	 * @param data
	 * @param include_standardization_in_pmml 
	 * @param params
	 */
	public void predict(ModelData data, String methodName, Long modelId,boolean use_pmml, boolean use_sklearn2pmml) throws ConstraintViolationException {
		
		System.out.println("Enter predict");
		
		if (modelId==null) {
//			logger.error("Model with supplied parameters has not been built");
			return;
		} else if (data.predictionSetInstances==null) {
//			logger.error("Dataset instances were not initialized");
			System.out.println("No prediction instances exiting predict method");
			return;
		} else {
			String [] lines=data.predictionSetInstances.split("\n");
			if(lines.length==1) {
				System.out.println("No prediction instances exiting predict method");
				return;
			}
		}
		
//		logger.debug("Validating Python model with dataset = " + data.datasetName + ", descriptors = " + data.descriptorSetName
//				+ ", splitting = " + data.splittingName + " using QSAR method = " + methodName 
//				+ " (qsar_models ID = " + modelId + ")");
		
//		ModelBytes modelBytes = modelBytesService.findByModelId(modelId,compressModelBytes);
//		Model model = modelBytes.getModel();
		
		Model model=modelService.findById(modelId);
		
//		byte[] bytes = modelBytes.getBytes();
		byte[] bytes = modelBytesService.getBytesSql(modelId, use_pmml);
		
//		System.out.print(new String (bytes));
		
		String strModelId = String.valueOf(modelId);
		
		//Following may not be necessary if webservice hasnt been restarted:
//		modelWebService.callInit(bytes, methodName, strModelId).getBody();
		
		if (use_pmml) {
			String details=new String(model.getDetails());
//			System.out.println(details);
			HttpResponse<String>response=modelWebService.callInitPmml(bytes, strModelId, details,use_sklearn2pmml);
		} else {
			HttpResponse<String>response=modelWebService.callInitPickle(bytes,strModelId);
		}
		
		
		Splitting splitting=splittingService.findByName(model.getSplittingName());
		
//		System.out.println("Splitting id = "+splitting.getId());
				
		String predictResponse = modelWebService.callPredict(data.predictionSetInstances, strModelId).getBody();
		ModelPrediction[] modelPredictionsArray = gson.fromJson(predictResponse, ModelPrediction[].class);
		List<ModelPrediction>modelTestPredictions=Arrays.asList(modelPredictionsArray);			
		for(ModelPrediction mp:modelTestPredictions) mp.split=DevQsarConstants.TEST_SPLIT_NUM;		
		postPredictions(modelTestPredictions, model,splitting);		
		
		List<ModelPrediction> modelTrainingPredictions = predictTraining(model, data, methodName, strModelId);
		for(ModelPrediction mp:modelTrainingPredictions) mp.split=DevQsarConstants.TRAIN_SPLIT_NUM;
		postPredictions(modelTrainingPredictions, model,splitting);
		
		calculateAndPostModelStatistics(modelTrainingPredictions, modelTestPredictions, model,true);
	}
	
	/**
	 * Validates a Python model with the given data and parameters
	 * @param data
	 * @param include_standardization_in_pmml
	 * @param params
	 */
	public void predictTraining(ModelData data, String methodName, Long modelId,boolean use_pmml, boolean use_sklearn2pmml) throws ConstraintViolationException {
		
		System.out.println("Enter predict");
		
		if (modelId==null) {
//			logger.error("Model with supplied parameters has not been built");
			return;
		} else if (data.predictionSetInstances==null) {
//			logger.error("Dataset instances were not initialized");
			return;
		}
		
//		logger.debug("Validating Python model with dataset = " + data.datasetName + ", descriptors = " + data.descriptorSetName
//				+ ", splitting = " + data.splittingName + " using QSAR method = " + methodName 
//				+ " (qsar_models ID = " + modelId + ")");
		
		ModelBytes modelBytes = modelBytesService.findByModelId(modelId,use_pmml);
		Model model = modelBytes.getModel();
		byte[] bytes = modelBytes.getBytes();
		
		String strModelId = String.valueOf(modelId);
				
		if (use_pmml) {
			String details=new String(model.getDetails());
			HttpResponse<String>response=modelWebService.callInitPmml(modelBytes.getBytes(),strModelId, details,use_sklearn2pmml);
		} else {
			HttpResponse<String>response=modelWebService.callInitPickle(modelBytes.getBytes(),strModelId);
		}

		
		
		Splitting splitting=splittingService.findByName(model.getSplittingName());
		
//		System.out.println("Splitting id = "+splitting.getId());
				
		String predictResponse = modelWebService.callPredict(data.predictionSetInstances, strModelId).getBody();
//		ModelPrediction[] modelPredictionsArray = gson.fromJson(predictResponse, ModelPrediction[].class);
//		List<ModelPrediction>modelTestPredictions=Arrays.asList(modelPredictionsArray);			
//		for(ModelPrediction mp:modelTestPredictions) mp.split=DevQsarConstants.TEST_SPLIT_NUM;		
//		postPredictions(modelTestPredictions, model,splitting);		
		
		List<ModelPrediction> modelTrainingPredictions = predictTraining(model, data, methodName, strModelId);
		for(ModelPrediction mp:modelTrainingPredictions) mp.split=DevQsarConstants.TRAIN_SPLIT_NUM;
		postPredictions(modelTrainingPredictions, model,splitting);
		
//		calculateAndPostModelStatistics(modelTrainingPredictions, modelTestPredictions, model);
	}

	private List<ModelPrediction> predictTraining(Model model, ModelData data, String methodName, String strModelId) {
		if (data.trainingSetInstances==null) {
//			logger.error("Dataset instances were not initialized");
			return null;
		}
		
		String predictTrainingResponse = modelWebService.callPredict(data.trainingSetInstances,strModelId).getBody();
		ModelPrediction[] modelTrainingPredictions = gson.fromJson(predictTrainingResponse, ModelPrediction[].class);
		return Arrays.asList(modelTrainingPredictions);
	}

	public Long build(String datasetName, String descriptorSetName, String splittingName, boolean removeLogP_Descriptors,
			String methodName,boolean use_pmml, boolean include_standardization_in_pmml,boolean use_sklearn2pmml) throws ConstraintViolationException {
		ModelData data = ModelData.initModelData(datasetName, descriptorSetName, splittingName, removeLogP_Descriptors,false);
		
		Long modelId = train(data, methodName,use_pmml,include_standardization_in_pmml);
		predict(data, methodName, modelId,use_pmml, use_sklearn2pmml);
		
		return modelId;
	}
	
	public Long buildWithPythonStorage(String datasetName, String descriptorSetName, String splittingName, 
			boolean removeLogDescriptors, String methodName, String descriptorEmbeddingName,boolean use_pmml) throws ConstraintViolationException {
		ModelData data = ModelData.initModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors,false);
		
		Long modelId = trainWithPythonStorage(data, methodName, descriptorEmbeddingName);
		predictPythonStorage(data, methodName, modelId,use_pmml);
		
		return modelId;
	}


	public Long buildWithPreselectedDescriptors(String datasetName, String descriptorSetName, String splittingName, 
			boolean removeLogDescriptors, String methodName, String descriptorEmbeddingName,boolean use_pmml, boolean include_standardization_in_pmml,boolean use_sklearn2pmml) throws ConstraintViolationException {
		ModelData data =ModelData.initModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors,false);
		
		Long modelId = trainWithPreselectedDescriptorsByEmbeddingName(data, methodName, descriptorEmbeddingName,use_pmml,include_standardization_in_pmml);
		predict(data, methodName, modelId,use_pmml, use_sklearn2pmml);
		
		return modelId;
	}
	
	
	public Long buildWithPreselectedDescriptors(String methodName,CalculationInfo ci, DescriptorEmbedding descriptorEmbedding,boolean use_pmml, boolean include_standardization_in_pmml,boolean use_sklearn2pmml) throws ConstraintViolationException {
		ModelData data = ModelData.initModelData(ci.datasetName, ci.descriptorSetName, ci.splittingName, ci.remove_log_p,false);
		
		Long modelId = trainWithPreselectedDescriptors(data, methodName, descriptorEmbedding,use_pmml,include_standardization_in_pmml);
		predict(data, methodName, modelId,use_pmml, use_sklearn2pmml);
		
		return modelId;
	}
	
	
	public long build(String methodName, CalculationInfo ci,boolean use_pmml, boolean include_standardization_in_pmml,boolean use_sklearn2pmml){
		
		ModelData data = ModelData.initModelData(ci,false);
		
//		System.out.println(data.trainingSetInstances);
//		System.out.println(data.predictionSetInstances);
		
		Long modelId = train(data, methodName,use_pmml,include_standardization_in_pmml);
		
		predict(data, methodName, modelId,use_pmml, use_sklearn2pmml);
		
		return modelId;
	}
	
	void getModelEquation(long modelId) {
		byte[] bytes = modelBytesService.getBytesSql(modelId, false);
		String strModelId = String.valueOf(modelId);
		
		HttpResponse<String>response=modelWebService.callInitPickle(bytes,strModelId);

		
	}
	
	
	public static void main(String[] args) {



		ModelWebService modelWs = new ModelWebService(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);
		WebServiceModelBuilder mb = new WebServiceModelBuilder(modelWs, "tmarti02");
		
//		Model model=mb.modelService.findById(1284L);
//		mb.crossValidate.addCV_DPIS(model);
		
		mb.getModelEquation(1567L);

		

		
		
	}

}
