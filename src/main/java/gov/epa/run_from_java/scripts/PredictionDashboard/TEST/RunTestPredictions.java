package gov.epa.run_from_java.scripts.PredictionDashboard.TEST;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
//import org.openscience.cdk.io.MDLV2000Reader;
//import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.smiles.SmilesParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.GenericSubstance;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionDashboardTableMaps;
import gov.epa.util.JsonUtilities;
//import gov.epa.test.api.predict.TestApi;
import gov.epa.util.StructureUtil;
import gov.epa.util.StructureUtil.APIMolecule;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 * @author TMARTI02
 */
public class RunTestPredictions {

	CreateJsonFiles cjf = new CreateJsonFiles();
	RunPredictions rp = new RunPredictions();
	MoleculeCreator mc=new MoleculeCreator();

	boolean debug = false;
	
	private static final Logger logger = LogManager.getLogger(RunTestPredictions.class);

	int nfiles = 10;
//	int nfiles = 1;
	
	String server = "http://v2626umcth882.rtord.epa.gov";
//	String server="http://localhost";
	String snapshot = "snapshot-2025-12-31";
	
	String sourceName="TEST5.1.3";
	
	String folderSrc = "data\\dsstox\\" + snapshot + "\\missing "+sourceName+" predictions\\";
	String folderDest = "data\\TEST5.1.3\\reports\\" + snapshot + "\\";
	int minPort=8081;

	
	public static String strCAS = "CAS";// Need it to be capitalized for things like the batch table
	public static String strName = "name";
	public static String strCID = "cid";
	public static String strSID = "sid";
	public static String strGSID = "gsid";
	public static String strSmiles = "smiles";
	public static String strInchi = "inchi";
	public static String strInchiKey = "inchiKey";
	public static String strInchiKey1 = "inchiKey1";
	public static String strMol = "mol";
	public static String strMolWeight = "molWeight";

	
	
	class RunPredictions {

		void runWithThreads() {
			for (int i = 1; i <= nfiles; i++) {
//			for (int i = 2; i <= 2; i++) {
				MyRunnableTask task = new MyRunnableTask(i);
				Thread thread = new Thread(task, "Thread-" + i);
				thread.start(); // Starts the thread, which calls the run() method
			}
		}

	}

	class MoleculeCreator {

		private static IAtomContainer getMoleculeFromSmiles(SmilesParser sp, IAtomContainer molecule) {
			AtomContainer molecule2 = null;

			String smiles = null;

			if (molecule.getProperty("smiles") != null) {
				smiles = molecule.getProperty("smiles");
			} else if (molecule.getProperty("SMILES") != null) {
				smiles = molecule.getProperty("SMILES");
			}

			if (smiles != null) {
				try {
					molecule2 = (AtomContainer) sp.parseSmiles(smiles);
					// System.out.println(DTXCID+"\t"+smiles+"\t"+molecule2.getAtomCount());
				} catch (Exception ex) {
					molecule2 = new AtomContainer();
				}

			} else {
				molecule2 = new AtomContainer();
			}

			molecule2.setProperties(molecule.getProperties());
			return molecule2;
		}



		public static String getStringStructure(BufferedReader br) {

			try {
				String type = "V2000";

				String strStructure = "";

				while (true) {
					String Line = br.readLine();

					if (Line == null)
						return null;
					// System.out.println(Line);
					if (Line.contains("V30 BEGIN CTAB"))
						type = "V3000";

					// System.out.println(Line);
					strStructure += Line + "\r\n";
					if (Line.contains("M  END"))
						break;
				}

				return strStructure;

			} catch (Exception ex) {
				// ex.printStackTrace();
				return null;
			}
		}

		
		public static String getDsstoxCompoundString(DsstoxCompound compound) {
			
			String strMolecule=null;
			
			try {
				strMolecule = compound.getMolFile();
				strMolecule += "\n";
			} catch (Exception ex) {
				return null;
			}

			GenericSubstance gs = compound.getGenericSubstanceCompound().getGenericSubstance();
			Hashtable<String, Object> htProperties = new Hashtable<>();
			
			
			htProperties.put(strCID, compound.getDsstoxCompoundId());
			htProperties.put(strSID, gs.getDsstoxSubstanceId());
			htProperties.put(strCAS, gs.getCasrn());
			htProperties.put(strName, gs.getPreferredName());

			if (compound.getSmiles() != null)
				htProperties.put(strSmiles, compound.getSmiles());

			if (compound.getMolWeight() != null)
				htProperties.put(strMolWeight, compound.getMolWeight());

			if (compound.getIndigoInchikey() != null)
				htProperties.put(strInchiKey, compound.getIndigoInchikey());

			for (String property : htProperties.keySet()) {
				strMolecule += "> <" + property + ">\n";
				strMolecule += htProperties.get(property) + "\n\n";
			}

			strMolecule += "$$$\n";
			return strMolecule;
		}
		
