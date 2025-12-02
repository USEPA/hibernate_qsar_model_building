package gov.epa.run_from_java.scripts.PredictionDashboard.TEST;

import java.io.*;
import java.sql.ResultSet;
import java.text.DecimalFormat;
//import java.text.DecimalFormat;
import java.util.*;

import org.checkerframework.checker.units.qual.mol;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.gson.Gson;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.Calculations.PredictToxicityWebPageCreatorFromJSON;
import ToxPredictor.Application.Calculations.TaskStructureSearch;
import ToxPredictor.Application.Calculations.CreateLookups.GetDTXSIDLookup;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSmiles;
import ToxPredictor.Application.model.PredictionResults;

import ToxPredictor.Application.model.SimilarChemical;
import ToxPredictor.Application.model.SimilarChemicals;
import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.Database.ResolverDb2;
import ToxPredictor.Utilities.FormatUtils;
import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.UnitConverter;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.MethodAD;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedADEstimate;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor;
import gov.epa.databases.dev_qsar.qsar_models.entity.Source;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxRecordServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.MethodServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionReportServiceImpl;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.service.DsstoxCompoundServiceImpl;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.DSSTOX_Loading.DSSTOX_Compounds_Script;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.DatabaseUtilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.HTMLReportCreator;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionDashboardTableMaps;
import gov.epa.run_from_java.scripts.PredictionDashboard.TEST.PredictionDashboardScriptTEST.InitializeDB;

import gov.epa.test.api.predict.TestApi;


/**
 * 
 * This version doesnt use lookups
 * 
 * 
 * @author TMARTI02
 */
public class PredictionDashboardScriptTEST2  {

	//Classes for handling each aspect of data loading:
	ConvertPredictionResultsToPredictionDashboard converter=new ConvertPredictionResultsToPredictionDashboard();
	
	InitializeDB initializeDB=new InitializeDB();


	MethodServiceImpl methodService=new MethodServiceImpl();
	PredictionDashboardServiceImpl predictionDashboardService=new PredictionDashboardServiceImpl();
	PredictionReportServiceImpl predictionReportService=new PredictionReportServiceImpl();
	DsstoxRecordServiceImpl dsstoxRecordService=new  DsstoxRecordServiceImpl();


	boolean useLatestModelIds=true; 
	boolean createReports=true;
	boolean createDetailedReports=false;
	
	static String lanId="tmarti02";
	static String version="5.1.3";
	String method = TESTConstants.ChoiceConsensus;
	List<String>endpoints=new ArrayList<>(TESTConstants.getFullEndpoints(null));

	public static String strInvalidStructure="Invalid structure";
	public static String strDescriptorGeneration="Descriptor generation error";
	
	static PredictionDashboardTableMaps tableMaps;


//	Hashtable<String, DSSToxRecord>htCAStoDsstoxRecord=GetDTXSIDLookup.getDsstoxRecordLookupByCAS();
	


	class ConvertPredictionResultsToPredictionDashboard {

		private Double convertLogMolarUnits(PredictionResults pr, String logMolarValue) {

			String endpoint=pr.getEndpoint();

			if (endpoint.equals(TESTConstants.ChoiceDM_LC50)
					|| endpoint.equals(TESTConstants.ChoiceFHM_LC50)
					|| endpoint.equals(TESTConstants.ChoiceTP_IGC50)
					|| endpoint.equals(TESTConstants.ChoiceWaterSolubility)
					|| endpoint.equals(TESTConstants.ChoiceRat_LD50)) {
				return Math.pow(10.0,-Double.parseDouble(logMolarValue));
			} else if (endpoint.equals(TESTConstants.ChoiceBCF) 
					|| endpoint.equals(TESTConstants.ChoiceVaporPressure)
					|| endpoint.contains(TESTConstants.ChoiceViscosity)
					|| endpoint.equals(TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity)) {
				return Math.pow(10.0,Double.parseDouble(logMolarValue));
			} else {
				System.out.println("Not handled:"+endpoint);
				return null;
			}
		}

