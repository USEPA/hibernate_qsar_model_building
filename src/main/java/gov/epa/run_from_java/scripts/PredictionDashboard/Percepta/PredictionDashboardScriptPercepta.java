package gov.epa.run_from_java.scripts.PredictionDashboard.Percepta;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesGenerator;

import com.google.gson.Gson;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxSnapshot;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport;
import gov.epa.databases.dev_qsar.qsar_models.entity.Source;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxRecordServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxSnapshotServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.MethodServiceImpl;

import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceService;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceServiceImpl;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.service.DsstoxCompoundServiceImpl;
import gov.epa.run_from_java.scripts.DSSTOX_Loading.DSSTOX_Compounds_Script;

import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.CreatorScript;
import gov.epa.run_from_java.scripts.PredictionDashboard.DashboardPredictionUtilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.DatabaseUtilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionDashboardTableMaps;
import gov.epa.run_from_java.scripts.PredictionDashboard.CompareToChemprop;
import gov.epa.util.StructureUtil;

/**
 * 
 *
 * count missing	dtxsid
0	DTXSID801000063
1	DTXSID80306012
2	DTXSID001001205
3	DTXSID001000071
4	DTXSID001000207
5	DTXSID001000326
6	DTXSID001006479
7	DTXSID001000457
8	DTXSID001047388
9	DTXSID001014929
10	DTXSID001001150
12	DTXSID001000419
14	DTXSID001000007
16	DTXSID001000152

TODO make sure this loads records after the code refactoring on 1/23/25

 * 
 * 
 * @author TMARTI02
 */
public class PredictionDashboardScriptPercepta {
	MethodServiceImpl methodService = new MethodServiceImpl();
	PredictionDashboardServiceImpl predictionDashboardService = new PredictionDashboardServiceImpl();
	DsstoxRecordServiceImpl dsstoxRecordService = new DsstoxRecordServiceImpl();
	HTMLReportCreatorPercepta htmlCreator=new HTMLReportCreatorPercepta();

	//	HashMap<String, String> hmModelNameToPropertyName;

	String lanId = "tmarti02";
	//	String version="2020.2.1";
	String version = "2023.1.2";

	Gson gson = new Gson();

	InitializeDB initializeDB=new InitializeDB();
	Converter converter=new Converter();
	UtilityMethods utilities=new UtilityMethods();
	
	
	private class Converter {

		public void convertPerceptaResultsToPredictionDashboard(List<Model> models, List<PredictionDashboard> allResults,
				AtomContainer ac, Hashtable<String, DsstoxRecord> htDTXCIDtoDsstoxRecord,
				Hashtable<String, Dataset> htModelNameToDataset,HashSet<String>loadedKeys, boolean saveReportsToHarddrive) {
		
			String dtxsid = ac.getProperty("DTXSID");
			String dtxcid = ac.getProperty("DTXCID");
			String smilesSDF=ac.getProperty("smiles");
		
			if(!htDTXCIDtoDsstoxRecord.containsKey(dtxcid)) {
				//			System.out.println("Dont have in dsstox_records:\t"+dtxcid);
				return;
			}
		
			boolean printedSmilesComparison=false;
		
			for (Model model : models) {
		
				String modelNameDB = model.getName();
		
				String modelNamePercepta=modelNameDB;
		
				if(modelNameDB.equals("ACD_LogD_7_4")) {
					modelNamePercepta="ACD_LogD_1";
				} else if(modelNameDB.equals("ACD_LogD_5_5")) {
					modelNamePercepta="ACD_LogD_2";
				} else if(modelNameDB.equals("ACD_LogP_Consensus")) {
					modelNamePercepta="ACD_LogP";
				}
		
		
				String strCaution=modelNamePercepta+"_Caution";			
				if(modelNameDB.contains("pKa")) {
					strCaution="ACD_pKa_Caution_Apparent";
				} else if (modelNameDB.contains("LogD")) {
					strCaution="ACD_LogD_Caution";
				}
		
				String caution=null;
		
				if(ac.getProperty(strCaution)!=null) {
					caution=ac.getProperty(strCaution);
				}
		
				Dataset dataset = htModelNameToDataset.get(modelNameDB);
				Property property = dataset.getProperty();
				String unitAbbreviation=dataset.getUnitContributor().getAbbreviation_ccd();
		
				//			System.out.println(modelName+"\t"+property.getName());
		
				PredictionDashboard pd = new PredictionDashboard();
				pd.setCreatedBy(lanId);
				pd.setCanonQsarSmiles("N/A");
				pd.setModel(model);
		
		
				pd.setDsstoxRecord(htDTXCIDtoDsstoxRecord.get(dtxcid));
		
				Percepta_Report pr = new Percepta_Report(pd,dataset);
		
				pr.setChemicalIdentifiers(pd);
		
				pr.modelDetails.propertyName = property.getName_ccd();
				pr.modelDetails.propertyDescription = property.getDescription();
		
				if(caution!=null) {
					pd.setPredictionError(caution);
					pr.modelResults.predictedError=caution;
		
				} else {
		
					if (property.getName().equals(DevQsarConstants.PKA_A)) {
						int num = get_pKa_Num(ac, property.getName());
						if (num != -1)
							setPKA(pr,ac, pd, num);
						else
							continue;
		
					} else if (property.getName().equals(DevQsarConstants.PKA_B)) {
						int num = get_pKa_Num(ac, property.getName());
						if (num != -1)
							setPKA(pr, ac, pd, num);
						else
							continue;
		
					} else {
		
						if (ac.getProperty(modelNamePercepta) == null) {
							System.out.println(modelNamePercepta + " not in atomContainer:\t"+dtxsid);
							continue;
						}
		
						if (ac.getProperty(modelNamePercepta).equals("Nan")) {
							//						pd.setPredictionValue(Double.NaN);
							pd.setPredictionError("NaN prediction");
						} else {
							double propertyValue = Double.parseDouble(ac.getProperty(modelNamePercepta));
							pd.setPredictionValue(propertyValue);
						}
		
						pr.modelResults.predictedValue = pd.getPredictionValue();
		
						if (ac.getProperty(modelNamePercepta + "_Error") != null)
							pr.modelResults.predictedUncertainty = Double.parseDouble(ac.getProperty(modelNamePercepta + "_Error"));
		
						pr.modelResults.originalUnit = ac.getProperty(modelNamePercepta + "_Units");
		
					}
		
					setExperimentalConditions(ac, modelNamePercepta, property, pr);
		
				}
		
		
				//			if(ac.getProperty(modelName+"_All")==null) {
				//				pr.estimatedValue.valueAll=pr.estimatedValue.value+"+/-"+pr.estimatedValue.valueError+" "+pr.estimatedValue.valueUnitsOriginal;
				//			} else {
				//				pr.estimatedValue.valueAll=ac.getProperty(modelName+"_All");
				//			}
		
				pd.setPredictionReport(getPredictionReport(pd, pr));
		
				//			if (property.getName().equals(DevQsarConstants.PKA_A)
				//					|| property.getName().equals(DevQsarConstants.PKA_B)) {
				//				System.out.println(Utilities.jsonToPrettyJson(gson.toJson(pr)));
				//			}
		
		
				if(loadedKeys.contains(pd.getKey())) {
		
					if(!printedSmilesComparison) {
						DsstoxRecord dr=htDTXCIDtoDsstoxRecord.get(dtxcid);
						String smilesDR=dr.getSmiles();
						compareSmiles(dtxcid, smilesSDF, dr, smilesDR);
						printedSmilesComparison=true;
					} 
		
					//				System.out.println("Already have "+pd.getKey());
					continue;
				} else {
					loadedKeys.add(pd.getKey());
				}
		
				if (saveReportsToHarddrive) {
					saveReportsToHarddrive(htmlCreator, pd);			
				}		
		
		
				allResults.add(pd);
			}
		}