		private static void getProperties(BufferedReader br, IAtomContainer molecule) throws IOException {
			String value = null;
			String field = null;

			while (true) {

				String Line = br.readLine();

				// System.out.println(Line);

				if (Line.contains(">  <") || Line.contains("><") || Line.contains("> <")) {
					if (field != null && value != null) {
						molecule.setProperty(field, value);
						// System.out.println(field+"\t"+value+"\n");
					}
					field = Line.substring(Line.indexOf("<") + 1, Line.length() - 1);
					value = null;
					// System.out.println(field);
				} else if (Line.contains("$$$")) {
					molecule.setProperty(field, value);
					break;
				} else if (Line.trim().length() > 0) {

					if (value == null)
						value = Line;
					else
						value += "\n" + Line;
				}

			}

			// System.out.println(gson.toJson(molecule.getProperties()));
		}
	}

	class MyRunnableTask implements Runnable {
		private int num;

		public MyRunnableTask(int num) {
			this.num = num;
		}

		@Override
		public void run() {
//			System.out.println("num " + num + " is running in thread: " + Thread.currentThread().getName());
			runFromJson(num);
		}

		private void runFromJson(int num) {

			boolean removeAlreadyRan = true;

			int maxCount = -1;// set to -1 to run all in sdf
//			int maxCount = 100;// set to -1 to run all in sdf

			int port = 8081 + num - 1;

			if (num > nfiles)
				port -= nfiles;

			new File(folderDest).mkdirs();

			String filenameSrcJson = "compounds_part_" + num + ".json";
			String filenameDestJson = filenameSrcJson;

			boolean skipMissingSID = true;
			String srcJsonPath = folderSrc + filenameSrcJson;
			String destJsonPath = folderDest + filenameDestJson;
			System.out.println("num=" + num + ",fileName=" + filenameSrcJson);

			runJson_all_endpoints_write_continuously_use_api(srcJsonPath, destJsonPath, skipMissingSID, maxCount,
					removeAlreadyRan, server, port);
		}

		private String getFieldFromJson(String line, String fieldName) {
			String value = line.substring(line.indexOf("\"" + fieldName + "\":\""), line.length());
			value = value.substring(fieldName.length() + 4, value.length());
			value = value.substring(0, value.indexOf("\""));
//			System.out.println(value);
			return value;
		}

