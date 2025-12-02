package gov.epa.databases.dsstox.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.UpdateTimestamp;


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
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@Column(name="updated_at")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;

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
