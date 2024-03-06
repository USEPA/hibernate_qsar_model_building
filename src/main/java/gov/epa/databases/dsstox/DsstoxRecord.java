package gov.epa.databases.dsstox;

import java.util.Iterator;
import java.util.Set;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;

import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.GenericSubstance;
import gov.epa.databases.dsstox.entity.GenericSubstanceCompound;
import gov.epa.databases.dsstox.entity.SourceGenericSubstanceMapping;
import gov.epa.databases.dsstox.entity.SourceSubstance;
import gov.epa.databases.dsstox.entity.SourceSubstanceIdentifier;
import gov.epa.databases.dsstox.service.SourceSubstanceServiceImpl;
import gov.epa.endpoints.datasets.ExplainedResponse;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

public class DsstoxRecord {
	public String dsstoxRecordId;
	public String externalId;
	public String connectionReason;
	public Double linkageScore;
	public Boolean curatorValidated;
	public String dsstoxSubstanceId;
	public String dsstoxCompoundId;
	public String casrn;
	public String casrnOther;//TMM
	public String preferredName;
	public String substanceType;
	public String smiles;
	public Double molWeight;
	public String qsarReadySmiles;
	public String msReadySmiles;
	public String synonymQuality;
	
	static SourceSubstanceServiceImpl sss=new SourceSubstanceServiceImpl();

	
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
	
	

	/**
	 * Assembles dsstoxRecord from the rid
	 * 
	 * @param dtxrid
	 * @return
	 */
	public static DsstoxRecord getDsstoxRecordByDTXRID(String dtxrid) {
		
		DsstoxRecord dr=new DsstoxRecord();	
		
		SourceSubstance sourceSubstance=sss.findByDtxrid(dtxrid);
		
		SourceGenericSubstanceMapping sgsm=sourceSubstance.getSourceGenericSubstanceMapping();
		GenericSubstance gs=sgsm.getGenericSubstance();
		GenericSubstanceCompound gsc=gs.getGenericSubstanceCompound();		
		DsstoxCompound c=gsc.getCompound();
		
		dr.setDsstoxCompoundId(c.getDsstoxCompoundId());
		dr.setDsstoxSubstanceId(gs.getDsstoxSubstanceId());

		dr.setCasrn(gs.getCasrn());
		dr.setPreferredName(gs.getPreferredName());
		dr.setDsstoxRecordId(dtxrid);
		
		dr.setSmiles(gsc.getCompound().getSmiles());
		
		dr.setMolWeight(c.getMolWeight());
		dr.setSubstanceType(gs.getSubstanceType());
		
		dr.setConnectionReason(sgsm.getConnectionReason());
		dr.setCuratorValidated(sgsm.getCuratorValidated());
		
//		SourceChemical sc=new SourceChemical();
//		fill_in_identifiers(sc, sourceSubstance);
//		System.out.println(Utilities.gson.toJson(sc));
//		System.out.println(Utilities.gson.toJson(dr));
		
//		public String externalId;
//		public Double linkageScore;
//		public String casrnOther;//TMM
//		public String qsarReadySmiles;
//		public String msReadySmiles;
//		public String synonymQuality;
		
		
		return dr;
	}
	
	
	/**
	 * Assembles dsstoxRecord from the rid
	 * 
	 * @param dtxrid
	 * @return
	 */
	public static DsstoxRecord getDsstoxRecord(SourceChemical sc) {
		
		DsstoxRecord dr=new DsstoxRecord();	
		
		SourceSubstanceServiceImpl sss=new SourceSubstanceServiceImpl();
		SourceSubstance sourceSubstance=sss.findByDtxrid(sc.getSourceDtxrid());
		
		SourceGenericSubstanceMapping sgsm=sourceSubstance.getSourceGenericSubstanceMapping();
		GenericSubstance gs=sgsm.getGenericSubstance();
		GenericSubstanceCompound gsc=gs.getGenericSubstanceCompound();
		
		if(gsc==null) return null;
		
		DsstoxCompound c=gsc.getCompound();
		
		dr.setDsstoxCompoundId(c.getDsstoxCompoundId());
		dr.setDsstoxSubstanceId(gs.getDsstoxSubstanceId());

		dr.setCasrn(gs.getCasrn());
		dr.setPreferredName(gs.getPreferredName());
		dr.setDsstoxRecordId(sc.getSourceDtxrid());
		
		dr.setSmiles(gsc.getCompound().getSmiles());
		
		dr.setMolWeight(c.getMolWeight());
		dr.setSubstanceType(gs.getSubstanceType());
		
		dr.setConnectionReason(sgsm.getConnectionReason());
		dr.setCuratorValidated(sgsm.getCuratorValidated());
		
		fill_in_identifiers(sc, sourceSubstance);
		
//		System.out.println(Utilities.gson.toJson(sc));
//		System.out.println(Utilities.gson.toJson(dr));
		
//		public String externalId;
//		public Double linkageScore;
//		public String casrnOther;//TMM
//		public String qsarReadySmiles;
//		public String msReadySmiles;
//		public String synonymQuality;
		
		
		return dr;
	}
	
	public static void fill_in_identifiers(SourceChemical sc, SourceSubstance sourceSubstance) {
		
		for (SourceSubstanceIdentifier ssi:sourceSubstance.getSourceSubstanceIdentifiers()) {
				String identifier = ssi.getIdentifier();
				switch (ssi.getIdentifierType()) {
				case "DTXSID":
					sc.setSourceDtxsid(identifier);
					break;
				case "NAME":
					if (sc.getSourceChemicalName()!=null) {
						sc.setSourceChemicalName(sc.getSourceChemicalName()+"; "+identifier);
					} else {
						sc.setSourceChemicalName(identifier);	
					}
//					System.out.println(identifier);
					break;
				case "CASRN":
					sc.setSourceCasrn(identifier);
					break;
				case "SMILES":
				case "STRUCTURE":
					sc.setSourceSmiles(identifier);
					break;
				}
			}
		}
	
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
		
		if(smiles.contains("*")) {
			return new ExplainedResponse(false, "UVCB");
		}
		
		
		try {
			AtomContainer molecule = (AtomContainer) DsstoxSession.smilesParser.parseSmiles(smiles.trim());
			
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
			while (atoms.hasNext() && !(containsCarbon && containsUnacceptableAtom)) {
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
//			e.printStackTrace();
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
				AtomContainer ac = (AtomContainer) DsstoxSession.smilesParser.parseSmiles(smiles);
				DsstoxSession.depictionGenerator.depict(ac);
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
	
	
	public static void main(String[] args) {
//		String rid="DTXRID2015044697";//only has CAS
//		String rid="DTXRID8016290872";//name, cas
//		String rid="DTXRID5016289776";
//		String rid="DTXRID805797238";//name
//		String rid="DTXRID405797250";

		DsstoxRecord dr=getDsstoxRecordByDTXRID("DTXRID2020689619");
		System.out.println(Utilities.gson.toJson(dr)+"\n");

		DsstoxRecord dr2=getDsstoxRecordByDTXRID("DTXRID405797179");
		System.out.println(Utilities.gson.toJson(dr2));
		
		
		
		
		
		
		//DOES all of the chemprop data already have a DTXCID mapping? or just RID and identifiers? how many are validated or just have a default mapping??
	}

}