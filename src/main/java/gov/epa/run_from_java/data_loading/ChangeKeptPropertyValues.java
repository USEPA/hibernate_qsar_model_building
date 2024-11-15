package gov.epa.run_from_java.data_loading;

import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.*;
import com.google.gson.Gson;
import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.service.*;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.endpoints.datasets.DatasetCreator;

import gov.epa.run_from_java.scripts.PredictScript;
import gov.epa.run_from_java.scripts.SqlUtilities;

/**
* @author TMARTI02
*/
public class ChangeKeptPropertyValues {

	public static String typeAnimalFish="Fish";
	public static String typeAnimalDaphnid="Daphnid";
	public static String typeAnimalFatheadMinnow="Fathead minnow";
	
	PropertyValueService propertyValueService = new PropertyValueServiceImpl();
			
	
	public static int removeBasedOnPredictedWS(String datasetNameOriginal, List<PropertyValue> propertyValues) {

		DecimalFormat df=new DecimalFormat("0.00E00");
		DecimalFormat df2=new DecimalFormat("0.0");
		
		boolean generateNewPredictions=true;
		
		long modelId=1066L;
		PredictScript ps=new PredictScript();
		String propertyNameModel=ps.getPropertyNameModel(modelId);


		//Folder for storing prediction hashtable:
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\ECOTOX_2023_12_14\\";
		String filePathPreds=folder+datasetNameOriginal+"_WS.json";

		Hashtable<String, Double> htPred = getPredictionHashtable(datasetNameOriginal, generateNewPredictions, modelId, ps,
				propertyNameModel,filePathPreds);

		List<DsstoxRecord>records=PredictScript.getDsstoxRecords();
		Hashtable<String, DsstoxRecord> htDsstox=PredictScript.getDsstoxHashtableByDTXSID(records);

		int countBefore=propertyValues.size();

		for (int i=0;i<propertyValues.size();i++) {

			PropertyValue pv=propertyValues.get(i);

			if(!pv.getKeep()) continue;

			String chemicalName=pv.getSourceChemical().getSourceChemicalName();
			String CAS=pv.getSourceChemical().getSourceCasrn();
			String dtxsid=pv.getSourceChemical().getSourceDtxsid();
			
			if(dtxsid==null) {
				System.out.println("Missing source dtxsid for "+chemicalName+" ("+CAS+")");
				continue;
			}

			Double toxValue_g_L=null;

			if(!htPred.containsKey(dtxsid)) {
//				System.out.println("prediction hashtable missing "+dtxsid);
				continue;
			}

			double mol_weight=htDsstox.get(dtxsid).getMolWeight();

			if(pv.getUnit().getName().equals("MOLAR")) {
				toxValue_g_L=pv.getValuePointEstimate()*mol_weight;
			} else if(pv.getUnit().getName().equals("G_L")) {
				toxValue_g_L=pv.getValuePointEstimate();
			} else {
				System.out.println(pv.getUnit().getAbbreviation()+"\tnot handled");
				continue;
			}

			double pred_Neg_Log_molar=htPred.get(dtxsid);
			double pred_molar=Math.pow(10.0, -pred_Neg_Log_molar);
			double wsValue_g_L=pred_molar*mol_weight;
			
			if(toxValue_g_L>10.0*wsValue_g_L) {				
				System.out.println(dtxsid+"\t"+df.format(toxValue_g_L)+"\t"+df.format(wsValue_g_L)+"\tWS ratio="+df2.format(toxValue_g_L/wsValue_g_L));
				
				propertyValues.remove(i--);
			}
		}
		
		int countAfter=propertyValues.size();
		
		return countBefore-countAfter;

	}

	
	
