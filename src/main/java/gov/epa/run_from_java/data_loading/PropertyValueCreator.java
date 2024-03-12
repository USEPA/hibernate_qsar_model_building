package gov.epa.run_from_java.data_loading;

import java.lang.reflect.Field;
import java.util.regex.Matcher;

import javax.validation.ConstraintViolationException;

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

		pv.setDocumentName(er.document_name);
		pv.setFileName(er.file_name);
		setPublicSource(er, pv, createDB_Entries);
		setPublicSourceOriginal(er, pv, createDB_Entries);//in some cases better to store original source info in the document_name if dont need entry in public_sources table
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



	/**
	 * Note this doesnt assure uniqueness of name field in the database.
	 * 
	 * @param er
	 * @param pv
	 * @param createDB_Entries
	 */
	private void setLiteratureSource(ExperimentalRecord er, PropertyValue pv,boolean createDB_Entries) {

		if (er.literatureSource==null) {
			return;
		}

		String literatureSourceCitation=er.literatureSource.getCitation();//should already be set		

				
		if (loader.literatureSourcesMap.containsKey(literatureSourceCitation)) {
			pv.setLiteratureSource(loader.literatureSourcesMap.get(literatureSourceCitation));
		} else {

			LiteratureSource ls=er.literatureSource;
			ls.setCreatedBy(loader.lanId);

			if(createDB_Entries) {
				try {
					ls = loader.literatureSourceService.create(ls);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			loader.literatureSourcesMap.put(literatureSourceCitation, ls);
			pv.setLiteratureSource(ls);

		}

	}

	public boolean postPropertyValue(PropertyValue propertyValue) {

		try {
			propertyValue = propertyValueService.create(propertyValue);
			return (propertyValue!=null);

		} catch(Exception ex) {
			ex.printStackTrace();
			return false;
		}

	}


	private void setSourceChemical(ExperimentalRecord rec,PropertyValue pv,boolean createDB_Entries) {

		SourceChemical sourceChemical =rec.getSourceChemical(loader.lanId, pv.getPublicSource(), pv.getLiteratureSource()); 
		
		SourceChemical dbSourceChemical=null;

		if(loader.sourceChemicalMap.containsKey(sourceChemical.getKey())) {
			dbSourceChemical=loader.sourceChemicalMap.get(sourceChemical.getKey());
			System.out.println("Found in map\t"+sourceChemical.getKey());
		} else if (!ExperimentalRecordLoader.loadSourceChemicalMap) {
			dbSourceChemical = sourceChemicalService.findMatch(sourceChemical);
			System.out.println("Found by one at a time service\t"+sourceChemical.getKey());
		}			
		
		if (dbSourceChemical==null) {

			if(createDB_Entries) {
				try {
					System.out.println("Creating "+sourceChemical.getKey());
					sourceChemical = sourceChemicalService.create(sourceChemical);
					
					//Store in map:
					loader.sourceChemicalMap.put(sourceChemical.getKey(),sourceChemical);
					//TODO Note: unique index for source_chemicals table isnt stopping creation of duplicates if dont put in map in line above
					
				} catch (ConstraintViolationException e) {
					e.printStackTrace();
				}
			}
		} else {
			sourceChemical = dbSourceChemical;
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