package gov.epa.run_from_java.data_loading;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import com.google.gson.Gson;

import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;


public class ExperimentalRecords extends ArrayList<ExperimentalRecord> {

	private static final long serialVersionUID = 5849588897944323620L;

	
	public void printPropertiesInExperimentalRecords() {
		//Following gets list of unique units in experimental records:
		List<String>properties=new ArrayList<String>();
		for (ExperimentalRecord er:this) {
			//			System.out.println(er.property_value_units_final);
			if (!properties.contains(er.property_name)) properties.add(er.property_name);
		}
		for (String property:properties) {
			System.out.println(property);
		}
	
	}
	
	public void writeRecordsToFile(String filePath) {
		
		File failedFile = new File(filePath);
		if (failedFile.getParentFile()!=null) { failedFile.getParentFile().mkdirs(); }
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
			bw.write(Utilities.gson.toJson(this));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static ExperimentalRecords getExperimentalRecords(String propertyName, String folderPath,String subString) {
		
		ExperimentalRecords records=new ExperimentalRecords();

		File folder=new File(folderPath);

		Gson gson=new Gson();
		
		for(File file: folder.listFiles()) {
			
			if(!file.getName().contains(".json"))continue;
			if(file.getName().contains("Original Records"))continue;
			if(file.getName().contains("-Bad")) continue;
			if(!file.getName().contains(subString)) continue;

			ExperimentalRecords recordsi=ExperimentalRecords.loadFromJson(file.getAbsolutePath(), gson);

			for(ExperimentalRecord er:recordsi) {		
				//					if(er.property_value_units_final==null) continue;//skip it because cant add qualitative property strings to datapoints yet
				if (propertyName==null || er.property_name.equals(propertyName)) records.add(er);
			}
			System.out.println(file.getName()+"\t"+recordsi.size());
		}

		return records;
	}
	
	public void printUniqueUnitsListInExperimentalRecords() {
		//Following gets list of unique units in experimental records:
		List<String>unitsList=new ArrayList<String>();

		System.out.println("\nUnique units:");
		for (ExperimentalRecord er:this) {
			//			System.out.println(er.property_value_units_final);
			String value=er.property_name+"\t"+er.property_value_units_final;
			if (!unitsList.contains(value)) unitsList.add(value);
		}
		Collections.sort(unitsList);
		for (String units:unitsList) System.out.println(units);

	}
	public static ExperimentalRecords loadFromJson(String jsonFilePath, Gson gson) {
		try {
			File file = new File(jsonFilePath);

			if (!file.exists()) {
				return null;
			}

			ExperimentalRecords chemicals = gson.fromJson(new FileReader(jsonFilePath), ExperimentalRecords.class);			
			return chemicals;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public static ExperimentalRecords loadFromJson(String jsonFilePath) {
		try {
			File file = new File(jsonFilePath);

			if (!file.exists()) {
				return null;
			}

			Gson gson=new Gson();
			ExperimentalRecords chemicals = gson.fromJson(new FileReader(jsonFilePath), ExperimentalRecords.class);			
			return chemicals;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	
	
	public static ExperimentalRecords getExperimentalRecords(String sourceName, String subfolder) {
		
		String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\";
		
		String folder=mainFolder+sourceName+"\\";
		if(subfolder!=null) folder+=subfolder+"\\";
		
		File Folder=new File(folder);

		System.out.println(folder+"\t"+Folder.exists());
		
		ExperimentalRecords records=new ExperimentalRecords();
		
		//TODO need to flag case where we accidentally have both numbered and non numbered jsons due to multiple parse runs with diff code
		
		for(File file:Folder.listFiles()) {
			if(!file.getName().contains(".json")) continue;
			if(file.getName().contains("Bad")) continue;
			if(file.getName().contains("Original Records")) continue;
			ExperimentalRecords experimentalRecords=ExperimentalRecords.loadFromJson(file.getAbsolutePath());
			System.out.println(file.getName()+"\t"+subfolder+"\t"+experimentalRecords.size());
			records.addAll(experimentalRecords);
		}
		
		
		return records;
	}


	public void getRecordsByProperty() {
		TreeMap <String,ExperimentalRecords>map=new TreeMap<String,ExperimentalRecords>();

		for (ExperimentalRecord er:this) {
			if(!er.keep) continue;
			if(map.get(er.property_name)==null) {
				ExperimentalRecords recs=new ExperimentalRecords();
				recs.add(er);
				map.put(er.property_name, recs);
			} else {
				ExperimentalRecords recs=map.get(er.property_name);
				recs.add(er);
			}
		}
		System.out.println("\nKept records:");
		for(String property:map.keySet()) {
			System.out.println(property+"\t"+map.get(property).size());
		}
		System.out.println("");
	}
	
}
