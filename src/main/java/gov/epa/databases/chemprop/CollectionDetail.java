package gov.epa.databases.chemprop;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;

public class CollectionDetail {
	public Long id;
	public String name;
	public String description;
	public String collection_type;
	public String created_by;
	public String updated_by;
	public String created_at;
	public String updated_at;
	
	private static transient Pattern websitePattern = Pattern.compile("[htf]{1,2}tps?://[A-Za-z0-9_\\-/\\.\\?=]+");
	
	public PublicSource asPublicSource(String createdBy) {
		PublicSource ps = new PublicSource();
		ps.setName(this.name);
		
		// Extract URL from description
		if (description!=null) {
			this.description = this.description.replaceAll("\r\n", "");
			if (this.description.length() > 255) {
				ps.setDescription(this.description.substring(0, 255));
			} else {
				ps.setDescription(this.description);
			}
			Matcher websiteMatcher = websitePattern.matcher(this.description);
			if (websiteMatcher.find()) {
				ps.setUrl(websiteMatcher.group());
			}
		}
		
		ps.setType(this.collection_type);
		ps.setCreatedBy(createdBy);
		return ps;
	}
	
	public static transient String tableName = "collection_details";
	
	public static HashMap<Long, CollectionDetail> getTableFromJsonFiles(String jsonFolderPath) {
		Gson gson = new Gson();
		HashMap<Long, CollectionDetail> hm = new HashMap<Long, CollectionDetail>();
		File jsonFolder = new File(jsonFolderPath);
		String[] jsonFileNames = jsonFolder.list();
		
		for (String fileName:jsonFileNames) {
			if (!fileName.startsWith(tableName) || !fileName.endsWith("json")) { continue; }
			
			String filePath = jsonFolderPath + File.separator + fileName;
			CollectionDetail[] table = null;
			try {
				JsonObject jo = gson.fromJson(new FileReader(filePath), JsonObject.class);
				if (!jo.has(tableName)) { continue; }
				
				String innerJson = gson.toJson(jo.get(tableName));
				table = gson.fromJson(innerJson, CollectionDetail[].class);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			if (table==null || table.length==0) { continue; }
			
			for (CollectionDetail t:table) {
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
	public static HashMap<Long, CollectionDetail> getTableFromJsonFiles2(String jsonFolderPath) {
		Gson gson = new Gson();
		HashMap<Long, CollectionDetail> hm = new HashMap<Long, CollectionDetail>();
		File jsonFolder = new File(jsonFolderPath);
		String[] jsonFileNames = jsonFolder.list();
		
		for (String fileName:jsonFileNames) {
			if (!fileName.startsWith(tableName) || !fileName.endsWith("json")) { continue; }
			
			String filePath = jsonFolderPath + File.separator + fileName;
			CollectionDetail[] table = null;
			try {
				table = gson.fromJson(new FileReader(filePath), CollectionDetail[].class);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			if (table==null || table.length==0) continue; 
			
			for (CollectionDetail t:table) {
				hm.put(t.id, t);
//				System.out.println(gson.toJson(t));
			}
		}
		
		System.out.println("Got " + hm.size() + " distinct records from " + tableName);
		return hm;
	}
}
