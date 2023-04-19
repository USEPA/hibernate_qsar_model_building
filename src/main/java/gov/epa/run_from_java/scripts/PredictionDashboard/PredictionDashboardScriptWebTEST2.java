package gov.epa.run_from_java.scripts.PredictionDashboard;


import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;


/**
* @author TMARTI02
*/
public class PredictionDashboardScriptWebTEST2 {

	PredictionDashboardServiceImpl predictionDashboardService=new PredictionDashboardServiceImpl();
	
	void storeAllResultsJsonToDatabase(String filepathJson) {
		
		Type listOfMyClassObject = new TypeToken<Hashtable<String,List<PredictionResultsWebTest2>>>() {}.getType();//TODO Change Object to actual class you want to use

		try {
			List<PredictionResultsWebTest2>resultsAll=Utilities.gson.fromJson(new FileReader(filepathJson), listOfMyClassObject);
			
			//TODO add hack method to switch the model ids to ones exist in database for that dataset
			
			//TODO Hashmap of models appearing in resultsAll, key=modelId
			HashMap<Long, Model> hmModels=createModelsMap(resultsAll);
			
			for (Object pr:resultsAll) {
				PredictionDashboard pd=convertPredictionResultsToPredictionDashboard(pr,hmModels);
				predictionDashboardService.create(pd);
//				System.out.println(Utilities.gson.toJson(pd));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}

	/**
	 * Map models by id number
	 * 
	 * @param resultsAll
	 * @return
	 */
	private HashMap<Long, Model> createModelsMap(List<PredictionResultsWebTest2> resultsAll) {
		HashMap<Long, Model> hmModels=new HashMap<>();
		return hmModels;
	}

	private PredictionDashboard convertPredictionResultsToPredictionDashboard(Object pr,
			HashMap<Long, Model> hmModels) {
		PredictionDashboard pd=new PredictionDashboard();
		
		Model model=new Model();
		Dataset dataset=new Dataset();
		
		//Converting units:
		if(dataset.getUnit().getName().equals(DevQsarConstants.NEG_LOG_M)) {
			if(dataset.getUnitContributor().getName().equals(DevQsarConstants.MOLAR)) {
				pd.setPredictionValue(Math.pow(10.0,-Double.parseDouble(pr.getPredToxValue())));
			}
		}
		
		
		
		
		return pd;
	}

	public static void main(String[] args) {
		
		PredictionDashboardScriptWebTEST2 p=new PredictionDashboardScriptWebTEST2();
		
		String fileNameJson="snapshot_compounds1.json";
		String strOutputFolder="reports/prediction_json";
		
		String destJsonPath=strOutputFolder+File.separator+fileNameJson;
		
		p.storeAllResultsJsonToDatabase(destJsonPath);
		

	}

}
