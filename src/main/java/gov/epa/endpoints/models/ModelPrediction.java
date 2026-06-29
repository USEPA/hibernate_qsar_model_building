package gov.epa.endpoints.models;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteValidation.CheckStructure;

public class ModelPrediction implements Comparable<ModelPrediction>{
	public String id;
	public Double exp;
	public Double pred;
	public Double weight;
	public Integer split;//T=0,P=1
	public String methodAbbrev;

	
//	public String dtxcid;
	public Boolean insideAD;
	public CheckStructure checkStructure;

	public String qsarClass;
	
	public ModelPrediction(String id, Double exp, Double pred, Integer split) {
		this.id = id;
		this.exp=exp;
		this.pred = pred;
		this.split=split;
	}
	
	
	public ModelPrediction(String id, Double exp, Double pred, Integer split,Boolean insideAD) {
		this.id = id;
		this.exp=exp;
		this.pred = pred;
		this.split=split;
		this.insideAD=insideAD;
	}

	public Double absError() {
		if(exp==null || pred==null)return null;
		return Math.abs(exp-pred);
	}
	
	
	@Override
	public int compareTo(ModelPrediction mp) {
		return -Double.compare(this.absError(), mp.absError());//puts largest errors first
	}
	
	
	
	public static List<ModelPrediction> getModelPredictions(String jsonFilePath) {
		
//		String jsonFilePath = "/Datasets/training_and_test_set_predictions.json"; // Adjust the path based on your JAR structure

        Type type = new TypeToken<List<ModelPrediction>>(){}.getType();

        try (InputStream inputStream = new FileInputStream(jsonFilePath);
	        
    		InputStreamReader reader = new InputStreamReader(inputStream)) {
            // Parse the JSON file into a Hashtable
            Gson gson = new Gson();
            List<ModelPrediction> htByCAS = gson.fromJson(reader, type);
            //endpointAbbrev...cas...methodAbbrev => prediction as string
            
            return htByCAS;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
		
        
    }

//	public ModelPrediction(String id, Double exp, Double pred) {
//		this.id = id;
//		this.exp = exp;
//		this.pred = pred;
//	}
	

}