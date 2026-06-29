package gov.epa.run_from_java.data_loading;

import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;
import gov.epa.databases.dev_qsar.exp_prop.entity.Parameter;
import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.service.*;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.endpoints.datasets.DatasetCreator;
import gov.epa.endpoints.datasets.MappedPropertyValue;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.run_from_java.scripts.PredictScript;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.util.JsonUtilities;

/**
* @author TMARTI02
*/
public class ChangeKeptPropertyValues {

	public static String typeAnimalFish="Fish";
	public static String typeAnimalDaphnid="Daphnid";
	public static String typeAnimalFatheadMinnow="Fathead minnow";
	

	PropertyValueService propertyValueService = new PropertyValueServiceImpl();
			
	
	public static int removeBasedOnPredictedWS(String datasetNameOriginal, List<PropertyValue> propertyValues, double factor) {

		DecimalFormat df=new DecimalFormat("0.00E00");
		DecimalFormat df2=new DecimalFormat("0.0");
		
		boolean generateNewPredictions=true;
		
		long modelId=1066L;
		PredictScript ps=new PredictScript();
		String propertyNameModel=ps.getPropertyNameModel(modelId);


		//Folder for storing prediction hashtable:
//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\ECOTOX_2023_12_14\\";
//		String filePathPreds=folder+datasetNameOriginal+"_WS.json";

		Hashtable<String, Double> htPred = getPredictionHashtable(datasetNameOriginal,modelId, ps,
				propertyNameModel);

		List<DsstoxRecord>records=PredictScript.getDsstoxRecords();
		Hashtable<String, DsstoxRecord> htDsstox=PredictScript.getDsstoxHashtableByDTXSID(records);

		int countBefore=propertyValues.size();

		for (int i=0;i<propertyValues.size();i++) {

			PropertyValue pv=propertyValues.get(i);

			if(!pv.getKeep()) continue;

			String chemicalName=pv.getSourceChemical().getSourceChemicalName();

			String dtxsid=pv.getSourceChemical().getSourceDtxsid();			

			String CAS=pv.getSourceChemical().getSourceCasrn();
//			String dtxsid=pv.getSourceChemical().getSourceDtxsid();
			
			if(dtxsid==null) {
				System.out.println("Missing source dtxsid for "+chemicalName+" ("+CAS+")");
				continue;
			}

			Double toxValue_g_L=null;

			if(dtxsid==null || !htPred.containsKey(dtxsid)) {
//				System.out.println("prediction hashtable missing "+dtxsid);
				continue;
			}

			double mol_weight=htDsstox.get(dtxsid).getMolWeight();
			
			Double pointEstimate=null;

			if(pv.getValuePointEstimate()==null) {
				if(pv.getValueMin()!=null && pv.getValueMax()!=null) {
					double diff=Math.abs(pv.getValueMax()-pv.getValueMin());
					
					if(diff<=1) {
						pointEstimate=(pv.getValueMin()+pv.getValueMax())/2.0;
					}
				} 
//				System.out.println(pv.getValueMin()+"\t"+pv.getValueMax()+"\t"+pv.getUnit().getAbbreviation());
			} else {
				pointEstimate=pv.getValuePointEstimate();
			}
			
			if(pointEstimate==null)continue;//dont check against WS			
			
			if(pv.getUnit().getName().equals("MOLAR")) {
				toxValue_g_L=pointEstimate*mol_weight;
			} else if(pv.getUnit().getName().equals("G_L")) {
				toxValue_g_L=pointEstimate;
			} else {
				System.out.println(pv.getUnit().getAbbreviation()+"\tnot handled");
				continue;
			}

			double pred_Neg_Log_molar=htPred.get(dtxsid);
			double pred_molar=Math.pow(10.0, -pred_Neg_Log_molar);
			double wsValue_g_L=pred_molar*mol_weight;
			
			if(toxValue_g_L>factor*wsValue_g_L) {				
				System.out.println(dtxsid+"\t"+df.format(toxValue_g_L)+"\t"+df.format(wsValue_g_L)+"\tWS ratio="+df2.format(toxValue_g_L/wsValue_g_L));
				propertyValues.remove(i--);
			}
		}
		
		int countAfter=propertyValues.size();
		
		return countBefore-countAfter;

	}
	
