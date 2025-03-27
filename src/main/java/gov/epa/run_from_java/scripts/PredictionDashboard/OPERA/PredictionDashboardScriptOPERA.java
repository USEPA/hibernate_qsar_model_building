package gov.epa.run_from_java.scripts.PredictionDashboard.OPERA;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.PropertyServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxSnapshot;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.MethodAD;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedADEstimate;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor;
import gov.epa.databases.dev_qsar.qsar_models.entity.Source;
import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxRecordServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxSnapshotServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.MethodADServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceService;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticServiceImpl;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.models.ModelStatisticCalculator;
import gov.epa.run_from_java.scripts.QsarModelsScript;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.CreatorScript;
import gov.epa.run_from_java.scripts.PredictionDashboard.DatabaseUtilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionDashboardTableMaps;
import gov.epa.run_from_java.scripts.PredictionDashboard.OPERA_Old.Lookup;
import gov.epa.run_from_java.scripts.PredictionDashboard.OPERA_Old.SqliteUtilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.TEST.HTMLReportCreatorTEST;
import gov.epa.util.ExcelSourceReader;
import gov.epa.util.StructureUtil;

/**
 * 
 * Class to take output from OPERA software and load it into postgres db
 * 
 * @author TMARTI02
 */
public class PredictionDashboardScriptOPERA {

	static final String STR_DTXCID="DSSTOX_COMPOUND_ID";
	public static String version="2.8";
	String userName="tmarti02";


	//	boolean useLatestModelIds=false;//if false use plot images using legacy model ids 
	static boolean createReports=true;//if false use plot images using legacy model ids
	static boolean setOverallAD=true;
	static boolean useLegacyModelIds=true;
	static boolean compareOperaStructure=false;
	static boolean storeNeighbors=true;
	
	InitializeDB initializeDB=new InitializeDB();
	public ExtraMethods extraMethods=new ExtraMethods();
	ConvertToPredictionDashboard converter=new ConvertToPredictionDashboard();
	static PredictionDashboardTableMaps tableMaps;
	Loader loader=new Loader();

	ToxVal toxval=new ToxVal();


	//	OPERA_lookups lookups=null;

	public class ExtraMethods {
		/**
		 * Parses a line in OPERA output file
		 * @param line
		 * @return
		 */
		private List<String> getRecordFromLine(String line) {
			List<String> values = new ArrayList<String>();
			try (Scanner rowScanner = new Scanner(line)) {
				rowScanner.useDelimiter(",");
				while (rowScanner.hasNext()) {
					values.add(rowScanner.next());
				}
			}
			return values;
		}

		private TreeMap<String,DsstoxRecord> getDsstoxRecordsMapSample() {
			TreeMap<String,DsstoxRecord>hmRecs=new TreeMap<>();

			DsstoxRecord dr=new DsstoxRecord();

			//Naphthalene
			dr.setDtxcid("DTXCID00913");
			dr.setDtxsid("DTXSID8020913");
			dr.setSmiles("C1=CC2=CC=CC=C2C=C1");
			dr.setMolWeight(128.174);
			dr.setCid(913L);
			dr.setId(113102L);
			//TODO add preferred name

			//		dr.setDtxcid("DTXCID101");
			//		dr.setDtxsid("DTXSID7020001");
			//		dr.setSmiles("NC1=NC2=C(C=C1)C1=CC=CC=C1N2");
			//		dr.setMolWeight(183.214);
			//		dr.setId(114150L);

			hmRecs.put(dr.getDtxcid(),dr);

			return hmRecs;
		}


		public void createPlotJsonCatmos() {

			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\CATMOS\\excel files\\";

			String filepathTraining=folder+"TrainingSet.xlsx";
			String filepathEvaluation=folder+"Evaluation_set.xlsx";

			ExcelSourceReader esr=new ExcelSourceReader();

			JsonArray jaTrain=esr.parseRecordsFromExcel(filepathTraining,1, 2, true);
			JsonArray jaEval=esr.parseRecordsFromExcel(filepathEvaluation,1, 2, true);
			//			System.out.println(Utilities.gson.toJson(jaTrain));	
			//			System.out.println(Utilities.gson.toJson(jaEval));

			ConsensusModelPredictions cmpSDF=null;
			try {
				cmpSDF=Utilities.gson.fromJson(new FileReader("data\\OPERA2.8\\reports\\plots\\CATMoS-LD50 OPERA2.8_from_sdf.json"), ConsensusModelPredictions.class);
				//				System.out.println(Utilities.gson.toJson(cmpSDF));
			} catch (Exception e) {
				e.printStackTrace();
			}

			Hashtable<String,ModelPrediction>htSDF=new Hashtable<>();

			for(ModelPrediction mp:cmpSDF.mpsTraining) {
				htSDF.put(mp.id, mp);
			}

			//			System.out.println(Utilities.gson.toJson(htSDF));
			List<ModelPrediction>mpsTrain=getCatmosModelPredictions(0, jaTrain);
			List<ModelPrediction>mpsEval=getCatmosModelPredictions(1, jaEval);

			int countTrain=fillInPred(htSDF, mpsTrain);
			int countEval=fillInPred(htSDF, mpsEval);

			System.out.println(mpsTrain.size()+"\t"+countTrain);
			System.out.println(mpsEval.size()+"\t"+countEval);

			double meanExpTraining=ModelStatisticCalculator.calcMeanExpTraining(mpsTrain);
			Map<String, Double>statsTraining=ModelStatisticCalculator.calculateContinuousStatistics(mpsTrain, meanExpTraining, DevQsarConstants.TAG_TRAINING);
			Map<String, Double>statsEval=ModelStatisticCalculator.calculateContinuousStatistics(mpsEval, meanExpTraining, DevQsarConstants.TAG_TEST);

			System.out.println(Utilities.gson.toJson(statsTraining));
			System.out.println(Utilities.gson.toJson(statsEval));

			ConsensusModelPredictions cmp=new ConsensusModelPredictions();
			cmp.mpsTest=mpsEval;
			cmp.mpsTraining=mpsTrain;
			cmp.units="log10(mg/kg)";
			cmp.propertyName="Oral rat LD50";
			cmp.modelName="CATMoS-LD50 OPERA2.8";

			try {
				FileWriter fw = new FileWriter("data\\OPERA2.8\\reports\\plots\\CATMoS-LD50 OPERA2.8.json");
				fw.write(Utilities.gson.toJson(cmp));
				fw.flush();
				fw.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

			//			System.out.println("");
			//			for(ModelPrediction mp:mpsEval) {
			//				System.out.println(mp.id+"\t"+mp.exp+"\t"+mp.pred);
			//			}

		}

		private int fillInPred(Hashtable<String, ModelPrediction> htSDF, List<ModelPrediction> mps) {

			DecimalFormat df=new DecimalFormat("0.00");

			int count=0;

			for(ModelPrediction mp:mps) {
				ModelPrediction mpSDF=htSDF.get(mp.id);
				if(mpSDF==null) {
					System.out.println(mp.id+"\tNot in sdf");
					continue;
				}
				mp.pred=mpSDF.pred;

				if(mp.exp==null) {
					System.out.println(mp.id+"\tMissing exp");
				}

				count++;
				//				System.out.println(count+"\t"+mp.id+"\t"+df.format(mp.exp)+"\t"+df.format(mpSDF.exp));
			}
			return count;
		}

		private List<ModelPrediction> getCatmosModelPredictions(int split, JsonArray ja) {
			List<ModelPrediction>mps=new ArrayList<>();
			for (int i=0;i<ja.size();i++) {

				JsonObject jo=ja.get(i).getAsJsonObject();

				//				String id=jo.get("Canonical_QSAR").getAsString();
				String id=jo.get("CASRN").getAsString();

				//				String id=null;
				//				if(jo.get("CASRN")!=null) {
				//					id=jo.get("CASRN").getAsString();
				//				} else {
				//					id=jo.get("CASRN").getAsString();
				//				}

				if(jo.get("LD50_mgkg")==null) continue;

				Double exp=Math.log10(jo.get("LD50_mgkg").getAsDouble());
				Double pred=null;

				ModelPrediction mp=new ModelPrediction(id,exp,pred,split);

				mps.add(mp);

			}
			return mps;
		}

		public void createPlotJsons() {

			List<String>propertyNames=DevQsarConstants.getOPERA_PropertyNames();
			PredictionDashboardTableMaps tableMaps=new PredictionDashboardTableMaps(PredictionDashboardTableMaps.fileJsonDsstoxRecords2024_11_12,PredictionDashboardTableMaps.fileJsonOtherCAS2024_11_12);//creates lookup maps for database objects so dont have to keep query the database

			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\OPERA\\OPERA_SDFS\\";

			int i=0;
			for (String propertyName:propertyNames) {

				String modelName=initializeDB.getModelName(propertyName);
				Model model=tableMaps.mapModels.get(modelName);
				Dataset dataset=tableMaps.mapDatasets.get(model.getDatasetName());
				String unitAbbrevCCD=dataset.getUnit().getAbbreviation_ccd();				

				if(unitAbbrevCCD.contains("Binary"))continue;

				String abbrev=modelName.replace(" OPERA2.8", "");
				String sdfFileName=abbrev+"_QR.sdf";
				String abbrevExp=abbrev;

				if(abbrev.equals("KM")) abbrevExp="LogKmHL";
				else if(abbrev.equals("BioDeg")) abbrevExp="LogHalfLife";
				else if(abbrev.equals("WS")) abbrevExp="LogMolar";
				else if(abbrev.equals("FUB")) abbrevExp="FU";
				else if(abbrev.equals("Clint")) {//in sqlite we converted to nonlog but sdf still in log units
					abbrevExp="Clint";
					unitAbbrevCCD="log10("+unitAbbrevCCD+")";
				}
				else if(abbrev.equals("AOH")) abbrevExp="LogOH";
				else if(abbrev.toLowerCase().contains("catmos")) abbrevExp="CATMoS_logLD50_data";				
				else if(!abbrev.contains("Log") && unitAbbrevCCD.contains("log")) abbrevExp="Log"+abbrev;
				else {
					abbrevExp=abbrev;
					//					System.out.println("Handle abbrev="+abbrev);
				}

				if(sdfFileName.equals("CACO2_QR.sdf")) continue;//doesnt exist

				if(sdfFileName.toLowerCase().contains("catmos")) {
					//TODO look up CATMOS training set from the spreadsheet
					sdfFileName="CATMoS_QR50k.sdf";
					unitAbbrevCCD=DevQsarConstants.LOG_MG_KG;
				}
				if(sdfFileName.equals("LogKOA_QR.sdf"))sdfFileName="KOA_QR.sdf";
				if(sdfFileName.equals("FUB_QR.sdf"))sdfFileName="FU_QR.sdf";

				String sdfPath=folder+sdfFileName;
				List<ModelPrediction>mpsTest=getModelPredictionsOperaSDF(sdfPath, "test",abbrevExp);
				List<ModelPrediction>mpsTraining=getModelPredictionsOperaSDF(sdfPath, "training",abbrevExp);

				//				if(mpsTraining.size()==0) {
				//					System.out.println(++i+"\t"+abbrev+"\t"+abbrevExp+"\t"+unitAbbrevCCD+"\t"+sdfFileName+"\t"+ propertyName);
				//				}

				System.out.println(++i+"\t"+abbrev+"\t"+abbrevExp+"\t"+unitAbbrevCCD+"\t"+sdfFileName+"\t"+ propertyName+"\t"+mpsTraining.size()+"\t"+mpsTest.size());


				String propertyNameCCD=dataset.getProperty().getName_ccd();

				//Following is used by "models/make_test_plots.py" python model building project to make plots:
				saveConsensusPredictionsToJson(modelName, propertyNameCCD, mpsTest, mpsTraining, unitAbbrevCCD);
				//				if(true)break;
			}
		}

		public void uploadPlots(String sourceName) {

			QsarModelsScript qms=new QsarModelsScript("tmarti02");

			System.out.print("Getting model map...");
			TreeMap<String, Model>mapModels=CreatorScript.getModelsMap();
			System.out.println("done\n");

			String folder="data\\"+sourceName+"\\reports\\plots\\";

			String sql="select fk_model_id,fk_file_type_id from qsar_models.model_files;";

			ResultSet rs= SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);

			List<String>modelFileKeys=new ArrayList<>();

			try {
				while (rs.next()) modelFileKeys.add(rs.getInt(1)+"\t"+rs.getInt(2));
			} catch (SQLException e) {
				e.printStackTrace();
			}

			for(File file:new File(folder).listFiles()) {
				if(!file.getName().contains("png")) continue;

				String modelName=file.getName().substring(0,file.getName().indexOf("_"));

				long file_type=-1;

				if(file.getName().contains("scatter")) file_type=3;
				if(file.getName().contains("histogram")) file_type=4;

				Model model=mapModels.get(modelName);

				boolean inDB=modelFileKeys.contains(model.getId()+"\t"+file_type);

				System.out.println("\n"+modelName+"\t"+model.getId()+"\t"+file_type+"\t"+inDB);

				try {

					byte[] bytes = Files.readAllBytes(file.toPath());

					if(!inDB) {
						System.out.println("\tCreating new model file record for "+modelName+"\t"+file_type);
						qms.uploadModelFile(model.getId(),file_type, bytes);
					} else {
						System.out.println("\tUpdating new model file record for "+modelName+"\t"+file_type);
						qms.updateModelFile(model.getId(),file_type, bytes);

					}

				} catch (Exception ex ) {
					ex.printStackTrace();
				}

			}
		}

		public class ConsensusModelPredictions {
			String propertyName;
			String modelName;
			String units;

			List<ModelPrediction>mpsTest;
			List<ModelPrediction>mpsTraining;
		}

