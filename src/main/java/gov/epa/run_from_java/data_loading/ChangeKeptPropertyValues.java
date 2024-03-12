package gov.epa.run_from_java.data_loading;

import java.io.FileReader;
import java.util.*;

import com.google.gson.Gson;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.service.*;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.endpoints.datasets.DatasetCreator;
import gov.epa.run_from_java.scripts.PredictScript;

/**
* @author TMARTI02
*/
public class ChangeKeptPropertyValues {

	
	PropertyValueService propertyValueService = new PropertyValueServiceImpl();
	
	public void updateKeepBasedOnPredictedWS(String propertyName, String datasetName, List<String> includedSources) {
	
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


		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\ECOTOX_2023_12_14\\";
//		String filepathWSpred=folder+"WS pred xgb.json";		
		String filepathWSpred=folder+"WS_pred_xgb_"+datasetName+".json";
		
		Hashtable<String, Double> htWSpred=getHashtableWSpred(filepathWSpred);
		
		
		List<DsstoxRecord>records=PredictScript.getDsstoxRecords();
		Hashtable<String, DsstoxRecord> htDsstox=PredictScript.getDsstoxHashtableByDTXSID(records);
		
		List<String>omitted=new ArrayList<>();
		
		List<PropertyValue>propertyValuesUpdate=new ArrayList<>();
		
		
		for (PropertyValue pv:propertyValues) {

			String chemicalName=pv.getSourceChemical().getSourceChemicalName();
			
			String dtxsid=pv.getSourceChemical().getSourceDtxsid();
			
//			System.out.println(pv.getUnit().getName());
			
			Double toxValue_g_L=null;
			
			
			if(htWSpred.containsKey(dtxsid)) {
		
				if(pv.getUnit().getName().equals("MOLAR")) {
					double mol_weight=htDsstox.get(dtxsid).getMolWeight();					
					toxValue_g_L=pv.getValuePointEstimate()*mol_weight;
				} else if(pv.getUnit().getName().equals("G_L")) {
					toxValue_g_L=pv.getValuePointEstimate();
				} else {
					System.out.println(pv.getUnit().getAbbreviation()+"\tnot handled");
					continue;
				}
				
				Double wsValue_g_L=htWSpred.get(dtxsid);
				
				if(toxValue_g_L>wsValue_g_L) {				
					System.out.println(dtxsid+"\t"+toxValue_g_L+"\t"+wsValue_g_L);
					pv.setKeep(false);
					pv.setKeepReason("Toxicity value exceeds predicted water solubility from XGB model");
					propertyValuesUpdate.add(pv);
				}
				
				
			} else {
				
				String smiles=htDsstox.get(dtxsid).getSmiles();
				
				//some of omitted might be for chemicals where the range in tox values was too wide for the median tox value
				if(smiles!=null && !smiles.contains(".") && !smiles.contains(":") &&  !smiles.contains("[") && smiles.contains("C")) 
					if(!omitted.contains(dtxsid+"\t"+smiles)) omitted.add(dtxsid+"\t"+smiles);
			}
			
		}
				
		propertyValueService.update(propertyValuesUpdate);
				
//		for (String dtxsid:omitted) {
//			System.out.println(dtxsid);
//		}


	}
	
	
	private Hashtable<String, Double> getHashtableWSpred(String filepathWSpred)  {
		Gson gson=new Gson();
		Hashtable<String, Double> htPredWS;
		try {
			htPredWS = gson.fromJson(new FileReader(filepathWSpred), (Hashtable.class));
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
		c.updateKeepBasedOnPredictedWS(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50, "exp_prop_96HR_FHM_LC50_v1 modeling",includedSources);
	}

}
