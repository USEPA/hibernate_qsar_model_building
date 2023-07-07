package gov.epa.run_from_java.scripts.PredictionDashboard.valery;

import java.util.ArrayList;

import com.google.gson.Gson;

import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.run_from_java.scripts.PredictionDashboard.valery.SDE_Prediction_Response.Prediction;

/**
* @author TMARTI02
*/
public class SDE_Prediction_Response {

    public Dataset dataset;
    public ArrayList<Prediction> predictions;

	
	public class Chemical{
	    public String inchi_key;
	    public String smiles;
	}

	public class Dataset{
	    public boolean binary;
	    public String dataset_description;
	    public int dataset_id;
	    public String dataset_name;
	    public Object mass_unit;
	    public ArrayList<Method> methods;
	    public String property_description;
	    public int property_id;
	    public String property_name;
	    public String unit;
	}

	public class Method{
	    public boolean binary;
	    public int model_id;
	    public String name;
	    public String type;
	    public String version;
	}

	public class Prediction{
	    public Chemical chemical;
	    public Values values;
	}


	public class Values{
	    public double avg;
	    public double exp;
	    public double rf;
	    public double xgb;
	    public double svm;
	    public double knn;
	    
	    public double avg_mass;
	    public double exp_mass;
	    public double rf_mass;
	    public double xgb_mass;
	    public double svm_mass;
	    public double knn_mass;

	}

	public static  ModelPrediction[] toModelPredictions(String predictResponse,String methodAbbrev) {
		Gson gson=new Gson();

		SDE_Prediction_Response[] predResponse= gson.fromJson(predictResponse, SDE_Prediction_Response[].class);

		ArrayList<Prediction>predictions=predResponse[0].predictions;
		
		ModelPrediction[]modelPredictions=new ModelPrediction[predictions.size()];
		
		System.out.println("predictions.size()="+predictions.size());
		
		int counter=0;
		for (Prediction prediction:predictions) {
			String smiles=prediction.chemical.smiles;
			Double exp=prediction.values.exp;
			
			Double pred=null;
			
			if (methodAbbrev.contains("consensus")) {
				pred=prediction.values.avg;//TODO use reflection instead of if statement	
			} else if (methodAbbrev.contains("rf")) {
				pred=prediction.values.rf;
			} else if (methodAbbrev.contains("xgb")) {
				pred=prediction.values.xgb;
			} else if (methodAbbrev.contains("svm")) {
				pred=prediction.values.svm;
			} else if (methodAbbrev.contains("knn")) {
				pred=prediction.values.knn;
			}
			
			
			ModelPrediction mp=new ModelPrediction(smiles, exp, pred, 1);
			modelPredictions[counter++]=mp;
			
//			System.out.println(mp.id+"\t"+mp.exp+"\t"+mp.pred);
		}
		return modelPredictions;
	}


}
