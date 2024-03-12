package gov.epa.run_from_java.scripts;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.validation.ConstraintViolationException;

import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.Config;
import gov.epa.databases.dev_qsar.qsar_models.entity.FileType;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInModelSet;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelFile;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSet;
//import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSetReport;
import gov.epa.databases.dev_qsar.qsar_models.service.ConfigService;
import gov.epa.databases.dev_qsar.qsar_models.service.ConfigServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInModelSetService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInModelSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelFileService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelFileServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
//import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetReportService;
//import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetReportServiceImpl;


import gov.epa.databases.dev_qsar.qsar_models.service.ModelFileServiceImpl;



import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetServiceImpl;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.models.WebServiceModelBuilder;
import gov.epa.run_from_java.scripts.RecalcStatsScript.SplitPredictions;
import gov.epa.web_services.ModelWebService;
import kong.unirest.Unirest;

public class QsarModelsScript {
	
	private ModelService modelService = new ModelServiceImpl();
	private ModelFileService modelFileService = new ModelFileServiceImpl();
	private ModelSetService modelSetService = new ModelSetServiceImpl();
	
	
	private ModelInModelSetService modelInModelSetService = new ModelInModelSetServiceImpl();
	private ModelBytesService modelBytesService = new ModelBytesServiceImpl();
	
	private String lanId;
	
	public QsarModelsScript(String lanId) {
		this.lanId = lanId;
		
		try {
			Unirest.config().followRedirects(true).socketTimeout(000).connectTimeout(000);
		} catch (Exception e) {
			// Ignore
		}
	}
	
	public void createModelSet(String name, String description) throws ConstraintViolationException {
		ModelSet modelSet = new ModelSet(name, description, lanId);
		modelSetService.create(modelSet);
	}

	public void addModelToSet(Long modelId, Long modelSetId) {
		Model model = modelService.findById(modelId);
		ModelSet modelSet = modelSetService.findById(modelSetId);
		addModelToSet(model, modelSet);
	}

	public void addModelListToSet(List<Long> modelIds, Long modelSetId) {
		List<Model> models = modelService.findByIdIn(modelIds);
		addModelsToSet(models, modelSetId);
	}
	
	public void addModelRangeToSet(Long minModelId, Long maxModelId, Long modelSetId) {
		List<Model> models = modelService.findByIdInRangeInclusive(minModelId, maxModelId);
		addModelsToSet(models, modelSetId);
	}

	private void addModelToSet(Model model, ModelSet modelSet) throws ConstraintViolationException {
		if (model==null) {
			System.out.println("Error: Null model");
			return;
		} else if (modelSet==null) {
			System.out.println("Error: Null model set");
			return;
		}
		
		Long modelId = model.getId();
		Long modelSetId = modelSet.getId();
		if (modelInModelSetService.findByModelIdAndModelSetId(modelId, modelSetId)!=null) {
			System.out.println("Warning: Model " + modelId + " already assigned to model set " + modelSetId);
			return;
		}
		
		ModelInModelSet modelInModelSet = new ModelInModelSet(model, modelSet, lanId);
		modelInModelSetService.create(modelInModelSet);
	}
	