		private void saveConsensusPredictionsToJson(String modelName,String propertyNameCCD, List<ModelPrediction> mpsTest,
				List<ModelPrediction> mpsTraining, String unitName) {
			String folder="data\\OPERA2.8\\reports\\plots\\";			
			new File(folder).mkdirs();

			ConsensusModelPredictions cmp=new ConsensusModelPredictions();
			cmp.mpsTest=mpsTest;
			cmp.mpsTraining=mpsTraining;
			cmp.units=unitName;
			cmp.propertyName=propertyNameCCD;
			cmp.modelName=modelName;

			try {

				if(modelName.equals("CATMoS-LD50 OPERA2.8")) {
					modelName="CATMoS-LD50 OPERA2.8_from_sdf";
				}

				FileWriter fw = new FileWriter(folder+modelName+".json");
				fw.write(Utilities.gson.toJson(cmp));
				fw.flush();
				fw.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		private List<ModelPrediction> getModelPredictionsOperaSDF(String sdfPath, String set,String abbrevExp) {

			//			List<ModelPrediction>mps=new ArrayList<>();

			Hashtable<String,List<ModelPrediction>>htMPs=new Hashtable<>();


			AtomContainerSet acs=new AtomContainerSet();

			try {
				IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(sdfPath),DefaultChemObjectBuilder.getInstance());								

				while (mr.hasNext()) {

					AtomContainer m=null;					
					try {
						m = (AtomContainer)mr.next();
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}

					if (m==null || m.getAtomCount()==0) break;
					acs.addAtomContainer(m);


					String id=(String)m.getProperties().get("casrn");

					if(id==null || id.isBlank()) {
						id=(String)m.getProperties().get("CASRN");
					}

					if(id==null || id.isBlank()) {
						id=(String)m.getProperties().get("CAS");
					}


					//					if (id!=null && id.equals("100-00-5") && abbrevExp.toLowerCase().contains("catmos")) {
					//						System.out.println(Utilities.gson.toJson(m.getProperties()));
					//					}

					//					if (id==null) {
					//						System.out.println(Utilities.gson.toJson(m.getProperties()));
					//					}

					if(m.getProperties().get("Tr_1_Tst_0")==null) {

						if(set.equals("test")) continue;


						Integer split=0;//just put in training
						try {
							//							String id=(String)m.getProperties().get("Canonical_QSARr");

							String strExp=(String)m.getProperties().get(abbrevExp);

							Double exp=null;

							if(!strExp.isBlank()) exp=Double.parseDouble(strExp);


							Double pred=null;

							if(abbrevExp.toLowerCase().contains("catmos")) {

								if(exp==null) {
									strExp=(String)m.getProperties().get("CATMoS_LD50_mgkg");
									if(!strExp.isBlank()) exp=Math.log10(Double.parseDouble(strExp));
								}

								String strPred=(String)m.getProperties().get("CATMoS_LD50_pred");

								if(!strPred.isBlank())
									pred=Double.parseDouble(strPred);
							}

							ModelPrediction mp=new ModelPrediction(id, exp, pred, split);

							if (id!=null && id.equals("100-00-5") && abbrevExp.toLowerCase().contains("catmos")) {
								System.out.println("Here2 100-00-5");
							}


							if(htMPs.get(id)==null) {
								List<ModelPrediction>mps=new ArrayList<>();
								mps.add(mp);
								htMPs.put(id, mps);

								if (id!=null && id.equals("100-00-5") && abbrevExp.toLowerCase().contains("catmos")) {
									System.out.println("Here3a 100-00-5");
								}

							} else {

								if (id!=null && id.equals("100-00-5") && abbrevExp.toLowerCase().contains("catmos")) {
									System.out.println("Here3b 100-00-5");
								}

								List<ModelPrediction>mps=htMPs.get(id);
								mps.add(mp);
							}

						} catch (Exception ex) {
							ex.printStackTrace();
						}

					} else {
						String Tr_1_Tst_0=(String)m.getProperties().get("Tr_1_Tst_0");

						Integer split=null;

						if (set.equals("test")) {
							if(Tr_1_Tst_0.equals("1")) continue;
							split=1;//in our db we use split=1 for test set (reverse of opera)
						} else {
							if(Tr_1_Tst_0.equals("0")) continue;
							split=0;
						}

						try {
							Double exp=Double.parseDouble((String)m.getProperties().get(abbrevExp));
							Double pred=null;
							ModelPrediction mp=new ModelPrediction(id, exp, pred, split);

							if(htMPs.get(id)==null) {
								List<ModelPrediction>mps=new ArrayList<>();
								mps.add(mp);
								htMPs.put(id, mps);
							} else {
								List<ModelPrediction>mps=htMPs.get(id);
								mps.add(mp);
							}


						} catch (Exception ex) {
							//							ex.printStackTrace();
						}
					}

					//					System.out.println(Utilities.gson.toJson(m.getProperties()));


				}// end while true;

				mr.close();


				//				System.out.println(Utilities.gson.toJson(mps));
				//				System.out.println(set+"\t"+mps.size());

			} catch (Exception ex) {
				System.out.println(sdfPath+"\terror getting model predictions");
				ex.printStackTrace();
				return null;

			}

			List<ModelPrediction>mps=new ArrayList<>();

			for(String id:htMPs.keySet()) {
				List<ModelPrediction>mpsId=htMPs.get(id);


				if(mpsId.size()>1) {//Somehow, the same qsar ready smiles can have diff preds!

					System.out.println(Utilities.gson.toJson(mpsId));					
					ModelPrediction mpNew=new ModelPrediction(mpsId.get(0).id,null,null,mpsId.get(0).split);

					//Try to assemble a complete mp from the list:

					for(ModelPrediction mp:mpsId) {
						if(mp.exp!=null)mpNew.exp=mp.exp;
						if(mp.pred!=null)mpNew.pred=mp.pred;
					}
					mps.add(mpNew);
				} else if (mpsId.size()==1) {
					mps.add(mpsId.get(0));
				}
			}

			return mps;
		}


		private void getLD50ResultsSDF(String folder,String sdfFilename) {

			AtomContainerSet acs=new AtomContainerSet();

			try {
				IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(folder+sdfFilename),DefaultChemObjectBuilder.getInstance());								

				FileWriter fw=new FileWriter(folder+"catmos sdf preds.txt");

				fw.write("dsstox_compound_id\tcas\tCATMoS_LD50_mgkg\tConsensus_LD50\tCATMoS_LD50_pred\r\n");

				while (mr.hasNext()) {

					AtomContainer m=null;					
					try {
						m = (AtomContainer)mr.next();
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}

					if (m==null || m.getAtomCount()==0) break;
					acs.addAtomContainer(m);


					try {

						String dsstox_compound_id=(String)m.getProperties().get("dsstox_compound_id");

						String cas=(String)m.getProperties().get("casrn");

						if(cas==null || cas.isBlank()) {
							cas=(String)m.getProperties().get("CASRN");
						}

						if(cas==null || cas.isBlank()) {
							cas=(String)m.getProperties().get("CAS");
						}

						String CATMoS_LD50_mgkg=(String)m.getProperties().get("CATMoS_LD50_mgkg");

						String strConsensus_LD50=(String)m.getProperties().get("consensus_LD50");
						Double Consensus_LD50=null;
						if(!strConsensus_LD50.isBlank())						
							Consensus_LD50=Math.pow(10,Double.parseDouble(strConsensus_LD50));


						String strCATMoS_LD50_pred=(String)m.getProperties().get("CATMoS_LD50_pred");
						Double CATMoS_LD50_pred=null;
						if(!strCATMoS_LD50_pred.isBlank())						
							CATMoS_LD50_pred=Math.pow(10,Double.parseDouble(strCATMoS_LD50_pred));

						String line=dsstox_compound_id+"\t"+cas+"\t"+CATMoS_LD50_mgkg+"\t"+Consensus_LD50+"\t"+CATMoS_LD50_pred;
						//						System.out.println(line);

						fw.write(line+"\r\n");
						fw.flush();


					} catch (Exception ex) {
						ex.printStackTrace();
					}


				}// end while true;

				mr.close();
				fw.close();


				//				System.out.println(Utilities.gson.toJson(mps));
				//				System.out.println(set+"\t"+mps.size());

			} catch (Exception ex) {
				System.out.println(sdfFilename+"\terror getting model predictions");
				ex.printStackTrace();
			}

		}

		void printColumnValues(String colName) {

			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.9\\";

			String filepathPredictionCsv=folder+"OPERA2.8_DSSTox_082021_1_first1000.csv";

			try {
				CSVReader reader = new CSVReader(new FileReader(filepathPredictionCsv));
				String []colNames=reader.readNext();

				int colNum=-1;

				for (int i=0;i<colNames.length;i++) {
					if(colNames[i].contentEquals(colName)) {
						colNum=i;
						break;
					}
				}


				while (true) {
					String []values=reader.readNext();
					if(values==null) break;
					System.out.println(values[0]+"\t"+values[colNum]);
				} 


			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}


		/**
		 * Compares database dsstoxrecords with json file
		 */
		void findMissingDsstoxRecords() {

			PredictionDashboardTableMaps lookups=new PredictionDashboardTableMaps(PredictionDashboardTableMaps.fileJsonDsstoxRecords2023_04_04,PredictionDashboardTableMaps.fileJsonOtherCAS2023_04_04);//creates lookup maps for database objects so dont have to keep query the database

			List<DsstoxRecord>recsJson=PredictionDashboardTableMaps.dsstoxRecords;

			System.out.println(recsJson.size());

			DsstoxRecordServiceImpl rs=new DsstoxRecordServiceImpl();
			List<DsstoxRecord>recsDB=rs.findAll();

			TreeMap<Long,DsstoxRecord> mapDsstoxRecordsByID_DB=new TreeMap<>();

			for (DsstoxRecord rec:recsDB) 	{
				if(rec.getDsstoxSnapshot().getId()!=1) continue;//only use records for desired snapshot
				mapDsstoxRecordsByID_DB.put(rec.getId(),rec);
			}

			System.out.println(recsDB.size());

			for (DsstoxRecord recJson:recsJson) {

				if(mapDsstoxRecordsByID_DB.get(recJson.getId())==null) {
					System.out.println(recJson.getId()+"\t"+recJson.getDtxsid()+"\t"+recJson.getDtxcid()+"\tmissing in db");
				}

			}


		}

		private void printValues(List<PredictionDashboard> predictionsDashboard) {
			int counter=0;

			for (PredictionDashboard pd:predictionsDashboard) {
				counter++;

				System.out.println(counter);
				System.out.println(pd.getCanonQsarSmiles());
				System.out.println(pd.getCreatedBy());
				System.out.println(pd.getExperimentalString());
				System.out.println(pd.getExperimentalValue());
				System.out.println(pd.getPredictionValue());
				System.out.println(pd.getDsstoxRecord().getId());
				System.out.println(pd.getModel().getId());
				System.out.println("");

				//				System.out.println(pd.toTsv());

			}



		}

		/**
		 * Compares chemicals in OPERA sqlite db with what's in the dsstox_records table
		 * 
		 * TODO add method to check what's in sqlite with what's in predictions_dashboard table in postgresql
		 * 
		 */
		public void getChemicalsMissingInSqliteDB() {
			try {

				String sql="select DSSTOX_COMPOUND_ID from Results";
				Statement sqliteStatement=SqliteUtilities.getStatement(Lookup.conn);
				ResultSet rs=SqliteUtilities.getRecords(sqliteStatement, sql);
				HashSet <String>dtxcidsSqlite=new HashSet<>();
				while (rs.next()) dtxcidsSqlite.add(rs.getString(1));


				Hashtable <String,DsstoxRecord>htDsstoxRecordsByDtxcid=new Hashtable<>();
				String sqlDsstoxRecords="select dtxsid,dtxcid,smiles from qsar_models.dsstox_records where fk_dsstox_snapshot_id=2;";
				ResultSet rs2=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sqlDsstoxRecords);
				while (rs2.next()) {
					DsstoxRecord dr=new DsstoxRecord();
					dr.setDtxsid(rs2.getString(1));
					dr.setDtxcid(rs2.getString(2));
					dr.setSmiles(rs2.getString(3));

					if (dr.getDtxcid()==null)continue;
					if (dr.getSmiles()==null)continue;

					if (dr.getSmiles().contains("*"))continue;
					if (dr.getSmiles().contains("|"))continue;	

					htDsstoxRecordsByDtxcid.put(dr.getDtxcid(), dr);
				}

				int count=0;

				//			FileWriter fw=new FileWriter("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\difference with 11_12_24 snapshot\\difference with 11_12_24 snapshot.smi");
				//			FileWriter fw=new FileWriter("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\difference with 11_12_24 snapshot\\difference with 11_12_24_SID snapshot.smi");
				//			FileWriter fw=new FileWriter("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\difference with 11_12_24 snapshot\\difference with 11_12_24_SID_CID snapshot.smi");
				FileWriter fw=new FileWriter("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\difference with 11_12_24 snapshot\\difference with 11_12_24_SID_CID snapshot2.smi");

				List<String> badBoronCids = Arrays.asList("DTXCID701767684", "DTXCID101741705", "DTXCID501714265",
						"DTXCID701566733");

				//see what's missing:
				for (String dtxcid:htDsstoxRecordsByDtxcid.keySet()) {
					if(dtxcidsSqlite.contains(dtxcid)) continue;
					DsstoxRecord dr=htDsstoxRecordsByDtxcid.get(dtxcid);

					if(badBoronCids.contains(dr.getDtxcid())) {
						System.out.println(dr.getSmiles()+"\t"+dr.getDtxsid());
						continue;
					}

					count++;
					//				fw.write(dr.getSmiles()+"\t"+dr.getDtxcid()+"\r\n");
					fw.write(dr.getSmiles()+"\t"+dr.getDtxsid()+"\t"+dr.getDtxcid()+"\r\n");//in order to get exp values
				}
				fw.flush();
				fw.close();


			} catch (Exception ex) {
				ex.printStackTrace();
			}


		}

		//			printValues(predictionsDashboard);

		//	/**
		//	 * Save to postgres database:
		//	 * 
		//	 * @param predictionsDashboard
		//	 * @param predictionReports - not really necessary to store in database because can regenerate from predictionDashboard objects
		//	 * @param useOPERA_Image_API
		//	 */
		//	void saveToDatabase(List<PredictionDashboard>predictionsDashboard, boolean useOPERA_Image_API) {
		//
		//		PredictionDashboardServiceImpl pds=new PredictionDashboardServiceImpl();
		//
		//		//Create the predictionsDashboard (reports, neighbors, and AD estimates get created automatically by hibernate
		//		//		predictionsDashboard=pds.createBatch(predictionsDashboard);
		//		pds.createSQL(predictionsDashboard);
		//
		//		//Note: dont need to create reports separately since there is a PredictionReport object in PredictionDashboard 
		//		//		PredictionReportServiceImpl prs=new PredictionReportServiceImpl();
		//		//		predictionReports=prs.createBatch(predictionReports);
		//	}


		/**
		 * Saves reports to hard drive in html/json
		 * 
		 * @param predictionsDashboard
		 * @param useLegacyModelIds
		 * @param tableMaps
		 * @return
		 */
		private List<PredictionReport> createReports(List<PredictionDashboard> predictionsDashboard, PredictionDashboardTableMaps tableMaps) {

			List<PredictionReport>predictionReports=new ArrayList<>();

			System.out.println("Enter createReports"+predictionsDashboard.size());

			for (PredictionDashboard pd:predictionsDashboard) {
				//			qsarPredictedADEstimates.addAll(pd.getQsarPredictedADEstimates());
				//			qsarPredictedNeighbors.addAll(pd.getQsarPredictedNeighbors());
				PredictionReport pr=writeReportToHardDrive(tableMaps, pd);
				predictionReports.add(pr);
			}
			return predictionReports;
		}

		private PredictionReport writeReportToHardDrive(PredictionDashboardTableMaps tableMaps,PredictionDashboard pd) {

			HTMLReportCreatorOpera h=new HTMLReportCreatorOpera();


			String datasetName=pd.getModel().getDatasetName();
			Dataset dataset=tableMaps.mapDatasets.get(datasetName);
			String unitAbbreviation=dataset.getUnitContributor().getAbbreviation_ccd();
			Property property=dataset.getProperty();

			OPERA_Report or=new OPERA_Report(pd,property, unitAbbreviation,useLegacyModelIds);
			String json=or.toJson();

			//			System.out.println("creating report files for "+pd.getModel().getName_ccd());

			if(pd.getDsstoxRecord()==null) {
				System.out.println("missing dsstoxrecord: "+pd.getDtxcid()+" for "+property.getName());
				return null;
			}

			String folder = "data\\"+pd.getModel().getSource().getName()+"\\reports\\"+pd.getDsstoxRecord().getDtxsid();

			File Folder=new File(folder);
			Folder.mkdirs();

			h.toHTMLFile(or, folder);
			or.toJsonFile(folder);
			//			System.out.println(Utilities.gson.toJson(or)+"\n\n*******************\n");

			PredictionReport predictionReport=new PredictionReport(pd, json,null, userName);

			pd.setPredictionReport(predictionReport);

			return predictionReport;
		}

	}




	//			printValues(predictionsDashboard);

	//	/**
	//	 * Save to postgres database:
	//	 * 
	//	 * @param predictionsDashboard
	//	 * @param predictionReports - not really necessary to store in database because can regenerate from predictionDashboard objects
	//	 * @param useOPERA_Image_API
	//	 */
	//	void saveToDatabase(List<PredictionDashboard>predictionsDashboard, boolean useOPERA_Image_API) {
	//
	//		PredictionDashboardServiceImpl pds=new PredictionDashboardServiceImpl();
	//
	//		//Create the predictionsDashboard (reports, neighbors, and AD estimates get created automatically by hibernate
	//		//		predictionsDashboard=pds.createBatch(predictionsDashboard);
	//		pds.createSQL(predictionsDashboard);
	//
	//		//Note: dont need to create reports separately since there is a PredictionReport object in PredictionDashboard 
	//		//		PredictionReportServiceImpl prs=new PredictionReportServiceImpl();
	//		//		predictionReports=prs.createBatch(predictionReports);
	//	}


	private void compareOPERAStructure(Hashtable<String, OPERA_Structure> htDTXCIDToOperaStructure,
			PredictionDashboard pd) {
		String smilesOPERA=htDTXCIDToOperaStructure.get(pd.getDtxcid()).Original_SMILES;
		String smilesSnapshot=pd.getDsstoxRecord().getSmiles();

		String inchiKeyOPERA=StructureUtil.indigoInchikey1FromSmiles(smilesOPERA);
		String inchiKeySnapshot=StructureUtil.indigoInchikey1FromSmiles(smilesSnapshot);

		if(inchiKeyOPERA==null) {
			System.out.println("Null inchiKeyOPERA for "+pd.getDtxcid()+"\t"+smilesOPERA+"\t"+pd.getPredictionValue());
		} else if(!inchiKeyOPERA.equals(inchiKeySnapshot)) {
			if(pd.getDsstoxRecord().getId()!=-1)
				System.out.println(pd.getDtxcid()+"\t"+smilesOPERA+"\t"+smilesSnapshot+"\t"+pd.getDsstoxRecord().getId());	
		}
	}




