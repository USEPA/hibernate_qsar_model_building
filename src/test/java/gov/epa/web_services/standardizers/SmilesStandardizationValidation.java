package gov.epa.web_services.standardizers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.exception.CDKException;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoException;
import com.epam.indigo.IndigoInchi;
import com.epam.indigo.IndigoObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.run_from_java.scripts.GetExpPropInfo.ExcelCreator;
import gov.epa.util.ParseStringUtils;
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer.SciDataExpertsStandardization;
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer.StandardizeResult;
import gov.epa.web_services.standardizers.Standardizer.StandardizeResponseWithStatus;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class SmilesStandardizationValidation {



	
	void go(String server, String workFlow, String folder, int start, int stop) {

		String structureFileName = "DSSTox_082021_Structures.csv";

		try {

			Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
			BufferedReader br = new BufferedReader(new FileReader(folder + structureFileName));
			String header = br.readLine().replace("\"", "");

			// "DSSTOX_COMPOUND_ID","Original_SMILES","Number of connected
			// components","Canonical_QSARr","Salt_Solvent","InChI_Code_QSARr","InChI
			// Key_QSARr","Salt_Solvent_ID"

			String[] fields = header.split(",");
			LinkedList<String> headers = ParseStringUtils.Parse3(header, ",");
			Hashtable<String, Integer> htCols = new Hashtable<>();
			for (int i = 0; i < headers.size(); i++) {
				htCols.put(fields[i], Integer.valueOf(i));
			}

			Indigo indigo = new Indigo();

			int currentLine = 1;

			JsonArray jaResults = new JsonArray();

			while (true) {

				String line = br.readLine();

				if (currentLine % 10 == 0)
					System.out.println(currentLine);

				if (currentLine < start)
					continue;

				if (currentLine > stop)
					break;

				LinkedList<String> values = ParseStringUtils.Parse3(line, ",");

				String DSSTOX_COMPOUND_ID = values.get(htCols.get("DSSTOX_COMPOUND_ID"));
				String Original_SMILES = values.get(htCols.get("Original_SMILES"));
				String QSAR_Ready_SMILES_OPERA = values.get(htCols.get("Canonical_QSARr"));
				String QSAR_Ready_InchiKey_OPERA = values.get(htCols.get("InChI Key_QSARr"));// we recalculate it
				// anyways

//	        	String QSAR_Ready_SMILES_SDE = runStandardize(Original_SMILES, server, workFlow,false);

	        	String QSAR_Ready_SMILES_SDE = SciDataExpertsStandardizer.runStandardize(Original_SMILES, server, workFlow,false).qsarReadySmiles;
	        	
	        	
				try {
					JsonObject jo = new JsonObject();
					jo.addProperty("DSSTOX_COMPOUND_ID", DSSTOX_COMPOUND_ID);
					jo.addProperty("Original_SMILES", Original_SMILES);
					jo.addProperty("QSAR_Ready_SMILES_OPERA", QSAR_Ready_SMILES_OPERA);

					Inchi inchiOPERA = toInchiIndigo(QSAR_Ready_SMILES_OPERA);
					if (inchiOPERA != null) {
						String inchiKey_OPERA = inchiOPERA.inchiKey;
						jo.addProperty("inchiKey_OPERA", inchiKey_OPERA);
					} else {
						jo.addProperty("inchiKey_OPERA", "N/A");
					}

					if (QSAR_Ready_SMILES_SDE==null) {
						jo.addProperty("QSAR_Ready_SMILES_SDE", "N/A");
						jo.addProperty("inchiKey_SDE", "N/A");
						// System.out.println(gson.toJson(jo));
					} else {
						String inchiKey_SDE_Canonical_Indigo = toInchiIndigo(QSAR_Ready_SMILES_SDE).inchiKey;
						jo.addProperty("QSAR_Ready_SMILES_SDE", QSAR_Ready_SMILES_SDE);
						jo.addProperty("inchiKey_SDE", inchiKey_SDE_Canonical_Indigo);
					}

					jaResults.add(jo);

				} catch (Exception ex) {
					ex.printStackTrace();
					continue;
				}
				currentLine++;
			}

			File folder2 = new File(folder + "//results_"
					+ server.replace("https://", "").replace("http://v2626umcth819.rtord.epa.gov:443", "819") + "_"
					+ workFlow);
			folder2.mkdirs();

			String outputFilePath = folder2.getAbsolutePath() + "//results_" + start + "-" + stop + ".json";

			FileWriter fw = new FileWriter(outputFilePath);

			fw.write(gson.toJson(jaResults));
			fw.flush();

			fw.close();

			// System.out.println(jaResults.size());
			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	void goRnd(String server, String workFlow, String folder, int count) {

		SciDataExpertsStandardizer standardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY,workFlow,server);

		
		String structureFileName = "DSSTox_082021_Structures.csv";

		try {

			Vector<Integer> indices = new Vector<>();

			int max = 1095917;

			while (indices.size() < count) {
				Integer index = Integer.valueOf((int) (Math.random() * max));
				if (!indices.contains(index)) {
					indices.add(index);
				}
			}

			Collections.sort(indices);

			//			for (Integer index:indices) {
			//				System.out.println(index);
			//			}

			//			if (true) return;

			Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
			BufferedReader br = new BufferedReader(new FileReader(folder + structureFileName));
			String header = br.readLine().replace("\"", "");

			// "DSSTOX_COMPOUND_ID","Original_SMILES","Number of connected
			// components","Canonical_QSARr","Salt_Solvent","InChI_Code_QSARr","InChI
			// Key_QSARr","Salt_Solvent_ID"

			String[] fields = header.split(",");
			LinkedList<String> headers = ParseStringUtils.Parse3(header, ",");
			Hashtable<String, Integer> htCols = new Hashtable<>();
			for (int i = 0; i < headers.size(); i++) {
				htCols.put(fields[i], Integer.valueOf(i));
			}

			Indigo indigo = new Indigo();

			int currentLine = 0;

			JsonArray jaResults = new JsonArray();

			while (true) {

				currentLine++;

				String line = br.readLine();

				if (line == null)
					break;

				//				if (currentLine%10==0) System.out.println(currentLine);

				if (!indices.contains(Integer.valueOf(currentLine))) {
					//					System.out.println("*"+currentLine);
					continue;
				} else {
					//					System.out.println(currentLine);
				}

				if (jaResults.size() % 10 == 0)
					System.out.println(jaResults.size());

				LinkedList<String> values = ParseStringUtils.Parse3(line, ",");

				String DSSTOX_COMPOUND_ID = values.get(htCols.get("DSSTOX_COMPOUND_ID"));
				String Original_SMILES = values.get(htCols.get("Original_SMILES"));
				String QSAR_Ready_SMILES_OPERA = values.get(htCols.get("Canonical_QSARr"));
				String QSAR_Ready_InchiKey_OPERA = values.get(htCols.get("InChI Key_QSARr"));// we recalculate it
				// anyways

								


				try {
					JsonObject jo = new JsonObject();
					jo.addProperty("DSSTOX_COMPOUND_ID", DSSTOX_COMPOUND_ID);
					jo.addProperty("Original_SMILES", Original_SMILES);
					jo.addProperty("QSAR_Ready_SMILES_OPERA", QSAR_Ready_SMILES_OPERA);

					Inchi inchiOPERA = toInchiIndigo(QSAR_Ready_SMILES_OPERA);
					if (inchiOPERA != null) {
						String inchiKey_OPERA = inchiOPERA.inchiKey;
						jo.addProperty("inchiKey_OPERA", inchiKey_OPERA);
					} else {
						jo.addProperty("inchiKey_OPERA", "N/A");
					}

					
					boolean full=false;

					HttpResponse<String>response=standardizer.callQsarReadyStandardizePost(Original_SMILES,full);
					String jsonResponse=standardizer.getResponseBody(response, full);
					String qsarSmiles=standardizer.getQsarReadySmilesFromPostJson(jsonResponse, full);
					
					if (qsarSmiles==null) {
						jo.addProperty("QSAR_Ready_SMILES_SDE", "N/A");
						jo.addProperty("inchiKey_SDE", "N/A");
					} else {
						jo.addProperty("QSAR_Ready_SMILES_SDE", qsarSmiles);
						String inchiKey_SDE_Canonical_Indigo = toInchiIndigo(qsarSmiles).inchiKey;
						jo.addProperty("inchiKey_SDE", inchiKey_SDE_Canonical_Indigo);
					}

					
//					String json = runStandardize(Original_SMILES, server, workFlow);
//					if (json.equals("[]")) {
//						jo.addProperty("QSAR_Ready_SMILES_SDE", "N/A");
//						jo.addProperty("inchiKey_SDE", "N/A");
//						// System.out.println(gson.toJson(jo));
//					} else {
//						JsonArray joArray = gson.fromJson(json, JsonArray.class);
//						if (joArray.size() == 1) {
//							JsonObject joResult = gson.fromJson(json, JsonArray.class).get(0).getAsJsonObject();
//							String QSAR_Ready_SMILES_SDE = joResult.get("canonicalSmiles").getAsString();
//							String inchiKey_SDE_Canonical_Indigo = toInchiIndigo(QSAR_Ready_SMILES_SDE).inchiKey;
//							jo.addProperty("QSAR_Ready_SMILES_SDE", QSAR_Ready_SMILES_SDE);
//							jo.addProperty("inchiKey_SDE", inchiKey_SDE_Canonical_Indigo);
//						} else {
//							jo.addProperty("QSAR_Ready_SMILES_SDE", "N/A");
//							jo.addProperty("inchiKey_SDE", "N/A");
//						}
//					}

					jaResults.add(jo);

				} catch (Exception ex) {
//					System.out.println(json);
					ex.printStackTrace();
					continue;
				}

			}

			File folder2 = new File(folder + "//results_"
					+ server.replace("https://", "").replace("http://v2626umcth819.rtord.epa.gov:443", "819") + "_"
					+ workFlow);
			folder2.mkdirs();

			String outputFilePath = folder2.getAbsolutePath() + "//results_rnd" + count + ".json";

			FileWriter fw = new FileWriter(outputFilePath);

			fw.write(gson.toJson(jaResults));
			fw.flush();

			fw.close();

			// System.out.println(jaResults.size());
			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	
	void goRndAllWorkflows(String server, String folder, int count) {


		
		List<String>workflows=new ArrayList<>();
		workflows.add("qsar-ready");
		workflows.add("QSAR-ready_CNL_edits");
//		workflows.add("QSAR-ready_CNL_edits_TMM");
		workflows.add("QSAR-ready_CNL_edits_TMM_2");
		
		String structureFileName = "DSSTox_082021_Structures.csv";

		try {

			File folder2 = new File(folder + "//results_"
					+ server.replace("https://", "").replace("http://v2626umcth819.rtord.epa.gov:443", "819") + "_"
					+ "allWorkflows");
			folder2.mkdirs();

			String outputFilePath = folder2.getAbsolutePath() + "//results_rnd" + count + ".json";

			FileWriter fw = new FileWriter(outputFilePath);

			//TODO add code to check if output file exists- then have it skip ahead appropriately so dont have to rerun if gets stuck
			
			HashSet<Integer> indices = new HashSet<>();

			int max = 1095917;
			
			Random random = new Random(34L);
			

			while (indices.size() < count) {
				int index =random.nextInt(max);
				
				System.out.println(index);
				
				if (!indices.contains(index)) {
					indices.add(index);
				}
			}

//			Collections.sort(indices);

			//			for (Integer index:indices) {
			//				System.out.println(index);
			//			}

			//			if (true) return;

			Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
			BufferedReader br = new BufferedReader(new FileReader(folder + structureFileName));
			String header = br.readLine().replace("\"", "");

			// "DSSTOX_COMPOUND_ID","Original_SMILES","Number of connected
			// components","Canonical_QSARr","Salt_Solvent","InChI_Code_QSARr","InChI
			// Key_QSARr","Salt_Solvent_ID"

			String[] fields = header.split(",");
			LinkedList<String> headers = ParseStringUtils.Parse3(header, ",");
			Hashtable<String, Integer> htCols = new Hashtable<>();
			for (int i = 0; i < headers.size(); i++) {
				htCols.put(fields[i], Integer.valueOf(i));
			}

			Indigo indigo = new Indigo();

			int currentLine = 0;

			JsonArray jaResults = new JsonArray();

			while (true) {

				currentLine++;

				String line = br.readLine();

				if (line == null)
					break;

				//				if (currentLine%10==0) System.out.println(currentLine);

				if (!indices.contains(Integer.valueOf(currentLine))) {
					//					System.out.println("*"+currentLine);
					continue;
				} else {
					//					System.out.println(currentLine);
				}

				if (jaResults.size() % 10 == 0)
					System.out.println(jaResults.size());

				LinkedList<String> values = ParseStringUtils.Parse3(line, ",");

				String DSSTOX_COMPOUND_ID = values.get(htCols.get("DSSTOX_COMPOUND_ID"));
				String Original_SMILES = values.get(htCols.get("Original_SMILES"));
				String QSAR_Ready_SMILES_OPERA = values.get(htCols.get("Canonical_QSARr"));
				String QSAR_Ready_InchiKey_OPERA = values.get(htCols.get("InChI Key_QSARr"));// we recalculate it
				// anyways

				try {
					JsonObject jo = new JsonObject();
					jo.addProperty("DSSTOX_COMPOUND_ID", DSSTOX_COMPOUND_ID);
					jo.addProperty("Original_SMILES", Original_SMILES);
					jo.addProperty("QSAR_Ready_SMILES_OPERA", QSAR_Ready_SMILES_OPERA);

					Inchi inchiOPERA = toInchiIndigo(QSAR_Ready_SMILES_OPERA);
					if (inchiOPERA != null) {
						String inchiKey_OPERA = inchiOPERA.inchiKey;
						jo.addProperty("inchiKey_OPERA", inchiKey_OPERA);
					} else {
						jo.addProperty("inchiKey_OPERA", "N/A");
					}

					
					for (String workflow:workflows) {

						boolean full=false;
						
						SciDataExpertsStandardizer standardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY,workflow,server);

						HttpResponse<String>response=standardizer.callQsarReadyStandardizePost(Original_SMILES,full);
						String jsonResponse=standardizer.getResponseBody(response, full);
						String qsarSmiles=standardizer.getQsarReadySmilesFromPostJson(jsonResponse, full);

						
						if (qsarSmiles==null) {
							jo.addProperty("QSAR_Ready_SMILES_"+workflow, "N/A");
							jo.addProperty("inchiKey_SDE_"+workflow, "N/A");
						} else {
							jo.addProperty("QSAR_Ready_SMILES_"+workflow, qsarSmiles);
							String inchiKey_SDE_Canonical_Indigo = toInchiIndigo(qsarSmiles).inchiKey;
							jo.addProperty("inchiKey_SDE_"+workflow, inchiKey_SDE_Canonical_Indigo);
						}

					}
					
					jaResults.add(jo);
					jo.addProperty("num", jaResults.size());

					fw.write(gson.toJson(jo)+"\n");
					fw.flush();

				} catch (Exception ex) {
//					System.out.println(json);
					ex.printStackTrace();
					continue;
				}

			}

			fw.close();

			// System.out.println(jaResults.size());
			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * This one stores array of qsar ready smiles
	 * 
	 * @param server
	 * @param workFlow
	 * @param folder
	 * @param start
	 * @param stop
	 */
	void go2(String server, String workFlow, String folder, int start, int stop) {

		String structureFileName = "DSSTox_082021_Structures.csv";

		try {

			Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
			BufferedReader br = new BufferedReader(new FileReader(folder + structureFileName));
			String header = br.readLine().replace("\"", "");

			// "DSSTOX_COMPOUND_ID","Original_SMILES","Number of connected
			// components","Canonical_QSARr","Salt_Solvent","InChI_Code_QSARr","InChI
			// Key_QSARr","Salt_Solvent_ID"

			String[] fields = header.split(",");
			LinkedList<String> headers = ParseStringUtils.Parse3(header, ",");
			Hashtable<String, Integer> htCols = new Hashtable<>();
			for (int i = 0; i < headers.size(); i++) {
				htCols.put(fields[i], Integer.valueOf(i));
			}

			Indigo indigo = new Indigo();

			int currentLine = 1;

			JsonArray jaResults = new JsonArray();

			while (true) {

				String line = br.readLine();

				if (currentLine % 10 == 0)
					System.out.println(currentLine);

				if (currentLine < start)
					continue;

				if (currentLine > stop)
					break;

				LinkedList<String> values = ParseStringUtils.Parse3(line, ",");

				String DSSTOX_COMPOUND_ID = values.get(htCols.get("DSSTOX_COMPOUND_ID"));
				String Original_SMILES = values.get(htCols.get("Original_SMILES"));
				String QSAR_Ready_SMILES_OPERA = values.get(htCols.get("Canonical_QSARr"));
				String QSAR_Ready_InchiKey_OPERA = values.get(htCols.get("InChI Key_QSARr"));

				//				String json = runStandardize(Original_SMILES, server, workFlow,false);

				String QSAR_Ready_SMILES_SDE = SciDataExpertsStandardizer.runStandardize(Original_SMILES, server, workFlow,false).qsarReadySmiles;


				try {
					JsonObject jo = new JsonObject();
					jo.addProperty("DSSTOX_COMPOUND_ID", DSSTOX_COMPOUND_ID);
					jo.addProperty("Original_SMILES", Original_SMILES);
					jo.addProperty("QSAR_Ready_SMILES_OPERA", QSAR_Ready_SMILES_OPERA);

					Inchi inchiOPERA = toInchiIndigo(QSAR_Ready_SMILES_OPERA);
					if (inchiOPERA != null) {
						String inchiKey_OPERA = inchiOPERA.inchiKey;
						jo.addProperty("inchiKey_OPERA", inchiKey_OPERA);
					} else {
						jo.addProperty("inchiKey_OPERA", "N/A");
					}

					if (QSAR_Ready_SMILES_SDE!=null) {
						jo.addProperty("QSAR_Ready_SMILES_SDE", QSAR_Ready_SMILES_SDE);
						Inchi inchiSDE = toInchiIndigo(QSAR_Ready_SMILES_SDE);
						if (inchiSDE != null)
							jo.addProperty("inchiKey_SDE", inchiSDE.inchiKey);

					} else {
						jo.addProperty("QSAR_Ready_SMILES_SDE", "N/A");
						jo.addProperty("inchiKey_SDE", "N/A");
					}

					jaResults.add(jo);

				} catch (Exception ex) {
					ex.printStackTrace();
					continue;
				}
				currentLine++;
			}

			File folder2 = new File(folder + "//results_"
					+ server.replace("https://", "").replace("http://v2626umcth819.rtord.epa.gov:443", "819") + "_"
					+ workFlow);
			folder2.mkdirs();

			String outputFilePath = folder2.getAbsolutePath() + "//results_" + start + "-" + stop + "_2.json";

			FileWriter fw = new FileWriter(outputFilePath);

			fw.write(gson.toJson(jaResults));
			fw.flush();

			fw.close();

			// System.out.println(jaResults.size());
			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static Inchi toInchiIndigo(String mol) {
		try {
			Indigo indigo = new Indigo();
			indigo.setOption("ignore-stereochemistry-errors", true);

			IndigoInchi indigoInchi = new IndigoInchi(indigo);

			IndigoObject m = indigo.loadMolecule(mol);

			Inchi inchi = new Inchi();
			inchi.inchi = indigoInchi.getInchi(m);
			inchi.inchiKey = indigoInchi.getInchiKey(inchi.inchi);
			inchi.inchiKey1 = inchi.inchiKey != null ? inchi.inchiKey.substring(0, 14) : null;

			return inchi;

		} catch (IndigoException ex) {
			//			log.error(ex.getMessage());
			return null;
		}
	}

	

	void goThroughResults(String server, String workFlow, String folder, int start, int stop) {
		File folder2 = new File(folder + "//results_"
				+ server.replace("https://", "").replace("http://v2626umcth819.rtord.epa.gov:443", "819") + "_"
				+ workFlow);
		String jsonPath = folder2.getAbsolutePath() + "//results_" + start + "-" + stop + ".json";
		String htmlPath = jsonPath.replace("json", "html");
		writeDiscrepancyWebpage(server, workFlow, jsonPath, htmlPath);
	}

	void goThroughResultsRnd(String server, String workFlow, String folder, int count) {

		File folder2 = new File(folder + "//results_"
				+ server.replace("https://", "").replace("http://v2626umcth819.rtord.epa.gov:443", "819") + "_"
				+ workFlow);
		String jsonPath = folder2.getAbsolutePath() + "//results_rnd" + count + ".json";

		//		writeDiscrepancyWebpage(server, workFlow, jsonPath, jsonPath.replace("json", "html"));
		writeDescrepancyExcelFile(server, workFlow, jsonPath, jsonPath.replace("json", "xlsx"));

	}

	private void writeDescrepancyExcelFile(String server, String workFlow, String jsonPath, String outputPath) {
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

		try {

			Reader reader = Files.newBufferedReader(Paths.get(jsonPath));
			JsonArray ja = gson.fromJson(reader, JsonArray.class);

			JsonArray vecMismatchHaveSDE = new JsonArray();
			JsonArray vecDontHaveSDE = new JsonArray();
			JsonArray vecMatch = new JsonArray();

			for (int i = 0; i < ja.size(); i++) {
				JsonObject jo = ja.get(i).getAsJsonObject();

				String inchiKey_OPERA = jo.get("inchiKey_OPERA").getAsString();
				String inchiKey_SDE = jo.get("inchiKey_SDE").getAsString();

				if (!inchiKey_SDE.equals("N/A")) {
					if (!inchiKey_OPERA.equals(inchiKey_SDE)) {
						//						System.out.println("*"+gson.toJson(jo));
						vecMismatchHaveSDE.add(jo);
					} else {
						//						System.out.println(jo.get("Original_SMILES").getAsString()+"\tSDE qsar ready smiles matches OPERA");
						vecMatch.add(jo);
					}

				} else {
					vecDontHaveSDE.add(jo);
					//					System.out.println(jo.get("Original_SMILES").getAsString()+"\tSDE qsar ready smiles = blank");
				}
			}

			XSSFWorkbook workbook = new XSSFWorkbook();



			writeRows(workbook,"InchiMismatch",vecMismatchHaveSDE);
			writeRows(workbook,"Multicomponent",vecDontHaveSDE);
			//			writeRows(fw, vecMismatchHaveSDE);
			//			writeRows(fw, vecDontHaveSDE);


			FileOutputStream saveExcel = new FileOutputStream(outputPath);

			workbook.write(saveExcel);
			workbook.close();
			//			saveExcel.close();



		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void writeDiscrepancyWebpage(String server, String workFlow, String jsonPath, String htmlPath) {
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

		try {
			FileWriter fw = new FileWriter(htmlPath);

			fw.write("<html>\r\n" + "<head>\r\n" + "<title>Standardizer Comparison</title>\r\n"
					+ "</head><div style=\"overflow-x:auto\">" + "workflow=" + workFlow + "<br>server=" + server
					+ "<br><br>\n" + "<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\r\n"
					+ "<tr bgcolor=\"#D3D3D3\">\r\n" + " <th>i</th>\r\n" + " <th>DTXCID</th>\r\n"
					+ "	<th>ORIGINAL</th>\r\n" + "	<th>OPERA_QSAR_READY</th>\r\n"
					+ "	<th>SCI_DATA_EXPERTS_QSAR_READY</th>\r\n" + "</tr>");

			Reader reader = Files.newBufferedReader(Paths.get(jsonPath));
			JsonArray ja = gson.fromJson(reader, JsonArray.class);

			JsonArray vecMismatchHaveSDE = new JsonArray();
			JsonArray vecDontHaveSDE = new JsonArray();
			JsonArray vecMatch = new JsonArray();

			for (int i = 0; i < ja.size(); i++) {
				JsonObject jo = ja.get(i).getAsJsonObject();

				String inchiKey_OPERA = jo.get("inchiKey_OPERA").getAsString();
				String inchiKey_SDE = jo.get("inchiKey_SDE").getAsString();

				if (!inchiKey_SDE.equals("N/A")) {
					if (!inchiKey_OPERA.equals(inchiKey_SDE)) {
						//						System.out.println("*"+gson.toJson(jo));
						vecMismatchHaveSDE.add(jo);
					} else {
						//						System.out.println(jo.get("Original_SMILES").getAsString()+"\tSDE qsar ready smiles matches OPERA");
						vecMatch.add(jo);
					}

				} else {
					vecDontHaveSDE.add(jo);
					//					System.out.println(jo.get("Original_SMILES").getAsString()+"\tSDE qsar ready smiles = blank");
				}
			}

			writeRows(fw, vecMismatchHaveSDE);
			writeRows(fw, vecDontHaveSDE);

			//			for (int i=0;i<vecMatch.size();i++) {
			//				JsonObject jo=vecMatch.get(i).getAsJsonObject();
			//				String Original_Smiles=jo.get("Original_SMILES").getAsString();	
			//				System.out.println(i+"\t"+Original_Smiles);
			//			}

			//			System.out.println(ja.size());
			fw.write("</table>\n");
			;
			fw.write("</html>\n");
			;

			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void goThroughResults2(String server, String workFlow, String folder, int start, int stop) {

		File folder2 = new File(folder + "//results_"
				+ server.replace("https://", "").replace("http://v2626umcth819.rtord.epa.gov:443", "819") + "_"
				+ workFlow);
		String jsonPath = folder2.getAbsolutePath() + "//results_" + start + "-" + stop + "_2.json";
		String htmlPath = jsonPath.replace("json", "html");

		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

		try {
			FileWriter fw = new FileWriter(htmlPath);

			fw.write("<html>\r\n" + "<head>\r\n" + "<title>Standardizer Comparison</title>\r\n"
					+ "</head><div style=\"overflow-x:auto\">" + "workflow=" + workFlow + "<br>server=" + server
					+ "<br><br>\n" + "<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\r\n"
					+ "<tr bgcolor=\"#D3D3D3\">\r\n" + " <th>i</th>\r\n" + " <th>DTXCID</th>\r\n"
					+ "	<th>ORIGINAL</th>\r\n" + "	<th>OPERA_QSAR_READY</th>\r\n"
					+ "	<th>SCI_DATA_EXPERTS_QSAR_READY</th>\r\n" + "</tr>");

			Reader reader = Files.newBufferedReader(Paths.get(jsonPath));
			JsonArray ja = gson.fromJson(reader, JsonArray.class);

			JsonArray vecMismatchHaveSDE = new JsonArray();
			JsonArray vecDontHaveSDE = new JsonArray();
			JsonArray vecMatch = new JsonArray();

			for (int i = 0; i < ja.size(); i++) {
				JsonObject jo = ja.get(i).getAsJsonObject();

				String inchiKey_OPERA = jo.get("inchiKey_OPERA").getAsString();

				String inchiKey_SDE = null;

				if (jo.get("inchiKey_SDE") == null) {
					inchiKey_SDE = "N/A";
					System.out.println(jo.get("Original_SMILES").getAsString());
					jo.addProperty("QSAR_Ready_SMILES_SDE", "N/A");
					//					continue;
				} else {
					inchiKey_SDE = jo.get("inchiKey_SDE").getAsString();
				}

				if (!inchiKey_SDE.equals("N/A")) {
					if (!inchiKey_OPERA.equals(inchiKey_SDE)) {
						//						System.out.println("*"+gson.toJson(jo));
						vecMismatchHaveSDE.add(jo);
					} else {
						//						System.out.println(jo.get("Original_SMILES").getAsString()+"\tSDE qsar ready smiles matches OPERA");
						vecMatch.add(jo);
					}

				} else {
					vecDontHaveSDE.add(jo);
					//					System.out.println(jo.get("Original_SMILES").getAsString()+"\tSDE qsar ready smiles = blank");
				}
			}

			writeRows(fw, vecMismatchHaveSDE);
			writeRows(fw, vecDontHaveSDE);

			//			for (int i=0;i<vecMatch.size();i++) {
			//				JsonObject jo=vecMatch.get(i).getAsJsonObject();
			//				String Original_Smiles=jo.get("Original_SMILES").getAsString();	
			//				System.out.println(i+"\t"+Original_Smiles);
			//			}

			//			System.out.println(ja.size());
			fw.write("</table>\n");
			;
			fw.write("</html>\n");
			;

			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void writeRows(FileWriter fw, JsonArray vecMismatchHaveSDE) throws IOException, CDKException {
		for (int i = 0; i < vecMismatchHaveSDE.size(); i++) {

			JsonObject jo = vecMismatchHaveSDE.get(i).getAsJsonObject();
			fw.write("<tr>\n");

			String CID = jo.get("DSSTOX_COMPOUND_ID").getAsString();

			String Original_Smiles = jo.get("Original_SMILES").getAsString();

			System.out.println(Original_Smiles);

			String Original_Smiles_Image = StructureImageUtil.generateImageSrcBase64FromSmiles(Original_Smiles);

			String QSAR_Ready_SMILES_OPERA = jo.get("QSAR_Ready_SMILES_OPERA").getAsString();
			String QSAR_Ready_SMILES_OPERA_Image = StructureImageUtil
					.generateImageSrcBase64FromSmiles(QSAR_Ready_SMILES_OPERA);

			String QSAR_Ready_SMILES_SDE = jo.get("QSAR_Ready_SMILES_SDE").getAsString();

			String QSAR_Ready_SMILES_SDE_Image = null;

			if (!QSAR_Ready_SMILES_SDE.equals("N/A"))
				QSAR_Ready_SMILES_SDE_Image = StructureImageUtil
				.generateImageSrcBase64FromSmiles(QSAR_Ready_SMILES_SDE);

			//				fw.write("<td><a href=\"https://comptox.epa.gov/dashboard/dsstoxdb/results?search="+CID+"\" target=\"_blank\">"+CID+"</a><br></td>\n");
			fw.write("<td>" + (i + 1) + "</td>\n");
			fw.write("<td>" + CID + "</td>\n");
			// TODO lookup SID

			fw.write("<td style=\"text-align:center;word-break:break-all\" bgcolor=\"#FFFFFF\">");
			fw.write("<img src=\"" + Original_Smiles_Image + "\" style=\"max-height:150px;max-width:300px\"><br>");
			fw.write("<br>" + Original_Smiles + "</td>\r\n");

			fw.write("<td style=\"text-align:center;word-break:break-all\" bgcolor=\"#FFFFFF\">");
			fw.write("<img src=\"" + QSAR_Ready_SMILES_OPERA_Image
					+ "\" style=\"max-height:150px;max-width:300px\"><br>");
			fw.write("<br>" + QSAR_Ready_SMILES_OPERA + "</td>\r\n");

			fw.write("<td style=\"text-align:center;word-break:break-all\" bgcolor=\"#F08080\">");

			if (QSAR_Ready_SMILES_SDE_Image == null) {
				fw.write("<br>");
			} else {
				fw.write("<img src=\"" + QSAR_Ready_SMILES_SDE_Image
						+ "\" style=\"max-height:150px;max-width:300px\"><br>");
			}

			fw.write("<br>" + QSAR_Ready_SMILES_SDE + "</td>\r\n");

			fw.write("</tr>\n");
		}
	}


	private void writeRows(Workbook workbook,String sheetName,JsonArray vecMismatchHaveSDE) throws IOException, CDKException {


		Sheet sheet=workbook.createSheet(sheetName);

		Row row1 = sheet.createRow(0);
		row1.createCell(0).setCellValue("DTXCID");
		row1.createCell(1).setCellValue("Original");
		row1.createCell(2).setCellValue("OPERA_QSAR_READY");
		row1.createCell(3).setCellValue("SDE_QSAR_READY");
		row1.createCell(4).setCellValue("SDE matches OPERA");

		sheet.setColumnWidth(0, 20*256);
		sheet.setColumnWidth(1, 60*256);
		sheet.setColumnWidth(2, 60*256);
		sheet.setColumnWidth(3, 60*256);

		for (int i = 0; i < vecMismatchHaveSDE.size(); i++) {

			JsonObject jo = vecMismatchHaveSDE.get(i).getAsJsonObject();

			int irow=(i+1);
			Row rowi = sheet.createRow(irow);


			String CID = jo.get("DSSTOX_COMPOUND_ID").getAsString();
			String Original_Smiles = jo.get("Original_SMILES").getAsString();
			System.out.println(Original_Smiles);
			String QSAR_Ready_SMILES_OPERA = jo.get("QSAR_Ready_SMILES_OPERA").getAsString();
			String QSAR_Ready_SMILES_SDE = jo.get("QSAR_Ready_SMILES_SDE").getAsString();
			
			String inchiKeyOPERA=toInchiIndigo(QSAR_Ready_SMILES_OPERA).inchiKey;
			String inchiKeySDE=toInchiIndigo(QSAR_Ready_SMILES_SDE).inchiKey;
			

			rowi.createCell(0).setCellValue(CID);
			rowi.createCell(1).setCellValue(Original_Smiles);
			rowi.createCell(2).setCellValue(QSAR_Ready_SMILES_OPERA);
			rowi.createCell(3).setCellValue(QSAR_Ready_SMILES_SDE);
			rowi.createCell(4).setCellValue(inchiKeyOPERA.equals(inchiKeySDE));
			
			rowi.setHeight((short)2000);

			createImage(Original_Smiles, irow, 1, sheet, 1);
			createImage(QSAR_Ready_SMILES_OPERA, irow, 2, sheet, 1);
			createImage(QSAR_Ready_SMILES_SDE, irow, 3, sheet, 1);
			
			rowi.setHeight((short)(2000*1.15));//add some space for smiles at bottom

		}

	}
	
	
	private void writeRowsMatch(Workbook workbook,String sheetName,JsonArray vecMismatchHaveSDE) throws IOException, CDKException {


		Sheet sheet=workbook.createSheet(sheetName);

		Row row1 = sheet.createRow(0);
		row1.createCell(0).setCellValue("DTXCID");

		row1.createCell(1).setCellValue("SmilesOriginal1");		
		row1.createCell(2).setCellValue("SMILES1");
		
		row1.createCell(3).setCellValue("SmilesOriginal2");
		row1.createCell(4).setCellValue("SMILES2");
		
		row1.createCell(5).setCellValue("Match");

		sheet.setColumnWidth(0, 20*256);
		sheet.setColumnWidth(1, 60*256);
		sheet.setColumnWidth(2, 60*256);
		sheet.setColumnWidth(3, 60*256);
		sheet.setColumnWidth(4, 60*256);
		sheet.setColumnWidth(5, 60*256);

		for (int i = 0; i < vecMismatchHaveSDE.size(); i++) {
			JsonObject jo = vecMismatchHaveSDE.get(i).getAsJsonObject();
			int irow=(i+1);
			Row rowi = sheet.createRow(irow);

			String CID = jo.get("DSSTOX_COMPOUND_ID").getAsString();
			String Smiles1 = jo.get("Smiles1").getAsString();
			String Smiles2 = jo.get("Smiles2").getAsString();
			
			String SmilesOriginal1 = jo.get("SmilesOriginal1").getAsString();
			String SmilesOriginal2 = jo.get("SmilesOriginal2").getAsString();

			
			String inchiKeyOPERA=toInchiIndigo(Smiles1).inchiKey;
			String inchiKeySDE=toInchiIndigo(Smiles2).inchiKey;
			

			rowi.createCell(0).setCellValue(CID);

			rowi.createCell(1).setCellValue(SmilesOriginal1);
			rowi.createCell(2).setCellValue(Smiles1);
			
			rowi.createCell(3).setCellValue(SmilesOriginal2);
			rowi.createCell(4).setCellValue(Smiles2);
			rowi.createCell(5).setCellValue(inchiKeyOPERA.equals(inchiKeySDE));
			
			rowi.setHeight((short)2000);
			
			createImage(SmilesOriginal1, irow, 1, sheet, 1);
			createImage(Smiles1, irow, 2, sheet, 1);
			createImage(SmilesOriginal2, irow, 3, sheet, 1);
			createImage(Smiles2, irow, 4, sheet, 1);

			
			rowi.setHeight((short)(2000*1.15));//add some space for smiles at bottom

		}

	}

	public static void createImage(String smiles, int startRow,int column,Sheet sheet, int rowspan) {

		Workbook wb=sheet.getWorkbook();
		if (smiles==null || smiles.equals("N/A") || smiles.contains("error")) return;		
		byte[] imageBytes=StructureImageUtil.generateImageBytesFromSmiles(smiles);		
		if (imageBytes==null) return;
		
		int pictureIdx = wb.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);

		//create an anchor with upper left cell column/startRow, only one cell anchor since bottom right depends on resizing
		CreationHelper helper = wb.getCreationHelper();
		ClientAnchor anchor = helper.createClientAnchor();
		anchor.setCol1(column);
		anchor.setRow1(startRow);

		//create a picture anchored to Col1 and Row1
		Drawing drawing = sheet.createDrawingPatriarch();
		Picture pict = drawing.createPicture(anchor, pictureIdx);

		//get the picture width in px
		int pictWidthPx = pict.getImageDimension().width;
		//get the picture height in px
		int pictHeightPx = pict.getImageDimension().height;

		//get column width of column in px
		float columnWidthPx = sheet.getColumnWidthInPixels(column);

		//get the heights of all merged rows in px
		float[] rowHeightsPx = new float[startRow+rowspan];
		float rowsHeightPx = 0f;
		for (int r = startRow; r < startRow+rowspan; r++) {
			Row row = sheet.getRow(r);
			float rowHeightPt = row.getHeightInPoints();
			rowHeightsPx[r-startRow] = rowHeightPt * Units.PIXEL_DPI / Units.POINT_DPI;
			rowsHeightPx += rowHeightsPx[r-startRow];
		}

		//calculate scale
		float scale = 1;
		if (pictHeightPx > rowsHeightPx) {
			float tmpscale = rowsHeightPx / (float)pictHeightPx;
			if (tmpscale < scale) scale = tmpscale;
		}
		if (pictWidthPx > columnWidthPx) {
			float tmpscale = columnWidthPx / (float)pictWidthPx;
			if (tmpscale < scale) scale = tmpscale;
		}

		//calculate the horizontal center position
		int horCenterPosPx = Math.round(columnWidthPx/2f - pictWidthPx*scale/2f);
		//set the horizontal center position as Dx1 of anchor


		anchor.setDx1(horCenterPosPx * Units.EMU_PER_PIXEL); //in unit EMU for XSSF


		//calculate the vertical center position
		int vertCenterPosPx = Math.round(rowsHeightPx/2f - pictHeightPx*scale/2f);
		//get Row1
		Integer row1 = null;
		rowsHeightPx = 0f;
		for (int r = 0; r < rowHeightsPx.length; r++) {
			float rowHeightPx = rowHeightsPx[r];
			if (rowsHeightPx + rowHeightPx > vertCenterPosPx) {
				row1 = r + startRow;
				break;
			}
			rowsHeightPx += rowHeightPx;
		}
		//set the vertical center position as Row1 plus Dy1 of anchor
		if (row1 != null) {
			anchor.setRow1(row1);
			if (wb instanceof XSSFWorkbook) {
				anchor.setDy1(Math.round(vertCenterPosPx - rowsHeightPx) * Units.EMU_PER_PIXEL); //in unit EMU for XSSF
			} else if (wb instanceof HSSFWorkbook) {
				//see https://stackoverflow.com/questions/48567203/apache-poi-xssfclientanchor-not-positioning-picture-with-respect-to-dx1-dy1-dx/48607117#48607117 for HSSF
				float DEFAULT_ROW_HEIGHT = 12.75f;
				anchor.setDy1(Math.round((vertCenterPosPx - rowsHeightPx) * Units.PIXEL_DPI / Units.POINT_DPI * 14.75f * DEFAULT_ROW_HEIGHT / rowHeightsPx[row1]));
			}
		}

		//set Col2 of anchor the same as Col1 as all is in one column
		anchor.setCol2(column);

		//calculate the horizontal end position of picture
		int horCenterEndPosPx = Math.round(horCenterPosPx + pictWidthPx*scale);
		//set set the horizontal end position as Dx2 of anchor

		anchor.setDx2(horCenterEndPosPx * Units.EMU_PER_PIXEL); //in unit EMU for XSSF

		//calculate the vertical end position of picture
		int vertCenterEndPosPx = Math.round(vertCenterPosPx + pictHeightPx*scale);
		//get Row2
		Integer row2 = null;
		rowsHeightPx = 0f;
		for (int r = 0; r < rowHeightsPx.length; r++) {
			float rowHeightPx = rowHeightsPx[r];
			if (rowsHeightPx + rowHeightPx > vertCenterEndPosPx) {
				row2 = r + startRow;
				break;
			}
			rowsHeightPx += rowHeightPx;
		}

		//set the vertical end position as Row2 plus Dy2 of anchor
		if (row2 != null) {
			anchor.setRow2(row2);
			anchor.setDy2(Math.round(vertCenterEndPosPx - rowsHeightPx) * Units.EMU_PER_PIXEL); //in unit EMU for XSSF
		}
	}

	public static void configUnirest() {

		try {// Need to suppress logging because it slows things down when have big data
			// sets...

			Set<String> artifactoryLoggers = new HashSet<String>(Arrays.asList("org.apache.http", "groovyx.net.http"));
			for (String log : artifactoryLoggers) {
				ch.qos.logback.classic.Logger artLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
						.getLogger(log);
				artLogger.setLevel(ch.qos.logback.classic.Level.INFO);
				artLogger.setAdditive(false);
			}

			Unirest.config().followRedirects(true).socketTimeout(000).connectTimeout(000);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		}

	}

	
	void compareResultsInJsonFile() {
		boolean lookAtSalts=false;
		String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 qsar ready standardizer\\results_819_QSAR-ready_CNL_edits_TMM\\";
		String filepathJson=folder+"results_rnd100000_allWorkflows.json";
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		
//		List<String>workflows=new ArrayList<>();
//		workflows.add("qsar-ready");
//		workflows.add("QSAR-ready_CNL_edits");
//		workflows.add("QSAR-ready_CNL_edits_TMM_2");

//		String workflow="QSAR-ready_CNL_edits_TMM_2";
//		String workflow="QSAR-ready_CNL_edits";

//		String workflow="QSAR-ready_CNL_edits_TMM_2";
//		String workflow="qsar-ready";
		
		String workflow="qsar-ready";
		String workflow2="QSAR-ready_CNL_edits_TMM_2";
		
		
		JsonArray ja=convertJsonObjectsToJsonArray(filepathJson, gson);
		
		int count=0;
		int countMismatch=0;
		
		for (int i=0;i<ja.size();i++) {
			
			JsonObject jo=ja.get(i).getAsJsonObject();
			
			String DSSTOX_COMPOUND_ID=jo.get("DSSTOX_COMPOUND_ID").getAsString();
			String Original_SMILES=jo.get("Original_SMILES").getAsString();
			String QSAR_Ready_SMILES_OPERA=jo.get("QSAR_Ready_SMILES_OPERA").getAsString();
			
			if (lookAtSalts) {
				if(!Original_SMILES.contains(".")) continue;	
			} else {
				if(Original_SMILES.contains(".")) continue;
			}
			
			if (lookAtSalts && !Original_SMILES.contains(".")) continue;			
			if (Original_SMILES.isBlank()) continue;
			
			String inchiKey_OPERA=jo.get("inchiKey_OPERA").getAsString();
			
			String inchiKey="";
			String qsarSmiles="";
			
			String inchiKey2="";
			String qsarSmiles2="";

			
			if (jo.get("QSAR_Ready_inchiKey_"+workflow)!=null) {
				inchiKey=jo.get("QSAR_Ready_inchiKey_"+workflow).getAsString();
				qsarSmiles=jo.get("QSAR_Ready_SMILES_"+workflow).getAsString();
			}
			
			
			if (jo.get("QSAR_Ready_inchiKey_"+workflow2)!=null) {
				inchiKey2=jo.get("QSAR_Ready_inchiKey_"+workflow2).getAsString();
				qsarSmiles2=jo.get("QSAR_Ready_SMILES_"+workflow2).getAsString();
			}

			
			if(!inchiKey.equals(inchiKey_OPERA)) {
				countMismatch++;
//				System.out.println(countMismatch+"\t"+Original_SMILES+"\t"+QSAR_Ready_SMILES_OPERA+"\t"+qsarSmiles);
			}
			
			if (inchiKey2.equals(inchiKey_OPERA) && !inchiKey.equals(inchiKey_OPERA)) {
				System.out.println(DSSTOX_COMPOUND_ID+"\t"+Original_SMILES+"\t"+qsarSmiles+"\t"+qsarSmiles2);
			}
			
//			if (!inchiKey2.equals(inchiKey_OPERA) && inchiKey.equals(inchiKey_OPERA)) {
//				System.out.println(DSSTOX_COMPOUND_ID+"\t"+QSAR_Ready_SMILES_OPERA+"\t"+qsarSmiles+"\t"+qsarSmiles2);
//			}

			
			count++;
		}
		System.out.println(countMismatch+" of "+count);

		
	}
	

	/**
	 * Reads a json file where file is a series of json objects rather than a proper JsonArray
	 * 
	 * @param filepathJson
	 * @param gson
	 * @return JsonArray
	 */
	private JsonArray convertJsonObjectsToJsonArray(String filepathJson, Gson gson) {

		JsonArray ja=new JsonArray();

		try {
			BufferedReader br=new BufferedReader(new FileReader(filepathJson));

			String json="";
			
			while (true) {
				String Line=br.readLine();
				if(Line==null) break;
				json+=Line+"\r\n";
				if (Line.contains("}")) {
//					System.out.println(json);
					JsonObject jo=gson.fromJson(json, JsonObject.class);
					ja.add(jo);
					json="";
				}
			}
			System.out.println(ja.size());			
//			System.out.println(gson.toJson(ja));
			br.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return ja;
	}
	void rerunChemicalsInJsonFile() {
		
		boolean full=false;
		
		String serverEPA="https://hcd.rtpnc.epa.gov/api/stdizer";
		String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 qsar ready standardizer\\results_819_QSAR-ready_CNL_edits_TMM\\";
		String filepathJson=folder+"results_rnd100000.json";
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
				
		List<String>workflows=new ArrayList<>();
		workflows.add("qsar-ready");
		workflows.add("QSAR-ready_CNL_edits");
//		workflows.add("QSAR-ready_CNL_edits_TMM");
		workflows.add("QSAR-ready_CNL_edits_TMM_2");
		
//		String workflow="QSAR-ready_CNL_edits_TMM_2";
		
		String json;
		String filepathJson2=folder+"results_rnd100000_allWorkflows.json";

		try {
			FileWriter fw=new FileWriter(filepathJson2);

			
			json = Files.readString(Paths.get(filepathJson));
			
			JsonArray ja=gson.fromJson(json, JsonArray.class);
			
			boolean start=false;
			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				
				String originalSmiles=jo.get("Original_SMILES").getAsString();
				String cid=jo.get("DSSTOX_COMPOUND_ID").getAsString();
				
				if(cid.equals("DTXCID50742721")) {
					System.out.println("continuing where left off at DTXCID50742721");
					start=true;
				}
				
				if(!start) continue;
				
				
				jo.remove("QSAR_Ready_SMILES_SDE");
				jo.remove("inchiKey_SDE");
				
				List<String>inchiKeys=new ArrayList<>();
				
				inchiKeys.add(jo.get("inchiKey_OPERA").getAsString());
				
				for (String workflow:workflows) {
//					StandardizeResponseWithStatus s=SciDataExpertsStandardizer.callQsarReadyStandardizeGet(serverEPA, workflow, originalSmiles);
					
					
					HttpResponse<String>response=SciDataExpertsStandardizer.callQsarReadyStandardizePost(originalSmiles,full,workflow,serverEPA);
					String jsonResponse=SciDataExpertsStandardizer.getResponseBody(response, full);
					String qsarSmiles=SciDataExpertsStandardizer.getQsarReadySmilesFromPostJson(jsonResponse, full);

					
					jo.addProperty("QSAR_Ready_SMILES_"+workflow, qsarSmiles);
					
					if (qsarSmiles!=null) {
						String inchiKey_SDE_Canonical_Indigo = toInchiIndigo(qsarSmiles).inchiKey;
						jo.addProperty("QSAR_Ready_inchiKey_"+workflow, inchiKey_SDE_Canonical_Indigo);
						inchiKeys.add(inchiKey_SDE_Canonical_Indigo);
					}
				}

				
				if (i%100==0) {
					System.out.println(i);
				}
				
//				System.out.println(gson.toJson(jo));
				fw.write(gson.toJson(jo)+"\r\n");
				
				
			}
			
			fw.flush();
			fw.close();

//			//If it finishes, write entire array to json
//			filepathJson2=folder+"results_rnd100000_allWorkflows2.json";			
//			fw=new FileWriter(filepathJson2);
//			String json2=gson.toJson(ja);
//			fw.write(json2);
//			fw.flush();
//			fw.close();
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	void rerunChemicalsInExcelFile() {

		System.out.println("enter rerunChemicalsInExcelFile");
		
        String workflowNew="QSAR-ready_CNL_edits_TMM_2";
        String server = "http://v2626umcth819.rtord.epa.gov:443";	        
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

		
		String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 qsar ready standardizer\\results_819_QSAR-ready_CNL_edits_TMM\\";
//		String fileName="results_rnd100000.xlsx";
//		String fileName="results_rnd10000.xlsx";
//		String fileName="results_rnd100000 small list only inchi mismatch.xlsx";

//		String fileName="results_rnd100000 small list only inchi mismatch3.xlsx";
//		String fileNameOut="results_rnd100000 small list only inchi mismatch3_rerun_scifinder_QSAR-ready_CNL_edits_TMM_2.xlsx";
//        String colNameSmilesOriginal="Scifinder";
//		String colNameOperaSmiles="OPERA_QSAR_READY_scifinder";
		
//		String fileName="results_rnd100000 small list only inchi mismatch3.xlsx";
//		String fileNameOut="results_rnd100000 small list only inchi mismatch3_rerun_original_QSAR-ready_CNL_edits_TMM_2.xlsx";
//        String colNameSmilesOriginal="Original";
//		String colNameOperaSmiles="OPERA_QSAR_READY";
		
		String fileName="results_rnd100000 small list only inchi mismatch3.xlsx";
        String colNameSmilesOriginal="OPERA_QSAR_READY";
		String colNameOperaSmiles="OPERA_QSAR_READY";
		String fileNameOut="results_rnd100000 small list only inchi mismatch3_rerun_smilesOriginal="+colNameSmilesOriginal+"_"+workflowNew+".xlsx";		
				
//		String fileName="results_rnd100000 small list only inchi mismatch3.xlsx";
//        String strSmilesOriginalColumnName="Original";
//		String fileNameOut="results_rnd100000 small list only inchi mismatch3_rerun_original_QSAR-ready_CNL_edits_TMM_2.xlsx";

		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(new File(folder+fileName));
			
	        Workbook workbook = new XSSFWorkbook(inputStream);
	        Sheet sheet = workbook.getSheetAt(0);
	        
	        	        
	        Row row0=sheet.getRow(0);	        
	        Hashtable<String,Integer>htColNames=new Hashtable<>();
	        for (int i=0;i<row0.getLastCellNum();i++) {
	        	htColNames.put(row0.getCell(i).getStringCellValue(), i);
	        }

	        int colSmilesOriginal=htColNames.get(colNameSmilesOriginal);
	        
	        
	        JsonArray vec=new JsonArray();
	        
	        
	        for (int i=1;i<=sheet.getLastRowNum();i++) {
	        	Row row=sheet.getRow(i);
	        	String DTXCID=row.getCell(htColNames.get("DTXCID")).getStringCellValue();
	        	
	        	String Type=row.getCell(htColNames.get("Type")).getStringCellValue();	        	
	        	if (!Type.toLowerCase().contains("double bond placement")) continue;
	        	
	        	
	        	if (row.getCell(colSmilesOriginal)==null) continue;
	        	
	        	String Original_SMILES=row.getCell(colSmilesOriginal).getStringCellValue();
	        	
//	        	System.out.println(Original_SMILES);
//	        	if (true) continue;
	        	
	        	String SmilesOpera=row.getCell(htColNames.get(colNameOperaSmiles)).getStringCellValue();
//	        	String SmilesSDE_old=row.getCell(htColNames.get("SDE_QSAR_READY")).getStringCellValue();	        		        

	        	StandardizeResult smilesResult=SciDataExpertsStandardizer.runStandardize(Original_SMILES, server, workflowNew,false);
	        	String SmilesSDE_new = smilesResult.qsarReadySmiles;	        	
								
	        	JsonObject jo=new JsonObject();	        	
	        	jo.addProperty("DSSTOX_COMPOUND_ID", DTXCID);
	        	jo.addProperty("Original_SMILES",Original_SMILES);
	        	jo.addProperty("QSAR_Ready_SMILES_OPERA",SmilesOpera);
	        	jo.addProperty("QSAR_Ready_SMILES_SDE",SmilesSDE_new);
	        	vec.add(jo);
	        	
//				if (!SmilesSDE_old.equals(SmilesSDE_new))
//					System.out.println(DTXCID+"\t"+Original_SMILES+"\t"+SmilesSDE_old+"\t"+SmilesSDE_new);
//				System.out.println(DTXCID+"\t"+Original_SMILES+"\t"+SmilesSDE_new);

				if (i%10==0) System.out.println(i);

	        }
	        
	        
	        workbook.close();
	        
	        
			workbook = new XSSFWorkbook();
			writeRows(workbook,workflowNew,vec);
			
			//			writeRows(fw, vecMismatchHaveSDE);
			//			writeRows(fw, vecDontHaveSDE);


			FileOutputStream saveExcel = new FileOutputStream(folder+fileNameOut);

			workbook.write(saveExcel);
			workbook.close();

	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		
	}
	
	
	static void rerunChemicalsInExcelFile2() {
		
		String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 qsar ready standardizer\\results_819_QSAR-ready_CNL_edits_TMM\\";

		String filename="000 unit tests results_rnd100000 small list only inchi mismatch3.xlsx";
		String sheetName="InchiMismatch";
		
		JsonArray ja=ExcelCreator.convertExcelToJsonArray(folder+filename, 0, sheetName);

		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

		
		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			
			String Type=jo.get("Type").getAsString();
			
			if (Type.equals("original structure wrong"))continue;
			
			System.out.println(i);
			System.out.println(gson.toJson(jo));
			
			
		}
		
		
		
			
			
	}
	
	void compareSmilesInExcelFile() {

		System.out.println("enter compareSmilesInExcelFile");
		
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

		
		String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 qsar ready standardizer\\results_819_QSAR-ready_CNL_edits_TMM\\";
		
		String fileName="compare opera smiles if use SDE2 as original smiles.xlsx";
		String fileNameOut=fileName.replace(".xlsx", " with Structures.xlsx");		
				

		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(new File(folder+fileName));
			
	        Workbook workbook = new XSSFWorkbook(inputStream);
	        Sheet sheet = workbook.getSheetAt(0);
	        
	        	        
	        Row row0=sheet.getRow(0);	        
	        Hashtable<String,Integer>htColNames=new Hashtable<>();
	        for (int i=0;i<row0.getLastCellNum();i++) {
	        	htColNames.put(row0.getCell(i).getStringCellValue(), i);
	        	
	        }

	        
	        int colSmiles1=htColNames.get("OPERA_QSAR_READY");
	        int colSmiles2=htColNames.get("OPERA smiles from SDE2 smiles");
	        
	        
	        int colSmilesOriginal1=htColNames.get("Original");
	        int colSmilesOriginal2=htColNames.get("SDE_QSAR_READY_new_rules (TMM)");
	        
	        System.out.println(colSmiles1);
	        System.out.println(colSmiles2);
	        
	        JsonArray vec=new JsonArray();
	        
	        for (int i=1;i<=sheet.getLastRowNum();i++) {
	        	Row row=sheet.getRow(i);
	        	String DTXCID=row.getCell(htColNames.get("DTXCID")).getStringCellValue();

	        	
	        	String SmilesOriginal1=row.getCell(colSmilesOriginal1).getStringCellValue();
	        	String SmilesOriginal2=row.getCell(colSmilesOriginal2).getStringCellValue();

	        	
	        	String Smiles1=row.getCell(colSmiles1).getStringCellValue();
	        	String Smiles2=row.getCell(colSmiles2).getStringCellValue();
								
	        	JsonObject jo=new JsonObject();	        	
	        	jo.addProperty("DSSTOX_COMPOUND_ID", DTXCID);
	        	jo.addProperty("Smiles1",Smiles1);
	        	jo.addProperty("Smiles2",Smiles2);
	        	
	        	jo.addProperty("SmilesOriginal1",SmilesOriginal1);
	        	jo.addProperty("SmilesOriginal2",SmilesOriginal2);
	        	
	        	vec.add(jo);
	        	
	        }
	        
	        
	        workbook.close();
	        
			workbook = new XSSFWorkbook();
			writeRowsMatch(workbook,"compare",vec);
			
			//			writeRows(fw, vecMismatchHaveSDE);
			//			writeRows(fw, vecDontHaveSDE);


			FileOutputStream saveExcel = new FileOutputStream(folder+fileNameOut);

			workbook.write(saveExcel);
			workbook.close();

	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		
	}
	

	
	
	
	public static void main(String[] args) {
		SmilesStandardizationValidation s = new SmilesStandardizationValidation();
		int start = 1;
		int stop = 100000;
		configUnirest();

//		String server = "http://v2626umcth819.rtord.epa.gov:443";
		String server="https://hcd.rtpnc.epa.gov";
		
		
		//		String server="https://hazard-dev.sciencedataexperts.com";
		//		String workFlow="QSAR-ready_CNL_edits";
//		String workFlow = "QSAR-ready_CNL_edits_TMM";
		String workFlow = "QSAR-ready_CNL_edits_TMM_2";
		//		String workFlow="qsar-ready";

		String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 qsar ready standardizer\\";
		//		s.go(server,workFlow,folder,start,stop);		
		//		s.goThroughResults(server,workFlow,folder, start, stop);

		//		s.go2(server,workFlow,folder,start,stop);
		//		s.goThroughResults2(server,workFlow,folder, start, stop);

		
		int count = 10000;
		
//		System.out.println(count);
//		s.goRnd(server, workFlow, folder, count);
		
		s.goRndAllWorkflows(server, folder, count);
		
//		s.goThroughResultsRnd(server, workFlow, folder, count);
//		s.rerunChemicalsInExcelFile();
		
//		s.rerunChemicalsInJsonFile();
//		s.compareResultsInJsonFile();
		
//		s.compareSmilesInExcelFile();
		
//		s.rerunChemicalsInExcelFile2();
		
		
	}

}
