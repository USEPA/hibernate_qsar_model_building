package gov.epa.databases.dsstox.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

@Entity
@Table(name="source_substances")
public class SourceSubstance {
	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;
	
	@Column(name="dsstox_record_id")
	private String dsstoxRecordId;
	
    @OneToOne(mappedBy="sourceSubstance")
    private SourceGenericSubstanceMapping sourceGenericSubstanceMapping;
	
	@ManyToOne
	@JoinColumn(name="fk_chemical_list_id")
	private ChemicalList chemicalList;

    @OneToMany(mappedBy="sourceSubstance", fetch=FetchType.EAGER)
    private List<SourceSubstanceIdentifier> sourceSubstanceIdentifiers;

//    @OneToMany(mappedBy="sourceSubstance")
//    private List<ChemPropMeasuredProperty> chemPropMeasuredProperties;

//    @OneToOne(mappedBy="sourceSubstance")
//    private ChemTrackCoaSummary chemTrackCoaSummary;
	
	@Column(name="external_id")
	private String externalId;
	
	@Column(name="warnings")
    private String warnings;

    @Column(name="qc_notes")
    private String qcNotes;

	@Column(name="created_at")
	@Generated(value=GenerationTime.INSERT)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	
	@Column(name="updated_at")
	@Generated(value=GenerationTime.ALWAYS)
	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedAt;

	@Column(name="created_by")
	private String createdBy;

	@Column(name="updated_by")
	private String updatedBy;
	
	public SourceSubstance() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDsstoxRecordId() {
		return dsstoxRecordId;
	}

	public void setDsstoxRecordId(String dsstoxRecordId) {
		this.dsstoxRecordId = dsstoxRecordId;
	}

	public SourceGenericSubstanceMapping getSourceGenericSubstanceMapping() {
		return sourceGenericSubstanceMapping;
	}

	public void setSourceGenericSubstanceMapping(SourceGenericSubstanceMapping sourceGenericSubstanceMapping) {
		this.sourceGenericSubstanceMapping = sourceGenericSubstanceMapping;
	}

	public ChemicalList getChemicalList() {
		return chemicalList;
	}

	public void setChemicalList(ChemicalList chemicalList) {
		this.chemicalList = chemicalList;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getWarnings() {
		return warnings;
	}

	public void setWarnings(String warnings) {
		this.warnings = warnings;
	}

	public String getQcNotes() {
		return qcNotes;
	}

	public void setQcNotes(String qcNotes) {
		this.qcNotes = qcNotes;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(Date modifiedAt) {
		this.modifiedAt = modifiedAt;
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

	public List<SourceSubstanceIdentifier> getSourceSubstanceIdentifiers() {
		return sourceSubstanceIdentifiers;
	}

	public void setSourceSubstanceIdentifiers(List<SourceSubstanceIdentifier> sourceSubstanceIdentifiers) {
		this.sourceSubstanceIdentifiers = sourceSubstanceIdentifiers;
	}
	
}
