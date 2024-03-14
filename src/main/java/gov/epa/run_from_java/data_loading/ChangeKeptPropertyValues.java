package gov.epa.run_from_java.data_loading;

import java.io.FileReader;
import java.util.*;

import com.google.gson.Gson;

import gov.epa.databases.dev_qsar.DevQsarConstants;
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

	
	PropertyValueService propertyValueService = new PropertyValueServiceImpl();
		
	public void updateKeepBasedOnPredictedWS(String datasetName, List<String> includedSources, boolean generateNewPredictions,boolean postUpdates,String userName) {
	
		boolean useKeep=false;
		boolean omitQualifiers=true;
		boolean convertLogMolar=true;

		String propertyNameDataset = getPropertyNameForDataset(datasetName);
		
		long modelId=1066L;
		PredictScript ps=new PredictScript();
		String propertyNameModel=ps.getPropertyNameModel(modelId);
		
		System.out.println("Selecting experimental property data for " + propertyNameDataset + "...");
		long t5 = System.currentTimeMillis();
		List<PropertyValue> propertyValues = propertyValueService.findByPropertyNameWithOptions(propertyNameDataset,
				useKeep, omitQualifiers);
		long t6 = System.currentTimeMillis();
		System.out.println("Selection time = " + (t6 - t5) / 1000.0 + " s");

		System.out.println("Raw records:" + propertyValues.size());
		DatasetCreator.excludePropertyValues2(includedSources, propertyValues);
		
		if (includedSources.size() > 0)
			System.out.println("Raw records after source exclusion:" + propertyValues.size());

		
		//Folder for storing prediction hashtable:
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\ECOTOX_2023_12_14\\";
		String filePathPreds=folder+datasetName+"_WS.json";
		
		Hashtable<String, Double> htPred = getPredictionHashtable(datasetName, generateNewPredictions, modelId, ps,
				propertyNameModel,filePathPreds);
				
		
		List<DsstoxRecord>records=PredictScript.getDsstoxRecords();
		Hashtable<String, DsstoxRecord> htDsstox=PredictScript.getDsstoxHashtableByDTXSID(records);
		
		
		List<PropertyValue>propertyValuesUpdate=new ArrayList<>();
		
		List<String>omittedDtxsids=new ArrayList<>();
		
		for (PropertyValue pv:propertyValues) {

			if(!pv.getKeep()) continue;
			
			String chemicalName=pv.getSourceChemical().getSourceChemicalName();
			String dtxsid=pv.getSourceChemical().getSourceDtxsid();
			
//			System.out.println(pv.getUnit().getName());
			
			Double toxValue_g_L=null;

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
				
				double pred_Neg_Log_molar=htPred.get(dtxsid);
				double pred_molar=Math.pow(10.0, -pred_Neg_Log_molar);
				double wsValue_g_L=pred_molar*mol_weight;
				
				if(toxValue_g_L>wsValue_g_L) {				
					System.out.println(dtxsid+"\t"+toxValue_g_L+"\t"+wsValue_g_L);
					pv.setKeep(false);
					pv.setKeepReason("Toxicity value exceeds predicted water solubility from XGB model");
					pv.setUpdatedBy(userName);
					propertyValuesUpdate.add(pv);
				}
				
			} else {
				if(!omittedDtxsids.contains(dtxsid)) omittedDtxsids.add(dtxsid);
//				System.out.println(dtxsid+"\tNo prediction in hashtable");
			}
			
		}
		System.out.println("Number of records to update:"+propertyValuesUpdate.size());
		