	private void addModelsToSet(List<Model> models, Long modelSetId) {
		ModelSet modelSet = modelSetService.findById(modelSetId);
		for (Model model:models) {
			try {
				addModelToSet(model, modelSet);
			} catch (ConstraintViolationException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	public void removeModelFromSet(Long modelId, Long modelSetId) {
		ModelInModelSet modelInModelSet = modelInModelSetService.findByModelIdAndModelSetId(modelId, modelSetId);
		if (modelInModelSet==null) {
			System.out.println("Warning: Model " + modelId + " not in model set " + modelSetId);
			return;
		}
		
		modelInModelSetService.delete(modelInModelSet);
	}
	
	
	
	public byte[] downloadModelFile(Long modelId, Long fileTypeId,String downloadFolder) {
		ModelFile modelFile = modelFileService.findByModelId(modelId,fileTypeId);
		Model model = modelFile.getModel();
		byte[] file = modelFile.getFile();
		
		String saveToFilePath = downloadFolder + File.separator + String.join("_", "model"+modelId, model.getDatasetName(), 
				model.getDescriptorSetName(), 
				model.getSplittingName(),
				model.getMethod().getName());
		
		if(fileTypeId==1) {
			saveToFilePath+=".pdf";
		} else if (fileTypeId==2) {
			saveToFilePath+=".xlsx";
		} else if (fileTypeId==3 || fileTypeId==4) {
			saveToFilePath+=".jpg";
		}
		safelyWriteBytes(saveToFilePath, file, true);
		
		return file;
	}
	
	
	
	public void uploadModelFile(Long modelId, Long fileTypeId,String reportFilePath) throws IOException, ConstraintViolationException {
		byte[] bytes = Files.readAllBytes(Paths.get(reportFilePath));
		
		System.out.println("Uploading filetype "+fileTypeId+" with bytecount " + bytes.length);
		
		Model model=new Model();
		model.setId(modelId);
		
		FileType fileType=new FileType();
		fileType.setId(fileTypeId);
		
		ModelFile modelSetReport = new ModelFile(model, fileType, bytes, lanId);
		modelFileService.create(modelSetReport);
	}
	
	public void uploadModelFile(Long modelId, Long fileTypeId,byte[] bytes) throws IOException, ConstraintViolationException {

		System.out.println("Uploading filetype "+fileTypeId+" with bytecount " + bytes.length);
		
		Model model=new Model();
		model.setId(modelId);
		
		FileType fileType=new FileType();
		fileType.setId(fileTypeId);
		
		ModelFile modelSetReport = new ModelFile(model, fileType, bytes, lanId);
		modelFileService.create(modelSetReport);
	}
	
	
	public void uploadModelFile(Model model, FileType fileType,String reportFilePath) throws IOException, ConstraintViolationException {
		byte[] bytes = Files.readAllBytes(Paths.get(reportFilePath));
		
		System.out.println("Uploading Excel model set report with bytecount " + bytes.length);
		ModelFile modelSetReport = new ModelFile(model, fileType, bytes, lanId);
		modelFileService.create(modelSetReport);
	}
	
	
	
	public byte[] downloadModelSetReport(Long modelId, Long fileTypeId,String downloadFolder) {
		ModelFile modelFile = modelFileService.findByModelId(modelId, fileTypeId);
		
		return writeSummaryFile(modelFile, downloadFolder);
	}
	
	public byte[] writeSummaryFile(ModelFile modelFile, String downloadFolder) {
		
		byte[] file = modelFile.getFile();
		
		String saveToFilePath = downloadFolder + File.separator + String.join("_", modelFile.getModel().getName(),
				modelFile.getFileType().getName()) 				
				+ ".xlsx";
		
		safelyWriteBytes(saveToFilePath, file, true);
		
		return file;
	}
	
	public byte[] writePMML(long modelID, String downloadFolder,boolean compressed) {
		ModelBytes modelBytes=modelBytesService.findByModelId(modelID,compressed);
		byte[] file = modelBytes.getBytes();
		String saveToFilePath = downloadFolder + File.separator + String.join("_", "model_"+modelID+".pmml");
		safelyWriteBytes(saveToFilePath, file, false);
		return file;
	}
	
	
	public URI safelyWriteBytes(String pathToFile, byte[] bytes, boolean browse) {
		File file = new File(pathToFile);
		file.getParentFile().mkdirs();
		
		Path path = null;
		try {
			path = Paths.get(pathToFile);
		} catch (InvalidPathException e) {
			String safePathToFile = pathToFile.replaceAll("^[A-za-z0-9._-]", "_");
			
			if (safePathToFile.length() > 255) {
				String fileType = safePathToFile.substring(safePathToFile.lastIndexOf("."));
				safePathToFile = safePathToFile.substring(0, (255 - fileType.length())) + fileType;
			}
			
			path = Paths.get(safePathToFile);
		}
		
		if (path==null) {
			System.out.println("Something is wrong with this filename: " + pathToFile + " and I can't fix it");
			return null;
		}
		
		try {
			Files.write(path, bytes);
			URI uri = path.toUri();
			
			if (browse) {
				Desktop desktop = Desktop.getDesktop();
				desktop.browse(uri);
			}
			
			return uri;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	void deleteModel(long modelID) {
		
		System.out.println("deleting model "+modelID);
		
		ModelService modelService = new ModelServiceImpl();
		Model model = modelService.findById(modelID);
		
		if(model==null) {
			System.out.println("No model "+modelID);
			return;
		}
				
		
		ModelFileService modelFileServiceImpl=new ModelFileServiceImpl();		
		ModelFile modelQmrf=modelFileServiceImpl.findByModelId(modelID,1L);		
		
		if (modelQmrf!=null) {
			modelFileServiceImpl.delete(modelQmrf);
		} else {
//			System.out.println("Qmrf for "+modelID+" is null");
		}
		
		ModelBytesService modelBytesService = new ModelBytesServiceImpl();
		modelBytesService.deleteByModelId(model.getId());
		
		modelService.delete(model);

	}
	
	void deleteModel(Model model) {
		deleteModel(model.getId());
	}
	
	/**
	 * Deletes a model by id
	 * Needs to delete the bytes first or it wont work (doesnt happen automatically like other tables do)
	 * 
	 * @param id
	 */
	void deleteModelsNoBytes() {
		ModelServiceImpl ms=new ModelServiceImpl();
		ModelBytesServiceImpl mb=new ModelBytesServiceImpl();
		
		List<Model>models=ms.getAll();
		System.out.println("Number of models="+models.size());
		
		
//		for (Model model:models) {
		for (int i=0;i<models.size();i++) {
			
			if(i<500) continue;
			
			Model model=models.get(i);
			
			System.out.println(i);
			
			if (model.getMethod().getName().contains("consensus")) continue;				
			if (model.getMethod().getName().contains("_")) continue;

			ModelBytes modelBytes=mb.findByModelId(model.getId(),false);
			
			if(modelBytes==null) {
				
//				System.out.println(model.getId()+"\t"+model.getMethod().getName());
//				if (model.getId()==571L) continue;//need to delete consensus it's attached to
				System.out.println("deleting:"+model.getId()+"\t"+model.getMethod().getName());
				ms.delete(model);
								
			}
		}
	}

	void deleteModelsWithAttributes() {

//		String splitting=DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
//		String splitting="T=all but PFAS, P=PFAS";
//		String splitting="T=PFAS only, P=PFAS";
		String splitting="OPERA";

		ModelServiceImpl ms=new ModelServiceImpl();

		List<Model>models=ms.getAll();

		Map <Long,Model>htModels=new TreeMap<>((Collections.reverseOrder()));				
		for (Model model:models)htModels.put(model.getId(), model);


		for (Long key:htModels.keySet()) {
			Model model=htModels.get(key);
			
//			if (!model.getSplittingName().equals(splitting)) continue;
			
//			if(!model.getDatasetName().equals("Henry's law constant OPERA")) continue;
			
//			if(!model.getDatasetName().contains("from exp_prop and chemprop"))continue;
			
//			if(!model.getDescriptorSetName().equals("T.E.S.T. 5.1"))continue; 
			
//			if(!model.getMethod().getName().contains("consensus")) continue;
			
			boolean hasEmbedding=model.getDescriptorEmbedding()!=null;
//			
			if(hasEmbedding) continue;

			if(!model.getDatasetName().contains("v1")) continue;
			
			if(!model.getMethod().getName().contains("consensus")) continue;
			
			
			System.out.println(model.getId()+"\t"+model.getDatasetName()+"\t"+model.getMethod().getName()+"\t"+model.getSplittingName()+"\t"+model.getDescriptorSetName()+"\thasEmbedding="+hasEmbedding);
						
			
			deleteModel(model);
		}
		
		

	}
	
	void deletePMMLModels() {

		String splitting=DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;

		ModelServiceImpl ms=new ModelServiceImpl();

		List<Model>models=ms.getAll();

		Map <Long,Model>htModels=new TreeMap<>((Collections.reverseOrder()));				
		for (Model model:models)htModels.put(model.getId(), model);


		int count=0;
		
		for (Long key:htModels.keySet()) {
			Model model=htModels.get(key);
			boolean hasEmbedding=model.getDescriptorEmbedding()!=null;

//			if(!model.getDatasetName().contains("v1 modeling")) continue;
			if(!model.getDatasetName().contains("res_qsar")) continue;
			
			System.out.println(++count+"\t"+model.getId()+"\t"+model.getDatasetName()+"\t"+model.getMethod().getName()+"\t"+model.getSplittingName()+"\t"+model.getDescriptorSetName()+"\thasEmbedding="+hasEmbedding);
			deleteModel(model);
		}
	}
	
	
	public void compareAPIPredictionsWithDB(Long existingModelId, String server, int port, String lanId)  {
		
		ModelService msi=new ModelServiceImpl();
		
		Model model=msi.findById(existingModelId);
		
//		System.out.println(model.getDescriptorSetName());
		
		
		ModelWebService ws = new ModelWebService(server, port);
		WebServiceModelBuilder mb = new WebServiceModelBuilder(ws, lanId);
		
				
//		System.out.println(use_pmml);
		
		ModelPrediction[] modelPredictions= mb.getModelPredictionsFromAPI(model);
		Hashtable<String,ModelPrediction>htMP_new=new Hashtable<>();
		
		for (ModelPrediction mp:modelPredictions) {
//			System.out.println(mp.id+"\t"+mp.pred);
			htMP_new.put(mp.id, mp);
		}
//		System.out.println(modelPredictions.length+"\n");

		SplitPredictions sp=RecalcStatsScript.SplitPredictions.getSplitPredictionsSql(model, model.getSplittingName());
		
//		System.out.println(sp.testSetPredictions.size());
		
		int errorCount=0;
		
		for (ModelPrediction mp:sp.testSetPredictions) {
			
			if(htMP_new.get(mp.id)==null) {
				System.out.println("Dont have new prediction for "+mp.id+"\t"+mp.pred);
				continue;
			}
			
			ModelPrediction mpNew=htMP_new.get(mp.id);
			
			double diff=Math.abs(mp.pred-mpNew.pred);
			
			if (diff>1e-4) {
				System.out.println(mp.id+"\t"+mp.exp+"\t"+mp.pred+"\t"+mpNew.pred);
				errorCount++;
			}
		}
		System.out.println("Number of diff preds="+errorCount);
		
	}
	

	public void compareSDE_API_PredictionsWithDB(Long existingModelId, String workflow, String server, int port, String lanId,boolean use_pmml, boolean use_cache)  {
		
		ModelService msi=new ModelServiceImpl();
		Model model=msi.findById(existingModelId);
		ModelWebService ws = new ModelWebService(server, port);
		
		ws.configUnirest(false);
		
		WebServiceModelBuilder mb = new WebServiceModelBuilder(ws, lanId);
		
		//Run predictions using SDE API:
		ModelPrediction[] modelPredictions= mb.getModelPredictionsFromSDE_API(model, workflow, use_cache);
				
//		if(true) return;
		
//		Hashtable<String,ModelPrediction>htMP_API=new Hashtable<>();
//		
//		for (ModelPrediction mp:modelPredictions) {
////			System.out.println(mp.id+"\t"+mp.pred);
//			htMP_API.put(mp.id, mp);
//		}
//		System.out.println(modelPredictions.length+"\n");

		//Get predictions from the database:
		SplitPredictions sp=RecalcStatsScript.SplitPredictions.getSplitPredictionsSql(model, model.getSplittingName());
		
//		System.out.println(sp.testSetPredictions.size());
		
		int errorCount=0;
		
		for (int i=0;i<sp.testSetPredictions.size();i++) {
			
//			if(htMP_API.get(mp.id)==null) {
//				System.out.println("Dont have API prediction for "+mp.id+"\t"+mp.pred);
//				continue;
//			}
//			ModelPrediction mpAPI=htMP_API.get(mp.id);
			
			//hopefully values are in exact same order (cant use hashtable approach since SDE changes the smiles)
			ModelPrediction mp=sp.testSetPredictions.get(i);
			ModelPrediction mpAPI=modelPredictions[i];
			
			double diff=Math.abs(mp.pred-mpAPI.pred);
			
			if (diff>1e-4) {
				System.out.println(mp.id+"\t"+mp.exp+"\t"+mp.pred+"\t"+mpAPI.pred+"\tmismatch");
				errorCount++;
			} else {
//				System.out.println(mp.id+"\t"+mp.exp+"\t"+mp.pred+"\t"+mpAPI.pred+"\tmatch");
			}
		}
		System.out.println("Number of diff preds="+errorCount);
		
	}
	

	public void downloadAllModelBytes(String folderPath) {
		File folder = new File(folderPath);
		folder.mkdirs();
		
		List<Model> models = modelService.getAll();
		for (Model model:models) {
			downloadModelBytes(model, folderPath);
		}
	}
	
	public void downloadModelBytes(Long modelId, String folderPath) {
		File folder = new File(folderPath);
		folder.mkdirs();
		
		Model model = modelService.findById(modelId);
		downloadModelBytes(model, folderPath);
	}
	
	@Deprecated
	public void downloadModelBytes(Model model, String folderPath) {
		Long modelId = model.getId();
		ModelBytes modelBytes = modelBytesService.findByModelId(modelId,false);
		if (modelBytes==null) {
			return;
		}
		
		byte[] bytes = modelBytes.getBytes();
		if (bytes==null) {
			return;
		}
		
		String saveToFilePath = folderPath + File.separator + String.join("_", 
				String.valueOf(modelId),
				model.getDatasetName(), 
				model.getDescriptorSetName(), 
				model.getSplittingName(),
				model.getMethod().getName()) 
				+ ".pickle";
		
		System.out.println(saveToFilePath + "\t" + bytes.length);
		safelyWriteBytes(saveToFilePath, bytes, false);
	}
	
	public void restoreAllModelBytes(String folderPath) {
		List<Model> models = modelService.getAll();
		for (Model model:models) {
			String bytesFilePath = folderPath + File.separator + String.join("_", 
					String.valueOf(model.getId()),
					model.getDatasetName(), 
					model.getDescriptorSetName(), 
					model.getSplittingName(),
					model.getMethod().getName()) 
					+ ".pickle";
			
			System.out.println("Restoring from " + bytesFilePath);
			
			byte[] bytes = null;
			try {
				bytes = Files.readAllBytes(Paths.get(bytesFilePath));
			} catch (IOException e) {
				System.out.println("Missing file at " + bytesFilePath);
				continue;
			}
			
			if (bytes==null) {
				System.out.println("No data at " + bytesFilePath);
				continue;
			}
			
			ModelBytes modelBytes = new ModelBytes(model, bytes, lanId);
			modelBytesService.create(modelBytes);
		}
	}

	public void lookAtAllModelBytes() {
		List<Model> models = modelService.getAll();
		for (Model model:models) {
			Long modelId = model.getId();
			if (modelId==168L || modelId==169L) {
				continue;
			}
			
			ModelBytes modelBytes = modelBytesService.findByModelId(modelId,false);
			if (modelBytes==null) {
				continue;
			}
			
			byte[] bytes = modelBytes.getBytes();
			if (bytes==null) {
				continue;
			}
			
			System.out.println(modelId + "\t" + bytes.length);
		}
	}
	
	public static String getComptoxImgUrl(String dsstoxIdType) {
		ConfigService configService = new ConfigServiceImpl();
		String dsstoxIdTypeCaps = dsstoxIdType.toUpperCase();
		if (dsstoxIdTypeCaps.equals("DTXSID") || dsstoxIdTypeCaps.equals("DTXCID")) {
			Config config = configService.findByKey("COMPTOX_" + dsstoxIdTypeCaps + "_IMG_URL");
			if (config!=null) {
				return config.getValueText();
			}
		}
		return null;
	}
	
	
	
	public static void main(String[] args) {
		String lanId="tmarti02";

		QsarModelsScript script = new QsarModelsScript(lanId);
//		script.deletePMMLModels();

		//****************************************************************************************

//		String modelWsServer=DevQsarConstants.SERVER_LOCAL;
//		int modelWsPort=5004;
//		
//		Long modelId=351L;//HLC, RF, no embedding, RND_REPRESENTATIVE splitting
//		boolean usePMML=false;
//		
//		Long modelId=735L;
//		boolean usePMML=true;
//		boolean include_standardization_in_pmml=false;
//		script.compareAPIPredictionsWithDB(modelId, modelWsServer,modelWsPort, lanId);//non pmml based model

		//****************************************************************************************
//		Long modelId=291L;//HLC,RF,embedding, T=PFAS only, P=PFAS- all predictions are same- handles embedding wrong
//		Long modelId=336L;//HLC,RF,no embedding, T=PFAS only, P=PFAS
//		Long modelId=306L;//HLC,RF,embedding, RND_REPRESENTATIVE splitting- predictions are different!
//		Long modelId=351L;//HLC,RF,no embedding, RND_REPRESENTATIVE splitting
//		boolean use_cache=false;
//		script.compareSDE_API_PredictionsWithDB(modelId,"qsar-ready", "http://localhost",8105, lanId,false,use_cache);//non pmml based model
		
		//****************************************************************************************
//		script.deleteModel(861L);
//		script.deleteModel(858L);
//		script.deleteModel(857L);
		
//		script.deleteModel(859L);
//		for (int i=1102;i>=1099;i--) script.deleteModel(i);
		for (int i=1103;i<=1106;i++) script.deleteModel(i);
		

//		script.downloadModelFile(1065L, 2L,"data\\reports\\model files download\\");
//		script.downloadModelFile(1065L, 1L,"data\\reports\\model files download\\");
		
//		script.downloadModelFile(776L, 2L,"data\\reports\\prediction reports upload\\");
		
//		script.downloadModelFile(1038L, 3L,"data\\reports\\prediction reports upload\\");
//		script.downloadModelFile(1038L, 4L,"data\\reports\\prediction reports upload\\");

		//****************************************************************************************
//		long[] modelIds = { 43, 45, 46, 137, 139, 140, 218, 219, 225, 226, 227, 232 };
//		for (long l:modelIds) {
//			script.downloadModelBytes(l, "data/dev_qsar/qsar_models/test_models");
//		}
		//****************************************************************************************

//		QsarModelsScript script = new QsarModelsScript("tmarti02");
//		script.writePMML(411, "data/reports/pmml download",true);
		//****************************************************************************************
//		script.deleteModelsWithAttributes();
		//****************************************************************************************
//		script.removeModelFromSet(6L, 7L);
		//****************************************************************************************
//		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
//		QsarModelsScript script = new QsarModelsScript("gsincl01");
//		script.lookAtAllModelBytes();
		//****************************************************************************************
		
//		script.addModelRangeToSet(145L, 148L, 2L);
//		script.deletePredictionReport();
		
//		for (Long num=145L;num<=148L;num++) script.removeModelFromSet(num, 2L);
		
//		script.downloadModelQmrf(1L, "data/dev_qsar/model_qmrfs");
//		script.downloadAllReportsForModelSet(1L, "data/dev_qsar/model_set_reports");
		
//		script.addModelRangeToSet(151L, 152L, 2L);
		
//		try {
//			script.uploadModelQmrf(1L, "data/dev_qsar/this_is_a_pdf.pdf");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
		//****************************************************************************************

//		try {
//			script.uploadModelSetReport(1L, 
//					"Henry's law constant OPERA",
//					"T.E.S.T. 5.1",
//					"OPERA",
//					"data/dev_qsar/this_is_an_xlsx.xlsx");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//****************************************************************************************

		
//		SplittingService sServ = new SplittingServiceImpl();
//		Splitting s = sServ.findByDatasetNameAndSplittingName("Water solubility OPERA", "Fake splitting!");
//		if (s==null) {
//			System.out.println("Dataset has not been split");
//		} else {
//			System.out.println(s.getName());
//		}

	}

}

