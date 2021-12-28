package gov.epa.databases.dsstox.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

@Entity
@Table(name="generic_substances")
public class GenericSubstance {
	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;
	
	@Column(name="dsstox_substance_id")
	private String dsstoxSubstanceId;
	
	@Column(name="casrn")
	private String casrn;
	
	@Column(name="preferred_name")
	private String preferredName;
	
	@Column(name="substance_type")
	private String substanceType;
	
	@Column(name="qc_notes")
	private String qcNotes;

	@Column(name="qc_notes_private")
	private String qcNotesPrivate;
	
	@Column(name="source")
	private String source;

	@Column(name="created_at")
	@Generated(value=GenerationTime.INSERT)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@Column(name="updated_at")
	@Generated(value=GenerationTime.ALWAYS)
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;

	@Column(name="created_by")
	private String createdBy;

	@Column(name="updated_by")
	private String updatedBy;
	
	// Table joins
    @OneToOne(mappedBy="genericSubstance")
    private GenericSubstanceCompound genericSubstanceCompound;

	@ManyToOne
	@JoinColumn(name="fk_qc_level_id")
	private QcLevel qcLevel;
    
//    @OneToMany(mappedBy="predecessorGenericSubstance")
//    private List<SubstanceRelationship> successorRelationships;
//    
//    @OneToMany(mappedBy="successorGenericSubstance")
//    private List<SubstanceRelationship> predecessorRelationships;
//    
//    @OneToMany(mappedBy="genericSubstance")
//    private List<OtherCasrn> otherCasrns;
//    
//    @OneToMany(mappedBy="genericSubstance")
//    private List<SourceGenericMapping> sourceGenericMappings;
//    
//    @OneToMany(mappedBy="genericSubstance")
//	  private List<Synonym> synonyms;

	public GenericSubstance() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDsstoxSubstanceId() {
		return dsstoxSubstanceId;
	}

	public void setDsstoxSubstanceId(String dsstoxSubstanceId) {
		this.dsstoxSubstanceId = dsstoxSubstanceId;
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

	public String getQcNotes() {
		return qcNotes;
	}

	public void setQcNotes(String qcNotes) {
		this.qcNotes = qcNotes;
	}

	public String getQcNotesPrivate() {
		return qcNotesPrivate;
	}

	public void setQcNotesPrivate(String qcNotesPrivate) {
		this.qcNotesPrivate = qcNotesPrivate;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
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

	public QcLevel getQcLevel() {
		return qcLevel;
	}

	public void setQcLevel(QcLevel qcLevel) {
		this.qcLevel = qcLevel;
	}
	
}