	public static int removeBasedOnBaselineToxicity(String datasetName, List<PropertyValue> propertyValues,String typeAnimal) {
		
		boolean generateNewPredictions=true;
		
		long modelId=1069L;//logKow XGB model
		PredictScript ps=new PredictScript();
		String propertyNameModel=ps.getPropertyNameModel(modelId);
		
		//Folder for storing prediction hashtable:
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\ECOTOX_2023_12_14\\";
//		String filePathPreds=folder+propertyNameDataset+"_LogKow.json";
		String filePathPreds=folder+datasetName+"_LogKow.json";
		
		Hashtable<String, Double> htPred = getPredictionHashtable(datasetName, generateNewPredictions, modelId, ps,
				propertyNameModel,filePathPreds);
						
		List<DsstoxRecord>records=PredictScript.getDsstoxRecords();
		Hashtable<String, DsstoxRecord> htDsstox=PredictScript.getDsstoxHashtableByDTXSID(records);
		
		List<String>omittedDtxsids=new ArrayList<>();
		List<String>omittedNames=new ArrayList<>();
		
		System.out.println("dtxsid\ttoxValue_g_L\t10*BaseLineTox_g_L");
		
		int countBefore=propertyValues.size();
		
		for (int i=0;i<propertyValues.size();i++) {

			PropertyValue pv=propertyValues.get(i);

			String chemicalName=pv.getSourceChemical().getSourceChemicalName();
			String dtxsid=pv.getSourceChemical().getSourceDtxsid();
			String CAS=pv.getSourceChemical().getSourceCasrn();
						
//			System.out.println(pv.getUnit().getName());
			
			Double toxValue_g_L=null;

			if(!pv.getKeep()) continue;
			
			if(dtxsid==null) {
				System.out.println("Missing source dtxsid for "+chemicalName+" ("+CAS+")");
				continue;
			}
			
			if(htPred.containsKey(dtxsid)) {
		
				double mol_weight=htDsstox.get(dtxsid).getMolWeight();
				
				if(pv.getUnit().getName().equals("MOLAR")) {
					toxValue_g_L=pv.getValuePointEstimate()*mol_weight;
				} else if(pv.getUnit().getName().equals("G_L")) {
					toxValue_g_L=pv.getValuePointEstimate();
				} else {
					System.out.println(pv.getUnit().getAbbreviation()+"\tnot handled");
					continue;
				}
								
				Double logKowPred=htPred.get(dtxsid);
//				System.out.println(dtxsid+"\t"+logKowPred);
								
				Double BaseLineTox_Log_mmol_L=null;
				
				if (typeAnimal.equals(typeAnimalFish)) {
					//ECOSAR manual, Baseline Toxicity Equation for Fish:
					BaseLineTox_Log_mmol_L=-0.8981*logKowPred + 1.7108;	
				} else if (typeAnimal.equals(typeAnimalFatheadMinnow)) {					
					//FHM model for nonpolar compounds, Nendza and Russom, 1991:
					BaseLineTox_Log_mmol_L=-0.79*logKowPred + 1.35;
					//Note this ends up excluding some records for methanol!
					//Is there a better model for FHM
				} else if (typeAnimal.equals(typeAnimalDaphnid)) {
//					ECOSAR manual, Baseline Toxicity Equation for Daphnid:
					BaseLineTox_Log_mmol_L=-0.8580*logKowPred + 1.3848;
				} else {
					System.out.println("Unknown animal type");
					return -9999;
				}
							
				double BaseLineTox_mmol_L=Math.pow(10.0, BaseLineTox_Log_mmol_L);
				double BaseLineTox_mol_L=BaseLineTox_mmol_L/1000.0;				
				double BaseLineTox_g_L=BaseLineTox_mol_L*mol_weight;
				
				if(toxValue_g_L>10.0*BaseLineTox_g_L) {
					System.out.println(dtxsid+"\t"+chemicalName+"\t"+toxValue_g_L+"\t"+10*BaseLineTox_g_L+"\tToxicity value exceeds 10*baseline toxicity (logKow from XGB model)");
					propertyValues.remove(i--);
				}
				
//				if(toxValue_g_L>wsValue_g_L) {				
			} else {
				if(!omittedDtxsids.contains(dtxsid)) omittedDtxsids.add(dtxsid);
				if(!omittedNames.contains(chemicalName)) omittedNames.add(chemicalName);
//				System.out.println(dtxsid+"\tNo prediction in hashtable");
			}
		}
		
		int countAfter=propertyValues.size();
		
//		System.out.println("DTXSIDs missing predicted logKow:"+omittedDtxsids.size());
//		for (String dtxsidOmitted:omittedDtxsids) {
//			System.out.println(dtxsidOmitted);
//		}
		
//		System.out.println("Names missing predicted logKow:"+omittedNames.size());
		//Most of omitted are salts that the dsstox mapping code omits
//		for (String omittedName:omittedNames) {
//			System.out.println(omittedName);
//		}
		
		return countBefore-countAfter;
		
	}