	public static List<MappedPropertyValue> removeBasedOnPredictedWS2(String datasetTsv,
	        Map<String, List<MappedPropertyValue>> unifiedPropertyValues,
	        double factor) {

	    DecimalFormat df  = new DecimalFormat("0.00E00");
	    DecimalFormat df2 = new DecimalFormat("0.0");

	    long modelId = 1066L;
	    PredictScript ps = new PredictScript();
	    String propertyNameModel = ps.getPropertyNameModel(modelId);

	    Hashtable<String, Double> htPred = ps.predictForTsv(null, modelId, datasetTsv);

	    // Iterate over the map with an iterator so we can safely remove entries
	    Iterator<Map.Entry<String, List<MappedPropertyValue>>> itMap =
	            unifiedPropertyValues.entrySet().iterator();
	    
	    
	    List<MappedPropertyValue>discarded=new ArrayList<>();

	    while (itMap.hasNext()) {
	        Map.Entry<String, List<MappedPropertyValue>> entry = itMap.next();
	        String qsarSmiles = entry.getKey();
	        List<MappedPropertyValue> propertyValues = entry.getValue();

	        // If prediction is missing, skip this chemical (avoid NPE)
	        Double predNegLogMolar = htPred.get(qsarSmiles);
	        if (predNegLogMolar == null) {
	            // Optional: log once
	            // System.out.println("No prediction for " + qsarSmiles + "; skipping.");
	            continue;
	        }

	        // Safe removal from the list while iterating
	        Iterator<MappedPropertyValue> itList = propertyValues.iterator();
	        while (itList.hasNext()) {
	            MappedPropertyValue mpv = itList.next();
	            PropertyValue pv = mpv.propertyValue;

	            if (!pv.getKeep()) continue;

	            Double mol_weight = mpv.dsstoxRecord.getMolWeight();
	            if (mol_weight == null) {
	                System.out.println("Missing MW for " + qsarSmiles);
	                continue;
	            }

	            // Derive point estimate if needed
	            Double pointEstimate = pv.getValuePointEstimate();
	            if (pointEstimate == null) {
	                if (pv.getValueMin() != null && pv.getValueMax() != null) {
	                    double diff = Math.abs(pv.getValueMax() - pv.getValueMin());
	                    if (diff <= 1) {
	                        pointEstimate = (pv.getValueMin() + pv.getValueMax()) / 2.0;
	                    }
	                }
	            }
	            if (pointEstimate == null) continue; // can't compare to WS

	            // Convert to g/L
	            Double toxValue_g_L;
	            String unitName = pv.getUnit().getName();
	            if ("MOLAR".equals(unitName)) {
	                toxValue_g_L = pointEstimate * mol_weight;
	            } else if ("G_L".equals(unitName)) {
	                toxValue_g_L = pointEstimate;
	            } else {
	                System.out.println(pv.getUnit().getAbbreviation() + "\tnot handled");
	                continue;
	            }

	            double pred_molar = Math.pow(10.0, -predNegLogMolar);
	            double waterSolubility_g_L = pred_molar * mol_weight;

	            if (toxValue_g_L > factor * waterSolubility_g_L) {
//	                System.out.println(
//	                    pv.getSourceChemical().getSourceCasrn() + "\t" +
//	                    df.format(toxValue_g_L) + "\t" + df.format(waterSolubility_g_L) +
//	                    "\tWS ratio=" + df2.format(toxValue_g_L / waterSolubility_g_L)
//	                );

	                itList.remove(); // safe removal from list during iteration

	                discarded.add(mpv);
	                
		            // Attach predicted WS as a parameter for traceability
		            ParameterValue pvWS = new ParameterValue();
		            Parameter pWS = new Parameter();
		            ExpPropUnit unit = new ExpPropUnit();
		            pWS.setName("Water solubility");
		            pvWS.setValuePointEstimate(predNegLogMolar);
		            unit.setAbbreviation("-log10(M)");
		            unit.setName("NEG_LOG_M");
		            pvWS.setParameter(pWS);
		            pvWS.setUnit(unit);
		            pv.addParameterValue(pvWS);
		            
		            pv.setKeepReason("Failed water solubility check");
	            }
	        }

	        // If the list is now empty, remove this chemical from the map using the map iterator
	        if (propertyValues.isEmpty()) {
	            itMap.remove(); // safe removal from map during iteration
	        }
	    }

	    return discarded;
	}
	
	
//	public static int removeBasedOnPredictedWS(String datasetNameOriginal,
//			Map<String, List<MappedPropertyValue>> unifiedPropertyValues) {
//		DecimalFormat df=new DecimalFormat("0.00E00");
//		DecimalFormat df2=new DecimalFormat("0.0");
//		
//		boolean generateNewPredictions=true;
//		
//		long modelId=1066L;
//		PredictScript ps=new PredictScript();
//		String propertyNameModel=ps.getPropertyNameModel(modelId);
//
//
//		//Folder for storing prediction hashtable:
////		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\ECOTOX_2023_12_14\\";
////		String filePathPreds=folder+datasetNameOriginal+"_WS.json";
//
//		Hashtable<String, Double> htPred = getPredictionHashtable(datasetNameOriginal,modelId, ps,
//				propertyNameModel);
//		
//		
////		System.out.println(Utilities.gson.toJson(htPred));
//
//		List<DsstoxRecord>records=PredictScript.getDsstoxRecords();
//		Hashtable<String, DsstoxRecord> htDsstox=PredictScript.getDsstoxHashtableByDTXSID(records);
//
//		int countBefore=0;
//		for (String key:unifiedPropertyValues.keySet()) {
//			List<MappedPropertyValue>listMPV=unifiedPropertyValues.get(key);
//			countBefore+=listMPV.size();
//		}
//
//		Iterator<Map.Entry<String, List<MappedPropertyValue>>> iterator = unifiedPropertyValues.entrySet().iterator();
//		
//		while (iterator.hasNext()) {
//						
//			Map.Entry<String, List<MappedPropertyValue>> entry = iterator.next();
//			
//			List<MappedPropertyValue>listMPV=entry.getValue();
//			
//			for(int j=0;j<listMPV.size();j++) {
//				
//				MappedPropertyValue mpv=listMPV.get(j);
//				String dtxsid=mpv.dsstoxRecord.dsstoxSubstanceId;
//				
////				System.out.println(dtxsid+"\t"+mpv.qsarPropertyValue);
//
////				double mol_weight=htDsstox.get(dtxsid).getMolWeight();
//				
//				if(!htPred.containsKey(dtxsid)) {
//					System.out.println("Dont have prediction for "+dtxsid);
//					continue;
//				}				
//				
//				double predWS=htPred.get(dtxsid);//should be in -logM
//				double toxValue=mpv.qsarPropertyValue;//should be in -logM
//				
//				if(toxValue<predWS-1.0) {				
////					System.out.println(dtxsid+"\t"+df.format(toxValue)+"\t"+df.format(predWS)+"\tExceed WS");					
//					listMPV.remove(j--);
//				}
//				
//			}
//			
//			if(listMPV.size()==0) iterator.remove();
//
//		}
//		
//		int countAfter=0;
//		for (String key:unifiedPropertyValues.keySet()) {
//			List<MappedPropertyValue>listMPV=unifiedPropertyValues.get(key);
//			countAfter+=listMPV.size();
//		}
//		
//		return countBefore-countAfter;
//	}
	
	
	
	
	public static int removeBasedOnWaterConcentrationAndPredictedWS(String datasetNameOriginal, List<PropertyValue> propertyValues) {

		DecimalFormat df=new DecimalFormat("0.00E00");
		DecimalFormat df2=new DecimalFormat("0.0");
		
//		boolean generateNewPredictions=true;
		
		long modelId=1066L;
		PredictScript ps=new PredictScript();
		String propertyNameModel=ps.getPropertyNameModel(modelId);

		//Folder for storing prediction hashtable:
//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\ECOTOX_2023_12_14\\";
//		String filePathPreds=folder+datasetNameOriginal+"_WS.json";

		Hashtable<String, Double> htPred = getPredictionHashtable(datasetNameOriginal,modelId, ps,
				propertyNameModel);

		
		List<DsstoxRecord>records=PredictScript.getDsstoxRecords();
		Hashtable<String, DsstoxRecord> htDsstox=PredictScript.getDsstoxHashtableByCASRN(records);

		int countBefore=propertyValues.size();

		for (int i=0;i<propertyValues.size();i++) {

			PropertyValue pv=propertyValues.get(i);

			if(!pv.getKeep()) continue;

			String chemicalName=pv.getSourceChemical().getSourceChemicalName();

//			String dtxsid=pv.getSourceChemical().getSourceDtxsid();			

			String CAS=pv.getSourceChemical().getSourceCasrn();
			
			
//			String dtxsid=pv.getSourceChemical().getSourceDtxsid();
			
			if(CAS==null) {
				System.out.println("Missing CAS for "+chemicalName);
				continue;
			}
			
			if(htDsstox.get(CAS)==null || htDsstox.get(CAS).getDtxsid()==null) {
				System.out.println(CAS+ " missing in dsstox records");
				continue;
			}
			
			DsstoxRecord dsstoxRecord =htDsstox.get(CAS);


			if(!htPred.containsKey(dsstoxRecord.getDtxsid())) {
//				System.out.println("prediction hashtable missing "+CAS);
				continue;
			}
			
			ParameterValue parameterValueCriterionWS=pv.getParameterValue("Criterion 3- Aqueous Solubility");
			
			String criterionWS=null;
			if(parameterValueCriterionWS!=null) {
				criterionWS=parameterValueCriterionWS.getValueText();
			}

			Double waterConc_g_L=null;
			
			if(pv.getParameterValue("Water concentration")==null) {
				
				if(!criterionWS.equals("2C")) {
					System.out.println(CAS+"\twater concentration unavailable\tcriterionWS="+criterionWS);
				}
				continue;
			} else {
				ParameterValue parameterValue=pv.getParameterValue("Water concentration");
				
				if(parameterValue.getUnit().getAbbreviation().equals("g/L")) {
					waterConc_g_L=parameterValue.getValuePointEstimate();
				} else {
					System.out.println(CAS+"\twater concentration units= "+parameterValue.getUnit().getAbbreviation());
					continue;
				}
			}
			
			double mol_weight=dsstoxRecord.getMolWeight();
			double pred_Neg_Log_molar=htPred.get(dsstoxRecord.getDtxsid());
			double pred_molar=Math.pow(10.0, -pred_Neg_Log_molar);
			double waterSolubility_g_L=pred_molar*mol_weight;
			
//			if(waterConc_g_L>10.0*waterSolubility_g_L) {				
//				System.out.println(dtxsid+"\t"+df.format(WaterConc_g_L)+"\t"+df.format(wsValue_g_L)+"\tWS ratio="+df2.format(WaterConc_g_L/wsValue_g_L));
//				propertyValues.remove(i--);
//			}
			
			boolean failsWS=waterConc_g_L>5.0*waterSolubility_g_L;
			boolean failsCriterion=criterionWS.contains("3");
			boolean match=failsWS==failsCriterion;
				
			if(!match && !failsWS)				
				System.out.println(CAS+"\t"+df.format(waterConc_g_L)+"\t"+df.format(waterSolubility_g_L)+"\tWS ratio="+df2.format(waterConc_g_L/waterSolubility_g_L)+"\t"+criterionWS+"\t"+match);
 			
		}
		
		int countAfter=propertyValues.size();
		return countBefore-countAfter;

	}
	
	
	static int countPropertyValues(Map<String, List<MappedPropertyValue>> unifiedPropertyValues) {
		
		int count=0;
		for (String qsarSmiles:unifiedPropertyValues.keySet()) {
			List<MappedPropertyValue>propertyValues=unifiedPropertyValues.get(qsarSmiles);
			count+=propertyValues.size();
		}
		return count;
		
	}
	

