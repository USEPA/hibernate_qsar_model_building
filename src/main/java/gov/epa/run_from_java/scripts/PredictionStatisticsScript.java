package gov.epa.run_from_java.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintViolationException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
//import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.JsonArray;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInConsensusModel;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInModelSet;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSet;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInConsensusMethodServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInConsensusModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelInModelSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetServiceImpl;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.models.ModelStatisticCalculator;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.endpoints.reports.predictions.ExcelReports.ExcelPredictionReportGenerator;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportDataPoint;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportModelMetadata;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportModelStatistic;
import gov.epa.endpoints.reports.predictions.QsarPredictedValue;
import gov.epa.run_from_java.scripts.ApplicabilityDomainScript.ApplicabilityDomainPrediction;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;
import gov.epa.run_from_java.scripts.GetExpPropInfo.ExcelCreator;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.util.FileUtils;
import gov.epa.run_from_java.scripts.RecalcStatsScript.SplitPredictions;

public class PredictionStatisticsScript {

	List<String> datasetNames = new ArrayList<>();

	PredictionStatisticsScript() {
		// datasetNames.add("HLC v1 res_qsar");
		// datasetNames.add("WS v1 res_qsar");
		// datasetNames.add("VP v1 res_qsar");
		// datasetNames.add("LogP v1 res_qsar");
		// datasetNames.add("BP v1 res_qsar");
		// datasetNames.add("MP v1 res_qsar");

		datasetNames.add("HLC v1 modeling");
		datasetNames.add("WS v1 modeling");
		datasetNames.add("VP v1 modeling");
		datasetNames.add("BP v1 modeling");
		datasetNames.add("LogP v1 modeling");
		datasetNames.add("MP v1 modeling");

	}

	String lanId = "tmarti02";
	static Connection conn = SqlUtilities.getConnectionPostgres();
	RecalcStatsScript recalcStatsScript = new RecalcStatsScript();

	ModelServiceImpl modelService = new ModelServiceImpl();

	/**
	 * Get the modelID for model for given dataset, method, and modelSet
	 * 
	 * @param modelId
	 * @param datasetName
	 * @param methodName
	 * @return
	 */
	Long getModelId(String modelSetName, String datasetName, String methodName) {

		String sql = "select mims.fk_model_id from qsar_models.models_in_model_sets mims\n"
				+ "join qsar_models.models m on m.id=mims.fk_model_id\n"
				+ "join qsar_models.methods m2 on m2.id=m.fk_method_id\n"
				+ "join qsar_models.model_sets ms on ms.id=mims.fk_model_set_id\n" + "where ms.\"name\"='"
				+ modelSetName + "' and \n" + "m.dataset_name ='" + datasetName.replace("'", "''") + "' and \n"
				+ "m2.\"name\" like '" + methodName + "%';";

		// System.out.println(sql+"\n");
		String strId = SqlUtilities.runSQL(conn, sql);
		if (strId == null)
			return null;
		else
			return (Long.parseLong(strId));
	}

	/**
	 * Get prediction statistic for model
	 * 
	 * @param modelId
	 * @param datasetName
	 * @param methodName
	 * @return
	 */
	Double getStat(long modelId, String statisticName) {

		String sql = "select ms.statistic_value  from qsar_models.model_statistics ms\n"
				+ "join qsar_models.\"statistics\" s2  on s2.id=ms.fk_statistic_id\n" + "where ms.fk_model_id="
				+ modelId + " and s2.\"name\" ='" + statisticName + "';";

//		 System.out.println(sql+"\n");
		// System.out.println(modelId);
		String result = DatabaseLookup.runSQL(conn, sql);
		if (result == null)
			return null;
		else
			return (Double.parseDouble(result));
	}

