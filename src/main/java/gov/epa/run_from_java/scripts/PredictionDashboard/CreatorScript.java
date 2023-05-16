package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.lang.reflect.Field;
import java.util.List;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropPropertyServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropUnitServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.PropertyServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.UnitServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;

/**
* @author TMARTI02
*/
public class CreatorScript {

	static PropertyServiceImpl propertyService=new PropertyServiceImpl();
	static ExpPropPropertyServiceImpl propertyServiceExpProp=new ExpPropPropertyServiceImpl();

	static UnitServiceImpl unitService=new UnitServiceImpl();
	static ExpPropUnitServiceImpl unitServiceExpProp=new ExpPropUnitServiceImpl();
	
	static ModelServiceImpl modelService=new ModelServiceImpl();
	static DatasetServiceImpl datasetService=new DatasetServiceImpl();

	
	public static Unit createUnit(String unitAbbrev,String lanId) {
		
		
		String unitName=getFieldName(unitAbbrev);
		
		Unit unit=unitService.findByName(unitName);
		
		if (unit!=null) {
//			System.out.println("Have unit:\t"+unit.getName()+"\t"+unit.getAbbreviation());
		} else {
			ExpPropUnit unitExpProp=unitServiceExpProp.findByName(unitName);
			
			if (unitExpProp!=null) {
//					System.out.println("we have exp_prop property="+propertyNameDB);
				unit=Unit.fromExpPropUnit(unitExpProp, lanId);
				unitService.create(unit);
			} else {
				System.out.print("Creating unit for "+unitName+"...");
				unit=new Unit(unitName,unitAbbrev,lanId);
				unit=unitService.create(unit);
				System.out.println("done");
			}
		}
		return unit;
	}
	
	
	public static void createDataset(Dataset dataset) {
		if (datasetService.findByName(dataset.getName())!=null) 
			return;

		System.out.print("Need to create dataset for "+dataset.getName()+"...");
		datasetService.create(dataset);
		System.out.println("done");

	}

	
	public static  Model createModel(Model model) {
		List<Model>models=modelService.findByDatasetName(model.getDatasetName());

		for (Model currentModel:models) {
//			if(!currentModel.getMethod().getName().equals(model.getMethod().getName())) continue;
//			if(!currentModel.getDescriptorSetName().equals(model.getDescriptorSetName())) continue;
//			if(!currentModel.getDatasetName().equals(model.getDatasetName())) continue;
//			if(!currentModel.getSplittingName().equals(model.getSplittingName())) continue;
//			if(!currentModel.getSource().equals(model.getSource())) continue;

			
			if(currentModel.getName()==null) continue;
					
			if(!currentModel.getName().equals(model.getName()))continue; 
			
			if(!currentModel.getSource().equals(model.getSource()))continue;
			return currentModel;
		}
		
		System.out.print("Creating model for "+model.getDatasetName()+"...");
		model=modelService.create(model);
		System.out.println("done");
		return model;
	}
	
	public static String getFieldName(String fieldValue)  {
		try {
			DevQsarConstants d=new DevQsarConstants();
			for (Field field:d.getClass().getDeclaredFields()) {
				if(field.get(d)==null) continue;
				if(field.get(d).equals(fieldValue)) return field.getName();	
			}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		System.out.println("Unknown unit for "+fieldValue);
		return "?";
		
	}

	
	
	public static  Property createProperty(String propertyNameDB, String propertyDescriptionDB,String lanId) {
		Property property=propertyService.findByName(propertyNameDB);
		
		if (property!=null) {
//			System.out.println("Have dataset property:\t"+property.getName()+"\t"+property.getDescription());
		} else {
//				System.out.println("Creating property for "+propertyNameDB);

			ExpPropProperty propertyExpProp=propertyServiceExpProp.findByPropertyName(propertyNameDB);
			
			if (propertyExpProp!=null) {
//				System.out.println("we have exp_prop property="+propertyNameDB);
				property=Property.fromExpPropProperty(propertyExpProp, lanId);
				propertyService.create(property);
			} else {
				System.out.print("Need to create property for "+propertyNameDB+"\t"+propertyDescriptionDB+"...");;
				property=new Property(propertyNameDB,propertyDescriptionDB,lanId);
				property=propertyService.create(property);
				System.out.println("done");
			}
		}
		return property;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
