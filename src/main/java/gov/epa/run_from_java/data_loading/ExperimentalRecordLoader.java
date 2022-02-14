package gov.epa.run_from_java.data_loading;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;
import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.Parameter;
import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropPropertyService;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropPropertyServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropUnitService;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropUnitServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.LiteratureSourceService;
import gov.epa.databases.dev_qsar.exp_prop.service.LiteratureSourceServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.ParameterService;
import gov.epa.databases.dev_qsar.exp_prop.service.ParameterServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueService;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.PublicSourceService;
import gov.epa.databases.dev_qsar.exp_prop.service.PublicSourceServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.SourceChemicalService;
import gov.epa.databases.dev_qsar.exp_prop.service.SourceChemicalServiceImpl;

public class ExperimentalRecordLoader {
	
	public class ExpPropData {
		public String url;
		public String propertyName;
		public String propertyUnitName;
		public String publicSourceName;
		public String literatureSourceName;
		
		public SourceChemical sourceChemical;
		public PropertyValue propertyValue;
		public ParameterValue pressureValue;
		public ParameterValue temperatureValue;
		public ParameterValue phValue;
		public ParameterValue measurementMethodValue;
		public ParameterValue reliabilityValue;
		
		public boolean post() {
			PublicSource ps = publicSourcesMap.get(publicSourceName);
			LiteratureSource ls = literatureSourcesMap.get(literatureSourceName);
			
			sourceChemical.setPublicSource(ps);
			sourceChemical.setLiteratureSource(ls);
			
			SourceChemical dbSourceChemical = sourceChemicalService.findMatch(sourceChemical);
			if (dbSourceChemical==null) {
				sourceChemical = sourceChemicalService.create(sourceChemical);
			} else {
				sourceChemical = dbSourceChemical;
				logger.trace("Found source chemical: " + sourceChemical.generateSrcChemId());
			}
			
			propertyValue.setSourceChemical(sourceChemical);
			propertyValue.setProperty(propertiesMap.get(propertyName));
			propertyValue.setUnit(unitsMap.get(propertyUnitName));
			propertyValue.setPublicSource(ps);
			propertyValue.setLiteratureSource(ls);
			
			if (url==null || url.isBlank() || (ps!=null && url.equals(ps.getUrl())) || (ls!=null && url.equals(ls.getUrl()))) {
				// No individual page URL, do nothing
			} else {
				propertyValue.setPageUrl(url);
			}
			
			if (pressureValue!=null) {
				pressureValue.setPropertyValue(propertyValue);
				pressureValue.setParameter(parametersMap.get("Pressure"));
				pressureValue.setUnit(unitsMap.get("mmHg"));
//				QueryExpPropDb.postParameterValue(expPropDbUrl, pressureValue);
				propertyValue.addParameterValue(pressureValue);
			}
			
			if (temperatureValue!=null) {
				temperatureValue.setPropertyValue(propertyValue);
				temperatureValue.setParameter(parametersMap.get("Temperature"));
				temperatureValue.setUnit(unitsMap.get("C"));
//				QueryExpPropDb.postParameterValue(expPropDbUrl, temperatureValue);
				propertyValue.addParameterValue(temperatureValue);
			}
			
			if (phValue!=null) {
				phValue.setPropertyValue(propertyValue);
				phValue.setParameter(parametersMap.get("pH"));
				phValue.setUnit(unitsMap.get("Log units"));
//				QueryExpPropDb.postParameterValue(expPropDbUrl, phValue);
				propertyValue.addParameterValue(phValue);
			}
			
			if (measurementMethodValue!=null) {
				measurementMethodValue.setPropertyValue(propertyValue);
				measurementMethodValue.setParameter(parametersMap.get("Measurement method"));
				measurementMethodValue.setUnit(unitsMap.get("Text"));
//				QueryExpPropDb.postParameterValue(expPropDbUrl, measurementMethodValue);
				propertyValue.addParameterValue(measurementMethodValue);
			}
			
			if (reliabilityValue!=null) {
				reliabilityValue.setPropertyValue(propertyValue);
				reliabilityValue.setParameter(parametersMap.get("Reliability"));
				reliabilityValue.setUnit(unitsMap.get("Text"));
				propertyValue.addParameterValue(reliabilityValue);
			}
			
			propertyValue = propertyValueService.create(propertyValue);
			if (propertyValue!=null) {
				return true;
			} else {
				return false;
			}
		}
	}

