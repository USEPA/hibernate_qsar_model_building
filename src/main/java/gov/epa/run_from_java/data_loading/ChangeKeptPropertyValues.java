package gov.epa.run_from_java.data_loading;

import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.service.*;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.endpoints.datasets.DatasetCreator;

import gov.epa.run_from_java.scripts.PredictScript;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

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
			
			if(toxValue_g_L>10.0*wsValue_g_L) {				
				System.out.println(dtxsid+"\t"+df.format(toxValue_g_L)+"\t"+df.format(wsValue_g_L)+"\tWS ratio="+df2.format(toxValue_g_L/wsValue_g_L));
				
				propertyValues.remove(i--);
			}
		}
		
		int countAfter=propertyValues.size();
		
		return countBefore-countAfter;

	}
	
	
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
			double t80=calcT80(LogKow);
			
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

	

	static double calcT80(double logKow) {
		
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
		double T=21;
		
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
		Hashtable<String, Double>htPred=null;

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
