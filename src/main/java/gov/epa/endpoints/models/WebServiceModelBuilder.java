package gov.epa.endpoints.models;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingService;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.SplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;
import gov.epa.web_services.ModelWebService;
import gov.epa.web_services.embedding_service.CalculationInfo;

public class WebServiceModelBuilder extends ModelBuilder {
	
	private DescriptorEmbeddingService descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();
	private ModelBytesService modelBytesService = new ModelBytesServiceImpl();
	
	StatisticServiceImpl statisticService=new StatisticServiceImpl();
	ModelStatisticServiceImpl modelStatisticService=new ModelStatisticServiceImpl();
	SplittingServiceImpl splittingService=new SplittingServiceImpl();
	DataPointInSplittingService dataPointInSplittingService = new DataPointInSplittingServiceImpl();
	DataPointServiceImpl dataPointService=new DataPointServiceImpl();
	
	CrossValidate crossValidate=new CrossValidate();
	
	
	int num_threads=8;
	 
	
	protected ModelWebService modelWebService;
	
	public WebServiceModelBuilder(ModelWebService modelWebService, String lanId) {
		super(lanId);
		this.modelWebService = modelWebService;
	}

	public void listDescriptors(Long modelId) {
		if (modelId==null) {
//			logger.error("Model with supplied parameters has not been built");
			return;
		}
		
		ModelBytes modelBytes = modelBytesService.findByModelId(modelId);
		if (modelBytes==null) {
			return;
		}
		
		byte[] bytes = modelBytes.getBytes();
		
		Model model = modelBytes.getModel();
		String fullMethodName = model.getMethod().getName();
		String methodName = fullMethodName.substring(0, fullMethodName.indexOf("_"));
		
		String strModelId = String.valueOf(modelId);
		String descriptors = modelWebService.callDescriptors(bytes, methodName, strModelId).getBody();
		
		String fileName = "data/descriptors/" + model.getDatasetName()
			+ "_" + model.getDescriptorSetName()
			+ "_" + model.getSplittingName()
			+ "_" + methodName + "_" + modelId + "_descriptors.txt";
		File file = new File(fileName);
		file.getParentFile().mkdirs();
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			bw.write(descriptors);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Builds a Python model with the given data and parameters
	 * @param data
	 * @param params
	 */
	@SuppressWarnings("deprecation")
	public Long train(ModelData data, String methodName) throws ConstraintViolationException {
		return trainWithPreselectedDescriptors(data, methodName, null);
	}

	Method getDetails(String methodName,Model model,JsonObject joDetails) {
		
		String version = joDetails.get("version").getAsString();
		Boolean isBinary = joDetails.get("is_binary").getAsBoolean();				
		String classOrRegr = isBinary ? "classifier" : "regressor";				
		String fullMethodName = methodName + "_" + classOrRegr + "_" + version;		
				
		String description=joDetails.get("description").getAsString();
		String description_url=joDetails.get("description_url").getAsString();
		
		Gson gson2 = new Gson();		
		String hyperparameters=gson2.toJson(joDetails.get("hyperparameter_grid").getAsJsonObject());
				
		Method method = methodService.findByName(fullMethodName);
		if (method==null) {
			method = new Method(fullMethodName, description, description_url,hyperparameters, isBinary, lanId);
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
		
		Model model = new Model(genericMethod, descriptorEmbedding, data.descriptorSetName, data.datasetName, data.splittingName, lanId);
		modelService.create(model);
		String hyperparameters = null;
		String strModelId = String.valueOf(model.getId());
		System.out.println("the model id is:" + strModelId);
		if (descriptorEmbedding != null) {
			modelWebService.callTrainWithPreselectedDescriptorsPythonStorage(data.trainingSetInstances, 
				data.removeLogP_Descriptors, methodName, strModelId, descriptorEmbedding.getEmbeddingTsv()).getBody();
			hyperparameters = modelWebService.callDetails(methodName, strModelId).getBody();
		} else {
			modelWebService.callTrainPythonStorage(data.trainingSetInstances, 
				data.removeLogP_Descriptors, methodName, strModelId);
			hyperparameters = modelWebService.callDetails(methodName, strModelId).getBody();
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
			if (!parser.parse(hyperparameters).equals(parser.parse(method.getHyperparameters()))) {
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
	public Long trainWithPreselectedDescriptorsByEmbeddingName(ModelData data, String methodName, String descriptorEmbeddingName) 
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
				
		return trainWithPreselectedDescriptors(data, methodName, descriptorEmbedding);
	}
	
	
	/**
	 * Builds a Python model with the given data and parameters
	 * @param data
	 * @param params
	 */
	
	public Long trainWithPreselectedDescriptors(ModelData data, String methodName, DescriptorEmbedding descriptorEmbedding) 
			throws ConstraintViolationException {

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
		
		Model model = new Model(genericMethod, descriptorEmbedding, data.descriptorSetName, data.datasetName, data.splittingName, lanId);
		modelService.create(model);
		
		String strModelId = String.valueOf(model.getId());
		
		System.out.println("strModelID="+strModelId);
		
		byte[] bytes=null;
		
		if (descriptorEmbedding!=null) {		
			bytes = modelWebService.callTrainWithPreselectedDescriptors(data.trainingSetInstances, data.removeLogP_Descriptors, methodName, strModelId, descriptorEmbedding.getEmbeddingTsv()).getBody();
		} else {
			bytes = modelWebService.callTrain(data.trainingSetInstances,data.removeLogP_Descriptors, methodName, strModelId).getBody();
		}

//		String description = modelWebService.callInfo(methodName).getBody();//can get from details call instead

		String details = modelWebService.callDetails(methodName, model.getId()+"").getBody();			
		
		System.out.println(details);
		
		JsonObject joDetails = gson.fromJson(details, JsonObject.class);
		
		Method method=getDetails(methodName, model,joDetails);

		//Call cross validate here:
		
//		createCV_Statistics(joDetails, model);//old way

		Gson gson2=new Gson();//no pretty printing
		String hyperparameters=gson2.toJson(joDetails.get("hyperparameters").getAsJsonObject());
		
		model.setMethod(method);
		model.setHyperparameters(hyperparameters);		
		modelService.update(model);

		ModelBytes modelBytes = new ModelBytes(model, bytes, lanId);
		modelBytesService.create(modelBytes);
		
		
		crossValidate.crossValidate(model,data.removeLogP_Descriptors,num_threads, true);			

		
		return model.getId();
	}
	
	class CrossValidate {
		
		int numSplits=5;
		
		
		public void crossValidate(Model model, boolean remove_log_p, int num_jobs, boolean postPredictions) {
			
			System.out.println(model.getSplittingName());
			
			addCV_DPIS(model);
			
			DescriptorEmbedding descriptorEmbedding=model.getDescriptorEmbedding();		
			List<DataPoint> dataPoints =dataPointService.findByDatasetName(model.getDatasetName());

			Map<String, Double> expMap = dataPoints.stream()
					.collect(Collectors.toMap(dp -> dp.getCanonQsarSmiles(), dp -> dp.getQsarPropertyValue()));

			System.out.println("Cross validation for dataset = " + model.getDatasetName() + ", descriptors = " + model.getDescriptorSetName()
			+ ", splitting = " + model.getSplittingName() + " using QSAR method = " + model.getMethod().getName());

			if (descriptorEmbedding != null) System.out.println("Embedding="+descriptorEmbedding.getEmbeddingTsv());

			calcCV_Folds(model, remove_log_p, num_jobs, expMap, postPredictions);
			
		}
		

		public void addCV_DPIS(Model model) {
			
			Splitting splittingCV1=splittingService.findByName(model.getSplittingName()+"_CV1");
			createSplittings(model, splittingCV1);
			createDataPointInSplittings(model, splittingCV1);
		}


		private void createDataPointInSplittings(Model model, Splitting splittingCV1) {
			//Check if have DPIS:
			String sql="select count(dpis.id) from qsar_datasets.data_points_in_splittings dpis\n"+ 
			"join qsar_datasets.splittings s on s.id=dpis.fk_splitting_id\n"+ 
			"join qsar_datasets.data_points dp on dp.id=dpis.fk_data_point_id\n"+ 
			"join qsar_datasets.datasets d on d.id=dp.fk_dataset_id\n"+ 
			"where s.\"name\" ='"+splittingCV1.getName()+"' and d.\"name\" ='"+model.getDatasetName()+"'";

//			System.out.println(sql);
			
			int countDPIS=Integer.parseInt(DatabaseLookup.runSQL(DatabaseLookup.getConnectionPostgres(), sql));
			
			if (countDPIS!=0) return;
						
//			System.out.println(countDPIS);
			
			List<String>ids=ModelData.getTrainingIds(model, false);
			Collections.shuffle(ids);
			
//			for (String id:ids) {
//				System.out.println(id);
//			}
			
			int idCount=ids.size();
			int countPerSplit=idCount/numSplits;
			
//			System.out.println(idCount);
			
			Hashtable<Integer,List<String>>htSplits=createSplitHashtable(ids, countPerSplit);
			
			
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


		private void createSplittings(Model model, Splitting splittingCV1) {
			
			if (splittingCV1!=null) return;
			
			for (int fold=1;fold<=5;fold++) {
				String name=model.getSplittingName()+"_CV"+fold;
				String description="Cross validation split "+fold+" for splitting = "+model.getSplittingName();
				Splitting splitting=new Splitting(name, description, 2, lanId);
				splittingService.create(splitting);
			}
			
		}


		private  Hashtable<Integer,List<String>> createSplitHashtable(List<String> ids, int countPerSplit) {
			Hashtable<Integer,List<String>>htSplits=new Hashtable<>();

			for (int fold=1;fold<=numSplits;fold++) {

				for (int j=1;j<=countPerSplit;j++) {
					if(htSplits.get(fold)==null) {
						List<String>ids_i=new ArrayList<>();
						htSplits.put(fold, ids_i);
						ids_i.add(ids.remove(0));
					} else {
						List<String>ids_i=htSplits.get(fold);
						ids_i.add(ids.remove(0));
					}
				}
//				System.out.println(fold+"\t"+htSplits.get(fold).size());
			}

			//Distribute remaining ids:
			int countRemaining=ids.size();
			int currentFold=1;
			for (int i=1;i<=countRemaining;i++) {
				List<String>ids_i=htSplits.get(currentFold);
				ids_i.add(ids.remove(0));
				currentFold++;
			}
			
//			for (int fold=1;fold<=numSplits;fold++) {
//				List<String>ids_i=htSplits.get(fold);
//				System.out.println(ids_i.size());
//			}
			
			return htSplits;
		}

		void calcCV_Folds( Model model, boolean remove_log_p, int num_jobs,Map<String, Double> expMap,boolean postPredictions) {
			
			double Q2_CV=0;
			double R2_CV=0;

			int countChemicals=0;
			
			for (int fold=1;fold<=5;fold++) {
								
				String splittingNameCV=model.getSplittingName()+"_CV"+fold;
				
				System.out.println("running "+splittingNameCV);
				
				List<ModelPrediction>mpsTestSet=crossValidateWithPreselectedDescriptors(model, remove_log_p, splittingNameCV, num_jobs);
				List<ModelPrediction>mpsTrainSet=getTrainingSetModelPredictions(dataPointInSplittingService, model.getDatasetName(), expMap, splittingNameCV);
				
				double Q2_CV_i=ModelStatisticCalculator.calculateQ2(mpsTrainSet, mpsTestSet);
				Q2_CV+=Q2_CV_i;
				
				double YbarTrain=ModelStatisticCalculator.calcMeanExpTraining(mpsTrainSet);				
				Map<String, Double>mapStats=ModelStatisticCalculator.calculateContinuousStatistics(mpsTestSet, YbarTrain, DevQsarConstants.TAG_TEST);				
				double R2_CV_i=mapStats.get(DevQsarConstants.PEARSON_RSQ + DevQsarConstants.TAG_TEST);
				R2_CV+=R2_CV_i;

				if (postPredictions) {
					postPredictions(mpsTestSet, model, splittingService.findByName(splittingNameCV));
				}
				
//				System.out.println(Utilities.gson.toJson(mpsTest));
				countChemicals+=mpsTestSet.size();
			}
			
//			System.out.println(countChemicals);
			
			R2_CV/=5.0;
			Q2_CV/=5.0;
			
			if (postPredictions) {			

				System.out.println("storing R2_CV_Training="+R2_CV);
				Statistic statistic=statisticService.findByName("R2_CV_Training");		
				ModelStatistic modelStatistic=new ModelStatistic(statistic, model, R2_CV, lanId);
				modelStatistic=modelStatisticService.create(modelStatistic);

//				String sql="Select ms.statistic_value from qsar_models.model_statistics ms where ms.fk_model_id="+model.getId()+" and ms.fk_statistic_id="+statistic.getId();
//				String statistic_value=DatabaseLookup.runSQL(DatabaseLookup.getConnectionPostgres(), sql);
																
//				if (statistic_value==null) {
//					modelStatistic=modelStatisticService.create(modelStatistic);
//				} else {
//					System.out.println("Already have "+statistic.getName()+" for "+model.getId());
//				}

				System.out.println("storing Q2_CV_Training="+Q2_CV);
				statistic=statisticService.findByName("Q2_CV_Training");
				modelStatistic=new ModelStatistic(statistic, model, Q2_CV, lanId);
				modelStatistic=modelStatisticService.create(modelStatistic);
				
//				if (modelStatisticService.findByModelId(model.getId(),statistic.getId())==null) {
//					modelStatistic=modelStatisticService.create(modelStatistic);
//				} else {
//					System.out.println("Already have "+statistic.getName()+" for "+model.getId());
//				}

			} else {
				System.out.println("Model = "+model.getId());
				System.out.println("R2_CV_Training="+R2_CV);
				System.out.println("Q2_CV_Training="+Q2_CV);
				System.out.println("");
			}

		}
		
		public List<ModelPrediction> crossValidateWithPreselectedDescriptors(Model model, boolean removeLogP,String splittingNameCV, int num_jobs)  {
			
			String qsarMethod=model.getMethod().getName();		
			qsarMethod=qsarMethod.substring(0,qsarMethod.indexOf("_"));
			
//			System.out.println(qsarMethod);
			
			ModelData data = ModelData.initModelData(model.getDatasetName(), model.getDescriptorSetName(), splittingNameCV, removeLogP,false);
			
			if (data.trainingSetInstances==null) {
//				logger.error("Dataset instances were not initialized");
				System.out.println("Dataset instances were not initialized");
				return null;
			}

			String predictResponse=modelWebService.crossValidate(qsarMethod,data.trainingSetInstances, data.predictionSetInstances, 
					removeLogP, num_jobs, model.getDescriptorEmbedding().getEmbeddingTsv(), model.getHyperparameters()).getBody();
			
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
		
	
		@Deprecated
		void createCV_Statistics(JsonObject jo,Model model) {

			JsonObject joTrainingStats = jo.get("training_stats").getAsJsonObject();		

			double R2_CV_Training=joTrainingStats.get("training_cv_r2").getAsDouble();
			System.out.println("storing R2_CV_Training="+R2_CV_Training);
			Statistic statistic=statisticService.findByName("R2_CV_Training");					
			ModelStatistic modelStatistic=new ModelStatistic(statistic, model, R2_CV_Training, lanId);
			modelStatistic=modelStatisticService.create(modelStatistic);

			double Q2_CV_Training=joTrainingStats.get("training_cv_q2").getAsDouble();
			System.out.println("storing Q2_CV_Training="+Q2_CV_Training);
			statistic=statisticService.findByName("Q2_CV_Training");					
			modelStatistic=new ModelStatistic(statistic, model, Q2_CV_Training, lanId);
			modelStatistic=modelStatisticService.create(modelStatistic);
			
			JsonArray jaPreds=joTrainingStats.get("training_cv_predictions").getAsJsonArray();
			
			ModelPrediction[] mps = gson.fromJson(jaPreds, ModelPrediction[].class);
					
//			for (CrossValidationPrediction cvp:cvps) {
//				System.out.println(cvp.id+"\t"+cvp.exp+"\t"+cvp.pred+"\t"+cvp.split);
//			}

			//TODO need to loop over 5 cross validation splittings
			
					
			List<DataPointInSplitting> dataPointsInSplittingCV1 = 
					dataPointInSplittingService.findByDatasetNameAndSplittingName(model.getDatasetName(), "CV1");
			
			postDPIS_and_CV_predictions(model, mps,dataPointsInSplittingCV1.size()==0);
			
			
//			Splitting splitting=splittingService.findByName(DevQsarConstants.SPLITTING_TRAINING_CROSS_VALIDATION);
//			postPredictions(Arrays.asList(mps), model,splitting);	//use split+10 because split 1 is already taken by test set predictions

		}
		
		/**
		 * Creates DataPointsInSplitting and posts CV prediction
		 * 
		 * @param model
		 * @param mps
		 * @param createDPIS
		 */
		@Deprecated
		private void postDPIS_and_CV_predictions(Model model, ModelPrediction[] mps,boolean createDPIS) {
			
			Hashtable<Integer,Splitting>htSplittings=new Hashtable<>();
			for (int i=1;i<=5;i++) {
				Splitting splitting=splittingService.findByName("CV"+i);
				htSplittings.put(i,splitting);
			}
					
			Hashtable<Integer,List<ModelPrediction>>htSets=new Hashtable<>();
			for (ModelPrediction mp:mps) {
				if(htSets.get(mp.split)==null) {
					List<ModelPrediction>mpsTestSet=new ArrayList<>();
					mpsTestSet.add(mp);
					htSets.put(mp.split, mpsTestSet);
					
				} else {
					List<ModelPrediction>mpsTestSet=htSets.get(mp.split);
					mpsTestSet.add(mp);					
				}
			}
			
			for (Integer key:htSets.keySet()) {			
				Splitting splittingCV=htSplittings.get(key);
				List<ModelPrediction>mpsTestSet=htSets.get(key);//predicts in test sets for CV

				if (createDPIS) {				
					System.out.print("Creating CV DPIS Split "+key+" ");
					createCV_DPIS(model, splittingCV, mpsTestSet);
					System.out.print("done\n");
				}			
				
				System.out.print("Posting CV predictions Split "+key+" ");
				postPredictions(mpsTestSet, model, splittingCV);
				System.out.print("done\n");
				
			}
		}
		
		
		@Deprecated
		private void createCV_DPIS(Model model, Splitting splittingCV, List<ModelPrediction> mpsTestSet) {
			
			
			List<DataPointInSplitting> dataPointsInSplittingModel = 
					dataPointInSplittingService.findByDatasetNameAndSplittingName(model.getDatasetName(), model.getSplittingName());

			
			List<DataPoint> dataPoints = 
					dataPointService.findByDatasetName(model.getDatasetName());

			Map<String, DataPoint> dataPointMap = dataPoints.stream()
					.collect(Collectors.toMap(dp -> dp.getCanonQsarSmiles(), dp -> dp));

			
			
			List<String>idsTest=new ArrayList<>();
			for (ModelPrediction mp:mpsTestSet) idsTest.add(mp.id);					

			
			List<DataPointInSplitting>dpisTrainingSet=new ArrayList<>();//training set for CV- need for stats
			List<DataPointInSplitting>dpisTestSet=new ArrayList<>();//training set for CV- need for stats

			for(ModelPrediction mp:mpsTestSet) {
				DataPointInSplitting dpisNew=new DataPointInSplitting(dataPointMap.get(mp.id), splittingCV, DevQsarConstants.TEST_SPLIT_NUM, lanId);
				dpisTestSet.add(dpisNew);										
			}
			
			for (DataPointInSplitting dpis:dataPointsInSplittingModel) {

				if(dpis.getSplitNum()!=DevQsarConstants.TRAIN_SPLIT_NUM) continue;//can have splitNum=2 for the PFAS ones...
				
				if(!idsTest.contains(dpis.getDataPoint().getCanonQsarSmiles())) {
					//in the model splitting training set and not in CV test set:
					DataPointInSplitting dpisNew=new DataPointInSplitting(dpis.getDataPoint(), splittingCV, DevQsarConstants.TRAIN_SPLIT_NUM, lanId);
					dpisTrainingSet.add(dpisNew);										
				}
			}
			
			for (DataPointInSplitting dpis:dpisTrainingSet) {
				dataPointInSplittingService.create(dpis);//TODO do this in batch mode
			}
			
			for (DataPointInSplitting dpis:dpisTestSet) {
				dataPointInSplittingService.create(dpis);//TODO do this in batch mode
			}
		}

	}
	
	

	/**
	 * Validates a Python model with the given data and parameters
	 * @param data
	 * @param params
	 */
	public void predictPythonStorage(ModelData data, String methodName, Long modelId) throws ConstraintViolationException {
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
		
		ModelBytes modelBytes = modelBytesService.findByModelId(modelId);
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
		
		calculateAndPostModelStatistics(Arrays.asList(modelTrainingPredictions), Arrays.asList(modelPredictions), model);

	}

	/**
	 * Validates a Python model with the given data and parameters
	 * @param data
	 * @param params
	 */
	public void predict(ModelData data, String methodName, Long modelId) throws ConstraintViolationException {
		
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
		
		ModelBytes modelBytes = modelBytesService.findByModelId(modelId);
		Model model = modelBytes.getModel();
		byte[] bytes = modelBytes.getBytes();
		
		String strModelId = String.valueOf(modelId);
		modelWebService.callInit(bytes, methodName, strModelId).getBody();
		
		Splitting splitting=splittingService.findByName(model.getSplittingName());
		
//		System.out.println("Splitting id = "+splitting.getId());
				
		String predictResponse = modelWebService.callPredict(data.predictionSetInstances, methodName, strModelId).getBody();
		ModelPrediction[] modelPredictionsArray = gson.fromJson(predictResponse, ModelPrediction[].class);
		List<ModelPrediction>modelTestPredictions=Arrays.asList(modelPredictionsArray);			
		for(ModelPrediction mp:modelTestPredictions) mp.split=DevQsarConstants.TEST_SPLIT_NUM;		
		postPredictions(modelTestPredictions, model,splitting);		
		
		List<ModelPrediction> modelTrainingPredictions = predictTraining(model, data, methodName, strModelId);
		for(ModelPrediction mp:modelTrainingPredictions) mp.split=DevQsarConstants.TRAIN_SPLIT_NUM;
		postPredictions(modelTrainingPredictions, model,splitting);
		
		calculateAndPostModelStatistics(modelTrainingPredictions, modelTestPredictions, model);
	}

	private List<ModelPrediction> predictTraining(Model model, ModelData data, String methodName, String strModelId) {
		if (data.trainingSetInstances==null) {
//			logger.error("Dataset instances were not initialized");
			return null;
		}
		
		String predictTrainingResponse = modelWebService.callPredict(data.trainingSetInstances, methodName, strModelId).getBody();
		ModelPrediction[] modelTrainingPredictions = gson.fromJson(predictTrainingResponse, ModelPrediction[].class);

		return Arrays.asList(modelTrainingPredictions);
	}

	public Long build(String datasetName, String descriptorSetName, String splittingName, boolean removeLogP_Descriptors,
			String methodName) throws ConstraintViolationException {
		ModelData data = ModelData.initModelData(datasetName, descriptorSetName, splittingName, removeLogP_Descriptors,false);
		
		Long modelId = train(data, methodName);
		predict(data, methodName, modelId);
		
		return modelId;
	}
	
	public Long buildWithPythonStorage(String datasetName, String descriptorSetName, String splittingName, 
			boolean removeLogDescriptors, String methodName, String descriptorEmbeddingName) throws ConstraintViolationException {
		ModelData data = ModelData.initModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors,false);
		
		Long modelId = trainWithPythonStorage(data, methodName, descriptorEmbeddingName);
		predictPythonStorage(data, methodName, modelId);
		
		return modelId;
	}


	public Long buildWithPreselectedDescriptors(String datasetName, String descriptorSetName, String splittingName, 
			boolean removeLogDescriptors, String methodName, String descriptorEmbeddingName) throws ConstraintViolationException {
		ModelData data =ModelData.initModelData(datasetName, descriptorSetName, splittingName, removeLogDescriptors,false);
		
		Long modelId = trainWithPreselectedDescriptorsByEmbeddingName(data, methodName, descriptorEmbeddingName);
		predict(data, methodName, modelId);
		
		return modelId;
	}
	
	
	public Long buildWithPreselectedDescriptors(String methodName,CalculationInfo ci, DescriptorEmbedding descriptorEmbedding) throws ConstraintViolationException {
		ModelData data = ModelData.initModelData(ci.datasetName, ci.descriptorSetName, ci.splittingName, ci.remove_log_p,false);
		
		Long modelId = trainWithPreselectedDescriptors(data, methodName, descriptorEmbedding);
		predict(data, methodName, modelId);
		
		return modelId;
	}
	
	

	public ModelPrediction[] rerunExistingModelPredictions(Long modelId) {
		ModelBytes modelBytes = modelBytesService.findByModelId(modelId);
		Model model = modelBytes.getModel();
		
		ModelData data = ModelData.initModelData(model.getDatasetName(), model.getDescriptorSetName(), model.getSplittingName(), false,false);
		
		byte[] bytes = modelBytes.getBytes();
		String methodName = model.getMethod().getName();
		String pythonMethodName = methodName.substring(0, methodName.indexOf("_"));
		String strModelId = String.valueOf(modelId);
		
		modelWebService.callInit(bytes, pythonMethodName, strModelId).getBody();
		String predictResponse = modelWebService.callPredict(data.predictionSetInstances, pythonMethodName, strModelId).getBody();
		ModelPrediction[] modelPredictions = gson.fromJson(predictResponse, ModelPrediction[].class);
		
		return modelPredictions;
	}

	public long build(String methodName, CalculationInfo ci){
		
		ModelData data = ModelData.initModelData(ci,false);
		
		Long modelId = train(data, methodName);
		predict(data, methodName, modelId);
		
		return modelId;
	}
	
	public static void main(String[] args) {

		WebServiceModelBuilder mb=new WebServiceModelBuilder (null,"tmarti02");
		Model model=mb.modelService.findById(1138L);
		mb.crossValidate.addCV_DPIS(model);
		
	}

}