	/**
	 * Prints summary of stats to the screen for a given method
	 * 
	 * TODO make it write to file
	 * 
	 * 
	 * @param statisticName
	 * @param methodName
	 * @param modelSetNames
	 * @param datasetNames
	 */
	private void createSummaryTableForMethod(String statName, String methodName, List<String> modelSetNames,
			List<String> datasetNames, Hashtable<String, Double> htVals) {

		StringBuffer sb = new StringBuffer();

		DecimalFormat df = new DecimalFormat("0.000");

		sb.append("\n" + statName + " results for method = " + methodName + "\n");
		sb.append("DatasetName\t");

		for (int j = 0; j < modelSetNames.size(); j++) {
			String modelSetName = modelSetNames.get(j);

			sb.append(modelSetName);
			if (j < modelSetNames.size() - 1)
				sb.append("\t");
			else
				sb.append("\n");
		}

		for (int i = 0; i < datasetNames.size(); i++) {
			String datasetName = datasetNames.get(i);
			sb.append(datasetName + "\t");

			for (int j = 0; j < modelSetNames.size(); j++) {
				String modelSetName = modelSetNames.get(j);

				String key = methodName + "\t" + modelSetName + "\t" + datasetName;

				// System.out.println(key);

				Double modelSetStat = htVals.get(key);

				if (modelSetStat == null)
					sb.append("NaN");
				else
					sb.append(df.format(modelSetStat));

				if (j < modelSetNames.size() - 1)
					sb.append("\t");
				else
					sb.append("\n");
			}

		}
		System.out.println(sb.toString());

		try {
			FileWriter fw = new FileWriter("data/reports/stats/" + methodName + "_" + statName + ".txt");
			fw.write(sb.toString());
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * Prints summary of stats for model set
	 * 
	 * TODO make it write to file
	 * 
	 * 
	 * @param statisticName
	 * @param methodName
	 * @param modelSetNames
	 * @param datasetNames
	 */
	private void createSummaryTableForModelSet(String statName, String modelSetName, List<String> methodNames,
			List<String> datasetNames, Hashtable<String, Double> htVals) {

		StringBuffer sb = new StringBuffer();
		DecimalFormat df = new DecimalFormat("0.000");

		sb.append("\n" + statName + " results for model set = " + modelSetName + "\n");
		sb.append("DatasetName\t");

		for (int j = 0; j < methodNames.size(); j++) {
			String methodName = methodNames.get(j);
			sb.append(methodName);
			if (j < methodNames.size() - 1)
				sb.append("\t");
			else
				sb.append("\r\n");
		}

		for (int i = 0; i < datasetNames.size(); i++) {

			String datasetName = datasetNames.get(i);

			String datasetName2 = null;

			if (datasetName.contains(" from")) {
				datasetName2 = datasetName.substring(0, datasetName.indexOf(" from")).trim();
			} else {
				datasetName2 = datasetName;
			}

			// String datasetName2=datasetName.replace(" from exp_prop and chemprop", "");
			sb.append(datasetName2 + "\t");

			for (int j = 0; j < methodNames.size(); j++) {
				String methodName = methodNames.get(j);

				String key = methodName + "\t" + modelSetName + "\t" + datasetName;

				// System.out.println(key);

				Double modelSetStat = htVals.get(key);

				if (modelSetName == null)
					sb.append("N/A");
				else
					sb.append(df.format(modelSetStat));

				if (j < methodNames.size() - 1)
					sb.append("\t");
				else
					sb.append("\r\n");
			}
		}

		System.out.println(sb.toString());

		try {
			FileWriter fw = new FileWriter("data/reports/stats/" + statName + "_" + modelSetName + ".txt");
			fw.write(sb.toString());
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	void createSummaryTableForMethod() {

//		String methodName = DevQsarConstants.CONSENSUS;
		String methodName = DevQsarConstants.XGB;

//		 String statisticName = "MAE_Test";
//		 String statisticName = "RMSE_Test";
		 
//		 String statisticName="PearsonRSQ_Test";
//		 String statisticName="Q2_Test";
		// String statisticName="Q2_CV_Training";
//		String statisticName = DevQsarConstants.Q2_F3_TEST;
//		 String statisticName="PearsonRSQ_CV_Training";
		 String statisticName="MAE_CV_Training";
		 		 

		// Getting predictions for PFAS compounds in test set:
		String listName = "PFASSTRUCTV4";
		String folder = "data/dev_qsar/dataset_files/";
		String filePath = folder + listName + "_qsar_ready_smiles.txt";
		HashSet<String> smilesArrayPFAS = SplittingGeneratorPFAS_Script.getPFASSmiles(filePath);

		List<String> modelSetNames = new ArrayList<>();

		modelSetNames.add("WebTEST2.0 PFAS");
		modelSetNames.add("WebTEST2.0 All but PFAS");
		modelSetNames.add("WebTEST2.0_justPFAS");
		modelSetNames.add("WebTEST2.0");

		modelSetNames.add("WebTEST2.1 PFAS");
		modelSetNames.add("WebTEST2.1 All but PFAS");
		modelSetNames.add("WebTEST2.1_justPFAS");
		modelSetNames.add("WebTEST2.1");

		// datasetNames.add("pKa_a from exp_prop and chemprop");
		// datasetNames.add("pKa_b from exp_prop and chemprop");

		Hashtable<String, Double> htVals = new Hashtable<>();
		addHashtableEntry(statisticName, methodName, modelSetNames, datasetNames, htVals);

		if (modelSetNames.contains("WebTEST2.1_justPFAS") || modelSetNames.contains("WebTEST2.0_justPFAS")) {
			for (String datasetName : datasetNames) {
				if (modelSetNames.contains("WebTEST2.0_justPFAS")) {
					addPFAS_Stats(statisticName, methodName, htVals, smilesArrayPFAS, datasetName, "WebTEST2.0");
				}

				if (modelSetNames.contains("WebTEST2.1_justPFAS")) {
					addPFAS_Stats(statisticName, methodName, htVals, smilesArrayPFAS, datasetName, "WebTEST2.1");
				}
			}
		}

		createSummaryTableForMethod(statisticName, methodName, modelSetNames, datasetNames, htVals);

	}

	void createSummaryTableForMethod_PFAS() {

		String methodName = DevQsarConstants.CONSENSUS;

		// String statisticName = "MAE_Test";
		String statisticName = "PearsonRSQ_Test";
		// String statisticName="Q2_CV_Training";

		// Getting predictions for PFAS compounds in test set:
		String listName = "PFASSTRUCTV4";
		String folder = "data/dev_qsar/dataset_files/";
		String filePath = folder + listName + "_qsar_ready_smiles.txt";
		HashSet<String> smilesArrayPFAS = SplittingGeneratorPFAS_Script.getPFASSmiles(filePath);

		List<String> modelSetNames = new ArrayList<>();

		modelSetNames.add("WebTEST2.0 PFAS");
		modelSetNames.add("WebTEST2.0 All but PFAS");
		modelSetNames.add("WebTEST2.0_justPFAS");
		modelSetNames.add("WebTEST2.0");

		modelSetNames.add("WebTEST2.1 PFAS");
		modelSetNames.add("WebTEST2.1 All but PFAS");
		modelSetNames.add("WebTEST2.1_justPFAS");
		modelSetNames.add("WebTEST2.1");

		List<String> datasetNames = new ArrayList<>();

		// datasetNames.add("HLC from exp_prop and chemprop");
		// datasetNames.add("WS from exp_prop and chemprop v2");
		// datasetNames.add("VP from exp_prop and chemprop");
		// datasetNames.add("LogP from exp_prop and chemprop");
		// datasetNames.add("MP from exp_prop and chemprop v2");
		// datasetNames.add("BP from exp_prop and chemprop v3");

		// datasetNames.add("HLC v1");
		// datasetNames.add("VP v1");
		// datasetNames.add("WS v1");
		// datasetNames.add("BP v1");
		// datasetNames.add("LogP v1");
		// datasetNames.add("MP v1");

		datasetNames.add("HLC v1 res_qsar");
		datasetNames.add("WS v1 res_qsar");
		datasetNames.add("VP v1 res_qsar");
		datasetNames.add("LogP v1 res_qsar");
		datasetNames.add("BP v1 res_qsar");
		datasetNames.add("MP v1 res_qsar");

		// datasetNames.add("pKa_a from exp_prop and chemprop");
		// datasetNames.add("pKa_b from exp_prop and chemprop");

		Hashtable<String, Double> htVals = new Hashtable<>();
		addHashtableEntry(statisticName, methodName, modelSetNames, datasetNames, htVals);

		for (String datasetName : datasetNames) {
			if (modelSetNames.contains("WebTEST2.0_justPFAS")) {
				addPFAS_Stats(statisticName, methodName, htVals, smilesArrayPFAS, datasetName, "WebTEST2.0");
			}

			if (modelSetNames.contains("WebTEST2.1_justPFAS")) {
				addPFAS_Stats(statisticName, methodName, htVals, smilesArrayPFAS, datasetName, "WebTEST2.1");
			}
		}

		createSummaryTableForMethod(statisticName, DevQsarConstants.CONSENSUS, modelSetNames, datasetNames, htVals);

	}

	void createSummaryTableForSet() {
		// String statisticName = "MAE_Test";
		String statisticName = "PearsonRSQ_Test";
		// String statisticName="Q2_CV_Training";

		// Getting predictions for PFAS compounds in test set:
		String listName = "PFASSTRUCTV4";
		String folder = "data/dev_qsar/dataset_files/";
		String filePath = folder + listName + "_qsar_ready_smiles.txt";
		HashSet<String> smilesArrayPFAS = SplittingGeneratorPFAS_Script.getPFASSmiles(filePath);

		// String modelSetName="WebTEST2.0 PFAS";
		// String modelSetName="WebTEST2.0 All but PFAS";
		// String modelSetName="WebTEST2.0_justPFAS";
		// String modelSetName="WebTEST2.0";

		String modelSetName = "WebTEST2.1 PFAS";
		// String modelSetName="WebTEST2.1 All but PFAS";
		// String modelSetName="WebTEST2.1_justPFAS";
		// String modelSetName="WebTEST2.1";

		List<String> modelSetNames = new ArrayList<>();
		modelSetNames.add(modelSetName);

		List<String> methodNames = new ArrayList<>();
		// methodNames.add(DevQsarConstants.KNN);
		methodNames.add(DevQsarConstants.RF);
		methodNames.add(DevQsarConstants.XGB);
		// methodNames.add(DevQsarConstants.SVM);
		methodNames.add(DevQsarConstants.CONSENSUS);

		Hashtable<String, Double> htVals = new Hashtable<>();
		for (String methodName : methodNames) {
			addHashtableEntry(statisticName, methodName, modelSetNames, datasetNames, htVals);
		}

		if (modelSetName.contains("_justPFAS")) {

			for (String methodName : methodNames) {

				System.out.println("\n" + methodName);

				for (String datasetName : datasetNames) {

					if (modelSetName.equals("WebTEST2.0_justPFAS")) {
						addPFAS_Stats(statisticName, methodName, htVals, smilesArrayPFAS, datasetName, "WebTEST2.0");
					}

					if (modelSetName.equals("WebTEST2.1_justPFAS")) {
						addPFAS_Stats(statisticName, methodName, htVals, smilesArrayPFAS, datasetName, "WebTEST2.1");
					}

				}
			}
		}

		// Create the summary table as text file:
		createSummaryTableForModelSet(statisticName, modelSetName, methodNames, datasetNames, htVals);

	}

	void createSummaryTableForSetOPERA() {
		// String statisticName = "MAE_Test";
		String statisticName = "PearsonRSQ_Test";
		// String statisticName="Q2_CV_Training";

		String modelSetName = "WebTEST2.1 Sample models";

		List<String> modelSetNames = new ArrayList<>();
		modelSetNames.add(modelSetName);

		String[] propertyNames = { DevQsarConstants.LOG_KOA, DevQsarConstants.LOG_KM_HL,
				DevQsarConstants.HENRYS_LAW_CONSTANT, DevQsarConstants.LOG_BCF, DevQsarConstants.LOG_OH,
				DevQsarConstants.LOG_KOC, DevQsarConstants.VAPOR_PRESSURE, DevQsarConstants.WATER_SOLUBILITY,
				DevQsarConstants.BOILING_POINT, DevQsarConstants.MELTING_POINT, DevQsarConstants.LOG_KOW };

		List<String> datasetNames = new ArrayList<>();

		for (String propertyName : propertyNames) {
			datasetNames.add(propertyName + " OPERA");
		}

		List<String> methodNames = new ArrayList<>();
		methodNames.add(DevQsarConstants.KNN);
		methodNames.add(DevQsarConstants.RF);
		methodNames.add(DevQsarConstants.XGB);
		methodNames.add(DevQsarConstants.SVM);
		methodNames.add("reg");
		// methodNames.add(DevQsarConstants.CONSENSUS);

		Hashtable<String, Double> htVals = new Hashtable<>();
		for (String methodName : methodNames) {
			addHashtableEntry(statisticName, methodName, modelSetNames, datasetNames, htVals);
		}

		// Create the summary table as text file:
		createSummaryTableForModelSet(statisticName, modelSetName, methodNames, datasetNames, htVals);

	}

	void createSummaryTableForSet2() {

		// Getting predictions for PFAS compounds in test set:
		String listName = "PFASSTRUCTV4";
		String folder = "data/dev_qsar/dataset_files/";
		String filePath = folder + listName + "_qsar_ready_smiles.txt";
		HashSet<String> smilesArrayPFAS = SplittingGeneratorPFAS_Script.getPFASSmiles(filePath);

		List<String> modelSetNames = new ArrayList<>();
		modelSetNames.add("WebTEST2.0");
		modelSetNames.add("WebTEST2.1");
		
//		modelSetNames.add("WebTEST2.0 PFAS");
//		modelSetNames.add("WebTEST2.1 PFAS");


		List<String> methodNames = new ArrayList<>();
		// methodNames.add(DevQsarConstants.KNN);
//		methodNames.add(DevQsarConstants.RF);
		methodNames.add(DevQsarConstants.XGB);
		// methodNames.add(DevQsarConstants.SVM);
//		methodNames.add(DevQsarConstants.CONSENSUS);

		List<String> statisticNames = new ArrayList<>();
//		statisticNames.add("Q2_CV_Training");
//		statisticNames.add("MAE_CV_Training");
//		statisticNames.add("PearsonRSQ_CV_Training");
		statisticNames.add("PearsonRSQ_Test");
//		statisticNames.add("RMSE_Test");
//		 statisticNames.add("MAE_Test");

		for (String modelSetName : modelSetNames) {

			for (String statisticName : statisticNames) {

				Hashtable<String, Double> htVals = new Hashtable<>();
				for (String methodName : methodNames) {
					addHashtableEntry(statisticName, methodName, modelSetNames, datasetNames, htVals);
				}

				for (String methodName : methodNames) {
					// System.out.println("\n"+methodName);

					for (String datasetName : datasetNames) {
						if (modelSetName.equals("WebTEST2.0_justPFAS")) {
							addPFAS_Stats(statisticName, methodName, htVals, smilesArrayPFAS, datasetName,
									"WebTEST2.0");
						}

						if (modelSetName.equals("WebTEST2.1_justPFAS")) {
							addPFAS_Stats(statisticName, methodName, htVals, smilesArrayPFAS, datasetName,
									"WebTEST2.1");
						}
					}
				}

				// Create the summary table as text file:
				createSummaryTableForModelSet(statisticName, modelSetName, methodNames, datasetNames, htVals);
			}
		}

	}

	void createSummaryTableForSetSermacs() {

		// Getting predictions for PFAS compounds in test set:
		String listName = "PFASSTRUCTV4";
		String folder = "data/dev_qsar/dataset_files/";
		String filePath = folder + listName + "_qsar_ready_smiles.txt";
		HashSet<String> smilesArrayPFAS = SplittingGeneratorPFAS_Script.getPFASSmiles(filePath);

		List<String> modelSetNames = new ArrayList<>();
		modelSetNames.add("WebTEST2.0");
		modelSetNames.add("WebTEST2.1");
		
//		modelSetNames.add("WebTEST2.0 PFAS");
//		modelSetNames.add("WebTEST2.1 PFAS");


		List<String> methodNames = new ArrayList<>();
//		 methodNames.add(DevQsarConstants.KNN);
		methodNames.add(DevQsarConstants.RF);
//		methodNames.add(DevQsarConstants.XGB);
		// methodNames.add(DevQsarConstants.SVM);
//		methodNames.add(DevQsarConstants.CONSENSUS);

		List<String> statisticNames = new ArrayList<>();
//		statisticNames.add("Q2_CV_Training");
//		statisticNames.add("MAE_CV_Training");
//		statisticNames.add("PearsonRSQ_CV_Training");
		statisticNames.add("PearsonRSQ_Test");
//		statisticNames.add("RMSE_Test");
//		 statisticNames.add("MAE_Test");

		for (String modelSetName : modelSetNames) {

			for (String statisticName : statisticNames) {

				Hashtable<String, Double> htVals = new Hashtable<>();
				for (String methodName : methodNames) {
					addHashtableEntry(statisticName, methodName, modelSetNames, datasetNames, htVals);
				}

				for (String methodName : methodNames) {
					// System.out.println("\n"+methodName);

					for (String datasetName : datasetNames) {
						if (modelSetName.equals("WebTEST2.0_justPFAS")) {
							addPFAS_Stats(statisticName, methodName, htVals, smilesArrayPFAS, datasetName,
									"WebTEST2.0");
						}

						if (modelSetName.equals("WebTEST2.1_justPFAS")) {
							addPFAS_Stats(statisticName, methodName, htVals, smilesArrayPFAS, datasetName,
									"WebTEST2.1");
						}
					}
				}

				// Create the summary table as text file:
				createSummaryTableForModelSet(statisticName, modelSetName, methodNames, datasetNames, htVals);
			}
		}

	}
	void createSummaryTableForMethod_Rnd_Representative() {
		// String statisticName = "MAE_Test";
		String statisticName = "PearsonRSQ_Test";

		List<String> modelSetNames = new ArrayList<>();

		modelSetNames.add("WebTEST2.0");// TODO calc stats just for PFAS
		modelSetNames.add("WebTEST2.1");

		List<String> datasetNames = new ArrayList<>();
		datasetNames.add("HLC from exp_prop and chemprop");
		datasetNames.add("WS from exp_prop and chemprop");
		datasetNames.add("VP from exp_prop and chemprop");
		datasetNames.add("LogP from exp_prop and chemprop");
		datasetNames.add("MP from exp_prop and chemprop");
		datasetNames.add("BP from exp_prop and chemprop");

		List<String> methodNames = new ArrayList<>();
		methodNames.add(DevQsarConstants.KNN);
		methodNames.add(DevQsarConstants.RF);
		methodNames.add(DevQsarConstants.XGB);
		methodNames.add(DevQsarConstants.SVM);
		methodNames.add(DevQsarConstants.CONSENSUS);

		Hashtable<String, Double> htVals = new Hashtable<>();
		for (String methodName : methodNames) {
			addHashtableEntry(statisticName, methodName, modelSetNames, datasetNames, htVals);
		}

		StringBuffer sb = new StringBuffer();

		// for (String modelSetName:modelSetNames) {
		// createSummaryTableForModelSet(statisticName, modelSetName, methodNames,
		// datasetNames, htVals,sb);
		// }
		//
		// System.out.println(sb.toString());

		try {
			FileWriter fw = new FileWriter("data/reports/" + statisticName + ".txt");
			fw.write(sb.toString());
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// for (String methodName:methodNames) {
		// createSummaryTableForMethod(statisticName, methodName, modelSetNames,
		// datasetNames, htVals);
		// }

		createSummaryTableForMethod(statisticName, DevQsarConstants.KNN, modelSetNames, datasetNames, htVals);

	}

	/**
	 * Stores stat in hashtable
	 * 
	 * @param statisticName
	 * @param methodName
	 * @param modelSetNames
	 * @param datasetNames
	 */
	private void addHashtableEntry(String statisticName, String methodName, List<String> modelSetNames,
			List<String> datasetNames, Hashtable<String, Double> htVals) {

		for (int i = 0; i < datasetNames.size(); i++) {
			String datasetName = datasetNames.get(i);

			for (int j = 0; j < modelSetNames.size(); j++) {
				String modelSetName = modelSetNames.get(j);

				if (modelSetName.contains("_justPFAS")) {
					// System.out.println("Skipping"+modelSetName);
					continue;
				}

				String key = methodName + "\t" + modelSetName + "\t" + datasetName;

				Long modelId = getModelId(modelSetName, datasetName, methodName);
				
//				System.out.println(modelId);
				
				if (modelId == null) {
					htVals.put(key, Double.NaN);
					continue;
				}

				Double stat = getStat(modelId, statisticName);

//				 System.out.println(modelId+"\t"+key+"\t"+stat);

				if (stat != null)
					htVals.put(key, stat);
				else
					htVals.put(key, Double.NaN);

			}
		}
	}

	// /**
	// * Stores stat in hashtable
	// *
	// * @param statisticName
	// * @param methodName
	// * @param modelSetNames
	// * @param datasetNames
	// */
	// private void addHashtableEntryLimitToPFAS(String statisticName, String
	// methodName, String datasetName,
	// Hashtable<String,Double>htVals,HashSet<String>smilesArrayPFAS) {
	// addPFAS_Stats(statisticName, methodName, htVals, smilesArrayPFAS,
	// datasetName, "WebTEST2.0");
	// addPFAS_Stats(statisticName, methodName, htVals, smilesArrayPFAS,
	// datasetName, "WebTEST2.1");
	// }

	private void addPFAS_Stats(String statisticName, String methodName, Hashtable<String, Double> htVals,
			HashSet<String> smilesArrayPFAS, String datasetName, String modelSetName) {

		System.out.println("Adding PFAS stats for " + datasetName + "\t" + modelSetName);

		String key = methodName + "\t" + modelSetName + "_justPFAS" + "\t" + datasetName;

		Long modelId = getModelId(modelSetName, datasetName, methodName);

		if (modelId == null) {
			htVals.put(key, Double.NaN);
			return;
		}

		Double stat = null;

		Model model = modelService.findById(modelId);

		if (statisticName.equals("Q2_CV_Training") || statisticName.equals(DevQsarConstants.PEARSON_RSQ_CV_TRAINING)) {
			stat = calculateCV_Stat_For_Just_PFAS(model, statisticName, smilesArrayPFAS);
		} else {
			stat = calcPredictionStatsForPFAS(model, statisticName, smilesArrayPFAS);
		}

		// System.out.println(key+"\t"+stat);

		if (stat != null)
			htVals.put(key, stat);
		else
			htVals.put(key, Double.NaN);

	}

	void createSummaryTableForMethodTEST() {

		List<String> modelSetNames = new ArrayList<>();

		String modelSetName = "WebTEST2.1 Sample models";

		List<String> methodNames = new ArrayList<>();
		methodNames.add(DevQsarConstants.KNN);
		methodNames.add(DevQsarConstants.RF);
		methodNames.add(DevQsarConstants.XGB);
		methodNames.add(DevQsarConstants.SVM);
		methodNames.add(DevQsarConstants.CONSENSUS);

		List<String> datasetNames = new ArrayList<>();
		datasetNames.add(DevQsarConstants.DEV_TOX + " TEST");
		datasetNames.add(DevQsarConstants.MUTAGENICITY + " TEST");
		datasetNames.add(DevQsarConstants.LLNA + " TEST");

		Hashtable<String, Double> htVals = new Hashtable<>();

		for (String methodName : methodNames) {
			addHashtableEntry("BA_Test", methodName, modelSetNames, datasetNames, htVals);
		}

		createSummaryTableForModelSet("BA_Test", modelSetName, methodNames, datasetNames, htVals);

		List<String> datasetNames2 = new ArrayList<>();
		datasetNames2.add(DevQsarConstants.LD50 + " TEST");
		datasetNames2.add(DevQsarConstants.LC50 + " TEST");
		datasetNames2.add(DevQsarConstants.LC50DM + " TEST");
		datasetNames2.add(DevQsarConstants.IGC50 + " TEST");

		for (String methodName : methodNames) {
			addHashtableEntry("PearsonRSQ_Test", methodName, modelSetNames, datasetNames2, htVals);
		}
		createSummaryTableForModelSet("PearsonRSQ_Test", modelSetName, methodNames, datasetNames, htVals);

	}

	String[] getDatasetSplittingNames(long modelId) {
		String[] values = new String[2];
		String sql = "select dataset_name,splitting_name  from qsar_models.models m\n" + "where m.id=" + modelId;
		ResultSet rs = DatabaseLookup.runSQL2(conn, sql);
		try {
			if (rs.next()) {
				values[0] = rs.getString(1);
				values[1] = rs.getString(2);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return values;
	}

	/**
	 * For splitting= "T=all, P=PFAS" generates the stats P=PFAS without having to
	 * make new models for this splitting (can use model for RND_REPRESENTATIVE)
	 * 
	 */
	public Double calcPredictionStatsForPFAS(Model model, String statisticName, HashSet<String> smilesArrayPFAS) {

		SplitPredictions sp = SplitPredictions.getSplitPredictionsSql(model, model.getSplittingName());

		// Remove non PFAS compounds:
		if (smilesArrayPFAS != null) {
			sp.removeNonPFAS(smilesArrayPFAS);
		}

		// for (int i=0;i<modelPredictions.size();i++) {
		// ModelPrediction mp=modelPredictions.get(i);
		// System.out.println(mp.ID+"\t"+mp.exp+"\t"+mp.pred);
		// }

		// ***************************************
		// Calc stats

		if (statisticName.equals(DevQsarConstants.Q2_F3_TEST)) {
			return ModelStatisticCalculator.calculateQ2_F3(sp.trainingSetPredictions, sp.testSetPredictions);
		}

		double mean_exp_training = ModelStatisticCalculator.calcMeanExpTraining(sp.trainingSetPredictions);
		Map<String, Double> statsMap = ModelStatisticCalculator.calculateContinuousStatistics(sp.testSetPredictions,
				mean_exp_training, DevQsarConstants.TAG_TEST);

		if (statsMap.get(statisticName) == null)
			return null;

		double stat = statsMap.get(statisticName);

		// System.out.println(datasetName+"\t"+"number of
		// preds="+modelPredictions.size());
		// System.out.println(statisticName+"="+stat);//need to make sure number of
		// chemicals matches excel table
		return stat;
	}

	/**
	 * 
	 * Calculates Q2_F3 see eqn 2 of Consonni et al, 2019
	 * (https://onlinelibrary.wiley.com/doi/full/10.1002/minf.201800029)
	 * 
	 * @param modelId
	 * @return
	 */
	public Double calculateCV_Stat_For_Just_PFAS(Model model, String stat, HashSet<String> smilesArrayPFAS) {

		double stat_Avg = 0;

		List<ModelPrediction> mpsTestSetPooled = new ArrayList<>();

		for (int i = 1; i <= 5; i++) {

			String splittingName = model.getSplittingName() + "_CV" + i;

			SplitPredictions sp = SplitPredictions.getSplitPredictionsSql(model, splittingName);

			if (smilesArrayPFAS != null) {
				sp.removeNonPFAS(smilesArrayPFAS);
			}

			mpsTestSetPooled.addAll(sp.testSetPredictions);

			// System.out.println("***"+sp.testSetPredictions.size()+"\t"+sp.trainingSetPredictions.size());

			// for (ModelPrediction mp:sp.trainingSetPredictions) {
			// System.out.println(mp.id+"\t"+mp.exp+"\t"+mp.pred);
			// }

			double stat_i = 0;

			if (stat.equals("Q2_CV_Training")) {
				stat_i = ModelStatisticCalculator.calculateQ2_F3(sp.trainingSetPredictions, sp.testSetPredictions);
			} else if (stat.equals(DevQsarConstants.PEARSON_RSQ_CV_TRAINING)) {
				double YbarTrain = ModelStatisticCalculator.calcMeanExpTraining(sp.trainingSetPredictions);
				Map<String, Double> mapStats = ModelStatisticCalculator
						.calculateContinuousStatistics(sp.testSetPredictions, YbarTrain, DevQsarConstants.TAG_TEST);
				stat_i = mapStats.get(DevQsarConstants.PEARSON_RSQ + DevQsarConstants.TAG_TEST);
			}

			// System.out.println("stat"+i+"="+stat_i);
			stat_Avg += stat_i;
		}
		stat_Avg /= 5.0;
		// System.out.println("stat_Avg="+stat_Avg);

		Map<String, Double> mapStats = ModelStatisticCalculator.calculateContinuousStatistics(mpsTestSetPooled, 0.0,
				DevQsarConstants.TAG_TEST);
		double R2_CV_pooled = mapStats.get(DevQsarConstants.PEARSON_RSQ + DevQsarConstants.TAG_TEST);

		if (stat.equals("Q2_CV_Training")) {
			return stat_Avg;
		} else if (stat.equals(DevQsarConstants.PEARSON_RSQ_CV_TRAINING)) {
//			System.out.println(stat_Avg + "\t" + R2_CV_pooled);
			return R2_CV_pooled;
		} else {
			return null;
		}

	}
	
	public void calculateCV_Stats_For_Just_PFAS(Model model, HashSet<String> smilesArrayPFAS, PredictionReportModelMetadata prmm) {

		double stat_Avg = 0;

		List<ModelPrediction> mpsTestSetPooled = new ArrayList<>();

		for (int i = 1; i <= 5; i++) {

			String splittingName = model.getSplittingName() + "_CV" + i;

			SplitPredictions sp = SplitPredictions.getSplitPredictionsSql(model, splittingName);

			if (smilesArrayPFAS != null) {
				sp.removeNonPFAS(smilesArrayPFAS);
			}

			mpsTestSetPooled.addAll(sp.testSetPredictions);

			// System.out.println("***"+sp.testSetPredictions.size()+"\t"+sp.trainingSetPredictions.size());

			// for (ModelPrediction mp:sp.trainingSetPredictions) {
			// System.out.println(mp.id+"\t"+mp.exp+"\t"+mp.pred);
			// }

//			double stat_i = 0;
//
//			if (stat.equals("Q2_CV_Training")) {
//				stat_i = ModelStatisticCalculator.calculateQ2_F3(sp.trainingSetPredictions, sp.testSetPredictions);
//			} else if (stat.equals(DevQsarConstants.PEARSON_RSQ_CV_TRAINING)) {
//				double YbarTrain = ModelStatisticCalculator.calcMeanExpTraining(sp.trainingSetPredictions);
//				Map<String, Double> mapStats = ModelStatisticCalculator
//						.calculateContinuousStatistics(sp.testSetPredictions, YbarTrain, DevQsarConstants.TAG_TEST);
//				stat_i = mapStats.get(DevQsarConstants.PEARSON_RSQ + DevQsarConstants.TAG_TEST);
//			}

			// System.out.println("stat"+i+"="+stat_i);
//			stat_Avg += stat_i;
		}
//		stat_Avg /= 5.0;
		// System.out.println("stat_Avg="+stat_Avg);

		Map<String, Double> mapStatsPooled = ModelStatisticCalculator.calculateContinuousStatistics(mpsTestSetPooled, 0.0,
				DevQsarConstants.TAG_TEST);
		
		double R2_CV_pooled = mapStatsPooled.get(DevQsarConstants.PEARSON_RSQ + DevQsarConstants.TAG_TEST);
		
		double MAE_CV_pooled = mapStatsPooled.get(DevQsarConstants.MAE + DevQsarConstants.TAG_TEST);

//		prmm.predictionReportModelStatistics
//				.add(new PredictionReportModelStatistic("Q2_CV_Training", Q2_CV_Training));

		prmm.predictionReportModelStatistics
				.add(new PredictionReportModelStatistic("MAE_CV_Training", MAE_CV_pooled));

		prmm.predictionReportModelStatistics.add(new PredictionReportModelStatistic(
				DevQsarConstants.PEARSON_RSQ_CV_TRAINING, R2_CV_pooled));
		

	}

	private List<ModelPrediction> mergeExpPredValues(Hashtable<String, Double> htPred, String datasetName,
			String splittingName) {
		String sql = "select dp.canon_qsar_smiles, dp. qsar_property_value from qsar_datasets.data_points dp\n"
				+ "join qsar_datasets.datasets d on dp.fk_dataset_id =d.id\n"
				+ "join qsar_datasets.data_points_in_splittings dpis on dpis.fk_data_point_id =dp.id\n"
				+ "join qsar_datasets.splittings s on s.id=dpis.fk_splitting_id\n" + "where d.\"name\" ='" + datasetName
				+ "' and dpis.split_num =1 and s.\"name\"='" + splittingName + "'";

		ResultSet rs = DatabaseLookup.runSQL2(conn, sql);
		List<ModelPrediction> modelPredictions = new ArrayList<>();

		try {
			while (rs.next()) {
				String ID = rs.getString(1);
				Double exp = Double.parseDouble(rs.getString(2));
				Double pred = htPred.get(ID);
				modelPredictions.add(new ModelPrediction(ID, exp, pred, 1));
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return modelPredictions;
	}

	void createPredictionReportsExcelForJustPFAS(String modelSetName, boolean upload,
			boolean deleteExistingReportInDatabase, boolean overWriteReportFiles, boolean overWriteExcelFiles,
			String filePathPFAS, boolean includeDescriptors) {

		HashSet<String> smilesArray = SplittingGeneratorPFAS_Script.getPFASSmiles(filePathPFAS);

		SampleReportWriter srw = new SampleReportWriter();

		String splittingName = DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
		// String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;//TODO get
		// from prediction report instead

		ModelSetServiceImpl modelSetService = new ModelSetServiceImpl();
		ModelSet ms = modelSetService.findByName(modelSetName);

		// Create the PredictionReport for all compounds in
		// SPLITTING_RND_REPRESENTATIVE:
		for (String datasetName : datasetNames) {
			srw.generateSamplePredictionReport(ms.getId(), datasetName, splittingName, upload,
					deleteExistingReportInDatabase, overWriteReportFiles, overWriteExcelFiles, includeDescriptors);
		}

		for (String datasetName : datasetNames) {

			if (datasetName.contains("pKa"))
				continue;

			PredictionReport predictionReport = SampleReportWriter.getReport(modelSetName, datasetName, splittingName);

			// Delete non PFAS from report and recalc stats:
			limitPredictionReportToPFAS(smilesArray, predictionReport);

			String filePath = "data/reports/" + modelSetName + "/" + datasetName + "_PredictionReport_only_PFAS.json";
			ReportGenerationScript.writeReport(predictionReport, filePath);

			// System.out.println(Utilities.gson.toJson(predictionReport));

			String outputFolder = "data/reports/prediction reports upload";

			String filepathExcel = outputFolder + File.separator + modelSetName + File.separator
					+ String.join("_", datasetName, splittingName) + "_PFAS.xlsx";

			if (!new File(filepathExcel).exists() || overWriteExcelFiles) {
				ExcelPredictionReportGenerator eprg = new ExcelPredictionReportGenerator();
				eprg.generate(predictionReport, filepathExcel, smilesArray,null);
				// TODO add code to upload this excel report
			} else {
				System.out.println("Exists:" + filepathExcel);
			}

		}

	}
	
	void createPredictionReportExcel(String modelSetName, String datasetName, boolean upload,
			boolean deleteExistingReportInDatabase, boolean overWriteReportFiles, boolean overWriteExcelFiles,
			boolean includeDescriptors) {

		SampleReportWriter srw = new SampleReportWriter();

		String splittingName = DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
		// String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;//TODO get
		// from prediction report instead

		ModelSetServiceImpl modelSetService = new ModelSetServiceImpl();
		ModelSet ms = modelSetService.findByName(modelSetName);

		// Create the PredictionReport for all compounds in
		// SPLITTING_RND_REPRESENTATIVE:
		
		srw.generateSamplePredictionReport(ms.getId(), datasetName, splittingName, upload,
					deleteExistingReportInDatabase, overWriteReportFiles, overWriteExcelFiles, includeDescriptors);
		

		
	}


	private void createPredictionReportsExcelPFASOnlyModels(String splittingName, String modelSetName,
			String filePathPFAS, boolean overWriteReportFiles, boolean overWriteExcelFiles,
			boolean includeDescriptors) {

		ModelSetServiceImpl modelSetService = new ModelSetServiceImpl();
		ModelSet ms = modelSetService.findByName(modelSetName);

		HashSet<String> smilesArray = SplittingGeneratorPFAS_Script.getPFASSmiles(filePathPFAS);

		SampleReportWriter srw = new SampleReportWriter();

		for (String datasetName : datasetNames) {

			PredictionReport predictionReport = srw.createPredictionReport(ms.getId(), datasetName, splittingName,
					overWriteReportFiles, includeDescriptors);

			// Get training and test set instances as strings using TEST descriptors:

			String outputFolder = "data/reports/prediction reports upload" + File.separator + modelSetName;
			File Folder = new File(outputFolder);
			Folder.mkdirs();

			String fileName = String.join("_", datasetName, splittingName) + ".xlsx";
			String filepathExcel = outputFolder + File.separator + fileName;

			if (!new File(filepathExcel).exists() || overWriteExcelFiles) {
				ExcelPredictionReportGenerator eprg = new ExcelPredictionReportGenerator();
				eprg.generate(predictionReport, filepathExcel, smilesArray,null);
				System.out.println("Created:" + filepathExcel);
			} else {
				System.out.println("Exists:" + filepathExcel);
			}

		}

	}

	void addMappedRecordsToExcel(String filepath, String dataSetName) {

		try {

			XSSFWorkbook wb = (XSSFWorkbook) WorkbookFactory.create(new File(filepath));

			String dataSetName2 = dataSetName.replace(" ", "_");
			String folder = "data\\dev_qsar\\output\\";
			String jsonPath = folder + "//" + dataSetName2 + "//" + dataSetName2 + "_Mapped_Records.json";

			JsonArray ja = Utilities.getJsonArrayFromJsonFile(jsonPath);

			ExcelCreator ec = new ExcelCreator();

			String[] fields = { "exp_prop_id", "canon_qsar_smiles", "page_url", "source_url", "source_doi",
					"source_name", "source_description", "source_type", "source_authors", "source_title",
					"source_dtxrid", "source_dtxsid", "source_casrn", "source_chemical_name", "source_smiles",
					"mapped_dtxcid", "mapped_dtxsid", "mapped_cas", "mapped_chemical_name", "mapped_smiles",
					"mapped_molweight", "value_original", "value_max", "value_min", "value_point_estimate",
					"value_units", "qsar_property_value", "qsar_property_units", "temperature_c", "pressure_mmHg", "pH",
					"notes", "qc_flag" };

			Hashtable<String, String> htDescriptions = ExcelCreator.getColumnDescriptions();

			ec.addSheet(wb, "Exp. Records", ja, fields, htDescriptions);

			System.out.println(wb.getNumberOfSheets());

			FileOutputStream fos = new FileOutputStream(filepath);
			wb.write(fos);
			wb.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void getStatsInsideAD(PredictionReport predictionReport,
			Hashtable<String, ApplicabilityDomainPrediction> htAD, HashSet<String> smilesArray) {


		// System.out.println(htAD.size());

		// //Delete old statistics:
		// for (PredictionReportModelMetadata
		// prmmd:predictionReport.predictionReportModelMetadata) {
		// prmmd.predictionReportModelStatistics.clear();
		// }

		Hashtable<String, List<ModelPrediction>> htModelPredictionsTestSet = new Hashtable<>();
		Hashtable<String, List<ModelPrediction>> htModelPredictionsTrainingSet = new Hashtable<>();

		for (int i = 0; i < predictionReport.predictionReportDataPoints.size(); i++) {
			PredictionReportDataPoint dp = predictionReport.predictionReportDataPoints.get(i);

			for (QsarPredictedValue qpv : dp.qsarPredictedValues) {

				if (qpv.splitNum == DevQsarConstants.TEST_SPLIT_NUM) {
					storePredictionInHashtable(htModelPredictionsTestSet, dp, qpv);
				}
				if (qpv.splitNum == DevQsarConstants.TRAIN_SPLIT_NUM) {
					storePredictionInHashtable(htModelPredictionsTrainingSet, dp, qpv);
				}
			}
		}

		boolean print = false;

		for (PredictionReportModelMetadata prmm : predictionReport.predictionReportModelMetadata) {

			// if (prmm.qsarMethodName.contains("consensus")) print=true;
			// else print=false;

			// System.out.println(prmm.qsarMethodName);

			List<ModelPrediction> trainingSetPredictions = htModelPredictionsTrainingSet.get(prmm.qsarMethodName);
			List<ModelPrediction> testSetPredictions = htModelPredictionsTestSet.get(prmm.qsarMethodName);

			for (int i = 0; i < testSetPredictions.size(); i++) {
				ModelPrediction mp = testSetPredictions.get(i);

				if (smilesArray != null) {
					if (!smilesArray.contains(mp.id)) {
						testSetPredictions.remove(i--);
					}
				}

				if (htAD.get(mp.id) != null) {
					ApplicabilityDomainPrediction ad = htAD.get(mp.id);
					if (!ad.AD)
						mp.pred = null;// stat calculations use null preds to calc coverage

					// System.out.println(mp.ID+"\t"+mp.AD);
				}

				// System.out.println(mp.ID+"\t"+mp.exp+"\t"+mp.pred);
			}

			for (int i = 0; i < trainingSetPredictions.size(); i++) {
				ModelPrediction mp = trainingSetPredictions.get(i);

				if (smilesArray != null) {
					if (!smilesArray.contains(mp.id)) {
						trainingSetPredictions.remove(i--);
					}
				}
				// System.out.println(mp.ID+"\t"+mp.exp+"\t"+mp.pred);
			}

			// System.out.println(testSetPredictions.size());
			// System.out.println(trainingSetPredictions.size());

			double meanExpTraining = calculateMeanExpTraining(trainingSetPredictions);

			Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator
					.calculateContinuousStatistics(testSetPredictions, meanExpTraining, DevQsarConstants.TAG_TEST);

			// Map<String, Double> modelTrainingStatisticValues =
			// ModelStatisticCalculator.calculateContinuousStatistics(
			// trainingSetPredictions, meanExpTraining, DevQsarConstants.TAG_TRAINING);

			if (print)
				System.out.println("test set");

			removeStat(prmm, "Coverage_Test");

			for (String statisticName : modelTestStatisticValues.keySet()) {

				double statisticValue = modelTestStatisticValues.get(statisticName);

				if (statisticName.equals("Coverage_Test")) {

					prmm.predictionReportModelStatistics
							.add(new PredictionReportModelStatistic(statisticName, statisticValue));

					if (print)
						System.out.println("\t" + statisticName + "\t" + statisticValue);

				} else {
					String statisticNameNew = statisticName + "_inside_AD";
					if (print)
						System.out.println("\t" + statisticNameNew + "\t" + statisticValue);
					prmm.predictionReportModelStatistics
							.add(new PredictionReportModelStatistic(statisticNameNew, statisticValue));
				}

			}

			if (print)
				System.out.println(Utilities.gson.toJson(prmm.predictionReportModelStatistics));

			//
			// if(print) System.out.println("\ntraining set");
			// for (String statisticName:modelTrainingStatisticValues.keySet()) {
			// if(print)
			// System.out.println("\t"+statisticName+"\t"+modelTestStatisticValues.get(statisticName));
			//
			// prmm.predictionReportModelStatistics.add(new
			// PredictionReportModelStatistic(statisticName,
			// modelTrainingStatisticValues.get(statisticName)));
			// }

			// System.out.println(Utilities.gson.toJson(prmm.predictionReportModelStatistics));

		} // end loop over model metadata
	}
	
	
	public static void addADsToReport(PredictionReport predictionReport,String methodName,Hashtable<String, ApplicabilityDomainPrediction> htAD) {
		
		for (int i=0;i<predictionReport.predictionReportDataPoints.size();i++) {				
			PredictionReportDataPoint dp=predictionReport.predictionReportDataPoints.get(i);
			
			for (QsarPredictedValue qpv:dp.qsarPredictedValues) {
				
//				System.out.println(methodName+"\t"+qpv.qsarMethodName+"\t"+qpv.qsarMethodName.contains(methodName));
				
				if(!qpv.qsarMethodName.equals(methodName)) continue;

				if(htAD.containsKey(dp.canonQsarSmiles)) {
					qpv.AD=htAD.get(dp.canonQsarSmiles).AD;
				}
			}
		}
	}

	public static void getStatsInsideAD(PredictionReport predictionReport,
			Hashtable<String, ApplicabilityDomainPrediction> htAD, HashSet<String> smilesArray, PredictionReportModelMetadata prmm) {
		boolean print = false;
		
		
		SplitPredictions sp = SplitPredictions.getSplitPredictions(predictionReport, prmm.qsarMethodName, smilesArray);
		
//		System.out.println(sp.trainingSetPredictions.size());
//		System.out.println(prmm.qsarMethodName+"\t"+sp.testSetPredictions.size());
		
		for (ModelPrediction mp : sp.testSetPredictions) {
			if (htAD.get(mp.id) != null) {
				
				ApplicabilityDomainPrediction ad = htAD.get(mp.id);
				
				if (!ad.AD)
					mp.pred = null;// stat calculations use null preds to calc coverage
				// System.out.println(mp.ID+"\t"+mp.AD);
			}
			// System.out.println(mp.ID+"\t"+mp.exp+"\t"+mp.pred);
		}

		double meanExpTraining = calculateMeanExpTraining(sp.trainingSetPredictions);

		Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator
				.calculateContinuousStatistics(sp.testSetPredictions, meanExpTraining, DevQsarConstants.TAG_TEST);

		// Map<String, Double> modelTrainingStatisticValues =
		// ModelStatisticCalculator.calculateContinuousStatistics(
		// trainingSetPredictions, meanExpTraining, DevQsarConstants.TAG_TRAINING);

		if (print)
			System.out.println("test set");

		removeStat(prmm, "Coverage_Test");

		for (String statisticName : modelTestStatisticValues.keySet()) {
			double statisticValue = modelTestStatisticValues.get(statisticName);

			if (statisticName.equals("Coverage_Test")) {
				prmm.predictionReportModelStatistics
						.add(new PredictionReportModelStatistic(statisticName, statisticValue));
				if (print)
					System.out.println("\t" + statisticName + "\t" + statisticValue);
			} else {
				String statisticNameNew = statisticName + "_inside_AD";
				if (print)
					System.out.println("\t" + statisticNameNew + "\t" + statisticValue);
				prmm.predictionReportModelStatistics
						.add(new PredictionReportModelStatistic(statisticNameNew, statisticValue));
			}
		}
		if (print)
			System.out.println(Utilities.gson.toJson(prmm.predictionReportModelStatistics));

	}

	private static void removeStat(PredictionReportModelMetadata prmm, String statName) {
		for (int i = 0; i < prmm.predictionReportModelStatistics.size(); i++) {
			PredictionReportModelStatistic prms = prmm.predictionReportModelStatistics.get(i);
			if (prms.statisticName.equals(statName)) {
				prmm.predictionReportModelStatistics.remove(i);
				break;
			}

		}
	}

	public static void getStatsOutsideAD(PredictionReport predictionReport,
			Hashtable<String, ApplicabilityDomainPrediction> htAD, HashSet<String> smilesArray) {


		// System.out.println(htAD.size());

		// //Delete old statistics:
		// for (PredictionReportModelMetadata
		// prmmd:predictionReport.predictionReportModelMetadata) {
		// prmmd.predictionReportModelStatistics.clear();
		// }

		Hashtable<String, List<ModelPrediction>> htModelPredictionsTestSet = new Hashtable<>();
		Hashtable<String, List<ModelPrediction>> htModelPredictionsTrainingSet = new Hashtable<>();

		for (int i = 0; i < predictionReport.predictionReportDataPoints.size(); i++) {
			PredictionReportDataPoint dp = predictionReport.predictionReportDataPoints.get(i);

			for (QsarPredictedValue qpv : dp.qsarPredictedValues) {

				if (qpv.splitNum == DevQsarConstants.TEST_SPLIT_NUM) {
					storePredictionInHashtable(htModelPredictionsTestSet, dp, qpv);
				}
				if (qpv.splitNum == DevQsarConstants.TRAIN_SPLIT_NUM) {
					storePredictionInHashtable(htModelPredictionsTrainingSet, dp, qpv);
				}
			}
		}

		boolean print = false;

		for (PredictionReportModelMetadata prmm : predictionReport.predictionReportModelMetadata) {

			// if (prmm.qsarMethodName.contains("consensus")) print=true;
			// else print=false;

			// System.out.println(prmm.qsarMethodName);

			List<ModelPrediction> trainingSetPredictions = htModelPredictionsTrainingSet.get(prmm.qsarMethodName);
			List<ModelPrediction> testSetPredictions = htModelPredictionsTestSet.get(prmm.qsarMethodName);

			int countPred = 0;

			for (int i = 0; i < testSetPredictions.size(); i++) {
				ModelPrediction mp = testSetPredictions.get(i);

				if (smilesArray != null) {
					if (!smilesArray.contains(mp.id)) {
						testSetPredictions.remove(i--);
					}
				}

				if (htAD.get(mp.id) != null) {
					ApplicabilityDomainPrediction ad = htAD.get(mp.id);
					if (ad.AD) {
						mp.pred = null;// stat calculations use null preds to calc coverage
					} else {
						countPred++;
					}

					// System.out.println(mp.ID+"\t"+mp.AD);
				}

				// System.out.println(mp.ID+"\t"+mp.exp+"\t"+mp.pred);
			}

			// System.out.println("countPred="+countPred);

			for (int i = 0; i < trainingSetPredictions.size(); i++) {
				ModelPrediction mp = trainingSetPredictions.get(i);

				if (smilesArray != null) {
					if (!smilesArray.contains(mp.id)) {
						trainingSetPredictions.remove(i--);
					}
				}
				// System.out.println(mp.ID+"\t"+mp.exp+"\t"+mp.pred);
			}

			// System.out.println(testSetPredictions.size());
			// System.out.println(trainingSetPredictions.size());

			double meanExpTraining = calculateMeanExpTraining(trainingSetPredictions);

			Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator
					.calculateContinuousStatistics(testSetPredictions, meanExpTraining, DevQsarConstants.TAG_TEST);

			// for (ModelPrediction mp:testSetPredictions) {
			// if (prmm.qsarMethodName.contains("consensus") && mp.pred!=null)
			// System.out.println(mp.exp+"\t"+mp.pred);
			// }

			// Map<String, Double> modelTrainingStatisticValues =
			// ModelStatisticCalculator.calculateContinuousStatistics(
			// trainingSetPredictions, meanExpTraining, DevQsarConstants.TAG_TRAINING);

			if (print)
				System.out.println("test set");
			for (String statisticName : modelTestStatisticValues.keySet()) {

				double statisticValue = modelTestStatisticValues.get(statisticName);

				if (statisticName.equals("Coverage_Test"))
					continue;
				else {
					String statisticNameNew = statisticName + "_outside_AD";
					if (print)
						System.out.println("\t" + statisticNameNew + "\t" + statisticValue);
					prmm.predictionReportModelStatistics
							.add(new PredictionReportModelStatistic(statisticNameNew, statisticValue));
				}

			}

			if (print)
				System.out.println(Utilities.gson.toJson(prmm.predictionReportModelStatistics));

			//
			// if(print) System.out.println("\ntraining set");
			// for (String statisticName:modelTrainingStatisticValues.keySet()) {
			// if(print)
			// System.out.println("\t"+statisticName+"\t"+modelTestStatisticValues.get(statisticName));
			//
			// prmm.predictionReportModelStatistics.add(new
			// PredictionReportModelStatistic(statisticName,
			// modelTrainingStatisticValues.get(statisticName)));
			// }

			// System.out.println(Utilities.gson.toJson(prmm.predictionReportModelStatistics));

		} // end loop over model metadata
	}

	public static void getStatsOutsideAD(PredictionReport predictionReport,
			Hashtable<String, ApplicabilityDomainPrediction> htAD, HashSet<String> smilesArray, PredictionReportModelMetadata prmm) {


		SplitPredictions sp = SplitPredictions.getSplitPredictions(predictionReport, prmm.qsarMethodName, smilesArray);
		boolean print = false;

		int countPred = 0;

		for (ModelPrediction mp : sp.testSetPredictions) {
			
//			System.out.println(mp.id+"\t"+mp.exp+"\t"+mp.pred+"\t"+htAD.get(mp.id));
			
			if (htAD.get(mp.id) != null) {
				ApplicabilityDomainPrediction ad = htAD.get(mp.id);
				if (ad.AD) {
					mp.pred = null;// stat calculations use null preds to calc coverage
				} else {
					countPred++;
				}
			}
		}

//		 System.out.println("countPred="+countPred);

		double meanExpTraining = calculateMeanExpTraining(sp.trainingSetPredictions);

		Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator
				.calculateContinuousStatistics(sp.testSetPredictions, meanExpTraining, DevQsarConstants.TAG_TEST);

		if (print)
			System.out.println("test set");
		for (String statisticName : modelTestStatisticValues.keySet()) {
			double statisticValue = modelTestStatisticValues.get(statisticName);

			if (statisticName.equals("Coverage_Test"))
				continue;
			else {
				String statisticNameNew = statisticName + "_outside_AD";
				// if(print) System.out.println("\t"+statisticNameNew+"\t"+statisticValue);
				// System.out.println("\t"+statisticNameNew+"\t"+statisticValue);
				prmm.predictionReportModelStatistics
						.add(new PredictionReportModelStatistic(statisticNameNew, statisticValue));
			}

		}

		if (print)
			System.out.println(Utilities.gson.toJson(prmm.predictionReportModelStatistics));

	}

	private void limitPredictionReportToPFAS(HashSet<String> smilesArray, PredictionReport predictionReport) {

		// Delete old statistics:
		for (PredictionReportModelMetadata prmmd : predictionReport.predictionReportModelMetadata) {
			prmmd.predictionReportModelStatistics.clear();
		}

		Hashtable<String, List<ModelPrediction>> htModelPredictionsTestSet = new Hashtable<>();
		Hashtable<String, List<ModelPrediction>> htModelPredictionsTrainingSet = new Hashtable<>();

		for (int i = 0; i < predictionReport.predictionReportDataPoints.size(); i++) {
			PredictionReportDataPoint dp = predictionReport.predictionReportDataPoints.get(i);

			if (!smilesArray.contains(dp.canonQsarSmiles)) {
				// System.out.println(dp.canonQsarSmiles);
				predictionReport.predictionReportDataPoints.remove(i--);
				continue;
			}

			for (QsarPredictedValue qpv : dp.qsarPredictedValues) {

				if (qpv.splitNum == DevQsarConstants.TEST_SPLIT_NUM) {
					storePredictionInHashtable(htModelPredictionsTestSet, dp, qpv);
				} else if (qpv.splitNum == DevQsarConstants.TRAIN_SPLIT_NUM) {
					storePredictionInHashtable(htModelPredictionsTrainingSet, dp, qpv);
				}
			}
		}

		for (PredictionReportModelMetadata prmm : predictionReport.predictionReportModelMetadata) {

			// System.out.println(prmm.qsarMethodName);

			List<ModelPrediction> trainingSetPredictions = htModelPredictionsTrainingSet.get(prmm.qsarMethodName);
			List<ModelPrediction> testSetPredictions = htModelPredictionsTestSet.get(prmm.qsarMethodName);

			// if (prmm.qsarMethodName.equals("knn_regressor_1.2")) {
			// for (ModelPrediction mp:testSetPredictions) {
			// System.out.println(mp.ID+"\t"+mp.exp+"\t"+mp.pred);
			// }
			// }

			double meanExpTraining = calculateMeanExpTraining(trainingSetPredictions);

			Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator
					.calculateContinuousStatistics(testSetPredictions, meanExpTraining, DevQsarConstants.TAG_TEST);

			double Q2_F3_TEST = ModelStatisticCalculator.calculateQ2_F3(trainingSetPredictions, testSetPredictions);
			modelTestStatisticValues.put(DevQsarConstants.Q2_F3_TEST, Q2_F3_TEST);

			Map<String, Double> modelTrainingStatisticValues = ModelStatisticCalculator.calculateContinuousStatistics(
					trainingSetPredictions, meanExpTraining, DevQsarConstants.TAG_TRAINING);

			for (String statisticName : modelTestStatisticValues.keySet()) {
				prmm.predictionReportModelStatistics.add(
						new PredictionReportModelStatistic(statisticName, modelTestStatisticValues.get(statisticName)));
			}

			for (String statisticName : modelTrainingStatisticValues.keySet()) {
				prmm.predictionReportModelStatistics.add(new PredictionReportModelStatistic(statisticName,
						modelTrainingStatisticValues.get(statisticName)));
			}

			Model model = modelService.findById(prmm.modelId);

			
//			double Q2_CV_Training = calculateCV_Stat_For_Just_PFAS(model, "Q2_CV_Training", smilesArray);
//			prmm.predictionReportModelStatistics
//					.add(new PredictionReportModelStatistic("Q2_CV_Training", Q2_CV_Training));
//			
//			
//			double MAE_CV_Training = calculateCV_Stat_For_Just_PFAS(model, "MAE_CV_Training", smilesArray);
//			prmm.predictionReportModelStatistics
//					.add(new PredictionReportModelStatistic("MAE_CV_Training", MAE_CV_Training));
//
//
//			double PearsonRSQ_CV_Training = calculateCV_Stat_For_Just_PFAS(model,
//					DevQsarConstants.PEARSON_RSQ_CV_TRAINING, smilesArray);
//			prmm.predictionReportModelStatistics.add(new PredictionReportModelStatistic(
//					DevQsarConstants.PEARSON_RSQ_CV_TRAINING, PearsonRSQ_CV_Training));
			
			
			calculateCV_Stats_For_Just_PFAS(model, smilesArray,prmm);

			// See if get same results as above if repull predictions from db:
			// double
			// PearsonRSQ_Test=calcPredictionStatsForPFAS(model,"PearsonRSQ_Test",smilesArray);
			// System.out.println(model.getMethod().getName()+",
			// PearsonRSQ_Test="+PearsonRSQ_Test);

		} // end loop over models
	}

	private String limitInstances(HashSet<String> smilesArray, String descriptors) {
		Reader inputString = new StringReader(descriptors);
		BufferedReader br = new BufferedReader(inputString);

		try {
			String header = br.readLine();

			String descriptorsNew = header + "\r\n";

			while (true) {
				String Line = br.readLine();
				if (Line == null)
					break;
				String smiles = Line.substring(0, Line.indexOf("\t"));
				if (!smilesArray.contains(smiles))
					continue;
				descriptorsNew += Line + "\r\n";

				System.out.println(smiles);

			}

			System.out.println("");

			return descriptorsNew;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private static double calculateMeanExpTraining(List<ModelPrediction> trainingSetPredictions) {
		double meanExpTraining = 0.0;
		int count = 0;
		for (ModelPrediction mp : trainingSetPredictions) {
			if (mp.exp != null) {
				meanExpTraining += mp.exp;
				count++;
			}
		}
		meanExpTraining /= count;
		return meanExpTraining;
	}

	private static void storePredictionInHashtable(Hashtable<String, List<ModelPrediction>> htModelPredictions,
			PredictionReportDataPoint dp, QsarPredictedValue qpv) {
		if (htModelPredictions.get(qpv.qsarMethodName) == null) {
			List<ModelPrediction> modelPredictions = new ArrayList<>();
			htModelPredictions.put(qpv.qsarMethodName, modelPredictions);
			modelPredictions.add(new ModelPrediction(dp.canonQsarSmiles, dp.experimentalPropertyValue,
					qpv.qsarPredictedValue, qpv.splitNum));
		} else {
			List<ModelPrediction> modelPredictions = htModelPredictions.get(qpv.qsarMethodName);
			modelPredictions.add(new ModelPrediction(dp.canonQsarSmiles, dp.experimentalPropertyValue,
					qpv.qsarPredictedValue, qpv.splitNum));
		}
	}

	private static void storePredictionInHashtable(List<ModelPrediction> mps, PredictionReportDataPoint dp,
			QsarPredictedValue qpv, String methodName) {

	}

	void createSpreadsheetExample() {
		File jsonFile = new File("data/reports/WebTEST2.0/HLC v1_PredictionReport_only_PFAS.json");

		PredictionReport predictionReport = null;

		try {
			predictionReport = Utilities.gson.fromJson(new FileReader(jsonFile), PredictionReport.class);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		File folder = new File("data\\reports\\prediction reports upload\\WebTEST2.0");
		folder.mkdirs();
		// e.generate(
		// predictionReport,folder.getAbsolutePath()+File.separator+"report.xlsx");

		ExcelPredictionReportGenerator e = new ExcelPredictionReportGenerator();

		e.generate(predictionReport,
				folder.getAbsolutePath() + File.separator + "sample_excel_less_decimal_places.xlsx", null,null);

	}

	void createPredictionReportsExcelForJustPFAS() {

		boolean overWriteReportFiles = false;
		boolean overWriteExcelFiles = true;
		boolean deleteExistingReportInDatabase = false;
		boolean upload = false;
		boolean includeDescriptors = true;

		String listName = "PFASSTRUCTV4";
		String folder = "data/dev_qsar/dataset_files/";
		String filePathPFAS = folder + listName + "_qsar_ready_smiles.txt";// TODO pass as parameter

		createPredictionReportsExcelForJustPFAS("WebTEST2.0", upload, deleteExistingReportInDatabase,
				overWriteReportFiles, overWriteExcelFiles, filePathPFAS, false);
		createPredictionReportsExcelForJustPFAS("WebTEST2.1", upload, deleteExistingReportInDatabase,
				overWriteReportFiles, overWriteExcelFiles, filePathPFAS, includeDescriptors);
	}
	
	void createPredictionReportExcel() {
		ModelSetServiceImpl modelSetService = new ModelSetServiceImpl();

		boolean overWriteReportFiles = false;
		boolean overWriteExcelFiles = false;
		boolean deleteExistingReportInDatabase = false;
		boolean upload = false;
		boolean includeDescriptors = true;

		String datasetName="datasetName";
		
		SampleReportWriter srw = new SampleReportWriter();
		String splittingName = DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
		ModelSet ms = modelSetService.findByName("WebTEST2.0");
		srw.generateSamplePredictionReport(ms.getId(), datasetName, splittingName, upload,
					deleteExistingReportInDatabase, overWriteReportFiles, overWriteExcelFiles, includeDescriptors);

		ms = modelSetService.findByName("WebTEST2.1");
		srw.generateSamplePredictionReport(ms.getId(), datasetName, splittingName, upload,
					deleteExistingReportInDatabase, overWriteReportFiles, overWriteExcelFiles, includeDescriptors);
		
	}
	
	void createExcelSummarysWithAD_JustPFAS() {
		boolean upload=false;
		boolean overwrite=true;
		
		String listName = "PFASSTRUCTV4";
		String folder = "data/dev_qsar/dataset_files/";
		String filePathPFAS = folder + listName + "_qsar_ready_smiles.txt";
		HashSet<String> smilesArray = SplittingGeneratorPFAS_Script.getPFASSmiles(filePathPFAS);

//		createExcelSummarysWithAD_JustPFAS("WebTEST2.0",smilesArray,upload,overwrite);
		createExcelSummarysWithAD_JustPFAS("WebTEST2.1",smilesArray,upload,overwrite);
	}

	private void createExcelSummarysWithAD_JustPFAS(String modelSetName, HashSet<String> smilesArray,boolean upload, boolean overwrite) {
		
			QsarModelsScript qms = new QsarModelsScript("tmarti02");
		
		String splittingName = DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
		// String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;//TODO get
		// from prediction report instead

		String outputFolder = "data/reports/prediction reports upload";
		ExcelPredictionReportGenerator eprg = new ExcelPredictionReportGenerator();

		Long fileTypeId=2L;//excel summary 
		String applicabilityDomain=DevQsarConstants.Applicability_Domain_TEST_Embedding_Euclidean;
		
		for (String datasetName : datasetNames) {

			String filePathReport="data/reports/" + modelSetName + "/" + datasetName + "_PredictionReport_with_AD.json";
			PredictionReport predictionReport = SampleReportWriter.getReport(filePathReport);
			String filepathExcel = outputFolder + File.separator + modelSetName + File.separator
					+ String.join("_", datasetName, splittingName) + "_with_AD.xlsx";
			
			File excelFile=new File(filepathExcel);
			
			System.out.println(!excelFile.exists()+"\t"+overwrite);
			
			if (!excelFile.exists() || overwrite) {
				System.out.println("generating excel file");
				eprg.generate(predictionReport, filepathExcel, null,applicabilityDomain);
			}
			


			Long modelId = getModelIdForReport(modelSetName, splittingName, datasetName, predictionReport);			
			
			if(modelId==null) {
				System.out.println("Cant associate model for "+datasetName+"\t"+splittingName+"\tmodelSetId="+modelSetName);
				return;
			}

			if(upload) {
				try {
					qms.uploadModelFile(modelId, fileTypeId, filepathExcel);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			filePathReport = "data/reports/" + modelSetName + "/" + datasetName + "_PredictionReport_only_PFAS_with_AD.json";
			
			if (!new File(filePathReport).exists()) continue;
			
			predictionReport = SampleReportWriter.getReport(filePathReport);
			filepathExcel = outputFolder + File.separator + modelSetName + File.separator
					+ String.join("_", datasetName, splittingName) + "_PFAS_with_AD.xlsx";
			
			
			excelFile=new File(filepathExcel);

			if (!excelFile.exists() || overwrite) {
				System.out.println("generating excel file");
				eprg.generate(predictionReport, filepathExcel, null,applicabilityDomain);
			}

		}
		
	}

	private Long getModelIdForReport(String modelSetName, String splittingName, String datasetName,
			PredictionReport predictionReport) {
		Long modelId=null;//modelId to associate report with
		
		for (PredictionReportModelMetadata mmd: predictionReport.predictionReportModelMetadata) {
			if(mmd.qsarMethodName.contains("consensus") || predictionReport.predictionReportModelMetadata.size()==1) {
				modelId=mmd.modelId;
				break;
			}
		}
		
		return modelId;
	}
	

	private void createExcelSummarysWithAD_OnlyPFAS() {
		
		String listName = "PFASSTRUCTV4";
		String folder = "data/dev_qsar/dataset_files/";
		String filePathPFAS = folder + listName + "_qsar_ready_smiles.txt";
		HashSet<String> smilesArray = SplittingGeneratorPFAS_Script.getPFASSmiles(filePathPFAS);

		String applicabilityDomain=DevQsarConstants.Applicability_Domain_TEST_Embedding_Euclidean;

//		createExcelSummarysWithAD_OnlyPFAS("WebTEST2.0 PFAS");
//		createExcelSummarysWithAD_OnlyPFAS("WebTEST2.0 All but PFAS");

		createExcelSummarysWithAD_OnlyPFAS("T=PFAS only, P=PFAS","WebTEST2.1 PFAS",smilesArray,applicabilityDomain);
		createExcelSummarysWithAD_OnlyPFAS("T=all but PFAS, P=PFAS", "WebTEST2.1 All but PFAS",smilesArray,applicabilityDomain);

	}
	
	private void createExcelSummarysWithAD_OnlyPFAS(String splittingName, String modelSetName,HashSet<String> smilesArray,String applicabilityDomain) {
		
		// String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;//TODO get
		// from prediction report instead

		String outputFolder = "data/reports/prediction reports upload";
		ExcelPredictionReportGenerator eprg = new ExcelPredictionReportGenerator();
		
		for (String datasetName : datasetNames) {

			String filePathReport="data/reports/" + modelSetName + "/" + datasetName + "_PredictionReport_with_AD.json";
			PredictionReport predictionReport = SampleReportWriter.getReport(filePathReport);
			String filepathExcel = outputFolder + File.separator + modelSetName + File.separator
					+ String.join("_", datasetName, splittingName) + "_with_AD.xlsx";
			eprg.generate(predictionReport, filepathExcel, smilesArray,applicabilityDomain);


		}
		
	}

	void createPredictionReportsExcelPFASOnlyModels() {
		String listName = "PFASSTRUCTV4";
		String folder = "data/dev_qsar/dataset_files/";
		String filePathPFAS = folder + listName + "_qsar_ready_smiles.txt";

		boolean overWriteReportFiles = false;
		boolean overWriteExcelFiles = true;
		boolean includeDescriptors = true;

		createPredictionReportsExcelPFASOnlyModels("T=PFAS only, P=PFAS", "WebTEST2.0 PFAS", filePathPFAS,
				overWriteReportFiles, overWriteExcelFiles, false);
		createPredictionReportsExcelPFASOnlyModels("T=all but PFAS, P=PFAS", "WebTEST2.0 All but PFAS", filePathPFAS,
				overWriteReportFiles, overWriteExcelFiles, false);

		createPredictionReportsExcelPFASOnlyModels("T=PFAS only, P=PFAS", "WebTEST2.1 PFAS", filePathPFAS,
				overWriteReportFiles, overWriteExcelFiles, includeDescriptors);

		createPredictionReportsExcelPFASOnlyModels("T=all but PFAS, P=PFAS", "WebTEST2.1 All but PFAS", filePathPFAS,
				overWriteReportFiles, overWriteExcelFiles, includeDescriptors);
	}

	void copyReportsToFolder() {

		File srcFolder = new File("data\\reports\\prediction reports upload\\");
		File destFolder = new File("data\\reportsForPaperV1");

		List<String> modelSetNames = new ArrayList<>();

		modelSetNames.add("WebTEST2.0 PFAS");
		modelSetNames.add("WebTEST2.0 All but PFAS");
		modelSetNames.add("WebTEST2.0");
		modelSetNames.add("WebTEST2.1 PFAS");
		modelSetNames.add("WebTEST2.1 All but PFAS");
		modelSetNames.add("WebTEST2.1");

		for (String modelSetName : modelSetNames) {

			File folder = new File(srcFolder.getAbsolutePath() + File.separator + modelSetName);

			for (String datasetName : datasetNames) {

				if (modelSetName.equals("WebTEST2.0")) {
					File fileSrc1 = new File(
							folder.getAbsolutePath() + File.separator + datasetName + "_RND_REPRESENTATIVE.xlsx");
					File fileSrc2 = new File(
							folder.getAbsolutePath() + File.separator + datasetName + "_RND_REPRESENTATIVE_PFAS.xlsx");

					File fileDest1 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
							+ File.separator + datasetName + "_T=all_P=all.xlsx");
					File fileDest2 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
							+ File.separator + datasetName + "_T=all_P=PFAS.xlsx");
					FileUtils.copyFile(fileSrc1, fileDest1);
					FileUtils.copyFile(fileSrc2, fileDest2);

				} else if (modelSetName.equals("WebTEST2.0 PFAS")) {
					File fileSrc1 = new File(
							folder.getAbsolutePath() + File.separator + datasetName + "_T=PFAS only, P=PFAS.xlsx");
					File fileDest1 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
							+ File.separator + datasetName + "_T=PFAS_P=PFAS.xlsx");
					FileUtils.copyFile(fileSrc1, fileDest1);
				} else if (modelSetName.equals("WebTEST2.0 All but PFAS")) {
					File fileSrc1 = new File(
							folder.getAbsolutePath() + File.separator + datasetName + "_T=all but PFAS, P=PFAS.xlsx");
					File fileDest1 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
							+ File.separator + datasetName + "_T=all but PFAS_P=PFAS.xlsx");
					FileUtils.copyFile(fileSrc1, fileDest1);
				} else if (modelSetName.equals("WebTEST2.1")) {
					File fileSrc1 = new File(
							folder.getAbsolutePath() + File.separator + datasetName + "_RND_REPRESENTATIVE.xlsx");
					File fileSrc2 = new File(
							folder.getAbsolutePath() + File.separator + datasetName + "_RND_REPRESENTATIVE_PFAS.xlsx");
					File fileDest1 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
							+ File.separator + datasetName + "_T=all_P=all_embedding.xlsx");
					File fileDest2 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
							+ File.separator + datasetName + "_T=all_P=PFAS_embedding.xlsx");
					FileUtils.copyFile(fileSrc1, fileDest1);
					FileUtils.copyFile(fileSrc2, fileDest2);
				} else if (modelSetName.equals("WebTEST2.1 PFAS")) {
					File fileSrc1 = new File(
							folder.getAbsolutePath() + File.separator + datasetName + "_T=PFAS only, P=PFAS.xlsx");
					File fileDest1 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
							+ File.separator + datasetName + "_T=PFAS_P=PFAS_embedding.xlsx");
					FileUtils.copyFile(fileSrc1, fileDest1);
				} else if (modelSetName.equals("WebTEST2.1 All but PFAS")) {
					File fileSrc1 = new File(
							folder.getAbsolutePath() + File.separator + datasetName + "_T=all but PFAS, P=PFAS.xlsx");
					File fileDest1 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
							+ File.separator + datasetName + "_T=all but PFAS_P=PFAS_embedding.xlsx");
					FileUtils.copyFile(fileSrc1, fileDest1);
				}

			}

		}

	}
	
	void copyReportsToFolderWithAD() {

		File srcFolder = new File("data\\reports\\prediction reports upload\\");
		File destFolder = new File("data\\reportsForPaperV1");

		List<String> modelSetNames = new ArrayList<>();

//		modelSetNames.add("WebTEST2.0 PFAS");
//		modelSetNames.add("WebTEST2.0 All but PFAS");
//		modelSetNames.add("WebTEST2.0");
		modelSetNames.add("WebTEST2.1 PFAS");
		modelSetNames.add("WebTEST2.1 All but PFAS");
		modelSetNames.add("WebTEST2.1");

		for (String modelSetName : modelSetNames) {

			File folder = new File(srcFolder.getAbsolutePath() + File.separator + modelSetName);

			for (String datasetName : datasetNames) {

				if (modelSetName.equals("WebTEST2.0")) {
					File fileSrc1 = new File(
							folder.getAbsolutePath() + File.separator + datasetName + "_RND_REPRESENTATIVE_with_AD.xlsx");
					File fileSrc2 = new File(
							folder.getAbsolutePath() + File.separator + datasetName + "_RND_REPRESENTATIVE_PFAS_with_AD.xlsx");

					File fileDest1 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
							+ File.separator + datasetName + "_T=all_P=all_with_AD.xlsx");
					File fileDest2 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
							+ File.separator + datasetName + "_T=all_P=PFAS_with_AD.xlsx");
					FileUtils.copyFile(fileSrc1, fileDest1);
					FileUtils.copyFile(fileSrc2, fileDest2);

				} else if (modelSetName.equals("WebTEST2.0 PFAS")) {
					File fileSrc1 = new File(
							folder.getAbsolutePath() + File.separator + datasetName + "_T=PFAS only, P=PFAS_with_AD.xlsx");
					File fileDest1 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
							+ File.separator + datasetName + "_T=PFAS_P=PFAS_with_AD.xlsx");
					FileUtils.copyFile(fileSrc1, fileDest1);
				} else if (modelSetName.equals("WebTEST2.0 All but PFAS")) {
					File fileSrc1 = new File(
							folder.getAbsolutePath() + File.separator + datasetName + "_T=all but PFAS, P=PFAS_with_AD.xlsx");
					File fileDest1 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
							+ File.separator + datasetName + "_T=all but PFAS_P=PFAS_with_AD.xlsx");
					FileUtils.copyFile(fileSrc1, fileDest1);
				} else if (modelSetName.equals("WebTEST2.1")) {
					File fileSrc1 = new File(
							folder.getAbsolutePath() + File.separator + datasetName + "_RND_REPRESENTATIVE_with_AD.xlsx");
					File fileSrc2 = new File(
							folder.getAbsolutePath() + File.separator + datasetName + "_RND_REPRESENTATIVE_PFAS_with_AD.xlsx");
					File fileDest1 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
							+ File.separator + datasetName + "_T=all_P=all_embedding_with_AD.xlsx");
					File fileDest2 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
							+ File.separator + datasetName + "_T=all_P=PFAS_embedding_with_AD.xlsx");
					FileUtils.copyFile(fileSrc1, fileDest1);
					FileUtils.copyFile(fileSrc2, fileDest2);
				} else if (modelSetName.equals("WebTEST2.1 PFAS")) {
					File fileSrc1 = new File(
							folder.getAbsolutePath() + File.separator + datasetName + "_T=PFAS only, P=PFAS_with_AD.xlsx");
					File fileDest1 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
							+ File.separator + datasetName + "_T=PFAS_P=PFAS_embedding_with_AD.xlsx");
					FileUtils.copyFile(fileSrc1, fileDest1);
				} else if (modelSetName.equals("WebTEST2.1 All but PFAS")) {
					File fileSrc1 = new File(
							folder.getAbsolutePath() + File.separator + datasetName + "_T=all but PFAS, P=PFAS_with_AD.xlsx");
					File fileDest1 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
							+ File.separator + datasetName + "_T=all but PFAS_P=PFAS_embedding_with_AD.xlsx");
					FileUtils.copyFile(fileSrc1, fileDest1);
				}

			}

		}

	}