	class ToxVal {

		void compareResultsToToxVal () {

			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\toxval\\";

			File fileOPERASqlite=new File(folder+"Bioconcentration factor OPERA 2.8.tsv");
			File fileToxval=new File(folder+"BCF OPERA from toxval v8.tsv");


			List<RecordToxValModel>recsSqlite=RecordToxValModel.getRecordsFromTSV(fileOPERASqlite);
			List<RecordToxValModel>recsToxval=RecordToxValModel.getRecordsFromTSV(fileToxval);

			String metric="BCF";
			boolean takeLog=true;
			double tol=0.01;

			Hashtable<String,RecordToxValModel>htSqlite=RecordToxValModel.getHashtable(recsSqlite, metric);		
			Hashtable<String,RecordToxValModel>htToxVal=RecordToxValModel.getHashtable(recsToxval, metric);

			DecimalFormat df=new DecimalFormat("0.000");

			int count=0;

			for(String dtxsid:htToxVal.keySet()) {
				RecordToxValModel rSqlite=htSqlite.get(dtxsid);
				RecordToxValModel rToxval=htToxVal.get(dtxsid);

				if(rSqlite==null) continue;
				if(rSqlite.value==null) continue;

				Double diff=null;

				if(takeLog) {
					diff=Math.abs(Math.log10(rSqlite.value/rToxval.value));
				} else {
					diff=Math.abs(rSqlite.value-rToxval.value);
				}

				if(diff>tol) {
					count++;
					System.out.println(count+"\t"+dtxsid+"\t"+df.format(rSqlite.value)+"\t"+df.format(rToxval.value)+"\t"+df.format(diff));
				}

			}

		}

		public void createToxValModelCSVFromOPERA2_8_SqliteDB() {

			version="2.8";
			setOverallAD=false;

			//		int count=40;//number of rows in the csv to use
			int stopCount=-1;

			//		HashSet<String> pd_keys = getPredictionsDashboardKeysInDB(minModelId,maxModelID);
			HashSet<String> pd_keys=new HashSet<String>();

			//		System.out.println("Number of OPERA predictions already in the database:"+pd_keys.size());

			//			File fileJsonRecords=PredictionDashboardTableMaps.fileJsonDsstoxRecords2023_04_04;
			PredictionDashboardTableMaps lookups=new PredictionDashboardTableMaps(PredictionDashboardTableMaps.fileJsonDsstoxRecords2023_04_04,PredictionDashboardTableMaps.fileJsonOtherCAS2023_04_04);//creates lookup maps for database objects so dont have to keep query the database

			//Note opera_lookups depends on two json files which make it run faster than pulling the info from a database:
			//		data\\dsstox\\json\\2023_04_snapshot_dsstox_records.json
			//		data\\dsstox\\json\\2023_04_snapshot_other_casrn lookup.json
			// 		These files are used to fix the neighbors which are missing dtxsids- but might need to pull info from prod_dsstox instead???

			System.out.println("\nGoing through resultSet");

			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\toxval\\";

			String propertyName=DevQsarConstants.BCF;

			try {

				FileWriter fw=new FileWriter(folder+propertyName+" OPERA 2.8.tsv");

				fw.write(new RecordToxValModel().toHeaderTSV()+"\r\n");

				int limit=-1;
				int offset=0;			
				String sql=Lookup.createSQLAll(offset,limit);

				Statement sqliteStatement=SqliteUtilities.getStatement(Lookup.conn);


				List<OPERA_Structure>operaStructures=OPERA_Structure.readStructureTableFromSqlite(sqliteStatement);
				Hashtable<String,OPERA_Structure>htDTXCIDToOperaStructure=new Hashtable<>();
				for (OPERA_Structure s:operaStructures) htDTXCIDToOperaStructure.put(s.DSSTOX_COMPOUND_ID, s);
				//			System.out.println(Utilities.gson.toJson(htDTXCIDToOperaStructure));


				ResultSet rs=SqliteUtilities.getRecords(sqliteStatement, sql);

				List<String>colNamesAll=new ArrayList<String>();
				for(int i=1;i<=rs.getMetaData().getColumnCount();i++) {
					colNamesAll.add(rs.getMetaData().getColumnName(i));
				}

				//			List<String>colNamesAll=Arrays.asList(colNames);
				TreeMap<String, List<String>>htColNames=converter.columnHandler.assignColumnsByProperty(colNamesAll);
				TreeMap<String, List<String>>htColNamesProperty=new TreeMap<>();
				//Just use column names for desired property:
				htColNamesProperty.put(propertyName, htColNames.get(propertyName));

				//
				int linesRead=0;

				while (rs.next()) {


					List<String>values=new ArrayList<String>();

					int columnCount = rs.getMetaData().getColumnCount();

					boolean HaveDTXCID=false;

					// The column count starts from 1
					for (int i = 1; i <= columnCount; i++ ) {
						if(rs.getString(i).contains("DTXCID")) HaveDTXCID=true;
						values.add(rs.getString(i));
					}

					//				for(int i=0;i<colNamesAll.size();i++) {
					//					System.out.println(colNamesAll.get(i)+"\t"+values.get(i));
					//				}

					if (values==null) break;
					linesRead++;

					if(linesRead%1000==0) {
						System.out.println(linesRead);
					}

					if(!HaveDTXCID) continue;

					//				if(HaveDTXCID) continue;
					//				else {
					//					for(int i=0;i<colNamesAll.size();i++) {
					//						System.out.println(colNamesAll.get(i)+"\t"+values.get(i));
					//					}
					//				}


					List<PredictionDashboard>predictionsDashboard=converter.convertValuesToRecords(false, colNamesAll, values, htColNamesProperty, lookups,
							htDTXCIDToOperaStructure, pd_keys);

					for(PredictionDashboard pd:predictionsDashboard) {

						Dataset dataset=lookups.mapDatasets.get(pd.getModel().getDatasetName());

						if(!dataset.getName().equals(initializeDB.getDatasetName(propertyName))) continue;

						if(pd.getDsstoxRecord().getDtxsid()==null) {
							System.out.println(pd.getDtxcid()+"\tNot in snapshot");
							continue;
						}

						if(propertyName.equals(DevQsarConstants.BCF)) {
							RecordToxValModel r=new RecordToxValModel();
							r.model_id=-1;
							r.chemical_id=-1;
							r.dtxsid=pd.getDsstoxRecord().getDtxsid();
							r.model="OPERA";
							r.metric="BCF";
							r.value=pd.getPredictionValue();

							if(r.value==null) {
								System.out.println(r.dtxsid+"\tnull BCF");
								continue;
							}

							r.units="L/kg wet-wt";
							r.qualifier="=";
							fw.write(r.toTSV()+"\r\n");

							//Handle AD, useGlobalAD since that's what we used in hazard module for BCF
							r=new RecordToxValModel();
							r.model_id=-1;
							r.chemical_id=-1;
							r.dtxsid=pd.getDsstoxRecord().getDtxsid();
							r.model="OPERA";
							r.metric="BCF_AD";

							for(QsarPredictedADEstimate adEstimate:pd.getQsarPredictedADEstimates()) {
								if(!adEstimate.getMethodAD().getName().equals(DevQsarConstants.Applicability_Domain_OPERA_global_index)) continue;
								//							System.out.println(adEstimate.getMethodAD().getName());
								r.value=adEstimate.getApplicabilityValue();
							}
							r.units="unitless";
							r.qualifier="=";
							fw.write(r.toTSV()+"\r\n");


						} else {
							System.out.println("need to set metric for "+propertyName);
							continue;
						}
					}

					if(linesRead==stopCount) break;
				}

				fw.flush();
				fw.close();

			}catch (Exception  ex) {
				ex.printStackTrace();
			}

		}



	}











	//	private Double getCategoryEPA(String strvalue) {
	//
	//		if(strvalue.equals("501-5000")) return 3.0;
	//				
	//		double value=Double.parseDouble(strvalue);
	//		if (value<=50) return 1.0;
	//		else if (value<=500) return 2.0;
	//		else if(value<=5000) return 3.0;
	//		else if(value>5000) return 4.0;
	//		else return null;
	//	}
	//	
	//	private Double getCategoryGHS(String strvalue) {
	//		double value=Double.parseDouble(strvalue);
	//		if (value<=5) return 1.0;
	//		else if (value<=50) return 2.0;
	//		else if(value<=300) return 3.0;
	//		else if(value<=2000) return 4.0;
	//		else if(value>2000) return 5.0;
	//		else return null;
	//	}


	class Loader {

		/**
		 * TODO This method needs to be updated to match the one for loading from sqlite
		 * 
		 * @param writeToDB
		 */
		public void createRecordsFromCSV(boolean writeToDB) {

			ConvertToPredictionDashboard converter=new ConvertToPredictionDashboard();


			PredictionDashboardServiceImpl predictionDashboardService=new PredictionDashboardServiceImpl();

			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.9\\";
			//		String filepathPredictionCsv=folder+"OPERA2.8_DSSTox_082021_1_sample.csv";
			//		String filepathStructureCSV=folder+"OPERA2.8_DSSTox_082021_1_sample_Structures.csv";
			String filepathPredictionCsv=folder+"OPERA2.8_DSSTox_082021_1_first1000.csv";
			String filepathStructureCSV=folder+"OPERA2.8_DSSTox_082021_1_first1000_structures.csv";

			int batchSize=1000;
			int count=40;//number of rows in the csv to use
			//		int count=-1;


			long minModelId=1019;//OPERA2.9
			long maxModelId=1051;//OPERA2.9

			HashSet<String> pd_keys = DatabaseUtilities.getPredictionsDashboardKeysInDB(minModelId,maxModelId);

			//TODO update as follows:
			//		HashSet<String> pd_keys=DatabaseUtilities.getLoadedKeys(source, snapshot);

			System.out.println("Number of OPERA predictions already in the database:"+pd_keys.size());

			PredictionDashboardTableMaps lookups=new PredictionDashboardTableMaps(PredictionDashboardTableMaps.fileJsonDsstoxRecords2023_04_04,PredictionDashboardTableMaps.fileJsonOtherCAS2023_04_04);//creates lookup maps for database objects so dont have to keep query the database

			//Note opera_lookups depends on two json files which make it run faster than pulling the info from a database:
			//		data\\dsstox\\json\\2023_04_snapshot_dsstox_records.json
			//		data\\dsstox\\json\\2023_04_snapshot_other_casrn lookup.json
			// 		These files are used to fix the neighbors which are missing dtxsids- but might need to pull info from prod_dsstox instead???

			System.out.println("\nGoing through csv");

			List<OPERA_Structure>operaStructures=OPERA_Structure.readStructureCSV(filepathStructureCSV, count);
			Hashtable<String,OPERA_Structure>htDTXCIDToOperaStructure=new Hashtable<>();
			for (OPERA_Structure s:operaStructures) htDTXCIDToOperaStructure.put(s.DSSTOX_COMPOUND_ID, s);

			List<PredictionDashboard>predictionsDashboard=new ArrayList<>();

			try {
				CSVReader reader = new CSVReader(new FileReader(filepathPredictionCsv));
				String []colNames=reader.readNext();
				List<String>colNamesAll=Arrays.asList(colNames);
				TreeMap<String, List<String>>htColNames=converter.columnHandler.assignColumnsByProperty(colNamesAll);

				int linesRead=0;

				while (true) {
					String []values=reader.readNext();
					if (values==null) break;
					linesRead++;

					List<String>values2=Arrays.asList(values);


					if(linesRead%100==0) {
						System.out.println(linesRead);
					}


					List<PredictionDashboard>predictionsDashboard2=converter.convertValuesToRecords(writeToDB,colNamesAll, values2, htColNames, lookups,
							htDTXCIDToOperaStructure, pd_keys);

					predictionsDashboard.addAll(predictionsDashboard2);

					if(writeToDB && predictionsDashboard.size()==batchSize) {
						//					System.out.println(counter);
						predictionDashboardService.createSQL(predictionsDashboard);
						predictionsDashboard.clear();
					}

					if(linesRead==count) break;
				}

				if(writeToDB) predictionDashboardService.createSQL(predictionsDashboard);//do last ones

			}catch (Exception  ex) {
				ex.printStackTrace();
			}

		}

		public void createRecordsFromOPERA2_8_SqliteDB(boolean writeToDB) {


			boolean writeReportsToHardDrive=false;
			boolean useLegacyModelIds=false;

			version="2.8";
			int batchSize=1000;
//			int count=1000;//number of rows in sqlite db to use
			int count=-1;

			int limit=-1;
			if(count!=-1) limit=count;
			
			int offset=200000;//already loaded these			

			boolean skipMissingDsstoxRecordID=true;

			String snapshotName="DSSTOX Snapshot 11/12/2024";		
			DsstoxSnapshotServiceImpl snapshotService = new DsstoxSnapshotServiceImpl();
			DsstoxSnapshot snapshot = snapshotService.findByName(snapshotName);

			SourceService sourceService=new SourceServiceImpl();
			String sourceName="OPERA" + version;
			Source source=sourceService.findByName(sourceName);


			//		HashSet<String> pd_keys = getPredictionsDashboardKeysInDB(minModelId,maxModelId);
			//		HashSet<String> pd_keys=new HashSet<String>();
			HashSet<String> pd_keys=DatabaseUtilities.getLoadedKeys(source, snapshot);
			System.out.println("Loaded keys: "+pd_keys.size());


			//Note following Maps object depends on two json files which make it run faster than pulling the info from a database:
			PredictionDashboardTableMaps tableMaps=new PredictionDashboardTableMaps(PredictionDashboardTableMaps.fileJsonDsstoxRecords2024_11_12,PredictionDashboardTableMaps.fileJsonOtherCAS2024_11_12);//creates lookup maps for database objects so dont have to keep query the database

			System.out.println("\nGoing through resultSet");

			List<PredictionDashboard>predictionsDashboard=new ArrayList<>();

			try {
				String sql=Lookup.createSQLAllSort(offset,limit,"DSSTOX_COMPOUND_ID");
				//			String sql="select * from Results where DSSTOX_COMPOUND_ID='DTXCID20135';";

				Statement sqliteStatement=SqliteUtilities.getStatement(Lookup.conn);

				List<OPERA_Structure>operaStructures=OPERA_Structure.readStructureTableFromSqlite(sqliteStatement);

				//Following is needed to lookup OPERA QSAR ready smiles:
				Hashtable<String,OPERA_Structure>htDTXCIDToOperaStructure=new Hashtable<>();
				for (OPERA_Structure s:operaStructures) htDTXCIDToOperaStructure.put(s.DSSTOX_COMPOUND_ID, s);
				//			System.out.println(Utilities.gson.toJson(htDTXCIDToOperaStructure));


				ResultSet rsResultsTable=SqliteUtilities.getRecords(sqliteStatement, sql);

				goThroughResultsRecords(writeToDB,writeReportsToHardDrive, batchSize, count,
						skipMissingDsstoxRecordID, pd_keys, tableMaps, predictionsDashboard, htDTXCIDToOperaStructure,
						rsResultsTable,false);


			}catch (Exception  ex) {
				ex.printStackTrace();
			}

		}
		
		

