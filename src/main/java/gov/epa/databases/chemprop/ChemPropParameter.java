package gov.epa.databases.chemprop;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.exp_prop.entity.Parameter;

public class ChemPropParameter {
	public Long id;
	public String name;
	public String description;
	public String standard_unit;
	public String created_by;
	public String updated_by;
	public String created_at;
	public String updated_at;
	
	public Parameter asParameter(String createdBy) {
		Parameter p = new Parameter();
		p.setName(this.name);
		p.setDescription(this.description);
		p.setCreatedBy(createdBy);
		return p;
	}
	
	public static transient String tableName = "parameters";
	
	public static HashMap<Long, ChemPropParameter> getTableFromJsonFiles(String jsonFolderPath) {
		Gson gson = new Gson();
		HashMap<Long, ChemPropParameter> hm = new HashMap<Long, ChemPropParameter>();
		File jsonFolder = new File(jsonFolderPath);
		String[] jsonFileNames = jsonFolder.list();
		
		for (String fileName:jsonFileNames) {
			if (!fileName.startsWith(tableName) || !fileName.endsWith("json")) { continue; }
			
			String filePath = jsonFolderPath + File.separator + fileName;
			ChemPropParameter[] table = null;
			try {
				JsonObject jo = gson.fromJson(new FileReader(filePath), JsonObject.class);
				if (!jo.has(tableName)) { continue; }
				
				String innerJson = gson.toJson(jo.get(tableName));
				table = gson.fromJson(innerJson, ChemPropParameter[].class);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			if (table==null || table.length==0) { continue; }
			
			for (ChemPropParameter t:table) {
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
	public static HashMap<Long, ChemPropParameter> getTableFromJsonFiles2(String jsonFolderPath) {
		Gson gson = new Gson();
		HashMap<Long, ChemPropParameter> hm = new HashMap<Long, ChemPropParameter>();
		File jsonFolder = new File(jsonFolderPath);
		String[] jsonFileNames = jsonFolder.list();
		
		for (String fileName:jsonFileNames) {
			if (!fileName.startsWith(tableName) || !fileName.endsWith("json")) { continue; }
			
			String filePath = jsonFolderPath + File.separator + fileName;
			ChemPropParameter[] table = null;
			try {
				table = gson.fromJson(new FileReader(filePath), ChemPropParameter[].class);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			if (table==null || table.length==0) { continue; }
			
			for (ChemPropParameter t:table) {
				hm.put(t.id, t);
			}
		}
		
		System.out.println("Got " + hm.size() + " distinct records from " + tableName);
		return hm;
	}
}