	private static final Pattern STRING_COLUMN_PATTERN = Pattern.compile("([~><=]{1,2})?(-?[0-9\\.]+)([-~])?(-?[0-9\\.]+)?");
	
	private ParameterService parameterService = new ParameterServiceImpl();
	private ExpPropPropertyService expPropPropertyService = new ExpPropPropertyServiceImpl();
	private ExpPropUnitService expPropUnitService = new ExpPropUnitServiceImpl();
	private LiteratureSourceService literatureSourceService = new LiteratureSourceServiceImpl();
	private PublicSourceService publicSourceService = new PublicSourceServiceImpl();
	private SourceChemicalService sourceChemicalService = new SourceChemicalServiceImpl();
	private PropertyValueService propertyValueService = new PropertyValueServiceImpl();
	
	private Map<String, LiteratureSource> literatureSourcesMap = new HashMap<String, LiteratureSource>();
	private Map<String, Parameter> parametersMap = new HashMap<String, Parameter>();
	private Map<String, ExpPropProperty> propertiesMap = new HashMap<String, ExpPropProperty>();
	private Map<String, PublicSource> publicSourcesMap = new HashMap<String, PublicSource>();
	private Map<String, ExpPropUnit> unitsMap = new HashMap<String, ExpPropUnit>();
	
	private String lanId;
	
	private static Gson gson =  new GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.serializeSpecialFloatingPointValues()
			.create();
	
	private static final Logger logger = LogManager.getLogger(ExperimentalRecordLoader.class);
	
	public ExperimentalRecordLoader(String lanId) {
		this.lanId = lanId;
		
		Logger apacheLogger = LogManager.getLogger("org.apache.http");
		apacheLogger.setLevel(Level.WARN);
		
		mapTables();
	}
	
	private void mapTables() {
		List<LiteratureSource> literatureSources = literatureSourceService.findAll();
		for (LiteratureSource ls:literatureSources) {
			literatureSourcesMap.put(ls.getName(), ls);
		}
		
		List<Parameter> parameters = parameterService.findAll();
		for (Parameter p:parameters) {
			parametersMap.put(p.getName(), p);
		}
		
		List<ExpPropProperty> properties = expPropPropertyService.findAll();
		for (ExpPropProperty p:properties) {
			propertiesMap.put(p.getName(), p);
		}
		
		List<PublicSource> publicSources = publicSourceService.findAll();
		for (PublicSource ps:publicSources) {
			publicSourcesMap.put(ps.getName(), ps);
		}
		
		List<ExpPropUnit> units = expPropUnitService.findAll();
		for (ExpPropUnit u:units) {
			unitsMap.put(u.getName(), u);
		}
	}
	
	private static ExperimentalRecords getPublicSourceRecords(String publicSourceName) {
		ExperimentalRecords records = new ExperimentalRecords();
		String publicSourceFolderPath = "data/dev_qsar/exp_prop/" + publicSourceName;
		File publicSourceFolder = new File(publicSourceFolderPath);
		File[] publicSourceFiles = publicSourceFolder.listFiles();
		
		String trimmedPublicSourceName = publicSourceName;
		if (publicSourceName.contains("/")) {
			trimmedPublicSourceName = publicSourceName.substring(publicSourceName.lastIndexOf("/") + 1);
		}
		for (File file:publicSourceFiles) {
			String fileName = file.getName();
//			System.out.println(fileName);
			if (!fileName.endsWith(".json")) {
				continue;
			} else if (fileName.startsWith(trimmedPublicSourceName + " Experimental Records")
					&& !fileName.contains("Failed")) {
				records.addAll(ExperimentalRecords.loadFromJson(file.getAbsolutePath(), gson));
			}
		}
		
		return records;
	}
	
	private static ExperimentalRecords getPublicSourceFailedRecords(String publicSourceName) {
		ExperimentalRecords records = new ExperimentalRecords();
		String failedFolderPath = "data/dev_qsar/exp_prop/loaded_with_failures/" + publicSourceName;
		File failedFolder = new File(failedFolderPath);
		File[] failedFiles = failedFolder.listFiles();
		
		String trimmedPublicSourceName = publicSourceName;
		if (publicSourceName.contains("/")) {
			trimmedPublicSourceName = publicSourceName.substring(publicSourceName.lastIndexOf("/") + 1);
		}
		for (File file:failedFiles) {
			String fileName = file.getName();
//			System.out.println(fileName);
			if (!fileName.endsWith(".json")) {
				continue;
			} else if (fileName.startsWith(trimmedPublicSourceName + " Experimental Records")
					&& fileName.contains("Failed")) {
				records.addAll(ExperimentalRecords.loadFromJson(file.getAbsolutePath(), gson));
			}
		}
		
		return records;
	}
	
