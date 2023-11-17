package gov.epa.databases.chemprop;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;

public class Endpoint {
	public Long id;
	public String abbreviation;
	public String name;
	public String description;
	public String standard_unit;
	public String created_by;
	public String updated_by;
	public String created_at;
	public String updated_at;
	
	public ExpPropProperty asProperty(String createdBy) {
		ExpPropProperty p = new ExpPropProperty();
		p.setName(this.name);
		if (this.description!=null && this.description.length() > 255) { 
			p.setDescription(this.description.substring(0,255));
		} else {
			p.setDescription(this.description);
		}
		p.setCreatedBy(createdBy);
		return p;
	}
	
	public static transient String tableName = "endpoints";
	
	public static HashMap<Long, Endpoint> getTableFromJsonFiles(String jsonFolderPath) {
		Gson gson = new Gson();
		HashMap<Long, Endpoint> hm = new HashMap<Long, Endpoint>();
		File jsonFolder = new File(jsonFolderPath);
		String[] jsonFileNames = jsonFolder.list();
		
		for (String fileName:jsonFileNames) {
			if (!fileName.startsWith(tableName) || !fileName.endsWith("json")) { continue; }
			
			String filePath = jsonFolderPath + File.separator + fileName;
			Endpoint[] table = null;
			try {
				JsonObject jo = gson.fromJson(new FileReader(filePath), JsonObject.class);
				if (!jo.has(tableName)) { continue; }
				
				String innerJson = gson.toJson(jo.get(tableName));
				table = gson.fromJson(innerJson, Endpoint[].class);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			if (table==null || table.length==0) { continue; }
			
			for (Endpoint t:table) {
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
	public static HashMap<Long, Endpoint> getTableFromJsonFiles2(String jsonFolderPath) {
		Gson gson = new Gson();
		HashMap<Long, Endpoint> hm = new HashMap<Long, Endpoint>();
		File jsonFolder = new File(jsonFolderPath);
		String[] jsonFileNames = jsonFolder.list();
		
		for (String fileName:jsonFileNames) {
			if (!fileName.startsWith(tableName) || !fileName.endsWith("json")) { continue; }
			
			String filePath = jsonFolderPath + File.separator + fileName;
			Endpoint[] table = null;
			try {
				table = gson.fromJson(new FileReader(filePath), Endpoint[].class);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			if (table==null || table.length==0) { continue; }
			
			for (Endpoint t:table) {
				hm.put(t.id, t);
			}
		}
		
		System.out.println("Got " + hm.size() + " distinct records from " + tableName);
		return hm;
	}

}
