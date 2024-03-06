package gov.epa.databases.chemprop;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;

public class SourceDetail {
	public Long id;
	public String name;
	public String label;
	public String short_description;
	public String long_description;
	public Long pmid;
	public String authors;
	public String affiliation;
	public String title;
	public String url;
	public Long page;
	public String figure;
	public String access_date;
	public String qc_notes;
	public String created_by;
	public String updated_by;
	public String created_at;
	public String updated_at;
	
	public LiteratureSource asLiteratureSource(String createdBy) {
		LiteratureSource ls =  new LiteratureSource();
		ls.setName(this.name);
		
		String strYear=name.substring(name.lastIndexOf("_")+1,name.length());
		ls.setYear(strYear);
		
		String firstAuthor=name.substring(0,name.lastIndexOf("_"));
		
//		System.out.println(firstAuthor);
		String description=long_description.replace(firstAuthor, "").trim();
		if(description.substring(0,1).equals("_")) description =description.substring(1,description.length());
		
		ls.setTitle(this.title);
		ls.setAuthor(this.authors);
		ls.setCitation(authors+ " ("+ls.getYear()+"). "+description);
		ls.setUrl(this.url);
		ls.setNotes(this.qc_notes);
		ls.setCreatedBy(createdBy);
		return ls;
	}
	
	public static transient String tableName = "source_details";
	
	public static HashMap<Long, SourceDetail> getTableFromJsonFiles(String jsonFolderPath) {
		Gson gson = new Gson();
		HashMap<Long, SourceDetail> hm = new HashMap<Long, SourceDetail>();
		File jsonFolder = new File(jsonFolderPath);
		String[] jsonFileNames = jsonFolder.list();
		
		for (String fileName:jsonFileNames) {
			if (!fileName.startsWith(tableName) || !fileName.endsWith("json")) { continue; }
			
			String filePath = jsonFolderPath + File.separator + fileName;
			SourceDetail[] table = null;
			try {
				JsonObject jo = gson.fromJson(new FileReader(filePath), JsonObject.class);
				if (!jo.has(tableName)) { continue; }
				
				String innerJson = gson.toJson(jo.get(tableName));
				table = gson.fromJson(innerJson, SourceDetail[].class);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			if (table==null || table.length==0) { continue; }
			
			for (SourceDetail t:table) {
				hm.put(t.id, t);
			}
		}
		
		System.out.println("Got " + hm.size() + " distinct records from " + tableName);
		return hm;
	}
	
	public static HashMap<Long, SourceDetail> getTableFromJsonFiles2(String jsonFolderPath) {
		Gson gson = new Gson();
		HashMap<Long, SourceDetail> hm = new HashMap<Long, SourceDetail>();
		File jsonFolder = new File(jsonFolderPath);
		String[] jsonFileNames = jsonFolder.list();
		
		for (String fileName:jsonFileNames) {
			if (!fileName.startsWith(tableName) || !fileName.endsWith("json")) { continue; }
			
			String filePath = jsonFolderPath + File.separator + fileName;
			SourceDetail[] table = null;
			try {
				table = gson.fromJson(new FileReader(filePath), SourceDetail[].class);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			if (table==null || table.length==0) { continue; }
			
			for (SourceDetail t:table) {
				hm.put(t.id, t);
			}
		}
		
		System.out.println("Got " + hm.size() + " distinct records from " + tableName);
		return hm;
	}
}
