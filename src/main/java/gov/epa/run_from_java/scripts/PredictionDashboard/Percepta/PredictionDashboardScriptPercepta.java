package gov.epa.run_from_java.scripts.PredictionDashboard;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxSnapshot;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.Source;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxRecordServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxSnapshotServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.MethodServiceImpl;

import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceService;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

/**
* @author TMARTI02
*/
public class PredictionDashboardScriptPercepta {
	MethodServiceImpl methodService=new MethodServiceImpl();
	PredictionDashboardServiceImpl predictionDashboardService=new PredictionDashboardServiceImpl();

	HashMap<String,String>hmModelNameToPropertyName;
	
	String lanId="tmarti02";
	String version="2020.2.1";


	PredictionDashboardScriptPercepta(){
		hmModelNameToPropertyName=getModelNameToPropertyNameMap();
	}
	
	private HashMap<String, Model> createModels(String version, HashMap<String, Method> hmMethods) {
		
		String sourceName="Percepta"+version;
		String descriptorSetName="Percepta "+version;
		String splittingName="Percepta";
		String methodName="percepta";

		SourceService ss=new SourceServiceImpl();
		Source source=ss.findByName(sourceName);

		
		HashMap<String,Model> hmModels=new HashMap<>();
		
		for (String modelName:hmModelNameToPropertyName.keySet()) {
			String propertyNameDB=hmModelNameToPropertyName.get(modelName);
			String datasetName=getDatasetName(propertyNameDB);
//			System.out.println(modelName+"\t"+propertyNameDB);
			Model model=new Model(modelName,hmMethods.get(methodName), null,descriptorSetName, datasetName, splittingName, source,lanId);
			model=CreatorScript.createModel(model);
			hmModels.put(modelName, model);
		}
		return hmModels;
	}
	
	
	void createDatasets() {

		//	HashMap<String, String>hmUnitsDataset=DevQsarConstants.getDatasetFinalUnitsMap();
		HashMap<String, String>hmUnitsDatasetContributor=DevQsarConstants.getContributorUnitsNameMap();


		for (String modelName:hmModelNameToPropertyName.keySet()) {
			String propertyNameDB=hmModelNameToPropertyName.get(modelName);
			
			String datasetName = getDatasetName(propertyNameDB);

			String unitAbbrev=hmUnitsDatasetContributor.get(propertyNameDB);

			if(unitAbbrev==null) {
				System.out.println(propertyNameDB+"\t"+datasetName+"\tMissing units skipping");
				continue;
			} 

			Unit unit=CreatorScript.createUnit(unitAbbrev,lanId);
			Unit unitContributor=CreatorScript.createUnit(unitAbbrev,lanId);

			String propertyDescriptionDB=DevQsarConstants.getPropertyDescription(propertyNameDB);

			
			if(propertyDescriptionDB.contains("*"))
				System.out.println(propertyNameDB+"\t"+propertyDescriptionDB);
			
			Property property=CreatorScript.createProperty(propertyNameDB, propertyDescriptionDB,lanId);

			String datasetDescription=propertyNameDB+" dataset from Percepta"+version;
//			//		System.out.println(datasetName+"\t"+datasetDescription);
//
			String dsstoxMappingStrategy=null;

			Dataset dataset=new Dataset(datasetName, datasetDescription, property, unit, unitContributor,
					dsstoxMappingStrategy, lanId);

			CreatorScript.createDataset(dataset);

			System.out.println(Utilities.gson.toJson(dataset));
		}



	}

	private String getDatasetName(String propertyNameDB) {
		String datasetName=propertyNameDB+" Percepta"+version;
		return datasetName;
	}

