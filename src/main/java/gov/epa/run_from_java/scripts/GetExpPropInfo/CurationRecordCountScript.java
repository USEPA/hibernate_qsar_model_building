package gov.epa.run_from_java.scripts.GetExpPropInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.endpoints.reports.predictions.ExcelReports.ExcelPredictionReportGenerator;
import gov.epa.run_from_java.scripts.SplittingGeneratorPFAS_Script;

/**
* @author TMARTI02
*/
public class CurationRecordCountScript {

	void go() {

		List<String>datasetNames=new ArrayList<>();

//		datasetNames.add("HLC v1 modeling");
		datasetNames.add("WS v1 modeling");
		datasetNames.add("VP v1 modeling");
		datasetNames.add("BP v1 modeling");
//		datasetNames.add("LogP v1 modeling");
		datasetNames.add("MP v1 modeling");
		
//		datasetNames.add("HLC from_exp_prop_and_chemprop");
////		datasetNames.add("WS from_exp_prop_and_chemprop");
//		datasetNames.add("VP from_exp_prop_and_chemprop");
//		datasetNames.add("BP from_exp_prop_and_chemprop");
//		datasetNames.add("LogP from_exp_prop_and_chemprop");
//		datasetNames.add("MP from_exp_prop_and_chemprop");

		

		String listName = "PFASSTRUCTV4";
		String folder = "data/dev_qsar/dataset_files/";
		String filePathPFAS = folder + listName + "_qsar_ready_smiles.txt";
		HashSet<String> smilesArray = SplittingGeneratorPFAS_Script.getPFASSmiles(filePathPFAS);

		int countCurate=0;
		
		for (String datasetName:datasetNames) {
			String jsonPath=ExcelPredictionReportGenerator.getMappedJsonPath(datasetName);
			
			JsonArray ja=Utilities.getJsonArrayFromJsonFile(jsonPath);
			
			Hashtable<String,List<Double>>ht=new Hashtable<>();
			
			for (int i=0;i<ja.size();i++) {
			
				JsonObject jo=ja.get(i).getAsJsonObject();
				String smiles=jo.get("canon_qsar_smiles").getAsString();
				
				if(!smilesArray.contains(smiles)) continue;
				
				if(ht.get(smiles)==null) {
					List<Double> qsarValues=new ArrayList<>();
					qsarValues.add(jo.get("qsar_property_value").getAsDouble());
					ht.put(smiles, qsarValues);
				} else {
					List<Double> qsarValues=ht.get(smiles);
					qsarValues.add(jo.get("qsar_property_value").getAsDouble());
				}
//				System.out.println(Utilities.gson.toJson(jo));
				
			}
			
			double tol=0.001;
			
			int countDiff=0;
			
			for (String smiles:ht.keySet()) {
				
				List<Double> qsarValues=ht.get(smiles);
				
				Collections.sort(qsarValues);
				
				countDiff++;
				
				for (int i=1;i<qsarValues.size();i++) {
					if(Math.abs(qsarValues.get(i)-qsarValues.get(i-1))>tol) countDiff++;
				}
				
			}
			
			countCurate+=countDiff;
			
			System.out.println(datasetName+"\t"+ht.size()+"\t"+countDiff);
		}
		
		System.out.println(countCurate);

		
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		CurationRecordCountScript c=new CurationRecordCountScript();
		c.go();
		
		//Output:
//		HLC v1 modeling	32	42
//		WS v1 modeling	81	50
//		VP v1 modeling	101	176
//		BP v1 modeling	261	142
//		LogP v1 modeling	55	63
//		MP v1 modeling	201	202
//		675


	}

}