		private int removeAlreadyRanChemicals(String destJsonPath, List<DsstoxCompound> compounds) {
//			System.out.println(destJsonPath);

			Hashtable<String, Integer> htCountByDTXCID = new Hashtable<>();

			try {

				File file = new File(destJsonPath);

				BufferedReader br = new BufferedReader(new FileReader(destJsonPath));

				int counter = 0;

				while (true) {
					String line = br.readLine();

					if (line == null)
						break;

					counter++;

					try {

						if (!line.contains("DTXCID"))
							continue;

						String dtxcid = getFieldFromJson(line, "DTXCID");
						String error = getFieldFromJson(line, "error");

						if (!line.contains("Q2_Test") && !line.contains("SP_Test") && error.equals("")) {
							// if doesnt have the stats then it didnt finish writing the line
							System.out.println(destJsonPath + "\t" + dtxcid + "\t" + line);
							continue;
						}

//						if (counter % 100000 == 0)
//							System.out.println("\t" + file.getName() + "\t" + counter);

						// System.out.println(pr.getDTXCID());

//						if (htCountByDTXCID.containsKey(pr.getDTXCID())) {
//							htCountByDTXCID.put(pr.getDTXCID(), htCountByDTXCID.get(pr.getDTXCID()) + 1);
//						} else {
//							htCountByDTXCID.put(pr.getDTXCID(), 1);
//						}

						if (htCountByDTXCID.containsKey(dtxcid)) {
							htCountByDTXCID.put(dtxcid, htCountByDTXCID.get(dtxcid) + 1);
						} else {
							htCountByDTXCID.put(dtxcid, 1);
						}

					} catch (Exception ex) {
//						JsonObject jo = gsonNotPretty.fromJson(line, JsonObject.class);
//						System.out.println(Utilities.toJson(jo));
					}

				}

				br.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// for (String line:lines) {
			// System.out.println(gson.toJson(htCountByDTXCID));
			int count = 0;

			Iterator<DsstoxCompound> iterator = compounds.iterator();
			while (iterator.hasNext()) {
				DsstoxCompound dc = iterator.next();

				if (dc.getDsstoxCompoundId()==null)continue;
				
				if (htCountByDTXCID.containsKey(dc.getDsstoxCompoundId())) {
					if (htCountByDTXCID.get(dc.getDsstoxCompoundId()) == 16) {
						iterator.remove(); // Removes safely
						count++;
					}
				}
			}
//			System.out.println("Removed " + count + " chemicals since already ran");
			return count;
		}


		

		private void runJson_all_endpoints_write_continuously_use_api(String srcJsonPath, String destJsonPath,
				boolean skipMissingSID, int maxCount, boolean removeAlreadyRan, String server, int port) {

//			List<String>skipDtxsids=Arrays.asList("DTXSID90332347","DTXSID101484125","DTXSID801336080");
			List<String> skipDtxsids = new ArrayList<>();

			Gson gson = new Gson();
			long beforeUsedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

			Type listOfMyClassObject = new TypeToken<List<DsstoxCompound>>() {
			}.getType();

			try {
				List<DsstoxCompound> compounds = gson.fromJson(new FileReader(srcJsonPath), listOfMyClassObject);

				if (debug)
					System.out.println("atom container count in json=" + compounds.size());

				File destFile = new File(destJsonPath);
				FileWriter fw;
				int countRan = 0;

				if (removeAlreadyRan) {
					if (destFile.exists()) {
						countRan = removeAlreadyRanChemicals(destJsonPath, compounds);
						if (debug)
							System.out.println(countRan + " removed since already ran");
					}
					fw = new FileWriter(destJsonPath, Charset.forName("UTF-8"), destFile.exists());
				} else {
					fw = new FileWriter(destJsonPath);
				}

				System.gc(); // Request garbage collection to get a more accurate 'after' reading


				// if(true)return;

				if (compounds.size() == 0) {
					if (debug)
						System.out.println("All chemicals ran");
					return;
				}

				System.out.println(srcJsonPath+ ", atom container count to run=" + compounds.size());
				
				if (debug)
					System.out.println("");

				if (debug)
					System.out.println("");

				int counter = countRan;

				for (DsstoxCompound dc:compounds) {
					
					if(dc.getDsstoxCompoundId()==null) {
						System.out.println(dc.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId()+"\tmissing dtxcid\t"+srcJsonPath);
						continue;
					}
					
												
					// for (APIMolecule molecule:molecules) {

					String dtxsid = dc.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId();

					if (skipDtxsids.contains(dtxsid)) {
						System.out.println("Skipping " + dtxsid);
						continue;
					}

					// if (debug)
					// System.out.println((i+countRan)+"\t"+destFile.getName()+"\t"+ac.getProperty("SMILES")+"");

					if (debug)
						logger.info("{}\t{}\t{}\t{}", (++counter + countRan), destFile.getName(), dtxsid,
								dc.getSmiles());

//					System.out.println(dc.toString());
//					if(true)return;

//					System.out.println("Running "+dtxsid+" from "+srcJsonPath);
//					System.out.println(counter+"\tRunning "+dtxsid+", "+dc.getSmiles());
					
					
					
//					System.out.println("Done running "+dtxsid);
					
					String molFile=MoleculeCreator.getDsstoxCompoundString(dc);
					
					if(molFile==null) {
						System.out.println(dc.getDsstoxCompoundId()+"\tError getting molFile");
						continue;
					}
					
					String results = runPredictionFromMolFileString(dc, molFile, server, port);
					
					if (results == null)
						continue;

					JsonArray ja = gson.fromJson(results, JsonArray.class);

					for (int i = 0; i < ja.size(); i++) {
						JsonObject jo = ja.get(i).getAsJsonObject();
						fw.write(gson.toJson(jo) + "\r\n");
						fw.flush();
					}

				}
				fw.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		public String runPredictionFromMolFileString(DsstoxCompound dc, String molFile, String server, int port) {

			String dtxsid = null;
			if(dc.getGenericSubstanceCompound()!=null) {
				dtxsid=dc.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId();
			}
			System.out.println("Running "+dc.getDsstoxCompoundId()+"\t"+dtxsid+"\t"+dc.getSmiles());

			
			try {

				// System.out.println(molFile);
				// if(ac.getAtomCount()==0)
				// System.out.println(smiles+"\t"+sid+"\t"+ac.getAtomCount());
				// if(true)return null;
				

				PostInput pi = new PostInput(molFile);
				Gson gson = new Gson();
				// System.out.println(gson.toJson(pi));

				String url = server + ":" + port + "/predictPost";
				// System.out.println(url);
				
//				System.out.println("\n"+gson.toJson(pi)+"\n");
				
				HttpResponse<String> responsePost = Unirest.post(url).header("Content-Type", "application/json")
						.body(gson.toJson(pi)).asString();

				// System.out.println(responsePost.getBody().toString());

				String json = responsePost.getBody().toString();
				return json;

			} catch (Exception e) {
				
				System.out.println("Couldnt get prediction for "+dc.getDsstoxCompoundId()+"\t"+dtxsid+"\t"+dc.getSmiles());
				logger.error("Couldnt get prediction for "+dc.getDsstoxCompoundId()+"\t"+dtxsid+"\t"+dc.getSmiles());
				return null;
			}

		}
	}

	public static class PostInput {
		public String molecule;// needs to be public or have getter method

		PostInput() {
		}// also need this constructor or it wont work

		PostInput(String molecule) {// molecule is in V3000 format with property block (DTXSID, CASRN, etc)
			this.molecule = molecule;
		}
	}

	class CreateJsonFiles {

		
		void convertCompoundJsonsToSdfs(String folder) {

			Type listOfMyClassObject = new TypeToken<List<DsstoxCompound>>() {}.getType();

			
			int totalCompounds=0;
			
			for (File file:new File(folder).listFiles()) {
				
				if(!file.getName().contains(".json")) continue;
				
				try {
					
					List<DsstoxCompound> compounds = JsonUtilities.gson.fromJson(new FileReader(file), listOfMyClassObject);
					
					System.out.println(file.getName()+"\t"+compounds.size());
					
					totalCompounds+=compounds.size();
				
					
					FileWriter fw=new FileWriter(file.getAbsolutePath().replace(".json", ".sdf"));
					
					
					for (DsstoxCompound compound:compounds) {
						
						fw.write(compound.getMolFile());
						
						Hashtable<String,String>ht=new Hashtable<>();
						ht.put("DTXSID", compound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId());
						ht.put("DTXCID", compound.getDsstoxCompoundId());
						ht.put("ID", compound.getDsstoxCompoundId());
						if(compound.getSmiles()!=null)
							ht.put("SMILES", compound.getSmiles());
						ht.put("PREFERRED_NAME", compound.getGenericSubstanceCompound().getGenericSubstance().getPreferredName());
						
						for(String key:ht.keySet()) {
							fw.write("\r\n");
							fw.write(">  <"+key+">\r\n");
							fw.write(ht.get(key)+"\r\n");
						}
						fw.write("\r\n$$$$\r\n");
						fw.flush();
					}
					fw.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
//				if(true)break;
			}
			
			System.out.println("totalCompounds=\t"+totalCompounds);
		}
		
		
		// Main: control max number of files (no empty-file emission)
		public static void saveCompoundsToJsonFiles(List<DsstoxCompound> compounds, String outputDir, int numFiles) {

			int total = compounds.size();
			int base = total / numFiles;
			int remainder = total % numFiles;

			Gson gson = new GsonBuilder().setPrettyPrinting().create();

			int start = 0;

			int totalOut = 0;

			for (int i = 0; i < numFiles; i++) {
				int size = base + (i < remainder ? 1 : 0);
				List<DsstoxCompound> chunk = compounds.subList(start, start + size);

				totalOut += chunk.size();

				String filename = "compounds_part_" + (i + 1) + ".json";
				String filepath = outputDir + File.separator + filename;
				System.out.println(filename + "\t" + chunk.size());

				try (FileWriter w = new FileWriter(filepath, StandardCharsets.UTF_8)) {
					w.write(gson.toJson(chunk));
					w.flush();
					w.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				start += size;
			}

			System.out.println(totalOut + "\t" + compounds.size());
		}
		
		public static void saveCompoundsToSdfFiles(List<DsstoxCompound> compounds, String outputDir, int numFiles) {

			int total = compounds.size();
			int base = total / numFiles;
			int remainder = total % numFiles;

			Gson gson = new GsonBuilder().setPrettyPrinting().create();

			int start = 0;

			int totalOut = 0;

			for (int i = 0; i < numFiles; i++) {
				int size = base + (i < remainder ? 1 : 0);
				List<DsstoxCompound> chunk = compounds.subList(start, start + size);

				totalOut += chunk.size();

				String filename = "compounds_part_" + (i + 1) + ".sdf";
				String filepath = outputDir + File.separator + filename;
				System.out.println(filename + "\t" + chunk.size());

				try (FileWriter w = new FileWriter(filepath, StandardCharsets.UTF_8)) {
					
					for(DsstoxCompound c:chunk) {
						w.write(c.getMolFile());
						w.write(">  <DTXCID>\r\n");
						w.write(c.getDsstoxCompoundId()+"\r\n");
						w.write("$$$$\r\n");
					}
					
					w.write(gson.toJson(chunk));
					w.flush();
					w.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				start += size;
			}

			System.out.println(totalOut + "\t" + compounds.size());
		}

		List<DsstoxCompound> getCompoundsForMissingDtxcids(HashSet<String> missingCids,String sourceName) {

			String snapshot = "snapshot-2025-12-31";
			String folder = "data\\dsstox\\" + snapshot;

			HashSet<String> dtxcids = new HashSet<>();
			HashSet<String> dtxsids = new HashSet<>();

			List<DsstoxCompound> compounds = new ArrayList<>();
			HashSet<String> cidsRetrieved = new HashSet<>();

			for (File file : new File(folder).listFiles()) {
				if (!file.getName().contains(".sdf"))
					continue;

				List<APIMolecule> mols = StructureUtil.readSDF_to_API_Molecules(file.getAbsolutePath(), -1);

				for (APIMolecule mol : mols) {
					DsstoxCompound compound = new DsstoxCompound(mol);

					if (cidsRetrieved.contains(compound.getDsstoxCompoundId())) {
						continue;
					}
					
					if(compound.getDsstoxCompoundId()==null) continue;
					

					if (missingCids.contains(compound.getDsstoxCompoundId())) {
						compounds.add(compound);
						cidsRetrieved.add(compound.getDsstoxCompoundId());
					}
				}
				System.out.println(file.getName() + "\t" + compounds.size());
			}

			String folderOut = folder + File.separator + "missing "+sourceName+" predictions";
			
			new File(folderOut).mkdirs();
			
			saveCompoundsToJsonFiles(compounds, folderOut, 10);
			

			return compounds;
		}

		// private HashSet<String>getDtxcidsWithTestPredictions() {
		//
		//// Dotenv dotenv = Dotenv.load();
		//// String dbHost = dotenv.get("DEV_QSAR_HOST");
		//// String host = System.getenv().get("DEV_QSAR_HOST");
		//// System.out.println(dbHost+"\t"+host);
		//
		// HashSet<String>dtxcids=new HashSet<>();
		//
		// String sql = "select distinct pd.dtxcid\r\n"
		// + "from qsar_models.predictions_dashboard pd\r\n"
		// + "join qsar_models.models m on m.id=pd.fk_model_id\r\n"
		// + "join qsar_models.sources s on m.fk_source_id = s.id\r\n"
		// + "join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id\r\n"
		// + "where s.name='TEST5.1.3';";
		//
		// System.out.println(sql);
		//
		// ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		//
		// try {
		// while(rs.next()) {
		// String dtxcid=rs.getString(1);
		// dtxcids.add(dtxcid);
		// System.out.println(dtxcid);
		// }
		// } catch (SQLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		//
		// return dtxcids;
		//
		// }

		private HashSet<String> getDtxcidsFromDsstoxSdfs() {

			String snapshot = "snapshot-2025-12-31";
			String folder = "data\\dsstox\\" + snapshot;

			HashSet<String> dtxcids = new HashSet<>();
			HashSet<String> dtxsids = new HashSet<>();
			for (File file : new File(folder).listFiles()) {
				if (!file.getName().contains(".sdf"))
					continue;

				List<APIMolecule> mols = StructureUtil.readSDF_to_API_Molecules(file.getAbsolutePath(), -1);

				for (APIMolecule mol : mols) {

					if (mol.htProperties.containsKey("DTXCID")) {
						dtxcids.add((String) mol.htProperties.get("DTXCID"));
					}

					if (mol.htProperties.containsKey("DTXSID")) {
						dtxsids.add((String) mol.htProperties.get("DTXSID"));
					}

				}
				System.out.println(file.getName() + "\t" + dtxsids.size() + "\t" + dtxcids.size());
			}

			return dtxcids;

		}

		// private HashSet<String>getDtxcidsWithTestPredictions() {
		//
		//// Dotenv dotenv = Dotenv.load();
		//// String dbHost = dotenv.get("DEV_QSAR_HOST");
		//// String host = System.getenv().get("DEV_QSAR_HOST");
		//// System.out.println(dbHost+"\t"+host);
		//
		// HashSet<String>dtxcids=new HashSet<>();
		//
		// String sql = "select distinct pd.dtxcid\r\n"
		// + "from qsar_models.predictions_dashboard pd\r\n"
		// + "join qsar_models.models m on m.id=pd.fk_model_id\r\n"
		// + "join qsar_models.sources s on m.fk_source_id = s.id\r\n"
		// + "join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id\r\n"
		// + "where s.name='TEST5.1.3';";
		//
		// System.out.println(sql);
		//
		// ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		//
		// try {
		// while(rs.next()) {
		// String dtxcid=rs.getString(1);
		// dtxcids.add(dtxcid);
		// System.out.println(dtxcid);
		// }
		// } catch (SQLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		//
		// return dtxcids;
		//
		// }

		HashSet<String> getDtxcidsInSnapshotWithoutPredictions(String sourceName) {

			HashSet<String> dtxcids = new HashSet<>();

			String sql = "select distinct dr.dtxcid\r\n" + "from qsar_models.dsstox_records dr\r\n"
					+ "where dr.fk_dsstox_snapshot_id = 4\r\n" + "and not exists (\r\n" + "select 1\r\n"
					+ "   from qsar_models.predictions_dashboard pd\r\n"
					+ "   join qsar_models.models m on m.id = pd.fk_model_id\r\n"
					+ "   join qsar_models.sources s on s.id = m.fk_source_id\r\n"
					+ "   where pd.dtxcid = dr.dtxcid\r\n" + "   and s.name = '"+sourceName+"' and dr.dtxcid is not null\r\n"
					+ ");\r\n" + "";

			// System.out.println(sql);
			ResultSet rs = SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);

			try {
				while (rs.next()) {
					String dtxcid = rs.getString(1);
					dtxcids.add(dtxcid);
//					 System.out.println(dtxcid);
				}
				
				System.out.println("missing dtxcids="+dtxcids.size());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return dtxcids;

		}

	}

//	private HashSet<String>getDtxcidsWithTestPredictions() {
//
////		Dotenv dotenv = Dotenv.load();
////		String dbHost = dotenv.get("DEV_QSAR_HOST");
////		String host = System.getenv().get("DEV_QSAR_HOST");
////		System.out.println(dbHost+"\t"+host);
//
//		HashSet<String>dtxcids=new HashSet<>();
//		
//		String sql = "select distinct pd.dtxcid\r\n"
//				+ "from qsar_models.predictions_dashboard pd\r\n"
//				+ "join qsar_models.models m on m.id=pd.fk_model_id\r\n"
//				+ "join qsar_models.sources s on m.fk_source_id = s.id\r\n"
//				+ "join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id\r\n"
//				+ "where s.name='TEST5.1.3';";
//		
//		System.out.println(sql);
//		
//		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
//		
//		try {
//			while(rs.next()) {
//				String dtxcid=rs.getString(1);
//				dtxcids.add(dtxcid);
//				System.out.println(dtxcid);
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//		return dtxcids;
//		
//	}
	
	void runSingleFromCompoundJson() {
		
		int port=8081;
		
		Type listOfMyClassObject = new TypeToken<List<DsstoxCompound>>() {}.getType();
		
		String srcJsonPath="data\\dsstox\\snapshot-2025-12-31\\missing TEST predictions\\compounds_part_1.json";

		
		try {
			
			List<DsstoxCompound> compounds = JsonUtilities.gson.fromJson(new FileReader(srcJsonPath), listOfMyClassObject);
			
//			Running DTXSID6047448 from data\dsstox\snapshot-2025-12-31\missing TEST predictions\compounds_part_1.json

			
			for (DsstoxCompound compound:compounds) {
				
				String dtxsid =compound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId();
				
				if (dtxsid.equals("DTXSID6047448")) {
//				if(compound.getSmiles()!=null && !compound.getSmiles().contains(".")) {
					
					String molFile=MoleculeCreator.getDsstoxCompoundString(compound);
					
					PostInput pi = new PostInput(molFile);
					
					System.out.println(pi.molecule);
					
					
//					System.out.println("Input: "+JsonUtilities.gson.toJson(pi));

					// System.out.println(gson.toJson(pi));

					String url = server + ":" + port + "/predictPost";
					// System.out.println(url);
//					System.out.println("\n"+gson.toJson(pi)+"\n");
					
					HttpResponse<String> responsePost = Unirest.post(url).header("Content-Type", "application/json")
							.body(JsonUtilities.gson.toJson(pi)).asString();
					String json = responsePost.getBody().toString();
//					System.out.println(json);
					
					
					JsonArray ja = JsonUtilities.gson.fromJson(json, JsonArray.class);
					
					System.out.println("Output: "+JsonUtilities.gson.toJson(ja));
					

					break;
				}
				
			}
			
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	void makeSureTestDatasetChemicalsAreInDsstoxSnapshot() {
		
		String filepathJsonCasLookup="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18_EPA_Github\\jar\\add dependencies\\Datasets-1.1.1\\gov\\epa\\webtest\\DsstoxRecord_lookup_from_cas.json";

		JsonObject joCasDict=JsonUtilities.getJsonObjectFromJsonFile(filepathJsonCasLookup);
		
//		System.out.println(joCasDict==null);
	
		PredictionDashboardTableMaps p=new PredictionDashboardTableMaps();
		p.getDsstoxRecordsFromJsonExport(PredictionDashboardTableMaps.fileJsonDsstoxRecords2025_12_31);

		Set<String> keys = joCasDict.keySet();
        for (String key : keys) {
            JsonObject joDsstoxRecord = joCasDict.get(key).getAsJsonObject();
            String dtxcid=joDsstoxRecord.get("cid").getAsString();
            String dtxsid=joDsstoxRecord.get("sid").getAsString();
            
            String smiles=null;
            
            if (joDsstoxRecord.get("smiles")!=null && !joDsstoxRecord.get("smiles").isJsonNull()) {
            	smiles=joDsstoxRecord.get("smiles").getAsString();	
            }
            
            if (!p.mapDsstoxRecordsBySID.containsKey(dtxsid) || !p.mapDsstoxRecordsByCID.containsKey(dtxcid)) {
            	String sql="select dsstox_compound_id from compounds where dsstox_compound_id ='"+dtxcid+"';";
            	
//            	System.out.println(sql);
            	
            	String dtxcid_prod=SqlUtilities.runSQL(SqlUtilities.getConnectionDSSTOX(), sql);
            	if(dtxcid_prod==null) {
                	System.out.println(key+"\t"+p.mapDsstoxRecordsBySID.containsKey(dtxsid)+"\t"+p.mapDsstoxRecordsByCID.containsKey(dtxcid)+"\t"+smiles);
            	}
            }
        }
		
		
		
	}
	
	
	public static void main(String[] args) {
		RunTestPredictions rtp = new RunTestPredictions();

//		String sourceName="TEST5.1.3";
//		String sourceName="Percepta2025.1.4";
		String sourceName="OPERA2.8";
		HashSet<String>missingDtxcids=rtp.cjf.getDtxcidsInSnapshotWithoutPredictions(sourceName);
		
		
		List<DsstoxCompound>missingCompounds=rtp.cjf.getCompoundsForMissingDtxcids(missingDtxcids,sourceName);
		rtp.cjf.convertCompoundJsonsToSdfs("data\\dsstox\\snapshot-2025-12-31\\missing "+sourceName+" predictions");
		

//		rtp.makeSureTestDatasetChemicalsAreInDsstoxSnapshot();
//		rtp.runSingleFromCompoundJson();
//		rtp.rp.runWithThreads();
		

	}

}