		private Double convertToMolar(String propertyNameDB, PredictionResults pr, Double MW, String massValue,String logMolarValue) {

			if (propertyNameDB.equals(DevQsarConstants.FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50)
					|| propertyNameDB.equals(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50)
					|| propertyNameDB.equals(DevQsarConstants.FORTY_EIGHT_HR_TETRAHYMENA_PYRIFORMIS_IGC50)
					|| propertyNameDB.equals(DevQsarConstants.WATER_SOLUBILITY)
					|| propertyNameDB.equals(DevQsarConstants.ORAL_RAT_LD50)) {

				if(MW==null) {
					return Math.pow(10.0,-Double.parseDouble(logMolarValue));
				} else if(pr.getPredictionResultsPrimaryTable().getMassUnits().contains("mg/")) {
					return Double.parseDouble(massValue)/1000.0/MW;//mol/L value
				} else {
					System.out.println("Handle "+pr.getPredictionResultsPrimaryTable().getMassUnits()+" in convertMassUnitsToMolar()");
				}

				return Math.pow(10.0,-Double.parseDouble(massValue));
			} else if (propertyNameDB.equals(DevQsarConstants.BCF) 
					|| propertyNameDB.equals(DevQsarConstants.VAPOR_PRESSURE)
					|| propertyNameDB.contains(DevQsarConstants.VISCOSITY)
					|| propertyNameDB.equals(DevQsarConstants.ESTROGEN_RECEPTOR_RBA)) {
				return Double.parseDouble(massValue);
			} else {
				System.out.println("Not handled:"+propertyNameDB);
				return null;
			}
		}


		
		
		
		public PredictionDashboard convertPredictionResultsToPredictionDashboard(PredictionResults predictionResults,  boolean writeReportToHarddrive) {
			
			
//			fixUnitsBCF(predictionResults);

			if(predictionResults.getSmiles()==null) predictionResults.setSmiles("N/A");

			PredictionDashboard pd=new PredictionDashboard();
			
			pd.endpoint=predictionResults.getEndpoint();//for convenience for html generation- needed?

			try {

				try {
					
					long t1=System.currentTimeMillis();

					String propertyNameTest=predictionResults.getEndpoint();
					String propertyNameDB=TESTConstants.getPropertyNameDB(propertyNameTest);
					String modelName=initializeDB.getModelName(propertyNameDB);

//					System.out.println(propertyNameTest+"\t"+propertyNameDB);

//					String methodName=null;
//					if (propertyNameDB.equals(DevQsarConstants.DEVELOPMENTAL_TOXICITY) || propertyNameDB.equals(DevQsarConstants.MUTAGENICITY) || propertyNameDB.equals(DevQsarConstants.ESTROGEN_RECEPTOR_BINDING)) {
//						methodName="consensus_classifier";
//					} else {
//						methodName="consensus_regressor";
//					}
//					Method method=new Method();
//					method.setName(methodName);//TODO do we need anything else?
//						
//					String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
//					String datasetName=initializeDB.getDatasetName(propertyNameDB);
//					String splittingName="TEST";
//					
//					Source source=new Source();
//					source.setDescription("<a href=\"https://www.epa.gov/chemical-research/toxicity-estimation-software-tool-test\">TEST5.1.3</a> is a free suite of QSAR Models to predict physicochemical properties and toxicity endpoints as described in the <a href=\"https://www.epa.gov/sites/production/files/2016-05/documents/600r16058.pdf\" target=\"_blank\">user guide</a>. ");
//					source.setName("TEST5.1.3");
//					source.setUrl("https://www.epa.gov/chemical-research/toxicity-estimation-software-tool-test");
//					
//					Model model=new Model(modelName, method, null, descriptorSetName, datasetName, splittingName, source,lanId);
//
//					String propertyAbbrev=TESTConstants.getAbbrevEndpoint(propertyNameTest);
//
//					model.setId(htModelNameToID.get(modelName));//for the training/test set plots from Asif's API
//					model.setName_ccd(getModelNameCCD(propertyAbbrev));
//					pd.setModel(model);

					pd.setModel(tableMaps.mapModels.get(modelName));
//					System.out.println(predictionResults.getEndpoint()+"\t"+model.getName());
					pd.setCanonQsarSmiles("N/A");
					pd.setDtxcid(predictionResults.getDTXCID());

//					DsstoxRecord dr=new DsstoxRecord();
//					dr.setCasrn(predictionResults.getCAS());
//					dr.setDtxsid(predictionResults.getDTXSID());
//					dr.setDtxcid(predictionResults.getDTXCID());
//					dr.setSmiles(predictionResults.getSmiles());
//					dr.setPreferredName(predictionResults.getName());
//					pd.setDsstoxRecord(dr);
					
					if(tableMaps.mapDsstoxRecordsByCID.containsKey(predictionResults.getDTXCID())) {
						pd.setDsstoxRecord(tableMaps.mapDsstoxRecordsByCID.get(predictionResults.getDTXCID()));
//						System.out.println(pd.getDsstoxRecord().getId());
						
					} else {
//						System.out.println("No dsstox record for "+predictionResults.getDTXCID());
						return null;
					}
					
					pd.setCreatedBy(lanId);


					if (predictionResults.getError()!=null && !predictionResults.getError().isBlank()) {
						pd.setPredictionError(predictionResults.getError());
						standardizeError(pd);
//						System.out.println(propertyNameDB+"\t"+pd.getPredictionError());
					}
//					} else {
//						setExperimentalPredictedValues(predictionResults, pd);
////						System.out.println(propertyNameDB+"\t"+pd.getExperimentalValue());
//					}
					
					setExperimentalPredictedValues(predictionResults, pd,tableMaps);
					
//					System.out.println(propertyNameDB+"\t"+pd.getExperimentalValue()+"\t"+predictionResults.getPredictionResultsPrimaryTable().getExpToxValMass());
//					System.out.println(propertyNameDB+"\t"+TESTConstants.isLogMolar(predictionResults.getEndpoint())+"\t"+TESTConstants.isBinary(predictionResults.getEndpoint()));
//					System.out.println(propertyNameDB+"\t"+pd.getExperimentalValue()+"\t"+pd.getPredictionValue());

					addNeighbors(predictionResults,pd);
					
//					MethodAD methodAD=new MethodAD();
//					methodAD.setDescription("Combined applicability domain from multiple AD measures");
//					methodAD.setDescription_long("Overall applicability domain from multiple AD measures");
//					methodAD.setName("Combined Applicability Domain");
//					methodAD.setName_display("Combined applicability domain");
//					methodAD.setMethodScope("Global");
					
					MethodAD methodAD=tableMaps.mapMethodAD.get("Combined Applicability Domain");
					addApplicabilityDomain(predictionResults, pd, methodAD);
					
//					String propertyNameTest=TESTConstants.getPropertyNameTest(propertyNameDB);
//					String dashboardUnits=TESTConstants.getDashboardUnits(propertyNameTest);
//					System.out.println(propertyNameDB+"\t"+dashboardUnits);
					
					String unitAbbreviation=TESTConstants.getDashboardUnits(propertyNameTest);
					String unitAbbreviationNeighbor=TESTConstants.getModelUnits(propertyNameTest);
					Property property=new Property();
					
//					property.setName(propertyNameDB);
					property.setDescription(DevQsarConstants.getPropertyDescription(propertyNameDB));
					property.setName(propertyNameDB);
					property.setName_ccd(DevQsarConstants.getPropertyNameCCD(propertyNameDB)); 
					
					
					long t1a=System.currentTimeMillis();
					TEST_Report tr=new TEST_Report(pd,predictionResults, property, unitAbbreviation,unitAbbreviationNeighbor, useLatestModelIds);
					long t2a=System.currentTimeMillis();
					
					String fileJson=tr.toJson();
					long t3=System.currentTimeMillis();
					
					HTMLReportCreatorTEST h=new HTMLReportCreatorTEST();
					tr.modelDetails.loadPlotsFromDB=true;
					
					String fileHtml=h.createReport(tr);
					long t4=System.currentTimeMillis();
					
//					System.out.println((t2a-t1a)+"\t"+(t3-t2a)+"\t"+(t4-t3));
//					System.out.println(timeJson+"\t"+timeHtml);
					
					PredictionReport pr=new PredictionReport(pd, fileJson, fileHtml, lanId);
					
					pd.setPredictionReport(pr);
					
					if(writeReportToHarddrive) {

						String folder = "data\\"+pd.getModel().getSource().getName()+"\\reports\\"+predictionResults.getDTXSID();
						File Folder=new File(folder);
						if(!Folder.exists()) Folder.mkdirs();

						String filename=tr.chemicalIdentifiers.dtxsid+"_"+tr.modelDetails.modelName+".html";
						h.writeStringToFile(fileHtml,folder,filename);
						h.writeStringToFile(fileJson,folder,filename.replace(".html",".json"));

						
						Utilities.saveJson(predictionResults, folder+File.separator+predictionResults.getDTXSID()+"_"+predictionResults.getEndpoint()+".json");
						
//						System.out.println(folder+File.separator+filename);
						
						gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.viewInWebBrowser(folder+File.separator+filename);

					}

					long t2=System.currentTimeMillis();
					
//					System.out.println((tR2-tR1)+"\t"+(t2-t1));//majority of time is from report creation
					
					

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			} catch (Exception e) {
				e.printStackTrace();
			} 

			return pd;
		}
		
		

		private void standardizeError(PredictionDashboard pd) {

			
			
			if (pd.getPredictionError().contains("could not parse")) {
				pd.setPredictionError(strInvalidStructure+": could not parse smiles");	
			} else if(pd.getPredictionError().toLowerCase().contains("unsupported element")) {
				pd.setPredictionError(strInvalidStructure+": unsupported element");
			} else if(pd.getPredictionError().toLowerCase().contains("only one nonhydrogen atom")) {
				pd.setPredictionError(strInvalidStructure+": only one nonhydrogen atom");
			} else if(pd.getPredictionError().toLowerCase().contains("number of atoms equals zero")) {
				pd.setPredictionError(strInvalidStructure+": number of atoms equals zero");
			} else if(pd.getPredictionError().toLowerCase().contains("does not contain carbon")) {
				pd.setPredictionError(strInvalidStructure+": does not contain carbon");
			} else if(pd.getPredictionError().toLowerCase().contains("multiple molecules")) {
				pd.setPredictionError(strInvalidStructure+": multiple molecules");
			} else if(pd.getPredictionError().toLowerCase().contains("paths")) {
				pd.setPredictionError(strDescriptorGeneration+": timeout finding paths");
			} else if(pd.getPredictionError().toLowerCase().contains("rings")) {
				pd.setPredictionError(strDescriptorGeneration+": timeout finding rings");
			} else if (pd.getPredictionError().equals("No prediction could be made due to applicability domain violations")) {
			} else if(pd.getPredictionError().toLowerCase().contains("one prediction")) {
			} else {
				System.out.println("Other:"+pd.getPredictionError());
			}
		}

		private String getModelNameCCD(String propertyAbbrev) {
			String abbrev=propertyAbbrev;
			if(propertyAbbrev.equals("LC50")) {
				abbrev="FHM_LC50";
			} else if(propertyAbbrev.equals("LC50DM")) {
				abbrev="DM_LC50";
			} else if(propertyAbbrev.equals("IGC50")) {
				abbrev="TP_IGC50";
			} else if (propertyAbbrev.equals("Density")) {
				abbrev="D";
			} else if (propertyAbbrev.equals("LD50")) {
				abbrev="Rat_LD50";
			} else if (propertyAbbrev.equals("Viscosity")) {
				abbrev="V";
			}
			
			String nameCCD="TEST_"+abbrev;
			
			return nameCCD;
		}

		private void addNeighbors(PredictionResults pr, PredictionDashboard pd) {


			if(pr.getSimilarChemicals().size()==0) return;

			List<QsarPredictedNeighbor>qpns=new ArrayList<>();
			pd.setQsarPredictedNeighbors(qpns);

			SimilarChemicals scTestSet=pr.getSimilarChemicals().get(0);//test set similar chemicals are first
			addNeighbors(pr, pd, qpns, scTestSet,1);

			SimilarChemicals scTrainingSet=pr.getSimilarChemicals().get(1);
			addNeighbors(pr, pd, qpns, scTrainingSet,0);

//			QsarPredictedNeighbor.addNeighborMetadata(tableMaps, pr.getEndpoint(), qpns);

		}


		private void addNeighbors(PredictionResults pr, PredictionDashboard pd, List<QsarPredictedNeighbor> qpns,
				SimilarChemicals scTestSet, int split_num) {

			int counter=0;

			for (SimilarChemical sc:scTestSet.getSimilarChemicalsList()) {
				QsarPredictedNeighbor qpn=new QsarPredictedNeighbor();

				qpn.setNeighborNumber(++counter);
				
				//				qpn.setDtxsid(sc.getDSSTOXSID());//let the lookup figure it out fresh
				qpn.setPredictionDashboard(pd);
				qpn.setSplitNum(split_num);
				qpn.setSimilarityCoefficient(Double.parseDouble(sc.getSimilarityCoefficient()));

				qpn.setExperimentalValue(sc.getExpVal());
				qpn.setPredictedValue(sc.getPredVal());
				
				qpn.setCasrn(sc.getCAS());
				qpn.setDtxsid(sc.getDSSTOXSID());
				
//				if(sc.getDSSTOXSID()!=null && tableMaps.mapDsstoxRecordsBySID.containsKey(sc.getDSSTOXSID())) {
//					qpn.setDsstoxRecord(tableMaps.mapDsstoxRecordsBySID.get(sc.getDSSTOXSID()));
////					DsstoxRecord dr=new DsstoxRecord();
////					dr.setDtxsid(sc.getDSSTOXSID());
////					dr.setMolImagePNGAvailable(true);//TODO
////					dr.setPreferredName(sc.getPreferredName());
////					dr.setDtxcid(sc.getDSSTOXCID());
////					qpn.setDsstoxRecord(dr);
//				} else if(sc.getCAS()!=null && tableMaps.mapDsstoxRecordsByCAS.containsKey(sc.getCAS())) {
//					qpn.setDsstoxRecord(tableMaps.mapDsstoxRecordsByCAS.get(sc.getCAS()));
////					System.out.println("Assigned neighbor by CAS="+sc.getCAS());
//				}
				
				
				if(sc.getCAS()!=null && tableMaps.mapDsstoxRecordsByCAS.containsKey(sc.getCAS())) {
					qpn.setDsstoxRecord(tableMaps.mapDsstoxRecordsByCAS.get(sc.getCAS()));
//					System.out.println("Assigned neighbor by CAS="+sc.getCAS());
				} else {
//					System.out.println("For "+pr.getDTXSID()+", "+pr.getEndpoint()+" couldn't assign dsstox record for neighbor with casrn="+sc.getCAS());
				}
				
//				
//				System.out.println(qpn.getDtxsid()+"\t"+qpn.getDsstoxRecord().isMolImagePNGAvailable());
				
				
				qpns.add(qpn);
			}
		}

		private void setNeighborExperimentalValue(PredictionResults pr,SimilarChemical sc,QsarPredictedNeighbor qpn) {

			if(sc.getExpVal()==null) return;

			//			if (pr.isLogMolarEndpoint()) {
			//				qpn.setExperimentalValue(convertLogMolarUnits(pr.getEndpoint(), sc.getExpVal()));
			//			} else {
			//				qpn.setExperimentalValue(Double.parseDouble(sc.getExpVal()));			
			//			}

			//Dont convert needs to match plot
			qpn.setExperimentalValue(sc.getExpVal());	


		}

		private void setNeighborPredictedValue(PredictionResults pr,SimilarChemical sc,QsarPredictedNeighbor qpn) {

			if(sc.getPredVal()==null) return;
			//							
			//			if (pr.isLogMolarEndpoint()) {
			//				qpn.setPredictedValue(convertLogMolarUnits(pr.getEndpoint(), sc.getPredVal()));
			//			} else {
			//				qpn.setPredictedValue(Double.parseDouble(sc.getPredVal()));			
			//			}

			//Dont convert needs to match plot
			qpn.setPredictedValue(sc.getPredVal());
		}



		private void addApplicabilityDomain(PredictionResults pr,PredictionDashboard pd, MethodAD methodAD) {

			QsarPredictedADEstimate adEstimate=new QsarPredictedADEstimate();
			adEstimate.setCreatedBy(lanId);
			adEstimate.setMethodAD(methodAD);


			if(pd.getPredictionError()==null) {
				adEstimate.setApplicabilityValue(1.0);
				adEstimate.setConclusion("Inside");
				adEstimate.setReasoning("Compound is inside TEST applicability domains");

			} else {
				//			if(pr.getDTXSID().equals("DTXSID7020005"))
				//				System.out.println("Error not null, "+pr.getDTXCID()+"\t"+pr.getEndpoint()+", error="+pd.getPredictionError());

				adEstimate.setApplicabilityValue(0.0);
				adEstimate.setConclusion("Outside");

				//				"The consensus prediction for this chemical is considered unreliable since only one prediction can only be made"
				//				"No prediction could be made due to applicability domain violation"
				//				"FindPaths"
				//				"FindRings"
				//				"Only one nonhydrogen atom"
				//				"Molecule does not contain carbon"
				//				"Molecule contains unsupported element"
				//				"Multiple molecules"

				
				adEstimate.setReasoning(pd.getPredictionError());

//				String e=pd.getPredictionError();
//				if(e.contains("unreliable") || e.contains("applicability")) {
//					adEstimate.setReasoning(pd.getPredictionError());	
//				} else if(e.contains("FindPaths") || e.contains("FindRings")) {
//					adEstimate.setReasoning("Descriptor calculation failed during "+e);	
//				} else {
//					adEstimate.setReasoning("Invalid structure: "+e);
//				}

			}

			adEstimate.setPredictionDashboard(pd);
			List<QsarPredictedADEstimate>adEstimates=new ArrayList<>();
			adEstimates.add(adEstimate);

			pd.setQsarPredictedADEstimates(adEstimates);

		}

		private void setExperimentalPredictedValues(PredictionResults pr,PredictionDashboard pd,PredictionDashboardTableMaps maps) {
			
			boolean printValues=false;
			
			
			if(pr.getPredictionResultsPrimaryTable()==null)return;
			
			Model model=pd.getModel();
			Dataset dataset=maps.mapDatasets.get(model.getDatasetName());
			
			String unitName=dataset.getUnit().getName();
			
			String finalUnitName=dataset.getUnitContributor().getName();
			DsstoxRecord dr=pd.getDsstoxRecord();
			String propertyName=dataset.getProperty().getName();
			String dtxcid=pd.getDtxcid();
			Double molecularWeight=dr.getMolWeight();

			Double predValue=pr.getPredValueInModelUnits();
			
			
			if(predValue!=null) {
				
				Double valueConverted=UnitConverter.convertUnits(predValue, unitName, finalUnitName,molecularWeight, propertyName, dtxcid);
				pd.setPredictionValue(valueConverted);
				
				if(dr.getSmiles().contains("|") || dr.getSmiles().contains("*")) {//UVCB
					pd.setPredictionValue(null);
					pd.setPredictionError(strInvalidStructure+": Markush structure");
				} else if(dr.getMolWeight()==null) {
//					System.out.println("No MW for "+dr.getDtxsid()+"\t"+dr.getSmiles());
				}
				
				if(printValues) {//for comparison with TEST software
					
					String strValueConverted=HTMLReportCreator.getFormattedValue(false, valueConverted, propertyName, 3);
					String units=dataset.getUnitContributor().getAbbreviation_ccd();
					
					if(units.contains("mol/")) {
						
						if(dr.getMolWeight()!=null) {
							Double valueMg=valueConverted*dr.getMolWeight()*1000.0;
							String strValueConvertedMg=HTMLReportCreator.getFormattedValue(false, valueMg, propertyName, 3);
							String unitsMg=units.replace("mol", "mg");
							System.out.println(dr.getDtxsid()+"\t"+propertyName+"\t"+strValueConvertedMg+"\t"+unitsMg);
						} else {
							System.out.println("Missing MW for "+dr.getDtxsid()+"\t"+dr.getSmiles());
						}
						
					} else {
						System.out.println(dr.getDtxsid()+"\t"+propertyName+"\t"+strValueConverted+"\t"+units);
					}
				}
			}
			
			Double expValue=pr.getExpValueInModelUnits();
			if(expValue!=null) {
				Double valueConverted=UnitConverter.convertUnits(expValue, unitName, finalUnitName,molecularWeight, propertyName, dtxcid);
				pd.setExperimentalValue(valueConverted);	
			}
			
		}

//		//Is this needed?
//		private void fixUnitsBCF(PredictionResults predictionResults) {
//			if(predictionResults.getPredictionResultsPrimaryTable()!=null) {
//				if(predictionResults.getEndpoint().equals("Bioconcentration factor")) {
//					predictionResults.getPredictionResultsPrimaryTable().setMolarLogUnits("log10(L/kg)");
//					if(predictionResults.getIndividualPredictionsForConsensus()!=null) {
//						predictionResults.getIndividualPredictionsForConsensus().setUnits("log10(L/kg)");
//					}
//				}
//			}
//		}

	}
	
