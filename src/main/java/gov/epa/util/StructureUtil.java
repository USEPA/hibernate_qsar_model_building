package gov.epa.util;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoException;
import com.epam.indigo.IndigoInchi;
import com.epam.indigo.IndigoObject;

import uk.ac.cam.ch.wwmm.opsin.NameToStructure;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult;

public class StructureUtil {
	
//	private static final Logger opsinLogger = LogManager.getLogger("uk.ac.cam.ch.wwmm.opsin");
	
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

		IndigoObject molecule = indigo.loadMolecule(smiles);
		String inchi = indigoInchi.getInchi(molecule);
		String inchikey = indigoInchi.getInchiKey(inchi);
		
		return inchikey;
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
}
