package gov.epa.databases.dsstox.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import com.google.gson.annotations.SerializedName;

@Entity
@Table(name="compounds")
public class DsstoxCompound {
	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;
	
	@Column(name="dsstox_compound_id")
	private String dsstoxCompoundId;

	@Column(name="acd_index_name")
	private String acdIndexName;

	@Column(name="acd_iupac_name")
	private String acdIupacName;

	@Column(name="chebi_id")
	private String chebiId;

	@Column(name="chemical_type")
	private String chemicalType;

	@Column(name="chemspider_id")
	private Long chemspiderId;

	@Column(name="chiral_stereo")
	private String chiralStereo;

	@Column(name="double_stereo")
	private String dblStereo;

	@Column(name="fragment_count")
	private Long fragmentCount;

	@Column(name="has_defined_isotope")
	private Boolean hasDefinedIsotope;

	@Column(name="inchi")
	private String inchi;

	
	@Column(name="jchem_inchi_key")
	private String jchemInchikey;

	
	@Column(name="indigo_inchi_key")
	private String indigoInchikey;

	
	@Column(name="jchem_inchi")
	private String jchemInchi;

	@Column(name="indigo_inchi")
	private String indigoInchi;

	
	@Lob @Basic(fetch=FetchType.LAZY)
	@Column(name="mol_file")
	private String molFile;

	@Lob @Basic(fetch=FetchType.LAZY)
	@Column(name="mol_file_3d")
	private String molFile3d;

	@Column(name="mol_formula")
	private String molFormula;

	@Lob @Basic(fetch=FetchType.LAZY)
	@Column(name="mol_image_png")
	private byte[] molImage;

	
//	@Column(name="mol_image_png_available")
	@Transient
	private boolean molImagePNGAvailable;

	@Column(name="mol_weight")
	private Double molWeight;

	@Column(name="monoisotopic_mass")
	private Double monoMass;
	
	@Column(name="mrv_file")
	private String mrvFile;

	@Column(name="organic_form")
	private String organicForm;

	@Column(name="pubchem_cid")
	private Long pubchemCid;

	@Column(name="pubchem_iupac_name")
	private Long pubchemIupacName;

	@Column(name="radical_count")
	private Long radicalCount;
	
	@Column(name="smiles")
	private String smiles;

	@Column(name="has_stereochemistry")
	private Boolean hasStereochemistry;

	@Column(name="pubchem_sources")
	private Long pubchemSources;

	@Column(name="created_at")
	@Generated(value=GenerationTime.INSERT)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	@Column(name="updated_at")
	@Generated(value=GenerationTime.ALWAYS)
	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedDate;

	@Column(name="created_by")
	private String createdBy;

	@Column(name="updated_by")
	private String updatedBy;
		
	// Table joins
	@OneToOne(mappedBy="compound")
	private GenericSubstanceCompound genericSubstanceCompound;

//	@OneToMany(mappedBy="compound")
//	private List<ChemPropPredictedProperty> chemPropPredictedProperties;
	
    @OneToMany(mappedBy="predecessorCompound")
    private List<CompoundRelationship> successorRelationships;
    
    @OneToMany(mappedBy="successorCompound")
    private List<CompoundRelationship> predecessorRelationships;
	
	public DsstoxCompound() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDsstoxCompoundId() {
		return dsstoxCompoundId;
	}

	public void setDsstoxCompoundId(String dsstoxCompoundId) {
		this.dsstoxCompoundId = dsstoxCompoundId;
	}

	public String getAcdIndexName() {
		return acdIndexName;
	}

	public void setAcdIndexName(String acdIndexName) {
		this.acdIndexName = acdIndexName;
	}

	public String getAcdIupacName() {
		return acdIupacName;
	}

	public void setAcdIupacName(String acdIupacName) {
		this.acdIupacName = acdIupacName;
	}

	public String getChebiId() {
		return chebiId;
	}

	public void setChebiId(String chebiId) {
		this.chebiId = chebiId;
	}

	public String getChemicalType() {
		return chemicalType;
	}

	public void setChemicalType(String chemicalType) {
		this.chemicalType = chemicalType;
	}

	public Long getChemspiderId() {
		return chemspiderId;
	}

	public void setChemspiderId(Long chemspiderId) {
		this.chemspiderId = chemspiderId;
	}

	public String getChiralStereo() {
		return chiralStereo;
	}

	public void setChiralStereo(String chiralStereo) {
		this.chiralStereo = chiralStereo;
	}

	public String getDblStereo() {
		return dblStereo;
	}

