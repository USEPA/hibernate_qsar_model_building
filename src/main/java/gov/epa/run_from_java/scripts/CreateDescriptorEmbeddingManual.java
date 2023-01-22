package gov.epa.run_from_java.scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.web_services.embedding_service.CalculationInfo;

public class CreateDescriptorEmbeddingManual {

	static Gson gson=new GsonBuilder().setPrettyPrinting().create();;
	
	
	
	static String getEmbedding(JsonArray ja,String set,String Property) {
		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();

			if(jo.get("set").getAsString().equals(set) && jo.get("Property").getAsString().equals(Property)) {
				String json=jo.get("embedding").getAsString();
				return convertEmbeddingJsonToTsv(jo);
			}
		}
		return null;
	}
	
	static void createEmbedding() {
		
		DescriptorEmbeddingServiceImpl desi=new DescriptorEmbeddingServiceImpl();
		
		String name="";
		
		String lanId="tmarti02";
		String descriptorSetName="WebTEST-default";
		String property="LogKoa";
		String set="OPERA";
		String datasetName=property+" "+set;
		
		CalculationInfo ci = new CalculationInfo();
		ci.num_generations = 100;
		ci.threshold=1;
		ci.remove_log_p = false;
		ci.qsarMethodGA = "knn";
		ci.datasetName=datasetName;
		ci.descriptorSetName=descriptorSetName;
		
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 python\\pf_python_modelbuilding\\embeddings\\";
		String embeddingPath=folder+descriptorSetName+"_ga_embeddings.csv";
		
		JsonArray ja=null; // Utilities.csvToGson(embeddingPath);
		// CR: have to have errorless project, this method wasn't pushed or something

		String embedding=getEmbedding(ja, set, property);//or just paste here
		
//		System.out.println(embedding);
		
				
		DescriptorEmbedding desE = new DescriptorEmbedding(ci, embedding,lanId);
		

		Date date = new Date();
		Timestamp timestamp2 = new Timestamp(date.getTime());
		desE.setCreatedAt(timestamp2);
		desE.setUpdatedAt(timestamp2);
		
		System.out.println(gson.toJson(desE));

		
//		desi.create(desE);
	}
	
	
	static void createEmbeddingsFromCSV() {
		DescriptorEmbeddingServiceImpl desi=new DescriptorEmbeddingServiceImpl();
		String lanId="tmarti02";
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 python\\pf_python_modelbuilding\\embeddings\\";
		String descriptorSetName="WebTEST-default";
		String embeddingPath=folder+descriptorSetName+"_ga_embeddings.csv";
		JsonArray ja= null; // Utilities.csvToGson(embeddingPath);
		// CR: have to have errorless project, this method wasn't pushed or something
		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
		
			String set=jo.get("set").getAsString();
			String property=jo.get("Property").getAsString();
			String datasetName=property+" "+set;
			String embedding = convertEmbeddingJsonToTsv(jo);
			
			CalculationInfo ci = new CalculationInfo();
			ci.num_generations = 100;
			ci.threshold=1;			
			ci.qsarMethodGA = "knn";
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			
			DescriptorEmbedding desE = new DescriptorEmbedding(ci, embedding, lanId);

			Date date = new Date();
			Timestamp timestamp2 = new Timestamp(date.getTime());
			desE.setCreatedAt(timestamp2);
			desE.setUpdatedAt(timestamp2);
			
			System.out.println(gson.toJson(desE));
			desi.create(desE);

		}
	}

	private static String convertEmbeddingJsonToTsv(JsonObject jo) {
		String json=jo.get("embedding").getAsString();
		ArrayList<String> list=gson.fromJson(json,ArrayList.class);
		String embedding="";
		for (int j=0;j<list.size();j++) {
			embedding+=list.get(j);
			if (j<list.size()-1) embedding +="\t";
		}
		return embedding;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

//		createEmbedding();
		createEmbeddingsFromCSV();
		
	}

}