	public static List<MappedPropertyValue> removeBasedOnWaterConcentrationAndPredictedWS2(String datasetTsv, Map<String, List<MappedPropertyValue>> unifiedPropertyValues, double factor, boolean skipMissing) {

		DecimalFormat df=new DecimalFormat("0.00E00");
		DecimalFormat df2=new DecimalFormat("0.0");
		
		long modelId=1066L;//water solubility model
//		String propertyNameModel=ps.getPropertyNameModel(modelId);

		//Folder for storing prediction hashtable:
//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\ECOTOX_2023_12_14\\";
//		String filePathPreds=folder+datasetNameOriginal+"_WS.json";

		PredictScript ps=new PredictScript();
//		Hashtable<String, Double> htPred = ps.predictForTsv(null, modelId, datasetTsv);
		
//		int countRemoved=0;
				
		List<MappedPropertyValue>propertyValuesRemoved=new ArrayList<>();
		

		for (String qsarSmiles:unifiedPropertyValues.keySet()) {
			
			List<MappedPropertyValue>propertyValues=unifiedPropertyValues.get(qsarSmiles);
			Iterator<MappedPropertyValue> it = propertyValues.iterator();
			
			while (it.hasNext()) {
			    MappedPropertyValue mpv = it.next();
				PropertyValue pv=mpv.propertyValue;

				if(!pv.getKeep()) continue;


				Double waterConc_g_L=null;
				ParameterValue parameterValueWC=pv.getParameterValue("Water concentration");
				
				if(parameterValueWC==null) {
//					System.out.println(qsarSmiles+"\tWater concentration missing");

					if(skipMissing) {
						propertyValuesRemoved.add(mpv);
						it.remove(); // safe removal while iterating
						pv.setKeepReason("Missing water concentration");
					}
					continue;
				} else {
					
					String unitAbbrev=parameterValueWC.getUnit().getAbbreviation();
					
					if(unitAbbrev.equals("g/L")) {
						waterConc_g_L=parameterValueWC.getValuePointEstimate();
					} else if(unitAbbrev.equals("Bq/mL") || unitAbbrev.equals("Ci/mol")) {
						System.out.println(qsarSmiles+"\tRemoved since have radiolabeling units of "+unitAbbrev);
						it.remove(); // safe removal while iterating
						propertyValuesRemoved.add(mpv);
						pv.setKeepReason("radiolabeling units for water concentration");
						continue;

					} else {
						System.out.println(qsarSmiles+"\twater concentration units= "+parameterValueWC.getUnit().getAbbreviation());
						continue;
					}
				}
				
				Double mol_weight=mpv.dsstoxRecord.getMolWeight();
				
				if(mol_weight==null) {
					System.out.println("Missing MW for "+qsarSmiles);
					continue;
				}
				
				
//				pWS.setName("Water solubility");
//				pvWS.setValuePointEstimate(waterSolubility_g_L);
//				unit.setAbbreviation("g/L");
//				unit.setName("G_L");
				
				ParameterValue parameterValueWS = pv.getParameterValue("Water solubility");//ChangeKeptPropertyValues.addPredictedWaterSolubilityAsParameter
				
				if(!parameterValueWS.getUnit().getAbbreviation().equals("g/L")) {
					System.out.println(qsarSmiles+"\twater solubility units= "+parameterValueWS.getUnit().getAbbreviation());
					continue;
				} 
				double waterSolubility_g_L=parameterValueWS.getValuePointEstimate();
				
//				if(waterConc_g_L>10.0*waterSolubility_g_L) {				
//					System.out.println(dtxsid+"\t"+df.format(WaterConc_g_L)+"\t"+df.format(wsValue_g_L)+"\tWS ratio="+df2.format(WaterConc_g_L/wsValue_g_L));
//					propertyValues.remove(i--);
//				}
				
				if (waterConc_g_L>factor*waterSolubility_g_L) {
					it.remove(); // safe removal while iterating
//					System.out.println(qsarSmiles+"\t"+df.format(waterConc_g_L)+"\t"+df.format(waterSolubility_g_L));
					propertyValuesRemoved.add(mpv);
					pv.setKeepReason("waterConc_g_L>factor*waterSolubility_g_L");
				}
			}
		}
		
//		int countAfter=countPropertyValues(unifiedPropertyValues);
		
		return propertyValuesRemoved;

	}
	
	
	public static void addPredictedWaterSolubilityAsParameter(String datasetTsv, Map<String, List<MappedPropertyValue>> unifiedPropertyValues) {

		DecimalFormat df=new DecimalFormat("0.00E00");
		DecimalFormat df2=new DecimalFormat("0.0");
		
		long modelId=1066L;//water solubility model
//		String propertyNameModel=ps.getPropertyNameModel(modelId);

		//Folder for storing prediction hashtable:
//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\ECOTOX_2023_12_14\\";
//		String filePathPreds=folder+datasetNameOriginal+"_WS.json";

		PredictScript ps=new PredictScript();
		Hashtable<String, Double> htPred = ps.predictForTsv(null, modelId, datasetTsv);
		

		for (String qsarSmiles:unifiedPropertyValues.keySet()) {
			
			List<MappedPropertyValue>propertyValues=unifiedPropertyValues.get(qsarSmiles);
			Iterator<MappedPropertyValue> it = propertyValues.iterator();
			
			while (it.hasNext()) {
			    MappedPropertyValue mpv = it.next();
				PropertyValue pv=mpv.propertyValue;

				if(!pv.getKeep()) continue;


//				Double waterConc_g_L=null;
//				ParameterValue parameterValueWC=pv.getParameterValue("Water concentration");
//				
//				if(parameterValueWC==null) {
//					System.out.println(qsarSmiles+"\tWater concentration missing");
//					continue;
//				} else {
//					
//					if(parameterValueWC.getUnit().getAbbreviation().equals("g/L")) {
//						waterConc_g_L=parameterValueWC.getValuePointEstimate();
//					} else {
//						System.out.println(qsarSmiles+"\twater concentration units= "+parameterValueWC.getUnit().getAbbreviation());
//						continue;
//					}
//				}
				
				Double mol_weight=mpv.dsstoxRecord.getMolWeight();
				
				if(mol_weight==null) {
					System.out.println("Missing MW for "+qsarSmiles);
					continue;
				}
				
				double pred_Neg_Log_molar=htPred.get(qsarSmiles);
				double pred_molar=Math.pow(10.0, -pred_Neg_Log_molar);
				double waterSolubility_g_L=pred_molar*mol_weight;
				
				ParameterValue pvWS=new ParameterValue();
				Parameter pWS=new Parameter();
				ExpPropUnit unit=new ExpPropUnit();
				pWS.setName("Water solubility");
				pvWS.setValuePointEstimate(waterSolubility_g_L);
				unit.setAbbreviation("g/L");
				unit.setName("G_L");
				pvWS.setParameter(pWS);
				pvWS.setUnit(unit);
				pv.addParameterValue(pvWS);
			}
		}
		
	}
	