		void saveToPredictionDashboardJsons(String folderpath) {
			
			List<String> modelsKeep = Arrays.asList("OPERA_BCF", "OPERA_CERAPP-Agonist", "OPERA_CERAPP-Antagonist",
					"OPERA_CERAPP-Binding", "OPERA_CoMPARA-Agonist", "OPERA_CoMPARA-Antagonist",
					"OPERA_CoMPARA-Binding");
			
			version="2.8";
			createReports=false;
			storeNeighbors=false;
			
//			int count=100000;//number of rows in sqlite db to use
			int count=-1;

			int limit=-1;
			if(count!=-1) limit=count;
			int offset=0;			

			boolean skipMissingDsstoxRecordID=true;

			String snapshotName="DSSTOX Snapshot 11/12/2024";		
			DsstoxSnapshotServiceImpl snapshotService = new DsstoxSnapshotServiceImpl();
			DsstoxSnapshot snapshot = snapshotService.findByName(snapshotName);

			SourceService sourceService=new SourceServiceImpl();
			String sourceName="OPERA" + version;
			Source source=sourceService.findByName(sourceName);

			
			//		HashSet<String> pd_keys = getPredictionsDashboardKeysInDB(minModelId,maxModelId);
			HashSet<String> pd_keys=new HashSet<String>();//doesnt matter if in postgres database, taking from OPERA sqlite
//			HashSet<String> pd_keys=DatabaseUtilities.getLoadedKeys(source, snapshot);
//			System.out.println("Loaded keys: "+pd_keys.size());


			//Note following Maps object depends on two json files which make it run faster than pulling the info from a database:
			PredictionDashboardTableMaps tableMaps=new PredictionDashboardTableMaps(PredictionDashboardTableMaps.fileJsonDsstoxRecords2024_11_12,PredictionDashboardTableMaps.fileJsonOtherCAS2024_11_12);//creates lookup maps for database objects so dont have to keep query the database

			System.out.println("\nGoing through resultSet");

			List<PredictionDashboard>predictionsDashboard=new ArrayList<>();

			try {
				String sql=Lookup.createSQLAll(offset,limit);
				//			String sql="select * from Results where DSSTOX_COMPOUND_ID='DTXCID20135';";

				Statement sqliteStatement=SqliteUtilities.getStatement(Lookup.conn);

				List<OPERA_Structure>operaStructures=OPERA_Structure.readStructureTableFromSqlite(sqliteStatement);

				//Following is needed to lookup OPERA QSAR ready smiles:
				Hashtable<String,OPERA_Structure>htDTXCIDToOperaStructure=new Hashtable<>();
				for (OPERA_Structure s:operaStructures) htDTXCIDToOperaStructure.put(s.DSSTOX_COMPOUND_ID, s);
				//			System.out.println(Utilities.gson.toJson(htDTXCIDToOperaStructure));

				ResultSet rsResultsTable=SqliteUtilities.getRecords(sqliteStatement, sql);

				goThroughResultsRecords(modelsKeep, folderpath, count,
						skipMissingDsstoxRecordID, pd_keys, tableMaps, predictionsDashboard, htDTXCIDToOperaStructure,
						rsResultsTable,false);


			}catch (Exception  ex) {
				ex.printStackTrace();
			}
			
		}
		

		/**
		 * 
		 * @param writeToDB whether or not to write to res_qsar
		 * @param dsstoxID which DTXSID or DTXCID to pull from sqlite DB
		 * @param printValues print OPERA Results record as key-value pairs
		 */
		public void createRecordsFromOPERA2_8_SqliteDB(boolean writeToDB,String dsstoxID,boolean printValues) {

			boolean writeReportsToHardDrive=true;
			int batchSize=1000;
			int count=-1;
			version="2.8";

			boolean skipMissingDsstoxRecordID=true;

			String snapshotName="DSSTOX Snapshot 11/12/2024";		
			DsstoxSnapshotServiceImpl snapshotService = new DsstoxSnapshotServiceImpl();
			DsstoxSnapshot snapshot = snapshotService.findByName(snapshotName);

			if(dsstoxID.contains("DTXSID")) {//need dtxcid since that's how stored in results table of sqlite
				System.out.println("\n"+dsstoxID);
				String sql="select dtxcid from qsar_models.dsstox_records where dtxsid='"+dsstoxID+"' and fk_dsstox_snapshot_id="+snapshot.getId()+";";
				//			System.out.println(sql);
				dsstoxID=SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql);
				System.out.println(dsstoxID);
			}

			//		if(true)return;

			SourceService sourceService=new SourceServiceImpl();
			String sourceName="OPERA" + version;
			Source source=sourceService.findByName(sourceName);

			HashSet<String> pd_keys=DatabaseUtilities.getLoadedKeys(source, snapshot);
			System.out.println("Loaded keys: "+pd_keys.size());

			if(tableMaps==null) {
				tableMaps=new PredictionDashboardTableMaps(PredictionDashboardTableMaps.fileJsonDsstoxRecords2024_11_12,PredictionDashboardTableMaps.fileJsonOtherCAS2024_11_12);//creates lookup maps for database objects so dont have to keep query the database			
			}

			System.out.println(tableMaps.mapDsstoxRecordsBySID.size());
			//		System.out.println(tableMaps.mapDsstoxRecordsBySID.get("DTXSID0050479"));


			System.out.println("\nGoing through resultSet");
			List<PredictionDashboard>predictionsDashboard=new ArrayList<>();

			try {
				String sql="SELECT * from Results where DSSTOX_COMPOUND_ID='"+dsstoxID+"';";

				//			System.out.println(sql);

				Statement sqliteStatement=SqliteUtilities.getStatement(Lookup.conn);
				List<OPERA_Structure>operaStructures=OPERA_Structure.readStructureTableFromSqlite(sqliteStatement);

				//Following is needed to lookup OPERA QSAR ready smiles:
				Hashtable<String,OPERA_Structure>htDTXCIDToOperaStructure=new Hashtable<>();
				for (OPERA_Structure s:operaStructures) htDTXCIDToOperaStructure.put(s.DSSTOX_COMPOUND_ID, s);
				//			System.out.println(Utilities.gson.toJson(htDTXCIDToOperaStructure));


				ResultSet rsResultsTable=SqliteUtilities.getRecords(sqliteStatement, sql);

				goThroughResultsRecords(writeToDB,writeReportsToHardDrive, batchSize, count,
						skipMissingDsstoxRecordID, pd_keys, tableMaps, predictionsDashboard, htDTXCIDToOperaStructure,
						rsResultsTable,printValues);


			}catch (Exception  ex) {
				ex.printStackTrace();
			}

		}

		private void goThroughResultsRecords(List<String>modelsKeep, String folder, int count,
				boolean skipMissingDsstoxRecordID, HashSet<String> pd_keys, PredictionDashboardTableMaps tableMaps,
				List<PredictionDashboard> predictionsDashboard, Hashtable<String, OPERA_Structure> htDTXCIDToOperaStructure,
				ResultSet rsResultsTable,boolean printValues) throws SQLException {
		
			PredictionDashboardServiceImpl predictionDashboardService=new PredictionDashboardServiceImpl();
		
			long t1=System.currentTimeMillis();
		
			List<String>colNamesAll=new ArrayList<String>();
			for(int i=1;i<=rsResultsTable.getMetaData().getColumnCount();i++) {
				colNamesAll.add(rsResultsTable.getMetaData().getColumnName(i));
			}
		
			//			List<String>colNamesAll=Arrays.asList(colNames);
			TreeMap<String, List<String>>htColNames=converter.columnHandler.assignColumnsByProperty(colNamesAll);
		
//			System.out.println(Utilities.gson.toJson(htColNames));
		
			//
			int lines=0;
			int records=0;
			
			int fileNum=1;
			
			try {

				String filename="OPERA28_BCF_ER_AR"+fileNum+".json";
				FileWriter fw = new FileWriter(folder+"OPERA28_BCF_ER_AR"+fileNum+".json");
				System.out.println(filename);

				while (rsResultsTable.next()) {
					
					records++;

					List<String>values=new ArrayList<String>();

					int columnCount = rsResultsTable.getMetaData().getColumnCount();

					// The column count starts from 1
					for (int i = 1; i <= columnCount; i++ ) {
						values.add(rsResultsTable.getString(i));
					}

					if(printValues) {
						for(int i=0;i<colNamesAll.size();i++) {
							System.out.println(colNamesAll.get(i)+"\t"+values.get(i));
						}
					}

					//			if (values==null) break;

					List<PredictionDashboard>predictionsDashboard2=converter.convertValuesToRecords(false,colNamesAll, values, htColNames, tableMaps,
							htDTXCIDToOperaStructure, pd_keys);


					if(skipMissingDsstoxRecordID && predictionsDashboard2.size()>0) {
						PredictionDashboard pd0=predictionsDashboard2.get(0);
						if(pd0.getDsstoxRecord()==null) {
							//						System.out.println("Missing in dsstoxRecords:\t"+pd0.getDtxcid());
							continue;
						}
					}

					for(PredictionDashboard pd:predictionsDashboard2) {
						
						if(!modelsKeep.contains(pd.getModel().getName_ccd())) continue;

						lines++;
						
						fw.write(pd.toJsonOneLine()+"\r\n");
						fw.flush();
						
						if(lines==100000) {
							lines=0;
							fw.close();
							fileNum++;
							filename="OPERA28_BCF_ER_AR"+fileNum+".json";
							System.out.println(filename);
							fw = new FileWriter(folder+filename);
						}
						
					}

					if(records%10000==0) System.out.println(records);
					
					if(records==count) break;
				
				}//loop over db records
				
				
				fw.close();

				long t2=System.currentTimeMillis();
				System.out.println("time to load:"+(t2-t1)/1000.0+" seconds");
				

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		
			
		}

		private void goThroughResultsRecords(boolean writeToDB, boolean writeReportsToHarddrive,int batchSize, int count,
				boolean skipMissingDsstoxRecordID, HashSet<String> pd_keys, PredictionDashboardTableMaps tableMaps,
				List<PredictionDashboard> predictionsDashboard, Hashtable<String, OPERA_Structure> htDTXCIDToOperaStructure,
				ResultSet rsResultsTable,boolean printValues) throws SQLException {
		
		
			PredictionDashboardServiceImpl predictionDashboardService=new PredictionDashboardServiceImpl();
		
		
			long t1=System.currentTimeMillis();
		
			List<String>colNamesAll=new ArrayList<String>();
			for(int i=1;i<=rsResultsTable.getMetaData().getColumnCount();i++) {
				colNamesAll.add(rsResultsTable.getMetaData().getColumnName(i));
			}
		
			//			List<String>colNamesAll=Arrays.asList(colNames);
			TreeMap<String, List<String>>htColNames=converter.columnHandler.assignColumnsByProperty(colNamesAll);
		
			//		System.out.println(Utilities.gson.toJson(htColNames));
		
			//
			int recordsRead=0;
		
			while (rsResultsTable.next()) {
		
				List<String>values=new ArrayList<String>();
		
				int columnCount = rsResultsTable.getMetaData().getColumnCount();
		
				// The column count starts from 1
				for (int i = 1; i <= columnCount; i++ ) {
					values.add(rsResultsTable.getString(i));
				}
		
				if(printValues) {
					for(int i=0;i<colNamesAll.size();i++) {
						System.out.println(colNamesAll.get(i)+"\t"+values.get(i));
					}
				}
		
				//			if (values==null) break;
		
				recordsRead++;
		
				if(recordsRead%1000==0) {
					System.out.println(recordsRead);
				}
		
				List<PredictionDashboard>predictionsDashboard2=converter.convertValuesToRecords(writeReportsToHarddrive,colNamesAll, values, htColNames, tableMaps,
						htDTXCIDToOperaStructure, pd_keys);
		
		
		
				if(skipMissingDsstoxRecordID && predictionsDashboard2.size()>0) {
					PredictionDashboard pd0=predictionsDashboard2.get(0);
					if(pd0.getDsstoxRecord()==null) {
						//						System.out.println("Missing in dsstoxRecords:\t"+pd0.getDtxcid());
						continue;
					}
				}
		
				predictionsDashboard.addAll(predictionsDashboard2);
		
				if(predictionsDashboard.size()>=batchSize) {					
					//					System.out.println(predictionsDashboard.size());					
					if(writeToDB) predictionDashboardService.createSQL(predictionsDashboard);
					predictionsDashboard.clear();
				}
		
				if(recordsRead==count) break;
			}
		
			if(writeToDB) {
				//				System.out.println("Here1");
				predictionDashboardService.createSQL(predictionsDashboard);//do last ones
			}
		
			long t2=System.currentTimeMillis();
		
			System.out.println("time to load:"+(t2-t1)/1000.0+" seconds");
		}



	}

	public static void main(String[] args) {
		PredictionDashboardScriptOPERA o= new PredictionDashboardScriptOPERA();

		version="2.8";
		useLegacyModelIds=false;

		//****************************************************************************************
//		DatabaseUtilities d=new DatabaseUtilities();
//		d.deleteAllRecords("OPERA2.8");
		//		o.initializeDB.initializeOPERARecords();//create db entries in properties, datasets, models, statistics tables
		
		// Run all in db:		
		o.loader.createRecordsFromOPERA2_8_SqliteDB(true);
//		o.loader.createRecordsFromOPERA2_8_SqliteDB(true,"DTXSID301346793");

		//		boolean printValues=true;//print OPERA Results record as key-value pairs
		boolean printValues=false;//print OPERA Results record as key-value pairs
		boolean writeToDB=true;
//		o.loader.createRecordsFromOPERA2_8_SqliteDB(writeToDB,"DTXSID7020182",printValues);//bisphenol-A
//		o.loader.createRecordsFromOPERA2_8_SqliteDB(writeToDB,"DTXSID2021315",printValues);//bisphenol-A
		
		
		//		o.loader.createRecordsFromOPERA2_8_SqliteDB(writeToDB,"DTXSID3039242",printValues);//bz
		//		o.loader.createRecordsFromOPERA2_8_SqliteDB(writeToDB,"DTXSID20879997",printValues);//long SMILES
		//		o.loader.createRecordsFromOPERA2_8_SqliteDB(writeToDB,"DTXSID1022681",printValues);//first in catmos sdf
		//		o.loader.createRecordsFromOPERA2_8_SqliteDB(writeToDB,"DTXSID9020584",printValues);//ethanol

		//		o.loader.createRecordsFromOPERA2_8_SqliteDB(writeToDB,"DTXSID0020232",printValues);//Clint=-7
		//		o.loader.createRecordsFromOPERA2_8_SqliteDB(writeToDB,"DTXCID309844",printValues);//has exp pKA_a
		
		//****************************************************************************************
		//Make files for exporting to hazardRecords database:
//		o.loader.saveToPredictionDashboardJsons("data\\OPERA2.8\\export\\");

//****************************************************************************************
//		o.toxval.createToxValModelCSVFromOPERA2_8_SqliteDB();

		//****************************************************************************************
		//		o.extraMethods.createPlotJsons();
		//		o.extraMethods.createPlotJsonCatmos();
		//		o.extraMethods.uploadPlots("OPERA2.8");		
		//		if(true)return;
		//****************************************************************************************

		//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\OPERA\\OPERA_SDFS\\";
		//		o.extraMethods.getLD50ResultsSDF(folder,"CATMoS_QR50k.sdf");

		//		o.extraMethods.getChemicalsMissingInSqliteDB();
		//		o.createRecordsFromCSV(false);

		//View reports by generating them on the fly from database:
		//		OPERA_Report_API ora=new OPERA_Report_API();
		//		ora.viewReportsFromDatabase("DTXSID50943897");
		//		ora.viewReportsFromDatabase("DTXSID3039242");

		//Bookkeeping methods:		
		//		o.extraMethods.findMissingDsstoxRecords();
		//		o.extraMethods.printColumnValues("CERAPP_Bind_exp");
		//		o.extraMethods.compareResultsToToxVal();

	}




	class InitializeDB {

		/**
		 * Creates properties, datasets, and models for OPERA in the postgres db
		 * 
		 */
		void initializeOPERARecords() {

			List<String>propertyNames=DevQsarConstants.getOPERA_PropertyNames();
			TreeMap <String,Property>mapProperties=createProperties(propertyNames);
			TreeMap<String,Dataset>mapDatasets=createDatasets(mapProperties,propertyNames);
			TreeMap<String, Model>mapModels=createModels(propertyNames);
			createStatistics(mapModels);
		}

		/**
		 * Creates applicability domains for OPERA in db
		 */
		private void createADs() {

			MethodADServiceImpl servMAD=new MethodADServiceImpl();

			String strGlobalAD="OPERA global applicability domain";
			String strLocalAD="OPERA local applicability domain";
			String strConfidenceIndex="OPERA confidence index";

			MethodAD methodAD=null;

			methodAD = new MethodAD(strGlobalAD,strGlobalAD, "Global",	userName);
			servMAD.create(methodAD);

			methodAD = new MethodAD(strLocalAD,strLocalAD, "Local",	userName);
			servMAD.create(methodAD);

			methodAD = new MethodAD(strConfidenceIndex,strConfidenceIndex, "Local",	userName);
			servMAD.create(methodAD);

		}

