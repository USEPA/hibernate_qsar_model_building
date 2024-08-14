package gov.epa.run_from_java.scripts.DSSTOX_Loading;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.CDL;
import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Value;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.DsstoxSession;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.GenericSubstance;
import gov.epa.databases.dsstox.entity.GenericSubstanceCompound;
import gov.epa.databases.dsstox.service.DsstoxCompoundServiceImpl;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.util.StructureUtil;
import gov.epa.util.StructureUtil.Inchi;

/**
 * @author TMARTI02
 */
public class DSSTOX_Compounds_Script {


	String lanId="tmarti02";
	Connection conn=SqlUtilities.getConnectionDSSTOX();

	
	/**
	 * Pulls compounds from Dsstox snapshot using sql query
	 * 
	 * @param offset
	 * @param limit
	 * @return
	 */
	List<DsstoxCompound> getCompoundsBySQL(int offset,int limit) {

		List<DsstoxCompound>compounds=new ArrayList<>();

		String sql="SELECT dsstox_compound_id,mol_file,smiles, jchem_inchi_key,indigo_inchi_key,mol_weight,gs.dsstox_substance_id, gs.casrn\n";  
		sql+="FROM compounds c\n";
		sql+="left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id\n";
		sql+="left join generic_substances gs on gs.id=gsc.fk_generic_substance_id\n"; 
		//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null) and gs.dsstox_substance_id is not null\n";
//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null)\n";
		sql+="ORDER BY dsstox_compound_id\n";
		sql+="LIMIT "+limit+" OFFSET "+offset;

		System.out.println(sql);

		ResultSet rs=SqlUtilities.runSQL2(conn, sql);

		try {
			while (rs.next()) {
				DsstoxCompound compound=new DsstoxCompound();				

				compound.setDsstoxCompoundId(rs.getString(1));

				if (rs.getString(2)!=null)
					compound.setMolFile(rs.getString(2));

				if (rs.getString(3)!=null)
					compound.setSmiles(rs.getString(3));

				if (rs.getString(4)!=null)
					compound.setJchemInchikey(rs.getString(4));

				if (rs.getString(5)!=null)
					compound.setIndigoInchikey(rs.getString(5));				

				if (rs.getString(6)!=null)
					compound.setMolWeight(Double.parseDouble(rs.getString(6)));

				if (rs.getString(7)!=null) {
					GenericSubstanceCompound gsc=new GenericSubstanceCompound();
					GenericSubstance gs=new GenericSubstance(); 
					compound.setGenericSubstanceCompound(gsc);
					gsc.setGenericSubstance(gs);
					gs.setDsstoxSubstanceId(rs.getString(7));

					if (rs.getString(8)!=null) {
						gs.setCasrn(rs.getString(8));
					}

				}

				compounds.add(compound);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		System.out.println(compounds.size());
		return compounds;
	}

	
	static public DsstoxCompound getCompoundByDTXCID(String dtxcid) {

		String sql="SELECT dsstox_compound_id,mol_file,smiles, jchem_inchi_key,indigo_inchi_key,mol_weight,gs.dsstox_substance_id, gs.casrn\n";  
		sql+="FROM compounds c\n";
		sql+="left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id\n";
		sql+="left join generic_substances gs on gs.id=gsc.fk_generic_substance_id\n"; 
		//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null) and gs.dsstox_substance_id is not null\n";
		sql+="where dsstox_compound_id='"+dtxcid+"';\n";

//		System.out.println(sql);

		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionDSSTOX(), sql);

		try {
			while (rs.next()) {
				DsstoxCompound compound=new DsstoxCompound();				

				compound.setDsstoxCompoundId(rs.getString(1));

				if (rs.getString(2)!=null)
					compound.setMolFile(rs.getString(2));

				if (rs.getString(3)!=null)
					compound.setSmiles(rs.getString(3));

				if (rs.getString(4)!=null)
					compound.setJchemInchikey(rs.getString(4));

				if (rs.getString(5)!=null)
					compound.setIndigoInchikey(rs.getString(5));				

				if (rs.getString(6)!=null)
					compound.setMolWeight(Double.parseDouble(rs.getString(6)));

				if (rs.getString(7)!=null) {
					GenericSubstanceCompound gsc=new GenericSubstanceCompound();
					GenericSubstance gs=new GenericSubstance(); 
					compound.setGenericSubstanceCompound(gsc);
					gsc.setGenericSubstance(gs);
					gs.setDsstoxSubstanceId(rs.getString(7));

					if (rs.getString(8)!=null) {
						gs.setCasrn(rs.getString(8));
					}

				}
				return compound;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}


	/**
	 * Writes compounds table to a series of json files in 50K chunks
	 */
	void compoundsToJsonFiles() {

		//		DsstoxCompoundServiceImpl compoundService=new DsstoxCompoundServiceImpl();
		//		List<DsstoxCompound>compounds=compoundService.findAll();

		int batchSize=50000;
		int i=0;

		while(true) {

//			File file=new File("data/dsstox/json/snapshot_compounds"+(i+1)+".json");
			File file=new File("data/dsstox/json/prod_compounds"+(i+1)+".json");

			if (file.exists()) {
				i++;
				continue;
			}

			List<DsstoxCompound>compounds=getCompoundsBySQL(i*batchSize, batchSize);

			if(compounds.size()==0) {
				break;
			} else {

				try {
					FileWriter fw=new FileWriter(file);
					fw.write(Utilities.gson.toJson(compounds));

					System.out.println((i+1)+"\t"+compounds.size());

					fw.flush();
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				i++;
			}
		}

	}

	/**
	 * Writes compounds table to a single tsv file (pulls from database 50K at a time)
	 * 
	 */
	void backupCompoundsInchis() {

		//		DsstoxCompoundServiceImpl compoundService=new DsstoxCompoundServiceImpl();
		//		List<DsstoxCompound>compounds=compoundService.findAll();

		int batchSize=50000;
		int i=0;

		File file=new File("data/dsstox/backup/snapshot_compounds_inchi_backup.tsv");

		try {

			FileWriter fw=new FileWriter(file);

			fw.write("dsstox_compound_id\tjchem_inchi_key\tindigo_inchi_key\t"
					+ "inchi\tjchem_inchi\tindigo_inchi\r\n");

			while(true) {

				List<DsstoxCompound>compounds=getCompoundsBySQL_Inchi(i*batchSize, batchSize);

				if(compounds.size()==0) {
					break;
				} else {
					System.out.println((i+1)+"\t"+compounds.size());

					for (DsstoxCompound compound:compounds) {

						if(compound.getInchi()!=null) {
							compound.setInchi(compound.getInchi().split("\n")[0]);
						}
						
						if(compound.getIndigoInchi()!=null) {
							compound.setIndigoInchi(compound.getIndigoInchi().split("\n")[0]);
						}

						if(compound.getJchemInchi()!=null) {
							compound.setJchemInchi(compound.getJchemInchi().split("\n")[0]);
						}


						fw.write(compound.getDsstoxCompoundId()+"\t"+ compound.getJchemInchikey()+"\t"+compound.getIndigoInchikey()+"\t"+
								compound.getInchi()+"\t"+compound.getJchemInchi()+"\t"+compound.getIndigoInchi()+"\r\n");
					}

					fw.flush();
					i++;
				}
			}

			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * Uses gabriels hibernate service- runs slowwww
	 */
	void backupCompoundsInchis2() {

		DsstoxCompoundServiceImpl compoundService=new DsstoxCompoundServiceImpl();

		int batchSize=50000;
		int i=0;

		File file=new File("data/dsstox/backup/snapshot_compounds_inchi_backup.tsv");

		try {

			FileWriter fw=new FileWriter(file);

			fw.write("dsstox_compound_id\tinchi\tjchem_inchi_key\tindigo_inchi_key\tjchem_inchi\tindigo_inchi\r\n");

			while(true) {

				List<DsstoxCompound>compounds=compoundService.findAll(i*batchSize, batchSize);

				if(compounds.size()==0) {
					break;
				} else {
					System.out.println((i+1)+"\t"+compounds.size());

					for (DsstoxCompound compound:compounds) {

						if(compound.getInchi()!=null) {
							compound.setInchi(compound.getInchi().split("\n")[0]);
						}

						fw.write(compound.getDsstoxCompoundId()+"\t"+compound.getInchi()+"\t"+
								compound.getJchemInchikey()+"\t"+compound.getIndigoInchikey()+"\t"+
								compound.getJchemInchi()+"\t"+compound.getIndigoInchi()+"\r\n");
					}

					fw.flush();
					i++;
				}
			}

			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private List<DsstoxCompound> getCompoundsBySQL_Inchi(int offset, int limit) {
		List<DsstoxCompound>compounds=new ArrayList<>();

		String sql="SELECT dsstox_compound_id, inchi, jchem_inchi_key, indigo_inchi_key,jchem_inchi, indigo_inchi, mol_file\n";  
		sql+="FROM compounds c\n";
		//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null) and gs.dsstox_substance_id is not null\n";
		sql+="ORDER BY dsstox_compound_id\n";
		sql+="LIMIT "+limit+" OFFSET "+offset;

		//		System.out.println(sql);

		ResultSet rs=SqlUtilities.runSQL2(conn, sql);

		try {
			while (rs.next()) {
				DsstoxCompound compound=new DsstoxCompound();				

				compound.setDsstoxCompoundId(rs.getString(1));

				if (rs.getString(2)!=null)
					compound.setInchi(rs.getString(2));

				if (rs.getString(3)!=null)
					compound.setJchemInchikey(rs.getString(3));

				if (rs.getString(4)!=null)
					compound.setIndigoInchikey(rs.getString(4));				

				if (rs.getString(5)!=null)
					compound.setJchemInchi(rs.getString(5));				

				if (rs.getString(6)!=null)
					compound.setIndigoInchi(rs.getString(6));
				
				if (rs.getString(7)!=null)
					compound.setMolFile(rs.getString(7));				


				compounds.add(compound);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		System.out.println(compounds.size());
		return compounds;


	}


	public static void createRecord(ResultSet rs, Object r) {
		ResultSetMetaData rsmd;
		try {
			rsmd = rs.getMetaData();

			int columnCount = rsmd.getColumnCount();

			// The column count starts from 1
			for (int i = 1; i <= columnCount; i++ ) {
				String name = rsmd.getColumnLabel(i);

				String val=rs.getString(i);

				//				System.out.println(name+"\t"+val);

				if (val!=null) {
					Field myField = r.getClass().getDeclaredField(name);	

					String type=myField.getType().getName();

					if (type.contentEquals("boolean")) {
						myField.setBoolean(r, Boolean.parseBoolean(val));
					} else if (type.contentEquals("double")) {
						myField.setDouble(r, Double.parseDouble(val));
					} else if (type.contentEquals("int")) {
						myField.setInt(r, Integer.parseInt(val));

					} else if (type.contentEquals("java.lang.Double")) {
						//						System.out.println(name+"\tDouble");
						try {
							Double dval=Double.parseDouble(val);						
							myField.set(r, dval);
						} catch (Exception ex) {
							System.out.println("Error parsing "+val+" for field "+name+" to Double for "+rs.getString(1));
						}
					} else if (type.contentEquals("java.lang.Integer")) {
						Integer ival=Integer.parseInt(val);
						myField.setInt(r,ival);
					} else if (type.contentEquals("java.lang.String")) {
						myField.set(r, val);
					} else if (type.contentEquals("java.util.Set")) {
						//						System.out.println(name+"\t"+val);
						val=val.replace("[", "").replace("]", "");

						String  [] values = val.split(", ");
						Set<String>list=new HashSet<>();
						for (String value:values) {
							list.add(value.trim());
						}
						myField.set(r,list);

					} else if (type.contentEquals("java.util.List")) {
						//						System.out.println(name+"\t"+val);
						val=val.replace("[", "").replace("]", "");

						String  [] values = val.split(",");
						ArrayList<String>list=new ArrayList<>();
						for (String value:values) {
							list.add(value.trim());
						}
						myField.set(r,list);

					} else {
						System.out.println("Need to implement: "+myField.getType().getName());
					}					

				}

			}


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<DsstoxCompound> getCompounds(int offset, int limit) {
		List<DsstoxCompound>compounds=new ArrayList<>();

		String sql="SELECT * FROM compounds c\n";
		sql+="ORDER BY dsstox_compound_id\n";
		sql+="LIMIT "+limit+" OFFSET "+offset;

		System.out.println(sql);

		ResultSet rs=SqlUtilities.runSQL2(conn, sql);


		try {
			while (rs.next()) {
				DsstoxCompound compound=new DsstoxCompound();				





				compounds.add(compound);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		System.out.println(compounds.size());
		return compounds;


	}
	/**
	 * Writes compounds table to a single tsv file (pulls from database 50K at a time)
	 * 
	 */
	void compoundsToTSV_File() {

		//		DsstoxCompoundServiceImpl compoundService=new DsstoxCompoundServiceImpl();
		//		List<DsstoxCompound>compounds=compoundService.findAll();

		int batchSize=50000;
		int i=0;

		File file=new File("data/dsstox/tsv/snapshot_compounds.tsv");

		try {

			FileWriter fw=new FileWriter(file);

			fw.write("cid\tsid\tsmiles\r\n");

			while(true) {

				List<DsstoxCompound>compounds=getCompoundsBySQL(i*batchSize, batchSize);

				if(compounds.size()==0) {
					break;
				} else {

					System.out.println((i+1)+"\t"+compounds.size());

					for (DsstoxCompound compound:compounds) {

						fw.write(compound.getDsstoxCompoundId()+"\t");

						if (compound.getGenericSubstanceCompound()!=null) {
							fw.write(compound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId()+"\t");
						} else {
							fw.write("N/A\t");
						}

						fw.write(compound.getSmiles()+"\r\n");
					}

					fw.flush();
					i++;
				}
			}


			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	static class OutputInchiIndigo {

		String dtxcid;
		String inchiNew;
		String inchiIndigoNew;
		String inchiKeyIndigoNew;

		static List<String>fieldNames=Arrays.asList("dtxcid","inchiNew","inchiIndigoNew","inchiKeyIndigoNew");
				
		OutputInchiIndigo(DsstoxCompound compound) {
			
			dtxcid=compound.getDsstoxCompoundId();
			
			Inchi inchiIndigo=StructureUtil.toInchiIndigo(compound.getMolFile());

			if (inchiIndigo!=null) {
				inchiIndigoNew=inchiIndigo.inchi;
				inchiKeyIndigoNew=inchiIndigo.inchiKey;
//				System.out.println(inchiKeyIndigoNew);
			} 

			if (compound.getInchi()!=null) {
//				System.out.println("Have inchi...");
				compound.setInchi(compound.getInchi().split("\n")[0]);	//get rid of aux line
				inchiNew=compound.getInchi();//use old value if available
			
			} else if (compound.getJchemInchi()!=null) {//doesnt happen
				System.out.println("Have jchem inchi...");
				compound.setJchemInchi(compound.getJchemInchi().split("\n")[0]);	//get rid of aux line
				inchiNew=compound.getJchemInchi();
				
			} else {
				if (inchiIndigo!=null) {
					System.out.println(compound.getDsstoxCompoundId()+"\tOriginal inchi was null, but have from indigo");
					inchiNew=inchiIndigo.inchi;
				} 
				 
			}

		}

		@Override
		public String toString() {

			String result="";

			for (int i=0;i<fieldNames.size();i++) {
				
				try {
					Field field= this.getClass().getDeclaredField(fieldNames.get(i));
					result+=field.get(this);
					
					if(i<fieldNames.size()-1) result+="\t";
					else result+="\r\n";

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
			}
			return result;
		}

		static String getHeader() {
			String header="";
			for (int i=0;i<fieldNames.size();i++) {
				header+=fieldNames.get(i);
				if(i<fieldNames.size()-1) header+="\t";
				else header+="\r\n";
			}
			return header;
		}
		
	}


	/**
	 * Writes compounds table to a single tsv file with indigo inchi keys
	 * 
	 */
	void generateNewIndigoValuesToTSV() {

		//		DsstoxCompoundServiceImpl compoundService=new DsstoxCompoundServiceImpl();
		//		List<DsstoxCompound>compounds=compoundService.findAll();

		int batchSize=10000;
		int i=0;

		File file=new File("data/dsstox/tsv/snapshot_compounds_indigo_inchi_key.tsv");

		File fileIndigoChanged=new File("data/dsstox/tsv/snapshot_compounds_indigo_inchi_key_changed.tsv");

		try {

			FileWriter fw=new FileWriter(file);
			fw.write(OutputInchiIndigo.getHeader());


			FileWriter fwChanged=new FileWriter(fileIndigoChanged);
			fwChanged.write(OutputInchiIndigo.getHeader());

			while(true) {

				List<DsstoxCompound>compounds=getCompoundsBySQL_Inchi(i*batchSize, batchSize);

				if(compounds.size()==0) {
					break;
				} else {

					System.out.println((i+1)+"\t"+compounds.size());

					for (DsstoxCompound compound:compounds) {

						//						if(compound.getGenericSubstanceCompound()==null) continue;//skip if have no SID

						OutputInchiIndigo outputLine=new OutputInchiIndigo(compound); 

						fw.write(outputLine.toString());
						fw.flush();	

						if (outputLine.inchiKeyIndigoNew==null || !outputLine.inchiKeyIndigoNew.equals(compound.getIndigoInchikey())) {

							if(outputLine.inchiKeyIndigoNew==null && compound.getIndigoInchikey()==null) {
								continue;
							}

							if(compound.getIndigoInchikey()==null) {
								continue;
							}

							//							System.out.println(outputLine.inchiKeyIndigoNew+"\t"+compound.getIndigoInchikey());

							fwChanged.write(outputLine.toString());
							fwChanged.flush();	
						}
					}

					i++;
				}
			}

			fw.close();
			fwChanged.close();


			//			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}




	/**
	 * Converts files in json folder to sdf format and saves to sdf folder
	 * 
	 */
	void convertJsonsToSDFs() {

		try {

			File folder=new File("data/dsstox/json");

			Type listOfMyClassObject = new TypeToken<List<DsstoxCompound>>() {}.getType();

			for (File file:folder.listFiles()) {

				if(!file.getName().contains("json")) {
					continue;
				}

				try {
					List<DsstoxCompound>compounds=Utilities.gson.fromJson(new FileReader(file), listOfMyClassObject);

					String filename=file.getName().replace(".json", ".sdf");
					String filepath="data/dsstox/sdf/"+filename;
					System.out.println(filename);
					createSDF(compounds, filepath);

				} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
					e.printStackTrace();
				}
			}


		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}


	public static void createSDF(List<DsstoxCompound> compounds, String filepath) throws IOException {
		FileWriter fw=new FileWriter(filepath);

		for (DsstoxCompound compound:compounds) {
			fw.write(compound.getMolFile());
			fw.write(">  <DTXCID>\n");
			fw.write(compound.getDsstoxCompoundId()+"\r\n\r\n");

			if (compound.getSmiles()!=null) {
				fw.write(">  <smiles>\n");
				fw.write(compound.getSmiles()+"\r\n\r\n");
			}								

			if (compound.getGenericSubstanceCompound()!=null && compound.getGenericSubstanceCompound().getGenericSubstance()!=null) {
				if(compound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId()!=null) {
					fw.write(">  <DTXSID>\n");
					fw.write(compound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId()+"\r\n\r\n");
				}

				if(compound.getGenericSubstanceCompound().getGenericSubstance().getCasrn()!=null) {
					fw.write(">  <CASRN>\n");
					fw.write(compound.getGenericSubstanceCompound().getGenericSubstance().getCasrn()+"\r\n\r\n");
				}

			} else {
				//							System.out.println(compound.getDsstoxCompoundId()+"\tDTXSID=null");
			}

			fw.write("$$$$\r\n");
		}

		fw.flush();
		fw.close();
	}


	void lookatRecordsWithInchiKey() {


		String sql="select smiles from compounds where indigo_inchi_key='HQAXQLUVPWIKNO-LPBINRMYSA-N'";


		ResultSet rs=SqlUtilities.runSQL2(conn, sql);

		try {
			while (rs.next()) {
				String smiles=rs.getString(1);
				System.out.println(smiles);				



			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}



	}


	void testGetCompounds() {
		DsstoxCompoundServiceImpl d=new DsstoxCompoundServiceImpl();
		System.out.println("here");

		List<DsstoxCompound>compounds=d.findAll(0, 1);
		DsstoxCompound c=compounds.get(0);
		System.out.println(c.getDsstoxCompoundId());

		//		System.out.println(d.findByDtxcid("DTXCID60820286").getIndigoInchikey());


	}


	void updateIndigoInchiValues(String filepathInchiUpdate) {
		
		try {
			
			BufferedReader br =new BufferedReader(new FileReader(filepathInchiUpdate));
			String header=br.readLine();
//			System.out.println(header);
			List<DsstoxCompound> compounds = new ArrayList<>();
			
			while (true) {

				String Line=br.readLine();
				if(Line==null) break;
				
				String [] values=Line.split("\t");
				
				DsstoxCompound compound=new DsstoxCompound();

				compound.setDsstoxCompoundId(values[0]);
				if (!values[2].equals("null"))	compound.setIndigoInchi(values[2]);
				if (!values[3].equals("null")) compound.setIndigoInchikey(values[3]);
				compound.setUpdatedBy(lanId);
				compounds.add(compound);
				
//				System.out.println(compound.getDsstoxCompoundId()+"\t"+compound.getIndigoInchikey()+"\t"+compound.getIndigoInchi());
				
//				System.out.println(cid+"\t"+mrv);
			}
			
			System.out.println("Done loading compounds from file");
			
//			System.out.println(Utilities.gson.toJson(compounds));
			updateIndigoInchiSQL(compounds);
			
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	
	public void updateIndigoInchiSQL(List<DsstoxCompound> compounds) {

		Connection conn = SqlUtilities.getConnectionDSSTOX();

		String SQL_UPDATE = "UPDATE compounds SET indigo_inchi=?, indigo_inchi_key=?, updated_by=?, updated_at=? WHERE dsstox_compound_id=?";

		int batchSize = 10000;

		try {
			conn.setAutoCommit(false);
			PreparedStatement prep = conn.prepareStatement(SQL_UPDATE);

			long t1 = System.currentTimeMillis();

			for (int counter = 0; counter < compounds.size(); counter++) {
//		 for (int counter = 0; counter < 2; counter++) {

				// System.out.println(counter);

				DsstoxCompound compound = compounds.get(counter);
								
//				System.out.println(compound.getDsstoxCompoundId());
				
				prep.setString(1, compound.getIndigoInchi());
				prep.setString(2, compound.getIndigoInchikey());
				prep.setString(3, compound.getUpdatedBy());
				prep.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
				prep.setString(5, compound.getDsstoxCompoundId());

				prep.addBatch();

				if (counter % batchSize == 0 && counter != 0) {
					System.out.println(counter);
					prep.executeBatch();
					conn.commit();
				}
			}
			
			int[] count = prep.executeBatch();// do what's left
			long t2 = System.currentTimeMillis();
			System.out.println("time to update " + compounds.size() + " indigo inchis using batchsize=" + batchSize + ":\t"
					+ (t2 - t1) / 1000.0 + " seconds");
			conn.commit();
			// conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void writeIsOrganicTextFile() {
		
		try {
			
			
			String filePathInput="data\\dsstox\\smi\\prod_dsstox_compounds.tsv";
			String filePathOutput="data\\dsstox\\smi\\prod_dsstox_compounds_is_organic.tsv";
			boolean writeAll=true;
			
//			String filePathInput="data\\dsstox\\smi\\prod_dsstox_compounds_EPISUITE.tsv";
//			String filePathOutput="data\\dsstox\\smi\\prod_dsstox_compounds_EPISUITE_not_organic.tsv";
//			boolean writeAll=false;
			
			BufferedReader br=new BufferedReader(new FileReader(filePathInput));
			FileWriter fw=new FileWriter(filePathOutput);
			
			DsstoxCompoundServiceImpl dsci=new DsstoxCompoundServiceImpl();
			
			
			
			fw.write("compound_id\tdsstox_compound_id\tsmiles\tformula\tisOrganic\r\n");
			
			br.readLine();
			while (true) {
				String line=br.readLine();
				if(line==null) break;
				
				String [] vals=line.split("\t");

				String dtxcid=vals[0];
				

				Boolean isOrganic=null;
				String smiles=null;
				String formula=null;

				if(vals.length==3) {

					formula=vals[1];
					smiles=vals[2];	

					try {
						AtomContainer molecule = (AtomContainer) DsstoxSession.smilesParser.parseSmiles(smiles.trim());
						isOrganic=false;
						
						for (int i=0;i<molecule.getAtomCount();i++) {
							Atom atom=(Atom)molecule.getAtom(i);

							if(atom.getSymbol().equals("C")) {
								isOrganic=true;
								break;
							}
						}
						
					} catch (Exception ex) {
						if(!formula.isBlank()) {
							try {
								IMolecularFormula mf=MolecularFormulaManipulator.getMolecularFormula(formula,DefaultChemObjectBuilder.getInstance());
								int countC=MolecularFormulaManipulator.getElementCount(mf, "C");
								isOrganic=countC>0;
//								System.out.println(dtxcid+"\t"+formula+"\t"+isOrganic);
							} catch (Exception ex2) {
//								System.out.println(dtxcid+"\t"+formula+"\tCant parse formula1");
							}	
						}
					}


//					if(isOrganic!=null && !isOrganic) System.out.println(smiles+"\tNot organic");

				} else if(vals.length==2) {
					formula=vals[1];
//					
					DsstoxRecord dr=dsci.findAsDsstoxRecordsByDtxcid(dtxcid).get(0);
					
//					System.out.println(dr.getno)
//					System.out.println(line);
					
					if(!formula.isBlank()) {
						try {
							IMolecularFormula mf=MolecularFormulaManipulator.getMolecularFormula(formula,DefaultChemObjectBuilder.getInstance());
							int countC=MolecularFormulaManipulator.getElementCount(mf, "C");
							isOrganic=countC>0;
//							System.out.println(dtxcid+"\t"+formula+"\t"+isOrganic);
						} catch (Exception ex2) {
//							System.out.println(dtxcid+"\t"+formula+"\tCant parse formula1");
						}	
					}
					
//					try {
//						
//						if(!formula.isBlank()) {
//							IMolecularFormula mf=MolecularFormulaManipulator.getMolecularFormula(formula,DefaultChemObjectBuilder.getInstance());
//							int countC=MolecularFormulaManipulator.getElementCount(mf, "C");
//							isOrganic=countC>0;
//							
//							if(isOrganic)
//								System.out.println(dtxcid+"\t"+formula);
//						}
//						
//					} catch (Exception ex) {
////						System.out.println(dtxcid+"\t"+formula+"\tCant parse formula2");
//					}
////					System.out.println(dtxcid+"\t"+formula+"\t"+isOrganic);

					
				} else {
					System.out.println(line);
				}
				
//				if(isOrganic!=null && !isOrganic)	 {
//					System.out.println(dtxcid+"\t"+smiles+"\t"+formula+"\t"+isOrganic+"\r\n");
//				}
				
				if(writeAll) {
					fw.write(dtxcid+"\t"+smiles+"\t"+formula+"\t"+isOrganic+"\r\n");
				} else if (isOrganic==null || !isOrganic) {
					fw.write(dtxcid+"\t"+smiles+"\t"+formula+"\t"+isOrganic+"\r\n");	
				}

				fw.flush();
				
			}
			
			
			br.close();
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	

	void writeIsOrganicTextFileFromJsonFiles() {
		
		try {
			MDLV3000Reader mr=new MDLV3000Reader();

			
			String filePathOutput="data\\dsstox\\smi\\prod_dsstox_compounds_is_organic.tsv";
			
			FileWriter fw=new FileWriter(filePathOutput);
			fw.write("dsstox_compound_id\tsmiles\tisOrganic\r\n");
			
			DsstoxCompoundServiceImpl dsci=new DsstoxCompoundServiceImpl();
			
			File folder=new File("data/dsstox/json");

			Type listOfMyClassObject = new TypeToken<List<DsstoxCompound>>() {}.getType();

			for (File file:folder.listFiles()) {

				if(!file.getName().contains("json") || file.getName().indexOf("prod_compounds")!=0) {
					continue;
				}
				
				System.out.println(file.getName());

				try {
					List<DsstoxCompound>compounds=Utilities.gson.fromJson(new FileReader(file), listOfMyClassObject);

					assignIsOrganicForCompounds(mr, compounds,fw);

				} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			

			fw.flush();
			fw.close();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}


	private void assignIsOrganicForCompounds(MDLV3000Reader mr, List<DsstoxCompound> compounds,FileWriter fw) throws Exception {
		for (DsstoxCompound compound:compounds) {
			
			if(compound.getDsstoxCompoundId()==null) continue;
			
			Boolean isOrganic=null;
			
			if(compound.getMolFile()!=null) {
				InputStream stream = new ByteArrayInputStream(compound.getMolFile().getBytes());
				mr.setReader(stream);

				IAtomContainer molecule=null;
				
				try {
					molecule = (IAtomContainer) mr.readMolecule(DefaultChemObjectBuilder.getInstance());
				} catch (Exception ex) {
					molecule=new AtomContainer();
				}
				
				isOrganic=isOrganic(molecule);
				
//					System.out.println(compound.getDsstoxCompoundId()+"\t"+compound.getSmiles()+"\t"+isOrganic);
				
			} else {
				System.out.println(compound.getDsstoxCompoundId()+"\tmissing mol file");
			}
			
			fw.write(compound.getDsstoxCompoundId()+"\t"+compound.getSmiles()+"\t"+isOrganic+"\r\n");
		}
	}

	
	boolean isOrganic(IAtomContainer molecule) {
		
		boolean isOrganic=false;
		
		for (int i=0;i<molecule.getAtomCount();i++) {
			Atom atom=(Atom)molecule.getAtom(i);

			if(atom.getSymbol().equals("C")) {
				isOrganic=true;
				break;
			}
		}
		return isOrganic;
	}
	
	void writeNotOrganicTextFile() {
		
		try {
			
			
			
			String filePathInput="data\\dsstox\\smi\\prod_dsstox_compounds_EPISUITE.tsv";
			String filePathOutput="data\\dsstox\\smi\\prod_dsstox_compounds_EPISUITE_not_organic.tsv";

			
			BufferedReader br=new BufferedReader(new FileReader(filePathInput));
			FileWriter fw=new FileWriter(filePathOutput);
			
			
			
			
			fw.write("compound_id\tdsstox_compound_id\tsmiles\tformula\tisOrganic\r\n");
			
			br.readLine();
			while (true) {
				String line=br.readLine();
				if(line==null) break;
				
				String [] vals=line.split("\t");

				String compound_id=vals[0];
				String dtxcid=vals[1];

				Boolean isOrganic=null;
				String smiles=null;
				String formula=null;

				if(vals.length==4) {

					formula=vals[2];
					smiles=vals[3];	

					try {
						AtomContainer molecule = (AtomContainer) DsstoxSession.smilesParser.parseSmiles(smiles.trim());
						isOrganic=false;
						
						for (int i=0;i<molecule.getAtomCount();i++) {
							Atom atom=(Atom)molecule.getAtom(i);

							if(atom.getSymbol().equals("C")) {
								isOrganic=true;
								break;
							}
						}
						
					} catch (Exception ex) {
						if(!formula.isBlank()) {
							try {
								IMolecularFormula mf=MolecularFormulaManipulator.getMolecularFormula(formula,DefaultChemObjectBuilder.getInstance());
								int countC=MolecularFormulaManipulator.getElementCount(mf, "C");
								isOrganic=countC>0;
//								System.out.println(dtxcid+"\t"+formula+"\t"+isOrganic);
							} catch (Exception ex2) {
//								System.out.println(dtxcid+"\t"+formula+"\tCant parse formula1");
							}	
						}
					}


//					if(isOrganic!=null && !isOrganic) System.out.println(smiles+"\tNot organic");

				} else if(vals.length==3) {
					formula=vals[2];
//					
					System.out.println(line);
					
//					try {
//						
//						if(!formula.isBlank()) {
//							IMolecularFormula mf=MolecularFormulaManipulator.getMolecularFormula(formula,DefaultChemObjectBuilder.getInstance());
//							int countC=MolecularFormulaManipulator.getElementCount(mf, "C");
//							isOrganic=countC>0;
//							
//							if(isOrganic)
//								System.out.println(dtxcid+"\t"+formula);
//						}
//						
//					} catch (Exception ex) {
////						System.out.println(dtxcid+"\t"+formula+"\tCant parse formula2");
//					}
////					System.out.println(dtxcid+"\t"+formula+"\t"+isOrganic);

					
				} else {
					System.out.println(line);
				}
				
//				if(isOrganic!=null && !isOrganic)	 {
//					System.out.println(dtxcid+"\t"+smiles+"\t"+formula+"\t"+isOrganic+"\r\n");
//				}
				
				if (isOrganic==null || !isOrganic) {
					fw.write(compound_id+"\t"+dtxcid+"\t"+smiles+"\t"+formula+"\t"+isOrganic+"\r\n");	
				}

				fw.flush();
				
			}
			
			
			br.close();
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	
	
	public static void main(String[] args) {
		DSSTOX_Compounds_Script d=new DSSTOX_Compounds_Script();
//		d.compoundsToJsonFiles();
//		d.convertJsonsToSDFs();
		
//		d.writeNotOrganicTextFile();
//		d.writeIsOrganicTextFile();
		d.writeIsOrganicTextFileFromJsonFiles();
		

		//			d.compoundsToTSV_File();
		//		d.lookatRecordsWithInchiKey();
		//		d.testGetCompounds();
//		d.backupCompoundsInchis();
//		d.backupCompoundsInchis2();

//		d.generateNewIndigoValuesToTSV();
//		d.updateIndigoInchiValues("data/dsstox/tsv/snapshot_compounds_indigo_inchi_key.tsv");

//		System.out.println("hi");

	}

}





