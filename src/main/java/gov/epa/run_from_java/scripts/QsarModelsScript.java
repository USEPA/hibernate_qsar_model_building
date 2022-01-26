package gov.epa.run_from_java.scripts;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInModelSet;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSet;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInModelSetService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInModelSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetServiceImpl;
import gov.epa.endpoints.reports.model_sets.ModelSetTable;
import gov.epa.endpoints.reports.model_sets.ModelSetTableGenerator;

public class QsarModelsScript {
	
	private ModelService modelService;
	private ModelSetService modelSetService;
	private ModelInModelSetService modelInModelSetService;
	
	private String lanId;
	
	public QsarModelsScript(String lanId) {
		this.modelService = new ModelServiceImpl();
		this.modelSetService = new ModelSetServiceImpl();
		this.modelInModelSetService = new ModelInModelSetServiceImpl();
		this.lanId = lanId;
	}
	
	public void createModelSet(String name, String description) {
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

	private void addModelToSet(Model model, ModelSet modelSet) {
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
		Set<ConstraintViolation<ModelInModelSet>> violations = modelInModelSetService.create(modelInModelSet);
		if (violations!=null && !violations.isEmpty()) {
			System.out.println("Error: Failed to add model " + modelId + " to model set " + modelSetId + ":");
			for (ConstraintViolation<ModelInModelSet> violation:violations) {
				System.out.println("\t" + violation.getMessage());
			}
		}
	}
	
	private void addModelsToSet(List<Model> models, Long modelSetId) {
		ModelSet modelSet = modelSetService.findById(modelSetId);
		for (Model model:models) {
			addModelToSet(model, modelSet);
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
	
	public static void main(String[] args) {
//		QsarModelsScript script = new QsarModelsScript("gsincl01");
//		script.removeModelFromSet(6L, 7L);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		ModelSetTableGenerator gen = new ModelSetTableGenerator();
		ModelSetTable table = gen.generate(7L);
		System.out.println(gson.toJson(table));
	}

}
