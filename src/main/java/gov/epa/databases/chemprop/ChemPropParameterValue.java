package gov.epa.databases.chemprop;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue;

public class ChemPropParameterValue {
	public Long id;
	public Long fk_parameter_set_id;
	public Long fk_parameter_id;
	public Double value;
	public String value_text;
	public String confidence;
	public String created_by;
	public String updated_by;
	public String created_at;
	public String updated_at;
	
	public ParameterValue asParameterValue(String createdBy) {
		ParameterValue pv = new ParameterValue();
		pv.setValuePointEstimate(this.value);
		pv.setValueText(this.value_text);
		pv.setCreatedBy(createdBy);
		return pv;
	}
	
	public static transient String tableName = "parameter_values";
	
	public static HashMap<Long, ChemPropParameterValue> getTableFromJsonFiles(String jsonFolderPath) {
		Gson gson = new Gson();
		HashMap<Long, ChemPropParameterValue> hm = new HashMap<Long, ChemPropParameterValue>();
		File jsonFolder = new File(jsonFolderPath);
		String[] jsonFileNames = jsonFolder.list();
		
		for (String fileName:jsonFileNames) {
			if (!fileName.startsWith(tableName) || !fileName.endsWith("json")) { continue; }
			
			String filePath = jsonFolderPath + File.separator + fileName;
			ChemPropParameterValue[] table = null;
			try {
				JsonObject jo = gson.fromJson(new FileReader(filePath), JsonObject.class);
				if (!jo.has(tableName)) { continue; }
				
				String innerJson = gson.toJson(jo.get(tableName));
				table = gson.fromJson(innerJson, ChemPropParameterValue[].class);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			if (table==null || table.length==0) { continue; }
			
			for (ChemPropParameterValue t:table) {
				hm.put(t.id, t);
			}
		}
		
		System.out.println("Got " + hm.size() + " distinct records from " + tableName);
		return hm;
	}
	
	/**
	 * This method uses json files created from DataGrip which outputs a json array
	 * 
	 * @param jsonFolderPath
	 * @return
	 */
	public static HashMap<Long, ChemPropParameterValue> getTableFromJsonFiles2(String jsonFolderPath) {
		Gson gson = new Gson();
		HashMap<Long, ChemPropParameterValue> hm = new HashMap<Long, ChemPropParameterValue>();
		File jsonFolder = new File(jsonFolderPath);
		String[] jsonFileNames = jsonFolder.list();
		
		for (String fileName:jsonFileNames) {
			if (!fileName.startsWith(tableName) || !fileName.endsWith("json")) { continue; }
			
			String filePath = jsonFolderPath + File.separator + fileName;
			ChemPropParameterValue[] table = null;
			try {
				table = gson.fromJson(new FileReader(filePath), ChemPropParameterValue[].class);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			if (table==null || table.length==0) { continue; }
			
			for (ChemPropParameterValue t:table) {
				hm.put(t.id, t);
			}
		}
		
		System.out.println("Got " + hm.size() + " distinct records from " + tableName);
		return hm;
	}
}
