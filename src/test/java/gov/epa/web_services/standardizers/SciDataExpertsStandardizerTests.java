package gov.epa.web_services.standardizers;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.CDL;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
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
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.OPERA.Lookup;
import gov.epa.run_from_java.scripts.OPERA.SqliteUtilities;
import gov.epa.web_services.standardizers.Standardizer.StandardizeResponseWithStatus;
import kong.unirest.HttpResponse;

/**
 * See Comptox/000 qsar ready
 * standardizer/results_819_QSAR-ready_CNL_edits_TMM/000 unit tests
 * results_rnd100000 small list only inchi mismatch3.xlsx
 * 
 * @author TMARTI02
 *
 */
public class SciDataExpertsStandardizerTests {

	//	String serverEPA = "https://ccte-cced.epa.gov/api/stdizer/";
	String serverEPA="https://hcd.rtpnc.epa.gov/api/stdizer";
	static String serverHost="https://hcd.rtpnc.epa.gov";

//		static String workflow = "QSAR-ready_CNL_edits";
	//	static String workflow="QSAR-ready_CNL_edits_TMM";
	static String workflow = "QSAR-ready_CNL_edits_TMM_2";
	//	String workflow = "qsar-ready";

	boolean full=true;//whether to have full output from stdizer

	
	static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	static JsonArray jaResults=new JsonArray();

