package gov.epa.util;

import java.io.BufferedReader;
import java.io.StringReader;

import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV3000Reader;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoException;
import com.epam.indigo.IndigoInchi;
import com.epam.indigo.IndigoObject;

import uk.ac.cam.ch.wwmm.opsin.NameToStructure;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult;

public class StructureUtil {
	
//	private static final Logger opsinLogger = LogManager.getLogger("uk.ac.cam.ch.wwmm.opsin");
	
	
	public static boolean isSalt(IAtomContainer molecule) {
		AtomContainerSet  AtomContainerSet2 = (AtomContainerSet)ConnectivityChecker.partitionIntoMolecules(molecule);
		return AtomContainerSet2.getAtomContainerCount() > 1; 
	}
	
	public static class SimpleOpsinResult {
		public String smiles;
		public String message;
		
		public SimpleOpsinResult(String smiles, String message) {
			this.smiles = smiles;
			this.message = message;
		}
		
		public static SimpleOpsinResult fromOpsinResult(OpsinResult or) {
			String getMessage = or.getMessage();
			String message = (getMessage==null || getMessage.isBlank()) ? null : getMessage;
			
			String getSmiles = or.getSmiles();
			String smiles = (getSmiles==null || getSmiles.isBlank()) ? null : getSmiles;
			
			return new SimpleOpsinResult(smiles, message);
		}
	}
	
	public static String indigoInchikeyFromSmiles(String smiles) throws IndigoException {
		Indigo indigo = new Indigo();
		indigo.setOption("ignore-stereochemistry-errors", true);
		IndigoInchi indigoInchi = new IndigoInchi(indigo);

		try {
		
			IndigoObject molecule = indigo.loadMolecule(smiles);
			String inchi = indigoInchi.getInchi(molecule);
			String inchikey = indigoInchi.getInchiKey(inchi);
		
			return inchikey;
			
		} catch (Exception ex) {
			return null;
		}
	}
	
	public static String indigoInchikey1FromSmiles(String smiles) throws IndigoException {
		Indigo indigo = new Indigo();
		indigo.setOption("ignore-stereochemistry-errors", true);
		IndigoInchi indigoInchi = new IndigoInchi(indigo);

		try {
		
			IndigoObject molecule = indigo.loadMolecule(smiles);
			String inchi = indigoInchi.getInchi(molecule);
			
			
			String inchikey = indigoInchi.getInchiKey(inchi);
			if(inchikey!=null) return inchikey.substring(0,14);
			
		} catch (Exception ex) {
		}

		return null;

	}
	
	public static Double molecularWeight(String smiles) throws IndigoException {
		Indigo indigo = new Indigo();
		indigo.setOption("ignore-stereochemistry-errors", true);

		try {
			IndigoObject molecule = indigo.loadMolecule(smiles);
			return molecule.molecularWeight();
		} catch (Exception ex) {
		}
		return null;

	}
	
	/**
	 * TODO this doesnt work yet
	 * 
	 * @param smiles
	 * @return
	 * @throws IndigoException
	 */
	public static String indigoInchikey1FromSmilesFixedH(String smiles) throws IndigoException {
		Indigo indigo = new Indigo();
		
		indigo.setOption("ignore-stereochemistry-errors", true);
		
//		indigo.setOption("/FixedH",true);
		
		indigo.setOption("inchi-options", "/FixedH");//TODO doesnt work
		
		IndigoInchi indigoInchi = new IndigoInchi(indigo);

		try {
		
			IndigoObject molecule = indigo.loadMolecule(smiles);
			String inchi = indigoInchi.getInchi(molecule);
			
			String inchikey = indigoInchi.getInchiKey(inchi);
			if(inchikey!=null) return inchikey.substring(0,14);
			
		} catch (Exception ex) {
		}

		return null;

	}

	
	public static Double molWeightFromSmiles(String smiles) throws IndigoException {

		Indigo indigo = new Indigo();
		indigo.setOption("ignore-stereochemistry-errors", true);
		
		try {
			IndigoObject molecule = indigo.loadMolecule(smiles);
			return molecule.molecularWeight();
		} catch (Exception ex) {
			return null;
		}
	}
	public static class Inchi {
		public String inchi, inchiKey, inchiKey1,warning; 
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

	public static SimpleOpsinResult opsinSmilesFromChemicalName(String chemicalName) {
		if (chemicalName==null) {
			return null;
		}
		
//		opsinLogger.setLevel(Level.OFF);
		NameToStructure nts = NameToStructure.getInstance();
		OpsinResult or = nts.parseChemicalName(chemicalName);
		return SimpleOpsinResult.fromOpsinResult(or);
	}

	
	public static void main(String[] args) {
		String inchiKey=indigoInchikeyFromSmiles("ClC1=C(Cl)[C@]2(Cl)[C@@H]3[C@@H]4CC(C=C4)[C@@H]3C1(Cl)C2(Cl)Cl");
		System.out.println(inchiKey);
	}
	
	public static IAtomContainer fromMolString(String mol3000) throws Exception {
		
		MDLV3000Reader mr=new MDLV3000Reader();
		StringReader reader = new StringReader(mol3000);
		mr.setReader(new BufferedReader(reader));
		return mr.readMolecule(DefaultChemObjectBuilder.getInstance());
			// TODO Auto-generated catch block
		
	}
	
}
