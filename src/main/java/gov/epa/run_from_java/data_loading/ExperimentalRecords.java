package gov.epa.run_from_java.data_loading;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.TreeMap;
import com.google.gson.Gson;


public class ExperimentalRecords extends ArrayList<ExperimentalRecord> {

	private static final long serialVersionUID = 5849588897944323620L;

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