	void copyReportsToFolderWithAD_CSS_Delivery() {

		File srcFolder = new File("data\\reports\\prediction reports upload\\");
		File destFolder = new File("data\\reportsForPaperV1");

		List<String> modelSetNames = new ArrayList<>();

//		modelSetNames.add("WebTEST2.0 PFAS");
//		modelSetNames.add("WebTEST2.0 All but PFAS");
//		modelSetNames.add("WebTEST2.0");
//		modelSetNames.add("WebTEST2.1 PFAS");
//		modelSetNames.add("WebTEST2.1 All but PFAS");
		modelSetNames.add("WebTEST2.1");

		for (String modelSetName : modelSetNames) {

			File folder = new File(srcFolder.getAbsolutePath() + File.separator + modelSetName);

			for (String datasetName : datasetNames) {

//				if (modelSetName.equals("WebTEST2.0")) {
//					File fileSrc1 = new File(
//							folder.getAbsolutePath() + File.separator + datasetName + "_RND_REPRESENTATIVE_with_AD.xlsx");
//					File fileSrc2 = new File(
//							folder.getAbsolutePath() + File.separator + datasetName + "_RND_REPRESENTATIVE_PFAS_with_AD.xlsx");
//
//					File fileDest1 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
//							+ File.separator + datasetName + "_T=all_P=all_with_AD.xlsx");
//					File fileDest2 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
//							+ File.separator + datasetName + "_T=all_P=PFAS_with_AD.xlsx");
//					FileUtils.copyFile(fileSrc1, fileDest1);
//					FileUtils.copyFile(fileSrc2, fileDest2);
//
//				} else if (modelSetName.equals("WebTEST2.0 PFAS")) {
//					File fileSrc1 = new File(
//							folder.getAbsolutePath() + File.separator + datasetName + "_T=PFAS only, P=PFAS_with_AD.xlsx");
//					File fileDest1 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
//							+ File.separator + datasetName + "_T=PFAS_P=PFAS_with_AD.xlsx");
//					FileUtils.copyFile(fileSrc1, fileDest1);
//				} else if (modelSetName.equals("WebTEST2.0 All but PFAS")) {
//					File fileSrc1 = new File(
//							folder.getAbsolutePath() + File.separator + datasetName + "_T=all but PFAS, P=PFAS_with_AD.xlsx");
//					File fileDest1 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
//							+ File.separator + datasetName + "_T=all but PFAS_P=PFAS_with_AD.xlsx");
//					FileUtils.copyFile(fileSrc1, fileDest1);
//				} else if (modelSetName.equals("WebTEST2.1")) {
				if (modelSetName.equals("WebTEST2.1")) {
				
					File fileSrc1 = new File(
							folder.getAbsolutePath() + File.separator + datasetName + "_RND_REPRESENTATIVE_with_AD.xlsx");
					File fileSrc2 = new File(
							folder.getAbsolutePath() + File.separator + datasetName + "_RND_REPRESENTATIVE_PFAS_with_AD.xlsx");
					
					File fileDest1 = new File(destFolder.getAbsolutePath() + File.separator+"0 CSS Delivery"+File.separator 
							+ "Excel summaries all classes"+ File.separator + datasetName + "_T=all_P=all.xlsx");
					File fileDest2 = new File(destFolder.getAbsolutePath() + File.separator+"0 CSS Delivery"+File.separator
							+ "Excel summaries only PFAS" + File.separator + datasetName + "_T=all_P=PFAS.xlsx");
					
					
					FileUtils.copyFile(fileSrc1, fileDest1);
					FileUtils.copyFile(fileSrc2, fileDest2);
				} 
//				else if (modelSetName.equals("WebTEST2.1 PFAS")) {
//					File fileSrc1 = new File(
//							folder.getAbsolutePath() + File.separator + datasetName + "_T=PFAS only, P=PFAS_with_AD.xlsx");
//					File fileDest1 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
//							+ File.separator + datasetName + "_T=PFAS_P=PFAS_embedding_with_AD.xlsx");
//					FileUtils.copyFile(fileSrc1, fileDest1);
//				} else if (modelSetName.equals("WebTEST2.1 All but PFAS")) {
//					File fileSrc1 = new File(
//							folder.getAbsolutePath() + File.separator + datasetName + "_T=all but PFAS, P=PFAS_with_AD.xlsx");
//					File fileDest1 = new File(destFolder.getAbsolutePath() + File.separator + datasetName
//							+ File.separator + datasetName + "_T=all but PFAS_P=PFAS_embedding_with_AD.xlsx");
//					FileUtils.copyFile(fileSrc1, fileDest1);
//				}

			}

		}

	}
	
	
	/**
	 * Give multiple stats for one model set in same table
	 */
	void createSummaryTableForSet3() {

		boolean limitToPFAS=false;
		 String modelSetName="WebTEST2.1";
//		String modelSetName = "WebTEST2.1 PFAS";
//		String modelSetName="WebTEST2.1 All but PFAS";

//		String methodName="consensus";
		String methodName="rf";
		
		List<String>statNames=new ArrayList<>();
//		statNames.add("MAE_CV_Training");
		statNames.add("MAE_Test");
		statNames.add("MAE_Test_inside_AD");
		statNames.add("MAE_Test_outside_AD");
		statNames.add("Coverage_Test");

		String header="Dataset\t";
		
		for (int i=0;i<statNames.size();i++) {
			header+=statNames.get(i);
			if(i<statNames.size()-1) header+="\t";
		}

		System.out.println("\n"+modelSetName);
		System.out.println(header);
		DecimalFormat df=new DecimalFormat("0.00");
		
		for (String datasetName:datasetNames) {
			
			String reportPath="data/reports/"+modelSetName+"/"+datasetName+"_PredictionReport_";
			
			if(limitToPFAS) reportPath+="only_PFAS_";
			
			reportPath+="with_AD.json";
					
			
			File reportFile=new File(reportPath);
			
				
			PredictionReport predictionReport = SampleReportWriter.getReport(reportPath);
			

			String values=datasetName+"\t";
			
			for (int i=0;i<statNames.size();i++) {
				values+=df.format(getStat(predictionReport, statNames.get(i),methodName));
				if(i<statNames.size()-1) values+="\t";
			}

			System.out.println(values);
			
			
		}
		
		
	}
	
