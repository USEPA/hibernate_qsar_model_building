package gov.epa.run_from_java.scripts.DSSTOX_Loading;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxSnapshot;
import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.DsstoxSession;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.GenericSubstance;
import gov.epa.databases.dsstox.entity.GenericSubstanceCompound;
import gov.epa.databases.dsstox.service.DsstoxCompoundServiceImpl;
import gov.epa.run_from_java.scripts.DsstoxSnapshotCreatorScriptDSSTOX;
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
	List<DsstoxCompound> getCompoundsBySQL(String sqlUpdatedAt,String sqlRelationship, boolean use3d, int offset,int limit) {

		List<DsstoxCompound>compounds=new ArrayList<>();
		
		String sql="SELECT dsstox_compound_id,";
		
		if (use3d) sql+="mol_file_3d,";	
		else sql+="mol_file,";
		
//		DATE_FORMAT(gs.updated_at,'%Y-%m-%d %h:i%:%s')
		
		sql+="smiles, jchem_inchi_key,indigo_inchi_key,mol_weight,"
				+ "gs.dsstox_substance_id, gs.casrn, gs.preferred_name,  gs.updated_at\n";  
		sql+="FROM compounds c\n";
		sql+="left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id\n";
		sql+="left join generic_substances gs on gs.id=gsc.fk_generic_substance_id\n"; 
		//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null) and gs.dsstox_substance_id is not null\n";
//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null)\n";
		
		sql+="where\n";
		
		sql+=sqlUpdatedAt+" and "+sqlRelationship+"\n";
		
		sql+="ORDER BY dsstox_compound_id\n";
		sql+="LIMIT "+limit+" OFFSET "+offset;

		System.out.println(sql);

		ResultSet rs=SqlUtilities.runSQL2(conn, sql);

		
//		Calendar cal = Calendar.getInstance(); 
		
		try {
			while (rs.next()) {
				DsstoxCompound compound=new DsstoxCompound();				

				compound.setDsstoxCompoundId(rs.getString(1));

				if (use3d) {
					if (rs.getString(2)!=null) compound.setMolFile3d(rs.getString(2));
				} else {
					if (rs.getString(2)!=null) compound.setMolFile(rs.getString(2));
				}
				

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
					
					if (rs.getString(9)!=null) {
						gs.setPreferredName(rs.getString(9));
					}

					
					if (rs.getTimestamp(10)!=null) {
						java.util.Date utilDate =new java.util.Date(rs.getTimestamp(10).getTime());
						gs.setUpdatedAt(utilDate);
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
	
	
	private List<DsstoxCompound> getCompoundsBySubstanceSQL(int offset, int limit) {

		List<DsstoxCompound>compounds=new ArrayList<>();
		
		String sql="SELECT dsstox_compound_id,mol_file,smiles, jchem_inchi_key,indigo_inchi_key,mol_weight,"
				+ "gs.dsstox_substance_id, gs.casrn, gs.preferred_name, gs.updated_at,"
				+ "CASE WHEN mol_image_png IS NULL THEN FALSE ELSE TRUE END\n";  
		
//		String sql="SELECT dsstox_compound_id,mol_file,smiles, jchem_inchi_key,indigo_inchi_key,mol_weight,"
//				+ "gs.dsstox_substance_id, gs.casrn, gs.preferred_name\n";  
		
		sql+="FROM generic_substances gs\n";
		sql+="left join generic_substance_compounds gsc on gs.id=gsc.fk_generic_substance_id\n";
		sql+="left join compounds c on gsc.fk_compound_id=c.id\n";
		
		//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null) and gs.dsstox_substance_id is not null\n";
//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null)\n";
//		sql+="where dsstox_compound_id is not null\n";
		
		
		sql+="ORDER BY dsstox_substance_id\n";
		sql+="LIMIT "+limit+" OFFSET "+offset;

//		System.out.println(sql);

		ResultSet rs=SqlUtilities.runSQL2(conn, sql);
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Define the format of your date string

	     
		try {
			while (rs.next()) {
				DsstoxCompound compound=new DsstoxCompound();				

				compound.setDsstoxCompoundId(rs.getString(1));

				if (rs.getString(2)!=null)
					compound.setMolFile(rs.getString(2));

				if (rs.getString(3)!=null) {
					compound.setSmiles(rs.getString(3));
				}
					

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
					
					if (rs.getString(9)!=null) {
						gs.setPreferredName(rs.getString(9));
					}
					
					if (rs.getString(10) != null) {
						try {
							java.util.Date utilDate = dateFormat.parse(rs.getString(10)); // Parse the string into a
							Date sqlDate = new Date(utilDate.getTime());
							gs.setUpdatedAt(sqlDate);
						} catch (Exception ex) {

						}
					}


				} else {
//					continue;
				}
				
				compound.setMolImagePNGAvailable(rs.getBoolean(11));//do we want to store image in db?

//				System.out.println(compound.getDsstoxCompoundId()+"\t"+compound.isMolImagePNGAvailable());

				compounds.add(compound);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		System.out.println(compounds.size());
		return compounds;	
	}
	
	
	private static String generateIdListString(List<String> idList) {
        StringBuilder idStringBuilder = new StringBuilder();
        for (int i = 0; i < idList.size(); i++) {
            idStringBuilder.append("'"+idList.get(i)+"'");
            if (i < idList.size() - 1) {
                idStringBuilder.append(",");
            }
        }
        return idStringBuilder.toString();
    }
	
	public List<DsstoxCompound> getCompoundsBySubstanceSQL(List<String>dtxcidsAll) {
		
		List<DsstoxCompound> compoundsAll=new ArrayList<>();
		
		int batchSize=1000;
		List<String>dtxcids2=new ArrayList<>();
		
		for (int i=0;i<dtxcidsAll.size();i++) {
			dtxcids2.add(dtxcidsAll.get(i));
			if(dtxcids2.size()==batchSize) {
				List<DsstoxCompound> compounds = getCompounds(dtxcids2);
				compoundsAll.addAll(compounds);
				dtxcids2.clear();
				System.out.println(compoundsAll.size());
			}
		}

		List<DsstoxCompound> compounds = getCompounds(dtxcids2);
		compoundsAll.addAll(compounds);
		System.out.println(compoundsAll.size());
		//		System.out.println(compounds.size());
		return compoundsAll;	
	}


	private List<DsstoxCompound> getCompounds(List<String> dtxcids) {
		List<DsstoxCompound>compounds=new ArrayList<>();
		
		String sql="SELECT dsstox_compound_id,mol_file,smiles, jchem_inchi_key,indigo_inchi_key,mol_weight,"
				+ "gs.dsstox_substance_id, gs.casrn, gs.preferred_name, gs.updated_at,"
				+ "mol_image_png IS NOT NULL\n";  
		sql+="FROM generic_substances gs\n";
		sql+="join generic_substance_compounds gsc on gs.id=gsc.fk_generic_substance_id\n";
		sql+="join compounds c on gsc.fk_compound_id=c.id\n";
		sql+="WHERE dsstox_compound_id IN (" + generateIdListString(dtxcids) + ");";
		
		
//		System.out.println(sql);
		
		ResultSet rs=SqlUtilities.runSQL2(conn, sql);
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Define the format of your date string
	     
		try {
			while (rs.next()) {

				DsstoxCompound compound=new DsstoxCompound();				
				compound.setDsstoxCompoundId(rs.getString(1));

				if (rs.getString(2)!=null)
					compound.setMolFile(rs.getString(2));

				if (rs.getString(3)!=null) {
					compound.setSmiles(rs.getString(3));
				}
					

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
					
					if (rs.getString(9)!=null) {
						gs.setPreferredName(rs.getString(9));
					}
					
					if (rs.getString(10) != null) {
						try {
							java.util.Date utilDate = dateFormat.parse(rs.getString(10)); // Parse the string into a
							Date sqlDate = new Date(utilDate.getTime());
							gs.setUpdatedAt(sqlDate);
						} catch (Exception ex) {

						}
					}


				} else {
//					continue;
				}
				
				compound.setMolImagePNGAvailable(rs.getBoolean(11));//do we want to store image in db?

//				System.out.println(compound.getDsstoxCompoundId()+"\t"+compound.isMolImagePNGAvailable());

				compounds.add(compound);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return compounds;
	}
	
	
	List<DsstoxCompound> getCompoundsBySQL(int offset,int limit) {

		List<DsstoxCompound>compounds=new ArrayList<>();
		
		String sql="SELECT dsstox_compound_id,mol_file,smiles, jchem_inchi_key,indigo_inchi_key,mol_weight,"
				+ "gs.dsstox_substance_id, gs.casrn, gs.preferred_name, gs.updated_at,"
				+ "CASE WHEN mol_image_png IS NULL THEN FALSE ELSE TRUE END\n";  
		
//		String sql="SELECT dsstox_compound_id,mol_file,smiles, jchem_inchi_key,indigo_inchi_key,mol_weight,"
//				+ "gs.dsstox_substance_id, gs.casrn, gs.preferred_name\n";  
		
		
		sql+="FROM compounds c\n";
		sql+="left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id\n";
		sql+="left join generic_substances gs on gs.id=gsc.fk_generic_substance_id\n"; 
		//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null) and gs.dsstox_substance_id is not null\n";
//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null)\n";
		
		sql+="where dsstox_compound_id is not null\n";
		
		
		sql+="ORDER BY dsstox_compound_id\n";
		sql+="LIMIT "+limit+" OFFSET "+offset;

//		System.out.println(sql);

		ResultSet rs=SqlUtilities.runSQL2(conn, sql);
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Define the format of your date string

	     
		try {
			while (rs.next()) {
				DsstoxCompound compound=new DsstoxCompound();				

				compound.setDsstoxCompoundId(rs.getString(1));

				if (rs.getString(2)!=null)
					compound.setMolFile(rs.getString(2));

				if (rs.getString(3)!=null) {
					compound.setSmiles(rs.getString(3));
				}
					

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
					
					if (rs.getString(9)!=null) {
						gs.setPreferredName(rs.getString(9));
					}
					
					if (rs.getString(10) != null) {
						try {
							java.util.Date utilDate = dateFormat.parse(rs.getString(10)); // Parse the string into a
							Date sqlDate = new Date(utilDate.getTime());
							gs.setUpdatedAt(sqlDate);
						} catch (Exception ex) {

						}
					}


				} else {
//					continue;
				}
				
				compound.setMolImagePNGAvailable(rs.getBoolean(11));//do we want to store image in db?

//				System.out.println(compound.getDsstoxCompoundId()+"\t"+compound.isMolImagePNGAvailable());

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

		boolean skipMarkush=false;
		int batchSize=25000;
		int i=0;
		
		
//		String date="2025-07-30";
		String date="2025-10-30";
		
		String folder="data\\dsstox\\snapshot-"+date+"\\json\\";
		
		new File(folder).mkdirs();
		
		Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd").disableHtmlEscaping().serializeSpecialFloatingPointValues().create();

		
		while(true) {

//			File file=new File("data/dsstox/json/snapshot_compounds"+(i+1)+".json");
			File file=new File(folder+"prod_compounds"+(i+1)+".json");

			
			if (file.exists()) {
				System.out.println(file.getName()+"\texists");
				i++;
				continue;
			}

//			List<DsstoxCompound>compounds=getCompoundsBySQL(i*batchSize, batchSize);
			List<DsstoxCompound>compounds=getCompoundsBySubstanceSQL(i*batchSize, batchSize);
			
			if(compounds.size()==0) {
				break;
			} else {

				try {
					FileWriter fw=new FileWriter(file);
					fw.write(gson.toJson(compounds));
					
					System.out.println((i+1)+"\t"+compounds.size());

					fw.flush();
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				i++;
			}
			
			System.gc();
			
		}

	}
	
	void substancesToJsonFiles() {

		//		DsstoxCompoundServiceImpl compoundService=new DsstoxCompoundServiceImpl();
		//		List<DsstoxCompound>compounds=compoundService.findAll();

		boolean skipMarkush=false;
		int batchSize=50000;
		int i=0;
		
		
//		String date="2025-07-30";
		String date="2025-10-30";
		
		String folder="data\\dsstox\\snapshot-"+date+"\\json\\";
		
		new File(folder).mkdirs();
		
		Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd").disableHtmlEscaping().serializeSpecialFloatingPointValues().create();

		
		while(true) {

//			File file=new File("data/dsstox/json/snapshot_compounds"+(i+1)+".json");
			File file=new File(folder+"prod_compounds"+(i+1)+".json");

			
			if (file.exists()) {
				System.out.println(file.getName()+"\texists");
				i++;
				continue;
			}

			List<DsstoxCompound>compounds=getCompoundsBySubstanceSQL(i*batchSize, batchSize);
			
			if(compounds.size()==0) {
				break;
			} else {

				try {
					FileWriter fw=new FileWriter(file);
					fw.write(gson.toJson(compounds));
					
					System.out.println((i+1)+"\t"+compounds.size());

					fw.flush();
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				i++;
			}
			
			System.gc();
			
		}

	}
	
	

	void compoundsToJsonFiles2() {

		//		DsstoxCompoundServiceImpl compoundService=new DsstoxCompoundServiceImpl();
		//		List<DsstoxCompound>compounds=compoundService.findAll();

		int batchSize=50000;
		int i=0;

		String date="2024-11-12";
		String sqlUpdatedAt="gs.updated_at < '"+date+"'";
		
		String sqlRelationship="gsc.relationship = \"Tested Chemical\"";
		boolean use3d=false;
		
		String folder="data/dsstox/snapshot-"+date+"/json/";
		if(use3d) folder="data/dsstox/snapshot-"+date+"/json3d/";
		
		new File(folder).mkdirs();

		
		int totalChemicals=0;
		
		while(true) {

			File file=new File(folder+"prod_compounds_updated_lt_"+date+"_"+(i+1)+".json");

			if (file.exists()) {
				i++;
				continue;
			}

			List<DsstoxCompound>compounds=getCompoundsBySQL(sqlUpdatedAt,sqlRelationship,use3d,i*batchSize, batchSize);

			if(compounds.size()==0) {
				break;
			} else {

				try {
					FileWriter fw=new FileWriter(file);
					fw.write(Utilities.gson.toJson(compounds));
					
					totalChemicals+=compounds.size();

					System.out.println((i+1)+"\t"+compounds.size()+"\t"+totalChemicals);

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
	
	void compoundsToJsonFilesMarkush() {

		//		DsstoxCompoundServiceImpl compoundService=new DsstoxCompoundServiceImpl();
		//		List<DsstoxCompound>compounds=compoundService.findAll();

		int batchSize=50000;
		int i=0;

		String date="2024-11-12";
		String sqlUpdatedAt="gs.updated_at < '"+date+"'";
		
		String sqlRelationship="gsc.relationship != \"Tested Chemical\"";
		boolean use3d=false;
		
		String folder="data/dsstox/snapshot-"+date+"/json/";
		if(use3d) folder="data/dsstox/snapshot-"+date+"/json3d/";
		
		new File(folder).mkdirs();

		
		int totalChemicals=0;
		
		while(true) {

			File file=new File(folder+"prod_compounds_updated_lt_"+date+"_"+(i+1)+"_markush.json");

			if (file.exists()) {
				i++;
				continue;
			}

			List<DsstoxCompound>compounds=getCompoundsBySQL(sqlUpdatedAt,sqlRelationship,use3d,i*batchSize, batchSize);

			if(compounds.size()==0) {
				break;
			} else {

				try {
					FileWriter fw=new FileWriter(file);
					fw.write(Utilities.gson.toJson(compounds));
					
					totalChemicals+=compounds.size();

					System.out.println((i+1)+"\t"+compounds.size()+"\t"+totalChemicals);

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
	
	
	void recentUpdateToJsonFiles() {

		//		DsstoxCompoundServiceImpl compoundService=new DsstoxCompoundServiceImpl();
		//		List<DsstoxCompound>compounds=compoundService.findAll();

		int batchSize=50000;
		
		String date="2024-11-12";
		String sqlUpdatedAt="gs.updated_at >= '"+date+"'";
		
		String sqlRelationship="gsc.relationship = \"Tested Chemical\"";
		boolean use3d=false;
		
		
		String folder="data/dsstox/snapshot-"+date+"/json/";
		if(use3d) folder="data/dsstox/snapshot-"+date+"/json3d/";
		
		new File(folder).mkdirs();
		
		
		File file=new File(folder+"prod_compounds_updated_gte_2024-11-12.json");

		List<DsstoxCompound>compounds=getCompoundsBySQL(sqlUpdatedAt,sqlRelationship,use3d,0, batchSize);
		System.out.println(compounds.size());
		
		try {
			FileWriter fw=new FileWriter(file);
			fw.write(Utilities.gson.toJson(compounds));
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	void recentUpdateToJsonFilesMarkush() {

		//		DsstoxCompoundServiceImpl compoundService=new DsstoxCompoundServiceImpl();
		//		List<DsstoxCompound>compounds=compoundService.findAll();

		int batchSize=50000;
		
		String date="2024-11-12";
		String sqlUpdatedAt="gs.updated_at >= '"+date+"'";
		
		String sqlRelationship="gsc.relationship != \"Tested Chemical\"";
		boolean use3d=false;
		
		
		String folder="data/dsstox/snapshot-"+date+"/json/";
		if(use3d) folder="data/dsstox/snapshot-"+date+"/json3d/";
		
		new File(folder).mkdirs();
		
		
		File file=new File(folder+"prod_compounds_updated_gte_2024-11-12_markush.json");

		List<DsstoxCompound>compounds=getCompoundsBySQL(sqlUpdatedAt,sqlRelationship,use3d,0, batchSize);
		System.out.println(compounds.size());
		
		try {
			FileWriter fw=new FileWriter(file);
			fw.write(Utilities.gson.toJson(compounds));
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	void updatedSincePreviousSnapshot() {

		//		DsstoxCompoundServiceImpl compoundService=new DsstoxCompoundServiceImpl();
		//		List<DsstoxCompound>compounds=compoundService.findAll();

		int batchSize=50000;
		
		String dateSnapshotLatest="2024-11-12";
		String dateSnapshotPrevious="2023-04-04";
		
		String sqlUpdatedAt="gs.updated_at >= '"+dateSnapshotPrevious+"' and gs.updated_at < '"+dateSnapshotLatest+"'";
		
		String sqlRelationship="gsc.relationship = \"Tested Chemical\"";
		boolean use3d=false;
		
		
		String folder="data/dsstox/snapshot-"+dateSnapshotLatest+"/json/";
		if(use3d) folder="data/dsstox/snapshot-"+dateSnapshotLatest+"/json3d/";
		
		new File(folder).mkdirs();
		
		
		File file=new File(folder+"prod_compounds_updated_gte_"+dateSnapshotPrevious+"_and_lt_"+dateSnapshotLatest+".json");

		List<DsstoxCompound>compounds=getCompoundsBySQL(sqlUpdatedAt,sqlRelationship,use3d,0, batchSize);
		System.out.println(compounds.size());
		
		try {
			FileWriter fw=new FileWriter(file);
			fw.write(Utilities.gson.toJson(compounds));
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

		
		boolean split_out_salts=false;
		boolean skipMissingSID=true;

		
		String destFolder="data/dsstox/sdf/";
		if(split_out_salts)	destFolder="data/dsstox/sdf_split_out_salts/";

		
//		boolean split_out_salts=false;
		
		
		File folderDest=new File(destFolder);
		folderDest.mkdirs();
		
		
		try {

			File folder=new File("data/dsstox/json");

			Type listOfMyClassObject = new TypeToken<List<DsstoxCompound>>() {}.getType();

			List<DsstoxCompound>saltCompoundsAll=new ArrayList<>();
			
			for (File file:folder.listFiles()) {

				if(!file.getName().contains("json")) {
					continue;
				}
				
				if(!file.getName().contains("snapshot_compounds")) {
					continue;
				}

				if(file.getName().contains("snapshot_compounds_for_toxprints")) {
					continue;
				}
				

				try {
					List<DsstoxCompound>compounds=Utilities.gson.fromJson(new FileReader(file), listOfMyClassObject);

					String filename=file.getName().replace(".json", ".sdf");
					String filepath=destFolder+filename;
					
					System.out.print(filename+"\t"+compounds.size());
					
					if (split_out_salts) {
						List<DsstoxCompound>saltCompounds=removeSalts(compounds);
						saltCompoundsAll.addAll(saltCompounds);
						System.out.print(saltCompounds.size()+"\n");
//						String filepathSalts=filepath.replace("snapshot_compounds", "salt_snapshot_compounds");
//						writeCompoundsToSDF(saltCompounds, filepathSalts, skipMissingSID);
					} else {
						System.out.print("\n");
					}
					

					writeCompoundsToSDF(compounds, filepath, skipMissingSID);

//					if(true) break;
						
				} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
					e.printStackTrace();
				}
			}//end loop over files
			
			writeCompoundsToSDF(saltCompoundsAll, destFolder+"salt_snapshot_compounds.sdf", skipMissingSID);


		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	

	void convertJsonsToSDFs2() {

		
		boolean split_out_salts=false;
		boolean skipMissingSID=true;

		String date="2024-11-12";
		String snapshotFolder="data/dsstox/snapshot-"+date+"/";
		
		
		String jsonFolder=snapshotFolder+"json/";

		String destFolder=snapshotFolder+"sdf/";
		if(split_out_salts)	destFolder=snapshotFolder+"sdf_split_out_salts/";
		
//		boolean split_out_salts=false;
		
		
		File folderDest=new File(destFolder);
		folderDest.mkdirs();
		
		
		try {

			File folder=new File(jsonFolder);

			Type listOfMyClassObject = new TypeToken<List<DsstoxCompound>>() {}.getType();

			List<DsstoxCompound>saltCompoundsAll=new ArrayList<>();
			
			for (File file:folder.listFiles()) {

				if(!file.getName().contains("json")) {
					continue;
				}
				
//				if(!file.getName().contains("snapshot_compounds")) {
//					continue;
//				}
//
//				if(file.getName().contains("snapshot_compounds_for_toxprints")) {
//					continue;
//				}
				

				try {
					List<DsstoxCompound>compounds=Utilities.gson.fromJson(new FileReader(file), listOfMyClassObject);

					String filename=file.getName().replace(".json", ".sdf");
					String filepath=destFolder+filename;
					
					System.out.print(filename+"\t"+compounds.size());
					
					if (split_out_salts) {
						List<DsstoxCompound>saltCompounds=removeSalts(compounds);
						saltCompoundsAll.addAll(saltCompounds);
						System.out.print(saltCompounds.size()+"\n");
//						String filepathSalts=filepath.replace("snapshot_compounds", "salt_snapshot_compounds");
//						writeCompoundsToSDF(saltCompounds, filepathSalts, skipMissingSID);
					} else {
						System.out.print("\n");
					}
					

					writeCompoundsToSDF(compounds, filepath, skipMissingSID);

//					if(true) break;
						
				} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
					e.printStackTrace();
				}
			}//end loop over files
			
			
			if(split_out_salts)
				writeCompoundsToSDF(saltCompoundsAll, destFolder+"salt_snapshot_compounds.sdf", skipMissingSID);


		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}


	public static void createSDF(List<DsstoxCompound> compounds, String filepath,boolean split_out_salts,boolean skipMissingSID) throws IOException {
		
		
		System.out.println("Compounds in file:"+compounds.size()); 
		
		if (split_out_salts) {
			List<DsstoxCompound>saltCompounds=removeSalts(compounds);
			String filepathSalts=filepath.replace("snapshot_compounds", "salt_snapshot_compounds");
			writeCompoundsToSDF(saltCompounds, filepathSalts, skipMissingSID);
		}
		writeCompoundsToSDF(compounds, filepath, skipMissingSID);
		
	}


	private static List<DsstoxCompound> removeSalts(List<DsstoxCompound> compounds) {
		
		MDLV3000Reader mr=new MDLV3000Reader();
		List<DsstoxCompound> saltCompounds=new ArrayList<>();
		
		
		for(int i=0;i<compounds.size();i++)  {

			DsstoxCompound compound=compounds.get(i);

			if(compound.getSmiles()!=null) {
				if(compound.getSmiles().contains(".")) {
					compounds.remove(i--);
					saltCompounds.add(compound);
					continue;
				} else {
					continue;
				}
				//				System.out.println(compound.getSmiles());
			}

			IAtomContainer molecule=null;
			if(compound.getMolFile()!=null) {
//				System.out.println(compound.getDsstoxCompoundId()+"\tno smiles");
				molecule=getMoleculeFromMolFileString(mr, compound);					
			} 
			
			if(molecule!=null && StructureUtil.isSalt(molecule)) {
				compounds.remove(i--);
				saltCompounds.add(compound);
				continue;
			}
			

		}
//		System.out.println("Salts="+saltCompounds.size());
		
		return saltCompounds;
		
		
	}


	private static void writeCompoundsToSDF(List<DsstoxCompound> compounds, String filepath, boolean skipMissingSID)
			throws IOException {
		FileWriter fw=new FileWriter(filepath);

		for (DsstoxCompound compound:compounds) {

			if (compound.getGenericSubstanceCompound()==null && skipMissingSID) {
				continue;
			}
			
			
			if(compound.getMolFile()==null) {//only seems to happen when dont have dtxsid
				System.out.println(compound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId()+" missing mol file,smiles="+compound.getSmiles());
				continue;
			}
			
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
				
				if(compound.getGenericSubstanceCompound().getGenericSubstance().getPreferredName()!=null) {
					fw.write(">  <PREFERRED_NAME>\n");
					fw.write(compound.getGenericSubstanceCompound().getGenericSubstance().getPreferredName()+"\r\n\r\n");
				}


			} else {
				//							System.out.println(compound.getDsstoxCompoundId()+"\tDTXSID=null");
			}

			fw.write("$$$$\r\n");
		}

		fw.flush();
		fw.close();
	}


	private static IAtomContainer getMoleculeFromMolFileString(MDLV3000Reader mr, DsstoxCompound compound) {
		IAtomContainer molecule=null;
		try {
			InputStream stream = new ByteArrayInputStream(compound.getMolFile().getBytes());
			mr.setReader(stream);
			molecule = (IAtomContainer) mr.readMolecule(DefaultChemObjectBuilder.getInstance());

		} catch (Exception ex) {
//			ex.printStackTrace();
		}
		
		return molecule;
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
	
	
	void createDsstoxRecordsUsingCompoundsRecordsNoDTXCID() {
		//make sure environment is use to use prod_dsstox
		
		DsstoxSnapshotCreatorScriptDSSTOX d=new DsstoxSnapshotCreatorScriptDSSTOX();
		
		String name="DSSTOX Snapshot 11/12/2024";
		String date="2024-11-12";
		DsstoxSnapshot snapshot=d.getSnapshot(name);
		
		System.out.println(snapshot.getId());
		
//		String sqlUpdatedAt="gs.updated_at < '"+date+"'";
		String sqlUpdatedAt="gs.updated_at < '"+"2025-02-13"+"'";
		
		List<gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord>records=d.getDsstoxRecordsNoCompound(snapshot,lanId,sqlUpdatedAt);
		System.out.println(records.size());

//		System.out.println(Utilities.gson.toJson(records));
		
		
		try {
			d.dsstoxRecordService.createBatchSQLNoCompound(records);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		
//		sqlUpdatedAt="gs.updated_at >= '"+date+"'";
//		records=d.getDsstoxRecordsNoCompound(snapshot,lanId,sqlUpdatedAt);
//		System.out.println(records.size());
//		
//		for (gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord record:records) {
//			System.out.println(record.getDtxsid());	
//		}
//		System.out.println(records.size());
		
//		System.out.println(Utilities.gson.toJson(records));

		
	}

	
	void filterJsonFiles() {
		
		 Logger.getLogger("org.openscience.cdk").setLevel(Level.OFF);
	     Logger.getLogger("org.openscience.cdk.io").setLevel(Level.OFF);

		
		String folderSrc="data\\dsstox\\snapshot-2025-07-30\\json\\";
		String folderDest="data\\dsstox\\snapshot-2025-07-30\\json filter\\";
		
		Type listOfMyClassObject = new TypeToken<List<DsstoxCompound>>() {}.getType();

		
		List<DsstoxCompound>compoundsAll=new ArrayList<>();
		
		
		for (File file:new File(folderSrc).listFiles()) {

			
			if(!file.getName().contains("json")) {
				continue;
			}
			
			try {
				List<DsstoxCompound>compounds=Utilities.gson.fromJson(new FileReader(file), listOfMyClassObject);
				
				int countRemoved=0;
				for(int i=0;i<compounds.size();i++)  {
					DsstoxCompound compound=compounds.get(i);
					if(compound.getGenericSubstanceCompound()==null) {
						compounds.remove(i--);
						countRemoved++;
					}
				}

				List<DsstoxCompound>salts=removeSalts(compounds);
				
				countRemoved+=salts.size();

				for(int i=0;i<compounds.size();i++)  {
					DsstoxCompound compound=compounds.get(i);
					if(compound.getSmiles()!=null) {
						if(compound.getSmiles().contains("|") && compound.getSmiles().contains("*") ) {
							compounds.remove(i--);
							countRemoved++;
						}
					}
				}
				
				System.out.println(file.getName()+"\t"+compounds.size()+"\t"+countRemoved);
				
				compoundsAll.addAll(compounds);

//				if(true) break;
					
			} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("All\t"+compoundsAll.size()+"\n");
		
		
		List<DsstoxCompound>compoundsChunk=new ArrayList<>();
		
		int num=1;
		
		for (DsstoxCompound dc:compoundsAll) {
			
			compoundsChunk.add(dc);
			
			if(compoundsChunk.size()==50000) {
				String filename="prod_compounds"+num+".json";
				System.out.println(filename+"\t"+compoundsChunk.size());
				ToxPredictor.Utilities.Utilities.toJsonFile(compoundsChunk, folderDest+filename);
				compoundsChunk.clear();
				num++;
			}
		}
	}
	
	
	
	public static void main(String[] args) {
		DSSTOX_Compounds_Script d=new DSSTOX_Compounds_Script();

		d.compoundsToJsonFiles();
//		d.filterJsonFiles();
		
		
//		d.convertJsonsToSDFs();
		
//		d.recentUpdateToJsonFiles();
//		d.recentUpdateToJsonFilesMarkush();
//		d.updatedSincePreviousSnapshot();
//		d.compoundsToJsonFiles2();
//		d.compoundsToJsonFilesMarkush();
//		d.convertJsonsToSDFs2();
//		d.createDsstoxRecordsUsingCompoundsRecordsNoDTXCID();
		
		
		/**
		 * Alternatively I could have taken the records from April 23 snapshot in the postgresl and updated it with the records from prod_dsstox after the date of the first snapshot
		 */

		
		
//		d.writeNotOrganicTextFile();
//		d.writeIsOrganicTextFile();
//		d.writeIsOrganicTextFileFromJsonFiles();
		

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





