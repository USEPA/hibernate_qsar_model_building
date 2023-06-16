package gov.epa.run_from_java.scripts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.CDL;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

/**
* @author TMARTI02
*/
public class CreateStatisticsScript {



	//Goes through a text file in the resources folder to add the statistics and matching descriptions
	void addStats() {
		try {
			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("statistics.csv");
			String csvAsString = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
			String json = CDL.toJSONArray(csvAsString).toString();

			inputStream.close();
			
			JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);
//			System.out.println("Number of records in csv:"+ja.size());

			List<Statistic>statistics=new ArrayList<>();
			
			String lanId="tmarti02";

			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();

				String name=jo.get("name").getAsString();
				String description=jo.get("description").getAsString();
				boolean isBinary=jo.get("isBinary").getAsBoolean();
				
				Statistic statistic=new Statistic(name,description,isBinary,lanId);
			
				statistics.add(statistic);
				System.out.println(name+"\t"+description+"\t"+isBinary);
			}

			StatisticServiceImpl ssi=new StatisticServiceImpl();
			ssi.createBatchSQL(statistics);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	public static void main(String[] args) {
	
		CreateStatisticsScript c=new CreateStatisticsScript();
		c.addStats();

	}
	
	
}