		/**
		 * Creates properties for OPERA if they arent in the db
		 * 
		 * @param htOperaToDatabaseProps
		 * @return
		 */
		private TreeMap <String,Property> createProperties(List<String>propertyNames) {

			PropertyServiceImpl ps=new PropertyServiceImpl();

			TreeMap <String,Property>mapProperties=CreatorScript.getPropertyMap();


			for (String propertyName:propertyNames) {


				boolean inDB=mapProperties.get(propertyName)!=null;

				if(!inDB) {
					//				System.out.println(operaAbbrev+"\t"+propertyName+"\t"+inDB);

					Property property=new Property();
					property.setName(propertyName);
					property.setCreatedBy(userName);
					property.setDescription(DevQsarConstants.getPropertyDescription(propertyName));

					System.out.println(Utilities.gson.toJson(property));
					ps.create(property);
					mapProperties.put(propertyName,property);
				} 

			}

			System.out.println("propertyMapDB.size()="+mapProperties.size());
			return mapProperties;

		}

		/**
		 * Creates OPERA models in the database
		 * 
		 * @param htOperaToDatabaseProps
		 * @return
		 */
		private TreeMap<String, Model> createModels(List<String>propertyNames) {

			TreeMap<String,Model> mapModels=CreatorScript.getModelsMap();

			Method method=MethodCreator.createMethod(userName, true);

			SourceService ss=new SourceServiceImpl();
			Source source=ss.findByName(getSource());

			for (String propertyName:propertyNames) {

				String descriptorSetName="PaDEL";
				String splittingName="OPERA";
				String modelName=getModelName(propertyName);
				String datasetName=getDatasetName(propertyName);
				DescriptorEmbedding embedding=null;//TODO could add this from the QMRF


				if(mapModels.get(modelName)!=null) {
					Model modelInDB=mapModels.get(modelName);
					if(modelInDB.getSource().getName().equals(source.getName())) {
						//					System.out.println(modelInDB.getName()+" is in db");
						continue;
					}
				}

				Model model=new Model(modelName, method, embedding,descriptorSetName, datasetName, splittingName, source,userName);
				//			System.out.println(Utilities.gson.toJson(model));

				model=CreatorScript.createModel(model);

				//			System.out.println(modelName+"\t"+model.getName());
				mapModels.put(modelName, model);
			}
			System.out.println("mapModels.size()="+mapModels.size());
			return mapModels;
		}


		/**
		 * Creates OPERA datasets in the database
		 * 
		 * @param propertyMap
		 * @param htOperaToDatabaseProps
		 * @return
		 */
		private TreeMap<String,Dataset> createDatasets(TreeMap <String,Property>propertyMap,List<String>propertyNames) {

			TreeMap<String,Dataset>mapDatasets=CreatorScript.getDatasetsMap();

			HashMap<String, String>hmUnitsDataset=DevQsarConstants.getDatasetFinalUnitsNameMapOPERA();
			HashMap<String, String>hmUnitsDatasetContributor=DevQsarConstants.getContributorUnitsNameMap();


			DatasetServiceImpl datasetService=new DatasetServiceImpl();

			for (String propertyName:propertyNames) {
				String datasetName = getDatasetName(propertyName);

				boolean inDB=mapDatasets.get(datasetName)!=null;

				if(inDB) {
					//				System.out.println(datasetName +" is in DB");
					continue;
				}

				String unitName=hmUnitsDataset.get(propertyName);
				String unitContributorName=hmUnitsDatasetContributor.get(propertyName);

				if (unitName==null || unitContributorName==null) {
					System.out.println(propertyName+"\t"+datasetName+"\t"+unitName+"\t"+unitContributorName);
				}

				Unit unit=CreatorScript.createUnit(unitName,userName);
				Unit unitContributor=CreatorScript.createUnit(unitContributorName,userName);

				String dsstoxMappingStrategy="OPERA";

				Property property=propertyMap.get(propertyName);

				Dataset dataset=new Dataset(datasetName, datasetName, property, unit, unitContributor, dsstoxMappingStrategy, userName);
				dataset=datasetService.create(dataset);

				mapDatasets.put(datasetName, dataset);

				//			System.out.println(Utilities.gson.toJson(dataset));

			}


			//		for (String propertyNameDB:propertyNames) {
			////			String datasetDescription=getDatasetDescription(propertyNameDB);
			//		}	

			System.out.println("mapDatasets.size()="+mapDatasets.size());

			return mapDatasets;
		}

		/**
		 * Store the statistics for the OPERA models
		 * Values taken from the QMRFs
		 * 
		 * @param mapModels
		 */
		private void createStatistics(TreeMap <String,Model>mapModels) {

			StatisticService ps=new StatisticServiceImpl();
			ModelStatisticService ms=new ModelStatisticServiceImpl();

			List<Statistic>statistics=ps.getAll();

			System.out.println(statistics.size());

			TreeMap <String,Statistic>mapStatistics=new TreeMap<>();
			for (Statistic statistic:statistics) {
				mapStatistics.put(statistic.getName(), statistic);
			}

			Statistic statisticR2_Training=mapStatistics.get(DevQsarConstants.PEARSON_RSQ_TRAINING);//Train//R2
			Statistic statisticRMSE_Training=mapStatistics.get(DevQsarConstants.RMSE_TRAINING);//Train//RMSE

			Statistic statisticR2_CV_Training=mapStatistics.get(DevQsarConstants.PEARSON_RSQ_CV_TRAINING);//5FoldCV//R2
			Statistic statisticRMSE_CV_Training=mapStatistics.get(DevQsarConstants.RMSE_CV_TRAINING);//5FoldCV//RMSE

			Statistic statisticR2_Test=mapStatistics.get(DevQsarConstants.PEARSON_RSQ_TEST);//TEST//R2
			Statistic statisticRMSE_Test=mapStatistics.get(DevQsarConstants.RMSE_TEST);//TEST//RMSE

			Statistic statisticBA_Training=mapStatistics.get(DevQsarConstants.BA_TRAINING);//Train//R2
			Statistic statisticSN_Training=mapStatistics.get(DevQsarConstants.SN_TRAINING);//Train//RMSE
			Statistic statisticSP_Training=mapStatistics.get(DevQsarConstants.SP_TRAINING);//Train//RMSE

			Statistic statisticBA_CV_Training=mapStatistics.get(DevQsarConstants.BA_CV_TRAINING);//Train//R2
			Statistic statisticSN_CV_Training=mapStatistics.get(DevQsarConstants.SN_CV_TRAINING);//Train//RMSE
			Statistic statisticSP_CV_Training=mapStatistics.get(DevQsarConstants.SP_CV_TRAINING);//Train//RMSE

			Statistic statisticBA_Test=mapStatistics.get(DevQsarConstants.BA_TEST);//Train//R2
			Statistic statisticSN_Test=mapStatistics.get(DevQsarConstants.SN_TEST);//Train//RMSE
			Statistic statisticSP_Test=mapStatistics.get(DevQsarConstants.SP_TEST);//Train//RMSE

			Model modelHL=getModel(DevQsarConstants.HENRYS_LAW_CONSTANT, mapModels);
			ms.create(new ModelStatistic(statisticR2_Training,modelHL,0.84,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Training,modelHL,1.91,userName));
			ms.create(new ModelStatistic(statisticR2_CV_Training,modelHL,0.84,userName));		
			ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelHL,1.96,userName));
			ms.create(new ModelStatistic(statisticR2_Test,modelHL,0.85,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Test,modelHL,1.82,userName));