	Double getStat(PredictionReport report,String statName,String methodName) {
		
		for(PredictionReportModelMetadata prmm: report.predictionReportModelMetadata) {
			
			if(prmm.qsarMethodName.contains(methodName)) {
				for(PredictionReportModelStatistic prms:prmm.predictionReportModelStatistics) {
					if(prms.statisticName.equals(statName)) return prms.statisticValue;
				}
			}
			
		}
		return null;
		
	}
	
	
	void createkNN_Reports() {
		
		SampleReportWriter srw = new SampleReportWriter();

//		String modelSetName="WebTEST2.0";
		String modelSetName="WebTEST2.1";
		String methodName="knn_regressor_1.2";
		String splittingName="RND_REPRESENTATIVE";
		
		boolean includeDescriptors=false;
		boolean includeOriginalCompounds=true;
		boolean overwriteJsonReport=true;
		
		for (String dataset:datasetNames) {
			srw.createPredictionReportMethod(modelSetName,methodName, dataset, splittingName, overwriteJsonReport, includeDescriptors,includeOriginalCompounds);
		}
		
//		String dataset="MP v1 modeling";
//		srw.createPredictionReportMethod(modelSetName,methodName, dataset, splittingName, overwriteJsonReport, includeDescriptors,includeOriginalCompounds);		
	}
	
