package gov.epa.run_from_java.scripts;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.validation.ConstraintViolationException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_datasets.service.SplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.SplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInModelSet;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelQmrf;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSet;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSetReport;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInModelSetService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInModelSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelQmrfService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelQmrfServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetReportService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetReportServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetServiceImpl;
import gov.epa.endpoints.reports.model_sets.ModelSetTable;
import gov.epa.endpoints.reports.model_sets.ModelSetTableGenerator;

public class QsarModelsScript {
	
	private ModelService modelService = new ModelServiceImpl();
	private ModelQmrfService modelQmrfService = new ModelQmrfServiceImpl();
	private ModelSetService modelSetService = new ModelSetServiceImpl();
	private ModelSetReportService modelSetReportService = new ModelSetReportServiceImpl();
	private ModelInModelSetService modelInModelSetService = new ModelInModelSetServiceImpl();
	
	private String lanId;
	
	public QsarModelsScript(String lanId) {
		this.lanId = lanId;
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
	
	public void uploadModelQmrf(Long modelId, String qmrfFilePath) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(qmrfFilePath));
		Model model = modelService.findById(modelId);
		
		if (bytes==null) {
			System.out.println("No file found at " + qmrfFilePath);
			return;
		} else if (model==null) {
			System.out.println("No model found for ID " + modelId);
			return;
		}
		
		System.out.println("Uploading QMRF with bytecount " + bytes.length);
		ModelQmrf modelQmrf = new ModelQmrf(model, bytes, lanId);
		modelQmrfService.create(modelQmrf);
	}
	
	public byte[] downloadModelQmrf(Long modelId, String downloadFolder) {
		ModelQmrf modelQmrf = modelQmrfService.findByModelId(modelId);
		Model model = modelQmrf.getModel();
		byte[] file = modelQmrf.getFile();
		
		String saveToFilePath = downloadFolder + File.separator + String.join("_", model.getDatasetName(), 
				model.getDescriptorSetName(), 
				model.getSplittingName(),
				model.getMethod().getName()) 
				+ ".pdf";
		
		safelyWriteBytes(saveToFilePath, file, true);
		
		return file;
	}
	
	public void uploadModelSetReport(Long modelSetId, String datasetName, String descriptorSetName, String splittingName, 
			String reportFilePath) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(reportFilePath));
		ModelSet modelSet = modelSetService.findById(modelSetId);
		
		if (bytes==null) {
			System.out.println("No file found at " + reportFilePath);
			return;
		} else if (modelSet==null) {
			System.out.println("No model set found for ID " + modelSetId);
			return;
		}
		
		System.out.println("Uploading Excel model set report with bytecount " + bytes.length);
		ModelSetReport modelSetReport = new ModelSetReport(modelSet, datasetName, descriptorSetName, splittingName, bytes, lanId);
		modelSetReportService.create(modelSetReport);
	}
	
	public byte[] downloadModelSetReport(Long modelSetId, String datasetName, String descriptorSetName, String splittingName,
			String downloadFolder) {
		ModelSetReport modelSetReport = modelSetReportService
				.findByModelSetIdAndModelData(modelSetId, datasetName, descriptorSetName, splittingName);
		return writeOneModelSetReport(modelSetReport, downloadFolder);
	}
	
	public byte[] writeOneModelSetReport(ModelSetReport modelSetReport, String downloadFolder) {
		ModelSet modelSet = modelSetReport.getModelSet();
		byte[] file = modelSetReport.getFile();
		
		String saveToFilePath = downloadFolder + File.separator + String.join("_", modelSet.getName(),
				modelSetReport.getDatasetName(), 
				modelSetReport.getDescriptorSetName(), 
				modelSetReport.getSplittingName())
				+ ".xlsx";
		
		safelyWriteBytes(saveToFilePath, file, true);
		
		return file;
	}
	
	public void downloadAllReportsForModelSet(Long modelSetId, String downloadFolder) {
		List<ModelSetReport> modelSetReports = modelSetReportService.findByModelSetId(modelSetId);
		for (ModelSetReport msr:modelSetReports) {
			writeOneModelSetReport(msr, downloadFolder);
		}
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
	
	public static void main(String[] args) {
//		QsarModelsScript script = new QsarModelsScript("gsincl01");
//		script.removeModelFromSet(6L, 7L);
		
//		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		QsarModelsScript script = new QsarModelsScript("gsincl01");
		
		script.downloadModelQmrf(1L, "data/dev_qsar/model_qmrfs");
		script.downloadAllReportsForModelSet(1L, "data/dev_qsar/model_set_reports");
		
//		try {
//			script.uploadModelQmrf(1L, "data/dev_qsar/this_is_a_pdf.pdf");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
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
		
//		SplittingService sServ = new SplittingServiceImpl();
//		Splitting s = sServ.findByDatasetNameAndSplittingName("Water solubility OPERA", "Fake splitting!");
//		if (s==null) {
//			System.out.println("Dataset has not been split");
//		} else {
//			System.out.println(s.getName());
//		}
	}

}