			Model modelLogP=getModel(DevQsarConstants.LOG_KOW, mapModels);
			ms.create(new ModelStatistic(statisticR2_Training,modelLogP,0.86,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Training,modelLogP,0.67,userName));
			ms.create(new ModelStatistic(statisticR2_CV_Training,modelLogP,0.85,userName));		
			ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelLogP,0.69,userName));
			ms.create(new ModelStatistic(statisticR2_Test,modelLogP,0.86,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Test,modelLogP,0.79,userName));

			Model modelKOA=getModel(DevQsarConstants.LOG_KOA, mapModels);
			ms.create(new ModelStatistic(statisticR2_Training,modelKOA,0.95,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Training,modelKOA,0.65,userName));
			ms.create(new ModelStatistic(statisticR2_CV_Training,modelKOA,0.95,userName));		
			ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelKOA,0.69,userName));
			ms.create(new ModelStatistic(statisticR2_Test,modelKOA,0.96,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Test,modelKOA,0.68,userName));


			Model modelMP=getModel(DevQsarConstants.MELTING_POINT, mapModels);
			ms.create(new ModelStatistic(statisticR2_Training,modelMP,0.74,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Training,modelMP,50.27,userName));
			ms.create(new ModelStatistic(statisticR2_CV_Training,modelMP,0.71,userName));		
			ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelMP,51.8,userName));
			ms.create(new ModelStatistic(statisticR2_Test,modelMP,0.73,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Test,modelMP,52.72,userName));

			Model modelBP=getModel(DevQsarConstants.BOILING_POINT, mapModels);
			ms.create(new ModelStatistic(statisticR2_Training,modelBP,0.93,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Training,modelBP,22.1,userName));
			ms.create(new ModelStatistic(statisticR2_CV_Training,modelBP,0.93,userName));		
			ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelBP,22.5,userName));
			ms.create(new ModelStatistic(statisticR2_Test,modelBP,0.93,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Test,modelBP,22.1,userName));

			Model modelVP=getModel(DevQsarConstants.VAPOR_PRESSURE, mapModels);
			ms.create(new ModelStatistic(statisticR2_Training,modelVP,0.91,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Training,modelVP,1.08,userName));
			ms.create(new ModelStatistic(statisticR2_CV_Training,modelVP,0.91,userName));		
			ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelVP,1.08,userName));
			ms.create(new ModelStatistic(statisticR2_Test,modelVP,0.92,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Test,modelVP,1.00,userName));

			Model modelWS=getModel(DevQsarConstants.WATER_SOLUBILITY, mapModels);
			ms.create(new ModelStatistic(statisticR2_Training,modelWS,0.87,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Training,modelWS,0.82,userName));
			ms.create(new ModelStatistic(statisticR2_CV_Training,modelWS,0.87,userName));		
			ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelWS,0.81,userName));
			ms.create(new ModelStatistic(statisticR2_Test,modelWS,0.86,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Test,modelWS,0.86,userName));


			Model modelAOH=getModel(DevQsarConstants.OH, mapModels);
			ms.create(new ModelStatistic(statisticR2_Training,modelAOH,0.85,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Training,modelAOH,1.12,userName));
			ms.create(new ModelStatistic(statisticR2_CV_Training,modelAOH,0.85,userName));		
			ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelAOH,1.14,userName));
			ms.create(new ModelStatistic(statisticR2_Test,modelAOH,0.83,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Test,modelAOH,1.23,userName));

			Model modelKOC=getModel(DevQsarConstants.KOC, mapModels);
			ms.create(new ModelStatistic(statisticR2_Training,modelKOC,0.81,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Training,modelKOC,0.54,userName));
			ms.create(new ModelStatistic(statisticR2_CV_Training,modelKOC,0.81,userName));		
			ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelKOC,0.55,userName));
			ms.create(new ModelStatistic(statisticR2_Test,modelKOC,0.71,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Test,modelKOC,0.61,userName));

			Model modelKM=getModel(DevQsarConstants.KmHL, mapModels);
			ms.create(new ModelStatistic(statisticR2_Training,modelKM,0.82,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Training,modelKM,0.5,userName));
			ms.create(new ModelStatistic(statisticR2_CV_Training,modelKM,0.83,userName));		
			ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelKM,0.49,userName));
			ms.create(new ModelStatistic(statisticR2_Test,modelKM,0.73,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Test,modelKM,0.62,userName));

			Model modelRBiodeg=getModel(DevQsarConstants.RBIODEG, mapModels);
			ms.create(new ModelStatistic(statisticBA_Training,modelRBiodeg,0.8,userName));		
			ms.create(new ModelStatistic(statisticSN_Training,modelRBiodeg,0.82,userName));
			ms.create(new ModelStatistic(statisticSP_Training,modelRBiodeg,0.79,userName));
			ms.create(new ModelStatistic(statisticBA_CV_Training,modelRBiodeg,0.8,userName));		
			ms.create(new ModelStatistic(statisticSN_CV_Training,modelRBiodeg,0.82,userName));
			ms.create(new ModelStatistic(statisticSP_CV_Training,modelRBiodeg,0.78,userName));
			ms.create(new ModelStatistic(statisticBA_Test,modelRBiodeg,0.79,userName));		
			ms.create(new ModelStatistic(statisticSN_Test,modelRBiodeg,0.81,userName));
			ms.create(new ModelStatistic(statisticSP_Test,modelRBiodeg,0.77,userName));

			Model modelBIODEG=getModel(DevQsarConstants.BIODEG_HL_HC, mapModels);
			ms.create(new ModelStatistic(statisticR2_Training,modelBIODEG,0.88,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Training,modelBIODEG,0.26,userName));
			ms.create(new ModelStatistic(statisticR2_CV_Training,modelBIODEG,0.89,userName));		
			ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelBIODEG,0.25,userName));
			ms.create(new ModelStatistic(statisticR2_Test,modelBIODEG,0.75,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Test,modelBIODEG,0.38,userName));


			Model modelBCF=getModel(DevQsarConstants.BCF, mapModels);
			ms.create(new ModelStatistic(statisticR2_Training,modelBCF,0.85,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Training,modelBCF,0.53,userName));
			ms.create(new ModelStatistic(statisticR2_CV_Training,modelBCF,0.84,userName));		
			ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelBCF,0.55,userName));
			ms.create(new ModelStatistic(statisticR2_Test,modelBCF,0.83,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Test,modelBCF,0.64,userName));


			Model modelCATMOS_LD50=getModel(DevQsarConstants.ORAL_RAT_LD50, mapModels);
			//Only stats in the QMRF:
			ms.create(new ModelStatistic(statisticR2_Training,modelCATMOS_LD50,0.85,userName));		
			ms.create(new ModelStatistic(statisticRMSE_Training,modelCATMOS_LD50,0.30,userName));
			ms.create(new ModelStatistic(statisticR2_CV_Training,modelCATMOS_LD50,0.79,userName));		


			//			Model modelCATMOS_VT=getModel(DevQsarConstants.ORAL_RAT_VERY_TOXIC, mapModels);
			//			//Only stats in the QMRF:
			//			ms.create(new ModelStatistic(statisticSN_Training,modelCATMOS_VT,0.87,userName));		
			//			ms.create(new ModelStatistic(statisticSP_Training,modelCATMOS_VT,0.99,userName));
			//			ms.create(new ModelStatistic(statisticBA_Training,modelCATMOS_VT,0.92,userName));
			//			ms.create(new ModelStatistic(statisticBA_CV_Training,modelCATMOS_VT,0.79,userName));		
			//			ms.create(new ModelStatistic(statisticSN_Test,modelCATMOS_VT,0.87,userName));		
			//			ms.create(new ModelStatistic(statisticSP_Test,modelCATMOS_VT,0.99,userName));
			//			ms.create(new ModelStatistic(statisticBA_Test,modelCATMOS_VT,0.93,userName));
			//
			//
			//			Model modelCATMOS_NT=getModel(DevQsarConstants.ORAL_RAT_NON_TOXIC, mapModels);
			//			//Only stats in the QMRF:
			//			ms.create(new ModelStatistic(statisticSN_Training,modelCATMOS_NT,0.88,userName));		
			//			ms.create(new ModelStatistic(statisticSP_Training,modelCATMOS_NT,0.97,userName));
			//			ms.create(new ModelStatistic(statisticBA_Training,modelCATMOS_NT,0.92,userName));
			//			ms.create(new ModelStatistic(statisticBA_CV_Training,modelCATMOS_NT,0.9,userName));		
			//			ms.create(new ModelStatistic(statisticSN_Test,modelCATMOS_NT,0.88,userName));		
			//			ms.create(new ModelStatistic(statisticSP_Test,modelCATMOS_NT,0.97,userName));
			//			ms.create(new ModelStatistic(statisticBA_Test,modelCATMOS_NT,0.92,userName));
			//
			//			Model modelCATMOS_EPA=getModel(DevQsarConstants.ORAL_RAT_EPA_CATEGORY, mapModels);
			//			//Only stats in the QMRF:
			//			ms.create(new ModelStatistic(statisticSN_Training,modelCATMOS_EPA,0.81,userName));		
			//			ms.create(new ModelStatistic(statisticSP_Training,modelCATMOS_EPA,0.92,userName));
			//			ms.create(new ModelStatistic(statisticBA_Training,modelCATMOS_EPA,0.87,userName));
			//			ms.create(new ModelStatistic(statisticBA_CV_Training,modelCATMOS_EPA,0.79,userName));		
			//			ms.create(new ModelStatistic(statisticSN_Test,modelCATMOS_EPA,0.81,userName));		
			//			ms.create(new ModelStatistic(statisticSP_Test,modelCATMOS_EPA,0.92,userName));
			//			ms.create(new ModelStatistic(statisticBA_Test,modelCATMOS_EPA,0.87,userName));
			//
			//
			//			Model modelCATMOS_GHS=getModel(DevQsarConstants.ORAL_RAT_GHS_CATEGORY, mapModels);
			//			//Only stats in the QMRF:
			//			ms.create(new ModelStatistic(statisticSN_Training,modelCATMOS_GHS,0.8,userName));		
			//			ms.create(new ModelStatistic(statisticSP_Training,modelCATMOS_GHS,0.95,userName));
			//			ms.create(new ModelStatistic(statisticBA_Training,modelCATMOS_GHS,0.88,userName));
			//			ms.create(new ModelStatistic(statisticBA_CV_Training,modelCATMOS_GHS,0.78,userName));		
			//			ms.create(new ModelStatistic(statisticSN_Test,modelCATMOS_GHS,0.8,userName));		
			//			ms.create(new ModelStatistic(statisticSP_Test,modelCATMOS_GHS,0.95,userName));
			//			ms.create(new ModelStatistic(statisticBA_Test,modelCATMOS_GHS,0.88,userName));


			//TODO add stats for following		

			// Get some of these from papers?		
			//		OPERA2.9_pKa_Basic
			//		OPERA2.9_pKa_Acidic
			//		OPERA2.9_LogD_ph7.4
			//		OPERA2.9_LogD_ph5.5

			//		OPERA2.9_CoMPARA_Binding
			//		OPERA2.9_CoMPARA_Antagonist
			//		OPERA2.9_CoMPARA_Agonist

			//		OPERA2.9_CERAPP_Binding
			//		OPERA2.9_CERAPP_Antagonist
			//		OPERA2.9_CERAPP_Agonist

			//		OPERA2.9_CACO2
			//		OPERA2.9_liq_chrom_Retention_Time
			//		OPERA2.9_Clint
			//		OPERA2.9_FUB


		}

		String getDatasetName(String propertyNameDB) {
			String v=null;

			String p=propertyNameDB;

			//From OPERA_models_2.9.xlsx:

			if(version.equals("2.9")) {
				if (p.equals(DevQsarConstants.BOILING_POINT) 
						|| p.equals(DevQsarConstants.MELTING_POINT)
						|| p.equals(DevQsarConstants.VAPOR_PRESSURE)
						|| p.equals(DevQsarConstants.LOG_KOW)
						|| p.equals(DevQsarConstants.LogD_pH_5_5)
						|| p.equals(DevQsarConstants.LogD_pH_7_4)
						|| p.equals(DevQsarConstants.WATER_SOLUBILITY)
						|| p.equals(DevQsarConstants.HENRYS_LAW_CONSTANT)) {
					v="2.9";
				} else if (p.equals(DevQsarConstants.CACO2)
						|| p.equals(DevQsarConstants.FUB)
						|| p.equals(DevQsarConstants.CLINT)) {
					v="2.8";
				} else {
					v="2.6";
				}
			} if(version.equals("2.8")) {

				if (p.equals(DevQsarConstants.CACO2)
						|| p.equals(DevQsarConstants.FUB)
						|| p.equals(DevQsarConstants.CLINT)) {
					v="2.8";

				} else {
					v="2.6";
				}
			}
			return propertyNameDB+" OPERA"+v;

		}

		Model getModel(String propertyName,TreeMap <String,Model>mapModels) {
			String modelName=getModelName(propertyName);
			return mapModels.get(modelName);
		}

		//	OPERA_lookups lookups=null;

		String getSource() {
			return "OPERA"+version;
		}

		/**
		 * Gets an OPERA model name based on the property name in the database
		 * 
		 * @param propertyNameDB
		 * @return
		 */
		public String getModelName(String propertyNameDB) {
			//using opera model names from following sql query: 
			//select distinct od."modelName" from stg_datahub.opera.opera_data od

			String abbrev="";


			switch (propertyNameDB) {

			case DevQsarConstants.BOILING_POINT:
				abbrev="BP";
				break;
			case DevQsarConstants.MELTING_POINT:
				abbrev="MP";
				break;
			case DevQsarConstants.HENRYS_LAW_CONSTANT:
				abbrev="HL";
				break;
			case DevQsarConstants.LOG_KOW:
				abbrev="LogP";
				break;
			case DevQsarConstants.LOG_KOA:
				abbrev="LogKOA";
				break;
			case DevQsarConstants.KOC:
				abbrev="KOC";
				break;
			case DevQsarConstants.KmHL:
				abbrev="KM";
				break;
			case DevQsarConstants.OH:
				abbrev="AOH";
				break;
			case DevQsarConstants.BIODEG_HL_HC:
				abbrev="BioDeg";
				break;
			case DevQsarConstants.RBIODEG:
				abbrev="RBioDeg";
				break;
			case DevQsarConstants.LogD_pH_5_5:
				abbrev="LogD-pH5.5";
				break;
			case DevQsarConstants.LogD_pH_7_4:
				abbrev="LogD-pH7.4";
				break;
			case DevQsarConstants.BCF:
				abbrev="BCF";
				break;
			case DevQsarConstants.CLINT:
				abbrev="Clint";
				break;
			case DevQsarConstants.FUB:
				abbrev="FUB";
				break;
			case DevQsarConstants.RT:
				abbrev="RT";
				break;
			case DevQsarConstants.ORAL_RAT_LD50:
				abbrev="CATMoS-LD50";
				break;
			case DevQsarConstants.ORAL_RAT_VERY_TOXIC:
				abbrev="CATMoS-VT";
				break;
			case DevQsarConstants.ORAL_RAT_NON_TOXIC:
				abbrev="CATMoS-NT";
				break;
			case DevQsarConstants.ORAL_RAT_GHS_CATEGORY:
				abbrev="CATMoS-GHS";
				break;
			case DevQsarConstants.ORAL_RAT_EPA_CATEGORY:
				abbrev="CATMoS-EPA";
				break;
			case DevQsarConstants.WATER_SOLUBILITY:
				abbrev="WS";
				break;
			case DevQsarConstants.VAPOR_PRESSURE:
				abbrev="VP";
				break;
			case DevQsarConstants.CACO2:
				abbrev="CACO2";
				break;
			case DevQsarConstants.ESTROGEN_RECEPTOR_AGONIST:
				abbrev="CERAPP-Agonist";
				break;
			case DevQsarConstants.ESTROGEN_RECEPTOR_ANTAGONIST:
				abbrev="CERAPP-Antagonist";
				break;
			case DevQsarConstants.ESTROGEN_RECEPTOR_BINDING:
				abbrev="CERAPP-Binding";
				break;
			case DevQsarConstants.ANDROGEN_RECEPTOR_AGONIST:
				abbrev="CoMPARA-Agonist";
				break;
			case DevQsarConstants.ANDROGEN_RECEPTOR_ANTAGONIST:
				abbrev="CoMPARA-Antagonist";
				break;
			case DevQsarConstants.ANDROGEN_RECEPTOR_BINDING:
				abbrev="CoMPARA-Binding";
				break;
			case DevQsarConstants.PKA_A:
				abbrev="pKa-Acidic";
				break;
			case DevQsarConstants.PKA_B:
				abbrev="pKa-Basic";
				break;

			default:
				return null;
			}

			//		return abbrev;//get the version from the fk_source_id in models table instead
			//		return "OPERA"+version+"_"+abbrev;
			return abbrev+" OPERA"+version;// can also get version from fk_source_id in models table

		}


	}//end InitializeDB class


	class ConvertToPredictionDashboard {

		ColumnHandler columnHandler=new ColumnHandler();
		NeighborMethods neighborMethods=new NeighborMethods();


		class ColumnHandler {


			private String getValue(List<String> colNamesCSV, List<String> values, String colName) {

				String value=values.get(colNamesCSV.indexOf(colName));

				if(value!=null && value.length()>0 && value.substring(value.length()-1,value.length()).equals("|")) {
					value=value.substring(0,value.length()-1);
				}
				value=value.trim();
				if (value.isBlank() || value.equals("?")) value=null;
				if(value!=null && value.equals("NA")) value=null;
				return value;
			}

			private void handleColumn(PredictionDashboardTableMaps lookups, Hashtable<String, OPERA_Structure> htDTXCIDToOperaStructure,
					String propertyName, PredictionDashboard pd, List<QsarPredictedADEstimate> qsarPredictedADEstimates,
					String unitName, String unitNameContributor, String colName,
					String value) {


				List<QsarPredictedNeighbor>neighbors=pd.getQsarPredictedNeighbors();

				if (colName.equals(STR_DTXCID)) {
					handleDTXCID(lookups, htDTXCIDToOperaStructure, pd, value);
				} else if (colName.contains("AD_") || colName.contains("AD_index") || colName.contains("Conf_index") ) {
					handleAD(lookups, pd, qsarPredictedADEstimates, colName, value);
					//					System.out.println(fieldName+"\t"+value+"\t"+unitNameContributor);
				} else if (colName.contains("CAS_neighbor")) {
					if(storeNeighbors) handleNeighborID(colName, neighbors, colName, value);
				} else if (colName.contains("InChiKey_neighbor")) {
					if(storeNeighbors) handleNeighborID(colName, neighbors, colName, value);
				} else if (colName.contains("DSSTOXMPID_neighbor")) {
					//dont need to store
				} else if (colName.contains("DTXSID_neighbor")) {
					if(storeNeighbors) handleNeighborID(colName, neighbors, colName, value);
				} else if (colName.contains("Exp_neighbor") ) {
					if(storeNeighbors) handleNeighborExp(pd, neighbors, unitName, unitNameContributor, colName, value, propertyName);
				} else if (colName.contains("pred_neighbor")) {
					if(storeNeighbors) handleNeighborPred(pd, neighbors, unitName, unitNameContributor, colName, value, propertyName);
				} else if  (colName.contains("_exp")) {
					handleExp(propertyName, pd, unitName, unitNameContributor, colName, value);
				} else if (colName.contains("_pred")) {
					Double dvalue = convertUnits(unitName, unitNameContributor, colName, value,pd.getDsstoxRecord());
					pd.setPredictionValue(dvalue);
//					System.out.println(colName+"\t"+dvalue);
				}
				
				
			}

			/**
			 * 
			 * Returns a map with key = property name and value is list of columns that correspond to that property
			 * TreeMap sorts entry by key
			 * 
			 * This is necessary because columns are not consistently labeled in terms of 
			 * the property name and sometimes multiple properties are lumped into the 
			 * same block of columns (rather than just making a each property completely separate)
			 * 
			 * @param colNamesAll column names from the output prediction text file
			 * @return map of column names to property name
			 */
			private TreeMap<String, List<String>> assignColumnsByProperty(List<String> colNamesAll) {


				TreeMap<String, List<String>> ht=new TreeMap<>();

				addSpecialPropertiesToColNameMap(colNamesAll, STR_DTXCID, ht);

				for (String colName:colNamesAll) {

					String propName = "";

					if (colName.contains("predRange") || !colName.contains("_") ||					
							colName.equals(STR_DTXCID) || colName.contains("pKa") || colName.contains("LogD")) {
						continue;//already handled earlier
					} else if (colName.contains("LogOH") || colName.contains("AOH")) {
						propName = DevQsarConstants.OH;
					} else if (colName.contains("BCF")) {
						propName = DevQsarConstants.BCF;				
					} else if (colName.contains("HL")) {
						propName = DevQsarConstants.HENRYS_LAW_CONSTANT;				
					} else if (colName.contains("KOA")) {
						propName = DevQsarConstants.LOG_KOA;				
					} else if (colName.contains("Koc")) {
						propName = DevQsarConstants.KOC;				
					} else if (colName.contains("KM")) {
						propName = DevQsarConstants.KmHL;				
					} else if (colName.contains("ReadyBiodeg")) {
						propName = DevQsarConstants.RBIODEG;				
					} else if (colName.contains("BioDeg")) {
						propName = DevQsarConstants.BIODEG_HL_HC;				
					} else if (colName.contains("BP")) {
						propName = DevQsarConstants.BOILING_POINT;				
					} else if (colName.contains("RT")) {
						propName = DevQsarConstants.RT;				
					} else if (colName.contains("WS")) {
						propName = DevQsarConstants.WATER_SOLUBILITY;				
					} else if (colName.contains("VP")) {
						propName = DevQsarConstants.VAPOR_PRESSURE;				
					} else if (colName.contains("LogP")) {
						propName = DevQsarConstants.LOG_KOW;				
					} else if (colName.contains("CERAPP_Bind")) {
						propName = DevQsarConstants.ESTROGEN_RECEPTOR_BINDING;				
					} else if (colName.contains("CERAPP_Ago")) {
						propName = DevQsarConstants.ESTROGEN_RECEPTOR_AGONIST;				
					} else if (colName.contains("CERAPP_Anta")) {
						propName = DevQsarConstants.ESTROGEN_RECEPTOR_ANTAGONIST;				
					} else if (colName.contains("CoMPARA_Bind")) {
						propName = DevQsarConstants.ANDROGEN_RECEPTOR_BINDING;				
					} else if (colName.contains("CoMPARA_Ago")) {
						propName = DevQsarConstants.ANDROGEN_RECEPTOR_AGONIST;				
					} else if (colName.contains("CoMPARA_Anta")) {
						propName = DevQsarConstants.ANDROGEN_RECEPTOR_ANTAGONIST;				
					} else if (colName.contains("CACO2")) {
						propName = DevQsarConstants.CACO2;				
					} else if (colName.contains("Clint")) {
						propName = DevQsarConstants.CLINT;				
					} else if (colName.contains("FUB")) {
						propName = DevQsarConstants.FUB;				
					} else if (colName.contains("MP")) {
						propName = DevQsarConstants.MELTING_POINT;								
						//			} else if (colName.equals("CATMoS_VT_pred")) {
						//				propName=DevQsarConstants.ORAL_RAT_VERY_TOXIC;//this will only end up storing the predicted value for these properties
						//			} else if (colName.equals("CATMoS_NT_pred")) {
						//				propName=DevQsarConstants.ORAL_RAT_NON_TOXIC;//this will only end up storing the predicted value for these properties
						//			} else if (colName.equals("CATMoS_EPA_pred")) {
						//				propName=DevQsarConstants.ORAL_RAT_EPA_CATEGORY;
						//			} else if (colName.equals("CATMoS_GHS_pred")) {
						//				propName=DevQsarConstants.ORAL_RAT_GHS_CATEGORY;//this will only end up storing the predicted value for these properties
					} else if (colName.contains("LD50") || colName.contains("CATMoS") || 
							colName.startsWith("CAS_neighbor_") || colName.startsWith("DTXSID_neighbor_") ||colName.startsWith("InChiKey_neighbor_")) {
						propName = DevQsarConstants.ORAL_RAT_LD50;
						//For catmos LD50, we have neighbors and exp value whereas the other catmos properties dont???
					} else {
						System.out.println(colName+"\tUnassigned");
					}

					if (ht.get(propName) == null) {
						List<String> colNames = new ArrayList<>();
						colNames.add(colName);
						ht.put(propName, colNames);
					} else {
						List<String> colNames = ht.get(propName);
						colNames.add(colName);
					}
				}//end loop over colNames


				//duplicate columns to other catmos properties:
				//		duplicateCatmosColumns(ht);


				//Add CID to each list
				Set<String> keys=ht.keySet();		
				for (String key:keys) {
					List<String> colNames = ht.get(key);
					colNames.add(0,"DSSTOX_COMPOUND_ID");
					//			if(colNames.size()!=26) {
					//				System.out.println(key+"\t"+colNames.size());	
					//			}
				}

				//		List<String> colNames = ht.get("CATMOS_LD50");
				//		for (int i=0;i<colNames.size();i++) {
				//			System.out.println((i+1)+"\t"+colNames.get(i));
				//		}

				ht.remove("");

				//		for(String key:ht.keySet()) {
				//			System.out.println("\n"+key);
				//			
				//			List<String> colNames = ht.get(key);
				//			for (String colName:colNames) {
				//				System.out.println("\t"+colName);
				//			}
				//		}

				return ht;

			}


			private void handleNeighborExp(PredictionDashboard pd, List<QsarPredictedNeighbor> neighbors, String unitName,
					String unitNameContributor, String colName, String value,String propertyName) {
				int neighborNum=Integer.parseInt(colName.substring(colName.length()-1,colName.length()));
				QsarPredictedNeighbor neighbor=neighbors.get(neighborNum-1);

				Double dvalue = convertUnits(unitName, unitNameContributor, colName, value,pd.getDsstoxRecord());

				if(dvalue!=null) {
					neighbor.setExperimentalValue(dvalue);
					neighbor.setExperimentalString(getBinaryConclusion(propertyName, dvalue));
				} else {
					neighbor.setExperimentalString(value);
				}
			}


			private void handleNeighborID(String column, List<QsarPredictedNeighbor> neighbors, String colName, String value) {

				int neighborNum=Integer.parseInt(colName.substring(colName.length()-1,colName.length()));
				QsarPredictedNeighbor neighbor=neighbors.get(neighborNum-1);

				if(value.substring(value.length()-1,value.length()).equals("|")) {
					value=value.substring(0,value.length()-1);
				}		

				if(column.contains("DTXSID")) {
					neighbor.setDtxsid(value);	
				} else if (column.contains("InChiKey")) {
					neighbor.setInchiKey(value);
				} else if (column.contains("CAS")) {
					neighbor.setCasrn(value);
				}

			}

			/**
			 * This method handles logD and pKa which essentially have multiple properties sharing the same fields
			 * 
			 * @param colNamesAll
			 * @param dtxcid
			 * @param ht
			 */
			private void addSpecialPropertiesToColNameMap(List<String> colNamesAll, String dtxcid,
					TreeMap<String, List<String>> ht) {

				ht.put(DevQsarConstants.LogD_pH_5_5, new ArrayList<>());
				ht.put(DevQsarConstants.LogD_pH_7_4, new ArrayList<>());
				ht.put(DevQsarConstants.PKA_A, new ArrayList<>());
				ht.put(DevQsarConstants.PKA_B, new ArrayList<>());

				for (String colName : colNamesAll) {

					if (colName.contains("predRange") || !colName.contains("_") || colName.equals(dtxcid))
						continue;

					if (colName.contains("LogD")) {
						if (colName.contains("LogD55"))
							ht.get(DevQsarConstants.LogD_pH_5_5).add(colName);
						else if (colName.contains("LogD74"))
							ht.get(DevQsarConstants.LogD_pH_7_4).add(colName);
						else {// add to both
							ht.get(DevQsarConstants.LogD_pH_5_5).add(colName);
							ht.get(DevQsarConstants.LogD_pH_7_4).add(colName);
						}
					}

					if (colName.contains("pKa")) {
						if (colName.contains("pKa_a"))
							ht.get(DevQsarConstants.PKA_A).add(colName);
						else if (colName.contains("pKa_b"))
							ht.get(DevQsarConstants.PKA_B).add(colName);
						else {// add to both
							ht.get(DevQsarConstants.PKA_A).add(colName);
							ht.get(DevQsarConstants.PKA_B).add(colName);
						}
					}
				}
			}

			private void handleExp(String propertyName, PredictionDashboard pd, String unitName, String unitNameContributor,
					String colName, String value) {
				//					if (propertyName.contains("Oral rat"))
				//						System.out.println(propertyName+"\t"+value+"\t"+pd.getDsstoxRecord().getDtxcid());

				//					if(propertyName.equals(DevQsarConstants.ORAL_RAT_VERY_TOXIC)|| 
				//							propertyName.equals(DevQsarConstants.ORAL_RAT_NON_TOXIC) || 
				//							propertyName.equals(DevQsarConstants.ORAL_RAT_EPA_CATEGORY) || 
				//							propertyName.equals(DevQsarConstants.ORAL_RAT_GHS_CATEGORY) ) {

				if(propertyName.equals(DevQsarConstants.ORAL_RAT_LD50) || propertyName.contains("receptor")) {
					pd.setExperimentalString(value);//always store as string because sometimes cant be saved as number due to -,<,> characters
				} else {
					Double dvalue = convertUnits(unitName, unitNameContributor, colName, value,pd.getDsstoxRecord());

					//			String strValue=HTML_Report_Creator_From_OPERA_Report.getFormattedValue(dvalue, propertyName);
					//			dvalue=Double.parseDouble(strValue);

					if(dvalue!=null) {
						pd.setExperimentalValue(dvalue);
					} else {
						pd.setExperimentalString(value);
					}
				}

				//		if(pd.getDtxcid().equals("DTXCID606")) {
				//			System.out.println(propertyName+"\t"+pd.getExperimentalValue()+"\t"+pd.getExperimentalString());
				//		}


			}

			private void handleAD(PredictionDashboardTableMaps lookups, PredictionDashboard pd,
					List<QsarPredictedADEstimate> qsarPredictedADEstimates, String colName, String value) {
				MethodAD methodAD=null;

				Double dvalue=Double.parseDouble(value); 

				if(dvalue!=null) {
					DecimalFormat df=new DecimalFormat("0.000");
					dvalue=Double.parseDouble(df.format(dvalue));//use 3 decimal places
				}


				String conclusion=null;
				String reasoning=null;

				if (colName.contains("AD_index")) {
					methodAD=lookups.mapMethodAD.get(DevQsarConstants.Applicability_Domain_OPERA_local_index);
				} else if (colName.contains("Conf_index")) {
					methodAD=lookups.mapMethodAD.get(DevQsarConstants.Applicability_Domain_OPERA_confidence_level);
				} else if (colName.contains("AD_")) {
					methodAD=lookups.mapMethodAD.get(DevQsarConstants.Applicability_Domain_OPERA_global_index);
					if(dvalue==1) conclusion="Inside";
					else if (dvalue==0) conclusion="Outside";
					reasoning=conclusion+" AD since value = "+value;
				}

				QsarPredictedADEstimate q=new QsarPredictedADEstimate();
				q.setCreatedBy(userName);
				q.setMethodAD(methodAD);
				q.setApplicabilityValue(dvalue);
				q.setPredictionDashboard(pd);
				q.setConclusion(conclusion);
				q.setReasoning(reasoning);

				if (colName.contains("Conf_index")) {
					qsarPredictedADEstimates.add(0,q);
				} else {
					qsarPredictedADEstimates.add(q);	
				}


				//		System.out.println("here methodAD="+methodAD==null);
				//		System.out.println(methodAD.getName());
			}

			private void handleDTXCID(PredictionDashboardTableMaps lookups, Hashtable<String, OPERA_Structure> htDTXCIDToOperaStructure,
					PredictionDashboard pd, String value) {

				if(lookups.mapDsstoxRecordsByCID.get(value)!=null) {
					pd.setDsstoxRecord(lookups.mapDsstoxRecordsByCID.get(value));
					pd.setDtxcid(value);
					//				OPERA_Structure opera_structure=htDTXCIDToOperaStructure.get(value);
					//TODO check if DTXSID in OPERA_2.8.db "IDs" table matches the one from the snapshot ht 

				} else {
					pd.setDtxcid(value);
					//				DsstoxRecord dr=new DsstoxRecord();
					//				dr.setDtxcid(value);
					//				dr.setId(-1L);
					//				pd.setDsstoxRecord(dr);
					//System.out.println("no matching  dsstoxRecord for "+value);
				}
				//Look up qsar ready smiles used by OPERA:
				pd.setCanonQsarSmiles(htDTXCIDToOperaStructure.get(value).Canonical_QSARr);

				//		if(pd.getCanonQsarSmiles()==null) {
				//			System.out.println(pd.getDtxcid()+"\tNo qsarSmiles");
				//		}

			}

			//	private Double getCategoryEPA(String strvalue) {
			//
			//		if(strvalue.equals("501-5000")) return 3.0;
			//				
			//		double value=Double.parseDouble(strvalue);
			//		if (value<=50) return 1.0;
			//		else if (value<=500) return 2.0;
			//		else if(value<=5000) return 3.0;
			//		else if(value>5000) return 4.0;
			//		else return null;
			//	}
			//	
			//	private Double getCategoryGHS(String strvalue) {
			//		double value=Double.parseDouble(strvalue);
			//		if (value<=5) return 1.0;
			//		else if (value<=50) return 2.0;
			//		else if(value<=300) return 3.0;
			//		else if(value<=2000) return 4.0;
			//		else if(value>2000) return 5.0;
			//		else return null;
			//	}





			/**
			 * Converts units to units indicated in unitNameContributor
			 *  
			 * @param unitName
			 * @param unitNameContributor
			 * @param colName
			 * @param value
			 * @param dr
			 * @return
			 */
			private Double convertUnits(String unitName, String unitNameContributor, String colName, String value,DsstoxRecord dr) {

				Double dvalue=null;

				try {
					dvalue=Double.parseDouble(value);
				} catch (Exception ex) {
					return null;
				}

				if(unitName.contains("LOG") && !unitNameContributor.contains("LOG")) {
					dvalue=Math.pow(10, dvalue);						
					//			System.out.println("Converted:"+colName+"\t"+dvalue+"\t"+unitNameContributor);

				} else if (!unitName.equals(unitNameContributor)) {

					if (unitName.equals("MG_KG") && unitNameContributor.equals("MOL_KG")) {

						if(dr==null) return null;

						double MW=dr.getMolWeight(); // need MW for oral rat LD50

						try {

							if(MW==0) {
								Indigo indigo = new Indigo();
								indigo.setOption("ignore-stereochemistry-errors", true);
								IndigoObject indigoMolecule = indigo.loadMolecule(dr.getSmiles());
								MW=indigoMolecule.molecularWeight();
							}

							if(MW!=0) {
								dvalue=dvalue/1000.0/MW;
								//						System.out.println("Original:"+colName+"\t"+value+"\t"+unitName);
								//						System.out.println("Converted:"+colName+"\t"+dvalue+"\t"+unitNameContributor);
							} else {
								System.out.println(dr.getDtxcid()+"\tCant Convert:"+colName+"\t"+dvalue+"\t"+unitName+"\t"+unitNameContributor);
							}
						} catch (Exception ex) {
							System.out.println(dr.getDtxcid()+"\tCant Convert:"+colName+"\t"+dvalue+"\t"+unitName+"\t"+unitNameContributor);
						}

					} else {
						System.out.println(unitName+" != "+unitNameContributor);
						System.out.println("Cant Convert:"+colName+"\t"+dvalue+"\t"+unitName+"\t"+unitNameContributor);
					}

				} else {
					//			System.out.println("Dont need to convert:"+unitName+ " = "+unitNameContributor);
				}
				
//				if(colName.contains("pred"))
//					System.out.println(colName+"\t"+dvalue);
				
				return dvalue;
			}

			//	private Double getCategoryEPA(String strvalue) {
			//
			//		if(strvalue.equals("501-5000")) return 3.0;
			//				
			//		double value=Double.parseDouble(strvalue);
			//		if (value<=50) return 1.0;
			//		else if (value<=500) return 2.0;
			//		else if(value<=5000) return 3.0;
			//		else if(value>5000) return 4.0;
			//		else return null;
			//	}
			//	
			//	private Double getCategoryGHS(String strvalue) {
			//		double value=Double.parseDouble(strvalue);
			//		if (value<=5) return 1.0;
			//		else if (value<=50) return 2.0;
			//		else if(value<=300) return 3.0;
			//		else if(value<=2000) return 4.0;
			//		else if(value>2000) return 5.0;
			//		else return null;
			//	}





			private void duplicateCatmosColumns(TreeMap<String, List<String>> ht) {
				String[] props = { DevQsarConstants.ORAL_RAT_VERY_TOXIC, DevQsarConstants.ORAL_RAT_NON_TOXIC,
						DevQsarConstants.ORAL_RAT_EPA_CATEGORY, DevQsarConstants.ORAL_RAT_GHS_CATEGORY };

				for(String prop:props) {
					List<String> colNames = ht.get(prop);
					colNames.add(0,"CATMoS_LD50_exp");

					//TODO do we want neighbor section for CATMoS models other than LD50? Causes issues because the experimental units aren't MG_KG for these models
					//			for (int i=1;i<=5;i++) {
					//				colNames.add("CAS_neighbor_"+i);	
					//				colNames.add("DTXSID_neighbor_"+i);
					//				colNames.add("InChiKey_neighbor_"+i);
					//				colNames.add("LD50_Exp_neighbor_"+i);
					//			}
				}
			}

			private void handleNeighborPred(PredictionDashboard pd, List<QsarPredictedNeighbor> neighbors, String unitName,
					String unitNameContributor, String colName, String value,String propertyName) {

				int neighborNum=Integer.parseInt(colName.substring(colName.length()-1,colName.length()));
				QsarPredictedNeighbor neighbor=neighbors.get(neighborNum-1);

				Double dvalue = columnHandler.convertUnits(unitName, unitNameContributor, colName, value,pd.getDsstoxRecord());

				if(dvalue!=null) {
					neighbor.setPredictedValue(dvalue);
					neighbor.setPredictedString(getBinaryConclusion(propertyName, dvalue));
				} else {
					neighbor.setPredictedString(value);
				}
			}

			private String getBinaryConclusion(String propertyName,Double value) {

				if(value==null) return null;

				if(propertyName.equals(DevQsarConstants.RBIODEG)) {
					if(value==0) return "Not readily biodeg.";
					else return "Readily biodeg.";
				} else if (propertyName.contains("receptor")) {
					if(value==0) {
						return "Inactive";
					} else {
						//				System.out.println(id);
						return "Active";
					}
				} else {
					return null;
				}
			}

		}//end class ColumnHandler

		class NeighborMethods {

			private void lookForMissingNeighbors(String propertyName, PredictionDashboard pd,
					List<QsarPredictedNeighbor> neighbors) {

				boolean haveFifthNeighbor=false;
				for (QsarPredictedNeighbor n:neighbors) {
					if(n.getNeighborNumber()==5) haveFifthNeighbor=true;
				}

				if(!haveFifthNeighbor) {
					System.out.println("Dont have 5th neighbor for "+propertyName+"\t"+pd.getDtxcid());
				}

				if (neighbors.size()<5) {
					System.out.println("Have have "+neighbors.size()+" neighbors for "+propertyName+"\t"+pd.getDtxcid());
				}

				//		for (QsarPredictedNeighbor n:neighbors) {
				//		System.out.println("#\t"+n.getNeighborNumber());
				//		System.out.println("DTXSID\t"+n.getDtxsid());
				//		System.out.println("CAS\t"+n.getCasrn());
				//		System.out.println("exp\t"+n.getExp());
				//		System.out.println("pred\t"+n.getPred());
				//	}

			}

			private void printNeighbors(String propertyName, List<QsarPredictedNeighbor> neighbors) {
				for (int i=0;i<neighbors.size();i++) {
					QsarPredictedNeighbor qpn=neighbors.get(i);
					if (qpn.getDsstoxRecord()!=null) {
						System.out.println(propertyName+"\t"+i+"\t"+qpn.getCasrn()+"\t"+qpn.getDtxsid()+"\t"+qpn.getDsstoxRecord().getId()+"\t"+qpn.getExperimentalValue()+"\t"+qpn.getPredictedValue());
					} else {
						System.out.println(propertyName+"\t"+i+"\t"+qpn.getCasrn()+"\t"+qpn.getDtxsid()+"\tnull"+"\t"+qpn.getExperimentalValue()+"\t"+qpn.getPredictedValue());
					}
				}
			}

			private void neighborsInitialize(String propertyName, PredictionDashboard pd) {
				List<QsarPredictedNeighbor>neighbors=new ArrayList<>();
				pd.setQsarPredictedNeighbors(neighbors);

				//Initialize neighbors
				for (int i=1;i<=5;i++) {
					QsarPredictedNeighbor n=new QsarPredictedNeighbor();
					n.setNeighborNumber(i);
					n.setSplitNum(0);//training set neighbors given in OPERA
					n.setCreatedBy(userName);
					n.setPredictionDashboard(pd);
					//				n.setPredictionDashboard(pd);//causes "failed to lazily initialize a collection of role:"
					neighbors.add(n);
				}

				if(propertyName.toLowerCase().contains("pka")) {//only has 3 neighbors
					neighbors.remove(4);
					neighbors.remove(3);
				}

			}

			private void neighborsUpdate(PredictionDashboardTableMaps lookups, String propertyName, PredictionDashboard pd) {


				List<QsarPredictedNeighbor>neighbors=pd.getQsarPredictedNeighbors();

				QsarPredictedNeighbor.removeEmptyNeighbors(neighbors);
				//			System.out.println("\nBefore cloning");
				//			printNeighbors(propertyName, neighbors);


				//Split the pipe delimited neighbors:
				neighbors=QsarPredictedNeighbor.splitNeighbors(neighbors,propertyName,lookups);			
				pd.setQsarPredictedNeighbors(neighbors);

				//			System.out.println(Utilities.gson.toJson(neighbors));
				//			lookForMissingNeighbors(propertyName, pd, neighbors);

				//Add SIDs if possible based on snapshot lookups:
				QsarPredictedNeighbor.addNeighborMetadata(lookups, propertyName, neighbors);


				QsarPredictedNeighbor.removeDuplicates(propertyName, neighbors);
			}



		}

		/**
		 * Takes values from a row in OPERA output and converts to predictionsDashboard
		 *  
		 * @param predictionsDashboard
		 * @param colNamesCSV
		 * @param values
		 * @param htColNames
		 * @param lookups
		 * @param htDTXCIDToOperaStructure 
		 * @param  
		 */
		private List<PredictionDashboard> convertValuesToRecords(boolean writeReportToHardDrive, List<String> colNamesCSV, List<String> values,
				TreeMap<String, List<String>> htColNames, PredictionDashboardTableMaps lookups,
				Hashtable<String, OPERA_Structure> htDTXCIDToOperaStructure, HashSet<String> pd_keys) {
			//		String regex = "^[-+]?\\d*[.]?\\d+|^[-+]?\\d+[.]?\\d*";//used to detect numerical values vs text

			List<String>propertyNamesOPERA=DevQsarConstants.getOPERA_PropertyNames();
			List<PredictionDashboard>predictionsDashboard=new ArrayList<>();

			for (String propertyName:propertyNamesOPERA) {
				//			System.out.println(property);
				//		String modelName="LogBCF OPERA2.9";
				List<String>colNamesCSV_Property=htColNames.get(propertyName);//csv columns pertaining to selected property

				PredictionDashboard pd=new PredictionDashboard();
				pd.setCreatedBy(userName);

				List<QsarPredictedADEstimate>qsarPredictedADEstimates=new ArrayList<>();
				pd.setQsarPredictedADEstimates(qsarPredictedADEstimates);

				if(storeNeighbors)
					neighborMethods.neighborsInitialize(propertyName, pd);

				Model model=lookups.mapModels.get(initializeDB.getModelName(propertyName));
				pd.setModel(model);

				//			System.out.println(propertyName+"\t"+model.getModelStatistics().size());

				Dataset dataset=lookups.mapDatasets.get(model.getDatasetName());

				//			System.out.println(model.getDatasetName()+"\t"+dataset.getUnit().getAbbreviation());

				String unitName=dataset.getUnit().getName();
				String unitNameContributor=dataset.getUnitContributor().getName();

				if (colNamesCSV_Property==null) {
					System.out.println("colnames are null for "+propertyName);
					continue;
				}

				for (String colName:colNamesCSV_Property) {
					if(colNamesCSV.indexOf(colName)==-1) {
						System.out.println(colName+" missing for "+propertyName);
						continue;
					}

					String value = columnHandler.getValue(colNamesCSV, values, colName);
					//System.out.println(propertyName+"\t"+colName+"\t"+value);

					if (value==null) {
						continue;
					}

					columnHandler.handleColumn(lookups, htDTXCIDToOperaStructure, propertyName, pd, qsarPredictedADEstimates, 
							unitName, unitNameContributor, colName, value);
				}//done iterating over col names for property

//				System.out.println(pd.getDtxcid()+"\t"+pd.getModel().getName_ccd()+"\t"+pd.getPredictionValue());
				
				
				if(pd.getDsstoxRecord()!=null && pd_keys.contains(pd.getKey()) && !writeReportToHardDrive) {
//					System.out.println("Already have in db: "+pd.getKey());
					continue;
				}
				
				if(pd.getDsstoxRecord()==null) {
//					System.out.println("Null DsstoxRecord for "+pd.getDtxcid());
					continue;
				}

				//Add it to list of keys so that DTXCID duplicates in kamels db don't mess up 
				// batch insert, could fix his db but need to be done carefully
				pd_keys.add(pd.getKey());//hashsets dont store duplicates:

				
//				if(pd.getPredictionValue()==null) {
					//System.out.println(pd.getModel().getName()+"\t"+pd.getPredictionValue());
					//continue;//dont store in database?
//				}

				predictionsDashboard.add(pd);

				//			setOverallAD(pd,lookups);

				if(setOverallAD) setOverallAD2(pd,lookups);

				if(pd.getExperimentalString()==null) {
					pd.setExperimentalString(columnHandler.getBinaryConclusion(propertyName, pd.getExperimentalValue()));
				}
				pd.setPredictionString(columnHandler.getBinaryConclusion(propertyName, pd.getPredictionValue()));

				if(storeNeighbors) {
					neighborMethods.neighborsUpdate(lookups, propertyName, pd);
					//	System.out.println("\nAfter cloning");
					//	printNeighbors(propertyName, neighbors);
				}
				
				//Create report:
				if(createReports) {
					createReport(pd, lookups,useLegacyModelIds,writeReportToHardDrive);
					//					if(writeReportToHardDrive) extraMethods.writeReportToHardDrive(lookups, pd);
				}

				//			if(pd.getDsstoxRecord().getId()==-1) {
				//				System.out.println(pd.getDtxcid()+"\tNot in snapshot");
				//			}
				//			System.out.println(pd.toTsv());
				//			if(propertyName.equals(DevQsarConstants.OH)) {
				//				System.out.println(pd.getDsstoxRecord().getDtxsid()+"\t"+pd.getDsstoxRecord().getDtxcid()+"\t"+pd.getDsstoxRecord().getCasrn()+"\t"+pd.getDsstoxRecord().getSmiles());
				//			}

			}//end loop over properties

			if(predictionsDashboard.size()==0) return predictionsDashboard;

			if(compareOperaStructure) {
				PredictionDashboard pd=predictionsDashboard.get(0);
				if(pd.getDsstoxRecord()!=null) compareOPERAStructure(htDTXCIDToOperaStructure, pd);
			}

			return predictionsDashboard;
		}

		private void createReport(PredictionDashboard pd,PredictionDashboardTableMaps lookups,boolean useLegacyModelIds,boolean writeReportToHarddrive) {

			long t1=System.currentTimeMillis();

			if(pd.getDsstoxRecord()==null) {
				//		System.out.println("missing dsstoxrecord: "+pd.getDtxcid()+" for "+property.getName());
				return;
			}

			String datasetName=pd.getModel().getDatasetName();
			Dataset dataset=lookups.mapDatasets.get(datasetName);
			String unitAbbreviation=dataset.getUnitContributor().getAbbreviation_ccd();
			Property property=dataset.getProperty();

			OPERA_Report or=new OPERA_Report(pd,property, unitAbbreviation,useLegacyModelIds);
			String fileJson=or.toJson();

			HTMLReportCreatorOpera h=new HTMLReportCreatorOpera();
			//			or.modelDetails.loadPlotsFromDB=true;
			or.modelDetails.loadPlotsFromDB=false;
			String fileHtml=h.createReport(or);

			if(writeReportToHarddrive) {
				String folder = "data\\"+pd.getModel().getSource().getName()+"\\reports\\"+pd.getDsstoxRecord().getDtxsid();
				File Folder=new File(folder);
				Folder.mkdirs();

				String filename=or.chemicalIdentifiers.dtxsid+"_"+or.modelDetails.modelName+".html";

				h.writeStringToFile(fileHtml,folder,filename);
				h.writeStringToFile(fileJson,folder,filename.replace(".html",".json"));
			}

			//			or.toJsonFile(folder);
			//			h.toHTMLFile(or, folder);	

			//		String folder = "data\\opera\\reports\\"+pd.getDsstoxRecord().getDtxcid();
			//		File Folder=new File(folder);
			//		Folder.mkdirs();
			//		or.toHTMLFile(folder);
			//		or.toJsonFile(folder);
			//			System.out.println(Utilities.gson.toJson(or)+"\n\n*******************\n");

			PredictionReport predictionReport=new PredictionReport(pd, fileJson,fileHtml, userName);
			pd.setPredictionReport(predictionReport);

			long t2=System.currentTimeMillis();

			//			System.out.println("Time to create report for "+pd.getModel().getName()+"="+(t2-t1)+" milliseconds");

		}

		private void setOverallAD(PredictionDashboard pd, PredictionDashboardTableMaps lookups) {

			QsarPredictedADEstimate adEstimateOverall=new QsarPredictedADEstimate();

			if (pd.getQsarPredictedADEstimates()==null || pd.getQsarPredictedADEstimates().size()==0) return;

			QsarPredictedADEstimate localAD=null;
			QsarPredictedADEstimate globalAD=null;


			for (QsarPredictedADEstimate adEstimate:pd.getQsarPredictedADEstimates()) {
				if(adEstimate.getMethodAD().getName().equals(DevQsarConstants.Applicability_Domain_OPERA_local_index)) {
					localAD=adEstimate;
				} else if(adEstimate.getMethodAD().getName().equals(DevQsarConstants.Applicability_Domain_OPERA_global_index)) {
					globalAD=adEstimate;
				}
			}

			if (localAD==null || globalAD==null) {
				System.out.println("Cant set overall AD, AD missing");
				return;
			}

			double localValue=localAD.getApplicabilityValue();
			double globalValue=globalAD.getApplicabilityValue();


			if(localValue<0.4 && globalValue==0) {
				adEstimateOverall.setApplicabilityValue(0.0);
				adEstimateOverall.setConclusion("Outside");
				adEstimateOverall.setReasoning("Outside training set (Global AD = 0) and poor local representation (Local AD index < 0.4)");
			} else if (localValue<0.4 && globalValue==1) {
				adEstimateOverall.setApplicabilityValue(0.0);
				adEstimateOverall.setConclusion("Outside");
				adEstimateOverall.setReasoning("Inside training set (Global AD = 1) but poor local representation (Local AD index < 0.4)");
				System.out.println(pd.getDtxcid()+"\t"+pd.getModel().getDatasetName()+"\tInside global, outside local");
			} else if (localValue>=0.4 && localValue<=0.6  && globalValue==0) {
				adEstimateOverall.setApplicabilityValue(1.0);
				adEstimateOverall.setConclusion("Inside");			
				adEstimateOverall.setReasoning("Outside training set (Global AD = 0) but fair local representation (0.4 <= Local AD index <=0.6)");
			} else if (localValue>=0.4 && localValue<=0.6  && globalValue==1) {
				adEstimateOverall.setApplicabilityValue(1.0);
				adEstimateOverall.setConclusion("Inside");			
				adEstimateOverall.setReasoning("Inside training set (Global AD = 1) and fair local representation  (0.4 <= Local AD index <=0.6)");
			} else if (localValue>0.6 && globalValue==0) {
				adEstimateOverall.setApplicabilityValue(1.0);
				adEstimateOverall.setConclusion("Inside");			
				adEstimateOverall.setReasoning("Outside training set (Global AD = 0) but good local representation (Local AD index > 0.6)");
			} else if (localValue>0.6 && globalValue==1) {
				adEstimateOverall.setApplicabilityValue(1.0);
				adEstimateOverall.setConclusion("Inside");			
				adEstimateOverall.setReasoning("Inside training set (Global AD = 1) and good local representation (Local AD index > 0.6)");
			} else {
				adEstimateOverall.setReasoning("Cannot reach a conclusion based on the applicability domain results");//Does this even happen?
			}

			//Based on discussion with Kamel dont put one
			adEstimateOverall.setApplicabilityValue(null);
			adEstimateOverall.setConclusion(null);			
			adEstimateOverall.setMethodAD(lookups.mapMethodAD.get(DevQsarConstants.Applicability_Domain_Combined));
			pd.getQsarPredictedADEstimates().add(adEstimateOverall);//put before confidence

		}

		private void setOverallAD2(PredictionDashboard pd, PredictionDashboardTableMaps lookups) {


			QsarPredictedADEstimate overallAD=new QsarPredictedADEstimate();

			if (pd.getQsarPredictedADEstimates()==null || pd.getQsarPredictedADEstimates().size()==0) return;

			QsarPredictedADEstimate localAD=null;
			QsarPredictedADEstimate globalAD=null;
			QsarPredictedADEstimate confidenceAD=null;


			for (QsarPredictedADEstimate adEstimate:pd.getQsarPredictedADEstimates()) {
				if(adEstimate.getMethodAD().getName().equals(DevQsarConstants.Applicability_Domain_OPERA_local_index)) {
					localAD=adEstimate;
				} else if(adEstimate.getMethodAD().getName().equals(DevQsarConstants.Applicability_Domain_OPERA_global_index)) {
					globalAD=adEstimate;
				} else if(adEstimate.getMethodAD().getName().equals(DevQsarConstants.Applicability_Domain_OPERA_confidence_level)) {
					confidenceAD=adEstimate;
				}
			}

			if (localAD==null || globalAD==null) {
				System.out.println("Cant set overall AD, AD missing");
				return;
			}

			double localValue=localAD.getApplicabilityValue();
			double globalValue=globalAD.getApplicabilityValue();

			String reasoning=null;

			if(localValue<0.4 && globalValue==0) {
				reasoning=("Outside training set (Global AD = 0) and poor local representation (Local AD index = "+localAD.getApplicabilityValue()+" &lt; 0.4)");
			} else if (localValue<0.4 && globalValue==1) {
				reasoning=("Inside training set (Global AD = 1) but poor local representation (Local AD index = "+localAD.getApplicabilityValue()+" &le; 0.4)");
				//			System.out.println(pd.getDtxcid()+"\t"+pd.getModel().getDatasetName()+"\tInside global, outside local");
			} else if (localValue>=0.4 && localValue<=0.6  && globalValue==0) {
				reasoning=("Outside training set (Global AD = 0) but fair local representation (0.4 &le; Local AD index = "+localAD.getApplicabilityValue()+" &le; 0.6)");
			} else if (localValue>=0.4 && localValue<=0.6  && globalValue==1) {
				reasoning=("Inside training set (Global AD = 1) and fair local representation  (0.4 &le; Local AD index = "+localAD.getApplicabilityValue()+" &le; 0.6)");
			} else if (localValue>0.6 && globalValue==0) {
				reasoning=("Outside training set (Global AD = 0) but good local representation (Local AD index = "+localAD.getApplicabilityValue()+ " &gt; 0.6)");
			} else if (localValue>0.6 && globalValue==1) {
				reasoning=("Inside training set (Global AD = 1) and good local representation (Local AD index = "+localAD.getApplicabilityValue()+ " &gt; 0.6)");
			} else {
				reasoning=("Cannot reach a conclusion based on the applicability domain results");//Does this even happen?
			}

			overallAD.setReasoning(reasoning);
			overallAD.setCreatedBy(userName);
			overallAD.setMethodAD(lookups.mapMethodAD.get(DevQsarConstants.Applicability_Domain_Combined));
			overallAD.setPredictionDashboard(pd);

			pd.getQsarPredictedADEstimates().clear();
			pd.getQsarPredictedADEstimates().add(globalAD);
			pd.getQsarPredictedADEstimates().add(localAD);
			pd.getQsarPredictedADEstimates().add(confidenceAD);
			pd.getQsarPredictedADEstimates().add(overallAD);

		}



	}



}