	void loadFromSDF(String filepathSDF,boolean skipMissingSID,int maxCount) {

		DsstoxSnapshotServiceImpl snapshotService=new  DsstoxSnapshotServiceImpl();
		DsstoxSnapshot snapshot=snapshotService.findByName("DSSTOX Snapshot 04/23");
		DsstoxRecordServiceImpl dsstoxRecordService=new  DsstoxRecordServiceImpl();
		Hashtable<String,Long> htCIDtoDsstoxRecordId=dsstoxRecordService.getRecordIdHashtable(snapshot,"dtxcid");
		
		SmilesGenerator sg= new SmilesGenerator(SmiFlavor.Unique);
		
		DashboardPredictionUtilities dpu = new DashboardPredictionUtilities();

		AtomContainerSet acs=dpu.readSDFV3000(filepathSDF);
		AtomContainerSet acs2 = dpu.filterAtomContainerSet(acs, skipMissingSID, maxCount);

		try {
			
			HashMap<String,Method> hmMethods=new HashMap<>();
			hmMethods.put("percepta",methodService.findByName("percepta"));
			
			HashMap<String, Model> hmModels = createModels(version, hmMethods);
			
			Iterator<IAtomContainer> iterator= acs2.atomContainers().iterator();

			int count=0;
			
			List<PredictionDashboard>predictions=new ArrayList<>();
			
			int batchSizePost=10000;
			
			
			while (iterator.hasNext()) {
				count++;
				
				AtomContainer ac=(AtomContainer) iterator.next();
				
//				System.out.println(Utilities.gson.toJson(ac.getProperties()));
				
				for (String modelName:hmModelNameToPropertyName.keySet()) {
					String propertyNameDB=hmModelNameToPropertyName.get(modelName);
					
//					System.out.println(modelName+"\t"+propertyNameDB);
					
					if (ac.getProperty(modelName)==null) continue;
					
					PredictionDashboard pd=new PredictionDashboard();
					
					pd.setCreatedBy(lanId);
					pd.setCanonQsarSmiles("N/A");
					
					String dtxcid=ac.getProperty("DTXCID");
					
					DsstoxRecord dr=new DsstoxRecord();
					dr.setId(htCIDtoDsstoxRecordId.get(dtxcid));
					pd.setDsstoxRecord(dr);
					
					pd.setModel(hmModels.get(modelName));

					if (ac.getProperty(modelName).equals("Nan")) {
						pd.setPredictionValue(Double.NaN);
					} else {
						double propertyValue=Double.parseDouble(ac.getProperty(modelName));
						pd.setPredictionValue(propertyValue);
					}
					
//					System.out.println(predictions.size()+"\t"+modelName+"\t"+propertyNameDB+"\t"+propertyValue);
//					predictionDashboardService.create(pd);
					predictions.add(pd);
					
					if(predictions.size()==batchSizePost) {
						predictionDashboardService.createSQL(predictions);
						predictions.clear();
					}
				}
			}
			
			predictionDashboardService.createSQL(predictions);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	
	
	
	
//Model names in prod_qsar:
//*ACD_Prop_Density
//*ACD_BP
//*ACD_VP
//*ACD_Sol =>ACD_SolInPW_v_LogP_Classic_And_v_Pka_Classic
//*ACD_Prop_Molar_Refractivity
//*ACD_FP
//*ACD_Prop_Molar_Volume
//*ACD_Prop_Polarizability
//*ACD_Prop_Parachor\	
//*ACD_Prop_Index_Of_Refraction
//*ACD_Prop_Surface_Tension
//*ACD_LogP_v_LogP_Classic
//*ACD_Prop_Dielectric_Constant
// These are in prod_qsar- but dont seem them on the dashboard not 100% sure which fields these are in the output
//	ACD_pKa_Basic
//	ACD_pKa_Acidic

//***************************************************************************
//properties in output:
//*ACD_Prop_Density
//*ACD_BP
//*ACD_FP
//*	ACD_VP
//*	ACD_Prop_Index_Of_Refraction
//*	ACD_Prop_Molar_Refractivity
//*	ACD_Prop_Molar_Volume
//*	ACD_Prop_Parachor
//*	ACD_Prop_Polarizability
//*	ACD_Prop_Surface_Tension
//*	ACD_LogP_v_LogP_Classic
//*	ACD_LogP_v_LogP_Consensus
//*	ACD_SolInPW_v_LogP_Classic_And_v_Pka_Classic
//*	ACD_LogD_v_LogP_Consensus_And_v_Pka_Classic

//	ACD_Prop_MW
//	ACD_Enthalpy
//	ACD_VP_Temp
//	ACD_SolInPW_pH_v_LogP_Classic_And_v_Pka_Classic
//	ACD_LogD_pH

//	ACD_RuleOf5_FRB
//	ACD_RuleOf5_HAcceptors
//	ACD_RuleOf5_HDonors
//	ACD_RuleOf5_MW
//	ACD_RuleOf5_PSA
//	ACD_RuleOf5_PSANoBiS
//	ACD_RuleOf5_v_LogP_Classic
//	ACD_RuleOf5_v_LogP_Consensus
//	ACD_Enthalpy
//	ACD_BP_Press
//	ACD_MolChar_C_ratio
//	ACD_MolChar_Halogen_ratio
//	ACD_MolChar_Hetero_ratio
//	ACD_MolChar_N_ratio
//	ACD_MolChar_NO_ratio
//	ACD_MolChar_Num_Aromatic_Rings
//	ACD_MolChar_Num_Rings_3
//	ACD_MolChar_Num_Rings_4
//	ACD_MolChar_Num_Rings_5
//	ACD_MolChar_Num_Rings_6
//	ACD_MolChar_Num_Rings
	//ACD_pKa_Apparent_v_Pka_Classic_1
	//ACD_pKa_Apparent_v_Pka_Classic_2

	
	HashMap<String,String>getModelNameToPropertyNameMap() {
		
		HashMap<String,String>hm=new HashMap<>();
		
		hm.put("ACD_Prop_Density",DevQsarConstants.DENSITY);
		hm.put("ACD_BP",DevQsarConstants.BOILING_POINT);
		hm.put("ACD_VP",DevQsarConstants.VAPOR_PRESSURE);
		hm.put("ACD_FP",DevQsarConstants.FLASH_POINT);
		hm.put("ACD_SolInPW_v_LogP_Classic_And_v_Pka_Classic",DevQsarConstants.WATER_SOLUBILITY);
		hm.put("ACD_Prop_Molar_Refractivity",DevQsarConstants.MOLAR_REFRACTIVITY);
		hm.put("ACD_Prop_Molar_Volume",DevQsarConstants.MOLAR_VOLUME);
		hm.put("ACD_Prop_Polarizability",DevQsarConstants.POLARIZABILITY);
		hm.put("ACD_Prop_Index_Of_Refraction",DevQsarConstants.INDEX_OF_REFRACTION);
		hm.put("ACD_Prop_Surface_Tension",DevQsarConstants.SURFACE_TENSION);
		hm.put("ACD_LogP_v_LogP_Classic",DevQsarConstants.LOG_KOW);
		hm.put("ACD_LogP_v_LogP_Consensus",DevQsarConstants.LOG_KOW);
		hm.put("ACD_Prop_Dielectric_Constant",DevQsarConstants.DIELECTRIC_CONSTANT);
		hm.put("ACD_LogD_v_LogP_Consensus_And_v_Pka_Classic",DevQsarConstants.LogD_pH_7_4);
//		hm.put("ACD_Prop_Parachor",DevQsarConstants.PARACHOR);//not of interest
		
		
		//Need to figure this out- which is which? Not ont he dashboard as far as I can tell
		//We arent allowed to expose pKA to dashboard according to tony
		//ACD_pKa_Apparent_v_Pka_Classic_1
		//ACD_pKa_Apparent_v_Pka_Classic_2

//		ACD_pKa_Basic - in prod_qsar but not on dashboard?
//		ACD_pKa_Acidic
		
		return hm;
	}
	
	public static void main(String[] args) {
		PredictionDashboardScriptPercepta p=new PredictionDashboardScriptPercepta();

//		p.createDatasets();

//		for (int i=6;i<=6;i++) {
		for (int i=1;i<=35;i++) {
			String filename="snapshot_compounds"+i+"_PERCEPTA.SDF";
			System.out.println(filename);
			String filepathJson="data\\dsstox\\percepta\\"+filename;
			p.loadFromSDF(filepathJson,true,-1);
		}
	}
	

	
}