	SciDataExpertsStandardizer standardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY,workflow,serverHost);


	public static String toInchiKey(String mol) {
		try {
			Indigo indigo = new Indigo();
			indigo.setOption("ignore-stereochemistry-errors", true);

			IndigoInchi indigoInchi = new IndigoInchi(indigo);

			IndigoObject m = indigo.loadMolecule(mol);

			String key=indigoInchi.getInchiKey(indigoInchi.getInchi(m));
			String inchiKey = key != null ? key.substring(0, 14) : null;

			return inchiKey;

		} catch (IndigoException ex) {
			// log.error(ex.getMessage());
			return null;
		}
	}


	/**
	 * See if all of extra metal fragments are excluded when combining with ethanol
	 */
	@Test
	public void testMetals() {

		List<String> smilesMetals=makeMetalsList();
		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order
		for (String smilesMetal:smilesMetals) {
			ht.put("CCO."+smilesMetal, "CCO");
		}
		runChemicalsInHashtable(ht, "metals");
	}

	//	@Test
	public void testsMulticomponent() {
		Statement sqliteStatement=SqliteUtilities.getStatement(Lookup.conn);

		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		//		int countInExclusionList=0;

		try {

			//			Vector<String>inchiKeyExclude=SciDataExpertsStandardizer.getInchiExclusions(serverEPA+"groups/qsar-ready-exclusions_TMM");

			String excelFilePath="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 qsar ready standardizer\\results_819_QSAR-ready_CNL_edits_TMM\\000 unit tests results_rnd100000 multicomponent.xlsx";

			FileInputStream inputStream = new FileInputStream(excelFilePath);
			Workbook wb = new XSSFWorkbook(inputStream);

			Sheet sheet=wb.getSheetAt(0);

			int count=0;


			List<String>smilesExclude=new ArrayList<>();

			String excludePath="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 qsar ready standardizer\\results_819_QSAR-ready_CNL_edits_TMM\\run opera\\salt_solvent.txt";



			List<String>lines = Files.readAllLines(Paths.get(excludePath));

			for (String line:lines) {
				String exclude=line.split("\t")[0];
				smilesExclude.add(exclude);
				//		    	System.out.println(exclude);
			}

			//Exclude following to minimize errors-need to check what salt_solvent are for these
			//			addExtraSmilesTMM(smilesExclude);

			//Metal ions to add:
			//			excludeMetalsTMM(smilesExclude);

			List<String>inchiKeyExclude2=new ArrayList<>();
			for(String smi:smilesExclude) {
				inchiKeyExclude2.add(toInchiKey(smi));
			}

			List<String>smilesOperaExclude=new ArrayList<>();

			System.out.println("cid\tsmiles\tsmilesOpera\tsalt_solvent");

			for (int rowNum=1;rowNum<=sheet.getLastRowNum();rowNum++) {


				//				if(rowNum!=1367) continue;

				Row row=sheet.getRow(rowNum);

				String cid=row.getCell(0).getStringCellValue();
				String smiles=row.getCell(1).getStringCellValue();
				String smilesOpera=row.getCell(2).getStringCellValue();

				if(!smiles.contains(".")) {
					System.out.println(cid+"\tnot a mixture");
					continue;
				}

				//				System.out.println(smiles+"\t"+smilesOpera);

				List<String>smilesArray=new ArrayList<>();

				//				StandardizeResponseWithStatus s=SciDataExpertsStandardizer.callQsarReadyStandardize(serverEPA, workflow, smiles);
				//				String smilesSDE=s.standardizeResponse.qsarStandardizedSmiles;				
				//				if (smilesSDE!=null) {
				//					String [] vals=smilesSDE.split("\\.");									
				//					for(String val:vals) smilesArray.add(val);
				//				}

				String smilesSDE="";				
				if (row.getCell(3)!=null) {
					smilesSDE=row.getCell(3).getStringCellValue();				
					String [] vals=smilesSDE.split("\\.");									
					for(String val:vals) smilesArray.add(val);
				}


				String inchiKeySmilesOpera=toInchiKey(smilesOpera);


				if (inchiKeySmilesOpera==null) continue;

				for (int j=0;j<smilesArray.size();j++) {
					String smilesFrag=smilesArray.get(j);

					String inchiKeyFrag=toInchiKey(smilesFrag);

					//					boolean fragmentInExclusionList=inchiKeyExclude.contains(inchiKeyFrag);
					//					
					//					if (fragmentInExclusionList) {
					//						countInExclusionList++;
					//						System.out.println(rowNum+"\t"+j+"\t"+smilesArray.get(j)+"\t"+inchiKeyFrag+"\t"+fragmentInExclusionList);
					//					}

					if (inchiKeyExclude2.contains(inchiKeyFrag) || smilesFrag.contains("[*")) {
						smilesArray.remove(j--);
						continue;
					}

					if (inchiKeySmilesOpera.equals(inchiKeyFrag)) continue;

					if (smiles.contains("*")) continue;


					if (!smilesOpera.contains(smilesFrag) && !smilesFrag.contains("*")) {
						if(!smilesOperaExclude.contains(smilesFrag)) {
							smilesOperaExclude.add(smilesFrag);

							if (smilesFrag.length()<9999) {

								String smilesFrag2=SciDataExpertsStandardizer.runStandardize(smilesFrag,serverEPA, workflow, false).qsarReadySmiles;

								String inchiKeyFrag2=toInchiKey(smilesFrag2);								

								if (inchiKeyFrag2==null || !inchiKeyFrag2.equals(inchiKeySmilesOpera)) {
									//									System.out.println(cid+"\t"+smilesFrag+"\t"+smilesOpera);
									//									System.out.println(smilesSDE2);

									count++;
									JsonObject jo = new JsonObject();
									jo.addProperty("cid", cid);
									jo.addProperty("rowNum", rowNum);
									jo.addProperty("count", count);
									jo.addProperty("smiles", smiles);
									jo.addProperty("smilesOpera", smilesOpera);

									jo.addProperty("smilesSDE", smilesSDE);

									jo.addProperty("smilesFrag", smilesFrag);
									jo.addProperty("smilesFrag2", smilesFrag2);

									String sql="SELECT Salt_Solvent from Structure where DSSTOX_COMPOUND_ID='"+cid+"'";


									String salt_solvent="";


									ResultSet rs=SqliteUtilities.getRecords(sqliteStatement, sql);
									if(rs.next()) {
										salt_solvent=rs.getString(1);
										jo.addProperty("OPERA_Salt_Solvent", salt_solvent);
									}

									//									if(smilesFrag2==null)
									System.out.println(gson.toJson(jo));
									System.out.println(cid+"\t"+smiles+"\t"+smilesOpera+"\t"+salt_solvent);

								}
							}

						}
					}
				}

				if (smiles.isEmpty())  {
					continue;
				}

				Collections.sort(smilesArray);

				//Remove duplicate structures in the smiles array:
				for (int i=0;i<smilesArray.size()-1;i++) {
					if (smilesArray.get(i).equals(smilesArray.get(i+1))) {
						smilesArray.remove(i--);
					}
				}

				//				if (smilesArray.size()<2) {
				//					System.out.println(smiles+"\tArraySize=="+smilesArray.size());
				//				}

				//				if (smilesArray.size()>1) {
				//					count++;										
				//					JsonObject jo = new JsonObject();
				//					jo.addProperty("cid", cid);
				//					jo.addProperty("rowNum", rowNum);
				//					jo.addProperty("count", count);
				//					jo.addProperty("smiles", smiles);
				//					jo.addProperty("smilesOpera", smilesOpera);
				//					jo.addProperty("smilesSDE", arrayToSmilesString(smilesArray));
				//					System.out.println(gson.toJson(jo));
				////					System.out.println(rowNum+"\t"+count+"\t"+cid+"\t"+smiles+"\t"+smilesOpera+"\t"+arrayToSmilesString(smilesArray));
				//				}


				//				if(rowNum%10==0) {
				//					System.out.println(rowNum);
				//				}

			} //end loop over rows

			Collections.sort(smilesOperaExclude);

			//			for (String smi:smilesOperaExclude) {
			//				if(smi.length()>10) System.out.println(smi);
			//			}


		} catch (Exception ex) {
			ex.printStackTrace();
		}


		//		assertEquals(countInExclusionList,0);
	}


	private List<String> makeMetalsList() {
		List<String> smilesList=new ArrayList<>();

		smilesList.add("[Ag+]");
		smilesList.add("[Au+3]");
		smilesList.add("[Au+]");
		smilesList.add("[Br-]");
		smilesList.add("[Ca+2]");
		smilesList.add("[Cd+2]");
		smilesList.add("[Cl-]");
		smilesList.add("[Co+2]");
		smilesList.add("[Co+3]");
		smilesList.add("[Co]");
		smilesList.add("[Cr]");
		smilesList.add("[Cu+2]");
		smilesList.add("[Cu+]");
		smilesList.add("[Fe+2]");
		smilesList.add("[Fe+3]");
		smilesList.add("[Fe]");
		smilesList.add("[K+]");
		smilesList.add("[Mg+2]");
		smilesList.add("[Mn]");
		smilesList.add("[Mo]");
		smilesList.add("[NH4+]");
		smilesList.add("[Na+]");
		smilesList.add("[Ni+2]");
		smilesList.add("[Pd+2]");
		smilesList.add("[Pr+3]");
		smilesList.add("[Pt+2]");
		smilesList.add("[Pt+4]");

		smilesList.add("[Rb+]"); //Already have qsar exclusion

		smilesList.add("[Re]");//Already have qsar exclusion
		smilesList.add("[186Re]");

		smilesList.add("[Rh]");//already have qsar exclusion
		smilesList.add("[Rh+2]");
		smilesList.add("[Rh+2]I");
		smilesList.add("[Rh+3]");

		smilesList.add("[Ru]");//already have qsar exclusion
		smilesList.add("[Ru+2]");
		smilesList.add("[Ru+3]");

		smilesList.add("[Sb+3]");//already have qsar exclusion

		smilesList.add("[Ti+4]");
		smilesList.add("[Ti]");
		smilesList.add("[V+2]");
		smilesList.add("[W]");
		smilesList.add("[Zn+2]");
		smilesList.add("[Zn]");
		return smilesList;
	}


	//	private void addExtraSmilesTMM(List<String> smilesExclude) {
	//		smilesExclude.add("Cl[Ru+2]Cl");
	//		smilesExclude.add("O");
	//		smilesExclude.add("Cl[Rh]Cl");
	//		smilesExclude.add("Br[Os]");
	//		smilesExclude.add("C[NH-]");
	//		smilesExclude.add("CC(C)[NH-]");
	//		smilesExclude.add("[CH2-]CCC");
	//		smilesExclude.add("Cl[Ru]Cl");
	//		smilesExclude.add("CC#N");
	//		smilesExclude.add("C[Sn](C)C");
	//		smilesExclude.add("[CH3-]");
	//		smilesExclude.add("C1CO1");
	//		smilesExclude.add("CC(C)[O-]");
	//		smilesExclude.add("[CH2-]C=C");
	//		smilesExclude.add("C[Ge](C)C");
	//		smilesExclude.add("Cl[Fe](Cl)Cl");
	//		smilesExclude.add("CC[O-]");
	//		smilesExclude.add("N#[O+]");
	//		smilesExclude.add("F[P-](F)(F)(F)(F)F");	
	//		smilesExclude.add("Cl[Ru-](Cl)(Cl)Cl");
	//		smilesExclude.add("CC1CO1");
	//		smilesExclude.add("F[Sb-](F)(F)(F)(F)F");
	//		smilesExclude.add("[NH-]CC[NH-]");
	//		smilesExclude.add("OCC1CO1");
	//		smilesExclude.add("O=C1CCCCCO1");
	//		smilesExclude.add("[O-]CC([O-])=O");
	//		smilesExclude.add("C1CCCN1");
	//		smilesExclude.add("Cl[Ru](Cl)(Cl)(Cl)Cl");
	//	}

	public static String arrayToSmilesString(List<String>smilesArray) {
		String smiles="";

		for (int i=0;i<smilesArray.size();i++) {
			smiles+=smilesArray.get(i);
			if(i<smilesArray.size()-1) smiles+=".";
		}

		return smiles;

	}


	@Test
	//Nitrogen compounds that currently fail
	public void failsStandardization() {

		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order


		ht.put("ON=C1C2=CC=CC=C2C2=CC=CC=C2C2=CC=CC=C21","ON=C1C2=CC=CC=C2C2=CC=CC=C2C2=CC=CC=C21");
		ht.put("ON=C1C=CN(O)C2C=C(Cl)C=CC=21","ON=C1C=CN(O)C2C=C(Cl)C=CC=21");
		ht.put("ON(O)C1=CC=CC=C1C1=CC=C(C=C2C(=O)N(C(=S)N(C3C=CC=CC=3)C2=O)C2C=CC=CC=2)O1","[O-][N+](=O)C1C=CC=CC=1C1=CC=C(C=C2C(=O)N(C(=S)N(C3C=CC=CC=3)C2=O)C2C=CC=CC=2)O1");
		ht.put("O/N=C1/SC2=CC=CC=C2N/1CCS","ON=C1SC2=CC=CC=C2N1CCS");
		ht.put("CCC1N(CCC)C2C(=CC(=CC=2[N]=1[O])C(F)(F)F)[N+]([O-])=O","CCC[n]1c2c(cc(cc2[n](O)c1CC)C(F)(F)F)[N+]([O-])=O");
		ht.put("CCC(C)[N+]([O-])(O)C1C=CC(=CC=1)NC1C=CC=CC=1","CCC(C)[N+]([O-])(O)C1C=CC(=CC=1)NC1C=CC=CC=1");
		ht.put("CC1C=CC(=CC=1)C1=CC(C=C(O1)C1C=CC=CC=1)=NO","CC1C=CC(=CC=1)C1=CC(C=C(O1)C1C=CC=CC=1)=NO");
		ht.put("CC1=C2N=C3C(NC(=N)N=C3O)=NCC2C2CNC3NC(=N)N=C(O)C=3N12","CC1=C2N=C3C(NC(=N)N=C3O)=NCC2C2CN=C3NC(=N)N=C(O)C3N12");
		ht.put("CC1=C(/C(=N/O)/C2=C(OC=C(C2=O)[N+]([O-])=O)N1O)[N+]([O-])=O","CC1=C(C(=NO)C2=C(OC=C(C2=O)[N+]([O-])=O)N1O)[N+]([O-])=O");
		ht.put("CC(C)(CC1CCCCC1N(O)N(O)C1CCCCC1CC(C)(C)N(O)O)N(O)O","CC(C)(CC1CCCCC1N(O)N(O)C1CCCCC1CC(C)(C)[N+]([O-])=O)[N+]([O-])=O");
		ht.put("CC(=O)N(O)CCCCCN=C(O)CCC(=O)N(O)CCCCCN=C(O)CCC(O)=NCCCCCN(O)O","CC(=O)N(O)CCCCCN=C(O)CCC(=O)N(O)CCCCCN=C(O)CCC(O)=NCCCCC[N+]([O-])=O");

		runChemicalsInHashtable(ht,"Fails standardization");
	}


	@Test
	//Nitrogen compounds that currently fail
	public void zwitterions() {

		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order

		ht.put("Cl.CC(=O)OC(CC([O-])=O)C[N+](C)(C)C","CC(=O)OC(CC([O-])=O)C[N+](C)(C)C");//zwitterion
		ht.put("CC1=CN(C2CC(O)C(CC/C(/[O-])=C\\[N+]#N)O2)C(=O)NC1=O","CC1=CN(C2CC(O)C(CCC([O-])=C[N+]#N)O2)C(=O)NC1=O");//zwitterion
		ht.put("[O-]N1C=C[N+]1=O","[O-]N1C=C[N+]1=O");//zwitterion
		ht.put("N#[N+]C=C([O-])CCC1(C(=C1C1C=CC=CC=1)C1C=CC=CC=1)C1C=CC=CC=1","N#[N+]C=C([O-])CCC1(C(=C1C1C=CC=CC=1)C1C=CC=CC=1)C1C=CC=CC=1");//zwitterion
		ht.put("[C-]#[N+]C(=CC1=CC=CS1)C(=O)OC(C)C","[C-]#[N+]C(=CC1=CC=CS1)C(=O)OC(C)C");//zwitterion
		ht.put("[C-]#[N+]C1=CC=C(Br)C=C1C(=O)C1=CC=CC=C1F","[C-]#[N+]C1=CC=C(Br)C=C1C(=O)C1=CC=CC=C1F");//zwitterion
		ht.put("[O-]S(=O)(=O)SCCC(=O)N1CC[N+]2(CC[N+]3(CC2)CCN(CC3)C(=O)CCSS([O-])(=O)=O)CC1","[O-]S(=O)(=O)SCCC(=O)N1CC[N+]2(CC[N+]3(CC2)CCN(CC3)C(=O)CCSS([O-])(=O)=O)CC1");//zwitterion
		ht.put("OC1=CC=NC([Se-])=[NH+]1","OC1=CC=NC([Se-])=[NH+]1");//zwitterion
		ht.put("Br.CCCCCOC1=CC=CC=C1/N=C(\\[O-])/OCC[N+]1(CCCCCCCCCC)CCCCC1","CCCCCOC1=CC=CC=C1N=C([O-])OCC[N+]1(CCCCCCCCCC)CCCCC1");//zwitterion
		ht.put("[C-]#[N+]C1=CC(Cl)=C(C=C1OC)OC","[C-]#[N+]C1=CC(Cl)=C(C=C1OC)OC");//shouldn’t neutralize [C-]#[N+]
		ht.put("[N-]=[N+]=NC1C=CC(CCOP([O-])(=O)OP([O-])([O-])=O)=CC=1","[N-]=[N+]=NC1C=CC(CCOP(O)(=O)OP(O)(O)=O)=CC=1");//zwitterion
		ht.put("[Mg+2].[O-][N+]1C=C(C=CC=1)C([O-])=O.[O-][N+]1C=C(C=CC=1)C([O-])=O","[O-][N+]1=CC(=CC=C1)C(O)=O");//zwitterion


		runChemicalsInHashtable(ht,"Zwitterions");
	}


	private void runChemicalsInHashtable(LinkedHashMap<String, String> ht,String type) {

		Set<String> smilesList = ht.keySet();

		int countMatch = 0;

		for (String smiles : smilesList) {

			//					StandardizeResponseWithStatus responseWithStatus = SciDataExpertsStandardizer
			//							.callQsarReadyStandardize(serverEPA, workflow, smiles);
			//					String qsarSmiles=responseWithStatus.standardizeResponse.qsarStandardizedSmiles;

			
			HttpResponse<String>response=standardizer.callQsarReadyStandardizePost(smiles,full);
			String jsonResponse=SciDataExpertsStandardizer.getResponseBody(response, full);
			String qsarSmiles=SciDataExpertsStandardizer.getQsarReadySmilesFromPostJson(jsonResponse, full);
			String changes=SciDataExpertsStandardizer.getChangesApplied(jsonResponse);

			if(changes.isEmpty()) changes="None";
			
			
//			System.out.println(smiles+"\t"+qsarSmiles);
			
			//	System.out.println(jsonResponse);
			//	if (responseWithStatus.standardizeResponse.qsarStandardizedSmiles == null) {

			JsonObject jo = new JsonObject();
			jo.addProperty("smiles", smiles);
			jo.addProperty("smilesExpected", ht.get(smiles));
			jo.addProperty("type",type);
			jo.addProperty("match",false);
			jo.addProperty("changes",changes);

			//			System.out.println(qsarSmiles);

			if (qsarSmiles == null) {
				jo.addProperty("smilesSDE", "null");
				//				System.out.println(gson.toJson(jo));
			} else {

				jo.addProperty("smilesSDE", qsarSmiles);

				String inchiKey1=toInchiKey(ht.get(smiles));
				String inchiKey2=toInchiKey(qsarSmiles);

				if (inchiKey1!=null && inchiKey2!=null && (qsarSmiles.equals(ht.get(smiles)) || inchiKey1.equals(inchiKey2))) {
					countMatch++;
					jo.addProperty("match",true);
					//				System.out.println(gson.toJson(jo));
				} 
			}

//			if(type.equals("Fails standardization") && jo.get("match").getAsBoolean()) {
//				System.out.println(qsarSmiles+"\t"+jsonResponse);
//			}

			
			jaResults.add(jo);

			//			System.out.println(gson.toJson(jo));
		}

//		System.out.println(type+"\t"+countMatch + " of " + ht.size() + " match");

		//		for (String smiles:badSmiles) {
		//			System.out.println(smiles);
		//		}

		assertEquals(ht.size(), countMatch);
	}

	@BeforeClass
	public static void printInfo() {
		System.out.println(serverHost);
		System.out.println(workflow+"\n");
	}

	@AfterClass
	public static void saveResults() {

		int countMatch=0;

		for (int i=0;i<jaResults.size();i++) {
			if(jaResults.get(i).getAsJsonObject().get("match").getAsBoolean()) {
				countMatch++;
			}
		}
		//TODO create excel file
		System.out.println("Overall\t"+countMatch + " of " + jaResults.size() + " match\n");
		//		 System.out.println(gson.toJson(jaResults));

		
//		for (int i = 0; i < jaResults.size(); i++) {
//			JsonObject jo = jaResults.get(i).getAsJsonObject();
//			String smiles=jo.get("smiles").getAsString();
//			System.out.println(smiles);
//		}

		XSSFWorkbook workbook = new XSSFWorkbook();

		try {

			writeResultsByType(jaResults, workbook);
			
//			if(true) return;
			

			Hashtable<String, JsonObject> htLookup = getLookupHashtable();
			writeRows(workbook,"compare",jaResults,htLookup);

			
			String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 qsar ready standardizer\\results_"+serverHost.replace("https://", "")+"\\";
			String fileNameOut="test_results_"+workflow+".xlsx";

			File Folder=new File(folder);
			Folder.mkdirs();


			System.out.println(folder+fileNameOut);

			FileOutputStream saveExcel = new FileOutputStream(folder+fileNameOut);

			workbook.write(saveExcel);
			workbook.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 


	}
	
	static void writeResultsByType(JsonArray ja,Workbook wb) {
		HashMap <String,JsonArray>ht=new HashMap<>();
		
		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			
			if(ht.get(jo.get("type").getAsString())==null) {
				JsonArray ja2=new JsonArray();
				ja2.add(jo);
				ht.put(jo.get("type").getAsString(),ja2);
			} else {
				JsonArray ja2=ht.get(jo.get("type").getAsString());
				ja2.add(jo);
			}
		}
		
		
		Sheet sheet=wb.createSheet("summary");

		Row row1 = sheet.createRow(0);

		row1.createCell(0).setCellValue("Type");
		row1.createCell(1).setCellValue("Count");
		row1.createCell(2).setCellValue("Number");
		
		int countMatchOverall=0;
		int irow=1;
		
		for (String key:ht.keySet()) {
			JsonArray ja2=ht.get(key);
			int countMatch=0;
			for (int i=0;i<ja2.size();i++) {
				JsonObject jo=ja2.get(i).getAsJsonObject();
				if(jo.get("match").getAsBoolean()) countMatch++;
				if(jo.get("match").getAsBoolean()) countMatchOverall++;
			}
			
			Row rowi = sheet.createRow(irow++);
			
			rowi.createCell(0).setCellValue(key);
			rowi.createCell(1).setCellValue(countMatch);
			rowi.createCell(2).setCellValue(ja2.size());
		}
		
		Row rowi = sheet.createRow(irow++);
		
		rowi.createCell(0).setCellValue("Overall");
		rowi.createCell(1).setCellValue(countMatchOverall);
		rowi.createCell(2).setCellValue(ja.size());

		
		sheet.setColumnWidth(0, 60*256);	
		
//		System.out.println(gson.toJson(jaResults));

	}
	


	private static Hashtable<String, JsonObject> getLookupHashtable() throws FileNotFoundException {
		String filename="DSSTox_082021_Structures.csv";
		InputStream inputStream = new FileInputStream("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 qsar ready standardizer\\"+filename);
		String csvAsString = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
		String json = CDL.toJSONArray(csvAsString).toString();
		JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);

		Hashtable<String,JsonObject>htLookup=new Hashtable<>();

		for (int i=1;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			String originalSmiles=jo.get("Original_SMILES").getAsString();
			htLookup.put(originalSmiles, jo);
			
//			System.out.println(originalSmiles);
			
			//				
		}

		System.out.println("done getting lookup hashtable");
		return htLookup;
	}


	private static void writeRows(Workbook workbook,String sheetName,JsonArray jaResults, Hashtable<String, JsonObject> htLookup) throws IOException, CDKException {

		Sheet sheet=workbook.createSheet(sheetName);

		Row row1 = sheet.createRow(0);

		row1.createCell(0).setCellValue("cid");
		row1.createCell(1).setCellValue("Original");
		row1.createCell(2).setCellValue("Smiles_OPERA");
		row1.createCell(3).setCellValue("Smiles_Expected");
		row1.createCell(4).setCellValue("Smiles_SDE");
		row1.createCell(5).setCellValue("SDE Matches Expected");
		row1.createCell(6).setCellValue("Type");
		row1.createCell(7).setCellValue("Changes");

		
		for (int i=1;i<=7;i++) {
			sheet.setColumnWidth(i, 60*256);	
		}
		

		for (int i = 0; i < jaResults.size(); i++) {

			JsonObject jo = jaResults.get(i).getAsJsonObject();

			int irow=(i+1);
			Row rowi = sheet.createRow(irow);

			String Original_Smiles = jo.get("smiles").getAsString();
			
			String cid=null;
			String smilesOpera=null;
						
			if (htLookup.get(Original_Smiles)!=null) {
				JsonObject joLookup=htLookup.get(Original_Smiles);
				cid=joLookup.get("DSSTOX_COMPOUND_ID").getAsString();
				smilesOpera=joLookup.get("Canonical_QSARr").getAsString();
			}
			

			String smilesExpected = jo.get("smilesExpected").getAsString();
			String smilesSDE = jo.get("smilesSDE").getAsString();
			boolean match = jo.get("match").getAsBoolean();
			String type = jo.get("type").getAsString();
			String changes = jo.get("changes").getAsString();

			//				String inchiKeyOPERA=toInchiIndigo(QSAR_Ready_SMILES_OPERA).inchiKey;
			//				String inchiKeySDE=toInchiIndigo(QSAR_Ready_SMILES_SDE).inchiKey;

			rowi.createCell(0).setCellValue(cid);

			rowi.createCell(1).setCellValue(Original_Smiles);
			rowi.createCell(2).setCellValue(smilesOpera);
			rowi.createCell(3).setCellValue(smilesExpected);
			rowi.createCell(4).setCellValue(smilesSDE);

			rowi.createCell(5).setCellValue(match);
			rowi.createCell(6).setCellValue(type);
			rowi.createCell(7).setCellValue(changes);

			//				rowi.createCell(4).setCellValue(inchiKeyOPERA.equals(inchiKeySDE));

			rowi.setHeight((short)2000);

			SmilesStandardizationValidation.createImage(Original_Smiles, irow, 1, sheet, 1);
			SmilesStandardizationValidation.createImage(smilesOpera, irow, 2, sheet, 1);
			SmilesStandardizationValidation.createImage(smilesExpected, irow, 3, sheet, 1);
			SmilesStandardizationValidation.createImage(smilesSDE, irow, 4, sheet, 1);

			rowi.setHeight((short)(2000*1.15));//add some space for smiles at bottom

		}

	}



	@Test
	public void runIsotopes() {
		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order
		ht.put("[2H]C1C([2H])=C([2H])C([2H])=C([2H])C=1[2H]","[2H]C1C([2H])=C([2H])C([2H])=C([2H])C=1[2H]");//OPERA gets rid of isotope info
		runChemicalsInHashtable(ht, "Isotopes");
	}


	@Test
	public void runSe_plus() {
		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order
		ht.put("C[Se+](C)CCO","C[SeH](C)CCO");//[Se+]
		runChemicalsInHashtable(ht, "[Se+]");
	}

	@Test
	public void runCdO_plus() {
		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order
		ht.put("COC=[O+]C.F[P-](F)(F)(F)(F)F","COCOC");//C=O+
		runChemicalsInHashtable(ht, "C=O+");
	}

	@Test
	public void runNdO_plus() {
		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order
		ht.put("CC[O+]=NC.[O-]Cl(=O)(=O)=O","CCONC");//N=O+
		runChemicalsInHashtable(ht, "N=O+");
	}


	@Test
	public void runSiO2() {
		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order
		ht.put("[Na+].CO[Si]([O-])=O","CO[Si](O)=O");//failed to neutralize [O-]
		runChemicalsInHashtable(ht, "[Si]([O-])=O");
	}

	@Test
	public void runIrCO() {
		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order
		ht.put("[Ir].[Ir].[Ir].[Ir].[C-]#[O+].[C-]#[O+].[C-]#[O+].[C-]#[O+].[C-]#[O+].[C-]#[O+].[C-]#[O+].[C-]#[O+].[C-]#[O+].[C-]#[O+].[C-]#[O+].[C-]#[O+]","[C-]#[O+]");//need to add exclusion. Exclusions currently not working
		runChemicalsInHashtable(ht, "[Ir].[C-]#[O+]");
	}



	@Test
	public void runSplus() {
		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order
		ht.put("C[S+](C)C1=CC2SC3C=C(C=CC=3NC=2C=C1)[S+](C)C.[O-]S([O-])(=O)=O.OS(O)(=O)=O","CS(C)C1=CC2SC3=CC(=CC=C3NC=2C=C1)S(C)C");//[S+]
		runChemicalsInHashtable(ht, "[S+]");
	}

	@Test
	public void runCdSplus() {
		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order
		ht.put("[I-].CN(C)C(=[S+]C1C=C(Br)C=CC=1)N(C)C","CN(C)C(=SC1=CC(Br)=CC=C1)N(C)C");//C=[S+]
		runChemicalsInHashtable(ht, "C=[S+]");
	}

	@Test
	public void runSi_minus() {
		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order
		ht.put("[Li+].C[Si](C)(C)O[Si](C)(C1C=CC=CC=1)[Si-](C)C1C=CC=CC=1","C[Si](C)(C)O[Si](C)(C1C=CC=CC=1)[SiH](C)C1C=CC=CC=1");//SDE fails to neutralize [Si-]
		runChemicalsInHashtable(ht, "[Si-]");
	}


	@Test
	public void runSi_dO_plus() {
		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order
		ht.put("C/C(=C\\C(C)=O)/[O+]=[Si](O/C(/C)=C\\C(C)=O)O/C(/C)=C/C(C)=O.F[As-](F)(F)(F)(F)F","CC(=O)C=C(C)O[SiH](OC(C)=CC(C)=O)OC(C)=CC(C)=O");//Si=[O+]
		runChemicalsInHashtable(ht, "Si=[O+]");
	}

	@Test
	public void runO_dC_dOplus() {
		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order
		ht.put("O=C=[O+][Si](F)(F)F","O=CO[Si](F)(F)F");//O=C=[O+]
		runChemicalsInHashtable(ht, "O=C=[O+]");
	}


	@Test
	public void runC_dC_Ominus() {
		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order
		ht.put("CCCC=C[O-]","CCCCC=O");//What is neutral form of C=C[O-]?
		runChemicalsInHashtable(ht, "C=C[O-]");
	}

	@Test
	public void runSimpleSalt() {
		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order
		ht.put("CCCCC.Cl","CCCCC");//O=C=[O+]
		runChemicalsInHashtable(ht, "Simple salt");
	}



	@Test
	public void runCationRing() {
		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order
		ht.put("C1=CC=CC=[O+]1","C1=CC=CC=[O+]1");//keep as cation? Is ring aromatic?
		runChemicalsInHashtable(ht, "ring with O+");
	}

	@Test
	public void runBminus() {
		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order
		ht.put("[Na+].O[B-](C1C=CC=CC=1)(C1C=CC=CC=1)C1C=CC=CC=1","O[B-](C1C=CC=CC=1)(C1C=CC=CC=1)C1C=CC=CC=1");//[B-]
		runChemicalsInHashtable(ht, "[B-]");
	}


	@Test
	public void runKetoneVsAlcohol() {
		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order
		ht.put("[Pr+3].CC(C)(C)/C(/[O-])=C/C(=O)C(F)(F)C(F)(F)C(F)(F)F.CC(C)(C)/C(/[O-])=C/C(=O)C(F)(F)C(F)(F)C(F)(F)F.CC(C)(C)/C(/[O-])=C/C(=O)C(F)(F)C(F)(F)C(F)(F)F","CC(C)(C)C(=O)CC(=O)C(F)(F)C(F)(F)C(F)(F)F");//Ketone vs alcohol by double bond shift
		runChemicalsInHashtable(ht, "runKetoneVsAlcohol");
	}

	@Test
	public void runOperaMakesBadAromaticRings() {
		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order
		ht.put("CCN(CC)C1=CC2[O+]=C3C=C(C=CC3=C(C3=CC=CC=C3C(=O)OCCCOC3C=CC(=CC=3)C3=C4C=CC(S4)=C(C4C=CC(N=4)=C(C4=CC=C(S4)C(=C4C=CC3=N4)C3C=CC=CC=3)C3C=CC=CC=3)C3C=CC=CC=3)C=2C=C1)N(CC)CC","CCN(CC)C1=CC2=[O+]C3=CC(=CC=C3C(C3=CC=CC=C3C(=O)OCCCOC3C=CC(=CC=3)C3C4=CC=C(S4)C(=C4C=CC(=N4)C(=C4C=CC(S4)=C(C4C=CC=3N=4)C3C=CC=CC=3)C3C=CC=CC=3)C3C=CC=CC=3)=C2C=C1)N(CC)CC");//OPERA makes unparseable aromatic rings
		runChemicalsInHashtable(ht, "Opera makes bad aromatic rings");
	}


	@Test
	public void runCationCantNeutralize() {
		LinkedHashMap<String, String> ht = new LinkedHashMap<>();// preserves order
		ht.put("[I-].CC1C=C(C=C(C)C=1C#[N+][O-])[N+](C)(C)C","CC1C=C(C=C(C)C=1C#[N+][O-])[N+](C)(C)C");//cant be neutralized?
		runChemicalsInHashtable(ht, "Cation that can't be neutralized");
	}

}