	public static List<MappedPropertyValue> removeBasedOnExposureDurationAndPredictedLogKow(String datasetTsv, Map<String, List<MappedPropertyValue>> unifiedPropertyValues, double factor, boolean skipMissing) {

		DecimalFormat df=new DecimalFormat("0.000");
//		DecimalFormat df2=new DecimalFormat("0.000");
		
		long modelId=1069L;//logKow XGB model
		
//		String propertyNameModel=ps.getPropertyNameModel(modelId);

		//Folder for storing prediction hashtable:
//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\ECOTOX_2023_12_14\\";
//		String filePathPreds=folder+datasetNameOriginal+"_WS.json";
		
//		PredictScript ps=new PredictScript();
//		Hashtable<String, Double> htPred = ps.predictForTsv(null, modelId, datasetTsv);
		
//		int countRemoved=0;
		
		List<MappedPropertyValue>propertyValuesRemoved=new ArrayList<>();

		
		int countBefore=countPropertyValues(unifiedPropertyValues);
		

		for (String qsarSmiles:unifiedPropertyValues.keySet()) {
			
			List<MappedPropertyValue>propertyValues=unifiedPropertyValues.get(qsarSmiles);
			Iterator<MappedPropertyValue> it = propertyValues.iterator();
			
			while (it.hasNext()) {
			    MappedPropertyValue mpv = it.next();
				PropertyValue pv=mpv.propertyValue;
				
				String chemicalName = pv.getSourceChemical().getSourceChemicalName();
				

				if(!pv.getKeep()) continue;


				Double durationDays=null;
				ParameterValue parameterValueED=pv.getParameterValue("Exposure duration");
				ParameterValue parameterValueT=pv.getParameterValue("Temperature");
				//TODO skip T80 check if value is kinetic based, e.g. calculation_method ="K1/K2" than "Cb/Cw"				
				
				if(parameterValueED==null) {
//					System.out.println(chemicalName+"\t"+qsarSmiles+"\tExposure duration parameter missing");
					
					if(skipMissing) {
						it.remove(); // safe removal while iterating
//						countRemoved++;
						propertyValuesRemoved.add(mpv);
						pv.setKeepReason("Missing exposure duration");
					}
					
					continue;
				} else if(parameterValueED.getValueText()!=null && parameterValueED.getValueText().equalsIgnoreCase("lifetime")) {
					System.out.println(chemicalName+"\t"+qsarSmiles+"\tassume SS since lifetime of exposure");
					continue;//assume SS since lifetime of exposure
				} else if(parameterValueED.getValuePointEstimate()==null) {
					System.out.println(chemicalName+"\t"+qsarSmiles+"\tMissing exposure duration point estimate");
				} else {
					if(parameterValueED.getUnit().getAbbreviation().equals("days")) {
						durationDays=parameterValueED.getValuePointEstimate();
					} else {
						System.out.println(chemicalName+"\t"+qsarSmiles+"\tExposure duration units= "+parameterValueED.getUnit().getAbbreviation());
						continue;
					}
				}

				Double temperature=21.0;
				if(parameterValueT!=null) {
					if(parameterValueT.getUnit().getAbbreviation().equals("C")) {
						if(parameterValueT.getValuePointEstimate()!=null) {
							temperature=parameterValueT.getValuePointEstimate();
						} else {
							System.out.println(chemicalName+"\t"+"Missing point estimate for temperature, have min/max?");
						}
					} else {
						System.out.println(chemicalName+"\t"+"Invalid temp units:"+ parameterValueT.getUnit().getAbbreviation());
					}
				}
				
				ParameterValue parameterValueT80 = pv.getParameterValue("T80");//previously stored from ChangeKeptPropertyValues.addT80_From_PredictedLogKow
				
				if(!parameterValueT80.getUnit().getAbbreviation().equals("days")) {
					System.out.println(qsarSmiles+"\tT80 units= "+parameterValueT80.getUnit().getAbbreviation());
					continue;
				} 

				double T80=parameterValueT80.getValuePointEstimate();
				
				if (durationDays * factor < T80) {
					it.remove(); // safe removal while iterating
//					System.out.println(chemicalName+"\t"+qsarSmiles+"\t"+df.format(durationDays)+"\t"+df.format(T80));
//					countRemoved++;
					propertyValuesRemoved.add(mpv);
					pv.setKeepReason("durationDays * factor < T80");

				}
			}
		}

//		System.out.println("# removed by exposure duration = "+countRemoved+" of "+countBefore);
//		int countAfter=countPropertyValues(unifiedPropertyValues);
		
		return propertyValuesRemoved;

	}
	
	
	public static void addT80_From_PredictedLogKow(String datasetTsv, Map<String, List<MappedPropertyValue>> unifiedPropertyValues) {

		DecimalFormat df=new DecimalFormat("0.000");
//		DecimalFormat df2=new DecimalFormat("0.000");
		
		long modelId=1069L;//logKow XGB model
		
//		String propertyNameModel=ps.getPropertyNameModel(modelId);

		//Folder for storing prediction hashtable:
//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\ECOTOX_2023_12_14\\";
//		String filePathPreds=folder+datasetNameOriginal+"_WS.json";

		PredictScript ps=new PredictScript();
		Hashtable<String, Double> htPred = ps.predictForTsv(null, modelId, datasetTsv);
		
		for (String qsarSmiles:unifiedPropertyValues.keySet()) {
			
			List<MappedPropertyValue>propertyValues=unifiedPropertyValues.get(qsarSmiles);
			Iterator<MappedPropertyValue> it = propertyValues.iterator();
			
			while (it.hasNext()) {
			    MappedPropertyValue mpv = it.next();
				PropertyValue pv=mpv.propertyValue;
				
				String chemicalName = pv.getSourceChemical().getSourceChemicalName();
				

				if(!pv.getKeep()) continue;


				ParameterValue parameterValueT=pv.getParameterValue("Temperature");
				Double temperature=21.0;

				if(parameterValueT!=null) {
					if(parameterValueT.getUnit().getAbbreviation().equals("C")) {
						if(parameterValueT.getValuePointEstimate()!=null) {
							temperature=parameterValueT.getValuePointEstimate();
						} else {
//							System.out.println(chemicalName+"\t"+"Missing point estimate for temperature, have min/max?");
						}
					} else {
//						System.out.println(chemicalName+"\t"+"Invalid temp units:"+ parameterValueT.getUnit().getAbbreviation());
					}
				}

				double LogKow=htPred.get(qsarSmiles);
				double T80=calcT80(LogKow, temperature);
				
				ParameterValue pvWS=new ParameterValue();
				Parameter pWS=new Parameter();
				ExpPropUnit unit=new ExpPropUnit();
				pWS.setName("T80");
				pvWS.setValuePointEstimate(T80);
				unit.setAbbreviation("days");
				unit.setName("DAYS");
				pvWS.setParameter(pWS);
				pvWS.setUnit(unit);
				pv.addParameterValue(pvWS);
			}
		}
	}
	
	
	/**
	 * time to get to 80% of steady state concentration
	 * 
	 * @param logKow
	 * @param logBCF
	 * @return
	 */
	static double calcT80(double logKow, Double T) {
		
		double W=0.002;
		double Dox=7.1;
		double Gv=980*Math.pow(W,0.65)/Dox;
		double Lb=0.05;
		double NLOMb=0.2;
		double NLOMg=0.24;
		double B=0.035;
		double Gd=0.015*W;
		double Gf=0.5*Gd;
		double Lg=0.012;
		double WCg=0.74;
		double WCb=1-(Lb+NLOMb);
		
		
		double Kow=Math.pow(10,logKow);
		double Ed=1/(3e-7*Kow+2);
		double Kgb=(Lg*Kow + NLOMg*B*Kow +WCg)/(Lb*Kow+NLOMb*B*Kow+WCb);
		double Ew=0.006;
		if(logKow>=0) Ew=1/(1.85+155/Kow);
		
//		double BCF=Math.pow(10,logBCF);
//		double Cb=BCF*Cw_g_L;//organism concentration in g/kg
		
		
		double k1=Ew*Gv/W;
		double k2=k1/(Lb*Kow+NLOMb*Kow*B+WCb);
		double ke=Gf*Ed*Kgb/W;
		double kg=0.00586*Math.pow(1.13,T-20)*Math.pow(1000*W,-0.2);		
		double km=0;//assumed to not be metabolized- not true for esters
		double kt=k2+ke+kg+km;
		
		double t80=1.6/kt;
		
//		System.out.println(t80);
		
		return t80;
		
	}
	