	/**
	 * Loops through Excel summary files, figures out matching model ids and uploads to model files table
	 */
	void uploadFinalExcelSummaries() {

		Long fileTypeId=2L;
		String splittingName=DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
		
		File folder=new File("data\\reports\\prediction reports upload\\WebTEST2.1\\");
		ModelServiceImpl ms=new ModelServiceImpl();
//		ModelInConsensusModelService micms=new ModelInConsensusMethodServiceImpl();
		QsarModelsScript qms=new QsarModelsScript("tmarti02");
		
		List<Model>models=ms.getAll();
		
		boolean hasEmbedding=true;//WebTEST2.1
		
		
		for (File file:folder.listFiles()) {
			
			if (file.getName().contains("_PFAS")) continue;//skip PFAS files
			if (!file.getName().contains("_with_AD")) continue;//need summaries with AD included
			
			String datasetName=file.getName();
			datasetName=datasetName.substring(0,datasetName.indexOf("_"));
			
			Model model=getMatchingModel(splittingName, models, datasetName,hasEmbedding);
			//TODO just get from model name once the final models have been renamed...
			
			if(model!=null) {
				System.out.println(file.getName()+"\t"+datasetName+"\t"+model.getId());
			}
			
			try {
				qms.uploadModelFile(model.getId(), fileTypeId, file.getAbsolutePath());
				
				String sql="update qsar_models.models set is_public=true where id="+model.getId()+";";
				SqlUtilities.runSQLUpdate(SqlUtilities.getConnectionPostgres(), sql);
				
				sql="update qsar_models.models set fk_ad_method=7 where id="+model.getId()+";";
				SqlUtilities.runSQLUpdate(SqlUtilities.getConnectionPostgres(), sql);
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	public static Model getMatchingModel(String splittingName, ModelInConsensusModelService micms, List<Model> allModels,
			String datasetName) {

		for(Model model:allModels) {
			
			if(!model.getDatasetName().equals(datasetName)) continue;
			if(!model.getSplittingName().equals(splittingName)) continue;
			if(!model.getMethod().getName().toLowerCase().contains("consensus")) continue;
			
			List<ModelInConsensusModel>modelsInConsensus=micms.findByConsensusModelId(model.getId());//note currently it doesnt lazy load them into model object by default so do a query
			
			for(ModelInConsensusModel modelInConsensus:modelsInConsensus) {
				if (modelInConsensus.getModel().getDescriptorEmbedding()!=null) {//needs to be a consensus of model that have embeddings
					return model;
				}
			}
		}
		
		return null;
	}
	
	public static Model getMatchingModel(String splittingName, List<Model> allModels,
			String datasetName,boolean hasEmbedding) {

		for(Model model:allModels) {
			
			if(!model.getDatasetName().equals(datasetName)) continue;
			if(!model.getSplittingName().equals(splittingName)) continue;
			
			if(hasEmbedding && model.getDescriptorEmbedding()!=null) return model;
			if(!hasEmbedding && model.getDescriptorEmbedding()==null) return model;
		}		
		return null;
	}

	
	/**
	 * Loops through QMRF files, figures out matching model ids and uploads to model files table
	 */
	void uploadFinalQmrfs() {

		Long fileTypeId=1L;//qmrf
		String splittingName=DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
		
		File folder=new File("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2023 8.4.11 papers\\000 2024 8.4.11 Revised Product Delivery\\files for delivery\\QMRFs\\");
		ModelServiceImpl ms=new ModelServiceImpl();
//		ModelInConsensusModelService micms=new ModelInConsensusMethodServiceImpl();
		QsarModelsScript qms=new QsarModelsScript("tmarti02");
		
		List<Model>models=ms.getAll();
		
		for (File file:folder.listFiles()) {
			
			if (!file.getName().contains(".pdf")) continue;
			
			String abbrev=file.getName().substring(0,file.getName().indexOf("_"));
			String datasetName=abbrev+" v1 modeling";
			
//			Model model=getMatchingModel(splittingName, micms, models, datasetName);
			
			Model model=getMatchingModel(splittingName, models, datasetName,true);

			
			if(model!=null) {
				System.out.println(file.getName()+"\t"+datasetName+"\t"+model.getId());
			}
			
			try {
				qms.uploadModelFile(model.getId(), fileTypeId, file.getAbsolutePath());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	public static void main(String[] args) {
		PredictionStatisticsScript ms = new PredictionStatisticsScript();
		// ms.createSpreadsheetExample();

		// ms.createSummaryTableForMethod_Rnd_Representative();

		 ms.createSummaryTableForMethod();
//		 ms.createSummaryTableForSet();
//		 ms.createSummaryTableForSet2();
//		 ms.createSummaryTableForSet3();

//		 ms.createSummaryTableForSetSermacs();
		 
		// ms.createSummaryTableForMethod_PFAS();

		// ms.createSummaryTableForSet();
		// ms.createSummaryTableForSetOPERA();

		//Create summary spreadsheets:
//		ms.createPredictionReportsExcelForJustPFAS();
//		ms.createPredictionReportsExcelPFASOnlyModels();
//		ms.copyReportsToFolder();
							
//		ms.createkNN_Reports();
		
		//Create spreadsheets with AD added in:
//		ms.createExcelSummarysWithAD_JustPFAS();
//		ms.createExcelSummarysWithAD_OnlyPFAS();
//		ms.copyReportsToFolderWithAD();
//		ms.copyReportsToFolderWithAD_CSS_Delivery();
		
//		ms.uploadFinalExcelSummaries();
//		ms.uploadFinalQmrfs();
		
		// ms.createSummaryTableForMethodTEST();
		// RecalcStatsScript.viewPredsForSplitSet(1111L);
	}

}
