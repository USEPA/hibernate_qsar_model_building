package gov.epa.run_from_java.scripts;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.run_from_java.scripts.GetExpPropInfo.ExcelCreator;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

public class LookAtPredictionsBySourceScript {
	
	
	public static void main(String[] args) {
		
		String modelSetName="WebTEST2.0";
//		String datasetName="HLC from exp_prop and chemprop";
//		String datasetName="VP from exp_prop and chemprop";
//		String datasetName="WS from exp_prop and chemprop v2";
//		String datasetName="LogP from exp_prop and chemprop";
//		String datasetName="WS from exp_prop and chemprop";
		
//		String datasetName="MP from exp_prop and chemprop_2";
		String datasetName="BP from exp_prop and chemprop v3";
//		String datasetName="MP from exp_prop and chemprop v2";

//		String datasetName="BP from exp_prop and chemprop";
		
		String splitting=DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
		String excelFileName=datasetName+"_"+splitting+".xlsx";
//		String excelFileName=datasetName+"_"+splitting+"_PFAS.xlsx";
		
		String filePath="data\\reports\\prediction reports upload\\"+modelSetName+"\\"+excelFileName;
		
		
		JsonArray jaConsensus=ExcelCreator.convertExcelToJsonArray(filePath, 0, "consensus_regressor");
		JsonArray jaRecords=ExcelCreator.convertExcelToJsonArray(filePath, 0, "Records");
		
		Hashtable<String,JsonObject>htConsensus=new Hashtable<>();
		Hashtable<String,JsonArray>htRecords=new Hashtable<>();
		
		for (int i=0;i<jaConsensus.size();i++) {
			JsonObject jo=jaConsensus.get(i).getAsJsonObject();
			String exp_prop_id=jo.get("exp_prop_id").getAsString();
//			System.out.println(exp_prop_id);
			if (exp_prop_id.contains("|")) {
				String [] ids=exp_prop_id.split("\\|");
				htConsensus.put(ids[0],jo);
				htConsensus.put(ids[1],jo);
			} else {
				htConsensus.put(exp_prop_id,jo);	
			}
		}

		
		List<String>sources=new ArrayList<>();
		
		for (int i=0;i<jaRecords.size();i++) {
			JsonObject jo=jaRecords.get(i).getAsJsonObject();
			String source_name=jo.get("source_name").getAsString();
			
			if (!sources.contains(source_name)) sources.add(source_name);

			if (htRecords.get(source_name)==null) {
				JsonArray ja=new JsonArray();
				ja.add(jo);
				htRecords.put(source_name,ja);
				
			} else {
				JsonArray ja=htRecords.get(source_name);
				ja.add(jo);
			}
//			System.out.println(exp_prop_id);
		}
		
		List<Double>errorsAllSources=getOverallErrors(jaConsensus);
		double MAE_Overall=calculateMean(errorsAllSources);
		double StDev_Overall=calculateStandardDeviation(errorsAllSources);
		
		System.out.println(datasetName);
		
		DecimalFormat df=new DecimalFormat("0.000");
		System.out.println("Overall MAE\t"+df.format(MAE_Overall));
		System.out.println("Overall StdDev\t"+df.format(StDev_Overall));
		System.out.println("Overallcount\t"+errorsAllSources.size()+"\n");
		
		
		for (String source:sources) {

			List<Double>errorsSource=getSourceErrors(htConsensus, htRecords, source);

			if (errorsSource.size()==0) continue;
			
			double MAE_Source=calculateMean(errorsSource);

			System.out.println(source+"\t"+df.format(MAE_Source)+"\t"+errorsSource.size());
		}
		
		
	}