	public static int removeBasedOnExposureDurationAndPredictedLogKow(String datasetNameOriginal, List<PropertyValue> propertyValues) {

		DecimalFormat df=new DecimalFormat("0.00E00");
		DecimalFormat df2=new DecimalFormat("0.0");
		
//		boolean generateNewPredictions=true;
		
		long modelId=1069L;
		PredictScript ps=new PredictScript();
		String propertyNameModel=ps.getPropertyNameModel(modelId);

		//Folder for storing prediction hashtable:
//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\ECOTOX_2023_12_14\\";
//		String filePathPreds=folder+datasetNameOriginal+"_WS.json";

		Hashtable<String, Double> htPred = getPredictionHashtable(datasetNameOriginal,modelId, ps,
				propertyNameModel);

		
		List<DsstoxRecord>records=PredictScript.getDsstoxRecords();
		Hashtable<String, DsstoxRecord> htDsstox=PredictScript.getDsstoxHashtableByCASRN(records);

		int countBefore=propertyValues.size();

		for (int i=0;i<propertyValues.size();i++) {

			PropertyValue pv=propertyValues.get(i);

			if(!pv.getKeep()) continue;

			String chemicalName=pv.getSourceChemical().getSourceChemicalName();

//			String dtxsid=pv.getSourceChemical().getSourceDtxsid();			

			String CAS=pv.getSourceChemical().getSourceCasrn();
			
			
//			String dtxsid=pv.getSourceChemical().getSourceDtxsid();
			
			if(CAS==null) {
				System.out.println("Missing CAS for "+chemicalName);
				continue;
			}
			
			if(htDsstox.get(CAS)==null || htDsstox.get(CAS).getDtxsid()==null) {
				System.out.println(CAS+ " missing in dsstox records");
				continue;
			}
			
			DsstoxRecord dsstoxRecord =htDsstox.get(CAS);


			if(!htPred.containsKey(dsstoxRecord.getDtxsid())) {
//				System.out.println("prediction hashtable missing "+CAS);
				continue;
			}
			
			ParameterValue parameterValueCriterionED=pv.getParameterValue("Criterion 4- Exposure Duration");
			
			String criterionED=null;
			if(parameterValueCriterionED!=null) {
				criterionED=parameterValueCriterionED.getValueText();
			}

			Double exposureDurationDays=null;
			
			
			
			if(pv.getParameterValue("Exposure Duration (in days or Lifetime)")==null) {
				System.out.println(CAS+"\tExposure duration unavailable\tcriterionWS="+criterionED);
				continue;
			} else {
				ParameterValue parameterValue=pv.getParameterValue("Exposure Duration (in days or Lifetime)");
				
				if(parameterValue.getValueText().equals("Lifetime")) {
					System.out.println("Lifetime exposure\t"+criterionED);
					continue;
				
				} else if(parameterValue.getValueText().equals("N/A")) {
					System.out.println("Duration=N/A\t"+criterionED);
					continue;
				} else {
					
					try {
						exposureDurationDays=Double.parseDouble(parameterValue.getValueText());
					} catch (Exception ex) {
						System.out.println("Failed to parse exposure duration="+parameterValue.getValueText());
						continue;
					}
				}
			}
			
			
			double LogKow=htPred.get(dsstoxRecord.getDtxsid());
			double t80=calcT80(LogKow, 21.0);
			
//			if(WaterConc_g_L>10.0*wsValue_g_L) {				
//				System.out.println(dtxsid+"\t"+df.format(WaterConc_g_L)+"\t"+df.format(wsValue_g_L)+"\tWS ratio="+df2.format(WaterConc_g_L/wsValue_g_L));
//				propertyValues.remove(i--);
//			}
			
			boolean failsED=t80>exposureDurationDays;
			boolean failsCriterion=criterionED.contains("3");
			boolean match=failsED==failsCriterion;
				
			double ratio=t80/exposureDurationDays;
			
			//108-70-3 8 days
			
			if(!match && failsCriterion) {				
				System.out.println(CAS+"\t"+LogKow+"\t"+df.format(exposureDurationDays)+"\t"+df.format(t80)+"\t"+df.format(ratio)+"\t"+criterionED);
//				System.out.println(CAS+"\t"+df.format(ratio)+"\t"+criterionED);
			}
		}
		
		int countAfter=propertyValues.size();
		return countBefore-countAfter;

	}

	

//	static double calcT80(double logKow) {
//		
//		double W=0.002;
//		double Dox=7.1;
//		double Gv=980*Math.pow(W,0.65)/Dox;
//		double Lb=0.05;
//		double NLOMb=0.2;
//		double NLOMg=0.24;
//		double B=0.035;
//		double Gd=0.015*W;
//		double Gf=0.5*Gd;
//		double Lg=0.012;
//		double WCg=0.74;
//		double WCb=1-(Lb+NLOMb);
//		double T=21;
//		
//		double Kow=Math.pow(10,logKow);
//		double Ed=1/(3e-7*Kow+2);
//		double Kgb=(Lg*Kow + NLOMg*B*Kow +WCg)/(Lb*Kow+NLOMb*B*Kow+WCb);
//		double Ew=0.006;
//		if(logKow>=0) Ew=1/(1.85+155/Kow);
//		
////		double BCF=Math.pow(10,logBCF);
////		double Cb=BCF*Cw_g_L;//organism concentration in g/kg
//		
//		
//		double k1=Ew*Gv/W;
//		double k2=k1/(Lb*Kow+NLOMb*Kow*B+WCb);
//		double ke=Gf*Ed*Kgb/W;
//		double kg=0.00586*Math.pow(1.13,T-20)*Math.pow(1000*W,-0.2);		
//		double km=0;//assumed to not be metabolized- not true for esters
//		double kt=k2+ke+kg+km;
//		
//		double t80=1.6/kt;
//		
////		System.out.println(t80);
//		
//		return t80;
//		
//	}

	
	
