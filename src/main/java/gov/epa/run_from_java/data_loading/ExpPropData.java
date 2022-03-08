package gov.epa.run_from_java.data_loading;

import java.util.regex.Matcher;

import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueService;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.SourceChemicalService;
import gov.epa.databases.dev_qsar.exp_prop.service.SourceChemicalServiceImpl;

public class ExpPropData {
		/**
		 * 
		 */
		final ExperimentalRecordLoader loader;
		
		SourceChemicalService sourceChemicalService = new SourceChemicalServiceImpl();
		PropertyValueService propertyValueService = new PropertyValueServiceImpl();

		/**
		 * @param experimentalRecordLoader
		 */
		ExpPropData(ExperimentalRecordLoader experimentalRecordLoader) {
			loader = experimentalRecordLoader;
		}

		public String url;
		public String propertyName;
		public String propertyUnitName;
		public String publicSourceName;
		public String literatureSourceName;
		
		public SourceChemical sourceChemical;
		public PropertyValue propertyValue;
		public ParameterValue reliabilityValue;
		
		public void getValues(ExperimentalRecord rec) {
			url = rec.url;
			propertyName = rec.property_name;
			if (rec.property_name.endsWith("_LC50")) {
				propertyName = "LC50";
			} else if (rec.property_name.endsWith("_LD50")) {
				propertyName = "LD50";
			} else if (rec.property_name.endsWith("_EyeIrritation")) {
				propertyName = "Eye irritation";
			} else if (rec.property_name.endsWith("_EyeCorrosion")) {
				propertyName = "Eye corrosion";
			} else if (rec.property_name.endsWith("_SkinIrritation")) {
				propertyName = "Skin irritation";
			} if (rec.property_name.equals("SkinSensitizationLLNA")) {
				propertyName = "LLNA";
			}
			
			publicSourceName = rec.source_name;
			if (publicSourceName.equals("OECD Toolbox Skin Irritation")) {
				publicSourceName = "OECD Toolbox";
			}
			
			if (publicSourceName.equals("Kodithala")) {
				literatureSourceName = "Kodithala et al. 2002";
			} else if (publicSourceName.equals("Verheyen")) {
				literatureSourceName = "Verheyen et al. 2016";
			} else {
				literatureSourceName = rec.original_source_name;
			}
			
			propertyUnitName = rec.property_value_units_final;
			if (propertyUnitName!=null && propertyUnitName.equals("binary")) {
				propertyUnitName = "Binary";
			} else if (propertyName!=null && propertyUnitName==null) {
				switch (rec.property_name) {
				case "Appearance":
				case "Water solubility":
				case "Vapor pressure":
					propertyUnitName = "Text";
					break;
				case "LLNA":
				case "Eye irritation":
				case "Eye corrosion":
				case "Skin irritation":
					propertyUnitName = "Binary";
				case "pKA":
				case "pKAa":
				case "pKAb":
				case "Octanol water partition coefficient":
				case "LogBCF":
				case "LogOH":
				case "LogKOC":
				case "LogKOA":
				case "LogHalfLife":
				case "LogKmHL":
					propertyUnitName = "Log units";
					break;
				default:
					propertyUnitName = "Missing";
					break;
				}
			}
			
			sourceChemical = getSourceChemical(rec);
			propertyValue = getPropertyValue(rec);
			reliabilityValue = getReliabilityValue(rec);
		}
		
		public void constructPropertyValue(boolean createLiteratureSources) {
			PublicSource ps = loader.publicSourcesMap.get(publicSourceName);
			LiteratureSource ls = loader.literatureSourcesMap.get(literatureSourceName);
			
			sourceChemical.setPublicSource(ps);
			
			if (createLiteratureSources && ls==null && literatureSourceName!=null && !literatureSourceName.isBlank()) {
				String lsName = literatureSourceName.length() > 255 ? literatureSourceName.substring(0, 255) : literatureSourceName;
				LiteratureSource lsNew = new LiteratureSource(lsName, literatureSourceName, loader.lanId);
				ls = loader.literatureSourceService.create(lsNew);
				loader.literatureSourcesMap.put(literatureSourceName, ls);
			}
			sourceChemical.setLiteratureSource(ls);
			
			SourceChemical dbSourceChemical = sourceChemicalService.findMatch(sourceChemical);
			if (dbSourceChemical==null) {
				sourceChemical = sourceChemicalService.create(sourceChemical);
			} else {
				sourceChemical = dbSourceChemical;
				ExperimentalRecordLoader.logger.trace("Found source chemical: " + sourceChemical.generateSrcChemId());
			}
			
			propertyValue.setSourceChemical(sourceChemical);
			propertyValue.setProperty(loader.propertiesMap.get(propertyName));
			propertyValue.setUnit(loader.unitsMap.get(propertyUnitName));
			propertyValue.setPublicSource(ps);
			propertyValue.setLiteratureSource(ls);
			
			if (url==null || url.isBlank() || (ps!=null && url.equals(ps.getUrl())) || (ls!=null && url.equals(ls.getUrl()))) {
				// No individual page URL, do nothing
			} else {
				propertyValue.setPageUrl(url);
			}
			
			if (reliabilityValue!=null) {
				reliabilityValue.setPropertyValue(propertyValue);
				reliabilityValue.setParameter(loader.parametersMap.get("Reliability"));
				reliabilityValue.setUnit(loader.unitsMap.get("Text"));
				propertyValue.addParameterValue(reliabilityValue);
			}
		}
		
		public boolean post() {
			propertyValue = propertyValueService.create(propertyValue);
			if (propertyValue!=null) {
				return true;
			} else {
				return false;
			}
		}
		
		static boolean parseStringColumn(String columnContents, ParameterValue value) {
			Matcher matcher = ExperimentalRecordLoader.STRING_COLUMN_PATTERN.matcher(columnContents);
			
			if (matcher.find()) {
				String qualifier = matcher.group(1);
				String double1 = matcher.group(2);
				String isRange = matcher.group(3);
				String double2 = matcher.group(4);
				
				try {
					value.setValueQualifier(qualifier);
					if (isRange!=null) {
						value.setValueMin(Double.parseDouble(double1));
						value.setValueMax(Double.parseDouble(double2));
					} else {
						value.setValuePointEstimate(Double.parseDouble(double1));
					}
				} catch (Exception e) {
					return false;
				}
				
				return true;
			} else {
				System.out.println("Warning: Failed to parse parameter value from: " + columnContents);
				return false;
			}
		}

		SourceChemical getSourceChemical(ExperimentalRecord rec) {
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
			
			return sourceChemical;
		}

		PropertyValue getPropertyValue(ExperimentalRecord rec) {
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

		ParameterValue getReliabilityValue(ExperimentalRecord rec) {
			if (rec.reliability!=null) {
				ParameterValue reliabilityValue = new ParameterValue();
				reliabilityValue.setCreatedBy(loader.lanId);
				reliabilityValue.setValueText(rec.reliability);
				return reliabilityValue;
			} else {
				return null;
			}
		}
	}