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

	public static SimpleOpsinResult opsinSmilesFromChemicalName(String chemicalName) {
		if (chemicalName==null) {
			return null;
		}
		
//		opsinLogger.setLevel(Level.OFF);
		NameToStructure nts = NameToStructure.getInstance();
		OpsinResult or = nts.parseChemicalName(chemicalName);
		return SimpleOpsinResult.fromOpsinResult(or);
	}

}
