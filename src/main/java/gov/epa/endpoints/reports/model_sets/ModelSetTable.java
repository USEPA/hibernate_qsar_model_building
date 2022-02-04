package gov.epa.endpoints.reports.model_sets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;
import gov.epa.endpoints.reports.ModelMetadata;

public class ModelSetTable {
	
	public static class ModelSetTableRow {
		Long propertyId;
		String propertyName;
		String propertyDescription;
		String propertyType;
		Long datasetId;
		String datasetName;
		String datasetDescription;
		String datasetUnitName;
		List<ModelSetTableModelMetadata> modelSetTableModelMetadata = new ArrayList<ModelSetTableModelMetadata>();
		
		public ModelSetTableRow(Dataset dataset) {
			this.datasetId = dataset.getId();
			this.datasetName = dataset.getName();
			this.datasetDescription = dataset.getDescription();
			
			Property property = dataset.getProperty();
			this.propertyId = property.getId();
			this.propertyName = property.getName();
			this.propertyDescription = property.getDescription();
			
			Set<String> propertyCategories = property.getPropertyInCategories().stream()
					.map(pic -> pic.getPropertyCategory().getName().toLowerCase())
					.collect(Collectors.toSet());
			if (propertyCategories.contains("tox")
					|| propertyCategories.contains("toxicity")
					|| propertyCategories.contains("toxicological")
					|| propertyCategories.contains("toxicology")) {
				this.propertyType = "Toxicological";
			} else if (propertyCategories.contains("physchem")
					|| propertyCategories.contains("physicochemical")
					|| propertyCategories.contains("physical")) {
				this.propertyType = "Physicochemical";
			} else {
				this.propertyType = "Other";
			}
			
			Unit unit = dataset.getUnit();
			this.datasetUnitName = unit.getName();
		}
	}
	
	public static class ModelSetTableModelMetadata extends ModelMetadata {
		String descriptorSetName;
		String splittingName;
		
		public ModelSetTableModelMetadata(Long modelId, String qsarMethodName, String qsarMethodDescription,
				String descriptorSetName, String splittingName) {
			super(modelId, qsarMethodName, qsarMethodDescription);
			this.descriptorSetName = descriptorSetName;
			this.splittingName = splittingName;
		}
	}
	
	String modelSetName;
	String modelSetDescription;
	List<ModelSetTableRow> modelSetTableRows = new ArrayList<ModelSetTableRow>();
	
	public ModelSetTable(String modelSetName, String modelSetDescription) {
		this.modelSetName = modelSetName;
		this.modelSetDescription = modelSetDescription;
	}

}