		private int get_pKa_Num(AtomContainer ac, String propertyName) {
		
			int num = -1;
			String type = null;
		
			if (propertyName.equals(DevQsarConstants.PKA_A))
				type = "MA";
			else if (propertyName.equals(DevQsarConstants.PKA_B))
				type = "MB";
		
			if (ac.getProperty("ACD_pKa_DissType_Apparent_1") != null
					&& ac.getProperty("ACD_pKa_DissType_Apparent_1").equals(type)) {
				num = 1;
			} else if (ac.getProperty("ACD_pKa_DissType_Apparent_2") != null
					&& ac.getProperty("ACD_pKa_DissType_Apparent_2").equals(type)) {
				num = 2;
			}
		
			return num;
		
		}

		private void setExperimentalConditions(AtomContainer ac, String modelName, Property property, Percepta_Report pr) {
			if (property.getName().equals(DevQsarConstants.BOILING_POINT)) {
				pr.modelResults.pressure = Double.parseDouble(ac.getProperty(modelName + "_Press"));
				pr.modelResults.pressureUnits = ac.getProperty(modelName + "_PressUnits");
			}
		
			if (property.getName().equals(DevQsarConstants.VAPOR_PRESSURE)) {
				pr.modelResults.temperature = Double.parseDouble(ac.getProperty(modelName + "_Temp"));
				pr.modelResults.temperatureUnits = ac.getProperty(modelName + "_TempUnits");
				pr.modelResults.temperatureUnits=pr.modelResults.temperatureUnits.replace("Celsius","°C");
			}
		
			if (property.getName().equals(DevQsarConstants.WATER_SOLUBILITY)) {
				pr.modelResults.pH = Double.parseDouble(ac.getProperty(modelName + "_pH"));
			}
		
			if (property.getName().equals(DevQsarConstants.LogD_pH_7_4)) {
				pr.modelResults.pH = Double.parseDouble(ac.getProperty("ACD_LogD_pH_1"));
			}
		
			if (property.getName().equals(DevQsarConstants.LogD_pH_5_5)) {
				pr.modelResults.pH = Double.parseDouble(ac.getProperty("ACD_LogD_pH_2"));
			}
		}

		/**
		 * When records are first loaded from the update sdf, the records in the older sdfs will conflict with them
		 * 
		 * @param dtxcid
		 * @param smilesSDF
		 * @param dr
		 * @param smilesDR
		 */
		private void compareSmiles(String dtxcid, String smilesSDF, DsstoxRecord dr, String smilesDR) {
			if(smilesSDF==null && smilesDR==null) {
				System.out.println(dtxcid+"\tNo smiles in either sdf or dsstoxRecord");
			} else if(smilesSDF!=null && smilesDR==null) {
				System.out.println(dtxcid+"\thave smiles in sdf but not in dsstoxRecord\t"+smilesSDF);
			} else if(smilesSDF==null && smilesDR!=null) {
				System.out.println(dtxcid+"\thave smiles in dsstoxRecord but not in sdf\t"+smilesDR);
			} else if(!smilesSDF.contentEquals(smilesDR)) {
		
				String inchiKeySDF=StructureUtil.indigoInchikey1FromSmiles(smilesSDF);
				String inchiKeyDR=StructureUtil.indigoInchikey1FromSmiles(smilesDR);
		
				//Note smileSDF may have been generated from CDK when missing, and smilesDR was generated from indigo
		
				if (inchiKeyDR!=null && inchiKeySDF!=null) {
					if(!inchiKeyDR.contentEquals(inchiKeySDF)) {
						System.out.println(dtxcid+"\tinchiKey mismatch\t"+inchiKeySDF+"\t"+inchiKeyDR+"\t"+dr.getGenericSubstanceUpdatedAt());
					}
				}
		
			} else {
				//			System.out.println(dtxsid+"\tsmiles match\t"+smilesSDF);	
			}
		}

