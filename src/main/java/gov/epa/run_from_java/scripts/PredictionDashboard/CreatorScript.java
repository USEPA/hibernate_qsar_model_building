package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

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
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxOtherCASRN;
import gov.epa.databases.dev_qsar.qsar_models.entity.MethodAD;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxRecordOtherCASRNServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.MethodADServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticServiceImpl;

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

	
	public static Unit createUnit(String unitName,String lanId) {
		
//		String unitName=getFieldName(unitAbbrev);
		String unitAbbrev=getFieldValue(unitName);
		
		Unit unit=unitService.findByName(unitName);
		
		if (unit!=null) {
//			System.out.println("Have unit:\t"+unit.getName()+"\t"+unit.getAbbreviation());
		} else {
			ExpPropUnit unitExpProp=unitServiceExpProp.findByName(unitName);
			
			if (unitExpProp!=null) {
//					System.out.println("we have exp_prop property="+propertyNameDB);
				unit=Unit.fromExpPropUnit(unitExpProp, lanId);
				unit=unitService.create(unit);
			} else {
				System.out.print("Creating unit for "+unitName+"...");
				unit=new Unit(unitName,unitAbbrev,lanId);
				unit=unitService.create(unit);
				System.out.println("done");
			}
		}
		return unit;
	}
	
	
	public static Dataset createDataset(Dataset dataset) {
		
		Dataset datasetDB=datasetService.findByName(dataset.getName());

		if (datasetDB!=null) 
			return datasetDB;

		System.out.print("Creating dataset "+dataset.getName()+"...");
		datasetDB=datasetService.create(dataset);
		System.out.println("done");
		
		return datasetDB;

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

			//If have same name and source id then we have a match:
			if(!currentModel.getName().equals(model.getName()))continue;
			if(!currentModel.getSource().getName().equals(model.getSource().getName()))continue;
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
	
	public static String getFieldValue(String fieldName)  {
		try {
			DevQsarConstants d=new DevQsarConstants();
			Field field=d.getClass().getField(fieldName);
			return (String) field.get(d);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		System.out.println("Unknown value for "+fieldName);
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
	
	/**
	 * Gets map of other casrns
	 * Key = DsstoxRecord id
	 * Value = list of DsstoxOtherCASRNs
	 * 
	 * @return
	 */
	public static TreeMap<Long, List<DsstoxOtherCASRN>> getOtherCAS_Map() {
		DsstoxRecordOtherCASRNServiceImpl ocs=new DsstoxRecordOtherCASRNServiceImpl();
		
//		System.out.println("Getting recs OtherCAS");
//		List<DsstoxOtherCASRN>recsOtherCAS=ocs.findAll();//slow for some reason
		List<DsstoxOtherCASRN>recsOtherCAS=ocs.findAllSql();
		
//		System.out.println("Done");
		
		TreeMap<Long,List<DsstoxOtherCASRN>> tmOtherCAS=new TreeMap<>();
		
		for(DsstoxOtherCASRN recOtherCAS:recsOtherCAS) {
			
			long id=recOtherCAS.getFk_dsstox_record_id();
			
			if(tmOtherCAS.get(id)==null) {
				List<DsstoxOtherCASRN>otherCASRNs=new ArrayList<>();
				tmOtherCAS.put(id,otherCASRNs);
				otherCASRNs.add(recOtherCAS);
			} else {
				List<DsstoxOtherCASRN>otherCASRNs=tmOtherCAS.get(id);
				otherCASRNs.add(recOtherCAS);
			}
		}
		return tmOtherCAS;
	}
	

	public static TreeMap <String,Property> getPropertyMap() {
		
		PropertyServiceImpl ps=new PropertyServiceImpl();
		List<Property>properties=ps.findAll();

		TreeMap <String,Property>mapProperties=new TreeMap<>();
		for (Property property:properties) {
			mapProperties.put(property.getName(), property);
		}
		return mapProperties;
	}
	

	public static TreeMap<String, Dataset> getDatasetsMap() {
		
		DatasetServiceImpl ps=new DatasetServiceImpl();
		List<Dataset>datasets=ps.findAll();

		TreeMap <String,Dataset>mapDatasets=new TreeMap<>();
		for (Dataset dataset:datasets) {
			mapDatasets.put(dataset.getName(), dataset);
		}
		return mapDatasets;
	}

	public static TreeMap<String, Model> getModelsMap() {
		
		ModelServiceImpl ps=new ModelServiceImpl();
		List<Model>models=ps.getAll();

		TreeMap <String,Model>mapModels=new TreeMap<>();
		for (Model model:models) {
			mapModels.put(model.getName(), model);
		}
		return mapModels;
	}
	
	private TreeMap<String, Statistic> getStatisticsMap() {
		
		StatisticService ps=new StatisticServiceImpl();
		List<Statistic>statistics=ps.getAll();

		TreeMap <String,Statistic>mapStatistics=new TreeMap<>();
		for (Statistic statistic:statistics) {
			mapStatistics.put(statistic.getName(), statistic);
		}
		return mapStatistics;
	}


	public static TreeMap<String,MethodAD> getMethodAD_Map() {
	
			MethodADServiceImpl servMAD=new MethodADServiceImpl();
			
			List<MethodAD>methodADs=servMAD.findAll();
	
			TreeMap<String,MethodAD>map=new TreeMap<>();
			
			for (MethodAD methodAD:methodADs) {
				map.put(methodAD.getName(), methodAD);
	//			System.out.println(methodAD.getName()+"\t"+methodAD.getDescription());
			}
	
			return map;
		}

}