	private  static Hashtable<String, Double> getPredictionHashtable(String datasetName, boolean generateNewPredictions,
			long modelId, PredictScript ps, String propertyNameModel,String filePathPreds) {
		Hashtable<String, Double>htPred=null;


		if(generateNewPredictions) {
			//Folder to save hashtable for inspection:
			htPred=ps.predict(filePathPreds,modelId, datasetName);
		} else {			
			htPred=getHashtablePred(filePathPreds);
		}
		return htPred;
	}



	private String getPropertyNameForDataset(String datasetName) {
		String sql="select p.name from qsar_datasets.datasets d\r\n"
				+ "join qsar_datasets.properties p on d.fk_property_id = p.id\r\n"
				+ "where d.name='"+datasetName+"';";
		
		String propertyName=SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql);
		return propertyName;
	}
	
	
	
	
		
	private static Hashtable<String, Double> getHashtablePred(String filepathPred)  {
		Gson gson=new Gson();
		Hashtable<String, Double> htPredWS;
		try {
			htPredWS = gson.fromJson(new FileReader(filepathPred), (Hashtable.class));
			return htPredWS;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		ChangeKeptPropertyValues c=new ChangeKeptPropertyValues();
		List<String>includedSources=Arrays.asList("ECOTOX_2023_12_14");
		
		//TODO implement filter to exclude LC50>10 * baseline LC50
		boolean generateNewPredictions=true;
		boolean postUpdates=true;
		
//		String userName="tmarti02";
//		c.updateKeepBasedOnPredictedWS("exp_prop_96HR_FHM_LC50_v1 modeling",includedSources,generateNewPredictions,postUpdates,userName);
//		c.updateKeepBasedOnPredictedWS("exp_prop_96HR_BG_LC50_v1 modeling",includedSources,generateNewPredictions,postUpdates,userName);
		
//		String typeAnimal="Fish";
//		String typeAnimal="Fathead minnow";
//		c.updateKeepBasedOnBaselineToxicity("exp_prop_96HR_FHM_LC50_v1 modeling",includedSources,generateNewPredictions,postUpdates,typeAnimal,userName);

		//c.updateKeepBasedExposureType(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50, "exp_prop_96HR_FHM_LC50_v1 modeling",includedSources,userName);
	}


	public static int removeBasedOnMissingExposureType(String datasetName, List<PropertyValue> propertyValues) {
		
		
		int countBefore=propertyValues.size();
		for (int i=0;i<propertyValues.size();i++) {
			PropertyValue pv=propertyValues.get(i);
			String dtxsid=pv.getSourceChemical().getSourceDtxsid();
			for (ParameterValue parameterValue:pv.getParameterValues()) {
				if(!parameterValue.getParameter().getName().equals("exposure_type")) continue;
				String exposure_type=parameterValue.getValueText().toLowerCase();
				if(exposure_type.contains("not reported")) {
//					System.out.println(dtxsid+"\texposure_type="+exposure_type);
					propertyValues.remove(i--);
					break;
				}
			}
		}
		int countAfter=propertyValues.size();
		
		return countBefore-countAfter;
		
	}
	
	public static int removeBasedOnConcentrationType(String datasetName, List<PropertyValue> propertyValues) {
		
		List<String>okTypes=Arrays.asList("active ingredient");
		
		int countBefore=propertyValues.size();
		for (int i=0;i<propertyValues.size();i++) {
			
			PropertyValue pv=propertyValues.get(i);
			String dtxsid=pv.getSourceChemical().getSourceDtxsid();
			
			for (ParameterValue parameterValue:pv.getParameterValues()) {
				
				if(!parameterValue.getParameter().getName().equals("concentration_type")) continue;
				
				String concentration_type=parameterValue.getValueText().toLowerCase();
				
				if(!okTypes.contains(concentration_type)) {
//					System.out.println(dtxsid+"\tconcentration_type="+concentration_type);
					propertyValues.remove(i--);
					break;
				}
				
			}
		}
		int countAfter=propertyValues.size();
		
		return countBefore-countAfter;
		
	}


}
