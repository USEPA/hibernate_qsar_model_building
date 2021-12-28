package gov.epa.databases.dsstox;

//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.HashSet;
//import java.util.Iterator;
//
//import org.openscience.cdk.AtomContainer;
//import org.openscience.cdk.AtomContainerSet;
//import org.openscience.cdk.DefaultChemObjectBuilder;
//import org.openscience.cdk.depict.DepictionGenerator;
//import org.openscience.cdk.exception.CDKException;
//import org.openscience.cdk.graph.ConnectivityChecker;
//import org.openscience.cdk.interfaces.IAtom;
//import org.openscience.cdk.smiles.SmilesParser;
//
//import gov.epa.dev_qsar.dataset_creation.ExplainedResponse;
//import gov.epa.dev_qsar.util.StructureUtil;

public class DsstoxRecord {
	public String dsstoxRecordId;
	public String externalId;
	public String connectionReason;
	public Double linkageScore;
	public Boolean curatorValidated;
	public String dsstoxSubstanceId;
	public String dsstoxCompoundId;
	public String casrn;
	public String preferredName;
	public String substanceType;
	public String smiles;
	public Double molWeight;
	public String qsarReadySmiles;
	public String msReadySmiles;
	public String synonymQuality;
	
	public DsstoxRecord() {}
	
	public DsstoxRecord(String dsstoxSubstanceId, String dsstoxCompoundId, String casrn, String preferredName, String substanceType, 
			String smiles, Double molWeight, String qsarReadySmiles) {
		this.dsstoxSubstanceId = dsstoxSubstanceId;
		this.dsstoxCompoundId = dsstoxCompoundId;
		this.casrn = casrn;
		this.preferredName = preferredName;
		this.substanceType = substanceType;
		this.smiles = smiles;
		this.molWeight = molWeight;
		this.qsarReadySmiles = qsarReadySmiles;
	}
	
//	private static final SmilesParser parser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
//	private static final DepictionGenerator generator = new DepictionGenerator();
//	
//	public ExplainedResponse validateStructure(boolean omitSalts, HashSet<String> acceptableAtoms) {
//		if (substanceType==null || substanceType.isBlank()
//				|| substanceType.equals("Mineral/Composite")
//				|| substanceType.equals("Mixture/Formulation")
//				|| substanceType.equals("Polymer")) {
//			return new ExplainedResponse(false, "Bad substance type: " + substanceType);
//		}
//		
//		if (smiles==null || smiles.isBlank() || smiles.toLowerCase().equals("null")) {
//			return new ExplainedResponse(false, "Missing SMILES");
//		}
//		
//		if (omitSalts && smiles.contains(".")) {
//			return new ExplainedResponse(false, "Omitted salt");
//		}
//		
//		try {
//			AtomContainer molecule = (AtomContainer) parser.parseSmiles(smiles.trim());
//			
//			if (molecule==null || molecule.getAtomCount() <= 1) {
//				return new ExplainedResponse(false, "Single atom");
//			}
//			
//			if (countOrganicFragments(molecule) > 1) {
//				return new ExplainedResponse(false, "Multiple organic fragments");
//			}
//			
//			String reason = null;
//			boolean containsCarbon = false;
//			boolean containsUnacceptableAtom = false;
//			Iterator<IAtom> atoms = molecule.atoms().iterator();
//			while (atoms.hasNext() && !(containsCarbon || containsUnacceptableAtom)) {
//				IAtom atom = atoms.next();
//				String symbol = atom.getSymbol();
//				
//				if (symbol.equals("C")) {
//					containsCarbon = true;
//				}
//				
//				if (!acceptableAtoms.contains(symbol)) {
//					reason = "Unacceptable atom";
//					containsUnacceptableAtom = true;
//				}
//			}
//			
//			if (!containsCarbon && !containsUnacceptableAtom) {
//				reason = "Inorganic";
//			}
//			
//			boolean valid = containsCarbon && !containsUnacceptableAtom;
//			if (valid) {
//				return new ExplainedResponse(true, "Valid structure for QSAR");
//			} else {
//				return new ExplainedResponse(false, reason);
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return new ExplainedResponse(false, "Structure validation failed: CDK SMILES parsing failed");
//		}
//	}
//	
//	public int countOrganicFragments(AtomContainer molecule) {
//		int count = 0;
//		AtomContainerSet moleculeSet = (AtomContainerSet) ConnectivityChecker.partitionIntoMolecules(molecule);
//		for (int i=0; i < moleculeSet.getAtomContainerCount(); i++) {
//			AtomContainer fragment = (AtomContainer) moleculeSet.getAtomContainer(i);
//			boolean isOrganic = false;
//			for (int j=0; j < fragment.getAtomCount(); j++) {
//				if (fragment.getAtom(j).getSymbol().equals("C")) {
//					isOrganic = true;
//					break;
//				}
//			}
//			if (isOrganic) count++;
//		}
//
//		return count;
//	}
//	
//	public boolean isWellDefined() {
//		if (smiles==null || smiles.isBlank() || smiles.toLowerCase().equals("null")) {
//			return false;
//		} else {
//			try {
//				String inchikey = StructureUtil.indigoInchikeyFromSmiles(smiles);
//				AtomContainer ac = (AtomContainer) parser.parseSmiles(smiles);
//				generator.depict(ac);
//				return true;
//			} catch (Exception e) {
//				return false;
//			}
//		}
//	}
}