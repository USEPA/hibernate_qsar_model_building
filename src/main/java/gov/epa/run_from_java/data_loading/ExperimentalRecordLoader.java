package gov.epa.run_from_java.data_loading;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;
import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.Parameter;
import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropPropertyService;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropPropertyServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropUnitService;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropUnitServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.LiteratureSourceService;
import gov.epa.databases.dev_qsar.exp_prop.service.LiteratureSourceServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.ParameterService;
import gov.epa.databases.dev_qsar.exp_prop.service.ParameterServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.PublicSourceService;
import gov.epa.databases.dev_qsar.exp_prop.service.PublicSourceServiceImpl;

public class ExperimentalRecordLoader {
	
	static final Pattern STRING_COLUMN_PATTERN = Pattern.compile("([~><=]{1,2})?(-?[0-9\\.]+)([-~])?(-?[0-9\\.]+)?");
	
	private ParameterService parameterService = new ParameterServiceImpl();
	private ExpPropPropertyService expPropPropertyService = new ExpPropPropertyServiceImpl();
	private ExpPropUnitService expPropUnitService = new ExpPropUnitServiceImpl();
	LiteratureSourceService literatureSourceService = new LiteratureSourceServiceImpl();
	private PublicSourceService publicSourceService = new PublicSourceServiceImpl();
	
	Map<String, LiteratureSource> literatureSourcesMap = new HashMap<String, LiteratureSource>();
	Map<String, Parameter> parametersMap = new HashMap<String, Parameter>();
	Map<String, ExpPropProperty> propertiesMap = new HashMap<String, ExpPropProperty>();
	Map<String, PublicSource> publicSourcesMap = new HashMap<String, PublicSource>();
	Map<String, ExpPropUnit> unitsMap = new HashMap<String, ExpPropUnit>();
	
	String lanId;
	
	private static Gson gson =  new GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.serializeSpecialFloatingPointValues()
			.create();
	
	public ExperimentalRecordLoader(String lanId) {
		this.lanId = lanId;
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
	
	private static ExperimentalRecords getPublicSourceRecords(String publicSourceName, String type) {
		ExperimentalRecords records = new ExperimentalRecords();
		String publicSourceFolderPath = "C:\\Users\\CRAMSLAN\\OneDrive - Environmental Protection Agency (EPA)\\VDI_Repo\\java\\github\\ghs-data-gathering\\data\\experimental" + "/" + publicSourceName;
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
			} else if (((fileName.startsWith(trimmedPublicSourceName + " Experimental Records")
					&& type.contains("physchem"))
					|| (fileName.startsWith(trimmedPublicSourceName + " Toxicity Experimental Records"))
					&& type.contains("tox"))
					&& !fileName.contains("Failed")) {
				records.addAll(ExperimentalRecords.loadFromJson(file.getAbsolutePath(), gson));
			}
		}
		
		return records;
	}
	
	public void load(List<ExperimentalRecord> records, String type, boolean createLiteratureSources) {
		List<ExperimentalRecord> failedRecords = new ArrayList<ExperimentalRecord>();
		int countSuccess = 0;
		int countFailure = 0;
		int countTotal = 0;
		for (ExperimentalRecord rec:records) {
			try {
				boolean success = false;
				if (type.contains("physchem")) {
					PhyschemExpPropData expPropData = new PhyschemExpPropData(this);
					expPropData.getValues(rec);
					expPropData.constructPropertyValue(createLiteratureSources);
					success = expPropData.post();
				} else if (type.contains("tox")) {
					ToxExpPropData expPropData = new ToxExpPropData(this);
					expPropData.getValues(rec);
					expPropData.constructPropertyValue(createLiteratureSources);
					success = expPropData.post();
				}
				
				if (success) {
					countSuccess++;
				} else {
					failedRecords.add(rec);
//					logger.warn(rec.id_physchem + ": Loading failed");
					countFailure++;
				}
			} catch (Exception e) {
				failedRecords.add(rec);
//				logger.warn(rec.id_physchem + ": Loading failed with exception: " + e.getMessage());
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
			String failedFilePath = "data/dev_qsar/exp_prop/" + type + "/"
					+ publicSourceName + "/" + publicSourceName + " Experimental Records-Failed.json";
			
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
	
	public static void main(String[] args) {
		System.out.println("eclipse recognizes new code4");
		String sourceName = "Burkhard_BCF";
		String type = "physchem";
		
		System.out.println("Retrieving records...");
		ExperimentalRecords records = getPublicSourceRecords(sourceName, type);
		System.out.println("Retrieved " + records.size() + " records");
		
		System.out.println("Loading " + records.size() + " records...");
		long t0 = System.currentTimeMillis();
		ExperimentalRecordLoader loader = new ExperimentalRecordLoader("cramslan");
		loader.load(records, type, true);
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