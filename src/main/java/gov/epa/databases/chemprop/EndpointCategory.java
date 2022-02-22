package gov.epa.databases.chemprop;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyCategory;

public class EndpointCategory {
	public Long id;
	public Long fk_endpoint_category_id_parent;
	public String abbreviation;
	public String name;
	public String description;
	public String created_by;
	public String updated_by;
	public String created_at;
	public String updated_at;
	
	public PropertyCategory asPropertyCategory(String createdBy) {
		PropertyCategory pc = new PropertyCategory();
		pc.setName(this.name);
		pc.setDescription(this.description);
		pc.setCreatedBy(createdBy);
		return pc;
	}
	
	public static transient String tableName = "endpoint_categories";
	
	public static HashMap<Long, EndpointCategory> getTableFromJsonFiles(String jsonFolderPath) {
		Gson gson = new Gson();
		HashMap<Long, EndpointCategory> hm = new HashMap<Long, EndpointCategory>();
		File jsonFolder = new File(jsonFolderPath);
		String[] jsonFileNames = jsonFolder.list();
		
		for (String fileName:jsonFileNames) {
			if (!fileName.startsWith(tableName) || !fileName.endsWith("json")) { continue; }
			
			String filePath = jsonFolderPath + File.separator + fileName;
			EndpointCategory[] table = null;
			try {
				JsonObject jo = gson.fromJson(new FileReader(filePath), JsonObject.class);
				if (!jo.has(tableName)) { continue; }
				
				String innerJson = gson.toJson(jo.get(tableName));
				table = gson.fromJson(innerJson, EndpointCategory[].class);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			if (table==null || table.length==0) { continue; }
			
			for (EndpointCategory t:table) {
				hm.put(t.id, t);
			}
		}
		
		System.out.println("Got " + hm.size() + " distinct records from " + tableName);
		return hm;
	}
}