	public void setDblStereo(String dblStereo) {
		this.dblStereo = dblStereo;
	}

	public Long getFragmentCount() {
		return fragmentCount;
	}

	public void setFragmentCount(Long fragmentCount) {
		this.fragmentCount = fragmentCount;
	}

	public Boolean getHasDefinedIsotope() {
		return hasDefinedIsotope;
	}

	public void setHasDefinedIsotope(Boolean hasDefinedIsotope) {
		this.hasDefinedIsotope = hasDefinedIsotope;
	}

	public String getInchi() {
		return inchi;
	}

	public void setInchi(String inchi) {
		this.inchi = inchi;
	}

	public String getJchemInchikey() {
		return jchemInchikey;
	}

	public void setJchemInchikey(String jchemInchikey) {
		this.jchemInchikey = jchemInchikey;
	}

	public String getIndigoInchikey() {
		return indigoInchikey;
	}

	public void setIndigoInchikey(String indigoInchikey) {
		this.indigoInchikey = indigoInchikey;
	}

	public String getMolFile() {
		return molFile;
	}

	public void setMolFile(String molFile) {
		this.molFile = molFile;
	}

	public String getMolFile3d() {
		return molFile3d;
	}

	public void setMolFile3d(String molFile3d) {
		this.molFile3d = molFile3d;
	}

	public String getMolFormula() {
		return molFormula;
	}

	public void setMolFormula(String molFormula) {
		this.molFormula = molFormula;
	}

	public byte[] getMolImage() {
		return molImage;
	}

	public void setMolImage(byte[] molImage) {
		this.molImage = molImage;
	}

	public Double getMolWeight() {
		return molWeight;
	}

	public void setMolWeight(Double molWeight) {
		this.molWeight = molWeight;
	}

	public Double getMonoMass() {
		return monoMass;
	}

	public void setMonoMass(Double monoMass) {
		this.monoMass = monoMass;
	}

	public String getMrvFile() {
		return mrvFile;
	}

	public void setMrvFile(String mrvFile) {
		this.mrvFile = mrvFile;
	}

	public String getOrganicForm() {
		return organicForm;
	}

	public void setOrganicForm(String organicForm) {
		this.organicForm = organicForm;
	}

	public Long getPubchemCid() {
		return pubchemCid;
	}

	public void setPubchemCid(Long pubchemCid) {
		this.pubchemCid = pubchemCid;
	}

	public Long getPubchemIupacName() {
		return pubchemIupacName;
	}

	public void setPubchemIupacName(Long pubchemIupacName) {
		this.pubchemIupacName = pubchemIupacName;
	}

	public Long getRadicalCount() {
		return radicalCount;
	}

	public void setRadicalCount(Long radicalCount) {
		this.radicalCount = radicalCount;
	}

	public String getSmiles() {
		return smiles;
	}

	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}

	public Boolean getHasStereochemistry() {
		return hasStereochemistry;
	}

	public void setHasStereochemistry(Boolean hasStereochemistry) {
		this.hasStereochemistry = hasStereochemistry;
	}

	public Long getPubchemSources() {
		return pubchemSources;
	}

	public void setPubchemSources(Long pubchemSources) {
		this.pubchemSources = pubchemSources;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public GenericSubstanceCompound getGenericSubstanceCompound() {
		return genericSubstanceCompound;
	}

	public void setGenericSubstanceCompound(GenericSubstanceCompound genericSubstanceCompound) {
		this.genericSubstanceCompound = genericSubstanceCompound;
	}

	public List<CompoundRelationship> getSuccessorRelationships() {
		return successorRelationships;
	}

	public void setSuccessorRelationships(List<CompoundRelationship> successorRelationships) {
		this.successorRelationships = successorRelationships;
	}

	public List<CompoundRelationship> getPredecessorRelationships() {
		return predecessorRelationships;
	}

	public void setPredecessorRelationships(List<CompoundRelationship> predecessorRelationships) {
		this.predecessorRelationships = predecessorRelationships;
	}

	public String getJchemInchi() {
		return jchemInchi;
	}

	public void setJchemInchi(String jchemInchi) {
		this.jchemInchi = jchemInchi;
	}

	public String getIndigoInchi() {
		return indigoInchi;
	}

	public void setIndigoInchi(String indigoInchi) {
		this.indigoInchi = indigoInchi;
	}

	public boolean isMolImagePNGAvailable() {
		return molImagePNGAvailable;
	}

	public void setMolImagePNGAvailable(boolean molImagePNGAvailable) {
		this.molImagePNGAvailable = molImagePNGAvailable;
	}



}