	public void load(List<ExperimentalRecord> records) {
		List<ExperimentalRecord> failedRecords = new ArrayList<ExperimentalRecord>();
		int countSuccess = 0;
		int countFailure = 0;
		int countTotal = 0;
		for (ExperimentalRecord rec:records) {
			try {
				ExpPropData expPropData = getExpPropData(rec);
				if (expPropData.post()) {
					countSuccess++;
				} else {
					failedRecords.add(rec);
					logger.warn(rec.id_physchem + ": Loading failed");
					countFailure++;
				}
			} catch (Exception e) {
				failedRecords.add(rec);
				logger.warn(rec.id_physchem + ": Loading failed with exception: " + e.getMessage());
				countFailure++;
			}
			
			countTotal++;
			if (countTotal % 1000 == 0) {
				System.out.println("Attempted to load " + countTotal + " property values: " 
						+ countSuccess + " successful; " 
						+ countFailure + " failed");
			}
		}
		System.out.println("Finished attempt to load " + countTotal + " property values: " 
				+ countSuccess + " successful; " 
				+ countFailure + " failed");
		
		if (!failedRecords.isEmpty()) {
			ExperimentalRecord recSample = failedRecords.iterator().next();
			String publicSourceName = recSample.source_name;
			String failedFilePath = "data/dev_qsar/exp_prop/" + publicSourceName + "/" + publicSourceName + " Experimental Records-Failed.json";
			
			File failedFile = new File(failedFilePath);
			if (failedFile.getParentFile()!=null) { failedFile.getParentFile().mkdirs(); }
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(failedFilePath))) {
				bw.write(gson.toJson(failedRecords));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public ExpPropData getExpPropData(ExperimentalRecord rec) {
		ExpPropData expPropData = new ExpPropData();
		
		expPropData.url = rec.url;
		expPropData.propertyName = rec.property_name;
		expPropData.publicSourceName = rec.source_name;
		expPropData.literatureSourceName = rec.original_source_name;
		
		expPropData.propertyUnitName = rec.property_value_units_final;
		if (expPropData.propertyName!=null && expPropData.propertyUnitName==null) {
			switch (rec.property_name) {
			case "Appearance":
			case "Water solubility":
			case "Vapor pressure":
				expPropData.propertyUnitName = "Text";
				break;
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
				expPropData.propertyUnitName = "Log units";
				break;
			default:
				expPropData.propertyUnitName = "Missing";
				break;
			}
		}
		
		expPropData.sourceChemical = getSourceChemical(rec);
		expPropData.propertyValue = getPropertyValue(rec);
		expPropData.pressureValue = getPressureValue(rec);
		expPropData.temperatureValue = getTemperatureValue(rec);
		expPropData.phValue = getPhValue(rec);
		expPropData.measurementMethodValue = getMeasurementMethodValue(rec);
		expPropData.reliabilityValue = getReliabilityValue(rec);
		
		return expPropData;
	}
	
	private SourceChemical getSourceChemical(ExperimentalRecord rec) {
		SourceChemical sourceChemical = new SourceChemical();
		sourceChemical.setCreatedBy(lanId);
		
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
	
	private PropertyValue getPropertyValue(ExperimentalRecord rec) {
		PropertyValue propertyValue = new PropertyValue();
		propertyValue.setCreatedBy(lanId);
		
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
	
	private ParameterValue getPressureValue(ExperimentalRecord rec) {
		if (rec.pressure_mmHg!=null) {
			ParameterValue pressureValue = new ParameterValue();
			pressureValue.setCreatedBy(lanId);
			parseStringColumn(rec.pressure_mmHg, pressureValue);
			return pressureValue;
		} else {
			return null;
		}
	}
	
	private ParameterValue getTemperatureValue(ExperimentalRecord rec) {
		if (rec.temperature_C!=null) {
			ParameterValue temperatureValue = new ParameterValue();
			temperatureValue.setCreatedBy(lanId);
			temperatureValue.setValuePointEstimate(rec.temperature_C);
			return temperatureValue;
		} else {
			return null;
		}
	}
	
	private ParameterValue getPhValue(ExperimentalRecord rec) {
		if (rec.pH!=null) {
			ParameterValue phValue = new ParameterValue();
			phValue.setCreatedBy(lanId);
			parseStringColumn(rec.pH, phValue);
			return phValue;
		} else {
			return null;
		}
	}
	
	private ParameterValue getMeasurementMethodValue(ExperimentalRecord rec) {
		if (rec.measurement_method!=null) {
			ParameterValue measurementMethodValue = new ParameterValue();
			measurementMethodValue.setCreatedBy(lanId);
			measurementMethodValue.setValueText(rec.measurement_method);
			return measurementMethodValue;
		} else {
			return null;
		}
	}
	
	private ParameterValue getReliabilityValue(ExperimentalRecord rec) {
		if (rec.reliability!=null) {
			ParameterValue reliabilityValue = new ParameterValue();
			reliabilityValue.setCreatedBy(lanId);
			reliabilityValue.setValueText(rec.reliability);
			return reliabilityValue;
		} else {
			return null;
		}
	}
	
	private void parseStringColumn(String columnContents, ParameterValue value) {
		Matcher matcher = STRING_COLUMN_PATTERN.matcher(columnContents);
		
		if (matcher.find()) {
			String qualifier = matcher.group(1);
			String double1 = matcher.group(2);
			String isRange = matcher.group(3);
			String double2 = matcher.group(4);
			
			value.setValueQualifier(qualifier);
			if (isRange!=null) {
				value.setValueMin(Double.parseDouble(double1));
				value.setValueMax(Double.parseDouble(double2));
			} else {
				value.setValuePointEstimate(Double.parseDouble(double1));
			}
		} else {
			System.out.println("Warning: Failed to parse parameter value from: " + columnContents);
		}
	}
	
	public static void main(String[] args) {
		System.out.println("Retrieving records...");
		ExperimentalRecords records = getPublicSourceRecords("LookChem");
		System.out.println("Retrieved " + records.size() + " records");
		
		List<ExperimentalRecord> recordsList = records.stream()
				.filter(r -> r.property_name.equals(DevQsarConstants.DENSITY))
				.collect(Collectors.toList());
		
		System.out.println("Loading " + recordsList.size() + " records...");
		long t0 = System.currentTimeMillis();
		ExperimentalRecordLoader loader = new ExperimentalRecordLoader("gsincl01");
		loader.load(recordsList);
		long t = System.currentTimeMillis();
		System.out.println("Loading completed in " + (t - t0)/1000.0 + " s");
		
//		ExperimentalRecords records = new ExperimentalRecords();
//		File failedFolder = new File("data/dev_qsar/exp_prop/loaded_with_failures");
//		File[] failedFiles = failedFolder.listFiles();
//		for (File file:failedFiles) {
//			String sourceName = file.getName();
//			if (!sourceName.contains(".")) {
//				ExperimentalRecords failedRecords = getPublicSourceFailedRecords(sourceName);
//				records.addAll(failedRecords);
//			}
//		}
		
//		ExperimentalRecords nameTooLong = new ExperimentalRecords();
//		ExperimentalRecords smilesTooLong = new ExperimentalRecords();
//		ExperimentalRecords other = new ExperimentalRecords();
//		for (ExperimentalRecord rec:records) {
//			boolean explained = false;
//			if (rec.chemical_name!=null && rec.chemical_name.length() > 255) {
//				nameTooLong.add(rec);
//				explained = true;
//			}
//			
//			if (rec.smiles!=null && rec.smiles.length() > 255) {
//				smilesTooLong.add(rec);
//				explained = true;
//			}
//			
//			if (!explained) {
//				other.add(rec);
//			}
//		}
//		
//		try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/dev_qsar/exp_prop/loaded_with_failures/name_too_long.json"))) {
//			writer.write(gson.toJson(nameTooLong));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/dev_qsar/exp_prop/loaded_with_failures/smiles_too_long.json"))) {
//			writer.write(gson.toJson(smilesTooLong));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/dev_qsar/exp_prop/loaded_with_failures/other_failures.json"))) {
//			writer.write(gson.toJson(other));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}