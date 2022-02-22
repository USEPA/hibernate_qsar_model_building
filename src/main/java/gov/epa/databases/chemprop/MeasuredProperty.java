package gov.epa.databases.chemprop;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;

public class MeasuredProperty {
	public Long id;
	public Long efk_dsstox_source_substance_id;
	public Long fk_endpoint_id;
	public Long fk_parameter_set_id;
	public Long fk_measurement_method_id;
	public Long fk_source_detail_id;
	public Double result_value;
	public Double result_min;
	public Double result_max;
	public Double result_error;
	public String result_text;
	public String result_concerns;
	public String created_by;
	public String updated_by;
	public String created_at;
	public String updated_at;
	
	public PropertyValue asPropertyValue(String createdBy) {
		PropertyValue pv = new PropertyValue();
		pv.setValuePointEstimate(this.result_value);
		pv.setValueMin(this.result_min);
		pv.setValueMax(this.result_max);
		pv.setValueError(this.result_error);
		pv.setValueText(this.result_text);
		pv.setKeep(true);
		pv.setQcFlag(false);
		pv.setNotes(this.result_concerns);
		pv.setCreatedBy(createdBy);
		return pv;
	}
	
	public String generateDtxrid() {
		Long temp = this.efk_dsstox_source_substance_id;
		List<Integer> digits = new ArrayList<Integer>();
		while (temp > 0) {
			digits.add((int) (temp % 10));
			temp = temp / 10;
		}
		Collections.reverse(digits);
		
		int sum = 0;
		for (int i = 0; i < digits.size(); i++) {
			sum += ((i + 1) * digits.get(i));
		}
		
		int checksum = sum % 10;
		String dtxrid = "DTXRID" + checksum + "0" + this.efk_dsstox_source_substance_id;
		return dtxrid;
	}
	
	public static transient String tableName = "measured_properties";
	
	public static HashMap<Long, MeasuredProperty> getTableFromJsonFiles(String jsonFolderPath) {
		Gson gson = new Gson();
		HashMap<Long, MeasuredProperty> hm = new HashMap<Long, MeasuredProperty>();
		File jsonFolder = new File(jsonFolderPath);
		String[] jsonFileNames = jsonFolder.list();
		
		for (String fileName:jsonFileNames) {
			if (!fileName.startsWith(tableName) || !fileName.endsWith("json")) { continue; }
			
			String filePath = jsonFolderPath + File.separator + fileName;
			MeasuredProperty[] table = null;
			try {
				JsonObject jo = gson.fromJson(new FileReader(filePath), JsonObject.class);
				if (!jo.has(tableName)) { continue; }
				
				String innerJson = gson.toJson(jo.get(tableName));
				table = gson.fromJson(innerJson, MeasuredProperty[].class);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (table==null || table.length==0) { continue; }
			
			for (MeasuredProperty t:table) {
				hm.put(t.id, t);
			}
		}
		
		System.out.println("Got " + hm.size() + " distinct records from " + tableName);
		return hm;
	}
}
