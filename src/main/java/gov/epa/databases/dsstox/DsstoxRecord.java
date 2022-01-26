package gov.epa.databases.dsstox;

import java.util.Iterator;
import java.util.Set;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.smiles.SmilesParser;

import gov.epa.endpoints.datasets.classes.ExplainedResponse;

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
	
	private static final SmilesParser parser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
	private static final DepictionGenerator generator = new DepictionGenerator();
	
	public ExplainedResponse validateStructure(boolean omitSalts, Set<String> acceptableAtoms) {
		if (substanceType==null || substanceType.isBlank()
				|| substanceType.equals("Mineral/Composite")
				|| substanceType.equals("Mixture/Formulation")
				|| substanceType.equals("Polymer")) {
			return new ExplainedResponse(false, "Bad substance type: " + substanceType);
		}
		
		if (smiles==null || smiles.isBlank() || smiles.toLowerCase().equals("null")) {
			return new ExplainedResponse(false, "Missing SMILES");
		}
		
		if (omitSalts && smiles.contains(".")) {
			return new ExplainedResponse(false, "Omitted salt");
		}
		
		try {
			AtomContainer molecule = (AtomContainer) parser.parseSmiles(smiles.trim());
			
			if (molecule==null || molecule.getAtomCount() <= 1) {
				return new ExplainedResponse(false, "Single atom");
			}
			
			if (countOrganicFragments(molecule) > 1) {
				return new ExplainedResponse(false, "Multiple organic fragments");
			}
			
			String reason = null;
			boolean containsCarbon = false;
			boolean containsUnacceptableAtom = false;
			Iterator<IAtom> atoms = molecule.atoms().iterator();
			while (atoms.hasNext() && !(containsCarbon || containsUnacceptableAtom)) {
				IAtom atom = atoms.next();
				String symbol = atom.getSymbol();
				
				if (symbol.equals("C")) {
					containsCarbon = true;
				}
				
				if (!acceptableAtoms.contains(symbol)) {
					reason = "Unacceptable atom";
					containsUnacceptableAtom = true;
				}
			}
			
			if (!containsCarbon && !containsUnacceptableAtom) {
				reason = "Inorganic";
			}
			
			boolean valid = containsCarbon && !containsUnacceptableAtom;
			if (valid) {
				return new ExplainedResponse(true, "Valid structure for QSAR");
			} else {
				return new ExplainedResponse(false, reason);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ExplainedResponse(false, "Structure validation failed: CDK SMILES parsing failed");
		}
	}
	
	public int countOrganicFragments(AtomContainer molecule) {
		int count = 0;
		AtomContainerSet moleculeSet = (AtomContainerSet) ConnectivityChecker.partitionIntoMolecules(molecule);
		for (int i=0; i < moleculeSet.getAtomContainerCount(); i++) {
			AtomContainer fragment = (AtomContainer) moleculeSet.getAtomContainer(i);
			boolean isOrganic = false;
			for (int j=0; j < fragment.getAtomCount(); j++) {
				if (fragment.getAtom(j).getSymbol().equals("C")) {
					isOrganic = true;
					break;
				}
			}
			if (isOrganic) count++;
		}

		return count;
	}
	
	public boolean isWellDefined() {
		if (smiles==null || smiles.isBlank() || smiles.toLowerCase().equals("null")) {
			return false;
		} else {
			try {
				AtomContainer ac = (AtomContainer) parser.parseSmiles(smiles);
				generator.depict(ac);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}

	public String getDsstoxRecordId() {
		return dsstoxRecordId;
	}

	public void setDsstoxRecordId(String dsstoxRecordId) {
		this.dsstoxRecordId = dsstoxRecordId;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getConnectionReason() {
		return connectionReason;
	}

	public void setConnectionReason(String connectionReason) {
		this.connectionReason = connectionReason;
	}

	public Double getLinkageScore() {
		return linkageScore;
	}

	public void setLinkageScore(Double linkageScore) {
		this.linkageScore = linkageScore;
	}

	public Boolean getCuratorValidated() {
		return curatorValidated;
	}

	public void setCuratorValidated(Boolean curatorValidated) {
		this.curatorValidated = curatorValidated;
	}

	public String getDsstoxSubstanceId() {
		return dsstoxSubstanceId;
	}

	public void setDsstoxSubstanceId(String dsstoxSubstanceId) {
		this.dsstoxSubstanceId = dsstoxSubstanceId;
	}

	public String getDsstoxCompoundId() {
		return dsstoxCompoundId;
	}

	public void setDsstoxCompoundId(String dsstoxCompoundId) {
		this.dsstoxCompoundId = dsstoxCompoundId;
	}

	public String getCasrn() {
		return casrn;
	}

	public void setCasrn(String casrn) {
		this.casrn = casrn;
	}

	public String getPreferredName() {
		return preferredName;
	}

	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}

	public String getSubstanceType() {
		return substanceType;
	}

	public void setSubstanceType(String substanceType) {
		this.substanceType = substanceType;
	}

	public String getSmiles() {
		return smiles;
	}

	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}

	public Double getMolWeight() {
		return molWeight;
	}

	public void setMolWeight(Double molWeight) {
		this.molWeight = molWeight;
	}

	public String getQsarReadySmiles() {
		return qsarReadySmiles;
	}

	public void setQsarReadySmiles(String qsarReadySmiles) {
		this.qsarReadySmiles = qsarReadySmiles;
	}

	public String getMsReadySmiles() {
		return msReadySmiles;
	}

	public void setMsReadySmiles(String msReadySmiles) {
		this.msReadySmiles = msReadySmiles;
	}

	public String getSynonymQuality() {
		return synonymQuality;
	}

	public void setSynonymQuality(String synonymQuality) {
		this.synonymQuality = synonymQuality;
	}
}