	public static int removeBasedOnBaselineToxicity(String datasetName, List<PropertyValue> propertyValues,String typeAnimal) {
		
		boolean generateNewPredictions=true;
		
		long modelId=1069L;//logKow XGB model
		PredictScript ps=new PredictScript();
		String propertyNameModel=ps.getPropertyNameModel(modelId);
		
		//Folder for storing prediction hashtable:
//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\ECOTOX_2023_12_14\\";
//		String filePathPreds=folder+propertyNameDataset+"_LogKow.json";
//		String filePathPreds=folder+datasetName+"_LogKow.json";
		
//		Hashtable<String, Double> htPred = getPredictionHashtable(datasetName, generateNewPredictions, modelId, ps,
//				propertyNameModel,filePathPreds);

		Hashtable<String, Double> htPred = getPredictionHashtable(datasetName, modelId, ps,
				propertyNameModel);

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
		
				
				Double pointEstimate=null;

				if(pv.getValuePointEstimate()==null) {
					if(pv.getValueMin()!=null && pv.getValueMax()!=null) {
						double diff=Math.abs(pv.getValueMax()-pv.getValueMin());
						
						if(diff<=1) {
							pointEstimate=(pv.getValueMin()+pv.getValueMax())/2.0;
						}
					} 
//					System.out.println(pv.getValueMin()+"\t"+pv.getValueMax()+"\t"+pv.getUnit().getAbbreviation());
				} else {
					pointEstimate=pv.getValuePointEstimate();
				}
				
				if(pointEstimate==null)continue;//dont check against baseline tox		
								
				double mol_weight=htDsstox.get(dtxsid).getMolWeight();
				
				if(pv.getUnit().getName().equals("MOLAR")) {
					toxValue_g_L=pointEstimate*mol_weight;
				} else if(pv.getUnit().getName().equals("G_L")) {
					toxValue_g_L=pointEstimate;
				} else {
					System.out.println(pv.getUnit().getAbbreviation()+"\tnot handled");
					continue;
				}
								
				Double logKowPred=htPred.get(dtxsid);
//				System.out.println(dtxsid+"\t"+logKowPred);
								
				Double BaseLineTox_Log_mmol_L=null;
				
				if (typeAnimal.equalsIgnoreCase(typeAnimalFish)) {
					//ECOSAR manual, Baseline Toxicity Equation for Fish:
					BaseLineTox_Log_mmol_L=-0.8981*logKowPred + 1.7108;	
				} else if (typeAnimal.equalsIgnoreCase(typeAnimalFatheadMinnow)) {					

					//FHM model for nonpolar compounds, Nendza and Russom, 1991:
					BaseLineTox_Log_mmol_L=-0.79*logKowPred + 1.35;
					//Note this ends up excluding some records for methanol!
					//Is there a better model for FHM

				} else if (typeAnimal.equalsIgnoreCase(typeAnimalDaphnid)) {

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
	
	
	public static int removeBasedOnBaselineToxicity(String datasetNameOriginal,
			Map<String, List<MappedPropertyValue>> unifiedPropertyValues, String typeAnimal) {

		boolean generateNewPredictions=true;

		long modelId=1069L;//logKow XGB model
		PredictScript ps=new PredictScript();
		String propertyNameModel=ps.getPropertyNameModel(modelId);

		//Folder for storing prediction hashtable:
		//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\ECOTOX_2023_12_14\\";
		//		String filePathPreds=folder+propertyNameDataset+"_LogKow.json";
		//		String filePathPreds=folder+datasetName+"_LogKow.json";

		//		Hashtable<String, Double> htPred = getPredictionHashtable(datasetName, generateNewPredictions, modelId, ps,
		//				propertyNameModel,filePathPreds);

		
		//TODO Map of logKow preds by dtxsid- probably would work better if used canonSmiles instead
		
		Hashtable<String, Double> htPred_logKow = getPredictionHashtable(datasetNameOriginal, modelId, ps,
				propertyNameModel);

		List<DsstoxRecord>records=PredictScript.getDsstoxRecords();
		Hashtable<String, DsstoxRecord> htDsstox=PredictScript.getDsstoxHashtableByDTXSID(records);

		List<String>omittedDtxsids=new ArrayList<>();
		List<String>omittedNames=new ArrayList<>();

		System.out.println("dtxsid\ttoxValue_g_L\t10*BaseLineTox_g_L");

		int countBefore=0;
		for (String key:unifiedPropertyValues.keySet()) {
			List<MappedPropertyValue>listMPV=unifiedPropertyValues.get(key);
			countBefore+=listMPV.size();
		}


		DecimalFormat df=new DecimalFormat("0.00");

		Iterator<Map.Entry<String, List<MappedPropertyValue>>> iterator = unifiedPropertyValues.entrySet().iterator();
		
		while (iterator.hasNext()) {
						
			Map.Entry<String, List<MappedPropertyValue>> entry = iterator.next();
			
			List<MappedPropertyValue>listMPV=entry.getValue();

			for(int j=0;j<listMPV.size();j++) {

				MappedPropertyValue mpv=listMPV.get(j);
				String dtxsid=mpv.dsstoxRecord.dsstoxSubstanceId;

				if(htPred_logKow.containsKey(dtxsid)) {

//					double mol_weight=htDsstox.get(dtxsid).getMolWeight();

					double toxValueNegLogM=mpv.qsarPropertyValue;//should be in -logM

					Double logKowPred=htPred_logKow.get(dtxsid);
					//					System.out.println(dtxsid+"\t"+logKowPred);

					Double BaseLineTox_Log_mmol_L=null;

					if (typeAnimal.equalsIgnoreCase(typeAnimalFish)) {
						//ECOSAR manual, Baseline Toxicity Equation for Fish:
						BaseLineTox_Log_mmol_L=-0.8981*logKowPred + 1.7108;	
					} else if (typeAnimal.equalsIgnoreCase(typeAnimalFatheadMinnow)) {					

						//FHM model for nonpolar compounds, Nendza and Russom, 1991:
						BaseLineTox_Log_mmol_L=-0.79*logKowPred + 1.35;
						//Note this ends up excluding some records for methanol!
						//Is there a better model for FHM

					} else if (typeAnimal.equalsIgnoreCase(typeAnimalDaphnid)) {

						//						ECOSAR manual, Baseline Toxicity Equation for Daphnid:
						BaseLineTox_Log_mmol_L=-0.8580*logKowPred + 1.3848;
					} else {
						System.out.println("Unknown animal type");
						return -9999;
					}

					double BaseLineTox_mmol_L=Math.pow(10.0, BaseLineTox_Log_mmol_L);
					double BaseLineTox_mol_L=BaseLineTox_mmol_L/1000.0;				
					double BaseLineToxNegLogM=-Math.log10(BaseLineTox_mol_L);

					if(toxValueNegLogM<BaseLineToxNegLogM-1) {
//						System.out.println(dtxsid+"\t"+df.format(toxValueNegLogM)+"\t"+df.format(BaseLineToxNegLogM)+"\texp tox > 10*baseline tox");
						listMPV.remove(j--);
					}
				
				} else {
					//					if(!omittedDtxsids.contains(dtxsid)) omittedDtxsids.add(dtxsid);
					//					if(!omittedNames.contains(chemicalName)) omittedNames.add(chemicalName);
					System.out.println(dtxsid+"\tNo logKow prediction in hashtable");
				}
			}//end loop over MappedPropertyValues

			if(listMPV.size()==0) iterator.remove();
		}

		//			System.out.println(pv.getUnit().getName());

		int countAfter=0;
		for (String key:unifiedPropertyValues.keySet()) {
			List<MappedPropertyValue>listMPV=unifiedPropertyValues.get(key);
			countAfter+=listMPV.size();
		}

		return countBefore-countAfter;
	}

	
	public static List<MappedPropertyValue> removeBasedOnBaselineToxicity2(
	        String datasetTsv,
	        Map<String, List<MappedPropertyValue>> unifiedPropertyValues,
	        String typeAnimal) {

	    boolean generateNewPredictions = true;

	    long modelId = 1069L; // logKow XGB model
	    PredictScript ps = new PredictScript();
	    String propertyNameModel = ps.getPropertyNameModel(modelId);

	    // Predictions keyed by canon_qsar_smiles (as in your code)
	    Hashtable<String, Double> htPred_logKow = ps.predictForTsv(null, modelId, datasetTsv);

	    List<DsstoxRecord> records = PredictScript.getDsstoxRecords();
	    Hashtable<String, DsstoxRecord> htDsstox = PredictScript.getDsstoxHashtableByDTXSID(records);

	    List<String> omittedDtxsids = new ArrayList<>();
	    List<String> omittedNames = new ArrayList<>();

	    System.out.println("dtxsid\ttoxValue_g_L\t10*BaseLineTox_g_L");

	    // Count before
	    int countBefore = 0;
	    for (List<MappedPropertyValue> listMPV : unifiedPropertyValues.values()) {
	        countBefore += listMPV.size();
	    }

	    // Iterate the map with an iterator to allow safe removal of entries
	    Iterator<Map.Entry<String, List<MappedPropertyValue>>> itMap =
	            unifiedPropertyValues.entrySet().iterator();
	    
	    List<MappedPropertyValue>discarded=new ArrayList<>();

	    while (itMap.hasNext()) {
	        Map.Entry<String, List<MappedPropertyValue>> entry = itMap.next();
	        String qsarSmiles = entry.getKey();
	        List<MappedPropertyValue> propertyValues = entry.getValue();

	        // If prediction missing or null, skip this chemical
	        Double logKowPred = htPred_logKow.get(qsarSmiles);
	        if (logKowPred == null) {
	            System.out.println(qsarSmiles + "\tNo logKow prediction in hashtable");
	            continue;
	        }

	        // Safe removal from the list while iterating
	        Iterator<MappedPropertyValue> iteratorPropertyValue = propertyValues.iterator();
	        while (iteratorPropertyValue.hasNext()) {
	            MappedPropertyValue mpv = iteratorPropertyValue.next();
	            PropertyValue pv = mpv.propertyValue;

	            // toxValueNegLogM should be in -logM
	            double toxValueNegLogM = mpv.qsarPropertyValue;

	            Double baseLineTox_Log_mmol_L;
	            if (typeAnimal.equalsIgnoreCase(typeAnimalFish)) {
	                // ECOSAR manual, Baseline Toxicity Equation for Fish:
	                baseLineTox_Log_mmol_L = -0.8981 * logKowPred + 1.7108;
	            } else if (typeAnimal.equalsIgnoreCase(typeAnimalFatheadMinnow)) {
	                // FHM model for nonpolar compounds, Nendza and Russom, 1991:
	                baseLineTox_Log_mmol_L = -0.79 * logKowPred + 1.35;
	            } else if (typeAnimal.equalsIgnoreCase(typeAnimalDaphnid)) {
	                // ECOSAR manual, Baseline Toxicity Equation for Daphnid:
	                baseLineTox_Log_mmol_L = -0.8580 * logKowPred + 1.3848;
	            } else {
	                System.out.println("Unknown animal type");
	                continue;
	            }

	            double baseLineTox_mmol_L = Math.pow(10.0, baseLineTox_Log_mmol_L);
	            double baseLineTox_mol_L = baseLineTox_mmol_L / 1000.0;
	            double baseLineToxNegLogM = -Math.log10(baseLineTox_mol_L);

	            // Remove if experimental toxicity > 10 * baseline toxicity (1 log unit lower than baseline on -logM)
	            if (toxValueNegLogM < baseLineToxNegLogM - 1) {
	                iteratorPropertyValue.remove();
	                discarded.add(mpv);
	                
		            // Attach baseline toxicity as a parameter for traceability
		            ParameterValue parameterValue = new ParameterValue();
		            Parameter parameter = new Parameter();
		            ExpPropUnit unit = new ExpPropUnit();
		            parameter.setName("baseline acute fish toxicity");
		            parameterValue.setValuePointEstimate(baseLineToxNegLogM);
		            unit.setAbbreviation(DevQsarConstants.NEG_LOG_M);
		            unit.setName("NEG_LOG_M");
		            parameterValue.setParameter(parameter);
		            parameterValue.setUnit(unit);
		            mpv.propertyValue.addParameterValue(parameterValue);
		            
		            pv.setKeepReason("Failed baseline toxicity check");

	            }

	            
	        }

	        // If this chemical has no remaining property values, remove it from the map safely
	        if (propertyValues.isEmpty()) {
	            itMap.remove();
	        }
	    }

	    return discarded;
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


	private  static Hashtable<String, Double> getPredictionHashtable(String datasetName, 
			long modelId, PredictScript ps, String propertyNameModel) {
		return ps.predict(null,modelId, datasetName);
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
	
	public static int removeBasedOnConcentrationType(List<PropertyValue> propertyValues) {
		
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



	public static int removeBasedOnResponseSite(List<PropertyValue> propertyValues, String typeAnimal) {
		// TODO Auto-generated method stub
		return 0;
	}


	public static int removeBasedOnObservationDays(List<PropertyValue> propertyValues,Double durationDays) {

		int countBefore=propertyValues.size();
		
		for (int i=0;i<propertyValues.size();i++) {
			
			PropertyValue pv=propertyValues.get(i);

			String dtxsid=pv.getSourceChemical().getSourceDtxsid();
			
			for (ParameterValue parameterValue:pv.getParameterValues()) {
				
				if(!parameterValue.getParameter().getName().equals("Observation duration"))continue;
				
				if(!parameterValue.getUnit().getAbbreviation().equals("days")) {
					System.out.println("observation duration unit = "+parameterValue.getUnit().getAbbreviation());
					propertyValues.remove(i--);
					break;
				}
				
				if(parameterValue.getValuePointEstimate()==null) {
					if(parameterValue.getValueMin()!=null && parameterValue.getValueMax()!=null) {
						parameterValue.setValuePointEstimate((parameterValue.getValueMin()+parameterValue.getValueMax())/2.0);
//						System.out.println("Obs duration from min and max="+parameterValue.getValuePointEstimate());
					} else {
//						System.out.println("Missing observation duration");
						propertyValues.remove(i--);
						break;
					}
				}
				
				
				double diff=Math.abs(durationDays-parameterValue.getValuePointEstimate());
				
				if(diff>0.1) {
//					System.out.println("observation duration = "+parameterValue.getValuePointEstimate()+" "+parameterValue.getUnit().getAbbreviation());
					propertyValues.remove(i--);
					break;
				}
			}
		}
		
		int countAfter=propertyValues.size();
		return countBefore-countAfter;
	}


	/**
	 * Filters propertyValues based on a parameter
	 * TODO currently it doesnt omit if the parameter is missing
	 * 
	 * 
	 * @param propertyValues
	 * @param parameterName
	 * @param parameterText
	 * @return
	 */
	public static int removeBasedOnParameterText(List<PropertyValue> propertyValues,
			String parameterName,String parameterText) {

		int countBefore=propertyValues.size();
		for (int i=0;i<propertyValues.size();i++) {
			
			PropertyValue pv=propertyValues.get(i);
			String dtxsid=pv.getSourceChemical().getSourceDtxsid();
			
			for (ParameterValue parameterValue:pv.getParameterValues()) {
				
				if(!parameterValue.getParameter().getName().equals(parameterName)) continue;
				
				String parameterValueText=parameterValue.getValueText().toLowerCase();
				
				if(!parameterValueText.equalsIgnoreCase(parameterText)) {
//					System.out.println(dtxsid+"\t"+parameterName+"="+parameterValueText);
					propertyValues.remove(i--);
					break;
				}
				
			}
		}
		int countAfter=propertyValues.size();
		
		return countBefore-countAfter;
	}
	
	
	/**
	 * 
	 * @param propertyValues
	 * @param parameterName
	 * @param acceptableParameterValues
	 * @param removeIfMissing
	 * @return
	 */
	public static int removeBasedOnParameterText(
	        List<PropertyValue> propertyValues,
	        String parameterName,
	        List<String> acceptableParameterValues,
	        boolean removeIfMissing) {
	
	    int countBefore = propertyValues.size();
	
	    for (Iterator<PropertyValue> it = propertyValues.iterator(); it.hasNext();) {
	        PropertyValue pv = it.next();
	
	        boolean haveParameterValue = false;
	        boolean removeCurrent = false;
	
	        for (ParameterValue parameterValue : pv.getParameterValues()) {
	            if (!parameterValue.getParameter().getName().equals(parameterName)) {
	                continue;
	            }
	
	            haveParameterValue = true;
	            String parameterValueText = parameterValue.getValueText();//TODO is this ever null?
	            
	            if(!acceptableParameterValues.contains(parameterValueText.toLowerCase())) {
	                removeCurrent = true;
	                break;
	            }
	        }
	
	        if (removeCurrent || (removeIfMissing && !haveParameterValue)) {
	            it.remove();
	        }
	    }
	
	    return countBefore - propertyValues.size();
	}	
	public static int removeBasedOnParameterText(List<PropertyValue> propertyValues,
			String parameterName,List<String> parameterValues) {

		int countBefore=propertyValues.size();
		for (int i=0;i<propertyValues.size();i++) {
			
			PropertyValue pv=propertyValues.get(i);
			String dtxsid=pv.getSourceChemical().getSourceDtxsid();
			
			for (ParameterValue parameterValue:pv.getParameterValues()) {
				
				if(!parameterValue.getParameter().getName().equals(parameterName)) continue;
				
				String parameterValueText=parameterValue.getValueText();

				boolean haveMatch=false;
				for (String text:parameterValues) {
					if(text.equalsIgnoreCase(parameterValueText)) {
//						System.out.println(dtxsid+"\t"+parameterName+"="+parameterValueText);
						haveMatch=true;
						break;
					}
				}
				
				if(!haveMatch) {
					propertyValues.remove(i--);
					break;
				}
				
			}
		}
		int countAfter=propertyValues.size();
		
		return countBefore-countAfter;
	}

	


}