	private static List<Double> getSourceErrors(Hashtable<String, JsonObject> htConsensus,
			Hashtable<String, JsonArray> htRecords, String source_name) {
		JsonArray jaRecordsSource=htRecords.get(source_name);
		
		Hashtable<String,ModelPrediction>htConsensusSource=new Hashtable<>();
		
		String fieldNameExp=null;
		String fieldNamePred=null;
		
		DecimalFormat df=new DecimalFormat("0");
		
		for (int i=0;i<jaRecordsSource.size();i++) {
			JsonObject joRecord=jaRecordsSource.get(i).getAsJsonObject();
			
			String idRecord=joRecord.get("exp_prop_id").getAsString();
			String exp_prop_id_Record=df.format(Double.parseDouble(idRecord));
			
//			System.out.println("exp_prop_id_Record\t"+exp_prop_id_Record);
			
			if(htConsensus.get(exp_prop_id_Record)==null) continue;
			JsonObject joConsensus=htConsensus.get(exp_prop_id_Record);
			String smilesConsensus=joConsensus.get("Canonical QSAR Ready Smiles").getAsString();
//			System.out.println(Utilities.gson.toJson(joConsensus));
			
			if(htConsensusSource.get(smilesConsensus)!=null) {
//				System.out.println("skipping:"+smilesConsensus);
				continue;
			}

			if (fieldNameExp==null) {
				for(Map.Entry<String, JsonElement> entry : joConsensus.entrySet()) {
				    if(entry.getKey().contains("Observed")) {
				    	fieldNameExp=entry.getKey();
				    } else if (entry.getKey().contains("Predicted")) {
				    	fieldNamePred=entry.getKey();
				    }
				}
			}
			
			double exp=joConsensus.get(fieldNameExp).getAsDouble();
			double pred=joConsensus.get(fieldNamePred).getAsDouble();
			
			ModelPrediction mp=new ModelPrediction(smilesConsensus,exp,pred,1);
			htConsensusSource.put(smilesConsensus,mp);

//			System.out.println(smilesConsensus+"\t"+Utilities.gson.toJson(joConsensus));

			
//			System.out.println(exp_prop_id);

		}
		
		List<ModelPrediction>mps=new ArrayList<>();

		int counter=0;
		
		for (String smiles:htConsensusSource.keySet()) {
			ModelPrediction mp=htConsensusSource.get(smiles);
//			System.out.println(++counter+"\t"+mp.exp+"\t"+mp.pred);
			mps.add(mp);
		}
		
		
//		System.out.println(Utilities.gson.toJson(mps));
		
		List<Double>errs=new ArrayList<>();
		
		for (ModelPrediction mp:mps) {
			double error=Math.abs(mp.exp-mp.pred);
			errs.add(error);
			
//			if (source_name.equals("Danish_EPA_SCPFAS_Report_2015")) {
//				System.out.println(mp.id+"\t"+mp.exp+"\t"+mp.pred);
//			}
			
		}
		
		return errs;

//		double MAE=calculateMean(errs);
//		
//		String tag="Test";
//		Map<String, Double>statsMap=ModelStatisticCalculator.calculateContinuousStatistics(mps, 0.0, tag);
//		Double MAE=statsMap.get(DevQsarConstants.MAE + tag);
//		return MAE;
	}
	
	
	private static List<Double> getOverallErrors(JsonArray jaConsensus) {

		String fieldNameExp=null;
		String fieldNamePred=null;
		List<ModelPrediction>mps=new ArrayList<>();
		
		for (int i=0;i<jaConsensus.size();i++) {
			JsonObject joConsensus=jaConsensus.get(i).getAsJsonObject();
			
			String smilesConsensus=joConsensus.get("Canonical QSAR Ready Smiles").getAsString();

			if (fieldNameExp==null) {
				for(Map.Entry<String, JsonElement> entry : joConsensus.entrySet()) {
				    if(entry.getKey().contains("Observed")) {
				    	fieldNameExp=entry.getKey();
				    } else if (entry.getKey().contains("Predicted")) {
				    	fieldNamePred=entry.getKey();
				    }
				}
			}
			double exp=joConsensus.get(fieldNameExp).getAsDouble();
			double pred=joConsensus.get(fieldNamePred).getAsDouble();
			ModelPrediction mp=new ModelPrediction(smilesConsensus,exp,pred,1);
			mps.add(mp);
		}
		
		List<Double>errs=new ArrayList<>();
		
		for (ModelPrediction mp:mps) {
			double error=Math.abs(mp.exp-mp.pred);
			errs.add(error);
		}
		
		return errs;

//		double stdev=calculateStandardDeviation(errs);
//		
//		System.out.println("stdev\t"+stdev);
//		
////		System.out.println(Utilities.gson.toJson(mps));
//
////		String tag="Test";
////		Map<String, Double>statsMap=ModelStatisticCalculator.calculateContinuousStatistics(mps, 0.0, tag);
////		Double MAE=statsMap.get(DevQsarConstants.MAE + tag);
//		
//		double MAE=calculateMean(errs);
//		
//		
//		return MAE;
	}
	
	public static double calculateMean(List<Double> array) {

	    // get the sum of array
	    double sum = 0.0;
	    for (double i : array) {
	        sum += i;
	    }

	    // get the mean of array
	    int length = array.size();
	    double mean = sum / length;

	    return mean;
	}
	
	public static double calculateStandardDeviation(List<Double> array) {

	    // get the sum of array
	    double sum = 0.0;
	    for (double i : array) {
	        sum += i;
	    }

	    // get the mean of array
	    int length = array.size();
	    double mean = sum / length;

	    // calculate the standard deviation
	    double standardDeviation = 0.0;
	    for (double num : array) {
	        standardDeviation += Math.pow(num - mean, 2);
	    }

	    return Math.sqrt(standardDeviation / length);
	}

}
