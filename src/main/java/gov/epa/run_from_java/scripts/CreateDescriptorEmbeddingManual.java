package gov.epa.run_from_java.scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.web_services.embedding_service.CalculationInfoGA;

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
		
		CalculationInfoGA ci = new CalculationInfoGA();
		ci.num_generations = 100;
		ci.threshold=1;
		ci.remove_log_p = false;
		ci.qsarMethodEmbedding = "knn";
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
			
			CalculationInfoGA ci = new CalculationInfoGA();
			ci.num_generations = 100;
			ci.threshold=1;			
			ci.qsarMethodEmbedding = "knn";
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
	
	public static void makeDescriptionJsonNotPrettyPrinted() {
		

		Gson gson=new Gson();//not pretty printed

		DescriptorEmbeddingServiceImpl es = new DescriptorEmbeddingServiceImpl();
		
		
		String sql="select id,description from qsar_models.descriptor_embeddings de ";
		
		Connection conn=SqlUtilities.getConnectionPostgres();
		ResultSet rs=DatabaseLookup.runSQL2(conn, sql);
		
		try {
			while (rs.next()) {
				int id=rs.getInt(1);
				String description=rs.getString(2);
				if (!description.contains("\n")) continue;

				CalculationInfoGA ci=gson.fromJson(description, CalculationInfoGA.class);
//				System.out.println(ci.toString());
				
				String sqlUpdate="UPDATE qsar_models.descriptor_embeddings\n"+ 
				"SET description='"+ci.toString()+"'\n"+
				"WHERE id="+id+";";
				
				System.out.println(sqlUpdate+"\n");
				DatabaseLookup.runSQL(conn, sqlUpdate);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	
	public static void testLoadEmbedding() {
		String endpoint = DevQsarConstants.LOG_HALF_LIFE;

		String sampleSource = "OPERA";

		CalculationInfoGA ci=new CalculationInfoGA();	
		ci.threshold=null;
		ci.qsarMethodEmbedding = DevQsarConstants.KNN;
		ci.datasetName = endpoint +" "+sampleSource;
		ci.descriptorSetName = "T.E.S.T. 5.1";		
		ci.splittingName="OPERA";
		ci.remove_log_p=false;
		

		DescriptorEmbeddingServiceImpl descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();
		DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);
		String descriptorEmbeddingName = descriptorEmbedding.getName();
		System.out.println(descriptorEmbeddingName);
		
		DescriptorEmbedding descriptorEmbedding2 = descriptorEmbeddingService.findByName(descriptorEmbeddingName);
		System.out.println(descriptorEmbedding2.getName());

	}

	
	static void createEmbeddingsFromJson() {
		DescriptorEmbeddingServiceImpl desi=new DescriptorEmbeddingServiceImpl();
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 python\\pf_python_modelbuilding\\datasets\\GA\\";
		
		String filename="PFAS_exp_prop_gen=10_opt=10_threshold=1_1674336604254.json";//T=all but PFAS, P=PFAS
//		String filename="exp_prop_gen=100_opt=10_threshold=1_1674349423275.json";//RND_REPRESENTATIVE
//		String filename="T=PFAS only, P=PFAS_gen=100_opt=10_threshold=1_1674409204305.json";
//		String filename="exp_prop_gen=100_opt=10_threshold=1_1674430512598.json";//100 gen RND_REPRESENTATIVE, LogP, remove_log_P=true
//		String filename="exp_prop_gen=10_opt=10_threshold=1_1674414440056.json";//10 gen RND_REPRESENTATIVE, LogP, remove_log_P=true
//		String filename="exp_prop_gen=10_opt=10_threshold=1_1674333465552.json";//10 gen RND_REPRESENTATIVE, MP
		
		
		String embeddingPath=folder+filename;
		
		Gson gson=new Gson();
		
		try {
			List<String> lines = Files.readAllLines(Paths.get(embeddingPath));
		
			String modelJson=lines.get(0);//knn info
//			System.out.println(modelJson);
			
			for (int i=1;i<lines.size();i++) {
				String line=lines.get(i);
//				System.out.println(line);
				
				
				DescriptorEmbedding deNew = Utilities.gson.fromJson(line, DescriptorEmbedding.class);

				CalculationInfoGA ci=gson.fromJson(deNew.getDescription(), CalculationInfoGA.class);				
				ci.datasetName=deNew.getDatasetName();
				ci.splittingName=deNew.getSplittingName();
				ci.descriptorSetName=deNew.getDescriptorSetName();
				ci.qsarMethodEmbedding=deNew.getQsarMethod();
				
				DescriptorEmbedding deDB=desi.findByGASettings(ci);

				if(deDB!=null) {
					System.out.println("already have "+deNew.getName()+" in DB");
					continue;
				}
				
//				System.out.println(deNew.getEmbeddingTsv());
				
//				String [] descriptors=deNew.getEmbeddingTsv().split("\t");
//				for (String descriptor:descriptors) {
//					System.out.println(descriptor);;
//				}
				
				deNew.setDescription(ci.toString());//make sure formatting is same				
				System.out.println(ci.toString());
				
//				System.out.println(deDB.getName());

//				if(true) continue;
				
				Date date = new Date();
				Timestamp timestamp2 = new Timestamp(date.getTime());
				deNew.setCreatedAt(timestamp2);
				deNew.setUpdatedAt(timestamp2);

//				System.out.println(desE.getDescription());
//				System.out.println(desE.getEmbeddingTsv());
				System.out.println(Utilities.gson.toJson(deNew));
				desi.create(deNew);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
//		createEmbeddingsFromCSV();
		createEmbeddingsFromJson();
//		testLoadEmbedding();
//		makeDescriptionJsonNotPrettyPrinted();
		
	}

}
