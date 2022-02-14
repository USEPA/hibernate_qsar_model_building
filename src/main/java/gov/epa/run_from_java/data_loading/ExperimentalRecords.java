package gov.epa.run_from_java.data_loading;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

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
	
}
