package gov.epa.run_from_java.scripts.PredictionDashboard.TEST;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

import java.util.zip.GZIPInputStream;

import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.Calculations.CreateLookups.GetTrainingTestSetPredictions;
import ToxPredictor.Application.Calculations.RunFromCommandLine.CompareStandaloneToSDE;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSDF;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSmiles;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSmiles.MoleculeCreator;

import ToxPredictor.Application.GUI.Miscellaneous.fraChart;
import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Application.model.PredictionResultsPrimaryTable;
import ToxPredictor.Application.model.SimilarChemical;
import ToxPredictor.Application.model.SimilarChemicals;

//import ToxPredictor.Application.model.IndividualPredictionsForConsensus.PredictionIndividualMethod;
//import ToxPredictor.Application.model.PredictionResults;
//import ToxPredictor.Application.model.PredictionResultsPrimaryTable;
//import ToxPredictor.Application.model.SimilarChemical;
//import ToxPredictor.Application.model.SimilarChemicals;


import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.Database.ResolverDb2;
import ToxPredictor.misc.StatisticsCalculator;
import ToxPredictor.misc.StatisticsCalculator.ModelPrediction;
import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxSnapshot;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.MethodAD;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedADEstimate;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor;
import gov.epa.databases.dev_qsar.qsar_models.entity.Source;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxRecordServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxSnapshotServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.MethodServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionReportServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceService;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceServiceImpl;
import gov.epa.endpoints.models.ModelBuilder;
//import gov.epa.endpoints.models.ModelPrediction;
//import gov.epa.endpoints.reports.WebTEST.ReportClasses.IndividualPredictionsForConsensus.PredictionIndividualMethod;
import gov.epa.run_from_java.scripts.QsarModelsScript;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.CreatorScript;
import gov.epa.run_from_java.scripts.PredictionDashboard.DatabaseUtilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionDashboardTableMaps;
import gov.epa.run_from_java.scripts.PredictionDashboard.OPERA.PredictionDashboardScriptOPERA;
import gov.epa.run_from_java.scripts.PredictionDashboard.TEST.PredictionDashboardScriptTEST.TESTAPIResults.PredictionAPI;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;


/**
 * @author TMARTI02
 */
public class PredictionDashboardScriptTEST  {

	//Classes for handling each aspect of data loading:
	ConvertPredictionResultsToPredictionDashboard converter=new ConvertPredictionResultsToPredictionDashboard();
	TESTUtilities testUtilities=new TESTUtilities();
	InitializeDB initializeDB=new InitializeDB();


	MethodServiceImpl methodService=new MethodServiceImpl();
	PredictionDashboardServiceImpl predictionDashboardService=new PredictionDashboardServiceImpl();
	PredictionReportServiceImpl predictionReportService=new PredictionReportServiceImpl();
	DsstoxRecordServiceImpl dsstoxRecordService=new  DsstoxRecordServiceImpl();


	//	long minModelId=223L;
	//	long maxModelID=240L;

	boolean useLatestModelIds=true; 
	boolean createReports=true;
	//	boolean convertPredictionMolarUnits=true;

	static String lanId="tmarti02";
	static String version="5.1.3";

	static PredictionDashboardTableMaps tableMaps;


	//	static String[] propertyNames = { "Fathead minnow LC50 (96 hr)", "Daphnia magna LC50 (48 hr)",
	//			"T. pyriformis IGC50 (48 hr)", "Oral rat LD50", "Bioconcentration factor", "Developmental Toxicity",
	//			"Mutagenicity", "Estrogen Receptor Binding", "Estrogen Receptor RBA", "Normal boiling point",
	//			"Melting point", "Flash point", "Vapor pressure at 25?C", "Density", "Surface tension at 25?C",
	//			"Thermal conductivity at 25?C", "Viscosity at 25?C", "Water solubility at 25?C" };


	
	//TODO these property names have been updated:
//	static String[] propertyNamesTestReports = { "Fathead minnow LC50 (96 hr)", "Daphnia magna LC50 (48 hr)",
//			"T. pyriformis IGC50 (48 hr)", "Oral rat LD50", "Bioconcentration factor", "Developmental Toxicity",
//			"Mutagenicity", "Normal boiling point",
//			"Melting point", "Flash point", "Vapor pressure at 25?C", "Density", "Surface tension at 25?C",
//			"Thermal conductivity at 25?C", "Viscosity at 25?C", "Water solubility at 25?C" };

	static String[] propertyNamesTestReports = { "96 Hour Fathead Minnow LC50", "48 Hour Daphnia Magna LC50",
			"48 Hour Tetrahymena Pyriformis IGC50", "Oral Rat LD50", "Bioconcentration Factor", "Developmental Toxicity",
			"Ames Mutagenicity", "Normal Boiling Point",
			"Melting Point", "Flash Point", "Vapor Pressure at 25°C", "Density at 25°C", 
			"Surface Tension at 25°C",
			"Thermal Conductivity at 25°C", "Viscosity at 25°C", "Water Solubility at 25°C" };
	

	public static class InitializeDB {

		ModelBuilder mb=new ModelBuilder(lanId);

		/**
		 * Creates properties, datasets, and models for OPERA in the postgres db
		 * 
		 */
		void initializeDB() {

			boolean postToDB=true;
			
			createDatasets();

			MethodServiceImpl methodService=new MethodServiceImpl();
			
			
			HashMap<String,Method> hmMethods=new HashMap<>();
			hmMethods.put("consensus_regressor",methodService.findByName("consensus_regressor"));
			hmMethods.put("consensus_classifier",methodService.findByName("consensus_classifier"));
			TreeMap<String, Model> hmModels = createModels(hmMethods);

			createStatistics(hmModels,postToDB);


		}

		/**
		 * Creates datasets and properties
		 */
		void createDatasets() {

			HashMap<String, String>hmUnitsDataset=DevQsarConstants.getDatasetFinalUnitsNameMap();
			HashMap<String, String>hmUnitsDatasetContributor=DevQsarConstants.getContributorUnitsNameMap();

			for (String propertyName:propertyNamesTestReports) {

				String propertyNameDB=TESTConstants.getPropertyNameDB(propertyName);
				
				if(propertyNameDB==null) {
					System.out.println("Null property name for "+propertyName);
					continue;
				}


				String datasetName = getDatasetName(propertyNameDB);

				String unitAbbrev=hmUnitsDataset.get(propertyNameDB);
				String unitContributorAbbrev=hmUnitsDatasetContributor.get(propertyNameDB);

//				System.out.println(propertyName+"\t"+propertyNameDB+"\t"+unitAbbrev+"\t"+unitContributorAbbrev);
				
				Unit unit=CreatorScript.createUnit(unitAbbrev,lanId);
				Unit unitContributor=CreatorScript.createUnit(unitContributorAbbrev,lanId);

				String propertyDescriptionDB=DevQsarConstants.getPropertyDescription(propertyNameDB);

				//			System.out.println(propertyName+"\t"+datasetName+"\t"+unitName+"\t"+unitContributorName);
				//			System.out.println(propertyName+"\t"+propertyDescriptionDB);
				//			System.out.println(propertyName+"\t"+unitName+"\t"+unitContributorName);

				//			
				Property property=CreatorScript.createProperty(propertyNameDB, propertyDescriptionDB,lanId);
				String datasetDescription=getDatasetDescription(propertyNameDB);
				//			System.out.println(datasetName+"\t"+datasetDescription);

				String dsstoxMappingStrategy="CASRN";

				Dataset dataset=new Dataset(datasetName, datasetDescription, property, unit, unitContributor,
						dsstoxMappingStrategy, lanId);

				CreatorScript.createDataset(dataset);

//				System.out.println(Utilities.gson.toJson(dataset));


			}

		}

		TreeMap<String, Model> createModels(HashMap<String, Method> hmMethods) {
			TreeMap<String,Model> hmModels=CreatorScript.getModelsMap();

			String sourceName=getSoftwareName();

			SourceService ss=new SourceServiceImpl();
			Source source=ss.findByName(sourceName);


			for (String propertyName:propertyNamesTestReports) {
				String descriptorSetName=getSoftwareName();

				String splittingName="TEST";
				String propertyNameDB=TESTConstants.getPropertyNameDB(propertyName);
				String datasetName=getDatasetName(propertyNameDB);

				String methodName=null;
				if (propertyNameDB.equals(DevQsarConstants.DEVELOPMENTAL_TOXICITY) || propertyNameDB.equals(DevQsarConstants.MUTAGENICITY) || propertyNameDB.equals(DevQsarConstants.ESTROGEN_RECEPTOR_BINDING)) {
					methodName="consensus_classifier";
				} else {
					methodName="consensus_regressor";
				}

				String modelName=getModelName(propertyNameDB); 

				//			System.out.println(modelName);

				if(hmModels.containsKey(modelName)) continue;

				Model model=new Model(modelName, hmMethods.get(methodName), null,descriptorSetName, datasetName, splittingName, source,lanId);
				model=CreatorScript.createModel(model);

				//			System.out.println(modelName+"\t"+model.getName());
				hmModels.put(modelName, model);
			}
			return hmModels;
		}

		String getDatasetName(String propertyNameDB) {
			return getModelName(propertyNameDB);
		}