	public static String convertToInClause(HashSet<String>items) {
		StringBuilder sb = new StringBuilder();

		int count = 0;
		for (String item : items) {
			sb.append("'").append(item).append("'");
			if (count < items.size() - 1) {
				sb.append(", ");
			}
			count++;
		}

		return sb.toString();
	}
	
	public static List<DSSToxRecord> getDsstoxRecords(HashSet<String>dtxsids) {

		String sql="SELECT gs.dsstox_substance_id,c.dsstox_compound_id,gs.casrn,c.smiles,gs.preferred_name FROM generic_substances gs\r\n"
				+ "	         join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id\r\n"
				+ "	join compounds c on gsc.fk_compound_id = c.id\r\n"
				+ "	where gs.dsstox_substance_id in ("+convertToInClause(dtxsids)+");";

//		System.out.println(sql);
		List<DSSToxRecord>recs=new ArrayList<>();
		try {
			ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionDSSTOX(), sql);
			while (rs.next()) {
				DSSToxRecord dr=new DSSToxRecord();
				dr.sid=rs.getString(1);
				dr.cid=rs.getString(2);
				dr.cas=rs.getString(3);
				dr.smiles=rs.getString(4);
				dr.name=rs.getString(5);
				dr.mol=null;
				recs.add(dr);
			}
		}catch (Exception ex) {
			ex.printStackTrace();
		}
		return recs;
	}