		private void setPKA(gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport pr, AtomContainer ac, PredictionDashboard pd, int num) {
		
			pr.modelResults.predictedValue = Double.parseDouble(ac.getProperty("ACD_pKa_Apparent_" + num));
			pr.modelResults.predictedUncertainty = Double.parseDouble(ac.getProperty("ACD_pKa_Error_Apparent_" + num));
			pd.setPredictionValue(pr.modelResults.predictedValue);
		
			pr.modelResults.equation = (String) ac.getProperty("ACD_pKa_Equation_Apparent_" + num);
			pr.modelResults.dissType_Apparent = (String) ac.getProperty("ACD_pKa_DissType_Apparent_" + num);
			pr.modelResults.accuracyExplanation_Apparent = (String) ac.getProperty("ACD_pKa_AccuracyExplanation_Apparent_" + num);
		
			//		Not used:		
			//		>  <ACD_pKa_IonicForm_Apparent>
			//		>  <ACD_pKa_All_Apparent_1>
		
		}

		private PredictionReport getPredictionReport(PredictionDashboard pd, Object obj) {
			return new PredictionReport(pd, gson.toJson(obj),null, pd.getCreatedBy());//todo add html report
		}

		//	public void addReportsFromSDF(String sdfPath,String dtxsid,List<String>modelNames) {
		//
		//		HTMLReportCreatorPercepta htmlCreator=new HTMLReportCreatorPercepta();
		//		CompareToChemprop c=new CompareToChemprop();
		//
		//		boolean writeToDB = true;
		//		boolean skipMissingSID = true;
		//		int maxCount =-1;
		//
		//		LinkedHashMap<String, String> hmModelNameToPropertyName = getModelNameToPropertyNameMap();
		//
		//		DsstoxSnapshotServiceImpl snapshotService = new DsstoxSnapshotServiceImpl();
		//		DsstoxSnapshot snapshot = snapshotService.findByName("DSSTOX Snapshot 04/23");
		//		//		Hashtable<String,Long> htDTXSIDtoDsstoxRecordId=dsstoxRecordService.getRecordIdHashtable(snapshot,"dtxsid");
		//		//		Hashtable<String,Long> htDTXSIDtoDsstoxRecordId=dsstoxRecordService.getDsstoxRecordsHashtableFromJsonExport(OPERA_lookups.fileJsonDsstoxRecords,"dtxsid");
		//		Hashtable<String, DsstoxRecord> htDTXSIDtoDsstoxRecord = dsstoxRecordService
		//				.getDsstoxRecordsHashtableFromJsonExport(OPERA_lookups.fileJsonDsstoxRecords2023_04_04, "dtxsid");
		//
		//		// Create datasets and lookup:
		//		Hashtable<String, Dataset> htModelNameToDataset = createDatasets();
		//
		//		// Create the models:
		//		HashMap<String, Method> hmMethods = new HashMap<>();
		//		hmMethods.put("percepta", methodService.findByName("percepta"));
		//		HashMap<String, Model> hmModels = createModels(version, hmMethods);
		//		List<Model> models = new ArrayList<>();
		//		for (String modelName : hmModelNameToPropertyName.keySet()) {
		//			//			System.out.println(modelName);
		//			models.add(hmModels.get(modelName));
		//		}
		//
		//		List<PredictionDashboard> pds = getResultsFromSDFFile(sdfPath, models,
		//				htDTXSIDtoDsstoxRecord, htModelNameToDataset, skipMissingSID, maxCount,true);
		//
		//		List<PredictionReport> reports=new ArrayList<>();
		//		
		//		for (PredictionDashboard pd : pds) {
		//			String dtxsidCurrent = pd.getDsstoxRecord().getDtxsid();
		//			if(!dtxsidCurrent.equals(dtxsid))continue;
		//			if(!modelNames.contains(pd.getModel().getName())) continue;
		//			
		//			String jsonReport=new String(pd.getPredictionReport().getFile());
		//			System.out.println(Utilities.jsonToPrettyJson(jsonReport));
		//			
		//			reports.add(pd.getPredictionReport());
		//		}
		//
		//		System.out.println(sdfPath + "\t" + reports.size());
		//		
		//		PredictionReportServiceImpl prs=new PredictionReportServiceImpl();
		//		
		//		//Note loading to the database removes pds if use createSQL			
		//		if (writeToDB) prs.createSQL(reports);// write to DB using sql, needs separate insert for each table, 10x faster
		//
		//	}
		
		
		private void saveReportsToHarddrive(HTMLReportCreatorPercepta htmlCreator, PredictionDashboard pd) {
			String json = new String(pd.getPredictionReport().getFileJson());
			File jsonFolder = new File("data\\percepta\\sample reports\\" + pd.getDsstoxRecord().getDtxsid() + "\\");
			jsonFolder.mkdirs();
			String filepathJsonReport = jsonFolder.getAbsolutePath() + File.separator + pd.getModel().getName()
					+ ".json";
			String json2 = Utilities.jsonToPrettyJson(json, filepathJsonReport);
		
			gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport pr=gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.fromJson(json);
			String filenameHTML=pd.getModel().getName()+ ".html";					
			htmlCreator.toHTMLFile(pr, jsonFolder.getAbsolutePath(), filenameHTML);
		}
		
	}
	
	
	private class UtilityMethods {