		String getModelName(String propertyNameDB) {
			return propertyNameDB+" "+getSoftwareName();
		}
		
		
		
		
		public Hashtable<String, Long> getModelNameToModelID_Hashtable() {
			
//			String jsonString = "[{\"name\": \"Ames Mutagenicity TEST5.1.3\", \"id\": 229},"
//	                + "{\"name\": \"Estrogen receptor relative binding affinity TEST5.1.3\", \"id\": 231},"
//	                + "{\"name\": \"Vapor pressure TEST5.1.3\", \"id\": 235},"
//	                + "{\"name\": \"Melting point TEST5.1.3\", \"id\": 233},"
//	                + "{\"name\": \"Boiling point TEST5.1.3\", \"id\": 232},"
//	                + "{\"name\": \"Flash point TEST5.1.3\", \"id\": 234},"
//	                + "{\"name\": \"Density TEST5.1.3\", \"id\": 236},"
//	                + "{\"name\": \"48 hour Tetrahymena pyriformis IGC50 TEST5.1.3\", \"id\": 225},"
//	                + "{\"name\": \"Bioconcentration factor TEST5.1.3\", \"id\": 227},"
//	                + "{\"name\": \"Water solubility TEST5.1.3\", \"id\": 240},"
//	                + "{\"name\": \"Oral rat LD50 TEST5.1.3\", \"id\": 226},"
//	                + "{\"name\": \"Viscosity TEST5.1.3\", \"id\": 239},"
//	                + "{\"name\": \"Estrogen receptor binding TEST5.1.3\", \"id\": 230},"
//	                + "{\"name\": \"96 hour fathead minnow LC50 TEST5.1.3\", \"id\": 223},"
//	                + "{\"name\": \"Thermal conductivity TEST5.1.3\", \"id\": 238},"
//	                + "{\"name\": \"Developmental toxicity TEST5.1.3\", \"id\": 228},"
//	                + "{\"name\": \"48 hour Daphnia magna LC50 TEST5.1.3\", \"id\": 224},"
//	                + "{\"name\": \"Surface tension TEST5.1.3\", \"id\": 237}]";
			
//	        Gson gson = new Gson();
//	        Type listType = new TypeToken<List<Model>>(){}.getType();
//	        List<Model> testList = gson.fromJson(jsonString, listType);
//
//	        Hashtable<String, Long> hashtable = new Hashtable<>();
//	        for (Model model : testList) {
//	            hashtable.put(model.getName(), model.getId());
//	        }
			
			String sql="select m.name,m.id from qsar_models.models m\r\n"
					+ "			join qsar_models.sources s on m.fk_source_id = s.id\r\n"
					+ "			where s.name='TEST5.1.3';";
			
			ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
	        Hashtable<String, Long> htModelNameToID = new Hashtable<>();
			
			try {
				while (rs.next()) {
					String name=rs.getString(1);
					long id=rs.getLong(2);
					htModelNameToID.put(name, id);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
	        return htModelNameToID;
		}
		
		
		
		String getModelNameCCD(String propertyNameDB) {
			
			
//			TESTConstants.getprop
			
			return "";
			
			
		}

		Model getModel(String propertyName,TreeMap <String,Model>mapModels) {
			String modelName=getModelName(propertyName);
			return mapModels.get(modelName);
		}


		String getDatasetDescription(String propertyNameDB) {


			if (propertyNameDB.equals(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50)) {
				return "96 hour fathead minnow LC50 data compiled from ECOTOX for the TEST software";
			} else if (propertyNameDB.equals(DevQsarConstants.FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50)) {
				return("48 hour Daphnia magna LC50 data compiled from ECOTOX for the TEST software");
			} else if (propertyNameDB.equals(DevQsarConstants.FORTY_EIGHT_HR_TETRAHYMENA_PYRIFORMIS_IGC50)) {
				return("48 hour T. pyriformis IGC50 data compiled from Schultz et al for the TEST software");
			} else if (propertyNameDB.equals(DevQsarConstants.ORAL_RAT_LD50)) {
				return("Oral rat LD50 data compiled from ChemIDplus for the TEST software");
			} else if (propertyNameDB.equals(DevQsarConstants.THERMAL_CONDUCTIVITY)) {
				return "Thermal conductivity data compiled from Jamieson and Vargaftik for the TEST software";
			} else if (propertyNameDB.equals(DevQsarConstants.SURFACE_TENSION)) {
				return "Surface tension data compiled from Jaspar for the TEST software";
			} else if (propertyNameDB.equals(DevQsarConstants.VISCOSITY)) {
				return "Viscosity data compiled from Viswanath and Riddick for the TEST software";
			} else if (propertyNameDB.equals(DevQsarConstants.BOILING_POINT)) { 
				return "Boiling point data compiled from EPISUITE data for the TEST software"; 
			} else if (propertyNameDB.equals(DevQsarConstants.VAPOR_PRESSURE)) { 
				return "Vapor pressure data compiled from EPISUITE data for the TEST software"; 
			} else if (propertyNameDB.equals(DevQsarConstants.WATER_SOLUBILITY)) { 
				return "Water solubility data compiled from EPISUITE data for the TEST software"; 
			} else if (propertyNameDB.equals(DevQsarConstants.MELTING_POINT)) { 
				return "Melting point data compiled from EPISUITE data for the TEST software"; 
			} else if (propertyNameDB.equals(DevQsarConstants.DENSITY)) { 
				return "Density data compiled from LookChem.com for the TEST software"; 
			} else if (propertyNameDB.equals(DevQsarConstants.FLASH_POINT)) { 
				return "Flash point data compiled from LookChem.com for the TEST software"; 
			} else if (propertyNameDB.equals(DevQsarConstants.BCF)) {
				return("Bioconcentration factor data compiled from literature sources for the TEST software");//TODO doesnt exist			
			} else if (propertyNameDB.equals(DevQsarConstants.DEVELOPMENTAL_TOXICITY)) {
				return("Developmental toxicity data compiled from Arena et al for the TEST software");//TODO doesnt exist
			} else if (propertyNameDB.equals(DevQsarConstants.AMES_MUTAGENICITY)) {
				return("Ames mutagenicity data  compiled from Hansen et al for the TEST software");//TODO doesnt exist

			} else  {
				return propertyNameDB;
			} 


		}

		//	String getFieldValue(String fieldName)  {
		//		
		//		try {
		//			DevQsarConstants d=new DevQsarConstants();
		//			
		//			Field myField = d.getClass().getField(fieldName);
		//			
		//			return (String)myField.get(d);
		//
		//		
		//		} catch (Exception e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		} 
		//		
		//		return "";
		//		
		//	}

		String getSoftwareName() {
			return "TEST"+version;
		}

		

		void createStatistics(String endpoint,Model model,boolean postToDB) {

			String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);

			Hashtable<String, List<ModelPrediction>>htDatasetPredictions=GetTrainingTestSetPredictions.getPredictionHashtable();
			
			StatisticsCalculator sc=new StatisticsCalculator();
			HashMap<String, Double> allStats=sc.getStatistics(endpoint,TESTConstants.ChoiceConsensus, htDatasetPredictions.get(abbrev));

//			System.out.println(Utilities.gson.toJson(allStats)+"\n");
			
			
			//			if(abbrev.equals("LC50")) {
			//				System.out.println("");
			//				for (ModelPrediction mp:mpsTest) {
			//					System.out.println(mp.id+"\t"+mp.exp+"\t"+mp.pred);
			//				}
			//				System.out.println("");
			//			}

			mb.PostModelStatistics(allStats, model, postToDB);


		}


		private void uploadPredictionChart(long modelId, String propertyName,String unitName, List<ModelPrediction>mps)  {

			QsarModelsScript qms=new QsarModelsScript("tmarti02");

			try {

				String axistitle=propertyName+" "+unitName;

				String xtitle="Exp. "+axistitle;
				String ytitle="Pred. "+axistitle;
				//				String title="Test set predictions for the "+methodColumnName+" method";
				String title="External prediction results";

				List<Double>exps=new ArrayList<>();
				List<Double>preds=new ArrayList<>();

				for (ModelPrediction mp:mps) {
					if(mp.exp==null || mp.pred==null) continue;
					exps.add(mp.exp);
					preds.add(mp.pred);
				}

				double []x=new double[exps.size()];
				double []y=new double[exps.size()];

				for(int i=0;i<exps.size();i++) {
					x[i]=exps.get(i);
					y[i]=preds.get(i);
				}

				//				fraChart.scalingFactor=3;

				fraChart fc = new fraChart(x,y,title,xtitle,ytitle);

				fc.jlChart.doDrawLegend=true;

				//				fc.jlChart.fontGridLines=new Font( "Arial", Font.PLAIN, 18);
				//				fc.jlChart.fontTitle=new Font( "Arial", Font.BOLD, 20);
				//				fc.jlChart.fontLegend = new Font("Arial", Font.PLAIN, 18);

				fc.WriteImageToFile("data\\TEST5.1.3\\reports\\plots\\"+propertyName+".png");
				qms.uploadModelFile(modelId, 3L, fc.jlChart.getChartBytes());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		/**
		 * Store the statistics for the OPERA models
		 * Values taken from the QMRFs
		 * 
		 * @param mapModels
		 */
		private void createStatistics(TreeMap <String,Model>mapModels,boolean postToDB) {
			int i=0;
			for (String propertyName:propertyNamesTestReports) {
				String name=propertyName.replace("25?C","25°C");
				String abbrev=TESTConstants.getAbbrevEndpoint(name);
				String propertyNameDB=TESTConstants.getPropertyNameDB(propertyName);
				Model model=getModel(propertyNameDB, mapModels);
				System.out.println(++i+"\t"+name+"\t"+abbrev+"\t"+model.getId());
				createStatistics(propertyName, model,postToDB);
				//				if(true)break;
			}
		}

		public class ConsensusModelPredictions {
			List<ModelPrediction>mpsTest;
			List<ModelPrediction>mpsTraining;
			String propertyName;
			String units;
			public String modelName;
		}


		public void createPlotJsons() {

//			QsarModelsScript qms=new QsarModelsScript("tmarti02");
//
//			int i=0;
//			int minPredCount=2;
//
//			System.out.print("Getting maps...");
//			TreeMap<String, Model>mapModels=CreatorScript.getModelsMap();
//			TreeMap<String, Dataset>mapDatasets=CreatorScript.getDatasetsMap();
//			System.out.println("done\n");
//
//			for (String propertyNameTestReport:propertyNamesTestReports) {
//
//				String name=propertyNameTestReport.replace("25?C","25°C");
//				String abbrev=TESTConstants.getAbbrevEndpoint(name);
//				String propertyNameDB=TESTConstants.getPropertyNameDB(propertyNameTestReport);
//
//				Model model=getModel(propertyNameDB, mapModels);
//
//				String unitName=mapDatasets.get(model.getDatasetName()).getUnit().getAbbreviation_ccd();
//				if(unitName.contains("Binary"))continue;
//
//				Dataset dataset=mapDatasets.get(model.getDatasetName());
//				String propertyNameCCD=dataset.getProperty().getName_ccd();
//
//				System.out.println(++i+"\t"+name+"\t"+abbrev+"\t"+model.getId());
//
////				List<ModelPrediction>mpsTest=getModelPredictionsConsensus(abbrev, "test",minPredCount);
////				List<ModelPrediction>mpsTraining=getModelPredictionsConsensus(abbrev, "training",minPredCount);
//				//Create charts using Java:
//				//				uploadPredictionChart(model.getId(), propertyNameDB, unitName, mpsTest);
//
//				//Following is used by "models/make_test_plots.py" python model building project to make plots:
////				saveConsensusPredictionsToJson(propertyNameCCD,model.getName(),mpsTest, mpsTraining, unitName);
//				//				if(true)break;
//			}
			
			TreeMap<String, Model>mapModels=CreatorScript.getModelsMap();
			TreeMap<String, Dataset>mapDatasets=CreatorScript.getDatasetsMap();
			
			for(String endpoint:RunFromSmiles.allEndpoints) {
				
				String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);
				String propertyNameDB=TESTConstants.getPropertyNameDB(endpoint);
				
				Model model=getModel(propertyNameDB, mapModels);
				
				String unitName=mapDatasets.get(model.getDatasetName()).getUnit().getAbbreviation_ccd();
				if(unitName.contains("Binary"))continue;
				
				Dataset dataset=mapDatasets.get(model.getDatasetName());
				String propertyNameCCD=dataset.getProperty().getName_ccd();

				
//				String jsonFilePath="gov"+File.separator+"epa"+File.separator+"webtest"+File.separator+abbrev+File.separator+abbrev+"_predictions.json";
				String jsonFilePath="gov/epa/webtest/"+abbrev+"/"+abbrev+"_predictions.json";

				List<ModelPrediction>mps=GetTrainingTestSetPredictions.getModelPredictions(jsonFilePath, true);

				List<ModelPrediction>mpsTrain=new ArrayList<>();
				List<ModelPrediction>mpsTest=new ArrayList<>();
				
				for(ModelPrediction mp:mps) {
					if(!mp.methodAbbrev.equals("consensus")) continue;
					if(mp.split==0)mpsTrain.add(mp);
					else if(mp.split==1) mpsTest.add(mp);
				}
				
				saveConsensusPredictionsToJson(propertyNameCCD,model.getName(),mpsTest, mpsTrain, unitName);
				
			}
			

		}

		//		public void uploadPlotsOld() {
		//
		//			QsarModelsScript qms=new QsarModelsScript("tmarti02");
		//			
		//			int i=0;
		//			int minPredCount=2;
		//			
		//			System.out.print("Getting maps...");
		//			TreeMap<String, Model>mapModels=CreatorScript.getModelsMap();
		//			TreeMap<String, Dataset>mapDatasets=CreatorScript.getDatasetsMap();
		//			System.out.println("done\n");
		//			
		//			for (String propertyName:propertyNamesTestReports) {
		//				String name=propertyName.replace("25?C","25°C");
		//				String abbrev=TESTConstants.getAbbrevEndpoint(name);
		//				String propertyNameDB=getPropertyNameDB(propertyName);
		//				Model model=getModel(propertyNameDB, mapModels);
		//				String unitName=mapDatasets.get(model.getDatasetName()).getUnit().getAbbreviation_ccd();
		//				if(unitName.contains("Binary"))continue;
		//
		//				System.out.println(++i+"\t"+name+"\t"+abbrev+"\t"+model.getId());
		//
		//				String folder="data\\TEST5.1.3\\reports\\plots\\";
		//				String filePathScatter=folder+propertyNameDB+"_consensus_preds_scatter_plot.png";
		//				String filePathHistogram=folder+propertyNameDB+"_consensus_preds_histogram.png";
		//				
		//				try {
		//					qms.uploadModelFile(model.getId(), 4L, filePathHistogram);
		//					qms.uploadModelFile(model.getId(), 3L, filePathScatter);
		//				} catch (Exception e) {
		//					e.printStackTrace();
		//				}
		//				
		////				if(true)break;
		//			}
		//		}



		private void saveConsensusPredictionsToJson(String propertyName, String modelName, List<ModelPrediction> mpsTest,
				List<ModelPrediction> mpsTraining, String unitName) {
			String folder="data\\TEST5.1.3\\reports\\plots\\";
			
			File Folder=new File(folder);
			if(!Folder.exists()) Folder.mkdirs();
			
			ConsensusModelPredictions cmp=new ConsensusModelPredictions();
			cmp.mpsTest=mpsTest;
			cmp.mpsTraining=mpsTraining;
			cmp.units=unitName;
			cmp.propertyName=propertyName;
			cmp.modelName=modelName;

			try {

				FileWriter fw = new FileWriter(folder+modelName+".json");
				fw.write(Utilities.gson.toJson(cmp));
				fw.flush();
				fw.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


//		private List<ModelPrediction>getModelPredictionsConsensus(String endpointAbbrev, String set, int minPredCount) {
//
//			List<ModelPrediction>mps=new ArrayList<>();
//			try {
//
//				InputStream ins = this.getClass().getClassLoader()
//						.getResourceAsStream(endpointAbbrev+"/"+endpointAbbrev+" "+set+" set predictions.txt");
//
//				BufferedReader br=new BufferedReader(new InputStreamReader(ins));
//				String header=br.readLine();
//				List<String>headers=Arrays.asList(header.split("\t"));
//
//				String endpoint=TESTConstants.getFullEndpoint(endpointAbbrev);
//				List<String>consensusMethods=new ArrayList<>();
//				consensusMethods.add("Hierarchical clustering");
//				if(TESTConstants.haveSingleModelMethod(endpoint)) consensusMethods.add("Single model");
//				if(TESTConstants.haveGroupContributionMethod(endpoint)) consensusMethods.add("Group contribution");
//				consensusMethods.add("Nearest neighbor");
//
//				//				System.out.println(header);
//
//				while (true) {
//
//					String Line=br.readLine();
//					if(Line==null || Line.isBlank()) break;
//					String [] strVals=Line.split("\t");
//					List<Double>consensusVals=new ArrayList<>();
//
//					int splitNum=-1;
//					if(set.equals("test")) splitNum=1;
//					if(set.equals("training")) splitNum=0;
//
//					String cas=strVals[headers.indexOf("CAS")];
//					double dexpval=Double.parseDouble(strVals[headers.indexOf("expToxicValue")]);
//
//					for (String method:consensusMethods) {
//						double dval=Double.parseDouble(strVals[headers.indexOf(method)]);
//						if(dval==-9999) continue;
//						consensusVals.add(dval);
//					}
//
//					Double dpredval=null;
//
//					if(consensusVals.size()>=minPredCount) {//need 2 or more or unreliable (AD)
//						dpredval=0.0;
//						for(Double val:consensusVals) dpredval+=val;
//						dpredval/=consensusVals.size();
//					}
//					ModelPrediction mp=new ModelPrediction(cas,dexpval,dpredval,splitNum);
//					mps.add(mp);
//					//					System.out.println(strVals[0]+"\t"+consensusVals);
//				}
//
//				//				System.out.println(Utilities.gson.toJson(mps));
//
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
//
//			return mps;
//
//		}

	}


	/**
	 * TODO move some of these to DatabaseUtilities
	 */
	class TESTUtilities {

		/**
		 * Gets records for a specific dtxcid in a json file
		 * 
		 * @param filepathJson
		 * @param filepathOutput
		 * @param dtxcid
		 */
		void extractRecords(String filepathJson,String filepathOutput,String id) {

			try {


				BufferedReader br=new BufferedReader(new FileReader(filepathJson));
				FileWriter fw=new FileWriter(filepathOutput);

				int count=0;

				Gson gson=new Gson();


				//			for (String line:lines) {
				while (true) {
					String line=br.readLine();
					if(line==null) break;
					count++;
					PredictionResults pr=Utilities.gson.fromJson(line,PredictionResults.class);

					if(id.contains("DTXSID") && pr.getDTXSID().equals(id)) {
						System.out.println(Utilities.gson.toJson(pr));
						fw.write(gson.toJson(pr)+"\r\n");
						fw.flush();
					} else	if(id.contains("DTXCID") && pr.getDTXCID().equals(id)) {
						System.out.println(Utilities.gson.toJson(pr));
						fw.write(gson.toJson(pr)+"\r\n");
						fw.flush();
					}				

				}
				fw.close();
				br.close();


			} catch (Exception e) {
				e.printStackTrace();
			} 

		}

		/**
		 * Fixes issue where the DTXSID was stored in the cas field for the test chemical in the similar chemicals tables
		 */
		void fixReportsInDB() {

			//		String sql="select file from qsar_models.pred"

			int batchSize=1000;
			int batch=0;
			int counter=0;

			List<PredictionReport>updatedReports=new ArrayList<>();

			while (true) {

				List<PredictionReport>reports=predictionReportService.getNonUpdatedPredictionReportsBySQL(batch*batchSize, batchSize);

				if (reports.size()==0) break;

				Gson gson=new Gson();

				for (PredictionReport report:reports) {

					counter++;

					if(counter%1000==0) System.out.println(counter);

					String strPredictionResults=new String(report.getFileJson());

					if (!strPredictionResults.contains("(test chemical)")) {
						//					System.out.println("Doesnt have \"(test chemical)\", skipping");
						continue;
					} 


					PredictionResults pr=gson.fromJson(strPredictionResults, PredictionResults.class);

					fixPredictionResults(pr);

					//				if (counter==1) {
					//					System.out.println(report.getId());
					//					System.out.println(Utilities.gson.toJson(pr));
					//					String strPredictionResults2=gson.toJson(pr);
					//					report.setFile(strPredictionResults2.getBytes());
					////					this.predictionReportService.updateSQL(report);
					//					updatedReports.add(report);
					//					if(updatedReports.size()==batchSize) {
					//						this.predictionReportService.updateSQL(updatedReports);
					//						updatedReports.clear();
					//					}
					////					if(true) break;
					////					this.predictionReportService.create(report)
					//				}

					String strPredictionResults2=gson.toJson(pr);
					report.setFileJson(strPredictionResults2.getBytes());

					//TODO update html file too

					report.setUpdatedBy(lanId);
					updatedReports.add(report);

					//				if (counter==1) {
					//					System.out.println(report.getId());
					//					this.predictionReportService.updateSQL(report);
					//				}


					if(updatedReports.size()==batchSize) {
						predictionReportService.updateSQL(updatedReports);
						updatedReports.clear();
					}

					//				if(true) break;

				}//end loop over reports from sql query

				batch++;//update which batch to return in main sql query


				//			if(true) break;

			}//end while true

			predictionReportService.updateSQL(updatedReports);//update any remaining reports
		}

		/**
		 * Fixes error where CAS was set to the SID for the test chemical in the similar chemicals table
		 * 
		 * @param pr
		 */
		private void fixPredictionResults(PredictionResults pr) {

			if (pr.getSimilarChemicals().size()==0) {
				//			System.out.println(Utilities.gson.toJson(pr));
				return;
			}

			Vector<SimilarChemical>similarChemicals0=pr.getSimilarChemicals().get(0).getSimilarChemicalsList();
			Vector<SimilarChemical>similarChemicals1=pr.getSimilarChemicals().get(1).getSimilarChemicalsList();

			if (pr.getCAS()==null) {
				//			System.out.println("CAS is null for "+pr.getDTXSID());
			} else {
				if (!pr.getCAS().contains("-")) {
					//				System.out.println("CAS="+pr.getCAS());
				}
			}

			if(similarChemicals0.size()>0) {
				SimilarChemical sc0_0=similarChemicals0.get(0);
				if(pr.getCAS()!=null) sc0_0.setCAS(pr.getCAS());
				else sc0_0.setCAS("N/A");
			}

			if(similarChemicals1.size()>0) {
				SimilarChemical sc1_0=similarChemicals1.get(0);
				if(pr.getCAS()!=null) sc1_0.setCAS(pr.getCAS());
				else sc1_0.setCAS("N/A");
			}
		}

		private void fixPredictionResults(PredictionResults pr,DsstoxRecord dr) {

			if(dr==null) {
				//				System.out.println("No dsstox record for "+pr.getCAS());
				return;
			}

			if (pr.getSimilarChemicals().size()==0) {
				//			System.out.println(Utilities.gson.toJson(pr));
				return;
			}


			if (pr.getCAS()==null) {
				//			System.out.println("CAS is null for "+pr.getDTXSID());
			} else {
				if (!pr.getCAS().contains("-")) {
					//				System.out.println("CAS="+pr.getCAS());
				}
			}

			Vector<SimilarChemical>similarChemicals0=pr.getSimilarChemicals().get(0).getSimilarChemicalsList();

			if(similarChemicals0.size()>0) {
				SimilarChemical sc=similarChemicals0.get(0);
				if(dr.getCasrn()!=null)	sc.setCAS(dr.getCasrn());
				else sc.setCAS("N/A");
			}

			Vector<SimilarChemical>similarChemicals1=pr.getSimilarChemicals().get(1).getSimilarChemicalsList();

			if(similarChemicals1.size()>0) {
				SimilarChemical sc=similarChemicals1.get(0);
				if(dr.getCasrn()!=null)	sc.setCAS(dr.getCasrn());
				else sc.setCAS("N/A");
			}
		}

		private void fixReportInDatabase() {
			String dtxsid="DTXSID80177704";//N-Methyl-N'-(4-methylphenyl)-N-nitrosourea
			String modelSource="TEST5.1.3";
			String propertyName=DevQsarConstants.WATER_SOLUBILITY;


			String report=predictionReportService.getReport(dtxsid, propertyName,modelSource);
			//		String report=pds.predictionReportService.getReport(dtxsid, "Boiling point TEST5.1.3");
			PredictionResults pr=Utilities.gson.fromJson(report, PredictionResults.class);

			fixPredictionResults(pr);

			//		System.out.println(Utilities.gson.toJson(similarChemicals0));
			//		System.out.println(Utilities.gson.toJson(similarChemicals1));
			System.out.println(Utilities.gson.toJson(pr));

			//TODO update it in db

		}

		/**
		 * TODO move to TEST_Report_API class
		 */
		void checkReportsForChemical() {

			String dtxsid="DTXSID00223252";
			String sql="select pr.id, pr.file from qsar_models.predictions_dashboard pd\n"
					+ "join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id=dr.id\n"
					+ "join qsar_models.models m on m.id=pd.fk_model_id\n"
					+ "join qsar_models.prediction_reports pr on pd.id = pr.fk_prediction_dashboard_id\n"
					+ "where dr.dtxsid='"+dtxsid+"' and m.\"source\" ='TEST5.1.3'";


			Connection conn=SqlUtilities.getConnectionPostgres();

			try {

				ResultSet rs=SqlUtilities.runSQL2(conn, sql);

				while (rs.next()) {

					Long id=rs.getLong(1);
					String strPredictionReport=new String(rs.getBytes(2));

					if (strPredictionReport.contains("test chemical")) {
						JsonObject jo=Utilities.gson.fromJson(strPredictionReport, JsonObject.class);
						System.out.println(id+"\t"+Utilities.gson.toJson(jo));
						break;
					}

				}


			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

		private HashSet<String> getPredictionDashboardKeysMissingReports() throws SQLException {
			HashSet<String> pd_keys=new HashSet<>();


			String sql="select canon_qsar_smiles, fk_dsstox_records_id ,fk_model_id from qsar_models.predictions_dashboard pd\n"+
					"left join qsar_models.prediction_reports pr on pd.id = pr.fk_prediction_dashboard_id\n"+
					"where fk_model_id>=223 and fk_model_id<=240 and pr.file is null";

			ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);


			while (rs.next()) {
				String canon_qsar_smiles=rs.getString(1);
				Long fk_dsstox_records_id=rs.getLong(2);
				String fk_model_id=rs.getString(3);
				String key=canon_qsar_smiles+"\t"+fk_dsstox_records_id+"\t"+fk_model_id;

				//				System.out.println(key);
				pd_keys.add(key);
			}
			return pd_keys;
		}

		void uploadMissingReports(String filepathJson) {

			try {

				HashSet<String> pd_keys = getPredictionDashboardKeysMissingReports();
				System.out.println("Number of predictions missing a report="+pd_keys.size());

				BufferedReader br=new BufferedReader(new FileReader(filepathJson));

				int countOK=0;

				List<PredictionReport>missingReports=new ArrayList<>();
				PredictionDashboardTableMaps tableMaps=new PredictionDashboardTableMaps(PredictionDashboardTableMaps.fileJsonDsstoxRecords2024_11_12,PredictionDashboardTableMaps.fileJsonOtherCAS2024_11_12);//creates lookup maps for database objects so dont have to keep query the database

				int counter=0;

				while (true) {


					String strPredictionResults=br.readLine();
					if(strPredictionResults==null) break;

					counter++;
					if(counter%1000==0) System.out.println(counter);

					PredictionResults predictionResults=Utilities.gson.fromJson(strPredictionResults,PredictionResults.class);

//					long t3=System.currentTimeMillis();

					//				if(!predictionResults.getDTXSID().equals("DTXSID10976888")) continue;

					PredictionDashboard pd=converter.convertPredictionResultsToPredictionDashboard(predictionResults,tableMaps,true);

					//Using key of all the main variables is faster than trying to do a database look up for each predictionDashboard record:
					//				String pd_id=predictionReportService.getPredictionDashboardId(SqlUtilities.getConnectionPostgres(), pd);
					String pd_key=pd.getKey();

					//				String strSQL="select id from qsar_models.prediction_reports pr where pr.fk_prediction_dashboard_id="+pd_id;
					//				String pr_id=SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), strSQL);

					if(!pd_keys.contains(pd_key)) {
						PredictionReport predictionReport=new PredictionReport(pd, strPredictionResults,null, lanId);
						missingReports.add(predictionReport);
						System.out.println(missingReports.size()+" Missing\tpd_key="+pd_key);	
					}else {
						countOK++;
						//					System.out.println(countOK+" In DB: pd_id="+ pd_key);
					}

					if (missingReports.size()==pd_keys.size()) {
						System.out.println("Exiting loop, found all the missing reports");
						break;
					}
				}

				System.out.println("missingReports.size()="+missingReports.size());

				predictionReportService.createSQL(missingReports);
				
				br.close();

			} catch (Exception e) {
				e.printStackTrace();
			} 

		}

		private void lookAtValuesInDatabase() {
			//		String dtxsid="DTXSID80177704";//N-Methyl-N'-(4-methylphenyl)-N-nitrosourea
			String dtxsid="DTXSID40177523";

			String modelSource="TEST5.1.3";
			//		String propertyName=DevQsarConstants.WATER_SOLUBILITY;

			File folder=new File("reports/"+dtxsid);
			folder.mkdirs();


			for (String propertyName: DevQsarConstants.TEST_SOFTWARE_PROPERTIES) {

				String report=predictionReportService.getReport(dtxsid, propertyName,modelSource);
				//			String report=pds.predictionReportService.getReport(dtxsid, "Boiling point TEST5.1.3");
				PredictionResults pr=Utilities.gson.fromJson(report, PredictionResults.class);

				//			String report2=Utilities.gson.toJson(pr);

				//			System.out.println(Utilities.gson.toJson(pr));

				String json=predictionDashboardService.getPredictionDashboardAsJson(dtxsid, propertyName, modelSource);
				//			System.out.println(json);


				try {
					FileWriter fw = new FileWriter(folder.getAbsolutePath()+File.separator+propertyName+".json");
					fw.write(report);
					fw.flush();
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}


			}
		}

		/**
		 * 
		 * Looks at json and sees what sids didnt make it into the db
		 * 
		 * TODO this method needs to be updated based on latest schema for predictions_dashboard table
		 * 
		 * @param filepathJson
		 */
		void findMissingPredictions(String filepathJson) {

			try {

				BufferedReader br=new BufferedReader(new FileReader(filepathJson));
				HashSet<String> sidsJsonFile=new HashSet<>();

				while (true) {
					String strPredictionResults=br.readLine();
					if(strPredictionResults==null) break;

					PredictionResults predictionResults=Utilities.gson.fromJson(strPredictionResults,PredictionResults.class);

					if (!sidsJsonFile.contains(predictionResults.getDTXSID())) {
						sidsJsonFile.add(predictionResults.getDTXSID());	
						//					System.out.println(predictionResults.getDTXSID());
						//					if (sidsJsonFile.size()==100) break;

						if (sidsJsonFile.size()%1000==0) {
							System.out.println("\t"+sidsJsonFile.size());
						}

					}

				}
				br.close();


				System.out.println("Unique sids in json file="+sidsJsonFile.size());

				HashSet<String> sidsDB=new HashSet<>();

				String sql="select dtxsid from qsar_models.predictions_dashboard pd where fk_model_id=223";

				ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);

				while (rs.next()) {
					String sid=rs.getString(1);
					//				System.out.println(sid);
					sidsDB.add(sid);
				}

				int countMissing=0;
				for (String sidJsonFile:sidsJsonFile) {
					if(!sidsDB.contains(sidJsonFile)) {
						countMissing++;
					}
				}
				System.out.println("Unique sids in db for TEST models="+sidsDB.size());

				System.out.println("countMissing="+countMissing);


			} catch (Exception e) {
				e.printStackTrace();
			} 

		}

		/* Possible prediction errors:
		"The consensus prediction for this chemical is considered unreliable since only one prediction can only be made"
		"No prediction could be made due to applicability domain violation"
		"FindPaths"
		"FindRings"
		"Only one nonhydrogen atom"
		"Molecule does not contain carbon"
		"Molecule contains unsupported element"
		"Multiple molecules"
		 */
		void lookAtSDEJsonAsHTMLReport() {
			String folder="data\\TEST1.0\\";
			String filepath=folder+"predictionResults.json.gz";

			CompareStandaloneToSDE c=new CompareStandaloneToSDE();

			try {

				InputStream fileStream = new FileInputStream(filepath);
				InputStream gzipStream = new GZIPInputStream(fileStream);
				Reader decoder = new InputStreamReader(gzipStream,  StandardCharsets.UTF_8);
				BufferedReader br = new BufferedReader(decoder);

				List<String> errors=new ArrayList<>();

				int index=1383;

				for (int i=1;i<=5000;i++) {

					String Line=br.readLine();
					PredictionResults pr=Utilities.gson.fromJson(Line, PredictionResults.class);

					if (pr.getError()!=null) {					
						if(!errors.contains(pr.getError())){
							errors.add(pr.getError());
						}
						//					System.out.println(pr.getDTXSID()+"\t"+pr.getEndpoint()+"\t"+pr.getError());
					}
					//				System.out.println(Utilities.gson.toJson(pr));

					//				if(i%1000==0) System.out.println(i);

					//				if(pr.getError()==null || pr.getError().isBlank()) {
					//					System.out.println(i+"\t"+pr.getDTXSID()+"\t"+pr.getEndpoint()+"\t"+pr.getPredictionResultsPrimaryTable().getPredToxValMass());
					//					System.out.println(Utilities.gson.toJson(pr));
					//					break;
					//				}

					c.fixPredictionResultsSDE(pr);

					if(i==index) {
						System.out.println(Utilities.gson.toJson(pr));
						String fileName="results.html";
						HTMLReportCreatorTEST.displayHTMLReport(pr, fileName, folder);
					}

				}

				for (String error:errors) {
					System.out.println(error);
				}


				br.close();


			} catch (Exception e) {
				e.printStackTrace();
			}
		}

//		private void fixPredictionResultsSDE(PredictionResults pr) {
//
//			for(PredictionIndividualMethod pred:pr.getIndividualPredictionsForConsensus().getConsensusPredictions()) {
//				pred.setMethod(TESTConstants.getFullMethod(pred.getMethod()));
//			}
//
//			pr.setEndpoint(TESTConstants.getFullEndpoint(pr.getEndpoint()));
//
//		}

		public void viewReportsFromDatabase(String id) {

			String folder="data\\opera\\reports";

			//TODO just add list of TEST endpoints to DevQsarConstants similar to OPERA
			List<String> propertyNames=TESTConstants.getFullEndpoints(null);
			List<String> propertyNamesDB=new ArrayList<>();

			for(String propertyName:propertyNames) {
				propertyNamesDB.add(TESTConstants.getPropertyNameDB(propertyName));
			}

			for (String propertyName:propertyNamesDB) {

				String modelName=initializeDB.getModelName(propertyName);
				//			System.out.println(modelName);

				PredictionResults pr=getPredictionResultsFromPredictionReport(id,modelName);

				if(pr==null) {
					System.out.println("No report for "+propertyName);
					continue;
				}

				if(propertyName.equals(DevQsarConstants.WATER_SOLUBILITY)) {
					System.out.println(Utilities.gson.toJson(pr));
				}

				String filename=pr.getDTXCID()+"_"+pr.getEndpoint()+".html";
				HTMLReportCreatorTEST.displayHTMLReport(pr, filename, folder);
			}

		}

		PredictionResults getPredictionResultsFromPredictionReport(String dtxid,String modelName) {

			String idCol="dtxcid";
			if (dtxid.contains("SID")) idCol="dtxsid";


			String sql="select file from qsar_models.prediction_reports pr\r\n"
					+ "join qsar_models.predictions_dashboard pd on pr.fk_predictions_dashboard_id = pd.id\r\n"
					+ "join qsar_models.models m on pd.fk_model_id = m.id\r\n"
					+ "join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id\r\n"
					+ "where dr."+idCol+"='"+dtxid+"' and dr.fk_dsstox_snapshot_id=1 and m.name='"+modelName+"';";

			try {
				Connection conn=SqlUtilities.getConnectionPostgres();

				ResultSet rs=SqlUtilities.runSQL2(conn, sql);

				if (rs.next()) {
					String json=new String(rs.getBytes(1));
					return Utilities.gson.fromJson(json,PredictionResults.class);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;

		}

		void testRetrievePredictionReport() {

			//To do it via sql:  select convert_from(file, 'ISO-8859-1') from qsar_models.prediction_reports r

			PredictionReport pr=predictionReportService.findByPredictionDashboardId(1406L);
			//		String strPredictionReport=pr.decompress(pr.getFile());
			//		String strPredictionReport=new String(pr.getFile(), StandardCharsets.ISO_8859_1);
			String strPredictionReport=new String(pr.getFileJson());

			System.out.println(strPredictionReport);

			PredictionResults predictionResults=Utilities.gson.fromJson(strPredictionReport, PredictionResults.class);
			System.out.println(Utilities.gson.toJson(predictionResults));
		}
	}




	/* Possible prediction errors:
    "Error processing record with CAS null, error=Timeout 120000 ms while generating paths for null."
    "The consensus prediction for this chemical is considered unreliable since only one prediction can only be made"
    "FindPaths"
    "Only one nonhydrogen atom"
    "FindRings"
    "Molecule does not contain carbon"
    "Molecule contains unsupported element"
    "No prediction could be made due to applicability domain violation"
    "Multiple molecules"
	 */
	void runFromDashboardJsonFileBatchPost(String filepathJson, boolean writeToDB, boolean skipER, 
			boolean fixReports, String dtxsid, boolean writeReportToHarddrive,boolean writePredictionReportJson) {

		try {

			System.out.println("Loading "+filepathJson);

			String snapshotName="DSSTOX Snapshot 11/12/2024";		
			DsstoxSnapshotServiceImpl snapshotService = new DsstoxSnapshotServiceImpl();
			DsstoxSnapshot snapshot = snapshotService.findByName(snapshotName);

			SourceService sourceService=new SourceServiceImpl();
			String sourceName="TEST" + version;
			Source source=sourceService.findByName(sourceName);


			BufferedReader br=new BufferedReader(new FileReader(filepathJson));

			int counter=0;
			int batchSize=16*100;
			//			int countToLoad=-1;
			//			int countToLoad=1;

			List<PredictionDashboard>predictionsDashboard=new ArrayList<>();
			//			List<PredictionReport>predictionReports=new ArrayList<>();

			//Get list of prediction dashboard keys already in the database:
//			HashSet<String> pd_keys=DatabaseUtilities.getLoadedKeys(source, snapshot);
			//			HashSet<String> pd_keys=new HashSet<>();//dont need because CONFLICT DO NOTHING in sql insert statement

			HashSet<String>cidsLoaded=DatabaseUtilities.getLoadedCIDsWithCount(source.getName(), 16);

			System.out.println("cidsLoaded.size()="+cidsLoaded.size());
			
			if(tableMaps==null)
				tableMaps=new PredictionDashboardTableMaps(PredictionDashboardTableMaps.fileJsonDsstoxRecords2024_11_12,PredictionDashboardTableMaps.fileJsonOtherCAS2024_11_12);//creates lookup maps for database objects so dont have to keep query the database

			boolean foundDtxsid=false;

			while (true) {
				//				System.out.println("start loop");

				String strPredictionResults=br.readLine();
				if(strPredictionResults==null) break;
				
				counter++;

				//	System.out.println(strPredictionResults);

				if(counter%batchSize==0) System.out.println(counter/16);

				long t1=System.currentTimeMillis();
				PredictionResults predictionResults=Utilities.gson.fromJson(strPredictionResults,PredictionResults.class);
				long t2=System.currentTimeMillis();
//				System.out.println("Time to parse json:"+(t2-t1)+"ms");
				
				if(cidsLoaded.contains(predictionResults.getDTXCID())) continue;
				
				if (skipER) {
					String propNameLC=predictionResults.getEndpoint().toLowerCase();
					if (propNameLC.contains("estrogen"))continue;
				}
				
				if(predictionResults.getPredictionResultsPrimaryTable().getExpCAS()!=null) {
					if(predictionResults.getEndpoint().equals(TESTConstants.ChoiceWaterSolubility)) {
						System.out.println(predictionResults.getDTXSID()+"\t"+predictionResults.getEndpoint()+"\tHas exp");
					}
				}

				fixUnitsBCF(predictionResults);
				
				//				if(!predictionResults.getError().isEmpty()) {
				//					System.out.println(predictionResults.getDTXCID()+"\t"+predictionResults.getSmiles());
				//					break;
				//				} else if(true) {
				////					System.out.println(predictionResults.getSmiles());
				//					continue;
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
				
				if(writeReportToHarddrive && writePredictionReportJson) {
					String folder = "data\\TEST5.1.3\\reports\\"+predictionResults.getDTXSID()+"\\";
					Utilities.saveJson(predictionResults, folder+predictionResults.getDTXSID()+"_"+predictionResults.getEndpoint()+".json");
				}


				if(fixReports) {
					DsstoxRecord dsstoxRecord=tableMaps.mapDsstoxRecordsByCID.get(predictionResults.getDTXCID());
					testUtilities.fixPredictionResults(predictionResults,dsstoxRecord);//fixes error where CAS was set to the SID for the test chemical in the similar chemicals table
				}
				
//				fixNearestNeighborAndConsensus(predictionResults);

				//System.out.println(Utilities.gson.toJson(predictionResults));
				
				

				long t3=System.currentTimeMillis();
				PredictionDashboard pd=converter.convertPredictionResultsToPredictionDashboard(predictionResults,tableMaps,writeReportToHarddrive);
				long t4=System.currentTimeMillis();
				

//				System.out.println("Time to do misc:"+(t3-t2)+"ms");
//				System.out.println("Time to convert to pd:"+(t4-t3)+"ms");
				
				if(pd==null) {
					//System.out.println(predictionResults.getDTXCID()+"\tMissing dsstox record");
					continue;
				}

				
				compareToAPI(pd,predictionResults);

				
				//				System.out.println(pd.toJson());

				predictionsDashboard.add(pd);

				if(predictionsDashboard.size()==batchSize) {
					//					System.out.println(counter);
					if(writeToDB) predictionDashboardService.createSQL(predictionsDashboard);
					predictionsDashboard.clear();
				}

				//				if(true) break;

			}
			br.close();

			System.out.println("exited main loop");
			System.out.println("remaining to load: "+predictionsDashboard.size());

			//Do what's left:
			if(writeToDB) predictionDashboardService.createSQL(predictionsDashboard);

		} catch (Exception e) {
			e.printStackTrace();
		} 

	}

	
	public static class TESTAPIResults{
	    public String uuid;
	    public long predictionTime;
	    public String software;
	    public String softwareVersion;
	    public String condition;
	    public ArrayList<PredictionAPI> predictions;
	    
		public static class PredictionAPI{
		    public String id;
		    public String smiles;
		    public String expValMolarLog;
		    public String expValMass;
		    public String predValMolarLog;
		    public String predValMass;
		    public String molarLogUnits;
		    public String message;
		    public String massUnits;
		    public String endpoint;
		    public String method;
		    public String dtxsid;
		    public String casrn;
		    public String preferredName;
		    public String inChICode;
		    public String inChIKey;
		    public String error;
		    public String errorCode;
		}

	}


	public static void compareToAPI(PredictionDashboard pd,PredictionResults pr) {

		
		if(!pr.getError().isBlank()) return;
 		
//		String datasetName=pd.getModel().getDatasetName();
//		Dataset dataset=tableMaps.mapDatasets.get(datasetName);
//		Property property=dataset.getProperty();


		String abbrev=TESTConstants.getAbbrevEndpoint(pr.getEndpoint());
		
		String smiles = pd.getDsstoxRecord().getSmiles();
		
		HttpResponse<String> response = Unirest.get("https://comptox.epa.gov/dashboard/web-test/"+abbrev)
				.queryString("smiles", smiles)
				.queryString("method", "Consensus")
				.asString();
		
		Gson gson=new Gson();
				
		TESTAPIResults tr=gson.fromJson(response.getBody(), TESTAPIResults.class);

		Hashtable<String,Double>htAPI=new Hashtable<>();
		
//		System.out.println(gson.toJson(tr.predictions));
		
		for (PredictionAPI pred:tr.predictions) {
//			System.out.println(smiles+"\t"+property.getName()+"\t"+pred.method+"\t"+ pred.predValMolarLog+"\t"+pred.predValMass);

			if (pr.isBinaryEndpoint()) {
				if(pred.message==null) continue;

				if(pred.message.contains("Positive")) {
					htAPI.put(pred.method, 1.0);						
				} else if(pred.message.contains("Negative")) {
					htAPI.put(pred.method, 0.0);		
				}
			} else if(pr.isLogMolarEndpoint()) {
				if(pred.predValMolarLog==null) continue;
				htAPI.put(pred.method, Double.parseDouble(pred.predValMolarLog));				
			} else {
				if(pred.predValMass==null) continue;
				htAPI.put(pred.method, Double.parseDouble(pred.predValMass));
			}
		}
		
		TEST_Report test_report=gson.fromJson(new String(pd.getPredictionReport().getFileJson()), TEST_Report.class);
		
		Hashtable<String,Double>htJson=new Hashtable<>();
		
		
//		{"consensus":"501.8","gc":"723.4","nn":"280.2"}
//		{"Consensus":"501.79","Nearest neighbor":"280.17","Hierarchical clustering":"N/A","Group contribution":"723.42"}

		if(test_report.modelResults.consensusPredictions==null) {
			System.out.println("Dont have predictions for "+pr.getDTXSID()+" for "+pr.getEndpoint());
			return;
		}
		
		for (gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.PredictionIndividualMethod pim: test_report.modelResults.consensusPredictions.predictionsIndividualMethod) {
			if(pim.predictedValue==null) continue;
			
			if(pim.method.equals("Consensus")) {
				htJson.put("consensus",pim.predictedValue);				
			} else if(pim.method.equals("Nearest neighbor")) {
				htJson.put("nn",pim.predictedValue);
			} else if(pim.method.equals("Hierarchical clustering")) {
				htJson.put("hc",pim.predictedValue);
			} else if(pim.method.equals("Group contribution")) {
				htJson.put("gc",pim.predictedValue);
			} else if(pim.method.equals("Single model")) {
				htJson.put("sm",pim.predictedValue);
			}
		}
		
		List<String>methods=Arrays.asList("consensus","nn","hc","sm","gc");
		
		boolean allMatch=true;
		
		for (String method:methods) {

			if((!htAPI.containsKey(method) && !htJson.containsKey(method))) {
				continue;
			}
			
			if((htAPI.containsKey(method) && !htJson.containsKey(method))) {
				System.out.println("missing pred for "+method);
				allMatch=false;
				break;
			}
				
			
			if(!htAPI.containsKey(method) && htJson.containsKey(method)) {
				System.out.println("missing pred for "+method);
				allMatch=false;
				break;
			}


			
			Double dpredAPI=htAPI.get(method);
			Double dpredJson=htJson.get(method);
			
//			double dpredAPI=Double.parseDouble(predAPI);
//			double dpredJson=Double.parseDouble(predJson);
			
			Double diff=null;
			
//			if(property.getName().equals(DevQsarConstants.WATER_SOLUBILITY)) {
//				diff=Math.abs(dpredAPI-dpredJson);
//
//				if(diff>0.1) {
//					System.out.println("mismatch\t"+method);
//					allMatch=false;
//				}
//			} else if (property.getName().equals(DevQsarConstants.BOILING_POINT)) {
//				diff=Math.abs(dpredAPI-dpredJson)/dpredAPI*100;
//				if(diff>0.1) {
//					allMatch=false;
//				}
//			} else if (property.getName().equals(DevQsarConstants.AMES_MUTAGENICITY)) {
//				
//				if(dpredJson>=0.5) dpredJson=1.0;
//				else dpredJson=0.0;
//				
//				diff=Math.abs(dpredAPI-dpredJson);
//				
//				if(diff>0.1) {
//					allMatch=false;
//				}
//				
//			}
			
			diff=Math.abs(dpredAPI-dpredJson);
			
			if(diff>0.1) {
				allMatch=false;
			}
			
			
			
		}
		
		if(htAPI.size()==0 && htJson.size()==0) return;
		if(allMatch)return;
		
		System.out.println(pr.getDTXSID()+"\t"+abbrev+"\t"+pd.getDsstoxRecord().getSmiles());
		System.out.println("API:"+gson.toJson(htAPI));
		System.out.println("JSON:"+gson.toJson(htJson));
		
//		System.out.println(response.getBody().toString());
		
	}


	/**
	 * There was a bug in RunTestCalculationFromJar where the test chemical is used in the 
	 * nearest neighbor prediction (not necessarily a bad thing, it's more accurate)
	 * but causes it to not match webtest real time preds
	 * 
	 * @param pr
	 */
//	private void fixNearestNeighborAndConsensus(PredictionResults pr) {
//
//		if(!pr.getError().isBlank()) return;
//		
//		if(pr.getPredictionResultsPrimaryTable()==null)return;
//		
//		if(pr.getSimilarChemicals()==null)return;
//		
//		SimilarChemicals sc=null;
//
//		if(pr.getSimilarChemicals().size()==0) {
//			System.out.println("Dont have test set chemicals for either set\t"+pr.getDTXSID());
//			return;
//		} else if(pr.getSimilarChemicals().size()==1) {
//			sc=pr.getSimilarChemicals().get(0);			
//			if(!sc.getSimilarChemicalsSet().equals("training")) {
//				System.out.println("Dont have training set analogs\t"+pr.getDTXSID());
//				return;
//			}
//		} else {
//			sc=pr.getSimilarChemicals().get(1);			
//			if(!sc.getSimilarChemicalsSet().equals("training")) {
//				System.out.println("Not training for set\t"+pr.getDTXSID());//shouldnt happen
//				return;
//			}
//		}
//		
//		Vector<SimilarChemical>simChems=sc.getSimilarChemicalsList();
//		
//		if(simChems.size()<4) {
////			System.out.println("Dont have 4 training neighbors for "+pr.getDTXSID()+"\t"+pr.getEndpoint());
//			return;//cant generate new NN pred
//		}
//		
//		double predNN=0;
//		
//		if(!simChems.get(0).getDSSTOXCID().equals(pr.getDTXCID())) {
//			System.out.println("First neighbor is not test chemical:\t"+pr.getDTXSID());
//			return;
//		}
//		
//		
//		for (int i=1;i<=3;i++) {
//			predNN+=Double.parseDouble(simChems.get(i).getExpVal());
//		}
//		
//		predNN/=3;
//		
//		Double predConsensus=0.0;
//		int countConsensus=0;
//		
//		DecimalFormat df=new DecimalFormat("0.00");
//		
//		for (PredictionIndividualMethod pim:pr.getIndividualPredictionsForConsensus().getConsensusPredictions()) {
//			if(pim.getMethod().equals("Consensus")) break;
//
//			if(pim.getMethod().equals("Nearest neighbor")) {
//				pim.setPrediction(df.format(predNN));
//			}
//			
//			if(pim.getPrediction().equals("N/A")) continue;
//			countConsensus++;
//			predConsensus+=Double.parseDouble(pim.getPrediction());
//		}
//		
//		String strPredConsensus="N/A";
//		if(countConsensus>=2) {
//			strPredConsensus=df.format(predConsensus/=countConsensus);
//		} 
//		
//		PredictionResultsPrimaryTable pt=pr.getPredictionResultsPrimaryTable();
//		
//		if(pr.isLogMolarEndpoint()) {
//			pt.setPredToxValue(strPredConsensus);
//			pt.setPredToxValMass(null);//calculate from logmolar later
//		} else if(pr.isBinaryEndpoint()) {
//			pt.setPredToxValue(strPredConsensus);
//		} else {
//			pt.setPredToxValMass(strPredConsensus);
//		}
//			
//		
////		System.out.println(pr.getEndpoint()+"\t"+predNN+"\t"+predConsensus);
//		
//	}


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

		private Double convertToMolar(Property property, PredictionResults pr, Double MW, String massValue,String logMolarValue) {

			String endpoint=property.getName();

			if (endpoint.equals(DevQsarConstants.FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50)
					|| endpoint.equals(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50)
					|| endpoint.equals(DevQsarConstants.FORTY_EIGHT_HR_TETRAHYMENA_PYRIFORMIS_IGC50)
					|| endpoint.equals(DevQsarConstants.WATER_SOLUBILITY)
					|| endpoint.equals(DevQsarConstants.ORAL_RAT_LD50)) {

				if(MW==null) {
					return Math.pow(10.0,-Double.parseDouble(logMolarValue));
				} else if(pr.getPredictionResultsPrimaryTable().getMassUnits().contains("mg/")) {
					return Double.parseDouble(massValue)/1000.0/MW;//mol/L value
				} else {
					System.out.println("Handle "+pr.getPredictionResultsPrimaryTable().getMassUnits()+" in convertMassUnitsToMolar()");
				}

				return Math.pow(10.0,-Double.parseDouble(massValue));
			} else if (endpoint.equals(DevQsarConstants.BCF) 
					|| endpoint.equals(DevQsarConstants.VAPOR_PRESSURE)
					|| endpoint.contains(DevQsarConstants.VISCOSITY)
					|| endpoint.equals(DevQsarConstants.ESTROGEN_RECEPTOR_RBA)) {
				return Double.parseDouble(massValue);
			} else {
				System.out.println("Not handled:"+endpoint);
				return null;
			}
		}


		public PredictionDashboard convertPredictionResultsToPredictionDashboard(PredictionResults pr,PredictionDashboardTableMaps tableMaps,
				boolean writeReportToHarddrive) {

			if(pr.getSmiles()==null) pr.setSmiles("N/A");

			PredictionDashboard pd=new PredictionDashboard();

			try {

				PredictionResultsPrimaryTable pt=pr.getPredictionResultsPrimaryTable();

				try {

					String propertyName=pr.getEndpoint();
					String propertyNameDB=TESTConstants.getPropertyNameDB(propertyName);
					String modelName=initializeDB.getModelName(propertyNameDB);

					//					pr.setEndpoint(propertyNameDB);

					//				System.out.println(Utilities.gson.toJson(htModels.get(modelName)));

					pd.setModel(tableMaps.mapModels.get(modelName));

					//				System.out.println("here");

					pd.setCanonQsarSmiles("N/A");
					pd.setDtxcid(pr.getDTXCID());

					if(tableMaps.mapDsstoxRecordsByCID.containsKey(pr.getDTXCID())) {
						pd.setDsstoxRecord(tableMaps.mapDsstoxRecordsByCID.get(pr.getDTXCID()));
					} else {
						return null;
					}


					//				pd.setDtxsid(pr.getDTXSID());
					//				pd.setDtxcid(pr.getDTXCID());
					//				pd.setSmiles(pr.getSmiles());

					pd.setCreatedBy(lanId);

					//	if(pr.getPredictionResultsPrimaryTable()!=null && pr.getPredictionResultsPrimaryTable().getMessage()!=null) {
					//		System.out.println("message="+pr.getPredictionResultsPrimaryTable().getMessage());
					//	}


					if (pr.getError()!=null && !pr.getError().isBlank()) {
						
						if(pr.getError().toLowerCase().contains("paths")) {
							System.out.println("Paths:"+pr.getError());
						} else if(pr.getError().toLowerCase().contains("rings")) {
							System.out.println("Rings:"+pr.getError());
						} else {
							System.out.println("Other:"+pr.getError());	
						}
						
						
						pd.setPredictionError(pr.getError());
					} else {
						setExperimentalPredictedValues(pr, pd);
					}

					if (pd.getPredictionError()!=null) {
						if (pd.getPredictionError().equals("No prediction could be made")) {
							pd.setPredictionError("No prediction could be made due to applicability domain violation");
						} else if (pd.getPredictionError().contains("could not parse")) {
							pd.setPredictionError("Could not parse smiles");	
						}
					}

					addNeighbors(pr,pd,tableMaps);

					addApplicabilityDomain(pr, pd,tableMaps.mapMethodAD);

					//Previously predictionResults object was used as the json report

					//Now creating a standardized report:
					long t1=System.currentTimeMillis();
					createReport(pd,pr, true, tableMaps,writeReportToHarddrive);
					long t2=System.currentTimeMillis();
//					System.out.println("Time to create reports:"+(t2-t1)+"ms");

					//				if(createReports)
					//					createReport(pd, useLatestModelIds, lookups);

					//	pd.setModel(null);//so can print out
					//  System.out.println(Utilities.gson.toJson(pd));
				} catch (Exception ex) {
					//					System.out.println(gson.toJson(pr));
					ex.printStackTrace();
				}



			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

			return pd;
		}

		private void addNeighbors(PredictionResults pr, PredictionDashboard pd,
				PredictionDashboardTableMaps tableMaps) {


			if(pr.getSimilarChemicals().size()==0) return;

			List<QsarPredictedNeighbor>qpns=new ArrayList<>();
			pd.setQsarPredictedNeighbors(qpns);

			SimilarChemicals scTestSet=pr.getSimilarChemicals().get(0);//test set similar chemicals are first
			addNeighbors(pr, pd, qpns, scTestSet,1);

			SimilarChemicals scTrainingSet=pr.getSimilarChemicals().get(1);
			addNeighbors(pr, pd, qpns, scTrainingSet,0);

			QsarPredictedNeighbor.addNeighborMetadata(tableMaps, pr.getEndpoint(), qpns);

		}


		private void addNeighbors(PredictionResults pr, PredictionDashboard pd, List<QsarPredictedNeighbor> qpns,
				SimilarChemicals scTestSet, int split_num) {

			int counter=0;
			for (SimilarChemical sc:scTestSet.getSimilarChemicalsList()) {
				QsarPredictedNeighbor qpn=new QsarPredictedNeighbor();

				qpn.setNeighborNumber(++counter);
				qpn.setDtxsid(sc.getDSSTOXSID());//already set in the old report using dtxsid lookup
				qpn.setCasrn(sc.getCAS());
				//				qpn.setDtxsid(sc.getDSSTOXSID());//let the lookup figure it out fresh
				qpn.setPredictionDashboard(pd);
				qpn.setSplitNum(split_num);
				qpn.setSimilarityCoefficient(Double.parseDouble(sc.getSimilarityCoefficient()));

				setNeighborExperimentalValue(pr,sc,qpn);
				setNeighborPredictedValue(pr,sc,qpn);

				//				try {
				//					qpn.setPredictedValue(Double.parseDouble(sc.getPredVal()));
				//				} catch (Exception ex) {
				//					qpn.setPredictedString(sc.getPredVal());
				//				}

				qpns.add(qpn);
			}
		}

		void setNeighborExperimentalValue(PredictionResults pr,SimilarChemical sc,QsarPredictedNeighbor qpn) {

			if(sc.getExpVal().equals("N/A")) return;

			//			if (pr.isLogMolarEndpoint()) {
			//				qpn.setExperimentalValue(convertLogMolarUnits(pr.getEndpoint(), sc.getExpVal()));
			//			} else {
			//				qpn.setExperimentalValue(Double.parseDouble(sc.getExpVal()));			
			//			}

			//Dont convert needs to match plot
			qpn.setExperimentalValue(sc.getExpVal());	


		}

		void setNeighborPredictedValue(PredictionResults pr,SimilarChemical sc,QsarPredictedNeighbor qpn) {

			if(sc.getPredVal().equals("N/A")) return;
			//							
			//			if (pr.isLogMolarEndpoint()) {
			//				qpn.setPredictedValue(convertLogMolarUnits(pr.getEndpoint(), sc.getPredVal()));
			//			} else {
			//				qpn.setPredictedValue(Double.parseDouble(sc.getPredVal()));			
			//			}

			//Dont convert needs to match plot
			qpn.setPredictedValue(sc.getPredVal());
		}


//		@Deprecated
//		private void setExperimentalPredictedValuesOld(PredictionResults pr, PredictionDashboard pd) {
//
//			PredictionResultsPrimaryTable pt=pr.getPredictionResultsPrimaryTable();
//			
//			if(pt==null)return;
//
//			if (pr.isBinaryEndpoint()) {
//
//				if (pt.getPredToxValue().equals("N/A")) {
//					pd.setPredictionError(pt.getMessage());
//				} else {
//					pd.setPredictionValue(pt.getPredToxValue());
//					pd.setPredictionString(pt.getPredValueConclusion());
//				}
//
//				if (!pt.getExpToxValue().equals("N/A")) {
//					pd.setExperimentalValue(pt.getExpToxValue());
//					pd.setExperimentalString(pt.getExpToxValueConclusion());
//					//				System.out.println(pr.getDTXCID()+"\t"+pr.getEndpoint()+"\tExperimental value="+pd.getExperimentalValue()+",Experimental string="+pd.getExperimentalString());
//				}
//
//
//			} else if (pr.isLogMolarEndpoint()) {
//
//				Double MW=pd.getDsstoxRecord().getMolWeight();
//
//				if(MW==null) {
//					System.out.println("Molecular weight is null for "+pd.getDsstoxRecord().getDtxsid());
//				}
//
//				String datasetName=pd.getModel().getDatasetName();
//				Dataset dataset=tableMaps.mapDatasets.get(datasetName);
//				Property property=dataset.getProperty();
//
//				if (pt.getPredToxValue().equals("N/A")) {
//					pd.setPredictionError(pt.getMessage());
//				} else if (pt.getPredToxValMass()!=null) {
//					pd.setPredictionValue(convertToMolar(property, pr,MW, pt.getPredToxValMass(),pt.getPredToxValue()));
//				} else {
//					pd.setPredictionValue(convertLogMolarUnits(pr,pt.getPredToxValue()));					
//				}
//				
//				if (pt.getPredToxValue().equals("N/A")) {
//					pd.setPredictionError(pt.getMessage());
//				} else if (pt.getPredToxValMass()!=null) {
//					pd.setPredictionValue(convertToMolar(property, pr,MW, pt.getPredToxValMass(),pt.getPredToxValue()));
//				} else {
//					pd.setPredictionValue(convertLogMolarUnits(pr,pt.getPredToxValue()));					
//				}
//				
//				
//
//				if (!pt.getExpToxValue().equals("N/A")) {
//					pd.setExperimentalValue(convertToMolar(property, pr,MW, pt.getExpToxValMass(),pt.getExpToxValue()));
//				}
//
//				//			System.out.println(pr.getDTXCID()+"\t"+pr.getEndpoint()+"\tExperimental value="+pd.getExperimentalValue());
//
//			} else {
//				
////				if (pt.getPredToxValMass()==null) {
////					System.out.println(Utilities.gson.toJson(pr));
////					return;
////				}
//
//				if (pt.getPredToxValMass().equals("N/A")) {
//					pd.setPredictionError(pt.getMessage());
//				} else {
//					pd.setPredictionValue(pt.getPredToxValMass());
//				}
//
//				if (!pt.getExpToxValMass().equals("N/A")) {
//					pd.setExperimentalValue(pt.getExpToxValMass());
//					//				System.out.println(pr.getDTXCID()+"\t"+pr.getEndpoint()+"\tExperimental value="+pd.getExperimentalValue());
//				}
//			}
//		}

		
		private void setExperimentalPredictedValues(PredictionResults pr,PredictionDashboard pd) {

			PredictionResultsPrimaryTable pt=pr.getPredictionResultsPrimaryTable();
			
			if(pt==null)return;
			
			if (pt.getPredToxValue().equals("N/A")) {
				pd.setPredictionError(pt.getMessage());
			} else {
				pd.setPredictionValue(pr.getPredValueInModelUnits());
//				pd.setPredictionString(pt.getPredValueEndpoint());
			}
			
			if (!pt.getExpToxValue().equals("N/A")) {
				pd.setExperimentalValue(pr.getExpValueInModelUnits());
//				pd.setExperimentalString(pt.getExpToxValueEndpoint());
				//				System.out.println(pr.getDTXCID()+"\t"+pr.getEndpoint()+"\tExperimental value="+pd.getExperimentalValue()+",Experimental string="+pd.getExperimentalString());
			}

		}
		
		private void addApplicabilityDomain(PredictionResults pr,PredictionDashboard pd, TreeMap<String, MethodAD> hmMethodAD) {

			QsarPredictedADEstimate adEstimate=new QsarPredictedADEstimate();
			adEstimate.setCreatedBy(lanId);
			adEstimate.setMethodAD(hmMethodAD.get(DevQsarConstants.Applicability_Domain_Combined));


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
//					adEstimate.setReasoning(e);	
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


		private void createReport(PredictionDashboard pd, PredictionResults pr, boolean useLatestModelIds, PredictionDashboardTableMaps tableMaps,boolean writeReportToHarddrive) {

			if(pd.getDsstoxRecord()==null) {
				//		System.out.println("missing dsstoxrecord: "+pd.getDtxcid()+" for "+property.getName());
				return;
			}

			String datasetName=pd.getModel().getDatasetName();
			Dataset dataset=tableMaps.mapDatasets.get(datasetName);
			Property property=dataset.getProperty();

			String unitAbbreviation=dataset.getUnitContributor().getAbbreviation_ccd();
			String unitAbbreviationNeighbor=dataset.getUnit().getAbbreviation_ccd();

			TEST_Report tr=new TEST_Report(pd,pr, property, unitAbbreviation,unitAbbreviationNeighbor, useLatestModelIds);
			String fileJson=tr.toJson();

			HTMLReportCreatorTEST h=new HTMLReportCreatorTEST();
			tr.modelDetails.loadPlotsFromDB=true;
			String fileHtml=h.createReport(tr);


			//			if(property.getName().equals(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50)) {
			//				System.out.println(Utilities.gson.toJson(pr)+"\n\n*******************\n");
			//				System.out.println(Utilities.gson.toJson(tr)+"\n\n*******************\n");
			//			}

			if(writeReportToHarddrive) {

				String folder = "data\\"+pd.getModel().getSource().getName()+"\\reports\\"+pd.getDsstoxRecord().getDtxsid();
				File Folder=new File(folder);
				if(!Folder.exists()) Folder.mkdirs();

				String filename=tr.chemicalIdentifiers.dtxsid+"_"+tr.modelDetails.modelName+".html";
				h.writeStringToFile(fileHtml,folder,filename);
				h.writeStringToFile(fileJson,folder,filename.replace(".html",".json"));

				gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.viewInWebBrowser(folder+File.separator+filename);

			}

			PredictionReport predictionReport=new PredictionReport(pd, fileJson,fileHtml, lanId);
			pd.setPredictionReport(predictionReport);

		}

	}

	void loadFromJsonFile() {

		boolean fixReports=false;
		boolean skipER=true;

		boolean writeToDB=false;
//		boolean writeToDB=true;

		boolean writeReportToHarddrive=true;
		boolean writePredictionReportJson=true;

		//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\RunTestCalculationsFromJar\\reports\\";
		//		String filePathJson=folder+"TEST_results_all_endpoints_sample.json";
		//		pds.runFromDashboardJsonFileBatchPost(filePathJson,writeToDB,skipER, fixReports);

		//		String dtxsid="DTXSID101256899";
//				String dtxsid="DTXSID40166952";//has exp BP
		//		String dtxsid="DTXSID3039242";//bz
//		String dtxsid="DTXSID40167000";
		String dtxsid=null;

		if(dtxsid==null) {
			writeReportToHarddrive=false;
			writePredictionReportJson=false;
		}

		//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\RunTestCalculationsFromJar\\reports sample\\";
		//		String filePathJson=folder+"predictionResults_DTXSID3039242.json";//
		//		runFromDashboardJsonFileBatchPost(filePathJson,writeToDB,skipER, fixReports,dtxsid,writeReportToHarddrive,writePredictionReportJson);


		//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\RunTestCalculationsFromJar\\reports clowder\\";
		//		int num=34;
		//		String filePathJson=folder+"TEST_results_all_endpoints_snapshot_compounds"+num+".json";//
		//		runFromDashboardJsonFileBatchPost(filePathJson,writeToDB,skipER, fixReports,dtxsid,writeReportToHarddrive,writePredictionReportJson);


		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\RunTestCalculationsFromJar\\reports clowder\\";
		for (int num=35;num<=35;num++) {
			String filePathJson=folder+"TEST_results_all_endpoints_snapshot_compounds"+num+".json";//
			runFromDashboardJsonFileBatchPost(filePathJson,writeToDB,skipER, fixReports,dtxsid,writeReportToHarddrive,writePredictionReportJson);
		}

	}

	public void runSDF_all_endpoints_to_DB() {

		boolean writeToDB=false;
		boolean debug=true;
		int maxCount=-1;
		boolean skipMissingSID=true;
		String method = TESTConstants.ChoiceConsensus;// what QSAR method being used (default- runs all methods and
		boolean createReports = true;// whether to store report
		boolean createDetailedReports = false;// detailed reports have lots more info and creates more html files
		boolean writeReportToHarddrive=false;
		boolean writePredictionReportJson=false;
		String dtxsid=null;
		
		
		String folderMain="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\";
		String folder=folderMain+"data\\dsstox\\snapshot-2024-11-12\\sdf\\";
		int num=1;
		String filenameSDF="prod_compounds_updated_lt_2024-11-12_"+num+".sdf";
		String sdfPath=folder+filenameSDF;
		System.out.println(sdfPath);

		AtomContainerSet acs=RunFromSDF.readSDFV3000(sdfPath);

		if(debug) System.out.println("atom container count in sdf="+acs.getAtomContainerCount());
		AtomContainerSet acs2 = RunFromSDF.filterAtomContainerSet(acs, skipMissingSID,maxCount);
		if(debug) System.out.println("atom container count filtered="+acs2.getAtomContainerCount());


		String snapshotName="DSSTOX Snapshot 11/12/2024";		
		DsstoxSnapshotServiceImpl snapshotService = new DsstoxSnapshotServiceImpl();
		DsstoxSnapshot snapshot = snapshotService.findByName(snapshotName);

		SourceService sourceService=new SourceServiceImpl();
		String sourceName="TEST" + version;
		Source source=sourceService.findByName(sourceName);
		
		HashSet<String>cidsLoaded=DatabaseUtilities.getLoadedCIDsWithCount(source.getName(),16);
		
		for(int i=0;i<acs2.getAtomContainerCount();i++) {
			IAtomContainer ac=acs2.getAtomContainer(i);
			if(cidsLoaded.contains(ac.getProperty("DTXCID"))) {
				acs2.removeAtomContainer(i--);
			}
		}
 		
		if(debug) System.out.println("atom container count to run="+acs2.getAtomContainerCount());


//		if(true)return;


		if (acs2.getAtomContainerCount()==0) {
			if(debug) System.out.println("All chemicals ran");
			return;
		}

		if(debug) System.out.println("");

		for (String endpoint:RunFromSmiles.allEndpoints) {
			if (debug) System.out.println("Loading "+endpoint);
			WebTEST4.loadTrainingData(endpoint,method);//Note: need to use webservice approach to make this data persistent
		}

		int counter=0;
		int batchSize=100;

		List<PredictionDashboard>predictionsDashboard=new ArrayList<>();

		HashSet<String> dtxcidsLoaded=DatabaseUtilities.getLoadedCIDsWithCount(source.getName(),16);
		System.out.println("Loaded dtxcids: "+dtxcidsLoaded.size());
		
		if(tableMaps==null)
			tableMaps=new PredictionDashboardTableMaps(PredictionDashboardTableMaps.fileJsonDsstoxRecords2024_11_12,PredictionDashboardTableMaps.fileJsonOtherCAS2024_11_12);//creates lookup maps for database objects so dont have to keep query the database

		int countAlreadyHave=0;
		boolean foundDtxsid=false;

		if(debug) System.out.println("");

		for (int i=0;i<acs2.getAtomContainerCount();i++) {

			IAtomContainer ac=acs2.getAtomContainer(i);
			
			String smiles=ac.getProperty("smiles");
			
			if(smiles!=null) {
				if(smiles.contains(".") || smiles.contains("|") || smiles.contains(":") || smiles.contains("*")) {
					continue;
				}
			}
			
			String dtxcid=ac.getProperty("DTXCID");			
			if(dtxcidsLoaded.contains(dtxcid)) continue;			

			if (debug) System.out.println(ac.getProperty("smiles")+"");

			counter++;

			//	System.out.println(strPredictionResults);

			if(counter%batchSize==0) System.out.println(counter);

			List<PredictionResults>results=RunFromSmiles.runEndpointsAsList(ac, RunFromSmiles.allEndpoints, method,createReports,createDetailedReports);

			for(PredictionResults predictionResults:results) {

				fixUnitsBCF(predictionResults);

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

				if(writeReportToHarddrive && writePredictionReportJson) {
					String folderReports = "data\\TEST5.1.3\\reports\\"+predictionResults.getDTXSID()+"\\";
					Utilities.saveJson(predictionResults, folderReports+predictionResults.getDTXSID()+"_"+predictionResults.getEndpoint()+".json");
				}

				//System.out.println(Utilities.gson.toJson(predictionResults));

				PredictionDashboard pd=converter.convertPredictionResultsToPredictionDashboard(predictionResults,tableMaps,writeReportToHarddrive);

				if(pd==null) {
					//System.out.println(predictionResults.getDTXCID()+"\tMissing dsstox record");
					continue;
				}
				//				System.out.println(pd.toJson());

				predictionsDashboard.add(pd);

				if(predictionsDashboard.size()==batchSize) {
					//					System.out.println(counter);
					if(writeToDB) predictionDashboardService.createSQL(predictionsDashboard);
					predictionsDashboard.clear();
				}

				//	if(predictionResults.getPredictionResultsPrimaryTable().getExpCAS()!=null) {
				//		System.out.println(predictionResults.getDTXSID()+"\t"+predictionResults.getEndpoint()+"\tHas exp");
				//	}


			}//end loop over reports for chemical

		}
		
		System.out.println("exited main loop");
		System.out.println("remaining to load: "+predictionsDashboard.size());

		//Do what's left:
		if(writeToDB) predictionDashboardService.createSQL(predictionsDashboard);


	}

	private void fixUnitsBCF(PredictionResults predictionResults) {
		if(predictionResults.getPredictionResultsPrimaryTable()!=null) {
			if(predictionResults.getEndpoint().equals("Bioconcentration factor")) {
				predictionResults.getPredictionResultsPrimaryTable().setMolarLogUnits("log10(L/kg)");
				if(predictionResults.getIndividualPredictionsForConsensus()!=null) {
					predictionResults.getIndividualPredictionsForConsensus().setUnits("log10(L/kg)");
				}
			}
		}
	}


	void runNewChemical() {

		boolean fixReports=false;
		boolean skipER=true;
		boolean writeToDB=false;

		List<String>dtxsids=new ArrayList<>();
		//		dtxsids.add("DTXSID7020182");//BPA
		dtxsids.add("DTXSID3039242");//Bz
		//		dtxsids.add("DTXSID00167081");//salt TODO

		for (String dtxsid:dtxsids) {
			runNewChemical(dtxsid);//Make original TEST json file with one model result on each line
			String folder="data\\TEST5.1.3\\reports\\"+dtxsid+"\\";
			String filePathJson=folder+dtxsid+"_TEST_PredictionResults.json";
			boolean writeReportToHarddrive=true;
			boolean writePredictionResults=true;
			runFromDashboardJsonFileBatchPost(filePathJson,writeToDB,skipER, fixReports,null,writeReportToHarddrive,writePredictionResults);
		}

	}


	public static void main(String[] args) {
		PredictionDashboardScriptTEST pds=new PredictionDashboardScriptTEST();

		DatabaseUtilities d=new DatabaseUtilities();
//		d.deleteAllRecords("TEST5.1.3");

//		pds.initializeDB.initializeDB();
		
//		pds.initializeDB.createPlotJsons();
//		PredictionDashboardScriptOPERA pdso= new PredictionDashboardScriptOPERA();
//		pdso.extraMethods.uploadPlots("TEST5.1.3");

		
//		pds.runSDF_all_endpoints_to_DB();
		
		pds.loadFromJsonFile();
				
		//		pds.runNewChemical();
		//		pds.runNewPredictionsFromTextFile();
		//		pds.runNewChemical("DTXSID7020182");
		//		pds.runNewChemical("DTXSID3039242");

//		RunFromSmiles.runDTXSIDAllEndpoints();
		

		//		boolean fixReports=false;
		//		boolean skipER=true;
		//		boolean writeToDB=false;

		//String filePathJson="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\RunTestCalculationsFromJar\\reports\\TEST_results_all_endpoints_snapshot_compounds33.json";
		//		String filePathJson2="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\RunTestCalculationsFromJar\\reports\\"+dtxsid+".json";
		//		pds.testUtilities.extractRecords(filePathJson,filePathJson2,dtxsid);

	}


	/**
	 * Generates new PredictionResults json from chemicals in a text file
	 * 
	 */
	private void runNewPredictionsFromTextFile() {

		RunFromSmiles.debug=false;

		//Need to set path of structure db because it's a relative path and this is calling TEST project:
		ResolverDb2.setSqlitePath("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18_EPA_Github\\databases\\snapshot.db");

		boolean createReports=true;//whether to store report
		boolean createDetailedReports=false;//detailed reports have lots more info and creates more html files
		String method =TESTConstants.ChoiceConsensus;//what QSAR method being used (default- runs all methods and takes average)

		List<String>endpoints=TESTConstants.getFullEndpoints(null);
		//		endpoints= Arrays.asList(TESTConstants.ChoiceFHM_LC50);

		Gson gson=new Gson();
		try {

			String filepath="data\\TEST1.0\\sample compounds.txt";
			String filepathOut="data\\TEST1.0\\sample compounds.json";

			BufferedReader br=new BufferedReader(new FileReader(filepath));
			br.readLine();

			FileWriter fw=new FileWriter(filepath.replace(".txt", ".json"));

			int count=0;

			while (true) {
				String Line=br.readLine();
				if(Line==null) break;

				count++;

				String [] vals=Line.split("\t");

				String dtxsid=vals[0];
				String dtxcid=vals[1];
				String casrn=vals[2];
				String smiles=vals[3];

				IAtomContainer molecule=MoleculeCreator.createMolecule(smiles, dtxsid, dtxcid, casrn);
				AtomContainerSet acs=new AtomContainerSet();
				acs.addAtomContainer(molecule);
				//				acs.addAtomContainer(RunFromSmiles.createMolecule("NC1=C(C=CC(=C1)NC(=O)C)OCC", "DTXSID7020053","17026-81-2"));


				List<PredictionResults>listPR=RunFromSDF.runEndpointsAsList(acs, endpoints, method,createReports,createDetailedReports);		
				System.out.println(count+"\t"+smiles+"\t"+listPR.size());

				//				if (smiles.contains(".")) {
				//					System.out.println(Utilities.gson.toJson(listPR.get(0)));	
				//				}

				for (PredictionResults pr:listPR) {
					String json=gson.toJson(pr);
					fw.write(json+"\r\n");
				}
				fw.flush();

				//				System.out.println(dtxsid+"\t"+smiles+"\t"+molecule.getAtomCount());
				//				if(true) break;
			}

			br.close();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void runNewChemical(String dtxsid) {

		RunFromSmiles.debug=false;

		//Need to set path of structure db because it's a relative path and this is calling TEST project:
		ResolverDb2.setSqlitePath("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18_EPA_Github\\databases\\snapshot.db");

		boolean createReports=true;//whether to store report
		boolean createDetailedReports=false;//detailed reports have lots more info and creates more html files
		String method =TESTConstants.ChoiceConsensus;//what QSAR method being used (default- runs all methods and takes average)

		List<String>endpoints=new ArrayList(TESTConstants.getFullEndpoints(null));

		//TODO need to add predictions text file to TEST jar for following endpoints or cant run from smiles:
		endpoints.remove(TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity);
		endpoints.remove(TESTConstants.ChoiceEstrogenReceptor);


		//		List<String>endpoints=Arrays.asList(TESTConstants.ChoiceFHM_LC50);
		//		endpoints= Arrays.asList(TESTConstants.ChoiceFHM_LC50);

		Gson gson=new Gson();
		try {
			String folder="data\\TEST5.1.3\\reports\\"+dtxsid+"\\";

			File F=new File(folder);
			F.mkdirs();

			String filepathOut=folder+dtxsid+"_TEST_PredictionResults.json";

			ArrayList<DSSToxRecord>recs=ResolverDb2.lookupByDTXSID(dtxsid);

			DSSToxRecord dr=recs.get(0);
			System.out.println(Utilities.gson.toJson(dr));

			FileWriter fw=new FileWriter(filepathOut);

			IAtomContainer molecule=MoleculeCreator.createMolecule(dr);
			AtomContainerSet acs=new AtomContainerSet();
			acs.addAtomContainer(molecule);
			//				acs.addAtomContainer(RunFromSmiles.createMolecule("NC1=C(C=CC(=C1)NC(=O)C)OCC", "DTXSID7020053","17026-81-2"));

			List<PredictionResults>listPR=RunFromSDF.runEndpointsAsList(acs, endpoints, method,createReports,createDetailedReports);		

			for (PredictionResults pr:listPR) {
				String json=gson.toJson(pr);
				fw.write(json+"\r\n");
			}
			fw.flush();

			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}


}