//		System.out.println("Missing predicted value:");
//		for (String dtxsidOmitted:omittedDtxsids) {
//			System.out.println(dtxsidOmitted);
//		}
		
		if(postUpdates)
			propertyValueService.update(propertyValuesUpdate);
				
	}


	public void updateKeepBasedOnBaselineToxicity(String datasetName, List<String> includedSources, boolean generateNewPredictions,boolean postUpdates,String typeAnimal,String userName) {
		
		boolean useKeep=false;
		boolean omitQualifiers=true;
		boolean convertLogMolar=false;

		String propertyNameDataset = getPropertyNameForDataset(datasetName);
		
		long modelId=1069L;//logKow XGB model
		PredictScript ps=new PredictScript();
		String propertyNameModel=ps.getPropertyNameModel(modelId);
		
		System.out.println("Selecting experimental property data for " + propertyNameDataset + "...");
		long t5 = System.currentTimeMillis();
		List<PropertyValue> propertyValues = propertyValueService.findByPropertyNameWithOptions(propertyNameDataset,
				useKeep, omitQualifiers);
		long t6 = System.currentTimeMillis();
		System.out.println("Selection time = " + (t6 - t5) / 1000.0 + " s");

		System.out.println("Raw records:" + propertyValues.size());
		DatasetCreator.excludePropertyValues2(includedSources, propertyValues);
		
		if (includedSources.size() > 0)
			System.out.println("Raw records after source exclusion:" + propertyValues.size());

		
		//Folder for storing prediction hashtable:
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\ECOTOX_2023_12_14\\";
//		String filePathPreds=folder+propertyNameDataset+"_LogKow.json";
		String filePathPreds=folder+datasetName+"_LogKow.json";
		
		Hashtable<String, Double> htPred = getPredictionHashtable(datasetName, generateNewPredictions, modelId, ps,
				propertyNameModel,filePathPreds);
						
		List<DsstoxRecord>records=PredictScript.getDsstoxRecords();
		Hashtable<String, DsstoxRecord> htDsstox=PredictScript.getDsstoxHashtableByDTXSID(records);
		
		
		List<PropertyValue>propertyValuesUpdate=new ArrayList<>();
		
		List<String>omittedDtxsids=new ArrayList<>();
		List<String>omittedNames=new ArrayList<>();
		
		System.out.println("dtxsid\ttoxValue_g_L\t10*BaseLineTox_g_L");
		
		for (PropertyValue pv:propertyValues) {

			String chemicalName=pv.getSourceChemical().getSourceChemicalName();
			String dtxsid=pv.getSourceChemical().getSourceDtxsid();
			
//			System.out.println(pv.getUnit().getName());
			
			Double toxValue_g_L=null;

			if(!pv.getKeep()) continue;
			
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
				
				if (typeAnimal.equals("Fish")) {
					//ECOSAR manual, Baseline Toxicity Equation for Fish:
					BaseLineTox_Log_mmol_L=-0.8981*logKowPred + 1.7108;	
				} else if (typeAnimal.equals("Fathead minnow")) {					
					//FHM model for nonpolar compounds, Nendza and Russom, 1991:
					BaseLineTox_Log_mmol_L=-0.79*logKowPred + 1.35;
					//Is there a better model for FHM
				} else if (typeAnimal.equals("Daphnid")) {
//					ECOSAR manual, Baseline Toxicity Equation for Daphnid:
					BaseLineTox_Log_mmol_L=-0.8580*logKowPred + 1.3848;
				} else {
					System.out.println("Unknown animal type");
					return;
				}
							
				double BaseLineTox_mmol_L=Math.pow(10.0, BaseLineTox_Log_mmol_L);
				double BaseLineTox_mol_L=BaseLineTox_mmol_L/1000.0;				
				double BaseLineTox_g_L=BaseLineTox_mol_L*mol_weight;
				
				if(toxValue_g_L>10.0*BaseLineTox_g_L) {
					System.out.println(dtxsid+"\t"+chemicalName+"\t"+toxValue_g_L+"\t"+10*BaseLineTox_g_L);
					pv.setKeep(false);
					pv.setKeepReason("Toxicity value exceeds 10*baseline toxicity (logKow from XGB model)");
					pv.setUpdatedBy(userName);
					propertyValuesUpdate.add(pv);
				}
				
//				if(toxValue_g_L>wsValue_g_L) {				
			} else {
				if(!omittedDtxsids.contains(dtxsid)) omittedDtxsids.add(dtxsid);
				if(!omittedNames.contains(chemicalName)) omittedNames.add(chemicalName);
//				System.out.println(dtxsid+"\tNo prediction in hashtable");
			}
			
		}
		System.out.println("typeAnimal:"+typeAnimal);
		System.out.println("Number of records to update:"+propertyValuesUpdate.size());
		
