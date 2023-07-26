package gov.epa.run_from_java.data_loading;

import java.lang.reflect.Field;
import java.util.regex.Matcher;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;
import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.Parameter;
import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueService;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.SourceChemicalService;
import gov.epa.databases.dev_qsar.exp_prop.service.SourceChemicalServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

public class PropertyValueCreator {
	/**
	 * 
	 */
	final ExperimentalRecordLoader loader;

	private SourceChemicalService sourceChemicalService = new SourceChemicalServiceImpl();
	private PropertyValueService propertyValueService = new PropertyValueServiceImpl();

	/**
	 * @param experimentalRecordLoader
	 */
	public PropertyValueCreator(ExperimentalRecordLoader experimentalRecordLoader) {
		loader = experimentalRecordLoader;
	}




	
	

	


	public PropertyValue createPropertyValue(ExperimentalRecord er,boolean createDB_Entries) {

		PropertyValue pv=getPropertyValue(er);

		setProperty(er,pv);
		setReliabilityValue(er,pv);
		setUnit(er,pv);

		setPublicSource(er, pv, createDB_Entries);
		setPublicSourceOriginal(er, pv, createDB_Entries);
		setLiteratureSource(er, pv, createDB_Entries);
				
		setSourceChemical(er,pv,createDB_Entries);		

//		System.out.println("publicSourceId="+pv.getPublicSource().getId());
//		System.out.println("publicSourceOriginalId="+pv.getPublicSourceOriginal().getId());
//		System.out.println("literatureSourceId="+pv.getLiteratureSource().getId());
//		System.out.println("sourceChemicalId="+pv.getSourceChemical().getId());
		
		String url = er.url;

		if (url==null || url.isBlank() || 
				(pv.getPublicSource()!=null && url.equals(pv.getPublicSource().getUrl())) 
				|| (pv.getLiteratureSource()!=null && url.equals(pv.getLiteratureSource().getUrl()))) {
			// No individual page URL, do nothing
		} else {
			pv.setPageUrl(url);
		}

		return pv;
	}

	private void setProperty(ExperimentalRecord er,PropertyValue pv) {
		if (loader.propertiesMap.containsKey(er.property_name)) {
			pv.setProperty(loader.propertiesMap.get(er.property_name));						
		} else {
			System.out.println("Unknown property:"+er.property_name);
		}
	}

	private void setUnit(ExperimentalRecord er,PropertyValue pv) {

		String unitName=DevQsarConstants.getExpPropUnitName(er.property_name,er.property_value_units_final);

		if (loader.unitsMap.containsKey(unitName)) {
			pv.setUnit(loader.unitsMap.get(unitName));
		} else {
			//TODO should we add missing units to units table?
//			System.out.println("Unknown unitName:"+unitName);
		}
	}


	private void setPublicSource(ExperimentalRecord er,PropertyValue pv, boolean createDB_Entries) {

		if(er.source_name==null) return;

		String name=er.source_name;
		

		if(loader.publicSourcesMap.containsKey(name)) {
			pv.setPublicSource(loader.publicSourcesMap.get(name));
		} else {
			
			PublicSource ps = new PublicSource();

			ps.setName(name);
			ps.setDescription("TODO");
			ps.setCreatedBy(loader.lanId);

			if(createDB_Entries) {
				ps = loader.publicSourceService.create(ps);
				loader.publicSourcesMap.put(name, ps);
//				System.out.println("publicSource.id="+ps.getId());
			}
			pv.setPublicSource(ps);
		}

	}