		private  void findMissingValues() {
			//		String folder="data\\percepta\\2024-10\\";
			String folder="data\\dsstox\\sdf_split_out_salts\\";
		
			//		List<String> dtxsids = Arrays.asList("DTXSID50892972", "DTXSID10893066", "DTXSID90873144", "DTXSID90893064",
			//				"DTXSID30892975", "DTXSID90892976", "DTXSID30893063", "DTXSID001250543", "DTXSID10892978",
			//				"DTXSID70692916", "DTXSID301029698", "DTXSID301194382", "DTXSID901288616", "DTXSID70892974",
			//				"DTXSID50892977", "DTXSID50893065", "DTXSID30422817");
		
			//These 2 are in the split out salts folder:
		
			HashSet<String>dtxsids=new HashSet<>();
		
			for (int i=1;i<=35;i++) {
				////		for (int i=1;i<=35;i++) {
				String filename="snapshot_compounds"+i+".SDF";
				////			System.out.println(filename);
				String filepathSDF=folder+filename;
				getDTXSIDs(filepathSDF, dtxsids);
			}
		
			Hashtable<String, DsstoxRecord> htDTXSIDtoDsstoxRecord = dsstoxRecordService
					.getDsstoxRecordsHashtableFromJsonExport(PredictionDashboardTableMaps.fileJsonDsstoxRecords2023_04_04, "dtxsid");
		
			List<DsstoxRecord>missingRecords=new ArrayList<>();
			for (String dtxsid:htDTXSIDtoDsstoxRecord.keySet()) {
				if(!dtxsids.contains(dtxsid)) {
					missingRecords.add(htDTXSIDtoDsstoxRecord.get(dtxsid));
				}
			}
		
			String json=Utilities.gson.toJson(missingRecords);
		
			try {
				FileWriter fw=new FileWriter("data/percepta/missing records split out salts.json");
				fw.write(json);
				fw.flush();
				fw.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		
		}

		/**
		 * Prints percepta results from sdf for a list of sids
		 * 
		 * @param filepathSDF
		 * @param dtxsids
		 */
		void printValues(String filepathSDF, List<String> dtxsids) {
		
			DashboardPredictionUtilities dpu = new DashboardPredictionUtilities();
			AtomContainerSet acs = dpu.readSDFV3000(filepathSDF);
			AtomContainerSet acs2 = dpu.filterAtomContainerSet(acs, true, -1);
		
			System.out.println(filepathSDF + "\t" + acs2.getAtomContainerCount());
		
			try {
		
				Iterator<IAtomContainer> iterator = acs2.atomContainers().iterator();
				List<PredictionDashboard> predictions = new ArrayList<>();
		
				while (iterator.hasNext()) {
					AtomContainer ac = (AtomContainer) iterator.next();
					String dtxsidCurrent = ac.getProperty("DTXSID");
					if (!dtxsids.contains(dtxsidCurrent))
						continue;
					//				System.out.println(filepathSDF);
					System.out.println(Utilities.gson.toJson(ac.getProperties()));
				}
		
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		}

		private  void printValues() {
			String folder="data\\percepta\\2024-10\\";
			//		String folder="data\\dsstox\\sdf_split_out_salts\\";
		
			//		List<String> dtxsids = Arrays.asList("DTXSID50892972", "DTXSID10893066", "DTXSID90873144", "DTXSID90893064",
			//				"DTXSID30892975", "DTXSID90892976", "DTXSID30893063", "DTXSID001250543", "DTXSID10892978",
			//				"DTXSID70692916", "DTXSID301029698", "DTXSID301194382", "DTXSID901288616", "DTXSID70892974",
			//				"DTXSID50892977", "DTXSID50893065", "DTXSID30422817");
		
			//These 2 are in the split out salts folder:
			//		List<String> dtxsids = Arrays.asList("DTXSID001250543","DTXSID301029698");
		
		
			//		List<String> dtxsids = Arrays.asList("DTXSID80306012");//1 missing
			//		List<String> dtxsids = Arrays.asList("DTXSID001006479");//6 missing
			//		List<String> dtxsids = Arrays.asList("DTXSID001000419");//12 missing
			//		List<String> dtxsids = Arrays.asList("DTXSID001000152");//16 missing
			//		List<String> dtxsids = Arrays.asList("DTXSID7020182");//bisphenol-A
			//		List<String> dtxsids = Arrays.asList("DTXSID3039242");//benzene
			//		List<String> dtxsids = Arrays.asList("DTXSID8022666");//betaine permanent zwitterion
			//		List<String> dtxsids = Arrays.asList("DTXSID8020094");//2-aminobenzoic acid- acid and base
		
		
			List<String> dtxsids = Arrays.asList("DTXSID7020182","DTXSID001006479","DTXSID001000152","DTXSID8022666","DTXSID8020094");
			
		
			for (int i=1;i<=35;i++) {
				////		for (int i=1;i<=35;i++) {
				String filename="snapshot_compounds"+i+".SDF";
				////			System.out.println(filename);
				String filepathSDF=folder+filename;
				//			p.loadFromSDF(filepathSDF,true,-1);
				//			printValues(filepathSDF,"DTXSID7020182");
				printValues(filepathSDF,dtxsids);
		
			}
		}

		private void lookAtMissingValues() {
			Hashtable<String, DsstoxRecord> htDTXSIDtoDsstoxRecord = dsstoxRecordService
					.getDsstoxRecordsHashtableFromJsonExport(new File("data//percepta//missing records 2024-10 percepta input.json"), "dtxsid");
		
			int count=0;
		
			List<DsstoxCompound> compounds=new ArrayList<>();
		
			DsstoxCompoundServiceImpl cs=new DsstoxCompoundServiceImpl();
			SmilesGenerator sg=SmilesGenerator.unique();
			for(String dtxsid:htDTXSIDtoDsstoxRecord.keySet()) {
				DsstoxRecord dr=htDTXSIDtoDsstoxRecord.get(dtxsid);
		
				//Skip salts
		
		
				if(dr.getSmiles()!=null) {
					if(dr.getSmiles().contains(".")) continue;
					if(dr.getSmiles().contains("*")) continue;
					if(dr.getSmiles().contains("|")) continue;
				}
				if(dr.getDtxcid()==null)continue;//no structure missing
		
				DsstoxCompound compound=cs.findByDtxcid(dr.getDtxcid());
		
				if(compound.getMolFile()!=null) {
					try {
						IAtomContainer ac=StructureUtil.fromMolString(compound.getMolFile());
		
						String smiles=sg.create(ac);
						System.out.println(dtxsid+"\t"+smiles);
		
						if(smiles.contains(".")) continue;
		
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//					e.printStackTrace();
						System.out.println("error for "+dtxsid);
						continue;
					}
				} else if (compound.getMolFile()==null) {
					if(compound.getSmiles()==null || compound.getSmiles().equals("null")) {
						System.out.println(dtxsid+"\tNo structure");
						continue;
					}
				}
		
		
		
				count++;
		
				compounds.add(compound);
		
				//			System.out.println(count+"\t"+Utilities.gson.toJson(compound));
		
				//			System.out.println(dtxsid+"\t"+compound.getChemicalType());
			}
			try {
				DSSTOX_Compounds_Script.createSDF(compounds, "data\\dsstox\\sdf_split_out_salts\\missing from snapshot.sdf", false, false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
		// ACD_pKa_Apparent_v_Pka_Classic_1
		// ACD_pKa_Apparent_v_Pka_Classic_2
		
		void lookAtpKA_Values() {
		
			String folder = "data\\dsstox\\percepta\\pka\\";
			//		String filepathSDF=folder+"snapshot_compounds35.sdf";
			String filepathSDF = folder + "snapshot_compounds35-no pka sort.sdf";
		
			DashboardPredictionUtilities dpu = new DashboardPredictionUtilities();
		
			AtomContainerSet acs = dpu.readSDFV3000(filepathSDF);
		
			Iterator<IAtomContainer> iterator = acs.atomContainers().iterator();
		
			int count = 0;
		
			while (iterator.hasNext()) {
				count++;
		
				AtomContainer ac = (AtomContainer) iterator.next();
		
				String DTXSID = (String) ac.getProperty("DTXSID");
		
				Double pKa1 = null;
				String pKaEqn1 = null;
				String pKaType1 = null;
		
				if (ac.getProperty("ACD_pKa_Apparent_1") != null) {
					pKa1 = Double.parseDouble(ac.getProperty("ACD_pKa_Apparent_1"));
				}
		
				if (ac.getProperty("ACD_pKa_Equation_Apparent_1") != null) {
					pKaEqn1 = ac.getProperty("ACD_pKa_Equation_Apparent_1");
				}
		
				if (ac.getProperty("ACD_pKa_DissType_Apparent_1") != null) {
					pKaType1 = ac.getProperty("ACD_pKa_DissType_Apparent_1");
				}
		
				Double pKa2 = null;
				String pKaEqn2 = null;
				String pKaType2 = null;
		
				if (ac.getProperty("ACD_pKa_Apparent_2") != null) {
					pKa2 = Double.parseDouble(ac.getProperty("ACD_pKa_Apparent_2"));
				}
		
				if (ac.getProperty("ACD_pKa_Equation_Apparent_2") != null) {
					pKaEqn2 = ac.getProperty("ACD_pKa_Equation_Apparent_2");
				}
		
				if (ac.getProperty("ACD_pKa_DissType_Apparent_2") != null) {
					pKaType2 = ac.getProperty("ACD_pKa_DissType_Apparent_2");
				}
		
				System.out.println(DTXSID + "\t" + pKa1 + "\t" + pKaEqn1 + "\t" + pKaType1 + "\t" + pKa2 + "\t" + pKaEqn2
						+ "\t" + pKaType2);
		
				// ACD_pKa_Apparent_1
				//			ACD_pKa_Equation_Apparent_1
				// ACD_pKa_Apparent_2
				// ACD_pKa_Equation_Apparent2
				//			DTXSID
		
			}
		
		}

		/**
		 * Gets DTXSIDs in SDF file
		 * 
		 * @param filepathSDF
		 * @param dtxsids
		 */
		private void getDTXSIDs(String filepathSDF,HashSet<String>dtxsids) {
		
			DashboardPredictionUtilities dpu = new DashboardPredictionUtilities();
			AtomContainerSet acs = dpu.readSDFV3000(filepathSDF);
			AtomContainerSet acs2 = dpu.filterAtomContainerSet(acs, true, -1);
		
			System.out.println(filepathSDF + "\t" + acs2.getAtomContainerCount());
		
			try {
		
				Iterator<IAtomContainer> iterator = acs2.atomContainers().iterator();
				List<PredictionDashboard> predictions = new ArrayList<>();
		
				while (iterator.hasNext()) {
					AtomContainer ac = (AtomContainer) iterator.next();
					String dtxsidCurrent = ac.getProperty("DTXSID");
					dtxsids.add(dtxsidCurrent);
					//				System.out.println(filepathSDF);
					//				System.out.println(Utilities.gson.toJson(ac.getProperties()));
				}
		
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		}

			
	}

	private class InitializeDB {

		LinkedHashMap<String, String>hmModelNameToPropertyName=null;
		
		InitializeDB() {
			hmModelNameToPropertyName = getModelNameToPropertyNameMap();
		}

		public void initializeDB() {
			createDatasets();
			createModels();
		}

		private Hashtable<String, Dataset> createDatasets() {

			Hashtable<String, Dataset> htModelNameToDataset = new Hashtable<>();

			// HashMap<String,
			// String>hmUnitsDataset=DevQsarConstants.getDatasetFinalUnitsMap();
			HashMap<String, String> hmUnitsDatasetContributor = DevQsarConstants.getContributorUnitsNameMap();

			for (String modelName : hmModelNameToPropertyName.keySet()) {
				String propertyNameDB = hmModelNameToPropertyName.get(modelName);

				String datasetName = getDatasetName(propertyNameDB);

				String unitAbbrev = hmUnitsDatasetContributor.get(propertyNameDB);

				if (unitAbbrev == null) {
					System.out.println(propertyNameDB + "\t" + datasetName + "\tMissing units skipping");
					continue;
				}

				Unit unit = CreatorScript.createUnit(unitAbbrev, lanId);
				Unit unitContributor = CreatorScript.createUnit(unitAbbrev, lanId);

				String propertyDescriptionDB = DevQsarConstants.getPropertyDescription(propertyNameDB);

				if (propertyDescriptionDB.contains("*"))
					System.out.println(propertyNameDB + "\t" + propertyDescriptionDB);

				Property property = CreatorScript.createProperty(propertyNameDB, propertyDescriptionDB, lanId);

				String datasetDescription = propertyNameDB + " dataset from Percepta" + version;
				//			//		System.out.println(datasetName+"\t"+datasetDescription);
				//
				String dsstoxMappingStrategy = null;

				Dataset dataset = new Dataset(datasetName, datasetDescription, property, unit, unitContributor,
						dsstoxMappingStrategy, lanId);

				CreatorScript.createDataset(dataset);

				htModelNameToDataset.put(modelName, dataset);

				//			System.out.println(Utilities.gson.toJson(dataset));
			}

			return htModelNameToDataset;

		}

		private String getDatasetName(String propertyNameDB) {
			String datasetName = propertyNameDB + " Percepta" + version;
			return datasetName;
		}

		private HashMap<String, Model> createModels() {

			
			HashMap<String, Method> hmMethods = new HashMap<>();
			hmMethods.put("percepta", methodService.findByName("percepta"));

			
			String sourceName = "Percepta" + version;
			String descriptorSetName = "Percepta " + version;
			String splittingName = "Percepta";
			String methodName = "percepta";

			SourceService ss = new SourceServiceImpl();
			Source source = ss.findByName(sourceName);

			HashMap<String, Model> hmModels = new HashMap<>();

			for (String modelName : hmModelNameToPropertyName.keySet()) {
				String propertyNameDB = hmModelNameToPropertyName.get(modelName);
				String datasetName = getDatasetName(propertyNameDB);
				//			System.out.println(modelName+"\t"+propertyNameDB);
				Model model = new Model(modelName, hmMethods.get(methodName), null, descriptorSetName, datasetName,
						splittingName, source, lanId);

				model.setIs_public(true);

				model = CreatorScript.createModel(model);
				hmModels.put(modelName, model);
			}
			return hmModels;
		}

		private Hashtable<String, Property> create_htModelNameToProperty() {

			System.out.print("creating htModelNameToProperty...");

			Hashtable<String, Property> htModelNameToProperty = new Hashtable<>();

			// HashMap<String,
			// String>hmUnitsDataset=DevQsarConstants.getDatasetFinalUnitsMap();
			HashMap<String, String> hmUnitsDatasetContributor = DevQsarConstants.getContributorUnitsNameMap();

			
			
			for (String modelName : hmModelNameToPropertyName.keySet()) {
				String propertyNameDB = hmModelNameToPropertyName.get(modelName);
				String propertyDescriptionDB = DevQsarConstants.getPropertyDescription(propertyNameDB);

				if (propertyDescriptionDB.contains("*"))
					System.out.println(propertyNameDB + "\t" + propertyDescriptionDB);

				Property property = CreatorScript.createProperty(propertyNameDB, propertyDescriptionDB, lanId);
				htModelNameToProperty.put(modelName, property);
			}

			System.out.println("done");

			return htModelNameToProperty;
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
		// ACD_pKa_Apparent_v_Pka_Classic_1
		// ACD_pKa_Apparent_v_Pka_Classic_2

		private LinkedHashMap<String, String> getModelNameToPropertyNameMap() {

			LinkedHashMap<String, String> hm = new LinkedHashMap<>();

			hm.put("ACD_Prop_Density", DevQsarConstants.DENSITY);
			hm.put("ACD_BP", DevQsarConstants.BOILING_POINT);
			hm.put("ACD_VP", DevQsarConstants.VAPOR_PRESSURE);
			hm.put("ACD_FP", DevQsarConstants.FLASH_POINT);
			hm.put("ACD_SolInPW", DevQsarConstants.WATER_SOLUBILITY);
			hm.put("ACD_Prop_Molar_Refractivity", DevQsarConstants.MOLAR_REFRACTIVITY);
			hm.put("ACD_Prop_Molar_Volume", DevQsarConstants.MOLAR_VOLUME);
			hm.put("ACD_Prop_Polarizability", DevQsarConstants.POLARIZABILITY);
			hm.put("ACD_Prop_Index_Of_Refraction", DevQsarConstants.INDEX_OF_REFRACTION);
			hm.put("ACD_Prop_Surface_Tension", DevQsarConstants.SURFACE_TENSION);
			hm.put("ACD_LogP_Consensus", DevQsarConstants.LOG_KOW);// consensus model selected in percepta options

			hm.put("ACD_LogD_7_4", DevQsarConstants.LogD_pH_7_4);
			hm.put("ACD_LogD_5_5", DevQsarConstants.LogD_pH_5_5);

			hm.put("ACD_pKa_Apparent_MA", DevQsarConstants.PKA_A);
			hm.put("ACD_pKa_Apparent_MB", DevQsarConstants.PKA_B);

			//		hm.put("ACD_Prop_Parachor",DevQsarConstants.PARACHOR);//not of interest
			hm.put("ACD_Prop_Dielectric_Constant",DevQsarConstants.DIELECTRIC_CONSTANT);

			// Need to figure this out- which is which?
			// We arent allowed to expose pKA to dashboard according to tony?

			// ACD_pKa_Apparent_v_Pka_Classic_1
			// ACD_pKa_Apparent_v_Pka_Classic_2

			//		ACD_pKa_Basic - in prod_qsar but not on dashboard?
			//		ACD_pKa_Acidic

			return hm;
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
	// ACD_pKa_Apparent_v_Pka_Classic_1
	// ACD_pKa_Apparent_v_Pka_Classic_2

	public void compileApiJsonResultsFromFolder(String folderpath,String snapshotName,boolean omitSalts) {

		CompareToChemprop c=new CompareToChemprop();

		boolean writeToDB = true;
		boolean skipMissingSID = true;
		int maxCount =-1;
		boolean saveReportsToHarddrive = false;
		if(maxCount==-1) saveReportsToHarddrive=false;


		DsstoxSnapshotServiceImpl snapshotService = new DsstoxSnapshotServiceImpl();
		DsstoxSnapshot snapshot = snapshotService.findByName(snapshotName);

		SourceService sourceService=new SourceServiceImpl();
		String sourceName="Percepta" + version;
		Source source=sourceService.findByName(sourceName);

		Hashtable<String, DsstoxRecord> htDTXCIDtoDsstoxRecord=null;

		//TODO use PredictionDashboardTableMaps instead of following hashtables:

		if(snapshotName.contentEquals("DSSTOX Snapshot 04/23")) {
			htDTXCIDtoDsstoxRecord = dsstoxRecordService
					.getDsstoxRecordsHashtableFromJsonExport2(PredictionDashboardTableMaps.fileJsonDsstoxRecords2023_04_04, "dtxcid");
		} else if (snapshotName.contentEquals("DSSTOX Snapshot 11/12/2024")) {
			htDTXCIDtoDsstoxRecord = dsstoxRecordService
					.getDsstoxRecordsHashtableFromJsonExport2(PredictionDashboardTableMaps.fileJsonDsstoxRecords2024_11_12, "dtxcid");
		} else {
			System.out.println("Invalid snapshot");
			return;
		}


		//		DsstoxRecord dr=htDTXSIDtoDsstoxRecord.get("DTXSID50228631");
		//		System.out.println(Utilities.gson.toJson(dr));
		//		if(true)return;

		// Create datasets and lookup:
		Hashtable<String, Dataset> htModelNameToDataset = initializeDB.createDatasets();

		// Create the models:
		HashMap<String, Model> hmModels = initializeDB.createModels();

		List<Model> models = new ArrayList<>();
		for (String modelName : initializeDB.hmModelNameToPropertyName.keySet()) {
			//			System.out.println(modelName);
			models.add(hmModels.get(modelName));
		}

		HashSet<String>loadedKeys=DatabaseUtilities.getLoadedKeys(source, snapshot);

		System.out.println("Loaded keys: "+loadedKeys.size());

		//		for (String key:loadedKeys) {
		//			System.out.println(key);
		//		}
		//		if(true)return;


		for (File file : new File(folderpath).listFiles()) {

			if (!file.getName().contains(".sdf"))
				continue;

			List<PredictionDashboard> pds = getResultsFromSDFFile(file.getAbsolutePath(), models,
					htDTXCIDtoDsstoxRecord, htModelNameToDataset, skipMissingSID, maxCount,omitSalts,loadedKeys,saveReportsToHarddrive);

			//		SortedMap<String, SortedMap<String,Prediction>> map=c.convertPredictionsDashboardToMap(pds);
			////			System.out.println(folderpath+"percepta_res_qsar_sample.json");
			//		Utilities.saveJson(map, folderpath+file.getName().replace(".sdf", "_map.json"));
			
			if (writeToDB && pds.size()>0) {
				predictionDashboardService.createSQL(pds);// write to DB using sql

			}
			System.out.println("\n"+file.getName()+"\t"+pds.size()+"\t"+loadedKeys.size());
		}
	}

	
	//	public void addReportsFromSDF(String sdfPath,String dtxsid,List<String>modelNames) {
	//
	//		HTMLReportCreatorPercepta htmlCreator=new HTMLReportCreatorPercepta();
	//		CompareToChemprop c=new CompareToChemprop();
	//
	//		boolean writeToDB = true;
	//		boolean skipMissingSID = true;
	//		int maxCount =-1;
	//
	//		LinkedHashMap<String, String> hmModelNameToPropertyName = getModelNameToPropertyNameMap();
	//
	//		DsstoxSnapshotServiceImpl snapshotService = new DsstoxSnapshotServiceImpl();
	//		DsstoxSnapshot snapshot = snapshotService.findByName("DSSTOX Snapshot 04/23");
	//		//		Hashtable<String,Long> htDTXSIDtoDsstoxRecordId=dsstoxRecordService.getRecordIdHashtable(snapshot,"dtxsid");
	//		//		Hashtable<String,Long> htDTXSIDtoDsstoxRecordId=dsstoxRecordService.getDsstoxRecordsHashtableFromJsonExport(OPERA_lookups.fileJsonDsstoxRecords,"dtxsid");
	//		Hashtable<String, DsstoxRecord> htDTXSIDtoDsstoxRecord = dsstoxRecordService
	//				.getDsstoxRecordsHashtableFromJsonExport(OPERA_lookups.fileJsonDsstoxRecords2023_04_04, "dtxsid");
	//
	//		// Create datasets and lookup:
	//		Hashtable<String, Dataset> htModelNameToDataset = createDatasets();
	//
	//		// Create the models:
	//		HashMap<String, Method> hmMethods = new HashMap<>();
	//		hmMethods.put("percepta", methodService.findByName("percepta"));
	//		HashMap<String, Model> hmModels = createModels(version, hmMethods);
	//		List<Model> models = new ArrayList<>();
	//		for (String modelName : hmModelNameToPropertyName.keySet()) {
	//			//			System.out.println(modelName);
	//			models.add(hmModels.get(modelName));
	//		}
	//
	//		List<PredictionDashboard> pds = getResultsFromSDFFile(sdfPath, models,
	//				htDTXSIDtoDsstoxRecord, htModelNameToDataset, skipMissingSID, maxCount,true);
	//
	//		List<PredictionReport> reports=new ArrayList<>();
	//		
	//		for (PredictionDashboard pd : pds) {
	//			String dtxsidCurrent = pd.getDsstoxRecord().getDtxsid();
	//			if(!dtxsidCurrent.equals(dtxsid))continue;
	//			if(!modelNames.contains(pd.getModel().getName())) continue;
	//			
	//			String jsonReport=new String(pd.getPredictionReport().getFile());
	//			System.out.println(Utilities.jsonToPrettyJson(jsonReport));
	//			
	//			reports.add(pd.getPredictionReport());
	//		}
	//
	//		System.out.println(sdfPath + "\t" + reports.size());
	//		
	//		PredictionReportServiceImpl prs=new PredictionReportServiceImpl();
	//		
	//		//Note loading to the database removes pds if use createSQL			
	//		if (writeToDB) prs.createSQL(reports);// write to DB using sql, needs separate insert for each table, 10x faster
	//
	//	}


	void loadPredictions2024() {

		String snapshotName="DSSTOX Snapshot 11/12/2024";
		boolean omitSalts=true;
		
		//Load latest results first (dtxcid has changed for some dtxsids):
		compileApiJsonResultsFromFolder("data\\percepta\\2024-11-12\\",snapshotName,omitSalts);
		compileApiJsonResultsFromFolder("data\\percepta\\2024-10\\",snapshotName,omitSalts);

	}


	public static void main(String[] args) {
		PredictionDashboardScriptPercepta p = new PredictionDashboardScriptPercepta();
		
//		p.initializeDB.initializeDB();

		//		DatabaseUtilities d=new DatabaseUtilities();
		//		d.deleteAllRecords("Percepta2023.1.2");

		p.compileApiJsonResultsFromFolder("data\\percepta\\testLoad\\","DSSTOX Snapshot 04/23",true);
		
//		p.loadPredictions2024();

		p.utilities.printValues();
//		p.utilities.findMissingValues();
//		p.utilities.lookAtMissingValues();
	
	}

	/**
	 * Gets results from a json file from the api Each line is a different set of
	 * api predictions *
	 * 
	 * @param filepath
	 * @param models
	 * @param skipMissingSID
	 * @param maxCount
	 * @param saveReportsToHarddrive 
	 * @return
	 */
	public List<PredictionDashboard> getResultsFromSDFFile(String filepathSDF, List<Model> models,
			Hashtable<String, DsstoxRecord> htDTXCIDtoDsstoxRecord, Hashtable<String, Dataset> htModelNameToDataset,
			boolean skipMissingSID, int maxCount,boolean omitSalts,HashSet<String>loadedKeys, boolean saveReportsToHarddrive) {

		try {

			DashboardPredictionUtilities dpu = new DashboardPredictionUtilities();
			AtomContainerSet acs = dpu.readSDFV3000(filepathSDF);
			AtomContainerSet acs2 = dpu.filterAtomContainerSet(acs, skipMissingSID, omitSalts, maxCount);
			System.out.println("\nAtom container count=" + acs2.getAtomContainerCount());

			List<PredictionDashboard> allResults = new ArrayList<>();
			Iterator<IAtomContainer> iterator = acs2.atomContainers().iterator();

			while (iterator.hasNext()) {
				AtomContainer ac = (AtomContainer) iterator.next();
				converter.convertPerceptaResultsToPredictionDashboard(models, allResults, ac, htDTXCIDtoDsstoxRecord,
						htModelNameToDataset,loadedKeys,saveReportsToHarddrive);
			}

			// System.out.println(lastLine);

			return allResults;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}


}
