package gov.epa.run_from_java.scripts.DSSTOX_Loading;

import java.lang.reflect.Type;
import java.io.BufferedReader;
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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.json.CDL;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.interfaces.IAtomContainer;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Value;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.GenericSubstance;
import gov.epa.databases.dsstox.entity.GenericSubstanceCompound;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.RunDashboardPredictions;
import gov.epa.util.StructureUtil;
import gov.epa.util.StructureUtil.Inchi;

/**
 * @author TMARTI02
 */
public class DSSTOX_Compounds_Script {



	Connection conn=SqlUtilities.getConnectionDSSTOX();

	List<DsstoxCompound> getCompoundsBySQL2(int offset,int limit) {

		List<DsstoxCompound>compounds=new ArrayList<>();

		String sql="SELECT dsstox_compound_id,mol_file,smiles, inchi, jchem_inchi_key,indigo_inchi_key,mol_weight,gs.dsstox_substance_id, gs.casrn\n";  
		sql+="FROM compounds c\n";
		sql+="left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id\n";
		sql+="left join generic_substances gs on gs.id=gsc.fk_generic_substance_id\n"; 
		//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null) and gs.dsstox_substance_id is not null\n";
		sql+="where (mol_weight is not null and mol_weight !=0) \n";
		sql+="ORDER BY dsstox_compound_id\n";
		sql+="LIMIT "+limit+" OFFSET "+offset;

//		System.out.println(sql);

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
					compound.setInchi(rs.getString(4));

				if (rs.getString(5)!=null)
					compound.setJchemInchikey(rs.getString(5));

				if (rs.getString(6)!=null)
					compound.setIndigoInchikey(rs.getString(6));				

				if (rs.getString(7)!=null)
					compound.setMolWeight(Double.parseDouble(rs.getString(7)));

				if (rs.getString(8)!=null) {
					GenericSubstanceCompound gsc=new GenericSubstanceCompound();
					GenericSubstance gs=new GenericSubstance(); 
					compound.setGenericSubstanceCompound(gsc);
					gsc.setGenericSubstance(gs);
					gs.setDsstoxSubstanceId(rs.getString(8));

					if (rs.getString(9)!=null) {
						gs.setCasrn(rs.getString(9));
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
		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null)\n";
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



/**
 * Writes compounds table to a series of json files in 50K chunks
 */
	void compoundsToJsonFiles() {

		//		DsstoxCompoundServiceImpl compoundService=new DsstoxCompoundServiceImpl();
		//		List<DsstoxCompound>compounds=compoundService.findAll();

		int batchSize=50000;
		int i=0;

		while(true) {

			File file=new File("data/dsstox/json/snapshot_compounds"+(i+1)+".json");

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
	
	static class OutputLine {
		
		DsstoxCompound compound;
		String inchiKeyIndigoNew;
		
		
		OutputLine(DsstoxCompound compound) {
			this.compound=compound;
			
			Inchi inchiIndigo=StructureUtil.toInchiIndigo(compound.getMolFile());
			
			if (compound.getInchi()==null) {
				if (inchiIndigo!=null) {
					compound.setInchi(inchiIndigo.inchi);
					System.out.println(compound.getDsstoxCompoundId()+"\tOriginal inchi was null, but have from indigo");
				} 
			} else {
				compound.setInchi(compound.getInchi().split("\n")[0]);							
			}
			
			if (inchiIndigo!=null) {
				inchiKeyIndigoNew=inchiIndigo.inchiKey;
			} 
		}

		@Override
		public String toString() {
			
			List<String>values=new ArrayList<>();
			
			values.add(compound.getDsstoxCompoundId());
			
			if (compound.getGenericSubstanceCompound()!=null) {
				values.add(compound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId());
			} else {
				values.add("null");
			}
			
			values.add(compound.getSmiles());
			values.add(compound.getInchi());
			values.add(inchiKeyIndigoNew);
			values.add(compound.getIndigoInchikey());
			values.add(compound.getJchemInchikey());
			
			String result="";
			for (int i=0;i<values.size();i++) {
				result+=values.get(i);
				if(i<values.size()-1) result+="\t";
				else result+="\r\n";
			}
			
			return result;
		}
		
		static String getHeader() {
			return "dsstox_compound_id\tdsstox_substance_id\tsmiles\tinchi\tindigo_inchi_key\tindigo_inchi_key_dsstox\tjchem_inchi_key\r\n";
		}
		
	}
	
	
	/**
	 * Writes compounds table to a single tsv file with indigo inchi keys
	 * 
	 */
	void compoundsToInchiKey_File() {

		//		DsstoxCompoundServiceImpl compoundService=new DsstoxCompoundServiceImpl();
		//		List<DsstoxCompound>compounds=compoundService.findAll();

		int batchSize=10000;
		int i=0;

		File file=new File("data/dsstox/tsv/snapshot_compounds_indigo_inchi_key.tsv");
		
		File fileIndigoChanged=new File("data/dsstox/tsv/snapshot_compounds_indigo_inchi_key_changed.tsv");
		
		try {
			
			FileWriter fw=new FileWriter(file);
			fw.write(OutputLine.getHeader());
			
			
			FileWriter fwChanged=new FileWriter(fileIndigoChanged);
			fwChanged.write(OutputLine.getHeader());
						
			while(true) {

				List<DsstoxCompound>compounds=getCompoundsBySQL2(i*batchSize, batchSize);

				if(compounds.size()==0) {
					break;
				} else {

					System.out.println((i+1)+"\t"+compounds.size());

					for (DsstoxCompound compound:compounds) {
						
						if(compound.getGenericSubstanceCompound()==null) continue;//skip if have no SID
						
						
						OutputLine outputLine=new OutputLine(compound); 

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

					System.out.println(filename);

					FileWriter fw=new FileWriter("data/dsstox/sdf/"+filename);

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

					//					if (true) break;


				} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
					e.printStackTrace();
				}
			}


		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}


	public static void main(String[] args) {
		DSSTOX_Compounds_Script d=new DSSTOX_Compounds_Script();
//			d.compoundsToJsonFiles();
//		d.convertJsonsToSDFs();
		
//			d.compoundsToTSV_File();
		
		
		d.compoundsToInchiKey_File();
	}

}