//		System.out.println("DTXSIDs missing predicted logKow:"+omittedDtxsids.size());
//		for (String dtxsidOmitted:omittedDtxsids) {
//			System.out.println(dtxsidOmitted);
//		}
		
//		System.out.println("Names missing predicted logKow:"+omittedNames.size());
		//Most of omitted are salts that the dsstox mapping code omits
//		for (String omittedName:omittedNames) {
//			System.out.println(omittedName);
//		}
		
		if(postUpdates)
			propertyValueService.update(propertyValuesUpdate);
				
	}

	private Hashtable<String, Double> getPredictionHashtable(String datasetName, boolean generateNewPredictions,
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
	
	
	
	public void updateKeepBasedExposureType(String propertyName, String datasetName, List<String> includedSources,String userName) {
		
		boolean useKeep=false;
		boolean omitQualifiers=true;
		
		System.out.println("Selecting experimental property data for " + propertyName + "...");
		long t5 = System.currentTimeMillis();
		List<PropertyValue> propertyValues = propertyValueService.findByPropertyNameWithOptions(propertyName,
				useKeep, omitQualifiers);
		long t6 = System.currentTimeMillis();
		System.out.println("Selection time = " + (t6 - t5) / 1000.0 + " s");

		System.out.println("Raw records:" + propertyValues.size());
		DatasetCreator.excludePropertyValues2(includedSources, propertyValues);
		
		if (includedSources.size() > 0)
			System.out.println("Raw records after source exclusion:" + propertyValues.size());

		
		List<DsstoxRecord>records=PredictScript.getDsstoxRecords();
		Hashtable<String, DsstoxRecord> htDsstox=PredictScript.getDsstoxHashtableByDTXSID(records);
		
//		List<String>omitted=new ArrayList<>();		
		List<PropertyValue>propertyValuesUpdate=new ArrayList<>();
				
		for (PropertyValue pv:propertyValues) {

			if(!pv.getKeep())continue;
			
//			String chemicalName=pv.getSourceChemical().getSourceChemicalName();
//			String dtxsid=pv.getSourceChemical().getSourceDtxsid();
			
			for(ParameterValue parameterValue:pv.getParameterValues()) {
				if(parameterValue.getParameter().getName().equals("exposure_type")) {
					if (parameterValue.getValueText().contains("Not reported")) {
						propertyValuesUpdate.add(pv);
						pv.setKeep(false);
						pv.setKeepReason("exposure_type is not reported");
						pv.setUpdatedBy(userName);
					}					
				}
			}
//			System.out.println(pv.getUnit().getName());
		}
		
		System.out.println(propertyValuesUpdate.size());
				
		propertyValueService.update(propertyValuesUpdate);
				
//		for (String dtxsid:omitted) {
//			System.out.println(dtxsid);
//		}


	}
		
	private Hashtable<String, Double> getHashtablePred(String filepathPred)  {
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
		boolean generateNewPredictions=false;
		boolean postUpdates=false;
		
		String userName="tmarti02";
		c.updateKeepBasedOnPredictedWS("exp_prop_96HR_FHM_LC50_v1 modeling",includedSources,generateNewPredictions,postUpdates,userName);
		
//		String typeAnimal="Fish";
		String typeAnimal="Fathead minnow";
//		c.updateKeepBasedOnBaselineToxicity("exp_prop_96HR_FHM_LC50_v1 modeling",includedSources,generateNewPredictions,postUpdates,typeAnimal,userName);

		//c.updateKeepBasedExposureType(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50, "exp_prop_96HR_FHM_LC50_v1 modeling",includedSources,userName);
	}

}