	private void setPublicSourceOriginal(ExperimentalRecord er,PropertyValue pv, boolean createDB_Entries) {

		if(er.original_source_name==null) return;

		String name=er.original_source_name;

		if(loader.publicSourcesMap.containsKey(name)) {

			pv.setPublicSourceOriginal(loader.publicSourcesMap.get(name));

		} else {

			PublicSource ps = new PublicSource();

			ps.setName(name);
			ps.setDescription("TODO");
			ps.setCreatedBy(loader.lanId);

			if(createDB_Entries) {
				try {
					ps = loader.publicSourceService.create(ps);
					loader.publicSourcesMap.put(name, ps);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			pv.setPublicSourceOriginal(ps);
		}



	}



	private void setLiteratureSource(ExperimentalRecord er, PropertyValue pv,boolean createDB_Entries) {

		if (er.literatureSource==null) {
			return;
		}

		String literatureSourceName=er.literatureSource.getName();//should already be set		

		if (loader.literatureSourcesMap.containsKey(literatureSourceName)) {
			pv.setLiteratureSource(loader.literatureSourcesMap.get(literatureSourceName));
		} else {
			er.literatureSource.setName(literatureSourceName);
			LiteratureSource ls=er.literatureSource;
			ls.setCreatedBy(loader.lanId);
			
			if(createDB_Entries) {
				try {
					ls = loader.literatureSourceService.create(ls);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			loader.literatureSourcesMap.put(literatureSourceName, ls);
			pv.setLiteratureSource(ls);

		}

	}

	public boolean postPropertyValue(PropertyValue propertyValue) {
		
		try {
			propertyValue = propertyValueService.create(propertyValue);
			
			if (propertyValue!=null) {
				return true;
			} else {
				return false;
			}

		} catch(Exception ex) {
			ex.printStackTrace();
			return false;
		}

	}

	
	

	private void setSourceChemical(ExperimentalRecord rec,PropertyValue pv,boolean createDB_Entries) {
		
		
		SourceChemical sourceChemical = new SourceChemical();
		sourceChemical.setCreatedBy(loader.lanId);

		if (rec.casrn!=null && !rec.casrn.isBlank()) {
			sourceChemical.setSourceCasrn(rec.casrn);
		}

		if (rec.chemical_name!=null && !rec.chemical_name.isBlank()) {
			sourceChemical.setSourceChemicalName(rec.chemical_name);
		}

		if (rec.smiles!=null && !rec.smiles.isBlank()) {
			sourceChemical.setSourceSmiles(rec.smiles);
		}

		if (rec.dsstox_substance_id!=null && !rec.dsstox_substance_id.isBlank()) {
			if (rec.dsstox_substance_id.startsWith("DTXCID")) {
				sourceChemical.setSourceDtxcid(rec.dsstox_substance_id);
			} else if (rec.dsstox_substance_id.startsWith("DTXSID")) {
				sourceChemical.setSourceDtxsid(rec.dsstox_substance_id);
			} else if (rec.dsstox_substance_id.startsWith("DTXRID")) {
				sourceChemical.setSourceDtxrid(rec.dsstox_substance_id);
			}
		}
		
		if(pv.getPublicSource()!=null) {
			sourceChemical.setPublicSource(pv.getPublicSource());
		}

		if(pv.getLiteratureSource()!=null) {
			sourceChemical.setLiteratureSource(pv.getLiteratureSource());
		}

		
		if (loader.sourceChemicalMap.containsKey(sourceChemical.getKey())) {
			sourceChemical=loader.sourceChemicalMap.get(sourceChemical.getKey());
		} else {
			if(createDB_Entries) {
				sourceChemical = sourceChemicalService.create(sourceChemical);
				loader.sourceChemicalMap.put(sourceChemical.getKey(), sourceChemical);
			} 
		}

		
		pv.setSourceChemical(sourceChemical);
	}

	private PropertyValue getPropertyValue(ExperimentalRecord rec) {
		PropertyValue propertyValue = new PropertyValue();
		propertyValue.setCreatedBy(loader.lanId);

		propertyValue.setValueQualifier(rec.property_value_numeric_qualifier);
		propertyValue.setValuePointEstimate(rec.property_value_point_estimate_final);
		propertyValue.setValueMin(rec.property_value_min_final);
		propertyValue.setValueMax(rec.property_value_max_final);

		if (rec.property_value_qualitative!=null && 
				rec.property_value_qualitative.length()>255) { 
			rec.property_value_qualitative = rec.property_value_qualitative.substring(0, 255);
		}
		propertyValue.setValueText(rec.property_value_qualitative);
		if (rec.property_value_string!=null && 
				rec.property_value_string.length()>1000) { 
			rec.property_value_string = rec.property_value_string.substring(0, 1000);
		}
		propertyValue.setValueOriginal(rec.property_value_string);
		propertyValue.setNotes(rec.note);
		propertyValue.setQcFlag(rec.flag);
		propertyValue.setKeep(rec.keep);
		propertyValue.setKeepReason(rec.reason);

		return propertyValue;
	}

	private void setReliabilityValue(ExperimentalRecord rec,PropertyValue pv) {

		if (rec.reliability!=null) {
			ParameterValue reliabilityValue = new ParameterValue();
			reliabilityValue.setCreatedBy(loader.lanId);
			reliabilityValue.setValueText(rec.reliability);
			reliabilityValue.setPropertyValue(pv);
			reliabilityValue.setParameter(loader.parametersMap.get("Reliability"));
			reliabilityValue.setUnit(loader.unitsMap.get("Text"));
			pv.addParameterValue(reliabilityValue);
		} 
	}






	
}