	/**
	 * For chemicals in TEST training/test sets
	 */
	void createDsstoxRecordLookup () {
		
		Hashtable<String, String>htCAS_to_SID=GetDTXSIDLookup.getDtxsidLookupByCAS();
		Hashtable<String, HashSet<String>>htSID_to_CAS=new Hashtable<>();
		
		for(String casrn:htCAS_to_SID.keySet()) {
			String sid=htCAS_to_SID.get(casrn);
			
			if(htSID_to_CAS.containsKey(sid)) {
				 HashSet<String>casrns=htSID_to_CAS.get(sid);
				 casrns.add(casrn);
			} else {
				 HashSet<String>casrns=new HashSet<>();
				 casrns.add(casrn);
				 htSID_to_CAS.put(sid,casrns);
			}
			
//			System.out.println(sid+"\t"+casrn);
		}
		
		
//		Hashtable<String, gov.epa.databases.dsstox.DsstoxRecord>htCAS_to_DsstoxRecord=new Hashtable<>();
		Hashtable<String, DSSToxRecord>htCAS_to_DSSToxRecord=new Hashtable<>();
//		GenericSubstanceServiceImpl gss=new GenericSubstanceServiceImpl();
		
		int counter=0;
		HashSet<String>sids=new HashSet<>();
		
		for (String sid:htSID_to_CAS.keySet()) {
			
			counter++;
			sids.add(sid);
			
			if(sids.size()==1000) {
				System.out.println(counter);
				List<DSSToxRecord>recs= getDsstoxRecords(sids);
				
				for(DSSToxRecord rec:recs) {
					HashSet<String>casrnsOriginal=htSID_to_CAS.get(rec.sid);
					for(String casrnOriginal:casrnsOriginal) {
						htCAS_to_DSSToxRecord.put(casrnOriginal,rec);	
					}
				}
				
				sids.clear();
			}
		}

		//do remaining:
		List<DSSToxRecord>recs= getDsstoxRecords(sids);
		for(DSSToxRecord rec:recs) {
			HashSet<String>casrnsOriginal=htSID_to_CAS.get(rec.sid);
			for(String casrnOriginal:casrnsOriginal) {
				htCAS_to_DSSToxRecord.put(casrnOriginal,rec);	
			}
		}

		
//		for(String casOriginal:htCAS_to_DSSToxRecord.keySet()) {
//			DSSToxRecord rec=htCAS_to_DSSToxRecord.get(casOriginal);
//			if(rec.smiles==null || rec.smiles.contains("|")) {
//				System.out.println(rec.cas+"\t"+rec.smiles);
//			}
//			if(rec.cid==null) {
//				System.out.println(rec.cas+"\t"+rec.smiles);
//			}
//			if(!casOriginal.equals(rec.cas)) {
//				System.out.println(casOriginal+"\t"+rec.cas);
//			}
//		}
		

		try {
			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18_EPA_Github\\jar\\add dependencies\\Datasets-1.1.1\\gov\\epa\\webtest\\";
			FileWriter fw= new FileWriter (folder+"DsstoxRecord_lookup_from_cas.json");
			fw.write(WebTEST4.gson.toJson(htCAS_to_DSSToxRecord));
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	class MyRunnableTask implements Runnable {
	    private int num;
	    private String folderDest;
	    Boolean writeToDB;
	    Boolean skipER;
	    String dtxsid;
	    Boolean writeReportToHarddrive;
	    Boolean writePredictionReportJson;
		HashSet<String> cidsLoaded;
	    
	    public MyRunnableTask(int num,String folderDest,boolean writeToDB,boolean skipER, 
	    		String dtxsid,boolean writeReportToHarddrive, HashSet<String> cidsLoaded) {
	    	this.num = num;
	        this.folderDest=folderDest;
	        this.writeToDB=writeToDB;
	        this.skipER=skipER;
	        this.dtxsid=dtxsid;
	        this.writeReportToHarddrive=writeReportToHarddrive;
	        this.cidsLoaded=cidsLoaded;
	    }

	    @Override
	    public void run() {
	        System.out.println("num "+num + " is running in thread: " + Thread.currentThread().getName());
	        String filePathJson=folderDest+"prod_compounds"+num+".json";
			List<PredictionDashboard>pds=runFromDashboardJsonFileBatchPost(filePathJson,writeToDB,
					skipER, dtxsid,writeReportToHarddrive,cidsLoaded);
	    }
	}
	
	
	void runWithThreads() {
		
		
		boolean writeToDB=true;
		boolean skipER=true;
		boolean	writeReportToHarddrive=false;

		File fileJsonDsstoxRecords=PredictionDashboardTableMaps.fileJsonDsstoxRecords2025_10_30;
		File fileJsonOtherCAS=PredictionDashboardTableMaps.fileJsonOtherCAS2025_10_30;
		tableMaps=new PredictionDashboardTableMaps(fileJsonDsstoxRecords,fileJsonOtherCAS);//creates lookup maps for database objects so dont have to keep query the database
		
		String sourceName="TEST" + version;
		HashSet<String>cidsLoaded=DatabaseUtilities.getLoadedCIDsWithCount(sourceName, 16);
		System.out.println("cidsLoaded.size()="+cidsLoaded.size());

		
		String snapshot = "snapshot-2025-07-30";
		String folderMain = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\";
		String folderDest = folderMain + "data\\TEST5.1.3\\reports\\" + snapshot + "\\";
		
		for (int i = 12; i <= 22; i++) {
            MyRunnableTask task = this.new MyRunnableTask(i, folderDest, writeToDB, skipER,
            		null, writeReportToHarddrive,cidsLoaded);
            		Thread thread = new Thread(task, "Thread-" + i);
            thread.start(); // Starts the thread, which calls the run() method
        }
		
	}
	
	
	void loadFromJsonFiles() {

//		boolean writeToDB=false;
		boolean writeToDB=true;
		boolean skipER=true;
		boolean writeReportToHarddrive=true;
		String dtxsid=null;
		//		String dtxsid="DTXSID101256899";
//				String dtxsid="DTXSID40166952";//has exp BP
		//		String dtxsid="DTXSID3039242";//bz
//		String dtxsid="DTXSID40167000";
//		String dtxsid="DTXSID9039234";//salt
//		String dtxsid="DTXSID50442193";
//		String dtxsid="DTXSID7020001";
//		String dtxsid="DTXSID0020022";
//		String dtxsid="DTXSID40177523";

		if(dtxsid==null) {
			writeReportToHarddrive=false;
		}

		File fileJsonDsstoxRecords=PredictionDashboardTableMaps.fileJsonDsstoxRecords2025_10_30;
		File fileJsonOtherCAS=PredictionDashboardTableMaps.fileJsonOtherCAS2025_10_30;
		
		if(tableMaps==null) {
			tableMaps=new PredictionDashboardTableMaps(fileJsonDsstoxRecords,fileJsonOtherCAS);//creates lookup maps for database objects so dont have to keep query the database
		}

		String sourceName="TEST" + version;
		HashSet<String>cidsLoaded=DatabaseUtilities.getLoadedCIDsWithCount(sourceName, 16);
		System.out.println("cidsLoaded.size()="+cidsLoaded.size());

		
		String snapshot = "snapshot-2025-07-30";
		String folderMain = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\";
		String folderDest = folderMain + "data\\TEST5.1.3\\reports\\" + snapshot + "\\clowder\\";
		
//		int num=13;
//		String filePathJson=folderDest+"prod_compounds"+num+".json";
//		List<PredictionDashboard>pds=runFromDashboardJsonFileBatchPost(filePathJson,writeToDB,skipER, dtxsid,writeReportToHarddrive,cidsLoaded);
		
		for (int num=7;num<=11;num++) {
			String filePathJson=folderDest+"prod_compounds"+num+".json";
			List<PredictionDashboard>pds=runFromDashboardJsonFileBatchPost(filePathJson,writeToDB,skipER, dtxsid,writeReportToHarddrive,cidsLoaded);
		}
		
	}
	


	void loadFromJsonFile() {

//		boolean writeToDB=false;
		boolean writeToDB=true;
		boolean skipER=true;
		boolean writeReportToHarddrive=true;

		String dtxsid=null;
//		String dtxsid="DTXSID40177523";
//		String dtxsid="DTXSID701532926";

		if(dtxsid==null) {
			writeReportToHarddrive=false;
		}

		File fileJsonDsstoxRecords=PredictionDashboardTableMaps.fileJsonDsstoxRecords2025_10_30;
		File fileJsonOtherCAS=PredictionDashboardTableMaps.fileJsonOtherCAS2025_10_30;		
		if(tableMaps==null) {
			tableMaps=new PredictionDashboardTableMaps(fileJsonDsstoxRecords,fileJsonOtherCAS);//creates lookup maps for database objects so dont have to keep query the database
		}

		String sourceName="TEST" + version;
		HashSet<String>cidsLoaded=DatabaseUtilities.getLoadedCIDsWithCount(sourceName, 16);
		System.out.println("cidsLoaded.size()="+cidsLoaded.size());

		String snapshot = "snapshot-2025-07-30";
		String folderMain = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\";
		String folderDest = folderMain + "data\\TEST5.1.3\\reports\\" + snapshot + "\\";
		String filenameJson="prod_compounds_no_test_prediction.json";
		String filePathJson=folderDest+filenameJson;
		List<PredictionDashboard>pds=runFromDashboardJsonFileBatchPost(filePathJson,writeToDB,skipER, dtxsid,writeReportToHarddrive,cidsLoaded);
		
		
	}
	
	List<PredictionDashboard> runFromDashboardJsonFileBatchPost(String filepathJson, boolean writeToDB, boolean skipER, 
			String dtxsid, boolean writeReportToHarddrive,HashSet<String>cidsLoaded) {

		try {

//			int skip=45000*16;
			int skip=-1;
			
			
			String filename=new File(filepathJson).getName();
			
			System.out.println("Loading "+filepathJson);

//			String snapshotName="DSSTOX Snapshot 11/12/2024";		
//			DsstoxSnapshotServiceImpl snapshotService = new DsstoxSnapshotServiceImpl();
//			DsstoxSnapshot snapshot = snapshotService.findByName(snapshotName);

			String sourceName="TEST" + version;
//			SourceService sourceService=new SourceServiceImpl();
//			Source source=sourceService.findByName(sourceName);
			

			BufferedReader br=new BufferedReader(new FileReader(filepathJson));

			int counter=0;
//			int batchSize=16*100;
			int batchSize=1000;
			
			List<PredictionDashboard>predictionsDashboard=new ArrayList<>();

			//Get list of prediction dashboard keys already in the database:
			
//			System.out.println("Getting loaded cids...");

//			HashSet<String>cidsLoaded=new HashSet<>();

//			Hashtable<String, Long>htModelNameToID=initializeDB.getModelNameToModelID_Hashtable();
			
			
			Hashtable<String, String>htError=new Hashtable<>();
			
			List<PredictionDashboard>flaggedPDs=new ArrayList<>();
			

			boolean foundDtxsid=false;

			while (true) {
				//				System.out.println("start loop");

				String strPredictionResults=br.readLine();
				if(strPredictionResults==null) break;
				
				counter++;
				
				//	System.out.println(strPredictionResults);

				if(counter%(1000*16)==0) {
					System.out.println(counter/16+"\t"+filename);
				}
				
				if(counter<skip) continue;
				
				PredictionResults predictionResults=null;
				
				long t1=System.currentTimeMillis();
				
				try {
					predictionResults=Utilities.gson.fromJson(strPredictionResults,PredictionResults.class);
				} catch (Exception ex) {
					System.out.println("cant parse json from line");
					continue;
				}
				
				long t2=System.currentTimeMillis();
				
				
//				standardizePredictionResultsError(predictionResults);
				
				
//				if(!predictionResults.getError().isBlank()) {
//					System.out.println(predictionResults.getDTXSID()+"\t"+predictionResults.getEndpoint()+"\t"+predictionResults.getError());
//				}
				long timeParseJson=t2-t1;
				
//				System.out.println("Time to parse json:"+(t2-t1)+"ms");
				
				if(cidsLoaded.contains(predictionResults.getDTXCID())) {
//					System.out.println("Skipping dtxcid="+predictionResults.getDTXCID());
					continue;
				}
				
				if (skipER) {
					String propNameLC=predictionResults.getEndpoint().toLowerCase();
					if (propNameLC.contains("estrogen"))continue;
				}
				
//				if(predictionResults.getPredictionResultsPrimaryTable().getExpCAS()!=null) {
//					if(predictionResults.getEndpoint().equals(TESTConstants.ChoiceWaterSolubility)) {
//						System.out.println(predictionResults.getDTXSID()+"\t"+predictionResults.getEndpoint()+"\tHas exp");
//					}
//				}

				if(dtxsid!=null) {
					if(!predictionResults.getDTXSID().equals(dtxsid)) {
						if(foundDtxsid)break;
						else {
							//System.out.println(dtxsid+"\t"+predictionResults.getDTXSID());
							continue;
						}
					} else {
						if(!foundDtxsid) System.out.println("found dtxsid");
						foundDtxsid=true;
					}
				}
				
//				System.out.println(strPredictionResults);
				//System.out.println(Utilities.gson.toJson(predictionResults));

				long t3=System.currentTimeMillis();
				PredictionDashboard pd=converter.convertPredictionResultsToPredictionDashboard(predictionResults,writeReportToHarddrive);
				long t4=System.currentTimeMillis();
				
				long timeConvert=t4-t3;
				
				
//				System.out.println(timeParseJson+"\t"+timeConvert);
				
//				System.out.println("Time to do misc:"+(t3-t2)+"ms");
//				System.out.println("Time to convert to pd:"+(t4-t3)+"ms");
				
				if(pd==null) {
//					System.out.println(predictionResults.getDTXCID()+"\tpd==null");
					continue;
				}

				//System.out.println(pd.toJson());

				predictionsDashboard.add(pd);
				
//				System.out.println("here pds.size()="+predictionsDashboard.size());
				

				if(predictionsDashboard.size()==batchSize) {
					//					System.out.println(counter);
					if(writeToDB) {
						System.out.println("Writing to db:"+Thread.currentThread().getName()+"\t"+counter/16);
						predictionDashboardService.createSQL(predictionsDashboard);
					}
					predictionsDashboard.clear();
				}
				
				if(dtxsid!=null) {
					flaggedPDs.add(pd);
				}
				
				//gets examples of each error:
//				if(pd.getPredictionError()!=null && !htError.containsKey(pd.getPredictionError())) {
////					System.out.println(pd.getPredictionError()+"\t"+predictionResults.getDTXSID()+"\t"+pd.getModel().getName());
//					htError.put(pd.getPredictionError(), predictionResults.getDTXSID()+"\t"+pd.getModel().getName());
////					flaggedPDs.add(pd);
//					String html=new String(pd.getPredictionReport().getFileHtml());
//					String folder="data\\TEST5.1.3\\reports\\error";
//					String filename=predictionResults.getDTXSID()+"_"+pd.getModel().getName()+".html";
//					HTMLReportCreator.writeStringToFile(html, folder,filename);
////					HTMLReportCreator.viewInWebBrowser(folder+File.separator+filename);
//				}
			}
			br.close();

			System.out.println("exited main loop");
//			System.out.println("remaining to load: "+predictionsDashboard.size());

			//Do what's left:
			if(writeToDB) predictionDashboardService.createSQL(predictionsDashboard);

			return flaggedPDs;
					
			
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 

	}
	
	/**
	 * Run the DsstoxRecords chemicals that dont have predictions
	 */
	void getCompoundsMissingTESTPredictions() {
		
		//First determine which dtxcids are missing from dsstoxrecords:
		List<String>dtxcids=getDtxcidsMissingPredictionsInDsstoxRecords(3,"TEST5.1.3");	
		System.out.println("dtxcids.size()="+dtxcids.size());		
		
		DSSTOX_Compounds_Script dcs=new DSSTOX_Compounds_Script();
		List<DsstoxCompound>compounds=dcs.getCompoundsBySubstanceSQL(dtxcids);
		
		String date="2025-07-30";
//		String date="2025-10-30";
		String folder="data\\dsstox\\snapshot-"+date+"\\json filter\\";
		File file=new File(folder+"prod_compounds_no_test_prediction2.json");
		
		try (FileWriter fw=new FileWriter(file);){
			fw.write(Utilities.gson.toJson(compounds));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("compounds.size()="+compounds.size());
		
		//see ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromCompoundsJson.runFromJson();

	}

	private List<String> getDtxcidsMissingPredictionsInDsstoxRecords(int fk_dsstox_snapshot_id,String sourceName ) {
		String sql="SELECT distinct dtxcid from qsar_models.dsstox_records dr\r\n"
				+ "where dr.fk_dsstox_snapshot_id="+fk_dsstox_snapshot_id+" and\r\n"
				+ "dr.smiles not like '%.%' and dr.smiles not like '%|%' and\r\n"
				+ "dr.smiles not like '%*%' and\r\n"
				+ "dr.dtxcid not in (\r\n"
				+ "		SELECT distinct pd.dtxcid from qsar_models.predictions_dashboard pd\r\n"
				+ "		join qsar_models.models m on m.id=pd.fk_model_id\r\n"
				+ "		join qsar_models.sources s on s.id=m.fk_source_id\r\n"
				+ "		where s.name='"+sourceName+"');";
				
		try {
		
			ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
			
			List<String>dtxcids=new ArrayList<>();
			while (rs.next()) {
				dtxcids.add(rs.getString(1));
			}
			
			return dtxcids;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	
	public static void main(String[] args) {

		PredictionDashboardScriptTEST2 pds=new PredictionDashboardScriptTEST2();
		
//		List<String>identifiers=Arrays.asList("Benzene","Bisphenol-A","4-Aminobenzoic acid","Polychlorinated biphenyls",
//				"Creosote","4-Piperidinol, 4-phenyl-1-(3-phenyl-2-propynyl)-, propionate (ester) hydrochloride");

		
		List<String> identifiers = Arrays.asList("DTXSID3039242", "DTXSID7020182", "DTXSID6024466", "DTXSID5024267",
				"DTXSID2023987", "DTXSID20211176");

//		List<String> identifiers = Arrays.asList("DTXSID7020182","DTXSID3039242");

//		boolean storeInDB=false;
//		boolean storeInDB=true;
//
//		for(String identifier:identifiers) {
//			pds.runNewChemical(identifier,storeInDB);
//		}
		
//		pds.runNewChemical("benzene");
//		pds.runNewChemical("1-butanol");
//		pds.runNewChemical("87689-21-2");
//		pds.runNewChemical("bisphenol-a");
//		pds.runNewChemical("DTXSID40177523");
//		pds.runNewChemical("DTXSID401020813",false);
//		pds.runNewChemical("DTXSID501030300",false);//paths error
		
		
		
//		pds.convertPredictionResultsToWebPages();
				
		//TODO fix situation where a molecule is a salt and it output the model graphs but no stats
		//TODO, do we want the have the model stats graphs when it had a bad structure?
		
//		pds.loadFromJsonFiles();
//		pds.runWithThreads();
		
		pds.getCompoundsMissingTESTPredictions();
//		pds.loadFromJsonFile();
		
		
		
		
//		pds.createDsstoxRecordLookup();
	}


	void convertPredictionResultsToWebPages() {

		String dtxsid="DTXSID5039224";
		int num=7;
		int from = 1 + 50000 * (num - 1);

		String snapshot = "snapshot-2025-07-30";
		String folderDest = "data\\TEST5.1.3\\reports\\" + snapshot + "\\";
		
		String filenameSDF = "50k_chunk_from_" + from + ".sdf";
		String filenameJson = filenameSDF.replace(".sdf", ".json");
		String destJsonPath = folderDest + filenameJson;

		String filenameHTML = dtxsid+"_Dashboard.html";
		String destHtmlPath=folderDest+filenameHTML;
		
		Gson gson=new Gson();
		
		List<PredictionResults>listPredictionResults=new ArrayList<>();
		
		Hashtable<String, Long>htModelNameToID=initializeDB.getModelNameToModelID_Hashtable();
		
		
		if(tableMaps==null) {
//			tableMaps=new PredictionDashboardTableMaps(dtxsid,snapshot.getId());	
			tableMaps=new PredictionDashboardTableMaps(PredictionDashboardTableMaps.fileJsonDsstoxRecords2025_10_30,PredictionDashboardTableMaps.fileJsonOtherCAS2025_10_30);//creates lookup maps for database objects so dont have to keep query the database
		}
		
		try (BufferedReader br=new BufferedReader(new FileReader(destJsonPath))) {
			
			while (true) {
				String line=br.readLine();
				if(line==null) break;
				PredictionResults pr = gson.fromJson(line, PredictionResults.class);
				if(pr.getDTXSID().equals(dtxsid)) listPredictionResults.add(pr);
			}
			

			List<PredictionDashboard>listPredictionDashboard=new ArrayList<>();
	        listPredictionResults.sort(Comparator.comparing(PredictionResults::getEndpoint));

	    	for (PredictionResults pr:listPredictionResults) {
//				System.out.println(pr.getEndpoint()+"\t"+Utilities.gson.toJson(pr.getHmStats()));
//	    		System.out.println(pr.getEndpoint()+"\t"+pr.getPredictionResultsPrimaryTable().getExpToxValue());
	    		PredictionDashboard pd=converter.convertPredictionResultsToPredictionDashboard(pr,false);
	    		listPredictionDashboard.add(pd);
//				PredictionDashboardScriptTEST.compareToAPI(pd, pr);
	    	}

	    	HTMLReportCreator hrc=new HTMLReportCreator();
	    	String title="TEST predictions for "+dtxsid;
			String html=hrc.writeTabbedWebpage(title, listPredictionDashboard,null);
			HTMLReportCreator.writeStringToFile(html, folderDest, filenameHTML);
			HTMLReportCreator.viewInWebBrowser(destHtmlPath);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	
	private void runNewChemical(String identifier, boolean storeInDB) {

		
		
		
//		Hashtable<String, Long>htModelNameToID=initializeDB.getModelNameToModelID_Hashtable();

		String server="http://localhost";
		int port=8081;
		
//		int port = 8081;		
//		String server="http://v2626umcth882.rtord.epa.gov";

		
//		RunFromSmiles.debug=false;
		//Need to set path of structure db because it's a relative path and this is calling TEST project:
//		ResolverDb2.setSqlitePath("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18_EPA_Github\\databases\\snapshot.db");
//    	ArrayList<DSSToxRecord>recs=ResolverDb2.lookup(identifier);
//    	if(recs.size()==0) {
//    		System.out.println(identifier+" not found in database");
//    		return;
//    	}
//    	IAtomContainer ac=TaskStructureSearch.getMoleculeFromDSSToxRecords(recs);
//    	List<PredictionResults>listPredictionResults2=RunFromSmiles.runEndpointsAsList(ac, endpoints, method, createReports, createDetailedReports);
    	
    	List<PredictionResults>listPredictionResults=TestApi.runPredictionFromIdentifier(identifier, server, port);    	
    	
    	if(listPredictionResults==null) {
    		System.out.println("no results for "+identifier);
    		return;
    	}
    	
		if(tableMaps==null) {
//			tableMaps=new PredictionDashboardTableMaps(dtxsid,snapshot.getId());	
			tableMaps=new PredictionDashboardTableMaps(PredictionDashboardTableMaps.fileJsonDsstoxRecords2025_10_30,PredictionDashboardTableMaps.fileJsonOtherCAS2025_10_30);//creates lookup maps for database objects so dont have to keep query the database
		}
		
//		System.out.println(identifier+"\tMW="+tableMaps.mapDsstoxRecordsBySID.get(identifier).getMolWeight());
		
    	
		List<PredictionDashboard>listPredictionDashboard=new ArrayList<>();
        listPredictionResults.sort(Comparator.comparing(PredictionResults::getEndpoint));

    	for (PredictionResults pr:listPredictionResults) {
//			System.out.println(pr.getEndpoint()+"\t"+Utilities.gson.toJson(pr.getHmStats()));
//    		System.out.println(pr.getEndpoint()+"\t"+pr.getPredictionResultsPrimaryTable().getExpToxValue());
    		PredictionDashboard pd=converter.convertPredictionResultsToPredictionDashboard(pr,false);
    		
    		if(pd==null) {
    			continue;
    		}
    		
    		listPredictionDashboard.add(pd);
    		
//    		System.out.println(pd.getDsstoxRecord().getId());
//    		System.out.println(pd.toJson());
    		
//			PredictionDashboardScriptTEST.compareToAPI(pd, pr);
    	}

//    	System.out.println(listPredictionDashboard.get(0).getDsstoxRecord().getId());
    	
    	
    	if(storeInDB) {

    		PredictionDashboard pd0=listPredictionDashboard.get(0);
        	String dtxcid=pd0.getDtxcid();
        	String sourceName=pd0.getModel().getSource().getName();

        	String sql="DELETE FROM qsar_models.predictions_dashboard\r\n"
					+ "WHERE fk_model_id IN (\r\n"
					+ "    SELECT m.id\r\n"
					+ "    FROM qsar_models.models m\r\n"
					+ "    JOIN qsar_models.sources s ON m.fk_source_id = s.id\r\n"
					+ "	    WHERE s.name = '"+sourceName+"'\r\n"
					+ ")\r\n"
					+ "AND dtxcid = '"+dtxcid+"';";
			
			SqlUtilities.runSQLUpdate(SqlUtilities.getConnectionPostgres(), sql);

    		predictionDashboardService.createSQL(listPredictionDashboard);
    	}
    	
    	HTMLReportCreator hrc=new HTMLReportCreator();
    	
    	String title="TEST predictions for "+identifier;
		String html = hrc.writeTabbedWebpage(title, listPredictionDashboard,tableMaps.mapDatasets);
		String folder="data\\TEST5.1.3\\reports\\"+identifier+"\\";
		String filename=identifier+".html";
		
		HTMLReportCreator.writeStringToFile(html, folder, filename);
		HTMLReportCreator.viewInWebBrowser(folder+filename);
		
	}


}


