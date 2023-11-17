package gov.epa.run_from_java.scripts.DSSTOX_Loading;


import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.GenericSubstance;
import gov.epa.databases.dsstox.entity.GenericSubstanceCompound;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.formula.MolecularFormula;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
/**
 * @author TMARTI02
 */
public class GetFluorinatedCompounds {

	Connection conn=SqlUtilities.getConnectionDSSTOX();



	//Some have null formula but are ok and have F:
	//mol_formula is null and smiles is not null and smiles not like '%*%' and smiles not like '%|%'




	/**
	 * Writes compounds table to a series of json files in 50K chunks
	 */
	void compoundsToJsonFiles() {



		File file=new File("data/dsstox/json/possible_fluorinated_compounds.json");

		List<DsstoxCompound>compounds=getCompoundsBySQL();

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







	List<DsstoxCompound> getCompoundsBySQL() {

		List<DsstoxCompound>compounds=new ArrayList<>();

		//		String where1="mol_formula is null and smiles is not null and smiles not like '%*%' and smiles not like '%|%'";
		String where1="mol_formula is null";//Probably dont need this condition since these usually have (CF2)n
		String where2="mol_formula like '%F%'";

		String sql="SELECT dsstox_compound_id,mol_file,smiles,mol_formula, gs.dsstox_substance_id, gs.casrn\n";  
		sql+="FROM compounds c\n";
		sql+="left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id\n";
		sql+="left join generic_substances gs on gs.id=gsc.fk_generic_substance_id\n"; 
		//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null) and gs.dsstox_substance_id is not null\n";
		sql+="where ("+where1+") or ("+where2+") \n";
		sql+="ORDER BY dsstox_compound_id\n";

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
					compound.setMolFormula(rs.getString(4));

				if (rs.getString(5)!=null) {
					GenericSubstanceCompound gsc=new GenericSubstanceCompound();
					GenericSubstance gs=new GenericSubstance(); 
					compound.setGenericSubstanceCompound(gsc);
					gsc.setGenericSubstance(gs);
					gs.setDsstoxSubstanceId(rs.getString(5));

					if (rs.getString(6)!=null) {
						gs.setCasrn(rs.getString(6));
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

	void findPossiblePFAS() {

		File file=new File("data/dsstox/json/possible_fluorinated_compounds.json");
		Gson gson=new Gson();
		Type myDataType = new TypeToken<Collection<DsstoxCompound>>(){}.getType();
		Path path = Paths.get(file.getAbsolutePath());

		Reader reader;
		try {
			reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);

			List<DsstoxCompound>compounds=gson.fromJson(reader, myDataType);

			boolean keepMissingSid=false;

			System.out.println(compounds.size());

			for (int i=0;i<compounds.size();i++) {

				DsstoxCompound c=compounds.get(i);

				if(!keepMissingSid && c.getGenericSubstanceCompound()==null) {
					compounds.remove(i--);
					continue;
				}

				if (c.getMolFormula()!=null) {
					i = handleMF(compounds, i, c);
				}else {
					compounds.remove(i--);
//					Ones without formulas in compounds table had repeat units
					
//					if(c.getSmiles()!=null && (c.getSmiles().contains("*") || c.getSmiles().contains("|"))) {
//						compounds.remove(i--);
//					} else if(c.getSmiles()!=null) {
//						i = handleSmiles(compounds, i, c);
//					} else if(c.getMolFile()!=null) { 
////						i = handleMolFile(compounds, i, c);
//					}
				}
			}
			System.out.println(compounds.size());
		
			File fileOut=new File("data/dsstox/json/fluorinated_compounds.json");
			
			FileWriter fw=new FileWriter(fileOut);
			fw.write(Utilities.gson.toJson(compounds));
			fw.flush();
			fw.close();

		
		} catch (IOException e) {
			e.printStackTrace();
		}

	}







	private int handleMolFile(List<DsstoxCompound> compounds, int i, DsstoxCompound c) {
		//						System.out.println(c.getMolFile());
		MDLV3000Reader mr=new MDLV3000Reader();
		InputStream stream = new ByteArrayInputStream(c.getMolFile().getBytes(StandardCharsets.UTF_8));

		try {
			mr.setReader(stream);
			IAtomContainer molecule=null;
			molecule = (IAtomContainer) mr.readMolecule(DefaultChemObjectBuilder.getInstance());

			IMolecularFormula mf=MolecularFormulaManipulator.getMolecularFormula(molecule);
			int countF=MolecularFormulaManipulator.getElementCount(mf, "F");

//			System.out.println(countF);

			if(countF<3) {
				compounds.remove(i--);
			} else {
				System.out.println("Handle molfile:"+compounds.get(i).getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId());
			}


		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
	}







	private int handleSmiles(List<DsstoxCompound> compounds, int i, DsstoxCompound c) {
		//						System.out.println(c.getSmiles());
		try {
			SmilesParser   sp  = new SmilesParser(SilentChemObjectBuilder.getInstance());
			IAtomContainer molecule   = sp.parseSmiles(c.getSmiles());
			IMolecularFormula mf=MolecularFormulaManipulator.getMolecularFormula(molecule);
			int countF=MolecularFormulaManipulator.getElementCount(mf, "F");
			
			if(countF<3) {
				compounds.remove(i--);
			} else {
				System.out.println("Handle smiles:"+compounds.get(i).getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId());
			}

		} catch (InvalidSmilesException e) {
			System.err.println(e.getMessage());
		}
		return i;
	}







	private int handleMF(List<DsstoxCompound> compounds, int i, DsstoxCompound c) {
		try {
			IMolecularFormula mf=MolecularFormulaManipulator.getMolecularFormula(c.getMolFormula(), DefaultChemObjectBuilder.getInstance());
			int countF=MolecularFormulaManipulator.getElementCount(mf, "F");
			//						System.out.println(c.getMolFormula()+"\t"+countF);
			if(countF<3) compounds.remove(i--);					
		} catch (Exception ex) {
			//						System.out.println("couldnt handle\t"+c.getMolFormula()+"\t"+c.getSmiles());
			compounds.remove(i--);
		}
		return i;
	}

	void convertJsonToSDF() {

		try {
			Type listOfMyClassObject = new TypeToken<List<DsstoxCompound>>() {}.getType();
			
			File fileIn=new File("data/dsstox/json/fluorinated_compounds.json");
			List<DsstoxCompound>compounds=Utilities.gson.fromJson(new FileReader(fileIn), listOfMyClassObject);

			String filename=fileIn.getName().replace(".json", ".sdf");

			System.out.println(filename);

			FileWriter fw=new FileWriter("data/dsstox/sdf/"+filename);

			System.out.println(compounds.size());
			
			for (DsstoxCompound compound:compounds) {

				
				if (compound.getMolFile()!=null) {
					fw.write(compound.getMolFile());	
				} else {
					System.out.println(compound.getSmiles());
				}
				
				
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


		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	public static void main(String[] args) {
		GetFluorinatedCompounds d=new GetFluorinatedCompounds();
		d.compoundsToJsonFiles();
		d.findPossiblePFAS();
		d.convertJsonToSDF();
	}

}
