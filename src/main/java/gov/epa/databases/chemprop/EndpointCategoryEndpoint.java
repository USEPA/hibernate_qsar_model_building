package gov.epa.databases.chemprop;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class EndpointCategoryEndpoint {
	public Long id;
	public Long fk_endpoint_category_id;
	public Long fk_endpoint_id;
	public String created_by;
	public String updated_by;
	public String created_at;
	public String updated_at;
	
	public static transient String tableName = "endpoint_category_endpoints";
	
	public static HashMap<Long, EndpointCategoryEndpoint> getTableFromJsonFiles(String jsonFolderPath) {
		Gson gson = new Gson();
		HashMap<Long, EndpointCategoryEndpoint> hm = new HashMap<Long, EndpointCategoryEndpoint>();
		File jsonFolder = new File(jsonFolderPath);
		String[] jsonFileNames = jsonFolder.list();
		
		for (String fileName:jsonFileNames) {
			if (!fileName.startsWith(tableName) || !fileName.endsWith("json")) { continue; }
			
			String filePath = jsonFolderPath + File.separator + fileName;
			EndpointCategoryEndpoint[] table = null;
			try {
				JsonObject jo = gson.fromJson(new FileReader(filePath), JsonObject.class);
				if (!jo.has(tableName)) { continue; }
				
				String innerJson = gson.toJson(jo.get(tableName));
				table = gson.fromJson(innerJson, EndpointCategoryEndpoint[].class);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			if (table==null || table.length==0) { continue; }
			
			for (EndpointCategoryEndpoint t:table) {
				hm.put(t.id, t);
			}
		}
		
		System.out.println("Got " + hm.size() + " distinct records from " + tableName);
		return hm;
	}
}
